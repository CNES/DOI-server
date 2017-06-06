#!/bin/sh
################################
#   Generates Datacite schema  #
################################
xjc -b dataciteBinding.xml https://schema.datacite.org/meta/kernel-4.0/metadata.xsd
