/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 * Class to process incoming events and convert to operations in Grouper, running with
 * root privileges
 *
 */
public class EsbListener {

  private GrouperSession grouperSession;

  private static final Log LOG = GrouperUtil.getLog(EsbListener.class);

  /**
   * 
   * @param the jsonString representation of a populated {@link EsbListenerEvent} class
   * @param an initialised grouperSession
   * @return returnMessage - a human readable string containing results of operations
   * to return to calling client
   */
  public String processEvent(String jsonString, GrouperSession grouperSession) {
    this.grouperSession = grouperSession;
    String returnMessage = "";
    EsbListenerEvent event = (EsbListenerEvent) GrouperUtil.jsonConvertFrom(jsonString,
        EsbListenerEvent.class);

    String subjectId = event.getSubjectId();
    if (subjectId == null || subjectId.equals("")) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("SubjectId null or blank");
      }
      return "Fatal error: subject not found";

    }
    Subject subject = SubjectFinder.findById(subjectId, false);
    if (subject == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("SubjectId " + subjectId + " not found");
      }
      return "Error: subject not found";
    }
    returnMessage = returnMessage
        + this.processMembershipChanges(event.getAddMembershipGroupNames(), "name",
            subject, true);
    returnMessage = returnMessage
        + this.processMembershipChanges(event.getAddMembershipGroupIds(), "id", subject,
            true);
    returnMessage = returnMessage
        + this.processMembershipChanges(event.getAddMembershipGroupExtensions(),
            "extension", subject, true);
    returnMessage = returnMessage
        + this.processMembershipChanges(event.getRemoveMembershipGroupNames(), "name",
            subject, false);
    returnMessage = returnMessage
        + this.processMembershipChanges(event.getRemoveMembershipGroupIds(), "id",
            subject, false);
    returnMessage = returnMessage
        + this.processMembershipChanges(event.getRemoveMembershipGroupExtensions(),
            "extension", subject, false);
    return returnMessage;

  }

  /**
   * Method to add and and remove subject membership to/from groups in registry
   * @param groups - array of group identifiers
   * @param searchType - method to use to search for group (depends on value in groups array)
   * @param subject - the subject to add/remove to/from groups
   * @param addOp - true is add memberships, false if delete memberships
   * @return returnMessage - simple human readable result to return to calling client
   */
  private String processMembershipChanges(String[] groups, String searchType,
      Subject subject, boolean addOp) {
    if (groups == null)
      return "";
    String returnMessage = "";
    if (LOG.isDebugEnabled()) {
      if (addOp) {
        LOG.debug("Adding " + subject.getId() + " to groups");
      } else {
        LOG.debug("Removing " + subject.getId() + " from groups");
      }
    }
    for (int i = 0; i < groups.length; i++) {
      Group group = null;
      if (searchType.equals("id")) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Finding group by id " + groups[i]);
        }
        group = GroupFinder.findByUuid(grouperSession, groups[i], false);
      } else if (searchType.equals("name")) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Finding group by name " + groups[i]);
        }
        group = GroupFinder.findByName(grouperSession, groups[i], false);
      } else if (searchType.equals("extension")) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Finding group by extension " + groups[i]);
        }
        group = GroupFinder
            .findByAttribute(grouperSession, "extension", groups[i], false);
      }
      if (group != null) {
        if (addOp) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Adding membership");
          }
          group.addMember(subject);
          returnMessage = returnMessage + "Added " + subject.getId()
              + " as member of group " + group.getName();
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Removing membership");
          }
          group.deleteMember(subject);
          returnMessage = returnMessage + "Deleted " + subject.getId()
              + " as member of group " + group.getName();
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Group id " + groups[i] + " not found");
        }
        return "Group id " + groups[i] + " not found\r\n";
      }
    }
    return returnMessage;
  }
}
