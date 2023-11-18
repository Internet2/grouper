package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class ProvisioningGroupCacheDropdownOptions extends ProvisioningGroupAttributeWithCacheDropdownOptions {

  
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    // alpha order
    Set<String> options = new TreeSet<String>();
    options.add("groupAttributeValueCache0");
    options.add("groupAttributeValueCache1");
    options.add("groupAttributeValueCache2");
    options.add("groupAttributeValueCache3");
    
    List<MultiKey> result = new ArrayList<MultiKey>();
    for (String option : options) {
      result.add(new MultiKey(option, option));
    }

    return result;
  }
}
