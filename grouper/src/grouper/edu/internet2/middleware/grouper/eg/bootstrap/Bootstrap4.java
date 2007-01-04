/*
  Copyright (C) 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2006-2007 The University Of Chicago

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
 * Step 4: Find stem by name.
 * <p>
 * Now that you have the <a href="./Bootstrap3.html">root stem</a> you may
 * create child stems beneath it.  Groups may <b>not</b> be created directly
 * beneath the root stem.  As you are bootstrapping your Groups Registry you are
 * interested in creating a stem named <i>etc</i>.
 * </p>
 * <p>
 * At this point you <b>could</b> just attempt to create your new stem and rely
 * upon an exception being thrown to indicate failure.  However, you might want
 * to make your code more resilient by first checking to see if the <i>etc</i>
 * stem already exists.  If it exists, you can skip ahead in the bootstrap
 * process; if it does not you can then create the stem.
 * </p>
 * <p>
 * You will use the <tt>StemFinder.findByName()</tt> method to determine if the
 * stem exists.  If it does not a <tt>StemNotFoundException</tt> will be thrown.
 * If it does, the stem will be returned.
 * </p>
 * </p>
 * <pre class="eg">
 * Stem etc = StemFinder.findByName(s, "etc");
 * </pre>
 * <p>
 * @author  blair christensen.
 * @version $Id: Bootstrap4.java,v 1.3 2007-01-04 17:17:45 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap4.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap4 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap4.class);


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

          Stem etc = null;
          try {
            etc = StemFinder.findByName(s, "etc");
            LOG.info("Found stem: name=" + etc.getName() + " uuid=" + etc.getUuid());
          }
          catch (StemNotFoundException eNSNF) {
            LOG.info(eNSNF.getMessage());
          }

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

} // public class Bootstrap4

