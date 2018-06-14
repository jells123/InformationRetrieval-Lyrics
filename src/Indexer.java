import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Indexer {
    public static void main(String args[]) {
        Indexer indexer = new Indexer();
        indexer.indexDocuments();
    }

    private void indexDocuments() {
        try {
            FileUtils.deleteDirectory(new File(Constants.index_dir));
        } catch (IOException ignored) {
        }
        ArrayList<Document> documents = getJsonDocuments();

        StandardAnalyzer std = new StandardAnalyzer();
        IndexWriterConfig idxWriteConf = new IndexWriterConfig(std);
        Path p = Paths.get(Constants.index_dir);
        try {
            IndexWriter idxWriter = new IndexWriter(FSDirectory.open(p), idxWriteConf);
            idxWriter.addDocuments(documents);
            idxWriter.commit();
            idxWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Document> getJsonDocuments() {
        JsonReader.getInstance().loadData("songfile.json");
        ArrayList<Song> songs = JsonReader.getInstance().getSongs();

        ArrayList<Document> documents = new ArrayList<>(songs.size());
        for (int id = 0; id < songs.size(); id++) {
            System.out.println("Loading " + songs.get(id).getTitle());
            documents.add(getJsonDocument(songs.get(id), id));
        }
        return documents;
    }

    private Document getJsonDocument(Song song, int id) {
        Document document = new Document();
        StoredField idField = new StoredField(Constants.id, id);

        String content = song.getLyrics();
        TextField contentField = new TextField(Constants.lyrics, content, Field.Store.NO);
        TextField fnameField = new TextField(Constants.songname, song.getTitle(), Field.Store.YES);
        TextField fartistField = new TextField(Constants.songartist, song.getArtist(), Field.Store.YES);
        TextField fcountryField = new TextField(Constants.country, song.getCountry(), Field.Store.YES);
        TextField fprovinceField = new TextField(Constants.province, song.getProvince(), Field.Store.YES);
        TextField fcityField = new TextField(Constants.city, song.getCity(), Field.Store.YES);
        Field fileSizeField_int = new IntPoint(Constants.songsize_int, (int) content.length());
        Field fileSizeField = new StoredField(Constants.songsize, (int) content.length());

        document.add(idField);
        document.add(contentField);
        document.add(fnameField);
        document.add(fartistField);
        document.add(fcountryField);
        document.add(fprovinceField);
        document.add(fcityField);
        document.add(fileSizeField_int);
        document.add(fileSizeField);

        return document;
    }

}
