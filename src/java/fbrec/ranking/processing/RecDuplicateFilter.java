package fbrec.ranking.processing;

import fbrec.error.PostProcessingException;
import fbrec.control.Config;
import fbrec.model.Recommendation;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * Removes duplicates from a result set, increasing the score of the product
 * @author Daniel
 */
public class RecDuplicateFilter implements IRecommendationProcessor{

    @Override
    public void process(ArrayList<Recommendation> recommendations) throws PostProcessingException {
        Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
        
        Recommendation rec1, rec2;
        for(int i = 0; i < recommendations.size(); i++){ 
            rec1 = recommendations.get(i); //element whoose duplicates are searched
            for(int k = i+1; k < recommendations.size(); k++){  //iterate over rest of list for comparison
                rec2 = recommendations.get(k);
                if(rec1.equals(rec2)){ //check if elements are equal
                    Logger.getLogger(Config.EVENT_LOGGER).info("recommendation removed: "+rec2);
                    
                    rec1.setScore(rec1.getScore()+rec2.getScore()); //global scores are added
                    recommendations.remove(k); //remove duplicate                    
                }
            }
        }
    }
}
