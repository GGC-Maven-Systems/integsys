/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleAddOn;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;
import ph.com.guanzongroup.integsys.model.ModelVehicleFinancingPromo_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class VehicleAddOnsPromo_EntryController  implements Initializable, ScreenInterface {
    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static VehicleAddOn poController;
    public int pnEditMode;

    private String psVariantId = "";
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    boolean lbresetpredicate = false;
    boolean pbEntered = false;
    boolean pbKeyPressed = false;
    LocalDate ValidFrom;
    
    private ObservableList<ModelVehicleFinancingPromo_Detail> list_data = FXCollections.observableArrayList(); //ViewList
    private ObservableList<ModelVehicleFinancingPromo_Detail> details_data = FXCollections.observableArrayList(); //Details
    private FilteredList<ModelVehicleFinancingPromo_Detail> filteredDataDetail;
    private FilteredList<ModelVehicleFinancingPromo_Detail> filteredDataList;
    ArrayList<ArrayList<ArrayList<String>>> array = new ArrayList<ArrayList<ArrayList<String>>>();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableDetail, loadTableList;
    
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApprove, btnVoid, btnHistory, btnPrint, btnClose;
    @FXML
    private TextField tfValidityID, tfValidityPeriod, tfDescription, tfAmount,tfAddOnsID, tfSRP, tfBrand, tfModel, tfVariant, tfColor;
    @FXML
    private DatePicker dpValidFrom, dpTo;
    @FXML
    private CheckBox cbActive, cbApplyToAll;
    @FXML
    private TableView tblViewDetail,tblViewList;
    @FXML
    private TableColumn tblNo, tblBrand, tblModel, tblVariant, tblColor, tblSRP, tblsamp,tblDescription, tblAmount;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehicleAddOn();
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
            poController.setWithUI(true);
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
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        //Clear data
                        poController.Detail().clear();
                        clearTextFields();

                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poJSON = poController.populateVehicleList();
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
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
                        ValidFrom = CustomCommonUtil.parseDateStringToLocalDate(CustomCommonUtil.formatDateToShortString(poController.Master().getFromDate()));
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            poController.InitTransaction();
                            clearTextFields();
                            poController.Master().setCompanyId(psCompanyId);
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
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnBrowse":
                        poController.Master().setRecordStatus("01234");
                        poJSON = poController.SearchTransaction("", true);
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

                            poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();

                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        String lsStat = "void";
                        if (ValidityPeriodStatus.APPROVED.equals(poController.Master().getRecordStatus())) {
                            lsStat = "cancel";
                        }
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsStat + " transaction?") == true) {
                            if (ValidityPeriodStatus.OPEN.equals(poController.Master().getRecordStatus())) {
                                poJSON = poController.VoidTransaction();
                            } else {
                                poJSON = poController.CancelTransaction();
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnPrint":
                        poController.printTransaction();
                        return;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadRecordDetail();
                loadTableDetail.reload();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
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
                if (tfAmount.isFocused()) {
                    pbEntered = true;
                }
                CommonUtils.SetNextFocus(txtField);
                event.consume();
                break;
            case F3:
                switch (lsID) {
                }
                break;
            case UP:
                JFXUtil.altSwitch(lsID, new Object[][]{
                    {new String[]{"tfAmount"}, (Runnable) () -> moveNext(true, true)},});
                break;
            case DOWN:
                JFXUtil.altSwitch(lsID, new Object[][]{
                    {new String[]{"tfAmount"}, (Runnable) () -> moveNext(false, true)},});
                break;
            default:
                break;
        }
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewList":
                    newIndex = !isMovedDown ? Integer.parseInt(filteredDataList.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex09())
                            : Integer.parseInt(filteredDataList.get(JFXUtil.moveToNextRow(currentTable)).getIndex09());
                    if (!list_data.isEmpty()) {
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
                if(lsValue == null){
                    lsValue = "";
                }
                switch (lsID) {
                    case "tfAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEntered) {
                            moveNext(false, true);
                            pbEntered = false;
                        }
                        break;
                    case "tfDescription":
                        poJSON = poController.Detail(pnDetail).setAddOnType(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEntered) {
                            moveNext(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableDetail.reload();
                });
            });

    private void loadRecordMaster() {
        Platform.runLater(() -> {
            lblStatus.setText(poController.getStatus(poController.Master().getRecordStatus()).toUpperCase());

            String lsStat = "Void";
            if (ValidityPeriodStatus.APPROVED.equals(poController.Master().getRecordStatus())) {
                lsStat = "Cancel";
            }
            btnVoid.setText(lsStat);
        });

        tfValidityID.setText(poController.Master().getValidityId());
        dpValidFrom.setValue(poController.Master().getFromDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getFromDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
        tfValidityPeriod.setText(poController.Master().getValidityDescription());
        dpTo.setValue(poController.Master().getThruDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getThruDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
    }

    private void loadRecordDetail() {
        try {
            JFXUtil.setDisabled(true, tfSRP, tfBrand, tfModel, tfVariant, tfColor, cbActive, cbApplyToAll, tfDescription, tfAmount);
            JFXUtil.clearNodes(tfSRP, tfBrand, tfModel, tfVariant, tfColor, cbActive, cbApplyToAll, tfDescription, tfAmount);
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            boolean lbShow1 = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
            JFXUtil.setDisabled(!lbShow1, cbActive, cbApplyToAll);
            if (pnEditMode == EditMode.READY) {
                JFXUtil.setDisabled(false, cbApplyToAll);
            } else {
                if (lbShow1) {
                    JFXUtil.setDisabled(JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getRecordStatus() ? "1" : "0", RecordStatus.INACTIVE), tfAmount,tfDescription);
                }
            }

            tfBrand.setText(poController.Detail(pnDetail).ModelVariant().Model().Brand().getDescription());
            tfModel.setText(poController.Detail(pnDetail).ModelVariant().Model().getDescription());
            tfVariant.setText(poController.Detail(pnDetail).ModelVariant().getDescription());
            tfColor.setText(poController.Detail(pnDetail).ModelVariant().Color().getDescription());
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), false));
            
            tfAddOnsID.setText(poController.Detail(pnDetail).getAddOnId());
            tfDescription.setText(poController.Detail(pnDetail).getAddOnType());
            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount().doubleValue(), false));
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount().doubleValue(), false));
            
            cbActive.setSelected(poController.Detail(pnDetail).getRecordStatus());
            cbApplyToAll.setSelected(poController.Detail(pnDetail).getRecordStatus());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbActive": // this is the id
                    poJSON = poController.Detail(pnDetail).setRecordStatus(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableDetail.reload();
                    break;
                case "cbApplyToAll": // this is the id
                    if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE ){
                        try {
                            poController.populateDetail(poController.Detail(pnDetail).getAddOnType(), checkedBox.isSelected(), poController.Detail(pnDetail).getAmount());
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    }
                    
                    loadTableDetail.reload();
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
                            LocalDate currentDate = oApp.getServerDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            LocalDate firstDayOfCurrentMonth = currentDate.withDayOfMonth(1);
                            if (selectedFromDate != null && selectedFromDate.isBefore(firstDayOfCurrentMonth)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be before the current month.");
                                dpValidFrom.setValue(firstDayOfCurrentMonth);
                                return;
                            }
                            if (toDate != null && selectedFromDate.isAfter(toDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                                loadRecordMaster();
                                return;
                            }
                            if (pbSuccess) {
                                poController.Master().setFromDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            poJSON = poController.populateVehicleList();
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            pbSuccess = false;
                            loadTableDetail.reload();
                            pbSuccess = true;
                            break;
                        case "dpTo":
                            LocalDate selectedToDate = dpTo.getValue();
                            LocalDate fromDate = dpValidFrom.getValue();
                            if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                                loadRecordMaster();
                                return;
                            }
                            if (pbSuccess) {
                                poController.Master().setThruDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            poJSON = poController.populateVehicleList();
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            pbSuccess = false;
                            loadTableDetail.reload();
                            pbSuccess = true;
                            break;
                        default:
                            break;
                    }
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpValidFrom, dpTo);
        JFXUtil.setActionListener(datepicker_Action, dpValidFrom, dpTo);

        dpTo.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Lost focus
                if (JFXUtil.isObjectEqualTo(dpTo.getEditor().getText(), null, "")) {
                    try {
                        if (pbSuccess) {
                            poJSON = poController.Master().setThruDate(null);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            dpTo.setValue(null);
                        }
                        poJSON = poController.populateVehicleList();
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        pbSuccess = false;
                        loadTableDetail.reload();
                        pbSuccess = true;
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                }
            }
        });
    }

    public void moveNext(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDetail.requestFocus();
            pnDetail = isUp ? Integer.parseInt(filteredDataList.get(JFXUtil.moveToPreviousRow(tblViewList)).getIndex09())
                    : Integer.parseInt(filteredDataList.get(JFXUtil.moveToNextRow(tblViewList)).getIndex09());
        }
        loadRecordDetail();
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.Detail(pnDetail).getAmount(), tfAmount},}, tfAmount); // default
    }

    private void addChildColumns(TableView<?> tableView, ArrayList<String> jsonArray) {
        for (int lnRow = 0; lnRow < jsonArray.size(); lnRow++) {
            
            TableColumn column = new TableColumn();

            column.setId("tbl_" + String.valueOf(jsonArray.get(lnRow)));
            column.setText(String.valueOf(jsonArray.get(lnRow)));
            JFXUtil.setColumnRight(column);

            column.setMinWidth(100);
            column.setPrefWidth(100);
            column.setMaxWidth(100);

            tableView.getColumns().add(column);
        }
        tblsamp.setVisible(false);
    }

    //create a dynamic loader of table column of tblViewDetail
    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfValidityPeriod);
        JFXUtil.setFocusListener(txtDetail_Focus, tfDescription, tfAmount);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail);
        JFXUtil.setCommaFormatter(tfAmount, tfSRP);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail,tblViewList);
        JFXUtil.adjustColumnForScrollbar(tblViewDetail,tblViewList);
    }

    private ArrayList<String> getCell(int row, int column) {
        while (array.size() <= row) {
            array.add(new ArrayList<ArrayList<String>>());
        }
        while (array.get(row).size() <= column) {
            array.get(row).add(new ArrayList<String>());
        }
        return array.get(row).get(column);
    }

    private String getCellData(int row, int column) {
        if (row >= array.size()) {
            return "";
        }
        if (column >= array.get(row).size()) {
            return "";
        }
        return String.join(", ", array.get(row).get(column));
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        plOrderNoPartial.clear();
                        try {
                            if (pnEditMode != EditMode.UNKNOWN) {
                                reInitializeColumns();
                            } 
                           if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            JFXUtil.disableAllHighlightByColor(tblViewDetail, "#FAA0A0", highlightedRowsMain);

                            array.clear();
                            int lnDynamicColumnCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getVariantDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.VariantDetail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                    continue;
                                }
                                details_data.add(
                                        new ModelVehicleFinancingPromo_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.VariantDetail(lnCtr).ModelVariant().Model().Brand().getDescription()),
                                                String.valueOf(poController.VariantDetail(lnCtr).ModelVariant().Model().getDescription()),
                                                String.valueOf(poController.VariantDetail(lnCtr).ModelVariant().getDescription()),
                                                String.valueOf(poController.VariantDetail(lnCtr).ModelVariant().Color().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.VariantDetail(lnCtr).getSRPAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.VariantDetail(lnCtr).getAmount(), false)),
                                                "",
                                                String.valueOf(lnCtr), //fixed column (this is not visible)
                                                "",//starts ammortization column dynamic
                                                "",
                                                "",
                                                "",
                                                "",
                                                ""
                                        ));
                                ArrayList<String> loArray = poController.loadUniqueAddOnType();
                                lnDynamicColumnCount = loArray.size();
                                for (int lnRow = 0; lnRow < loArray.size(); lnRow++) {
                                    String lsArray = loArray.get(lnRow); // this is the column title per array
                                    if(lsArray != null && !"".equals(lsArray)){
                                       getCell(lnCtr, lnRow).add(String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(poController.VariantDetail(lnRow).getVariantId(), lsArray), false)));
                                    }
                                    
                                }
                                //store agein as a prepared and then will dynamically added in details_data
                                if (!poController.VariantDetail(lnCtr).getRecordStatus()) {
                                    JFXUtil.highlightByKey(tblViewDetail, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsMain);
                                }
                            }
                            //then re-add in here
                            int lnCount = 7;
                            for (lnCtr = 0; lnCtr < poController.getVariantDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.VariantDetail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                    continue;
                                }
                                lnCount = 7;
                                //well need to define the number of loJSONArray
                                for (int lnMAcount = 0; lnMAcount < lnDynamicColumnCount; lnMAcount++) {
                                    lnCount += 1;
                                    details_data.get(lnCtr).setIndexDynamic(lnCount, getCellData(lnCtr, lnMAcount)); //11
                                }
                            }
