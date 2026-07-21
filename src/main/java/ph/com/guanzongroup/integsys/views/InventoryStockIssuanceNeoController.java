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
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;
import org.guanzon.cas.inv.warehouse.InventoryStockIssuanceNeo;
import org.guanzon.cas.inv.warehouse.status.DeliveryIssuanceType;
import org.guanzon.cas.inv.warehouse.status.InventoryStockIssuanceStatus;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Transfer_Detail;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Transfer_Master;
import org.guanzon.cas.inv.warehouse.services.DeliveryIssuanceControllers;

/**
 * FXML Controller class
 *
 * @author User
 */
public class InventoryStockIssuanceNeoController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Issuance Entry";
    private String psIndustryID, psCompanyID, psCategoryID;
    private Control lastFocusedControl;
    private InventoryStockIssuanceNeo poAppController;
    private ObservableList<Model_Inventory_Transfer_Master> laTransactionMaster;
    private ObservableList<Model_Inventory_Transfer_Detail> laTransactionDetail;
    private int pnSelectMaster, pnEditMode, pnTransactionDetail;

    @FXML
    AnchorPane apMainAnchor, apMaster, apDetail, apDelivery;

    @FXML
    TextField tfSearchSourceno, tfSearchTransNo, tfTransNo, tfClusterName, tfTrucking, tfDiscountRate, tfDiscountAmount, tfTotal;

    @FXML
    DatePicker dpTransactionDate, dpDelDate;

    @FXML
    ComboBox cbDelType;

    @FXML
    TextArea taRemarks;

    @FXML
    TextField tfSearchSerial, tfSearchBarcode, tfSearchDescription, tfSupersede, tfBrand, tfModel, tfColor,
            tfVariant, tfMeasure, tfInvType, tfCost, tfIssuedQty, tfProjectCode, tfQOH;

    @FXML
    Button btnNew, btnUpdate, btnSearch, btnBrowse, btnSave, btnCancel, btnHistory, btnRetrieve, btnClose, btnVoid;

    @FXML
    TableView<Model_Inventory_Transfer_Master> tblViewMaster;

    @FXML
    TableView<Model_Inventory_Transfer_Detail> tblViewDetails;

    @FXML
    TableColumn<Model_Inventory_Transfer_Master, String> tblColNo, tblColTransNo, tblColTransDate, tblColBranch;

    @FXML
    TableColumn<Model_Inventory_Transfer_Detail, String> tblColDetailNo, tblColDetailOrderNo, tblColDetailSerial, tblColDetailBarcode, tblColDetailDescr,
            tblColDetailBrand, tblColDetailVariant, tblColDetailCost, tblColDetailQOH, tblColDetailOrderQty;

    @FXML
    Label lblSource, lblStatus;

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
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new DeliveryIssuanceControllers(poApp, poLogWrapper).InventoryStockIssuanceNeo();
            poAppController.setTransactionStatus("10");

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {
                poAppController.setTransactionStatus("10");
                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCompanyID(psCompanyID);
                poAppController.setCategoryID(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID
                        + "\nCompany :" + psCompanyID
                        + "\nCategory:" + psCategoryID);

                btnNew.fire();
            });
            initializeTableDetail();
            initControlEvents();
            lblSource.setText(poAppController.getMaster().Company().getCompanyName() + " - " + poAppController.getMaster().Industry().getDescription());

        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());

        }
    }

    @FXML
    void ontblMasterClicked(MouseEvent e) {
        pnSelectMaster = tblViewMaster.getSelectionModel().getSelectedIndex();
        if (pnSelectMaster < 0) {
            return;
        }

        if (e.getClickCount() == 1 && !e.isConsumed()) {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") != true) {
                    return;
                }
            }
            try {
                e.consume();
                if (!isJSONSuccess(poAppController.OpenTransaction(tblColTransNo.getCellData(pnSelectMaster)), psFormName)) {
                    ShowMessageFX.Information("Failed to open transaction", psFormName, null);
                    return;
                }

                getLoadedTransaction();

                initButtonDisplay(poAppController.getEditMode());
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {

                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            }

        }
        return;
    }

    @FXML
    void ontblDetailClicked(MouseEvent e) {
        try {
            pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1;
            if (pnTransactionDetail <= 0) {
                return;
            }

            loadSelectedTransactionDetail(pnTransactionDetail);
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
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

                        case "tfClusterName":
                            if (!isJSONSuccess(poAppController.searchTransactionDestination(tfClusterName.getText(), false),
                                    "Initialize Search Destination! ")) {
                                return;
                            }
                            tfClusterName.setText(poAppController.getMaster().BranchDestination().getBranchName());
                            break;
                        case "tfTrucking":
                            if (!isJSONSuccess(poAppController.searchTransactionTrucking(tfTrucking.getText(), false),
                                    "Initialize Search Trucking! ")) {
                                return;
                            }
                            tfTrucking.setText(poAppController.getMaster().TruckingCompany().getCompanyName());
                            break;
                        case "tfSearchSerial":
                            if (pnTransactionDetail > 0) {
                                if (!isJSONSuccess(poAppController.searchDetailByIssuance(pnTransactionDetail, tfSearchSerial.getText(), false, true),
                                        "Initialize Search Serial! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedTransactionDetail(pnTransactionDetail);
                            }
                            break;
                        case "tfSearchBarcode":
                            if (pnTransactionDetail > 0) {
                                if (!isJSONSuccess(poAppController.searchDetailByIssuance(pnTransactionDetail, tfSearchBarcode.getText(), true, false),
                                        "Initialize Search Barcode! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedTransactionDetail(pnTransactionDetail);
                            }
                            break;
                        case "tfSearchDescription":
                            if (pnTransactionDetail > 0) {
                                if (!isJSONSuccess(poAppController.searchDetailByIssuance(pnTransactionDetail, tfSearchDescription.getText(), false, false),
                                        "Initialize Search Description! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedTransactionDetail(pnTransactionDetail);
                            }
                            break;
                        case "tfSupersede":
                            if (pnTransactionDetail > 0) {
                                if (!isJSONSuccess(poAppController.searchDetailByBarcode(pnTransactionDetail, tfSupersede.getText(), true),
                                        "Initialize Search Supersede! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedTransactionDetail(pnTransactionDetail);
                            }
                            break;
                        case "tfProjectCode":
                            if (!isJSONSuccess(poAppController.searchTransactionProject(tfProjectCode.getText(), false),
                                    "Initialize Search Trucking! ")) {
                                return;
                            }
                            tfProjectCode.setText(poAppController.getMaster().Project().getProjectDescription());
                            break;
                    }
                    break;

                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                "Initialize Browse Transaction")) {
                            return;
                        }
                        getLoadedTransaction();
                        initButtonDisplay(poAppController.getEditMode());
                        break;

                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchSourceno":
                            if (!tfTransNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Transaction", "Are you sure you want to replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchSourceno.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

//                                tfSearchSourceno.setText(poAppController.getMaster().Branch().getBranchName());
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchTransNo":
                            if (!tfTransNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Transaction", "Are you sure you want to replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }

//                                tfSearchTransNo.setText(poAppController.getMaster().getTransactionNo());
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        default:
                            //Search record
                            if (!isJSONSuccess(poAppController.searchTransaction("", true, true),
                                    "Initialize Browse Transaction")) {
                                return;
                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;

                case "btnNew":
                    if (!isJSONSuccess(poAppController.NewTransaction(), "Initialize New Transaction")) {
                        return;
                    }
//                    clearAllInputs();
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnUpdate":
                    if (poAppController.getMaster().getTransactionNo() == null || poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Stock Request Issuance", "");
                        return;
                    }

                    if (!isJSONSuccess(poAppController.UpdateTransaction(), "Initialize UPdate Transaction")) {
                        return;
                    }
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnSave":
                    if (tfTransNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Stock Request Issuance", "");
                        return;
                    }
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save transaction?") != true) {
                        return;
                    }
                    if (!isJSONSuccess(poAppController.SaveTransaction(), "Initialize Save Transaction")) {
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Do you want to confirm transaction?") == true) {
                        if (!isJSONSuccess(poAppController.CloseTransaction(), "Initialize Close Transaction")) {
                            return;
                        }
                        if (ShowMessageFX.YesNo(null, psFormName, "Do you want to print transaction?") == true) {
                            if (!isJSONSuccess(poAppController.printRecord(), "Initialize print Transaction")) {
                                return;
                            }
                        }
                    }

                    reloadTableDetail();
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();

                    break;

                 case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        if (poAppController.getEditMode() != EditMode.ADDNEW) {
                            if (!isJSONSuccess(poAppController.OpenTransaction(tfTransNo.getText()),
                                    "Initialize Open Transaction")) {

                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        }
                        poAppController = new DeliveryIssuanceControllers(poApp, poLogWrapper).InventoryStockIssuanceNeo();
                        poAppController.setTransactionStatus("10");
                        if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                            unloadForm appUnload = new unloadForm();
                            appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        }

                        Platform.runLater(() -> {

                            poAppController.setTransactionStatus("10");
                            poAppController.getMaster().setIndustryId(psIndustryID);
                            poAppController.setIndustryID(psIndustryID);
                            poAppController.setCompanyID(psCompanyID);
                            poAppController.setCategoryID(psCategoryID);

                            clearAllInputs();
                        });
                        pnEditMode = poAppController.getEditMode();
                        return;
                    }
                    break;
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", psFormName, null);
                        return;
                    }

                    try {
                        poAppController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", psFormName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                    }
                    break;

                case "btnRetrieve":
                    if (lastFocusedControl == null) {
                        loadTransactionMasterList(tfSearchSourceno.getText(), "e.sBranchNm");
//                            getLoadedTransaction();
                        initButtonDisplay(poAppController.getEditMode());
                        break;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchSourceno":

                            loadTransactionMasterList(tfSearchSourceno.getText(), "e.sBranchNm");
//                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchTransNo":

                            loadTransactionMasterList(tfSearchTransNo.getText(), "a.sTransNox");
//                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        default:
                            loadTransactionMasterList(tfSearchSourceno.getText(), "e.sBranchNm");
//                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;
                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                    }
                    break;
                case "btnVoid":
                    if (tfTransNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null, "Issuance Approval");
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to Void/Cancel transaction?") == true) {
                        if (btnVoid.getText().equals("Void")) {
                            if (!isJSONSuccess(poAppController.VoidTransaction(), "Initialize Void Transaction")) {
                                return;
                            }
                        } else {
                            if (!isJSONSuccess(poAppController.CancelTransaction(), "Initialize Cancel Transaction")) {
                                return;
                            }

                        }
                        reloadTableDetail();
                        getLoadedTransaction();
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;
            }

            initButtonDisplay(poAppController.getEditMode());

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());
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
                case "taRemarks":
                    poAppController.getMaster().setRemarks(lsValue);
                    loadTransactionMaster();
                    break;

            }
        } else {
            loTextField.selectAll();
        }
    };

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        try {
            if (lsValue == null) {
                return;
            }

            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {
                    case "tfDiscountRate":
                        if (lsValue.isEmpty()) {
                            ShowMessageFX.Information("Invalid freight amount", psFormName, null);
                            loTextField.requestFocus();
                            return;
                        }

                        poAppController.getMaster().setFreight(Double.parseDouble(lsValue));
                        loadTransactionMaster();
                        break;

                    case "tfDiscountAmount":
                        if (lsValue.isEmpty()) {
                            ShowMessageFX.Information("Invalid discount amount", psFormName, null);
                            loTextField.requestFocus();
                            return;
                        }
                        if (tfDiscountRate.getText() != null && !tfDiscountRate.getText().isEmpty()) {
                            try {
                                double discountRate = Double.parseDouble(tfDiscountRate.getText());
                                if (discountRate <= 0) {
                                    ShowMessageFX.Information(
                                            "Invalid discount amount. Please add freight amount first.",
                                            psFormName, null
                                    );
                                    tfDiscountRate.requestFocus();
                                    tfDiscountAmount.setText("0.0");
                                    poAppController.getMaster().setDiscount(0.0);
                                    return;
                                }
                                if (Double.parseDouble(tfDiscountAmount.getText()) > 99.9) {
                                    ShowMessageFX.Information(
                                            "Invalid discount amount",
                                            psFormName, null
                                    );
                                    tfDiscountRate.requestFocus();
                                    tfDiscountAmount.setText("0.0");
                                    poAppController.getMaster().setDiscount(0.0);
                                    return;
                                }

                            } catch (NumberFormatException e) {
                                ShowMessageFX.Information(
                                        "Invalid input. Please enter a valid numeric discount rate.",
                                        psFormName, null
                                );
                                tfDiscountRate.requestFocus();
                                tfDiscountAmount.setText("0.0");
                                poAppController.getMaster().setDiscount(0.0);
                                return;
                            }
                        } else {
                            ShowMessageFX.Information(
                                    "Discount rate cannot be empty. Please enter a value.",
                                    psFormName, null
                            );
                            tfDiscountRate.requestFocus();
                            tfDiscountAmount.setText("0.0");
                            poAppController.getMaster().setDiscount(0.0);
                            return;
                        }
                        poAppController.getMaster().setDiscount(Double.parseDouble(lsValue));
                        loadTransactionMaster();
                        break;

                    case "tfIssuedQty":
                        if (poAppController.getDetail(pnTransactionDetail).getStockId() == null
                                || poAppController.getDetail(pnTransactionDetail).getStockId().isEmpty()) {
                            if (Double.parseDouble(tfIssuedQty.getText()) > 0.0) {
                                tfIssuedQty.setText("0.00");
                                loTextField.requestFocus();
                                ShowMessageFX.Information("Unable to set quantity! No Stock Invetory Detected", psFormName, null);
                            }
                            return;
                        }
                        double lnIssuedQty;
                        try {
                            lnIssuedQty = Double.parseDouble(lsValue);
                        } catch (NumberFormatException e) {
                            lnIssuedQty = 0.0; // default if parsing fails
                            poAppController.getDetail(pnTransactionDetail).setQuantity(lnIssuedQty);
                            reloadTableDetail();
                            loadSelectedTransactionDetail(pnTransactionDetail);
                            loTextField.requestFocus();
                        }
                        if (lnIssuedQty < 0.00) {
                            return;
                        }
                        // check if serialized
                        if (poAppController.getDetail(pnTransactionDetail).Inventory().isSerialized()) {
                            // must be whole number AND exactly 1
                            if (lnIssuedQty != 1 || lnIssuedQty % 1 != 0) {
                                ShowMessageFX.Information("Invalid quantity for serialized item", psFormName, null);
                                lnIssuedQty = 1; // force to 1
                                loTextField.setText("1");
                            }
                            if (poAppController.getDetail(pnTransactionDetail).getSerialID() == null
                                    || poAppController.getDetail(pnTransactionDetail).getSerialID().isEmpty()) {
                                //Search record and sepate the row
                                if (!isJSONSuccess(poAppController.searchDetailByIssuance(pnTransactionDetail, tfSearchSerial.getText(), true, true),
                                        "Initialize Search Serial")) {
                                    lnIssuedQty = 0;
                                    return;
                                }

                            }
                        }
                        if (lnIssuedQty > poAppController.getDetail(pnTransactionDetail).InventoryMaster().getQuantityOnHand()) {
                            lnIssuedQty = poAppController.getDetail(pnTransactionDetail).InventoryMaster().getQuantityOnHand();
                            ShowMessageFX.Information("Issued Quantity exceed Quantity on Hand Detected", psFormName, null);
                            loTextField.setText(String.valueOf(lnIssuedQty));
                            tfIssuedQty.requestFocus();
                        }

                        poAppController.getDetail(pnTransactionDetail).setQuantity(lnIssuedQty);

                        reloadTableDetail();
                        loadSelectedTransactionDetail(pnTransactionDetail);
                        break;

                }
            } else {
                loTextField.selectAll();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
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
                            default:
                                CommonUtils.SetNextFocus(loTxtField);
                                break;
                            case "tfSearchSourceno":
                                if (!tfTransNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Transaction", "Are you sure you want to replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(lsValue, true, true),
                                        "Initialize Search Source No! ")) {
                                    return;
                                }

//                                tfSearchSourceno.setText(poAppController.getMaster().Branch().getBranchName());
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchTransNo":
                                if (!tfTransNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Transaction", "Are you sure you want to replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(lsValue, true, true),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }

//                                tfSearchTransNo.setText(poAppController.getMaster().getTransactionNo());
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfClusterName":
                                if (!isJSONSuccess(poAppController.searchTransactionDestination(tfClusterName.getText(), false),
                                        "Initialize Search Destination! ")) {
                                    return;
                                }
                                tfClusterName.setText(poAppController.getMaster().BranchDestination().getBranchName());
                                break;
                            case "tfTrucking":
                                if (!isJSONSuccess(poAppController.searchTransactionTrucking(tfTrucking.getText(), false),
                                        "Initialize Search Trucking! ")) {
                                    return;
                                }
                                tfTrucking.setText(poAppController.getMaster().TruckingCompany().getCompanyName());
                                break;
                            case "tfProjectCode":
                                if (!isJSONSuccess(poAppController.searchTransactionProject(tfProjectCode.getText(), false),
                                        "Initialize Search Trucking! ")) {
                                    return;
                                }
                                tfProjectCode.setText(poAppController.getMaster().Project().getProjectDescription());
                                break;
                            case "tfSearchSerial":
                                if (pnTransactionDetail > 0) {
                                    if (!isJSONSuccess(poAppController.searchDetailByIssuance(pnTransactionDetail, tfSearchSerial.getText(), false, true),
                                            "Initialize Search Serial! ")) {
                                        return;
                                    }
                                    reloadTableDetail();

                                    loadSelectedTransactionDetail(pnTransactionDetail);
                                }
                                break;
                            case "tfSearchBarcode":
                                if (pnTransactionDetail > 0) {
                                    if (!isJSONSuccess(poAppController.searchDetailByIssuance(pnTransactionDetail, tfSearchBarcode.getText(), true, false),
                                            "Initialize Search Barcode! ")) {
                                        return;
                                    }
                                    reloadTableDetail();

                                    loadSelectedTransactionDetail(pnTransactionDetail);
                                }
                                break;
                            case "tfSearchDescription":
                                if (pnTransactionDetail > 0) {
                                    if (!isJSONSuccess(poAppController.searchDetailByIssuance(pnTransactionDetail, tfSearchDescription.getText(), false, false),
                                            "Initialize Search Description! ")) {
                                        return;
                                    }
                                    reloadTableDetail();

                                    loadSelectedTransactionDetail(pnTransactionDetail);
                                }
                                break;
                            case "tfSupersede":
                                if (pnTransactionDetail > 0) {
                                    if (!isJSONSuccess(poAppController.searchDetailByBarcode(pnTransactionDetail, tfSupersede.getText(), true),
                                            "Initialize Search Supersede! ")) {
                                        return;
                                    }
                                    reloadTableDetail();
                                    loadSelectedTransactionDetail(pnTransactionDetail);
                                }
                                break;

                        }
                        break;
                }
            }
        } catch (Exception ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private double computeDiscount(double total, double discount) {
        double totalDisc = (discount / 100) * total;
        return totalDisc;
    }

    private void loadDeliveryTypes() {
        List<String> deliveryTypes = DeliveryIssuanceType.DeliveryType;
        cbDelType.setItems(FXCollections.observableArrayList(deliveryTypes));
    }

    private void loadTransactionMasterList(String value, String fsColumn) {
        StackPane overlay = getOverlayProgress(apMaster);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_Inventory_Transfer_Master>> loadTransaction = new Task<ObservableList<Model_Inventory_Transfer_Master>>() {
            @Override
            protected ObservableList<Model_Inventory_Transfer_Master> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadTransactionList(value, fsColumn),
                        "Initialize : Load of Transaction List")) {
                    return null;
                }

                List<Model_Inventory_Transfer_Master> rawList = poAppController.getMasterList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                ObservableList<Model_Inventory_Transfer_Master> laMasterList = getValue();
                tblViewMaster.setItems(laMasterList);

                tblColNo.setCellValueFactory(loModel -> {
                    int index = tblViewMaster.getItems().indexOf(loModel.getValue()) + 1;
                    return new SimpleStringProperty(String.valueOf(index));
                });
                tblColTransNo.setCellValueFactory(loModel -> {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionNo()));
                });
                tblColTransDate.setCellValueFactory(loModel -> {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionDate()));
                });
                tblColBranch.setCellValueFactory(loModel -> {
                    try {
                        return new SimpleStringProperty(String.valueOf(loModel.getValue().BranchDestination().getBranchName()));
                    } catch (Exception e) {
                        poLogWrapper.severe(psFormName, e.getMessage());
                        return new SimpleStringProperty("");
                    }
                });

                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                Logger
                        .getLogger(DeliverySchedule_EntryController.class
                                .getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadTransaction);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadTransactionMaster() {
        try {
            lblSource.setText((poAppController.getMaster().Company().getCompanyName() == null ? "" : (poAppController.getMaster().Company().getCompanyName() + " - "))
                    + (poAppController.getMaster().Industry().getDescription() == null ? "" : poAppController.getMaster().Industry().getDescription()));
            lblStatus.setText(InventoryStockIssuanceStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())) == null ? "STATUS"
                    : InventoryStockIssuanceStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));

            tfTransNo.setText(poAppController.getMaster().getTransactionNo());
            dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getTransactionDate()));
            tfClusterName.setText(poAppController.getMaster().BranchDestination().getBranchName());
            tfTrucking.setText(poAppController.getMaster().TruckingCompany().getCompanyName());
            tfDiscountRate.setText(String.valueOf(poAppController.getMaster().getFreight()));
            tfDiscountAmount.setText(String.valueOf(poAppController.getMaster().getDiscount()));
            tfTotal.setText(String.valueOf(poAppController.getMaster().getTransactionTotal()));
            taRemarks.setText(poAppController.getMaster().getRemarks());
            tfProjectCode.setText(poAppController.getMaster().Project().getProjectDescription());

            computeTotal();
            cbDelType.getSelectionModel().select(Integer.parseInt(poAppController.getMaster().getDeliveryType()));
            if (poAppController.getMaster().getTransactionStatus().equals(InventoryStockIssuanceStatus.CONFIRMED)) {
                btnVoid.setText("Cancel");
            } else {
                btnVoid.setText("Void");
            }
            if (tfTransNo.getText().trim().isEmpty()) {
                lblStatus.setText("UNKNOWN");
            }
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());

        }
    }

    private void loadSelectedTransactionDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        int tblIndex = fnRow - 1;
        tfSearchSerial.setText(tblColDetailSerial.getCellData(tblIndex));
        tfSearchBarcode.setText(tblColDetailBarcode.getCellData(tblIndex));
        tfSearchDescription.setText(tblColDetailDescr.getCellData(tblIndex));
        tfBrand.setText(tblColDetailBrand.getCellData(tblIndex));
        tfVariant.setText(tblColDetailVariant.getCellData(tblIndex));
        tfCost.setText(tblColDetailCost.getCellData(tblIndex));
        tfIssuedQty.setText(tblColDetailOrderQty.getCellData(tblIndex));
        tfQOH.setText(tblColDetailQOH.getCellData(tblIndex));
