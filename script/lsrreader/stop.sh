#!/bin/bash
kill $(ps aux | grep  '[L]SRClusterReader' | awk '{print $2}')
