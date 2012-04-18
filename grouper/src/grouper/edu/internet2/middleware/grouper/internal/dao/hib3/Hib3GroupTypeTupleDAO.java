/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import org.hibernate.HibernateException;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeTupleDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Basic Hibernate <code>Group</code> and <code>GroupType</code> tuple DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupTypeTupleDAO.java,v 1.6 2009-03-24 17:12:08 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3GroupTypeTupleDAO extends Hib3DAO implements GroupTypeTupleDAO {

  /**
   * 
   */
  private static final String KLASS = Hib3GroupTypeTupleDAO.class.getName();

  /**
   * 
   * @param g
   * @param type
   * @return the group type tuple
   * @throws GrouperDAOException
   */
  public static GroupTypeTuple findByGroupAndType(Group g, GroupType type)
    throws  GrouperDAOException {
    return findByGroupAndType(g, type, true);
  }

  /**
   * 
   * @param g
   * @param type
   * @param exceptionIfNotExist should this throw an exception if not exist?
   * @return the group type tuple
   * @throws GrouperDAOException
   */
  public static GroupTypeTuple findByGroupAndType(Group g, GroupType type, boolean exceptionIfNotExist)
    throws  GrouperDAOException {
    GroupTypeTuple groupTypeTuple = HibernateSession.byHqlStatic()
      .createQuery(
        "from GroupTypeTuple as gtt where"
        + " gtt.groupUuid    = :group"
        + " and gtt.typeUuid = :type")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByGroupAndType")
        .setString( "group", g.getUuid()        )
        .setString( "type",  type.getUuid() )
        .uniqueResult(GroupTypeTuple.class);
    if (groupTypeTuple == null && exceptionIfNotExist) {
      throw new GrouperDAOException("GroupTypeTuple not found");       
    }
    if (groupTypeTuple != null) {
      groupTypeTuple.assignGroupUuid(g.getUuid(), g);
    }
    return groupTypeTuple;
  }


  /**
   * 
   * @param hs
   * @throws HibernateException
   */
  protected static void reset(Session hs) throws  HibernateException {
    hs.delete("from GroupTypeTuple");
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupTypeTupleDAO#findByUuidOrKey(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public GroupTypeTuple findByUuidOrKey(String uuid, String groupUuid, String typeUuid,
      boolean exceptionIfNull) throws GrouperDAOException {
    try {
      GroupTypeTuple groupTypeTuple = HibernateSession.byHqlStatic()
        .createQuery("from GroupTypeTuple as theGroupTypeTuple where theGroupTypeTuple.id = :uuid " +
        		"or (theGroupTypeTuple.groupUuid = :theGroupUuid and theGroupTypeTuple.typeUuid = :theTypeUuid)")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrKey")
        .setString("uuid", uuid)
        .setString("theGroupUuid", groupUuid)
        .setString("theTypeUuid", typeUuid)
        .uniqueResult(GroupTypeTuple.class);
      if (groupTypeTuple == null && exceptionIfNull) {
        throw new RuntimeException("Can't find groupTypeTuple by uuid: '" + uuid + "' or groupUuid '" + groupUuid + "', typeUuid '" + typeUuid + "'");
      }
      return groupTypeTuple;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find groupTypeTuple by uuid: '" 
        + uuid + "' or groupUuid '" + groupUuid + "', typeUuid '" + typeUuid + "'" + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupTypeTupleDAO#saveUpdateProperties(edu.internet2.middleware.grouper.GroupTypeTuple)
   */
  public void saveUpdateProperties(GroupTypeTuple groupTypeTuple) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update GroupTypeTuple " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId " +
        "where id = :theUuid")
        .setLong("theHibernateVersionNumber", groupTypeTuple.getHibernateVersionNumber())
        .setString("theContextId", groupTypeTuple.getContextId())
        .setString("theUuid", groupTypeTuple.getId())
        .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupTypeTupleDAO#update(edu.internet2.middleware.grouper.GroupTypeTuple)
   */
  public void update(GroupTypeTuple groupTypeTuple) throws GrouperDAOException {
    HibernateSession.byObjectStatic().update(groupTypeTuple);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupTypeTupleDAO#save(edu.internet2.middleware.grouper.GroupTypeTuple)
   */
  public void save(GroupTypeTuple groupTypeTuple) throws GrouperDAOException {
    HibernateSession.byObjectStatic().save(groupTypeTuple);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupTypeTupleDAO#delete(edu.internet2.middleware.grouper.GroupTypeTuple)
   */
  public void delete(GroupTypeTuple groupTypeTuple) throws GrouperDAOException {
    HibernateSession.byObjectStatic().delete(groupTypeTuple);
  }
} 

