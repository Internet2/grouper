---
title: "Grouper provisioning configuration validation"
space: Grouper
pageId: 28555162
version: 7
lastUpdated: 2026-07-01T05:38:49.036Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555162/Grouper+provisioning+configuration+validation
---

## Run provisioning configuration validation

1. When a provisioner is created in the UI, the validation will run and alert the user of issues before the provisioner is saved
2. When a provisioner is edited in the UI, the validation will run and alert the user of issues before the provisioner is saved
3. Provisioner diagnostics will run the configuration validation and display results
4. When a provisioner runs, the configuration will be periodically validated (cached results). Errors are not fatal.

## Provisioning configuration editing in UI

It is assumed that provisioners will either:

1. Be configured in the UI
2. or if configured in config files, the provisioner will be pulled up in the UI for dignostics (which runs configuration validation)

## Basic built-in validation

Each config in the base config for the provisioner has JSON metadata which facilitates basic built in validation

```
# number of attributes for target groups
# {valueType: "integer", order: 19999, subSection: "group", defaultValue: "0", showEl:"${operateOnGrouperGroups}", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"] }
# provisioner.genericProvisioner.numberOfGroupAttributes =

```

The GrouperProvisioningConfigurationValidation.java file holds built in generic framework validation for all provisioners. Each validation rule is in its own method so it can be overridden by a provisioner to be removed or adjusted. The validation result can cause an indicator to be placed on the UI configuration screen to alert the user where the problem is. If a field is missing it can display an error at the top of the screen. If there are a few fields and they all should be marked, multiple validation errors can display at one time. All validations should be unit tested.

For example, this will display an error on the "deleteEntities" config item on the UI

```
  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public MultiKey validateEntityDeleteHasDeleteType(Map<String, String> suffixToConfigValue) {
    
    boolean deleteEntities = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteEntities"), false);
    if (deleteEntities) {
      
      boolean deleteEntitiesIfNotExistInGrouper = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteEntitiesIfNotExistInGrouper"), false);
      boolean deleteEntitiesIfGrouperDeleted = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteEntitiesIfGrouperDeleted"), false);
      boolean deleteEntitiesIfGrouperCreated = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteEntitiesIfGrouperCreated"), false);
      
      int deleteTypes = 0;
      if (deleteEntitiesIfNotExistInGrouper) {
        deleteTypes++;        
      }
      if (deleteEntitiesIfGrouperDeleted) {
        deleteTypes++;        
      }
      if (deleteEntitiesIfGrouperCreated) {
        deleteTypes++;        
      }
      
      if (deleteTypes != 1) {
        return new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType"), "deleteEntities");
      }
    }
    return null;
    
  }

```

## Provisioner-specific configuration validation

Provisioners should extend the generic class and implement validation specific to the provisioning at hand. Validations can make network calls, though they should not take too long (e.g. all validations shouldnt take more than 10 seconds). If something takes a while that can be added to diagnostics instead of validation. e.g. yes validate that an OU exists. e.g. no, do not retrieve all users to validate that filter.

See LDAP for an example

Extend the class

```
public class LdapSyncConfigurationValidation extends GrouperProvisioningConfigurationValidation {
```

Register the class with the provisioner

```
  @Override
  protected Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningConfigurationValidationClass() {
    return LdapSyncConfigurationValidation.class;
  }  

```

Override the method that does the validation and include the generic logic

```
  @Override
  public List<MultiKey> validateFromSuffixValueMap(
      Map<String, String> suffixToConfigValue) {
    List<MultiKey> errorMessagesAndConfigSuffixes = GrouperUtil.nonNull(super.validateFromSuffixValueMap(suffixToConfigValue));
    
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateDnExistsAndString(suffixToConfigValue));
    // add more validation calls here

    return errorMessagesAndConfigSuffixes;
  }

```

Implement a validation call, each in own method

```
  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public List<MultiKey> validateDnExistsAndString(Map<String, String> suffixToConfigValue) {
    
    List<MultiKey> result = new ArrayList<MultiKey>();

    OBJECT_TYPE: for (String objectType: new String[] {"targetGroupAttribute", "targetEntityAttribute"}) {
      
      String objectTypeLabel = null;
      
      if (StringUtils.equals("targetGroupAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsGroup");
      } else if (StringUtils.equals("targetEntityAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsEntity");
      } else {
        throw new RuntimeException("Cant find object type: " + objectType);
      }
      GrouperTextContainer.assignThreadLocalVariable("type", objectTypeLabel);

      for (int i=0; i< 20; i++) {

        Boolean isField = GrouperUtil.booleanObjectValue(suffixToConfigValue.get(objectType + "."+i+".isFieldElseAttribute"));
        if (isField == null) {
          if (i>0) {
            result.add(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")}));
          }
          continue OBJECT_TYPE;
          
        }
        if (!isField) {
          continue;
        }

        String nameConfigKey = objectType + "."+i+".fieldName";
        String name = suffixToConfigValue.get(nameConfigKey);
        String type = suffixToConfigValue.get(objectType + "."+i+".valueType");
        
        // all good, field with name "name" and type string
        if (StringUtils.equals(name, "name")) {
          if (!StringUtils.equalsIgnoreCase(type, "string")) {
            result.add(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), nameConfigKey}));
          }
          continue OBJECT_TYPE;
        }
        
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    return result;
    
  }

```

Unit test validations

```
    // test some validation, this used to be 'name' instead of 'displayName'
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "displayName");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    List<MultiKey> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();

    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    // provisioning.configuration.validation.dnRequired = Error: ${type} field 'name' is required.  It represents the LDAP DN
    // provisioning.configuration.validation.dnString = Error: ${type} field 'name' is must be value type 'string'.  It represents the LDAP DN
    assertTrue(errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")})));
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), "targetGroupAttribute.0.fieldName")));
    GrouperTextContainer.resetThreadLocalVariableMap();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "int");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    
    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    assertFalse(errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")})));
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), "targetGroupAttribute.0.fieldName")));
    GrouperTextContainer.resetThreadLocalVariableMap();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "string");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    
    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    assertFalse(errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")})));
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), "targetGroupAttribute.0.fieldName")));
    GrouperTextContainer.resetThreadLocalVariableMap();
    // end test some config

```

**See also**

[provisioning validation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554271/Grouper+provisioning+validation)
