/**
 * Copyright 2014 Internet2
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
 */
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperAccessAdapter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefAssignmentType;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
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
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.stem.StemHierarchyType;
import edu.internet2.middleware.grouper.stem.StemSet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
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
   * @throws GrouperDAOException 
   * @since   
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
              
              _group.validate();
              
              byObject.save(_group);
              
              // take care of group sets
              createGroupSetsForGroup(_group);
              
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
              
              attributeDef.validate();
              
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

    _child.validate();
    
    HibernateSession.byObjectStatic().save(_child);
    
    createGroupSetsForStem(_child);
    
    createStemSetsForStem(_child.getUuid(), _child.getParentUuid());
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
    
    createStemSetsForStem(_root.getUuid(), _root.getParentUuid());
  } 
  
  /**
   * @param stemId
   * @param parentStemId
   */
  public void createStemSetsForStem(String stemId, String parentStemId) {
    
    List<StemSet> allStemSets = new LinkedList<StemSet>();

    // batch to help performance
    int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
    if (batchSize <= 0) {
      batchSize = 1;
    }
    
    // create self one first
    StemSet selfStemSet = new StemSet();
    selfStemSet.setId(GrouperUuid.getUuid());
    selfStemSet.setDepth(0);
    selfStemSet.setIfHasStemId(stemId);
    selfStemSet.setThenHasStemId(stemId);
    selfStemSet.setType(StemHierarchyType.self);
    selfStemSet.setParentStemSetId(selfStemSet.getId());
    
    allStemSets.add(selfStemSet);
   
    // now find ancestor stems
    if (parentStemId != null) {
      Set<StemSet> stemSets = GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(parentStemId);
      allStemSets.addAll(createNonSelfStemSetsForStem(new LinkedList<StemSet>(stemSets), stemId, selfStemSet));
    }
    
    for (int i = 0; i < GrouperUtil.batchNumberOfBatches(allStemSets, batchSize); i++) {
      List<StemSet> currentBatch = GrouperUtil.batchList(allStemSets, batchSize, i);
      GrouperDAOFactory.getFactory().getStemSet().saveBatch(currentBatch);
    }
  }
  
  /**
   * @param ifHasStemSetsOfParentStem
   * @param ifHasStemId
   * @param firstParentStemSet
   * @return new child stemSets
   */
  private List<StemSet> createNonSelfStemSetsForStem(List<StemSet> ifHasStemSetsOfParentStem, String ifHasStemId, StemSet firstParentStemSet) {
    
    String nextParentStemSetId = firstParentStemSet.getId();
    
    // sort by depth
    Collections.sort(ifHasStemSetsOfParentStem, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    
    List<StemSet> newChildren = new LinkedList<StemSet>();
    
    for (StemSet currIfHasStemSet : ifHasStemSetsOfParentStem) {
      StemSet childStemSet = new StemSet();
      childStemSet.setId(GrouperUuid.getUuid());
      childStemSet.setDepth(currIfHasStemSet.getDepth() + 1 + firstParentStemSet.getDepth());
      childStemSet.setThenHasStemId(currIfHasStemSet.getThenHasStemId());
      childStemSet.setIfHasStemId(ifHasStemId);
      childStemSet.setType(childStemSet.getDepth() == 1 ? StemHierarchyType.immediate : StemHierarchyType.effective);
      childStemSet.setParentStemSetId(nextParentStemSetId);
      
      newChildren.add(childStemSet);
      
      nextParentStemSetId = childStemSet.getId();
    }
    
    return newChildren;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#moveStemSets(java.util.List, java.util.List, java.lang.String, int)
   */
  public void moveStemSets(List<StemSet> ifHasStemSetsOfParentStem, List<StemSet> oldStemSets, String currentStemId, int depthOfFirstParent) {

    // we want to keep one of the oldStemSets -- with the lowest depth .. remove from the collection
    // also sort desc so they can be deleted later in order
    Collections.sort(oldStemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o2.getDepth()).compareTo(o1.getDepth());
      }
    });
    
    StemSet firstParentStemSet = oldStemSets.remove(oldStemSets.size() - 1);
    
    List<StemSet> allStemSetsForCurrentNode = createNonSelfStemSetsForStem(new LinkedList<StemSet>(ifHasStemSetsOfParentStem), currentStemId, firstParentStemSet);
    
    // batch to help performance
    int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
    if (batchSize <= 0) {
      batchSize = 1;
    }
    
    // add new stem sets
    for (int i = 0; i < GrouperUtil.batchNumberOfBatches(allStemSetsForCurrentNode, batchSize); i++) {
      List<StemSet> currentBatch = GrouperUtil.batchList(allStemSetsForCurrentNode, batchSize, i);
      GrouperDAOFactory.getFactory().getStemSet().saveBatch(currentBatch);
    }
    
    // take care of children, separately for each ifHasStemId
    Set<StemSet> allOldStemSetChildren = GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemOfStemChildrenAndMinDepth(currentStemId, depthOfFirstParent + 1, new QueryOptions().sortAsc("ss.ifHasStemId"));

    if (allOldStemSetChildren.size() > 0) {
      List<StemSet> oldStemSetChildrenSameIfId = new LinkedList<StemSet>();
      String lastIfId = null;
      for (StemSet oldStemSetChild : allOldStemSetChildren) {
        if (lastIfId != null && !oldStemSetChild.getIfHasStemId().equals(lastIfId)) {
          moveStemSets(ifHasStemSetsOfParentStem, oldStemSetChildrenSameIfId, lastIfId, depthOfFirstParent + 1);

          oldStemSetChildrenSameIfId.clear();
        }
        
        lastIfId = oldStemSetChild.getIfHasStemId();
        oldStemSetChildrenSameIfId.add(oldStemSetChild);
      }
      
      if (oldStemSetChildrenSameIfId.size() > 0) {
        moveStemSets(ifHasStemSetsOfParentStem, oldStemSetChildrenSameIfId, lastIfId, depthOfFirstParent + 1);
      }
    }
    
    // delete old stem sets
    for (StemSet stemSetToDelete : oldStemSets) {
      stemSetToDelete.delete();
    }
  }
  
  /**
   * @param group
   */
  private void createGroupSetsForGroup(Group group) {
    
    // add group sets
    Set<Field> fields = FieldFinder.findAll();
    for (Field field : fields) {
      if (field.getType().equals(FieldType.ACCESS) || Group.getDefaultList().getUuid().equals(field.getUuid())) {
        if (group.getTypeOfGroup() != null && group.getTypeOfGroup().supportsField(field)) {
          GroupSet groupSet = new GroupSet();
          groupSet.setId(GrouperUuid.getUuid());
          groupSet.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
          groupSet.setDepth(0);
          groupSet.setMemberGroupId(group.getUuid());
          groupSet.setOwnerGroupId(group.getUuid());
          groupSet.setParentId(groupSet.getId());
          groupSet.setFieldId(field.getUuid());
          GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
        }
      }
    }
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

    // delete stem sets
    GrouperDAOFactory.getFactory().getStemSet().deleteByIfHasStemId(_ns.getUuid());
    
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
        .createQuery("from Stem as ns where lower(ns.nameDb) like lower(:value) or lower(ns.alternateNameDb) like lower(:value)")
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
        .createQuery("from Stem as ns where (lower(ns.nameDb) like lower(:value) or lower(ns.alternateNameDb) like lower(:value)) and ns.nameDb like :scope")
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
    return findAllByApproximateNameAny(name, null, null);
  } 

  /**
   * if there are sort fields, go through them, and replace name with nameDb, etc,
   * extension for extensionDb, displayName with displayNameDb, and displayExtension with displayExtensionDb
   * @param querySort
   */
  public static void massageSortFields(QuerySort querySort) {
    massageSortFields(querySort, "ns");
  }

  /**
   * if there are sort fields, go through them, and replace name with nameDb, etc,
   * extension for extensionDb, displayName with displayNameDb, and displayExtension with displayExtensionDb
   * @param querySort
   * @param alias is the hql alias
   */
  public static void massageSortFields(QuerySort querySort, String alias) {
    if (querySort == null) {
      return;
    }
    for (QuerySortField querySortField : GrouperUtil.nonNull(querySort.getQuerySortFields())) {
      if (StringUtils.equals("extension", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".extensionDb");
      }
      if (StringUtils.equals("name", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".nameDb");
      }
      if (StringUtils.equals("displayExtension", querySortField.getColumn())
          || StringUtils.equals("display_extension", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".displayExtensionDb");
      }
      if (StringUtils.equals("displayName", querySortField.getColumn())
          || StringUtils.equals("display_name", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".displayNameDb");
      }
    }

  }



  /**
   * @param name 
   * @param scope 
   * @return set stems
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateNameAny(String name, String scope) {
    return findAllByApproximateNameAny(name, scope, null);
  }
  
  /**
   * @param name 
   * @param scope 
   * @param queryOptions 
   * @return set stems
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Stem> findAllByApproximateNameAny(String name, String scope, QueryOptions queryOptions)
    throws  GrouperDAOException {
    try {
      
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      
      StringBuilder hql = new StringBuilder("from Stem as ns where "
          + "(   lower(ns.nameDb)            like :name  "
          + " or lower(ns.alternateNameDb)     like :name  "
          + " or lower(ns.displayNameDb)       like :name ) ");
      
      if (!StringUtils.isBlank(scope)) {
        hql.append("and ns.nameDb like :scope ");
        byHqlStatic.setString("scope", scope + "%");
      }
      
      return byHqlStatic
        .createQuery(hql.toString())
        .options(queryOptions)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByApproximateNameAny")
        .setString("name", "%" + StringUtils.defaultString(name).toLowerCase() + "%")
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
    return findAllChildStems(ns, scope, null);
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
  public Set<Stem> findAllChildStems(Stem ns, Stem.Scope scope, boolean orderByName) {
    QueryOptions queryOptions = null;
    if (orderByName) {
      queryOptions = new QueryOptions();
      queryOptions.sortAsc("name");
    }
    return findAllChildStems(ns, scope, queryOptions);
  }
  
  /**
   * @param ns 
   * @param scope 
   * @param queryOptions 
   * @return set stem
   * @throws GrouperDAOException 
   * @see     StemDAO#findAllChildStems(Stem, Stem.Scope)
   * @throws  IllegalStateException if unknown scope.
   * @since   @HEAD@
   */
  public Set<Stem> findAllChildStems(Stem ns, Stem.Scope scope, QueryOptions queryOptions)
    throws  GrouperDAOException,
            IllegalStateException {
    return findAllChildStems(ns, scope, queryOptions, true);
  }

  /**
   * @param ns 
   * @param scope 
   * @param queryOptions 
   * @param checkSecurity
   * @return set stem
   * @throws GrouperDAOException 
   * @see     StemDAO#findAllChildStems(Stem, Stem.Scope)
   * @throws  IllegalStateException if unknown scope.
   * @since   @HEAD@
   */
  public Set<Stem> findAllChildStems(Stem ns, Stem.Scope scope, QueryOptions queryOptions, boolean checkSecurity)
    throws  GrouperDAOException,
            IllegalStateException {

    Set<Stem> stemsSet;
    try {
      if (queryOptions != null) {
        
        massageSortFields(queryOptions.getQuerySort());
        
      }

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder sql = new StringBuilder();
      if (Stem.Scope.ONE == scope) {
        sql.append("from Stem as ns where ns.parentUuid = :parent");
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        sql.append("from Stem as ns where ns.nameDb not like :stem");
      } else if (Stem.Scope.SUB == scope) {
        sql.append("from Stem as ns where ns.nameDb like :scope");
      } else {
        throw new IllegalStateException("unknown search scope: " + scope);
      }

      if (checkSecurity) {
        appendQueryFilterIfNeededViewChildObjects("ns", GrouperSession.staticGrouperSession(), sql, byHqlStatic, true);
      }
      
      if (Stem.Scope.ONE == scope) {
        stemsSet = byHqlStatic
          .createQuery(sql.toString())
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindChildStems")
          .options(queryOptions)
          .setString("parent", ns.getUuid())
          .listSet(Stem.class);
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        stemsSet = byHqlStatic
          .createQuery(sql.toString())
          .options(queryOptions)
          .setCacheable(false)
          .setCacheRegion(KLASS + ".FindChildStems")
          .setString("stem", Stem.DELIM)
          .listSet(Stem.class);
      } else if (Stem.Scope.SUB == scope) {
        stemsSet = byHqlStatic
          .createQuery(sql.toString())
          .options(queryOptions)
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
   * @param children 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void renameStemAndChildren(final Set children)
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
              return null;
            }
        
      });
    }
    catch (GrouperDAOException e) {
      String error = "Problem saving children of stem: children: " + GrouperUtil.toStringSafe(children) + ", " + e.getMessage();
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
      GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet,
      QueryOptions queryOptions)
      throws GrouperDAOException {
    return findAllChildGroupsSecure(ns, scope, grouperSession, subject, inPrivSet, queryOptions, null);
  }


  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#findAllChildGroupsSecure(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, Set)
   */
  public Set<Group> findAllChildGroupsSecure(Stem ns, Scope scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) throws GrouperDAOException {
    
    Set<Group> groupsSet;
    try {
      if (Stem.Scope.ONE == scope) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getImmediateChildrenSecure(
            grouperSession, ns, subject, inPrivSet, queryOptions, typeOfGroups);
      } else if (Stem.Scope.SUB == scope && ns.isRootStem()) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure(grouperSession, subject, inPrivSet, queryOptions, typeOfGroups);
      } else if (Stem.Scope.SUB == scope) {
        groupsSet = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure(ns.getNameDb() + Stem.DELIM, grouperSession, 
            subject, inPrivSet, queryOptions, typeOfGroups);
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
    
    if (queryOptions != null) {
      
      massageSortFields(queryOptions.getQuerySort());
      
    }
    
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
      queryOptions.sortAsc("ns.displayNameDb");
    }
  
    StringBuilder sql = new StringBuilder("select distinct ns from Stem as ns ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(subject, byHqlStatic, 
        sql, "ns.uuid", inPrivSet);
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append(" ns.parentUuid = :parent ");
    
    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }

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
    return getAllStemsSecureHelper(scope, grouperSession, subject, inPrivSet, 
        queryOptions, false, null, null, false, null, null, null, null, null, null, null);
  }

  /**
   * @param scope 
   * @param grouperSession
   * @param subject
   * @param inPrivSet
   * @param queryOptions
   * @param splitScope
   * @param parentStemId
   * @param stemScope
   * @param findByUuidOrName if we are looking by uuid or name
   * @param userHasInGroupFields find stems where the user has these fields in a group
   * @param userHasInAttributeFields find stems where the user has these fields in an attribute
   * @param totalStemIds
   * @param idOfAttributeDefName 
   * @param attributeValue 
   * @param attributeCheckReadOnAttributeDef
   * @param attributeValuesOnAssignment
   * @return the matching stems
   * @throws GrouperDAOException
   */
  private Set<Stem> getAllStemsSecureHelper(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet,
      QueryOptions queryOptions, boolean splitScope,
      String parentStemId, Scope stemScope, boolean findByUuidOrName,
      Collection<Field> userHasInGroupFields, Collection<Field> userHasInAttributeFields,
      Collection<String> totalStemIds, String idOfAttributeDefName, Object attributeValue, 
      Boolean attributeCheckReadOnAttributeDef, Set<Object> attributeValuesOnAssignment)
          throws GrouperDAOException {

    if ((attributeValue != null || GrouperUtil.length(attributeValuesOnAssignment) > 0) && StringUtils.isBlank(idOfAttributeDefName)) {
      throw new RuntimeException("If you are searching by attributeValue then you must specify an attribute definition name");
    }

    if (attributeValue != null && GrouperUtil.length(attributeValuesOnAssignment) > 0) {
      throw new RuntimeException("Cant send in attributeValue and attributeValuesOnAssignment"); 
    }
    
    if (idOfAttributeDefName == null && attributeCheckReadOnAttributeDef != null) {
      throw new RuntimeException("Cant pass attributeCheckReadOnAttributeDef if not passing idOfAttributeDefName");
    }
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("ns.displayNameDb");
    }

    Set<Stem> overallResults = new LinkedHashSet<Stem>();

    int stemBatches = GrouperUtil.batchNumberOfBatches(totalStemIds, 100);

    List<String> totalStemIdsList = new ArrayList<String>(GrouperUtil.nonNull(totalStemIds));
    
    if (subject == null && GrouperUtil.length(inPrivSet) > 0) {
      subject = GrouperSession.staticGrouperSession().getSubject();
    }
    
    for (int stemIndex = 0; stemIndex < stemBatches; stemIndex++) {
      
      List<String> stemIds = GrouperUtil.batchList(totalStemIdsList, 100, stemIndex);

      StringBuilder sql = new StringBuilder("select distinct ns from Stem ns ");
  
      if (!StringUtils.isBlank(parentStemId) || stemScope != null) {
  
        if (StringUtils.isBlank(parentStemId) || stemScope == null) {
          throw new RuntimeException("If you are passing in a parentStemId or a stemScope, then you need to pass both of them: " + parentStemId + ", " + stemScope);
        }
  
        if (stemScope == Scope.SUB) {
          sql.append(", StemSet theStemSet ");
        }
      }      
  
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
      //see if we are adding more to the query
      boolean changedQuery = false;
      
      if (GrouperUtil.length(inPrivSet) > 0) {
        changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(subject, byHqlStatic, 
            sql, "ns.uuid", inPrivSet);
      }
  
      if (findByUuidOrName && StringUtils.isBlank(scope)) {
        throw new RuntimeException("If you are looking by uuid or name, you need to pass in a scope");
      }
      
      //see if there is a scope
      if (!StringUtils.isBlank(scope)) {
        
        StringBuilder whereClause = new StringBuilder();
        
        String[] scopes = splitScope ? GrouperUtil.splitTrim(scope, " ") : new String[]{scope};
  
        if (scopes.length > 1 && findByUuidOrName) {
          throw new RuntimeException("If you are looking by uuid or name, then you can only pass in one scope: " + scope);
        }
        
        int index = 0;
        for (String theScope : scopes) {
          if (whereClause.length() > 0) {
            whereClause.append(" and ");
          } else {
            if (changedQuery) {
              whereClause.append(" and ");
            } else {
              whereClause.append(" where ");
            }
            whereClause.append(" (( ");
          }
  
          if (findByUuidOrName) {
            whereClause.append(" ns.nameDb = :scope" + index + " or ns.alternateNameDb = :scope" + index 
                + " or ns.displayNameDb = :scope" + index + " ");
            byHqlStatic.setString("scope" + index, theScope);
          } else {
            whereClause.append(" ( lower(ns.nameDb) like :scope" + index 
                + " or lower(ns.displayNameDb) like :scope" + index 
                + " or lower(ns.descriptionDb) like :scope" + index + " ) ");
            if (splitScope) {
              theScope = "%" + theScope + "%";
            } else if (!theScope.endsWith("%")) {
              theScope += "%";
            }
            byHqlStatic.setString("scope" + index, theScope.toLowerCase());
  
          }        
          
          index++;
        }
  
        whereClause.append(" ) or ( ns.uuid = :stemId  )) ");
        byHqlStatic.setString("stemId", scope);
  
        sql.append(whereClause);
        changedQuery = true;
      }

      if (!StringUtils.isBlank(idOfAttributeDefName)) {

        if (changedQuery) {
          sql.append(" and ");
        }  else {
          sql.append(" where ");
        }
        changedQuery = true;

        //make sure user can READ the attribute
        AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder().addIdOfAttributeDefName(idOfAttributeDefName);

        //default to check security
        if (attributeCheckReadOnAttributeDef == null || attributeCheckReadOnAttributeDef) {
          attributeDefNameFinder.assignPrivileges(AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
        }
        
        AttributeDefName attributeDefName = attributeDefNameFinder.findAttributeName();

        //cant read the attribute????
        if (attributeDefName == null) {
          return new HashSet<Stem>();
        }
          
        AttributeDef attributeDef = attributeDefName.getAttributeDef();
        
        if (GrouperUtil.length(attributeValuesOnAssignment) > 0) {
  
  
          sql.append(" exists ( select aav ");
          
          sql.append(" from AttributeAssign aa, AttributeAssign aaOnAssign, AttributeAssignValue aav ");
          
          sql.append(" where ns.uuid = aa.ownerStemId ");
          sql.append(" and aa.id = aaOnAssign.ownerAttributeAssignId ");
          
          sql.append(" and aaOnAssign.attributeDefNameId = :idOfAttributeDefName ");
          byHqlStatic.setString("idOfAttributeDefName", idOfAttributeDefName);
          sql.append(" and aa.enabledDb = 'T' ");
  
          AttributeDefValueType attributeDefValueType = attributeDef.getValueType();
  
          Hib3AttributeAssignDAO.queryByValuesAddTablesWhereClause(byHqlStatic, null, sql, attributeDefValueType, attributeValuesOnAssignment, "aaOnAssign");
          
          sql.append(" ) ");
          
          
        } else {
        

          sql.append(" exists ( select ");
          
          sql.append(attributeValue == null ? "aa" : "aav");
          
          sql.append(" from AttributeAssign aa ");
  
          if (attributeValue != null) {
            sql.append(", AttributeAssignValue aav ");
          }
          
          sql.append(" where ns.uuid = aa.ownerStemId ");
          sql.append(" and aa.attributeDefNameId = :idOfAttributeDefName ");
          byHqlStatic.setString("idOfAttributeDefName", idOfAttributeDefName);
          sql.append(" and aa.enabledDb = 'T' ");
  
          if (attributeValue != null) {
  
            AttributeDefValueType attributeDefValueType = attributeDef.getValueType();
  
            Hib3AttributeAssignDAO.queryByValueAddTablesWhereClause(byHqlStatic, null, sql, attributeDefValueType, attributeValue);
            
          }
          
          sql.append(" ) ");
        }        
      }
      
      
      
      if (GrouperUtil.length(stemIds) > 0) {

        if (changedQuery) {
          sql.append(" and ");
        }  else {
          sql.append(" where ");
        }
        changedQuery = true;
        sql.append(" ns.uuid in (");
        sql.append(HibUtils.convertToInClause(stemIds, byHqlStatic));
        sql.append(") ");
        
      }

      if (!StringUtils.isBlank(parentStemId) || stemScope != null) {
        if (changedQuery) {
          sql.append(" and ");
        }  else {
          sql.append(" where ");
        }
        changedQuery = true;
        switch(stemScope) {
          case ONE:
            sql.append(" ns.parentUuid = :theStemId ");
            byHqlStatic.setString("theStemId", parentStemId);
            break;
          case SUB:
            
            sql.append(" ns.parentUuid = theStemSet.ifHasStemId " +
              " and theStemSet.thenHasStemId = :theStemId ");
            byHqlStatic.setString("theStemId", parentStemId);
            
            break;
          
        }
      }
  
  
      if (GrouperUtil.length(userHasInGroupFields) > 0) {
        
        if (changedQuery) {
          sql.append(" and ");
        } else {
          sql.append(" where ");
        }
        changedQuery = true;
        
        Member membershipMember = MemberFinder.findBySubject(grouperSession, subject, false);
        
        if (membershipMember == null) {
          return new HashSet<Stem>();
        }
        
        sql.append(" exists (select 1 from Group theGroup, MembershipEntry fieldMembership " +
        		" where fieldMembership.ownerGroupId = theGroup.uuid " +
            " and theGroup.parentUuid = ns.uuid ");
        HibUtils.convertFieldsToSqlInString(userHasInGroupFields, byHqlStatic, sql, "fieldMembership.fieldId");
    		sql.append(" and fieldMembership.memberUuid = :fieldMembershipMemberUuid and fieldMembership.enabledDb = 'T' ) ");
        byHqlStatic.setString("fieldMembershipMemberUuid", membershipMember.getUuid());
        
      }
      
      if (GrouperUtil.length(userHasInAttributeFields) > 0) {
        
        if (changedQuery) {
          sql.append(" and ");
        } else {
          sql.append(" where ");
        }
        changedQuery = true;
        
        Member membershipMember = MemberFinder.findBySubject(grouperSession, subject, false);
        
        if (membershipMember == null) {
          return new HashSet<Stem>();
        }
        
        sql.append(" exists (select 1 from AttributeDef theAttributeDef, MembershipEntry fieldMembership " +
            " where fieldMembership.ownerAttrDefId = theAttributeDef.id " +
            " and theAttributeDef.stemId = ns.uuid ");
        HibUtils.convertFieldsToSqlInString(userHasInGroupFields, byHqlStatic, sql, "fieldMembership.fieldId");
        sql.append(" and fieldMembership.memberUuid = :fieldMembershipMemberUuid and fieldMembership.enabledDb = 'T' ) ");
        byHqlStatic.setString("fieldMembershipMemberUuid", membershipMember.getUuid());
        
      }

      changedQuery = appendQueryFilterIfNeededViewChildObjects("ns", grouperSession, sql, byHqlStatic, changedQuery);
      
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
  
      Set<Stem> stems = byHqlStatic.createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllStemsSecure")
        .options(queryOptions)
        .listSet(Stem.class);
      
      //if the hql didnt filter, this will
      Set<Stem> tempStems = grouperSession.getNamingResolver()
        .postHqlFilterStems(stems, subject, inPrivSet);
      
      overallResults.addAll(GrouperUtil.nonNull(tempStems));
    }
      
    //if find by uuid or name, try to narrow down to one...
    if (findByUuidOrName) {
      
      //get the one with uuid
      for (Stem stem : overallResults) {
        if (StringUtils.equals(scope, stem.getId())) {
          return GrouperUtil.toSet(stem);
        }
      }
      
      //get the one with name
      for (Stem stem : overallResults) {
        if (StringUtils.equals(scope, stem.getName())) {
          return GrouperUtil.toSet(stem);
        }
      }
      
      //get the one with alternate name
      for (Stem stem : overallResults) {
        if (StringUtils.equals(scope, stem.getAlternateName())) {
          return GrouperUtil.toSet(stem);
        }
      }
      
    }
    
    return overallResults;
  }

  /** 
   * people with true here will see all folders
   * TODO replace with ehcache
   */
  private static ExpirableCache<MultiKey, Boolean> showAllFoldersCache = new ExpirableCache<MultiKey, Boolean>(2);
  
  
  /**
   * @param stemVariable
   * @param grouperSession
   * @param sql
   * @param byHqlStatic
   * @param changedQuery
   * @return if changed
   */
  private boolean appendQueryFilterIfNeededViewChildObjects(String stemVariable, GrouperSession grouperSession, StringBuilder sql, ByHqlStatic byHqlStatic, boolean changedQuery) {
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("security.show.folders.where.user.can.see.subobjects", true)) {
      
      if (!PrivilegeHelper.isWheelOrRootOrViewonlyRoot(grouperSession.getSubject())) {
        
        //exclude superusers with lots of privileges if their performance is bad
        final String excludesGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.show.all.folders.if.in.group");
        Boolean exclude = false;
        
        if (!StringUtils.isBlank(excludesGroupName)) {

          final MultiKey userKey = new MultiKey(grouperSession.getSubject().getSourceId(), grouperSession.getSubject().getId());
          exclude = showAllFoldersCache.get(userKey);
          
          if (exclude == null) {

            final Subject SUBJECT = grouperSession.getSubject();

            exclude = (Boolean)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {

              public Object callback(GrouperSession grouperSession)
                  throws GrouperSessionException {
                Group group = GroupFinder.findByName(grouperSession, excludesGroupName, true);
                return group.hasMember(SUBJECT);
              }

            });
            
            showAllFoldersCache.put(userKey, exclude);
          }
        }
        
        if (!exclude) {
          
          long startMillis = System.currentTimeMillis();
          try {
            if (changedQuery) {
              sql.append(" and ");
            } else {
              sql.append(" where ");
            }
            changedQuery = true;
            
            sql.append(" ( ");
            
            Member allMember = MemberFinder.internal_findAllMember();
            Member member = MemberFinder.internal_findBySubject(grouperSession.getSubject(), null, false);
            Set<String> memberIds = GrouperUtil.toSet(allMember.getUuid());
            if (member != null) {
              memberIds.add(member.getUuid());
            }
    
            {
              sql.append(" exists (select 1 from Group theGroup, MembershipEntry fieldMembership, StemSet stemSet " +
                  " where fieldMembership.ownerGroupId = theGroup.uuid " +
                  " and stemSet.ifHasStemId = theGroup.parentUuid " +
                  " and stemSet.thenHasStemId = " + stemVariable + ".uuid and fieldMembership.fieldId in (");
              //look for groups where the user or GrouperAll has a privilege
              Collection<String> accessPrivs = PrivilegeHelper.fieldIdsFromPrivileges(AccessPrivilege.ALL_PRIVILEGES);
              String accessInClause = HibUtils.convertToInClause(accessPrivs, byHqlStatic);
              
              sql.append(accessInClause).append(") and fieldMembership.memberUuid in (");
              
              String memberInClause = HibUtils.convertToInClause(memberIds, byHqlStatic);
              sql.append(memberInClause).append(")");
              
              // don't return disabled memberships
              sql.append(" and fieldMembership.enabledDb = 'T'");
              
              sql.append(" ) ");
            }
            
            {
              sql.append(" or exists (select 1 from Stem theStem543, MembershipEntry fieldMembership, StemSet stemSet " +
                  " where fieldMembership.ownerStemId = theStem543.uuid " +
                  " and stemSet.ifHasStemId in (theStem543.parentUuid, theStem543.uuid) " +
                  " and stemSet.thenHasStemId = " + stemVariable + ".uuid and fieldMembership.fieldId in (");
              //look for groups where the user or GrouperAll has a privilege
              Collection<String> accessPrivs = PrivilegeHelper.fieldIdsFromPrivileges(NamingPrivilege.ALL_PRIVILEGES);
              String accessInClause = HibUtils.convertToInClause(accessPrivs, byHqlStatic);
              
              sql.append(accessInClause).append(") and fieldMembership.memberUuid in (");
              
              String memberInClause = HibUtils.convertToInClause(memberIds, byHqlStatic);
              sql.append(memberInClause).append(")");
              
              // don't return disabled memberships
              sql.append(" and fieldMembership.enabledDb = 'T'");
              
              sql.append(" ) ");
            }
            
            {
              sql.append(" or exists (select 1 from AttributeDef theAttributeDef, MembershipEntry fieldMembership, StemSet stemSet " +
                  " where fieldMembership.ownerAttrDefId = theAttributeDef.id " +
                  " and stemSet.ifHasStemId = theAttributeDef.stemId " +
                  " and stemSet.thenHasStemId = " + stemVariable + ".uuid and fieldMembership.fieldId in (");
              //look for groups where the user or GrouperAll has a privilege
              Collection<String> accessPrivs = PrivilegeHelper.fieldIdsFromPrivileges(AttributeDefPrivilege.ALL_PRIVILEGES);
              String accessInClause = HibUtils.convertToInClause(accessPrivs, byHqlStatic);
              
              sql.append(accessInClause).append(") and fieldMembership.memberUuid in (");
              
              String memberInClause = HibUtils.convertToInClause(memberIds, byHqlStatic);
              sql.append(memberInClause).append(")");
              
              // don't return disabled memberships
              sql.append(" and fieldMembership.enabledDb = 'T'");
              
              sql.append(" ) ");
            }
            
            
            sql.append(" ) ");
          } finally {
            int logAfterSeconds = GrouperConfig.retrieveConfig().propertyValueInt("security.show.all.folders.log.above.seconds", -1);
            if (logAfterSeconds > 0) {
              int seconds = (int)((System.currentTimeMillis() - startMillis) / 1000);
              if (seconds > logAfterSeconds && logAfterSeconds > -1) {
                LOG.error("Showing folders securely too too long.  It took " + seconds
                    + " seconds but max configured limit is " + logAfterSeconds + " seconds, for user: " 
                    + grouperSession.getSubject().getSourceId() + ":" + grouperSession.getSubject().getId() + ", "
                    + grouperSession.getSubject().getName() + ", " + grouperSession.getSubject().getDescription());
              }
            }
          }
        }        
      }
    }
    return changedQuery;
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
              
              attributeDefName.validate();
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
    
    List<String> stemIds = GrouperUtil.listFromCollection(GrouperDAOFactory.getFactory().getGroupSet().findAllOwnerStemsByMemberGroup(groupId));
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
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }

      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.nameDb = :name or ns.alternateNameDb = :name")
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
   * @param name
   * @param exceptionIfNull
   * @param queryOptions 
   * @return stem
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   */
  public Stem findByCurrentName(String name, boolean exceptionIfNull, QueryOptions queryOptions) 
    throws  GrouperDAOException,
            StemNotFoundException {
    try {
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.nameDb = :name")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByCurrentName")
        .options(queryOptions)
        .setString("name", name)
        .uniqueResult(Stem.class);
      if (stemDto == null && exceptionIfNull) {
        throw new StemNotFoundException("Can't find stem by current name: '" + name + "'");
      }
      return stemDto;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find stem by current name: '" 
        + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
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
  public Stem findByAlternateName(String name, boolean exceptionIfNull, QueryOptions queryOptions) 
    throws  GrouperDAOException,
            StemNotFoundException {
    try {
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.alternateNameDb = :name")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByAlternateName")
        .options(queryOptions)
        .setString("name", name)
        .uniqueResult(Stem.class);
      if (stemDto == null && exceptionIfNull) {
        throw new StemNotFoundException("Can't find stem by alternate name: '" + name + "'");
      }
      return stemDto;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find stem by alternate name: '" 
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
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
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
    return findByUuidOrName(uuid, name, exceptionIfNull, null);
  }
    
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean, QueryOptions)
   */
  public Stem findByUuidOrName(String uuid, String name, boolean exceptionIfNull, QueryOptions queryOptions)
      throws GrouperDAOException, StemNotFoundException {
    
    if (StringUtils.equals(name, ":") || StringUtils.isBlank(name)) {
      return StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    }
    
    try {
      Stem stemDto = HibernateSession.byHqlStatic()
        .createQuery("from Stem as ns where ns.uuid = :uuid or ns.nameDb = :name")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .options(queryOptions)
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
    
    List<String> namesList = GrouperUtil.listFromCollection(names);
    
    Set<Stem> stems = new LinkedHashSet<Stem>();
    if (GrouperUtil.length(names) == 0) {
      return stems;
    }
    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(namesList, batchSize);

    for (int i=0; i<pages; i++) {
      List<String> namePageList = GrouperUtil.batchList(namesList, batchSize, i);

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select ns from Stem as ns "
          + " where ns.nameDb in (");

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
      queryOptions.sortAsc("ns.displayNameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct ns from Stem ns, Group theGroup ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);

    if (changedQuery) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    
    sql.append(" theGroup.parentUuid = ns.uuid ");
    
    try {

      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
      
      String sqlString = sql.toString();
      Set<Stem> stems = new HashSet<Stem>();
      
      if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
        stems = byHqlStatic.createQuery(sqlString)
          .setCacheable(false)
          .setCacheRegion(KLASS + ".GetAllStemsWithGroupsSecure")
          .options(queryOptions)
          .listSet(Stem.class);
      }
            
      //if the hql didnt filter, this will
      Set<Stem> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterStemsWithGroups(stems, subject, inPrivSet);

      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    }
  }

  /**
   * @see StemDAO#findStemsInStemWithoutPrivilege(GrouperSession, String, Scope, Subject, Privilege, QueryOptions, boolean, String)
   */
  public Set<Stem> findStemsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, 
      Privilege privilege, QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("ns.displayNameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct ns from Stem ns ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query, note, this is for the ADMIN list since the user should be able to read privs
    Set<Privilege> adminSet = GrouperUtil.toSet(NamingPrivilege.CREATE);
    grouperSession.getNamingResolver().hqlFilterStemsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sql, "ns.uuid", adminSet);

    boolean changedQueryNotWithPriv = grouperSession.getNamingResolver().hqlFilterStemsNotWithPrivWhereClause(subject, byHqlStatic, 
        sql, "ns.uuid", privilege, considerAllSubject);

    if (!StringUtils.isBlank(sqlLikeString)) {
      sql.append(" and ns.nameDb like :sqlLikeString ");
      byHqlStatic.setString("sqlLikeString", sqlLikeString);
    }
    

    switch (scope) {
      case ONE:
        
        sql.append(" and ns.parentUuid = :stemId ");
        byHqlStatic.setString("stemId", stemId);
        
        break;
        
      case SUB:
        
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, true);
        sql.append(" and ns.nameDb like :stemPattern ");
        byHqlStatic.setString("stemPattern", stem.getName() + ":%");

        break;
        
      default:
        throw new RuntimeException("Need to pass in a scope, or its not implemented: " + scope);
    }
    
    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
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

  /**
   * @see StemDAO#getAllStemsSplitScopeSecure(String, GrouperSession, Subject, Set, QueryOptions)
   * @Override
   */
  public Set<Stem> getAllStemsSplitScopeSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions) {
    return this.getAllStemsSecureHelper(scope, grouperSession, subject, privileges, 
        queryOptions, true, null, null, false, null, null, null, null, null, null, null);
  }

  /**
   * @see StemDAO#findByUuids(Collection, QueryOptions)
   */
  public Set<Stem> findByUuids(Collection<String> uuids, QueryOptions queryOptions) {
    int uuidsLength = GrouperUtil.length(uuids);
    if (uuidsLength > 100) {
      throw new RuntimeException("Dont pass more than 100 ids: " + uuidsLength);
    }
    
    Set<Stem> results = new LinkedHashSet<Stem>();
    
    if (uuidsLength == 0) {
      return results;
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder();
    
    sql.append("from Stem as ns where ns.uuid in (");
    
    sql.append(HibUtils.convertToInClause(uuids, byHqlStatic));
    sql.append(") ");
   
    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }

    Set<Stem> stems = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByUuids")
      .options(queryOptions)
      .listSet(Stem.class);

    return stems;
  }

  /**
   * not a secure method, find by id index
   * @see StemDAO#findByIdIndex(Long, boolean, QueryOptions)
   */
  @Override
  public Stem findByIdIndex(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws GroupNotFoundException {
    
    StringBuilder hql = new StringBuilder("select theStem from Stem as theStem where (theStem.idIndex = :theIdIndex)");
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true).setCacheRegion(KLASS + ".FindByIdIndex").options(queryOptions);
    
    byHqlStatic.createQuery(hql.toString());
    
    Stem stem = byHqlStatic.setLong("theIdIndex", idIndex).uniqueResult(Stem.class);

    //handle exceptions out of data access method...
    if (stem == null && exceptionIfNotFound) {
      throw new StemNotFoundException("Cannot find stem with idIndex: '" + idIndex + "'");
    }
    return stem;
    
  }

  /**
   * @see StemDAO#getAllStemsSecure(String, GrouperSession, Subject, Set, QueryOptions, boolean, String, Scope, boolean, Collection, Collection, Collection)
   */
  @Override
  public Set<Stem> getAllStemsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions,
      boolean splitScope, String parentStemId, Scope stemScope, boolean findByUuidOrName,
      Collection<Field> userHasInGroupFields, Collection<Field> userHasInAttributeFields,
      Collection<String> totalStemIds)
      throws GrouperDAOException {
    return getAllStemsSecureHelper(scope, grouperSession, subject, inPrivSet, queryOptions, 
        splitScope, parentStemId, stemScope, findByUuidOrName, userHasInGroupFields, userHasInAttributeFields,
        totalStemIds, null, null, null, null);
  }

  /**
   * @see StemDAO#getAllStemsSecure(String, GrouperSession, Subject, Set, QueryOptions, boolean, String, Scope, boolean, Collection, Collection, Collection, String, Object, Boolean)
   */
  @Override
  public Set<Stem> getAllStemsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions,
      boolean splitScope, String parentStemId, Scope stemScope, boolean findByUuidOrName,
      Collection<Field> userHasInGroupFields, Collection<Field> userHasInAttributeFields,
      Collection<String> totalStemIds, String idOfAttributeDefName, Object attributeValue, Boolean attributeCheckReadOnAttributeDef)
      throws GrouperDAOException {
    return getAllStemsSecureHelper(scope, grouperSession, subject, inPrivSet, queryOptions, 
        splitScope, parentStemId, stemScope, findByUuidOrName, userHasInGroupFields, userHasInAttributeFields,
        totalStemIds, idOfAttributeDefName, attributeValue, attributeCheckReadOnAttributeDef, null);
  }

  @Override
  public Set<Stem> getAllStemsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions,
      boolean splitScope, String parentStemId, Scope stemScope, boolean findByUuidOrName,
      Collection<Field> userHasInGroupFields, Collection<Field> userHasInAttributeFields,
      Collection<String> stemIds, String idOfAttributeDefName, Object attributeValue,
      Boolean attributeCheckReadOnAttributeDef, Set<Object> attributeValuesOnAssignment) throws GrouperDAOException {
    return getAllStemsSecureHelper(scope, grouperSession, subject, inPrivSet, queryOptions, 
        splitScope, parentStemId, stemScope, findByUuidOrName, userHasInGroupFields, userHasInAttributeFields,
        stemIds, idOfAttributeDefName, attributeValue, attributeCheckReadOnAttributeDef, attributeValuesOnAssignment);

  }

} 

