package fbrec.control;

import fbrec.ranking.Ranker;
import fbrec.matching.Matcher;
import com.restfb.exception.FacebookGraphException;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookOAuthException;
import fbrec.error.PostProcessingException;
import fbrec.error.MatchingException;
import fbrec.error.TaggingException;
import fbrec.tagging.processing.TagMerger;
import fbrec.tagging.processing.SemanticsEnhancer;
import fbrec.tagging.processing.TresholdFilter;
import fbrec.tagging.module.EntertainmentModule;
import fbrec.error.ConfigException;
import fbrec.tagging.Tagger;

import fbrec.model.Recommendation;
import fbrec.ranking.Ranking;
import fbrec.tagging.module.BrandsModule;
import fbrec.ranking.processing.RecDuplicateFilter;
import fbrec.ranking.processing.RecListTrimmer;
import fbrec.ranking.processing.ScoreRanker;
import fbrec.tagging.FbConnector;
import fbrec.tagging.FbConnector.FbProfile;
import fbrec.tagging.module.SportsModule;
import fbrec.tagging.module.PosTextModule;
import fbrec.tagging.processing.TagListTrimmer;
import fbrec.tagging.processing.TagDuplicateFilter;
import java.io.IOException;
import legacy.TfIdfTextModule;
import org.apache.log4j.Logger;

/**
 * Manges the recommendation process.
 * Responsible for initiating data colletion, calling modules and initiating
 * ranking process
 * @author Daniel
 */
public class RecommendationProcess {
    private String       accessToken;
    private FbConnector  fbConnector;
    private Matcher      matcher;
    private Ranker       ranker;
    private Tagger       tagger;
    private int          numResults;
    private int          numTags;
    private FbProfile    fbProfile;

    
    /**
     * Creates a new RecommendationProcess instance
     * and initializes it
     * 
     * @param aAccessToken  Facebook access token used for authentication 
     */
    public RecommendationProcess(String accessToken) {
        this.accessToken = accessToken;
        this.fbConnector = new FbConnector(accessToken);
        this.ranker      = new Ranker();
        this.tagger      = new Tagger();
    }
    
    
    /**
     * Adds the Modules used for recommendation computation
     */
    public RecommendationProcess init(int numResults, int numTags) throws ConfigException, IOException{
        //load configuration
        Config.loadConfig();
        
        //check if numResults is valid
        this.numResults = (numResults > 0)  ? numResults    : Config.DEFAULT_NUM_RESULTS;
        this.numTags    = (numTags > 0)     ? numTags       : Config.DEFAULT_NUM_TAGS;
        fbProfile       = fbConnector.getProfile();
        this.matcher    = new Matcher(fbProfile, this.numResults);
        
        //add modules and processors
        tagger.addModule(new EntertainmentModule(Config.ENTERTAINMENT_WEIGHT, Config.MAX_NUM_TAGS_PER_MODULE));
        tagger.addModule(new BrandsModule(Config.BRANDS_WEIGHT, Config.MAX_NUM_TAGS_PER_MODULE));
        tagger.addModule(new SportsModule(Config.SPORTS_WEIGHT, Config.MAX_NUM_TAGS_PER_MODULE));
        //tagger.addModule(new PosTextModule(Config.TEXT_WEIGHT, Config.MAX_NUM_TAGS_PER_MODULE));
        tagger.addModule(new TfIdfTextModule(Config.TEXT_WEIGHT, Config.MAX_NUM_TAGS_PER_MODULE));
        tagger.addProcessor(new SemanticsEnhancer());
        tagger.addProcessor(new TagDuplicateFilter());
        tagger.addProcessor(new TagMerger());
        //tagger.addProcessor(new TresholdFilter(Config.MIN_TAG_SCORE_PERCENT, this.numTags));
        tagger.addProcessor(new TagListTrimmer(this.numTags));
        ranker.addProcessor(new RecDuplicateFilter());
        ranker.addProcessor(new ScoreRanker());
        ranker.addProcessor(new RecListTrimmer(this.numResults));
        
        
        return this;
    }
    
    
    /**
     * Starts the recommendation computation and returns the results.
     * 
     * @return resultlist of recommendations
     */
    public Ranking getRecommendations() throws TaggingException, FacebookOAuthException, FacebookNetworkException, FacebookGraphException, PostProcessingException, MatchingException{
        //log process start
        Logger.getLogger(Config.EVENT_LOGGER).info("----------------------------------------");
        Logger.getLogger(Config.EVENT_LOGGER).info("---- RECOMMENDATION-PROCESS STARTED ----");
        Logger.getLogger(Config.EVENT_LOGGER).info("----------------------------------------");  
        Logger.getLogger(Config.EVENT_LOGGER).info("accesstoken: "+accessToken);
        Logger.getLogger(Config.EVENT_LOGGER).info("number of results: "+numResults);
        Logger.getLogger(Config.EVENT_LOGGER).info("number of tags: "+numTags);
        Ranking result;
        
        //call modules
        Logger.getLogger(Config.EVENT_LOGGER).info("--- calling modules");
        tagger.tagProfile(fbProfile);
        
        //call tag post processing
        Logger.getLogger(Config.EVENT_LOGGER).info("--- calling tag postprocessors");
        tagger.processTags();
        
        //match products
        Logger.getLogger(Config.EVENT_LOGGER).info("--- starting matching process");
        matcher.matchTagsToProducts(tagger.getTags());
        
        //recommendation post processing
        Logger.getLogger(Config.EVENT_LOGGER).info("--- starting ranking");
        ranker.processRecommendations(matcher.getRecommendations());
        
        result = ranker.getRanking();
        
        //logging
        for(Recommendation rec : result){
            Logger.getLogger(Config.RESULT_LOGGER).info(rec);
        }
        
        //setting permissions & token
        result.setAccessToken(accessToken);
        result.setPermissions(fbConnector.expectedPermissions());
        
        //log process end
        Logger.getLogger(Config.EVENT_LOGGER).info("-----------------------------------------");
        Logger.getLogger(Config.EVENT_LOGGER).info("---- RECOMMENDATION-PROCESS FINISHED ----");
        Logger.getLogger(Config.EVENT_LOGGER).info("-----------------------------------------");
        
        return result;
    }
}
