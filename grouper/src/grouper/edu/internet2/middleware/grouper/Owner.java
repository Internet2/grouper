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
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.lang.builder.*;

/** 
 * An object that can have memberships assigned to it.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Owner.java,v 1.14 2006-07-10 18:17:07 blair Exp $
 * @since   1.0
 */
public abstract class Owner {

  // HIBERNATE PROPERTIES //
  private String  id;
  private String  create_source;
  private long    create_time;
  private Member  creator_id;
  private Member  modifier_id;
  private String  modify_source;
  private long    modify_time;
  private String  uuid;

  // PROTECTED TRANSIENT INSTANCE VARIABLES //
  protected transient GrouperSession  s;
  

  // CONSTRUCTORS //

  // Default constructor for Hibernate.
  // @since 1.0
  protected Owner() {
    super();
  } // protected Owner() 


  // PROTECTED ABSTRACT INSTANCE METHODS //

  // @since 1.0
  protected abstract void setModified();


  // PROTECTED INSTANCE METHODS //

  // @since 1.0
  protected GrouperSession getSession() {
    GrouperSession.validate(this.s);
    return this.s;
  } // protected GrouperSession getSession()

  // @since 1.0
  // FIXME Deprecate?
  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  } // protected void setSession(s)

  // @since 1.0
  // TODO Replace `setSession(s)`
  protected void setSessionNew(GrouperSession s) 
    throws  ModelException
  {
    GrouperSessionValidator.validate(s);
    this.s = s;
  } // protected void setSession(s)


  // GETTERS //
  // @since 1.0
  protected String getCreate_source() {
    return this.create_source;
  }
  // @since 1.0
  protected long getCreate_time() {
    return this.create_time;
  }
  // @since 1.0
  protected Member getCreator_id() {
    return this.creator_id;
  }
  // @since 1.0
  protected String getId() {
    return this.id;
  }
  // @since 1.0
  protected Member getModifier_id() {
    return this.modifier_id;
  }
  // @since 1.0
  protected String getModify_source() {
    return this.modify_source;
  }
  // @since 1.0
  protected long getModify_time() {
    return this.modify_time;
  }
  /**
   * Return UUID.
   * <pre class="eg">
   * String uuid = o.getUuid();
   * </pre>
   * @return  UUID of object.
   * @since   1.0
   */
  public String getUuid() {
    return this.uuid;
  } // public String getUuid()


  // SETTERS //
  // @since 1.0
  protected void setId(String id) {
    this.id = id;
  }
  // @since 1.0
  protected void setCreate_source(String create_source) {
    this.create_source = create_source;
  }
  // @since 1.0
  protected void setCreate_time(long create_time) {
    this.create_time = create_time;
  }
  // @since 1.0
  protected void setCreator_id(Member creator_id) {
    this.creator_id = creator_id;
  }
  // @since 1.0
  protected void setModifier_id(Member modifier_id) {
      this.modifier_id = modifier_id;
  }
  // @since 1.0
  protected void setModify_source(String modify_source) {
    this.modify_source = modify_source;
  }
  // @since 1.0
  protected void setModify_time(long modify_time) {
    this.modify_time = modify_time;
  }
  // @since 1.0
  protected void setUuid(String uuid) {
    this.uuid = uuid;
  }

}

