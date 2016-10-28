package fbrec.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Dictionary for English - German translation
 * @author Daniel
 */
public class Dictionary {
    private static BiMap<String, String> dict;                                //dictionary EN -> DE
    
    static{
        dict = HashBiMap.create();
        dict.put("movie", "film");
        dict.put("book", "buch");
        dict.put("tv show", "fernsehen");
        dict.put("games/toys", "spiele");
        dict.put("musician/band", "musik");
        dict.put("clothing", "kleidung");
        dict.put("jewelery/watches", "schmuck");
        dict.put("athlete", "athlet");
        dict.put("professional sports team", "sport");
    }
    
    /**
     * translates given word to german.
     * returns null when there is no dictionary entry
     * @param word
     * @return 
     */
    public static String toGerman(String word){
        if(dict.containsKey(word.toLowerCase())) return dict.get(word.toLowerCase());
        else return null;
    }
    
     /**
     * translates given word to english.
     * returns null when there is no dictionary entry
     * @param word
     * @return 
     */
    public static String toEnglish(String word){
        if(dict.inverse().containsKey(word.toLowerCase())) return dict.inverse().get(word.toLowerCase());
        else return null;
    }
}
