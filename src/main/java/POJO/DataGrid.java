package POJO;

import utils.ExecutionException;

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
        return String.valueOf(data);
    }

    public boolean compareTo(DataGrid dataGrid) {
        if (this.type != dataGrid.type) {
            throw new ExecutionException("Can't compare type: " + this.type + " with type " + dataGrid.type);
        }
        return this.data.toString().compareTo(dataGrid.toString()) == 0;
    }

    public boolean greatThan(DataGrid dataGrid) {
        if (this.type != dataGrid.type) {
            throw new ExecutionException("Can't compare type: " + this.type + " with type " + dataGrid.type);
        }
        if (this.type == Type.STRING) {
            return this.data.toString().compareTo(dataGrid.toString()) > 0;
        } else {
            return (int) this.data > (int) dataGrid.data;
        }
    }

    public boolean minorThan(DataGrid dataGrid) {
        if (this.type != dataGrid.type) {
            throw new ExecutionException("Can't compare type: " + this.type + " with type " + dataGrid.type);
        }
        if (this.type == Type.STRING) {
            return this.data.toString().compareTo(dataGrid.toString()) < 0;
        } else {
            return (int) this.data < (int) dataGrid.data;
        }
    }
}
