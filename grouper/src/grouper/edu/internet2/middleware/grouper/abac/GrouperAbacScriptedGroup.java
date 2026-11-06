/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.abac;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

/**
 * making a group a scripted (JEXL) group, changing its script, and stopping it being one, with the
 * checks the UI's loader screen makes before it saves.  the UI calls these, so any other way of
 * saving a script can make the same checks and the same save.
 *
 * <p>who may do this is not checked here: callers must check PrivilegeHelper.canEditAbacLoader for
 * the user first.  the UI's loader screen does, except when a scripted group is switched to another
 * loader type, where only the new type's privilege is checked (as it always has been).  the script
 * and its settings are attribute values only the system can write, so
 * the save runs as root, as the UI's always has.  the methods which read or write as root without
 * checking anything start with internal_, so a call with no check before it stands out.</p>
 */
public class GrouperAbacScriptedGroup {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperAbacScriptedGroup.class);

  /**
   * a script is kept in an attribute value, which is a varchar(4000) column.  counted in UTF-8
   * bytes, since that is how oracle counts it
   */
  public static final int MAX_SCRIPT_BYTES = 4000;

  /**
   * what checkScriptForSave found
   */
  public static class SaveCheck {

    /**
     * false if the script cannot be saved.  kept apart from the message, so a refusal whose text is
     * missing is still a refusal
     */
    private boolean allowed = true;

    /**
     * why the script cannot be saved, worded with the UI's text, as plain text which can have line
     * breaks; or null if it can be saved
     */
    private String errorMessage;

    /**
     * @return false if the script cannot be saved, whatever the message says
     */
    public boolean isAllowed() {
      return this.allowed;
    }

    /** the analysis, if it got that far */
    private GrouperJexlScriptAnalysis grouperJexlScriptAnalysis;

    /** how many members the script selects, or -1 if not counted */
    private int populationCount = -1;

    /**
     * @return why the script cannot be saved, worded with the UI's text, as plain text which can
     * have line breaks; or null if it can be saved
     */
    public String getErrorMessage() {
      return this.errorMessage;
    }

    /**
     * @return the analysis, if it got that far
     */
    public GrouperJexlScriptAnalysis getGrouperJexlScriptAnalysis() {
      return this.grouperJexlScriptAnalysis;
    }

    /**
     * @return how many members the script selects, or -1 if not counted
     */
    public int getPopulationCount() {
      return this.populationCount;
    }
  }

  /**
   * check that a script fits in MAX_SCRIPT_BYTES.  the script is kept in a varchar(4000) column,
   * which oracle counts in bytes.  the save and both Analyze paths (UI and MCP) call this before
   * parsing, so a script too long to save is never parsed or counted
   * @param script the script, not blank
   * @return what was found
   */
  public static SaveCheck checkScriptLength(String script) {
    SaveCheck saveCheck = new SaveCheck();
    if (script.getBytes(StandardCharsets.UTF_8).length > MAX_SCRIPT_BYTES) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptTooLong");
    }
    return saveCheck;
  }

  /**
   * the checks the UI makes before it saves a script, in its order: a script is given, it fits in
   * MAX_SCRIPT_BYTES, the group is not a composite, GrouperAbac.validScript finds nothing wrong
   * and the script can be parsed, the script does not use the group it is for, the analysis in
   * save mode gives no error and no warning, every group the script uses exists by that name and
   * the user can READ it (groupNotFoundMessage), and it selects no more members than the user is
   * allowed.  the analysis errors cover a group the user can see but not read, and a data field
   * whose privacy realm they cannot update.  a group they cannot see, or one which is not there,
   * the analysis treats as empty, and groupNotFoundMessage refuses.  in save mode the only warning
   * is that unresolvable or deleted entities match the script, which has always refused a save.
   * a group just made and not in the sql cache yet counts as empty for the size check until the
   * cache has it, which is accepted, since that is a minute or so.  it stops at the first, and
   * words it the way the analysis words its own messages.  only the whole script is counted,
   * since the size check is all a save needs.  only direct self reference is refused: a script
   * using a group which itself contains this one still feeds back into it
   * @param grouperDataEngine loaded data engine
   * @param group the group the script is for
   * @param script the script
   * @param effectiveSourceIds the subject sources the script will use
   * @param loggedInSubject who is saving it
   * @return what was found
   */
  public static SaveCheck checkScriptForSave(GrouperDataEngine grouperDataEngine, Group group,
      String script, Set<String> effectiveSourceIds, Subject loggedInSubject) {

    SaveCheck saveCheck = new SaveCheck();

    if (StringUtils.isBlank(script)) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptRequired");
      return saveCheck;
    }

    SaveCheck lengthCheck = checkScriptLength(script);
    if (!lengthCheck.isAllowed()) {
      return lengthCheck;
    }

    // a composite's members come from its factors.  the sync adds and removes members directly,
    // which a composite cannot have
    if (group.isHasComposite()) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptComposite");
      return saveCheck;
    }

    SaveCheck parseCheck = checkScriptParses(grouperDataEngine, script);
    if (!parseCheck.isAllowed()) {
      return parseCheck;
    }

    // before the analysis, which runs count queries.  a group made of its own members either does
    // nothing, only ever grows, or with a not flips its members on every sync
    if (refersToGroup(parseCheck.getGrouperJexlScriptAnalysis(), group)) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptRefersToItself");
      return saveCheck;
    }

    GrouperJexlScriptAnalysis grouperJexlScriptAnalysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, loggedInSubject, false, effectiveSourceIds, false, false);
    saveCheck.grouperJexlScriptAnalysis = grouperJexlScriptAnalysis;

    // e.g. a group in the script the user can see but not read, or a data field whose privacy
    // realm they cannot update.  a group they cannot see is not an error here: the analysis treats
    // it as missing, and groupNotFoundMessage refuses it below
    if (StringUtils.isNotBlank(grouperJexlScriptAnalysis.getErrorMessage())) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = plainText(grouperJexlScriptAnalysis.getErrorMessage());
      return saveCheck;
    }
    if (StringUtils.isNotBlank(grouperJexlScriptAnalysis.getWarningMessage())) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = plainText(grouperJexlScriptAnalysis.getWarningMessage());
      return saveCheck;
    }

    String groupNotFoundMessage = groupNotFoundMessage(grouperJexlScriptAnalysis, loggedInSubject);
    if (groupNotFoundMessage != null) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = groupNotFoundMessage;
      return saveCheck;
    }

    if (GrouperUtil.length(grouperJexlScriptAnalysis.getGrouperJexlScriptParts()) > 0) {
      saveCheck.populationCount = grouperJexlScriptAnalysis.getGrouperJexlScriptParts().get(0).getPopulationCount();
      Integer maxMembers = GrouperAbac.maxAbacMembershipSizeForSubject(loggedInSubject);
      if (maxMembers != null && saveCheck.populationCount > maxMembers.intValue()) {
        saveCheck.allowed = false;
        saveCheck.errorMessage = tooManyMembersMessage(saveCheck.populationCount, maxMembers.intValue());
        return saveCheck;
      }
    }

    return saveCheck;
  }

  /**
   * @param populationCount how many members a script selects
   * @param maxMembers the most members the user may give a scripted group
   * @return the refusal, in words
   */
  public static String tooManyMembersMessage(int populationCount, int maxMembers) {
    String message = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptMaxSizeExceeded");
    message = Strings.CS.replace(message, "##count##", String.valueOf(populationCount));
    return Strings.CS.replace(message, "##max##", String.valueOf(maxMembers));
  }

  /**
   * check that a script parses, for a save or a preview.  only the parser's own error is shown.
   * the rest of building the script's parts can fail too, before the privacy realm checks run, and
   * its messages can carry what the user may not be allowed to see: a globalAttributeValue is read
   * while the parts are built, so a value which is not a number comes back as the text of a
   * NumberFormatException, and others say whether a data field is multi-valued or assigned.  those
   * go to the log, and the user gets the generic invalid script message.  a database error, a
   * GrouperDAOException or anything with a SQLException as a cause, is thrown as it is, to the
   * generic error, since it is not the script's fault
   * @param grouperDataEngine loaded data engine
   * @param script the script
   * @return what was found, with the parsed script as the analysis if it parses
   */
  public static SaveCheck checkScriptParses(GrouperDataEngine grouperDataEngine, String script) {

    SaveCheck saveCheck = new SaveCheck();

    String invalidMessage = GrouperAbac.validScript(script, grouperDataEngine);
    if (StringUtils.isBlank(invalidMessage)) {
      try {
        saveCheck.grouperJexlScriptAnalysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScript(grouperDataEngine, script);
      } catch (JexlException jexlException) {
        // JEXL starts its message with where it was called from in Grouper and a line and column,
        // e.g. GrouperLoaderJexlScriptFullSync.analyzeJexlScript:685@1:17.  the place in Grouper
        // is the same for every script, and the line and column count from the script after it
        // was trimmed and joined onto one line, so neither helps the user find the problem.  just
        // the parser's description is shown
        invalidMessage = jexlException.getMessage();
        JexlInfo jexlInfo = jexlException.getInfo();
        if (invalidMessage != null && jexlInfo != null) {
          invalidMessage = StringUtils.trim(Strings.CS.removeStart(invalidMessage, jexlInfo.toString()));
        }
        invalidMessage = StringUtils.defaultIfBlank(invalidMessage, jexlException.getClass().getSimpleName());
      } catch (GrouperDAOException grouperDAOException) {
        // a database failure is not the script's fault, so it is not called an invalid script
        throw grouperDAOException;
      } catch (RuntimeException runtimeException) {
        // GcDbAccess wraps a database error in a plain RuntimeException, e.g. reading global values
        if (causedBySqlException(runtimeException)) {
          throw runtimeException;
        }
        // usually the user's mistake, e.g. a data field name which does not exist
        LOG.warn("Error building the parts of a scripted group script", runtimeException);
        saveCheck.allowed = false;
        saveCheck.errorMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptInvalid");
        return saveCheck;
      }
    }
    if (!StringUtils.isBlank(invalidMessage)) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptInvalid")
          + "\n" + invalidMessage;
    }
    return saveCheck;
  }

  /**
   * @param throwable an exception
   * @return true if a SQLException is in its cause chain, i.e. it is a database error and not the
   * script's fault
   */
  private static boolean causedBySqlException(Throwable throwable) {
    Throwable current = throwable;
    // bounded, in case of a cause loop
    for (int i = 0; current != null && i < 20; i++) {
      if (current instanceof SQLException) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  /**
   * @param grouperJexlScriptAnalysis an analyzed script
   * @param group a group
   * @return true if the script uses the group, by memberOf, memberOfAny, recentMemberOf or a bare
   * group name
   */
  public static boolean refersToGroup(GrouperJexlScriptAnalysis grouperJexlScriptAnalysis, Group group) {
    for (String groupName : GrouperUtil.nonNull(grouperJexlScriptAnalysis.getRecentMemberOfGroupNames())) {
      if (Strings.CS.equals(groupName, group.getName())) {
        return true;
      }
    }
    for (GrouperJexlScriptPart grouperJexlScriptPart : GrouperUtil.nonNull(grouperJexlScriptAnalysis.getGrouperJexlScriptParts())) {
      for (MultiKey argument : GrouperUtil.nonNull(grouperJexlScriptPart.getArguments())) {
        if (Strings.CS.equals("group", (String)argument.getKey(0))
            && Strings.CS.equals(group.getName(), (String)argument.getKey(2))) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * every group a script uses has to exist by that exact name, and the user has to be able to
   * READ it.  the analysis checks READ only on a group it finds in the sql cache, and treats any
   * other as empty: one which does not exist yet, one not cached yet because it was just made, or
   * one named by its old (alternate) name.  saved like that, a group made or renamed to that name
   * later would put its members in this one with nobody having checked READ on it.  one message
   * for all of these, so it does not tell anybody whether a group they cannot see exists.  an
   * existing group takes a little longer to check than a missing one, which is accepted as far
   * weaker than a message saying so
   * @param grouperJexlScriptAnalysis an analyzed script
   * @param loggedInSubject who is saving it
   * @return what is wrong, naming the group, or null if every group is there and readable
   */
  public static String groupNotFoundMessage(GrouperJexlScriptAnalysis grouperJexlScriptAnalysis,
      final Subject loggedInSubject) {

    final Set<String> groupNames = new TreeSet<String>(GrouperUtil.nonNull(grouperJexlScriptAnalysis.getRecentMemberOfGroupNames()));
    for (GrouperJexlScriptPart grouperJexlScriptPart : GrouperUtil.nonNull(grouperJexlScriptAnalysis.getGrouperJexlScriptParts())) {
      for (MultiKey argument : GrouperUtil.nonNull(grouperJexlScriptPart.getArguments())) {
        if (Strings.CS.equals("group", (String)argument.getKey(0))) {
          groupNames.add((String)argument.getKey(2));
        }
      }
    }

    // as root, so a group the user cannot see is found, and then refused for READ, rather than
    // reading as not there.  the message is the same either way
    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        for (String groupName : groupNames) {
          // not from the finder's caches, which on another node can still hold a group under a name
          // it no longer has
          Group referencedGroup = GroupFinder.findByName(grouperSession, groupName, false,
              new QueryOptions().secondLevelCache(false));
          // the finder also matches an old name, and on some databases a name in other case
          if (referencedGroup == null || !Strings.CS.equals(referencedGroup.getName(), groupName)
              || !referencedGroup.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
            String message = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptGroupNotFound");
            return message + " '" + groupName + "'";
          }
        }
        return null;
      }
    });
  }

  /**
   * text from the analysis, which is written for the UI and can have tags in it, as plain text:
   * line breaks kept, tags removed and entities turned back into characters
   * @param html text from the analysis
   * @return the text, or null if null
   */
  public static String plainText(String html) {
    if (html == null) {
      return null;
    }
    String text = html.replaceAll("(?i)<br\\s*/?>", "\n");
    text = text.replaceAll("<[^>]*>", "");
    return GrouperUtil.xmlEscape(text, false).trim();
  }

  /**
   * make the group a scripted group with this script, or change its script, in one transaction.
   * the group's subject sources are kept in an attribute only when they differ from the defaults,
   * as the UI keeps them.  the members change when the scripted group sync next runs, not here
   * @param group the group
   * @param script the script, already checked with checkScriptForSave
   * @param sourceIds the subject sources to keep for the group, or null or empty for the defaults
   * @return INSERT if it was not a scripted group, UPDATE if something changed, NO_CHANGE if not
   */
  public static SaveResultType internal_saveScript(final Group group, final String script, final Set<String> sourceIds) {

    return (SaveResultType)GrouperTransaction.callbackGrouperTransaction(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {

        return GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

            AttributeDefName marker = markerAttributeDefName();
            if (marker == null) {
              throw new RuntimeException("Cant find jexl script attribute!");
            }

            SaveResultType saveResultType = SaveResultType.NO_CHANGE;

            AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, marker, false, false);
            if (attributeAssign == null) {
              attributeAssign = group.getAttributeDelegate().assignAttribute(marker).getAttributeAssign();
              saveResultType = SaveResultType.INSERT;
            }

            String scriptName = GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT;
            if (!Strings.CS.equals(script, attributeAssign.getAttributeValueDelegate().retrieveValueString(scriptName))) {
              attributeAssign.getAttributeValueDelegate().assignValue(scriptName, script);
              if (saveResultType == SaveResultType.NO_CHANGE) {
                saveResultType = SaveResultType.UPDATE;
              }
            }

            String sourceIdsName = GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_SUBJECT_SOURCE_IDS;
            String currentSourceIds = attributeAssign.getAttributeValueDelegate().retrieveValueString(sourceIdsName);
            if (sourceIds == null || sourceIds.isEmpty() || sourceIds.equals(GrouperAbac.globalDefaultSubjectSourceIds())) {
              if (!StringUtils.isBlank(currentSourceIds)) {
                attributeAssign.getAttributeDelegate().removeAttributeByName(sourceIdsName);
                if (saveResultType == SaveResultType.NO_CHANGE) {
                  saveResultType = SaveResultType.UPDATE;
                }
              }
            } else {
              // alphabetically sorted, comma separated
              String sortedSourceIds = GrouperUtil.join(new TreeSet<String>(sourceIds).iterator(), ", ");
              if (!Strings.CS.equals(sortedSourceIds, currentSourceIds)) {
                attributeAssign.getAttributeValueDelegate().assignValue(sourceIdsName, sortedSourceIds);
                if (saveResultType == SaveResultType.NO_CHANGE) {
                  saveResultType = SaveResultType.UPDATE;
                }
              }
            }

            return saveResultType;
          }
        });
      }
    });
  }

  /**
   * stop the group being a scripted group.  the script and its settings are kept on the marker's
   * assignment, so they go with it.  the members are left as they are, as direct members, since the
   * sync only looks at groups with the marker
   * @param group the group
   * @return DELETE if it was a scripted group, NO_CHANGE if not
   */
  public static SaveResultType internal_removeScript(final Group group) {

    return (SaveResultType)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        AttributeDefName marker = markerAttributeDefName();
        if (marker == null || !group.getAttributeDelegate().hasAttribute(marker)) {
          return SaveResultType.NO_CHANGE;
        }
        group.getAttributeDelegate().removeAttribute(marker);
        return SaveResultType.DELETE;
      }
    });
  }

  /**
   * @param group a group
   * @return true if it has the scripted group marker, which is what internal_removeScript removes,
   * script or no script.  read as root, so check who may see it
   */
  public static boolean internal_isScriptedGroup(final Group group) {
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return markerAssignment(group) != null;
      }
    });
  }

  /**
   * @param group a group
   * @return its script, or null if it is not a scripted group.  read as root, so check who may see it
   */
  public static String internal_retrieveScript(final Group group) {
    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        AttributeAssign attributeAssign = markerAssignment(group);
        if (attributeAssign == null) {
          return null;
        }
        return attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT);
      }
    });
  }

  /**
   * @param group a group
   * @return the subject sources kept for it, or null if it uses the defaults or is not a scripted
   * group.  read as root, so check who may see it
   */
  public static Set<String> internal_retrieveSourceIds(final Group group) {
    return (Set<String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        AttributeAssign attributeAssign = markerAssignment(group);
        if (attributeAssign == null) {
          return null;
        }
        String sourceIds = attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_SUBJECT_SOURCE_IDS);
        if (StringUtils.isBlank(sourceIds)) {
          return null;
        }
        return new LinkedHashSet<String>(GrouperUtil.splitTrimToList(sourceIds, ","));
      }
    });
  }

  /**
   * @param group a group
   * @return how many enabled immediate members it has, which are the members who stay when it stops
   * being a scripted group.  not checked for privileges, so check who may see it
   */
  public static int internal_retrieveImmediateMemberCount(Group group) {
    return new GcDbAccess().sql("select count(1) from grouper_memberships gms where "
        + GrouperLoaderJexlScriptFullSync.IMMEDIATE_MEMBER_CONDITION + " and gms.enabled = 'T'")
        .addBindVar(group.getId()).addBindVar(Group.getDefaultList().getId()).select(Integer.class);
  }

  /**
   * @param group a group
   * @return the other kind of loader the group has, e.g. "SQL loader", or null if none.  a group
   * has one loader, and the UI's loader screen is where one is switched for another.  read as
   * root, so check who may see it
   */
  public static String internal_retrieveOtherLoaderType(final Group group) {
    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        AttributeDefName sqlLoader = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
            GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc")
            + ":legacy:attribute:legacyGroupType_grouperLoader", false);
        if (sqlLoader != null && group.getAttributeDelegate().hasAttribute(sqlLoader)) {
          return "SQL loader";
        }

        AttributeDefName ldapLoader = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
            LoaderLdapUtils.grouperLoaderLdapName(), false);
        if (ldapLoader != null && group.getAttributeDelegate().hasAttribute(ldapLoader)) {
          return "LDAP loader";
        }

        AttributeDefName recentMemberships = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
            GrouperRecentMemberships.recentMembershipsStemName() + ":"
            + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, false);
        if (recentMemberships != null && group.getAttributeDelegate().hasAttribute(recentMemberships)) {
          return "recent memberships loader";
        }

        return null;
      }
    });
  }

  /**
   * @param group a group
   * @return the group's scripted group marker assignment, or null.  call as root
   */
  private static AttributeAssign markerAssignment(Group group) {
    AttributeDefName marker = markerAttributeDefName();
    if (marker == null) {
      return null;
    }
    return group.getAttributeDelegate().retrieveAssignment(null, marker, false, false);
  }

  /**
   * @return the scripted group marker, or null if it is not there
   */
  private static AttributeDefName markerAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
        GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, false);
  }

}
