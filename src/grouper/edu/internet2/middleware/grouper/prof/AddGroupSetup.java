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
import  org.apache.commons.logging.*;      

/**
 * @author  blair christensen.
 * @version $Id: AddGroupSetup.java,v 1.1 2006-08-15 15:32:21 blair Exp $
 * @since   1.1.0
 */
public class AddGroupSetup extends BaseGrouperProfSetup {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(AddGroupSetup.class);


  // MAIN //
  public static void main(String args[]) {
    RegistryReset.reset();
    try { 
      Stem root = StemFinder.findRootStem(
        GrouperSession.start(
          SubjectFinder.findById("GrouperSystem")
        )
      );
      root.addChildStem(AddGroup.NS, AddGroup.NS);
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    System.exit(0); 
  } // public static void main(args[])

} // public class AddGroupSetup

