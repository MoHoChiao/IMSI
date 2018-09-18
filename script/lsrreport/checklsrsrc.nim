import os, re, times, strutils
import smtp, net

# add d:ssl to comiler option if useSsl = true
proc sendmail() =
    let smtpConn = newSmtp(useSsl = false, debug=true)
    smtpConn.connect("192.168.99.147", Port 25)
    # smtpConn.auth("edwardsayer", "jadmocnuparxswqi")
    let fromaddr = "noc-pmats@aptg.com.tw"
    #toaddrs  = ["maxhuang@aptg.com.tw"]
    let toaddrs  = @["maxhuang@aptg.com.tw"]
    var msg = createMessage("IMSI Mapping: LSR Source Disconnected Warning",
        "As title!!!", toaddrs)
    smtpConn.sendmail(fromaddr, toaddrs, $msg)
    smtpConn.close()

proc checkDirUpdateIn(dir: string, minutes: int): bool =
    let n = getTime()
    for file in walkDirRec dir:
        # 不用判斷是檔案還是目錄，只會傳回目錄
        # echo if fileExists(file): "file" else: "dir"
        # if file.match re".*\..*":
        # let ft = getLastModificationTime(file)
        # echo file, " ", ft.getLocalTime.format("yyyy-MM-dd HH:mm:ss"), " ", n - ft
        let ft = getLastModificationTime(file)
        let diffminutes = (n.toUnix - ft.toUnix) div 60
        if diffminutes <= minutes:
            return true
    return false

if isMainModule:
    let args = commandLineParams()
    if (args.len < 2):
        echo "usage: checklsrsrc path minutes"
        quit()

    let dir = args[0]
    let minutes = parseInt(args[1])
    if checkDirUpdateIn(dir, minutes):
        echo "Got new source files"
    else:
        echo "No new file, send warning mail."
        sendmail()


