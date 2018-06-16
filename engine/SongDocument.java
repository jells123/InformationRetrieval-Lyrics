package engine;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;

import java.util.*;

public class SongDocument
{
    public String _songtitle;
    public String _lyrics;
    public String _artist;
    public String _country;

    public int _ID;

    public double _bow_representation[];
    public double _tf_representation[];
    public double _tf_idf_representation[];

    public ArrayList <String> _terms;

    public SongDocument(String title, String lyrics, String artist, String country, int id)
    {
        this._songtitle = title;
        this._lyrics = lyrics;
        this._artist = artist;
        this._country = country;
        this._ID = id;
    }

    public void print()
    {
        System.out.print(_songtitle);
        System.out.print(_lyrics);
    }

    private String[] getTokenizedAndNormalized(TokenizerME tokenizer, PorterStemmer stemmer)
    {
        String tokens[] = tokenizer.tokenize(_lyrics);
        for (int i = 0; i < tokens.length; i++)
            tokens[i] = stemmer.stem(tokens[i].toLowerCase());
        return tokens;
    }

    public void doProcessing(
//            engine.Dictionary dictionary,
            TokenizerME tokenizer, PorterStemmer stemmer)
    {
        String tokenizedAndNormalized[] = getTokenizedAndNormalized(tokenizer, stemmer);

        Set <String> fTokens = new HashSet <>();
        for (String s : tokenizedAndNormalized) {
//            if (dictionary._terms.contains(s))
            fTokens.add(s);
        }

        _terms = new ArrayList <>(fTokens.size());
        _terms.addAll(fTokens);
        Collections.sort(_terms);
    }
    public void doProcessing(
            engine.Dictionary dictionary,
            TokenizerME tokenizer, PorterStemmer stemmer)
    {
        String tokenizedAndNormalized[] = getTokenizedAndNormalized(tokenizer, stemmer);

        Set <String> fTokens = new HashSet <>();
        for (String s : tokenizedAndNormalized) {
            if (dictionary._terms.contains(s))
                fTokens.add(s);
        }

        _terms = new ArrayList <>(fTokens.size());
        _terms.addAll(fTokens);
        Collections.sort(_terms);
    }

    public void computeVectorRepresentations(Dictionary dictionary, TokenizerME tokenizer, PorterStemmer stemmer)
    {
        String tokenizedAndNormalized[] = getTokenizedAndNormalized(tokenizer, stemmer);

        _bow_representation = new double[dictionary._terms.size()];
        for (int i = 0; i < _bow_representation.length; i++) {
            _bow_representation[i] = 0;
        }
        HashMap <String, Integer> termIDs = dictionary._termID;
        double max = -1.0;
        for ( String term : tokenizedAndNormalized ) {
            if (dictionary._terms.contains(term)) {
                int id = termIDs.get(term);
                _bow_representation[id] += 1;
            }
        }

        for (int i = 0; i < _bow_representation.length; i++) {
            if (_bow_representation[i] > max)
                max = _bow_representation[i];
        }

        _tf_representation = new double[dictionary._terms.size()];
        for ( int i = 0; i < dictionary._terms.size(); i++ ) {
            _tf_representation[i] = _bow_representation[i] / max;
        }

        _tf_idf_representation = new double[dictionary._terms.size()];
        for ( int i = 0; i < dictionary._terms.size(); i++ ) {
            double idf = dictionary._idf.get(i);
            double tf = _tf_representation[i];
            _tf_idf_representation[i] = tf * idf;
        }
    }
}
