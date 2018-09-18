#!/bin/bash
cd "${0%/*}"
nohup java -cp ./NetScoutMerger-fat.jar tw.moze.imsi.loader.DMSLoader &> nohup.out &
