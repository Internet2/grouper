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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
/**
 * @author shilen
 * $Id$
 */
public class Hib3PITPermissionAllViewDAO extends Hib3DAO implements PITPermissionAllViewDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITPermissionAllViewDAO.class.getName();
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO#findPermissions(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.sql.Timestamp, java.sql.Timestamp)
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefSourceIds, Collection<String> attributeDefNameSourceIds, 
      Collection<String> roleSourceIds, Collection<String> actions, Collection<String> memberSourceIds, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo) {
    
    int memberIdsSize = GrouperUtil.length(memberSourceIds);
    int roleIdsSize = GrouperUtil.length(roleSourceIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefSourceIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameSourceIds);
    
    if (memberIdsSize == 0 && roleIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in members and/or attributeDefId(s) and/or roleId(s) and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (memberIdsSize + roleIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many memberIdsSize " + memberIdsSize 
          + " roleIdsSize " + roleIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct pea ";
    
    StringBuilder sqlTables = new StringBuilder(" from PITPermissionAllView pea ");
    
    StringBuilder sqlWhereClause = new StringBuilder("");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "pea.attributeDefSourceId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "pea.roleSourceId", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);

    StringBuilder sql;
    if (sqlWhereClause.length() > 0) {
      if (changedQuery) {
        sql = sqlTables.append(" and ").append(sqlWhereClause);
        
      } else {
        sql = sqlTables.append(" where ").append(sqlWhereClause);
        
      }
    } else {
      //where and will be removed later on
      sql = sqlTables.append(" where ");
    }
    
    if (actionsSize > 0) {
      sql.append(" and pea.action in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (roleIdsSize > 0) {
      sql.append(" and pea.roleSourceId in (");
      sql.append(HibUtils.convertToInClause(roleSourceIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and pea.attributeDefSourceId in (");
      sql.append(HibUtils.convertToInClause(attributeDefSourceIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and pea.attributeDefNameSourceId in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameSourceIds, byHqlStatic));
      sql.append(") ");
    }
    if (memberIdsSize > 0) {
      sql.append(" and pea.memberSourceId in (");
      sql.append(HibUtils.convertToInClause(memberSourceIds, byHqlStatic));
      sql.append(") ");
    }
    
    if (pointInTimeFrom != null) {
      Long endDateAfter = pointInTimeFrom.getTime() * 1000;
      sql.append(" and (pea.membershipEndTimeDb is null or pea.membershipEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.groupSetEndTimeDb is null or pea.groupSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.actionSetEndTimeDb is null or pea.actionSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.attributeDefNameSetEndTimeDb is null or pea.attributeDefNameSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.roleSetEndTimeDb is null or pea.roleSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (pea.attributeAssignEndTimeDb is null or pea.attributeAssignEndTimeDb > '" + endDateAfter + "')");
    }
    
    if (pointInTimeTo != null) {
      Long startDateBefore = pointInTimeTo.getTime() * 1000;
      sql.append(" and pea.membershipStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.groupSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.actionSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.attributeDefNameSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.roleSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and pea.attributeAssignStartTimeDb < '" + startDateBefore + "'");
    }
    
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".findPermissions");

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findPermissions.maxResultSize", 30000);
    
    String sqlString = sql.toString();
    
    //if we did where and, then switch to where
    sqlString = sqlString.replaceAll("where\\s+and", "where");
    
    Set<PermissionEntry> results = byHqlStatic.createQuery(selectPrefix + sqlString).listSet(PermissionEntry.class);

    int size = GrouperUtil.length(results);
    if (maxAssignments >= 0) {
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
    }

    //nothing to filter
    if (size == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterPermissions(grouperSessionSubject, results);
    
    //we should be down to the secure list
    return results;
  }
}
