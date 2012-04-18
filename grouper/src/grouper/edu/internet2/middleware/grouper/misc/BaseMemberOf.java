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

package edu.internet2.middleware.grouper.misc;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;

/** 
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseMemberOf.java,v 1.7 2009-06-09 22:55:40 shilen Exp $
 * @since   1.2.0
 */
public abstract class BaseMemberOf implements MemberOf {

  /** */
  private Composite       c;
  /** */
  private Set<GrouperAPI> deletes         = new LinkedHashSet<GrouperAPI>();
  /** */
  private Set<GrouperAPI> updates         = new LinkedHashSet<GrouperAPI>();
  /** */
  private Set<GrouperAPI> effDeletes      = new LinkedHashSet<GrouperAPI>();
  /** */
  private Set<GrouperAPI> effSaves        = new LinkedHashSet<GrouperAPI>();
  /** */
  private Field           f               = Group.getDefaultList();
  /** */
  private Group           g;
  /** */
  private Member       _m;
  /** */
  private Membership   _ms;
  /** */
  private Set<Group>             modifiedGroups  = new LinkedHashSet();
  /** */
  private Set<Stem>             modifiedStems   = new LinkedHashSet();
  /** */
  private Stem            ns;
  /** */
  private String          ownerGroupId;  
  /** */
  private String          ownerStemId;  
  /** */
  private Set<GrouperAPI> saves           = new LinkedHashSet<GrouperAPI>();
  /** */
  private Set<String> groupIdsWithNewMemberships = new LinkedHashSet<String>();
  /** */
  private Set<String> stemIdsWithNewMemberships = new LinkedHashSet<String>();


  // CONSTRUCTORS //

  // @since   1.2.0
  protected BaseMemberOf() {
    super();
  } // protected BaseMemberOf()


  // PUBLIC INSTANCE METHODS //
  
  /**
   * @since   1.2.0
   */
  public Set<GrouperAPI> getDeletes() {
    return this.deletes;
  }
  
  /**
   * @since   1.2.0
   */
  public Set<Group> getModifiedGroups() {
    return this.modifiedGroups;
  }

  /**
   * @since   1.2.0
   */
  public Set<Stem> getModifiedStems() {
    return this.modifiedStems;
  }

  /**
   * @since   1.2.0
   */
  public Set<GrouperAPI> getSaves() {
    return this.saves;
  }  
  
  /**
   * @return updates to objects
   */
  public Set<GrouperAPI> getUpdates() {
    return this.updates;
  }  
  
  
  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  public Set<GrouperAPI> addDelete(GrouperAPI dto) {
    this.deletes.add(dto);
    return this.deletes;
  }
  // @since   1.2.0
  protected Set<GrouperAPI> addDeletes(Collection<GrouperAPI> c) {
    this.deletes.addAll(c);
    return this.deletes;
  }
  // @since   1.2.0
  protected Set<GrouperAPI> addEffectiveDeletes(Collection<? extends GrouperAPI> c) {
    this.effDeletes.addAll(c);
    return this.effDeletes;
  }
  // @since   1.2.0
  protected Set<? extends GrouperAPI> addEffectiveSaves(Collection<? extends GrouperAPI> c) {
    this.effSaves.addAll(c);
    return this.effSaves;
  }
  // @since   1.2.0
  public Set<GrouperAPI> addSave(GrouperAPI dto) {
    this.saves.add(dto);
    return this.saves;
  }
  // @since   1.2.0
  protected Set<GrouperAPI> addSaves(Collection<GrouperAPI> c) {
    this.saves.addAll(c);
    return this.saves;
  }
  // @since   1.2.0
  public Composite getComposite() {
    return this.c;
  }  
  // @since   1.2.0
  public Set<GrouperAPI> getEffectiveDeletes() {
    return this.effDeletes;
  }
  // @since   1.2.0
  public Set<GrouperAPI> getEffectiveSaves() {
    return this.effSaves;
  }
  // @since   1.2.0
  public Field getField() {
    return this.f;
  }
  // @since   1.2.0
  public Group getGroup() {
    return this.g;
  }
  // @since   1.2.0
  public Member getMember() {
    return this._m;
  }
  // @since   1.2.0
  public Membership getMembership() {
    return this._ms;
  }

  /**
   * @return owner group id
   */
  protected String getOwnerGroupId() {
    return this.ownerGroupId;
  }
  /**
   * @return owner group id
   */
  protected String getOwnerStemId() {
    return this.ownerStemId;
  }
  // @since   1.2.0
  public Stem getStem() {
    return this.ns;
  }
  // @since   1.2.0
  protected MemberOf setComposite(Composite c) {
    this.c = c;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setField(Field f) {
    this.f = f;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setGroup(Group g) {
    this.g = g;
    this.setOwnerGroupId( g.getUuid() );
    return this;
  }
  // @since   1.2.0
  protected MemberOf setMember(Member _m) {
    this._m = _m;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setMembership(Membership _ms) {
    this._ms = _ms;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setModifiedGroups(Set<Group> modifiedGroups) {
    this.modifiedGroups = modifiedGroups;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setModifiedStems(Set<Stem> modifiedStems) {
    this.modifiedStems = modifiedStems;
    return this;
  }
  /**
   * @param ownerGroupId1
   * @return self for chaining
   */
  protected MemberOf setOwnerGroupId(String ownerGroupId1) {
    this.ownerGroupId = ownerGroupId1;
    return this;
  }
  /**
   * @param ownerStemId1
   * @return self for chaining
   */
  protected MemberOf setOwnerStemId(String ownerStemId1) {
    this.ownerStemId = ownerStemId1;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setStem(Stem ns) {
    this.ns = ns;
    this.setOwnerStemId( ns.getUuid() );
    return this;
  }
  
  /**
   * @param dto
   * @return self for chaining
   */
  public Set<GrouperAPI> addUpdate(GrouperAPI dto) {
    this.updates.add(dto);
    return this.updates;
  }
  
  protected MemberOf setGroupIdsWithNewMemberships(Set<String> ids) {
    this.groupIdsWithNewMemberships = ids;
    return this;
  }
  
  protected MemberOf setStemIdsWithNewMemberships(Set<String> ids) {
    this.stemIdsWithNewMemberships = ids;
    return this;
  }
  
  public Set<String> getGroupIdsWithNewMemberships() {
    return this.groupIdsWithNewMemberships;
  }
  
  public Set<String> getStemIdsWithNewMemberships() {
    return this.stemIdsWithNewMemberships;
  }
}

