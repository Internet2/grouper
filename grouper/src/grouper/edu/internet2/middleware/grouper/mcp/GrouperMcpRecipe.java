/*******************************************************************************
 * Copyright 2024 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.mcp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.subject.Subject;

/**
 * An MCP recipe: a named piece of guidance which tells an AI client how this institution wants
 * a task done, in the client's own terms.  Recipes are configuration, not database rows, so
 * they cascade the way every other Grouper config does: Grouper can ship built-in recipes in
 * grouper.base.properties, an institution overrides one by setting the same keys, and setting
 * enabled to false turns one off without deleting it.
 *
 * <p>Config shape, one block per recipe:</p>
 *
 * <pre>
 * grouperMcpRecipe.&lt;configId&gt;.enabled          = true
 * grouperMcpRecipe.&lt;configId&gt;.name             = policy-groups
 * grouperMcpRecipe.&lt;configId&gt;.summary          = one line stating the rule itself
 * grouperMcpRecipe.&lt;configId&gt;.body             = the detailed task
 * grouperMcpRecipe.&lt;configId&gt;.groupNameCanUse  = group whose members may see and read it
 * grouperMcpRecipe.&lt;configId&gt;.groupNameCanEdit = group whose members may edit it over MCP
 * grouperMcpRecipe.&lt;configId&gt;.toolNames        = group_save, group_delete
 * </pre>
 *
 * <p>A blank groupNameCanUse means nobody can use the recipe, and a blank groupNameCanEdit
 * means nobody owns its content.  Blank is not "everyone": a recipe steers what an agent does on
 * behalf of other people, so a field left empty must fail closed.  The same is true of the two
 * global administration groups, which grant nobody until a deployer names a group, and there is
 * no wheel or root fallback.</p>
 *
 * <p>Editing a recipe's content over MCP also needs grouper.mcp.recipe.allowEditInMcp, which is
 * off by default: the delegated edit screen is how an owner edits their own recipe until a
 * deployer turns that path on.</p>
 *
 * <p>The groups are resolved in a root session on purpose.  A recipe's audience is often a
 * group the audience itself cannot view, and being unable to see the group which grants you
 * something should not be the same as not being granted it.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpRecipe {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpRecipe.class);

  /** config prefix for all recipe properties */
  public static final String CONFIG_PREFIX = "grouperMcpRecipe.";

  /** config key of the group whose members administer recipes in the UI, granting nothing over MCP */
  public static final String CONFIG_GROUP_CAN_ADMIN_IN_UI = "grouper.mcp.recipe.groupNameCanAdminInUi";

  /** config key of the group whose members can read and update any recipe over MCP */
  public static final String CONFIG_GROUP_CAN_ADMIN_IN_MCP = "grouper.mcp.recipe.groupNameCanAdminInMcp";

  /** config key of the switch which turns the per recipe groupNameCanEdit delegation on for MCP */
  public static final String CONFIG_ALLOW_EDIT_IN_MCP = "grouper.mcp.recipe.allowEditInMcp";

  /** config key of how long recipe lookups are cached, in seconds.  0 turns caching off */
  public static final String CONFIG_CACHE_SECONDS = "grouper.mcp.recipe.cacheSeconds";

  /** config key of the most entries the cache holds before it is emptied */
  public static final String CONFIG_CACHE_MAX_SIZE = "grouper.mcp.recipe.cacheMaxSize";

  /**
   * one cached answer and when it goes stale
   */
  private static class CacheEntry {

    /** when this entry stops being used, millis since 1970 */
    private long expiresMillis;

    /** the cached answer */
    private Object value;
  }

  /**
   * Recipe lookups are cached per node.  They are on two hot paths: tools/list, which runs on
   * every client connect, and the Miscellaneous screen, which asks whether to show the recipes
   * link for every user who loads it.  Both would otherwise parse the config, build the tool
   * map, and resolve group memberships every time.
   *
   * <p>Expiry is by time alone, with no cross node invalidation, which is why a membership
   * change takes up to the TTL to be seen and why removing somebody from a groupNameCanUse
   * group leaves them able to read that recipe until then.  That is a deliberate trade for
   * guidance text.  Writes made through this class do clear the cache on the node which made
   * them, so somebody who edits a recipe and reads it straight back sees their own change.</p>
   */
  private static final Map<String, CacheEntry> recipeCache =
      new java.util.concurrent.ConcurrentHashMap<String, CacheEntry>();

  /**
   * read a cached answer
   * @param key the cache key
   * @return the cached value, or null when absent, stale, or caching is off
   */
  private static Object cacheGet(String key) {

    if (cacheSeconds() <= 0) {
      return null;
    }

    CacheEntry cacheEntry = recipeCache.get(key);

    if (cacheEntry == null) {
      return null;
    }

    if (cacheEntry.expiresMillis < System.currentTimeMillis()) {
      recipeCache.remove(key);
      return null;
    }

    return cacheEntry.value;
  }

  /**
   * store an answer
   * @param key the cache key
   * @param value the value to cache
   */
  private static void cachePut(String key, Object value) {

    int cacheSeconds = cacheSeconds();

    if (cacheSeconds <= 0) {
      return;
    }

    // the per subject entries grow with the number of distinct users on this node, so the cache
    // is bounded by count as well as by age.  emptying it wholesale rather than evicting the
    // least used keeps this to a handful of lines, and the cost of a miss here is small
    int maxSize = GrouperConfig.retrieveConfig().propertyValueInt(CONFIG_CACHE_MAX_SIZE, 5000);

    if (recipeCache.size() >= maxSize) {
      recipeCache.clear();
    }

    CacheEntry cacheEntry = new CacheEntry();
    cacheEntry.expiresMillis = System.currentTimeMillis() + (cacheSeconds * 1000L);
    cacheEntry.value = value;

    recipeCache.put(key, cacheEntry);
  }

  /**
   * how long answers are cached for, in seconds
   * @return the ttl, 0 or less meaning caching is off
   */
  private static int cacheSeconds() {
    return GrouperConfig.retrieveConfig().propertyValueInt(CONFIG_CACHE_SECONDS, 120);
  }

  /**
   * problems which were not there when a recipe was saved: a tool renamed or removed by an
   * upgrade, or a group deleted since.  reported on the recipes screen, because a recipe in
   * this state looks fine and does nothing.
   * @return one line per problem, in recipe order, empty when there are none
   */
  @SuppressWarnings("unchecked")
  public static List<String> retrieveProblems() {

    List<String> cached = (List<String>) cacheGet("problems");

    if (cached != null) {
      return cached;
    }

    List<String> problems = new ArrayList<String>();

    GrouperSession rootSession = GrouperSession.startRootSession();

    try {

      // including the disabled recipes, because the screen which shows this shows those rows
      // too, and a problem waiting on the other side of re-enabling one is worth knowing about
      for (GrouperMcpRecipe recipe : retrieveAllRecipes(true).values()) {

        String disabledNote = recipe.isEnabled() ? "" : " (this recipe is disabled)";

        for (String toolName : recipe.getToolNames()) {
          if (!GrouperMcpToolNames.isToolName(toolName)) {
            problems.add("Recipe '" + recipe.getName() + "' points at tool '" + toolName
                + "', which does not exist. Nothing is added to that tool's description."
                + disabledNote);
          }
        }

        // the two are checked by name rather than by value, so that a recipe naming the same
        // group for both does not report the same field twice
        if (StringUtils.isNotBlank(recipe.getGroupNameCanUse())
            && GroupFinder.findByName(rootSession, recipe.getGroupNameCanUse(), false) == null) {
          problems.add("Recipe '" + recipe.getName() + "' names group '"
              + recipe.getGroupNameCanUse() + "' as its audience, which cannot be found. Nobody "
              + "can use that recipe." + disabledNote);
        }

        if (StringUtils.isNotBlank(recipe.getGroupNameCanEdit())
            && GroupFinder.findByName(rootSession, recipe.getGroupNameCanEdit(), false) == null) {
          problems.add("Recipe '" + recipe.getName() + "' names group '"
              + recipe.getGroupNameCanEdit() + "' as its editors, which cannot be found. Nobody "
              + "can edit that recipe." + disabledNote);
        }
      }

    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    List<String> unmodifiableProblems = Collections.unmodifiableList(problems);

    cachePut("problems", unmodifiableProblems);

    return unmodifiableProblems;
  }

  /**
   * problems already logged, so that a broken recipe is reported once rather than on every
   * client which connects.  cleared with the rest of the cache, so a problem which is fixed and
   * then reappears is logged again
   */
  private static final Set<String> loggedProblems =
      java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());

  /**
   * log a problem the first time it is seen
   * @param problem the message
   */
  public static void logProblemOnce(String problem) {
    if (loggedProblems.add(problem)) {
      LOG.error(problem);
    }
  }

  /**
   * forget everything cached on this node.  called by the write paths so that whoever just made
   * a change sees it immediately rather than waiting out the TTL.  other nodes still wait
   */
  public static void clearCache() {
    recipeCache.clear();
    loggedProblems.clear();
  }

  /**
   * cache key for one subject.
   *
   * <p>The source id and subject id are length prefixed rather than just joined by a separator.
   * Both come from a subject source, so either may contain whatever the separator is, and a
   * plain join is ambiguous: source "jdbc" with id "a__b" and source "jdbc__a" with id "b" both
   * flatten to the same key.  Two subjects sharing a key would hand one of them the other's
   * cached recipes, so the encoding has to be one that cannot collide rather than one that is
   * merely unlikely to.  Lengths make each part self delimiting.</p>
   *
   * <p>The suffix is one of this class's own constants, so it needs no such treatment.</p>
   *
   * @param subject the subject
   * @param suffix what is being cached about them
   * @return the key
   */
  private static String subjectCacheKey(Subject subject, String suffix) {

    String sourceId = StringUtils.defaultString(subject.getSourceId());
    String subjectId = StringUtils.defaultString(subject.getId());

    return suffix + "__" + sourceId.length() + "_" + sourceId
        + "__" + subjectId.length() + "_" + subjectId;
  }

  /**
   * pattern which finds the config id of every configured recipe.  name is the anchor because
   * it is required, so a half written recipe with only a summary does not become a config id
   */
  private static final Pattern CONFIG_ID_PATTERN =
      Pattern.compile("^grouperMcpRecipe\\.([^.]+)\\.name$");

  /** config id of this recipe */
  private String configId;

  /** short name the AI client refers to this recipe by, e.g. policy-groups */
  private String name;

  /** one line stating the rule itself, which is also what is added to the description of
   *  each tool named in toolNames */
  private String summary;

  /** the detailed task: what the client should actually do */
  private String body;

  /** name of the group whose members may see and read this recipe, blank means nobody */
  private String groupNameCanUse;

  /** name of the group whose members may edit this recipe over MCP, blank means nobody */
  private String groupNameCanEdit;

  /** names of the MCP tools this recipe applies to, may be empty */
  private List<String> toolNames = new ArrayList<String>();

  /** whether this recipe is in use.  a disabled one is never shown to a client */
  private boolean enabled = true;

  /**
   * whether this recipe is in use
   * @return true if enabled
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * decides which recipes are described first when there are more than fit.  ascending, so a
   * lower number is more important, and the default sits in the middle of the range so a recipe
   * can be moved either way without renumbering the rest
   */
  private int priority = DEFAULT_PRIORITY;

  /** priority a recipe has when it does not set one */
  public static final int DEFAULT_PRIORITY = 100;

  /**
   * who last changed this recipe through Grouper, as a packed subject string,
   * sourceId::::subjectId.  a subject is only identified by both together, and this is the form
   * SubjectFinder.findByPackedSubjectString reads, so the value can be resolved back to a
   * subject without anything here having to know how to take it apart
   */
  private String lastEditedBy;

  /** their name when they made the change, for display, may be blank */
  private String lastEditedByName;

  /** date whoever it was last changed it, yyyy/MM/dd, may be blank */
  private String lastEditedOn;

  /**
   * who last changed this recipe through Grouper, as a packed subject string
   * sourceId::::subjectId, which SubjectFinder.findByPackedSubjectString resolves.  blank when
   * the recipe has only ever been edited outside Grouper, in a properties file or straight in
   * the database
   * @return the packed subject string, or null
   */
  public String getLastEditedBy() {
    return this.lastEditedBy;
  }

  /**
   * name of whoever last changed this recipe, as it was when they changed it.  stored rather
   * than looked up so that showing a recipe never costs a subject resolution, and so that the
   * name survives the subject going away
   * @return the name, or null
   */
  public String getLastEditedByName() {
    return this.lastEditedByName;
  }

  /**
   * date this recipe was last changed through Grouper, yyyy/MM/dd
   * @return the date, or null
   */
  public String getLastEditedOn() {
    return this.lastEditedOn;
  }

  /**
   * one line saying where this guidance came from and who is behind it, for the client which is
   * about to read the body.  a recipe should arrive looking like local advice with a name on it
   * rather than an anonymous instruction from the system
   * @return the attribution line, never null
   */
  public String attributionLine() {

    StringBuilder attribution = new StringBuilder();
    attribution.append("Institution guidance from recipe '").append(this.name).append("'");

    // the name is what a reader recognises, so it leads.  the subject id follows it because a
    // name on its own does not identify anybody, and this line is the provenance of guidance
    // which is about to steer what an agent does
    if (StringUtils.isNotBlank(this.lastEditedBy)) {

      attribution.append(", last edited by ");

      if (StringUtils.isNotBlank(this.lastEditedByName)) {
        attribution.append(this.lastEditedByName).append(" (")
            .append(GrouperUtil.prefixOrSuffix(this.lastEditedBy, "::::", false)).append(")");
      } else {
        attribution.append(GrouperUtil.prefixOrSuffix(this.lastEditedBy, "::::", false));
      }

      if (StringUtils.isNotBlank(this.lastEditedOn)) {
        attribution.append(" on ").append(this.lastEditedOn);
      }
    }

    attribution.append(".");

    return attribution.toString();
  }

  /**
   * decides which recipes are described first when there are more than fit
   * @return the priority, lower is more important
   */
  public int getPriority() {
    return this.priority;
  }

  /**
   * config id of this recipe
   * @return the config id
   */
  public String getConfigId() {
    return this.configId;
  }

  /**
   * short name the AI client refers to this recipe by
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * one line stating the rule itself
   * @return the summary
   */
  public String getSummary() {
    return this.summary;
  }

  /**
   * the detailed task
   * @return the body
   */
  public String getBody() {
    return this.body;
  }

  /**
   * name of the group whose members may see and read this recipe
   * @return the group name, blank means nobody
   */
  public String getGroupNameCanUse() {
    return this.groupNameCanUse;
  }

  /**
   * name of the group whose members may edit this recipe over MCP
   * @return the group name, blank means nobody
   */
  public String getGroupNameCanEdit() {
    return this.groupNameCanEdit;
  }

  /**
   * names of the MCP tools this recipe applies to
   * @return the tool names, never null
   */
  public List<String> getToolNames() {
    return this.toolNames;
  }

  /**
   * the tool names as they were configured, for screens which want to show them as text
   * @return comma separated tool names, never null
   */
  public String getToolNamesCommaSeparated() {
    return GrouperUtil.join(this.toolNames.iterator(), ", ");
  }

  /**
   * order recipes the way a deployer asked for them: by priority ascending, then by name so
   * that two recipes at the same priority always come out in the same order.  used both to
   * decide which recipes are described in full when there are more than fit, and to order the
   * pointers added to one tool's description
   * @param recipes the recipes to sort, not modified
   * @return a new list in priority order
   */
  public static List<GrouperMcpRecipe> sortByPriority(Collection<GrouperMcpRecipe> recipes) {

    List<GrouperMcpRecipe> sorted = new ArrayList<GrouperMcpRecipe>(GrouperUtil.nonNull(recipes));

    Collections.sort(sorted, new Comparator<GrouperMcpRecipe>() {

      public int compare(GrouperMcpRecipe first, GrouperMcpRecipe second) {

        if (first.getPriority() != second.getPriority()) {
          return first.getPriority() < second.getPriority() ? -1 : 1;
        }

        return StringUtils.defaultString(first.getName())
            .compareTo(StringUtils.defaultString(second.getName()));
      }
    });

    return sorted;
  }

  /**
   * whether this subject administers recipes through the UI screens.  this grants nothing over
   * MCP, which is the point of having two groups: somebody can administer recipes at a keyboard
   * without their AI client gaining anything.  membership of the MCP admin group counts here
   * too: somebody trusted to change any field of any recipe through an agent
   * is plainly trusted to do it at a keyboard, and this is a union at check time rather than a
   * rule that one group must contain the other, so neither group has to be maintained against
   * the other.
   *
   * <p>There is no wheel or root fallback here, and that is deliberate.  toolNames staples text
   * onto a built in tool's description for a whole population, which should not be one menu
   * click away for everybody who happens to administer Grouper.  It is a competence guardrail
   * and not a security boundary: an administrator can always reach the underlying config.</p>
   *
   * @param subject the subject to check
   * @return true if the subject can administer recipes in the UI
   */
  public static boolean canAdminInUi(Subject subject) {
    boolean[] adminFlags = retrieveAdminFlags(subject);
    return adminFlags[0] || adminFlags[1];
  }

  /**
   * whether this subject can read and update any recipe over MCP, including the fields which
   * decide who a recipe reaches and which tools it attaches to.
   *
   * <p>Read as well as update: being able to rewrite a recipe one cannot read is worse than
   * either consistent answer, so this also lets a member fetch any recipe body, not only the
   * ones whose groupNameCanUse includes them.  That is not new access for the person, who can
   * already read every recipe on the screens, but it is new access for their AI client.</p>
   *
   * <p>Note this is the MCP administration group alone, unlike {@link #canAdminInUi(Subject)},
   * which is the union of both.  Somebody in the UI group only is an ordinary MCP user here.</p>
   *
   * @param subject the subject to check
   * @return true if the subject can administer recipes over MCP
   */
  public static boolean canAdminInMcp(Subject subject) {
    return retrieveAdminFlags(subject)[1];
  }

  /**
   * resolve both administration groups for a subject in one root session, cached together.  they
   * are always wanted at the same time, so doing them separately would double the session and
   * lookup cost on the Miscellaneous screen for no benefit
   * @param subject the subject to check
   * @return two flags: in the UI admin group, and in the MCP admin group
   */
  private static boolean[] retrieveAdminFlags(final Subject subject) {

    if (subject == null) {
      return new boolean[] { false, false };
    }

    String cacheKey = subjectCacheKey(subject, "adminFlags");

    boolean[] cached = (boolean[]) cacheGet(cacheKey);

    if (cached != null) {
      return cached;
    }

    final String uiGroupName = StringUtils.trimToNull(GrouperConfig.retrieveConfig()
        .propertyValueString(CONFIG_GROUP_CAN_ADMIN_IN_UI));
    final String mcpGroupName = StringUtils.trimToNull(GrouperConfig.retrieveConfig()
        .propertyValueString(CONFIG_GROUP_CAN_ADMIN_IN_MCP));

    final boolean[] adminFlags = new boolean[] { false, false };

    if (uiGroupName == null && mcpGroupName == null) {
      cachePut(cacheKey, adminFlags);
      return adminFlags;
    }

    GrouperSession rootSession = GrouperSession.startRootSession();

    try {

      GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession theGrouperSession) {
          adminFlags[0] = isSubjectInGroup(theGrouperSession, subject, uiGroupName);
          adminFlags[1] = isSubjectInGroup(theGrouperSession, subject, mcpGroupName);
          return null;
        }
      });

    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    cachePut(cacheKey, adminFlags);

    return adminFlags;
  }

  /**
   * membership check which fails closed on a group name which is unset or does not resolve
   * @param theGrouperSession a root session
   * @param subject the subject to check
   * @param groupName the group name, may be null
   * @return true if the subject is a member
   */
  private static boolean isSubjectInGroup(GrouperSession theGrouperSession, Subject subject,
      String groupName) {

    if (groupName == null) {
      return false;
    }

    Group group = GroupFinder.findByName(theGrouperSession, groupName, false);

    if (group == null) {
      LOG.error("MCP recipe config names group '" + groupName
          + "' which cannot be found, so it grants nobody");
      return false;
    }

    return group.hasMember(subject);
  }

  /**
   * whether the per recipe groupNameCanEdit delegation is honoured over MCP at all.  off by
   * default: a deployer turns it on once, rather than having to blank groupNameCanEdit on every
   * recipe to close the path.  it does not gate the delegated edit screen, which is the only
   * way a process owner can edit their own recipe when this is off
   * @return true if delegated content edits are allowed over MCP
   */
  public static boolean isAllowEditInMcp() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean(CONFIG_ALLOW_EDIT_IN_MCP, false);
  }

  /**
   * retrieve every enabled recipe which is configured, without applying any security.  callers
   * which hand recipes to a client must filter with {@link #retrieveRecipesCanUse(Subject)}
   * instead of calling this.
   * @return the recipes, keyed by name, in name order, never null
   */
  public static Map<String, GrouperMcpRecipe> retrieveAllRecipes() {
    return retrieveAllRecipes(false);
  }

  /**
   * retrieve every configured recipe, optionally including the disabled ones.
   *
   * <p>Disabled means disabled to a client: a recipe with enabled = false is in no tool
   * description, no list, and no get, so it cannot be fetched by name at all.  Administration is
   * the other case.  Somebody who has turned a recipe off while they rewrite it still has to be
   * able to open it, and the name of a disabled recipe still has to block a new recipe taking
   * the same name, or re-enabling the first one would produce two recipes a client cannot tell
   * apart.  So the screens and the checks around them pass true here, and everything which
   * hands a recipe to a client does not.</p>
   *
   * @param includeDisabled true for administration, false for anything a client sees
   * @return the recipes, keyed by name, never null
   */
  @SuppressWarnings("unchecked")
  public static Map<String, GrouperMcpRecipe> retrieveAllRecipes(boolean includeDisabled) {
    return retrieveAllRecipes(includeDisabled, true);
  }

  /**
   * retrieve every recipe, optionally bypassing the per node cache.
   *
   * <p>Reading uncached matters for the name uniqueness check.  The cache is node local with a
   * time based expiry, so a name taken on another node within the TTL is not visible here yet,
   * and two administrators on two nodes could each be told a name is free.  Runtime parsing
   * would then pick one of the duplicates and the other would be unreachable by name, which is
   * how an AI client asks for a recipe.  Going to the configuration directly narrows that to
   * the gap between the check and the write, without a lock.</p>
   *
   * <p>This is not free: it re-reads and re-parses the recipe configuration.  Use it for
   * validation before a write, not on the delivery paths.</p>
   *
   * <p>It narrows the window rather than closing it.  The configuration underneath has its own
   * refresh interval, so a name written on another node moments ago may still not be visible.
   * Closing it completely would need a lock or a uniqueness constraint, which is more than the
   * consequence warrants: both sides of a collision are institution authored guidance text, and
   * the recipes screen reports a duplicate through retrieveProblems once the caches catch up.</p>
   *
   * @param includeDisabled whether to include recipes which are turned off
   * @param useCache false to read the configuration rather than the cache
   * @return the recipes keyed by name, never null
   */
  public static Map<String, GrouperMcpRecipe> retrieveAllRecipes(boolean includeDisabled,
      boolean useCache) {

    String cacheKey = includeDisabled ? "allRecipesIncludingDisabled" : "allRecipes";

    Map<String, GrouperMcpRecipe> cached = useCache
        ? (Map<String, GrouperMcpRecipe>) cacheGet(cacheKey) : null;

    if (cached != null) {
      return cached;
    }

    Map<String, GrouperMcpRecipe> recipesByName = new TreeMap<String, GrouperMcpRecipe>();

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    Set<String> configIds = new LinkedHashSet<String>(
        grouperConfig.propertyConfigIds(CONFIG_ID_PATTERN));

    for (String configId : configIds) {

      String prefix = CONFIG_PREFIX + configId + ".";

      boolean enabled = grouperConfig.propertyValueBoolean(prefix + "enabled", true);

      if (!enabled && !includeDisabled) {
        continue;
      }

      String name = StringUtils.trimToNull(grouperConfig.propertyValueString(prefix + "name"));
      String summary = StringUtils.trimToNull(grouperConfig.propertyValueString(prefix + "summary"));
      String body = StringUtils.trimToNull(grouperConfig.propertyValueString(prefix + "body"));

      // a recipe with no name, summary, or body has nothing to give a client.  skip it rather
      // than advertising an empty entry the client would have to spend a call to find out is
      // empty
      if (name == null || summary == null || body == null) {
        LOG.warn("MCP recipe '" + configId
            + "' is missing name, summary, or body, so it is being ignored");
        continue;
      }

      GrouperMcpRecipe recipe = new GrouperMcpRecipe();
      recipe.configId = configId;
      recipe.name = name;
      recipe.summary = summary;
      recipe.body = body;
      recipe.groupNameCanUse = StringUtils.trimToNull(
          grouperConfig.propertyValueString(prefix + "groupNameCanUse"));
      recipe.groupNameCanEdit = StringUtils.trimToNull(
          grouperConfig.propertyValueString(prefix + "groupNameCanEdit"));

      String toolNames = grouperConfig.propertyValueString(prefix + "toolNames");
      if (StringUtils.isNotBlank(toolNames)) {
        recipe.toolNames.addAll(GrouperUtil.splitTrimToList(toolNames, ","));
      }

      recipe.enabled = enabled;
      recipe.priority = grouperConfig.propertyValueInt(prefix + "priority", DEFAULT_PRIORITY);

      recipe.lastEditedBy = StringUtils.trimToNull(
          grouperConfig.propertyValueString(prefix + "lastEditedBy"));
      recipe.lastEditedByName = StringUtils.trimToNull(
          grouperConfig.propertyValueString(prefix + "lastEditedByName"));
      recipe.lastEditedOn = StringUtils.trimToNull(
          grouperConfig.propertyValueString(prefix + "lastEditedOn"));

      // two recipes with the same name would be ambiguous to a client asking for one by name.
      // an enabled one always wins over a disabled one, since otherwise a disabled recipe could
      // hide a live one from its own owner in the administration view.  otherwise the first
      // config id wins, and either way the collision is logged: silently dropping guidance an
      // administrator believes is in place is worse than the duplicate
      GrouperMcpRecipe existingWithName = recipesByName.get(name);

      if (existingWithName != null) {

        boolean replacing = !existingWithName.enabled && enabled;

        LOG.error("MCP recipe name '" + name + "' is configured more than once ('"
            + existingWithName.getConfigId() + "' and '" + configId + "'), using '"
            + (replacing ? configId : existingWithName.getConfigId()) + "'");

        if (!replacing) {
          continue;
        }
      }

      recipesByName.put(name, recipe);
    }

    // unmodifiable because this instance is shared with every later caller through the cache,
    // so one stray mutation would corrupt what everybody else sees
    Map<String, GrouperMcpRecipe> unmodifiableRecipes = Collections.unmodifiableMap(recipesByName);

    cachePut(cacheKey, unmodifiableRecipes);

    return unmodifiableRecipes;
  }

  /**
   * retrieve the recipes this subject may see and read.  the group check is batched into one
   * membership query across every recipe, because this runs on tools/list, which is on the
   * critical path of every client connecting.
   * @param subject the authenticated subject
   * @return the recipes the subject may use, keyed by name, never null
   */
  public static Map<String, GrouperMcpRecipe> retrieveRecipesCanUse(Subject subject) {
    return retrieveRecipesForGroups(subject, true, false);
  }

  /**
   * retrieve the recipes this subject may edit over MCP
   * @param subject the authenticated subject
   * @return the recipes the subject may edit, keyed by name, never null
   */
  public static Map<String, GrouperMcpRecipe> retrieveRecipesCanEdit(Subject subject) {
    // editing is administration, so a recipe which is turned off is still the owner's to fix
    return retrieveRecipesForGroups(subject, false, true);
  }

  /**
   * retrieve the recipes which apply to a given MCP tool and which this subject may use.  this
   * is what puts a pointer to a recipe on the description of the tool it is about, which is the
   * only place we have evidence a client reliably reads.
   * @param toolName the MCP tool name
   * @param subject the authenticated subject
   * @return the recipes for that tool, never null
   */
  public static List<GrouperMcpRecipe> retrieveRecipesForTool(String toolName, Subject subject) {

    List<GrouperMcpRecipe> result = new ArrayList<GrouperMcpRecipe>();

    if (StringUtils.isBlank(toolName)) {
      return result;
    }

    Map<String, GrouperMcpRecipe> recipes = retrieveRecipesCanUse(subject);

    for (GrouperMcpRecipe recipe : recipes.values()) {
      for (String recipeToolName : recipe.getToolNames()) {
        if (Strings.CS.equals(toolName, recipeToolName)) {
          result.add(recipe);
          break;
        }
      }
    }

    return result;
  }

  /**
   * update the content fields of one recipe.  only name, summary, and body can be changed here:
   * groupNameCanUse, groupNameCanEdit, toolNames, and enabled decide who a recipe reaches and
   * what it can influence, so they are deliberately not reachable from MCP and stay with
   * configuration administrators.
   * @param configId the recipe to update
   * @param name the new name
   * @param summary the new summary
   * @param body the new body
   */
  public static void updateRecipeContent(String configId, String name, String summary, String body) {

    Map<String, String> fieldValues = new LinkedHashMap<String, String>();
    fieldValues.put("name", name);
    fieldValues.put("summary", summary);
    fieldValues.put("body", body);

    updateRecipeFields(configId, fieldValues);
  }

  /**
   * update named fields of one recipe.  the caller decides which fields it is allowed to pass:
   * a delegated editor is held to the content fields by its own path, while a recipe
   * administrator may pass any of them
   * @param configId the recipe to update
   * @param fieldValues config suffix to new value, e.g. summary to the new summary
   */
  public static void updateRecipeFields(String configId, Map<String, String> fieldValues) {

    if (StringUtils.isBlank(configId)) {
      throw new RuntimeException("configId cannot be blank");
    }

    String prefix = CONFIG_PREFIX + configId + ".";

    // belt and braces.  every caller is expected to have validated already so it could report
    // the problem itself, so anything caught here is a caller which did not
    String validationError = validateFieldValues(configId, fieldValues);

    if (validationError != null) {
      throw new RuntimeException("Refusing to write recipe '" + configId + "': " + validationError);
    }

    for (Map.Entry<String, String> fieldValue : GrouperUtil.nonNull(fieldValues).entrySet()) {

      // these say who changed the recipe, so they are written here from the session rather than
      // taken from whoever is calling.  a caller which echoes a whole record back is not an
      // error, its values are just ignored
      if (isAttributionField(fieldValue.getKey())) {
        continue;
      }

      storeConfigValue(prefix + fieldValue.getKey(), fieldValue.getValue());
    }

    stampAttribution(configId);

    // so that whoever just made this change sees it straight away rather than reading their own
    // recipe back stale for the rest of the TTL.  other nodes still wait out their own
    clearCache();
  }

  /**
   * check field values which are about to be written to a recipe.
   *
   * <p>The recipe screens validate through the configuration module, which reads what each field
   * declares in grouper.base.properties: whether it is required, and what type it holds.  This
   * asks the same metadata rather than restating it, because a second copy of those rules in
   * Java is a copy which can disagree with the declaration, and has: a field declared required
   * went unchecked here until somebody noticed by reading.  Adding a field to the config now
   * gets it validated on this path without anybody remembering to come here.</p>
   *
   * <p>What stays written out below is only what the metadata cannot express: that a tool name
   * has to be a tool which exists, and a group name a group which resolves.</p>
   *
   * <p>Callers should call this so they can report the problem in their own terms.
   * {@link #updateRecipeFields(String, Map)} calls it again as a last line of defence, where a
   * failure is a caller which skipped it rather than something a user did, and throws.</p>
   *
   * @param configId the recipe being written
   * @param fieldValues config suffix to new value; only the ones present are checked
   * @return an error to show the caller, or null when everything is acceptable
   */
  public static String validateFieldValues(String configId, Map<String, String> fieldValues) {

    if (GrouperUtil.length(fieldValues) == 0) {
      return null;
    }

    GrouperMcpRecipeConfiguration recipeConfiguration = new GrouperMcpRecipeConfiguration();
    recipeConfiguration.setConfigId(configId);

    Map<String, GrouperConfigurationModuleAttribute> attributes = recipeConfiguration.retrieveAttributes();

    for (Map.Entry<String, String> fieldValue : fieldValues.entrySet()) {

      String field = fieldValue.getKey();
      String value = fieldValue.getValue();

      GrouperConfigurationModuleAttribute attribute = attributes.get(field);

      if (attribute != null) {

        // required comes from the declaration, so a field which the screens will not let you
        // blank cannot be blanked from here either.  blanking one is worth an error rather than
        // a silent success: it can take a recipe away from every client
        if (attribute.isRequired() && StringUtils.isBlank(value)) {
          return "'" + field + "' cannot be blank.";
        }

        // so does the type
        ConfigItemMetadata configItemMetadata = attribute.getConfigItemMetadata();

        if (StringUtils.isNotBlank(value) && configItemMetadata != null
            && configItemMetadata.getValueType() != null) {

          try {
            configItemMetadata.getValueType().convertValue(value, true);
          } catch (RuntimeException e) {
            return "'" + field + "' must be "
                + configItemMetadata.getValueType().getStringForUi() + ".";
          }
        }
      }

      // the rest is what no metadata can say

      if (Strings.CS.equals("toolNames", field) && StringUtils.isNotBlank(value)) {
        for (String toolName : GrouperUtil.splitTrimToList(value, ",")) {
          if (!GrouperMcpToolNames.isToolName(toolName)) {
            return "There is no MCP tool called '" + toolName + "', so a recipe pointing at it "
                + "would add nothing to anything.";
          }
        }
      }

      if ((Strings.CS.equals("groupNameCanUse", field)
          || Strings.CS.equals("groupNameCanEdit", field)) && StringUtils.isNotBlank(value)) {

        GrouperSession rootSession = GrouperSession.startRootSession();

        try {
          if (GroupFinder.findByName(rootSession, value, false) == null) {
            return "Group '" + value + "' could not be found, so nobody would be able to "
                + (Strings.CS.equals("groupNameCanUse", field) ? "use" : "edit") + " this recipe.";
          }
        } finally {
          GrouperSession.stopQuietly(rootSession);
        }
      }
    }

    return null;
  }

  /**
   * whether a field is one this class writes itself to record who made a change
   * @param fieldName the config suffix
   * @return true if it is an attribution field
   */
  public static boolean isAttributionField(String fieldName) {
    return Strings.CS.equals("lastEditedBy", fieldName)
        || Strings.CS.equals("lastEditedByName", fieldName)
        || Strings.CS.equals("lastEditedOn", fieldName);
  }

  /**
   * record who is changing a recipe and when.  called by every path which writes one through
   * Grouper, so that the body a client is handed can say where it came from and who is behind
   * it.  a recipe edited outside Grouper, in a properties file or straight in the database,
   * keeps whatever attribution it had, which is why the line says who last edited it rather
   * than claiming to be authoritative
   * @param configId the recipe being changed
   */
  public static void stampAttribution(String configId) {

    Subject subject = GrouperSession.staticGrouperSession(false) == null
        ? null : GrouperSession.staticGrouperSession(false).getSubject();

    if (subject == null) {
      return;
    }

    String prefix = CONFIG_PREFIX + configId + ".";

    // a packed subject string, the form SubjectFinder.findByPackedSubjectString reads back.
    // four colons is what that API uses to mean the subject id follows, as opposed to six for a
    // subject identifier or eight for either
    storeConfigValue(prefix + "lastEditedBy", subject.getSourceId() + "::::" + subject.getId());
    storeConfigValue(prefix + "lastEditedByName", subject.getName());
    storeConfigValue(prefix + "lastEditedOn",
        new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
  }

  /**
   * write one config key to the database config, inserting it when it is only in a file today.
   * recipes are never passwords, so there is no encryption to consider here
   * @param configKey the key
   * @param value the value
   */
  private static void storeConfigValue(String configKey, String value) {

    Set<GrouperConfigHibernate> existing = GrouperDAOFactory.getFactory().getConfig()
        .findAll(ConfigFileName.GROUPER_PROPERTIES, null, configKey);

    if (GrouperUtil.length(existing) == 0) {
      GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
      grouperConfigHibernate.setConfigEncrypted(false);
      grouperConfigHibernate.setConfigFileHierarchyDb("INSTITUTION");
      grouperConfigHibernate.setConfigFileNameDb(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName());
      grouperConfigHibernate.setConfigKey(configKey);
      grouperConfigHibernate.setValueToSave(value);
      grouperConfigHibernate.saveOrUpdate(true);
    } else {
      GrouperConfigHibernate grouperConfigHibernate = existing.iterator().next();
      grouperConfigHibernate.setConfigEncrypted(false);
      grouperConfigHibernate.setValueToSave(value);
      grouperConfigHibernate.saveOrUpdate(false);
    }
  }

  /**
   * filter all recipes down to the ones whose use or edit group contains this subject
   * @param subject the authenticated subject
   * @param useGroup true to check groupNameCanUse, false to check groupNameCanEdit
   * @return the recipes, keyed by name, never null
   */
  @SuppressWarnings("unchecked")
  private static Map<String, GrouperMcpRecipe> retrieveRecipesForGroups(final Subject subject,
      final boolean useGroup, final boolean includeDisabled) {

    final Map<String, GrouperMcpRecipe> result = new TreeMap<String, GrouperMcpRecipe>();

    if (subject == null) {
      return result;
    }

    // includeDisabled is part of the key.  the two callers happen to correlate it with useGroup
    // today, but keying on only one of them would hand a third caller the wrong map
    String cacheKey = subjectCacheKey(subject,
        (useGroup ? "recipesCanUse" : "recipesCanEdit") + (includeDisabled ? "WithDisabled" : ""));

    Map<String, GrouperMcpRecipe> cached = (Map<String, GrouperMcpRecipe>) cacheGet(cacheKey);

    if (cached != null) {
      return cached;
    }

    final Map<String, GrouperMcpRecipe> allRecipes = retrieveAllRecipes(includeDisabled);

    if (allRecipes.isEmpty()) {
      Map<String, GrouperMcpRecipe> noRecipes = Collections.unmodifiableMap(result);
      cachePut(cacheKey, noRecipes);
      return noRecipes;
    }

    // resolve the groups as root: a recipe's audience often cannot view the group which grants
    // it to them, and that should not stop the recipe reaching them
    GrouperSession rootSession = GrouperSession.startRootSession();

    try {

      GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession theGrouperSession) {

          MembershipFinder membershipFinder = null;
          Map<String, Group> groupsByName = new TreeMap<String, Group>();

          // pass 1: resolve each distinct group once and collect them for a single membership
          // query.  a per recipe membership check would put one query per recipe on tools/list
          for (GrouperMcpRecipe recipe : allRecipes.values()) {

            String groupName = useGroup ? recipe.getGroupNameCanUse() : recipe.getGroupNameCanEdit();

            // blank fails closed: nobody can use, or nobody can edit
            if (StringUtils.isBlank(groupName) || groupsByName.containsKey(groupName)) {
              continue;
            }

            Group group = GroupFinder.findByName(theGrouperSession, groupName, false);

            if (group == null) {
              // once, not on every cache miss for every user who connects
              logProblemOnce("MCP recipe '" + recipe.getConfigId() + "' refers to group '"
                  + groupName + "' which cannot be found, so nobody can "
                  + (useGroup ? "use" : "edit") + " that recipe");
              continue;
            }

            groupsByName.put(groupName, group);

            if (membershipFinder == null) {
              membershipFinder = new MembershipFinder()
                  .addSubject(subject)
                  .addField(Group.getDefaultList())
                  .assignCheckSecurity(false);
            }
            membershipFinder.addGroup(group);
          }

          if (membershipFinder == null) {
            return null;
          }

          MembershipResult membershipResult = membershipFinder.findMembershipResult();

          // pass 2: keep the recipes whose group the subject is in
          for (GrouperMcpRecipe recipe : allRecipes.values()) {

            String groupName = useGroup ? recipe.getGroupNameCanUse() : recipe.getGroupNameCanEdit();

            if (StringUtils.isBlank(groupName) || !groupsByName.containsKey(groupName)) {
              continue;
            }

            if (membershipResult.hasGroupMembership(groupName, subject)) {
              result.put(recipe.getName(), recipe);
            }
          }

          return null;
        }
      });

    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // shared with later callers through the cache, so it must not be mutable
    Map<String, GrouperMcpRecipe> unmodifiableResult = Collections.unmodifiableMap(result);

    cachePut(cacheKey, unmodifiableResult);

    return unmodifiableResult;
  }

}
