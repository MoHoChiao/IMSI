#!/bin/bash
cd "${0%/*}"
find /data/imsi_mapping_cluster_v1/src -type f -mtime +1 -delete
find /data/imsi_mapping_cluster_v1/filtered/dms/10* -type f -mtime +2 -delete
find /data/imsi_mapping_cluster_v1/src -type d -empty -delete
find /data/imsi_mapping_cluster_v1/filtered -type d -empty -delete

