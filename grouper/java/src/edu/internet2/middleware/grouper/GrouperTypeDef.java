package edu.internet2.middleware.directory.grouper;

/** 
 * Class representing a type definition for a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperTypeDef.java,v 1.1 2004-07-26 17:03:49 blair Exp $
 */
public class GrouperGroupTypeDef {

  private int     groupType;
  private String  groupField;

  /*
   * XXX Is this class actually needed?
   */

  /**
   * Create a {@link GrouperGroupTypeDef} object.
   * <p>
   * XXX Is this class needed?  Or do {@link GrouperField} and 
   *     {@link GrouperType} provide everything this class might be
   *     needed for?
   */
  public GrouperGroupTypeDef() {
    // Nothing -- Yet
  }

  /*
   * Below for Hibernate
   */
  
  private int getGroupType() {
    return this.groupType;
  }

  private void setGroupType(int groupType) {
    this.groupType = groupType;
  }

  private String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

}

