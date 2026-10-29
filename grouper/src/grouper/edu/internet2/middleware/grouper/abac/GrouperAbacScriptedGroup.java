/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.abac;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
   * the checks the UI makes before it saves a script, in its order: a script is given,
   * GrouperAbac.validScript finds nothing wrong, the analysis in save mode gives no error and no
   * warning, and it selects no more members than the user is allowed.  the errors cover READ on
   * each group it uses and update on each data field's privacy realm.  in save mode the only
   * warning is that unresolvable or deleted entities match the script, which has always refused
   * a save.  it stops at the first, and
   * words it the way the analysis words its own messages.  only the whole script is counted, since
   * the size check is all a save needs
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

    String invalidMessage = GrouperAbac.validScript(script, grouperDataEngine);
    if (!StringUtils.isBlank(invalidMessage)) {
      saveCheck.allowed = false;
      saveCheck.errorMessage = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptInvalid")
          + "\n" + invalidMessage;
      return saveCheck;
    }

    GrouperJexlScriptAnalysis grouperJexlScriptAnalysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, loggedInSubject, false, effectiveSourceIds, false, false);
    saveCheck.grouperJexlScriptAnalysis = grouperJexlScriptAnalysis;

    // e.g. a group in the script the user cannot read, or a data field whose privacy realm they
    // cannot update
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

    if (GrouperUtil.length(grouperJexlScriptAnalysis.getGrouperJexlScriptParts()) > 0) {
      saveCheck.populationCount = grouperJexlScriptAnalysis.getGrouperJexlScriptParts().get(0).getPopulationCount();
      Integer maxMembers = GrouperAbac.maxAbacMembershipSizeForSubject(loggedInSubject);
      if (maxMembers != null && saveCheck.populationCount > maxMembers.intValue()) {
        saveCheck.allowed = false;
        String message = GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptMaxSizeExceeded");
        message = Strings.CS.replace(message, "##count##", String.valueOf(saveCheck.populationCount));
        message = Strings.CS.replace(message, "##max##", String.valueOf(maxMembers.intValue()));
        saveCheck.errorMessage = message;
        return saveCheck;
      }
    }

    return saveCheck;
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
   * @return the scripted group marker, or null if it is not there
   */
  private static AttributeDefName markerAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
        GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, false);
  }

}
