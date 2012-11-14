package edu.internet2.middleware.authzStandardApiClient.testSuite;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;

/**
 * result bean for test suite
 * @author mchyzer
 *
 */
public class AsacTestSuiteResults {
  
  /** verbose level, low, medium, high */
  private AsacTestSuiteVerbose verbose = AsacTestSuiteVerbose.medium;


  
  /**
   * verbose level, low, medium, high
   * @return the verbose
   */
  public AsacTestSuiteVerbose getVerbose() {
    return this.verbose;
  }

  
  /**
   * verbose level, low, medium, high
   * @param verbose1 the verbose to set
   */
  public void setVerbose(AsacTestSuiteVerbose verbose1) {
    this.verbose = verbose1;
  }

  /**
   * run all tests
   */
  public void runAllTestSuites() {
    
    //add all tests
    this.asacTestSuiteResultList.add(new AsacTestSuiteDefaultResource(this));
    
    
    //run them
    for (AsacTestSuiteResult asacTestSuiteResult : this.asacTestSuiteResultList) {
      asacTestSuiteResult.runTests();
    }
    
  }
  
  /**
   * list of tests
   */
  private List<AsacTestSuiteResult> asacTestSuiteResultList = new ArrayList<AsacTestSuiteResult>();
  
  /**
   * @return the failureCount
   */
  public int getFailureCount() {
    
    int failureCount = 0;
    
    for (AsacTestSuiteResult asacTestSuiteResult : StandardApiClientUtils.nonNull(this.asacTestSuiteResultList)) {
      failureCount += asacTestSuiteResult.getFailureCount();
    }
    
    return failureCount;
  }

  /**
   * @return the report
   */
  public String getReport() {
    StringBuilder result = new StringBuilder();
    
    for (AsacTestSuiteResult asacTestSuiteResult : StandardApiClientUtils.nonNull(this.asacTestSuiteResultList)) {
      String report = asacTestSuiteResult.getReport();
      if (!StandardApiClientUtils.isBlank(report)) {
        if (result.length() > 0) {
          result.append("\n");
        }
        result.append(report);
      }
    }
    
    return result.toString();
  }

  /**
   * @return the successCount
   */
  public int getSuccessCount() {
    int successCount = 0;
    
    for (AsacTestSuiteResult asacTestSuiteResult : StandardApiClientUtils.nonNull(this.asacTestSuiteResultList)) {
      successCount += asacTestSuiteResult.getSuccessCount();
    }
    
    return successCount;
  }

  /**
   * count of how many testCount
   * @return the testCount
   */
  public int getTestCount() {
    int testCount = 0;
    
    for (AsacTestSuiteResult asacTestSuiteResult : StandardApiClientUtils.nonNull(this.asacTestSuiteResultList)) {
      testCount += asacTestSuiteResult.getTestCount();
    }
    
    return testCount;
  }

  /**
   * if the tests run successfully
   * @return the success
   */
  public boolean isSuccess() {
    //if there are no failures
    return this.getFailureCount() == 0;
  }
  
  /**
   * @return the warningCount
   */
  public int getWarningCount() {
    int warningCount = 0;
    
    for (AsacTestSuiteResult asacTestSuiteResult : StandardApiClientUtils.nonNull(this.asacTestSuiteResultList)) {
      warningCount += asacTestSuiteResult.getWarningCount();
    }
    
    return warningCount;
  }
  
  
  
  
}
