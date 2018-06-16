import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Normalizer {

    private static String TOKENIZER_MODEL = "models/en-token.bin";
    private static String POS_MODEL = "models/en-pos-maxent.bin";
    private static String LEMMATIZER_DICT = "models/en-lemmatizer.dict";

    private static TokenizerModel _tokenizerModel;
    private static POSModel _posModel;
    private static DictionaryLemmatizer _lemmatizer;
    private static StopWordsRemover _StopWordsRemover;

    private static Normalizer instance = null;

    public Normalizer() {
        initModels();
    }

    public static Normalizer getInstance() {
        if(instance == null)
            instance = new Normalizer();
        return instance;
    }

    private void initModels() {
        try {
            File modelFile = new File(TOKENIZER_MODEL);
            this._tokenizerModel = new TokenizerModel(modelFile);
            modelFile = new File(POS_MODEL);
            this._posModel = new POSModel(modelFile);
            modelFile = new File(LEMMATIZER_DICT);
            this._lemmatizer = new DictionaryLemmatizer(modelFile);
            this._StopWordsRemover = new StopWordsRemover();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] removeFromList(String[] tab, String word) {
        ArrayList<String> words = new ArrayList<>();
        for (String i : tab) {
            if (!i.equals(word)) {
                words.add(i);
            }
        }
        return words.toArray(new String[0]);
    }
    private static String[] removeStopWords(String[] tab){
        ArrayList<String> words = new ArrayList<>();
        for (String i : tab) {
            if (_StopWordsRemover.isStopWord(i)==false) {
                words.add(i);
            }
        }
        return words.toArray(new String[0]);
    }

    public String[] normalizeText(String text) {
        TokenizerME tok = new TokenizerME(_tokenizerModel);
        String[] tokens = tok.tokenize(text);
        POSTaggerME pt = new POSTaggerME(_posModel);
        String[] tags = pt.tag(tokens);
        String[] lemmatize = _lemmatizer.lemmatize(tokens, tags);
        String[] noStopWords = removeStopWords(lemmatize);
        return removeFromList(noStopWords, "O");
    }

    public String concatenate(String[] normalized) {
        ArrayList<String> words = new ArrayList<>(Arrays.asList(normalized));
        StringBuilder sb = new StringBuilder();

        for (String i : words) {
            sb.append(i).append(" ");
        }
        return sb.toString();
    }

}
