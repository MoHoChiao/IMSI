#!/bin/bash
if ps aux | grep  '[L]SRReader' > /dev/null ; then
  echo 'LSRReader is Running...'
  cd "${0%/*}"
  tail -f -n300 nohup.out
else
  echo 'LSRReader is not Running...'
fi
