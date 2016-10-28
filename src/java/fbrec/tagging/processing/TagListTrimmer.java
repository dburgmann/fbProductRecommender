/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.tagging.processing;

import fbrec.control.Config;
import fbrec.model.Tag;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
* Reduces the size of the Tag-List based on given parameters
**/
public class TagListTrimmer implements ITagProcessor{
    int    newSize;

    /**
     * The Listsize is reduced to the given percentage of the old size but is never
     * reduced to a size smaller than minSize
     * @param percentage
     * @param minSize 
     */
    public TagListTrimmer(int maxSize) {
        this.newSize = maxSize;
    }

    
    @Override
    public void process(List<Tag> tags) {
        Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
        //sort
        Collections.sort(tags, Collections.reverseOrder());                                      
        
        //check if new size is bigger than current size
        if(newSize > tags.size()){                                              //new size is bigger => do nothing
            Logger.getLogger(Config.EVENT_LOGGER).warn("There are less tags than demanded! No Elements removed.");
        }else if(newSize < tags.size()){                                        //new size is smaller => remove elements
            Logger.getLogger(Config.EVENT_LOGGER).info(newSize-tags.size()+" elements removed");
            List<Tag> remove = tags.subList(newSize, tags.size());
            
            if(Logger.getLogger(Config.EVENT_LOGGER).getLevel().equals(Level.DEBUG)){
                for(Tag tag : remove){
                    Logger.getLogger(Config.EVENT_LOGGER).debug("tag removed: "+tag);
                }
            }
            
            remove.clear();
        }        
    }
}
