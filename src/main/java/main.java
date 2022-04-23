import POJO.Database;
import POJO.Table;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import utils.SyntaxException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class main {
    public static void main(String[] args) {
        //Initialization
        Database db = new Database();
        StringBuilder sb = new StringBuilder();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        db.load();
        while(true) {
            //用户输入分号时代表语句结束?
        }

    }
}
