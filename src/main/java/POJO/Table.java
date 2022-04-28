package POJO;


import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import utils.ExecuteEngine;
import utils.ExecutionException;


import java.io.Serializable;
import java.util.*;

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
        this(true, null, "", new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), null, new Indexes(0), null, new ArrayList<>(), new ArrayList<>());

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
        //will shallow copy keys
        this.primaryKey = table.primaryKey;
        this.uniqueSet = table.uniqueSet;
        this.foreignKeyList = table.foreignKeyList;

    }

    //Coping without meta
    public Table(Map<String, DataRow> data) {
        this();
        for (Map.Entry<String, DataRow> entry : data.entrySet()) {
            this.data.put(entry.getKey(), entry.getValue().clone());
        }
    }

    public Table(String str) {
        this.data = new HashMap<>();
        data.put("result", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(str)));
    }

    public Table(String str1, String str2) {
        this.data = new HashMap<>();
        data.put("result1", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(str1)));
        data.put("result2", new DataRow(Arrays.asList(Type.STRING), Arrays.asList(str2)));
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

    public Indexes getIndexes() {
        return indexes;
    }

    //setters and getters
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getColumnIndex(String columnName) {
        if (!this.columnIndexes.containsKey(columnName)) {
            throw new ExecutionException(columnName + " doesn't exist in table " + this.getTableName());
        }
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

    public void setTypes(List<Type> types) {
        this.types = types;
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
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
        Map<String, Set<String>> curIndex = new HashMap<>();
        indexes.getIndexes().set(colInd, curIndex); // initialize new index object into table
        data.forEach((k, v) -> { // construct object
            String fieldValue = v.getDataGrids().get(colInd).toString();
            if (curIndex.containsKey(fieldValue)) {
                curIndex.get(fieldValue).add(k);
            } else {
                curIndex.put(fieldValue, new HashSet<>(Arrays.asList(k)));
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
            Map<String, Set<String>> curIndex = indexes.getIndexes().get(i);
            if (types.get(i) == Type.INT) {  //check if it should be integer
                newRow.getDataGrids().set(i, new DataGrid(Type.INT, Integer.parseInt(dataGrid.toString())));
            }
            if (curIndex != null) {
                String fieldValue = dataGrid.toString();
                if (curIndex.containsKey(fieldValue)) {
                    curIndex.get(fieldValue).add(newRow.getDataGrids().get(primaryKey).toString());
                } else {
                    curIndex.put(fieldValue, new HashSet<>(Arrays.asList(newRow.getDataGrids().get(primaryKey).toString())));
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
        Map<String, Set<String>> index = table.indexes.getIndexes().get(colInd);
        if (index != null) {  // if column is indexed
            Set<String> primaryKeyValue = index.get(data.toString());
            if (primaryKeyValue == null) {
                return null;
            }
            DataRow refRow = table.data.get(primaryKeyValue.iterator().next());
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
    public void visit(Update update) {
        ArrayList<UpdateSet> updateSets = update.getUpdateSets();
        ArrayList<Pair<Integer, Expression>> ops = new ArrayList<>();
        for (UpdateSet set : updateSets) {
            set.getColumns().get(0).accept(this);
            String colName = returnValue.columnNames.get(0);
            int colInd = this.getColumnIndex(colName);
            ops.add(new Pair<>(colInd, set.getExpressions().get(0)));
        }
        if (update.getWhere() != null) { // has where
            update.getWhere().accept(this);
            if (this.returnValue.data == null) {
                throw new ExecutionException("No such row.");
            }
            for (Pair<Integer, Expression> op : ops) {
                int colInd = op.getFirst();
                if (colInd == primaryKey) { // if pk
                    throw new ExecutionException("Primary key is not modifiable");
                }
                op.getSecond().accept(returnValue);
                Map<String, DataRow> res = returnValue.returnValue.data;
                if (res.size() == 1) {
                    Object val = res.values().iterator().next().getDataGrids().get(0).getData();
                    DataGrid valGrid = res.values().iterator().next().getDataGrids().get(0);
                    for (String s : returnValue.data.keySet()) {
                        update(colInd, s, val, valGrid);
                    }
                } else {
                    for (String s : returnValue.data.keySet()) {
                        Object val = res.get(s).getDataGrids().get(0).getData();
                        DataGrid valGrid = res.get(s).getDataGrids().get(0);
                        update(colInd, s, val, valGrid);
                    }
                }
            }
        } else { // UPDATE ALL !!!
            for (Pair<Integer, Expression> op : ops) {
                int colInd = op.getFirst();
                op.getSecond().accept(this);
                Map<String, DataRow> res = returnValue.data;
                if (res.size() == 1) {
                    Object val = res.get("result").getDataGrids().get(0).getData();
                    DataGrid valGrid = res.get("result").getDataGrids().get(0);
                    for (String s : data.keySet()) {
                        update(colInd, s, val, valGrid);
                    }
                } else {
                    for (String s : data.keySet()) {
                        Object val = res.get(s).getDataGrids().get(0).getData();
                        DataGrid valGrid = res.get(s).getDataGrids().get(0);
                        update(colInd, s, val, valGrid);
                    }
                }
            }
        }
        this.returnValue = new Table("True");
    }

    private void update(int colInd, String s, Object val, DataGrid valGrid) {
        if (foreignKeyList.get(colInd) != null) { //check fk
            DataGrid ref = findReferenceGrid(foreignKeyList.get(colInd).getFirst(), foreignKeyList.get(colInd).getSecond(), valGrid);
            if (ref == null) {
                throw new ExecutionException("Failed by foreign key constraint.");
            }
            updateIndex(colInd, s, val);
            this.data.get(s).getDataGrids().set(colInd, ref);
        } else {
            updateIndex(colInd, s, val);
            this.data.get(s).getDataGrids().get(colInd).setData(val);
        }

    }

    private void updateIndex(int colInd, String s, Object val) {
        Map<String, Set<String>> index = indexes.getIndexes().get(colInd);
        if (index != null) { //update index
            if (index.containsKey(val.toString())) {
                index.get(val.toString()).add(s);
            } else {
                index.put(val.toString(), new HashSet<>(Arrays.asList(s)));
            }
            Set<String> hits = index.get(data.get(s).getDataGrids().get(colInd).toString());
            hits.remove(s);
            if (hits.size() == 0) {
                index.remove(data.get(s).getDataGrids().get(colInd).toString());
            }
        }
    }

    @Override
    public void visit(Delete delete) {
        if (delete.getWhere() != null) { // has where
            delete.getWhere().accept(this);
            for (String s : returnValue.data.keySet()) {
                delete(s);
            }
        } else { // DELETE ALL !!!
            for (String s : this.data.keySet()) {
                delete(s);
            }
        }
        this.returnValue = new Table("True");
    }

    private void delete(String s) {
        for (int i = 0; i < this.indexes.getIndexes().size(); i++) {
            Map<String, Set<String>> index = this.indexes.getIndexes().get(i);
            if (index != null) {
                index.get(data.get(s).getDataGrids().get(i).toString()).remove(s);
            }
        }
        this.data.get(s).getDataGrids().forEach(x -> x.setData(null));
        this.data.remove(s);
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
        this.returnValue = new Table(((Column) selectExpressionItem.getExpression()).toString());
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        Table table = new Table(this);
        //processing where statement
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(table);
            table = table.returnValue;
        }

        //TODO: order by limit distinct
        if (plainSelect.getDistinct() != null) { // if distinct
            HashSet<String> set = new HashSet<>();
            Map<String, DataRow> data = table.getData();
            Iterator<String> iterator = data.keySet().iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                String curVal = table.getData().get(next).getDataGrids().stream().map(DataGrid::toString).reduce("", (x, y) -> x + " # " + y);
                if (!set.add(curVal)) {
                    iterator.remove();
                }
            }
        }

        if (plainSelect.getOrderByElements() != null) { // if order by
            OrderByElement element = plainSelect.getOrderByElements().get(0);
            Database temp = new Database();
            element.getExpression().accept(temp);
            String colName = (String) temp.getReturnValue().getData().get("result").getDataGrids().get(0).getData();
            int colInd = table.getColumnIndex(colName); // get column index
            List<Map.Entry<String, DataRow>> list = new ArrayList<>(table.getData().entrySet()); //construct list from table
            if (table.getTypes().get(colInd) == Type.STRING) { // sort the list.
                list.sort((o1, o2) -> ((String) o2.getValue().getDataGrids().get(colInd).getData())
                        .compareTo((String) o1.getValue().getDataGrids().get(colInd).getData()));
            } else {
                list.sort((o1, o2) -> (int) o2.getValue().getDataGrids().get(colInd).getData()
                        - (int) o1.getValue().getDataGrids().get(colInd).getData());
            }
            if (element.isAsc()) { // Ascending or Descending
                if (plainSelect.getLimit() != null) { // if limit
                    plainSelect.getLimit().getRowCount().accept(table);
                    int lim = (int) table.getReturnValue().getData().get("result").getDataGrids().get(0).getData(); //get lim count
                    if (list.size() > lim) {
                        list = list.subList(list.size() - lim, list.size());
                    }
                }
                Collections.reverse(list);
            } else {
                if (plainSelect.getLimit() != null) { // if limit
                    plainSelect.getLimit().getRowCount().accept(table);
                    int lim = (int) table.getReturnValue().getData().get("result").getDataGrids().get(0).getData(); //get lim count
                    if (list.size() > lim) {
                        list = list.subList(0, lim);
                    }
                }
            }
            Map<String, DataRow> orderedMap = new LinkedHashMap<>();
            for (Map.Entry<String, DataRow> entry : list) { // write into a hashmap that preserves order
                orderedMap.put(entry.getKey(), entry.getValue());
            }
            table.setData(orderedMap);
        } else {
            if (plainSelect.getLimit() != null) { // if limit
                plainSelect.getLimit().getRowCount().accept(table);
                int lim = (int) table.getReturnValue().getData().get("result").getDataGrids().get(0).getData(); //get lim count
                if (table.getData().size() > lim) {
                    int cur = 0;
                    Map<String, DataRow> data = table.getData();
                    Iterator<Map.Entry<String, DataRow>> iterator = table.getData().entrySet().iterator();
                    Map<String, DataRow> newdata = new HashMap<>();
                    while (iterator.hasNext() && cur < lim) {
                        Map.Entry<String, DataRow> next = iterator.next();
                        cur++;
                        newdata.put(next.getKey(), next.getValue());
                    }
                    table.setData(newdata);
                }
            }
        }

        Table res = new Table();
        res.setTableName(table.tableName);
        res.simple = false;
        List<Integer> columnIndexFromOrigin = new ArrayList<>();

        //copying table object and re-construct its meta info.
        int cnt = 0;
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            // not using recursive accept because *(all columns) is already atomic.
            // will immediately break from loop and only return itself.
            if (selectItem instanceof AllColumns) {
                res = table;
                break;
            }
            selectItem.accept(table);
            String columnName = table.returnValue.data.get("result").getDataGrids().get(0).getData().toString();
            if (!table.columnIndexes.containsKey(columnName)) {
                throw new ExecutionException(columnName + " doesn't exist");
            }
            res.columnNames.add(columnName);
            res.columnIndexes.put(columnName, cnt++);
            int idx = table.columnIndexes.get(columnName);
            columnIndexFromOrigin.add(idx);
            res.types.add(table.types.get(idx));
        }

        //The case where table is actually re-constructed, copying its data to table
        if (res != table) {
            res.data = new LinkedHashMap<>();
            for (Map.Entry<String, DataRow> entry : table.data.entrySet()) {
                List<DataGrid> dataList = new ArrayList<>();
                for (int idx : columnIndexFromOrigin) {
                    dataList.add(entry.getValue().getDataGrids().get(idx));
                }
                DataRow dataRow = new DataRow(dataList);
                res.data.put(entry.getKey(), dataRow);
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(AndExpression andExpression) {
        /*
          recursively calculate expressions
          */
        andExpression.getLeftExpression().accept(this);
        Table table_l = this.returnValue;
        andExpression.getRightExpression().accept(this);
        Table table_r = this.returnValue;

        /*
         * logically and two tables, left combine, so traversing table_l is faster.
         * */
        Table res = new Table(table_l);
        Iterator<String> iterator = res.data.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (!table_r.data.containsKey(next)) {
                iterator.remove();
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(OrExpression orExpression) {
        /*
         * recursivelly calculate expressions
         * */
        orExpression.getLeftExpression().accept(this);
        Table table_l = this.returnValue;
        orExpression.getRightExpression().accept(this);
        Table table_r = this.returnValue;


        /*
         * logically or two tables, left combine, so traversing table_r is faster.
         * */
        Table res = new Table(table_r);
        for (Map.Entry<String, DataRow> entry : res.data.entrySet()) {
            if (!table_l.data.containsKey(entry.getKey())) {
                res.data.put(entry.getKey(), entry.getValue());
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(Column tableColumn) {
        String columnName = tableColumn.getColumnName();
        Table table = new Table();
        table.columnNames.add(columnName);
        table.columnIndexes.put(columnName, 0);
        table.indexes.getIndexes().add(this.indexes.getIndexes().get(this.getColumnIndex(columnName)));
        for (Map.Entry<String, DataRow> rowEntry : this.data.entrySet()) {
            Integer colInd = this.columnIndexes.get(columnName);
            if (colInd == null) {
                colInd = this.getColumnIndex(tableColumn.getTable().getName() + "." + columnName);
            }
            DataRow dataRow = new DataRow(Arrays.asList(
                    rowEntry.getValue().getDataGrids().get(colInd)));
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

        Table res = new Table(this);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                if (!dataGrid_l.compareTo(dataGrid_r)) {
                    res.data = new HashMap<>();
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
                Map<String, Set<String>> index=table_l.indexes.getIndexes().get(0);
                if (index!=null){
                    Map<String, DataRow> newData=new HashMap<>();
                    Set<String> hits=index.get(dataGrid.toString());
                    for (String hit : hits) {
                        newData.put(hit,res.data.get(hit));
                    }
                    res.data=newData;
                }else {
                    for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                        if (!dataGrid.compareTo(rowEntry.getValue().getDataGrids().get(0))) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.size() == 0 || table_r.columnNames.size() == 0 ||
                    table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    if (!dataGrid_l.compareTo(dataGrid_r)) {
                        res.data.remove(rowEntry.getKey());
                    }
                }
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        Expression leftExpression = notEqualsTo.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = notEqualsTo.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                if (dataGrid_l.compareTo(dataGrid_r)) {
                    res.data = new HashMap<>();
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
                    if (dataGrid.compareTo(rowEntry.getValue().getDataGrids().get(0))) {
                        res.data.remove(rowEntry.getKey());
                    }
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.size() == 0 || table_r.columnNames.size() == 0 ||
                    table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    if (dataGrid_l.compareTo(dataGrid_r)) {
                        res.data.remove(rowEntry.getKey());
                    }
                }
            } else {
                //the case where left column and right column are the same
                res.data = new HashMap<>();
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(MinorThan minorThan) {
        Expression leftExpression = minorThan.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = minorThan.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                if (!dataGrid_l.minorThan(dataGrid_r)) {
                    res.data = new HashMap<>();
                }
            } else {
                // The case that only one side has result
                if (table_l.data.containsKey("result")) {
                    DataGrid dataGrid = table_l.data.get("result").getDataGrids().get(0);
                    for (Map.Entry<String, DataRow> rowEntry : table_r.data.entrySet()) {
                        if (!dataGrid.minorThan(rowEntry.getValue().getDataGrids().get(0))) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                } else {
                    DataGrid dataGrid = table_r.data.get("result").getDataGrids().get(0);
                    for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                        if (!rowEntry.getValue().getDataGrids().get(0).minorThan(dataGrid)) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.size() == 0 || table_r.columnNames.size() == 0 ||
                    table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    if (!dataGrid_l.minorThan(dataGrid_r)) {
                        res.data.remove(rowEntry.getKey());
                    }
                }
            } else {
                //the case where left column and right column are the same
                res.data = new HashMap<>();
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        Expression leftExpression = greaterThan.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = greaterThan.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                if (!dataGrid_l.greatThan(dataGrid_r)) {
                    res.data = new HashMap<>();
                }
            } else {
                // The case that only one side has result
                if (table_l.data.containsKey("result")) {
                    DataGrid dataGrid = table_l.data.get("result").getDataGrids().get(0);
                    for (Map.Entry<String, DataRow> rowEntry : table_r.data.entrySet()) {
                        if (!dataGrid.greatThan(rowEntry.getValue().getDataGrids().get(0))) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                } else {
                    DataGrid dataGrid = table_r.data.get("result").getDataGrids().get(0);
                    for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                        if (!rowEntry.getValue().getDataGrids().get(0).greatThan(dataGrid)) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.size() == 0 || table_r.columnNames.size() == 0 ||
                    table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    if (!dataGrid_l.greatThan(dataGrid_r)) {
                        res.data.remove(rowEntry.getKey());
                    }
                }
            } else {
                res.data = new HashMap<>();
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        Expression leftExpression = minorThanEquals.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = minorThanEquals.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                if (dataGrid_l.greatThan(dataGrid_r)) {
                    res.data = new HashMap<>();
                }
            } else {
                // The case that only one side has result
                if (table_l.data.containsKey("result")) {
                    DataGrid dataGrid = table_l.data.get("result").getDataGrids().get(0);
                    for (Map.Entry<String, DataRow> rowEntry : table_r.data.entrySet()) {
                        if (dataGrid.greatThan(rowEntry.getValue().getDataGrids().get(0))) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                } else {
                    DataGrid dataGrid = table_r.data.get("result").getDataGrids().get(0);
                    for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                        if (rowEntry.getValue().getDataGrids().get(0).greatThan(dataGrid)) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                }
            }
        } else {
            //The case that left and right are both column
            if (table_l.columnNames.size() == 0 || table_r.columnNames.size() == 0 ||
                    table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    if (dataGrid_l.greatThan(dataGrid_r)) {
                        res.data.remove(rowEntry.getKey());
                    }
                }
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        Expression leftExpression = greaterThanEquals.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = greaterThanEquals.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                if (dataGrid_l.minorThan(dataGrid_r)) {
                    res.data = new HashMap<>();
                }
            } else {
                // The case that only one side has result
                if (table_l.data.containsKey("result")) {
                    DataGrid dataGrid = table_l.data.get("result").getDataGrids().get(0);
                    for (Map.Entry<String, DataRow> rowEntry : table_r.data.entrySet()) {
                        if (dataGrid.minorThan(rowEntry.getValue().getDataGrids().get(0))) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                } else {
                    DataGrid dataGrid = table_r.data.get("result").getDataGrids().get(0);
                    for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                        if (rowEntry.getValue().getDataGrids().get(0).minorThan(dataGrid)) {
                            res.data.remove(rowEntry.getKey());
                        }
                    }
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.size() == 0 || table_r.columnNames.size() == 0 ||
                    table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    if (dataGrid_l.minorThan(dataGrid_r)) {
                        res.data.remove(rowEntry.getKey());
                    }
                }
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(Addition addition) {
        Expression leftExpression = addition.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = addition.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this.data);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                res.data = new HashMap<>();
                DataGrid dataGrid = new DataGrid(dataGrid_l);
                dataGrid.add(dataGrid_r);
                res.data.put("result", new DataRow(Arrays.asList(dataGrid)));
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
                    DataGrid dataGrid_new = new DataGrid(dataGrid);
                    dataGrid_new.add(rowEntry.getValue().getDataGrids().get(0));
                    res.data.put(rowEntry.getKey(), new DataRow(Arrays.asList(dataGrid_new)));
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    DataGrid dataGrid = new DataGrid(dataGrid_l);
                    dataGrid.add(dataGrid_r);
                    res.data.put(rowEntry.getKey(), new DataRow(Arrays.asList(dataGrid)));
                }
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(Subtraction subtraction) {
        Expression leftExpression = subtraction.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = subtraction.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this.data);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                res.data = new HashMap<>();
                DataGrid dataGrid = new DataGrid(dataGrid_l);
                dataGrid.sub(dataGrid_r);
                res.data.put("result", new DataRow(Arrays.asList(dataGrid)));
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
                    DataGrid dataGrid_new = new DataGrid(dataGrid);
                    dataGrid_new.sub(rowEntry.getValue().getDataGrids().get(0));
                    res.data.put(rowEntry.getKey(), new DataRow(Arrays.asList(dataGrid_new)));
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    DataGrid dataGrid = new DataGrid(dataGrid_l);
                    dataGrid.sub(dataGrid_r);
                    res.data.put(rowEntry.getKey(), new DataRow(Arrays.asList(dataGrid)));
                }
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(Multiplication multiplication) {
        Expression leftExpression = multiplication.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = multiplication.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this.data);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                res.data = new HashMap<>();
                DataGrid dataGrid = new DataGrid(dataGrid_l);
                dataGrid.mul(dataGrid_r);
                res.data.put("result", new DataRow(Arrays.asList(dataGrid)));
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
                    DataGrid dataGrid_new = new DataGrid(dataGrid);
                    dataGrid_new.mul(rowEntry.getValue().getDataGrids().get(0));
                    res.data.put(rowEntry.getKey(), new DataRow(Arrays.asList(dataGrid_new)));
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    DataGrid dataGrid = new DataGrid(dataGrid_l);
                    dataGrid.mul(dataGrid_r);
                    res.data.put(rowEntry.getKey(), new DataRow(Arrays.asList(dataGrid)));
                }
            }
        }
        this.returnValue = res;
    }

    @Override
    public void visit(Division division) {
        Expression leftExpression = division.getLeftExpression();
        leftExpression.accept(this);
        Table table_l = this.returnValue;
        Expression rightExpression = division.getRightExpression();
        rightExpression.accept(this);
        Table table_r = this.returnValue;

        Table res = new Table(this.data);
        if (table_l.data.containsKey("result") || table_r.data.containsKey("result")) {
            //The case that left or right contains result
            if (table_l.data.containsKey("result") && table_r.data.containsKey("result")) {
                // The case that left and right are both result
                DataGrid dataGrid_l = table_l.data.get("result").getDataGrids().get(0);
                DataGrid dataGrid_r = table_r.data.get("result").getDataGrids().get(0);
                res.data = new HashMap<>();
                DataGrid dataGrid = new DataGrid(dataGrid_l);
                dataGrid.div(dataGrid_r);
                res.data.put("result", new DataRow(Arrays.asList(dataGrid)));
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
                    DataGrid dataGrid_new = new DataGrid(dataGrid);
                    dataGrid_new.div(rowEntry.getValue().getDataGrids().get(0));
                    res.data.put(rowEntry.getKey(), new DataRow(Arrays.asList(dataGrid_new)));
                }
            }
        } else {
            //The case that left and right are both columns
            if (table_l.columnNames.get(0).compareTo(table_r.columnNames.get(0)) != 0) {
                for (Map.Entry<String, DataRow> rowEntry : table_l.data.entrySet()) {
                    DataGrid dataGrid_l = rowEntry.getValue().getDataGrids().get(0);
                    DataGrid dataGrid_r = table_r.data.get(rowEntry.getKey()).getDataGrids().get(0);
                    DataGrid dataGrid = new DataGrid(dataGrid_l);
                    dataGrid.div(dataGrid_r);
                    res.data.put(rowEntry.getKey(), new DataRow(Arrays.asList(dataGrid)));
                }
            }
        }
        this.returnValue = res;
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
        int size = 12;
        if (data.size() == 0) {
            List<DataGrid> head = new ArrayList<>();
            for (String columnName : columnNames) {
                head.add(new DataGrid(Type.STRING, columnName));
            }
            for (int i = 0; i < head.size(); ++i) {
                sb.append("+");
                for (int j = 0; j < size; ++j) {
                    sb.append("-");
                }
            }
            print_row(sb, new DataRow(head));
            sb.append("+\n");
            return sb.toString();
        }
        for (int i = 0; i < data.values().iterator().next().getDataGrids().size(); ++i) {
            sb.append("+");
            for (int j = 0; j < size; ++j) {
                sb.append("-");
            }
        }

        if (simple) {
            List<DataGrid> head = new ArrayList<>();
            head.add(new DataGrid(Type.STRING, "result"));
            print_row(sb, new DataRow(head));
            for (String s : data.keySet()) {
                print_row(sb, data.get(s));
            }
        } else {
            List<DataGrid> head = new ArrayList<>();
            for (String columnName : columnNames) {
                head.add(new DataGrid(Type.STRING, columnName));
            }
            print_row(sb, new DataRow(head));
            for (String s : data.keySet()) {
                print_row(sb, data.get(s));
            }
        }
        sb.append("+\n");
        sb.append(this.getData().size()).append(" rows in total").append("\n");
        return new String(sb);
    }

    static void print_row(StringBuilder sb, DataRow row) {
        int size = 12;
        List<DataGrid> table = row.getDataGrids();
        sb.append("+\n");
        for (int i = 0; i < table.size(); ++i) {
            sb.append("|");
            int len = table.get(i).toString().length();
            int left_space = (size - len) % 2 == 0 ? (size - len) / 2 : (size - len) / 2 + 1;
            int right_space = (size - len) / 2;
            for (int j = 0; j < left_space; ++j) {
                sb.append(" ");
            }
            sb.append(table.get(i).toString());
            for (int j = 0; j < right_space; ++j) {
                sb.append(" ");
            }
        }
        sb.append("|\n");
        for (int i = 0; i < table.size(); ++i) {
            sb.append("+");
            for (int j = 0; j < size; ++j) {
                sb.append("-");
            }
        }
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

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.getExpression().accept(this);
        int res = -(int) (this.returnValue.getData().get("result").getDataGrids().get(0).getData());
        this.returnValue = new Table(res);
    }
}
