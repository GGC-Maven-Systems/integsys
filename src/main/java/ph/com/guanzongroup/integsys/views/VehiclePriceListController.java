/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelVehiclePriceList_Detail;
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
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehiclePriceList;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;
import ph.com.guanzongroup.integsys.model.ModelVehiclePriceHistory;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehiclePriceListController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnPriceHistory = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static VehiclePriceList poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    boolean lbresetpredicate = false;
    boolean pbEntered = false;
    boolean pbKeyPressed = false;

    private ObservableList<ModelVehiclePriceList_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelVehiclePriceHistory> pricehistory_data = FXCollections.observableArrayList();
    private FilteredList<ModelVehiclePriceList_Detail> filteredDataDetail;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableDetail, loadTablePriceHistory;
    ObservableList<String> comboboxliststatus = FXCollections.observableArrayList(
            ValidityPeriodStatus.OPEN,
            ValidityPeriodStatus.APPROVED,
            ValidityPeriodStatus.CANCELLED,
            ValidityPeriodStatus.VOID
    );
    ObservableList<String> pricelistyears = FXCollections.observableArrayList();
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail, apPriceHistory;
    @FXML
    private Label lblSource, lblStatus, lblTotalVariants, lblTotalColors;
    @FXML
    private ComboBox cmbSearchPriceListYear, cmbSearchStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnApprove, btnVoid, btnRemake, btnHistory, btnClose, btnCopyBasePriceToAllColors, btnAddVehicleDescription, btnExpandAll, btnCollapseAll;
    @FXML
    private TextField tfValidityID, tfBrand, tfModel, tfVariant, tfColor, tfYearModel, tfVehicleType, tfBodyType, tfTransmission, tfBaseSRP, tfVariantPriceHistory, tfColorPriceHistory;
    @FXML
    private DatePicker dpValidFrom, dpValidTo;
    @FXML
    private CheckBox cbActive;
    @FXML
    private TreeTableView tblViewDetail;
    @FXML
    private TreeTableColumn tblRowNo, tblBrand, tblModel, tblVariant, tblColor, tblTransmission, tblBaseSRP, tblDetailStatus;
    @FXML
    private TableView tblViewPriceHistory;
    @FXML
    private TableColumn tblPrice, tblEffectiveFrom, tblEffectiveTo, tblPriceStatus, tblUpdatedBy, tblDateUpdated;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehiclePriceList();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initTextFields();
            initDatepickers();
            initDetailsGrid();
            initPriceHistoryGrid();
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
//                        ValidFrom = CustomCommonUtil.parseDateStringToLocalDate(CustomCommonUtil.formatDateToShortString(poController.Master().getFromDate()));
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
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apDetail);
                        break;
                    case "btnRemake":
                        break;
                    case "btnCopyBasePriceToAllColors":
                        break;
                    case "btnAddVehicleDescription":
                        break;
                    case "btnExpandAll":
                        break;
                    case "btnCollapseAll":
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
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;
//        TableView<?> currentTable = tblViewDetail;
//        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;

                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfBrand":
                            poJSON = poController.SearchBrand(lsValue, pbEntered, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                        case "tfModel":
                            poJSON = poController.SearchModel(lsValue, pbEntered, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                        case "tfVariant":
                            poJSON = poController.SearchModelVariant(lsValue, pbEntered, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                    }
                    loadTableDetail.reload();
                    break;
                case UP:
//                    JFXUtil.altSwitch(lsID, new Object[][]{
//                        {new String[]{"tfReservationAmount"}, (Runnable) () -> moveNext(true, true)},});
                    break;
                case DOWN:
//                    JFXUtil.altSwitch(lsID, new Object[][]{
//                        {new String[]{"tfReservationAmount"}, (Runnable) () -> moveNext(false, true)},});
                    break;
                default:
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBrand":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setBrandId(null);
                        }
                        break;
                    case "tfModel":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setModelId(null);
                        }
                        break;
                    case "tfVariant":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setVariantId(null);
                        }
                        break;
                    case "tfBaseSRP":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setSRPAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableDetail.reload();
                });
            });
    ChangeListener<Boolean> txtPriceHistory_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfVariantPriceHistory":
                        break;
                    case "tfColorPriceHistory":
                        break;
                }
            });

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbActive":
                    poJSON = poController.Detail(pnDetail).setRecordStatus(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableDetail.reload();
                    break;
            }
        }
    }

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbSearchPriceListYear":
                        //reload for filter
                        break;
                    case "cmbSearchStatus":
                        //reload for filter
                        break;
                }
            });

    private void loadRecordBrowse() {
        cmbSearchPriceListYear.getSelectionModel().clearSelection();
        cmbSearchStatus.getSelectionModel().clearSelection();
    }

    private void loadRecordMaster() {
        Platform.runLater(() -> {
            lblStatus.setText(poController.getStatus(poController.Master().getRecordStatus()).toUpperCase());
        });
        tfValidityID.setText(poController.Master().getValidityId());
        String lsValidFrom = JFXUtil.formatDateToString(poController.Master().getFromDate());
        JFXUtil.setDateValue(dpValidFrom, JFXUtil.isObjectEqualTo(lsValidFrom, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidFrom, "yyyy-MM-dd"));
        String lsValidTo = JFXUtil.formatDateToString(poController.Master().getThruDate());
        JFXUtil.setDateValue(dpValidTo, JFXUtil.isObjectEqualTo(lsValidTo, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidTo, "yyyy-MM-dd"));
    }

    private void loadRecordDetail() {
        try {
            tfBrand.setText(poController.Detail(pnDetail).Brand().getDescription());
            tfModel.setText(poController.Detail(pnDetail).Model().getDescription());
            tfVariant.setText(poController.Detail(pnDetail).ModelVariant().getDescription());
            tfColor.setText(poController.Detail(pnDetail).ModelVariant().Color().getDescription());
            tfYearModel.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getPriceYear(), false));
            tfVehicleType.setText(poController.Detail(pnDetail).ModelVariantInsurance().getVehicleType());
            tfBodyType.setText(poController.Detail(pnDetail).ModelVariantInsurance().getBodyType());
            tfTransmission.setText(poController.Detail(pnDetail).ModelVariantInsurance().getTransmission());
            tfBaseSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), true));
            cbActive.setSelected(poController.Detail(pnDetail).getRecordStatus());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordPriceHistory() {
        tfVariantPriceHistory.setText("");
        tfColorPriceHistory.setText("");
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
                            LocalDate toDate = dpValidTo.getValue();
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

                            pbSuccess = false;
                            loadTableDetail.reload();
                            pbSuccess = true;
                            break;
                        case "dpValidTo":
                            LocalDate selectedToDate = dpValidTo.getValue();
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

                            pbSuccess = false;
                            loadTableDetail.reload();
                            pbSuccess = true;
                            break;
                        default:
                            break;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpValidFrom, dpValidTo);
        JFXUtil.setActionListener(datepicker_Action, dpValidFrom, dpValidTo);

        dpValidTo.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Lost focus
                if (JFXUtil.isObjectEqualTo(dpValidTo.getEditor().getText(), null, "")) {
                    if (pbSuccess) {
                        poJSON = poController.Master().setThruDate(null);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        dpValidTo.setValue(null);
                    }
                    pbSuccess = false;
                    loadTableDetail.reload();
                    pbSuccess = true;
                }
            }
        });
    }

    public void moveNext(boolean isUp, boolean continueNext) {
//        if (continueNext) {
//            apDetail.requestFocus();
//            pnDetail = isUp ? Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex09())
//                    : Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex09());
//        }
//        loadRecordDetail();
//        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
//            return;
//        }
//        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
//            {poController.Detail(pnDetail).getReservationAmount(), tfReservationAmount},}, tfReservationAmount); // default
    }

    //create a dynamic loader of table column of tblViewDetail
    private void initComboboxes() {
        ObservableList<String> pricelistyears = FXCollections.observableArrayList();
        for (int year = 2000; year <= 2027; year++) {
            pricelistyears.add(String.valueOf(year));
        }

        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(pricelistyears, cmbSearchPriceListYear), new JFXUtil.Pairs<>(comboboxliststatus, cmbSearchStatus));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbSearchPriceListYear, cmbSearchStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbSearchPriceListYear, cmbSearchStatus);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtDetail_Focus, apDetail);
        JFXUtil.setFocusListener(txtPriceHistory_Focus, apPriceHistory);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apDetail, apPriceHistory);
        JFXUtil.setCommaFormatter(tfVariantPriceHistory, tfColorPriceHistory, tfBaseSRP);

        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewPriceHistory);
        JFXUtil.adjustColumnForScrollbar(tblViewPriceHistory);
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
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }

                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                    continue;
                                }
                                details_data.add(
                                        new ModelVehiclePriceList_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.Detail(lnCtr).Brand().getDescription()),
                                                String.valueOf(poController.Detail(lnCtr).Model().getDescription()),
                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().getDescription()),
                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().Color().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).ModelVariantInsurance().getTransmission(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getSRPAmount(), false)),
                                                (poController.Detail(lnCtr).getRecordStatus() ? "Active" : "Inactive")
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
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    }
                    );
                }
        );
