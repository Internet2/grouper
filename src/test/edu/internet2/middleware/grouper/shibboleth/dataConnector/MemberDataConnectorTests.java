package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Map;

import junit.textui.TestRunner;

import org.opensaml.util.resource.ResourceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.subject.Subject;

public class MemberDataConnectorTests extends BaseDataConnectorTest {

  private static final Logger LOG = GrouperUtil.getLogger(MemberDataConnectorTests.class);

  public static final String RESOLVER_CONFIG = TEST_PATH + "MemberDataConnectorTests-resolver.xml";

  public MemberDataConnectorTests(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(MemberDataConnectorTests.class);
    //TestRunner.run(new MemberDataConnectorTests("testUnknownSource"));
  }

  private void runResolveTest(String groupDataConnectorName, Subject subject, AttributeMap correctMap) {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      MemberDataConnector mdc = (MemberDataConnector) gContext.getBean(groupDataConnectorName);
      AttributeMap currentMap = new AttributeMap(mdc.resolve(getShibContext(subject.getId())));
      if (LOG.isDebugEnabled()) {
        LOG.debug("correct\n{}", correctMap);
        LOG.debug("current\n{}", currentMap);
      }
      assertEquals(correctMap, currentMap);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testUnknownSource() {
    try {
      PSPUtil.createSpringContext(TEST_PATH + "MemberDataConnectorTests-unknownSource.xml");
      fail("Should throw a BeanCreationException");
    } catch (BeanCreationException e) {
      // OK
    } catch (ResourceException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void testUnknownSourceIdentifier() {
    try {
      PSPUtil.createSpringContext(TEST_PATH + "MemberDataConnectorTests-unknownSourceIdentifier.xml");
      fail("Should throw a BeanCreationException");
    } catch (BeanCreationException e) {
      // OK
    } catch (ResourceException e) {
      throw new RuntimeException(e);
    }
  }

  public void testSubjectNotFound() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      MemberDataConnector mdc = (MemberDataConnector) gContext.getBean("testIdOnly");
      Map<String, BaseAttribute> map = mdc.resolve(getShibContext("notfound"));
      assertTrue(map.isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testMemberNotFound() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      MemberDataConnector mdc = (MemberDataConnector) gContext.getBean("testIdOnly");
      Map<String, BaseAttribute> map = mdc.resolve(getShibContext(SubjectTestHelper.SUBJ9_ID));
      assertTrue(map.isEmpty());
      assertNotNull(SubjectFinder.findByIdOrIdentifier(SubjectTestHelper.SUBJ9_ID, false));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testDoNotReturnGroups() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      MemberDataConnector mdc = (MemberDataConnector) gContext.getBean("testIdOnly");
      Map<String, BaseAttribute> map = mdc.resolve(getShibContext(groupA.getName()));
      assertTrue(map.isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testIdOnly() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ0_ID);

    runResolveTest("testIdOnly", SubjectTestHelper.SUBJ0, correct);
  }
  
  public void testIdOnlySource() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ0_ID);

    runResolveTest("testIdOnlySource", SubjectTestHelper.SUBJ0, correct);
  }

  public void testIdNameDescription() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ0_ID);
    correct.setAttribute("name", SubjectTestHelper.SUBJ0_NAME);
    correct.setAttribute("description", "description." + SubjectTestHelper.SUBJ0_ID);

    runResolveTest("testIdNameDescription", SubjectTestHelper.SUBJ0, correct);
  }

  public void testAttributesOnly() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ0_ID);
    correct.setAttribute("name", SubjectTestHelper.SUBJ0_NAME);
    correct.setAttribute("description", "description." + SubjectTestHelper.SUBJ0_ID);
    correct.setAttribute("loginid", "id." + SubjectTestHelper.SUBJ0_ID);
    correct.setAttribute("lfname", "name." + SubjectTestHelper.SUBJ0_ID);

    runResolveTest("testAttributesOnly", SubjectTestHelper.SUBJ0, correct);
  }

  public void testGroupsSubj0() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ0_ID);
    correct.addAttribute("groups", groupA);
    correct.addAttribute("groups", groupB);

    runResolveTest("testGroups", SubjectTestHelper.SUBJ0, correct);
  }

  public void testGroupsSubj1() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ1_ID);
    correct.addAttribute("groups", groupB);

    runResolveTest("testGroups", SubjectTestHelper.SUBJ1, correct);
  }

  public void testGroupsSubj2() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ2_ID);

    runResolveTest("testGroups", SubjectTestHelper.SUBJ2, correct);
  }

  public void testGroupsCustomListSubj0() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ0_ID);

    runResolveTest("testGroupsCustomList", SubjectTestHelper.SUBJ0, correct);
  }

  public void testGroupsCustomListSubj1() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ1_ID);

    runResolveTest("testGroupsCustomList", SubjectTestHelper.SUBJ1, correct);
  }

  public void testGroupsCustomListSubj2() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ2_ID);
    correct.addAttribute("groups:all:customList", groupB);

    runResolveTest("testGroupsCustomList", SubjectTestHelper.SUBJ2, correct);
  }
  
  public void testGroupsFilterExactAttributeSubj0() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ0_ID);
    correct.addAttribute("groups", groupA);

    runResolveTest("testGroupsFilterExactAttribute", SubjectTestHelper.SUBJ0, correct);
  }

  public void testAdminsSubj3() {
    AttributeMap correct = new AttributeMap();
    correct.setAttribute("id", SubjectTestHelper.SUBJ3_ID);
    correct.addAttribute("admins", groupA);

    runResolveTest("testAdmins", SubjectTestHelper.SUBJ3, correct);
  }

}
