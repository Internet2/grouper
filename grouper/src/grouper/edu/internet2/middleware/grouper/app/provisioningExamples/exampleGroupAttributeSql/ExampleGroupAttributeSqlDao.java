package edu.internet2.middleware.grouper.app.provisioningExamples.exampleGroupAttributeSql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisionerCommands;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningDao;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * this has groups, and multivalued attributes, specifically members and other lists
 * @author mchyzer
 *
 */
public class ExampleGroupAttributeSqlDao extends SqlProvisioningDao {

  private static final String groupAttributesTableName = "testgrouper_pro_ldap_group_attr";
  private static final String dbExternalSystemConfigId = "grouper";
  private static final String groupTableName = "testgrouper_prov_group1";
  private static final List<String> groupTablePrimaryColNamesList = GrouperUtil.toList("uugid", "displayname");
  private static final List<String> groupTableAttributesColNamesList = GrouperUtil.toList("group_uuid", "attribute_name", "attribute_value");
  private static final List<String> groupAttributesFilterColumn = GrouperUtil.toList("group_uuid");
  private static final String groupTableIdColumn = "uugid";
  private static final List<String> groupColumnsToFilterOn = GrouperUtil.toList("uugid");
  
  
  @Override
  public void setGrouperProvisioner(final GrouperProvisioner provisioner) {
    super.setGrouperProvisioner(provisioner);
  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    // testgrouper_prov_group
    // uuid, display_name
    // attribute table: testgrouper_pro_ldap_group_attr
    // group_uuid, attribute_name, attribute_value
    
    // uugid, displayname
    // administrators, contacts, members
    
    List<Object[]> groupPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsNoFilter(dbExternalSystemConfigId, groupTablePrimaryColNamesList, groupTableName);
    List<Object[]> attributeValuesSeparateTable = SqlProvisionerCommands.retrieveObjectsNoFilter(dbExternalSystemConfigId, groupTableAttributesColNamesList, groupAttributesTableName);

    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();

    retrieveGroupsAddRecord(result, 
        groupPrimaryAttributeValues, attributeValuesSeparateTable, 
        groupTablePrimaryColNamesList, groupTableAttributesColNamesList,
        groupTableIdColumn, groupAttributeNameToConfigAttribute);
   
    return new TargetDaoRetrieveAllGroupsResponse(result);
  }

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    
    List<Object> idsToRetrieve = GrouperUtil.toList(targetDaoRetrieveGroupRequest.getTargetGroup().getId());
    
    List<Object[]>  groupPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsColumnFilter(
        dbExternalSystemConfigId, groupTablePrimaryColNamesList, groupTableName, null, null, 
        groupColumnsToFilterOn, idsToRetrieve, null, false);

    if (GrouperUtil.length(groupPrimaryAttributeValues) == 0) {
      return new TargetDaoRetrieveGroupResponse(null);
    }
    
    List<Object> mainTableIdsFound = new ArrayList<Object>();
    for (Object[] groupPrimaryAttributeValue : groupPrimaryAttributeValues) {
      Object mainTableId = groupPrimaryAttributeValue[0];
      GrouperUtil.assertion(!GrouperUtil.isBlank(mainTableId), "Why is main table ID blank?");
      mainTableIdsFound.add(mainTableId);
    }
    
    List<Object[]> attributeValuesSeparateTable = SqlProvisionerCommands.retrieveObjectsColumnFilter(dbExternalSystemConfigId, groupTableAttributesColNamesList, 
        groupAttributesTableName, null,  null, 
        groupAttributesFilterColumn, mainTableIdsFound, null, false);

    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();

    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();

    retrieveGroupsAddRecord(result, 
        groupPrimaryAttributeValues, attributeValuesSeparateTable, 
        groupTablePrimaryColNamesList, groupTableAttributesColNamesList,
        groupTableIdColumn, groupAttributeNameToConfigAttribute);
   
    GrouperUtil.assertion(GrouperUtil.length(result) == 1, "found multiple results!");
    
