/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import ph.com.guanzongroup.integsys.model.ModelVehicleFinancingPromo_Detail;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleFinancingPrice;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehicleFinancingPromo_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
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
    private FilteredList<ModelVehicleFinancingPromo_Detail> filteredDataDetail;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableDetail;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList();
    ArrayList<ArrayList<ArrayList<String>>> array = new ArrayList<ArrayList<ArrayList<String>>>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail, apDetail11;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApprove, btnVoid, btnHistory, btnPrint, btnClose;
    @FXML
    private TextField tfValidityID, tfValidityPeriod, tfFinancingID, tfDescription, tfReservationAmount, tfSRP, tfDownPaymentRate;
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
    LocalDate ValidFrom;

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
            poController.setWithUI(true);
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
                        poController.printTransaction(cmbDownPaymentRate.getSelectionModel().getSelectedItem().toString());
                        return;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

//                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisapprove", "btnVoid")) {
//                } else {
//                    loadRecordDetail();
//                    loadTableDetail.reload();
//                }
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
                if (tfReservationAmount.isFocused()) {
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
                    {new String[]{"tfReservationAmount"}, (Runnable) () -> moveNext(true, true)},});
                break;
            case DOWN:
                JFXUtil.altSwitch(lsID, new Object[][]{
                    {new String[]{"tfReservationAmount"}, (Runnable) () -> moveNext(false, true)},});
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
                case "tblViewDetail":
                    newIndex = !isMovedDown ? Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex09())
                            : Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToNextRow(currentTable)).getIndex09());
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
                    case "tfReservationAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setReservationAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.Detail(pnDetail).getReservationAmount();
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
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbDownPaymentRate":
                        //there should be method
                        //trigger filtering in table

                        filteredDataDetail.setPredicate(orders -> {
                            String lsval = selectedValue.toString();
                            if (lsval == null
                                    || lsval.isEmpty()
                                    || lsval.equalsIgnoreCase("--All--")) {
                                return true;
                            }
                            return lsval.equalsIgnoreCase(orders.getIndex07());
                        });
                        break;
                }
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
            JFXUtil.setDisabled(true, tfSRP, tfDownPaymentRate, cbActive, cmbDownPaymentRate, tfReservationAmount);
            JFXUtil.clearNodes(tfFinancingID, tfSRP, tfDownPaymentRate, cbActive, tfReservationAmount, tfDescription);
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            boolean lbShow1 = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
            boolean lbShow2 = pnEditMode == EditMode.UPDATE;
            JFXUtil.setDisabled(!lbShow1, cbActive, cmbDownPaymentRate);
            if (pnEditMode == EditMode.READY) {
                JFXUtil.setDisabled(false, cmbDownPaymentRate);
            } else {
                if (lbShow1) {
                    JFXUtil.setDisabled(JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getRecordStatus() ? "1" : "0", RecordStatus.INACTIVE), tfReservationAmount);
                }
            }

            tfFinancingID.setText(poController.Detail(pnDetail).getVehicleFinancingId());
            tfDescription.setText(JFXUtil.concatStrings(poController.Detail(pnDetail).ModelVariant().Model().Brand().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().Model().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().Color().getDescription()));
            cbActive.setSelected(poController.Detail(pnDetail).getRecordStatus());
            tfReservationAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getReservationAmount().doubleValue(), false));
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), false));
            tfDownPaymentRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDownPaymentRate().doubleValue(), false));
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
                            if (pnEditMode == EditMode.ADDNEW) {
                                String lsDateFrom = CustomCommonUtil.formatDateToShortString(JFXUtil.getFirstDayOfMonth(oApp.getServerDate()));
                                ValidFrom = CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd");
                            }
                            if (toDate != null && selectedFromDate.isBefore(ValidFrom)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, back date is not allowed.");
                                loadRecordMaster();
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
            pnDetail = isUp ? Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex09())
                    : Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex09());
        }
        loadRecordDetail();
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.Detail(pnDetail).getReservationAmount(), tfReservationAmount},}, tfReservationAmount); // default
    }

    private void addChildColumns(TableView<?> tableView, TableColumn parentColumn, JSONArray jsonArray) {
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
        JFXUtil.setCommaFormatter(tfReservationAmount, tfSRP, tfDownPaymentRate);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);
        JFXUtil.adjustColumnForScrollbar(tblViewDetail);
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
                                reInitializeComboboxes();
                            }
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            JFXUtil.disableAllHighlightByColor(tblViewDetail, "#FAA0A0", highlightedRowsMain);

                            array.clear();
                            int lnAmmortizationColumnCount = 0;
                            double lnTotal = 0.0;
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                    continue;
                                }
                                details_data.add(
                                        new ModelVehicleFinancingPromo_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().Brand().getDescription()),
                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().getDescription()),
                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().getDescription()),
                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().Color().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getSRPAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDownPaymentRate(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getReservationAmount(), false)),
                                                String.valueOf(lnCtr), //fixed column (this is not visible)
                                                "",//starts ammortization column dynamic
                                                "",
                                                "",
                                                "",
                                                "",
                                                ""
                                        ));
                                JSONArray loJSONArray = poController.loadStandardInterestRates();
                                lnAmmortizationColumnCount = loJSONArray.size();
                                for (int lnRow = 0; lnRow < loJSONArray.size(); lnRow++) { //sample is it have size of 3
                                    JSONObject loJSONObject = (JSONObject) loJSONArray.get(lnRow);
                                    int lnDuration = (int) loJSONObject.get("nDuration"); // this is the column title per array
                                    Double ldblRate = (Double) loJSONObject.get("nRateValx");
                                    getCell(lnCtr, lnRow).add(String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getMontlyAmortizationAmount(lnCtr, lnDuration, ldblRate), false)));
                                }
                                //store agein as a prepared and then will dynamically added in details_data
                                if (!poController.Detail(lnCtr).getRecordStatus()) {
                                    JFXUtil.highlightByKey(tblViewDetail, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsMain);
                                }
                            }
                            //then re-add in here
                            int lnCount = 9;
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                    continue;
                                }
                                lnCount = 9;
                                //well need to define the number of loJSONArray
                                for (int lnMAcount = 0; lnMAcount < lnAmmortizationColumnCount; lnMAcount++) {
                                    lnCount += 1;
                                    details_data.get(lnCtr).setIndexDynamic(lnCount, getCellData(lnCtr, lnMAcount)); //11
                                }
                            }
                            int lnTempRow = getDetailRowFilter(filteredDataDetail, pnDetail, 9);
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
                                tblViewDetail.getSelectionModel().select(lnTempRow);
                                tblViewDetail.getFocusModel().focus(lnTempRow);
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

    private boolean isCmbActive() {
        if (cmbDownPaymentRate.getSelectionModel().getSelectedItem() != null) {
            String lsSelected = cmbDownPaymentRate.getSelectionModel().getSelectedItem().toString();
            if (lsSelected != null && cmbDownPaymentRate.getItems().contains("--ALL--")) {
                return false;
            } else {
                return true;
            }
        } else {
        }
        return false;
    }

    private void reInitializeColumns() {
        try {
            removeChildColumns(tblViewDetail, tblMonthlyAmortization);
            JSONArray loJSONArray = poController.loadStandardInterestRates();
            addChildColumns(tblViewDetail, tblMonthlyAmortization, loJSONArray);
            Platform.runLater(() -> {
                disableTableColumnReordering(tblViewDetail);
                tblViewDetail.refresh();
            });

            JFXUtil.setColumnCenter(tblNo);
            JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor, tblSRP);
            JFXUtil.setColumnRight(tblDPRate, tblReservationAmount);
            JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
            tblViewDetail.setItems(details_data);

            if (isCmbActive()) {
            } else {
                filteredDataDetail = new FilteredList<>(details_data, b -> true);
            }

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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
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

    private void reInitializeComboboxes() {
        try {
            String lsSelected = "";
            if (cmbDownPaymentRate.getSelectionModel().getSelectedItem() != null) {
                lsSelected = cmbDownPaymentRate.getSelectionModel().getSelectedItem().toString();
            }

            ArrayList<Double> laStandardDownpaymentRate = poController.loadStandardDownpaymentRates();
            comboboxlist.clear();
            for (int lnCtr2 = 0; lnCtr2 < laStandardDownpaymentRate.size(); lnCtr2++) {
                comboboxlist.add(CustomCommonUtil.setIntegerValueToDecimalFormat(String.valueOf(laStandardDownpaymentRate.get(lnCtr2)), false));
            }
            comboboxlist.add("--All--");
            cmbDownPaymentRate.setItems(comboboxlist);
            if (!comboboxlist.isEmpty()) {
                cmbDownPaymentRate.getSelectionModel().selectLast();
            }

            if (isCmbActive() && !lsSelected.isEmpty()) {
                cmbDownPaymentRate.getSelectionModel().select(lsSelected);
            } else {
            }
            JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbDownPaymentRate);
            JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbDownPaymentRate);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblNo);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor);
        JFXUtil.setColumnRight(tblDPRate, tblReservationAmount, tblSRP);
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
                        pnDetail = lnRow;
                        loadRecordDetail();
                        moveNext(false, false);
                    }
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewDetail, item -> ((ModelVehicleFinancingPromo_Detail) item).getIndex01(), highlightedRowsMain);
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
