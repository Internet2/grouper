/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.util.Properties;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlArgs;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlExport2.java,v 1.8 2008-09-29 03:38:27 mchyzer Exp $
 * @since   1.1.0
 */
public class TestXmlExport2 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestXmlExport2.class);

  public TestXmlExport2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testNullArgHandling() {
    LOG.info("testNullArgHandling");
    try {
      String[] args = {};
      try {
        XmlArgs.internal_getXmlExportArgs(args);
        fail("did not throw IllegalStateException with 0 args");
      }
      catch (IllegalStateException eIS) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testNullArgHandling()

  public void testMinimalOkArgHandling() {
    LOG.info("testMinimalOkArgHandling");
    try {
      String      subj  = "GrouperSystem";
      String      file  = "export.xml";
      String[]    args  = { subj, file };
      Properties  rc    = XmlArgs.internal_getXmlExportArgs(args);
      assertTrue( "subject" , rc.getProperty(XmlArgs.RC_SUBJ).equals(subj)  );
      assertTrue( "file"    , rc.getProperty(XmlArgs.RC_EFILE).equals(file) );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalOkArgHandling()

  public void testMinimalPlusPropsOkArgHandling() {
    LOG.info("testMinimalPlusPropsOkArgHandling");
    try {
      String      subj  = "GrouperSystem";
      String      file  = "export.xml";
      String      props = "user.properties";
      String[]    args  = { subj, file, props };
      Properties  rc    = XmlArgs.internal_getXmlExportArgs(args);
      assertTrue( "subject" , rc.getProperty(XmlArgs.RC_SUBJ).equals(subj)    );
      assertTrue( "file"    , rc.getProperty(XmlArgs.RC_EFILE).equals(file)   );
      assertTrue( "props"   , rc.getProperty(XmlArgs.RC_UPROPS).equals(props) );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalPlusPropsOkArgHandling()

  public void testTooManyPositionalArgsArgHandling() {
    LOG.info("testTooManyPositionalArgsArgHandling");
    try {
      String[]    args  = { "GrouperSystem", "export.xml", "user.properties", "extra arg" };
      try {
        XmlArgs.internal_getXmlExportArgs(args);
        fail("did not throw IllegalArgumentException with too many position args");
      }
      catch (IllegalArgumentException eIA) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testTooManyPositionalArgsArgHandling()

  public void testMinimalPlusNameOkArgHandling() {
    LOG.info("testMinimalPlusNameOkArgHandling");
    try {
      String      subj  = "GrouperSystem";
      String      file  = "export.xml";
      String      name  = "i2";
      String[]    args  = { subj, file, "-name", name };
      Properties  rc    = XmlArgs.internal_getXmlExportArgs(args);
      assertTrue( "subject" , rc.getProperty(XmlArgs.RC_SUBJ).equals(subj)  );
      assertTrue( "file"    , rc.getProperty(XmlArgs.RC_EFILE).equals(file) );
      assertTrue( "name"    , rc.getProperty(XmlArgs.RC_NAME).equals(name)  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalPlusNameOkArgHandling()

  public void testMinimalPlusUuidOkArgHandling() {
    LOG.info("testMinimalPlusUuidOkArgHandling");
    try {
      String      subj  = "GrouperSystem";
      String      file  = "export.xml";
      String      uuid  = "abcdefg";
      String[]    args  = { subj, file, "-id", uuid };
      Properties  rc    = XmlArgs.internal_getXmlExportArgs(args);
      assertTrue( "subject" , rc.getProperty(XmlArgs.RC_SUBJ).equals(subj)  );
      assertTrue( "file"    , rc.getProperty(XmlArgs.RC_EFILE).equals(file) );
      assertTrue( "uuid"    , rc.getProperty(XmlArgs.RC_UUID).equals(uuid)  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalPlusUuidOkArgHandling()

  public void testMinimalPlusNameAndUuidArgHandling() {
    LOG.info("testMinimalPlusNameAndUuidArgHandling");
    try {
      String[]    args  = { "GrouperSystem", "export.xml", "-name", "root", "-id", "abdefg" };
      try {
        XmlArgs.internal_getXmlExportArgs(args);
        fail("did not throw IlllegalArgumentException when both -name and -id specified");
      }
      catch (IllegalArgumentException eIA) {  
        assertTrue(true);
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalPlusNameAndUuidArgHandling()

} // public class TestXmlExport2

