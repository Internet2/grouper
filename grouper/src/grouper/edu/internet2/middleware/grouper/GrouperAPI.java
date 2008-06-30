/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.io.Serializable;
import java.util.Set;

import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/** 
 * Base Grouper API class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperAPI.java,v 1.13 2008-06-30 04:01:33 mchyzer Exp $
 * @since   1.2.0
 */
public abstract class GrouperAPI implements HibGrouperLifecycle, Lifecycle {

  /** save the state when retrieving from DB */
  @GrouperIgnoreDbVersion
  protected Object dbVersion = null;

  /** field name for db version */
  public static final String FIELD_DB_VERSION = "dbVersion";
  
  /**
   * call this method to get the field value (e.g. from dbVersionDifferentFields).
   * some objects have different interpretations (e.g. Group will process attribute__whatever)
   * @param fieldName
   * @return the value
   */
  public Object fieldValue(String fieldName) {
    return GrouperUtil.fieldValue(this, fieldName);
  }
  
  /**
   * version of this object in the database
   * @return the db version
   */
  public Object dbVersion() {
    return this.dbVersion;
  }

  /**
   * see if the state of this object has changed compared to the DB state (last known)
   * @return true if changed, false if not
   */
  public boolean dbVersionIsDifferent() {
    Set<String> differentFields = dbVersionDifferentFields();
    return differentFields.size() > 0;
  }



  /**
   * see which fields have changed compared to the DB state (last known)
   * note that attributes will print out: attribute__attributeName
   * @return a set of attributes changed, or empty set if none
   */
  public Set<String> dbVersionDifferentFields() {
    throw new RuntimeException("Not implemented");
  }


  /**
   * take a snapshot of the data since this is what is in the db
   */
  public void dbVersionReset() {
  }

  /**
   * set to null (e.g. on delete)
   */
  public void dbVersionClear() {
    this.dbVersion = null;
  }


  /**
   * @see org.hibernate.classic.Lifecycle#onDelete(org.hibernate.Session)
   */
  public boolean onDelete(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }


  /**
   * @see org.hibernate.classic.Lifecycle#onLoad(org.hibernate.Session, java.io.Serializable)
   */
  public void onLoad(Session s, Serializable id) {
    this.dbVersionReset();
  }


  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostDelete(HibernateSession hibernateSession) {
  }


  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostSave(HibernateSession hibernateSession) {
  }


  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
  }


  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreDelete(HibernateSession hibernateSession) {
  }


  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreSave(HibernateSession hibernateSession) {
    
  }


  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreUpdate(HibernateSession hibernateSession) {
  }


  /**
   * @see org.hibernate.classic.Lifecycle#onSave(org.hibernate.Session)
   */
  public boolean onSave(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }


  /**
   * @see org.hibernate.classic.Lifecycle#onUpdate(org.hibernate.Session)
   */
  public boolean onUpdate(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  } 

} 

