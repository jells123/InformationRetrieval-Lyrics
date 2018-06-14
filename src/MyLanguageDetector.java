import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;

import java.io.File;
import java.io.IOException;

public class MyLanguageDetector {
    private static String LANG_DETECT_MODEL = "models/langdetect-183.bin";
    private static LanguageDetectorME _languageDetector;

    public static void initModel() {
        try {
            File modelFile = new File(LANG_DETECT_MODEL);
            LanguageDetectorModel model = new LanguageDetectorModel(modelFile);
            _languageDetector = new LanguageDetectorME(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LanguageDetectorME get_languageDetector() {
        return _languageDetector;
    }
}
