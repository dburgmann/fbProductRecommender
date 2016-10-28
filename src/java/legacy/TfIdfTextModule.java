package legacy;

import fbrec.control.Config;
import fbrec.error.TaggingException;
import fbrec.tagging.FbConnector.FbMessage;
import fbrec.tagging.FbConnector.FbStatus;
import fbrec.model.Tag;
import fbrec.tagging.module.Module;
import legacy.TfidfMatrix;
import legacy.TermMatrix.Doc;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Daniel
 */
public class TfIdfTextModule extends Module {
    //data from facebook
    private List<FbStatus>    statuses;
    private List<FbMessage>   outbox;

    public TfIdfTextModule(double weight, int numResults) {
        super(weight, numResults);
    }
    
    @Override
    protected boolean retrieveData() {
        statuses    = profile.statuses();
        outbox      = profile.outbox();
        if(statuses == null && outbox == null){
            Logger.getLogger(Config.EVENT_LOGGER).warn("no permission to retrieve posts from facebook.");
            return false;
        }
        return true;
    }

    @Override
    protected void generateTags() throws TaggingException{
        try {
            //vars
            List<String>    topTerms;
            List<Doc>       postDocuments;
            TfidfMatrix     tfidfMatrix;
            Tag             tag;
            double          tfidf;
            
            postDocuments   = documents();                                      //get list of documents
            tfidfMatrix     = new TfidfMatrix(postDocuments);                   //get tfidf matrix
            
            for(Doc doc : postDocuments){                                       //iterate through docs to determine top-terms
                topTerms = tfidfMatrix.topTerms(doc.ID, 2);                     //get top-terms
                for(String tagText : topTerms){                                 //add top terms to tag-tree
                    tfidf = tfidfMatrix.tfidf(doc.ID, tagText);
                    tag   = new Tag(tagText, tfidf, getClass(), "posts");
                    tags.add(tag);
                }
            }          
        } catch (Exception ex) {
            throw new TaggingException(ex, this.getClass());
        }
    }
    
    
    /**
     * Generates a List of documents from the posts-list retrieved from facebook.
     * For Use in tf-idf matrix
     * @return 
     */
    private List<Doc> documents(){
        List<Doc> result = new ArrayList<Doc>();
        int       count  = 0;
        
        if(statuses != null){
            for(FbStatus status:  statuses){
                result.add(new Doc(count, status.message));
                count++;
            }
        }else{
            Logger.getLogger(Config.EVENT_LOGGER).warn("status messages could not be retrieved.");
        }
        
        if(outbox != null){
            for(FbMessage message:  outbox){
                result.add(new Doc(count, message.message));
                count++;
            }
        }else{
            Logger.getLogger(Config.EVENT_LOGGER).warn("outbox messages could not be retrieved.");
        }
        return result;
    }
}
 