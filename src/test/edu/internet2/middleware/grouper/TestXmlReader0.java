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
import  java.io.*;
import  java.util.Properties;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlReader0.java,v 1.2 2006-10-11 15:40:40 blair Exp $
 * @since   1.1.0
 */
public class TestXmlReader0 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXmlReader0.class);

  public TestXmlReader0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetDocumentFromString() {
    LOG.info("testGetDocumentFromString");
    try {
      // Populate the registry and export to a string
      R           r         = R.populateRegistry(1, 2, 0);
      Writer      w         = new StringWriter();
      XmlExporter exporter  = new XmlExporter(r.rs, new Properties());
      exporter.export(w);
      r.rs.stop();

      // Read document
      XmlReader.getDocumentFromString( w.toString() );
      assertTrue(true);
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testGetDocumentFromString()

} // public class TestXmlReader0

