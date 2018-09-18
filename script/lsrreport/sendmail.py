# This Python file uses the following encoding: utf-8
"""
正常信件:
Subject = ISMI Mapping Report Mail
Content = Attached please find IMSI Mapping report file.
          IMSI rate (82.33%)

低 IMSI rate (<70%) 信件:
Subject = ISMI Mapping Report Mail ** LOW IMSI RATE **
Content = Attached please find IMSI Mapping report file.
          Warning: IMSI rate (68.33%) is LOW

2017-12-16:
* 信件內容加註說明
* 取

"""
import smtplib
import socket
import mimetypes
from email.mime.multipart import MIMEMultipart
from email import encoders
from email.message import Message
from email.mime.audio import MIMEAudio
from email.mime.base import MIMEBase
from email.mime.image import MIMEImage
from email.mime.text import MIMEText
import time
from datetime import date, timedelta

import csv

def getMsg(sender, receiver, subject, content, attach):
    msg = MIMEMultipart()
    msg["From"] = sender
    msg["To"] = receiver
    msg["Subject"] = subject
    msg.preamble = content
    # msg.preamble = content

    text = MIMEText(content, 'plain', 'utf8')
    msg.attach(text)

    ctype, encoding = mimetypes.guess_type(attach)

    if ctype is None or encoding is not None:
        ctype = "application/octet-stream"

    if attach.endswith(".csv"):    # python 2.7 wrong guess
        ctype = "application/vnd.ms-excel"

    maintype, subtype = ctype.split("/", 1)

    if maintype == "text":
        fp = open(attach, 'rb')
        # Note: we should handle calculating the charset
        part = MIMEText(fp.read(), _subtype=subtype)
        fp.close()
    elif maintype == "image":
        fp = open(attach, "rb")
        part = MIMEImage(fp.read(), _subtype=subtype)
        fp.close()
    elif maintype == "audio":
        fp = open(attach, "rb")
        part = MIMEAudio(fp.read(), _subtype=subtype)
        fp.close()
    else:
        fp = open(attach, "rb")
        part = MIMEBase(maintype, subtype)
        part.set_payload(fp.read())
        fp.close()

    part.add_header("Content-Disposition", "attachment", filename=attach)
    encoders.encode_base64(part)
    msg.attach(part)
    return msg

def sendMail(subject, content, attach, flag):
    fromaddr = "noc-pmats@aptg.com.tw"
    #toaddrs  = ["maxhuang@aptg.com.tw"]
    toaddrs  = "maxhuang@aptg.com.tw" #for python 2.x compability
    #from = noc-pmats@aptg.com.tw
    #name = 效能分析暨技術支援組
    #黃志煜 <maxhuang@aptg.com.tw>

    # msg = "Subject: Hello\n\nThis is the body of the message."

    print ("Sending mail:")
    print ("Subject:", subject)
    print ("Attctch:", attach)
    print ("Content:\n" + content)

    if not flag:
        return

    try:
        server = smtplib.SMTP('192.168.99.147', 25)
        # result = server.sendmail(fromaddr, toaddrs, msg)
        msg = getMsg(fromaddr, toaddrs, subject, content, attach)
        result = server.sendmail(fromaddr, toaddrs, msg.as_string())
        server.quit()
        if result:
            for r in result.keys():
                print ("Error sending to", r )
                rt = result[r]
                print ("Code", rt[0], ":", rt[1])
    except (smtplib.SMTPException, socket.error) as arg:
        print ("SMTP Server could not send mail", arg)

def sendAlert():
    subject = "ISMI Mapping Alert Mail"
    content = "ISMI Mapping rate for last hour is less than 70%.\nAttached please find IMSI Mapping alert file."
    lsrdir = "/data/imsi_mapping/mapped/lsr/"
    attach = lsrdir + "imsi_rate.txt"
    sendMail(subject, content, attach, False);

def sendReport():
    subject = "ISMI Mapping Report Mail"
    content = "Attached please find IMSI Mapping report file."
    today = time.strftime("%Y-%m-%d", time.localtime())

    lsrdir = "/data/imsi_mapping/mapped/lsr/"
    attach = lsrdir + "userreport_daily.%s.csv" % (today)
    imsi_rate = getYesterdayImsiRate(attach)
    print ("imsi_rate=", imsi_rate)
    if (imsi_rate < 70):
        subject += " ** LOW IMSI RATE **"
        content += "\nWarning: IMSI rate (%.2f%%) is LOW"% imsi_rate
    else:
        content += "\nIMSI rate (%.2f%%)"% imsi_rate

    content += """
Attachment file field description
-----------------------------------
session amount of X/Y:
X=eNB
Y=MME
N=Nokia
E=Ericsson
"""
    sendMail(subject, content, attach, True)

def getYesterdayImsiRate(path):
    ret = 0
    yesterday = (date.today() - timedelta(1)).strftime("%Y-%m-%d")
    with open(path, 'rt') as fin:
        cin = csv.DictReader(fin)
        for row in cin:
            row = dict(row)
            datef = row["Date"]
            name = row["eNB--MME"]
            imsi_rate = row["IMSI rate"]
            if ("IMSI mapping rate form R930" == name and datef == yesterday):
                ret = float(imsi_rate.strip("%"))
                break
    return ret

if __name__ == "__main__":
    sendReport()
    print ("done!")
