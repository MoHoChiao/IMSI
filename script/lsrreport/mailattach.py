import smtplib
import mimetypes
from email.mime.multipart import MIMEMultipart
from email import encoders
from email.message import Message
from email.mime.audio import MIMEAudio
from email.mime.base import MIMEBase
from email.mime.image import MIMEImage
from email.mime.text import MIMEText

def getMsg(sender, receicer, subject, content, attach):
    msg = MIMEMultipart()
    msg["From"] = sender
    msg["To"] = receicer
    msg["Subject"] = subject
    # msg.preamble = content

    text = MIMEText(content, 'plain', 'utf8')
    msg.attach(text)

    ctype, encoding = mimetypes.guess_type(attach)
    if ctype is None or encoding is not None:
        ctype = "application/octet-stream"

    maintype, subtype = ctype.split("/", 1)

    if maintype == "text":
        fp = open(attach, "rb")
        # Note: we should handle calculating the charset
        part = MIMEText(fp.read(), _subtype=subtype, _charset="utf8")
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

    encoders.encode_base64(part)
    part.add_header("Content-Disposition", "attachment", filename=attach)
    msg.attach(part)
    return msg


emailfrom = "edwardsayer@gmail.com"
emailto = "edwardsayer@moze.tw"
fileToSend = "email_message.txt"
username = "edwardsayer@gmail.com"
password = "jadmocnuparxswqi"

server = smtplib.SMTP("smtp.gmail.com:587")
server.starttls()
server.login(username,password)
msg = getMsg(emailfrom, emailto, "test mail attachement", "bla bla", fileToSend)
server.sendmail(emailfrom, emailto, msg.as_string())
server.quit()