package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;


public class GrouperDigitalMarketplaceUserDoesNotExist extends RuntimeException {

  public GrouperDigitalMarketplaceUserDoesNotExist() {
  }

  public GrouperDigitalMarketplaceUserDoesNotExist(String message) {
    super(message);
  }

  public GrouperDigitalMarketplaceUserDoesNotExist(Throwable cause) {
    super(cause);
  }

  public GrouperDigitalMarketplaceUserDoesNotExist(String message, Throwable cause) {
    super(message, cause);
  }

  public GrouperDigitalMarketplaceUserDoesNotExist(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
