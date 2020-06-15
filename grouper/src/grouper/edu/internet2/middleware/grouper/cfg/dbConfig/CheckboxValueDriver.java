package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

public interface CheckboxValueDriver {
  
  List<MultiKey> retrieveCheckboxAttributes();

}
