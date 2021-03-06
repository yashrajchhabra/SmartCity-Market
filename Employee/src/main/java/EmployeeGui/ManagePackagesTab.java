package EmployeeGui;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.google.common.eventbus.Subscribe;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;

import BasicCommonClasses.CatalogProduct;
import BasicCommonClasses.Ingredient;
import BasicCommonClasses.Location;
import BasicCommonClasses.PlaceInMarket;
import BasicCommonClasses.ProductPackage;
import BasicCommonClasses.SmartCode;
import EmployeeContracts.IWorker;
import EmployeeDefs.AEmployeeException.AmountBiggerThanAvailable;
import EmployeeDefs.AEmployeeException.ConnectionFailure;
import EmployeeDefs.AEmployeeException.EmployeeNotConnected;
import EmployeeDefs.AEmployeeException.InvalidParameter;
import EmployeeDefs.AEmployeeException.ProductNotExistInCatalog;
import EmployeeDefs.AEmployeeException.ProductPackageDoesNotExist;
import EmployeeGuiContracts.CatalogProductEvent;
import EmployeeImplementations.Manager;
import GuiUtils.DialogMessagesService;
import GuiUtils.MarkLocationOnStoreMap;
import GuiUtils.RadioButtonEnabler;
import SMExceptions.CommonExceptions.CriticalError;
import SMExceptions.SMException;
import SmartcodeParser.SmartcodePrint;
import UtilsContracts.BarcodeScanEvent;
import UtilsContracts.IBarcodeEventHandler;
import UtilsContracts.IConfiramtionDialog;
import UtilsContracts.IConfiramtionWithCloseDialog;
import UtilsContracts.IEventBus;
import UtilsContracts.SmartcodeScanEvent;
import UtilsImplementations.BarcodeEventHandler;
import UtilsImplementations.InjectionFactory;
import UtilsImplementations.ProjectEventBus;
import UtilsImplementations.StackTraceUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * ManagePackagesTab - This class is the controller for the Manage Packages Tab
 * all action of this tab should be here.
 * 
 * @author Shimon Azulay
 * @since 2017-02-04
 */
public class ManagePackagesTab implements Initializable {

	static Logger log = Logger.getLogger(ManagePackagesTab.class.getName());

	@FXML
	VBox rootPane;

	@FXML
	VBox scanOrTypeCodePane;

	@FXML
	HBox productAfterScanPane;

	@FXML
	VBox smartcodeOperationsPane;

	@FXML
	VBox barcodeOperationsPane;

	@FXML
	GridPane quickProductDetailsPane;

	@FXML
	HBox addPackageToWarehouseParametersPane;

	@FXML
	JFXTextField barcodeTextField;

	@FXML
	Button showMoreDetailsButton;

	@FXML
	Label smartCodeValLabel;

	@FXML
	Label productNameValLabel;

	@FXML
	Label expirationDateValLabel;

	@FXML
	Label amoutInStoreValLabel;

	@FXML
	Label AmountInWarehouseValLabel;

	@FXML
	Label expirationDateLabel;

	@FXML
	CheckBox printTheSmartCodeCheckBox;

	@FXML
	Button runTheOperationButton;

	@FXML
	Spinner<Integer> editPackagesAmountSpinner;

	@FXML
	JFXDatePicker datePicker;

	@FXML
	JFXRadioButton printSmartCodeRadioButton;

	@FXML
	JFXRadioButton addPackageToStoreRadioButton;

	@FXML
	JFXRadioButton removePackageFromStoreRadioButton;

	@FXML
	JFXRadioButton removePackageFromWarhouseRadioButton;

	@FXML
	JFXRadioButton addPakageToWarhouseRadioButton;

	@FXML
	JFXButton showDatePickerBtn;

	@FXML
	ImageView searchCodeButton;

	@FXML
	private JFXListView<String> employeesList;

	@FXML
	private StackPane stackPane;

	@FXML
	private VBox waitingPane;

	@FXML
	private VBox detailsPane;

	@FXML
	private Label emplyeeTitleLbl;

	@FXML
	private Label emplyeeNameLbl;

	@FXML
	private Label employeeUser;

