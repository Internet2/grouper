package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDownloadFileTest;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Throwaway suite for GRP-7438 (gsh template download files).  Delete before commit.
 */
public class TrashGshTemplateDownloadSuite {

  /**
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("TrashGshTemplateDownloadSuite");
    suite.addTestSuite(GshTemplateDownloadFileTest.class);
    // GshTemplateExec changed (assigns template config id on the output)
    suite.addTestSuite(GshTemplateExecTest.class);
    suite.addTestSuite(UiV2DownloadFileTest.class);
    return suite;
  }

}
