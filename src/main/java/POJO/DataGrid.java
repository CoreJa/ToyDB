package POJO;

import java.io.Serializable;

public class DataGrid implements Serializable {
    private Object data;
    private Type type;

    public DataGrid(Type type, Object data) {
        this.data = data;
        this.type = type;
    }

    public Object getData() {
        if (type == Type.INT) {
            return (int) this.data;
        } else if (type == Type.STRING) {
            return (String) this.data;
        } else {
            return this.data;
        }
    }
}
