package fbrec.tagging.module;

/**
 * Generates tags based on brand likes
 * @author Daniel
 */
public class BrandsModule extends LikeBasedModule{
    public BrandsModule(double weight, int numResults) {
        super(weight, numResults);
    }
    
    @Override
    protected String[] likeCategories() {
        String[] cats = {"Clothing","Jewelry/watches"};
        return cats;
    }
}
