package edu.internet2.middleware.subject.provider;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Source;

public class SourceManagerOptionValueDriver implements OptionValueDriver {

  public SourceManagerOptionValueDriver() {

  }
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    
    for (Source source: SourceManager.getInstance().getSources()) {
      
      String configId = source.getId();
      keysAndLabels.add(new MultiKey(configId, source.getName() + " (" + source.getId() + ")"));
    }
    
    return keysAndLabels;

  }

}
