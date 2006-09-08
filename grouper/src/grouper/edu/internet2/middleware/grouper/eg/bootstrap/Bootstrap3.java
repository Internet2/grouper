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
 * Step 3: Find the root stem.
 * <p>
 * Now that you have a <a href="./Bootstrap2.html">session</a> you can begin to
 * put it to use.  Before you can begin adding stems, groups and memberships to
 * your Groups Registry you will first need to retrieve the registry's
 * <b>root stem</b>.  The root stem was created when you ran <tt>ant
 * db-init</tt> and until you finish bootstrapping your registry is is the only
 * stem within it.  All groups and stems that you add to your Groups Registry
 * will exist beneath the root stem which serves to anchor the entire Groups
 * Registry.
 * </p>
 * <p>
 * You retrieve the root stem using the {@link StemFinder#findRootStem(GrouperSession)}
 * method.  Upon retrieving the root stem, the API will associate your session
 * with it.  All operations you perform on the root stem will be performed
 * within your session context.
 * </p>
 * <pre class="eg">
 * Stem rootStem = StemFinder.findRootStem(s);
 * </pre>
 * <p>
 * If the API is unable to retrieve the root stem a {@link GrouperRuntimeException} 
 * will be thrown.  The example code does not bother catching that exception as
 * the root stem should <b>never</b> be absent and if it is your Groups Registry
 * is presumably either not fully installed or misconfigured.
 * </p>
 * @author  blair christensen.
 * @version $Id: Bootstrap3.java,v 1.2 2006-09-08 19:33:15 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap3.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap3 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap3.class);


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

          Stem rootStem = StemFinder.findRootStem(s);
          LOG.info(
            "Found root stem: name=" + rootStem.getName() + " uuid=" + rootStem.getUuid()
          );

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

} // public class Bootstrap3

