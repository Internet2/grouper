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
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.util.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXml15.java,v 1.2 2007-01-04 17:17:46 blair Exp $
 * @since   1.1.0
 */
public class TestXml15 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXml15.class);

  public TestXml15(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGroupExportTrueFalseFullImportFullGroup() {
    LOG.info("testGroupExportTrueFalseFullImportFullGroup");
    try {
      // Populate Registry And Verify
      R     r   = R.populateRegistry(1, 2, 0);
      Group gA  = assertFindGroupByName( r.rs, "i2:a:a" );
      // For Later Validation
      boolean has_a   = gA.hasAdmin( SubjectFinder.findAllSubject() );
      boolean has_oi  = gA.hasOptin( SubjectFinder.findAllSubject() );
      boolean has_oo  = gA.hasOptout( SubjectFinder.findAllSubject() );
      boolean has_r   = gA.hasRead( SubjectFinder.findAllSubject() );
      boolean has_u   = gA.hasUpdate( SubjectFinder.findAllSubject() );
      boolean has_v   = gA.hasView( SubjectFinder.findAllSubject() );
      Subject val_c   = gA.getCreateSubject();
      Date    val_ct  = gA.getCreateTime();
      String  val_d   = gA.getDescription();
      String  val_de  = gA.getDisplayExtension();
      String  val_dn  = gA.getDisplayName();
      String  val_e   = gA.getExtension();
      String  val_n   = gA.getName();
      String  val_u   = gA.getUuid();
      assertFindGroupByName( r.rs, "i2:a:b" ); // group should exist now
      r.rs.stop();

      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      exporter.export(w, GroupFinder.findByName(s, val_n), true, false);
      String          xml       = w.toString();
      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      r = R.populateRegistry(1, 0, 0);
      assertFindStemByName(r.rs, "i2:a");
      assertDoNotFindGroupByName(r.rs, "i2:a:a");
      r.rs.stop();

      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter importer = new XmlImporter(s, new Properties());
      importer.load( StemFinder.findByName(s, "i2:a"), XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName(s, val_n);
      assertGroupHasAdmin( gA, SubjectFinder.findAllSubject(), has_a );
      assertGroupHasOptin( gA, SubjectFinder.findAllSubject(), has_oi );
      assertGroupHasOptout( gA, SubjectFinder.findAllSubject(), has_oo );
      assertGroupHasRead( gA, SubjectFinder.findAllSubject(), has_r );
      assertGroupHasUpdate( gA, SubjectFinder.findAllSubject(), has_u );
      assertGroupHasView( gA, SubjectFinder.findAllSubject(), has_v );
      assertGroupCreateSubject( gA, val_c );
      assertGroupCreateTime( gA, val_ct );
      assertGroupDescription( gA, val_d );
      assertGroupDisplayExtension( gA, val_de );
      assertGroupDisplayName( gA, val_dn );
      assertGroupExtension( gA, val_e );
      assertGroupName( gA, val_n );
      assertGroupUuid( gA, val_u );
      assertDoNotFindGroupByName( s, "i2:a:b" ); // group should not exist now
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupExportTrueFalseFullImportFullGroup()

} // public class TestXml15

