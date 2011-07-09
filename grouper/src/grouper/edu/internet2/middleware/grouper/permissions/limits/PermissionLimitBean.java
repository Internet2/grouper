package edu.internet2.middleware.grouper.permissions.limits;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * holds the permission limit, and all values (prefetched).
 * At some point we should have an equals method (and hashcode) which will remove dupes, dont take
 * into account the permission limit bean type
 * 
 * @author mchyzer
 *
 */
public class PermissionLimitBean {

  
  /**
   * 
   * @param permissionEntrySet
   * @return the map
   */
  private static Map<String, Set<PermissionEntry>> attributeAssignIdsToPermissionEntry(Collection<PermissionEntry> permissionEntrySet) {
    Map<String, Set<PermissionEntry>> result = new HashMap<String, Set<PermissionEntry>>();
    
    for (PermissionEntry permissionEntry : permissionEntrySet) {
      
      String attributeAssignId = permissionEntry.getAttributeAssignId();
      
      Set<PermissionEntry> permissionEntries = result.get(attributeAssignId);
      if (permissionEntries == null) {
        
        permissionEntries = new HashSet<PermissionEntry>();
        result.put(attributeAssignId, permissionEntries);          
      }
      permissionEntries.add(permissionEntry);
      
    }      
    return result;
  }
  
  /**
   * role id to permission entries
   * @param permissionEntrySet
   * @return the map
   */
  private static Map<String, Set<PermissionEntry>> roleIdsToPermissionEntry(Collection<PermissionEntry> permissionEntrySet) {
    Map<String, Set<PermissionEntry>> result = new HashMap<String, Set<PermissionEntry>>();
    
    for (PermissionEntry permissionEntry : permissionEntrySet) {
      
      String roleId = permissionEntry.getRoleId();
      
      Set<PermissionEntry> permissionEntries = result.get(roleId);
      if (permissionEntries == null) {
        
        permissionEntries = new HashSet<PermissionEntry>();
        result.put(roleId, permissionEntries);          
      }
      permissionEntries.add(permissionEntry);
      
    }      
    return result;
  }
  
  /**
   * role id / member id to permission entries
   * @param permissionEntrySet
   * @return the map
   */
  private static Map<MultiKey, Set<PermissionEntry>> roleIdsMemberIdsToPermissionEntry(Collection<PermissionEntry> permissionEntrySet) {
    Map<MultiKey, Set<PermissionEntry>> result = new HashMap<MultiKey, Set<PermissionEntry>>();
    
    for (PermissionEntry permissionEntry : permissionEntrySet) {
      
      String roleId = permissionEntry.getRoleId();
      String memberId = permissionEntry.getMemberId();
      
      MultiKey multiKey = new MultiKey(roleId, memberId);
      
      Set<PermissionEntry> permissionEntries = result.get(multiKey);
      if (permissionEntries == null) {
        
        permissionEntries = new HashSet<PermissionEntry>();
        result.put(multiKey, permissionEntries);          
      }
      permissionEntries.add(permissionEntry);
      
    }      
    return result;
  }
  
  /**
   * get attribute assign ids for permission entries
   * @param permissionEntrySet
   * @return attribute assign ids
   */
  private static Set<String> attributeAssignIds(Collection<PermissionEntry> permissionEntrySet) {
    Set<String> result = new HashSet<String>();
    for (PermissionEntry permissionEntry : permissionEntrySet) {
      
      String attributeAssignId = permissionEntry.getAttributeAssignId();
      result.add(attributeAssignId);
    }
    return result;
  }
  
  /**
   * get role ids for permissions
   * @param permissionEntrySet
   * @return role ids
   */
  private static Set<String> roleIds(Collection<PermissionEntry> permissionEntrySet) {
    Set<String> result = new HashSet<String>();
    
    for (PermissionEntry permissionEntry : permissionEntrySet) {
      result.add(permissionEntry.getRoleId());
    }
    return result;
  }
  
  /**
   * get role ids for permissions
   * @param permissionEntrySet
   * @return role ids
   */
  private static Set<MultiKey> roleMemberIds(Collection<PermissionEntry> permissionEntrySet) {
    Set<MultiKey> result = new HashSet<MultiKey>();
    
    for (PermissionEntry permissionEntry : permissionEntrySet) {
      result.add(new MultiKey(permissionEntry.getRoleId(), permissionEntry.getMemberId()));
    }
    return result;
  }
  
  /**
   * add a limit to matching permission entries
   * @param limitAssign
   * @param permissionEntries
   * @param limitAssignsMap
   */
  private static void addLimitToPermissionEntries(AttributeAssign limitAssign, Collection<PermissionEntry> permissionEntries, Map<PermissionEntry, Set<AttributeAssign>> limitAssignsMap) {
    
    for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {
      Set<AttributeAssign> assignSet = limitAssignsMap.get(limitAssign.getId());
      if (assignSet == null) {
        assignSet = new HashSet<AttributeAssign>();
        limitAssignsMap.put(permissionEntry, assignSet);
      }
      assignSet.add(limitAssign);
    }

  }
  
  
  
