
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.TransferMode;
import javafx.scene.input.Dragboard;
import javafx.scene.control.cell.TextFieldTableCell;
import java.io.FileReader;
import java.io.IOException;


import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.scene.control.*;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;
import org.json.JSONArray;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.*;
import javafx.scene.Node;
import java.io.FileOutputStream;
import java.io.File;

import javafx.scene.layout.Priority;
import javafx.geometry.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;


public class DatabaseJavaFX1Version41 extends Application {
    final String apiKey = "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";
    String url ;
    String user = "root";
    String password = "214319";
    private VBox dynamicFormArea; 
    private TableView<ObservableList<Object>> tableView1;
    private String currentDisplayedTableName;
    private static Connection connection=null; 
    private boolean isConnected = false;
    private MenuItem exportToExcelItem;
    private MenuItem testConnectionItem;
    private MenuItem AIItem;
    private MenuItem comingSoonItem;
    private Menu fileMenu;
    private MenuBar menuBar;


    TextArea logArea = new TextArea();
    Button createBtn = new Button("CREATE");
    Button alterBtn=new Button("ALTER");
    Button insertBtn = new Button("INSERT");
    Button updateBtn = new Button("UPDATE");
    Button selectBtn = new Button("SELECT");
    Button displayBtn = new Button("DISPLAY");
    Button deleteBtn = new Button("DELETE");
    Button truncateBtn = new Button("TRUNCATE");
    Button StatBtn=new Button("STATISTICS");
    Button dropBtn = new Button("DROP");
    Button clearLogBtn = new Button("CLEAR LOG");
    public static void main(String[] args) throws Exception {
        launch(args);
    }
    private boolean testAndConnectDatabase(String username, String password, String dbName) {
    try {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", username, password);
        ResultSet rs = conn.createStatement().executeQuery("SHOW DATABASES LIKE '" + dbName + "'");
        if (!rs.next()) {
            conn.createStatement().executeUpdate("CREATE DATABASE " + dbName);
            log("Database '" + dbName + "' created.\n");
        } else {
            log("Using existing database '" + dbName + "'.\n");
        }
        conn.close();

        url = "jdbc:mysql://localhost:3306/" + dbName;
        connection = DriverManager.getConnection(url, username, password);
        isConnected = true;
        log("DB Connection to '" + dbName + "' established.\n");
        return true;
    } catch (SQLException e) {
        isConnected = false;
        log("DB Connection Failed: " + e.getMessage() + "\n");
        return false;
    }
}
    @Override
    public void start(Stage primaryStage) {
        
        exportToExcelItem = new MenuItem("Export to Excel");
        testConnectionItem = new MenuItem("Test Connection");
        AIItem = new MenuItem("AI-SQL");
        comingSoonItem = new MenuItem("Coming soon....");

        
        testConnectionItem.setOnAction(e -> showConnectionForm());
        exportToExcelItem.setOnAction(e -> showExportForm());
        AIItem.setOnAction(e -> IntegrateAI());
        fileMenu = new Menu("File");
        fileMenu.getItems().addAll(testConnectionItem, exportToExcelItem, AIItem, comingSoonItem);

        menuBar = new MenuBar(fileMenu);
    
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        
        createBtn.setOnAction(e -> showCreateTableWindow());
        insertBtn.setOnAction(e -> showInsertWindow());
        updateBtn.setOnAction(e -> showUpdateWindow());
        displayBtn.setOnAction(e-> displayTableWithCheckbox());
        deleteBtn.setOnAction(e -> deleteSelectedRows()); 
        StatBtn.setOnAction(e->showStatisticalOperations());
        selectBtn.setOnAction(e->executeCustomQueryWithCheckboxSupport()); 
        alterBtn.setOnAction(e->showAlterTableForm());   
        truncateBtn.setOnAction(e -> TruncateTableAction("Truncate", "TRUNCATE TABLE "));
        dropBtn.setOnAction(e -> DropTableAction("Drop", "DROP TABLE "));
        clearLogBtn.setOnAction(e -> logArea.clear());

        buttonBar.getChildren().addAll(createBtn,alterBtn, insertBtn, updateBtn, displayBtn,selectBtn, deleteBtn, truncateBtn, dropBtn,StatBtn,clearLogBtn);
        VBox topBox = new VBox(menuBar, buttonBar);

        dynamicFormArea = new VBox(10);
        dynamicFormArea.setStyle("-fx-padding: 10;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setOrientation(Orientation.VERTICAL);
        verticalSplit.getItems().addAll(dynamicFormArea, logArea);
        verticalSplit.setDividerPositions(0.6);

      
        tableView1 = new TableView<>();
        tableView1.setPlaceholder(new Label("Query results will appear here."));
        VBox tableBox = new VBox(tableView1);
        VBox.setVgrow(tableView1, Priority.ALWAYS);
        disableAllButtons();

        SplitPane horizontalSplit = new SplitPane();
        horizontalSplit.getItems().addAll(verticalSplit, tableBox);
        horizontalSplit.setDividerPositions(0.5);

        VBox centerPane = new VBox(horizontalSplit);
        VBox.setVgrow(horizontalSplit, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(centerPane);
        root.setOnDragOver(event -> {
            if (event.getGestureSource() != root && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        root.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (file.getName().endsWith(".csv")) {
                    log("CSV dropped: " + file.getName());
                    handleCSV(file);
                } else {
                    log("Only .CSV Files supported.");
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });


        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("style11.css").toExternalForm());
        primaryStage.setTitle("JavaFX SQL Tool");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public void showStatisticalOperations() {
    dynamicFormArea.getChildren().clear();
    tableView1.getItems().clear();
    tableView1.getColumns().clear();

    Label tableLabel = new Label("Enter Table Name: ");
    TextField tableField = new TextField();
    tableField.setPromptText("e.g., employees");

    ContextMenu suggestionMenu = new ContextMenu();

    
    tableField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal.isEmpty()) {
            suggestionMenu.hide();
            return;
        }

        ObservableList<MenuItem> suggestions = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             ResultSet rs = conn.getMetaData().getTables(null, null, newVal + "%", new String[]{"TABLE"})) {

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                MenuItem item = new MenuItem(tableName);
                item.setOnAction(e -> {
                    tableField.setText(tableName);
                    suggestionMenu.hide();
                });
                suggestions.add(item);
            }

            if (!suggestions.isEmpty()) {
                suggestionMenu.getItems().setAll(suggestions);
                if (!suggestionMenu.isShowing()) {
                    suggestionMenu.show(tableField, Side.BOTTOM, 0, 0);
                }
            } else {
                suggestionMenu.hide();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    });

    Button loadBtn = new Button("Load Columns");

    ComboBox<String> columnComboBox = new ComboBox<>();
    columnComboBox.setPromptText("Select Column");

    ComboBox<String> operationComboBox = new ComboBox<>();
    operationComboBox.getItems().addAll("MAX", "MIN", "AVG", "SUM", "COUNT");
    operationComboBox.setPromptText("Select Operation");

    Button calcBtn = new Button("Calculate");
    calcBtn.setDisable(true);

    

    loadBtn.setOnAction(e -> {
        String tableName = tableField.getText().trim();
        if (tableName.isEmpty()) return;

        columnComboBox.getItems().clear();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM `" + tableName + "`")) {

            while (rs.next()) {
                String colName = rs.getString("Field");
                columnComboBox.getItems().add(colName);
            }

            if (!columnComboBox.getItems().isEmpty()) {
                calcBtn.setDisable(false);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Failed to load columns: " + ex.getMessage());
        }
    });

    calcBtn.setOnAction(e -> {
        String table = tableField.getText().trim();
        String column = columnComboBox.getValue();
        String operation = operationComboBox.getValue();

        if (table == null || column == null || operation == null) {
            log("❌ Please fill all fields.");
            return;
        }

        String query = "SELECT " + operation + "(`" + column + "`) FROM `" + table + "`";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                Object result = rs.getObject(1);
                log("Result: " + result);
            } else {
                log("No data returned.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            log("SQL Error: " + ex.getMessage());
        }
    });

    VBox layout = new VBox(15,
            tableLabel, tableField, loadBtn,
            new Label("Column:"), columnComboBox,
            new Label("Operation:"), operationComboBox,
            calcBtn
    );
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);

    dynamicFormArea.getChildren().add(layout);
}

