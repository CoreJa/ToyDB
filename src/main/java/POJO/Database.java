package POJO;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import utils.ExecuteEngine;
import utils.SyntaxException;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Database extends ExecuteEngine implements Serializable{
    private Map<String, Table> tables;// tableName, table
    static String filename = "./ToyDB.db"; // Where to save
    private Table returnValue;// ???

    //Constructors
    public Database() {//Load from file
        this.tables = new HashMap<>();
        //this.tables = this.Load();
    }

    public Database(Map<String, Table> tablesMap) {
        this.tables = tablesMap;
    }

    //Storage
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
        try {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tables = (Map<String, Table>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
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

    public void addTable(String tableName, Table table) {
        this.tables.put(tableName, table);
    }

    public static String getFilename() {
        return filename;
    }

    // visit
    @Override
    public void visit(CreateTable createTable){
        Table table = new Table(createTable);
        this.addTable(table.getTableName(), table);
    }

    @Override
    public void visit(CreateIndex createIndex) {
        Table table = tables.get(createIndex.getTable().getName());
        createIndex.accept(table);
        this.returnValue = table.getReturnValue();
    }

    @Override
    public void visit(Insert insert) {
        Table table = tables.get(insert.getTable().getName());
        insert.accept(table);
        this.returnValue = table.getReturnValue();
    }

    @Override
    public void visit(Drop drop) {
        if (drop.getType().toLowerCase().compareTo("table") == 0) {//Drop Table

        }
        if (drop.getType().toLowerCase().compareTo("index") == 0) {//Drop Index
            // getSchemaName actually gets table name. e.g. drop index tableName.indexName
            String tableName = drop.getName().getSchemaName();
            Table table = tables.get(tableName);
            drop.accept(table);
            this.returnValue = table.getReturnValue();
        }

    }

    @Override
    public void visit(Select selectStatement) throws SyntaxException {
        SelectBody selectBody = selectStatement.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            PlainSelect stmt = (PlainSelect) selectBody;
            FromItem fromItem = stmt.getFromItem();
            if (fromItem instanceof net.sf.jsqlparser.schema.Table) {
                net.sf.jsqlparser.schema.Table fromItemTable = (net.sf.jsqlparser.schema.Table) fromItem;
                Table table = tables.get((fromItemTable.getSchemaName()));


            } else if (fromItem instanceof SubSelect) {
                SubSelect subSelect = (SubSelect) fromItem;
                this.visit(subSelect);
            } else if (fromItem instanceof SubJoin) {
                //TODO: 暂时不知道subJoin
            }



        } else {
            throw new SyntaxException(selectBody.toString());
        }
    }

    public static void main(String[] args) {
        String selectDemo2 = "SELECT DISTINCT ID, ID2 " +
                "FROM MY_TABLE1, MY_TABLE2, (SELECT * FROM MY_TABLE3) LEFT OUTER JOIN MY_TABLE4 " +
                "WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)";
        try {
            new Database().visit((Select) CCJSqlParserUtil.parse(selectDemo2));
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
        HashMap<String, DataRow> stringDataRowHashMap = new HashMap<>();
        stringDataRowHashMap.put("a", new DataRow(Arrays.asList(Type.STRING, Type.INT), Arrays.asList("a", 1)));
        Database database = new Database();
//        database.createTable("test",new Table(Arrays.asList("s","b"),stringDataRowHashMap));
        database.Save();
    }
}
