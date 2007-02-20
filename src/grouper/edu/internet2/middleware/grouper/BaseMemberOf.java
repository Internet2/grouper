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
import  java.util.LinkedHashSet;
import  java.util.Set;

/** 
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseMemberOf.java,v 1.2 2007-02-20 20:29:20 blair Exp $
 * @since   1.2.0
 */
abstract class BaseMemberOf {

  // PRIVATE INSTANCE VARIABLES //
  private Composite       c;
  private Field           f           = Group.getDefaultList();
  private Group           g;
  private GrouperSession  s;
  private MemberDTO       _m;
  private MembershipDTO   _ms;
  private Set             modifiedGroups  = new LinkedHashSet();
  private Set             modifiedStems   = new LinkedHashSet();
  private Stem            ns;
  private String          ownerUUID;  


  // CONSTRUCTORS //

  // @since   1.2.0
  protected BaseMemberOf() {
    super();
  } // protected BaseMemberOf()


  // PROTECTED ABSTRACT METHODS //

  // @since   1.2.0
  protected abstract void addImmediate(GrouperSession s, Group g, Field f, MemberDTO _m)
    throws  IllegalStateException;  // TODO 20070220 what would be more appropriate?

  // @since   1.2.0
  protected abstract void addImmediate(GrouperSession s, Stem ns, Field f, MemberDTO _m)
    throws  IllegalStateException;  // TODO 20070220 what would be more appropriate?


  // GETTERS //

  protected Composite getComposite() {
    return this.c;
  }  
  protected Field getField() {
    return this.f;
  }
  protected Group getGroup() {
    return this.g;
  }
  protected GrouperSession getSession() {
    return this.s;
  }
  protected MemberDTO getMemberDTO() {
    return this._m;
  }
  protected MembershipDTO getMembershipDTO() {
    return this._ms;
  }
  protected Set getModifiedGroups() {
    return this.modifiedGroups;
  }
  protected Set getModifiedStems() {
    return this.modifiedStems;
  }
  protected String getOwnerUuid() {
    return this.ownerUUID;
  }
  protected Stem getStem() {
    return this.ns;
  }


  // SETTERS //

  protected void setComposite(Composite c) {
    this.c = c;
  }
  protected void setField(Field f) {
    this.f = f;
  }
  protected void setGroup(Group g) {
    this.g = g;
    this.setOwnerUuid( g.getUuid() );
  }
  protected void setSession(GrouperSession s) {
    this.s = s;
  }
  protected void setMemberDTO(MemberDTO _m) {
    this._m = _m;
  }
  protected void setMembershipDTO(MembershipDTO _ms) {
    this._ms = _ms;
  }
  protected void setModifiedGroups(Set modifiedGroups) {
    this.modifiedGroups = modifiedGroups;
  }
  protected void setModifiedStems(Set modifiedStems) {
    this.modifiedStems = modifiedStems;
  }
  protected void setOwnerUuid(String ownerUUID) {
    this.ownerUUID = ownerUUID;
  }
  protected void setStem(Stem ns) {
    this.ns = ns;
    this.setOwnerUuid( ns.getUuid() );
  }

} // abstract class BaseMemberOf

