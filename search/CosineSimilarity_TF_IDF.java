package search;

import engine.SongDocument;

import java.util.ArrayList;
import java.util.Collections;

public class CosineSimilarity_TF_IDF
{
    private ArrayList <SongDocument> _documents;

    public CosineSimilarity_TF_IDF(ArrayList <SongDocument> documents)
    {
        this._documents = documents;
    }

    public ArrayList <Score> getSortedDocuments(SongDocument query)
    {
        return getSortedDocuments(query._tf_idf_representation);
    }

    public ArrayList <Score> getSortedDocuments(double[] queryVector)
    {
        ArrayList <Score> scores = new ArrayList <>(_documents.size());

        for (SongDocument doc : _documents) {
            double[] tf_idf = doc._tf_idf_representation;

            double dotProd = 0.0;
            double normA = 0.0;
            double normB = 0.0;
            for (int i = 0; i < tf_idf.length; i++) {
                dotProd += tf_idf[i] * queryVector[i];
                normA += Math.pow(tf_idf[i], 2);
                normB += Math.pow(queryVector[i], 2);
            }
            scores.add(new Score(doc, dotProd / (Math.sqrt(normA) * Math.sqrt(normB))));
        }

        Collections.sort(scores);
        return scores;
    }

    public String getName()
    {
        return "Cosine similarity";
    }

    public static void getCosineSimilarity() {
        ;
    }
}
