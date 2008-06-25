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
import org.hsqldb.util.SqlTool.SqlToolException;

/**
 * This class extends TestSetup to open and close the Grouper and Signet HSQL
 * dabatabases around the test suite.
 */
public class DatabaseWrapperTestSetup extends TestSetup {

    private static String DB_SRC     = "testDb";
    private static String DB_TARGET  = "target/testDb";
    private static int    DB_PORT    = 51515;
    private HsqlRunner    hsqlRunner = null;

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

        File dbDir = new File(DB_TARGET);

        FileUtils.deleteDirectory(dbDir);
        FileUtils.copyFileToDirectory(new File(DB_SRC + "/grouper/hsqldb/grouper.properties"), dbDir);
        FileUtils.copyFileToDirectory(new File(DB_SRC + "/grouper/hsqldb/grouper.script"), dbDir);
        FileUtils.copyFileToDirectory(new File(DB_SRC + "/signet/hsqldb/signet.properties"), dbDir);
        FileUtils.copyFileToDirectory(new File(DB_SRC + "/signet/hsqldb/signet.script"), dbDir);

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
        hsqlRunner.stop();

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
            server.setPort(DB_PORT);
            server.setDatabasePath(0, DB_TARGET + "/grouper");
            server.setDatabaseName(0, "grouperdb");
            server.setDatabasePath(1, DB_TARGET + "/signet");
            server.setDatabaseName(1, "signetdb");
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
