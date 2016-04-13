/*******************************************************************************
 * Copyright 2016 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.Acknowledge;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AcknowledgeResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMessageAcknowledgeResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;

/**
 * @author vsachdeva
 */
public class WsSampleAcknowledgeMessage implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    acknowledgeMessage(WsSampleGeneratedType.soap);
  }

  /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
  @Override
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    acknowledgeMessage(wsSampleGeneratedType);
  }

  /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
  public static void acknowledgeMessage(WsSampleGeneratedType wsSampleGeneratedType) {
    try {
      //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
      GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
      Options options = stub._getServiceClient().getOptions();
      HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
      auth.setUsername(GeneratedClientSettings.USER);
      auth.setPassword(GeneratedClientSettings.PASS);
      auth.setPreemptiveAuthentication(true);

      options.setProperty(HTTPConstants.AUTHENTICATE, auth);
      options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
      options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
          new Integer(3600000));

      GrouperSession.startRootSession();
      GrouperBuiltinMessagingSystem.createQueue("def");
      GrouperBuiltinMessagingSystem.allowSendToQueue("def", SubjectTestHelper.SUBJ0);
      GrouperBuiltinMessagingSystem.allowReceiveFromQueue("def", SubjectTestHelper.SUBJ0);

      GrouperSession.start(SubjectTestHelper.SUBJ0);

      GrouperMessagingEngine
          .send(new GrouperMessageSendParam().assignQueueOrTopicName("def")
              .addMessageBody("message body")
              .assignQueueType(GrouperMessageQueueType.queue));

      GrouperMessageReceiveResult grouperMessageReceiveResult = GrouperMessagingEngine
          .receive(new GrouperMessageReceiveParam().assignQueueName("def"));

      GrouperMessage grouperMessage = grouperMessageReceiveResult.getGrouperMessages()
          .iterator().next();

      Acknowledge acknowledgeMessage = Acknowledge.class.newInstance();

      //version, e.g. v1_3_000
      acknowledgeMessage.setClientVersion(GeneratedClientSettings.VERSION);
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
      wsSubjectLookup.setSubjectId(SubjectTestHelper.SUBJ0.getId());
      acknowledgeMessage.setActAsSubjectLookup(wsSubjectLookup);
      acknowledgeMessage.setQueueOrTopicName("def");
      acknowledgeMessage.setAcknowledgeType("mark_as_processed");
      acknowledgeMessage.setMessageIds(new String[] { grouperMessage.getId() });

      AcknowledgeResponse acknowledgeResponse = stub.acknowledge(acknowledgeMessage);

      System.out.println(ToStringBuilder.reflectionToString(
          acknowledgeResponse));

      WsMessageAcknowledgeResults wsMessageAcknowledgeResults = acknowledgeResponse
          .get_return();

      if (wsMessageAcknowledgeResults.getQueueOrTopicName().equals("def")) {
        throw new RuntimeException("didnt get correct queue or topic name! ");
      }

      if (!StringUtils.equals("T",
          wsMessageAcknowledgeResults.getResultMetadata().getSuccess())) {
        throw new RuntimeException("didnt get success! ");
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
