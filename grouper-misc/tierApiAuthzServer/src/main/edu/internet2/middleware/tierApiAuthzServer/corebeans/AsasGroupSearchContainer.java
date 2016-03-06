/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 * Multiple groups
 * @author mchyzer
 *
 */
public class AsasGroupSearchContainer extends AsasResponseBeanBase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    AsasGroupSearchContainer asasGroupSearchContainer = new AsasGroupSearchContainer();
    AsasGroup asasGroup = new AsasGroup();
    asasGroup.setId("id");
    asasGroup.setName("name");
    asasGroupSearchContainer.setGroups(new AsasGroup[]{asasGroup});
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasGroupSearchContainer), true);
    
    System.out.println(string);
    
    
  }

  
  /**
   * list of groups
   */
  private AsasGroup[] groups = null;

  
  /**
   * @return the groups
   */
  public AsasGroup[] getGroups() {
    return this.groups;
  }

  
  /**
   * @param groups the groups to set
   */
  public void setGroups(AsasGroup[] groups1) {
    this.groups = groups1;
  }
  
  
  
}
