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
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestXmlImport2.java,v 1.1 2006-07-03 19:01:50 blair Exp $
 * @since   1.0
 */
public class TestXmlImport2 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestXmlImport2.class);

  public TestXmlImport2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetDefaultOptions() {
    LOG.info("testGetDefaultOptions");
    try {
      XmlImporter xml     = new XmlImporter( new Properties() );  
      Properties  options = xml.getOptions();
      Assert.assertNotNull("options !null", options);
      Assert.assertTrue("0 set options", options.size() == 0);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetDefaultOptions()

}

