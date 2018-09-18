#!/bin/bash
if ps aux | grep  '[D]MSReader' > /dev/null ; then
  echo 'DMSReader is Running...'
  cd "${0%/*}"
  tail -f -n300 nohup.out
else
  echo 'DMSReader is not Running...'
fi
