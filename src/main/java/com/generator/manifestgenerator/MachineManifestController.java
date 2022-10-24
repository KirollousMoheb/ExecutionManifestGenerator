package com.generator.manifestgenerator;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.controlsfx.control.CheckComboBox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MachineManifestController {
    private ArrayList<String> fg_names=new ArrayList<>();
    private List<CheckComboBox<String>> fg_modes_checkboxes = new ArrayList<>();

    private int count=0;
    @FXML
    Accordion accordion;
    @FXML
    ScrollPane scroll;
    @FXML
    TextField filename;
    @FXML
    TextField fgname;
    public void generateManifest(){
        if(filename.getText().trim().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Execution Manifest Error");
            alert.setHeaderText("Missing Execution Manifest Id");
            alert.setContentText("You must Enter Execution Manifest Id");
            alert.showAndWait();
            return;
        }
        JSONObject fgs_objs = new JSONObject();
        JSONObject main_obj=new JSONObject();
        for(int i=0;i<fg_names.size();i++){
            JSONObject fg_obj = new JSONObject();
            fg_obj.put("states",getSelectedItems(fg_modes_checkboxes.get(i)));
            fgs_objs.put(fg_names.get(i),fg_obj);
        }
        main_obj.put("function_groups",fgs_objs);
        try (FileWriter Data = new FileWriter(filename.getText()+".json")) {
            Data.write(prettifyJSON(main_obj.toString(),4)); // setting spaces for indent
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Machine Manifest Generated");
            alert.setHeaderText("File Saved Successfully");
            Path path = Paths.get(filename.getText()+".json");

            alert.setContentText("Path: "+path.toAbsolutePath().toString());
            alert.showAndWait();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }
    public static String prettifyJSON(final String json_str, final int indent_width) {
        final char[] chars = json_str.toCharArray();
        final String newline = System.lineSeparator();

        String ret = "";
        boolean begin_quotes = false;

        for (int i = 0, indent = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\"') {
                ret += c;
                begin_quotes = !begin_quotes;
                continue;
            }

            if (!begin_quotes) {
                switch (c) {
                    case '{':
                    case '[':
                        ret += c + newline + String.format("%" + (indent += indent_width) + "s", "");
                        continue;
                    case '}':
                    case ']':
                        ret += newline + ((indent -= indent_width) > 0 ? String.format("%" + indent + "s", "") : "") + c;
                        continue;
                    case ':':
                        ret += c + " ";
                        continue;
                    case ',':
                        ret += c + newline + (indent > 0 ? String.format("%" + indent + "s", "") : "");
                        continue;
                    default:
                        if (Character.isWhitespace(c)) continue;
                }
            }

            ret += c + (c == '\\' ? "" + chars[++i] : "");
        }

        return ret;
    }

    public void clickBack(ActionEvent e) throws IOException {
    Stage primaryStage = (Stage)((Node)e.getSource()).getScene().getWindow();
    FXMLLoader MainScreenPaneLoader = new FXMLLoader(getClass().getResource("main.fxml"));
    Parent MainScreenPane = MainScreenPaneLoader.load();
    Scene MainScreenScene = new Scene(MainScreenPane, 620, 500);
    primaryStage.setTitle("Manifest Generator");
    primaryStage.setScene(MainScreenScene);
}
public void removeFunctionGroup(){
    removePane(accordion);
}
public void addFunctionGroup(){
    if(fgname.getText()==null|fgname.getText().trim().isEmpty()){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Machine Manifest Error");
        alert.setHeaderText("Function Group name is Empty");
        alert.setContentText("Please Add a Function Group name");
        alert.showAndWait();
        return;
    }else if(checkFGName()){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Machine Manifest Error");
        alert.setHeaderText("Function Group name already exists");
        alert.setContentText("Please Add a different Function Group name");
        alert.showAndWait();
        return;
    }
    fg_names.add(fgname.getText().trim());
    addPane(accordion,scroll);
    fgname.clear();


}
    private void removePane(Accordion accordion) {
        TitledPane expandedPane = accordion.getExpandedPane();

        if (expandedPane != null) {
            int expandedIndex = accordion.getPanes().indexOf(expandedPane);
            accordion.getPanes().remove(expandedPane);
            fg_names.remove(expandedIndex);
            fg_modes_checkboxes.remove(expandedIndex);

            int nPanes = accordion.getPanes().size();

            if (nPanes > 0) {
                TitledPane nextPane = accordion.getPanes().get(
                        Math.max(0, expandedIndex - 1)
                );

                accordion.setExpandedPane(
                        nextPane
                );

                nextPane.requestFocus();
            }
            count--;

        }
    }

public boolean checkFGName(){
    for(int i=0;i< fg_names.size();i++){
        if(fg_names.get(i).equals(fgname.getText().trim())){
            return true;
        }
    }
    return false;
}

    private void addPane(Accordion accordion, ScrollPane scrollPane) {
        TitledPane newPane = createTitledPane();
        count++;

        accordion.getPanes().add(newPane);
        accordion.setExpandedPane(newPane);
        newPane.requestFocus();

        scrollPane.applyCss();
        scrollPane.layout();

        scrollPane.setVvalue(scrollPane.getVmax());
    }
    private TitledPane createTitledPane() {
        return new TitledPane(
                fgname.getText(),
                createTitledPaneContent()
        );
    }
    private Parent createTitledPaneContent() {

        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(new Label("Function Group Modes: "), 0, 0);
        CheckComboBox<String> checkComboBox=createCheckComboBox();
        grid.add(checkComboBox,1,0);
        fg_modes_checkboxes.add(checkComboBox);

        return grid;
    }
    private List<String> getSelectedItems(CheckComboBox<String> checkComboBox) {
        return checkComboBox.getCheckModel().getCheckedItems();
    }
    private CheckComboBox<String> createCheckComboBox() {
        // Create the list of items to be shown on the combobox
        ObservableList<String> programmingLanguages = FXCollections.observableArrayList(
                "off",
                "startup",
                "running",
                "shutdown",
                "restart"
        );
        // Attach the list to the Combobox
        CheckComboBox<String> checkComboBox = new CheckComboBox<>(programmingLanguages);
        //As soon as an item is selected or selection is changed, display all the selected items

        return checkComboBox;
    }
}
