/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package table_des_matieresfx;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import table_des_matieresfx.lib.MyUtil;

/**
 *
 * @author Stefano Crapanzano
 */
public class FXMLDocumentController implements Initializable {

    private Connection connection;

    private Predicate<Prelevement> predicateNom;

    private ObservableList<Prelevement> data;
    SortedList<Prelevement> sortedData;

    @FXML
    private Label statusBar;
    @FXML
    private MenuItem menuQuitter;
    @FXML
    private TextField txtfldId;
    @FXML
    private DatePicker datepckAjouterDate;
    @FXML
    private TextField txtfldAjouterNom;
    @FXML
    private TextField txtfldAjouterType;
    @FXML
    private TextField txtfldAjouterCasier;
    @FXML
    private TextField txtfldAjouterCheminComplet;
    @FXML
    private TableView<Prelevement> tablePrelevement;
    @FXML
    private TableColumn<Prelevement, String> id;
    @FXML
    private TableColumn<Prelevement, String> date;
    @FXML
    private TableColumn<Prelevement, String> nom;
    @FXML
    private TableColumn<Prelevement, String> type;
    @FXML
    private TableColumn<Prelevement, String> casier;
    @FXML
    private TableColumn<Prelevement, String> chemin;
    @FXML
    private TitledPane titledPanePrelevement;
    @FXML
    private TitledPane titledPaneRechercher;

    @FXML
    private Button btnAjouterPrelevement;
    @FXML
    private Button btnModifierPrelevement;
    @FXML
    private Button btnSupprimerPrelevement;
    @FXML
    private Button btnEffacerFormulaire;

    @FXML
    private Button btnEffacerFiltre;
    @FXML
    private TextField dateRecherche;
    @FXML
    private TextField txtRechercheNom;
    @FXML
    private TextField txtRechercheType;
    @FXML
    private CheckBox chkLienBrise;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        btnModifierPrelevement.setDisable(true);
        btnSupprimerPrelevement.setDisable(true);

        menuQuitter.setOnAction(null);

        id.setCellValueFactory(new PropertyValueFactory<Prelevement, String>("id"));
        date.setCellValueFactory(new PropertyValueFactory<Prelevement, String>("date"));
        nom.setCellValueFactory(new PropertyValueFactory<Prelevement, String>("nom"));
        type.setCellValueFactory(new PropertyValueFactory<Prelevement, String>("type"));
        casier.setCellValueFactory(new PropertyValueFactory<Prelevement, String>("casier"));

