/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

/** 
 * Class representing a field within a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperField.java,v 1.11 2004-11-23 19:43:26 blair Exp $
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
    groupField  = null;
    readPriv    = null;
    writePriv   = null;
    isList      = null;
  }

  public String toString() {
    return  this.getGroupField()  + ":" + 
            this.getReadPriv()    + ":" +
            this.getWritePriv()   + ":" +
            this.getIsList();
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

