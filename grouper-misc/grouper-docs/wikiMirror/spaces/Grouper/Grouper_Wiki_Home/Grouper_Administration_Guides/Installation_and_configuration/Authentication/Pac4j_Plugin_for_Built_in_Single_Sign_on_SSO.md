---
title: "Pac4j Plugin for Built-in Single Sign-on (SSO)"
space: Grouper
pageId: 28549858
version: 28
lastUpdated: 2026-07-19T00:32:42.669Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549858/Pac4j+Plugin+for+Built-in+Single+Sign-on+SSO
---

Grouper supports the pac4j plugin library (since Grouper v2.6.10+), which provides single sign-on (SSO) capabilities within Grouper. Since it runs in the same Java process, there is no need for an external Apache or Shibboleth SP service. Since Grouper v5, there is only a single Grouper process and Apache and the Shibboleth SP are not bundled in the image, so this is a useful option for providing SSO. Pac4j can be configured for OIDC and CAS authentication as well as SAML, so it may also be an attractive option even in v4.

In addition to the pac4j solution, the other alternatives for SSO in V5+ are mainly (a) Use [OIDC authentication](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548296/OIDC+authentication+to+Grouper+UI) built into Grouper; or (b) use a "sidecar" container as the front end to the UI, and proxy requests to the backend UI container while adding the authenticated username in an HTTP header.

# Setup

