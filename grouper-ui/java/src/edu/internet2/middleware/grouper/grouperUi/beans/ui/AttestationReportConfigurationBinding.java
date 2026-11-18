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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Binds the report configuration of a folder's report attestation to the folder and to what
 * the logged in user may read.</p>
 *
 * <p>Why: the attestation settings store the report configuration's marker assignment id.  The
 * edit form only offers report configurations on the folder that the user can read
 * ({@link AttestationContainer#getAllReportConfigurationsOnFolder()}), but the save resolved the
 * submitted id globally, so a folder admin could bind any report configuration (of another folder
 * or a group) whose viewers group they are in.  And the folder attestation screen resolved the
 * stored id as root and showed the report's name and marker id to every folder admin, even one
 * who is not in the report's viewers group and cannot see it on the folder's Reports screen.</p>
 *
 * <ul>
 *   <li>{@link #reportConfigurationOnFolderForEdit(AttestationContainer, String)}: the save and the
 *   edit form accept only a report configuration in the same list the form offers (on this
 *   folder, readable by the user); anything else is null, so the existing "report name required"
 *   validation refuses the save.</li>
 *   <li>{@link #readableOrPlaceholder(GrouperReportConfigurationBean)}: the view shows the report
 *   configuration only to users who can read it, others see a "restricted report" placeholder
 *   with no marker id.</li>
 * </ul>
 *
 * <p>One version for every release: only uses APIs that exist from 2.6.19 through 7.x (no
 * commons-lang import, which moved to lang3 in 6.0.0).</p>
 */
public class AttestationReportConfigurationBinding {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(AttestationReportConfigurationBinding.class);

  /** text if the externalized text key is not there (patched releases ship no text change) */
  private static final String DEFAULT_RESTRICTED_TEXT = "(restricted report)";

  /**
   * no instances
   */
  private AttestationReportConfigurationBinding() {
  }

  // ---------------------------------------------------------------------------------------------
  // edit / save
  // ---------------------------------------------------------------------------------------------

  /**
   * Resolve the report configuration id submitted on the folder attestation edit form.  Only a
   * report configuration that the form offers is accepted: one on the folder of the screen that
   * the logged in user can read (the same rule as the drop down,
   * {@link AttestationContainer#getAllReportConfigurationsOnFolder()}).
   * @param attestationContainer container of the request
   * @param attributeAssignmentMarkerId the submitted id (marker assignment id of the report config)
   * @return the report configuration, or null if blank, unknown, on another folder or group, not
   * readable by the user, or there is no folder on the screen (group attestation)
   */
  public static GrouperReportConfigurationBean reportConfigurationOnFolderForEdit(
      AttestationContainer attestationContainer, String attributeAssignmentMarkerId) {

    if (GrouperUtil.isBlank(attributeAssignmentMarkerId) || attestationContainer == null) {
      return null;
    }

    // report attestation is only for folders: no folder on the screen means no report applies
    // (getAllReportConfigurationsOnFolder needs the folder of the screen)
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    if (guiStem == null || guiStem.getStem() == null) {
      LOG.warn("Report configuration " + attributeAssignmentMarkerId
          + " submitted without a folder on the screen, ignored");
      return null;
    }

    List<GrouperReportConfigurationBean> reportConfigurationsOnFolder = attestationContainer.getAllReportConfigurationsOnFolder();
    if (reportConfigurationsOnFolder != null) {
      for (GrouperReportConfigurationBean reportConfiguration : reportConfigurationsOnFolder) {
        if (reportConfiguration != null
            && attributeAssignmentMarkerId.equals(reportConfiguration.getAttributeAssignmentMarkerId())) {
          return reportConfiguration;
        }
      }
    }

    // log only ids: the name of a report the user cannot read is not logged on their behalf
    LOG.warn("Report configuration " + attributeAssignmentMarkerId + " is not a readable report configuration on folder "
        + guiStem.getStem().getId() + ", ignored, user: " + loggedInSubjectIdOrNull());
    return null;
  }

  // ---------------------------------------------------------------------------------------------
  // view
  // ---------------------------------------------------------------------------------------------

  /**
   * The report configuration to show on the folder attestation screen.
   * @param reportConfiguration the configured report (resolved as root), or null
   * @return null if none is configured; the report configuration if the logged in user can read
   * it (in its viewers group, or wheel / root); else a placeholder without marker id.  Fails
   * closed: if the user is not known or the check fails, the placeholder.
   */
  public static GrouperReportConfigurationBean readableOrPlaceholder(final GrouperReportConfigurationBean reportConfiguration) {

    if (reportConfiguration == null) {
      return null;
    }

    if (canRead(reportConfiguration)) {
      return reportConfiguration;
    }

    return restrictedPlaceholder();
  }

  /**
   * @param reportConfiguration report configuration
   * @return true if the logged in user can read the report configuration
   */
  private static boolean canRead(final GrouperReportConfigurationBean reportConfiguration) {
    try {
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      if (loggedInSubject == null) {
        return false;
      }
      // in a root callback like AttestationContainer.getAllReportConfigurationsOnFolder: in some
      // releases isCanRead starts a root session itself, and the callback keeps that from
      // becoming the session of the rest of the request
      Object result = GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return reportConfiguration.isCanRead(loggedInSubject);
        }
      });
      return Boolean.TRUE.equals(result);
    } catch (RuntimeException re) {
      LOG.warn("Cannot check if user can read report configuration " + reportConfiguration.getAttributeAssignmentMarkerId(), re);
      return false;
    }
  }

  /**
   * @return a report configuration with only a "restricted report" name: no marker id, viewers
   * group, query or anything else of the real report
   */
  public static GrouperReportConfigurationBean restrictedPlaceholder() {
    GrouperReportConfigurationBean placeholder = new GrouperReportConfigurationBean();
    placeholder.setReportConfigName(restrictedText());
    return placeholder;
  }

  /**
   * @param reportConfiguration report configuration or null
   * @return true if this is a placeholder from {@link #restrictedPlaceholder()} (no marker id)
   */
  public static boolean isRestrictedPlaceholder(GrouperReportConfigurationBean reportConfiguration) {
    return reportConfiguration != null && GrouperUtil.isBlank(reportConfiguration.getAttributeAssignmentMarkerId());
  }

  /**
   * @return the placeholder text (not escaped, the JSP escapes the report name)
   */
  private static String restrictedText() {
    String text = null;
    try {
      text = TextContainer.textOrNull("guiReportConfigRestrictedReference");
    } catch (RuntimeException re) {
      // no text container (e.g. not in a UI request): use the default
      text = null;
    }
    if (GrouperUtil.isBlank(text)) {
      text = DEFAULT_RESTRICTED_TEXT;
    }
    return text;
  }

  /**
   * @return id of the logged in subject for log messages, or null
   */
  private static String loggedInSubjectIdOrNull() {
    try {
      Subject subject = GrouperUiFilter.retrieveSubjectLoggedIn();
      return subject == null ? null : subject.getId();
    } catch (RuntimeException re) {
      return null;
    }
  }
}
