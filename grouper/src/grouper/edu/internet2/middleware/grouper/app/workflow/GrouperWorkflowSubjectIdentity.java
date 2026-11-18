package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_ACTION;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Source-bound subject identity for workflows.  In Grouper a subject is identified by source id
 * plus subject id; two sources can have a subject with the same id (e.g. a group uuid in g:gsa
 * and a person id in a jdbc source).  Workflow log entries and approval states store both, but
 * the instance service used to compare and resolve by subject id only, so a same-id subject
 * from another source could view an instance, be resolved as the initiator (and be added to the
 * group on completion) or as an explicit approver.
 *
 * Everything here requires both the source id and the subject id.  A record with a blank source
 * id does not match anyone and resolves to no subject (fail closed); workflow has recorded the
 * source since before it shipped, so this only affects hand-edited data.
 *
 * In its own class so the two workflow files only swap call sites, and one version ships to
 * every release.
 * </pre>
 */
public class GrouperWorkflowSubjectIdentity {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowSubjectIdentity.class);

  /**
   * no instances
   */
  private GrouperWorkflowSubjectIdentity() {
  }

  /**
   * is this log entry for this subject (same source id and same subject id)
   * @param entry
   * @param subject
   * @return true if both ids are there and match
   */
  public static boolean matches(GrouperWorkflowInstanceLogEntry entry, Subject subject) {
    if (entry == null || subject == null) {
      return false;
    }
    if (StringUtils.isBlank(entry.getSubjectId()) || StringUtils.isBlank(entry.getSubjectSourceId())) {
      return false;
    }
    return StringUtils.equals(entry.getSubjectId(), subject.getId())
        && StringUtils.equals(entry.getSubjectSourceId(), subject.getSourceId());
  }

  /**
   * are these the same subject (same source id and same subject id)
   * @param subject1
   * @param subject2
   * @return true if same
   */
  public static boolean sameSubject(Subject subject1, Subject subject2) {
    if (subject1 == null || subject2 == null) {
      return false;
    }
    if (StringUtils.isBlank(subject1.getId()) || StringUtils.isBlank(subject1.getSourceId())) {
      return false;
    }
    return StringUtils.equals(subject1.getId(), subject2.getId())
        && StringUtils.equals(subject1.getSourceId(), subject2.getSourceId());
  }

  /**
   * find the subject who initiated the workflow instance, by source and id from the initiate
   * log entry
   * @param instance
   * @param exceptionIfNull true to throw if there is no such subject (or no source / entry)
   * @return the subject or null
   */
  public static Subject findInitiator(GrouperWorkflowInstance instance, boolean exceptionIfNull) {

    GrouperWorkflowInstanceLogEntry entry = null;
    if (instance != null && instance.getGrouperWorkflowInstanceLogEntries() != null) {
      entry = instance.getGrouperWorkflowInstanceLogEntries().getLogEntryByActionName(INITIATE_ACTION);
    }

    String subjectId = entry == null ? null : entry.getSubjectId();
    String sourceId = entry == null ? null : entry.getSubjectSourceId();

    return findByIdAndSource(subjectId, sourceId, exceptionIfNull, "workflow initiator");
  }

  /**
   * find the explicit approver subject of an approval state, by source and id
   * @param approvalState
   * @return the subject or null if none configured or not found
   */
  public static Subject findApprover(GrouperWorkflowApprovalState approvalState) {
    if (approvalState == null || StringUtils.isBlank(approvalState.getApproverSubjectId())) {
      return null;
    }
    return findByIdAndSource(approvalState.getApproverSubjectId(),
        approvalState.getApproverSubjectSourceId(), false, "workflow approver");
  }

  /**
   * find by id and source, never by id alone
   * @param subjectId
   * @param sourceId
   * @param exceptionIfNull
   * @param label for messages
   * @return the subject or null
   */
  private static Subject findByIdAndSource(String subjectId, String sourceId, boolean exceptionIfNull, String label) {

    if (StringUtils.isBlank(subjectId) || StringUtils.isBlank(sourceId)) {
      // fail closed: do not fall back to an id only lookup, which could pick another source
      String message = label + " has no " + (StringUtils.isBlank(subjectId) ? "subject id" : "subject source id")
          + ", subject id: " + subjectId;
      if (exceptionIfNull) {
        throw new RuntimeException(message);
      }
      LOG.error(message);
      return null;
    }

    if (exceptionIfNull) {
      return SubjectFinder.findByIdAndSource(subjectId, sourceId, true);
    }

    try {
      return SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
    } catch (RuntimeException re) {
      // e.g. the source is no longer configured; treat as not found like a missing subject
      LOG.error("Cannot find " + label + " " + sourceId + " :: " + subjectId, re);
      return null;
    }
  }
}
