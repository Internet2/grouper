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
/**
 * 
 */
package edu.internet2.middleware.grouper.messaging;

import java.util.Collection;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;


/**
 * extend this class and register in the grouper-loader.properties to be a change log consumer
 * @author mchyzer
 *
 */
public abstract class MessagingListenerBase {

  /**
   * process the messages.  mark them as processed
   * @param messageSystemName
   * @param queue
   * @param grouperMessageList  NOTE, DO NOT CHANGE OR EDIT THE OBJECTS IN THIS LIST, THEY MIGHT BE SHARED!
   * @param messagingListenerMetadata
   */
  public abstract void processMessages(String messageSystemName, String queue, Collection<GrouperMessage> grouperMessageList, 
      MessagingListenerMetadata messagingListenerMetadata);
  
}
