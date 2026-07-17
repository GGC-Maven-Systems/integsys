package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.OtherPaymentStatusUpdate;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.OtherPaymentStatus;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class OtherPaymentStatusController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private int pnMain = 0;
    private final String pxeModuleName = "Other Payment Status Update";
    private OtherPaymentStatusUpdate poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    ;

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();

    ObservableList<String> cCheckState = FXCollections.observableArrayList("FLOAT", "OPEN", "POSTED", "CANCELLED");

    JFXUtil.ReloadableTableTask loadTableMain;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchIndustry, tfSearchBankName, tfSearchBankAccount, tfSearchDVNo, tfTransactionNo, tfBankName, tfBankAccount, tfSupplierBank, tfSupplierAccountNo, tfVoucherNo, tfReferenceNo, tfPaymentAmount, tfDisbursementType;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSave, btnCancel, btnRetrieve, btnHistory, btnClose;
    @FXML
    private DatePicker dpTransactionDate, dpPostingDate;
    @FXML
    private ComboBox cmbPaymentStatus;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblDVNo, tblDVDate, tblBankName, tblBankAccount, tblReferenceNo;
    @FXML
    private Pagination pagination;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new CashflowControllers(oApp, null).OtherPaymentStatusUpdate();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initMainGrid();
            initComboBoxes();
            initDatePicker();
            initTextFields();
            initLoadTable();
            initTableOnClick();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            pagination.setPageCount(1);
            Platform.runLater(() -> {
                poController.setTransactionStatus(OtherPaymentStatus.OPEN + OtherPaymentStatus.FLOAT);
                poController.setCompanyID(psCompanyId);
                poController.setIndustryID(psIndustryId);
                poController.Master().setIndustryID(psIndustryId);
                poController.Master().setCompanyID(psCompanyId);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void resetComboboxValue() {
        cCheckState = FXCollections.observableArrayList("FLOAT", "OPEN", "POSTED", "CANCELLED");
        if (pnEditMode == (EditMode.UPDATE)) {
            switch (poController.Master().getDisbursementType()) {
                case DisbursementStatic.DisbursementType.CHECK:
                    break;
                case DisbursementStatic.DisbursementType.WIRED:
                    cCheckState.remove("FLOAT");
                    break;
                case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    cCheckState.remove("FLOAT");
                    cCheckState.remove("OPEN");
                    break;
            }
        }

        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(cCheckState, cmbPaymentStatus));
        apMaster.requestFocus();
    }

    @FXML
    void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                    if ("error".equals((String) poJSON.get("resulf"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    poJSON = poController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }

                    if (poController.OtherPayments().getModel().getTransactionStatus().equals(OtherPaymentStatus.FLOAT)) {
                        if (poController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.DIGITAL_PAYMENT)) {
                            poController.OtherPayments().getModel().setTransactionStatus((OtherPaymentStatus.POSTED));
                            JFXUtil.showTooltip("Default status: POSTED. Can be changed.", cmbPaymentStatus);
                        } else {
                            poController.OtherPayments().getModel().setTransactionStatus((OtherPaymentStatus.OPEN));
                            JFXUtil.showTooltip("Default status: OPEN. Can be changed.", cmbPaymentStatus);
                        }
                    }

                    pnEditMode = poController.getEditMode();
                    loadRecordMaster();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
                        JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
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
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(npe));
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                        return;
                    }

                    switch (poController.OtherPayments().getModel().getTransactionStatus()) {
                        case OtherPaymentStatus.CANCELLED:
                            poJSON = poController.ReturnTransaction("");
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            break;
                        default:
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            break;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                    pnEditMode = EditMode.UNKNOWN;
                    JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                    break;
                case "btnRetrieve":
                    retrieveDisbursements();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this Tab?", "Close Tab", null)) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else { 
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", pxeModuleName, null);
                    break; 
            }
            if (lsButton.equals("btnSave") || lsButton.equals("btnCancel")) {
                clearTextFields();
                pnEditMode = EditMode.UNKNOWN;
            }
            initButton(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpPostingDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpPostingDate);
    }

    private void initTextFields() {
        //Initialise  TextField KeyPressed
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchBankName, tfSearchBankAccount, tfSearchDVNo, tfSearchIndustry);
        JFXUtil.setFocusListener(txtMaster_Focus, tfReferenceNo);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchBankName":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBank("");
                        }
                        break;
                    case "tfSearchBankAccount":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBankAccount("");
                        }
                        break;
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                }
                loadRecordSearch();
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfReferenceNo":
                        poJSON = poController.OtherPayments().getModel().setReferNox(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            poController.OtherPayments().getModel().setReferNox("");
                        }
                        break;
                }
                loadRecordMaster();
            });

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                String selected = "";
                switch (cmbId) {
                    case "cmbPaymentStatus":
                        switch (String.valueOf(selectedValue)) {
                            case "FLOAT":
                                selected = "0";
                                break;
                            case "OPEN":
                                selected = "1";
                                break;
                            case "POSTED":
                                selected = "2";
                                break;
                            case "CANCELLED":
                                selected = "3";
                                break;
                        }
                        poJSON = poController.OtherPayments().getModel().setTransactionStatus(selected);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        }
                        break;
                }
                loadRecordMaster();
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();

        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearchBankName":
                                poJSON = poController.SearchBanks(lsValue);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                loadRecordSearch();
                                retrieveDisbursements();
                                break;
                            case "tfSearchBankAccount":
                                poJSON = poController.SearchBankAccount(lsValue, poController.getSearchBankId());
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                loadRecordSearch();
                                retrieveDisbursements();
                                break;
                            case "tfSearchIndustry":
                                poJSON = poController.SearchIndustry(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    break;
                                }
                                loadRecordSearch();
                                retrieveDisbursements();
                                break;
                            case "tfSearchDVNo":
                                retrieveDisbursements();
                                break;
                        }
                        event.consume();
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");
        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                LocalDate currentDate = null, transactionDate = null, referenceDate = null, selectedDate = null;
                String lsServerDate = "", lsTransDate = "", lsRefDate = "", lsSelectedDate = "";

                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }
                lsServerDate = sdfFormat.format(oApp.getServerDate());
                currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                switch (datePicker.getId()) {
                    case "dpPostingDate":
                        //back date not allowed
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                            transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            }
                            if (selectedDate.isBefore(transactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Posting date cannot be before than the transaction date.");
                                pbSuccess = false;
                            }
                            if (pbSuccess && (selectedDate.isAfter(transactionDate))) {
                                JFXUtil.setJSONError(poJSON, "Posting date cannot be later than the transaction date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.OtherPayments().getModel().setPostedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordSearch() {
        try {
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchBankName.setText(poController.getSearchBank());
            tfSearchBankAccount.setText(poController.getSearchBankAccount());
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMaster() {
        try {
            boolean lbShow = OtherPaymentStatus.POSTED.equals(poController.OtherPayments().getModel().getTransactionStatus());

            if (poController.OtherPayments().getModel().getTransactionStatus().equals(OtherPaymentStatus.POSTED) && pnEditMode == EditMode.UPDATE) {
                if (JFXUtil.isObjectEqualTo(poController.OtherPayments().getModel().getPostedDate(), null, "")) {
                    SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                    String lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                    poController.OtherPayments().getModel().setPostedDate(SQLUtil.toDate(lsTransDate, SQLUtil.FORMAT_SHORT_DATE));
                }
            }
            if (!lbShow && pnEditMode == EditMode.UPDATE) {
                poController.OtherPayments().getModel().setPostedDate(null);
            }

            resetComboboxValue();
            Platform.runLater(() -> {
                JFXUtil.setDisabled(!lbShow, dpPostingDate);
            });
            JFXUtil.setDisabled(true, tfSupplierBank, tfSupplierAccountNo);

            tfTransactionNo.setText(poController.Master().getTransactionNo());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));

            tfBankName.setText(poController.OtherPayments().getModel().Banks().getBankName() != null ? poController.OtherPayments().getModel().Banks().getBankName() : "");
            tfBankAccount.setText(poController.OtherPayments().getModel().getBankAcountID() != null ? poController.OtherPayments().getModel().getBankAcountID() : "");
            tfReferenceNo.setText(poController.OtherPayments().getModel().getReferNox() != null ? poController.OtherPayments().getModel().getReferNox() : "");
            String selected = "";
            switch (Integer.parseInt(poController.OtherPayments().getModel().getTransactionStatus())) {
                case 0:
                    selected = "FLOAT";
                    break;
                case 1:
                    selected = "OPEN";
                    break;
                case 2:
                    selected = "POSTED";
                    break;
                case 3:
                    selected = "CANCELLED";
                    break;
            }
            if (Integer.parseInt(poController.OtherPayments().getModel().getTransactionStatus()) == 0) {
                JFXUtil.setCmbValue(cmbPaymentStatus, -1);
            } else {
                JFXUtil.setCmbValue(cmbPaymentStatus, selected);
            }
            tfVoucherNo.setText(poController.Master().getVoucherNo());
            dpPostingDate.setValue(poController.OtherPayments().getModel().getPostedDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.OtherPayments().getModel().getPostedDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);
            tfPaymentAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.OtherPayments().getModel().getTotalAmount(), true));

            tfSupplierBank.setText("");
            tfSupplierAccountNo.setText("");

            JFXUtil.setStatusValue(tfDisbursementType, DisbursementStatic.DisbursementType.class, poController.Master().getDisbursementType());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(cCheckState, cmbPaymentStatus));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbPaymentStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbPaymentStatus);
    }

    private void retrieveDisbursements() {
        try {
            poJSON = poController.loadTransactionList(tfSearchIndustry.getText(), tfSearchBankName.getText(), tfSearchBankAccount.getText(), tfSearchDVNo.getText());
            if ("success".equals(poJSON.get("result"))) {
            } else {
                if (tfSearchIndustry.getText().isEmpty()) {
                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                }
            }
            loadTableMain.reload();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            try {
                                main_data.clear();
                                if (poController.getOtherPaymentList().size() > 0) {
                                    for (int lnCntr = 0; lnCntr <= poController.getOtherPaymentList().size() - 1; lnCntr++) {
                                        main_data.add(new ModelDisbursementVoucher_Main(
                                                String.valueOf(lnCntr + 1),
                                                poController.getOtherPayment(lnCntr).getVoucherNo(),
                                                CustomCommonUtil.formatDateToShortString(poController.getOtherPayment(lnCntr).getTransactionDate()),
                                                poController.getOtherPayment(lnCntr).OtherPayments().Banks().getBankName(),
                                                poController.getOtherPayment(lnCntr).OtherPayments().Bank_Account_Master().getAccountNo(),
                                                poController.getOtherPayment(lnCntr).OtherPayments().getReferNox(),
                                                poController.getOtherPayment(lnCntr).getTransactionNo()
                                        ));
                                        if (OtherPaymentStatus.POSTED.equals(poController.getOtherPayment(lnCntr).OtherPayments().getTransactionStatus())) {
                                            JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCntr + 1), "#C1E1C1", highlightedRowsMain);
                                        }
                                    }
                                } else {
                                    main_data.clear();
                                    filteredMain_Data.clear();
                                }

                                if (pnMain < 0 || pnMain
                                        >= main_data.size()) {
                                    if (!main_data.isEmpty()) {
                                        /* FOCUS ON FIRST ROW */
                                        JFXUtil.selectAndFocusRow(tblViewMainList, 0);
                                        pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                                    }
                                } else {
                                    /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                    JFXUtil.selectAndFocusRow(tblViewMainList, pnMain);
                                }
                                JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredMain_Data);
                            } catch (GuanzonException | SQLException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDVDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblBankName, tblBankAccount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {

                int lnRow = tblViewMainList.getSelectionModel().getSelectedIndex();
                if (lnRow >= 0) {
                    loadTableDetailFromMain();
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelDisbursementVoucher_Main) item).getIndex01(), highlightedRowsMain);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();
        ModelDisbursementVoucher_Main selected = (ModelDisbursementVoucher_Main) tblViewMainList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                String lsTransactionNo = selected.getIndex07();
                if (!JFXUtil.loadValidation(pnEditMode, pxeModuleName, poController.Master().getTransactionNo(), lsTransactionNo)) {
                    return;
                }

                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;

                clearTextFields();
                poJSON = poController.OpenTransaction(lsTransactionNo);
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                pnEditMode = poController.getEditMode();
                loadRecordMaster();
                initButton(pnEditMode);
            } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(!lbShow, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(false, btnUpdate);
        JFXUtil.setButtonsVisibility(fnEditMode != EditMode.UNKNOWN, btnHistory);
        JFXUtil.setDisabled(!lbShow, apMaster);
        if (fnEditMode == EditMode.READY) {
            switch (poController.OtherPayments().getModel().getTransactionStatus()) {
                case CheckStatus.FLOAT:
                case CheckStatus.OPEN:
                case CheckStatus.STOP_PAYMENT:
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                    break;
            }
        }
    }

    private void clearTextFields() {
        JFXUtil.clearTextFields(apMaster);
    }
}
