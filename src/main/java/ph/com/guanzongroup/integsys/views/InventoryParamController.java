package ph.com.guanzongroup.integsys.views;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvControllers;
import org.json.simple.JSONObject;

/**
 * FXML Controller class
 *
 * @author mnv
 */
public class InventoryParamController implements Initializable, ScreenInterface {

    private final String psFormName = "Inventory Parameter";
    LogWrapper poLogWrapper = new LogWrapper("cas", "cas-err.log");

    private GRiderCAS poApp;
    private Inventory poAppController;
    private int pnSelectRecord, pnRecordDetail;
    private Control lastFocusedControl;
    private String psIndustryID;
    private String psCategoryID;

    private ObservableList<Model_Inventory> laRecordDetail;
    private boolean pbLoaded = false;

    private unloadForm poUnload = new unloadForm();

    String category = System.getProperty("store.inventory.industry");
    ObservableList<String> unitType = FXCollections.observableArrayList(
            "LDU",
            "Regular",
            "Free",
            "Live",
            "Service",
            "RDU",
            "Others"
    );

    @FXML
    private AnchorPane apMaster, apDetail, apDetailOther, apMainAnchor;

    @FXML
    private TextField tfSearchDescription, tfSearchBarcode;

    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnSearch, btnCancel, btnClose,
            btnAuto, btnAddSubItem, btnDelSubUnit;

    @FXML
    private TextField tfStockID, tfBarcode, tfAltBarcode, tfBriefDescription, tfDescription, tfInvType,
            tfCategory1, tfCategory2, tfCategory3, tfCategory4, tfBrand, tfModel, tfColor,
            tfMeasure, tfVariant, tfDiscount1, tfDiscount2, tfDiscount3, tfDiscount4, tfMinLevel,
            tfMaxLevel, tfCost, tfSRP, tfSuperseded, tfShelfLife,
            tfBarcodeOther, tfDescriptionOther, tfMeasurementOther, tfQtyOther;

    @FXML
    private ComboBox cmbUnitType;

    @FXML
    private CheckBox chkSerialized, chkCombo, chkPromo, chkRecordStatus;

    @FXML
    private Label lblStatus;

    @FXML
    private GridPane subItemFields;

    @FXML
    private TableView<Model_Inventory> tblSubItems;

    @FXML
    private TableColumn<Model_Inventory, String> tblColNo, tblColBarcode,
            tblColDescription, tblColMeasurement, tblColQty;

    /**
     * Initializes the controller class.
     */
    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            poAppController = new InvControllers(poApp, poLogWrapper).Inventory();
            poAppController.initialize();
            //initlalize and validate record objects from class controller

