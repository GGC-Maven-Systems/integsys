/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelVehicleFinancingPromo_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleFinancingPrice;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;

/**
 * FXML Controller class
 *
 * @author User
 */
public class VehicleFinancingPromo_EntryController implements Initializable, ScreenInterface {
    
    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    boolean lsIsSaved = false;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static VehicleFinancingPrice poController;
    public int pnEditMode;
    
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    boolean lbresetpredicate = false;
    boolean pbEntered = false;
    boolean pbKeyPressed = false;
    
    private ObservableList<ModelVehicleFinancingPromo_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDeliveryAcceptance_Main> filteredData;
    private FilteredList<ModelVehicleFinancingPromo_Detail> filteredDataDetail;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    
    JFXUtil.ReloadableTableTask loadTableDetail;
    JFXUtil.StageManager stageSerialDialog = new JFXUtil.StageManager();
    ObservableList<String> comboboxlist = FXCollections.observableArrayList();
    
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail, apDetail11;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnApprove, btnVoid, btnHistory, btnExport, btnClose;
    @FXML
    private TextField tfValidityID, tfValidityPeriod, tfFinancingID, tfDescription, tfReservationAmount, tfSRP;
    @FXML
    private DatePicker dpValidFrom, dpTo;
    @FXML
    private CheckBox cbActive;
    @FXML
    private ComboBox cmbDownPaymentRate;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblNo, tblBrand, tblModel, tblVariant, tblColor, tblSRP, tblDPRate, tblReservationAmount, tblMonthlyAmortization, tblsamp;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehicleFinancingPrice();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initTextFields();
            initDatepickers();
            initDetailsGrid();
            initTableOnClick();
            clearTextFields();
            loadRecordDetail();
            loadTableDetail.reload();
            pnEditMode = poController.getEditMode();
            initComboboxes();
            initButton(pnEditMode);
            Platform.runLater(() -> {
//            psIndustryId = "";
//            poController.setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
//            poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
//            poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
//            poController.setPurpose(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT);
                loadRecordSearch();
                btnNew.fire();
            });
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }
    
    @Override
    public void setIndustryID(String fsValue) {
        psIndustryId = fsValue;
    }
    
    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }
    
    @Override
    public void setCategoryID(String fsValue) {
        psCategoryId = fsValue;
    }
    
    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        
        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            stageSerialDialog.closeDialog();
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        //Clear data
//                        poController.resetMaster();
//                        poController.resetOthers();
                        poController.Detail().clear();
                        clearTextFields();
                        
                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poController.populateVehicleList();
                        poController.initFields();
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        //Populate purhcase receiving serials
//                        for (int lnCtr = 0; lnCtr <= poController.getDetailCount() - 1; lnCtr++) {
//                            poController.getPurchaseOrderReceivingSerial(poController.Detail(lnCtr).getEntryNo());
//                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
//                            psSupplierId = poController.Master().getSupplierId();

                            //Clear data
//                            poController.resetMaster();
//                            poController.resetOthers();
                            poController.Detail().clear();
                            clearTextFields();

//                            poController.Master().setIndustryId(psIndustryId);
                            poController.Master().setCompanyId(psCompanyId);
