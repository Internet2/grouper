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

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetMemberships;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsMembership;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetMemberships implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    getMemberships(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getMemberships(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getMemberships(
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

      GetMemberships getMemberships = GetMemberships.class.newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      getMemberships.setActAsSubjectLookup(actAsSubject);

      //version, e.g. v1_3_000
      getMemberships.setClientVersion(GeneratedClientSettings.VERSION);
      
      getMemberships.setEnabled("T");

      getMemberships.setFieldName("");
      getMemberships.setIncludeGroupDetail("");
      getMemberships.setIncludeSubjectDetail("T");
      
      getMemberships.setMembershipIds(new String[]{null});
      
      getMemberships.setParams(new WsParam[]{null});
      
      getMemberships.setScope("");
      getMemberships.setSourceIds(new String[]{null});
      getMemberships.setStemScope("");
      
      getMemberships.setSubjectAttributeNames(new String[]{null});

      WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
      wsGroupLookup.setGroupName("test:testGroup");
      getMemberships.setWsGroupLookups(new WsGroupLookup[]{wsGroupLookup});

      getMemberships.setWsMemberFilter("Immediate");
      
      getMemberships.setWsSubjectLookups(new WsSubjectLookup[]{null});
      getMemberships.setWsStemLookup(new WsStemLookup());
      
      getMemberships.setPointInTimeFrom("2019/11/01 10:10:10.000");
      
//      --pageSize=1 --pageNumber=1 --sortString=displayName --ascending=true",
//      --pageSizeForMember=1 --pageNumberForMember=1 --sortStringForMember=name --ascendingForMember=true
//      getMemberships.setPageSize("1");
//      getMemberships.setPageNumber("1");
//      getMemberships.setSortString("displayName");
//      getMemberships.setAscending("true");

//      getMemberships.setPageSizeForMember("1");
//      getMemberships.setPageNumberForMember("1");
//      getMemberships.setSortStringForMember("name");
//      getMemberships.setAscendingForMember("true");
      
      WsGetMembershipsResults wsGetMembershipsResults = stub.getMemberships(getMemberships)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsGetMembershipsResults));

      WsMembership[] wsMembershipsResultArray = wsGetMembershipsResults.getWsMemberships();

      for (WsMembership wsMembership : wsMembershipsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsMembership));
      }
      
      WsGroup[] wsGroupsResultArray = wsGetMembershipsResults.getWsGroups();

      for (WsGroup wsGroup : wsGroupsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsGroup));
      }

      WsSubject[] wsSubjectsResultArray = wsGetMembershipsResults.getWsSubjects();

      for (WsSubject wsSubject : wsSubjectsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsSubject));
      }

      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
