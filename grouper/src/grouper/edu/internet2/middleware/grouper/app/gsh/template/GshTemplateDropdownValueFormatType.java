package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public enum GshTemplateDropdownValueFormatType {
  
  
  csv {

    @Override
    public List<MultiKey> retrieveKeysAndLabels(String configuredValueBasedOnType) {
      
      List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
      
      String[] keys = GrouperUtil.splitTrim(configuredValueBasedOnType, ",");
      
      for (String key: keys) {
        key = GrouperUtil.replace(key, "&#x2c;", ",");
        keysAndLabels.add(new MultiKey(key, key));
      }
      
      return keysAndLabels;
    }
  },
  json {

    @Override
    public List<MultiKey> retrieveKeysAndLabels(String configuredValueBasedOnType) {
      
      List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
      
      try {
        ArrayNode arrayNode = (ArrayNode) GrouperUtil.jsonJacksonNode(configuredValueBasedOnType);
        for (int i=0; i<arrayNode.size(); i++) {
          JsonNode keyLabelNode = arrayNode.get(i);
          String key = GrouperUtil.jsonJacksonGetString(keyLabelNode, "key");
          String label = GrouperUtil.jsonJacksonGetString(keyLabelNode, "label");
          keysAndLabels.add(new MultiKey(key, label));
        }
      } catch (Exception e) {
        throw new RuntimeException(configuredValueBasedOnType + " is not a valid json array, e.g. [{key: \"1234\", label: \"Business school\"},{key: \"2345\", label: \"Engineering school\"}]", e);
      }
      
      return keysAndLabels;
    }
  },
  javaclass {


    @Override
    public List<MultiKey> retrieveKeysAndLabels(String configuredValueBasedOnType) {
      
      Class<OptionValueDriver> klass = GrouperUtil.forName(configuredValueBasedOnType);
      OptionValueDriver optionValueDriver = GrouperUtil.newInstance(klass);
      return optionValueDriver.retrieveKeysAndLabels();
    }
  };
  
  public boolean doesValuePassValidation(String valueFromUser, List<MultiKey> validKeyValues) {
    for (MultiKey validKeyValue: validKeyValues) {
      if (StringUtils.equals(valueFromUser, (String)validKeyValue.getKey(0))) {
        return true;
      }
    }
    return false;
  }
  
  public abstract List<MultiKey> retrieveKeysAndLabels(String configuredValueBasedOnType);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static GshTemplateDropdownValueFormatType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateDropdownValueFormatType.class, string, exceptionOnNotFound);
  }

}