//                            poController.Master().setSupplierId(psSupplierId);
                            pnEditMode = EditMode.UNKNOWN;
                            
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                            ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                            return;
                        }
                        
                        try {
                            poController.ShowStatusHistory();
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.AddDetail();
                                loadTableDetail.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
//                                psSupplierId = poController.Master().getSupplierId();

                                // Confirmation Prompt
//                                JSONObject loJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
//                                if ("success".equals(loJSON.get("result"))) {
//                                    if (poController.Master().getTransactionStatus().equals(PurchaseOrderReceivingStatus.OPEN)) {
//                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
//                                            loJSON = poController.ConfirmTransaction("");
//                                            if ("success".equals((String) loJSON.get("result"))) {
//                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
//                                            } else {
//                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
//                                            }
//                                        }
//                                    }
//                                }
                                // Print Transaction Prompt
                                lsIsSaved = false;
//                                loJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
//                                poController.loadAttachments();
                                loadRecordDetail();
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnBrowse":
//                        poController.Master().getModel().setTransactionStatus(APPaymentAdjustmentStatus.RETURNED + "" + APPaymentAdjustmentStatus.OPEN);
                        poController.Master().setRecordStatus("01234");
                        poJSON = poController.SearchTransaction("", false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfValidityID.requestFocus();
                            return;
                        }
                        pnEditMode = poController.Master().getEditMode();
                        break;
                    case "btnApprove":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to approve transaction?") == true) {
                            poJSON = poController.ApproveTransaction();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to void transaction?") == true) {
                            poJSON = poController.VoidTransaction();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnExport":
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisapprove", "btnVoid")) { //|| lsButton.equals("btnCancel")
                } else {
                    loadRecordDetail();
                    loadTableDetail.reload();
                }
                initButton(pnEditMode);
                if (lsButton.equals("btnUpdate")) {
                    moveNext(false, false);
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (ParseException ex) {
            Logger.getLogger(VehicleFinancingPromo_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void moveNext(boolean isUp, boolean continueNext) {
//        if (details_data.size() <= 0) {
//            return;
//        }
//
//        if (continueNext) {
//            apDetail.requestFocus();
//
//            pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewDetail) : JFXUtil.moveToNextRow(tblViewDetail);
//        }
//        loadRecordDetail();
//        tfReceiveQuantity.requestFocus();
    }
    
    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();
        int lnRow = pnDetail;
        TableView<?> currentTable = tblViewDetail;
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
        
        switch (event.getCode()) {
            case TAB:
            case ENTER:
                pbEntered = true;
                CommonUtils.SetNextFocus(txtField);
                event.consume();
                break;
            case F3:
                switch (lsID) {
                }
                break;
            case UP:
                switch (lsID) {
                    case "tfBarcode":
                    case "tfReceiveQuantity":
                        moveNext(true, true);
                        event.consume();
                        break;
                }
                break;
            case DOWN:
                switch (lsID) {
                    case "tfBarcode":
                    case "tfReceiveQuantity":
                        moveNext(false, true);
                        event.consume();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
            switch (currentTableID) {
                case "tblViewDetail":
                    if (!details_data.isEmpty()) {
                        pnDetail = newIndex;
                        loadRecordDetail();
                    }
                    break;
            }
        }
    };
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfValidityPeriod":
                        poJSON = poController.Master().setValidityDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                }
            });
    
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDescription":
                        poJSON = poController.Master().setValidityDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfReservationAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setReservationAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableDetail.reload();
                });
            });
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbDownPaymentRate":
                        break;
                }
            });
    
    private void loadRecordMaster() {
        tfValidityID.setText(poController.Master().getValidityId());
        dpValidFrom.setValue(poController.Master().getFromDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getFromDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
        tfValidityPeriod.setText(poController.Master().getValidityDescription());
        dpTo.setValue(poController.Master().getThruDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getThruDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
    }
    
    private void loadRecordDetail() {
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        tfFinancingID.setText(poController.Detail(pnDetail).getValidityId());
//        tfDescription.setText(poController.Detail(pnDetail).getDescription());
        cbActive.setSelected(poController.Detail(pnDetail).getRecordStatus());
        tfReservationAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getReservationAmount().doubleValue(), false));
        tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), false));
//        JFXUtil.setCmbValue(cmbDownPaymentRate, !poController.Detail(pnDetail).getDownPaymentRate().equals("") ? Integer.valueOf(poController.Detail(pnDetail).getDownPaymentRate()) : -1);
    }
