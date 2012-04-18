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
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.changeLog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.LdappcTestHelper;
import edu.internet2.middleware.ldappc.spml.BasePSPProvisioningTest;
import edu.internet2.middleware.ldappc.spml.PSP;

/** Abstract class for testing the {@link LdappcngConsumer} */
public abstract class BaseLdappcngConsumerTest extends BasePSPProvisioningTest {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(BaseLdappcngConsumerTest.class);

  /** The ldappcng change log consumer. */
  private LdappcngConsumer ldappcngConsumer;

  /** The temporary file to which spml requests and responses are written. */
  private File tmpFile;

  /**
   * Constructor
   * 
   * @param name
   * @param confDir
   */
  public BaseLdappcngConsumerTest(String name, String confDir) {
    super(name, confDir);
  }

  /**
   * Create temporary file. Initialize the {@link LdappcngConsumer} and underlying {@link PSP}.
   * 
   * {@inheritDoc}
   */
  public void setUp() {

    super.setUp();

    GrouperLoaderConfig.testConfig.put("changeLog.consumer.ldappcng.class", LdappcngConsumer.class.getName());
    // GrouperLoaderConfig.testConfig.put("changeLog.consumer.ldappcng.quartzCron", "0 0 8 * * ?");
    GrouperLoaderConfig.testConfig.put("changeLog.consumer.ldappcng.confDir", GrouperUtil.fileFromResourceName(confDir)
        .getAbsolutePath());

    try {
      tmpFile = File.createTempFile(getName(), ".tmp");
      LOG.debug("creating tmp file '{}'", tmpFile);
      tmpFile.deleteOnExit();

      setUpLdappcngConsumer(tmpFile);

    } catch (IOException e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    } catch (ResourceException e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }

    runChangeLog();
  }

  /**
   * Initialize the {@Link LdappcngConsumer} and configure the underlying {@link PSP} to write spml requests and
   * responses to the given file.
   * 
   * @param outputFile
   * @throws ResourceException
   */
  protected void setUpLdappcngConsumer(File outputFile) throws ResourceException {
    ldappcngConsumer = new LdappcngConsumer();
    ldappcngConsumer.initialize();
    ldappcngConsumer.getPsp().setWriteRequests(true);
    ldappcngConsumer.getPsp().setWriteResponses(true);
    ldappcngConsumer.getPsp().setPathToOutputFile(outputFile.getAbsolutePath());
  
    // set the writer to null so we write a new file for every test, necessary when PSP is static
    ldappcngConsumer.getPsp().setWriter(null);
  
    // just for tests
    ldappcngConsumer.getPsp().setLogSpml(true);
  
    // just for tests
    psp = ldappcngConsumer.getPsp();
  }

  /**
   * Delete the temporary file.
   * 
   * {@inheritDoc}
   */
  public void tearDown() {

    // delete tmp file if it exists
    if (tmpFile != null) {
      if (tmpFile.exists()) {
        LOG.debug("deleting tmp file '{}'", tmpFile);
        tmpFile.delete();
      }
    }

    super.tearDown();
  }

  /**
   * Process change log records.
   */
  public void runChangeLog() {

    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName("ldappcng");
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    hib3GrouploaderLog.store();

    try {
      ChangeLogHelper.processRecords("ldappcng", hib3GrouploaderLog, ldappcngConsumer);
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
    } catch (Exception e) {
      LOG.error("Error processing records", e);
      e.printStackTrace();
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
    }
    hib3GrouploaderLog.store();
  }

  /**
   * Given spml messages separated by an empty line, return a list of these messages, split by the empty line.
   * 
   * @param xml the xml containing empty line separated xml messages
   * @return the list of messages
   */
  protected List<String> parseStrings(String xml) {
    List<String> strings = new ArrayList<String>();
    String[] toks = xml.split("\\n\\n");
    for (int i = 0; i < toks.length; i++) {
      if (toks[i].startsWith("\n")) {
        strings.add(toks[i]);
      } else {
        strings.add("\n" + toks[i]);
      }
    }
    return strings;
  }

  /**
   * Verify that the spml messages in the given file match those in the temporary file.
   * 
   * @param correctXMLFileName the file containing correct spml messages
   */
  public void verifySpml(String correctXMLFileName) {
    try {
      String currentXML = DatatypeHelper.inputstreamToString(new FileInputStream(tmpFile), null);
      List<String> currentXMLMsgs = parseStrings(currentXML);

      String correctXML = DatatypeHelper.inputstreamToString(
          new FileInputStream(LdappcTestHelper.getFile(correctXMLFileName)), null);
      List<String> correctXMLMsgs = parseStrings(correctXML);

      Assert.assertEquals("Number of messages mismatch", correctXMLMsgs.size(), currentXMLMsgs.size());

      // verify each message in order
      for (int i = 0; i < currentXMLMsgs.size(); i++) {
        InputStream correctInputStream = new ByteArrayInputStream(correctXMLMsgs.get(i).getBytes("UTF-8"));
        InputStream currentInputStream = new ByteArrayInputStream(currentXMLMsgs.get(i).getBytes("UTF-8"));

        LdappcTestHelper.verifySpml(ldappcngConsumer.getPsp().getXMLMarshaller(), ldappcngConsumer.getPsp()
            .getXmlUnmarshaller(), correctInputStream, currentInputStream, false, propertiesFile);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    } catch (IOException e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
  }

  /**
   * Write the correct spml file from output written to the temporary file. Usually used once during test setup.
   * 
   * @param correctXMLFileName the file which will contain the correct spml messages
   */
  protected void verifySpmlWrite(String correctXMLFileName) {
    try {
      String currentXML = DatatypeHelper.inputstreamToString(new FileInputStream(tmpFile), null);
      List<String> strings = parseStrings(currentXML);
      LdappcTestHelper.writeCorrectTestFile(propertiesFile, correctXMLFileName, strings.toArray(new String[] {}));
    } catch (IOException e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
  }

}
