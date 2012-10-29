/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * Data Access Object for audit type
 * @author  mchyzer
 * @version $Id: Hib3AuditTypeDAO.java,v 1.3 2009-05-13 12:15:01 mchyzer Exp $
 */
public class Hib3AuditTypeDAO extends Hib3DAO implements AuditTypeDAO {
  
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AuditTypeDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#saveOrUpdate(edu.internet2.middleware.grouper.audit.AuditType)
   */
  public void saveOrUpdate(AuditType auditType) {
    
    //assign id if not there
    if (StringUtils.isBlank(auditType.getId())) {
      auditType.setId(GrouperUuid.getUuid());
    }

    auditType.truncate();
    HibernateSession.byObjectStatic().saveOrUpdate(auditType);
  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(@SuppressWarnings("unused") HibernateSession hibernateSession) {
    //i think we dont want to delete these in a reset...
    //hibernateSession.byHql().createQuery("delete from AuditType").executeUpdate();
    //tell the cache it is empty...
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#deleteEntriesAndTypesByCategoryAndAction(java.lang.String, java.lang.String)
   */
  public void deleteEntriesAndTypesByCategoryAndAction(String category, String action) {
    
    //delete entries
    HibernateSession.byHqlStatic()
      .createQuery("delete from AuditEntry as auditEntry where auditEntry.auditTypeId = " +
      		"(select auditType.id from AuditType auditType " +
      		"where auditType.auditCategory = :theAuditCategory and auditType.actionName = :theActionName)")
      		.setString("theAuditCategory", category).setString("theActionName", action).executeUpdate();

    //delete types
    HibernateSession.byHqlStatic()
      .createQuery("delete from AuditType where auditCategory = :theAuditCategory and actionName = :theActionName")
      .setString("theAuditCategory", category).setString("theActionName", action).executeUpdate();
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#findAll()
   */
  public Set<AuditType> findAll() {
    return HibernateSession.byHqlStatic().createQuery("from AuditType").listSet(AuditType.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#findByCategory(java.lang.String)
   */
  public Set<AuditType> findByCategory(String categoryName) {
    return HibernateSession.byHqlStatic().createQuery("from AuditType where auditCategory = :theAuditCategory")
      .setCacheable(true).setCacheRegion(KLASS + ".FindByCategory")
      .setString("theAuditCategory", categoryName).listSet(AuditType.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#findByUuidOrName(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public AuditType findByUuidOrName(String id, String auditCategory, String actionName, boolean exceptionIfNull) {
    return findByUuidOrName(id, auditCategory, actionName, exceptionIfNull, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#findByUuidOrName(java.lang.String, java.lang.String, java.lang.String, boolean, QueryOptions)
   */
  public AuditType findByUuidOrName(String id, String auditCategory, String actionName, boolean exceptionIfNull,
      QueryOptions queryOptions) {
    try {
      AuditType auditType = HibernateSession.byHqlStatic()
        .createQuery("from AuditType as theAuditType where theAuditType.id = :theId or " +
        		"(theAuditType.auditCategory = :theAuditCategory and theAuditType.actionName = :theActionName)")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .options(queryOptions)
        .setString("theId", id)
        .setString("theAuditCategory", auditCategory)
        .setString("theActionName", actionName)
        .uniqueResult(AuditType.class);
      if (auditType == null && exceptionIfNull) {
        throw new GroupNotFoundException("Can't find auditType by id: '" + id + "' or auditCategory '" + auditCategory 
            + "', actionName: '" + actionName + "'");
      }
      return auditType;
    } catch (GrouperDAOException e) {
      String error = "Problem find audit type by id: '" + id + "' or auditCategory '" + auditCategory + "', actionName: '" 
        + actionName + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#saveUpdateProperties(edu.internet2.middleware.grouper.audit.AuditType)
   */
  public void saveUpdateProperties(AuditType auditType) {

    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AuditType " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreateTimeLong, " +
        "lastUpdatedDb = :theModifyTimeLong " +
        "where id = :theUuid")
        .setLong("theHibernateVersionNumber", auditType.getHibernateVersionNumber())
        .setLong("theCreateTimeLong", auditType.getCreatedOnDb())
        .setLong("theModifyTimeLong", auditType.getLastUpdatedDb())
        .setString("theContextId", auditType.getContextId())
        .setString("theUuid", auditType.getId()).executeUpdate();
  }
  
} 

