package edu.internet2.middleware.grouper.app.workflow;

import java.util.ArrayList;
import java.util.Arrays;
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
  
  public List<String> validate(Group group, boolean checkForDuplicateConfig) {
    
    List<String> errors = new ArrayList<String>();
    
    if (StringUtils.isBlank(workflowConfigType)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowTypeRequiredError"));
    }
    
    //TODO if workflow type is not one of the workflow types we know of, error
    
    if (StringUtils.isBlank(workflowConfigApprovals)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowApprovalsRequiredError"));
    }
    
    boolean isInitiateStateAvailable = false;
    boolean isCompleteStateAvailable = false;
    boolean workflowApprovalsValidJson = true;
    
    try { 
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(workflowConfigApprovals);
      if (!jsonObject.containsKey("states")) {
        errors.add(GrouperTextContainer.retrieveFromRequest()
            .getText().get("workflowApprovalsStatesRequiredError"));
      } else {
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
          
          //TODO validate group ids
        }
      }
    } catch (Exception e) {
      workflowApprovalsValidJson = false;
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowApprovalsInvalidJsonError"));
    }
    
    if (workflowApprovalsValidJson && !isInitiateStateAvailable) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowApprovalsInitiateStateRequiredError"));
    }
    
    if (workflowApprovalsValidJson && !isCompleteStateAvailable) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowApprovalsCompleteStateRequiredError"));
    }
    
    
    if (StringUtils.isBlank(workflowConfigName)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigNameRequiredError"));
    }
    
    if (checkForDuplicateConfig) {
      List<GrouperWorkflowConfig> configs = GrouperWorkflowConfigService.getWorkflowConfigs(group);
      
      for (GrouperWorkflowConfig config: configs) {
        if (config.getWorkflowConfigName().equals(workflowConfigName)) {
          errors.add(GrouperTextContainer.retrieveFromRequest()
              .getText().get("workflowConfigNameAlreadyInUseError"));
          break;
        }
      }
    }
    
    if (StringUtils.isBlank(workflowConfigId)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigIdRequiredError"));
    }
    
    String configIdRegex = "^[a-zA-Z0-9_-]*$";
    
    if (!workflowConfigId.matches(configIdRegex)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigIdNotValidError"));
    }
    
    if (checkForDuplicateConfig && GrouperWorkflowConfigService.workflowIdExists(workflowConfigId)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigIdAlreadyInUseError"));
    }
    
    if (StringUtils.isBlank(workflowConfigDescription)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigDescriptionRequiredError"));
    }
    
    if (workflowConfigDescription.length() > 4000) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigDescriptionLengthExceedsMaxLengthError"));
    }
    
    if (StringUtils.isBlank(workflowConfigParams)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigParamsRequiredError"));
    }
    
    try {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(workflowConfigParams);
      if (!jsonObject.containsKey("params")) {
        errors.add(GrouperTextContainer.retrieveFromRequest()
            .getText().get("workflowParamsParamsRequiredError"));
      } else {
        JSONArray jsonArray = jsonObject.getJSONArray("params");
        int totalParams = 0;
        for (int i=0; i<jsonArray.size(); i++) {
          totalParams++;
          JSONObject jsonObject2 = jsonArray.getJSONObject(i);
          
          String paramName = jsonObject2.containsKey("paramName") ? jsonObject2.getString("paramName"): null;
          String type = jsonObject2.containsKey("type") ? jsonObject2.getString("type"): null;
          String editableInStates = jsonObject2.containsKey("editableInStates") ? jsonObject2.getString("editableInStates"): null;
          
          if (StringUtils.isBlank(paramName)) {
            String error = GrouperTextContainer.retrieveFromRequest().getText().get("workflowParamsParamNameMissingError");
            error.replace("$$index$$", String.valueOf(i));
            errors.add(error);
          }
          
          if (StringUtils.isBlank(type)) {
            String error = GrouperTextContainer.retrieveFromRequest().getText().get("workflowParamsTypeMissingError");
            error.replace("$$index$$", String.valueOf(i));
            errors.add(error);
          }
          
          if (StringUtils.isBlank(editableInStates)) {
            String error = GrouperTextContainer.retrieveFromRequest().getText().get("workflowParamsEditableInStatesMissingError");
            error.replace("$$index$$", String.valueOf(i));
            errors.add(error);
          }
          
          String required = jsonObject2.containsKey("required") ? jsonObject2.getString("required"): null;
          if (type.equals("checkbox")) {
            if (StringUtils.isNotBlank(required) && (!required.equals("true") || !required.equals("false"))) {
              errors.add(GrouperTextContainer.retrieveFromRequest()
                  .getText().get("workflowParamsInvalidCheckboxValueError"));
            }
          }
          
        }
        
        if (totalParams > 10) {
          errors.add(GrouperTextContainer.retrieveFromRequest()
              .getText().get("workflowParamsExceedsMaxSizeError"));
        }
      }
    } catch (Exception e) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowParamsInvalidJsonError"));
    }
    
    if (StringUtils.isNotBlank(workflowConfigViewersGroupId)) {
      Group viewersGroupId = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), workflowConfigViewersGroupId, false);
      if (viewersGroupId == null) {
        errors.add(GrouperTextContainer.retrieveFromRequest()
            .getText().get("workflowViewerGroupIdNotFoundError"));
      }
    }
    
    
    if (StringUtils.isBlank(workflowConfigEnabled)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigEnabledRequiredError"));
    }
    
    List<String> validEnabledValues = Arrays.asList("true", "false", "noNewSubmissions");
    
    if (!validEnabledValues.contains(workflowConfigEnabled)) {
      errors.add(GrouperTextContainer.retrieveFromRequest()
          .getText().get("workflowConfigEnabledInvalidValueError"));
    }
    
    if (StringUtils.isBlank(workflowConfigForm)) {
      //TODO confirm from Chris
    }
    
    return errors;
    
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
