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
import java.util.ArrayList;

public class main {
    public static void main (String[] args)throws IOException{
        //Initialization
        Database db = new Database();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        db.load();
        jitPreheat(db);
        while(true){//单语句
        while(true){//each statement
            StringBuilder statementBuilder = new StringBuilder();
            while (true) {//each line
                System.out.print("> "); // '>' means the program is excepting the user input
                String line = stdin.readLine();
                if (line == null || line.length() != 0 && (line.charAt(line.length()-1)==';') ) {//';' meaning end of a statement
                    statementBuilder.append(line);
                    break;
                }
                statementBuilder.append(line + " ");
            }

            // Handle the statement
            if(statementBuilder.length()==0){continue;} // skip empty lines
            String statementText = statementBuilder.toString();
            if(statementText.length() >= 5 && statementText.substring(0,5).compareTo("exit;") == 0) {
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

        //Saving, exit
        System.out.println("Exiting the program. Saving the database...");
        db.save();
        try {
            stdin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully exit.");

        return;

    }
    public static void jitPreheat(Database db){
        ArrayList<String> stmts=new ArrayList<>();
        stmts.add("select * from table31 where col2=1;");
        stmts.add("select * from table31 where col2=1 order by col1;");
        stmts.add("select distinct col1 from table31 where col2=1 order by col1 limit 5;");
        stmts.add("create index3i on table3i(col2);");
        stmts.add("update table3i set col2=col1/col2;");
        stmts.add("drop index table3i.index3i;");
        stmts.add("update table3i set col2=col1/col2;");
        stmts.add("select col1 from table31 where col1>50000;");
        stmts.add("select col1 from table31 where col1<50000;");
        stmts.add("select col1 from table31 where col1>=50000;");
        stmts.add("select col1 from table31 where col1<=50000;");
        stmts.add("update table3i set col2=col1+col2;");
        stmts.add("update table3i set col2=col2-col1;");
        for (String stmt : stmts) {
            long start=System.currentTimeMillis();
            try {
                Statement statement = CCJSqlParserUtil.parse(stmt);
                statement.accept(db);
//                System.out.println(db.getReturnValue().toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis()-start);
        }
    }
}
