/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * if processing permissions, you can filter out either redundant permissions (find best in set),
 * or do that and filter out redundant roles (if flattening roles) (find best in set)
 */
public enum PermissionProcessor {

  /** this will see if there are two rows with the same 
   * role/subject/permissionName/action, pick the best one, and remove the others */
  FILTER_REDUNDANT_PERMISSIONS {

    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionProcessor#processPermissions(java.util.Set)
     */
    @Override
    public void processPermissions(Set<PermissionEntry> permissionEntrySet) {
      
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
        PermissionEntry.orderByAndSetFriendlyHeuristic(permissionEntryList);
        
        //the first one is the one we want
        permissionEntrySet.add(permissionEntryList.get(0));
      }

    }
  },
  
  /**
   * if there are two entries for the same subject/permissionName/action in different roles, it will pick the best one, and remove the others
   */
  FILTER_REDUNDANT_PERMISSIONS_AND_ROLES {

    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionProcessor#processPermissions(java.util.Set)
     */
    @Override
    public void processPermissions(Set<PermissionEntry> permissionEntrySet) {

      if (GrouperUtil.length(permissionEntrySet) <= 1) {
        return;
      }
      
      //first filter the permissions
      FILTER_REDUNDANT_PERMISSIONS.processPermissions(permissionEntrySet);
      
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
            if (!permissionEntry.isDisallowed()) {
              permissionEntrySet.add(permissionEntry);
              continue OUTER;
            }
          }
        }
        
        //well, just get the first disallow
        permissionEntrySet.add(permissionEntryList.get(0));
      }
    }
  };
  
  /**
   * filer permissions out which can be pruned based on the type of processor
   * @param permissionEntrySet
   */
  public abstract void processPermissions(Set<PermissionEntry> permissionEntrySet);
  
}
