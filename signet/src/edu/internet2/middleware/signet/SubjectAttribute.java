/*--
$Id: SubjectAttribute.java,v 1.1 2005-04-19 10:11:29 acohen Exp $
$Date: 2005-04-19 10:11:29 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;

import edu.internet2.middleware.subject.SubjectType;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SubjectAttribute
{
  private Integer     id;
  private String      subjectId;
  private Date        modifyDatetime;
  private SubjectType subjectType;
  private String      name;
  private int         instance;
  private String      value;
  private String      searchValue;
  /**
   * @return Returns the id.
   */
  public String getSubjectId() {
    return this.subjectId;
  }
  /**
   * @param id The id to set.
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }
  /**
   * @return Returns the instance.
   */
  public int getInstance() {
    return this.instance;
  }
  /**
   * @param instance The instance to set.
   */
  public void setInstance(int instance) {
    this.instance = instance;
  }
  /**
   * @return Returns the modifyDatetime.
   */
  public Date getModifyDatetime() {
    return this.modifyDatetime;
  }
  /**
   * @param modifyDatetime The modifyDatetime to set.
   */
  public void setModifyDatetime(Date modifyDatetime) {
    this.modifyDatetime = modifyDatetime;
  }
  /**
   * @return Returns the name.
   */
  public String getName() {
    return this.name;
  }
  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }
  /**
   * @return Returns the searchValue.
   */
  public String getSearchValue() {
    return this.searchValue;
  }
  /**
   * @param searchValue The searchValue to set.
   */
  public void setSearchValue(String searchValue) {
    this.searchValue = searchValue;
  }
  /**
   * @return Returns the subjectType.
   */
  public SubjectType getSubjectType() {
    return this.subjectType;
  }
  /**
   * @param subjectType The subjectType to set.
   */
  public void setSubjectType(SubjectType subjectType) {
    this.subjectType = subjectType;
  }
  /**
   * @return Returns the value.
   */
  public String getValue() {
    return this.value;
  }
  /**
   * @param value The value to set.
   */
  public void setValue(String value) {
    this.value = value;
  }
  /**
   * @return Returns the id.
   */
  public Integer getId() {
    return this.id;
  }
  /**
   * @param id The id to set.
   */
  public void setId(Integer id) {
    this.id = id;
  }
}
