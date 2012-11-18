/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.corebeans;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.authzStandardApiServer.contentType.AsasRestContentType;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;


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
    List<AsasGroup> asasGroupList = new ArrayList<AsasGroup>();
    AsasGroup asasGroup = new AsasGroup();
    asasGroup.setId("id");
    asasGroup.setName("name");
    asasGroupList.add(asasGroup);
    asasGroupSearchContainer.setGroups(asasGroupList);
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasGroupSearchContainer), true);
    
    System.out.println(string);
    
    string = StandardApiServerUtils.indent(AsasRestContentType.xml.writeString(asasGroupSearchContainer), true);
    
    System.out.println(string);
    
  }

  
  /**
   * list of groups
   */
  private List<AsasGroup> groups = null;

  
  /**
   * @return the groups
   */
  public List<AsasGroup> getGroups() {
    return this.groups;
  }

  
  /**
   * @param groups the groups to set
   */
  public void setGroups(List<AsasGroup> groups1) {
    this.groups = groups1;
  }
  
  
  
}
