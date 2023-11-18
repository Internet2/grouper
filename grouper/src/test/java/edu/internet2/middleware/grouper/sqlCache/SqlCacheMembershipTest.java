package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.subj.TestSubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class SqlCacheMembershipTest extends GrouperTest {

  public SqlCacheMembershipTest(String name) {
    super(name);
  }

  protected void setUp() {
    super.setUp();
  }

  protected void tearDown() {
    super.tearDown();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new SqlCacheMembershipTest("testChangeLogConsumer"));
  }

  public void testChangeLogConsumer() {

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheGroup");

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    long millisPreAdd = System.currentTimeMillis();
    GrouperUtil.sleep(1000);
    
    testGroup.addMember(SubjectTestHelper.SUBJ0);

    GrouperUtil.sleep(1000);
    long millisPostAdd = System.currentTimeMillis();

    AttributeAssignResult markerAttributeResult = testGroup.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheGroup");
    GrouperUtil.sleep(2000);

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("CHANGE_LOG_consumer_sqlCacheGroup");

    String sqlMembershipView = "select gscmv.group_name, gscmv.list_name, gscmv.subject_source, gscmv.subject_id, gscmv.flattened_add_timestamp from grouper_sql_cache_mship_v gscmv order by 1, 2, 3, 4";
    
    List<Object[]> groupNameFieldNameSubjectIdSourceIdInDbs = new GcDbAccess().sql(
        sqlMembershipView)
      .selectList(Object[].class);

    assertEquals(1, GrouperUtil.length(groupNameFieldNameSubjectIdSourceIdInDbs));

    assertEquals(testGroup.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[0]);
    assertEquals(Group.getDefaultList().getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[1]);
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[2]);
    assertEquals(SubjectTestHelper.SUBJ0.getId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[3]);
    assertTrue(millisPreAdd < ((Timestamp)groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[4]).getTime());
    // TODO uncomment when fixed
    // assertTrue(millisPostAdd > ((Timestamp)groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[4]).getTime());

    
  }
}
