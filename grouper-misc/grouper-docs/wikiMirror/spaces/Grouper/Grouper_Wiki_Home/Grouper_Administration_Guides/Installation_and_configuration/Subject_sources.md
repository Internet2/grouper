---
title: "Subject sources"
space: Grouper
pageId: 28544786
version: 42
lastUpdated: 2026-07-01T05:48:02.020Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544786/Subject+sources
---

> In Grouper `v7` and `v8`, all deployments must migrate to **entity data field subject sources**. In `v9` and `v10`, the non-entity-data-field subject sources are removed. See the sub-pages below ("Migrating to data fields subject source" and "Subject source using data fields") to plan the migration.

**Sub-pages**

**Table of contents**

> In Grouper `v2.3+` you should use [subject.properties, not source.xml](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555326/Grouper+sources.xml+conversion+to+subject.properties).

The Subject API is used to integrate a java application with a site's existing Identity Management operations (see architectural diagram). It enables any type of object whose identity is being managed - person, group, application, computer, etc. - to be presented to that application without requiring the application to be specifically designed for particular object types or with knowledge of how those objects are stored and represented. Those details form the configuration of the Subject API.

Figure 1 (below) illustrates the general role of the Subject API in the interaction between an application and a site's Identity Management infrastructure. There are two parts to the Subject API: the Source interface and the Subject interface. An application uses the Source interface to search for and select Subjects from back-end stores, which are presented as abstracted, flat Subject objects via the Subject interface.

> 

## Debugging

Run the Subject API diagnostics from GSH. Also use the Subject API diagnostics in "misc" in the UI (if Grouper starts... if there is a subject API problem it is severe for Grouper).

> Running the Subject API diagnostics requires a Grouper administrator. In the UI, the diagnostics are shown only to members of the **wheel** or **GrouperSystem** (root) group; in GSH they run under a root session. The "member change subject" operation described below likewise requires a root session (GrouperSystem, or a member of the wheel/sysadmin group).

```
GrouperSession.startRootSession();
new edu.internet2.middleware.grouper.grouperUi.serviceLogic.SubjectSourceDiagnostics().assignSourceId("SMUPerson_DEV").assignSubjectId("empl1").assignSubjectIdentifier("netid@school.edu").assignSearchString("em").subjectSourceDiagnosticsFromGsh()
===> 
SUCCESS: Found subject by id in 37ms: 'empl1'
         with SubjectFinder.findByIdAndSource("empl1", "SMUPerson_DEV", false)
SUCCESS: Subject id in returned subject matches the subject id searched for: 'empl1'
WARNING: No subject found by identifier in 14ms: 'netid@school.edu'
         with SubjectFinder.findByIdentifierAndSource("netid@school.edu", "SMUPerson_DEV", false)

```

Note: to debug your Subject API configuration, set this in the log4j2 configuration (`log4j2.xml`), inside the `<Loggers>` element.

```
<Logger name="edu.internet2.middleware.subject.provider" level="debug" />
<Logger name="org.ldaptive" level="debug" />
```

The LDAP subject source uses Ldaptive, so logging its activity is done via the `org.ldaptive` logger (older docs reference the retired `edu.vt.middleware.ldap` package).

If you are using a JDBC source, you can use the p6spy SQL driver: set `spy.properties` to specify the underlying driver and the log file name.

## Number of sources

Decide how many sources you need. It should be the minimal number that you can do. For people, it should be one. If you dont have one single source, consider working on that initiative. Having multiple subjects in Grouper that represent the same person will lead to problems (e.g. seeing what someone has access to). You might end up with a source for people and a source for service principals.

## Choosing identifiers for subjects

Identifiers and their management can get complicated. They can be revoked or not, re-assigned or not, lucent or opaque, etc. Depending on such characteristics, a given identifier might be a good or bad choice to use in the context of managing the identified subject's group memberships.

For example, a username is often lucent - easily remembered by the person to whom it is associated. But it may also be revokable, meaning that it no longer refers to that person (perhaps they have a new one), or even re-assignable, meaning that it might refer to some other person at a later time. If a username is used to record membership, username changes must trigger corresponding membership changes. A username is better suited to authentication than it is to indicating membership.

On the other hand, an opaque registryID (machine, not human, readable) that never changes is great for membership, but lousy for authentication - it might not even be known by the person to whom it is associated. How would I identify myself to Grouper if I wished to opt-in to a list or manage a group?

Grouper accommodates subject identifier issues in two ways. First, it maintains UUIDs for every subject and group within the Groups Registry. These are never exposed by the UI, but are associated with externally supplied subject identifiers within the Groups Registry (in the grouper_members table). This approach allows the identifier associated with a given subject to be changed without any need to change actual memberships.

Second, by relying on the Subject API, Grouper is able to lookup subjects that are presented with an identifier in one namespace and obtain identifiers in other namespaces for that subject. That means that it can translate a username into a registryID, for example. So, when a user authenticates to an application using the Grouper API, that application can use the Subject API to fetch an identifier for the person chosen by the site for use in memberships. Similarly, when a membership in the Groups Registry is to be expressed elsewhere, the identifier used for group members can be translated by a provisioning connector by use of the Subject API into one that is suitable in the provisioned context.

