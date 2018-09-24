#!/bin/bash
if ps aux | grep  '[L]SRClusterLoader' > /dev/null ; then
  echo 'LSRClusterLoader is Running...'
  cd "${0%/*}"
  tail -f -n300 nohup.out
else
  echo 'LSRClusterLoader is not Running...'
fi
