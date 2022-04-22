package POJO;

import java.io.Serializable;

public class DataGrid implements Serializable {
    // type of the data cell
    private Object data;
    private Type type; //enum type, a list of supported types

    public DataGrid(Type type, Object data) {
        this.data = data;
        this.type = type;
    }

    public DataGrid(Type type, Object data) {
        this.data = data;
        this.type = type;
    }

    public Object getData() {
        return this.data;
    }


    public String toString(){//shi
        return this.data.toString();
    }
}
