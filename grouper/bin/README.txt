gsh provides a command line interface to Grouper. Many operations which were previously invoked using Ant or specialized scripts are now invoked from gsh. 

Information on command line arguments to gsh can be found by by running gsh with the -h option:

Windows type: gsh -h
*nix type: ./gsh.sh -h

Properties that influence the gsh startup:

    GROUPER_HOME: if set to a valid Grouper directory, it will use this directory. Otherwise, it will determine it based on the path to gsh
    GROUPER_CONF: if set to a valid conf directory, it will use this directory. Otherwise it will determine it based on GROUPER_HOME
    MEM_START: Override the default -Xms Java parameter (initial Java heap size)
    MEM_MAX: Override the default -Xmx Java parameter (maximum Java heap size)
    CLASSPATH: Will prepend to the constructed classpath
    GSH_JVMARGS: Additional arguments to pass to Java
    GSH_CYGWIN: if set and not blank, the script will convert paths and the classpath to Windows-style, for use with Windows Java under Cygwin
    GSH_QUIET: if set and not blank, will not output preliminary diagnostic information before starting Java, other than errors


If the file GROUPER_HOME/bin/setenv.sh exists (setenv.bat in Windows), the script will source this file before running.

Note that Windows has a -initEnv option for adding gsh to the path so you can run it from anywhere. For *nix 'source gsh.sh' for the same result.

gsh provides an interactive shell. See https://spaces.internet2.edu/pages/viewpage.action?pageId=14517859 for details.
