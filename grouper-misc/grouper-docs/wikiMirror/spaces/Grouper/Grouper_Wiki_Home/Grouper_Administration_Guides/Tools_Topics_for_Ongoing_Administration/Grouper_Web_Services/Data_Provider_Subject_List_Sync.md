---
title: "Data Provider Subject List Sync"
space: Grouper
pageId: 28549041
version: 3
lastUpdated: 2026-07-01T05:42:55.146Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549041/Data+Provider+Subject+List+Sync
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Execute a data provider sync on a list of subjects. v5.21.4+

#### Features

- Identify the data provider by config id
- Specify the list of subjects by subject id or identifier
- Can actAs another user
- If using the new data field subject source, this can be used to immediately add the subject to Grouper once it's available in the data provider.

#### Data provider subject list sync

- REST request: POST /grouper-ws/servicesRest/v5_0_000/dataProviderSubjectListSync
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGshTemplateExecResult.WsGshTemplateExecResultCode.html)

#### Example of sending JSON output in an output line

```
POST https://grouperWs.school.edu/grouper-ws/servicesRest/v5_0_000/dataProviderSubjectListSync
Content-Type: application/json
Authorization: sas9f8d7sa9df87asd98f

{
  "WsRestDataProviderSubjectListSyncRequest": {
    "dataProviderConfigId": "dataFieldSource",
    "subjectLookups": [
      {
        "subjectId": "test.subject.1",
        "subjectSourceId": "dataFieldSubjectSource"
      },
      {
        "subjectId": "test.subject.2"
      },
      {
        "subjectIdentifier": "id.test.subject.3",
        "subjectSourceId": "dataFieldSubjectSource"
      }
    ]
  }
}

RESPONSE
STATUS: 200
x-grouper-resultcode: SUCCESS
x-grouper-resultcode2: NONE
x-grouper-success: T
{
  "WsDataProviderSubjectListSyncResult": {
    "resultMetadata": {
      "resultCode": "SUCCESS",
      "resultMessage": "Success for: clientVersion: 5.0.0, dataProviderConfigId: dataFieldSource, subjectLookups: Array size: 3: [0]: WsSubjectLookup[subjectId=test.subject.1,subjectSourceId=dataFieldSubjectSou...\n, actAsSubject: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "192",
      "serverVersion": "5.0.0"
    }
  }
}

```
