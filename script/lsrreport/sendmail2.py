# This Python file uses the following encoding: utf-8
import smtplib
import socket
import mimetypes
from email.mime.multipart import MIMEMultipart
from email import encoders
from email import Encoders
from email.message import Message
from email.mime.audio import MIMEAudio
from email.mime.base import MIMEBase
from email.mime.image import MIMEImage
from email.mime.text import MIMEText
import time

def getMsg(sender, receiver, subject, content, attach):
    msg = MIMEMultipart()
    msg["From"] = sender
    msg["To"] = receiver
    msg["Subject"] = subject
    # msg.preamble = content
 
    text = MIMEText(content, 'plain', 'utf8')
    msg.attach(text)

    if attach:
        with open(attach, "rb") as f:
            part = MIMEBase('application', 'csv')
            part.set_payload(f.read())
            part.add_header('Content-Disposition', 'attachment', filename=attach)
            Encoders.encode_base64(part)
            msg.attach(part)

    return msg

def sendMail(subject, content, attach):
    fromaddr = "noc-pmats@aptg.com.tw"
    #toaddrs  = ["maxhuang@aptg.com.tw"]
    toaddrs  = "maxhuang@aptg.com.tw" #for python 2.x compability
    #from = noc-pmats@aptg.com.tw
    #name = 效能分析暨技術支援組
    #黃志煜 <maxhuang@aptg.com.tw>

    # msg = "Subject: Hello\n\nThis is the body of the message."

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
        print ("Done sent mail!")
    except (smtplib.SMTPException, socket.error) as arg:
        print ("SMTP Server could not send mail", arg)

def sendAlert():
    subject = "ISMI Mapping Alert Mail"
    content = "ISMI Mapping rate for last hour is less than 70%.\nAttached please find IMSI Mapping alert file."
    lsrdir = "/data/imsi_mapping/mapped/lsr/"
    attach = lsrdir + "imsi_rate.txt"
    sendMail(subject, content, attach)

def sendReport():
    subject = "ISMI Mapping Report Mail"
    content = "Attached please find IMSI Mapping report file."
    today = time.strftime("%Y-%m-%d", time.localtime())

    lsrdir = "/data/imsi_mapping/mapped/lsr/"
    attach = lsrdir + "userreport_daily.%s.csv" % (today)
    sendMail(subject, content, attach)

def sendTestMail():
    subject = "Test mail from ISMI Mapping Server"
    content = "This is a test mail from ISMI Mapping Server."
    attach = None
    sendMail(subject, content, attach)

if __name__ == "__main__":
    #sendReport()
    sendTestMail()
    print ("done!")
