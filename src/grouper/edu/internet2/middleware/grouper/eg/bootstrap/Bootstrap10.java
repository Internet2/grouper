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
 * Step 10: Add wheel group member.
 * <p>
 * If the <a href="./Bootstrap8.html">subject</a> is 
 * <a href="./Bootstrap9.html">not a member</a> of the wheel group you may call
 * the wheel group's <tt>addMember()</tt> method to make the subject a member.
 * </p>
 * <pre class="eg">
 * wheel.addMember(subject);
 * </pre>
 * <p>
 * If the subject cannot be added to the group two exceptions may be thrown:
 * </p>
 * <pre class="eg">
 * catch (InsufficientPrivilegeException eIP) {
 *   // not privileged to add member
 * }
 * catch (MemberAddException eMA) {
 *   // error adding member 
 * }
 * </pre>
 * @author  blair christensen.
 * @version $Id: Bootstrap10.java,v 1.2 2007-01-04 17:17:45 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap10.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap10 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap10.class);


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
              if (wheel.hasMember(all)) {
                // subject already a member
                LOG.info("GrouperAll is already a member of the wheel group");
              }
              else {
                // not a member
                LOG.info("GrouperAll is not a member of the wheel group");
                try {
                  wheel.addMember(all);
                  LOG.info("Added GrouperAll as member of wheel group");
                  exit_value = 0;
                }
                catch (InsufficientPrivilegeException eIP) {
                  // not privileged to add member
                  LOG.error(eIP.getMessage()); 
                }
                catch (MemberAddException eMA) {
                  // error adding member 
                  LOG.error(eMA.getMessage());
                }
              } 
            }
          }

          s.stop();
          LOG.info("Stopped GrouperSession");
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

} // public class Bootstrap10

