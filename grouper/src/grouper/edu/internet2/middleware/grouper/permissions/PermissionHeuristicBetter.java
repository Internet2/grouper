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
