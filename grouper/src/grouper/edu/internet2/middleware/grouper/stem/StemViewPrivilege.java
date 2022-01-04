package edu.internet2.middleware.grouper.stem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class StemViewPrivilege implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -8676875678328668860L;

  private String stemUuid;
  
  private String memberUuid;
  
  private String objectType;
  
  
  
  
  public String getStemUuid() {
    return stemUuid;
  }

  
  public void setStemUuid(String stemUuid) {
    this.stemUuid = stemUuid;
  }

  
  public String getMemberUuid() {
    return memberUuid;
  }

  
  public void setMemberUuid(String memberUuid) {
    this.memberUuid = memberUuid;
  }

  
  public String getObjectType() {
    return objectType;
  }

  
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  static final int BATCH_SIZE = 200;

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  /**
   * 
   * @return group name where membership means a subject can edit/add workflow
   */
  public static String stemViewAdminGroupName() {
    return GrouperConfig.retrieveConfig().propertyValueString("security.show.all.folders.if.in.group", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":sysadminStemViewers");
  }

  /**
   * cache the stem view admins members
   */
  private static ExpirableCache<MultiKey, Boolean> stemViewAdminMembersExpirableCache = new ExpirableCache<MultiKey, Boolean>(5);

  /**
   * cache the stem view admins group
   */
  private static ExpirableCache<Boolean, Group> stemViewAdminExpirableCache = new ExpirableCache<Boolean, Group>(10);
  
  /**
   * could be null while bootstrapping
   * @return group name where membership means a subject can edit/add workflow
   */
  public static Group stemViewAdminGroup() {
    Group group = stemViewAdminExpirableCache.get(Boolean.TRUE);
    if (group == null) {
      synchronized (stemViewAdminExpirableCache) {
        group = stemViewAdminExpirableCache.get(Boolean.TRUE);
        if (group == null) {
          group = (Group)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupFinder.findByName(stemViewAdminGroupName(), false);
            }
          });
        }
        stemViewAdminExpirableCache.put(Boolean.TRUE, group);
      }
    }
    return group;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(StemViewPrivilege.class);

  /**
   * could be null while bootstrapping
   * @return group name where membership means a subject can edit/add workflow
   */
  public static Boolean stemViewAdmin(final Subject subject) {
    MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
    Boolean result = stemViewAdminMembersExpirableCache.get(multiKey);
    if (result == null) {
      result = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return stemViewAdminGroup().hasMember(subject);
        }
      });
      stemViewAdminMembersExpirableCache.put(multiKey, result);
    }
    return result;
  }

  static boolean testingWaitForAttributes = false;
  
  /**
   * recalculate stem view privileges, but do attributes in a new thread
   * @param memberIds
   */
  public static void recalculateStemViewPrivilegesForUsers(Collection<String> memberIdsCollection) {

    if (GrouperUtil.length(memberIdsCollection) == 0) {
      return;
    }
    
    final List<String> memberIdsList = new ArrayList<String>(memberIdsCollection);

    @SuppressWarnings("unchecked")
    Map<String, Object> debugMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
    
    long startNanos = System.nanoTime();
    
    debugMap.put("memberIdCount", GrouperUtil.length(memberIdsCollection));
    
    final RuntimeException[] attrException = new RuntimeException[1];
    try {
      // do attributes in a thread
      Thread attributeThread = new Thread(new Runnable() {
  
        @Override
        public void run() {
          try {
            recalculateStemViewPrivilegesAttributeInsert(debugMap, memberIdsList, null);
            recalculateStemViewPrivilegesGroupDelete(debugMap, memberIdsList, null);
            recalculateStemViewPrivilegesStemDelete(debugMap, memberIdsList, null);
            recalculateStemViewPrivilegesAttributeDelete(debugMap, memberIdsList, null);

          } catch (RuntimeException re) {
            attrException[0] = re;
            LOG.error("Error in attribute stem view privileges", re);
          }
        }

      });
      
      attributeThread.start();

      recalculateStemViewPrivilegesStemInsert(debugMap, memberIdsList, null);
      recalculateStemViewPrivilegesGroupInsert(debugMap, memberIdsList, null);
      
      // update the last calc time
      List<String> memberIdsToProcessAgain = recalculateStemViewPrivilegesLastLoginUpdate(debugMap, memberIdsList, "pass1_");
      
      // if it wasnt there, insert
      memberIdsToProcessAgain = recalculateStemViewPrivilegesLastLoginInsert(debugMap, memberIdsToProcessAgain);
      
      // if had problem with insert, update one more time
      recalculateStemViewPrivilegesLastLoginUpdate(debugMap, memberIdsToProcessAgain, "pass2_");
      
      // try to wait for attributes
      GrouperUtil.threadJoin(attributeThread, 4000);
      
      if (testingWaitForAttributes) {
        GrouperUtil.threadJoin(attributeThread);
      }
      
      if (attrException[0] != null) {
        throw attrException[0];
      }
            
    } finally {
      debugMap.put("tookMs", (System.nanoTime()-startNanos) / 1000000);
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /**
   * @param memberId
   */
  public static void recalculatePrivilegesIfNotAlreadyIncludedInIncremental(Subject subject) {

    if (subject == null || PrivilegeHelper.isWheelOrRootOrViewonlyRoot(subject)) {
      return;
    }
    
    int recalcChangeLogIfNeededInLastSeconds = StemViewPrivilege.recalcChangeLogIfNeededInLastSeconds();

    String memberId = null;
    Long lastStemViewNeed = null;
    
    //  # 0 means dont do this for anyone (full recalc each time)
    //  # -1 means do this for everyone who has ever checked stem view,
    //  # other negative values are not valid.
    if (recalcChangeLogIfNeededInLastSeconds != 0) {
    
      StringBuilder sql = new StringBuilder();
      sql.append("select gm.id, gll.last_stem_view_need "
          + " from grouper_last_login gll, grouper_members gm where gll.member_uuid = gm.id "
          + " and gm.subject_source = ? and gm.subject_id = ?");

      GcDbAccess gcDbAccess = new GcDbAccess().sql(sql.toString());
      gcDbAccess.addBindVar(subject.getSourceId()).addBindVar(subject.getId());

      Object[] results = gcDbAccess.select(Object[].class);
      
      if (results != null) {
        memberId = (String)results[0]; 
        lastStemViewNeed = GrouperUtil.longObjectValue(results[1], true);
      }
      
    }

    // lets see if not need to recalc
    if (recalcChangeLogIfNeededInLastSeconds > 0 && lastStemViewNeed != null 
        && (System.currentTimeMillis() - lastStemViewNeed) / 1000 < recalcChangeLogIfNeededInLastSeconds) {
      
      return;
      
    }
    
    // we need a recalc
    if (memberId == null) {
      
      memberId = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true).getId();
      
    }
    
    recalculateStemViewPrivilegesForUsers(GrouperUtil.toSet(memberId));
    
  }
  
  /**
   * 
   * @param stemId
   * @param memberId
   */
  public static void addStemPrivilegeIfNeeded(String stemId, String memberId) {
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("security.folders.are.viewable.by.all", false)) {
      
      return;
      
    }

    int recalcChangeLogIfNeededInLastSeconds = StemViewPrivilege.recalcChangeLogIfNeededInLastSeconds();

    GcDbAccess gcDbAccess = new GcDbAccess();
    
    //  # 0 means dont do this for anyone (full recalc each time),
    //  # -1 means do this for everyone who has ever checked stem view,
    //  # other negative values are not valid.
    if (recalcChangeLogIfNeededInLastSeconds == 0) {
      return;

    } else if (recalcChangeLogIfNeededInLastSeconds == -1) {
      gcDbAccess.sql("select count(1) from grouper_last_login gll where gll.member_uuid = ?");
      gcDbAccess.addBindVar(memberId);
    } else if (recalcChangeLogIfNeededInLastSeconds < -1) {
      throw new RuntimeException("Invalid ifNeededInLastSeconds setting: " + recalcChangeLogIfNeededInLastSeconds);
    } else {
      gcDbAccess.sql("select count(1) from grouper_last_login gll where gll.member_uuid = ?"
          + " and gll.last_stem_view_need is not null and gll.last_stem_view_need >= ? ");
      gcDbAccess.addBindVar(memberId);
      gcDbAccess.addBindVar(System.currentTimeMillis() - (recalcChangeLogIfNeededInLastSeconds*1000));
    }
    
    int count = gcDbAccess.select(int.class);
    
    if (count == 0) {
      return;
    }
    
    gcDbAccess = new GcDbAccess();
    gcDbAccess.sql("select count(1) from grouper_stem_view_privilege where member_uuid = ? and stem_uuid = ? and object_type = 'S'");
    gcDbAccess.addBindVar(memberId);
    gcDbAccess.addBindVar(stemId);
    count = gcDbAccess.select(int.class);
    
    if (count == 0) {
      new GcDbAccess().sql("insert into grouper_stem_view_privilege (member_uuid, stem_uuid, object_type) values (?, ?, 'S')")
        .addBindVar(memberId).addBindVar(stemId).executeSql();
    }
    
  }
  
  /**
   * 
   * @param debugMap
   * @param memberIdsList
   * @return the list of member ids to reprocess
   */
  private static List<String> recalculateStemViewPrivilegesLastLoginUpdate(
      Map<String, Object> debugMap, final List<String> memberIdsList, String logPrefix) {
    List<String> memberIdsToProcessAgain = new ArrayList<String>();
    if (GrouperUtil.length(memberIdsList) == 0) {
      return memberIdsToProcessAgain;
    }
    long groupStemStartNanos;
    groupStemStartNanos = System.nanoTime();
    
    // update last login
    int rowsLastLoginUpdate = 0;
    
    StringBuilder sql = new StringBuilder("update grouper_last_login set last_stem_view_compute = ? where member_uuid = ?");
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    
    List<List<Object>> batchBindVariables = new ArrayList<List<Object>>();

    long nowMillis = System.currentTimeMillis();

    for (String memberId : memberIdsList) {
      batchBindVariables.add(GrouperUtil.toList(nowMillis, memberId));
    }
    
    int[] rowsUpdated = gcDbAccess.batchBindVars(batchBindVariables).sql(sql.toString()).executeBatchSql();
    
    for (int i=0;i<memberIdsList.size(); i++) {
      if (rowsUpdated[i] != 1) {
        memberIdsToProcessAgain.add(memberIdsList.get(i));
      } else {
        rowsLastLoginUpdate++;
      }
    }

    debugMap.put(logPrefix + "rowsLastLoginUpdate", rowsLastLoginUpdate);
    debugMap.put(logPrefix+ "rowsLastLoginUpdateTookMs", (System.nanoTime()-groupStemStartNanos) / 1000000);
    debugMap.put(logPrefix+ "lastLoginUpdateProcessAgainCount", GrouperUtil.length(memberIdsToProcessAgain));
    return memberIdsToProcessAgain;
  }

  /**
   * 
   * @param debugMap
   * @param memberIdsList
   * @return the list of member ids to reprocess
   */
  private static List<String> recalculateStemViewPrivilegesLastLoginInsert(
      Map<String, Object> debugMap, final List<String> memberIdsList) {
    List<String> memberIdsToProcessAgain = new ArrayList<String>();
    if (GrouperUtil.length(memberIdsList) == 0) {
      return memberIdsToProcessAgain;
    }
    long groupStemStartNanos = System.nanoTime();
    
    // update last login
    int rowsLastLoginInsert = 0;
    
    StringBuilder sql = new StringBuilder("insert into grouper_last_login (member_uuid, last_stem_view_compute) values (?, ?)");
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    
    List<List<Object>> batchBindVariables = new ArrayList<List<Object>>();
    long nowMillis = System.currentTimeMillis();
    for (String memberId : memberIdsList) {
      batchBindVariables.add(GrouperUtil.toList(memberId, nowMillis));
    }
    try {
      int[] rowsInserted = gcDbAccess.batchBindVars(batchBindVariables).sql(sql.toString()).executeBatchSql();
      
    
      for (int i=0;i<memberIdsList.size(); i++) {
        if (rowsInserted[i] != 1) {
          memberIdsToProcessAgain.add(memberIdsList.get(i));
        } else {
          rowsLastLoginInsert++;
        }
      }
    } catch (RuntimeException re) {
      
      // try again
      for (String memberId : memberIdsList) {
        try {
          
          gcDbAccess = new GcDbAccess().sql(sql.toString()).addBindVar(memberId).addBindVar(nowMillis);
          int rowCount = gcDbAccess.executeSql();
          if (rowCount != 1) {
            memberIdsList.add(memberId);
          } else {
            rowsLastLoginInsert++;
          }
        } catch (RuntimeException re2) {
          
          memberIdsToProcessAgain.add(memberId);
        }
      }      
    }
    debugMap.put("rowsLastLoginInsert", rowsLastLoginInsert);
    debugMap.put("rowsLastLoginInsertTookMs", (System.nanoTime()-groupStemStartNanos) / 1000000);
    debugMap.put("lastLoginInsertProcessAgainCount", GrouperUtil.length(memberIdsToProcessAgain));
    return memberIdsToProcessAgain;
  }

  /**
   * assume the members are relevant, recalc for some stems
   * @param memberIdsSet
   * @param stemIdsSet
   */
  public static void recalculateStemViewPrivilegesStemDelete(Set<String> memberIdsSet, Set<String> stemIdsSet) {
    
    if (GrouperUtil.length(memberIdsSet) == 0) {
      return;
    }
    
    List<String> memberIdsList = new ArrayList<String>(memberIdsSet);
    List<String> stemIdsList = stemIdsSet == null ? null : new ArrayList<String>(memberIdsSet);

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("memberIdCount", GrouperUtil.length(memberIdsSet));
    
    try {
      recalculateStemViewPrivilegesStemDelete(debugMap, memberIdsList, stemIdsList);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /**
   * 
   * @return
   */
  public static int recalcChangeLogIfNeededInLastSeconds() {
    return GrouperConfig.retrieveConfig().propertyValueInt("security.folders.view.privileges.recalcChangeLog.ifNeededInLastSeconds", 604800);
  }
  
  private static void recalculateStemViewPrivilegesStemDelete(Map<String, Object> debugMap, 
      final List<String> memberIdsList, List<String> stemIds) {
    long stemDeleteTookMs = 0;
    if (GrouperUtil.length(stemIds) == 0) {
      recalculateStemViewPrivilegesStemDeleteHelper(debugMap, memberIdsList, stemIds);
      stemDeleteTookMs = (Long)debugMap.get("stemDeleteTookMs");
    } else {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemIds, 200);
      int stemRowsDeleted = 0;
      for (int i=0;i<numberOfBatches;i++) {
        List<String> batchStemIds = GrouperUtil.batchList(stemIds, 200, i);
        recalculateStemViewPrivilegesStemDeleteHelper(debugMap, memberIdsList, batchStemIds);
        stemDeleteTookMs += (Long)debugMap.get("stemDeleteTookMs");
        stemRowsDeleted += (Integer)debugMap.get("stemRowsDeleted");
      }
      debugMap.put("stemRowsDeleted", stemRowsDeleted);
    }
    debugMap.put("stemDeleteTookMs", stemDeleteTookMs/1000000);
  }

  private static void recalculateStemViewPrivilegesStemDeleteHelper(
      Map<String, Object> debugMap, final List<String> memberIdsList, List<String> stemIds) {
    long groupStemStartNanos = System.nanoTime();
    groupStemStartNanos = System.nanoTime();
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsList, BATCH_SIZE);

    int[] rowsDeleted = new int[] {0};
    for (int i=0;i<numberOfBatches;i++) {
      final int I = i;
      // this table locks in mysql for some reason
      GrouperUtil.tryMultipleTimes(5, new Runnable() {

        @Override
        public void run() {
          GcDbAccess gcDbAccess = new GcDbAccess();
          List<String> batchMemberIds = GrouperUtil.batchList(memberIdsList, BATCH_SIZE, I);
          
          StringBuilder sql = new StringBuilder("delete from grouper_stem_view_privilege where member_uuid in (");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          sql.append(") ");
          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and stem_uuid in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }
          sql.append("and object_type = 'S' and (member_uuid, stem_uuid) not in (select distinct gmav.member_id, gmav.owner_stem_id as stem_id ");
          sql.append(" from grouper_memberships_all_v gmav, grouper_fields gfl ");
          sql.append(" where gmav.field_id = gfl.id AND gmav.immediate_mship_enabled = 'T' ");
          sql.append(" and gfl.type = 'naming' and gmav.member_id in ( ");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          
          sql.append(") ");

          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and gmav.owner_stem_id in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }

          sql.append(" )");
          
          synchronized(StemViewPrivilege.class) {
            int rows = gcDbAccess.sql(sql.toString()).executeSql();
            rowsDeleted[0] += rows;
          }
        }
      });
    }
  
    debugMap.put("stemRowsDeleted", rowsDeleted[0]);
    debugMap.put("stemDeleteTookMs", System.nanoTime()-groupStemStartNanos);
  }

  /**
   * assume the members are relevant, recalc for some stems
   * @param memberIdsSet
   * @param stemIdsSet
   */
  public static void recalculateStemViewPrivilegesStemInsert(Set<String> memberIdsSet, Set<String> stemIdsSet) {
    
    if (GrouperUtil.length(memberIdsSet) == 0) {
      return;
    }
    
    List<String> memberIdsList = new ArrayList<String>(memberIdsSet);
    List<String> stemIdsList = stemIdsSet == null ? null : new ArrayList<String>(memberIdsSet);

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("memberIdCount", GrouperUtil.length(memberIdsSet));
    
    try {
      recalculateStemViewPrivilegesStemInsert(debugMap, memberIdsList, stemIdsList);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }


  private static void recalculateStemViewPrivilegesStemInsert(Map<String, Object> debugMap, 
      final List<String> memberIdsList, List<String> stemIds) {
    long stemInsertTookMs = 0;
    if (GrouperUtil.length(stemIds) == 0) {
      recalculateStemViewPrivilegesStemInsertHelper(debugMap, memberIdsList, stemIds);
      stemInsertTookMs = (Long)debugMap.get("stemInsertTookMs");
    } else {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemIds, 200);
      int stemRowsInserted = 0;
      for (int i=0;i<numberOfBatches;i++) {
        List<String> batchStemIds = GrouperUtil.batchList(stemIds, 200, i);
        recalculateStemViewPrivilegesStemInsertHelper(debugMap, memberIdsList, batchStemIds);
        stemInsertTookMs += (Long)debugMap.get("stemInsertTookMs");
        stemRowsInserted += (Integer)debugMap.get("stemRowsInserted");
      }
      debugMap.put("stemRowsInserted", stemRowsInserted);
    }
    debugMap.put("stemInsertTookMs", stemInsertTookMs/1000000);
  }

  private static void recalculateStemViewPrivilegesStemInsertHelper(Map<String, Object> debugMap, 
      final List<String> memberIdsList, List<String> stemIds) {
    long groupStemStartNanos = System.nanoTime();
    int[] rowsInserted = new int[] {0};
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsList, BATCH_SIZE);

    for (int i=0;i<numberOfBatches;i++) {
      final int I = i;
      // this table locks in mysql for some reason
      GrouperUtil.tryMultipleTimes(5, new Runnable() {

        @Override
        public void run() {
          GcDbAccess gcDbAccess = new GcDbAccess();
          List<String> batchMemberIds = GrouperUtil.batchList(memberIdsList, BATCH_SIZE, I);
          
          StringBuilder sql = new StringBuilder("insert into grouper_stem_view_privilege (stem_uuid, object_type, member_uuid) "
              + " ( select distinct gmav.owner_stem_id as stem_id, 'S' as object_type, gmav.member_id "  
              + " from grouper_memberships_all_v gmav, grouper_fields gfl "
              + " where gmav.field_id = gfl.id AND gmav.immediate_mship_enabled = 'T' "
              + " and gfl.type = 'naming' and gmav.member_id in ( ");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          sql.append(" ) ");
          
          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and gmav.owner_stem_id in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }
          sql.append(" and not exists (select 1 from grouper_stem_view_privilege gsvp "   
            + " where gsvp.member_uuid = gmav.member_id and gsvp.object_type ='S' and gsvp.stem_uuid = gmav.owner_stem_id ))");
          
          synchronized(StemViewPrivilege.class) {
            int rows = gcDbAccess.sql(sql.toString()).executeSql();
            rowsInserted[0] += rows;
          }
        }
      });
    }

    debugMap.put("stemRowsInserted", rowsInserted[0]);
    debugMap.put("stemInsertTookMs", System.nanoTime()-groupStemStartNanos);
  }
  
  /**
   * assume the members are relevant, recalc for some stems
   * @param memberIdsSet
   * @param stemIdsSet
   */
  public static void recalculateStemViewPrivilegesGroupDelete(Set<String> memberIdsSet, Set<String> stemIdsSet) {
    
    if (GrouperUtil.length(memberIdsSet) == 0) {
      return;
    }
    
    List<String> memberIdsList = new ArrayList<String>(memberIdsSet);
    List<String> stemIdsList = stemIdsSet == null ? null : new ArrayList<String>(memberIdsSet);

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("memberIdCount", GrouperUtil.length(memberIdsSet));
    
    try {
      recalculateStemViewPrivilegesGroupDelete(debugMap, memberIdsList, stemIdsList);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }


  private static void recalculateStemViewPrivilegesGroupDelete(Map<String, Object> debugMap, 
      final List<String> memberIdsList, List<String> stemIds) {
    long groupDeleteTookMs = 0;
    if (GrouperUtil.length(stemIds) == 0) {
      recalculateStemViewPrivilegesGroupDeleteHelper(debugMap, memberIdsList, stemIds);
      groupDeleteTookMs = (Long)debugMap.get("groupDeleteTookMs");
    } else {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemIds, 200);
      int groupRowsDeleted = 0;
      for (int i=0;i<numberOfBatches;i++) {
        List<String> batchStemIds = GrouperUtil.batchList(stemIds, 200, i);
        recalculateStemViewPrivilegesGroupDeleteHelper(debugMap, memberIdsList, batchStemIds);
        groupDeleteTookMs += (Long)debugMap.get("groupDeleteTookMs");
        groupRowsDeleted += (Integer)debugMap.get("groupRowsDeleted");
      }
      debugMap.put("groupRowsDeleted", groupRowsDeleted);
    }
    debugMap.put("groupDeleteTookMs", groupDeleteTookMs/1000000);
  }

  private static void recalculateStemViewPrivilegesGroupDeleteHelper(
      Map<String, Object> debugMap, final List<String> memberIdsList, List<String> stemIds) {
    long groupStemStartNanos = System.nanoTime();
    groupStemStartNanos = System.nanoTime();
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsList, BATCH_SIZE);

    int[] rowsDeleted = new int[] {0};
    for (int i=0;i<numberOfBatches;i++) {
      final int I = i;
      // this table locks in mysql for some reason
      GrouperUtil.tryMultipleTimes(5, new Runnable() {

        @Override
        public void run() {
          GcDbAccess gcDbAccess = new GcDbAccess();
          List<String> batchMemberIds = GrouperUtil.batchList(memberIdsList, BATCH_SIZE, I);
          
          StringBuilder sql = new StringBuilder("delete from grouper_stem_view_privilege where member_uuid in (");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          sql.append(") ");
          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and stem_uuid in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }
          
          sql.append(" and object_type = 'G' and (member_uuid, stem_uuid) not in (select distinct gmav.member_id, gg.parent_stem as stem_id "
              + " from grouper_memberships_all_v gmav, grouper_groups gg, grouper_fields gfl "
              + " where gmav.owner_group_id = gg.id AND gmav.field_id = gfl.id AND gmav.immediate_mship_enabled = 'T' "
              + " and gfl.type = 'access' and gmav.member_id in ( ");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          sql.append(") ");

          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and gg.parent_stem in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }

          sql.append(" )");
          synchronized(StemViewPrivilege.class) {
            int rows = gcDbAccess.sql(sql.toString()).executeSql();
            rowsDeleted[0] += rows;
          }
        }
      });
    }
  
    debugMap.put("groupRowsDeleted", rowsDeleted[0]);
    debugMap.put("groupDeleteTookMs", System.nanoTime()-groupStemStartNanos);
  }

  private static void recalculateStemViewPrivilegesGroupInsert(Map<String, Object> debugMap, 
      final List<String> memberIdsList, List<String> stemIds) {
    long groupInsertTookMs = 0;
    if (GrouperUtil.length(stemIds) == 0) {
      recalculateStemViewPrivilegesGroupInsertHelper(debugMap, memberIdsList, stemIds);
      groupInsertTookMs = (Long)debugMap.get("groupInsertTookMs");
    } else {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemIds, 200);
      int groupRowsInserted = 0;
      for (int i=0;i<numberOfBatches;i++) {
        List<String> batchStemIds = GrouperUtil.batchList(stemIds, 200, i);
        recalculateStemViewPrivilegesGroupInsertHelper(debugMap, memberIdsList, batchStemIds);
        groupInsertTookMs += (Long)debugMap.get("groupInsertTookMs");
        groupRowsInserted += (Integer)debugMap.get("groupRowsInserted");
      }
      debugMap.put("groupRowsInserted", groupRowsInserted);
    }
    debugMap.put("groupInsertTookMs", groupInsertTookMs/1000000);
  }

  /**
   * assume the members are relevant, recalc for some stems
   * @param memberIdsSet
   * @param stemIdsSet
   */
  public static void recalculateStemViewPrivilegesGroupInsert(Set<String> memberIdsSet, Set<String> stemIdsSet) {
    
    if (GrouperUtil.length(memberIdsSet) == 0) {
      return;
    }
    
    List<String> memberIdsList = new ArrayList<String>(memberIdsSet);
    List<String> stemIdsList = stemIdsSet == null ? null : new ArrayList<String>(memberIdsSet);

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("memberIdCount", GrouperUtil.length(memberIdsSet));
    
    try {
      recalculateStemViewPrivilegesGroupInsert(debugMap, memberIdsList, stemIdsList);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }


  private static void recalculateStemViewPrivilegesGroupInsertHelper(Map<String, Object> debugMap, 
      final List<String> memberIdsList, List<String> stemIds) {
    long groupStemStartNanos = System.nanoTime();
    final int[] rowsInserted = new int[] {0};
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsList, BATCH_SIZE);

    for (int i=0;i<numberOfBatches;i++) {
      final int I = i;
      // this table locks in mysql for some reason
      GrouperUtil.tryMultipleTimes(5, new Runnable() {

        @Override
        public void run() {
          GcDbAccess gcDbAccess = new GcDbAccess();
          List<String> batchMemberIds = GrouperUtil.batchList(memberIdsList, BATCH_SIZE, I);
          
          
          StringBuilder sql = new StringBuilder("insert into grouper_stem_view_privilege (stem_uuid, object_type, member_uuid) "
              + " ( select distinct gg.parent_stem as stem_id, 'G' as object_type, gmav.member_id "  
              + " from grouper_memberships_all_v gmav, grouper_groups gg, grouper_fields gfl "
              + " where gmav.owner_group_id = gg.id AND gmav.field_id = gfl.id AND gmav.immediate_mship_enabled = 'T' "
              + " and gfl.type = 'access' and gmav.member_id in ( ");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          sql.append(" ) ");
          
          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and gg.parent_stem in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }
          sql.append(" and not exists (select 1 from grouper_stem_view_privilege gsvp "   
            + " where gsvp.member_uuid = gmav.member_id and gsvp.object_type ='G' and gsvp.stem_uuid = gg.parent_stem ) ) ");
          synchronized(StemViewPrivilege.class) {
            int rows = gcDbAccess.sql(sql.toString()).executeSql();
            rowsInserted[0] += rows;
          }
        }
      });
    }
  
    debugMap.put("groupRowsInserted", rowsInserted[0]);
    debugMap.put("groupInsertTookMs", System.nanoTime()-groupStemStartNanos);
  }
  
  /**
   * assume the members are relevant, recalc for some stems
   * @param memberIdsSet
   * @param stemIdsSet
   */
  public static void recalculateStemViewPrivilegesAttributeInsert(Set<String> memberIdsSet, Set<String> stemIdsSet) {
    
    if (GrouperUtil.length(memberIdsSet) == 0) {
      return;
    }
    
    List<String> memberIdsList = new ArrayList<String>(memberIdsSet);
    List<String> stemIdsList = stemIdsSet == null ? null : new ArrayList<String>(memberIdsSet);

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("memberIdCount", GrouperUtil.length(memberIdsSet));
    
    try {
      recalculateStemViewPrivilegesAttributeInsert(debugMap, memberIdsList, stemIdsList);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }


  private static void recalculateStemViewPrivilegesAttributeInsert(Map<String, Object> debugMap, final List<String> memberIdsList, List<String> stemIds) {
    long attrInsertTookMs = 0;
    if (GrouperUtil.length(stemIds) == 0) {
      recalculateStemViewPrivilegesAttributeInsertHelper(debugMap, memberIdsList, stemIds);
      attrInsertTookMs = (Long)debugMap.get("attrInsertTookMs");
    } else {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemIds, BATCH_SIZE);
      int attrRowsInserted = 0;
      for (int i=0;i<numberOfBatches;i++) {
        List<String> batchStemIds = GrouperUtil.batchList(stemIds, BATCH_SIZE, i);
        recalculateStemViewPrivilegesAttributeInsertHelper(debugMap, memberIdsList, batchStemIds);
        attrInsertTookMs += (Long)debugMap.get("attrInsertTookMs");
        attrRowsInserted += (Integer)debugMap.get("attrRowsInserted");
      }
      debugMap.put("attrRowsInserted", attrRowsInserted);
    }
    debugMap.put("attrInsertTookMs", attrInsertTookMs/1000000);
  }

  
  
  private static void recalculateStemViewPrivilegesAttributeInsertHelper(Map<String, Object> debugMap, 
      final List<String> memberIdsList, List<String> stemIds) {
    long attrStartNanos = System.nanoTime();
    final int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsList, BATCH_SIZE);

    final int[] rowsInserted = new int[] {0};
    for (int i=0;i<numberOfBatches;i++) {
      final int I = i;
      // this table locks in mysql for some reason
      GrouperUtil.tryMultipleTimes(5, new Runnable() {

        @Override
        public void run() {

          GcDbAccess gcDbAccess = new GcDbAccess();
          List<String> batchMemberIds = GrouperUtil.batchList(memberIdsList, BATCH_SIZE, I);
          StringBuilder sql = new StringBuilder("insert into grouper_stem_view_privilege (stem_uuid, object_type, member_uuid) ( "
              + "select distinct ga.stem_id as stem_id, 'A' as object_type, gmav.member_id "
              + " from grouper_memberships_all_v gmav, grouper_attribute_def ga, grouper_fields gfl "
              + " where gmav.owner_attr_def_id = ga.id AND gmav.field_id = gfl.id AND gmav.immediate_mship_enabled = 'T' "
              + " and gfl.type = 'attributeDef' and gmav.member_id in ( ");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          sql.append(" ) ");
          
          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and ga.stem_id in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }

          sql.append(" and not exists (select 1 from grouper_stem_view_privilege gsvp "
              + " where gsvp.member_uuid = gmav.member_id and gsvp.object_type ='A' and gsvp.stem_uuid = ga.stem_id ) )");
          
          synchronized(StemViewPrivilege.class) {
            int rows = gcDbAccess.sql(sql.toString()).executeSql();
            rowsInserted[0] += rows;
          }
        }
      });
    }

    debugMap.put("attrRowsInserted", rowsInserted[0]);
    debugMap.put("attrInsertTookMs", System.nanoTime()-attrStartNanos);
  }

  /**
   * assume the members are relevant, recalc for some stems
   * @param memberIdsSet
   * @param stemIdsSet
   */
  public static void recalculateStemViewPrivilegesAttributeDelete(Set<String> memberIdsSet, Set<String> stemIdsSet) {
    
    if (GrouperUtil.length(memberIdsSet) == 0) {
      return;
    }
    
    List<String> memberIdsList = new ArrayList<String>(memberIdsSet);
    List<String> stemIdsList = stemIdsSet == null ? null : new ArrayList<String>(memberIdsSet);

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("memberIdCount", GrouperUtil.length(memberIdsSet));
    
    try {
      recalculateStemViewPrivilegesAttributeDelete(debugMap, memberIdsList, stemIdsList);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  private static void recalculateStemViewPrivilegesAttributeDelete(Map<String, Object> debugMap, final List<String> memberIdsList, List<String> stemIds) {
    long attrDeleteTookMs = 0;
    if (GrouperUtil.length(stemIds) == 0) {
      recalculateStemViewPrivilegesAttributeDeleteHelper(debugMap, memberIdsList, stemIds);
      attrDeleteTookMs = (Long)debugMap.get("attrDeleteTookMs");
    } else {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemIds, BATCH_SIZE);
      int attrRowsDeleted = 0;
      for (int i=0;i<numberOfBatches;i++) {
        List<String> batchStemIds = GrouperUtil.batchList(stemIds, BATCH_SIZE, i);
        recalculateStemViewPrivilegesAttributeDeleteHelper(debugMap, memberIdsList, batchStemIds);
        attrDeleteTookMs += (Long)debugMap.get("attrDeleteTookMs");
        attrRowsDeleted += (Integer)debugMap.get("attrRowsDeleted");
      }
      debugMap.put("attrRowsDeleted", attrRowsDeleted);
    }
    debugMap.put("attrDeleteTookMs", attrDeleteTookMs/1000000);
  }

  private static void recalculateStemViewPrivilegesAttributeDeleteHelper(Map<String, Object> debugMap, final List<String> memberIdsList, List<String> stemIds) {
    long attrStartNanos = System.nanoTime();
    final int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsList, BATCH_SIZE);

    final int rowsDeleted[] = new int[] {0};
    for (int i=0;i<numberOfBatches;i++) {
      final int I = i;
      // this table locks in mysql for some reason
      GrouperUtil.tryMultipleTimes(5, new Runnable() {

        @Override
        public void run() {
          GcDbAccess gcDbAccess = new GcDbAccess();
          List<String> batchMemberIds = GrouperUtil.batchList(memberIdsList, BATCH_SIZE, I);
          StringBuilder sql = new StringBuilder("delete from grouper_stem_view_privilege where member_uuid in (");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          sql.append(") ");
          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and stem_uuid in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }
          sql.append(" and object_type = 'A' and (member_uuid, stem_uuid) not in (select distinct gmav.member_id, ga.stem_id "
              + " from grouper_memberships_all_v gmav, grouper_attribute_def ga, grouper_fields gfl "
              + " where gmav.owner_attr_def_id = ga.id AND gmav.field_id = gfl.id AND gmav.immediate_mship_enabled = 'T' "
              + " and gfl.type = 'attributeDef' "
              + " and gmav.member_id in (");
          GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberIds));
          for (String memberId : batchMemberIds) {
            gcDbAccess.addBindVar(memberId);
          }
          sql.append(") ");
    
          if (GrouperUtil.length(stemIds) > 0) {
            sql.append(" and ga.stem_id in ( ");
            GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(stemIds));
            for (String stemId : stemIds) {
              gcDbAccess.addBindVar(stemId);
            }
            sql.append(" ) ");
          }
    
          sql.append(" )");
          synchronized(StemViewPrivilege.class) {
            int rows = gcDbAccess.sql(sql.toString()).executeSql();
            rowsDeleted[0] += rows;
          }
        }
      });
    }

    debugMap.put("attrRowsDeleted", rowsDeleted[0]);
    
    // start with nanos
    debugMap.put("attrDeleteTookMs", System.nanoTime()-attrStartNanos);
  }

  /**
   * @param subject
   */
  public static void updateLastStemViewNeed(Subject subject) {
    new GcDbAccess().sql("update grouper_last_login gll set last_stem_view_need = ? "
        + " where gll.member_uuid = (select gm.id from grouper_members gm where gm.subject_source = ? and gm.subject_id = ? )")
      .addBindVar(System.currentTimeMillis()).addBindVar(subject.getSourceId()).addBindVar(subject.getId()).executeSql();
    
  }


}
