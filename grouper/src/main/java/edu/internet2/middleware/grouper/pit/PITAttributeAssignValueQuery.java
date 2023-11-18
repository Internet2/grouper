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
 * Point in time attribute assign query
 * 
 * @author shilen
 * $Id$
 */
public class PITAttributeAssignValueQuery {
  
  /**
   * query for assignment values that started after this date
   */
  private Date startDateAfter = null;
  
  /**
   * query for assignment values that started before this date
   */
  private Date startDateBefore = null;

  /**
   * query for assignment values that ended after this date or have not ended yet
   */
  private Date endDateAfter = null;
  
  /**
   * query for assignment values that ended before this date
   */
  private Date endDateBefore = null;
  
  /**
   * attribute def name id
   */
  private String attributeDefNameId = null;
  
  /**
   * action id
   */
  private String actionId = null;
  
  /**
   * owner id for assignments on attribute assigns 
   */
  private String ownerAttributeAssignId;
  
  /**
   * owner id for assignments on attribute defs 
   */
  private String ownerAttributeDefId;
  
  /**
   * owner id for assignments on groups
   */
  private String ownerGroupId;
  
  /**
   * owner id for assignments on members
   */
  private String ownerMemberId;
  
  /**
   * owner id for assignments on memberships
   */
  private String ownerMembershipId;
  
  /**
   * owner id for assignments on stems
   */
  private String ownerStemId;
  
  /** 
   * attribute assign id
   */
  private String attributeAssignId;
  
  /** 
   * string value 
   */
  private String valueString;

  /** 
   * floating point value
   */
  private Double valueFloating;

  /** 
   * integer value
   */
  private Long valueInteger;
  
  /** 
   * member id value
   */
  private String valueMemberId;
  
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
  public PITAttributeAssignValueQuery setExtraCriterion(Criterion extraCriterion) {
    this.extraCriterion = extraCriterion;
    return this;
  }

  /**
   * query for assignment values that started after this date
   * @param startDateAfter
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setStartDateAfter(Date startDateAfter) {
    this.startDateAfter = startDateAfter;
    return this;
  }
  
  /**
   * query for assignment values that started before this date
   * @param startDateBefore
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setStartDateBefore(Date startDateBefore) {
    this.startDateBefore = startDateBefore;
    return this;
  }

  /**
   * query for assignment values that ended after this date or have not ended yet
   * @param endDateAfter
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setEndDateAfter(Date endDateAfter) {
    this.endDateAfter = endDateAfter;
    return this;
  }
  
  /**
   * query for assignment values that ended before this date
   * @param endDateBefore
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setEndDateBefore(Date endDateBefore) {
    this.endDateBefore = endDateBefore;
    return this;
  }

  /**
   * query for assignment values that were active at any point in the specified date range
   * @param fromDate
   * @param toDate
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setActiveDateRange(Date fromDate, Date toDate) {
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
  public PITAttributeAssignValueQuery setQueryOptions(QueryOptions queryOptions) {
    this.queryOptions = queryOptions;
    return this;
  }
  
  /**
   * query for assignments with this attributeDefNameId
   * @param attributeDefNameId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setAttributeDefNameId(String attributeDefNameId) {
    this.attributeDefNameId = attributeDefNameId;
    return this;
  }
  
  /**
   * query for assignments with this actionId
   * @param actionId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setActionId(String actionId) {
    this.actionId = actionId;
    return this;
  }
  
  /**
   * query for assignments with this ownerAttributeAssignId
   * @param ownerAttributeAssignId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setOwnerAttributeAssignId(String ownerAttributeAssignId) {
    this.ownerAttributeAssignId = ownerAttributeAssignId;
    return this;
  }
  
  /**
   * query for assignments with this ownerAttributeDefId
   * @param ownerAttributeDefId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setOwnerAttributeDefId(String ownerAttributeDefId) {
    this.ownerAttributeDefId = ownerAttributeDefId;
    return this;
  }
  
  /**
   * query for assignments with this ownerGroupId
   * @param ownerGroupId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setOwnerGroupId(String ownerGroupId) {
    this.ownerGroupId = ownerGroupId;
    return this;
  }
  
  /**
   * query for assignments with this ownerMemberId
   * @param ownerMemberId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setOwnerMemberId(String ownerMemberId) {
    this.ownerMemberId = ownerMemberId;
    return this;
  }
  
  /**
   * query for assignments with this ownerMembershipId
   * @param ownerMembershipId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setOwnerMembershipId(String ownerMembershipId) {
    this.ownerMembershipId = ownerMembershipId;
    return this;
  }
  
  /**
   * query for assignments with this ownerStemId
   * @param ownerStemId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
    return this;
  }
  
  /**
   * query for assignments with given id
   * @param attributeAssignId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
    return this;
  }
  
  /**
   * query for assignments with given value
   * @param valueString
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setValueString(String valueString) {
    this.valueString = valueString;
    return this;
  }
  
  /**
   * query for assignments with given value
   * @param valueMemberId
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setValueMemberId(String valueMemberId) {
    this.valueMemberId = valueMemberId;
    return this;
  }
  
  /**
   * query for assignments with given value
   * @param valueInteger
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setValueInteger(Long valueInteger) {
    this.valueInteger = valueInteger;
    return this;
  }
  
  /**
   * query for assignments with given value
   * @param valueFloating
   * @return this for chaining
   */
  public PITAttributeAssignValueQuery setValueFloating(Double valueFloating) {
    this.valueFloating = valueFloating;
    return this;
  }
  

