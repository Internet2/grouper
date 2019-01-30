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
/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITMembershipView;
import edu.internet2.middleware.subject.Source;

/**
 * 
 */
public interface PITMembershipViewDAO extends GrouperDAO {


  /**
   * @param pitMembership
   * @return set
   */
  public Set<PITGroupSet> findPITGroupSetsJoinedWithNewPITMembership(PITMembership pitMembership);
  
  /**
   * @param pitGroupSet
   * @return set
   */
  public Set<PITMembership> findPITMembershipsJoinedWithNewPITGroupSet(PITGroupSet pitGroupSet);
  
  /**
   * @param pitMembership
   * @return set
   */
  public Set<PITGroupSet> findPITGroupSetsJoinedWithOldPITMembership(PITMembership pitMembership);
  
  /**
   * @param pitGroupSet
   * @return set
   */
  public Set<PITMembership> findPITMembershipsJoinedWithOldPITGroupSet(PITGroupSet pitGroupSet);
  
  /**
   * @param ownerId
   * @param memberId
   * @param fieldId
   * @param activeOnly
   * @return set
   */
  public Set<PITMembershipView> findByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId, boolean activeOnly);
  
  /**
   * Get members by owner and field.
   * @param ownerId
   * @param fieldId
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param sources
   * @param queryOptions
   * @return set of members
   */
  public Set<Member> findAllMembersByPITOwnerAndPITField(String ownerId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, Set<Source> sources, QueryOptions queryOptions);
  
  /**
   * Get memberships by owner, member, and field.
   * @param ownerId
   * @param memberId
   * @param fieldId
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param queryOptions
   * @return set of pit memberships
   */
  public Set<PITMembershipView> findAllByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions);
  
  /**
   * @param totalGroupIds
   * @param totalMemberIds
   * @param fields
   * @param sources
   * @param checkSecurity
   * @param fieldType
   * @param queryOptionsForMember
   * @param filterForMember
   * @param splitScopeForMember
   * @param hasFieldForMember
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @return set of PITMembership, PITGroup, PITMember, and Member
   */
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<Field> fields,
      Set<Source> sources, Boolean checkSecurity, FieldType fieldType,
      QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, Timestamp pointInTimeFrom, Timestamp pointInTimeTo);
}
