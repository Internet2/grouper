/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PermissionEntryUtils {

  /**
   * if internal heuristic is not set, set it, order by so most important as at top...
   * @param permissionEntries
   */
  public static void orderByAndSetFriendlyHeuristic(List<PermissionEntry> permissionEntries) {
    if (GrouperUtil.length(permissionEntries) < 1) {
      return;
    }
    
    for (PermissionEntry permissionEntry : permissionEntries) {
      PermissionHeuristics permissionHeuristics = permissionEntry.getPermissionHeuristics();
      if (permissionHeuristics == null) {
        permissionHeuristics = new PermissionHeuristics();
        permissionEntry.setPermissionHeuristics(permissionHeuristics);
      }
      if (permissionHeuristics.getInternalScore() == -1) {
        long internalScore = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        permissionHeuristics.setInternalScore(internalScore);
      }
    }
    
    Collections.sort(permissionEntries, new Comparator<PermissionEntry>() {

      public int compare(PermissionEntry o1, PermissionEntry o2) {
        PermissionHeuristics permissionHeuristics1 = o1.getPermissionHeuristics();
        PermissionHeuristics permissionHeuristics2 = o2.getPermissionHeuristics();
        
        Long score1 = permissionHeuristics1.getInternalScore();
        Long score2 = permissionHeuristics2.getInternalScore();
        
        return score2.compareTo(score1);
      }

    });
    
    int previousFriendlyScore = 0;
    long previousHeuristic = -1;
    for (PermissionEntry permissionEntry : permissionEntries) {
      PermissionHeuristics permissionHeuristics = permissionEntry.getPermissionHeuristics();
      if (previousHeuristic == -1) {
        permissionHeuristics.setFriendlyScore(1);
      } else {
        //if equal then same score
        if (permissionHeuristics.getInternalScore() == previousHeuristic) {
          permissionHeuristics.setFriendlyScore(previousFriendlyScore);
        } else {
          permissionHeuristics.setFriendlyScore(previousFriendlyScore + 1);
        }
      }
      
      
      previousFriendlyScore = permissionHeuristics.getFriendlyScore();
      previousHeuristic = permissionHeuristics.getInternalScore();
      
      
    }  
  }
  
  /**
   * see if a permission is in the list of entries
   * @param permissionEntries
   * @param roleName
   * @param attributeDefNameName
   * @param action
   * @param subjectSourceId
   * @param subjectId
   * @return true if the item is in the list
   */
  public static boolean collectionContains(Collection<PermissionEntry> permissionEntries, String roleName, 
      String attributeDefNameName, String action, String subjectSourceId, String subjectId) {
    return collectionFindFirst(permissionEntries, roleName, attributeDefNameName, action, subjectSourceId, subjectId, null, false) != null;
  }
    
  /**
   * find the first permission entry in the list of entries
   * @param permissionEntries
   * @param roleName
   * @param attributeDefNameName
   * @param action
   * @param subjectSourceId
   * @param subjectId
   * @param permissionType e.g. role or role_subject
   * @return true if the item is in the list
   */
  public static PermissionEntry collectionFindFirst(Collection<PermissionEntry> permissionEntries, String roleName, 
      String attributeDefNameName, String action, String subjectSourceId, String subjectId, String permissionType) {
    return collectionFindFirst(permissionEntries, roleName, attributeDefNameName, action, subjectSourceId, subjectId, permissionType, true);
  }
  
  
  /**
   * find the first permission entry in the list of entries
   * @param permissionEntries
   * @param roleName
   * @param attributeDefNameName
   * @param action
   * @param subjectSourceId
   * @param subjectId
   * @param permissionType e.g. role or role_subject
   * @param considerPermissionType 
   * @return true if the item is in the list
   */
  public static PermissionEntry collectionFindFirst(Collection<PermissionEntry> permissionEntries, String roleName, 
      String attributeDefNameName, String action, String subjectSourceId, String subjectId, String permissionType, boolean considerPermissionType) {
    for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {
      if (StringUtils.equals(roleName, permissionEntry.getRoleName())
          && StringUtils.equals(attributeDefNameName, permissionEntry.getAttributeDefNameName())
          && StringUtils.equals(action, permissionEntry.getAction())
          && StringUtils.equals(subjectSourceId, permissionEntry.getSubjectSourceId())
          && StringUtils.equals(subjectId, permissionEntry.getSubjectId())
          && (considerPermissionType ? StringUtils.equals(permissionType, permissionEntry.getPermissionTypeDb()) : true)
           ) {
        return permissionEntry;
      }
    }
    return null;
  }
}
