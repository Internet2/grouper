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
 * Point in time permission query
 * 
 * @author shilen
 * $Id$
 */
public class PITPermissionAllViewQuery {
  
  /**
   * query for permissions that started after this date
   */
  private Date startDateAfter = null;
  
  /**
   * query for permissions that started before this date
   */
  private Date startDateBefore = null;

  /**
   * query for permissions that ended after this date or have not ended yet
   */
  private Date endDateAfter = null;
  
  /**
   * query for permissions that ended before this date
   */
  private Date endDateBefore = null;
  
  /**
   * attribute def name id
   */
  private String attributeDefNameId = null;

  /**
   * member id
   */
  private String memberId = null;
  
  /**
   * action id
   */
  private String actionId = null;
  
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
  public PITPermissionAllViewQuery setExtraCriterion(Criterion extraCriterion) {
    this.extraCriterion = extraCriterion;
    return this;
  }

  /**
   * query for permissions that started after this date
   * @param startDateAfter
   * @return this for chaining
   */
  public PITPermissionAllViewQuery setStartDateAfter(Date startDateAfter) {
    this.startDateAfter = startDateAfter;
    return this;
  }
  
  /**
   * query for permissions that started before this date
   * @param startDateBefore
   * @return this for chaining
   */
  public PITPermissionAllViewQuery setStartDateBefore(Date startDateBefore) {
    this.startDateBefore = startDateBefore;
    return this;
  }

  /**
   * query for permissions that ended after this date or have not ended yet
   * @param endDateAfter
   * @return this for chaining
   */
  public PITPermissionAllViewQuery setEndDateAfter(Date endDateAfter) {
    this.endDateAfter = endDateAfter;
    return this;
  }
  
  /**
   * query for permissions that ended before this date
   * @param endDateBefore
   * @return this for chaining
   */
  public PITPermissionAllViewQuery setEndDateBefore(Date endDateBefore) {
    this.endDateBefore = endDateBefore;
    return this;
  }

