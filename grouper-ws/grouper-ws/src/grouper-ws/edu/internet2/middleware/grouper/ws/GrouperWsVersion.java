/*
 * @author mchyzer $Id: GrouperWsVersion.java,v 1.10.2.1 2009-01-25 12:58:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * grouper service version
 */
public enum GrouperWsVersion {

  /**
   * grouper version 1.3.1, second build
   */
  v1_4_000(true, 2),

  /**
   * grouper version 1.3.1, second build
   */
  v1_3_001(false, 2),
  
  /**
   * grouper version 1.3, first build
   */
  v1_3_000(false, 1);
  
  /**
   * regex pattern for ws version
   */
  private static Pattern versionPattern = Pattern.compile("^v(\\d+)_(\\d+)_(\\d+)$");
  
  /**
   * see if this version is less than the argument one
   * @param other
   * @return true if less than, false if equal or greater
   */
  public boolean lessThanArg(GrouperWsVersion other) {
    
    if (this == other) {
      return false;
    }
    
    Matcher matcher = versionPattern.matcher(this.name());
    if (!matcher.matches()) {
      throw new RuntimeException("Cant match string: " + this.name());
    }
    int thisMajorNumber = GrouperUtil.intValue(matcher.group(1));
    int thisMinorNumber = GrouperUtil.intValue(matcher.group(2));
    int thisBuildNumber = GrouperUtil.intValue(matcher.group(3));
    
    if (other == null) {
      throw new NullPointerException("other is null");
    }
    matcher = versionPattern.matcher(other.name());
    if (!matcher.matches()) {
      throw new RuntimeException("Cant match other string: " + other.name());
    }
    int otherMajorNumber = GrouperUtil.intValue(matcher.group(1));
    int otherMinorNumber = GrouperUtil.intValue(matcher.group(2));
    int otherBuildNumber = GrouperUtil.intValue(matcher.group(3));
    
    if (thisMajorNumber < otherMajorNumber) {
      return true;
    }
    if (thisMajorNumber > otherMajorNumber) {
      return false;
    }
    if (thisMinorNumber < otherMinorNumber) {
      return true;
    }
    if (thisMinorNumber > otherMinorNumber) {
      return false;
    }
    if (thisBuildNumber < otherBuildNumber) {
      return true;
    }
    if (thisBuildNumber > otherBuildNumber) {
      return false;
    }
    throw new RuntimeException("Should not get here! " + this + ", " + other);
  }

  /**
   * result code changed in 1.4 to include a response for if the membership already existed
   * @param didntAlreadyExist
   * @return the code success or if it already existed
   */
  public WsAddMemberResultCode addMemberSuccessResultCode(boolean didntAlreadyExist) {
    
    //before 1.4, all we had was success
    if (this.lessThanArg(v1_4_000)) {
      return WsAddMemberResultCode.SUCCESS;
    }
    //now we have two codes
    return didntAlreadyExist ? WsAddMemberResultCode.SUCCESS : WsAddMemberResultCode.SUCCESS_ALREADY_EXISTED;
  }

  /** current version */
  private static GrouperWsVersion currentVersion = null;

  /** ordered version number to know which is more recent etc */
  @SuppressWarnings("unused")
  private int revision;

  /** 
   * the actual string of the version, not the "name" of the enum
   * typcially will be whatever grouper is, then a build number for
   * web services
   */
  private boolean currentVersionBoolean;

  /**
   * constructor
   * @param theCurrentVersion
   * @param theRevision 
   */
  private GrouperWsVersion(boolean theCurrentVersion, int theRevision) {

    this.currentVersionBoolean = theCurrentVersion;
    this.revision = theRevision;

  }

  /** current client version */
  public static ThreadLocal<GrouperWsVersion> currentClientVersion = new ThreadLocal<GrouperWsVersion>();

  /**
   * put the current client version
   * @param clientVersion
   */
  public static void assignCurrentClientVersion(GrouperWsVersion clientVersion) {
    currentClientVersion.set(clientVersion);
  }
  
  /**
   * put the current client version
   * @param clientVersion
   * @param soapOnly true if only doing this for rest
   */
  public static void assignCurrentClientVersion(GrouperWsVersion clientVersion, boolean soapOnly) {
    if (soapOnly) {
      if (!GrouperRestServlet.isRestRequest()) {
        assignCurrentClientVersion(clientVersion);
      }
    } else {
      assignCurrentClientVersion(clientVersion);
    }
  }
  
  /**
   * return current client version or null
   * @return the current client version or null
   */
  public static GrouperWsVersion retrieveCurrentClientVersion() {
    return currentClientVersion.get();
  }
  
  /**
   * get the current version
   * @return the current version
   */
  public static GrouperWsVersion currentVersion() {

    //lazyload the current version
    if (currentVersion == null) {
      //find current version
      for (GrouperWsVersion grouperServiceVersion : GrouperWsVersion.values()) {
        //we found it
        if (grouperServiceVersion.currentVersionBoolean) {
          //make sure not more than one
          if (currentVersion != null) {
            GrouperUtil.assertion(!grouperServiceVersion.currentVersionBoolean,
                "Cant have more than one current version");
          }
          currentVersion = grouperServiceVersion;
        }
      }
    }
    //there must be one and only one
    GrouperUtil.assertion(currentVersion != null, "There is no current version!");
    return currentVersion;
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exception on null will not allow null or blank entries
   * @param exceptionOnNull
   * @return the enum or null or exception if not found
   */
  public static GrouperWsVersion valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsVersion.class, 
        string, exceptionOnNull);

  }

}