// CheckBox handler

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbActive": // this is the id
                    break;
            }
        }
    }
    
    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                try {
                    poJSON = new JSONObject();
                    JFXUtil.setJSONSuccess(poJSON, "success");
                    LocalDate dateNow = LocalDate.now();
                    String inputText = datePicker.getEditor().getText();
                    if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                        return;
                    }
                    switch (datePicker.getId()) {
                        case "dpValidFrom":
                            LocalDate selectedFromDate = dpValidFrom.getValue();
                            LocalDate toDate = dpTo.getValue();
                            if (toDate != null && selectedFromDate.isAfter(toDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                                String lsDateFrom = CustomCommonUtil.formatDateToShortString(JFXUtil.getFirstDayOfMonth(oApp.getServerDate()));
                                dpValidFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
                                return;
                            }
                            if (pbSuccess) {
                                poController.Master().setFromDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false;
                            loadRecordMaster();
                            pbSuccess = true;
                            break;
                        case "dpTo":
                            LocalDate selectedToDate = dpTo.getValue();
                            LocalDate fromDate = dpValidFrom.getValue();
                            if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                                dpTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
                                return;
                            }
                            if (pbSuccess) {
                                poController.Master().setThruDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false;
                            loadRecordMaster();
                            pbSuccess = true;
                            break;
                        default:
                            break;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });
    
    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpValidFrom, dpTo);
        JFXUtil.setActionListener(datepicker_Action, dpValidFrom, dpTo);
    }
    
    private void addChildColumns(
            TableView<?> tableView,
            TableColumn parentColumn,
            JSONArray jsonArray) {
        
        for (int lnRow = 0; lnRow < jsonArray.size(); lnRow++) {
            
            JSONObject loJSONObject = (JSONObject) jsonArray.get(lnRow);
            
            int lnDuration = ((Number) loJSONObject.get("nDuration")).intValue();
            
            TableColumn column = new TableColumn();
            
            column.setId("tbl_" + String.valueOf(lnDuration));
            column.setText(String.valueOf(lnDuration));
            JFXUtil.setColumnRight(column);
            
            column.setMinWidth(100);
            column.setPrefWidth(100);
            column.setMaxWidth(100);
            
            parentColumn.getColumns().add(column);
        }
        tblsamp.setVisible(false);
    }

    //create a dynamic loader of table column of tblViewDetail
    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlist, cmbDownPaymentRate));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbDownPaymentRate);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbDownPaymentRate);
    }
    
    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfValidityPeriod);
        JFXUtil.setFocusListener(txtDetail_Focus, tfDescription, tfReservationAmount);
        
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail);
        JFXUtil.setCommaFormatter(tfReservationAmount);
        
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);
        
        JFXUtil.adjustColumnForScrollbar(tblViewDetail);
    }
    
    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    try {
                        pbEntered = false;
                        // Setting data to table detail
                        poController.loadStandardInterestRates();
                        poController.loadStandardDownpaymentRates();
                        Platform.runLater(() -> {
                            int lnCtr;
                            details_data.clear();
                            plOrderNoPartial.clear();
                            try {
                                poController.populateVehicleList();
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    reInitializeColumns();
                                    reInitializeComboboxes();
                                    poController.ReloadDetail();
                                }
                                
                                double lnTotal = 0.0;
                                for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                    details_data.add(
                                            new ModelVehicleFinancingPromo_Detail(String.valueOf(lnCtr + 1),
                                                    String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().Brand().getDescription()),
                                                    String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().getDescription()),
                                                    String.valueOf(poController.Detail(lnCtr).ModelVariant().getDescription()),
                                                    String.valueOf(poController.Detail(lnCtr).ModelVariant().Color().getDescription()),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getSRPAmount(), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDownPaymentRate(), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getReservationAmount(), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(0, false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(0, false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(0, false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(0, false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(0, false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(0, false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(0, false))
                                            ));
                                    
                                }
//                                JSONArray loJSONArray = poController.loadStandardInterestRates();
//                                for (int lnRow = 0; lnRow < loJSONArray.size(); lnRow++) {
//                                    JSONObject loJSONObject = (JSONObject) loJSONArray.get(lnRow);
//                                    int lnDuration = (int) loJSONObject.get("nDuration");
//                                    Double ldblRate = (Double) loJSONObject.get("nRateValx");
//                                    System.out.println("Duration : " + lnDuration);
//                                    System.out.println("Rate : " + ldblRate);
//                                    System.out.println("Montly Amortization Amount : " + poController.getMontlyAmortizationAmount(lnCtr, lnDuration, ldblRate)); // returns amount cash, must define index in count
//                                }
                                if (pnDetail < 0 || pnDetail
                                        >= details_data.size()) {
                                    if (!details_data.isEmpty()) {
                                        /* FOCUS ON FIRST ROW */
                                        tblViewDetail.getSelectionModel().select(0);
                                        tblViewDetail.getFocusModel().focus(0);
                                        pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                                        loadRecordDetail();
                                    }
                                } else {
                                    /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                    tblViewDetail.getSelectionModel().select(pnDetail);
                                    tblViewDetail.getFocusModel().focus(pnDetail);
                                    loadRecordDetail();
                                }
                                loadRecordMaster();
                            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        }
                        );
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                }
        );
    }
    
    private void reInitializeColumns() {
        try {
            JSONArray loJSONArray = poController.loadStandardInterestRates();
            addChildColumns(tblViewDetail, tblMonthlyAmortization, loJSONArray);
            tblViewDetail.getColumns();
            JFXUtil.setColumnCenter(tblNo);
            JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor, tblSRP);
            JFXUtil.setColumnRight(tblDPRate, tblReservationAmount);
            JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
            tblViewDetail.setItems(details_data);
            //include reinitialize of comboboxes
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehicleFinancingPromo_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void reInitializeComboboxes() {
        try {
            ArrayList<Double> laStandardDownpaymentRate = poController.loadStandardDownpaymentRates();
            
            for (int lnCtr2 = 0; lnCtr2 < laStandardDownpaymentRate.size(); lnCtr2++) {
                comboboxlist.add(String.valueOf(laStandardDownpaymentRate.get(lnCtr2)));
            }
            cmbDownPaymentRate.setItems(comboboxlist);
            if (!comboboxlist.isEmpty()) {
                cmbDownPaymentRate.getSelectionModel().selectFirst();
            }
            JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbDownPaymentRate);
            JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbDownPaymentRate);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehicleFinancingPromo_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblNo);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor, tblSRP);
        JFXUtil.setColumnRight(tblDPRate, tblReservationAmount);
    }
    
    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail);
        loadRecordDetail();
        loadTableDetail.reload();
    }
    
    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    public void initTableOnClick() {
        tblViewDetail.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleFinancingPromo_Detail selected = (ModelVehicleFinancingPromo_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        stageSerialDialog.closeDialog();
                        pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                        loadRecordDetail();
                        moveNext(false, false);
                    }
                }
            }
        });
    }
    
    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        
        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);
        
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail);
        JFXUtil.setButtonsVisibility(true, btnApprove);
        JFXUtil.setButtonsVisibility(true, btnExport);
        
        if (fnValue != EditMode.READY) {
            return;
        }
//        switch (poController.Master().getTransactionStatus()) {
//            case ControllerStatus.VOID:
//            case ControllerStatus.CANCELLED:
//                JFXUtil.setButtonsVisibility(false, btnUpdate);
//                break;
//        }
    }
}
