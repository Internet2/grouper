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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignDAO extends Hib3DAO implements PITAttributeAssignDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssign)
   */
  public void saveOrUpdate(PITAttributeAssign pitAttributeAssign) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssign);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeAssign> pitAttributeAssigns) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssigns);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssign)
   */
  public void delete(PITAttributeAssign pitAttributeAssign) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssign);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITAttributeAssign set ownerAttributeAssignId = null where ownerAttributeAssignId is not null and sourceId not in (select attrAssign.id from AttributeAssign as attrAssign)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeAssign where sourceId not in (select attrAssign.id from AttributeAssign as attrAssign)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITAttributeAssign findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITAttributeAssign pitAttributeAssign = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITAttributeAssign.class);
    
    if (pitAttributeAssign == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITAttributeAssign with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssign;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITAttributeAssign findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITAttributeAssign pitAttributeAssign = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITAttributeAssign.class);
    
    if (pitAttributeAssign == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssign with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssign;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITAttributeAssign> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITAttributeAssign> pitAttributeAssign = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    if (pitAttributeAssign.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssign with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssign;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findBySourceIdMostRecent(java.lang.String, boolean)
   */
  public PITAttributeAssign findBySourceIdMostRecent(String id, boolean exceptionIfNotFound) {
    Set<PITAttributeAssign> pitAttributeAssign = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.sourceId = :id order by startTimeDb desc")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdMostRecent")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    if (pitAttributeAssign.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssign with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssign.iterator().next();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findById(java.lang.String, boolean)
   */
  public PITAttributeAssign findById(String id, boolean exceptionIfNotFound) {
    PITAttributeAssign pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssign as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssign.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssign with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveByOwnerPITAttributeAssignId(java.lang.String)
   */
  public Set<PITAttributeAssign> findActiveByOwnerPITAttributeAssignId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerAttributeAssignId = :id and attrAssign.activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveByOwnerPITAttributeAssignId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveByOwnerPITAttributeAssignId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerPITAttributeAssignId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerAttributeAssignId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerPITAttributeAssignId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#updateOwnerPITMembershipId(java.lang.String, java.lang.String)
   */
  public void updateOwnerPITMembershipId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssign set ownerMembershipId = :newId where ownerMembershipId = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#updateOwnerPITAttributeAssignId(java.lang.String, java.lang.String)
   */
  public void updateOwnerPITAttributeAssignId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssign set ownerAttributeAssignId = :newId where ownerAttributeAssignId = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveByOwnerPITMembershipId(java.lang.String)
   */
  public Set<PITAttributeAssign> findActiveByOwnerPITMembershipId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerMembershipId = :id and attrAssign.activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveByOwnerPITMembershipId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerPITMembershipId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerPITMembershipId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerMembershipId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerPITMembershipId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerPITGroupId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerPITGroupId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerGroupId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerPITGroupId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerPITGroupIdAndPITAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerPITGroupIdAndPITAttributeDefNameId(String pitGroupId, String pitAttributeDefNameId) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerGroupId = :pitGroupId and attrAssign.attributeDefNameId = :pitAttributeDefNameId and attributeAssignTypeDb = 'group'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerPITGroupIdAndPITAttributeDefNameId")
      .setString("pitGroupId", pitGroupId)
      .setString("pitAttributeDefNameId", pitAttributeDefNameId)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerPITStemId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerPITStemId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerStemId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerPITStemId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerPITAttributeDefId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerPITAttributeDefId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerAttributeDefId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerPITAttributeDefId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public long deleteInactiveRecords(Timestamp time) {
    
    return HibernateSession.byHqlStatic().createQuery(
        "select id from PITAttributeAssign a where a.endTimeDb is not null and a.endTimeDb < :time " +
          "and not exists (select 1 from PITAttributeAssignValue v where v.attributeAssignId = a.id)").setLong("time", time.getTime() * 1000)
        //do this since mysql cant handle self-referential foreign keys
        .assignBatchPreExecuteUpdateQuery("update PITAttributeAssign a set a.ownerAttributeAssignId = null where a.ownerAttributeAssignId is not null")
        .deleteInBatches(String.class, "PITAttributeAssign", "id");
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findAssignmentsOnAssignments(java.util.Collection, java.sql.Timestamp, java.sql.Timestamp)
   */
  public Set<PITAttributeAssign> findAssignmentsOnAssignments(Collection<PITAttributeAssign> attributeAssigns, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    
    List<PITAttributeAssign> attributeAssignsList = GrouperUtil.listFromCollection(attributeAssigns);
    
    int attributeAssignsSize = GrouperUtil.length(attributeAssignsList);

    Set<PITAttributeAssign> results = new LinkedHashSet<PITAttributeAssign>();
    
    if (attributeAssignsSize == 0) {
      return results;
    }
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignsSize, 100);

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);

    for (int i = 0; i < numberOfBatches; i++) {
      
      List<PITAttributeAssign> currentBatch = GrouperUtil.batchList(attributeAssignsList, 100, i);
      
      int currentBatchSize = GrouperUtil.length(currentBatch);
      if (currentBatchSize == 0) {
        continue;
      }
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      String selectPrefix = "select distinct aa ";
      StringBuilder sqlTables = new StringBuilder(" from PITAttributeAssign aa, PITAttributeDefName adn, PITAttributeDef ad ");
      
      StringBuilder sqlWhereClause = new StringBuilder(" aa.attributeDefNameId = adn.id and ad.id = adn.attributeDefId ");
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      Subject grouperSessionSubject = grouperSession.getSubject();
      
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "ad.sourceId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
      
      StringBuilder sql;
      sql = sqlTables.append(" where ").append(sqlWhereClause);
              
      //convert to a list of ids
      Set<String> ids = new LinkedHashSet<String>();
      for (PITAttributeAssign attributeAssign : currentBatch) {
        ids.add(attributeAssign.getId());
      }
        
      sql.append(" and aa.ownerAttributeAssignId in (");
      sql.append(HibUtils.convertToInClause(ids, byHqlStatic));
      sql.append(") ");
      
      if (pointInTimeFrom != null) {
        Long endDateAfter = pointInTimeFrom.getTime() * 1000;
        sql.append(" and (aa.endTimeDb is null or aa.endTimeDb > '" + endDateAfter + "')");
      }
      
      if (pointInTimeTo != null) {
        Long startDateBefore = pointInTimeTo.getTime() * 1000;
        sql.append(" and aa.startTimeDb < '" + startDateBefore + "'");
      }
      
      byHqlStatic
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAssignmentsOnAssignments");

      Set<PITAttributeAssign> tempResults = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(PITAttributeAssign.class);

      //nothing to filter
      if (GrouperUtil.length(tempResults) > 0) {
        //if the hql didnt filter, we need to do that here
        tempResults = grouperSession.getAttributeDefResolver().postHqlFilterPITAttributeAssigns(grouperSessionSubject, tempResults);
      }
      
      results.addAll(tempResults);
      
      if (maxAssignments >= 0) {

        //see if too many
        if (results.size() > maxAssignments) {
          throw new RuntimeException("Too many results: " + results.size());
        }
        
      }
    }
    
    //we should be down to the secure list
    return results;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByPITAttributeAssignActionId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByPITAttributeAssignActionId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
        .byHqlStatic()
        .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.attributeAssignActionId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByPITAttributeAssignActionId")
        .setString("id", id)
        .listSet(PITAttributeAssign.class);
      
      return assignments;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByPITAttributeDefNameId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByPITAttributeDefNameId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
        .byHqlStatic()
        .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.attributeDefNameId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByPITAttributeDefNameId")
        .setString("id", id)
        .listSet(PITAttributeAssign.class);
      
      return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findMissingActivePITAttributeAssigns()
   */
  public Set<AttributeAssign> findMissingActivePITAttributeAssigns() {

    Set<AttributeAssign> assigns = HibernateSession
      .byHqlStatic()
      .createQuery("select assign from AttributeAssign assign where assign.enabledDb='T' and " +
          "not exists (select 1 from PITAttributeAssign pit where assign.id = pit.sourceId and pit.activeDb = 'T') " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = assign.id " +
          "    and type.actionName='addAttributeAssign' and type.changeLogCategory='attributeAssign' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeAssigns")
      .listSet(AttributeAssign.class);
    
    return assigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findMissingInactivePITAttributeAssigns()
   */
  public Set<PITAttributeAssign> findMissingInactivePITAttributeAssigns() {

    Set<PITAttributeAssign> assigns = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssign pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeAssign assign where assign.id = pit.sourceId and assign.enabledDb='T') " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteAttributeAssign' and type.changeLogCategory='attributeAssign' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeAssigns")
      .listSet(PITAttributeAssign.class);
    
    return assigns;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITAttributeAssign where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssign where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}
