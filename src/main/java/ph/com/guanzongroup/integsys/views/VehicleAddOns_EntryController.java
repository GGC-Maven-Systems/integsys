/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelVehicleAddOns_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleAddOns;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehicleAddOns_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static VehicleAddOns poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    boolean lbresetpredicate = false;
    boolean pbEntered = false;
    boolean pbKeyPressed = false;

    private ObservableList<ModelVehicleAddOns_Detail> details_data = FXCollections.observableArrayList();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableDetail;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList();
    ArrayList<ArrayList<ArrayList<String>>> array = new ArrayList<ArrayList<ArrayList<String>>>();

    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail, apDetail11;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnApprove, btnVoid, btnHistory, btnClose;
    @FXML
    private TextField tfValidityID, tfAddOnType, tfAddOnID, tfDescription, tfSRP, tfAmount;
    @FXML
    private DatePicker dpValidFrom, dpTo;
    @FXML
    private CheckBox cbActive, cbActiveAll;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblNo1, tblNo, tblBrand, tblModel, tblVariant, tblColor, tblSRP, tblAmount, tblStatus;

    LocalDate ValidFrom;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehicleAddOns();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initDetailsGrid();
            initCheckboxes();
            initTextFields();
            initDatepickers();
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
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?") == true) {
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
                        poController.populateVehicleList();
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
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster);
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

