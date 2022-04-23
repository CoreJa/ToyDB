package utils;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class ExecuteEngine implements StatementVisitor, FromItemVisitor, ItemsListVisitor, ExpressionVisitor {
    @Override
    public void visit(SavepointStatement savepointStatement) {
        throw new NotImplementedException("SavepointStatement");
    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {
        throw new NotImplementedException("RollbackStatement");

    }

    @Override
    public void visit(Comment comment) {
        throw new NotImplementedException("Comment");

    }

    @Override
    public void visit(Commit commit) {
        throw new NotImplementedException("Commit");
    }

    @Override
    public void visit(Delete delete) {
        throw new NotImplementedException("Delete");
    }

    @Override
    public void visit(Update update) {
        throw new NotImplementedException("Update");
    }

    @Override
    public void visit(Insert insert) {
        throw new NotImplementedException("Insert");
    }

    @Override
    public void visit(Replace replace) {
        throw new NotImplementedException("Replace");
    }

    @Override
    public void visit(Drop drop) {
        throw new NotImplementedException("Drop");
    }

    @Override
    public void visit(Truncate truncate) {
        throw new NotImplementedException("truncate");
    }

    @Override
    public void visit(CreateIndex createIndex) {
        throw new NotImplementedException("createIndex");
    }

    @Override
    public void visit(CreateSchema aThis) {
        throw new NotImplementedException("CreateSchema");
    }

    @Override
    public void visit(CreateTable createTable) {
        throw new NotImplementedException("createTable");
    }

    @Override
    public void visit(CreateView createView) {
        throw new NotImplementedException("createView");
    }

    @Override
    public void visit(AlterView alterView) {
        throw new NotImplementedException("alterView");
    }

    @Override
    public void visit(Alter alter) {
        throw new NotImplementedException("alter");
    }

    @Override
    public void visit(Statements stmts) {
        throw new NotImplementedException("Statements");
    }

    @Override
    public void visit(Execute execute) {
        throw new NotImplementedException("Execute");
    }

    @Override
    public void visit(SetStatement set) {
        throw new NotImplementedException("SetStatement");
    }

    @Override
    public void visit(ResetStatement reset) {
        throw new NotImplementedException("ResetStatement");
    }

    @Override
    public void visit(ShowColumnsStatement set) {
        throw new NotImplementedException("ShowColumnsStatement");
    }

    @Override
    public void visit(ShowTablesStatement showTables) {
        throw new NotImplementedException("ShowTablesStatement");
    }

    @Override
    public void visit(Merge merge) {
        throw new NotImplementedException("Merge");
    }

    @Override
    public void visit(Select select) {
        throw new NotImplementedException("Select");
    }

    @Override
    public void visit(Upsert upsert) {
        throw new NotImplementedException("Upsert");
    }

    @Override
    public void visit(UseStatement use) {
        throw new NotImplementedException("UseStatement");
    }

    @Override
    public void visit(Block block) {
        throw new NotImplementedException("Block");
    }

    @Override
    public void visit(ValuesStatement values) {
        throw new NotImplementedException("ValuesStatement");
    }

    @Override
    public void visit(DescribeStatement describe) {
        throw new NotImplementedException("DescribeStatement");
    }

    @Override
    public void visit(ExplainStatement aThis) {
        throw new NotImplementedException("ExplainStatement");
    }

    @Override
    public void visit(ShowStatement aThis) {
        throw new NotImplementedException("ShowStatement");
    }

    @Override
    public void visit(DeclareStatement aThis) {
        throw new NotImplementedException("DeclareStatement");
    }

    @Override
    public void visit(Grant grant) {
        throw new NotImplementedException("Grant");
    }

    @Override
    public void visit(CreateSequence createSequence) {
        throw new NotImplementedException("CreateSequence");
    }

    @Override
    public void visit(AlterSequence alterSequence) {
        throw new NotImplementedException("AlterSequence");
    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        throw new NotImplementedException("CreateFunctional");
    }

    @Override
    public void visit(CreateSynonym createSynonym) {
        throw new NotImplementedException("CreateSynonym");
    }

    @Override
    public void visit(AlterSession alterSession) {
        throw new NotImplementedException("AlterSession");
    }

    @Override
    public void visit(IfElseStatement aThis) {
        throw new NotImplementedException("IfElseStatement");
    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {
        throw new NotImplementedException("RenameTableStatement");
    }

    @Override
    public void visit(PurgeStatement purgeStatement) {
        throw new NotImplementedException("PurgeStatement");
    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {
        throw new NotImplementedException("AlterSystemStatement");
    }

    @Override
    public void visit(Table tableName) {
        throw new NotImplementedException("Table");
    }

    @Override
    public void visit(BitwiseRightShift aThis) {
        throw new NotImplementedException("BitwiseRightShift");
    }

    @Override
    public void visit(BitwiseLeftShift aThis) {
        throw new NotImplementedException("BitwiseLeftShift");
    }

    @Override
    public void visit(NullValue nullValue) {
        throw new NotImplementedException("NullValue");
    }

    @Override
    public void visit(Function function) {
        throw new NotImplementedException("Function");
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        throw new NotImplementedException("SignedExpression");
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        throw new NotImplementedException("JdbcParameter");
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        throw new NotImplementedException("JdbcNamedParameter");
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        throw new NotImplementedException("DoubleValue");
    }

    @Override
    public void visit(LongValue longValue) {
        throw new NotImplementedException("LongValue");
    }

    @Override
    public void visit(HexValue hexValue) {
        throw new NotImplementedException("HexValue");
    }

    @Override
    public void visit(DateValue dateValue) {
        throw new NotImplementedException("DateValue");
    }

    @Override
    public void visit(TimeValue timeValue) {
        throw new NotImplementedException("TimeValue");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        throw new NotImplementedException("TimestampValue");
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        throw new NotImplementedException("Parenthesis");
    }

    @Override
    public void visit(StringValue stringValue) {
        throw new NotImplementedException("StringValue");
    }

    @Override
    public void visit(Addition addition) {
        throw new NotImplementedException("Addition");
    }

    @Override
    public void visit(Division division) {
        throw new NotImplementedException("Division");
    }

    @Override
    public void visit(IntegerDivision division) {
        throw new NotImplementedException("IntegerDivision");
    }

    @Override
    public void visit(Multiplication multiplication) {
        throw new NotImplementedException("Multiplication");
    }

    @Override
    public void visit(Subtraction subtraction) {
        throw new NotImplementedException("Subtraction");
    }

    @Override
    public void visit(AndExpression andExpression) {
        throw new NotImplementedException("AndExpression");
    }

    @Override
    public void visit(OrExpression orExpression) {
        throw new NotImplementedException("OrExpression");
    }

    @Override
    public void visit(XorExpression orExpression) {
        throw new NotImplementedException("XorExpression");
    }

    @Override
    public void visit(Between between) {
        throw new NotImplementedException("Between");
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        throw new NotImplementedException("EqualsTo");
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        throw new NotImplementedException("GreaterThan");
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        throw new NotImplementedException("GreaterThanEquals");
    }

    @Override
    public void visit(InExpression inExpression) {
        throw new NotImplementedException("InExpression");
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        throw new NotImplementedException("FullTextSearch");
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        throw new NotImplementedException("IsNullExpression");
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        throw new NotImplementedException("IsBooleanExpression");
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        throw new NotImplementedException("LikeExpression");
    }

    @Override
    public void visit(MinorThan minorThan) {
        throw new NotImplementedException("MinorThan");
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        throw new NotImplementedException("MinorThanEquals");
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        throw new NotImplementedException("NotEqualsTo");
    }

    @Override
    public void visit(Column tableColumn) {
        throw new NotImplementedException("Column");
    }

    @Override
    public void visit(SubSelect subSelect) {
        throw new NotImplementedException("SubSelect");
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        throw new NotImplementedException("CaseExpression");
    }

    @Override
    public void visit(WhenClause whenClause) {
        throw new NotImplementedException("WhenClause");
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        throw new NotImplementedException("ExistsExpression");
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        throw new NotImplementedException("AnyComparisonExpression");
    }

    @Override
    public void visit(Concat concat) {
        throw new NotImplementedException("Concat");
    }

    @Override
    public void visit(Matches matches) {
        throw new NotImplementedException("Matches");
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        throw new NotImplementedException("BitwiseAnd");
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        throw new NotImplementedException("BitwiseOr");
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        throw new NotImplementedException("BitwiseXor");
    }

    @Override
    public void visit(CastExpression cast) {
        throw new NotImplementedException("CastExpression");
    }

    @Override
    public void visit(Modulo modulo) {
        throw new NotImplementedException("Modulo");
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        throw new NotImplementedException("AnalyticExpression");
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        throw new NotImplementedException("ExtractExpression");
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        throw new NotImplementedException("IntervalExpression");
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        throw new NotImplementedException("OracleHierarchicalExpression");
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        throw new NotImplementedException("RegExpMatchOperator");
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        throw new NotImplementedException("JsonExpression");
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        throw new NotImplementedException("JsonOperator");
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        throw new NotImplementedException("RegExpMySQLOperator");
    }

    @Override
    public void visit(UserVariable var) {
        throw new NotImplementedException("UserVariable");
    }

    @Override
    public void visit(NumericBind bind) {
        throw new NotImplementedException("NumericBind");
    }

    @Override
    public void visit(KeepExpression aexpr) {
        throw new NotImplementedException("KeepExpression");
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        throw new NotImplementedException("MySQLGroupConcat");
    }

    @Override
    public void visit(ValueListExpression valueList) {
        throw new NotImplementedException("ValueListExpression");
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        throw new NotImplementedException("RowConstructor");
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        throw new NotImplementedException("RowGetExpression");
    }

    @Override
    public void visit(OracleHint hint) {
        throw new NotImplementedException("OracleHint");
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        throw new NotImplementedException("TimeKeyExpression");
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        throw new NotImplementedException("DateTimeLiteralExpression");
    }

    @Override
    public void visit(NotExpression aThis) {
        throw new NotImplementedException("NotExpression");
    }

    @Override
    public void visit(NextValExpression aThis) {
        throw new NotImplementedException("NextValExpression");
    }

    @Override
    public void visit(CollateExpression aThis) {
        throw new NotImplementedException("CollateExpression");
    }

    @Override
    public void visit(SimilarToExpression aThis) {
        throw new NotImplementedException("SimilarToExpression");
    }

    @Override
    public void visit(ArrayExpression aThis) {
        throw new NotImplementedException("ArrayExpression");
    }

    @Override
    public void visit(ArrayConstructor aThis) {
        throw new NotImplementedException("ArrayConstructor");
    }

    @Override
    public void visit(VariableAssignment aThis) {
        throw new NotImplementedException("VariableAssignment");
    }

    @Override
    public void visit(XMLSerializeExpr aThis) {
        throw new NotImplementedException("XMLSerializeExpr");
    }

    @Override
    public void visit(TimezoneExpression aThis) {
        throw new NotImplementedException("TimezoneExpression");
    }

    @Override
    public void visit(JsonAggregateFunction aThis) {
        throw new NotImplementedException("JsonAggregateFunction");
    }

    @Override
    public void visit(JsonFunction aThis) {
        throw new NotImplementedException("JsonFunction");
    }

    @Override
    public void visit(ConnectByRootOperator aThis) {
        throw new NotImplementedException("ConnectByRootOperator");
    }

    @Override
    public void visit(OracleNamedFunctionParameter aThis) {
        throw new NotImplementedException("OracleNamedFunctionParameter");
    }

    @Override
    public void visit(ExpressionList expressionList) {
        throw new NotImplementedException("ExpressionList");
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        throw new NotImplementedException("NamedExpressionList");
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        throw new NotImplementedException("MultiExpressionList");
    }

    @Override
    public void visit(SubJoin subjoin) {
        throw new NotImplementedException("SubJoin");
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        throw new NotImplementedException("LateralSubSelect");
    }

    @Override
    public void visit(ValuesList valuesList) {
        throw new NotImplementedException("ValuesList");
    }

    @Override
    public void visit(TableFunction tableFunction) {
        throw new NotImplementedException("TableFunction");
    }

    @Override
    public void visit(ParenthesisFromItem aThis) {
        throw new NotImplementedException("ParenthesisFromItem");
    }
}
