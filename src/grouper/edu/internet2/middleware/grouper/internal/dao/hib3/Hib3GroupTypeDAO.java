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

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/** 
 * Basic Hibernate <code>GroupType</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupTypeDAO.java,v 1.12 2009-02-07 20:16:08 mchyzer Exp $
 */
public class Hib3GroupTypeDAO extends Hib3DAO implements GroupTypeDAO {

  /** */
  private static final String KLASS = Hib3GroupTypeDAO.class.getName();

  /**
   * insert or update
   * @param groupType
   * @throws GrouperDAOException 
   */
  public void createOrUpdate(final GroupType groupType) throws GrouperDAOException {

    HibernateSession.byObjectStatic().saveOrUpdate(groupType); 
        
  }
  
  /**
   * @param _f 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void createField(Field _f)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().save(_f);
  } 

  /**
   * @param _gt 
   * @param fields 
   * @throws GrouperDAOException 
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
   * @param _f 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void deleteField(Field _f) throws  GrouperDAOException {
    HibernateSession.byObjectStatic().delete(_f);
  } 

  /**
   * @param name 
   * @return boolean
   * @throws GrouperDAOException 
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
   * @return set of types
   * @throws GrouperDAOException 
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
   * @param uuid 
   * @return type
   * @throws GrouperDAOException 
   * @throws SchemaException 
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
      throw new SchemaException("Group type with uuid: '" + uuid + "' cant be found");
    }
    return groupType;
  } 

  /**
   * 
   * @param hibernateSession
   * @throws HibernateException
   */
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

  /**
   * find all group types by creator
   * @param member
   * @return the group types
   */
  public Set<GroupType> findAllByCreator(Member member) {
    if (member == null || StringUtils.isBlank(member.getUuid())) {
      throw new RuntimeException("Need to pass in a member");
    }
    Set<GroupType> groupTypes = HibernateSession.byHqlStatic()
      .createQuery("from GroupType as gt where gt.creatorUuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByCreator")
      .setString( "uuid", member.getUuid() ).listSet(GroupType.class);
    return groupTypes;
  }
} 

