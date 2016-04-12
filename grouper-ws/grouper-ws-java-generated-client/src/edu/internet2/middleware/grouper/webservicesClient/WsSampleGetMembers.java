/*******************************************************************************
 * Copyright 2012 Internet2
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
/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedUtils;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetMembers;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGetMembersResult;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsResultMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetMembers implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    getMembersPenn(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getMembers(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getMembers(WsSampleGeneratedType wsSampleGeneratedType) {
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

      GetMembers getMembers = GetMembers.class.newInstance();

      //version, e.g. v1_3_000
      getMembers.setClientVersion(GeneratedClientSettings.VERSION);

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      getMembers.setActAsSubjectLookup(actAsSubject);

      WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
      wsGroupLookup.setGroupName("aStem:aGroup");

      WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
      getMembers.setWsGroupLookups(wsGroupLookups);

      getMembers.setFieldName("");
      getMembers.setMemberFilter("All");
      getMembers.setIncludeGroupDetail("F");
      getMembers.setIncludeSubjectDetail("T");
      getMembers.setSubjectAttributeNames(new String[] { "a", "name" });
      WsGetMembersResults wsGetMembersResults = stub.getMembers(getMembers)
                                                          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
                    wsGetMembersResults));

      WsGetMembersResult[] wsGetMemberResults = wsGetMembersResults.getResults();
      int i = 0;

      for (WsGetMembersResult wsGetMembersResult : GeneratedUtils.nonNull(
                    wsGetMemberResults)) {
        WsResultMeta resultMetadata = wsGetMembersResult.getResultMetadata();
        System.out.println("Result: " + i++ + ": code: " +
                    resultMetadata.getResultCode());

        WsSubject[] wsSubjectArray = wsGetMembersResult.getWsSubjects();

        for (WsSubject wsSubject : GeneratedUtils.nonNull(
                        wsSubjectArray)) {
          System.out.println(ToStringBuilder.reflectionToString(
                            wsSubject));
        }
      }

      if (!StringUtils.equals("T",
                wsGetMembersResults.getResultMetadata().getSuccess())) {
        throw new RuntimeException("didnt get success! ");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getMembersPenn(WsSampleGeneratedType wsSampleGeneratedType) {
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

      GetMembers getMembers = GetMembers.class.newInstance();

      //version, e.g. v1_3_000
      getMembers.setClientVersion(GeneratedClientSettings.VERSION);

      WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
      wsGroupLookup.setGroupName("test:testGroup");

      WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
      getMembers.setWsGroupLookups(wsGroupLookups);

      getMembers.setMemberFilter("All");
      getMembers.setIncludeGroupDetail("F");
      getMembers.setIncludeSubjectDetail("T");
      getMembers.setSubjectAttributeNames(new String[] { "a", "name" });
      WsGetMembersResults wsGetMembersResults = stub.getMembers(getMembers)
                                                          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
                    wsGetMembersResults));

      WsGetMembersResult[] wsGetMemberResults = wsGetMembersResults.getResults();
      int i = 0;

      for (WsGetMembersResult wsGetMembersResult : GeneratedUtils.nonNull(
                    wsGetMemberResults)) {
        WsResultMeta resultMetadata = wsGetMembersResult.getResultMetadata();
        System.out.println("Result: " + i++ + ": code: " +
                    resultMetadata.getResultCode());

        WsSubject[] wsSubjectArray = wsGetMembersResult.getWsSubjects();

        for (WsSubject wsSubject : GeneratedUtils.nonNull(
                        wsSubjectArray)) {
          System.out.println(ToStringBuilder.reflectionToString(
                            wsSubject));
        }
      }

      if (!StringUtils.equals("T",
                wsGetMembersResults.getResultMetadata().getSuccess())) {
        throw new RuntimeException("didnt get success! ");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
