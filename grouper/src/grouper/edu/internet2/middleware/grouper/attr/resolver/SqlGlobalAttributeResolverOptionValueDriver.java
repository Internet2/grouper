package edu.internet2.middleware.grouper.attr.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class SqlGlobalAttributeResolverOptionValueDriver implements OptionValueDriver {

  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    
    List<GlobalAttributeResolverConfiguration> resolverConfigs = (List<GlobalAttributeResolverConfiguration>) (Object) new GlobalAttributeResolverConfiguration().listAllConfigurationsOfThisType();
    
    for (GlobalAttributeResolverConfiguration resolverConfig: resolverConfigs) {
      
      if (StringUtils.equals(resolverConfig.retrieveAttributes().get("resolverType").getValueOrExpressionEvaluation(), "sql")) {
        String configId = resolverConfig.getConfigId();
        keysAndLabels.add(new MultiKey(configId, configId));
      }
      
    }
    
    Collections.sort(keysAndLabels, new Comparator<MultiKey>() {

      @Override
      public int compare(MultiKey o1, MultiKey o2) {
        return ((String)o1.getKey(0)).compareTo((String)o2.getKey(0));
      }
    });
    
    return keysAndLabels;
  }

}
