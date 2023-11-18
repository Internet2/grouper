package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperWorkflowConfigParams {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowConfigParams.class);

  /**
   * list of params
   */
  private List<GrouperWorkflowConfigParam> params = new ArrayList<GrouperWorkflowConfigParam>();

  
  /**
   * list of params
   * @return
   */
  public List<GrouperWorkflowConfigParam> getParams() {
    return params;
  }

  /**
   * list of params
   * @param params
   */
  public void setParams(List<GrouperWorkflowConfigParam> params) {
    this.params = params;
  }
  
  /**
   * get config param by param name and type
   * @param name
   * @param type
   * @return
   */
  public GrouperWorkflowConfigParam getConfigParamByNameAndType(String name, String type) {
    
    for (GrouperWorkflowConfigParam configParam: params) {
      if (configParam.getParamName().equals(name) && configParam.getType().equals(type)) {
        return configParam;
      }
    }
    
    return null;
  }
  
  /**
   * build params object from json string 
   * @param params
   * @return
   */
  public static GrouperWorkflowConfigParams buildParamsFromJsonString(String params) {

    try {
      GrouperWorkflowConfigParams configParams = GrouperWorkflowSettings.objectMapper
          .readValue(params, GrouperWorkflowConfigParams.class);
      return configParams;
    } catch (Exception e) {
      LOG.error(
          "could not convert: " + params + " to GrouperWorkflowConfigParams object");
      throw new RuntimeException(
          "could not convert json string to GrouperWorkflowConfigParams object", e);
    }

  }
  
  private static GrouperWorkflowConfigParams getDefaultConfigParams() {

    GrouperWorkflowConfigParams configParams = new GrouperWorkflowConfigParams();

    List<GrouperWorkflowConfigParam> params = new ArrayList<GrouperWorkflowConfigParam>();
    GrouperWorkflowConfigParam param1 = new GrouperWorkflowConfigParam();
    param1.setEditableInStates(Arrays.asList(INITIATE_STATE));
    param1.setParamName("notes");
    param1.setType("textarea");
    params.add(param1);

    GrouperWorkflowConfigParam param2 = new GrouperWorkflowConfigParam();
    param2.setEditableInStates(Arrays.asList("groupManager"));
    param2.setParamName("notesForApprovers");
    param2.setType("textarea");
    params.add(param2);

    configParams.setParams(params);

    return configParams;
  }
  
  /**
   * get default config params string
   * @return
   */
  public static String getDefaultConfigParamsString() {
    GrouperWorkflowConfigParams defaultConfigParams = getDefaultConfigParams();
    try {      
      return GrouperWorkflowSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultConfigParams);
    } catch(Exception e) {
      throw new RuntimeException("Could not convert default config params json into string");
    }
  }
  
}
