---
title: "Grouper Training - Administration - Lesson: GSH templates"
space: Grouper
pageId: 28544443
version: 20
lastUpdated: 2025-04-09T00:33:00.876Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544443/Grouper+Training+-+Administration+-+Lesson+GSH+templates
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Create control group for allowed users

Create folder: `test:testTemplates`

Create group in **test:testTemplates**: `customAppUsers`

Add kjenkins to the test:testTemplates:customAppUsers group

Grant customAppUsers view on the testTemplates folder

## Create template

As banderson: Miscellaneous → GSH templates → Add GSH template

- Config ID: customApp
- Template: GshTemplateConfiguration
- Template type: GSH
- Template version: V2
- Show on folders: True
- Folder show type: Certain folders
- Folder uuids or names to show: test:testTemplates, app
- Folder show on descendants: Certain folders
- Run template owner type: folder
- Run template folder name: test:testTemplates
- Security run type: Specified group
- Group uuid or name: `test:testTemplates:customAppUsers`
- Run as type: GrouperSystem
- Template name: Custom App template
- Template description: Creates app in institution specific way
- Show in actions dropdown: true
- Template label in actions dropdown: Create custom app from template
- GSH script: Use `//` for now, and replace once we save it
- Number of inputs: 2
- Input 1: gsh_input_appName
- Input 1 label: App name
- Input 1 description: App name is alphanumeric app folder display extension
- Input 1 validation type: Built-in
- Input 1 builtin validation: Alpha-numeric and underscores
- Input 2: gsh_input_subjectIds
- Input 2 label: Subject IDs
- Input 2 description: Comma separated net ID's, e.g. kjenkins, banderson
- Input 2 form element type: Textarea
- Input 2 validation type: Regex
- Input 2 validation regex: ^[a-z0-9, ]+$
- Input 2 validation message: Comma separated lower case alphanumeric net IDs only

## Replace script

Edit the template. Copy the code below and paste into the GSH script field

```groovy
/**
 * Example V2 template that takes a list of users and an app name, and performs:
 *  - validate users all resolve to subjects
 *  - create app stem
 *  - create admin group and add users
 *  - admin group has inherited ADMIN on app folder
 *  - add attestation for the admin group
 */

import edu.internet2.middleware.grouper.GroupSave
import edu.internet2.middleware.grouper.GrouperSession
import edu.internet2.middleware.grouper.PrivilegeGroupInheritanceSave
import edu.internet2.middleware.grouper.Stem
import edu.internet2.middleware.grouper.StemFinder
import edu.internet2.middleware.grouper.StemSave
import edu.internet2.middleware.grouper.SubjectFinder
import edu.internet2.middleware.grouper.app.attestation.AttestationGroupSave
import edu.internet2.middleware.grouper.app.attestation.AttestationType
import edu.internet2.middleware.grouper.app.gsh.template.*
import edu.internet2.middleware.grouper.privs.AccessPrivilege
import edu.internet2.middleware.grouper.util.GrouperUtil
import edu.internet2.middleware.subject.Subject
import edu.internet2.middleware.grouper.Group

public class GTEDemoTemplateV2 extends GshTemplateV2 {

    /**
     *
     * @param input holder for input strings [gsh_input_appName, gsh_input_subjectIds]
     * @param output
     */
    @Override
    public void gshRunLogic(GshTemplateV2input input, GshTemplateV2output output) {
        GrouperSession grouperSession  = input.getGsh_builtin_grouperSession()
        GshTemplateOutput out = output.getGsh_builtin_gshTemplateOutput()

        String ownerStemName = input.getGsh_builtin_ownerStemName()
        Stem ownerStem = StemFinder.findByName(grouperSession, ownerStemName, true)

        // 1. app should not already exist
        String appName = input.getGsh_builtin_inputString("gsh_input_appName")

        String appPath = "${ownerStem.name}:${appName}"
        Stem appStem = StemFinder.findByName(grouperSession, appPath, false)
        if (appStem != null) {
            out.addValidationLine("gsh_input_appName", "Error: App already exists '${appPath}'!")
        }

        // 2. resolve subjects, they need to resolve
        String subjectIds = input.getGsh_builtin_inputString("gsh_input_subjectIds")

        List<String> subjectIdList = GrouperUtil.splitTrimToList(subjectIds, ",")
        List<Subject> subjects = []
        subjectIdList.each { subjectId ->
            Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectId, "eduLDAP", false)
            if (subject == null) {
                out.addValidationLine("gsh_input_subjectIds", "Error: SubjectId cannot be found '${subjectId}'!")
            } else {
                subjects << subject
            }
        }

        // 3. Do not proceed if there is an error
        if (GrouperUtil.length(out.getValidationLines()) > 0) {
            out.assignIsError(true)
            return
        }

        // 4. Create stem
        appStem = new StemSave().assignName(appPath).save()
        out.addOutputLine("App folder created: ${appPath}")

        // 5. Create admin group
        Group adminGroup = new GroupSave().assignName("${appPath}:Admins").save()
        out.addOutputLine("Admin group created: " + adminGroup.name)

        // 6. Add users
        subjects.each { subject ->
            adminGroup.addMember(subject, false)
            out.addOutputLine("Added admin: ${subject.description}")
        }

        // 7. add inherited privileges
        PrivilegeGroupInheritanceSave privilegeGroupInheritanceSave = new PrivilegeGroupInheritanceSave().
                assignStem(appStem).
                assignStemScope(Stem.Scope.SUB)
        privilegeGroupInheritanceSave.assignSubject(adminGroup.toSubject()).addPrivilege(AccessPrivilege.ADMIN).save()
        out.addOutputLine("Assigned inherited group ADMIN priv on: ${appStem.name} to group: ${adminGroup.name}")

        // 8. add attestation
        AttestationGroupSave attestationGroupSave = new AttestationGroupSave().assignGroup(adminGroup)
        attestationGroupSave.assignAttestationType(AttestationType.group).
                assignDaysUntilRecertify(30).
                assignMarkAsAttested(true).
                assignSendEmail(true).
                save()
        out.addOutputLine("Configured attestation on group: ${adminGroup.name}")

        // 9. success message
        out.addOutputLine("Finished running institution app template")
    }
}
```

## Run template

As kjenkins, navigate to test:testTemplates, choose "Create custom app from template"

- App name: demoApp
- Subject IDs: banderson, lmiller

## View audits

As banderson, view audits for demoAppAdmins
