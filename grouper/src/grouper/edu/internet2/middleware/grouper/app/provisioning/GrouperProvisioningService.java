package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.sql.Timestamp;
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
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperProvisioningService {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningService.class);
  
  
  private static final ExpirableCache<MultiKey, Boolean> viewableGroupToSubject = new ExpirableCache<MultiKey, Boolean>(5);
  private static final ExpirableCache<MultiKey, Boolean> editableGroupToSubject = new ExpirableCache<MultiKey, Boolean>(5);
  
        
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
          .addAttributeValuesOnAssignment2(target)
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
    if (attributeAssign != null) {
      return buildGrouperProvisioningAttributeValue(attributeAssign);
    }
    
    if (!(grouperObject instanceof Group) && !(grouperObject instanceof Stem)) {
      return null;
    }
    
    // ok nothing direct.  let's look for indirect.
    if (GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + targetName + ".class") == null) {
      // invalid target
      return null;
    }
    
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner(targetName); 
    provisioner.retrieveGrouperProvisioningConfiguration().configureProvisionableSettings();
    

    GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes;
    String parentStemId;
    Set<String> policyGroupIds = new HashSet<String>();
    
    if (grouperObject instanceof Group) {
      grouperProvisioningObjectAttributes = new GrouperProvisioningObjectAttributes(grouperObject.getId(), grouperObject.getName(), ((Group)grouperObject).getIdIndex(), null);
      grouperProvisioningObjectAttributes.setOwnedByGroup(true);
      parentStemId = ((Group)grouperObject).getParentUuid();
      policyGroupIds = provisioner.retrieveGrouperDao().retrieveProvisioningGroupIdsThatArePolicyGroups(Collections.singleton(grouperObject.getId()));
    } else {
      grouperProvisioningObjectAttributes = new GrouperProvisioningObjectAttributes(grouperObject.getId(), grouperObject.getName(), ((Stem)grouperObject).getIdIndex(), null);
      grouperProvisioningObjectAttributes.setOwnedByStem(true);
      parentStemId = ((Stem)grouperObject).getParentUuid();
    }

    Map<String, GrouperProvisioningObjectAttributes> ancestorProvisioningGroupAttributes = provisioner.retrieveGrouperDao().retrieveAncestorProvisioningAttributesByFolder(parentStemId);
    
    Map<String, GrouperProvisioningObjectAttributes> calculatedProvisioningAttributes = GrouperProvisioningService.calculateProvisioningAttributes(provisioner, Collections.singleton(grouperProvisioningObjectAttributes), ancestorProvisioningGroupAttributes, policyGroupIds);
    if (calculatedProvisioningAttributes.size() == 0) {
      return null;
    }
    
    GrouperProvisioningObjectAttributes calculatedGrouperProvisioningObjectAttributes = calculatedProvisioningAttributes.values().iterator().next();
    
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = new GrouperProvisioningAttributeValue();
    grouperProvisioningAttributeValue.setDirectAssignment(false);
    grouperProvisioningAttributeValue.setDoProvision(targetName);
    grouperProvisioningAttributeValue.setTargetName(targetName);
    grouperProvisioningAttributeValue.setStemScopeString(calculatedGrouperProvisioningObjectAttributes.getProvisioningStemScope());
    grouperProvisioningAttributeValue.setOwnerStemId(calculatedGrouperProvisioningObjectAttributes.getProvisioningOwnerStemId());
    grouperProvisioningAttributeValue.setMetadataNameValues(calculatedGrouperProvisioningObjectAttributes.getMetadataNameValues());
    
    return grouperProvisioningAttributeValue;
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
    
    if (!StringUtils.equals(StringUtils.defaultIfBlank(one.getStemScopeString(), "sub"), StringUtils.defaultIfBlank(two.getStemScopeString(), "sub"))) {
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
   * @return if made changes
   */
  public static boolean saveOrUpdateProvisioningAttributes(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue, GrouperObject grouperObject) {
    
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
      if (!newValueDifferentFromOldValue) return false;
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
    return true;
  }
  
  /**
   * is given target editable for given subject and grouper object
   * @param target
   * @param subject
   * @param grouperObject
   * @return
   */
  public static boolean isTargetEditable(GrouperProvisioningTarget target, Subject subject, GrouperObject grouperObject) {
    
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
   * is given target viewable for given subject and grouper object
   * @param target
   * @param subject
   * @param grouperObject
   * @return
   */
  public static boolean isTargetViewable(GrouperProvisioningTarget target, Subject subject, GrouperObject grouperObject) {
    
    if (isTargetEditable(target, subject, grouperObject)) {
      return true;
    }
    
    return isTargetViewableBySubject(target, subject);
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
  
  public static void deleteInvalidIndirectProvisioningAssignments() {
    Set<AttributeAssign> assignments = GrouperDAOFactory.getFactory().getAttributeAssign().findByAttributeDefNameAndValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getId(), "false", null);
    
    if (assignments.size() > 0) {
      System.out.println("Found " + assignments.size() + " invalid indirect provisioning assignments.  Deleting them now.");
      for (AttributeAssign assignment : assignments) {
        assignment.getOwnerAttributeAssign().delete();
      }
      System.out.println("Finished deleting all invalid indirect provisioning assignments.");
    } else {
      System.out.println("Found no invalid indirect provisioning assignments.");
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
   * 
   * @param provisionerName
   * @param objectType
   * @param errorCode
   * @param errorDuration
   * @return
   */
  public static GrouperProvisioningErrorSummary retrieveProvisioningErrorSummary(String provisionerName, 
      String objectType, GcGrouperSyncErrorCode errorCode,  String errorDuration) {
    
    if (StringUtils.isNotBlank(objectType)) {
      
      if (StringUtils.equals(objectType, "group")) {
        GrouperProvisioningErrorSummary oneBigSummary = retrieveErrorsSummaryForGroup(provisionerName, errorCode, errorDuration);
        oneBigSummary.setErrorsCount(oneBigSummary.getGroupErrorsCount());
        return oneBigSummary;
      }
      else if (StringUtils.equals(objectType, "entity")) {
        GrouperProvisioningErrorSummary oneBigSummary =  retrieveErrorsSummaryForEntity(provisionerName, errorCode, errorDuration);
        oneBigSummary.setErrorsCount(oneBigSummary.getEntityErrorsCount());
        return oneBigSummary;
      }
      else if (StringUtils.equals(objectType, "membership")) {
        GrouperProvisioningErrorSummary oneBigSummary = retrieveErrorsSummaryForMembership(provisionerName, errorCode, errorDuration);
        oneBigSummary.setErrorsCount(oneBigSummary.getMembershipErrorsCount());
        return oneBigSummary;
      } else {
        throw new RuntimeException("Invalid objectType: "+objectType+". Valid values are group, entity, and membership.");
      }
      
    }
    
    // retrieve all summaries
    GrouperProvisioningErrorSummary oneBigSummary = retrieveErrorsSummaryForGroup(provisionerName, errorCode, errorDuration);
    oneBigSummary.setErrorsCount(oneBigSummary.getGroupErrorsCount());
    
    GrouperProvisioningErrorSummary summaryForEntity = retrieveErrorsSummaryForEntity(provisionerName, errorCode, errorDuration);
    oneBigSummary.setEntityErrorsCount(summaryForEntity.getEntityErrorsCount());
    oneBigSummary.setEntityErrorTypeCount(summaryForEntity.getEntityErrorTypeCount());
    oneBigSummary.setErrorsCount(oneBigSummary.getErrorsCount() +  summaryForEntity.getEntityErrorsCount());
    
    GrouperProvisioningErrorSummary summaryForMembership = retrieveErrorsSummaryForMembership(provisionerName, errorCode, errorDuration);
    oneBigSummary.setMembershipErrorsCount(summaryForMembership.getMembershipErrorsCount());
    oneBigSummary.setMembershipsErrorTypeCount(summaryForMembership.getMembershipsErrorTypeCount());
    oneBigSummary.setErrorsCount(oneBigSummary.getErrorsCount() +  summaryForMembership.getMembershipErrorsCount());
    return oneBigSummary;
    
  }
  
  /**
   * retrieve list of errors
   * @param provisionerName
   * @param objectType
   * @param errorCode
   * @param errorDuration
   * @return
   */
  public static List<GrouperProvisioningError> retrieveProvisioningErrors(String provisionerName, 
      String objectType, GcGrouperSyncErrorCode errorCode,  String errorDuration) {
    
    List<GrouperProvisioningError> provisioningErrors = new ArrayList<>();
    
    if (StringUtils.isNotBlank(objectType)) {
      
      if (StringUtils.equals(objectType, "group")) {
        provisioningErrors = retrieveProvisioningGroupErrors(provisionerName, errorCode, errorDuration);
      }
      else if (StringUtils.equals(objectType, "entity")) {
        provisioningErrors = retrieveProvisioningEntityErrors(provisionerName, errorCode, errorDuration);
      }
      else if (StringUtils.equals(objectType, "membership")) {
        provisioningErrors = retrieveProvisioningMembershipErrors(provisionerName, errorCode, errorDuration);
      } else {
        throw new RuntimeException("Invalid objectType: "+objectType+". Valid values are group, entity, and membership.");
      }
      
    } else {
      
      provisioningErrors = retrieveProvisioningGroupErrors(provisionerName, errorCode, errorDuration);
      List<GrouperProvisioningError> entityErrors = retrieveProvisioningEntityErrors(provisionerName, errorCode, errorDuration);
      List<GrouperProvisioningError> membershipErrors = retrieveProvisioningMembershipErrors(provisionerName, errorCode, errorDuration);

      provisioningErrors.addAll(entityErrors);
      provisioningErrors.addAll(membershipErrors);
    }
    
    Collections.sort(provisioningErrors, new Comparator<GrouperProvisioningError>() {
      @Override
      public int compare(GrouperProvisioningError o1, GrouperProvisioningError o2) {
        return o2.getErrorTimestamp().compareTo(o1.getErrorTimestamp());
      }
    });
    
    if (provisioningErrors.size() > 1000) {
      provisioningErrors = provisioningErrors.subList(0, 1000);
    }
    
    return provisioningErrors;
  }
  
  /**
   * 
   * @param provisionerName
   * @param errorCode
   * @param errorDuration
   * @return
   */
  private static List<GrouperProvisioningError> retrieveProvisioningGroupErrors(String provisionerName, GcGrouperSyncErrorCode errorCode, String errorDuration) {
    
    List<GrouperProvisioningError> result = new ArrayList<>();
    
    StringBuilder query = new StringBuilder("select gsg.group_name, gsg.error_message, gsg.error_code, gsg.error_timestamp from grouper_sync_group gsg, grouper_sync gs where gsg.grouper_sync_id  = gs.id and gs.provisioner_name = ? ");
    
    if (errorCode != null) {
      query.append(" and gsg.error_code = ? ");
    } else {
      query.append(" and gsg.error_code is not null ");
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      query.append(" and gsg.error_timestamp > ? ");
    }
    
    GcDbAccess gcDbAccess = new GcDbAccess().sql(query.toString());
    gcDbAccess.addBindVar(provisionerName);
    if (errorCode != null) {
      gcDbAccess.addBindVar(errorCode.name());
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      Timestamp errorTimestamp = convertErrorDurationToTimestamp(errorDuration);
      gcDbAccess.addBindVar(errorTimestamp);
    }
    
    List<Object[]> rows = gcDbAccess.selectList(Object[].class);
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerName);
    GrouperProvisioningConfiguration provisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    for (Object[] row: rows) {
      
      GrouperProvisioningError error = new GrouperProvisioningError();
      error.setErrorCode(GrouperUtil.stringValue(row[2]));
      error.setErrorDescription(GrouperUtil.stringValue(row[1]));
      error.setErrorTimestamp( GrouperUtil.timestampObjectValue(row[3], true));
      error.setFatal(isErrorFatal(provisioningConfiguration, GrouperUtil.stringValue(row[2])));
      error.setGroupName(GrouperUtil.stringValue(row[0]));
      error.setObjectType("group");
      result.add(error);
    }
    
    return result;
  }
  
  /**
   * 
   * @param provisionerName
   * @param errorCode
   * @param errorDuration
   * @return
   */
  private static List<GrouperProvisioningError> retrieveProvisioningEntityErrors(String provisionerName, GcGrouperSyncErrorCode errorCode, String errorDuration) {
    
    List<GrouperProvisioningError> result = new ArrayList<>();
    
    StringBuilder query = new StringBuilder("select gsm.subject_id, gsm.subject_identifier, gsm.error_message, gsm.error_code, gsm.error_timestamp from grouper_sync_member gsm, grouper_sync gs where gsm.grouper_sync_id  = gs.id and gs.provisioner_name = ? ");
    
    if (errorCode != null) {
      query.append(" and gsm.error_code = ? ");
    } else {
      query.append(" and gsm.error_code is not null ");
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      query.append(" and gsm.error_timestamp > ? ");
    }
    
    GcDbAccess gcDbAccess = new GcDbAccess().sql(query.toString());
    gcDbAccess.addBindVar(provisionerName);
    if (errorCode != null) {
      gcDbAccess.addBindVar(errorCode.name());
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      Timestamp errorTimestamp = convertErrorDurationToTimestamp(errorDuration);
      gcDbAccess.addBindVar(errorTimestamp);
    }
    
    List<Object[]> rows = gcDbAccess.selectList(Object[].class);
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerName);
    GrouperProvisioningConfiguration provisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    for (Object[] row: rows) {
      
      GrouperProvisioningError error = new GrouperProvisioningError();
      error.setErrorCode(GrouperUtil.stringValue(row[3]));
      error.setErrorDescription(GrouperUtil.stringValue(row[2]));
      error.setErrorTimestamp( GrouperUtil.timestampObjectValue(row[4], true));
      error.setFatal(isErrorFatal(provisioningConfiguration, GrouperUtil.stringValue(row[3])));
      error.setSubjectId(GrouperUtil.stringValue(row[0]));
      error.setSubjectIdentifier(GrouperUtil.stringValue(row[1]));
      error.setObjectType("entity");
      result.add(error);
    }
    
    return result;
  }
  
  /**
   * 
   * @param provisionerName
   * @param errorCode
   * @param errorDuration
   * @return
   */
  private static List<GrouperProvisioningError> retrieveProvisioningMembershipErrors(String provisionerName, GcGrouperSyncErrorCode errorCode, String errorDuration) {
    
    List<GrouperProvisioningError> result = new ArrayList<>();
    
    StringBuilder query = new StringBuilder("select gsg.group_name, gsm2.subject_id, gsm2.subject_identifier, gsm.error_message, gsm.error_code, gsm.error_timestamp from grouper_sync_membership gsm, grouper_sync gs, grouper_sync_group gsg, grouper_sync_member gsm2 where gsm.grouper_sync_id  = gs.id and gsm.grouper_sync_group_id = gsg.id and gsm.grouper_sync_member_id = gsm2.id and gs.provisioner_name = ? ");
    
    if (errorCode != null) {
      query.append(" and gsm.error_code = ? ");
    } else {
      query.append(" and gsm.error_code is not null ");
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      query.append(" and gsm.error_timestamp > ? ");
    }
    
    GcDbAccess gcDbAccess = new GcDbAccess().sql(query.toString());
    gcDbAccess.addBindVar(provisionerName);
    if (errorCode != null) {
      gcDbAccess.addBindVar(errorCode.name());
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      Timestamp errorTimestamp = convertErrorDurationToTimestamp(errorDuration);
      gcDbAccess.addBindVar(errorTimestamp);
    }
    
    List<Object[]> rows = gcDbAccess.selectList(Object[].class);
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerName);
    GrouperProvisioningConfiguration provisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    for (Object[] row: rows) {
      
      GrouperProvisioningError error = new GrouperProvisioningError();
      error.setErrorCode(GrouperUtil.stringValue(row[4]));
      error.setErrorDescription(GrouperUtil.stringValue(row[3]));
      error.setErrorTimestamp( GrouperUtil.timestampObjectValue(row[5], true));
      error.setFatal(isErrorFatal(provisioningConfiguration, GrouperUtil.stringValue(row[4])));
      error.setGroupName(GrouperUtil.stringValue(row[0]));
      error.setSubjectId(GrouperUtil.stringValue(row[1]));
      error.setSubjectIdentifier(GrouperUtil.stringValue(row[2]));
      error.setObjectType("membership");
      result.add(error);
    }
    
    return result;
  }
  
  /**
   * 
   * @param grouperProvisioningConfiguration
   * @param errorCode
   * @return
   */
  private static boolean isErrorFatal(GrouperProvisioningConfiguration grouperProvisioningConfiguration, String errorCode) {
    
    GcGrouperSyncErrorCode syncErrorCode = GcGrouperSyncErrorCode.valueOf(errorCode);
    
    if (syncErrorCode == GcGrouperSyncErrorCode.INV && grouperProvisioningConfiguration.isErrorHandlingInvalidDataIsAnError()) {
      return true;
    }
    
    if (syncErrorCode == GcGrouperSyncErrorCode.LEN && grouperProvisioningConfiguration.isErrorHandlingLengthValidationIsAnError()) {
      return true;
    }

    if (syncErrorCode == GcGrouperSyncErrorCode.REQ && grouperProvisioningConfiguration.isErrorHandlingRequiredValidationIsAnError()) {
      return true;
    }

    if (syncErrorCode == GcGrouperSyncErrorCode.MAT && grouperProvisioningConfiguration.isErrorHandlingMatchingValidationIsAnError()) {
      return true;
    }

    if (syncErrorCode == GcGrouperSyncErrorCode.DNE && grouperProvisioningConfiguration.isErrorHandlingTargetObjectDoesNotExistIsAnError()) {
      return true;
    }
    
    return false;
    
  }
  
  /**
   * convert duration string into timestamp
   * @param durationString
   * @return
   */
  private static Timestamp convertErrorDurationToTimestamp(String durationString) {
    
    if (StringUtils.equals(durationString, "last_15_minutes")) {
      long millisIn15Minutes = 15*60*1000;
      return new Timestamp(System.currentTimeMillis() - millisIn15Minutes);
    } else if (StringUtils.equals(durationString, "last_hour")) {
      long millisIn60Minutes = 60*60*1000;
      return new Timestamp(System.currentTimeMillis() - millisIn60Minutes);
    } else if (StringUtils.equals(durationString, "last_day")) {
      long millisInOneDay = 1*24*60*60*1000;
      return new Timestamp(System.currentTimeMillis() - millisInOneDay);
    } else {
      throw new RuntimeException("Invalid durationString: "+durationString+". Valid values are last_15_minutes, last_hour, and last_day.");
    }
  } 
  
  private static GrouperProvisioningErrorSummary retrieveErrorsSummaryForGroup(String provisionerName, GcGrouperSyncErrorCode errorCode, String errorDuration) {
      
    StringBuilder query = new StringBuilder("select count(gsg.id), gsg.error_code from grouper_sync_group gsg, grouper_sync gs where gsg.grouper_sync_id  = gs.id and gs.provisioner_name = ? ");
    
    if (errorCode != null) {
      query.append(" and gsg.error_code = ? ");
    } else {
      query.append(" and gsg.error_code is not null ");
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      query.append(" and gsg.error_timestamp > ? ");
    }
    
    query.append(" group by gsg.error_code");
    
    GcDbAccess gcDbAccess = new GcDbAccess().sql(query.toString());
    gcDbAccess.addBindVar(provisionerName);
    if (errorCode != null) {
      gcDbAccess.addBindVar(errorCode.name());
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      Timestamp errorTimestamp = convertErrorDurationToTimestamp(errorDuration);
      gcDbAccess.addBindVar(errorTimestamp);
    }
    
    List<Object[]> countAndErrorCodes = gcDbAccess.selectList(Object[].class);
    
    GrouperProvisioningErrorSummary summary = new GrouperProvisioningErrorSummary();
    
    Map<String, Long> groupErrorTypeCount = new HashMap<>();
    
    long totalGroupErrors = 0;
    
    for (Object[] errorCountAndErrorCode: countAndErrorCodes) {
      groupErrorTypeCount.put(GrouperUtil.stringValue(errorCountAndErrorCode[1]), GrouperUtil.longValue(errorCountAndErrorCode[0], 0));
      totalGroupErrors += GrouperUtil.longValue(errorCountAndErrorCode[0], 0);
    }
    
    summary.setGroupErrorTypeCount(groupErrorTypeCount);
    summary.setGroupErrorsCount(totalGroupErrors);
    
    return summary;
    
  }
  
  private static GrouperProvisioningErrorSummary retrieveErrorsSummaryForEntity(String provisionerName, GcGrouperSyncErrorCode errorCode, String errorDuration) {
    
    StringBuilder query = new StringBuilder("select count(gsm.id), gsm.error_code from grouper_sync_member gsm, grouper_sync gs where gsm.grouper_sync_id  = gs.id and gs.provisioner_name = ? ");
    
    if (errorCode != null) {
      query.append(" and gsm.error_code = ? ");
    } else {
      query.append(" and gsm.error_code is not null ");
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      query.append(" and gsm.error_timestamp > ? ");
    }
    
    query.append(" group by gsm.error_code");
    
    GcDbAccess gcDbAccess = new GcDbAccess().sql(query.toString());
    gcDbAccess.addBindVar(provisionerName);
    if (errorCode != null) {
      gcDbAccess.addBindVar(errorCode.name());
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      Timestamp errorTimestamp = convertErrorDurationToTimestamp(errorDuration);
      gcDbAccess.addBindVar(errorTimestamp);
    }
    
    List<Object[]> countAndErrorCodes = gcDbAccess.selectList(Object[].class);
    
    GrouperProvisioningErrorSummary summary = new GrouperProvisioningErrorSummary();
    
    Map<String, Long> entityErrorTypeCount = new HashMap<>();
    
    long totalEntityErrors = 0;
    
    for (Object[] errorCountAndErrorCode: countAndErrorCodes) {
      entityErrorTypeCount.put(GrouperUtil.stringValue(errorCountAndErrorCode[1]), GrouperUtil.longValue(errorCountAndErrorCode[0], 0));
      totalEntityErrors += GrouperUtil.longValue(errorCountAndErrorCode[0], 0);
    }
    
    summary.setEntityErrorTypeCount(entityErrorTypeCount);
    summary.setEntityErrorsCount(totalEntityErrors);
    
    return summary;
    
  }
  
  /**
   * 
   * @param provisionerName
   * @param errorCode
   * @param errorDuration
   * @return
   */
  private static GrouperProvisioningErrorSummary retrieveErrorsSummaryForMembership(String provisionerName, GcGrouperSyncErrorCode errorCode, String errorDuration) {
    
    StringBuilder query = new StringBuilder("select count(gsm.id), gsm.error_code from grouper_sync_membership gsm, grouper_sync gs where gsm.grouper_sync_id  = gs.id and gs.provisioner_name = ? ");
    
    if (errorCode != null) {
      query.append(" and gsm.error_code = ? ");
    } else {
      query.append(" and gsm.error_code is not null ");
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      query.append(" and gsm.error_timestamp > ? ");
    }
    
    query.append(" group by gsm.error_code");
    
    GcDbAccess gcDbAccess = new GcDbAccess().sql(query.toString());
    gcDbAccess.addBindVar(provisionerName);
    if (errorCode != null) {
      gcDbAccess.addBindVar(errorCode.name());
    }
    
    if (StringUtils.isNotBlank(errorDuration)) {
      Timestamp errorTimestamp = convertErrorDurationToTimestamp(errorDuration);
      gcDbAccess.addBindVar(errorTimestamp);
    }
    
    List<Object[]> countAndErrorCodes = gcDbAccess.selectList(Object[].class);
    
    GrouperProvisioningErrorSummary summary = new GrouperProvisioningErrorSummary();
    
    Map<String, Long> membershipErrorTypeCount = new HashMap<>();
    
    long totalMembershipErrors = 0;
    
    for (Object[] errorCountAndErrorCode: countAndErrorCodes) {
      membershipErrorTypeCount.put(GrouperUtil.stringValue(errorCountAndErrorCode[1]), GrouperUtil.longValue(errorCountAndErrorCode[0], 0));
      totalMembershipErrors += GrouperUtil.longValue(errorCountAndErrorCode[0], 0);
    }
    
    summary.setMembershipsErrorTypeCount(membershipErrorTypeCount);
    summary.setMembershipErrorsCount(totalMembershipErrors);
    
    return summary;
    
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
    
    MultiKey multiKey = new MultiKey(groupAllowedToAssign, subject.getId());
    
    Boolean isEditable = viewableGroupToSubject.get(multiKey);
    if (isEditable != null) {
      return isEditable;
    }
    
    Boolean isMember = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
       
        
        Group group = GroupFinder.findByName(grouperSession, groupAllowedToAssign, false);
        if (group == null) {
          group = GroupFinder.findByUuid(grouperSession, groupAllowedToAssign, false);
          if (group == null) {
            LOG.error(groupAllowedToAssign+" is not a valid group id or group name");
            return false;
          }
        }
        
        return group.hasMember(subject);
        
      }
      
    });
    
    editableGroupToSubject.put(multiKey, isMember);
    
    return isMember;

  }
  
  /**
   * can the given subject view the given target
   * @param target
   * @param subject
   * @return
   */
  private static boolean isTargetViewableBySubject(GrouperProvisioningTarget target, Subject subject) {
    
    if (PrivilegeHelper.isWheelOrRootOrViewonlyRoot(subject)) {
      return true;
    }
    
    String groupAllowedToView = target.getGroupAllowedToView();
    
    if (StringUtils.isBlank(groupAllowedToView)) {
      return false;
    }
    
    MultiKey multiKey = new MultiKey(groupAllowedToView, subject.getId());
    
    Boolean isViewable = viewableGroupToSubject.get(multiKey);
    if (isViewable != null) {
      return isViewable;
    }
    
    Boolean isMember = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        Group group = GroupFinder.findByName(grouperSession, groupAllowedToView, false);
        if (group == null) {
          group = GroupFinder.findByUuid(grouperSession, groupAllowedToView, false);
          if (group == null) {
            LOG.error(groupAllowedToView+" is not a valid group id or group name");
            return false;
          }
        }
        
        return group.hasMember(subject);
        
      }
      
    });
    
    viewableGroupToSubject.put(multiKey, isMember);
    
    return isMember;

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
   * @return calculated provisioning attributes
   */
  public static Map<String, GrouperProvisioningObjectAttributes> calculateProvisioningAttributes(GrouperProvisioner grouperProvisioner, Set<GrouperProvisioningObjectAttributes> grouperProvisioningObjectAttributesToProcess, Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningFolderAttributes, Set<String> policyGroupIds) {
    String configId = grouperProvisioner.getConfigId();

    Map<String, GrouperProvisioningObjectAttributes> allCalculatedProvisioningAttributes = new HashMap<String, GrouperProvisioningObjectAttributes>();
    
    // go through each group and recompute what the attributes should be by looking at ancestor folders
    for (GrouperProvisioningObjectAttributes grouperProvisioningObjectAttribute : grouperProvisioningObjectAttributesToProcess) {

      if (grouperProvisioningObjectAttribute.isDeleted()) {
        continue;
      }
      
      if ("true".equalsIgnoreCase(grouperProvisioningObjectAttribute.getProvisioningDirectAssign())) {
        if (GrouperUtil.equals(configId, grouperProvisioningObjectAttribute.getProvisioningDoProvision())) {
          allCalculatedProvisioningAttributes.put(grouperProvisioningObjectAttribute.getId(), grouperProvisioningObjectAttribute);
        }
        continue;
      }
      
      GrouperProvisioningObjectAttributes ancestorGrouperProvisioningObjectAttribute = null;
      
      int depth = 0;
      String currObjectName = grouperProvisioningObjectAttribute.getName();
      while (true) {
        depth++;
        currObjectName = GrouperUtil.parentStemNameFromName(currObjectName);
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
      
      if (ancestorGrouperProvisioningObjectAttribute != null) {
        GrouperProvisioningObjectAttributes calculatedProvisioningAttributes = new GrouperProvisioningObjectAttributes(grouperProvisioningObjectAttribute.getId(), grouperProvisioningObjectAttribute.getName(), grouperProvisioningObjectAttribute.getIdIndex(), null);
        calculatedProvisioningAttributes.setProvisioningDirectAssign("false");
        calculatedProvisioningAttributes.setProvisioningDoProvision(ancestorGrouperProvisioningObjectAttribute.getProvisioningDoProvision());
        calculatedProvisioningAttributes.setProvisioningMetadataJson(ancestorGrouperProvisioningObjectAttribute.getProvisioningMetadataJson());
        calculatedProvisioningAttributes.setProvisioningOwnerStemId(ancestorGrouperProvisioningObjectAttribute.getId());
        calculatedProvisioningAttributes.setProvisioningTarget(ancestorGrouperProvisioningObjectAttribute.getProvisioningTarget());
      
        allCalculatedProvisioningAttributes.put(calculatedProvisioningAttributes.getId(), calculatedProvisioningAttributes);
      }
    }

    return allCalculatedProvisioningAttributes;
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
  
  public static List<GcGrouperSyncMembership> retrieveGcGrouperSyncMembershipsByMemberIdAndInTargetStartTimeRange(String memberId, Timestamp inTargetStartTimeFrom, Timestamp inTargetStartTimeTo) {
    String grouperSyncMembershipQuery = "select gsm.* from grouper_sync_membership gsm, grouper_sync_group gsg, grouper_sync_member gsmem " + 
        "where gsm.grouper_sync_group_id =  gsg.id " + 
        " and gsm.grouper_sync_member_id = gsmem.id " +
        " and gsmem.member_id = ? " +
        " and gsm.in_target_start is not null " +
        " and gsm.in_target_start > ? " +
        " and gsm.in_target_start < ? ";
    
    String grouperSyncQuery = "select * from grouper_sync gs where id = ? ";
    
    List<GcGrouperSyncMembership> grouperSyncMemberships = new GcDbAccess().sql(grouperSyncMembershipQuery)
        .addBindVar(memberId)
        .addBindVar(inTargetStartTimeFrom)
        .addBindVar(inTargetStartTimeTo)
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
  
  public static List<GcGrouperSyncMembership> retrieveGcGrouperSyncMembershipsByMemberIdAndInTargetEndTimeRange(String memberId, Timestamp inTargetEndTimeFrom, Timestamp inTargetEndTimeTo) {
    String grouperSyncMembershipQuery = "select gsm.* from grouper_sync_membership gsm, grouper_sync_group gsg, grouper_sync_member gsmem " + 
        "where gsm.grouper_sync_group_id =  gsg.id " + 
        " and gsm.grouper_sync_member_id = gsmem.id " +
        " and gsmem.member_id = ? " +
        " and gsm.in_target_end is not null " +
        " and gsm.in_target_end > ? " +
        " and gsm.in_target_end < ? ";
    
    String grouperSyncQuery = "select * from grouper_sync gs where id = ? ";
    
    List<GcGrouperSyncMembership> grouperSyncMemberships = new GcDbAccess().sql(grouperSyncMembershipQuery)
        .addBindVar(memberId)
        .addBindVar(inTargetEndTimeFrom)
        .addBindVar(inTargetEndTimeTo)
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
}
