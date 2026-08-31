---
title: "Developer's Guide to the Grouper API"
space: GrIntDev
pageId: 48792640
version: 8
lastUpdated: 2026-07-12T06:45:35.726Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792640/Developer+s+Guide+to+the+Grouper+API
---

### Using the Grouper API to bootstrap your Groups Registry

This document is current as of the v2.0 release.

- Find *GrouperSystem* Subject

```
Subject grouperSystem = SubjectFinder.findRootSubject();

```

- Start-and-stop sessions

```
Subject subject = SubjectFinder.findById("mchyzer", true);
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      
      //do some grouper stuff
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

```

- Find the root stem

```
Stem rootStem = StemFinder.findRootStem(grouperSession);

```

- Find stem by name (note: the true/false is if an exception should be thrown if not found)

```
Stem stem = StemFinder.findbyName(grouperSession, "some:stem:name", true);

```

- Create stem

```
Stem stem = new StemSave(grouperSession).assignName("some:stem:name").assignCreateParentStemsIfNotExist(true).save();

```

- Find group by name (note: the true/false is if an exception should be thrown if not found)

```
Group group = GroupFinder.findByName(grouperSession, "some:group:name", true);

```

- Create group

```
Group group = new GroupSave(grouperSession).assignName("some:folder:groupName").save();

```

- Find *GrouperAll* subject

```
Subject grouperAll = SubjectFinder.findAllSubject();

```

- Check for membership in the wheel group
  
  
  ```
   boolean isWheelOrRoot = PrivilegeHelper.isWheelOrRoot(subject);
  
  ```
- Add wheel group member

```
String groupName = GrouperConfig.getProperty( GrouperConfig.PROP_WHEEL_GROUP );
Group wheelGroup = GroupFinder.findByName( GrouperSession.staticGrouperSession().internal_getRootSession(), groupName, true );
wheelGroup.addMember(subject, false);

```

- See if a subject is in the registry by ID and source ID, without resolving the subject

```
gsh 0% GrouperSession grouperSession = GrouperSession.startRootSession();
gsh 1% Subject subject = new SubjectImpl("test.subject.0", null, null, "person", "jdbc");
gsh 2% MemberFinder.findBySubject(grouperSession, subject, false);
member: id='test.subject.0' type='person' source='jdbc' uuid='fc98430743f7435d9f69286624a2f8b5' 
gsh 3% subject = new SubjectImpl("test.subject.0xyz", null, null, "person", "jdbc");
subject: id='test.subject.0xyz' type='person' source='jdbc' name='null' 
gsh 4% MemberFinder.findBySubject(grouperSession, subject, false);
gsh 5%

```
