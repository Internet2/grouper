/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitInterface;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;


/**
 * if processing permissions, you can filter out either redundant permissions (find best in set),
 * or do that and filter out redundant roles (if flattening roles) (find best in set)
 */
public enum PermissionProcessor {

  /** 
   * this will see if there are two rows with the same 
   * role/subject/permissionName/action, pick the best one, and remove the others 
   */
  FILTER_REDUNDANT_PERMISSIONS {

    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionProcessor#processPermissions(java.util.Collection, Map)
     */
    @Override
    public void processPermissions(Collection<PermissionEntry> permissionEntrySet, 
        Map<String, Object> limitEnvVars) {
      
      if (GrouperUtil.length(permissionEntrySet) <= 1) {
        return;
      }
      
      //at least we are doing the permissions... do those first
      //multikey is memberId, roleId, permissionNameId, action
      Map<MultiKey, List<PermissionEntry>> permissionMap = new LinkedHashMap<MultiKey, List<PermissionEntry>>();
      for (PermissionEntry permissionEntry : permissionEntrySet) {
        MultiKey key = new MultiKey(permissionEntry.getMemberId(), permissionEntry.getRoleId(), permissionEntry.getAttributeDefNameId(), permissionEntry.getAction());
        List<PermissionEntry> permissionList = permissionMap.get(key);
        if (permissionList == null) {
          permissionList = new ArrayList<PermissionEntry>();
          permissionMap.put(key, permissionList);
        }
        permissionList.add(permissionEntry);
      }
      
      //go through the map, and find the best permissions, and put back in set
      permissionEntrySet.clear();
      
      for (List<PermissionEntry> permissionEntryList : permissionMap.values()) {
        
        //set the heuristics
        PermissionEntryUtils.orderByAndSetFriendlyHeuristic(permissionEntryList);
        
        //the first one is the one we want
        permissionEntrySet.add(permissionEntryList.get(0));
      }

    }
  },
  
  /**
   * if there are two entries for the same subject/permissionName/action in different roles, 
   * it will pick the best one, and remove the others
   */
  FILTER_REDUNDANT_PERMISSIONS_AND_ROLES {

    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionProcessor#processPermissions(java.util.Collection, Map)
     */
    @Override
    public void processPermissions(Collection<PermissionEntry> permissionEntrySet, 
        Map<String, Object> limitEnvVars) {

      if (GrouperUtil.length(permissionEntrySet) <= 1) {
        return;
      }
      
      //first filter the permissions
      FILTER_REDUNDANT_PERMISSIONS.processPermissions(permissionEntrySet, limitEnvVars);
      
      filterRedundantRoles(permissionEntrySet);
      
    }
  }, 
  /** this will see if there are two rows with the same 
   * role/subject/permissionName/action, pick the best one, and remove the others */
  FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS {
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionProcessor#processPermissions(java.util.Collection, Map)
     */
    @Override
    public void processPermissions(Collection<PermissionEntry> permissionEntrySet, 
        Map<String, Object> limitEnvVars) {
      
      FILTER_REDUNDANT_PERMISSIONS.processPermissions(permissionEntrySet, limitEnvVars);
      PROCESS_LIMITS.processPermissions(permissionEntrySet, limitEnvVars);
  
    }
  }, 
  
  /**
   * if there are two entries for the same subject/permissionName/action in different roles, it will pick the best one, and remove the others
   */
  FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS {
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionProcessor#processPermissions(java.util.Collection, Map)
     */
    @Override
    public void processPermissions(Collection<PermissionEntry> permissionEntrySet, 
        Map<String, Object> limitEnvVars) {

      FILTER_REDUNDANT_PERMISSIONS.processPermissions(permissionEntrySet, limitEnvVars);
      
      //we need to process limits before looking at roles
      PROCESS_LIMITS.processPermissions(permissionEntrySet, limitEnvVars);
      filterRedundantRoles(permissionEntrySet);
    }
  }, 
  
  /** 
   * this will look at the permissions and see if there are limits assigned and see if the limits 
   * will rule out any of the entries 
   */
  PROCESS_LIMITS {
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionProcessor#processPermissions(java.util.Collection, Map)
     */
    @Override
    public void processPermissions(Collection<PermissionEntry> permissionEntrySet, 
        Map<String, Object> limitEnvVarsString) {
      
      //get limits from permissions
      Map<PermissionEntry, Set<PermissionLimitBean>> permissionLimitBeanMap = GrouperUtil.nonNull(PermissionLimitBean.findPermissionLimits(permissionEntrySet));
      
      processLimits(permissionEntrySet, limitEnvVarsString, permissionLimitBeanMap);
    }
  };
  
