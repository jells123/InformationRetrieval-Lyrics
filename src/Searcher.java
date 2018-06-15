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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Searcher {

    private final static String conjunction = "AND";

    public static void main(String args[]) {
        //searchResultsForQuery("Wir sind nett Leute");
        searchResultsForQuery("chamber");
    }

    private static String expandWithSynonyms(String query) {
        WordNet wordNet = new WordNet();
        HashMap<String, ArrayList<String>> words = wordNet.getSimilarWords(query);

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, ArrayList<String>> entry : words.entrySet()) {
            String keyword = entry.getKey();
            ArrayList<String> synonyms = entry.getValue();
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

        if(language.equals("eng")) {
            selectedQuery = expandWithSynonyms(query);
        } else {
            System.out.println("It's not a query in English!\nTrying to find results without synonyms.");
        }

        System.out.println("Query parser = " + selectedQuery);

        try {
            QueryParser qp = new QueryParser(Constants.lyrics, analyzer);
            Query q = qp.parse(selectedQuery);
            printResultsForQuery(indexSearcher, q, language);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printResultsForQuery(IndexSearcher indexSearcher, Query q, String language) {
        try {
            TopDocs topDocs = indexSearcher.search(q, Constants.top_docs);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = indexSearcher.doc(sd.doc);
                if (!doc.get(Constants.language).equals(language)) {
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
