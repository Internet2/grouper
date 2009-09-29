package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.textui.TestRunner;

import org.opensaml.util.resource.ResourceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.shibboleth.filter.GroupQueryFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;

public class GroupDataConnectorTests extends GrouperTest {

  /** logger */
  private static final Logger LOG = GrouperUtil.getLogger(GroupDataConnectorTests.class);

  public static final String TEST_PATH = "/test/edu/internet2/middleware/grouper/shibboleth/dataConnector/";

  public static final String RESOLVER_CONFIG = TEST_PATH + "test-resolver.xml";

  private Group groupA;

  private Group groupB;

  private Group groupC;

  private Stem root;

  private Stem parentStem;

  private Stem childStem;

  private GrouperSession grouperSession;

  private AttributeMap correctAttributesA;

  private AttributeMap correctAttributesB;

  private AttributeMap correctAttributesC;

  public GroupDataConnectorTests(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GroupDataConnectorTests("testAttributesAndMembersCustomList"));
  }

  public void setUp() {

    super.setUp();

    grouperSession = SessionHelper.getRootSession();

    root = StemHelper.findRootStem(grouperSession);

    parentStem = StemHelper.addChildStem(root, "parentStem", "Parent Stem");

    childStem = StemHelper.addChildStem(parentStem, "childStem", "Child Stem");

    GroupType type = GroupType.createType(grouperSession, "groupType");
    type.addAttribute(grouperSession, "attr1", AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false);
    type.addList(grouperSession, "customList", AccessPrivilege.READ, AccessPrivilege.UPDATE);

    Field customList = FieldFinder.find("customList", true);

    // group A
    groupA = StemHelper.addChildGroup(this.parentStem, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);
    correctAttributesA = new AttributeMap();
    correctAttributesA.setAttribute("extension", "groupA");
    correctAttributesA.setAttribute("displayExtension", "Group A");
    correctAttributesA.setAttribute("name", "parentStem:groupA");
    correctAttributesA.setAttribute("displayName", "Parent Stem:Group A");
    correctAttributesA.setAttribute(BaseGrouperDataConnector.PARENT_STEM_NAME_ATTR, "parentStem");

    // group B
    groupB = StemHelper.addChildGroup(this.parentStem, "groupB", "Group B");
    groupB.setDescription("Group B Description");
    groupB.addType(type);
    groupB.setAttribute("attr1", "value1", false);
    groupB.addMember(SubjectTestHelper.SUBJ1);
    groupB.addMember(groupA.toSubject());
    groupB.addMember(SubjectTestHelper.SUBJ2, customList);
    groupB.store();

    correctAttributesB = new AttributeMap();
    correctAttributesB.setAttribute("extension", "groupB");
    correctAttributesB.setAttribute("displayExtension", "Group B");
    correctAttributesB.setAttribute("name", "parentStem:groupB");
    correctAttributesB.setAttribute("displayName", "Parent Stem:Group B");
    correctAttributesB.setAttribute("description", "Group B Description");
    correctAttributesB.setAttribute(BaseGrouperDataConnector.PARENT_STEM_NAME_ATTR, "parentStem");
    correctAttributesB.setAttribute("attr1", "value1");

    // group C
    groupC = StemHelper.addChildGroup(this.childStem, "groupC", "Group C");

    correctAttributesC = new AttributeMap();
    correctAttributesC.setAttribute("extension", "groupC");
    correctAttributesC.setAttribute("displayExtension", "Group C");
    correctAttributesC.setAttribute("name", "parentStem:childStem:groupC");
    correctAttributesC.setAttribute("displayName", "Parent Stem:Child Stem:Group C");
    correctAttributesC.setAttribute(BaseGrouperDataConnector.PARENT_STEM_NAME_ATTR, "parentStem:childStem");

  }

  public static ShibbolethResolutionContext getShibContext(String principal) {
    BaseSAMLProfileRequestContext attributeRequestContext = new BaseSAMLProfileRequestContext();
    attributeRequestContext.setPrincipalName(principal);
    return new ShibbolethResolutionContext(attributeRequestContext);
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

  public class AttributeMap {

    private Map<String, BaseAttribute> map;

    public AttributeMap() {
      this.map = new HashMap<String, BaseAttribute>();
    }

    public AttributeMap(Map<String, BaseAttribute> map) {
      this.map = map;
    }

    public Map<String, BaseAttribute> getMap() {
      return map;
    }

    public void addAttribute(String name, Member... members) {
      if (members == null) {
        return;
      }
      Set<Member> list = new LinkedHashSet<Member>();
      for (Member member : members) {
        list.add(member);
      }
      BaseAttribute attr = null;
      if (map.containsKey(name)) {
        attr = map.get(name);
      } else {
        attr = new BasicAttribute(name);
        map.put(name, attr);
      }
      attr.getValues().addAll(list);
    }

    public void setAttribute(String name, String... values) {
      if (values == null) {
        return;
      }
      ArrayList<String> list = new ArrayList<String>();
      for (String value : values) {
        list.add(value);
      }
      BasicAttribute attr = new BasicAttribute(name);
      attr.setValues(list);
      map.put(attr.getId(), attr);
    }

    public void setAttribute(String name, Member... members) {
      if (members == null) {
        return;
      }
      Set<Member> list = new LinkedHashSet<Member>();
      for (Member member : members) {
        list.add(member);
      }
      BasicAttribute attr = new BasicAttribute(name);
      attr.setValues(list);
      map.put(attr.getId(), attr);
    }

    /**
     * Attempt to evaluate equals() for an arbitrary BaseAttribute<Type>.
     */
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof AttributeMap)) {
        return false;
      }

      final AttributeMap that = (AttributeMap) o;

      if (!this.getMap().equals(that.getMap())) {
        return false;
      }

      TreeSet<String> allKeys = new TreeSet<String>();
      allKeys.addAll(this.getMap().keySet());
      allKeys.addAll(that.getMap().keySet());

      for (String key : allKeys) {
        BaseAttribute correctAttribute = this.getMap().get(key);
        BaseAttribute currentAttribute = that.getMap().get(key);
        if (correctAttribute == null && currentAttribute == null) {
          continue;
        }
        if (correctAttribute == null) {
          return false;
        }
        if (currentAttribute == null) {
          return false;
        }

        Collection<?> correctValues = correctAttribute.getValues();
        Collection<?> currentValues = currentAttribute.getValues();

        if (correctValues.size() != currentValues.size()) {
          return false;
        }
        // "sort" values, even if they are not comparable (?)
        Map<Integer, Object> correctValueMap = new TreeMap<Integer, Object>();
        for (Object correctValue : correctValues) {
          correctValueMap.put(Integer.valueOf(correctValue.hashCode()), correctValue);
        }
        Map<Integer, Object> currentValueMap = new TreeMap<Integer, Object>();
        for (Object currentValue : currentValues) {
          currentValueMap.put(Integer.valueOf(currentValue.hashCode()), currentValue);
        }

        ArrayList<Object> correctValueList = new ArrayList<Object>();
        for (Integer k : correctValueMap.keySet()) {
          correctValueList.add(correctValueMap.get(k));
        }
        ArrayList<Object> currentValueList = new ArrayList<Object>();
        for (Integer k : currentValueMap.keySet()) {
          currentValueList.add(currentValueMap.get(k));
        }

        if (!correctValueList.equals(currentValueList)) {
          return false;
        }
      }

      return true;
    }
  }

  public static String toString(Map<String, BaseAttribute> map) {
    if (map == null) {
      return null;
    }
    StringBuffer buffer = new StringBuffer();
    TreeSet<String> sortedKeys = new TreeSet<String>(map.keySet());
    for (String key : sortedKeys) {
      for (Object value : map.get(key).getValues()) {
        buffer.append(key + " : '" + value + "'\n");
      }
    }
    return buffer.toString();
  }

  public void testFieldSyntax1() {
    try {
      PSPUtil.createSpringContext(TEST_PATH + "test-resolver-invalid-1.xml");
      fail("Should throw a BeanCreationException");
    } catch (BeanCreationException e) {
      // OK
    } catch (ResourceException e) {
      throw new RuntimeException(e);
    }
  }

  public void testFieldSyntax2() {
    try {
      PSPUtil.createSpringContext(TEST_PATH + "test-resolver-invalid-2.xml");
      fail("Should throw a BeanCreationException");
    } catch (BeanCreationException e) {
      // OK
    } catch (ResourceException e) {
      throw new RuntimeException(e);
    }
  }

  public void testFieldSyntax3() {
    try {
      PSPUtil.createSpringContext(TEST_PATH + "test-resolver-invalid-3.xml");
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
    correctAttributesA.setAttribute("members", MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0,
        false));
    runResolveTest("testAttributesAndMembers", groupA, correctAttributesA);
  }

  public void testAttributesAndMembersB() {
    correctAttributesB.addAttribute("members", groupA.toMember());
    correctAttributesB.addAttribute("members", MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0,
        false));
    correctAttributesB.addAttribute("members", MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1,
        false));
    runResolveTest("testAttributesAndMembers", groupB, correctAttributesB);
  }

  public void testAttributesAndMembersCustomList() {
    correctAttributesB.addAttribute("members:all:customList", MemberFinder.findBySubject(grouperSession,
        SubjectTestHelper.SUBJ2, false));
    runResolveTest("testAttributesAndMembersCustomList", groupB, correctAttributesB);
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

}
