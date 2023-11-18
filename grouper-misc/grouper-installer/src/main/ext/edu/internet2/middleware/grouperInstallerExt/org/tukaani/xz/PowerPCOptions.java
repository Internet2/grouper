/*
 * PowerPCOptions
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package edu.internet2.middleware.grouperInstallerExt.org.tukaani.xz;

import java.io.InputStream;
import edu.internet2.middleware.grouperInstallerExt.org.tukaani.xz.simple.PowerPC;

/**
 * BCJ filter for big endian PowerPC instructions.
 */
public class PowerPCOptions extends BCJOptions {
    private static final int ALIGNMENT = 4;

    public PowerPCOptions() {
        super(ALIGNMENT);
    }

    public FinishableOutputStream getOutputStream(FinishableOutputStream out) {
        return new SimpleOutputStream(out, new PowerPC(true, startOffset));
    }

    public InputStream getInputStream(InputStream in) {
        return new SimpleInputStream(in, new PowerPC(false, startOffset));
    }

    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.POWERPC_FILTER_ID);
    }
}
