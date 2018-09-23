# This Python file uses the following encoding: utf-8
"""
generate user report file
python 2.x compatible

history:


2017-12-16:
* 一個檔案含八天統計資料，信件內文加入說明
* csv 'eNB--MME' 欄位內容變更
"""
import time
import csv
import os
import sys

basedir = "/data/imsi_mapping_cluster_v1/mapped"
dmsdir = basedir + "/dms/"
lsrdir = basedir + "/lsr/"

# d:\data\imsi_mapping\mapped\dms\dmsreport.2017-09-27.XN.csv
# d:\data\imsi_mapping\mapped\lsr\lsrreport_daily.2017-10-16.csv

def getDate(dt = time.strftime("%Y-%m-%d", time.localtime())):
    return dt

def getPrevDate(dt):
    lt = time.strptime(dt, "%Y-%m-%d")
    lt = time.localtime(time.mktime(lt)- (60*60*24))
    return time.strftime("%Y-%m-%d", lt)

def getPrefMonth(dt):
    lt = time.strptime(dt, "%Y-%m-%d")
    lt = time.localtime(time.mktime(lt) - (8*60*60*24))
    return time.strftime("%Y-%m-%d", lt)

def getDmsFiles(dt):
    ret = {}
    base = dmsdir + "dmsreport." + dt
    ret["NN"] = base + ".NN.csv"
    ret["NE"] = base + ".NE.csv"
    ret["EN"] = base + ".EN.csv"
    ret["EE"] = base + ".EE.csv"
    return ret

def getLsrFile(dt):
    return lsrdir + "lsrreport_daily." + dt + ".csv"

def readDmsData(path, dmsprevdate):
    with open(path, 'rt') as fin:
        cin = csv.DictReader(fin)
        ret = {"Total" : 0, "IMSI": 0, "File Date": dmsprevdate}
        for row in cin:
            row = dict(row)
            if row["File Time"].startswith(dmsprevdate):
                ret["Total"] += int(row["Total"])
                ret["IMSI"] += int(row["IMSI(string)"])
            if row["File Time"] < dmsprevdate:
                break
        return ret

def readLsrData(path, lsrprevdate):
    with open(path, 'rt') as fin:
        cin = csv.DictReader(fin)
        for row in cin:
            row = dict(row)
            if lsrprevdate == row["File Date"]:
                ret = {}
                ret["Total"] = row["Total"]
                ret["IMEI Rate"] = row["NewIMEI Rate"]
                ret["IMSI Rate"] = row["Mapped Rate"]
                ret["File Date"] = row["File Date"]
                return ret


def getPrevReport(dt):
    prevdt = getPrevDate(dt)
    prevmon = getPrefMonth(dt)
    prevreportfile = lsrdir + "userreport_daily." + prevdt + ".csv"

    if not os.path.exists(prevreportfile):
        return []

    with open(prevreportfile, 'rt') as fin:
        cin = csv.reader(fin)
        rows = [row for row in cin]
        rows = rows[1:]
        rows = [row for row in rows if prevmon <= row[0] < dt]
        return rows

def getDmsRow(title, src, note = ""):
    row = []
    row.append(src["File Date"])
    row.append(title)
    row.append(src["Total"])
    row.append(src["IMSI"])
    row.append("%.2f%%"%(float(src["IMSI"]*100)/src["Total"])) # force float division in python 2.x
    row.append(note)
    return row

