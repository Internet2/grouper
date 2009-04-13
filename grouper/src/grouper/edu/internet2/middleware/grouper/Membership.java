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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
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
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteAlreadyDeletedException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
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
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
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
 * @version $Id: Membership.java,v 1.119 2009-04-13 20:24:29 mchyzer Exp $
 */
public class Membership extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned {

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

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";

  /** constant for field name for: parentUUID */
  public static final String FIELD_PARENT_UUID = "parentUUID";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /** constant for field name for: viaCompositeId */
  public static final String FIELD_VIA_COMPOSITE_ID = "viaCompositeId";

  /** constant for field name for: viaGroupId */
  public static final String FIELD_VIA_GROUP_ID = "viaGroupId";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME_LONG, FIELD_CREATOR_UUID, FIELD_DEPTH, FIELD_FIELD_ID, 
      FIELD_MEMBER_UUID, FIELD_OWNER_GROUP_ID, FIELD_OWNER_STEM_ID, FIELD_PARENT_UUID, 
      FIELD_TYPE, FIELD_UUID, FIELD_VIA_COMPOSITE_ID, FIELD_VIA_GROUP_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME_LONG, FIELD_CREATOR_UUID, FIELD_DB_VERSION, FIELD_DEPTH, 
      FIELD_FIELD_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_MEMBER_UUID, FIELD_OWNER_GROUP_ID, 
      FIELD_OWNER_STEM_ID, FIELD_PARENT_UUID, FIELD_TYPE, FIELD_UUID, 
      FIELD_VIA_COMPOSITE_ID, FIELD_VIA_GROUP_ID);

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
   * get the name of the owner (group or stem)
   * @return the name
   */
  public String getOwnerName() {
    try {
      if (!StringUtils.isBlank(this.ownerGroupId)) {
        Group owner = this.getGroup();
        return owner.getName();
      }
      if (!StringUtils.isBlank(this.ownerStemId)) {
        Stem owner = this.getStem();
        return owner.getName();
      }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    } catch (StemNotFoundException snfe) {
      throw new RuntimeException(snfe);
    }
    return null;
  }
  
  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   */
  public static final String IMMEDIATE = "immediate";


  /** */
  private static final EventLog EL = new EventLog();
  
  /** */
  public  static final  String            CACHE_GET_GROUP = Membership.class.getName() + ".getGroup";
  /** */
  private static        EhcacheController cc= new EhcacheController();
  /** */
  public  static final  String            CACHE_GET_STEM = Membership.class.getName() + ".getStem";
 
  
  /** */
  private long    createTimeLong  = new Date().getTime();           // reasonable default

  /** */
  private String  creatorUUID;

  /** */
  private int     depth       = 0;                              // reasonable default

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Member  member;

  /** */
  private String  memberUUID;

  /** 
   * if group membership, this is the group id 
   */
  private String ownerGroupId;

  /** 
   * if stem membership, this is the stem id 
   */
  private String ownerStemId;
  
  /** */
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
  
  /** */
  private String  uuid        = GrouperUuid.getUuid(); // reasonable default

  /** */
  private String  viaGroupId     = null; 

  /** */
  private String  viaCompositeId     = null; 

  /**
   * id of the field which is the list name and type
   */
  private String fieldId;

  /**
   * 
   */
  public static final String COLUMN_VIA_ID_BAK = "via_id_bak";

  /**
   * 
   */
  public static final String COLUMN_OWNER_ID_BAK = "owner_id_bak";

  /**
   * 
   */
  public static final String COLUMN_VIA_COMPOSITE_ID = "via_composite_id";

  /**
   * 
   */
  public static final String COLUMN_VIA_GROUP_ID = "via_group_id";

  /**
   * 
   */
  public static final String COLUMN_OWNER_STEM_ID = "owner_stem_id";

  /**
   * 
   */
  public static final String COLUMN_OWNER_GROUP_ID = "owner_group_id";

  /**
   * 
   */
  public static final String COLUMN_OWNER_ID = "owner_id";

  /**
   * 
   */
  public static final String COLUMN_VIA_ID = "via_id";

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
   * @return create time
   * @since   1.2.0
   */
  public Date getCreateTime() {
    return new Date( this.getCreateTimeLong() );
  } // public Date getCreateTime()

  /**
   * @return creator
   * @throws MemberNotFoundException 
   * @since   1.2.0
   */
  public Member getCreator() 
    throws  MemberNotFoundException
  {
    try {
      Member m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getCreatorUuid(), true );
      return m;
    }
    catch (GrouperDAOException eDAO) {
      throw new GrouperException( eDAO.getMessage(), eDAO );
    }
  } // public Member getCreator()

  /**
   * number of hops between this membership and direct membership
   * @return depth
   */
  public int getDepth() {
    return this.depth;
  }
   
  /**
   * Get this membership's group.  To get the groups of a bunch of membership, might want to try
   * retrieveGroups()
   * <pre class="eg">
   * Group g = ms.getGroup();
   * </pre>
   * @return  A {@link Group}
   * @throws GroupNotFoundException if group not found
   */
  public Group getGroup() throws GroupNotFoundException {
    String uuid = this.getOwnerGroupId();
    if (uuid == null) {
      throw new GroupNotFoundException();
    }
    Group g = getGroupFromCache(uuid);
    if (g != null) {
      return g;
    }
    g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid, true);
    putGroupInCache(g);
    return g;
  }
  /**
   * retrieve a set of groups based on some memberships (and store in each membership, like getGroup
   * @param memberships
   * @return the set of groups
   */
  public static Set<Group> retrieveGroups(Collection<Membership> memberships) {
    try {
      
      //first lets see which uuids are not in cache
      Set<String> uuidsNotInCache = new HashSet<String>();
      
      //TODO update for 1.5 with group owner
      for (Membership membership : memberships) {
        
        String uuid = membership.getOwnerGroupId();
        if (uuid == null) {
          throw new GroupNotFoundException("Group uuid is null! " + membership.getUuid());
        }
        
        if (membership.getGroupFromCache(uuid) == null) {
          uuidsNotInCache.add(uuid);
        }
        
      }
      
      //now lets get all those groups including attributes
      Set<Group> groupsFromDb = GrouperDAOFactory.getFactory().getGroup().findByUuids(uuidsNotInCache, false);
      
      Set<Group> groups = new LinkedHashSet<Group>();
      
      //now we have everything we need
      for (Membership membership : memberships) {
        
        String uuid = membership.getOwnerGroupId();
        
        Group group = membership.getGroupFromCache(uuid);
        if (group == null) {
          group = GrouperUtil.retrieveByProperty(groupsFromDb, Group.FIELD_UUID, uuid);
          if (group == null && !FieldType.NAMING.equals(membership.getField().getType())) {
            group = membership.getGroup();
          } else {
            if (group != null) {
              //add to local cache
              membership.putGroupInCache(group);
            }
          }
        }
        if (group != null) {
          groups.add(group);
        }
        
      }
      return groups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem", gnfe);
    }
  }

  /**
   * Get this membership's list.
   * <pre class="eg">
   * String list = g.getList();
   * </pre>
   * @return  The {@link Field} type of this membership.
   */
  public Field getList() {
    return FieldFinder.find( this.getListName(), true );
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
    member = GrouperDAOFactory.getFactory().getMember().findByUuid(uuid, true) ;
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
    Membership parent = GrouperDAOFactory.getFactory().getMembership().findByUuid(uuid, true) ;
    return parent;
  } // public Membership getParentMembership()

  /** 
   * @return stem
   * @throws StemNotFoundException 
   * @since   1.2.0
   */
  public Stem getStem() 
    throws StemNotFoundException
  {
    String uuid = this.getOwnerStemId();
    if (uuid == null) {
      throw new StemNotFoundException("membership stem not found");
    }
    Stem ns = getStemFromCache(uuid);
	if(ns != null) return ns;
    
     ns = GrouperDAOFactory.getFactory().getStem().findByUuid(uuid, true) ;
    putStemInCache(ns);
    return ns;
  } // public Stem getStem()

  /**
   * @return type effective, immediate, composite
   * @since   1.2.0
   */
  public String getType() {
    return this.type;
  } 

  /**
   * @return uuid
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
   * @return composite
   * @throws CompositeNotFoundException 
   * 
   * @since   1.2.0
   */
  public Composite getViaComposite() 
    throws  CompositeNotFoundException
  {
    String uuid = this.getViaCompositeId();
    if (uuid == null) {
      throw new CompositeNotFoundException();
    }
    Composite via = GrouperDAOFactory.getFactory().getComposite().findByUuid(uuid, true) ;
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
    String uuid = this.getViaGroupId();
    if (uuid == null) {
      throw new GroupNotFoundException();
    }
    Group via = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid, true);
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

            GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
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

  /**
   * 
   * @param s
   * @param g
   * @param subj
   * @param f
   * @throws MemberAddException
   * @return the membership if available
   */
  public static Membership internal_addImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f) throws  MemberAddException {
    
    String errorString = "membership: group: " + (g == null ? null : g.getName())
      + ", subject: " + (subj  == null ? null : subj.getId())
      + ", field: " + (f == null ? null : f.getName());
    try {
      GrouperSession.validate(s);
      Member    m   = MemberFinder.internal_findReadableMemberBySubject(s, subj, true);
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.addImmediate( s, g, f, m );
      internal_insertPersistDefaultMemberOf(mof);
      EL.addEffMembers( s, g, subj, f, mof.getEffectiveSaves() );
      EL.delEffMembers( s, g, subj, f, mof.getEffectiveDeletes() );
      return mof.getMembership();
    } catch (HookVeto hookVeto) {
      //just throw, this is ok
      throw hookVeto;
    } catch (IllegalStateException eIS) {
      if (eIS instanceof MembershipAlreadyExistsException) {
        throw new MemberAddAlreadyExistsException(eIS.getMessage() + ", " + errorString, eIS);
      }
      throw new MemberAddException( eIS.getMessage() + ", " + errorString, eIS );
    }    
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberAddException(eIP.getMessage() + ", " + errorString, eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberAddException( eMNF.getMessage() + ", " + errorString, eMNF );
    }
  } 

  /**
   * 
   * @param s
   * @param ns
   * @param subj
   * @param f
   * @throws MemberAddException
   */
  public static void internal_addImmediateMembership(
    GrouperSession s, Stem ns, Subject subj, Field f)
    throws  MemberAddException  {
    try {
      GrouperSession.validate(s);
      Member    m   = MemberFinder.internal_findReadableMemberBySubject(s, subj, true);
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.addImmediate( s, ns, f, m );
      internal_insertPersistDefaultMemberOf(mof);
      EL.addEffMembers( s, ns, subj, f, mof.getEffectiveSaves() );
    } catch (MembershipAlreadyExistsException eIS) {
      throw new MemberAddAlreadyExistsException( eIS.getMessage(), eIS );
    } catch (IllegalStateException eIS) {
      throw new MemberAddException( eIS.getMessage(), eIS );
    }    
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberAddException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberAddException( eMNF.getMessage(), eMNF );
    }
  }

  /**
   * 
   * @param s
   * @param g
   * @param subj
   * @param f
   * @return default member of
   * @throws MemberDeleteException
   */
  public static DefaultMemberOf internal_delImmediateMembership(GrouperSession s, Group g, Subject subj, Field f)
    throws  MemberDeleteException {
    try {
      GrouperSession.validate(s); 
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj, true);
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.deleteImmediate(
        s, g, 
        GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, IMMEDIATE, true
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
      throw new MemberDeleteAlreadyDeletedException(eMSNF.getMessage(), eMSNF);
    }
  }

  /**
   * 
   * @param s
   * @param ns
   * @param subj
   * @param f
   * @return default member of
   * @throws MemberDeleteException
   */
  public static DefaultMemberOf internal_delImmediateMembership(GrouperSession s, Stem ns, Subject subj, Field f)
    throws  MemberDeleteException
  {
    try {
      GrouperSession.validate(s); 
      // Who we're deleting
      //Member m = PrivilegeResolver.internal_canViewSubject(s, subj);
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj, true);
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.deleteImmediate(
        s, ns,
        GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType( 
          ns.getUuid(), m.getUuid(), f, IMMEDIATE , true
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
      throw new MemberDeleteAlreadyDeletedException(eMSNF.getMessage(), eMSNF);
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
  public static Set<GrouperAPI> internal_deleteAllField(final GrouperSession s, final Group g, final Field f)
    throws  MemberDeleteException,
            SchemaException
  {
    GrouperSession.validate(s);
    try {
      return (Set<GrouperAPI>)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          try {          
            Set           deletes = new LinkedHashSet();
            Membership    ms;
            MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();
  
            // Deal with where group is a member
            Iterator itIs = g.toMember().getImmediateMemberships(f).iterator();
            while (itIs.hasNext()) {
              ms   = (Membership) itIs.next();
              DefaultMemberOf mof  = new DefaultMemberOf();
              mof.deleteImmediate(
                s, ms.getGroup(),
                dao.findByGroupOwnerAndMemberAndFieldAndType( 
                  ms.getGroup().getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE, true
                ),
                 ms.getMember()
              );
              GrouperDAOFactory.getFactory().getMembership().update(mof);
              deletes.addAll( mof.getDeletes() );
            }
  
            // Deal with group's members
            Iterator itHas = dao.findAllByGroupOwnerAndFieldAndType( g.getUuid(), f, IMMEDIATE ).iterator();
            while (itHas.hasNext()) {
              ms = (Membership)itHas.next() ;
              DefaultMemberOf mof = new DefaultMemberOf();
              mof.deleteImmediate(
                s, g,
                dao.findByGroupOwnerAndMemberAndFieldAndType(
                  g.getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE, true
                ), ms.getMember()
              );
              GrouperDAOFactory.getFactory().getMembership().update(mof);
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
  public static Set<GrouperAPI>  internal_deleteAllField(final GrouperSession s, final Stem ns, final Field f)
    throws  MemberDeleteException
  {
    GrouperSession.validate(s);
    return (Set<GrouperAPI>)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        try {

          Set<GrouperAPI>            deletes = new LinkedHashSet<GrouperAPI> ();
          Membership    ms;
          MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();

          // Deal with stem's members
          Iterator itHas = dao.findAllByStemOwnerAndFieldAndType( ns.getUuid(), f, IMMEDIATE ).iterator();
          while (itHas.hasNext()) {
            ms = (Membership) itHas.next() ;
            DefaultMemberOf mof = new DefaultMemberOf();
            mof.deleteImmediate(
              s, ns,
              dao.findByStemOwnerAndMemberAndFieldAndType(
                ns.getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE, true
              ), ms.getMember());
            GrouperDAOFactory.getFactory().getMembership().update(mof);
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
  }

  /**
   * 
   * @param s
   * @param g
   * @param type
   * @return set
   * @throws MemberDeleteException
   * @throws SchemaException
   */
  public static Set<GrouperAPI>  internal_deleteAllFieldType(GrouperSession s, Group g, FieldType type) 
    throws  MemberDeleteException,
            SchemaException  {
    GrouperSession.validate(s);
    Set<GrouperAPI>       deletes = new LinkedHashSet<GrouperAPI> ();
    Field     f;
    Iterator  it      = FieldFinder.findAllByType(type).iterator();
    while (it.hasNext()) {
      f = (Field) it.next();
      deletes.addAll( internal_deleteAllField(s, g, f) );
    }
    return deletes;
  }

  /**
   * 
   * @param s
   * @param ns
   * @param type
   * @return set of grouper api
   * @throws MemberDeleteException
   * @throws SchemaException
   */
  public static Set<GrouperAPI>  internal_deleteAllFieldType(GrouperSession s, Stem ns, FieldType type) 
    throws  MemberDeleteException,
            SchemaException {
    GrouperSession.validate(s);
    Set       deletes = new LinkedHashSet();
    Field     f;
    Iterator  it      = FieldFinder.findAllByType(type).iterator();
    while (it.hasNext()) {
      f = (Field) it.next();
      deletes.addAll( internal_deleteAllField(s, ns, f) );
    }
    return deletes;
  }

  /**
   * 
   * @param uuid
   * @return group
   */
  private Group getGroupFromCache(String uuid) {
	  Element el = cc.getCache(CACHE_GET_GROUP).get(uuid);
	    if (el != null) {
	      return (Group) el.getObjectValue();
	    }
	    return null;
  } 

  /**
   * 
   * @param uuid
   * @return stem
   */
  private Stem getStemFromCache(String uuid) {
	  Element el = cc.getCache(CACHE_GET_STEM).get(uuid);
	    if (el != null) {
	      return (Stem) el.getObjectValue();
	    }
	    return null;
  } 

  /**
   * 
   * @param g
   */
  private void putGroupInCache(Group g) {
	  cc.getCache(CACHE_GET_GROUP).put( new Element( g.getUuid(),g) );
  }

  /**
   * 
   * @param stem
   */
  private void putStemInCache(Stem stem) {
	  cc.getCache(CACHE_GET_STEM).put( new Element( stem.getUuid(),stem) );
  }


  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
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
      .append( this.fieldId,   that.fieldId   )
      .append( this.memberUUID, that.memberUUID )
      .append( this.ownerGroupId,  that.ownerGroupId  )
      .append( this.ownerStemId,  that.ownerStemId  )
      .append( this.viaGroupId,    that.viaGroupId    )
      .append( this.viaCompositeId,    that.viaCompositeId    )
      .append( this.parentUUID,    that.parentUUID    )
      .isEquals();
  } 

  /**
   * 
   * @return create time
   */
  public long getCreateTimeLong() {
    return this.createTimeLong;
  }

  /**
   * 
   * @return creator uuid
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * 
   * @return list name
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
    Field field = FieldFinder.findById(this.fieldId, true);
    return field;
  }

  /**
   * 
   * @return list type
   */
  public String getListType() {
    Field field = this.getField();
    return field == null ? null : field.getTypeString();
  }

  /**
   * @return member uuid
   * @since   1.2.0
   */
  public String getMemberUuid() {
    return this.memberUUID;
  }

  /**
   * @return parent uuid
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUUID;
  
  }

  /**
   * @return hash code
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.fieldId   )
      .append( this.memberUUID )
      .append( this.ownerGroupId  )
      .append( this.ownerStemId  )
      .append( this.viaGroupId    )
      .append( this.viaCompositeId    )
      .append( this.parentUUID    )
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
   * @param createTime 
   * @since   1.2.0
   */
  public void setCreateTimeLong(long createTime) {
    this.createTimeLong = createTime;
  
  }

  /**
   * @param creatorUUID 
   * @since   1.2.0
   */
  public void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  
  }

  /**
   * @param depth 
   * @since   1.2.0
   */
  public void setDepth(int depth) {
    this.depth = depth;
  
  }
  
  /**
   * @param member 
   * @since   1.3.0
   */
  
  public void setMember(Member member) {
    this.member = member;
  
  }

  /**
   * @param memberUUID
   */
  public void setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
  
  }

  /**
   * @param parentUUID 
   * @since   1.2.0
   */
  public void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
  
  }

  /**
   * @param type 
   * @since   1.2.0
   */
  public void setType(String type) {
    this.type = type;
  
  }

  /**
   * @param uuid 
   * @since   1.2.0
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  
  }

  /**
   * if effective, this is group it is in
   * @return group id
   */
  public String getViaGroupId() {
    return this.viaGroupId;
  }

  /**
   * if effective, this is group it is in
   * @param viaGroupId
   */
  public void setViaGroupId(String viaGroupId) {
    this.viaGroupId = viaGroupId;
  }

  /**
   * if composite, this is composite id
   * @return composite id
   */
  public String getViaCompositeId() {
    return this.viaCompositeId;
  }

  /**
   * if composite, this is composite id
   * @param viaCompositeId
   */
  public void setViaCompositeId(String viaCompositeId) {
    this.viaCompositeId = viaCompositeId;
  }

  /**
   * @return string
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
      .append( "groupId",   this.getOwnerGroupId()   )
      .append( "stemId",   this.getOwnerStemId()   )
      .append( "parentUuid",  this.getParentUuid()  )
      .append( "type",        this.getType()        )
      .append( "uuid",        this.getUuid()        )
      .append( "viaGroupId",     this.getViaGroupId()     )
      .append( "viaCompositeId",     this.getViaCompositeId()     )
      .toString();
  } // public String toString()

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {

    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
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
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_POST_INSERT, HooksMembershipBean.class, 
        this, Membership.class, VetoTypeGrouper.MEMBERSHIP_POST_INSERT, true, false);

    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
        MembershipHooks.METHOD_MEMBERSHIP_POST_COMMIT_INSERT, HooksMembershipBean.class, 
        this, Membership.class);

  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {

    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
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

  /**
   * if group membership, this is the group id
   * @return the group id
   */
  public String getOwnerGroupId() {
    return this.ownerGroupId;
  }


  /**
   * if this is a group membership, this is the group id
   * @param groupId1
   */
  public void setOwnerGroupId(String groupId1) {
    this.ownerGroupId = groupId1;
  }


  /**
   * if this is a stem membership, this is the stem id
   * @return stem id
   */
  public String getOwnerStemId() {
    return this.ownerStemId;
  }

  /**
   * if this is a stem membership, this is the stem id
   * @param stemId1
   */
  public void setOwnerStemId(String stemId1) {
    this.ownerStemId = stemId1;
  }

  /** context id of the transaction */
  private String contextId;

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }
  
}
