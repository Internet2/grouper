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

package edu.internet2.middleware.grouper.privs;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/** 
 * @author  blair christensen.
 * @version $Id: GrouperPrivilegeAdapter.java,v 1.4 2008-10-23 04:48:57 mchyzer Exp $
 * @since   1.1.0
 */
public class GrouperPrivilegeAdapter {
	
	// PRIVATE CLASS VARIABLES //
	  private static Map<String,Privilege> list2priv = new HashMap();


	  // STATIC //
	  //2007-11-02 Gary Brown
	  //Not ideal but need to lookup privilege for list
	  static {
	    list2priv.put( "admins",  AccessPrivilege.ADMIN      );
	    list2priv.put( "optins",  AccessPrivilege.OPTIN    );
	    list2priv.put( "optouts", AccessPrivilege.OPTOUT   );
	    list2priv.put( "readers", AccessPrivilege.READ   );
	    list2priv.put( "updaters",AccessPrivilege.UPDATE   );
	    list2priv.put( "viewers",AccessPrivilege.VIEW      );
	  } // static

  // public CLASS METHODS //

  // @since   1.2.0
  public static Field internal_getField(Map priv2list, Privilege p)
    throws  SchemaException
  {
    if (priv2list.containsKey(p)) {
      return FieldFinder.find( (String) priv2list.get(p) );
    }
    throw new SchemaException("invalid privilege");
  } // public static Field internal_getField(priv2list, p)

  // @since   1.2.0
  //2007-11-02 Gary Brown
  //If p==null determine by looking at the Membership list
  //Discard those which are not privileges i.e. members / custom lists
  //Added Owner to signature so we don't need to compute it 
  //consequently all Memberships must be of the same Owner
  public static Set<? extends GrouperPrivilege> internal_getPrivs(
    GrouperSession s, final Owner ownerGroupOrStem,final Subject subj, final Member m, final Privilege p, final Iterator it
  )
    throws  SchemaException
  {
    return (Set)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        // TODO 20070531 split and test
        Membership  ms;
        Subject     owner   = subj;
        Set         privs   = new LinkedHashSet();
        boolean     revoke  = true;
        Privilege localP = null;
        Subject mSubj = new LazySubject(m);
        while (it.hasNext()) {
          ms = (Membership) it.next() ;
          if(p!=null) {
            localP=p;
          }else{
            localP=list2priv.get(ms.getList().getName());
          }
          
          //Since we are getting everything, could get members or custom lists which do not correspond to privileges
          if(localP==null) continue;
          try {
            if (!SubjectHelper.eq(mSubj, subj)) {
              owner   = m.getSubject();
              revoke  = false;
            }
          }
          catch (SubjectNotFoundException eSNF) {
            LOG.error(eSNF.getMessage());
          }
          if ( ms.getViaUuid() != null ) {
            try {
              owner   = ms.getViaGroup().toSubject();
              revoke  = false;
            }
            catch (GroupNotFoundException eGNF) {
              LOG.error(eGNF.getMessage() );
            }
          }
          
            if (Privilege.isAccess(localP))  {
              privs.add(
                new AccessPrivilege((Group)ownerGroupOrStem, subj, owner, localP, grouperSession.getAccessClass(), revoke)
              );
            }
            else{
              privs.add(
                new NamingPrivilege( (Stem)ownerGroupOrStem, subj, owner, localP, grouperSession.getNamingClass(), revoke )
              );
            }

        }
        return privs;
      }
      
    });
  } // public Set internal_getPrivs(s, subj, m, p, it)

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperPrivilegeAdapter.class);

  /**
   * @since   1.2.0
   * @param s
   * @param m
   * @param f
   * @return the set
   * @throws GroupNotFoundException
   */
  public static Set internal_getGroupsWhereSubjectHasPriv(GrouperSession s, final Member m, final Field f)
    throws  GroupNotFoundException
  {
    try {
      return (Set)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          Set         mships  = new LinkedHashSet();
          Membership  ms;
          // Perform query as ROOT to prevent privilege constraints getting in the way
          Iterator    it      = MembershipFinder.internal_findMemberships( grouperSession.internal_getRootSession(), m, f ).iterator();
          while (it.hasNext()) {
            ms = (Membership) it.next();
            try {
              mships.add( ms.getGroup() );
            } catch (GroupNotFoundException gnfe) {
              throw new GrouperSessionException(gnfe);
            }
          }
          return mships;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof GroupNotFoundException) {
        throw (GroupNotFoundException)gse.getCause();
      }
      throw gse;
    }
  } // public static Set internal_getGroupsWhereSubjectHasPriv(s, m, f)

  /**
   * @since   1.2.0
   * @param s
   * @param m
   * @param f
   * @return the set
   * @throws StemNotFoundException
   */
  public static Set internal_getStemsWhereSubjectHasPriv(GrouperSession s, final Member m, final Field f)
    throws  StemNotFoundException
  {
    try {
      return (Set)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          Set         mships  = new LinkedHashSet();
          Membership  ms;
          // Perform query as ROOT to prevent privilege constraints getting in the way
          Iterator    it      = MembershipFinder.internal_findMemberships( grouperSession.internal_getRootSession(), 
              m, f ).iterator();
          while (it.hasNext()) {
            ms = (Membership) it.next();
            try {
              mships.add( ms.getStem() );
            } catch (StemNotFoundException snfe) {
              throw new GrouperSessionException(snfe);
            }
          }
          return mships;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof StemNotFoundException) {
        throw (StemNotFoundException)gse.getCause();
      }
      throw gse;
      
    }
  } // public static Set internal_getStemsWhereSubjectHasPriv(s, m, f)

} // class GrouperPrivilegeAdapter

