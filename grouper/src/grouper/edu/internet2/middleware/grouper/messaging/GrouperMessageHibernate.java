/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.messaging;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;

/**
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GrouperMessageHibernate extends GrouperAPI implements GrouperMessage, Hib3GrouperVersioned {

  /** db uuid for this row */
  public static final String COLUMN_ID = "id";

  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** microseconds since 1970 this message was sent (note this is probably unique, but not necessarily) */
  public static final String COLUMN_SENT_TIME_MICROS = "sent_time_micros";

  /** milliseconds since 1970 that the message was attempted to be received */
  public static final String COLUMN_GET_ATTEMPT_TIME_MILLIS = "get_attempt_time_millis";
  
  /** milliseconds since 1970 that the message was attempted to be received */
  public static final String COLUMN_ATTEMPT_TIME_EXPIRES_MILLIS = "attempt_time_expires_millis";
  
  /** how many times this message has been attempted to be retrieved */
  public static final String COLUMN_GET_ATTEMPT_COUNT = "get_attempt_count";
  
  /** state of this message: IN_QUEUE, GET_ATTEMPTED, PROCESSED */
  public static final String COLUMN_STATE = "state";
  
  /** millis since 1970 that this message was successfully received */
  public static final String COLUMN_GET_TIME_MILLIS = "get_time_millis";
  
  /** member id of user who sent the message */
  public static final String COLUMN_FROM_MEMBER_ID = "from_member_id";
  
  /** queue name for the message */
  public static final String COLUMN_QUEUE_NAME = "queue_name";
  
  /** message body */
  public static final String COLUMN_MESSAGE_BODY = "message_body";
  
  
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID);

  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_MESSAGE = "grouper_message";

  /** context id ties multiple db changes */
  private String contextId;

  /** how many times this message has been attempted to be retrieved */
  private Integer getAttemptCount;
  
  /** milliseconds since 1970 that the message was attempted to be received */
  private Long getAttemptTimeMillis;
  
  /** millis since 1970 that this message was successfully received */
  private Long getTimeMillis;

  /**
   * queue name for the message
   */
  private String queueName;

  /**
   * millis since 1970 that this message attempt expires if not sent successfully
   * note this will be reset to null when message sent successfully
   */
  private Long attemptTimeExpiresMillis;
  
  /**
   * millis since 1970 that this message attempt expires if not sent successfully
   * note this will be reset to null when message sent successfully
   * @return the attemptTimeExpiresMillis
   */
  public Long getAttemptTimeExpiresMillis() {
    return this.attemptTimeExpiresMillis;
  }
  
  /**
   * millis since 1970 that this message attempt expires if not sent successfully
   * note this will be reset to null when message sent successfully
   * @param attemptTimeExpiresMillis1 the attemptTimeExpiresMillis to set
   */
  public void setAttemptTimeExpiresMillis(Long attemptTimeExpiresMillis1) {
    this.attemptTimeExpiresMillis = attemptTimeExpiresMillis1;
  }

  /**
   * state of this message: IN_QUEUE, GET_ATTEMPTED, PROCESSED
   */
  private String state;
  
  /**
   * state of this message: IN_QUEUE, GET_ATTEMPTED, PROCESSED
   * @return the state
   */
  public String getState() {
    return this.state;
  }
  
  /**
   * state of this message: IN_QUEUE, GET_ATTEMPTED, PROCESSED
   * @param state1 the state to set
   */
  public void setState(String state1) {
    this.state = state1;
  }

  /**
   * millis since 1970 that this message was sent
   */
  private Long sentTimeMicros;
  
  /**
   * member id of the subject sending from
   */
  private String fromMemberId;

  /** id of this type */
  private String id;

  /**
   * body of message (e.g. the json or encrypted message)
   */
  private String messageBody;

  
  
  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessage#getFromMemberId()
   */
  public String getFromMemberId() {
    return this.fromMemberId;
  }


  /**
   * @return id
   */
  public String getId() {
    return id;
  }


  /**
   * body of message (e.g. the json or encrypted message)
   * @return the messageBody
   */
  public String getMessageBody() {
    return this.messageBody;
  }


  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessage#setFromMemberId(java.lang.String)
   */
  public void setFromMemberId(String fromMemberId1) {
    this.fromMemberId = fromMemberId1;
  }

  /**
   * body of message (e.g. the json or encrypted message)
   * @param messageBody1 the messageBody to set
   */
  public void setMessageBody(String messageBody1) {
    this.messageBody = messageBody1;
  }

  /**
   * millis since 1970 that this message was sent
   * @return the sentTimeMicros
   */
  public Long getSentTimeMicros() {
    return this.sentTimeMicros;
  }
  
  /**
   * millis since 1970 that this message was sent
   * @param sentTimeMicros1 the sentTimeMicros to set
   */
  public void setSentTimeMicros(Long sentTimeMicros1) {
    this.sentTimeMicros = sentTimeMicros1;
  }


  /**
   * queue name for the message
   * @return the queueName
   */
  public String getQueueName() {
    return this.queueName;
  }

  
  /**
   * queue name for the message
   * @param queueName1 the queueName to set
   */
  public void setQueueName(String queueName1) {
    this.queueName = queueName1;
  }

  /**
   * millis since 1970 that this message was successfully received
   * @return the getTimeMillis
   */
  public Long getGetTimeMillis() {
    return this.getTimeMillis;
  }
  
  /**
   * millis since 1970 that this message was successfully received
   * @param getTimeMillis1 the getTimeMillis to set
   */
  public void setGetTimeMillis(Long getTimeMillis1) {
    this.getTimeMillis = getTimeMillis1;
  }

  /**
   * milliseconds since 1970 that the message was attempted to be received
   * @return the getAttemptTimeMillis
   */
  public Long getGetAttemptTimeMillis() {
    return this.getAttemptTimeMillis;
  }
  
  /**
   * milliseconds since 1970 that the message was attempted to be received
   * @param getAttemptTimeMillis1 the getAttemptTimeMillis to set
   */
  public void setGetAttemptTimeMillis(Long getAttemptTimeMillis1) {
    this.getAttemptTimeMillis = getAttemptTimeMillis1;
  }

  /**
   * how many times this message has been attempted to be retrieved
   * @return the getAttemptCount
   */
  public Integer getGetAttemptCount() {
    return this.getAttemptCount;
  }



  
  /**
   * @param getAttemptCount1 the getAttemptCount to set
   */
  public void setGetAttemptCount(Integer getAttemptCount1) {
    this.getAttemptCount = getAttemptCount1;
  }



  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  
  
  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }

  /**
   * set context id
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getMessage().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getMessage().delete(this);
  }

  /**
   * @return queue or topic
   */
  public String getQueueOrTopic() {
    return this.getQueueName();
  }

  /**
   * @param theQueueOrTopic1
   */
  public void setQueueOrTopic(String theQueueOrTopic1) {
    this.setQueueName(theQueueOrTopic1);
  }

}
