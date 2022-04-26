package POJO;

import utils.ExecutionException;

import java.io.Serializable;

public class DataGrid implements Serializable {
    // type of the data cell
    private static final long serialVersionUID = 1L;
    private Object data;
    private Type type; //enum type, a list of supported types

    public DataGrid(Type type, Object data) {
        this.data = data;
        this.type = type;
    }

    public DataGrid(DataGrid dataGrid) {
        this.data = dataGrid.data;
        this.type = dataGrid.type;
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
        this.checkType(dataGrid, "compare");
        return this.data.toString().compareTo(dataGrid.toString()) == 0;
    }

    public boolean greatThan(DataGrid dataGrid) {
        this.checkType(dataGrid, "compare");
        if (this.type == Type.STRING) {
            return this.data.toString().compareTo(dataGrid.toString()) > 0;
        } else {
            return (int) this.data > (int) dataGrid.data;
        }
    }

    public boolean minorThan(DataGrid dataGrid) {
        this.checkType(dataGrid, "compare");
        if (this.type == Type.STRING) {
            return this.data.toString().compareTo(dataGrid.toString()) < 0;
        } else {
            return (int) this.data < (int) dataGrid.data;
        }
    }

    private void checkType(DataGrid dataGrid, String operation) {
        if (this.type != dataGrid.type) {
            throw new ExecutionException("Can't " + operation + " type: " + this.type + " with type " + dataGrid.type);
        }
    }

    public void add(DataGrid dataGrid) {
        checkType(dataGrid, "add");
        if (this.type != Type.INT || dataGrid.type != Type.INT) {
            throw new ExecutionException("Type " + this.type + "doesn't support Addition operation.");
        }
        this.data = (int) this.data + (int) dataGrid.data;
    }

    public void sub(DataGrid dataGrid) {
        checkType(dataGrid, "subtract");
        if (this.type != Type.INT || dataGrid.type != Type.INT) {
            throw new ExecutionException("Type " + this.type + "doesn't support Subtraction operation.");
        }
        this.data = (int) this.data - (int) dataGrid.data;
    }

    public void mul(DataGrid dataGrid) {
        checkType(dataGrid, "multiply");
        if (this.type != Type.INT || dataGrid.type != Type.INT) {
            throw new ExecutionException("Type " + this.type + "doesn't support Multiplication operation.");
        }
        this.data = (int) this.data * (int) dataGrid.data;
    }

    public void div(DataGrid dataGrid) {
        checkType(dataGrid, "divide");
        if (this.type != Type.INT || dataGrid.type != Type.INT) {
            throw new ExecutionException("Type " + this.type + "doesn't support Division operation.");
        }
        this.data = (int) this.data / (int) dataGrid.data;
    }
}
