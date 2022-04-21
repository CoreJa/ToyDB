package POJO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataRow implements Serializable {
    private List<DataGrid> dataGrids;

    public DataRow(List<Type> types, List<Object> data) {
        this.dataGrids = new ArrayList<>();
        int n = types.size();
        for (int i = 0; i < n; i++) {
            DataGrid dataGrid = new DataGrid(types.get(i), data.get(i));
            dataGrids.add(dataGrid);
        }
    }

    public List<DataGrid> getDataGrids() {
        return dataGrids;
    }
}