def getNewReport(dmsinfos, lsrinfo):
    # dms field = Total, IMSI, File Date
    # rows:
    # sum of EE
    # sum of EN
    # sum of total ERT eNB  // used in LSR IMSI matching
    # sum of NE
    # sum of NN
    # sum of total NSN eNB
    # total E + N           // used in DMS output reports
    # sum of total ERT MME
    # sum of total NSN MME
    ret = []
    ret.append(getDmsRow("session amount of EE", dmsinfos["EE"]))
    ret.append(getDmsRow("session amount of EN", dmsinfos["EN"]))
    total_e_enb = {
        "Total" : dmsinfos["EE"]["Total"] + dmsinfos["EN"]["Total"],
        "IMSI" : dmsinfos["EE"]["IMSI"] + dmsinfos["EN"]["IMSI"],
        "File Date" : dmsinfos["EE"]["File Date"]
    }
    ret.append(getDmsRow("sum of total ERT eNB", total_e_enb, "used in LSR IMSI matching"))

    ret.append(getDmsRow("session amount of NE", dmsinfos["NE"]))
    ret.append(getDmsRow("session amount of NN", dmsinfos["NN"]))
    total_n_enb = {
        "Total" : dmsinfos["NE"]["Total"] + dmsinfos["NN"]["Total"],
        "IMSI" : dmsinfos["NE"]["IMSI"] + dmsinfos["NN"]["IMSI"],
        "File Date" : dmsinfos["NN"]["File Date"]
    }
    ret.append(getDmsRow("sum of total NSN eNB", total_n_enb))

    total_e_n = {
        "Total" : total_e_enb["Total"] + total_n_enb["Total"],
        "IMSI" : total_e_enb["IMSI"] + total_n_enb["IMSI"],
        "File Date" : total_e_enb["File Date"]
    }
    ret.append(getDmsRow("total E + N", total_e_n, "used in DMS output reports"))

    total_e_mme = {
        "Total" : dmsinfos["NE"]["Total"] + dmsinfos["EE"]["Total"],
        "IMSI" : dmsinfos["NE"]["IMSI"] + dmsinfos["EE"]["IMSI"],
        "File Date" : dmsinfos["NE"]["File Date"]
    }
    ret.append(getDmsRow("sum of total ERT MME", total_e_mme))

    total_n_mme = {
        "Total" : dmsinfos["EN"]["Total"] + dmsinfos["NN"]["Total"],
        "IMSI" : dmsinfos["EN"]["IMSI"] + dmsinfos["NN"]["IMSI"],
        "File Date" : dmsinfos["NN"]["File Date"]
    }
    ret.append(getDmsRow("sum of total NSN MME", total_n_mme))

    # lsr field = Total, IMEI Rate, IMSI Rate, File Date
    # rows:
    # IMSI mapping rate
    # IMEI mapping rate
    ret.append([lsrinfo["File Date"], "IMSI mapping rate form R930", "", "", lsrinfo["IMSI Rate"], ""])
    ret.append([lsrinfo["File Date"], "IMEI mapping rate form R930", "", "", lsrinfo["IMEI Rate"], ""])
    return ret

def genReport(dt, dmsinfos, lsrinfo):
    header = ["Date", "eNB--MME", "total record", "total IMSI", "IMSI rate", "Note"]
    reportfile = lsrdir + "userreport_daily." + dt + ".csv"
    olddata = getPrevReport(dt)
    newdata = getNewReport(dmsinfos, lsrinfo)

    #with open(reportfile, 'wt', newline='') as fout:
    with open(reportfile, 'wt') as fout:
        csvout = csv.writer(fout)
        csvout.writerow(header)
        csvout.writerows(olddata)
        csvout.writerows(newdata)

def genDailyReport(dt):
    "gen report for previous day's data"
    prevdt = getPrevDate(dt)
    dmsfiles = getDmsFiles(dt)
    dmsinfos = {}
    for k, v in dmsfiles.items():
        if not os.path.exists(v):
            print ("source file '%s' not found"%(v))
            return False
        dmsinfo = readDmsData(v, prevdt)
        dmsinfos[k] = dmsinfo

    lsrfile = getLsrFile(dt)
    if not os.path.exists(lsrfile):
        return False
    lsrinfo = readLsrData(lsrfile, prevdt)
    genReport(dt, dmsinfos, lsrinfo)
    return True


if __name__ == "__main__":
    dtl = []
    if len(sys.argv) < 2:
        dtl.append(getDate())
    else:
        dt = getDate()
        dtl.append(dt)
        i = 1
        while i < int(sys.argv[1]):
            dt = getPrevDate(dt)
            dtl.insert(0, dt)
            i += 1

    for dt in dtl:
        genDailyReport(dt)