            //background thread
            Platform.runLater(() -> {
                poAppController.setRecordStatus("01");
                //initialize logged in category/industry
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCategory(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID);
                System.err.println("Initialize value : Category >" + psCategoryID);

                //hide temporary
                apDetail.setVisible(false);
                btnNew.fire();
            });
            initializeTableDetail();
            initControlEvents();
        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, e.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
            }
        }
    }

    @FXML
    void ontblDetailClicked(MouseEvent ex) {
        try {
            pnRecordDetail = tblSubItems.getSelectionModel().getSelectedIndex() + 1;
            if (pnRecordDetail <= 0) {
                return;
            }

            loadSelectedRecordDetail(pnRecordDetail);
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            e.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, e.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
            }
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnSearch":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        return;
                    }

                    switch (lastFocusedControl.getId()) {

                        case "tfCategory1":
                            if (!isJSONSuccess(poAppController.searchCategory(tfCategory1.getText() != null ? tfCategory1.getText() : "", false),
                                    "Initialize Search Category! ")) {
                                return;
                            }
                            loadRecord();
                            break;
                        case "tfCategory2":
                            if (!isJSONSuccess(poAppController.searchCategory2(tfCategory2.getText() != null ? tfCategory2.getText() : "", false),
                                    "Initialize Search Category 2! ")) {
                                return;
                            }
                            loadRecord();
                            break;
                        case "tfCategory3":
                            if (!isJSONSuccess(poAppController.searchCategory3(tfCategory3.getText() != null ? tfCategory3.getText() : "", false),
                                    "Initialize Search Category 3! ")) {
                                return;
                            }
                            loadRecord();
                            break;
                        case "tfCategory4":
                            if (!isJSONSuccess(poAppController.searchCategory4(tfCategory4.getText() != null ? tfCategory4.getText() : "", false),
                                    "Initialize Search Category 4! ")) {
                                return;
                            }
                            loadRecord();
                            break;

                        case "tfInvType":
                            if (!isJSONSuccess(poAppController.searchInvType(tfInvType.getText() != null ? tfInvType.getText() : "", false),
                                    "Initialize Search Inventory Type! ")) {
                                return;
                            }
                            loadRecord();
                            break;

                        case "tfBrand":
                            if (!isJSONSuccess(poAppController.searchBrand(tfBrand.getText() != null ? tfBrand.getText() : "", false),
                                    "Initialize Search Brand! ")) {
                                return;
                            }
                            loadRecord();
                            break;

                        case "tfModel":
                            if (!isJSONSuccess(poAppController.searchModel(tfModel.getText() != null ? tfModel.getText() : "", false),
                                    "Initialize Search Model! ")) {
                                return;
                            }
                            loadRecord();
                            break;

                        case "tfColor":
                            if (!isJSONSuccess(poAppController.searchColor(tfColor.getText() != null ? tfColor.getText() : "", false),
                                    "Initialize Search Color! ")) {
                                return;
                            }
                            loadRecord();
                            break;

                        case "tfMeasure":
                            if (!isJSONSuccess(poAppController.searchMeasure(tfMeasure.getText() != null ? tfMeasure.getText() : "", false),
                                    "Initialize Search Measure! ")) {
                                return;
                            }
                            loadRecord();
                            break;

                        case "tfVariant":
                            if (!isJSONSuccess(poAppController.searchVariant(tfVariant.getText() != null ? tfVariant.getText() : "", false),
                                    "Initialize Search Variant! ")) {
                                return;
                            }
                            loadRecord();
                            break;

                        case "tfSuperseded":
                            if (!isJSONSuccess(poAppController.searchSuperseded(tfSuperseded.getText() != null ? tfSuperseded.getText() : "", false),
                                    "Initialize Search Superseded! ")) {
                                return;
                            }
                            loadRecord();
                            break;

                        //SUB ITEM SEARCHING 
                        case "tfBarcodeOther":
                            if (!isJSONSuccess(poAppController.searchOther(pnRecordDetail, tfBarcodeOther.getText() != null ? tfBarcodeOther.getText() : "", true),
                                    "Initialize Search Check! ")) {
                                return;
                            }
                            reloadTableDetail();
                            loadSelectedRecordDetail(pnRecordDetail);
                            break;
                        case "tfDescriptionOther":
                            if (!isJSONSuccess(poAppController.searchOther(pnRecordDetail, tfDescriptionOther.getText() != null ? tfDescriptionOther.getText() : "", false),
                                    "Initialize Search Check! ")) {
                                return;
                            }
                            reloadTableDetail();
                            loadSelectedRecordDetail(pnRecordDetail);
                            break;

                    }
                    break;

                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        if (!tfStockID.getText().isEmpty()) {
                            if (ShowMessageFX.OkayCancel(null, "Search Record! by Barcode", "Are you sure you want replace loaded Record?") == false) {
                                return;
                            }
                        }
                        if (!isJSONSuccess(poAppController.searchRecord(tfSearchBarcode.getText() != null ? tfSearchBarcode.getText() : "", true),
                                "Initialize Search Barcode No! ")) {
                            return;
                        }

                        getLoadedRecord();
                        initButtonDisplay(poAppController.getEditMode());
                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchBarcode":
                            if (!tfStockID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Record! by Barcode", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchBarcode.getText() != null ? tfSearchBarcode.getText() : "", true),
                                    "Initialize Search Barcode No! ")) {
                                return;
                            }

                            getLoadedRecord();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchDescription":
                            if (!tfStockID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Record! by Description", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchDescription.getText() != null ? tfSearchDescription.getText() : "", false),
                                    "Initialize Search Record! ")) {
                                return;
                            }

                            getLoadedRecord();
                            initButtonDisplay(poAppController.getEditMode());
                            break;

                        default:
                            if (!tfStockID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Record! by Barcode", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchBarcode.getText() != null ? tfSearchBarcode.getText() : "", true),
                                    "Initialize Search Barcode No! ")) {
                                return;
                            }

                            getLoadedRecord();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;

                case "btnNew":
                    if (!isJSONSuccess(poAppController.newRecord(), "Initialize New Record")) {
                        return;
                    }
                    clearAllInputs();

                    poAppController.getModel().setCategoryFirstLevelId(psCategoryID);
                    getLoadedRecord();

                    break;

                case "btnUpdate":
                    if (poAppController.getModel().getStockId() == null || poAppController.getModel().getStockId().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }
                    poAppController.openRecord(poAppController.getModel().getStockId());
                    if (!isJSONSuccess(poAppController.updateRecord(), "Initialize Update Record")) {
                        return;
                    }
                    getLoadedRecord();
                    break;

                case "btnSave":
                    if (tfStockID.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }

                    if (!isJSONSuccess(poAppController.SaveRecord(), "Initialize Save Record")) {
                        return;
                    }
                    getLoadedRecord();

                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poAppController = new InvControllers(poApp, poLogWrapper).Inventory();
                        poAppController.initialize();
                        //initlalize and validate record objects from class controller
