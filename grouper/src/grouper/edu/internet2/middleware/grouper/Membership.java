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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * A list membership in the Groups Registry.
 * 
 * A membership is the object which represents a join of member
 * and group.  Has metadata like type and creator,
 * and, if an effective membership, the parent membership
 * 
 * <p/>
 * @author  blair christensen.
 * @version $Id: Membership.java,v 1.104 2008-09-10 05:45:59 mchyzer Exp $
 */
public class Membership extends GrouperAPI implements Hib3GrouperVersioned {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** table name where memberships are stored */
  public static final String TABLE_GROUPER_MEMBERSHIPS = "grouper_memberships";
  
  /** id col in db */
  public static final String COLUMN_ID = "id";

  /** id col in db */
  public static final String COLUMN_FIELD_ID = "field_id";

  /** list_name col in db */
  public static final String COLUMN_LIST_NAME = "list_name";

  /** list_type col in db */
  public static final String COLUMN_LIST_TYPE = "list_type";

  /** old_list_name col in db */
  public static final String COLUMN_OLD_LIST_NAME = "old_list_name";

  /** old_list_type col in db */
  public static final String COLUMN_OLD_LIST_TYPE = "old_list_type";

  /** uuid col in db */
  public static final String COLUMN_MEMBERSHIP_UUID = "membership_uuid";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_MEMBERSHIP_UUID = "old_membership_uuid";
  

  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: createTimeLong */
  public static final String FIELD_CREATE_TIME_LONG = "createTimeLong";

  /** constant for field name for: creatorUUID */
  public static final String FIELD_CREATOR_UUID = "creatorUUID";

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

  /** constant for field name for: memberUUID */
  public static final String FIELD_MEMBER_UUID = "memberUUID";

  /** constant for field name for: ownerUUID */
  public static final String FIELD_OWNER_UUID = "ownerUUID";

  /** constant for field name for: parentUUID */
  public static final String FIELD_PARENT_UUID = "parentUUID";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /** constant for field name for: viaUUID */
  public static final String FIELD_VIA_UUID = "viaUUID";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME_LONG, FIELD_CREATOR_UUID, FIELD_DEPTH, FIELD_FIELD_ID, 
      FIELD_MEMBER_UUID, FIELD_OWNER_UUID, 
      FIELD_PARENT_UUID, FIELD_TYPE, FIELD_UUID, FIELD_VIA_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME_LONG, FIELD_CREATOR_UUID, FIELD_DB_VERSION, FIELD_DEPTH, 
      FIELD_FIELD_ID, FIELD_HIBERNATE_VERSION_NUMBER,
      FIELD_MEMBER_UUID, FIELD_OWNER_UUID, FIELD_PARENT_UUID, FIELD_TYPE, 
      FIELD_UUID, FIELD_VIA_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  
  /** A member of a group (aka composite member) has either or both of
   * an immediate (direct) membership, or an effective (indirect) membership **/
  public static final String COMPOSITE = "composite";
  
  /** 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   */
  public static final String EFFECTIVE = "effective";
  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   */
  public static final String IMMEDIATE = "immediate";


  // PRIVATE CLASS CONSTANTS //
  private static final EventLog EL = new EventLog();
  
  // Cache groups and stems
  public  static final  String            CACHE_GET_GROUP = Membership.class.getName() + ".getGroup";
  private static        EhcacheController cc= new EhcacheController();
  public  static final  String            CACHE_GET_STEM = Membership.class.getName() + ".getStem";
 
  
  private long    createTimeLong  = new Date().getTime();           // reasonable default

  private String  creatorUUID;

  private int     depth       = 0;                              // reasonable default

  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Member  member;

  private String  memberUUID;

  private String  ownerUUID;

  private String  parentUUID  = null;                           // reasonable default

  /** either composite, immediate, effective */
  private String  type        = Membership.IMMEDIATE;           // reasonable default

  /**
   * if this is a composite membership
   * @return true if composite
   */
  public boolean isComposite() {
    return StringUtils.equals(this.type, Membership.COMPOSITE);
  }
  
  /**
   * if this is a immediate membership
   * @return true if immediate
   */
  public boolean isImmediate() {
    return StringUtils.equals(this.type, Membership.IMMEDIATE);
  }
  