  /**
   * if any role has it, one of them stays
   * @param permissionEntrySet
   */
  private static void filterRedundantRoles(Collection<PermissionEntry> permissionEntrySet) {
    //now filter out roles
    //multikey is memberId, permissionNameId, action
    Map<MultiKey, List<PermissionEntry>> permissionMap = new LinkedHashMap<MultiKey, List<PermissionEntry>>();
    for (PermissionEntry permissionEntry : permissionEntrySet) {
      MultiKey key = new MultiKey(permissionEntry.getMemberId(), permissionEntry.getAttributeDefNameId(), permissionEntry.getAction());
      List<PermissionEntry> permissionList = permissionMap.get(key);
      if (permissionList == null) {
        permissionList = new ArrayList<PermissionEntry>();
        permissionMap.put(key, permissionList);
      }
      permissionList.add(permissionEntry);
    }
    
    //go through the map, and find the best permissions, and put back in set
    permissionEntrySet.clear();
    
    OUTER: for (List<PermissionEntry> permissionEntryList : permissionMap.values()) {
      
      if (permissionEntryList.size() > 1) {
        //if any in the list is an allow, then use it (allow from any role ok)
        for (PermissionEntry permissionEntry : permissionEntryList) {
          if (permissionEntry.isAllowedOverall()) {
            permissionEntrySet.add(permissionEntry);
            continue OUTER;
          }
        }
      }
      
      //well, just get the first disallow
      permissionEntrySet.add(permissionEntryList.get(0));
    }

  }
  
  /** keep the caches by how long they are cached m*/
  private static Map<Integer, ExpirableCache<MultiKey, Boolean>> limitLogicCaches = new HashMap<Integer, ExpirableCache<MultiKey, Boolean>>();
  
  /**
   * filer permissions out which can be pruned based on the type of processor
   * @param permissionEntrySet
   * @param limitEnvVars if processing limits, pass in a map of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  public abstract void processPermissions(Collection<PermissionEntry> permissionEntrySet, 
      Map<String, Object> limitEnvVars);

  /**
   * process limits on some permission entries
   * @param permissionEntrySet
   * @param limitEnvVarsString can have types in there or not
   * @param permissionLimitBeanMap the map of permission entry to its associated limits
   * you can get that with PermissionLimitBean.findPermissionLimits()
   */
  public static void processLimits(Collection<PermissionEntry> permissionEntrySet,
      Map<String, Object> limitEnvVarsString,
      Map<PermissionEntry, Set<PermissionLimitBean>> permissionLimitBeanMap) {
    //if there are string values, and needed to be typecast, do that here
    Map<String, Object> limitEnvVarsObject = GrouperUtil.typeCastStringStringMap(limitEnvVarsString);
    
    PermissionLimitUtils.addStandardLimitVariablesIfNotExist(limitEnvVarsObject);
    
    //TODO add logging in here
    for (PermissionEntry permissionEntry : permissionEntrySet) {
      
      Set<PermissionLimitBean> permissionLimitBeanSet = permissionLimitBeanMap.get(permissionEntry);
      
      for (PermissionLimitBean permissionLimitBean : GrouperUtil.nonNull(permissionLimitBeanSet)) {
        
        AttributeAssign limit = permissionLimitBean.getLimitAssign();
        
        Set<AttributeAssignValue> limitValues = permissionLimitBean.getLimitAssignValues();
        
        String limitName = limit.getAttributeDefName().getName();
        PermissionLimitInterface permissionLimitInterface = PermissionLimitUtils
          .logicInstance(limitName);
        
        if (permissionLimitInterface == null) {
          throw new RuntimeException("Cannot find logic class for limit: " + limitName);
        }
        
        //lets check the cache
        int cacheMinutes = permissionLimitInterface.cacheLimitValueResultMinutes();
        
        MultiKey multiKey = null;
        ExpirableCache<MultiKey, Boolean> cache = null;
        
        if (cacheMinutes > 0) {
          cache = limitLogicCaches.get(cacheMinutes);
          if (cache == null) {
            cache = new ExpirableCache<MultiKey, Boolean>(cacheMinutes);
            limitLogicCaches.put(cacheMinutes, cache);
          }
          
          //lets make the key
          List<Object> keyParts = new ArrayList<Object>();
          keyParts.add(limit.getAttributeDefNameId());
          keyParts.add(GrouperUtil.length(limitValues));
          keyParts.add(GrouperUtil.length(limitEnvVarsString));
          
          //lets add the limit values
          List<String> limitValuesStringList = new ArrayList<String>();
          for (AttributeAssignValue attributeAssignValue : GrouperUtil.nonNull(limitValues)) {
            limitValuesStringList.add(attributeAssignValue.valueString(false));
          }
          //sort so always the same
          Collections.sort(limitValuesStringList);
          for (String value : limitValuesStringList) {
            keyParts.add(value);
          }
          
          //get the map, sorted
          TreeMap<String, Object> sortedArgMap = new TreeMap<String, Object>(GrouperUtil.nonNull(limitEnvVarsString));
          for (String key : sortedArgMap.keySet()) {
            keyParts.add(key);
            keyParts.add(sortedArgMap.get(key));
          }
          multiKey = new MultiKey(keyParts.toArray());
          
          Boolean result = cache.get(multiKey);
          if (result != null) {
            if (!result) {
              permissionEntry.setAllowedOverall(false);
              continue;
            }
          }
        }

        //run the logic
        boolean allowed = permissionLimitInterface.allowPermission(permissionEntry, limit, limitValues, limitEnvVarsObject, permissionLimitBeanSet);
        
        if (!allowed) {
          permissionEntry.setAllowedOverall(false);
        }
        
        //cache the result?
        if (cacheMinutes > 0) {
          cache.put(multiKey, allowed);
        }
        
      }
    }
  }
  
  

}
