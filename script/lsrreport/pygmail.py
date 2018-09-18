# Import smtplib for the actual sending function

import smtplib
import sendmail
from email.mime.base import MIMEBase
# Import the email modules we'll need

from email.mime.text import MIMEText

textfile = 'email_message.txt'
smtp_server = 'smtp.gmail.com'
sender = 'Edward Hsieh<edwardsayer@gmail.com>'
receiver = 'Moze Edward<edwardsayer@moze.tw>'
account = 'edwardsayer@gmail.com'
password = "jadmocnuparxswqi"
subject = "A mail with attach"
content = "Please see attach"
attach = "test.csv"


msg = sendmail.getMsg(sender, receiver, subject, content, attach)

s = smtplib.SMTP('smtp.gmail.com', 587)
s.ehlo()
s.starttls()
s.ehlo()
s.login(account, password)
s.sendmail(sender, receiver, msg.as_string())
s.quit()