package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class FreshRequesterProvisionerTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {

    FreshRequesterMockServiceHandler.ensureFreshserviceMockTables();
    TestRunner.run(new FreshRequesterProvisionerTest("testRetrieveRequesterGroups"));

  }

  @Override
  public String defaultConfigId() {
    return "freshRequesterProvisioner";
  }

  public static boolean startTomcat = false;

  public FreshRequesterProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();

    FreshRequesterMockServiceHandler.ensureFreshserviceMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();
  }

  public void testRetrieveRequesterGroups() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // insert some groups directly into the mock table
    new GcDbAccess().connectionName("grouper").sql("insert into mock_freshreq_group (id, name, description) values (1001, 'IT Support', 'IT support team group')").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_freshreq_group (id, name, description) values (1002, 'HR Team', 'Human resources department')").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_freshreq_group (id, name, description) values (1003, 'Engineering', 'Engineering department')").executeSql();

    List<FreshRequesterGroup> groups = FreshRequesterApiCommands.retrieveRequesterGroups("freshServiceDev");

    assertEquals(3, groups.size());

    Map<Long, FreshRequesterGroup> groupById = new HashMap<Long, FreshRequesterGroup>();
    for (FreshRequesterGroup group : groups) {
      groupById.put(group.getId(), group);
    }

    FreshRequesterGroup group1001 = groupById.get(1001L);
    assertNotNull(group1001);
    assertEquals("IT Support", group1001.getName());
    assertEquals("IT support team group", group1001.getDescription());

    FreshRequesterGroup group1002 = groupById.get(1002L);
    assertNotNull(group1002);
    assertEquals("HR Team", group1002.getName());
    assertEquals("Human resources department", group1002.getDescription());

    FreshRequesterGroup group1003 = groupById.get(1003L);
    assertNotNull(group1003);
    assertEquals("Engineering", group1003.getName());
    assertEquals("Engineering department", group1003.getDescription());
  }

}
