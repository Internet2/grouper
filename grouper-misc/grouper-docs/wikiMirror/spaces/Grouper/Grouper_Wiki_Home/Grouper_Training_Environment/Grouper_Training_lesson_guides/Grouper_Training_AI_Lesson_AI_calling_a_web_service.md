---
title: "Grouper Training - AI - Lesson: AI calling a web service"
space: Grouper
pageId: 28544838
version: 8
lastUpdated: 2025-12-07T19:41:49.168Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544838/Grouper+Training+-+AI+-+Lesson+AI+calling+a+web+service
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson guide**

1. (optional) If you are in the demo server, [see this RedCapUser group](https://grouperdemo.internet2.edu/grouper_v5/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=a08831a68d874c9880e2edb6a4ad317d)
2. See members
3. Example subject IDs:  
    
  
  ```
  test.subject.0
  test.subject.1
  test.subject.2
  test.subject.3
  test.subject.4
  test.subject.7
  test.subject.9
  ```
4. Go to ChatGPT → GPTs → Explore → + Create
5. Create tab
6. Enter this prompt in the create prompt  
    
  
  ```
  Make a GPT that calls actions against Internet2 Grouper to determine if someone has access to an app (that the user inputs).  Ask the user for the subject ID of the app user they are interested in.  Ask the user for a description of the service, then search for the policy group.  If there are multiple and you are not confident as to which one should be used, ask the user which one of the choices it is.  Then using the description of that group, see what the policy is (it must be in the description).  Based on the subject ID of the app user they are interested (previously entered), see if they are in the policy group, and also proactively check all of the groups that make up the policy, and explain to the GPT user exactly why the app user does or does not have access. 
  ```
7. When it asks for the name of the GPT, use:  
    
  
  ```
  Grouper Demo Access Analyst
  ```
8. Accept whatever image it makes for you, and type  
    
  
  ```
  finish
  ```
9. Click the Configure tab
10. Recommended model: GPT-5.1 thinking
11. Remove all Capabilities
12. Click "Create" to save your work
13. Go back to Edit the GPT in Configure tab
14. Click on Actions → Create new Action
15. API Key authn, Auth type: Basic, get API key from slack (do not share this or use it after the training)
16. Click "Get help from actions GPT"
17. Enter this prompt in the new tab  
    
  
  ```
  create a spec for this API.  there are two actions.
  Server domain name: https://grouperdemo.internet2.edu
  Put the paths below in their path in the spec
  
  1. Finding a group
  POST /grouper-ws_v5/servicesRest/v5_0_000/groups
  Content-Type: application/json
  
  sample request (searchTerm is dynamic, whatever being searched for):
  {
    "WsRestFindGroupsLiteRequest":{
      "queryFilterType":"FIND_BY_GROUP_NAME_APPROXIMATE",
      "groupName":"searchTerm"
    }
  }
  
  sample response (the name and description of each group will be used by the GPT later):
  {
    "WsFindGroupsResults": {
      "groupResults": [{
        "extension": "users",
        "typeOfGroup": "group",
        "displayExtension": "users",
        "description": "This is the group of users who can use the issueTracker",
        "displayName": "apps:issueTracker:users",
        "name": "apps:issueTracker:users",
        "uuid": "d411272913d74c8fb7d4d4deb76cfbd5",
        "idIndex": "10320",
        "enabled": "T"
      }],
      "resultMetadata": {
        "resultCode": "SUCCESS",
        "resultMessage": "Success for: clientVersion: 5.0.0, wsQueryFilter: WsQueryFilter[queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE,groupName=issuetracker]\n, includeGroupDetail: false, actAsSubject: null, paramNames: \n, params: null\n, wsGroupLookups: null",
        "success": "T"
      },
      "responseMetadata": {
        "millis": "450",
        "serverVersion": "5.22.4"
      }
    }
  }
  
  2. Seeing if a user (by subject ID) is in a group (by groupName).
  
  POST /grouper-ws_v5/servicesRest/v5_0_001/groups
  Content-Type: application/json
  
  sample request, the group name and subject id are dynamic:
  {
    "WsRestHasMemberRequest":{
      "wsGroupLookup": {
        "groupName": "apps:issueTracker:users"
      },
      "subjectLookups":[
        {
          "subjectId":"test.subject.2"
        }
      ]
    }
  }
  
  Sample response.  The boolean to focus on is: IS_NOT_MEMBER (false) or IS_MEMBER (true)
  
  {
    "WsHasMemberResults": {
      "results": [{
        "wsSubject": {
          "resultCode": "SUCCESS",
          "success": "T",
          "id": "test.subject.2",
          "name": "my name is test.subject.2",
          "sourceId": "jdbc"
        },
        "resultMetadata": {
          "resultCode": "IS_NOT_MEMBER",
          "success": "T"
        }
      }],
      "wsGroup": {
        "extension": "users",
        "typeOfGroup": "group",
        "displayExtension": "users",
        "description": "This is the group of users who can use the issueTracker",
        "displayName": "apps:issueTracker:users",
        "name": "apps:issueTracker:users",
        "uuid": "d411272913d74c8fb7d4d4deb76cfbd5",
        "idIndex": "10320",
        "enabled": "T"
      },
      "resultMetadata": {
        "resultCode": "SUCCESS",
        "resultMessage": "Success for: clientVersion: 5.0.0, wsGroupLookup: WsGroupLookup[pitGroups=[],groupName=apps:issueTracker:users], subjectLookups: Array size: 1: [0]: WsSubjectLookup[subjectId=test.subject.2]\n\n memberFilter: All, actAsSubject: null, fieldName: null, includeGroupDetail: false, includeSubjectDetail: false, subjectAttributeNames: null\n,params: null\n,pointInTimeFrom: null, pointInTimeTo: null",
        "success": "T"
      },
      "responseMetadata": {
        "millis": "797",
        "serverVersion": "5.22.4"
      }
    }
  }
  
  
  ```
18. Take your spec (use copy button on chatgpt) or use this one if yours does not work, paste back in to the other tab (schema textarea) where configuring the GPT  
    
  
  ```
  openapi: 3.1.0
  info:
    title: Grouper Demo API
    description: |
      This API allows clients to search for groups and check group membership using the Internet2 Grouper demo service.
    version: 1.0.0
  servers:
    - url: https://grouperdemo.internet2.edu
      description: Grouper Demo Server
  paths:
    /grouper-ws_v5/servicesRest/v5_0_000/groups:
      post:
        operationId: findGroups
        summary: Find groups by name (approximate match)
        description: Search for groups using an approximate group name match.
        requestBody:
          required: true
          content:
            application/json:
              schema:
                type: object
                properties:
                  WsRestFindGroupsLiteRequest:
                    type: object
                    required:
                      - queryFilterType
                      - groupName
                    properties:
                      queryFilterType:
                        type: string
                        enum: [FIND_BY_GROUP_NAME_APPROXIMATE]
                        example: FIND_BY_GROUP_NAME_APPROXIMATE
                      groupName:
                        type: string
                        description: Group name search term (partial/approximate).
                        example: issueTracker
        responses:
          '200':
            description: A list of groups matching the search term.
            content:
              application/json:
                schema:
                  type: object
                  properties:
                    WsFindGroupsResults:
                      type: object
                      properties:
                        groupResults:
                          type: array
                          items:
                            type: object
                            properties:
                              extension:
                                type: string
                              typeOfGroup:
                                type: string
                              displayExtension:
                                type: string
                              description:
                                type: string
                              displayName:
                                type: string
                              name:
                                type: string
                              uuid:
                                type: string
                              idIndex:
                                type: string
                              enabled:
                                type: string
                        resultMetadata:
                          type: object
                          properties:
                            resultCode:
                              type: string
                            resultMessage:
                              type: string
                            success:
                              type: string
                        responseMetadata:
                          type: object
                          properties:
                            millis:
                              type: string
                            serverVersion:
                              type: string
  
    /grouper-ws_v5/servicesRest/v5_0_001/groups:
      post:
        operationId: checkGroupMembership
        summary: Check if a user is a member of a group
        description: Determine if a user (by subjectId) is a member of the specified group.
        requestBody:
          required: true
          content:
            application/json:
              schema:
                type: object
                properties:
                  WsRestHasMemberRequest:
                    type: object
                    required:
                      - wsGroupLookup
                      - subjectLookups
                    properties:
                      wsGroupLookup:
                        type: object
                        required:
                          - groupName
                        properties:
                          groupName:
                            type: string
                            description: Fully qualified name of the group.
                            example: apps:issueTracker:users
                      subjectLookups:
                        type: array
                        items:
                          type: object
                          required:
                            - subjectId
                          properties:
                            subjectId:
                              type: string
                              description: The subject ID of the user.
                              example: test.subject.2
        responses:
          '200':
            description: Membership result
            content:
              application/json:
                schema:
                  type: object
                  properties:
                    WsHasMemberResults:
                      type: object
                      properties:
                        results:
                          type: array
                          items:
                            type: object
                            properties:
                              wsSubject:
                                type: object
                                properties:
                                  resultCode:
                                    type: string
                                  success:
                                    type: string
                                  id:
                                    type: string
                                  name:
                                    type: string
                                  sourceId:
                                    type: string
                              resultMetadata:
                                type: object
                                properties:
                                  resultCode:
                                    type: string
                                    description: Result code such as IS_MEMBER or IS_NOT_MEMBER.
                                  success:
                                    type: string
                        wsGroup:
                          type: object
                          properties:
                            extension:
                              type: string
                            typeOfGroup:
                              type: string
                            displayExtension:
                              type: string
                            description:
                              type: string
                            displayName:
                              type: string
                            name:
                              type: string
                            uuid:
                              type: string
                            idIndex:
                              type: string
                            enabled:
                              type: string
                        resultMetadata:
                          type: object
                          properties:
                            resultCode:
                              type: string
                            resultMessage:
                              type: string
                            success:
                              type: string
                        responseMetadata:
                          type: object
                          properties:
                            millis:
                              type: string
                            serverVersion:
                              type: string
  ```
  
    
    
  
  
  1. Note the paths above have a slightly different version just so they can have unique paths in the spec
19. Click Update in upper right to save the GPT
20. Close the other tab that made the API spec (keep the Custom GPT tab open)
21. Use the GPT and enter this prompt (click through approvals)  
    
  
  ```
  analyze test.subject.0 for the redcap service
  ```
22.
