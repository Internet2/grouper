/**
 * Copyright 2014 Internet2
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
/**
 * 
 */
package edu.internet2.middleware.grouper.tableIndex;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefTest;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3TableIndexDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public class TableIndexTest extends GrouperTest {

  /**
   * 
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.tableIndex.groupWhoCanAssignIdIndex", "etc:canAssignIdIndex");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("etc:canAssignIdIndex").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TableIndexTest("testSaveAttributeDefName"));
  }

  /**
   * 
   */
  public TableIndexTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public TableIndexTest(String name) {
    super(name);
  }

  public void testSaveGroup() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    for (int i=0;i<92;i++) {
      Group group = new GroupSave(grouperSession).assignName("test:someName" 
          + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
          .assignCreateParentStemsIfNotExist(true).save();
      Long idIndex = group.getIdIndex();
      assertNotNull(idIndex);
    }
    
    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    Group group = new GroupSave(grouperSession).assignName("test:someName" 
        + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
        .assignCreateParentStemsIfNotExist(true).save();
    Long idIndex = group.getIdIndex();
    assertNotNull(idIndex);
    
    
    GrouperSession.stopQuietly(grouperSession);
    
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    try {
      group = new GroupSave(grouperSession).assignName("test:someName" 
          + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
          .assignCreateParentStemsIfNotExist(true).save();
      fail("Shouldnt get here");
    } catch (RuntimeException e) {
      //good
    }
    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }
  
  /**
   * 
   */
  public void testHibernate() {
    
    TableIndex tableIndex = GrouperDAOFactory.getFactory().getTableIndex().findByType(TableIndexType.group);
    
    if (tableIndex != null) {
      GrouperDAOFactory.getFactory().getTableIndex().delete(tableIndex);
    }
    
    tableIndex = new TableIndex();
    tableIndex.setType(TableIndexType.group);
    tableIndex.setId(GrouperUuid.getUuid());
    tableIndex.setLastIndexReserved(10);
    tableIndex.saveOrUpdate();
  }
  

  /**
   * 
   */
  public void testIndexesGroup() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    long originalNumberOfTimesIndexesReserved = Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes;
    Set<Long> idIndexes = new HashSet<Long>();
    for (int i=0;i<92;i++) {
      Group group = new GroupSave(grouperSession).assignName("test:someName" + GrouperUtil.uniqueId()).assignCreateParentStemsIfNotExist(true).save();
      Long idIndex = group.getIdIndex();
      assertNotNull(idIndex);
      assertFalse(idIndexes.contains(idIndex));
      idIndexes.add(idIndex);
    }
    assertEquals(originalNumberOfTimesIndexesReserved+9, Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes);
  }

  /**
   * 
   */
  public void testIndexesStem() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    long originalNumberOfTimesIndexesReserved = Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes;
    Set<Long> idIndexes = new HashSet<Long>();
    for (int i=0;i<92;i++) {
      Stem stem = new StemSave(grouperSession).assignName("test:someName" + GrouperUtil.uniqueId()).assignCreateParentStemsIfNotExist(true).save();
      Long idIndex = stem.getIdIndex();
      assertNotNull(idIndex);
      assertFalse(idIndexes.contains(idIndex));
      idIndexes.add(idIndex);
    }
    assertTrue(9 <= Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes - originalNumberOfTimesIndexesReserved);
    assertTrue(11 >= Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes - originalNumberOfTimesIndexesReserved);
  }

  /**
   * 
   */
  public void testIndexesAttributeDef() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    long originalNumberOfTimesIndexesReserved = Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes;
    Set<Long> idIndexes = new HashSet<Long>();
    for (int i=0;i<92;i++) {
      AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("test:someName" + GrouperUtil.uniqueId()).assignCreateParentStemsIfNotExist(true).save();
      Long idIndex = attributeDef.getIdIndex();
      assertNotNull(idIndex);
      assertFalse(idIndexes.contains(idIndex));
      idIndexes.add(idIndex);
    }
    assertTrue(9 <= Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes - originalNumberOfTimesIndexesReserved);
    assertTrue(11 >= Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes - originalNumberOfTimesIndexesReserved);
  }

  /**
   * 
   */
  public void testIndexesAttributeDefName() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    long originalNumberOfTimesIndexesReserved = Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes;
    Set<Long> idIndexes = new HashSet<Long>();
    AttributeDef attributeDef = AttributeDefTest.exampleAttributeDefDb();
    attributeDef.store();
    for (int i=0;i<92;i++) {
      AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:someName" + GrouperUtil.uniqueId()).assignCreateParentStemsIfNotExist(true).save();
      Long idIndex = attributeDefName.getIdIndex();
      assertNotNull(idIndex);
      assertFalse(idIndexes.contains(idIndex));
      idIndexes.add(idIndex);
    }
    assertTrue(9 <= Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes - originalNumberOfTimesIndexesReserved);
    assertTrue(11 >= Hib3TableIndexDAO.testingNumberOfTimesReservedIndexes - originalNumberOfTimesIndexesReserved);
  }

  /**
   * 
   */
  public void testSaveStem() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    for (int i=0;i<92;i++) {
      Stem folder = new StemSave(grouperSession).assignName("test:someName" 
          + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
          .assignCreateParentStemsIfNotExist(true).save();
      Long idIndex = folder.getIdIndex();
      assertNotNull(idIndex);
    }
    
    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    Stem folder = new StemSave(grouperSession).assignName("test:someName" 
        + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
        .assignCreateParentStemsIfNotExist(true).save();
    Long idIndex = folder.getIdIndex();
    assertNotNull(idIndex);
    
    
    GrouperSession.stopQuietly(grouperSession);
    
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    try {
      folder = new StemSave(grouperSession).assignName("test:someName" 
          + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
          .assignCreateParentStemsIfNotExist(true).save();
      fail("Shouldnt get here");
    } catch (RuntimeException e) {
      //good
    }
    
    GrouperSession.stopQuietly(grouperSession);
    

  }
  
  /**
   * 
   */
  public void testSaveAttributeDef() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    for (int i=0;i<92;i++) {
      AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("test:someName" 
          + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
          .assignCreateParentStemsIfNotExist(true).save();
      Long idIndex = attributeDef.getIdIndex();
      assertNotNull(idIndex);
    }
    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("test:someName" 
        + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
        .assignCreateParentStemsIfNotExist(true).save();
    Long idIndex = attributeDef.getIdIndex();
    assertNotNull(idIndex);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    try {
      attributeDef = new AttributeDefSave(grouperSession).assignName("test:someName" 
          + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
          .assignCreateParentStemsIfNotExist(true).save();
      fail("Shouldnt get here");
    } catch (RuntimeException e) {
      //good
    }
    
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testSaveAttributeDefName() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef attributeDef = AttributeDefTest.exampleAttributeDefDb();
    for (int i=0;i<92;i++) {
      AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:someName" 
          + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
          .assignCreateParentStemsIfNotExist(true).save();
      Long idIndex = attributeDefName.getIdIndex();
      assertNotNull(idIndex);
    }
    
    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:someName" 
        + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
        .assignCreateParentStemsIfNotExist(true).save();
    Long idIndex = attributeDefName.getIdIndex();
    assertNotNull(idIndex);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    try {
      attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:someName" 
          + GrouperUtil.uniqueId()).assignIdIndex((long)new Random().nextInt(100000) + 10000)
          .assignCreateParentStemsIfNotExist(true).save();
      fail("Shouldnt get here");
    } catch (RuntimeException e) {
      //good
    }
    
    GrouperSession.stopQuietly(grouperSession);

  }
  

}
