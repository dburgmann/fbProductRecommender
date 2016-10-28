package fbrec.ranking.processing;

import fbrec.error.PostProcessingException;
import fbrec.control.Config;
import fbrec.model.Recommendation;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.log4j.Logger;

/**
 * Orders the resultlist and sets ranks accordingly
 * @author Daniel
 */
public class ScoreRanker implements IRecommendationProcessor{

    @Override
    public void process(ArrayList<Recommendation> recommendations) throws PostProcessingException {
        Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
        
        Collections.sort(recommendations, Collections.reverseOrder());          //sort descending
        for(int i = 0; i < recommendations.size(); i++){
            recommendations.get(i).setRank(i+1);
        }
    }    
}
