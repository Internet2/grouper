package edu.internet2.middleware.ldappcTest;

import junit.extensions.TestSetup;
import junit.framework.Test;

public class TestServerWrapperSetup extends TestSetup {

    private static String HSQLDB_JAR = "~/.m2/repository/hsqldb/hsqldb/1.8.0.7/hsqldb-1.8.0.7.jar";
    private static long   DB_PORT    = 51515;
    private static String DB_SRC     = "testDb";
    private static String DB_TARGET  = "target/testDb";
    private static String SQLTOOL_RC = "testDb/sqltool.rc";

    public TestServerWrapperSetup(Test suite) {
        super(suite);
    }

    protected void setUp() throws Exception {
        System.out.println("**********Setting up*************");
        System.out.flush();

//        <!-- output directories -->
//        <property name="builddir" location="build"/>
//        <property name="buildapp" location="${builddir}/${app}"/>
//        <property name="buildapp.classes" location="${buildapp}/classes"/>
//        <property name="buildapp.schemadir" location="${buildapp.classes}/edu/internet2/middleware/${app}/schema"/>
//        <property name="buildapp.lib" location="${buildapp}/lib"/>
//        <property name="buildapp.logs" location="${buildapp}/logs"/>
//        <property name="buildapp.conf" location="${buildapp}/conf"/>
//        <property name="buildapp.scripts" location="${buildapp}/scripts"/>
//
//        <property name="distdirName" value="distributions"/>
//        <property name="distdir" location="${basedir}/${distdirName}"/>
//        <property name="distdir.src" location="${distdir}/src"/>
//        <property name="distdir.conf" location="${distdir}/conf"/>
//        <property name="distdir.doc" location="${distdir}/doc"/>
//        <property name="distdir.doc.javadoc" location="${distdir.doc}/javadoc"/>
//        <property name="distdir.lib" location="${distdir}/lib"/>
//        <property name="distdir.src" location="${distdir}/src"/>
//        <property name="distdir.src.java" location="${distdir.src}/java"/>
//        <property name="distdir.testDb" location="${distdir}/testDb"/>
//        <property name="distdir.buildXml" location="${distdir}/build.xml"/>
//
//        <property name="tests" value="edu.internet2.middleware.ldappcTest.AllJUnitTests" />
//        <property name="verboseDeletes" value="false"/>
//        <property name="buildapp.doc" value="${buildapp}/doc"/>
//        <!--
//        <property name="buildapp.doc.javadoc" value="${buildapp.doc}/javadoc"/>
//         -->
//        <property name="javadocPackages" 
//                value="edu.internet2.middleware.ldappc.*,edu.internet2.middleware.ldappcTest.*" />
//        <property name="logdir" value="${basedir}/logs" />
//        <property name="outputdir" value="${basedir}/output" />

//        <property name="master" value="conf/testConfigurations/testSetU/antMaster.properties"/>


//        <path id="buildClasspath">
//          <pathelement location="conf"/>
//          <pathelement location="testDb/grouper/conf"/>
//          <pathelement location="testDb/signet/conf"/>
//
//          <fileset dir="testDb/grouper/conf">
//            <include name="ehcache.xml"/>
//            <include name="grouper.ehcache.xml"/>
//            <include name="grouper.hibernate.properties"/>
//            <include name="grouper.properties"/>
//          </fileset>
//
//          <fileset dir="testDb/signet/conf">
//            <include name="hibernate.cfg.xml"/>
//          </fileset>
//        </path>

//        <echo message="Starting HSQLDB server"/>
//        rm -Rf ${DB_TARGET}
//        mkdir ${DB_TARGET}
//        mkdir ${DB_TARGET}/grouper
//        mkdir ${DB_TARGET}/signet
//        cp -p ${DB_SRC}/grouper/hsqldb/grouper.* ${DB_TARGET}/grouper
//        cp -p ${DB_SRC}/signet/hsqldb/signet.* ${DB_TARGET}/signet
//
//        java -Xmx300M -cp ${HSQLDB_JAR} org.hsqldb.Server -port ${DB_PORT} \
//            -database.0 ${DB_TARGET}/grouper -dbname.0 grouperdb \
//            -database.1 ${DB_TARGET}/signet  -dbname.1 signetdb
//
//        <!-- wait for at most 30 seconds-->
    }

    protected void tearDown() throws Exception {
        System.out.println("**********Tearing Down*************");
        System.out.flush();

//        <echo message="Stopping HSQLDB SIGNET"/>
//        java -Xmx300M -cp ${HSQLDB_JAR} org.hsqldb.util.SqlTool --rcfile ${SQLTOOL_RC} --sql "SHUTDOWN"\; signetdb

//        <echo message="Stopping HSQLDB GROUPER"/>
//        java -Xmx300M -cp ${HSQLDB_JAR} org.hsqldb.util.SqlTool --rcfile ${SQLTOOL_RC} --sql "SHUTDOWN"\; grouperdb

    }
}