    public void showAlterTableForm() {
    dynamicFormArea.getChildren().clear();
    tableView1.getItems().clear();
    tableView1.getColumns().clear();

    Label tableLabel = new Label("Enter Table Name: ");
    TextField tableField = new TextField();
    tableField.setPromptText("e.g., employees");

    ContextMenu suggestionMenu = new ContextMenu();

    tableField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal.isEmpty()) {
            suggestionMenu.hide();
            return;
        }

        ObservableList<MenuItem> suggestions = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             ResultSet rs = conn.getMetaData().getTables(null, null, newVal + "%", new String[]{"TABLE"})) {

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                MenuItem item = new MenuItem(tableName);
                item.setOnAction(e -> {
                    tableField.setText(tableName);
                    suggestionMenu.hide();
                });
                suggestions.add(item);
            }

            if (!suggestions.isEmpty()) {
                suggestionMenu.getItems().setAll(suggestions);
                if (!suggestionMenu.isShowing()) {
                    suggestionMenu.show(tableField, Side.BOTTOM, 0, 0);
                }
            } else {
                suggestionMenu.hide();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    });

    Button submitBtn = new Button("Submit");
    VBox columnEditor = new VBox(10);
    columnEditor.setPadding(new Insets(10));

  
    Label renameLabel = new Label("Rename Table To (optional):");
    TextField renameField = new TextField();
    renameField.setPromptText("Leave empty to keep same");
    renameLabel.setVisible(false);
    renameField.setVisible(false);

    
    Button applyChangesBtn = new Button("Apply Changes");
    applyChangesBtn.setVisible(false);

    submitBtn.setOnAction(e -> {
        columnEditor.getChildren().clear();
        renameField.setText("");  

        String tableName = tableField.getText().trim();
        if (tableName.isEmpty()) return;

        renameLabel.setVisible(true);
        renameField.setVisible(true);
        applyChangesBtn.setVisible(true);  

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM `" + tableName + "`")) {

            while (rs.next()) {
                String colName = rs.getString("Field");
                String colType = rs.getString("Type");

                TextField newColField = new TextField(colName);
                ComboBox<String> typeBox = new ComboBox<>();
                typeBox.getItems().addAll("VARCHAR(100)", "INT", "DATE", "FLOAT", "BOOLEAN", "TEXT", "DATETIME");
                typeBox.setValue(colType);

                HBox hbox = new HBox(10,
                        new Label("Column: " + colName),
                        new Label("→"), newColField,
                        new Label("Type:"), typeBox);
                hbox.setAlignment(Pos.CENTER_LEFT);
                columnEditor.getChildren().add(hbox);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Failed to load columns: " + ex.getMessage());
        }
    });

    applyChangesBtn.setOnAction(e -> {
        String oldTableName = tableField.getText().trim();
        String newTableName = renameField.getText().trim();
        if (oldTableName.isEmpty()) {
            log("Please enter the table name.");
            return;
        }

        if (newTableName.isEmpty()) {
            newTableName = oldTableName;
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            if (!newTableName.equals(oldTableName)) {
                stmt.executeUpdate("RENAME TABLE `" + oldTableName + "` TO `" + newTableName + "`");
            }

            for (Node node : columnEditor.getChildren()) {
                if (node instanceof HBox hbox) {
                    Label colLabel = (Label) hbox.getChildren().get(0);
                    TextField newColField = (TextField) hbox.getChildren().get(2);
                    ComboBox<?> typeBox = (ComboBox<?>) hbox.getChildren().get(4);

                    String oldCol = colLabel.getText().replace("Column: ", "").trim();
                    String newCol = newColField.getText().trim();
                    String newType = typeBox.getValue().toString().trim();

                    if (!oldCol.equals(newCol) || !newType.isEmpty()) {
                        String alterSQL = "ALTER TABLE `" + newTableName + "` CHANGE `" + oldCol + "` `" + newCol + "` " + newType;
                        stmt.executeUpdate(alterSQL);
                    }
                }
            }

            log("Table updated successfully.");

        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Error: " + ex.getMessage());
        }
    });

    VBox layout = new VBox(15,
            tableLabel, tableField,
            submitBtn,
            renameLabel, renameField,
            columnEditor,
            applyChangesBtn  
    );
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);

    dynamicFormArea.getChildren().add(layout);
}

    private void showConnectionForm() {
    dynamicFormArea.getChildren().clear();
    dynamicFormArea.setDisable(false);

    Label userLabel = new Label("Username:");
    TextField userField = new TextField();

    Label passLabel = new Label("Password:");
    PasswordField passField = new PasswordField();

    Label dbLabel = new Label("Database Name:");
    ComboBox<String> dbComboBox = new ComboBox<>();
    dbComboBox.setEditable(true); 

    Label statusLabel = new Label();
    statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

    Button authorizeBtn = new Button("Authorize");
    authorizeBtn.setStyle("-fx-font-size: 16px;");

    authorizeBtn.setOnAction(e -> {
        String username = userField.getText().trim();
        String password = passField.getText().trim();
        String dbName = dbComboBox.getEditor().getText().trim();

        if (username.isEmpty() || password.isEmpty() || dbName.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        // Step 1: Face Recognition
        new Thread(() -> {
            boolean faceOK = runFaceAuthScript();

            javafx.application.Platform.runLater(() -> {
                if (!faceOK) {
                    statusLabel.setText("Face not authorized.");
                    return;
                }

                // Step 2: Check/Create DB and connect
                boolean dbOK = testAndConnectDatabase(username, password, dbName);
                if (dbOK) {
                    log("Authorized & Connected to DB.");
                    
                    enableAllButtons();
                    dynamicFormArea.getChildren().clear();
                    testConnectionItem.setDisable(true);
                } else {
                    statusLabel.setText("DB connection failed.");
                }
            });
        }).start();
    });

    dynamicFormArea.getChildren().addAll(
            userLabel, userField,
            passLabel, passField,
            dbLabel, dbComboBox,
            authorizeBtn,
            statusLabel
    );

    loadDatabaseList(userField, passField, dbComboBox);
}
private void loadDatabaseList(TextField userField, PasswordField passField, ComboBox<String> dbComboBox) {
    userField.textProperty().addListener((obs, oldVal, newVal) -> updateDBList(newVal, passField.getText(), dbComboBox));
    passField.textProperty().addListener((obs, oldVal, newVal) -> updateDBList(userField.getText(), newVal, dbComboBox));
}

private void updateDBList(String username, String password, ComboBox<String> dbComboBox) {
    if (username.isEmpty() || password.isEmpty()) return;

    new Thread(() -> {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", username, password);
            ResultSet rs = conn.createStatement().executeQuery("SHOW DATABASES");

            List<String> dbs = new ArrayList<>();
            while (rs.next()) {
                dbs.add(rs.getString(1));
            }
            conn.close();

            javafx.application.Platform.runLater(() -> {
                dbComboBox.getItems().clear();
                dbComboBox.getItems().addAll(dbs);
            });
        } catch (SQLException e) {
            
        }
    }).start();
}


    

