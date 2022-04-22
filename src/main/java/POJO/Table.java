package POJO;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import utils.SyntaxException;


import java.io.Serializable;
import java.util.*;

public class Table extends StatementVisitorAdapter implements Serializable {
    private String tableName;
    private List<String> columnNames;
    private Map<String, Integer> columnIndexes;
    private List<Type> types;
    private Map<String, DataRow> data;//key: primary key; value: data record

    // Indexes of all columns, elements corresponding to PrimaryKey and None Indexed columns should be Null.
    private List<Map<String, String>> indexes;

    //Constraints
    private Integer primaryKey; // index in columnNames
    private Set<String> primaryKeySet; // maintain a HashSet of primary keys. Always cast to String.
    private List<Map.Entry<String, Integer>> foreignKeyList;


    //Constructors
    public Table(String tableName, List<String> columnNames, Map<String, DataRow> data) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.data = data;
    }

    public Table(Table table){
        this.tableName=table.tableName;
        this.columnNames=table.columnNames;

    }

    public Table(String tableName) {
        this(tableName, new ArrayList<String>(), new HashMap<String, DataRow>());
    }

    public Table(CreateTable createTableStatement) throws SyntaxException {
        //create table by statement
        // define the name and dataType of each column
        this.tableName = createTableStatement.getTable().getName();
        List<ColumnDefinition> columnDefinitionList = createTableStatement.getColumnDefinitions();
        this.columnNames = new ArrayList<>();
        Set<String> check = new HashSet<>(); //check duplication of column names
        types = new ArrayList<>();

        for (ColumnDefinition def : columnDefinitionList) {
            // column name
            String columnName = def.getColumnName();
            if (!check.contains(columnName)) {
                check.add(columnName);
                columnNames.add(columnName);
            } else {
                throw new SyntaxException("Duplicate column name.");
            }
            // data type
            String columnLowerCaseType = def.getColDataType().getDataType().toLowerCase();//string of type name
            if (columnLowerCaseType.compareTo("char") == 0 || columnLowerCaseType.compareTo("varchar") == 0) {
                types.add(Type.STRING);
            } else if (columnLowerCaseType.compareTo("integer") == 0 || columnLowerCaseType.compareTo("smallint") == 0) {
                types.add(Type.INT);
            } else {
                throw new SyntaxException("Wrong or unsupported data type.");
            }
        }
        // StatementVisitorAdapter
        //primary key, foreign key constraints
    }

    public int insertTable(Insert insertStatement) throws SyntaxException {

        return 0;
    }

    //setters and getters
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public Table getReturnValue() {
        return returnValue;
    }

    public boolean createIndex(String columnName){
        int colInd=columnIndexes.get(columnName); //get column index by column name

        if (colInd == primaryKey) {
            throw new SyntaxException("Can't create index on primary key.");
        }
        if (indexes.get(colInd) != null) { // judge if index already exists
            return false;
        }
        Map<String, List<String>> curIndex=new HashMap<>();
        indexes.set(colInd,curIndex); // initialize new index object into table
        data.forEach((k,v)->{ // constuct object
            String fieldValue=v.getDataGrids().get(colInd).toString();
            if (curIndex.containsKey(fieldValue)) {
                curIndex.get(fieldValue).add(k);
            }else{
                curIndex.put(fieldValue, new ArrayList<>(Arrays.asList(k)));
            }
        });
        return true;
    }

    @Override
    public void visit(CreateIndex createIndex) {
        this.returnValue=new Table(this.createIndex(createIndex.getIndex().getColumnsNames().get(0)));
    }

    @Override
    public void visit(Select selectStatement) throws SyntaxException {
        SelectBody selectBody=selectStatement.getSelectBody();
    }

    }
}
