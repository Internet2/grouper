package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import junit.textui.TestRunner;


public class SqlCacheGroupDaoTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new SqlCacheGroupDaoTest("testRetrieveOrCreate"));
  }
  
  public SqlCacheGroupDaoTest(String name) {
    super(name);
  }

  protected void setUp() {
    super.setUp();
  }

  protected void tearDown() {
    super.tearDown();
  }

  public void testStoreRetrieve() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Field members = Group.getDefaultList();
    
    SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
    sqlCacheGroup.setFieldInternalId(members.getInternalId());
    sqlCacheGroup.setGroupInternalId(group.getInternalId());
    sqlCacheGroup.setEnabledOn(new Timestamp(System.currentTimeMillis() + 10*1000*60));
    SqlCacheGroupDao.store(sqlCacheGroup);
    
    sqlCacheGroup = SqlCacheGroupDao.retrieveByInternalId(sqlCacheGroup.getInternalId());
    
    SqlCacheGroupDao.delete(sqlCacheGroup);

    sqlCacheGroup = SqlCacheGroupDao.retrieveByInternalId(sqlCacheGroup.getInternalId());
    
    assertNull(sqlCacheGroup);
    
  }
  

  public void testRetrieveOrCreate() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Field members = Group.getDefaultList();
    
    SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
    sqlCacheGroup.setFieldInternalId(members.getInternalId());
    sqlCacheGroup.setGroupInternalId(group.getInternalId());
    sqlCacheGroup.setEnabledOn(new Timestamp(System.currentTimeMillis() + 10*1000*60));

    SqlCacheGroupDao.retrieveOrCreateBySqlGroupCache(GrouperUtil.toList(sqlCacheGroup));
    
    sqlCacheGroup = SqlCacheGroupDao.retrieveByInternalId(sqlCacheGroup.getInternalId());

    
    MultiKey multiKey = new MultiKey(group.getInternalId(), members.getInternalId());
    
    sqlCacheGroup = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(GrouperUtil.toList(multiKey)).get(multiKey);
    
    assertNotNull(sqlCacheGroup);

    multiKey = new MultiKey(group.getName(), members.getName());
    
    sqlCacheGroup = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(GrouperUtil.toList(multiKey)).get(multiKey);
    
    assertNotNull(sqlCacheGroup);
    
    SqlCacheGroupDao.delete(sqlCacheGroup);

    sqlCacheGroup = SqlCacheGroupDao.retrieveByInternalId(sqlCacheGroup.getInternalId());
    
    assertNull(sqlCacheGroup);
    
  }
  
  
  
}
