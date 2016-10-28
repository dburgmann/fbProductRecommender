package fbrec.tagging.processing;

import fbrec.model.Tag;
import java.util.List;

/**
 * Interface for all tag processors
 * @author Daniel
 */
public interface ITagProcessor {
    public void process(List<Tag> tags);
}
