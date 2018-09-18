import os
import time
path = "."
# files = [x for x in os.walk(path)]
files = [os.path.join(dp, f) for dp, dn, filenames in os.walk(path) for f in filenames]
now = time.time() # value is floating point of seconds
print (now)
for file in files:
    ft = os.path.getmtime(file) # value is floating point of seconds
    print (file, ft , now-ft)
