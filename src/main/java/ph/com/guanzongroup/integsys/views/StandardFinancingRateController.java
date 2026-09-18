package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.StandardFinancingRates;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class StandardFinancingRateController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static StandardFinancingRates poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private boolean pbEntered = false;
    private int pnDetail = 0;
    private boolean isForDialog = false;
    private boolean isForUpdate = false;
    private String lsStandardRateID = "";
    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnVoid, btnDeactivate, btnHistory, btnClose;
    @FXML
    private FontAwesomeIconView faActivate111, faActivate, faActivate1, faActivate11;
    @FXML
    private TextField tfStandardRateID, tfDuration, tfRate;
    @FXML
    private DatePicker dpValidFrom, dpTo;
    @FXML
    private ComboBox cmbType;
    @FXML
    private Label lblStatus, lblParameterLabel;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList("Interest rate", "Downpayment rate");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (isForDialog()) {
            } else {
                JFXUtil.setVisibility(false, lblParameterLabel);
                poController = new SalesControllers(oApp, null).StandardFinancingRates();
            }
            poController.initialize();
            poJSON = new JSONObject();
            initTextFields();
            initDatepickers();
            initComboboxes();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            poController.setWithUI(true);
            Platform.runLater(() -> {
                loadRecordMaster();
                if (isForUpdate()) {
                    try {
                        poController.openRecord(lsStandardRateID);
                        loadRecordMaster();
                        pnEditMode = poController.getEditMode();
                        initButton(pnEditMode);
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                } else {
                    btnNew.fire();
                }
            });
            poController.setRecordStatus("01234");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initializeDialog(GRiderCAS oApp1) {
        oApp = oApp1;
        poController = new SalesControllers(oApp1, null).StandardFinancingRates();
    }

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        System.out.println(fsValue);
        this.psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        //No category
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poController.setRecordStatus("01234");
                        poJSON = poController.searchRecord("", false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfStandardRateID.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnClose":
                        //define for standalone and for parameter
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            CommonUtils.closeStage(btnClose);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        clearTextFields();

                        poJSON = poController.NewRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            poController.initialize();
                            clearTextFields();
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
                            poJSON = poController.saveRecord();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                loadRecordMaster();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                // Print Transaction Prompt
                                poController.initialize();
                                pnEditMode = poController.getEditMode();
                            }
                        } else {
                            return;
                        }
                        if (isForDialog()) {
                            CommonUtils.closeStage(btnClose);
                        }
                        break;
                    case "btnActivate":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to activate the transaction?") == false) {
                            return;
                        }
                        poJSON = poController.ActivateRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getStandardRateId());
                        pnEditMode = poController.getEditMode();
                        if (isForDialog()) {
                            CommonUtils.closeStage(btnClose);
                        }
                        break;
                    case "btnVoid":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to void the transaction?") == false) {
                            return;
                        }
                        poJSON = poController.VoidRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getStandardRateId());
                        pnEditMode = poController.getEditMode();
                        if (isForDialog()) {
                            CommonUtils.closeStage(btnClose);
                        }
                        break;
                    case "btnDeactivate":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to deactivate the transaction?") == false) {
                            return;
                        }
                        poJSON = poController.DeactivateRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getStandardRateId());
                        pnEditMode = poController.getEditMode();
                        if (isForDialog()) {
                            CommonUtils.closeStage(btnClose);
                        }
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                loadRecordMaster();
                initButton(pnEditMode);
            } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDuration":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setDuration(Integer.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (Double.valueOf(lsValue) > 100.00) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Rate must not be greater than 100.00");
                            break;
                        }
                        poJSON = poController.getModel().setRate(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbType":
                        poJSON = poController.getModel().setRateType(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

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
                                poController.getModel().setFromDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                                poController.getModel().setThruDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfDuration, tfRate);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster);
        JFXUtil.inputDecimalOnly(tfRate);
        JFXUtil.inputIntegersOnly(tfDuration);
    }

    private void initDatepickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpValidFrom, dpTo);
        JFXUtil.setActionListener(datepicker_Action, dpValidFrom, dpTo);

        dpTo.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Lost focus
                if (JFXUtil.isObjectEqualTo(dpTo.getEditor().getText(), null, "")) {
                    if (pbSuccess) {
                        poJSON = poController.getModel().setThruDate(null);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        dpTo.setValue(null);
                    }
                    pbSuccess = false;
                    loadRecordMaster();
                    pbSuccess = true;
                }
            }
        });
    }

    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlist, cmbType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbType);
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();
        int lnRow = pnDetail;

        switch (event.getCode()) {
            case TAB:
            case ENTER:
                pbEntered = true;
                CommonUtils.SetNextFocus(txtField);
                event.consume();
                break;
            case F3:
                break;
            default:
                break;
        }
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apMaster);
    }

    public void ForDialog(boolean lbisForDialog) {
        JFXUtil.setVisibility(!lbisForDialog, lblParameterLabel);
        isForDialog = lbisForDialog;
    }

    public boolean isForDialog() {
        return isForDialog;
    }

    public void openRecordForAdd() {
        btnNew.fire();
    }

    public void isForUpdate(boolean lbisForUpdate) {
        isForUpdate = lbisForUpdate;
    }

    private boolean isForUpdate() {
        return isForUpdate;
    }

    public void openRecordForUpdate(String lsValue) {
        lsStandardRateID = lsValue;
    }

    private void loadRecordMaster() {
        JFXUtil.setStatusValue(lblStatus, FinancingRateStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.getModel().getRecordStatus());
        tfStandardRateID.setText(poController.getModel().getStandardRateId());
        dpValidFrom.setValue(poController.getModel().getFromDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.getModel().getFromDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);

        if (JFXUtil.isObjectEqualTo(poController.getModel().getRateType(), "", null)) {
            poController.getModel().setRateType("0");
            JFXUtil.setCmbValue(cmbType, !poController.getModel().getRateType().equals("") ? Integer.valueOf(poController.getModel().getRateType()) : -1);
        } else {
            JFXUtil.setCmbValue(cmbType, !poController.getModel().getRateType().equals("") ? Integer.valueOf(poController.getModel().getRateType()) : -1);
        }

        if (JFXUtil.isObjectEqualTo(poController.getModel().getRateType(), "1")) {
            JFXUtil.setDisabled(true, tfDuration);
            poController.getModel().setDuration(0);
        } else {
            JFXUtil.setDisabled(false, tfDuration);
        }
        tfDuration.setText(String.valueOf(poController.getModel().getDuration()));
        dpTo.setValue(poController.getModel().getThruDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.getModel().getThruDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
        tfRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getRate().doubleValue(), false));
    }

    private boolean isActive() {
        switch (poController.getModel().getRecordStatus()) {
            case FinancingRateStatus.ACTIVE:
                return true;
        }
        return false;
    }

    private void btnActivateVisibility() {
        try {
            if (JFXUtil.isObjectEqualTo(poController.getModel().getThruDate(), null, "")) {
                if (!isActive()) {
                    JFXUtil.setButtonsVisibility(true, btnActivate);
                } else {
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                }
                return;
            }
            SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
            String lsServerDate = sdfFormat.format(oApp.getServerDate());
            LocalDate currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
            LocalDate selectedDate = LocalDate.parse(CustomCommonUtil.formatDateToShortString(poController.getModel().getThruDate()), DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
            if (currentDate.isBefore(selectedDate) || currentDate.isEqual(selectedDate)) {
                //if valid thru is before current date or today
                //also if null show btnactivate
                if (!isActive()) {
                    JFXUtil.setButtonsVisibility(true, btnActivate);
                }
            } else {
                if (isActive()) {
                    JFXUtil.setButtonsVisibility(false, btnUpdate);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(StandardFinancingRateController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew, btnBrowse);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);
        JFXUtil.setButtonsVisibility(false, btnActivate, btnVoid, btnDeactivate);
        JFXUtil.setDisabled(!lbShow, apMaster);
        if (fnValue != EditMode.READY) {
            return;
        }
        //enables disables visibility of buttons
        switch (poController.getModel().getRecordStatus()) {
            case FinancingRateStatus.OPEN:
                JFXUtil.setButtonsVisibility(true, btnVoid);
                JFXUtil.setButtonsVisibility(false, btnDeactivate);
                btnActivateVisibility();
                break;
            case FinancingRateStatus.ACTIVE:
                JFXUtil.setButtonsVisibility(true, btnDeactivate);
                JFXUtil.setButtonsVisibility(false, btnActivate, btnVoid);
                btnActivateVisibility();
                break;
            case FinancingRateStatus.INACTIVE:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                JFXUtil.setButtonsVisibility(false, btnVoid, btnDeactivate);
                btnActivateVisibility();
                break;
            case FinancingRateStatus.VOID:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                JFXUtil.setButtonsVisibility(false, btnVoid, btnDeactivate);
                JFXUtil.setButtonsVisibility(false, btnActivate);
                break;
        }
    }
}
