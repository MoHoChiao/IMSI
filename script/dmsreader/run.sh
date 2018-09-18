#!/bin/bash
cd "${0%/*}"
nohup java -cp ./NetScoutMerger-fat.jar tw.moze.imsi.reader.DMSReader &> nohup.out &
