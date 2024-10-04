package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class SqlCacheGroupTest extends GrouperTest {

  public SqlCacheGroupTest(String name) {
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
    TestRunner.run(new SqlCacheGroupTest("testAttributeDefs"));
  }

  public void testAttributeDefs() {
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    new StemSave().assignName("test").save();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // make sure nothing is out of sync to begin with
    runRullSync(true);

    long originalSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    AttributeDef testAttrDef = new AttributeDefSave().assignName("test:testAttributeDef").assignCreateParentStemsIfNotExist(false).save();
    testAttrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
    testAttrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
    
    // try an attr def that's deleted too
    AttributeDef testAttrDef2 = new AttributeDefSave().assignName("test:testAttributeDef2").assignCreateParentStemsIfNotExist(false).save();
    testAttrDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
    testAttrDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
    testAttrDef2.delete();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    long newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);
    
    PITAttributeDef pitTestAttrDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(testAttrDef.getId(), true);
    PITAttributeDef pitTestAttrDef2 = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(testAttrDef2.getId(), true);
    
    // eight fields times two stems
    assertEquals(originalSize + 16, newSize);
    
    String sqlCacheGroupQuery = "select gscg.internal_id, gscg.group_internal_id, gf.name, gscg.membership_size, gscg.enabled_on, gscg.disabled_on from grouper_sql_cache_group gscg, grouper_fields gf where gscg.field_internal_id = gf.internal_id and gscg.group_internal_id = ? and gf.type in ('attributeDef') order by 2, 3";
    
    verifyInitialAttributeDefs(testAttrDef, testAttrDef2, pitTestAttrDef, pitTestAttrDef2, sqlCacheGroupQuery, true);
    
    // full sync should see no changes
    runRullSync(false);
    
    // make some changes and verify that it's all corrected by the full sync
    assertEquals(2, new GcDbAccess().sql("delete from grouper_sql_cache_mship where sql_cache_group_internal_id in (select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('attributeDef')))").addBindVar(testAttrDef.getIdIndex()).executeSql());
    assertEquals(8, new GcDbAccess().sql("delete from grouper_sql_cache_group where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('attributeDef'))").addBindVar(testAttrDef.getIdIndex()).executeSql());
    assertEquals(8, new GcDbAccess().sql("update grouper_sql_cache_group set disabled_on = null where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('attributeDef'))").addBindVar(testAttrDef2.getIdIndex()).executeSql());
    runRullSync(true);
    verifyInitialAttributeDefs(testAttrDef, testAttrDef2, pitTestAttrDef, pitTestAttrDef2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // make some more changes and verify that it's all corrected by the full sync
    assertEquals(8, new GcDbAccess().sql("update grouper_sql_cache_group set disabled_on = ? where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('attributeDef'))").addBindVar(new Timestamp(System.currentTimeMillis())).addBindVar(testAttrDef.getIdIndex()).executeSql());
    runRullSync(true);
    verifyInitialAttributeDefs(testAttrDef, testAttrDef2, pitTestAttrDef, pitTestAttrDef2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // make some more changes and verify that it's all corrected by the full sync
    assertEquals(8, new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '999' where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('attributeDef'))").addBindVar(testAttrDef.getIdIndex()).executeSql());
    assertEquals(8, new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '999' where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('attributeDef'))").addBindVar(testAttrDef2.getIdIndex()).executeSql());
    runRullSync(true);
    verifyInitialAttributeDefs(testAttrDef, testAttrDef2, pitTestAttrDef, pitTestAttrDef2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // adjust membership counts
    testAttrDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
    testAttrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, true);
    testAttrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, true);
    testAttrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    // doesn't change
    assertEquals(originalSize + 16, newSize);

    {  
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testAttrDef.getIdIndex()).selectList(Object[].class);
      
      assertEquals(8, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("attrAdmins", sqlCacheGroups.get(0)[2]);
      assertEquals(3L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertNull(sqlCacheGroups.get(0)[5]);
      
      assertEquals("attrDefAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(1L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertNull(sqlCacheGroups.get(1)[5]);
      
      assertEquals("attrDefAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertNull(sqlCacheGroups.get(2)[5]);
      
      assertEquals("attrOptins", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertNull(sqlCacheGroups.get(3)[5]);
      
      assertEquals("attrOptouts", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertNull(sqlCacheGroups.get(4)[5]);
      
      assertEquals("attrReaders", sqlCacheGroups.get(5)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(5)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[4], false).getTime());
      assertNull(sqlCacheGroups.get(5)[5]);
      
      assertEquals("attrUpdaters", sqlCacheGroups.get(6)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(6)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[4], false).getTime());
      assertNull(sqlCacheGroups.get(6)[5]);
      
      assertEquals("attrViewers", sqlCacheGroups.get(7)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(7)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[4], false).getTime());
      assertNull(sqlCacheGroups.get(7)[5]);
    }
    
    // full sync should see no changes
    runRullSync(false);
    
    // delete the group
    testAttrDef.delete();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    // doesn't change
    assertEquals(originalSize + 16, newSize);

    pitTestAttrDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(testAttrDef.getId(), true);

    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testAttrDef.getIdIndex()).selectList(Object[].class);
      
      assertEquals(8, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("attrAdmins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertEquals(pitTestAttrDef.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[5], false).getTime());
      
      assertEquals("attrDefAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertEquals(pitTestAttrDef.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[5], false).getTime());
      
      assertEquals("attrDefAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertEquals(pitTestAttrDef.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[5], false).getTime());
      
      assertEquals("attrOptins", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertEquals(pitTestAttrDef.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[5], false).getTime());
      
      assertEquals("attrOptouts", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertEquals(pitTestAttrDef.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[5], false).getTime());
      
      assertEquals("attrReaders", sqlCacheGroups.get(5)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(5)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[4], false).getTime());
      assertEquals(pitTestAttrDef.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[5], false).getTime());
      
      assertEquals("attrUpdaters", sqlCacheGroups.get(6)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(6)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[4], false).getTime());
      assertEquals(pitTestAttrDef.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[5], false).getTime());
      
      assertEquals("attrViewers", sqlCacheGroups.get(7)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(7)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[4], false).getTime());
      assertEquals(pitTestAttrDef.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[5], false).getTime());
    }
    
    // full sync should see no changes
    runRullSync(false);
  }

  private void verifyInitialAttributeDefs(AttributeDef testAttrDef, AttributeDef testAttrDef2, PITAttributeDef pitTestAttrDef, PITAttributeDef pitTestAttrDef2, String sqlCacheGroupQuery, boolean verifyDisabledTime) {
    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testAttrDef.getIdIndex()).selectList(Object[].class);
  
      assertEquals(8, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("attrAdmins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertNull(sqlCacheGroups.get(0)[5]);
      
      assertEquals("attrDefAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(2L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertNull(sqlCacheGroups.get(1)[5]);
      
      assertEquals("attrDefAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertNull(sqlCacheGroups.get(2)[5]);
      
      assertEquals("attrOptins", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertNull(sqlCacheGroups.get(3)[5]);
      
      assertEquals("attrOptouts", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertNull(sqlCacheGroups.get(4)[5]);
      
      assertEquals("attrReaders", sqlCacheGroups.get(5)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(5)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[4], false).getTime());
      assertNull(sqlCacheGroups.get(5)[5]);
      
      assertEquals("attrUpdaters", sqlCacheGroups.get(6)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(6)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[4], false).getTime());
      assertNull(sqlCacheGroups.get(6)[5]);
      
      assertEquals("attrViewers", sqlCacheGroups.get(7)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(7)[3], false).longValue());
      assertEquals(pitTestAttrDef.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[4], false).getTime());
      assertNull(sqlCacheGroups.get(7)[5]);
    }
    
    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testAttrDef2.getIdIndex()).selectList(Object[].class);
  
      assertEquals(8, GrouperUtil.length(sqlCacheGroups));

      assertEquals("attrAdmins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestAttrDef2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestAttrDef2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(0)[5]);
      }
      
      assertEquals("attrDefAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestAttrDef2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestAttrDef2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(1)[5]);
      }
      
      assertEquals("attrDefAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestAttrDef2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestAttrDef2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(2)[5]);
      }
      
      assertEquals("attrOptins", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestAttrDef2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestAttrDef2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(3)[5]);
      }
      
      assertEquals("attrOptouts", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestAttrDef2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestAttrDef2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(4)[5]);
      }
      
      assertEquals("attrReaders", sqlCacheGroups.get(5)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(5)[3], false).longValue());
      assertEquals(pitTestAttrDef2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestAttrDef2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(5)[5]);
      }
      
      assertEquals("attrUpdaters", sqlCacheGroups.get(6)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(6)[3], false).longValue());
      assertEquals(pitTestAttrDef2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestAttrDef2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(6)[5]);
      }
      
      assertEquals("attrViewers", sqlCacheGroups.get(7)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(7)[3], false).longValue());
      assertEquals(pitTestAttrDef2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestAttrDef2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(7)[5]);
      }
    }
  }
  
  public void testStems() {
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    new StemSave().assignName("test").save();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // make sure nothing is out of sync to begin with
    runRullSync(true);

    long originalSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    Stem testStem = new StemSave().assignName("test:testStem").assignCreateParentStemsIfNotExist(false).save();
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    testStem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);
    
    // try a stem that's deleted too
    Stem testStem2 = new StemSave().assignName("test:testStem2").assignCreateParentStemsIfNotExist(false).save();
    testStem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    testStem2.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);
    testStem2.delete();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    long newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);
    
    PITStem pitTestStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(testStem.getId(), true);
    PITStem pitTestStem2 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(testStem2.getId(), true);
    
    // five fields times two stems
    assertEquals(originalSize + 10, newSize);
    
    String sqlCacheGroupQuery = "select gscg.internal_id, gscg.group_internal_id, gf.name, gscg.membership_size, gscg.enabled_on, gscg.disabled_on from grouper_sql_cache_group gscg, grouper_fields gf where gscg.field_internal_id = gf.internal_id and gscg.group_internal_id = ? and gf.type in ('naming') order by 2, 3";
    
    verifyInitialStems(testStem, testStem2, pitTestStem, pitTestStem2, sqlCacheGroupQuery, true);
    
    // full sync should see no changes
    runRullSync(false);
    
    // make some changes and verify that it's all corrected by the full sync
    assertEquals(2, new GcDbAccess().sql("delete from grouper_sql_cache_mship where sql_cache_group_internal_id in (select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('naming')))").addBindVar(testStem.getIdIndex()).executeSql());
    assertEquals(5, new GcDbAccess().sql("delete from grouper_sql_cache_group where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('naming'))").addBindVar(testStem.getIdIndex()).executeSql());
    assertEquals(5, new GcDbAccess().sql("update grouper_sql_cache_group set disabled_on = null where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('naming'))").addBindVar(testStem2.getIdIndex()).executeSql());
    runRullSync(true);
    verifyInitialStems(testStem, testStem2, pitTestStem, pitTestStem2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // make some more changes and verify that it's all corrected by the full sync
    assertEquals(5, new GcDbAccess().sql("update grouper_sql_cache_group set disabled_on = ? where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('naming'))").addBindVar(new Timestamp(System.currentTimeMillis())).addBindVar(testStem.getIdIndex()).executeSql());
    runRullSync(true);
    verifyInitialStems(testStem, testStem2, pitTestStem, pitTestStem2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // make some more changes and verify that it's all corrected by the full sync
    assertEquals(5, new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '999' where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('naming'))").addBindVar(testStem.getIdIndex()).executeSql());
    assertEquals(5, new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '999' where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('naming'))").addBindVar(testStem2.getIdIndex()).executeSql());
    runRullSync(true);
    verifyInitialStems(testStem, testStem2, pitTestStem, pitTestStem2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // adjust membership counts
    testStem.revokePriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);
    testStem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN);
    testStem.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ADMIN);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    // doesn't change
    assertEquals(originalSize + 10, newSize);

    {  
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testStem.getIdIndex()).selectList(Object[].class);
      
      assertEquals(5, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("creators", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertNull(sqlCacheGroups.get(0)[5]);
      
      assertEquals("stemAdmins", sqlCacheGroups.get(1)[2]);
      assertEquals(3L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertNull(sqlCacheGroups.get(1)[5]);
      
      assertEquals("stemAttrReaders", sqlCacheGroups.get(2)[2]);
      assertEquals(1L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertNull(sqlCacheGroups.get(2)[5]);
      
      assertEquals("stemAttrUpdaters", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertNull(sqlCacheGroups.get(3)[5]);
      
      assertEquals("stemViewers", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertNull(sqlCacheGroups.get(4)[5]);
    }
    
    // full sync should see no changes
    runRullSync(false);
    
    // delete the group
    testStem.delete();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    // doesn't change
    assertEquals(originalSize + 10, newSize);

    pitTestStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(testStem.getId(), true);

    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testStem.getIdIndex()).selectList(Object[].class);
      
      assertEquals(5, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("creators", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertEquals(pitTestStem.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[5], false).getTime());
      
      assertEquals("stemAdmins", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertEquals(pitTestStem.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[5], false).getTime());
      
      assertEquals("stemAttrReaders", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertEquals(pitTestStem.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[5], false).getTime());
      
      assertEquals("stemAttrUpdaters", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertEquals(pitTestStem.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[5], false).getTime());
      
      assertEquals("stemViewers", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertEquals(pitTestStem.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[5], false).getTime());
    }
    
    // full sync should see no changes
    runRullSync(false);
  }

  private void verifyInitialStems(Stem testStem, Stem testStem2, PITStem pitTestStem, PITStem pitTestStem2, String sqlCacheGroupQuery, boolean verifyDisabledTime) {
    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testStem.getIdIndex()).selectList(Object[].class);
  
      assertEquals(5, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("creators", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertNull(sqlCacheGroups.get(0)[5]);
      
      assertEquals("stemAdmins", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertNull(sqlCacheGroups.get(1)[5]);
      
      assertEquals("stemAttrReaders", sqlCacheGroups.get(2)[2]);
      assertEquals(2L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertNull(sqlCacheGroups.get(2)[5]);
      
      assertEquals("stemAttrUpdaters", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertNull(sqlCacheGroups.get(3)[5]);
      
      assertEquals("stemViewers", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestStem.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertNull(sqlCacheGroups.get(4)[5]);
    }
    
    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testStem2.getIdIndex()).selectList(Object[].class);
  
      assertEquals(5, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("creators", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestStem2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestStem2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(0)[5]);
      }
      
      assertEquals("stemAdmins", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestStem2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestStem2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(1)[5]);
      }
      
      assertEquals("stemAttrReaders", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestStem2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestStem2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(2)[5]);
      }
      
      assertEquals("stemAttrUpdaters", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestStem2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestStem2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(3)[5]);
      }
      
      assertEquals("stemViewers", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestStem2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestStem2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(4)[5]);
      }
    }
  }

  public void testGroups() {
    // we want to make the id index and internal id for groups out of sync to make sure we're saving the actual internal id.
    TableIndex.reserveIds(TableIndexType.groupInternalId, 10000);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    new StemSave().assignName("test").save();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // make sure nothing is out of sync to begin with
    runRullSync(true);

    long originalSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(false).save();
    testGroup.addMember(SubjectTestHelper.SUBJ0);
    testGroup.addMember(SubjectTestHelper.SUBJ1);
    
    // try a group that's deleted too
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(false).save();
    testGroup2.addMember(SubjectTestHelper.SUBJ0);
    testGroup2.addMember(SubjectTestHelper.SUBJ1);
    testGroup2.delete();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    long newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);
    
    PITGroup pitTestGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(testGroup.getId(), true);
    PITGroup pitTestGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(testGroup2.getId(), true);
    
    // members list plus 8 access fields times two groups
    assertEquals(originalSize + 18, newSize);
    
    String sqlCacheGroupQuery = "select gscg.internal_id, gscg.group_internal_id, gf.name, gscg.membership_size, gscg.enabled_on, gscg.disabled_on from grouper_sql_cache_group gscg, grouper_fields gf where gscg.field_internal_id = gf.internal_id and gscg.group_internal_id = ? and gf.type in ('list','access') order by 2, 3";
    
    verifyInitialGroups(testGroup, testGroup2, pitTestGroup, pitTestGroup2, sqlCacheGroupQuery, true);
    
    // full sync should see no changes
    runRullSync(false);
    
    // make some changes and verify that it's all corrected by the full sync
    assertEquals(4, new GcDbAccess().sql("delete from grouper_sql_cache_mship where sql_cache_group_internal_id in (select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access')))").addBindVar(testGroup.getInternalId()).executeSql());
    assertEquals(9, new GcDbAccess().sql("delete from grouper_sql_cache_group where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(testGroup.getInternalId()).executeSql());
    assertEquals(9, new GcDbAccess().sql("update grouper_sql_cache_group set disabled_on = null where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(testGroup2.getInternalId()).executeSql());
    runRullSync(true);
    verifyInitialGroups(testGroup, testGroup2, pitTestGroup, pitTestGroup2, sqlCacheGroupQuery, false);    
    runRullSync(false);
    
    // make some more changes and verify that it's all corrected by the full sync
    assertEquals(9, new GcDbAccess().sql("update grouper_sql_cache_group set disabled_on = ? where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(new Timestamp(System.currentTimeMillis())).addBindVar(testGroup.getInternalId()).executeSql());
    runRullSync(true);
    verifyInitialGroups(testGroup, testGroup2, pitTestGroup, pitTestGroup2, sqlCacheGroupQuery, false);    
    runRullSync(false);
    
    // make some more changes and verify that it's all corrected by the full sync
    assertEquals(9, new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '999' where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(testGroup.getInternalId()).executeSql());
    assertEquals(9, new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '999' where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(testGroup2.getInternalId()).executeSql());
    runRullSync(true);
    verifyInitialGroups(testGroup, testGroup2, pitTestGroup, pitTestGroup2, sqlCacheGroupQuery, false);    
    runRullSync(false);
    
    // adjust membership counts
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);
    testGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    testGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    testGroup.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    // doesn't change
    assertEquals(originalSize + 18, newSize);

    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testGroup.getInternalId()).selectList(Object[].class);
  
      assertEquals(9, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("admins", sqlCacheGroups.get(0)[2]);
      assertEquals(3L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertNull(sqlCacheGroups.get(0)[5]);
      
      assertEquals("groupAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertNull(sqlCacheGroups.get(1)[5]);
      
      assertEquals("groupAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertNull(sqlCacheGroups.get(2)[5]);
      
      assertEquals("members", sqlCacheGroups.get(3)[2]);
      assertEquals(1L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertNull(sqlCacheGroups.get(3)[5]);
      
      assertEquals("optins", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertNull(sqlCacheGroups.get(4)[5]);
      
      assertEquals("optouts", sqlCacheGroups.get(5)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(5)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[4], false).getTime());
      assertNull(sqlCacheGroups.get(5)[5]);
      
      assertEquals("readers", sqlCacheGroups.get(6)[2]);
      assertEquals(1L, GrouperUtil.longObjectValue(sqlCacheGroups.get(6)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[4], false).getTime());
      assertNull(sqlCacheGroups.get(6)[5]);
      
      assertEquals("updaters", sqlCacheGroups.get(7)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(7)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[4], false).getTime());
      assertNull(sqlCacheGroups.get(7)[5]);
      
      assertEquals("viewers", sqlCacheGroups.get(8)[2]);
      assertEquals(1L, GrouperUtil.longObjectValue(sqlCacheGroups.get(8)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(8)[4], false).getTime());
      assertNull(sqlCacheGroups.get(8)[5]);
    }
    
    // full sync should see no changes
    runRullSync(false);
    
    // delete the group
    testGroup.delete();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    // doesn't change
    assertEquals(originalSize + 18, newSize);

    pitTestGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(testGroup.getId(), true);

    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testGroup.getInternalId()).selectList(Object[].class);
  
      assertEquals(9, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("admins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[5], false).getTime());
      
      assertEquals("groupAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[5], false).getTime());
      
      assertEquals("groupAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[5], false).getTime());
      
      assertEquals("members", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[5], false).getTime());
      
      assertEquals("optins", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[5], false).getTime());
      
      assertEquals("optouts", sqlCacheGroups.get(5)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(5)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[5], false).getTime());
      
      assertEquals("readers", sqlCacheGroups.get(6)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(6)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[5], false).getTime());
      
      assertEquals("updaters", sqlCacheGroups.get(7)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(7)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[5], false).getTime());
      
      assertEquals("viewers", sqlCacheGroups.get(8)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(8)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(8)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(8)[5], false).getTime());
    }
    
    // full sync should see no changes
    runRullSync(false);
  }

  private void verifyInitialGroups(Group testGroup, Group testGroup2, PITGroup pitTestGroup, PITGroup pitTestGroup2, String sqlCacheGroupQuery, boolean verifyDisabledTime) {
    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testGroup.getInternalId()).selectList(Object[].class);
  
      assertEquals(9, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("admins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertNull(sqlCacheGroups.get(0)[5]);
      
      assertEquals("groupAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertNull(sqlCacheGroups.get(1)[5]);
      
      assertEquals("groupAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertNull(sqlCacheGroups.get(2)[5]);
      
      assertEquals("members", sqlCacheGroups.get(3)[2]);
      assertEquals(2L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertNull(sqlCacheGroups.get(3)[5]);
      
      assertEquals("optins", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      assertNull(sqlCacheGroups.get(4)[5]);
      
      assertEquals("optouts", sqlCacheGroups.get(5)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(5)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[4], false).getTime());
      assertNull(sqlCacheGroups.get(5)[5]);
      
      assertEquals("readers", sqlCacheGroups.get(6)[2]);
      assertEquals(1L, GrouperUtil.longObjectValue(sqlCacheGroups.get(6)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[4], false).getTime());
      assertNull(sqlCacheGroups.get(6)[5]);
      
      assertEquals("updaters", sqlCacheGroups.get(7)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(7)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[4], false).getTime());
      assertNull(sqlCacheGroups.get(7)[5]);
      
      assertEquals("viewers", sqlCacheGroups.get(8)[2]);
      assertEquals(1L, GrouperUtil.longObjectValue(sqlCacheGroups.get(8)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(8)[4], false).getTime());
      assertNull(sqlCacheGroups.get(8)[5]);
    }
    
    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testGroup2.getInternalId()).selectList(Object[].class);
  
      assertEquals(9, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("admins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(0)[5]);
      }
      
      assertEquals("groupAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(1)[5]);
      }
      
      assertEquals("groupAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(2)[5]);
      }
      
      assertEquals("members", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(3)[5]);
      }
      
      assertEquals("optins", sqlCacheGroups.get(4)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(4)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(4)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(4)[5]);
      }
      
      assertEquals("optouts", sqlCacheGroups.get(5)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(5)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(5)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(5)[5]);
      }
      
      assertEquals("readers", sqlCacheGroups.get(6)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(6)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(6)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(6)[5]);
      }
      
      assertEquals("updaters", sqlCacheGroups.get(7)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(7)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(7)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(7)[5]);
      }
      
      assertEquals("viewers", sqlCacheGroups.get(8)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(8)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(8)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(8)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(8)[5]);
      }
    }
  }
  
  public void testEntities() {
    // we want to make the id index and internal id for groups out of sync to make sure we're saving the actual internal id.
    TableIndex.reserveIds(TableIndexType.groupInternalId, 10000);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    new StemSave().assignName("test").save();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // make sure nothing is out of sync to begin with
    runRullSync(true);

    long originalSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    Group testGroup = (Group)new EntitySave(GrouperSession.staticGrouperSession()).assignName("test:testEntity").assignCreateParentStemsIfNotExist(false).save();
    testGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ, true);
    testGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ, true);
    
    // try an entity that's deleted too
    Group testGroup2 = (Group)new EntitySave(GrouperSession.staticGrouperSession()).assignName("test:testEntity2").assignCreateParentStemsIfNotExist(false).save();
    testGroup2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ, true);
    testGroup2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ, true);
    testGroup2.delete();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    long newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);
    
    PITGroup pitTestGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(testGroup.getId(), true);
    PITGroup pitTestGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(testGroup2.getId(), true);
    
    // 4 access fields times two groups
    assertEquals(originalSize + 8, newSize);
    
    String sqlCacheGroupQuery = "select gscg.internal_id, gscg.group_internal_id, gf.name, gscg.membership_size, gscg.enabled_on, gscg.disabled_on from grouper_sql_cache_group gscg, grouper_fields gf where gscg.field_internal_id = gf.internal_id and gscg.group_internal_id = ? and gf.type in ('list','access') order by 2, 3";
    
    verifyInitialEntities(testGroup, testGroup2, pitTestGroup, pitTestGroup2, sqlCacheGroupQuery, true);
    
    // full sync should see no changes
    runRullSync(false);
    
    // make some changes and verify that it's all corrected by the full sync
    assertEquals(2, new GcDbAccess().sql("delete from grouper_sql_cache_mship where sql_cache_group_internal_id in (select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access')))").addBindVar(testGroup.getInternalId()).executeSql());
    assertEquals(4, new GcDbAccess().sql("delete from grouper_sql_cache_group where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(testGroup.getInternalId()).executeSql());
    assertEquals(4, new GcDbAccess().sql("update grouper_sql_cache_group set disabled_on = null where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(testGroup2.getInternalId()).executeSql());
    runRullSync(true);
    verifyInitialEntities(testGroup, testGroup2, pitTestGroup, pitTestGroup2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // make some more changes and verify that it's all corrected by the full sync
    assertEquals(4, new GcDbAccess().sql("update grouper_sql_cache_group set disabled_on = ? where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(new Timestamp(System.currentTimeMillis())).addBindVar(testGroup.getInternalId()).executeSql());
    runRullSync(true);
    verifyInitialEntities(testGroup, testGroup2, pitTestGroup, pitTestGroup2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // make some more changes and verify that it's all corrected by the full sync
    assertEquals(4, new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '999' where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(testGroup.getInternalId()).executeSql());
    assertEquals(4, new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '999' where group_internal_id = ? and field_internal_id in (select internal_id from grouper_fields gf where gf.type in ('list','access'))").addBindVar(testGroup2.getInternalId()).executeSql());
    runRullSync(true);
    verifyInitialEntities(testGroup, testGroup2, pitTestGroup, pitTestGroup2, sqlCacheGroupQuery, false);
    runRullSync(false);
    
    // adjust membership counts
    testGroup.revokePriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    testGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    testGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    testGroup.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    // doesn't change
    assertEquals(originalSize + 8, newSize);

    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testGroup.getInternalId()).selectList(Object[].class);
  
      assertEquals(4, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("admins", sqlCacheGroups.get(0)[2]);
      assertEquals(3L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertNull(sqlCacheGroups.get(0)[5]);
      
      assertEquals("groupAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(1L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertNull(sqlCacheGroups.get(1)[5]);
      
      assertEquals("groupAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertNull(sqlCacheGroups.get(2)[5]);
      
      assertEquals("viewers", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertNull(sqlCacheGroups.get(3)[5]);
    }
    
    // full sync should see no changes
    runRullSync(false);
    
    // delete the group
    testGroup.delete();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    newSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_group").select(Long.class);

    // doesn't change
    assertEquals(originalSize + 8, newSize);

    pitTestGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(testGroup.getId(), true);

    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testGroup.getInternalId()).selectList(Object[].class);
  
      assertEquals(4, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("admins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[5], false).getTime());
      
      assertEquals("groupAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[5], false).getTime());
      
      assertEquals("groupAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[5], false).getTime());
     
      assertEquals("viewers", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertEquals(pitTestGroup.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[5], false).getTime());
    }
    
    // full sync should see no changes
    runRullSync(false);
  }

  private void verifyInitialEntities(Group testGroup, Group testGroup2, PITGroup pitTestGroup, PITGroup pitTestGroup2, String sqlCacheGroupQuery, boolean verifyDisabledTime) {
    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testGroup.getInternalId()).selectList(Object[].class);
  
      assertEquals(4, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("admins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      assertNull(sqlCacheGroups.get(0)[5]);
      
      assertEquals("groupAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(2L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      assertNull(sqlCacheGroups.get(1)[5]);
      
      assertEquals("groupAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      assertNull(sqlCacheGroups.get(2)[5]);
      
      assertEquals("viewers", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestGroup.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      assertNull(sqlCacheGroups.get(3)[5]);
    }
    
    {
      List<Object[]> sqlCacheGroups = new GcDbAccess().sql(sqlCacheGroupQuery).addBindVar(testGroup2.getInternalId()).selectList(Object[].class);
  
      assertEquals(4, GrouperUtil.length(sqlCacheGroups));
      
      assertEquals("admins", sqlCacheGroups.get(0)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(0)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(0)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(0)[5]);
      }
      
      assertEquals("groupAttrReaders", sqlCacheGroups.get(1)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(1)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(1)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(1)[5]);
      }
      
      assertEquals("groupAttrUpdaters", sqlCacheGroups.get(2)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(2)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(2)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(2)[5]);
      }
      
      assertEquals("viewers", sqlCacheGroups.get(3)[2]);
      assertEquals(0L, GrouperUtil.longObjectValue(sqlCacheGroups.get(3)[3], false).longValue());
      assertEquals(pitTestGroup2.getStartTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[4], false).getTime());
      if (verifyDisabledTime) {
        assertEquals(pitTestGroup2.getEndTime().getTime(), GrouperUtil.timestampObjectValue(sqlCacheGroups.get(3)[5], false).getTime());
      } else {
        assertNotNull(sqlCacheGroups.get(3)[5]);
      }
    }
  }
  
  private Hib3GrouperLoaderLog runRullSync(boolean expectChanges) {
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);

    new SqlCacheFullSyncDaemon().run(otherJobInput);
    
    if (!expectChanges) {
      assertEquals(0, hib3GrouperLoaderLog.getInsertCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getDeleteCount().intValue());
    }
    
    return hib3GrouperLoaderLog;
  }
}
