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
//        stmts.add("select table1i.col1,table2i.col2 from table1i,table2i where table1i.col2=table2i.col1 and table1i.col1=680 order by table1i.col1 desc limit 5;");
//        stmts.add("select * from table2i join table3i on table2i.col2=table3i.col1 order by table2i.col2 desc limit 5;");
//        stmts.add("select table1i.col1 from table1i join table3i on table1i.col2=table3i.col2 limit 5;");
//        stmts.add("select distinct col1 from table1i order by col2 desc limit 5;");
//        stmts.add("create table tableName ("+
//                "    col1 int UNIQUE,\n" +
//                "    col2 int NOT NULL,\n"+
//                "    PRIMARY KEY (col1));");
//        stmts.add("insert into tableName "+
//                "  values (1,1);");
//        stmts.add("create table table2 ("+
//                "  col3 int unique,"+
//                "  col4 int not null,"+
//                "  primary key (col3),"+
//                "  foreign key (col4) references tableName(col1));");
//        stmts.add("create index myIndex on tableName(col2);");
//        stmts.add("insert into tableName "+
//                "  values (2,2);");
//        stmts.add("insert into table2 "+
//                "  values (2,2);");
//        stmts.add("insert into table2 "+
//                "  values (3,3);");
//        stmts.add("update tableName set col1=2 where col1 =1;");
//        stmts.add("update tableName set col2=col2+1 where col1 =2;");
//        stmts.add("drop index tableName.myIndex;");
//
//        stmts.add("select distinct col2 from table31;");
        stmts.add("select * from table3i where col2=75828;");
        stmts.add("create index index3i on table3i(col2);");
        stmts.add("select * from table3i where col2=75828;");
        stmts.add("select * from table3i where col2=100001;");
        stmts.add("create index index3i on table4i(col2);");
        stmts.add("drop index table3i.index3i;");
        stmts.add("drop index table3i.index3i;");
        stmts.add("select * from table3i where col2=75828;");


        stmts.add("select table1i.col1, table21.col1, table21.col2 from table1i, table21 where table1i.col1=1 and table1i.col2=table1i.col1 limit 5;");
        stmts.add("select table2i.col1, table31.col1, table31.col2 from table2i, table31 where table2i.col2=table31.col1 order by table2i.col1 limit 5;");
        stmts.add("select table2i.col1, table31.col1, table31.col2 from table2i, table31 where table2i.col1>=5001 and table2i.col2=table31.col1 order by table2i.col1 limit 5;");

        stmts.add("select * from table1i order by col1;");
        stmts.add("select * from table1i order by col1 asc;");
        stmts.add("select * from table1i order by col1 desc;");

        stmts.add("select * from table3i order by col1 limit 10;");
        stmts.add("select * from table3i order by col1 desc limit 10;");
        stmts.add("select * from table3i where col1<=2000 order by col1 desc limit 10;");

        stmts.add("select distinct col2 from table31;");
        stmts.add("select distinct col2 from table31 limit 10;");

        stmts.add("update table31 set col2=2 where col1=1;");
        stmts.add("select * from table31 order by col1 limit 5;");
        stmts.add("update table31 set col2=2 where col1=0;");
        stmts.add("update table3i set col2=col1/col2;");
        stmts.add("select distinct col2 from table3i;");
        stmts.add("update table3i set col2=col1/col2;");
        stmts.add("select distinct col2 from table3i limit 5;");

        stmts.add("select col1 from table4i where col2=75828;");
        stmts.add("create index index4i on table4i(col2);");
        stmts.add("select col1 from table4i where col2=75828;");
        stmts.add("select col1 from table4i where col2>500000 order by col1 limit 5;");
        stmts.add("update table4i set col2=col1/col2;");
        stmts.add("select distinct col2 from table4i;");
        stmts.add("drop index table4i.index4i;");
        stmts.add("update table4i set col2=col1/col2;");
        stmts.add("select * from table4i order by col1 limit 5;");


        for (String stmt : stmts) {
            long start=System.currentTimeMillis();
            try {
                Statement statement = CCJSqlParserUtil.parse(stmt);
                statement.accept(db);
                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis()-start);
        }
//        for (int i = 3; i < 100000; i++) {
//            try {
//                Statement statement = CCJSqlParserUtil.parse("insert into tableName "+
//                        "  values ("+i+",\'"+1+"\');");
//                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
//            } catch (JSQLParserException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//        stmts=new ArrayList<>();
//        stmts.add("insert into table2 "+
//                "  values (1000,1000);");
//        stmts.add("create index myIndex on tableName(col2);");
//        stmts.add("insert into table2 "+
//                "  values (3,3);");
//        stmts.add("update tableName set col2=col2+1 where col1>50000;");
//        stmts.add("select col1 from tableName where col1 = 50000 order by col1 asc limit 5");
//        stmts.add("select col2 from tableName where col2=\'1\' order by col2 desc limit 5");
//        stmts.add("select distinct col2 from tableName order by col2 asc limit 5");
//        stmts.add("create index myIndex on tableName(col2);");
//        stmts.add("update tableName set col2=col2+1 where col1<50000;");
//        stmts.add("delete from tableName where col1>=50000;");
//        stmts.add("update tableName set col2=col2+1;");
//        stmts.add("select distinct col2 from tableName order by col2 asc limit 5");
//        stmts.add("select distinct * from tableName order by col2 asc limit 5");

//        stmts.add("select col1 from tableName where col1 = 50000 order by col1 asc limit 5");
//        stmts.add("select col2 from tableName where col2=\'1\' order by col2 desc limit 5");
//        stmts.add("select distinct col2 from tableName order by col2 asc limit 5");

//        stmts.add("select distinct col2 from tableName order by col2 asc");
//        stmts.add("select col1 from tableName order by col1 asc");
//        stmts.add("select col1,col2 from tableName order by col1 asc");
//        stmts.add("select * from tableName where col2=\'1\' limit 5");
//        stmts.add("select col1 from tableName");

//        for (String stmt : stmts) {
//            long start=System.currentTimeMillis();
//            try {
//                Statement statement = CCJSqlParserUtil.parse(stmt);
//                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
//            } catch (JSQLParserException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//            System.out.println(System.currentTimeMillis()-start);
//        }
//        db.save();
    }
}
