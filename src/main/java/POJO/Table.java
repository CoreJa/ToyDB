package POJO;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import utils.SyntaxException;

import java.io.Serializable;
import java.util.*;

public class Table implements Serializable {
    private String tableName;
    private List<String> columnNames;
    private List<Type> types;
    private Map<String, DataRow> data;//key: primary key; value: data record

    public Table(List<String> columnNames, Map<String, DataRow> data) {
        this.columnNames = columnNames;
        this.data = data;
    }

    public Table(CreateTable createTableStatement) throws SyntaxException {
        // define the name and dataType of each column
        this.tableName = createTableStatement.getTable().getName();
        List<ColumnDefinition> columnDefinitionList = createTableStatement.getColumnDefinitions();
        columnNames = new ArrayList<>();
        types = new ArrayList<>();
        Set<String> check = new HashSet<>(); //check duplication of column names
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
            } else if (columnLowerCaseType.compareTo("int") == 0 || columnLowerCaseType.compareTo("smallint") == 0) {
                types.add(Type.INT);
            } else {
                throw new SyntaxException("Wrong or unsupported data type.");
            }
        }
    }

    public int insertT(Insert insertStatement) throws SyntaxException {
        return 0;
    }
}
