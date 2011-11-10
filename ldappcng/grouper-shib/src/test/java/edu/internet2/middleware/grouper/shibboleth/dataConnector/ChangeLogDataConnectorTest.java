/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.List;

import junit.textui.TestRunner;

import org.hibernate.criterion.Restrictions;
import org.opensaml.util.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * Test for {@link ChangeLogDataConnector}.
 */
public class ChangeLogDataConnectorTest extends BaseDataConnectorTest {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(ChangeLogDataConnectorTest.class);

  /** Path to attribute resolver configuration. */
  public static final String RESOLVER_CONFIG = TEST_PATH + "ChangeLogDataConnectorTest-resolver.xml";

  /** The data connector. */
  private ChangeLogDataConnector changeLogDataConnector;

  /** The spring context. */
  private GenericApplicationContext gContext;

  /**
   * 
   * Constructor
   * 
   * @param name
   */
  public ChangeLogDataConnectorTest(String name) {
    super(name);
  }

  /**
   * Run tests.
   * 
   * @param args
   */
  public static void main(String[] args) {
    // TestRunner.run(ChangeLogDataConnectorTest.class);
    TestRunner.run(new ChangeLogDataConnectorTest("testFilterChangeLogExactAttribute"));
  }

  /**
   * {@inheritDoc}
   */
  public void setUp() {

    super.setUp();

    // setup Spring
    try {
      gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
    } catch (ResourceException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    // convert temp change log records
    ChangeLogTempToEntity.convertRecords();

    // delete a group and memberships
    groupA.delete();

    // convert temp change log records
    ChangeLogTempToEntity.convertRecords();
  }

  /**
   * Assert that the attributes returned from the data connector match the provided attributes.
   * 
   * @param dataConnector the data connector
   * @param sequenceNumber the change log entry identifier
   * @param correctMap the correct attributes
   */
  protected void runResolveTest(ChangeLogDataConnector dataConnector, String sequenceNumber, AttributeMap correctMap) {
    try {
      AttributeMap currentMap = new AttributeMap(dataConnector.resolve(getShibContext(sequenceNumber)));
      LOG.debug("current attributes\n{}", currentMap);
      LOG.debug("correct attributes\n{}", correctMap);
      assertEquals(correctMap, currentMap);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test audit filter by both category and action.
   */
  public void testFilterChangeLogAudit() {

    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterChangeLogAudit");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      boolean match = false;

      List<AuditEntry> auditEntries = new UserAuditQuery().setExtraCriterion(
          Restrictions.eq(AuditEntry.FIELD_CONTEXT_ID, changeLogEntry.getContextId())).execute();

      for (AuditEntry auditEntry : auditEntries) {
        if (auditEntry.getAuditType().getActionName().equals("deleteGroup")
            && auditEntry.getAuditType().getAuditCategory().equals("group")) {
          match = true;
          break;
        }
      }

      if (match) {
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.GROUP_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.MEMBERSHIP_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

  /**
   * Test audit filter by action only.
   */
  public void testFilterChangeLogAuditAction() {

    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterChangeLogAuditAction");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      boolean match = false;

      List<AuditEntry> auditEntries = new UserAuditQuery().setExtraCriterion(
          Restrictions.eq(AuditEntry.FIELD_CONTEXT_ID, changeLogEntry.getContextId())).execute();

      for (AuditEntry auditEntry : auditEntries) {
        if (auditEntry.getAuditType().getActionName().equals("deleteGroup")) {
          match = true;
          break;
        }
      }

      if (match) {
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.GROUP_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.MEMBERSHIP_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

  /**
   * Test audit filter by category only.
   */
  public void testFilterChangeLogAuditCategory() {

    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterChangeLogAuditCategory");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      boolean match = false;

      List<AuditEntry> auditEntries = new UserAuditQuery().setExtraCriterion(
          Restrictions.eq(AuditEntry.FIELD_CONTEXT_ID, changeLogEntry.getContextId())).execute();

      for (AuditEntry auditEntry : auditEntries) {
        if (auditEntry.getAuditType().getAuditCategory().equals("stem")) {
          match = true;
          break;
        }
      }

      if (match) {
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_ADD)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.STEM_ADD.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.STEM_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_UPDATE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.STEM_UPDATE.values()) {
            // a runtime exception is thrown here, see ChangeLogLabels.STEM_UPDATE.displayExtension
            if (changeLogLabel.name().equals(ChangeLogLabels.STEM_UPDATE.displayExtension.name())) {
              continue;
            }
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

  /**
   * Test filter by change log entry category and action.
   */
  public void testFilterChangeLogEntry() {

    // initialize the data connector
    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterChangeLogEntry");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
        // for every field in the change log entry, add the corresponding attribute to the map
        for (ChangeLogLabel changeLogLabel : ChangeLogLabels.MEMBERSHIP_DELETE.values()) {
          correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

  /**
   * Test filter by change log action only.
   */
  public void testFilterChangeLogEntryAction() {

    // initialize the data connector
    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterChangeLogEntryAction");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      if (changeLogEntry.getChangeLogType().getActionName().equals("deleteMembership")) {
        // for every field in the change log entry, add the corresponding attribute to the map
        for (ChangeLogLabel changeLogLabel : ChangeLogLabels.MEMBERSHIP_DELETE.values()) {
          correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

  /**
   * Test filter by change log category only.
   */
  public void testFilterChangeLogEntryCategory() {

    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterChangeLogEntryCategory");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      if (changeLogEntry.getChangeLogType().getChangeLogCategory().equals("membership")) {
        // for every field in the change log entry, add the corresponding attribute to the map
        if (changeLogEntry.getChangeLogType().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.MEMBERSHIP_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
        if (changeLogEntry.getChangeLogType().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.MEMBERSHIP_ADD.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

  /**
   * Test filter by change log category and action and audit category and action.
   */
  public void testFilterDeleteMembership() {

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    ChangeLogTempToEntity.convertRecords();

    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterDeleteMembership");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      boolean match = false;

      List<AuditEntry> auditEntries = new UserAuditQuery().setExtraCriterion(
          Restrictions.eq(AuditEntry.FIELD_CONTEXT_ID, changeLogEntry.getContextId())).execute();

      if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
        for (AuditEntry auditEntry : auditEntries) {
          if (!(auditEntry.getAuditType().getActionName().equals("deleteGroup") && auditEntry.getAuditType()
              .getAuditCategory().equals("group"))) {
            match = true;
            break;
          }
        }
      }

      if (match) {
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.MEMBERSHIP_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

  /**
   * Test filter by change log category and action and audit category and action. Should not match any entries.
   */
  public void testFilterDeleteMembershipNoMatch() {

    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterDeleteMembership");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      boolean match = false;

      List<AuditEntry> auditEntries = new UserAuditQuery().setExtraCriterion(
          Restrictions.eq(AuditEntry.FIELD_CONTEXT_ID, changeLogEntry.getContextId())).execute();

      if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
        for (AuditEntry auditEntry : auditEntries) {
          if (!(auditEntry.getAuditType().getActionName().equals("deleteGroup") && auditEntry.getAuditType()
              .getAuditCategory().equals("group"))) {
            match = true;
            break;
          }
        }
      }

      if (match) {
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.GROUP_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
          for (ChangeLogLabel changeLogLabel : ChangeLogLabels.MEMBERSHIP_DELETE.values()) {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          }
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

  /**
   * Test filter by change log exact attribute.
   */
  public void testFilterChangeLogExactAttribute() {

    // initialize the data connector
    changeLogDataConnector = (ChangeLogDataConnector) gContext.getBean("testFilterChangeLogExactAttribute");

    // retrieve all change log entries
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(-1, 1000);

    // for every change log entry
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // the empty map
      AttributeMap correctMap = new AttributeMap();

      // if change log entry matches, build correct map
      if (changeLogEntry.getChangeLogType().getActionName().equals("updateStem")) {
        // for every field in the change log entry, add the corresponding attribute to the map

        for (ChangeLogLabel changeLogLabel : ChangeLogLabels.STEM_UPDATE.values()) {
          try {
            correctMap.setAttribute(changeLogLabel.name(), changeLogEntry.retrieveValueForLabel(changeLogLabel));
          } catch (RuntimeException e) {
            // do not blame me
          }
        }
      }

      // verify that the correct attributes are returned from the data connector for every change log entry
      runResolveTest(changeLogDataConnector, ChangeLogDataConnector.principalName(changeLogEntry.getSequenceNumber()),
          correctMap);
    }
  }

}
