/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.privs;
import java.util.Set;

import net.sf.ehcache.Element;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Decorator that provides <i>Wheel</i> privilege resolution for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: WheelAccessResolver.java,v 1.19 2009-02-27 20:51:46 shilen Exp $
 * @since   1.2.1
 */
public class WheelAccessResolver extends AccessResolverDecorator {

  /** if use wheel group */
  private boolean useWheel    = false;
  
  /** wheel group */
  private Group   wheelGroup;
  
  /** 2007-11-02 Gary Brown
   * Provide cache for wheel group members
   * Profiling showed lots of time rechecking memberships */
  public  static final  String            CACHE_IS_WHEEL_MEMBER = WheelAccessResolver.class.getName() + ".isWheelMember";
  
  /** cache controller */
  private EhcacheController cc;

  /** wheel session */
  private GrouperSession wheelSession = null;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WheelAccessResolver.class);

  /** only log this once... */
  private static boolean loggedWheelGroupMissing = false;
  
  /**
   * @param resolver resolver
   * @since   1.2.1
   */
  public WheelAccessResolver(AccessResolver resolver) {
    super(resolver);
    this.cc = new EhcacheController();
    // TODO 20070816 this is ugly
    String useWheelString = this.getConfig( GrouperConfig.PROP_USE_WHEEL_GROUP );
    this.useWheel = Boolean.valueOf( useWheelString ).booleanValue();
    // TODO 20070816 and this is even worse
    if (this.useWheel) {
      String wheelName = null;
      try {
        wheelName = this.getConfig( GrouperConfig.PROP_WHEEL_GROUP );
        this.wheelSession = GrouperSession.start( SubjectFinder.findRootSubject(), false );
        this.wheelGroup = GroupFinder.findByName(
                            this.wheelSession,
                            wheelName
                          );
      }
      catch (Exception e) {
        
        String error = "Initialisation error with wheel group name '" + wheelName 
          + "': " + e.getClass().getSimpleName() 
          + "\n" + ExceptionUtils.getFullStackTrace(e);

        //only log this once as error, dont log if checking config
        if (!loggedWheelGroupMissing && !GrouperCheckConfig.inCheckConfig) {
        	//OK, so wheel group does not exist. Not fatal...
          LOG.error(error);
          loggedWheelGroupMissing = true;
        } else {
          LOG.debug(error);
        }
        this.useWheel=false;
      }
    }
  }



  /**
   * @see     AccessResolver#getConfig(String)
   * @throws  IllegalStateException if any parameter is null.
   */
  public String getConfig(String key) 
    throws IllegalStateException
  {
    return super.getDecoratedResolver().getConfig(key);
  }

  /**
   * @see     AccessResolver#getGroupsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
	//Get any user privs
	Set<AccessPrivilege> accessPrivs =super.getDecoratedResolver().getPrivileges(group, subject);
	
	//Add any due to Wheel.
	if (this.useWheel) {
	      if ( isWheelMember(subject) ) {
		    Set<Privilege> privs = Privilege.getAccessPrivs();
		    AccessPrivilege ap = null;
		    for(Privilege p : privs) {
			  	//Not happy about the klass but will do for now in the absence of a GrouperSession
			  	if(!p.equals(AccessPrivilege.OPTIN) && !p.equals(AccessPrivilege.OPTOUT)) {
			  		ap = new AccessPrivilege(group,subject,SubjectFinder.findRootSubject(),p,GrouperConfig.getProperty("privileges.access.interface"),false);
			  		accessPrivs.add(ap);
			  	}
		    }
	      }
	}
    return accessPrivs;
  }

  /**
   * @see     AccessResolver#getSubjectsWithPrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getSubjectsWithPrivilege(group, privilege);
  }

  /**
   * @see     AccessResolver#grantPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().grantPrivilege(group, subject, privilege);
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
	//Admin incorporates other privileges - except optin /optout
	//Which we don't want to assume
    if (this.useWheel) {
      if ( isWheelMember(subject) ) {
    	  if(!AccessPrivilege.OPTOUT.equals(privilege) 
    			  && !AccessPrivilege.OPTIN.equals(privilege)) {
    		  return true;
    	  
    	  }
      }
    }
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hasPrivilege(group, subject, privilege);
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(group, privilege);
  }
            
  /**
   * @see     AccessResolver#revokePrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(group, subject, privilege);
  } 
  
  /**
   * Retrieve boolean from cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  Cached return value or null.
   * @since   1.2.1
   */
  private Boolean getFromIsWheelMemberCache(Subject subj) {
    Element el = this.cc.getCache(CACHE_IS_WHEEL_MEMBER).get(subj);
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }
  
  /**
   * Put boolean into cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @param rv 
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(Subject subj,  Boolean rv) {
    this.cc.getCache(CACHE_IS_WHEEL_MEMBER).put( new Element( subj,rv) );
  }
  
  /**
   * Put boolean into cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  if wheel member
   * @since   1.2.1
   */
  private boolean isWheelMember(final Subject subj) {
	  Boolean rv = getFromIsWheelMemberCache(subj);
	  if(rv==null) {
	    
	    rv = (Boolean)GrouperSession.callbackGrouperSession(this.wheelSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          return WheelAccessResolver.this.wheelGroup.hasMember(subj);
        }
	    });
		  
		  putInHasPrivilegeCache(subj, rv);
	  }
	  return rv;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    this.cc.flushCache();
    super.getDecoratedResolver().flushCache();
  }


  /*
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(g1, g2, priv);
  }


  /*
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }            
}

