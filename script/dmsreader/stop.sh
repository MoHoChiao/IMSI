#!/bin/bash
kill $(ps aux | grep  '[D]MSReader' | awk '{print $2}')
