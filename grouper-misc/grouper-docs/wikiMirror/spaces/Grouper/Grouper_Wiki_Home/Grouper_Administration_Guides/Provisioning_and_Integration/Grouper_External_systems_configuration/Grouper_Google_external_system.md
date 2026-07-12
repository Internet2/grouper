---
title: "Grouper Google external system"
space: Grouper
pageId: 28548243
version: 16
lastUpdated: 2026-07-01T05:44:58.822Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548243/Grouper+Google+external+system
---

The Google external system connects to your Google Cloud account, using your domain and a service account. In order to managed provisioned groups and/or users, the following permission scopes need to be added to the service account:

- https://www.googleapis.com/auth/admin.directory.user
- https://www.googleapis.com/auth/admin.directory.group
- https://www.googleapis.com/auth/admin.directory.group.member
- https://www.googleapis.com/auth/apps.groups.settings

## Configure external system

Set the following fields in the UI when adding and configuring the Google provisioner:

- Config id: A unique alphanumeric key for this external system. This is what will appear in the provisioner drop down for the external system
- Domain name: Your Google organization name
- Group domain name: Optional, if your groups are in a subdomain or different domain, set this to that domain for provisioned groups
- Service account email: Email for the service account that can connect, and which has the proper permission scopes added in Google
- Service account PKCS12 file path: If you chose to generate your service account's authentication key in .p12 format, this is the full p12 file. This will need to be built into your image, or mounted in your container at runtime. The path will be the full path to this file in the container
- Service account private key: If you chose to generate your authentication key in JSON format, the private key will be in the "private key" field in this file. Extract the value, and convert "\n" values to carriage returned. The result to enter should like like the following:

> -----BEGIN PRIVATE KEY-----  
> MIIEvQIBTRiNA0twW45qJhtzxNp3ndHI50ySkuiPxooNrcrRZ1PxneMEZIAqIhoS  
> BqroyUW42kPKLVs5xLd3gobtB0qaM7Hakx5CkVjkiKc576NzKURvcAg3jjktToqL  
> ...  
> 9BFo9NQldVF5HkMmUtoHSoe=  
> -----END PRIVATE KEY-----

- Service impersonation user: The email of the principal to be used as the creator and modifier of objects, and for auditing

Other optional fields, which can generally be left as the defaults:

- Token api url: Default value is 'https://oauth2.googleapis.com/token'.
- Directory api base url: Default value is 'https://admin.googleapis.com/admin/directory/v1'.
- Group settings api base url: Default value is 'https://www.googleapis.com/groups/v1/groups'.
- Page size for groups: Page size for batched groups fetches. Default value is '200'.
- Page size for users: page size for batched user fetches. Default value is '500'.
- Page size for memberships: page size for batched membership fetches. Default value is '200'.
- Proxy URL: If firewall rules prevents direct internet access from Grouper, set this to
- Proxy type: HTTP or SOCKS5

using the private key instead of the PKCS12 file path is an easier option, since it can be modified without needing to manage a file in the container. The value is treated as a password, so it can't be viewed in the UI, and can't be exported. If you have a P12 key and want to extract the private key as text, you can run the following command:

```
openssl pkcs12 -in yourfile.p12 -out yourfile.pem -nodes
```

The password for the p12 file from Google is generally "notasecret".

## Test the external system

Once created, the external system "Test" action will not only attempt to make an HTTP connection to the Google APi endpoint, but will also authenticate, and then make an attempt to query group "testFakeGroupId". This group will likely not exist, but a Not Found response will still be considered successful. The test will only fail if there is an unexpected connection error while making the query.

## Use the external system

[Grouper google provisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554610/Grouper+Google+GCP+provisioner)

## Instructions on setting up a new account

### Video showing google external system

[Video](https://www.youtube.com/watch?v=c5gC09Zug14)

### Configure credential

- Sign up for GCP
- Go to IAM & Admin → Groups
- It will say "This feature requires an organization" and at the bottom of the screen, click the button "GO TO THE CHECKLIST"
- Follow the instructions to set up cloud identity, verify your domain.
- In the project, allow admin SDK by selecting the project from the drop down. Click on APIs and Services. Click on Enable APIs and Services. Search for and select Admin SDK API. Click Enable.
- Go back to [https://console.cloud.google.com/](https://console.cloud.google.com/) IAM & Admin.
- Under IAM → Permissions, add a new Principal with role Owner.
- On the left, click on Service Accounts. Create a new service account with role owner. Under the newly created service account, create a new key.
- Domain-wide Delegation. 
  
  - Go to: [http://admin.google.com/](http://admin.google.com/).
  - Security → Access and Data Control → API controls.
  - Click: Manage Domain Wide Delegation
  - Add new
  - Use the client ID from the Service Account Detail "Unique ID"
  - Use these scopes
    
    
    ```
    https://www.googleapis.com/auth/admin.directory.user, https://www.googleapis.com/auth/admin.directory.group, https://www.googleapis.com/auth/admin.directory.group.member, https://www.googleapis.com/auth/apps.groups.settings
    ```
- The user "impersonated as" (main admin user) needs to be an admin
- Enable the group settings API for your project:
  
  - [https://console.cloud.google.com/](https://console.cloud.google.com/)
  - APIs and Services → Enabled APIs and Services
  - Click: Enable APIs and Services
  - Search for Group Settings API, click on it
  - Click: Enable
