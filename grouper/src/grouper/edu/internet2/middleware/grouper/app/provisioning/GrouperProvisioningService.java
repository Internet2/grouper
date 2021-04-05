package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperProvisioningService {
  
  
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningService.class);
      
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

//  /**
//   * 
//   * @param gcGrouperSync
//   * @param groupIds
//   * @return the map of group id to group sync for use later on
//   */
//  public static GrouperProvisioningProcessingResult processProvisioningMetadataForGroupIds(GcGrouperSync gcGrouperSync, Collection<String> groupIds) {
//    
//    GrouperProvisioningProcessingResult grouperProvisioningProcessingResult = new GrouperProvisioningProcessingResult();
//    
//    grouperProvisioningProcessingResult.setGcGrouperSync(gcGrouperSync);
//    
//    if (GrouperUtil.length(groupIds) == 0) {
//      return grouperProvisioningProcessingResult;
//    }
//    
//    // get the provisioned groups from these group ids
//    Map<String, Group> groupIdToProvisionedGroupMap = GrouperProvisioningService.findAllGroupsForTargetAndGroupIds(gcGrouperSync.getProvisionerName(), groupIds);
//    grouperProvisioningProcessingResult.setGroupIdGroupMap(groupIdToProvisionedGroupMap);
//    
//    Map<String, GcGrouperSyncGroup> groupIdToGroupSyncMap = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIds);
//    grouperProvisioningProcessingResult.setGroupIdToGcGrouperSyncGroupMap(groupIdToGroupSyncMap);
//    
//    List<GcGrouperSyncGroup> gcGrouperSyncGroupsToUpdate = new ArrayList<GcGrouperSyncGroup>();
//    List<GcGrouperSyncGroup> gcGrouperSyncGroupsToInsert = new ArrayList<GcGrouperSyncGroup>();
//    
//    List<String> groupIdsToAddToTarget = new ArrayList<String>();
//    grouperProvisioningProcessingResult.setGroupIdsToAddToTarget(groupIdsToAddToTarget);
//
//    List<String> groupIdsToRemoveFromTarget = new ArrayList<String>();
//    grouperProvisioningProcessingResult.setGroupIdsToRemoveFromTarget(groupIdsToRemoveFromTarget);
//
//    for (String groupId : groupIdToGroupSyncMap.keySet()) {
//      
//      GcGrouperSyncGroup gcGrouperSyncGroup = groupIdToGroupSyncMap.get(groupId);
//
//      Group group = groupIdToProvisionedGroupMap.get(groupId);
//      if (group == null) {
//        //the group is not supposed to be provisioned
//        
//        // its in target and shouldnt be
//        if (gcGrouperSyncGroup.isInTarget()) {
//          groupIdsToRemoveFromTarget.add(groupId);
//        }
//        
//        //it should not be provisioned
//        if (gcGrouperSyncGroup.isProvisionable()) {
//          gcGrouperSyncGroup.setProvisionable(false);
//          gcGrouperSyncGroup.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
//          gcGrouperSyncGroupsToUpdate.add(gcGrouperSyncGroup);
//        }
//      } else {
//        
//        // the group should be provisionable
//        // its not in target and should be
//        if (!gcGrouperSyncGroup.isInTarget()) {
//          groupIdsToAddToTarget.add(groupId);
//        }
//        
//        boolean needsUpdate = false;
//        
//        // update some metadata
//        if (!StringUtils.equals(group.getName(), gcGrouperSyncGroup.getGroupName())) {
//          gcGrouperSyncGroup.setGroupName(group.getName());
//          needsUpdate = true;
//        }
//        // update some metadata
//        if (!GrouperUtil.equals(group.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex())) {
//          gcGrouperSyncGroup.setGroupIdIndex(group.getIdIndex());
//          needsUpdate = true;
//        }
//        
//        //it is not be provisioned, but should be
//        if (!gcGrouperSyncGroup.isProvisionable()) {
//          gcGrouperSyncGroup.setProvisionable(true);
//          gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
//          gcGrouperSyncGroup.setProvisionableEnd(null);
//          needsUpdate = true;
//        }
//
//        if (needsUpdate) {
//          gcGrouperSyncGroupsToUpdate.add(gcGrouperSyncGroup);
//        }
//      }
//    }
//    
//    // find the ones where tracking objects dont exist
//    for (String groupId : groupIdToProvisionedGroupMap.keySet()) {
//
//      Group group = groupIdToProvisionedGroupMap.get(groupId);
//
//      GcGrouperSyncGroup gcGrouperSyncGroup = groupIdToGroupSyncMap.get(groupId);
//
//      // these are not tracked... start tracking them
//      if (gcGrouperSyncGroup == null) {
//    
//        // this is not provisionable or in target
//        gcGrouperSyncGroup = new GcGrouperSyncGroup();
//        gcGrouperSyncGroup.setGrouperSync(gcGrouperSync);
//        gcGrouperSyncGroup.setGroupId(groupId);
//        gcGrouperSyncGroup.setGroupIdIndex(group.getIdIndex());
//        gcGrouperSyncGroup.setGroupName(group.getName());
//        gcGrouperSyncGroup.setProvisionable(true);
//        gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
//        groupIdToGroupSyncMap.put(groupId, gcGrouperSyncGroup);
//        gcGrouperSyncGroupsToInsert.add(gcGrouperSyncGroup);
//        groupIdsToAddToTarget.add(groupId);
//      }
//      
//    }
//    
//    return grouperProvisioningProcessingResult;
//  }
  
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
   * retrieve type setting for a given member and target name.
   * @param member
   * @param targetName
   * @return
   */
  public static GrouperProvisioningAttributeValue getProvisioningAttributeValue(Member member, String targetName) {
    
    Set<AttributeAssign> attributeAssigns = member.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    
    AttributeAssign attributeAssign = getAttributeAssign(attributeAssigns, targetName);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildGrouperProvisioningAttributeValue(attributeAssign);
  }
  
  /**
   * retrieve all the configured provisioning attributes for a given member
   * @param member
   * @return
   */
  public static List<GrouperProvisioningAttributeValue> getProvisioningAttributeValues(Member member) {
    
    final List<GrouperProvisioningAttributeValue> result = new ArrayList<GrouperProvisioningAttributeValue>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Map<String, GrouperProvisioningTarget> targetNames = GrouperProvisioningSettings.getTargets(true);
        
        for (String targetName: targetNames.keySet()) {
          GrouperProvisioningAttributeValue value = getProvisioningAttributeValue(member, targetName);
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
   * retrieve type setting for a given membership and target name.
   * @param group
   * @param member
   * @param targetName
   * @return
   */
  public static GrouperProvisioningAttributeValue getProvisioningAttributeValue(Group group, Member member, String targetName) {
    
    Set<AttributeAssign> attributeAssigns = group.getAttributeDelegateEffMship(member).retrieveAssignments(retrieveAttributeDefNameBase());
    
    AttributeAssign attributeAssign = getAttributeAssign(attributeAssigns, targetName);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildGrouperProvisioningAttributeValue(attributeAssign);
  }
  
  /**
   * retrieve all the configured provisioning attributes for a given membership
   * @param group
   * @param member
   * @return
   */
  public static List<GrouperProvisioningAttributeValue> getProvisioningAttributeValues(Group group, Member member) {
    
    final List<GrouperProvisioningAttributeValue> result = new ArrayList<GrouperProvisioningAttributeValue>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Map<String, GrouperProvisioningTarget> targetNames = GrouperProvisioningSettings.getTargets(true);
        
        for (String targetName: targetNames.keySet()) {
          GrouperProvisioningAttributeValue value = getProvisioningAttributeValue(group, member, targetName);
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
    
    return getAttributeAssign(attributeAssigns, targetName);
  }
  
  
  private static AttributeAssign getAttributeAssign(Set<AttributeAssign> attributeAssigns, String targetName) {
    
    for (AttributeAssign attributeAssign: GrouperUtil.nonNull(attributeAssigns)) {
      
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
    String doProvision = doProvisionAssignValue != null ? doProvisionAssignValue.getValueString(): null;
    result.setDoProvision(doProvision);
    
    AttributeAssignValue ownerStemIdAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_OWNER_STEM_ID);
    result.setOwnerStemId(ownerStemIdAssignValue != null ? ownerStemIdAssignValue.getValueString(): null);
    
    AttributeAssignValue provisioningObjectMetadataJson = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_METADATA_JSON);
    if (provisioningObjectMetadataJson != null && StringUtils.isNotBlank(provisioningObjectMetadataJson.getValueString())) {
//      GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata = GrouperProvisioningObjectMetadata.buildGrouperProvisioningObjectMetadataFromJsonString(provisioningObjectMetadataJson.getValueString());
//      result.setGrouperProvisioningObjectMetadata(grouperProvisioningObjectMetadata);
      try {
        Map<String, Object> metadataNameValues = GrouperProvisioningSettings.objectMapper
            .readValue(provisioningObjectMetadataJson.getValueString(), Map.class);
        result.setMetadataNameValues(metadataNameValues);
      } catch(Exception e) {
        throw new RuntimeException("could not convert json string" +provisioningObjectMetadataJson.getValueString() + " to Map object", e);
      }
      
      
    }
    
    return result;
  }
  
  private static boolean grouperProvisioningAttributeValuesDifferent(GrouperProvisioningAttributeValue one, 
      GrouperProvisioningAttributeValue two) {
    
    if (one == null && two == null) return false;
    if (one == null || two == null) return true;
    
    if (!StringUtils.equals(one.getStemScopeString(), two.getStemScopeString())) {
      return true;
    }
    
    if (one.isDirectAssignment() != two.isDirectAssignment()) {
      return true;
    }
    
    if (!StringUtils.equals(one.getDoProvision(), two.getDoProvision())) {
      return true;
    }
    
    if (!StringUtils.equals(one.getOwnerStemId(), two.getOwnerStemId())) {
      return true;
    }
    
    if (one.getMetadataNameValues() != null && two.getMetadataNameValues() == null) return true;
    if (one.getMetadataNameValues() == null && two.getMetadataNameValues() != null) return true;
    if (one.getMetadataNameValues() != null && two.getMetadataNameValues() != null) {
      return !one.getMetadataNameValues().equals(two.getMetadataNameValues());
    }
    
    return false;
  }
  
  /**
   * save or update provisioning config for a given member
   * @param grouperProvisioningAttributeValue
   * @param member
   */
  public static void saveOrUpdateProvisioningAttributes(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue, Member member) {
    
    Set<AttributeAssign> attributeAssigns = member.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    
    AttributeAssign attributeAssign = getAttributeAssign(attributeAssigns, grouperProvisioningAttributeValue.getTargetName());
    if (attributeAssign == null) {
      attributeAssign = member.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getTargetName());

    Map<String, Object> metadataNameValues = grouperProvisioningAttributeValue.getMetadataNameValues();
    if (metadataNameValues != null) {
      attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_METADATA_JSON, true);
      try {
        String metadataItemsAsString = GrouperProvisioningSettings.objectMapper.writeValueAsString(metadataNameValues);
        attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), metadataItemsAsString);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("could not convert map into json string", e);
      }
    }
    
    attributeAssign.saveOrUpdate();
  }
  
  /**
   * save or update provisioning config for a given group, member
   * @param grouperProvisioningAttributeValue
   * @param membership
   */
  public static void saveOrUpdateProvisioningAttributes(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue, Group group, Member member) {
    
    Set<AttributeAssign> attributeAssigns = group.getAttributeDelegateEffMship(member).retrieveAssignments(retrieveAttributeDefNameBase());
    
    AttributeAssign attributeAssign = getAttributeAssign(attributeAssigns, grouperProvisioningAttributeValue.getTargetName());
    if (attributeAssign == null) {
      attributeAssign = group.getAttributeDelegateEffMship(member).addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getTargetName());

    Map<String, Object> metadataNameValues = grouperProvisioningAttributeValue.getMetadataNameValues();
    if (metadataNameValues != null) {
      attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_METADATA_JSON, true);
      try {
        String metadataItemsAsString = GrouperProvisioningSettings.objectMapper.writeValueAsString(metadataNameValues);
        attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), metadataItemsAsString);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("could not convert map into json string", e);
      }
    }
    
    attributeAssign.saveOrUpdate();
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
    } else {
      
      GrouperProvisioningAttributeValue existingGrouperProvisioningAttributeValue = buildGrouperProvisioningAttributeValue(attributeAssign);
      boolean newValueDifferentFromOldValue = grouperProvisioningAttributeValuesDifferent(grouperProvisioningAttributeValue, existingGrouperProvisioningAttributeValue);
      if (!newValueDifferentFromOldValue) return;
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(grouperProvisioningAttributeValue.isDirectAssignment()));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getTargetName());
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DO_PROVISION, true);
    if (grouperProvisioningAttributeValue.getDoProvision() == null) {
      attributeAssign.getAttributeDelegate().removeAttribute(attributeDefName);
    } else {
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getDoProvision());
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_OWNER_STEM_ID, true);
    if (grouperProvisioningAttributeValue.getOwnerStemId() == null) {
      attributeAssign.getAttributeDelegate().removeAttribute(attributeDefName);
    } else {
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getOwnerStemId());
    }
    
    Map<String, Object> metadataNameValues = grouperProvisioningAttributeValue.getMetadataNameValues();
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_METADATA_JSON, true);
    if (metadataNameValues != null && metadataNameValues.size() > 0) {
      try {
        String metadataItemsAsString = GrouperProvisioningSettings.objectMapper.writeValueAsString(metadataNameValues);
        attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), metadataItemsAsString);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("could not convert map into json string", e);
      }
    } else {
      attributeAssign.getAttributeDelegate().removeAttribute(attributeDefName);
    }
    
    if (grouperProvisioningAttributeValue.isDirectAssignment() && grouperObject instanceof Stem) {
      attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_STEM_SCOPE, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getStemScopeString());
    }
    
    attributeAssign.saveOrUpdate();
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
          savedValue.setDoProvision(attributeValue.getDoProvision());
          savedValue.setOwnerStemId(parent.getId());
          savedValue.setTargetName(attributeValue.getTargetName());
          savedValue.setMetadataNameValues(attributeValue.getMetadataNameValues());
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
    
    if (grouperObject != null) {
      if (grouperObject instanceof Stem) {
        isEditable = isTargetEditableForStem(target, (Stem)grouperObject);
      } else {
        isEditable = !target.isAllowAssignmentsOnlyOnOneStem();
      }
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
      
      Set<AttributeAssign> assignments = GrouperDAOFactory.getFactory().getAttributeAssign().findByAttributeDefNameAndValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getId(), targetToRemove, null);
      for (AttributeAssign assignment : assignments) {
        assignment.getOwnerAttributeAssign().delete();
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
   * retrieve recent activity for all the groups for a given provisioner name
   * @param provisionerName
   * @return
   */
  public static List<GcGrouperSyncGroup> retrieveRecentActivityForGroup(String provisionerName) {

    List<GcGrouperSyncGroup> gcGrouperSyncGroups = new ArrayList<GcGrouperSyncGroup>();
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerName);
    
    if (gcGrouperSync == null) {
      return gcGrouperSyncGroups;
    }
    
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(QueryPaging.page(200, 1, false));
    queryOptions.sort(QuerySort.desc("inTargetStart"));
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroupsInTargetStart = HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery("from GcGrouperSyncGroup where grouperSyncId = :theGrouperSyncId and inTargetStart is not null")
      .setString("theGrouperSyncId", gcGrouperSync.getId())
      .list(GcGrouperSyncGroup.class);
    
    gcGrouperSyncGroups.addAll(gcGrouperSyncGroupsInTargetStart);
    
    queryOptions = new QueryOptions();
    queryOptions.paging(QueryPaging.page(200, 1, false));
    queryOptions.sort(QuerySort.desc("inTargetEnd"));
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroupsInTargetEnd = HibernateSession.byHqlStatic().options(queryOptions)
        .createQuery("from GcGrouperSyncGroup where grouperSyncId = :theGrouperSyncId and inTargetEnd is not null")
        .setString("theGrouperSyncId", gcGrouperSync.getId())
        .list(GcGrouperSyncGroup.class);
    
    gcGrouperSyncGroups.addAll(gcGrouperSyncGroupsInTargetEnd);
    
    // remove duplicates
    Set<String> grouperSyncGroupIds = new HashSet<String>();
    List<GcGrouperSyncGroup> uniqueGrouperSyncGroups = new ArrayList<GcGrouperSyncGroup>();
    
    for (GcGrouperSyncGroup gcGrouperSyncGroup: gcGrouperSyncGroups) {
      if (!grouperSyncGroupIds.contains(gcGrouperSyncGroup.getId())) {
        uniqueGrouperSyncGroups.add(gcGrouperSyncGroup);
        grouperSyncGroupIds.add(gcGrouperSyncGroup.getId());
      }
    }
    
    
    Collections.sort(uniqueGrouperSyncGroups, new Comparator<GcGrouperSyncGroup>() {

      @Override
      public int compare(GcGrouperSyncGroup o1, GcGrouperSyncGroup o2) {
        
        long o1TimestampLong = o1.getInTargetStart() == null ? o1.getInTargetEnd().getTime()
            : (o1.getInTargetEnd() == null ? o1.getInTargetStart().getTime(): Math.max(o1.getInTargetStart().getTime(), o1.getInTargetEnd().getTime()));
        
        long o2TimestampLong = o2.getInTargetStart() == null ? o2.getInTargetEnd().getTime()
            : (o2.getInTargetEnd() == null ? o2.getInTargetStart().getTime(): Math.max(o2.getInTargetStart().getTime(), o2.getInTargetEnd().getTime()));
        
        if (o2TimestampLong == o1TimestampLong) return 0;
        if (o2TimestampLong > o1TimestampLong) return 1;
        return -1;
      }
      
    });
    
    return uniqueGrouperSyncGroups;
    
  }
  
  /**
   * retrieve recent activity for all the members for a given provisioner name
   * @param provisionerName
   * @return
   */
  public static List<GcGrouperSyncMember> retrieveRecentActivityForMember(String provisionerName) {

    List<GcGrouperSyncMember> gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerName);
    
    if (gcGrouperSync == null) {
      return gcGrouperSyncMembers;
    }
    
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(QueryPaging.page(200, 1, false));
    queryOptions.sort(QuerySort.desc("inTargetStart"));
    
    List<GcGrouperSyncMember> gcGrouperSyncMembersInTargetStart = HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery("from GcGrouperSyncMember where grouperSyncId = :theGrouperSyncId and inTargetStart is not null")
      .setString("theGrouperSyncId", gcGrouperSync.getId())
      .list(GcGrouperSyncMember.class);
    
    gcGrouperSyncMembers.addAll(gcGrouperSyncMembersInTargetStart);
    
    queryOptions = new QueryOptions();
    queryOptions.paging(QueryPaging.page(200, 1, false));
    queryOptions.sort(QuerySort.desc("inTargetEnd"));
    
    List<GcGrouperSyncMember> gcGrouperSyncMembersInTargetEnd = HibernateSession.byHqlStatic().options(queryOptions)
        .createQuery("from GcGrouperSyncMember where grouperSyncId = :theGrouperSyncId and inTargetEnd is not null")
        .setString("theGrouperSyncId", gcGrouperSync.getId())
        .list(GcGrouperSyncMember.class);
    
    gcGrouperSyncMembers.addAll(gcGrouperSyncMembersInTargetEnd);
    
    // remove duplicates
    Set<String> grouperSyncMemberIds = new HashSet<String>();
    List<GcGrouperSyncMember> uniqueGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();
    
    for (GcGrouperSyncMember gcGrouperSyncMember: gcGrouperSyncMembers) {
      if (!grouperSyncMemberIds.contains(gcGrouperSyncMember.getId())) {
        uniqueGrouperSyncMembers.add(gcGrouperSyncMember);
        grouperSyncMemberIds.add(gcGrouperSyncMember.getId());
      }
    }
    
    
    Collections.sort(uniqueGrouperSyncMembers, new Comparator<GcGrouperSyncMember>() {

      @Override
      public int compare(GcGrouperSyncMember o1, GcGrouperSyncMember o2) {
        
        long o1TimestampLong = o1.getInTargetStart() == null ? o1.getInTargetEnd().getTime()
            : (o1.getInTargetEnd() == null ? o1.getInTargetStart().getTime(): Math.max(o1.getInTargetStart().getTime(), o1.getInTargetEnd().getTime()));
        
        long o2TimestampLong = o2.getInTargetStart() == null ? o2.getInTargetEnd().getTime()
            : (o2.getInTargetEnd() == null ? o2.getInTargetStart().getTime(): Math.max(o2.getInTargetStart().getTime(), o2.getInTargetEnd().getTime()));
        
        if (o2TimestampLong == o1TimestampLong) return 0;
        if (o2TimestampLong > o1TimestampLong) return 1;
        return -1;
      }
      
    });
    
    return uniqueGrouperSyncMembers;
    
  }
  
  /**
   * retrieve recent activity for all the memberships for a given provisioner name
   * @param provisionerName
   * @return
   */
  public static List<GcGrouperSyncMembership> retrieveRecentActivityForMembership(String provisionerName) {

    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerName);
    
    if (gcGrouperSync == null) {
      return gcGrouperSyncMemberships;
    }
    
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(QueryPaging.page(200, 1, false));
    queryOptions.sort(QuerySort.desc("inTargetStart"));
    
    List<GcGrouperSyncMembership> gcGrouperSyncMembershipsInTargetStart = HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery("from GcGrouperSyncMembership where grouperSyncId = :theGrouperSyncId and inTargetStart is not null")
      .setString("theGrouperSyncId", gcGrouperSync.getId())
      .list(GcGrouperSyncMembership.class);
    
    gcGrouperSyncMemberships.addAll(gcGrouperSyncMembershipsInTargetStart);
    
    queryOptions = new QueryOptions();
    queryOptions.paging(QueryPaging.page(200, 1, false));
    queryOptions.sort(QuerySort.desc("inTargetEnd"));
    
    List<GcGrouperSyncMembership> gcGrouperSyncMembershipsInTargetEnd = HibernateSession.byHqlStatic().options(queryOptions)
        .createQuery("from GcGrouperSyncMembership where grouperSyncId = :theGrouperSyncId and inTargetEnd is not null")
        .setString("theGrouperSyncId", gcGrouperSync.getId())
        .list(GcGrouperSyncMembership.class);
    
    gcGrouperSyncMemberships.addAll(gcGrouperSyncMembershipsInTargetEnd);
    
    // remove duplicates
    Set<String> grouperSyncMembershipIds = new HashSet<String>();
    List<GcGrouperSyncMembership> uniqueGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership: gcGrouperSyncMemberships) {
      if (!grouperSyncMembershipIds.contains(gcGrouperSyncMembership.getId())) {
        uniqueGrouperSyncMemberships.add(gcGrouperSyncMembership);
        grouperSyncMembershipIds.add(gcGrouperSyncMembership.getId());
      }
    }
    
    List<String> grouperSyncGroupIds = new ArrayList<String>();
    List<String> grouperSyncMemberIds = new ArrayList<String>();
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership: uniqueGrouperSyncMemberships) {
      grouperSyncGroupIds.add(gcGrouperSyncMembership.getGrouperSyncGroupId());
      grouperSyncMemberIds.add(gcGrouperSyncMembership.getGrouperSyncMemberId());
    }
    
    Map<String, GcGrouperSyncGroup> idToGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByIds(grouperSyncGroupIds);
    Map<String, GcGrouperSyncMember> idToMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByIds(grouperSyncMemberIds);
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership: uniqueGrouperSyncMemberships) {
      
      if (idToGroup.containsKey(gcGrouperSyncMembership.getGrouperSyncGroupId())) {
        gcGrouperSyncMembership.setGrouperSyncGroup(idToGroup.get(gcGrouperSyncMembership.getGrouperSyncGroupId()));        
      }
      
      if (idToMember.containsKey(gcGrouperSyncMembership.getGrouperSyncMemberId())) {
        gcGrouperSyncMembership.setGrouperSyncMember(idToMember.get(gcGrouperSyncMembership.getGrouperSyncMemberId()));        
      }
      
    }
    
    Collections.sort(uniqueGrouperSyncMemberships, new Comparator<GcGrouperSyncMembership>() {

      @Override
      public int compare(GcGrouperSyncMembership o1, GcGrouperSyncMembership o2) {
        
        long o1TimestampLong = o1.getInTargetStart() == null ? o1.getInTargetEnd().getTime()
            : (o1.getInTargetEnd() == null ? o1.getInTargetStart().getTime(): Math.max(o1.getInTargetStart().getTime(), o1.getInTargetEnd().getTime()));
        
        long o2TimestampLong = o2.getInTargetStart() == null ? o2.getInTargetEnd().getTime()
            : (o2.getInTargetEnd() == null ? o2.getInTargetStart().getTime(): Math.max(o2.getInTargetStart().getTime(), o2.getInTargetEnd().getTime()));
        
        if (o2TimestampLong == o1TimestampLong) return 0;
        if (o2TimestampLong > o1TimestampLong) return 1;
        return -1;
      }
      
    });
    
    return uniqueGrouperSyncMemberships;
    
  }
  
  /**
   * get gc grouper sync members for a given member id
   * @param memberId
   * @return
   */
  public static List<GcGrouperSyncMember> retrieveGcGrouperSyncMembers(String memberId) {
    
    String grouperSyncMemberQuery = "select gsm.* from grouper_sync_member gsm " + 
        "where " +
        " gsm.member_id = ? order by grouper_sync_id";
    
    String grouperSyncQuery = "select gs.* from grouper_sync gs where id = ? ";
    
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
   * retrieve grouper sync group
   * @param groupId
   * @param provsionerName
   * @return
   */
  public static GcGrouperSyncGroup retrieveGcGrouperGroup(String groupId, String provsionerName) {
    
    String sql = "select gsg.* from grouper_sync_group gsg, grouper_sync gs where"
        + " gsg.grouper_sync_id = gs.id and gs.provisioner_name = ? and gsg.group_id = ? ";
    
    GcGrouperSyncGroup grouperSyncGroup = new GcDbAccess().sql(sql)
        .addBindVar(provsionerName)
        .addBindVar(groupId)
        .select(GcGrouperSyncGroup.class);
    
    return grouperSyncGroup;
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
        " and gsg.group_id = ? order by gsm.grouper_sync_id";
    
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
      
      GcGrouperSyncMember grouperSyncMember = GcGrouperSyncDao.retrieveById(null, grouperSyncId)
          .getGcGrouperSyncMemberDao().memberRetrieveById(grouperSyncMembership.getGrouperSyncMemberId());
      grouperSyncMembership.setGrouperSyncMember(grouperSyncMember);
      
      GcGrouperSyncGroup grouperSyncGroup = GcGrouperSyncDao.retrieveById(null, grouperSyncId)
          .getGcGrouperSyncGroupDao().groupRetrieveById(grouperSyncMembership.getGrouperSyncGroupId());
      grouperSyncMembership.setGrouperSyncGroup(grouperSyncGroup);
    }
    
    return grouperSyncMemberships;
    
  }
  
  /**
   * retrieve gc grouper sync logs
   * @param provisionerId
   * @param groupId
   * @param queryOptions
   * @return
   */
  public static List<GcGrouperSyncLog> retrieveGcGrouperSyncLogs(String provisionerId, String groupId, QueryOptions queryOptions) {
    
    List<GcGrouperSyncLog> gcGrouperSyncLogs = new ArrayList<GcGrouperSyncLog>();
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerId);
    
    if (gcGrouperSync == null) {
      return gcGrouperSyncLogs;
    }
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(groupId);
    
    if (gcGrouperSyncGroup == null) {
      return gcGrouperSyncLogs;
    }
    
    if (queryOptions.getQueryPaging() == null) {
      queryOptions.paging(QueryPaging.page(100, 1, false));
    }
    
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sort(QuerySort.desc("lastUpdated"));
    }
    
    gcGrouperSyncLogs = HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery("from GcGrouperSyncLog where grouperSyncId = :theGrouperSyncId and grouperSyncOwnerId = :theGrouperSyncOwnerId")
      .setString("theGrouperSyncId", gcGrouperSync.getId())
      .setString("theGrouperSyncOwnerId", gcGrouperSyncGroup.getId())
      .list(GcGrouperSyncLog.class);
    
    
    return gcGrouperSyncLogs;
  }
  
  /**
   * retrieve gc grouper sync jobs for a provisioner id
   * @param provisionerId
   * @return
   */
  public static List<GcGrouperSyncJob> retrieveGcGroupSyncJobs(String provisionerId) {
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerId);
    
    if (gcGrouperSync == null) {
      return new ArrayList<GcGrouperSyncJob>();
    }
    
    return gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveAll();
  }
  
  /**
   * retrieve gc grouper sync logs for a provisioner id
   * @param provisionerId
   * @param queryOptions
   * @return
   */
  public static List<GrouperSyncLogWithOwner> retrieveGcGrouperSyncLogs(String provisionerId, QueryOptions queryOptions) {

    List<GrouperSyncLogWithOwner> grouperSyncLogsWithOwner = new ArrayList<GrouperSyncLogWithOwner>();
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, provisionerId);
    
    if (gcGrouperSync == null) {
      return grouperSyncLogsWithOwner;
    }
    
    if (queryOptions.getQueryPaging() == null) {
      queryOptions.paging(QueryPaging.page(100, 1, false));
    }
    
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sort(QuerySort.desc("lastUpdated"));
    }
    
    List<GcGrouperSyncLog> gcGrouperSyncLogs = HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery("from GcGrouperSyncLog where grouperSyncId = :theGrouperSyncId")
      .setString("theGrouperSyncId", gcGrouperSync.getId())
      .list(GcGrouperSyncLog.class);
    
    Set<String> ownerIds = new HashSet<String>();
    
    for (GcGrouperSyncLog gcGrouperSyncLog: gcGrouperSyncLogs) {

      if (StringUtils.isNotBlank(gcGrouperSyncLog.getGrouperSyncOwnerId())) {
        ownerIds.add(gcGrouperSyncLog.getGrouperSyncOwnerId());
      }
    }
    
    Map<String, GcGrouperSyncJob> gcGrouperSyncJobs = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveByIds(ownerIds);
    Map<String, GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByIds(ownerIds);
    Map<String, GcGrouperSyncMember> gcGrouperSyncMembers = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByIds(ownerIds);
    
    
    for (GcGrouperSyncLog gcGrouperSyncLog: gcGrouperSyncLogs) {
      
      GrouperSyncLogWithOwner grouperSyncLogWithOwner = new GrouperSyncLogWithOwner();
      grouperSyncLogsWithOwner.add(grouperSyncLogWithOwner);
      
      grouperSyncLogWithOwner.setGcGrouperSyncLog(gcGrouperSyncLog);
      
      if (StringUtils.isNotBlank(gcGrouperSyncLog.getGrouperSyncOwnerId())) {
        if (gcGrouperSyncJobs.containsKey(gcGrouperSyncLog.getGrouperSyncOwnerId())) {
          GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSyncJobs.get(gcGrouperSyncLog.getGrouperSyncOwnerId());
          grouperSyncLogWithOwner.setGcGrouperSyncJob(gcGrouperSyncJob);
          
          String logType = GrouperTextContainer.textOrNull("provisionerLogsTypeOfLogJob");
          if (StringUtils.isBlank(logType)) {
            logType = "Job";
          }
          
          grouperSyncLogWithOwner.setLogType(logType);
          continue;
        }
        
        if (gcGrouperSyncGroups.containsKey(gcGrouperSyncLog.getGrouperSyncOwnerId())) {
          GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSyncGroups.get(gcGrouperSyncLog.getGrouperSyncOwnerId());
          grouperSyncLogWithOwner.setGcGrouperSyncGroup(gcGrouperSyncGroup);
          
          String logType = GrouperTextContainer.textOrNull("provisionerLogsTypeOfLogGroup");
          if (StringUtils.isBlank(logType)) {
            logType = "Group";
          }
          
          grouperSyncLogWithOwner.setLogType(logType);
          continue;
        }
        
        if (gcGrouperSyncMembers.containsKey(gcGrouperSyncLog.getGrouperSyncOwnerId())) {
          GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSyncMembers.get(gcGrouperSyncLog.getGrouperSyncOwnerId());
          grouperSyncLogWithOwner.setGcGrouperSyncMember(gcGrouperSyncMember);
          
          String logType = GrouperTextContainer.textOrNull("provisionerLogsTypeOfLogMember");
          if (StringUtils.isBlank(logType)) {
            logType = "Member";
          }
          
          grouperSyncLogWithOwner.setLogType(logType);
          continue;
        }
        
      }
      
    }
    
    return grouperSyncLogsWithOwner;
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
  public static void deleteAttributeAssign(GrouperObject grouperObject, String targetName) {
    AttributeAssign currentAttributeAssign = getAttributeAssign(grouperObject, targetName);
    if (currentAttributeAssign != null) {
      currentAttributeAssign.delete();
    }
  }
  
  /**
   * @param grouperProvisioner
   * @param grouperProvisioningObjectAttributesToProcess
   * @param grouperProvisioningFolderAttributes
   * @param policyGroupIds
   */
  public static void propagateProvisioningAttributes(GrouperProvisioner grouperProvisioner, Set<GrouperProvisioningObjectAttributes> grouperProvisioningObjectAttributesToProcess, Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningFolderAttributes, Set<String> policyGroupIds) {
    String configId = grouperProvisioner.getConfigId();
    AttributeDefName attributeDefNameBase = GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase();
    AttributeDefName attributeDefNameDirectAssign = AttributeDefNameFinder.findByName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT, true);
    AttributeDefName attributeDefNameDoProvision = AttributeDefNameFinder.findByName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION, true);
    AttributeDefName attributeDefNameMetadataJson = AttributeDefNameFinder.findByName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON, true);
    AttributeDefName attributeDefNameOwnerStemId = AttributeDefNameFinder.findByName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID, true);
    AttributeDefName attributeDefNameStemScope = AttributeDefNameFinder.findByName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE, true);
    AttributeDefName attributeDefNameTarget = AttributeDefNameFinder.findByName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET, true);

    int provisioningAttributesFoldersDeleted = 0;
    int provisioningAttributesFoldersAddedOrUpdated = 0;
    int provisioningAttributesGroupsDeleted = 0;
    int provisioningAttributesGroupsAddedOrUpdated = 0;
    
    // get a map of all child -> parent, maybe this is cheaper than having to recalculate it multiple times per object
    Map<String, String> childToParent = new HashMap<String, String>();
    
    for (GrouperProvisioningObjectAttributes grouperProvisioningObjectAttribute : grouperProvisioningObjectAttributesToProcess) {
      String parentStemName = GrouperUtil.parentStemNameFromName(grouperProvisioningObjectAttribute.getName());
      
      if (parentStemName != null) {
        childToParent.put(grouperProvisioningObjectAttribute.getName(), parentStemName);
      }
    }
    
    for (GrouperProvisioningObjectAttributes grouperProvisioningObjectAttribute : grouperProvisioningFolderAttributes.values()) {
      String parentStemName = GrouperUtil.parentStemNameFromName(grouperProvisioningObjectAttribute.getName());
      
      if (parentStemName != null) {
        childToParent.put(grouperProvisioningObjectAttribute.getName(), parentStemName);
      }
    }
    
    // go through each group/folder and recompute what the attributes should be by looking at ancestor folders and if it doesn't match what's in the db, then update db
    for (GrouperProvisioningObjectAttributes grouperProvisioningObjectAttribute : grouperProvisioningObjectAttributesToProcess) {

      if ("true".equalsIgnoreCase(grouperProvisioningObjectAttribute.getProvisioningDirectAssign())) {
        continue;
      }
      
      GrouperProvisioningObjectAttributes ancestorGrouperProvisioningObjectAttribute = null;
      
      int depth = 0;
      String currObjectName = new String(grouperProvisioningObjectAttribute.getName());
      while (true) {
        depth++;
        currObjectName = childToParent.get(currObjectName);
        if (currObjectName == null) {
          break;
        }
        
        GrouperProvisioningObjectAttributes currGrouperProvisioningObjectAttribute = grouperProvisioningFolderAttributes.get(currObjectName);
        if (currGrouperProvisioningObjectAttribute != null && "true".equalsIgnoreCase(currGrouperProvisioningObjectAttribute.getProvisioningDirectAssign())) {
          
          if (depth > 1 && !"sub".equalsIgnoreCase(currGrouperProvisioningObjectAttribute.getProvisioningStemScope())) {
            // not applicable, continue going up the hiearchy
            continue;
          }
          
          if (!GrouperUtil.equals(configId, currGrouperProvisioningObjectAttribute.getProvisioningDoProvision())) {
            // not supposed to provision anything under here
            break;
          }
          
          if (grouperProvisioningObjectAttribute.isOwnedByGroup()) {
            if (isOnlyProvisionPolicyGroups(grouperProvisioner, currGrouperProvisioningObjectAttribute)) {
              if (!policyGroupIds.contains(grouperProvisioningObjectAttribute.getId())) {
                // not provisioning group that isn't a policy group
                break;
              }
            }
            
            String provisionableRegex = getProvisionableRegex(grouperProvisioner, currGrouperProvisioningObjectAttribute);
            if (!GrouperUtil.isEmpty(provisionableRegex)) {
              if (!GrouperProvisioningObjectMetadata.groupNameMatchesRegex(grouperProvisioningObjectAttribute.getName(), provisionableRegex)) {
                // not provisioning group that doesn't match regex
                break;
              }
            }
          }
          
          ancestorGrouperProvisioningObjectAttribute = currGrouperProvisioningObjectAttribute;
          break;
        }
      }
      
      if (ancestorGrouperProvisioningObjectAttribute == null) {
        if (!GrouperUtil.isEmpty(grouperProvisioningObjectAttribute.getProvisioningTarget())) {
          // delete the marker
          
          AttributeAssignable object;
          
          if (grouperProvisioningObjectAttribute.isOwnedByGroup()) {
            object = GroupFinder.findByName(GrouperSession.staticGrouperSession(), grouperProvisioningObjectAttribute.getName(), false);
          } else {
            object = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperProvisioningObjectAttribute.getName(), false);
          }
          
          if (object == null) {
            // guess it was deleted?
            continue;
          }
          
          LOG.info("For " + configId + " and stemName=" + grouperProvisioningObjectAttribute.getName() + " deleting marker attribute");
          
          Set<AttributeAssign> markerAttributeAssigns = object.getAttributeDelegate().retrieveAssignments(attributeDefNameBase);
          for (AttributeAssign markerAttributeAssign : GrouperUtil.nonNull(markerAttributeAssigns)) {
            AttributeAssignValue attributeAssignValue = markerAttributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(attributeDefNameTarget.getName());
            if (attributeAssignValue != null && configId.equals(attributeAssignValue.getValueString())) {
              markerAttributeAssign.delete();
              break;
            }
          }
          
          if (grouperProvisioningObjectAttribute.isOwnedByGroup()) {
            provisioningAttributesGroupsDeleted++;
            
            Group group =(Group)object;
            ProvisioningMessage provisioningMessage = new ProvisioningMessage();
            provisioningMessage.setGroupIdsForSync(new String[] {group.getId()});
            provisioningMessage.setBlocking(false);
            provisioningMessage.send(grouperProvisioningObjectAttribute.getProvisioningTarget());
          } else {
            provisioningAttributesFoldersDeleted++;
          }
        }
      } else {
        String existingDirectAssign = grouperProvisioningObjectAttribute.getProvisioningDirectAssign();
        String existingDoProvision = grouperProvisioningObjectAttribute.getProvisioningDoProvision();
        String existingMetadataJson = grouperProvisioningObjectAttribute.getProvisioningMetadataJson();
        String existingOwnerStemId = grouperProvisioningObjectAttribute.getProvisioningOwnerStemId();
        String existingStemScope = grouperProvisioningObjectAttribute.getProvisioningStemScope();
        String existingTarget = grouperProvisioningObjectAttribute.getProvisioningTarget();
        
        String actualDirectAssign = "false";
        String actualDoProvision = ancestorGrouperProvisioningObjectAttribute.getProvisioningDoProvision();
        String actualMetadataJson = ancestorGrouperProvisioningObjectAttribute.getProvisioningMetadataJson();
        String actualOwnerStemId = ancestorGrouperProvisioningObjectAttribute.getId();
        String actualStemScope = null;
        String actualTarget = ancestorGrouperProvisioningObjectAttribute.getProvisioningTarget();
        
        if (!GrouperUtil.equals(existingDirectAssign, actualDirectAssign) ||
            !GrouperUtil.equals(existingDoProvision, actualDoProvision) ||
            !GrouperUtil.equals(existingMetadataJson, actualMetadataJson) ||
            !GrouperUtil.equals(existingOwnerStemId, actualOwnerStemId) ||
            !GrouperUtil.equals(existingStemScope, actualStemScope) ||
            !GrouperUtil.equals(existingTarget, actualTarget)) {

          AttributeAssignable object;
          
          if (grouperProvisioningObjectAttribute.isOwnedByGroup()) {
            object = GroupFinder.findByName(GrouperSession.staticGrouperSession(), grouperProvisioningObjectAttribute.getName(), false);
          } else {
            object = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperProvisioningObjectAttribute.getName(), false);
          }
          
          if (object == null) {
            // guess it was deleted?
            continue;
          }
          
          HibernateSession.callbackHibernateSession(
              GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
              new HibernateHandler() {

                public Object callback(HibernateHandlerBean hibernateHandlerBean)
                    throws GrouperDAOException {
          
                  
                  AttributeAssign markerAssign = null;
                  
                  Set<AttributeAssign> markerAttributeAssigns = object.getAttributeDelegate().retrieveAssignments(attributeDefNameBase);
                  for (AttributeAssign markerAttributeAssign : GrouperUtil.nonNull(markerAttributeAssigns)) {
                    AttributeAssignValue attributeAssignValue = markerAttributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(attributeDefNameTarget.getName());
                    if (attributeAssignValue != null && configId.equals(attributeAssignValue.getValueString())) {
                      markerAssign = markerAttributeAssign;
                      break;
                    }
                  }
                  
                  if (markerAssign == null) {
                    markerAssign = object.getAttributeDelegate().internal_addAttributeHelper(null, attributeDefNameBase, false, null).getAttributeAssign();
                  }
                  
                  if (!GrouperUtil.equals(existingDirectAssign, actualDirectAssign)) {
                    LOG.info("For " + configId + " and stemName=" + grouperProvisioningObjectAttribute.getName() + " updating provisioningDirectAssign to: " + actualDirectAssign);
                    markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameDirectAssign.getName(), actualDirectAssign);
                  }
                  
                  if (!GrouperUtil.equals(existingDoProvision, actualDoProvision)) {
                    LOG.info("For " + configId + " and stemName=" + grouperProvisioningObjectAttribute.getName() + " updating provisioningDoProvision to: " + actualDoProvision);
                    if (actualDoProvision == null) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameDoProvision);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameDoProvision.getName(), actualDoProvision);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingMetadataJson, actualMetadataJson)) {
                    LOG.info("For " + configId + " and stemName=" + grouperProvisioningObjectAttribute.getName() + " updating provisioningMetadataJson to: " + actualMetadataJson);
                    if (actualMetadataJson == null) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameMetadataJson);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameMetadataJson.getName(), actualMetadataJson);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingOwnerStemId, actualOwnerStemId)) {
                    LOG.info("For " + configId + " and stemName=" + grouperProvisioningObjectAttribute.getName() + " updating provisioningOwnerStemId to: " + actualOwnerStemId);
                    if (actualOwnerStemId == null) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameOwnerStemId);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameOwnerStemId.getName(), actualOwnerStemId);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingStemScope, actualStemScope)) {
                    LOG.info("For " + configId + " and stemName=" + grouperProvisioningObjectAttribute.getName() + " updating provisioningStemScope to: " + actualStemScope);
                    markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameStemScope.getName(), actualStemScope);
                  }
                  
                  if (!GrouperUtil.equals(existingTarget, actualTarget)) {
                    LOG.info("For " + configId + " and stemName=" + grouperProvisioningObjectAttribute.getName() + " updating provisioningTarget to: " + actualTarget);
                    if (actualTarget == null) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameTarget);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameTarget.getName(), actualTarget);
                    }
                  }
                  
                  return null;
                }
              });
          
          if (grouperProvisioningObjectAttribute.isOwnedByGroup()) {
            provisioningAttributesGroupsAddedOrUpdated++;
            
            Group group =(Group)object;
            ProvisioningMessage provisioningMessage = new ProvisioningMessage();
            provisioningMessage.setGroupIdsForSync(new String[] {group.getId()});
            provisioningMessage.setBlocking(false);
            provisioningMessage.send(actualTarget);
          } else {
            provisioningAttributesFoldersAddedOrUpdated++;
          }
        }
      }
    }
    
    if (provisioningAttributesGroupsAddedOrUpdated > 0) {
      grouperProvisioner.getDebugMap().put("provisioningAttributesGroupsAddedOrUpdated", provisioningAttributesGroupsAddedOrUpdated);
    }
    
    if (provisioningAttributesFoldersAddedOrUpdated > 0) {
      grouperProvisioner.getDebugMap().put("provisioningAttributesFoldersAddedOrUpdated", provisioningAttributesFoldersAddedOrUpdated);
    }
    
    if (provisioningAttributesGroupsDeleted > 0) {
      grouperProvisioner.getDebugMap().put("provisioningAttributesGroupsDeleted", provisioningAttributesGroupsDeleted);
    }
    
    if (provisioningAttributesFoldersDeleted > 0) {
      grouperProvisioner.getDebugMap().put("provisioningAttributesFoldersDeleted", provisioningAttributesFoldersDeleted);
    }
  }
  
  private static boolean isOnlyProvisionPolicyGroups(GrouperProvisioner grouperProvisioner, GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes) {    
    if (grouperProvisioner.retrieveGrouperProvisioningConfiguration().isAllowPolicyGroupOverride()) {
      Boolean override = (Boolean)grouperProvisioningObjectAttributes.getMetadataNameValues().get("md_grouper_allowPolicyGroupOverride");
      if (override != null) {
        return override;
      }
    }
    
    return grouperProvisioner.retrieveGrouperProvisioningConfiguration().isOnlyProvisionPolicyGroups();
  }
  
  private static String getProvisionableRegex(GrouperProvisioner grouperProvisioner, GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes) {    
    if (grouperProvisioner.retrieveGrouperProvisioningConfiguration().isAllowProvisionableRegexOverride()) {
      String override = (String)grouperProvisioningObjectAttributes.getMetadataNameValues().get("md_grouper_allowProvisionableRegexOverride");
      if (!GrouperUtil.isEmpty(override)) {
        return override;
      }
    }
    
    return grouperProvisioner.retrieveGrouperProvisioningConfiguration().getProvisionableRegex();
  }
}