For a fully integrated sample configuration, see the docker-compose setup in the [`src/test/docker` folder](https://github.com/Internet2/grouper/tree/GROUPER_4_BRANCH/grouper-misc/grouper-ext-auth/src/test/docker)of the Git repository. The environment includes sample Grouper configurations for SAML2, OIDC, or CAS, along with a Shibboleth IDP that can authenticate Grouper using any of these methods.

## Adding the plugin jar to the image (versions prior to v6.1.0)

> In Grouper v5.21.0 through v6.0.1, The grouper-authentication-plugin.jar supplied with Grouper would work for containers using it (e.g. UI), but containers not using it (e.g. WS, daemon, gsh) would be unable to start. If you were mounting an older version of the jar into your image (which most users were doing), this avoided the issue ([GRP-6624](https://grouper.atlassian.net/browse/GRP-6624)). The problem was fixed in v6.1.0 and v7. Starting with those versions, you can use the jar supplied with the Grouper image and no longer need to add a local file.

Starting in v6.1.0 and v7, the grouper-authentication-plugin.jar library is included in the Grouper image, at location /opt/grouper/plugins/grouper-authentication-plugin.jar. Prior to those versions, you need to download it separately and add it to your institutional image. A working version can be downloaded from [https://github.internet2.edu/internet2/grouper-ext-auth/releases](https://github.internet2.edu/internet2/grouper-ext-auth/releases)). The downloaded file can have the version number in the file name, but it is recommended to leave it out of the name when copied into the container. This way, the configuration does not need to be changed every time the version changes. For example:

```
COPY grouper-authentication-plugin-1.0.0.jar /opt/grouper/plugins/grouper-authentication-plugin.jar
```

The default location for plugin jars is /opt/grouper/plugins, but you can can use an alternate location and configure Grouper to use that directory.

## Enabling the plugin in grouper.properties

In grouper.properties, add the following:

```
grouper.osgi.enable = true
grouper.osgi.jar.dir = /opt/grouper/plugins
grouper.osgi.framework.boot.delegation=org.osgi.*,javax.*,org.apache.commons.logging,edu.internet2.middleware.grouperClient.*,edu.internet2.middleware.grouper.*,org.w3c.*,org.xml.*,sun.*

grouperOsgiPlugin.0.jarName = grouper-authentication-plugin.jar
```

If you are on Grouper v6.1.0+, the jar directory and jar name already match the file supplied in the Grouper image, so you can add this verbatim. If you are on an older Grouper version and manually added the plugin jar, the `grouper.osgi.jar.dir` property should point to the directory you copied the file to in your image build file. Property `grouperOsgiPlugin.0.jarName` is the name of the file you copied in, inside of the OSGI plugin directory.

## Configuring the UI or WS for authentication

In grouper-ui.properties (and/or grouper-ws.properties for WS), add properties appropriate for desired authentication. Note that only one authentication type can be used for each container type (UI vs. WS), but UI and WS could each have its own distinct authentication configuration.

If you do not already have a grouper.properties and/or grouper-ui.properties because you are storing configuration in the database, it is recommended to create files specifically for the pac4 configuration, so that you can fix any login issues offline, in case you are locked out of the UI. At least grouper.is.extAuth.enabled should be outside of the database, so that it can be switched to false for emergency troubleshooting. Other values can be stored in the database once they are correct and stable.

Most of the configuration for the underlying authentication library is exposed to the Grouper configuration. Any field in the Java classes can be directly set using the field name or a setter used by using a related property (setting `attribute=value` will call `setAttribute(value)` )

### SAML2

For SAML2, for example:

```
grouper.is.extAuth.enabled = true
external.authentication.grouperContextUrl = https://grouper-ui.unicon.local/grouper

external.authentication.provider = saml
external.authentication.saml.identityProviderEntityId = https://idp-host-name/idp/shibboleth
external.authentication.saml.serviceProviderEntityId = https://sp-host-name/grouper
external.authentication.saml.serviceProviderMetadataPath = file:/opt/grouper/sp-metadata.xml
external.authentication.saml.identityProviderMetadataPath = file:/opt/grouper/idp-metadata.xml
external.authentication.saml.keystorePath = file:/opt/grouper/sp-keystore.p12
#external.authentication.saml.keyStoreType = PKCS12
## You may want to store the keystorePassword and privateKeyPassword in database config, to keep it out of version control
external.authentication.saml.keystorePassword = testme
external.authentication.saml.privateKeyPassword = testme
external.authentication.saml.attributeAsId = urn:oid:0.9.2342.19200300.100.1.1

#external.authentication.exclusions = /status
```

The three Path properties above (serviceProviderMetadataPath, identityProviderMetadataPath, and keystorePath) can handle various urls:

- the *resource:* or the *classpath:* prefixes refer to a classpath
- the *http:* or the *https:* prefixes refer to a web url
- the *file:* prefix or no prefix at all refer to a local filesystem file

The serviceProviderMetadataPath is optional, and pac4j will generate a new file at that location if it does not exist. If there is an existing SP metadata definition, it will use the HTTP-POST ACS url as the callback endpoint after login. However, the recommended callback URL to use is `<grouperContextUrl>/callback` for proper functionality. If pac4j generates the SP xml file, it will use `<grouperContextUrl>/callback?client_name=client` for the ACS url. The ACS url needs to be registered with your IDP for login to succeed.

Pac4j uses a keystore instead of separate PEM files for the SP key and certificate. Either a JKS or PKCS12 file type can be used. The `keyStoreType` configuration property is optional, as pac4j should be able to determine the file format without this. If you want to keep the keystore itself out of version control, there are strategies for doing this, like converting it to a base64 environment variable, and converting back with a container startup hook (see below for a sample grouperScriptHooks.sh example).

The `keystorePassword` and `privateKeyPassword` refer to the passphrase used when setting up the keystore. For security, you might want to store these two properties in the database instead of defining them here.

The `attributeAsId` value is optional, and refers to the OID of a response attribute to use for the username, if it is not in the nameId field.

Property `external.authentication.exclusions` is optional, and represents the URI’s (comma-separated) that will be allowed without authentication. The default is /status (i.e., you don't need to set this), so that the health check endpoint can be accessed by external monitoring systems. To disable this exclusion, set the value to blank.

For more information and more options, see [https://www.pac4j.org/5.7.x/docs/clients/saml.html](https://www.pac4j.org/5.7.x/docs/clients/saml.html) and [https://github.com/pac4j/pac4j/blob/5.7.x/pac4j-saml/src/main/java/org/pac4j/saml/config/SAML2Configuration.java](https://github.com/pac4j/pac4j/blob/5.7.x/pac4j-saml/src/main/java/org/pac4j/saml/config/SAML2Configuration.java). For example, requests are normally not signed, but you can set `external.authentication.saml.authnRequestSigned=true` to enable it.

See the section below for specific instructions on migrating from a Shibboleth SP to pac4j.

### OIDC

For OIDC, for example:

```
grouper.is.extAuth.enabled = true
external.authentication.grouperContextUrl = https://grouper-ui.unicon.local/grouper

external.authentication.provider = oidc
external.authentication.oidc.clientId = *****
external.authentication.oidc.discoveryURI = https://idp-host-name/.well-known/openid-configuration
external.authentication.oidc.secret = *****
external.authentication.oidc.claimAsUsername = preferred_username
 
```

For more information and more options, see [https://www.pac4j.org/5.7.x/docs/clients/openid-connect.html](https://www.pac4j.org/5.7.x/docs/clients/openid-connect.html) and [https://github.com/pac4j/pac4j/blob/5.7.x/pac4j-oidc/src/main/java/org/pac4j/oidc/config/OidcConfiguration.java](https://github.com/pac4j/pac4j/blob/5.7.x/pac4j-oidc/src/main/java/org/pac4j/oidc/config/OidcConfiguration.java)

### CAS

For CAS, for example:

```
grouper.is.extAuth.enabled = true
external.authentication.grouperContextUrl = https://grouper-ui.unicon.local/grouper

# Note for CAS: you'll need to make sure that the CAS server SSL certificate is available in the trust store
external.authentication.provider = cas
external.authentication.cas.prefixUrl = https://idp-host-name/idp/profile/cas
external.authentication.cas.protocol = CAS20
```

For more information and more options, see [https://www.pac4j.org/5.7.x/docs/clients/cas.html](https://www.pac4j.org/5.7.x/docs/clients/cas.html) and [https://github.com/pac4j/pac4j/blob/5.7.x/pac4j-cas/src/main/java/org/pac4j/cas/config/CasConfiguration.java](https://github.com/pac4j/pac4j/blob/5.7.x/pac4j-cas/src/main/java/org/pac4j/cas/config/CasConfiguration.java)

## Environment variables

If you are in V4, you may be using Pac4j to prepare for V5 and a single Tomcat process. To get to that environment you will want to set:

- GROUPER_RUN_TOMCAT_NOT_SUPERVISOR=true
- GROUPER_TOMCAT_HTTP_PORT=8080 (this is the default; set to -1 if only using the SSL port)
- GROUPER_TOMCAT_HTTPS_PORT=8443 (the default is -1; set to a value if using SSL and not offloading it to a load balancer)
- GROUPER_RUN_SHIB_SP=false (this is the default if not set for other reasons)

Other general settings for either V4 or V5

- GROUPER_UI_GROUPER_AUTH=false (if pac4j is running, authentication may never fall through to local auth, but this ensures it)

## Logging

Since the plugin uses an isolated Java environment, it doesn't utilize the same log4j2 logging as the rest of Grouper. Instead, it uses SimpleLogger for SLF4J. To set up custom log levels for Pac4j classes, there are two ways to set this up:

1) Set environment variable, e.g.: GROUPER_EXTRA_CATALINA_OPTS="-[Dorg.slf4j.simpleLogger.log.org](http://Dorg.slf4j.simpleLogger.log.org).pac4j=debug"

2) Allegedly, creating a file in the WEB-INF/classes folder called simplelogger.properties also works, but this hasn't been tried. Syntax of the properties in this file will be like "org.slf4j.simpleLogger.log.a.b.c = debug"

Some packages you may want to set for debugging are:

- org.pac4j: The entire set of pac4j operations, for full debugging
- org.opensaml.xmlsec.signature and org.pac4j.saml.profile.impl: logs signature validation
- org.opensaml.security.x509.impl and org.opensaml.security.trust.impl: certificate operations

## Converting a Grouper image from Shibboleth SP to pac4j configuration

The following tips describe the basic steps needed to move from a Shibboleth SP running inside a Grouper container to a pac4j SAML configuration.

1) (if version < v6.1.0) Include the pac4j jar file into your image (or mount it at runtime)

Download the jar, then copy into the image via the Dockerfile or mount into a running container, as described above.

2) Convert the SP cert and key PEM files to a keystore

Pac4j uses a keystore to read certificates instead of PEM files. The locations of the key and certificate files are defined in your /etc/shibboleth/shibboleth2.xml file, in the `<CredentialResolver>` section. use the following command to convert these into a PKCS12 keystore, renaming filenames as needed. The command will ask for a password, which will need to go into the configuration in the `keystorePassword` and `privateKeyPassword` properties.

`openssl pkcs12 -export -out sp-keystore.p12 -inkey sp-key.pem -in sp-cert.pem`If there is also a CA certificate chain to include, the `-certfile ca-cert.pem` option can be added.

3) Extract other properties

Other files and properties needed for pac4j can be extracted from shibboleth2.xml, or from the currently running Shibboleth SP:

- identityProviderEntityId: From shibboleth2.xml, `<SSO entityID="YOUR_IDP_ENTITYID" ...>`
- serviceProviderEntityId: From shibboleth2.xml, `<ApplicationDefaults entityID="YOUR_SP_ENTITYID" ...>`
- serviceProviderMetadataPath; The location of the SP metadata, which will be generated by pac4j if the file is missing. If pac4j generates the file, it will use `<grouperContextUrl>/callback?client_name=client` as the ACS callback endpoint. If you use your own existing SP metadata (from existing SP or IDP metadata files, or the deprecated /shibboleth.SSO/Metadata endpoint), you can set your own ACS url, but `<grouperContextUrl>/callback` (with or without extra query parameters) is the only one to reliably work.
- identityProviderMetadataPath: From shibboleth2.xml, `<MetadataProvider>` node. This could be either a URL or a file.
- attributeAsId (optional): If you are not using a nameId for the username and instead getting it from an attribute, this is the OID for it. The attribute you are currently using will be in shibboleth2.xml, likely the first item in the `ApplicationDefaults REMOTE_USER="..."` list. The OID for it is in its entry in attribute-map.xml.

4) Change the ACS endpoint

The callback endpoint after login will no longer be `/Shibboleth.sso/SAML2/POST`. The correct one for pac4j will be `<grouperContextUrl>/callback?client_name=client` (default), or a custom one if you have it defined in your SP metadata. This will need to be changed (or added) in the `<AssertionConsumerService>` SAML:2.0:bindings:HTTP-POST entry in the IDP metadata.

5) Add files to the Docker image, and update grouper.properties and grouper-ui.properties.

The keystore and metadata files need to be added to the Docker image, or mounted at runtime. Pac4j configuration is to be added to the appropriate Grouper configuration files residing in /opt/grouper/grouperWebapp/WEB-INF/classes.

If you have no easy way to mount files at runtime, you can convert the keystore to base64, inject it as a secret or environment variable at runtime, and convert it back with a startup hook. The example script is one way to do that:

```
#!/bin/sh

# called after the setupFiles functions is called, almost before the process starts
grouperScriptHooks_setupFilesPostChown() {

  echo "INSTITUTIONAL_GROUPER_CONTAINER; INFO: (grouperScriptHooks.sh-body) running grouperScriptHooks_setupFilesPostChown"

  # For security, don't store the pac4j keystore in a version control vile. Mount it at runtime in the UI container
  if [ -n "$PAC4J_KEYSTORE" ]; then
    echo "INSTITUTIONAL_GROUPER_CONTAINER; INFO: (grouperScriptHooks.sh-body) PAC4J_KEYSTORE is set; creating /opt/grouper/pac4j/keys/sp-keystore.p12"
    echo "$PAC4J_KEYSTORE" | base64 -d > /opt/grouper/pac4j/keys/sp-keystore.p12
    md5sum /opt/grouper/pac4j/keys/sp-keystore.p12
    ls -al /opt/grouper/pac4j/keys/sp-keystore.p12
  fi
}
```

6) UI container environment variables

