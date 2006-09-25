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
 * Step 5: Create stem.
 * <p>
 * If the <i>etc</i> stem <a href="./Bootstrap4.html">does not exist</a> your
 * next step in bootstrapping your Groups Registry will be to create it.
 * </p>
 * <p>
 * To create a new stem you need three things:
 * </p>
 * <ol>
 * <li>The <a href="./Bootstrap3.html">parent stem</a></li>
 * <li>The <tt>extension</tt> to assign to the new stem</li>
 * <li>The <tt>displayExtension</tt> to assign to the new group.</li>
 * </ol>
 * <p>
 * To actually create the new child stem you will call the
 * <tt>addChildStem()</tt> method on the parent stem.  That will create the new
 * stem beneath the parent and it takes the <tt>extension</tt> and
 * <tt>displayExtension</tt> as parameters.
 * </p>
 * <pre class="eg">
 * Stem etc = rootStem.addChildStem("etc", "Grouper Administration");
 * </pre>
 * <p>
 * Creating a child stem may throw two possible exceptions:
 * </p>
 * <pre class="eg">
 * catch (InsufficientPrivilegeException eIP) {
 *   // Not privileged to create stem
 * }
 * catch (StemAddException eNSA) {
 *   // Error adding stem
 * }
 * catch (StemAddException eNSA) {
* }
 * </pre>
 * @author  blair christensen.
 * @version $Id: Bootstrap5.java,v 1.1 2006-09-25 14:37:07 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap5.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap5 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap5.class);


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

          Stem etc;
          try {
            etc = StemFinder.findByName(s, "etc");
            LOG.info("Found stem: name=" + etc.getName() + " uuid=" + etc.getUuid());
          }
          catch (StemNotFoundException eNSNF) {
            LOG.info(eNSNF.getMessage());
            try {
              etc = rootStem.addChildStem("etc", "GrouperAdministration");  
              LOG.info("Created stem: name=" + etc.getName() + " uuid=" + etc.getUuid());
            }
            catch (InsufficientPrivilegeException eIP) {
              // Not privileged to create stem
              LOG.error(eIP.getMessage());
            }
            catch (StemAddException eNSA) {
              // Error adding stem
              LOG.error(eNSA.getMessage());
            }
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

} // public class Bootstrap5

