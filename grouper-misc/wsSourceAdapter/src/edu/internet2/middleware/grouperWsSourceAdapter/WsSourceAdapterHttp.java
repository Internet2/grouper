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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperWsSourceAdapter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;


/**
 *
 */
public class WsSourceAdapterHttp {

  /**
   * 
   * @param url
   * @return the result
   */
  public static String urlGet(String url) {
    HttpClient httpClient = new HttpClient();

    //http://somehost.whatever.org/bsp/contacts/urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7
    GetMethod getMethod = new GetMethod(url);

    try {
      httpClient.executeMethod(getMethod);

      int statusCode = getMethod.getStatusCode();
      
      if (statusCode == 404) {
        throw new WsSourceAdapterNotFound();
      }
      
      if (statusCode != 200) {
        throw new RuntimeException("Status code: " + statusCode);
      }
      
      String response = getMethod.getResponseBodyAsString();
      
      //      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      //      <contacts:bambooContact xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:bsp="http://projectbamboo.org/bsp/resource" xmlns:contacts="http://projectbamboo.org/bsp/services/core/contact" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      //          <dcterms:subject/>
      //          <dcterms:creator xsi:type="dcterms:URI">urn:uuid:99999999-9999-9999-9999-999999999999</dcterms:creator>
      //          <dcterms:created xsi:type="dcterms:W3CDTF">2014-03-17T21:25:36.487Z</dcterms:created>
      //          <bsp:modifier>urn:uuid:99999999-9999-9999-9999-999999999999</bsp:modifier>
      //          <dcterms:modified xsi:type="dcterms:W3CDTF">2014-03-17T21:25:36.488Z</dcterms:modified>
      //          <contacts:contactId>urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7</contacts:contactId>
      //          <contacts:contactNote>Bamboo Person One Contact</contacts:contactNote>
      //          <contacts:emails>
      //              <email>PersonOne@example1.com</email>
      //              <contacts:email>PersonOne@example1.com</contacts:email>
      //          </contacts:emails>
      //          <contacts:displayName>Mr. John Doe</contacts:displayName>
      //          <contacts:partNames>
      //              <contacts:partNameType>HONORIFIC_PREFIX</contacts:partNameType>
      //              <contacts:partNameContent>Mr.</contacts:partNameContent>
      //              <contacts:partNameLang>eng</contacts:partNameLang>
      //          </contacts:partNames>
      //          <contacts:partNames>
      //              <contacts:partNameType>NAME_GIVEN</contacts:partNameType>
      //              <contacts:partNameContent>John</contacts:partNameContent>
      //              <contacts:partNameLang>spa</contacts:partNameLang>
      //          </contacts:partNames>
      //          <contacts:partNames>
      //              <contacts:partNameType>NAME_FAMILY_PATERNAL</contacts:partNameType>
      //              <contacts:partNameContent>Doe</contacts:partNameContent>
      //              <contacts:partNameLang>spa</contacts:partNameLang>
      //          </contacts:partNames>
      //          <contacts:telephones>
      //              <contacts:telephoneType>VOICE</contacts:telephoneType>
      //              <contacts:telephoneNumber>212-555-1212</contacts:telephoneNumber>
      //              <contacts:locationType>HOME</contacts:locationType>
      //          </contacts:telephones>
      //          <contacts:telephones>
      //              <contacts:telephoneType>SMS</contacts:telephoneType>
      //              <contacts:telephoneNumber>999-555-1212</contacts:telephoneNumber>
      //              <contacts:locationType>SABBATICAL</contacts:locationType>
      //          </contacts:telephones>
      //          <contacts:iMs>
      //              <contacts:instantMessagingType>SKYPE</contacts:instantMessagingType>
      //              <contacts:account>PersonOneSkype</contacts:account>
      //              <contacts:locationType>WORK</contacts:locationType>
      //          </contacts:iMs>
      //          <contacts:addresses>
      //              <contacts:streetAddress1>123 Main St.</contacts:streetAddress1>
      //              <contacts:streetAddress2>2nd Fl</contacts:streetAddress2>
      //              <contacts:locality>New York</contacts:locality>
      //              <contacts:region>NY</contacts:region>
      //              <contacts:postalCode>10001</contacts:postalCode>
      //              <contacts:country>USA</contacts:country>
      //              <contacts:locationType>WORK</contacts:locationType>
      //          </contacts:addresses>
      //      </contacts:bambooContact>
      
      //System.out.println(response);
      return response;
      
    } catch (Exception ioe) {
      throw new RuntimeException(ioe);
    }

  }
}
