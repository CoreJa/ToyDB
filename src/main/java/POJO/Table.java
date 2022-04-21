package POJO;

import adaptors.StatementVisitorAdaptor;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Table implements Serializable {
    private String tableName;
    private List<String> columnNames;
    private List<DataRow.Types> types;
    private Map<String, DataRow> data;//key: primary key; value: data record

    public Table(List<String> columnNames, Map<String, DataRow> data) {
        this.columnNames = columnNames;
        this.data = data;
    }

    public Table(CreateTable createTableStatement) {
        // define the name and dataType of each column
        this.tableName = createTableStatement.getTable().getName();
        List<ColumnDefinition> columnDefinitionList = createTableStatement.getColumnDefinitions();
        columnNames = new ArrayList<>();
        types = new ArrayList<>();
        for(Iterator<ColumnDefinition> iter = columnDefinitionList.iterator(); iter.hasNext();) {
            ColumnDefinition def = iter.next();
            columnNames.add(def.getColumnName());
            if(def.getColDataType().getDataType().toLowerCase().compareTo("string") == 0) {
                types.add(DataRow.Types.STRING);
            }
        }
    }
}
