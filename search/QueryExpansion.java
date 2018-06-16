package search;

import engine.Dictionary;
import engine.SongDocument;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class QueryExpansion
{
    private RealMatrix _correlationMatrix;
    private double CORRELATION_THRESHOLD = 0.3d;

    private Dictionary _dictionary;
    private net.sf.extjwnl.dictionary.Dictionary _wordnetDictionary;
    private static final String DICT_PATH = "wn3.1.dict/dict";

    private ArrayList <SongDocument> _documents;

    private TokenizerME _tokenizer;
    private PorterStemmer _stemmer;

    public QueryExpansion(
//              Dictionary dictionary,
              ArrayList <SongDocument> documents
//              TokenizerME tokenizer,
//              PorterStemmer stemmer
    ) throws IOException {

        this._dictionary = new Dictionary(1024);
        this._documents = documents;

        TokenizerModel tokenizerModel = new TokenizerModel(new File("models/en-token.bin"));
        _tokenizer = new TokenizerME(tokenizerModel);
        _stemmer = new PorterStemmer();

        for (SongDocument d : documents) {
            d.doProcessing(_tokenizer, _stemmer);
            for (String s : d._terms) {
                _dictionary._keywords.add(s);
            }
        }
//        _dictionary.doProcessing(_stemmer, false);
        _dictionary.copyToTerms();
        _dictionary.computeIDFs(documents);

        for (SongDocument d : documents) {
            d.computeVectorRepresentations(_dictionary, _tokenizer, _stemmer);
        }
        // compute vector representations!
        RealMatrix A = MatrixUtils.createRealMatrix(_dictionary._terms.size(), _documents.size());

        for (int i = 0; i < _documents.size(); i++) {
            double[] bow = _documents.get(i)._bow_representation;
            A.setColumnVector(i, new ArrayRealVector(bow));
        }

        for (int i = 0; i < _dictionary._terms.size(); i++) {
            RealVector oldRow = A.getRowVector(i);
            double length = oldRow.getNorm();
            if (length == 0)
                continue; // iks de
            RealVector newRow = oldRow.mapDivide(length);
            A.setRowVector(i, newRow);
        }

        RealMatrix AT = A.transpose();
        _correlationMatrix = A.multiply(AT);
    }

    public void printMatrix(RealMatrix A) {
        for (int i = 0; i < A.getRowDimension(); i++) {
            for (int j = 0; j < A.getColumnDimension(); j++) {
                System.out.print(A.getRowVector(i).getEntry(j) + " ");
            }
            System.out.println("");
        }
    }

    public String expandQuery(String q)
    {
        double data[][] = _correlationMatrix.getData();

        SongDocument query = new SongDocument("query", q, "", "", 0);
        query.doProcessing(_dictionary, _tokenizer, _stemmer);
        query.computeVectorRepresentations(_dictionary, _tokenizer, _stemmer);

        Set <Integer> uniqueTerms_Query = new HashSet <>(_dictionary._terms.size());
        for (int i = 0; i < _dictionary._terms.size(); i++) {
            if (query._bow_representation[i] > 0) {
                uniqueTerms_Query.add(i);
            }
        }

        System.out.print("Original terms: ");
        for (Integer i : uniqueTerms_Query)
            System.out.print(_dictionary._terms.get(i) + " ");
        System.out.println("");
        System.out.println("Added terms: ");

        Set <Integer> uniqueTerms_ModifiedQuery = new HashSet <>(_dictionary._terms.size());
        for (Integer i : uniqueTerms_Query)
        {
            double newCorr, maxCorrelation = -1.0d;
            int index = -1;
            String newTerm, originalTerm = _dictionary._terms.get(i);

            for (int t = 0; t < _dictionary._terms.size(); t++) {
                newTerm = _dictionary._terms.get(t);
                newCorr = _correlationMatrix.getRowVector(i).getEntry(t);
                if (!newTerm.equals(originalTerm)
                        && !uniqueTerms_Query.contains(t)
                        && !uniqueTerms_ModifiedQuery.contains(t)
                        && newCorr > CORRELATION_THRESHOLD
                        && newCorr > maxCorrelation) {
                    index = t;
                    maxCorrelation = newCorr;
                }
            }
            if (index > -1)
            {
                System.out.println(String.format("   %s -> %s   correlation = %.2f",
                        _dictionary._terms.get(i),
                        _dictionary._terms.get(index),
                        data[i][index]));
                uniqueTerms_ModifiedQuery.add(index);
            }
        }

        StringBuilder content = new StringBuilder();
        String term;
        for (Integer i : uniqueTerms_Query) {
            term = _dictionary._terms.get(i);
            content.append(_dictionary._termsToKeywords.get(term) + " ");
        }
        for (Integer i : uniqueTerms_ModifiedQuery) {
            term = _dictionary._terms.get(i);
            content.append(_dictionary._termsToKeywords.get(term) + " ");
        }

        System.out.println("Content for the modified query = " + content);
        return content.toString();
    }

    public ArrayList <Score> getSortedDocuments(double[] queryVector)
    {
        return null;
    }

    public String getName()
    {
        return "Cosine similarity + correlation matrix";
    }

}
