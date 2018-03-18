
import requests
import time


def FindNode(maclist):

    iplist=[]
    uplist=[]

    nodeiplist=[]
    oct1=100
    oct2=71
    oct3=190
    oct4=0

    for x in range(256-oct3):
        oct4 = 0
        while oct4<256:

            iplist.append(str(oct1)+"."+str(oct2)+"."+str(oct3+x)+"."+str(oct4))
            oct4+=1
        #iplist.append(str(oct1) + "." + str(oct2) + "." + str(oct3) + "." + str(oct4))

    timelist=[]




    for ip in iplist:
        print(ip)
    print("\n")

    print("Looking for",len(maclist),"node(s)", maclist)

    count=0
    for ip in iplist:


        if len(timelist)!=0:
            print(count,"out of",len(iplist), "ips to check approx max of",int(((len(iplist)-count)*(sum(timelist))/len(timelist))/60), "mins left","avg time is",sum(timelist)/len(timelist))
        print(ip)

        start = time.time()
        try:
            r = requests.get("http://"+ip,timeout=0.5)

            print("port 80 sucess")

            uplist.append(ip)
            for mac in maclist:
                if mac in r.text:
                    Nodeip=ip
                    print("\n\nFound NODE",mac,"@",ip)
                    nodeiplist.append((ip,mac))
                    if len(nodeiplist)==len(maclist):
                        return nodeiplist

        except:
            print("connection failed")
        end = time.time()
        count+=1
        elapsed=end - start
        print("time elapsed",elapsed)
        timelist.append(elapsed)

    print("other IPs with webserver on port 80")
    for ip in uplist:
        print(ip)



    return nodeiplist



maclist= ["A0:20:A6:02:78:06"]
nodeaddresses = FindNode(maclist)

with open('nodeips.txt', 'w') as fp:
    for node in nodeaddresses:
        fp.write(str(node[0])+"\n"+(str(node[1])))

