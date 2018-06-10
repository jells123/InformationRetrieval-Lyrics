import json
with open("songs/songfile.txt") as f:
    print(type(json.load(f)[0]))