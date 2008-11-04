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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlImporter;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlImport1.java,v 1.10 2008-11-04 15:16:52 isgwb Exp $
 * @since   1.0
 */
public class TestXmlImport1 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestXmlImport1.class);

  public TestXmlImport1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

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

}

