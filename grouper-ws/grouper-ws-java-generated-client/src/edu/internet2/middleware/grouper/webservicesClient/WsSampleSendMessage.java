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

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.SendMessage;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessageResults;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsSubjectLookup;

/**
 * @author vsachdeva
 */
public class WsSampleSendMessage implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    sendMessage(WsSampleGeneratedType.soap);
  }

  /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    sendMessage(wsSampleGeneratedType);
  }

  /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
  public static void sendMessage(WsSampleGeneratedType wsSampleGeneratedType) {
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

      SendMessage sendMessage = SendMessage.class.newInstance();

      //version, e.g. v1_3_000
      sendMessage.setClientVersion(GeneratedClientSettings.VERSION);
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
      wsSubjectLookup.setSubjectId("test.subject.0");
      sendMessage.setActAsSubjectLookup(wsSubjectLookup);

      WsMessage wsMessage1 = new WsMessage();
      wsMessage1.setMessageBody("Test message body");

      WsMessage wsMessage2 = new WsMessage();
      wsMessage2.setMessageBody("Test another message body");

      sendMessage.setMessages(new WsMessage[] { wsMessage1, wsMessage2 });
      sendMessage.setQueueOrTopicName("def");
      sendMessage.setQueueType("queue");

      WsMessageResults wsMessageResults = stub.sendMessage(sendMessage).get_return();

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
