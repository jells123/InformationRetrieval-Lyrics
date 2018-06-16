

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class StopWordsRemover {
    private final String stopWordsFile = "stopwords.txt";
    private List<String> stopWordsList;
    private Set<String> stopWordsSet;
    public StopWordsRemover(){
        this.stopWordsList = new ArrayList<String>();
        this.readStopWords();
        this.stopWordsSet =  new HashSet<String>(this.stopWordsList);
    }
    private void readStopWords(){
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(this.stopWordsFile);
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                this.stopWordsList.add(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isStopWord(String word){
        if(this.stopWordsSet.contains(word)){
            return true;
        }
        return false;
    }

}
