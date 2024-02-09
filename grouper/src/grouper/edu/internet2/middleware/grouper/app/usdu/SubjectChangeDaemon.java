package edu.internet2.middleware.grouper.app.usdu;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcCaseIgnoreHashMap;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 */
@DisallowConcurrentExecution
public class SubjectChangeDaemon extends OtherJobBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SubjectChangeDaemon.class);
  
  /** batch size */
  private static final int BATCH_SIZE = 200;

  /**
   * 
   */
  public SubjectChangeDaemon() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    
  }

  /**
   * 
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    String jobName = otherJobInput.getJobName();
    
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    if (hib3GrouperLoaderLog == null) {
      hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    }
        
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    long now = System.nanoTime();
    
    try {
      debugMap.put("job", "subjectChangeDaemon");

      String subjectSourceId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".subjectChangeDaemon.subjectSourceId");
      String database = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".subjectChangeDaemon.database", "grouper");
      String table = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".subjectChangeDaemon.table");
      String useSubjectIdOrIdentifier = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".subjectChangeDaemon.useSubjectIdOrIdentifier");
      String columnSubjectId = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".subjectChangeDaemon.columnSubjectId");
      String columnSubjectIdentifier = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".subjectChangeDaemon.columnSubjectIdentifier");
      String columnPrimaryKey = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".subjectChangeDaemon.columnPrimaryKey");
      boolean deleteProcessedRows = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".subjectChangeDaemon.deleteProcessedRows", false);
      String columnCreateTimestamp = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".subjectChangeDaemon.columnCreateTimestamp");
      String columnProcessedTimestamp = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".subjectChangeDaemon.columnProcessedTimestamp");

      debugMap.put("subjectSourceId", subjectSourceId);
      debugMap.put("database", database);
      debugMap.put("table", table);
      debugMap.put("useSubjectIdOrIdentifier", useSubjectIdOrIdentifier);
      debugMap.put("columnSubjectId", columnSubjectId);
      debugMap.put("columnSubjectIdentifier", columnSubjectIdentifier);
      debugMap.put("columnPrimaryKey", columnPrimaryKey);
      debugMap.put("deleteProcessedRows", deleteProcessedRows);
      debugMap.put("columnCreateTimestamp", columnCreateTimestamp);
      debugMap.put("columnProcessedTimestamp", columnProcessedTimestamp);
      
      String columnSubjectValue = null;
      if (useSubjectIdOrIdentifier.equals("subjectId")) {
        columnSubjectValue = columnSubjectId;
      } else if (useSubjectIdOrIdentifier.equals("subjectIdentifier")) {
        columnSubjectValue = columnSubjectIdentifier;
      } else {
        throw new RuntimeException("Unexpected useSubjectIdOrIdentifier:" + useSubjectIdOrIdentifier);
      }
      
      if (GrouperUtil.isBlank(columnSubjectValue)) {
        throw new RuntimeException("No subject column specified");
      }
      
      if (!deleteProcessedRows && GrouperUtil.isBlank(columnProcessedTimestamp)) {
        throw new RuntimeException("columnProcessedTimestamp is required if deleteProcessedRows is false");
      }
      
      String sql;
      
      if (deleteProcessedRows) {
        sql = "select * from " + table;
      } else {
        sql = "select * from " + table + " where " + columnProcessedTimestamp + " is null";
      }
      
      List<GcCaseIgnoreHashMap> sqlResults = new GcDbAccess().connectionName(database).sql(sql).selectListMap();
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      long millisGetData = (System.nanoTime() - now) / 1000000;
      hib3GrouperLoaderLog.setMillisGetData(GrouperUtil.intObjectValue(millisGetData, false));

      if (sqlResults.size() > 0) {
        String updateSql;
        if (deleteProcessedRows) {
          updateSql = "delete from " + table + " where " + columnPrimaryKey + " = ?";
        } else {
          updateSql = "update " + table + " set " + columnProcessedTimestamp + " = ? where " + columnPrimaryKey + " = ?";
        }
        
        Timestamp lastUSDUSuccessStartTimestamp = HibernateSession.byHqlStatic().createQuery("select max(theLoaderLog.startedTime) from Hib3GrouperLoaderLog theLoaderLog " +
            "where theLoaderLog.jobName = 'OTHER_JOB_usduDaemon' and theLoaderLog.status = 'SUCCESS'").uniqueResult(Timestamp.class);
        
        int numberOfBatches = GrouperUtil.batchNumberOfBatches(sqlResults, BATCH_SIZE, true);
        for (int i = 0; i < numberOfBatches; i++) {
          GrouperDaemonUtils.stopProcessingIfJobPaused();

          List<GcCaseIgnoreHashMap> batchSqlResults = GrouperUtil.batchList(sqlResults, BATCH_SIZE, i);
          processBatch(hib3GrouperLoaderLog, batchSqlResults, subjectSourceId, database, useSubjectIdOrIdentifier,
              columnPrimaryKey, columnCreateTimestamp, columnSubjectValue, deleteProcessedRows, updateSql, 
              lastUSDUSuccessStartTimestamp);
        }        
      }
      
      hib3GrouperLoaderLog.setTotalCount(sqlResults.size());
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      debugMap.put("tookMillis", ((System.nanoTime()-now)/1000000));
      String debugMessage = GrouperUtil.mapToString(debugMap);
      hib3GrouperLoaderLog.appendJobMessage(debugMessage);
      if (LOG.isDebugEnabled()) {
        LOG.debug(debugMessage);
      }
    }

    return null;
  }

  private void processBatch(Hib3GrouperLoaderLog hib3GrouperLoaderLog, List<GcCaseIgnoreHashMap> sqlResults, 
      String subjectSourceId, String database, String useSubjectIdOrIdentifier, String columnPrimaryKey, 
      String columnCreateTimestamp, String columnSubjectValue, boolean deleteProcessedRows, String updateSql, 
      Timestamp lastUSDUSuccessStartTimestamp) {
    
    if (sqlResults.size() == 0) {
      return;
    }
    
    List<List<Object>> bindVars = new ArrayList<List<Object>>();
    Set<Subject> resolvedSubjects = new LinkedHashSet<Subject>();
    Set<Member> unresolvableMembers = new LinkedHashSet<Member>();
    
    for (GcCaseIgnoreHashMap sqlResult : sqlResults) {
      Object primaryKey = sqlResult.get(columnPrimaryKey);
      String subjectValue = sqlResult.getString(columnSubjectValue);
      Timestamp createTimestamp = sqlResult.getTimestamp(columnCreateTimestamp);
                
      if (lastUSDUSuccessStartTimestamp == null || lastUSDUSuccessStartTimestamp.getTime() < createTimestamp.getTime()) {
        Subject subject = null;
        if (useSubjectIdOrIdentifier.equals("subjectId")) {
          subject = SubjectFinder.findByIdAndSource(subjectValue, subjectSourceId, true, false);
        } else {
          subject = SubjectFinder.findByIdentifierAndSource(subjectValue, subjectSourceId, true, false);
        }
        
        if (subject == null) {
          if (useSubjectIdOrIdentifier.equals("subjectId")) {
            Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subjectValue, subjectSourceId, false);
            if (member != null) {
              LOG.info("Found unresolvable member with subject id=" + member.getSubjectId());
              unresolvableMembers.add(member);
            }
          } else {
            Member member = GrouperDAOFactory.getFactory().getMember().findBySubjectIdentifier(subjectValue, subjectSourceId, false);
            if (member != null) {
              Subject subject2 = SubjectFinder.findByIdAndSource(member.getSubjectId(), member.getSubjectSourceId(), true, false);
              if (subject2 == null) {
                LOG.info("Found unresolvable member with subject id=" + member.getSubjectId());
                unresolvableMembers.add(member);
              } else {
                // something isn't right.  ignoring.
                LOG.warn("Unable to resolve subject by identifier=" + subjectValue + ", found in grouper with subject id=" + member.getSubjectId() + ", and was able to resolve that.");
              }
            }
          }
        } else {
          LOG.info("Resolved subject using subject value=" + subjectValue);
          hib3GrouperLoaderLog.addUpdateCount(1);
          resolvedSubjects.add(subject);
        }
      }
      
      if (deleteProcessedRows) {
        bindVars.add(GrouperUtil.toList(primaryKey));
      } else {
        bindVars.add(GrouperUtil.toList(new Date(), primaryKey));
      }
    }
    
    if (unresolvableMembers.size() > 0) {
      Long deleted = UsduJob.deleteUnresolvableMembers(GrouperSession.staticGrouperSession(), unresolvableMembers, hib3GrouperLoaderLog);
      hib3GrouperLoaderLog.addDeleteCount(deleted.intValue());
    }
    
    if (resolvedSubjects.size() > 0) {
      sendProvisioningMessages(database, updateSql, resolvedSubjects);
    }
    
    new GcDbAccess().connectionName(database).sql(updateSql).batchBindVars(bindVars).executeBatchSql();
  }

  private void sendProvisioningMessages(String database, String updateSql, Set<Subject> subjects) {
    if (subjects.size() == 0) {
      return;
    }

    Set<Member> members = MemberFinder.findBySubjects(subjects, false);
    
    if (members.size() == 0) {
      return;
    }

    Map<String, Set<String>> provisionerNameToMemberIds = new LinkedHashMap<String, Set<String>>();

    List<String> memberIds = new ArrayList<String>();
    for (Member member : members) {
      memberIds.add(member.getId());
    }

    GcDbAccess gcDbAccess = new GcDbAccess().sql("select gs.provisioner_name, gsm.member_id from grouper_sync gs, grouper_sync_member gsm "
        + " where gs.id = gsm.grouper_sync_id and gs.sync_engine='provisioning' and gsm.member_id in ("
        + GrouperClientUtils.appendQuestions(GrouperUtil.length(memberIds)) + ")");

    for (String memberId : memberIds) {
      gcDbAccess.addBindVar(memberId);
    }

    List<Object[]> provisionerNameAndMemberIds = gcDbAccess.selectList(Object[].class);
    for (Object[] provisionerNameAndMemberId : provisionerNameAndMemberIds) {
      String provisionerName = (String)provisionerNameAndMemberId[0];
      String memberId = (String)provisionerNameAndMemberId[1];

      if (provisionerNameToMemberIds.get(provisionerName) == null) {
        provisionerNameToMemberIds.put(provisionerName, new LinkedHashSet<String>());
      }

      provisionerNameToMemberIds.get(provisionerName).add(memberId);
    }

    for (String provisionerName : provisionerNameToMemberIds.keySet()) {
      ProvisioningMessage provisioningMessage = new ProvisioningMessage();
      provisioningMessage.setMemberIdsForSync(GrouperUtil.toArray(provisionerNameToMemberIds.get(provisionerName), String.class));
      provisioningMessage.setBlocking(false);
      provisioningMessage.send(provisionerName);
    }
  }
}
