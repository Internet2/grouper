/**
 * Copyright 2012 Internet2
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
import java.util.Set;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AuditEntryNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;

/**
 * Data Access Object for audit entry
 * @author  mchyzer
 * @version $Id: Hib3AuditEntryDAO.java,v 1.4 2009-06-28 19:02:17 mchyzer Exp $
 */
public class Hib3AuditEntryDAO extends Hib3DAO implements AuditEntryDAO {
  
  /**
   * 
   */
  private static final String KLASS = Hib3AuditEntryDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO#saveOrUpdate(edu.internet2.middleware.grouper.audit.AuditEntry)
   */
  public void saveOrUpdate(AuditEntry auditEntry) {
    auditEntry.truncate();
    HibernateSession.byObjectStatic().saveOrUpdate(auditEntry);
  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AuditEntry").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO#findById(java.lang.String, boolean)
   */
  public AuditEntry findById(String id, boolean exceptionIfNotFound) {
    AuditEntry auditEntry = HibernateSession.byHqlStatic().createQuery("from AuditEntry where id = :theId")
      .setString("theId", id).uniqueResult(AuditEntry.class);
    if (auditEntry == null && exceptionIfNotFound) {
      throw new AuditEntryNotFoundException("Cant find audit entry by id: " + id);
    }
    return auditEntry;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO#saveUpdateProperties(edu.internet2.middleware.grouper.audit.AuditEntry)
   */
  public void saveUpdateProperties(AuditEntry auditEntry) {

    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AuditEntry " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreateTimeLong, " +
        "lastUpdatedDb = :theModifyTimeLong " +
        "where id = :theUuid")
        .setLong("theHibernateVersionNumber", auditEntry.getHibernateVersionNumber())
        .setLong("theCreateTimeLong", auditEntry.getCreatedOnDb())
        .setLong("theModifyTimeLong", auditEntry.getLastUpdatedDb())
        .setString("theContextId", auditEntry.getContextId())
        .setString("theUuid", auditEntry.getId()).executeUpdate();
  }

  /**
   * find audit entries that a user did
   * @see AuditEntryDAO#findByActingUser(QueryOptions)
   */
  @Override
  public Set<AuditEntry> findByActingUser(String actAsMemberId, QueryOptions queryOptions) {

    StringBuilder sql = new StringBuilder("select theAuditEntry "
        + " from AuditEntry theAuditEntry where "
        + " (theAuditEntry.actAsMemberId = :actAsMemberId) or (theAuditEntry.loggedInMemberId = :loggedInMemberId and theAuditEntry.actAsMemberId is null) ");

    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQueryPaging() == null) {
      int defaultHib3AuditEntryPageSize = GrouperConfig.retrieveConfig().propertyValueInt("defaultHib3AuditEntryPageSize", 1000);
      queryOptions.paging(QueryPaging.page(defaultHib3AuditEntryPageSize, 1, false));
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sort(QuerySort.desc("createdOnDb"));
    }
    
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(sql.toString())
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByActingUser")
      .setString( "actAsMemberId", actAsMemberId ) 
      .setString( "loggedInMemberId", actAsMemberId ) 
      .listSet(AuditEntry.class);
  }

} 

