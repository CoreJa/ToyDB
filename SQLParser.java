import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.io.StringReader;
import java.util.List;


public class SQLParser{
    private List<Statement> statementList;
    public List<Statement> getStatementList() {
        return statementList;
    }
    public void parseStatements(String sqlQueries) throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(sqlQueries);
        this.statementList = statements.getStatements();
        for(Statement stmt: this.statementList) {
            parseStatement(stmt);
        }
    }
    public void parseStatement(Statement stmt) {
        // Data Manipulation
        if(stmt instanceof Insert) {
            //
        } else if(stmt instanceof Delete) {

        } else if(stmt instanceof Update) {

        } else if(stmt instanceof Select) {

        }

        // Data Definition
        if(stmt instanceof CreateTable) {

        } else if (stmt instanceof CreateIndex) {

        } else if (stmt instanceof Drop) {
            // Drop table or Drop index!
        }

    }

}
