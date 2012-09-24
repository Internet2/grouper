/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

package edu.internet2.middleware.grouper.eg;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * EXAMPLE Bootstrap your Groups Registry by creating a sysadmin (wheel) group.
 * <p>Running <code>ant eg.bootstrap</code> will run this example code.</p>
 * <h4>Step 0: Import Required Packages</h4>
 * <p>To begin using the Grouper API you will first need to import the Grouper API,
 * located in the <i>edu.internet2.middleware.grouper</i> package.  For many operations,
 * including those in this example, you will also need to import the I2MI Subject API
 * which is located in the <i>edu.internet2.middleware.subject</i> package.<p>
 * <pre class="eg">
 * import edu.internet2.middleware.grouper.*; // Import Grouper API
 * import edu.internet2.middleware.subject.*; // Import Subject API
 * </pre>
 * <h4>Step 1: Find <i>GrouperSystem</i> Subject</h4>
 * <p>The next step is to find the <i>GrouperSystem</i> subject.  This subject is internal
 * to the Grouper API and is the "root" user for your Groups Registry.  As normal access
 * and naming privileges do not apply to <i>GrouperSystem</i> you must as as this subject
 * to bootstrap your registry for use by others.</p>
 * <pre class="eg">
 * Subject grouperSystem = SubjectFinder.findRootSubject();
 * </pre>
 * <h4>Step 2: Start Session</h4>
 * <p>Almost all Grouper API operations take place within the context of a
 * <code>GrouperSession</code> and each session has an associated subject.  The privilegs
 * of the session's subject determine what API actions can be performed.  As this session
 * will be acting as <i>GrouperSystem</i> there will be no restrictions.</code>
 * <pre class="eg">
 * try {
 *  GrouperSession s = GrouperSession.start(grouperSystem);  
 * }
 * catch (SessionException eS) {
 *   // Error starting session
 * }
 * </pre>
 * <h4>Step 3: Find Root Stem</h4>
 * <p>At the very base of the Groups Registry is the root stem.  All other stems and
 * groups in the registry descend from this stem.</p>
 * <pre class="eg">
 * Stem root = StemFinder.findRootStem(s);
 * </pre>
 * <h4>Step 4: Find Or Add Top-Level Stem "etc" (or whatever is configured in grouper.properties)</h4>
 * <p>After retrieving the root stem you can create a new top-level stem, in this case
 * named "etc", beneath the root stem.  
 * <pre class="eg">
 * Stem   etc;
 * String extn         = "etc";
 * String displayExtn  = "Grouper Administration";
 * // check to see if stem already exists
 * try {
 *   etc = StemFinder.findByName(s, extn);
 * }
 * catch (StemNotFoundException eNSNF) {
 *   // create stem if it doesn't exist
 *   try {
 *     etc = root.addChildStem(extn, displayExtn);
 *   }
 *   catch (InsufficientPrivilegeException eIP) {
 *     // not privileged to create top-level stem
 *   }
 *   catch (StemAddException eNSA) {
 *     // error adding top-level stem
 *   }
 * }
 * </pre>
 * However, we are creating a dynamically named stem based on the wheel group named in grouper.properties:
 * <pre class="eg">
 *    try {
 *      this.etc = Stem.saveStem(this.s, etcStem, null, etcStem, displayExtn, null, SaveMode.INSERT_OR_UPDATE, true);
 *      System.err.println("created top-level stem: " + this.etc);
 *    }
 *    catch (Exception eIP) {
 *      throw new GrouperRuntimeException( "error adding top-level stem: " + eIP.getMessage() + ", " + etcStem, eIP );
 *    }
 * </pre> 
 * <h4>Step 5: Find Or Add Sysadmin Group "etc:sysadmin" (or whatever is configured in grouper.properties)</h4>
 * <p>After adding the top-level "etc" stem you can then create the wheel group
 * ("etc:sysadmin") beneath it.</p>
 * <pre class="eg">
 * Group   wheel;
 * String  extn        = "wheel";
 * String  displayExtn = "Wheel Group";
 * // check to see if group exists
 * try {
 *   wheel = GroupFinder.findByName( s, etc.getName() + ":" + extn );
 * }
 * catch (GroupNotFoundException eGNF) {
 *   try {
 *     // create group if it doesn't exist
 *     wheel = etc.addChildGroup(extn, displayExtn);
 *   }
 *   catch (GroupAddException eGA) {
 *     // error adding wheel group
 *   } 
 *   catch (InsufficientPrivilegeException eIP) {
 *     // not privileged to create wheel group
 *   }
 * }
 * </pre>
 * However, we are doing things more dynamically:
 * <pre class="eg">
 *    try {
 *      // create group if it doesn't exist
 *      this.wheel = Group.saveGroup(this.s, wheelGroupName, null, wheelGroupName, displayExtn, null, SaveMode.INSERT_OR_UPDATE, true);
 *      System.err.println("created sysadmin (wheel) group: " + this.wheel);
 *    }
 *    catch (Exception eGA) {
 *      throw new GrouperRuntimeException( "error adding sysadmin group: " + eGA.getMessage() + ", " + wheelGroupName, eGA );
 *    }
 * </pre>
 * <h4>Step 6: Find <i>GrouperAll</i> Subject</h4>
 * <p><i>GrouperAll</i> is another subject internal to Grouper.  When you assign a
 * membership or grant a privilege to <i>GrouperAll</i> it is the equivalent of performing
 * that operation for <b>*all subjects*.</b>.</p>
 * <pre class="eg">
 * Subject grouperAll = SubjectFinder.findAllSubject();
 * </pre>
 * <h4>Step 7: Add <i>GrouperAll</i> As Member Of The Wheel Group</h4>
 * <p>Now that the wheel group exists you may add members to it.  Provided you have
 * enabled use of a wheel group in <code>conf/grouper.properties</code> all members added
 * to this group will now have root-like privileges over the entire Groups Registry.</p>
 * <pre class="eg">
 * // verify GrouperAll is not already a member before attempting to add it
 * if ( !wheel.hasMember(grouperAll) ) {
 *   try {
 *     // this is *not* recommend for most grouper deployments as it will give every
 *     // subject root-like privileges over the entire groups registry. 
 *     wheel.addMember(grouperAll);
 *   }
 *   catch (InsufficientPrivilegeException eIP) {
 *     // not privileged to add GrouperAll as member to wheel group
 *   }
 *   catch (MemberAddException eMA) {
 *     // error adding GrouperAll as member to wheel group
 *   }
 * }
 * </pre>
 * <h4>Step 8: Stop Session</h4>
 * <p>The final action after bootstrapping your Groups Registry is to stop the session you
 * started at the beginning of the bootstrapping process.</p>
 * <pre class="eg">
 * try {
 *  s.stop();
 * }
 * catch (SessionException eS) {
 *   // Error stopping session
 * }
 * </pre>
 * @author  blair christensen.
 * @version $Id: Bootstrap.java,v 1.6 2009-03-15 06:37:24 mchyzer Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/Bootstrap.java?root=I2MI&view=markup">Source</a>
 * @since   1.2.0
 */
