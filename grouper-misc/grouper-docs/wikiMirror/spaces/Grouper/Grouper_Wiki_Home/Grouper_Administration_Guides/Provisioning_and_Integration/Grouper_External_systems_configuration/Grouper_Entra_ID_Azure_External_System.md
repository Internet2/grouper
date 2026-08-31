---
title: "Grouper Entra ID (Azure) External System"
space: Grouper
pageId: 28549126
version: 5
lastUpdated: 2026-07-01T05:42:46.147Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549126/Grouper+Entra+ID+Azure+External+System
---

## Configuration example

grouper-loader.properties

```
grouper.azureConnector.myAzure.clientId = 51e6dc4f-a85d-41c7-9569-8ac1b3159801
grouper.azureConnector.myAzure.clientSecret = *******
grouper.azureConnector.myAzure.graphEndpoint = https://graph.microsoft.com
grouper.azureConnector.myAzure.graphVersion = beta
grouper.azureConnector.myAzure.groupLookupAttribute = displayName
grouper.azureConnector.myAzure.groupLookupValueFormat = ${group.getName()}
grouper.azureConnector.myAzure.loginEndpoint = https://login.microsoftonline.com/
grouper.azureConnector.myAzure.resource = https://graph.microsoft.com
grouper.azureConnector.myAzure.resourceEndpoint = https://graph.microsoft.com/beta/
grouper.azureConnector.myAzure.tenantId = 455754be-3a2b-40c9-acef-c425a92d7276
```

## Set up Entra ID / Azure

1. Sign up with Azure
2. On the left menu, go to Azure Active Directory
3. Create a new app registration
  
  1. Select: Who can use this app: Accounts in any organizational directory (Any Azure AD directory - Multitenant) and personal Microsoft accounts (e.g. Skype, Xbox)
4. After the app is registered, click on API Permissions and give Microsoft graph access
  
  1. Give full permissions for Directory, Group, User, and GroupMember
    
    1. Note: if you give smaller credentials, e.g. Directory.Read.All, and give owner on specific groups, then the credential can only manage those specific groups
  2. Grant admin consent for default directory
  3. Check [https://jwt.ms](https://jwt.ms) with the token, should see
  4. Permissions look like this. Note you can clamp down these permissions as needed
  5. From basic testing, if using read-only entities, the following Admin consent permissions seems to work (should not need any User consent grants):
  6. For even tight permissions, see below for setting the Grouper service account as the owner for new groups
5. On the left, under Certificates and Secrets, create a new secret
6. When testing using Postman, you will only need the secret value to get access token which will be used to call the graph API
7. To get an access token, make a POST call to [https://login.microsoftonline.com/a98c57b9-a771-4c01-b69b-83cceb36c834/oauth2/v2.0/token](https://login.microsoftonline.com/a98c57b9-a771-4c01-b69b-83cceb36c834/oauth2/v2.0/token) (id is the directory tenant id)
8. Under form data send these four key values. client_id = clientId, scope = [https://graph.microsoft.com/.default](https://graph.microsoft.com/.default), client_secret = clientSecret, grant_type=client_credentials
  
  1. Content-type: application/x-www-form-urlencoded
  2. Post body looks like this:
    
    
    ```
    client_id=aea2eb2a-bc4f-4ae5-a315-3XXXXX&scope=https%3A%2F%2Fgraph.microsoft.com%2F.default&grant_type=client_credentials&client_secret=ewC8Q~yGN4dyBaSYBrOXXXXXXXXXX
    ```
  3. Configure external system in grouper-loader.properties
    
    
    ```
    grouper.azureConnector.azure.clientId = aea2eb2a-bc4f-4ae5-a315-38XXXXX
    grouper.azureConnector.azure.clientSecret = ewC8Q~yXXXXX
    grouper.azureConnector.azure.graphEndpoint = https://graph.microsoft.com
    grouper.azureConnector.azure.graphVersion = beta
    grouper.azureConnector.azure.loginEndpoint = https://login.microsoftonline.com/
    grouper.azureConnector.azure.resource = https://graph.microsoft.com
    grouper.azureConnector.azure.resourceEndpoint = https://graph.microsoft.com/beta/
    grouper.azureConnector.azure.tenantId = 5e7fa4df-8d24XXXXXXX
    ```
9. The client id is the Application (client) ID next to Directory tenant id on the Overview page of the app.
10. The response from the above POST call will give you an access token in the body which we will use to access graph APIs like [https://graph.microsoft.com/v1.0/groups](https://graph.microsoft.com/v1.0/groups)
11. For the above request, send Authorization header with value Bearer <access token>

## Add Microsoft certificate for graph apis

1. Go to [https://graph.microsoft.com/applications](https://graph.microsoft.com/applications) in your browser and download the certificate by clicking on the padlock sign in the address bar.
2. Find out the path to the security directory inside the jre. e.g. /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/jre/lib/security
3. From the terminal run "sudo keytool -import -alias microsoft.graph -keystore cacerts -file ~/[graph.microsoft.com](http://graph.microsoft.com).cer
4. For the password enter: changeit

## Grouper development team testing

Set this in grouper.hibernate.properties (or set env var: GROUPER_MOCK_SERVICES=true)

```
grouper.is.mockServices = true
```

test config

```
grouper.azureConnector.azureTest.clientId = fd805xxxxdfb
grouper.azureConnector.azureTest.clientSecret = *******
grouper.azureConnector.azureTest.graphEndpoint = https://graph.microsoft.com
grouper.azureConnector.azureTest.graphVersion = v1.0
grouper.azureConnector.azureTest.loginEndpoint = http://localhost:8400/grouper/mockServices/azure/auth/
grouper.azureConnector.azureTest.resource = https://graph.microsoft.com
grouper.azureConnector.azureTest.resourceEndpoint = http://localhost:8400/grouper/mockServices/azure/
grouper.azureConnector.azureTest.tenantId = 6c4dxxx0d
```

## Use the external system

[Grouper Entra ID (Azure) provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555567/Grouper+Entra+ID+Provisioner+Current+Azure+O365)
