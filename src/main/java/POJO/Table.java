package POJO;

import adaptors.StatementVisitorAdaptor;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Table implements Serializable {
    private List<String> columnNames;
    private List<String> types;
    private Map<String, DataRow> data;//key: primary key; value: data record

    public Table(List<String> columnNames, Map<String, DataRow> data) {
        this.columnNames = columnNames;
        this.data = data;
    }

    public Table(CreateTable createTableStatement) {
        StatementVisitor statementVisitor = new StatementVisitorAdaptor();
    }
}
