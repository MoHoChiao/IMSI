#!/bin/bash
kill $(ps aux | grep  '[D]MSLoader' | awk '{print $2}')
