package sebe3012.servercontroller.gui;

import sebe3012.servercontroller.ServerController;
import sebe3012.servercontroller.event.ChangeControlsEvent;
import sebe3012.servercontroller.eventbus.EventHandler;
import sebe3012.servercontroller.eventbus.IEventHandler;
import sebe3012.servercontroller.gui.dialog.AddonInstallDialog;
import sebe3012.servercontroller.gui.dialog.CreditsDialog;
import sebe3012.servercontroller.gui.dialog.Dialog;
import sebe3012.servercontroller.gui.dialog.LicenseDialog;
import sebe3012.servercontroller.gui.dialog.RConDialog;
import sebe3012.servercontroller.gui.dialog.ServerDialog;
import sebe3012.servercontroller.gui.dialog.SettingsDialog;
import sebe3012.servercontroller.gui.tab.ServerTab;
import sebe3012.servercontroller.gui.tab.TabServerHandler;
import sebe3012.servercontroller.gui.tab.Tabs;
import sebe3012.servercontroller.gui.tree.RootTreeEntry;
import sebe3012.servercontroller.gui.tree.StructureCell;
import sebe3012.servercontroller.gui.tree.TreeEntry;
import sebe3012.servercontroller.preferences.PreferencesConstants;
import sebe3012.servercontroller.preferences.ServerControllerPreferences;
import sebe3012.servercontroller.save.ServerSave;
import sebe3012.servercontroller.server.BasicServer;
import sebe3012.servercontroller.server.Servers;
import sebe3012.servercontroller.util.GUIUtil;
import sebe3012.servercontroller.util.I18N;
import sebe3012.servercontroller.util.design.Design;
import sebe3012.servercontroller.util.design.Designs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class FrameHandler implements IEventHandler {

	private Dialog addonInstallDialog = new AddonInstallDialog();
	private Dialog creditsDialog = new CreditsDialog();
	private Dialog licenseDialog = new LicenseDialog();
	private Dialog rconDialog = new RConDialog();
	private Dialog settingsDialog = new SettingsDialog();
	private Dialog serverDialog = new ServerDialog();

	public static TabPane mainPane;
	public static TreeView<TreeEntry<?>> tree;
	public static TreeItem<TreeEntry<?>> rootItem;
	public static ProgressBar currentProgress;
	public static VBox buttonList;
	//FIXME public static Thread monitoringThread;

	private static final Logger log = LogManager.getLogger();

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private MenuBar mBar;

	@FXML
	private TreeView<TreeEntry<?>> lView;

	@FXML
	private Label credits;

	@FXML
	private VBox vBox;

	@FXML
	private TabPane main;

	@FXML
	private ToolBar toolbar;

	@FXML
	private ProgressBar progressBar;

	@FXML
	void initialize() {
		init();
	}

	@FXML
	void onSettingsClicked() {
		settingsDialog.showDialog();
	}

	@FXML
	void onCreditsItemClicked() {
		creditsDialog.showDialog();
	}

	@FXML
	void onAddServerItemClicked() {
		serverDialog.showDialog();
	}

	@FXML
	void onSaveItemClicked() {
		ServerSave.saveServerController();
	}

	@FXML
	void onOpenItemClicked() {
		ServerSave.loadServerController();
	}

	@FXML
	void onServerEditItemClicked() {
		Servers.editCurrentServer();
	}

	@FXML
	void onServerRemoveItemClicked() {
		Tabs.removeCurrentTab();
	}

	@FXML
	void onLicenseClicked() {
		licenseDialog.showDialog();
	}

	@FXML
	void onDesignClicked() {
		Designs.showDesignDialog();
	}

	@FXML
	void onRConClicked() {
		rconDialog.showDialog();
	}

	@FXML
	void onAddonInstallClicked() {
		addonInstallDialog.showDialog();
	}

	private void init() {
		Designs.registerDesign(new Design("css/bright", "bright"));
		Designs.registerDesign(new Design("css/dark", "dark"));

		lView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Object o = newValue.getValue().getItem();

			if (o instanceof BasicServer) {
				BasicServer server = (BasicServer) o;
				main.getSelectionModel().select(Servers.findTab(server));
			}

		});

		main.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

			if (newValue instanceof ServerTab) {

				ServerTab tab = (ServerTab) newValue;

				BasicServer server = tab.getTabContent().getContentHandler().getServerHandler().getServer();

				for (TreeItem<TreeEntry<?>> item : lView.getRoot().getChildren()) {

					if (item.getValue().getItem() instanceof BasicServer) {

						if (item.getValue().getItem().equals(server)) {
							lView.getSelectionModel().select(item);
						}
					}
				}
			}

		});


		/*Tab mainTab = new Tab(); TODO Future use
		mainTab.setClosable(true);
		mainTab.setText(I18N.translate("tab_home"));
		mainTab.setContent(new BorderPane(new Label(I18N.translate("tab_home"))));
		main.getTabs().add(mainTab);*/

		String designID = ServerControllerPreferences.loadSetting(PreferencesConstants.KEY_DESIGN, Designs.getDefaultDesign().getId());

		Designs.setCurrentDesign(designID);

		EventHandler.EVENT_BUS.registerEventListener(this);

		vBox.getStyleClass().add("button-tree");
		credits.setText(ServerController.VERSION);

		main.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

			/*FIXME
			FrameHandler.this.dataCounterRam.set(0);
			FrameHandler.this.dataCounterCpu.set(0);*/

			if (newValue instanceof ServerTab) {
				TabServerHandler handler = ((ServerTab) newValue).getTabContent().getContentHandler()
						.getServerHandler();
				if (handler.hasServer()) {
					EventHandler.EVENT_BUS.post(new ChangeControlsEvent(handler.getServer().getExtraControls()));
				}
			}
		});

		lView.setCellFactory(e -> new StructureCell());

		lView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {

				TreeItem<TreeEntry<?>> item = lView.getSelectionModel().getSelectedItem();
				if (item != null && item.getValue().onDoubleClick()) {
					event.consume();
				}
			}

		});

		currentProgress = progressBar;
		FrameHandler.hideBar();

		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/add.png").toExternalForm(), e -> serverDialog.showDialog(), I18N.translate("tooltip_add_server"));
		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/remove.png").toExternalForm(), e -> Tabs.removeCurrentTab(), I18N.translate("tooltip_remove_server"));
		GUIUtil.addSeparatorToToolbar(toolbar);
		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/start_all.png").toExternalForm(), e -> Servers.startAllServers(), I18N.translate("tooltip_start_all_servers"));
		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/restart_all.png").toExternalForm(), e -> Servers.restartAllServers(), I18N.translate("tooltip_restart_all_servers"));
		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/stop_all.png").toExternalForm(), e -> Servers.stopAllServers(), I18N.translate("tooltip_stop_all_servers"));
		GUIUtil.addSeparatorToToolbar(toolbar);
		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/start.png").toExternalForm(), e -> Servers.startCurrentServer(), I18N.translate("tooltip_start_server"));
		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/restart.png").toExternalForm(), e -> Servers.restartCurrentServer(), I18N.translate("tooltip_restart_server"));
		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/stop.png").toExternalForm(), e -> Servers.stopCurrentServer(), I18N.translate("tooltip_stop_server"));
		GUIUtil.addSeparatorToToolbar(toolbar);
		GUIUtil.addButtonToToolbar(toolbar, ClassLoader.getSystemResource("png/toolbar/edit.png").toExternalForm(), e -> Servers.editCurrentServer(), I18N.translate("tooltip_edit_server"));

		mainPane = main;
		tree = lView;
		buttonList = vBox;

		rootItem = new TreeItem<>(new RootTreeEntry(I18N.translate("tree_servers")));
		lView.setRoot(rootItem);
		rootItem.setExpanded(true);

		/*FIXME initCharts();

		monitoringThread = new Thread(new ServerWatcher());
		monitoringThread.setName("Server Monitoring Thread");
		monitoringThread.start();*/


		log.info("FXML initialized");
	}

	public static void showBar() {
		log.debug("Showing the progress bar");
		Platform.runLater(() -> FrameHandler.currentProgress.setVisible(true));
	}

	public static void hideBar() {
		log.debug("Hiding the progress bar");
		Platform.runLater(() -> FrameHandler.currentProgress.setVisible(false));
	}

	@Subscribe
	public void changeExtraButton(ChangeControlsEvent event) {
		Platform.runLater(() -> {

			vBox.getChildren().clear();

			event.getNewControls().forEach(control -> {
				control.setPrefWidth(1000);
				vBox.getChildren().add(control);
			});

		});

	}
