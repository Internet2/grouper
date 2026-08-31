---
title: "Migrate a custom UI GSH script to compiled Java"
space: Grouper
pageId: 28555824
version: 2
lastUpdated: 2026-07-01T05:37:23.640Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555824/Migrate+a+custom+UI+GSH+script+to+compiled+Java
---

## Today vs compiled

A custom UI runs a Groovy body on the join/leave button today — configured as a text entry of type `gshScript` in the custom UI config. The script receives `group`, `subject`, `subjectLoggedIn`, and `grouperSession`, and the same script runs for both join and leave (distinguished by button-state flags).

To convert: create a compiled GSH template (`templateType=customUi`, `templateMode=compiled`) whose body extends `GrouperTemplateCustomUi` and overrides `runOnJoin` and/or `runOnLeave`. Then, in the custom UI config, add a text entry of type `gshTemplateConfigId` whose value is that template's config id. When set, it runs instead of the `gshScript` entry, and the framework calls the right method per action (no more button-flag branching). The context (group, subject, logged-in subject, container) arrives on the `CustomUiTemplateInput`.

## Before (interpreted gshScript text entry)

// one script for both actions; group, subject, subjectLoggedIn, grouperSession are prepended if (cu_joinGroupButtonPressed) { group.addMember(subject, false); } else if (cu_leaveGroupButtonPressed) { group.deleteMember(subject, false); } 

## After (compiled GrouperTemplateCustomUi)

package edu.institution.grouper.gsh; import edu.internet2.middleware.grouper.grouperUi.beans.ui.CustomUiTemplateInput; import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperTemplateCustomUi; public class SelfServiceJoinTemplate extends GrouperTemplateCustomUi { @Override public void runOnJoin(CustomUiTemplateInput customUiTemplateInput) { customUiTemplateInput.getGroup().addMember(customUiTemplateInput.getSubject(), false); } @Override public void runOnLeave(CustomUiTemplateInput customUiTemplateInput) { customUiTemplateInput.getGroup().deleteMember(customUiTemplateInput.getSubject(), false); } }
