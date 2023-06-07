package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;


public class SqlCacheMembershipDaoTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new SqlCacheMembershipDaoTest("testStoreRetrieve"));
  }
  
  public SqlCacheMembershipDaoTest(String name) {
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
    
    Subject subject = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    group.addMember(subject);
    Member member = MemberFinder.findBySubject(grouperSession, subject, false, null);
    
    SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
    sqlCacheGroup.setFieldInternalId(members.getInternalId());
    sqlCacheGroup.setGroupInternalId(group.getInternalId());
    sqlCacheGroup.setEnabledOn(new Timestamp(System.currentTimeMillis() + 10*1000*60));
    SqlCacheGroupDao.store(sqlCacheGroup);
    
    
    SqlCacheMembership sqlCacheMembership = new SqlCacheMembership();
    sqlCacheMembership.setSqlCacheGroupInternalId(sqlCacheGroup.getInternalId());
    sqlCacheMembership.setMemberInternalId(member.getInternalId());
    sqlCacheMembership.setFlattenedAddTimestamp(new Timestamp(System.currentTimeMillis()));
    SqlCacheMembershipDao.store(sqlCacheMembership);
    
    sqlCacheMembership = SqlCacheMembershipDao.retrieveByInternalId(sqlCacheMembership.getInternalId());
    
    SqlCacheMembershipDao.delete(sqlCacheMembership);

    sqlCacheMembership = SqlCacheMembershipDao.retrieveByInternalId(sqlCacheMembership.getInternalId());
    
    assertNull(sqlCacheMembership);
    
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
