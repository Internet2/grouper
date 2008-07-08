/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package edu.internet2.middleware.ldappcTest.wrappers;

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.commons.io.FileUtils;
import org.hsqldb.Server;

import edu.internet2.middleware.ldappc.util.ResourceBundleUtil;

/**
 * This class extends TestSetup to open and close the Grouper and Signet HSQL
 * dabatabases around the test suite.
 */
public class DatabaseWrapperTestSetup extends TestSetup {

    private boolean    useEmbeddedGrouper = true;
    private boolean    useEmbeddedSignet  = true;
    private int        dbPort             = 0;
    private String     grouperDbTarget    = null;
    private String     signetDbTarget     = null;
    private HsqlRunner hsqlRunner         = null;

    /**
     * @param suite
     *            the test suite to wrap with this class.
     */
    public DatabaseWrapperTestSetup(Test suite) {
        super(suite);
    }

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();

        useEmbeddedGrouper = "true".equals(ResourceBundleUtil.getString("testUseEmbeddedGrouper"));
        useEmbeddedSignet = "true".equals(ResourceBundleUtil.getString("testUseEmbeddedSignet"));

        if (!useEmbeddedGrouper && !useEmbeddedSignet) {
            return;
        }

        String dbPortStr = ResourceBundleUtil.getString("testEmbeddedDbPort");
        dbPort = Integer.parseInt(dbPortStr);

        grouperDbTarget = useEmbeddedGrouper ? ResourceBundleUtil.getString("testGrouperDbTarget") : "";
        signetDbTarget = useEmbeddedSignet ? ResourceBundleUtil.getString("testSignetDbTarget") : "";

        if (useEmbeddedGrouper) {
            File dbDir = new File(grouperDbTarget);
            FileUtils.deleteDirectory(dbDir);
            String dbSource = ResourceBundleUtil.getString("testGrouperDbSource");
            FileUtils.copyFileToDirectory(new File(dbSource + ".properties"), dbDir);
            FileUtils.copyFileToDirectory(new File(dbSource + ".script"), dbDir);
        }

        if (useEmbeddedSignet) {
            File dbDir = new File(signetDbTarget);
            if (!grouperDbTarget.equals(signetDbTarget)) {
                FileUtils.deleteDirectory(dbDir);
            }
            String dbSource = ResourceBundleUtil.getString("testSignetDbSource");
            FileUtils.copyFileToDirectory(new File(dbSource + ".properties"), dbDir);
            FileUtils.copyFileToDirectory(new File(dbSource + ".script"), dbDir);
        }

        hsqlRunner = new HsqlRunner();
        new Thread(hsqlRunner).start();

        // Wait for at most 30 seconds
        for (int i = 0; i < 30; i++) {
            Thread.sleep(1000);
            if (hsqlRunner.isStarted()) {
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        if (useEmbeddedGrouper || useEmbeddedSignet) {
            hsqlRunner.stop();
        }

        if (useEmbeddedGrouper) {
            File dbDir = new File(grouperDbTarget);
            FileUtils.deleteDirectory(dbDir);
        }

        if (useEmbeddedSignet) {
            File dbDir = new File(signetDbTarget);
            FileUtils.deleteDirectory(dbDir);
        }

        super.tearDown();
    }

    /**
     * Runnable class to maintain the database thread so that it can be opened
     * in a standard fashion by the subject API and by grouper and signet.
     */
    public class HsqlRunner implements Runnable {
        private Server  server  = null;
        private boolean started = false;

        /**
         * @return the database start status.
         */
        public boolean isStarted() {
            return started;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            server = new Server();
            server.setSilent(true);
            server.setPort(dbPort);
            int dbIndex = 0;
            if (useEmbeddedGrouper) {
                server.setDatabasePath(dbIndex, grouperDbTarget + "/grouper");
                server.setDatabaseName(dbIndex, "grouperdb");
                dbIndex++;
            }
            if (useEmbeddedSignet) {
                server.setDatabasePath(dbIndex, signetDbTarget + "/signet");
                server.setDatabaseName(dbIndex, "signetdb");
                dbIndex++;
            }
            server.start();
            started = true;
        }

        /**
         * Shut down the databases. Reset the start status.
         */
        public void stop() {
            server.shutdown();
            started = false;
        }
    }
}
