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
import  org.apache.commons.lang.builder.*;

/**
 * {@link Membership} utility helper class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipHelper.java,v 1.11 2007-01-09 17:30:23 blair Exp $
 */
class MembershipHelper {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static String internal_getPretty(Membership ms) {
    String  via = _getViaString( ms.getVia_id() );
    Owner   o   = ms.getOwner_id();
    if      (o instanceof Composite) {
      return new ToStringBuilder(ms).toString();
    }
    else if (o instanceof Group) {
      Group   g   = (Group) o;
      return new ToStringBuilder(ms)
        .append(  "group"   , g.getName()             )
        .append(  "subject" , ms.getMember_id()       )
        .append(  "field"   , ms.getField().getName() )
        .append(  "depth"   , ms.getDepth()           )
        .append(  "via"     , via                     )
        .toString();
    }
    else if (o instanceof Stem) {
      Stem ns = (Stem) o;
      return new ToStringBuilder(ms)
        .append(  "stem"    , ns.getName()            )
        .append(  "subject" , ms.getMember_id()       )
        .append(  "field"   , ms.getField().getName() ) 
        .append(  "depth"   , ms.getDepth()           )
        .append(  "via"     , via                     )
        .toString();
    }
    else {
      throw new GrouperRuntimeException("INVALID OWNER CLASS: " + o.getClass().getName());
    }
  } // protected static String internal_getPretty(ms)

  // @since   1.2.0
  private static String _getViaString(String viaUUID) {
    String s = GrouperConfig.EMPTY_STRING;
    if (viaUUID != null) {
      try {
        Owner via = HibernateOwnerDAO.findByUuid(viaUUID);
        if (via instanceof Composite) {
          Composite c     = (Composite) via;
          String    left  = GrouperConfig.EMPTY_STRING;
          String    owner = GrouperConfig.EMPTY_STRING;
          String    right = GrouperConfig.EMPTY_STRING;
          try {
            owner = c.getOwnerGroup().getName();
            left  = c.getLeftGroup().getName();
            right = c.getRightGroup().getName();
          }
          catch (GroupNotFoundException eGNF) {
            // TODO 20070109 what goes here?
          }
          s = c.getType().toString() 
            + "/group=" + owner
            + "/left="  + left
            + "/right=" + right
            ;
        }
        else {
          Group g = (Group) via;
          s       = g.getName();
        }
      }
      catch (OwnerNotFoundException eONF) {
        // TODO 20070109 what goes here?
      }
    }
    return s;
  } // private static String _getViaString(viaUUID)


} // class MembershipHelper
 
