#!/usr/bin/bash

# Can execute this in a Docker container=: `docker run -it --rm tier/grouper bash`

unset GROUPER_HOME
unset GROUPER_CONF
unset JAVA_HOME

GRP_API_DIR=/opt/grouper/grouper.apiBinary
GRP_UI_DIR=/opt/grouper/grouper.ui/WEB-INF
GRP_STAGE_DIR=/tmp/grouper-stage
export CLASSPATH="x y z"

#GRP_API_DIR=/home/cer28/Git/grouper-dist/grouper/grouper
#GRP_UI_DIR=/home/cer28/Git/grouper-dist/grouper/grouper-ui/dist/grouper/WEB-INF

mkdir -p $GRP_STAGE_DIR
mkdir -p $GRP_STAGE_DIR/fake-java-true/bin
mkdir -p $GRP_STAGE_DIR/fake-java-false/bin

cp -p /usr/bin/true $GRP_STAGE_DIR/fake-java-true/bin/java
cp -p /usr/bin/false $GRP_STAGE_DIR/fake-java-false/bin/java

export JAVA_HOME=$GRP_STAGE_DIR/fake-java-true
# JAVA_HOME=/usr/lib/jvm/zulu-8/


# Test 1: Execute gsh from current directory
test1() {
	unset GROUPER_HOME
	unset GROUPER_CONF

	echo "Test 1A Execute ./gsh from api bin directory"
	cd $GRP_API_DIR/bin
	./gsh.sh
	echo exitcode $? should be 0
	echo '--------'

	echo "Test 1b Execute ./gsh from webapp bin directory"
	cd $GRP_UI_DIR/bin
	./gsh.sh
	echo exitcode $? should be 0
	echo '--------'

	#echo "Test 1b Execute ./gsh from a Maven directory
	#/home/Git/grouper/grouper/unc-grouper-ui/unc-grouper-ui-war/target/grouper/WEB-INF/bin/gsh
	#echo exitcode $? should be 0
}

# Test 2: Set grouper home and call gsh from outside grouper
test2() {

	unset GROUPER_HOME
	unset GROUPER_CONF

	cd /tmp

	echo "Test 2A Execute gsh from api bin directory"
	$GRP_API_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'

	echo "Test 2B Execute gsh from webapp bin directory"
	$GRP_UI_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'

	echo "Test 2C Execute gsh from a Maven directory"
	/cygdrive/c/Users/cer28/Documents/UNC/Git/grouper/grouper/unc-grouper-ui/unc-grouper-ui-war/target/grouper/WEB-INF/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'
}


# Test 3: Set grouper home and call gsh in non-grouper directory
test3() {
	JAVA_HOME=$GRP_STAGE_DIR/fake-java-true
	unset GROUPER_HOME
	unset GROUPER_CONF
	cd /tmp

	mkdir -p $GRP_STAGE_DIR/test3/bin

	cp -p $GRP_API_DIR/bin/gsh.sh $GRP_STAGE_DIR/test3/bin

	echo "Test 3A set grouper home as api, should not use gsh directory for calcs"
	GROUPER_HOME=$GRP_API_DIR \
	  $GRP_STAGE_DIR/test3/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'

	echo "Test 3B set grouper home as webapp, should not use gsh directory for calcs"
	GROUPER_HOME=$GRP_UI_DIR \
	  $GRP_STAGE_DIR/test3/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'
}


# Test 4: conf set to external dir (with spaces); execute from bin directory
test4() {
	ln -s $GRP_API_DIR/conf "$GRP_STAGE_DIR/Grouper Conf"
	ln -s $GRP_UI_DIR/classes "$GRP_STAGE_DIR/Grouper Classes"

	JAVA_HOME=$GRP_STAGE_DIR/fake-java-true
	unset GROUPER_HOME
	unset GROUPER_CONF
	cd /tmp

	echo "Test 4A conf set to external dir (with spaces); execute from api bin directory"
	GROUPER_CONF="$GRP_STAGE_DIR/Grouper Conf"
	$GRP_API_DIR/bin/gsh.sh
	echo "exitcode $? should be 0"
	echo '--------'

	echo "Test 4B conf set to external dir (with spaces); execute from webapp bin directory"
	set GROUPER_CONF=$GRP_STAGE_DIR/Grouper Classes
	$GRP_UI_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'
}