	@FXML
	private JFXButton removeBtn;

	RadioButtonEnabler radioButtonContainerSmarcodeOperations = new RadioButtonEnabler();

	RadioButtonEnabler radioButtonContainerBarcodeOperations = new RadioButtonEnabler();

	CatalogProduct catalogProduct;

	int amountInStore = -1;

	int amountInWarehouse = -1;

	LocalDate expirationDate;

	IBarcodeEventHandler barcodeEventHandler = InjectionFactory.getInstance(BarcodeEventHandler.class);

	IWorker worker = InjectionFactory.getInstance(Manager.class);

	JFXDatePicker datePickerForSmartCode;

	IEventBus eventBus;

	JFXPopup popupExpired;

	JFXListView<String> expiredList;

	JFXButton remove;

	JFXCheckBox showMapOnClick;

	@FXML
	private JFXButton showExpiredProducts;

	@Override
	public void initialize(URL location, ResourceBundle __) {
		eventBus = InjectionFactory.getInstance(ProjectEventBus.class);
		eventBus.register(this);
		barcodeEventHandler.register(this);
		barcodeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*"))
				barcodeTextField.setText(newValue.replaceAll("[^\\d]", ""));
			showScanCodePane(true);
			resetParams();
			// searchCodeButton.setDisable(newValue.isEmpty());
		});
		editPackagesAmountSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

		editPackagesAmountSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null || newValue < 1)//
				editPackagesAmountSpinner.getValueFactory().setValue(oldValue);
			enableRunTheOperationButton();
		});

		editPackagesAmountSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue) {
				editPackagesAmountSpinner.increment(0);
				enableRunTheOperationButton();
			}
		});

		final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
			@Override
			public DateCell call(final DatePicker __) {
				return new DateCell() {
					@Override
					public void updateItem(LocalDate item, boolean empty) {
						super.updateItem(item, empty);

						if (!item.isBefore(LocalDate.now()))
							return;
						setDisable(true);
						setStyle("-fx-background-color: #EEEEEE;");
					}
				};
			}
		};
		datePicker.setDayCellFactory(dayCellFactory);
		datePicker.setValue(LocalDate.now());

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10, 50, 50, 50));
		vbox.setSpacing(10);

		datePickerForSmartCode = new JFXDatePicker();
		Label lbl = new Label("Add Expiration Date To Your Barcode"), lbl2 = new Label("Than Hit The Search Button");
		vbox.getChildren().addAll(lbl,lbl2, datePickerForSmartCode);

		JFXPopup popup = new JFXPopup(vbox);
		showDatePickerBtn
				.setOnMouseClicked(e -> popup.show(showDatePickerBtn, PopupVPosition.TOP, PopupHPosition.LEFT));

		radioButtonContainerSmarcodeOperations.addRadioButtons(
				Arrays.asList(new RadioButton[] { printSmartCodeRadioButton, addPackageToStoreRadioButton,
						removePackageFromStoreRadioButton, removePackageFromWarhouseRadioButton }));

		radioButtonContainerBarcodeOperations
				.addRadioButtons(Arrays.asList(new RadioButton[] { addPakageToWarhouseRadioButton }));

		Label lbl1 = new Label("Expired Products");
		remove = new JFXButton("Remove Selected From System");
		JFXButton close = new JFXButton("Close");
		remove.getStyleClass().add("JFXButton");
		close.getStyleClass().add("JFXButton");
		showMapOnClick = new JFXCheckBox();
		showMapOnClick.setText("Show Product Location On Click");
		showMapOnClick.getStyleClass().add("JFXCheckBox");
		remove.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent __) {
				DialogMessagesService.showConfirmationDialog("Remove Expired Product From System", null,
						"Are You Sure?", new ExpiredProductRemove());
				popupExpired.hide();
			}
		});

		close.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent __) {
				popupExpired.hide();
			}
		});

		expiredList = new JFXListView<String>();
		expiredList.setMinWidth(600);
		expiredList.setMaxWidth(600);
		expiredList.setMaxHeight(400);

		HBox manubtnContanier = new HBox();
		manubtnContanier.getChildren().addAll(remove, close);
		manubtnContanier.setSpacing(10);
		manubtnContanier.setAlignment(Pos.CENTER);
		VBox manuContainer = new VBox();
		manuContainer.getChildren().addAll(lbl1, showMapOnClick, expiredList, manubtnContanier);
		manuContainer.setPadding(new Insets(10, 50, 50, 50));
		manuContainer.setSpacing(10);

		popupExpired = new JFXPopup(manuContainer);
		showExpiredProducts.setOnMouseClicked(e -> {
			createExpiredList();
			remove.setDisable(true);
			popupExpired.show(showExpiredProducts, PopupVPosition.TOP, PopupHPosition.LEFT);
		});

		expiredList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				remove.setDisable(false);

				if (!showMapOnClick.isSelected())
					return;
				CatalogProduct p = null;
				try {
					p = worker.viewProductFromCatalog(expireds.get(expiredList.getSelectionModel().getSelectedItem())
							.getSmartCode().getBarcode());
				} catch (InvalidParameter | CriticalError | EmployeeNotConnected | ProductNotExistInCatalog
						| ConnectionFailure e) {
					log.fatal(e);
					log.debug(StackTraceUtil.stackTraceToStr(e));
					e.showInfoToUser();
				}
				Location loc = p.getLocations().iterator().next();
				DialogMessagesService.showConfirmationWithCloseDialog(
						"Product Location Is: (col=" + loc.getX() + ", row=" + loc.getY() + ")", null,
						new MarkLocationOnStoreMap().mark(loc.getX(), loc.getY()), new LocationMapClose());
				popupExpired.hide();
			}
		});

		expiredList.setDepth(1);
		expiredList.setExpanded(true);

		resetParams();
		showScanCodePane(true);

	}

	class LocationMapClose implements IConfiramtionWithCloseDialog {

		@Override
		public void onClose() {
			remove.setDisable(true);
			popupExpired.show(showExpiredProducts, PopupVPosition.TOP, PopupHPosition.LEFT);
		}

	}

	class ExpiredProductRemove implements IConfiramtionDialog {

		@Override
		public void onYes() {
			removeExpiredProduct();
		}

		@Override
		public void onNo() {
			// Nothing to do

		}

	}

	void removeExpiredProduct() {
		try {
			worker.removeProductPackageFromStore(expireds.get(expiredList.getSelectionModel().getSelectedItem()));
		} catch (InvalidParameter | CriticalError | EmployeeNotConnected | ProductNotExistInCatalog
				| AmountBiggerThanAvailable | ProductPackageDoesNotExist | ConnectionFailure e) {
			log.fatal(e);
			log.debug(StackTraceUtil.stackTraceToStr(e));
			e.showInfoToUser();
		}

		createExpiredList();
		remove.setDisable(true);

	}

	HashMap<String, ProductPackage> expireds;

	ObservableList<String> dataExpireds;

	FilteredList<String> filteredDataExpireds;

	class DatesComparator implements Comparator<ProductPackage> {

		@Override
		public int compare(ProductPackage p1, ProductPackage p2) {
			return p1.getSmartCode().getExpirationDate().compareTo(p2.getSmartCode().getExpirationDate());
		}

	}

	private void createExpiredList() {

		expireds = new HashMap<String, ProductPackage>();

		try {
			worker.getAllExpiredProductPackages().forEach(expired -> expireds.put(generateExpiredLayoutKey(expired), expired));
		} catch (ConnectionFailure | CriticalError | EmployeeNotConnected e) {
			log.fatal(e);
			log.debug(StackTraceUtil.stackTraceToStr(e));
			e.showInfoToUser();
		}

		dataExpireds = FXCollections.observableArrayList();

		ArrayList<ProductPackage> expiredProductPackageSortedList = new ArrayList<ProductPackage>(expireds.values());
		Collections.sort(expiredProductPackageSortedList, new DatesComparator());
		ArrayList<String> expiredStringSortedList = new ArrayList<String>();
		expiredProductPackageSortedList
				.forEach(expired -> expiredStringSortedList.add(generateExpiredLayoutKey(expired)));

		dataExpireds.setAll(expiredStringSortedList);

		filteredDataExpireds = new FilteredList<>(dataExpireds, s -> true);

		expiredList.setItems(filteredDataExpireds);
	}

	private String generateExpiredLayoutKey(ProductPackage pr) {
		String ret = "Error with expired product: ";
		try {
			CatalogProduct p = worker.viewProductFromCatalog(pr.getSmartCode().getBarcode());
			ret = p.getName() + " with barcode " + p.getBarcode() + " expired on "
					+ pr.getSmartCode().getExpirationDate();
		} catch (InvalidParameter | CriticalError | EmployeeNotConnected | ProductNotExistInCatalog
				| ConnectionFailure e) {
			log.fatal(e);
			log.debug(StackTraceUtil.stackTraceToStr(e));
			e.showInfoToUser();
			return ret + pr.getSmartCode().toString();
		}
		return ret;
	}

	@FXML
	void mouseClikedOnBarcodeField(MouseEvent __) {
		showScanCodePane(true);
		resetParams();
	}

	private void addProductParametersToQuickView(String productName, String productBarcode,
			String productExpirationDate, String amountInStore, String amountInWarehouse) {
		smartCodeValLabel.setText(productBarcode);

		productNameValLabel.setText(productName);

		expirationDateValLabel.setText(productExpirationDate);

		amoutInStoreValLabel.setText(amountInStore);

		AmountInWarehouseValLabel.setText(amountInWarehouse);

	}

	private void enableRunTheOperationButton() {
		log.info("===============================enableRunTheOperationButton======================================");
		if (barcodeOperationsPane.isVisible()) {
			log.info("barcode pane is visible");
			log.info("addPakageToWarhouseRadioButton is selected: " + addPakageToWarhouseRadioButton.isSelected());
			runTheOperationButton.setDisable(!addPakageToWarhouseRadioButton.isSelected());
		} else {
			log.info("barcode pane is invisible");

			if (removePackageFromStoreRadioButton.isSelected()) {
				log.info("removePackageFromStoreRadioButton");
				log.info("amount in store: " + amountInStore + " ; amount in spinner: "
						+ editPackagesAmountSpinner.getValue());
				runTheOperationButton.setDisable(amountInStore < editPackagesAmountSpinner.getValue());

			} else if (removePackageFromWarhouseRadioButton.isSelected()) {
				log.info("removePackageFromWarhouseRadioButton");
				log.info("amount in werehouse: " + amountInWarehouse + " ; amount in spinner: "
						+ editPackagesAmountSpinner.getValue());
				runTheOperationButton.setDisable(amountInWarehouse < editPackagesAmountSpinner.getValue());

			} else if (addPackageToStoreRadioButton.isSelected()) {
				log.info("addPackageToStoreRadioButton");
				log.info("amount in werehouse: " + amountInWarehouse + " ; amount in spinner: "
						+ editPackagesAmountSpinner.getValue());
				runTheOperationButton.setDisable(amountInWarehouse < editPackagesAmountSpinner.getValue());

			} else if (!printSmartCodeRadioButton.isSelected())
				runTheOperationButton.setDisable(true);
			else {
				log.info("printSmartCodeRadioButton");
				runTheOperationButton.setDisable(false);
			}
		}
		log.info("===============================enableRunTheOperationButton======================================");
	}

	private void showScanCodePane(boolean show) {

		scanOrTypeCodePane.setVisible(show);
		productAfterScanPane.setVisible(!show);
		// showDatePickerBtn.setVisible(show);
		(show ? scanOrTypeCodePane : datePickerForSmartCode).toFront();
	}

	private void resetParams() {
		datePickerForSmartCode.setValue(null);
		this.expirationDate = null;
		addProductParametersToQuickView("N/A", "N/A", "N/A", "N/A", "N/A");
		amountInWarehouse = amountInStore = -1;
	}

	private void showSmartCodeOperationsPane(boolean show) {

		smartcodeOperationsPane.setVisible(show);
		barcodeOperationsPane.setVisible(!show);
		(!show ? barcodeOperationsPane : smartcodeOperationsPane).toFront();

		if (show)
			runTheOprBtnTxt((JFXRadioButton) radioButtonContainerSmarcodeOperations.getSelected());
		else
			runTheOprBtnTxt((JFXRadioButton) radioButtonContainerBarcodeOperations.getSelected());

	}

	private void getProductCatalog() throws SMException {

		catalogProduct = worker.viewProductFromCatalog(Long.parseLong(barcodeTextField.getText()));

	}

	private void barcodeEntered() throws SMException {

		getProductCatalog();
		showScanCodePane(false);
		showSmartCodeOperationsPane(false);

		addProductParametersToQuickView(catalogProduct.getName(), barcodeTextField.getText(), "N/A", "N/A", "N/A");

	}

	private void smartcodeEntered() throws SMException {
		log.info("===============================smartcodeEntered======================================");
		getProductCatalog();

		updateToSmartCode();
		log.info("amount in store: " + amountInStore);
		log.info("amount in werehouse: " + amountInWarehouse);
		log.info("===============================smartcodeEntered======================================");

	}

	@FXML
	private void searchCodeButtonPressed(MouseEvent __) {
		try {

			LocalDate expirationDate = this.expirationDate != null ? this.expirationDate
					: datePickerForSmartCode.getValue();

			if (expirationDate == null)
				barcodeEntered();
			else {
				this.expirationDate = expirationDate;
				smartcodeEntered();
			}

		} catch (SMException e) {
			log.fatal(e);
			log.debug(StackTraceUtil.stackTraceToStr(e));
			e.showInfoToUser();
			return;
		}

		enableRunTheOperationButton();
	}

	@FXML
	private void radioButtonHandling(ActionEvent ¢) {
		radioButtonContainerSmarcodeOperations.selectRadioButton((RadioButton) ¢.getSource());
		enableRunTheOperationButton();
		runTheOprBtnTxt((JFXRadioButton) ¢.getSource());
	}

	@FXML
	private void radioButtonHandlingBarcode(ActionEvent ¢) {
		radioButtonContainerBarcodeOperations.selectRadioButton((RadioButton) ¢.getSource());
		enableRunTheOperationButton();
		runTheOprBtnTxt((JFXRadioButton) ¢.getSource());
	}

	private void updateToSmartCode() throws SMException {
		SmartCode c = new SmartCode(catalogProduct.getBarcode(), expirationDate);
		this.amountInStore = worker
				.getProductPackageAmount(new ProductPackage(c, 1, new Location(0, 0, PlaceInMarket.STORE)));
		this.amountInWarehouse = worker
				.getProductPackageAmount(new ProductPackage(c, 1, new Location(0, 0, PlaceInMarket.WAREHOUSE)));

		showScanCodePane(false);
		showSmartCodeOperationsPane(true);

		addProductParametersToQuickView(catalogProduct.getName(), barcodeTextField.getText(),
				c.getExpirationDate() + "", Integer.toString(amountInStore), Integer.toString(amountInWarehouse));
	}

	@FXML
	private void runTheOperationButtonPressed(ActionEvent __) {

		SmartCode smartcode = new SmartCode(Long.parseLong(barcodeTextField.getText()), datePicker.getValue());
		int amountVal = editPackagesAmountSpinner.getValue();
		log.info("===============================runTheOperationButtonPressed======================================");
		log.info("amount in spinner: " + amountVal);

		try {
			if (barcodeOperationsPane.isVisible()) {
				log.info("barcode pane is visible");
				if (addPakageToWarhouseRadioButton.isSelected()) {
					log.info("addPakageToWarhouseRadioButton");
					// init
					Location loc = new Location(0, 0, PlaceInMarket.WAREHOUSE);
					ProductPackage pp = new ProductPackage(smartcode, amountVal, loc);

					// exec
					worker.addProductToWarehouse(pp);

					if (printTheSmartCodeCheckBox.isSelected())
						new SmartcodePrint(smartcode, amountVal).print();

					printToSuccessLog("Added (" + amountVal + ") product packages (" + pp + ") to warehouse");

					this.expirationDate = datePicker.getValue();

					updateToSmartCode();

				}
			} else if (addPackageToStoreRadioButton.isSelected()) {
				log.info("addPackageToStoreRadioButton");
				Location loc = new Location(0, 0, PlaceInMarket.STORE);
				ProductPackage pp = new ProductPackage(smartcode, amountVal, loc);
				worker.placeProductPackageOnShelves(pp);
				printToSuccessLog("Added (" + amountVal + ") product packages (" + pp + ") to store");
				updateToSmartCode();
			} else if (removePackageFromStoreRadioButton.isSelected()) {
				log.info("removePackageFromStoreRadioButton");
				Location loc = new Location(0, 0, PlaceInMarket.STORE);
				ProductPackage pp = new ProductPackage(smartcode, amountVal, loc);
				worker.removeProductPackageFromStore(pp);
				printToSuccessLog("Removed (" + amountVal + ") product packages (" + pp + ") from store");
				updateToSmartCode();
			} else if (!removePackageFromWarhouseRadioButton.isSelected()) {
				if (printSmartCodeRadioButton.isSelected())
					new SmartcodePrint(smartcode, amountVal).print();
			} else {
				log.info("removePackageFromWarhouseRadioButton");
				Location loc = new Location(0, 0, PlaceInMarket.WAREHOUSE);
				ProductPackage pp = new ProductPackage(smartcode, amountVal, loc);
				worker.removeProductPackageFromStore(pp);
				printToSuccessLog("Removed (" + amountVal + ") product packages (" + pp + ") from warehouse");
				updateToSmartCode();
			}

		} catch (SMException e) {
			log.fatal(e);
			log.debug(StackTraceUtil.stackTraceToStr(e));
			e.showInfoToUser();
		}
		log.info("===============================runTheOperationButtonPressed======================================");
	}

	private void printToSuccessLog(String msg) {
		((TextArea) rootPane.getScene().lookup("#successLogArea"))
				.appendText(new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(new Date()) + " :: " + msg + "\n");
	}

	@FXML
	private void showMoreDetailsButtonPressed(ActionEvent __) {

		if (catalogProduct == null)
			return;
		int xLocation = catalogProduct.getLocations().iterator().next().getX(),
				yLocation = catalogProduct.getLocations().iterator().next().getY();
		DialogMessagesService.showInfoDialog(catalogProduct.getName(), null,
				"Barcode: " + catalogProduct.getBarcode() + "\nDescription: " + catalogProduct.getDescription()
						+ "\nManufacturer: " + catalogProduct.getManufacturer().getName() + "\nIngredients: "
						+ getIngredients() + "Price: " + catalogProduct.getPrice() + " Nis\nProduct Location col="
						+ xLocation + ", row=" + yLocation + ")",
				new MarkLocationOnStoreMap().mark(xLocation, yLocation));

	}

	String ingr = "";

	private String getIngredients() {
		HashSet<Ingredient> ingerdients = catalogProduct.getIngredients();
		if (ingerdients == null || ingerdients.isEmpty())
			return "N/A\n";

		ingr = "";
		ingerdients.forEach(ing -> {
			String temp = ing.getName() + "\n";
			ingr += temp;
		});
		return ingr;
	}

	@Subscribe
	public void barcodeScanned(BarcodeScanEvent ¢) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				resetParams();
				barcodeTextField.setText(Long.toString(¢.getBarcode()));
				searchCodeButtonPressed(null);
			}
		});

	}

	@Subscribe
	public void smartcoseScanned(SmartcodeScanEvent ¢) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				SmartCode smartcode = ¢.getSmarCode();
				resetParams();
				barcodeTextField.setText(Long.toString(smartcode.getBarcode()));
				expirationDate = smartcode.getExpirationDate();
				searchCodeButtonPressed(null);
			}
		});

	}

	@Subscribe
	public void onCatalogProductEvent(CatalogProductEvent e) {
		mouseClikedOnBarcodeField(null);
	}

	@FXML
	void enterSearchPressed(KeyEvent e) {
		if (e.getCode().equals(KeyCode.ENTER))
			searchCodeButtonPressed(null);

	}

	private void runTheOprBtnTxt(JFXRadioButton btn) {
		runTheOperationButton.setText(btn.getText());
	}
}
