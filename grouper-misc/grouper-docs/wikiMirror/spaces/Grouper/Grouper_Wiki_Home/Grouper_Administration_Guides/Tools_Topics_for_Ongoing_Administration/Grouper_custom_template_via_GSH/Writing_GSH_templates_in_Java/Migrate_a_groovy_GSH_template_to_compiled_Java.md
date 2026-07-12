---
title: "Migrate a groovy GSH template to compiled Java"
space: Grouper
pageId: 28555804
version: 5
lastUpdated: 2026-07-01T05:37:26.694Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555804/Migrate+a+groovy+GSH+template+to+compiled+Java
---

**ABAC templates migrate identically** — same `GshTemplateV2` base and `gshRunLogic` method; just keep `templateType=abac` (and, as before, do not print to the screen or validate).

## Today vs compiled

A gsh template is a `grouperGshTemplate` config. Today it has `templateMode=interpreted` and a Groovy body. To convert, set `templateMode=compiled` on the same config and rewrite the body as a Java class extending `GshTemplateV2` implementing `gshRunLogic`. Inputs (`gsh_input_*`), owner stem/group, and the output object are unchanged.

## Before (interpreted V2 Groovy)

// imports prepended by the runtime; one-line statements; no chaining class CreateGroup extends GshTemplateV2 { public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) { GshTemplateOutput output = gshTemplateV2output.getGsh_builtin_gshTemplateOutput(); GrouperSession grouperSession = gshTemplateV2input.getGsh_builtin_grouperSession(); String stemName = gshTemplateV2input.getGsh_builtin_ownerStemName(); String extension = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_groupExtension"); GroupSave groupSave = new GroupSave(grouperSession); groupSave.assignName(stemName + ":" + extension); groupSave.assignCreateParentStemsIfNotExist(true); groupSave.save(); output.addOutputLine("Created group " + stemName + ":" + extension); } } 

## After (compiled Java)

package edu.institution.grouper.gsh; import edu.internet2.middleware.grouper.GrouperSession; import edu.internet2.middleware.grouper.GroupSave; import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2; import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input; import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output; import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput; public class CreateGroupTemplate extends GshTemplateV2 { @Override public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) { GshTemplateOutput output = gshTemplateV2output.getGsh_builtin_gshTemplateOutput(); GrouperSession grouperSession = gshTemplateV2input.getGsh_builtin_grouperSession(); String stemName = gshTemplateV2input.getGsh_builtin_ownerStemName(); String extension = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_groupExtension"); new GroupSave(grouperSession) .assignName(stemName + ":" + extension) .assignCreateParentStemsIfNotExist(true) .save(); output.addOutputLine("Created group " + stemName + ":" + extension); } }
