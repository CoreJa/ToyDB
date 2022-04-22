package POJO;

import java.io.*;
import java.util.Arrays;
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
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(tables);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Table> Load() {
        Map<String, Table> tables;
        try
        {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tables = (Map<String, Table>) in.readObject();
            in.close();
            fileIn.close();
        }catch(IOException i)
        {
            i.printStackTrace();
            return new HashMap<String, Table>();
        } catch (ClassNotFoundException c) {
            System.out.println("Table Class not found");
            c.printStackTrace();
            return new HashMap<String, Table>();
        }
        return tables;
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public void createTable(String tableName, Table table) {
        this.tables.put(tableName, table);
    }

    public static String getFilename() {
        return filename;
    }

    public static void main(String[] args) {
        HashMap<String, DataRow> stringDataRowHashMap = new HashMap<>();
        stringDataRowHashMap.put("a", new DataRow(Arrays.asList(Type.STRING, Type.INT), Arrays.asList("a", 1)));
        Database database = new Database();
//        database.createTable("test",new Table(Arrays.asList("s","b"),stringDataRowHashMap));
        database.Save();
    }
}
