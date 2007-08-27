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

package edu.internet2.middleware.grouper.internal.dao.hibernate;
import  edu.internet2.middleware.grouper.ErrorLog;
import  edu.internet2.middleware.grouper.MemberNotFoundException;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  edu.internet2.middleware.subject.Subject;
import  java.io.Serializable;
import  java.util.HashMap;
import  net.sf.hibernate.*;

/**
 * Basic Hibernate <code>Member</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: HibernateMemberDAO.java,v 1.5 2007-08-27 17:49:26 blair Exp $
 * @since   1.2.0
 */
public class HibernateMemberDAO extends HibernateDAO implements Lifecycle,MemberDAO {


  private static        HashMap<String, Boolean>    existsCache     = new HashMap<String, Boolean>();
  private               String                      id;
  private static final  String                      KLASS           = HibernateMemberDAO.class.getName();
  private static        HashMap<String, MemberDTO>  uuid2dtoCache   = new HashMap<String, MemberDTO>();
  private               String                      subjectID;
  private               String                      subjectSourceID;
  private               String                      subjectTypeID;
  private               String                      uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public String create(MemberDTO _m) 
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) Rosetta.getDAO(_m);
      try {
        hs.save(dao);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public boolean exists(String uuid) 
    throws  GrouperDAOException
  {
    if ( existsCache.containsKey(uuid) ) {
      return existsCache.get(uuid).booleanValue();
    }
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("select m.id from HibernateMemberDAO as m where m.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".Exists");
      qry.setString("uuid", uuid);
      boolean rv  = false;
      if ( qry.uniqueResult() != null ) {
        rv = true; 
      }
      hs.close();
      existsCache.put(uuid, rv);
      return rv;
    }
    catch (HibernateException eH) {
      ErrorLog.fatal( HibernateMemberDAO.class, eH.getMessage() );
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public MemberDTO findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    return this.findBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() );
  } 

  /**
   * @since   1.2.0
   */
  public MemberDTO findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateMemberDAO as m where "
        + "     m.subjectId       = :sid    "  
        + "and  m.subjectSourceId = :source "
        + "and  m.subjectTypeId   = :type"
      );
      qry.setCacheable(false); // but i probably should - or at least permit it
      //qry.setCacheRegion(KLASS + ".FindBySubject");
      qry.setString( "sid",    id   );
      qry.setString( "type",   type );
      qry.setString( "source", src  );
      HibernateMemberDAO dao = (HibernateMemberDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new MemberNotFoundException();
      }
      MemberDTO _m = new MemberDTO()
        .setId( dao.getId() )
        .setUuid( dao.getUuid() )
        .setSubjectId( dao.getSubjectId() )
        .setSubjectSourceId( dao.getSubjectSourceId() )
        .setSubjectTypeId( dao.getSubjectTypeId() );
      uuid2dtoCache.put( _m.getUuid(), _m );
      return _m;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public MemberDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    if ( uuid2dtoCache.containsKey(uuid) ) {
      return uuid2dtoCache.get(uuid);
    }
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateMemberDAO as m where m.uuid = :uuid");
      qry.setCacheable(false); // but i probably should - or at least permit it
      //qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateMemberDAO dao = (HibernateMemberDAO) qry.uniqueResult(); 
      hs.close();
      if (dao == null) {
        throw new MemberNotFoundException();
      }
      MemberDTO _m = new MemberDTO()
        .setId( dao.getId() )
        .setUuid( dao.getUuid() )
        .setSubjectId( dao.getSubjectId() )
        .setSubjectSourceId( dao.getSubjectSourceId() )
        .setSubjectTypeId( dao.getSubjectTypeId() );
      uuid2dtoCache.put(uuid, _m);
      return _m;
    }
    catch (HibernateException eH) {
      ErrorLog.fatal( HibernateMemberDAO.class, eH.getMessage() );
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /** 
   * @since   1.2.0
   */
  public String getId() {
    return this.id;
  } 

  /** 
   * @since   1.2.0
   */
  public String getSubjectId() {
    return this.subjectID;
  }

  /**
   * @since   1.2.0
   */
  public String getSubjectSourceId() {
    return this.subjectSourceID;
  }

  /**
   * @since   1.2.0
   */
  public String getSubjectTypeId() {
    return this.subjectTypeID;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  // @since   1.2.0 
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getUuid(), false );
    uuid2dtoCache.remove( this.getUuid() );
    return Lifecycle.NO_VETO;
  } 

  // @since   1.2.0
  public void onLoad(Session hs, Serializable id) {
    // nothing
  }

  // @since   1.2.0
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getUuid(), true );
    return Lifecycle.NO_VETO;
  } 

  // @since   1.2.0
  public boolean onUpdate(Session hs) 
    throws  CallbackException
  {
    // nothing
    return Lifecycle.NO_VETO;
  } // public boolean onUpdate(hs)k


  /** 
   * @since   1.2.0
   */
  public MemberDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public MemberDAO setSubjectId(String subjectID) {
    this.subjectID = subjectID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public MemberDAO setSubjectSourceId(String subjectSourceID) {
    this.subjectSourceID = subjectSourceID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public MemberDAO setSubjectTypeId(String subjectTypeID) {
    this.subjectTypeID = subjectTypeID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public MemberDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public void update(MemberDTO _m) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update( _m.getDAO() );
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateMemberDAO as m where m.subjectId != 'GrouperSystem'");
    existsCache = new HashMap<String, Boolean>();
  } 

} 

