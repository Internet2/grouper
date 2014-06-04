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
 * $Id: TestgrouperLoaderGroups.java,v 1.1 2008-12-09 08:11:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;


/**
 * maps to test testgrouper_loader_groups table
 */
@SuppressWarnings("serial")
public class TestgrouperLoaderGroups extends GrouperAPI implements Serializable, Hib3GrouperVersioned {
  
  /** id col */
  private String id;
  
  /** group name */
  private String groupName;
  
  /** group display name */
  private String groupDisplayName;
  
  /** col */
  private String groupDescription;
  
  /** 
   */
  public TestgrouperLoaderGroups() {
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
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * 
   * @param theGroupName
   */
  public void setGroupName(String theGroupName) {
    this.groupName = theGroupName;
  }

  /**
   * 
   * @return col
   */
  public String getGroupDisplayName() {
    return this.groupDisplayName;
  }

  /**
   * @param theGroupDisplayName
   */
  public void setGroupDisplayName(String theGroupDisplayName) {
    this.groupDisplayName = theGroupDisplayName;
  }

  /**
   * @return col
   */
  public String getGroupDescription() {
    return this.groupDescription;
  }

  /**
   * @param theGroupDescription
   */
  public void setGroupDescription(String theGroupDescription) {
    this.groupDescription = theGroupDescription;
  }



  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return new TestgrouperLoaderGroups(this.groupName, this.groupDisplayName, this.groupDescription);
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
   * @param theGroupName
   * @param theGroupDisplayName
   * @param theGroupDescription
   */
  public TestgrouperLoaderGroups(String theGroupName, String theGroupDisplayName, String theGroupDescription) {
    this.groupName = theGroupName;
    this.groupDisplayName = theGroupDisplayName;
    this.groupDescription = theGroupDescription;
  }
  
  
  
}
