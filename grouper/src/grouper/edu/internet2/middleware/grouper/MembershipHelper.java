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
import  edu.internet2.middleware.subject.provider.*;
import  org.apache.commons.logging.*;
import  org.apache.commons.lang.builder.*;


/**
 * {@link Membership} utility helper class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipHelper.java,v 1.1.2.1 2006-05-19 16:13:32 blair Exp $
 */
class MembershipHelper {

  // Private Class Constants //
  private static final Log LOG = LogFactory.getLog(MembershipHelper.class);


  // Protected Class Methods //
  protected static String getPretty(Membership ms) {
    ms.setSession(ms.getSession()); // TODO Is this still necessary?
    String  via = new String();
    if (ms.getVia_id() != null) {
      if (ms.getVia_id() instanceof Composite) {
        Composite c = (Composite) ms.getVia_id();
        via = c.getType().toString() 
          + "/group=" + c.getOwnerGroup().getName()
          + "/left="  + c.getLeftGroup().getName() 
          + "/right=" + c.getRightGroup().getName()
          ;
      }
      else {
        Group v = (Group) ms.getVia_id();
        via = v.getName();
      }
    }
    Owner o = ms.getOwner_id();
    if      (o instanceof Composite) {
      return new ToStringBuilder(ms).toString();  // TODO Improve
    }
    else if (o instanceof Group) {
      Group   g   = (Group) o;
      return new ToStringBuilder(ms)  //, ToStringStyle.SIMPLE_STYLE)
        .append(  "group"   , g.getName()             )
        .append(  "subject" , ms.getMember_id()       )
        .append(  "field"   , ms.getField().getName() )
        .append(  "depth"   , ms.getDepth()           )
        .append(  "via"     , via                     )
        .toString();
    }
    else if (o instanceof Stem) {
      Stem ns = (Stem) o;
      return new ToStringBuilder(ms)  //, ToStringStyle.SIMPLE_STYLE)
        .append(  "stem"    , ns.getName()            )
        .append(  "subject" , ms.getMember_id()       )
        .append(  "field"   , ms.getField().getName() ) 
        .append(  "depth"   , ms.getDepth()           )
        .append(  "via"     , via                     )
        .toString();
    }
    else {
      throw new RuntimeException("INVALID OWNER CLASS: " + o.getClass().getName());
    }
  } // protected static String getPretty(ms)

} // class MembershipHelper
 
