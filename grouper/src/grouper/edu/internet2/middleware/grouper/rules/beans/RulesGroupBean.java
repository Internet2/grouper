/*******************************************************************************
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * @author mchyzer
 *
 */
public class RulesGroupBean extends RulesBean {

  /**
   * 
   */
  public RulesGroupBean() {
    
  }
  
  /**
   * @see RulesBean#hasGroup()
   */
  @Override
  public boolean hasGroup() {
    return true;
  }

  /**
   * 
   * @param group
   */
  public RulesGroupBean(Group group) {
    super();
    this.group = group;
  }


  /** group */
  private Group group;

  /**
   * group
   * @return group
   */
  @Override
  public Group getGroup() {
    return group;
  }

  /**
   * group
   * @param group1
   */
  public void setGroup(Group group1) {
    this.group = group1;
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (this.group != null) {
      result.append("group: ").append(this.group.getName()).append(", ");
    }
    return result.toString();
  }
  
  
}
