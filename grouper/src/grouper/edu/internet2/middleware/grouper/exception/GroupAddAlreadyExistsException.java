package edu.internet2.middleware.grouper.exception;

/**
 * If there is a problem with adding a group where@SuppressWarnings("serial")
 a group exists by same name
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GroupAddAlreadyExistsException extends GroupAddException {

  public GroupAddAlreadyExistsException() {
    
  }

  public GroupAddAlreadyExistsException(String msg) {
    super(msg);
  }

  public GroupAddAlreadyExistsException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public GroupAddAlreadyExistsException(Throwable cause) {
    super(cause);
  }

}
