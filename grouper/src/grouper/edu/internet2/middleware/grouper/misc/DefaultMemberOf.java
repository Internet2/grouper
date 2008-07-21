/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.CompositeMembershipValidator;
import edu.internet2.middleware.grouper.validator.EffectiveMembershipValidator;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.ImmediateMembershipValidator;

/** 
 * Perform <i>member of</i> calculation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: DefaultMemberOf.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.0
 */
@GrouperIgnoreDbVersion
public class DefaultMemberOf extends BaseMemberOf implements GrouperCloneable {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: _m */
  public static final String FIELD__M = "_m";

  /** constant for field name for: _ms */
  public static final String FIELD__MS = "_ms";

  /** constant for field name for: c */
  public static final String FIELD_C = "c";

  /** constant for field name for: deletes */
  public static final String FIELD_DELETES = "deletes";

  /** constant for field name for: effDeletes */
  public static final String FIELD_EFF_DELETES = "effDeletes";

  /** constant for field name for: effSaves */
  public static final String FIELD_EFF_SAVES = "effSaves";

  /** constant for field name for: f */
  public static final String FIELD_F = "f";

  /** constant for field name for: g */
  public static final String FIELD_G = "g";

  /** constant for field name for: modifiedGroups */
  public static final String FIELD_MODIFIED_GROUPS = "modifiedGroups";

  /** constant for field name for: modifiedStems */
  public static final String FIELD_MODIFIED_STEMS = "modifiedStems";

  /** constant for field name for: ns */
  public static final String FIELD_NS = "ns";

  /** constant for field name for: ownerUUID */
  public static final String FIELD_OWNER_UUID = "ownerUUID";

