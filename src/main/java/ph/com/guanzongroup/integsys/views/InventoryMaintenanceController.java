package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
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
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.inv.InvMaster;
import org.guanzon.cas.inv.model.Model_Inv_Ledger;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.services.InvControllers;
import org.json.simple.JSONObject;

/**
 * FXML Controller class
 *
 * @author mnv
 */
public class InventoryMaintenanceController implements Initializable, ScreenInterface {
    
    private final String psFormName = "Inventory Maintenance";
    LogWrapper poLogWrapper = new LogWrapper("cas", "cas-err.log");
    
    private GRiderCAS poApp;
    private InvMaster poAppController;
    private Control lastFocusedControl;
    private String psIndustryID;
    private String psCategoryID;
    private double xOffset;
    private double yOffset;
    
    private boolean pbLoaded = false;
    
    private unloadForm poUnload = new unloadForm();
    
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
    private AnchorPane apMaster, apDetail, apMainAnchor;
    
    @FXML
    private TextField tfSearchDescription, tfSearchBarcode;
    
    @FXML
    private Button btnBrowse, btnSave, btnUpdate, btnSearch, btnCancel, btnClose,
            btnLedger, btnSerial;
    
    @FXML
    private TextField tfStockID, tfBarcode, tfAltBarcode, tfBriefDescription, tfDescription, tfInvType,
            tfCategory1, tfCategory2, tfCategory3, tfCategory4, tfBrand, tfModel, tfColor,
            tfMeasure, tfVariant, tfDiscount1, tfDiscount2, tfDiscount3, tfDiscount4, tfMinLevel,
            tfMaxLevel, tfCost, tfSRP, tfSuperseded, tfShelfLife,
            tfLocation, tfSection, tfWarehouse, tfLevel, tfClass, tfQOH, tfAveMonthlySales, tfMinLevelMaster, tfCXOrder,
            tfMaxLevelMaster, tfOnHand;
    @FXML
    private DatePicker dpBegDate;
    @FXML
    private ComboBox cmbUnitType;
    
    @FXML
    private CheckBox chkSerialized, chkCombo, chkPromo, chkRecordStatus;
    
    @FXML
    private Label lblStatus;

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
            poAppController = new InvControllers(poApp, poLogWrapper).InventoryMaster();
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
                
            });
            initControlEvents();
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(CheckDeposit_EntryController.class.getName()).log(Level.SEVERE, null, e);
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
                        case "tfLocation":
                            if (!isJSONSuccess(poAppController.searchLocation(tfLocation.getText() != null ? tfLocation.getText() : "", true, false),
                                    "Initialize Search Location! By Location! ")) {
                                return;
                            }
                            loadRecord();
                            break;
                        case "tfWarehouse":
                            if (!isJSONSuccess(poAppController.searchLocation(tfWarehouse.getText() != null ? tfWarehouse.getText() : "", false, true),
                                    "Initialize Search Location! By Warehouse! ")) {
                                return;
                            }
                            loadRecord();
                            break;
                        case "tfSection":
                            if (!isJSONSuccess(poAppController.searchLocation(tfSection.getText() != null ? tfSection.getText() : "", false, false),
                                    "Initialize Search Location! By Section! ")) {
                                return;
                            }
                            loadRecord();
                            break;
                        case "tfLevel":
                            if (!isJSONSuccess(poAppController.searchBinLevel(tfLevel.getText() != null ? tfLevel.getText() : "", false),
                                    "Initialize Search Bin Level! ")) {
                                return;
                            }
                            loadRecord();
                            break;
                        
                    }
                    break;
                
                case "btnBrowse":
