package edu.internet2.middleware.grouper.shibboleth.filter;

import java.util.ArrayList;
import java.util.Set;

import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.Resource;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.GroupDataConnector;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

public class GroupQueryFilterTest extends GrouperTest {

  private GenericApplicationContext gContext;

  private Group groupA;

  private Group groupB;

  private Group groupC;

  private Stem root;

  private Stem parentStem;

  private Stem childStem;

  private GrouperSession grouperSession;

  public void setUp() {

    super.setUp();

    grouperSession = SessionHelper.getRootSession();

    root = StemHelper.findRootStem(grouperSession);

    parentStem = StemHelper.addChildStem(root, "parentStem", "parentStem");

    childStem = StemHelper.addChildStem(parentStem, "childStem", "childStem");

    groupA = StemHelper.addChildGroup(this.parentStem, "groupA", "Group A");

    groupB = StemHelper.addChildGroup(this.parentStem, "groupB", "Group B");

    groupC = StemHelper.addChildGroup(this.childStem, "groupC", "Group C");

    try {
      ArrayList<Resource> resources = new ArrayList<Resource>();
      resources.add(new ClasspathResource(
          "/test/edu/internet2/middleware/grouper/shibboleth/filter/GroupQueryFilterTest.xml"));

      gContext = new GenericApplicationContext();
      SpringConfigurationUtils.populateRegistry(gContext, resources);
      gContext.refresh();
      gContext.registerShutdownHook();
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
  }

  public void testExactAttribute() {

    GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testExactAttribute");

    GroupQueryFilter filter = gdc.getGroupQueryFilter();

    Set<Group> groups = filter.getResults(grouperSession);

    assertEquals(1, groups.size());

    assertTrue(groups.contains(groupA));
    assertFalse(groups.contains(groupB));
    assertFalse(groups.contains(groupC));

    assertTrue(filter.matchesGroup(groupA));
    assertFalse(filter.matchesGroup(groupB));
    assertFalse(filter.matchesGroup(groupC));
  }

  public void testStemNameSUB() {

    GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testStemNameSUB");

    GroupQueryFilter filter = gdc.getGroupQueryFilter();

    Set<Group> groups = filter.getResults(grouperSession);

    assertEquals(3, groups.size());

    assertTrue(groups.contains(groupA));
    assertTrue(groups.contains(groupB));
    assertTrue(groups.contains(groupC));

    assertTrue(filter.matchesGroup(groupA));
    assertTrue(filter.matchesGroup(groupB));
    assertTrue(filter.matchesGroup(groupC));
  }

  public void testStemNameONE() {

    GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testStemNameONE");

    GroupQueryFilter filter = gdc.getGroupQueryFilter();

    Set<Group> groups = filter.getResults(grouperSession);

    assertEquals(2, groups.size());

    assertTrue(groups.contains(groupA));
    assertTrue(groups.contains(groupB));
    assertFalse(groups.contains(groupC));

    assertTrue(filter.matchesGroup(groupA));
    assertTrue(filter.matchesGroup(groupB));
    assertFalse(groups.contains(groupC));
  }

  public void testAnd() {

    GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testAnd");

    GroupQueryFilter filter = gdc.getGroupQueryFilter();

    Set<Group> groups = filter.getResults(grouperSession);

    assertEquals(1, groups.size());

    assertFalse(groups.contains(groupA));
    assertTrue(groups.contains(groupB));
    assertFalse(groups.contains(groupC));

    assertFalse(filter.matchesGroup(groupA));
    assertTrue(filter.matchesGroup(groupB));
    assertFalse(filter.matchesGroup(groupC));
  }

  public void testOr() {

    GroupDataConnector gdc = (GroupDataConnector) gContext.getBean("testOr");

    GroupQueryFilter filter = gdc.getGroupQueryFilter();

    Set<Group> groups = filter.getResults(grouperSession);

    assertEquals(2, groups.size());

    assertFalse(groups.contains(groupA));
    assertTrue(groups.contains(groupB));
    assertTrue(groups.contains(groupC));

    assertFalse(filter.matchesGroup(groupA));
    assertTrue(filter.matchesGroup(groupB));
    assertTrue(filter.matchesGroup(groupC));
  }

}
