/*
 * SPARCOptions
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package edu.internet2.middleware.grouperInstallerExt.org.tukaani.xz;

import java.io.InputStream;
import edu.internet2.middleware.grouperInstallerExt.org.tukaani.xz.simple.SPARC;

/**
 * BCJ filter for SPARC.
 */
public class SPARCOptions extends BCJOptions {
    private static final int ALIGNMENT = 4;

    public SPARCOptions() {
        super(ALIGNMENT);
    }

    public FinishableOutputStream getOutputStream(FinishableOutputStream out) {
        return new SimpleOutputStream(out, new SPARC(true, startOffset));
    }

    public InputStream getInputStream(InputStream in) {
        return new SimpleInputStream(in, new SPARC(false, startOffset));
    }

    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.SPARC_FILTER_ID);
    }
}
