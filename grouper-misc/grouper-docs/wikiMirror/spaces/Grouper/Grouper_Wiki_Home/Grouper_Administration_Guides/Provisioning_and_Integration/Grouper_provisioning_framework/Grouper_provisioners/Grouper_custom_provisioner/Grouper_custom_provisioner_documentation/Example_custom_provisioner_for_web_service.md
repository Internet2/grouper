---
title: "Example custom provisioner for web service"
space: Grouper
pageId: 28564251
version: 22
lastUpdated: 2026-07-12T06:40:16.263Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564251/Example+custom+provisioner+for+web+service
---

Valid for Grouper v2.6.18+

[This is the lite version of this implementation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564240/Example+generic+provisioner+for+web+service+WS)

This is an example of how an institution would make a provisioner that is not included in the Grouper Provisioning Framework. If you have something that is generic that others can leverage, maybe we should add it to the Grouper product. If your needs are specific to your institution, this is what you need to do.

This example is based on requirements posted to the slack channel from University of Minnesota.

All the code is included in Grouper, so you can see the source code. Though this provisioner is not enabled, it is intended as an example.

[Example WS source code](https://github.com/Internet2/grouper/tree/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner)

[Example WS test source code](https://github.com/Internet2/grouper/tree/GROUPER_2_6_BRANCH/grouper/src/test/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner)

**Table of contents**

****

## WS spec

The first step is to identify and document the WS spec. Proof of concepts of calling the WS could be done.

In this case there is one operation, a REPLACE of members for a group

It is assumed that authentication is basic auth.

HTTP method

```
PUT /path/endpoint/<SOURCE>/<ROLE>
```

HTTP body

```
<?xml version="1.0"?>
<ExternalRoleRequest>
 <Users>
    <netID>USER1234</netID>
    <netID>USER5678</netID>
    <netID>USER9012</netID>
    <netID>USER3456</netID>
  </Users>
</ExternalRoleRequest>
```

## Design the target representation of objects

Plan out which operations of the WS will be used for Grouper and how.

There is one operation, so we will use that

The <SOURCE> will be configured for the provisioner instance. If you want to provision to multiple sources, make another provisioner. This is an assumption and could be metadata or based on a parent folder or whatever.

The <ROLE> will be the sole attribute of the target representation of the group. We will translate this from the extension of the group. Again this is an assumption. Figure out how you want to translate based on your requirements.

| **Target group representation** |
| --- |
| Attribute | Translation | Notes |
| role | group extension | The provisioner will put this attribute in the role spot in the URL |

The provisioning type will be membershipObjects. We could have probably used groupAttributes, but this is what we did. The membershipObject will have two attributes.

| **Target membership representation** |
| --- |
| Attribute | Translation | Notes |
| role | group extension | The provisioner needs this to differentiate memberships from one group to another. Needs a tuple |
| netID | entity subjectIdentifier | The main subject identifier defaults to the subject source subjectIdentifier0. Will make the XML based on these |

We are not selecting or changing entities and there is a straight translation from grouper provisioning entities (subjectIdentifier0) so we don't need to define a target representation of entities.

## Custom external system

Note, this is optional. You dont need an external system to make your provisioner. It is nice to have one to see it in the UI, test it, re-use it, etc. If you dont create an external system, just reference whatever properties you need from a grouper config file, and set those in the config file or configuration screen in the UI.

In this case we could probably re-use a built in Grouper basic auth WS external system. However, to show how to make your own, we will just implement one anyways

1. [Implement the external system](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/ExampleWsExternalSystem.java) - note, the test method in this case uses the DAO implementation below... this is very circular. You could implement something simple here instead if you like...
2. Register this external system in grouper.properties (config id doesnt matter)
  
  
  ```
  grouperExtraExternalSystem.exampleWsExternalSystem.class = edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.ExampleWsExternalSystem
  ```
3. Register a file which has the [spec for the external system wizard](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/grouper.extraMetadata.externalSystem.exampleWsExternalSystem.properties)
  
  1. The path must be the same path as your External System implementation (identify some java package that is your own). So you might start with edu/upenn/penngroups/myProvisioner if you were at penn...
  2. The filename is: grouper.extraMetadata.externalSystem.<externalSystemConfigIdAbove>.properties
    
    
    ```
    edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/grouper.extraMetadata.externalSystem.exampleWsExternalSystem.properties
    ```
4. Then in that file, identify which properties you need. In this case we will implement testing of the external system which will just all a method on it. In this case we will just replace a group memberships. In yours you might have a less heavy method to call.  
  
  
  1. Look in the base properties files of Grouper for examples of the metadata (the JSON commented out above each property). Note this file has all commented out properties, not actual properties
    
    
    
    
    ```
    ############################################
    ## example external system
    ############################################ 
    
    # endpoint prefix
    # {valueType: "string", required: true}
    # grouper.exampleWsExternalSystem.myExampleExternalSystem.endpointPrefix =
    
    # user name
    # {valueType: "string", required: true}
    # grouper.exampleWsExternalSystem.myExampleExternalSystem.userName =
    
    # password
    # {valueType: "password", sensitive: true, required: true}
    # grouper.exampleWsExternalSystem.myExampleExternalSystem.password =
    
    # test source
    # {valueType: "string", required: false}
    # grouper.exampleWsExternalSystem.myExampleExternalSystem.testSource =
    
    # test role
    # {valueType: "string", required: false}
    # grouper.exampleWsExternalSystem.myExampleExternalSystem.testRole =
    
    # comma separated net ids
    # {valueType: "string", required: false}
    # grouper.exampleWsExternalSystem.myExampleExternalSystem.testNetIds =
    ```
5. You can externalize that text in grouper.text.en.us.properties
  
  
  
  
  ```
  config.ExampleWsExternalSystem.title = Example WS external system
  
  config.ExampleWsExternalSystem.attribute.endpointPrefix.label = Endpoint
  config.ExampleWsExternalSystem.attribute.endpointPrefix.description = This is the prefix of the endpoint before the source and role, e.g. http://localhost:8080/grouper/mockServices/exampleWs
  
  config.ExampleWsExternalSystem.attribute.userName.label = Username
  config.ExampleWsExternalSystem.attribute.userName.description = Basic auth username
  
  config.ExampleWsExternalSystem.attribute.password.label = Password
  config.ExampleWsExternalSystem.attribute.password.description = Basic auth password
  
  config.ExampleWsExternalSystem.attribute.testSource.label = Test source
  config.ExampleWsExternalSystem.attribute.testSource.description = When hitting the 'test' button this is the source that will be sent
  
  config.ExampleWsExternalSystem.attribute.testRole.label = Test role
  config.ExampleWsExternalSystem.attribute.testRole.description = When hitting the 'test' button this is the role that will be sent
  
  config.ExampleWsExternalSystem.attribute.testNetIds.label = 
  config.ExampleWsExternalSystem.attribute.testNetIds.description = When hitting the 'test' button these are the netID's that will be sent
  
  
  ```
6. Now you can see the external system in the UI and configure one
7. Test the external system (note, this relies on the Mock and provisioner implementation below)

## Mock service

In Grouper, when we maintain mock services for all our WS based provisioners. We do this so we can easily and quickly unit test locally without needing accounts or environments. This is ***optional***. If you want to do that, it is the first step.

Note you need to [setup your development environment to run tomcat](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/48792900/How+to+Setup+a+lite+Grouper+Development+Environment+for+Grouper) and the grouper webapp or deploy this to one of your environments at your institution.

[Implement the mock service class](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/ExampleWsMockServiceHandler.java)

```
private static void createMockExampleTable(DdlVersionBean ddlVersionBean, Database database) {
    
    final String tableName = "mock_example_ws";

    try {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_name", Types.VARCHAR, "256", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "net_id", Types.VARCHAR, "256", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "source", Types.VARCHAR, "256", true, true);
    } catch (Exception e) {
      
    }
    
  }
```

The other steps in the source file:

1. Implement the server side of basic auth
2. Check to see if table there, and if not, create it
3. Operation for replace memberships which parses XML and inserts/deletes in table
4. Linking the path and method to that operation

To link this mock server to the Grouper mock system and run it:

1. Register the mock server in grouper.properties (myExampleWsMock is the config id, any config id is fine)
  
  
  ```
  grouperExtraMockServer.myExampleWsMock.class = edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.ExampleWsMockServiceHandler
  grouperExtraMockServer.myExampleWsMock.path = exampleWs
  
  
  ```
2. Enable mocks in your env in grouper.hibernate.properties (UI or WS, we generally use UI but should work in WS too)
  
  
  ```
  grouper.is.mockServices = true
  ```
3. Tell the mock server which external system (credentials) it is in grouper.properties (see code for this mock service)
  
  
  ```
  grouperTest.exampleWs.mockExternalSystem.configId = myExampleExternalSystem1
  ```
4. Now you can hit this server just like the spec!
5. We can see this mock data in the database

## Provisioner wizard

This enables you to have a custom wizard for your provisioner. This is optional

1. The [ProvisioningConfiguration](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/ExampleWsProvisionerConfiguration.java) class identifies the configs for this provisioner. It also specifies the startWiths (optional) explained below
2. Register the provisioner in the grouper.properties
  
  
  ```
  grouperExtraProvisionerConfiguration.exampleWsProvisionerConfig.class = edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.ExampleWsProvisionerConfiguration
  ```
3. These are the properties and json metadata needed for this provisioner. Pick keys that differentiate this provisioner from others (see myExampleWs in there?). The file must be in the same Java package as the provisioner, and must be named like this: grouper-loader.extraMetadata.provisioner.exampleWsProvisionerConfig.properties. where exampleWsProvisionerConfig is the config ID of the provisioner registered above in the grouper.properties. Note the drop down for attribute names for group and membership are just copied from the base config, the configID is changed, and the drop down is added with the valid attribute names
  
  
  ```
  # provisioner class
  # {valueType: "class", required: true, readOnly: true}
  # provisioner.myExampleWsProvisioner.class = edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.GrouperExampleWsProvisioner
  
  # Example WS external system endpoint
  # {valueType: "string", required: true, order: 19, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.ExampleWsExternalSystem"}
  # provisioner.myExampleWsProvisioner.exampleWsExternalSystemConfigId =
  
  # This is the 'source' part of URL to the service: PUT /path/endpoint/<SOURCE>/<ROLE>
  # {valueType: "string", required: true, order: 20}
  # provisioner.myExampleWsProvisioner.exampleWsSource =
  
  # Name of the attribute
  # {valueType: "string", order: 21000, required: true, showEl: "${operateOnGrouperGroups && numberOfGroupAttributes > $i$}", formElement: "dropdown", optionValues: ["role"], repeatGroup: "targetGroupAttribute", repeatCount: 20}
  # provisioner.myExampleWsProvisioner.targetGroupAttribute.$i$.name =
  
  # Name of the attribute
  # {valueType: "string", order: 5710, required: true, showEl: "${operateOnGrouperMemberships && numberOfMembershipAttributes > $i$}", formElement: "dropdown", optionValues: ["role", "netID"], repeatGroup: "targetMembershipAttribute", repeatCount: 20}
  # provisioner.myExampleWsProvisioner.targetMembershipAttribute.$i$.name =
  
  
  ```
4. You can externalize that text in grouper.text.en.us.properties . Note the ExampleWsProvisionerConfiguration is the classname defined above
  
  
  ```
  config.ExampleWsProvisionerConfiguration.title = Example replace WS
  
  config.ExampleWsProvisionerConfiguration.attribute.exampleWsSource.label = Example WS source
  config.ExampleWsProvisionerConfiguration.attribute.exampleWsSource.description = Source part of the URL for this service
  config.ExampleWsProvisionerConfiguration.attribute.exampleWsExternalSystemConfigId.label = WS external system
  config.ExampleWsProvisionerConfiguration.attribute.exampleWsExternalSystemConfigId.description = External system that this provisioner points to
    
  ```
5. Now you will see the provisioner in the wizard screen with any extra configs

## Configure the provisioner

This provisioner needs to:

- Not really do much with entities
- Not insert/update/delete groups
- Just replace memberships
- Map the role and netID

```
provisioner.exampleWsProvTest.addDisabledFullSyncDaemon = true
provisioner.exampleWsProvTest.addDisabledIncrementalSyncDaemon = true
provisioner.exampleWsProvTest.class = edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.GrouperExampleWsProvisioner
provisioner.exampleWsProvTest.customizeGroupCrud = true
provisioner.exampleWsProvTest.customizeMembershipCrud = true
provisioner.exampleWsProvTest.deleteGroups = false
provisioner.exampleWsProvTest.deleteMemberships = false
provisioner.exampleWsProvTest.exampleWsExternalSystemConfigId = myExampleExternalSystem1
provisioner.exampleWsProvTest.exampleWsSource = mySource
provisioner.exampleWsProvTest.insertGroups = false
provisioner.exampleWsProvTest.insertMemberships = false
provisioner.exampleWsProvTest.logAllObjectsVerbose = true
provisioner.exampleWsProvTest.membership2AdvancedOptions = true
provisioner.exampleWsProvTest.membershipMatchingIdExpression = ${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('role'), targetMembership.retrieveAttributeValueString('netID'))}
provisioner.exampleWsProvTest.numberOfGroupAttributes = 1
provisioner.exampleWsProvTest.numberOfMembershipAttributes = 2
provisioner.exampleWsProvTest.operateOnGrouperGroups = true
provisioner.exampleWsProvTest.operateOnGrouperMemberships = true
provisioner.exampleWsProvTest.provisioningType = membershipObjects
provisioner.exampleWsProvTest.replaceMemberships = true
provisioner.exampleWsProvTest.selectGroups = false
provisioner.exampleWsProvTest.selectMemberships = false
provisioner.exampleWsProvTest.showAdvanced = true
provisioner.exampleWsProvTest.startWith = this is start with read only
provisioner.exampleWsProvTest.subjectSourcesToProvision = jdbc
provisioner.exampleWsProvTest.targetGroupAttribute.0.name = role
provisioner.exampleWsProvTest.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.exampleWsProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = extension
provisioner.exampleWsProvTest.targetMembershipAttribute.0.name = role
provisioner.exampleWsProvTest.targetMembershipAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.exampleWsProvTest.targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField = extension
provisioner.exampleWsProvTest.targetMembershipAttribute.1.name = netID
provisioner.exampleWsProvTest.targetMembershipAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.exampleWsProvTest.targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField = subjectIdentifier
provisioner.exampleWsProvTest.updateGroups = false

```

## Unit test the provisioner

Based on the exported config, and the mock server (or real service if there is a test env), [we can create a unit test](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/test/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/ExampleWsProvisionerTest.java)

## Make a "start with"

Based on the exported config, consider what needs to be asked in the start with.

1. Configure the wizard properties in the [template grouper-loader file for this provisioner](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/grouper-loader.extraMetadata.provisioner.exampleWsProvisionerConfig.properties) (same file that provisioner wizard properties are in): grouper-loader.extraMetadata.provisioner.exampleWsProvisionerConfig.properties
  
  
  
  
  ```
  ################################################
  ## provisioner startWith - example ws properties
  ################################################
  
  # Example ws start with config
  # {valueType: "string", order: 15, readOnly: true}
  # provisionerStartWith.exampleWs.startWith = exampleWs
  
  # This is the example ws external system config id
  # {valueType: "string", required: true, order: 20, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.ExampleWsExternalSystem"}
  # provisionerStartWith.exampleWs.exampleWsExternalSystemConfigId =
  
  # This is the 'source' part of URL to the service: PUT /path/endpoint/<SOURCE>/<ROLE>
  # {valueType: "string", required: true, order: 50}
  # provisionerStartWith.exampleWs.exampleWsSource =
  
  # This is the 'role' part of URL to the service: PUT /path/endpoint/<SOURCE>/<ROLE>
  # {valueType: "string", required: true, order: 60, formElement: "dropdown", optionValues: ["extension", "id", "idIndex", "idIndexString", "name", "other", "script"]}
  # provisionerStartWith.exampleWs.groupTranslation =
  
  # This is the 'netID' part of the body of the request
  # {valueType: "string", order: 70, required: true, formElement: "dropdown", optionValues: ["other", "script", "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2"]}
  # provisionerStartWith.exampleWs.entityTranslation =
  
  # Add disabled full sync daemon?
  # {valueType: "boolean", order: 100, defaultValue: "true"}
  # provisionerStartWith.exampleWs.addDisabledFullSyncDaemon =
  
  # Add disabled incremental sync daemon?
  # {valueType: "boolean", order: 200, defaultValue: "true"}
  # provisionerStartWith.exampleWs.addDisabledIncrementalSyncDaemon =
  ```
2. Register the "start with" in the [provisioner configuration](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/ExampleWsProvisionerConfiguration.java)
  
  
  
  
  ```
   public final static Set<String> startWithConfigClassNames = new LinkedHashSet<String>();
    
    static {
      startWithConfigClassNames.add(ExampleWsProvisioningStartWith.class.getName());
    }
    
    @Override
    public List<ProvisionerStartWithBase> getStartWithConfigClasses() {
      
      List<ProvisionerStartWithBase> result = new ArrayList<ProvisionerStartWithBase>();
      
      for (String className: startWithConfigClassNames) {
        try {
          Class<ProvisionerStartWithBase> configClass = (Class<ProvisionerStartWithBase>) GrouperUtil.forName(className);
          ProvisionerStartWithBase config = GrouperUtil.newInstance(configClass);
          result.add(config);
        } catch (Exception e) {
          //TODO
        }
      }
      
      return result;
      
    }
  
  
  ```
3. [Implement the logic of the "start with"](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/ExampleWsProvisioningStartWith.java)
4. Add externalized text to grouper.text.en.us.properties
  
  
  
  
  ```
  provisionerStartWithOption_edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.ExampleWsProvisioningStartWith = Example replace WS 'start with'
  
  config.ExampleWsProvisioningStartWith.attribute.exampleWsExternalSystemConfigId.label = WS external system
  config.ExampleWsProvisioningStartWith.attribute.exampleWsExternalSystemConfigId.description = External system that this provisioner points to
  
  config.ExampleWsProvisioningStartWith.attribute.exampleWsSource.label = Example WS source
  config.ExampleWsProvisioningStartWith.attribute.exampleWsSource.description = Source part of the URL for this service
  
  config.ExampleWsProvisioningStartWith.attribute.groupTranslation.label = Role translation
  config.ExampleWsProvisioningStartWith.attribute.groupTranslation.description = How is the group "role" translated
  
  config.ExampleWsProvisioningStartWith.attribute.entityTranslation.label = NetID translation
  config.ExampleWsProvisioningStartWith.attribute.entityTranslation.description = How is the entity "netID" translated
  
  config.ExampleWsProvisioningStartWith.attribute.addDisabledFullSyncDaemon.label = $$config.GenericConfiguration.attribute.addDisabledFullSyncDaemon.label$$
  config.ExampleWsProvisioningStartWith.attribute.addDisabledFullSyncDaemon.description = $$config.GenericConfiguration.attribute.addDisabledFullSyncDaemon.description$$
  
  config.ExampleWsProvisioningStartWith.attribute.addDisabledIncrementalSyncDaemon.label = $$config.GenericConfiguration.attribute.addDisabledIncrementalSyncDaemon.label$$
  config.ExampleWsProvisioningStartWith.attribute.addDisabledIncrementalSyncDaemon.description = $$config.GenericConfiguration.attribute.addDisabledIncrementalSyncDaemon.description$$
  
  
  
  ```
5. See the "start with" in action

## Implement the configuration subclass for provisioner properties

[This is a javabean for the provisioner configuration](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/GrouperExampleWsConfiguration.java). This is used in your DAO code

## Implement the DAO

The [DAO is the operations](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/GrouperExampleWsTargetDao.java) that communicate with the target and convert the native data (in this case JSON) with the provisioning object model in the target representation

## Implement the provisioner class which identifies the other classes

[This is the main provisioner class](https://github.com/Internet2/grouper/blob/GROUPER_2_6_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/app/provisioningExamples/exampleWsReplaceProvisioner/GrouperExampleWsProvisioner.java). It identifies which parts of the provisioner are customized

## See the provisioner in action

In this case the provisioner calls a web service that uses a database to store the data. When the provisioner runs, this is what the data looks like...
