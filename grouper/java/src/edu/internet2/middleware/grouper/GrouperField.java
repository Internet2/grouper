package edu.internet2.middleware.directory.grouper;

/** 
 * Class representing a field within a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperField.java,v 1.4 2004-08-03 01:00:58 blair Exp $
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

  protected String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

  protected String getReadPriv() {
    return this.readPriv;
  }

  private void setReadPriv(String readPriv) {
    this.readPriv = readPriv;
  }

  protected String getWritePriv() {
    return this.writePriv;
  }

  private void setWritePriv(String writePriv) {
    this.writePriv = writePriv;
  }

  protected String getIsList() {
    return this.isList;
  }

  private void setIsList(String isList) {
    this.isList = isList;
  }

}

