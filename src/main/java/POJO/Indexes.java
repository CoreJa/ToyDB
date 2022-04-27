package POJO;

import java.io.Serializable;
import java.util.*;

public class Indexes implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> indexNames;
    private List<Map<String, Set<String>>> indexes;
    public Indexes(int numberOfColumns){
        indexNames=new ArrayList<>(Collections.nCopies(numberOfColumns,null));
        indexes=new ArrayList<>(Collections.nCopies(numberOfColumns,null));
    }

    public List<String> getIndexNames() {
        return indexNames;
    }

    public List<Map<String, Set<String>>> getIndexes() {
        return indexes;
    }
}
