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
import  java.util.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXml23.java,v 1.1 2006-10-16 19:05:57 blair Exp $
 * @since   1.1.0
 */
public class TestXml23 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXml23.class);

  public TestXml23(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testUpdateOkNamingPrivsInIgnoreMode() {
    LOG.info("testUpdateOkNamingPrivsInIgnoreMode");
    try {
      // Populate Registry And Verify
      R       r     = R.populateRegistry(2, 0, 0);
      Stem    nsA   = r.getStem("a");
      Stem    nsB   = r.getStem("b");
      String  nameA = nsA.getName();
      String  nameB = nsB.getName();
      nsA.grantPriv( SubjectFinder.findAllSubject(), NamingPrivilege.CREATE );
      // Make sure no exception is thrown due to nsB not existing when updating
      nsB.grantPriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
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
      nsA = assertFindStemByName(r.rs, nameA, "recreate");
      assertDoNotFindStemByName(r.rs, nameB, "recreate");
      r.rs.stop();

      // Import 
      s                   = GrouperSession.start( SubjectFinder.findRootSubject() );
      Properties  custom  = new Properties();
      custom.setProperty("import.data.privileges", "ignore");
      XmlImporter importer = new XmlImporter(s, custom);
      importer.update( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      nsA = assertFindStemByName( s, "i2:a" );
      // Should not have
      assertStemHasCreate( nsA, SubjectFinder.findAllSubject(), false );
      // Should not have
      assertStemHasStem( nsA, SubjectFinder.findAllSubject(), false );
      s.stop();
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testUpdateOkNamingPrivsInIgnoreMode()

} // public class TestXml23

