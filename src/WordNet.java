import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class WordNet {
    private Dictionary _wordnetDictionary;
    private static final String DICT_PATH = "wn3.1.dict/dict";

    private static String TOKENIZER_MODEL = "models/en-token.bin";
    private static String POS_MODEL = "models/en-pos-maxent.bin";
    public static String LEMMATIZER_DICT = "models/en-lemmatizer.dict";

    private TokenizerModel _tokenizerModel;
    private POSModel _posModel;
    private DictionaryLemmatizer _lemmatizer;

    public WordNet() {
        try {
            initModels();
            _wordnetDictionary = Dictionary.getFileBackedInstance(DICT_PATH);
        } catch (JWNLException e) {
            e.printStackTrace();
        }

    }

    private void initModels() {
        try {
            File modelFile = new File(TOKENIZER_MODEL);
            this._tokenizerModel = new TokenizerModel(modelFile);
            modelFile = new File(POS_MODEL);
            this._posModel = new POSModel(modelFile);
            modelFile = new File(LEMMATIZER_DICT);
            this._lemmatizer = new DictionaryLemmatizer(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] processQuery(String[] tokens) {
        POSTaggerME pt = new POSTaggerME(this._posModel);
        String[] tags = pt.tag(tokens);
        String[] lemmatize = _lemmatizer.lemmatize(tokens, tags);
        return pt.tag(lemmatize);
    }

    public HashMap<String, HashSet<String>> getSimilarWords(String query) {
        TokenizerME tok = new TokenizerME(this._tokenizerModel);
        String[] tokens = tok.tokenize(query);
        String[] posTags = processQuery(tokens);

        Set<String> uniqueTerms_Set = new HashSet<>();
        HashMap<String, HashSet<String>> synonymsMap = new HashMap<>();

        System.out.println("Original keywords: ");
        for (String i : tokens)
            System.out.println("   keyword=" + i);
        System.out.println("");
        System.out.println("Suggesting other keywords: ");

        for (int i = 0; i < tokens.length; i++) {
            if (!uniqueTerms_Set.contains(tokens[i])) {
                String keyword = tokens[i];
                uniqueTerms_Set.add(keyword);

                System.out.println("   for the keyword = " + keyword + ": ");
                IndexWord baseForm = null;

                try {
                    if (posTags[i].startsWith("V"))
                        baseForm = _wordnetDictionary.getMorphologicalProcessor().lookupBaseForm(POS.VERB, keyword);
                    else if (posTags[i].startsWith("N"))
                        baseForm = _wordnetDictionary.getMorphologicalProcessor().lookupBaseForm(POS.NOUN, keyword);
                    else
                        System.err.println("Huston, we've got a problem: " + posTags[i]);

                    HashSet<String> synonyms = new HashSet<>();

                    if (baseForm != null) {
                        for (Synset s : baseForm.getSenses()) {
                            for (Word w : s.getWords()) {
                                System.out.println("        " + w.getLemma());
                                synonyms.add(w.getLemma());
                            }
                        }
                    }
                    synonymsMap.put(keyword, synonyms);

                } catch (JWNLException e) {
                    e.printStackTrace();
                }
            }
        }
        return synonymsMap;
    }

}
