---
title: "Grouper threaded performance improvements"
space: GrIntDev
pageId: 48824681
version: 9
lastUpdated: 2026-07-12T07:02:44.451Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824681/Grouper+threaded+performance+improvements
---

This is a wiki for Grouper developers to explain multi-threaded operations to improve performance.

 

### General information

 There is a GrouperCallable which will automatically manage the GrouperSession in the new thread.

 The GrouperFuture will keep track of the callable so it can be tried again later.

 Its probably a good idea to keep a thread pool size for a task, and making threading optional (-1 means dont use threads?)

 

- Note: this example doesnt use a thread pool (except whatever the Java default is), but it will limit the number of threads use so it is like a thread pool
- In this case, loader jobs, a real thread pool might not be a good idea since either all loader jobs share one thread pool (would be bad), or you create a new pool for each group being synced

 Also, if a thread fails (hopefully due to concurrency issues), it is a good idea to try it again after the threads are done.

 

### Here is an example of threading in the loader

 
```

      final boolean useThreads = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.use.groupThreads", true);

      //see when threads are done processing
      List<GrouperFuture> futures = new ArrayList<GrouperFuture>();

      //if there were thread problems, run those again
      List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();

      int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.groupThreadPoolSize", 20);

      for (final String groupName : groupNamesToSync) {
        
        if (LOG.isDebugEnabled()) {
          LOG.debug(groupNameOverall + ": syncing membership for " + groupName + " " + count + " out of " + groupNamesToSync.size() + " groups");
        }
        
        GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("syncLogicForOneGroup") {

          @Override
          public Void callLogic() {
            syncGroupLogicForOneGroup(grouperLoaderResultsetOverall,
                GrouperSession.staticGrouperSession(), andGroups, groupTypes, hib3GrouploaderLogOverall,
                statusOverall, groupNameToDisplayName, groupNameToDescription, privsToAdd,
                groupStartedMillis, membershipsInRegistry, groupName);
            return null;
          }
        };

        if (!useThreads) {
          grouperCallable.callLogic();
        } else {
          GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
          futures.add(future);
          
          // if there are more threads running that the max, then wait for some to finish until below limit
          GrouperFuture.waitForJob(futures, groupThreadPoolSize, callablesWithProblems);

        }

        count++;
      }

      // wait for the rest
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);

      // if there was a problem in any threads, try them again
      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      

```

 

### Retrying errors

 In the above code, if an error is found, it will be tried again not in a thread. A warning will be logged for each thread that has a problem. If no other errors occur, then everything is fine and ignore it. Here is an example of the warning:

 
```

2014-11-07 08:02:32,590: [main] WARN  GrouperFuture.waitForJob(155) -  - Non fatal problem with callable.  Will try again not in thread
java.lang.RuntimeException: testing,
Problem in job: syncLogicForOneGroup: loader:group1_systemOfRecord
 at edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$10.callLogic(GrouperLoaderType.java:1387)
 at edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$10.callLogic(GrouperLoaderType.java:1)
 at edu.internet2.middleware.grouper.util.GrouperCallable$1.callback(GrouperCallable.java:151)
 at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:974)
 at edu.internet2.middleware.grouper.util.GrouperCallable.callLogicWithSessionIfExists(GrouperCallable.java:148)
 at edu.internet2.middleware.grouper.util.GrouperCallable.call(GrouperCallable.java:121)
 at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
 at java.util.concurrent.FutureTask.run(FutureTask.java:138)
 at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:895)
 at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:918)
 at java.lang.Thread.run(Thread.java:662)

```

 

### Transactional problems

 There are transactional problems with multi-threading. For example in the loader, it tries to process multiple groups at once. And if the parent stems dont exist for those groups, each thread will try to create those. And if they are the same stem, there are issues. This should be handled at the API layer so that the API can handle multithreaded operations in other areas. Not only does this need to be synchronized, but there needs to be some flags so that threads know that another tx is working on creating the resource, and then it should wait a bit for thaat transaction to finish. This is currently implemented for stem creation and member creation.

 First off, synchronize on the exact same objects. In this case we keep a cache (add entried to ehcache.xml), so they arent there forever

 
```

  /**
   * keep a map of strings to synchronize on
   */
  private static GrouperCache<String, String> stemLocksCache = null;
  
  /**
   * stem locks cache
   * @return the cache
   */
  private static GrouperCache<String, String> stemLocksCache() {
    if (stemLocksCache == null) {
      synchronized(Stem.class) {
        if (stemLocksCache == null) {
          stemLocksCache = new GrouperCache<String, String>(Stem.class.getName() + ".stemLocksCache");
        }
      }
    }
    return stemLocksCache;
  }

```

 Then you need to get the lock object

 
```

    if (stem == null) {
      
      synchronized (Stem.class) {
        //get the same key for name
        if (!stemLocksCache().containsKey(stemName)) {
          stemLocksCache().put(stemName, stemName);
        }
        stemName = stemLocksCache().get(stemName);
      }
      final String STEM_NAME = stemName;

      synchronized(stemName) {

```

 Then you need to register if you are creating the object, and check is someone else is in process of creating. If someone else is, you need to check the DB in and out of txs

 
```

        //something created this recently
        if (stemCreatedCache().get(stemName) != null) {

          //wait for the transaction to finish...
          for (int i=0;i<20;i++) {

            //in transaction
            stem = StemFinder.findByName(session.internal_getRootSession(), STEM_NAME, false, new QueryOptions().secondLevelCache(false));
            
            if (stem != null) {
              break;
            }

            //out of transaction
            stem = (Stem)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.NONE, new GrouperTransactionHandler() {
              
              public Object callback(GrouperTransaction grouperTransaction)
                  throws GrouperDAOException {
                return StemFinder.findByName(session.internal_getRootSession(), STEM_NAME, false, new QueryOptions().secondLevelCache(false));
              }
            });
            
            if (stem != null) {
              
              // if its in another transaction... wait a couple seconds for it to finish
              GrouperUtil.sleep(2000);

              break;
            }

            GrouperUtil.sleep(1000);
          }
          
        }
        
        
        if (stem == null) {

          //race conditions
          stemCreatedCache().put(stemName, Boolean.TRUE);

```

 sdf
