---
title: "Grouper external system mock services"
space: Grouper
pageId: 28547497
version: 15
lastUpdated: 2026-07-01T05:46:38.494Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547497/Grouper+external+system+mock+services
---

The Grouper UI includes an optional mock service handler, to simulate various external systems. This can be used to develop and test provisioning configurations, before pointing it at a live system such as a cloud IAM service. Ideally you would only enable this temporarily in a non-production Grouper environment long enough to create the provisioner with the desired settings, and then switch the provisioner to use an external system that goes against the live service.

The mock service is disabled by default, but is enabled in the UI from grouper.hibernate.properties value `grouper.is.mockServices = true` (see Configuration below). Once enabled, external systems can be created that utilize the Grouper mock endpoints instead of the actual server endpoints. The mock service providers utilize internal Grouper database tables to persist data, in order to effectively mimic authentication, searching, matching, and CRUD operations on IAM groups, entities, and memberships. A provisioner that works with a mock system should reliably work the same as the live provider. But note that the mock systems only implement basic operations, and does not fully implement all the functions of the live services they imitate.

Note that the internal DB tables are not automatically created by a Grouper installation. To force creation of the tables for a specific mock external system, invoke the test function from the UI once the system has been set up, or run a provisioning job using the mock service as its external system.

Depending on the provisioner needs, you may need to manually insert data into one of the mock tables, once they have been initialized. For example, an Azure provisioner with read-only entities needs subject records in the mock_azure_user table, so that it can simulate looking up target entities.

## Property configuration

grouper.hibernate.properties

```
grouper.is.mockServices = true
```

grouper.properties (optional)

```
# If requests and responses should be logged
# {valueType: "boolean", defaultValue: "false"}
grouper.mock.services.logRequestsResponses = true
```

log4j2.additionalLoggers.xml.txt (optional)

```
        <Logger name="edu.internet2.middleware.grouper.j2ee.MockServiceServlet" level="debug" additivity="false">
            __LOGPIPESTART__<AppenderRef ref="logpipe_grouper_daemon"/>__LOGPIPEEND__
            __FILESTART__<AppenderRef ref="file_grouper_daemon"/>__FILEEND__
            __STDERRSTART__<AppenderRef ref="stderr"/>__STDERREND__
        </Logger>
```

## External system configuration