  /**
   * find permission limits based on the permission entries.  Note, every input will be in result map, 
   * though the limits might be null or empty
   * @param permissionEntrySet
   * @return the map that finds permission limits based on permission entries
   */
  public static Map<PermissionEntry, Set<PermissionLimitBean>> findPermissionLimits(
      Collection<PermissionEntry> permissionEntrySet) {
    
    Map<PermissionEntry, Set<PermissionLimitBean>> result = new LinkedHashMap<PermissionEntry, Set<PermissionLimitBean>>();
    
    if (GrouperUtil.length(permissionEntrySet) == 0) {
      return result;
    }

    //############################## get limits by attribute assign id
    
    {
      //lets get the limits...
      final Set<String> attributeAssignIds = attributeAssignIds(permissionEntrySet);

      //lets get the limits...
      final Set<String> roleIds = roleIds(permissionEntrySet);

      final Set<MultiKey> roleMemberIds = roleMemberIds(permissionEntrySet);

      final Map<String, Set<PermissionEntry>> attributeAssignIdToPermissionEntries = attributeAssignIdsToPermissionEntry(permissionEntrySet);
      
      final Map<String, Set<PermissionEntry>> roleIdToPermissionEntries = roleIdsToPermissionEntry(permissionEntrySet);
      
      final Map<MultiKey, Set<PermissionEntry>> roleIdMemberIdToPermissionEntries = roleIdsMemberIdsToPermissionEntry(permissionEntrySet);
      
      //map from attribute assign id to the limit assign
      final Map<PermissionEntry, Set<AttributeAssign>> limitAssignsMap = new HashMap<PermissionEntry, Set<AttributeAssign>>();
      
      //map from the limit assign id to the values
      final Map<String, Set<AttributeAssignValue>> limitAssignValuesMap = new HashMap<String, Set<AttributeAssignValue>>();
      
      //lets get the limits for those attribute assign ids
      //do this as grouper system so we dont miss any
      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Set<AttributeAssign> allLimitAssignments = new HashSet<AttributeAssign>();
          
          //find assignments on assignments
          {
            Set<AttributeAssign> limitAssignments = GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getAttributeAssign()
                .findAssignmentsOnAssignmentsByIds(attributeAssignIds, null, AttributeDefType.limit, true));
            
            allLimitAssignments.addAll(limitAssignments);
            
            //keep track of limit assignments on permission assignments
            for (AttributeAssign limitAssign : limitAssignments) {
              
              Set<PermissionEntry> permissionEntries = attributeAssignIdToPermissionEntries.get(limitAssign.getId());
              
              addLimitToPermissionEntries(limitAssign, permissionEntries, limitAssignsMap);
            }
          }
          
          //find assignments on role
          {
            int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(roleIds), 100);
            Set<AttributeAssign> roleAssigns = new HashSet<AttributeAssign>();
            for (int i=0;i<numberOfBatches; i++) {
              
              List<String> currentBatch = GrouperUtil.batchList(roleIds, 100, i);
              Set<AttributeAssign> currentAttributeAssignSet = GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(null, null, null, currentBatch, null, true, false, AttributeDefType.limit));
              roleAssigns.addAll(GrouperUtil.nonNull(currentAttributeAssignSet));
            }
            
            allLimitAssignments.addAll(roleAssigns);

