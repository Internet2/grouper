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
 * @version $Id: TestXml7.java,v 1.2 2006-09-27 13:56:54 blair Exp $
 * @since   1.1.0
 */
public class TestXml7 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXml7.class);

  public TestXml7(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFullExportFullImportFullMemberships() {
    LOG.info("testFullExportFullImportFullMemberships");
    try {
      // Populate Registry And Verify
      R       r     = R.populateRegistry(1, 4, 2);
      Group   gA    = assertFindGroupByName( r.rs, "i2:a:a" );
      Group   gB    = assertFindGroupByName( r.rs, "i2:a:b" );
      Group   gC    = assertFindGroupByName( r.rs, "i2:a:c" );
      Group   gD    = assertFindGroupByName( r.rs, "i2:a:d" );
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      gA.addMember( subjA );
      gB.addMember( subjB );
      gC.addMember( gA.toSubject() );
      gD.addCompositeMember( CompositeType.UNION, gA, gB );
      boolean has_a_a = gA.hasMember(subjA);
      boolean has_a_b = gA.hasMember(subjB);
      boolean has_a_A = gA.hasMember( gA.toSubject() );
      boolean has_a_B = gA.hasMember( gB.toSubject() );
      boolean has_a_C = gA.hasMember( gC.toSubject() );
      boolean has_a_D = gA.hasMember( gD.toSubject() );
      boolean has_b_a = gB.hasMember(subjA);
      boolean has_b_b = gB.hasMember(subjB);
      boolean has_b_A = gB.hasMember( gA.toSubject() );
      boolean has_b_B = gB.hasMember( gB.toSubject() );
      boolean has_b_C = gB.hasMember( gC.toSubject() );
      boolean has_b_D = gB.hasMember( gD.toSubject() );
      boolean has_c_a = gC.hasMember(subjA);
      boolean has_c_b = gC.hasMember(subjB);
      boolean has_c_A = gC.hasMember( gA.toSubject() );
      boolean has_c_B = gC.hasMember( gB.toSubject() );
      boolean has_c_C = gC.hasMember( gC.toSubject() );
      boolean has_c_D = gC.hasMember( gD.toSubject() );
      boolean has_d_a = gD.hasMember(subjA);
      boolean has_d_b = gD.hasMember(subjB);
      boolean has_d_A = gD.hasMember( gA.toSubject() );
      boolean has_d_B = gD.hasMember( gB.toSubject() );
      boolean has_d_C = gD.hasMember( gC.toSubject() );
      boolean has_d_D = gD.hasMember( gD.toSubject() );
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
      assertDoNotFindGroupByName( s, "i2:a:a" );
      assertDoNotFindGroupByName( s, "i2:a:b" );
      assertDoNotFindGroupByName( s, "i2:a:c" );
      assertDoNotFindGroupByName( s, "i2:a:d" );
      s.stop();

      // Add Subjects
      r = R.populateRegistry(0, 0, 2);
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      r.rs.stop();

      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter importer = new XmlImporter(s, new Properties());
      importer.update( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName( s, "i2:a:a" );
      gB  = assertFindGroupByName( s, "i2:a:b" );
      gC  = assertFindGroupByName( s, "i2:a:c" );
      gD  = assertFindGroupByName( s, "i2:a:d" );
      assertGroupHasMember( gA, subjA, has_a_a );
      assertGroupHasMember( gA, subjB, has_a_b );
      assertGroupHasMember( gA, gA.toSubject(), has_a_A );
      assertGroupHasMember( gA, gB.toSubject(), has_a_B );
      assertGroupHasMember( gA, gC.toSubject(), has_a_C );
      assertGroupHasMember( gA, gD.toSubject(), has_a_D );
      assertGroupHasMember( gB, subjA, has_b_a );
      assertGroupHasMember( gB, subjB, has_b_b );
      assertGroupHasMember( gB, gA.toSubject(), has_b_A );
      assertGroupHasMember( gB, gB.toSubject(), has_b_B );
      assertGroupHasMember( gB, gC.toSubject(), has_b_C );
      assertGroupHasMember( gB, gD.toSubject(), has_b_D );
      assertGroupHasMember( gC, subjA, has_c_a );
      assertGroupHasMember( gC, subjB, has_c_b );
      assertGroupHasMember( gC, gA.toSubject(), has_c_A );
      assertGroupHasMember( gC, gB.toSubject(), has_c_B );
      assertGroupHasMember( gC, gC.toSubject(), has_c_C );
      assertGroupHasMember( gC, gD.toSubject(), has_c_D );
      assertGroupHasMember( gD, subjA, has_d_a );
      assertGroupHasMember( gD, subjB, has_d_b );
      assertGroupHasMember( gD, gA.toSubject(), has_d_A );
      assertGroupHasMember( gD, gB.toSubject(), has_d_B );
      assertGroupHasMember( gD, gC.toSubject(), has_d_C );
      assertGroupHasMember( gD, gD.toSubject(), has_d_D );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportFullMemberships()

} // public class TestXml7

