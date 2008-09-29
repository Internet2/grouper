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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlExporter;
import edu.internet2.middleware.grouper.xml.XmlImporter;
import edu.internet2.middleware.grouper.xml.XmlReader;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestXml10.java,v 1.6 2008-09-29 03:38:27 mchyzer Exp $
 * @since   1.1.0
 */
public class TestXml10 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestXml10.class);

  public TestXml10(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGroupExportFalseFalseFullImportFullGroup() {
    LOG.info("testGroupExportFalseFalseFullImportFullGroup");
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
      exporter.export(w, GroupFinder.findByName(s, val_n), false, false);
      String          xml       = w.toString();
      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindGroupByName(s, val_n);
      s.stop();

      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter importer = new XmlImporter(s, new Properties());
      importer.load( XmlReader.getDocumentFromString(xml) );
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
  } // public void testGroupExportFalseFalseFullImportFullGroup()

} // public class TestXml10