  /**
   * query for permissions that were active at any point in the specified date range
   * @param fromDate
   * @param toDate
   * @return this for chaining
   */
  public PITPermissionAllViewQuery setActiveDateRange(Date fromDate, Date toDate) {
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
  public PITPermissionAllViewQuery setQueryOptions(QueryOptions queryOptions) {
    this.queryOptions = queryOptions;
    return this;
  }
  
  /**
   * query for permissions with this attributeDefNameId
   * @param attributeDefNameId
   * @return this for chaining
   */
  public PITPermissionAllViewQuery setAttributeDefNameId(String attributeDefNameId) {
    this.attributeDefNameId = attributeDefNameId;
    return this;
  }

  /**
   * query for permissions with this memberId
   * @param memberId
   * @return this for chaining
   */
  public PITPermissionAllViewQuery setMemberId(String memberId) {
    this.memberId = memberId;
    return this;
  }
  
  /**
   * query for permissions with this actionId
   * @param actionId
   * @return this for chaining
   */
  public PITPermissionAllViewQuery setActionId(String actionId) {
    this.actionId = actionId;
    return this;
  }

  /**
   * 
   * @return set of PITPermissionAllView objects
   */
  public Set<PITPermissionAllView> execute() {
    
    List<Criterion> criterionList = new ArrayList<Criterion>();
    
    if (this.attributeDefNameId != null) {
      criterionList.add(Restrictions.eq(PITPermissionAllView.FIELD_ATTRIBUTE_DEF_NAME_ID, this.attributeDefNameId));
    }
    
    if (this.memberId != null) {
      criterionList.add(Restrictions.eq(PITPermissionAllView.FIELD_MEMBER_ID, this.memberId));
    }
    
    if (this.actionId != null) {
      criterionList.add(Restrictions.eq(PITPermissionAllView.FIELD_ACTION_ID, this.actionId));
    }
    
    if (this.startDateAfter != null) {
      criterionList.add(Restrictions.disjunction()
          .add(Expression.ge(PITPermissionAllView.FIELD_MEMBERSHIP_START_TIME_DB, this.startDateAfter.getTime() * 1000))
          .add(Expression.ge(PITPermissionAllView.FIELD_GROUP_SET_START_TIME_DB, this.startDateAfter.getTime() * 1000)) 
          .add(Expression.ge(PITPermissionAllView.FIELD_ACTION_SET_START_TIME_DB, this.startDateAfter.getTime() * 1000)) 
          .add(Expression.ge(PITPermissionAllView.FIELD_ATTRIBUTE_DEF_NAME_SET_START_TIME_DB, this.startDateAfter.getTime() * 1000)) 
          .add(Expression.ge(PITPermissionAllView.FIELD_ROLE_SET_START_TIME_DB, this.startDateAfter.getTime() * 1000))
          .add(Expression.ge(PITPermissionAllView.FIELD_ATTRIBUTE_ASSIGN_START_TIME_DB, this.startDateAfter.getTime() * 1000)));
    }
    
    if (this.startDateBefore != null) {
      criterionList.add(Expression.le(PITPermissionAllView.FIELD_MEMBERSHIP_START_TIME_DB, this.startDateBefore.getTime() * 1000));
      criterionList.add(Expression.le(PITPermissionAllView.FIELD_GROUP_SET_START_TIME_DB, this.startDateBefore.getTime() * 1000));
      criterionList.add(Expression.le(PITPermissionAllView.FIELD_ACTION_SET_START_TIME_DB, this.startDateBefore.getTime() * 1000));
      criterionList.add(Expression.le(PITPermissionAllView.FIELD_ATTRIBUTE_DEF_NAME_SET_START_TIME_DB, this.startDateBefore.getTime() * 1000));
      criterionList.add(Expression.le(PITPermissionAllView.FIELD_ROLE_SET_START_TIME_DB, this.startDateBefore.getTime() * 1000));
      criterionList.add(Expression.le(PITPermissionAllView.FIELD_ATTRIBUTE_ASSIGN_START_TIME_DB, this.startDateBefore.getTime() * 1000));
    }
    
    if (this.endDateAfter != null) {
      criterionList.add(Restrictions.or(
          Expression.isNull(PITPermissionAllView.FIELD_MEMBERSHIP_END_TIME_DB),
          Expression.ge(PITPermissionAllView.FIELD_MEMBERSHIP_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
      
      criterionList.add(Restrictions.or(
          Expression.isNull(PITPermissionAllView.FIELD_GROUP_SET_END_TIME_DB),
          Expression.ge(PITPermissionAllView.FIELD_GROUP_SET_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
      
      criterionList.add(Restrictions.or(
          Expression.isNull(PITPermissionAllView.FIELD_ACTION_SET_END_TIME_DB),
          Expression.ge(PITPermissionAllView.FIELD_ACTION_SET_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
      
      criterionList.add(Restrictions.or(
          Expression.isNull(PITPermissionAllView.FIELD_ATTRIBUTE_DEF_NAME_SET_END_TIME_DB),
          Expression.ge(PITPermissionAllView.FIELD_ATTRIBUTE_DEF_NAME_SET_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
      
      criterionList.add(Restrictions.or(
          Expression.isNull(PITPermissionAllView.FIELD_ROLE_SET_END_TIME_DB),
          Expression.ge(PITPermissionAllView.FIELD_ROLE_SET_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
      
      criterionList.add(Restrictions.or(
          Expression.isNull(PITPermissionAllView.FIELD_ATTRIBUTE_ASSIGN_END_TIME_DB),
          Expression.ge(PITPermissionAllView.FIELD_ATTRIBUTE_ASSIGN_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
    }
    
    if (this.endDateBefore != null) {
      criterionList.add(Restrictions.disjunction()
          .add(Expression.le(PITPermissionAllView.FIELD_MEMBERSHIP_END_TIME_DB, this.endDateBefore.getTime() * 1000))
          .add(Expression.le(PITPermissionAllView.FIELD_GROUP_SET_END_TIME_DB, this.endDateBefore.getTime() * 1000))
          .add(Expression.le(PITPermissionAllView.FIELD_ACTION_SET_END_TIME_DB, this.endDateBefore.getTime() * 1000))
          .add(Expression.le(PITPermissionAllView.FIELD_ATTRIBUTE_DEF_NAME_SET_END_TIME_DB, this.endDateBefore.getTime() * 1000))
          .add(Expression.le(PITPermissionAllView.FIELD_ROLE_SET_END_TIME_DB, this.endDateBefore.getTime() * 1000))
          .add(Expression.le(PITPermissionAllView.FIELD_ATTRIBUTE_ASSIGN_END_TIME_DB, this.endDateBefore.getTime() * 1000)));
    }
    
    if (this.extraCriterion != null) {
      criterionList.add(this.extraCriterion);
    }
    
    Criterion allCriteria = HibUtils.listCrit(criterionList);
    
    Set<PITPermissionAllView> results = HibernateSession.byCriteriaStatic()
      .options(this.queryOptions).listSet(PITPermissionAllView.class, allCriteria);
    return results;
  }
}