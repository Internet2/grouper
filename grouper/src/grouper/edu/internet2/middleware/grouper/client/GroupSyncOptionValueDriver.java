/**
 * 
 */
package edu.internet2.middleware.grouper.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientGroupConfigBean;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GroupSyncOptionValueDriver implements OptionValueDriver {

  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();

    Map<String, ClientGroupConfigBean> clientGroupConfigBeanMap = ClientConfig.clientGroupConfigBeanCache();
    
    for (ClientGroupConfigBean clientGroupConfigBean : clientGroupConfigBeanMap.values()) {      
      keysAndLabels.add(new MultiKey(clientGroupConfigBean.getConfigId(), clientGroupConfigBean.getConfigId()));
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
