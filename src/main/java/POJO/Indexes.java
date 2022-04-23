package POJO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Indexes implements Serializable {
    private List<String> indexNames;
    private List<Map<String, List<String>>> indexes;
    public Indexes(int n){
        indexNames=new ArrayList<>(Collections.nCopies(n,null));
        indexes=new ArrayList<>(Collections.nCopies(n,null));
    }

    public List<String> getIndexNames() {
        return indexNames;
    }

    public List<Map<String, List<String>>> getIndexes() {
        return indexes;
    }
}
