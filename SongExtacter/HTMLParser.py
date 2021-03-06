from bs4 import BeautifulSoup

class SongHTMLParser():
    def __init__(self,html):
        self.soup = BeautifulSoup(html,'html.parser')
        #print(self.soup.prettify())
    def getLyrics(self):
        lyricsDiv = self.soup.findAll("div",{"class": "lyricbox"})
        parts = list([str(x) for x in lyricsDiv[0].contents[0:-2]])
        converted = ['\n' if(x=="<br/>") else x  for x in parts]
        return "".join(converted)
    def getAlbum(self):
        albumDiv = self.soup.findAll("div",{"id" : "song-header-container"})
        album = str(albumDiv[0].contents[3]).split('<i>')[1].split('title="')[1].split('">')[0].replace(" ", "_")
        return album
    def getGenre(self):
        genreDiv = self.soup.findAll("div",{"class": "lyricbox"})

    def getTitleAndArtist(self):
        headerTitle = self.soup.findAll("h1",{"class" : "page-header__title"})
        header = headerTitle[0].contents[0]
        data =header.split(":") # data[0] - artist data data [1] song nane
        return data[0],data[1][:-7]
    def isEnglish(self):
        links = self.soup.findAll('a')
        for l in links:
            href = str(l.get('href'))
            hrefsParams = href.split('/')
            if ("Category:Language" in hrefsParams and len(hrefsParams)==4 and hrefsParams[-1]=="English"):
                return True
        return False

class GenreHTMLParser():
    def __init__(self, html):
        self.soup = BeautifulSoup(html, 'html.parser')

    def getGenre(self):
        lis = self.soup.findAll("li", {'class' : 'category normal'})
        for l in lis:
            possible_genre = str(l.get('data-name'))
            if possible_genre.find("Genre") == 0:
                return possible_genre.split('Genre/')[1]

        return ":("


class ArtistHTMLParser():
    def __init__(self,html):
        self.soup = BeautifulSoup(html,'html.parser')
    def getHomeTown(self):
        linksHome = self.soup.findAll('a')
        for l in linksHome:
            href = str(l.get('href'))
            hrefsParams = href.split('/')
            if("Category:Hometown" in hrefsParams and len(hrefsParams)==6):
                city = hrefsParams[-1]
                province = hrefsParams[-2]
                country = hrefsParams[-3]
                return country,province,city
