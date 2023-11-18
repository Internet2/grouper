package edu.internet2.middleware.grouper.app.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;

public class GrouperWorkflowInstanceValidator {
  
  
  /**
   * validate what user submitted and return errors if any
   * @param paramNamesValues
   * @param state
   * @return
   */
  public List<String> validateFormValues(Map<GrouperWorkflowConfigParam, String> paramNamesValues,
      String state) {
    
    List<String> errors = new ArrayList<String>();
    
    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    for (Map.Entry<GrouperWorkflowConfigParam, String> entry: paramNamesValues.entrySet()) {
      
      GrouperWorkflowConfigParam param = entry.getKey();
      String paramValue = entry.getValue();
      
      if (StringUtils.isBlank(paramValue) && param.isRequired()) {
        String error = contentKeys.get("workflowSubmitFormFieldRequired");
        error = error.replace("$$fieldName$$", param.getParamName());
        errors.add(error);
      }
      
    }
    
    return errors;
    
  }

}
