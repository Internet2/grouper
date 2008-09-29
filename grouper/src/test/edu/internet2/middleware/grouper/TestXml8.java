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
import java.util.Properties;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlExporter;
import edu.internet2.middleware.grouper.xml.XmlImporter;
import edu.internet2.middleware.grouper.xml.XmlReader;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestXml8.java,v 1.8 2008-09-29 03:38:27 mchyzer Exp $
 * @since   1.1.0
 */
public class TestXml8 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestXml8.class);

  public TestXml8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFullExportFullImportCustomTypes() {
    LOG.info("testFullExportFullImportCustomTypes");
    try {
      // Populate Registry And Verify
      R         r     = R.populateRegistry(1, 1, 1);
      Group     gA    = assertFindGroupByName( r.rs, "i2:a:a" );
      Subject   subjA = r.getSubject("a");
      GroupType type  = GroupType.createType(r.rs, "custom type");
      Field     attr  = type.addAttribute(r.rs, "custom attribute", AccessPrivilege.READ, AccessPrivilege.UPDATE, false);
      Field     list  = type.addList(r.rs, "custom list", AccessPrivilege.READ, AccessPrivilege.UPDATE);
      gA.addType( type );
      gA.setAttribute( attr.getName(), attr.getName() );
      gA.store();
      gA.addMember( subjA, list );
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
      s.stop();

      // Add Subjects
      r = R.populateRegistry(0, 0, 1);
      subjA = r.getSubject("a");
      r.rs.stop();

      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter importer = new XmlImporter(s, new Properties());
      importer.load( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Verify
      s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      gA    = assertFindGroupByName( s, "i2:a:a" );
      type  = assertFindGroupType("custom type");
      attr  = assertFindField("custom attribute");
      list  = assertFindField("custom list");
      assertGroupHasType( gA, type, true );
      assertGroupAttribute( gA, attr.getName(), attr.getName() );
      assertGroupHasMember( gA, subjA, list, true );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullExportFullImportCustomTypes()

} // public class TestXml8

