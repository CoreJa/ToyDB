import POJO.Database;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import utils.ExecutionException;

import java.util.ArrayList;

public class testzhy {
    public static void main(String[] args) {
        Database db = new Database();
        db.load();
        ArrayList<String> stmts=new ArrayList<>();
        stmts.add("create table tableName ("+
                "    col1 int UNIQUE,\n" +
                "    col2 char NOT NULL,\n"+
                "    PRIMARY KEY (col1));");
//        stmts.add("insert into tableName "+
//                "  values (1,1);");
        stmts.add("create table table2 ("+
                "  col3 int unique,"+
                "  col4 int not null,"+
                "  primary key (col3),"+
                "  foreign key (col4) references tableName(col1));");
        stmts.add("create index myIndex on tableName(col2);");
        stmts.add("insert into tableName "+
                "  values (2,'2');");
        stmts.add("insert into table2 "+
                "  values (2,2);");
        stmts.add("insert into table2 "+
                "  values (3,3);");
        stmts.add("drop index tableName.myIndex;");
        for (String stmt : stmts) {
            try {
                Statement statement = CCJSqlParserUtil.parse(stmt);
                statement.accept(db);
                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        for (int i = 3; i < 100000; i++) {
            try {
                Statement statement = CCJSqlParserUtil.parse("insert into tableName "+
                        "  values ("+i+",\'"+1+"\');");
                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        stmts=new ArrayList<>();
//        stmts.add("insert into table2 "+
//                "  values (1000,1000);");
//        stmts.add("create index myIndex on tableName(col2);");
//        stmts.add("insert into table2 "+
//                "  values (3,3);");
        stmts.add("select col1 from tableName order by col1 asc limit 5");
        stmts.add("select distinct col2 from tableName order by col2 desc limit 5");

        for (String stmt : stmts) {
            try {
                Statement statement = CCJSqlParserUtil.parse(stmt);
                statement.accept(db);
                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
//        db.save();
    }
}
