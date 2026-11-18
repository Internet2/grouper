/**
 * Copyright 2026 Internet2
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
 */
package edu.internet2.middleware.grouper.app.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/**
 * <pre>
 * Runs the emailed group members export (ReportConfigType.GROUP_MEMBERS_EXPORT) as the subject who
 * requested it, and binds the download of the result to that subject.
 *
 * Why: the export is requested by anyone with READ on the group, but GrouperReportJob runs every
 * report as root (it needs root to read the report config attributes and write the instance).
 * Subject attribute filtering (subject source requireGroupNameForView, the SubjectCustomizer) is
 * skipped for root, so the emailed "all fields" file could hold attributes that "Download now"
 * hides from the same user.  The data part of the export is therefore run in a session of the
 * requester, READ is checked again when the job runs, and the subjects are filtered for the
 * requester before any attribute is read.
 *
 * Once two readers of a group can get different files, the download must also be bound to the
 * requester (else any other reader with the report instance id gets the requester's file, and the
 * one-time download deletes it).  isRequester() is that check, and it reads the stored requester
 * with the same parser as the generation side.
 *
 * The requester is stored on the system-managed report config as sqlConfig = sourceId::subjectId
 * (set by UiV2GroupImport.groupExportEmailSubmit).
 *
 * Only uses APIs present in every release that has the emailed export (7.6.0 and later).
 * </pre>
 */
public class GroupMembersExportAsRequester {

  /**
   * separator between source id and subject id in the stored requester
   */
  private static final String REQUESTER_SEPARATOR = "::";

  /**
   * not meant to be instantiated
   */
  private GroupMembersExportAsRequester() {
  }

  // ---------------------------------------------------------------------------------------------
  // requester parsing (shared by the job and the download so both read sqlConfig the same way)
  // ---------------------------------------------------------------------------------------------

  /**
   * parse the requester stored on the export config
   * @param configBean
   * @return array of sourceId, subjectId, or null if blank or malformed
   */
  public static String[] requesterSourceIdSubjectId(GrouperReportConfigurationBean configBean) {
    if (configBean == null) {
      return null;
    }
    String requester = configBean.getSqlConfig();
    if (StringUtils.isBlank(requester) || !requester.contains(REQUESTER_SEPARATOR)) {
      return null;
    }
    String sourceId = StringUtils.substringBefore(requester, REQUESTER_SEPARATOR);
    String subjectId = StringUtils.substringAfter(requester, REQUESTER_SEPARATOR);
    if (StringUtils.isBlank(sourceId) || StringUtils.isBlank(subjectId)) {
      return null;
    }
    return new String[] {sourceId, subjectId};
  }

  /**
   * find the subject who requested the export.  Fails closed: throws if the requester is missing,
   * malformed, or cannot be found, so the job records an error and produces no file
   * @param configBean
   * @return the requester subject, never null
   */
  public static Subject requesterSubject(GrouperReportConfigurationBean configBean) {
    String[] sourceIdSubjectId = requesterSourceIdSubjectId(configBean);
    if (sourceIdSubjectId == null) {
      throw new RuntimeException("Group members export config is missing the requesting subject (sqlConfig), "
          + "not running the export: " + (configBean == null ? null : configBean.getReportConfigName()));
    }
    Subject subject = SubjectFinder.findByIdAndSource(sourceIdSubjectId[1], sourceIdSubjectId[0], false);
    if (subject == null) {
      throw new RuntimeException("Cannot find the requesting subject of group members export: "
          + sourceIdSubjectId[0] + REQUESTER_SEPARATOR + sourceIdSubjectId[1]);
    }
    return subject;
  }

  /**
   * see if a subject is the one who requested this export.  Source id and subject id must both
   * match exactly; a blank or malformed stored requester matches nobody
   * @param configBean
   * @param subject e.g. the logged in user
   * @return true if the subject is the requester
   */
  public static boolean isRequester(GrouperReportConfigurationBean configBean, Subject subject) {
    if (subject == null) {
      return false;
    }
    String[] sourceIdSubjectId = requesterSourceIdSubjectId(configBean);
    if (sourceIdSubjectId == null) {
      return false;
    }
    return StringUtils.equals(sourceIdSubjectId[0], subject.getSourceId())
        && StringUtils.equals(sourceIdSubjectId[1], subject.getId());
  }

  // ---------------------------------------------------------------------------------------------
  // generation as the requester
  // ---------------------------------------------------------------------------------------------

  /**
   * Run the handler in a grouper session of the requester (also the static session, since subject
   * filtering reads GrouperSession.staticGrouperSession()).  READ (or ADMIN) on the group is checked
   * again now, since it may have been removed after the request.  The handler gets the requester
   * session and should find the group again with it
   * @param configBean the export config (requester in sqlConfig)
   * @param group the group being exported (found as root by the caller)
   * @param grouperSessionHandler the data work
   * @return what the handler returns
   */
  public static Object callbackAsRequester(GrouperReportConfigurationBean configBean, Group group,
      GrouperSessionHandler grouperSessionHandler) {

    if (group == null) {
      throw new RuntimeException("Group members export has no group");
    }

    // resolved under the caller's session (root in the report job)
    Subject requester = requesterSubject(configBean);

    GrouperSession requesterSession = null;
    try {
      // do not replace the thread local root session of the job; callbackGrouperSession below
      // makes the requester session the static one only for the data work
      requesterSession = GrouperSession.start(requester, false);

      Group groupAsRequester = GroupFinder.findByUuid(requesterSession, group.getId(), false);
      if (groupAsRequester == null || !PrivilegeHelper.canRead(requesterSession, groupAsRequester, requester)) {
        throw new RuntimeException("Requester " + configBean.getSqlConfig()
            + " can no longer read group " + group.getName() + ", not running the group members export");
      }

      return GrouperSession.callbackGrouperSession(requesterSession, grouperSessionHandler);
    } finally {
      GrouperSession.stopQuietly(requesterSession);
    }
  }

  /**
   * Filter resolved member subjects for the requester the way a user lookup is filtered
   * (subject source requireGroupNameForView, then the SubjectCustomizer, if configured).
   * Each subject is cloned first: subjects come from the shared subject cache (possibly resolved
   * earlier as root), and filtering removes attributes, so filtering the cached object would change
   * what other callers see.  The clone is a SubjectImpl, so requireGroupNameForView also applies to
   * subjects that were resolved lazily.
   * Must be called while the requester session is the static session (see callbackAsRequester).
   * @param requesterSession
   * @param subjects resolved subjects of the members
   * @return the filtered copies; a subject the customizer drops is not in the result
   */
  public static Set<Subject> filterForRequester(GrouperSession requesterSession, Set<Subject> subjects) {
    Set<Subject> copies = new LinkedHashSet<Subject>();
    if (subjects == null) {
      return copies;
    }
    for (Subject subject : subjects) {
      if (subject == null) {
        continue;
      }
      // member subjects are usually LazySubjects that resolve on first use, here in the requester
      // session.  A subject the requester may not see (subject customizer / source filter) resolves
      // to null and the clone throws; leave it out, so like a subject the filter drops, that member
      // gets the id columns only.  Must not fail the whole export for one member
      try {
        copies.add(SubjectImpl.cloneSubject(subject));
      } catch (RuntimeException re) {
        continue;
      }
    }
    if (copies.size() == 0) {
      return copies;
    }
    Set<Subject> filtered = SubjectFinder.filterSubjects(requesterSession, copies, null);
    return filtered == null ? new LinkedHashSet<Subject>() : filtered;
  }

}
