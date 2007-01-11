package edu.internet2.middleware.ldappcTest;

/**
 *  SkeletonTest.java
 *  Template used to create other tests
 */

import junit.framework.TestCase;


/**
 * A convenience class for starting a set of test cases.   It can also be used as 
 * a template for other test classes.
 */
public class SkeletonTest extends TestCase 
{
    /**
     * Class constructor
     * @param name Name of the test case.
     */
    public SkeletonTest(String name) 
    {
        super(name);
    }

    /**
     * Set up the fixture.
     */
    protected void setUp() 
    {
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() 
    {
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) 
    {
         junit.textui.TestRunner.run(SkeletonTest.class);
    }

    /**
     * A sanity test -- must always be okay or something is drastically wrong.
     */
    public void testAssert() 
    {
    	assertTrue(true);
    }
}
