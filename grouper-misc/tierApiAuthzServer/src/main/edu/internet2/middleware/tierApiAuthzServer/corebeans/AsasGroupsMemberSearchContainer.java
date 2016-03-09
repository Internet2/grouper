/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 * Has member
 * @author mchyzer
 *
 */
public class AsasGroupsMemberSearchContainer extends AsasUserContainer {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    AsasGroupsMemberSearchContainer asasGroupMemberContainer = new AsasGroupsMemberSearchContainer();
    
    asasGroupMemberContainer.setUserName("johnsmith");
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasGroupMemberContainer), true);
    
    System.out.println(string);
    
    
  }

  
}
