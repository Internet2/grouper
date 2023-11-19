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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITMembershipDAO extends Hib3DAO implements PITMembershipDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITMembershipDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public void saveOrUpdate(PITMembership pitMembership) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitMembership);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITMembership> pitMemberships) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitMemberships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#delete(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public void delete(PITMembership pitMembership) {
    HibernateSession.byObjectStatic().delete(pitMembership);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITMembership where sourceId not in (select ms.immediateMembershipId from ImmediateMembershipEntry as ms)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITMembership findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITMembership pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITMembership.class);
    
    if (pitMembership == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITMembership with sourceId=" + id + " not found");
    }
    
    return pitMembership;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceIdsActive(java.util.Collection)
   */
  public Set<PITMembership> findBySourceIdsActive(Collection<String> ids) {
    int idsSize = GrouperUtil.length(ids);

    Set<PITMembership> results = new HashSet<PITMembership>();

    if (idsSize == 0) {
      return results;
    }

    List<String> idsList = new ArrayList<String>(ids);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsSize, 100);
   
    //if there are more than 100, batch these up and return them
    for (int i=0;i<numberOfBatches; i++) {

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      byHqlStatic.setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive");

      StringBuilder sql = new StringBuilder("select pitMembership from PITMembership as pitMembership where ");

      List<String> currentBatch = GrouperUtil.batchList(idsList, 100, i);

      sql.append(" pitMembership.sourceId in (");
      sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      sql.append(") and activeDb = 'T'");
   
      Set<PITMembership> localResult = byHqlStatic.createQuery(sql.toString()).listSet(PITMembership.class);
      results.addAll(localResult);
    }

    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceIds(java.util.Collection)
   */
  public Set<PITMembership> findBySourceIds(Collection<String> ids) {
    int idsSize = GrouperUtil.length(ids);

    Set<PITMembership> results = new HashSet<PITMembership>();

    if (idsSize == 0) {
      return results;
    }

    List<String> idsList = new ArrayList<String>(ids);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsSize, 100);
   
    //if there are more than 100, batch these up and return them
    for (int i=0;i<numberOfBatches; i++) {

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      byHqlStatic.setCacheable(true).setCacheRegion(KLASS + ".FindBySourceId");

      StringBuilder sql = new StringBuilder("select pitMembership from PITMembership as pitMembership where ");

      List<String> currentBatch = GrouperUtil.batchList(idsList, 100, i);

      sql.append(" pitMembership.sourceId in (");
      sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      sql.append(")");
   
      Set<PITMembership> localResult = byHqlStatic.createQuery(sql.toString()).listSet(PITMembership.class);
      results.addAll(localResult);
    }

    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITMembership findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITMembership pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITMembership.class);
    
    if (pitMembership == null && exceptionIfNotFound) {
      throw new RuntimeException("PITMembership with sourceId=" + id + " not found");
    }
    
    return pitMembership;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITMembership> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITMembership> pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITMembership.class);
    
    if (pitMembership.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITMembership with sourceId=" + id + " not found");
    }
    
    return pitMembership;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findBySourceIdMostRecent(java.lang.String, boolean)
   */
  public PITMembership findBySourceIdMostRecent(String id, boolean exceptionIfNotFound) {
    Set<PITMembership> pitMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select pitMembership from PITMembership as pitMembership where pitMembership.sourceId = :id order by startTimeDb desc")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdMostRecent")
      .setString("id", id)
      .listSet(PITMembership.class);
    
    if (pitMembership.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITMembership with sourceId=" + id + " not found");
    }
    
    if (pitMembership.size() == 0) {
      return null;
    }
    
    return pitMembership.iterator().next();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findById(java.lang.String, boolean)
   */
  public PITMembership findById(String id, boolean exceptionIfNotFound) {
    PITMembership pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITMembership as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITMembership.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITMembership with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public long deleteInactiveRecords(Timestamp time) {
    
    return HibernateSession.byHqlStatic().createQuery(
        "select id from PITMembership m where m.endTimeDb is not null and m.endTimeDb < :time " +
          "and not exists (select 1 from PITAttributeAssign a where a.ownerMembershipId = m.id)")
          .setLong("time", time.getTime() * 1000)
        .deleteInBatches(String.class, "PITMembership", "id");

    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findAllByPITOwner(java.lang.String)
   */
  public Set<PITMembership> findAllByPITOwner(String ownerId) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembership ms where "
        + "ms.ownerId = :ownerId");
    
    return HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByPITOwner")
      .setString("ownerId", ownerId)
      .listSet(PITMembership.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findAllByPITMember(java.lang.String)
   */
  public Set<PITMembership> findAllByPITMember(String memberId) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembership ms where "
        + "ms.memberId = :memberId");
    
    return HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByPITMember")
      .setString("memberId", memberId)
      .listSet(PITMembership.class);
  }
  
  /**
   * 
   */
  public Set<PITMembership> findAllByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembership ms where "
        + "ms.ownerId = :ownerId "
        + "and ms.memberId = :memberId "
        + "and ms.fieldId = :fieldId");
    
    return HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByPITOwnerAndPITMemberAndPITField")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .listSet(PITMembership.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findMissingActivePITMemberships()
   */
  public Set<Membership> findMissingActivePITMemberships() {

    // note that doing actual checks for the addMembership and addPrivilege change log events seem to be very expensive...
    Set<Membership> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select ms from ImmediateMembershipEntry ms where ms.enabledDb='T' and " +
          "not exists (select 1 from PITMembership pit where ms.immediateMembershipId = pit.sourceId and pit.activeDb = 'T') " +
          "and not exists (select 1 from ChangeLogEntryTemp temp " +
          "    where temp.string01 = ms.immediateMembershipId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITMemberships")
      .listSet(Membership.class);
    
    return mships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findMissingInactivePITMemberships()
   */
  public Set<PITMembership> findMissingInactivePITMemberships() {

    Set<PITMembership> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITMembership pit where activeDb = 'T' and " +
          "not exists (select 1 from ImmediateMembershipEntry ms where ms.immediateMembershipId = pit.sourceId and ms.enabledDb='T') " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteMembership' and type.changeLogCategory='membership' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deletePrivilege' and type.changeLogCategory='privilege' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITMemberships")
      .listSet(PITMembership.class);
    
    return mships;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITMembership where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITMembership where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}

