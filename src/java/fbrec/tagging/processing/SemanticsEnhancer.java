package fbrec.tagging.processing;

import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;
import fbrec.control.Config;
import fbrec.model.Tag;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.xml.stream.XMLStreamException;
import org.apache.log4j.Logger;

/**
 * Enhances the semantics of the tags in the list, by adding synonyms to their 
 * word pool.
 * @author Daniel
 */
public class SemanticsEnhancer implements ITagProcessor{

    @Override
    public void process(List<Tag> tags) {
        try {
            Logger.getLogger(Config.EVENT_LOGGER).info("-- "+this.getClass().toString()+" called...");
            GermaNet        gnet = new GermaNet(Config.getFile(Config.GERMANET_DIR));
            List<Synset>    synsets;
            
            for(Tag tag : tags){
                synsets = gnet.getSynsets(tag.text);                            //get Synset if exists
                
                for(Synset synset :synsets){
                    Logger.getLogger(Config.EVENT_LOGGER).debug("found synset for tag: "+tag);
                    tag.wordPool.addAll(synset.getAllOrthForms());              //add all corresponding orthforms to wordpool
                }
            }
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(SemanticsEnhancer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            java.util.logging.Logger.getLogger(SemanticsEnhancer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SemanticsEnhancer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
