#!/bin/bash
if ps aux | grep  '[L]SRClusterLoader' > /dev/null ; then
  echo 'LSRClusterLoader is already running...'
else  
  cd "${0%/*}"
  date '+%Y-%m-%d %H:%M:%S' >> history.txt
  mv nohup.out nohup_$(date +%F-%H:%M).out
  nohup java -cp ./NetScoutMerger-fat.jar tw.moze.imsi.loader.LSRClusterLoader &>nohup.out &
fi
