package analyzer;

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

import java.util.List;

public class StatementAnalyzer implements StatementVisitor {
    //Getters
    public CreateTable getCreateTable() {
        return createTable;
    }

    public CreateIndex getCreateIndex() {
        return createIndex;
    }

    public Drop getDrop() {
        return drop;
    }

    public Select getSelect() {
        return select;
    }

    public Insert getInsert() {
        return insert;
    }

    public Delete getDelete() {
        return delete;
    }

    public Update getUpdate() {
        return update;
    }

    public String getType() {
        return type;
    }

    //Types of statements
    private String type; //lower case
    //Data Definition Language (DDL)
    private CreateTable createTable;
    private CreateIndex createIndex;
    private Drop drop;
    //Data Manipulation Language (DML)
    private Select select;
    private Insert insert;
    private Delete delete;
    private Update update;

    @Override
    public void visit(CreateTable createTable) {
        this.type = "createtable";
        this.createTable = createTable;
    }

    @Override
    public void visit(CreateIndex createIndex) {
        this.type = "createindex";
        this.createIndex = createIndex;
    }

    @Override
    public void visit(Drop drop) {
        this.type = "drop";
        this.drop = drop;
    }

    @Override
    public void visit(Select select) {
        this.type = "select";
        this.select = select;
    }

    @Override
    public void visit(Delete delete) {
        this.type = "delete";
        this.delete = delete;
    }

    @Override
    public void visit(Update update) {
        this.type = "update";
        this.update = update;
    }

    @Override
    public void visit(Insert insert) {
        this.type = "insert";
        this.insert = insert;
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {

    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {

    }

    @Override
    public void visit(Comment comment) {

    }

    @Override
    public void visit(Commit commit) {

    }

    @Override
    public void visit(Replace replace) {

    }

    @Override
    public void visit(Truncate truncate) {

    }

    @Override
    public void visit(CreateSchema createSchema) {

    }

    @Override
    public void visit(CreateView createView) {

    }

    @Override
    public void visit(AlterView alterView) {

    }

    @Override
    public void visit(Alter alter) {

    }

    @Override
    public void visit(Statements statements) {

    }

    @Override
    public void visit(Execute execute) {

    }

    @Override
    public void visit(SetStatement setStatement) {

    }

    @Override
    public void visit(ResetStatement resetStatement) {

    }

    @Override
    public void visit(ShowColumnsStatement showColumnsStatement) {

    }

    @Override
    public void visit(ShowTablesStatement showTablesStatement) {

    }

    @Override
    public void visit(Merge merge) {

    }

    @Override
    public void visit(Upsert upsert) {

    }

    @Override
    public void visit(UseStatement useStatement) {

    }

    @Override
    public void visit(Block block) {

    }

    @Override
    public void visit(ValuesStatement valuesStatement) {

    }

    @Override
    public void visit(DescribeStatement describeStatement) {

    }

    @Override
    public void visit(ExplainStatement explainStatement) {

    }

    @Override
    public void visit(ShowStatement showStatement) {

    }

    @Override
    public void visit(DeclareStatement declareStatement) {

    }

    @Override
    public void visit(Grant grant) {

    }

    @Override
    public void visit(CreateSequence createSequence) {

    }

    @Override
    public void visit(AlterSequence alterSequence) {

    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {

    }

    @Override
    public void visit(CreateSynonym createSynonym) {

    }

    @Override
    public void visit(AlterSession alterSession) {

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {

    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {

    }

    @Override
    public void visit(PurgeStatement purgeStatement) {

    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {

    }
}
