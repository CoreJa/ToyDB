package POJO;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
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
        if (table == null) {
            throw new ExecutionException("No such table");
        }
        createIndex.accept(table);
        this.returnValue = table.getReturnValue();
    }

    @Override
    public void visit(Drop drop) {
        if (drop.getType().toLowerCase().compareTo("table") == 0) {//Drop Table
            String tableName = drop.getName().getName();
            // use dropped to check if the statement is valid
            if (this.tables.get(tableName) == null) {// Check: if tableName exists
                throw new ExecutionException("Drop table failed: TABLE " + tableName + " not exists");
            }
            for (Map.Entry<String, Table> tableElement : this.tables.entrySet()) {// Check: if is depended on by other tables
                Table table = tableElement.getValue();
                List<Pair<String, Integer>> foreignKeyList = table.getForeignKeyList();
                for (int i = 0; i < foreignKeyList.size(); i++) {
                    Pair<String, Integer> pair = foreignKeyList.get(i);
                    if (pair != null && pair.getFirst().compareTo(tableName) == 0) {
                        throw new ExecutionException("Drop table failed: " + table.getTableName() + "." + table.getColumnNames().get(i) + " still depends on " + tableName);
                    }
                }
            }
            //check passed. start deleting
            try {//update metadata
                CCJSqlParserUtil.parse("DELETE FROM TABLES WHERE Table=\'" + tableName + "\'").accept(this);
                CCJSqlParserUtil.parse("DELETE FROM COLUMNS WHERE Table=\'" + tableName + "\'").accept(this);
            } catch (JSQLParserException e) {
                e.printStackTrace();
            }
            this.tables.remove(tableName);
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
        String tableName = insert.getTable().getName();
        Table table = tables.get(tableName);
        if (table == null) {
            throw new ExecutionException("Insert failed: table " + tableName + " not exist");
        }
        insert.accept(table);
        this.returnValue = table.getReturnValue();
    }

    @Override
    public void visit(Update update) {
        String tableName = update.getTable().getName();
        Table table = tables.get(tableName);
        if (table == null) {
            throw new ExecutionException("Update failed: table " + tableName + " not exist");
        }
        update.accept(table);
        this.returnValue = table.getReturnValue();
    }

    @Override
    public void visit(Delete delete) {
        String tableName = delete.getTable().getName();
        Table table = tables.get(tableName);
        if (table == null) {
            throw new ExecutionException("Delete failed: table " + tableName + " not exist");
        }
        delete.accept(table);
        this.returnValue = table.getReturnValue();
    }

    private Pair<String, String> getEq(AndExpression expr) {
        Expression l = expr.getLeftExpression();
        Expression r = expr.getRightExpression();
        if (l instanceof EqualsTo) {
            if (((EqualsTo) l).getLeftExpression() instanceof Column && ((EqualsTo) l).getRightExpression() instanceof Column) {
                if (((Column) ((EqualsTo) l).getLeftExpression()).getTable().getName().compareTo(
                        ((Column) ((EqualsTo) l).getRightExpression()).getTable().getName()) != 0) {
                    return new Pair<>(((EqualsTo) l).getLeftExpression().toString(), ((EqualsTo) l).getRightExpression().toString());
                }
            }
        }
        if (r instanceof EqualsTo) {
            if (((EqualsTo) r).getLeftExpression() instanceof Column && ((EqualsTo) r).getRightExpression() instanceof Column) {
                if (((Column) ((EqualsTo) r).getLeftExpression()).getTable().getName().compareTo(
                        ((Column) ((EqualsTo) r).getRightExpression()).getTable().getName()) != 0) {
                    return new Pair<>(((EqualsTo) r).getLeftExpression().toString(), ((EqualsTo) r).getRightExpression().toString());
                }
            }
        }
        Pair<String, String> ret;
        if (l instanceof AndExpression) {
            ret = getEq((AndExpression) l);
            if (ret != null) {
                return ret;
            }
        }
        if (r instanceof AndExpression) {
            ret = getEq((AndExpression) r);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
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
                Table rightTable = this.getTable(rightTableName);
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

                if (rightCol == null || leftCol == null) {
                    Expression exp = plainSelect.getWhere();
                    if (exp instanceof EqualsTo) {
                        if (((EqualsTo) exp).getLeftExpression() instanceof Column && ((EqualsTo) exp).getRightExpression() instanceof Column) {
                            leftCol = ((EqualsTo) exp).getLeftExpression().toString();
                            rightCol = ((EqualsTo) exp).getRightExpression().toString();
                        }
                    } else if (exp != null && exp instanceof AndExpression) {
                        Pair<String, String> eq = getEq((AndExpression) plainSelect.getWhere());
                        if (eq != null) {
                            leftCol = eq.getFirst();
                            rightCol = eq.getSecond();
                        }
                    }
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
                    //optimization
                    Map<String, Set<String>> cache = null;
                    //check for existed cache
                    if (rightTable.getIndexes().getIndexes().get(cacheColIdx) != null) {
                        cache = rightTable.getIndexes().getIndexes().get(cacheColIdx);
                    } else {
                        //calculate if we need to build this cache
                        if (leftTable.getData().size() * rightTable.getData().size() > 10000) {
                            //build cache if cache doesn't exists
                            cache = new HashMap<>();
                            for (Map.Entry<String, DataRow> rightEntry : rightData.entrySet()) {
                                String key = rightEntry.getValue().getDataGrids().get(cacheColIdx).toString();
                                if (cache.containsKey(key)) {
                                    cache.get(key).add(rightEntry.getKey());
                                } else {
                                    cache.put(key, new HashSet<>(Arrays.asList(rightEntry.getKey())));
                                }
                            }
                        }
                    }
                    int rightPaddingSize = rightTable.getColumnNames().size();
                    int leftPaddingSize = leftTable.getColumnNames().size() - rightPaddingSize;

                    if (cache == null) {
                        Map<String, Set<String>> x = new HashMap<>();
                        for (Map.Entry<String, DataRow> rightEntry : rightData.entrySet()) {
                            String key = rightEntry.getValue().getDataGrids().get(cacheColIdx).toString();
                            if (x.containsKey(key)) {
                                x.get(key).add(rightEntry.getKey());
                            } else {
                                x.put(key, new HashSet<>(Arrays.asList(rightEntry.getKey())));
                            }
                        }
                        cache = x;
                    }
                    // joining data with cache
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
                    } else if (join.isInner() || join.isSimple() || (!join.isOuter() && !join.isNatural() &&
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
            table.getIndexes().getIndexes().addAll(Collections.nCopies(table.getColumnNames().size(), null));
        }
        // recursively parsing select
        plainSelect.accept(table);
        this.returnValue = table.getReturnValue();
    }

    @Override
    public void visit(Select select) {
        select.getSelectBody().accept(this);
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

    @Override
    public void visit(Statements stmts) {
        for (Statement stmt : stmts.getStatements()) {
            stmt.accept(this);
        }
    }
}
