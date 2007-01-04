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
 * @version $Id: TestXml27.java,v 1.2 2007-01-04 17:17:46 blair Exp $
 * @since   1.1.0
 */
public class TestXml27 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXml27.class);

  public TestXml27(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testUpdateOkMembershipsInAddMode() {
    LOG.info("testUpdateOkMembershipsInAddMode");
    try {
      // Populate Registry And Verify
      R       r     = R.populateRegistry(1, 2, 2);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      String  nameA = gA.getName();
      gA.addMember( r.getSubject("a") );
      // Make sure no exception is thrown due to gB not existing when updating
      gB.addMember( r.getSubject("b") );
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
      r = R.populateRegistry(1, 1, 2);
      gA = assertFindGroupByName(r.rs, nameA, "recreate");
      // Now add another mship
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      gA.addMember(subjB);
      assertGroupHasMember(gA, subjB, true);
      r.rs.stop();

      // Import 
      s                   = GrouperSession.start( SubjectFinder.findRootSubject() );
      Properties  custom  = new Properties();
      custom.setProperty("import.data.lists", "add");
      XmlImporter importer = new XmlImporter(s, custom);
      importer.update( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Verify
      s   = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA  = assertFindGroupByName(s, nameA);
      // Should have
      assertGroupHasMember(gA, subjA, true);
      // Should still have
      assertGroupHasMember(gA, subjB, true);
      s.stop();
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testUpdateOkMembershipsInAddMode()

} // public class TestXml27

