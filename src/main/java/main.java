import POJO.Database;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.drop.Drop;

public class main {
    public static void main(String[] args) {
        String selectDemo1 = "SELECT DISTINCT(c.address), c.date FROM customer c\n";

        String selectDemo2 = "Select t1.f1\n" +
                "from my.table1 t1\n" +
                "join my.table2 t2\n" +
                "on t1.f1 = t2.f1 ";

        String selectDemo3 = "select t1.f1\n" +
                "from my.table1 t1\n" +
                " join (my.table2 t2\n" +
                " left join my.table3 t3\n" +
                " on t2.f1 = t3.f1) as joinalias1\n" +
                " on t1.f1 = t2.f1; ";

        String createTableDemo = "CREATE TABLE EMPLOYEE\n" +
                "(emp SMALLINT NOT NULL,\n" +
                "name CHAR(20) NOT NULL,\n" +
                "address VARCHAR NOT NULL,\n" +
                "primary key (emp));";

        String createIndexDemo = "create index indname on Tablename (ColName);";

        String dropIndexDemo = "drop index tablename.indname;";

        String insertDemo = " insert into tableName values (value1,value2);";

        try {
            Statement stmt = CCJSqlParserUtil.parse(insertDemo);
            System.out.println(stmt);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }


        return;
    }
}
