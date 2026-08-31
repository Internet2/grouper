---
title: "Subject API diagnostics"
space: Grouper
pageId: 28545428
version: 17
lastUpdated: 2026-07-01T05:47:14.098Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545428/Subject+API+diagnostics
---

The Subject API Diagnostics screen is an administrative screen for Grouper admins to troubleshoot and verify [subject API](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544786/Subject+sources) sources.

This is on by default in a 2.3.0 patch or 2.3.1+.

## Configure

Set this in the grouper-ui.properties to customize this enhancement

```
# should show subject api diagnostics?
uiV2.admin.subjectApiDiagnostics.show = true
 
# put in a group here if you want to allow the subject API diagnostics to certin users.  
# note, admins can always see the screen
uiV2.admin.subjectApiDiagnostics.must.be.in.group =
```

## Run from GSH

If the UI doesnt start

grouper.properties (temporarily)

```
gsh.exitOnSubjectCheckConfigProblem = false
```

if gsh doesnt start, try:

1. take source out of subject.properties
2. start gsh and get prompt
3. add source back in to subject.properties
4. run this in GSH
  
  
  ```
  GrouperSession.startRootSession();
  edu.internet2.middleware.grouper.cache.GrouperCacheUtils.clearAllCaches();
  GrouperUtil.assignField(edu.internet2.middleware.subject.provider.SourceManager.class, null, "manager", null, null);
  -- continue below --
  ```

gsh

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

## Run

Go to the New UI, click on Miscellaneous, then click on "Subject API diagnostics"

## Try

If you have an account on the demo server you can see the diagnostics there:

[https://grouperdemo.internet2.edu/grouper_v2_3/grouperUi/app/UiV2Main.index?operation=UiV2Admin.subjectApiDiagnostics](https://grouperdemo.internet2.edu/grouper_v2_3/grouperUi/app/UiV2Main.index?operation=UiV2Admin.subjectApiDiagnostics)

## Diagnostics

Note: email attribute is not validated

```
SUCCESS: The emailAttributeName is configured to be: 'mail'
SUCCESS: The email address 'whatever@someplace.edu' was found and has a valid format
```

See Also

[Subject sources](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544786/Subject+sources)
