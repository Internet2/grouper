package edu.internet2.middleware.grouper.entity;

import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class EntityFinderTest extends GrouperTest {

  /**
   * 
   * @param name
   */
  public EntityFinderTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new EntityFinderTest("testFinder"));
  }

  /**
   * test the finder
   */
  public void testFinder() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Entity testEntity = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName("test:testEntity").save();
    Entity testEntity2 = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName("test:testEntity2").save();
    Entity testEntity3 = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName("test:tesvA:testEntity3").save();
    Entity testEntity4 = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName("test:tesvA:testEntity4").save();
    
    testEntity.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    testEntity3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    testEntity2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    testEntity4.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    
    List<Entity> entities = new ArrayList<Entity>(new EntityFinder().addParentFolderName("test").findEntities());
    
    assertEquals(2, GrouperUtil.length(entities));
    assertEquals("test:testEntity", entities.get(0).getName());
    assertEquals("test:testEntity2", entities.get(1).getName());

    entities = new ArrayList<Entity>(new EntityFinder().addName(testEntity.getName()).addName(testEntity2.getName()).findEntities());
    
    assertEquals(2, GrouperUtil.length(entities));
    assertEquals("test:testEntity", entities.get(0).getName());
    assertEquals("test:testEntity2", entities.get(1).getName());

    entities = new ArrayList<Entity>(new EntityFinder().addId(testEntity.getId()).addId(testEntity2.getId()).findEntities());
    
    assertEquals(2, GrouperUtil.length(entities));
    assertEquals("test:testEntity", entities.get(0).getName());
    assertEquals("test:testEntity2", entities.get(1).getName());

    entities = new ArrayList<Entity>(new EntityFinder().addId(testEntity.getId()).addName(testEntity.getName()).findEntities());
    
    assertEquals(1, GrouperUtil.length(entities));
    assertEquals("test:testEntity", entities.get(0).getName());

    
    Stem test = StemFinder.findByName(grouperSession, "test", true);
    Stem testA = StemFinder.findByName(grouperSession, "test:tesvA", true);

    entities = new ArrayList<Entity>(new EntityFinder().addParentFolderId(test.getUuid()).addParentFolderId(testA.getUuid()).findEntities());
    
    assertEquals(4, GrouperUtil.length(entities));
    assertEquals("test:testEntity", entities.get(0).getName());
    assertEquals("test:testEntity2", entities.get(1).getName());
    assertEquals("test:tesvA:testEntity3", entities.get(2).getName());
    assertEquals("test:tesvA:testEntity4", entities.get(3).getName());
    
    entities = new ArrayList<Entity>(new EntityFinder().addAncestorFolderId(test.getUuid()).findEntities());
    
    assertEquals(4, GrouperUtil.length(entities));
    assertEquals("test:testEntity", entities.get(0).getName());
    assertEquals("test:testEntity2", entities.get(1).getName());
    assertEquals("test:tesvA:testEntity3", entities.get(2).getName());
    assertEquals("test:tesvA:testEntity4", entities.get(3).getName());
    
    entities = new ArrayList<Entity>(new EntityFinder().addAncestorFolderName(test.getName()).findEntities());
    
    assertEquals(4, GrouperUtil.length(entities));
    assertEquals("test:testEntity", entities.get(0).getName());
    assertEquals("test:testEntity2", entities.get(1).getName());
    assertEquals("test:tesvA:testEntity3", entities.get(2).getName());
    assertEquals("test:tesvA:testEntity4", entities.get(3).getName());
    
    entities = new ArrayList<Entity>(new EntityFinder().assignTerms("test tesv").findEntities());
    
    assertEquals(2, GrouperUtil.length(entities));
    assertEquals("test:tesvA:testEntity3", entities.get(0).getName());
    assertEquals("test:tesvA:testEntity4", entities.get(1).getName());

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    entities = new ArrayList<Entity>(new EntityFinder().addAncestorFolderName(test.getName()).findEntities());
    
    assertEquals(2, GrouperUtil.length(entities));
    assertEquals("test:testEntity", entities.get(0).getName());
    assertEquals("test:tesvA:testEntity3", entities.get(1).getName());

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    entities = new ArrayList<Entity>(new EntityFinder().addAncestorFolderName(test.getName()).findEntities());
    
    assertEquals(2, GrouperUtil.length(entities));
    assertEquals("test:testEntity2", entities.get(0).getName());
    assertEquals("test:tesvA:testEntity4", entities.get(1).getName());

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    entities = new ArrayList<Entity>(new EntityFinder().addAncestorFolderName(test.getName()).findEntities());
    
    assertEquals(0, GrouperUtil.length(entities));

    GrouperSession.stopQuietly(grouperSession);

  }
  
}
