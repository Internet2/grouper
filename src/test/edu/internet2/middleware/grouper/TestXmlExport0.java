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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlExport0.java,v 1.2 2006-08-30 18:35:37 blair Exp $
 * @since   1.0
 */
public class TestXmlExport0 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestXmlExport0.class);

  public TestXmlExport0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testInstantiateWithEmptyOptions() {
    LOG.info("testInstantiateWithEmptyOptions");
    try {
      XmlExporter xml = new XmlExporter( new Properties() );  
      Assert.assertNotNull("xml !null", xml);
      Assert.assertTrue("xml instanceof XmlExporter", xml instanceof XmlExporter);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testInstantiateWithEmptyOptions()

}

