package POJO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Database {
    private Map<String, Table> tables;
    static String filename = "./ToyDB.db"; // Where to save


    public Database(Map<String, Table> tables) {
        this.tables = tables;
    }

    //Constructors

    public Database() {
        this.tables = this.Load();
    }

    public void Save() {
    }

    private Map<String, Table> Load() {

        return new HashMap<String, Table>();
    }
}
