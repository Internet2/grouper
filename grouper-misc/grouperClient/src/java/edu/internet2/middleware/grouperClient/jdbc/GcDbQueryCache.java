package edu.internet2.middleware.grouperClient.jdbc;


/**
 *
 */
public class GcDbQueryCache {

  /**
   * When we were created.
   */
  private Long instantiatedMillis;
  
  /**
   * When we expire.
   */
  private Long expireMillis;
  
  /**
   * What's being cached.
   */
  private Object thingBeingCached;

  /**
   * Create an object to cache database result objects.
   * @param expireInMinutes is how many minutes this is valid for.
   * @param _thingBeingCached is the thing being cached.
   */
  public GcDbQueryCache(int expireInMinutes, Object _thingBeingCached) {
    super();
    this.instantiatedMillis = System.currentTimeMillis();
    this.expireMillis = this.instantiatedMillis + (expireInMinutes * 1000 * 60);
    this.thingBeingCached = _thingBeingCached;
  }
  
  /**
   * Whether this cache object is expired.
   * @return true if so.
   */
  public boolean expired(){
    return System.currentTimeMillis() > this.expireMillis;
  }

  
  /**
   * @return the thingBeingCached
   */
  public Object getThingBeingCached() {
    return this.thingBeingCached;
  }
  

}
