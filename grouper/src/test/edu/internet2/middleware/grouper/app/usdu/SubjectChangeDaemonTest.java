package edu.internet2.middleware.grouper.app.usdu;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import junit.textui.TestRunner;

public class SubjectChangeDaemonTest extends GrouperTest {

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new SubjectChangeDaemonTest("testSubjectIdentifierAndKeepProcessedRows"));
  }
  
  public SubjectChangeDaemonTest(String name) {
    super(name);
  }
  
  @Override
  protected void tearDown() {
    super.tearDown();
    
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();

        {
          Table table = database.findTable("testgrouper_subjectchg1");
          
          if (table != null) {
            database.removeTable(table);
          }
        }
        {
          Table table = database.findTable("testgrouper_subjectchg2");
          
          if (table != null) {
            database.removeTable(table);
          }
        }
      }
    });
  }
  
  @Override
  protected void setUp() {
    super.setUp();
   
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();

        {
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"testgrouper_subjectchg1");
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "id", 
              Types.BIGINT, "12", true, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "subject_string", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "create_timestamp", 
              Types.TIMESTAMP, null, false, false);
        }
        {
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"testgrouper_subjectchg2");
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "id", 
              Types.BIGINT, "12", true, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "subject_string", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "create_timestamp", 
              Types.TIMESTAMP, null, false, false);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "processed_timestamp", 
              Types.TIMESTAMP, null, false, false);
        }
      }
    });
  }
  
  private void setUpDaemonConfig(boolean deleteProcessedRows, boolean useSubjectId) {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.subjectSourceId", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.database", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.columnPrimaryKey", "id");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.columnCreateTimestamp", "create_timestamp");
    
    if (deleteProcessedRows) {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.deleteProcessedRows", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.table", "testgrouper_subjectchg1");
    } else {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.columnProcessedTimestamp", "processed_timestamp");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.table", "testgrouper_subjectchg2");
    }
    
    if (useSubjectId) {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.useSubjectIdOrIdentifier", "subjectId");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.columnSubjectId", "subject_string");
    } else {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.useSubjectIdOrIdentifier", "subjectIdentifier");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.mySubjectChangeId.subjectChangeDaemon.columnSubjectIdentifier", "subject_string");
    }
  }
  
  public void testSubjectIdAndDeleteProcessedRows() {
    setUpDaemonConfig(true, true);
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:group").assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectFinder.findById("test.subject.0", true));
    group.addMember(SubjectFinder.findById("test.subject.1", true));
    group.addMember(SubjectFinder.findById("test.subject.2", true));
    group.addMember(SubjectFinder.findById("test.subject.3", true));
    group.addMember(SubjectFinder.findById("test.subject.4", true));
    group.addMember(SubjectFinder.findById("test.subject.5", true));

    // two will be unresolvable
    Subject subj6 = SubjectFinder.findById("test.subject.6", true);
    Subject subj7 = SubjectFinder.findById("test.subject.7", true);
    group.addMember(subj6);
    group.addMember(subj7);

    // one before usdu
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(0).addBindVar("test.subject.0").addBindVar(new Date()).executeSql();
    
    {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // ignore
      }
      
      // fake usdu run
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobName("OTHER_JOB_usduDaemon");
      hib3GrouperLoaderLog.setStatus("SUCCESS");
      hib3GrouperLoaderLog.setStartedTime(new Timestamp(new Date().getTime()));
      hib3GrouperLoaderLog.store();
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // ignore
      }
    }
    
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(1).addBindVar("test.subject.1").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(2).addBindVar("test.subject.2").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(3).addBindVar("test.subject.3").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(4).addBindVar("test.subject.4").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(5).addBindVar("test.subject.5").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(6).addBindVar("test.subject.6").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(7).addBindVar("test.subject.7").addBindVar(new Date()).executeSql();
    
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.0' where subject_id='test.subject.0'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.1' where subject_id='test.subject.1'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.2' where subject_id='test.subject.2'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.3' where subject_id='test.subject.3'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.4' where subject_id='test.subject.4'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.5' where subject_id='test.subject.5'").executeSql();
    deleteSubject(subj6);
    deleteSubject(subj7);
    Hib3MemberDAO.membersCacheClear();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    otherJobInput.setJobName("OTHER_JOB_mySubjectChangeId");
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    new SubjectChangeDaemon().run(otherJobInput);
    
    assertEquals(8, (int)hib3GrouperLoaderLog.getTotalCount());
    assertEquals(5, (int)hib3GrouperLoaderLog.getUpdateCount());
    assertEquals(0, (int)hib3GrouperLoaderLog.getDeleteCount());
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    
    assertEquals("x-id.test.subject.0", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.0'").select(String.class));
    assertEquals("id.test.subject.1", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.1'").select(String.class));
    assertEquals("id.test.subject.2", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.2'").select(String.class));
    assertEquals("id.test.subject.3", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.3'").select(String.class));
    assertEquals("id.test.subject.4", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.4'").select(String.class));
    assertEquals("id.test.subject.5", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.5'").select(String.class));
    
    assertTrue(group.hasMember(subj6));
    assertTrue(group.hasMember(subj7));
    Member member6 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj6, false);
    Member member7 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj7, false);
    assertFalse(member6.isSubjectResolutionDeleted());
    assertFalse(member7.isSubjectResolutionDeleted());
    assertFalse(member6.isSubjectResolutionResolvable());
    assertFalse(member7.isSubjectResolutionResolvable());
    
    assertEquals(0, (int)new GcDbAccess().sql("select count(*) from testgrouper_subjectchg1").select(int.class));
  }
  
  public void testSubjectIdentifierAndKeepProcessedRows() {
    setUpDaemonConfig(false, false);
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:group").assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectFinder.findById("test.subject.0", true));
    group.addMember(SubjectFinder.findById("test.subject.1", true));
    group.addMember(SubjectFinder.findById("test.subject.2", true));
    group.addMember(SubjectFinder.findById("test.subject.3", true));
    group.addMember(SubjectFinder.findById("test.subject.4", true));
    group.addMember(SubjectFinder.findById("test.subject.5", true));

    // two will be unresolvable
    Subject subj6 = SubjectFinder.findById("test.subject.6", true);
    Subject subj7 = SubjectFinder.findById("test.subject.7", true);
    group.addMember(subj6);
    group.addMember(subj7);

    // one before usdu
    new GcDbAccess().sql("insert into testgrouper_subjectchg2 values (?, ?, ?)").addBindVar(0).addBindVar("id.test.subject.0").addBindVar(new Date()).executeSql();
    
    {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // ignore
      }
      
      // fake usdu run
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobName("OTHER_JOB_usduDaemon");
      hib3GrouperLoaderLog.setStatus("SUCCESS");
      hib3GrouperLoaderLog.setStartedTime(new Timestamp(new Date().getTime()));
      hib3GrouperLoaderLog.store();
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // ignore
      }
    }
    
    new GcDbAccess().sql("insert into testgrouper_subjectchg2 values (?, ?, ?)").addBindVar(1).addBindVar("id.test.subject.1").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg2 values (?, ?, ?)").addBindVar(2).addBindVar("id.test.subject.2").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg2 values (?, ?, ?)").addBindVar(3).addBindVar("id.test.subject.3").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg2 values (?, ?, ?)").addBindVar(4).addBindVar("id.test.subject.4").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg2 values (?, ?, ?)").addBindVar(5).addBindVar("id.test.subject.5").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg2 values (?, ?, ?)").addBindVar(6).addBindVar("id.test.subject.6").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg2 values (?, ?, ?)").addBindVar(7).addBindVar("id.test.subject.7").addBindVar(new Date()).executeSql();
    
    new GcDbAccess().sql("update grouper_members set description='x-description.test.subject.0' where subject_id='test.subject.0'").executeSql();
    new GcDbAccess().sql("update grouper_members set description='x-description.test.subject.1' where subject_id='test.subject.1'").executeSql();
    new GcDbAccess().sql("update grouper_members set description='x-description.test.subject.2' where subject_id='test.subject.2'").executeSql();
    new GcDbAccess().sql("update grouper_members set description='x-description.test.subject.3' where subject_id='test.subject.3'").executeSql();
    new GcDbAccess().sql("update grouper_members set description='x-description.test.subject.4' where subject_id='test.subject.4'").executeSql();
    new GcDbAccess().sql("update grouper_members set description='x-description.test.subject.5' where subject_id='test.subject.5'").executeSql();
    deleteSubject(subj6);
    deleteSubject(subj7);
    Hib3MemberDAO.membersCacheClear();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    otherJobInput.setJobName("OTHER_JOB_mySubjectChangeId");
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    new SubjectChangeDaemon().run(otherJobInput);
    
    assertEquals(8, (int)hib3GrouperLoaderLog.getTotalCount());
    assertEquals(5, (int)hib3GrouperLoaderLog.getUpdateCount());
    assertEquals(0, (int)hib3GrouperLoaderLog.getDeleteCount());
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    
    assertEquals("x-description.test.subject.0", new GcDbAccess().sql("select description from grouper_members where subject_id='test.subject.0'").select(String.class));
    assertEquals("description.test.subject.1", new GcDbAccess().sql("select description from grouper_members where subject_id='test.subject.1'").select(String.class));
    assertEquals("description.test.subject.2", new GcDbAccess().sql("select description from grouper_members where subject_id='test.subject.2'").select(String.class));
    assertEquals("description.test.subject.3", new GcDbAccess().sql("select description from grouper_members where subject_id='test.subject.3'").select(String.class));
    assertEquals("description.test.subject.4", new GcDbAccess().sql("select description from grouper_members where subject_id='test.subject.4'").select(String.class));
    assertEquals("description.test.subject.5", new GcDbAccess().sql("select description from grouper_members where subject_id='test.subject.5'").select(String.class));
    
    assertTrue(group.hasMember(subj6));
    assertTrue(group.hasMember(subj7));
    Member member6 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj6, false);
    Member member7 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj7, false);
    assertFalse(member6.isSubjectResolutionDeleted());
    assertFalse(member7.isSubjectResolutionDeleted());
    assertFalse(member6.isSubjectResolutionResolvable());
    assertFalse(member7.isSubjectResolutionResolvable());
    
    assertEquals(8, (int)new GcDbAccess().sql("select count(*) from testgrouper_subjectchg2").select(int.class));
    assertEquals(0, (int)new GcDbAccess().sql("select count(*) from testgrouper_subjectchg2 where processed_timestamp is null").select(int.class));
  }
  
  public void testUsduDeleteImmediately() {
    setUpDaemonConfig(true, true);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("usdu.delete.ifAfterDays", "-1");

    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:group").assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectFinder.findById("test.subject.0", true));
    group.addMember(SubjectFinder.findById("test.subject.1", true));
    group.addMember(SubjectFinder.findById("test.subject.2", true));
    group.addMember(SubjectFinder.findById("test.subject.3", true));
    group.addMember(SubjectFinder.findById("test.subject.4", true));
    group.addMember(SubjectFinder.findById("test.subject.5", true));

    // two will be unresolvable
    Subject subj6 = SubjectFinder.findById("test.subject.6", true);
    Subject subj7 = SubjectFinder.findById("test.subject.7", true);
    group.addMember(subj6);
    group.addMember(subj7);

    // one before usdu
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(0).addBindVar("test.subject.0").addBindVar(new Date()).executeSql();
    
    {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // ignore
      }
      
      // fake usdu run
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobName("OTHER_JOB_usduDaemon");
      hib3GrouperLoaderLog.setStatus("SUCCESS");
      hib3GrouperLoaderLog.setStartedTime(new Timestamp(new Date().getTime()));
      hib3GrouperLoaderLog.store();
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // ignore
      }
    }
    
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(1).addBindVar("test.subject.1").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(2).addBindVar("test.subject.2").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(3).addBindVar("test.subject.3").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(4).addBindVar("test.subject.4").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(5).addBindVar("test.subject.5").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(6).addBindVar("test.subject.6").addBindVar(new Date()).executeSql();
    new GcDbAccess().sql("insert into testgrouper_subjectchg1 values (?, ?, ?)").addBindVar(7).addBindVar("test.subject.7").addBindVar(new Date()).executeSql();
    
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.0' where subject_id='test.subject.0'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.1' where subject_id='test.subject.1'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.2' where subject_id='test.subject.2'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.3' where subject_id='test.subject.3'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.4' where subject_id='test.subject.4'").executeSql();
    new GcDbAccess().sql("update grouper_members set subject_identifier0='x-id.test.subject.5' where subject_id='test.subject.5'").executeSql();
    deleteSubject(subj6);
    deleteSubject(subj7);
    Hib3MemberDAO.membersCacheClear();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    otherJobInput.setJobName("OTHER_JOB_mySubjectChangeId");
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    new SubjectChangeDaemon().run(otherJobInput);
    
    assertEquals(8, (int)hib3GrouperLoaderLog.getTotalCount());
    assertEquals(5, (int)hib3GrouperLoaderLog.getUpdateCount());
    assertEquals(2, (int)hib3GrouperLoaderLog.getDeleteCount());
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    
    assertEquals("x-id.test.subject.0", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.0'").select(String.class));
    assertEquals("id.test.subject.1", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.1'").select(String.class));
    assertEquals("id.test.subject.2", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.2'").select(String.class));
    assertEquals("id.test.subject.3", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.3'").select(String.class));
    assertEquals("id.test.subject.4", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.4'").select(String.class));
    assertEquals("id.test.subject.5", new GcDbAccess().sql("select subject_identifier0 from grouper_members where subject_id='test.subject.5'").select(String.class));
    
    assertFalse(group.hasMember(subj6));
    assertFalse(group.hasMember(subj7));
    Member member6 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj6, false);
    Member member7 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj7, false);
    assertTrue(member6.isSubjectResolutionDeleted());
    assertTrue(member7.isSubjectResolutionDeleted());
    assertFalse(member6.isSubjectResolutionResolvable());
    assertFalse(member7.isSubjectResolutionResolvable());
    
    assertEquals(0, (int)new GcDbAccess().sql("select count(*) from testgrouper_subjectchg1").select(int.class));
  }
  
  private void deleteSubject(Subject subject) {
    
    List<RegistrySubject> registrySubjects = HibernateSession.byCriteriaStatic()
      .list(RegistrySubject.class, Restrictions.eq("id", subject.getId()));

    for (RegistrySubject registrySubject : registrySubjects) {
      registrySubject.delete(GrouperSession.staticGrouperSession());
    }

    SubjectFinder.flushCache();

    try {
      SubjectFinder.findById(subject.getId(), true);
      fail("should not find subject " + subject.getId());
    } catch (SubjectNotFoundException e) {
      // OK
    } catch (SubjectNotUniqueException e) {
      fail("subject should be unique " + subject.getId());
    }
  }
}