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
/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.filter;

import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

/**
 * Query by groups that had a membership change after the specified date. <p/>
 * 
 * @author shilen
 * 
 * @version $Id: GroupMembershipModifiedAfterFilter.java,v 1.1 2009-03-19 13:46:23 shilen Exp $
 */
public class GroupMembershipModifiedAfterFilter extends BaseQueryFilter {

  private Date d;

  private Stem ns;

  /**
   * {@link QueryFilter} that returns groups that had a membership change
   * after the specified date. 
   * <p/>
   * @param   d   Find groups with membership changes after this date.
   * @param   ns  Restrict results to within this stem.
   */
  public GroupMembershipModifiedAfterFilter(Date d, Stem ns) {
    this.d = (Date) d.clone();
    this.ns = ns;
  }

  public Set getResults(GrouperSession s) throws QueryException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set results;

    if (ns.isRootStem()) {
      results = PrivilegeHelper.canViewGroups(s, GrouperDAOFactory.getFactory()
          .getGroup().findAllByLastMembershipAfter(this.d));
    } else {
      results = PrivilegeHelper.canViewGroups(s, GrouperDAOFactory.getFactory()
          .getGroup().findAllByLastMembershipAfter(this.d, getStringForScope(ns)));
    }
    return results;
  }

}