//                            int lnTempRow = getDetailRowFilter(filteredDataDetail, pnDetail, 9);
//                            if (pnDetail < 0 || pnDetail
//                                    >= details_data.size()) {
//                                if (!details_data.isEmpty()) {
//                                    /* FOCUS ON FIRST ROW */
//                                    tblViewDetail.getSelectionModel().select(0);
//                                    tblViewDetail.getFocusModel().focus(0);
//                                    pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
//                                    loadRecordDetail();
//                                }
//                            } else {
//                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//                                tblViewDetail.getSelectionModel().select(lnTempRow);
//                                tblViewDetail.getFocusModel().focus(lnTempRow);
//                                loadRecordDetail();
//                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    }
                    );
                }
        );
        
        loadTableList = new JFXUtil.ReloadableTableTask(
                tblViewList,
                list_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        list_data.clear();
                        try {
                            if (pnEditMode != EditMode.UNKNOWN) {
                                reInitializeColumns();
                            }
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
//                            JFXUtil.disableAllHighlightByColor(tblViewDetail, "#FAA0A0", highlightedRowsMain);
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                    continue;
                                }
                                if (!psVariantId.equals(poController.Detail(lnCtr).getVariantId())) {
                                    continue;
                                }
                                list_data.add(
                                        new ModelVehicleFinancingPromo_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.Detail(lnCtr).getAddOnType()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), false)),
                                                String.valueOf(lnCtr), //fixed column (this is not visible)
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",//starts ammortization column dynamic
                                                "",
                                                "",
                                                "",
                                                "",
                                                ""
                                        ));
                            }
                            int lnTempRow = getDetailRowFilter(filteredDataList, pnDetail, 4);
                            if (pnDetail < 0 || pnDetail
                                    >= list_data.size()) {
                                if (!list_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewList.getSelectionModel().select(0);
                                    tblViewList.getFocusModel().focus(0);
                                    pnDetail = tblViewList.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewList.getSelectionModel().select(lnTempRow);
                                tblViewList.getFocusModel().focus(lnTempRow);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    }
                    );
                }
        );
        
    }

    public static int getDetailRowFilter(ObservableList<?> dataList, int lnpn, int columnIndex) {
        try {
            String getterName = String.format("getIndex%02d", columnIndex);

            for (int lnCtr = 0; lnCtr < dataList.size(); lnCtr++) {
                Object item = dataList.get(lnCtr);

                String value = (String) item.getClass()
                        .getMethod(getterName)
                        .invoke(item);

                if (String.valueOf(lnpn).equals(value)) {
                    return lnCtr;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // not found
    }

    private void removeChildColumns(TableView<?> tableView, TableColumn<?, ?> parentColumn) {
        if (parentColumn == null) {
            return;
        }

        if (parentColumn.getColumns().size() > 1) {
            parentColumn.getColumns().remove(1, parentColumn.getColumns().size());
        }
        tableView.refresh();
    }

    private void reInitializeColumns() {
        //            removeChildColumns(tblViewDetail, tblMonthlyAmortization);
        ArrayList<String> loArray = poController.loadUniqueAddOnType();
        addChildColumns(tblViewDetail, loArray);
        Platform.runLater(() -> {
            disableTableColumnReordering(tblViewDetail);
            tblViewDetail.refresh();
        });
        JFXUtil.setColumnCenter(tblNo);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor, tblSRP);
        JFXUtil.setColumnRight(tblSRP, tblAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);
        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        SortedList<ModelVehicleFinancingPromo_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewDetail.comparatorProperty());
        tblViewDetail.setItems(sortedData);
        tblViewDetail.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblViewDetail.lookup("TableHeaderRow");
            if (header != null) {
                header.reorderingProperty().addListener((o, oldVal, newVal) -> {
                    header.setReordering(false);
                });
            }
        });
        
        //ViewList
        JFXUtil.setColumnLeft(tblDescription);
        JFXUtil.setColumnRight(tblAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewList);
        tblViewList.setItems(list_data);
        filteredDataList = new FilteredList<>(list_data, b -> true);
        SortedList<ModelVehicleFinancingPromo_Detail> sortedData1 = new SortedList<>(filteredDataList);
        sortedData1.comparatorProperty().bind(tblViewList.comparatorProperty());
        tblViewList.setItems(sortedData);
        tblViewList.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblViewList.lookup("TableHeaderRow");
            if (header != null) {
                header.reorderingProperty().addListener((o, oldVal, newVal) -> {
                    header.setReordering(false);
                });
            }
        });
        
    }

    public static void disableTableColumnReordering(final TableView<?> tableView) {
        Platform.runLater(() -> {
            TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");
            if (header == null) {
                return;
            }
            header.setReordering(false);
            if (!header.reorderingProperty().isBound()) {
                header.reorderingProperty().addListener(
                        (obs, oldValue, newValue) -> {
                            if (newValue) {
                                header.setReordering(false);
                            }
                        });
            }
        });
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblNo);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor);
        JFXUtil.setColumnRight(tblAmount, tblSRP);
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
                        int lnRow = Integer.parseInt(filteredDataDetail.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex09());
