/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.subject;

import java.util.Set;


/**
 * page of results
 */
public class SearchPageResult {
  
  /**
   * 
   */
  public SearchPageResult() {
    
  }
  
  
  
  /**
   * @param tooManyResults1
   * @param results1
   */
  public SearchPageResult(boolean tooManyResults1, Set<Subject> results1) {
    super();
    this.tooManyResults = tooManyResults1;
    this.results = results1;
  }



  /** if too many results were found */
  private boolean tooManyResults = false;

  /**
   * results of search
   */
  private Set<Subject> results = null;

  
  /**
   * if too many results were found
   * @return the tooManyResults
   */
  public boolean isTooManyResults() {
    return this.tooManyResults;
  }

  
  /**
   * if too many results were found
   * @param tooManyResults1 the tooManyResults to set
   */
  public void setTooManyResults(boolean tooManyResults1) {
    this.tooManyResults = tooManyResults1;
  }

  
  /**
   * results that were found, might not be all
   * @return the results
   */
  public Set<Subject> getResults() {
    return this.results;
  }

  
  /**
   * results that were found, might not be all
   * @param results1 the results to set
   */
  public void setResults(Set<Subject> results1) {
    this.results = results1;
  }
  
  
  
}
