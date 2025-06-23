package edu.internet2.middleware.grouper.app.subectSource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.subj.GrouperDataFieldSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class DataFieldSubjectSourceConfiguration extends SubjectSourceConfiguration {
  
  
  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    } 
    
    GrouperConfigurationModuleAttribute numberOfAttributes = this.retrieveAttributes().get("numberOfAttributes");
    
    int numberOfAttributesLength = 0;
    
    Set<Integer> priorities = new HashSet<Integer>();
    
    if (numberOfAttributes != null) {
      
      numberOfAttributesLength = GrouperUtil.intValue(numberOfAttributes.getValueOrExpressionEvaluationValue(), 0);
      
      for (int i=0; i<numberOfAttributesLength; i++) {
        
        GrouperConfigurationModuleAttribute attributeName = this.retrieveAttributes().get("attribute."+i+".privacyPriority");
        String privacyPriorityString = attributeName.getValueOrExpressionEvaluationValue();
        if (StringUtils.isNotBlank(privacyPriorityString)) {
          Integer privacyPriority = Integer.valueOf(privacyPriorityString);
          if (priorities.contains(privacyPriority)) {
            String errorMessage = GrouperTextContainer.textOrNull("grouperDataFieldSubjectSourceDuplicatePrivacyPriorityNotAllowed");
            errorsToDisplay.add(errorMessage);
            return;
          }
        }
      }
    }
    
    int size = priorities.size();
    for (int i = 1; i <= size; i++) {
      if (!priorities.contains(i)) {
        // Missing a number in the sequence
        String errorMessage = GrouperTextContainer.textOrNull("grouperDataFieldSubjectSourceMissingPriority");
        errorMessage = errorMessage.replace("$$priority$$", String.valueOf(i));
        errorsToDisplay.add(errorMessage);
        return;
      }
    }
    
  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.SUBJECT_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "subjectApi.source." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(subjectApi.source)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "adapterClass";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperDataFieldSourceAdapter.class.getName();
  }

}
