#!/bin/bash
if ps aux | grep  '[D]MSClusterReader' > /dev/null ; then
  echo 'DMSClusterReader is Running...'
  cd "${0%/*}"
  tail -f -n300 nohup.out
else
  echo 'DMSClusterReader is not Running...'
fi
