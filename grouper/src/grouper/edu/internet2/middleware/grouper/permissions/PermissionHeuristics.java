/**
 * Copyright 2012 Internet2
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

import java.util.List;

import edu.internet2.middleware.grouper.permissions.PermissionHeuristic.PermissionHeuristicType;

/**
 * collection of PermissionHeuristic
 * @author mchyzer
 *
 */
public class PermissionHeuristics {

  /**
   * see why this one is better than the arg
   * @param other
   * @return the heuristic
   */
  public PermissionHeuristicBetter whyBetterThanArg(PermissionHeuristics other) {
    
    if (this.internalScore <= other.internalScore) {
      return null;
    }

    PermissionHeuristicBetter permissionHeuristicBetter = new PermissionHeuristicBetter();
    
    //these arent calculated by default
    PermissionHeuristics thisHeuristics = PermissionHeuristic.computeHeuristics(this.internalScore);
    other = PermissionHeuristic.computeHeuristics(other.internalScore);
    
    //at this point we know that this is better than the arg... lets see why
    OUTER: for (PermissionHeuristic thisPermissionHeuristic : thisHeuristics.permissionHeuristicList) {

      //if its allow, then we are done
      if (thisPermissionHeuristic.getPermissionHeuristicType() == PermissionHeuristicType.allow) {
        permissionHeuristicBetter.setThisPermissionHeuristic(thisPermissionHeuristic);
        //there isnt an other
        return permissionHeuristicBetter;
      }

      long thisMaxScore = thisPermissionHeuristic.getPermissionHeuristicType().maxScore();
      
      //lets see if the other one has it
      for (PermissionHeuristic otherPermissionHeuristic : other.getPermissionHeuristicList()) {
        
        
        long otherMaxScore = otherPermissionHeuristic.getPermissionHeuristicType().maxScore();
        
        if (thisPermissionHeuristic.getPermissionHeuristicType() == otherPermissionHeuristic.getPermissionHeuristicType()) {
          
          if (thisPermissionHeuristic.getDepth() < otherPermissionHeuristic.getDepth()) {
            
            permissionHeuristicBetter.setThisPermissionHeuristic(thisPermissionHeuristic);
            permissionHeuristicBetter.setOtherPermissionHeuristic(otherPermissionHeuristic);
            return permissionHeuristicBetter;
            
          } else if (thisPermissionHeuristic.getDepth() > otherPermissionHeuristic.getDepth()) {
            
            throw new RuntimeException("Why is this depth more than the other??? " + this + ", " + other);
            
          } else {
            continue OUTER;
          }
          
        } else if (thisMaxScore > otherMaxScore) {

          //if this is more important than the other, then that shows something...
          permissionHeuristicBetter.setThisPermissionHeuristic(thisPermissionHeuristic);
          permissionHeuristicBetter.setOtherPermissionHeuristic(otherPermissionHeuristic);
          return permissionHeuristicBetter;
          
        }
        
      }
      //cant find it
      permissionHeuristicBetter.setThisPermissionHeuristic(thisPermissionHeuristic);
      return permissionHeuristicBetter;
    }
    
    //why havent we found it yet
    throw new RuntimeException("Why did we not find it??? " + this + ", " + other);
    
    
  }
  
  /** list of permission heuristics */
  private List<PermissionHeuristic> permissionHeuristicList;

  /**
   * friendly score which just ranks the list: 1, 2, 3, etc.  ties will get the same score
   */
  private int friendlyScore;

  /**
   * internal score will be a number which signifies how important, and can go back
   */
  private long internalScore = -1;

  /**
   * internal score will be a number which signifies how important, and can go back
   * @return internal score
   */
  public long getInternalScore() {
    return this.internalScore;
  }

  /**
   * internal score will be a number which signifies how important, and can go back
   * @param internalScore1
   */
  public void setInternalScore(long internalScore1) {
    this.internalScore = internalScore1;
  }

  /**
   * list of permission heuristics
   * @return list of permission heuristics
   */
  public List<PermissionHeuristic> getPermissionHeuristicList() {
    return this.permissionHeuristicList;
  }

  /**
   * list of permission heuristics
   * @param permissionHeuristicList1
   */
  public void setPermissionHeuristicList(List<PermissionHeuristic> permissionHeuristicList1) {
    this.permissionHeuristicList = permissionHeuristicList1;
  }

  /**
   * friendly score which just ranks the list: 1, 2, 3, etc.  ties will get the same score
   * @return the friendly score
   */
  public int getFriendlyScore() {
    return this.friendlyScore;
  }

  /**
   * friendly score which just ranks the list: 1, 2, 3, etc.  ties will get the same score
   * @param friendlyScore1
   */
  public void setFriendlyScore(int friendlyScore1) {
    this.friendlyScore = friendlyScore1;
  }
  
  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("friendlyScore: ").append(this.friendlyScore);
    result.append(", internalScore: ").append(this.internalScore);
    result.append(", permissionHeuristicList: ").append(PermissionHeuristic.collectionToString(this.permissionHeuristicList));
    return result.toString();
  }


  
}
