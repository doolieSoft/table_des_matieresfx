/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package table_des_matieresfx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.textfield.TextFields;
import table_des_matieresfx.lib.MyUtil;

/**
 *
 * @author Stefano Crapanzano
 */
public class FXMLDocumentController implements Initializable {

    private Connection connection;

    private ObservableList<Prelevement> data;
    private SortedList<Prelevement> sortedData;
    private FilteredList<Prelevement> filteredData;
    @FXML
    private AnchorPane principalPane;
    @FXML
    private Label labelId;
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
    private Button btnQuitter;
    @FXML
    private Button btnAjouterPrelevement;
    @FXML
    private Button btnModifierPrelevement;
    @FXML
    private Button btnSupprimerPrelevement;

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

        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        nom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        casier.setCellValueFactory(new PropertyValueFactory<>("casier"));
        chemin.setCellValueFactory(new PropertyValueFactory<>("chemin"));

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
        tablePrelevement.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                btnAjouterPrelevement.setDisable(true);
                btnModifierPrelevement.setDisable(false);
                btnSupprimerPrelevement.setDisable(false);

                labelId.setText(Integer.toString(newSelection.getId()));
                datepckAjouterDate.getEditor().setText(newSelection.getDate());
                txtfldAjouterNom.setText(newSelection.getNom());
                txtfldAjouterType.setText(newSelection.getType());
                txtfldAjouterCasier.setText(newSelection.getCasier());
                txtfldAjouterCheminComplet.setText(newSelection.getChemin());

            }
        });

        tablePrelevement.setRowFactory(tv -> {
            TableRow<Prelevement> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() > 0 && (!row.isEmpty())) {

                    Prelevement rowData = row.getItem();
                    labelId.setText(Integer.toString(rowData.getId()));
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
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.printf(FXMLDocumentController.class.getName() + " " + ex.getLocalizedMessage());
        }

        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        filteredData = new FilteredList<>(data, p -> true);

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

        Set<String> hashsetNom = new HashSet<>();

        filteredData.stream().forEach((p) -> {
            hashsetNom.add(p.getNom());
        });

        TextFields.bindAutoCompletion(
                txtRechercheNom,
                hashsetNom);
        TextFields.bindAutoCompletion(
                txtfldAjouterNom,
                hashsetNom);

        Set<String> hashsetType = new HashSet<>();

        filteredData.stream().forEach((p) -> {
            hashsetType.add(p.getType());
        });

        TextFields.bindAutoCompletion(
                txtRechercheType,
                hashsetType);
        TextFields.bindAutoCompletion(
                txtfldAjouterType,
                hashsetType);

        Set<String> hashsetCasier = new HashSet<>();
        filteredData.stream().forEach((p) -> {
            hashsetCasier.add(p.getCasier());
        });

        TextFields.bindAutoCompletion(
                txtfldAjouterCasier,
                hashsetCasier);

    }

    public Predicate<Prelevement> isNomInTable() {
        return n -> {
            if (txtRechercheNom.getText() == null || txtRechercheNom.getText().isEmpty()) {
                return true;
            }

            String[] word = MyUtil.toUpperCaseExceptµ(txtRechercheNom.getText()).split(" ");
            for (String s : word) {
                if (!MyUtil.toUpperCaseExceptµ(n.getNom()).contains(s)) {
                    return false;
                }
            }
            return true;
        };
    }

    public Predicate<Prelevement> isTypeInTable() {
        return n -> {
            if (txtRechercheType.getText() == null || txtRechercheType.getText().isEmpty()) {
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

            return n.getDate().contains(dateRecherche.getText());
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
        clearForm();
        titledPanePrelevement.setExpanded(true);
    }

    @FXML
    public void onButtonRechercher() {
        titledPaneRechercher.setExpanded(true);
    }

    @FXML
    public void onButtonAjouterPrelevement() {

        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:table_des_matieres.db");
            txtfldAjouterNom.setText(MyUtil.toUpperCaseExceptµ(txtfldAjouterNom.getText().trim()));
            txtfldAjouterType.setText(MyUtil.toUpperCaseExceptµ(txtfldAjouterType.getText().trim()));
            txtfldAjouterCasier.setText(MyUtil.toUpperCaseExceptµ(txtfldAjouterCasier.getText().trim()));
            txtfldAjouterCheminComplet.setText(MyUtil.toUpperCaseExceptµ(txtfldAjouterCheminComplet.getText().trim()));

            statement = connection.createStatement();
            String query = "INSERT INTO ELEMENT(date, nom, type, casier, chemin) VALUES ('" + datepckAjouterDate.getEditor().getText() + "'"
                    + ", '" + txtfldAjouterNom.getText() + "'"
                    + ", '" + txtfldAjouterType.getText() + "'"
                    + ", '" + txtfldAjouterCasier.getText() + "'"
                    + ", '" + txtfldAjouterCheminComplet.getText() + "')";

            int ret = statement.executeUpdate(query);
            rs = statement.getGeneratedKeys();

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
            System.out.printf(FXMLDocumentController.class.getName() + " " + ex.getLocalizedMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) { /* ignored */ }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) { /* ignored */ }
            try {
                connection.close();
            } catch (Exception e) { /* ignored */ }
        }

    }

    @FXML
    public void onButtonModifierPrelevement() {

        Statement statement = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:table_des_matieres.db");

            statement = connection.createStatement();

            txtfldAjouterNom.setText(MyUtil.toUpperCaseExceptµ(txtfldAjouterNom.getText().trim()));
            txtfldAjouterType.setText(MyUtil.toUpperCaseExceptµ(txtfldAjouterType.getText().trim()));
            txtfldAjouterCasier.setText(MyUtil.toUpperCaseExceptµ(txtfldAjouterCasier.getText().trim()));
            txtfldAjouterCheminComplet.setText(MyUtil.toUpperCaseExceptµ(txtfldAjouterCheminComplet.getText().trim()));

            String query = "UPDATE ELEMENT SET DATE='" + datepckAjouterDate.getEditor().getText() + "'"
                    + ", NOM='" + txtfldAjouterNom.getText() + "'"
                    + ", TYPE='" + txtfldAjouterType.getText() + "'"
                    + ", CASIER='" + txtfldAjouterCasier.getText() + "' "
                    + ", CHEMIN='" + txtfldAjouterCheminComplet.getText() + "' "
                    + " WHERE ELEMENT_ID=" + labelId.getText();

            int ret = statement.executeUpdate(query);

            int idSelected = tablePrelevement.getSelectionModel().getSelectedIndex();

            data.set(data.indexOf(tablePrelevement.getSelectionModel().getSelectedItem()),
                    new Prelevement(Integer.valueOf(labelId.getText()),
                            datepckAjouterDate.getEditor().getText(),
                            txtfldAjouterNom.getText(),
                            txtfldAjouterType.getText(),
                            txtfldAjouterCasier.getText(),
                            txtfldAjouterCheminComplet.getText()));

            tablePrelevement.getSelectionModel().select(idSelected);
            tablePrelevement.requestFocus();
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.printf(FXMLDocumentController.class.getName() + " " + ex.getLocalizedMessage());
        } finally {
            //try { rs.close(); } catch (Exception e) { /* ignored */ }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) { /* ignored */ }
            try {
                connection.close();
            } catch (Exception e) { /* ignored */ }
        }
    }

    @FXML
    public void onButtonSupprimerPrelevement() {
        Statement statement = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:table_des_matieres.db");

            statement = connection.createStatement();
            String query = "DELETE FROM ELEMENT "
                    + " WHERE ELEMENT_ID=" + labelId.getText();

            int ret = statement.executeUpdate(query);

            data.remove(data.indexOf(tablePrelevement.getSelectionModel().getSelectedItem()));
            tablePrelevement.getSelectionModel().clearSelection();
            clearForm();
            btnAjouterPrelevement.setDisable(false);
            btnModifierPrelevement.setDisable(true);
            btnSupprimerPrelevement.setDisable(true);

        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.printf(FXMLDocumentController.class.getName() + " " + ex.getLocalizedMessage());
        } finally {
            //try { rs.close(); } catch (Exception e) { /* ignored */ }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) { /* ignored */ }
            try {
                connection.close();
            } catch (Exception e) { /* ignored */ }
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
    public void onButtonEffacerFiltre() {
        clearFiltre();
    }

    @FXML
    public void onButtonSelectionnerFichier() {
        FileChooser browse = new FileChooser();
        browse.setTitle("Sélectionner le fichier du prélèvement ...");

        browse.getExtensionFilters().addAll(
                new ExtensionFilter("Word Files", "*.doc", "*.docx"),
                new ExtensionFilter("PDF Files", "*.pdf"),
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new ExtensionFilter("All Files", "*.*"));

        File selectedFile = browse.showOpenDialog(principalPane.getScene().getWindow());

        if (selectedFile != null) {
            txtfldAjouterCheminComplet.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    public void onButtonOuvrir() {
        try {
            if (txtfldAjouterCheminComplet.getText() == null || txtfldAjouterCheminComplet.getText().isEmpty()) {
                Notifications.create()
                        .title("Impossible d'ouvrir le fichier")
                        .text("Le champ chemin est vide, veuillez compléter le formulaire!")
                        .showWarning();

            } else {
                java.awt.Desktop.getDesktop().open(new File(txtfldAjouterCheminComplet.getText()));
            }
        } catch (java.lang.IllegalArgumentException ex) {
            Notifications.create()
                    .title("Fichier non trouvé")
                    .text("Le fichier que vous essayez d'ouvrir est introuvable!")
                    .showWarning();
        } catch (IOException ex) {

            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void onButtonQuitter() {
        Platform.exit();
    }

    private ObservableList getInitialPrelevementData() throws SQLException {
        List prelevements = new ArrayList();

        ResultSet rs = null;

        connection = DriverManager.getConnection("jdbc:sqlite:table_des_matieres.db");

        Statement statement = connection.createStatement();
        try {
            rs = statement.executeQuery(
                    "SELECT ELEMENT_ID, DATE, TYPE, NOM, CASIER, CHEMIN  FROM ELEMENT ORDER BY ELEMENT_ID");
            int i = 0;
            while (rs.next()) {
                prelevements.add(new Prelevement(rs.getInt("element_id"), rs.getString("date"), rs.getString("nom"), rs.getString("type"), rs.getString("casier"), rs.getString("chemin")));
                i++;
            }

            rs.close();
        } catch (SQLException ex) {
            statement.executeUpdate("CREATE TABLE ELEMENT (CHEMIN TEXT, ELEMENT_ID INTEGER PRIMARY KEY, NOM TEXT, DATE TEXT, TYPE TEXT, CASIER TEXT);");
            connection.close();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) { /* ignored */ }
            try {
                statement.close();
            } catch (Exception e) { /* ignored */ }
            try {
                connection.close();
            } catch (Exception e) { /* ignored */ }
        }

        data = FXCollections.observableList(prelevements);
        return data;
    }

    private void clearForm() {
        labelId.setText("");
        txtfldAjouterNom.clear();
        txtfldAjouterCasier.clear();
        txtfldAjouterCheminComplet.clear();
        txtfldAjouterType.clear();
        datepckAjouterDate.getEditor().clear();

        btnAjouterPrelevement.setDisable(false);
        btnModifierPrelevement.setDisable(true);
        btnSupprimerPrelevement.setDisable(true);
    }

    private void clearFiltre() {
        dateRecherche.clear();
        txtRechercheNom.clear();
        txtRechercheType.clear();
        chkLienBrise.setSelected(false);
    }

}
