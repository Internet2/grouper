/* 
 * Copyright (C) 2004 TODO
 * All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the Academic
 * Free License version 2.1.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.opensource.org/licenses/afl-2.1.php>
 */

package edu.internet2.middleware.grouper;

/** 
 * Class representing a field within a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperField.java,v 1.7 2004-08-24 17:37:57 blair Exp $
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

