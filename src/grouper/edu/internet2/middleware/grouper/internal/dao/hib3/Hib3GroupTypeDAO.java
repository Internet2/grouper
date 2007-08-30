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
import  org.apache.commons.lang.builder.*;
import  org.hibernate.*;
import  org.hibernate.classic.Lifecycle;


/** 
 * Basic Hibernate <code>GroupType</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3GroupTypeDAO.java,v 1.1 2007-08-30 15:52:22 blair Exp $
 */
public class Hib3GroupTypeDAO extends Hib3DAO implements GroupTypeDAO, Lifecycle {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3GroupTypeDAO.class.getName();


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
   * @since   @HEAD@
   */
  public String create(GroupTypeDTO _gt)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = Hib3DAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_gt);
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
   * @since   @HEAD@
   */
  public String createField(FieldDTO _f)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = Hib3DAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_f);
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
   * @since   @HEAD@
   */
  public void delete(GroupTypeDTO _gt, Set fields)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = Hib3DAO.getSession();
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
   * @since   @HEAD@
   */
  public void deleteField(FieldDTO _f) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = Hib3DAO.getSession();
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
   * @since   @HEAD@
   */
  public boolean existsByName(String name)
    throws  GrouperDAOException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("select gt.id from Hib3GroupTypeDAO gt where gt.name = :name");
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
   * @since   @HEAD@
   */
  public Set findAll() 
    throws  GrouperDAOException
  {
    Set types = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3GroupTypeDAO order by name asc");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAll");
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        types.add( GroupTypeDTO.getDTO( (GroupTypeDAO) it.next() ) );
      }
      hs.close();  
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH ); 
    }
    return types;
  } 

  /**
   * @since   @HEAD@
   */
  public GroupTypeDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3GroupTypeDAO as gt where gt.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      Hib3GroupTypeDAO dao = (Hib3GroupTypeDAO) qry.uniqueResult();
      if (dao == null) {
        throw new SchemaException();
      }
      hs.close();
      return (GroupTypeDTO) GroupTypeDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Hib3GroupTypeDAO)) {
      return false;
    }
    Hib3GroupTypeDAO that = (Hib3GroupTypeDAO) other;
    return new EqualsBuilder()
      .append( this.getUuid(), that.getUuid() )
      .isEquals();
  } // public boolean equals(other)
  

  /**
   * @since   @HEAD@
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   @HEAD@
   */
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   @HEAD@
   */
  public Set getFields() {
    return new Hib3FieldDAO().findAllFieldsByGroupType( this.getUuid() );
  }

  /**
   * @since   @HEAD@
   */
  public boolean getIsAssignable() {
    return this.isAssignable;
  }

  /** 
   * @since   @HEAD@
   */
  public boolean getIsInternal() {
    return this.isInternal;
  }

  /**
   * @since   @HEAD@
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   @HEAD@
   */
  public String getName() {
    return this.name;
  }

  /**
   * @since   @HEAD@
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @since   @HEAD@
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getUuid() )
      .toHashCode();
  } // public int hashCode()

  // @since   @HEAD@ 
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    return Lifecycle.NO_VETO;
  } // public boolean onDelete(hs)

  // @since   @HEAD@
  public void onLoad(Session hs, Serializable id) {
    // Nothing
  } // public void onLoad(hs, id)

  // @since   @HEAD@
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    return Lifecycle.NO_VETO;
  } // public boolean onSave(hs)

  // @since   @HEAD@
  public boolean onUpdate(Session hs) 
    throws  CallbackException
  {
    return Lifecycle.NO_VETO;
  } // public boolean onUpdate(hs)k

  /**
   * @since   @HEAD@
   */
  public GroupTypeDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupTypeDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }
  
  /**
   * @since   @HEAD@
   */
  public GroupTypeDAO setFields(Set fields) {
    // nothing
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupTypeDAO setIsAssignable(boolean isAssignable) {
    this.isAssignable = isAssignable;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupTypeDAO setIsInternal(boolean isInternal) {
    this.isInternal = isInternal;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupTypeDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupTypeDAO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupTypeDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   @HEAD@
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

  // @since   @HEAD@
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

