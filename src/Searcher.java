import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {

    public static void main(String args[]) {
        IndexReader reader = getIndexReader();
        assert reader != null;
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        String queryMammal = "motherfucker";                // XD
        TermQuery tq1;
        {
            System.out.println("1) term query: motherfucker (CONTENT)");

            String queryMammalNorm = analyzer.normalize("fieldName?", queryMammal).utf8ToString();
            Term term = new Term(Constants.lyrics, queryMammalNorm);
            tq1 = new TermQuery(term);

            printResultsForQuery(indexSearcher, tq1);
        }

        String queryBird = "Charlie";
        queryMammal = "Mack";

        TermQuery tq2;
        {
            System.out.println("2) term query Charlie Mack (CONTENT)");

            String queryMammalNorm = analyzer.normalize("fieldName?", queryMammal).utf8ToString();
            Term term1 = new Term(Constants.lyrics, queryMammalNorm);
            tq1 = new TermQuery(term1);

            String queryBirdNorm = analyzer.normalize("fieldName?", queryBird).utf8ToString();
            Term term2 = new Term(Constants.lyrics, queryBirdNorm);
            tq2 = new TermQuery(term2);

            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            bq.add(tq1, BooleanClause.Occur.SHOULD);
            bq.add(tq2, BooleanClause.Occur.SHOULD);
            bq.setMinimumNumberShouldMatch(1);

            printResultsForQuery(indexSearcher, bq.build());
        }

        {
            System.out.println("7) Fuzzy querry (CONTENT): gasolin?");
            Term fuzzy = new Term(Constants.lyrics, "gasolin");
            FuzzyQuery fq = new FuzzyQuery(fuzzy);
            printResultsForQuery(indexSearcher, fq);
        }

        String queryP1 = "(\"fuck you\"~10) OR bat";
        String queryP2 = "(\"God bless your soul\"~10) OR (\"are loved\"~10)";

        String selectedQuery = queryP2;
        {
            System.out.println("8) query parser = " + selectedQuery);
            try {
                QueryParser qp = new QueryParser(Constants.lyrics, analyzer);
                Query q = qp.parse(selectedQuery);
                printResultsForQuery(indexSearcher, q);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printResultsForQuery(IndexSearcher indexSearcher, Query q) {
        try {
            TopDocs topDocs = indexSearcher.search(q, Constants.top_docs);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = indexSearcher.doc(sd.doc);
                System.out.format("%f: %s (Id=%s) (Artist=%s) (Location=%s, %s, %s) (Size=%sb)\n",
                        sd.score, doc.get(Constants.songname), doc.get(Constants.id),
                        doc.get(Constants.songartist),doc.get(Constants.country),doc.get(Constants.province),
                        doc.get(Constants.city),doc.get(Constants.songsize));
            }
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
