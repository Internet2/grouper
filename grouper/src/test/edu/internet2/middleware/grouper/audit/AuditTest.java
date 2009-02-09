/*
 * @author mchyzer
 * $Id: AuditTest.java,v 1.4 2009-02-09 05:33:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;


/**
 *
 */
public class AuditTest extends GrouperTest {
  /**
   * @param name
   */
  public AuditTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AuditTest("testTypes"));
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ApiConfig.testConfig.put("grouper.env.name", "testEnv");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    ApiConfig.testConfig.remove("grouper.env.name");

  }

  /**
   * @throws Exception 
   * 
   */
  public void testTypes() throws Exception {
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    //add a type
    GrouperSession grouperSession = SessionHelper.getRootSession();
    GroupType groupType = GroupType.createType(grouperSession, "test1");
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationNanos should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 2, one for select, one for insert: " + auditEntry.getQueryCount(), 2 <= auditEntry.getQueryCount());

    assertEquals("Context id's should match", auditEntry.getContextId(), groupType.getContextId());
    
    assertEquals("engine should be junit", GrouperEngineBuiltin.JUNIT.getGrouperEngine(), auditEntry.getGrouperEngine());

    assertNotNull("createdOn should exist", auditEntry.getCreatedOn());

    assertEquals("testEnv", auditEntry.getEnvName());
    
    assertNotNull(auditEntry.getLastUpdated());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    assertTrue("grouper version is blank", StringUtils.isNotBlank(auditEntry.getGrouperVersion()));

    assertTrue("server host is blank", StringUtils.isNotBlank(auditEntry.getServerHost()));

    assertTrue("server user name is blank", StringUtils.isNotBlank(auditEntry.getServerUserName()));

    groupType.delete(grouperSession);

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly two audit", auditCount+2, newAuditCount);
  
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOn").list(AuditEntry.class);
    AuditEntry auditEntry2 = auditEntries.get(1);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertTrue("durationNanos should exist", auditEntry2.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 1, one for delete: " + auditEntry2.getQueryCount(), 2 <= auditEntry2.getQueryCount());
  
    assertEquals("Context id's shouldnt match", auditEntry2.getContextId(), groupType.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
    
    
  }
}
