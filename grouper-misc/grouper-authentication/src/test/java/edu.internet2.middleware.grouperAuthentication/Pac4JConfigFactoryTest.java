package edu.internet2.middleware.grouperAuthentication;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import junit.framework.TestCase;
import junit.textui.TestRunner;

public class Pac4JConfigFactoryTest extends TestCase {
    /**
     * @param name
     */
    public Pac4JConfigFactoryTest(String name) {
        super(name);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        //TestRunner.run(GrouperBoxFullRefreshTest.class);
        TestRunner.run(new Pac4JConfigFactoryTest("testPac4JConfigFactorCAS"));
    }

    /**
     *
     */
    public void testPac4JConfigFactorCAS() {
        String ringo = "";
        String authMechanism = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.mechanism");

    }
}
