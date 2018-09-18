#!/bin/bash
if ps aux | grep  '[L]SRLoader' > /dev/null ; then
  echo 'LSRLoader is Running...'
  cd "${0%/*}"
  tail -f -n300 nohup.out
else
  echo 'LSRLoader is not Running...'
fi
