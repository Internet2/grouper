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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.PITStemDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITStemDAO extends Hib3DAO implements PITStemDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITStemDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITStem)
   */
  public void saveOrUpdate(PITStem pitStem) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitStem);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITStem> pitStems) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitStems);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#delete(edu.internet2.middleware.grouper.pit.PITStem)
   */
  public void delete(final PITStem pitStem) {
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.obliterate.stem.in.transaction", false)) {
      
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
    
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
    
              Hib3PITStemDAO.this.deleteHelper(pitStem);
              
              return null;
           }
      });
    } else {
      Hib3PITStemDAO.this.deleteHelper(pitStem);
    }

  }

  /**
   * delete logic
   * @param pitStem
   */
  private void deleteHelper(PITStem pitStem) {
    
    boolean printOutput = PITStem.printOutputOnDelete();
    
    if (pitStem.isActive()) {
      throw new RuntimeException("Cannot delete active point in time stem object with id=" + pitStem.getId());
    }
    
    if (printOutput) {
      System.out.println("Obliterating stem from point in time: " + pitStem.getName() + ", ID=" + pitStem.getId());
    }
    
    // delete groups
    Set<PITGroup> groups = GrouperDAOFactory.getFactory().getPITGroup().findByPITStemId(pitStem.getId());
    for (PITGroup group : groups) {
      GrouperDAOFactory.getFactory().getPITGroup().delete(group);
      if (printOutput) {
        System.out.println("Done deleting group from point in time: " + group.getName() + ", ID=" + group.getId());
      }
    }
    
    // delete attribute def names
    Set<PITAttributeDefName> attrs = GrouperDAOFactory.getFactory().getPITAttributeDefName().findByPITStemId(pitStem.getId());
    for (PITAttributeDefName attr : attrs) {
      GrouperDAOFactory.getFactory().getPITAttributeDefName().delete(attr);
      if (printOutput) {
        System.out.println("Done deleting attributeDefName from point in time: " + attr.getName() + ", ID=" + attr.getId());
      }
    }
    
    // delete attribute defs
    Set<PITAttributeDef> defs = GrouperDAOFactory.getFactory().getPITAttributeDef().findByPITStemId(pitStem.getId());
    for (PITAttributeDef def : defs) {
      GrouperDAOFactory.getFactory().getPITAttributeDef().delete(def);
      if (printOutput) {
        System.out.println("Done deleting attributeDef from point in time: " + def.getName() + ", ID=" + def.getId());
      }
    }
    
    // delete child stems
    Set<PITStem> stems = GrouperDAOFactory.getFactory().getPITStem().findByParentPITStemId(pitStem.getId());
    for (PITStem stem : stems) {
      // call helper so it doesnt do another tx check
      deleteHelper(stem);
    }
    
    HibernateSession.byObjectStatic().delete(pitStem);

  }

  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys    
    hibernateSession.byHql().createQuery("update PITStem set parentStemId = null where sourceId not in (select s.uuid from Stem as s)").executeUpdate();

    hibernateSession.byHql().createQuery("delete from PITStem where sourceId not in (select s.uuid from Stem as s)").executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITStem findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    return findBySourceIdActive(id, false, exceptionIfNotFound);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findBySourceIdActive(java.lang.String, boolean, boolean)
   */
  public PITStem findBySourceIdActive(String id, boolean createIfNotFound, boolean exceptionIfNotFound) {
    PITStem pitStem = HibernateSession
      .byHqlStatic()
      .createQuery("select pitStem from PITStem as pitStem where pitStem.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITStem.class);
    
    if (pitStem == null && createIfNotFound) {
      Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(id, false);

      if (stem != null) {
        String contextId = null;
        if (!GrouperUtil.isEmpty(stem.getContextId())) {
          contextId = stem.getContextId();
        }
        
        String parentStemId = null;
        boolean proceedWithPITStemCreation = true;
        
        if (stem.getParentUuid() != null) {
          PITStem pitStemParent = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(stem.getParentUuid(), true, false);
          
          if (pitStemParent != null) {
            parentStemId = pitStemParent.getId();
          } else {
            proceedWithPITStemCreation = false;
          }
        }
        
        if (proceedWithPITStemCreation) {
          pitStem = new PITStem();
          pitStem.setId(GrouperUuid.getUuid());
          pitStem.setSourceId(id);
          pitStem.setNameDb(stem.getName());
          pitStem.setParentStemId(parentStemId);
          pitStem.setContextId(contextId);
          pitStem.setActiveDb("T");
          pitStem.setStartTimeDb(System.currentTimeMillis() * 1000);
          
          pitStem.saveOrUpdate();
        }
      }
    }
    
    if (pitStem == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITStem with sourceId=" + id + " not found");
    }
    
    return pitStem;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITStem findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITStem pitStem = HibernateSession
      .byHqlStatic()
      .createQuery("select pitStem from PITStem as pitStem where pitStem.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITStem.class);
    
    if (pitStem == null && exceptionIfNotFound) {
      throw new RuntimeException("PITStem with sourceId=" + id + " not found");
    }
    
    return pitStem;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITStem> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITStem> pitStems = HibernateSession
      .byHqlStatic()
      .createQuery("select pitStem from PITStem as pitStem where pitStem.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITStem.class);
    
    if (pitStems.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITStem with sourceId=" + id + " not found");
    }
    
    return pitStems;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findById(java.lang.String, boolean)
   */
  public PITStem findById(String id, boolean exceptionIfNotFound) {
    PITStem pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITStem as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITStem.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITStem with id=" + id + " not found");
    }
    
    return pit;
  }
  
  public Map<String, PITStem> findByIds(Collection<String> ids) {
    int idsSize = GrouperUtil.length(ids);

    Map<String, PITStem> results = new LinkedHashMap<String, PITStem>();

    if (idsSize == 0) {
      return results;
    }

    List<String> idsList = new ArrayList<String>(ids);

    int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 200);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsSize, batchSize);
   
    for (int i=0;i<numberOfBatches; i++) {

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      byHqlStatic.setCacheable(true).setCacheRegion(KLASS + ".FindByIds");

      StringBuilder sql = new StringBuilder("select pitStem from PITStem as pitStem where ");

      List<String> currentBatch = GrouperUtil.batchList(idsList, batchSize, i);

      sql.append(" pitStem.id in (");
      sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      sql.append(")");
   
      Set<PITStem> localResult = byHqlStatic.createQuery(sql.toString()).listSet(PITStem.class);
      for (PITStem pitStem : localResult) {
        results.put(pitStem.getId(), pitStem);
      }
    }

    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public long deleteInactiveRecords(Timestamp time) {
    
    return HibernateSession.byHqlStatic().createQuery(
        "select id from PITStem where endTimeDb is not null and endTimeDb < :time").setLong("time", time.getTime() * 1000)
        //do this since mysql cant handle self-referential foreign keys
        .assignBatchPreExecuteUpdateQuery("update PITStem set parentStemId = null where parentStemId in ")
        .deleteInBatches(String.class, "PITStem", "id");
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findByParentPITStemId(java.lang.String)
   */
  public Set<PITStem> findByParentPITStemId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select pitStem from PITStem as pitStem where pitStem.parentStemId = :id order by pitStem.nameDb")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByParentPITStemId")
        .setString("id", id)
        .listSet(PITStem.class);
  }

  public Set<PITStem> findByName(String stemName, boolean orderByStartTime) {
    String sql = "select pitStem from PITStem as pitStem where pitStem.nameDb = :name";
    
    if (orderByStartTime) {
      sql += " order by startTimeDb";
    }
    
    Set<PITStem> pitStems = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false).setCacheRegion(KLASS + ".FindByName")
      .setString("name", stemName)
      .listSet(PITStem.class);
    
    return pitStems;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findMissingActivePITStems()
   */
  public Set<Stem> findMissingActivePITStems() {

    Set<Stem> stems = HibernateSession
      .byHqlStatic()
      .createQuery("select s from Stem s where " +
          "not exists (select 1 from PITStem pitStem, PITStem pitParentStem where pitStem.parentStemId = pitParentStem.id" +
          "            and s.uuid = pitStem.sourceId and s.nameDb = pitStem.nameDb and s.parentUuid = pitParentStem.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = s.uuid " +
          "    and type.actionName='addStem' and type.changeLogCategory='stem' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = s.uuid " +
          "    and type.actionName='updateStem' and type.changeLogCategory='stem' and type.id=temp.changeLogTypeId) " +
          "order by s.nameDb")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITStems")
      .listSet(Stem.class);
    
    return stems;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findMissingInactivePITStems()
   */
  public Set<PITStem> findMissingInactivePITStems() {

    Set<PITStem> stems = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITStem pit where activeDb = 'T' and " +
          "not exists (select 1 from Stem s where s.uuid = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteStem' and type.changeLogCategory='stem' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITStems")
      .listSet(PITStem.class);
    
    return stems;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITStem where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITStem where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}

