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
 * $Id: GrouperReport.java,v 1.7 2009-06-09 22:55:40 shilen Exp $
 */
package edu.internet2.middleware.grouper.misc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.usdu.USDU;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperReport {
  
  /** whether to find unresolvable subjects */
  private boolean findUnresolvables = false;
  
  /** whether to find bad memberships */
  private boolean findBadMemberships = false;

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperReport.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(report(true, true));
  }
  
  /**
   * Whether or not to find unresolvable subjects as part of the report.  Defaults to false.
   * @param findUnresolvables
   * @return GrouperReport
   */
  public GrouperReport findUnresolvables(boolean findUnresolvables) {
    this.findUnresolvables = findUnresolvables;
    return this;
  }
  
  /**
   * Whether or not to find bad memberships as part of the report.  Defaults to false.
   * @param findBadMemberships
   * @return GrouperReport
   */
  public GrouperReport findBadMemberships(boolean findBadMemberships) {
    this.findBadMemberships = findBadMemberships;
    return this;
  }

  /**
   * format with commas
   * @param theLong
   * @return the string
   */
  public static String formatCommas(Long theLong) {
    if (theLong == null) {
      return "";
    }
    DecimalFormat myFormatter = new DecimalFormat("###,###");
    String output = myFormatter.format(theLong);
    return output;
  }

  /**
   * @param findUnresolvables 
   * @param findBadMemberships 
   * @return the report
   * @throws GrouperReportException
   */
  public static String report(boolean findUnresolvables, boolean findBadMemberships) {
    return new GrouperReport().findBadMemberships(findBadMemberships)
      .findUnresolvables(findUnresolvables).runReport();
  }
  
  /**
   * @return the report
   * @throws GrouperReportException
   */
  public String runReport() {

    GrouperStartup.startup();

    GrouperSession grouperSession = null;
    
    StringBuilder result = new StringBuilder();
    
    try {
      grouperSession = GrouperSession.startRootSession();
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_YEAR, -1);
      Date yesterday = calendar.getTime(); 
  
      result.append("Grouper daily report\n\n");
      result.append("----------------\n");
      result.append("OVERALL:\n");
      result.append("----------------\n");
      
      result.append("environment:           ").append(GrouperConfig.retrieveConfig().propertyValueString("grouper.env.name")).append("\n");
      
      Long membershipCountTotal = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from MembershipEntry").uniqueResult(Long.class);
      result.append("memberships:           ").append(formatCommas(membershipCountTotal)).append("\n");
  
      Long groupCountTotal = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from Group").uniqueResult(Long.class);
      result.append("groups:                ").append(formatCommas(groupCountTotal)).append("\n");
  
      Long memberCountTotal = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from Member").uniqueResult(Long.class);
      result.append("members:               ").append(formatCommas(memberCountTotal)).append("\n");
      
      Long folderCountTotal = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from Stem").uniqueResult(Long.class);
      result.append("folders:               ").append(formatCommas(folderCountTotal)).append("\n");
    
      String unresolvableResults = "Not configured to compute this today";
      Set<Member> usduMembers = new HashSet<Member>();
      if (findUnresolvables) {
        usduMembers = GrouperUtil.nonNull(USDU.getUnresolvableMembers(grouperSession, null));
        unresolvableResults = formatCommas(Long.valueOf(usduMembers.size()));
      }
      result.append("unresolvable subjects: ").append(unresolvableResults).append("\n");
      
      
      String badMembershipResults = "Not configured to compute this today";
      String badMembershipGshScript = null;
      String badMembershipOutput = null;
      int badMembershipCount = 0;
      if (findBadMemberships) {
        ByteArrayOutputStream  baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        FindBadMemberships.clearResults();
        FindBadMemberships.checkAll(printStream);
        badMembershipOutput = baos.toString();
        badMembershipGshScript = FindBadMemberships.gshScript == null ? "" : FindBadMemberships.gshScript.toString();
        if (!StringUtils.isBlank(badMembershipGshScript)) {
          int theCount = StringUtils.countMatches(badMembershipGshScript, "\n");
          theCount = theCount == 0 ? 1 : theCount;
          badMembershipCount += theCount;
        }
        if (badMembershipCount == 0) {
          badMembershipResults = "0";
        } else {
          badMembershipResults = badMembershipCount + " lines in report below";
        }
      }
      result.append("bad memberships:       ").append(badMembershipResults).append("\n");

      
      result.append("\n----------------\n");
      result.append("WITHIN LAST DAY:\n");
      result.append("----------------\n");
      Long membershipNewCountDay = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from MembershipEntry where createTimeLong > :createTime or groupSetCreateTimeLong > :createTime")
        .setLong("createTime", yesterday.getTime()).uniqueResult(Long.class);
      result.append("new memberships:       ").append(formatCommas(membershipNewCountDay)).append("\n");
    
      Long groupNewCountDay = HibernateSession.byHqlStatic().createQuery(
          "select count(*) from Group where createTimeLong > :createTime")
        .setLong("createTime", yesterday.getTime()).uniqueResult(Long.class);
      result.append("new groups:            ").append(formatCommas(groupNewCountDay)).append("\n");
  
      Long groupUpdatedCountDay = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from Group where createTimeLong > :createTime and modifyTimeLong < :createTime2")
        .setLong("createTime", yesterday.getTime())
        .setLong("createTime2", yesterday.getTime()).uniqueResult(Long.class);
      result.append("updated groups:        ").append(formatCommas(groupUpdatedCountDay)).append("\n");
  
      Long stemNewCountDay = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from Stem where createTimeLong > :createTime")
        .setLong("createTime", yesterday.getTime()).uniqueResult(Long.class);
      result.append("new folders:           ").append(formatCommas(stemNewCountDay)).append("\n");

      result.append("\n----------------\n");
      result.append("LOADER SUMMARY WITHIN LAST DAY\n");
      result.append("----------------\n");
      
      Long loaderLogCount = HibernateSession.byHqlStatic().createQuery(
          "select count(*) from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated")
          .setTimestamp("lastUpdated", yesterday).uniqueResult(Long.class);
      result.append("jobs:                  ").append(formatCommas(loaderLogCount)).append("\n");
      
      long loaderErrorCount = -1;
      for (GrouperLoaderStatus grouperLoaderStatus : GrouperLoaderStatus.values()) {
        if (GrouperLoaderStatus.SUCCESS.equals(grouperLoaderStatus)) {
          continue;
        }
        Long loaderCount = HibernateSession.byHqlStatic().createQuery(
            "select count(*) from Hib3GrouperLoaderLog where status = '" + grouperLoaderStatus.name() + "' and lastUpdated > :lastUpdated")
            .setTimestamp("lastUpdated", yesterday).uniqueResult(Long.class);

        if (grouperLoaderStatus == GrouperLoaderStatus.ERROR) {
          loaderErrorCount = loaderCount;
        }
        
        if (grouperLoaderStatus != GrouperLoaderStatus.SUCCESS 
            && (grouperLoaderStatus == GrouperLoaderStatus.ERROR || loaderCount > 0)) {
          String label = grouperLoaderStatus.getFriendlyString();
          result.append(label).append(":").append(StringUtils.repeat(" ", 22-label.length()))
            .append(formatCommas(loaderCount)).append("\n");
        }
      }
      Long loaderUsduCount = HibernateSession.byHqlStatic().createQuery(
        "select sum(unresolvableSubjectCount) from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated" +
        " and jobType like 'SQL%' and parentJobId is null")
        .setTimestamp("lastUpdated", yesterday).uniqueResult(Long.class);
      result.append("unresolvable subjects: ").append(formatCommas(loaderUsduCount)).append("\n");
  
      Long loaderInsertCount = HibernateSession.byHqlStatic().createQuery(
          "select sum(insertCount) from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated" +
          " and jobType like 'SQL%' and parentJobId is null")
        .setTimestamp("lastUpdated", yesterday).uniqueResult(Long.class);
      result.append("inserts:               ").append(formatCommas(loaderInsertCount)).append("\n");
  
      Long loaderUpdateCount = HibernateSession.byHqlStatic().createQuery(
          "select sum(updateCount) from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated" +
          " and jobType like 'SQL%' and parentJobId is null")
        .setTimestamp("lastUpdated", yesterday).uniqueResult(Long.class);
      result.append("updates:               ").append(formatCommas(loaderUpdateCount)).append("\n");
  
      Long loaderDeleteCount = HibernateSession.byHqlStatic().createQuery(
          "select sum (deleteCount) from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated" +
          " and jobType like 'SQL%' and parentJobId is null")
        .setTimestamp("lastUpdated", yesterday).uniqueResult(Long.class);
      result.append("deletes:               ").append(formatCommas(loaderDeleteCount)).append("\n");
  
      Long loaderTotalCount = HibernateSession.byHqlStatic().createQuery(
          "select sum (totalCount) from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated" +
          " and jobType like 'SQL%' and parentJobId is null")
        .setTimestamp("lastUpdated", yesterday).uniqueResult(Long.class);
      result.append("total loader mships:   ").append(formatCommas(loaderTotalCount)).append("\n");
  
      Long processingSum = GrouperUtil.defaultIfNull(HibernateSession.byHqlStatic().createQuery(
          "select sum(millis) from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated and parentJobId is null")
          .setTimestamp("lastUpdated", yesterday).uniqueResult(Long.class), new Long(0));
      result.append("processing time:       ").append(GrouperUtil.convertMillisToFriendlyString(processingSum)).append("\n");

      if (loaderErrorCount > 0) {
        List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byHqlStatic().createQuery(
          "from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated and status != 'SUCCESS'")
            .setTimestamp("lastUpdated", yesterday).list(Hib3GrouperLoaderLog.class);
        result.append("\n----------------\n");
        result.append("LOADER JOBS WITH NON-SUCCESS\n");
        result.append("----------------\n");
        for (Hib3GrouperLoaderLog loaderLog : loaderLogs) {
          result.append("\njob:               ").append(loaderLog.getJobName())
            .append("\n");
          result.append("status:            ").append(loaderLog.getStatus()).append(", started: ")
            .append(loaderLog.getStartedTime()).append(" (")
            .append(GrouperUtil.convertMillisToFriendlyString(loaderLog.getMillis())).append(")\n");
          result.append("ins/upd/del/tot:   ").append(loaderLog.getInsertCount()).append("/")
            .append(loaderLog.getUpdateCount()).append("/").append(loaderLog.getDeleteCount()).append("/")
            .append(loaderLog.getTotalCount()).append("\n");
          if (loaderLog.getUnresolvableSubjectCount() > 0) {
            result.append("unresolv subjects: ").append(loaderLog.getUnresolvableSubjectCount()).append("\n");
          }
          result.append("error:             ").append(loaderLog.getJobDescription()).append("\n");
        }
      }

      if (loaderLogCount > 0) {
        List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byHqlStatic().createQuery(
          "from Hib3GrouperLoaderLog where lastUpdated > :lastUpdated " +
          " and status != 'ERROR' and jobName != 'CHANGE_LOG_changeLogTempToChangeLog'")
            .setTimestamp("lastUpdated", yesterday).options(new QueryOptions().paging(50, 1, false))
            .list(Hib3GrouperLoaderLog.class);
        if (loaderLogs.size() > 0) {
          result.append("\n----------------\n");
          result.append("LOADER JOBS SUCCESS (max 50 of them)\n");
          result.append("----------------\n");
          for (Hib3GrouperLoaderLog loaderLog : loaderLogs) {
            result.append("\njob:               ").append(loaderLog.getJobName())
              .append("\n");
            result.append("status:            ").append(loaderLog.getStatus()).append(", started: ")
              .append(loaderLog.getStartedTime()).append(" (")
              .append(GrouperUtil.convertMillisToFriendlyString(loaderLog.getMillis())).append(")\n");
            result.append("ins/upd/del/tot:   ").append(loaderLog.getInsertCount()).append("/")
              .append(loaderLog.getUpdateCount()).append("/").append(loaderLog.getDeleteCount())
              .append("/").append(loaderLog.getTotalCount()).append("\n");
            if (loaderLog.getUnresolvableSubjectCount() > 0) {
              result.append("unresolv subjects: ").append(loaderLog.getUnresolvableSubjectCount()).append("\n");
            }
          }
        }
      }

      
      if (usduMembers.size() > 0) {
        int usduToPrint = Math.min(50, usduMembers.size());
        result.append("\n----------------\n");
        result.append("UNRESOLVABLE SUBJECTS " + usduToPrint + " of " + usduMembers.size() + "\n");
        result.append("----------------\n");
        Iterator<Member> iterator = usduMembers.iterator();
        int count = 1;
        while (count <= usduToPrint && iterator.hasNext()) {
          Member member = iterator.next();
          result.append(member.getSubjectSourceId() + ": " + member.getSubjectId()).append("\n");
          if (++count > usduToPrint) {
            break;
          }
        }
      }
      
      if (badMembershipCount > 0) {
        result.append("\n----------------\n");
        result.append("BAD MEMBERSHIPS OUTPUT\n");
        result.append("----------------\n");
        result.append(badMembershipOutput);

        result.append("\n----------------\n");
        result.append("BAD MEMBERSHIPS GSH\n");
        result.append("----------------\n");
        result.append(badMembershipGshScript);
        
      }
      
      result.append("\n----------------\n");
      result.append("GROUPER INFO\n");
      result.append("----------------\n");
      ByteArrayOutputStream  baos = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(baos);
      GrouperInfo.grouperInfo(printStream, false);
      result.append(new String(baos.toByteArray()));
      
    } catch (Exception e) {
      GrouperReportException gre = new GrouperReportException("Problem generating daily report", e);
      gre.setResult(result.toString());
      throw gre;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
      
    return result.toString();
  }

}