//                        unloadForm appUnload = new unloadForm();
//                        appUnload.unloadForm(apMainAnchor, poApp, psFormName);

                        //background thread
                        Platform.runLater(() -> {
                            poAppController.setRecordStatus("01");
                            //initialize logged in category/industry
                            poAppController.setIndustryID(psIndustryID);
                            poAppController.setCategory(psCategoryID);
                            System.err.println("Initialize value : Industry >" + psIndustryID);
                            System.err.println("Initialize value : Category >" + psCategoryID);

                            clearAllInputs();
                        });
                        break;
                    }
                    break;

                case "btnAuto":
                    ShowMessageFX.Information(null, psFormName,
                            "This feature is under development and will be available soon.\nThank you for your patience!");
                    break;

                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
            }

            initButtonDisplay(poAppController.getEditMode());

        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
            e.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, e.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
            }
        }
    }

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
//        try {
        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {

                case "tfBarcode":
                    poAppController.getModel().setBarCode(lsValue);

                    loadRecord();
//                        loadSelectedRecordDetail(pnRecordDetail);

                    break;
                case "tfAltBarcode":
                    poAppController.getModel().setAlternateBarCode(lsValue);

                    loadRecord();
                    break;
                case "tfBriefDescription":
                    poAppController.getModel().setBriefDescription(lsValue);

                    loadRecord();
                    break;
                case "tfDescription":
                    poAppController.getModel().setDescription(lsValue);

                    loadRecord();
                    break;
                case "tfDiscount1":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfDiscount1.getText()) > 0.0) {
                            tfDiscount1.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Discount! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnDiscount1;
                    try {
                        lnDiscount1 = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnDiscount1 = 0.0; // default if parsing fails
                        poAppController.getModel().setDiscountRateLevel1(lnDiscount1);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }
                    if (lnDiscount1 > 100.00) {
                        lnDiscount1 = 0.0; // default 
                        poAppController.getModel().setDiscountRateLevel1(lnDiscount1);
//                            reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                        return;
                    }

                    poAppController.getModel().setDiscountRateLevel1(lnDiscount1);
                    loadRecord();

                    break;

                case "tfDiscount2":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfDiscount2.getText()) > 0.0) {
                            tfDiscount2.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Discount! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnDiscount2;
                    try {
                        lnDiscount2 = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnDiscount2 = 0.0; // default if parsing fails
                        poAppController.getModel().setDiscountRateLevel2(lnDiscount2);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }
                    if (lnDiscount2 > 100.00) {
                        lnDiscount2 = 0.0; // default 
                        poAppController.getModel().setDiscountRateLevel2(lnDiscount2);
//                            reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                        return;
                    }

                    poAppController.getModel().setDiscountRateLevel2(lnDiscount2);
                    loadRecord();

                    break;

                case "tfDiscount3":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfDiscount3.getText()) > 0.0) {
                            tfDiscount3.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Discount! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnDiscount3;
                    try {
                        lnDiscount3 = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnDiscount3 = 0.0; // default if parsing fails
                        poAppController.getModel().setDiscountRateLevel3(lnDiscount3);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }
                    if (lnDiscount3 > 100.00) {
                        lnDiscount3 = 0.0; // default 
                        poAppController.getModel().setDiscountRateLevel3(lnDiscount3);
//                            reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                        return;
                    }

                    poAppController.getModel().setDiscountRateLevel3(lnDiscount3);
                    loadRecord();

                    break;

                case "tfDiscount4":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfDiscount4.getText()) > 0.0) {
                            tfDiscount4.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Discount! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnDiscount4;
                    try {
                        lnDiscount4 = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnDiscount4 = 0.0; // default if parsing fails
                        poAppController.getModel().setDealerDiscountRate(lnDiscount4);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }
                    if (lnDiscount4 > 100.00) {
                        lnDiscount4 = 0.0; // default 
                        poAppController.getModel().setDealerDiscountRate(lnDiscount4);
//                            reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                        return;
                    }

                    poAppController.getModel().setDealerDiscountRate(lnDiscount4);
                    loadRecord();

                    break;

                case "tfMinLevel":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfMinLevel.getText()) > 0.0) {
                            tfMinLevel.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Min Level ! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    int lnMinLevel;
                    try {
                        lnMinLevel = Integer.parseInt(lsValue);
                    } catch (NumberFormatException e) {
                        lnMinLevel = 0; // default if parsing fails
                        poAppController.getModel().setMinimumInventoryLevel(lnMinLevel);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }

                    poAppController.getModel().setMinimumInventoryLevel(lnMinLevel);
                    loadRecord();

                    break;

                case "tfMaxLevel":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfMaxLevel.getText()) > 0.0) {
                            tfMaxLevel.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Min Level ! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    int lnMaxLevel;
                    try {
                        lnMaxLevel = Integer.parseInt(lsValue);
                    } catch (NumberFormatException e) {
                        lnMaxLevel = 0; // default if parsing fails
                        poAppController.getModel().setMaximumInventoryLevel(lnMaxLevel);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }

                    poAppController.getModel().setMaximumInventoryLevel(lnMaxLevel);
                    loadRecord();

                    break;

                case "tfCost":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfCost.getText()) > 0.0) {
                            tfCost.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Cost! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnCost;
                    try {
                        lnCost = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnCost = 0.0; // default if parsing fails
                        poAppController.getModel().setCost(lnCost);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }

                    poAppController.getModel().setCost(lnCost);
                    loadRecord();

                    break;

                case "tfSRP":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfSRP.getText()) > 0.0) {
                            tfSRP.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set SRP! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnSRP;
                    try {
                        lnSRP = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnSRP = 0.0; // default if parsing fails
                        poAppController.getModel().setSellingPrice(lnSRP);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }

                    poAppController.getModel().setSellingPrice(lnSRP);
                    loadRecord();

                    break;

                case "tfShelfLife":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfShelfLife.getText()) > 0.0) {
                            tfShelfLife.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Shelf Life! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnShelf;
                    try {
                        lnShelf = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnShelf = 0.0; // default if parsing fails
                        poAppController.getModel().setSellingPrice(lnShelf);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }

                    poAppController.getModel().setSellingPrice(lnShelf);
                    loadRecord();

                    break;
                // SUB ITEM FOCUS

                case "tfQtyOther":
                    if (poAppController.getOther(pnRecordDetail).getStockId() == null
                            || poAppController.getOther(pnRecordDetail).getStockId().isEmpty()) {
                        if (Double.parseDouble(tfShelfLife.getText()) > 0.0) {
                            tfQtyOther.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set sub-item quantity! No Stock Invetory Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnQty;
                    try {
                        lnQty = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnQty = 0.0; // default if parsing fails
//                        poAppController.getOther(pnRecordDetail).setQuantity(lnQty);
                        reloadTableDetail();
                        loadRecord();
                        loTextField.requestFocus();
                    }

//                        poAppController.getOther(pnRecordDetail).setQuantity(lnQty);
                    loadRecord();

                    break;

            }
        } else {
            loTextField.selectAll();
        }
//        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
//            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
//        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchBarcode":
                                if (!tfStockID.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Record! by Barcode", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchRecord(tfSearchBarcode.getText() != null ? tfSearchBarcode.getText() : "", true),
                                        "Initialize Search Barcode No! ")) {
                                    return;
                                }

                                getLoadedRecord();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchDescription":
                                if (!tfStockID.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Record! by Description", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchRecord(tfSearchDescription.getText() != null ? tfSearchDescription.getText() : "", false),
                                        "Initialize Search Record! ")) {
                                    return;
                                }

                                getLoadedRecord();
                                initButtonDisplay(poAppController.getEditMode());
                                break;

                            case "tfCategory1":
                                if (!isJSONSuccess(poAppController.searchCategory(tfCategory1.getText() != null ? tfCategory1.getText() : "", false),
                                        "Initialize Search Category! ")) {
                                    return;
                                }
                                loadRecord();
                                break;
                            case "tfCategory2":
                                if (!isJSONSuccess(poAppController.searchCategory2(tfCategory2.getText() != null ? tfCategory2.getText() : "", false),
                                        "Initialize Search Category 2! ")) {
                                    return;
                                }
                                loadRecord();
                                break;
                            case "tfCategory3":
                                if (!isJSONSuccess(poAppController.searchCategory3(tfCategory3.getText() != null ? tfCategory3.getText() : "", false),
                                        "Initialize Search Category 3! ")) {
                                    return;
                                }
                                loadRecord();
                                break;
                            case "tfCategory4":
                                if (!isJSONSuccess(poAppController.searchCategory4(tfCategory4.getText() != null ? tfCategory4.getText() : "", false),
                                        "Initialize Search Category 4! ")) {
                                    return;
                                }
                                loadRecord();
                                break;

                            case "tfInvType":
                                if (!isJSONSuccess(poAppController.searchInvType(tfInvType.getText() != null ? tfInvType.getText() : "", false),
                                        "Initialize Search Inventory Type! ")) {
                                    return;
                                }
                                loadRecord();
                                break;

                            case "tfBrand":
                                if (!isJSONSuccess(poAppController.searchBrand(tfBrand.getText() != null ? tfBrand.getText() : "", false),
                                        "Initialize Search Brand! ")) {
                                    return;
                                }
                                loadRecord();
                                break;

                            case "tfModel":
                                if (!isJSONSuccess(poAppController.searchModel(tfModel.getText() != null ? tfModel.getText() : "", false),
                                        "Initialize Search Model! ")) {
                                    return;
                                }
                                loadRecord();
                                break;

                            case "tfColor":
                                if (!isJSONSuccess(poAppController.searchColor(tfColor.getText() != null ? tfColor.getText() : "", false),
                                        "Initialize Search Color! ")) {
                                    return;
                                }
                                loadRecord();
                                break;

                            case "tfMeasure":
                                if (!isJSONSuccess(poAppController.searchMeasure(tfMeasure.getText() != null ? tfMeasure.getText() : "", false),
                                        "Initialize Search Measure! ")) {
                                    return;
                                }
                                loadRecord();
                                break;

                            case "tfVariant":
                                if (!isJSONSuccess(poAppController.searchVariant(tfVariant.getText() != null ? tfVariant.getText() : "", false),
                                        "Initialize Search Variant! ")) {
                                    return;
                                }
                                loadRecord();
                                break;

                            case "tfSuperseded":
                                if (!isJSONSuccess(poAppController.searchSuperseded(tfSuperseded.getText() != null ? tfSuperseded.getText() : "", false),
                                        "Initialize Search Superseded! ")) {
                                    return;
                                }
                                loadRecord();
                                break;

                            //SUB ITEM SEARCHING 
                            case "tfBarcodeOther":
                                if (!isJSONSuccess(poAppController.searchOther(pnRecordDetail, tfBarcodeOther.getText() != null ? tfBarcodeOther.getText() : "", true),
                                        "Initialize Search Check! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedRecordDetail(pnRecordDetail);
                                break;
                            case "tfDescriptionOther":
                                if (!isJSONSuccess(poAppController.searchOther(pnRecordDetail, tfDescriptionOther.getText() != null ? tfDescriptionOther.getText() : "", false),
                                        "Initialize Search Check! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedRecordDetail(pnRecordDetail);
                                break;

                        }
                        break;
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {

            e.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, e.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
            }
        }
    }
    private final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {

            }
        } else {
            loTextField.selectAll();
        }

    };

    private void txtArea_KeyPressed(KeyEvent event) {
        TextArea loTxtField = (TextArea) event.getSource();
        String txtFieldID = ((TextArea) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case UP:
                        CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        return;
                    case DOWN:
                        CommonUtils.SetNextFocus(loTxtField);
                        return;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, e.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
            }
        }
    }

    private void loadRecord() {
        try {
            lblStatus.setText(RecordStatus.ACTIVE.equals(poAppController.getModel().getRecordStatus()) ? "ACTIVE" : "INACTIVE");

            tfStockID.setText(poAppController.getModel().getStockId());
            tfBarcode.setText(poAppController.getModel().getBarCode());
            tfAltBarcode.setText(poAppController.getModel().getAlternateBarCode());
            tfBriefDescription.setText(poAppController.getModel().getBriefDescription());
            tfDescription.setText(poAppController.getModel().getDescription());
            cmbUnitType.getSelectionModel().select(Integer.parseInt(poAppController.getModel().getUnitType()));
            tfInvType.setText(poAppController.getModel().InventoryType().getDescription());
            tfCategory1.setText(poAppController.getModel().Category().getDescription());
            tfCategory2.setText(poAppController.getModel().CategoryLevel2().getDescription());
            tfCategory3.setText(poAppController.getModel().CategoryLevel3().getDescription());
            tfCategory4.setText(poAppController.getModel().CategoryLevel4().getDescription());
            tfBrand.setText(poAppController.getModel().Brand().getDescription());
            tfModel.setText(poAppController.getModel().Model().getDescription());
            tfColor.setText(poAppController.getModel().Color().getDescription());
            tfMeasure.setText(poAppController.getModel().Measure().getDescription());
            tfVariant.setText(poAppController.getModel().Variant().getDescription());
            tfSuperseded.setText(poAppController.getModel().Superseded().getBarCode());

            tfDiscount1.setText(CommonUtils.NumberFormat(poAppController.getModel().getDiscountRateLevel1(), "##0.00"));
            tfDiscount2.setText(CommonUtils.NumberFormat(poAppController.getModel().getDiscountRateLevel2(), "##0.00"));
            tfDiscount3.setText(CommonUtils.NumberFormat(poAppController.getModel().getDiscountRateLevel3(), "##0.00"));
            tfDiscount4.setText(CommonUtils.NumberFormat(poAppController.getModel().getDealerDiscountRate(), "##0.00"));

            tfMinLevel.setText(CommonUtils.NumberFormat(poAppController.getModel().getMinimumInventoryLevel(), "##0"));
            tfMaxLevel.setText(CommonUtils.NumberFormat(poAppController.getModel().getMaximumInventoryLevel(), "##0"));
            tfShelfLife.setText(CommonUtils.NumberFormat(poAppController.getModel().getShelfLife(), "##0"));
            tfCost.setText(CommonUtils.NumberFormat(poAppController.getModel().getCost(), "###,###,##0.0000"));
            tfSRP.setText(CommonUtils.NumberFormat(poAppController.getModel().getSellingPrice(), "###,###,##0.0000"));

            chkCombo.setSelected(poAppController.getModel().isComboInventory());
            chkPromo.setSelected(poAppController.getModel().isWithPromo());
            chkSerialized.setSelected(poAppController.getModel().isSerialized());
            chkRecordStatus.setSelected(poAppController.getModel().isRecordActive());
        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, e.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
            }
        }
    }

    private void loadSelectedRecordDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        int tblIndex = fnRow - 1;
        tfBarcodeOther.setText(tblColBarcode.getCellData(tblIndex));
        tfDescriptionOther.setText(tblColBarcode.getCellData(tblIndex));
        tfMeasurementOther.setText(tblColBarcode.getCellData(tblIndex));
        tfQtyOther.setText(tblColBarcode.getCellData(tblIndex));

    }

    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TextArea) {
                TextArea loControlField = (TextArea) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtArea_KeyPressed);
                loControlField.focusedProperty().addListener(txtArea_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);
            }
        }

        chkSerialized.setOnAction(e -> {
            poAppController.getModel().isSerialized(chkSerialized.isSelected());
        });

        chkCombo.setOnAction(e -> {
            poAppController.getModel().isComboInventory(chkCombo.isSelected());
        });

        chkPromo.setOnAction(e -> {
            poAppController.getModel().isWithPromo(chkPromo.isSelected());
        });

        chkRecordStatus.setOnAction(e -> {
            poAppController.getModel().isRecordActive(chkRecordStatus.isSelected());
        });

        cmbUnitType.setOnAction(e -> {
            poAppController.getModel().setUnitType(String.valueOf(cmbUnitType.getSelectionModel().getSelectedIndex()));
        });
        clearAllInputs();
    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
    }

    private void clearAllInputs() {

        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl instanceof TextArea) {
                ((TextArea) loControl).clear();
            } else if (loControl != null && loControl instanceof TableView) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }

            } else if (loControl instanceof DatePicker) {
                ((DatePicker) loControl).setValue(null);
            } else if (loControl instanceof ComboBox) {
                ((ComboBox) loControl).setItems(null);
            } else if (loControl instanceof ComboBox) {
                ((ComboBox) loControl).setItems(null);
            } else if (loControl instanceof CheckBox) {
                ((CheckBox) loControl).setSelected(false);
            }
        }
        initButtonDisplay(poAppController.getEditMode());
        cmbUnitType.setItems(unitType);

    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnClose");

        // Show-only based on mode
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnNew", "btnUpdate");

        apMaster.setDisable(!lbShow);