//                    if (lastFocusedControl == null) {
//                        if (!tfStockID.getText().isEmpty()) {
//                            if (ShowMessageFX.OkayCancel(null, "Search Record! by Barcode", "Are you sure you want replace loaded Record?") == false) {
//                                return;
//                            }
//                        }
//                        if (!isJSONSuccess(poAppController.searchRecordInventoryMaster(tfSearchBarcode.getText() != null ? tfSearchBarcode.getText() : "", true),
//                                "Initialize Search Barcode No! ")) {
//                            return;
//                        }
//                        if (poAppController.getEditMode() == EditMode.ADDNEW) {
//                            ShowMessageFX.Warning("No Inventory Detected", "Inventory Master", "No inventory found in your Branch!!!Please "
//                                    + "Save Record to create. ");
//                        }
//
//                        getLoadedRecord();
//                        initButtonDisplay(poAppController.getEditMode());
//                        return;
//                    }
                    
                    switch (lastFocusedControl.getId()) {
                        case "tfSearchBarcode":
                            if (!tfStockID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Record! by Barcode", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecordInventoryMaster(tfSearchBarcode.getText() != null ? tfSearchBarcode.getText() : "", true),
                                    "Initialize Search Barcode No! ")) {
                                return;
                            }
                            if (poAppController.getEditMode() == EditMode.ADDNEW) {
                                ShowMessageFX.Warning("No Inventory Detected", "Inventory Master", "No inventory found in your Branch!!!Please "
                                        + "Save Record to create. ");
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
                            if (!isJSONSuccess(poAppController.searchRecordInventoryMaster(tfSearchDescription.getText() != null ? tfSearchDescription.getText() : "", false),
                                    "Initialize Search Record! ")) {
                                return;
                            }
                            if (poAppController.getEditMode() == EditMode.ADDNEW) {
                                ShowMessageFX.Warning("No Inventory Detected", "Inventory Master", "No inventory found in your Branch!!!Please "
                                        + "Save Record to create. ");
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
                            if (!isJSONSuccess(poAppController.searchRecordInventoryMaster(tfSearchBarcode.getText() != null ? tfSearchBarcode.getText() : "", true),
                                    "Initialize Search Barcode No! ")) {
                                return;
                            }
                            if (poAppController.getEditMode() == EditMode.ADDNEW) {
                                ShowMessageFX.Warning("No Inventory Detected", "Inventory Master", "No inventory found in your Branch!!!Please "
                                        + "Save Record to create. ");
                            }
                            getLoadedRecord();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
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
                        poAppController = new InvControllers(poApp, poLogWrapper).InventoryMaster();
                        poAppController.initialize();
//                        //initlalize and validate record objects from class controller
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
                
                case "btnLedger":
                    if (!isJSONSuccess(showLedger(), "Initialize show Ledger Record")) {
                        return;
                    }
                    break;
                case "btnSerial":
                    if (!isJSONSuccess(showSerial(), "Initialize show Serial Record")) {
                        return;
                    }
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
                
                case "tfMinLevelMaster":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfMinLevelMaster.getText()) > 0.0) {
                            tfMinLevelMaster.setText("0.00");
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
                        poAppController.getModel().setMinimumLevel(lnMinLevel);
                        loadRecord();
                        loTextField.requestFocus();
                        if (Platform.isFxApplicationThread()) {
                            ShowMessageFX.Warning(null, psFormName, e.getMessage());
                        } else {
                            Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
                        }
                    }
                    
                    poAppController.getModel().setMinimumLevel(lnMinLevel);
                    loadRecord();
                    
                    break;
                
                case "tfMaxLevelMaster":
                    if (poAppController.getModel().getStockId() == null
                            || poAppController.getModel().getStockId().isEmpty()) {
                        if (Double.parseDouble(tfMaxLevelMaster.getText()) > 0.0) {
                            tfMaxLevelMaster.setText("0.00");
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
                        poAppController.getModel().setMaximumLevel(lnMaxLevel);
                        loadRecord();
                        loTextField.requestFocus();
                        if (Platform.isFxApplicationThread()) {
                            ShowMessageFX.Warning(null, psFormName, e.getMessage());
                        } else {
                            Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
                        }
                    }
                    
                    poAppController.getModel().setMaximumLevel(lnMaxLevel);
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
        JSONObject poJSON = new JSONObject();
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
                                if (!isJSONSuccess(poAppController.searchRecordInventoryMaster(tfSearchBarcode.getText() != null ? tfSearchBarcode.getText() : "", true),
                                        "Initialize Search Barcode No! ")) {
                                    return;
                                }
                                if (poAppController.getEditMode() == EditMode.ADDNEW) {
                                    ShowMessageFX.Warning("No Inventory Detected", "Inventory Master", "No inventory found in your Branch!!!Please "
                                            + "Save Record to create. ");
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
                                if (!isJSONSuccess(poAppController.searchRecordInventoryMaster(tfSearchDescription.getText() != null ? tfSearchDescription.getText() : "", false),
                                        "Initialize Search Record! ")) {
                                    return;
                                }
                                
                                if (poAppController.getEditMode() == EditMode.ADDNEW) {
                                    ShowMessageFX.Warning("No Inventory Detected", "Inventory Master", "No inventory found in your Branch!!!Please "
                                            + "Save Record to create. ");
                                }
                                
                                getLoadedRecord();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            
                            case "tfLocation":
                                if (!isJSONSuccess(poAppController.searchLocation(tfLocation.getText() != null ? tfLocation.getText() : "", true, false),
                                        "Initialize Search Location! By Location! ")) {
                                    return;
                                }
                                loadRecord();
                                break;
                            case "tfWarehouse":
                                if (!isJSONSuccess(poAppController.searchLocation(tfWarehouse.getText() != null ? tfWarehouse.getText() : "", false, true),
                                        "Initialize Search Location! By Warehouse! ")) {
                                    return;
                                }
                                loadRecord();
                                break;
                            case "tfSection":
                                if (!isJSONSuccess(poAppController.searchLocation(tfSection.getText() != null ? tfSection.getText() : "", false, false),
                                        "Initialize Search Location! By Section! ")) {
                                    return;
                                }
                                loadRecord();
                                break;
                            case "tfLevel":
                                if (!isJSONSuccess(poAppController.searchBinLevel(tfLevel.getText() != null ? tfLevel.getText() : "", false),
                                        "Initialize Search Bin Level! ")) {
                                    return;
                                }
                                loadRecord();
                                break;
                            
                        }
                        break;
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            ex.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, ex.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, ex.getMessage()));
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
        } catch (Exception ex) {
            ex.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, ex.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, ex.getMessage()));
            }
        }
    }
    
    private void loadRecord() {
        try {
            lblStatus.setText(RecordStatus.ACTIVE.equals(poAppController.getModel().getRecordStatus()) ? "ACTIVE" : "INACTIVE");
            
            tfStockID.setText(poAppController.getModel().getStockId());
            tfBarcode.setText(poAppController.getModel().Inventory().getBarCode());
            tfAltBarcode.setText(poAppController.getModel().Inventory().getAlternateBarCode());
            tfBriefDescription.setText(poAppController.getModel().Inventory().getBriefDescription());
            tfDescription.setText(poAppController.getModel().Inventory().getDescription());
            cmbUnitType.getSelectionModel().select(Integer.parseInt(poAppController.getModel().Inventory().getUnitType()));
            tfInvType.setText(poAppController.getModel().Inventory().InventoryType().getDescription());
            tfCategory1.setText(poAppController.getModel().Inventory().Category().getDescription());
            tfCategory2.setText(poAppController.getModel().Inventory().CategoryLevel2().getDescription());
            tfCategory3.setText(poAppController.getModel().Inventory().CategoryLevel3().getDescription());
            tfCategory4.setText(poAppController.getModel().Inventory().CategoryLevel4().getDescription());
            tfBrand.setText(poAppController.getModel().Inventory().Brand().getDescription());
            tfModel.setText(poAppController.getModel().Inventory().Model().getDescription());
            tfColor.setText(poAppController.getModel().Inventory().Color().getDescription());
            tfMeasure.setText(poAppController.getModel().Inventory().Measure().getDescription());
            tfVariant.setText(poAppController.getModel().Inventory().Variant().getDescription());
            tfSuperseded.setText(poAppController.getModel().Inventory().Superseded().getBarCode());
            
            tfDiscount1.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getDiscountRateLevel1(), "##0.00"));
            tfDiscount2.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getDiscountRateLevel2(), "##0.00"));
            tfDiscount3.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getDiscountRateLevel3(), "##0.00"));
            tfDiscount4.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getDealerDiscountRate(), "##0.00"));
            
            tfMinLevel.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getMinimumInventoryLevel(), "##0"));
            tfMaxLevel.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getMaximumInventoryLevel(), "##0"));
            tfShelfLife.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getShelfLife(), "##0"));
            tfCost.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getCost(), "###,###,##0.0000"));
            tfSRP.setText(CommonUtils.NumberFormat(poAppController.getModel().Inventory().getSellingPrice(), "###,###,##0.0000"));
            
            chkCombo.setSelected(poAppController.getModel().Inventory().isComboInventory());
            chkPromo.setSelected(poAppController.getModel().Inventory().isWithPromo());
            chkSerialized.setSelected(poAppController.getModel().Inventory().isSerialized());
            chkRecordStatus.setSelected(poAppController.getModel().Inventory().isRecordActive());

            //loading of Record 
            tfLocation.setText(poAppController.getModel().Location().getDescription());
            tfWarehouse.setText(poAppController.getModel().Location().Warehouse().getDescription());
            tfSection.setText(poAppController.getModel().Location().Section().getDescription());
            tfLevel.setText(poAppController.getModel().BinLevel().getBinName());
            dpBegDate.setValue(ParseDate(poAppController.getModel().getBeginningInventoryDate()));
            tfClass.setText(poAppController.getModel().getInventoryClassification());
            tfQOH.setText(CommonUtils.NumberFormat(poAppController.getModel().getBeginningInventoryQuantity(), "##0.00"));
            tfOnHand.setText(CommonUtils.NumberFormat(poAppController.getModel().getQuantityOnHand(), "##0.00"));
            tfAveMonthlySales.setText(CommonUtils.NumberFormat(poAppController.getModel().getAverageMonthlySales(), "##0.00"));
            tfCXOrder.setText(CommonUtils.NumberFormat(poAppController.getModel().getReserveOrderQuantity(), "##0.00"));
            tfMinLevelMaster.setText(CommonUtils.NumberFormat(poAppController.getModel().getMinimumLevel(), "##0"));
            tfMaxLevelMaster.setText(CommonUtils.NumberFormat(poAppController.getModel().getMaximumLevel(), "##0"));
            
        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(null, psFormName, e.getMessage());
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, e.getMessage()));
            }
        }
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
        tfStockID.setText("");
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
        String lsStockId = tfStockID.getText();
        boolean lbHasRecord = lsStockId != null && !lsStockId.isEmpty();
        boolean lbIsSerialized = lbHasRecord
                && "1".equals(poAppController.getModel().getRecordStatus());
        // Always show these buttons
        initButtonControls(true, "btnClose");

        // Show-only based on mode
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnNew", "btnUpdate");

        // Transaction-dependent buttons (only when not editing)
        initButtonControls(!lbShow && lbHasRecord, "btnUpdate", "btnLedger", "btnSerial");
        initButtonControls(!lbShow && lbHasRecord && lbIsSerialized, "btnSerial");
        
        apMaster.setDisable(!lbShow);//viewing only
        apDetail.setDisable(!lbShow);
        tfWarehouse.setDisable(fnEditMode != EditMode.ADDNEW);
        tfSection.setDisable(fnEditMode != EditMode.ADDNEW);
        tfLevel.setDisable(fnEditMode != EditMode.ADDNEW);
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
    
    private void getLoadedRecord() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadRecord();
    }
    
    private JSONObject showLedger() {
        JSONObject loJSON = new JSONObject();
        if (tfStockID.getText().isEmpty()) {
            loJSON.put("result", "error");
            loJSON.put("message", "Please select a record first.");
            return loJSON;
        }
        
        if (poAppController.getModel().getEditMode() == EditMode.ADDNEW) {
            loJSON.put("result", "error");
            loJSON.put("message", "Inventory is not yet saved.");
            return loJSON;
        }
        
        StackPane overlay = getOverlayProgress(apMainAnchor);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);
        
        Task<ObservableList<Model_Inv_Ledger>> loadLedger = new Task<ObservableList<Model_Inv_Ledger>>() {
            @Override
            protected ObservableList<Model_Inv_Ledger> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadLedgerList("", ""),
                        "Initialize : Load of Record List")) {
                    return null;
                }
                
                List<Model_Inv_Ledger> rawList = poAppController.getLedgerList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }
            
            @Override
            protected void succeeded() {
                
                if (poAppController.getLedgerList().size() <= 0) {
                    return;
                }
                InventoryLedgerController inventoryLedger = new InventoryLedgerController();
                inventoryLedger.setInventoryMaster(poAppController);
                inventoryLedger.setGRider(poApp);
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/ph/com/guanzongroup/integsys/views/InventoryLedger.fxml"));
                fxmlLoader.setController(inventoryLedger);
                
                Parent parent;
                try {
                    parent = fxmlLoader.load();
                    Stage stage = new Stage();
                    
                    Stage parentStage = (Stage) apMainAnchor.getScene().getWindow();
                    stage.initOwner(parentStage);
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initStyle(StageStyle.UNDECORATED);
                    
                    parent.setOnMousePressed(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            xOffset = event.getSceneX();
                            yOffset = event.getSceneY();
                        }
                    });
                    parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            stage.setX(event.getScreenX() - xOffset);
                            stage.setY(event.getScreenY() - yOffset);
                            
                        }
                    });
                    
                    Scene scene = new Scene(parent);
                    stage.setScene(scene);
                    stage.showAndWait();
                    
                    loJSON.put("result", "success");
                } catch (IOException ex) {
                    
                    loJSON.put("result", "error");
                    loJSON.put("message", ex.getMessage());
                }
                
                overlay.setVisible(false);
                pi.setVisible(false);
            }
            
            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                ex.printStackTrace();
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }
            
            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadLedger);
        thread.setDaemon(true);
        thread.start();
        
        loJSON.put("result", "success");
        return loJSON;
    }
    
    private JSONObject showSerial() throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        if (tfStockID.getText().isEmpty()) {
            loJSON.put("result", "error");
            loJSON.put("message", "Please select a record first.");
            return loJSON;
        }
        
        if (!poAppController.getModel().Inventory().isSerialized()) {
            loJSON.put("result", "error");
            loJSON.put("message", "Non-serialize inventory detected.");
            return loJSON;
        }
        
        if (poAppController.getModel().getEditMode() == EditMode.ADDNEW) {
            loJSON.put("result", "error");
            loJSON.put("message", "Inventory is not yet saved.");
            return loJSON;
        }
        
        StackPane overlay = getOverlayProgress(apMainAnchor);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);
        
        Task<ObservableList<Model_Inv_Serial>> loadSerial = new Task<ObservableList<Model_Inv_Serial>>() {
            @Override
            protected ObservableList<Model_Inv_Serial> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadSerialList(""),
                        "Initialize : Load of Record List")) {
                    return null;
                }
                
                List<Model_Inv_Serial> rawList = poAppController.getSerialList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }
            
            @Override
            protected void succeeded() {
                if (poAppController.getSerialList().size() <= 0) {
                    return;
                }
                InventorySerialController inventorySerial = new InventorySerialController();
                inventorySerial.setInventoryMaster(poAppController);
                inventorySerial.setGRider(poApp);
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/ph/com/guanzongroup/integsys/views/InventorySerial.fxml"));
                fxmlLoader.setController(inventorySerial);
                
                Parent parent;
                try {
                    parent = fxmlLoader.load();
                    Stage stage = new Stage();
                    
                    Stage parentStage = (Stage) apMainAnchor.getScene().getWindow();
                    stage.initOwner(parentStage);
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initStyle(StageStyle.UNDECORATED);
                    
                    parent.setOnMousePressed(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            xOffset = event.getSceneX();
                            yOffset = event.getSceneY();
                        }
                    });
                    parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            stage.setX(event.getScreenX() - xOffset);
                            stage.setY(event.getScreenY() - yOffset);
                            
                        }
                    });
                    
                    Scene scene = new Scene(parent);
                    stage.setScene(scene);
                    stage.showAndWait();
                    
                    loJSON.put("result", "success");
                } catch (IOException ex) {
                    
                    ex.printStackTrace(); // prints full trace
                    loJSON.put("result", "error");
                    loJSON.put("message", ex.getMessage());
                }
                
                overlay.setVisible(false);
                pi.setVisible(false);
            }
            
            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                ex.printStackTrace();
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }
            
            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadSerial);
        thread.setDaemon(true);
        thread.start();
        
        loJSON.put("result", "success");
        return loJSON;
    }
    
    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");
        
        System.out.println("isJSONSuccess called. Thread: " + Thread.currentThread().getName());
        
        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(psFormName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, message));
                }
            }
            return false;
        }
        
        if ("success".equalsIgnoreCase(result)) {
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Information(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, message));
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
