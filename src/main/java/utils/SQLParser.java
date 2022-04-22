package utils;

import adaptors.StatementVisitorAdaptor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.Iterator;
import java.util.List;


public class SQLParser{
    private List<Statement> statementList;
    public List<Statement> getStatementList() {
        return statementList;
    }

    public void parseStatements(String sqlQueries) throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(sqlQueries);
        this.statementList = statements.getStatements();
        StatementVisitorAdaptor statementVisitorAdaptor = new StatementVisitorAdaptor();
        for(Statement statement: this.statementList) {
            parseStatement(statement);
        }
    }

    public void parseStatement(Statement statement) {
        StatementVisitorAdaptor sqlVisitorAdaptor = new StatementVisitorAdaptor();
        statement.accept(sqlVisitorAdaptor);
    }
}