- GROUPER_RUN_TOMCAT_NOT_SUPERVISOR=true
- GROUPER_TOMCAT_HTTP_PORT=8080 (this is the default; set to -1 if only using the SSL port)
- GROUPER_TOMCAT_HTTPS_PORT=8443 (the default is -1; set to a value if using SSL and not offloading it to a load balancer)
- GROUPER_RUN_SHIB_SP=false (this is the default if not set for other reasons)

## AWS Considerations

### Load balancer health check port after moving off of Apache

If you are migrating from an image that used Shibboleth, you also likely had Apache running on ports 80 and 443. You might have had your AWS target groups defined to listen on either 80 or 443, and maybe also your health checks on those ports. After moving to V5+, Apache will no longer be running on 80 and 443, so you will have problems starting up Grouper with health checks failing. The two options are:

1. Set GROUPER_TOMCAT_HTTP_PORT=80 or GROUPER_TOMCAT_HTTPS_PORT=443, so the monitor on those ports continue to work
2. Recreate the target group to listen on 8080 instead of 80 (there is no way to edit the existing definition, so a new one needs to be created). Set your ALB and ECS to use the new target group.

### WAF (web application firewall) restrictions

When using an AWS load balancer for the Grouper UI, the AWS default WAF rejects logins because the request body is too large, and it triggers the SizeRestrictions_BODY block. Instead of using the default rule, customize it and turn off specific checks.

