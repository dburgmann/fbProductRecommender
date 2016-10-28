package fbrec.tagging.module;

/**
 * Generates tags based on entertainment likes
 * @author Daniel
 */
public class EntertainmentModule extends LikeBasedModule{
    
    public EntertainmentModule(double weight, int numResults) {
        super(weight, numResults);
    }
    
    @Override
    protected String[] likeCategories() {
        String[] cats = {"Book","Movie","Tv show","Games/toys", "Musician/band"};
        return cats;
    }
}
