/*
  Copyright 2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006 The University Of Chicago

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

package edu.internet2.middleware.grouper.prof;
import  edu.internet2.middleware.grouper.*; 
import  edu.internet2.middleware.subject.*; 

/**
 * Profile adding a {@link Group}.
 * @author  blair christensen.
 * @version $Id: AddGroup.java,v 1.1 2006-08-15 15:32:21 blair Exp $
 * @since   1.1.0
 */
public class AddGroup {

  // PROTECTED CLASS CONSTANTS //
  protected static final String NS = "example";


  // MAIN //

  /**
   * @since 1.1.0
   */
  public static void main(String args[]) {
    try {
      // TODO Argh.  Ideally this wouldn't be here.
      Stem  ns  = StemFinder.findByName(
        GrouperSession.start(
          SubjectFinder.findById("GrouperSystem")
        ),
        NS
      );
      Group g   = ns.addChildGroup("group", "group");
      System.exit(0);
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  } // public static void main(args[])

} // public class AddGroup

