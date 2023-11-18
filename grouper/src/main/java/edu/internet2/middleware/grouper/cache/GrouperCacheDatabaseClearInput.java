package edu.internet2.middleware.grouper.cache;

/**
 * parameter to cache clear
 * @author mchyzer
 *
 */
public class GrouperCacheDatabaseClearInput {

  public GrouperCacheDatabaseClearInput() {

  }
  /**
   * send cache name if this cache is called by prefix
   */
  private String cacheName;

  /**
   * send cache name if this cache is called by prefix
   * @return
   */
  public String getCacheName() {
    return cacheName;
  }

  /**
   * send cache name if this cache is called by prefix
   * @param cacheName
   */
  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

}
