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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlExporter;
import edu.internet2.middleware.grouper.xml.XmlReader;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlReader.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 * @since   1.1.0
 */
public class TestXmlReader extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestXmlReader.class);

  public TestXmlReader(String name) {
    super(name);
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
      unexpectedException(e);
    }
  } // public void testGetDocumentFromString()

  public void testGetDocumentFromFile() {
    LOG.info("testGetDocumentFromFile");
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
      XmlReader.getDocumentFromFile( tmp.getAbsolutePath() );
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testGetDocumentFromFile()

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
      unexpectedException(e);
    }
  } // public void testGetDocumentFromURL()

} // public class TestXmlReader0

