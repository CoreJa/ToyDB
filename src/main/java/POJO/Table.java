package POJO;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import utils.ExecuteEngine;
import utils.SyntaxException;


import javax.swing.table.TableModel;
import javax.xml.crypto.Data;
import java.io.Serializable;
import java.util.*;

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
    private Map<String, DataRow> data;//key: primary key; value: data record
    private Table returnValue;
    // Indexes of all columns, elements corresponding to PrimaryKey and None Indexed columns should be Null.
    private Indexes indexes;
    //Constraints
    private Integer primaryKey; // index in columnNames
    private List<Set<String>> uniqueSet; // maintain a HashSet of value of . Always cast to String.
    private List<Pair<String, Integer>> foreignKeyList;

    //Constructors (with DB)
    public Table() {
        this(null, null, new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), null, null, null, new ArrayList<>(), new ArrayList<>());

    }

    public Table(Database db, String tableName, List<String> columnNames, Map<String, Integer> columnIndexes, List<Type> types, Map<String, DataRow> data, Table returnValue, Indexes indexes, Integer primaryKey, List<Set<String>> uniqueSet, List<Pair<String, Integer>> foreignKeyList) {
        this.simple = false;
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

    public Table(Database db, String tableName) { // ???
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
        try {
            this.indexes = new Indexes(columnDefinitionList.size());
        } catch (NullPointerException e) {
            throw new SyntaxException("Can't create empty table");
        }
        this.columnIndexes = new HashMap<>();
        this.uniqueSet = new ArrayList<>();
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
            // column specs - unique
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
        this.tableName = table.tableName;
        this.columnNames = table.columnNames;

    }

    public Table(String tableName) {
        this.data = new HashMap<>();
        data.put("result", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(tableName)));
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
            if (types.get(i) == Type.INT) {  //check if it should be integer
                newRow.getDataGrids().set(i, new DataGrid(Type.INT, Integer.parseInt(dataGrid.toString())));
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
            DataRow refRow = table.data.get(index.get(data.toString()).get(0));
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
    public void visit(Column tableColumn) {
        this.returnValue = new Table(tableColumn.getColumnName());
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
         * recursivelly calculate expressions
         * */
        Expression leftExpression = andExpression.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.getReturnValue();
        Expression rightExpression = andExpression.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.getReturnValue();

        /*
         * logically and two tables
         * */
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
    public void visit(Between between) {

    }

    @Override
    public void visit(EqualsTo equalsTo) {

    }

    @Override
    public void visit(GreaterThan greaterThan) {

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
