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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Basic Hibernate <code>Stem</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3StemDAO.java,v 1.18 2009-02-09 05:33:30 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3StemDAO extends Hib3DAO implements StemDAO {

  // PRIVATE CLASS CONSTANTS //
  /** */
  private static final String KLASS = Hib3StemDAO.class.getName();


  /**
   * @since   @HEAD@
   */
  public void createChildGroup(final Stem _stem, final Group _group, final Member _member)
    throws  GrouperDAOException {
    
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              ByObject byObject = hibernateSession.byObject();
              byObject.save(_group);

              // add group-type tuples
              GroupTypeTuple  tuple = new GroupTypeTuple();
              Iterator                    it    = _group.getTypesDb().iterator();
              while (it.hasNext()) {
                tuple.setGroupUuid( _group.getUuid() );
                tuple.setTypeUuid( ( (GroupType) it.next() ).getUuid() );
                byObject.save(tuple); // new group-type tuple
              }
              hibernateSession.byObject().update( _stem );
              if ( !GrouperDAOFactory.getFactory().getMember().exists( _member.getUuid() ) ) {
                byObject.save( _member );
              }
              return null;
            }
        
      });
    } catch (GrouperDAOException e) {
      String error = "Problem create child stem: " + GrouperUtil.toStringSafe(_stem)
        + ", child: " + GrouperUtil.toStringSafe(_group) + ", memberDto: " 
        + GrouperUtil.toStringSafe(_member) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public void createChildStem(final Stem _parent, final Stem _child)
    throws  GrouperDAOException {
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

              hibernateSession.byObject().save(_child);
              hibernateSession.byObject().update( _parent );
              return null;
            }
        
      });
    }
    catch (GrouperDAOException e) {
      String error = "Problem creating child stem: " + GrouperUtil.toStringSafe(_parent)
        + ", privs: " + GrouperUtil.toStringSafe(_child) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 


  /**
   * @since   @HEAD@
   */
  public void createRootStem(Stem _root)
    throws  GrouperDAOException {
    try {
      HibernateSession.byObjectStatic().save(_root);
    }
    catch (GrouperDAOException e) {
      String error = "Problem creating root stem: " + GrouperUtil.toStringSafe(_root)
        + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public void delete(Stem _ns)
    throws  GrouperDAOException {
    try {
      HibernateSession.byObjectStatic().delete(_ns);
    } catch (GrouperDAOException e) {
      String error = "Problem deleting: " + GrouperUtil.toStringSafe(_ns)
        + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public boolean exists(String uuid) 
    throws  GrouperDAOException {
    try {
      Object id = HibernateSession.byHqlStatic()
        .createQuery("select ns.id from Stem ns where ns.uuid = :uuid")
        .setString("uuid", uuid).uniqueResult(Object.class);
      
      boolean rv  = false;
      if ( id != null ) {
        rv = true; 
      }
      return rv;
    }
    catch (GrouperDAOException e) {
      String error = "Problem querying stem by uuid: '" + uuid + "', " + e.getMessage();
      LOG.fatal( error );
      throw new GrouperDAOException( error, e );
    }
  } 
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3StemDAO.class);

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where lower(ns.displayExtensionDb) like lower(:value)")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateDisplayExtension")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate display extension: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateDisplayExtension(String val, String scope)
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where lower(ns.displayExtensionDb) like lower(:value) and ns.nameDb like :scope")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateDisplayExtension")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .setString("scope", scope + "%")
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate display extension: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateDisplayName(String val) 
    throws  GrouperDAOException
  {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where lower(ns.displayNameDb) like lower(:value)")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateDisplayName")
        .setString(  "value" , "%" + val.toLowerCase() + "%" ).listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate display name: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateDisplayName(String val, String scope)
    throws  GrouperDAOException
  {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where lower(ns.displayNameDb) like lower(:value) and ns.nameDb like :scope")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateDisplayName")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .setString("scope", scope + "%")
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate display name: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateExtension(String val) 
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where lower(ns.extensionDb) like lower(:value)")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateExtension")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate extension: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateExtension(String val, String scope)
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where lower(ns.extensionDb) like lower(:value) and ns.nameDb like :scope")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateExtension")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .setString("scope", scope + "%")
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate extension: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateName(String val) 
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where lower(ns.nameDb) like lower(:value)")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateName")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate name: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateName(String val, String scope)
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where lower(ns.nameDb) like lower(:value) and ns.nameDb like :scope")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateName")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .setString("scope", scope + "%")
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate name: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery(
        "from Stem as ns where "
        + "   lower(ns.nameDb)            like :name "
        + "or lower(ns.displayNameDb)       like :name "
        + "or lower(ns.extensionDb)         like :name "
        + "or lower(ns.displayExtensionDb)  like :name" )
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByApproximateNameAny")
        .setString("name", "%" + name.toLowerCase() + "%")
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate any: '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateNameAny(String name, String scope)
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery(
        "from Stem as ns where "
        + "(   lower(ns.nameDb)            like :name "
        + " or lower(ns.displayNameDb)       like :name "
        + " or lower(ns.extensionDb)         like :name "
        + " or lower(ns.displayExtensionDb)  like :name ) "
        + "and ns.nameDb like :scope ")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByApproximateNameAny")
        .setString("name", "%" + name.toLowerCase() + "%")
        .setString("scope", scope + "%")
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate any: '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException {
    try {
     return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.createTimeLong > :time")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
        .setLong( "time", d.getTime() )
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by created after: '" + d + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByCreatedAfter(Date d, String scope)
    throws  GrouperDAOException {
    try {
     return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.createTimeLong > :time and ns.nameDb like :scope")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
        .setLong( "time", d.getTime() )
        .setString("scope", scope + "%")
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by created after: '" + d + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.createTimeLong < :time")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByCreatedBefore")
        .setLong( "time", d.getTime() )
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by created before: '" + d + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Stem> findAllByCreatedBefore(Date d, String scope)
    throws  GrouperDAOException {
    try {
      return HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.createTimeLong < :time and ns.nameDb like :scope")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByCreatedBefore")
        .setLong( "time", d.getTime() )
        .setString("scope", scope + "%")
        .listSet(Stem.class);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by created before: '" + d + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 
  
  /**
   * @see     StemDAO#findAllChildGroups(Stem, Stem.Scope)
   * @since   @HEAD@
   */
  public Set<Group> findAllChildGroups(Stem ns, Stem.Scope scope)
    throws  GrouperDAOException {

    Set<Group> groupsSet;
    try {
      if (Stem.Scope.ONE == scope) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getImmediateChildren(ns);
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getAllGroups();
      } else if (Stem.Scope.SUB == scope) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getAllGroups(ns.getNameDb() + Stem.DELIM);
      } else {
        throw new IllegalStateException("unknown search scope: " + scope);
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all child groups, stem name: '" 
        + (ns == null ? null : ns.getNameDb()) + "', scope: '" + scope + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    return groupsSet;
  }
  
  /**
   * @see     StemDAO#findAllChildStems(Stem, Stem.Scope)
   * @throws  IllegalStateException if unknown scope.
   * @since   @HEAD@
   */
  public Set<Stem> findAllChildStems(Stem ns, Stem.Scope scope)
    throws  GrouperDAOException,
            IllegalStateException {
    Set<Stem> stemsSet;
    try {
      if (Stem.Scope.ONE == scope) {
        stemsSet = HibernateSession.byHqlStatic()
          .createQuery("from Stem as ns where ns.parentUuid = :parent")
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindChildStems")
          .setString("parent", ns.getUuid())
          .listSet(Stem.class);
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        stemsSet = HibernateSession.byHqlStatic()
          .createQuery("from Stem as ns where ns.nameDb not like :stem")
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindChildStems")
          .setString("stem", Stem.DELIM)
          .listSet(Stem.class);
      } else if (Stem.Scope.SUB == scope) {
        stemsSet = HibernateSession.byHqlStatic()
          .createQuery("from Stem as ns where ns.nameDb like :scope")
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindChildStems")
          .setString("scope", ns.getNameDb() + Stem.DELIM + "%")
          .listSet(Stem.class);
      } else {
        throw new IllegalStateException("unknown search scope: " + scope);
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all child stems, stem name: '" 
        + (ns == null ? null : ns.getNameDb()) + "', scope: '" + scope + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    return stemsSet;
  } 

  /**
   * @since   @HEAD@
   */
  public Stem findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException {
    try {
      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.nameDb = :name")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByName")
        .setString("name", name)
        .uniqueResult(Stem.class);
      if (stemDto == null) {
        throw new StemNotFoundException("Can't find stem by name: '" + name + "'");
      }
      return stemDto;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find stem by name: '" 
        + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Stem findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException {
    try {
      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.uuid = :uuid")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByUuid")
        .setString("uuid", uuid)
        .uniqueResult(Stem.class);
      if (stemDto == null) {
        throw new StemNotFoundException("Can't find stem by uuid: '" + uuid + "'");
      }
      return stemDto;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find stem by uuid: '" 
        + uuid + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 


  /**
   * @since   @HEAD@
   */
  public Set<Stem> getAllStems()
    throws  GrouperDAOException {
    try {
      Set<Stem> stems = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllStems")
        .listSet(Stem.class);
      return stems;
    } 
    catch (GrouperDAOException e) {
      String error = "Problem getting all stems: " + e.getMessage();
      throw new GrouperDAOException(error, e);
    }
  }

  /** 
   * @since   @HEAD@
   */
  public void revokePriv(final Stem _ns, final DefaultMemberOf mof)
    throws  GrouperDAOException {
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

              ByObject byObject = hibernateSession.byObject();
              byObject.delete(mof.getDeletes());
              hibernateSession.misc().flush();
              
              byObject.saveOrUpdate(mof.getSaves());
              hibernateSession.misc().flush();
              
              byObject.update( _ns );
              return null;
            }
        
      });
    }
    catch (GrouperDAOException e) {
      String error = "Problem revoking priv: " + GrouperUtil.toStringSafe(_ns)
      + ", defaultMemberOf: " + GrouperUtil.toStringSafe(mof) + ", " + e.getMessage();

      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public void renameStemAndChildren(final Stem _ns, final Set children)
    throws  GrouperDAOException {
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              ByObject byObject = hibernateSession.byObject();
              byObject.update(children);
              byObject.update( _ns );
              return null;
            }
        
      });
    }
    catch (GrouperDAOException e) {
      String error = "Problem revoking priv: " + GrouperUtil.toStringSafe(_ns)
        + ", children: " + GrouperUtil.toStringSafe(children) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 
  
  /**
   * @since   @HEAD@
   */
  public void revokePriv(final Stem _ns, final Set toDelete)
    throws  GrouperDAOException
  {
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

              ByObject byObject = hibernateSession.byObject();
              byObject.delete(toDelete);
              byObject.update( _ns );
              return null;
            }
        
      });
    }
    catch (GrouperDAOException e) {
      String error = "Problem revoking priv: " + GrouperUtil.toStringSafe(_ns)
        + ", privs: " + GrouperUtil.toStringSafe(toDelete) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public void update(Stem _ns)
    throws  GrouperDAOException {

    try {
      HibernateSession.byObjectStatic().update(_ns);
    }
    catch (GrouperDAOException e) {
      String error = "Problem with hib update: " + GrouperUtil.toStringSafe(_ns)
       + ",\n" + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 


  // PROTECTED CLASS METHODS //

  /**
   * @param hibernateSession 
   * @throws HibernateException 
   * 
   */
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    // To appease Oracle the root stem is named ":" internally.
    List<Stem> stems = 
      hibernateSession.byHql().createQuery("from Stem as ns where ns.nameDb not like :stem order by nameDb desc")
      .setString("stem", Stem.DELIM)
      .list(Stem.class);

    // Deleting each stem from the time created in descending order. This is necessary to prevent
    // deleting parent stems before child stems which causes integrity constraint violations  on some
    // databases.
    for (Stem stem : stems) {
      hibernateSession.byHql().createQuery("delete from Stem ns where ns.uuid=:uuid")
      .setString("uuid", stem.getUuid())
      .executeUpdate();
    }

    // Reset "modify" columns.  Setting the modifierUuid property to null is important to avoid foreign key issues.
    hibernateSession.byHql().createQuery("update Stem as ns set ns.modifierUuid = :id, ns.modifyTimeLong = :time")
      .setString("id", null)
      .setLong("time", new Long(0))
      .executeUpdate();
  }

  /**
   * find stems by creator or modifier
   * @param member
   * @return the stems
   */
  public Set<Stem> findByCreatorOrModifier(Member member) {
    if (member == null || StringUtils.isBlank(member.getUuid())) {
      throw new RuntimeException("Need to pass in a member");
    }
    Set<Stem> stems = HibernateSession.byHqlStatic()
      .createQuery("from Stem as s where s.creatorUuid = :uuid1 or s.modifierUuid = :uuid2")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByCreatorOrModifier")
      .setString( "uuid1", member.getUuid() ).setString("uuid2", member.getUuid())
      .listSet(Stem.class);
    return stems;
  }

} 

