package edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisionerGeneric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
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
 * DAO is the glue from the provisioner to the target.  Implement the select/insert/update/delete.
 * If the target supports batching (either multiple updates at a time for example, or batching operations), 
 * implement that instead of individual operations.  Each operation needs to be registered in 
 * registerGrouperProvisionerDaoCapabilities() or it wont be used
 */
public class GrouperExampleWsGenericTargetDao extends GrouperProvisionerTargetDaoBase {

  /**
   * some things 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");
  
  /**
   * select/insert/update is better than replace, but if thats all you got, run with it.
   * this is an example of a "command" method which doesnt use anything from provisioning.
   * So this could be called from another place for another reason (e.g. test external system).
   * You can put all these types of methods in a commands class instead of the dao class
   * to organize things.  This is how Grouper implements provisioners.
   * @param debugMap
   * @param configId
   * @param netIds
   * @param source
   * @param role
   */
  public static void replaceMembers(Map<String, Object> debugMap,
      String configId, List<String> netIds,
      String source, String role) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    // sentive stuff shouldnt be logged in command log
    grouperHttpCall.assignDoNotLogHeaders(doNotLogHeaders);
    
    // get configs from grouper.properties based on the provisioner config ID.
    // these values need to be put in grouper.properties manually, not on provisioning wizard.
    String url = GrouperConfig.retrieveConfig().propertyValueStringRequired("exampleWs." + configId + ".endpointPrefix");
    
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    
    url = url + "/"+ GrouperUtil.escapeUrlEncode(source)+ "/"+GrouperUtil.escapeUrlEncode(role);
    
    debugMap.put("url", url);

    // grouperHttpClient is the recommended way to call web services.  It does proxies, and logs via commands class. 
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
    
    String userName = GrouperConfig.retrieveConfig().propertyValueStringRequired("exampleWs."+configId+".userName");
    String password = GrouperConfig.retrieveConfig().propertyValueStringRequired("exampleWs."+configId+".password");
    grouperHttpCall.assignUser(userName);
    grouperHttpCall.assignPassword(password);
    
    grouperHttpCall.assignBody(document.asXML());
    grouperHttpCall.executeRequest();
    
    int code = -1;

    try {
      code = grouperHttpCall.getResponseCode();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    Set<Integer> allowedReturnCodes = new HashSet<>();
    // add allows return codes here if more than one
    allowedReturnCodes.add(200);
    
    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException(
          "Invalid return code '" + code + "', expecting: " + GrouperUtil.setToString(allowedReturnCodes)
              + ". '" + debugMap.get("url") + "' ");
    }

  }
  
  /**
   * this is the method the provisioning framework will call
   * @param this is the group and memberships to replace
   * @return tell the framework what happened or what was returned
   */
  @Override
  public TargetDaoReplaceGroupMembershipsResponse replaceGroupMemberships(
      TargetDaoReplaceGroupMembershipsRequest targetDaoReplaceGroupMembershipsRequest) {
        
    List<ProvisioningMembership> targetMemberships = targetDaoReplaceGroupMembershipsRequest.getTargetMemberships();
    
    ProvisioningGroup targetGroup = targetDaoReplaceGroupMembershipsRequest.getTargetGroup();

    // the role attribute was predetermined to be in the target representation for this provisioner
    ProvisioningAttribute roleAttribute = targetGroup.getAttributes().get("role");

    // its gotta be there
    if (roleAttribute == null || roleAttribute.getValue() == null || GrouperUtil.isBlank(roleAttribute.getValue())) {
      targetGroup.getProvisioningGroupWrapper().setErrorCode(GcGrouperSyncErrorCode.REQ);
      targetGroup.setException(new RuntimeException("role is a required attribute."));
      return new TargetDaoReplaceGroupMembershipsResponse();
    }
    
    String roleValue = GrouperUtil.stringValue(roleAttribute.getValue());
    
    List<String> netIds = new ArrayList<>();
    for (ProvisioningMembership provisioningMembership: targetMemberships) {

      // the netID attribute was predetermined to be in the target representation for this provisioner
      String netId = provisioningMembership.retrieveAttributeValueString("netID");
      if (StringUtils.isNotBlank(netId)) {
        netIds.add(netId);
      }
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "replaceGroupMemberships");
    
    String configId = this.getGrouperProvisioner().getConfigId();
    String source = GrouperConfig.retrieveConfig().propertyValueStringRequired("exampleWs."+configId+".source");

    // call the command
    try {
      replaceMembers(debugMap, configId, netIds, source, roleValue);
    } catch (RuntimeException re) {
      // however exceptions are handled, put some info in there
      GrouperUtil.injectInException(re, GrouperUtil.mapToString(debugMap));
      throw re;
    }
    
    // tell the provisioner that everything is ok
    targetGroup.setProvisioned(true);
    for (ProvisioningMembership provisioningMembership: targetMemberships) {
      provisioningMembership.setProvisioned(true);
    }

    // theres nothing in this response
    return new TargetDaoReplaceGroupMembershipsResponse();
  }

  /** logger if needed */
  private static final Log LOG = GrouperUtil.getLog(GrouperExampleWsGenericTargetDao.class);

  /**
   * tell the framework what methods are available
   */
  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setCanReplaceGroupMemberships(true);
    
  }

}
