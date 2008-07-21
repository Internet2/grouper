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

import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.StemNameFilter;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.xml.XmlExporter;
import edu.internet2.middleware.grouper.xml.XmlImporter;
import edu.internet2.middleware.grouper.xml.XmlReader;

/**
 * @author  blair christensen.
 * @version $Id: TestXml11.java,v 1.5 2008-07-21 04:43:57 mchyzer Exp $
 * @since   1.1.0
 */
public class TestXml11 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestXml11.class);

  public TestXml11(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testCollectionExport() {
    LOG.info("testCollectionExport");
    try {
      // Populate Registry And Verify
      R r   = R.populateRegistry(2, 2, 2);
      assertFindStemByName(r.rs, "i2:a");
      assertFindStemByName(r.rs, "i2:b");
      r.rs.stop();

      // Export
      GrouperSession  s         = GrouperSession.start( SubjectFinder.findRootSubject() );
      Writer          w         = new StringWriter();
      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      GrouperQuery    gq        = GrouperQuery.createQuery( 
        s, new StemNameFilter( "%", StemFinder.findRootStem(s) ) 
      );
      exporter.export(w, gq.getStems(), "StemNameFilter('%', <root stem>)");
      String          xml       = w.toString();
      s.stop();

      // Reset And Verify
      RegistryReset.reset();
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindStemByName(s, "i2:a");
      assertDoNotFindStemByName(s, "i2:b");
      s.stop();

      // Import 
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      XmlImporter importer = new XmlImporter(s, new Properties());
      importer.load( XmlReader.getDocumentFromString(xml) );
      s.stop();

      // Verify
      s = GrouperSession.start( SubjectFinder.findRootSubject() );
      assertDoNotFindStemByName(s, "i2:a");
      assertDoNotFindStemByName(s, "i2:b");
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCollectionExport()

} // public class TestXml11

