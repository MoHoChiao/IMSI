#!/bin/bash
kill $(ps aux | grep  '[L]SRReader' | awk '{print $2}')
