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
 * keep track of which version grouper is.  Update this file (the GROUPER_VERSION constant) before each
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
  public static final String GROUPER_VERSION = "2.3.0";
  
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
  private static Pattern pattern = Pattern.compile("^[vV]?(\\d+)[\\._](\\d+)[\\._](\\d+)$");
  
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

  /**
   * get the patch index file if can find or null if not
   * @param errorIfProblem if should throw error if problem
   * @return the file
   */
  public static File grouperPatchStatusFile(boolean errorIfProblem) {
    File log4jFile = GrouperUtil.fileFromResourceName("log4j.properties");
    if (log4jFile.exists()) {
      File confDir = log4jFile.getParentFile();
      if (confDir.exists()) {
        File grouperDir = confDir.getParentFile();
        if (grouperDir.exists()) {
          File patchIndexFile = new File(GrouperUtil.fileCanonicalPath(grouperDir) + File.separator + "grouperPatchStatus.properties");
          return patchIndexFile;
        } else {
          if (errorIfProblem) {
            throw new RuntimeException("grouper dir doesnt exist");
          }
        }
      } else {
        if (errorIfProblem) {
          throw new RuntimeException("conf dir doesnt exist");
        }
      }
    } else {
      if (errorIfProblem) {
        throw new RuntimeException("log4j.properties doesnt exist");
      }
    }
    if (errorIfProblem) {
      throw new RuntimeException("Problem finding grouperPatchStatus.properties");
    }
    return null;
  }
  
  /**
   * map of patch type (e.g. api, ui), and numbers of patches installed in order
   * @return the map of patches installed, never returns null
   */
  public static Map<String, Set<Integer>> patchesInstalled() {
    Map<String, Set<Integer>> patchesInstalled = patchesInstalledCache.get(Boolean.TRUE);
    
    if (patchesInstalled == null) {
      synchronized (patchesInstalledCache) {
        
        patchesInstalled = patchesInstalledCache.get(Boolean.TRUE);
        if (patchesInstalled == null) {
          
          patchesInstalled = new TreeMap<String, Set<Integer>>();
          
          try {

            File patchIndexFile = grouperPatchStatusFile(true);
            if (patchIndexFile.exists()) {
              Properties props = GrouperUtil.propertiesFromFile(patchIndexFile, false);
              
              Pattern patchStatePattern = Pattern.compile("^grouper_v" + GROUPER_VERSION.replace(".", "_") + "_(.*)_patch_(.*)\\.state$");
              for (String key : (Set<String>)(Object)props.keySet()) {
                Matcher matcher = patchStatePattern.matcher(key);

                if (matcher.matches() && "applied".equals(props.getProperty(key))) {
                  String engine = matcher.group(1);
                  String patchNumberString = matcher.group(2);
                  Set<Integer> patchNumbers = patchesInstalled.get(engine);
                  if (patchNumbers == null) {
                    patchNumbers = new TreeSet<Integer>();
                    patchesInstalled.put(engine, patchNumbers);
                  }
                  
                  try {
                    int patchNumberInt = GrouperUtil.intValue(patchNumberString); 
                    patchNumbers.add(patchNumberInt);
                  } catch (Exception e) {
                    String logMessage = "Error: cant parse version number from grouperPatchStatus.properties entry: " + key + " in " + patchIndexFile.getAbsolutePath();
                    try {
                      LOG.error(logMessage);
                    } catch (Exception e2) {
                      System.out.println(logMessage);
                    }
                    //swallow this since could be in startup
                  }
                }
              }
            }
          } catch (Exception e) {
            String logMessage = "Error: cant process grouperPatchStatus.properties";
            try {
              LOG.error(logMessage, e);
            } catch (Exception e2) {
              System.out.println(logMessage);
              e.printStackTrace();
            }
            //swallow this since could be in startup
          }
          patchesInstalledCache.put(Boolean.TRUE, patchesInstalled);
        }
      }
    }
    return patchesInstalled;
  }
  

}