private boolean runFaceAuthScript() {
    try {
        ProcessBuilder pb = new ProcessBuilder("python", "FaceCompare2.py");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }
        int exitCode = process.waitFor();
        String result = json.toString();
        return exitCode == 0 && result.contains("\"authorized\"");
    } catch (IOException | InterruptedException ex) {
        ex.printStackTrace();
        return false;
    }
}


    private void disableAllButtons() {
        createBtn.setDisable(true);
        insertBtn.setDisable(true);
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        displayBtn.setDisable(true);
        selectBtn.setDisable(true);
        truncateBtn.setDisable(true);
        dropBtn.setDisable(true);
        clearLogBtn.setDisable(true);
        exportToExcelItem.setDisable(true);
        AIItem.setDisable(true);
        comingSoonItem.setDisable(true);
        tableView1.setDisable(true);
        logArea.setDisable(true);
        dynamicFormArea.setDisable(true);
        alterBtn.setDisable(true);
        StatBtn.setDisable(true);

    }

    private void enableAllButtons() {
        createBtn.setDisable(false);
        insertBtn.setDisable(false);
        updateBtn.setDisable(false);
        deleteBtn.setDisable(false);
        displayBtn.setDisable(false);
        selectBtn.setDisable(false);
        truncateBtn.setDisable(false);
        dropBtn.setDisable(false);
        clearLogBtn.setDisable(false);
        exportToExcelItem.setDisable(false);
        AIItem.setDisable(false);
        comingSoonItem.setDisable(false);
        tableView1.setDisable(false);
        logArea.setDisable(false);
        dynamicFormArea.setDisable(false);
        alterBtn.setDisable(false);
        StatBtn.setDisable(false);
    }
    private void log(String msg) {
    String timestamp = java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    logArea.appendText("[" + timestamp + "] " + msg + "\n");
}

    private void showExportForm() {
    ComboBox<String> exportTableCombo = new ComboBox<>();
    exportTableCombo.getItems().addAll(getTableNamesFromDatabase());
    exportTableCombo.setPromptText("Select Table");
    Label label = new Label("Select Table to Export:");
    Button exportBtn = new Button("Export to Excel");
    exportBtn.setOnAction(e -> {
        String selectedTable = exportTableCombo.getValue();
        if (selectedTable != null && !selectedTable.isEmpty()) {
            exportTableToExcel(selectedTable);
        } else {
            log(" Please select a table first.");
        }
    });
    VBox box = new VBox(10, label, exportTableCombo, exportBtn);
    dynamicFormArea.getChildren().clear();
    dynamicFormArea.getChildren().add(box);
}
 
