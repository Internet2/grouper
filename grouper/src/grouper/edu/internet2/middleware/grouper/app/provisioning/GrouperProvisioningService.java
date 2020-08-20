package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_LAST_FULL_MILLIS_SINCE_1970;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_LAST_FULL_SUMMARY;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_LAST_INCREMENTAL_MILLIS_SINCE_1970;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_LAST_INCREMENTAL_SUMMARY;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperProvisioningService {
  
  /**
   * find all groups provisionable in target
   * @param target
   * @return the groups
   */
  public static Set<Group> findAllGroupsForTarget(final String target) {
    @SuppressWarnings("unchecked")
    Set<Group> groups = (Set<Group>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Set<Group> groups = new GroupFinder().assignIdOfAttributeDefName(GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getId())
          .addAttributeValuesOnAssignment(target)
          .assignIdOfAttributeDefName2(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision().getId())
          .addAttributeValuesOnAssignment2("true")
          .findGroups();
        return groups;
      }
      
    });

    return groups;
    
  }

  /**
   * 
   * @param gcGrouperSync
   * @param groupIds
   * @return the map of group id to group sync for use later on
   */
  public static GrouperProvisioningProcessingResult processProvisioningMetadataForGroupIds(GcGrouperSync gcGrouperSync, Collection<String> groupIds) {
    
    GrouperProvisioningProcessingResult grouperProvisioningProcessingResult = new GrouperProvisioningProcessingResult();
    
    grouperProvisioningProcessingResult.setGcGrouperSync(gcGrouperSync);
    
    if (GrouperUtil.length(groupIds) == 0) {
      return grouperProvisioningProcessingResult;
    }
    
    // get the provisioned groups from these group ids
    Map<String, Group> groupIdToProvisionedGroupMap = GrouperProvisioningService.findAllGroupsForTargetAndGroupIds(gcGrouperSync.getProvisionerName(), groupIds);
    grouperProvisioningProcessingResult.setGroupIdGroupMap(groupIdToProvisionedGroupMap);
    
    Map<String, GcGrouperSyncGroup> groupIdToGroupSyncMap = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIds);
    grouperProvisioningProcessingResult.setGroupIdToGcGrouperSyncGroupMap(groupIdToGroupSyncMap);
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroupsToUpdate = new ArrayList<GcGrouperSyncGroup>();
    List<GcGrouperSyncGroup> gcGrouperSyncGroupsToInsert = new ArrayList<GcGrouperSyncGroup>();
    
    List<String> groupIdsToAddToTarget = new ArrayList<String>();
    grouperProvisioningProcessingResult.setGroupIdsToAddToTarget(groupIdsToAddToTarget);

    List<String> groupIdsToRemoveFromTarget = new ArrayList<String>();
    grouperProvisioningProcessingResult.setGroupIdsToRemoveFromTarget(groupIdsToRemoveFromTarget);

    for (String groupId : groupIdToGroupSyncMap.keySet()) {
      
      GcGrouperSyncGroup gcGrouperSyncGroup = groupIdToGroupSyncMap.get(groupId);

      Group group = groupIdToProvisionedGroupMap.get(groupId);
      if (group == null) {
        //the group is not supposed to be provisioned
        
        // its in target and shouldnt be
        if (gcGrouperSyncGroup.isInTarget()) {
          groupIdsToRemoveFromTarget.add(groupId);
        }
        
        //it should not be provisioned
        if (gcGrouperSyncGroup.isProvisionable()) {
          gcGrouperSyncGroup.setProvisionable(false);
          gcGrouperSyncGroup.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
          gcGrouperSyncGroupsToUpdate.add(gcGrouperSyncGroup);
        }
      } else {
        
        // the group should be provisionable
        // its not in target and should be
        if (!gcGrouperSyncGroup.isInTarget()) {
          groupIdsToAddToTarget.add(groupId);
        }
        
        boolean needsUpdate = false;
        
        // update some metadata
        if (!StringUtils.equals(group.getName(), gcGrouperSyncGroup.getGroupName())) {
          gcGrouperSyncGroup.setGroupName(group.getName());
          needsUpdate = true;
        }
        // update some metadata
        if (!GrouperUtil.equals(group.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex())) {
          gcGrouperSyncGroup.setGroupIdIndex(group.getIdIndex());
          needsUpdate = true;
        }
        
        //it is not be provisioned, but should be
        if (!gcGrouperSyncGroup.isProvisionable()) {
          gcGrouperSyncGroup.setProvisionable(true);
          gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
          gcGrouperSyncGroup.setProvisionableEnd(null);
          needsUpdate = true;
        }

        if (needsUpdate) {
          gcGrouperSyncGroupsToUpdate.add(gcGrouperSyncGroup);
        }
      }
    }
    
    // find the ones where tracking objects dont exist
    for (String groupId : groupIdToProvisionedGroupMap.keySet()) {

      Group group = groupIdToProvisionedGroupMap.get(groupId);

      GcGrouperSyncGroup gcGrouperSyncGroup = groupIdToGroupSyncMap.get(groupId);

      // these are not tracked... start tracking them
      if (gcGrouperSyncGroup == null) {
    
        // this is not provisionable or in target
        gcGrouperSyncGroup = new GcGrouperSyncGroup();
        gcGrouperSyncGroup.setGrouperSync(gcGrouperSync);
        gcGrouperSyncGroup.setGroupId(groupId);
        gcGrouperSyncGroup.setGroupIdIndex(group.getIdIndex());
        gcGrouperSyncGroup.setGroupName(group.getName());
        gcGrouperSyncGroup.setProvisionable(true);
        gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
        groupIdToGroupSyncMap.put(groupId, gcGrouperSyncGroup);
        gcGrouperSyncGroupsToInsert.add(gcGrouperSyncGroup);
        groupIdsToAddToTarget.add(groupId);
      }
      
    }
    
    return grouperProvisioningProcessingResult;
  }
  
  /**
   * find all groups provisionable in target
   * @param target
   * @param groupIds
   * @return the groupId to group map
   */
  public static Map<String, Group> findAllGroupsForTargetAndGroupIds(final String target, final Collection<String> groupIds) {

    Map<String, Group> result = new HashMap<String, Group>();

    // we need some group ids
    if (GrouperUtil.length(groupIds) == 0) {
      return result;
    }
    
    @SuppressWarnings("unchecked")
    Set<Group> groups = (Set<Group>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Set<Group> groups = new GroupFinder().assignGroupIds(groupIds)
          .assignIdOfAttributeDefName(GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getId())
          .addAttributeValuesOnAssignment(target)
          .assignIdOfAttributeDefName2(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision().getId())
          .addAttributeValuesOnAssignment2("true")
          .findGroups();
        return groups;
      }
      
    });

    for (Group group : groups) {
      result.put(group.getId(), group);
    }
    
    return result;
  }
  
  /**
   * link up attribute ids to group ids to investigate
   */
  private static ExpirableCache<String, String> attributeAssignIdToGroupId = new ExpirableCache<String, String>(20);
  
  /**
   * find all groups provisionable in target
   * @param target
   * @param attributeAssignIdsOnId
   * @return the groups
   */
  public static Set<String> findAllGroupIdsFromAttributeAssignIdsOnIds (Set<String> attributeAssignIdsOnIdInput) {
    
    Set<String> groupIds = new HashSet<String>();

    Set<String> attributeAssignIdsOnIdToLookup = new HashSet<String>(attributeAssignIdsOnIdInput);
    
    // step 1, look in cache to see if we already know
    Iterator<String> attributeAssignIdsOnIdToLookupIterator = attributeAssignIdsOnIdToLookup.iterator();
    
    while (attributeAssignIdsOnIdToLookupIterator.hasNext()) {
      String attributeAssignId = attributeAssignIdsOnIdToLookupIterator.next();
      String groupId = attributeAssignIdToGroupId.get(attributeAssignId);
      if (groupId != null) {
        
        // empty string means not there
        if (!StringUtils.isBlank(groupId)) {
          groupIds.add(groupId);
        }
        // otherwise its the empty string, and that assign is not found to be associated with a groupId, just dont look
        attributeAssignIdsOnIdToLookupIterator.remove();
      }
    }
    
    if (GrouperUtil.length(attributeAssignIdsOnIdToLookup) == 0) {
      return groupIds;
    }
    
    // step 2, look for existing attribute assignments
    List<String> attributeAssignIdsOnIdList = new ArrayList<String>(attributeAssignIdsOnIdToLookup);
    
    int batchSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("provisioningAttributeAssignIdsBatchSize", 900);
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignIdsOnIdList, batchSize);
    
    for (int batchIndex = 0; batchIndex < numberOfBatches; batchIndex++) {

      List<String> attributeAssignIdsOnIdsBatch = GrouperUtil.batchList(attributeAssignIdsOnIdList, batchSize, batchIndex);

      //  select gaa1.owner_group_id, gaa2.id from grouper_attribute_assign gaa2, grouper_attribute_assign gaa1 
      //  where gaa1.id = gaa2.owner_attribute_assign_id  and gaa1.owner_group_id is not null and gaa2.id = '421ab5e6525f46429fa667849f2634cf';
      String sql = "select gaa1.owner_group_id, gaa2.id from grouper_attribute_assign gaa2, grouper_attribute_assign gaa1 " + 
          " where gaa1.id = gaa2.owner_attribute_assign_id  and gaa1.owner_group_id is not null "
          + " and gaa1.enabled = 'T' and gaa2.enabled = 'T' "
          + "and gaa2.id in (" 
          + GrouperClientUtils.appendQuestions(GrouperUtil.length(attributeAssignIdsOnIdsBatch)) + ")";
      List<Object[]> results = new GcDbAccess().sql(sql).bindVars(GrouperUtil.toArray(attributeAssignIdsOnIdsBatch, Object.class)).selectList(Object[].class);
      
      for (Object[] row : GrouperUtil.nonNull(results)) {

        String ownerGroupId = (String)row[0];
        String attributeAssignOnAssignId = (String)row[1];
        groupIds.add(ownerGroupId);

        // no longer need to lookup
        attributeAssignIdsOnIdToLookup.remove(attributeAssignOnAssignId);
        // cache this
        attributeAssignIdToGroupId.put(attributeAssignOnAssignId, ownerGroupId);
      }

    }

    if (GrouperUtil.length(attributeAssignIdsOnIdToLookup) == 0) {
      return groupIds;
    }

    
    // step 3, look in point in time
    attributeAssignIdsOnIdList = new ArrayList<String>(attributeAssignIdsOnIdToLookup);
    
    numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignIdsOnIdList, batchSize);
    
    for (int batchIndex = 0; batchIndex < numberOfBatches; batchIndex++) {

      List<String> attributeAssignIdsOnIdsBatch = GrouperUtil.batchList(attributeAssignIdsOnIdList, batchSize, batchIndex);

      //  select gpg.source_id, gpaa2.source_id from grouper_pit_attribute_assign gpaa2, grouper_pit_attribute_assign gpaa1, grouper_pit_groups gpg 
      //  where gpg.id = gpaa1.owner_group_id and gpaa1.id = gpaa2.owner_attribute_assign_id and gpaa2.source_id = '70761e3ce3204e5888d09a33c1c49246' ;
      String sql = "select gpg.source_id, gpaa2.source_id from grouper_pit_attribute_assign gpaa2, grouper_pit_attribute_assign gpaa1, grouper_pit_groups gpg \n" + 
          "    where gpg.id = gpaa1.owner_group_id and gpaa1.id = gpaa2.owner_attribute_assign_id and gpaa2.source_id in (" 
          + GrouperClientUtils.appendQuestions(GrouperUtil.length(attributeAssignIdsOnIdsBatch)) + ")";

      List<Object[]> results = new GcDbAccess().sql(sql).bindVars(GrouperUtil.toArray(attributeAssignIdsOnIdsBatch, Object.class)).selectList(Object[].class);
      
      for (Object[] row : GrouperUtil.nonNull(results)) {

        String ownerGroupId = (String)row[0];
        String attributeAssignOnAssignId = (String)row[1];
        groupIds.add(ownerGroupId);

        // no longer need to lookup
        attributeAssignIdsOnIdToLookup.remove(attributeAssignOnAssignId);
        // cache this
        attributeAssignIdToGroupId.put(attributeAssignOnAssignId, ownerGroupId);
      }

    }

    // cant find some of them
    for (String attributeAssignOnAssignId : GrouperUtil.nonNull(attributeAssignIdsOnIdToLookup)) {

      // empty string means cant find
      attributeAssignIdToGroupId.put(attributeAssignOnAssignId, "");
      
    }
    
    return groupIds;
    
  }
  
  /**
   * retrieve type setting for a given grouper object (group/stem) and target name.
   * @param grouperObject
   * @param targetName
   * @return
   */
  public static GrouperProvisioningAttributeValue getProvisioningAttributeValue(GrouperObject grouperObject, String targetName) {
    
    AttributeAssign attributeAssign = getAttributeAssign(grouperObject, targetName);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildGrouperProvisioningAttributeValue(attributeAssign);
  }

  /**
   * retrieve all the configured provisioning attributes for a given grouper object (group/stem)
   * @param grouperObject
   * @return
   */
  public static List<GrouperProvisioningAttributeValue> getProvisioningAttributeValues(final GrouperObject grouperObject) {
    
    final List<GrouperProvisioningAttributeValue> result = new ArrayList<GrouperProvisioningAttributeValue>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Map<String, GrouperProvisioningTarget> targetNames = GrouperProvisioningSettings.getTargets(true);
        
        for (String targetName: targetNames.keySet()) {
          GrouperProvisioningAttributeValue value = getProvisioningAttributeValue(grouperObject, targetName);
          if (value != null) {
            result.add(value);
          }
        }
        
        return null;
      }
      
    });
    
    return result;
  }
  
  /**
   * get provisioning attributes for a given grouper object and target name
   * @param grouperObject
   * @param targetName
   * @return
   */
  private static AttributeAssign getAttributeAssign(GrouperObject grouperObject, String targetName) {
    
    Set<AttributeAssign> attributeAssigns = null;
    
    if (grouperObject instanceof Group) {
      Group group = (Group)grouperObject;
      attributeAssigns = group.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    } else {
      Stem stem = (Stem)grouperObject;
      attributeAssigns = stem.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    }
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_TARGET);
      if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
        return null;
      }
      
      String targetNameFromDB = attributeAssignValue.getValueString();
      if (targetName.equals(targetNameFromDB)) {
       return attributeAssign;
      }
    }
    return null;
    
  }
  
  private static Set<String> getDistinctProvisionerConfigIds() {
    
    Set<String> result = new HashSet<String>();
    String query = "select distinct value_string from grouper_aval_asn_asn_group_v where attribute_def_name_name2 = ?";
    
    List<String> configIds = new GcDbAccess().sql(query)
      .addBindVar(GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getName())
      .selectList(String.class);
    
    result.addAll(configIds);
    
    query = "select distinct value_string from grouper_aval_asn_asn_stem_v where attribute_def_name_name2 = ?";
    
    configIds = new GcDbAccess().sql(query)
      .addBindVar(GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getName())
      .selectList(String.class);
    
    result.addAll(configIds);
    
    return result;
  }
  
  /**
   * build provisioning attribute object from underlying info
   * @param attributeAssign
   * @return
   */
  private static GrouperProvisioningAttributeValue buildGrouperProvisioningAttributeValue(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperProvisioningAttributeValue result = new GrouperProvisioningAttributeValue();
    result.setTargetName(attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_TARGET).getValueString());
    
    AttributeAssignValue directAssignmentAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT);
    String directAssignmentStr = directAssignmentAssignValue != null ? directAssignmentAssignValue.getValueString(): null;
    boolean directAssignment = BooleanUtils.toBoolean(directAssignmentStr);
    result.setDirectAssignment(directAssignment);
    
    AttributeAssignValue stemScopeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_STEM_SCOPE);
    result.setStemScopeString(stemScopeAssignValue != null ? stemScopeAssignValue.getValueString(): null);
    
    AttributeAssignValue doProvisionAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_DO_PROVISION);
    String doProvisionStr = doProvisionAssignValue != null ? doProvisionAssignValue.getValueString(): null;
    boolean doProvision = BooleanUtils.toBoolean(doProvisionStr);
    result.setDoProvision(doProvision);
    
    AttributeAssignValue lastProvisionedFullMillisAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_LAST_FULL_MILLIS_SINCE_1970);
    result.setLastFullMillisSince1970String(lastProvisionedFullMillisAssignValue != null ? lastProvisionedFullMillisAssignValue.getValueString(): null);
    
    AttributeAssignValue lastProvisionedIncrementalMillisAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_LAST_INCREMENTAL_MILLIS_SINCE_1970);
    result.setLastIncrementalMillisSince1970String(lastProvisionedIncrementalMillisAssignValue != null ? lastProvisionedIncrementalMillisAssignValue.getValueString(): null);
    
    AttributeAssignValue lastProvisionedFullSummaryAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_LAST_FULL_SUMMARY);
    result.setLastFullSummary(lastProvisionedFullSummaryAssignValue != null ? lastProvisionedFullSummaryAssignValue.getValueString(): null);
    
    AttributeAssignValue lastProvisionedIncrementalSummaryAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_LAST_INCREMENTAL_SUMMARY);
    result.setLastIncrementalSummary(lastProvisionedIncrementalSummaryAssignValue != null ? lastProvisionedIncrementalSummaryAssignValue.getValueString(): null);
    
    AttributeAssignValue ownerStemIdAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_OWNER_STEM_ID);
    result.setOwnerStemId(ownerStemIdAssignValue != null ? ownerStemIdAssignValue.getValueString(): null);
    
    return result;
  }
  
  /**
   * save or update provisioning config for a given grouper object (group/stem)
   * @param grouperProvisioningAttributeValue
   * @param grouperObject
   */
  public static void saveOrUpdateProvisioningAttributes(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue, GrouperObject grouperObject) {
    
    AttributeAssign attributeAssign = getAttributeAssign(grouperObject, grouperProvisioningAttributeValue.getTargetName());
   
    if (attributeAssign == null) {
      if (grouperObject instanceof Group) {
        attributeAssign = ((Group)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else {
        attributeAssign = ((Stem)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      }
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(grouperProvisioningAttributeValue.isDirectAssignment()));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getTargetName());
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DO_PROVISION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(grouperProvisioningAttributeValue.isDoProvision()));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_OWNER_STEM_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.isDirectAssignment() ? null: grouperProvisioningAttributeValue.getOwnerStemId());
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_STEM_SCOPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getStemScopeString());
    
    attributeAssign.saveOrUpdate();
    
    if (grouperObject instanceof Stem && grouperProvisioningAttributeValue.isDirectAssignment()) {
      // delete all the existing attributes on children where the current stem is the owner stem
      deleteAttributesOnAllChildrenWithIndirectConfig((Stem)grouperObject, grouperProvisioningAttributeValue.getTargetName());
      
      GrouperProvisioningAttributeValue valueToSave = GrouperProvisioningAttributeValue.copy(grouperProvisioningAttributeValue);
      valueToSave.setOwnerStemId(((Stem)grouperObject).getId());
      valueToSave.setDirectAssignment(false);
      saveOrUpdateProvisioningAttributesOnChildren((Stem)grouperObject, valueToSave, grouperProvisioningAttributeValue.getStemScope());
    }
    
  }
  
  /**
   * find provisioning config in the parent hierarchy for a given grouper object for all targets (ldap, box, etc) and assign that config to this grouper object.
   * @param grouperObject
   */
  public static void copyConfigFromParent(final GrouperObject grouperObject) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Map<String, GrouperProvisioningTarget> targetNames = GrouperProvisioningSettings.getTargets(true);
        
        for (String targetName: targetNames.keySet()) {
          copyConfigFromParent(grouperObject, targetName);
        }
        
        return null;
        
      }
      
    });
    
  }
  
  /**
   * find provisioning config in the parent hierarchy for a given grouper object and target. Assign that config to the given grouper object
   * @param grouperObject
   * @param targetName
   */
  public static void copyConfigFromParent(GrouperObject grouperObject, String targetName) {
    
    //don't do this now
    if (GrouperCheckConfig.isInCheckConfig() || !GrouperProvisioningSettings.provisioningInUiEnabled()) {
      return;
    }
    
    if (grouperObject instanceof Stem && ((Stem) grouperObject).isRootStem()) {
      return;
    }
    
    deleteAttributeAssign(grouperObject, targetName);
    
    // if we changed from direct to indirect, we need to go through all the children
    // and delete metadata on them that were inheriting from this stem.
    if (grouperObject instanceof Stem) {
      deleteAttributesOnAllChildrenWithIndirectConfig((Stem)grouperObject, targetName);
    }
    
    Stem parent = grouperObject.getParentStem();
    
    if(parent.isRootStem()) {
      return;
    }
    
    GrouperProvisioningAttributeValue savedValue = null;
    
    int distanceFromParent = 1;
    
    while (parent != null) {
      
      GrouperProvisioningAttributeValue attributeValue = getProvisioningAttributeValue(parent, targetName);
      
      if (attributeValue != null && attributeValue.isDirectAssignment()) {
        
        if (attributeValue.getStemScope() == Stem.Scope.SUB || (attributeValue.getStemScope() == Stem.Scope.ONE && distanceFromParent < 2 )) {
          savedValue = new GrouperProvisioningAttributeValue();
          savedValue.setDirectAssignment(false);
          savedValue.setDoProvision(attributeValue.isDoProvision());
          savedValue.setOwnerStemId(parent.getId());
          savedValue.setStemScopeString(attributeValue.getStemScopeString());
          savedValue.setTargetName(attributeValue.getTargetName());
          saveOrUpdateProvisioningAttributes(savedValue, grouperObject);
          break;
        }
        
      }
      
      parent = parent.getParentStem();
      distanceFromParent++;
      
      if (parent.isRootStem()) {
        break;
      }
      
    }
    
    // if it's a stem where we changed from direct to indirect, we need to go through all the children of that stem and update the attributes with some parent's metadata if there
    if (grouperObject instanceof Stem && savedValue != null) {
      saveOrUpdateProvisioningAttributesOnChildren((Stem)grouperObject, savedValue, savedValue.getStemScope());
    }
    
  }

  /**
   * save provisioning attributes on children of a given stem
   * @param parentStem
   * @param valueToSave
   * @param scope
   */
  private static void saveOrUpdateProvisioningAttributesOnChildren(Stem parentStem, GrouperProvisioningAttributeValue valueToSave, Scope scope) {
    
    Set<String> childrenStemIds = new HashSet<String>();
    
    for (Stem stem: parentStem.getChildStems(scope)) {
      childrenStemIds.add(stem.getId());
    }
    
    Set<GrouperObject> children = new HashSet<GrouperObject>(parentStem.getChildGroups(scope));
    children.addAll(parentStem.getChildStems(scope));
    
    for (GrouperObject childGrouperObject: children) {
      boolean shouldSaveForThisChild = true;
      
      GrouperProvisioningAttributeValue mayBeGroupTypeAttributeValue = getProvisioningAttributeValue(childGrouperObject, valueToSave.getTargetName());
      if (mayBeGroupTypeAttributeValue != null) {
        
        if (mayBeGroupTypeAttributeValue.isDirectAssignment()) {
          shouldSaveForThisChild = false;
          continue;
        }
        
        String ownerStemId = mayBeGroupTypeAttributeValue.getOwnerStemId();

        // some child of parentStem's settings are already configured on this group/stem, we don't need to update because we will increase the distance otherwise
        if (childrenStemIds.contains(ownerStemId)) {
          shouldSaveForThisChild = false;
        }
        
      }
      
      if (shouldSaveForThisChild) {
        saveOrUpdateProvisioningAttributes(valueToSave, childGrouperObject);
      }
      
    }
  }
  
  /**
   * is given target editable for given subject and grouper object
   * @param target
   * @param subject
   * @param grouperObject
   * @return
   */
  public static boolean isTargetEditable(GrouperProvisioningTarget target, Subject subject, GrouperObject grouperObject) {
    
    boolean readOnly = target.isReadOnly();
    
    if(readOnly) {
      return false;
    }
    
    boolean isEditable = true;
    
    if (grouperObject instanceof Stem) {
      isEditable = isTargetEditableForStem(target, (Stem)grouperObject);
    } else {
      isEditable = !target.isAllowAssignmentsOnlyOnOneStem();
    }
    
    return isEditable && isTargetEditableBySubject(target, subject);
  }
  
  /**
   * delete all the attribute assigns where the config doesn't exist
   */
  public static void deleteInvalidConfigs() {
    Set<String> assignedTargets = getDistinctProvisionerConfigIds();
    Map<String, GrouperProvisioningTarget> validTargets = GrouperProvisioningSettings.getTargets(false);
    
    Set<String> targetsToRemove = new HashSet<String>(assignedTargets);
    targetsToRemove.removeAll(validTargets.keySet());
    
    for (String targetToRemove: targetsToRemove) {
      
      Map<String, AttributeAssign> idToAttributeAssignMap = new AttributeAssignFinder().assignAttributeCheckReadOnAttributeDef(false)
      .assignIdOfAttributeDefNameOnAssignment0(GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getId())
      .assignAttributeValuesOnAssignment0(GrouperUtil.toSet(targetToRemove))
      .addAttributeDefNameId(GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase().getId())
      .findAttributeAssignFinderResults().getIdToAttributeAssignMap();
      
      for (AttributeAssign attributeAssign: idToAttributeAssignMap.values()) {
        attributeAssign.delete();
      }
            
    }
  }
  
  /**
   * get number of groups in a provisioning target that are in a given stem
   * @param stemId
   * @param targetName
   * @return
   */
  public static long retrieveNumberOfGroupsInTargetInStem(String stemId, String targetName) {
    
    String query = "select count(gsg.id) from grouper_sync_group gsg, grouper_sync gs, grouper_groups gg, grouper_stem_set gss " + 
        "where gss.then_has_stem_id = ? " + 
        "and gsg.provisionable = 'T' " + 
        "and gsg.group_id = gg.id " + 
        "and gs.provisioner_name = ? " + 
        "and gs.id = gsg.grouper_sync_id " + 
        "and gg.parent_stem = gss.if_has_stem_id";
    
    long groupCount = new GcDbAccess().sql(query)
      .addBindVar(stemId)
      .addBindVar(targetName)
      .select(Long.class);
    
    return groupCount;
  }
  
  /**
   * get number of groups in a provisioning target that also contain the given member
   * @param stemId
   * @param targetName
   * @return
   */
  public static long retrieveNumberOfGroupsInTargetInMember(String memberId, String targetName) {
    
    String query = "select count(distinct(gm.owner_group_id)) from grouper_sync gs, grouper_memberships gm, grouper_sync_member gsm " + 
        "where gsm.member_id = ? " + 
        "and gsm.provisionable = 'T' " + 
        "and gm.member_id = gsm.member_id " +
        "and gs.provisioner_name = ? " + 
        "and gs.id = gsm.grouper_sync_id ";
    
    long groupCount = new GcDbAccess().sql(query)
      .addBindVar(memberId)
      .addBindVar(targetName)
      .select(Long.class);
    
    return groupCount;
  }
  
  /**
   * get number of users in a provisioning target that are in a given stem
   * @param stemId
   * @param targetName
   * @return
   */
  public static long retrieveNumberOfUsersInTargetInStem(String stemId, String targetName) {
    
    String query = "select count(distinct(gsmember.id)) from grouper_sync_group gsg, grouper_sync gs, grouper_groups gg, grouper_stem_set gss, " + 
        "grouper_sync_membership gsm, grouper_sync_member gsmember " + 
        "where gss.then_has_stem_id = ? " + 
        "and gsg.provisionable = 'T' " + 
        "and gsmember.provisionable = 'T' " + 
        "and gsg.group_id = gg.id " + 
        "and gs.provisioner_name = ? " + 
        "and gs.id = gsg.grouper_sync_id " + 
        "and gg.parent_stem = gss.if_has_stem_id " + 
        "and gsm.grouper_sync_group_id = gsg.id " + 
        "and gsm.grouper_sync_member_id = gsmember.id";
    
    long usersCount = new GcDbAccess().sql(query)
      .addBindVar(stemId)
      .addBindVar(targetName)
      .select(Long.class);
    
    return usersCount;
  }
  
  /**
   * get number of users in a provisioning target that are in a given group
   * @param stemId
   * @param targetName
   * @return
   */
  public static long retrieveNumberOfUsersInTargetInGroup(String groupId, String targetName) {
    
    String query = "select count(distinct(gsmember.id)) from grouper_sync_group gsg, grouper_sync gs, grouper_group_set gg, " + 
        "grouper_sync_membership gsm, grouper_sync_member gsmember " +
        "where gg.owner_group_id = ? " +
        "and gsg.provisionable = 'T' " +
        "and gsmember.provisionable = 'T' " +
        "and gsg.group_id = gg.owner_group_id " +
        "and gs.provisioner_name = ? " +
        "and gs.id = gsg.grouper_sync_id " +
        "and gsm.grouper_sync_group_id = gsg.id " +
        "and gsm.grouper_sync_member_id = gsmember.id";
    
    long usersCount = new GcDbAccess().sql(query)
      .addBindVar(groupId)
      .addBindVar(targetName)
      .select(Long.class);
    
    return usersCount;
  }
  
  /**
   * get number of memberships in a provisioning target that are in a given stem
   * @param stemId
   * @param targetName
   * @return
   */
  public static long retrieveNumberOfMembershipsInTargetInStem(String stemId, String targetName) {
    
    String query = "select count(gsm.id) from grouper_sync_group gsg, grouper_sync gs, grouper_groups gg, grouper_stem_set gss, " + 
        "grouper_sync_membership gsm " + 
        "where gss.then_has_stem_id = ? " + 
        "and gsg.group_id = gg.id " + 
        "and gsg.provisionable = 'T' " + 
        "and gs.provisioner_name = ? " + 
        "and gs.id = gsg.grouper_sync_id " + 
        "and gg.parent_stem = gss.if_has_stem_id " + 
        "and gsm.grouper_sync_group_id = gsg.id";
    
    long membershipsCount = new GcDbAccess().sql(query)
      .addBindVar(stemId)
      .addBindVar(targetName)
      .select(Long.class);
    
    return membershipsCount;
  }
  
  /**
   * get gc grouper sync members for a given member id
   * @param memberId
   * @return
   */
  public static List<GcGrouperSyncMember> retrieveGcGrouperSyncMembers(String memberId) {
    
    String grouperSyncMemberQuery = "select * from grouper_sync_member gsm " + 
        "where gsm.provisionable = 'T' " +
        "and gsm.member_id = ?";
    
    String grouperSyncQuery = "select * from grouper_sync gs where id = ? ";
    
    List<GcGrouperSyncMember> grouperSyncMembers = new GcDbAccess().sql(grouperSyncMemberQuery)
        .addBindVar(memberId)
        .selectList(GcGrouperSyncMember.class);
    
    for (GcGrouperSyncMember grouperSyncMember : GrouperUtil.nonNull(grouperSyncMembers)) {
      String grouperSyncId = grouperSyncMember.getGrouperSyncId();
      
      //GcGrouperSync gcGrouperSync = grouperSyncMember.getGrouperSync();
      
      GcGrouperSync gcGrouperSync = new GcDbAccess().sql(grouperSyncQuery)
      .addBindVar(grouperSyncId)
      .select(GcGrouperSync.class);
      
      grouperSyncMember.setGrouperSync(gcGrouperSync);
    }
    
    return grouperSyncMembers;
    
  }
  
  /**
   * get gc grouper sync memberships for a given member id and group id
   * @param memberId
   * @param groupId
   * @return
   */
  public static List<GcGrouperSyncMembership> retrieveGcGrouperSyncMemberships(String memberId, String groupId) {
    
    String grouperSyncMembershipQuery = "select gsm.* from grouper_sync_membership gsm, grouper_sync_group gsg, grouper_sync_member gsmem " + 
        "where gsm.grouper_sync_group_id =  gsg.id " + 
        " and gsm.grouper_sync_member_id = gsmem.id "
        + " and gsmem.member_id = ? " +
        " and gsg.group_id = ? ";
    
    String grouperSyncQuery = "select * from grouper_sync gs where id = ? ";
    
    List<GcGrouperSyncMembership> grouperSyncMemberships = new GcDbAccess().sql(grouperSyncMembershipQuery)
        .addBindVar(memberId)
        .addBindVar(groupId)
        .selectList(GcGrouperSyncMembership.class);
    
    for (GcGrouperSyncMembership grouperSyncMembership : GrouperUtil.nonNull(grouperSyncMemberships)) {
      String grouperSyncId = grouperSyncMembership.getGrouperSyncId();
      
      GcGrouperSync gcGrouperSync = new GcDbAccess().sql(grouperSyncQuery)
      .addBindVar(grouperSyncId)
      .select(GcGrouperSync.class);
      
      grouperSyncMembership.setGrouperSync(gcGrouperSync);
    }
    
    return grouperSyncMemberships;
    
  }
  
  private static boolean isTargetEditableForStem(GrouperProvisioningTarget target, Stem stem) {
    
    boolean allowAssignOnOneStem = target.isAllowAssignmentsOnlyOnOneStem();
    
    if (allowAssignOnOneStem) { //can not edit if this target is already assigned to another stem
      
      List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
          .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("true")
          .assignNameOfAttributeDefName2(provisioningConfigStemName()+":"+PROVISIONING_TARGET).addAttributeValuesOnAssignment2(target.getName())
          .findStems());
      
      if (stems.size() > 1) {
        return false;
      }
      
      if (stems.size() == 1 && !stems.get(0).getId().equals(stem.getId())) {
        return false; // stem we are working with is not the same it's already assigned to therefore not editable
      }
      
    }
    
    return true;
  }
  
  /**
   * can the given subject edit the given target
   * @param target
   * @param subject
   * @return
   */
  private static boolean isTargetEditableBySubject(GrouperProvisioningTarget target, Subject subject) {
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    String groupAllowedToAssign = target.getGroupAllowedToAssign();
    if (StringUtils.isBlank(groupAllowedToAssign)) {
      return PrivilegeHelper.isWheelOrRoot(subject); // only grouper system admin is allowed when no specific group is allowed to assign the given target
    }
    
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupAllowedToAssign, false);
    if (group == null) {
      try { // try looking up group by id
        Long groupId = Long.valueOf(groupAllowedToAssign);
        group = GroupFinder.findByIdIndexSecure(groupId, false, new QueryOptions());
        if (group == null) {
          throw new RuntimeException(groupAllowedToAssign+" is not a valid group id or group name");
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(groupAllowedToAssign+" is not a valid group id or group name");
      }
     
    }
    
    for (Member member: group.getMembers()) {
      Subject groupSubject = member.getSubject();
      if (subject.getId().equals(groupSubject.getId())) {
        return true;
      }
    }
    
    return false;

  }
  
  /**
   * delete provisioning attributes on all the children of a given stem and target
   * @param stem
   * @param targetName
   */
  private static void deleteAttributesOnAllChildrenWithIndirectConfig(Stem stem, String targetName) {
      
      Set<GrouperObject> children = new HashSet<GrouperObject>(stem.getChildGroups(Scope.SUB));
      children.addAll(stem.getChildStems(Scope.SUB));
      
      for (GrouperObject childGrouperObject: children) {
        GrouperProvisioningAttributeValue mayBeGroupTypeAttributeValue = getProvisioningAttributeValue(childGrouperObject, targetName);
        if (mayBeGroupTypeAttributeValue != null) {
          
          if (mayBeGroupTypeAttributeValue.isDirectAssignment()) {
            continue;
          }
          
          String ownerStemId = mayBeGroupTypeAttributeValue.getOwnerStemId();
          if (stem.getId().equals(ownerStemId)) {
            deleteAttributeAssign(childGrouperObject, targetName);
          }
        }
        
      }
      
    }

  /**
   * delete provisioning attributes from a given grouper object and target
   * @param grouperObject
   * @param targetName
   */
  private static void deleteAttributeAssign(GrouperObject grouperObject, String targetName) {
    AttributeAssign currentAttributeAssign = getAttributeAssign(grouperObject, targetName);
    if (currentAttributeAssign != null) {
      currentAttributeAssign.delete();
    }
  }
}
