package edu.internet2.middleware.grouper.misc;


/**
 * grouper objects extend this, e.g. groups, stems, attribute def names
 * @author mchyzer
 *
 */
public interface GrouperObject {

  /**
   * name of object, e.g. a:b:c
   * @return the name of this object
   */
  public String getName();
  
  /**
   * description of object
   * @return description
   */
  public String getDescription();
  
  /**
   * display name of object
   * @return display name
   */
  public String getDisplayName();
}