private void exportTableToExcel(String tableName) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Excel File");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
    File file = fileChooser.showSaveDialog(null);
    if (file == null) {
        log("Export cancelled.");
        return;
    }
    try (Connection conn = DriverManager.getConnection(url, user,password);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
         Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet(tableName);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Row header = sheet.createRow(0);
        for (int i = 1; i <= columnCount; i++) {
            header.createCell(i - 1).setCellValue(metaData.getColumnName(i));
        }
        int rowIndex = 1;
        while (rs.next()) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                row.createCell(i - 1).setCellValue(value != null ? value.toString() : "");
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        log("Data exported successfully to:\n" + file.getAbsolutePath());
    } catch (Exception e) {
        log(" Error during export:\n" + e.getMessage());
    }
}
    private void displayTableWithCheckbox() {
    dynamicFormArea.getChildren().clear();
tableView1.getItems().clear();
tableView1.getColumns().clear();

Label tableLabel = new Label("Enter Table Name: ");
TextField tableField = new TextField();
tableField.setPromptText("e.g., employees");


ContextMenu suggestionMenu = new ContextMenu();

tableField.textProperty().addListener((observable, oldValue, newValue) -> {
    if (newValue.isEmpty()) {
        suggestionMenu.hide();
        return;
    }

    ObservableList<MenuItem> suggestions = FXCollections.observableArrayList();

    try (Connection conn = DriverManager.getConnection(url, user, password);
         ResultSet rs = conn.getMetaData().getTables(null, null, newValue + "%", new String[]{"TABLE"})) {

        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");

            MenuItem item = new MenuItem(tableName);
            item.setOnAction(e -> {
                tableField.setText(tableName);   
                suggestionMenu.hide();           
            });

            suggestions.add(item);
        }

        if (!suggestions.isEmpty()) {
            suggestionMenu.getItems().setAll(suggestions);
            if (!suggestionMenu.isShowing()) {
                suggestionMenu.show(tableField, Side.BOTTOM, 0, 0);
            }
        } else {
            suggestionMenu.hide();
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
});

Button submitBtn = new Button("Submit");

VBox layout = new VBox(15, tableLabel, tableField, submitBtn);
layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

dynamicFormArea.getChildren().add(layout);

    submitBtn.setOnAction(e -> {
        String tableName = tableField.getText().trim();
        if (tableName.isEmpty()) {
            log("Table name cannot be empty.");
            return;
        }
        currentDisplayedTableName = tableName;
        tableView1.getItems().clear();
        tableView1.getColumns().clear();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            
            TableColumn<ObservableList<Object>, Boolean> checkCol = new TableColumn<>("Select");
            checkCol.setCellValueFactory(param -> {
                Object obj = param.getValue().get(0);
                if (obj instanceof BooleanProperty bp) {
                    return bp;
                } else {
                    BooleanProperty newBp = new SimpleBooleanProperty(false);
                    param.getValue().set(0, newBp);
                    return newBp;
                }
            });
            checkCol.setCellFactory(tc -> new CheckBoxTableCell<>());
            tableView1.getColumns().add(checkCol);

            for (int i = 1; i <= columnCount; i++) {
                final int colIndex = i;
                String colName = metaData.getColumnName(i);

                TableColumn<ObservableList<Object>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(data -> {
                    Object value = data.getValue().get(colIndex);
                    return new SimpleStringProperty(value != null ? value.toString() : "");
                });

                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(event -> {
                    String newValue = event.getNewValue();
                    int rowIndex = event.getTablePosition().getRow();
                    int tableColIndex = event.getTablePosition().getColumn();
                    ObservableList<Object> row = event.getTableView().getItems().get(rowIndex);

                    Object oldValue = row.get(tableColIndex);
                    if (newValue.equals(oldValue)) return;

                    row.set(tableColIndex, newValue);
                    String updatedColumn = event.getTableColumn().getText();
                    StringBuilder whereClause = new StringBuilder();

                    for (int j = 1; j < event.getTableView().getColumns().size(); j++) {
                    if (j == tableColIndex) continue;

                    String whereCol = tableView1.getColumns().get(j).getText();
                    Object whereVal = row.get(j);
                    if (whereCol != null && !whereCol.isBlank()) {
                        if (whereClause.length() > 0) whereClause.append(" AND ");

                        whereClause.append("`")
                                .append(whereCol.trim().replaceAll("[^a-zA-Z0-9_]", "_"))
                                .append("` = '")
                                .append(whereVal != null ? whereVal.toString().replace("'", "''") : "")
                                .append("'");
                    }
                }

                    String sql = "UPDATE " + currentDisplayedTableName +
                                 " SET " + updatedColumn + " = '" + newValue.replace("'", "''") + "' " +
                                 "WHERE " + whereClause;

                    try (Connection conn2 = DriverManager.getConnection(url, user, password);
                         Statement stmt2 = conn2.createStatement()) {
                        int updated = stmt2.executeUpdate(sql);
                        log("Cell updated: " + updated + " row(s) affected.");
                    } catch (SQLException ex) {
                        log("Edit failed: " + ex.getMessage());
                    }
                });

                tableView1.getColumns().add(col);
            }

            ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                row.add(new SimpleBooleanProperty(false)); 
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }
            tableView1.setItems(data);
            tableView1.setEditable(true); 
            log("Displayed table: " + tableName);

        } catch (SQLException ex) {
            log("Failed to display table: " + ex.getMessage());
        }
    });
}
    private void refreshTableData(String currentDisplayedTableName){
    if (currentDisplayedTableName == null || currentDisplayedTableName.isEmpty()) return;
    tableView1.getItems().clear();
    tableView1.getColumns().clear();
    try (Connection conn = DriverManager.getConnection(url, user, password);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM " + currentDisplayedTableName)) {

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        TableColumn<ObservableList<Object>, Boolean> checkCol = new TableColumn<>("Select");
        checkCol.setCellValueFactory(param -> {
            Object obj = param.getValue().get(0);
            if (obj instanceof BooleanProperty bp) {
                return bp;
            } else {
                BooleanProperty newBp = new SimpleBooleanProperty(false);
                param.getValue().set(0, newBp); 
                return newBp;
            }
        });
        checkCol.setCellFactory(tc -> new CheckBoxTableCell<>());
        tableView1.getColumns().add(checkCol);
        for (int i = 1; i <= columnCount; i++) {
            final int colIndex = i;
            String colName = metaData.getColumnName(i);

            TableColumn<ObservableList<Object>, String> col = new TableColumn<>(colName);
            col.setCellValueFactory(data -> {
                Object value = data.getValue().get(colIndex);
                return new SimpleStringProperty(value != null ? value.toString() : "");
            });
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setOnEditCommit(event -> {
                String newValue = event.getNewValue();
                int rowIndex = event.getTablePosition().getRow();
                int tableColIndex = event.getTablePosition().getColumn();
                ObservableList<Object> row = event.getTableView().getItems().get(rowIndex);

                Object oldValue = row.get(tableColIndex);
                if (newValue.equals(oldValue)) return;

                row.set(tableColIndex, newValue);
                String updatedColumn = event.getTableColumn().getText();
                StringBuilder whereClause = new StringBuilder();
                for (int j = 1; j < event.getTableView().getColumns().size(); j++) {
                    if (j == tableColIndex) continue;
                    String whereCol = tableView1.getColumns().get(j).getText();
                    Object whereVal = row.get(j);
                    if (whereClause.length() > 0) whereClause.append(" AND ");
                    whereClause.append("").append(whereCol).append(" = '")
                               .append(whereVal != null ? whereVal.toString().replace("'", "''") : "")
                               .append("'");
                }

                String sql = "UPDATE " + currentDisplayedTableName +
                             " SET " + updatedColumn + " = '" + newValue.replace("'", "''") + "' " +
                             "WHERE " + whereClause;

                try (Connection conn2 = DriverManager.getConnection(url, user, password);
                     Statement stmt2 = conn2.createStatement()) {
                    stmt2.executeUpdate(sql);
                    log("Updated in DB: " + updatedColumn + " = " + newValue);
                } catch (SQLException ex) {
                    log("Update failed: " + ex.getMessage());
                }
            });

            tableView1.getColumns().add(col);
        }
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        while (rs.next()) {
            ObservableList<Object> row = FXCollections.observableArrayList();
            row.add(new SimpleBooleanProperty(false));
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getString(i));
            }
            data.add(row);
        }

        tableView1.setEditable(true); 
        tableView1.setItems(data);

    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}


    private void deleteSelectedRows() {
    String tableName = currentDisplayedTableName;
    if (tableName == null || tableName.isEmpty()) {
        log("⚠ Table name is not set.");
        return;
    }

    ObservableList<ObservableList<Object>> items = tableView1.getItems();
    List<ObservableList<Object>> toDelete = new ArrayList<>();
    List<String> columnNames = new ArrayList<>();

    
    for (int i = 1; i < tableView1.getColumns().size(); i++) {
        String colText = tableView1.getColumns().get(i).getText();
        String safeCol = colText.replaceAll("[^a-zA-Z0-9_]", "_");
        columnNames.add(safeCol);
    }

    for (ObservableList<Object> row : items) {
        BooleanProperty selected = (BooleanProperty) row.get(0);
        if (selected.get()) {
            toDelete.add(row);
        }
    }

    if (toDelete.isEmpty()) {
        log("No rows selected for deletion.");
        return;
    }

    try (Connection conn = DriverManager.getConnection(url, user, password)) {
        for (ObservableList<Object> row : toDelete) {
            StringBuilder where = new StringBuilder(" WHERE ");
            for (int i = 0; i < columnNames.size(); i++) {
                where.append("`").append(columnNames.get(i)).append("` = ?");
                if (i < columnNames.size() - 1) where.append(" AND ");
            }

            String deleteSQL = "DELETE FROM `" + tableName + "`" + where;

            try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                for (int i = 0; i < columnNames.size(); i++) {
                    pstmt.setString(i + 1, row.get(i + 1).toString());
                }
                pstmt.executeUpdate();
            }
        }

        log("🗑 Deleted " + toDelete.size() + " row(s).");
        refreshTableData(currentDisplayedTableName);

    } catch (SQLException ex) {
        log("SQL Error during deletion: " + ex.getMessage());
    }
}



    private void showCreateTableWindow() {
    dynamicFormArea.getChildren().clear();
    tableView1.getItems().clear();
    tableView1.getColumns().clear();


    Label tableLabel = new Label("Enter Table Name: ");
    TextField tableField = new TextField();
    tableField.setPromptText("e.g., employees");

    Button submitBtn = new Button("Submit");

    VBox layout = new VBox(15, tableLabel, tableField, submitBtn);
    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

    dynamicFormArea.getChildren().add(layout);

    submitBtn.setOnAction(ev -> {
        String tableName = tableField.getText().trim();

        if (tableName.isEmpty()) {
            log("Table name can't be empty.\nPlease enter a table name to proceed.");
            return;
        }
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData dbMeta = conn.getMetaData();
            try (ResultSet tables = dbMeta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (tables.next()) {
                    log("Table '" + tableName + "' already exists! Please choose another name.");
                } else {
                    getFieldDetails(tableName); 
                }
            }
        } catch (SQLException ex) {
            log("Database error while checking table existence: " + ex.getMessage());
        }
    });
    }
    private void IntegrateAI(){
        dynamicFormArea.getChildren().clear();
        tableView1.getItems().clear();
        tableView1.getColumns().clear();
        Label tableLabel = new Label("Enter Text :");
        TextField tableField = new TextField();
        tableField.setPromptText("Provide statement to convert it into SQL and execute in database...");
        Button submitBtn = new Button("Submit");
        VBox layout = new VBox(15, tableLabel, tableField, submitBtn);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        dynamicFormArea.getChildren().add(layout);
        submitBtn.setOnAction(ai->{
            chatgptIntegrationAndDisplay("Generate only sql query for : "+tableField.getText(),apiKey);
        });
    }
    private List<String> getTableNamesFromDatabase() {
    List<String> tableList = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(url, user, password)) {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        while (rs.next()) {
            tableList.add(rs.getString(3));
        }
    } catch (SQLException e) {
        log("Error fetching tables: " + e.getMessage());
    }
    return tableList;
}
    
    private TextField tf = new TextField();

    private void showInsertWindow() {
    dynamicFormArea.getChildren().clear();
tableView1.getItems().clear();
tableView1.getColumns().clear();

Label tableLabel = new Label("Enter Table Name: ");
TextField tableField = new TextField();
tableField.setPromptText("e.g., employees");


ContextMenu suggestionMenu = new ContextMenu();

tableField.textProperty().addListener((observable, oldValue, newValue) -> {
    if (newValue.isEmpty()) {
        suggestionMenu.hide();
        return;
    }

    ObservableList<MenuItem> suggestions = FXCollections.observableArrayList();

    try (Connection conn = DriverManager.getConnection(url, user, password);
         ResultSet rs = conn.getMetaData().getTables(null, null, newValue + "%", new String[]{"TABLE"})) {

        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");

            MenuItem item = new MenuItem(tableName);
            item.setOnAction(e -> {
                tableField.setText(tableName);   
                suggestionMenu.hide();           
            });

            suggestions.add(item);
        }

        if (!suggestions.isEmpty()) {
            suggestionMenu.getItems().setAll(suggestions);
            if (!suggestionMenu.isShowing()) {
                suggestionMenu.show(tableField, Side.BOTTOM, 0, 0);
            }
        } else {
            suggestionMenu.hide();
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
});

Button submitBtn = new Button("Submit");

VBox layout = new VBox(15, tableLabel, tableField, submitBtn);
layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

dynamicFormArea.getChildren().add(layout);

    
    submitBtn.setOnAction(e -> {
        String tableName = tableField.getText().trim();
        refreshTableData(tableName);
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null);
            List<String> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }

            if (columns.isEmpty()) {
                log("Table not found or has no columns.");
                return;
            }

            dynamicFormArea.getChildren().clear();

            VBox insertLayout = new VBox(10);
            insertLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");

            List<TextField> valueFields = new ArrayList<>();

            for (String col : columns) {
                Label colLabel = new Label("Enter value for " + col + ":");
                TextField tf = new TextField();
                insertLayout.getChildren().addAll(colLabel, tf);
                valueFields.add(tf);
            }
            
            deleteBtn.setOnAction(eh->{
                deleteSelectedRows();
            });

            Button insertBtn = new Button("Insert");

            insertLayout.getChildren().add(insertBtn);
            dynamicFormArea.getChildren().add(insertLayout);

            insertBtn.setOnAction(ev -> {
                try (Connection conn2 = DriverManager.getConnection(url, user, password)) {
                    StringBuilder q = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
                    for (int i = 0; i < valueFields.size(); i++) {
                        q.append("?");
                        if (i != valueFields.size() - 1) q.append(", ");
                    }
                    q.append(")");

                    PreparedStatement ps = conn2.prepareStatement(q.toString());
                    for (int i = 0; i < valueFields.size(); i++) {
                        ps.setString(i + 1, valueFields.get(i).getText().trim());
                    }
                    deleteBtn.setOnAction(eh->{
                        deleteSelectedRows();
                    });

                    ps.executeUpdate();
                    refreshTableData(tableName);
                    log("Values inserted successfully into table: " + tableName);
                    for (TextField tf : valueFields) {
                        tf.clear();
                    }
                } catch (SQLException ex) {
                    log("Insert error: " + ex.getMessage());
                }
            });

        } catch (SQLException ex) {
            log("Database error: " + ex.getMessage());
        }
    }); 
    }
    private void showUpdateWindow() {
    dynamicFormArea.getChildren().clear();
    tableView1.getItems().clear();
    tableView1.getColumns().clear();

    Label tableLabel = new Label("Enter Table Name:");
    TextField tableField = new TextField();
    tableField.setPromptText("Start typing table name...");

    ContextMenu suggestionMenu = new ContextMenu();

    tableField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue.isEmpty()) {
            suggestionMenu.hide();
            return;
        }

        ObservableList<MenuItem> suggestions = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             ResultSet rs = conn.getMetaData().getTables(null, null, newValue + "%", new String[]{"TABLE"})) {

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");

                MenuItem item = new MenuItem(tableName);
                item.setOnAction(e -> {
                    tableField.setText(tableName);
                    refreshTableData(tableName);
                    suggestionMenu.hide();
                });

                suggestions.add(item);
            }

            if (!suggestions.isEmpty()) {
                suggestionMenu.getItems().setAll(suggestions);
                if (!suggestionMenu.isShowing()) {
                    suggestionMenu.show(tableField, Side.BOTTOM, 0, 0);
                }
            } else {
                suggestionMenu.hide();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    });

    VBox setBox = new VBox(5);
    VBox whereBox = new VBox(5);
    Button addSetRowBtn = new Button("➕ Add SET Clause");
    Button addWhereRowBtn = new Button("➕ Add WHERE Clause");
    Button updateBtn = new Button("Update");

    addSetRowBtn.setOnAction(e -> {
        String selectedTable = tableField.getText();
        if (selectedTable != null && !selectedTable.trim().isEmpty()) {
            refreshTableData(selectedTable);
            addClauseRow(setBox, selectedTable, logArea);
        } else {
            log("Please enter a valid table name first.");
        }
    });

    addWhereRowBtn.setOnAction(e -> {
        String selectedTable = tableField.getText();
        if (selectedTable != null && !selectedTable.trim().isEmpty()) {
            addClauseRow(whereBox, selectedTable, logArea);
        } else {
            log("Please enter a valid table name first.");
        }
    });

    updateBtn.setOnAction(e -> {
        String selectedTable = tableField.getText();
        if (selectedTable == null || selectedTable.trim().isEmpty()) {
            log("Table name is required.");
            return;
        }
        buildAndExecuteUpdate(selectedTable.trim(), setBox, whereBox, logArea);
    });

    VBox layout = new VBox(12,
            tableLabel, tableField,
            new Label("SET Clauses:"), setBox, addSetRowBtn,
            new Label("WHERE Clauses:"), whereBox, addWhereRowBtn,
            updateBtn
    );
    layout.setStyle("-fx-padding: 20; -fx-alignment: top-left;");
    dynamicFormArea.getChildren().add(layout);
}

