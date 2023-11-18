/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * subject source cache stats for a day
 */
public class SubjectSourceCacheStat {

  /**
   * e.g. yyyy_mm_dd
   */
  private String date;

  /**
   * e.g. yyyy_mm_dd
   * @return the date
   */
  public String getDate() {
    return this.date;
  }
  
  /**
   * e.g. yyyy_mm_dd
   * @param date the date to set
   */
  public void setDate(String date) {
    this.date = date;
  }

  /**
   * 
   */
  public SubjectSourceCacheStat() {
  }

  /**
   * cache hits since last retrieve
   */
  private int cacheHitsSinceLastRetrieve;
  
  /**
   * cache hits since last retrieve
   * @return the cacheHitsSinceLastRetrieve
   */
  public int getCacheHitsSinceLastRetrieve() {
    return this.cacheHitsSinceLastRetrieve;
  }
  
  /**
   * cache hits since last retrieve
   * @param cacheHitsSinceLastRetrieve the cacheHitsSinceLastRetrieve to set
   */
  public void setCacheHitsSinceLastRetrieve(int cacheHitsSinceLastRetrieve) {
    this.cacheHitsSinceLastRetrieve = cacheHitsSinceLastRetrieve;
  }

  /**
   * add up all the refreshes of all items in cache
   */
  private long cacheRefreshesTotalOfItemsInCache;
  
  
  /**
   * add up all the refreshes of all items in cache
   * @return the cacheRefreshesTotalOfItemsInCache
   */
  public long getCacheRefreshesTotalOfItemsInCache() {
    return this.cacheRefreshesTotalOfItemsInCache;
  }

  
  /**
   * add up all the refreshes of all items in cache
   * @param cacheRefreshesTotalOfItemsInCache the cacheRefreshesTotalOfItemsInCache to set
   */
  public void setCacheRefreshesTotalOfItemsInCache(long cacheRefreshesTotalOfItemsInCache) {
    this.cacheRefreshesTotalOfItemsInCache = cacheRefreshesTotalOfItemsInCache;
  }

  /**
   * total number of hits for items in cache
   */
  private int cacheHitsTotalOfItemsInCache;
  
  /**
   * total number of hits for items in cache
   * @return the cacheHitsTotalOfItemsInCache
   */
  public int getCacheHitsTotalOfItemsInCache() {
    return this.cacheHitsTotalOfItemsInCache;
  }

  /**
   * @param cacheHitsTotalOfItemsInCache the cacheHitsTotalOfItemsInCache to set
   */
  public void setCacheHitsTotalOfItemsInCache(int cacheHitsTotalOfItemsInCache) {
    this.cacheHitsTotalOfItemsInCache = cacheHitsTotalOfItemsInCache;
  }

  /** 
   * count of cache hits 
   */
  private int cacheHit;
  
  /**
   * count of cache hits 
   * @return the cacheHit
   */
  public int getCacheHit() {
    return this.cacheHit;
  }

  /**
   * add cache hits
   * @param numberToAdd
   */
  public synchronized void cacheHitAdd(int numberToAdd) {
    this.cacheHit+=numberToAdd;
  }
  
  /**
   * retrieve a subject that was resolved
   */
  private int cacheMissResolved;
  
  /**
   * retrieve a subject that was resolved
   * @return the cacheMissResolved
   */
  public int getCacheMissResolved() {
    return this.cacheMissResolved;
  }
  
  /**
   * add missed resolved subjects
   * @param numberToAdd
   */
  public synchronized void cacheMissResolvedAdd(int numberToAdd) {
    this.cacheMissResolved+=numberToAdd;
  }
  
  /**
   * cache miss unresolved
   */
  private int cacheMissUnresolved;
  
  /**
   * cache miss unresolved
   * @return the cacheMissUnresolved
   */
  public int getCacheMissUnresolved() {
    return this.cacheMissUnresolved;
  }

  /**
   * add missed unresolved subjects
   * @param numberToAdd
   */
  public synchronized void cacheMissUnresolvedAdd(int numberToAdd) {
    this.cacheMissUnresolved+=numberToAdd;
  }
  

  /**
   * count of cache misses for individual queries
   */
  private int cacheMissIndividual;

