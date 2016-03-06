/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzClient.corebeans;

import java.util.List;


/**
 * Multiple groups
 * @author mchyzer
 *
 */
public class AsacGroupSearchContainer extends AsacResponseBeanBase {

  
  /**
   * list of groups
   */
  private List<AsacGroup> groups = null;

  
  /**
   * @return the groups
   */
  public List<AsacGroup> getGroups() {
    return this.groups;
  }

  
  /**
   * @param groups the groups to set
   */
  public void setGroups(List<AsacGroup> groups1) {
    this.groups = groups1;
  }
  
  
  
}
