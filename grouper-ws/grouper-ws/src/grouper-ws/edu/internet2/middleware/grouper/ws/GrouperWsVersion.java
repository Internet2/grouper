/*
 * @author mchyzer $Id: GrouperWsVersion.java,v 1.5 2008-10-21 05:27:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import edu.internet2.middleware.grouper.util.GrouperUtil;
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

  /**
   * get the current version
   * @return the current version
   */
  public static GrouperWsVersion currentVersion() {

    //lazyload the current version
    if (currentVersion == null) {
      //find current version
      for (GrouperWsVersion grouperServiceVersion : GrouperWsVersion.values()) {
        //make sure not more than one
        if (currentVersion != null) {
          GrouperUtil.assertion(!grouperServiceVersion.currentVersionBoolean,
              "Cant have more than one current version");
        }
        //we found it
        if (grouperServiceVersion.currentVersionBoolean) {
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
