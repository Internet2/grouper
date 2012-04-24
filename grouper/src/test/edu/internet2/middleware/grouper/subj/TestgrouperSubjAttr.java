/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: TestgrouperLoader.java,v 1.1 2008-11-08 08:15:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;


/**
 * maps to test testgrouper_subj_attr table
 */
@SuppressWarnings("serial")
public class TestgrouperSubjAttr extends GrouperAPI implements Hib3GrouperVersioned {
  
  /** name of the subject attribute table in the db */
  public static final String TABLE_TESTGROUPER_SUBJ_ATTR = "testgrouper_subj_attr";

  /** subjectId col */
  private String subjectId;
  
  /** id col */
  private String id;
  
  /** student major */
  private String major;
  
  /** employee title */
  private String title;
  
  /**
   * 
   */
  public TestgrouperSubjAttr() {
    super();
  }  
  
  /**
   * @return the subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }
  
  /**
   * @param _subjectId the subjectId to set
   */
  public void setSubjectId(String _subjectId) {
    this.subjectId = _subjectId;
  }



  /**
   * 
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * 
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * 
   * @return title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * 
   * @param _title
   */
  public void setTitle(String _title) {
    this.title = _title;
  }

  /**
   * 
   * @return col
   */
  public String getMajor() {
    return this.major;
  }

  /**
   * @param _major
   */
  public void setMajor(String _major) {
    this.major = _major;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return new TestgrouperSubjAttr(this.subjectId, this.major, this.title);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    if (StringUtils.isBlank(this.id)) {
      this.id = GrouperUuid.getUuid();
    }
  }

  /**
   * @param _subjectId
   * @param _major
   * @param _title
   */
  public TestgrouperSubjAttr(String _subjectId, String _major, String _title) {
    this.subjectId = _subjectId;
    this.major = _major;
    this.title = _title;
  }
  
}
