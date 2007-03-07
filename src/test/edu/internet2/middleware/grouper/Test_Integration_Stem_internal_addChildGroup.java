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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_Stem_internal_addChildGroup.java,v 1.1 2007-03-07 20:30:44 blair Exp $
 * @since   1.2.0
 */
public class Test_Integration_Stem_internal_addChildGroup extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Integration_Stem_internal_addChildGroup.class);


  // TESTS //  

  public void testInternal_AddChildGroup_ReuseExistingMemberAfterGroupDeletion() {
    try {
      LOG.info("testInternal_AddChildGroup_ReuseExistingMemberAfterGroupDeletion");
      RegistryReset.reset();
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      Stem            root  = StemFinder.findRootStem(s).addChildStem("uchicago", "uchicago");
      Stem            ns    = root.addChildStem("nsit", "nsit");
      Group           g     = ns.addChildGroup("nas", "nas");
      Member          m     = g.toMember();

      g.delete();
      g = ns.internal_addChildGroup("nas", "nas", g.getUuid() );
      assertEquals( "reusing existing member definition", m, g.toMember() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testInternal_AddChildGroup_ReuseExistingMemberAfterGroupDeletion()

  public void testInternal_AddChildGroup_CreateNewMemberAfterRegistryReset() {
    try {
      LOG.info("testInternal_AddChildGroup_CreateNewMemberAfterRegistryReset");
      RegistryReset.reset();
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      Stem            root  = StemFinder.findRootStem(s).addChildStem("uchicago", "uchicago");
      Stem            ns    = root.addChildStem("nsit", "nsit");
      Group           g     = ns.addChildGroup("nas", "nas");
      Member          m     = g.toMember();

      RegistryReset.reset();
      root  = StemFinder.findRootStem(s).addChildStem("uchicago", "uchicago");
      ns    = root.addChildStem("nsit", "nsit");
      g     = ns.internal_addChildGroup("nas", "nas", g.getUuid() );
      assertTrue("new member definition created", !m.getUuid().equals( g.toMember().getUuid() ) );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testInternal_AddChildGroup_CreateNewMemberAfterRegistryReset()

} // public class Test_Integration_Stem_internal_addChildGroup extends GrouperTest

