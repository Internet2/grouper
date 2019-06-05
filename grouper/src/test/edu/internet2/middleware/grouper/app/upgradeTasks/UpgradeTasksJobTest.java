package edu.internet2.middleware.grouper.app.upgradeTasks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.exception.GroupSetNotFoundException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * 
 */
public class UpgradeTasksJobTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UpgradeTasksJobTest("testVersion1"));
  }
  
  /**
   * @param name
   */
  public UpgradeTasksJobTest(String name) {
    super(name);
  }
  

  /**
   * 
   */
  public void testVersion1() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Entity testEntity1 = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testEntity1").save();
    Entity testEntity2 = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testEntity2").save();
        
    GroupSet gs1 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrReaders", true).getId());
    GroupSet gs2 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
    GroupSet gs3 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrReaders", true).getId());
    GroupSet gs4 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
    
    gs1.delete(false);
    gs2.delete(false);
    gs3.delete(false);
    gs4.delete(false);
    
    ChangeLogTempToEntity.convertRecords();
    
    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrReaders", true).getId());
      fail("didn't throw exception");
    } catch (GroupSetNotFoundException e) {
      // good
    }

    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
      fail("didn't throw exception");
    } catch (GroupSetNotFoundException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrReaders", true).getId());
      fail("didn't throw exception");
    } catch (GroupSetNotFoundException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
      fail("didn't throw exception");
    } catch (GroupSetNotFoundException e) {
      // good
    }

    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs1.getId(), false).size(), 0);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs2.getId(), false).size(), 0);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs3.getId(), false).size(), 0);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs4.getId(), false).size(), 0);

    UpgradeTasksJob.runDaemonStandalone();
    
    gs1 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrReaders", true).getId());
    gs2 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
    gs3 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrReaders", true).getId());
    gs4 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
        
    assertNotNull(gs1);
    assertNotNull(gs2);
    assertNotNull(gs3);
    assertNotNull(gs4);

    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs1.getId(), false).size(), 1);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs2.getId(), false).size(), 1);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs3.getId(), false).size(), 1);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs4.getId(), false).size(), 1);
  }
}