    return new TargetDaoRetrieveGroupResponse(result.get(0));

  }

  public List<Object[]> toListObjectArray(Object[] input) {
    List<Object[]> result = new ArrayList<>();
    result.add(input);
    return result;
  }
  
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();

    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    List<Object[]> attributeValuesPrimaryTable = toListObjectArray(new Object[] {
        targetGroup.retrieveAttributeValueString("uugid"), targetGroup.retrieveAttributeValueString("displayname")});

    
    List<Object[]> attributeValuesAttributeTable = new ArrayList<Object[]>();

    // dont do members yet
    //  for (String member : (Set<String>)(Object)targetGroup.retrieveAttributeValueSet("members")) {
    //    attributeValuesAttributeTable.add(new Object[] {targetGroup.retrieveAttributeValueString("uugid"), "members", member});
    //  }
    for (String member : (Set<String>)(Object)targetGroup.retrieveAttributeValueSet("administrators")) {
      attributeValuesAttributeTable.add(new Object[] {targetGroup.retrieveAttributeValueString("uugid"), "administrators", member});
    }
    for (String member : (Set<String>)(Object)targetGroup.retrieveAttributeValueSet("contacts")) {
      attributeValuesAttributeTable.add(new Object[] {targetGroup.retrieveAttributeValueString("uugid"), "contacts", member});
    }
        
    SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, groupTableName, groupTablePrimaryColNamesList, attributeValuesPrimaryTable);
    
    SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, groupAttributesTableName, groupTableAttributesColNamesList, attributeValuesAttributeTable);
 
    targetGroup.setProvisioned(true);
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
      provisioningObjectChange.setProvisioned(true);
    }
      
    return new TargetDaoInsertGroupResponse();
  }

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(
      TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    // replace the group main table
    SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, groupTableName, 
        GrouperUtil.toList("displayname"), toListObjectArray(new Object[] {targetGroup.retrieveAttributeValueString("displayname")}),
        GrouperUtil.toList("uugid"), toListObjectArray(new Object[] {targetGroup.getId()}));

    List<Object[]> attributeTableInserts = new ArrayList<>();
    List<Object[]> attributeTableDeletes = new ArrayList<>();
        
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
      String fieldName = provisioningObjectChange.getAttributeName();

      if (StringUtils.equals(fieldName, "members") || StringUtils.equals(fieldName, "administrators") || StringUtils.equals(fieldName, "contacts")) {
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          attributeTableInserts.add(new Object[] {targetGroup.getId(), fieldName, provisioningObjectChange.getNewValue()});
        }
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
          attributeTableDeletes.add(new Object[] {targetGroup.getId(), fieldName, provisioningObjectChange.getOldValue()});
        }
      }
    }

    if (attributeTableInserts.size() > 0 ) {
      
      SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, groupAttributesTableName, groupTableAttributesColNamesList, attributeTableInserts); 
      
    }
    
    if (attributeTableDeletes.size() > 0 ) {
      SqlProvisionerCommands.deleteObjects(attributeTableDeletes, dbExternalSystemConfigId, groupAttributesTableName, 
          groupTableAttributesColNamesList, null, null, null, false, 
          false); 
      
    }

    markProvisioned(targetGroup, true);
    return new TargetDaoUpdateGroupResponse();
  }

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {

    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    SqlProvisionerCommands.deleteObjects(toListObjectArray(new Object[] {targetGroup.getId()}), dbExternalSystemConfigId, groupTableName, GrouperUtil.toList("uugid"),
        groupAttributesTableName, "group_uuid", null, false, true);

    markProvisioned(targetGroup, true);
    return new TargetDaoDeleteGroupResponse();
  }

  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    
    List<Object> idsToRetrieve = GrouperUtil.toList(targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup().getId());
    
    List<Object[]>  groupPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsColumnFilter(
        dbExternalSystemConfigId, groupTablePrimaryColNamesList, groupTableName, null, null, 
        groupColumnsToFilterOn, idsToRetrieve, null, false);

    if (GrouperUtil.length(groupPrimaryAttributeValues) == 0) {
      return new TargetDaoRetrieveMembershipsByGroupResponse(null);
    }
    
    List<Object> mainTableIdsFound = new ArrayList<Object>();
    for (Object[] groupPrimaryAttributeValue : groupPrimaryAttributeValues) {
      Object mainTableId = groupPrimaryAttributeValue[0];
      GrouperUtil.assertion(!GrouperUtil.isBlank(mainTableId), "Why is main table ID blank?");
      mainTableIdsFound.add(mainTableId);
    }
    
    List<Object[]> attributeValuesSeparateTable = SqlProvisionerCommands.retrieveObjectsColumnFilter(dbExternalSystemConfigId, groupTableAttributesColNamesList, 
        groupAttributesTableName, GrouperUtil.toList("attribute_name"),  GrouperUtil.toList("members"), 
        groupAttributesFilterColumn, mainTableIdsFound, null, false);

    List<ProvisioningGroup> provisioningGroup = new ArrayList<ProvisioningGroup>();

    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();

    retrieveGroupsAddRecord(provisioningGroup, 
        groupPrimaryAttributeValues, attributeValuesSeparateTable, 
        groupTablePrimaryColNamesList, groupTableAttributesColNamesList,
        groupTableIdColumn, groupAttributeNameToConfigAttribute);
   
    GrouperUtil.assertion(GrouperUtil.length(provisioningGroup) == 1, "found multiple results!");
    
    TargetDaoRetrieveMembershipsByGroupResponse resultResponse = new TargetDaoRetrieveMembershipsByGroupResponse();
    resultResponse.setTargetMemberships(GrouperUtil.toList(provisioningGroup.get(0)));
    return resultResponse;
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities)
  {
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
  }

  private void markProvisioned(final ProvisioningUpdatable updatable, final boolean isProvisioned) {
    updatable.setProvisioned(isProvisioned);
    for (ProvisioningObjectChange change : GrouperUtil.nonNull(updatable.getInternal_objectChanges())) {
      change.setProvisioned(isProvisioned);
    }
  }
}