            for (AttributeAssign limitAssign : roleAssigns) {
              
              Set<PermissionEntry> permissionEntries = roleIdToPermissionEntries.get(limitAssign.getOwnerGroupId());
              addLimitToPermissionEntries(limitAssign, permissionEntries, limitAssignsMap);
              
            }
            
          }
          
          //find assignments on role/member (any membership)
          {
            int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(roleMemberIds), 50);
            Set<AttributeAssign> roleMemberAssigns = new HashSet<AttributeAssign>();
            for (int i=0;i<numberOfBatches; i++) {
              
              List<MultiKey> currentBatch = GrouperUtil.batchList(roleMemberIds,50, i);
              Set<AttributeAssign> currentAttributeAssignSet = GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, currentBatch, null, true, false, AttributeDefType.limit));
              roleMemberAssigns.addAll(GrouperUtil.nonNull(currentAttributeAssignSet));
            }
            
            allLimitAssignments.addAll(roleMemberAssigns);

            for (AttributeAssign limitAssign : roleMemberAssigns) {
              
              Set<PermissionEntry> permissionEntries = roleIdMemberIdToPermissionEntries.get(new MultiKey(limitAssign.getOwnerGroupId(), limitAssign.getOwnerMemberId()));
              addLimitToPermissionEntries(limitAssign, permissionEntries, limitAssignsMap);
              
            }
            
          }
          
          //get the values of the assignments
          if (GrouperUtil.length(limitAssignsMap) > 0) {
            Set<String> limitAssignIds = new HashSet<String>();
            
            //get all the assign ids
            for (AttributeAssign limitAssign : allLimitAssignments) {
              limitAssignIds.add(limitAssign.getId());
            }
  
            //get all assigns at once
            Set<AttributeAssignValue> limitAssignValues = GrouperDAOFactory.getFactory()
              .getAttributeAssignValue().findByAttributeAssignIds(limitAssignIds);
            
            for (AttributeAssignValue limitAssignValue : GrouperUtil.nonNull(limitAssignValues)) {
              Set<AttributeAssignValue> limitAssignValuesSet = limitAssignValuesMap.get(limitAssignValue.getAttributeAssignId());
              if (limitAssignValuesSet == null) {
                limitAssignValuesSet = new LinkedHashSet<AttributeAssignValue>();
                limitAssignValuesMap.put(limitAssignValue.getAttributeAssignId(), limitAssignValuesSet);
              }
              limitAssignValuesSet.add(limitAssignValue);
            }
            
          }
          return null;
        }
      });
      
      for (PermissionEntry permissionEntry : permissionEntrySet) {
        
        Set<AttributeAssign> limitAttributeAssigns = limitAssignsMap.get(permissionEntry);
        if (GrouperUtil.length(limitAttributeAssigns) > 0) {
          //Map<PermissionEntry, Set<PermissionLimitBean>>
          Set<PermissionLimitBean> permissionLimitBeanSet = result.get(permissionEntry);
          if (permissionLimitBeanSet == null) {
            permissionLimitBeanSet = new LinkedHashSet<PermissionLimitBean>();
            result.put(permissionEntry, permissionLimitBeanSet);
          }
          
          for (AttributeAssign limitAssign : limitAttributeAssigns) {
            PermissionLimitBean permissionLimitBean = new PermissionLimitBean();
            permissionLimitBean.setPermissionLimitBeanType(PermissionLimitBeanType.ATTRIBUTE_ASSIGNMENT);
            permissionLimitBean.setLimitAssign(limitAssign);
            permissionLimitBean.setLimitAssignValues(limitAssignValuesMap.get(limitAssign.getId()));
            permissionLimitBeanSet.add(permissionLimitBean);
          }
          
        }
      }
      //init uninitialized values to null
      for (PermissionEntry permissionEntry : permissionEntrySet) {
        
        if (!limitAssignsMap.containsKey(permissionEntry)) {
          result.put(permissionEntry, null);
        }
      }
            
    }    
    
    return result;
    
  }

  /** the type of this permission limit, e.g. permission assign, role, role membership, etc */
  private PermissionLimitBeanType permissionLimitBeanType;

  /**
   * the type of this permission limit, e.g. permission assign, role, role membership, etc
   * @return the type of permission limit
   */
  public PermissionLimitBeanType getPermissionLimitBeanType() {
    return this.permissionLimitBeanType;
  }

  /**
   * the type of this permission limit, e.g. permission assign, role, role membership, etc.
   * @param permissionLimitBeanType1
   */
  public void setPermissionLimitBeanType(PermissionLimitBeanType permissionLimitBeanType1) {
    this.permissionLimitBeanType = permissionLimitBeanType1;
  }
  
  /** the attribute assignment of the permission limit */
  private AttributeAssign limitAssign;

  /**
   * the attribute assignment of the permission limit
   * @return the attribute assignment of the permission limit
   */
  public AttributeAssign getLimitAssign() {
    return this.limitAssign;
  }

  /**
   * the attribute assignment of the permission limit
   * @param attributeAssign1
   */
  public void setLimitAssign(AttributeAssign attributeAssign1) {
    this.limitAssign = attributeAssign1;
  }
  
  /**
   * the values on this assign which (if not a marker) configure the limit, e.g. the EL string, or amount value etc
   */
  private Set<AttributeAssignValue> limitAssignValues;

  /**
   * the values on this assign which (if not a marker) configure the limit, e.g. the EL string, or amount value etc
   * @return the values on this assign which (if not a marker) configure the limit, e.g. the EL string, or amount value etc
   */
  public Set<AttributeAssignValue> getLimitAssignValues() {
    return this.limitAssignValues;
  }

  /**
   * the values on this assign which (if not a marker) configure the limit, e.g. the EL string, or amount value etc
   * @param attributeAssignValues1
   */
  public void setLimitAssignValues(Set<AttributeAssignValue> attributeAssignValues1) {
    this.limitAssignValues = attributeAssignValues1;
  }
  
  
  
}