  /**
   * count of cache misses for individual queries
   * @return the cacheMissIndividual
   */
  public int getCacheMissIndividual() {
    return this.cacheMissIndividual;
  }
  
  /**
   * add cache individual misses
   * @param numberToAdd
   */
  public synchronized void cacheMissIndividualAdd(int numberToAdd) {
    this.cacheMissIndividual+=numberToAdd;
  }
  
  /**
   * count of cache misses when getting a list (note, some might be found in list)
   */
  private int cacheMissList;


  /**
   * count of cache misses when getting a list (note, some might be found in list)
   * @return the cacheMissList
   */
  public int getCacheMissList() {
    return this.cacheMissList;
  }

  /**
   * add cache list misses
   * @param numberToAdd
   */
  public synchronized void cacheMissListAdd(int numberToAdd) {
    this.cacheMissList+=numberToAdd;
  }
  
  /**
   * time it takes for all cache hits
   */
  private long cacheHitNanos;

  /**
   * @return the cacheHitNanos
   */
  public long getCacheHitNanos() {
    return this.cacheHitNanos;
  }

  /**
   * add cache hit nanos
   * @param numberToAdd
   */
  public synchronized void cacheHitNanosAdd(long numberToAdd) {
    this.cacheHitNanos+=numberToAdd;
  }

  /**
   * time it takes for all individual cache misses
   */
  private long cacheMissIndividualNanos;

  /**
   * time it takes for all individual cache misses
   * @return the cacheMissIndividualNanos
   */
  public long getCacheMissIndividualNanos() {
    return this.cacheMissIndividualNanos;
  }

  /**
   * add miss individual nanos
   * @param numberToAdd
   */
  public synchronized void cacheMissIndividualNanosAdd(long numberToAdd) {
    this.cacheMissIndividualNanos+=numberToAdd;
  }

  /**
   * time it takes for cache miss list queries
   */
  private long cacheMissListNanos;

  /**
   * time it takes for cache miss list queries
   * @return the cacheMissListNanos
   */
  public long getCacheMissListNanos() {
    return this.cacheMissListNanos;
  }

  /**
   * add miss list nanos
   * @param numberToAdd
   */
  public synchronized void cacheMissListNanosAdd(long numberToAdd) {
    this.cacheMissListNanos+=numberToAdd;
  }

  /**
   * how many background subject update queries
   */
  private int refreshQueryCountInBackground;

  /**
   * how many background subject update queries
   * @return the refreshQueryCountInBackground
   */
  public int getRefreshQueryCountInBackground() {
    return this.refreshQueryCountInBackground;
  }

  /**
   * add refresh query count background nanos
   * @param numberToAdd
   */
  public synchronized void refreshQueryCountInBackgroundAdd(long numberToAdd) {
    this.refreshQueryCountInBackground+=numberToAdd;
  }

  /**
   * count of subjects refreshed in background
   */
  private long refreshSubjectCount;
  
  /**
   * @return the refreshSubjectCount
   */
  public long getRefreshSubjectCount() {
    return this.refreshSubjectCount;
  }

  /**
   * add refresh subject count
   * @param numberToAdd
   */
  public synchronized void refreshSubjectCountAdd(long numberToAdd) {
    this.refreshSubjectCount+=numberToAdd;
  }

  /**
   * time spent refreshing in backgound
   */
  private long refreshInBackgroundNanos;
  
  
  /**
   * @return the refreshInBackgroundNanos
   */
  public long getRefreshInBackgroundNanos() {
    return this.refreshInBackgroundNanos;
  }

  /**
   * add refresh background nanos
   * @param numberToAdd
   */
  public synchronized void refreshInBackgroundNanosAdd(long numberToAdd) {
    this.refreshInBackgroundNanos+=numberToAdd;
  }
  
  /**
   * number of items removed from cache
   */
  private int cacheRemove;

  
  /**
   * number of items removed from cache
   * @return the cacheRemove
   */
  public int getCacheRemove() {
    return this.cacheRemove;
  }

  /**
   * number of items removed from cache
   * @param numberToAdd
   */
  public synchronized void cacheRemoveAdd(long numberToAdd) {
    this.cacheRemove+=numberToAdd;
  }
  
