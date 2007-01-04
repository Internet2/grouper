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
 * Step 7: Create group.
 * <p>
 * If the <i>etc:wheel</i> group <a href="./Bootstrap6.html">does not exist</a> 
 * you can now create it.
 * </p>
 * <p>
 * Creating a new child group is just like creating a new child stem.  You will
 * need the parent stem along with the <tt>extension</tt> and
 * <tt>displayExtension</tt> to assign to the new group.
 * </p>
 * <pre class="eg">
 * Group wheel = etc.addChildGroup("wheel", "Wheel Group");
 * </pre>
 * <p>
 * This will create a group beneath the <i>etc</i> stem with the <tt>name</tt>
 * <i>etc:wheel</i> and the <tt>displayName</tt> <i>Grouper Administration:Wheel
 * Group</i>.
 * </p>
 * <p>
 * If the group cannot be created several exceptions may be thrown.
 * </p>
 * <pre class="eg">
 * catch (GroupAddException eGA) {
 *   // Error adding group
 * }
 * catch (InsufficientPrivilegeException eIP) {
 *   // Not privileged to add group
 * }
 * </pre>
 * @author  blair christensen.
 * @version $Id: Bootstrap7.java,v 1.2 2007-01-04 17:17:45 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap7.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap7 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap7.class);


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

          // add-or-retrieve stem
          Stem etc = null;
          try {
            etc = StemFinder.findByName(s, "etc");
            LOG.info("Found stem: name=" + etc.getName() + " uuid=" + etc.getUuid());
          }
          catch (StemNotFoundException eNSNF) {
            LOG.info(eNSNF.getMessage());
            try {
              etc = rootStem.addChildStem("etc", "Grouper Administration");  
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
          // add-or-retrieve group
          if (etc != null) {
            Group wheel = null;
            try {
              wheel = GroupFinder.findByName(s, "etc:wheel");
              LOG.info("Found group: name= " + wheel.getName() + " uuid=" + wheel.getUuid());
            }
            catch (GroupNotFoundException eGNF) {
              LOG.info(eGNF.getMessage());
              try {
                wheel = etc.addChildGroup("wheel", "Wheel Group");
                LOG.info("Created group: name=" + wheel.getName() + " uuid=" + wheel.getUuid());
              }
              catch (GroupAddException eGA) {
                // Error adding group
                LOG.error(eGA.getMessage());
              }
              catch (InsufficientPrivilegeException eIP) {
                // Not privileged to add group
                LOG.error(eIP.getMessage());
              }
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

} // public class Bootstrap7

