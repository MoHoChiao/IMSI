#!/bin/bash
if ps aux | grep '[D]MSClusterLoader' > /dev/null ; then
  echo 'DMSClusterLoader is already running...'
else
  cd "${0%/*}"
  date '+%Y-%m-%d %H:%M:%S' >> history.txt
  mv nohup.out nohup_$(date +%F-%H:%M).out
  nohup java -cp ./NetScoutMerger-fat.jar tw.moze.imsi.loader.DMSClusterLoader &>nohup.out &
fi

