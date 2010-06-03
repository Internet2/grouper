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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.BaseProvisioningTest;

/**
 * Basic test for the PSP CLI.
 */
public class PSPCLITest extends BaseProvisioningTest {

  public static final String CONFIG_PATH = TEST_PATH + "/spml";

  public static final String DATA_PATH = CONFIG_PATH + "/data/";

  /** Command line arguments. */
  private List<String> cmds;

  /** Output file. */
  private File tmpFile;

  public static void main(String[] args) {
    TestRunner.run(PSPCLITest.class);
    // TestRunner.run(new PSPCLITest("testInterval"));
  }

  public PSPCLITest(String name) {
    super(name, CONFIG_PATH);
  }

  public void setUp() {

    super.setUp();

    // use a callback to make sure there are no threadlocal sessions
    GrouperSession.callbackGrouperSession(GrouperSession.startRootSession(), new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        Stem edu = StemFinder.findRootStem(grouperSession).addChildStem("edu", "education");
        Group groupA = edu.addChildGroup("groupA", "Group A");

        Group groupB = edu.addChildGroup("groupB", "Group B");
        groupB.addMember(groupA.toSubject());
        groupB.setDescription("descriptionB");
        groupB.store();
        grouperSession.stop();

        return null;
      }
    });

    // verify no threadlocal sessions, so we can test psp session instantiation
    if (GrouperSession.staticGrouperSession(false) != null) {
      fail("A threadlocal session already exists");
    }

    try {
      tmpFile = File.createTempFile("PSPOptionsTest", ".tmp");
    } catch (IOException e) {
      e.printStackTrace();
      fail("An exception occurred : " + e);
    }
    tmpFile.deleteOnExit();

    String confDir = GrouperUtil.fileFromResourceName(PSPLdapTest.CONFIG_PATH).getAbsolutePath();

    cmds = new ArrayList<String>();

    // tmp file output
    cmds.add("-" + PSPOptions.Opts.output.getOpt());
    cmds.add(tmpFile.getAbsolutePath());

    // conf dir
    cmds.add("-" + PSPOptions.Opts.conf.getOpt());
    cmds.add(confDir);

    // request id
    cmds.add("-" + PSPOptions.Opts.requestID);
    cmds.add("REQUEST1");

    // returnData = everything
    cmds.add("-" + PSPOptions.Opts.returnEverything.getOpt());

    // print requests
    cmds.add("-" + PSPOptions.Opts.printRequests.getOpt());

    // updated since
    cmds.add("-" + PSPOptions.Opts.lastModifyTime.getOpt());
    cmds.add("2010-04-14_15:23:53");
  }

  public void testNotFound() throws Exception {

    // calc ID=test
    cmds.add("-" + PSPOptions.Mode.calc.getOpt());
    cmds.add("test");

    String[] args = cmds.toArray(new String[] {});

    PSPCLI.main(args);

    String correct = "<ldappc:calcRequest xmlns:ldappc='http://grouper.internet2.edu/ldappc' requestID='REQUEST1' returnData='everything'>"
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
  }

  public void testCalc() {

    // calc ID=edu:groupB
    cmds.add("-" + PSPOptions.Mode.calc.getOpt());
    cmds.add("edu:groupB");

    String[] args = cmds.toArray(new String[] {});

    PSPCLI.main(args);

    String actual = GrouperUtil.readFileIntoString(tmpFile);

    String correct = "<ldappc:calcRequest xmlns:ldappc='http://grouper.internet2.edu/ldappc' requestID='REQUEST1' returnData='everything'>"
        + System.getProperty("line.separator")
        + "  <ldappc:id ID='edu:groupB'/>"
        + System.getProperty("line.separator")
        + "</ldappc:calcRequest>"
        + System.getProperty("line.separator")
        + "<ldappc:calcResponse xmlns:ldappc='http://grouper.internet2.edu/ldappc' status='success' requestID='REQUEST1'>"
        + System.getProperty("line.separator")
        + "  <ldappc:id ID='edu:groupB'/>"
        + System.getProperty("line.separator")
        + "  <ldappc:pso entityName='group'>"
        + System.getProperty("line.separator")
        + "    <psoID ID='cn=groupB,ou=edu,ou=testgroups,"
        + base
        + "' targetID='ldap'/>"
        + System.getProperty("line.separator")
        + "    <data>"
        + System.getProperty("line.separator")
        + "      <dsml:attr xmlns:dsml='urn:oasis:names:tc:DSML:2:0:core' name='objectClass'>"
        + System.getProperty("line.separator")
        + "        <dsml:value>top</dsml:value>"
        + System.getProperty("line.separator")
        + "        <dsml:value>"
        + properties.get("groupObjectClass")
        + "</dsml:value>"
        + System.getProperty("line.separator")
        + "      </dsml:attr>"
        + System.getProperty("line.separator")
        + "      <dsml:attr xmlns:dsml='urn:oasis:names:tc:DSML:2:0:core' name='cn'>"
        + System.getProperty("line.separator")
        + "        <dsml:value>groupB</dsml:value>"
        + System.getProperty("line.separator")
        + "      </dsml:attr>"
        + System.getProperty("line.separator")
        + "      <dsml:attr xmlns:dsml='urn:oasis:names:tc:DSML:2:0:core' name='description'>"
        + System.getProperty("line.separator")
        + "        <dsml:value>descriptionB</dsml:value>"
        + System.getProperty("line.separator")
        + "      </dsml:attr>"
        + System.getProperty("line.separator")
        + "    </data>"
        + System.getProperty("line.separator")
        + "    <capabilityData mustUnderstand='true' capabilityURI='urn:oasis:names:tc:SPML:2:0:reference'>"
        + System.getProperty("line.separator")
        + "      <spmlref:reference xmlns='urn:oasis:names:tc:SPML:2:0' xmlns:spmlref='urn:oasis:names:tc:SPML:2:0:reference' typeOfReference='member'>"
        + System.getProperty("line.separator")
        + "        <spmlref:toPsoID ID='cn=groupA,ou=edu,ou=testgroups," + base + "' targetID='ldap'/>"
        + System.getProperty("line.separator")
        + "      </spmlref:reference>"
        + System.getProperty("line.separator")
        + "    </capabilityData>"
        + System.getProperty("line.separator")
         + "  </ldappc:pso>"
         + System.getProperty("line.separator")
        + "</ldappc:calcResponse>"
        + System.getProperty("line.separator");

    assertEquals(correct, actual);
  }

  public void testInterval() throws Exception {

    // calc ID=edu:groupB
    cmds.add("-" + PSPOptions.Mode.bulkCalc.getOpt());

    // calc ID=edu:groupB
    cmds.add("-" + PSPOptions.Opts.interval.getOpt());
    cmds.add("2");

    String[] args = cmds.toArray(new String[] {});

    PSPOptions options = new PSPOptions(args);
    options.parseCommandLineOptions();

    PSPCLI pspCLI = new PSPCLI(options);

    pspCLI.schedule();
    
    Timer timer = pspCLI.getTimer();

    // sleep for 6s, should process 3 intervals
    Thread.sleep(6 * 1000);

    timer.cancel();

    // the last modify time should be within 2 seconds (the interval) or less, right ?
    assertTrue((new Date().getTime() - options.getLastModifyTime().getTime()) <= 2 * 1000);
  }

}
