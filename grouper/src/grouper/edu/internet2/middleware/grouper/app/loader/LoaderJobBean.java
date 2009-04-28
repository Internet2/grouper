/*
 * @author mchyzer
 * $Id: LoaderJobBean.java,v 1.1.2.1 2009-04-28 19:37:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for group low level hooks
 */
@GrouperIgnoreDbVersion
public class LoaderJobBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: andGroups */
  public static final String FIELD_AND_GROUPS = "andGroups";

  /** constant for field name for: groupLikeString */
  public static final String FIELD_GROUP_LIKE_STRING = "groupLikeString";

  /** constant for field name for: groupNameOverall */
  public static final String FIELD_GROUP_NAME_OVERALL = "groupNameOverall";

  /** constant for field name for: groupQuery */
  public static final String FIELD_GROUP_QUERY = "groupQuery";

  /** constant for field name for: groupTypes */
  public static final String FIELD_GROUP_TYPES = "groupTypes";

  /** constant for field name for: grouperLoaderDb */
  public static final String FIELD_GROUPER_LOADER_DB = "grouperLoaderDb";

  /** constant for field name for: grouperLoaderType */
  public static final String FIELD_GROUPER_LOADER_TYPE = "grouperLoaderType";

  /** constant for field name for: grouperSession */
  public static final String FIELD_GROUPER_SESSION = "grouperSession";

  /** constant for field name for: hib3GrouploaderLogOverall */
  public static final String FIELD_HIB3_GROUPLOADER_LOG_OVERALL = "hib3GrouploaderLogOverall";

  /** constant for field name for: query */
  public static final String FIELD_QUERY = "query";

  /** constant for field name for: startTime */
  public static final String FIELD_START_TIME = "startTime";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_AND_GROUPS, FIELD_GROUP_LIKE_STRING, FIELD_GROUP_NAME_OVERALL, FIELD_GROUP_QUERY, 
      FIELD_GROUP_TYPES, FIELD_GROUPER_LOADER_DB, FIELD_GROUPER_LOADER_TYPE, FIELD_GROUPER_SESSION, 
      FIELD_HIB3_GROUPLOADER_LOG_OVERALL, FIELD_QUERY, FIELD_START_TIME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * start time of job
   */
  private long startTime;
  
  /**
   * type of loader
   */
  private GrouperLoaderType grouperLoaderType;

  /**
   * group name for the job.  If this is a group list, then this is the overall group name
   */
  private String groupNameOverall;
  
  /**
   * database this job runs against
   */
  private GrouperLoaderDb grouperLoaderDb;
  
  /**
   * quert for the job
   */
  private String query;
  
  /**
   * log
   */
  private Hib3GrouperLoaderLog hib3GrouploaderLogOverall;
  
  /**
   * grouper session for the job, probably a root session
   */
  private GrouperSession grouperSession;
  
  /**
   * members must be in these groups to be in the overall group
   */
  private List<Group> andGroups;
  
  /**
   * group types to add to loader managed group
   */
  private List<GroupType> groupTypes;
  
  /**
   * groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   */
  private String groupLikeString; 
  
  /**
   * query for the job
   */
  private String groupQuery;
  
  /**
   * 
   */
  public LoaderJobBean() {
    super();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public LoaderJobBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * type of job, e.g. group list, or sql simple
   * @return type
   */
  public GrouperLoaderType getGrouperLoaderType() {
    return this.grouperLoaderType;
  }

  /**
   * overall group name (if a group list job, then overall, if sql simple, then the group)
   * @return group name overall
   */
  public String getGroupNameOverall() {
    return this.groupNameOverall;
  }

  /**
   * database this runs against
   * @return loader db
   */
  public GrouperLoaderDb getGrouperLoaderDb() {
    return this.grouperLoaderDb;
  }

  /**
   * query for the job
   * @return query
   */
  public String getQuery() {
    return this.query;
  }

  /**
   * log entry for the job
   * @return log
   */
  public Hib3GrouperLoaderLog getHib3GrouploaderLogOverall() {
    return this.hib3GrouploaderLogOverall;
  }

  /**
   * grouper session (probably a root session)
   * @return session
   */
  public GrouperSession getGrouperSession() {
    return this.grouperSession;
  }

  /**
   * members must be in these groups also to be in the overall group
   * @return and groups
   */
  public List<Group> getAndGroups() {
    return this.andGroups;
  }

  /**
   * group types to add to loader managed group
   * @return group types
   */
  public List<GroupType> getGroupTypes() {
    return this.groupTypes;
  }

  /**
   * 
   * @return group like string
   */
  public String getGroupLikeString() {
    return this.groupLikeString;
  }

  /**
   * group query
   * @return group query
   */
  public String getGroupQuery() {
    return this.groupQuery;
  }

  /**
   * @param grouperLoaderType1
   * @param groupNameOverall1
   * @param grouperLoaderDb1
   * @param query1
   * @param hib3GrouploaderLogOverall1
   * @param grouperSession1
   * @param andGroups1
   * @param groupTypes1
   * @param groupLikeString1 groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   * @param groupQuery1
   * @param startTime1 
   */
  public LoaderJobBean(GrouperLoaderType grouperLoaderType1,
      String groupNameOverall1, GrouperLoaderDb grouperLoaderDb1, String query1,
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1,
      GrouperSession grouperSession1, List<Group> andGroups1,
      List<GroupType> groupTypes1, String groupLikeString1, String groupQuery1, long startTime1) {
    this.grouperLoaderType = grouperLoaderType1;
    this.groupNameOverall = groupNameOverall1;
    this.grouperLoaderDb = grouperLoaderDb1;
    this.query = query1;
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
    this.grouperSession = grouperSession1;
    this.andGroups = andGroups1;
    this.groupTypes = groupTypes1;
    this.groupLikeString = groupLikeString1;
    this.groupQuery = groupQuery1;
    this.startTime = startTime1;
  }

  /**
   * type of job, e.g. sql simple or group list
   * @param grouperLoaderType
   */
  public void setGrouperLoaderType(GrouperLoaderType grouperLoaderType) {
    this.grouperLoaderType = grouperLoaderType;
  }

  /**
   * group name for job (if group list, this is the overall name)
   * @param groupNameOverall
   */
  public void setGroupNameOverall(String groupNameOverall) {
    this.groupNameOverall = groupNameOverall;
  }

  /**
   * db this job runs against
   * @param grouperLoaderDb
   */
  public void setGrouperLoaderDb(GrouperLoaderDb grouperLoaderDb) {
    this.grouperLoaderDb = grouperLoaderDb;
  }

  /**
   * query for this job (if runs against query)
   * @param query1
   */
  public void setQuery(String query1) {
    this.query = query1;
  }

  /**
   * 
   * @param hib3GrouploaderLogOverall1
   */
  public void setHib3GrouploaderLogOverall(
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1) {
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
  }

  /**
   * grouper session, probably a root session
   * @param grouperSession1
   */
  public void setGrouperSession(GrouperSession grouperSession1) {
    this.grouperSession = grouperSession1;
  }

  /**
   * members must be in these groups also to be in the overall group
   * @param andGroups1
   */
  public void setAndGroups(List<Group> andGroups1) {
    this.andGroups = andGroups1;
  }

  /**
   * group types to add to loader managed group
   * @param groupTypes
   */
  public void setGroupTypes(List<GroupType> groupTypes) {
    this.groupTypes = groupTypes;
  }

  /**
   * groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   * @param groupLikeString
   */
  public void setGroupLikeString(String groupLikeString) {
    this.groupLikeString = groupLikeString;
  }

  /**
   * 
   * @param groupQuery1
   */
  public void setGroupQuery(String groupQuery1) {
    this.groupQuery = groupQuery1;
  }

  /**
   * start time of job
   * @return start time
   */
  public long getStartTime() {
    return this.startTime;
  }

  /**
   * start time of job
   * @param startTime1
   */
  public void setStartTime(long startTime1) {
    this.startTime = startTime1;
  }

}
