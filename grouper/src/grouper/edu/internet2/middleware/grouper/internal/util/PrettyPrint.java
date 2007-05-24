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

package edu.internet2.middleware.grouper.internal.util;
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.GroupNotFoundException;
import  edu.internet2.middleware.grouper.MemberNotFoundException;
import  edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;

/**
 * Utility class for pretty printing objects.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrettyPrint.java,v 1.2 2007-05-24 19:22:47 blair Exp $
 * @since   1.2.0
 */
public class PrettyPrint {

  // PRIVATE CLASS CONSTANTS //
  private static final String CLOSE = ">";
  private static final String DELIM = "|";
  private static final String OPEN  = "=<";

  // PUBLIC CLASS METHODS //

  /**
   * Return a pretty printed string of the specified object (if possible).
   * <p/>
   * @since   1.2.0
   */
  public static String pp(Object obj) {
    if      (obj instanceof CompositeDTO)  {
      return _pp( (CompositeDTO) obj );
    } 
    else if (obj instanceof MembershipDTO) {
      return _pp( (MembershipDTO) obj );
    }
    return obj.toString();
  }


  // PRIVATE CLASS METHODS //

  /**
   * Return a pretty printed <i>CompositeDTO</i>.
   */
  private static String _pp(CompositeDTO _c) {
    try {
      GroupDTO  _gOwner = GrouperDAOFactory.getFactory().getGroup().findByUuid( _c.getFactorOwnerUuid() );
      GroupDTO  _gLeft  = GrouperDAOFactory.getFactory().getGroup().findByUuid( _c.getLeftFactorUuid() );
      GroupDTO  _gRight = GrouperDAOFactory.getFactory().getGroup().findByUuid( _c.getRightFactorUuid() );
      return _c.getClass().getName() 
        + OPEN
        + "owner="  + _gOwner.getAttributes().get("name")
        + DELIM
        + "left="   + _gLeft.getAttributes().get("name")
        + DELIM
        + "right="  + _gRight.getAttributes().get("name")
        + DELIM
        + "type="   + _c.getType()
        + DELIM
        + "uuid="   + _c.getUuid()
        + CLOSE
        ;
    }
    catch (GroupNotFoundException eGNF) {
      throw new IllegalStateException( eGNF.getMessage(), eGNF );
    } 
  }

  /**
   * Return a pretty printed <i>MembershipDTO</i>.
   * @since   1.2.0
   */
  private static String _pp(MembershipDTO _ms) {
    try {
      GroupDTO  _g  = GrouperDAOFactory.getFactory().getGroup().findByUuid( _ms.getOwnerUuid() );
      MemberDTO _m  = GrouperDAOFactory.getFactory().getMember().findByUuid( _ms.getMemberUuid() );
      return _ms.getClass().getName()
        + OPEN 
        + "group="    + _g.getAttributes().get("name")
        + DELIM
        + "member="   + _m.getSubjectId() + "@" + _m.getSubjectSourceId()
        + DELIM
        + "listName=" + _ms.getListName()
        + DELIM
        + "depth="    + _ms.getDepth()
        + DELIM
        + "type="     + _ms.getType()
        + DELIM
        + "via="      + _ms.getViaUuid()
        + DELIM
        + "uuid="     + _ms.getUuid()
        + CLOSE
        ;
    }
    catch (GroupNotFoundException eGNF) {
      throw new IllegalStateException( eGNF.getMessage(), eGNF );
    }
    catch (MemberNotFoundException eMNF) {
      throw new IllegalStateException( eMNF.getMessage(), eMNF );
    }
  }
 
} 

