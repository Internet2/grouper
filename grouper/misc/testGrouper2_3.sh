#!/bin/bash -v

branch=GROUPER_2_3_BRANCH
release=2.3.0

if [ -d "/tmp/testGrouper/$branch" ]; then
  /bin/rm -rf "/tmp/testGrouper/$branch"
fi

mkdir -p /tmp/testGrouper/$branch

cd /tmp/testGrouper/$branch
wget https://software.internet2.edu/grouper/release/$release/grouperInstaller.jar

export JAVA_HOME=/opt/java8
export PATH=$JAVA_HOME/bin:$PATH

FILE="/tmp/testGrouper/$branch/grouper.installer.properties"

/bin/cat <<EOM >$FILE
download.server.url = http://software.internet2.edu/grouper
grouper.version = 2.3.0
grouperInstaller.useTestPatches = true
grouperInstaller.autorun.useDefaultsAsMuchAsAvailable = true
grouperInstaller.print.autorunKeys = true
grouperInstaller.autorun.deleteAndInitDatabase = true
grouperInstaller.autorun.addQuickstartSubjectsToDb = false
grouperInstaller.autorun.addQuickstartData = false
grouperInstaller.autorun.installUi = false
grouperInstaller.autorun.tomcatPorts = 8600,8601,8602
grouperInstaller.autorun.grouperSystemPassword = pass
grouperInstaller.autorun.installWs = false
grouperInstaller.autorun.installClient = false
grouperInstaller.autorun.installPspng = false
grouperInstaller.autorun.installPsp = false
grouperInstaller.autorun.startGrouperDaemons = false
grouperInstaller.autorun.installGrouperWsScim = false
grouperInstaller.autorun.installGrouperRabbitMqMessaging = false
grouperInstaller.autorun.installGrouperAwsSqsMessaging = false
grouperInstaller.autorun.installGrouperActiveMqMessaging = false
EOM

java -cp .:grouperInstaller.jar edu.internet2.middleware.grouperInstaller.GrouperInstaller

wget https://github.com/Internet2/grouper/archive/$branch.zip

unzip -q $branch.zip

rm $branch.zip

cd /tmp/testGrouper/$branch/grouper-$branch/grouper

/home/mchyzer/software/ant/bin/ant test.compile

cp -R /tmp/testGrouper/$branch/grouper-$branch/grouper/src/test/edu /tmp/testGrouper/$branch/grouper.apiBinary-$release/conf
cp -R /tmp/testGrouper/$branch/grouper-$branch/grouper/dist/build/test/edu /tmp/testGrouper/$branch/grouper.apiBinary-$release/conf

cd /tmp/testGrouper/$branch/grouper.apiBinary-$release

./bin/gsh -test -all
