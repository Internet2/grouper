---
title: "Steps to run grouper-ws-scim"
space: GrIntDev
pageId: 48792551
version: 14
lastUpdated: 2026-07-12T07:01:09.322Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792551/Steps+to+run+grouper-ws-scim
---

### Option 1 - From installer

1. Run the installer, at end you will be prompted to install SCIM server

### Option 2 - From Source

1. run mvn clean install -Dlicense.skip=true from grouper-ws-scim module
2. Copy the generated war file from target directory and paste it to apache tomees' webapps directory
3. Start tomee by running bin/startup.sh

### Verify / run install

1. Go to url [http://localhost:8080/grouper-ws-scim](http://localhost:8080/grouper-ws-scim/) to make sure that the server is up.
2. Go to url: [http://localhost:8080/grouper-ws-scim/v2/Groups/top:user](http://localhost:8080/grouper-ws-scim/v2/Groups/top:user) (Make sure there is a group named top:user in grouper)
3. To create a new group: make a POST request to [http://localhost:8080/grouper-ws-scim/v2/Groups](http://localhost:8080/grouper-ws-scim/v2/Groups) with the payload:
  
  {  
  "id": "top:user25",  
  "displayName": "top display name user 25",  
  "urn:grouper:params:scim:schemas:extension:GroupExtension": {  
   "description": "User 25 Group simple description",  
   "typeOfGroup": "role",  
   "assignReadPrivToAll": true,  
   "assignViewPrivToAll": true,  
   "assignOptInPrivToAll": true  
  },  
  "schemas": [  
  "urn:ietf:params:scim:schemas:core:2.0:Group",  
  "urn:grouper:params:scim:schemas:extension:GroupExtension"  
  ]  
  }
4. For POST request above, set Content-Type header value to application/scim+json
5. To delete a Group, send a DELETE request to [http://localhost:8080/grouper-ws-scim/v2/Groups/top:user25](http://localhost:8080/grouper-ws-scim/v2/Groups/top:user25)
6. To Update a group send a PUT request to [http://localhost:8080/grouper-ws-scim/v2/Groups/top:user25](http://localhost:8080/grouper-ws-scim/v2/Groups/top:user25) with the payload:  
  {  
  "id": "top:user23",  
  "displayName": "top display name user 23",  
  "urn:grouper:params:scim:schemas:extension:GroupExtension": {  
  "description": "User 23 Group simple description updated",  
  "typeOfGroup": "group",  
  "assignReadPrivToAll": true,  
  "assignViewPrivToAll": true,  
  "assignOptInPrivToAll": false  
  },  
  "schemas": [  
  "urn:ietf:params:scim:schemas:core:2.0:Group",  
  "urn:grouper:params:scim:schemas:extension:GroupExtension"  
  ]  
  }
