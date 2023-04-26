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
/*
 * @author mchyzer
 * $Id: RestConverterTest.java,v 1.5 2009-12-16 06:02:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembership;
import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * test the output converter
 */
public class RestConverterTest extends TestCase {

  /**
   * @param name
   */
  public RestConverterTest(String name) {
    super(name);
  }

  /**
   * run a test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RestConverterTest("testMarshal2"));
  }
  
  /**
   * unmarshal a problem string
   */
  public void testMarshal3() {
    
    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
    wsGetMembershipsResults.setWsMemberships(new WsMembership[0]);
    String string = WsRestRequestContentType.json.writeString(wsGetMembershipsResults);
    StringBuilder stringBuilder = new StringBuilder();
    
    //System.out.println(string);

    Object object = WsRestRequestContentType.json.parseString(string, stringBuilder);

    //System.out.println(object);
    
  }
  
}
