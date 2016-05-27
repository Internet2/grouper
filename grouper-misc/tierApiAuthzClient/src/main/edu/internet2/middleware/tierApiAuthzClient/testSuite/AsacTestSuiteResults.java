package edu.internet2.middleware.tierApiAuthzClient.testSuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientUtils;

/**
 * result bean for test suite
 * @author mchyzer
 *
 */
public class AsacTestSuiteResults {

  /**
   * number of spaces to right pad in report
   */
  public static final int RIGHT_PAD_SPACES = 43;
  
  /**
   * list of tests
   */
  @SuppressWarnings("unchecked")
  private static final List<Class<? extends AsacTestSuiteResult>> ALL_TEST_SUITES = Collections.unmodifiableList(StandardApiClientUtils.toList(
    AsacTestSuiteDefaultResource.class,
    AsacTestSuiteDefaultVersionResource.class,
    AsacTestSuiteVersionResource.class,
    AsacTestSuiteFolderSave.class));

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
    
    for (Class<? extends AsacTestSuiteResult> theClass: ALL_TEST_SUITES) {
      AsacTestSuiteResult asacTestSuiteResult = StandardApiClientUtils.construct(theClass, 
          new Class<?>[]{AsacTestSuiteResults.class}, new Object[]{this});
      this.asacTestSuiteResultList.add(asacTestSuiteResult);
    }
    
    //run them
    for (AsacTestSuiteResult asacTestSuiteResult : this.asacTestSuiteResultList) {
      asacTestSuiteResult.runTests();
    }
    
  }

  /**
   * run all tests
   */
  public void runTests(List<String> tests) {

    Set<String> suitesToRunAll = new HashSet<String>();
    
    Map<String, List<String>> testsToRunInSuite = new HashMap<String, List<String>>();
    
    //lets keep track of which things are full suites and which things are individual tests inside a suite
    for (String test : tests) {
      if (test.contains(".")) {
        String suite = StandardApiClientUtils.prefixOrSuffix(test, ".", true);
        String testName = StandardApiClientUtils.prefixOrSuffix(test, ".", false);
        List<String> testNames = testsToRunInSuite.get(suite);
        if (!testsToRunInSuite.containsKey(suite)) {
          testNames = new ArrayList<String>();
          testsToRunInSuite.put(suite, testNames);
        }
        testNames.add(testName);
      } else {
        suitesToRunAll.add(test);
      }
    }
    
    for (Class<? extends AsacTestSuiteResult> theClass: ALL_TEST_SUITES) {
      
      AsacTestSuiteResult asacTestSuiteResult = StandardApiClientUtils.construct(theClass, 
          new Class<?>[]{AsacTestSuiteResults.class}, new Object[]{this});
      
      //lets see if it matches a suite or test in a suite
      String suiteName = asacTestSuiteResult.getName();
      if (suitesToRunAll.contains(suiteName) || testsToRunInSuite.containsKey(suiteName)) {
        this.asacTestSuiteResultList.add(asacTestSuiteResult);
      }
    }
    
    //run them
    for (AsacTestSuiteResult asacTestSuiteResult : this.asacTestSuiteResultList) {
      String suiteName = asacTestSuiteResult.getName();
      if (suitesToRunAll.contains(suiteName)) {
        asacTestSuiteResult.runTests();
      } else {
        //else just run specific tests
        asacTestSuiteResult.runTests(testsToRunInSuite.get(suiteName));
      }
    }
    
  }

  /**
   * list of tests
   */
  private List<AsacTestSuiteResult> asacTestSuiteResultList = new ArrayList<AsacTestSuiteResult>();

  /**
   * if the indent flag should be sent to the server
   */
  private boolean indent = false;
  
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
   * summary label overall
   * @return the summary label
   */
  public String getSummaryLabel() {
    return this.isSuccess() ? "SUCCESS" : "FAILURE";
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


  /**
   * if indent flag should be sent
   * @param indent1
   * @return this for chaining
   */
  public void setIndent(boolean indent1) {
    this.indent = indent1;
  }


  
  /**
   * if indent flag should be set
   * @return the indent
   */
  public boolean isIndent() {
    return this.indent;
  }
  
  
  
  
}
