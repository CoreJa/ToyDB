import POJO.Database;
import POJO.Table;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import utils.SyntaxException;

public class TestYC {
    public static void main(String[] args) {

        String createTableDemo = "CREATE TABLE Orders (\n" +
                "    OrderID int UNIQUE,\n" +
                "    OrderNumber int NOT NULL,\n" +
                "    PersonID int,\n" +
                "    PRIMARY KEY (OrderID),\n" +
                "    FOREIGN KEY (PersonID) REFERENCES Persons(PersonID)\n" +
                ");";
        String createTableDemo2 = "CREATE TABLE AS TABLE2";
        String dropTableDemo = "DROP TABLE T23";
        Table tab = null;
        Database db = new Database();
        try {
            Statement stmt = CCJSqlParserUtil.parse(createTableDemo2);
            tab = new Table();
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        } catch (SyntaxException e) {
            e.printStackTrace();
        }

        System.out.println(tab);

        return;
    }

}
