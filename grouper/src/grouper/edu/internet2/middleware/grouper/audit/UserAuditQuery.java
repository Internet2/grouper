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
/*
 * @author mchyzer
 * $Id: UserAuditQuery.java,v 1.5 2009-08-11 14:13:51 isgwb Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * use method chaining and hibernate criteria to query user audits
 */
public class UserAuditQuery {

  /**
   * query by audit type category
   */
  private List<String> auditTypeCategoryList;
  
  /**
   * field value must match all audit types
   */
  private Map<String, Object> auditFieldValue;
  
  /**
   * query by audit type action
   */
  private Set<AuditType> auditTypeActionList;
  
  /**
   * audit type string, and value pairs
   */
  private Map<String, String> auditTypeFieldValue;
  
  /**
   * query options
   */
  private QueryOptions queryOptions = new QueryOptions().paging(10, 1, true).sortDesc(AuditEntry.FIELD_LAST_UPDATED_DB);

  /**
   * query for records on this date
   */
  private Date onDate = null;
  
  /**
   * query for records after this date
   */
  private Date fromDate = null;

  /**
   * query for records before this date
   */
  private Date toDate = null;

  /**
   * query for records of this logged in member
   */
  private Member loggedInMember;
  
  /**
   * query for records of this act as member
   */
  private Member actAsMember;
  
  /**
   * 
   * @param loggedInMember
   * @return this for chaining
   */
  public UserAuditQuery loggedInMember(Member loggedInMember) {
    this.loggedInMember = loggedInMember;
    return this;
  }
  
  /**
   * 
   * @param actAsInMember
   * @return this for chaining
   */
  public UserAuditQuery actAsMember(Member actAsMember) {
    this.actAsMember = actAsMember;
    return this;
  }

  /**
   * extra criteria
   */
  private Criterion extraCriterion;
  
  
  
  /**
   * extra criteria
   * @param extraCriterion1
   * @return this for chaining
   */
  public UserAuditQuery setExtraCriterion(Criterion extraCriterion1) {
    this.extraCriterion = extraCriterion1;
    return this;
  }

  /**
   * query for records after this date
   * @param fromDate1
   * @return this for chaining
   */
  public UserAuditQuery setFromDate(Date fromDate1) {
    this.fromDate = fromDate1;
    return this;
  }

  /**
   * query for records before this date
   * @param toDate1
   * @return this for chaining
   */
  public UserAuditQuery setToDate(Date toDate1) {
    this.toDate = toDate1;
    return this;
  }

