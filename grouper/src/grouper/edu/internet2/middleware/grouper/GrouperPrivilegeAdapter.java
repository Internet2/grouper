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
import  java.util.*;


/** 
 * @author  blair christensen.
 * @version $Id: GrouperPrivilegeAdapter.java,v 1.3 2006-12-14 15:49:45 blair Exp $
 * @since   1.1.0
 */
class GrouperPrivilegeAdapter {

  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static Field getField(Map priv2list, Privilege p)
    throws  SchemaException
  {
    if (priv2list.containsKey(p)) {
      return FieldFinder.find( (String) priv2list.get(p) );
    }
    throw new SchemaException("invalid privilege");
  } // protected static Field getField(priv2list, p)

  // @since   1.1.0
  protected static Set getPrivs(
    GrouperSession s, Subject subj, Member m, Privilege p, Iterator it
  )
    throws  SchemaException
  {
    // TODO 20061005 refactor: this is a monstrosity
    Membership  ms;
    Subject     owner   = subj;
    Set         privs   = new LinkedHashSet();
    boolean     revoke  = true;
    while (it.hasNext()) {
      ms = (Membership) it.next();
      ms.setSession(s);
      try {
        if (!SubjectHelper.eq(m.getSubject(), subj)) {
          owner   = m.getSubject();
          revoke  = false;
        }
      }
      catch (SubjectNotFoundException eSNF) {
        ErrorLog.error(GrouperPrivilegeAdapter.class, eSNF.getMessage());
      }
      try {
        owner   = ms.getViaGroup().toSubject();
        revoke  = false;
      }
      catch (GroupNotFoundException eGNF) {
        // ignore
      }
      try {
        if (Privilege.isAccess(p))  {
          privs.add(
            new AccessPrivilege(ms.getGroup(), subj, owner, p, s.getAccessClass(), revoke)
          );
        }
        else                        {
          privs.add(
            new NamingPrivilege(ms.getStem(), subj, owner, p, s.getNamingClass(), revoke)
          );
        }
      }
      catch (GroupNotFoundException eGNF) {
        ErrorLog.error(GrouperPrivilegeAdapter.class, eGNF.getMessage());
      }
      catch (StemNotFoundException eNSNF) {
        ErrorLog.error(GrouperPrivilegeAdapter.class, eNSNF.getMessage());
      }
    }
    return privs;
  } // protected Set getPrivs(s, subj, m, p, it)

  // @since   1.2.0
  protected static Set internal_getGroupsWhereSubjectHasPriv(GrouperSession s, Member m, Field f)
    throws  GroupNotFoundException
  {
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    // Perform query as ROOT to prevent privilege constraints getting in the way
    Iterator    it      = MembershipFinder.findMemberships( s.getRootSession(), m, f ).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      ms.setSession(s);
      mships.add( ms.getGroup() );
    }
    return mships;
  } // protected static Set internal_getGroupsWhereSubjectHasPriv(s, m, f)

  // @since   1.2.0
  protected static Set internal_getStemsWhereSubjectHasPriv(GrouperSession s, Member m, Field f)
    throws  StemNotFoundException
  {
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    // Perform query as ROOT to prevent privilege constraints getting in the way
    Iterator    it      = MembershipFinder.findMemberships( s.getRootSession(), m, f ).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      ms.setSession(s);
      mships.add( ms.getStem() );
    }
    return mships;
  } // protected static Set internal_getStemsWhereSubjectHasPriv(s, m, f)

} // class GrouperPrivilegeAdapter

