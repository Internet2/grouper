---
title: "Grouper UI favorites and preferences user data"
space: GrIntDev
pageId: 48824692
version: 10
lastUpdated: 2026-07-12T06:46:36.382Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824692/Grouper+UI+favorites+and+preferences+user+data
---

The Grouper 2.2 UI can hold preferences, favorites, and recently used objects are stored with the user.

 This information is stored as attributes on a membership on a UI users group. This group is generally internal to the UI and not needed outside. If no one can READ or VIEW it, it is invisible to users.

 The attributes are single assignable, and single valued. The value type is String.

 Attribute data is stored as a JSON object in the attribute value. The structure of the recently used objects and favorites is:

 
```

{
  "list": [
    {
      "timestamp": 1374034138579,
      "uuid": "d9b0b9c3d3bc43ddb082a3f2fde6fee5"
    },
    {
      "timestamp": 1374034119978,
      "uuid": "c0cc2ebbbe994574ab5deed048d92676"
    }
  ]
}

```

 The JSON is stored in one attribute which has a max size of 4k characters, and the max number of elements is 30. So if you have more than 30 favorites, they are in a queue by date, and the oldest one will fall off.

 The built in user data types are:

 

- favoriteAttributeDef
- favoriteAttributeDefName
- favoriteGroup
- favoriteMember
- favoriteStem
- recentAttributeDef
- recentAttributeDefName
- recentGroup
- recentMember
- recentStem

 The API to manipulate the attribute is:

 
```

    GrouperUserDataApi.recentlyUsedStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);

```

 Note that the userDataGroupName is the group which has the membership. So multiple UIs could use this feature to store their own recently used or favorite items.

 In the UI, we will store changes periodically, and eventually we should mark the attributes as not applicable for auditing and point in time auditing.

 Here is some GSH to run from the WEB-INF/bin dir to manage Grouper UI favorites

 
```

grouperSession = GrouperSession.startRootSession();
subject = SubjectFinder.findRootSubject();

group = GroupFinder.findByName(grouperSession, "test:testGroup", true);
GrouperUserDataApi.favoriteGroupAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, group);

stem = StemFinder.findByName(grouperSession, "test", true);
GrouperUserDataApi.favoriteStemAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, stem);

stem = StemFinder.findByName(grouperSession, "test:test2", true);
GrouperUserDataApi.favoriteStemAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, stem);

subjectFavorite = SubjectFinder.findByIdOrIdentifier("test.subject.0", true);
GrouperUserDataApi.favoriteMemberAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, subjectFavorite);

subjectFavorite = SubjectFinder.findByIdOrIdentifier("test.subject.1", true);
GrouperUserDataApi.favoriteMemberAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, subjectFavorite);

attributeDef = AttributeDefFinder.findByName("etc:attribute:rules:rulesTypeDef", true);
GrouperUserDataApi.favoriteAttributeDefAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, attributeDef);

attributeDef = AttributeDefFinder.findByName("etc:attribute:loaderLdap:grouperLoaderLdapValueDef", true);
GrouperUserDataApi.favoriteAttributeDefAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, attributeDef);

attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:rules:ruleCheckOwnerId", true);
GrouperUserDataApi.favoriteAttributeDefNameAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, attributeDefName);

attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:permissionLimits:limitWeekday9to5", true);
GrouperUserDataApi.favoriteAttributeDefNameAdd(edu.internet2.middleware.grouper.ui.util.GrouperUiUserData.grouperUiGroupNameForUserData(), subject, attributeDefName);

```

 sdf
