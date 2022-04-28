package utils;

import POJO.Database;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import utils.ExecutionException;

import java.util.ArrayList;

public class preloadData {
    public static void preload(Database db) {
        ArrayList<String> stmts=new ArrayList<>();
        stmts.add("create table table1i ("+
                "    col1 int UNIQUE,\n" +
                "    col2 int ,\n"+
                "    PRIMARY KEY (col1));");
        stmts.add("create table table11 ("+
                "    col1 int UNIQUE,\n" +
                "    col2 int ,\n"+
                "    PRIMARY KEY (col1));");
        stmts.add("create table table2i ("+
                "    col1 int UNIQUE,\n" +
                "    col2 int ,\n"+
                "    PRIMARY KEY (col1));");
        stmts.add("create table table21 ("+
                "    col1 int UNIQUE,\n" +
                "    col2 int ,\n"+
                "    PRIMARY KEY (col1));");
        stmts.add("create table table3i ("+
                "    col1 int UNIQUE,\n" +
                "    col2 int ,\n"+
                "    PRIMARY KEY (col1));");
        stmts.add("create table table31 ("+
                "    col1 int UNIQUE,\n" +
                "    col2 int ,\n"+
                "    PRIMARY KEY (col1));");
//        stmts.add("create table table4i ("+
//                "    col1 int UNIQUE,\n" +
//                "    col2 int ,\n"+
//                "    PRIMARY KEY (col1));");
        for (String stmt : stmts) {
            try {
                Statement statement = CCJSqlParserUtil.parse(stmt);
                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        for (int i = 1; i < 1001; i++) {
            try {
                Statement statement = CCJSqlParserUtil.parse("insert into table1i "+
                        "  values ("+i+","+i+");");
                statement.accept(db);
                statement = CCJSqlParserUtil.parse("insert into table11 "+
                        "  values ("+i+","+1+");");
                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        for (int i = 1; i < 10001; i++) {
            try {
                Statement statement = CCJSqlParserUtil.parse("insert into table2i "+
                        "  values ("+i+","+i+");");
                statement.accept(db);
                statement = CCJSqlParserUtil.parse("insert into table21 "+
                        "  values ("+i+","+1+");");
                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        for (int i = 1; i < 100001; i++) {
            try {
                Statement statement = CCJSqlParserUtil.parse("insert into table3i "+
                        "  values ("+i+","+i+");");
                statement.accept(db);
                statement = CCJSqlParserUtil.parse("insert into table31 "+
                        "  values ("+i+","+1+");");
                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
//        for (int i = 1; i < 1000001; i++) {
//            try {
//                Statement statement = CCJSqlParserUtil.parse("insert into table4i "+
//                        "  values ("+i+","+i+");");
//                statement.accept(db);
//            } catch (JSQLParserException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }

    }
}
