---
title: "Grouper provisioning target DAO"
space: Grouper
pageId: 28555666
version: 9
lastUpdated: 2026-07-12T04:58:32.943Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555666/Grouper+provisioning+target+DAO
---

> The info on this page applies to Grouper 2.6 and above.

The connection between the [Grouper Provisioning framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework) and the target is the target DAO (data access object). This is an interface where the available/applicable methods are implemented.

Base class: edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase

Here are some target DAO methods

```
TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest)
TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest)
TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest)
TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest)
TargetDaoRetrieveAllDataResponse retrieveAllData(TargetDaoRetrieveAllDataRequest)
TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest)
TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest)
```

## Which methods should you implement?

1. Depends on which type of provisioning (e.g. provisioning memberships as group attributes requires different methods than provisioning membership objects)
2. If you can batch calls, then implement those methods (e.g. insertGroups vs insertGroup)
  
  
  
  1. Note, if you implement the batched method, you dont have to implement the non-batched method
3. Try your best guess, if provisioning doesnt throw an error, then you good
4. Consult with the Grouper team if you are unsure
  
  
  
  1. TODO we can give more info depending on provisioning type

## Identify the capabilities of the DAO

All methods and options that the DAO supports must be identified in the DAO capabilities

e.g. an example of a provisioner that provisions memberships as group attributes

```
  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {

    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroupWithOrWithoutMembershipAttribute(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroupMembershipAttribute(true);
    
  }

```

## How DAO is used

1. The configuration of the provisioner informs the target settings, the provisioner behaviors, etc
2. The provisioner behaviors help the framework to know which DAO methods to call and when
  
  
  
  1. e.g. full or incremental sync
  2. e.g. syncing memberships are objects, group attributes, entity attributes, etc
  3. e.g. should we delete entities? groups? which fields to update or insert?
  4. This is the "should the provisioner do a certain thing?"
3. If the provisioner needs to talk to the target, it always goes through the DAO adapter
  
  
  
  1. This is a facade around the DAO e.g. so it can translate compatible methods (turn individual calls into batched etc)
4. The DAO adapter and the target DAO are both implementations of the Base DAO
  
  
  
  1. the Base DAO just has method signatures and most methods are optional
5. The target DAO must identify which capabilities (methods) it supports so the framework knows which methods to call (e.g. individual or batched)
  
  
  
  1. This is the "can the provisioner do a certain thing?"
6. The DAO adapter use the configuration and DAO capabilities to call the correct and efficient Target DAO method
7. The DAO adapter delegates to the Target DAO
8. The Target DAO talks to the target and transforms data into the Grouper Provisioning beans
  
  
  
  1. It needs to time each atomic call to the target and register the results with the framework
  2. It might need to do error handling
  3. It must mark each insert/update/delete as complete (provisioned)

## Mark changes as complete

After you insert/update/delete, you need to mark each object as provisioned or throw an exception

e.g.

```
      targetGroup.setProvisioned(true);
```

In a batched method, set each item to provisioned or not if some succeed or some fail.

## Exception handling

If there is an exception, it should be set in the object. If you just throw the exception, then the caller (framework) will set exceptions into the objects and set provisioned false. Assigning exceptions is more important for batched methods

```
      targetGroup.setProvisioned(false);
      targetGroup.setException(e);
```

Note: you can easily store a string expression of an object in the exception (or other info)

```
      GrouperUtil.injectInException(e, targetGroup.toString());
```

## Time the individual atomic target calls

```
        long startNanos = System.nanoTime();
        try {
          ..target code
        } finally {
          this.addTargetDaoTimingInfo(new TargetDaoTimingInfo(<method name>, startNanos));
        }
        
      }

```

e.g.

```
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetGroups.size(), 900);
      for (int i = 0; i < numberOfBatches; i++) {
        long startNanos = System.nanoTime();
        try {
          List<ProvisioningGroup> currentBatchGrouperTargetGroups = GrouperUtil.batchList(grouperTargetGroups, 900, i);
          StringBuilder sql = new StringBuilder(sqlInitial);
          sql.append(" where g.").append(groupTableIdColumn).append(" in (");
          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
          
          for (int j=0; j<currentBatchGrouperTargetGroups.size();j++) {
            ProvisioningGroup grouperTargetGroup = currentBatchGrouperTargetGroups.get(j);
            gcDbAccess.addBindVar(grouperTargetGroup.getId());
            if (j>0) {
              sql.append(",");
            }
            sql.append("?");
          }
          sql.append(" ) ");
          groupsAndAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
          retrieveGroupsByAttributesAddRecord(result, groupTableIdColumn, groupAttributeTableAttributeNameColumn,
              groupAttributeTableAttributeValueColumn, commaSeparatedGroupColumnNames,
              groupColumnNamesArray, groupAttributeColumnNamesArray,
              groupsAndAttributeValues);
        } finally {
          this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroups", startNanos));
        }
        
      }

```
