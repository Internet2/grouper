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
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.SchemaException;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import  edu.internet2.middleware.grouper.internal.dto.FieldDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  java.io.Serializable;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  net.sf.hibernate.*;

import  org.apache.commons.lang.builder.*;

/** 
 * Schema specification for a Group type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateGroupTypeDAO.java,v 1.3 2007-04-18 15:56:59 blair Exp $
 */
public class HibernateGroupTypeDAO extends HibernateDAO implements GroupTypeDAO, Lifecycle {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateGroupTypeDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  creatorUUID;
  private long    createTime;
  private boolean isAssignable  = true;
  private boolean isInternal    = false;
  private String  id;
  private String  name;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public String create(GroupTypeDTO _gt)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) Rosetta.getDAO(_gt);
      // TODO 20070403 DRY w/ the other DAO classes
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
  public String createField(FieldDTO _f)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) Rosetta.getDAO(_f);
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
  public void delete(GroupTypeDTO _gt, Set fields)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        // delete fields
        Iterator it = fields.iterator();
        while (it.hasNext()) {
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        // delete grouptype
        hs.delete( Rosetta.getDAO(_gt) );

        tx.commit();
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

  /**
   * @since   1.2.0
   */
  public void deleteField(FieldDTO _f) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete( Rosetta.getDAO(_f) );
        tx.commit();
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

  /**
   * @since   1.2.0
   */
  public boolean existsByName(String name)
    throws  GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("select gt.id from HibernateGroupTypeDAO gt where gt.name = :name");
      qry.setString("name", name);
      boolean rv  = false;
      if ( qry.uniqueResult() != null ) {
        rv = true;
      }
      hs.close();
      return rv;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 
  
  /**
   * @since   1.2.0
   */
  public Set findAll() 
    throws  GrouperDAOException
  {
    Set types = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupTypeDAO order by name asc");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAll");
      types.addAll( Rosetta.getDTO( qry.list() ) );
      hs.close();  
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH ); 
    }
    return types;
  } 

  /**
   * @since   1.2.0
   */
  public GroupTypeDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupTypeDAO as gt where gt.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateGroupTypeDAO dao = (HibernateGroupTypeDAO) qry.uniqueResult();
      if (dao == null) {
        throw new SchemaException();
      }
      hs.close();
      return (GroupTypeDTO) Rosetta.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof HibernateGroupTypeDAO)) {
      return false;
    }
    HibernateGroupTypeDAO that = (HibernateGroupTypeDAO) other;
    return new EqualsBuilder()
      .append( this.getUuid(), that.getUuid() )
      .isEquals();
  } // public boolean equals(other)
  

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   1.2.0
   */
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   1.2.0
   */
  public Set getFields() {
    return new HibernateFieldDAO().findAllFieldsByGroupType( this.getUuid() );
  }

  /**
   * @since   1.2.0
   */
  public boolean getIsAssignable() {
    return this.isAssignable;
  }

  /** 
   * @since   1.2.0
   */
  public boolean getIsInternal() {
    return this.isInternal;
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
  public String getName() {
    return this.name;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getUuid() )
      .toHashCode();
  } // public int hashCode()

  // @since   1.2.0 
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    return Lifecycle.NO_VETO;
  } // public boolean onDelete(hs)

  // @since   1.2.0
  public void onLoad(Session hs, Serializable id) {
    // Nothing
  } // public void onLoad(hs, id)

  // @since   1.2.0
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    return Lifecycle.NO_VETO;
  } // public boolean onSave(hs)

  // @since   1.2.0
  public boolean onUpdate(Session hs) 
    throws  CallbackException
  {
    return Lifecycle.NO_VETO;
  } // public boolean onUpdate(hs)k

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }
  
  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setFields(Set fields) {
    // nothing
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setIsAssignable(boolean isAssignable) {
    this.isAssignable = isAssignable;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setIsInternal(boolean isInternal) {
    this.isInternal = isInternal;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "createTime",   this.getCreateTime()   )
      .append( "creatorUuid",  this.getCreatorUuid()  )
      .append( "fields",       this.getFields()       )
      .append( "id",           this.getId()           )
      .append( "isAssignable", this.getIsAssignable() )
      .append( "isInternal",   this.getIsInternal()   )
      .append( "name",         this.getName()         )
      .append( "uuid",         this.getUuid()         )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    GroupTypeDTO  _type;
    Iterator      it    = GrouperDAOFactory.getFactory().getGroupType().findAll().iterator();
    Iterator      itF;
    while (it.hasNext()) {
      _type = (GroupTypeDTO) it.next();
      if ( ! ( _type.getName().equals("base") || _type.getName().equals("naming") ) ) {
        itF = _type.getFields().iterator(); 
        while (itF.hasNext()) {
          FieldDTO f = (FieldDTO) itF.next();
          hs.delete( Rosetta.getDAO(f) );
        }
        hs.delete( Rosetta.getDAO(_type) );
      }
    }
  }

} 

