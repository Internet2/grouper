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
 * @author mchyzer
 * $Id: GrouperVersion.java,v 1.9 2009-11-18 17:03:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouperInstaller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;



/**
 * keep track of which version grouper is.  Update this file (the GROUPER_VERSION constant) before each
 * non-release-candidate release
 */
public class GiGrouperVersion {
  
  /**
   * return the parsed and tostring version of this version string (consistent), 
   * or null if nothing passed in
   * @param versionString
   * @return the version string
   */
  public static String stringValueOrNull(String versionString) {
    if (GrouperInstallerUtils.isBlank(versionString)) {
      return null;
    }
    return valueOfIgnoreCase(versionString, true).toString();
  }
  
  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GiGrouperVersion)) {
      return false;
    }
    GiGrouperVersion other = (GiGrouperVersion)obj;
    if (this.build != other.build) {
      return false;
    }
    if (this.major != other.major) {
      return false;
    }
    if (this.minor != other.minor) {
      return false;
    }
    if (!GrouperInstallerUtils.equals(this.rc, other.rc)) {
      return false;
    }
    return true;
  }

  
  
  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    
    return ("" + this.major + "" + this.minor + "" + this.build + "" + this.rc).hashCode();
    
  }


  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String result = this.major + "." + this.minor + "." + this.build;
    if (this.rc != null) {
      result+= "rc" + this.rc;
    }
    return result;
  }
  
  /** 
   * current version
   * this must be three integers separated by dots for major version, minor version, and build number.
   * update this before each
   * non-release-candidate release (e.g. in preparation for it)
   * e.g. 1.5.0
   * DEV NOTE: this cant be read from version file since in dev there is no grouper jar so I dont know the version
   */
  public static final String GROUPER_VERSION = "2.4.0";
  
  /**
   * se we dont have to keep constructing this
   */
  private static GiGrouperVersion currentVersion = null;
  
  /**
   * current grouper version
   * @return current grouper version
   */
  public static GiGrouperVersion currentVersion() {
    if (currentVersion == null) {
      currentVersion = valueOfIgnoreCase(GROUPER_VERSION);
    }
    return currentVersion;
  }
  
  /** major number */
  private int major;
  
  /** minor number */
  private int minor;

  /** build number */
  private int build;
  
  /** rc number (release cnadidate) */
  private Integer rc;

  /** keep a map of max size 1000 */
  private static Map<String, GiGrouperVersion> versionCache = new HashMap<String, GiGrouperVersion>();

  /**
   * convert string to version like an enum would
   * 
   * @param string cannot be blank
   * @return the enum or null or exception if not found
   */
  public static GiGrouperVersion valueOfIgnoreCase(String string) {
    return valueOfIgnoreCase(string, true);
  }

  /**
   * convert string to version like an enum would
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GiGrouperVersion valueOfIgnoreCase(String string, boolean exceptionOnNull) {

    if (GrouperInstallerUtils.isBlank(string)) {
      if (exceptionOnNull) {
        throw new RuntimeException("Not expecting a blank string for version");
      }
      return null;
    }
    
    //see if in cache
    GiGrouperVersion grouperVersion = versionCache.get(string);
    if (grouperVersion != null) {
      return grouperVersion;
    }
    
    grouperVersion = new GiGrouperVersion(string);
    
    //dont cause a memory leak
    if (versionCache.size() < 1000) {
      versionCache.put(string, grouperVersion);
    }
    return grouperVersion;
  }
  
  /**
   * private constructor
   * @param versionString
   */
  public GiGrouperVersion(String versionString) {
    Matcher grouperMatcher = shortPattern.matcher(versionString);
    
    // 1.2
    if (!grouperMatcher.matches()) {
    
      grouperMatcher = pattern.matcher(versionString);
      
      // 1.2.3
      if (!grouperMatcher.matches()) {
        

        grouperMatcher = rcPattern.matcher(versionString);
        
        // 1.2.3rc4
        if (!grouperMatcher.matches()) {
        
        
          throw new RuntimeException("Invalid version: " + versionString
              + ", expecting something like: 1.1 or 1.2.3 or 1.2.3rc4");
        }
        this.rc = GrouperInstallerUtils.intValue(grouperMatcher.group(4));
      }
      this.build = GrouperInstallerUtils.intValue(grouperMatcher.group(3));

    }
    //get the grouper versions
    this.major = GrouperInstallerUtils.intValue(grouperMatcher.group(1));
    this.minor = GrouperInstallerUtils.intValue(grouperMatcher.group(2));

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
   * see if the grouper version is greater than or equal to a certain version
   * @param version
   * @return true if the grouper version is greater than or equal to a certain version
   */
  public boolean greaterOrEqualToArg(String version) {
    return this.thisGreaterThanArgument(new GiGrouperVersion(version), true);
  }
  
  /**
   * see if this version is less than the argument one
   * @param other
   * @param orEqual 
   * @return true if less than, false if equal or greater
   */
  public boolean lessThanArg(GiGrouperVersion other, boolean orEqual) {
    //switch these around.  if a < b, then b > a
    return other.thisGreaterThanArgument(this, orEqual);
  }

  /**
   * see if this version is less than the argument one, only considering major and minor version
   * @param other
   * @param orEqual 
   * @return true if less than, false if equal or greater
   */
  public boolean lessThanMajorMinorArg(GiGrouperVersion other, boolean orEqual) {
    
    GiGrouperVersion thisMajorMinor = valueOfIgnoreCase(this.major + "." + this.minor + ".0");
    GiGrouperVersion otherMajorMinor = valueOfIgnoreCase(other.major + "." + other.minor + ".0");
    
    return thisMajorMinor.lessThanArg(otherMajorMinor, orEqual);
    
  }

  /**
   * see if this version is less than the argument one
   * @param other
   * @return true if less than, false if equal or greater
   */
  public boolean lessThanArg(GiGrouperVersion other) {
    return this.lessThanArg(other, false);
  }
  
  /**
   * see if the grouper version is greater than or equal to a certain version
   * @param version
   * @return true if the grouper version is greater than or equal to a certain version
   */
  public boolean greaterOrEqualToArg(GiGrouperVersion version) {
    return this.thisGreaterThanArgument(version, true);
  }
  
  /**
   * <pre>
   * start of string, optional v or V, first digit, period or underscore, second digit, period or underscore, third digit, end of string
   * parens are for capturing
   * ^(\\d+)\\.(\\d+)\\.(\\d+)$
   * </pre>
   */
  private static Pattern pattern = Pattern.compile("^[vV]?(\\d+)[\\._](\\d+)[\\._](\\d+)$");
  
  /**
   * <pre>
   * start of string, optional v or V, first digit, period or underscore, second digit, period or underscore, third digit, end of string
   * parens are for capturing
   * ^(\\d+)\\.(\\d+)$
   * </pre>
   */
  private static Pattern shortPattern = Pattern.compile("^[vV]?(\\d+)[\\._](\\d+)$");
  
  /**
   * <pre>
   * start of string, optional v or V, first digit, period or underscope, second digit, period or underscore, third digit, 
   * period, rc, 4th digit, end of string
   * parens are for capturing
   * ^(\\d+)\\.(\\d+)\\.(\\d+)$
   * </pre>
   */
  private static Pattern rcPattern = Pattern.compile("^[vV]?(\\d+)[\\._](\\d+)[\\._](\\d+)\\-?rc(\\d+)$");
  
  /**
   * helper method for unit testing
   * @param grouperVersion
   * @param anotherVersion
   * @return true if grouper is greater
   */
  public static boolean _grouperVersionGreaterOrEqualHelper(String grouperVersion, String anotherVersion) {
    GiGrouperVersion grouper = new GiGrouperVersion(grouperVersion);
    GiGrouperVersion another = new GiGrouperVersion(anotherVersion);
    //for grouper version to be greater or equal
    return grouper.thisGreaterThanArgument(another, true);
  }

  /**
   * see if this version is greater than or equal the argument
   * @param anotherVersion
   * @param orEqual if true then count equals as a true
   * @return true if this is less
   */
  private boolean thisGreaterThanArgument(GiGrouperVersion anotherVersion, boolean orEqual) {
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
    if (GrouperInstallerUtils.equals(this.rc, anotherVersion.rc)) {
      return orEqual;
    }
    //not having a release candidate version is higher than having one
    if (this.rc == null) {
      return true;
    }
    if (anotherVersion.rc == null) {
      return false;
    }
    return this.rc > anotherVersion.rc;
  }
}
