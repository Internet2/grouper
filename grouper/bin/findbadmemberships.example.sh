#!/bin/sh

# Run Grouper's Bad Membership Finder Utility

# if not in bin dir, but in grouper_home, then cd to bin
[ -e bin/findbadmemberships.sh ] && cd bin

[ -e findbadmemberships.sh ] || echo "Cant find findbadmemberships.sh.  Are you in the grouper_home dir or grouper_home\bin dir?"
[ -e findbadmemberships.sh ] || exit 1

JAVA=java

APP=edu.internet2.middleware.grouper.FindBadMemberships

GROUPER_HOME=..

GROUPER_CONF=../conf

MEM_START=256m

MEM_MAX=768m

${JAVA} -Xms${MEM_START} -Xmx${MEM_MAX} -jar ${GROUPER_HOME}/lib/invoker.jar -cpdir ${GROUPER_CONF} -cpalljars ${GROUPER_HOME}/lib -cpjar ${GROUPER_HOME}/dist/lib/grouper.jar ${APP} "$@"

exit $?