Subject ID: should be unchangeable, unrevokable. Usually this an opaque id (number or uuid etc). The source that a subject is associated with also should not change.

Subject Identifier: anything that can refer to a subject uniquely. Usually these are netIds, eppns, etc.

It would be nice if subject id's and identifiers are unique across sources, though this is not required.

You should not have the same subject in more than one source.

Subjects should be resolvable for as long as you want users to be able to search for them or view them on the UI. It is possible for subjects to not be active in which case they are not searchable, but still be resolvable so they can be shown in the UI in auditing.

## Examples

- Penn JDBC2 example

## Search and selection methods

The Source interface provides three principal methods of searching for and selecting Subjects. These methods are used in the Grouper API, and are exposed in the UI and WS.

| Method | Description |
| --- | --- |
| **getSubject** | Retrieve a specific subject from a specific source by its SubjectId. |
| **getSubjectByIdentifier** | Retrieve a specific subject by unique match against one or more configured *identifying attributes*. |
| **search** | List all subjects meeting a given search criterion. |

Deployers supply back-end specific search & selection statements for each of these three methods that determine 1) when a Subject matches each search criterion and 2) which of its attributes will be presented to the calling application. Callers need only persist a reference to the sourceId and subjectId of Subjects to be able to fully instantiate them at any time. Various methods in the Subject interface provide access to these identifiers and other attributes of each Subject.

The getSubject() method is used by the application to instantiate a Subject object from its persisted subject reference data (subjectId and sourceId). For example, the Grouper UI uses getSubject() to display the name each member of a group.

The getSubjectByIdentifier() method is used to enable the application to locate a unique subject by reference to any of its identifying attributes. For example, consider a site that manages both netIds and registryIds for its users, and suppose they choose to use registryId as their subjectId. When a user logs in with their netId, the application uses getSubjectByIdentifier() to locate and instantiate a Subject object for the user from the user's netId.

The search() method is used by a User Interface application to allow a human to search for and list subjects using familiar attributes like name parts, departments, etc. For example, to grant a person a privilege, an application's UI first does a search() using the user's specified search term, displays a list of the names and descriptions of the matching subjects, and enables the UI user to select one.

There are attributes that need to be configured for a subject in addition to subjectId:

- name: This is generally the first and last name for a subject. If this is private data and you dont want to list it, you can use a netId or something to help differentiate the subject from other subjects. Worst case, subjectId
- description: This should be something that is standalone to show information about the subject when a list is displayed to help the user select the correct subject. This is the description attribute at Penn
  
  
  ```
  Chris Hyzer (mchyzer, 10021368) (active) Staff - Isc-applications & Information Services - Application Architect (also: Alumni)
  ```

## Changing IDs

The subject ID and source ID tuple uniquely identifies a subject in Grouper.

If you are changing a subject ID in Grouper, if the old subject_id (and subject_source) is in the grouper_members table, and the new subject_id and subject_source, is not in the members table, you can just change the subject_id value for that row to be the new ID. More commonly, the subject ID changes in the subject source, and a loader job will pull in the new ID, so you end up with both rows in the grouper_members table. In this case the old subject ID (which hopefully is not an unresolvable subject) needs to be merged into the new subject ID.

You need to perform the "[member change subject](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh)" operation in GSH or the web service

You can also make a GSH template to help your IAM staff perform this action.

Here is an example of such a GSH template

