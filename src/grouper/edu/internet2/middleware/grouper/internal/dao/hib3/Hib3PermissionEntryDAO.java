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
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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


}
