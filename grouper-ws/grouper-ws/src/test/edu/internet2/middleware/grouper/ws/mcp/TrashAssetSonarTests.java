package edu.internet2.middleware.grouper.ws.mcp;

import edu.internet2.middleware.grouper.app.assetSonarProvisioning.AllAssetSonarProvisionerTests;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * THROWAWAY suite for GRP-7433 (AssetSonar on a WsBearerToken external system): the whole AssetSonar
 * provisioner suite plus the MCP AssetSonar lookups. Needs the test Tomcat running (restart it so the
 * mock sees the new config). Delete once green -- do not commit.
 */
public class TrashAssetSonarTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(TrashAssetSonarTests.class.getName());
    suite.addTest(AllAssetSonarProvisionerTests.suite());
    suite.addTest(new GrouperMcpAdminExternalSystemGetTest("testGetUserAssetSonar"));
    suite.addTest(new GrouperMcpAdminExternalSystemGetTest("testGetUserAssetSonarBadLookupField"));
    suite.addTest(new GrouperMcpAdminExternalSystemGetTest("testToolDefinition"));
    return suite;
  }

}
