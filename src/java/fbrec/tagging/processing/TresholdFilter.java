/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.tagging.processing;

import fbrec.control.Config;
import fbrec.model.Tag;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Removes all elements whoose score is lower than a treshold.
 * The treshold is at least as high as the lowest score of all tags in the list
 * because this is typically the score of terms occouring only once in the entire collection.
 * The maximum of the lowest score and the default treshold is used as treshold.
 * @author Daniel
 */
public class TresholdFilter implements ITagProcessor{
    private double percentage;
    private int    minNumTags;

    public TresholdFilter(double percentage, int minNumTags) {
        this.percentage = percentage;
        this.minNumTags = minNumTags;
    }
    
    @Override
    public void process(List<Tag> tags) {
        Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
        
        Tag     tag;
        double  treshold;
        double  lowestScore;
        
        Collections.sort(tags);
        lowestScore = 0; //tags.get(0).score;
        treshold = Math.max(tags.get(tags.size()-1).score * percentage, lowestScore);
        
        if(tags.get(0).score == tags.get(tags.size()-1).score) return;          //all have same score -> no filtering
        
        for(int i = 0; i < tags.size(); i++){
            tag = tags.get(i);
            if(tag.score <= treshold){                                          //remove all elements with a score smaller or eqal the treshold
                Logger.getLogger(Config.EVENT_LOGGER).debug("tag removed: "+tag);
                
                tags.remove(tag);
                i--;                                                            //decrement because list size was reduced with removal
            }
        }
    }

}
