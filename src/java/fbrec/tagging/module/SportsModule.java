/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.tagging.module;

/**
 *
 * @author Daniel
 */
public class SportsModule extends LikeBasedModule{

    public SportsModule(double weight, int numResults) {
        super(weight, numResults);
    }
    
    @Override
    protected String[] likeCategories() {
        String[] cats = {"Athlete", "Professional Sports Team"};
        return cats;
    }
    
}
