package POJO;

import java.io.Serializable;

public class DataGrid implements Serializable {
    // type of the data cell
    private static final long serialVersionUID = 1L;
    private Object data;
    private Type type; //enum type, a list of supported types

    public void setType(Type type) {
        this.type = type;
    }

    public DataGrid(Type type, Object data) {
        this.data = data;
        this.type = type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public DataGrid clone() {
        return new DataGrid(this.type, this.data);
    }

    public String toString() {//shi
        return this.data.toString();
    }

    public boolean compareTo(DataGrid dataGrid) {
        return (this.type == dataGrid.type) && (this.data.toString().compareTo(dataGrid.toString()) == 0);
    }
}
