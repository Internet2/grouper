# The following describes the purpose of the testSetX directory.

# The configuration files in this directory are an example that was used
# during development to start Auth2ldap and the Signet and Grouper
# built-in test case databases, but not to run the standard test cases.
# Rather, the build script will prompt for the Auth2ldap command line
# parameters.

# The antmaster.properties file in the testSetX directory is
# designed to run Auth2ldap with settings in the antSystem.properties
# file that explicitly set the database related parameters.  This is
# as opposed to the implicit setting done when running the built-in
# test cases from the standard directory (testDb) in the standard format.
# The explicit settings allow starting both the Signet and Grouper databases
# from elsewhere than from a directory under the installation directory
# determined by the gsDataDir parameter.  The values used in the 
# antSystem.properties file in the runExplicitConfig directory are actually
# the same values as are used to run the built-in test cases; however,
# other locations could be used.  Currently, only both Signet and Grouper
# can be started using the same port.  This needs to be changed to allow
# them to be started independently; however, generally when users are 
# not using the built-in test cases, they will be starting their own
# Signet and Grouper databases without using this build file.
