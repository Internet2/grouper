package edu.internet2.middleware.grouper.permissions;

import java.util.List;

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
    
    //at this point we know that this is better than the arg... lets see why
    for (PermissionHeuristic thisPermissionHeuristic : this.permissionHeuristicList) {
      
      //lets see if the other one has it
      for (PermissionHeuristic otherPermissionHeuristic : other.getPermissionHeuristicList()) {
        
        if (thisPermissionHeuristic.getPermissionHeuristicType() == otherPermissionHeuristic.getPermissionHeuristicType()) {
          
          if (thisPermissionHeuristic.getDepth() < otherPermissionHeuristic.getDepth()) {
            
            permissionHeuristicBetter.setThisPermissionHeuristic(thisPermissionHeuristic);
            permissionHeuristicBetter.setOtherPermissionHeuristic(otherPermissionHeuristic);
            return permissionHeuristicBetter;
            
          } else if (thisPermissionHeuristic.getDepth() > otherPermissionHeuristic.getDepth()) {
            
            throw new RuntimeException("Why is this depth more than the other??? " + this + ", " + other);
            
          } else {
            continue;
          }
          
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
  private long internalScore;

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