//                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisapprove", "btnVoid")) {
//                } else {
//                    loadRecordDetail();
//                    loadTableDetail.reload();
//                }
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
        try {
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
                        case "tfAddOnType":
                            poJSON = poController.SearchAddOnType("", false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                return;
                            }
                            poController.populateVehicleList();
                            loadTableDetail.reload();
                            break;
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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(VehicleAddOns_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    newIndex = !isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex10())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex10());
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
                try {
                    switch (lsID) {
                        case "tfAddOnType":
                            if (lsValue.isEmpty()) {
                                poController.Master().setValidityDescription("");
                                poController.populateVehicleList();
                                loadTableDetail.reload();
                            }
                            break;
                    }
                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDescription":
                        break;
                    case "tfAmount":
                        if (isContainsChecked()) {
                            lsValue = JFXUtil.removeComma(lsValue);
                            processAction("tfAmount", CustomCommonUtil.setIntegerValueToDecimalFormat(Double.parseDouble(lsValue)));
                            return;
                        } else {
                            lsValue = JFXUtil.removeComma(lsValue);
                            poJSON = poController.Detail(pnDetail).setAmount(Double.parseDouble(lsValue));
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            poController.Detail(pnDetail).getAmount();
                            if (pbEntered) {
                                moveNext(false, true);
                                pbEntered = false;
                            }
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
        String lsValidFrom = JFXUtil.formatDateToString(poController.Master().getFromDate());
        JFXUtil.setDateValue(dpValidFrom, JFXUtil.isObjectEqualTo(lsValidFrom, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidFrom, "yyyy-MM-dd"));
        String lsTo = JFXUtil.formatDateToString(poController.Master().getThruDate());
        JFXUtil.setDateValue(dpTo, JFXUtil.isObjectEqualTo(lsTo, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsTo, "yyyy-MM-dd"));
        tfAddOnType.setText(poController.Master().getValidityDescription());
    }

    private void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            boolean lbisActive = poController.Detail(pnDetail).getRecordStatus();
            JFXUtil.setDisabled(!lbisActive, tfAmount);

            tfAddOnID.setText(poController.Detail(pnDetail).getAddOnId());
            tfDescription.setText(JFXUtil.concatStrings(poController.Detail(pnDetail).ModelVariant().Model().Brand().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().Model().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().Color().getDescription()));
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), false));
            cbActive.setSelected(poController.Detail(pnDetail).getRecordStatus());
            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount().doubleValue(), false));
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
                case "cbActiveAll": // this is the id
                    //set to 1 all of column 2 row data value to enable checked
                    for (int lnCtr = 0; lnCtr < checkedItem.size(); lnCtr++) {
                        if (checkedBox.isSelected()) {
                            checkedItem.set(lnCtr, "1");
                        } else {
                            checkedItem.set(lnCtr, "0");
                        }
                    }
                    loadTableDetail.reload();
                    break;
                case "cbActive": // this is the id
                    if (isContainsChecked()) {
                        processAction("cbActive", checkedBox.isSelected() ? "1" : "0");
                    } else {
                        poJSON = poController.Detail(pnDetail).setRecordStatus(checkedBox.isSelected());
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadTableDetail.reload();
                    }
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

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelVehicleAddOns_Detail.class, tblViewDetail, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 0:
                            checkedItem.set(rowIndex, lbisTrue ? "1" : "0");
                            //set external temporary data of index to save as reference
                            // if detected unchecked then must update
                            pnDetail = rowIndex;
                            Platform.runLater(() -> {
                                loadTableDetail.reload();
                                JFXUtil.runWithDelay(0.30, () -> {
                                    if (lbisTrue) {
                                        JFXUtil.selectAndFocusRow(tblViewDetail, rowIndex);
                                    }
                                });
                            });
                            break;
                    }
                }, 0);//starts 0,1,2 
    }

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
            pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex10())
                    : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex10());
        }
        loadRecordDetail();
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.Detail(pnDetail).getAmount(), tfAmount},}, tfAmount); // default
    }

    public boolean isContainsChecked() {
        return checkedItem != null && checkedItem.contains("1");
    }

    private void processAction(String identifier, String lsValue) {
        boolean lbcb = false;
        String lsMessage = "";
        lsMessage = "remove";
        switch (identifier) {
            case "cbActive":
                lbcb = lsValue.equals("1");
                lsMessage = "activate/deactivate";
                break;
            case "tfAmount":
                lsMessage = "change amount of the";
                break;
        }

        if (checkedItem.stream().anyMatch("1"::equals)) {
        } else {
            ShowMessageFX.Warning(null, pxeModuleName, "No items were selected to " + lsMessage + ".");
            return;
        }
        if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + lsMessage + " selected item/s?")) {
            return;
        }
        String firstValue = null;
        boolean isConsistent = true;
        checkedItems.clear();
        List<Integer> listCount = new ArrayList<>();
        for (Object item : tblViewDetail.getItems()) {
            ModelVehicleAddOns_Detail item1 = (ModelVehicleAddOns_Detail) item;
            String lschecked = item1.getIndex01();
            int lnCtr = Integer.valueOf(item1.getIndex10());
            if (lschecked.equals("1")) {
                listCount.add(Integer.parseInt(item1.getIndex10()));
                String currentValue = item1.getIndex09();
                // checkedItems.add();
                switch (identifier) {
                    case "cbActive":
                        if (firstValue == null) {
                            // First value found
                            firstValue = currentValue;
                        } else {
                            // Compare with the first value
                            if (!Objects.equals(firstValue, currentValue)) {
                                isConsistent = false;
                                ShowMessageFX.Warning(null, pxeModuleName, "Unable to simultaneously update different statuses");
                                return;
                            }
                        }
                        break;
                    case "tfAmount":
                        if (firstValue == null) {
                            // First value found
                            firstValue = currentValue;
                        } else {
                            // Compare with the first value
                            if (!Objects.equals(firstValue, currentValue)) {
                                isConsistent = false;
                                ShowMessageFX.Warning(null, pxeModuleName, "Unable to simultaneously update with an inactive status included");
                                return;
                            }
                        }
                        break;
                }
            }
        }
        if (checkedItem.isEmpty()) {
            return;
        }

        switch (identifier) {
            case "cbActive":
                if (isConsistent) {
                    for (int i = 0; i < listCount.size(); i++) {
                        int number = listCount.get(i);
                        poController.Detail(number).setRecordStatus(lbcb);
                    }

                }
            case "tfAmount":
                if (isConsistent) {
                    for (int i = 0; i < listCount.size(); i++) {
                        int number = listCount.get(i);
                        lsValue = JFXUtil.removeComma(lsValue);
                        poController.Detail(number).setAmount(Double.parseDouble(lsValue));
                    }
                }
                break;
        }

        resetCheckboxSelection();
        pnEditMode = poController.getEditMode();
        loadTableDetail.reload();
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, apMaster);
        JFXUtil.setFocusListener(txtDetail_Focus, apDetail);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail);
        JFXUtil.setCommaFormatter(tfAmount);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);
        JFXUtil.adjustColumnForScrollbar(tblViewDetail);
    }

    private String getData(String lsValue) {
        return JFXUtil.isObjectEqualTo(lsValue, null, "") ? "" : lsValue;
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    pbEntered = false;
                    // Setting data to table detail
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEntered = false;
                        details_data.clear();
                        try {
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if(JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getVariantId(), null, "")){
                                    continue;
                                }
                                checkedItems(lnCtr);
                                details_data.add(
                                        new ModelVehicleAddOns_Detail(checkedItem.get(lnCtr),
                                                String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().Brand().getDescription())),
                                                getData(String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().getDescription())),
                                                getData(String.valueOf(poController.Detail(lnCtr).ModelVariant().getDescription())),
                                                getData(String.valueOf(poController.Detail(lnCtr).ModelVariant().Color().getDescription())),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getSRPAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), false)),
                                                poController.Detail(lnCtr).getRecordStatus() ? "Active" : "Inactive",
                                                String.valueOf(lnCtr)
                                        ));
                            }

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
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblNo1, tblNo, tblStatus);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor);
        JFXUtil.setColumnRight(tblAmount, tblSRP);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);
    }

    public void clearTextFields() {
        resetCheckboxSelection();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail);
    }

    private void resetCheckboxSelection() {
        cbActiveAll.setSelected(false);
        if (!checkedItem.isEmpty()) {
            checkedItem.clear();
        }
    }

    private void checkedItems(int lnCtr) {
        try {
            if (checkedItem.get(lnCtr) == null) {
                checkedItem.add("0");
            }
        } catch (Exception e) {
            checkedItem.add("0");
        }
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
                    ModelVehicleAddOns_Detail selected = (ModelVehicleAddOns_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex10());
                        pnDetail = lnRow;
                        loadRecordDetail();
//                        moveNext(false, false);
                    }
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewDetail, item -> ((ModelVehicleAddOns_Detail) item).getIndex01(), highlightedRowsMain);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSave, btnCancel, btnSearch);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail, cbActiveAll);
        JFXUtil.setButtonsVisibility(lbShow2, btnApprove);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getRecordStatus()) {
            case ValidityPeriodStatus.OPEN:
                break;
            case ValidityPeriodStatus.VOID:
            case ValidityPeriodStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove, btnVoid);
                break;
            case ValidityPeriodStatus.APPROVED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove);
                break;
        }
    }
}
