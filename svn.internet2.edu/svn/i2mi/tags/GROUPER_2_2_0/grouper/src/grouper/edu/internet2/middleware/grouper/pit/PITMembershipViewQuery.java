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
package edu.internet2.middleware.grouper.pit;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * Point in time membership query
 * 
 * @author shilen
 * $Id$
 */
public class PITMembershipViewQuery {
  
  /**
   * query for memberships that started after this date
   */
  private Date startDateAfter = null;
  
  /**
   * query for memberships that started before this date
   */
  private Date startDateBefore = null;

  /**
   * query for memberships that ended after this date or have not ended yet
   */
  private Date endDateAfter = null;
  
  /**
   * query for memberships that ended before this date
   */
  private Date endDateBefore = null;
  
  /**
   * owner group id
   */
  private String ownerGroupId = null;
  
  /**
   * owner stem id
   */
  private String ownerStemId = null;
  
  /**
   * owner attr def id
   */
  private String ownerAttrDefId = null;

  /**
   * member id
   */
  private String memberId = null;
  
  /**
   * field id
   */
  private String fieldId = null;
  
  /**
   * query options
   */
  private QueryOptions queryOptions = null;

  /**
   * extra criteria
   */
  private Criterion extraCriterion;
  
  /**
   * extra criteria
   * @param extraCriterion
   * @return this for chaining
   */
  public PITMembershipViewQuery setExtraCriterion(Criterion extraCriterion) {
    this.extraCriterion = extraCriterion;
    return this;
  }

  /**
   * query for memberships that started after this date
   * @param startDateAfter
   * @return this for chaining
   */
  public PITMembershipViewQuery setStartDateAfter(Date startDateAfter) {
    this.startDateAfter = startDateAfter;
    return this;
  }
  
  /**
   * query for memberships that started before this date
   * @param startDateBefore
   * @return this for chaining
   */
  public PITMembershipViewQuery setStartDateBefore(Date startDateBefore) {
    this.startDateBefore = startDateBefore;
    return this;
  }

  /**
   * query for memberships that ended after this date or have not ended yet
   * @param endDateAfter
   * @return this for chaining
   */
  public PITMembershipViewQuery setEndDateAfter(Date endDateAfter) {
    this.endDateAfter = endDateAfter;
    return this;
  }
  
  /**
   * query for memberships that ended before this date
   * @param endDateBefore
   * @return this for chaining
   */
  public PITMembershipViewQuery setEndDateBefore(Date endDateBefore) {
    this.endDateBefore = endDateBefore;
    return this;
  }

  /**
   * query for memberships that were active at any point in the specified date range
   * @param fromDate
   * @param toDate
   * @return this for chaining
   */
  public PITMembershipViewQuery setActiveDateRange(Date fromDate, Date toDate) {
    this.startDateBefore = toDate;
    this.endDateAfter = fromDate;
    return this;
  }

  /**
   * query options
   * @return query options
   */
  public QueryOptions getQueryOptions() {
    return this.queryOptions;
  }

  /**
   * query options
   * @param queryOptions
   * @return this for chaining
   */
  public PITMembershipViewQuery setQueryOptions(QueryOptions queryOptions) {
    this.queryOptions = queryOptions;
    return this;
  }
  
  /**
   * query for memberships with this ownerGroupId.
   * can only set one of ownerGroupId, ownerStemId, ownerAttrDefId.
   * @param ownerGroupId
   * @return this for chaining
   */
  public PITMembershipViewQuery setOwnerGroupId(String ownerGroupId) {
    this.ownerGroupId = ownerGroupId;
    return this;
  }
  
