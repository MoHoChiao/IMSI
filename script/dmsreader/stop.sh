#!/bin/bash
kill $(ps aux | grep  '[D]MSClusterReader' | awk '{print $2}')
