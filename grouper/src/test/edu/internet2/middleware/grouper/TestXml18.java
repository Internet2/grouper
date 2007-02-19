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
 * @version $Id: TestXml18.java,v 1.5 2007-02-19 20:43:29 blair Exp $
 * @since   1.1.0
 */
public class TestXml18 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXml18.class);

  public TestXml18(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testUpdateOkDoNotUpdateStemAttrs() {
    LOG.info("testUpdateOkDoNotUpdateStemAttrs");
    try {
      // Export - Setup
      R       r     = R.populateRegistry(2, 0, 0);
      Stem    nsA   = r.getStem("a");
      String  nameA = nsA.getName();
      String  orig  = nsA.getDescription();
      nsA.setDescription(nameA);
      assertStemDescription(nsA, nameA);
      r.rs.stop();

      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      exporter.export(w);
      String          xml       = w.toString();
      s.stop();

      // Reset
      RegistryReset.reset();

      // Install Subjects and partial registry
      r = R.populateRegistry(1, 0, 0);
      assertFindStemByName(r.rs, nameA, "recreate");
      r.rs.stop();

      // Update
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter importer  = new XmlImporter(s, new Properties());
      importer.update( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Import - Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      nsA = assertFindStemByName(s, nameA, "update");
      assertStemDescription(nsA, orig);
      s.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testUpdateOkDoNotUpdateStemAttrs()

} // public class TestXml18

