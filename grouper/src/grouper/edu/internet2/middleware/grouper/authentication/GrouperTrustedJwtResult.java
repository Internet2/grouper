package edu.internet2.middleware.grouper.authentication;


public enum GrouperTrustedJwtResult {

  /**
   * if the token isnt even there
   */
  ERROR_MISSING_TOKEN,
  
  /**
   * if the JWT is trusted and subject found
   */
  SUCCESS_SUBJECT_FOUND, 
  
  /**
   * invalid token
   */
  ERROR_TOKEN_INVALID;
  
  
  
}
