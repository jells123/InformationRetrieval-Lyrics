from SongExtracter import SongExtracter
import json
from sys import stdout
songCount =10
songs =[]
se = SongExtracter()
ctr=0
while(len(songs)!=songCount):
    try:
        song = se.getSong()
    except:
        song=None
    if(song is not None):
        songs.append(song.__dict__)
        ctr+=1
        stdout.write("\rDone %d/%d" % (ctr  ,songCount))
        stdout.flush()

jsonSting = json.dumps(songs)

with open("songs/songfile.txt",'w+') as f:
    f.write(jsonSting)
