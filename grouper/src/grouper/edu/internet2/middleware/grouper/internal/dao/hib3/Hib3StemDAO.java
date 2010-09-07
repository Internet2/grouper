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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefAssignmentType;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Basic Hibernate <code>Stem</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3StemDAO.java,v 1.38 2009-11-17 02:52:29 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3StemDAO extends Hib3DAO implements StemDAO {

  // PRIVATE CLASS CONSTANTS //
  /** */
  private static final String KLASS = Hib3StemDAO.class.getName();


  /**
   * @param _stem 
   * @param _group 
   * @param _member 
   * @param attributes 
   * @throws GrouperDAOException 
   * @since   
   */
  public void createChildGroup(final Stem _stem, final Group _group, final Member _member, final Map<String, String> attributes)
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
              Iterator                    it    = _group.getTypesDb().iterator();
              while (it.hasNext()) {
  
                GroupType groupType = (GroupType) it.next();
  
                //see if that record exists
                if (null == Hib3GroupTypeTupleDAO.findByGroupAndType(_group, groupType, false)) {
                  GroupTypeTuple tuple = new GroupTypeTuple();
                  tuple.setId(GrouperUuid.getUuid());
                  tuple.assignGroupUuid( _group.getUuid(), _group );
                  tuple.setTypeUuid( groupType.getUuid() );
                  byObject.saveOrUpdate(tuple); // new group-type tuple
                }
              }
              
              //loop through in case an attribute is set in hook
              if (attributes != null) {
                for (String key : attributes.keySet()) {
                  _group.setAttribute(key, attributes.get(key), false);
                }
              }
              
              //MCH 2009/03/23 remove this for optimistic locking
              //hibernateSession.byObject().update( _stem );
              hibernateSession.misc().flush();
              if ( !GrouperDAOFactory.getFactory().getMember().exists( _member.getUuid() ) ) {
                byObject.save( _member );
              }
              return null;
            }
        
      });
    } catch (GrouperDAOException e) {
      String error = "Problem create child group: " + GrouperUtil.toStringSafe(_stem)
        + ", child: " + GrouperUtil.toStringSafe(_group) + ", memberDto: " 
        + GrouperUtil.toStringSafe(_member) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @param _stem 
   * @param attributeDef 
   * @throws GrouperDAOException 
   * @since   
   */
  public void createChildAttributeDef(final Stem _stem, final AttributeDef attributeDef)
    throws  GrouperDAOException {
    
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              ByObject byObject = hibernateSession.byObject();
              
              byObject.save(attributeDef);
              
              // add group sets to each security list field
              Set<Field> fields = FieldFinder.findAll();
              Iterator<Field> iter = fields.iterator();
              
              while (iter.hasNext()) {
                Field field = iter.next();
                if (field.isAttributeDefListField()) {
                  GroupSet groupSet = new GroupSet();
                  groupSet.setId(GrouperUuid.getUuid());
                  groupSet.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
                  groupSet.setDepth(0);
                  groupSet.setMemberAttrDefId(attributeDef.getId());
                  groupSet.setOwnerAttrDefId(attributeDef.getId());
                  groupSet.setParentId(groupSet.getId());
                  groupSet.setFieldId(field.getUuid());
                  GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
                }
              }

              

              
              hibernateSession.misc().flush();
              return null;
            }
        
      });
    } catch (GrouperDAOException e) {
      String error = "Problem create child attributeDef: " + GrouperUtil.toStringSafe(_stem)
        + ", child: " + GrouperUtil.toStringSafe(attributeDef) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @param _child 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void createChildStem(final Stem _child)
    throws  GrouperDAOException {

    HibernateSession.byObjectStatic().save(_child);
    
    createGroupSetsForStem(_child);
  } 


  /**
   * @param _root 
   * @throws GrouperDAOException 
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
    
    createGroupSetsForStem(_root);
  } 
  
  /**
   * 
   * @param stem
   */
  private void createGroupSetsForStem(Stem stem) {
    
    // add group sets
    Set<Field> fields = FieldFinder.findAll();
    Iterator<Field> iter = fields.iterator();
    
    while (iter.hasNext()) {
      Field field = iter.next();
      if (field.isStemListField()) {
        GroupSet groupSet = new GroupSet();
        groupSet.setId(GrouperUuid.getUuid());
        groupSet.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
        groupSet.setDepth(0);
        groupSet.setMemberStemId(stem.getUuid());
        groupSet.setOwnerStemId(stem.getUuid());
        groupSet.setParentId(groupSet.getId());
        groupSet.setFieldId(field.getUuid());
        GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
      }
    }
  }

  /**
   * @param _ns 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void delete(Stem _ns)
    throws  GrouperDAOException {
    
    // delete the group set
    GrouperDAOFactory.getFactory().getGroupSet().deleteSelfByOwnerStem(_ns.getUuid());

    try {
      HibernateSession.byObjectStatic().delete(_ns);
    } catch (GrouperDAOException e) {
      String error = "Problem deleting: " + GrouperUtil.toStringSafe(_ns)
        + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @param uuid 
   * @return if exists
   * @throws GrouperDAOException 
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
   * @param val 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param val 
   * @param scope 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param val 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param val 
   * @param scope 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param val 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param val 
   * @param scope 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param val 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param val 
   * @param scope 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param name 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param name 
   * @param scope 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param d 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param d 
   * @param scope 
   * @return  set stems
   * @throws GrouperDAOException 
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
   * @param d 
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param d 
   * @param scope 
   * @return set stems
   * @throws GrouperDAOException 
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
    return findAllChildStems(ns, scope, false);
  }
  
  /**
   * @param ns 
   * @param scope 
   * @param orderByName 
   * @return set stem
   * @throws GrouperDAOException 
   * @see     StemDAO#findAllChildStems(Stem, Stem.Scope)
   * @throws  IllegalStateException if unknown scope.
   * @since   @HEAD@
   */
  public Set<Stem> findAllChildStems(Stem ns, Stem.Scope scope, boolean orderByName)
    throws  GrouperDAOException,
            IllegalStateException {
    Set<Stem> stemsSet;
    try {
      if (Stem.Scope.ONE == scope) {
        String sql = "from Stem as ns where ns.parentUuid = :parent";
        if (orderByName) {
          sql += " order by ns.nameDb";
        }
        stemsSet = HibernateSession.byHqlStatic()
          .createQuery(sql)
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindChildStems")
          .setString("parent", ns.getUuid())
          .listSet(Stem.class);
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        String sql = "from Stem as ns where ns.nameDb not like :stem";
        if (orderByName) {
          sql += " order by ns.nameDb";
        }
        stemsSet = HibernateSession.byHqlStatic()
          .createQuery(sql)
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindChildStems")
          .setString("stem", Stem.DELIM)
          .listSet(Stem.class);
      } else if (Stem.Scope.SUB == scope) {
        String sql = "from Stem as ns where ns.nameDb like :scope";
        if (orderByName) {
          sql += " order by ns.nameDb";
        }
        stemsSet = HibernateSession.byHqlStatic()
          .createQuery(sql)
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
   * @param name
   * @return stem
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   * @deprecated
   */
  @Deprecated
  public Stem findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException {
    return findByName(name, true);
  } 

  /**
   * @param name
   * @param exceptionIfNull
   * @return stem
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   */
  public Stem findByName(String name, boolean exceptionIfNull) 
    throws  GrouperDAOException,
            StemNotFoundException {
    return findByName(name, exceptionIfNull, null);
  } 

  /**
   * @param uuid
   * @return stem
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   * @deprecated
   */
  @Deprecated
  public Stem findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException {
    return findByUuid(uuid, true);
  } 

  /**
   * @param uuid
   * @param exceptionIfNull
   * @return stem
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   */
  public Stem findByUuid(String uuid, boolean exceptionIfNull)
    throws  GrouperDAOException,
            StemNotFoundException {
    return findByUuid(uuid, exceptionIfNull, null);
  } 


  /**
   * @return set stems
   * @throws GrouperDAOException 
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
   * @param _ns 
   * @param children 
   * @throws GrouperDAOException 
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
   * @param _ns 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void update(final Stem _ns) throws  GrouperDAOException {

    try {
      HibernateSession.byObjectStatic().update(_ns);
    }
    catch (GrouperDAOException e) {
      String error = "Problem with hib update: " + GrouperUtil.toStringSafe(_ns)
       + ",\n" + e.getMessage();
      GrouperUtil.injectInException(e, error);
      throw e;
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

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#findAllChildGroupsSecure(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Group> findAllChildGroupsSecure(Stem ns, Scope scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions) throws GrouperDAOException {
    
    Set<Group> groupsSet;
    try {
      if (Stem.Scope.ONE == scope) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getImmediateChildrenSecure(
            grouperSession, ns, subject, inPrivSet, queryOptions);
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure(grouperSession, subject, inPrivSet, queryOptions);
      } else if (Stem.Scope.SUB == scope) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure(ns.getNameDb() + Stem.DELIM, grouperSession, 
            subject, inPrivSet, queryOptions);
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
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#findAllChildMembershipGroupsSecure(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Group> findAllChildMembershipGroupsSecure(Stem ns, Scope scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions) throws GrouperDAOException {
    
    Set<Group> groupsSet;
    try {
      if (Stem.Scope.ONE == scope) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getImmediateChildrenMembershipSecure(
            grouperSession, ns, subject, inPrivSet, queryOptions, true);
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getAllGroupsMembershipSecure(grouperSession, subject, inPrivSet, queryOptions, true);
      } else if (Stem.Scope.SUB == scope) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getAllGroupsMembershipSecure(ns.getNameDb() + Stem.DELIM, grouperSession, 
            subject, inPrivSet, queryOptions, true);
      } else {
        throw new IllegalStateException("unknown search scope: " + scope);
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all child membership groups, stem name: '" 
        + (ns == null ? null : ns.getNameDb()) + "', scope: '" + scope + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

    return groupsSet;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#findAllChildStemsSecure(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Stem> findAllChildStemsSecure(Stem ns, Scope scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions) throws GrouperDAOException {
    
    Set<Stem> stemsSet;
    try {
      if (Stem.Scope.ONE == scope) {
        stemsSet = GrouperDAOFactory.getFactory().getStem().getImmediateChildrenSecure(
            grouperSession, ns, subject, inPrivSet, queryOptions);
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        stemsSet = GrouperDAOFactory.getFactory().getStem().getAllStemsSecure(grouperSession, subject, inPrivSet, queryOptions);
      } else if (Stem.Scope.SUB == scope) {
        stemsSet = GrouperDAOFactory.getFactory().getStem().getAllStemsSecure(ns.getNameDb() + Stem.DELIM, grouperSession, 
            subject, inPrivSet, queryOptions);
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
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#getImmediateChildrenSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Stem> getImmediateChildrenSecure(GrouperSession grouperSession, 
      final Stem stem, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException {
  
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theStem.displayNameDb");
    }
  
    StringBuilder sql = new StringBuilder("select distinct theStem from Stem as theStem ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(subject, byHqlStatic, 
        sql, "theStem.uuid", inPrivSet);
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append(" theStem.parentUuid = :parent ");
    
    Set<Stem> stems = byHqlStatic.createQuery(sql.toString())
      .setString("parent", stem.getUuid())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".getImmediateChildrenSecure")
      .options(queryOptions)
      .listSet(Stem.class);

    //if the hql didnt filter, this will
    Set<Stem> filteredStems = grouperSession.getNamingResolver()
      .postHqlFilterStems(stems, subject, inPrivSet);

    return filteredStems;
  
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#getAllStemsSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Stem> getAllStemsSecure(GrouperSession grouperSession,
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
      throws GrouperDAOException {
    return getAllStemsSecure(null, grouperSession, subject, inPrivSet, queryOptions);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#getAllStemsSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Stem> getAllStemsSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet,
      QueryOptions queryOptions) throws GrouperDAOException {

    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theStem.displayNameDb");
    }
    //TODO update for 1.5

    StringBuilder sql = new StringBuilder("select distinct theStem from Stem theStem ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(subject, byHqlStatic, 
        sql, "theStem.uuid", inPrivSet);

    //see if there is a scope
    if (!StringUtils.isBlank(scope)) {
      if (!changedQuery) {
        sql.append(" where ");
      } else {
        sql.append(" and ");
      }
      sql.append(" theStem.nameDb like :scope");
      byHqlStatic.setString("scope", scope + "%");

    }
    
    Set<Stem> stems = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".GetAllStemsSecure")
      .options(queryOptions)
      .listSet(Stem.class);
    
    //if the hql didnt filter, this will
    Set<Stem> filteredStems = grouperSession.getNamingResolver()
      .postHqlFilterStems(stems, subject, inPrivSet);

    return filteredStems;
  }

  /**
   * @param _parent 
   * @param attributeDefName 
   * @throws GrouperDAOException 
   * 
   */
  public void createChildAttributeDefName(Stem _parent, final AttributeDefName attributeDefName)
      throws GrouperDAOException {
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              ByObject byObject = hibernateSession.byObject();
              
              byObject.save(attributeDefName);
              
              AttributeDefNameSet attributeDefNameSet = new AttributeDefNameSet();
              attributeDefNameSet.setId(GrouperUuid.getUuid());
              attributeDefNameSet.setDepth(0);
              attributeDefNameSet.setIfHasAttributeDefNameId(attributeDefName.getId());
              attributeDefNameSet.setThenHasAttributeDefNameId(attributeDefName.getId());
              attributeDefNameSet.setType(AttributeDefAssignmentType.self);
              attributeDefNameSet.setParentAttrDefNameSetId(attributeDefNameSet.getId());
              attributeDefNameSet.saveOrUpdate();
              
              hibernateSession.misc().flush();
              return null;
            }
        
      });
    } catch (GrouperDAOException e) {
      String error = "Problem create child attributeDef: " + GrouperUtil.toStringSafe(_parent)
        + ", child: " + GrouperUtil.toStringSafe(attributeDefName) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#updateLastMembershipChange(java.lang.String)
   */
  public void updateLastMembershipChange(String stemId) {
    HibernateSession.bySqlStatic().executeSql(
        "update grouper_stems set last_membership_change = ? where id = ?",
        GrouperUtil.toList((Object) System.currentTimeMillis(), stemId));
  }
  

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#updateLastMembershipChangeIncludeAncestorGroups(java.lang.String)
   */
  public void updateLastMembershipChangeIncludeAncestorGroups(String groupId) {
    
    // note that i'm not doing this all in one update statement with a subquery due to
    // a mysql bug:  http://bugs.mysql.com/bug.php?id=8139
    
    Set<String> stemIds = GrouperDAOFactory.getFactory().getGroupSet().findAllOwnerStemsByMemberGroup(groupId);
    if (stemIds.size() == 0) {
      return;
    }
    
    String queryPrefix = "update grouper_stems set last_membership_change = ? where id ";
    Object time = (Object) System.currentTimeMillis();
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemIds, 100);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> stemIdsInBatch = GrouperUtil.batchList(stemIds, 100, i);
      List<Object> params = new ArrayList<Object>();
      params.add(time);
      params.addAll(stemIdsInBatch);
      
      String queryInClause = HibUtils.convertToInClauseForSqlStatic(stemIdsInBatch);
      HibernateSession.bySqlStatic().executeSql(queryPrefix + " in (" + queryInClause + ")", params);    
    }
  }

  /**
   * @param name
   * @param exceptionIfNull
   * @param queryOptions 
   * @return stem
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   */
  public Stem findByName(String name, boolean exceptionIfNull, QueryOptions queryOptions) 
    throws  GrouperDAOException,
            StemNotFoundException {
    try {
      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.nameDb = :name")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByName")
        .options(queryOptions)
        .setString("name", name)
        .uniqueResult(Stem.class);
      if (stemDto == null && exceptionIfNull) {
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
   * @param uuid
   * @param exceptionIfNull
   * @param queryOptions 
   * @return stem
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   */
  public Stem findByUuid(String uuid, boolean exceptionIfNull, QueryOptions queryOptions)
    throws  GrouperDAOException,
            StemNotFoundException {
    try {
      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.uuid = :uuid")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuid")
        .options(queryOptions)
        .setString("uuid", uuid)
        .uniqueResult(Stem.class);
      if (stemDto == null && exceptionIfNull) {
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
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean)
   */
  public Stem findByUuidOrName(String uuid, String name, boolean exceptionIfNull)
      throws GrouperDAOException, StemNotFoundException {
    
    if (StringUtils.equals(name, ":") || StringUtils.isBlank(name)) {
      return StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    }
    
    try {
      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.uuid = :uuid or ns.nameDb = :name")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("uuid", uuid)
        .setString("name", name)
        .uniqueResult(Stem.class);
      if (stemDto == null && exceptionIfNull) {
        throw new StemNotFoundException("Can't find stem by uuid: '" + uuid + "' or name '" + name + "'");
      }
      return stemDto;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find stem by uuid: '" 
        + uuid + "' or name '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#saveUpdateProperties(edu.internet2.middleware.grouper.Stem)
   */
  public void saveUpdateProperties(Stem stem) {
    
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update Stem " +
    		"set hibernateVersionNumber = :theHibernateVersionNumber, " +
    		"creatorUuid = :theCreatorUuid, " +
    		"createTimeLong = :theCreateTimeLong, " +
    		"modifierUuid = :theModifierUuid, " +
    		"modifyTimeLong = :theModifyTimeLong, " +
    		"contextId = :theContextId, " +
    		"lastMembershipChangeDb = :theLastMembershipChangeDb " +
    		"where uuid = :theUuid")
    		.setLong("theHibernateVersionNumber", stem.getHibernateVersionNumber())
    		.setString("theCreatorUuid", stem.getCreatorUuid())
    		.setLong("theCreateTimeLong", stem.getCreateTimeLong())
    		.setString("theModifierUuid", stem.getModifierUuid())
    		.setLong("theModifyTimeLong", stem.getModifyTimeLong())
    		.setString("theContextId", stem.getContextId())
    		.setString("theUuid", stem.getUuid())
    		.setLong("theLastMembershipChangeDb", stem.getLastMembershipChangeDb()).executeUpdate();
  }
  
  /**
   * find all parent stems by group
   * @param groups
   * @return the groups
   */
  public Set<Stem> findParentsByGroups(Collection<Group> groups) {
    Set<String> names = GrouperUtil.findParentStemNames(groups);
    Set<Stem> stems = findByNames(names, true);
    return stems;
  }
    
  /** batch size for stems (setable for testing) */
  static int batchSize = 50;

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#findByNames(java.util.Collection, boolean)
   */
  public Set<Stem> findByNames(Collection<String> names, boolean exceptionOnNotFound)
      throws StemNotFoundException {
    if (names == null) {
      return null;
    }
    Set<Stem> stems = new LinkedHashSet<Stem>();
    if (GrouperUtil.length(names) == 0) {
      return stems;
    }
    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(names, batchSize);

    for (int i=0; i<pages; i++) {
      List<String> namePageList = GrouperUtil.batchList(names, batchSize, i);

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select theStem from Stem as theStem "
          + " where theStem.nameDb in (");

      //add all the uuids
      byHqlStatic.setCollectionInClause(query, namePageList);
      query.append(")");
      Set<Stem> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByNames")
        .listSet(Stem.class);
      if (exceptionOnNotFound && currentList.size() != namePageList.size()) {
        throw new GroupNotFoundException("Didnt find all names: " + GrouperUtil.toStringForLog(namePageList)
            + " , " + namePageList.size() + " != " + currentList.size());
      }
      
      //we want to put these in in order...
      for (String name : namePageList) {
        name = StringUtils.equals(":", name) ? Stem.ROOT_NAME : name;
        stems.add(GrouperUtil.retrieveByProperty(currentList, Stem.FIELD_NAME, name));
      }
      
    }
    return stems;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#getAllStemsWithGroupsSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Stem> getAllStemsWithGroupsSecure(GrouperSession grouperSession,
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
      throws GrouperDAOException {
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theStem.displayNameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct theStem from Stem theStem, Group theGroup ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);

    if (changedQuery) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    
    sql.append(" theGroup.parentUuid = theStem.uuid ");
    
    try {

      Set<Stem> stems = byHqlStatic.createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllStemsWithGroupsSecure")
        .options(queryOptions)
        .listSet(Stem.class);
            
      //if the hql didnt filter, this will
      Set<Stem> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterStemsWithGroups(stems, subject, inPrivSet);

      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    }
  }

  /**
   * @see StemDAO#findStemsInStemWithoutPrivilege(GrouperSession, String, Scope, Subject, Privilege, QueryOptions, boolean)
   */
  public Set<Stem> findStemsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, QueryOptions queryOptions, boolean considerAllSubject) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theStem.displayNameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct theStem from Stem theStem ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query, note, this is for the ADMIN list since the user should be able to read privs
    Set<Privilege> adminSet = GrouperUtil.toSet(NamingPrivilege.CREATE);
    grouperSession.getNamingResolver().hqlFilterStemsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sql, "theStem.uuid", adminSet);

    boolean changedQueryNotWithPriv = grouperSession.getNamingResolver().hqlFilterStemsNotWithPrivWhereClause(subject, byHqlStatic, 
        sql, "theStem.uuid", privilege, considerAllSubject);

    switch (scope) {
      case ONE:
        
        sql.append(" and theStem.parentUuid = :stemId ");
        byHqlStatic.setString("stemId", stemId);
        
        break;
        
      case SUB:
        
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, true);
        sql.append(" and theStem.nameDb like :stemPattern ");
        byHqlStatic.setString("stemPattern", stem.getName() + ":%");

        break;
        
      default:
        throw new RuntimeException("Need to pass in a scope, or its not implemented: " + scope);
    }
    
    Set<Stem> stems = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindStemsInStemWithoutPrivilege")
      .options(queryOptions)
      .listSet(Stem.class);
          
    //if the hql didnt filter, this will
    Set<Stem> filteredStems = grouperSession.getNamingResolver()
      .postHqlFilterStems(stems, grouperSession.getSubject(), adminSet);

    if (!changedQueryNotWithPriv) {
      
      //didnt do this in the query
      Set<Stem> originalList = new LinkedHashSet<Stem>(filteredStems);
      filteredStems = grouperSession.getNamingResolver()
        .postHqlFilterStems(originalList, subject, GrouperUtil.toSet(privilege));
      
      //we want the ones in the original list not in the new list
      if (filteredStems != null) {
        originalList.removeAll(filteredStems);
      }
      filteredStems = originalList;
    }
    
    return filteredStems;
    
  }

} 

