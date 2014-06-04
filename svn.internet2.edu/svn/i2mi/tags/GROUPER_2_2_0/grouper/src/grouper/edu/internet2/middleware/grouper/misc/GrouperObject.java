package edu.internet2.middleware.grouper.misc;

import java.util.Set;


/**
 * grouper objects extend this, e.g. groups, stems, attribute def names
 * @author mchyzer
 *
 */
public interface GrouperObject {

  /**
   * id of object
   * @return id
   */
  public String getId();
  
  /**
   * see if this object matches the filter strings
   * @param filterStrings
   * @return true if matches
   */
  public boolean matchesLowerSearchStrings(Set<String> filterStrings);
  
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
