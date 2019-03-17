#!/bin/sh
################################
#   Generates Datacite schema  #
################################
xjc -b dataciteBinding.xml https://schema.datacite.org/meta/kernel-4.1/metadata.xsd
