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
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.util.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXml12.java,v 1.2 2006-10-16 18:41:01 blair Exp $
 * @since   1.1.0
 */
public class TestXml12 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXml12.class);

  public TestXml12(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testStemExportRootTrueFullImportFullStem() {
    LOG.info("testStemExportRootTrueFullImportFullStem");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(2, 0, 0);
      Stem  nsA = assertFindStemByName( r.rs, "i2:a" );
      // For Later Validation
      boolean has_c   = nsA.hasCreate( SubjectFinder.findAllSubject() );
      boolean has_s   = nsA.hasStem( SubjectFinder.findAllSubject() );
      Subject val_c   = nsA.getCreateSubject();
      Date    val_ct  = nsA.getCreateTime();
      String  val_d   = nsA.getDescription();
      String  val_de  = nsA.getDisplayExtension();
      String  val_dn  = nsA.getDisplayName();
      String  val_e   = nsA.getExtension();
      String  val_n   = nsA.getName();
      String  val_u   = nsA.getUuid();
      assertFindStemByName( r.rs, "i2:b" ); // stem should exist now
      r.rs.stop();

      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      exporter.export(w, StemFinder.findRootStem(s), false);
      String          xml       = w.toString();
      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindStemByName(s, val_n);
      s.stop();

      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter importer = new XmlImporter(s, new Properties());
      importer.load( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      nsA = assertFindStemByName(s, val_n);
      assertStemHasCreate( nsA, SubjectFinder.findAllSubject(), has_c );
      assertStemHasStem( nsA, SubjectFinder.findAllSubject(), has_s );
      assertStemCreateSubject( nsA, val_c );
      assertStemCreateTime( nsA, val_ct );
      assertStemDescription( nsA, val_d );
      assertStemDisplayExtension( nsA, val_de );
      assertStemDisplayName( nsA, val_dn );
      assertStemExtension( nsA, val_e );
      assertStemName( nsA, val_n );
      assertStemUuid( nsA, val_u );
      assertFindStemByName( s, "i2:b" ); // stem should exist 
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testStemExportRootTrueFullImportFullStem()

} // public class TestXml12

