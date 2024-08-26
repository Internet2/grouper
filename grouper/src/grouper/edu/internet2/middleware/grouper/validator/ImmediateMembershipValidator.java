/**
 * Copyright 2014 Internet2
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
 */
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

package edu.internet2.middleware.grouper.validator;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;

/** 
 * An immediate member is directly assigned to a group.
 * A composite group has no immediate members.  Note that a 
 * member can have 0 to 1 immediate memberships
 * to a single group, and 0 to many effective memberships to a group.
 * A group can have potentially unlimited effective 
 * memberships
 * 
 * @author  blair christensen.
 * @version $Id: ImmediateMembershipValidator.java,v 1.6 2009-12-07 07:31:08 mchyzer Exp $
 * @since   1.2.0
 */
public class ImmediateMembershipValidator extends MembershipValidator {

  /** */
  public static final String INVALID_CIRCULAR    = "membership cannot be circular";
  /** */
  public static final String INVALID_CIRCULAR_VIA_COMPOSITE    = "membership cannot be circular via a composite";
  /** */
  public static final String INVALID_DEPTH       = "membership depth != 0";
  /** */
  public static final String INVALID_EXISTS      = "membership already exists";
  /** */
  public static final String INVALID_PARENTUUID  = "membership cannot have parentUuid";
  /** */
  public static final String INVALID_TYPE        = "membership type is not IMMEDIATE";
  /** */
  public static final String INVALID_VIAUUID     = "membership cannot have viaUuid";


  /**
   * 
   * @param _ms
   * @return membership validator
   */
  public static MembershipValidator validate(Membership _ms) {
    ImmediateMembershipValidator  v     = new ImmediateMembershipValidator();
    NotNullValidator              vNull = NotNullValidator.validate(_ms);
    if (vNull.isInvalid()) {
      v.setErrorMessage( vNull.getErrorMessage() );
    }
    // validate immediate membership attributes
    else if ( !MembershipType.IMMEDIATE.getTypeString().equals( _ms.getType() ) ) { // type must be immediate
      v.setErrorMessage(INVALID_TYPE);
    }
    else if ( _ms.getDepth() != 0 )                           { // must have depth == 0
      v.setErrorMessage(INVALID_DEPTH);
    }
    else if ( _ms.getViaGroupId() != null )                      { // must not have a via
      v.setErrorMessage(INVALID_VIAUUID);
    }
    else if ( _ms.getViaCompositeId() != null )                      { // must not have a via
      v.setErrorMessage(INVALID_VIAUUID);
    }
    else if ( v._isCircular(_ms) )                            { // cannot be a direct member of oneself
      v.setErrorMessage(INVALID_CIRCULAR);
    } else if (v._isCircularViaComposite(_ms)) {
      v.setErrorMessage(INVALID_CIRCULAR_VIA_COMPOSITE);
    } else if (GrouperConfig.ALL.equals(_ms.getMember().getSubjectId()) && _ms.getFieldId().equals(Group.getDefaultList().getUuid())) {
      v.setErrorMessage("GrouperAll can't be member of a group.");
    } else if (GrouperConfig.ALL.equals(_ms.getMember().getSubjectId()) && _ms.getField().isGroupAccessField() && 
        AccessPrivilege.MANAGE_PRIVILEGES.contains(Privilege.listToPriv(_ms.getField().getName(), true))) {
      v.setErrorMessage("GrouperAll can't get a manage privilege.");
    } else {
      // Perform generic Membership validation
      MembershipValidator vMS = MembershipValidator.validate(_ms);
      if (vMS.isInvalid()) {
        v.setErrorMessage( vMS.getErrorMessage() );
      }
      else {
        v.setIsValid(true);
      }
    }
    return v;
  }

  /**
   * TODO 20070531 it would be nice if i could avoid exceptions here
   * @param _ms
   * @return if circular
   * @throws IllegalStateException
   */
  private boolean _isCircular(Membership _ms) 
    throws  IllegalStateException  {
    if ( GrouperConfig.LIST.equals( _ms.getListName() ) ) {
      try {
        Group  _g  = GrouperDAOFactory.getFactory().getGroup().findByUuid( _ms.getOwnerGroupId(), true );
        Member _m  = GrouperDAOFactory.getFactory().getMember().findByUuid( _ms.getMemberUuid(), true );
        if ( _g.getUuid().equals( _m.getSubjectId() ) ) {
          return true;
        }
      }
      catch (GroupNotFoundException eGNF)   {
        throw new IllegalStateException( 
          "error verifying membership is not circular: " + eGNF.getMessage(), eGNF
        );
      }
      catch (MemberNotFoundException eMNF)  {
        throw new IllegalStateException( 
          "error verifying membership is not circular: " + eMNF.getMessage(), eMNF
        );
      }
    }
    return false;
  }

  private boolean _isCircularViaComposite(Membership membership) {
    
    if (membership.getOwnerGroupId() != null && membership.getField().equals(Group.getDefaultList()) && membership.getMember().getSubjectTypeId().equals("group")) {
      String sql = "select count(*) from grouper_composites gc, grouper_group_set ggs1, grouper_group_set ggs2 "
          + "where (gc.left_factor = ggs1.owner_group_id or gc.right_factor=ggs1.owner_group_id) "
          + "and ggs1.member_group_id = ? and ggs1.field_id = ? "
          + "and ggs2.owner_group_id = ? and ggs2.member_group_id = gc.owner and ggs2.field_id = ?";
      
      List<Object> params = new ArrayList<Object>();
      params.add(membership.getOwnerGroupId());
      params.add(membership.getFieldId());
      params.add(membership.getMember().getSubjectId());
      params.add(membership.getFieldId());

      List<Type> types = new ArrayList<Type>();
      types.add(StringType.INSTANCE);
      types.add(StringType.INSTANCE);
      types.add(StringType.INSTANCE);
      types.add(StringType.INSTANCE);
      
      int count = HibernateSession.bySqlStatic().select(int.class, sql, params, types);
      if (count > 0) {
        return true;
      }
    }

    return false;
  }
}