        datepckAjouterDate.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            @Override
            public String toString(LocalDate localDate) {
                if (localDate == null) {
                    return "";
                }
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString) {
                if (dateString == null || dateString.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(dateString, dateTimeFormatter);
            }
        });

        tablePrelevement.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablePrelevement.setRowFactory(tv -> {
            TableRow<Prelevement> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() > 0 && (!row.isEmpty())) {

                    btnAjouterPrelevement.setDisable(true);
                    btnModifierPrelevement.setDisable(false);
                    btnSupprimerPrelevement.setDisable(false);

                    Prelevement rowData = row.getItem();
                    txtfldId.setText(Integer.toString(rowData.getId()));
                    datepckAjouterDate.getEditor().setText(rowData.getDate());
                    txtfldAjouterNom.setText(rowData.getNom());
                    txtfldAjouterType.setText(rowData.getType());
                    txtfldAjouterCasier.setText(rowData.getCasier());
                    txtfldAjouterCheminComplet.setText(rowData.getChemin());

                    titledPanePrelevement.setExpanded(true);

                }
            });
            return row;
        });

        try {
            data = getInitialPrelevementData();

            tablePrelevement.getItems().setAll(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        FilteredList<Prelevement> filteredData = new FilteredList<>(data, p -> true);
        
        
        StringConverter converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = 
                DateTimeFormatter.ofPattern("yyyy/MM/dd");
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        }; 
       
        dateRecherche.textProperty().addListener(e -> {
                filteredData.setPredicate(isDateInTable().and(isNomInTable()).and(isTypeInTable()).and(isLienBriseInTable()));
        });

        txtRechercheNom.textProperty().addListener(e -> {
            filteredData.setPredicate(isDateInTable().and(isNomInTable()).and(isTypeInTable()).and(isLienBriseInTable()));
        });
        txtRechercheType.textProperty().addListener(e -> {
            filteredData.setPredicate(isDateInTable().and(isNomInTable()).and(isTypeInTable()).and(isLienBriseInTable()));
        });

        chkLienBrise.selectedProperty().addListener(e -> {
            filteredData.setPredicate(isDateInTable().and(isNomInTable()).and(isTypeInTable()).and(isLienBriseInTable()));
        });

        // 2. Wrap the FilteredList in a SortedList. 
        sortedData = new SortedList<>(filteredData);

        // 3. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(tablePrelevement.comparatorProperty());
        // 4. Add sorted (and filtered) data to the table.
        tablePrelevement.setItems(sortedData);

    }

    public Predicate<Prelevement> isNomInTable() {
        return n -> {
            if (txtRechercheNom.getText() == null || txtRechercheNom.getText().isEmpty()) {
                return true;
            }

                String[] word = MyUtil.toUpperCaseExceptµ(txtRechercheType.getText()).split(" ");
                for (String s : word) {
                    if (!MyUtil.toUpperCaseExceptµ(n.getType()).contains(s)) {
                        return false;
                    }
                }
                return true;
        };
    }
    public Predicate<Prelevement> isDateInTable() {
        return n -> {
                if (dateRecherche.getText() == null || dateRecherche.getText().isEmpty()) {
                    return true;
                }

                if(n.getDate().contains(dateRecherche.getText())) {
                        return true;
                }
                return false;
        };
    }
    
    public Predicate<Prelevement> isLienBriseInTable() {
        return n -> {
                if (chkLienBrise.isSelected() == false || chkLienBrise == null) {
                    return true;
                }
                
                File f = new File(n.getChemin());
                return !f.exists();
        };
    }
    
    @FXML
    public void onButtonNouveauPrelevement() {
        titledPanePrelevement.setExpanded(true);
    }
    
    @FXML
    public void onButtonRechercher() {
        titledPaneRechercher.setExpanded(true);
    }
    @FXML
    public void onButtonAjouterPrelevement() {
        System.out.println("Click on enregistrer");
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:table_des_matieres.db");

            Statement statement = connection.createStatement();
            String query = "INSERT INTO ELEMENT(date, nom, type, casier, chemin) VALUES ('" + datepckAjouterDate.getEditor().getText() + "'"
                    + ", '" + MyUtil.toUpperCaseExceptµ(txtfldAjouterNom.getText()) + "'"
                    + ", '" + MyUtil.toUpperCaseExceptµ(txtfldAjouterType.getText()) + "'"
                    + ", '" + MyUtil.toUpperCaseExceptµ(txtfldAjouterCasier.getText()) + "'"
                    + ", '" + MyUtil.toUpperCaseExceptµ(txtfldAjouterCheminComplet.getText()).trim() + "')";

            int ret = statement.executeUpdate(query);
            ResultSet rs = statement.getGeneratedKeys();

            data.add(new Prelevement(rs.getInt(1),
                    datepckAjouterDate.getEditor().getText(),
                    txtfldAjouterNom.getText(),
                    txtfldAjouterType.getText(),
                    txtfldAjouterCasier.getText(),
                    txtfldAjouterCheminComplet.getText()));
            
            tablePrelevement.getSelectionModel().selectLast();
            tablePrelevement.scrollTo(tablePrelevement.getSelectionModel().getSelectedIndex());
            tablePrelevement.requestFocus();

        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void onButtonModifierPrelevement() {

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:table_des_matieres.db");

            Statement statement = connection.createStatement();
            String query = "UPDATE ELEMENT SET DATE='" + datepckAjouterDate.getEditor().getText() + "'"
                    + ", NOM='" + MyUtil.toUpperCaseExceptµ(txtfldAjouterNom.getText()) + "'"
                    + ", TYPE='" + MyUtil.toUpperCaseExceptµ(txtfldAjouterType.getText()) + "'"
                    + ", CASIER='" + MyUtil.toUpperCaseExceptµ(txtfldAjouterCasier.getText()) + "' "
                    + ", CHEMIN='" + MyUtil.toUpperCaseExceptµ(txtfldAjouterCheminComplet.getText()).trim() + "' "
                    + " WHERE ELEMENT_ID=" + txtfldId.getText();

            int ret = statement.executeUpdate(query);

            int indexSelected = tablePrelevement.getSelectionModel().getSelectedIndex();
            int indexSource = sortedData.getSourceIndexFor(data, indexSelected);
            data.set(indexSource,
                    new Prelevement(Integer.valueOf(txtfldId.getText()),
                            datepckAjouterDate.getEditor().getText(),
                            txtfldAjouterNom.getText(),
                            txtfldAjouterType.getText(),
                            txtfldAjouterCasier.getText(),
                            txtfldAjouterCheminComplet.getText()));

            tablePrelevement.getSelectionModel().select(indexSelected);

        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void onButtonSupprimerPrelevement() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:table_des_matieres.db");

            Statement statement = connection.createStatement();
            String query = "DELETE FROM ELEMENT "
                    + " WHERE ELEMENT_ID=" + txtfldId.getText();

            int ret = statement.executeUpdate(query);

            int indexSelected = tablePrelevement.getSelectionModel().getSelectedIndex();
            int indexSource = sortedData.getSourceIndexFor(data, indexSelected);
            data.remove(indexSource);
            tablePrelevement.getSelectionModel().clearSelection();
            clearForm();
            btnAjouterPrelevement.setDisable(false);
            btnModifierPrelevement.setDisable(true);
            btnSupprimerPrelevement.setDisable(true);

        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void onButtonEffacerFormulaire() {

        btnAjouterPrelevement.setDisable(false);
        btnModifierPrelevement.setDisable(true);
        btnSupprimerPrelevement.setDisable(true);

        clearForm();

    }
    
    @FXML
    public void onCheckLienBrise() {

    }
    
    @FXML
    public void onButtonEffacerFiltre() {
        clearFiltre();
    }

    private ObservableList getInitialPrelevementData() throws SQLException {
        List prelevements = new ArrayList();

        ResultSet rs = null;
        Statement statement = null;

        connection = DriverManager.getConnection("jdbc:sqlite:table_des_matieres.db");

        statement = connection.createStatement();
        try {
            rs = statement.executeQuery(
                    "SELECT ELEMENT_ID, DATE, TYPE, NOM, CASIER, CHEMIN  FROM ELEMENT ORDER BY ELEMENT_ID");
            int i = 0;
            while (rs.next()) {
                prelevements.add(new Prelevement(rs.getInt("element_id"), rs.getString("date"), rs.getString("nom"), rs.getString("type"), rs.getString("casier"), rs.getString("chemin")));
                i++;
            }

            rs.close();
        } catch (SQLException e) {
            statement.executeUpdate("CREATE TABLE ELEMENT (CHEMIN TEXT, ELEMENT_ID INTEGER PRIMARY KEY, NOM TEXT, DATE TEXT, TYPE TEXT, CASIER TEXT);");
            connection.close();
        }
        connection.close();

        data = FXCollections.observableList(prelevements);
        return data;
    }

    private void clearForm() {
        txtfldId.clear();
        txtfldAjouterNom.clear();
        txtfldAjouterCasier.clear();
        txtfldAjouterCheminComplet.clear();
        txtfldAjouterType.clear();
        datepckAjouterDate.getEditor().clear();
    }

    private void clearFiltre() {
        dateRecherche.clear();
        txtRechercheNom.clear();
        txtRechercheType.clear();
        chkLienBrise.setSelected(false);
    }

}
