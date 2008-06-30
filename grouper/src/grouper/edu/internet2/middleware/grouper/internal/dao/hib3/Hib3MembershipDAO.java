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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipAddMemberBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.util.Quote;

/**
 * Basic Hibernate <code>Membership</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3MembershipDAO.java,v 1.15 2008-06-30 17:40:43 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MembershipDAO extends Hib3DAO implements MembershipDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3MembershipDAO.class.getName();


  /**
   * <p/>
   * @since   @HEAD@ 
   */
  public boolean exists(String ownerUUID, String memberUUID, String listName, String msType)
    throws  GrouperDAOException {
    Object id = HibernateSession.byHqlStatic()
      .createQuery("select ms.id from Membership as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".Exists")
      .setString( "owner",  ownerUUID  )
      .setString( "member", memberUUID )
      .setString( "fname",  listName   )
      .setString( "type",   msType     )
      .uniqueResult(Object.class);
    boolean rv  = false;
    if ( id != null ) {
      rv = true;
    }
    return rv;
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery("from Membership as ms where  "
        + "     ms.createTimeLong > :time             "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
      .setLong(   "time",  d.getTime()            )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ).listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByCreatedBefore(Date d, Field f) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.createTimeLong < :time             "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
      .setLong(   "time",  d.getTime()            )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ).listSet(Membership.class);
    
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByMember(String memberUUID) 
    throws  GrouperDAOException {
    return HibernateSession.byHqlStatic()
      .createQuery("from Membership as ms where ms.memberUuid = :member")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByMember")
      .setString("member", memberUUID)
      .listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByMemberAndVia(String memberUUID, String viaUUID) 
    throws  GrouperDAOException {

    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.viaUuid     = :via             "
      )
      .setString( "member", memberUUID )
      .setString( "via",    viaUUID    ).listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByOwnerAndField(String ownerUUID, Field f) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.ownerUuid   = :owner            "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndField")
      .setString( "owner", ownerUUID                )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ) 
      .listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByOwnerAndFieldAndType(String ownerUUID, Field f, String type) 
    throws  GrouperDAOException {
    return HibernateSession.byHqlStatic()
      .createQuery("from Membership as ms where  "
        + "     ms.ownerUuid   = :owner            "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            "
        + "and  ms.type = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMembershipsByType")
      .setString( "owner" , ownerUUID                 )
      .setString( "fname" , f.getName()             )
      .setString( "ftype" , f.getType().toString()  )
      .setString( "type"  , type                    )
      .listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f) 
    throws  GrouperDAOException {
    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.ownerUuid   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .listSet(Membership.class);
  } 

  /**
   * @see     MembershipDAO#findAllMembersByOwnerAndField(String, Field)
   * @since   @HEAD@
   */
  public Set<Member> findAllMembersByOwnerAndField(String ownerUUID, Field f)
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery(
          "select m"
        + " from Member m, Membership ms where"
        + " ms.ownerUuid      = :owner"
        + " and ms.listName   = :fname"
        + " and ms.listType   = :ftype"
        + " and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembersByOwnerAndField")
      .setString( "owner", ownerUUID ) 
      .setString( "fname", f.getName() )
      .setString( "ftype", f.getType().toString() )
      .listSet(Member.class);
  }

  /**
   * @since   @HEAD@
   */
  public Membership findByOwnerAndMemberAndFieldAndType(String ownerUUID, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException {
    Membership membershipDto = HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByOwnerAndMemberAndFieldAndType")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() ) 
      .setString( "type",   type                   )
      .uniqueResult(Membership.class);
    if (membershipDto == null) {
      throw new MembershipNotFoundException();
    }
    return membershipDto;
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllChildMemberships(Membership _ms) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery("from Membership as ms where ms.parentUuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindChildMemberships")
      .setString( "uuid", _ms.getUuid() )
      .listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffective(String ownerUUID, String memberUUID, Field f, String viaUUID, int depth) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             "
        + "and  ms.viaUuid    = :via              "
        + "and  ms.depth      = :depth            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffective")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .setString( "via",    viaUUID                )
      .setInteger("depth",  depth                  )
      .listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.listName  = :fname             "
        + "and  ms.listType  = :ftype             "
        + "and  ms.type = :type                   ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField")
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f)
    throws  GrouperDAOException {
    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .listSet(Membership.class);
  } 
  
  /**
   * @since   1.2.1
   */
  public Set<Membership> findAllByOwnerAndMember(String ownerUUID, String memberUUID) 
    throws  GrouperDAOException {
    
    //Added by Gary Brown 2007-11-01 so that getPrivs can do one query rather than 6

    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.ownerUuid   = :owner            "  
        + "and  ms.memberUuid  = :member           "
      ).setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .listSet(Membership.class);
  }

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllImmediateByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField")
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.IMMEDIATE   )
      .listSet(Membership.class);
  } 

  /**
   * @since   @HEAD@
   */
  public Membership findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
  {
    Membership membershipDto = HibernateSession.byHqlStatic()
      .createQuery("from Membership as ms where ms.uuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid)
      .uniqueResult(Membership.class);
    if (membershipDto == null) {
      throw new MembershipNotFoundException("could not find membership with uuid: " + Quote.single(uuid));
    }
    return membershipDto;
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findMembershipsByMemberAndField(String memberUUID, Field f)
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery(
        "from Membership as ms where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMemberships")
      .setString( "member", memberUUID             )
      .setString( "fname" , f.getName()            )
      .setString( "ftype" , f.getType().toString() )
      .listSet(Membership.class);
  } 

  /**
   * @param mof 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void update(final DefaultMemberOf mof) 
    throws  GrouperDAOException {
    // TODO 20070404 this is incredibly ugly
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            
            
            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
                MembershipHooks.METHOD_MEMBERSHIP_PRE_ADD_MEMBER,
                HooksMembershipAddMemberBean.class, mof, DefaultMemberOf.class, 
                VetoTypeGrouper.MEMBERSHIP_PRE_ADD_MEMBER);
            
            ByObject byObject = hibernateSession.byObject();
            byObject.delete(mof.getDeletes());

            byObject.saveOrUpdate(mof.getSaves());

            byObject.saveOrUpdate(mof.getModifiedGroups());
            
            byObject.saveOrUpdate(mof.getModifiedStems());

            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
                MembershipHooks.METHOD_MEMBERSHIP_POST_ADD_MEMBER,
                HooksMembershipAddMemberBean.class, mof, DefaultMemberOf.class, 
                VetoTypeGrouper.MEMBERSHIP_POST_ADD_MEMBER);
            
            return null;
          }
      
    });
  } 


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    Session hs = hibernateSession.getSession();
    HibUtils.executeSql("update grouper_memberships set PARENT_MEMBERSHIP = null", null);
    
    List<Membership> memberships = 
      hs.createQuery("from Membership as ms order by createTimeLong desc")
      .list()
      ;

    // Deleting each membership from the time created in descending order. 
    // This is necessary to prevent deleting parent memberships before child 
    // memberships which causes integrity constraint violations on some databases. 
    for (Membership membership : memberships) {
      hs.createQuery("delete from Membership ms where ms.uuid=:uuid")
      .setString("uuid", membership.getUuid())
      .executeUpdate();
    }

  }

} 

