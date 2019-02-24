package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class FullSyncQueueItem {
  private static Logger LOG = LoggerFactory.getLogger(FullSyncQueueItem.class);

  // This is used to give each queue item a unique number (at least for the current daemon run)
  private static final AtomicInteger queueItemCounter = new AtomicInteger();

  final public static String ID_PROPERTY = "id";
  int id;

  // What provisioner is this message targeting?
  final public static String PROVISIONER_NAME_PROPERTY = "provisioner_name";
  final String provisionerName;

  // Source of the full-sync request
  final public static String SOURCE_PROPERTY = "source";
  String source;

  // Why is the full-sync requested
  final public static String REASON_PROPERTY = "reason";
  String reason;

  // An external reference for logging or reply correlation
  final public static String EXTERNAL_REFERENCE = "external_reference";
  String externalReference;

  // how many times has this full-sync request been processed
  final public static String RETRY_COUNT_PROPERTY = "retry_count";
  int retryCount=0;

  // How old can the cached information be used. Defaults to firstQueuedDate
  final public static String ASOF_JEPOCH_PROPERTY = "asof_jepoch";
  DateTime asofDate = new DateTime();

  // When was this request first brought into grouper? This stays the same across Retries
  final public static String FIRST_QUEUED_JEPOCH_PROPERTY = "first_queued_jepoch";
  DateTime firstQueuedDate = new DateTime();


  // When was this request most recently queued or requeued
  final public static String MOST_RECENT_QUEUED_JEPOCH_PROPERTY = "most_recent_queued_jepoch";
  DateTime mostRecentQueuedDate = new DateTime();

  // When was this object pulled out of a queue. This allows us to
  // calculate how long object spent in queue, even after it's been in memory for a while
  transient DateTime mostRecentDequeueTime = null;

  // Should this request wait for a while before being processed. This is used to
  // implement an 'exponential' backoff as retry_count increases
  final public static String WAKE_TIME_JEPOCH_PROPERTY = "wake_time_jepoch";
  DateTime wakeTimeDate=null;

  transient JobStatistics stats = new JobStatistics();

  // Should this FullSyncQueueItem be sent to a queue when processed
  final public static String REPLY_QUEUE = "reply_queue";
  String replyQueue;

  // Was this request processed successfully
  final public static String WAS_SUCCESSFUL_PROPERTY="was_successful";
  Boolean wasSuccessful;

  // Will this (unsuccessful) request be retried
  final public static String WILL_BE_RETRIED_PROPERTY="will_be_retried";
  Boolean willBeRetried;

  // what acknowledgement to pass to messaging when we've handled this group
  // This comes from the GrouperMessagingSystem
  transient GrouperMessageAcknowledgeParam messageToAcknowledge;

  // What queue brought this event into Grouper
  transient FullSyncProvisioner.QUEUE_TYPE sourceQueue;

  // What Full-sync command should be processed
  final public static String COMMAND_PROPERTY="command";
  FullSyncProvisioner.FULL_SYNC_COMMAND command;

  // group to process (valid for FULL_SYNC_GROUP-command type)
  final public static String GROUP_PROPERTY = "group";
  String groupName;

  // What happened when the group was processed
  final public static String PROCESSING_RESULT_MESSAGE_PROPERTY = "processing_result_message";
  String processingResultMessage;


  public FullSyncQueueItem(String provisionerName, FullSyncProvisioner.FULL_SYNC_COMMAND command, String reason)
  {
    this.id = queueItemCounter.incrementAndGet();
    this.command=command;
    this.provisionerName = provisionerName;
    this.reason=reason;
    messageToAcknowledge=null;
  }

  public FullSyncQueueItem(String provisionerName, String groupName, String reason) {
      this(provisionerName, FullSyncProvisioner.FULL_SYNC_COMMAND.FULL_SYNC_GROUP, groupName, reason, null);
  }

  public FullSyncQueueItem(String provisionerName, FullSyncProvisioner.FULL_SYNC_COMMAND command,
                           String groupName, String reason,
                           GrouperMessageAcknowledgeParam messageToAcknowledge) {
      this.id = queueItemCounter.incrementAndGet();
      this.command= command;
      this.provisionerName = provisionerName;
      this.groupName = groupName;
      this.reason = reason;
      this.messageToAcknowledge = messageToAcknowledge;
  }


  public static FullSyncQueueItem fromJson(String provisionerName, String jsonString) {
    // Old message format: just the group's name or all-groups (TODO)
    if ( !jsonString.startsWith("{") ) {
      return new FullSyncQueueItem(
              provisionerName,
              jsonString,
              "from old-format message");
    }

    JSONObject jsonObject = JSONObject.fromObject(jsonString);

    String commandString = jsonObject.getString(COMMAND_PROPERTY);

    if ( commandString==null )
    {
      LOG.error("{}: Json of FullSyncQueueItem did not include {}: {}", provisionerName, COMMAND_PROPERTY, jsonString);
      return null;
    }


    String reason = jsonObject.getString(REASON_PROPERTY);

    FullSyncQueueItem result = new FullSyncQueueItem(
            provisionerName,
            FullSyncProvisioner.FULL_SYNC_COMMAND.valueOf(commandString),
            reason);


    // Fill in the details
    if ( jsonObject.containsKey(ID_PROPERTY) )
      result.id = jsonObject.getInt(ID_PROPERTY);

    if ( jsonObject.containsKey(SOURCE_PROPERTY) )
      result.source = jsonObject.getString(SOURCE_PROPERTY);
    if ( jsonObject.containsKey(EXTERNAL_REFERENCE) )
      result.externalReference = jsonObject.getString(EXTERNAL_REFERENCE);

    if ( jsonObject.containsKey(GROUP_PROPERTY) )
      result.groupName = jsonObject.getString(GROUP_PROPERTY);

    if ( jsonObject.containsKey(FIRST_QUEUED_JEPOCH_PROPERTY) )
      result.firstQueuedDate = new DateTime(jsonObject.getLong(FIRST_QUEUED_JEPOCH_PROPERTY));

    if ( jsonObject.containsKey(MOST_RECENT_QUEUED_JEPOCH_PROPERTY) )
      result.mostRecentQueuedDate = new DateTime(jsonObject.getLong(MOST_RECENT_QUEUED_JEPOCH_PROPERTY));

    if ( jsonObject.containsKey(ASOF_JEPOCH_PROPERTY) )
      result.asofDate = new DateTime(jsonObject.getLong(ASOF_JEPOCH_PROPERTY));

    if ( jsonObject.containsKey(WAS_SUCCESSFUL_PROPERTY) )
      result.wasSuccessful = jsonObject.getBoolean(WAS_SUCCESSFUL_PROPERTY);

    if ( jsonObject.containsKey(WILL_BE_RETRIED_PROPERTY) )
      result.willBeRetried = jsonObject.getBoolean(WILL_BE_RETRIED_PROPERTY);

    if ( jsonObject.containsKey(REPLY_QUEUE) )
      result.replyQueue = jsonObject.getString(REPLY_QUEUE);

    if ( jsonObject.containsKey(RETRY_COUNT_PROPERTY) )
      result.retryCount = jsonObject.getInt(RETRY_COUNT_PROPERTY);

    if ( jsonObject.containsKey(WAKE_TIME_JEPOCH_PROPERTY) )
      result.wakeTimeDate = new DateTime(jsonObject.getLong(WAKE_TIME_JEPOCH_PROPERTY));

    if ( jsonObject.containsKey(PROCESSING_RESULT_MESSAGE_PROPERTY) )
      result.processingResultMessage = jsonObject.getString(PROCESSING_RESULT_MESSAGE_PROPERTY);

    if ( result.command== FullSyncProvisioner.FULL_SYNC_COMMAND.FULL_SYNC_GROUP &&
            result.groupName == null ) {
      LOG.error("{}: Json of Group-command FullSyncQueueItem did not include {}: {}", provisionerName, GROUP_PROPERTY, jsonString);
      return null;
    }

    return result;
  }

  public String toJson() {
    // Manually building map that will be JSONed. Jackson could do this, but that isn't
    // included in grouper right now, and I don't want to add such a dependency in this upcoming
    // patch.
    // Also, net.sf.json seems to be bean-specific and not able to JSONify general objects,
    // nor to control the attribute names.
    Map<String, Object> result = new HashMap<>();

    result.put(ID_PROPERTY, id);
    result.put(PROVISIONER_NAME_PROPERTY, provisionerName);
    result.put(COMMAND_PROPERTY, command.name());

    result.put(SOURCE_PROPERTY, source);
    result.put(EXTERNAL_REFERENCE, externalReference);
    result.put(REASON_PROPERTY, reason);

    result.put(GROUP_PROPERTY, groupName);

    result.put(FIRST_QUEUED_JEPOCH_PROPERTY, firstQueuedDate.getMillis());
    result.put(ASOF_JEPOCH_PROPERTY, asofDate.getMillis());
    result.put(MOST_RECENT_QUEUED_JEPOCH_PROPERTY, mostRecentQueuedDate.getMillis());

    if ( wakeTimeDate!=null ) {
      result.put(WAKE_TIME_JEPOCH_PROPERTY, wakeTimeDate.getMillis());
    }

    if ( wasSuccessful!= null ) {
      result.put(WAS_SUCCESSFUL_PROPERTY, wasSuccessful ? "true" : "false");
    }

    result.put(REPLY_QUEUE, replyQueue);
    result.put(RETRY_COUNT_PROPERTY, retryCount);

    if ( willBeRetried!=null ) {
      result.put(WILL_BE_RETRIED_PROPERTY, willBeRetried ? "true" : "false");
    }

    result.put(PROCESSING_RESULT_MESSAGE_PROPERTY, processingResultMessage);

    // This jsonutils invocation wraps the result in a "<Class>": <result> envelope
    // which gets in my way
    // return JsonUtils.jsonConvertTo(result);
    // So this is a partial copy of the jsonConvertTo method...

    JsonConfig jsonConfig = new JsonConfig();
    jsonConfig.setJsonPropertyFilter(new PropertyFilter() {
      public boolean apply(Object source, String name, Object value) {
        return value == null;
      }
    });
    JSONObject jsonObject = JSONObject.fromObject(result, jsonConfig);
    String json = jsonObject.toString();
    return json;
  }


  public void wasDequeued() {
    mostRecentDequeueTime=DateTime.now();
  }


  public void processingCompletedSuccessfully() {
      // Only act the first time we're told this was completed
      if ( stats.processingCompletedTime != null ) {
          return;
      }

      processingResultMessage="Successful";
      stats.processingCompletedTime = new Date();
      wasSuccessful = true;

      acknowledgeMessage();
  }

  public void processingCompletedUnsuccessfully(boolean willBeRetried, String messageFormat, Object... messageArgs) {
    // Only act the first time we're told this was completed
    if ( stats.processingCompletedTime != null ) {
        return;
    }
    this.willBeRetried=willBeRetried;
    processingResultMessage = String.format(messageFormat, messageArgs);

    stats.processingCompletedTime = new Date();
    wasSuccessful = false;
    acknowledgeMessage();
  }

  public void acknowledgeMessage() {
    if ( messageToAcknowledge != null ) {
      LOG.info("Acknowledging that message was processed: {}", this);
      GrouperMessagingEngine.acknowledge(messageToAcknowledge);
    }
  }

  public boolean hasBeenProcessed() {
  return stats.processingCompletedTime != null;
}

  public Duration getTimeSpentInQueue() {
    if ( mostRecentDequeueTime==null ) {
      return new Duration(mostRecentQueuedDate, Instant.now());
    } else {
      return new Duration(mostRecentQueuedDate, mostRecentDequeueTime);
    }
}

  public Duration getTimeSinceFirstQueued() {
    return new Duration(firstQueuedDate, Instant.now());
  }

  public String getName() {
    switch (command) {
      case FULL_SYNC_GROUP:
        return groupName;
      case CLEANUP:
        return "Extra-Group Cleanup";
      case FULL_SYNC_ALL_GROUPS:
        return "All-Groups";
    }
    return "Unknown-command-"+command.name();
  }

  public String toString() {
      return String.format("%s|qid=%d|Trigger=%s|ExternalRef=%s|QTime=%.1f secs|Age=%.1f secs",
              getName(), id, reason, externalReference,
              getTimeSpentInQueue().getMillis()/1000.0,
              getTimeSinceFirstQueued().getMillis()/1000.0);
  }

  public void incrementRetryCount() {
    retryCount++;
    mostRecentQueuedDate = new DateTime();

    // Push event out 10 seconds more each retry, up to 15minutes
    int sleepTime_secs = Math.min(10*retryCount, 900);

    wakeTimeDate = new DateTime().withDurationAdded(1000*sleepTime_secs, 1);
  }
}
