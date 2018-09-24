#!/bin/bash
if ps aux | grep '[D]MSClusterReader' > /dev/null ; then
  echo 'DMSClusterReader is already running...'
else
  cd "${0%/*}"
  date '+%Y-%m-%d %H:%M:%S' >> history.txt
  mv nohup.out nohup_$(date +%F-%H:%M).out
  nohup java -cp ./NetScoutMerger-fat.jar tw.moze.imsi.reader.DMSClusterReader &>nohup.out &
fi

