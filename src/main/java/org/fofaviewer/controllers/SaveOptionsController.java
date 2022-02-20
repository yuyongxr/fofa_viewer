package org.fofaviewer.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import org.fofaviewer.controls.CloseableTabPane;
import org.fofaviewer.utils.DataUtil;
import org.fofaviewer.utils.ResourceBundleUtil;
import org.fofaviewer.utils.SQLiteUtils;
import org.tinylog.Logger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SaveOptionsController {
    private ArrayList<CheckBox> boxes;
    private boolean isProject;
    private ResourceBundle bundle;
    private Map<String, String> ruleMap;
    @FXML
    private VBox window;
    @FXML
    private VBox vbox;
    @FXML
    private Label title;
    @FXML
    private Button selectAll;
    @FXML
    private Button unselect;
    @FXML
    private VBox project;
    @FXML
    private Label label_project_name;
    @FXML
    private TextField project_name;
    @FXML
    private GridPane rule;
    @FXML
    private Label label_rule_name;
    @FXML
    private Label label_rule_description;
    @FXML
    private TextField rule_name;
    @FXML
    private TextField rule_description;
    @FXML
    private void initialize(){
        bundle = ResourceBundleUtil.getResource();
        boxes = new ArrayList<>();
        title.setText(bundle.getString("SAVE_QUERY_TIP1"));
        selectAll.setText(bundle.getString("SAVE_QUERY_SELECT_ALL"));
        unselect.setText(bundle.getString("SAVE_QUERY_REVERSE_SELECTED"));
        label_project_name.setText(bundle.getString("SAVE_QUERY_PROJECT_NAME"));
        label_rule_name.setText(bundle.getString("SAVE_QUERY_RULE_NAME"));
        label_rule_description.setText(bundle.getString("SAVE_QUERY_RULE_DESCRIPTION"));
    }

    public void setTabs(CloseableTabPane tabPane){
        if(isProject){
            for(String queryTxt : tabPane.getTabsTxt()){
                if(queryTxt != null){
                    CheckBox box = new CheckBox(queryTxt);
                    box.setFont(Font.font(14));
                    boxes.add(box);
                    vbox.getChildren().add(box);
                }
            }
        }else{
            ruleMap = new HashMap<>();
            ToggleGroup group = new ToggleGroup();
            for(String queryTxt : tabPane.getTabsTxt()){
                if(queryTxt != null){
                    RadioButton rbtn = new RadioButton(queryTxt);
                    rbtn.setFont(Font.font(14));
                    rbtn.setToggleGroup(group);
                    rbtn.setUserData(queryTxt);
                    vbox.getChildren().add(rbtn);
                }
            }
            group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                if (group.getSelectedToggle() != null) {
                    ruleMap.put("query_text", group.getSelectedToggle().getUserData().toString());
                }
            });
        }
    }

    public void setProject(boolean isProject, DialogPane dialogPane) {
        this.isProject = isProject;
        if(isProject){ //save project
            window.getChildren().remove(rule);
            dialogPane.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, e -> {
                List<String> res = new ArrayList<>();
                for(CheckBox box:boxes){
                    if(box.isSelected()){
                        res.add(box.getText());
                    }
                }
                if(res.size() == 0){
                    DataUtil.showAlert(Alert.AlertType.WARNING, null, bundle.getString("SAVE_QUERY_TIP2")).showAndWait();
                    e.consume();
                }else{
                    if(project_name.getText().equals("")){
                        DataUtil.showAlert(Alert.AlertType.WARNING, null, bundle.getString("SAVE_QUERY_TIP3")).showAndWait();
                        e.consume();
                    }else{
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setTitle(bundle.getString("DIRECTORY_CHOOSER_TITLE"));
                        File file = directoryChooser.showDialog(window.getScene().getWindow());
                        if(file!=null){
                            try {
                                File f = new File(file.getAbsolutePath() + System.getProperty("file.separator")
                                        + project_name.getText() + ".txt");
                                if(!f.exists()){
                                    f.createNewFile();
                                    FileWriter writer = new FileWriter(f);
                                    BufferedWriter bw = new BufferedWriter(writer);
                                    for(String str : res){
                                        bw.write(str + "\n");
                                    }
                                    bw.close();
                                    writer.close();
                                    DataUtil.showAlert(Alert.AlertType.INFORMATION, null, bundle.getString("SAVE_QUERY_SAVE_SUCCESS")).showAndWait();
                                }else{
                                    DataUtil.showAlert(Alert.AlertType.WARNING, null, bundle.getString("SAVE_QUERY_FILE_EXISTS")).showAndWait();
                                    e.consume();
                                }
                            }catch (IOException ex){
                                Logger.error(ex);
                            }
                        }
                    }
                }
            });
        }else{ // save rule
            selectAll.setVisible(false);
            unselect.setVisible(false);
            window.getChildren().remove(project);
            dialogPane.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, e -> {
                if(!rule_name.getText().equals("") && ruleMap.containsKey("query_text")){
                    ruleMap.put("rule_name", rule_name.getText());
                    ruleMap.put("rule_description", rule_description.getText());
                    if(SQLiteUtils.insertRule(ruleMap)){
                        DataUtil.showAlert(Alert.AlertType.INFORMATION, null, bundle.getString("SAVE_QUERY_ADD_RULE_SUCCESS")).showAndWait();
                    }
                }
            });
        }
    }

    @FXML
    private void selectAll(){
        for(CheckBox box : boxes){
            box.setSelected(true);
        }
    }

    @FXML
    private void unSelect(){
        for(CheckBox box : boxes){
            box.setSelected(!box.isSelected());
        }
    }
}
