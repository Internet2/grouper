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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXml0.java,v 1.2 2006-09-27 13:56:54 blair Exp $
 * @since   1.1.0
 */
public class TestXml0 extends TestCase {

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

  public void testFullExportToStringAndFullImport() {
    LOG.info("testFullExportToStringAndFullImport");
    try {
      // Export - Setup
      R       r     = R.populateRegistry(2, 2, 2);
      Group   gAA   = r.getGroup("a", "a");
      Group   gAB   = r.getGroup("a", "b");
      Group   gBA   = r.getGroup("b", "a");
      Group   gBB   = r.getGroup("b", "b");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      gAA.addMember(  subjA                                 );
      gAB.addMember(  subjB                                 );
      gBA.addMember(  gAA.toSubject()                       );
      gBB.addCompositeMember( CompositeType.UNION, gAA, gAB );
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

      // Install Subjects
      r     = R.populateRegistry(0, 0, 2);
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      r.rs.stop();

      // Import
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter     importer  = new XmlImporter(s, new Properties());
      importer.load( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Import - Verify
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Stem            ns  = StemFinder.findByName(  s, "i2"                 );  
      Assert.assertTrue(  "found ns"  , ns != null  );
      Stem            nsA = StemFinder.findByName(  s, ns.getName() + ":a"  );
      Assert.assertTrue(  "found nsA" , nsA != null );
      Stem            nsB = StemFinder.findByName(  s, ns.getName() + ":b"  );
      Assert.assertTrue(  "found nsB" , nsB != null );
      gAA = GroupFinder.findByName(s, nsA.getName() + ":a");
      Assert.assertTrue(  "found gAA" , gAA != null );
      gAB = GroupFinder.findByName(s, nsA.getName() + ":b");
      Assert.assertTrue(  "found gAB" , gAB != null );
      gBA = GroupFinder.findByName(s, nsB.getName() + ":a");
      Assert.assertTrue(  "found gBA" , gBA != null );
      gBB = GroupFinder.findByName(s, nsB.getName() + ":b");
      Assert.assertTrue(  "found gBB" , gBB != null );
      Assert.assertTrue( "gAA hasMember subjA", gAA.hasMember(subjA) );
      Assert.assertTrue( "gAB hasMember subjB", gAB.hasMember(subjB) );
      Assert.assertTrue( "gBB hasMember subjA", gBB.hasMember(subjA) );
      Assert.assertTrue( "gBB hasMember subjB", gBB.hasMember(subjB) );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportToStringAndFullImport()

} // public class TestXml0

