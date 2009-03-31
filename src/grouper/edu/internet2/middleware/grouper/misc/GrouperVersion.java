/*
 * @author mchyzer
 * $Id: GrouperVersion.java,v 1.7 2009-03-31 06:58:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * keep track of which version grouper is.  Update this file (the GROUPER_VERSION constant) before each
 * non-release-candidate release
 */
public class GrouperVersion {
  
  /** 
   * current version
   * this must be three integers separated by dots for major version, minor version, and build number.
   * update this before each
   * non-release-candidate release (e.g. in preparation for it)
   * e.g. 1.5.0-rc1
   */
  public static final String GROUPER_VERSION = "1.5.0-rc1";
  
  /** major number */
  private int major;
  
  /** minor number */
  private int minor;

  /** build number */
  private int build;
  
  /** rc number (release cnadidate) */
  private Integer rc;

  /**
   * private constructor
   * @param versionString
   */
  private GrouperVersion(String versionString) {
    Matcher grouperMatcher = pattern.matcher(versionString);
    if (!grouperMatcher.matches()) {
      grouperMatcher = rcPattern.matcher(versionString);
      if (!grouperMatcher.matches()) {
        
        throw new RuntimeException("Invalid grouper version: " + versionString
            + ", expecting something like: 1.2.3 or 1.2.3rc4");
      }
      this.rc = GrouperUtil.intValue(grouperMatcher.group(4));
    }
    //get the grouper versions
    this.major = GrouperUtil.intValue(grouperMatcher.group(1));
    this.minor = GrouperUtil.intValue(grouperMatcher.group(2));
    this.build = GrouperUtil.intValue(grouperMatcher.group(3));

  }
  
  /**
   * see if the grouper version is greater than or equal to a certain version
   * @param version
   * @return true if the grouper version is greater than or equal to a certain version
   */
  public static boolean grouperVersionGreaterOrEqual(String version) {
    return _grouperVersionGreaterOrEqualHelper(GROUPER_VERSION, version);
  }
  
  /**
   * <pre>
   * start of string, first digit, period, second digit, period, third digit, end of string
   * parens are for capturing
   * ^(\\d+)\\.(\\d+)\\.(\\d+)$
   * </pre>
   */
  private static Pattern pattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");
  
  /**
   * <pre>
   * start of string, first digit, period, second digit, period, third digit, 
   * period, rc, 4th digit, end of string
   * parens are for capturing
   * ^(\\d+)\\.(\\d+)\\.(\\d+)$
   * </pre>
   */
  private static Pattern rcPattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)\\-?rc(\\d+)$");
  
  /**
   * helper method for unit testing
   * @param grouperVersion
   * @param anotherVersion
   * @return true if grouper is greater
   */
  public static boolean _grouperVersionGreaterOrEqualHelper(String grouperVersion, String anotherVersion) {
    GrouperVersion grouper = new GrouperVersion(grouperVersion);
    GrouperVersion another = new GrouperVersion(anotherVersion);
    //for grouper version to be greater or equal
    return grouper.thisGreaterThanEqualArgument(another);
  }

  /**
   * see if this version is greater than or equal the argument
   * @param anotherVersion
   * @return true if this is less
   */
  private boolean thisGreaterThanEqualArgument(GrouperVersion anotherVersion) {
    //compare, first with major, minor, then build
    if (this.major > anotherVersion.major) {
      return true;
    } else if (this.major < anotherVersion.major) {
      return false;
    }
    if (this.minor > anotherVersion.minor) {
      return true;
    } else if (this.minor < anotherVersion.minor) {
      return false;
    }
    //note its greater or equal
    if (this.build > anotherVersion.build) {
      return true;
    } else if (this.build < anotherVersion.build) {
      return false;
    }
    if (this.rc == anotherVersion.rc) {
      return true;
    }
    //not having a release candidate version is higher than having one
    if (this.rc == null) {
      return true;
    }
    if (anotherVersion.rc == null) {
      return false;
    }
    return this.rc >= anotherVersion.rc;
  }
}
