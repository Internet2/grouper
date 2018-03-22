#!/usr/bin/perl -w

#
# Copyright 2018 Internet2
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


# This program scans all files under grouper/src/test with class names *Tests, *Test, Test*, or Suite .
# For each file, it first parses the tests contained in the class (where signature is public void test*),
# and outputs a summary of what it finds. It then prints a call hierarchy starting with the AllTests class,
# traversing each suite or class referenced in each class. Finally it outputs classes which look like unit
# test files which can't be reached from a path starting wth AllTests. Some of these will be classes with
# similar names but aren't unit test classes, while others may be candidates for adding to an existing test
# suite


use File::Find;

my @candidate_files = ();

sub findTestFiles {
    push @candidate_files, $File::Find::name if /^(.*Tests|.*Test|Test.*|Suite.*)\.java$/;
}

find(\&findTestFiles, '../src/test');


my %tests = ();   # class -> test.class
my %suites = ();  # class -> suite.class

my %classMap = ();  # class -> testId (grouper subpackage + class)
my %testCount = ();  # class -> int : sum of all tests under this hierarchy

my %calledBy = ();

my %fileMap = ();

my @classes = ();

print "File summary (" . scalar(gmtime) . ")\n", '-'x39, "\n\n";

print join("\t", 'Filename', 'Class', 'TestId (can be used as param to AllTests)', 'Number of Tests', 'Number of Suites', 'Number of manual Suites', 'Parent Class', 'has JUnit-BEGIN'), "\n";

foreach my $file (@candidate_files) {
    next if $file =~ /^--/;
    last if $file =~ /^#/;
    $file =~ s/\r?\n//;
    my @tests = ();
    my @suites = ();
    my @suitesManual = ();
    my $class = undef;
    my $extends = undef;
    my $isJunitBegin= 0;
    my $testId = $file;
    $testId =~ s/\//./g;
    $testId =~ s/.*edu\.internet2\.middleware\.grouper\.(.*)\.java/$1/;

    open(IN, $file);
    while (<IN>) {
        if (/^\s*public class (\w+)( extends (\w+))?/) {
            $class = $1;
            $extends = $3;
        } elsif (/^\s*public void (test\w+)/) {
            push @tests, $1;
        } elsif (/^\s*public static void (test\w+)/) {
            print "WARNING: public static void isn't a valid signature for a unit test ($file#$1)\n";
        } elsif (m~^\s*//\$JUnit-BEGIN\$~) {
            $isJunitBegin = 1;
        } elsif (/^\s*suite\.addTestSuite\((\w+)\.class/) {
            push @suites, $1;
        } elsif (/^\s*suite\.addTest\((\w+)\.suite/) {
            push @suitesManual, $1;
        }
    }
    close IN;

    print join("\t",
        $file,
        $class,
        $testId,
        scalar(@tests),
        scalar(@suites),
        scalar(@suitesManual),
        $extends||'?',
        $isJunitBegin,
    ), "\n";

    $tests{$class} = [@tests];
    $suites{$class} = [@suites, @suitesManual];
    $classMap{$class} = $testId;
    $fileMap{$class} = $file;

    push @classes, $class;
}



print "\n\nCall hierarchy\n", '-'x14, "\n\n";

&countSub('AllTests');

&printSub('AllTests', 0);


print "\n\nUnreferenced classes\n", '-'x20, "\n\n";

foreach my $class (@classes) {
    if (!exists $calledBy{$class} and $class ne 'AllTests') {
        printSub($class, 0);
    }
}
exit 0;


# Also sets %calledBy
sub countSub {
    my ($class) = @_;
    my $count = scalar(@{$tests{$class}});
    foreach my $test (@{$tests{$class}}) {
        $calledBy{$test} = $class;
    }

    foreach my $suite (@{$suites{$class}}) {
        $count += &countSub($suite);
        if (!exists $calledBy{$suite}) {
            $calledBy{$suite} = $class;
        } else {
            print "WARNING: class $class called by both $calledBy{$suite} and $class\n";
        }
    }

    $testCount{$class} = $count;



}


sub printSub {
    my ($class, $level) = @_;
    print "    "x$level, $classMap{$class} || '?'.$class, "\t", $testCount{$class}||0, "\n";
    foreach my $test (@{$tests{$class}}) {
        print "    "x($level+1), "* $test\n";
    }
    foreach my $suite (@{$suites{$class}}) {
        #print "    "x($level+1), "$suite\n";
        &printSub($suite, 1+$level)
    }

}

__END__
