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
 * Step 6: Find group by name.
 * <p>
 * Now that you have either <a href="./Bootstrap4.html">retrieved</a> or 
 * <a href="./Bootstrap5.html">created</a> the <i>etc</i> stem you may now do
 * the same for the wheel group.
 * </p>
 * <p>
 * As you did with the <i>etc</i> stem, first check to see if the group exists
 * before trying to create it.  Finding a group is extremely similar to finding
 * a stem as you will need your session and a name to query on.  The only
 * difference is that you will use the <tt>GroupFinder.findByName()</tt> method
 * instead of the <tt>StemFinder.findByName()</tt> method.  As you are looking
 * for a group with the extension <i>wheel</i> beneath the stem named <i>etc</i>
 * the name of the group you are looking for will be <i>etc:wheel</i>.
 * </p>
 * <p>
 * <pre class="eg">
 * Group wheel = GroupFinder.findByName(s, "etc:wheel");
 * </pre>
 * <p>
 * If the group is not found a <tt>GroupNotFoundException</tt> will be thrown.
 * </p>
 * </pre>
 * @author  blair christensen.
 * @version $Id: Bootstrap6.java,v 1.3 2007-01-04 17:17:45 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap6.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap6 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap6.class);


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
          if (etc != null) {
            Group wheel = null;
            try {
              wheel = GroupFinder.findByName(s, "etc:wheel");
              LOG.info("Found group: name= " + wheel.getName() + " uuid=" + wheel.getUuid());
            }
            catch (GroupNotFoundException eGNF) {
              LOG.info(eGNF.getMessage());
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

} // public class Bootstrap6

