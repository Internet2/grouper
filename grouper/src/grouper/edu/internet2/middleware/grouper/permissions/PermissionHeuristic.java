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
/**
 * 
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * <pre>
 * this takes a permissions assignment (PermissionEntry), and 
 * gives it a score so you can find the best entry, even if it is 
 * a tie, to decide if allow or deny.
 * 
 * Max depth resource: 30 (configurable in grouper.properties)
 * 
 * (864000) person/role assignment
 * 
 * (432000) direct role assignment (14400 * 30)
 * 
 * (14400) role assignment with role depth 29
 * 
 * (7200) assignment to user as opposed to group
 *
 * (3600) direct resource assignment (120*30)
 * 
 * (120) indirect direct resource assignment depth 29
 *
 * (60) direct action assignment (2*30)
 *
 * (58) action depth 1
 *
 * (56) action depth 2
 *
 * (54) action depth 3
 *
 * (2) action depth 29
 *
 * (1) allow
 *
 * </pre>
 * @author mchyzer
 *
 */
public class PermissionHeuristic {

  /**
   * if this is a heuristic which has depth, then this is it, or -1
   */
  private int depth = -1;

  /**
   * type of heuristic
   */
  private PermissionHeuristicType permissionHeuristicType;
  
  /**
   * type of heuristic
   * @return type
   */
  public PermissionHeuristicType getPermissionHeuristicType() {
    return this.permissionHeuristicType;
  }

  /**
   * type of heuristic
   * @param permissionHeuristicType1
   */
  public void setPermissionHeuristicType(PermissionHeuristicType permissionHeuristicType1) {
    this.permissionHeuristicType = permissionHeuristicType1;
  }

