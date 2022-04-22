package utils;

import analyzer.StatementAnalyzer;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.JSQLParserException;

import java.util.List;


public class SQLParser{
    private List<Statement> statementList;
    public List<Statement> getStatementList() {
        return statementList;
    }

    public void parseStatements(String sqlQueries) throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(sqlQueries);
        this.statementList = statements.getStatements();
        StatementAnalyzer statementAnalyzer = new StatementAnalyzer();
        for(Statement statement: this.statementList) {
            parseStatement(statement);
        }
    }

    public void parseStatement(Statement statement) {
        StatementAnalyzer sqlVisitorAdaptor = new StatementAnalyzer();
        statement.accept(sqlVisitorAdaptor);
    }
}
