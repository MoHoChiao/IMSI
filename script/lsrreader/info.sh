#!/bin/bash
if ps aux | grep  '[L]SRClusterReader' > /dev/null ; then
  echo 'LSRClusterReader is Running...'
  cd "${0%/*}"
  tail -f -n300 nohup.out
else
  echo 'LSRClusterReader is not Running...'
fi