  /**
   * items in cache with no accesses since last refresh
   */
  private int itemsWithNoAccessSinceLastRefresh;

  /**
   * items in cache with no accesses since last refresh
   * @return the itemsWithNoAccessSinceLastRefresh
   */
  public int getItemsWithNoAccessSinceLastRefresh() {
    return this.itemsWithNoAccessSinceLastRefresh;
  }

  
  /**
   * items in cache with no accesses since last refresh
   * @param itemsWithNoAccessSinceLastRefresh the itemsWithNoAccessSinceLastRefresh to set
   */
  public void setItemsWithNoAccessSinceLastRefresh(int itemsWithNoAccessSinceLastRefresh) {
    this.itemsWithNoAccessSinceLastRefresh = itemsWithNoAccessSinceLastRefresh;
  }

  /**
   * items with 1 refresh
   */
  private int itemsWith1Refresh;

  
  /**
   * items with 1 refresh
   * @return the itemsWith1Refresh
   */
  public int getItemsWith1Refresh() {
    return this.itemsWith1Refresh;
  }

  
  /**
   * items with 1 refresh
   * @param itemsWith1Refresh1 the itemsWith1Refresh to set
   */
  public void setItemsWith1Refresh(int itemsWith1Refresh1) {
    this.itemsWith1Refresh = itemsWith1Refresh1;
  }

  
  /**
   * items with 3 refresh
   */
  private int itemsWith3Refresh;

  /**
   * items with 3 refresh
   * @return the itemsWith3Refresh
   */
  public int getItemsWith3Refresh() {
    return this.itemsWith3Refresh;
  }
  
  /**
   * items with 3 refresh
   * @param itemsWith3Refresh the itemsWith3Refresh to set
   */
  public void setItemsWith3Refresh(int itemsWith3Refresh) {
    this.itemsWith3Refresh = itemsWith3Refresh;
  }

  /**
   * items with 10 refresh
   */
  private int itemsWith10Refresh;
  
  /**
   * items with 10 refresh
   * @return the itemsWith10Refresh
   */
  public int getItemsWith10Refresh() {
    return this.itemsWith10Refresh;
  }
  
  /**
   * items with 10 refresh
   * @param itemsWith10Refresh the itemsWith10Refresh to set
   */
  public void setItemsWith10Refresh(int itemsWith10Refresh) {
    this.itemsWith10Refresh = itemsWith10Refresh;
  }


  /**
   * items with 20 refresh
   */
  private int itemsWith20Refresh;

  
  /**
   * items with 20 refresh
   * @return the itemsWith20Refresh
   */
  public int getItemsWith20Refresh() {
    return this.itemsWith20Refresh;
  }

  
  /**
   * items with 20 refresh
   * @param itemsWith20Refresh the itemsWith20Refresh to set
   */
  public void setItemsWith20Refresh(int itemsWith20Refresh) {
    this.itemsWith20Refresh = itemsWith20Refresh;
  }

  /**
   * items with 50 refresh
   */
  private int itemsWith50Refresh;
  
  /**
   * items with 50 refresh
   * @return the itemsWith50Refresh
   */
  public int getItemsWith50Refresh() {
    return this.itemsWith50Refresh;
  }
  
  /**
   * items with 50 refresh
   * @param itemsWith50Refresh the itemsWith50Refresh to set
   */
  public void setItemsWith50Refresh(int itemsWith50Refresh) {
    this.itemsWith50Refresh = itemsWith50Refresh;
  }
  
  /**
   * items in cache with 1 access total
   */
  private int itemsWith1AccessTotal;
  
  /**
   * items in cache with 1 access total
   * @return the itemsWith1AccessTotal
   */
  public int getItemsWith1AccessTotal() {
    return this.itemsWith1AccessTotal;
  }
  
  /**
   * items in cache with 1 access total
   * @param itemsWith1AccessTotal the itemsWith1AccessTotal to set
   */
  public void setItemsWith1AccessTotal(int itemsWith1AccessTotal) {
    this.itemsWith1AccessTotal = itemsWith1AccessTotal;
  }

  /**
   * items in cache with 3 access total
   */
  private int itemsWith3AccessTotal;
  
