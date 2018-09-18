#!/bin/bash
if ps aux | grep  '[D]MSLoader' > /dev/null ; then
  echo 'DMSLoader is Running...'
  cd "${0%/*}"
  tail -f -n300 nohup.out 
else
  echo 'DMSLoader is not Running...'
fi
