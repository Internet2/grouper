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
import  edu.internet2.middleware.subject.*; // Import Subject API
import  org.apache.commons.logging.*;       // For logging

/**
 * Example: Start and stop a {@link GrouperSession}.
 * @author  blair christensen.
 * @version $Id: StartAndStopSession.java,v 1.2 2006-08-11 18:50:49 blair Exp $
 * @since   1.0.1
 */
public class StartAndStopSession {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(StartAndStopSession.class);


  // MAIN //
  public static void main(String args[]) {
    int exit_value = 0;
    try {
      Subject subj = SubjectFinder.findById(
        "GrouperSystem", "application", InternalSourceAdapter.ID
      );

      try {
        GrouperSession s = GrouperSession.start(subj);
        LOG.info("Started GrouperSession: " + s);
        try {
          s.stop();
          LOG.info("Stopped GrouperSession");
        }
        catch (SessionException eS) {
          LOG.error("Did not stop GrouperSession: " + eS.getMessage());
          exit_value = 1;
        }
      }
      catch (SessionException eS) {
        LOG.error("Failed to start GrouperSession: " + eS.getMessage());
        exit_value = 1;
      }

    }
    catch (Exception e) {
      LOG.error("UNEXPECTED ERROR: " + e.getMessage());
      exit_value = 1;
    }
    System.exit(exit_value);
  } // public static void main(args[])

} // public class StartAndStopSession

