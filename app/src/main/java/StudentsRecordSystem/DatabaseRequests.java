/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package StudentsRecordSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author Jomari Tenorio
 */
public class DatabaseRequests extends DatabaseUtility {

    /**
     *
     * For initialization of database
     * @param databaseName
    */
    DatabaseRequests(String databaseName) {
        String defaultPath = System.getProperty("user.home") + "\\SchoolRecordSystem\\";
        if (!Files.exists(Paths.get((databaseName.endsWith(".db"))? defaultPath+databaseName : defaultPath+databaseName+".db"))) {
            try {
                Files.createDirectories(Paths.get(System.getProperty("user.home") + "\\SchoolRecordSystem"));
                this.setDatabaseName(System.getProperty("user.home") + "\\SchoolRecordSystem\\" + databaseName);
                if (!initializeDatabaseTable()) {
                    System.exit(1);
                }
            } catch (IOException ex) {
                Logger.getLogger(DatabaseRequests.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.setDatabaseName(System.getProperty("user.home") + "\\SchoolRecordSystem\\" + databaseName);
        }
    }
    
    /**
     * Initialize the database when the application first run
     * @return Boolean 
     */
    private boolean initializeDatabaseTable(){
        String sqlQuery = "CREATE TABLE students_tbl (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                + "first_name VARCHAR(50) NOT NULL, last_name VARCHAR(50) NOT NULL,"
                + "age INT NOT NULL, phone VARCHAR(20) NOT NULL, course VARHCAR(10) NOT NULL,"
                + "student_no VARHCAR(6) NOT NULL)";
        if (ExecuteQuery("DROP TABLE IF EXISTS students_tbl")) {
            return ExecuteQuery(sqlQuery);
        }
        return false;
    }
    
    /**
     * Set the column header of the table
     * @param tableModel 
     */
    private void fillColumnDataHeaders(DefaultTableModel tableModel) {
        String[] columnNames = getColumnNames();
        if (columnNames != null) {
            for(String columName : columnNames) {
                tableModel.addColumn(columName.replace("_", " ").toUpperCase());
            }
        }
    }
    
    /**
     * Set the data row of the table
     * @param tableModel
     * @param table
     * @param resData
     * @return Boolean
     */
    private boolean fillDataTableRows(DefaultTableModel tableModel, JTable table, HashMap<Connection,ResultSet> rawData) {
        Connection connection = (Connection) rawData.keySet().toArray()[0];
        ResultSet resData = rawData.get(connection);
        if (resData != null) {
            try {
                fillColumnDataHeaders(tableModel);
                String[] columns = {"id", "first_name", "last_name", "age", "phone", "course", "student_no"};
                DefaultTableCellRenderer render = new DefaultTableCellRenderer();
                render.setHorizontalAlignment(JLabel.CENTER);
                if (resData.next()) {
                    do {
                        Object[] data = new Object[7];
                        for (int i = 0; i < columns.length; i++) {
                            data[i] = (Object)resData.getString(columns[i]);
                        }
                        tableModel.addRow(data);
                    } while(resData.next());
                    table.setModel(tableModel);
                    TableColumn tableColumn = table.getColumnModel().getColumn(0);
                    tableColumn.setPreferredWidth(0);
                    tableColumn.setMinWidth(0);
                    tableColumn.setMaxWidth(0);
                    TableModel tableM = table.getModel();

                    for (int columnIndex = 0; columnIndex < tableM.getColumnCount(); columnIndex++)
                    {
                        table.getColumnModel().getColumn(columnIndex).setCellRenderer(render);
                    }
                    return true;
                } else {
                    table.setModel(tableModel);
                    return false;
                }
                
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseRequests.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Close(connection);
            }
        }
        return false;
    }
    
    /**
     * Initialize the JTable rows and columns
     * @param table 
     */
    public void initializeTable(JTable table) {
        HashMap<Connection,ResultSet> resData = RetrieveQuery("SELECT id, first_name, last_name, age, phone, course, student_no FROM students_tbl;");
        fillDataTableRows(new DefaultTableModel(), table, resData);
    }
    
    /**
     * Get the column names from the database
     * @return String[]
     */
    private String[] getColumnNames() {
        String[] columns = new String[7];
        HashMap<Connection, ResultSet> rawData = RetrieveQuery("SELECT name FROM PRAGMA_TABLE_INFO('students_tbl');");
        Connection key = (Connection) rawData.keySet().toArray()[0];
        ResultSet columnNames = rawData.get(key);
        try {
           if (columnNames != null) {
               int index = 0;
               while(columnNames.next()) {
                    columns[index++] = columnNames.getString("name");
               }
               return columns;
           } else {
               return null;
           }
        }catch(SQLException e) {
            return null;
        } finally {
            Close(key);
        }
    }
    
    /**
     * Use for searching database table
     * @param table
     * @param searchQuery
     * @return Boolean
     */
    public boolean searchData(JTable table, String searchQuery) {
        HashMap<Connection,ResultSet> rawData;
        if (!searchQuery.isBlank() || searchQuery.isEmpty())
            rawData = RetrieveQuery("SELECT id, first_name, last_name, age, phone, course, student_no FROM students_tbl WHERE " +
                String.format("first_name LIKE '%s%%' OR last_name LIKE '%s%%' OR age LIKE '%s%%' OR phone LIKE '%s%%' OR course LIKE '%s%%' OR student_no LIKE '%s%%';", searchQuery,
                            searchQuery, searchQuery, searchQuery, searchQuery, searchQuery));
        else 
            rawData = RetrieveQuery("SELECT id, first_name, last_name, age, phone, course, student_no FROM students_tbl;");
        return fillDataTableRows(new DefaultTableModel(), table, rawData);
    }
    
    /**
     * Use for inserting data in the database table
     * @param data
     * @param table
     * @return Boolean
     */
    public boolean insertData(String[] data, JTable table) {
        if (ExecuteQuery(String.format("INSERT INTO students_tbl (first_name, last_name, age, phone,course, student_no) VALUES ('%s', '%s', %s , '%s', '%s', '%s');", data[0], data[1], data[2], data[3], data[4], data[5]))) {
            HashMap<Connection,ResultSet> rawData = RetrieveQuery("SELECT id, first_name, last_name, age, phone, course, student_no FROM students_tbl;");
            fillDataTableRows(new DefaultTableModel(), table, rawData);
            return true;
        }
        return false;
    }
    
    /**
     * Use for updating data in database table
     * @param data
     * @param table
     * @param id
     * @return Boolean
     */
    public boolean updateData(String[] data, JTable table, String id) {
        if(ExecuteQuery(String.format("UPDATE students_tbl SET first_name = '%s',"
                + " last_name = '%s', age = %s, phone = '%s', course = '%s', student_no = '%s'"+
                " WHERE id = %s;", data[0],data[1],data[2],data[3],data[4],data[5], id))){
            HashMap<Connection, ResultSet> rawData = RetrieveQuery("SELECT id, first_name, last_name, age, phone, course, student_no FROM students_tbl;");
            return fillDataTableRows(new DefaultTableModel(), table, rawData);
            

        }
        return false;
    }
    
    /**
     * Use for deleting data in the database table
     * @param table
     * @param id
     * @return Boolean
     */
    public boolean deleteData(JTable table, String id) {
        if(ExecuteQuery(String.format("DELETE FROM students_tbl WHERE id = %s", id))) {
            HashMap<Connection,ResultSet> rawData = RetrieveQuery("SELECT id, first_name, last_name, age, phone, course, student_no FROM students_tbl;");
            fillDataTableRows(new DefaultTableModel(), table, rawData);
            return true;
        }
        return false;
    }
}
