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
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * Basic Hibernate <code>Member</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3MemberDAO.java,v 1.9 2008-09-10 05:45:58 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MemberDAO extends Hib3DAO implements MemberDAO {

  private static        GrouperCache<String, Boolean>    existsCache     =  
    new GrouperCache<String, Boolean>("edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.exists",
          1000, false, 30, 120, false); 
    

  
  
  private static final  String                      KLASS           = Hib3MemberDAO.class.getName();
  //TODO, move this to ehcache?
  private static        GrouperCache<String, Member>  uuid2dtoCache   = 
    new GrouperCache<String, Member>("edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.find",
            1000, false, 30, 120, false);
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
    if ( existsCache.containsKey(uuid) ) {
      return existsCache.get(uuid).booleanValue();
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
    existsCache.put(uuid, rv);
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
   * @since   @HEAD@
   */
  public Member findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    return this.findBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() );
  } 

  /**
   * @since   @HEAD@
   */
  public Member findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException {
    Member memberDto = HibernateSession.byHqlStatic()
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
    if (memberDto == null) {
      throw new MemberNotFoundException();
    }
    uuid2dtoCache.put( memberDto.getUuid(), memberDto );
    return memberDto;
  } 

  /**
   * @since   @HEAD@
   */
  public Member findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    if ( uuid2dtoCache.containsKey(uuid) ) {
      return uuid2dtoCache.get(uuid);
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
    if (memberDto == null) {
      throw new MemberNotFoundException();
    }
    uuid2dtoCache.put(uuid, memberDto);
    return memberDto;
  } 

  /** logger */
  private static final Log LOG = LogFactory.getLog(Hib3MemberDAO.class);

  /**
   * update the exists cache
   * @param uuid
   * @param exists
   */
  public void existsCachePut(String uuid, boolean exists) {
    existsCache.put( uuid, exists );
  }
  
  /**
   * remove from cache
   * @param uuid
   */
  public void uuid2dtoCacheRemove(String uuid) {
    uuid2dtoCache.remove(uuid);
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
    existsCache.clear();
  } 

} 

