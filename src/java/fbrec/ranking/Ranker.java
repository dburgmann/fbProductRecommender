package fbrec.ranking;

import fbrec.ranking.processing.IRecommendationProcessor;
import fbrec.error.PostProcessingException;
import fbrec.model.Recommendation;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ranking of recommendations
 * @author Daniel
 */
public class Ranker{
    private ArrayList<IRecommendationProcessor> filters = null;
    private Ranking                             ranking = null;

    public Ranker() {
        filters = new ArrayList<IRecommendationProcessor>();
    }
    
    
    /**
     * Adds the specified processor to the filter list
     * @param processor
     */
    public void addProcessor(IRecommendationProcessor processor){
        filters.add(processor);
    }
    
    
    /**
     * Executes the processing - calling all processors which were added
     * @throws FilterException 
     */
    public void processRecommendations(List<Recommendation> recommendations) throws PostProcessingException{
        ranking = new Ranking(recommendations);
        for(IRecommendationProcessor filter : filters){
            filter.process(ranking);
        }
    }

    public Ranking getRanking() {
        return ranking;
    }
    
    
    

}