/*FIXME Too many bugs
	private NumberAxis ramAxis;
	private NumberAxis cpuAxis;

	private AreaChart.Series<Number, Number> ramSeries;
	private AreaChart.Series<Number, Number> cpuSeries;

	private int maxValues = 10;
	private IntegerProperty dataCounterRam = new SimpleIntegerProperty(0);
	private IntegerProperty dataCounterCpu = new SimpleIntegerProperty(0);

	public static ConcurrentLinkedQueue<Number> ramData = new ConcurrentLinkedQueue<>();
	public static ConcurrentLinkedQueue<Number> cpuData = new ConcurrentLinkedQueue<>();

	private void initCharts() {

		ramAxis = new NumberAxis(0, maxValues, 1);
		ramAxis.setForceZeroInRange(false);
		ramAxis.setAutoRanging(false);

		cpuAxis = new NumberAxis(0, maxValues, 1);
		cpuAxis.setForceZeroInRange(false);
		cpuAxis.setAutoRanging(false);

		NumberAxis ramAxisY = new NumberAxis();
		ramAxisY.setAutoRanging(true);
		ramAxisY.setLowerBound(0);
		ramAxisY.setUpperBound(100);
		ramAxisY.setTickUnit(20);

		NumberAxis cpuAxisY = new NumberAxis();
		cpuAxisY.setAutoRanging(true);
		cpuAxisY.setLowerBound(0);
		cpuAxisY.setUpperBound(100);
		cpuAxisY.setTickUnit(20);

		AreaChart<Number, Number> ram = new AreaChart<Number, Number>(ramAxis, ramAxisY) {
			@Override
			protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
			}
		};

		AreaChart<Number, Number> cpu = new AreaChart<Number, Number>(cpuAxis, cpuAxisY) {
			@Override
			protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
			}
		};

		ram.setAnimated(false);
		cpu.setAnimated(false);

		ram.setTitle("RAM");
		cpu.setTitle("CPU");

		ramSeries = new AreaChart.Series<>();
		cpuSeries = new AreaChart.Series<>();

		ramSeries.setName(I18N.translate("ram_usage"));
		cpuSeries.setName(I18N.translate("cpu_usage"));

		ram.getData().add(ramSeries);
		cpu.getData().add(cpuSeries);

		leftBox.getChildren().add(cpu);
		leftBox.getChildren().add(ram);

		new AnimationTimer() {

			@Override
			public void handle(long now) {
				addDataToSeries(ramData, ramSeries, ramAxis, dataCounterRam);
			}
		}.start();

		new AnimationTimer() {

			@Override
			public void handle(long now) {
				addDataToSeries(cpuData, cpuSeries, cpuAxis, dataCounterCpu);
			}
		}.start();
	}

	private void addDataToSeries(ConcurrentLinkedQueue<Number> queue, AreaChart.Series<Number, Number> series, NumberAxis axis, IntegerProperty dataCounter) {
		for (int i = 0; i < 20; i++) {
			if (queue.isEmpty()) break;
			series.getData().add(new AreaChart.Data<>(dataCounter.get(), queue.remove()));
			dataCounter.set(dataCounter.get() + 1);
		}
		if (series.getData().size() > maxValues) {
			series.getData().remove(0, series.getData().size() - maxValues);
		}

		axis.setLowerBound(dataCounter.get() - maxValues);
		axis.setUpperBound(dataCounter.get() - 1);
	}*/
}
