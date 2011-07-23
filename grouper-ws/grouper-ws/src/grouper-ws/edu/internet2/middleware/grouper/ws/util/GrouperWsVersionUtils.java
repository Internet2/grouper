package edu.internet2.middleware.grouper.ws.util;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAddMemberResult.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup.SubjectFindResult;

/**
 * WS grouper version utils
 * @author mchyzer
 *
 */
public class GrouperWsVersionUtils {
  
  /**
   * result code changed in 1.4 to include a response for if the membership already existed
   * @param didntAlreadyExist
   * @param subjectFindResult 
   * @return the code success or if it already existed
   */
  public static WsAddMemberResultCode addMemberSuccessResultCode(boolean didntAlreadyExist, WsSubjectLookup.SubjectFindResult subjectFindResult) {
    
    if (retrieveCurrentClientVersion().greaterOrEqualToArg(GrouperVersion.valueOfIgnoreCase("v1_4_000"))) {

      if (subjectFindResult == SubjectFindResult.SUCCESS_CREATED) {
        return WsAddMemberResultCode.SUCCESS_CREATED;
      }
      
      //now we have two codes
      return didntAlreadyExist ? WsAddMemberResultCode.SUCCESS : WsAddMemberResultCode.SUCCESS_ALREADY_EXISTED;
    }
    //before 1.4, all we had was success
    return WsAddMemberResultCode.SUCCESS;
  }


  /** current client version */
  public static ThreadLocal<GrouperVersion> currentClientVersion = new ThreadLocal<GrouperVersion>();

  /**
   * put the current client version
   * @param clientVersion
   * @param warnings 
   */
  public static void assignCurrentClientVersion(GrouperVersion clientVersion, StringBuilder warnings) {
    currentClientVersion.set(clientVersion);
    if (GrouperVersion.currentVersion().lessThanMajorMinorArg(clientVersion, false)) {
      String warning = "Client version: " + clientVersion + " is less than (major/minor) server version: " + GrouperVersion.currentVersion();
      if (warnings.indexOf(warning) == -1) {
        if (warning.length() > 0) {
          warnings.append(", ");
        }
        warnings.append(warning);
      }
    }
  }
  
  /**
   * put the current client version
   */
  public static void removeCurrentClientVersion() {
    currentClientVersion.remove();
  }

  /**
   * put the current client version
   * @param soapOnly true if only doing this for rest
   */
  public static void removeCurrentClientVersion(boolean soapOnly) {
    if (soapOnly) {
      if (!GrouperRestServlet.isRestRequest()) {
        currentClientVersion.remove();
      }
    } else {
      currentClientVersion.remove();
    }
  }
  
  /**
   * return current client version or null
   * @return the current client version or null
   */
  public static GrouperVersion retrieveCurrentClientVersion() {
    return currentClientVersion.get();
  }

}
