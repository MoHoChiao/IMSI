"""
generate user report file
python 2.x compatible
"""

import time
import csv
import os
import sendmail

lsrdir = "/data/imsi_mapping_cluster_v1/mapped/lsr/"

# d:\data\imsi_mapping\mapped\lsr\lsrreport.2017-11-07.csv

def getDate(dt = time.strftime("%Y-%m-%d", time.localtime())):
    return dt

def getLsrFile(dt):
    return lsrdir + "lsrreport." + dt + ".csv"

def readLsrData(path):
    lasttime = None
    ret = []
    with open(path, 'rt') as fin:
        cin = csv.DictReader(fin)
        for row in cin:
            row = dict(row)
            filetime = row["File Time"]
            if lasttime == None:
                # time.strptime('2017-10-17 18:09:42', "%Y-%m-%d %H:%M:%S")
                lasttime = time.mktime(time.strptime(filetime, "%Y-%m-%d %H:%M:%S"))
            rowtime = time.mktime(time.strptime(filetime, "%Y-%m-%d %H:%M:%S"))
            if lasttime - rowtime > 60 * 60:
                break
            item = {}
            item["Total"] = int(row["Total"])
            item["IMSI"] = int(row["IMSI Mapped"])
            item["Time"] = filetime
            ret.append(item)
    return ret

def getlastimsirate(lsrinfo):
    sum_total = 0
    sum_imsi = 0
    for row in lsrinfo:
        # print(row)
        sum_total += row["Total"]
        sum_imsi += row["IMSI"]
    avg = float(sum_imsi)*100/sum_total # force float division in python 2.x
    return avg

def writeratefile(newline):
    ratefile = lsrdir + "imsi_rate.txt"
    lines = []
    if os.path.exists(ratefile):
        with open (ratefile, "rt") as fi:
            lines = fi.readlines()

    lines.append(newline)
    if (len(lines) > 1000):
        lines = lines[-1000:]
    with open (ratefile, "wt") as fo:
        fo.writelines(lines)
        fo.write("\n")

if __name__ == "__main__":
    lsrdate = getDate()
    lsrfile = getLsrFile(lsrdate)
    lsrinfo = readLsrData(lsrfile)
    imsirate = getlastimsirate(lsrinfo)
    now = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    newline = "%s imsirate = %.2f%%" % (now, imsirate)
    print(newline)
    writeratefile(newline)
    if (imsirate < 70):
        print("IMSI rate is less than 70%, send alert!")
        # sendmail.sendAlert()
        print ("done!")
