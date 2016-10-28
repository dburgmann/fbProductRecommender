package fbrec.tagging.module;

import fbrec.control.Config;
import fbrec.error.TaggingException;
import fbrec.tagging.FbConnector.FbProfile;
import fbrec.model.Tag;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Abstract parent class for all modules.
 * A module is responsible for retrieving data from facebook and transforming the 
 * data into a list of tags.
 * @author Daniel
 */
public abstract class Module {
    protected FbProfile     profile;
    protected List<Tag>     tags;
    protected double        weight;     //weighting factor of this module
    protected int           numResults;
    
    public Module(double weight, int numResults) {
        this.weight   = weight;
        tags = new ArrayList<Tag>();
    }
   
    /**
     * retrieves the data from facebook.
     */
    protected abstract boolean retrieveData();
    
    /**
     * generates the tags from the retrieved data.
     * @throws ModuleException 
     */
    protected abstract void generateTags() throws TaggingException;
    
    /**
     * Public interface for initiating the module process.
     * initiates data retrieval, tag generation, score normalization and applying the weighting of the module
     * @return
     * @throws ModuleException 
     */
    public List<Tag> getTags(FbProfile profile) throws TaggingException{
        Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
        
        this.profile = profile;
        
        //start process
        if(!retrieveData()){
            Logger.getLogger(Config.EVENT_LOGGER).warn("module aborted because data could not be retrieved.");
            return tags;
        }
        generateTags();
        normalizeScores();
        
        //logging
        if(tags.isEmpty()){
            Logger.getLogger(Config.EVENT_LOGGER).warn("module did not generate any tags - to less information.");
        }
        
        //apply boost
        for(Tag tag:tags){
            tag.score = tag.score * weight;
            Logger.getLogger(Config.EVENT_LOGGER).info("tag (normalized & weighted): "+tag);
        }
        return tags;
    }
    
    /**
     * Normalizes the scores of the module
     */
     protected void normalizeScores() {
        Double scoreSum = 0.0;
        for(Tag tag : tags){
            scoreSum += tag.score;
        }
        
        for(Tag tag : tags){
            tag.score = tag.score / scoreSum;
        }
    }
    
    
    
}
