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
 * @version $Id: Hib3PermissionEntryDAO.java,v 1.1 2009-10-02 05:57:58 mchyzer Exp $
 */
public class Hib3PermissionEntryDAO extends Hib3DAO implements PermissionEntryDAO {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3PermissionEntryDAO.class);

  /** */
  private static final String KLASS = Hib3PermissionEntryDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findByMemberId(java.lang.String)
   */
  public Set<PermissionEntry> findByMemberId(String memberId) {
    Set<PermissionEntry> permissionEntries = HibernateSession.byHqlStatic().createQuery(
        "select thePermissionEntryRole from PermissionEntryRole thePermissionEntryRole where thePermissionEntryRole.memberId = :theMemberId")
        .setString("theMemberId", memberId)
        .listSet(PermissionEntry.class);

      return permissionEntries;
  }


}
