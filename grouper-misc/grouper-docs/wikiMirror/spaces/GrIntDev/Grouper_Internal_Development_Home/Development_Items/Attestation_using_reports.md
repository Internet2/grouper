---
title: "Attestation using reports"
space: GrIntDev
pageId: 48792824
version: 8
lastUpdated: 2026-07-12T06:45:44.818Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792824/Attestation+using+reports
---

Fixed in patch: grouper_v2_4_0_ui_patch_41

## Folder attestation using reports

- On the folder [attestation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545015/Grouper+attestation) page, under the "Has attestation" option, include an option called "Attestation type". The options are to attest individual groups or attest a report.
  
  - etc:attribute:attestation:attestationType - group or report
- If attestation type of report is selected, the "Folder scope" option isn't shown. Instead there's a new option to select a report that's configured on the same folder. This assumes the authenticated user has access to that report.
  
  - etc:attribute:attestation:attestationReportConfigurationId - some identifier of the report?
- If attestation type of report is selected, the user is required to enter the name of a group who is allowed to attest the report.
  
  - etc:attribute:attestation:attestationAuthorizedGroupId
- If attestation type of report is selected, you can either email a list of email addresses or based on the group above. I think the current attributes can be used (attestationSendEmail and attestationEmailAddresses). Though the UI would need to be different. e.g. "Yes, email the authorized group members" rather than "Yes, email the group admins and updaters".
- If attestation type of report is selected, "Mark groups as reviewed" is replaced with "Mark report as reviewed".
- The following attributes would also now be applicable to folders
  
  - etc:attribute:attestation:attestationCalculatedDaysLeft
  - etc:attribute:attestation:attestationDateCertified
- Adjust all the code so that the type is taken into account. e.g. stemAttestationProcessHelper shouldn't propagate attestation attributes if the type is report.
- Update daemon that sends out emails to send a different version of the email if the type is report. Include a link to the report in the email?
- Update folder UI to indicate if attestation on the report is needed. Add link to report there.
- Groups and sub-folders can still have separate attestation configs.

## Group attestation using reports (not implemented yet)

- On the group attestation page, under the "Has attestation" option, include an option called "Attestation type". The options are to attest the group or attest a report.
  
  - etc:attribute:attestation:attestationType - group or report
- If attestation type of report is selected, allow user to select a report that's configured on that group.
  
  - etc:attribute:attestation:attestationReportConfigurationId - some identifier of the report?
- If type is report, "Mark this group as reviewed" is replaced with "Mark report as reviewed".

S**ee Also**

[Grouper Attestation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545015/Grouper+attestation)