The context for the services is /grouper/mockServices/<serviceName> . Thus, the base endpoints for the mock services will start with this, rather than the actual base in the live system. For example, the Duo service would be [https://localhost:8443/grouper/mockServices/duo](https://localhost:8443/grouper/mockServices/duo). Authentication depends on the particular external system implementation, but is not usually strict. Some services may allow any authentication value, but most services check the authentication against a specific mock authentication property that needs to be added.

## Implemented mock services

| External System  (link to external system page) | base URI | DB tables |
| --- | --- | --- |
| [Adobe](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547372/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Adobe) | /grouper/mockServices/adobe | mock_adobe_auth    mock_adobe_group    mock_adobe_user    mock_adobe_membership |
| [Azure](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549126/Grouper+Entra+ID+Azure+External+System) | /grouper/mockServices/azure | mock_azure_auth    mock_azure_group    mock_azure_user    mock_azure_membership |
| [AWS SCIM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564269/Grouper+provisioning+SCIM+for+AWS) | /grouper/mockServices/awsScim | mock_scim_user    mock_scim_group    mock_scim_membership |
| [Box](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547924/Grouper+Box+External+System) | /grouper/mockServices/box | mock_box_auth    mock_box_group    mock_box_user    mock_box_membership |
| [Duo](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548604/Grouper+Duo+External+System) | /grouper/mockServices/duo | mock_duo_group    mock_duo_user    mock_duo_membership |
| [Duo roles](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548604/Grouper+Duo+External+System) | /grouper/mockServices/duoRole | mock_duo_role_user |
| GitHub SCIM | /grouper/mockServices/githubScim | mock_scim_group    mock_scim_user    mock_scim_membership |
| [Google](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548243/Grouper+Google+external+system) | /grouper/mockServices/google | mock_google_auth    mock_google_group    mock_google_user    mock_google_membership |
| [Okta](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547432/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Okta) | /grouper/mockServices/okta | mock_okta_auth    mock_okta_group    mock_okta_user    mock_okta_membership |
| Remedy | /grouper/mockServices/remedy | mock_remedy_auth    mock_remedy_group    mock_remedy_user    mock_remedy_membership |
| Digital marketplace | /grouper/mockServices/digitalMarketplace | mock_digital_marketplace_auth    mock_digital_marketplace_group    mock_digital_marketplace_user    mock_digital_mp_membership |
| [TeamDynamix](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547992/Grouper+TeamDynamix+External+System) | /grouper/mockServices/teamdynamix | mock_teamdynamix_auth    mock_teamdynamix_group    mock_teamdynamix_user    mock_teamdynamix_membership |

Custom mock services can be added by writing a Java class (extend class edu.internet2.middleware.grouper.j2ee.MockServiceHandler) and configuring:

grouper.properties

```
grouperExtraMockServer.<service>.class = edu.internet2.middleware.grouper.app.MyService.MyServiceMockServiceHandler
grouperExtraMockServer.<service>.path = myService
```

## Instructions for specific external systems

### Adobe

Configure a web service similar to a real Adobe connection, except for the following:  
  
Token URL: http://localhost:8080/grouper/mockServices/adobe/token  
Service URL: http://localhost:8080/grouper/mockServices/adobe  
  
You can use arbitrary values for Client id, client secret, scopes, API key header name, and API key password. If you wish to test the connection to the mock service, also set:  
  
Test URL suffix: /users/0/0  
Test HTTP method: GET  
Test HTTP response code: 200  
Test response body regex: .*"result":"success".*  
  
Once created, you will then need to set grouper.properties `grouperTest.adobe.mock.configId` to the configId for the external system. The mock server will use the secrets from the referenced external system for authentication.

### Azure

Configure an Azure external system with the following:  
  
Login endpoint: http://localhost:8080/grouper/mockServices/azure/auth  
Resource: http://localhost:8080/grouper/mockServices/azure/  
Resource endpoint: http://localhost:8080/grouper/mockServices/azure/  
Graph endpoint: http://localhost:8080/grouper/mockServices/azure/  
  
You can use arbitrary values for Tenant id, Client id, Client secret, and Graph version.

Once created, if your config id differs from the default mock config "myAzure", you will then need to set grouper.properties `grouperTest.azure.mock.configId` to the configId for the external system. The mock server will use the secrets from the referenced external system for authentication.

### awsScim

> The AWS SCIM mock service currently only works when there is exactly one provisioner tied to this bearer token web service external system. The mock server will emulate behavior specific to the provisioner expectations.

Configure a web service external system with the following:  
  
Authentication type: bearerToken  
Endpoint base URL: http://localhost:8080/grouper/mockServices/awsScim/v2/  
Test URL suffix: /Users?count=0  
Test HTTP method: GET  
Test HTTP response code: 200  
Test response body regex: .*totalResults.*  
  
You can use arbitrary values for the secret.

### Box

Configure a Box external system with the following properties:  
  
Box base URL: [http://localhost:8080/grouper/mockServices/box](http://localhost:8080/grouper/mockServices/box)  
Box authentication URL: [http://localhost:8080/grouper/mockServices/box/token/](http://localhost:8080/grouper/mockServices/box/token/)  
Authentication type: JWT  
Private key contents: Use a private PKCS#8 key, or generate one with `openssl genrsa -aes256 -out private_key.pem 1024`. Include the `-----BEGIN ENCRYPTED PRIVATE KEY-----` and `-----END ENCRYPTED PRIVATE KEY-----` lines  
Private key pass: The password for the private key above  
Public key id: Value doesn't seem necessary for connecting, so may be arbitrary. But you can extract the public key with `openssl rsa -in private_key.pem -pubout -out public.pem` and supplying the key password.  
  
You can use arbitrary values for Enterprise id, Client id, and Client secret.  
  
Once created, you will then need to set grouper.properties `grouperTest.box.mock.configId` to the configId for the external system. The mock server will use the secrets from the referenced external system for authentication.

### Duo

Configure a Duo external system with the following properties:  
  
Domain name: localhost:8080/grouper/mockServices/duo  
Use ssl: True or false, depending on whether your Grouper UI is https or not  
  
You can use arbitrary values for Integration key and Secret key.  
  
Once created, if your config id differs from the default mock config "duo1", you will then need to set grouper.properties `grouperTest.duo.mock.configId` to the configId for the external system. The mock server will use the secrets from the referenced external system for authentication.

### Duo Role

Similar to Duo above, but use:  
  
Domain name: localhost:8080/grouper/mockServices/duoRole  
  
The provisioner type to use with this system is "Duo administrators".  
  
Note that both the Duo and the Duo Role mock systems both use the same grouperTest.duo.mock.configId to match the expected authentication values. Thus, if both a Duo and Duo Role system are in use, the integration and secret keys should be the same for both.

### GitHub SCIM

> The GitHub SCIM mock service currently only works when there is exactly one provisioner tied to this bearer token web service external system. The mock server will emulate behavior specific to the provisioner expectations.

Configure a web service external system with the following:  
  
Endpoint base URL: http://localhost:8080/grouper/mockServices/githubScim/v2/organizations/orgName  
  
You can use arbitrary values for the secret.

### Google

Configure a Google external system with the following:  
  
Service account private key: The private key from an RSA keypair. See below to generate on using GSH  
Token api url: http://localhost:8080/grouper/mockServices/google/token/  
Directory api base url: http://localhost:8080/grouper/mockServices/google  
Group settings api base url: http://localhost:8080/grouper/mockServices/google/settings  
  
If you don't want to use your production Google authentication key, you can generate a keypair using this GSH script:

```
def (publicKey, privateKey) = GrouperUtil.generateRsaKeypair(1024)

println "Public key: ${publicKey}"
println "Private key: ${privateKey}"
```

The private key is entered in the "Service account private key" field. The public key is used in grouper.properties property `grouperTest.google.mock.publicKey`, as described below.  
  
You can use arbitrary values for Domain name, Group domain name, Service account email, and Service impersonation user.  
  
Once created, you will then need to set two testing properties,

**grouper.properties:**

- `grouperTest.google.mock.configId` = the configId for the external system. The mock server will use the secrets from the referenced external system for authentication
- `grouperTest.google.mock.publicKey` = the public key corresponding to the private key in the external system

### Okta

> Not working in v4.17.0 to v4.17.6 and v5.14.1 to v5.17.1 (JIRA [GRP-6063](https://todos.internet2.edu/browse/GRP-6063))

Configure a Web service external system with the following properties:  
  
Authentication type: oauthClientCredentials  
Token URL: http://localhost:8080/grouper/mockServices/okta/oauth2/v1/token  
Service URL: http://localhost:8080/grouper/mockServices/okta/api/v1  
Client credential type: publicPrivateKey  
Public key id: Use a real RSA public key, or generate one (See below on using GSH to generate)  
Private key: Use a real RSA public key, or generate one (See below on using GSH to generate)  
Grant type: client_credentials  
Scopes: okta.users.manage okta.groups.manage  
Test URL suffix /groups/abc123xyz456  
Test HTTP method: GET  
Test HTTP response code: 404  
Test response body regex: .*not.*  
  
You can use arbitrary values for Client id.  
  
Also set the following properties:  
  
grouper.properties: `grouperTest.okta.mock.publicKey = <the public key>`

grouper-loader.properties: `grouperTest.okta.mock.configId = <the configId>`

If you don't want to use your production Okta authentication key, you can generate a keypair using this GSH script (note: size needs to be 2048):

```
def (publicKey, privateKey) = GrouperUtil.generateRsaKeypair(2048)

println "Public key: ${publicKey}"
println "Private key: ${privateKey}"
```

### Remedy

Configure a Remedy external system with the following properties:  
  
URL: http://localhost:8080/grouper/mockServices/remedy/  
Token URL: http://localhost:8080/grouper/mockServices/remedy/token/  
  
You can use arbitrary values for username and password.  
  
Once created, if your config id differs from the default mock config "myRemedy", you will then need to set grouper.properties `grouperTest.remedy.mock.configId` to the configId for the external system. The mock server will use the secrets from the referenced external system for authentication.

### Digital Marketplace

Configure a Remedy digital marketplace external system with the following properties:  
  
URL: http://localhost:8080/grouper/mockServices/digitalMarketplace/  
Token URL: http://localhost:8080/grouper/mockServices/digitalMarketplace/token/  
  
You can use arbitrary values for username, password, and X-Requested-By header value.  
  
Once created, if your config id differs from the default mock config "myDigitalMarketplace", you will then need to set grouper.properties `grouperTest.digitalMarketplace.mock.configId` to the configId for the external system. The mock server will use the secrets from the referenced external system for authentication.

### Teamdynamix

Configure a TeamDynamix external system with the following properties:  
  
URL: http://localhost:8080/grouper/mockServices/teamdynamix/  
  
You can use arbitrary values for Beid and Web services key.