//                        pnDetail = lnRow;
                        if (lnRow < 0 || lnRow > poController.getVariantDetailCount()- 1) {
                            return;
                        }
                        psVariantId = poController.VariantDetail(lnRow).getVariantId();
                        loadTableList.reload();
//                        moveNext(false, false);
                    }
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewDetail, item -> ((ModelVehicleFinancingPromo_Detail) item).getIndex01(), highlightedRowsMain);
        
        tblViewList.setOnMouseClicked(event -> {
            if (list_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleFinancingPromo_Detail selected = (ModelVehicleFinancingPromo_Detail) tblViewList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(filteredDataList.get(tblViewList.getSelectionModel().getSelectedIndex()).getIndex04());
                        pnDetail = lnRow;
                        loadRecordDetail();
                        moveNext(false, false);
                    }
                }
            }
        });
//        JFXUtil.applyRowHighlighting(tblViewList, item -> ((ModelVehicleFinancingPromo_Detail) item).getIndex01(), highlightedRowsMain);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster);
        JFXUtil.setButtonsVisibility(lbShow2, btnApprove, btnPrint);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getRecordStatus()) {
            case ValidityPeriodStatus.OPEN:
                JFXUtil.setButtonsVisibility(false, btnPrint);
                break;
            case ValidityPeriodStatus.VOID:
            case ValidityPeriodStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove, btnVoid, btnPrint);
                break;
            case ValidityPeriodStatus.APPROVED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove);
                break;
        }
    }
}


