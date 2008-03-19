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
import org.hibernate.Session;

import edu.internet2.middleware.grouper.CompositeNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.CompositeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.util.Rosetta;

/**
 * Basic Hibernate <code>Composite</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3CompositeDAO.java,v 1.3 2008-03-19 20:43:24 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3CompositeDAO extends Hib3DAO implements CompositeDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3CompositeDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private long    createTime;
  private String  creatorUUID;
  private String  factorOwnerUUID;
  private String  id;
  private String  leftFactorUUID;
  private String  rightFactorUUID;
  private String  type;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public Set findAsFactor(GroupDTO _g) 
    throws  GrouperDAOException
  {
    Set composites = new LinkedHashSet();
    List<CompositeDAO> compositeDAOs = HibernateSession.byHqlStatic()
      .createQuery("from Hib3CompositeDAO as c where (" 
          + " c.leftFactorUuid = :left or c.rightFactorUuid = :right "
          + ")")
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindAsFactor")
          .setString( "left",  _g.getUuid() )
          .setString( "right", _g.getUuid() )
          .list(CompositeDAO.class);
    for (CompositeDAO compositeDAO : compositeDAOs) {
        composites.add( CompositeDTO.getDTO( compositeDAO ) );
    }
    return composites;
  } // public Set findAsFactor(_g)

  /**
   * @since   @HEAD@
   */
  public CompositeDTO findAsOwner(GroupDTO _g) 
    throws  CompositeNotFoundException,
            GrouperDAOException {
    Hib3CompositeDAO dao = HibernateSession.byHqlStatic()
      .createQuery("from Hib3CompositeDAO as c where c.factorOwnerUuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAsOwner")
      .setString( "uuid", _g.getUuid() ).uniqueResult(Hib3CompositeDAO.class);
    if (dao == null) {
      throw new CompositeNotFoundException();
    }
    return CompositeDTO.getDTO(dao);
  } // public CompositeDTO findAsOwner(_g)

  /**
   * @since   @HEAD@
   */
  public CompositeDTO findByUuid(String uuid) 
    throws  CompositeNotFoundException,
            GrouperDAOException {
    Hib3CompositeDAO dao = HibernateSession.byHqlStatic()
    .createQuery("from Hib3CompositeDAO as c where c.uuid = :uuid")
    .setCacheable(false)
    .setCacheRegion(KLASS + ".FindByUuid")
    .setString( "uuid", uuid ).uniqueResult(Hib3CompositeDAO.class);

    if (dao == null) {
      throw new CompositeNotFoundException();
    }
    return CompositeDTO.getDTO(dao);
  } // public CompositeDTO findByUuid(uuid)

  /**
   * @since   @HEAD@
   */
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   @HEAD@
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getFactorOwnerUuid() {
    return this.factorOwnerUUID;
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
  public String getLeftFactorUuid() {
    return this.leftFactorUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getRightFactorUuid() {
    return this.rightFactorUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getType() {
    return this.type;
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
  public CompositeDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setFactorOwnerUuid(String factorOwnerUUID) {
    this.factorOwnerUUID = factorOwnerUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setLeftFactorUuid(String leftFactorUUID) {
    this.leftFactorUUID = leftFactorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setRightFactorUuid(String rightFactorUUID) {
    this.rightFactorUUID = rightFactorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public void update(final Set toAdd, final Set toDelete, final Set modGroups, final Set modStems) 
    throws  GrouperDAOException {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            Session     hs  = hibernateSession.getSession();
            
            Iterator it = toDelete.iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.delete( grouperDAO );
            } 
            hs.flush();
            
            it = toAdd.iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.save( grouperDAO );
            }
            hs.flush();

            it = modGroups.iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.update( grouperDAO );
            }
            hs.flush();

            it = modStems.iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.update( grouperDAO );
            }
            return null;
          }
      
    });
  } // public void update(toAdd, toDelete, modGroups, modStems)


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.createQuery("delete from Hib3CompositeDAO").executeUpdate();
  } 

} 

