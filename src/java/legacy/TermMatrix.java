/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package legacy;

import fbrec.control.Config;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;

/**
 *
 * @author Daniel
 */
public abstract class TermMatrix {
    private CharArraySet stopWords;    
    
    protected void loadStopwords(){                
        //try to load stopwords file
        try {
            FileReader fr = Config.loadFile(Config.GERMAN_STOPWORD_FILE);
            stopWords  = WordlistLoader.getSnowballWordSet(fr, Version.LUCENE_40);
            fr.close();
        } catch (IOException ex) {
            Logger.getLogger(Config.EVENT_LOGGER).log(Level.WARNING, "Unable to open stop word list, using default instead", ex);
            stopWords = GermanAnalyzer.getDefaultStopSet();
        }
    }
    
    /**
     * Returns a TokenStream for given String.
     * Applies filters to improve data quality
     * @param str
     * @return 
     */
    protected TokenStream tokenizeSingleWords(String str){
        loadStopwords();
        
        //tokenize
        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_40, new StringReader(str));
        tokenStream = new PatternReplaceFilter(tokenStream, Pattern.compile("[0-9]*"), "", true);   //filter out all numbers
        tokenStream = new LengthFilter(true, tokenStream, 3, 100);                                  //remove empty and single letter tokens
        tokenStream = new LowerCaseFilter(Version.LUCENE_40, tokenStream);                          //lower case all tokens
        tokenStream = new GermanNormalizationFilter(tokenStream);                                   //apply german normalization (eg. ae vs ä)
        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream, stopWords);                    //stop word filtering

        return tokenStream;
    }
    
    
        /**
     * Returns a TokenStream for given String.
     * Applies filters to improve data quality
     * @param str
     * @return 
     */
    protected TokenStream tokenizeNShingles(String str, int n){
        loadStopwords();
        
        //tokenize
        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_40, new StringReader(str));
        tokenStream = new GermanNormalizationFilter(tokenStream);                                   //apply german normalization (eg. ae vs ä)
        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream, stopWords);                    //stop word filtering
        tokenStream = new PatternReplaceFilter(tokenStream, Pattern.compile("[0-9]*"), "", true);   //filter out all numbers
        tokenStream = new LowerCaseFilter(Version.LUCENE_40, tokenStream);                          //lower case all tokens
        tokenStream = new ShingleFilter(tokenStream, n);

        return tokenStream;
    }
    
    
   /**
    * Data Class for documents
    */
   public static class Doc{
       public final int ID;
       public String text;

        public Doc(int ID, String text) {
            this.ID = ID;
            this.text = text;
        }
   }
    
}
