/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.*;


/**
 * Find memberships within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.1.2.9 2005-11-07 00:31:15 blair Exp $
 */
class MembershipFinder {

  // Protected Class Methods

  // @return  {@link Membership} if it exists
  protected static Membership findMembership(Group g, Member m, String field) 
    throws MembershipNotFoundException
  {
    try {
      Membership  ms      = null;
      Session     hs      = HibernateHelper.getSession();
      List        mships  = hs.find(
                          "from Membership as ms where "
                          + "ms.group_id      = ?      "
                          + "and ms.member_id = ?      "
                          + "and ms.list_id   = ?      ",
                          new Object[] {
                            g.getUuid(),
                            m.getUuid(),
                            field
                          },
                          new Type[] {
                            Hibernate.STRING,
                            Hibernate.STRING,
                            Hibernate.STRING
                          }
                        )
                        ;
      if (mships.size() == 1) {
        ms = (Membership) mships.get(0);
      }
      hs.close();
      if (ms == null) {
        throw new MembershipNotFoundException("membership not found");
      }
      return ms; 
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error checking membership: " + e.getMessage()
      );  
    }
  } // protected static Membership findMembership(g, m, field)

}

