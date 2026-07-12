---
title: "Grouper Box External System"
space: Grouper
pageId: 28547924
version: 6
lastUpdated: 2026-07-12T15:26:49.494Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547924/Grouper+Box+External+System
---

## Configure credentials

1. Sign up with Box: [https://account.box.com/signup/personal?tc=annual](https://account.box.com/signup/personal?tc=annual)
2. Enable 2-step verification
3. Go to the dev console: [https://app.box.com/developers/console](https://app.box.com/developers/console)
4. Click on Create New App and select Custom App
5. Select Server Authentication (With JWT) for the authentication method and enter an App Name.
6. Click the Create button, which will redirect you to the Configuration screen of the new app.
7. From this screen, copy the Client Id, Client Secret, and Enterprise Id
8. On the same screen, change the App access level to App + Enterprise Access
9. Under Add and Manage Public Keys, click on Generate a Public/Private keypair. After 2 step verification, it will prompt you to download a json file. Save it on your computer. It has all the necessary information to connect with Box.
10. Click on the Save Changes button in the top right corner.
11. Go to the Authorization tab for this app and click on the Review and Submit button.
12. You will receive an email to approve this app. Go to your inbox and approve the app.
13. Now, you're all set to set up an external system and provisioner within Grouper.

## 

Test config

```
grouperClient.boxConnector.localBox.authenticationType = JWT
grouperClient.boxConnector.localBox.authenticationUrl = http://localhost:8080/grouper/mockServices/box/token
grouperClient.boxConnector.localBox.baseUrl = http://localhost:8080/grouper/mockServices/box
grouperClient.boxConnector.localBox.clientId = y5x93fo8m1uwxizeic9b88xvkkol1alm
grouperClient.boxConnector.localBox.clientSecret = *******
grouperClient.boxConnector.localBox.enterpriseId = 964565703
grouperClient.boxConnector.localBox.privateKeyContents_0 = *******
grouperClient.boxConnector.localBox.privateKeyPass = *******
grouperClient.boxConnector.localBox.publicKeyId = z30urhgy
```

## Use the external system

[Grouper Box provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555350/Grouper+Box+Provisioner)
