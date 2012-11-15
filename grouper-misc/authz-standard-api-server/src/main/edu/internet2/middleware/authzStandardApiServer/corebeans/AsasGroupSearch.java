/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.corebeans;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.authzStandardApiServer.contentType.AsasRestContentType;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.annotations.XStreamImplicit;


/**
 * Multiple groupList
 * @author mchyzer
 *
 */
public class AsasGroupSearch {

  /** paging information for response, if null there is no paging */
  private AsasPaging paging;
  
  /**
   * if null there is no paging
   * @return the paging
   */
  public AsasPaging getPaging() {
    return this.paging;
  }
  
  /**
   * if null there is no paging
   * @param paging1 the paging to set
   */
  public void setPaging(AsasPaging paging1) {
    this.paging = paging1;
  }


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    AsasGroupSearch asasGroups = new AsasGroupSearch();
    List<AsasGroup> asasGroupList = new ArrayList<AsasGroup>();
    AsasGroup asasGroup = new AsasGroup();
    asasGroup.setId("id");
    asasGroup.setName("name");
    asasGroupList.add(asasGroup);
    asasGroups.setGroupList(asasGroupList);
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasGroups), true);
    
    System.out.println(string);
    
    string = StandardApiServerUtils.indent(AsasRestContentType.xml.writeString(asasGroups), true);
    
    System.out.println(string);
    
  }
  
  /**
   * list of groupList
   */
  @XStreamImplicit
  private List<AsasGroup> groupList = null;

  
  /**
   * @return the groupList
   */
  public List<AsasGroup> getGroupList() {
    return this.groupList;
  }

  
  /**
   * @param groupList the groupList to set
   */
  public void setGroupList(List<AsasGroup> groups1) {
    this.groupList = groups1;
  }
  
  
  
}
