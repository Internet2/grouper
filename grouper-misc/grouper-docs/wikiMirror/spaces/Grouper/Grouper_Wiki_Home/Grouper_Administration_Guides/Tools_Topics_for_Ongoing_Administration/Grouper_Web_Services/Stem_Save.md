---
title: "Stem Save"
space: Grouper
pageId: 28548111
version: 14
lastUpdated: 2026-07-12T15:26:51.384Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548111/Stem+Save
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Stem save will insert or update a stem's uuid, extension, display name, or description (with restrictions)

#### Features

- Can pass SaveMode which is INSERT, UPDATE, or INSERT_OR_UPDATE (default)
- If the parent stem doesnt exist, the call will fail
- Lookup stem to edit by stem lookup (by name or uuid)
- Returns stem
- Can actAs another user

#### Stem save Lite service

- Accepts one stem to save
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#stemSaveLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.misc.SaveMode-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on stemSaveLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#stemSaveLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveLiteRequest-) (click on stemSaveLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v1_3_000/stems/aStem%3AaStem2
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/stem/WsRestStemSaveLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsStemSaveLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsStemSaveLiteResult.WsStemSaveLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/stemSave/) (all files with "Lite" in them, click on "download" to see file)

#### Stem save service

- Accepts multiple stems to save
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#stemSave-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsStemToSave:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on stemSave), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#stemSave-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveRequest-) (click on stemSave)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v1_3_000/stems
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/stem/WsRestStemSaveRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/stem/WsRestStemSaveRequest.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsStemSaveResults.WsStemSaveResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsStemSaveResult.WsStemSaveResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/stemSave/) (all files without "Lite" in them, click on "download" to see files)

### Move or copy stem

Send in a wsStemLookup, and no wsStem. This will return SUCCESS_INSERTED if successful

Note, the documentation below is copied from the [move/copy documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545331/Move+and+Copy) which is most of up to date

Params (note param indexes passed in should be sequential starting with 0)

This will return the destination stem with the extension of the Stem copied/moved (which may or may not be the resulting stem)

| Param | Value | Notes |
| --- | --- | --- |
| [--paramName0=moveOrCopy] [--paramValue0=copy] | move\|copy |  |
| [--paramName1=moveOrCopyToStemUuid] [--paramValue1=abc123] | uuid of a stem to move or copy to | mutually exclusive with moveOrCopyStemName andmoveOrCopyToStemIdIndex |
| [--paramName2=moveOrCopyToStemName] [--paramValue2=a:b:c] | name of stem to move or copy to | mutually exclusive with moveOrCopyStemUuid andmoveOrCopyToStemIdIndex |
| [--paramName3=moveOrCopyToStemIdIndex] [--paramValue3=19] | id index of stem to move or copy to | mutually exclusive with moveOrCopyStemUuid andmoveOrCopyStemName |
| [--paramName4=copyPrivilegesOfGroup] [--paramValue4=true] | true\|false | Whether to copy the access privileges of the group.    If this option is selected, you must have READ access to all privileges. |
| [--paramName5=copyGroupAsPrivilege] [--paramValue5=true] | true\|false | Whether to copy access and naming privileges where the group is a member.    For instance, if you are copying Group X and Group X has admin privileges    to Group Y, then if this option is enabled, after Group X is copied, the new    group will also have admin privileges to Group Y. If this option is selected,    you must have access to add privileges to the other groups and folders. |
| [--paramName6=copyListMembersOfGroup] [--paramValue6=true] | true\|false | Whether to copy list memberships of the group. If this option is selected    and this group has custom lists, you must have read access to them. |
| [--paramName7=copyListGroupAsMember] [--paramValue7=true] | true\|false | Whether to copy list memberships where the group is a member.    For instance, if you are copying Group X and Group X is a member of    Group Y, then if this option is enabled, after Group X is copied, the    new group will also be a member of Group Y. If this option is selected,    you must have access to add memberships to the other groups. |
| [--paramName8=copyAttributes] [--paramValue8=true] | true\|false | Whether to copy attributes. If this option is selected, you must have READ   access to all attributes. These are attributes that are added to GroupTypes.    This does not include attributes in the new attribute framework in v1.5.0. |
| [--paramName9=moveAssignAlternateName] [--paramValue9=true] | true\|false | Whether to assign the old name of the stem as an alternate name of    the stem after the move. This allows API methods like StemFinder.findByName()   to find the stem using the old and new names and can make it easier to transition    from the old name to the new name. |
| [--paramName10=copyPrivilegesOfStem] [--paramValue10=true] | true\|false | Whether to copy the naming privileges of the folder and child folders. |

Example

```
<WsRestStemSaveRequest>
  <wsStemToSaves>
    <WsStemToSave>
      <wsStemLookup>
        <stemName>test:testStemToCopy</stemName>
      </wsStemLookup>
      <wsStem>
        <displayExtension>testStemToCopy</displayExtension>
        <name>test:testStemToCopy</name>
      </wsStem>
    </WsStemToSave>
  </wsStemToSaves>
  <params>
    <WsParam>
      <paramName>moveOrCopy</paramName>
      <paramValue>move</paramValue>
    </WsParam>
    <WsParam>
      <paramName>moveOrCopyToStemUuid</paramName>
      <paramValue>a465830677a8485c8abc3e7c31a312b3</paramValue>
    </WsParam>
    <WsParam>
      <paramName>moveAssignAlternateName</paramName>
      <paramValue>false</paramValue>
    </WsParam>
  </params>
</WsRestStemSaveRequest>
```

sdf
