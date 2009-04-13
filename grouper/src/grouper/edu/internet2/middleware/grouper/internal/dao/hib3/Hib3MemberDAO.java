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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MemberNotUniqueException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * Basic Hibernate <code>Member</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3MemberDAO.java,v 1.15 2009-04-13 20:24:29 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MemberDAO extends Hib3DAO implements MemberDAO {

  private static        GrouperCache<String, Boolean>    existsCache     =  null;
  
  
    

  
  
  private static final  String                      KLASS           = Hib3MemberDAO.class.getName();

  private static        GrouperCache<String, Member>  uuid2dtoCache   = null;
  

  /**
   * @since   @HEAD@
   */
  public void create(Member _m) 
    throws  GrouperDAOException {

    HibernateSession.byObjectStatic().save(_m);
  } 

  /**
   * @since   @HEAD@
   */
  public boolean exists(String uuid) 
    throws  GrouperDAOException
  {
    if ( getExistsCache().containsKey(uuid) ) {
      return getExistsCache().get(uuid).booleanValue();
    }
    Object id = null;
    try {
      id = HibernateSession.byHqlStatic()
        .createQuery("select m.id from Member as m where m.uuid = :uuid")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".Exists")
        .setString("uuid", uuid).uniqueResult(Object.class);
    } catch (GrouperDAOException gde) {
      Throwable throwable = gde.getCause();
      //CH 20080218 this was legacy error handling
      if (throwable instanceof HibernateException) {
        LOG.fatal( throwable.getMessage() );
      }
      throw gde;
    }
    boolean rv  = false;
    if ( id != null ) {
      rv = true; 
    }
    getExistsCache().put(uuid, rv);
    return rv;
  } 
  
  /**
   * @since   @HEAD@
   */
  public Set findAll() 
    throws  GrouperDAOException
  {
    return findAll(null);
  } // public Set findAll()
  
  /**
   * @since   @HEAD@
   */
  public Set<Member> findAll(Source source) 
    throws  GrouperDAOException
  {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    if (source == null) {
      byHqlStatic.createQuery("from Member");
    } else {
      byHqlStatic.createQuery("from Member as m where m.subjectSourceIdDb=:sourceId");
      byHqlStatic.setString("sourceId", source.getId());
    }
    return byHqlStatic.listSet(Member.class);  
  } // public findAll(Source source)

  /**
   * @deprecated use overload
   */
  @Deprecated
  public Member findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException {
    return findBySubject(subj, true);
  } 

  /**
   * 
   */
  public Member findBySubject(Subject subj, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    return this.findBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() );
  }

  /**
   * @deprecated use overload
   */
  @Deprecated
  public Member findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException {
    return findBySubject(id, src, type, true);
  } 

  /**
   * 
   */
  public Member findBySubject(String id, String src, String type, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    Member member = HibernateSession.byHqlStatic()
      .createQuery("from Member as m where "
        + "     m.subjectIdDb       = :sid    "  
        + "and  m.subjectSourceIdDb = :source "
        + "and  m.subjectTypeId   = :type")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindBySubject")
        .setString( "sid",    id   )
        .setString( "type",   type )
        .setString( "source", src  )
        .uniqueResult(Member.class);
    if (member == null) {
      throw new MemberNotFoundException();
    }
    getUuid2dtoCache().put( member.getUuid(), member );
    return member;
  }



  /**
   * @deprecated use overload
   */
  @Deprecated
  public Member findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException  {
    return findByUuid(uuid, true);
  } 

  /**
   * 
   */
  public Member findByUuid(String uuid, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    if ( getUuid2dtoCache().containsKey(uuid) ) {
      return getUuid2dtoCache().get(uuid);
    }
    Member memberDto = null;
    
    try {
      memberDto = HibernateSession.byHqlStatic()
      .createQuery("from Member as m where m.uuid = :uuid")
      .setCacheable(false) // but i probably should - or at least permit it
      //.setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid).uniqueResult(Member.class);
    } catch (GrouperDAOException gde) {
      Throwable throwable = gde.getCause();
      //CH 20080218 this was legacy error handling
      if (throwable instanceof HibernateException) {
        LOG.fatal( throwable.getMessage() );
      }
      throw gde;
    }
    if (memberDto == null && exceptionIfNull) {
      throw new MemberNotFoundException();
    }
    getUuid2dtoCache().put(uuid, memberDto);
    return memberDto;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3MemberDAO.class);

  /**
   * update the exists cache
   * @param uuid
   * @param exists
   */
  public void existsCachePut(String uuid, boolean exists) {
	  getExistsCache().put( uuid, exists );
  }
  
  /**
   * remove from cache
   * @param uuid
   */
  public void uuid2dtoCacheRemove(String uuid) {
	  getUuid2dtoCache().remove(uuid);
  }
  
  /**
   * @since   @HEAD@
   */
  public void update(Member _m) 
    throws  GrouperDAOException {
    
    HibernateSession.byObjectStatic().update(_m);
    
  } 


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    hibernateSession.byHql().createQuery("delete from Member as m where m.subjectIdDb != :subject")
      .setString( "subject", "GrouperSystem" )
      .executeUpdate()
      ;
    getExistsCache().clear();
  }

  /**
   * @deprecated use overload
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findBySubject(java.lang.String)
   */
  @Deprecated
  public Member findBySubject(String subjectId) throws GrouperDAOException,
      MemberNotFoundException, MemberNotUniqueException {
    return findBySubject(subjectId, true);
  }

  /**
   * 
   */
  public Member findBySubject(String subjectId, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException, MemberNotUniqueException {
    List<Member> members = HibernateSession.byHqlStatic().createQuery(
        "from Member as m where m.subjectIdDb       = :sid")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindBySubjectId").setString("sid", subjectId)
        .list(Member.class);
    if (GrouperUtil.length(members) ==0) {
      throw new MemberNotFoundException("Cant find member with subjectId: '" + subjectId + "'");
    }
    if (GrouperUtil.length(members) > 1) {
      throw new MemberNotUniqueException("Subject id '" + subjectId + "' is not unique in the members table");
    }
    Member member = members.get(0);
    getUuid2dtoCache().put(member.getUuid(), member);
    return member;
  }

  /**
   * @deprecated use overload
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findBySubject(java.lang.String, java.lang.String)
   */
  @Deprecated
  public Member findBySubject(String subjectId, String src) throws GrouperDAOException,
      MemberNotFoundException {
    return findBySubject(subjectId, src, true);
  } 

  /**
   * 
   */
  public Member findBySubject(String subjectId, String src, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    Member member = HibernateSession.byHqlStatic().createQuery(
        "from Member as m where " + "     m.subjectIdDb       = :sid    "
            + "and  m.subjectSourceIdDb = :source ").setCacheable(true).setCacheRegion(
        KLASS + ".FindBySubjectIdSrc").setString("sid", subjectId).setString("source",
        src).uniqueResult(Member.class);
    if (member == null && exceptionIfNull) {
      throw new MemberNotFoundException();
    }
    getUuid2dtoCache().put(member.getUuid(), member);
    return member;
  }

  
  private static GrouperCache<String, Boolean> getExistsCache() {
	  if(existsCache==null) {
		  existsCache=new GrouperCache<String, Boolean>("edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.exists",
	          1000, false, 30, 120, false); 
	  }
	  return existsCache;
  }
  
  private static GrouperCache<String, Member> getUuid2dtoCache() {
	  if(uuid2dtoCache==null) {
		  uuid2dtoCache=new GrouperCache<String, Member>("edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.uuid2dtoCache",
	          1000, false, 30, 120, false); 
	  }
	  return uuid2dtoCache;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#_internal_membersIntersection(java.lang.String, java.lang.String)
   */
  public Set<String> _internal_membersIntersection(String groupUuid1, String groupUuid2) {
    Set<String> memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid " +
    		"from Member theMember, " +
        "Membership theMembership, Membership theMembership2 " +
        "where theMembership.ownerGroupId = :group1uuid " +
        "and theMembership2.ownerGroupId = :group2uuid " +
        "and theMember.uuid = theMembership.memberUuid " +
        "and theMember.uuid = theMembership2.memberUuid " +
        "and theMembership.fieldId = :fuuid " +
        "and theMembership2.fieldId = :fuuid ")
        .setString("group1uuid", groupUuid1)
        .setString("group2uuid", groupUuid2)
        .setString("fuuid", Group.getDefaultList().getUuid())
        .listSet(String.class);
    return memberUuids;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#_internal_membersUnion(java.lang.String, java.lang.String)
   */
  public Set<String> _internal_membersUnion(String groupUuid1, String groupUuid2) {
    Set<String> memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid " +
        "from Member theMember, " +
        "Membership theMembership " +
        "where theMembership.ownerGroupId in (:group1uuid, " +
        " :group2uuid) " +
        "and theMember.uuid = theMembership.memberUuid " +
        "and theMembership.fieldId = :fuuid ")
        .setString("group1uuid", groupUuid1)
        .setString("group2uuid", groupUuid2)
        .setString("fuuid", Group.getDefaultList().getUuid())
        .listSet(String.class);
    return memberUuids;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#_internal_membersComplement(java.lang.String, java.lang.String)
   */
  public Set<String> _internal_membersComplement(String groupUuid1, String groupUuid2) {
    Set<String> memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid from Member theMember, " +
        "Membership theMembership " +
        "where theMembership.ownerGroupId = :group1uuid " +
        "and theMember.uuid = theMembership.memberUuid " +
        "and theMembership.fieldId = :fuuid " +
        "and not exists (select theMembership2.memberUuid from Membership theMembership2 " +
        "where theMembership2.memberUuid = theMember.uuid and theMembership2.fieldId = :fuuid " +
        "and theMembership2.ownerUuid = :group2uuid) ")
        .setString("group1uuid", groupUuid1)
        .setString("group2uuid", groupUuid2)
        .setString("fuuid", Group.getDefaultList().getUuid())
        .listSet(String.class);
    return memberUuids;
  }

} 

