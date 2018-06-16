package engine;

import opennlp.tools.stemmer.PorterStemmer;

import java.util.*;

public class Dictionary
{
    public ArrayList <String> _keywords;
    public ArrayList <String> _terms;
    public HashMap <String, String> _termsToKeywords;
    public ArrayList <Double> _idf;

    public HashMap <String, Integer> _termID;

    public Dictionary(int capacity)
    {
        this._keywords = new ArrayList <>(capacity);
    }

    public void computeIDFs(ArrayList <SongDocument> documents)
    {
        // TODO compute idfs values
        // 1) iterate over the _terms
        // 2) check how many documents contain this term (document._terms.contains())
        // 3) compute idf value (Math.log()) and update _idf vector (add idf value)
        _idf = new ArrayList <>(_terms.size());
        for ( String term : _terms ) {
            int howMany  = 0;
            for ( SongDocument doc : documents ) {
                if (doc._terms.contains(term))
                    howMany += 1;
            }
            // https://pl.wikipedia.org/wiki/TFIDF
            Double idfVal = Math.log((double) documents.size() / (double) howMany);
            if (howMany == 0)
                idfVal = -1.0;

            _idf.add(idfVal);
        }
    }

    public void copyToTerms() {
        _termsToKeywords = new HashMap <>(_keywords.size());
        _terms = new ArrayList <>(_keywords.size());
        for (String s : _keywords)
        {
            _termsToKeywords.put(s, s); // hihi
        }

        _terms.addAll(_keywords);
        Collections.sort(_terms);

        _termID = new HashMap <>();
        for (int i = 0; i < _terms.size(); i++)
            _termID.put(_terms.get(i), i);

    }

    public void doProcessing(PorterStemmer stemmer, boolean log)
    {
        Set <String> stemmed = new HashSet <>();
        _termsToKeywords = new HashMap <>(_keywords.size());
        _terms = new ArrayList <>(_keywords.size());
        for (String s : _keywords)
        {
            String stem = stemmer.stem(s);
            stemmed.add(stem);
            _termsToKeywords.put(stem, s);
        }

        _terms.addAll(stemmed);
        Collections.sort(_terms);

        _termID = new HashMap <>();
        for (int i = 0; i < _terms.size(); i++)
            _termID.put(_terms.get(i), i);

        if (log)
        {
            for (String s : _terms)
                System.out.println(s);
            System.out.println(_terms.size() + " " + _keywords.size());
        }
    }

    void print()
    {
        for (int i = 0; i < _terms.size(); i++)
        {
            System.out.println(i + " : " + _terms.get(i) + "   idf = " + _idf.get(i));
        }
    }
}
