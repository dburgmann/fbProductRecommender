package fbrec.tagging.processing;

import fbrec.control.Config;
import fbrec.model.Tag;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Merges tags which occour in the word pool of another.
 * @author Daniel
 */
public class TagMerger implements ITagProcessor{

    @Override
    public void process(List<Tag> tags) {
        Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
        
        boolean isMerged;
        Tag tag1, tag2;
        for(int i = 0; i < tags.size(); i++){ 
            isMerged = false;
            tag1     = tags.get(i);                                             //element whoose duplicates are searched
            for(int k = i+1; k < tags.size(); k++){                             //iterate over rest of list for comparison
                tag2 = tags.get(k);
                if(tag2.wordPool.contains(tag1.text)){                          //check if tag's wordPool contains tag1
                    tag2.score = tag1.score+tag2.score;                         //change attributes
                    tag2.source.addAll(tag1.source);
                    tag2.base.addAll(tag1.base);
                    isMerged = true;                                                                
                }
            }
            if(isMerged){
                Logger.getLogger(Config.EVENT_LOGGER).debug("tag merged: "+tag1);
                
                tags.remove(i);
            }                                        //remove merged tag           
        }
        
        //todo: testen!!
        
        /**
        Tag tag1, tag2;
        for(int i = 0; i < tags.size(); i++){ 
            tag1 = tags.get(i);                                                 //element whoose duplicates are searched
            for(int k = i+1; k < tags.size(); k++){                             //iterate over rest of list for comparison
                tag2 = tags.get(k);
                if(tag1.equalsSemantically(tag2)){                              //check if elements are equal
                    tag1.score = tag1.score+tag2.score;                         //global scores are added
                    tag1.source.addAll(tag2.source);
                    tag1.base.addAll(tag2.base);
                    tags.remove(k);                                             //remove duplicate
                }
            }
        }*/
    }
}
