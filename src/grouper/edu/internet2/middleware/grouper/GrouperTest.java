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
import  junit.framework.*;

/**
 * Grouper-specific JUnit assertions.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperTest.java,v 1.2 2006-09-25 18:17:14 blair Exp $
 * @since   1.1.0
 */
public class GrouperTest extends TestCase {

  // CONSTRUCTORS //

  /** 
   * @since   1.1.0
   */
  public GrouperTest(String name) {
    super(name);
  } // public GrouperTest()


  // PUBLIC INSTANCE METHODS //

  /**  
   * @since   1.1.0
   */
  public void assertDoNotFindGroupByName(GrouperSession s, String name) {
    try {
      GroupFinder.findByName(s, name);
      fail("unexpectedly found group by name: " + name);
    }
    catch (GroupNotFoundException eGNF) {
      assertTrue(true);
    }
  } // public void assertDoNotFindGroupByName(s, name)

  /**  
   * @return  Retrieved {@link Group}.
   * @since   1.1.0
   */
  public Group assertFindGroupByName(GrouperSession s, String name) {
    Group g = null;
    try {
      g = GroupFinder.findByName(s, name);
      assertTrue(true);
    }
    catch (GroupNotFoundException eGNF) {
      fail("did not find group (" + name + ") by name: " + eGNF.getMessage());
    }
    return g;
  } // public Group assertFindGroupByName(s, name)

  /**  
   * @return  Retrieved {@link Stem}.
   * @since   1.1.0
   */
  public Stem assertFindStemByName(GrouperSession s, String name) {
    Stem ns = null;
    try {
      ns = StemFinder.findByName(s, name);
      assertTrue(true);
    }
    catch (StemNotFoundException eNSNF) {
      fail("did not find stem (" + name + ") by name: " + eNSNF.getMessage());
    }
    return ns;
  } // public Stem assertFindStemByName(s, name)

  /**
   * @since   1.1.0
   */
  public void assertHasMember(Group g, Subject subj) {
    assertTrue(
      "group (" + g.getName() + ") has member (" + subj.getId() + ")",
      g.hasMember(subj)
    );
  } // public void assertHasMember(g, subj)

} // public class GrouperTest

