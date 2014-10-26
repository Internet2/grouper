package edu.internet2.middleware.grouperClient.jdbc;

import java.util.Iterator;

import edu.internet2.middleware.grouperClient.collections.MultiKey;


/**
 * Map for caching some query results for x amount of time. This map checks itself every X minutes and evicts expired content.
 */
public class GcDbQueryCacheMap extends java.util.HashMap<MultiKey, GcDbQueryCache> {

  /**
   * Serial.
   */
  private static final long serialVersionUID = 8631754376159267786L;

  /**
   * The last time that we evicted.
   */
  private Long lastEvictMillis;

  /**
   * How often to evict - not too often because we are synchronized.
   */
  private Long evictMillis = 1000L * 30 * 1;


  /**
   * Constructor.
   */
  public GcDbQueryCacheMap(){
    // Don't evict until a while after we are instantiated.
    this.lastEvictMillis = System.currentTimeMillis();

    // Start eviction thread.
    Thread janitor = new Thread(new Janitor(this));

    janitor.setDaemon(true);
    
    janitor.start();
    
  }


  /**
   * Associated thread to call get every once in a while to ensure that eviction happens.
   */
  public class Janitor implements Runnable{

    /**
     * The map to call every so often to clear.
     */
    private GcDbQueryCacheMap dbQueryCacheMap;


    /**
     * Constructor.
     * @param _dbQueryCacheMap is the cache to clear.
     */
    public Janitor(GcDbQueryCacheMap _dbQueryCacheMap){
      this.dbQueryCacheMap = _dbQueryCacheMap;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      while (true){
        this.dbQueryCacheMap.get(new MultiKey(null, null));
        try{
          Thread.sleep(1000 * 30);
        } catch (Exception e){
          throw new RuntimeException(e);
        }
      }
    }
  };


  /**
   * Return the object if it is not expired and if it exists.
   * @see java.util.HashMap#get(java.lang.Object)
   */
  @Override
  public synchronized GcDbQueryCache get(Object key) {
    checkEvict();

    GcDbQueryCache dbQueryCache = super.get(key);
    if (dbQueryCache == null){
      return null;
    }
    if (dbQueryCache.expired()){
      this.remove(dbQueryCache);
      return null;
    }
    return dbQueryCache;
  }


  /**
   * Check for things that need to be evicted and evict them.
   */
  private synchronized void checkEvict(){
    if (System.currentTimeMillis() > this.lastEvictMillis + this.evictMillis){
      Iterator<GcDbQueryCache> values = this.values().iterator();
      while (values.hasNext()){
        GcDbQueryCache dbQueryCache = values.next();
        if (dbQueryCache.expired()){
          values.remove();
        }
      }
      this.lastEvictMillis = System.currentTimeMillis();
    }
  }

}
