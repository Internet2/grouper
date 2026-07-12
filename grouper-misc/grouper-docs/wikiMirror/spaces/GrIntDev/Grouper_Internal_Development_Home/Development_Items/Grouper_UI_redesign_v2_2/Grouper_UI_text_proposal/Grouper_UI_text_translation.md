---
title: "Grouper UI text translation"
space: GrIntDev
pageId: 48795902
version: 3
lastUpdated: 2026-07-12T07:02:40.907Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48795902/Grouper+UI+text+translation
---

There is a french translation to the grouper UI text file: grouper-ui\conf\grouperText\[grouper.text.en.us](http://grouper.text.en.us).base.properties

If you want to translate the text to a different language, or re-translate new properties, use the grouperInstaller

This utility will:

- Make a backup of the previous translated file, e.g. [grouper.text.fr.fr](http://grouper.text.fr.fr).base.2018_04_25_08_50_38_328.properties
- Go through the main properties file, and make sure all comments and whitespace are in the translated file
  
  - Note, if you have comments in the translated file, they will be removed
- There is an interactive mode that prompts the user for the translated value, prompting the user with the comments and english version
- The non-interactive mode will still sync up the properties and comments but not prompt the user for each key which does not have a translation
- Remove keys in the translated file which are not in the original file

### Interactive mode

Prompt the user for each untranslated key.

```
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, 'admin' utilities, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'admin', 'createPatch' or blank for the default) [admin]: <using autorun property grouperInstaller.autorun.actionEgInstallUpgradePatch>: 'admin'
What admin action do you want to do (manage, upgradeTask, develop)? : <using autorun property grouperInstaller.autorun.adminAction>: 'develop'
What do you want to develop (translate, back, exit)? : <using autorun property grouperInstaller.autorun.developAction>: 'translate'
What is the location of the grouper.text.en.us.base.properties file: 
<using autorun property grouperInstaller.autorun.translate.from>: 'C:/Users/mchyzer/Documents/GitHub/grouper_v2_3/grouper-ui/conf/grouperText/grouper.text.en.us.base.properties'
What is the location of the translated file: 
<using autorun property grouperInstaller.autorun.translate.to>: 'C:/Users/mchyzer/Documents/GitHub/grouper_v2_3/grouper-ui/conf/grouperText/grouper.text.fr.fr.base.properties'
The translated file was backed up to: C:\Users\mchyzer\Documents\GitHub\grouper_v2_3\grouper-ui\conf\grouperText\grouper.text.fr.fr.base.2018_04_25_09_14_47_929.properties
Do you want to edit this file inline (if not will just run a report) (t|f) [t]: <grouperInstaller.translate.editInline>: 

tooltipTargetted.stems.copy.copyListGroupAsMember=If you select this option and groups in the folder being copied are members of other groups, the new copied groups will be added to the other groups' membership list.  If you do not have access to add members to the other groups, you will get a privilege error.

1: Enter a translation for line 199:<autorun.translate.value>: test test

tooltipTargetted.stems.copy.copyGroupAsPrivilege=If you select this option and groups in the folder being copied have privileges to other groups or folders, the new copied groups will also be given privileges to those other groups or folders.  If you do not have access to add privileges to the other groups or folders, you will get a privilege error.

2: Enter a translation for line 201:<autorun.translate.value>: another guy

saved-subjects.stems.added=This folder was added to your list in the folder workspace.

3: Enter a translation for line 992:<autorun.translate.value>: more more more

externalSubjectSelfRegister.indicatesRequiredField = indicates a required field

4: Enter a translation for line 1713:<autorun.translate.value>: yes it does

simpleAttributeUpdate.assignHeaderAssignmentType = Assignment type

5: Enter a translation for line 1944:<autorun.translate.value>: here we go again
You translated 5 missing properties.

```

### Generate a report

This is a non interactive mode that will setup the translated file with all properties and let the user know which properties need to be translated.

```
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, 'admin' utilities, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'admin', 'createPatch' or blank for the default) [admin]: <using autorun property grouperInstaller.autorun.actionEgInstallUpgradePatch>: 'admin'
What admin action do you want to do (manage, upgradeTask, develop)? : <using autorun property grouperInstaller.autorun.adminAction>: 'develop'
What do you want to develop (translate, back, exit)? : <using autorun property grouperInstaller.autorun.developAction>: 'translate'
What is the location of the grouper.text.en.us.base.properties file: 
<using autorun property grouperInstaller.autorun.translate.from>: 'C:/Users/mchyzer/Documents/GitHub/grouper_v2_3/grouper-ui/conf/grouperText/grouper.text.en.us.base.properties'
What is the location of the translated file: 
<using autorun property grouperInstaller.autorun.translate.to>: 'C:/Users/mchyzer/Documents/GitHub/grouper_v2_3/grouper-ui/conf/grouperText/grouper.text.fr.fr.base.properties'
The translated file was backed up to: C:\Users\mchyzer\Documents\GitHub\grouper_v2_3\grouper-ui\conf\grouperText\grouper.text.fr.fr.base.2018_04_25_08_50_38_328.properties
Do you want to edit this file inline (if not will just run a report) (t|f) [t]: <grouperInstaller.translate.editInline>: f
1: Translate line 199:

tooltipTargetted.stems.copy.copyListGroupAsMember=If you select this option and groups in the folder being copied are members of other groups, the new copied groups will be added to the other groups' membership list.  If you do not have access to add members to the other groups, you will get a privilege error.

2: Translate line 201:

tooltipTargetted.stems.copy.copyGroupAsPrivilege=If you select this option and groups in the folder being copied have privileges to other groups or folders, the new copied groups will also be given privileges to those other groups or folders.  If you do not have access to add privileges to the other groups or folders, you will get a privilege error.

3: Translate line 992:

saved-subjects.stems.added=This folder was added to your list in the folder workspace.

4: Translate line 1713:

externalSubjectSelfRegister.indicatesRequiredField = indicates a required field

...

19: Translate line 3252:

# if you cant view a group or entity, this will be in the extension and name
# the prefix is what it uses to set the folder
guiCantViewPrefix = You cannot VIEW

...

22: Translate line 3261:

# when a subject cannot be resolved
guiUnresolvableSubject = Entity not found

...

850: Translate line 9012:

audits_STEM_ATTESTATION_UPDATE = <strong>$$auditsEdited$$</strong> $$auditsAttestation$$ on $$auditsStem$$ ${grouperRequestContainer.guiAuditEntry.guiStem.shortLink}.

You have 850 missing properties, they need translation:

```
