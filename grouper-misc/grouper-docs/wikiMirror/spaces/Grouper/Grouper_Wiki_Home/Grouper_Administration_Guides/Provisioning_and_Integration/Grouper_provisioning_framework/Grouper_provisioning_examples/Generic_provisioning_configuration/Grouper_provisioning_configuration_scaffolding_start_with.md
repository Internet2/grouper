---
title: "Grouper provisioning configuration scaffolding (start with)"
space: Grouper
pageId: 28560139
version: 5
lastUpdated: 2026-07-01T05:36:12.346Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560139/Grouper+provisioning+configuration+scaffolding+start+with
---

> The info on this page applies to Grouper v2.6 and above.

Each Grouper provisioner has one or more scaffolds (start with) to help get started with common use cases of that provisioner.

This is a kickstart for the provisioning wizard.

When a new provisioner is configured, the user will have an option to start with a scaffold, or to start from scratch.

More scaffolds can be added as more use cases are documented.

## Example

Add a provisioner

Select SQL

If user picks 'Blank configuration', show normal screen

If a user is in the middle of a startWith, and changes startWith, then start over with new blank startWith

Select which scaffold

When that is submitted, it will validate based on rules in the config metadata and however the provisioner validates. In this case it could see if the table exists, and columns exist, etc.

If valid, it will generate an initial config for the provisioner, and population the full config screen. It will also give instructions to the user based on which scaffold was selected.

e.g.

```
        provisioner.someSqlProvisioner.class = edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner
        provisioner.someSqlProvisioner.dbExternalSystemConfigId = <what they selected>
        provisioner.someSqlProvisioner.groupTableIdColumn = <what user inputted>
        provisioner.someSqlProvisioner.groupTableName = <what user inputted>
        provisioner.someSqlProvisioner.numberOfGroupAttributes = <number of columns set>
        provisioner.someSqlProvisioner.operateOnGrouperGroups = true
        provisioner.someSqlProvisioner.provisioningType = groupAttributes
        provisioner.someSqlProvisioner.targetGroupAttribute.0.name = <column name inputted>
        provisioner.someSqlProvisioner.targetGroupAttribute.1.name = <column name inputted>
```

## Technical design (grouper team only)

Leverage configuration metadata for wizard forms. Each scaffold will have its own class in the package of the provisioner

```
public class SqlProvisioningGroupTableStartWith extends ProvisionerStartWithBase {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisionerStartWith." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(provisionerStartWith)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "startWith";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "sqlGroupTable";
  }
}
```

Provisioner provides the screen with the classes of options

```
SqlProvisioningGroupTableStartWith, SqlProvisioningEntityTableStartWith, SqlProvisioningGroupAndMembershipTableStartWith
```

Configuration metadata

```
(these configurations will NEVER be set...  these are just for metadata for wizard)
(get the screen text from provisioner is not overridden)

#######################################
## provisioner startWiths
#######################################

# this is the sql external system config id
# {valueType: "string", order: 25, readOnly: true}
# provisionerStartWith.sqlGroupTable.startWith = sqlGroupTable

////// since this is same suffix as the provisioner suffix, just get this part from there so its consistent: valueType: "string",  
////// required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem

# this is the sql external system config id
# {order: 50}
# provisionerStartWith.sqlGroupTable.dbExternalSystemConfigId =

////// since this is same suffix as the provisioner suffix, just get this part from there so its consistent: valueType: "string",  
////// required: true

# group table name
# {order: 100}
# provisionerStartWith.sqlGroupTable.groupTableName =

////// this is not in the provisioner config, so we need to externalized text
////// config.SqlProvisioningGroupTableStartWith.attribute.columnNames.label = Column names
////// config.SqlProvisioningGroupTableStartWith.attribute.columnNames.description = List the comma separated columns names of the groups table that Grouper needs to know about

# column names
# {valueType: "string", order: 100, required: true}
# provisionerStartWith.sqlGroupTable.columnNames =

////// since this is same suffix as the provisioner suffix, just get this part from there so its consistent: valueType: "string",  
////// required: true

# user primary key
# {order: 200}
# provisionerStartWith.sqlGroupTable.userPrimaryKey =

```
