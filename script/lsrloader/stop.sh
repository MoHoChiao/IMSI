#!/bin/bash
kill $(ps aux | grep  '[L]SRClusterLoader' | awk '{print $2}')
