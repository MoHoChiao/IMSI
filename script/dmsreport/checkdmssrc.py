# This Python file uses the following encoding: utf-8
"""
DMS 資料來源斷線告警:
Subject = IMSI Mapping: DMS Source Disconnected Warning
Content = As title!!!
"""
import smtplib
import socket
from email.mime.multipart import MIMEMultipart
import time
import sys, os


def getMsg(sender, receiver, subject, content):
    msg = MIMEMultipart()
    msg["From"] = sender
    msg["To"] = receiver
    msg["Subject"] = subject
    msg.preamble = content
    # msg.preamble = content

    text = MIMEText(content, 'plain', 'utf8')
    msg.attach(text)
    return msg

def sendMail(subject, content, flag):
    fromaddr = "noc-pmats@aptg.com.tw"
    #toaddrs  = ["maxhuang@aptg.com.tw"]
    #toaddrs  = "maxhuang@aptg.com.tw" #for python 2.x compability
    toaddrs  = fromaddr
    #from = noc-pmats@aptg.com.tw
    #name = 效能分析暨技術支援組
    #黃志煜 <maxhuang@aptg.com.tw>

    # msg = "Subject: Hello\n\nThis is the body of the message."

    print ("Sending mail:")
    print ("Subject:", subject)
    print ("Content:\n" + content)

    if not flag:
        return

    try:
        server = smtplib.SMTP('192.168.99.147', 25)
        # result = server.sendmail(fromaddr, toaddrs, msg)
        msg = getMsg(fromaddr, toaddrs, subject, content)
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
    subject = "IMSI Mapping: DMS Source Disconnected Warning"
    content = "As title!!!"
    sendMail(subject, content, True);

def checkDirUpdateIn(path, minutes):
    files = [os.path.join(dp, f) for dp, dn, filenames in os.walk(path) for f in filenames]
    now = time.time() # value is floating point of seconds
    for file in files:
        ft = os.path.getmtime(file) # value is floating point of seconds
        if (now-ft)/60 <= minutes:
            return True
    return False


if __name__ == "__main__":
    args = sys.argv
    if (len(args) < 3):
        print("usage: checkdmssrc path minutes")
        quit()

    dir = args[1]
    minutes = int(args[2])
    if checkDirUpdateIn(dir, minutes):
        print("Got new source files")
    else:
        print("No new file, send warning mail.")
        sendAlert()
