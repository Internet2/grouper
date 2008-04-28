/*
 * @author mchyzer
 * $Id: GrouperLoaderType.java,v 1.1 2008-04-28 06:40:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.loader.db.GrouperLoaderResultset;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * type of loaders (e.g. sql simple)
 */
public enum GrouperLoaderType {

  /** 
   * simple sql query where all results are all members of group.
   * must have a subject_id col, and optionally a subject_source_id col
   */
  SQL_SIMPLE {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.loader.GrouperLoaderType#attributeRequired(java.lang.String)
     */
    @Override
    public boolean attributeRequired(String attributeName) {
      return StringUtils.equals(GrouperLoader.GROUPER_LOADER_DB_NAME, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_QUERY, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, attributeName);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.loader.GrouperLoaderType#attributeOptional(java.lang.String)
     */
    @Override
    public boolean attributeOptional(String attributeName) {
      return StringUtils.equals(GrouperLoader.GROUPER_LOADER_PRIORITY, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, attributeName);
    }
    
    /**
     * sync up a group membership based on query and db
     * @param groupName
     * @param grouperLoaderDb
     * @param query
     */
    @SuppressWarnings("unchecked")
    @Override
    public void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query) {
      
      //get a resultset from the db
      final GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(grouperLoaderDb, query);
      
      //get group
      GrouperSession grouperSession = null;
      
      try {
        grouperSession = GrouperSession.start(
          SubjectFinder.findById("GrouperSystem")
        );
 
        final Group group = GroupFinder.findByName(grouperSession, groupName);
        
        final Set<Member> currentMembers = group.getImmediateMembers();
        
        //now lets remove data from each since the member is there and is supposed to be there
        Iterator<Member> iterator = currentMembers.iterator();
        
        while (iterator.hasNext()) {
          
          Member member = iterator.next();
          //see if it is in the current list
          if (grouperLoaderResultset.remove(member.getSubjectId(), member.getSubjectSourceId())) {
            //if so, then remove, no need to change
            iterator.remove();
          }
        }
        
        //lets lookup the subjects first
        final Set<Subject> subjectsToAdd = new HashSet<Subject>();
        //here are new members
        for (int i=0;i<grouperLoaderResultset.numberOfRows();i++) {
          
          String subjectId = (String)grouperLoaderResultset.getCell(i, GrouperLoaderResultset.SUBJECT_ID_COL, true);
          String subjectSourceId = (String)grouperLoaderResultset.getCell(i, GrouperLoaderResultset.SUBJECT_SOURCE_ID_COL, false);
          
          Subject subject = null;
          if (!StringUtils.isBlank(subjectSourceId)) {
            subject = SubjectFinder.getSource(subjectSourceId).getSubject(subjectId);
          } else {
            subject = SubjectFinder.findById(subjectId);
          }
          subjectsToAdd.add(subject);
        }
        
        //here are members to remove
        final Set<Subject> subjectsToRemove = new HashSet<Subject>();
        //first remove members
        for (Member member : currentMembers) {
          subjectsToRemove.add(member.getSubject());
        }
        
        //now the currentMembers is full of members to remove, and the grouperLoaderResultset is full
        //of members to add
        //start a transaction
        GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            try {
              //first remove members
              for (Subject subject : subjectsToRemove) {
                group.deleteMember(subject);
              }
              
              //then add new members
              for (Subject subject : subjectsToAdd) {
                group.addMember(subject);
              }

              grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            
            
            return null;
          }
          
        });
        
        //now get the membership from the group
      } catch (Exception e) {
        throw new RuntimeException("Problem with group: " + groupName, e);
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
      
      
    }
  };
  
  /**
   * see if an attribute if required or not
   * @param attributeName
   * @return true if required, false if not
   */
  public abstract boolean attributeRequired(String attributeName);

  /**
   * sync up a group membership based on query and db
   * @param groupName
   * @param grouperLoaderDb
   * @param query
   */
  public abstract void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query);
  
  /**
   * see if an attribute if optional or not (if not, then it is either required or forbidden)
   * @param attributeName
   * @return true if optional, false if not
   */
  public abstract boolean attributeOptional(String attributeName);
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperLoaderType.class);

  /**
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param group is the group to get the attribute from
   * @param attributeName
   * @return the attribute value
   */
  private Integer attributeValueValidateRequiredInteger(Group group, String attributeName) {
    String attributeValueString = StringUtils.trim(attributeValueValidateRequired(group, attributeName));
    return GrouperUtil.intObjectValue(attributeValueString, true);
  }

  /**
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param group is the group to get the attribute from
   * @param attributeName
   * @return the attribute value
   */
  private String attributeValueValidateRequired(Group group, String attributeName) {
    
    String attributeValue = GrouperLoaderUtils.groupGetAttribute(group, attributeName);
    
    boolean hasValue = StringUtils.isNotBlank(attributeValue);
    boolean isRequired = this.attributeRequired(attributeName);
    boolean isOptional = this.attributeOptional(attributeName);
    
    //must have value if required
    if (!hasValue && isRequired) {
      throw new RuntimeException("Attribute '" + attributeName + "' is required, but is not set for loader type: " 
          + this.name() + ", groupName: " + group.getName());
    }
    
    // must not have value if not required or optional
    if (hasValue && !isRequired && !isOptional) {
      LOG.error("Attribute '" + attributeName + "' is not required or optional, " +
      		"but is set to '" + attributeValue + "' for loader type: " 
          + this.name() + ", groupName: " + group.getName());
    }
    return attributeValue;
  }
  
  /**
   * for all jobs in this loader type, schedule them with quartz
   */
  public static void scheduleLoads() {
    
    Set<Group> groups = retrieveGroups();
    
    for (Group group : groups) {
      
      try {
        //lets get all attribute values
        String grouperLoaderType = GrouperLoaderUtils.groupGetAttribute(group, GrouperLoader.GROUPER_LOADER_TYPE);
        
        GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);

        String grouperLoaderDbName = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_DB_NAME);
        String grouperLoaderQuery = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_QUERY);
        String grouperLoaderScheduleType = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
        String grouperLoaderQuartzCron = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
        Integer grouperLoaderIntervalSeconds = grouperLoaderTypeEnum.attributeValueValidateRequiredInteger(group, GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS);
        Integer grouperLoaderPriority = grouperLoaderTypeEnum.attributeValueValidateRequiredInteger(group, GrouperLoader.GROUPER_LOADER_PRIORITY);
        
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = new JobDetail("jobForGroup_" + group.getName(), null, GrouperLoaderJob.class);

        //set data for the job to execute
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_GROUP_NAME, group.getName());
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_TYPE, grouperLoaderType);
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_DB_NAME, grouperLoaderDbName);
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_QUERY, grouperLoaderQuery);
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, 
            grouperLoaderScheduleType);
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, 
            grouperLoaderQuartzCron);
        //put as string since getting as integer will require it to be string
        if (grouperLoaderIntervalSeconds != null) {
          jobDetail.getJobDataMap().putAsString(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, 
              grouperLoaderIntervalSeconds);
        } else {
          jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, 
              grouperLoaderIntervalSeconds);
        }
        if (grouperLoaderPriority != null) {
          //put as string since getting as integer will require it to be string
          jobDetail.getJobDataMap().putAsString(GrouperLoader.GROUPER_LOADER_PRIORITY, 
              grouperLoaderPriority);
        } else {
          jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_PRIORITY, 
              grouperLoaderPriority);
          
        }
        //schedule this job based on the schedule type and params
        GrouperLoaderScheduleType grouperLoaderScheduleTypeEnum = GrouperLoaderScheduleType
          .valueOfIgnoreCase(grouperLoaderScheduleType, true);
        
        Trigger trigger = grouperLoaderScheduleTypeEnum.createTrigger(grouperLoaderQuartzCron, grouperLoaderIntervalSeconds);
        
        trigger.setName("triggerForGroup_" + group.getName());
        
        //if there is a priority, set it
        if (grouperLoaderPriority != null) {
          trigger.setPriority(grouperLoaderPriority);
        }

        scheduler.scheduleJob(jobDetail, trigger);

        
      } catch (Exception e) {
        //dont fail on all if any fail
        try {
          LOG.error("Could not schedule group: '" + group.getName() + "', '" + group.getUuid() + "'", e);
        } catch (Exception e2) {
          //dont let error message mess us up
          LOG.error("Could not schedule group.", e);
          LOG.error(e2);
        }
      }
      
      
    }
    
  }

  /**
   * retrieve all loader groups from the db
   * @return the groups (will not return null, only the empty set if none)
   */
  @SuppressWarnings("unchecked")
  private static Set<Group> retrieveGroups() {
    try {
      GrouperSession grouperSession = GrouperSession.start(
          SubjectFinder.findById("GrouperSystem")
        );
      //find all groups with the attribute with this type
//      Set<Group> groupSet = new GroupAttributeExactFilter(GrouperLoader.GROUPER_LOADER_TYPE, this.name(), 
//          StemFinder.findRootStem(grouperSession)).getResults(grouperSession);
//      return GrouperUtil.nonNull(groupSet);
      Set<Group> groupSet = GroupFinder.findAllByType(grouperSession, GroupTypeFinder.find("grouperLoader"));
      return groupSet;
    } catch (Exception e) {
      throw new RuntimeException("Problem with finding loader groups", e);
    }
    
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperLoaderType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperLoaderType.class, 
        string, exceptionOnNull);

  }

}
