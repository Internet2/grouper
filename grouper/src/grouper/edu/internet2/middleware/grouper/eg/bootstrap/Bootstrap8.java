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
 * Step 8: Find <i>GrouperAll</i> subject.
 * <p>
 * Once you have <a href="./Bootstrap7.html">created</a> or 
 * <a href="./Bootstrap6.html">retrieved</a> your wheel group there are only
 * additional steps before your Groups Registry is bootstrapped.  You must add 
 * a member to the wheel group and then you must enable the wheel group.  To
 * begin adding a member to the wheel group you will first need to retrieve the
 * subject you wish to add as a member.  As this is going to be a very
 * egalitarian Groups Registry the <i>GrouperAll</i> subject will be added as a
 * member.  To find the <i>GrouperAll</i> subject you may use the 
 * <tt>SubjectFinder.findAllSubject()</tt> convenience method.
 * </p>
 * <pre class="eg">
 * Subject all = SubjectFinder.findAllSubject();
 * </pre>
 * <p>
 * Adding the <i>GrouperAll</i> subject to your wheel group will give
 * <b>everyone</b> root-like privileges over your entire registry.  This is not
 * recommended for most environments.
 * </p>
 * @author  blair christensen.
 * @version $Id: Bootstrap8.java,v 1.2 2007-01-04 17:17:45 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap8.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap8 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap8.class);


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
            // add member if not already a member
            if (wheel != null) {
              Subject all = SubjectFinder.findAllSubject();
              LOG.info("Found GrouperAll: " + all.getId());
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

} // public class Bootstrap8

