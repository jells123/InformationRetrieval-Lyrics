import urllib.request
from HTMLParser import SongHTMLParser,ArtistHTMLParser,GenreHTMLParser
from Song import Song
class SongExtracter:
    def __init__(self):
        self.apiURL = "http://lyrics.wikia.com/wiki/"
        self.randSongURL = "Special:RandomInCategory/Song"

    def getSong(self):
        contents = urllib.request.urlopen(self.apiURL + self.randSongURL).read()
        decoded = contents.decode("utf-8")
        parser = SongHTMLParser(decoded)
        if(parser.isEnglish()==False):
            return None
        lyric = parser.getLyrics()
        artist,songTitle = parser.getTitleAndArtist()
        album = parser.getAlbum()
        genre = self.getGenreInfo(album)
        print(genre)
        print('Country' not in genre)
        if 'Country' not in genre:
            return None
        city="";province="";country=""
        artistInfo = self.getArtitstInfo(artist)
        if (artistInfo is not None):
            city = artistInfo[2].replace("_"," ")
            province = artistInfo[1].replace("_"," ")
            country = artistInfo[0].replace("_"," ")
        return Song(songTitle,artist,lyric,city,province,country, genre)
    def getArtitstInfo(self,artist):
        artist = str(artist).replace(" ","_")
        contents = urllib.request.urlopen(self.apiURL +artist).read()
        decoded = contents.decode("utf-8")
        parser = ArtistHTMLParser(decoded)
        return parser.getHomeTown()
    def getGenreInfo(self,album):
        contents = urllib.request.urlopen(self.apiURL +album).read()
        decoded = contents.decode("utf-8")
        parser = GenreHTMLParser(decoded)
        return parser.getGenre()