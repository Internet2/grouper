package edu.internet2.middleware.grouper.app.messagingProvisioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class MessagingGrouperExternalSystem implements OptionValueDriver {

  public final static Set<String> messagingSystemExternalTypeClassNames = new LinkedHashSet<String>();
  static {
    messagingSystemExternalTypeClassNames.add("edu.internet2.middleware.grouperMessagingActiveMQ.ActiveMqGrouperExternalSystem");
    messagingSystemExternalTypeClassNames.add("edu.internet2.middleware.grouperMessagingRabbitmq.RabbitMqGrouperExternalSystem");
    messagingSystemExternalTypeClassNames.add("edu.internet2.middleware.grouperMessagingAWS.SqsGrouperExternalSystem");
    messagingSystemExternalTypeClassNames.add("edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem");
  }
  
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    
    List<GrouperExternalSystem> externalMessagingSystems = (List<GrouperExternalSystem>) (Object) GrouperExternalSystem.retrieveAllConfigurations(messagingSystemExternalTypeClassNames);
    
    for (GrouperExternalSystem externalMessagingSystem: externalMessagingSystems) {
      
      if (externalMessagingSystem.isEnabled()) {
        String configId = externalMessagingSystem.getConfigId();
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
