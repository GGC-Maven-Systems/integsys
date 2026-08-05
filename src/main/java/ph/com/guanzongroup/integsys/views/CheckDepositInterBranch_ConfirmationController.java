package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.CheckDeposit;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckDepositStatus;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class CheckDepositInterBranch_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private CheckDeposit poController;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private boolean pbIsCheckedJournalTab = false;
    private int pnMain = 0;
    private int pnDetail = 0;
    private int pnAttachment;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    private int pnDetailJE = 0;
    private int currentIndex = 0;
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> journal_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    Scene scene = null;
    private FileChooser fileChooser;
    private boolean pbEntered = false;
    private boolean pbEnteredJE = false;
    private FilteredList<ModelTableMain> filteredData;
    private boolean tooltipShown = false;
    JFXUtil.ReloadableTableTask loadTableMain, loadTableDetail, loadTableDetailJE, loadTableAttachment;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private String psSearchDate = "";

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster, apDetail, apJournalMaster, apJournalDetails, apAttachments, apAttachmentButtons, apTransaction;
    @FXML
    private TextField tfSearchBankAccountNo, tfSearchTransNo, tfTransactionNo, tfBankMaster, tfBankAccountNo, tfBankAccountName, tfTotal, tfCheckTransNo, tfBank, tfPayee, tfNote, tfCheckNo, tfCheckAmount, tfJournalTransactionNo, tfTotalDebitAmount, tfTotalCreditAmount, tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount, tfAttachmentNo;
    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpTransactionReferDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear;
    @FXML
    private Label lblSource, lblStatus, lblJournalTransactionStatus;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnApprove, btnVoid, btnPrint, btnHistory, btnRetrieve, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private TextArea taRemarks, taJournalRemarks;
    @FXML
    private CheckBox cbReverse, cbJEReverse;
    @FXML
    private TableView tblViewDetail, tblVwJournalDetails, tblAttachments, tblViewMain;
    @FXML
    private TableColumn tblColDetailNo, tblColDetailReference, tblColDetailBank, tblColDetailPayee, tblColDetailDate, tblColDetailCheckNo, tblColDetailCheckAmount, tblJournalRowNo, tblJournalReportMonthYear, tblJournalAccountCode, tblJournalAccountDescription, tblJournalDebitAmount, tblJournalCreditAmount, tblRowNoAttachment, tblFileNameAttachment, tblColNo, tblColTransNo, tblColTransDate, tblColBankAccountNo, tblColBankAccountNme;
    @FXML
    private Tab tabJournal, tabAttachments;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

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
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            poController = new CashflowControllers(oApp, logwrapr).CheckDeposit();
            poJSON = new JSONObject();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            poController.setTransactionStatus("01");
            initLoadTable();
            initTextFields();
            initDatePicker();
            initDetailGrid();
            initMainGrid();
            initDetailJEGrid();
            initAttachmentsGrid();
            initTableOnClick();
            initTabPane();
            clearTextFields();
            initComboboxes();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            Platform.runLater(() -> {
                try {
                    poController.isCheckDepositSupplier(false);
                    poController.Master().setIndustryId(psIndustryId);
                    poController.Master().setCompany(psCompanyId);
//                    poController.setIndustryId(psIndustryId);
                    poController.setCompanyId(psCompanyId);
//                poController.setCategoryID(psCategoryId);
//                    poController.Master().setBranchCode(oApp.getBranchCode());
                    loadRecordSearch();
                    lblSource.setText(poController.getCompanyName() + " - " + poController.Master().Industry().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });
            initAttachmentPreviewPane();
            JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private boolean DoesContainValidDetail() {
        String lsParticular = "";
        if (poController.getDetailCount() <= 0) {
            return false;
        }
        return true;
    }
    String lsValidDisbMessage = "Please provide at least one valid check deposit detail to proceed.";

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Check Deposit":
                    if (pnEditMode == EditMode.UNKNOWN) {
                        pnDetailJE = 0;
                    } else {
                        loadRecordMaster();
                    }
                    break;
                case "Journal":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apJournalDetails, apJournalMaster);
                        if (DoesContainValidDetail()) {
                            pbIsCheckedJournalTab = true;
                            populateJE();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                            ShowMessageFX.Warning(null, pxeModuleName, lsValidDisbMessage);
                        }
                    }
                    break;
                case "Attachments":
                    if (pnEditMode == EditMode.READY) {
                        JFXUtil.clearTextFields(apAttachments);
                        if (DoesContainValidDetail()) {
//                            if (isSourceNoAvailable()) {
//                                pbIsCheckedAttachmentTab = true;
//                            try {
//                                poController.loadAttachments();
//                            } catch (GuanzonException | SQLException ex) {
//                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//                            }
//                            }
                            loadTableAttachment.reload();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                            ShowMessageFX.Warning(null, pxeModuleName, lsValidDisbMessage);
                        }
                    }
                    break;
            }
        });
    }

    private void populateJE() {
        try {
            poJSON = new JSONObject();
            JFXUtil.clearTextFields(apJournalMaster, apJournalDetails);
            poController.getEditMode();
            poJSON = poController.populateJournal();
            if (JFXUtil.isJSONSuccess(poJSON)) {
                loadTableDetailJE.reload();
            } else {
                journal_data.clear();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initComboboxes() {
        // ComboBox setup
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poController.TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (Exception e) {
                }
            }
        });
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAttachmentType);
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpSearchTransactionDate, dpTransactionDate, dpTransactionReferDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear);
        JFXUtil.setActionListener(datepicker_Action, dpSearchTransactionDate, dpTransactionDate, dpTransactionReferDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear);
        dpSearchTransactionDate.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                String text = dpSearchTransactionDate.getEditor().getText();
                if (text == null || text.trim().isEmpty()) {
                    dpSearchTransactionDate.setValue(null);
                    psSearchDate = "";
                    return;
                }
                try {
                    LocalDate date = LocalDate.parse(text, formatter);
                    dpSearchTransactionDate.setValue(date);
                } catch (DateTimeParseException e) {
                    dpSearchTransactionDate.setValue(null);
                    psSearchDate = "";
                    dpSearchTransactionDate.requestFocus();
                }
            }
        });
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                try {
                    poJSON = new JSONObject();
                    String lsTransDate;
                    LocalDate transactionDate;
                    switch (datePicker.getId()) {
                        case "dpSearchTransactionDate":
                            psSearchDate = CustomCommonUtil.formatLocalDateToShortString(ldSelectedDate);
                            loadTableMain.reload();
                            break;
                        case "dpTransactionDate":
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                            //back date not allowed
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                                transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                                if (pbSuccess && (!lsTransDate.equals(lsSelectedDate))) {
                                    if (oApp.getUserLevel() <= UserRight.ENCODER) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Change in Transaction Date Detected\n\n"
                                                + "If YES, please seek approval to proceed with the new selected date.\n"
                                                + "If NO, the previous transaction date will be retained.") == true) {
                                            poJSON = ShowDialogFX.getUserApproval(oApp);
                                            if (!"success".equals((String) poJSON.get("result"))) {
                                                pbSuccess = false;
                                            } else {
                                                if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                                                    poJSON.put("result", "error");
                                                    poJSON.put("message", "User is not an authorized approving officer.");
                                                    pbSuccess = false;
                                                } else {
                                                    String lsReferDate = sdfFormat.format(poController.Master().getTransactionReferDate());
                                                    LocalDate ldReferDate = LocalDate.parse(lsReferDate,
                                                            DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                                                    if (pbSuccess && (ldReferDate.isBefore(ldSelectedDate))) {
                                                        poController.Master().setTransactionReferDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                                                    }
                                                }
                                            }
                                        } else {
                                            pbSuccess = false;
                                        }
                                    }
                                }

                                if (pbSuccess) {
                                    poJSON = poController.Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                        case "dpTransactionReferDate":
                            String lsTransDate1 = sdfFormat.format(poController.Master().getTransactionDate());
                            LocalDate ldTransactionDate = LocalDate.parse(lsTransDate1,
                                    DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (pbSuccess && (ldSelectedDate.isBefore(ldTransactionDate))) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Reference date cannot be before the Check deposit date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poJSON = poController.Master().setTransactionReferDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }

                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                            break;
                        case "dpReportMonthYear":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                                transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                                if (ldSelectedDate.isAfter(ldCurrentDate)) {
                                    JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                    pbSuccess = false;
                                }

                                if (pbSuccess && (ldSelectedDate.isAfter(transactionDate))) {
                                    JFXUtil.setJSONError(poJSON, "Report date cannot be later than the transaction date.");
                                    pbSuccess = false;
                                }

                                if (pbSuccess) {
                                    poController.Journal().Detail(pnDetailJE).setForMonthOf((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                                } else {
                                    if ("error".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    }
                                }
                                pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                                loadTableDetailJE.reload();
                                pbSuccess = true; //Set to original value
                            }
                            break;
                        default:
                            break;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pbIsCheckedJournalTab = false;
                    pnEditMode = poController.getEditMode();
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                    loadTableDetail.reload();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail, apJournalDetails, apTransaction);
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                        return;
                    }
                    poJSON = poController.SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                    poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                    if ("success".equals(poJSON.get("result"))) {
                        pnEditMode = poController.getEditMode();
                        initButton(pnEditMode);
                    }
                    if (pnEditMode == EditMode.READY) {
                        if (CheckDepositStatus.OPEN.equals(poController.Master().getTransactionStatus())) {
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) { //requires to review journal entry
                                if (!poController.existJournal().equals("")) {
                                    if (!pbIsCheckedJournalTab) {
                                        ShowMessageFX.Warning(null, pxeModuleName, "Please check the Journal Entry before saving.");
                                        break;
                                    } else {
                                        poJSON = poController.ConfirmTransaction();
                                        if ("error".equals((String) poJSON.get("result"))) {
                                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                            break;
                                        } else {
                                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                            JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                        }
                                    }
                                } else {
                                    ShowMessageFX.Warning(null, pxeModuleName, "No journal entry found. Add a journal entry and save before confirming.");
                                    break;
                                }
                            }
                        }
                    }
                    JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
                        JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                        pnEditMode = EditMode.UNKNOWN;
                        break;
                    } else {
                        return;
                    }
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning(null, pxeModuleName, "No transaction status history to load!");
                        return;
                    }

                    try {
                        poController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(npe));
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                    }
                    break;
                case "btnRetrieve":
                    loadTableMain.reload();
                    break;
                case "btnApprove":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?\nPlease ensure that the Journal Entry period date is correct before proceeding.")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            if (!poController.existJournal().equals("")) {
                                if (!pbIsCheckedJournalTab) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "Please check the Journal Entry before confirming.");
                                    JFXUtil.clickTabByTitleText(tabPaneMain, "Journal");
                                    return;
                                } else {
                                    poJSON = poController.ConfirmTransaction();
                                    if ("error".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                        return;
                                    } else {
                                        ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                        JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                                        JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                    }
                                }
                            } else {
                                ShowMessageFX.Warning(null, pxeModuleName, "This transaction has no journal entry. Please add a journal entry by updating the transaction to enable confirmation.");
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                case "btnPrint":
                    poJSON = poController.PrintDepositSlip();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    } else {
                        ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
                case "btnVoid":
                    String lsStat = "";
                    switch (poController.Master().getTransactionStatus()) {
                        case CheckDepositStatus.OPEN:
                            lsStat = "void";
                            break;
                        case CheckDepositStatus.CONFIRMED:
                            lsStat = "cancel";
                            break;
                    }

                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsStat + " the transaction?")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            switch (poController.Master().getTransactionStatus()) {
                                case CheckDepositStatus.OPEN:
                                    poJSON = poController.VoidTransaction();
                                    break;
                                case CheckDepositStatus.CONFIRMED:
                                    poJSON = poController.CancelTransaction();
                                    break;
                            }

                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                pnEditMode = poController.getEditMode();
                                JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnAddAttachment":
                    fileChooser = new FileChooser();
                    fileChooser.setTitle("Choose Image");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.pdf")
                    );
                    java.io.File selectedFile = fileChooser.showOpenDialog((Stage) btnAddAttachment.getScene().getWindow());

                    if (selectedFile != null) {
                        // Read image from the selected file
                        Path imgPath = selectedFile.toPath();
                        Image loimage = new Image(Files.newInputStream(imgPath));
                        imageView.setImage(loimage);

                        //Validate attachment
                        String imgPath2 = selectedFile.getName().toString();
                        for (int lnCtr = 0; lnCtr <= poController.getTransactionAttachmentCount() - 1; lnCtr++) {
                            if (imgPath2.equals(poController.TransactionAttachmentList(lnCtr).getModel().getFileName())
                                    && RecordStatus.ACTIVE.equals(poController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                ShowMessageFX.Warning(null, pxeModuleName, "File name already exists.");
                                pnAttachment = lnCtr;
                                loadRecordAttachment(true);
                                return;
                            }
                        }

                        //Limit maximum pages of pdf to add
                        if (imgPath2.toLowerCase().endsWith(".pdf")) {
                            try ( PDDocument document = PDDocument.load(selectedFile)) {
                                PDFRenderer pdfRenderer = new PDFRenderer(document);
                                int pageCount = document.getNumberOfPages();
                                if (pageCount > 20) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "PDF exceeds maximum allowed pages.");
                                    return;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        }

                        pnAttachment = poController.addAttachment(imgPath2);
                        //Copy file to Attachment path
                        poController.copyFile(selectedFile.toString());
                        loadTableAttachment.reload();
                        tblAttachments.getFocusModel().focus(pnAttachment);
                        tblAttachments.getSelectionModel().select(pnAttachment);
                    }
                    break;
                case "btnRemoveAttachment":
                    if (poController.getTransactionAttachmentCount() <= 0) {
                        return;
                    } else {
                        for (int lnCtr = 0; lnCtr < poController.getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                if (pnAttachment == lnCtr) {
                                    return;
                                }
                            }
                        }
                    }
                    poJSON = poController.removeAttachment(pnAttachment);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    attachment_data.remove(tblAttachments.getSelectionModel().getSelectedIndex());
                    if (pnAttachment != 0) {
                        pnAttachment -= 1;
                    }
                    loadRecordAttachment(false);
                    loadTableAttachment.reload();
                    if (attachment_data.size() <= 0) {
                        JFXUtil.clearTextFields(apAttachments);
                    }
                    initAttachmentsGrid();
                    break;
                case "btnArrowRight":
                    slideImage(1);
                    break;
                case "btnArrowLeft":
                    slideImage(-1);
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button is not registered, Please contact admin to assist about the unregistered button");
                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsButton, "btnConfirm", "btnSave", "btnCancel", "btnVoid", "btnApprove")) {
                pbIsCheckedJournalTab = false;
                poController.resetTransaction();
                clearTextFields();
                JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnPrint", "btnRetrieve", "btnSearch", "btnArrowRight", "btnArrowLeft", "btnHistory", "btnRemoveAttachment", "btnAddAttachment")) {
            } else {
                loadRecordMaster();
                loadTableDetail.reload();
                loadTableDetailJE.reload();
                loadTableAttachment.reload();
            }
            initButton(pnEditMode);
            if (lsButton.equals("btnUpdate")) {
                moveNext(false, false);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException | IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poController.TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                }
                int lnAttachmentType = 0;
                lnAttachmentType = Integer.parseInt(lsAttachmentType);
                cmbAttachmentType.getSelectionModel().select(lnAttachmentType);

                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";
                        // in server
                        if (poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                            filePath2 = poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        } else {
                            filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        }

                        if (filePath != null && !filePath.isEmpty()) {
                            Path imgPath = Paths.get(filePath2);
                            String convertedPath = imgPath.toUri().toString();
                            boolean isPdf = filePath.toLowerCase().endsWith(".pdf");

                            // Clear previous content
                            stackPane1.getChildren().clear();
                            if (!isPdf) {
                                // ----- IMAGE VIEW -----
                                Image loimage = new Image(convertedPath);
                                imageView.setImage(loimage);
                                JFXUtil.adjustImageSize(loimage, imageView, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);

                                PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
                                delay.setOnFinished(event -> {
                                    Platform.runLater(() -> {
                                        JFXUtil.stackPaneClip(stackPane1);
                                    });
                                });
                                delay.play();

                                // Add ImageView directly to stackPane
                                stackPane1.getChildren().add(imageView);
                                stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);

                                // Align buttons on top
                                StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);

                                // Optional: add some margin
                                StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));
                            } else {
                                // ----- PDF VIEW -----
                                JFXUtil.PDFViewConfig(filePath2, stackPane1, btnArrowLeft, btnArrowRight, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);
                            }
                        } else {
                            imageView.setImage(null);
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                }
            } else {
                if (!lbloadImage) {
                    imageView.setImage(null);
                    stackPane1.getChildren().clear();
                    stackPane1.getChildren().add(imageView);
                    stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);
                    Platform.runLater(() -> JFXUtil.stackPaneClip(stackPane1));
                    pnAttachment = 0;
                }
            }
        } catch (Exception e) {
        }
    }

    private void loadRecordSearch() {
        tfSearchBankAccountNo.setText(poController.getSearchBankAccountNo());
        JFXUtil.updateCaretPositions(apBrowse);
    }

    private void loadRecordMaster() {
        try {
            String lsStat = "";
            switch (poController.Master().getTransactionStatus()) {
                case CheckDepositStatus.OPEN:
                    lsStat = "Void";
                    break;
                case CheckDepositStatus.CONFIRMED:
                    lsStat = "Cancel";
                    break;
            }
            btnVoid.setText(lsStat);
            poController.computeFields();
            JFXUtil.setStatusValue(lblStatus, CheckDepositStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            tfTransactionNo.setText(poController.Master().getTransactionNo());
            String lsBank = JFXUtil.isObjectEqualTo(poController.Master().Banks().getBankName(), null, "")
                    ? poController.Master().BankAccount().Banks().getBankName() : poController.Master().Banks().getBankName();
            tfBankMaster.setText(lsBank);
            tfBankAccountNo.setText(poController.Master().BankAccount().getAccountNo());
            tfBankAccountName.setText(poController.Master().BankAccount().getAccountName());
            taRemarks.setText(poController.Master().getRemarks());
            JFXUtil.setDateValue(dpTransactionDate, CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            JFXUtil.setDateValue(dpTransactionReferDate, CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionReferDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotalDeposit(), false));
            JFXUtil.updateCaretPositions(apMaster);
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            if (poController.Detail(pnDetail).getSourceNo().isEmpty()) {
                JFXUtil.setDisabled(false, tfCheckTransNo, tfCheckNo);
            } else {
                JFXUtil.setDisabled(true, tfCheckTransNo, tfCheckNo);
            }
            JFXUtil.setDisabled(true, tfBank, tfPayee, tfCheckAmount);
            tfCheckTransNo.setText(poController.Detail(pnDetail).Disbursement().getVoucherNo());
            tfBank.setText(poController.Detail(pnDetail).CheckPayment().Banks().getBankName());
            tfPayee.setText(poController.Detail(pnDetail).CheckPayment().Payee().getPayeeName());
            tfNote.setText(poController.Detail(pnDetail).getRemarks());
            tfCheckNo.setText(poController.Detail(pnDetail).CheckPayment().getCheckNo());
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).CheckPayment().getAmount(), false));
            dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Detail(pnDetail).CheckPayment().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE)));
            cbReverse.setSelected(poController.Detail(pnDetail) != null && poController.Detail(pnDetail).isReverse());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMasterJE() {
        JFXUtil.setStatusValue(lblJournalTransactionStatus, JournalStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Journal().Master().getTransactionStatus());
        tfJournalTransactionNo.setText(poController.Journal().Master().getTransactionNo());
        dpJournalTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Journal().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
        double lnTotalDebit = 0;
        double lnTotalCredit = 0;
        for (int lnCtr = 0; lnCtr < poController.Journal().getDetailCount(); lnCtr++) {
            if (!poController.Journal().Detail(lnCtr).isReverse()) {
                continue;
            }
            lnTotalDebit += poController.Journal().Detail(lnCtr).getDebitAmount();
            lnTotalCredit += poController.Journal().Detail(lnCtr).getCreditAmount();
        }
        tfTotalDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDebit, true));
        tfTotalCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalCredit, true));
        taJournalRemarks.setText(poController.Journal().Master().getRemarks());

        JFXUtil.updateCaretPositions(apJournalMaster);
    }

    public void loadRecordDetailJE() {
        try {
            if (pnDetailJE < 0 || pnDetailJE > poController.Journal().getDetailCount() - 1) {
                return;
            }
            boolean lbShow = poController.Journal().Detail(pnDetailJE).getEditMode() == EditMode.UPDATE;
            JFXUtil.setDisabled(lbShow, tfAccountCode, tfAccountDescription);

            cbJEReverse.setSelected(poController.Journal().Detail(pnDetailJE).isReverse());

            tfAccountCode.setText(poController.Journal().Detail(pnDetailJE).getAccountCode());
            tfAccountDescription.setText(poController.Journal().Detail(pnDetailJE).Account_Chart().getDescription());
            String lsReportMonth = CustomCommonUtil.formatDateToShortString(poController.Journal().Detail(pnDetailJE).getForMonthOf());
            JFXUtil.setDateValue(dpReportMonthYear, CustomCommonUtil.parseDateStringToLocalDate(lsReportMonth, "yyyy-MM-dd"));
            tfDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(pnDetailJE).getDebitAmount(), true));
            tfCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(pnDetailJE).getCreditAmount(), true));

            JFXUtil.updateCaretPositions(apJournalDetails);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initAttachmentPreviewPane() {
        imageviewerutil.initAttachmentPreviewPane(stackPane1, imageView);
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            imageviewerutil.ldstackPaneHeight = computedHeight;
            loadTableAttachment.reload();
            loadRecordAttachment(true);
        });
    }

    public void slideImage(int direction) {
        if (attachment_data.size() <= 0) {
            return;
        }
        int lnRow = Integer.valueOf(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
        currentIndex = lnRow - 1;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
            slideOut.setByX(direction * -400); // Move left or right

            JFXUtil.selectAndFocusRow(tblAttachments, newIndex);
            int lnIndex = Integer.valueOf(attachment_data.get(newIndex).getIndex01());
            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data, lnIndex, 3);
            pnAttachment = lnTempRow;
            loadRecordAttachment(false);

            // Create a transition animation
            slideOut.setOnFinished(event -> {
                imageView.setTranslateX(direction * 400);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), imageView);
                slideIn.setToX(0);
                slideIn.play();

                loadRecordAttachment(true);
            });

            slideOut.play();
        }
        if (JFXUtil.isImageViewOutOfBounds(imageView, stackPane1)) {
            JFXUtil.resetImageBounds(imageView, stackPane1);
        }
    }

    public void initAttachmentsGrid() {
        /*FOCUS ON FIRST ROW*/
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
        tblAttachments.setItems(attachment_data);
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblColNo, tblColTransNo, tblColTransDate);
        JFXUtil.setColumnLeft(tblColBankAccountNme, tblColBankAccountNo);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMain);

        tblViewMain.setItems(main_data);
    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblColDetailReference, tblColDetailNo, tblColDetailDate, tblColDetailCheckNo);
        JFXUtil.setColumnLeft(tblColDetailBank, tblColDetailPayee);
        JFXUtil.setColumnRight(tblColDetailCheckAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);

        tblViewDetail.setItems(detail_data);
    }

    private void initDetailJEGrid() {
        JFXUtil.setColumnCenter(tblJournalRowNo, tblJournalReportMonthYear);
        JFXUtil.setColumnLeft(tblJournalAccountCode, tblJournalAccountDescription);
        JFXUtil.setColumnRight(tblJournalDebitAmount, tblJournalCreditAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwJournalDetails);
        tblVwJournalDetails.setItems(journal_data);
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();

        pnMain = tblViewMain.getSelectionModel().getSelectedIndex();
        ModelTableMain selected = (ModelTableMain) tblViewMain.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                String lsTransactionNo = selected.getIndex02();
                if (!JFXUtil.loadValidation(pnEditMode, pxeModuleName, poController.Master().getTransactionNo(), lsTransactionNo)) {
                    return;
                }
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                JFXUtil.clearTextFields(apMaster);
                JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                poJSON = poController.OpenTransaction(lsTransactionNo);
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    poController.resetTransaction();
                    return;
                }
                pnEditMode = poController.getEditMode();
                loadTableDetail.reload();
                moveNext(false, false);
            } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }

    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbReverse":
                    if (poController.Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                        poController.Detail().remove(pnDetail);
                    } else {
                        poController.Detail(pnDetail).isReverse(cbReverse.isSelected());
                    }
                    loadTableDetail.reload();
                    if (checkedBox.isSelected()) {
                        moveNext(false, false);
                    }
                    break;
                case "cbJEReverse":
                    if (poController.Journal().Detail(pnDetailJE).getEditMode() == EditMode.ADDNEW) {
                        poController.Journal().Detail().remove(pnDetailJE);
                    } else {
                        poController.Journal().Detail(pnDetailJE).isReverse(cbJEReverse.isSelected());
                    }
                    loadRecordMasterJE();
                    loadTableDetailJE.reload();
                    if (checkedBox.isSelected()) {
                        moveNextJE(false, false);
                    }
                    break;
            }
        }
    }

    private void initLoadTable() {
        loadTableAttachment = new JFXUtil.ReloadableTableTask(
                tblAttachments,
                attachment_data,
                () -> {
                    imageviewerutil.scaleFactor = 1.0;
                    JFXUtil.resetImageBounds(imageView, stackPane1);
                    Platform.runLater(() -> {
                        try {
                            attachment_data.clear();
                            int lnCtr;
                            int lnCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getTransactionAttachmentCount(); lnCtr++) {
                                if (RecordStatus.INACTIVE.equals(poController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    continue;
                                }
                                lnCount += 1;
                                attachment_data.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                                String.valueOf(poController.TransactionAttachmentList(lnCtr).getModel().getFileName()),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(attachment_data, pnAttachment, 3); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= attachment_data.size()) {
                                if (!attachment_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblAttachments, 0);
                                    int lnRow = Integer.parseInt(attachment_data.get(0).getIndex03());
                                    pnAttachment = lnRow;
                                    loadRecordAttachment(true);
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblAttachments, lnTempRow);
                                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                                pnAttachment = lnRow;
                                loadRecordAttachment(true);
                            }
                            if (attachment_data.size() <= 0) {
                                loadRecordAttachment(false);
                            }
                        } catch (Exception e) {
                        }
                    });
                });
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMain,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            main_data.clear();
                            JFXUtil.disableAllHighlight(tblViewMain, highlightedRowsMain);
                            poJSON = poController.loadTransactionList(tfSearchTransNo.getText(), tfSearchBankAccountNo.getText(), psSearchDate);

                            if ("success".equals(poJSON.get("result"))) {
                                if (poController.getTransactionListCount() > 0) {
                                    for (int lnCntr = 0; lnCntr < poController.getTransactionListCount(); lnCntr++) {
                                        try {
                                            main_data.add(new ModelTableMain(
                                                    String.valueOf(lnCntr + 1),
                                                    poController.TransactionList(lnCntr).getTransactionNo(),
                                                    CustomCommonUtil.formatDateToShortString(poController.TransactionList(lnCntr).getTransactionDate()),
                                                    poController.TransactionList(lnCntr).BankAccount().getAccountNo(),
                                                    poController.TransactionList(lnCntr).BankAccount().getAccountName(),
                                                    "", "", "", "", ""
                                            ));
                                            if (poController.TransactionList(lnCntr).getTransactionStatus().equals(CheckDepositStatus.VOID)) {
                                                JFXUtil.highlightByKey(tblViewMain, String.valueOf(lnCntr + 1), "#FAA0A0", highlightedRowsMain);
                                            }
                                            if (poController.TransactionList(lnCntr).getTransactionStatus().equals(CheckDepositStatus.CONFIRMED)) {
                                                JFXUtil.highlightByKey(tblViewMain, String.valueOf(lnCntr + 1), "#C1E1C1", highlightedRowsMain);
                                            }
                                        } catch (SQLException | GuanzonException ex) {
                                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                        }
                                    }
                                } else {
                                    main_data.clear();
                                }
                            }

                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                detail_data, () -> {
                    Platform.runLater(() -> {
                        try {
                            detail_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            int OriginalRow = 0;
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (!poController.Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                OriginalRow += 1;
                                String lsdate = JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).CheckPayment().getCheckDate(), null, "") ? ""
                                        : CustomCommonUtil.formatDateToMMDDYYYY(poController.Detail(lnCtr).CheckPayment().getCheckDate());
                                detail_data.add(new ModelTableDetail(
                                        String.valueOf(OriginalRow),
                                        poController.Detail(lnCtr).Disbursement().getVoucherNo(),
                                        poController.Detail(lnCtr).CheckPayment().Banks().getBankName(),
                                        poController.Detail(lnCtr).CheckPayment().Payee().getPayeeName(),
                                        lsdate,
                                        poController.Detail(lnCtr).CheckPayment().getCheckNo(),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).CheckPayment().getAmount(), false),
                                        String.valueOf(lnCtr),
                                        "", ""));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(detail_data, pnDetail, 8); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= detail_data.size()) {
                                if (!detail_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetail, 0);
                                    int lnRow = Integer.parseInt(detail_data.get(0).getIndex08());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetail, lnTempRow);
                                int lnRow = Integer.parseInt(detail_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex08());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableDetailJE = new JFXUtil.ReloadableTableTask(
                tblVwJournalDetails,
                journal_data,
                () -> {
                    Platform.runLater(() -> {
                        journal_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.Journal().ReloadDetail();
                            }
                            String lsReportMonthYear = "";
                            String lsAcctCode = "";
                            String lsAccDesc = "";
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.Journal().getDetailCount(); lnCtr++) {
                                lsReportMonthYear = CustomCommonUtil.formatDateToShortString(poController.Journal().Detail(lnCtr).getForMonthOf());
                                lsAcctCode = poController.Journal().Detail(lnCtr).getAccountCode();
                                lsAccDesc = poController.Journal().Detail(lnCtr).Account_Chart().getDescription();
                                if (lsAcctCode == null) {
                                    lsAcctCode = "";
                                }
                                if (lsAccDesc == null) {
                                    lsAccDesc = "";
                                }
                                if (!poController.Journal().Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                lnRowCount += 1;
                                journal_data.add(
                                        new ModelJournalEntry_Detail(
                                                String.valueOf(lnRowCount),
                                                String.valueOf(CustomCommonUtil.parseDateStringToLocalDate(lsReportMonthYear, "yyyy-MM-dd")),
                                                String.valueOf(lsAcctCode),
                                                String.valueOf(lsAccDesc),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(lnCtr).getDebitAmount(), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(lnCtr).getCreditAmount(), true)),
                                                String.valueOf(lnCtr)
                                        ));

                                lsReportMonthYear = "";
                                lsAcctCode = "";
                                lsAccDesc = "";
                            }
                            int lnTempRow = JFXUtil.getDetailRow(journal_data, pnDetailJE, 07); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= journal_data.size()) {
                                if (!journal_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwJournalDetails, 0);
                                    int lnRow = Integer.parseInt(journal_data.get(0).getIndex07());
                                    pnDetailJE = lnRow;
                                    loadRecordDetailJE();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwJournalDetails, lnTempRow);
                                int lnRow = Integer.parseInt(journal_data.get(tblVwJournalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                                pnDetailJE = lnRow;
                                loadRecordDetailJE();
                            }
                            loadRecordMasterJE();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchBankAccountNo":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBankAccountNo("");
                        }
                        loadRecordSearch();
                        break;
                }

            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBankMaster":
                        if (lsValue.isEmpty()) {
                            poController.Master().setBanks("");
                            poController.Master().setBankAccount("");
                        }
                        break;
                    case "tfBankAccountNo":
                        if (lsValue.isEmpty()) {
                            poController.Master().setBankAccount("");
                        }
                        break;
                    case "tfBankAccountName":
                        if (lsValue.isEmpty()) {
                            poController.Master().setBankAccount("");
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfCheckTransNo":
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfCheckNo":
                        JFXUtil.inputIntegersOnly(tfCheckNo);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfNote":
                        poJSON = poController.Detail(pnDetail).setRemarks(lsValue.trim());
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEntered) {
                            JFXUtil.runWithDelay(0.50, () -> {
                                loadTableDetail.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    moveNext(false, true);
                                });
                                pbEntered = false;
                            });
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.Master().setRemarks(lsValue.trim());
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                    case "taJournalRemarks":
                        poJSON = poController.Journal().Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterJE();
                        break;
                }
            });
    ChangeListener<Boolean> txtDetailJE_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfAccountCode":
                        if (lsValue.isEmpty()) {
                            poController.Journal().Detail(pnDetailJE).setAccountCode("");
                            loadTableDetailJE.reload();
                        }
                        break;
                    case "tfAccountDescription":
                        if (lsValue.isEmpty()) {
                            poController.Journal().Detail(pnDetailJE).setAccountCode("");
                            loadTableDetailJE.reload();
                        }
                        break;
                    case "tfDebitAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.Journal().Detail(pnDetailJE).getCreditAmount() > 0.0000 && Double.parseDouble(lsValue) > 0) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                            poController.Journal().Detail(pnDetailJE).setDebitAmount(0.0000);
                            JFXUtil.textFieldMoveNext(tfDebitAmount);
                            break;
                        } else {
                            poJSON = poController.Journal().Detail(pnDetailJE).setDebitAmount((Double.parseDouble(lsValue)));
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.70, () -> {
                                    pnDetailJE = lnReturned;
                                    loadTableDetailJE.reload();
                                });
                                return;
                            } else {
                            }
                        }
                        break;
                    case "tfCreditAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.Journal().Detail(pnDetailJE).getDebitAmount() > 0.0000 && Double.parseDouble(lsValue) > 0) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                            poController.Journal().Detail(pnDetailJE).setCreditAmount(0.0000);
                            JFXUtil.textFieldMoveNext(tfCreditAmount);
                            break;
                        } else {
                            poJSON = poController.Journal().Detail(pnDetailJE).setCreditAmount((Double.parseDouble(lsValue)));
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.70, () -> {
                                    pnDetailJE = lnReturned;
                                    loadTableDetailJE.reload();
                                });
                                return;
                            }
                        }
                        if (pbEnteredJE) {
                            JFXUtil.runWithDelay(0.50, () -> {
                                loadTableDetailJE.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    moveNextJE(false, true);
                                });
                                pbEnteredJE = false;
                            });
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetailJE.reload();
                });
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = (lsTxtField.getText() == null ? "" : lsTxtField.getText()); //IMPORTANT: as searchRecord does not have null validator in searching instead ""
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        if (tfNote.isFocused()) {
                            pbEntered = true;
                        }
                        if (tfCreditAmount.isFocused()) {
                            pbEnteredJE = true;
                        }
                        CommonUtils.SetNextFocus(lsTxtField);
                        event.consume();
                        break;
                    case F3:
                        switch (txtFieldID) {
                            //apBrowse
                            case "tfSearchBankAccountNo":
                                poJSON = poController.SearchBankAccount(tfSearchBankAccountNo.getText(), true, true);
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                }
                                loadRecordSearch();
                                loadTableMain.reload();
                                break;
                            case "tfSearchTransNo":
                                if (!tooltipShown) {
                                    JFXUtil.showTooltip("NOTE: Results appear directly in the table view, no pop-up dialog.", tfSearchTransNo);
                                    tooltipShown = true;
                                }
                                loadTableMain.reload();
                                break;
                            //apMaster
                            case "tfBankMaster":
                                poJSON = poController.SearchBanks(lsValue, false, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfBankAccountNo);
                                }
                                loadRecordMaster();
                                break;
                            case "tfBankAccountNo":
                                poJSON = poController.SearchBankAccount(lsValue, true, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(taRemarks);
                                }
                                loadRecordMaster();
                                return;
                            case "tfBankAccountName":
                                poJSON = poController.SearchBankAccount(lsValue, false, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(taRemarks);
                                }
                                loadRecordMaster();
                                return;
                            case "tfCheckTransNo":
                                poJSON = poController.searchCheckPayment(lsValue, tfCheckNo.getText(), true);
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetail = lnReturned;
                                        loadTableDetail.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetail = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadTableDetail.reload();
                                }
                                return;
                            case "tfCheckNo":
                                poJSON = poController.searchCheckPayment(tfCheckNo.getText(), lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetail = lnReturned;
                                        loadTableDetail.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetail = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadTableDetail.reload();
                                }
                                return;
                            //apJournalDetails
                            case "tfAccountCode":
                                poJSON = poController.Journal().SearchAccountCode(pnDetailJE, lsValue, true, poController.Master().getIndustryId(), null);
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetailJE = lnReturned;
                                        loadTableDetailJE.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetailJE = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadTableDetailJE.reload();
                                    JFXUtil.textFieldMoveNext(tfDebitAmount);
                                }
                                break;
                            case "tfAccountDescription":
                                poJSON = poController.Journal().SearchAccountCode(pnDetailJE, lsValue, false, poController.Master().getIndustryId(), null);
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetailJE = lnReturned;
                                        loadTableDetailJE.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetailJE = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadTableDetailJE.reload();
                                    JFXUtil.textFieldMoveNext(tfDebitAmount);
                                }
                                break;
                        }
                        break;
                    case UP:
                        JFXUtil.altSwitch(txtFieldID, new Object[][]{
                            {new String[]{"tfCheckTransNo", "tfNote", "tfCheckNo"}, (Runnable) () -> moveNext(true, true)},
                            {new String[]{"tfAccountCode", "tfAccountDescription", "tfDebitAmount"}, (Runnable) () -> moveNextJE(true, true)}
                        });
                        event.consume();
                        break;
                    case DOWN:
                        JFXUtil.altSwitch(txtFieldID, new Object[][]{
                            {new String[]{"tfCheckTransNo", "tfNote", "tfCheckNo"}, (Runnable) () -> moveNext(false, true)},
                            {new String[]{"tfAccountCode", "tfAccountDescription", "tfDebitAmount"}, (Runnable) () -> moveNextJE(false, true)}
                        });
                        event.consume();
                        break;
                    default:
                        break;
                }
            } catch (SQLException | GuanzonException | ExceptionInInitializerError | CloneNotSupportedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks, taJournalRemarks);
        JFXUtil.setFocusListener(txtBrowse_Focus, apBrowse);
        JFXUtil.setFocusListener(txtMaster_Focus, apMaster);
        JFXUtil.setFocusListener(txtDetail_Focus, apDetail);
        JFXUtil.setFocusListener(txtDetailJE_Focus, apJournalDetails);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail, apJournalMaster, apJournalDetails, apTransaction);
        JFXUtil.setCommaFormatter(tfDebitAmount, tfCreditAmount);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail, tblVwJournalDetails, tblAttachments, tblViewMain);
        JFXUtil.adjustColumnForScrollbar(tblViewDetail, tblVwJournalDetails, tblAttachments, tblViewMain);
    }

    public void initTableOnClick() {
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                imageviewerutil.scaleFactor = 1.0;
                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                pnAttachment = lnRow;
                loadRecordAttachment(true);
                JFXUtil.resetImageBounds(imageView, stackPane1);
            }
        });

        tblViewDetail.setOnMouseClicked(event -> {
            if (!detail_data.isEmpty() && event.getClickCount() == 1) {
                ModelTableDetail selected = (ModelTableDetail) tblViewDetail.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(detail_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex08());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });

        tblViewMain.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                pnMain = tblViewMain.getSelectionModel().getSelectedIndex();
                if (pnMain >= 0) {
                    loadTableDetailFromMain();
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        tblVwJournalDetails.setOnMouseClicked(event -> {
            if (!journal_data.isEmpty() && event.getClickCount() == 1) {
                int lnRow = Integer.parseInt(journal_data.get(tblVwJournalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnDetailJE = lnRow;
                loadRecordDetailJE();
                moveNextJE(false, false);
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMain, item -> ((ModelTableMain) item).getIndex01(), highlightedRowsMain);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    if (!detail_data.isEmpty()) {
                        pnDetail = isMovedDown ? Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex08())
                                : Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex08());
                        loadRecordDetail();
                    }
                    break;
                case "tblVwJournalDetails":
                    if (journal_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(journal_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(journal_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailJE = newIndex;
                    loadRecordDetailJE();
                    break;
                case "tblAttachments":
                    if (attachment_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03())
                            : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                    pnAttachment = newIndex;
                    loadRecordAttachment(true);
                    break;
            }
        }
    };

    public void moveNext(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apDetail.requestFocus();
                pnDetail = isUp ? Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex08())
                        : Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex08());
            }
            loadRecordDetail();
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnDetail).getRemarks(), tfCheckTransNo},
                {poController.Detail(pnDetail).CheckPayment().getCheckNo(), tfCheckNo},
                {poController.Detail(pnDetail).getRemarks(), tfNote},}, tfNote); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void moveNextJE(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apJournalDetails.requestFocus();
                pnDetailJE = isUp ? Integer.parseInt(journal_data.get(JFXUtil.moveToPreviousRow(tblVwJournalDetails)).getIndex07())
                        : Integer.parseInt(journal_data.get(JFXUtil.moveToNextRow(tblVwJournalDetails)).getIndex07());
            }
            loadRecordDetailJE();
            if (pnDetailJE < 0 || pnDetailJE > poController.Journal().getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Journal().Detail(pnDetailJE).getAccountCode(), tfAccountCode},
                {poController.Journal().Detail(pnDetailJE).Account_Chart().getDescription(), tfAccountDescription}, // if null or empty, then requesting focus to the txtfield
                {poController.Journal().Detail(pnDetailJE).getDebitAmount(), tfDebitAmount},
                {poController.Journal().Detail(pnDetailJE).getCreditAmount(), tfCreditAmount},}, tfDebitAmount); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY);
        boolean lbShow3 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);

        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail, apJournalMaster, apJournalDetails, apAttachments);
        JFXUtil.setButtonsVisibility(true, btnRetrieve);
        JFXUtil.setButtonsVisibility(false, btnApprove, btnPrint);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case CheckDepositStatus.OPEN:
                JFXUtil.setButtonsVisibility(true, btnApprove);
                break;
            case CheckDepositStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnApprove);
                JFXUtil.setButtonsVisibility(true, btnPrint);
                break;
            case CheckDepositStatus.VOID:
            case CheckDepositStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid, btnApprove);
                break;
        }
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apTransaction, apMaster, apDetail, apJournalMaster, apJournalDetails, apAttachments);
    }
}
