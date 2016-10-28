package fbrec.ranking.processing;

import fbrec.error.PostProcessingException;
import fbrec.model.Recommendation;
import java.util.ArrayList;

/**
 * Interface for recommendation list processors
 * @author Daniel
 */
public interface IRecommendationProcessor {
    public void process(ArrayList<Recommendation> recommendations) throws PostProcessingException;
}
