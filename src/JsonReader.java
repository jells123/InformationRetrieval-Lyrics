import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class JsonReader {

    private JSONArray jsonSongs;
    private ArrayList<Song> songs;
    private static JsonReader instance = null;

    private JsonReader() {
        songs = new ArrayList<>();
    }

    public static JsonReader getInstance() {
        if(instance == null)
            instance = new JsonReader();
        return instance;
    }

    public void loadData(String file) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(file));
            jsonSongs = (JSONArray) obj;
            parseSongs();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void parseSongs() {
        for (Object song : jsonSongs) {
            JSONObject jsonSong = (JSONObject) song;
            songs.add(new Song(jsonSong.get("title").toString(),jsonSong.get("artist").toString(),
                    jsonSong.get("lyric").toString(), jsonSong.get("country").toString(),
                    jsonSong.get("province").toString(), jsonSong.get("city").toString()));
        }
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }
}