  /**
   * if this is a effective membership
   * @return true if effective
   */
  public boolean isEffective() {
    return StringUtils.equals(this.type, Membership.EFFECTIVE);
  }
  
  private String  uuid        = GrouperUuid.getUuid(); // reasonable default

  private String  viaUUID     = null;                           // reasonable default

  /**
   * id of the field which is the list name and type
   */
  private String fieldId;

  /** 
   * Get child memberships of this membership. 
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg"> 
   * Set children = ms.getChildMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   */
  public Set getChildMemberships() {
    // Ideally I would use a Hibernate mapping for this, but...
    //   * It wasn't working and I didn't have time to debug it at the time.
    //   * I still need to filter
    return PrivilegeHelper.canViewMemberships(
      GrouperSession.staticGrouperSession(), GrouperDAOFactory.getFactory().getMembership().findAllChildMemberships( this )
    );
  } // public Set getChildMemberships()

  /**
   * @since   1.2.0
   */
  public Date getCreateTime() {
    return new Date( this.getCreateTimeLong() );
  } // public Date getCreateTime()

  /**
   * @since   1.2.0
   */
  public Member getCreator() 
    throws  MemberNotFoundException
  {
    try {
      Member m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getCreatorUuid() );
      return m;
    }
    catch (GrouperDAOException eDAO) {
      throw new GrouperRuntimeException( eDAO.getMessage(), eDAO );
    }
  } // public Member getCreator()

  /**
   */
  public int getDepth() {
    return this.depth;
  } // public int getDepth()
   
  /**
   * Get this membership's group.
   * <pre class="eg">
   * Group g = ms.getGroup();
   * </pre>
   * @return  A {@link Group}
   */
  public Group getGroup() 
    throws  GroupNotFoundException
  {
    String uuid = this.getOwnerUuid();
    if (uuid == null) {
      throw new GroupNotFoundException();
    }
	Group g = getGroupFromCache(uuid);
	if(g !=null) return g;
    
    g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid) ;
    putGroupInCache(g);
    return g;
  } // public Group getGroup()

  /**
   * Get this membership's list.
   * <pre class="eg">
   * String list = g.getList();
   * </pre>
   * @return  The {@link Field} type of this membership.
   */
  public Field getList() {
    try {
      return FieldFinder.find( this.getListName() );
    }
    catch (SchemaException eS) {
      throw new GrouperRuntimeException( eS.getMessage(), eS );
    }
  } // public Field getList()

  /**
   * Get this membership's member.
   * 
   * All immediate subjects, and effective members are members.  
   * No duplicates will be returned (e.g. if immediate and effective).
   * 
   * <pre class="eg">
   * Member m = ms.getMember();
   * </pre>
   * @return  A {@link Member}
   * @throws  MemberNotFoundException
   */
  public Member getMember() 
    throws MemberNotFoundException
  {
	if(member !=null) {
	  return member;
	}

    String uuid = this.getMemberUuid();
    if (uuid == null) {
      throw new MemberNotFoundException("membership does not have a member!");
    }
    member = GrouperDAOFactory.getFactory().getMember().findByUuid(uuid) ;
    return member;
  } // public Member getMember()

  /**
   * Get parent membership of this membership.
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * try {
   *   Membership parent = ms.getParentMembership();
   * }
   * catch (MembershipNotFoundException e) {
   *   // Unable to retrieve parent membership
   * }
   * </pre>
   * @return  A {@link Membership}
   * @throws  MembershipNotFoundException
   */
  public Membership getParentMembership() 
    throws MembershipNotFoundException
  {
    String uuid = this.getParentUuid();
    if (uuid == null) {
      throw new MembershipNotFoundException("no parent");
    }
    Membership parent = GrouperDAOFactory.getFactory().getMembership().findByUuid(uuid) ;
    return parent;
  } // public Membership getParentMembership()

  /** 
   * @since   1.2.0
   */
  public Stem getStem() 
    throws StemNotFoundException
  {
    String uuid = this.getOwnerUuid();
    if (uuid == null) {
      throw new StemNotFoundException("membership stem not found");
    }
    Stem ns = getStemFromCache(uuid);
	if(ns != null) return ns;
    
     ns = GrouperDAOFactory.getFactory().getStem().findByUuid(uuid) ;
    putStemInCache(ns);
    return ns;
  } // public Stem getStem()

  /**
   * @since   1.2.0
   */
  public String getType() {
    return this.type;
  } 

  /**
   */
  public String getUuid() {
    return this.uuid;
  } 

  /**
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * @since   1.2.0
   */
  public Composite getViaComposite() 
    throws  CompositeNotFoundException
  {
    String uuid = this.getViaUuid();
    if (uuid == null) {
      throw new CompositeNotFoundException();
    }
    Composite via = GrouperDAOFactory.getFactory().getComposite().findByUuid(uuid) ;
    return via;
  } // public Composite getViaComposite()

  /**
   * Get this membership's via group.
   * <p>{@link Group}s with {@link Composite} memberships will <b>not</b> have a
   * via group.  Use the {@link #getViaComposite() getViaComposite()} method instead.</p>
   * <pre class="eg">
   * try {
   *   Group via = ms.getViaGroup();
   * }
   * catch (GroupNotFoundException e) {
   *   // Unable to retrieve via group
   * }
   * </pre>
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public Group getViaGroup() 
    throws GroupNotFoundException
  {
    String uuid = this.getViaUuid();
    if (uuid == null) {
      throw new GroupNotFoundException();
    }
    Group via = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid);
    return via;
  } // public Group getViaGroup()

  /**
   * wrapper to run hooks on default member of add member
   * @param mof
   */
  private static void internal_insertPersistDefaultMemberOf(final DefaultMemberOf mof) {

    GrouperTransaction.callbackGrouperTransaction(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {

          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
                MembershipHooks.METHOD_MEMBERSHIP_PRE_ADD_MEMBER,
                HooksMembershipChangeBean.class, mof, DefaultMemberOf.class, 
                VetoTypeGrouper.MEMBERSHIP_PRE_ADD_MEMBER);
            
            GrouperDAOFactory.getFactory().getMembership().update(mof);

            GrouperHooksUtils.schedulePostCommitHooksIfRegistered(mof, GrouperHookType.MEMBERSHIP, 
                MembershipHooks.METHOD_MEMBERSHIP_POST_COMMIT_ADD_MEMBER, HooksMembershipChangeBean.class, 
                mof, DefaultMemberOf.class);

            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
                MembershipHooks.METHOD_MEMBERSHIP_POST_ADD_MEMBER,
                HooksMembershipChangeBean.class, mof, DefaultMemberOf.class, 
                VetoTypeGrouper.MEMBERSHIP_POST_ADD_MEMBER);
            return null;
          }
          
        });


  }
  
  // @since   1.2.0
  public static void internal_addImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f
  )
    throws  MemberAddException
  {
    try {
      GrouperSession.validate(s);
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj);
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.addImmediate( s, g, f, m );
      internal_insertPersistDefaultMemberOf(mof);
      EL.addEffMembers( s, g, subj, f, mof.getEffectiveSaves() );
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (IllegalStateException eIS)           {
      throw new MemberAddException( eIS.getMessage(), eIS );
    }    
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberAddException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberAddException( eMNF.getMessage(), eMNF );
    }
  } // public static void internal_addImmediateMembership(s, g, subj, f)

  // @since   1.2.0
  public static void internal_addImmediateMembership(
    GrouperSession s, Stem ns, Subject subj, Field f
  )
    throws  MemberAddException
  {
    try {
      GrouperSession.validate(s);
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj);
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.addImmediate( s, ns, f, m );
      internal_insertPersistDefaultMemberOf(mof);
      EL.addEffMembers( s, ns, subj, f, mof.getEffectiveSaves() );
    }
    catch (IllegalStateException eIS)           {
      throw new MemberAddException( eIS.getMessage(), eIS );
    }    
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberAddException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberAddException( eMNF.getMessage(), eMNF );
    }
  } // public static void internal_addImmediateMembership(s, ns, subj, f)

  // @since   1.2.0
  public static DefaultMemberOf internal_delImmediateMembership(GrouperSession s, Group g, Subject subj, Field f)
    throws  MemberDeleteException
  {
    try {
      GrouperSession.validate(s); 
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj);
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.deleteImmediate(
        s, g, 
        GrouperDAOFactory.getFactory().getMembership().findByOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, IMMEDIATE
        ), 
        m
      );
      return mof;
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberDeleteException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberDeleteException( eMNF.getMessage(), eMNF );
    }
    catch (MembershipNotFoundException eMSNF)   {
      throw new MemberDeleteException(eMSNF.getMessage(), eMSNF);
    }
  } // public static void internal_delImmediateMembership(s, g, subj, f)

  // @since   1.2.0
  public static DefaultMemberOf internal_delImmediateMembership(GrouperSession s, Stem ns, Subject subj, Field f)
    throws  MemberDeleteException
  {
    try {
      GrouperSession.validate(s); 
      // Who we're deleting
      //Member m = PrivilegeResolver.internal_canViewSubject(s, subj);
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj);
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.deleteImmediate(
        s, ns,
        GrouperDAOFactory.getFactory().getMembership().findByOwnerAndMemberAndFieldAndType( 
          ns.getUuid(), m.getUuid(), f, IMMEDIATE 
        ), 
        m
      );
      return mof;
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberDeleteException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberDeleteException( eMNF.getMessage(), eMNF );
    }
    catch (MembershipNotFoundException eMSNF)   {
      throw new MemberDeleteException(eMSNF.getMessage(), eMSNF);
    }
  } // public static void internal_delImmediateMembership(s, ns, subj, f)

  /**
   * @since   1.2.0
   * @param s
   * @param g
   * @param f
   * @return the set
   * @throws MemberDeleteException
   * @throws SchemaException
   */
  public static Set internal_deleteAllField(GrouperSession s, final Group g, final Field f)
    throws  MemberDeleteException,
            SchemaException
  {
    GrouperSession.validate(s);
    try {
      return (Set)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
  
            Set           deletes = new LinkedHashSet();
            DefaultMemberOf      mof;
            Membership    ms;
            MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();
  
            // Deal with where group is a member
            Iterator itIs = g.toMember().getImmediateMemberships(f).iterator();
            while (itIs.hasNext()) {
              ms   = (Membership) itIs.next();
              mof  = new DefaultMemberOf();
              mof.deleteImmediate(
                grouperSession, ms.getGroup(),
                dao.findByOwnerAndMemberAndFieldAndType( 
                  ms.getGroup().getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE
                ),
                 ms.getMember()
              );
              deletes.addAll( mof.getDeletes() );
            }
  
            // Deal with group's members
            Iterator itHas = dao.findAllByOwnerAndFieldAndType( g.getUuid(), f, IMMEDIATE ).iterator();
            while (itHas.hasNext()) {
              ms = (Membership)itHas.next() ;
              mof = new DefaultMemberOf();
              mof.deleteImmediate(
                grouperSession, g,
                dao.findByOwnerAndMemberAndFieldAndType(
                  g.getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE
                ), ms.getMember()
              );
              deletes.addAll( mof.getDeletes() );
            }
  
            return deletes;
          } catch (SchemaException se) {
            throw new GrouperSessionException(se);
          } catch (GroupNotFoundException eGNF) {
            throw new GrouperSessionException(new MemberDeleteException( eGNF.getMessage(), eGNF ));
          } catch (MemberNotFoundException eMNF) {
            throw new GrouperSessionException(new MemberDeleteException(eMNF));
          } catch (MembershipNotFoundException eMSNF) {
            throw new GrouperSessionException(new MemberDeleteException( eMSNF.getMessage(), eMSNF ));
          }
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause()  instanceof MemberDeleteException) {
        throw (MemberDeleteException)gse.getCause();
      }
      if (gse.getCause()  instanceof SchemaException) {
        throw (SchemaException)gse.getCause();
      }
      throw gse;
    }
  } // public static Set internal_deleteAllField(s, g, f)

  /**
   * @since   1.2.0
   * @param s
   * @param ns
   * @param f
   * @return the set
   * @throws MemberDeleteException
   */
  public static Set internal_deleteAllField(GrouperSession s, final Stem ns, final Field f)
    throws  MemberDeleteException
  {
    GrouperSession.validate(s);
    return (Set)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {

          Set           deletes = new LinkedHashSet();
          DefaultMemberOf      mof;
          Membership    ms;
          MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();

          // Deal with stem's members
          Iterator itHas = dao.findAllByOwnerAndFieldAndType( ns.getUuid(), f, IMMEDIATE ).iterator();
          while (itHas.hasNext()) {
            ms = (Membership) itHas.next() ;
            mof = new DefaultMemberOf();
            mof.deleteImmediate(
              grouperSession, ns,
              dao.findByOwnerAndMemberAndFieldAndType(
                ns.getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE
              ), ms.getMember());
            deletes.addAll( mof.getDeletes() );
          }

          return deletes;
        }
        catch (MemberNotFoundException eMNF) {
          throw new GrouperSessionException(new MemberDeleteException( eMNF.getMessage(), eMNF ));
        }
        catch (MembershipNotFoundException eMSNF) {
          throw new GrouperSessionException(new MemberDeleteException( eMSNF.getMessage(), eMSNF ));
        }
      }
      
    });
  } // public static Set internal_deleteAllField(s, ns, f)

  // @since   1.2.0
  public static Set internal_deleteAllFieldType(GrouperSession s, Group g, FieldType type) 
    throws  MemberDeleteException,
            SchemaException
  {
    GrouperSession.validate(s);
    Set       deletes = new LinkedHashSet();
    Field     f;
    Iterator  it      = FieldFinder.findAllByType(type).iterator();
    while (it.hasNext()) {
      f = (Field) it.next();
      deletes.addAll( internal_deleteAllField(s, g, f) );
    }
    return deletes;
  } // public static Set internal_deleteAllFieldType(s, g, f)

  // @since   1.2.0
  public static Set internal_deleteAllFieldType(GrouperSession s, Stem ns, FieldType type) 
    throws  MemberDeleteException,
            SchemaException
  {
    GrouperSession.validate(s);
    Set       deletes = new LinkedHashSet();
    Field     f;
    Iterator  it      = FieldFinder.findAllByType(type).iterator();
    while (it.hasNext()) {
      f = (Field) it.next();
      deletes.addAll( internal_deleteAllField(s, ns, f) );
    }
    return deletes;
  } // public static Set internal_deleteAllFieldType(s, ns, f)

  //@since   1.3.0
  private Group getGroupFromCache(String uuid) {
	  Element el = cc.getCache(CACHE_GET_GROUP).get(uuid);
	    if (el != null) {
	      return (Group) el.getObjectValue();
	    }
	    return null;
  } 
  
  //@since   1.3.0
  private Stem getStemFromCache(String uuid) {
	  Element el = cc.getCache(CACHE_GET_STEM).get(uuid);
	    if (el != null) {
	      return (Stem) el.getObjectValue();
	    }
	    return null;
  } 
  
  //@since   1.3.0
  private void putGroupInCache(Group g) {
	  cc.getCache(CACHE_GET_GROUP).put( new Element( g.getUuid(),g) );
  }
  
  //@since   1.3.0
  private void putStemInCache(Stem stem) {
	  cc.getCache(CACHE_GET_STEM).put( new Element( stem.getUuid(),stem) );
  }

  // PUBLIC INSTANCE METHODS //
  
  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Membership)) {
      return false;
    }
    Membership that = (Membership) other;
    return new EqualsBuilder()
      .append( this.getDepth(),      that.getDepth()      )
      .append( this.getListName(),   that.getListName()   )
      .append( this.getListType(),   that.getListType()   )
      .append( this.getMemberUuid(), that.getMemberUuid() )
      .append( this.getOwnerUuid(),  that.getOwnerUuid()  )
      .append( this.getViaUuid(),    that.getViaUuid()    )
      .append( this.getParentUuid(),    that.getParentUuid()    )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public long getCreateTimeLong() {
    return this.createTimeLong;
  }

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getListName() {
    Field field = this.getField();
    return field == null ? null : field.getName();
  }
  
  /**
   * get the field based on field id (if there is one there)
   * @return the field or null if not there
   */
  private Field getField() {
    if (StringUtils.isBlank(this.fieldId)) {
      return null;
    }
    Field field = FieldFinder.findById(this.fieldId);
    return field;
  }

  /**
   * @since   1.2.0
   */
  public String getListType() {
    Field field = this.getField();
    return field == null ? null : field.getTypeString();
  }

  /**
   * @since   1.2.0
   */
  public String getMemberUuid() {
    return this.memberUUID;
  }

  /**
   * @since   1.2.0
   */
  
  public String getOwnerUuid() {
    return this.ownerUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUUID;
  
  }

  /**
   * @since   1.2.0
   */
  public String getViaUuid() {
    return this.viaUUID;
  }

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getDepth()      )
      .append( this.getListName()   )
      .append( this.getListType()   )
      .append( this.getMemberUuid() )
      .append( this.getOwnerUuid()  )
      .append( this.getViaUuid()    )
      .append( this.getParentUuid()    )
      .toHashCode();
  } // public int hashCode()

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_PRE_INSERT, HooksMembershipBean.class, 
        this, Membership.class, VetoTypeGrouper.MEMBERSHIP_PRE_INSERT, false, false);

  }

  /**
   * @since   1.2.0
   */
  public void setCreateTimeLong(long createTime) {
    this.createTimeLong = createTime;
  
  }

  /**
   * @since   1.2.0
   */
  public void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  
  }

  /**
   * @since   1.2.0
   */
  public void setDepth(int depth) {
    this.depth = depth;
  
  }
  
  /**
   * @since   1.3.0
   */
  
  public void setMember(Member member) {
    this.member = member;
  
  }

  public void setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
  
  }

  /**
   * @since   1.2.0
   */
  public void setOwnerUuid(String ownerUUID) {
    this.ownerUUID = ownerUUID;
  
  }

  /**
   * @since   1.2.0
   */
  public void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
  
  }

  /**
   * @since   1.2.0
   */
  public void setType(String type) {
    this.type = type;
  
  }

  /**
   * @since   1.2.0
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  
  }

  /**
   * @since   1.2.0
   */
  public void setViaUuid(String viaUUID) {
    this.viaUUID = viaUUID;
  
  }

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "createTime",  this.getCreateTimeLong()  )
      .append( "creatorUuid", this.getCreatorUuid() )
      .append( "depth",       this.getDepth()       )
      .append( "listName",    this.getListName()    )
      .append( "listType",    this.getListType()    )
      .append( "memberUuid",  this.getMemberUuid()  )
      .append( "ownerUuid",   this.getOwnerUuid()   )
      .append( "parentUuid",  this.getParentUuid()  )
      .append( "type",        this.getType()        )
      .append( "uuid",        this.getUuid()        )
      .append( "viaUuid",     this.getViaUuid()     )
      .toString();
  } // public String toString()

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {

    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_POST_COMMIT_DELETE, HooksMembershipBean.class, 
        this, Membership.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_POST_DELETE, HooksMembershipBean.class, 
        this, Membership.class, VetoTypeGrouper.MEMBERSHIP_POST_DELETE, false, true);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {

    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_POST_COMMIT_INSERT, HooksMembershipBean.class, 
        this, Membership.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_POST_INSERT, HooksMembershipBean.class, 
        this, Membership.class, VetoTypeGrouper.MEMBERSHIP_POST_INSERT, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {

    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_POST_COMMIT_UPDATE, HooksMembershipBean.class, 
        this, Membership.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_POST_UPDATE, HooksMembershipBean.class, 
        this, Membership.class, VetoTypeGrouper.MEMBERSHIP_POST_UPDATE, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_PRE_DELETE, HooksMembershipBean.class, 
        this, Membership.class, VetoTypeGrouper.MEMBERSHIP_PRE_DELETE, false, false);

  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_PRE_UPDATE, HooksMembershipBean.class, 
        this, Membership.class, VetoTypeGrouper.MEMBERSHIP_PRE_UPDATE, false, false);
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public Membership dbVersion() {
    return (Membership)this.dbVersion;
  }

  /**
   * note, these are massaged so that name, extension, etc look like normal fields.
   * access with fieldValue()
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public Membership clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * id of the field which is the list name and type
   * @return the field id
   */
  public String getFieldId() {
    return fieldId;
  }

  /**
   * id of the field which is the list name and type
   * @param fieldId1
   */
  public void setFieldId(String fieldId1) {
    this.fieldId = fieldId1;
  }

  
}
