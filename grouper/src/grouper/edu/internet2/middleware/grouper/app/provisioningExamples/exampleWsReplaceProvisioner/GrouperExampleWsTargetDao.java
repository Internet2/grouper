package edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoReplaceGroupMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoReplaceGroupMembershipsResponse;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;

/**
 */
public class GrouperExampleWsTargetDao extends GrouperProvisionerTargetDaoBase {

  /**
   * 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");
  
  private static void executeMethod(Map<String, Object> debugMap,
      String configId, List<String> netIds,
      String source, String role) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    grouperHttpCall.assignDoNotLogHeaders(doNotLogHeaders);
    
    String url = GrouperConfig.retrieveConfig().propertyValueString("grouper.exampleWsExternalSystem." + configId + ".endpointPrefix");
    
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    
    url = url + "/"+ GrouperUtil.escapeUrlEncode(source)+ "/"+GrouperUtil.escapeUrlEncode(role);
    
    debugMap.put("url", url);

    grouperHttpCall.assignUrl(url);
    grouperHttpCall.assignGrouperHttpMethod("PUT");
    
    
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
    
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement("ExternalRoleRequest");
    Element usersElement = root.addElement("Users");
    
    for (String netId: netIds) {
      Element netIdElement = usersElement.addElement("netID");
      netIdElement.addText(netId);
    }
    
    String userName = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.exampleWsExternalSystem."+configId+".userName");
    String password = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.exampleWsExternalSystem."+configId+".password");
    String userNamePassword = userName+":"+password;
    
    String base64Encoded = Base64.getEncoder().encodeToString(userNamePassword.getBytes());
    grouperHttpCall.addHeader("Authorization", "Basic " + base64Encoded);
    
    grouperHttpCall.assignBody(document.asXML());
    grouperHttpCall.executeRequest();
    
    int code = -1;

    try {
      code = grouperHttpCall.getResponseCode();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    Set<Integer> allowedReturnCodes = new HashSet<>();
    allowedReturnCodes.add(200);
    
    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException(
          "Invalid return code '" + code + "', expecting: " + GrouperUtil.setToString(allowedReturnCodes)
              + ". '" + debugMap.get("url") + "' ");
    }

  }
  
  @Override
  public TargetDaoReplaceGroupMembershipsResponse replaceGroupMemberships(
      TargetDaoReplaceGroupMembershipsRequest targetDaoReplaceGroupMembershipsRequest) {
    
    GrouperExampleWsConfiguration grouperExampleWsConfiguration = (GrouperExampleWsConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    List<ProvisioningMembership> targetMemberships = targetDaoReplaceGroupMembershipsRequest.getTargetMemberships();
    
    ProvisioningGroup targetGroup = targetDaoReplaceGroupMembershipsRequest.getTargetGroup();

    ProvisioningAttribute roleAttribute = targetGroup.getAttributes().get("role");
    
    if (roleAttribute == null || roleAttribute.getValue() == null || GrouperUtil.isBlank(roleAttribute.getValue())) {
      targetGroup.getProvisioningGroupWrapper().setErrorCode(GcGrouperSyncErrorCode.REQ);
      targetGroup.setException(new RuntimeException("role is a required attribute."));
      return new TargetDaoReplaceGroupMembershipsResponse();
    }
    
    String roleValue = GrouperUtil.stringValue(roleAttribute.getValue());
    
    List<String> netIds = new ArrayList<>();
    for (ProvisioningMembership provisioningMembership: targetMemberships) {
      String netId = provisioningMembership.retrieveAttributeValueString("netID");
      if (StringUtils.isNotBlank(netId)) {
        netIds.add(netId);
      }
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "replaceGroupMemberships");
    
    executeMethod(debugMap, grouperExampleWsConfiguration.getExampleWsExternalSystemConfigId(), netIds, 
        grouperExampleWsConfiguration.getExampleWsSource(), roleValue);
    
    for (ProvisioningMembership provisioningMembership: targetMemberships) {
      provisioningMembership.setProvisioned(true);
    }
    targetGroup.setProvisioned(true);
    
    return new TargetDaoReplaceGroupMembershipsResponse();
  }



  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setCanReplaceGroupMemberships(true);
    
  }

}
