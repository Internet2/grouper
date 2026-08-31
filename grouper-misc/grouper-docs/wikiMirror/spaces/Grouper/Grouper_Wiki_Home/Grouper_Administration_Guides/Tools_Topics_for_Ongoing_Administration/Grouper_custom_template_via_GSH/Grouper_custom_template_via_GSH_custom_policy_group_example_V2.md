---
title: "Grouper custom template via GSH custom policy group example V2"
space: Grouper
pageId: 28549736
version: 5
lastUpdated: 2026-01-15T17:22:10.374Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549736/Grouper+custom+template+via+GSH+custom+policy+group+example+V2
---

This custom policy group example creates an access policy in a different manner from the built-in template. V2 does not allow the intermediate folder to be specified and must be called from a folder where a child ref folder exists:

- Intermediate groups are placed in a reference folder and will throw an error if it doesn't exist.
- Enforces an "Access", "Allow", and "Deny" suffixes on the policy groups.
- Enforces intermediate groups go into a 'ref' folder
- Adds a central 'globalDeny' group to the policy

### Inputs

| Name | Type | Validation | Default | Required | Jexl for showEl | Description |
| --- | --- | --- | --- | --- | --- | --- |
| gsh_input_apName | String | Alphanumberics, underscores, and dashes |  | required |  | The name of the access policy should be short, concise, and unique. It should convey the product where the access is being granted and what level of access is being given. e.g. spartAppUsers, cloudAdminRole, etc. |
| gsh_input_apDesc | String | None |  | required |  | The description of the access policy should clearly convey to most readers the level of access being granted. |

### Config Screen

### Configuration

```
grouperGshTemplate.addUNCGAccessPolicyV2.displayErrorOutput = true
grouperGshTemplate.addUNCGAccessPolicyV2.folderShowOnDescendants = descendants
grouperGshTemplate.addUNCGAccessPolicyV2.folderShowType = certainFolders
grouperGshTemplate.addUNCGAccessPolicyV2.folderUuidToShow = uncg\u003Aapps, uncg\u003Ausers\u003Ajfwillia
grouperGshTemplate.addUNCGAccessPolicyV2.gshTemplate = import java.util.*;\n\
import edu.internet2.middleware.grouper.cfg.*;\n\
import edu.internet2.middleware.grouper.misc.*;\n\
...
grouperGshTemplate.addUNCGAccessPolicyV2.input.0.formElementType = text
grouperGshTemplate.addUNCGAccessPolicyV2.input.0.label = Access Policy Name
grouperGshTemplate.addUNCGAccessPolicyV2.input.0.maxLength = 63
grouperGshTemplate.addUNCGAccessPolicyV2.input.0.name = gsh_input_apName
grouperGshTemplate.addUNCGAccessPolicyV2.input.0.required = true
grouperGshTemplate.addUNCGAccessPolicyV2.input.0.type = string
grouperGshTemplate.addUNCGAccessPolicyV2.input.0.validationBuiltin = alphaNumericUnderscoreDash
grouperGshTemplate.addUNCGAccessPolicyV2.input.0.validationType = builtin
grouperGshTemplate.addUNCGAccessPolicyV2.input.1.description = The description of the access policy should clearly convey to most readers the level of access being granted.
grouperGshTemplate.addUNCGAccessPolicyV2.input.1.label = Description
grouperGshTemplate.addUNCGAccessPolicyV2.input.1.name = gsh_input_apDesc
grouperGshTemplate.addUNCGAccessPolicyV2.input.1.required = true
grouperGshTemplate.addUNCGAccessPolicyV2.input.1.type = string
grouperGshTemplate.addUNCGAccessPolicyV2.input.1.validationType = none
grouperGshTemplate.addUNCGAccessPolicyV2.input.2.defaultValue = False
grouperGshTemplate.addUNCGAccessPolicyV2.input.2.description = Advanced options to customize where the allow, deny groups are built. Default is a sibling ref folder(required).  Subject to the user's access privileges.
grouperGshTemplate.addUNCGAccessPolicyV2.input.2.label = Advanced Options
grouperGshTemplate.addUNCGAccessPolicyV2.input.2.name = gsh_input_apAdvOp
grouperGshTemplate.addUNCGAccessPolicyV2.input.2.type = boolean
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.description = Folder ID Path for where the allow, deny groups will be created.
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.formElementType = text
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.label = Intermediate Group Folder
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.name = gsh_input_apIntGrpFldr
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.required = true
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.showEl = \u0024{gsh_input_apAdvOp == "True"}
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.type = string
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.validationRegex = [a-zA-z\u003A_\u005C-0-9]+
grouperGshTemplate.addUNCGAccessPolicyV2.input.3.validationType = regex
grouperGshTemplate.addUNCGAccessPolicyV2.moreActionsLabel = Add UNCG Access Policy
grouperGshTemplate.addUNCGAccessPolicyV2.numberOfInputs = 4
grouperGshTemplate.addUNCGAccessPolicyV2.requireFolderPrivilege = create
grouperGshTemplate.addUNCGAccessPolicyV2.runAsType = currentUser
grouperGshTemplate.addUNCGAccessPolicyV2.securityRunType = privilegeOnObject
grouperGshTemplate.addUNCGAccessPolicyV2.showInMoreActions = true
grouperGshTemplate.addUNCGAccessPolicyV2.showOnFolders = true
grouperGshTemplate.addUNCGAccessPolicyV2.templateDescription = This template will create the allow, deny, and access groups for an access policy.  globalDeny will automatically be added to deny groups.
grouperGshTemplate.addUNCGAccessPolicyV2.templateName = Add UNCG Access Policy
grouperGshTemplate.addUNCGAccessPolicyV2.templateVersion = V1
```

