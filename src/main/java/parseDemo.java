import POJO.Database;
import POJO.Table;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import utils.SyntaxException;

public class parseDemo {
    public static void main(String[] args) {
        String selectDemo1 = "SELECT DISTINCT(c.address), c.date FROM customer c\n";

        String selectDemo2 = "SELECT * " +
                "FROM MY_TABLE1, MY_TABLE2, (SELECT * FROM MY_TABLE3) LEFT OUTER JOIN MY_TABLE4 " +
                "WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)";

        String selectDemo3 = "select t1.f1\n" +
                "from my.table1 t1\n" +
                " join (my.table2 t2\n" +
                " left join my.table3 t3\n" +
                " on t2.f1 = t3.f1) as joinalias1\n" +
                " on t1.f1 = t2.f1; ";

        String createTableDemo = "CREATE TABLE Orders (\n" +
                "    OrderID int UNIQUE,\n" +
                "    OrderNumber int NOT NULL,\n" +
                "    PersonID int,\n" +
                "    PRIMARY KEY (OrderID),\n" +
                "    FOREIGN KEY (PersonID) REFERENCES Persons(PersonID)\n" +
                ");";

        String createIndexDemo = "create index indname on Tablename (ColName);";
        String dropTableDemo = "DROP TABLE T23.index2333;";
        String selectDemo = "SELECT DISTINCT ID,ID2 \" +\n" +
                "                \"FROM (SELECT * FROM MY_TABLE3), MY_TABLE1, MY_TABLE2 LEFT OUTER JOIN MY_TABLE4 \" +\n" +
                "                \"WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)";

        Table tab = null;
        Database db = new Database();
        try {
            Statement stmt = CCJSqlParserUtil.parse(dropTableDemo);
            tab = new Table();
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        } catch (SyntaxException e) {
            e.show();
        }

        System.out.println(tab);

        return;
    }

}
