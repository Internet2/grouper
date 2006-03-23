/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * An object that can have associated memberships within the Groups
 * Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Owner.java,v 1.3 2006-03-23 18:36:31 blair Exp $
 *     
*/
public class Owner implements Serializable {

  // Private Class Constants
  private static final EventLog EL          = new EventLog();
  private static final Log      LOG         = LogFactory.getLog(Owner.class);


  // Hibernate Properties
  private String  id;
  private String  create_source;
  private long    create_time;
  private Member  creator_id;
  private Member  modifier_id;
  private String  modify_source;
  private long    modify_time;
  private String  owner_uuid;
  private Status  status;

  // Transient Instance Variables
  protected transient GrouperSession  s;
  

  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public Owner() {
    // Nothing
  }

  // Protected Instance Methods
  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  } // protected void setSession(s)


  // Hibernate Accessors
  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  private String getCreate_source() {
    return this.create_source;
  }

  private void setCreate_source(String create_source) {
    this.create_source = create_source;
  }

  private long getCreate_time() {
    return this.create_time;
  }

  private void setCreate_time(long create_time) {
    this.create_time = create_time;
  }

  private String getModify_source() {
    return this.modify_source;
  }

  private void setModify_source(String modify_source) {
    this.modify_source = modify_source;
  }

  private long getModify_time() {
    return this.modify_time;
  }

  private void setModify_time(long modify_time) {
    this.modify_time = modify_time;
  }

  protected String getOwner_uuid() {
    return this.owner_uuid;
  }
  
  protected void setOwner_uuid(String owner_uuid) {
    this.owner_uuid = owner_uuid;
  }

  private Member getCreator_id() {
    return this.creator_id;
  }

  private void setCreator_id(Member creator_id) {
    this.creator_id = creator_id;
  }

  private Member getModifier_id() {
    return this.modifier_id;
  }

  private void setModifier_id(Member modifier_id) {
      this.modifier_id = modifier_id;
  }

  private Status getStatus() {
    return this.status;
  }
  private void setStatus(Status s) {
    this.status = s;
  }

}

