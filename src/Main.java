import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        JsonReader.getInstance().loadData("songfile.json");
        ArrayList<Song> songs = JsonReader.getInstance().getSongs();
        for(Song s : songs) {
            System.out.println(s.toString());
        }

    }
}
