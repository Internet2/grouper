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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.dto.GrouperDTO;
import edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import edu.internet2.middleware.grouper.internal.dto.StemDTO;

/** 
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseMemberOf.java,v 1.9 2008-06-24 06:07:03 mchyzer Exp $
 * @since   1.2.0
 */
public abstract class BaseMemberOf implements MemberOf {

  // PRIVATE INSTANCE VARIABLES //
  private Composite       c;
  private Set<GrouperDTO> deletes         = new LinkedHashSet<GrouperDTO>();
  private Set<GrouperDTO> effDeletes      = new LinkedHashSet<GrouperDTO>();
  private Set<GrouperDTO> effSaves        = new LinkedHashSet<GrouperDTO>();
  private Field           f               = Group.getDefaultList();
  private Group           g;
  private MemberDTO       _m;
  private MembershipDTO   _ms;
  private Set<GroupDTO>             modifiedGroups  = new LinkedHashSet();
  private Set<StemDTO>             modifiedStems   = new LinkedHashSet();
  private Stem            ns;
  private String          ownerUUID;  
  private Set<GrouperDTO> saves           = new LinkedHashSet<GrouperDTO>();


  // CONSTRUCTORS //

  // @since   1.2.0
  protected BaseMemberOf() {
    super();
  } // protected BaseMemberOf()


  // PUBLIC INSTANCE METHODS //
  
  /**
   * @since   1.2.0
   */
  public Set getDeletes() {
    return this.deletes;
  }
  
  /**
   * @since   1.2.0
   */
  public Set<GroupDTO> getModifiedGroups() {
    return this.modifiedGroups;
  }

  /**
   * @since   1.2.0
   */
  public Set<StemDTO> getModifiedStems() {
    return this.modifiedStems;
  }

  /**
   * @since   1.2.0
   */
  public Set getSaves() {
    return this.saves;
  }  
  
  
  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected Set<GrouperDTO> addDelete(GrouperDTO dto) {
    this.deletes.add(dto);
    return this.deletes;
  }
  // @since   1.2.0
  protected Set<GrouperDTO> addDeletes(Collection<GrouperDTO> c) {
    this.deletes.addAll(c);
    return this.deletes;
  }
  // @since   1.2.0
  protected Set<GrouperDTO> addEffectiveDeletes(Collection<GrouperDTO> c) {
    this.effDeletes.addAll(c);
    return this.effDeletes;
  }
  // @since   1.2.0
  protected Set<GrouperDTO> addEffectiveSaves(Collection<GrouperDTO> c) {
    this.effSaves.addAll(c);
    return this.effSaves;
  }
  // @since   1.2.0
  protected Set<GrouperDTO> addSave(GrouperDTO dto) {
    this.saves.add(dto);
    return this.saves;
  }
  // @since   1.2.0
  protected Set<GrouperDTO> addSaves(Collection<GrouperDTO> c) {
    this.saves.addAll(c);
    return this.saves;
  }
  // @since   1.2.0
  public Composite getComposite() {
    return this.c;
  }  
  // @since   1.2.0
  protected Set<GrouperDTO> getEffectiveDeletes() {
    return this.effDeletes;
  }
  // @since   1.2.0
  protected Set<GrouperDTO> getEffectiveSaves() {
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
  public MemberDTO getMemberDTO() {
    return this._m;
  }
  // @since   1.2.0
  public MembershipDTO getMembershipDTO() {
    return this._ms;
  }
  // @since   1.2.0
  protected String getOwnerUuid() {
    return this.ownerUUID;
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
    this.setOwnerUuid( g.getUuid() );
    return this;
  }
  // @since   1.2.0
  protected MemberOf setMemberDTO(MemberDTO _m) {
    this._m = _m;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setMembershipDTO(MembershipDTO _ms) {
    this._ms = _ms;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setModifiedGroups(Set<GroupDTO> modifiedGroups) {
    this.modifiedGroups = modifiedGroups;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setModifiedStems(Set<StemDTO> modifiedStems) {
    this.modifiedStems = modifiedStems;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setOwnerUuid(String ownerUUID) {
    this.ownerUUID = ownerUUID;
    return this;
  }
  // @since   1.2.0
  protected MemberOf setStem(Stem ns) {
    this.ns = ns;
    this.setOwnerUuid( ns.getUuid() );
    return this;
  }

}

