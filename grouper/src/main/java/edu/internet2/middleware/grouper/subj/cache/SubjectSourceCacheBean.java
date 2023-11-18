/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj.cache;

import java.io.Serializable;
import java.util.List;


/**
 * the whole cache of the subject source
 */
public class SubjectSourceCacheBean implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;


  /**
   * 
   */
  public SubjectSourceCacheBean() {
  }

  /**
   * items in cache
   */
  private List<SubjectSourceCacheItem> subjectSourceCacheItems;

  
  /**
   * @return the subjectSourceCacheItems
   */
  public List<SubjectSourceCacheItem> getSubjectSourceCacheItems() {
    return this.subjectSourceCacheItems;
  }

  
  /**
   * @param subjectSourceCacheItems the subjectSourceCacheItems to set
   */
  public void setSubjectSourceCacheItems(
      List<SubjectSourceCacheItem> subjectSourceCacheItems) {
    this.subjectSourceCacheItems = subjectSourceCacheItems;
  }

  /**
   * when the cache was last stored
   */
  private long cacheLastStored;
  
  /**
   * when the cache was last stored
   * @return the cacheLastStored
   */
  public long getCacheLastStored() {
    return this.cacheLastStored;
  }
  
  /**
   * when the cache was last stored
   * @param cacheLastStored1 the cacheLastStored to set
   */
  public void setCacheLastStored(long cacheLastStored1) {
    this.cacheLastStored = cacheLastStored1;
  }
  
  
}
