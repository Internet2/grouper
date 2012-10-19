/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

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
    TestRunner.run(new EntityFinderTest("testSubjectFinder"));
  }

  /**
   * test the finder
   */
  public void testEntityAudits() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    new StemSave(grouperSession).assignName("test").save();
    
    //lets see whats in the audit log
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    assertEquals(0, auditCount);
    
    Entity testEntity = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName("test:testEntity").save();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", 1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertEquals("entity", auditEntry.getAuditType().getAuditCategory());
    assertEquals("addEntity", auditEntry.getAuditType().getActionName());

    //lets see whats in the audit log
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
    
    testEntity.setDescription("something else");
    testEntity.store();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly one audit", 1, newAuditCount);
    
    auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertEquals("entity", auditEntry.getAuditType().getAuditCategory());
    assertEquals("updateEntity", auditEntry.getAuditType().getActionName());

    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
    testEntity.delete();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly one audit", 1, newAuditCount);
    
    auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertEquals("entity", auditEntry.getAuditType().getAuditCategory());
    assertEquals("deleteEntity", auditEntry.getAuditType().getActionName());
    
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

  
  /**
   * test the finder by id
   */
  public void testFinderByName() {
    
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

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //lets assign an entity id... note this should be able to be done by any admin of the entity
    try {
      
      testEntity.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "test:some/weird:id");
      
      fail("Viewers cant assign an entity subject id");
    } catch (Exception e) {
      //good
    }

    GrouperSession.stopQuietly(grouperSession);
    
    //admins can assign the attribute
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    testEntity2.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "test:some/weird:id2");

    List<Object[]> groupAttributeAssignValues = new EntityFinder().addName("test:some/weird:id2").findEntitiesAndSubjectIdentifier();

    assertEquals(1, GrouperUtil.length(groupAttributeAssignValues));
    assertEquals(testEntity2.getName() , ((Entity)groupAttributeAssignValues.get(0)[0]).getName());
    assertEquals("test:some/weird:id2" , groupAttributeAssignValues.get(0)[1]);
    
    
    Entity entity = new EntityFinder().addName("test:some/weird:id2").findEntity(true);

    assertNotNull(entity);
    
    assertEquals(testEntity2.getName(), entity.getName());
    
    //search by term in identifier, should find
    entity = new EntityFinder().assignTerms("me/we id2").findEntity(true);
    
    assertNotNull(entity);
    
    assertEquals(testEntity2.getName(), entity.getName());
    
    GrouperSession.stopQuietly(grouperSession);

    
//    assertEquals(2, GrouperUtil.length(entities));
//    assertEquals("test:testEntity", entities.get(0).getName());
//    assertEquals("test:testEntity2", entities.get(1).getName());

  }

  /**
   * test the finder by id
   */
  public void testSubjectFinder() {
    
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

    testEntity2.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "test:some/weird:id2");

    //try subjectfinder from g:gsa
    
    Subject subject = SubjectFinder.findByIdAndSource(testEntity.getId(), "g:gsa",  false);
    
    assertNull(subject);
    
    subject = SubjectFinder.findByIdAndSource(testEntity.getId(), "grouperEntities",  false);
    
    assertNotNull(subject);
    
    subject = SubjectFinder.findByIdentifierAndSource(testEntity.getName(), "g:gsa",  false);
    
    assertNull(subject); 
    
    subject = SubjectFinder.findByIdentifierAndSource(testEntity.getName(), "grouperEntities",  false);
    
    assertNotNull(subject);
    
    Set<Subject> subjects = SubjectFinder.findAll("st:testE", "g:gsa");
    
    assertEquals(0, GrouperUtil.length(subjects)); 
    
    subjects = SubjectFinder.findAll("st:testE", "grouperEntities");
    
    assertEquals(2, GrouperUtil.length(subjects)); 
    
    subjects = SubjectFinder.findAll("some/weird", "g:gsa");
    
    assertEquals(0, GrouperUtil.length(subjects)); 
    
    subjects = SubjectFinder.findAll("some/weird", "grouperEntities");
    
    assertEquals(1, GrouperUtil.length(subjects)); 
    
    subject = SubjectFinder.findByIdentifierAndSource("test:some/weird:id2", "g:gsa", false);
    
    assertNull(subject); 
    
    subject = SubjectFinder.findByIdentifierAndSource("test:some/weird:id2", "grouperEntities", false);
    
    assertNotNull(subject); 
    
    subject = subjects.iterator().next();
    
    assertEquals("test:some/weird:id2", subject.getAttributeValue("entityIdAttribute"));
    assertEquals("test:some/weird:id2", subject.getAttributeValue("entityId"));
    assertEquals("some/weird:id2", subject.getAttributeValue("entityExtension"));

    try {
      testEntity3.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "test:some/weird:id2");
      fail("shouldnt get here");
    } catch (Exception e) {
      //ignore
    }
    
    testEntity3 = new EntityFinder().addId(testEntity3.getId()).findEntity(true);

    //lets set the entity id to something not in the folder
    
    try {
      testEntity4.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "testa:some/weird:id2");
      fail("shouldnt get here");
    } catch (Exception e) {
      //ignore
    }
    
    testEntity4.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "test:tesvA:some/weird4:id4");
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //admins can assign the attribute
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);


    
    GrouperSession.stopQuietly(grouperSession);

    
//    assertEquals(2, GrouperUtil.length(entities));
//    assertEquals("test:testEntity", entities.get(0).getName());
//    assertEquals("test:testEntity2", entities.get(1).getName());

  }

  /**
   * test the finder
   */
  public void testFinderNotGroup() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName("test:testGroup").save();
    
    Subject subject = SubjectFinder.findByIdOrIdentifier("test:testGroup", true);
    
    assertEquals(SubjectFinder.internal_getGSA().getId(), subject.getSourceId());
    
  }

  
}