//        loadTablePriceHistory = new JFXUtil.ReloadableTableTask(
//                tblViewPriceHistory,
//                pricehistory_data,
//                () -> {
//                    pbEntered = false;
//                    Platform.runLater(() -> {
//                        int lnCtr;
//                        pricehistory_data.clear();
//                        plOrderNoPartial.clear();
//                        try {
//                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
//                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).ModelVariant().getDescription(), null, "")) {
//                                    continue;
//                                }
//                                pricehistory_data.add(
//                                        new ModelVehiclePriceHistory(String.valueOf(lnCtr + 1),
//                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().Brand().getDescription()),
//                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().getDescription()),
//                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().getDescription()),
//                                                String.valueOf(poController.Detail(lnCtr).ModelVariant().Color().getDescription()),
//                                                String.valueOf(lnCtr)
//                                        ));
//                            }
//
//                            if (pnPriceHistory < 0 || pnPriceHistory
//                                    >= pricehistory_data.size()) {
//                                if (!pricehistory_data.isEmpty()) {
//                                    /* FOCUS ON FIRST ROW */
//                                    tblViewPriceHistory.getSelectionModel().select(0);
//                                    tblViewPriceHistory.getFocusModel().focus(0);
//                                    pnPriceHistory = tblViewPriceHistory.getSelectionModel().getSelectedIndex();
//                                    loadRecordDetail();
//                                }
//                            } else {
//                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//                                tblViewPriceHistory.getSelectionModel().select(pnPriceHistory);
//                                tblViewPriceHistory.getFocusModel().focus(pnPriceHistory);
//                                loadRecordDetail();
//                            }
//                            loadRecordMaster();
//                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
//                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//                        }
//                    }
//                    );
//                }
//        );
    }

    private void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNo);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor, tblTransmission, tblBaseSRP, tblDetailStatus);
