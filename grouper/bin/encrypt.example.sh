#!/bin/sh

# if not in bin dir, but in grouper_home then cd to bin
[ -e bin/encrypt.example.sh ] && cd bin

[ -e encrypt.sh ] || echo "Cant find encrypt.sh.  Are you in the grouper_home dir or grouper_home\bin dir?"
[ -e encrypt.sh ] || exit 1

JAVA=java

GROUPERENCRYPT=GROUPERENCRYPT=edu.internet2.middleware.grouper.util.rijndael.Encrypt

GROUPER_HOME=..

GROUPER_CONF=../conf

MEM_START=64m

MEM_MAX=256m

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/invoker.jar -cpdir ${GROUPER_CONF} -cpalljars ${GROUPER_HOME}/lib -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar ${GROUPERENCRYPT_JVMARGS} ${GROUPERENCRYPT} "$@"

exit $?
