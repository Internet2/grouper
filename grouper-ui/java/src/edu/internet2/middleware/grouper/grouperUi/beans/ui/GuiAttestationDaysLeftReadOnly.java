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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Read-only calculation of attestation "days left until recertify" for the UI screens.</p>
 *
 * <p>Why this exists: the attestation screens (UiV2Attestation.retrieveGuiAttestation and
 * GuiAttestation.convertGroupIntoGuiAttestation) used to call
 * GrouperAttestationJob.updateCalculatedDaysUntilRecertify when the stored
 * attestationCalculatedDaysLeft value was blank.  That method is not a calculation: it persists
 * the value (and migrates legacy group and ancestor folder metadata), and the screens run it in a
 * root session for callers who only have READ on the group.  A read path must not write.</p>
 *
 * <p>This class mirrors GrouperAttestationJob.updateCalculatedDaysLeft with
 * resetDateCertified=false, but only returns the number.  Persisting the value stays with the
 * attestation daemon (which recalculates every assignment) and with the UPDATE-gated actions
 * (attest, clear, edit save).</p>
 *
 * <p>Only uses APIs present in every Grouper 5.x, 6.x and 7.x release (no commons-lang or
 * commons-lang3 import), so the same file serves every release range.</p>
 */
public class GuiAttestationDaysLeftReadOnly {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GuiAttestationDaysLeftReadOnly.class);

  /**
   * no instances
   */
  private GuiAttestationDaysLeftReadOnly() {
  }

  /**
   * calculate days left from the values on an attestation assignment, without writing anything.
   * Call inside the same (root) session that read the assignment.
   * @param attributeAssign the attestation marker assignment on the group or folder, or null
   * @return days left until recertify, or -1 if not applicable (no assignment, attestation off,
   * bad date)
   */
  public static int calculateDaysLeft(AttributeAssign attributeAssign) {
    if (attributeAssign == null) {
      return -1;
    }
    String hasAttestation = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
    String dateCertified = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName());
    String daysUntilRecertify = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName());
    return calculateDaysLeft(hasAttestation, dateCertified, daysUntilRecertify, attributeAssign.getId());
  }

  /**
   * calculate days left from already-read attestation values, without writing anything
   * @param hasAttestation value of attestationHasAttestation ("true", "false" or blank)
   * @param dateCertified value of attestationDateCertified (yyyy/MM/dd) or blank
   * @param configuredDaysUntilRecertify value of attestationDaysUntilRecertify or blank for the
   * default (attestation.default.daysUntilRecertify, 180)
   * @param idForLogging attribute assign id or owner name, only used in the log message
   * @return days left until recertify (0 if overdue or never certified), or -1 if attestation is
   * explicitly off or the date cannot be parsed
   */
  public static int calculateDaysLeft(String hasAttestation, String dateCertified,
      String configuredDaysUntilRecertify, String idForLogging) {

    // the job removes the calculated value when attestation is off, which the screens show as -1.
    // A blank value is a legacy assignment the migration would mark as having attestation, so
    // compute for it (this only affects what is displayed; nothing is stored)
    if (!GrouperUtil.isBlank(hasAttestation) && !GrouperUtil.booleanValue(hasAttestation, true)) {
      return -1;
    }

    int daysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
    if (!GrouperUtil.isBlank(configuredDaysUntilRecertify)) {
      try {
        daysUntilRecertify = Integer.valueOf(configuredDaysUntilRecertify.trim());
      } catch (NumberFormatException nfe) {
        // the job would throw here; for display keep the default rather than break the screen
        LOG.error("Invalid days until recertify: '" + configuredDaysUntilRecertify + "', " + idForLogging, nfe);
      }
    }

    // never certified: needs certification now (same as the job)
    if (GrouperUtil.isBlank(dateCertified)) {
      return 0;
    }

    Date lastCertifiedDate = null;
    try {
      lastCertifiedDate = new SimpleDateFormat("yyyy/MM/dd").parse(dateCertified);
    } catch (ParseException e) {
      LOG.error("Could not convert " + dateCertified + " to date, " + idForLogging, e);
      return -1;
    }
    long millisSinceCertify = new Date().getTime() - lastCertifiedDate.getTime();
    int daysSinceCertify = (int)TimeUnit.DAYS.convert(millisSinceCertify, TimeUnit.MILLISECONDS);
    int daysLeft = daysUntilRecertify - daysSinceCertify;
    if (daysLeft < 0) {
      daysLeft = 0;
    }
    return daysLeft;
  }
}
