package POJO;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
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
import utils.SyntaxException;


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
    private List<String> columnNames;
    private Map<String, Integer> columnIndexes;
    private List<Type> types;

    public Map<String, DataRow> getData() {
        return data;
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
        this(false, null, null, new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), null, new Indexes(0), null, new ArrayList<>(), new ArrayList<>());

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

    public Table(Database db, String tableName) {//TODO:这个不是说不要了吗？najiu buyao le ba
        this.db = db;
        this.tableName = tableName;
        this.data = new HashMap<>();
        data.put("result", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(tableName)));
    }

    public Table(Database db, CreateTable createTableStatement) throws SyntaxException {
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
            throw new SyntaxException("Can't create empty table");
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
                throw new SyntaxException("Duplicate column name");
            }
            // column data type
            String columnLowerCaseType = def.getColDataType().getDataType().toLowerCase();//string of type name
            if (columnLowerCaseType.compareTo("char") == 0 || columnLowerCaseType.compareTo("varchar") == 0) {
                types.add(Type.STRING);
            } else if (columnLowerCaseType.compareTo("int") == 0 || columnLowerCaseType.compareTo("integer") == 0 || columnLowerCaseType.compareTo("smallint") == 0) {
                types.add(Type.INT);
            } else {
                throw new SyntaxException("Wrong or unsupported data type.");
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
                        throw new SyntaxException("Foreign key no references");
                    }
                    int foreignKeyIndexReferenced = this.db.getTable(foreignTableName).columnIndexes.get(foreignKeyReferenced);
                    if (this.db.getTable(foreignTableName).uniqueSet == null) {
                        throw new SyntaxException("Foreign key not unique");
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

    public Table(Table table) {//TODO: copy other tables, deep copy
        this.simple = table.simple;
        this.db = table.db;
        this.tableName = table.tableName;
        this.columnNames = new ArrayList<>();
        this.columnNames.addAll(table.columnNames);
        this.columnIndexes=new HashMap<>();
        this.columnIndexes.putAll(table.columnIndexes);


    }

    public Table(String str) {
        this.data = new HashMap<>();
        data.put("result", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(str)));
    }

    //indicates transferring a column name
    public Table(Column column) {
        this.data = new HashMap<>();
        data.put("column", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(column.getColumnName())));
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

    //create index
    public boolean createIndex(String indexName, String columnName) {
        int colInd = columnIndexes.get(columnName); //get column index by column name

        if (colInd == primaryKey) {
            throw new SyntaxException("Can't create index on primary key.");
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
            throw new SyntaxException("Value count does not match.");
        }
        if (this.data.containsKey(newRow.getDataGrids().get(this.primaryKey).toString())) { // check primary key
            throw new SyntaxException("Primary key already exists");
        }
        for (int i = 0; i < newRow.getDataGrids().size(); i++) { //check value one by one
            DataGrid dataGrid = newRow.getDataGrids().get(i);
            Map<String, List<String>> curIndex= indexes.getIndexes().get(i);
            if (types.get(i) == Type.INT) {  //check if it should be integer
                newRow.getDataGrids().set(i, new DataGrid(Type.INT, Integer.parseInt(dataGrid.toString())));
            }
            if (curIndex!=null){
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
                    throw new SyntaxException("Failed by foreign key constraint");
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
            List<String> primaryKeyValue =index.get(data.toString());
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
        SelectBody selectBody = select.getSelectBody();

        //plain select without where statement for now
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            Table table = new Table(this);
            table.columnNames = new ArrayList<>();
            table.columnIndexes = new HashMap<>();
            table.types = new ArrayList<>();
            ArrayList<Integer> columnList = new ArrayList<>();
            int cnt = 0;
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                String columnName = ((Column) ((SelectExpressionItem) selectItem).getExpression()).getColumnName();
                table.columnNames.add(columnName);
                table.columnIndexes.put(columnName, cnt++);
                int idx = this.columnIndexes.get(columnName);
                columnList.add(idx);
                table.types.add(this.types.get(idx));
            }
            table.data = new HashMap<>();
            for (Map.Entry<String, DataRow> entry : this.data.entrySet()) {
                List<Object> dataList = new ArrayList<>();
                for (int idx : columnList) {
                    dataList.add(entry.getValue().getDataGrids().get(idx));
                }
                DataRow dataRow = new DataRow(table.types, dataList);
                table.data.put(entry.getKey(), dataRow);
            }
            this.returnValue = table;
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        /*
          recursively calculate expressions
          */
        Expression leftExpression = andExpression.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.getReturnValue();
        Expression rightExpression = andExpression.getRightExpression();
        rightExpression.accept(this);
        Table table_r = new Table(this.getReturnValue());

        /*
         * logically and two tables
         * */
        Iterator<String> iterator=table_l.data.keySet().iterator();
        
        for (Map.Entry<String, DataRow> entry : table_l.data.entrySet()) {
            if (!table_r.data.containsKey(entry.getKey())) {
                table_l.data.remove(entry.getKey());
            }
        }

        this.returnValue = table_l;
    }

    @Override
    public void visit(OrExpression orExpression) {
        /*
         * recursivelly calculate expressions
         * */
        Expression leftExpression = orExpression.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.getReturnValue();
        Expression rightExpression = orExpression.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.getReturnValue();

        /*
         * logically or two tables
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
        this.returnValue = new Table(tableColumn);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        Expression leftExpression = equalsTo.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = equalsTo.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;
        if (table_l.data.containsKey("column") && table_r.data.containsKey("column")) {
            //The case that where consists of two columns
            String columnName_l = table_l.data.get("column").getDataGrids().get(0).toString();
            String columnName_r = table_r.data.get("column").getDataGrids().get(0).toString();
            if (columnName_l.compareTo(columnName_r) != 0) {
                this.data = new HashMap<>();
            }
            this.returnValue = this;
        } else if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
            // The case that where consists of one column and one data
            DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
            DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
            if (!dataGrid_l.compareTo(dataGrid_r)) {
                this.data = new HashMap<>();
            }
            this.returnValue = this;
        } else {
            // The case that where consists of one column and one data
            if (table_r.data.containsKey("column")) {
                //Swap so table_l is always column
                Table table_tmp = table_l;
                table_l = table_r;
                table_r = table_tmp;
            }
            String columnName = table_l.data.get("column").getDataGrids().get(0).toString();
            if (!this.columnIndexes.containsKey(columnName)) {
                throw new ExecutionException(columnName + " doesn't exist in " + table_l.getTableName());
            }
            int idx = this.columnIndexes.get(columnName);

            DataGrid dataGrid = table_r.data.get("result").getDataGrids().get(0);

            for (Map.Entry<String, DataRow> entry : this.data.entrySet()) {
                DataRow dataRow = entry.getValue();
                if (!dataRow.getDataGrids().get(idx).compareTo(dataGrid)) {
                    //remove datarow from this table if datagrid is not equal
                    this.data.remove(entry.getKey());
                }
            }
            this.returnValue = this;
        }
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        super.visit(notEqualsTo);
    }

    @Override
    public void visit(MinorThan minorThan) {
        super.visit(minorThan);
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
    public void visit(Drop drop) {
        if (drop.getType().compareTo("index") == 0) {
            String indexName = drop.getName().getName();
            int index = indexes.getIndexNames().indexOf(indexName);
            if (index == -1) {
                throw new SyntaxException("No such index");
            }
            indexes.getIndexes().set(index, null);
            indexes.getIndexNames().set(index, null);
            this.returnValue = new Table(true);
        } else {
            throw new SyntaxException("Not Implemented yet");
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
