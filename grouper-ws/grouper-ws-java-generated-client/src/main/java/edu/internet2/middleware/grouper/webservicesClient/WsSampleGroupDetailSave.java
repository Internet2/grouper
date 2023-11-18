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
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GroupSave;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGroupDetailSave implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    groupDetailSave(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    groupDetailSave(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void groupDetailSave(WsSampleGeneratedType wsSampleGeneratedType) {
    try {
      //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
      GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);

      HttpClientParams.getDefaultParams().setParameter(
          HttpClientParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
      
      Options options = stub._getServiceClient().getOptions();
      HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
      auth.setUsername(GeneratedClientSettings.USER);
      auth.setPassword(GeneratedClientSettings.PASS);
      auth.setPreemptiveAuthentication(true);

      options.setProperty(HTTPConstants.AUTHENTICATE, auth);
      options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
      options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(3600000));

      GroupSave groupSave = GroupSave.class.newInstance();

      //version, e.g. v1_3_000
      groupSave.setClientVersion(GeneratedClientSettings.VERSION);

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      groupSave.setActAsSubjectLookup(actAsSubject);

      WsGroupToSave wsGroupToSaveLeft= new WsGroupToSave();
      WsGroupLookup leftGroupLookup = new WsGroupLookup();
      leftGroupLookup.setGroupName("aStem:whateverGroupLeft");
      wsGroupToSaveLeft.setWsGroupLookup(leftGroupLookup);
      WsGroup wsGroupLeft = new WsGroup();
      wsGroupLeft.setDescription("whateverGroupLeftDesc");
      wsGroupLeft.setDisplayExtension("whateverGroupLeftDispExt");
      wsGroupLeft.setExtension("whateverGroupLeft");
      wsGroupLeft.setName("aStem:whateverGroupLeft");
      wsGroupToSaveLeft.setWsGroup(wsGroupLeft);

      WsGroupToSave wsGroupToSaveRight= new WsGroupToSave();
      WsGroupLookup rightGroupLookup = new WsGroupLookup();
      rightGroupLookup.setGroupName("aStem:whateverGroupRight");
      wsGroupToSaveRight.setWsGroupLookup(rightGroupLookup);
      WsGroup wsGroupRight = new WsGroup();
      wsGroupRight.setDescription("whateverGroupRightDesc");
      wsGroupRight.setDisplayExtension("whateverGroupRightDispExt");
      wsGroupRight.setExtension("whateverGroupRight");
      wsGroupRight.setName("aStem:whateverGroupRight");
      wsGroupToSaveRight.setWsGroup(wsGroupRight);
      
      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupLookup.setGroupName("aStem:whateverGroup");
      wsGroupToSave.setWsGroupLookup(wsGroupLookup);
      WsGroup wsGroup = new WsGroup();
      wsGroup.setDescription("desc1");
      wsGroup.setDisplayExtension("disp1");
      wsGroup.setExtension("whateverGroup");
      wsGroup.setName("aStem:whateverGroup");
      wsGroupToSave.setWsGroup(wsGroup);
      groupSave.setIncludeGroupDetail("T");
      groupSave.setTxType("");

      
      WsGroupDetail wsGroupDetail = new WsGroupDetail();
      wsGroup.setDetail(wsGroupDetail);
      wsGroupDetail.setCompositeType("intersection");
      wsGroupDetail.setHasComposite("T");
      wsGroupDetail.setLeftGroup(wsGroupLeft);
      wsGroupDetail.setRightGroup(wsGroupRight);
      wsGroupDetail.setTypeNames(new String[]{"aType", "aType2"});
      wsGroupDetail.setAttributeNames(new String[]{"attr_1", "attr2_1"});
      wsGroupDetail.setAttributeValues(new String[]{"val_1", "val2_1"});

      WsGroupToSave[] wsGroupToSaves = new WsGroupToSave[] {wsGroupToSaveLeft, wsGroupToSaveRight, wsGroupToSave};
      groupSave.setWsGroupToSaves(wsGroupToSaves);
      
      WsGroupSaveResults wsGroupSaveResults = stub.groupSave(groupSave).get_return();
      System.out.println(ToStringBuilder.reflectionToString(wsGroupSaveResults));

      WsGroupSaveResult[] wsGroupSaveResultArray = wsGroupSaveResults.getResults();

      for (WsGroupSaveResult wsGroupSaveResult : GeneratedClientSettings
          .nonNull(wsGroupSaveResultArray)) {
        System.out.println(ToStringBuilder.reflectionToString(wsGroupSaveResult));
      }
      if (!StringUtils.equals("T", wsGroupSaveResults.getResultMetadata().getSuccess())) {
        throw new RuntimeException("didnt get success! ");
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
