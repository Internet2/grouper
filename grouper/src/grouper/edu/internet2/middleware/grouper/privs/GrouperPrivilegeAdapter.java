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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/** 
 * @author  blair christensen.
 * @version $Id: GrouperPrivilegeAdapter.java,v 1.12 2009-09-28 05:06:46 mchyzer Exp $
 * @since   1.1.0
 */
public class GrouperPrivilegeAdapter {

	  /**
	   * convert a set of privileges to a set of fields
	   * @param priv2list 
	   * @param privileges
	   * @return the set of fields
	   */
	  public static Set<Field> fieldSet(@SuppressWarnings("unused") Map<Privilege, String> priv2list, Set<Privilege> privileges) {
	    if (privileges == null) {
	      return null;
	    }
	    try {
  	    Set<Field> fields = new LinkedHashSet<Field>();
  	    for (Privilege privilege : privileges) {
  	      Field field = privilege.getField();
  	      fields.add(field);
  	    }
  	    return fields;
	    } catch (SchemaException se) {
	      throw new RuntimeException("Problem: " + se.getMessage(), se);
	    }
	  }

    /**
     * convert a set of privileges to a set of fields
     * @param priv2list 
     * @param privileges
     * @return the set of fields
     */
    public static Set<String> fieldNameSet(Map<Privilege, String> priv2list, Set<Privilege> privileges) {
      if (privileges == null) {
        return null;
      }
      Set<String> fieldNames = new LinkedHashSet<String>();
      Set<Field> fieldSet = fieldSet(priv2list, privileges);
      
      for (Field field : fieldSet) {
        fieldNames.add(field.getName());
      }
      return fieldNames;
    }

    /**
     * convert a set of privileges to a set of fields
     * @param priv2list 
     * @param privileges
     * @return the set of fields
     */
    public static Set<String> fieldIdSet(Map<Privilege, String> priv2list, Set<Privilege> privileges) {
      if (privileges == null) {
        return null;
      }
      Set<String> fieldNames = new LinkedHashSet<String>();
      Set<Field> fieldSet = fieldSet(priv2list, privileges);
      
      for (Field field : fieldSet) {
        fieldNames.add(field.getUuid());
      }
      return fieldNames;
    }

