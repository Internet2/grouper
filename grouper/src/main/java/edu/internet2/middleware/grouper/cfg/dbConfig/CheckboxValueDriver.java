package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

public interface CheckboxValueDriver {
  
  /**
   * @return list of multikeys. first key is id, second key is name and the third key is should this checkbox be auto selected or not
   */
  List<MultiKey> retrieveCheckboxAttributes();

}
