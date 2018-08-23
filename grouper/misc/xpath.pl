#!/usr/bin/perl
use strict;
use warnings;
use XML::LibXML;

my $parser = XML::LibXML->new();
my $document = $parser->parse_file($ARGV[0]);
my @nodes = $document->findnodes($ARGV[1]);
for my $node (@nodes) {
  print $node->textContent, "\n";
}