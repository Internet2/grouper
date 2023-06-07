package edu.internet2.middleware.grouper.sqlCache;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeAssignResult markerAttributeResult = testGroup.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheGroup");
    GrouperUtil.sleep(2000);

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("CHANGE_LOG_consumer_sqlCacheGroup");

    
    
  }
}
