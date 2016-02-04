package sebe3012.servercontroller.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sebe3012.servercontroller.gui.dialog.BatchServerDialog;
import sebe3012.servercontroller.gui.tab.ServerTab;
import sebe3012.servercontroller.gui.tab.Tabs;
import sebe3012.servercontroller.server.BatchServer;
import sebe3012.servercontroller.server.Servers;

public class FrameHandler {

	public static TabPane mainPane;
	public static ListView<BatchServer> list;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private MenuBar mBar;

	@FXML
	private ListView<BatchServer> lView;

	@FXML
	private Label credits;

	@FXML
	private VBox vBox;

	@FXML
	private TabPane main;

	@FXML
	private MenuItem over;

	@FXML
	private MenuItem addServer;

	@FXML
	void initialize() {
		init();
	}

	@FXML
	void onOverItemClicked(ActionEvent event) {
		showCredits();
	}

	@FXML
	void onAddServerItemClicked(ActionEvent event) {
		new BatchServerDialog(new Stage());
	}

	@FXML
	void onServerEditItemClicked(ActionEvent event) {
		ServerTab tab = (ServerTab) mainPane.getSelectionModel().getSelectedItem();
		BatchServer bs = Tabs.servers.get(tab.getTabContent().getId()).getServer();
		if (bs.isRunning()) {
			Alert dialog = new Alert(AlertType.WARNING, "Der Server mu� erst gestoppt werden", ButtonType.OK);
			dialog.getDialogPane().getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
			dialog.setTitle("Fehler");
			dialog.setHeaderText("");
			dialog.showAndWait();
		} else {
			new BatchServerDialog(new Stage(), bs.getBatchFile().getAbsolutePath(),
					bs.getPropertiesFile().getAbsolutePath(), bs.getName());
		}
	}

	private void showCredits() {
		Alert credits = new Alert(AlertType.INFORMATION,
				"ServerController by Sebastian Knackstedt (Sebe3012)\n� 2016 Germany", ButtonType.OK);
		credits.setTitle("�ber");
		credits.setHeaderText("");
		credits.getDialogPane().getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
		credits.showAndWait();
	}

	private void init() {
		lView.setCellFactory(e -> {
			return new ServerCell();
		});
		lView.setOnMouseClicked(event -> {
			BatchServer bs = lView.getSelectionModel().getSelectedItem();
			Tabs.servers.forEach((id, server) -> {
				if (server.getServer().equals(bs)) {
					main.getSelectionModel().select(main.getTabs().get(id));
				}
			});
		});
		mainPane = main;
		list = lView;
		lView.setItems(Servers.servers);
		System.out.println("FXML intitialize");
	}

	private class ServerCell extends ListCell<BatchServer> {

		@Override
		protected void updateItem(BatchServer item, boolean empty) {
			super.updateItem(item, empty);
			if (item == null || empty) {
			} else {
				setText(item.getName());
			}
		}

	}
}
