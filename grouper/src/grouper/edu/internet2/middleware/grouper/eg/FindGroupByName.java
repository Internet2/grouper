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

package edu.internet2.middleware.grouper.eg;
import  edu.internet2.middleware.grouper.*; // Import Grouper API
import  org.apache.commons.logging.*;       // For logging

/**
 * Example: Find a {@link Group} by name within the Groups Registry.
 * @author  blair christensen.
 * @version $Id: FindGroupByName.java,v 1.5 2006-08-30 19:31:02 blair Exp $
 * @since   1.1.0
 */
public class FindGroupByName {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(FindGroupByName.class);


  // MAIN //
  public static void main(String args[]) {
    int exit_value = 0;
    try {
      GrouperSession s = GrouperSession.start(
        SubjectFinder.findById(
          "GrouperSystem", "application", InternalSourceAdapter.ID
        )
      );
    
      try {
        String  name  = "etc:wheel";
        GroupFinder.findByName(s, name);
        LOG.info("Found Group by name: " + name);
      }
      catch (GroupNotFoundException eGNF) {
        LOG.error(eGNF.getMessage());
        exit_value = 1;
      }

      s.stop();
    }
    catch (Exception e) {
      LOG.error("UNEXPECTED ERROR: " + e.getMessage());
      exit_value = 1;
    }
    System.exit(exit_value);
  } // public static void main(args[])

} // public class FindGroupByName

