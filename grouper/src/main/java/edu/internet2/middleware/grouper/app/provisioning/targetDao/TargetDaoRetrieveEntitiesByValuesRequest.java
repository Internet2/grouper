package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.Set;

public class TargetDaoRetrieveEntitiesByValuesRequest {
  
  private Set<Object> searchValues;

  
  public Set<Object> getSearchValues() {
    return searchValues;
  }

  
  public void setSearchValues(Set<Object> searchValues) {
    this.searchValues = searchValues;
  }
  
}
