import POJO.Database;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import utils.SyntaxException;

import java.util.ArrayList;

public class testzhy {
    public static void main(String[] args) {
        Database db = new Database();
        db.load();
        ArrayList<String> stmts=new ArrayList<>();
        stmts.add("create table tableName ("+
                "    col1 int UNIQUE,\n" +
                "    col2 int NOT NULL,\n"+
                "    PRIMARY KEY (col1));");
        stmts.add("insert into tableName "+
                "  values (1,1);");
        stmts.add("create table table2 ("+
                "  col3 int unique,"+
                "  col4 int not null,"+
                "  primary key (col3),"+
                "  foreign key (col4) references tableName(col2));");
        stmts.add("create index myIndex on tableName(col2);");
        stmts.add("insert into tableName "+
                "  values (2,2);");
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
            } catch (SyntaxException e) {
                e.printStackTrace();
            }
        }
        db.save();
    }
}