//        tfReceiveQuantity.setText(tblColDetailRecQty.getCellData(tblIndex));

//        taNote.setText(poAppController.getDetail(fnRow).getNote());
        tfSupersede.setText(poAppController.getDetail(fnRow).InventorySupersede().getBarCode());
        tfModel.setText(poAppController.getDetail(fnRow).Inventory().Model().getDescription());
        tfColor.setText(poAppController.getDetail(fnRow).Inventory().Color().getDescription());
        tfMeasure.setText(poAppController.getDetail(fnRow).Inventory().Measure().getDescription());
        tfInvType.setText(poAppController.getDetail(fnRow).Inventory().InventoryType().getDescription());

    }

//    private void loadSelectedTableItem(int fnRow, TableView<?> ftblSrc, HashMap<String, Object> fmapObject) {
//        //iterate to table columns
//        for (TableColumn<?, ?> column : ftblSrc.getColumns()) {
//            String lscolumnID = column.getId(); //get column id
//            Object loField = fmapObject.get(lscolumnID); //get assigned object and validate type of object that will display the cell value
//
//            //display value to field if object is not empty
//            if (loField instanceof TextField) {
//                if (loField != null) {
//                    ((TextField) loField).setText(column.getCellData(fnRow) == null ? "" : column.getCellData(fnRow).toString());
//                }
//            }
//
//            if (loField instanceof DatePicker) {
//                if (loField != null) {
//                    ((DatePicker) loField).setValue(CustomCommonUtil.parseDateStringToLocalDate(
//                            column.getCellData(fnRow) == null ? "" : column.getCellData(fnRow).toString(), "yyyy-MM-dd"));
//                }
//            }
//        }
//    }
//
//    private void loadSelectedDetail() {
//        try {
//            HashMap<String, Object> loMapFields = new HashMap<>();
//            loMapFields.put("tblColDetailSerial", tfSearchSerial);
//            loMapFields.put("tblColDetailBarcode", tfSearchBarcode);
//            loMapFields.put("tblColDetailDescr", tfSearchDescription);
//            loMapFields.put("tblColDetailBrand", tfBrand);
//            loMapFields.put("tblColDetailVariant", tfVariant);
//            loMapFields.put("tblColDetailCost", tfCost);
//            loMapFields.put("tblColDetailOrderQty", tfIssuedQty);
//
//            loadSelectedTableItem(pnTransactionDetail - 1, tblViewDetails, loMapFields);
//
//            tfColor.setText(poAppController.getDetail(pnTransactionDetail).Inventory().Color().getDescription());
//            tfMeasure.setText(poAppController.getDetail(pnTransactionDetail).Inventory().Measure().getDescription());
//            tfInvType.setText(poAppController.getDetail(pnTransactionDetail).Inventory().InventoryType().getDescription());
//            tfSupersede.setText(poAppController.getDetail(pnTransactionDetail).InventorySupersede().getBarCode());
//
//        } catch (SQLException | GuanzonException e) {
//            poLogWrapper.severe(psFormName, e.getMessage());
//        }
//    }
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
                loControlField.focusedProperty().addListener(txtArea_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);
            }
        }

        cbDelType.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.intValue() != 1) {
                    poAppController.getMaster().setDeliveryType(String.valueOf(newVal.intValue()));
                } else {
                    poAppController.getMaster().setDeliveryType("0");
                    cbDelType.getSelectionModel().select(0);
                    ShowMessageFX.Information(
                            null,
                            psFormName, "Unable to Select delivery please Use Cluster Issuance.");
                }
            }
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
            } else if (loControl != null && (loControl instanceof TableView)) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }
            } else if (loControl != null && (loControl instanceof ComboBox)) {
                ComboBox cbox = (ComboBox) loControl;
                if (cbox.getItems() != null) {
                    cbox.getItems().clear();
                }
            }
        }
        pnEditMode = poAppController.getEditMode();
        loadDeliveryTypes();
        initButtonDisplay(poAppController.getEditMode());
        if (tfTransNo.getText().trim().isEmpty()) {
            lblStatus.setText("UNKNOWN");
        }
    }

    private void initButtonDisplay(int fnEditMode) {

        boolean lbEditing = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        String lsTransNo = tfTransNo.getText();
        boolean lbHasTransaction = lsTransNo != null && !lsTransNo.isEmpty();
        boolean lbIsApproved = lbHasTransaction
                && "1".equals(poAppController.getMaster().getTransactionStatus());

        // Always visible
        initButtonControls(true, "btnRetrieve", "btnClose");

        // Editing mode buttons
        initButtonControls(lbEditing, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbEditing, "btnBrowse", "btnNew");

        // Transaction-dependent buttons (only when not editing)
        initButtonControls(!lbEditing && lbHasTransaction, "btnUpdate", "btnVoid", "btnHistory", "btnPrint");
        initButtonControls(!lbEditing && lbHasTransaction && !lbIsApproved, "btnUpdate");

        // Disable panes during editing
        apMaster.setDisable(!lbEditing);
        apDetail.setDisable(!lbEditing);

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
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
                ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

                poLogWrapper.severe(psFormName + " :" + e.getMessage());
                ;
            }
        }
    }

    private void initializeTableDetail() {
        if (laTransactionDetail == null) {
            laTransactionDetail = FXCollections.observableArrayList();

            tblViewDetails.setItems(laTransactionDetail);

            tblColDetailCost.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColDetailOrderQty.setStyle("-fx-alignment: CENTER; -fx-padding: 0 5 0 0;");
            tblColDetailQOH.setStyle("-fx-alignment: CENTER; -fx-padding: 0 5 0 0;");

            tblColDetailNo.setCellValueFactory((loModel) -> {
                int index = tblViewDetails.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDetailOrderNo.setCellValueFactory((loModel) -> new SimpleStringProperty(loModel.getValue().getOrderNo()));

            tblColDetailSerial.setCellValueFactory(loModel -> {
                try {
                    String s1 = loModel.getValue().InventorySerial().getSerial01();
                    String s2 = loModel.getValue().InventorySerial().getSerial02();

                    String xserialname = (s1 != null && !s1.isEmpty() ? s1 : "")
                            + ((s1 != null && !s1.isEmpty() && s2 != null && !s2.isEmpty()) ? " / " : "")
                            + (s2 != null && !s2.isEmpty() ? s2 : "");

                    return new SimpleStringProperty(xserialname);
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailBarcode.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().getBarCode());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailDescr.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().getDescription());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailBrand.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().Brand().getDescription());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailVariant.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().Variant().getDescription());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailCost.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().Inventory().getCost()));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailQOH.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().InventoryMaster().getQuantityOnHand()));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailOrderQty.setCellValueFactory((loModel) -> new SimpleStringProperty(String.valueOf(loModel.getValue().getQuantity())));

        }
    }

    private void reloadTableDetail() {
        List<Model_Inventory_Transfer_Detail> rawDetail = poAppController.getDetailList();
        laTransactionDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnTransactionDetail >= 1 && pnTransactionDetail < laTransactionDetail.size())
                ? pnTransactionDetail - 1
                : laTransactionDetail.size() - 1;

        tblViewDetails.getSelectionModel().select(indexToSelect);

        pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        tblViewDetails.refresh();
        computeTotal();
    }

    private void getLoadedTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadTransactionMaster();
        reloadTableDetail();
        loadSelectedTransactionDetail(pnTransactionDetail);
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            if (message != null) {
                poLogWrapper.severe(psFormName + " :" + message);
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, message));
                }
            }
            return false;
        }

        String message = (String) loJSON.get("message");
        poLogWrapper.severe(psFormName + " :" + message);
        if (message != null) {
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Information(null, psFormName, message);
            } else {
                Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, message));
            }
        }

        poLogWrapper.info(psFormName + " : Success on " + fsModule);
        return true;
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
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
                ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }

    private void computeTotal() {
        double lnDiscountedFreight = poAppController.getMaster().getFreight() - computeDiscount(
                poAppController.getMaster().getFreight(), poAppController.getMaster().getDiscount());
        double lnDetailTotal = 0.0d;
        for (Model_Inventory_Transfer_Detail loDetail : laTransactionDetail) {
            lnDetailTotal = lnDetailTotal + (loDetail.getInventoryCost() * loDetail.getQuantity());

        }
        double lnTotalAmount = lnDetailTotal + lnDiscountedFreight;

        poAppController.getMaster().setTransactionTotal(lnTotalAmount);
        tfTotal.setText(CommonUtils.NumberFormat(poAppController.getMaster().getTransactionTotal(), "###,###,##0.00"));
    }

}
