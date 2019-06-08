package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class GrouperWorkflowConfig {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowConfig.class);
  
  private String workflowConfigType;
  
  private String workflowConfigApprovals = "{\n" + 
      "  states: [\n" + 
      "    {\n" + 
      "      stateName: \"initiate\",\n" + 
      "    },\n" + 
      "    {\n" + 
      "      stateName: \"groupManager\",\n" + 
      "      //who can approve\n" + 
      "      approverManagersOfGroupId: \"sdgf76gdf87\" <-- groupId of current group\n" + 
      "                                               <-- updater/admins of group\n" + 
      "    },\n" + 
      "    {\n" + 
      "      stateName: \"complete\",\n" + 
      "      //if blank use the selected groups from form or just the group\n" + 
      "      //the workflow is assigned to\n" + 
      "      actions: [\n" + 
      "           {\n" + 
      "              actionName: \"assignToGroup\",\n" + 
      "              actionArg0: \"sgk234kh234\"   <-- groupId of current group\n" + 
      "            }\n" + 
      "        ]\n" + 
      "    }\n" + 
      "  ]\n" + 
      "}";
  
  private String workflowConfigName = "groupName_managerApproval";
  
  private String workflowConfigId = "groupName_managerApproval";
  
  // put in text file
  private String workflowConfigDescription = "Group: $groupDisplayPath% approval for membership.  The group's managers will be notified about requests and can approve them.";
  
  private String workflowConfigParams = "{\n" + 
      "  params: [\n" + 
      "    {\n" + 
      "      paramName: \"notes\",\n" + 
      "      type: \"textarea\",\n" + 
      "      editableInStates: \"initiate\"\n" + 
      "    },\n" + 
      "    {\n" + 
      "      paramName: \"notesForApprovers\",\n" + 
      "      type: \"textarea\",\n" + 
      "      editableInStates: \"supervisor, dataOwner\"\n" + 
      "    }\n" + 
      "  ]\n" + 
      "}";
  
  private String workflowConfigForm = "Submit this form to be added to this group.<br /><br />\n" + 
      "The managers of the group will be notified to approve this request.<br /><br />\n" + 
      "Notes (optional): <textarea rows=\"4\" cols=\"50\" name=\"notes\" id=\"notesId\"></textarea><br /><br />\n" + 
      "Notes for approvers: <textarea rows=\"4\" cols=\"50\" name=\"notesForApprovers\" id=\"notesForApproversId\"></textarea><br /><br />";
  
  private String workflowConfigViewersGroupId;
  
  private boolean workflowConfigSendEmail = true;
  
  private String workflowConfigEnabled = "true";
  
  private String attributeAssignmentMarkerId;
  
  
  public String getWorkflowConfigType() {
    return workflowConfigType;
  }

  
  public void setWorkflowConfigType(String workflowConfigType) {
    this.workflowConfigType = workflowConfigType;
  }

  
  public String getWorkflowConfigApprovals() {
    return workflowConfigApprovals;
  }

  
  public void setWorkflowConfigApprovals(String workflowConfigApprovals) {
    this.workflowConfigApprovals = workflowConfigApprovals;
  }

  
  public String getWorkflowConfigName() {
    return workflowConfigName;
  }

  
  public void setWorkflowConfigName(String workflowConfigName) {
    this.workflowConfigName = workflowConfigName;
  }

  
  public String getWorkflowConfigId() {
    return workflowConfigId;
  }

  
  public void setWorkflowConfigId(String workflowConfigId) {
    this.workflowConfigId = workflowConfigId;
  }

  
  public String getWorkflowConfigDescription() {
    return workflowConfigDescription;
  }

  
  public void setWorkflowConfigDescription(String workflowConfigDescription) {
    this.workflowConfigDescription = workflowConfigDescription;
  }

  
  public String getWorkflowConfigParams() {
    return workflowConfigParams;
  }

  
  public void setWorkflowConfigParams(String workflowConfigParams) {
    this.workflowConfigParams = workflowConfigParams;
  }

  
  public String getWorkflowConfigForm() {
    return workflowConfigForm;
  }

  
  public void setWorkflowConfigForm(String workflowConfigForm) {
    this.workflowConfigForm = workflowConfigForm;
  }

  
  public String getWorkflowConfigViewersGroupId() {
    return workflowConfigViewersGroupId;
  }

  
  public void setWorkflowConfigViewersGroupId(String workflowConfigViewersGroupId) {
    this.workflowConfigViewersGroupId = workflowConfigViewersGroupId;
  }
  
  
  public boolean isWorkflowConfigSendEmail() {
    return workflowConfigSendEmail;
  }

  
  public void setWorkflowConfigSendEmail(boolean workflowConfigSendEmail) {
    this.workflowConfigSendEmail = workflowConfigSendEmail;
  }

  
  public String getWorkflowConfigEnabled() {
    return workflowConfigEnabled;
  }

  public void setWorkflowConfigEnabled(String workflowConfigEnabled) {
    this.workflowConfigEnabled = workflowConfigEnabled;
  }

  public String getAttributeAssignmentMarkerId() {
    return attributeAssignmentMarkerId;
  }


  public void setAttributeAssignmentMarkerId(String attributeAssignmentMarkerId) {
    this.attributeAssignmentMarkerId = attributeAssignmentMarkerId;
  }
  
  public void validate(Group group) {
    
    if (StringUtils.isBlank(workflowConfigType)) {
      // error
      GrouperTextContainer.retrieveFromRequest().getText().get("error key");
    }
    
    if (StringUtils.isBlank(workflowConfigApprovals)) {
      // error
    }
    
    boolean isInitiateStateAvailable = false;
    boolean isCompleteStateAvailable = false;
    
    try { 
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(workflowConfigApprovals);
      JSONArray jsonArray = jsonObject.getJSONArray("states");
      for (int i=0; i<jsonArray.size(); i++) {
        JSONObject jsonObject2 = jsonArray.getJSONObject(i);
        String stateName = jsonObject2.getString("stateName");
        //TODO validate allowed group id
        if (StringUtils.isNotBlank(stateName) && stateName.equals("initiate")) {
          isInitiateStateAvailable = true;
        }
        if (StringUtils.isNotBlank(stateName) && stateName.equals("complete")) {
          isCompleteStateAvailable = true;
        }
      }
    } catch (Exception e) {
      // have well formatted json
    }
    
    if (!isInitiateStateAvailable) {
      // error
    }
    
    if (!isCompleteStateAvailable) {
      // error
    }
    
    
    if (StringUtils.isBlank(workflowConfigName)) {
      // error
    }
    
    List<GrouperWorkflowConfig> configs = GrouperWorkflowConfigService.getWorkflowConfigs(group);
    
    for (GrouperWorkflowConfig config: configs) {
      if (config.getWorkflowConfigName().equals(workflowConfigName)) {
        // error - break
      }
    }
    
    if (StringUtils.isBlank(workflowConfigId)) {
      // error
    }
    
    if (GrouperWorkflowConfigService.workflowIdExists(workflowConfigId)) {
      // error
    }
    
    if (StringUtils.isBlank(workflowConfigDescription)) {
      // error
    }
    
    if (workflowConfigDescription.length() > 4000) {
      // error
    }
    
    if (StringUtils.isBlank(workflowConfigParams)) {
      // error
    }
    
    try {   
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(workflowConfigParams);
      JSONArray jsonArray = jsonObject.getJSONArray("params");
      int totalParams = 0;
      for (int i=0; i<jsonArray.size(); i++) {
        totalParams++;
        JSONObject jsonObject2 = jsonArray.getJSONObject(i);
        String paramName = jsonObject2.getString("paramName");
        String type = jsonObject2.getString("type");
        String editableInStates = jsonObject2.getString("editableInStates");
        if (StringUtils.isBlank(paramName) || StringUtils.isBlank(type) || StringUtils.isBlank(editableInStates)) {
          // error
        }
        
        String required = jsonObject2.getString("required");
        if (type.equals("checkbox")) {
          if (StringUtils.isNotBlank(required) && (!required.equals("true") || !required.equals("false"))) {
            // error
          }
        }
        
      }
      
      if (totalParams > 10) {
        // error
      }
      
    } catch (Exception e) {
      // have well formatted json
    }
    
    if (StringUtils.isNotBlank(workflowConfigViewersGroupId)) {
      Group viewersGroupId = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), workflowConfigViewersGroupId, false);
      if (viewersGroupId == null) {
        // error
      }
    }
    
    
    if (StringUtils.isBlank(workflowConfigEnabled)) {
      // error
    }
    
    if (!workflowConfigEnabled.equals("true") || !workflowConfigEnabled.equals("false")
        || !workflowConfigEnabled.equals("noNewSubmissions")) {
      // error
    }
    
    if (StringUtils.isNotBlank(workflowConfigForm)) {
      
      
      
      
    }
    
    
  }
  
  public static void main(String[] args) {
    
    String approvals = "{\n" + 
        "  states: [\n" + 
        "    {\n" + 
        "      stateName: \"initiate\",\n" + 
        "    },\n" + 
        "    {\n" + 
        "      stateName: \"groupManager\",\n" + 
        "      approverManagersOfGroupId: \"sdgf76gdf87\" \n" + 
        "    },\n" + 
        "    {\n" + 
        "      stateName: \"complete\",\n" + 
        "      actions: [\n" + 
        "           {\n" + 
        "              actionName: \"assignToGroup\",\n" + 
        "              actionArg0: \"sgk234kh234\"  \n" + 
        "            }\n" + 
        "        ]\n" + 
        "    }\n" + 
        "  ]\n" + 
        "}";
    
    try {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(approvals);
    } catch (Exception e) {
      System.out.println("e is "+e);
    }
  }
  
  public boolean isSubjectInAllowedGroup(Subject subject) {
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(workflowConfigApprovals);
    JSONArray jsonArray = jsonObject.getJSONArray("states");
    JSONObject initiateStateObject = null;
    for (int i=0; i<jsonArray.size(); i++) {
      JSONObject jsonObject2 = jsonArray.getJSONObject(i);
      String stateName = jsonObject2.getString("stateName");
      if (StringUtils.isNotBlank(stateName) && stateName.equals("initiate")) {
        initiateStateObject = jsonObject2;
        break;
      }
    }
    
    if (initiateStateObject != null && initiateStateObject.containsKey("allowedGroupId")) {
      String allowedGroupId = initiateStateObject.getString("allowedGroupId");
      if (StringUtils.isBlank(allowedGroupId)) {
        return false;
      }
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(),allowedGroupId, false);
      if (group == null) {
        LOG.error("allowed group id "+allowedGroupId+" not found in workflow id "+workflowConfigId);
        return false;
      }
      
      Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(),
          subject, false);
      return member != null && member.isMember(group);
    } 
    
    return false;
    
  }
  
}
