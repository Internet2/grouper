package edu.internet2.middleware.grouper.pit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;


/**
 * Point in time attribute assign query
 * 
 * @author shilen
 * $Id$
 */
public class PITAttributeAssignQuery {
  
  /**
   * query for assignments that started after this date
   */
  private Date startDateAfter = null;
  
  /**
   * query for assignments that started before this date
   */
  private Date startDateBefore = null;

  /**
   * query for assignments that ended after this date or have not ended yet
   */
  private Date endDateAfter = null;
  
  /**
   * query for assignments that ended before this date
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
  public PITAttributeAssignQuery setExtraCriterion(Criterion extraCriterion) {
    this.extraCriterion = extraCriterion;
    return this;
  }

  /**
   * query for assignments that started after this date
   * @param startDateAfter
   * @return this for chaining
   */
  public PITAttributeAssignQuery setStartDateAfter(Date startDateAfter) {
    this.startDateAfter = startDateAfter;
    return this;
  }
  
  /**
   * query for assignments that started before this date
   * @param startDateBefore
   * @return this for chaining
   */
  public PITAttributeAssignQuery setStartDateBefore(Date startDateBefore) {
    this.startDateBefore = startDateBefore;
    return this;
  }

  /**
   * query for assignments that ended after this date or have not ended yet
   * @param endDateAfter
   * @return this for chaining
   */
  public PITAttributeAssignQuery setEndDateAfter(Date endDateAfter) {
    this.endDateAfter = endDateAfter;
    return this;
  }
  
  /**
   * query for assignments that ended before this date
   * @param endDateBefore
   * @return this for chaining
   */
  public PITAttributeAssignQuery setEndDateBefore(Date endDateBefore) {
    this.endDateBefore = endDateBefore;
    return this;
  }

  /**
   * query for assignments that were active at any point in the specified date range
   * @param fromDate
   * @param toDate
   * @return this for chaining
   */
  public PITAttributeAssignQuery setActiveDateRange(Date fromDate, Date toDate) {
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
  public PITAttributeAssignQuery setQueryOptions(QueryOptions queryOptions) {
    this.queryOptions = queryOptions;
    return this;
  }
  
  /**
   * query for assignments with this attributeDefNameId
   * @param attributeDefNameId
   * @return this for chaining
   */
  public PITAttributeAssignQuery setAttributeDefNameId(String attributeDefNameId) {
    this.attributeDefNameId = attributeDefNameId;
    return this;
  }
  
  /**
   * query for assignments with this actionId
   * @param actionId
   * @return this for chaining
   */
  public PITAttributeAssignQuery setActionId(String actionId) {
    this.actionId = actionId;
    return this;
  }
  
  /**
   * query for assignments with this ownerAttributeAssignId
   * @param ownerAttributeAssignId
   * @return this for chaining
   */
  public PITAttributeAssignQuery setOwnerAttributeAssignId(String ownerAttributeAssignId) {
    this.ownerAttributeAssignId = ownerAttributeAssignId;
    return this;
  }
  
  /**
   * query for assignments with this ownerAttributeDefId
   * @param ownerAttributeDefId
   * @return this for chaining
   */
  public PITAttributeAssignQuery setOwnerAttributeDefId(String ownerAttributeDefId) {
    this.ownerAttributeDefId = ownerAttributeDefId;
    return this;
  }
  
  /**
   * query for assignments with this ownerGroupId
   * @param ownerGroupId
   * @return this for chaining
   */
  public PITAttributeAssignQuery setOwnerGroupId(String ownerGroupId) {
    this.ownerGroupId = ownerGroupId;
    return this;
  }
  
  /**
   * query for assignments with this ownerMemberId
   * @param ownerMemberId
   * @return this for chaining
   */
  public PITAttributeAssignQuery setOwnerMemberId(String ownerMemberId) {
    this.ownerMemberId = ownerMemberId;
    return this;
  }
  
  /**
   * query for assignments with this ownerMembershipId
   * @param ownerMembershipId
   * @return this for chaining
   */
  public PITAttributeAssignQuery setOwnerMembershipId(String ownerMembershipId) {
    this.ownerMembershipId = ownerMembershipId;
    return this;
  }
  
  /**
   * query for assignments with this ownerStemId
   * @param ownerStemId
   * @return this for chaining
   */
  public PITAttributeAssignQuery setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
    return this;
  }
  

  /**
   * 
   * @return set of PITAttributeAssign objects
   */
  public Set<PITAttributeAssign> execute() {
    
    List<Criterion> criterionList = new ArrayList<Criterion>();
    
    if (this.ownerAttributeAssignId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssign.FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, this.ownerAttributeAssignId));
    }
    
    if (this.ownerAttributeDefId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssign.FIELD_OWNER_ATTRIBUTE_DEF_ID, this.ownerAttributeDefId));
    }
    
    if (this.ownerGroupId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssign.FIELD_OWNER_GROUP_ID, this.ownerGroupId));
    }
    
    if (this.ownerMemberId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssign.FIELD_OWNER_MEMBER_ID, this.ownerMemberId));
    }
    
    if (this.ownerMembershipId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssign.FIELD_OWNER_MEMBERSHIP_ID, this.ownerMembershipId));
    }
    
    if (this.ownerStemId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssign.FIELD_OWNER_STEM_ID, this.ownerStemId));
    }
    
    if (this.attributeDefNameId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssign.FIELD_ATTRIBUTE_DEF_NAME_ID, this.attributeDefNameId));
    }
    
    if (this.actionId != null) {
      criterionList.add(Restrictions.eq(PITAttributeAssign.FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, this.actionId));
    }
    
    if (this.startDateAfter != null) {
      criterionList.add(Expression.ge(PITAttributeAssign.FIELD_START_TIME_DB, this.startDateAfter.getTime() * 1000));
    }
    
    if (this.startDateBefore != null) {
      criterionList.add(Expression.le(PITAttributeAssign.FIELD_START_TIME_DB, this.startDateBefore.getTime() * 1000));
    }
    
    if (this.endDateAfter != null) {
      criterionList.add(Restrictions.or(
          Expression.isNull(PITAttributeAssign.FIELD_END_TIME_DB),
          Expression.ge(PITAttributeAssign.FIELD_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
    }
    
    if (this.endDateBefore != null) {
      criterionList.add(Expression.le(PITAttributeAssign.FIELD_END_TIME_DB, this.endDateBefore.getTime() * 1000));
    }
    
    if (this.extraCriterion != null) {
      criterionList.add(this.extraCriterion);
    }
    
    Criterion allCriteria = HibUtils.listCrit(criterionList);
    
    Set<PITAttributeAssign> results = HibernateSession.byCriteriaStatic()
      .options(this.queryOptions).listSet(PITAttributeAssign.class, allCriteria);
    return results;
  }
}