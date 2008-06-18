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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dto.FieldDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;


/** 
 * Basic Hibernate <code>GroupType</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupTypeDAO.java,v 1.2.4.3 2008-06-18 09:22:21 mchyzer Exp $
 */
public class Hib3GroupTypeDAO extends Hib3DAO implements GroupTypeDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3GroupTypeDAO.class.getName();


  /**
   * @since   @HEAD@
   */
  public void create(GroupTypeDTO _gt)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().save(_gt);
  } 

  /**
   * @since   @HEAD@
   */
  public void createField(FieldDTO _f)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().save(_f);
  } 

  /**
   * @since   @HEAD@
   */
  public void delete(GroupTypeDTO _gt, Set fields)
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
  public void deleteField(FieldDTO _f) throws  GrouperDAOException {
    HibernateSession.byObjectStatic().delete(_f);
  } 

  /**
   * @since   @HEAD@
   */
  public boolean existsByName(String name)
    throws  GrouperDAOException {

    Object id = HibernateSession.byHqlStatic()
      .createQuery("select gt.id from GroupTypeDTO gt where gt.name = :name")
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
  public Set<GroupTypeDTO> findAll() 
    throws  GrouperDAOException {

    return HibernateSession.byHqlStatic()
      .createQuery("from GroupTypeDTO order by name asc")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAll")
      .listSet(GroupTypeDTO.class);
  } 

  /**
   * @since   @HEAD@
   */
  public GroupTypeDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException
  {
    GroupTypeDTO groupTypeDTO = HibernateSession.byHqlStatic()
      .createQuery("from GroupTypeDTO as gt where gt.uuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid)
      .uniqueResult(GroupTypeDTO.class);
    if (groupTypeDTO == null) {
      throw new SchemaException();
    }
    return groupTypeDTO;
  } 

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    GroupTypeDTO  _type;
    Iterator      it    = GrouperDAOFactory.getFactory().getGroupType().findAll().iterator();
    ByObject byObject = hibernateSession.byObject();
    while (it.hasNext()) {
      _type = (GroupTypeDTO) it.next();
      if ( ! ( _type.getName().equals("base") || _type.getName().equals("naming") ) ) {
        byObject.delete(_type.getFields());
        byObject.delete( _type );
      }
    }
  }

} 