  /**
   * query for records on this date
   * @param onDate
   * @return this for chaining
   */
  public UserAuditQuery setOnDate(Date onDate) {
    this.onDate = onDate;
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
  public UserAuditQuery setQueryOptions(QueryOptions queryOptions) {
    this.queryOptions = queryOptions;
    return this;
  }

  /**
   * 
   * @return the results
   */
  public List<AuditEntry> execute() {
    
    List<Criterion> criterionList = new ArrayList<Criterion>();
    
    if (this.extraCriterion != null) {
      criterionList.add(this.extraCriterion);
    }
    Date theOnDate = this.onDate;
    Date theFromDate = this.fromDate;
    Date theToDate = this.toDate;
    
    //if dates are equal, then its just "on"
    if (theOnDate == null && theFromDate != null && theToDate != null && GrouperUtil.equals(theFromDate, theToDate)) {
      theOnDate = theFromDate;
      theFromDate = null;
      theToDate = null;
    }
    
    if (theFromDate != null) {
      criterionList.add(Restrictions.ge(AuditEntry.FIELD_LAST_UPDATED_DB, theFromDate.getTime()));
    }
    if (theToDate != null) {
      criterionList.add(Restrictions.le(AuditEntry.FIELD_LAST_UPDATED_DB, theToDate.getTime()));
    }
    
    if (theOnDate != null) {
      //get beginning of the date
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(theOnDate);
      calendar.clear(Calendar.HOUR_OF_DAY);
      calendar.clear(Calendar.MINUTE);
      calendar.clear(Calendar.SECOND);
      calendar.clear(Calendar.MILLISECOND);
      long from = calendar.getTimeInMillis();
      calendar.add(Calendar.DAY_OF_YEAR, 1);
      long to = calendar.getTimeInMillis();
      criterionList.add(Restrictions.ge(AuditEntry.FIELD_LAST_UPDATED_DB, from));
      criterionList.add(Restrictions.le(AuditEntry.FIELD_LAST_UPDATED_DB, to));
    }
    
    Criterion loggedInCriterion = null;
    Criterion actAsCriterion = null;
    
    
    if (this.loggedInMember != null) {
    	loggedInCriterion=Restrictions.eq(AuditEntry.FIELD_LOGGED_IN_MEMBER_ID, 
    	          this.loggedInMember.getUuid());
    }
    
    if (this.actAsMember != null) {
    	actAsCriterion=Restrictions.eq(AuditEntry.FIELD_ACT_AS_MEMBER_ID, 
    	          this.actAsMember.getUuid());
    }
    
    if(loggedInCriterion != null && actAsCriterion !=null) {
    	criterionList.add(Restrictions.or(loggedInCriterion, actAsCriterion));
    }else if(loggedInCriterion != null) {
    	criterionList.add(loggedInCriterion);
    }else if(actAsCriterion != null) {
    	criterionList.add(actAsCriterion);
    }
    
    //add categories to actions
    for (String auditTypeCategory : GrouperUtil.nonNull(this.auditTypeCategoryList)) {
      Collection<AuditType> auditTypes = AuditTypeFinder.findByCategory(auditTypeCategory);
      for (AuditType auditType : GrouperUtil.nonNull(auditTypes)) {
        this.addAuditTypeAction(auditType.getAuditCategory(), auditType.getActionName());
      }
    }

    if (GrouperUtil.length(this.auditTypeActionList) > 0) {
      Set<String> auditTypeIds = new LinkedHashSet<String>();
      for (AuditType auditType : this.auditTypeActionList) {
        auditTypeIds.add(auditType.getId());
      }
      criterionList.add(Restrictions.in(AuditEntry.FIELD_AUDIT_TYPE_ID, auditTypeIds));
    }
    
    if (this.auditFieldValue != null) {
      
      for (String fieldName : this.auditFieldValue.keySet()) {
        Object value = this.auditFieldValue.get(fieldName);

        //find the field name for this fieldName in all audit types
        Criterion criterion = AuditFieldType.criterion(fieldName, value);
        if (criterion == null) {
          throw new RuntimeException("Cant find audit type for '" + fieldName + "'");
        }
        
        criterionList.add(criterion);
        
      }
    }
    
    Criterion allCriteria = HibUtils.listCrit(criterionList);
    
    List<AuditEntry> results = HibernateSession.byCriteriaStatic()
      .options(this.queryOptions).list(AuditEntry.class, allCriteria);
    return results;
  }
  
  /**
   * return one string report (e.g. for gsh)
   * @return the report
   */
  public String executeReport(){
    return this.executeReport(false);
  }
  
  /**
   * return one string report (e.g. for gsh)
   * @return the report
   */
  public String executeReportExtended(){
    return this.executeReport(true);
  }
  
  /**
   * return one string report (e.g. for gsh)
   * @param extended if should only print small report or large
   * @return the report
   */
  private String executeReport(boolean extended){
    
    List<AuditEntry> results = this.execute();

    StringBuilder report = new StringBuilder();
    
    QueryPaging queryPaging = this.queryOptions == null ? null : this.queryOptions.getQueryPaging();
    QuerySort querySort = this.queryOptions == null ? null : this.queryOptions.getQuerySort();

    report.append("Results ");
    if (queryPaging != null) {
      report.append(queryPaging.getPageStartIndex())
        .append(" - ").append(queryPaging.getPageEndIndex()).append(" of ")
        .append(queryPaging.getTotalRecordCount());
      
    } else {
      if (GrouperUtil.length(results) > 0) {
        report.append("1");
      } else {
        report.append("0");
      }
      report.append(" - ").append(GrouperUtil.length(results));
    }

    if (querySort != null) {
      report.append("    ordered by: ").append(querySort.sortString(false));
    }
    report.append("\n");

    
    if (GrouperUtil.length(results) == 0 && (this.queryOptions == null || this.queryOptions.isRetrieveResults())) {
      return "No results found.";
    }
    
    for (AuditEntry auditEntry : results) {
      String auditEntryString = auditEntry.toStringReport(extended);
      
      report.append(auditEntryString);
      
      if (report.charAt(report.length()-1) != '\n') {
        report.append("\n");
      }
      
    }
    
    return report.toString();
  }

  /**
   * get the field name based on field name and audit types
   * @param fieldName
   * @return the field name
   */
  String translateFieldName(String fieldName) {
    String translatedFieldName = null;
    if (this.auditTypeActionList != null) {
      for (AuditType auditType : this.auditTypeActionList ) {
        String field = auditType.retrieveAuditEntryFieldForLabel(fieldName);
        if (translatedFieldName != null) {
          if (!StringUtils.equals(translatedFieldName, field)) {
            throw new RuntimeException("Ambiguous field: " + fieldName 
                + ", could be " + translatedFieldName + ", or " + field);
          }
        }
        translatedFieldName = field;
      }
      if (translatedFieldName == null) {
        throw new RuntimeException("Cant find field: " + fieldName);
      }
      return translatedFieldName;
    }
    
    //if there are no actions, just get all actions where 
    //TODO, do this later
    throw new RuntimeException("Not implemented querying by field name without action or category: " + fieldName);
  }
  
  
  /**
   * query by audit type category
   * @param auditTypeCategoryList
   * @return this for chaining
   */
  public UserAuditQuery setAuditTypeCategoryList(List<String> auditTypeCategoryList) {
    this.auditTypeCategoryList = auditTypeCategoryList;
    return this;
  }

  /**
   * query by audit type action
   * @param auditTypeActionList
   * @return this for chaining
   */
  public UserAuditQuery setAuditTypeActionList(List<AuditTypeIdentifier> auditTypeActionList) {
    if (this.auditTypeActionList == null) {
      this.auditTypeActionList = new LinkedHashSet<AuditType>();
    }
    this.auditTypeActionList.clear();
    for (AuditTypeIdentifier auditTypeIdentifier : GrouperUtil.nonNull(auditTypeActionList)) {
      this.addAuditTypeAction(auditTypeIdentifier.getAuditCategory(), auditTypeIdentifier.getActionName());
    }
    return this;
  }

  /**
   * query by audit type category, add a criteria to list
   * @param auditTypeCategory
   * @return this for chaining
   */
  public UserAuditQuery addAuditTypeCategory(String auditTypeCategory) {
    if (this.auditTypeCategoryList == null) {
      this.auditTypeCategoryList = new ArrayList<String>();
    }
    this.auditTypeCategoryList.add(auditTypeCategory);
    return this;
  }

  /**
   * query by audit type action, add a criteria to list
   * @param auditTypeCategory 
   * @param auditTypeAction
   * @return this for chaining
   */
  public UserAuditQuery addAuditTypeAction(String auditTypeCategory, String auditTypeAction) {
    if (this.auditTypeActionList == null) {
      this.auditTypeActionList = new LinkedHashSet<AuditType>();
    }
    AuditType auditType = AuditTypeFinder.find(auditTypeCategory, auditTypeAction, false);
    this.auditTypeActionList.add(auditType);
    return this;
  }

  /**
   * query by audit type action, add a criteria to list
   * @param auditTypeField 
   * @param auditTypeValue
   * @return this for chaining
   */
  public UserAuditQuery addAuditTypeFieldValue(String auditTypeField, Object auditTypeValue) {
    if (this.auditFieldValue == null) {
      this.auditFieldValue = new LinkedHashMap<String, Object>();
    }
    this.auditFieldValue.put(auditTypeField, auditTypeValue);
    return this;
  }
}