//        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
//Row is equivalent to tableview model
        TreeItem<ModelVehiclePriceList_Detail> root = new TreeItem<>(new ModelVehiclePriceList_Detail("", "", "", "", "", "", "", ""));
//
//        TreeItem<Row> parent1 = new TreeItem<>(new Row("Order #1001", "Parent", "300.00"));
//        parent1.getChildren().add(new TreeItem<>(new Row("Item A", "Child", "100.00")));
//        parent1.getChildren().add(new TreeItem<>(new Row("Item B", "Child", "200.00")));
//
//        TreeItem<Row> parent2 = new TreeItem<>(new Row("Order #1002", "Parent", "150.00"));
//        parent2.getChildren().add(new TreeItem<>(new Row("Item C", "Child", "150.00")));
//
//        root.getChildren().addAll(parent1, parent2);
//        parent1.setExpanded(true);   
//
//        tblViewDetail.setRoot(root);
//        tblViewDetail.setShowRoot(false);
    }

    public void initPriceHistoryGrid() {
        JFXUtil.setColumnCenter(tblUpdatedBy, tblDateUpdated);
        JFXUtil.setColumnLeft(tblEffectiveFrom, tblEffectiveTo);
        JFXUtil.setColumnRight(tblPrice, tblPriceStatus);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewPriceHistory);
        tblViewPriceHistory.setItems(pricehistory_data);
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
            if (event.getClickCount() == 1) {   // single click (use == 2 for double click)
                // The selected item is a TreeItem, not your model directly
                TreeItem<ModelVehiclePriceList_Detail> selectedItem = (TreeItem<ModelVehiclePriceList_Detail>) tblViewDetail.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Your model object is inside the TreeItem
                    ModelVehiclePriceList_Detail selected = selectedItem.getValue();
                    // Parent or child?
                    boolean isParent = selectedItem.getParent() == tblViewDetail.getRoot();
                    int lnRow = Integer.parseInt(selected.getIndex09());
                    if (isParent) {
                        // parent row clicked
                    } else {
                        // child row clicked
                        // TreeItem<ModelVehiclePriceList_Detail> parentItem = selectedItem.getParent();
                        // ModelVehiclePriceList_Detail parentData = parentItem.getValue();
                    }
                }
            }
        });
        tblViewPriceHistory.setOnMouseClicked(event -> {
            if (pricehistory_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehiclePriceHistory selected = (ModelVehiclePriceHistory) tblViewPriceHistory.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(filteredDataDetail.get(tblViewPriceHistory.getSelectionModel().getSelectedIndex()).getIndex09());
                        pnPriceHistory = lnRow;
                        moveNext(false, false);
                    }
                }
            }
        });

//        JFXUtil.applyRowHighlighting(tblViewDetail, item -> ((ModelVehiclePriceList_Detail) item).getIndex01(), highlightedRowsMain);
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
