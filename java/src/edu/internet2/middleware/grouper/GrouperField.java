package edu.internet2.middleware.directory.grouper;

/** 
 * Class representing a field within a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperField.java,v 1.3 2004-07-26 17:03:49 blair Exp $
 */
public class GrouperField {

  private String groupField;
  // XXX GrouperPrivileges? object?
  private String readPriv;
  // XXX GrouperPrivileges? object?
  private String writePriv;
  // XXX Proper type?
  private String isList;

  /**
   * Create a {@link GrouperField} object.
   */
  public GrouperField() {
    // Nothing -- Yet
  }

  /*
   * Below for Hibernate
   */

  private String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

  private String getReadPriv() {
    return this.readPriv;
  }

  private void setReadPriv(String readPriv) {
    this.readPriv = readPriv;
  }

  private String getWritePriv() {
    return this.writePriv;
  }

  private void setWritePriv(String writePriv) {
    this.writePriv = writePriv;
  }

  private String getIsList() {
    return this.isList;
  }

  private void setIsList(String isList) {
    this.isList = isList;
  }

}

