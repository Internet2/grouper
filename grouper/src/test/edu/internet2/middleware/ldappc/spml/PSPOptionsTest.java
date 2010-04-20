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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.ParseException;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.SchemaEntityRef;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.ProvisioningRequest;

/**
 * Basic tests for cli argument processing.
 */
public class PSPOptionsTest extends TestCase {

  public static void main(String[] args) {
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

  public void testRun() throws Exception {

    File tmpFile = File.createTempFile("PSPOptionsTest", ".tmp");
    tmpFile.deleteOnExit();

    String confDir = GrouperUtil.fileFromResourceName(PSPLdapTest.CONFIG_PATH).getAbsolutePath();

    List<String> cmds = new ArrayList<String>();
    // calc ID=test
    cmds.add("-" + PSPOptions.Mode.calc.getOpt());
    cmds.add("test");

    // tmp file output
    cmds.add("-" + PSPOptions.Opts.output.getOpt());
    cmds.add(tmpFile.getAbsolutePath());

    // conf dir
    cmds.add("-" + PSPOptions.Opts.conf.getOpt());
    cmds.add(confDir);

    // request id
    cmds.add("-" + PSPOptions.Opts.requestID);
    cmds.add("REQUEST1");

    // returnData = data
    cmds.add("-" + PSPOptions.Opts.returnData.getOpt());

    // print requests
    cmds.add("-" + PSPOptions.Opts.printRequests.getOpt());

    // updated since
    cmds.add("-" + PSPOptions.Opts.lastModifyTime.getOpt());
    cmds.add("2010-04-14_15:23:53");

    String[] args = cmds.toArray(new String[] {});

    PSPOptions options = new PSPOptions(args);
    options.parseCommandLineOptions();

    PSPCLI.main(args);

    String correct = "<ldappc:calcRequest xmlns:ldappc='http://grouper.internet2.edu/ldappc' requestID='REQUEST1' returnData='data'>"
        + System.getProperty("line.separator")
        + "  <ldappc:id ID='test'/>"
        + System.getProperty("line.separator")
        + "</ldappc:calcRequest>"
        + System.getProperty("line.separator")
        + "<ldappc:calcResponse xmlns:ldappc='http://grouper.internet2.edu/ldappc' status='failure' requestID='REQUEST1' error='noSuchIdentifier'>"
        + System.getProperty("line.separator")
        + "  <errorMessage>Unable to calculate provisioned object.</errorMessage>"
        + System.getProperty("line.separator")
        + "  <ldappc:id ID='test'/>"
        + System.getProperty("line.separator")
        + "</ldappc:calcResponse>"
        + System.getProperty("line.separator");

    String actual = GrouperUtil.readFileIntoString(tmpFile);

    assertEquals(correct, actual);

    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2010);
    calendar.set(Calendar.MONTH, Calendar.APRIL);
    calendar.set(Calendar.DAY_OF_MONTH, 14);
    calendar.set(Calendar.HOUR_OF_DAY, 15);
    calendar.set(Calendar.MINUTE, 23);
    calendar.set(Calendar.SECOND, 53);
    calendar.set(Calendar.MILLISECOND, 0);

    // assertEquals(calendar.getTime(), options.getLastModifyTime());
  }
}
