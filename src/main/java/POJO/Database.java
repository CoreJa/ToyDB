package POJO;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import utils.ExecuteEngine;
import utils.ExecutionException;

import java.io.*;
import java.util.*;

import static java.util.Collections.unmodifiableList;

public class Database extends ExecuteEngine implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Table> tables;// tableName, table
    static String filename = "./ToyDB.db"; // Where to save
    private Table returnValue;// ???

    // Constructors
    public Database() {//Load from file
        this.tables = new HashMap<>();
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Table");
        List<Type> types = new ArrayList<>();
        types.add(Type.STRING);
        this.tables.put("TABLES", new Table(this, "TABLES", columnNames, types, 0));
    }

    public Database(Map<String, Table> tablesMap) {
        this.tables = tablesMap;
    }


    // Storage
    public boolean save(String filename) {
        boolean flag = false;
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this.tables);
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return flag;
        }
    }

    public boolean save() {
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
            this.tables = new HashMap<>();
        } catch (ClassNotFoundException e) {
            System.out.println("Table Class not found");
        } finally {
            return flag;
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
    public void visit(CreateTable createTable) {
        Table table = new Table(this, createTable);
        this.tables.put(table.getTableName(), table);
        this.returnValue = table.getReturnValue();
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
            if (this.tables.remove(tableName) == null) {
                throw new ExecutionException("Drop table: TABLE " + tableName + " not exists");
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
    public void visit(Select select) {
        //TODO: test
        String tableName = ((PlainSelect) select.getSelectBody()).getFromItem().toString();
        if (!tables.containsKey(tableName)) {
            throw new ExecutionException(tableName + " doesn't exist.");
        }
        Table table = new Table(tables.get(tableName));
        select.accept(table);
        this.returnValue = table.getReturnValue();

        if (((PlainSelect) select.getSelectBody()).getDistinct()!=null){ // if distinct
            HashSet<List<DataGrid>> set=new HashSet<>();
            Iterator<String> iterator=returnValue.getData().keySet().iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                List<DataGrid> curVal = Collections.unmodifiableList(returnValue.getData().get(next).getDataGrids());
                if (!set.add(curVal)) {
                    returnValue.getData().remove(next);
                }
            }
        }
        // if order by
        // if limit


    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
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
