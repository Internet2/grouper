#!/bin/sh

# if not in bin dir, but in grouper_home, then cd to bin
[ -e groupertest.sh ] || cd bin

[ -e groupertest.sh ] || echo "Cant find groupertest.sh.  Are you in the grouper_home dir or grouper_home\bin dir?"
[ -e groupertest.sh ] || exit 1

JAVA=java

GROUPERTEST=edu.internet2.middleware.grouper.SuiteDefault

GROUPER_HOME=..

GROUPER_CONF=../conf

MEM_START=64m

MEM_MAX=256m

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/grouper/invoker.jar -cpdir ${GROUPER_CONF} -cpalljars ${GROUPER_HOME}/lib -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar -cpjar ${GROUPER_HOME}/dist/binary/zip/lib/test/grouper-test.jar ${GROUPERTEST_JVMARGS} ${GROUPERTEST} "$@"

exit $?
