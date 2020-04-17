# Azure AD Provisioner

This project is an Internet2 Grouper connector that synchronizes Grouper groups and users to Microsoft Azure Active Directory/Office 365.

This currently only supports security or Office 365 (unified) groups. Support for mail-enabled groups is unavailable due
to lack of support in the Microsoft API.

# Installing

1. build

    ```
    mvn clean package
    mvn dependency:copy-dependencies
    ```

2. copy contents of file to grouper home

    (2.5+ Docker container)
    ```
    cp target/grouper-azure-provisioner-2.5.0-SNAPSHOT.jar /opt/grouperContainer/lib/
    cp target/dependency/* /opt/grouperContainer/lib/
    ```

    (2.3, 2.4)
    ```
    cp target/grouper-azure-provisioner-2.5.0-SNAPSHOT.jar /opt/grouper.apiBinary-{version}/lib/custom
    cp target/dependency/* /opt/grouper.apiBinary-{version}/lib/custom
    ```

# Configuring

The provisioning attribute to add to the provisionable object is, by default, `etc:attribute:office365:o365Sync`. If set on a folder,
all groups under this folder (recursively) will be automatically provisioned. Alternatively, the attribute can be set on
an individual group. _It is not recommended to assign directly to a group_, as there is currently a race condition that
may occur in the current version of this code (i.e., if the changelog consumer runs after group creation but before
attribute assignment, provisioning will not occur).

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
    changeLog.consumer.o365.tenantId = @o365.tenantId@
    changeLog.consumer.o365.clientId = @o365.clientId@
    changeLog.consumer.o365.clientSecret = @o365.clientSecret@
    #changeLog.consumer.o365.idAttribute =
    #changeLog.consumer.o365.groupJexl =
    #changeLog.consumer.o365.groupType = [Security* | Unified]
    #changeLog.consumer.o365.visibility = [Public* | Private | Hiddenmembership]
    #changeLog.consumer.o365.proxyType = [http | socks]
    #changeLog.consumer.o365.proxyHost =
    #changeLog.consumer.o365.proxyPort =
```

Replace `@o365.tenantId@`, `@o365.clientId@` and `@o365.clientSecret@` with appropriate values from the application configuration.

The property `idAttribute` specifies what attribute is used to build the Azure user principal, and will default to "uid" if not set.
The Azure principal will get built as idattribute + "@" + tenantId. Whatever attribute is used must be available as a key as
returned in `subject.getAttributes()`; i.e., if you want to use the subject's id or identifier, it needs to be defined in the
subject attributes too.

If `groupJexl` is defined, it will be used to calculate the Azure group name, instead of the group name including
the path. The variable `group` is available within the jexl scriptlet to represent the group object. Brackets are
not needed around the scriptlet. For example:

`group.name.replaceAll("^app:azure:", "").replaceAll(":", "_")`

will remove the initial prefix "app:azure:" from the group path, and replace all folder separators with underscores.

The optional `groupType` property can set the provisioned groups as either a security group (`groupType = Security`)
or an Office 365 group (`groupType = Unified`). If not set, the default will be a security group. Mail-enabled groups
are not currently available, as they cannot be set through the Microsoft web service API.

For a Unified group provisioner only, the `visibility` property sets the Office 365 visibility. Possible values are Public (default),
Private, or Hiddenmembership. See [Microsoft's documentation on the option](https://docs.microsoft.com/en-us/graph/api/resources/group?view=graph-rest-1.0#group-visibility-options)
for more information.

If the daemon server requires a proxy to access the internet, a HTTP or SOCKS proxy can be defined using proxyType,
proxyHost, and proxyPort. Currently, the SOCKS5 proxy only supports anonymous access.

## Multiple Provisioners

It is possible to set up multiple Azure provisioners, each with different settings. One scenario for this would be to
have one folder for security groups and another for Office 365 groups. Or, different folders can have different
Jexl expressions, etc. To distinguish them, they need separate consumer attributes created. Then, each loader
configuration would reference their respective attribute names, and folders would set the distinguishing
syncAttributeName attribute to select a provisioner. Other required properties need to be repeated for each provisioner
For example:

```
    # Creates security groups
    changeLog.consumer.o365.class = edu.internet2.middleware.grouper.changeLog.consumer.Office365ChangeLogConsumer
    changeLog.consumer.o365.tenantId = my-tenant.onmicrosoft.com
    changeLog.consumer.o365.clientId = ...
    changeLog.consumer.o365.clientSecret = ...
    ...
    changeLog.consumer.o365.syncAttributeName = etc:attribute:office365:o365Sync
    changeLog.consumer.o365.groupJexl = ...

    # Creates Office 365 groups
    changeLog.consumer.o365Unified.class = edu.internet2.middleware.grouper.changeLog.consumer.Office365ChangeLogConsumer
    changeLog.consumer.o365Unified.tenantId = my-tenant.onmicrosoft.com
    changeLog.consumer.o365Unified.clientId = ...
    changeLog.consumer.o365Unified.clientSecret = ...
    ...
    changeLog.consumer.o365Unified.syncAttributeName = etc:attribute:office365:o365SyncUnified
    changeLog.consumer.o365Unified.groupType = Unified
```

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

# Acknowledgments

The Office365/Azure provisioner for Grouper originated as a project from Unicon, Inc. Primary developers were Bill
Thompson, Jj, and John Gasper, with other contributions by Chris Hyzer and Russ Trotter. The source project can be
found at https://github.com/Unicon/office365-and-azure-ad-grouper-provisioner .

This project includes contributions from Kansas State University, project https://github.com/kstateome/office365-and-azure-ad-grouper-provisioner.git.
Principal development was done by David Malia, with contribution by Kurt Zoglmann.
