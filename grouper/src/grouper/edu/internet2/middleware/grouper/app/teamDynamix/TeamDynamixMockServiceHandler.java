package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.Set;

import edu.internet2.middleware.grouper.app.azure.GrouperAzureAuth;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureGroup;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureMembership;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureUser;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class TeamDynamixMockServiceHandler extends MockServiceHandler {
  
  public TeamDynamixMockServiceHandler() {
  }

  /**
   * 
   */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /**
   * 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  private String configId;
  /**
   * params to not log all of
   */
  @Override
  public Set<String> doNotLogParameters() {
    
    return doNotLogParameters;
  }

  /**
   * headers to not log all of
   */
  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  /**
   * 
   */
  public static void ensureTeamDynamixMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_teamdynamix_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_teamdynamix_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_teamdynamix_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_teamdynamix_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          TeamDynamixGroup.createTableTeamDynamixGroup(ddlVersionBean, database);
          TeamDynamixAuth.createTableTeamDynamixAuth(ddlVersionBean, database);
          TeamDynamixUser.createTableTeamDynamixUser(ddlVersionBean, database);
          TeamDynamixMembership.createTableTeamDynamixMembership(ddlVersionBean, database);
          
        }
      });
  
    }    
  }

  /**
   * 
   */
  public static void dropAzureMockTables() {
    MockServiceServlet.dropMockTable("mock_teamdynamix_membership");
    MockServiceServlet.dropMockTable("mock_teamdynamix_user");
    MockServiceServlet.dropMockTable("mock_teamdynamix_group");
    MockServiceServlet.dropMockTable("mock_teamdynamix_auth");
  }


  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest,
      MockServiceResponse mockServiceResponse) {
    // TODO Auto-generated method stub
    
  }

}
