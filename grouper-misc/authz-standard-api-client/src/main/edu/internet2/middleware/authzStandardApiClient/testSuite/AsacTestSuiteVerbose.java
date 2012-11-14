package edu.internet2.middleware.authzStandardApiClient.testSuite;

import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;

/**
 * how verbose do we want our tests
 * @author mchyzer
 *
 */
public enum AsacTestSuiteVerbose {
  
  /**
   * just give errors and warnings
   */
  low {

    /**
     * @see AsacTestSuiteVerbose#atLeastVerbose(AsacTestSuiteVerbose)
     */
    @Override
    public boolean atLeastVerbose(AsacTestSuiteVerbose asacTestSuiteVerbose) {
      switch(asacTestSuiteVerbose) {
        case low:
          return true;
        case medium:
        case high:
          return false;
      }
      throw new RuntimeException("Note expecting verbose level: " + asacTestSuiteVerbose);
    }
  },
  
  /**
   * give the high level progress
   */
  medium {

    /**
     * @see AsacTestSuiteVerbose#atLeastVerbose(AsacTestSuiteVerbose)
     */
    @Override
    public boolean atLeastVerbose(AsacTestSuiteVerbose asacTestSuiteVerbose) {
      switch(asacTestSuiteVerbose) {
        case low:
        case medium:
          return true;
        case high:
          return false;
      }
      throw new RuntimeException("Note expecting verbose level: " + asacTestSuiteVerbose);
    }
  },
  
  /**
   * give a line for each test
   */
  high {

    /**
     * @see AsacTestSuiteVerbose#atLeastVerbose(AsacTestSuiteVerbose)
     */
    @Override
    public boolean atLeastVerbose(AsacTestSuiteVerbose asacTestSuiteVerbose) {
      switch(asacTestSuiteVerbose) {
        case low:
        case medium:
        case high:
          return true;
      }
      throw new RuntimeException("Note expecting verbose level: " + asacTestSuiteVerbose);
    }
  };
  
  /**
   * see if the current level is at least the level passed in
   * @param asacTestSuiteVerbose
   * @return true if at least this verbose
   */
  public abstract boolean atLeastVerbose(AsacTestSuiteVerbose asacTestSuiteVerbose);

  /**
   * convert a string to this enum, ignore case
   * @param input
   * @param exceptionIfNull
   * @return the enum
   */
  public static AsacTestSuiteVerbose valueOfIgnoreCase(String input, boolean exceptionIfNull) {
    return StandardApiClientUtils.enumValueOfIgnoreCase(AsacTestSuiteVerbose.class, input, exceptionIfNull);
  }
  
}