  /**
   * items in cache with 1 access total
   * @return the itemsWith3AccessTotal
   */
  public int getItemsWith3AccessTotal() {
    return this.itemsWith3AccessTotal;
  }

  
  /**
   * @param itemsWith3AccessTotal the itemsWith3AccessTotal to set
   */
  public void setItemsWith3AccessTotal(int itemsWith3AccessTotal) {
    this.itemsWith3AccessTotal = itemsWith3AccessTotal;
  }

  /**
   * items in cache with 10 access total
   */
  private int itemsWith10AccessTotal;

  
  
  /**
   * @return the itemsWith10AccessTotal
   */
  public int getItemsWith10AccessTotal() {
    return this.itemsWith10AccessTotal;
  }

  
  /**
   * @param itemsWith10AccessTotal the itemsWith10AccessTotal to set
   */
  public void setItemsWith10AccessTotal(int itemsWith10AccessTotal) {
    this.itemsWith10AccessTotal = itemsWith10AccessTotal;
  }

  /**
   * items in cache with 20 access total
   */
  private int itemsWith20AccessTotal;

  /**
   * @return the itemsWith20AccessTotal
   */
  public int getItemsWith20AccessTotal() {
    return this.itemsWith20AccessTotal;
  }

  
  /**
   * @param itemsWith20AccessTotal the itemsWith20AccessTotal to set
   */
  public void setItemsWith20AccessTotal(int itemsWith20AccessTotal) {
    this.itemsWith20AccessTotal = itemsWith20AccessTotal;
  }


  /**
   * items in cache with 50 access total
   */
  private int itemsWith50AccessTotal;
  
    
  /**
   * @return the itemsWith50AccessTotal
   */
  public int getItemsWith50AccessTotal() {
    return this.itemsWith50AccessTotal;
  }

  
  /**
   * @param itemsWith50AccessTotal the itemsWith50AccessTotal to set
   */
  public void setItemsWith50AccessTotal(int itemsWith50AccessTotal) {
    this.itemsWith50AccessTotal = itemsWith50AccessTotal;
  }

  /**
   * items in cache with 1 accesses since last refresh
   */
  private int itemsWith1AccessSinceLastRefresh;

  /**
   * items in cache with 1 accesses since last refresh
   * @return the itemsWith1AccessSinceLastRefresh
   */
  public int getItemsWith1AccessSinceLastRefresh() {
    return this.itemsWith1AccessSinceLastRefresh;
  }
  
  /**
   * items in cache with 1 accesses since last refresh
   * @param itemsWith1AccessSinceLastRefresh the itemsWith1AccessSinceLastRefresh to set
   */
  public void setItemsWith1AccessSinceLastRefresh(int itemsWith1AccessSinceLastRefresh) {
    this.itemsWith1AccessSinceLastRefresh = itemsWith1AccessSinceLastRefresh;
  }

  /**
   * items in cache with 3 accesses since last refresh
   */
  private int itemsWith3AccessSinceLastRefresh;
  
  /**
   * items in cache with 3 accesses since last refresh
   * @return the itemsWith3AccessSinceLastRefresh
   */
  public int getItemsWith3AccessSinceLastRefresh() {
    return this.itemsWith3AccessSinceLastRefresh;
  }
  
  /**
   * items in cache with 3 accesses since last refresh
   * @param itemsWith3AccessSinceLastRefresh the itemsWith3AccessSinceLastRefresh to set
   */
  public void setItemsWith3AccessSinceLastRefresh(int itemsWith3AccessSinceLastRefresh) {
    this.itemsWith3AccessSinceLastRefresh = itemsWith3AccessSinceLastRefresh;
  }

  /**
   * items in cache with 10 accesses since last refresh
   */
  private int itemsWith10AccessSinceLastRefresh;

  /**
   * items in cache with 10 accesses since last refresh
   * @return the itemsWith10AccessSinceLastRefresh
   */
  public int getItemsWith10AccessSinceLastRefresh() {
    return this.itemsWith10AccessSinceLastRefresh;
  }
  
  /**
   * items in cache with 10 accesses since last refresh
   * @param itemsWith10AccessSinceLastRefresh1 the itemsWith10AccessSinceLastRefresh to set
   */
  public void setItemsWith10AccessSinceLastRefresh(int itemsWith10AccessSinceLastRefresh1) {
    this.itemsWith10AccessSinceLastRefresh = itemsWith10AccessSinceLastRefresh1;
  }

