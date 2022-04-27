import POJO.Database;
import POJO.Table;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import utils.ExecutionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    public static void main (String[] args)throws IOException{
        //Initialization
        Database db = new Database();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        db.load();
        while(true){//单语句
            StringBuilder statementBuilder = new StringBuilder();
            while (true) {//单行
                System.out.print("> "); // > means the program is excepting the user input
                String line = stdin.readLine();
                if (line == null || line.length() == 0) {
                    break;
                }//用户输入空行时代表语句结束
                statementBuilder.append(line + " ");
            }

            // Handle the statement
            if(statementBuilder.length()==0){continue;} // skip empty lines
            String statementText = statementBuilder.toString();
            if(statementText.compareTo("exit ") == 0) {
                break;
            }
            long start=System.currentTimeMillis();
            try {
                Statement statement = CCJSqlParserUtil.parse(statementBuilder.toString());
                statement.accept(db);// TODO: what should we print if the statement is valid
                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                System.out.println(e.getMessage());
            } catch (ExecutionException e) {
                System.out.println(e.getMessage());
            } finally {
                statementBuilder.delete(0, statementBuilder.capacity());
            }
            System.out.println(System.currentTimeMillis()-start+"ms");
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
