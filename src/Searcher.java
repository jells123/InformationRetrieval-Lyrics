import engine.SongDocument;
import opennlp.tools.langdetect.Language;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import search.QueryExpansion;
import search.Score;
import search.WordNet;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Searcher {

    private final static String conjunction = "AND";

    public static void main(String args[]) {
        String query = "lovely cake for breakfast";
        System.out.println("Query: " + query);

        String synonyms = getSynonyms(query, 3);
        query = expandWithSynonyms(query, 3);
        System.out.println("Add synonyms: " + query);

        try {
            Indexer indexer = new Indexer();
            ArrayList<Document> documents = indexer.getJsonDocuments();
            ArrayList<SongDocument> songDocuments = new ArrayList<SongDocument>();
            for (Document doc : documents) {
                songDocuments.add(new SongDocument(
                        doc.get(Constants.songname),
                        doc.get(Constants.lyrics),
                        doc.get(Constants.songartist),
                        doc.get(Constants.country),
                        Integer.parseInt(doc.get(Constants.id))
                ));
            }
            QueryExpansion qExp = new QueryExpansion(songDocuments);
            query = qExp.expandQuery(query);
            query = expandWithSynonyms(query, 2);
            for (String s : synonyms.split("\\s+")) {
                if (!query.contains(s)) {
                    query += s + " ";
                }
            }
            System.out.println("QueryExp + another synonyms: " + query);
            searchResultsForQuery(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        ArrayList<Score> scores =
    }

    private static String expandWithSynonyms(String query, int synonymsCount) {
        WordNet wordNet = new WordNet();
        HashMap<String, HashSet<String>> words = wordNet.getSimilarWords(query);

        int counter = 0;
        StringBuilder sb = new StringBuilder();
        HashSet<String> wordsSet = new HashSet<>();

        for (String substring : query.split("\\s+")) {
            wordsSet.add(substring);
        }

        for (Map.Entry<String, HashSet<String>> entry : words.entrySet()) {
            String keyword = entry.getKey();
            wordsSet.add(keyword);
//            sb.append("(\"").append(keyword).append("\" OR \"");

            int tmpSize = wordsSet.size();
            HashSet<String> synonyms = entry.getValue();
            for (String s : synonyms) {
                for (String substring : s.split("\\s+")) {
                    if (wordsSet.size() < tmpSize + synonymsCount) {
                        wordsSet.add(substring);
//                        sb.append(s).append("\" OR \"");
                    }
                }
            }
//            sb.delete(sb.toString().length() - 6, sb.toString().length() - 1);
//            sb.append(") " + conjunction + " ");
        }
//        sb.delete(sb.toString().length() - 4, sb.toString().length() - 1);

        for (String s : wordsSet) {
            sb.append(s).append(" ");
        }
        return sb.toString();
//        return wordsSet;
    }

    private static String getSynonyms(String query, int synonymsCount) {
        WordNet wordNet = new WordNet();
        HashMap<String, HashSet<String>> words = wordNet.getSimilarWords(query);

        int counter = 0;
        StringBuilder sb = new StringBuilder();
        HashSet<String> wordsSet = new HashSet<>();

        for (Map.Entry<String, HashSet<String>> entry : words.entrySet()) {
            String keyword = entry.getKey();

            int tmpSize = wordsSet.size();
            HashSet<String> synonyms = entry.getValue();
            for (String s : synonyms) {
                for (String substring : s.split("\\s+")) {
                    if (wordsSet.size() < tmpSize + synonymsCount) {
                        wordsSet.add(substring);
                    }
                }
            }
        }

        for (String s : wordsSet) {
            sb.append(s).append(" ");
        }
        return sb.toString();
    }

    private static String expandWithSynonymsGetQuery(String query, int synonymsCount) {
        WordNet wordNet = new WordNet();
        HashMap<String, HashSet<String>> words = wordNet.getSimilarWords(query);

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, HashSet<String>> entry : words.entrySet()) {
            String keyword = entry.getKey();
            HashSet<String> synonyms = entry.getValue();
            sb.append("(\"").append(keyword).append("\" OR \"");

            for (String s : synonyms) {
                sb.append(s).append("\" OR \"");
            }
            sb.delete(sb.toString().length() - 6, sb.toString().length() - 1);
            sb.append(") " + conjunction + " ");
        }
        sb.delete(sb.toString().length() - 4, sb.toString().length() - 1);

        return sb.toString();
    }

    private static void searchResultsForQuery(String query) {
        MyLanguageDetector.initModel();
        Language[] predictLanguages = MyLanguageDetector.get_languageDetector().predictLanguages(query);
        String language = predictLanguages[0].getLang();

        String selectedQuery = query;
        Analyzer analyzer = new StandardAnalyzer();

        IndexReader reader = getIndexReader();
        assert reader != null;
        IndexSearcher indexSearcher = new IndexSearcher(reader);

        if(language.equals("eng") || query.split(" ").length == 1) {
//            selectedQuery = expandWithSynonyms(query, 3);
            selectedQuery = selectedQuery;
        } else {
            System.out.println("It's not a query in English!\nTrying to find results without synonyms.");
        }

        System.out.println("Query parser = " + selectedQuery);

        try {
            QueryParser qp = new QueryParser(Constants.lyrics, analyzer);
            Query q = qp.parse(selectedQuery);
            printResultsForQuery(indexSearcher, q, language, query);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printResultsForQuery(IndexSearcher indexSearcher, Query q, String language, String original) {
        try {
            TopDocs topDocs = indexSearcher.search(q, Constants.top_docs);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = indexSearcher.doc(sd.doc);
                if (!doc.get(Constants.language).equals(language) && original.split(" ").length > 1) {
                    System.err.println("Languages are not the same!");
                    System.err.println("Got " + language + " and " + doc.get(Constants.language));
                }
                System.out.format("%f: %s (Id=%s) (Artist=%s) (Location=%s, %s, %s) (Size=%sb)\n",
                        sd.score, doc.get(Constants.songname), doc.get(Constants.id),
                        doc.get(Constants.songartist), doc.get(Constants.country), doc.get(Constants.province),
                        doc.get(Constants.city), doc.get(Constants.songsize));
            }
            if(topDocs.scoreDocs.length == 0) System.err.println("Couldn't find anything relevant, sorry :(");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static IndexReader getIndexReader() {
        try {
            Directory dir = FSDirectory.open(Paths.get(Constants.index_dir));
            return DirectoryReader.open(dir);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
