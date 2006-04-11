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
 * An object that can have memberships assigned to it.
 * <p />
 * @author  blair christensen.
 * @version $Id: Owner.java,v 1.4.2.1 2006-04-11 18:50:30 blair Exp $
 *     
*/
public class Owner implements Serializable {

  // Private Class Constants
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(Owner.class);


  // Hibernate Properties
  private String  id;
  private String  create_source;
  private long    create_time;
  private Member  creator_id;
  private Member  modifier_id;
  private String  modify_source;
  private long    modify_time;
  private Status  status;
  private String  uuid;

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


  // Getters //
  protected String getCreate_source() {
    return this.create_source;
  }
  protected long getCreate_time() {
    return this.create_time;
  }
  protected Member getCreator_id() {
    return this.creator_id;
  }
  protected String getId() {
    return this.id;
  }
  protected Member getModifier_id() {
    return this.modifier_id;
  }
  protected String getModify_source() {
    return this.modify_source;
  }
  protected long getModify_time() {
    return this.modify_time;
  }
  protected Status getStatus() {
    return this.status;
  }
  /**
   * Return UUID.
   * <pre class="eg">
   * String uuid = o.getUuid();
   * </pre>
   * @return  UUID of object.
   */
  public String getUuid() {
    return this.uuid;
  } // public String getUuid()


  // Setters //
  protected void setId(String id) {
    this.id = id;
  }
  protected void setCreate_source(String create_source) {
    this.create_source = create_source;
  }
  protected void setCreate_time(long create_time) {
    this.create_time = create_time;
  }
  protected void setCreator_id(Member creator_id) {
    this.creator_id = creator_id;
  }
  protected void setModifier_id(Member modifier_id) {
      this.modifier_id = modifier_id;
  }
  protected void setModify_source(String modify_source) {
    this.modify_source = modify_source;
  }
  protected void setModify_time(long modify_time) {
    this.modify_time = modify_time;
  }
  protected void setStatus(Status s) {
    this.status = s;
  }
  protected void setUuid(String uuid) {
    this.uuid = uuid;
  }

}

