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
import  java.io.*;
import  java.util.Properties;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlReader2.java,v 1.4 2007-01-08 18:04:07 blair Exp $
 * @since   1.1.0
 */
public class TestXmlReader2 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXmlReader2.class);

  public TestXmlReader2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetDocumentFromURL() {
    LOG.info("testGetDocumentFromURL");
    try {
      // Populate the registry and export to a file
      R           r         = R.populateRegistry(1, 2, 0);
      File        tmp       = File.createTempFile("grouper", ".xml");
      tmp.deleteOnExit();
      Writer      w         = new BufferedWriter( new FileWriter(tmp) );
      XmlExporter exporter  = new XmlExporter(r.rs, new Properties());
      exporter.export(w);
      r.rs.stop();

      // Read document
      XmlReader.getDocumentFromURL( tmp.toURL() );
      assertTrue(true);
    }
    catch (Exception e) {
      internal_e(e);
    }
  } // public void testGetDocumentFromURL()

} // public class TestXmlReader2

