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
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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
 */

package edu.internet2.middleware.ldappc.spml;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.ParseException;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.SchemaEntityRef;

import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.ProvisioningRequest;

/**
 * Basic tests for cli argument processing.
 */
public class PSPOptionsTest extends TestCase {

  public static void main(String[] args) {
    TestRunner.run(PSPOptionsTest.class);
    // TestRunner.run(new PSPOptionsTest("testCalc"));
  }

  public PSPOptionsTest(String name) {
    super(name);
  }

  public void testCalc() throws Exception {

    String[] args = new String[] { "-" + PSPOptions.Mode.calc.getOpt(), "test",
        "-" + PSPOptions.Opts.returnData.getOpt(), "-" + PSPOptions.Opts.targetID.getOpt(), "target1",
        "-" + PSPOptions.Opts.requestID.getOpt(), "REQUEST1" };

    PSPOptions options = new PSPOptions(args);
    options.parseCommandLineOptions();

    List<ProvisioningRequest> requests = options.getRequests();
    assertEquals(1, requests.size());
    CalcRequest request = new CalcRequest();
    request.setId("test");
    request.setReturnData(ReturnData.DATA);
    request.addSchemaEntity(new SchemaEntityRef("target1", null));
    request.setRequestID("REQUEST1");
    assertEquals(request, requests.get(0));
  }

  public void testMissingRequiredID() {

    try {
      PSPOptions options = new PSPOptions(new String[] { "-" + PSPOptions.Mode.calc.getOpt() });
      options.parseCommandLineOptions();
      fail("should throw exception");
    } catch (ParseException e) {
      // OK
      assertTrue(e instanceof MissingArgumentException);
    }

    try {
      PSPOptions options = new PSPOptions(new String[] { "-" + PSPOptions.Mode.diff.getOpt() });
      options.parseCommandLineOptions();
      fail("should throw exception");
    } catch (ParseException e) {
      // OK
      assertTrue(e instanceof MissingArgumentException);
    }

    try {
      PSPOptions options = new PSPOptions(new String[] { "-" + PSPOptions.Mode.sync.getOpt() });
      options.parseCommandLineOptions();
      fail("should throw exception");
    } catch (ParseException e) {
      // OK
      assertTrue(e instanceof MissingArgumentException);
    }

  }

  public void testMissingRequiredOptions() {

    try {
      PSPOptions options = new PSPOptions(null);
      options.parseCommandLineOptions();
      fail("should throw exception");
    } catch (ParseException e) {
      // OK
      assertTrue(e instanceof MissingOptionException);
    }
  }

}
