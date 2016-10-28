/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.util;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fbrec.control.Config;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Daniel
 */
public class PosFilter {
        /**
     * Applies POS-Filtering to given document text.
     * The text string must at least consist of one sentence.
     * @param text          sentence or document that should be filtered
     * @param allowedPos    allowed POS-Tags according to Stuttgard - Tübingen-Tagset
     * @return  text consisting only of words with allowed pos tags
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static String filter(String text, String[] allowedPos) throws IOException, ClassNotFoundException{
        String              filteredText    = "";
        MaxentTagger        posTagger;
        List<List<HasWord>> untaggedSentence;
        List<TaggedWord>    taggedSentence;
           
        posTagger        = new MaxentTagger(Config.POS_MODEL_FILE);             //load tagger
        untaggedSentence = MaxentTagger.tokenizeText(new StringReader(text));   //tokenize
                
        for(List<HasWord> sentence : untaggedSentence){
            taggedSentence = posTagger.tagSentence(sentence);                   //tag sentence
            for(TaggedWord word : taggedSentence){  
                if(Arrays.asList(allowedPos).contains(word.tag())){             //filter sentence
                    filteredText += " "+word.value();                           //reconcatenate
                }
            }
        }
        return filteredText;
    }
}
