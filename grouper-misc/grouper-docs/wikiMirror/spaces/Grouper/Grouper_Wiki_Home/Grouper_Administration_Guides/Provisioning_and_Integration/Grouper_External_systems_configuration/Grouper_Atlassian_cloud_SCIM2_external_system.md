---
title: "Grouper Atlassian cloud SCIM2 external system"
space: Grouper
pageId: 28547461
version: 4
lastUpdated: 2026-07-01T05:46:43.556Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547461/Grouper+Atlassian+cloud+SCIM2+external+system
---

## Video

[Video](https://youtu.be/u4_2tdX_2-g)

## Create a credential in Atlassian cloud

1. Go to [https://admin.atlassian.com/](https://admin.atlassian.com/)
2. Select your organization
3. Security → Identity providers → Add identity provider
4. Enter the name and get the URL and bearer token (API key)
5. To generate a new bearer token
  
  1. Select the Identity provider
  2. User provisioning (three dots)
  3. Regenerate API key

## Add the external system in Grouper

1. Add WS Bearer token external system
2. Enter the URL and API key from Atlassian
3. Test with:
  
  1. Test URL suffix: Groups
  2. Test HTTP method: GET
  3. Test HTTP response code: 200
  4. Test response body regex:
    
    
    ```
    .*totalResults.*
    ```

## Provisioner example

[Atlassian provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28565516/Grouper+provisioning+SCIM+for+Atlassian+example+with+local+entities)
