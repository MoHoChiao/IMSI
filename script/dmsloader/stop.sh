#!/bin/bash
kill $(ps aux | grep  '[D]MSClusterLoader' | awk '{print $2}')
