/*
 * @author mchyzer
 * $Id: UserAuditQuery.java,v 1.1 2009-04-15 15:56:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
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
   * 
   * @param loggedInMember
   * @return this for chaining
   */
  public UserAuditQuery loggedInMember(Member loggedInMember) {
    this.loggedInMember = loggedInMember;
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
   */
  public void setQueryOptions(QueryOptions queryOptions) {
    this.queryOptions = queryOptions;
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
    
    if (this.fromDate != null) {
      criterionList.add(Expression.ge(AuditEntry.FIELD_LAST_UPDATED_DB, this.fromDate.getTime()));
    }
    if (this.toDate != null) {
      criterionList.add(Expression.le(AuditEntry.FIELD_LAST_UPDATED_DB, this.toDate.getTime()));
    }
    
    if (this.onDate != null) {
      //get beginning of the date
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(this.onDate);
      calendar.clear(Calendar.HOUR_OF_DAY);
      calendar.clear(Calendar.MINUTE);
      calendar.clear(Calendar.SECOND);
      calendar.clear(Calendar.MILLISECOND);
      long from = calendar.getTimeInMillis();
      calendar.add(Calendar.DAY_OF_YEAR, 1);
      long to = calendar.getTimeInMillis();
      criterionList.add(Expression.ge(AuditEntry.FIELD_LAST_UPDATED_DB, from));
      criterionList.add(Expression.le(AuditEntry.FIELD_LAST_UPDATED_DB, to));
    }

    if (this.loggedInMember != null) {
      criterionList.add(Restrictions.eq(AuditEntry.FIELD_LOGGED_IN_MEMBER_ID, 
          this.loggedInMember.getUuid()));
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
}