1) Remove AWS-AWSManagedRulesCommonRuleSet

2) Add custom rule "CustomFromAWSCommonRuleSet-AllowRequestBody"

The type of rule will be a "Managed rule group". You need to override SizeRestrictions_BODY (change it to override to Count). Although unrelated to Pac4j, the default rule CrossSiteScripting_BODY breaks the Configuration history feature in Grouper (the rule disallows form data with "filter=" even though it's not cross-site). So you might as well add this override too (set override to Count). You can edit these in the Details tab, or you can edit it as JSON and paste as below:

```
{
  "Name": "CustomFromAWSCommonRuleSet-AllowRequestBody",
  "Priority": 2,
  "Statement": {
    "ManagedRuleGroupStatement": {
      "VendorName": "AWS",
      "Name": "AWSManagedRulesCommonRuleSet",
      "RuleActionOverrides": [
        {
          "Name": "SizeRestrictions_BODY",
          "ActionToUse": {
            "Count": {}
          }
        },
        {
          "Name": "CrossSiteScripting_BODY",
          "ActionToUse": {
            "Count": {}
          }
        }
      ]
    }
  },
  "OverrideAction": {
    "None": {}
  },
  "VisibilityConfig": {
    "SampledRequestsEnabled": true,
    "CloudWatchMetricsEnabled": true,
    "MetricName": "CustomFromAWSCommonRuleSet-AllowRequestBody"
  }

```

With that customization, a suitable set of WAF rules could be the following. Note that AWS-AWSManagedRulesAdminProtectionRuleSet is not included, as it interferes with Grouper's /UiV2Admin/ pages.

| Name | Priority |
| --- | --- |
| AWS-AWSManagedRulesAmazonIpReputationList | 0 |
| AWS-AWSManagedRulesAnonymousIpList | 1 |
| CustomFromAWSCommonRuleSet-AllowRequestBody | 2 |
| AWS-AWSManagedRulesLinuxRuleSet | 3 |
| AWS-AWSManagedRulesKnownBadInputsRuleSet | 4 |

## ADFS Considerations

You must set Maximum authentication time to successfully authenticate using Microsoft ADFS 2.0/3.0.

pac4j has the default maximum time set to 1 hour while ADFS has it set to 8 hours. Therefore it can happen that ADFS sends an assertion which is still valid on ADFS side but evaluated as invalid on the pac4j side.  
If misaligned, you can see the following error message: org.pac4j.saml.exceptions.SAMLException: Authentication issue instant is too old or in the future  
There are two possibilities how to make the values equal:

- change the value in ADFS management console in the trust properties dialog
- change the value on pac4j side using the setMaximumAuthenticationLifetime method.
