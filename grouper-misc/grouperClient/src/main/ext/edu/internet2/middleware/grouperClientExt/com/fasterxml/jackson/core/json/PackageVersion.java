package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.json;

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
        "2.14.2", "edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core", "jackson-core");

    @Override
    public Version version() {
        return VERSION;
    }
}
