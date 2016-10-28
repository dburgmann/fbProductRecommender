package fbrec.tagging.module;

import com.google.common.base.Joiner;
import fbrec.control.Config;
import fbrec.error.TaggingException;
import fbrec.tagging.FbConnector.FbMessage;
import fbrec.tagging.FbConnector.FbStatus;
import fbrec.model.Tag;
import fbrec.tagging.FbConnector.FbFriend;
import fbrec.util.FriendsTokenFilter;
import fbrec.util.PosFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;



/**
 *
 * @author Daniel
 */
public class PosTextModule extends Module {
    //data from facebook
    private List<FbStatus>    statuses;
    private List<FbMessage>   outbox;
    private List<FbFriend>    friends;
    private String[]          allowedPos = {"NN", "NE", "ADJA", "ADJD"}; //allowed POS-Tags
    private String[]          fields     = {"status-messages", "outbox"};

    public PosTextModule(double weight, int numResults) {
        super(weight,numResults);
    }
    
    @Override
    protected boolean retrieveData() {
        statuses    = profile.statuses();
        outbox      = profile.outbox();
        friends     = profile.friends();
        if(statuses == null && outbox == null){
            Logger.getLogger(Config.EVENT_LOGGER).warn("no permission to retrieve posts from facebook.");
            return false;
        }
        return true;
    }

    @Override
    protected void generateTags() throws TaggingException{
        try {
            String              document        = generateDocument();           //document created from all texts
            int                 count           = 0;
            List<TfTerm>        termList;
            
            //execute pos filtering
            document   = PosFilter.filter(document, allowedPos);
            
            //get occourences
            termList   = getFrequencies(tokenizeAndFilter(document));
            
            //sort
            Collections.sort(termList, Collections.reverseOrder());
            
            //select Tags
            for(TfTerm term : termList){
                if(term.tf > 1){
                    tags.add(new Tag(term.text, term.tf, getClass(), fields));
                    count++;
                }
                if(count == numResults) break;
            } 
        } catch (Exception ex) {
            throw new TaggingException(ex, this.getClass());
        }
    }
    
    
    /**
     * Generates a document from the posts-list retrieved from facebook.
     * For Use in tf-idf matrix
     * @return 
     */
    private String generateDocument(){
        String  result = "";
        Joiner  joiner  = Joiner.on(" ");
        if(statuses != null) result += joiner.join(statuses);
        else Logger.getLogger(Config.EVENT_LOGGER).warn("status messages could not be retrieved.");
        
        if(outbox != null) result += joiner.join(outbox);
        else Logger.getLogger(Config.EVENT_LOGGER).warn("outbox messages could not be retrieved.");

        return result;
    }
   
    
    protected CharArraySet loadStopwords(){                
        //try to load stopwords file
        CharArraySet stopWords;
        try {
            FileReader fr = Config.loadFile(Config.GERMAN_STOPWORD_FILE);
            stopWords  = WordlistLoader.getSnowballWordSet(fr, Version.LUCENE_40);
            fr.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Config.EVENT_LOGGER).log(Level.WARNING, "Unable to open stop word list, using default instead", ex);
            stopWords = GermanAnalyzer.getDefaultStopSet();
        }
        return stopWords;
    }
    
    /**
     * Returns a TokenStream for given String.
     * Applies filters to improve data quality
     * @param str
     * @return 
     */
    protected TokenStream tokenizeAndFilter(String str){
        //tokenize
        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_40, new StringReader(str));        
        tokenStream = new LengthFilter(true, tokenStream, 3, 100);                                  //remove empty and single letter tokens
        tokenStream = new LowerCaseFilter(Version.LUCENE_40, tokenStream);                          //lower case all tokens
        tokenStream = new GermanNormalizationFilter(tokenStream);                                   //apply german normalization (eg. ae vs ä)
        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream, loadStopwords());              //stop word filtering
        tokenStream = new FriendsTokenFilter(tokenStream, friends);

        return tokenStream;
    }
    
    
    private List<TfTerm> getFrequencies(TokenStream tokenStream) throws IOException{
        CharTermAttribute   charTermAttribute   = tokenStream.addAttribute(CharTermAttribute.class);
        String              token;
        
        HashMap<String, TfTerm> temp = new HashMap<String, TfTerm>();
        //iterate over tokens
        while(tokenStream.incrementToken()){
            token   = charTermAttribute.toString();
            
            if(temp.containsKey(token)){
                temp.get(token).tf += 1;
            }else{
                temp.put(token, new TfTerm(token, 1));
            }
        }
        return new ArrayList<TfTerm>(temp.values());       
    }
    
    private class TfTerm implements Comparable{
        public String text;
        public double tf;

        public TfTerm(String text, double tf) {
            this.tf = tf;
            this.text = text;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + (this.text != null ? this.text.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TfTerm other = (TfTerm) obj;
            if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
                return false;
            }
            return true;
        }        

        @Override
        public int compareTo(Object t) {
            if(t instanceof TfTerm){
                TfTerm term = (TfTerm) t;
                if(tf > term.tf ) return 1;
                if(tf < term.tf)  return -1;
            }
            return 0;
        }
   }
}