  /**
   * @return set of PITAttributeAssignValueView objects
   */
  public Set<PITAttributeAssignValueView> execute() {
    
    List<Criterion> criterionList = new ArrayList<Criterion>();

    if (this.attributeAssignId != null) {
      Set<PITAttributeAssign> pitRows = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceId(this.attributeAssignId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITAttributeAssign pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_ATTRIBUTE_ASSIGN_ID, ids));
    }
    
    if (this.ownerAttributeAssignId != null) {
      Set<PITAttributeAssign> pitRows = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceId(this.ownerAttributeAssignId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITAttributeAssign pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, ids));      
    }
    
    if (this.ownerAttributeDefId != null) {
      Set<PITAttributeDef> pitRows = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceId(this.ownerAttributeDefId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITAttributeDef pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_OWNER_ATTRIBUTE_DEF_ID, ids));
    }
    
    if (this.ownerGroupId != null) {
      Set<PITGroup> pitRows = GrouperDAOFactory.getFactory().getPITGroup().findBySourceId(this.ownerGroupId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITGroup pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_OWNER_GROUP_ID, ids));
    }
    
    if (this.ownerMemberId != null) {
      Set<PITMember> pitRows = GrouperDAOFactory.getFactory().getPITMember().findBySourceId(this.ownerMemberId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITMember pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_OWNER_MEMBER_ID, ids));
    }
    
    if (this.ownerMembershipId != null) {
      Set<PITMembership> pitRows = GrouperDAOFactory.getFactory().getPITMembership().findBySourceId(this.ownerMembershipId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITMembership pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_OWNER_MEMBERSHIP_ID, ids));
    }
    
    if (this.ownerStemId != null) {
      Set<PITStem> pitRows = GrouperDAOFactory.getFactory().getPITStem().findBySourceId(this.ownerStemId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITStem pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_OWNER_STEM_ID, ids));
    }
    
    if (this.attributeDefNameId != null) {
      Set<PITAttributeDefName> pitRows = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceId(this.attributeDefNameId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITAttributeDefName pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_ATTRIBUTE_DEF_NAME_ID, ids));
    }
    
    if (this.actionId != null) {
      Set<PITAttributeAssignAction> pitRows = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceId(this.actionId, true);
      Set<String> ids = new LinkedHashSet<String>();
      for (PITAttributeAssignAction pit : pitRows) {
        ids.add(pit.getId());
      }
      
      criterionList.add(Restrictions.in(PITAttributeAssignValueView.FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, ids));
    }
    
    if (this.valueString != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssignValueView.FIELD_VALUE_STRING, this.valueString));
    }
    
    if (this.valueMemberId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssignValueView.FIELD_VALUE_MEMBER_ID, this.valueMemberId));
    }
    
    if (this.valueInteger != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssignValueView.FIELD_VALUE_INTEGER, this.valueInteger));
    }
    
    if (this.valueFloating != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssignValueView.FIELD_VALUE_FLOATING, this.valueFloating));
    }
    
    if (this.startDateAfter != null) {
      criterionList.add(Restrictions.ge(PITAttributeAssignValueView.FIELD_START_TIME_DB, this.startDateAfter.getTime() * 1000));
    }
    
    if (this.startDateBefore != null) {
      criterionList.add(Restrictions.le(PITAttributeAssignValueView.FIELD_START_TIME_DB, this.startDateBefore.getTime() * 1000));
    }
    
    if (this.endDateAfter != null) {
      criterionList.add(Restrictions.or(
          Restrictions.isNull(PITAttributeAssignValueView.FIELD_END_TIME_DB),
          Restrictions.ge(PITAttributeAssignValueView.FIELD_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
    }
    
    if (this.endDateBefore != null) {
      criterionList.add(Restrictions.le(PITAttributeAssignValueView.FIELD_END_TIME_DB, this.endDateBefore.getTime() * 1000));
    }
    
    if (this.extraCriterion != null) {
      criterionList.add(this.extraCriterion);
    }
    
    Criterion allCriteria = HibUtils.listCrit(criterionList);
    
    Set<PITAttributeAssignValueView> results = HibernateSession.byCriteriaStatic()
      .options(this.queryOptions).listSet(PITAttributeAssignValueView.class, allCriteria);
    return results;
  }
}
