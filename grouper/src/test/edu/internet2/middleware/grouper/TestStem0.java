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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestStem0.java,v 1.5.2.1 2008-03-19 18:46:11 mchyzer Exp $
 */
public class TestStem0 extends GrouperTest {

  // Private Static Class Constants
  /** log */
  private static final Log LOG = LogFactory.getLog(TestStem0.class);

  /**
   * ctor
   * @param name name
   */
  public TestStem0(String name) {
    super(name);
  }

  /**
   * set up
   */
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  /** 
   * tear down
   */
  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * test delete
   */
  public void testDeleteEmptyStem() {
    LOG.info("testDeleteEmptyStem");
    try {
      R r = R.populateRegistry(0, 0, 0);
      r.ns.delete();
      Assert.assertTrue("deleted stem", true);
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteEmptyStem()

  /**
   * test static save stem
   * @throws Exception if problem
   */
  public void testStaticSaveStem() throws Exception {
    
    R.populateRegistry(1, 2, 0);
    
    String displayExtension = "testing123 display";
    GrouperSession rootSession = SessionHelper.getRootSession();
    String stemDescription = "description";
    try {
      String stemNameNotExist = "whatever123:whatever:testing123";
      
      GrouperTest.deleteAllStemsIfExists(rootSession, stemNameNotExist);
      
      Stem.saveStem(rootSession, stemNameNotExist, stemDescription, 
          displayExtension, stemNameNotExist, 
          null, SaveMode.UPDATE, false);
      fail("this should fail, since stem doesnt exist");
    } catch (StemNotFoundException e) {
      //good, caught an exception
      //e.printStackTrace();
    }
    
    /////////////////////////////////
    String stemName = "i2:a:testing123";
    GrouperTest.deleteStemIfExists(rootSession, stemName);
    
    //////////////////////////////////
    //this should insert
    Stem createdStem = Stem.saveStem(rootSession, null, stemDescription, 
        displayExtension, stemName, 
        null, SaveMode.INSERT, false);
    
    //now retrieve
    Stem foundStem = StemFinder.findByName(rootSession, stemName);
    
    assertEquals(stemName, createdStem.getName());
    assertEquals(stemName, foundStem.getName());
    
    assertEquals(displayExtension, createdStem.getDisplayExtension());
    assertEquals(displayExtension, foundStem.getDisplayExtension());
    
    assertEquals(stemDescription, createdStem.getDescription());
    assertEquals(stemDescription, foundStem.getDescription());
    
    ///////////////////////////////////
    //this should update by uuid
    createdStem = Stem.saveStem(rootSession, stemName, stemDescription + "1", 
        displayExtension, stemName, 
        createdStem.getUuid(), SaveMode.INSERT_OR_UPDATE, false);
    assertEquals("this should update by uuid", stemDescription + "1", createdStem.getDescription());
    
    //this should update by name
    createdStem = Stem.saveStem(rootSession, stemName, stemDescription + "2", 
        displayExtension, stemName, 
        null, SaveMode.UPDATE, false);
    assertEquals("this should update by name", stemDescription + "2", createdStem.getDescription());
    
    /////////////////////////////////////
    //create a stem that creates a bunch of stems
    String stemsNotExist = "whatever123:heythere:another";
    //lets also delete those stems
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    createdStem = Stem.saveStem(rootSession, stemsNotExist, stemDescription, 
        displayExtension, stemsNotExist, 
        null, SaveMode.INSERT_OR_UPDATE, true);
    
    assertEquals(stemDescription, createdStem.getDescription());
    //clean up
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    
    rootSession.stop();
    
  }

}

