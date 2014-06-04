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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.RoleSetViewDAO;
import edu.internet2.middleware.grouper.permissions.role.RoleSetView;

/**
 * Data Access Object for role set view
 * @author  mchyzer
 * @version $Id: Hib3RoleSetViewDAO.java,v 1.2 2009-10-02 05:57:58 mchyzer Exp $
 */
public class Hib3RoleSetViewDAO extends Hib3DAO implements RoleSetViewDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3RoleSetViewDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetViewDAO#findByRoleSetViews(java.util.Set)
   */
  public Set<RoleSetView> findByRoleSetViews(Set<String> roleNames) {

    Criterion ifHasCriteria = Restrictions.in(RoleSetView.FIELD_IF_HAS_ROLE_NAME, roleNames);
    Criterion thenHasCriteria = Restrictions.in(RoleSetView.FIELD_THEN_HAS_ROLE_NAME, roleNames);
    
    QueryOptions queryOptions = new QueryOptions();
    //ifHas.name, thenHas.name, gadns.depth, gadnParentIfHas.name, gadnParentThenHas.name
    QuerySort querySort = QuerySort.asc(RoleSetView.FIELD_PARENT_THEN_HAS_NAME);
    querySort.insertSortToBeginning(RoleSetView.FIELD_PARENT_IF_HAS_NAME, true);
    querySort.insertSortToBeginning(RoleSetView.FIELD_DEPTH, true);
    querySort.insertSortToBeginning(RoleSetView.FIELD_THEN_HAS_ROLE_NAME, true);
    querySort.insertSortToBeginning(RoleSetView.FIELD_IF_HAS_ROLE_NAME, true);
    
    queryOptions.sort(querySort);

    return HibernateSession.byCriteriaStatic().options(queryOptions).listSet(RoleSetView.class, 
        HibUtils.listCritOr(ifHasCriteria, thenHasCriteria));
  }

} 

