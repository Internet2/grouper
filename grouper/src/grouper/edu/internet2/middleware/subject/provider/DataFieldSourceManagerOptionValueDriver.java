package edu.internet2.middleware.subject.provider;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.subj.GrouperDataFieldSourceAdapter;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Source;

public class DataFieldSourceManagerOptionValueDriver implements OptionValueDriver {
  
  public DataFieldSourceManagerOptionValueDriver() {

  }
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    
    for (Source source: SourceManager.getInstance().getSources()) {
      
      if (source instanceof GrouperDataFieldSourceAdapter) {
        String configId = source.getId();
        keysAndLabels.add(new MultiKey(configId, source.getName() + " (" + source.getId() + ")"));
      }
    }
    
    return keysAndLabels;

  }
}
