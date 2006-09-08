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

package edu.internet2.middleware.grouper.eg.bootstrap;
import  edu.internet2.middleware.grouper.*; // Import Grouper API
import  edu.internet2.middleware.subject.*; // Import Subject API
import  org.apache.commons.logging.*;       // For logging

/**
 * Step 2: Start-and-stop sessions.
 * <p>
 * Using the <code>Subject</code> object retrieved in the 
 * <a href="./Bootstrap1.html">previous example</a> you can now start a 
 * {@link GrouperSession}.  Your retrieved subject will be associated with the
 * newly created session and all operations performed within this session
 * context will be restricted by privileges held by the subject.  As the subject
 * in this case is <i>GrouperSystem</i> there will be <b>no</b> privilege
 * restrictions.
 * </p>
 * <pre class="eg">
 * GrouperSession s = GrouperSession.start(root);
 * </pre>
 * <p>
 * A {@link SessionException} may be thrown while starting a session:
 * </p>
 * <pre class="eg">
 * catch (SessionException eStart) {
 *   // Error starting session
 * }
 * </pre> 
 * <p>
 * Once you are done with a session you stop it:
 * </p>
 * <pre class="eg">
 * s.stop();
 * </pre>
 * <p>
 * Stopping a session may also throw a {@link SessionException}.
 * </p>
 * <pre class="eg">
 * catch (SessionException eStop) {
 *   // Error stopping session
 * }
 * </pre> 
 * @author  blair christensen.
 * @version $Id: Bootstrap2.java,v 1.2 2006-09-08 19:33:15 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap2.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap2 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap2.class);


  // MAIN //
  public static void main(String args[]) {
    int exit_value = 1; // Indicate failure by default
    try {
      String  subjectId = "GrouperSystem";
      Subject root      = SubjectFinder.findById(subjectId);
      LOG.info("Found GrouperSystem: " + root.getId());

      try {
        GrouperSession s = GrouperSession.start(root);
        LOG.info("Started GrouperSession as " + s.getSubject().getId());
        try {
          s.stop();
          LOG.info("Stopped GrouperSession");
          exit_value = 0;
        }
        catch (SessionException eStop) {
          // Error stopping session
          LOG.error(eStop.getMessage());
        } 
      }
      catch (SessionException eStart) {
        // Error starting session
        LOG.error(eStart.getMessage());
      }

    }
    catch (SubjectNotFoundException   eSNF) {
      // No matching subject id found
      LOG.error(eSNF.getMessage());
    }
    catch (SubjectNotUniqueException  eSNU) {
      // More than one subject with this subject id was found
      LOG.error(eSNU.getMessage());
    }
    System.exit(exit_value);
  } // public static void main(args[])

} // public class Bootstrap2

