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
package edu.internet2.middleware.grouper.app.loader;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;


/**
 * maps to test testgrouper_loader table
 */
@SuppressWarnings("serial")
public class TestgrouperLoader extends GrouperAPI implements Hib3GrouperVersioned {
  
  /** id col */
  private String id;
  
  /** col */
  private String col1;
  
  /** col */
  private String col2;
  
  /** col */
  private String col3;

  /**
   * 
   */
  public TestgrouperLoader() {
    super();
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
   * @return col1
   */
  public String getCol1() {
    return this.col1;
  }

  /**
   * 
   * @param _col1
   */
  public void setCol1(String _col1) {
    this.col1 = _col1;
  }

  /**
   * 
   * @return col
   */
  public String getCol2() {
    return this.col2;
  }

  /**
   * @param _col2
   */
  public void setCol2(String _col2) {
    this.col2 = _col2;
  }

  /**
   * @return col
   */
  public String getCol3() {
    return this.col3;
  }

  /**
   * @param _col3
   */
  public void setCol3(String _col3) {
    this.col3 = _col3;
  }



  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return new TestgrouperLoader(this.col1, this.col2, this.col3);
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
   * @param _col1
   * @param _col2
   * @param _col3
   */
  public TestgrouperLoader(String _col1, String _col2, String _col3) {
    this.col1 = _col1;
    this.col2 = _col2;
    this.col3 = _col3;
  }
  
  
  
}
