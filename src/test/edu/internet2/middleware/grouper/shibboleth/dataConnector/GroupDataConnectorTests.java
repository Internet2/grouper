package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;

import org.opensaml.util.resource.ResourceException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.shibboleth.filter.GroupQueryFilter;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class GroupDataConnectorTests extends BaseDataConnectorTest {

  public static final String RESOLVER_CONFIG = TEST_PATH + "GroupDataConnectorTests-resolver.xml";

  public GroupDataConnectorTests(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GroupDataConnectorTests("testAllA"));
  }

  private void runResolveTest(String groupDataConnectorName, Group group, AttributeMap correctMap) {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean(groupDataConnectorName);
      AttributeMap currentMap = new AttributeMap(gdc.resolve(getShibContext(group.getName())));
      assertEquals(correctMap, currentMap);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFieldSyntax1() {
    try {
      PSPUtil.createSpringContext(TEST_PATH + "GroupDataConnectorTests-resolver-invalid-1.xml");
      fail("Should throw a BeanCreationException");
    } catch (BeanCreationException e) {
      // OK
    } catch (ResourceException e) {
      throw new RuntimeException(e);
    }
  }

  public void testFieldSyntax2() {
    try {
      PSPUtil.createSpringContext(TEST_PATH + "GroupDataConnectorTests-resolver-invalid-2.xml");
      fail("Should throw a BeanCreationException");
    } catch (BeanCreationException e) {
      // OK
    } catch (ResourceException e) {
      throw new RuntimeException(e);
    }
  }

  public void testFieldSyntax3() {
    try {
      PSPUtil.createSpringContext(TEST_PATH + "GroupDataConnectorTests-resolver-invalid-3.xml");
      fail("Should throw a BeanCreationException");
    } catch (BeanCreationException e) {
      // OK
    } catch (ResourceException e) {
      throw new RuntimeException(e);
    }
  }

  public void testGroupNotFound() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testAttributesOnly");
      Map<String, BaseAttribute> map = gdc.resolve(getShibContext("notfound"));
      assertTrue(map.isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testAllA() {
    correctAttributesA.setAttribute("members", memberSubj0);
    correctAttributesA.setAttribute("members:all", memberSubj0);
    correctAttributesA.setAttribute("members:immediate", memberSubj0);
    
    correctAttributesA.setAttribute("groups", groupB);
    correctAttributesA.setAttribute("groups:all", groupB);
    correctAttributesA.setAttribute("groups:immediate", groupB);
    
    correctAttributesA.setAttribute("admins", SubjectTestHelper.SUBJ3);

    runResolveTest("testAll", groupA, correctAttributesA);
  }

  public void testAttributesOnlyA() {
    runResolveTest("testAttributesOnly", groupA, correctAttributesA);
  }

  public void testAttributesOnlyB() {
    runResolveTest("testAttributesOnly", groupB, correctAttributesB);
  }

  public void testAttributesOnlyC() {
    runResolveTest("testAttributesOnly", groupC, correctAttributesC);
  }

  public void testAttributesAndMembersA() {
    correctAttributesA.setAttribute("members", memberSubj0);
    runResolveTest("testAttributesAndMembers", groupA, correctAttributesA);
  }

  public void testAttributesAndMembersB() {
    correctAttributesB.addAttribute("members", groupA.toMember());
    correctAttributesB.addAttribute("members", memberSubj0);
    correctAttributesB.addAttribute("members", memberSubj1);
    runResolveTest("testAttributesAndMembers", groupB, correctAttributesB);
  }

  public void testAttributesAndMembersCustomList() {
    correctAttributesB.addAttribute("members:all:customList", memberSubj2);
    runResolveTest("testAttributesAndMembersCustomList", groupB, correctAttributesB);
  }

  public void testAttributesAndImmediateMembersB() {
    correctAttributesB.addAttribute("members:immediate", groupA.toMember());
    correctAttributesB.addAttribute("members:immediate", memberSubj1);
    runResolveTest("testAttributesAndImmediateMembers", groupB, correctAttributesB);
  }

  public void testAttributesAndEffectiveMembersB() {
    correctAttributesB.addAttribute("members:effective", memberSubj0);
    runResolveTest("testAttributesAndEffectiveMembers", groupB, correctAttributesB);
  }

  public void testAttributesAndAllMembersB() {
    correctAttributesB.addAttribute("members:all", groupA.toMember());
    correctAttributesB.addAttribute("members:all", memberSubj0);
    correctAttributesB.addAttribute("members:all", memberSubj1);
    runResolveTest("testAttributesAndAllMembers", groupB, correctAttributesB);
  }

  public void testFilterExactAttribute() {

    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterExactAttribute");

      GroupQueryFilter filter = gdc.getGroupQueryFilter();

      Set<Group> groups = filter.getResults(grouperSession);

      assertEquals(1, groups.size());

      assertTrue(groups.contains(groupA));
      assertFalse(groups.contains(groupB));
      assertFalse(groups.contains(groupC));

      assertTrue(filter.matchesGroup(groupA));
      assertFalse(filter.matchesGroup(groupB));
      assertFalse(filter.matchesGroup(groupC));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterStemNameSUB() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterStemNameSUB");

      GroupQueryFilter filter = gdc.getGroupQueryFilter();

      Set<Group> groups = filter.getResults(grouperSession);

      assertEquals(3, groups.size());

      assertTrue(groups.contains(groupA));
      assertTrue(groups.contains(groupB));
      assertTrue(groups.contains(groupC));

      assertTrue(filter.matchesGroup(groupA));
      assertTrue(filter.matchesGroup(groupB));
      assertTrue(filter.matchesGroup(groupC));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterStemNameONE() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterStemNameONE");

      GroupQueryFilter filter = gdc.getGroupQueryFilter();

      Set<Group> groups = filter.getResults(grouperSession);

      assertEquals(2, groups.size());

      assertTrue(groups.contains(groupA));
      assertTrue(groups.contains(groupB));
      assertFalse(groups.contains(groupC));

      assertTrue(filter.matchesGroup(groupA));
      assertTrue(filter.matchesGroup(groupB));
      assertFalse(groups.contains(groupC));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterAnd() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterAnd");

      GroupQueryFilter filter = gdc.getGroupQueryFilter();

      Set<Group> groups = filter.getResults(grouperSession);

      assertEquals(1, groups.size());

      assertFalse(groups.contains(groupA));
      assertTrue(groups.contains(groupB));
      assertFalse(groups.contains(groupC));

      assertFalse(filter.matchesGroup(groupA));
      assertTrue(filter.matchesGroup(groupB));
      assertFalse(filter.matchesGroup(groupC));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterOr() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterOr");

      GroupQueryFilter filter = gdc.getGroupQueryFilter();

      Set<Group> groups = filter.getResults(grouperSession);

      assertEquals(2, groups.size());

      assertFalse(groups.contains(groupA));
      assertTrue(groups.contains(groupB));
      assertTrue(groups.contains(groupC));

      assertFalse(filter.matchesGroup(groupA));
      assertTrue(filter.matchesGroup(groupB));
      assertTrue(filter.matchesGroup(groupC));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterMatchExactAttribute() {

    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterExactAttribute");

      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupA.getName())).isEmpty());
      assertTrue("map should be empty", gdc.resolve(getShibContext(groupB.getName())).isEmpty());
      assertTrue("map should be empty", gdc.resolve(getShibContext(groupC.getName())).isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterMatchStemNameSUB() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterStemNameSUB");

      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupA.getName())).isEmpty());
      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupB.getName())).isEmpty());
      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupC.getName())).isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterMatchStemNameONE() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterStemNameONE");

      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupA.getName())).isEmpty());
      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupB.getName())).isEmpty());
      assertTrue("map should be empty", gdc.resolve(getShibContext(groupC.getName())).isEmpty());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testMatchFilterAnd() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterAnd");

      assertTrue("map should be empty", gdc.resolve(getShibContext(groupA.getName())).isEmpty());
      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupB.getName())).isEmpty());
      assertTrue("map should be empty", gdc.resolve(getShibContext(groupC.getName())).isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testMatchFilterOr() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testFilterOr");

      assertTrue("map should be empty", gdc.resolve(getShibContext(groupA.getName())).isEmpty());
      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupB.getName())).isEmpty());
      assertTrue("map should not be empty", !gdc.resolve(getShibContext(groupC.getName())).isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
