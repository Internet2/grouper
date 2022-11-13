package edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.j2ee.Authentication;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class ExampleWsMockServiceHandler extends MockServiceHandler {

  /**
   * 
   */
  public static void ensureMockTable() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_example_ws").select(int.class);
    } catch (Exception e) {

      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        
        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          createMockExampleTable(ddlVersionBean, database);
        }
      });
  
    }    
  }
  
  private static void createMockExampleTable(DdlVersionBean ddlVersionBean, Database database) {
    
    final String tableName = "mock_example_ws";

    try {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_name", Types.VARCHAR, "256", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "net_id", Types.VARCHAR, "256", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "source", Types.VARCHAR, "256", true, true);
    } catch (Exception e) {
      
    }
    
  }
  
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    
    String basicAuth = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    
    String userName = Authentication.retrieveUsername(basicAuth);
    String password = Authentication.retrievePassword(basicAuth);
    
    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.exampleWs.mockExternalSystem.configId");
    
    String expectedUserName = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.exampleWsExternalSystem."+configId+".userName");
    String expectedPassword = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.exampleWsExternalSystem."+configId+".password");
    
    if (!StringUtils.equals(expectedUserName, userName)) {
      throw new RuntimeException("Username does not match with what is in grouper config");
    }
    if (!StringUtils.equals(expectedPassword, password)) {
      throw new RuntimeException("password does not match with what is in grouper config");
    }
    
  }
  
  private void replaceMemberships(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    String body = mockServiceRequest.getRequestBody();
    
    /**
     * <?xml version="1.0"?>
        <ExternalRoleRequest>
         <Users>
        <netID>USER1234</netID>
        <netID>USER5678</netID>
        <netID>USER9012</netID>
        <netID>USER3456</netID>
        </Users>
        </ExternalRoleRequest>
     */
    List<String> netIds = new ArrayList<>();
    
    try {
      Document document = DocumentHelper.parseText(body);
      Element rootElement = document.getRootElement();
      Element usersElement = rootElement.element("Users");
      for (Element element : usersElement.elements("netID")) {
        netIds.add(element.getText());
      }
      
      String netIdSelectQuery = "select net_id from mock_example_ws where group_name = ? and source = ?";
      
      String role = mockServiceRequest.getPostMockNamePaths()[1];
      String source = mockServiceRequest.getPostMockNamePaths()[0];
      
      List<String> existingNetIdsList = new GcDbAccess().sql(netIdSelectQuery)
          .addBindVar(role)
          .addBindVar(source)
          .selectList(String.class);
      Set<String> existingNetIds = new HashSet<>(existingNetIdsList);
      
      Set<String> inserts = new HashSet<>(netIds);
      
      inserts.removeAll(existingNetIds);
      
      if (inserts.size() > 0) {
        List<List<Object>> batchBindVars = new ArrayList<>();
        for (String insertNetId: inserts) {
          batchBindVars.add(GrouperUtil.toList(role, insertNetId, source));
        }
          
        new GcDbAccess().sql("insert into mock_example_ws (group_name, net_id, source) values (?, ?, ?)")
        .batchBindVars(batchBindVars)
        .executeBatchSql();
      }
      
      Set<String> deletes = new HashSet<>(existingNetIds);
      
      deletes.removeAll(netIds);
      
      if (deletes.size() > 0) {
        List<List<Object>> batchBindVars = new ArrayList<>();
        for (String deleteNetId: deletes) {
          batchBindVars.add(GrouperUtil.toList(deleteNetId, role, source));
        }
          
        new GcDbAccess().sql("delete from mock_example_ws where net_id = ? and group_name = ? and source = ?")
        .batchBindVars(batchBindVars)
        .executeBatchSql();
      }
      mockServiceResponse.setResponseCode(200);
      
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(500);
      return;
    }
    
    
  }
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    ensureMockTable();
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) != 2) {
      throw new RuntimeException("Pass in source and role on the path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);
    
    mockServiceRequest.setPostMockNamePaths(paths);
    
    checkAuthorization(mockServiceRequest);
    
    if (StringUtils.equals("PUT", mockServiceRequest.getHttpServletRequest().getMethod())) {
        replaceMemberships(mockServiceRequest, mockServiceResponse);
        return;
    }
    
    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
    
  }

}
