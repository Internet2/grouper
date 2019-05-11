/**
 * 
 */
package edu.internet2.middleware.grouper.app.grouperTypes;

import edu.internet2.middleware.grouper.Stem;

/**
 * class to store stem and candidate object type
 */
public class StemObjectType {
  
  /**
   * stem with extension that matches one of the object types 
   */
  private Stem stem;
  
  /**
   * matching object type
   */
  private String objectType;

  public StemObjectType(Stem stem, String objectType) {
    super();
    this.stem = stem;
    this.objectType = objectType;
  }

  /**
   * stem with extension that matches one of the object types
   * @return
   */
  public Stem getStem() {
    return stem;
  }

  /**
   * matching object type
   * @return
   */
  public String getObjectType() {
    return objectType;
  }

  
}
