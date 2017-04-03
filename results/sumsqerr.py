import sys
from os import listdir

def sumerr(fname1, ftype1, fname2, ftype2):
    with open(fname1) as f1:
      with open(fname2) as f2:
        lines1 = f1.readlines()
        csv1 = [[s.strip() for s in l.split(',')] for l in lines1]
        lines2 = f2.readlines()
        csv2 = [[s.strip() for s in l.split(',')] for l in lines2]
        if ftype1 == "raw":
            csv1lat = 3
            csv1lng = 4
        else:
            csv1lat = 1
            csv1lng = 2

        if ftype2 == "raw":
            csv2lat = 3
            csv2lng = 4
        else:
            csv2lat = 1
            csv2lng = 2

        sumsqerr = 0.0
        for i in range(1,len(csv1)):
            laterr = float(csv1[i][csv1lat]) - float(csv2[i][csv2lat])
            lngerr = float(csv1[i][csv1lng]) - float(csv2[i][csv2lng])
            sumsqerr += laterr*laterr + lngerr*lngerr
        return sumsqerr

err = 0.0
for f in listdir(sys.argv[1]):
    ferr = sumerr(sys.argv[1]+"/"+f, sys.argv[2], sys.argv[3]+"/"+f, sys.argv[4])
    print "Error due to " + f + ": " + str(ferr)
    err += ferr
print "Sum Squared Error: " + str(err)

