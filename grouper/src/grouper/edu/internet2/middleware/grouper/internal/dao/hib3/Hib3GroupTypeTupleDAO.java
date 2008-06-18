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

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupTypeTupleDTO;

/**
 * Basic Hibernate <code>Group</code> and <code>GroupType</code> tuple DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupTypeTupleDAO.java,v 1.2.4.1 2008-06-18 09:22:21 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3GroupTypeTupleDAO extends Hib3DAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3GroupTypeTupleDAO.class.getName();


  // @since   @HEAD@
  // TODO 20070418 public until i refactor "Test_Integration_Hib3GroupDAO_delete#testDelete_GroupTypeTuplesDeletedWhenRegistryIsReset()"
  public static GroupTypeTupleDTO findByGroupAndType(GroupDTO g, GroupTypeDTO type)
    throws  GrouperDAOException
  {
    GroupTypeTupleDTO dto = HibernateSession.byHqlStatic()
      .createQuery(
        "from GroupTypeTupleDTO as gtt where"
        + " gtt.groupUuid    = :group"
        + " and gtt.typeUuid = :type")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByGroupAndType")
        .setString( "group", g.getUuid()        )
        .setString( "type",  type.getUuid() )
        .uniqueResult(GroupTypeTupleDTO.class);
    if (dto == null) {
      throw new GrouperDAOException("GroupTypeTupleDTO not found");       
    }
    return dto;
  }


  // PUBLIC INSTANCE METHODS //

  // @since   @HEAD@
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from GroupTypeTupleDTO");
  } // protected static void reset(hs)

} 