```
grouperGshTemplate.changePennid.defaultRunButtonFolderUuidOrName = penn\u003Aetc\u003Atemplates\u003AchangePennid
grouperGshTemplate.changePennid.displayErrorOutput = true
grouperGshTemplate.changePennid.folderShowOnDescendants = certainFoldersAndDescendants
grouperGshTemplate.changePennid.folderShowType = certainFolders
grouperGshTemplate.changePennid.folderUuidToShow = penn\u003Aetc\u003Atemplates\u003AchangePennid
grouperGshTemplate.changePennid.gshTemplate = import org.apache.commons.lang3.StringUtils;\n\
\n\
import edu.internet2.middleware.grouper.GrouperSession;\n\
import edu.internet2.middleware.grouper.Member;\n\
import edu.internet2.middleware.grouper.MemberFinder;\n\
import edu.internet2.middleware.grouper.SubjectFinder;\n\
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;\n\
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;\n\
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n\
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n\
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n\
import edu.internet2.middleware.grouper.exception.GrouperSessionException;\n\
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;\n\
import edu.internet2.middleware.grouper.misc.GrouperStartup;\n\
import edu.internet2.middleware.grouper.util.GrouperUtil;\n\
import edu.internet2.middleware.subject.Subject;\n\
\n\
public class Test102changePennId extends GshTemplateV2 {\n\
\n\
\n\
  @Override\n\
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,\n\
      GshTemplateV2output gshTemplateV2output) {\n\
    \n\
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();\n\
\n\
    String configId = "changePennid";\n\
    \n\
    String oldPennId = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_oldPennId");\n\
    String newPennId = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_newPennId");\n\
    boolean deleteOld = GrouperUtil.booleanValue(gshTemplateV2input.getGsh_builtin_inputBoolean("gsh_input_deleteOld"), false);\n\
\n\
    Subject oldSubject = SubjectFinder.findByIdAndSource(oldPennId, "pennperson", false);\n\
    if (oldSubject == null) {\n\
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addValidationLine("gsh_input_oldPennId", "Old PennId not found!");\n\
      return;\n\
    }\n\
    Subject newSubject = SubjectFinder.findByIdAndSource(newPennId, "pennperson", false);\n\
    if (newSubject == null) {\n\
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addValidationLine("gsh_input_newPennId", "New PennId not found!");\n\
      return;\n\
    }\n\
    \n\
    Member oldMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), oldSubject, false);\n\
    if (oldMember == null) {\n\
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Old member is not found, there is nothing to do!");\n\
      return;\n\
    }\n\
\n\
    String result = oldMember.changeSubjectReport(newSubject, deleteOld);\n\
    result = GrouperUtil.escapeHtml(result, true);\n\
    result = StringUtils.replace(result, "\u005Cn", "<br />");\n\
\n\
    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Success\u003A the PennId was changed");\n\
    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine(result);\n\
  }\n\
\n\
\n\
  \n\
  public static void main(String[] args) {\n\
\n\
    GrouperStartup.startup();\n\
    \n\
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {\n\
      \n\
      @Override\n\
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {\n\
        \n\
        Subject subject = SubjectFinder.findByIdAndSource("10021368", "pennperson", true);\n\
\n\
        Test102changePennId test102changePennId = new Test102changePennId();\n\
        GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();\n\
        gshTemplateV2input.setGsh_builtin_subject(subject);\n\
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();\n\
        gshTemplateRuntime.setTemplateConfigId("changePennid");\n\
        //TODO add inputs\n\
        gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(gshTemplateRuntime);\n\
\n\
        GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();\n\
        \n\
        test102changePennId.gshRunLogic(gshTemplateV2input, gshTemplateV2output);\n\
        \n\
        System.out.println(gshTemplateV2output.getGsh_builtin_gshTemplateOutput());\n\
\n\
        return null;\n\
      }\n\
    });\n\
    \n\
    System.exit(0);\n\
\n\
  }\n\
\n\
}
grouperGshTemplate.changePennid.input.0.description = The previous PennId which is rolling into the new PennId
grouperGshTemplate.changePennid.input.0.label = Old PennId
grouperGshTemplate.changePennid.input.0.maxLength = 8
grouperGshTemplate.changePennid.input.0.name = gsh_input_oldPennId
grouperGshTemplate.changePennid.input.0.required = true
grouperGshTemplate.changePennid.input.0.validationMessage = Enter an 8 digit PennId
grouperGshTemplate.changePennid.input.0.validationRegex = ^[0-9]{8}\u0024
grouperGshTemplate.changePennid.input.0.validationType = regex
grouperGshTemplate.changePennid.input.1.description = Enter the new PennId that the old PennId should roll in to
grouperGshTemplate.changePennid.input.1.label = New PennId
grouperGshTemplate.changePennid.input.1.maxLength = 8
grouperGshTemplate.changePennid.input.1.name = gsh_input_newPennId
grouperGshTemplate.changePennid.input.1.required = true
grouperGshTemplate.changePennid.input.1.validationMessage = Enter an 8 digit PennId
grouperGshTemplate.changePennid.input.1.validationRegex = ^[0-9]{8}\u0024
grouperGshTemplate.changePennid.input.1.validationType = regex
grouperGshTemplate.changePennid.input.2.defaultValue = false
grouperGshTemplate.changePennid.input.2.description = If the old PennId should be removed
grouperGshTemplate.changePennid.input.2.label = Delete old PennId
grouperGshTemplate.changePennid.input.2.name = gsh_input_deleteOld
grouperGshTemplate.changePennid.input.2.type = boolean
grouperGshTemplate.changePennid.moreActionsLabel = Change PennId
grouperGshTemplate.changePennid.numberOfInputs = 3
grouperGshTemplate.changePennid.runAsType = currentUser
grouperGshTemplate.changePennid.runButtonGroupOrFolder = folder
grouperGshTemplate.changePennid.securityRunType = wheel
grouperGshTemplate.changePennid.showInMoreActions = true
grouperGshTemplate.changePennid.showOnFolders = true
grouperGshTemplate.changePennid.templateDescription = To change a PennId or merge a PennId into another one.  Will copy all the memberships and privileges over to the new PennId.  Can remove the old PennId.  Note\u003A the new PennId needs to be resolvable (I believe).
grouperGshTemplate.changePennid.templateName = Change PennId
grouperGshTemplate.changePennid.templateType = gsh
grouperGshTemplate.changePennid.templateVersion = V2

```

## The Subject API in Grouper architecture

> 

**See Also**

[Subject API Diagnostics in UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545428/Subject+API+diagnostics)

[Renaming a subject source](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544350/Renaming+a+subject+source)
