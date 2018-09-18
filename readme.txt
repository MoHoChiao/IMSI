

IMSI Query 的設計

src folder:
	/data/imsi_mapping/src/lsr/10.108.200.141/
	/data/imsi_mapping/src/lsr/10.108.200.142/
	/data/imsi_mapping/src/lsr/10.108.200.143/
	
	sub folder and file name structure
	yyMMdd/HH/yyyyMMddHH.{unix_time_sec}.csv.gz
	
mapped folder:
	/data/imsi_mapping/mapped/lsr/10.108.200.141/
	/data/imsi_mapping/mapped/lsr/10.108.200.142/
	/data/imsi_mapping/mapped/lsr/10.108.200.143/

	sub folder and file name structure
	yyMMdd/HH/yyyyMMddHH.{unix_time_sec}.csv
	yyMMdd/HH/yyyyMMddHH.{unix_time_sec}.missing.csv
	
Server 連接配置
	
NK-IMSIServer01 (10.108.61.171, 10.108.61.172)
  ^--> LSR
  
NH-IMSIServer01 (10.108.61.129, 10.108.61.131)
  ^--> DMS 
  

	