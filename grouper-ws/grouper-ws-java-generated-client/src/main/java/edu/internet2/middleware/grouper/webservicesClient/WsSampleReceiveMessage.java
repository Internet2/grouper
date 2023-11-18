/*******************************************************************************
 * Copyright 2016 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ReceiveMessage;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsMessage;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsMessageResults;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubjectLookup;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;

/**
 * @author vsachdeva
 */
public class WsSampleReceiveMessage implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    receiveMessage(WsSampleGeneratedType.soap);
  }

  /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    receiveMessage(wsSampleGeneratedType);
  }

  /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
  public static void receiveMessage(WsSampleGeneratedType wsSampleGeneratedType) {
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

      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          GrouperBuiltinMessagingSystem.allowSendToQueue("def", grouperSession.getSubject());
          GrouperMessagingEngine.send(new GrouperMessageSendParam().assignQueueOrTopicName("def")
              .addMessageBody("message body").assignQueueType(GrouperMessageQueueType.queue));
          return null;
        }
      });
      
      ReceiveMessage receiveMessage = ReceiveMessage.class.newInstance();

      //version, e.g. v1_3_000
      receiveMessage.setClientVersion(GeneratedClientSettings.VERSION);
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
      wsSubjectLookup.setSubjectId("test.subject.0");
      receiveMessage.setActAsSubjectLookup(wsSubjectLookup);

      receiveMessage.setQueueOrTopicName("def");
      receiveMessage.setMessageSystemName("");
      receiveMessage.setRoutingKey("");
      receiveMessage.setAutocreateObjects("F");
      receiveMessage.setBlockMillis("-1");
      receiveMessage.setMaxMessagesToReceiveAtOnce("-1");
      receiveMessage.setParams(new WsParam[]{});
      
      WsMessageResults wsMessageResults = stub.receiveMessage(receiveMessage).get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsMessageResults));

      WsMessage[] messages = wsMessageResults.getMessages();

      if (messages != null) {
        for (WsMessage msg : messages) {
          System.out.println(ToStringBuilder.reflectionToString(
              msg));
        }
      }
      if (!StringUtils.equals("T",
          wsMessageResults.getResultMetadata().getSuccess())) {
        throw new RuntimeException("didnt get success! ");
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
