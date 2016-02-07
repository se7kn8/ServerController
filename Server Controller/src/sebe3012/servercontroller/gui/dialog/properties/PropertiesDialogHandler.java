package sebe3012.servercontroller.gui.dialog.properties;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

public class PropertiesDialogHandler {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ListView<String> lProperties;

	@FXML
	private ListView<String> rProperties;

	@FXML
	private Button btnSave;

	@FXML
	void initialize() {
		lProperties.setEditable(true);
		rProperties.setEditable(true);
		rProperties.setCellFactory(e -> {
			return new TextFieldListCell<>(new StringConverter<String>() {

				@Override
				public String toString(String object) {
					return object;
				}

				@Override
				public String fromString(String string) {
					return string;
				}
			});
		});
		ObservableList<String> keys = FXCollections
				.observableArrayList(PropertiesDialog.properties.getAllValues().keySet());
		ObservableList<String> values = FXCollections
				.observableArrayList(PropertiesDialog.properties.getAllValues().values());
		lProperties.setItems(keys);
		rProperties.setItems(values);
	}

	@FXML
	void onLViewClicked() {
		rProperties.getSelectionModel().select(lProperties.getSelectionModel().getSelectedIndex());
	}

	@FXML
	void onRViewClicked() {
		lProperties.getSelectionModel().select(rProperties.getSelectionModel().getSelectedIndex());
	}

	@FXML
	void onSaveClicked() {
		try {
			Path file = Paths.get(PropertiesDialog.properties.getProperitesFile().toURI());
			List<String> lines = new ArrayList<>();
			for (int i = 0; i < lProperties.getItems().size(); i++) {
				lines.add(lProperties.getItems().get(i) + "=" + rProperties.getItems().get(i));
			}
			Files.write(file, lines, Charset.forName("UTF-8"));

			Alert warning = new Alert(AlertType.WARNING,
					"Der Server mu� neugestartet werden, damit die �nderungen wirksam werden", ButtonType.OK);
			warning.getDialogPane().getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
			warning.setHeaderText("");
			warning.setTitle("Achtung");
			warning.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			Alert dialog = new Alert(AlertType.ERROR, "Es ist ein Fehler beim Speichern aufgetreten", ButtonType.OK);
			dialog.getDialogPane().getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
			dialog.setHeaderText("");
			dialog.setTitle("Fehler");
			dialog.showAndWait();
		}

	}
}