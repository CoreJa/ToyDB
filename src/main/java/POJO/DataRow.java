package POJO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataRow implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<DataGrid> dataGrids;

    public DataRow(List<Type> types, List<Object> data) {
        this.dataGrids = new ArrayList<>();
        int n = types.size();
        for (int i = 0; i < n; i++) {
            DataGrid dataGrid = new DataGrid(types.get(i), data.get(i));
            dataGrids.add(dataGrid);
        }
    }

    public DataRow(List<DataGrid> datagrids) {
        this.dataGrids = datagrids;
    }

    public DataRow(DataRow dataRow1, DataRow dataRow2) {
        this.dataGrids = new ArrayList<>();
        this.dataGrids.addAll(dataRow1.getDataGrids());
        this.dataGrids.addAll(dataRow2.getDataGrids());
    }

    public List<DataGrid> getDataGrids() {
        return dataGrids;
    }

    @Override
    public DataRow clone() {
        List<DataGrid> dataGrids = new ArrayList<>();
        for (DataGrid datagrid : this.dataGrids) {
            dataGrids.add(datagrid.clone());
        }
        return new DataRow(dataGrids);
    }

    @Override
    public String toString() {
        return dataGrids.toString();
    }
}
