/**
 * Copyright 2020 Internet2
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
 */
package edu.internet2.middleware.grouper.app.usdu;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;

public class UsduJobProvisionerSyncTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();    
  }
  
  @Override
  protected void tearDown() {
    super.tearDown();
    
    // this should be done by the next test but just in case
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().clear();
  }

  public void test() {
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignCreateParentStemsIfNotExist(true).assignName("test:group1").save();
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ2);
    group.addMember(SubjectTestHelper.SUBJ3);
    
    Member member0 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, false);
    Member member2 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, false);
    Member member3 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, false);
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test1");
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(member0.getId());
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(member1.getId());
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId("bogus");
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test2");
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(member1.getId());
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(member2.getId());
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId("bogus");
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();

    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test3");
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(member2.getId());
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(member3.getId());
    gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId("bogus");
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberFromId2", "test1 ${subject.name} fromid2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberFromId3", "test1 ${subject.name} fromid3");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberToId2", "test1 ${subject.name} toid2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberToId3", "test1 ${subject.name} toid3");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test2.common.enabled", "false");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test3.common.subjectLink.memberFromId2", "test3 ${subject.name} fromid2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test3.common.subjectLink.memberFromId3", "test3 ${subject.name} fromid3");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test3.common.subjectLink.autoMemberFromId3", "false");

    UsduJob.runDaemonStandalone();
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test1");
    assertEquals("test1 my name is test.subject.0 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.0 fromid3", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.0 toid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.0 toid3", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId3());
    assertEquals("test1 my name is test.subject.1 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.1 fromid3", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.1 toid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.1 toid3", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());

    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test2");
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test3");
    assertEquals("test3 my name is test.subject.2 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    assertEquals("test3 my name is test.subject.3 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId3());
    
    // now test an update for test1
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberFromId2", "test1 ${subject.name} fromid2 update");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberFromId3", "test1 ${subject.name} fromid3 update");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberToId2", "test1 ${subject.name} toid2 update");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberToId3", "test1 ${subject.name} toid3 update");
    
    UsduJob.runDaemonStandalone();
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test1");
    assertEquals("test1 my name is test.subject.0 fromid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.0 fromid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.0 toid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.0 toid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId3());
    assertEquals("test1 my name is test.subject.1 fromid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.1 fromid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.1 toid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.1 toid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());

    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test2");
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test3");
    assertEquals("test3 my name is test.subject.2 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    assertEquals("test3 my name is test.subject.3 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId3());
    
    // disable sync for test1
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.autoMemberFromId2", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.autoMemberFromId3", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.autoMemberToId2", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.autoMemberToId3", "false");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberFromId2", "test1 ${subject.name} fromid2 update2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberFromId3", "test1 ${subject.name} fromid3 update2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberToId2", "test1 ${subject.name} toid2 update2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.memberToId3", "test1 ${subject.name} toid3 update2");    
    
    UsduJob.runDaemonStandalone();
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test1");
    assertEquals("test1 my name is test.subject.0 fromid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.0 fromid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.0 toid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.0 toid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId3());
    assertEquals("test1 my name is test.subject.1 fromid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.1 fromid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.1 toid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.1 toid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());

    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test2");
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test3");
    assertEquals("test3 my name is test.subject.2 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    assertEquals("test3 my name is test.subject.3 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId3());
    
    // disable provisioner for test1
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.enabled", "false");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.autoMemberFromId2", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.subjectLink.autoMemberToId2", "true");

    UsduJob.runDaemonStandalone();
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test1");
    assertEquals("test1 my name is test.subject.0 fromid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.0 fromid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.0 toid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.0 toid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId3());
    assertEquals("test1 my name is test.subject.1 fromid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.1 fromid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.1 toid2 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.1 toid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());

    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test2");
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test3");
    assertEquals("test3 my name is test.subject.2 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    assertEquals("test3 my name is test.subject.3 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId3());
    
    // enable provisioner for test1
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.test1.common.enabled", "true");
    
    UsduJob.runDaemonStandalone();
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test1");
    assertEquals("test1 my name is test.subject.0 fromid2 update2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.0 fromid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.0 toid2 update2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.0 toid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId()).getMemberToId3());
    assertEquals("test1 my name is test.subject.1 fromid2 update2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertEquals("test1 my name is test.subject.1 fromid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertEquals("test1 my name is test.subject.1 toid2 update2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertEquals("test1 my name is test.subject.1 toid3 update", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());    

    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test2");
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId()).getMemberToId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "test3");
    assertEquals("test3 my name is test.subject.2 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId()).getMemberToId3());
    assertEquals("test3 my name is test.subject.3 fromid2", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberFromId3());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId2());
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId()).getMemberToId3());
  }
}