  /**
   * items in cache with 20 accesses since last refresh
   */
  private int itemsWith20AccessSinceLastRefresh;
  
  /**
   * items in cache with 20 accesses since last refresh
   * @return the itemsWith20AccessSinceLastRefresh
   */
  public int getItemsWith20AccessSinceLastRefresh() {
    return this.itemsWith20AccessSinceLastRefresh;
  }
  
  /**
   * items in cache with 20 accesses since last refresh
   * @param itemsWith20AccessSinceLastRefresh1 the itemsWith20AccessSinceLastRefresh to set
   */
  public void setItemsWith20AccessSinceLastRefresh(int itemsWith20AccessSinceLastRefresh1) {
    this.itemsWith20AccessSinceLastRefresh = itemsWith20AccessSinceLastRefresh1;
  }

  /**
   * size of cache
   */
  private int cacheSizeSubjects;

  
  /**
   * size of cache
   * @return the cacheSizeSubjects
   */
  public int getCacheSizeSubjects() {
    return this.cacheSizeSubjects;
  }

  
  /**
   * size of cache
   * @param cacheSizeSubjects the cacheSizeSubjects to set
   */
  public void setCacheSizeSubjects(int cacheSizeSubjects) {
    this.cacheSizeSubjects = cacheSizeSubjects;
  }

  /**
   * size of cache Resolved
   */
  private int cacheSizeSubjectsResolved;

  
  /**
   * size of cache Resolved
   * @return the cacheSizeSubjects
   */
  public int getCacheSizeSubjectsResolved() {
    return this.cacheSizeSubjectsResolved;
  }

  
  /**
   * size of cache Resolved
   * @param cacheSizeSubjectsResolved the cacheSizeSubjects to set
   */
  public void setCacheSizeSubjectsResolved(int cacheSizeSubjectsResolved) {
    this.cacheSizeSubjectsResolved = cacheSizeSubjectsResolved;
  }

  /**
   * size of cache Unresolved
   */
  private int cacheSizeSubjectsUnresolved;

  
  /**
   * size of cache Unresolved
   * @return the cacheSizeSubjects
   */
  public int getCacheSizeSubjectsUnresolved() {
    return this.cacheSizeSubjectsUnresolved;
  }

  /**
   * size of cache Unresolved
   * @param cacheSizeSubjectsUnresolved the cacheSizeSubjects to set
   */
  public void setCacheSizeSubjectsUnresolved(int cacheSizeSubjectsUnresolved) {
    this.cacheSizeSubjectsUnresolved = cacheSizeSubjectsUnresolved;
  }

  /**
   * number of lookups for cache
   */
  private int cacheSizeLookups;
  
  /**
   * number of lookups for cache
   * @return the cacheSizeLookups
   */
  public int getCacheSizeLookups() {
    return this.cacheSizeLookups;
  }

  
  /**
   * number of lookups for cache
   * @param cacheSizeLookups the cacheSizeLookups to set
   */
  public void setCacheSizeLookups(int cacheSizeLookups) {
    this.cacheSizeLookups = cacheSizeLookups;
  }
  
