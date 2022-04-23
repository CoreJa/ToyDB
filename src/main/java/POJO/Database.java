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
    private static final long serialVersionUID = 1L;
    private Map<String, Table> tables;// tableName, table
    static String filename = "./ToyDB.db"; // Where to save
    private Table returnValue;// ???

    // Constructors
    public Database() {//Load from file
        //无参构造方法是创建时默认调用的. 如果想要使用load(), 请在main里初始化过程中load
        this.tables = new HashMap<>();
    }

    public Database(Map<String, Table> tablesMap) {
        this.tables = tablesMap;
    }


    // Storage
    public boolean save(String filename) {
        boolean flag = false;
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))){
            out.writeObject(this.tables);
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return flag;
        }
    }
    public boolean save(){
        return this.save(this.filename);
    }

    public boolean load(String filename) {//return if load successfully
        boolean flag = false;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            this.tables = (Map<String, Table>) in.readObject();
            flag = true;
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("DB file not found, creating an empty one.");
            this.tables=new HashMap<>();
        } catch (ClassNotFoundException e) {
            System.out.println("Table Class not found");
        } finally {
            return flag; // TODO: fall backs?
        }
    }
    public boolean load() {
        return this.load(this.filename);
    }


    // Getters, setters(putTable)
    public Table getTable(String tableName) {
        return tables.get(tableName);
    }
    public void putTable(Table table) {
        tables.put(table.getTableName(), table);
    }
    public Table getReturnValue() {
        return returnValue;
    }

    // visit
    @Override
    public void visit(CreateTable createTable){
        Table table = new Table(this, createTable);
        this.tables.put(table.getTableName(), table); // TODO: table name already exists? return value?
    }

    @Override
    public void visit(CreateIndex createIndex) {
        Table table = tables.get(createIndex.getTable().getName());
        createIndex.accept(table);
        this.returnValue = table.getReturnValue();
    }

    @Override
    public void visit(Drop drop) {
        if (drop.getType().toLowerCase().compareTo("table") == 0) {//Drop Table
            String tableName = drop.getName().getName(); //和下面的句式结构不一样, 注意
            // use dropped to check if the statement is valid
            if(this.tables.remove(tableName) == null) {
                throw new SyntaxException("Drop table: TABLE " + tableName + " not exists");
            }
        }
        if (drop.getType().toLowerCase().compareTo("index") == 0) {//Drop Index
            //DROP INDEX tableName.indexName
            String tableName = drop.getName().getSchemaName();
            //String indexName = drop.getName().getName();
            Table table = tables.get(tableName);
            drop.accept(table);
            this.returnValue = table.getReturnValue();
        }

    }

    @Override
    public void visit(Insert insert) {
        Table table = tables.get(insert.getTable().getName());
        insert.accept(table);
        this.returnValue = table.getReturnValue();
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
        database.save();
    }
}