  /**
   * query for memberships with this ownerStemId.
   * can only set one of ownerGroupId, ownerStemId, ownerAttrDefId.
   * @param ownerStemId
   * @return this for chaining
   */
  public PITMembershipViewQuery setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
    return this;
  }
  
  /**
   * query for memberships with this ownerAttrDefId.
   * can only set one of ownerGroupId, ownerStemId, ownerAttrDefId.
   * @param ownerAttrDefId
   * @return this for chaining
   */
  public PITMembershipViewQuery setOwnerAttrDefId(String ownerAttrDefId) {
    this.ownerAttrDefId = ownerAttrDefId;
    return this;
  }

  /**
   * query for memberships with this memberId
   * @param memberId
   * @return this for chaining
   */
  public PITMembershipViewQuery setMemberId(String memberId) {
    this.memberId = memberId;
    return this;
  }
  
  /**
   * query for memberships with this fieldId
   * @param fieldId
   * @return this for chaining
   */
  public PITMembershipViewQuery setFieldId(String fieldId) {
    this.fieldId = fieldId;
    return this;
  }

  /**
   * 
   * @return set of PITMembershipView objects
   */
  public Set<PITMembershipView> execute() {
    
    List<Criterion> criterionList = new ArrayList<Criterion>();
    
    if (this.ownerGroupId != null) {
      Set<PITGroup> pitGroups = GrouperDAOFactory.getFactory().getPITGroup().findBySourceId(this.ownerGroupId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITGroup pitGroup : pitGroups) {
        ids.add(pitGroup.getId());
      }
      
      criterionList.add(Restrictions.in(PITMembershipView.FIELD_OWNER_ID, ids));
    } else if (this.ownerStemId != null) {
      Set<PITStem> pitStems = GrouperDAOFactory.getFactory().getPITStem().findBySourceId(this.ownerStemId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITStem pitStem : pitStems) {
        ids.add(pitStem.getId());
      }
      
      criterionList.add(Restrictions.in(PITMembershipView.FIELD_OWNER_ID, ids));
    } else if (this.ownerAttrDefId != null) {
      Set<PITAttributeDef> pitAttributeDefs = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceId(this.ownerAttrDefId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITAttributeDef pitAttributeDef : pitAttributeDefs) {
        ids.add(pitAttributeDef.getId());
      }
      
      criterionList.add(Restrictions.in(PITMembershipView.FIELD_OWNER_ID, ids));
    }
    
    if (this.memberId != null) {
      Set<PITMember> pitMembers = GrouperDAOFactory.getFactory().getPITMember().findBySourceId(this.memberId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITMember pitMember : pitMembers) {
        ids.add(pitMember.getId());
      }
      
      criterionList.add(Restrictions.in(PITMembershipView.FIELD_MEMBER_ID, ids));
    }
    
    if (this.fieldId != null) {
      Set<PITField> pitFields = GrouperDAOFactory.getFactory().getPITField().findBySourceId(this.fieldId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITField pitField : pitFields) {
        ids.add(pitField.getId());
      }
      
      criterionList.add(Restrictions.in(PITMembershipView.FIELD_FIELD_ID, ids));
    }
    
    if (this.startDateAfter != null) {
      criterionList.add(Restrictions.or(
          Restrictions.ge(PITMembershipView.FIELD_MEMBERSHIP_START_TIME_DB, this.startDateAfter.getTime() * 1000), 
          Restrictions.ge(PITMembershipView.FIELD_GROUP_SET_START_TIME_DB, this.startDateAfter.getTime() * 1000)));
    }
    
    if (this.startDateBefore != null) {
      criterionList.add(Restrictions.le(PITMembershipView.FIELD_MEMBERSHIP_START_TIME_DB, this.startDateBefore.getTime() * 1000));
      criterionList.add(Restrictions.le(PITMembershipView.FIELD_GROUP_SET_START_TIME_DB, this.startDateBefore.getTime() * 1000));
    }
    
    if (this.endDateAfter != null) {
      criterionList.add(Restrictions.or(
          Restrictions.isNull(PITMembershipView.FIELD_MEMBERSHIP_END_TIME_DB),
          Restrictions.ge(PITMembershipView.FIELD_MEMBERSHIP_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
      
      criterionList.add(Restrictions.or(
          Restrictions.isNull(PITMembershipView.FIELD_GROUP_SET_END_TIME_DB),
          Restrictions.ge(PITMembershipView.FIELD_GROUP_SET_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
    }
    
    if (this.endDateBefore != null) {
      criterionList.add(Restrictions.or(
          Restrictions.le(PITMembershipView.FIELD_MEMBERSHIP_END_TIME_DB, this.endDateBefore.getTime() * 1000), 
          Restrictions.le(PITMembershipView.FIELD_GROUP_SET_END_TIME_DB, this.endDateBefore.getTime() * 1000)));
    }
    
    if (this.extraCriterion != null) {
      criterionList.add(this.extraCriterion);
    }
    
    Criterion allCriteria = HibUtils.listCrit(criterionList);
    
    Set<PITMembershipView> results = HibernateSession.byCriteriaStatic()
      .options(this.queryOptions).listSet(PITMembershipView.class, allCriteria);
    return results;
  }
}
