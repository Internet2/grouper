/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlExport0.java,v 1.5 2006-09-26 19:51:17 blair Exp $
 * @since   1.0
 */
public class TestXmlExport0 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestXmlExport0.class);

  public TestXmlExport0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetDefaultOptions() {
    LOG.info("testGetDefaultOptions");
    try {
      XmlExporter xml = new XmlExporter(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        new Properties()
      );  
      Assert.assertNotNull("xml !null", xml);
      Assert.assertTrue("xml instanceof XmlExporter", xml instanceof XmlExporter);
      Properties options = xml.getOptions();
      Assert.assertNotNull("options !null", options);
      Assert.assertTrue("13 set options", options.size() == 13);
      String k = "export.metadata";
      String v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.data";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.privs.naming";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.privs.access";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.privs.immediate-only";
      v = "false";
      T.string(k, v, options.getProperty(k));
      k = "export.group.members";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.group.members.immediate-only";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.group.lists";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.group.lists.immediate-only";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.group.internal-attributes";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.group.custom-attributes";
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.stem.internal-attributes";
      v = "false";
      T.string(k, v, options.getProperty(k));
      k = "export.privs.for-parents";
      v = "false";
      T.string(k, v, options.getProperty(k));
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetDefaultOptions()

}

