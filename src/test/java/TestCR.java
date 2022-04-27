import POJO.Database;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import utils.ExecutionException;

import java.util.ArrayList;

public class TestCR {
    public static void main(String[] args) {
        Database db = new Database();
        db.load();
//        loadData(db);
//        db.save();
        ArrayList<String> stmts = new ArrayList<>();
//        stmts.add("select col3,col4 from table2");
//        stmts.add("select col3,col2 from table2");
//
//        stmts.add("select * from tableName");
//        stmts.add("select col1 from tableName");

//        stmts.add("select * from tableName where tableName.col1=10");
//        stmts.add("select * from tableName where col1=10");
//        stmts.add("select * from tableName where col1=col1");
//        stmts.add("select * from tableName where col1=col2");
//        stmts.add("select * from tableName where 10=col1");
//
//        stmts.add("select * from tableName where col1!=10");
//        stmts.add("select * from tableName where 10!=10");
//        stmts.add("select * from tableName where 10!=15");
//        stmts.add("select * from tableName where 10<>15");
//        stmts.add("select * from tableName where col1!=col2");
//        stmts.add("select * from tableName where col1!=col2 and col2!=col3");
//        stmts.add("select * from tableName where col1!=col2 or col2!=col3");
//
//        stmts.add("select * from tableName where col1<10");
//        stmts.add("select * from tableName where col1<=10");
//        stmts.add("select * from tableName where col1>1000");
//        stmts.add("select * from tableName where col1>=1000");
//        stmts.add("select * from tableName where col1<=col2");
//        stmts.add("select * from tableName where col1>col2");
//        stmts.add("select * from tableName where col1>col2 and col2<col3");
//        stmts.add("select * from tableName where col1>col2 and 6<5");
//        stmts.add("select * from tableName where col1<10 and col2<15");
//        stmts.add("select * from tableName where col1<10 or col2<15");
//        stmts.add("select * from tableName where col1<10 and col1>10");
//        stmts.add("select * from tableName where col1<=10 and col1>=10");
//
//        stmts.add("select * from tableName where col1=10 or col2=10");
//        stmts.add("select * from tableName where col1=10 or col1=15");
//        stmts.add("select * from tableName where col1=10 or col1=15 or col1=100");
//        stmts.add("select * from tableName where col1=10 or col2=15");
//
//        stmts.add("select * from tableName where col1=10 and col2=10");
//        stmts.add("select * from tableName where col1=10 and col2=10 and col3=10");
//        stmts.add("select * from tableName where col1=10 and col1=15");
//        stmts.add("select * from tableName where col1=10 and col2=15");
//
//        stmts.add("select * from tableName where col1<10+5");
//        stmts.add("select * from tableName where col1>col2+5");
//        stmts.add("select * from tableName where col1>col2+col3");
//        stmts.add("select * from tableName where col1>col2+col3+999");
//
//        stmts.add("select * from tableName where col1<10-5");
//        stmts.add("select * from tableName where col1>col2-5");
//        stmts.add("select * from tableName where col1>col2-col3");
//        stmts.add("select * from tableName where col1>col2-col3+999");
//
//        stmts.add("select * from tableName where col1<10*2");
//        stmts.add("select * from tableName where col1>col2*2");
//        stmts.add("select * from tableName where col1>col2*col3");
//        stmts.add("select * from tableName where col1>col2*col3*2");
//
//        stmts.add("select * from tableName where col1<10/2");
//        stmts.add("select * from tableName where col1<col2/2");
//        stmts.add("select * from tableName where col1<col2/col3");
//        stmts.add("select * from tableName where col1<col2/col3/2");

        stmts.add("select * from tableName join table2 on table2.col3=tableName.col3 and table2.col4=tableName.col1;");
        stmts.add("select * from tableName join table2 on col4=col1;");
        stmts.add("select * from tableName join table2 on col3=col3;");

        for (String stmt : stmts) {
            try {
                Statement statement = CCJSqlParserUtil.parse(stmt);
                System.out.println(stmt);
                statement.accept(db);
                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException | ExecutionException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private static void loadData(Database db) {
        ArrayList<String> stmts = new ArrayList<>();
        stmts.add("create table tableName (" +
                "    col1 int UNIQUE,\n" +
                "    col2 int NOT NULL,\n" +
                "    col3 int NOT NULL,\n" +
                "    PRIMARY KEY (col1));");
        stmts.add("insert into tableName " +
                "  values (1,1,1);");
        stmts.add("create table table2 (" +
                "  col3 int unique," +
                "  col4 int not null," +
                "  primary key (col3)," +
                "  foreign key (col4) references tableName(col2));");
        stmts.add("create index myIndex on tableName(col2);");
        stmts.add("insert into tableName " +
                "  values (2,2,2);");
        stmts.add("insert into table2 " +
                "  values (2,2);");
//        stmts.add("insert into table2 " +
//                "  values (3,3);");
        stmts.add("drop index tableName.myIndex;");
        for (int i = 3; i <= 1000; i++) {
            stmts.add("insert into tableName values (" + i + "," + i + "," + i + ");");
        }
        stmts.add("insert into tableName values (1001,1,1);");
        stmts.add("insert into tableName values (1002,1,1);");
        stmts.add("insert into tableName values (1003,1003,2);");

        for (String stmt : stmts) {
            try {
                Statement statement = CCJSqlParserUtil.parse(stmt);
                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
