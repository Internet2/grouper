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
import  edu.internet2.middleware.subject.Subject;
import  java.io.Serializable;
import  java.util.HashMap;
import  java.util.Map;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Member} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateMemberDAO.java,v 1.11 2007-02-22 20:12:43 blair Exp $
 * @since   1.2.0
 */
class HibernateMemberDAO extends HibernateDAO implements Lifecycle {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateMemberDAO.class.getName();


  // PRIVATE CLASS VARIABLES //
  private static SimpleBooleanCache existsCache = new SimpleBooleanCache();


  // HIBERNATE PROPERTIES //
  private String  id;
  private String  memberUUID;
  private String  subjectID;
  private String  subjectSourceID;
  private String  subjectTypeID;


  // PUBLIC INSTANCE METHODS //

  // @since   1.2.0 
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getMemberUuid(), true );
    return Lifecycle.NO_VETO;
  } // public boolean onDelete(hs)

  // @since   1.2.0
  public void onLoad(Session hs, Serializable id) {
    // nothing
  } // public void onLoad(hs, id)

  // @since   1.2.0
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getMemberUuid(), true );
    return Lifecycle.NO_VETO;
  } // public boolean onSave(hs)

  // @since   1.2.0
  public boolean onUpdate(Session hs) 
    throws  CallbackException
  {
    // nothing
    return Lifecycle.NO_VETO;
  } // public boolean onUpdate(hs)k



  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static String create(MemberDTO dto) 
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = Rosetta.getDAO(dto);
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
  } // protected static String create(dto)

  // @since   1.2.0
  protected static boolean exists(String uuid) 
    throws  GrouperDAOException
  {
    if ( existsCache.containsKey(uuid) ) {
      return existsCache.get(uuid).booleanValue();
    }
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("select m.id from HibernateMemberDAO as m where m.memberUuid = :uuid");
      qry.setCacheable(true);
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
  } // protected static boolean exists(uuid)

  // @since   1.2.0
  protected static MemberDTO findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    return findBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() );
  } // protected static MemberDTO findBySubject(subj)

  // @since   1.2.0
  protected static MemberDTO findBySubject(String id, String src, String type) 
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
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindBySubject");
      qry.setString( "sid",    id   );
      qry.setString( "type",   type );
      qry.setString( "source", src  );
      HibernateMemberDAO dao = (HibernateMemberDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new MemberNotFoundException();
      }
      MemberDTO dto = new MemberDTO();
      dto.setId( dao.getId() );
      dto.setMemberUuid( dao.getMemberUuid() );
      dto.setSubjectId( dao.getSubjectId() );
      dto.setSubjectSourceId( dao.getSubjectSourceId() );
      dto.setSubjectTypeId( dao.getSubjectTypeId() );
      return dto;
    }
    catch (HibernateException eH) {
      String msg = E.MEMBER_NEITHER_FOUND_NOR_CREATED + eH.getMessage();
      ErrorLog.fatal(HibernateMemberDAO.class, msg);
      throw new GrouperDAOException(msg, eH);
    }
  } // protected static MemberDTO findBySubject(id, src, type)

  // @since   1.2.0
  protected static MemberDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateMemberDAO as m where m.memberUuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateMemberDAO dao = (HibernateMemberDAO) qry.uniqueResult(); 
      hs.close();
      if (dao == null) {
        throw new MemberNotFoundException();
      }
      MemberDTO dto = new MemberDTO();
      dto.setId( dao.getId() );
      dto.setMemberUuid( dao.getMemberUuid() );
      dto.setSubjectId( dao.getSubjectId() );
      dto.setSubjectSourceId( dao.getSubjectSourceId() );
      dto.setSubjectTypeId( dao.getSubjectTypeId() );
      return dto;
    }
    catch (HibernateException eH) {
      ErrorLog.fatal( HibernateMemberDAO.class, eH.getMessage() );
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static MemberDTO findByUuid(uuid)

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateMemberDAO as m where m.subjectId != 'GrouperSystem'");
    existsCache.removeAll(); 
  } // protected static void reset(hs)

  // @since   1.2.0
  protected static void update(MemberDTO dto) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update( dto.getDAO() );
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
  } // protected static void update(dto)


  // GETTERS //
  protected String getId() {
    return this.id;
  } 
  protected String getMemberUuid() {
    return this.memberUUID;
  }
  protected String getSubjectId() {
    return this.subjectID;
  }
  protected String getSubjectSourceId() {
    return this.subjectSourceID;
  }
  protected String getSubjectTypeId() {
    return this.subjectTypeID;
  }


  // SETTERS //
  protected void setId(String id) {
    this.id = id;
  }
  protected void setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
  }
  protected void setSubjectId(String subjectID) {
    this.subjectID = subjectID;
  }
  protected void setSubjectSourceId(String subjectSourceID) {
    this.subjectSourceID = subjectSourceID;
  }
  protected void setSubjectTypeId(String subjectTypeID) {
    this.subjectTypeID = subjectTypeID;
  }

} // class HibernateMemberDAO extends HibernateDAO implements Lifecycle

