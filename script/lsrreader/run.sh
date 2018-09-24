#!/bin/bash
if ps aux | grep '[L]SRClusterReader' > /dev/null ; then
  echo 'LSRClusterReader is already running...'
else  
  cd "${0%/*}"
  date '+%Y-%m-%d %H:%M:%S' >> history.txt
  mv nohup.out nohup_$(date +%F-%H:%M).out
  nohup java -cp ./NetScoutMerger-fat.jar tw.moze.imsi.reader.LSRClusterReader &>nohup.out &
fi

