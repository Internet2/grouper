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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncRequest;

/**
 * Basic test for the PSP CLI.
 */
public class PSPCLITest extends BasePSPProvisioningTest {

  public static final String CONFIG_PATH = TEST_PATH + "/spml";

  public static final String DATA_PATH = CONFIG_PATH + "/data/";

  public static void main(String[] args) {
    TestRunner.run(PSPCLITest.class);
    // TestRunner.run(new PSPCLITest("testIntervalBulkSyncFullSync"));
  }

  public PSPCLITest(String name) {
    super(name, CONFIG_PATH);
  }

  private List<String> getCmds() {
    List<String> cmds = new ArrayList<String>();

    // conf dir
    cmds.add("-" + PSPOptions.Opts.conf.getOpt());
    cmds.add(GrouperUtil.fileFromResourceName(PSPLdapTest.CONFIG_PATH).getAbsolutePath());

    // request id
    cmds.add("-" + PSPOptions.Opts.requestID);
    cmds.add("REQUEST1");

    // returnData = everything
    cmds.add("-" + PSPOptions.Opts.returnEverything.getOpt());

    // print requests
    cmds.add("-" + PSPOptions.Opts.printRequests.getOpt());

    // log spml
    cmds.add("-" + PSPOptions.Opts.logSpml.getOpt());

    return cmds;
  }

  private File getTmpFile() {
    try {
      File tmpFile = File.createTempFile("PSPOptionsTest", ".tmp");
      tmpFile.deleteOnExit();
      return tmpFile;
    } catch (IOException e) {
      e.printStackTrace();
      fail("An exception occurred : " + e);
    }

    return null;
  }

  public void testNotFound() throws Exception {

    List<String> cmds = getCmds();

    // tmp file output
    File tmpFile = getTmpFile();
    cmds.add("-" + PSPOptions.Opts.output.getOpt());
    cmds.add(tmpFile.getAbsolutePath());

    // calc ID=test
    cmds.add("-" + PSPOptions.Mode.calc.getOpt());
    cmds.add("test");

    PSPOptions options = new PSPOptions(cmds.toArray(new String[] {}));
    options.parseCommandLineOptions();

    PSPCLI pspCLI = new PSPCLI(options);

    pspCLI.run();

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
        + "</ldappc:calcResponse>" + System.getProperty("line.separator");

    String actual = GrouperUtil.readFileIntoString(tmpFile);

    assertEquals(correct, actual);
  }

  private void bulkSync(String[] args) throws Exception {

    PSPOptions options = new PSPOptions(args);
    options.parseCommandLineOptions();

    PSPCLI pspCLI = new PSPCLI(options);

    pspCLI.schedule();

    Timer timer = pspCLI.getTimer();

    // process 3 intervals, approximately
    Thread.sleep(options.getInterval() * 4 * 1000);

    timer.cancel();
  }

  public void testIntervalBulkSync() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    List<String> cmds = getCmds();

    // tmp file output
    File tmpFile = getTmpFile();
    cmds.add("-" + PSPOptions.Opts.output.getOpt());
    cmds.add(tmpFile.getAbsolutePath());

    // calc ID=edu:groupB
    cmds.add("-" + PSPOptions.Mode.bulkSync.getOpt());

    // calc ID=edu:groupB
    cmds.add("-" + PSPOptions.Opts.interval.getOpt());
    cmds.add("2");

    bulkSync(cmds.toArray(new String[] {}));

    // TODO test output file for correctness
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAdd.after.ldif");
  }

  public void testIntervalBulkSyncWithLastModifyTime() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    List<String> cmds = getCmds();

    // tmp file output
    File tmpFile = getTmpFile();
    cmds.add("-" + PSPOptions.Opts.output.getOpt());
    cmds.add(tmpFile.getAbsolutePath());

    // calc ID=edu:groupB
    cmds.add("-" + PSPOptions.Mode.bulkSync.getOpt());

    // calc ID=edu:groupB
    cmds.add("-" + PSPOptions.Opts.interval.getOpt());
    cmds.add("2");

    bulkSync(cmds.toArray(new String[] {}));

    // TODO test output file for correctness
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAdd.after.ldif");

    // run again with last modify time set
    // updated since, use bulk request for date formatting, ugly
    BulkSyncRequest request = new BulkSyncRequest();
    request.setUpdatedSince(new Date());
    cmds.add("-" + PSPOptions.Opts.lastModifyTime.getOpt());
    cmds.add(request.getUpdatedSince());

    bulkSync(cmds.toArray(new String[] {}));

    // TODO test output file for correctness
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAdd.after.ldif");
  }

  public void testIntervalBulkSyncFullSync() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    List<String> cmds = getCmds();

    // tmp file output
    File tmpFile = getTmpFile();
    cmds.add("-" + PSPOptions.Opts.output.getOpt());
    cmds.add(tmpFile.getAbsolutePath());

    // bulkSync
    cmds.add("-" + PSPOptions.Mode.bulkSync.getOpt());

    // interval 2
    cmds.add("-" + PSPOptions.Opts.interval.getOpt());
    cmds.add("2");

    // full sync interval 2
    cmds.add("-" + PSPOptions.Opts.intervalFullSync.getOpt());
    cmds.add("4");

    // iterations e
    cmds.add("-" + PSPOptions.Opts.iterations.getOpt());
    cmds.add("3");

    if (true) {
      // run again with last modify time set
      // updated since, use bulk request for date formatting, ugly
      BulkSyncRequest request = new BulkSyncRequest();
      request.setUpdatedSince(new Date());
      cmds.add("-" + PSPOptions.Opts.lastModifyTime.getOpt());
      cmds.add(request.getUpdatedSince());
    }

    bulkSync(cmds.toArray(new String[] {}));

    // TODO test output file for correctness
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAdd.after.ldif");
  }

}
