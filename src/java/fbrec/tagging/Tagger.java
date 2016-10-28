package fbrec.tagging;

import fbrec.model.Tag;
import fbrec.control.Config;
import fbrec.error.TaggingException;
import fbrec.tagging.processing.ITagProcessor;
import fbrec.error.PostProcessingException;
import fbrec.tagging.FbConnector.FbProfile;
import fbrec.tagging.module.Module;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Controls the tagging process.
 * @author Daniel
 */
public class Tagger{
    private ArrayList<Tag>              tags    = null;
    private ArrayList<ITagProcessor>    filters = null;
    private ArrayList<Module>           modules = null;

    public Tagger(){
        super();
        filters = new ArrayList<ITagProcessor>();
        modules = new ArrayList<Module>();
        tags    = new ArrayList<Tag>();
    }

    /**
     * adds given module to the execution list
     * @param module 
     */
    public void addModule(Module module){
        modules.add(module);
    }
    
    
    /**
     * generates tags for given profile
     * @param profile
     * @throws TaggingException 
     */
    public void tagProfile(FbProfile profile) throws TaggingException{
        for(Module module : modules){
            tags.addAll(module.getTags(profile));
        }
    }
    
    
    /**
     * Adds the specified processing to the filter list
     * @param processor 
     */
    public void addProcessor(ITagProcessor processor){
        filters.add(processor);
    }
    
    
    /**
     * Executes the processing - calling all processors which were added and
     * reducing the result list to the size which was specified in config
     * @throws FilterException 
     */
    public void processTags() throws PostProcessingException{
        for(ITagProcessor filter : filters){
            filter.process(tags);
        }
        Logger.getLogger(Config.EVENT_LOGGER).info("- Tags after post processing:"+tags.size());
        if(Logger.getLogger(Config.EVENT_LOGGER).getLevel().equals(Level.DEBUG)){
            for(Tag tag : tags){
                Logger.getLogger(Config.EVENT_LOGGER).debug(tag);
            }
        }
    }
    
    /**
     * returns generated tags
     * @return 
     */
    public List<Tag> getTags(){
        return tags;
    }
}
