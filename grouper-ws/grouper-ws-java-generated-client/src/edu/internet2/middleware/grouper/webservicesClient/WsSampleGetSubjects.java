/*******************************************************************************
 * Copyright 2012 Internet2
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
/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetSubjects implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    getSubjects(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getSubjects(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getSubjects(
      WsSampleGeneratedType wsSampleGeneratedType) {
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

      GetSubjects getSubjects = GetSubjects.class.newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      getSubjects.setActAsSubjectLookup(actAsSubject);

      //version, e.g. v1_3_000
      getSubjects.setClientVersion(GeneratedClientSettings.VERSION);
      
      getSubjects.setFieldName("");

      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupLookup.setGroupName("aStem:aGroup");
      getSubjects.setWsGroupLookup(wsGroupLookup);

      getSubjects.setIncludeGroupDetail("");
      getSubjects.setIncludeSubjectDetail("T");
      
      getSubjects.setParams(new WsParam[]{null});
      
      getSubjects.setSearchString("test");
      
      getSubjects.setSourceIds(new String[]{null});
      
      getSubjects.setSubjectAttributeNames(new String[]{null});

      getSubjects.setWsMemberFilter("");
      
      getSubjects.setWsSubjectLookups(new WsSubjectLookup[]{new WsSubjectLookup()});

      WsGetSubjectsResults wsGetSubjectsResults = stub.getSubjects(getSubjects)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsGetSubjectsResults));

      WsSubject[] wsSubjectsResultArray = wsGetSubjectsResults.getWsSubjects();

      for (WsSubject wsSubject : wsSubjectsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsSubject));
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