private void addClauseRow(VBox box, String tableName, TextArea log) {
    ComboBox<String> columnCombo = new ComboBox<>();
    columnCombo.setPromptText("Column");
    TextField valueField = new TextField();
    valueField.setPromptText("Value");
    Button removeBtn = new Button("❌");

    HBox row = new HBox(10, columnCombo, new Label("="), valueField, removeBtn);
    row.setStyle("-fx-alignment: center-left;");
    removeBtn.setOnAction(e -> box.getChildren().remove(row));
    box.getChildren().add(row);

    if (tableName != null && !tableName.isEmpty()) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null);
            List<String> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();
            columnCombo.getItems().setAll(columns);
        } catch (SQLException e) {
            log.appendText("Could not load columns: " + e.getMessage() + "\n");
        }
    }
}

private void buildAndExecuteUpdate(String tableName, VBox setBox, VBox whereBox, TextArea log) {
    try (Connection conn = DriverManager.getConnection(url, user, password)) {
        ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null);
        Set<String> validColumns = new HashSet<>();
        while (rs.next()) validColumns.add(rs.getString("COLUMN_NAME"));
        rs.close();

        List<String> setClauses = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (Node node : setBox.getChildren()) {
            if (node instanceof HBox row) {
                ComboBox<?> cb = (ComboBox<?>) row.getChildren().get(0);
                TextField tf = (TextField) row.getChildren().get(2);
                String col = String.valueOf(cb.getValue());
                if (col != null && !tf.getText().isEmpty()) {
                    if (!validColumns.contains(col)) {
                        log.appendText("Invalid SET column: " + col + "\n");
                        return;
                    }
                    setClauses.add(col + " = ?");
                    parameters.add(tf.getText());
                }
            }
        }

        for (Node node : whereBox.getChildren()) {
            if (node instanceof HBox row) {
                ComboBox<?> cb = (ComboBox<?>) row.getChildren().get(0);
                TextField tf = (TextField) row.getChildren().get(2);
                String col = String.valueOf(cb.getValue());
                if (col != null && !tf.getText().isEmpty()) {
                    if (!validColumns.contains(col)) {
                        log.appendText("Invalid WHERE column: " + col + "\n");
                        return;
                    }
                    whereClauses.add(col + " = ?");
                    parameters.add(tf.getText());
                }
            }
        }

        if (setClauses.isEmpty()) {
            log.appendText("No SET values provided.\n");
            return;
        }

        String sql = "UPDATE " + tableName + " SET " + String.join(", ", setClauses);
        if (!whereClauses.isEmpty()) {
            sql += " WHERE " + String.join(" AND ", whereClauses);
        }

        log.appendText("Executing SQL: " + sql + "\n");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            int count = ps.executeUpdate();
            log.appendText("Update successful. Rows affected: " + count + "\n");
            currentDisplayedTableName = tableName;
            refreshTableData(tableName);
        }

    } catch (SQLException e) {
        log.appendText("SQL Error: " + e.getMessage() + "\n");
    }
}

    private void DropTableAction(String action, String sqlPrefix) {
        dynamicFormArea.getChildren().clear();
tableView1.getItems().clear();
tableView1.getColumns().clear();

Label tableLabel = new Label("Enter Table Name: ");
TextField tableField = new TextField();
tableField.setPromptText("e.g., employees");

ContextMenu suggestionMenu = new ContextMenu();

tableField.textProperty().addListener((observable, oldValue, newValue) -> {
    if (newValue.isEmpty()) {
        suggestionMenu.hide();
        return;
    }

    ObservableList<MenuItem> suggestions = FXCollections.observableArrayList();

    try (Connection conn = DriverManager.getConnection(url, user, password);
         ResultSet rs = conn.getMetaData().getTables(null, null, newValue + "%", new String[]{"TABLE"})) {

        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");

            MenuItem item = new MenuItem(tableName);
            item.setOnAction(e -> {
                tableField.setText(tableName);   
                suggestionMenu.hide();           
            });

            suggestions.add(item);
        }

        if (!suggestions.isEmpty()) {
            suggestionMenu.getItems().setAll(suggestions);
            if (!suggestionMenu.isShowing()) {
                suggestionMenu.show(tableField, Side.BOTTOM, 0, 0);
            }
        } else {
            suggestionMenu.hide();
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
});

Button submitBtn = new Button("Submit");

VBox layout = new VBox(15, tableLabel, tableField, submitBtn);
layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

dynamicFormArea.getChildren().add(layout);

        
        submitBtn.setOnAction(e -> {
            String tableName = tableField.getText().trim();
            
            if (tableName.isEmpty()) {
                log("Please Enter the table name to proceed");
                return;
            }
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sqlPrefix + tableName);
                log("Table dropped successfully");
            } catch (SQLException ex) {
                log("Error:Can't drop the table\nPlease check table exist or not\nplease enter correct table name\n.");
            }
        });

    }

    private void TruncateTableAction(String action, String sqlPrefix) {
        dynamicFormArea.getChildren().clear();
tableView1.getItems().clear();
tableView1.getColumns().clear();

Label tableLabel = new Label("Enter Table Name: ");
TextField tableField = new TextField();
tableField.setPromptText("e.g., employees");

ContextMenu suggestionMenu = new ContextMenu();

tableField.textProperty().addListener((observable, oldValue, newValue) -> {
    if (newValue.isEmpty()) {
        suggestionMenu.hide();
        return;
    }

    ObservableList<MenuItem> suggestions = FXCollections.observableArrayList();

    try (Connection conn = DriverManager.getConnection(url, user, password);
         ResultSet rs = conn.getMetaData().getTables(null, null, newValue + "%", new String[]{"TABLE"})) {

        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");

            MenuItem item = new MenuItem(tableName);
            item.setOnAction(e -> {
                tableField.setText(tableName);   
                suggestionMenu.hide();           
            });

            suggestions.add(item);
        }

        if (!suggestions.isEmpty()) {
            suggestionMenu.getItems().setAll(suggestions);
            if (!suggestionMenu.isShowing()) {
                suggestionMenu.show(tableField, Side.BOTTOM, 0, 0);
            }
        } else {
            suggestionMenu.hide();
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
});

Button submitBtn = new Button("Submit");

VBox layout = new VBox(15, tableLabel, tableField, submitBtn);
layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

dynamicFormArea.getChildren().add(layout);

        
        submitBtn.setOnAction(e -> {
            String tableName = tableField.getText().trim();
            
            if (tableName.isEmpty()) {
                log("Please Enter the table name to proceed");
                return;
            }
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sqlPrefix + tableName);
                log("Table truncated successfully");
            } catch (SQLException ex) {
                log("Error:Can't drop the table\nPlease check table exist or not\nplease enter correct table name\n.");
            }
            refreshTableData(tableName);
        });

    }
    private void getFieldDetails(String tableName) {
    dynamicFormArea.getChildren().clear();
    tableView1.getItems().clear();
        tableView1.getColumns().clear();
    VBox layout = new VBox(10);
    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

    List<HBox> fieldRows = new ArrayList<>();

    Button addColumnBtn = new Button("+ Column");
    Button createTableBtn = new Button("Create Table");

    VBox fieldList = new VBox(10);

    layout.getChildren().addAll(fieldList, addColumnBtn, createTableBtn);
    dynamicFormArea.getChildren().add(layout);

    addColumnBtn.setOnAction(ev -> {
        HBox row = new HBox(10);
        row.setStyle("-fx-alignment: center;");

        TextField fieldName = new TextField();
        fieldName.setPromptText("Field Name");

        ComboBox<String> dataTypeBox = new ComboBox<>();
        dataTypeBox.getItems().addAll("INT", "VARCHAR(100)", "DOUBLE", "DATE", "BOOLEAN");
        dataTypeBox.setValue("VARCHAR(100)");

        ComboBox<String> constraintBox = new ComboBox<>();
        constraintBox.getItems().addAll("None", "PRIMARY KEY", "FOREIGN KEY", "NOT NULL", "UNIQUE");
        constraintBox.setValue("None");

        Button removeBtn = new Button("❌");
        removeBtn.setOnAction(rmv -> {
            fieldList.getChildren().remove(row);
            fieldRows.remove(row);
        });

        row.getChildren().addAll(fieldName, dataTypeBox, constraintBox, removeBtn);
        fieldRows.add(row);
        fieldList.getChildren().add(row);
    });

    createTableBtn.setOnAction(ev -> {
        if (fieldRows.isEmpty()) {
            log("Add at least one column before creating the table.");
            return;
        }

        StringBuilder query = new StringBuilder("CREATE TABLE " + tableName + " (");

        for (int i = 0; i < fieldRows.size(); i++) {
            HBox row = fieldRows.get(i);
            TextField fieldName = (TextField) row.getChildren().get(0);
            @SuppressWarnings("unchecked")
            ComboBox<String> dataTypeBox = (ComboBox<String>) row.getChildren().get(1);
            @SuppressWarnings("unchecked")
            ComboBox<String> constraintBox = (ComboBox<String>) row.getChildren().get(2);

            String name = fieldName.getText().trim();
            String type = dataTypeBox.getValue();
            String constraint = constraintBox.getValue();

            if (name.isEmpty()) {
                log("Field name at row " + (i + 1) + " cannot be empty.");
                return;
            }

            query.append(name).append(" ").append(type);
            if (!"None".equals(constraint)) {
                query.append(" ").append(constraint);
            }

            if (i != fieldRows.size() - 1) query.append(", ");
        }

        query.append(")");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query.toString());
            log("Table created and displayed successfully!");
            refreshTableData(tableName);
        } catch (SQLException ex) {
            log("Error creating table: " + ex.getMessage());
        }
    });

    
    addColumnBtn.fire();
}
    private HBox createWhereRow() {
    ComboBox<String> colCombo = new ComboBox<>();
    colCombo.getItems().addAll(getColumnsForTable(currentDisplayedTableName));
    colCombo.setPromptText("Column");

    ComboBox<String> opCombo = new ComboBox<>();
    opCombo.getItems().addAll("=", "<", ">", "<=", ">=", "<>", "LIKE", "NOT LIKE");
    opCombo.setPromptText("Op");

    TextField valField = new TextField();
    valField.setPromptText("Value");

    HBox row = new HBox(10, colCombo, opCombo, valField);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
    }
    private void executeCustomQueryWithCheckboxSupport() {
    dynamicFormArea.getChildren().clear();
tableView1.getItems().clear();
tableView1.getColumns().clear();


ComboBox<String> selectColCombo = new ComboBox<>();
selectColCombo.setPromptText("Select column");
selectColCombo.getItems().add("*");

ComboBox<String> orderByColCombo = new ComboBox<>();
orderByColCombo.setPromptText("Order By Column");

TextField tableField = new TextField();
tableField.setPromptText("Enter table name...");

ContextMenu suggestionMenu = new ContextMenu();

tableField.textProperty().addListener((obs, oldText, newText) -> {
    if (newText.isEmpty()) {
        suggestionMenu.hide();
        return;
    }

    ObservableList<MenuItem> suggestions = FXCollections.observableArrayList();

    try (Connection conn = DriverManager.getConnection(url, user, password);
         ResultSet rs = conn.getMetaData().getTables(null, null, newText + "%", new String[]{"TABLE"})) {

        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            MenuItem item = new MenuItem(tableName);
            item.setOnAction(e -> {
                tableField.setText(tableName);
                suggestionMenu.hide();

                
                List<String> cols = getColumnsForTable(tableName);
                selectColCombo.getItems().clear();
                selectColCombo.getItems().add("*");
                selectColCombo.getItems().addAll(cols);

                orderByColCombo.getItems().clear();
                orderByColCombo.getItems().addAll(cols);
            });
            suggestions.add(item);
        }

        if (!suggestions.isEmpty()) {
            suggestionMenu.getItems().setAll(suggestions);
            if (!suggestionMenu.isShowing()) {
                suggestionMenu.show(tableField, Side.BOTTOM, 0, 0);
            }
        } else {
            suggestionMenu.hide();
        }

    } catch (SQLException e) {
        log("Table suggestion failed: " + e.getMessage());
    }
});