  /**
   * @see Object#equals(Object)
   * @return object equals
   */
  @Override
  public boolean equals(Object obj) {
    
    if (!(obj instanceof PermissionHeuristic)) {
      return false;
    }
    PermissionHeuristic otherPermissionHeuristic = (PermissionHeuristic)obj;
    
    return new EqualsBuilder().append(this.depth, otherPermissionHeuristic.depth)
      .append(this.permissionHeuristicType, otherPermissionHeuristic.permissionHeuristicType).isEquals();
  }


  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.depth).append(this.permissionHeuristicType).toHashCode();
  }


  /**
   * if this is a heuristic which has depth, then this is it, or -1
   * @return heuristic
   */
  public int getDepth() {
    return this.depth;
  }


  /**
   * if this is a heuristic which has depth, then this is it, or -1
   * @param depth1
   */
  public void setDepth(int depth1) {
    this.depth = depth1;
  }


  /**
   * which type of heuristic (not including depth)
   * @author mchyzer
   *
   */
  public static enum PermissionHeuristicType {

    /**
     * (864000) person/role assignment
     */
    personRole {

      /**
       * @see PermissionHeuristicType#maxScore()
       */
      @Override
      public long maxScore() {
        return role.maxScore() * 2;
      }
      
      /**
       * @see PermissionHeuristicType#minScore()
       */
      @Override
      public long minScore() {
        return role.maxScore() * 2;
      }
      
    },
    
    /**
     * (432000) direct role assignment (14400 * 30)
     * 
     * (14400) role assignment with role depth 29
     * 
     */
    role {

      /**
       * @see PermissionHeuristicType#maxScore()
       */
      @Override
      public long maxScore() {
        return minScore() * maxDepth();
      }
      
      /**
       * @see PermissionHeuristicType#minScore()
       */
      @Override
      public long minScore() {
        return assignedToUserNotGroup.maxScore() * 2;
      }
    },

    /**
     * (7200) assignment to user as opposed to group
     */
    assignedToUserNotGroup {

      /**
       * @see PermissionHeuristicType#maxScore()
       */
      @Override
      public long maxScore() {
        return resource.maxScore() * 2;
      }
      
      /**
       * @see PermissionHeuristicType#minScore()
       */
      @Override
      public long minScore() {
        return resource.maxScore() * 2;
      }
      
    },

    /**
     * (3600) direct resource assignment (120*30)
     * 
     * (120) indirect direct resource assignment depth 29
     */
    resource {

      /**
       * @see PermissionHeuristicType#maxScore()
       */
      @Override
      public long maxScore() {
        return minScore() * maxDepth();
      }
      
      
      /**
       * @see PermissionHeuristicType#minScore()
       */
      @Override
      public long minScore() {
        return action.maxScore() * 2;
      }


    },
    
    /**
     * (58) action depth 1
     *
     * (56) action depth 2
     *
     * (54) action depth 3
     *
     * (2) action depth 29
     */
    action {

      /**
       * @see PermissionHeuristicType#maxScore()
       */
      @Override
      public long maxScore() {
        return minScore() * maxDepth();
      }

      
      /**
       * @see PermissionHeuristicType#minScore()
       */
      @Override
      public long minScore() {
        return allow.maxScore() * 2;
      }

    },

    /**
     *
     * (1) allow
     */
    allow {

      /**
       * @see PermissionHeuristicType#maxScore()
       */
      @Override
      public long maxScore() {
        return 1;
      }
      
      /**
       * @see PermissionHeuristicType#minScore()
       */
      @Override
      public long minScore() {
        return 1;
      }
      
    };
    
    /**
     * max score if this is assigned with 0 depth (if applicable) and nothing else)
     * @return max score
     */
    public abstract long maxScore();

    /**
     * min score if this is assigned with 29 depth (if applicable) and nothing else, assuming 30 is max depth
     * @return max score
     */
    public abstract long minScore();
    
  }
  
  /**
   * get all types ordered by most important
   * @return all types
   */
  public static List<PermissionHeuristicType> allTypesOrdered() {
    return GrouperUtil.toList(PermissionHeuristicType.personRole, PermissionHeuristicType.role,
        PermissionHeuristicType.assignedToUserNotGroup, PermissionHeuristicType.resource,
        PermissionHeuristicType.action, PermissionHeuristicType.allow);
  }
  
  /**
   * computer heuristics
   * 
   * @param score
   * @return the list of heuristics, e.g. for comparison
   */
  public static PermissionHeuristics computeHeuristics(long score) {
    
    long originalScore = score;
    
    List<PermissionHeuristic> permissionHeuristicsList = new ArrayList<PermissionHeuristic>();
    
    //this is the ordered list
    
    //GrouperUtil.toList(PermissionHeuristicType.personRole, PermissionHeuristicType.role,
    //    PermissionHeuristicType.assignedToUserNotGroup, PermissionHeuristicType.resource,
    //    PermissionHeuristicType.action, PermissionHeuristicType.allow)
    
    if (score >= PermissionHeuristicType.personRole.minScore()) {
      PermissionHeuristic permissionHeuristic = new PermissionHeuristic();
      //there isnt really a depth here
      permissionHeuristic.setPermissionHeuristicType(PermissionHeuristicType.personRole);
      permissionHeuristicsList.add(permissionHeuristic);
      score -= PermissionHeuristicType.personRole.minScore();
    }
    
    if (score >= PermissionHeuristicType.role.minScore()) {
      PermissionHeuristic permissionHeuristic = new PermissionHeuristic();
      permissionHeuristic.setPermissionHeuristicType(PermissionHeuristicType.role);
      permissionHeuristicsList.add(permissionHeuristic);
      
      //lets see how big the depth is
      int depth = (int)(score / PermissionHeuristicType.role.minScore());
      
      score -= depth * PermissionHeuristicType.role.minScore();
      
      depth = maxDepth() - depth;
      permissionHeuristic.setDepth(depth);
      

    }
    
    if (score >= PermissionHeuristicType.assignedToUserNotGroup.minScore()) {
      PermissionHeuristic permissionHeuristic = new PermissionHeuristic();

      //there isnt really a depth here
      permissionHeuristic.setPermissionHeuristicType(PermissionHeuristicType.assignedToUserNotGroup);
      permissionHeuristicsList.add(permissionHeuristic);
      score -= PermissionHeuristicType.assignedToUserNotGroup.minScore();
    }
    
    if (score >= PermissionHeuristicType.resource.minScore()) {
      PermissionHeuristic permissionHeuristic = new PermissionHeuristic();
      permissionHeuristic.setPermissionHeuristicType(PermissionHeuristicType.resource);
      permissionHeuristicsList.add(permissionHeuristic);
      
      //lets see how big the depth is
      int depth = (int)(score / PermissionHeuristicType.resource.minScore());
      
      
      score -= depth * PermissionHeuristicType.resource.minScore();
      depth = maxDepth() - depth;
      permissionHeuristic.setDepth(depth);
    }
    
    if (score >= PermissionHeuristicType.action.minScore()) {
      PermissionHeuristic permissionHeuristic = new PermissionHeuristic();
      permissionHeuristic.setPermissionHeuristicType(PermissionHeuristicType.action);
      permissionHeuristicsList.add(permissionHeuristic);
      
      //lets see how big the depth is
      int depth = (int)(score / PermissionHeuristicType.action.minScore());
      
      score -= depth * PermissionHeuristicType.action.minScore();

      depth = maxDepth() - depth;
      permissionHeuristic.setDepth(depth);
    }
    
    if (score >= PermissionHeuristicType.allow.minScore()) {
      PermissionHeuristic permissionHeuristic = new PermissionHeuristic();

      //there isnt really a depth here
      permissionHeuristic.setPermissionHeuristicType(PermissionHeuristicType.allow);
      permissionHeuristicsList.add(permissionHeuristic);
      score -= PermissionHeuristicType.allow.minScore();
    }
    
    if (score != 0) {
      throw new RuntimeException("Why is resulting score not 0? " + originalScore + ", " + score + ", " + collectionToString(permissionHeuristicsList));
    }
    PermissionHeuristics permissionHeuristics = new PermissionHeuristics();
    permissionHeuristics.setPermissionHeuristicList(permissionHeuristicsList);
    permissionHeuristics.setInternalScore(originalScore);
    return permissionHeuristics;
    
  }
  
  /**
   * cache the max depth in grouper
   */
  static int maxDepth = -1;
  
  /**
   * max depth in grouper
   * @return max depth
   */
  static int maxDepth() {
    if (maxDepth == -1) {
      synchronized(PermissionHeuristic.class) {
        maxDepth = GrouperConfig.retrieveConfig().propertyValueInt("grouper.max.permission.depth", 30);
      }
    }
    return maxDepth;
  }
  
  /**
   * compute a heuristic based on how important the aspects of the permissionEntry are
   * see the class javadoc for more info
   * @param permissionEntry
   * @return the number
   */
  public static long computePermissionHeuristic(PermissionEntry permissionEntry) {
    
    //* (864000) person/role assignment
    //* 
    //* (432000) direct role assignment (14400 * 30)
    //* 
    //* (14400) role assignment with role depth 29
    //* 
    //* (7200) assignment to user as opposed to group
    //*
    //* (3600) direct resource assignment (120*30)
    //* 
    //* (120) indirect direct resource assignment depth 29
    //*
    //* (60) direct action assignment (2*30)
    //*
    //* (58) action depth 1
    //*
    //* (56) action depth 2
    //*
    //* (54) action depth 3
    //*
    //* (2) action depth 29
    //*
    //* (1) allow
    
    long result = 0;
    
    //if not enabled, then -1...
    if (permissionEntry instanceof PermissionEntryImpl && !permissionEntry.isEnabled()) {
      return -1;
    }
    
    //if allow, add one
    if (!permissionEntry.isDisallowed()) {
      result += 1;
    }
    
    int maxDepth = maxDepth();
    
    long currentMaxScore = 2;

    //process the action depth
    {
      int permissionActionDepth = Math.min(permissionEntry.getAttributeAssignActionSetDepth(), maxDepth);
      
      result += (maxDepth - permissionActionDepth) * currentMaxScore;
    }
    
    //double the max so far
    currentMaxScore = 2*maxDepth*currentMaxScore;
    
    //process the resource depth
    {
      int permissionResourceDepth = Math.min(permissionEntry.getAttributeDefNameSetDepth(), maxDepth);
      
      result += (maxDepth - permissionResourceDepth) * currentMaxScore;
    }
    
    //double the max so far
    currentMaxScore = 2*maxDepth*currentMaxScore;
    
    //user as opposed to group... I dont really have a way to get that at this point... need more cols in view
    //keep a placeholder for the future
    currentMaxScore *= 2;
    
    if (permissionEntry.getPermissionType() != PermissionType.role_subject) { 
      //process the role inheritance depth
      {
        int roleInheritanceDepth = Math.min(permissionEntry.getRoleSetDepth(), maxDepth);
        
        result += (maxDepth - roleInheritanceDepth) * currentMaxScore;
      }
    }
    
    //double the max so far
    currentMaxScore = 2*maxDepth*currentMaxScore;
    
    if (permissionEntry.getPermissionType() == PermissionType.role_subject) {
      result += currentMaxScore;
    }
    
    currentMaxScore *= 2;
    return result;
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (this.permissionHeuristicType == null) {
      result.append("null");
    } else {
      result.append(this.permissionHeuristicType.name());
    }
    if (this.depth != -1) {
      result.append("(").append(this.depth).append(")");
    }
    return result.toString();
  }
  
  /**
   * convert a collection to string
   * @param permissionHeuristics
   * @return the string
   */
  public static String collectionToString(Collection<PermissionHeuristic> permissionHeuristics) {
    StringBuilder result = new StringBuilder();
    if (permissionHeuristics == null) {
      return "null";
    }
    for (PermissionHeuristic permissionHeuristic : permissionHeuristics) {
      result.append(permissionHeuristic.toString());
    }
    return result.toString();
  }
  
}
