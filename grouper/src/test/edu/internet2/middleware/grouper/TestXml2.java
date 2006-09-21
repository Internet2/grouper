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
 * @version $Id: TestXml2.java,v 1.1 2006-09-21 19:57:55 blair Exp $
 * @since   1.1.0
 */
public class TestXml2 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestXml2.class);

  public TestXml2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFullExportToStringAndUpdateOnly() {
    LOG.info("testFullExportToStringAndUpdateOnly");
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

      // Install Subjects and partial registry
      r     = r.populateRegistry(1, 2, 2);
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      r.rs.stop();

      // Import
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter     importer  = new XmlImporter(s, new Properties());
      importer.update( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Import - Verify
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Stem            ns  = StemFinder.findByName(  s, "i2"                 );  
      Assert.assertTrue(  "found ns"  , ns != null  );
      Stem            nsA = StemFinder.findByName(  s, ns.getName() + ":a"  );
      Assert.assertTrue(  "found nsA" , nsA != null );
      try {
        StemFinder.findByName(s, ns.getName() + ":b");
        T.fail("found stem (nsB) that wasn't manually created");
      }
      catch (StemNotFoundException eGNF) {
        T.ok("stem (nsB) not created during update");
      }
      gAA = GroupFinder.findByName(s, nsA.getName() + ":a");
      Assert.assertTrue(  "found gAA" , gAA != null );
      gAB = GroupFinder.findByName(s, nsA.getName() + ":b");
      Assert.assertTrue(  "found gAB" , gAB != null );
      Assert.assertFalse( "FIXME 20060921 BUG gAA hasMember subjA", gAA.hasMember(subjA) );
      Assert.assertFalse( "FIXME 20060921 BUG gAB hasMember subjB", gAB.hasMember(subjB) );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportToStringAndUpdateOnly()

} // public class TestXml2