# Test 5: define grouper_conf, including spaces in name
test5() {
	ln -s $GRP_API_DIR/conf "$GRP_STAGE_DIR/Grouper Conf"
	ln -s $GRP_UI_DIR/classes "$GRP_STAGE_DIR/Grouper Classes"

	JAVA_HOME=$GRP_STAGE_DIR/fake-java-true
	unset GROUPER_HOME
	unset GROUPER_CONF
	cd /tmp

	echo "Test 5A grouper conf is staging/Grouper+Conf; run from api bin"
	GROUPER_CONF="$GRP_STAGE_DIR/Grouper Conf" \
	  $GRP_API_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'

	echo "Test 5B grouper conf is staging/Grouper+Classes; run from webapp bin"
	GROUPER_CONF="$GRP_STAGE_DIR/Grouper Classes" \
	  $GRP_UI_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'

	echo "Test 5C mix up conf and classes does not matter"
	GROUPER_CONF="$GRP_STAGE_DIR/Grouper Conf" \
	  $GRP_UI_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'

	echo "Test 5D mix up conf and classes does not matter"
	GROUPER_CONF="$GRP_STAGE_DIR/Grouper Classes" \
	  $GRP_API_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo '--------'
}

# Test 6: define grouper_home, optionally grouper_conf
test6() {
	#MKLINK /J "$GRP_STAGE_DIR/Grouper API Home" $GRP_API_DIR
	#MKLINK /J "$GRP_STAGE_DIR/Grouper WEBAPP Home" ${GRP_API_DIR}-ui/dist/grouper

	ln -s $GRP_API_DIR "$GRP_STAGE_DIR/Grouper API Home"
	ln -s $GRP_UI_DIR/.. "$GRP_STAGE_DIR/Grouper WEBAPP Home"

	JAVA_HOME=$GRP_STAGE_DIR/fake-java-true
	unset GROUPER_HOME
	unset GROUPER_CONF
	cd /tmp

	echo "Test 6A gsh location is webapp, home set to api"
	GROUPER_HOME="$GRP_STAGE_DIR/Grouper API Home" \
	  "$GRP_STAGE_DIR/Grouper WEBAPP Home/WEB-INF/bin/gsh.sh"
	echo exitcode $? should be 0
	echo '--------'

	echo "Test 6B gsh location is api, home set to webapp"
	GROUPER_HOME="$GRP_STAGE_DIR/Grouper WEBAPP Home/WEB-INF" \
	  "$GRP_STAGE_DIR/Grouper API Home/bin/gsh.sh"
	echo exitcode $? should be 0
	echo '--------'


	echo "Test 6C home and conf are set differently"
	GROUPER_HOME="$GRP_STAGE_DIR/Grouper API Home" \
	  GROUPER_CONF="$GRP_STAGE_DIR/Grouper Conf" \
	  "$GRP_STAGE_DIR/Grouper API Home/bin/gsh.sh"
	echo exitcode $? should be 0
	echo '--------'
}


# Test 7: test failures
test7() {
	mkdir -p $GRP_STAGE_DIR/fake-home/bin
	cp $GRP_API_DIR/bin/gsh.sh $GRP_STAGE_DIR/fake-home/bin/
	mkdir -p $GRP_STAGE_DIR/fake-home/conf

	export JAVA_HOME=$GRP_STAGE_DIR/fake-java-true

	unset GROUPER_HOME
	unset GROUPER_CONF
	cd /tmp

	echo "Test 7A grouper_home not a good home (no grouper jar)"
	GROUPER_HOME=$GRP_STAGE_DIR/fake-home \
	  "$GRP_API_DIR/bin/gsh.sh"
	echo exitcode $? should be 80
	echo '--------'

	echo "Test 7B parent of gsh not a good home (no grouper jar)"
	unset GROUPER_HOME
	"$GRP_STAGE_DIR/fake-home/bin/gsh.sh"
	echo exitcode $? should be 81
	echo '--------'

	echo "Test 7C grouper_conf not good (missing directory)"
	GROUPER_CONF=C:/Bogus-path \
	  "$GRP_API_DIR/bin/gsh.sh"
	echo exitcode $? should be 82
	echo '--------'

	echo "Test 7D grouper_conf not good (missing hibernate properties)"
	GROUPER_CONF=$GRP_STAGE_DIR/fake-home/conf \
	  "$GRP_API_DIR/bin/gsh.sh"
	echo exitcode $? should be 82
	echo '--------'
}

