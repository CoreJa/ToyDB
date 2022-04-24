package POJO;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import utils.ExecuteEngine;
import utils.ExecutionException;

import java.io.*;
import java.util.*;


public class Database extends ExecuteEngine implements Serializable{
    private static final long serialVersionUID = 1L;
    private Map<String, Table> tables;// tableName, table
    static String filename = "./ToyDB.db"; // Where to save
    private Table returnValue;

    // Constructors
    public Database(){//Load from file
        this.tables = new HashMap<>();
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Table");
        List<Type> types = new ArrayList<>();
        types.add(Type.STRING);
        this.tables.put("TABLES", new Table(this, "TABLES", columnNames, types, 0));


        this.returnValue = null;
        // Create TABLES table
        String createTABLESQuery = "CREATE TABLE TABLES(" +
                " Table char," +
                " PRIMARY KEY(Table));";
        // Create COLUMNS table
        String createCOLUMNSQuery = "CREATE TABLE COLUMNS(" +
                " Table char," +
                " Column char," +
                " Type char," +
                " Display char," + //Display = Table + "." + Column
                " PRIMARY KEY(Display));";
        try {
            CCJSqlParserUtil.parse(createTABLESQuery).accept(this);
            CCJSqlParserUtil.parse(createCOLUMNSQuery).accept(this);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    public Database(Map<String, Table> tablesMap){
        this();
        this.tables = tablesMap;
    }


    // Storage
    public boolean save(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this.tables);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
        Table table = new Table(this, createTable); // create table object
        Table TABLES = this.tables.get("TABLES");
        if (TABLES!= null
                && TABLES.getColumnIndexes()!= null
                && TABLES.getColumnIndexes().containsKey(table.getTableName())) {
            throw new ExecutionException("Table already exists");
        }
        this.tables.put(table.getTableName(), table);
        this.returnValue = table.getReturnValue();
        //update metadata in TABLES
        try{
            CCJSqlParserUtil.parse("INSERT INTO TABLES VALUES (\'" + table.getTableName() + "\');").accept(this);
            for (int i = 0; i < table.getColumnNames().size(); i++) {
                String type = table.getTypes().get(i) == Type.STRING ? "string" : "int";
                String insertCOLUMNSQuery = "INSERT INTO COLUMNS VALUES (\'" + table.getTableName() + "\'," +
                        "\'" + table.getColumnNames().get(i) + "\'," +
                        "\'" + type + "\'," +
                        "\'" + table.getTableName() + "." + table.getColumnNames().get(i) + "\')";
                CCJSqlParserUtil.parse(insertCOLUMNSQuery).accept(this);
            }
        }catch(JSQLParserException e) {
            e.printStackTrace();
        }
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
            try {
                CCJSqlParserUtil.parse("DELETE FROM TABLES WHERE Table=\'" + tableName + "\'").accept(this);
                CCJSqlParserUtil.parse("DELETE FROM COLUMNS WHERE Table=\'" + tableName + "\'").accept(this);
            } catch(JSQLParserException e) {
                e.printStackTrace();
            }
        }
        if (drop.getType().toLowerCase().compareTo("index") == 0) {//Drop Index
            //DROP INDEX tableName.indexName
            if (drop.getName().getSchemaName() == null || drop.getName().getName()==null) {
                throw new SyntaxException("Should specify both table name and index name.");
            }
            String tableName = drop.getName().getSchemaName();
            //String indexName = drop.getName().getName();
            Table table = tables.get(tableName);
            if (table == null) {
                throw new SyntaxException("No such table.");
            }
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
        String tableName = ((PlainSelect) select.getSelectBody()).getFromItem().toString();
        if (!tables.containsKey(tableName)) {
            throw new ExecutionException(tableName + " doesn't exist.");
        }
        // will duplicate a new table object here
        Table table = new Table(tables.get(tableName));
        select.accept(table);
        this.returnValue = table.getReturnValue();


        /*
        -------------- Below are post-processes of select, write result to this.returnValue before get into this --------------
         */

        if (((PlainSelect) select.getSelectBody()).getDistinct()!=null){ // if distinct
            HashSet<String> set=new HashSet<>();
            Map<String,DataRow> data=returnValue.getData();
            Iterator<String> iterator=data.keySet().iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                String curVal = returnValue.getData().get(next).getDataGrids().stream().map(DataGrid::toString).reduce("",(x, y)->x+" # "+y);
                if (!set.add(curVal)) {
//                    data.remove(next);
                    iterator.remove();
                }
            }
        }

        if (((PlainSelect) select.getSelectBody()).getOrderByElements()!=null){ // if order by
            OrderByElement element=((PlainSelect) select.getSelectBody()).getOrderByElements().get(0);
            Database temp=new Database();
            element.getExpression().accept(temp);
            String colName=(String)temp.returnValue.getData().get("result").getDataGrids().get(0).getData();
            int colInd=returnValue.getColumnIndex(colName); // get column index
            List<Map.Entry<String, DataRow>> list = new ArrayList<>(returnValue.getData().entrySet()); //construct list from table
            if (returnValue.getTypes().get(colInd)==Type.STRING){ // sort the list.
                list.sort((o1, o2) -> o2.getValue().getDataGrids().get(colInd).toString()
                        .compareTo(o1.getValue().getDataGrids().get(colInd).toString()));
            }else{
                list.sort((o1, o2) ->(int)o2.getValue().getDataGrids().get(colInd).getData()
                                   - (int)o1.getValue().getDataGrids().get(colInd).getData());
            }
            if (!element.isAsc()) { // Ascending or Descending
                Collections.reverse(list);
            }
            LinkedHashMap<String,DataRow> orderedMap=new LinkedHashMap<>();
            for (Map.Entry<String, DataRow> entry : list) { // write into a hashmap that preserves order
                orderedMap.put(entry.getKey(),entry.getValue());
            }
            returnValue.setData(orderedMap);
        }

        if (((PlainSelect) select.getSelectBody()).getLimit()!=null){ // if limit
            ((PlainSelect) select.getSelectBody()).getLimit().getRowCount().accept(returnValue);
            int lim=(int)returnValue.getReturnValue().getData().get("result").getDataGrids().get(0).getData(); //get lim count
            if(returnValue.getData().size()>lim){
                int cur=0;
                Map<String,DataRow> data=returnValue.getData();
                Iterator<String> iterator=returnValue.getData().keySet().iterator();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    cur++;
                    if (cur>lim){
//                        data.remove(next);
                        iterator.remove();
                    }
                }
            }
        }


    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(Column tableColumn) {
        this.returnValue=new Table(tableColumn.getColumnName());
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