### GSH template script

```
import java.util.*;
import edu.internet2.middleware.grouper.cfg.*;
import edu.internet2.middleware.grouper.misc.*;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouper.app.attestation.*;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.attr.finder.*;
import edu.internet2.middleware.grouper.attr.value.*;
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.subject.*;
import org.apache.commons.lang3.*;

// default variables
// For GroupFinder, StemFinder
//gs = GrouperSession.startRootSession();
// Default descriptions for allow, deny groups
String apAllowGrpDesc = "Intermediate allow group for the access policy "+gsh_input_apName;
String apDenyGrpDesc = "Intermediate deny group for the access policy "+gsh_input_apName;

// Validate inputs first

// If a group in this folder already has roleName, throw an error
if(GroupFinder.findByName(gsh_builtin_grouperSession, gsh_builtin_ownerStemName+":"+gsh_input_apName, false)!=null){
     gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_apName", "Error: access policy exists");
}

//Setup the ref folder name
apIntGrpFldrNm = gsh_builtin_ownerStemName+":ref";

Stem apIntGrpFldr = null

try {
     //Find intermediate folder
     apIntGrpFldr = StemFinder.findByName(gsh_builtin_grouperSession, apIntGrpFldrNm, false);
     gsh_builtin_gshTemplateOutput.addOutputLine("Reference folder found: " + apIntGrpFldr.getName());
} catch (Exception missingRefFolder){
     gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_apIntGrpFldr", "Error: No valid reference folder found");
}

// Do not proceed if there is an error
if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
  gsh_builtin_gshTemplateOutput.assignIsError(true);
  GrouperUtil.gshReturn();
}

// Now, do the work

//Find globalDeny Group
Group globalDenyGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "uncg:reference:role:security:globalDeny", false);
gsh_builtin_gshTemplateOutput.addOutputLine("GlobalDeny group found: " + globalDenyGroup.getName());

//setup gdgTypeGroupSave to assign types to the groups
GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();

//Create access policy group
Group apGroup = new GroupSave().assignName(gsh_builtin_ownerStemName + ":" + gsh_input_apName + "Access").assignDescription(gsh_input_apDesc).save();
gdgTypeGroupSave.assignGroup(apGroup).assignType("policy").save();
gsh_builtin_gshTemplateOutput.addOutputLine("Access policy group created: " + apGroup.getName());

//Create intermediate allow group
Group apAllowGroup = new GroupSave().assignName(apIntGrpFldr.getName() + ":" + gsh_input_apName + "Allow").assignDescription(apAllowGrpDesc).save();
gdgTypeGroupSave.assignGroup(apAllowGroup).assignType("intermediate").save();
gsh_builtin_gshTemplateOutput.addOutputLine("Allow group created: " + apAllowGroup.getName());

//Create intermediate deny group
Group apDenyGroup = new GroupSave().assignName(apIntGrpFldr.getName() + ":" + gsh_input_apName+"Deny").assignDescription(apDenyGrpDesc).save();
gdgTypeGroupSave.assignGroup(apDenyGroup).assignType("intermediate").save();
gsh_builtin_gshTemplateOutput.addOutputLine("Deny group created: " + apDenyGroup.getName());
//Add globalDeny as a member of the deny group
new MembershipSave().assignGroupName(apDenyGroup.getName()).assignSubject(globalDenyGroup.toSubject()).save();
gsh_builtin_gshTemplateOutput.addOutputLine("GlobalDeny added as a member to the Deny group");

//Create the composite
Composite apComposite = new CompositeSave().assignOwnerName(apGroup.getName()).assignLeftFactorName(apAllowGroup.getName()).assignRightFactorName(apDenyGroup.getName()).assignType("complement").save();
gsh_builtin_gshTemplateOutput.addOutputLine("Access policy now defined as the membership in the allow group " + apAllowGroup.getName() + " minus " + apDenyGroup.getName()+"\n\n\n");
     
//Wrap-up messages
gsh_builtin_gshTemplateOutput.addOutputLine("Access policy " + apGroup.getName() + " has been successfully created. \n\n Contact 6-Tech for support." );

//Work completed, add finishing output line
gsh_builtin_gshTemplateOutput.addOutputLine("Finished running access policy template for role: " + gsh_input_apName);
```

### Template UI

Template results:
