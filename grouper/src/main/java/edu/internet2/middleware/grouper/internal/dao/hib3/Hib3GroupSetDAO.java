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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GroupSetNotFoundException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupSetDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * @author shilen
 * @version $Id: Hib3GroupSetDAO.java,v 1.10 2009-10-20 14:55:50 shilen Exp $
 */
public class Hib3GroupSetDAO extends Hib3DAO implements GroupSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3GroupSetDAO.class.getName();


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#save(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public void save(GroupSet groupSet) {
    HibernateSession.byObjectStatic().save(groupSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#save(java.util.Set)
   */
  public void save(Set<GroupSet> groupSets) {
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      save(iter.next());
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#saveBatch(java.util.Set)
   */
  public void saveBatch(Set<GroupSet> groupSets) {
    HibernateSession.byObjectStatic().saveBatch(groupSets);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#delete(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public void delete(final GroupSet groupSet) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            /*
            // Self groupSets should not be deleted using this method so i don't think
            // this problem exists here.  Setting parent_id to null could cause a 
            // constraint violation since it's part of the unique index.
            
            //set parent to null so mysql doest get mad
            //http://bugs.mysql.com/bug.php?id=15746
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
                "update GroupSet set parentId = null where id = :id")
                .setString("id", groupSet.getId()).executeUpdate();
            */
            
            
            hibernateHandlerBean.getHibernateSession().byObject().delete(groupSet);
            return null;

          }
      
    });

  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#delete(java.util.Set)
   */
  public void delete(Set<GroupSet> groupSets) {
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      delete(iter.next());
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#update(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public void update(GroupSet groupSet) {
    HibernateSession.byObjectStatic().update(groupSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#update(java.util.Set)
   */
  public void update(Set<GroupSet> groupSets) {
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      update(iter.next());
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#deleteSelfByOwnerGroupAndField(java.lang.String, java.lang.String)
   */
  public void deleteSelfByOwnerGroupAndField(final String groupId, final String field) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            Set<GroupSet> groupSets = hibernateHandlerBean.getHibernateSession().byHql().createQuery("from GroupSet where ownerGroupId = :id and fieldId = :field and depth='0'")
                .setString("id", groupId)
                .setString("field", field)
                .listSet(GroupSet.class);

            for (GroupSet groupSet : groupSets) {
              if (GrouperDdlUtils.isMysql()) {
                //do this since mysql cant handle self-referential foreign keys
                groupSet.setParentId(null);
                hibernateHandlerBean.getHibernateSession().byObject().update(groupSet);
              }

              hibernateHandlerBean.getHibernateSession().byObject().delete(groupSet);
            }

            return null;
          }
        });
  }



  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#deleteSelfByOwnerStem(java.lang.String)
   */
  public void deleteSelfByOwnerStem(final String stemId) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            Set<GroupSet> groupSets = hibernateHandlerBean.getHibernateSession().byHql().createQuery("from GroupSet where ownerStemId = :id and depth='0'")
                .setString("id", stemId)
                .listSet(GroupSet.class);

            for (GroupSet groupSet : groupSets) {
              if (GrouperDdlUtils.isMysql()) {
                //do this since mysql cant handle self-referential foreign keys
                groupSet.setParentId(null);
                hibernateHandlerBean.getHibernateSession().byObject().update(groupSet);
              }

              hibernateHandlerBean.getHibernateSession().byObject().delete(groupSet);
            }
            
            return null;
          }
        });

  }

  
  /**
   * reset group set
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    Session hs = hibernateSession.getSession();

    // Find the root stem
    Stem rootStem = GrouperDAOFactory.getFactory().getStem().findByName(Stem.ROOT_INT, true);
    
    // get the max depth
    int maxDepth = HibernateSession.byHqlStatic().createQuery("select max(depth) from GroupSet").uniqueResult(Integer.class);
    
    // delete the rows that don't have self reference by the depth desc
    for (int i = maxDepth; i > 0; i--) {
      hs.createQuery("delete from GroupSet where depth = :depth").setInteger("depth", i).executeUpdate();
    }
    
    // set parent_id to null due to mysql bug -- http://bugs.mysql.com/bug.php?id=15746
    // also note that parent_id is part of a unique constraint, but now that we have only self groupSets, that doesn't matter.
    hs.createQuery("update GroupSet set parentId = null where ownerStemId not like :owner or ownerStemId is null")
        .setString("owner", rootStem.getUuid())
        .executeUpdate();
    
    hs.createQuery("delete from GroupSet where parentId = null")
        .executeUpdate();
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByGroupOwnerAndField(java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public Set<GroupSet> findAllByGroupOwnerAndField(String groupId, Field field) {
    Set<GroupSet> groupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.ownerGroupId = :owner and gs.fieldId = :fuuid")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllByGroupOwnerAndField")
        .setString("owner", groupId)
        .setString("fuuid", field.getUuid())
        .listSet(GroupSet.class);

    return groupSets;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findParentGroupSet(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public GroupSet findParentGroupSet(GroupSet groupSet) {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindParentGroupSet")
      .setString("id", groupSet.getParentId())
      .uniqueResult(GroupSet.class);
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByMemberGroup(java.lang.String)
   */
  public Set<GroupSet> findAllByMemberGroup(String groupId) {
    Set<GroupSet> groupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.memberGroupId = :member and gs.type = 'effective'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllByMemberGroup")
        .setString("member", groupId)
        .listSet(GroupSet.class);

    return groupSets;
  }
  


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByMemberGroupAndField(java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public Set<GroupSet> findAllByMemberGroupAndField(String memberId, Field field) {
    Set<GroupSet> groupSets = HibernateSession
    .byHqlStatic()
    .createQuery("select gs from GroupSet as gs where gs.memberGroupId = :member and gs.fieldId = :field and gs.type = 'effective'")
    .setCacheable(false).setCacheRegion(KLASS + ".FindAllByMemberGroupAndField")
    .setString("member", memberId)
    .setString("field", field.getUuid())
    .listSet(GroupSet.class);

return groupSets;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findById(java.lang.String)
   */
  public GroupSet findById(String groupSetId) {
    GroupSet groupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", groupSetId)
      .uniqueResult(GroupSet.class);
    
    if (groupSet == null) {
      throw new GroupSetNotFoundException("Didn't find groupSet with id: " + groupSetId);
    }
    
    return groupSet;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllChildren(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public Set<GroupSet> findAllChildren(GroupSet groupSet) {
    Set<GroupSet> allChildren = new LinkedHashSet<GroupSet>();
    Set<GroupSet> children = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.parentId = :parent and gs.type = 'effective'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllChildren")
        .setString("parent", groupSet.getId())
        .listSet(GroupSet.class);
    
    Iterator<GroupSet> iter = children.iterator();
    
    while (iter.hasNext()) {
      GroupSet child = iter.next();
      allChildren.addAll(findAllChildren(child));
      allChildren.add(child);
    }
    
    return allChildren;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findImmediateChildByParentAndMemberGroup(edu.internet2.middleware.grouper.group.GroupSet, java.lang.String)
   */
  public GroupSet findImmediateChildByParentAndMemberGroup(GroupSet parentGroupSet,
      String memberGroupId) {
    
    int depth = parentGroupSet.getDepth() + 1;
    
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.parentId = :id and gs.memberGroupId = :memberGroupId and gs.depth = :depth")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildByParentAndMemberGroup")
      .setString("id", parentGroupSet.getId())
      .setString("memberGroupId", memberGroupId)
      .setInteger("depth", depth)
      .uniqueResult(GroupSet.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findSelfGroup(java.lang.String, java.lang.String)
   */
  public GroupSet findSelfGroup(String groupId, String fieldId) {
    GroupSet groupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerGroupId = :id and memberGroupId = :id and fieldId = :field and depth='0'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindSelfGroup")
      .setString("id", groupId)
      .setString("field", fieldId)
      .uniqueResult(GroupSet.class);
    
    if (groupSet == null) {
      throw new GroupSetNotFoundException("Didn't find groupSet of depth 0 with owner and member: " + groupId);
    }
    
    return groupSet;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findSelfStem(java.lang.String, java.lang.String)
   */
  public GroupSet findSelfStem(String stemId, String fieldId) {
    GroupSet groupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerStemId = :id and memberStemId = :id and fieldId = :field and depth='0'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindSelfStem")
      .setString("id", stemId)
      .setString("field", fieldId)
      .uniqueResult(GroupSet.class);
    
    if (groupSet == null) {
      throw new GroupSetNotFoundException("Didn't find groupSet of depth 0 with owner and member: " + stemId);
    }
    
    return groupSet;
  }
  
  public Set<GroupSet> findAllSelfGroupSetsByOwnerWherePITFieldExists(String ownerId) {
    Set<GroupSet> groupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerId = :ownerId and gs.depth='0' and gs.fieldId in (select sourceId from PITField)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindAllSelfGroupSetsByOwnerWherePITFieldExists")
      .setString("ownerId", ownerId)
      .listSet(GroupSet.class);
    
    return groupSets;
  }
  
  public Set<GroupSet> findAllSelfGroupSetsByFieldWherePITGroupExists(String fieldId) {
    Set<GroupSet> groupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.fieldId = :fieldId and gs.depth='0' and gs.ownerId in (select sourceId from PITGroup)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindAllSelfGroupSetsByFieldWherePITGroupExists")
      .setString("fieldId", fieldId)
      .listSet(GroupSet.class);
    
    return groupSets;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#deleteSelfByOwnerGroup(edu.internet2.middleware.grouper.Group)
   */
  public void deleteSelfByOwnerGroup(final Group group) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            Set<GroupSet> groupSets = hibernateHandlerBean.getHibernateSession().byHql().createQuery("from GroupSet where ownerGroupId = :id and depth='0'")
                .setString("id", group.getId())
                .listSet(GroupSet.class);

            for (GroupSet groupSet : groupSets) {
              if (GrouperDdlUtils.isMysql()) {
                //do this since mysql cant handle self-referential foreign keys
                groupSet.setParentId(null);
                hibernateHandlerBean.getHibernateSession().byObject().update(groupSet);
              }

              hibernateHandlerBean.getHibernateSession().byObject().delete(groupSet);
            }
            
            return null;
          }
        });
  }



  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findImmediateByOwnerGroupAndMemberGroupAndField(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public GroupSet findImmediateByOwnerGroupAndMemberGroupAndField(String ownerGroupId,
      String memberGroupId, Field field) {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerGroupId = :ownerGroupId and gs.memberGroupId = :memberGroupId and fieldId = :field and type = 'effective' and depth = '1'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateByOwnerGroupAndMemberGroupAndField")
      .setString("ownerGroupId", ownerGroupId)
      .setString("memberGroupId", memberGroupId)
      .setString("field", field.getUuid())
      .uniqueResult(GroupSet.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findImmediateByOwnerStemAndMemberGroupAndField(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public GroupSet findImmediateByOwnerStemAndMemberGroupAndField(String ownerStemId,
      String memberGroupId, Field field) {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerStemId = :ownerStemId and gs.memberGroupId = :memberGroupId and fieldId = :field and type = 'effective' and depth = '1'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateByOwnerStemAndMemberGroupAndField")
      .setString("ownerStemId", ownerStemId)
      .setString("memberGroupId", memberGroupId)
      .setString("field", field.getUuid())
      .uniqueResult(GroupSet.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByCreator(edu.internet2.middleware.grouper.Member)
   */
  public Set<GroupSet> findAllByCreator(Member member) {
    Set<GroupSet> groupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.creatorId = :member")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllByCreator")
        .setString("member", member.getUuid())
        .listSet(GroupSet.class);

    return groupSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findMissingSelfGroupSetsForGroups()
   */
  public Set<Object[]> findMissingSelfGroupSetsForGroups() {
    String sql = "select g, f from Field as f, Group as g " +
        "where (f.name = 'members' or f.typeString = 'access') " +
        //for entities, dont put in group sets for members, optins, optouts, updaters, readers
        "and (g.typeOfGroupDb != 'entity' or (f.name = 'admins' or f.name = 'viewers' or f.name = 'groupAttrReaders' or f.name = 'groupAttrUpdaters')) " +
        "and not exists " +
        "(select gs.ownerGroupId from GroupSet as gs where gs.ownerGroupId = g.id and gs.fieldId = f.uuid and gs.depth='0')";
    
    Set<Object[]> missing = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMissingSelfGroupSetsForGroups")
      .listSet(Object[].class);
    
    return missing;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findMissingSelfGroupSetsForGroupsWithCustomFields()
   */
  public Set<Object[]> findMissingSelfGroupSetsForGroupsWithCustomFields() {
    
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");  
    
    String sql = "select g, f from AttributeAssignValue v, Field f, AttributeAssign valueAssign, AttributeDefName groupType, AttributeAssign groupTypeAssign, Group g where " +
    		         "v.valueString = f.uuid and " +
    		         "v.attributeAssignId = valueAssign.id and " +
    		         "valueAssign.ownerAttributeDefId = groupType.attributeDefId and " +
    		         "groupType.nameDb like :groupTypePrefix and " +
    		         "groupTypeAssign.attributeDefNameId=groupType.id and " +
    		         "groupTypeAssign.ownerGroupId = g.id and " +
    		         "g.typeOfGroupDb != 'entity' " +
                 "and not exists " +
                 "(select gs.ownerGroupId from GroupSet as gs where gs.ownerGroupId = g.id and gs.fieldId = f.uuid and gs.depth='0')";
    
    Set<Object[]> missing = HibernateSession
        .byHqlStatic()
        .createQuery(sql)
        .setString("groupTypePrefix", stemName + ":" + groupTypePrefix + "%")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindMissingSelfGroupSetsForGroupsWithCustomFields")
        .listSet(Object[].class);
    
    return missing;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findMissingSelfGroupSetsForStems()
   */
  public Set<Object[]> findMissingSelfGroupSetsForStems() {
    
    String sql = "select s, f from Field as f, Stem as s " +
                 "where f.typeString = 'naming' " +
                 "and not exists " +
                 "(select gs.ownerStemId from GroupSet as gs where gs.ownerStemId = s.id and gs.fieldId = f.uuid and gs.depth='0')";
    
    Set<Object[]> missing = HibernateSession
        .byHqlStatic()
        .createQuery(sql)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindMissingSelfGroupSetsForStems")
        .listSet(Object[].class);
    
    return missing;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#deleteSelfByOwnerAttrDef(java.lang.String)
   */
  public void deleteSelfByOwnerAttrDef(final String attrDefId) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            Set<GroupSet> groupSets = hibernateHandlerBean.getHibernateSession().byHql().createQuery("from GroupSet where ownerAttrDefId = :id and depth='0'")
                .setString("id", attrDefId)
                .listSet(GroupSet.class);

            for (GroupSet groupSet : groupSets) {
              if (GrouperDdlUtils.isMysql()) {
                //do this since mysql cant handle self-referential foreign keys
                groupSet.setParentId(null);
                hibernateHandlerBean.getHibernateSession().byObject().update(groupSet);
              }

              hibernateHandlerBean.getHibernateSession().byObject().delete(groupSet);
            }

            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findImmediateByOwnerAttrDefAndMemberGroupAndField(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public GroupSet findImmediateByOwnerAttrDefAndMemberGroupAndField(
      String ownerAttrDefId, String memberGroupId, Field field) {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerAttrDefId = :ownerAttrDefId " +
      		"and gs.memberGroupId = :memberGroupId and fieldId = :field and type = 'effective' and depth = '1'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateByOwnerAttrDefAndMemberGroupAndField")
      .setString("ownerAttrDefId", ownerAttrDefId)
      .setString("memberGroupId", memberGroupId)
      .setString("field", field.getUuid())
      .uniqueResult(GroupSet.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findMissingSelfGroupSetsForAttrDefs()
   */
  public Set<Object[]> findMissingSelfGroupSetsForAttrDefs() {
    String sql = "select a, f from Field as f, AttributeDef as a " +
        "where f.typeString = 'attributeDef' " +
        "and not exists " +
        "(select gs.ownerAttrDefId from GroupSet as gs where gs.ownerAttrDefId = a.id and gs.fieldId = f.uuid and gs.depth='0')";
    
    Set<Object[]> missing = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMissingSelfGroupSetsForAttrDefs")
      .listSet(Object[].class);
    
    return missing;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findSelfAttrDef(java.lang.String, java.lang.String)
   */
  public GroupSet findSelfAttrDef(String attrDefId, String fieldId) {
    GroupSet groupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerAttrDefId = :id " +
      		"and memberAttrDefId = :id and fieldId = :field and depth='0'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindSelfAttrDef")
      .setString("id", attrDefId)
      .setString("field", fieldId)
      .uniqueResult(GroupSet.class);
    
    if (groupSet == null) {
      throw new GroupSetNotFoundException("Didn't find groupSet of depth 0 with owner and member: " + attrDefId);
    }
    
    return groupSet;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllOwnerGroupsByMemberGroup(java.lang.String)
   */
  public Set<String> findAllOwnerGroupsByMemberGroup(String groupId) {
    Set<String> ownerGroupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct ownerGroupId from GroupSet where memberGroupId = :groupId and ownerGroupId is not null")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllOwnerGroupByMemberGroup")
      .setString("groupId", groupId)
      .listSet(String.class);
    
    return ownerGroupSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByParentId(java.lang.String)
   */
  public Set<GroupSet> findAllByParentId(String id) {
    Set<GroupSet> groupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.parentId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllByParentId")
        .setString("id", id)
        .listSet(GroupSet.class);

    return groupSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllOwnerStemsByMemberGroup(java.lang.String)
   */
  public Set<String> findAllOwnerStemsByMemberGroup(String groupId) {
    Set<String> ownerStemSet = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct ownerStemId from GroupSet where memberGroupId = :groupId and ownerStemId is not null")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllOwnerStemByMemberGroup")
      .setString("groupId", groupId)
      .listSet(String.class);
  
    return ownerStemSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findByOwnerMemberFieldParentAndType(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public GroupSet findByOwnerMemberFieldParentAndType(String ownerId, String memberId, String fieldId, 
      String parentId, String mshipType, boolean exceptionIfNotFound) {
    
    GroupSet groupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where memberId = :memberId and fieldId = :fieldId and " +
      		"ownerId = :ownerId and parentId = :parentId and type = :type")
      .setCacheable(true).setCacheRegion(KLASS + ".FindByOwnerMemberFieldParentAndType")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .setString("parentId", parentId)
      .setString("type", mshipType)
      .uniqueResult(GroupSet.class);
    
    if (groupSet == null && exceptionIfNotFound) {
      throw new GroupSetNotFoundException("Didn't find groupSet with ownerId= " + ownerId + ", memberId= " + memberId + ", fieldId= " + fieldId 
          + ", parentId= " + parentId + ", mshipType= " + mshipType);
    }
    
    return groupSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByOwnerAndMemberAndField(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<GroupSet> findAllByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId) {
    Set<GroupSet> gs = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet gs where ownerId = :ownerId and memberId = :memberId and fieldId = :fieldId")
      .setCacheable(false)
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .listSet(GroupSet.class);
    
    return gs;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findTypeMismatch()
   */
  public Set<GroupSet> findTypeMismatch() {
    String sql = "select distinct gs from GroupSet gs " +
        "where gs.fieldId = :fieldId " +
        "and gs.depth = '0' " +
        "and ((gs.type <> 'composite' " +
        "     and exists (select 1 from Composite c where c.factorOwnerUuid = gs.ownerId)) " +
        "or (gs.type = 'composite' " + 
        "    and not exists (select 1 from Composite c where c.factorOwnerUuid = gs.ownerId))) ";

    Set<GroupSet> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(GroupSet.class);
    
    return results;  
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findDuplicateSelfGroupSets()
   */
  public Set<GroupSet> findDuplicateSelfGroupSets() {
    Set<GroupSet> duplicates = new LinkedHashSet<GroupSet>();
    
    String sql = "select ownerId, memberId, fieldId from GroupSet where depth='0' group by ownerId, memberId, fieldId having count(*) > 1";
    
    Set<Object[]> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .listSet(Object[].class);
    
    for (Object[] result : results) {
      String ownerId = (String)result[0];
      String memberId = (String)result[1];
      String fieldId = (String)result[2];

      Set<GroupSet> groupSets = HibernateSession.byHqlStatic()
        .createQuery("select gs from GroupSet gs where memberId = :memberId and fieldId = :fieldId and ownerId = :ownerId and depth='0'")
        .setCacheable(false)
        .setString("ownerId", ownerId)
        .setString("memberId", memberId)
        .setString("fieldId", fieldId)
        .listSet(GroupSet.class);
      
      Set<GroupSet> noForeignKeys = new LinkedHashSet<GroupSet>();
      for (GroupSet groupSet : groupSets) {
        long count = HibernateSession.byHqlStatic()
          .createQuery("select count(*) from GroupSet where parentId = :parentId")
          .setCacheable(false)
          .setString("parentId", groupSet.getId())
          .uniqueResult(Long.class);
        
        if (count == 1) {
          noForeignKeys.add(groupSet);
        }
      }
      
      List<GroupSet> currentDuplicates = new ArrayList<GroupSet>();
      currentDuplicates.addAll(noForeignKeys);
      if (groupSets.size() == currentDuplicates.size()) {
        currentDuplicates.remove(0);
      }
      
      duplicates.addAll(currentDuplicates);
    }
    
    return duplicates;  
  }
  
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findBadGroupSetsForCompositeGroups()
   */
  public Set<GroupSet> findBadGroupSetsForCompositeGroups() {
    String sql = "select distinct gs from GroupSet gs, Composite c " +
        "where gs.ownerGroupId = c.factorOwnerUuid " +
        "and gs.fieldId = :fieldId " +
        "and gs.depth = '1' ";
    
    Set<GroupSet> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(GroupSet.class);
    
    return results;  
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findMissingEffectiveGroupSets()
   */
  public Set<Object[]> findMissingEffectiveGroupSets() {
    String sql = "select gs1, gs2 from GroupSet gs1, GroupSet gs2 " +
        "where gs1.memberId = gs2.ownerId " +
        "and gs1.id <> gs2.id " +
        "and gs1.depth > '0' and gs2.depth = '1' " +
        "and gs2.fieldId = :fieldId " +
        "and (gs1.ownerId <> gs2.memberId or gs1.fieldId <> :fieldId) " +
        "and not exists (select 1 from GroupSet gs3 " +
            "where gs3.ownerId = gs1.ownerId " +
            "and gs3.memberId = gs2.memberId " +
            "and gs3.fieldId = gs1.fieldId " +
            "and gs3.type = 'effective' " +
            "and gs3.parentId = gs1.id)";
    
    Set<Object[]> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Object[].class);
    
    return results;  
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findBadEffectiveGroupSets()
   */
  public Set<GroupSet> findBadEffectiveGroupSets() {
    String sql = "select gs3 from GroupSet gs1, GroupSet gs3 " +
        "where gs3.parentId = gs1.id " +
        "and gs3.type = 'effective' " +
        "and gs3.fieldId = gs1.fieldId " +
        "and gs3.ownerId = gs1.ownerId " +
        "and gs1.depth > '0' " +   
        "and not exists (select 1 from GroupSet gs2 " +
            "where gs1.memberId = gs2.ownerId " +
            "and gs2.depth = '1' " +
            "and gs1.id <> gs2.id " +
            "and gs2.fieldId = :fieldId " +
            "and (gs1.ownerId <> gs2.memberId or gs1.fieldId <> :fieldId) " +
            "and gs3.memberId = gs2.memberId)";
    
    Set<GroupSet> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(GroupSet.class);
    
    return results;  
  }

  /**
   * @see GroupSetDAO#findAllByOwnerGroupAndFieldAndMembershipMember(String, String, Member)
   */
  @Override
  public Set<GroupSet> findAllByOwnerGroupAndFieldAndMembershipMember(String ownerGroupId,
      String fieldId, Member membershipMember) {

    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder();
    
    sql.append("select distinct gs from GroupSet gs, " +
        " MembershipEntry listMembership where gs.ownerGroupId = :ownerGroupId ");
    sql.append(" and gs.fieldId = :fieldId ");
    sql.append(" and listMembership.ownerGroupId = gs.memberGroupId ");
    sql.append(" and listMembership.fieldId = gs.memberFieldId ");
    sql.append(" and listMembership.memberUuid = :memberId  and listMembership.enabledDb = 'T' ");
    byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setString("ownerGroupId", ownerGroupId)
      .setString("memberId", membershipMember.getId());
    byHqlStatic.setString("fieldId", fieldId);
    Set<GroupSet> groupSets = byHqlStatic
        .listSet(GroupSet.class);

    return groupSets;
  }

  /**
   * @see GroupSetDAO#findAllByOwnerStemAndFieldAndMembershipMember(String, String, Member)
   */
  @Override
  public Set<GroupSet> findAllByOwnerStemAndFieldAndMembershipMember(String ownerStemId,
      String fieldId, Member membershipMember) {
  
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    StringBuilder sql = new StringBuilder();
    
    sql.append("select distinct gs from GroupSet gs, " +
        " MembershipEntry listMembership where gs.ownerStemId = :ownerStemId ");
    sql.append(" and gs.fieldId = :fieldId ");
    sql.append(" and ( listMembership.ownerGroupId = gs.memberGroupId ");
    sql.append(" or listMembership.ownerStemId = gs.memberStemId ) ");
    sql.append(" and listMembership.fieldId = gs.memberFieldId ");
    sql.append(" and listMembership.memberUuid = :memberId  and listMembership.enabledDb = 'T' ");
    byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setString("ownerStemId", ownerStemId)
      .setString("memberId", membershipMember.getId());
    byHqlStatic.setString("fieldId", fieldId);
    Set<GroupSet> groupSets = byHqlStatic
        .listSet(GroupSet.class);
  
    return groupSets;
  }

  /**
   * @see GroupSetDAO#findAllByOwnerAttributeDefAndFieldAndMembershipMember(String, String, Member)
   */
  @Override
  public Set<GroupSet> findAllByOwnerAttributeDefAndFieldAndMembershipMember(String ownerAttrDefId,
      String fieldId, Member membershipMember) {
  
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    StringBuilder sql = new StringBuilder();
    
    sql.append("select distinct gs from GroupSet gs, " +
        " MembershipEntry listMembership where gs.ownerAttrDefId = :ownerAttrDefId ");
    sql.append(" and gs.fieldId = :fieldId ");
    sql.append(" and (listMembership.ownerGroupId = gs.memberGroupId ");
    sql.append(" or listMembership.ownerAttrDefId = gs.memberAttrDefId) ");
    sql.append(" and listMembership.fieldId = gs.memberFieldId ");
    sql.append(" and listMembership.memberUuid = :memberId  and listMembership.enabledDb = 'T' ");
    byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setString("ownerAttrDefId", ownerAttrDefId)
      .setString("memberId", membershipMember.getId());
    byHqlStatic.setString("fieldId", fieldId);
    Set<GroupSet> groupSets = byHqlStatic
        .listSet(GroupSet.class);
  
    return groupSets;
  }
}

