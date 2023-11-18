/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.file;


/**
 *
 */
public interface GrouperSftpCallback {

  /**
   * This method will be called when connection to an sftp server.
   * Return null if there is no info to return
   * @param grouperSftpSession 
   * @return the return value to be passed to return value of callback method
   */
  public Object callback(GrouperSftpSession grouperSftpSession);

}