  /**
   * generate the stats line for this stat
   * @return the stats line
   */
  public String statsLine() {
    
    Map<String, Object> stats = new LinkedHashMap<String, Object>();
    
    // date : String
    stats.put("date", this.date);
    
    //  cacheSizeSubjects : int
    stats.put("subjectsInCache", this.cacheSizeSubjects);
    //  cacheSizeLookups : int
    stats.put("cacheKeysIdsIdentifiers", this.cacheSizeLookups);

    //  cacheHit : int
    stats.put("cacheHitsIndividual", this.cacheHit);
    //  cacheHitNanos : long
    stats.put("microsPerCacheHit", (int)(((1.0d * this.cacheHitNanos) / this.cacheHit) / 1000));

    //  cacheMissIndividual : int
    stats.put("cacheMissesIndividual", this.cacheMissIndividual);
    //  cacheMissIndividualNanos : long
    stats.put("microsPerCacheMissIndividual", (int)(((1.0d * this.cacheMissIndividualNanos) / this.cacheMissIndividual) / 1000));

    //  cacheMissList : int
    stats.put("cacheMissesForList", this.cacheMissList);
    //  cacheMissListNanos : long
    stats.put("microsPerCacheMissForList", (int)(((1.0d * this.cacheMissListNanos) / this.cacheMissList) / 1000));

    //  cacheMissResolved : int
    stats.put("cacheMissesResolved", this.cacheMissResolved);
    //  cacheMissUnresolved : int
    stats.put("cacheMissesUnresolved", this.cacheMissUnresolved);

    //  cacheRemove : int
    stats.put("cacheRemoves", this.cacheRemove);

    //  cacheHitsSinceLastRetrieve : int
    stats.put("cacheHitsSinceLastRetrieve", this.cacheHitsSinceLastRetrieve);
    //  cacheHitsTotalOfItemsInCache : int
    stats.put("cacheHitsTotalOfItemsInCache", this.cacheHitsTotalOfItemsInCache);
    //  cacheSizeSubjectsResolved : int
    stats.put("cacheSizeSubjectsResolved", this.cacheSizeSubjectsResolved);
    //  cacheSizeSubjectsUnresolved : int
    stats.put("cacheSizeSubjectsUnresolved", this.cacheSizeSubjectsUnresolved);

    //  refreshSubjectCount : long
    stats.put("numberOfSubjectsRefreshedInBackground", this.refreshSubjectCount);
    //  refreshQueryCountInBackground : int
    stats.put("refreshQueriesInBackground", this.refreshQueryCountInBackground);
    //  refreshInBackgroundNanos : long
    stats.put("microsPerBackgroundRefreshQuery", (int)(((1.0d * this.refreshInBackgroundNanos) / this.refreshQueryCountInBackground) / 1000));
    // cacheRefreshesTotalOfItemsInCache : long
    stats.put("cacheRefreshesTotalOfItemsInCache", this.cacheRefreshesTotalOfItemsInCache);
    
    //  itemsWithNoAccessSinceLastRefresh : int
    stats.put("itemsWithNoAccessSinceLastRefresh", this.itemsWithNoAccessSinceLastRefresh);
    //  itemsWith1AccessSinceLastRefresh : int
    stats.put("itemsWith1AccessSinceLastRefresh", this.itemsWith1AccessSinceLastRefresh);
    //  itemsWith3AccessSinceLastRefresh : int
    stats.put("itemsWith3AccessSinceLastRefresh", this.itemsWith3AccessSinceLastRefresh);
    //  itemsWith10AccessSinceLastRefresh : int
    stats.put("itemsWith10AccessSinceLastRefresh", this.itemsWith10AccessSinceLastRefresh);
    //  itemsWith20AccessSinceLastRefresh : int
    stats.put("itemsWith20AccessSinceLastRefresh", this.itemsWith20AccessSinceLastRefresh);
  
    //  itemsWith1Refresh : int
    stats.put("itemsWith1Refresh", this.itemsWith1Refresh);
    //  itemsWith3Refresh : int
    stats.put("itemsWith3Refresh", this.itemsWith3Refresh);
    //  itemsWith10Refresh : int
    stats.put("itemsWith10Refresh", this.itemsWith10Refresh);
    //  itemsWith20Refresh : int
    stats.put("itemsWith20Refresh", this.itemsWith20Refresh);
    //  itemsWith50Refresh : int
    stats.put("itemsWith50Refresh", this.itemsWith50Refresh);
  
    //  itemsWith1AccessTotal : int
    stats.put("itemsWith1AccessTotal", this.itemsWith1AccessTotal);
    //  itemsWith3AccessTotal : int
    stats.put("itemsWith3AccessTotal", this.itemsWith3AccessTotal);
    //  itemsWith10AccessTotal : int
    stats.put("itemsWith10AccessTotal", this.itemsWith10AccessTotal);
    //  itemsWith20AccessTotal : int
    stats.put("itemsWith20AccessTotal", this.itemsWith20AccessTotal);
    //  itemsWith50AccessTotal : int
    stats.put("itemsWith50AccessTotal", this.itemsWith50AccessTotal);
    
    return GrouperUtil.mapToString(stats);
  }
}
