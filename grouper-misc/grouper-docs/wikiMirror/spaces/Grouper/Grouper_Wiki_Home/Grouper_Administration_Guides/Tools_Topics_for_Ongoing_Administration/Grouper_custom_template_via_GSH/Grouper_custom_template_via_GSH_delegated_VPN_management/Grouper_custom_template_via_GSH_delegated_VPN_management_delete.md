---
title: "Grouper custom template via GSH delegated VPN management - delete"
space: Grouper
pageId: 28554486
version: 3
lastUpdated: 2026-07-01T05:40:20.744Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554486/Grouper+custom+template+via+GSH+delegated+VPN+management+-+delete
---

This GSH template allows networking staff to delete a VPN instance. Note there is no drop down, you need to type in the VPN correctly, to prevent accidental deletion

## GSH script

////uncomment to compile in eclipse (and last line)  
//// these are standard imports, can be commented out in script but needed in eclipse  
//import edu.internet2.middleware.grouper.*;  
//import edu.internet2.middleware.grouper.misc.*;  
//import edu.internet2.middleware.grouper.attr.*;  
//import edu.internet2.middleware.grouper.attr.assign.*;  
//import edu.internet2.middleware.grouper.attr.finder.*;  
//import edu.internet2.middleware.grouper.util.*;  
//import java.util.*;  
//import edu.internet2.middleware.grouper.app.gsh.template.*;

import edu.internet2.middleware.grouper.app.reports.*;

//public class Test4 {  
//  
// public static void main(String[] args) {  
//   
// GrouperStartup.startup();  
//   
// //input of project name, alphanumeric, start with lower.  
// String gsh_input_projectSystemName = "darStaff";  
//  
// GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();  
// GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();  
   
 String projectStemName = "penn:isc:ts:networking:service:sraVpn:service:" + gsh_input_projectSystemName;

Stem projectFolder = StemFinder.findByName(gsh_builtin_grouperSession, projectStemName , false);  
 if (projectFolder == null) {  
 gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_projectSystemName",  
 "Error: project name cannot be found '" + projectStemName + "', should be a system extension (ID) of a folder in the VPN service folder!");  
 gsh_builtin_gshTemplateOutput.assignIsError(true);  
 GrouperUtil.gshReturn();  
 }

// 0. remove the report  
 AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("penn:etc:reportConfig:reportConfigMarker", true);  
 Set<AttributeAssign> attributeAssigns = new AttributeAssignFinder().addOwnerStemId(projectFolder.getId()).addAttributeDefNameId(attributeDefName.getId()).findAttributeAssigns();  
   
 for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {  
 GrouperReportConfigurationBean reportConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssign.getId());  
 try {GrouperReportConfigService.deleteGrouperReportConfig(projectFolder, reportConfigBean);} catch (org.quartz.SchedulerException se) {throw new RuntimeException("error", se);}  
 gsh_builtin_gshTemplateOutput.addOutputLine("Removed report: " + attributeAssign.getId());  
 }  
   
 // 1. Obliterate project folder  
 STEM_DELETE: {  
 StemSave projectFolderSave = new StemSave().assignName(projectStemName).assignSaveMode(SaveMode.DELETE);  
 projectFolderSave.save();  
 gsh_builtin_gshTemplateOutput.addOutputLine("Project folder deleted: " + projectStemName);  
 }

String ownersGroupName = projectStemName + ":" + gsh_input_projectSystemName + "SraVpnOwners";  
 String ownersAttestationEmailAddress = ownersGroupName + "@grouper";

// 2. Remove from grouper.properties that the super admins group can receive email  
 EMAIL_DELETE: {  
 GrouperEmail.removeAllowEmailToGroup(ownersAttestationEmailAddress );  
 gsh_builtin_gshTemplateOutput.addOutputLine("Removed the configuration to allow emailing the " + gsh_input_projectSystemName + " Owners");  
 }

// done!  
 gsh_builtin_gshTemplateOutput.addOutputLine("Finished removing VPN project: " + gsh_input_projectSystemName);  
// }  
//  
//}

## Configuration
