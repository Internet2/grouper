---
title: "Grouper TeamDynamix External System"
space: Grouper
pageId: 28547992
version: 9
lastUpdated: 2026-11-20T21:04:16.848Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547992/Grouper+TeamDynamix+External+System
---

## Create TeamDynamix Admin Service Account

TeamDynamix documents the process for creating an admin API service account at [https://solutions.teamdynamix.com/TDClient/1965/Portal/KB/ArticleDet?ID=132442](https://solutions.teamdynamix.com/TDClient/1965/Portal/KB/ArticleDet?ID=132442) (see "Admin Service Accounts" section).

Login to your TeamDynamix admin site with a user that has admin rights.

Navigate to the "Security" tab

On the security tab make note of the BEID and select "Add Admin Service Account".

Enter the first and last name of the service account

Once saved, TeamDynamix will generate a web services key

Use the BEID and webservice key when creating the external connection below.

## Configure credentials

You will need to get the BEID and webservice key from your institution's TeamDynamix administrator.

Test config

```
grouper.teamDynamix.teamdx.beid = your_beid
grouper.teamDynamix.teamdx.url = https://nusupport.univ.edu/SBTDWebApi/
grouper.teamDynamix.teamdx.webServicesKey = your_webservice_key
```

The web services key is a credential. As of v7.9.1, v6.5.1 and v4.26.1 it is treated as a password: the external system screen stores it encrypted and the config screens mask it. Save it on the external system screen, or keep it in a password file. A value saved in clear in an earlier version stays in clear until it is re-saved; re-save it and rotate it in TeamDynamix.

## Use the external system

[Grouper TeamDynamix provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555382/Grouper+TeamDynamix+Provisioner)

## Grouper development team testing

Set this in grouper.hibernate.properties (or set env var: GROUPER_MOCK_SERVICES=true)

```
grouper.is.mockServices = true
```

URL for the local external system is [http://localhost:8080/grouper/mockServices/teamdynamix/](http://localhost:8080/grouper/mockServices/teamdynamix/)