  /** constant for field name for: saves */
  public static final String FIELD_SAVES = "saves";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD__M, FIELD__MS, FIELD_C, FIELD_DELETES, 
      FIELD_EFF_DELETES, FIELD_EFF_SAVES, FIELD_F, FIELD_G, 
      FIELD_MODIFIED_GROUPS, FIELD_MODIFIED_STEMS, FIELD_NS, FIELD_OWNER_UUID, 
      FIELD_SAVES);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public Object clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  // PUBLIC INSTANCE METHODS //
 
  /**
   * @since   1.2.0
   */
  public void addComposite(GrouperSession s, final Group g, final Composite c)
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        DefaultMemberOf.this.setComposite(c);
        DefaultMemberOf.this.setGroup(g);
        DefaultMemberOf.this._evaluateAddCompositeMembership();
        DefaultMemberOf.this.addSave( c );
        return null;
      }
      
    });
  } 

  /**
   * @since   1.2.0
   */
  public void deleteComposite(GrouperSession s, final Group g, final Composite c)
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        DefaultMemberOf.this.setComposite(c);
        DefaultMemberOf.this.setGroup(g);
        DefaultMemberOf.this._evaluateDeleteCompositeMembership();
        DefaultMemberOf.this.addDelete( c );
        return null;
      }
      
    });
  }

  /**
   * @since   1.2.0
   */
  public void addImmediate(GrouperSession s, Group g, Field f, Member _m)
    throws  IllegalStateException 
  {
    //note, no need for GrouperSession inverse of control
    this.setGroup(g);
    this._evaluateAddImmediateMembership(s, f, _m);
  } 

  /**
   * @since   1.2.0
   */
  public void addImmediate(GrouperSession s, Stem ns, Field f, Member _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setStem(ns);
    this._evaluateAddImmediateMembership(s, f, _m);
  }

  /**
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Group g, Membership _ms, Member _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setGroup(g);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  }

  /**
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Stem ns, Membership _ms, Member _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setStem(ns);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  } 


  // PROTECTED INSTANCE METHODS //

  // m->{ "group" | "stem" } = { group or stem uuid = group or stem object }
  // @since   1.2.0 
  public Map identifyGroupsAndStemsToMarkAsModified(Map m, Iterator it) 
    throws  IllegalStateException
  {
    // This method is still a lot bigger and more hackish than I'd like but...

    // So that everything has the same modify time within here
    String        modifierUuid  = GrouperSession.staticGrouperSession().getMember().getUuid();
    long          modifyTime    = new java.util.Date().getTime();

    // TODO 20070531 this is horribly ugly.
    Map<String, Group> groups = new HashMap<String, Group>();
    if (m.containsKey("groups")) {
      groups = (Map) m.get("groups");
    }
    Map<String, Stem> stems = new HashMap<String, Stem>();
    if (m.containsKey("stems")) {
      stems = (Map) m.get("stems");
    }
    
    Membership _ms;
    String        k;
    Group      _g;
    Stem       _ns;
    try {
      GroupDAO  gDAO  = GrouperDAOFactory.getFactory().getGroup();
      StemDAO   nsDAO = GrouperDAOFactory.getFactory().getStem();
      while (it.hasNext()) {
        _ms = (Membership) it.next();
        k   = _ms.getOwnerUuid();
        if      ( _ms.getListType().equals(FieldType.LIST.toString()) || _ms.getListType().equals(FieldType.ACCESS.toString()) ) {
          if ( !groups.containsKey(k) ) {
            _g = gDAO.findByUuid(k);
            _g.setModifierUuid(modifierUuid);
            _g.setModifyTimeLong(modifyTime);
            groups.put(k, _g);
          }
        }
        else if ( _ms.getListType().equals(FieldType.NAMING.toString()) ) {
          if ( !stems.containsKey(k) ) {
            _ns = nsDAO.findByUuid(k);
            _ns.setModifierUuid(modifierUuid);
            _ns.setModifyTimeLong(modifyTime);
            stems.put(k, _ns);
          }
        }
        else {
          throw new IllegalStateException( "unknown membership type: " + _ms.getListType() );
        }
      }
    }
    catch (GroupNotFoundException eGNF) {
      throw new IllegalStateException( "attempt to modify a group that cannot be found: " + eGNF.getMessage() );
    }
    catch (StemNotFoundException eNSNF) {
      throw new IllegalStateException( "attempt to modify a stem that cannot be found: " + eNSNF.getMessage() );
    }
    // add the owner
    if      ( this.getGroup() != null ) {
      _g = this.getGroup();
      _g.setModifierUuid(modifierUuid);
      _g.setModifyTimeLong(modifyTime);
      groups.put( _g.getUuid(), _g );
    }
    else if ( this.getStem() != null )  {
      _ns = this.getStem();
      _ns.setModifierUuid(modifierUuid);
      _ns.setModifyTimeLong(modifyTime);
      stems.put( _ns.getUuid(), _ns );
    }
    else {
      throw new IllegalStateException("MemberOf has no group or stem as owner");
    }
    // assemble the map
    m.put("groups", groups);
    m.put("stems", stems);
    return m;
  }


  // PRIVATE INSTANCE METHODS //

  // Add m's hasMembers to o
  // @since   1.2.0
  private Set _addHasMembersToOwner(Set<Membership> hasMembers) 
    throws  IllegalStateException
  {
    Set           mships      = new LinkedHashSet();
    Membership hasMS;
    Membership _ms;
    Iterator<Membership>      it          = hasMembers.iterator();
    // cache values outside of iterator
    int           depth       = this.getMembership().getDepth();
    String        listName    = this.getMembership().getListName();
    String        listType    = this.getMembership().getListType();
    String        memberUUID  = GrouperSession.staticGrouperSession().getMember().getUuid();
    String        msUUID      = this.getMembership().getUuid();
    String        ownerUUID   = this.getMembership().getOwnerUuid();
    while (it.hasNext()) {
      hasMS = it.next();

      _ms = new Membership();
      _ms.setCreatorUuid(memberUUID);
      _ms.setDepth(depth + hasMS.getDepth() + 1);
      _ms.setListName(listName);
      _ms.setListType(listType);
      _ms.setMemberUuid( hasMS.getMemberUuid() );
      _ms.setOwnerUuid(ownerUUID);
      _ms.setType(Membership.EFFECTIVE);
      if ( hasMS.getDepth() == 0 ) {
        _ms.setViaUuid( hasMS.getOwnerUuid() );  // hasMember m was immediate
        _ms.setParentUuid(msUUID);
      }
      else {
        _ms.setViaUuid( hasMS.getViaUuid() ); // hasMember m was effective
        if ( hasMS.getParentUuid() != null ) {
          _ms.setParentUuid( hasMS.getParentUuid() );
        }
        else {
          _ms.setParentUuid( hasMS.getUuid() );
        }
      }
      GrouperValidator v = EffectiveMembershipValidator.validate(_ms);
      if (v.isInvalid()) {
        throw new IllegalStateException( v.getErrorMessage() );
      }

      mships.add(_ms);
    }
    return mships;
  } // private Set _addHasMembersToOwner(hasMembers)

  /**
   * Add members of <i>member</i> to where <i>group</i> is a member.
   * @param   isMember  <i>Set</i> of <i>Membership</i> objects.
   * @since   1.2.0
   */
  private Set _addHasMembersToWhereGroupIsMember(Set isMember, Set hasMembers) 
    throws  IllegalStateException
  {
    Set mships = new LinkedHashSet();

    // Add the members of m to where g is a member but only if f == "members"
    if (this.getField().equals(Group.getDefaultList())) {
      Membership _ms;
      Membership hasMS;
      Membership isMS;
      Iterator      itHM; 
      Iterator      itIM    = isMember.iterator();
      while (itIM.hasNext()) {
        isMS = (Membership) itIM.next();
        
        itHM = hasMembers.iterator();
        while (itHM.hasNext()) {
          hasMS = (Membership) itHM.next();

          _ms = new Membership();
          _ms.setCreatorUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
          _ms.setDepth( isMS.getDepth() + hasMS.getDepth() + 2 );
          _ms.setListName( isMS.getListName() );
          _ms.setListType( isMS.getListType() );
          _ms.setMemberUuid( hasMS.getMemberUuid() );
          _ms.setOwnerUuid( isMS.getOwnerUuid() );
          _ms.setType(Membership.EFFECTIVE);
          if ( hasMS.getDepth() == 0 ) { // hasMember m was immediate
            _ms.setViaUuid( hasMS.getOwnerUuid() );
            _ms.setParentUuid( isMS.getUuid() );
          }
          else { // hasMember m was effective
            _ms.setViaUuid( hasMS.getViaUuid() );
            if ( hasMS.getParentUuid() != null ) {
              _ms.setParentUuid( hasMS.getParentUuid() );
            }
            else {
              _ms.setParentUuid( hasMS.getUuid() );
            }
          }
          GrouperValidator v = EffectiveMembershipValidator.validate(_ms);
          if (v.isInvalid()) {
            throw new IllegalStateException( v.getErrorMessage() );
          }

          mships.add(_ms);
        }
      }
    }

    return mships;
  } 

  /**
   * Add the member of each <code>isMember</code> membership to where the group is a member.
   * @param   isMember  <i>Set</i> of <i>Membership</i> objects.
   * @since   1.2.0
   */
  private Set _addMemberToWhereGroupIsMember(Set isMember) 
    throws  IllegalStateException
  {
    Set mships = new LinkedHashSet();

    // Add m to where g is a member if f == "members"
    if ( this.getField().equals( Group.getDefaultList() ) ) {
      Membership isMS;
      Iterator      itIM  = isMember.iterator();
      Membership _ms;
      while (itIM.hasNext()) {
        //isMS = (Membership) ( (Membership) isIt.next() ).get();
        isMS = (Membership) itIM.next();

        _ms = new Membership();
        _ms.setCreatorUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
        _ms.setDepth( isMS.getDepth() + this.getMembership().getDepth() + 1 );
        _ms.setListName( isMS.getListName() );
        _ms.setListType( isMS.getListType() );
        _ms.setMember(this.getMember());
        _ms.setMemberUuid( this.getMembership().getMemberUuid() );
        _ms.setOwnerUuid( isMS.getOwnerUuid() );
        _ms.setType(Membership.EFFECTIVE);
        if ( this.getMembership().getDepth() == 0 ) { // this memberhsip was immediate
          _ms.setViaUuid( this.getMembership().getOwnerUuid() );
          _ms.setParentUuid( isMS.getUuid() );
        }
        else { // that membership was effective
          _ms.setViaUuid( this.getMembership().getViaUuid() ); 
          if ( this.getMembership().getParentUuid() != null ) {
            _ms.setParentUuid( this.getMembership().getParentUuid() );
          }
          else {
            _ms.setParentUuid( this.getMembership().getUuid() );
          }
        }
        GrouperValidator v = EffectiveMembershipValidator.validate(_ms);
        if (v.isInvalid()) {
          throw new IllegalStateException( v.getErrorMessage() );
        }

        mships.add(_ms);
      }
    }

    return mships;
  } 

  // @since   1.2.0
  private Set _createNewCompositeMembershipObjects(Set memberUUIDs) 
    throws  IllegalStateException
  {
    GrouperValidator  v;
    Set               mships  = new LinkedHashSet();
    Membership     _ms;
    Iterator          it      = memberUUIDs.iterator();
    while (it.hasNext()) {
      _ms = new Membership();
      _ms.setCreatorUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
      _ms.setDepth(0);
      _ms.setListName( this.getField().getName() );
      _ms.setListType( this.getField().getType().toString() );
      _ms.setMemberUuid( (String) it.next() );
      _ms.setOwnerUuid( this.getOwnerUuid() );
      _ms.setParentUuid(null);
      _ms.setType(Membership.COMPOSITE);
      _ms.setViaUuid( this.getComposite().getUuid() );
     
      v = CompositeMembershipValidator.validate(_ms);
      if (v.isInvalid()) {
        throw new IllegalStateException( v.getErrorMessage() );
      }
      mships.add(_ms);
    }
    return mships;
  } // private Set _createNewCompositeMembershipObjects(memberUUIDs)

  /**
   * Evaluate the addition of a new composite membership.
   * @since   1.2.0
   */
  private void _evaluateAddCompositeMembership()
    throws  IllegalStateException
  {
    Set composites = new LinkedHashSet();
    if      ( this.getComposite().getType().equals(CompositeType.COMPLEMENT) )    {
      composites.addAll( this._evaluateAddCompositeMembershipComplement() );
    }
    else if ( this.getComposite().getType().equals(CompositeType.INTERSECTION) )  {
      composites.addAll( this._evaluateAddCompositeMembershipIntersection() );
    }
    else if ( this.getComposite().getType().equals(CompositeType.UNION) )         {
      composites.addAll( this._evaluateAddCompositeMembershipUnion() );
    }
    else {
      throw new IllegalStateException( E.MOF_CTYPE + this.getComposite().getType().toString() );
    }
    this.addEffectiveSaves(composites);
    // we also need to propogate changes up to where the composite owner is a member
    this.addEffectiveSaves( 
      this._addHasMembersToWhereGroupIsMember(
        GrouperDAOFactory.getFactory().getMembership().findAllByMember( this.getGroup().toMember().getUuid() ), 
        composites
      )
    );

    this.addSaves( this.getEffectiveSaves() );
    this._identifyGroupsAndStemsToMarkAsModified();
  } 

  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipComplement() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getLeftFactorUuid() ) );
    memberUUIDs.removeAll( this._findMemberUUIDs(this.getComposite() .getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 
  
  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipIntersection() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getLeftFactorUuid() ) );
    memberUUIDs.retainAll( this._findMemberUUIDs( this.getComposite().getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipUnion() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getLeftFactorUuid() ) );
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  // @since   1.2.0
  // TODO 20070531 split; this method has gotten too large
  private void _evaluateAddImmediateMembership(GrouperSession s, final Field f, final Member _m) 
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Membership _ms = new Membership();
        _ms.setCreatorUuid( grouperSession.getMember().getUuid() );
        _ms.setListName( f.getName() );
        _ms.setListType( f.getType().toString() );
        _ms.setMemberUuid( _m.getUuid() );
        _ms.setOwnerUuid( DefaultMemberOf.this.getOwnerUuid() );

        GrouperValidator v = ImmediateMembershipValidator.validate(_ms);
        if (v.isInvalid()) {
          throw new IllegalStateException( v.getErrorMessage() );
        }

        try {
          // TODO 20070531 look into finding a way to avoid SchemaExceptions here
          DefaultMemberOf.this.setField( FieldFinder.find( _ms.getListName() ) );
        }
        catch (SchemaException eS) {
          throw new IllegalStateException( eS.getMessage(), eS );
        }
        DefaultMemberOf.this.setMember(_m);
        DefaultMemberOf.this.setMembership(_ms);

        Set results = new LinkedHashSet();
        // If we are working on a group, where is it a member
        Set isMember = new LinkedHashSet();
        if ( DefaultMemberOf.this.getGroup() != null ) {
          isMember = GrouperDAOFactory.getFactory().getMembership().findAllByMember( DefaultMemberOf.this.getGroup().toMember().getUuid() );
        }
        // Members of _m if owner is a group
        Set<Membership> hasMembers = DefaultMemberOf.this._findMembersOfMember();
        // Add _m to where owner is member if f == "members"
        results.addAll( DefaultMemberOf.this._addMemberToWhereGroupIsMember(isMember) );
        // Add members of _m to owner
        results.addAll( DefaultMemberOf.this._addHasMembersToOwner(hasMembers) );
        // Add members of _m to where owner is member if f == "members"
        results.addAll( DefaultMemberOf.this._addHasMembersToWhereGroupIsMember(isMember, hasMembers) );

        DefaultMemberOf.this.addEffectiveSaves(results);

        DefaultMemberOf.this.addSave(_ms); // Save the immediate
        DefaultMemberOf.this.addSaves( DefaultMemberOf.this.getEffectiveSaves() );
        DefaultMemberOf.this._identifyGroupsAndStemsToMarkAsModified();
        return null;
      }
      
    });
  } // private void _evaluateAddImmediateMembership(s, f, _m)

  // @since   1.2.0
  private void _evaluateDeleteCompositeMembership() 
    throws  IllegalStateException
  {
    Membership _ms;
    DefaultMemberOf      mof;
    Iterator      it  = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField( 
      this.getGroup().getUuid(), Group.getDefaultList() 
    ).iterator();
    try {
      MemberDAO dao = GrouperDAOFactory.getFactory().getMember();
      while (it.hasNext()) {
        _ms = (Membership) it.next();
        mof = new DefaultMemberOf();
        mof.deleteImmediate( 
          GrouperSession.staticGrouperSession(), this.getGroup(), _ms, dao.findByUuid( _ms.getMemberUuid() ) 
        );
        this.addDeletes( mof.getDeletes() );
      }
    }
    catch (MemberNotFoundException eMNF) {
      throw new IllegalStateException( eMNF.getMessage(), eMNF );
    }
    this._identifyGroupsAndStemsToMarkAsModified();
  } // private void _evalulateDeleteCompositeMembership()

  /**
   * @since   1.2.0
   */
  private void _evaluateDeleteImmediateMembership(GrouperSession s, final Membership _ms, final Member _m) 
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          DefaultMemberOf.this.setField( FieldFinder.find( _ms.getListName() ) );
        }
        catch (SchemaException eS) {
          throw new IllegalStateException( eS.getMessage(), eS );
        }
        DefaultMemberOf.this.setMember(_m);
        DefaultMemberOf.this.setMembership(_ms);

        // Find child memberships that need deletion
        Set       children  = new LinkedHashSet();
        Iterator  it        = MembershipFinder.internal_findAllChildrenNoPriv(_ms).iterator();
        while (it.hasNext()) {
          children.add( (Membership) it.next() );
        }
        DefaultMemberOf.this.addEffectiveDeletes(children);
        // Find all effective memberships that need deletion
        try {
          DefaultMemberOf.this.addEffectiveDeletes( MembershipFinder.internal_findAllForwardMembershipsNoPriv(_ms, children) );
        }
        catch (SchemaException eS) {
          throw new IllegalStateException( eS.getMessage(), eS );
        }

        // And now set everything else
        DefaultMemberOf.this.addDeletes( DefaultMemberOf.this.getEffectiveDeletes() );
        DefaultMemberOf.this._identifyGroupsAndStemsToMarkAsModified();

        DefaultMemberOf.this.addDelete(_ms); // Delete the immediate
        return null;
      }
      
    });
  } // private void _evaluateDeleteImmediateMembership(s, _ms, _m)

  // Find m's hasMembers
  private Set<Membership> _findMembersOfMember() {
    Set<Membership> hasMembers = new LinkedHashSet();
    if (this.getMember().getSubjectTypeId().equals("group")) {
      hasMembers = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField(
        this.getMember().getSubjectId(), Group.getDefaultList()
      );
    }
    return hasMembers;
  } // private Set _findMembersOfMember()

  // Given a group uuid, find the uuids of all of its members
  // @since   1.2.0
  private Set _findMemberUUIDs(String groupUUID) 
    throws  IllegalStateException
  {
    try {
      Set       memberUUIDs = new LinkedHashSet();
      Group  _g          = GrouperDAOFactory.getFactory().getGroup().findByUuid(groupUUID);
      Iterator  it          = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField(
         _g.getUuid(), Group.getDefaultList() 
      ).iterator();
      while (it.hasNext()) {
        memberUUIDs.add( ( (Membership) it.next() ).getMemberUuid() );  
      }
      return memberUUIDs;
    }
    catch (GroupNotFoundException eGNF) {
      throw new IllegalStateException( eGNF.getMessage(), eGNF );
    }
  } // private Set _findMemberUUIDs(groupUUID)

  // @since   1.2.0
  private void _identifyGroupsAndStemsToMarkAsModified() {
    Map modified  = new HashMap();
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.getEffectiveSaves().iterator() );
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.getEffectiveDeletes().iterator() );
    Map groups    = (Map) modified.get("groups");
    Map stems     = (Map) modified.get("stems");
    this.setModifiedGroups( new LinkedHashSet( groups.values() ) );
    this.setModifiedStems( new LinkedHashSet( stems.values() ) );
  } // private void _identifyGroupsAndStemsToMarkAsModified()

} 

