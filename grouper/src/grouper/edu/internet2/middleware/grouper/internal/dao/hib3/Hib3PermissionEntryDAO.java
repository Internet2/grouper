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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Basic Hibernate <code>PermissionEntry</code> DAO interface.
 * @author  Chris Hyzer
 * @version $Id: Hib3PermissionEntryDAO.java,v 1.4 2009-10-26 04:52:17 mchyzer Exp $
 */
public class Hib3PermissionEntryDAO extends Hib3DAO implements PermissionEntryDAO {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3PermissionEntryDAO.class);

  /** */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3PermissionEntryDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findByMemberId(java.lang.String)
   */
  public Set<PermissionEntry> findByMemberId(String memberId) {
    Set<PermissionEntry> permissionEntries = HibernateSession.byHqlStatic().createQuery(
        "select thePermissionEntryAll from PermissionEntryAll thePermissionEntryAll where thePermissionEntryAll.memberId = :theMemberId")
        .setString("theMemberId", memberId)
        .listSet(PermissionEntry.class);

      return permissionEntries;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#hasPermissionBySubjectIdSourceIdActionAttributeDefName(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public boolean hasPermissionBySubjectIdSourceIdActionAttributeDefName(String subjectId, String sourceId, 
      String action, String attributeDefNameName) {
    Long count = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from PermissionEntryAll thePermissionEntryAll " 
          + "where thePermissionEntryAll.subjectId = :theSubjectId " +
          		"and thePermissionEntryAll.subjectSourceId = :theSubjectSourceId " +
          		"and thePermissionEntryAll.action = :theAction " +
          		"and thePermissionEntryAll.attributeDefNameName = :theAttributeDefNameName")
        .setString("theSubjectId", subjectId)
        .setString("theSubjectSourceId", sourceId)
        .setString("theAction", action)
        .setString("theAttributeDefNameName", attributeDefNameName)
        .uniqueResult(Long.class);

    return count > 0;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findByMemberIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<PermissionEntry> findByMemberIdAndAttributeDefNameId(String memberId,
      String attributeDefNameId) {
    Set<PermissionEntry> permissionEntries = HibernateSession.byHqlStatic().createQuery(
      "select thePermissionEntryAll from PermissionEntryAll thePermissionEntryAll where thePermissionEntryAll.memberId = :theMemberId" +
      " and thePermissionEntryAll.attributeDefNameId = :theAttributeDefNameId")
      .setString("theMemberId", memberId)
      .setString("theAttributeDefNameId", attributeDefNameId)
      .listSet(PermissionEntry.class);
  
    return permissionEntries;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findPermissions(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, java.util.Collection)
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> roleIds,
      Collection<String> actions, Boolean enabled, Collection<String> memberIds) {
    
    int memberIdsSize = GrouperUtil.length(memberIds);
    int roleIdsSize = GrouperUtil.length(roleIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (memberIdsSize == 0 && roleIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in members and/or attributeDefId(s) and/or roleId(s) and/or roleNames and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (memberIdsSize + roleIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many memberIdsSize " + memberIdsSize 
          + " roleIdsSize " + roleIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct pea ";
    
    //doesnt work due to composite key, hibernate puts parens around it and mysql fails
    //String countPrefix = "select count(distinct pea) ";
    
    StringBuilder sqlTables = new StringBuilder(" from PermissionEntryAll pea ");
    
    StringBuilder sqlWhereClause = new StringBuilder("");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "pea.attributeDefId", AttributeDefPrivilege.READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "pea.roleId", AccessPrivilege.READ_PRIVILEGES);

    StringBuilder sql;
    if (changedQuery) {
      if (sqlWhereClause.length() > 0) {
        sql = sqlTables.append(" and ").append(sqlWhereClause);
      } else {
        sql = sqlTables;
      }
    } else {
      sql = sqlTables.append(" where ").append(sqlWhereClause);
    }
    
    if (enabled != null && enabled) {
      sql.append(" and pea.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and pea.enabledDb = 'F' ");
    }
    if (actionsSize > 0) {
      sql.append(" and pea.action in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (roleIdsSize > 0) {
      sql.append(" and pea.roleId in (");
      sql.append(HibUtils.convertToInClause(roleIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and pea.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and pea.attributeDefNameId in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    if (memberIdsSize > 0) {
      sql.append(" and pea.memberId in (");
      sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".findPermissions");

    int maxAssignments = GrouperConfig.getPropertyInt("ws.findPermissions.maxResultSize", 30000);
    
    String sqlString = sql.toString();
    
    //if we did where and, then switch to where
    sqlString = sqlString.replaceAll("where\\s+and", "where");
    
    Set<PermissionEntry> results = byHqlStatic.createQuery(selectPrefix + sqlString).listSet(PermissionEntry.class);

    int size = GrouperUtil.length(results);
    if (maxAssignments >= 0) {

      //doesnt work on mysql i think due to hibernate and composite key
      //size = byHqlStatic.createQuery(countPrefix + sqlString).uniqueResult(long.class);    
      
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

  /**
   * @see PermissionEntryDAO#findPermissionsByAttributeDefDisabledRange(String, Timestamp, Timestamp)
   * find permissions by attribute definition which are about to expire
   */
  public Set<PermissionEntry> findPermissionsByAttributeDefDisabledRange(
      String attributeDefId, Timestamp disabledDateFrom, Timestamp disabledDateTo) {
    
    if (disabledDateFrom == null && disabledDateTo == null) {
      throw new RuntimeException("Need to pass in disabledFrom or disabledTo");
    }
    
    //if they got it backwards, then fix it for them
    if (disabledDateFrom != null && disabledDateTo != null 
        && disabledDateFrom.getTime() > disabledDateTo.getTime()) {
      
      Timestamp temp = disabledDateFrom;
      disabledDateFrom = disabledDateTo;
      disabledDateTo = temp;
      
    }
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder(
        "select thePermissionEntry from PermissionEntryAll as thePermissionEntry, AttributeDefName theAttributeDefName where  "
        + " thePermissionEntry.attributeDefNameId = theAttributeDefName.id "
        + " and theAttributeDefName.attributeDefId   = :theAttributeDefId "
        + " and thePermissionEntry.memberId is not null "
        + " and thePermissionEntry.enabledDb = 'T' ");
    
    if (disabledDateFrom != null) {
      sql.append(" and thePermissionEntry.disabledTimeDb >= :disabledDateFrom ");
      byHqlStatic.setLong( "disabledDateFrom" , disabledDateFrom.getTime() );
    }
    if (disabledDateTo != null) {
      sql.append(" and thePermissionEntry.disabledTimeDb <= :disabledDateTo ");
      byHqlStatic.setLong( "disabledDateTo" , disabledDateTo.getTime() );
    }

    sql.append(
        " and not exists ( select validPermissionEntry.attributeAssignId from PermissionEntryAll as validPermissionEntry " +
        " where validPermissionEntry.attributeDefNameId = thePermissionEntry.attributeDefNameId " +
        " and validPermissionEntry.actionId = thePermissionEntry.actionId " +
        //note, who cares which role it is, if the user has the permission...  (not exactly right if not flattening permissions, but thats ok)
        //" and validPermissionEntry.roleId = thePermissionEntry.roleId " +
        " and validPermissionEntry.memberId = thePermissionEntry.memberId " +
        " and validPermissionEntry.enabledDb = 'T' and ( validPermissionEntry.disabledTimeDb is null ");

    if (disabledDateTo != null) {
      sql.append(" or validPermissionEntry.disabledTimeDb > :disabledDateTo ");
    } else if (disabledDateFrom != null) {
      sql.append(" or validPermissionEntry.disabledTimeDb < :disabledDateFrom ");
    }
    
    
    sql.append(") )");
    
    Set<PermissionEntry> permissionEntries = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindPermissionsByAttributeDefDisabledRange")
      .setString( "theAttributeDefId" , attributeDefId )
      .listSet(PermissionEntry.class);

    return permissionEntries;

    
  }


}
