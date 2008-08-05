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
import  java.util.*;

import  junit.framework.*;

import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlImport0.java,v 1.5.8.1 2008-08-05 14:06:37 isgwb Exp $
 * @since   1.0
 */
public class TestXmlImport0 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestXmlImport0.class);

  public TestXmlImport0(String name) {
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
      T.amount("set options", 7, options.size());
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

}

