import POJO.Database;
import POJO.Table;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import utils.SyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    public static void main (String[] args)throws IOException{
        //Initialization
        Database db = new Database();
        StringBuilder statementBuilder = new StringBuilder();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        db.load();
        for(String line = stdin.readLine();  line.compareTo("exit")!= 0;line = stdin.readLine()){//单语句
            statementBuilder.delete(0, statementBuilder.capacity());
            while (true) {//单行
                if ((line = stdin.readLine()) == null || line.length() == 0) break;//用户输入空行时代表语句结束
                statementBuilder.append(line + " ");
            }
            // Handle the statement
            String command = statementBuilder.toString();
            if(command.compareTo("exit ") == 0) {
                System.out.println("Exit request sent.");
                break;
            }
            try {
                Statement statement = CCJSqlParserUtil.parse(command);
                statement.accept(db);// TODO: what should we print if the statement is valid
            } catch(JSQLParserException e) {
                e.printStackTrace();
            } catch(SyntaxException e) {
                e.show();
            }
        }

        //尾处理
        db.save();
        try {
            stdin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully exit.");

        return;

    }
}
