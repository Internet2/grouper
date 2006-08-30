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
 * Example: Add a root-level {@link Stem} to the Groups Registry.
 * @author  blair christensen.
 * @version $Id: AddRootStem.java,v 1.4 2006-08-30 18:35:38 blair Exp $
 * @since   1.1.0
 */
public class AddRootStem {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(AddRootStem.class);


  // MAIN //
  public static void main(String args[]) {
    int exit_value = 0;
    try {
      GrouperSession  s     = GrouperSession.start(
        SubjectFinder.findById(
          "GrouperSystem", "application", InternalSourceAdapter.ID
        )
      );
      Stem            root  = StemFinder.findRootStem(s);

      try {
        String  extension         = "etc";
        String  displayExtension  = "Grouper Administration";
        root.addChildStem(extension, displayExtension);
        LOG.info("Added root-level Stem: " + extension);
      }
      catch (InsufficientPrivilegeException eIP) {
        LOG.error(eIP.getMessage());
        exit_value = 1;
      }
      catch (StemAddException eSA) {
        LOG.error(eSA.getMessage());
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

} // public class AddRootStem

