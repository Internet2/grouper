---
title: "OIDC authentication to Grouper UI"
space: Grouper
pageId: 28548296
version: 12
lastUpdated: 2026-07-01T05:44:53.620Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548296/OIDC+authentication+to+Grouper+UI
---

This is for Grouper 2.6.16+

If you have an existing OIDC IDP you can connect the Grouper UI to it.

Shibboleth has OIDC that requires some setup if you are not already using it.

# Documentation for end users

Note you need this set correctly in grouper.properties (or in config in database) if the redirect URI is not specified

```
grouper.ui.url = https://whatevergrouper.school.edu/grouper/
```

Set these environment variables in the container:

GROUPER_UI_GROUPER_AUTH = false

GROUPER_RUN_SHIB_SP = false

Set up OIDC external system like below.

Redirect URL is your grouper URL with this ending: /grouperUi/app/UiV2Main.oidc, e.g. https://groupservice.institution.edu/grouper/grouperUi/app/UiV2Main.oidc

In your grouper.hibernate config, make sure [grouper.is](http://grouper.is).ui.basicAuthn is set to false.

After configuring the external system and setting the basicAuthn property to false, restart your server. Go to the grouper UI URL, it should redirect you to the OIDC login page. After you enter your credentials, you will be redirected back to grouper UI.

When you add the appropriate configuration to your OIDC OP to handle the Grouper RP, the OIDC client will be using `client_secret_basic` .

In 4.13.1+ and 5.10.2+, the source for claims (to be used as the subject id) can come from either the userinfo endpoint or the id token.

- userinfo endpoint - The authorization code will be sent to the token endpoint. An access token will be received from there and sent to the userinfo endpoint. The json response from there should include the configured subject id claim.
- id token - The authorization code will be sent to the token endpoint. An id token will be received from there. The payload in the id token should include the configured subject id claim.

# Internal documentation for developers

Run OIDC container

```
docker run --rm -d -p 9000:9000 --mount type=bind,src=/Users/vsachdeva/Downloads/oidc_users/users.json,dst=/tmp/users.json  -e "REDIRECTS=http://localhost:8080/grouper/grouperUi/app/UiV2Main.oidc" -e "USERS_FILE=/tmp/users.json"  qlik/simple-oidc-provider
```

users.json file can be found at grouper/misc/oidc_container_users.json.

Set the following OIDC external system properties

```
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.authorizeUri").value("http://localhost:9000/auth").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.clientId").value("foo").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.clientSecret").value("bar").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.tokenEndpointUri").value("http://localhost:9000/token").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.userInfoUri").value("http://localhost:9000/me").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.configurationMetadataUri").value("http://localhost:9000/.well-known/openid-configuration").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.useForUi").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.useConfigurationMetadata").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.scope").value("openid email profile").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.oidcResponseType").value("code").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.subjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.subjectIdClaimName").value("sub").store();
    new    GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.redirectUri").value("http://localhost:8080/grouper/grouperUi/app/UiV2Main.oidc").store();
```

In grouper.hibernate properties file, set [grouper.is](http://grouper.is).ui.basicAuthn to false.

After you start your grouper UI, go to the firefox browser only to test. Chrome doesn't work.

You will be presented with a username and password fields. Username is the email from users.json file and password is also in users.json file.

## Test the OIDC

You cannot use a code twice, so if there is only one real redirect URL (you dont have a fake one configured),

1. Click on the Grouper URL
2. While logging in, temporarily set your /etc/hosts (or windows equiv) to something like 127.0.0.1
3. The URL will not resolve, and you can grab the code (carefully) from the URL
4. You can run GSH to see if things work
  
  
  ```
  import edu.internet2.middleware.grouper.authentication.GrouperOidc;
  import edu.internet2.middleware.grouper.subj.SubjectHelper;
  import edu.internet2.middleware.subject.Subject;
  
  GrouperOidc grouperOidc = new GrouperOidc();
      
  // hard code the config id if not compile
  grouperOidc.assignExternalSystemConfigId(GrouperOidc.externalSystemConfigIdForUi());
      
  grouperOidc.assignAuthorizationCode("AAdzZWNyZXJ6EHr_rvg");
      
  grouperOidc.retrieveAndParseTokens();
      
  Subject subject = grouperOidc.findSubject();
      
  System.out.println(SubjectHelper.getPretty(subject));
  
  
  ```
5. Change your /etc/hosts back
