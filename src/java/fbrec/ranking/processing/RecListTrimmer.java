package fbrec.ranking.processing;

import fbrec.control.Config;
import fbrec.error.PostProcessingException;
import fbrec.model.Recommendation;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * Scales the recommendation list to given size by removing recommendations or 
 * adding dummy recommendations.
 * @author Daniel
 */
public class RecListTrimmer implements IRecommendationProcessor{
    private int numResults;
    
    
    public RecListTrimmer(int numResults) {
        this.numResults = numResults;
    }

    
    
    @Override
    public void process(ArrayList<Recommendation> recommendations) throws PostProcessingException {
        Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
        if(recommendations.size() > numResults){                                //if to many recommendations are in list
            recommendations.subList(numResults, recommendations.size()).clear();//remove recommendations
            recommendations.trimToSize();
        }else{
            while(recommendations.size() < numResults){                         //if not enough recommendations are in list
                recommendations.add(Recommendation.dummy());                    //ensure length with dummy recommendations
            }
        }
    }
    
}
