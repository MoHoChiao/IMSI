#!/bin/bash
cd "${0%/*}"
java -cp ./NetScoutMerger-fat.jar tw.moze.imsi.report.DMSClusterReport
