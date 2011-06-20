/**
 * 
 */
package edu.internet2.middleware.grouper.permissions;


/**
 * 
 * See why one PermissionHeuristics is better than another one
 * 
 * @author mchyzer
 *
 */
public class PermissionHeuristicBetter {

  /** this permision heuristic, which is better */
  private PermissionHeuristic thisPermissionHeuristic;
  
  /** other permission heuristic, to show the depth difference */
  private PermissionHeuristic otherPermissionHeuristic;

  /**
   * this permision heuristic, which is better
   * @return this permision heuristic, which is better
   */
  public PermissionHeuristic getThisPermissionHeuristic() {
    return this.thisPermissionHeuristic;
  }
  
  /**
   * this permision heuristic, which is better
   * @param thisPermissionHeuristic1
   */
  public void setThisPermissionHeuristic(PermissionHeuristic thisPermissionHeuristic1) {
    this.thisPermissionHeuristic = thisPermissionHeuristic1;
  }

  /**
   * other permission heuristic, to show the depth difference
   * @return other permission heuristic, to show the depth difference
   */
  public PermissionHeuristic getOtherPermissionHeuristic() {
    return this.otherPermissionHeuristic;
  }

  /**
   * other permission heuristic, to show the depth difference
   * @param otherPermissionHeuristic1
   */
  public void setOtherPermissionHeuristic(PermissionHeuristic otherPermissionHeuristic1) {
    this.otherPermissionHeuristic = otherPermissionHeuristic1;
  }
  
  
  
}
