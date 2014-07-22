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
/*
 * @author mchyzer
 * $Id: AuditEntryTest.java,v 1.3 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;


/**
 *
 */
public class AuditEntryTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AuditEntryTest("testFindByActingUserId"));
  }

  /**
   * @param name
   */
  public AuditEntryTest(String name) {
    super(name);
    
  }

  /**
   * test
   */
  public void testFindByActingUserId() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    new GroupSave(grouperSession).assignName("test:testGroup").save();
    new GroupSave(grouperSession).assignName("test:testGroup2").save();

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.startRootSession();

    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    
    Set<AuditEntry> auditEntries = GrouperDAOFactory.getFactory().getAuditEntry().findByActingUser(member.getUuid(), null);
    
    
    
    
  }
  
  /**
   * 
   */
  public void testLength() {
    //clear out
    AuditTypeDAO auditTypeDao = GrouperDAOFactory.getFactory().getAuditType();
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
    
    final AuditType auditType = new AuditType("a", "b", null, "s1", "s2");
    auditType.setId(GrouperUuid.getUuid());
        
    auditTypeDao.saveOrUpdate(auditType);
    
    //update and save again
    auditType.setLabelString03("s3");
    auditTypeDao.saveOrUpdate(auditType);

    AuditEntry auditEntry = new AuditEntry();
    auditEntry.setAuditTypeId(auditType.getId());
    auditEntry.setDescription("whatever");
    auditEntry.setId(GrouperUuid.getUuid());
    auditEntry.setString01("something");
    
    auditEntry.setDurationMicroseconds(1000000000000l);
    
    AuditEntryDAO auditEntryDao = GrouperDAOFactory.getFactory().getAuditEntry();
    auditEntryDao.saveOrUpdate(auditEntry);
    
    //edit and save again
    auditEntry.setEnvName("hey");
    auditEntryDao.saveOrUpdate(auditEntry);
    
    
    //clear out
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
  }

  /**
   * 
   */
  public void testWhatever() {
    
  }
  
  /**
   * make an example audit entry for testing
   * @return an example audit entry
   */
  public static AuditEntry exampleAuditEntry() {
    AuditEntry auditEntry = new AuditEntry();
    auditEntry.setActAsMemberId("actAsMemberId");
    auditEntry.setAuditTypeId("auditTypeId");
    auditEntry.setContextId("contextId");
    auditEntry.setCreatedOnDb(3L);
    auditEntry.setDescription("description");
    auditEntry.setDurationMicroseconds(4L);
    auditEntry.setEnvName("envName");
    auditEntry.setGrouperEngine("grouperEngine");
    auditEntry.setGrouperVersion("grouperVersion");
    auditEntry.setHibernateVersionNumber(5L);
    auditEntry.setId("id");
    auditEntry.setInt01(11L);
    auditEntry.setInt02(12L);
    auditEntry.setInt03(13L);
    auditEntry.setInt04(14L);
    auditEntry.setInt05(15L);
    auditEntry.setLastUpdatedDb(6L);
    auditEntry.setLoggedInMemberId("loggedInMemberId");
    auditEntry.setQueryCount(7);
    auditEntry.setServerHost("serverHost");
    auditEntry.setServerUserName("serverUserName");
    auditEntry.setString01("string01");
    auditEntry.setString02("string02");
    auditEntry.setString03("string03");
    auditEntry.setString04("string04");
    auditEntry.setString05("string05");
    auditEntry.setString06("string06");
    auditEntry.setString07("string07");
    auditEntry.setString08("string08");
    auditEntry.setUserIpAddress("userIpAddress");
    return auditEntry;
  }
  
  /**
   * make an example auditentry from db for testing
   * @return an example audit entry
   */
  public static AuditEntry exampleAuditEntryDb() {
    return exampleAuditEntryDb("theId");
  }

  
  /**
   * retrieve example audit entry from db for testing
   * @return an example audit entry
   */
  public static AuditEntry exampleRetrieveAuditEntryDb() {
    return exampleRetrieveAuditEntryDb("theId", true);
  }

  /**
   * retrieve example audit entry from db for testing
   * @param id 
   * @param exceptionIfNull 
   * @return an example audit entry
   */
  public static AuditEntry exampleRetrieveAuditEntryDb(String id, boolean exceptionIfNull) {
    AuditType auditType = AuditTypeFinder.find(AuditTypeBuiltin.GROUP_ADD.getAuditCategory(), 
        AuditTypeBuiltin.GROUP_ADD.getActionName(), true);
    AuditEntry auditEntry = HibernateSession.byHqlStatic().createQuery("from AuditEntry as theAuditEntry " +
    		"where theAuditEntry.auditTypeId = :theAuditTypeId and theAuditEntry.string01 = :theId ")
    		.setString("theAuditTypeId", auditType.getId())
        .setString("theId", id)
    		.uniqueResult(AuditEntry.class);
    if (auditEntry == null && exceptionIfNull) {
      throw new RuntimeException("Cant find audit entry");
    }
    return auditEntry;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AuditEntry auditEntryOriginal = exampleAuditEntryDb("exampleInsert");
    
    //do this because last membership update isnt there, only in db
    auditEntryOriginal = exampleRetrieveAuditEntryDb("exampleInsert", true);
    AuditEntry auditEntryCopy = exampleRetrieveAuditEntryDb("exampleInsert", true);
    AuditEntry auditEntryCopy2 = exampleRetrieveAuditEntryDb("exampleInsert", true);
    HibernateSession.byObjectStatic().delete(auditEntryCopy);
    
    //lets insert the original
    auditEntryCopy2.xmlSaveBusinessProperties(null);
    auditEntryCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    auditEntryCopy = exampleRetrieveAuditEntryDb("exampleInsert", true);
    
    assertFalse(auditEntryCopy == auditEntryOriginal);
    assertFalse(auditEntryCopy.xmlDifferentBusinessProperties(auditEntryOriginal));
    assertFalse(auditEntryCopy.xmlDifferentUpdateProperties(auditEntryOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AuditEntry auditEntry = null;
    AuditEntry exampleAuditEntry = null;

    
    //TEST UPDATE PROPERTIES
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();
      
      auditEntry.setContextId("abc");
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertTrue(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setContextId(exampleAuditEntry.getContextId());
      auditEntry.xmlSaveUpdateProperties();

      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
      
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();
      
      auditEntry.setCreatedOnDb(3L);
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertTrue(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setCreatedOnDb(exampleAuditEntry.getCreatedOnDb());
      auditEntry.xmlSaveUpdateProperties();

      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
      
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();
      
      auditEntry.setHibernateVersionNumber(4L);
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertTrue(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setHibernateVersionNumber(exampleAuditEntry.getHibernateVersionNumber());
      auditEntry.xmlSaveUpdateProperties();

      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
      
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();
      
      auditEntry.setLastUpdatedDb(5L);
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertTrue(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setLastUpdatedDb(exampleAuditEntry.getLastUpdatedDb());
      auditEntry.xmlSaveUpdateProperties();

      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
      
    }
    
    //TEST BUSINESS PROPERTIES
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setActAsMemberId("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setActAsMemberId(exampleAuditEntry.getActAsMemberId());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setAuditTypeId("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setAuditTypeId(exampleAuditEntry.getAuditTypeId());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setDescription("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setDescription(exampleAuditEntry.getDescription());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setDurationMicroseconds(3L);
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setDurationMicroseconds(exampleAuditEntry.getDurationMicroseconds());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setEnvName("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setEnvName(exampleAuditEntry.getEnvName());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setGrouperEngine("grouperEngine");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setGrouperEngine(exampleAuditEntry.getGrouperEngine());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setGrouperVersion("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setGrouperVersion(exampleAuditEntry.getGrouperVersion());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setId("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setId(exampleAuditEntry.getId());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setInt01(11L);
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setInt01(exampleAuditEntry.getInt01());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setInt02(12L);
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setInt02(exampleAuditEntry.getInt02());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setInt03(13L);
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setInt03(exampleAuditEntry.getInt03());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setInt04(14L);
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setInt04(exampleAuditEntry.getInt04());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setInt05(15L);
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setInt05(exampleAuditEntry.getInt05());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setLoggedInMemberId("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setLoggedInMemberId(exampleAuditEntry.getLoggedInMemberId());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setQueryCount(6);
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setQueryCount(exampleAuditEntry.getQueryCount());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setServerHost("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setServerHost(exampleAuditEntry.getServerHost());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setServerUserName("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setServerUserName(exampleAuditEntry.getServerUserName());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setString01("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setString01(exampleAuditEntry.getString01());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setString02("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setString02(exampleAuditEntry.getString02());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setString03("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setString03(exampleAuditEntry.getString03());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setString04("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setString04(exampleAuditEntry.getString04());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setString05("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setString05(exampleAuditEntry.getString05());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setString06("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setString06(exampleAuditEntry.getString06());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setString07("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setString07(exampleAuditEntry.getString07());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setString08("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setString08(exampleAuditEntry.getString08());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
    
    {
      auditEntry = exampleAuditEntryDb();
      exampleAuditEntry = exampleRetrieveAuditEntryDb();

      auditEntry.setUserIpAddress("abc");
      
      assertTrue(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));

      auditEntry.setUserIpAddress(exampleAuditEntry.getUserIpAddress());
      auditEntry.xmlSaveBusinessProperties(exampleRetrieveAuditEntryDb());
      auditEntry.xmlSaveUpdateProperties();
      
      auditEntry = exampleRetrieveAuditEntryDb();
      
      assertFalse(auditEntry.xmlDifferentBusinessProperties(exampleAuditEntry));
      assertFalse(auditEntry.xmlDifferentUpdateProperties(exampleAuditEntry));
    
    }
  }

  /**
   * make an example auditentry from db for testing
   * @param id 
   * @return an example audit entry
   */
  public static AuditEntry exampleAuditEntryDb(String id) {
    
    AuditEntry auditEntry = exampleRetrieveAuditEntryDb(id, false);
    
    if (auditEntry == null) {
      auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ADD, "id", 
          id, "name", "theName", "parentStemId", "theParentStemId", "displayName", 
          "theDisplayName", "description", "theDescription");
      auditEntry.setDescription("Added group: for testing");
      auditEntry.saveOrUpdate(false);
      
    }
    
    return auditEntry;
  }

}
