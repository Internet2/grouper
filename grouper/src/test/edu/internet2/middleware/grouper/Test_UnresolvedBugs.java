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
import  java.io.StringWriter;
import  java.io.Writer;
import  java.util.Properties;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_UnresolvedBugs.java,v 1.2 2007-03-07 19:13:59 blair Exp $
 * @since   1.2.0
 */
public class Test_UnresolvedBugs extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_UnresolvedBugs.class);


  // TESTS //  

  public void testUnresolvedBug0() {
    try {
      LOG.info("testUnresolvedBug0");
    
      RegistryReset.reset();
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      Stem            root  = StemFinder.findRootStem(s).addChildStem("uchicago", "uchicago");
      Stem            ns    = root.addChildStem("nsit", "nsit");
      Group           g     = ns.addChildGroup("nas", "nas");

      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      Writer          w         = new StringWriter();
      exporter.export(w);
      String          xml       = w.toString();

      g.delete();
      ns.delete();

/* 
      root.delete();
      root  = StemFinder.findRootStem(s).addChildStem("uchicago", "uchicago");
      ns = root.internal_addChildStem("nsit", "nsit", ns.getUuid() );
      // this fails
      g = ns.internal_addChildGroup("nas", "nas", g.getUuid() );
      // this does not
      //g = ns.addChildGroup("nas", "nas");
*/
      XmlImporter importer = new XmlImporter(s, new Properties());
      // FIXME 20070307 why does this error out when trying to save the child group?
      try {
        importer.load( XmlReader.getDocumentFromString(xml) );
        String msg = "GrouperException not thrown when adding child group from xml";
        TestLog.lookInto(Test_UnresolvedBugs.class, msg);
        fail(msg);
      }
      catch (GrouperException eG) {
        String msg = "GrouperException thrown when adding child group from xml";
        TestLog.stillFailing(Test_UnresolvedBugs.class, msg);
        assertTrue(msg, true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testUnresolvedBug0()

} // public class Test_UnresolvedBugs extends GrouperTest

