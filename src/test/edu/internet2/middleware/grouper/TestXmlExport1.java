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
import edu.internet2.middleware.grouper.xml.XmlExporter;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlExport1.java,v 1.12 2008-11-04 15:16:52 isgwb Exp $
 * @since   1.1.0
 */
public class TestXmlExport1 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestXmlExport1.class);

  public TestXmlExport1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testSetCustomOption() {
    LOG.info("testSetCustomOption");
    try {
      Properties  custom  = new Properties();
      custom.setProperty("export.metadata", "false");
      XmlExporter xml     = new XmlExporter(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        custom
      );  
      Properties  options = xml.internal_getOptions();
      Assert.assertTrue("15 set options", options.size() == 15);
      String k = "export.metadata";
      String v = "false";
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
      v = "true";
      T.string(k, v, options.getProperty(k));
      k = "export.privs.for-parents";
      v = "false";
      T.string(k, v, options.getProperty(k));
      k = "export.data.fail-on-unresolvable-subject";
      v = "false";
      T.string(k, v, options.getProperty(k));
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testSetCustomOption()

}

