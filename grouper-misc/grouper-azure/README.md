# office365-and-azure-ad-grouper-provisioner
This project is an Internet2 Grouper connector (full sync and changelog consumer) that synchronizes Grouper groups and users to Microsoft Azure Active Directory/Office 365.

Note that this currently only supports security groups. Support for other group types is planned.

# Running

1. build

    ```
    ./gradlew clean distZip
    ```

1. copy contents of file to grouper home

    ```
    unzip build/distributions/office-365-azure-ad-grouper-provisioner-1.0.0.zip -d /tmp
    cp /tmp/office-365-azure-ad-grouper-provisioner-1.0.0/*.jar /opt/grouper.apiBinary-2.3.0/lib/custom
    ```

1. Set up stem for provisioning and ID attribute

    ```
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef provisioningMarkerAttributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("etc:attribute:office365:o365SyncDef").assignToStem(true).assignToGroup(true).save();
    AttributeDefName provisioningMarkerAttributeName = new AttributeDefNameSave(grouperSession, provisioningMarkerAttributeDef).assignName("etc:attribute:office365:o365Sync").save();

    rootStem = addStem("", "test", "test");
    rootStem.getAttributeDelegate().assignAttribute(provisioningMarkerAttributeName);

    AttributeDef o365Id = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("etc:attribute:office365:o365IdDef").assignToGroup(true).assignValueType(AttributeDefValueType.string).save();
    AttributeDefName o365IdName = new AttributeDefNameSave(grouperSession, o365Id).assignName("etc:attribute:office365:o365Id").save();
    ```

2. Configure loader job in `grouper-loader.properties`. Note that you will need to set up an application with access to your domain.
See documentation at [http://graph.microsoft.io/en-us/docs].

    ```
    changeLog.consumer.o365.class = edu.internet2.middleware.grouper.changeLog.consumer.Office365ChangeLogConsumer
    # fire every 5 seconds
    changeLog.consumer.o365.quartzCron =  0,5,10,15,20,25,30,35,40,45,50,55 * * * * ?
    changeLog.consumer.o365.syncAttributeName = etc:attribute:office365:o365Sync
    changeLog.consumer.o365.retryOnError = true
    changeLog.consumer.o365.clientId = @o365.clientId@
    changeLog.consumer.o365.clientSecret = @o365.clientSecret@
    ```

    Replace `@o365.clientId@` and `@o365.clientSecret@` with appropriate values from the application configuration.

# Office 365 Notes

Login to the app management console:

https://apps.dev.microsoft.com/

For first time use, create Office 365 account first:

* Enroll in Office 365 (30-day trail account is fine), during enrollment, note the tenant value of the form "abcdef.onmicrosoft.com"
* At Office 365 management portal, click "Azure AD" link
* In Azure AD portal, click "Azure Activity Directory"
* Under "Manage" tab, click "App registrations"
* In intro text "To view and manage your registrations for converged applications, please visit the Microsoft Application Console.", click that link

Once in the App Mgmt Console, create an app:
* Note the "Application Id" that is generated (this will be the client id for OAuth2 tokens)
* Use "Generate New Password" when creating keys (this is the client secret for OAuth2 tokens)
* Under "Platforms" tab, click "Add Platform" and specify a redirect URL like "http://localhost/grouper"
* Under "Microsoft Graph Permissions", in "Application Permissions" specify required Graph API permissions
* At bottom of page, click "Save"
* IMPORTANT: after permissions are added (or modified) admin consent must be granted at the URL template: 
https://login.microsoftonline.com/$TENANT/adminconsent?client_id=$APPLICATION_ID

# Graph API Notes

To get a token for making Graph API calls, do the following:

* POST http method
* URL is of the form https://login.microsoftonline.com/$TENANT/oauth2/v2.0/token
 
Use the following request parameters:   
* client_secret - specify password generated from "Generate New Password" above
* client_id - specify Application Id that's generated when creating app
* grant_type - specify client_credentials, this value implies "Application Permissions" (as opposed to "Delegated Permissions")
* redirect_uri - specify http://localhost/grouper
* scope - specify https://graph.microsoft.com/.default
