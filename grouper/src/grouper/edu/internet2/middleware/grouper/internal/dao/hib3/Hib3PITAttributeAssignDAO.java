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
    hibernateSession.byHql().createQuery("update PITAttributeAssign set ownerAttributeAssignId = null where ownerAttributeAssignId is not null and id not in (select attrAssign.id from AttributeAssign as attrAssign)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeAssign where id not in (select attrAssign.id from AttributeAssign as attrAssign)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findById(java.lang.String)
   */
  public PITAttributeAssign findById(String id) {
    PITAttributeAssign pitAttributeAssign = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssign.class);
    
    return pitAttributeAssign;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveByOwnerAttributeAssignId(java.lang.String)
   */
  public Set<PITAttributeAssign> findActiveByOwnerAttributeAssignId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerAttributeAssignId = :id and attrAssign.activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveByOwnerAttributeAssignId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveByOwnerAttributeAssignId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerAttributeAssignId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerAttributeAssignId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerAttributeAssignId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#updateOwnerMembershipId(java.lang.String, java.lang.String)
   */
  public void updateOwnerMembershipId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssign set ownerMembershipId = :newId where ownerMembershipId = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#updateOwnerAttributeAssignId(java.lang.String, java.lang.String)
   */
  public void updateOwnerAttributeAssignId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssign set ownerAttributeAssignId = :newId where ownerAttributeAssignId = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveByOwnerMembershipId(java.lang.String)
   */
  public Set<PITAttributeAssign> findActiveByOwnerMembershipId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerMembershipId = :id and attrAssign.activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveByOwnerMembershipId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerMembershipId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerMembershipId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerMembershipId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerMembershipId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerGroupId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerGroupId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerGroupId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerGroupId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerStemId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerStemId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerStemId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerStemId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByOwnerAttributeDefId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByOwnerAttributeDefId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerAttributeDefId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerAttributeDefId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    HibernateSession.byHqlStatic()
      .createQuery("update PITAttributeAssign a set a.ownerAttributeAssignId = null where a.endTimeDb is not null and a.endTimeDb < :time and a.ownerAttributeAssignId is not null " +
      		"and not exists (select 1 from PITAttributeAssignValue v where v.attributeAssignId = a.id)")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssign a where a.endTimeDb is not null and a.endTimeDb < :time and a.ownerAttributeAssignId is null " +
      		"and not exists (select 1 from PITAttributeAssignValue v where v.attributeAssignId = a.id)")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findAssignmentsOnAssignments(java.util.Collection, java.sql.Timestamp, java.sql.Timestamp)
   */
  public Set<PITAttributeAssign> findAssignmentsOnAssignments(Collection<PITAttributeAssign> attributeAssigns, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    
    int attributeAssignsSize = GrouperUtil.length(attributeAssigns);

    Set<PITAttributeAssign> results = new LinkedHashSet<PITAttributeAssign>();
    
    if (attributeAssignsSize == 0) {
      return results;
    }
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignsSize, 100);

    int maxAssignments = GrouperConfig.getPropertyInt("ws.findAttrAssignments.maxResultSize", 30000);

    for (int i = 0; i < numberOfBatches; i++) {
      
      List<PITAttributeAssign> currentBatch = GrouperUtil.batchList(attributeAssigns, 100, i);
      
      int currentBatchSize = GrouperUtil.length(currentBatch);
      if (currentBatchSize == 0) {
        continue;
      }
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      String selectPrefix = "select distinct aa ";
      StringBuilder sqlTables = new StringBuilder(" from PITAttributeAssign aa, PITAttributeDefName adn ");
      
      StringBuilder sqlWhereClause = new StringBuilder(" aa.attributeDefNameId = adn.id ");
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      Subject grouperSessionSubject = grouperSession.getSubject();
      
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.READ_PRIVILEGES);
      
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
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByAttributeAssignActionId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByAttributeAssignActionId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
        .byHqlStatic()
        .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.attributeAssignActionId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByAttributeAssignActionId")
        .setString("id", id)
        .listSet(PITAttributeAssign.class);
      
      return assignments;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findByAttributeDefNameId(java.lang.String)
   */
  public Set<PITAttributeAssign> findByAttributeDefNameId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
        .byHqlStatic()
        .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.attributeDefNameId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByAttributeDefNameId")
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
          "not exists (select 1 from PITAttributeAssign pit where assign.id = pit.id and pit.activeDb = 'T') " +
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
          "not exists (select 1 from AttributeAssign assign where assign.id = pit.id and assign.enabledDb='T') " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deleteAttributeAssign' and type.changeLogCategory='attributeAssign' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeAssigns")
      .listSet(PITAttributeAssign.class);
    
    return assigns;
  }
}
