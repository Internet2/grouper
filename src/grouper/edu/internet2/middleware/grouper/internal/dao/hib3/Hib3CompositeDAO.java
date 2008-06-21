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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.CompositeNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateMisc;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.CompositeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.dto.GrouperDTO;

/**
 * Basic Hibernate <code>Composite</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3CompositeDAO.java,v 1.4 2008-06-21 04:16:12 mchyzer Exp $
 */
public class Hib3CompositeDAO extends Hib3DAO implements CompositeDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3CompositeDAO.class.getName();


  /**
   * @since   @HEAD@
   */
  public Set<CompositeDTO> findAsFactor(GroupDTO _g) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery("from CompositeDTO as c where (" 
          + " c.leftFactorUuid = :left or c.rightFactorUuid = :right "
          + ")")
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindAsFactor")
          .setString( "left",  _g.getUuid() )
          .setString( "right", _g.getUuid() )
          .listSet(CompositeDTO.class);
  } // public Set findAsFactor(_g)

  /**
   * @since   @HEAD@
   */
  public CompositeDTO findAsOwner(GroupDTO _g) 
    throws  CompositeNotFoundException,
            GrouperDAOException {
    CompositeDTO dto = HibernateSession.byHqlStatic()
      .createQuery("from CompositeDTO as c where c.factorOwnerUuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAsOwner")
      .setString( "uuid", _g.getUuid() ).uniqueResult(CompositeDTO.class);
    if (dto == null) {
      throw new CompositeNotFoundException();
    }
    return dto;
  } // public CompositeDTO findAsOwner(_g)

  /**
   * @since   @HEAD@
   */
  public CompositeDTO findByUuid(String uuid) 
    throws  CompositeNotFoundException,
            GrouperDAOException {
    CompositeDTO dto = HibernateSession.byHqlStatic()
    .createQuery("from CompositeDTO as c where c.uuid = :uuid")
    .setCacheable(false)
    .setCacheRegion(KLASS + ".FindByUuid")
    .setString( "uuid", uuid ).uniqueResult(CompositeDTO.class);

    if (dto == null) {
      throw new CompositeNotFoundException();
    }
    return dto;
  } // public CompositeDTO findByUuid(uuid)

  /**
   * @since   @HEAD@
   */
  public void update(final Set toAdd, final Set toDelete, final Set modGroups, final Set modStems) 
    throws  GrouperDAOException {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            
            ByObject byObject = hibernateSession.byObject();
            HibernateMisc misc = hibernateSession.misc();

            byObject.delete(toDelete);
            misc.flush();
            
            byObject.save(toAdd);
            misc.flush();

            byObject.update(modGroups);
            misc.flush();

            byObject.update(modStems);
            return null;
          }
      
    });
  } // public void update(toAdd, toDelete, modGroups, modStems)


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    hibernateSession.byHql().createQuery("delete from CompositeDTO").executeUpdate();
  } 

} 

