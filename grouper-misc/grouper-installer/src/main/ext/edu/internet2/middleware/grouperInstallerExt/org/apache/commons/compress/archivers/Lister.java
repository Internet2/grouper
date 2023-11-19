/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.archivers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Simple command line application that lists the contents of an archive.
 *
 * <p>The name of the archive must be given as a command line argument.</p>
 * <p>The optional second argument defines the archive type, in case the format is not recognised.</p>
 *
 * @since 1.1
 */
public final class Lister {
    private static final ArchiveStreamFactory factory = new ArchiveStreamFactory();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            return;
        }
        System.out.println("Analysing "+args[0]);
        File f = new File(args[0]);
        if (!f.isFile()) {
            System.err.println(f + " doesn't exist or is a directory");
        }
        InputStream fis = new BufferedInputStream(new FileInputStream(f));
        ArchiveInputStream ais;
        if (args.length > 1) {
            ais = factory.createArchiveInputStream(args[1], fis);
        } else {
            ais = factory.createArchiveInputStream(fis);
        }
        System.out.println("Created "+ais.toString());
        ArchiveEntry ae;
        while((ae=ais.getNextEntry()) != null){
            System.out.println(ae.getName());
        }
        ais.close();
        fis.close();
    }

    private static void usage() {
        System.out.println("Parameters: archive-name [archive-type]");
    }

}