  /**
   * 2007-11-02 Gary Brown
   * If p==null determine by looking at the Membership list
   * Discard those which are not privileges i.e. members / custom lists
   * Added Owner to signature so we don't need to compute it 
   * consequently all Memberships must be of the same Owner
   * @param s
   * @param ownerGroupOrStemOrAttributeDef
   * @param subj
   * @param m
   * @param p
   * @param it
   * @return the set
   * @throws SchemaException
   */
  public static Set<? extends GrouperPrivilege> internal_getPrivs(
    GrouperSession s, final Owner ownerGroupOrStemOrAttributeDef,final Subject subj, 
    final Member m, final Privilege p, final Iterator it
  )
    throws  SchemaException  {
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
            String listName = ms.getList().getName();
            localP=AccessPrivilege.listToPriv(listName);
            if (localP == null) {
              localP = AttributeDefPrivilege.listToPriv(listName);
            }
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
          if ( ms.getViaGroupId() != null ) {
            try {
              Group viaGroup = ms.getViaGroup();
              if (LOG.isDebugEnabled()) {
                //temporary log message to try privilege
                LOG.debug("finding group subject: " + viaGroup.getName());
              }
              owner   = viaGroup.toSubject();
              revoke  = false;
            }
            catch (GroupNotFoundException eGNF) {
              LOG.error(eGNF.getMessage() );
            }
          }
          
            if (Privilege.isAccess(localP))  {
              privs.add(
                new AccessPrivilege((Group)ownerGroupOrStemOrAttributeDef, subj, owner, localP, grouperSession.getAccessClass(), revoke, ms.getContextId())
              );
            }
            else if (Privilege.isNaming(localP)){
              privs.add(
                new NamingPrivilege( (Stem)ownerGroupOrStemOrAttributeDef, subj, owner, localP, grouperSession.getNamingClass(), revoke, ms.getContextId() )
              );
            } else if (Privilege.isAttributeDef(localP)){
                privs.add(
                  new AttributeDefPrivilege( (AttributeDef)ownerGroupOrStemOrAttributeDef, subj, owner, localP, grouperSession.getNamingClass(), revoke, ms.getContextId() )
                );
            } else {
              throw new RuntimeException("Cant find type of privilege: " + localP);
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
  public static Set<Group> internal_getGroupsWhereSubjectHasPriv(GrouperSession s, final Member m, final Field f)
    throws  GroupNotFoundException
  {
    try {
      return (Set<Group>)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          Privilege privilege = AccessPrivilege.listToPriv(f.getName());
          
          Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure(
              grouperSession, m.getSubject(), GrouperUtil.toSet(privilege), null);
          
          return groups;
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
   * @param grouperSession 
   * @param stemId 
   * @param scope 
   * @param subject 
   * @param privilege 
   * @param considerAllSubject
   * @return the set of groups
   */
  public static Set<Group> internal_getGroupsWhereSubjectDoesntHavePriv(GrouperSession grouperSession, 
      final String stemId, final Scope scope, 
      final Subject subject, final Privilege privilege, final boolean considerAllSubject) {
    return (Set<Group>)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findGroupsInStemWithoutPrivilege(
            grouperSession, stemId, scope, subject, privilege, null, considerAllSubject);
        
        return groups;
      }
      
    });
  }

  /**
   * @param grouperSession 
   * @param stemId 
   * @param scope 
   * @param subject 
   * @param privilege 
   * @param considerAllSubject
   * @return the set of stems
   */
  public static Set<Stem> internal_getStemsWhereSubjectDoesntHavePriv(GrouperSession grouperSession, 
      final String stemId, final Scope scope, 
      final Subject subject, final Privilege privilege, final boolean considerAllSubject) {
    return (Set<Stem>)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        Set<Stem> stems = GrouperDAOFactory.getFactory().getStem().findStemsInStemWithoutPrivilege(
            grouperSession, stemId, scope, subject, privilege, null, considerAllSubject);
        
        return stems;
      }
      
    });
  }

  /**
   * @param grouperSession 
   * @param stemId 
   * @param scope 
   * @param subject 
   * @param privilege 
   * @param considerAllSubject
   * @return the set of attributeDefs
   */
  public static Set<AttributeDef> internal_getAttributeDefsWhereSubjectDoesntHavePriv(GrouperSession grouperSession, 
      final String stemId, final Scope scope, 
      final Subject subject, final Privilege privilege, final boolean considerAllSubject) {
    return (Set<AttributeDef>)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        Set<AttributeDef> attributeDefs = GrouperDAOFactory.getFactory().getAttributeDef().findAttributeDefsInStemWithoutPrivilege(
            grouperSession, stemId, scope, subject, privilege, null, considerAllSubject);
        
        return attributeDefs;
      }
      
    });
  }

  /**
   * @param grouperSession 
   * @param member 
   * @param field 
   * @since   1.2.0
   * @return the set
   * @throws GroupNotFoundException
   */
  public static Set<Stem> internal_getStemsWithGroupsWhereSubjectHasPriv(
      GrouperSession grouperSession, final Member member, final Field field) {

    Privilege privilege = AccessPrivilege.listToPriv(field.getName());
    
    Set<Stem> stems = GrouperDAOFactory.getFactory().getStem().getAllStemsWithGroupsSecure(
        grouperSession, member.getSubject(), GrouperUtil.toSet(privilege), null);
    
    return stems;
  } // public static Set internal_getGroupsWhereSubjectHasPriv(s, m, f)

  /**
   * @since   1.2.0
   * @param s
   * @param m
   * @param f
   * @return the set
   * @throws StemNotFoundException
   */
  public static Set<Stem> internal_getStemsWhereSubjectHasPriv(GrouperSession s, final Member m, final Field f)
    throws  StemNotFoundException
  {
    try {
      return (Set<Stem>)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          Privilege privilege = NamingPrivilege.listToPriv(f.getName());
          
          Set<Stem>         stems  = GrouperDAOFactory.getFactory().getStem().getAllStemsSecure(
              grouperSession, m.getSubject(), GrouperUtil.toSet(privilege), null);

          return stems;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof StemNotFoundException) {
        throw (StemNotFoundException)gse.getCause();
      }
      throw gse;
      
    }
  } // public static Set internal_getStemsWhereSubjectHasPriv(s, m, f)

  /**
   * @since   1.2.0
   * @param s
   * @param m
   * @param f
   * @return the set
   * @throws GroupNotFoundException
   */
  public static Set<AttributeDef> internal_getAttributeDefsWhereSubjectHasPriv(GrouperSession s, final Member m, final Field f)
    throws  GroupNotFoundException {
    try {
      return (Set<AttributeDef>)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          Privilege privilege = AttributeDefPrivilege.listToPriv(f.getName());
          
          Set<AttributeDef>         attributeDefs  = GrouperDAOFactory.getFactory().getAttributeDef().getAllAttributeDefsSecure(
              grouperSession, m.getSubject(), GrouperUtil.toSet(privilege), null);
          return attributeDefs;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof GroupNotFoundException) {
        throw (GroupNotFoundException)gse.getCause();
      }
      throw gse;
    }
  } // public static Set internal_getGroupsWhereSubjectHasPriv(s, m, f)

} // class GrouperPrivilegeAdapter

