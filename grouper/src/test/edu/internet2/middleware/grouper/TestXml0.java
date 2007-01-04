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
import  java.util.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXml0.java,v 1.6 2007-01-04 17:17:46 blair Exp $
 * @since   1.1.0
 */
public class TestXml0 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXml0.class);

  public TestXml0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFullExportWithEscapedCharactersAndFullImportFullStem() {
    LOG.info("testFullExportWithEscapedCharactersAndFullImportFullStem");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(4, 1, 0);
      // For Later Validation
      String  amp   = "ampersand - &";
      String  apos  = "apostrophe - '";
      String  gt    = "greater than - >";
      String  lt    = "less than - <";
      // Set and verify unsightly values
      Stem  nsA = assertFindStemByName( r.rs, "i2:a" );
      nsA.setDisplayExtension(amp);
      assertStemDisplayExtension(nsA, amp);

      Stem  nsB = assertFindStemByName( r.rs, "i2:b" );
      nsB.setDisplayExtension(apos);
      assertStemDisplayExtension(nsB, apos);

      Stem  nsC = assertFindStemByName( r.rs, "i2:c" );
      nsC.setDisplayExtension(gt);
      assertStemDisplayExtension(nsC, gt);

      Stem  nsD = assertFindStemByName( r.rs, "i2:d" );
      nsD.setDisplayExtension(lt);
      assertStemDisplayExtension(nsD, lt);

      Group gAA = assertFindGroupByName( r.rs, "i2:a:a" );
      gAA.setDisplayExtension(amp);
      assertGroupDisplayExtension(gAA, amp);

      Group gBA = assertFindGroupByName( r.rs, "i2:b:a" );
      gBA.setDisplayExtension(apos);
      assertGroupDisplayExtension(gBA, apos);

      Group gCA = assertFindGroupByName( r.rs, "i2:c:a" );
      gCA.setDisplayExtension(gt);
      assertGroupDisplayExtension(gCA, gt);

      Group gDA = assertFindGroupByName( r.rs, "i2:d:a" );
      gDA.setDisplayExtension(lt);
      assertGroupDisplayExtension(gDA, lt);

      r.rs.stop();

      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      exporter.export(w);
      String          xml       = w.toString();
      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindStemByName( s, "i2:a" );
      s.stop();

      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter importer = new XmlImporter(s, new Properties());
      importer.load( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );

      nsA = assertFindStemByName(s, "i2:a");
      assertStemDisplayExtension(nsA, amp);

      nsB = assertFindStemByName(s, "i2:b");
      assertStemDisplayExtension(nsB, apos);

      nsC = assertFindStemByName(s, "i2:c");
      assertStemDisplayExtension(nsC, gt);

      nsD = assertFindStemByName(s, "i2:d");
      assertStemDisplayExtension(nsD, lt);

      gAA = assertFindGroupByName(s, "i2:a:a");
      assertGroupDisplayExtension(gAA, amp);

      gBA = assertFindGroupByName(s, "i2:b:a");
      assertGroupDisplayExtension(gBA, apos);

      gCA = assertFindGroupByName(s, "i2:c:a");
      assertGroupDisplayExtension(gCA, gt);

      gDA = assertFindGroupByName(s, "i2:d:a");
      assertGroupDisplayExtension(gDA, lt);

      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportWithEscapedCharactersAndFullImportFullStem()

} // public class TestXml0

