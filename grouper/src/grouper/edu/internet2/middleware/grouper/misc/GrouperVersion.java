/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: GrouperVersion.java,v 1.9 2009-11-18 17:03:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;


/**
 * keep track of which version grouper is.  Update this file (the GrouperVersion.grouperVersion() constant) before each
 * non-release-candidate release
 */
public class GrouperVersion {
  
  /**
   * return the parsed and tostring version of this version string (consistent), 
   * or null if nothing passed in
   * @param versionString
   * @return the version string
   */
  public static String stringValueOrNull(String versionString) {
    if (StringUtils.isBlank(versionString)) {
      return null;
    }
    return valueOfIgnoreCase(versionString, true).toString();
  }
  
  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GrouperVersion)) {
      return false;
    }
    GrouperVersion other = (GrouperVersion)obj;
    if (this.build != other.build) {
      return false;
    }
    if (this.major != other.major) {
      return false;
    }
    if (this.minor != other.minor) {
      return false;
    }
    if (!GrouperUtil.equals(this.rc, other.rc)) {
      return false;
    }
    return true;
  }

  
  
  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.build).append(this.major).append(this.minor).append(this.rc).hashCode();
  }



  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String result = this.major + "." + this.minor + "." + this.build;
    if (this.rcString != null) {
      result+= this.rcString;
    }
    return result;
  }
  
  private static String grouperVersionString = null;
  
  /**
   * get the version from jar e.g. 2.5.12
   * @return the version
   */
  public static String grouperVersion() {
    if (grouperVersionString == null) {

      try {
        grouperVersionString = GrouperCheckConfig.jarVersion(GrouperVersion.class);
      } catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Can't find version of grouper jar, using 2.5.0", e);
        } else {
          LOG.warn("Can't find version of grouper jar, using 2.5.0");
        }
      }
      if (grouperVersionString == null) {
        grouperVersionString = "2.5.0";
      }
    }
    return grouperVersionString;
  }
  
  /**
   * se we dont have to keep constructing this
   */
  private static GrouperVersion currentVersion = null;
  
  /**
   * current grouper version
   * @return current grouper version
   */
  public static GrouperVersion currentVersion() {
    if (currentVersion == null) {
      currentVersion = valueOfIgnoreCase(grouperVersion());
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

  /** rc component full string including "rc" */
  private String  rcString;

  /** keep a map of max size 1000 */
  private static Map<String, GrouperVersion> versionCache = new HashMap<String, GrouperVersion>();

  /**
   * convert string to version like an enum would
   * 
   * @param string cannot be blank
   * @return the enum or null or exception if not found
   */
  public static GrouperVersion valueOfIgnoreCase(String string) {
    return valueOfIgnoreCase(string, true);
  }

  /**
   * convert string to version like an enum would
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperVersion valueOfIgnoreCase(String string, boolean exceptionOnNull) {

    if (StringUtils.isBlank(string)) {
      if (exceptionOnNull) {
        throw new RuntimeException("Not expecting a blank string for version");
      }
      return null;
    }
    
    //see if in cache
    GrouperVersion grouperVersion = versionCache.get(string);
    if (grouperVersion != null) {
      return grouperVersion;
    }
    
    grouperVersion = new GrouperVersion(string);
    
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
  public GrouperVersion(String versionString) {
    if (versionString != null) {
      Matcher grouperMatcher = pattern.matcher(versionString);
      if (!grouperMatcher.matches()) {
        throw new RuntimeException("Invalid grouper version: " + versionString
            + ", expecting something like: 1.2.3, 1.2.3rc4, or 1.2.3-SNAPSHOT");
      }

      //get the grouper versions
      this.major = GrouperUtil.intValue(grouperMatcher.group(1));
      this.minor = GrouperUtil.intValue(grouperMatcher.group(2));
      this.build = GrouperUtil.intValue(grouperMatcher.group(3));

      this.rcString = grouperMatcher.group(4);
      if ("-SNAPSHOT".equals(grouperMatcher.group(4))) {
        // snapshot will always be less than any rc version
        this.rc = -1;
      } else if (grouperMatcher.group(5) != null) {
        this.rc = GrouperUtil.intValue(grouperMatcher.group(5));
      }
    }

  }
  
  /**
   * see if the grouper version is greater than or equal to a certain version
   * @param version
   * @return true if the grouper version is greater than or equal to a certain version
   */
  public static boolean grouperVersionGreaterOrEqual(String version) {
    return _grouperVersionGreaterOrEqualHelper(GrouperVersion.grouperVersion(), version);
  }
  
  /**
   * see if the grouper version is greater than or equal to a certain version
   * @param version
   * @return true if the grouper version is greater than or equal to a certain version
   */
  public boolean greaterOrEqualToArg(String version) {
    return this.thisGreaterThanArgument(new GrouperVersion(version), true);
  }
  
  /**
   * see if this version is less than the argument one
   * @param other
   * @param orEqual 
   * @return true if less than, false if equal or greater
   */
  public boolean lessThanArg(GrouperVersion other, boolean orEqual) {
    //switch these around.  if a < b, then b > a
    return other.thisGreaterThanArgument(this, orEqual);
  }

  /**
   * see if this version is less than the argument one, only considering major and minor version
   * @param other
   * @param orEqual 
   * @return true if less than, false if equal or greater
   */
  public boolean lessThanMajorMinorArg(GrouperVersion other, boolean orEqual) {
    
    GrouperVersion thisMajorMinor = valueOfIgnoreCase(this.major + "." + this.minor + ".0");
    GrouperVersion otherMajorMinor = valueOfIgnoreCase(other.major + "." + other.minor + ".0");
    
    return thisMajorMinor.lessThanArg(otherMajorMinor, orEqual);
    
  }

  /**
   * see if this version is same argument one, only considering major and minor version
   * @param other
   * @param orEqual 
   * @return true if less than, false if equal or greater
   */
  public boolean sameMajorMinorArg(GrouperVersion other) {
    
    return this.major == other.major && this.minor == other.minor;
    
  }

  /**
   * see if this version is less than the argument one
   * @param other
   * @return true if less than, false if equal or greater
   */
  public boolean lessThanArg(GrouperVersion other) {
    return this.lessThanArg(other, false);
  }
  
  /**
   * see if the grouper version is greater than or equal to a certain version
   * @param version
   * @return true if the grouper version is greater than or equal to a certain version
   */
  public boolean greaterOrEqualToArg(GrouperVersion version) {
    return this.thisGreaterThanArgument(version, true);
  }

  /**
   * <pre>
   * start of string, optional v or V, first digit, period or underscore, second digit, period or underscore, third digit, end of string
   * parens are for capturing
   * ^(\\d+)\\.(\\d+)\\.(\\d+)$
   * </pre>
   */
  private static Pattern pattern = Pattern.compile("^[vV]?(\\d+)[\\._](\\d+)[\\._](\\d+)(-?rc(\\d+)|-SNAPSHOT)?$");

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
    return grouper.thisGreaterThanArgument(another, true);
  }

  /**
   * see if this version is greater than or equal the argument
   * @param anotherVersion
   * @param orEqual if true then count equals as a true
   * @return true if this is less
   */
  private boolean thisGreaterThanArgument(GrouperVersion anotherVersion, boolean orEqual) {
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
    if (GrouperUtil.equals(this.rc, anotherVersion.rc)) {
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
  
  /**
   * cache this so we dont have to lookup patches all the time
   */
  private static ExpirableCache<Boolean, Map<String, Set<Integer>>> patchesInstalledCache = new ExpirableCache<Boolean, Map<String, Set<Integer>>>(10);
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperVersion.class);


}
