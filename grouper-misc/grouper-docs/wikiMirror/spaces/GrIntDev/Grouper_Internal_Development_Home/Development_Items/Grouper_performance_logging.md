---
title: "Grouper performance logging"
space: GrIntDev
pageId: 48792681
version: 6
lastUpdated: 2026-07-12T07:01:12.433Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792681/Grouper+performance+logging
---

In Grouper v2.5.55+ there is a performance logging API to see where time is spent in operations and count queries and access to external systems.

## Use

To enable performance logging

1. Turn on a timer in grouper.properties (see the config for the available timers, the following list is just the starting point)
  
  
  ```
  #####################################
  ## Performance timers
  #####################################
  
  
  # if log performance info on GroupSave
  # set in log4j.properties: log4j.logger.edu.internet2.middleware.grouper.util.PerformanceLogger = INFO
  # {valueType: "boolean", required: true}
  grouper.log.performance.info.on.GroupSave = true
  
  # if log performance info on StemSave
  # set in log4j.properties: log4j.logger.edu.internet2.middleware.grouper.util.PerformanceLogger = INFO
  # {valueType: "boolean", required: true}
  grouper.log.performance.info.on.StemSave = true
  
  # if log performance info on Stem view screen
  # set in log4j.properties: log4j.logger.edu.internet2.middleware.grouper.util.PerformanceLogger = INFO
  # {valueType: "boolean", required: true}
  grouper.log.performance.info.on.StemUiView = true
  
  # if log performance info on Stem more actions
  # set in log4j2.xml: log4j.logger.edu.internet2.middleware.grouper.util.PerformanceLogger = INFO
  # {valueType: "boolean", required: true}
  grouper.log.performance.info.on.StemUiMoreActions = true
  
  # if log performance info on left tree menu
  # set in log4j2.xml: log4j.logger.edu.internet2.middleware.grouper.util.PerformanceLogger = INFO
  # {valueType: "boolean", required: true}
  grouper.log.performance.info.on.UiTreeMenu = true
    
  ```
2. The log4j.properties by default should have this, but if you override, then add to your log4j.properties
  
  
  ```
  log4j.logger.edu.internet2.middleware.grouper.util.PerformanceLogger = INFO
  ```
3. See the logs in the default log file or in another file if you send them elsewhere
  
  
  ```
  2021-08-10 02:57:51,122: [main] INFO  GroupSave.save(978) -  - performanceLogFor: GroupSave, name: test:test_1628578670871, sqlQueries_durationMs: 155, sqlQueries_count: 52, preSaveHook_elapsedMs: 151, changeLog_elapsedMs: 183, postSaveHook_elapsedMs: 185, saveGroup_elapsedMs: 185, groupSets_elapsedMs: 196, member_elapsedMs: 199, defaultPrivs_elapsedMs: 200, attributes_elapsedMs: 200, groupCreateRule_elapsedMs: 221, audit_elapsedMs: 237, insertGroup_elapsedMs: 237, resultType: INSERT, took_elapsedMs: 249
  
  2021-08-10 02:57:51,227: [main] INFO  GroupSave.save(978) -  - performanceLogFor: GroupSave, name: test:test_1628578671195, sqlQueries_durationMs: 30, sqlQueries_count: 25, preSaveHook_elapsedMs: 12, changeLog_elapsedMs: 13, postSaveHook_elapsedMs: 14, saveGroup_elapsedMs: 14, groupSets_elapsedMs: 22, member_elapsedMs: 27, defaultPrivs_elapsedMs: 27, attributes_elapsedMs: 27, groupCreateRule_elapsedMs: 27, audit_elapsedMs: 28, insertGroup_elapsedMs: 28, resultType: INSERT, took_elapsedMs: 32
  
  
  ```
4. Analyze the log file
  
  
  
  | Key | Value | Description |
  | --- | --- | --- |
  | performanceLogFor | GroupSave | Type of performance timer |
  | name | test:test_1628578670871 | Non-numeric values are generally name/value pairs to describe the operation |
  | preSaveHook_elapsedMs | 151 | Labels ending in "elapsedMs" are just timing gates that list how long since the start of the operation that have elapsed.  In this case after 151ms the process is done with pre-hooks for insert |
  | sqlQueries_durationMs | 30 | A duration is when work is timed, and the duration is stored.  If the thing is done more than once, then the sum of the durations is stored |
  | sqlQueries_count | 25 | If a duration is registered more than once, then there will be a _count which lists how many times the duration was registered  In this case there were 25 queries and the sum of all durations of the queries is 30ms. So the queries avg about 1ms per query. |
  | took_elapsedMs | 32 | Total time of the timer |
  
  In this case you see the workflow for a group insert: preSaveHook, changeLog, postSaveHook, saveGroup, groupSets, member, defaultPrivs, attributes, groupCreateRule, audit  
    
  The first row is before things were initted in Grouper so it is taking more time to warm up. The second one you see there are 25 queries and all the time is in queries. It runs queries to make sure things are allowed and if they exist for 12ms, spends 8ms on groupSets, 5ms on member, and 4ms to commit.

## Developing

1. Pick an operation to add a timer to
2. Make up a label, e.g. GroupSave is for the GroupSave chaining class that inserts/updates/deletes groups
3. Make a constant String for that label
  
  
  ```
    /**
     * use this for performance log label
     */
    public static final String PERFORMANCE_LOG_LABEL = "GroupSave";
  
  
  ```
4. Add the label to the grouper.base.properties
5. Wrap the operation like
  
  
  ```
  // one timing gate
  PerformanceLogger.performanceTimingGate(GroupSave.PERFORMANCE_LOG_LABEL, "saveGroup");
  
  
  
  // one duration
  long startNanos = System.nanoTime();
  try {
    // work
  
  } finally {
    PerformanceLogger.performanceTimingDuration(GroupSave.PERFORMANCE_LOG_LABEL, "somethingWs", System.nanoTime()-startNanos);
  }  
  
  // add a duration for any performance timer in use in the thread
  long startNanos = System.nanoTime();
  try { // work} finally {
    PerformanceLogger.performanceTimingAllDuration(PerformanceLogger.PERFORMANCE_LOG_LABEL_SQL, System.nanoTime()-startNanos);
  } 
  
  // end it
  } finally {
    PerformanceLogger.performanceTimingData(GroupSave.PERFORMANCE_LOG_LABEL, "resultType", this.saveResultType);
    if (PerformanceLogger.performanceTimingEnabled(GroupSave.PERFORMANCE_LOG_LABEL)) {
      PerformanceLogger.performanceLog().info(PerformanceLogger.performanceTimingDataResult(GroupSave.PERFORMANCE_LOG_LABEL));
    }
  }
  
  
  ```
6. Use a JSP tag
  
  
  ```
              <grouper:performanceTimingGate label="StemUiView" key="post_viewStemInJsp" />
  ```
