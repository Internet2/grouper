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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;

/** 
 * @author  blair christensen.
 * @version $Id: MembershipValidator.java,v 1.22 2007-02-08 16:25:25 blair Exp $
 * @since   1.0
 */
class MembershipValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void internal_validateComposite(MembershipDTO dto)
    throws  ModelException
  {
    _validate(dto, Membership.COMPOSITE); 
    // Verify Depth
    if ( dto.getDepth() != 0 ) {
      throw new ModelException( E.ERR_D + dto.getDepth() );
    }
    // Verify Via
    if ( dto.getViaUuid() == null ) {
      throw new ModelException(E.ERR_VC + "null");
    }    
    // Verify Parent Membership
    if ( dto.getParentUuid() != null ) {
      throw new ModelException(E.ERR_PMS);
    }
  } // protected static void internal_validateComposite(dto)

  // @since   1.2.0
  protected static void internal_validateEffective(MembershipDTO dto)
    throws  ModelException
  {
    _validate(dto, Membership.EFFECTIVE); 
    // Verify Depth
    if ( !(dto.getDepth() > 0) ) {
      throw new ModelException( E.ERR_D + dto.getDepth() );
    }
    // Verify Via
    if ( dto.getViaUuid() == null ) {
      throw new ModelException(E.ERR_VC + "null");
    }    
    // Verify Parent Membership
    if ( dto.getParentUuid() == null ) {
      throw new ModelException(E.MSV_NO_PARENT);
    }
  } // protected static void internal_validateEffective(dto)

  // @since   1.2.0
  protected static void internal_validateImmediate(MembershipDTO dto)
    throws  ModelException
  {
    _validate(dto, Membership.IMMEDIATE); 
    _validateDoesNotExist(dto);
    _notCircular(dto);
    // Verify Depth
    if ( dto.getDepth() != 0 ) {
      throw new ModelException( E.ERR_D + dto.getDepth() );
    }
    // Verify Via
    if ( dto.getViaUuid() != null ) {
      throw new ModelException(E.ERR_IV);
    }
    // Verify Parent Membership
    if ( dto.getParentUuid() != null ) {
      throw new ModelException(E.ERR_PMS);
    }
  } // protected static void internal_validateImmediate(dto)


  // PRIVATE CLASS METHODS //

  // @since 1.0
  private static void _notCircular(MembershipDTO dto) 
    throws  ModelException
  {
    try {
      if ( dto.getListName().equals(GrouperConfig.LIST) ) {
        GroupDTO  g = HibernateGroupDAO.findByUuid( dto.getOwnerUuid() );
        MemberDTO m = HibernateMemberDAO.findByUuid( dto.getMemberUuid() );
        // TODO 20070124 is this still sufficient and accurate?
        if ( g.getUuid().equals( m.getSubjectId() ) ) {
          throw new ModelException(E.MSV_CIRCULAR);
        }
      }
    }
    catch (GroupNotFoundException eGNF) {
      // If the owner is not a group then it can't be circular
    } 
    catch (MemberNotFoundException eMNF) {
      throw new ModelException( eMNF.getMessage(), eMNF );
    } 
  } // private static void _notCircular(dto)

  // @since 1.0
  private static void _validate(MembershipDTO dto, String type) 
    throws  ModelException
  {
    Validator.internal_notNullPerModel( dto.getCreateTime(),  "null creation time" );
    Validator.internal_notNullPerModel( dto.getCreatorUuid(), "null creator"       );
    // Verify type
    if ( !dto.getType().equals(type) ) {
      throw new ModelException( E.MSV_TYPE + dto.getType() );
    }
    // Verify Owner
    try {
      HibernateGroupDAO.findByUuid( dto.getOwnerUuid() );
    }
    catch (GroupNotFoundException eGNF) {
      try {
        HibernateStemDAO.findByUuid( dto.getOwnerUuid() );
      }
      catch (StemNotFoundException eNSNF) {
        throw new ModelException("unable to find membership owner");
      }
    }
    // Verify Member
    try {
      HibernateMemberDAO.findByUuid( dto.getMemberUuid() );
    }
    catch (GrouperDAOException eDAO) {
      throw new ModelException(E.ERR_M + ": " + eDAO.getMessage() );
    }
    catch (MemberNotFoundException eMNF) {
      throw new ModelException(E.ERR_M + ": " + eMNF.getMessage() );
    }
    // Verify Field
    try {
      Field f = FieldFinder.find( dto.getListName() );
      if (! 
        ( 
              ( f.getType().equals(FieldType.ACCESS) ) 
          ||  ( f.getType().equals(FieldType.LIST  ) )    
          ||  ( f.getType().equals(FieldType.NAMING) )
        )
      )
      {
        throw new ModelException(E.ERR_FT + f.getType());
      }
    }
    catch (SchemaException eS) {
      throw new ModelException( eS.getMessage(), eS );
    }
  } // private static void _validate(dto, type)

  // Verify that membership doesn't already exist
  // @since 1.0
  private static void _validateDoesNotExist(MembershipDTO dto) 
    throws  ModelException
  {
    try {
      HibernateMembershipDAO.findByOwnerAndMemberAndFieldAndType(
        dto.getOwnerUuid(), dto.getMemberUuid(), FieldFinder.find( dto.getListName() ), Membership.IMMEDIATE
      );
      throw new ModelException(E.ERR_MAE);
    }
    catch (MembershipNotFoundException eMNF) {
      // Ignore - this is what we want. 
    }
    catch (SchemaException eS) {
      throw new ModelException( eS.getMessage(), eS );
    }
  } // private static void _validateDoesNotExist(dto)

} // class MembershipValidator

