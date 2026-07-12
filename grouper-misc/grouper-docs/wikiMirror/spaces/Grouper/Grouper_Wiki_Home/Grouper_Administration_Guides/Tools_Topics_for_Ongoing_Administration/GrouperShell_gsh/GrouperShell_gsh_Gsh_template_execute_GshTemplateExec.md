---
title: "GrouperShell (gsh) Gsh template execute (GshTemplateExec)"
space: Grouper
pageId: 28547861
version: 4
lastUpdated: 2026-07-01T05:46:00.842Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547861/GrouperShell+gsh+Gsh+template+execute+GshTemplateExec
---

Use this class to execute a custom gsh template

Sample call

> GshTemplateExec exec = new GshTemplateExec(); exec.assignConfigId("testGshTemplateConfig"); exec.assignCurrentUser(subject); exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem); exec.assignOwnerStemName(ownerStem.getName()); GshTemplateInput input = new GshTemplateInput(); input.assignName("gsh_input_myExtension"); input.assignValueString("zoomTest"); exec.addGshTemplateInput(input); GshTemplateExecOutput output = exec.execute();  
>  if (output.getGshTemplateOutput().isError()) {  
>  // handle this... e.g. from another template: gsh_builtin_gshTemplateOutput.addOutputLine("Error running sub-template");  
>  }  
>  if (GrouperUtil.length(output.getGshTemplateOutput().getValidationLines()) > 0) {  
>  for (GshValidationLine gshValidationLine : output.getGshTemplateOutput().getValidationLines()) {  
>  // handle this... e.g. from another template   
>  // gsh_builtin_gshTemplateOutput.addOutputLine((String)(gshValidationLine.getInputName() + ": " + gshValidationLine.getText()));  
>  }  
>  }

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/gsh/template/GshTemplateExec.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/gsh/template/GshTemplateExec.html)
