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
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/** 
 * Basic Hibernate <code>GroupType</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupTypeDAO.java,v 1.15 2009-04-13 20:24:29 mchyzer Exp $
 */
public class Hib3GroupTypeDAO extends Hib3DAO implements GroupTypeDAO {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3GroupTypeDAO.class);

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
   * @param field 
   * @throws GrouperDAOException 
   */
  public void deleteField(final Field field) throws  GrouperDAOException {

    //do this in its own tx so we can be sure it is done and move on to refreshing cache
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        hibernateSession.byObject().delete(field);    
        return null;
      }
      
    });
    
    FieldFinder.clearCache();

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
   */
  @Deprecated
  public GroupType findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException {
    return findByUuid(uuid, true);
  } 

  /**
   * @param uuid 
   * @return type
   * @throws GrouperDAOException 
   * @throws SchemaException 
   */
  public GroupType findByUuid(String uuid, boolean exceptionIfNull)
      throws GrouperDAOException, SchemaException {
    GroupType groupType = HibernateSession.byHqlStatic()
      .createQuery("from GroupType as gt where gt.uuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid)
      .uniqueResult(GroupType.class);
    if (groupType == null && exceptionIfNull) {
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
    throws  HibernateException {
    
    //delete from field where the type is not base or naming
    hibernateSession.byHql().createQuery("delete from Field field where not exists " +
    		"(from GroupType groupType where groupType.uuid = field.groupTypeUuid and groupType.name in ('base', 'naming'))")
    		.executeUpdate();
    //delete from type where it is not base or naming
    hibernateSession.byHql().createQuery("delete from GroupType groupType where groupType.name not in ('base', 'naming')")
        .executeUpdate();
    
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

