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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;


/** 
 * Basic Hibernate <code>GroupType</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupTypeDAO.java,v 1.5 2008-06-29 17:42:41 mchyzer Exp $
 */
public class Hib3GroupTypeDAO extends Hib3DAO implements GroupTypeDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3GroupTypeDAO.class.getName();


  /**
   * @since   @HEAD@
   */
  public void create(GroupType _gt)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().save(_gt);
  } 

  /**
   * @since   @HEAD@
   */
  public void createField(Field _f)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().save(_f);
  } 

  /**
   * @since   @HEAD@
   */
  public void delete(GroupType _gt, Set fields)
    throws  GrouperDAOException {
    List<Object> list = new ArrayList<Object>();
    for (Object field: fields) {
      list.add(field);
    }
    list.add(_gt);
    HibernateSession.byObjectStatic().delete(list);
  } 

  /**
   * @since   @HEAD@
   */
  public void deleteField(Field _f) throws  GrouperDAOException {
    HibernateSession.byObjectStatic().delete(_f);
  } 

  /**
   * @since   @HEAD@
   */
  public boolean existsByName(String name)
    throws  GrouperDAOException {

    Object id = HibernateSession.byHqlStatic()
      .createQuery("select gt.id from GroupType gt where gt.name = :name")
      .setString("name", name).uniqueResult(Object.class);
    boolean rv  = false;
    if ( id != null ) {
      rv = true;
    }
    return rv;
  } 
  
  /**
   * @since   @HEAD@
   */
  public Set<GroupType> findAll() 
    throws  GrouperDAOException {

    return HibernateSession.byHqlStatic()
      .createQuery("from GroupType order by name asc")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAll")
      .listSet(GroupType.class);
  } 

  /**
   * @since   @HEAD@
   */
  public GroupType findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException
  {
    GroupType groupType = HibernateSession.byHqlStatic()
      .createQuery("from GroupType as gt where gt.uuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid)
      .uniqueResult(GroupType.class);
    if (groupType == null) {
      throw new SchemaException();
    }
    return groupType;
  } 

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    GroupType  _type;
    Iterator      it    = GrouperDAOFactory.getFactory().getGroupType().findAll().iterator();
    ByObject byObject = hibernateSession.byObject();
    while (it.hasNext()) {
      _type = (GroupType) it.next();
      if ( ! ( _type.getName().equals("base") || _type.getName().equals("naming") ) ) {
        byObject.setIgnoreHooks(true).delete(_type.getFields());
        byObject.setIgnoreHooks(true).delete( _type );
      }
    }
  }

} 

