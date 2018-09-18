#!/bin/bash
kill $(ps aux | grep  '[L]SRLoader' | awk '{print $2}')
