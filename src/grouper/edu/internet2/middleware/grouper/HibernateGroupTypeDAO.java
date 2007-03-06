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
 * @version $Id: HibernateGroupTypeDAO.java,v 1.10 2007-03-06 20:19:00 blair Exp $
 */
class HibernateGroupTypeDAO extends HibernateDAO implements Lifecycle {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateGroupTypeDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  creatorUUID;
  private long    createTime;
  private boolean isAssignable  = true;
  private boolean isInternal    = false;
  private String  id;
  private String  name;
  private String  typeUUID;


  // PUBLIC INSTANCE METHODS //

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
      .append( this.getTypeUuid(), that.getTypeUuid() )
      .isEquals();
  } // public boolean equals(other)
  
  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getTypeUuid() )
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
  public String toString() {
    return new ToStringBuilder(this)
      .append( "createTime",   this.getCreateTime()   )
      .append( "creatorUuid",  this.getCreatorUuid()  )
      .append( "fields",       this.getFields()       )
      .append( "id",           this.getId()           )
      .append( "isAssignable", this.getIsAssignable() )
      .append( "isInternal",   this.getIsInternal()   )
      .append( "name",         this.getName()         )
      .append( "typeUuid",     this.getTypeUuid()     )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static String create(GroupTypeDTO gt)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = Rosetta.getDAO(gt);
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
  } // protected static String create(GroupTypeDTO gt)

  // @since   1.2.0
  protected static String createField(FieldDTO _f)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = Rosetta.getDAO(_f);
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
  } // protected static String create(FieldDTO _f)

  // @since   1.2.0
  protected static void delete(GroupTypeDTO _type, Set fields)
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
        hs.delete( Rosetta.getDAO(_type) );

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
  } // protected static void delete(type

  // @since   1.2.0
  protected static void deleteField(FieldDTO _f) 
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
  } // protected static void deleteField(_f)

  // @since   1.2.0
  protected static Set findAll() 
    throws  GrouperDAOException
  {
    Set types = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupTypeDAO order by name asc");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAll");
      types.addAll( Rosetta.getDTO( qry.list() ) );
      hs.close();  
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH ); 
    }
    return types;
  } // protected static Set findAll()

  // @since   1.2.0
  protected static GroupTypeDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupTypeDAO as gt where gt.typeUuid = :uuid");
      qry.setCacheable(true);
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
  } // protected static GroupTypeDTO findByUuid(uuid)

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    GroupTypeDTO  _type;
    Iterator      it    = HibernateGroupTypeDAO.findAll().iterator();
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
  } // protected static void reset(hs)


  // GETTERS //

  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected long getCreateTime() {
    return this.createTime;
  }
  protected Set getFields() {
    return HibernateFieldDAO.findAllFieldsByGroupType( this.getTypeUuid() );
  }
  protected boolean getIsAssignable() {
    return this.isAssignable;
  }
  protected boolean getIsInternal() {
    return this.isInternal;
  }
  protected String getId() {
    return this.id;
  }
  protected String getName() {
    return this.name;
  }
  protected String getTypeUuid() {
    return this.typeUUID;
  }


  // SETTERS //

  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setFields(Set fields) {
  }
  protected void setIsAssignable(boolean isAssignable) {
    this.isAssignable = isAssignable;
  }
  protected void setIsInternal(boolean isInternal) {
    this.isInternal = isInternal;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setTypeUuid(String typeUUID) {
    this.typeUUID = typeUUID;
  }

} // class HibernateGroupTypeDAO extends HibernateDAO implements Lifecycle

