package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;


public class SubjectContainer {

  /**
   * get sources to pick which source
   * @return the sources
   */
  public Set<Source> getSources() {
    
    //we could cache this at some point
    Collection<Source> sources = SourceManager.getInstance().getSources();
    
    return new LinkedHashSet<Source>(sources);
  }
  
}
