package fbrec.tagging.module;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fbrec.control.Config;
import fbrec.error.TaggingException;
import fbrec.tagging.FbConnector.FbLike;
import fbrec.model.Tag;
import fbrec.util.Dictionary;
import java.util.Collection;
import org.apache.log4j.Logger;

/**
 *
 * @author Daniel
 */
public abstract class LikeBasedModule extends Module{
    Multimap<String, FbLike> categoryLikes;
    
    public LikeBasedModule(double weight, int numResults) {
        super(weight, numResults);
    }
    
    protected abstract String[] likeCategories();
    
    @Override
    protected boolean retrieveData() {
        Logger.getLogger(Config.EVENT_LOGGER).info("retrieving data...");
        Collection<FbLike> likes;
        categoryLikes = HashMultimap.create();
        for(String cat: likeCategories()){
            likes = profile.likesWithCategory(cat.toUpperCase());
            if(likes != null){
                categoryLikes.putAll(cat, likes);
            }else{
                Logger.getLogger(Config.EVENT_LOGGER).warn("no permission to retrieve likes from facebook.");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void generateTags() throws TaggingException {
        Tag tag;
        Collection<FbLike> catLikes;
        for(String cat : likeCategories()){
            catLikes = categoryLikes.get(cat);                                  //get likes of category
            for(FbLike like : catLikes){                                            
                tag = new Tag(like.name.toLowerCase(), 0.5, getClass(), cat+"-likes");        //generate new tag
                tag.wordPool.add(Dictionary.toGerman(cat));                     //add translation to wordPool
                tags.add(tag);
                
                if(tags.size() == numResults) return;                           //return when enough likes were found
            }
        }
    }
}