# Test 8: -initEnv tests
test8() {
	unset GROUPER_HOME
	unset GROUPER_CONF

	echo "Test 8A -initEnv normal usage, grouper_home and conf reset, based on gsh parent"
	source $GRP_API_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo "Value of GROUPER_HOME: $GROUPER_HOME (should be api dir)"
	echo "Value of GROUPER_CONF: $GROUPER_CONF (should be api conf dir)"
	echo '--------'

	unset GROUPER_HOME
	unset GROUPER_CONF

	echo "Test 8B -initEnv same, webapp structure"
	source $GRP_UI_DIR/bin/gsh.sh
	echo exitcode $? should be 0
	echo "Value of GROUPER_HOME: \"$GROUPER_HOME\" (should be ui dir)"
	echo "Value of GROUPER_CONF: \"$GROUPER_CONF\" (should be ui conf dir)"
	echo '--------'

	unset GROUPER_HOME
	unset GROUPER_CONF

	echo "Test 8C -initEnv normal usage, grouper_home and conf reset, based on gsh parent, passing $2 as conf dir"
	echo "Unimplemented in bash script"
	echo '--------'


	echo "Test 8D same, webapp structure"
	echo "Unimplemented in bash script"
	echo '--------'

	echo "Test 8E -initEnv plus parent of gsh not a good home (no grouper jar)"
	source "$GRP_STAGE_DIR/fake-home/bin/gsh.sh"
	echo exitcode $? should be 81
	echo "Value of GROUPER_HOME: \"$GROUPER_HOME\" (should be blank)"
	echo "Value of GROUPER_CONF: \"$GROUPER_CONF\" (should be blank)"
	echo '--------'

	unset GROUPER_HOME
	unset GROUPER_CONF

	echo "Test 8F -initEnv plus $2 parameter grouper_conf not good (no hibernate properties)"
	echo "Unimplemented in bash script"
	echo '--------'

	echo "Test 8G -initEnv plus spaces in name"
	source "$GRP_STAGE_DIR/Grouper API Home/bin/gsh.sh"
	echo exitcode $? should be 0
	echo "Value of GROUPER_HOME: \"$GROUPER_HOME\" (should be Grouper API Home)"
	echo "Value of GROUPER_CONF: \"$GROUPER_CONF\" (should be Grouper API Home/conf)"
	echo '--------'

	unset GROUPER_HOME
	unset GROUPER_CONF

	echo "Test 8H -initEnv plus spaces in name, passing $2"
	echo "Unimplemented in bash script"
	echo '--------'

	unset GROUPER_HOME
	unset GROUPER_CONF
}


# Test 9: gsh exit codes
test9() {
	unset GROUPER_HOME
	unset GROUPER_CONF
	cd /tmp

	echo "Test 9A Java returns false"
	JAVA_HOME=$GRP_STAGE_DIR/fake-java-false \
	  "$GRP_API_DIR/bin/gsh.sh"
	echo exitcode $? should be 1
	echo '--------'

	echo "Test 9B invalid Java"
	JAVA_HOME=$GRP_STAGE_DIR/fake-java-BOGUS \
	  "$GRP_API_DIR/bin/gsh.sh"
	echo exitcode $? should be 3
	echo '--------'
}


if [ -n "$1" ]; then
	echo calling $1
	$1
else
	test1
	test2
	test3
	test4
	test5
	test6
	test7
	test8
	test9
fi
exit 0




