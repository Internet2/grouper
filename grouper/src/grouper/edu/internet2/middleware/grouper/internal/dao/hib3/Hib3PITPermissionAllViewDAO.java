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
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITPermissionAllView;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
/**
 * @author shilen
 * $Id$
 */
public class Hib3PITPermissionAllViewDAO extends Hib3DAO implements PITPermissionAllViewDAO {

  private static final String PERMISSION_ENTRY_COLUMNS = "gr.nameDb as roleName, gm.subjectSourceId as subjectSourceId, gm.subjectId as subjectId, gaaa.nameDb as action, gadn.nameDb as attributeDefNameName, gr.id as roleId, gadn.attributeDefId as attributeDefId, gm.id as memberId, gadn.id as attributeDefNameId, gaaa.id as actionId, gmav.depth as membershipDepth, grs.depth as roleSetDepth, gadns.depth as attributeDefNameSetDepth, gaaas.depth as attributeAssignActionSetDepth, gmav.membershipId as membershipId, gmav.groupSetId as groupSetId, grs.id as roleSetId, gadns.id as attributeDefNameSetId, gaaas.id as actionSetId, gaa.id as attributeAssignId, gaa.attributeAssignTypeDb as attributeAssignTypeDb, gmav.groupSetActiveDb as groupSetActiveDb, gmav.groupSetStartTimeDb as groupSetStartTimeDb, gmav.groupSetEndTimeDb as groupSetEndTimeDb, gmav.membershipActiveDb as membershipActiveDb, gmav.membershipStartTimeDb as membershipStartTimeDb, gmav.membershipEndTimeDb as membershipEndTimeDb, grs.activeDb as roleSetActiveDb, grs.startTimeDb as roleSetStartTimeDb, grs.endTimeDb as roleSetEndTimeDb, gaaas.activeDb as actionSetActiveDb, gaaas.startTimeDb as actionSetStartTimeDb, gaaas.endTimeDb as actionSetEndTimeDb, gadns.activeDb as attributeDefNameSetActiveDb, gadns.startTimeDb as attributeDefNameSetStartTimeDb, gadns.endTimeDb as attributeDefNameSetEndTimeDb, gaa.activeDb as attributeAssignActiveDb, gaa.startTimeDb as attributeAssignStartTimeDb, gaa.endTimeDb as attributeAssignEndTimeDb, gaa.disallowedDb as disallowedDb, gaaa.sourceId as actionSourceId, gr.sourceId as roleSourceId, gadn.sourceId as attributeDefNameSourceId, gad.sourceId as attributeDefSourceId, gm.sourceId as memberSourceId, gmav.membershipSourceId as membershipSourceId, gaa.sourceId as attributeAssignSourceId";

  private static final String PERMISSION_ENTRY_TABLES = "PITGroup gr, PITMembershipView gmav, PITMember gm, PITField gf, PITRoleSet grs, PITAttributeDef gad, PITAttributeAssign gaa, PITAttributeDefName gadn, PITAttributeDefNameSet gadns, PITAttributeAssignAction gaaa, PITAttributeAssignActionSet gaaas";
  
  private static final String PERMISSION_ENTRY_WHERE_CLAUSE = "gmav.ownerGroupId = gr.id and gmav.fieldId = gf.id and gf.typeDb = 'list' and gf.nameDb = 'members' and gmav.memberId = gm.id and gadn.attributeDefId = gad.id and gad.attributeDefTypeDb = 'perm' and gaa.attributeDefNameId = gadns.ifHasAttributeDefNameId and gadn.id = gadns.thenHasAttributeDefNameId and gaa.attributeAssignActionId = gaaas.ifHasAttrAssignActionId and gaaa.id = gaaas.thenHasAttrAssignActionId and ((grs.ifHasRoleId = gr.id and gaa.ownerGroupId = grs.thenHasRoleId and gaa.attributeAssignTypeDb = 'group') or (gmav.ownerGroupId = gaa.ownerGroupId  and gmav.memberId = gaa.ownerMemberId and gaa.attributeAssignTypeDb = 'any_mem' and grs.ifHasRoleId = gr.id and grs.depth='0'))";
  
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

    String selectPrefix = "select distinct " + PERMISSION_ENTRY_COLUMNS + " ";
    
    StringBuilder sqlTables = new StringBuilder(" from " + PERMISSION_ENTRY_TABLES + " ");
    
    StringBuilder sqlWhereClause = new StringBuilder(" " + PERMISSION_ENTRY_WHERE_CLAUSE + " ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "gad.sourceId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "gr.sourceId", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
    
    StringBuilder sql;
    if (changedQuery) {
      if (sqlWhereClause.length() > 0) {
        sql = sqlTables.append(" and ").append(sqlWhereClause);
      } else {
        throw new RuntimeException("Unexpected.");
      }
    } else {
      sql = sqlTables.append(" where ").append(sqlWhereClause);
    }
    
    if (actionsSize > 0) {
      sql.append(" and gaaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (roleIdsSize > 0) {
      sql.append(" and gr.sourceId in (");
      sql.append(HibUtils.convertToInClause(roleSourceIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and gad.sourceId in (");
      sql.append(HibUtils.convertToInClause(attributeDefSourceIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and gadn.sourceId in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameSourceIds, byHqlStatic));
      sql.append(") ");
    }
    if (memberIdsSize > 0) {
      sql.append(" and gm.sourceId in (");
      sql.append(HibUtils.convertToInClause(memberSourceIds, byHqlStatic));
      sql.append(") ");
    }
    
    if (pointInTimeFrom != null) {
      Long endDateAfter = pointInTimeFrom.getTime() * 1000;
      sql.append(" and (gmav.membershipEndTimeDb is null or gmav.membershipEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (gmav.groupSetEndTimeDb is null or gmav.groupSetEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (gaaas.endTimeDb is null or gaaas.endTimeDb > '" + endDateAfter + "')");
      sql.append(" and (gadns.endTimeDb is null or gadns.endTimeDb > '" + endDateAfter + "')");
      sql.append(" and (grs.endTimeDb is null or grs.endTimeDb > '" + endDateAfter + "')");
      sql.append(" and (gaa.endTimeDb is null or gaa.endTimeDb > '" + endDateAfter + "')");
    }
    
    if (pointInTimeTo != null) {
      Long startDateBefore = pointInTimeTo.getTime() * 1000;
      sql.append(" and gmav.membershipStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and gmav.groupSetStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and gaaas.startTimeDb < '" + startDateBefore + "'");
      sql.append(" and gadns.startTimeDb < '" + startDateBefore + "'");
      sql.append(" and grs.startTimeDb < '" + startDateBefore + "'");
      sql.append(" and gaa.startTimeDb < '" + startDateBefore + "'");
    }
    
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".findPermissions");

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findPermissions.maxResultSize", 30000);
    
    String sqlString = sql.toString();
    
    //if we did where and, then switch to where
    sqlString = sqlString.replaceAll("where\\s+and", "where");
    
    Set<PITPermissionAllView> resultsTemp = byHqlStatic.createQuery(selectPrefix + sqlString)
      .assignConvertHqlColumnsToObject(true)
      .listSet(PITPermissionAllView.class);

    int size = GrouperUtil.length(resultsTemp);
    if (maxAssignments >= 0) {
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
    }
    
    Set<PermissionEntry> results = new LinkedHashSet<PermissionEntry>(resultsTemp);

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
