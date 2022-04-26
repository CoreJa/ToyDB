package POJO;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import utils.ExecuteEngine;
import utils.ExecutionException;
import utils.ExecutionException;


import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Table extends ExecuteEngine implements Serializable {
    private static final long serialVersionUID = 1L;
    /* indicates if the table is a temporary table used for transmitting return value, which means does not contain
     *  metadata, default is true, set false in complex constructors.*/
    private boolean simple = true;
    private Database db;
    private String tableName;

    protected List<String> getColumnNames() {
        return columnNames;
    }

    protected Map<String, Integer> getColumnIndexes() {
        return columnIndexes;
    }

    private List<String> columnNames;
    private Map<String, Integer> columnIndexes;
    private List<Type> types;

    public Map<String, DataRow> getData() {
        return data;
    }

    public void setData(Map<String, DataRow> data) {
        this.data = data;
    }

    private Map<String, DataRow> data;//key: primary key; value: data record
    private Table returnValue;
    // Indexes of all columns, elements corresponding to PrimaryKey and None Indexed columns should be Null.
    private Indexes indexes;
    //Constraints
    private Integer primaryKey; // index in columnNames
    private List<Set<String>> uniqueSet; // maintain a HashSet of value of . Always cast to String.
    private List<Pair<String, Integer>> foreignKeyList;

    //Constructors (with DB)
    public Table() {//by default
        this(true, null, null, new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), null, new Indexes(0), null, new ArrayList<>(), new ArrayList<>());

    }

    public Table(boolean simple, Database db, String tableName, List<String> columnNames, Map<String, Integer> columnIndexes,
                 List<Type> types, Map<String, DataRow> data, Table returnValue, Indexes indexes,
                 Integer primaryKey, List<Set<String>> uniqueSet, List<Pair<String, Integer>> foreignKeyList) {
        this.simple = simple;
        this.db = db;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.columnIndexes = columnIndexes;
        this.types = types;
        this.data = data;
        this.returnValue = returnValue;
        this.indexes = indexes;
        this.primaryKey = primaryKey;
        this.uniqueSet = uniqueSet;
        this.foreignKeyList = foreignKeyList;
    }

    public Table(Database db, String tableName, List<String> columnNames, List<Type> types, Integer primaryKey) {
        // Used for TABLES, COLUMNS table
        Map<String, Integer> columnIndexes = new HashMap<>();
        List<Set<String>> uniqueSet = new ArrayList<>();
        List<Pair<String, Integer>> foreignKeyList = new ArrayList<>();
        for (String columnName : columnNames) {
            columnIndexes.put(columnName, columnIndexes.size());
            uniqueSet.add(null);
            foreignKeyList.add(null);
        }

        this.simple = false;
        this.db = db;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.columnIndexes = columnIndexes;
        this.types = types;
        this.data = new HashMap<>();
        this.returnValue = null;
        this.indexes = new Indexes(columnNames.size());
        this.primaryKey = primaryKey;
        this.uniqueSet = uniqueSet;
        this.foreignKeyList = foreignKeyList;

    }

    public Table(Database db, CreateTable createTableStatement) throws ExecutionException {
        // create table by statement
        // define the name and dataType of each column
        List<ColumnDefinition> columnDefinitionList = createTableStatement.getColumnDefinitions();

        this.simple = false;
        this.db = db;
        this.tableName = createTableStatement.getTable().getName();
        this.columnNames = new ArrayList<>();
        this.types = new ArrayList<>();
        this.data = new HashMap<>();
        try {
            this.indexes = new Indexes(columnDefinitionList.size());
        } catch (NullPointerException e) {
            throw new ExecutionException("Can't create empty table");
        }
        this.columnIndexes = new HashMap<>();
        this.uniqueSet = new ArrayList<>(); // candidate keys have not null uniqueSet items
        this.foreignKeyList = new ArrayList<>();

        Set<String> check = new HashSet<>(); //check duplication of column names

        for (ColumnDefinition def : columnDefinitionList) {
            // column name
            String columnName = def.getColumnName();
            if (!check.contains(columnName)) {
                check.add(columnName);
                columnNames.add(columnName);
                columnIndexes.put(columnName, columnNames.size() - 1);
//                indexes.getIndexNames().add(null);
//                indexes.getIndexes().add(null);
            } else {
                throw new ExecutionException("Duplicate column name");
            }
            // column data type
            String columnLowerCaseType = def.getColDataType().getDataType().toLowerCase();//string of type name
            if (columnLowerCaseType.compareTo("char") == 0 || columnLowerCaseType.compareTo("varchar") == 0) {
                types.add(Type.STRING);
            } else if (columnLowerCaseType.compareTo("int") == 0 || columnLowerCaseType.compareTo("integer") == 0 || columnLowerCaseType.compareTo("smallint") == 0) {
                types.add(Type.INT);
            } else {
                throw new ExecutionException("Wrong or unsupported data type.");
            }
            // column specs - only support unique
            uniqueSet.add(null);
            if (def.getColumnSpecs() != null
                    && def.getColumnSpecs().size() > 0
                    && def.getColumnSpecs().get(0).toLowerCase().compareTo("unique") == 0) {
                this.uniqueSet.set(columnIndexes.get(columnName), new HashSet<>());
            }
            this.returnValue = this;

        }
        //constraints: primary key, foreign key
        for (int i = 0; i < columnNames.size(); i++) {
            foreignKeyList.add(null);
        }
        if (createTableStatement.getIndexes() != null) {
            for (Index index : createTableStatement.getIndexes()) {
                //check primary key
                if (index.getType().toLowerCase().compareTo("primary key") == 0) {
                    primaryKey = columnIndexes.get(index.getColumnsNames().get(0));
                    if (uniqueSet.get(primaryKey) == null) {
                        uniqueSet.set(primaryKey, new HashSet<>()); // primary key should be unique
                    }
                }

                //check foreign key(s)
                if (index instanceof ForeignKeyIndex) {
                    int foreignKeyIndexHere = columnIndexes.get(index.getColumnsNames().get(0));
                    String foreignTableName = ((ForeignKeyIndex) index).getTable().getName();
                    String foreignKeyReferenced = ((ForeignKeyIndex) index).getReferencedColumnNames().get(0);
                    if (this.db == null
                            || this.db.getTable(foreignTableName) == null
                            || this.db.getTable(foreignTableName).columnIndexes.get(foreignKeyReferenced) == null) {
                        throw new ExecutionException("Foreign key no references");
                    }
                    int foreignKeyIndexReferenced = this.db.getTable(foreignTableName).columnIndexes.get(foreignKeyReferenced);
                    if (this.db.getTable(foreignTableName).uniqueSet == null) {
                        throw new ExecutionException("Foreign key not unique");
                    }
                    foreignKeyList.set(foreignKeyIndexHere, new Pair<String, Integer>(foreignTableName, foreignKeyIndexReferenced));
                }
            }
        }

    }

    //Constructors (without DB)

    public Table(String tableName, List<String> columnNames, Map<String, DataRow> data) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.data = data;
    }

    //Copying all the data to a new table object
    public Table(Table table) {
        this.simple = table.simple;
        this.db = table.db;
        this.tableName = table.tableName;
        this.columnNames = new ArrayList<>();
        this.columnNames.addAll(table.columnNames);
        this.columnIndexes = new HashMap<>();
        this.columnIndexes.putAll(table.columnIndexes);
        this.types = new ArrayList<>();
        this.types.addAll(table.types);
        this.data = new HashMap<>();
        for (Map.Entry<String, DataRow> entry : table.data.entrySet()) {
            this.data.put(entry.getKey(), entry.getValue().clone());
        }
        this.returnValue = null;
        this.indexes = table.indexes;

    }

    public Table(String str) {
        this.data = new HashMap<>();
        data.put("result", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(str)));
    }

    public Table(int n) {
        this.data = new HashMap<>();
        data.put("result", new DataRow(Arrays.asList(Type.INT), Arrays.asList(n)));
    }

    public Table(boolean bool) {
        this.data = new HashMap<>();
        data.put("result", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(bool ? "true" : "false")));
    }

    public Table(DataRow row) {
        this.data = new HashMap<>();
        data.put("result", row);
    }


    //setters and getters
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getColumnIndex(String columnName) {
        return this.columnIndexes.get(columnName);
    }

    public String getTableName() {
        return tableName;
    }

    public Table getReturnValue() {
        return returnValue;
    }

    public List<Type> getTypes() {
        return types;
    }

    //create index
    public boolean createIndex(String indexName, String columnName) {
        int colInd = columnIndexes.get(columnName); //get column index by column name

        if (colInd == primaryKey) {
            throw new ExecutionException("Can't create index on primary key.");
        }
        if (indexes.getIndexes().get(colInd) != null) { // judge if index already exists
            return false;
        }
        indexes.getIndexNames().set(colInd, indexName); // store index name
        Map<String, List<String>> curIndex = new HashMap<>();
        indexes.getIndexes().set(colInd, curIndex); // initialize new index object into table
        data.forEach((k, v) -> { // construct object
            String fieldValue = v.getDataGrids().get(colInd).toString();
            if (curIndex.containsKey(fieldValue)) {
                curIndex.get(fieldValue).add(k);
            } else {
                curIndex.put(fieldValue, new ArrayList<>(Arrays.asList(k)));
            }
        });
        return true;
    }

    @Override
    public void visit(CreateIndex createIndex) {
        this.returnValue = new Table(this.createIndex(createIndex.getIndex().getName(), //index name
                createIndex.getIndex().getColumnsNames().get(0))); // column name
    }

    @Override
    public void visit(Insert insert) {
        insert.getItemsList().accept(this);
        returnValue = insert(returnValue.data.values().iterator().next());
    }

    public Table insert(DataRow newRow) {
        if (newRow.getDataGrids().size() != this.columnNames.size()) { // check value count
            throw new ExecutionException("Value count does not match.");
        }
        if (this.data.containsKey(newRow.getDataGrids().get(this.primaryKey).toString())) { // check primary key
            throw new ExecutionException("Primary key already exists");
        }
        for (int i = 0; i < newRow.getDataGrids().size(); i++) { //check value one by one
            DataGrid dataGrid = newRow.getDataGrids().get(i);
            Map<String, List<String>> curIndex = indexes.getIndexes().get(i);
            if (types.get(i) == Type.INT) {  //check if it should be integer
                newRow.getDataGrids().set(i, new DataGrid(Type.INT, Integer.parseInt(dataGrid.toString())));
            }
            if (curIndex != null) {
                String fieldValue = dataGrid.toString();
                if (curIndex.containsKey(fieldValue)) {
                    curIndex.get(fieldValue).add(newRow.getDataGrids().get(primaryKey).toString());
                } else {
                    curIndex.put(fieldValue, new ArrayList<>(Arrays.asList(newRow.getDataGrids().get(primaryKey).toString())));
                }
            }
            if (foreignKeyList.get(i) != null) {  // check if it has foreign key constraint
                DataGrid refGrid = findReferenceGrid(foreignKeyList.get(i).getFirst(),
                        foreignKeyList.get(i).getSecond(), newRow.getDataGrids().get(i));
                if (refGrid == null) {
                    throw new ExecutionException("Failed by foreign key constraint");
                }
                newRow.getDataGrids().set(i, refGrid);
            }
        }
        this.data.put(newRow.getDataGrids().get(this.primaryKey).toString(), newRow); // write into main hashmap
        return new Table(true);
    }

    public DataGrid findReferenceGrid(String tableName, int colInd, DataGrid data) {
        Table table = db.getTable(tableName);

        if (colInd == table.primaryKey) { // if rely on primary key
            DataRow refRow = table.data.get(data.toString());
            return refRow == null ? null : refRow.getDataGrids().get(colInd);
        }
        Map<String, List<String>> index = table.indexes.getIndexes().get(colInd);
        if (index != null) {  // if column is indexed
            List<String> primaryKeyValue = index.get(data.toString());
            if (primaryKeyValue == null) {
                return null;
            }
            DataRow refRow = table.data.get(primaryKeyValue.get(0));
            return refRow == null ? null : refRow.getDataGrids().get(colInd);
        } else {
            for (DataRow value : table.data.values()) {
                DataGrid curGrid = value.getDataGrids().get(colInd);
                if (curGrid.compareTo(data)) {
                    return curGrid;
                }
            }
        }
        return null;
    }

    @Override
    public void visit(ExpressionList expressionList) {
        List<Expression> exprs = expressionList.getExpressions();
        List<Object> a = new ArrayList<>();
        exprs.forEach((k) -> {
            k.accept(this);
            a.add((Object) this.returnValue.data.get("result").getDataGrids().get(0).toString());
        });
        this.returnValue = new Table(new DataRow(Collections.nCopies(exprs.size(), Type.STRING), a));
    }

    @Override
    public void visit(Select select) {
        select.getSelectBody().accept(this);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        this.returnValue = new Table(((Column) selectExpressionItem.getExpression()).getColumnName());
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        Table table = new Table();
        table.setTableName(this.tableName);
        table.simple = false;
        List<Integer> columnIndexFromOrigin = new ArrayList<>();

        //copying table object and re-construct its meta info.
        int cnt = 0;
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            // not using recursive accept because *(all columns) is already atomic.
            // will immediately break from loop and only return itself.
            if (selectItem instanceof AllColumns) {
                table = this;
                break;
            }
            selectItem.accept(this);
            String columnName = this.returnValue.data.get("result").getDataGrids().get(0).getData().toString();
            if (!this.columnIndexes.containsKey(columnName)) {
                throw new ExecutionException(columnName + " doesn't exist");
            }
            table.columnNames.add(columnName);
            table.columnIndexes.put(columnName, cnt++);
            int idx = this.columnIndexes.get(columnName);
            columnIndexFromOrigin.add(idx);
            table.types.add(this.types.get(idx));
        }

        //The case where table is actually re-constructed, copying its data to table
        if (table != this) {
            table.data = new HashMap<>();
            for (Map.Entry<String, DataRow> entry : this.data.entrySet()) {
                List<DataGrid> dataList = new ArrayList<>();
                for (int idx : columnIndexFromOrigin) {
                    dataList.add(entry.getValue().getDataGrids().get(idx));
                }
                DataRow dataRow = new DataRow(dataList);
                table.data.put(entry.getKey(), dataRow);
            }
        }

        //processing where statement
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(table);
            table = table.returnValue;
        }
        this.returnValue = table;
    }

    @Override
    public void visit(AndExpression andExpression) {
        /*
          recursively calculate expressions
          */
        Table table_l = new Table(this);
        andExpression.getLeftExpression().accept(table_l);
        table_l = table_l.getReturnValue();
        Table table_r = new Table(this);
        andExpression.getRightExpression().accept(table_r);
        table_r = table_r.getReturnValue();

        /*
         * logically and two tables, left combine, so traversing table_l is faster.
         * */
        Iterator<String> iterator = table_l.data.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (!table_r.data.containsKey(next)) {
//                table_l.data.remove(next);
                iterator.remove();
            }
        }
        this.returnValue = table_l;
    }

    @Override
    public void visit(OrExpression orExpression) {
        /*
         * recursivelly calculate expressions
         * */
        Table table_l = new Table(this);
        orExpression.getLeftExpression().accept(table_l);
        table_l = table_l.getReturnValue();
        Table table_r = new Table(this);
        orExpression.getRightExpression().accept(table_r);
        table_r = table_r.getReturnValue();


        /*
         * logically or two tables, left combine, so traversing table_r is faster.
         * */
        for (Map.Entry<String, DataRow> entry : table_r.data.entrySet()) {
            if (!table_l.data.containsKey(entry.getKey())) {
                table_l.data.put(entry.getKey(), entry.getValue());
            }
        }
        this.returnValue = table_l;
    }

    @Override
    public void visit(Column tableColumn) {
        String columnName = tableColumn.getColumnName();
        Table table = new Table();
        table.columnNames.add(columnName);
        table.columnIndexes.put(columnName, 0);
        for (Map.Entry<String, DataRow> rowEntry : this.data.entrySet()) {
            DataRow dataRow = new DataRow(Arrays.asList(
                    rowEntry.getValue().getDataGrids().get(this.columnIndexes.get(columnName))));
            table.data.put(rowEntry.getKey(), dataRow);
        }
        this.returnValue = table;
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        Expression leftExpression = equalsTo.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = equalsTo.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                if (!dataGrid_l.compareTo(dataGrid_r)) {
                    this.data = new HashMap<>();
                }
            } else {
                // The case that only one side has result
                if (table_l.data.containsKey("result")) {
                    //Swap so table_l is always column
                    Table table_tmp = table_l;
                    table_l = table_r;
                    table_r = table_tmp;
                }
                DataGrid dataGrid = table_r.data.get("result").getDataGrids().get(0);
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    if (!dataGrid.compareTo(rowEntry.getValue().getDataGrids().get(0))) {
                        this.data.remove(rowEntry.getKey());
                    }
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    if (!dataGrid_l.compareTo(dataGrid_r)) {
                        this.data.remove(rowEntry.getKey());
                    }
                }
            }
        }
        this.returnValue = this;
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
    }

    @Override
    public void visit(MinorThan minorThan) {

    }

    @Override
    public void visit(GreaterThan greaterThan) {

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        super.visit(minorThanEquals);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {

    }

    @Override
    public void visit(Addition addition) {

    }

    @Override
    public void visit(Subtraction subtraction) {

    }

    @Override
    public void visit(Multiplication multiplication) {

    }

    @Override
    public void visit(Division division) {

    }

    @Override
    public void visit(Drop drop) {
        if (drop.getType().compareTo("index") == 0) {
            String indexName = drop.getName().getName();
            int index = indexes.getIndexNames().indexOf(indexName);
            if (index == -1) {
                throw new ExecutionException("No such index");
            }
            indexes.getIndexes().set(index, null);
            indexes.getIndexNames().set(index, null);
            this.returnValue = new Table(true);
        } else {
            throw new ExecutionException("Not Implemented yet");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (simple) {
            sb.append(this.data.toString()).append("\n");
        } else {
            sb.append(this.tableName).append("\n")
                    .append(this.columnNames.toString()).append("\n")
                    .append(this.data.toString()).append("\n");
        }
        return new String(sb);
    }

    @Override
    public void visit(LongValue longValue) {
        this.returnValue = new Table((int) longValue.getValue());
    }

    @Override
    public void visit(StringValue stringValue) {
        this.returnValue = new Table(stringValue.getValue());
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    public static void main(String[] args) {
        String selectDemo1 = "SELECT DISTINCT(c.address), c.date FROM customer c\n";
        try {
            Statement selectStmt = CCJSqlParserUtil.parse(selectDemo1);
//            Table table = new Table("test");
//            table.visit((Select) selectStmt);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }
}
