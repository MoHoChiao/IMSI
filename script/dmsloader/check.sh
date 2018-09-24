#!/bin/bash
if ps aux | grep  '[D]MSClusterLoader' > /dev/null ; then
  echo 'DMSClusterLoader is Running...'
  cd "${0%/*}"
  tail -f -n300 nohup.out 
else
  echo 'DMSClusterLoader is not Running...'
fi
