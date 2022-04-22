package analyzer;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

import java.util.ArrayList;
import java.util.List;

public class SelectAnalyzer implements SelectItemVisitor {
    // Return a list of column names
    private List<String> columns;

    public SelectAnalyzer() {
        this.columns = new ArrayList<>();
    }

    public List<String> getColumns() {
        return columns;
    }


    public void visit(AllColumns columns) {
        //TODO: All columns of 'COLUMNS'

    }

    public void visit(AllTableColumns columns) {
        Table table = columns.getTable();
        String schemaName = table.getSchemaName();//name of the schema
        String tableName = table.getName();//name of the table
        //TODO: All columns of 'tableName' (saved in Pivot?)

    }

    public void visit(SelectExpressionItem item) {

    }
}
