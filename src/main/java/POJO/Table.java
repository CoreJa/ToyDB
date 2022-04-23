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
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import utils.ExecuteEngine;
import utils.SyntaxException;


import javax.swing.table.TableModel;
import java.io.Serializable;
import java.util.*;

public class Table extends ExecuteEngine implements Serializable {
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
    public Table(Database db, String tableName) {
        this.db = db;
        this.tableName = tableName;
        this.data = new HashMap<>();
        data.put("result", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(tableName)));
    }

    public Table(Database db, CreateTable createTableStatement) throws SyntaxException {
        // create table by statement
        // define the name and dataType of each column
        List<ColumnDefinition> columnDefinitionList = createTableStatement.getColumnDefinitions();

        this.db = db;
        this.tableName = createTableStatement.getTable().getName();
        this.columnNames = new ArrayList<>();
        this.types = new ArrayList<>();
        this.indexes = new Indexes(columnDefinitionList.size());
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
            if(def.getColumnSpecs()!= null
                    && def.getColumnSpecs().size() > 0
                    && def.getColumnSpecs().get(0).toLowerCase().compareTo("unique") == 0) {
                this.uniqueSet.set(columnIndexes.get(columnName), new HashSet<>());
            }

        }
        //constraints: primary key, foreign key
        for(int i = 0; i < columnNames.size(); i++) {
            foreignKeyList.add(null);
        }
        for (Index index : createTableStatement.getIndexes()) {
            //check primary key
            if (index.getType().toLowerCase().compareTo("primary key") == 0) {
                primaryKey = columnIndexes.get(index.getColumnsNames().get(0));
                if(uniqueSet.get(primaryKey) == null) {
                    uniqueSet.set(primaryKey, new HashSet<>()); // primary key should be unique
                }
            }

            //check foreign key(s)
            if (index instanceof ForeignKeyIndex) {
                int foreignKeyIndexHere = columnIndexes.get(index.getColumnsNames().get(0));
                String foreignTableName = ((ForeignKeyIndex) index).getTable().getName();
                String foreignKeyReferenced = ((ForeignKeyIndex) index).getReferencedColumnNames().get(0);
                if(this.db == null 
                        || this.db.getTable(foreignTableName) == null
                        || this.db.getTable(foreignTableName).columnIndexes.get(foreignKeyReferenced) == null) {
                    throw new SyntaxException("Foreign key no references");
                }
                int foreignKeyIndexReferenced = this.db.getTable(foreignTableName).columnIndexes.get(foreignKeyReferenced);
                if(this.db.getTable(foreignTableName).uniqueSet == null) {
                    throw new SyntaxException("Foreign key not unique");
                }
                foreignKeyList.set(foreignKeyIndexHere, new Pair<String, Integer>(foreignTableName, foreignKeyIndexReferenced));
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

    public Table insert(DataRow newRow){
        if (newRow.getDataGrids().size() != this.columnNames.size()) {
            throw new SyntaxException("Value count does not match.");
        }
        for (int i = 0; i < newRow.getDataGrids().size(); i++) {
            DataGrid dataGrid = newRow.getDataGrids().get(i);
            if (foreignKeyList.get(i) != null) {

            } else if (types.get(i) == Type.INT) {
                newRow.getDataGrids().set(i, new DataGrid(Type.INT, Integer.parseInt(dataGrid.toString())));
            }
        }
        return new Table(true);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        HashMap<String, DataRow> newData = new HashMap<>();
        List<Expression> exprs = expressionList.getExpressions();
        List<Object> a = new ArrayList<>();
        exprs.forEach((k) -> {
            k.accept(this);
            a.add((Object) this.returnValue.data.get("result").getDataGrids().get(0).toString());
        });
        newData.put("result", new DataRow(Collections.nCopies(exprs.size(), Type.STRING), a));
    }

    @Override
    public void visit(Column tableColumn) {
        this.returnValue = new Table(tableColumn.getColumnName());
    }

    @Override
    public void visit(Select select) {
        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            Expression expression = plainSelect.getWhere();

        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        Expression leftExpression = andExpression.getLeftExpression();
        leftExpression.accept(this);
        Table table = this.getReturnValue();
        table.data.get("result").getDataGrids().get(0).toString().equals("true");
        Expression rightExpression = andExpression.getRightExpression();
        rightExpression.accept(this);

    }

    @Override
    public void visit(OrExpression orExpression) {

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
    public void visit(InExpression inExpression) {

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
        sb.append(this.tableName).append("\n")
                .append(this.columnNames.toString()).append("\n")
                .append(this.data.toString()).append("\n");
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
