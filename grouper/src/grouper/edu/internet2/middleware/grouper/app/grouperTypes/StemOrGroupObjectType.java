/**
 * 
 */
package edu.internet2.middleware.grouper.app.grouperTypes;

import edu.internet2.middleware.grouper.misc.GrouperObject;

/**
 * class to store stem/group and candidate object type
 */
public class StemOrGroupObjectType {
  
  // only group or stem allowed
  private GrouperObject grouperObject;
  
  /**
   * matching object type
   */
  private String objectType;
  
  public StemOrGroupObjectType(GrouperObject grouperObject, String objectType) {
    super();
    this.grouperObject = grouperObject;
    this.objectType = objectType;
  }

  /**
   * matching object type
   * @return
   */
  public String getObjectType() {
    return objectType;
  }
  
  public GrouperObject getGrouperObject() {
    return grouperObject;
  }

}
