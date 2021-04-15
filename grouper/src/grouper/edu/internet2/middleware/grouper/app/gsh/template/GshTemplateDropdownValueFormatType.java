package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public enum GshTemplateDropdownValueFormatType {
  
  
  sql {

    @Override
    public List<MultiKey> retrieveKeysAndLabels(GshTemplateInputConfig gshTemplateInputConfig) {
            
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gshTemplateInputConfig.getDropdownSqlDatabase());
      
      String sql = gshTemplateInputConfig.getDropdownSqlValue();
      
      MultiKey cacheKey = new MultiKey(new Object[] {sql});
      
      if (sql.contains("$$subjectId$$")) {
        Subject subject = gshTemplateInputConfig.getGshTemplateConfig().getCurrentUser();
        GrouperUtil.assertion(subject != null, "Current user is null!");
        sql = GrouperUtil.replace(sql, "$$subjectId$$", "?");
        gcDbAccess.addBindVar(subject.getId());
        
        cacheKey = new MultiKey(new Object[] {sql, subject.getId()});
      }
      
      int cacheForMinutes = gshTemplateInputConfig.getDropdownSqlCacheForMinutes();
      
      List<MultiKey> keysAndLabels = null;
      ExpirableCache<MultiKey, List<MultiKey>> dropdownCache = null;
      if (cacheForMinutes > 0) {
        
        dropdownCache = sqlDropdownCache.get(cacheForMinutes);
        if (dropdownCache == null) {
          dropdownCache = new ExpirableCache<MultiKey, List<MultiKey>>(cacheForMinutes);
          sqlDropdownCache.put(cacheForMinutes, dropdownCache);
        }
        
        keysAndLabels = dropdownCache.get(cacheKey);
        
      }

      if (keysAndLabels == null) {
        List<Object[]> keysAndLabelsQuery = gcDbAccess.sql(sql).selectList(Object[].class);
        
        keysAndLabels = new ArrayList<MultiKey>();
        
        for (Object[] keyAndLabel : keysAndLabelsQuery) {
          Object key = keyAndLabel[0];
          Object value = keyAndLabel[keyAndLabel.length == 1 ? 0 : 1];
          MultiKey multiKey = new MultiKey(key, value);
          keysAndLabels.add(multiKey);
        }
        if (dropdownCache != null) {
          dropdownCache.put(cacheKey, keysAndLabels);
        }
      }      
      return keysAndLabels;
    }
  },
  
  csv {

    @Override
    public List<MultiKey> retrieveKeysAndLabels(GshTemplateInputConfig gshTemplateInputConfig) {
      
      List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
      
      String[] keys = GrouperUtil.splitTrim(gshTemplateInputConfig.getDropdownCsvValue(), ",");
      
      for (String key: keys) {
        key = GrouperUtil.replace(key, "&#x2c;", ",");
        keysAndLabels.add(new MultiKey(key, key));
      }
      
      return keysAndLabels;
    }
  },
  json {

    @Override
    public List<MultiKey> retrieveKeysAndLabels(GshTemplateInputConfig gshTemplateInputConfig) {
      
      List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
      
      try {
        ArrayNode arrayNode = (ArrayNode) GrouperUtil.jsonJacksonNode(gshTemplateInputConfig.getDropdownJsonValue());
        for (int i=0; i<arrayNode.size(); i++) {
          JsonNode keyLabelNode = arrayNode.get(i);
          String key = GrouperUtil.jsonJacksonGetString(keyLabelNode, "key");
          String label = GrouperUtil.jsonJacksonGetString(keyLabelNode, "label");
          keysAndLabels.add(new MultiKey(key, label));
        }
      } catch (Exception e) {
        throw new RuntimeException(gshTemplateInputConfig.getDropdownJsonValue() + " is not a valid json array, e.g. [{key: \"1234\", label: \"Business school\"},{key: \"2345\", label: \"Engineering school\"}]", e);
      }
      
      return keysAndLabels;
    }
  },
  javaclass {


    @Override
    public List<MultiKey> retrieveKeysAndLabels(GshTemplateInputConfig gshTemplateInputConfig) {
      
      Class<OptionValueDriver> klass = GrouperUtil.forName(gshTemplateInputConfig.getDropdownJavaClassValue());
      OptionValueDriver optionValueDriver = GrouperUtil.newInstance(klass);
      return optionValueDriver.retrieveKeysAndLabels();
    }
  };
  
  private static Map<Integer, ExpirableCache<MultiKey, List<MultiKey>>> sqlDropdownCache = new HashMap<Integer, ExpirableCache<MultiKey, List<MultiKey>>>();
  
  public boolean doesValuePassValidation(String valueFromUser, List<MultiKey> validKeyValues) {
    for (MultiKey validKeyValue: validKeyValues) {
      if (StringUtils.equals(valueFromUser, (String)validKeyValue.getKey(0))) {
        return true;
      }
    }
    return false;
  }
  
  public abstract List<MultiKey> retrieveKeysAndLabels(GshTemplateInputConfig gshTemplateInputConfig);
  
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
