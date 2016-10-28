package fbrec.model;

import de.tuebingen.uni.sfs.germanet.api.WordCategory;
import fbrec.control.Config;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Represents a tag. A Tag is a selected term and contains additional information
 * to the term itself. Only for Data encapsulation, offers no functionality.
 * @author Daniel
 */
public class Tag implements Comparable<Tag> {
    public String               text        = "";                               //term the tag represents
    public double               score       = 0.0;                              //score of the tag
    public Set<String>          wordPool    = null;                             //pool of words which have a connection to this tag
 
    public Set<String>          source      = null;                             //modules which contributed in creating this tag
    public Set<String>          base        = null;                             //fb-fields the the tag is based on

    public Tag(String text, double score, Class source, String[] base) {
        this.text   = text.toLowerCase();
        this.score  = score;
        this.source   = new HashSet<String>();
        this.base     = new HashSet<String>();
        this.wordPool = new HashSet<String>();
        
        this.source.add(source.toString());
        this.base.addAll(Arrays.asList(base));
        
        Logger.getLogger(Config.EVENT_LOGGER).debug("new tag (on creation): "+this);
    }
    
    public Tag(String text, double score, Class source, String base) {
        this.text   = text.toLowerCase();
        this.score  = score;
        this.source   = new HashSet<String>();
        this.base     = new HashSet<String>();
        this.wordPool = new HashSet<String>();
        
        this.source.add(source.toString());
        this.base.add(base);
        
        Logger.getLogger(Config.EVENT_LOGGER).debug("new tag (on creation): "+this);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Tag){
            Tag castedObj = (Tag) obj;
            return castedObj.text.equals(text);
        }
        return false;
    }    
    
    public boolean equalsSemantically(Tag tag){
        if(this.equals(tag) || wordPool.contains(tag.text)){
            return true;
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.text != null ? this.text.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Tag o) {
        if(score == o.score) return 0;
        if(score >  o.score) return 1;
        return -1;
    }

    @Override
    public String toString() {
        return text+":"+score;
    }
    
    

}


