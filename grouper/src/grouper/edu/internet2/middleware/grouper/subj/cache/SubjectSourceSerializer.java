/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj.cache;

import java.util.Map;


/**
 * serialize a cache to storage
 */
public abstract class SubjectSourceSerializer {

  /**
   * 
   */
  public SubjectSourceSerializer() {
  }

  /**
   * store subject cache to serialization mechanism
   * @param subjectSourceCacheBean
   * @param debugMap if not null add debug info
   */
  public abstract void storeSubjectCache(SubjectSourceCacheBean subjectSourceCacheBean, Map<String, Object> debugMap);
  
  /**
   * store subject cache to serialization mechanism
   * @param newerThanMillis
   * @param debugMap if not null add debug info
   * @return subjectSourceCacheBean
   */
  public abstract SubjectSourceCacheBean retrieveLatestSubjectCache(long newerThanMillis, Map<String, Object> debugMap);
  
  /**
   * if there are old caches that can be safely removed, remove them
   * @param keepNewestIfNewerThanMillis 
   * @param debugMap if not null add debug info
   * @return how many caches removed
   */
  public abstract int cleanupOldSubjectCaches(long keepNewestIfNewerThanMillis, Map<String, Object> debugMap);
  
}
