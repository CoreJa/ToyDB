import net.sf.jsqlparser.JSQLParserException;
import utils.SQLParser;

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


        SQLParser parser = new SQLParser();

        try{
            parser.parseStatements(selectDemo2);
            System.out.println(parser);
            // see the hierarchy in debug mode.
        } catch (JSQLParserException e) {
            System.out.println("Invalid sql query. Please try again.");
            //throw new RuntimeException(e);//TODO: handle exceptions
        }

        return;
    }
}
