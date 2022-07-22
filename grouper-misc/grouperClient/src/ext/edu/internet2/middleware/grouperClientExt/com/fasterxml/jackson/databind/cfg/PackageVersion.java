package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.cfg;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.Version;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.Versioned;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.util.VersionUtil;

/**
 * Automatically generated from PackageVersion.java.in during
 * packageVersion-generate execution of maven-replacer-plugin in
 * pom.xml.
 */
public final class PackageVersion implements Versioned {
    public final static Version VERSION = VersionUtil.parseVersion(
        "2.13.3", "com.fasterxml.jackson.core", "jackson-databind");

    @Override
    public Version version() {
        return VERSION;
    }
}
