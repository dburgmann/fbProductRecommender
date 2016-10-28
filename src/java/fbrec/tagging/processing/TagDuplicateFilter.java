package fbrec.tagging.processing;

import fbrec.control.Config;
import fbrec.model.Tag;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Removes duplicates from the tag list.
 * @author Daniel
 */
public class TagDuplicateFilter implements ITagProcessor{

    @Override
    public void process(List<Tag> tags) {
        Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
        
        //find real duplicates
        Tag tag1, tag2;
        for(int i = 0; i < tags.size(); i++){ 
            tag1 = tags.get(i);                                                 //element whoose duplicates are searched
            for(int k = i+1; k < tags.size(); k++){                             //iterate over rest of list for comparison
                tag2 = tags.get(k);
                if(tag1.equals(tag2)){                                          //check if elements are equal
                    Logger.getLogger(Config.EVENT_LOGGER).debug("tag removed: "+tag1);
                    
                    tag1.score = tag1.score+tag2.score;                         //global scores are added
                    tag1.source.addAll(tag2.source);                            //source sets are combined
                    tag1.base.addAll(tag2.base);                                //base sets are combined
                    tags.remove(k);                                             //remove duplicate                
                }
            }
        }
    }
}
