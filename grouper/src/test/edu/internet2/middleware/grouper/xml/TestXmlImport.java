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

package edu.internet2.middleware.grouper.xml;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlImport.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 * @since   1.0
 */
public class TestXmlImport extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestXmlImport.class);

  public TestXmlImport(String name) {
    super(name);
  }

  public void testGetDefaultOptions() {
    LOG.info("testInstantiateWithoutOptions");
    try {
      XmlImporter xml = new XmlImporter(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        new Properties()
      );
      Assert.assertNotNull("xml !null", xml);
      Assert.assertTrue("xml instanceof XmlImporter", xml instanceof XmlImporter);
      Properties options = xml.internal_getOptions();
      Assert.assertNotNull("options !null", options);
      T.amount("set options", 8, options.size());
      String k = "import.metadata.group-types";
      String v = "true";
      T.string(k, v, options.getProperty(k));
      k = "import.metadata.group-type-attributes";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "import.data.apply-new-group-types";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "import.data.update-attributes";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "import.data.lists";
      v = "replace";
      T.string(k, v, options.getProperty(k));
      k = "import.data.privileges";
      v = "add";
      T.string(k, v, options.getProperty(k));
      k = "import.data.fail-on-unresolvable-subject";
      v = "false";
      T.string(k, v, options.getProperty(k));
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testInstantiateWithoutOptions()

  public void testMinimalOkArgHandling() {
    LOG.info("testMinimalOkArgHandling");
    try {
      String      subj  = "GrouperSystem";
      String      file  = "import.xml";
      String[]    args  = { subj, file };
      Properties  rc    = XmlArgs.internal_getXmlImportArgs(args);
      assertTrue( "subject" , rc.getProperty(XmlArgs.RC_SUBJ).equals(subj)  );
      assertTrue( "file"    , rc.getProperty(XmlArgs.RC_IFILE).equals(file) );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalOkArgHandling()

  public void testMinimalPlusNameAndUuidArgHandling() {
    LOG.info("testMinimalPlusNameAndUuidArgHandling");
    try {
      String[]    args  = { "GrouperSystem", "import.xml", "-name", "root", "-id", "abdefg" };
      try {
        XmlArgs.internal_getXmlImportArgs(args);
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

  public void testMinimalPlusNameOkArgHandling() {
    LOG.info("testMinimalPlusNameOkArgHandling");
    try {
      String      subj  = "GrouperSystem";
      String      file  = "import.xml";
      String      name  = "i2";
      String[]    args  = { subj, file, "-name", name };
      Properties  rc    = XmlArgs.internal_getXmlImportArgs(args);
      assertTrue( "subject" , rc.getProperty(XmlArgs.RC_SUBJ).equals(subj)  );
      assertTrue( "file"    , rc.getProperty(XmlArgs.RC_IFILE).equals(file) );
      assertTrue( "name"    , rc.getProperty(XmlArgs.RC_NAME).equals(name)  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalPlusNameOkArgHandling()

  public void testMinimalPlusPropsOkArgHandling() {
    LOG.info("testMinimalPlusPropsOkArgHandling");
    try {
      String      subj  = "GrouperSystem";
      String      file  = "import.xml";
      String      props = "user.properties";
      String[]    args  = { subj, file, props };
      Properties  rc    = XmlArgs.internal_getXmlImportArgs(args);
      assertTrue( "subject" , rc.getProperty(XmlArgs.RC_SUBJ).equals(subj)    );
      assertTrue( "file"    , rc.getProperty(XmlArgs.RC_IFILE).equals(file)   );
      assertTrue( "props"   , rc.getProperty(XmlArgs.RC_UPROPS).equals(props) );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalPlusPropsOkArgHandling()

  public void testMinimalPlusUuidOkArgHandling() {
    LOG.info("testMinimalPlusUuidOkArgHandling");
    try {
      String      subj  = "GrouperSystem";
      String      file  = "import.xml";
      String      uuid  = "abcdefg";
      String[]    args  = { subj, file, "-id", uuid };
      Properties  rc    = XmlArgs.internal_getXmlImportArgs(args);
      assertTrue( "subject" , rc.getProperty(XmlArgs.RC_SUBJ).equals(subj)  );
      assertTrue( "file"    , rc.getProperty(XmlArgs.RC_IFILE).equals(file) );
      assertTrue( "uuid"    , rc.getProperty(XmlArgs.RC_UUID).equals(uuid)  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testMinimalPlusUuidOkArgHandling()

  public void testNullArgHandling() {
    LOG.info("testNullArgHandling");
    try {
      String[] args = {};
      try {
        XmlArgs.internal_getXmlImportArgs(args);
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

  public void testSetCustomOptions() {
    LOG.info("testSetCustomOptions");
    try {
      Properties custom = new Properties();
      custom.setProperty("import.metadata.group-types", "false");
      XmlImporter xml = new XmlImporter(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        custom
      );
      Assert.assertNotNull("xml !null", xml);
      Assert.assertTrue("xml instanceof XmlImporter", xml instanceof XmlImporter);
      Properties options = xml.internal_getOptions();
      Assert.assertNotNull("options !null", options);
      T.amount("set options", 8, options.size());
      String k = "import.metadata.group-types";
      String v = "false";
      T.string(k, v, options.getProperty(k));
      k = "import.metadata.group-type-attributes";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "import.data.apply-new-group-types";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "import.data.update-attributes";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "import.data.lists";
      v = "replace";
      T.string(k, v, options.getProperty(k));
      k = "import.data.privileges";
      v = "add";
      T.string(k, v, options.getProperty(k));
      k = "import.data.fail-on-unresolvable-subject";
      v = "false";
      T.string(k, v, options.getProperty(k));
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testSetCustomOptions()

  public void testTooManyPositionalArgsArgHandling() {
    LOG.info("testTooManyPositionalArgsArgHandling");
    try {
      String[]    args  = { "GrouperSystem", "import.xml", "user.properties", "extra arg" };
      try {
        XmlArgs.internal_getXmlImportArgs(args);
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

}

