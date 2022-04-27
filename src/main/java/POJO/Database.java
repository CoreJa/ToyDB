package POJO;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import utils.ExecuteEngine;
import utils.ExecutionException;
import utils.preloadData;

import java.io.*;
import java.util.*;


public class Database extends ExecuteEngine implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Table> tables;// tableName, table
    static String filename = "./ToyDB.db"; // Where to save
    private Table returnValue;

    // Constructors
    public Database() {
        this.tables = new HashMap<>();
        this.returnValue = null;
    }

    public void createMetadata() {//Load from file
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Table");
        List<Type> types = new ArrayList<>();
        types.add(Type.STRING);
        this.tables.put("TABLES", new Table(this, "TABLES", columnNames, types, 0));
        columnNames = new ArrayList<>();
        columnNames.add("Table");
        columnNames.add("Column");
        columnNames.add("Type");
        columnNames.add("Display");
        types = new ArrayList<>();
        types.add(Type.STRING);
        types.add(Type.STRING);
        types.add(Type.STRING);
        types.add(Type.STRING);
        this.tables.put("COLUMNS", new Table(this, "COLUMNS", columnNames, types, 3));


    }


    public Database(Map<String, Table> tablesMap) {
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
            this.createMetadata();
            preloadData.preload(this);
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
        if (TABLES != null
                && TABLES.getColumnIndexes() != null
                && TABLES.getColumnIndexes().containsKey(table.getTableName())) {
            throw new ExecutionException("Table already exists");
        }
        this.tables.put(table.getTableName(), table);
        this.returnValue = table.getReturnValue();
        //update metadata in TABLES
        try {
            CCJSqlParserUtil.parse("INSERT INTO TABLES VALUES (\'" + table.getTableName() + "\');").accept(this);
            for (int i = 0; i < table.getColumnNames().size(); i++) {
                String type = table.getTypes().get(i) == Type.STRING ? "string" : "int";
                String insertCOLUMNSQuery = "INSERT INTO COLUMNS VALUES (\'" + table.getTableName() + "\'," +
                        "\'" + table.getColumnNames().get(i) + "\'," +
                        "\'" + type + "\'," +
                        "\'" + table.getTableName() + "." + table.getColumnNames().get(i) + "\')";
                CCJSqlParserUtil.parse(insertCOLUMNSQuery).accept(this);
            }
        } catch (JSQLParserException e) {
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
            } catch (JSQLParserException e) {
                e.printStackTrace();
            }
        }
        if (drop.getType().toLowerCase().compareTo("index") == 0) {//Drop Index
            //DROP INDEX tableName.indexName
            if (drop.getName().getSchemaName() == null || drop.getName().getName() == null) {
                throw new ExecutionException("Should specify both table name and index name.");
            }
            String tableName = drop.getName().getSchemaName();
            //String indexName = drop.getName().getName();
            Table table = tables.get(tableName);
            if (table == null) {
                throw new ExecutionException("No such table.");
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
    public void visit(Update update) {
        Table table = tables.get(update.getTable().getName());
        update.accept(table);
        this.returnValue = table.getReturnValue();
    }

    @Override
    public void visit(Delete delete) {
        Table table = tables.get(delete.getTable().getName());
        delete.accept(table);
        this.returnValue = table.getReturnValue();
    }

//    private Pair<String,String> getEq(AndExpression expr){
//        Expression l=expr.getLeftExpression();
//        Expression r=expr.getRightExpression();
//        if (l instanceof  EqualsTo){
//
//        }
//    }

    @Override
    public void visit(Select select) {
        PlainSelect plainSelect = ((PlainSelect) select.getSelectBody());
        String tableName = plainSelect.getFromItem().toString();
        if (!tables.containsKey(tableName)) {
            throw new ExecutionException(tableName + " doesn't exist.");
        }
        // will duplicate a new table object here
        Table table = new Table(tables.get(tableName));

        //join statement
        if (plainSelect.getJoins() != null) {
            Table leftTable = new Table();
            leftTable.setSimple(false);
            // modify column names and indexes
            int cnt = 0;
            for (String columnName : table.getColumnNames()) {
                String name = table.getTableName() + "." + columnName;
                leftTable.getColumnNames().add(name);
                leftTable.getColumnIndexes().put(name, cnt++);
                leftTable.getTypes().add(table.getTypes().get(table.getColumnIndex(columnName)));
            }
            leftTable.setData(table.getData());

            for (Join join : plainSelect.getJoins()) {
                //assume we always get Table
                String rightTableName = ((net.sf.jsqlparser.schema.Table) join.getRightItem()).getName();
                Table rightTable = new Table(this.getTable(rightTableName));
                //modify left table column names and indexes as we join right table
                for (String columnName : rightTable.getColumnNames()) {
                    String name = rightTableName + "." + columnName;
                    leftTable.getColumnNames().add(name);
                    leftTable.getColumnIndexes().put(name, cnt++);
                    leftTable.getTypes().add(rightTable.getTypes().get(rightTable.getColumnIndex(columnName)));
                }
                //detect if we have on statement
                String leftCol = null;
                String rightCol = null;
                for (Expression onExpression : join.getOnExpressions()) {
                    onExpression.accept(this);
                    leftCol = this.returnValue.getData().get("result1").getDataGrids().get(0).getData().toString();
                    rightCol = this.returnValue.getData().get("result2").getDataGrids().get(0).getData().toString();
                }

                if (rightCol == null||leftCol == null) {

                }

                Map<String, DataRow> leftData = leftTable.getData();
                Map<String, DataRow> rightData = rightTable.getData();
                Map<String, DataRow> joinedData = new HashMap<>();

                if ((leftCol == null || rightCol == null) ||
                        (leftCol.split("\\.")[0].compareTo(rightCol.split("\\.")[0]) == 0)) {
                    // the case that no on statement is found or cols are from the same table

                    if (leftCol != null && rightCol != null && leftCol.compareTo(rightCol) != 0) {
                        // clean left data first
                        int colIdx1 = leftTable.getColumnIndex(leftCol);
                        int colIdx2 = rightTable.getColumnIndex(rightCol);
                        Iterator<String> iterator = leftData.keySet().iterator();
                        while (iterator.hasNext()) {
                            String next = iterator.next();
                            DataRow dataRow = leftData.get(next);
                            if (dataRow.getDataGrids().get(colIdx1).compareTo(dataRow.getDataGrids().get(colIdx2))) {
                                iterator.remove();
                            }
                        }
                    }
                    // then joining right data
                    for (Map.Entry<String, DataRow> leftEntry : leftData.entrySet()) {
                        for (Map.Entry<String, DataRow> rightEntry : rightData.entrySet()) {
                            joinedData.put(leftEntry.getKey() + "#" + rightEntry.getKey(),
                                    new DataRow(leftEntry.getValue(), rightEntry.getValue()));
                        }
                    }
                } else {
                    //the case that leftCol and rightCol are from different table
                    int cacheColIdx;
                    int leftIdx;
                    if (rightTableName.compareTo(leftCol.split("\\.")[0]) == 0) {
                        cacheColIdx = rightTable.getColumnIndex(leftCol.split("\\.")[1]);
                        leftIdx = leftTable.getColumnIndex(rightCol);
                    } else if (rightTableName.compareTo(rightCol.split("\\.")[0]) == 0) {
                        cacheColIdx = rightTable.getColumnIndex(rightCol.split("\\.")[1]);
                        leftIdx = leftTable.getColumnIndex(leftCol);
                    } else {
                        throw new ExecutionException("joined table doesn't have key " + leftCol + " or " + rightCol);
                    }
                    //build cache
                    Map<String, List<String>> cache = new HashMap<>();
                    for (Map.Entry<String, DataRow> rightEntry : rightData.entrySet()) {
                        String key = rightEntry.getValue().getDataGrids().get(cacheColIdx).toString();
                        if (cache.containsKey(key)) {
                            cache.get(key).add(rightEntry.getKey());
                        } else {
                            cache.put(key, Arrays.asList(rightEntry.getKey()));
                        }
                    }
                    int rightPaddingSize = rightTable.getColumnNames().size();
                    int leftPaddingSize = leftTable.getColumnNames().size() - rightPaddingSize;

                    if (join.isFull()) {
                        //full join
                        Set<String> notJoinedRow = new HashSet<>(rightTable.getData().keySet());
                        for (Map.Entry<String, DataRow> leftEntry : leftData.entrySet()) {
                            String x = leftEntry.getValue().getDataGrids().get(leftIdx).toString();
                            if (cache.containsKey(x)) {
                                for (String pk : cache.get(x)) {
                                    joinedData.put(leftEntry.getKey() + "#" + pk,
                                            new DataRow(leftEntry.getValue(), rightTable.getData().get(pk)));
                                    notJoinedRow.remove(pk);
                                }
                            } else {
                                joinedData.put(leftEntry.getKey() + "#",
                                        new DataRow(leftEntry.getValue(), new DataRow(rightPaddingSize)));
                            }
                        }
                        for (String s : notJoinedRow) {
                            joinedData.put("#" + s,
                                    new DataRow(new DataRow(leftPaddingSize), rightTable.getData().get(s)));
                        }
                    } else if (join.isLeft()) {
                        //left outer join
                        for (Map.Entry<String, DataRow> leftEntry : leftData.entrySet()) {
                            String x = leftEntry.getValue().getDataGrids().get(leftIdx).toString();
                            if (cache.containsKey(x)) {
                                for (String pk : cache.get(x)) {
                                    joinedData.put(leftEntry.getKey() + "#" + pk,
                                            new DataRow(leftEntry.getValue(), rightTable.getData().get(pk)));
                                }
                            } else {
                                joinedData.put(leftEntry.getKey() + "#",
                                        new DataRow(leftEntry.getValue(), new DataRow(rightPaddingSize)));
                            }
                        }
                    } else if (join.isRight()) {
                        //right outer join
                        Set<String> notJoinedRow = new HashSet<>(rightTable.getData().keySet());
                        for (Map.Entry<String, DataRow> leftEntry : leftData.entrySet()) {
                            String x = leftEntry.getValue().getDataGrids().get(leftIdx).toString();
                            if (cache.containsKey(x)) {
                                for (String pk : cache.get(x)) {
                                    joinedData.put(leftEntry.getKey() + "#" + pk,
                                            new DataRow(leftEntry.getValue(), rightTable.getData().get(pk)));
                                    notJoinedRow.remove(pk);
                                }
                            }
                        }
                        for (String s : notJoinedRow) {
                            joinedData.put("#" + s,
                                    new DataRow(new DataRow(leftPaddingSize), rightTable.getData().get(s)));
                        }
                    } else if (join.isInner() || (!join.isOuter() && !join.isSimple() && !join.isNatural() &&
                            !join.isCross() && !join.isSemi() && !join.isStraight() && !join.isApply())) {
                        //inner join
                        for (Map.Entry<String, DataRow> leftEntry : leftData.entrySet()) {
                            String x = leftEntry.getValue().getDataGrids().get(leftIdx).toString();
                            if (cache.containsKey(x)) {
                                for (String pk : cache.get(x)) {
                                    joinedData.put(leftEntry.getKey() + "#" + pk,
                                            new DataRow(leftEntry.getValue(), rightTable.getData().get(pk)));
                                }
                            }
                        }
                    } else {
                        throw new ExecutionException("This type of join is not implemented yet!");
                    }
                }
                leftTable.setData(joinedData);
            }
            table = leftTable;
        }
        // recursively parsing select
        plainSelect.accept(table);
        this.returnValue = table.getReturnValue();


        /*
        -------------- Below are post-processes of select, write result to this.returnValue before get into this --------------
         */

        if (plainSelect.getDistinct() != null) { // if distinct
            HashSet<String> set = new HashSet<>();
            Map<String, DataRow> data = returnValue.getData();
            Iterator<String> iterator = data.keySet().iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                String curVal = returnValue.getData().get(next).getDataGrids().stream().map(DataGrid::toString).reduce("", (x, y) -> x + " # " + y);
                if (!set.add(curVal)) {
                    iterator.remove();
                }
            }
        }

        if (plainSelect.getOrderByElements() != null) { // if order by
            OrderByElement element = plainSelect.getOrderByElements().get(0);
            Database temp = new Database();
            element.getExpression().accept(temp);
            String colName = (String) temp.returnValue.getData().get("result").getDataGrids().get(0).getData();
            int colInd = returnValue.getColumnIndex(colName); // get column index
            List<Map.Entry<String, DataRow>> list = new ArrayList<>(returnValue.getData().entrySet()); //construct list from table
            if (returnValue.getTypes().get(colInd) == Type.STRING) { // sort the list.
                list.sort((o1, o2) -> ((String) o2.getValue().getDataGrids().get(colInd).getData())
                        .compareTo((String) o1.getValue().getDataGrids().get(colInd).getData()));
            } else {
                list.sort((o1, o2) -> (int) o2.getValue().getDataGrids().get(colInd).getData()
                        - (int) o1.getValue().getDataGrids().get(colInd).getData());
            }
            if (element.isAsc()) { // Ascending or Descending
                if (plainSelect.getLimit() != null) { // if limit
                    plainSelect.getLimit().getRowCount().accept(returnValue);
                    int lim = (int) returnValue.getReturnValue().getData().get("result").getDataGrids().get(0).getData(); //get lim count
                    if (list.size() > lim) {
                        list = list.subList(list.size() - lim, list.size());
                    }
                }
                Collections.reverse(list);
            } else {
                if (plainSelect.getLimit() != null) { // if limit
                    plainSelect.getLimit().getRowCount().accept(returnValue);
                    int lim = (int) returnValue.getReturnValue().getData().get("result").getDataGrids().get(0).getData(); //get lim count
                    if (list.size() > lim) {
                        list = list.subList(0, lim);
                    }
                }
            }
            Map<String, DataRow> orderedMap = new LinkedHashMap<>();
            for (Map.Entry<String, DataRow> entry : list) { // write into a hashmap that preserves order
                orderedMap.put(entry.getKey(), entry.getValue());
            }
            returnValue.setData(orderedMap);
        } else {
            if (plainSelect.getLimit() != null) { // if limit
                plainSelect.getLimit().getRowCount().accept(returnValue);
                int lim = (int) returnValue.getReturnValue().getData().get("result").getDataGrids().get(0).getData(); //get lim count
                if (returnValue.getData().size() > lim) {
                    int cur = 0;
                    Map<String, DataRow> data = returnValue.getData();
                    Iterator<Map.Entry<String, DataRow>> iterator = returnValue.getData().entrySet().iterator();
                    Map<String, DataRow> newdata = new HashMap<>();
                    while (iterator.hasNext() && cur < lim) {
                        Map.Entry<String, DataRow> next = iterator.next();
                        cur++;
                        newdata.put(next.getKey(), next.getValue());
                    }
                    returnValue.setData(newdata);
                }
            }
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        equalsTo.getLeftExpression().accept(this);
        String left = this.returnValue.getData().get("result").getDataGrids().get(0).getData().toString();
        equalsTo.getRightExpression().accept(this);
        String right = this.returnValue.getData().get("result").getDataGrids().get(0).getData().toString();
        this.returnValue = new Table(left, right);
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
        this.returnValue = new Table(tableColumn.toString());
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
