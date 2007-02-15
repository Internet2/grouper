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
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Field} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateFieldDAO.java,v 1.9 2007-02-15 18:30:50 blair Exp $
 * @since   1.2.0
 */
class HibernateFieldDAO extends HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateFieldDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  fieldUUID;
  private String  groupTypeUUID;
  private String  id;
  private boolean isNullable;
  private String  name;
  private String  readPrivilege;
  private String  type;
  private String  writePrivilege;


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findAll() 
    throws  GrouperRuntimeException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateFieldDAO order by name asc");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAll");
      fields.addAll( Rosetta.getDTO( qry.list() ) );
      hs.close();  
    }
    catch (HibernateException eH) {
      String msg = E.FIELD_FINDALL + eH.getMessage();
      ErrorLog.fatal(FieldFinder.class, msg);
      throw new GrouperDAOException(msg, eH);
    }
    return fields;
  } // protected Static Set findAll()

  // @since   1.2.0
  protected static Set findAllFieldsByGroupType(String uuid)
    throws  GrouperDAOException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateFieldDAO as f where f.groupTypeUuid = :uuid order by f.name asc");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllFieldsByGroupType");
      qry.setString("uuid", uuid);
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        fields.add( (FieldDTO) Rosetta.getDTO( it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return fields;
  } // protected static Set findAllFieldsByGroupType(uuid)

  // @since   1.2.0
  protected static Set findAllByType(FieldType type) 
    throws  GrouperDAOException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateFieldDAO where type = :type order by name asc");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByType");
      qry.setString( "type", type.toString() );
      fields.addAll( Rosetta.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      String msg = E.FIELD_FINDTYPE + eH.getMessage();
      ErrorLog.fatal(FieldFinder.class, msg);
      throw new GrouperDAOException(msg, eH);
    }
    return fields;
  } // protected static Set fieldAllByType(type)

  // @since   1.2.0
  protected static boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = null;
      if      ( f.getType().equals(FieldType.ATTRIBUTE) ) {
        qry = hs.createQuery("from HibernateAttributeDAO as a where a.attrName = :name");
      }
      else if ( f.getType().equals(FieldType.LIST) )      {
        qry = hs.createQuery("from HibernateMembershipDAO as ms where ms.listName = :name");
      }
      else {
        String msg = E.GROUPTYPE_FIELDNODELTYPE + f.getType().toString();
        ErrorLog.error(HibernateFieldDAO.class, msg);
        throw new SchemaException(msg);
      }
      qry.setCacheable(false);
      qry.setString("name", f.getName() );
      if (qry.list().size() > 0) {
        return true;
      }
    }
    catch (HibernateException eH) {
      String msg = E.HIBERNATE + eH.getMessage();
      ErrorLog.fatal(HibernateFieldDAO.class, msg);
      throw new GrouperDAOException(msg, eH);
    }
    return false;
  } // protected static boolean isInUse()


  // GETTERS //

  protected String getFieldUuid() {
    return this.fieldUUID;
  }
  protected String getGroupTypeUuid() {
    return this.groupTypeUUID;
  }
  protected String getId() {
    return this.id;
  }
  protected boolean getIsNullable() {
    return this.isNullable;
  }
  protected String getName() {
    return this.name;
  }
  protected String getReadPrivilege() {
    return this.readPrivilege;
  }
  protected String getType() {
    return this.type;
  }
  protected String getWritePrivilege() {
    return this.writePrivilege;
  }


  // SETTERS //

  protected void setFieldUuid(String fieldUUID) {
    this.fieldUUID = fieldUUID;
  }
  protected void setGroupTypeUuid(String groupTypeUUID) {
    this.groupTypeUUID = groupTypeUUID;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setIsNullable(boolean isNullable) {
    this.isNullable = isNullable;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setReadPrivilege(String readPrivilege) {
    this.readPrivilege = readPrivilege;
  }
  protected void setType(String type) {
    this.type = type;
  }
  protected void setWritePrivilege(String writePrivilege) {
    this.writePrivilege = writePrivilege;
  }

} // class HibernateFieldDAO extends HibernateDAO

