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
public class AsasGroups {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    AsasGroups asasGroups = new AsasGroups();
    List<AsasGroup> asasGroupList = new ArrayList<AsasGroup>();
    AsasGroup asasGroup = new AsasGroup();
    asasGroup.setId("id");
    asasGroup.setName("name");
    asasGroupList.add(asasGroup);
    asasGroups.setAsasGroups(asasGroupList);
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasGroups), true);
    
    System.out.println(string);
    
    string = StandardApiServerUtils.indent(AsasRestContentType.xml.writeString(asasGroups), true);
    
    System.out.println(string);
    
  }
  
  /**
   * list of groups
   */
  private List<AsasGroup> asasGroups = null;

  
  /**
   * @return the asasGroups
   */
  public List<AsasGroup> getAsasGroups() {
    return asasGroups;
  }

  
  /**
   * @param asasGroups the asasGroups to set
   */
  public void setAsasGroups(List<AsasGroup> asasGroups) {
    this.asasGroups = asasGroups;
  }
  
  
  
}
