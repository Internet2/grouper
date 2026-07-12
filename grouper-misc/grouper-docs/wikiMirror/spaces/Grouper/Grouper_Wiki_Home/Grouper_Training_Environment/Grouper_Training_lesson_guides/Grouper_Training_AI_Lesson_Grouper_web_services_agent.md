---
title: "Grouper Training - AI - Lesson: Grouper web services agent"
space: Grouper
pageId: 28545405
version: 15
lastUpdated: 2025-12-08T17:34:38.096Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545405/Grouper+Training+-+AI+-+Lesson+Grouper+web+services+agent
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

#### Try out the WS agent

1. Setup the GTE to respond to WS calls
2. Make a group of users who can call WS (probably not necessary but it is a best practice). In the "etc" folder, make a group:   
    
  
  ```
  webServiceClientUsers
  ```
3. Add banderson to that group
4. Configure that group to control who can call WS. Import this config into grouper-ws properties or just add one config in the UI.  
  Miscellaneous → Configure → Configuration files → Actions → Import → paste → grouper-ws.properties
  
  
  ```
  ws.client.user.group.name = etc:webServiceClientUsers
  ```
5. Set a password for banderson
6. In your VM command line:
  
  
  ```
  gte-gsh
  ```
  
    
  When you get the GSH prompt:  
    
  
  ```
  new GrouperPasswordSave().assignApplication(GrouperPassword.Application.WS).assignUsername("banderson").assignPassword("password").save();
  
  :q
  ```
7. [Navigate to the agent](https://chatgpt.com/g/g-68489514bbac819183145375142b0215-internet2-grouper-web-service-agent/). This is a publicly available custom GPT that can write Grouper web service requests.
8. Enter this prompt  
    
  
  ```
  find all direct members of test:testGroup
  ```
  
    
  If it asks you for endpoint use  
    
  
  ```
  https://localhost:8443/grouper-ws/servicesRest/json/v5_20_5
  ```
  
    
  If it asks you for subject source, use:  
    
  
  ```
  eduLDAP
  ```
  
    
  If it asks you for authentication method use  
    
  
  ```
  basic
  ```
9. If ChatGTP doesn't give the curl command, ask it for one. Tell it to use insecure SSL, the user is banderson and password is password  
  You should get something like this command:  
    
  
  ```
  curl --insecure -u "banderson:password" \
    -H "Content-Type: application/json" \
    -X POST \
    -d '{
    "WsRestGetMembersRequest": {
      "wsGroupLookups": [
        {
          "groupName": "test:testGroup"
        }
      ],
      "memberFilter": "immediate"
    }
  }' \
    "https://localhost:8443/grouper-ws/servicesRest/v5_20_5/groups"
  ```
10. Question from slide
11. Enter this prompt:  
    
  
  ```
  When I ask about Grouper web services, tailor it to my institution.  My base url is: 
  
  https://localhost:8443/grouper-ws/servicesRest/json/v5_20_5
  
  And my username is banderson, and I am using basic authentication in the HTTP authorization header.  my subject source id is eduLDAP.  By default to not include subject detail.  My subject ID's are numeric employee IDs.
  ```
12. Enter this prompt  
    
  
  ```
  show me a sample response
  ```
13. Create a new GPT: Explore GPT → Create
14. Configure tab
15. Name:   
    
  
  ```
  My Grouper WS agent
  ```
16. Description  
    
  
  ```
  Helps craft, fix, and understand my institution's Grouper Web Service REST JSON requests.
  ```
17. Instructions  
    
  
  ```
  This GPT acts as a Grouper Web Services agent to assist users in crafting, validating, and understanding RESTful web service calls to a Grouper installation. It begins by asking for the user's endpoint (e.g., https://grouperws.server.school.edu/grouperWs), which it uses to determine the correct path prefix for requests instead of defaulting to "grouper-ws." It then asks for the user's subject source and the kind of authentication they use, giving examples based on their answer.
  
  It guides users exclusively in constructing REST calls using JSON, avoiding XML and lite formats entirely. It helps identify and fix issues in invalid requests, and formats responses for clarity. It avoids including the `actAsSubjectLookup` field unless the user is specifically doing an "act as" operation. It avoids including the `fieldName` field unless the request is explicitly about privileges, since it defaults to "members" otherwise. It relies on the structure defined in the `grouperWs_swagger.json` specification but adapts the path based on the user-provided endpoint.
  
  The GPT reminds users that the WS user must have valid basic auth credentials, the correct privileges on the Grouper objects involved, and potentially membership in a WS-allowed group as required by their environment. It can provide sample calls such as adding members to groups and interpret the responses meaningfully.
  
  When asked about Grouper web services, tailor it to my institution.  My base url is: 
  
  https://localhost:8443/grouper-ws/servicesRest/json/v5_20_005
  
  I am using basic authentication in the HTTP authorization header.  my subject source id is eduLDAP.  By default to not include subject detail.  My subject ID's are numeric employee IDs.  When you see something like this in the swagger "vG_E_MEF", that is the version "v5_20_005"
  ```
18. Download grouperWs_swagger.json from: [https://github.com/Internet2/grouper/tree/GROUPER_5_BRANCH/grouper/misc/aiGsh](https://github.com/Internet2/grouper/tree/GROUPER_5_BRANCH/grouper/misc/aiGsh)
19. Upload that as a knowledge file
20. Capabilities: web search and canvas
21. Model: GPT 5.1
22. Additional settings: uncheck box to improve model based on conversation
23. Create / Save the GPT, and run it, and enter the prompt  
    
  
  ```
  find all direct members of test:testGroup
  
  ```
  
  If it does not know what direct means, tell it that it means immediate
