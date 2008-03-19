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
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.CallbackException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import edu.internet2.middleware.grouper.ErrorLog;
import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.grouper.internal.util.Rosetta;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * Basic Hibernate <code>Member</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3MemberDAO.java,v 1.4.2.1 2008-03-19 18:46:10 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MemberDAO extends Hib3HibernateVersioned implements Lifecycle,MemberDAO {


  private static        HashMap<String, Boolean>    existsCache     = new HashMap<String, Boolean>();
  private static final  String                      KLASS           = Hib3MemberDAO.class.getName();
  private static        HashMap<String, MemberDTO>  uuid2dtoCache   = new HashMap<String, MemberDTO>();
  private               String                      subjectID;
  private               String                      subjectSourceID;
  private               String                      subjectTypeID;
  private               String                      uuid;
  /**
   * @since   @HEAD@
   */
  public long create(MemberDTO _m) 
    throws  GrouperDAOException {

    Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_m);
    HibernateSession.byObjectStatic().save(dao);
    return ((MemberDAO)dao).getHibernateVersion();
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
        .createQuery("select m.id from Hib3MemberDAO as m where m.uuid = :uuid")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".Exists")
        .setString("uuid", uuid).uniqueResult(Object.class);
    } catch (GrouperDAOException gde) {
      Throwable throwable = gde.getCause();
      //CH 20080218 this was legacy error handling
      if (throwable instanceof HibernateException) {
        ErrorLog.fatal( Hib3MemberDAO.class, throwable.getMessage() );
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
  public Set findAll(Source source) 
    throws  GrouperDAOException
  {
    Set members = new LinkedHashSet();   
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    if (source == null) {
      byHqlStatic.createQuery("from Hib3MemberDAO");
    } else {
      byHqlStatic.createQuery("from Hib3MemberDAO as m where m.subjectSourceId=:sourceId");
      byHqlStatic.setString("sourceId", source.getId());
    }
    List<Hib3MemberDAO> memberDAOs = byHqlStatic.list(Hib3MemberDAO.class);  
    for(Hib3MemberDAO memberDAO : memberDAOs) {
      members.add(new MemberDTO()
        .setHibernateVersion(memberDAO.getHibernateVersion())
        .setUuid(memberDAO.getUuid())
        .setSubjectId(memberDAO.getSubjectId())
        .setSubjectSourceId(memberDAO.getSubjectSourceId())
        .setSubjectTypeId(memberDAO.getSubjectTypeId()));
    }     
    return members;
  } // public findAll(Source source)

  /**
   * @since   @HEAD@
   */
  public MemberDTO findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    return this.findBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() );
  } 

  /**
   * @since   @HEAD@
   */
  public MemberDTO findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException {
    Hib3MemberDAO dao = HibernateSession.byHqlStatic()
      .createQuery("from Hib3MemberDAO as m where "
        + "     m.subjectId       = :sid    "  
        + "and  m.subjectSourceId = :source "
        + "and  m.subjectTypeId   = :type")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindBySubject")
        .setString( "sid",    id   )
        .setString( "type",   type )
        .setString( "source", src  )
        .uniqueResult(Hib3MemberDAO.class);
    if (dao == null) {
      throw new MemberNotFoundException();
    }
    MemberDTO _m = new MemberDTO()
      .setHibernateVersion(dao.getHibernateVersion())
      .setUuid( dao.getUuid() )
      .setSubjectId( dao.getSubjectId() )
      .setSubjectSourceId( dao.getSubjectSourceId() )
      .setSubjectTypeId( dao.getSubjectTypeId() );
    uuid2dtoCache.put( _m.getUuid(), _m );
    return _m;
  } 

  /**
   * @since   @HEAD@
   */
  public MemberDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    if ( uuid2dtoCache.containsKey(uuid) ) {
      return uuid2dtoCache.get(uuid);
    }
    Hib3MemberDAO dao = null;
    
    try {
      dao = HibernateSession.byHqlStatic()
      .createQuery("from Hib3MemberDAO as m where m.uuid = :uuid")
      .setCacheable(false) // but i probably should - or at least permit it
      //.setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid).uniqueResult(Hib3MemberDAO.class);
    } catch (GrouperDAOException gde) {
      Throwable throwable = gde.getCause();
      //CH 20080218 this was legacy error handling
      if (throwable instanceof HibernateException) {
        ErrorLog.fatal( Hib3MemberDAO.class, throwable.getMessage() );
      }
      throw gde;
    }
    if (dao == null) {
      throw new MemberNotFoundException();
    }
    MemberDTO _m = new MemberDTO()
      .setUuid( dao.getUuid() )
      .setSubjectId( dao.getSubjectId() )
      .setSubjectSourceId( dao.getSubjectSourceId() )
      .setSubjectTypeId( dao.getSubjectTypeId() );
    uuid2dtoCache.put(uuid, _m);
    return _m;
  } 

  /** 
   * @since   @HEAD@
   */
  public String getSubjectId() {
    return this.subjectID;
  }

  /**
   * @since   @HEAD@
   */
  public String getSubjectSourceId() {
    return this.subjectSourceID;
  }

  /**
   * @since   @HEAD@
   */
  public String getSubjectTypeId() {
    return this.subjectTypeID;
  }

  /**
   * @since   @HEAD@
   */
  public String getUuid() {
    return this.uuid;
  }

  // @since   @HEAD@ 
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getUuid(), false );
    uuid2dtoCache.remove( this.getUuid() );
    return Lifecycle.NO_VETO;
  } 

  // @since   @HEAD@
  public void onLoad(Session hs, Serializable id) {
    // nothing
  }

  // @since   @HEAD@
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getUuid(), true );
    return Lifecycle.NO_VETO;
  } 

  // @since   @HEAD@
  public boolean onUpdate(Session hs) 
    throws  CallbackException
  {
    // nothing
    return Lifecycle.NO_VETO;
  } // public boolean onUpdate(hs)k


  /**
   * @since   @HEAD@
   */
  public MemberDAO setSubjectId(String subjectID) {
    this.subjectID = subjectID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MemberDAO setSubjectSourceId(String subjectSourceID) {
    this.subjectSourceID = subjectSourceID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MemberDAO setSubjectTypeId(String subjectTypeID) {
    this.subjectTypeID = subjectTypeID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MemberDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public void update(MemberDTO _m) 
    throws  GrouperDAOException {
    
    HibernateSession.byObjectStatic().update(_m.getDAO());
    
  } 


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.createQuery("delete from Hib3MemberDAO as m where m.subjectId != :subject")
      .setParameter( "subject", "GrouperSystem" )
      .executeUpdate()
      ;
    existsCache = new HashMap<String, Boolean>();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO#getId()
   */
  @Override
  protected String getId() {
    return this.uuid;
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.hib3.Hib3HibernateVersioned#setHibernateVersion(long)
   */
  @Override
  public Hib3MemberDAO setHibernateVersion(long hibernateVersion) {
    return (Hib3MemberDAO)super.setHibernateVersion(hibernateVersion);
  }
} 