public class Bootstrap {

  // PRIVATE INSTANCE VARIABLES //
  private Stem            etc;
  private Subject         grouperAll;
  private Subject         grouperSystem;
  private Stem            root; 
  private GrouperSession  s;
  private Group           wheel;

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Bootstrap.class);


  // MAIN //

  /**
   * Run example code.
   * @since   1.2.0
   */
  public static void main(String args[]) {
    int exitValue = 1; // indicate failure by default

    try {
      final Bootstrap bs = new Bootstrap();
      bs.findGrouperSystem();
      bs.startSession();
      GrouperSession.callbackGrouperSession(bs.s, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          bs.findRootStem();
          bs.findOrAddTopLevelStem();
          bs.findOrAddWheelGroup();
          bs.findGrouperAll();
          bs.addGrouperAllToWheelGroupIfNotAlreadyAMember();
          return null;
        }
        
      });
      bs.stopSession();
      exitValue = 0;
    }
    catch (GrouperException eGRE) {
      System.err.println( eGRE.getMessage() );
    }

    System.exit(exitValue);
  }


  // CONSTRUCTORS //

  private Bootstrap() {
    super();
  }
  

  // PRIVATE INSTANCE METHODS //

  private void addGrouperAllToWheelGroupIfNotAlreadyAMember() {
    // verify GrouperAll is not already a member before attempting to add it
    if ( !this.wheel.hasMember(grouperAll) ) {
      try {
        // this is *not* recommend for most grouper deployments as it will give every
        // subject root-like privileges over the entire groups registry. 
        this.wheel.addMember(grouperAll);
        System.err.println("added GrouperAll to sysadmin (wheel) group");
      }
      catch (InsufficientPrivilegeException eIP) {
        throw new GrouperException( "error adding GrouperAll to sysadmin (wheel) group: " + eIP.getMessage(), eIP );
      }
      catch (MemberAddException eMA) {
        throw new GrouperException( "error adding GrouperAll to sysadmin (wheel) group: " + eMA.getMessage(), eMA );
      }
    }
    else {
      System.err.println("GrouperAll is already a member of the wheel group");
    }
  }

  private void findGrouperAll() {
    this.grouperAll = SubjectFinder.findAllSubject();
    System.err.println("found GrouperAll subject: " + this.grouperAll);
  }

  private void findGrouperSystem() {
    this.grouperSystem = SubjectFinder.findRootSubject();
    System.err.println("found GrouperSystem subject: " + this.grouperSystem);
  }

  private void findOrAddTopLevelStem() {
    String etcStem = GrouperUtil.parentStemNameFromName(this.wheelGroupName());
    String displayExtn  = "Grouper Administration";
    // check to see if stem exists
    try {
      this.etc = StemFinder.findByName(this.s, etcStem, true);
      System.err.println("found top-level stem: " + this.etc);
    }
    catch (StemNotFoundException eNSNF) {
      // create stem if it doesn't exist
      try {
        this.etc = Stem.saveStem(this.s, etcStem, null, etcStem, displayExtn, null, SaveMode.INSERT_OR_UPDATE, true);
        System.err.println("created top-level stem: " + this.etc);
      }
      catch (Exception eIP) {
        throw new GrouperException( "error adding top-level stem: " + eIP.getMessage() + ", " + etcStem, eIP );
      }
    }
  }

  private void findRootStem() {
    this.root = StemFinder.findRootStem(this.s);
    System.err.println("found root stem: " + this.root);
  }

  private void findOrAddWheelGroup() {
    String wheelGroupName = wheelGroupName();
    String  extn        = GrouperUtil.extensionFromName(wheelGroupName);
    
    //if etc:wheel (the old way), use that as the friendly name
    String  displayExtn = StringUtils.equals(wheelGroupName, "etc:wheel") ? "Wheel Group" 
        : "Sysadmin group";
    // check to see if group exists
    try {
      this.wheel = GroupFinder.findByName( s, this.etc.getName() + ":" + extn , true);
      System.err.println("found sysadmin (wheel) group: " + this.wheel);
    }
    catch (GroupNotFoundException eGNF) {
      try {
        // create group if it doesn't exist
        this.wheel = Group.saveGroup(this.s, wheelGroupName, null, wheelGroupName, displayExtn, displayExtn, SaveMode.INSERT_OR_UPDATE, true);
        System.err.println("created sysadmin (wheel) group: " + this.wheel);
      }
      catch (Exception eGA) {
        throw new GrouperException( "error adding sysadmin group: " + eGA.getMessage() + ", " + wheelGroupName, eGA );
      }
    }
  }


  /**
   * @return
   */
  private String wheelGroupName() {
    String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
    
    if (StringUtils.isBlank(wheelGroupName)) {
      throw new RuntimeException("grouper.properties must have an extry for " +
          "groups.wheel.group, e.g. etc:sysadmingroup");
    }
    return wheelGroupName;
  }

  private void startSession() {
    try {
      this.s = GrouperSession.start(this.grouperSystem, false);  
      System.err.println("started session: " + this.s);
    }
    catch (SessionException eS) {
      throw new GrouperException( "error starting session: " + eS.getMessage(), eS );
    }
  }

  private void stopSession() {
    try {
      this.s.stop();
      System.err.println("stopped session");
    }
    catch (SessionException eS) {
      throw new GrouperException( "error stopping session: " + eS.getMessage(), eS );
    }
  }

}