//        apDetail.setDisable(!lbShow);
        apDetailOther.setDisable(!lbShow);
    }

    private void initButtonControls(boolean visible, String... buttonFxIdsToShow) {
        Set<String> showOnly = new HashSet<>(Arrays.asList(buttonFxIdsToShow));

        for (Field loField : getClass().getDeclaredFields()) {
            loField.setAccessible(true);
            String fieldName = loField.getName(); // fx:id

            // Only touch the buttons listed
            if (!showOnly.contains(fieldName)) {
                continue;
            }
            try {
                Object value = loField.get(this);
                if (value instanceof Button) {
                    Button loButton = (Button) value;
                    loButton.setVisible(visible);
                    loButton.setManaged(visible);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, e.getMessage());
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
                }
            }
        }
    }

    private void initializeTableDetail() {
        if (laRecordDetail == null) {
            laRecordDetail = FXCollections.observableArrayList();

            tblSubItems.setItems(laRecordDetail);

            tblColQty.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

            tblColNo.setCellValueFactory((loModel) -> {
                int index = tblSubItems.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColBarcode.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(loModel.getValue().getBarCode());
            });

            tblColDescription.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(loModel.getValue().getDescription());
            });

            tblColMeasurement.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Measure().getDescription());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    if (Platform.isFxApplicationThread()) {
                        ShowMessageFX.Warning(null, psFormName, e.getMessage());
                    } else {
                        Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
                    }
                    return new SimpleStringProperty("");
                }
            });

            //refactor me when sub item is to is available
            tblColQty.setCellValueFactory((loModel) -> {
//                try {
                return new SimpleStringProperty("0.00");
//                } catch (SQLException | GuanzonException e) {
//                    poLogWrapper.severe(psFormName, e.getMessage());
//                    return new SimpleStringProperty("0.0");
//                }
            });

        }
    }

    private void reloadTableDetail() {
        List<Model_Inventory> rawDetail = poAppController.getSubItemList();
        laRecordDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnRecordDetail >= 1 && pnRecordDetail < laRecordDetail.size())
                ? pnRecordDetail - 1
                : laRecordDetail.size() - 1;

        tblSubItems.getSelectionModel().select(indexToSelect);

        pnRecordDetail = tblSubItems.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        tblSubItems.refresh();
    }

    private void getLoadedRecord() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadRecord();
        reloadTableDetail();
        loadSelectedRecordDetail(pnRecordDetail);
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        System.out.println("isJSONSuccess called. Thread: " + Thread.currentThread().getName());

        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(psFormName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message));
                }
            }
            return false;
        }

        if ("success".equalsIgnoreCase(result)) {
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Information(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, fsModule + ": " + message));
                }
            }
            poLogWrapper.info(psFormName + " : Success on " + fsModule);
            return true;
        }

        // Unknown or null result
        poLogWrapper.warning(psFormName + " : Unrecognized result: " + result);
        return false;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
    }

    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox
                        || value instanceof CheckBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, e.getMessage());
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
                }
            }
        }
        return controls;
    }

}
