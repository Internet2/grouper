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
import  edu.internet2.middleware.subject.*;
import  org.apache.commons.lang.builder.*;

/** 
 * A {@link PrivilegeCache} element.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrivilegeCacheElement.java,v 1.3 2006-08-22 19:48:22 blair Exp $
 * @since   1.1.0
 */
public class PrivilegeCacheElement {

  // PRIVATE INSTANCE VARIABLES //
  private boolean hasPriv       = false;
  private boolean isCached      = false;
  private String  ownerUuid     = GrouperConfig.EMPTY_STRING;
  private String  priv          = GrouperConfig.EMPTY_STRING;
  private String  subjectId     = GrouperConfig.EMPTY_STRING;
  private String  subjectSrc    = GrouperConfig.EMPTY_STRING;
  private String  subjectType   = GrouperConfig.EMPTY_STRING;


  // CONSTRUCTORS //

  /**
   * @since   1.1.0
   */
  protected PrivilegeCacheElement() {
    super();
  } // protected PrivilegeCacheElement()

  /**
   * @since   1.1.0
   */
  public PrivilegeCacheElement(Owner o, Subject subj, Privilege p) { 
    this.isCached     = true; // TODO ???
    this.ownerUuid    = o.getUuid();
    this.priv         = p.getName();
    this.subjectId    = subj.getId();
    this.subjectSrc   = subj.getSource().getId();
    this.subjectType  = subj.getType().getName();
  } // public PrivilegeCacheElement(o, subj, p)

  /**
   * @since   1.1.0
   */
  public PrivilegeCacheElement(
    Owner o, Subject subj, Privilege p, boolean hasPriv
  )
  { 
    this(o, subj, p);
    this.hasPriv      = hasPriv;
    this.isCached     = true;
  } // public PrivilegeCacheElement(o, subj, p, hasPriv)



  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.1.0
   */
  public boolean getHasPriv() {
    return this.hasPriv;
  } // public boolean getHasPriv()

  /**
   * @since   1.1.0
   */
  public boolean getIsCached() {
    return this.isCached;
  } // public boolean getIsCached()

  /**
   * @since   1.1.0
   */
  public String getOwnerUuid() {
    return this.ownerUuid;
  } // public String getOwnerUuid()
  
  /**
   * @since   1.1.0
   */
   public String getPrivilege() {
     return this.priv;
   } // public String getPrivilege()
   
  /**
   * @since   1.1.0
   */
  public String getSubjectId() {
    return this.subjectId;
  } // public String getSubjectId()

  /**
   * @since   1.1.0
   */
  public String getSubjectSource() {
    return this.subjectSrc;
  } // public String getSubjectSource()

  /**
   * @since   1.1.0
   */
   public String getSubjectType() {
      return this.subjectType;
  } // public String getSubjectType()

  /**
   * @since   1.1.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append("hasPriv"       , this.getHasPriv()       )
      .append("isCached"      , this.getIsCached()      )
      .append("ownerUuid"     , this.getOwnerUuid()     )
      .append("subjectId"     , this.getSubjectId()     )
      .append("subjectSource" , this.getSubjectSource() )
      .append("subjectType"   , this.getSubjectType()   )
      .append("privilege"     , this.getPrivilege()     )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   1.1.0
  protected void setIsCached(boolean cached) {
    this.isCached = cached;
  } // protected void setIsCached(cached0

} // public class PrivilegeCacheElement