VBox whereConditionsBox = new VBox(5);
Button addWhereBtn = new Button("+ Where");

ComboBox<String> orderTypeCombo = new ComboBox<>();
orderTypeCombo.getItems().addAll("ASC", "DESC");
orderTypeCombo.setPromptText("Order");

Button runQueryBtn = new Button("Run Query");

VBox layout = new VBox(10,
        new Label("Table:"), tableField,
        new Label("Select Column:"), selectColCombo,
        new Label("Where Conditions:"), whereConditionsBox,
        addWhereBtn,
        new Label("Order By:"), new HBox(10, orderByColCombo, orderTypeCombo),
        runQueryBtn
);
layout.setPadding(new Insets(15));
layout.setAlignment(Pos.TOP_LEFT);

dynamicFormArea.getChildren().add(layout);
;


    addWhereBtn.setOnAction(e -> {
        ComboBox<String> whereColCombo = new ComboBox<>();
        whereColCombo.setPromptText("Column");

        ComboBox<String> operatorCombo = new ComboBox<>();
        operatorCombo.setPromptText("Operator");
        operatorCombo.getItems().addAll("=", "<", ">", "<=", ">=", "<>", "LIKE", "NOT LIKE");

        TextField valueField = new TextField();
        valueField.setPromptText("Value");

        Button removeBtn = new Button("❌");

        HBox row = new HBox(10, whereColCombo, operatorCombo, valueField, removeBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        String selectedTable = tableField.getText();
        if (selectedTable != null) {
            whereColCombo.getItems().addAll(getColumnsForTable(selectedTable));
        }

        removeBtn.setOnAction(ev -> whereConditionsBox.getChildren().remove(row));
        whereConditionsBox.getChildren().add(row);
    });

    runQueryBtn.setOnAction(e -> {
        String tableName = tableField.getText();
        if (tableName == null || tableName.trim().isEmpty()) {
            log("Please select a table.");
            return;
        }

        String selectColumn = selectColCombo.getValue();
        StringBuilder query = new StringBuilder("SELECT ");
        query.append((selectColumn == null || selectColumn.isEmpty()) ? "*" : selectColumn)
             .append(" FROM ").append(tableName);

        List<String> whereClauses = new ArrayList<>();

        for (Node node : whereConditionsBox.getChildren()) {
            if (node instanceof HBox row) {
                @SuppressWarnings("unchecked")
                ComboBox<String> cb = (ComboBox<String>) row.getChildren().get(0);
                @SuppressWarnings("unchecked")
                ComboBox<String> op = (ComboBox<String>) row.getChildren().get(1);
                TextField tf = (TextField) row.getChildren().get(2);

                if (cb.getValue() != null && op.getValue() != null && !tf.getText().isEmpty()) {
                    String condition = cb.getValue() + " " + op.getValue() + " '" + tf.getText().replace("'", "''") + "'";
                    whereClauses.add(condition);
                }
            }
        }

        if (!whereClauses.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        currentDisplayedTableName = tableName;
        tableView1.getItems().clear();
        tableView1.getColumns().clear();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query.toString())) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            
            TableColumn<ObservableList<Object>, Boolean> checkCol = new TableColumn<>("Select");
            checkCol.setCellValueFactory(param -> {
                Object obj = param.getValue().get(0);
                if (obj instanceof BooleanProperty bp) return bp;
                BooleanProperty newBp = new SimpleBooleanProperty(false);
                param.getValue().set(0, newBp);
                return newBp;
            });
            checkCol.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
            tableView1.getColumns().add(checkCol);

            
            for (int i = 1; i <= columnCount; i++) {
                final int index = i;
                String colName = meta.getColumnName(i);

                TableColumn<ObservableList<Object>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(data -> {
                    Object val = data.getValue().get(index);
                    return new SimpleStringProperty(val != null ? val.toString() : "");
                });

                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(event -> {
                    String newValue = event.getNewValue();
                    int rowIndex = event.getTablePosition().getRow();
                    ObservableList<Object> row = event.getTableView().getItems().get(rowIndex);

                    String editedColName = col.getText();
                    Object oldValue = row.get(index);
                    if (newValue.equals(oldValue)) return;

                    row.set(index, newValue); 

                    StringBuilder whereClause = new StringBuilder();
                    for (int j = 1; j < event.getTableView().getColumns().size(); j++) {
                        if (j == index) continue;
                        String whereCol = event.getTableView().getColumns().get(j).getText();
                        Object whereVal = row.get(j);
                        if (whereClause.length() > 0) whereClause.append(" AND ");
                        whereClause.append("").append(whereCol).append(" = '")
                                .append(whereVal != null ? whereVal.toString().replace("'", "''") : "").append("'");
                    }

                    String updateSQL = "UPDATE " + currentDisplayedTableName + " SET " + editedColName + " = '" +
                                       newValue.replace("'", "''") + "' WHERE " + whereClause;

                    try (Connection updateConn = DriverManager.getConnection(url, user, password);
                         Statement updateStmt = updateConn.createStatement()) {
                        int updated = updateStmt.executeUpdate(updateSQL);
                        log("Edit successful: " + updated + " row(s) updated.");
                    } catch (SQLException ex) {
                        log("Edit failed: " + ex.getMessage());
                    }
                });

                tableView1.getColumns().add(col);
            }

            ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                row.add(new SimpleBooleanProperty(false)); // for checkbox
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

            tableView1.setItems(data);
            tableView1.setEditable(true);
            log("Query executed: " + query);

        } catch (SQLException ex) {
            log("Query failed: " + ex.getMessage());
        }
    });

    deleteBtn.setOnAction(e -> {
        ObservableList<ObservableList<Object>> allRows = tableView1.getItems();
        ObservableList<TableColumn<ObservableList<Object>, ?>> columns = tableView1.getColumns();
        List<ObservableList<Object>> rowsToDelete = new ArrayList<>();

        for (ObservableList<Object> row : allRows) {
            Object checkbox = row.get(0);
            if (checkbox instanceof BooleanProperty bp && bp.get()) {
                rowsToDelete.add(row);
            }
        }
        if (rowsToDelete.isEmpty()) {
            log("No rows selected for deletion.");
            return;
        }
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            for (ObservableList<Object> row : rowsToDelete) {
                StringBuilder whereClause = new StringBuilder();
                for (int i = 1; i < columns.size(); i++) {
                    String colName = columns.get(i).getText();
                    Object value = row.get(i);
                    if (value == null) continue;
                    if (whereClause.length() > 0) whereClause.append(" AND ");
                    whereClause.append("").append(colName).append(" = '")
                               .append(value.toString().replace("'", "''")).append("'");
                }

                String sql = "DELETE FROM " + currentDisplayedTableName + " WHERE " + whereClause;
                try (Statement stmt = conn.createStatement()) {
                    int deleted = stmt.executeUpdate(sql);
                    log("🗑 Deleted " + deleted + " row(s): " + whereClause);
                }
            }
            refreshTableData(currentDisplayedTableName);
        } catch (SQLException ex) {
            log("Error deleting rows: " + ex.getMessage());
        }
    });
    }
    private List<String> getColumnsForTable(String tableName) {
        List<String> columns = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null);
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            log("Error fetching columns: " + e.getMessage());
        }
        return columns;
    }
    public void chatgptIntegrationAndDisplay(String userPrompt, String apiKey) {
    new Thread(() -> {
        try {
            
            tableView1.getItems().clear();
            tableView1.getColumns().clear();
            URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("HTTP-Referer", "https://yourname.github.io");
            conn.setRequestProperty("X-Title", "JavaFX-SQL-Bot");

            String requestBody = """
            {
              "model": "mistralai/mistral-7b-instruct",
              "messages": [{"role": "user", "content": "%s"}]
            }
            """.formatted(userPrompt);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            InputStream inputStream = conn.getResponseCode() < 300 ? conn.getInputStream() : conn.getErrorStream();
            String response = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining());

            JSONObject json = new JSONObject(response);
            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                Platform.runLater(() -> log("⚠ No choices returned from API."));
                return;
            }

            String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
            String mysqlQuery = extractSQL(content);
            if (mysqlQuery.isEmpty()) {
                Platform.runLater(() -> log("⚠ No SQL found in response."));
                return;
            }

            log("Generated SQL: " + mysqlQuery);

            currentDisplayedTableName = "";
            String[] words = mysqlQuery.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].equalsIgnoreCase("from") || words[i].equalsIgnoreCase("into") || words[i].equalsIgnoreCase("update") || words[i].equalsIgnoreCase("table")) {
                    currentDisplayedTableName = words[i + 1].replaceAll("[^a-zA-Z0-9_]", "").trim();
                    break;
                }
            }

            String dbUrl = "jdbc:mysql://localhost:3306/CRTTraining";
            String dbUser = "root";
            String dbPassword = "anand@511";

            Platform.runLater(() -> {
                try (Connection conn2 = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                     Statement stmt = conn2.createStatement()) {

                    boolean hasResultSet = stmt.execute(mysqlQuery);

                    
                    if (hasResultSet) {
                        ResultSet rs = stmt.getResultSet();
                        ResultSetMetaData meta = rs.getMetaData();
                        int colCount = meta.getColumnCount();

                        tableView1.getItems().clear();
                        tableView1.getColumns().clear();

                        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

                      
                        TableColumn<ObservableList<Object>, Boolean> selectCol = new TableColumn<>("Select");
                        selectCol.setCellValueFactory(param -> {
                            Object val = param.getValue().get(0);
                            return (val instanceof BooleanProperty)
                                    ? (BooleanProperty) val
                                    : new SimpleBooleanProperty(false);
                        });
                        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
                        tableView1.getColumns().add(selectCol);

                      
                        for (int i = 1; i <= colCount; i++) {
                            final int colIndex = i;
                            TableColumn<ObservableList<Object>, String> col = new TableColumn<>(meta.getColumnName(i));
                            col.setCellValueFactory(param -> {
                                Object value = param.getValue().get(colIndex);
                                return new SimpleStringProperty(value == null ? "" : value.toString());
                            });
                            col.setCellFactory(TextFieldTableCell.forTableColumn());
                            col.setOnEditCommit(event -> {
                                ObservableList<Object> row = event.getRowValue();
                                row.set(colIndex, event.getNewValue());

                                try {
                                    String pk = meta.getColumnName(1);
                                    Object pkVal = row.get(1);
                                    String colName = meta.getColumnName(colIndex);
                                    String newVal = event.getNewValue();

                                    String updateSQL = "UPDATE " + currentDisplayedTableName + " SET " + colName + " = ? WHERE " + pk + " = ?";
                                    try (PreparedStatement ps = conn2.prepareStatement(updateSQL)) {
                                        ps.setString(1, newVal);
                                        ps.setString(2, pkVal.toString());
                                        ps.executeUpdate();
                                        log("Updated: " + updateSQL);
                                    }
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            });
                            tableView1.getColumns().add(col);
                        }

                        while (rs.next()) {
                            ObservableList<Object> row = FXCollections.observableArrayList();
                            row.add(new SimpleBooleanProperty(false));
                            for (int i = 1; i <= colCount; i++) {
                                row.add(rs.getString(i));
                            }
                            data.add(row);
                        }

                        tableView1.setItems(data);
                        tableView1.setEditable(true);

                    } else {
                        log("Query executed (no result set).");

                        if (!currentDisplayedTableName.isEmpty()) {
                            ResultSet rs = stmt.executeQuery("SELECT * FROM " + currentDisplayedTableName);
                            ResultSetMetaData meta = rs.getMetaData();
                            int colCount = meta.getColumnCount();

                            tableView1.getItems().clear();
                            tableView1.getColumns().clear();

                            ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

                            TableColumn<ObservableList<Object>, Boolean> selectCol = new TableColumn<>("Select");
                            selectCol.setCellValueFactory(param -> {
                                Object val = param.getValue().get(0);
                                return (val instanceof BooleanProperty)
                                        ? (BooleanProperty) val
                                        : new SimpleBooleanProperty(false);
                            });
                            selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
                            tableView1.getColumns().add(selectCol);

                            for (int i = 1; i <= colCount; i++) {
                                final int colIndex = i;
                                TableColumn<ObservableList<Object>, String> col = new TableColumn<>(meta.getColumnName(i));
                                col.setCellValueFactory(param -> {
                                    Object value = param.getValue().get(colIndex);
                                    return new SimpleStringProperty(value == null ? "" : value.toString());
                                });
                                col.setCellFactory(TextFieldTableCell.forTableColumn());
                                tableView1.getColumns().add(col);
                            }

                            while (rs.next()) {
                                ObservableList<Object> row = FXCollections.observableArrayList();
                                row.add(new SimpleBooleanProperty(false));
                                for (int i = 1; i <= colCount; i++) {
                                    row.add(rs.getString(i));
                                }
                                data.add(row);
                            }

                            tableView1.setItems(data);
                            tableView1.setEditable(true);
                        }
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    log("⚠ SQL Execution Error: " + ex.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> log("⚠ API Call Error: " + e.getMessage()));
        }
    }).start();
}
public static String extractSQL(String content) {
    int start = content.indexOf("```");
    int end = content.indexOf("```", start + 3);

    if (start != -1 && end != -1) {
        String inside = content.substring(start + 3, end).trim();
        if (inside.startsWith("sql") || inside.startsWith("postgresql")) {
            int nl = inside.indexOf('\n');
            if (nl != -1) inside = inside.substring(nl + 1).trim();
        }
        return inside;
    }
    for (String line : content.split("\n")) {
        if (line.trim().toLowerCase().startsWith("select")) {
            return line.trim().replace(";", "") + ";";
        }
    }

    return "";
}


private void handleCSV(File file) {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String headerLine = reader.readLine();
        if (headerLine == null) return;

        String[] headers = headerLine.split(",");
        List<String> cleanHeaders = new ArrayList<>();
        Set<String> uniqueHeaders = new HashSet<>();
        for (String h : headers) {
            String clean = h.trim().replaceAll("[^a-zA-Z0-9_]", "_");
            if (clean.isEmpty()) clean = "col_" + uniqueHeaders.size();
            while (!uniqueHeaders.add(clean)) {
                clean += "_1";
            }
            cleanHeaders.add(clean);
        }
      
        dynamicFormArea.getChildren().clear();
        Label tableLabel = new Label("Enter Table Name: ");
        TextField tableField = new TextField();
        Button submitBtn = new Button("Submit");
        VBox layout = new VBox(15, tableLabel, tableField, submitBtn);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        dynamicFormArea.getChildren().add(layout);

      
        List<String[]> rows = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            rows.add(line.split(",", -1));
        }

        submitBtn.setOnAction(ev -> {
            String tableName = tableField.getText().trim().replaceAll("[^a-zA-Z0-9_]", "_");
            if (tableName.isEmpty()) {
                log("Invalid table name.");
                return;
            }

            try {
                connectToDB();
                Statement stmt = connection.createStatement();

                stmt.executeUpdate("DROP TABLE IF EXISTS `" + tableName + "`");

                StringBuilder createSQL = new StringBuilder("CREATE TABLE `" + tableName + "` (");
                for (String col : cleanHeaders) {
                    createSQL.append("`").append(col).append("` VARCHAR(255),");
                }
                createSQL.setLength(createSQL.length() - 1);
                createSQL.append(")");
                stmt.executeUpdate(createSQL.toString());

                StringBuilder insertSQL = new StringBuilder("INSERT INTO `" + tableName + "` VALUES (");
                insertSQL.append("?,".repeat(cleanHeaders.size()));
                insertSQL.setLength(insertSQL.length() - 1);
                insertSQL.append(")");
                PreparedStatement pstmt = connection.prepareStatement(insertSQL.toString());

                for (String[] row : rows) {
                    for (int i = 0; i < cleanHeaders.size(); i++) {
                        pstmt.setString(i + 1, (i < row.length) ? row[i] : "");
                    }
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                log("Table `" + tableName + "` created and data inserted.");

                try {
                    displayTableWithCheckbox();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log("UI Display Error: " + ex.getMessage());
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                log("DB Error: " + ex.getMessage());
            }
        });

    } catch (IOException e) {
        e.printStackTrace();
        log("File Read Error: " + e.getMessage());
    }
}



private void connectToDB() {
    try {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password); // statically declared
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

 
    
}
