package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperAbac {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperAbac.class);

  public static final String GROUPER_JEXL_SCRIPT_MARKER_DEF = "grouperJexlScriptMarkerDef";
  
  public static final String GROUPER_JEXL_SCRIPT_MARKER = "grouperJexlScriptMarker";

  public static final String GROUPER_JEXL_SCRIPT_VALUE_DEF = "grouperJexlScriptValueDef";

  public static final String GROUPER_JEXL_SCRIPT_JEXL_SCRIPT = "grouperJexlScriptJexlScript";

  public static final String GROUPER_JEXL_SCRIPT_JEXL_LAST_GROUP_SYNC_DEF = "grouperJexlScriptJexlLastGroupSyncDef";

  public static final String GROUPER_JEXL_SCRIPT_JEXL_LAST_GROUP_SYNC = "grouperJexlScriptJexlLastGroupSync";

  public static final String GROUPER_JEXL_SCRIPT_SUBJECT_SOURCE_IDS = "grouperJexlScriptSubjectSourceIds";

  /**
   * 
   * @return the stem name
   */
  public static String jexlScriptStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:abacJexlScript";
  }

  /**
   * cache the global default subject source ids for 10 minutes
   */
  private static ExpirableCache<Boolean, Set<String>> globalDefaultSubjectSourceIdsCache = new ExpirableCache<>(2);

  /**
   * get the global default subject source ids for ABAC.
   * if the config is blank, return all non-internal and non-grouper subject source ids
   * (excludes g:gsa, g:isa, grouperEntities, grouperExternal).
   * if the config is populated, return exactly those source ids.
   * @return the set of subject source ids
   */
  public static Set<String> globalDefaultSubjectSourceIds() {
    Set<String> result = globalDefaultSubjectSourceIdsCache.get(Boolean.TRUE);
    if (result != null) {
      return result;
    }

    String configValue = GrouperConfig.retrieveConfig().propertyValueString("grouper.abac.globalDefaultSubjectSourceIds");

    result = new LinkedHashSet<String>();

    if (StringUtils.isNotBlank(configValue)) {
      for (String sourceId : GrouperUtil.splitTrim(configValue, ",")) {
        result.add(sourceId);
      }
    } else {
      Collection<Source> sources = SourceManager.getInstance().getSources();
      for (Source source : sources) {
        if (!internalSourceId(source.getId())
            && !StringUtils.equals(source.getId(), "grouperEntities")
            && !StringUtils.equals(source.getId(), "grouperExternal")) {
          result.add(source.getId());
        }
      }
    }

    globalDefaultSubjectSourceIdsCache.put(Boolean.TRUE, result);
    return result;
  }

  /**
   * cache the allow user override setting for 10 minutes
   */
  private static ExpirableCache<Boolean, Boolean> allowUserOverrideSubjectSourceIdsCache = new ExpirableCache<>(2);

  /**
   * whether users can override the default subject source ids on a per-group basis
   * @return true if allowed
   */
  public static boolean allowUserOverrideSubjectSourceIds() {
    Boolean result = allowUserOverrideSubjectSourceIdsCache.get(Boolean.TRUE);
    if (result != null) {
      return result;
    }
    result = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.abac.allowUserOverrideSubjectSourceIds", false);
    allowUserOverrideSubjectSourceIdsCache.put(Boolean.TRUE, result);
    return result;
  }

  /**
   * cache the available subject source ids for 10 minutes
   */
  private static ExpirableCache<Boolean, Set<String>> availableSubjectSourceIdsCache = new ExpirableCache<>(2);

  /**
   * get the available subject source ids that users can pick from.
   * if the config is blank, returns empty set (no UI picker).
   * @return the set of available subject source ids
   */
  public static Set<String> availableSubjectSourceIds() {
    Set<String> result = availableSubjectSourceIdsCache.get(Boolean.TRUE);
    if (result != null) {
      return result;
    }

    String configValue = GrouperConfig.retrieveConfig().propertyValueString("grouper.abac.availableSubjectSourceIds");

    result = new LinkedHashSet<String>();

    if (StringUtils.isNotBlank(configValue)) {
      for (String sourceId : GrouperUtil.splitTrim(configValue, ",")) {
        // only block g:gsa and g:isa
        if (!StringUtils.equals(sourceId, "g:gsa") && !StringUtils.equals(sourceId, "g:isa")) {
          result.add(sourceId);
        }
      }
    }

    availableSubjectSourceIdsCache.put(Boolean.TRUE, result);
    return result;
  }

  /**
   * whether the UI should show the subject source picker for a given group.
   * requires: allowUserOverride is true, available list is not blank, and available list has more than 1 item.
   * @return true if the picker should be shown
   */
  public static boolean showSubjectSourcePicker() {
    if (!allowUserOverrideSubjectSourceIds()) {
      return false;
    }
    Set<String> available = availableSubjectSourceIds();
    if (GrouperUtil.length(available) <= 1) {
      return false;
    }
    return true;
  }

  /**
   * get the effective subject source ids for a given group.
   * considers all 3 global configs and the per-group attribute.
   * always removes g:gsa and g:isa.
   * @param perGroupSourceIds comma-separated per-group source IDs from the attribute, or null
   * @return the set of subject source ids to use
   */
  public static Set<String> effectiveSubjectSourceIds(String perGroupSourceIds) {

    Set<String> result = new LinkedHashSet<String>();

    // if override is not allowed or available list is blank, always use global defaults
    if (!allowUserOverrideSubjectSourceIds() || GrouperUtil.length(availableSubjectSourceIds()) == 0) {
      result.addAll(globalDefaultSubjectSourceIds());
    } else if (StringUtils.isNotBlank(perGroupSourceIds)) {
      Set<String> available = availableSubjectSourceIds();
      for (String sourceId : GrouperUtil.splitTrim(perGroupSourceIds, ",")) {
        // only allow sources that are in the available list
        if (available.contains(sourceId)) {
          result.add(sourceId);
        }
      }
      // if nothing valid remains, fall back to global defaults
      if (result.isEmpty()) {
        result.addAll(globalDefaultSubjectSourceIds());
      }
    } else {
      result.addAll(globalDefaultSubjectSourceIds());
    }

    // never include g:gsa or g:isa
    result.remove("g:gsa");
    result.remove("g:isa");

    return result;
  }

  /**
   * generate a SQL IN clause fragment and bind variables for subject source filtering.
   * e.g. returns "gm.subject_source in (?, ?)" and ["pennperson", "pennCommunity"]
   * @param sourceIds the source IDs to include, or null to use global defaults
   * @return MultiKey where key(0) is the SQL fragment string and key(1) is a List of bind variable strings
   */
  public static MultiKey subjectSourceInClause(Set<String> sourceIds) {
    if (sourceIds == null || sourceIds.size() == 0) {
      sourceIds = globalDefaultSubjectSourceIds();
    }

    // always remove g:gsa and g:isa
    Set<String> filtered = new LinkedHashSet<String>(sourceIds);
    filtered.remove("g:gsa");
    filtered.remove("g:isa");

    String sqlFragment = "gm.subject_source in (" + GrouperClientUtils.appendQuestions(filtered.size()) + ")";
    List<String> bindVars = new ArrayList<String>(filtered);

    return new MultiKey(sqlFragment, bindVars);
  }

  /**
   * a configured tier: members of groupName may save ABAC scripted groups up to maxSize members
   */
  private static class MaxMembershipSizeTier {
    private String groupName;
    private int maxSize;
  }

  /**
   * cache the parsed max-membership-size tiers for a couple minutes
   */
  private static ExpirableCache<Boolean, List<MaxMembershipSizeTier>> maxMembershipSizeTiersCache = new ExpirableCache<>(2);

  /** matches grouper.abac.maxMembershipSizeLimit.&lt;configId&gt;.groupName to pull out configIds */
  private static final Pattern maxMembershipSizeTierPattern =
      Pattern.compile("^grouper\\.abac\\.maxMembershipSizeLimit\\.([^.]+)\\.groupName$");

  /**
   * Read the configured size tiers from grouper.properties:
   *   grouper.abac.maxMembershipSizeLimit.&lt;configId&gt;.groupName = etc:abac:largeGroupEditors
   *   grouper.abac.maxMembershipSizeLimit.&lt;configId&gt;.maxSize   = 200000
   * @return the tiers (never null)
   */
  private static List<MaxMembershipSizeTier> maxMembershipSizeTiers() {
    List<MaxMembershipSizeTier> result = maxMembershipSizeTiersCache.get(Boolean.TRUE);
    if (result != null) {
      return result;
    }
    result = new ArrayList<MaxMembershipSizeTier>();
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIds = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(maxMembershipSizeTierPattern));
    for (String configId : configIds) {
      String groupName = grouperConfig.propertyValueString("grouper.abac.maxMembershipSizeLimit." + configId + ".groupName");
      int maxSize = grouperConfig.propertyValueInt("grouper.abac.maxMembershipSizeLimit." + configId + ".maxSize", -1);
      if (StringUtils.isBlank(groupName) || maxSize < 0) {
        LOG.error("ABAC max membership size tier '" + configId + "' is missing groupName or maxSize; skipping");
        continue;
      }
      MaxMembershipSizeTier tier = new MaxMembershipSizeTier();
      tier.groupName = groupName;
      tier.maxSize = maxSize;
      result.add(tier);
    }
    maxMembershipSizeTiersCache.put(Boolean.TRUE, result);
    return result;
  }

  /**
   * The maximum ABAC scripted-group membership size the given subject is allowed to save.
   * The highest applicable cap wins: the base default (if configured) plus the maxSize of every
   * tier group the subject is a member of. Returns null when no cap applies to this subject (no
   * tiers matched and no default configured) — i.e. unlimited, so the feature never locks anyone
   * out unless an admin deliberately configures caps.
   * @param subject the subject saving the scripted group
   * @return the max allowed membership size, or null for unlimited
   */
  public static Integer maxAbacMembershipSizeForSubject(final Subject subject) {
    if (subject == null) {
      return null;
    }
    List<MaxMembershipSizeTier> tiers = maxMembershipSizeTiers();
    String defaultConfig = GrouperConfig.retrieveConfig().propertyValueString("grouper.abac.defaultMaxMembershipSizeLimit");

    if (tiers.isEmpty() && StringUtils.isBlank(defaultConfig)) {
      // feature not configured at all -- unlimited
      return null;
    }

    Integer cap = null;
    if (StringUtils.isNotBlank(defaultConfig)) {
      cap = GrouperUtil.intValue(defaultConfig);
    }

    if (!tiers.isEmpty()) {
      // membership checks run as root -- the subject may not be able to read the config groups
      final List<MaxMembershipSizeTier> finalTiers = tiers;
      Integer tierCap = (Integer)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Integer highest = null;
          for (MaxMembershipSizeTier tier : finalTiers) {
            Group group = GroupFinder.findByName(grouperSession, tier.groupName, false);
            if (group == null) {
              LOG.error("ABAC max membership size tier group '" + tier.groupName + "' does not exist; skipping");
              continue;
            }
            if (group.hasMember(subject)) {
              if (highest == null || tier.maxSize > highest) {
                highest = tier.maxSize;
              }
            }
          }
          return highest;
        }
      });
      if (tierCap != null && (cap == null || tierCap > cap)) {
        cap = tierCap;
      }
    }

    return cap;
  }

  /**
   * clear all expirable caches, useful for testing
   */
  public static void clearCaches() {
    globalDefaultSubjectSourceIdsCache.clear();
    allowUserOverrideSubjectSourceIdsCache.clear();
    availableSubjectSourceIdsCache.clear();
    maxMembershipSizeTiersCache.clear();
  }

  /**
   *
   * @param sourceId
   * @return true
   */
  public static boolean internalSourceId(String sourceId) {
    return StringUtils.equals(sourceId, "g:gsa")
        || StringUtils.equals(sourceId, "g:isa");
  }
  
  /**
   * return the error message or null for valid
   * @param script
   * @return the error message or null for valid
   */
  public static String validScript(String script, GrouperDataEngine grouperDataEngine) {
    if (script == null) {
      return "script is null";
    }
    //TODO
    if (true) {
      return null;
    }
    script = script.trim();
    try {
      GrouperAbacEntity grouperAbacEntity = new GrouperAbacEntity();
      grouperAbacEntity.setGrouperDataEngine(grouperDataEngine);
      grouperAbacEntity.setMultiValuedGroupExtensionInFolder(new HashMap<String, Set<String>>());
      grouperAbacEntity.setSingleValuedGroupExtensionInFolder(new HashMap<String, String>());
      grouperAbacEntity.setMemberOfGroupNames(new HashSet<String>());
      final JexlEngine jexlEngine = new JexlEngine();
      jexlEngine.setSilent(false);
      // not sure this works
      jexlEngine.setLenient(true);

      jexlEngine.createScript(script.substring(2, script.length()-1));
      
      Map<String, Object> variableMap = new HashMap<String, Object>();
      
      variableMap.put("entity", grouperAbacEntity);
      
      Object result = runScriptStatic(script, variableMap);
      GrouperUtil.booleanValue(result);
      
      Pattern patternGroupWithDoubleQuote = Pattern.compile("entity\\.memberOf\\s*\\(\\s*\"");
      Matcher matcherGroupWithDoubleQuote = patternGroupWithDoubleQuote.matcher(script);
      if (matcherGroupWithDoubleQuote.matches()) {
        return GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptInvalidDoubleQuote");
      }
      // TODO make sure count of entity.memberOf equals count of regex
      // TODO make sure group names exist
      // TODO make sure user can read group names
      
      return null;
    } catch (RuntimeException re) {
      return GrouperUtil.getFullStackTrace(re);
    }

  }
  
  public static Object runScriptStatic(String script, Map<String, Object> variableMap) {
    Object result = GrouperUtil.substituteExpressionLanguageScript(script, variableMap, true, false, true);
    return result;
    
  }
  
}
