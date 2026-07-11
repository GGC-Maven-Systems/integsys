/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.JournalProposalStatus;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;
import ph.com.guanzongroup.cas.cashflow.status.OtherPaymentStatus;
import ph.com.guanzongroup.integsys.model.ModelBIR_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Detail;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.model.ModelJournalEntryProposal_Detail;
import ph.com.guanzongroup.integsys.model.ModelJournalEntryProposal_Main;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class DisbursementVoucher_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON, poJSONVAT;
    JFXUtil.StageManager stageView = new JFXUtil.StageManager();
    private static final int ROWS_PER_PAGE = 50;
    private int pnMain = 0;
    private int pnMainJEP = 0;
    private int pnDetail = 0;
    private int pnDetailJE = 0;
    private int pnDetailJEP = 0;
    private int pnDetailBIR = 0;
    private int pnAttachment = 0;
    private boolean pbIsCheckedBIRTab = false;
    private boolean pbIsCheckedAttachmentTab = false;
    private final String pxeModuleName = "Disbursement Voucher Confirmation";
    private DisbursementVoucher poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierPayeeId = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;
    Map<String, String> imageinfo_temp = new HashMap<>();
    private ObservableList<ModelDisbursementVoucher_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> journal_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntryProposal_Detail> journalproposal_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntryProposal_Main> journalproposalmain_data = FXCollections.observableArrayList();
    private ObservableList<ModelBIR_Detail> BIR_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean tooltipShown = false;
    private boolean pbEnteredDV = false;
    private boolean pbEnteredJE = false;
    private boolean pbEnteredJEP = false;
    private boolean pbEnteredBIR = false;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();

    JFXUtil.ReloadableTableTask loadTableMain, loadTableDetail, loadTableDetailJE, loadTableDetailJEP, loadTableMainJEP, loadTableDetailBIR, loadTableAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    ObservableList<String> cPaymentMode = FXCollections.observableArrayList(
            "CHECK", "CHECK DEPOSIT", "BANK TRANSFER", "DIGITAL PAYMENT");
    ObservableList<String> cDisbursementMode = FXCollections.observableArrayList("DELIVER", "PICK-UP");
    ObservableList<String> cPayeeType = FXCollections.observableArrayList("INDIVIDUAL", "CORPORATION");
    ObservableList<String> cClaimantType = FXCollections.observableArrayList("AUTHORIZED REPRESENTATIVE", "PAYEE");
    ObservableList<String> cCheckStatus = FXCollections.observableArrayList("FLOATING", "OPEN",
            "CLEARED  / POSTED", "CANCELLED", "STALED", "HOLD / STOP PAYMENT",
            "BOUNCED / DISCHONORED", "VOID");
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    ObservableList<JFXUtil.Status> statusJEP = FXCollections.observableArrayList(
            new JFXUtil.Status(JournalProposalStatus.OPEN, "OPEN"),
            new JFXUtil.Status(JournalProposalStatus.CONFIRMED, "CONFIRMED"),
            new JFXUtil.Status(JournalProposalStatus.POSTED, "POSTED"),
            new JFXUtil.Status(JournalProposalStatus.CANCELLED, "CANCELLED"),
            new JFXUtil.Status(JournalProposalStatus.VOID, "VOID"),
            new JFXUtil.Status(JournalProposalStatus.RETURNED, "RETURNED")
    );
    private int currentIndex = 0;
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    AnchorPane root = null;
    Scene scene = null;
    /* DV  & Journal */
    @FXML
    private ComboBox<JFXUtil.Status> cmbJournalProposalStatus;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMasterDetail, apDVMaster1, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apDVMaster2, apDVMaster3, apDVDetail, apJournalMaster, apJournalDetails, apJournalProposalList, apJournalProposalMaster, apJournalProposalDetails, apBIRDetail, apAttachments;
    @FXML
    private Label lblSource, lblDVTransactionStatus, lblJournalTransactionStatus;
    @FXML
    private TextField tfSearchIndustry, tfSearchTransaction, tfSearchSupplier, tfDVTransactionNo, tfSupplier, tfVoucherNo, tfBankNameCheck, tfBankAccountCheck, tfPayeeName, tfCheckNo, tfCheckAmount, tfAuthorizedPerson, tfBankNameBTransfer, tfBankAccountBTransfer, tfPaymentAmountBTransfer, tfSupplierBank, tfSupplierAccountNoBTransfer, tfBankTransReferNo, tfPaymentStatusBTransfer, tfBankNameOnlinePayment, tfBankAccountOnlinePayment, tfPaymentAmount, tfSupplierServiceName, tfSupplierAccountNo, tfPaymentReferenceNo, tfOnlinePaymentStatus, tfTotalAmount, tfVatableSales, tfVatAmountMaster, tfVatZeroRatedSales, tfVatExemptSales, tfLessWHTax, tfTotalNetAmount, tfAdvances, tfRefNoDetail, tfVatableSalesDetail, tfVatExemptDetail, tfVatZeroRatedSalesDetail, tfVatRateDetail, tfVatAmountDetail, tfPurchasedAmountDetail, tfNetAmountDetail, tfAdvancesDetail, tfSourceNoDetail, tfJournalTransactionNo, tfTotalDebitAmount, tfTotalCreditAmount, tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount, tfJournalProposalTransactionNo, tfTotalProposalDebitAmount, tfTotalProposalCreditAmount, tfJournalProposalBranch, tfJournalProposalDepartment, tfJournalProposalAccountCode, tfJournalProposalAccountDescription, tfJournalProposalDebitAmount, tfJournalProposalCreditAmount, tfBIRTransactionNo, tfTaxCode, tfParticular, tfBaseAmount, tfTaxRate, tfTotalTaxAmount, tfAttachmentNo, tfAttachmentSource;
    @FXML
    private Button btnUpdate, btnSave, btnCancel, btnConfirm, btnVoid, btnRetrieve, btnHistory, btnClose, btnUndo, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain, tabPanePaymentMode;
    @FXML
    private Tab tabDetails, tabCheck, tabBankTransfer, tabOnlinePayment, tabJournal, tabJournalProposal, tabBIR, tabAttachments;
    @FXML
    private DatePicker dpDVTransactionDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear, dpPeriodFrom, dpPeriodTo;
    @FXML
    private ComboBox cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbCheckStatus, cmbAttachmentType;
    @FXML
    private CheckBox chbkPrintByBank, chbkIsCrossCheck, chbkIsPersonOnly, chbkVatClassification, cbReverse, cbJEReverse, cbJEProposalReverse, cbBIRReverse, cbJEMasterProposalReverse;
    @FXML
    private TextArea taDVRemarks, taJournalRemarks, taJournalProposalRemarks;
    @FXML
    private TableView tblVwDetails, tblVwJournalDetails, tblVwJournalProposalList, tblVwJournalProposalDetails, tblVwBIRDetails, tblAttachments, tblViewMainList;
    @FXML
    private TableColumn tblDVRowNo, tblReferenceNo, tblTransactionTypeDetail, tblPurchasedAmount, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales, tblNetAmount, tblJournalRowNo, tblJournalReportMonthYear, tblJournalAccountCode, tblJournalAccountDescription, tblJournalDebitAmount, tblJournalCreditAmount, tblJournalProposalListRowNo, tblJournalProposalListTransNo, tblJournalProposalListBranch, tblJournalProposalListDepartment, tblJournalProposalListDebitAmt, tblJournalProposalListCreditAmt, tblJournalProposalRowNo, tblJournalProposalReportMonthYear, tblJournalProposalAccountCode, tblJournalProposalAccountDescription, tblJournalProposalDebitAmount, tblJournalProposalCreditAmount, tblBIRRowNo, tblBIRParticular, tblTaxCode, tblBaseAmount, tblTaxRate, tblTaxAmount, tblRowNoAttachment, tblFileNameAttachment, tblRowNo, tblSupplier, tblPaymentForm, tblTransDate, tblReferNo;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;
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
    ChangeListener<Scene> WindowKeyEvent = (obs, oldScene, newScene) -> {
        if (newScene != null) {
            setKeyEvent(newScene);
        }
    };

    public void TriggerWindowEvent() {
        root = (AnchorPane) AnchorMain;
        scene = root.getScene();
        if (scene != null) {
            setKeyEvent(scene);
        } else {
            root.sceneProperty().addListener(WindowKeyEvent);
        }
    }

    public void RemoveWindowEvent() {
        root.sceneProperty().removeListener(WindowKeyEvent);
        scene.setOnKeyPressed(null);
        stageAttachment.closeDialog();
    }

    private void setKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5) {

                if (JFXUtil.isObjectEqualTo(poController.getEditMode(), EditMode.ADDNEW, EditMode.READY, EditMode.UPDATE)) {
                    showAttachmentDialog();
                }
            }
            if (event.getCode() == KeyCode.F12) {
                LoginControllerHolder.getMainController().eventf12(LoginControllerHolder.getMainController().getTab());
            }
        }
        );
        scene.focusOwnerProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                if (newNode instanceof Button) {
                } else {
                    lastFocusedTextField.set(newNode);
                    previousSearchedTextField.set(null);
                }
            }
        });
    }

    public void showAttachmentDialog() {
        poJSON = new JSONObject();
        stageAttachment.closeDialog();
        if (poController.getTransactionAttachmentCount() <= 0) {
            ShowMessageFX.Warning(null, pxeModuleName, "No transaction attachment to load.");
            return;
        }
        Map<String, Pair<String, String>> data = new HashMap<>();
        data.clear();
        int lnCount = 0;
        for (int lnCtr = 0; lnCtr < poController.getTransactionAttachmentCount(); lnCtr++) {
            if (RecordStatus.INACTIVE.equals(poController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                continue;
            }
            lnCount += 1;
            data.put(String.valueOf(lnCount), new Pair<>(String.valueOf(poController.TransactionAttachmentList(lnCtr).getModel().getFileName()),
                    poController.TransactionAttachmentList(lnCtr).getModel().getDocumentType()));
        }
        AttachmentDialogController controller = new AttachmentDialogController();
        controller.setOpenedImage(pnAttachment);
        controller.addData(data);

        try {
            stageAttachment.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/AttachmentDialog.fxml"), controller, "Attachment Dialog", false, false, true);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new CashflowControllers(oApp, null).DisbursementVoucher();
            poJSON = new JSONObject();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initButtonsClickActions();
            initTextFields();
            initComboBoxes();
            initDatePicker();
            initDetailGrid();
            initMainGrid();
            initDetailJEGrid();
            initMainJEPGrid();
            initDetailJEPGrid();
            initDetailBIRGrid();
            initAttachmentsGrid();
            initTableOnClick();
            initTabPane();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initDVMasterTabs();
            initButton(pnEditMode);
            pagination.setPageCount(1);
            JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference

            Platform.runLater(() -> {
                try {
                    poController.Master().setIndustryID(psIndustryId);
                    poController.Master().setCompanyID(psCompanyId);
                    poController.setIndustryID(psIndustryId);
                    poController.setCompanyID(psCompanyId);
                    poController.setCategoryID(psCategoryId);
                    poController.Master().setBranchCode(oApp.getBranchCode());
                    poController.setTransactionStatus(DisbursementStatic.OPEN + DisbursementStatic.CONFIRMED + DisbursementStatic.RETURNED);
                    loadRecordSearch();
                    lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            });
            initAttachmentPreviewPane();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Disbursement Voucher":
                    if (pnEditMode == EditMode.UNKNOWN) {
                        pnDetailJE = 0;
                        pnDetailBIR = 0;
                    } else {
                        loadRecordMaster();
                    }
                    break;
                case "Journal":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apJournalDetails, apJournalMaster);
                        if (poController.Detail(0).getSourceNo() != null && !poController.Detail(0).getSourceNo().isEmpty()) {
                            populateJE();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
                            ShowMessageFX.Warning(null, pxeModuleName, "Please provide at least one valid disbursement detail to proceed.");
                        }
                    }
                    break;
                case "Journal Proposal":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apJournalProposalMaster, apJournalProposalDetails);
                        if (poController.Detail(0).getSourceNo() != null && !poController.Detail(0).getSourceNo().isEmpty()) {
                            populateJEP();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
                            ShowMessageFX.Warning(null, pxeModuleName, "Please provide at least one valid disbursement detail to proceed.");
                        }
                    }
                    break;
                case "BIR 2307":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apBIRDetail);
                        if (poController.Detail(0).getSourceNo() != null && !poController.Detail(0).getSourceNo().isEmpty()) {
                            pbIsCheckedBIRTab = true;
                            populateBIR();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
                            ShowMessageFX.Warning(null, pxeModuleName, "Please provide at least one valid disbursement detail to proceed.");
                        }
                    }
                    break;
                case "Attachments":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apAttachments);
                        if (poController.Detail(0).getSourceNo() != null && !poController.Detail(0).getSourceNo().isEmpty()) {
                            pbIsCheckedAttachmentTab = true;
                            try {
                                poController.loadAttachments();
                            } catch (GuanzonException | SQLException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            }
                            loadTableAttachment.reload();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
                            ShowMessageFX.Warning(null, pxeModuleName, "Please provide at least one valid disbursement detail to proceed.");
                        }
                    }
                    break;
            }
        });
        JFXUtil.onTabSelected(tabPanePaymentMode, tabTitle -> {
            try {
                switch (tabTitle) {
                    case "Check":
                        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                            poController.populateCheck();
                            loadRecordMasterCheck();
                        }
                        break;
                    case "Bank Transfer":
                        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                            poController.populateOtherPayment();
                            loadRecordMasterBankTransfer();
                        }
                        break;
                    case "Digital Payment":
                        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                            poController.populateOtherPayment();
                            loadRecordMasterOnlinePayment();
                        }
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        });

        JFXUtil.checkDisabledTabs(tabPanePaymentMode, tab -> {
            ShowMessageFX.Warning(null, pxeModuleName, "This tab has been disabled as only one option applies based on the selected payment form.");
            JFXUtil.glowOnce(cmbPaymentMode, "#FF8201");
        });
    }

    //Disables/ Enables tabs
    private void initDVMasterTabs() {
        boolean lbShow = JFXUtil.isObjectEqualTo(pnEditMode, EditMode.READY, EditMode.ADDNEW, EditMode.UPDATE);
        JFXUtil.setDisabled(true, tabCheck, tabOnlinePayment, tabBankTransfer);
        switch (poController.Master().getDisbursementType()) {
            case DisbursementStatic.DisbursementType.CHECK:
                JFXUtil.setDisabled(!lbShow, tabCheck);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Check");
                loadRecordMasterCheck();
                //must reset data of check
                break;
            case DisbursementStatic.DisbursementType.CHECK_DEPOSIT:
                JFXUtil.setDisabled(!lbShow, tabCheck);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Check");
                loadRecordMasterCheck();
                //must reset data of check
                break;
            case DisbursementStatic.DisbursementType.WIRED:
                JFXUtil.setDisabled(!lbShow, tabBankTransfer);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Bank Transfer");
                loadRecordMasterBankTransfer();
                //must reset data of btransfer
                break;
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                JFXUtil.setDisabled(!lbShow, tabOnlinePayment);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Digital Payment");
                loadRecordMasterOnlinePayment();
                //must reset data of online payment
                break;
            default:
                JFXUtil.setDisabled(false, tabCheck);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Check");
                break;
        }
        initButton(pnEditMode);
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnUpdate, btnSave, btnCancel, btnConfirm, btnVoid, btnRetrieve, btnHistory, btnClose, btnUndo, btnArrowRight, btnArrowLeft);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    String lsUserId = oApp.getUserID();
                    String lsPosition = poController.checkPosition(DisbursementStatic.CONFIRMED, lsUserId);
                    if (lsPosition == null || "".equals(lsPosition)) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized officer.");
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    //Recheck transaction status
                    poController.setForm(DisbursementStatic.CONFIRMED);
                    poJSON = poController.checkUpdateTransaction(false);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }

                    poJSON = poController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pbIsCheckedBIRTab = false;
                    pnEditMode = poController.getEditMode();
                    CustomCommonUtil.switchToTab(tabDetails, tabPaneMain);
                    loadTableDetail.reload();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apJournalProposalMaster, apJournalProposalDetails, apBrowse, apDVMaster1, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apDVDetail, apJournalDetails, apBIRDetail);
                    break;
                case "btnSave":
                    handleSaveWithLoading();
                    return;
                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?")) {
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
                        ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                    }
                    break;
                case "btnRetrieve":
                    loadTableMain.reload();
                    break;
                case "btnConfirm":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            if (poController.Master().getVATSale() > 0.0000 && !pbIsCheckedBIRTab) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Please check the BIR 2307 before confirming.");
                                return;
                            }

                            poJSON = poController.ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    String lsTransaction = "";
                    if (DisbursementStatic.OPEN.equals(poController.Master().getTransactionStatus())) {
                        lsTransaction = "void";
                    } else if (DisbursementStatic.CONFIRMED.equals(poController.Master().getTransactionStatus())) {
                        lsTransaction = "cancel";
                    } else {
                        lsTransaction = "cancel";
                    }
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsTransaction + " transaction?")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            switch (poController.Master().getTransactionStatus()) {
                                case DisbursementStatic.OPEN:
                                    poJSON = poController.VoidTransaction("");
                                    break;
                                case DisbursementStatic.CONFIRMED:
                                default:
                                    poJSON = poController.CancelTransaction("");
                                    break;
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                pnEditMode = poController.getEditMode();
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        stageAttachment.closeDialog();
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                case "btnUndo":
                    boolean lbFound = false;
                    for (int lnCtr = poController.getDetailCount() - 1; lnCtr >= 0; lnCtr--) {
                        if (poController.Detail(lnCtr).getEditMode() == EditMode.UPDATE) {
                            if (poController.Detail(lnCtr).getAmountApplied() == 0.0000) {
                                if (!ShowMessageFX.YesNo(null, "Undo Reversed item", "Are you sure you want to undo previously reversed item?")) {
                                    return;
                                }
                                poController.Detail(lnCtr).setAmountApplied(poController.Detail(lnCtr).getAmount());
                                pnDetail = lnCtr;
                                lbFound = true;
                                break;
                            }
                        }
                    }
                    if (!lbFound) {
                        ShowMessageFX.Information(null, pxeModuleName, "No Reversed item found");
                    }
                    loadTableDetail.reload();
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
            cmdReloadProcess(lsButton);
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void cmdReloadProcess(String lsButton) {
        if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVoid", "btnConfirm", "btnDVCancel")) {
            pbIsCheckedBIRTab = false;
            poController.resetTransaction();
            poController.Master().setIndustryID(psIndustryId);
            poController.Master().setCompanyID(psCompanyId);
            poController.Master().setSupplierClientID(psSupplierPayeeId);
            clearTextFields();
            JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
            pnEditMode = EditMode.UNKNOWN;
        }

        if (JFXUtil.isObjectEqualTo(lsButton, "btnRetrieve", "btnSearch", "btnUndo", "btnArrowRight", "btnArrowLeft", "btnHistory")) {
        } else {
            loadRecordMaster();
            loadTableDetail.reload();
            loadTableDetailJE.reload();
            loadTableDetailBIR.reload();
            loadTableAttachment.reload();
            loadTableMainJEP.reload();
            loadTableDetailJEP.reload();
        }
        initButton(pnEditMode);
        if (lsButton.equals("btnUpdate")) {
            moveNext(false, false);
        }
    }

    private Stage getOwnerStage() {
        return AnchorMain != null
                && AnchorMain.getScene() != null
                ? (Stage) AnchorMain.getScene().getWindow()
                : null;
    }

    private void handleSaveWithLoading() {
        try {
            AtomicReference<JSONObject> loProcessResult = new AtomicReference<>();
            AtomicReference<JSONObject> loOpenResultRef = new AtomicReference<>();

            // Recheck transaction status before dispatching save to the loading task.
            poController.setForm(DisbursementStatic.CONFIRMED);
            poJSON = poController.checkUpdateTransaction(false);
            if (!"success".equals(String.valueOf(poJSON.get("result")))) {
                ShowMessageFX.Warning(null, pxeModuleName, String.valueOf(poJSON.get("message")));
                return;
            }

            if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                return;
            }

            if (DisbursementStatic.CONFIRMED.equals(poController.Master().getTransactionStatus())
                    && !pbIsCheckedBIRTab
                    && poController.Master().getVATSale() > 0.0000) {
                ShowMessageFX.Warning(null, pxeModuleName, "Please check the BIR 2307 before saving.");
                return;
            }

            if (!DisbursementStatic.OPEN.equals(poController.Master().getTransactionStatus())) {
                poJSON = poController.callApproval();
                if (!"success".equals(String.valueOf(poJSON.get("result")))) {
                    ShowMessageFX.Warning(null, pxeModuleName, String.valueOf(poJSON.get("message")));
                    return;
                }

                if (!DisbursementStatic.RETURNED.equals(poController.Master().getTransactionStatus())) {
                    String lsUserId2 = String.valueOf(poJSON.get("sUserIDxx"));
                    if ("null".equals(lsUserId2) || "".equals(lsUserId2)) {
                        lsUserId2 = oApp.getUserID();
                    }

                    String lsPosition2 = poController.checkPosition(poController.Master().getTransactionStatus(), lsUserId2);
                    if (lsPosition2 == null || "".equals(lsPosition2)) {
                        ShowMessageFX.Warning(null, pxeModuleName, "User is not an authorized officer.");
                        return;
                    }
                }
            }

            JFXUtil.runWithLoading(
                    getOwnerStage(),
                    apButton,
                    () -> {
                        try {
                            JSONObject loSaveJSON = poController.SaveTransaction();
                            loProcessResult.set(loSaveJSON);

                            if ("success".equals(String.valueOf(loSaveJSON.get("result")))) {
                                JSONObject loOpenJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                                loOpenResultRef.set(loOpenJSON);
                            }
                        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            throw new RuntimeException(ex);
                        }
                    },
                    () -> {
                        JSONObject loSaveJSON = loProcessResult.get();
                        if (loSaveJSON == null) {
                            ShowMessageFX.Error(null, pxeModuleName, "Unable to save transaction.");
                            return;
                        }

                        if (!"success".equals(String.valueOf(loSaveJSON.get("result")))) {
                            ShowMessageFX.Warning(null, pxeModuleName, String.valueOf(loSaveJSON.get("message")));
                            return;
                        }

                        ShowMessageFX.Information(null, pxeModuleName, String.valueOf(loSaveJSON.get("message")));

                        JSONObject loOpenJSON = loOpenResultRef.get();
                        if (loOpenJSON != null && "success".equals(String.valueOf(loOpenJSON.get("result")))) {
                            pnEditMode = poController.getEditMode();
                        }

                        if (pnEditMode == EditMode.READY
                        && !DisbursementStatic.CONFIRMED.equals(poController.Master().getTransactionStatus())
                        && ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                            if (!pbIsCheckedBIRTab && poController.Master().getVATSale() > 0.0000) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Please check the BIR 2307 before confirming.");
                            } else {
                                try {
                                    poJSON = poController.ConfirmTransaction("");
                                    if ("error".equals(String.valueOf(poJSON.get("result")))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, String.valueOf(poJSON.get("message")));
                                    } else {
                                        ShowMessageFX.Information(null, pxeModuleName, String.valueOf(poJSON.get("message")));
                                        JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                    }
                                } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                }
                            }
                        }
                        pnEditMode = poController.getEditMode();
                        cmdReloadProcess("btnSave");
                    }
            );
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void populateBIR() {
        try {
            poJSON = new JSONObject();
            JFXUtil.clearTextFields(apBIRDetail);
            poJSON = poController.populateWithholdingTaxDeduction();
            if (JFXUtil.isJSONSuccess(poJSON)) {
                poController.setDefaultWithHoldingTax();
                loadTableDetailBIR.reload();
            } else {
                BIR_data.clear();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void populateJEP() {
        JFXUtil.clearTextFields(apJournalProposalMaster, apJournalProposalDetails);
        poController.getEditMode();
        Platform.runLater(() -> {
            loadTableMainJEP.reload();
            JFXUtil.runWithDelay(0.50, () -> {
                loadTableDetailJEP.reload();
            });
        });
    }

    private void populateJE() {
        try {
            poJSON = new JSONObject();
            JFXUtil.clearTextFields(apJournalMaster, apJournalDetails);
            poController.getEditMode();
            poController.loadTBJ();
            loadTableDetailJE.reload();
//            poJSON = poController.populateJournal();
//            if (JFXUtil.isJSONSuccess(poJSON)) {
//                loadTableDetailJE.reload();
//            } else {
//                journal_data.clear();
//            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();
        pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
        ModelDisbursementVoucher_Main selected = (ModelDisbursementVoucher_Main) tblViewMainList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                stageAttachment.closeDialog();
                String lsTransactionNo = selected.getIndex06();
                if (!JFXUtil.loadValidation(pnEditMode, pxeModuleName, poController.Master().getTransactionNo(), lsTransactionNo)) {
                    return;
                }
                pbIsCheckedBIRTab = false;
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                clearTextFields();
                JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
                poJSON = poController.OpenTransaction(lsTransactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    poController.resetTransaction();
                    poController.Master().setIndustryID(psIndustryId);
                    poController.Master().setCompanyID(psCompanyId);
                    pnEditMode = EditMode.UNKNOWN;
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                } else {
                    pnEditMode = poController.getEditMode();
                }
                Platform.runLater(() -> {
                    loadTableDetail.reload();
                });
                moveNext(false, false);
            } catch (CloneNotSupportedException | SQLException | ScriptException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void loadTableDetailFromMainJEP() {
        JFXUtil.clearTextFields(apJournalProposalMaster, apJournalProposalDetails);
        pnMainJEP = tblVwJournalProposalList.getSelectionModel().getSelectedIndex();
        loadRecordMasterJEP();
        loadTableDetailJEP.reload();
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            try {
                                main_data.clear();
                                JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                                poJSON = poController.loadTransactionList(tfSearchIndustry.getText(), tfSearchSupplier.getText(), tfSearchTransaction.getText(), DisbursementStatic.CONFIRMED);
                                int lnRowNo = 0;
                                if (poController.getMasterList().size() > 0) {
                                    for (int lnCtr = 0; lnCtr <= poController.getMasterList().size() - 1; lnCtr++) {
                                        String lsPaymentForm = "";
                                        lsPaymentForm = JFXUtil.setStatusValue(null, DisbursementStatic.DisbursementType.class, poController.getMaster(lnCtr).getDisbursementType());
                                        lnRowNo += 1;
                                        String lsSupplier = poController.getMaster(lnCtr).Payee().APClient().getCompanyName();
                                        if (lsSupplier == null || "".equals(lsSupplier)) {
                                            lsSupplier = poController.getMaster(lnCtr).Payee().getPayeeName();
                                        }
                                        main_data.add(new ModelDisbursementVoucher_Main(
                                                String.valueOf(lnRowNo),
                                                lsSupplier,
                                                lsPaymentForm,
                                                CustomCommonUtil.formatDateToShortString(poController.getMaster(lnCtr).getTransactionDate()),
                                                poController.getMaster(lnCtr).getVoucherNo(),
                                                poController.getMaster(lnCtr).getTransactionNo()
                                        ));
                                        if (poController.getMaster(lnCtr).getTransactionStatus().equals(DisbursementStatic.RETURNED)) {
                                            JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnRowNo), "#FAA0A0", highlightedRowsMain);
                                        }
                                        if (poController.getMaster(lnCtr).getTransactionStatus().equals(DisbursementStatic.CONFIRMED)) {
                                            JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnRowNo), "#C1E1C1", highlightedRowsMain);
                                        }
                                    }
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
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });

        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblVwDetails,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            pbEnteredDV = false;
                            details_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                                poJSON = poController.computeDetailFields(true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    details_data.clear();
                                }
                            }
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (poController.Detail(lnCtr).getSourceNo() != null && !"".equals(poController.Detail(lnCtr).getSourceNo())) {
                                    if (poController.Detail(lnCtr).getAmountApplied() == 0.0000 && poController.Detail(lnCtr).getEditMode() != EditMode.ADDNEW) {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelDisbursementVoucher_Detail(String.valueOf(lnRowCount),
                                                poController.getReferenceNo(lnCtr),
                                                poController.getSourceCodeDescription(poController.Detail(lnCtr).getSourceCode()),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmountApplied(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatSales(), true),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatAmount(), true)),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatRates(), false),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailZeroVat(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatExempt(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), true),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 11); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwDetails, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex11());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                } else {
                                    JFXUtil.clearTextFields(apMasterDetail);
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwDetails, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblVwDetails.getSelectionModel().getSelectedIndex()).getIndex11());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (CloneNotSupportedException ex) {
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
                        pbEnteredJE = false;
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
        loadTableMainJEP = new JFXUtil.ReloadableTableTask(
                tblVwJournalProposalList,
                journalproposalmain_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            journalproposalmain_data.clear();
                            try {
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    poController.ReloadJournalProposal();
                                }
                                for (int lnCtr = 0; lnCtr < poController.getJournalProposalList().size(); lnCtr++) {
                                    journalproposalmain_data.add(
                                            new ModelJournalEntryProposal_Main(
                                                    String.valueOf(lnCtr + 1),
                                                    poController.JournalProposal(lnCtr).Master().getTransactionNo(),
                                                    poController.JournalProposal(lnCtr).Master().Branch().getBranchName(),
                                                    poController.JournalProposal(lnCtr).Master().Department().getDescription(),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.JournalProposal(lnCtr).getTotalDebitAmount(), false),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.JournalProposal(lnCtr).getTotalCreditAmount(), false)
                                            ));

                                }
                                if (pnMainJEP < 0 || pnMainJEP
                                        >= journalproposalmain_data.size()) {
                                    if (!journalproposalmain_data.isEmpty()) {
                                        /* FOCUS ON FIRST ROW */
                                        JFXUtil.selectAndFocusRow(tblVwJournalProposalList, 0);
                                        pnMainJEP = tblVwJournalProposalList.getSelectionModel().getSelectedIndex();
                                    }
                                } else {
                                    /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                    JFXUtil.selectAndFocusRow(tblVwJournalProposalList, pnMainJEP);
                                }
                                loadRecordMasterJEP();
                            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            }
                        });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });
        loadTableDetailJEP = new JFXUtil.ReloadableTableTask(
                tblVwJournalProposalDetails,
                journalproposal_data,
                () -> {
                    Platform.runLater(() -> {
                        pbEnteredJEP = false;
                        journalproposal_data.clear();
                        try {
                            if (poController.getJournalProposalList() == null) {
                                return;
                            } else {
                                if (poController.getJournalProposalList().isEmpty()) {
                                    return;
                                }
                            }
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.JournalProposal(pnMainJEP).ReloadDetail();
                            }
                            String lsReportMonthYear = "";
                            String lsAcctCode = "";
                            String lsAccDesc = "";
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.JournalProposal(pnMainJEP).getDetailCount(); lnCtr++) {
                                lsReportMonthYear = CustomCommonUtil.formatDateToShortString(poController.JournalProposal(pnMainJEP).Detail(lnCtr).getForMonthOf());
                                lsAcctCode = poController.JournalProposal(pnMainJEP).Detail(lnCtr).getAccountCode();
                                lsAccDesc = poController.JournalProposal(pnMainJEP).Detail(lnCtr).Account_Chart().getDescription();
                                if (lsAcctCode == null) {
                                    lsAcctCode = "";
                                }
                                if (lsAccDesc == null) {
                                    lsAccDesc = "";
                                }
                                if (!poController.JournalProposal(pnMainJEP).Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                lnRowCount += 1;
                                journalproposal_data.add(
                                        new ModelJournalEntryProposal_Detail(
                                                String.valueOf(lnRowCount),
                                                String.valueOf(CustomCommonUtil.parseDateStringToLocalDate(lsReportMonthYear, "yyyy-MM-dd")),
                                                String.valueOf(lsAcctCode),
                                                String.valueOf(lsAccDesc),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.JournalProposal(pnMainJEP).Detail(lnCtr).getDebitAmount(), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.JournalProposal(pnMainJEP).Detail(lnCtr).getCreditAmount(), true)),
                                                String.valueOf(lnCtr)
                                        ));

                                lsReportMonthYear = "";
                                lsAcctCode = "";
                                lsAccDesc = "";
                            }
                            int lnTempRow = JFXUtil.getDetailRow(journalproposal_data, pnDetailJEP, 07); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= journalproposal_data.size()) {
                                if (!journalproposal_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwJournalProposalDetails, 0);
                                    int lnRow = Integer.parseInt(journalproposal_data.get(0).getIndex07());
                                    pnDetailJEP = lnRow;
                                    loadRecordDetailJEP();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwJournalProposalDetails, lnTempRow);
                                int lnRow = Integer.parseInt(journalproposal_data.get(tblVwJournalProposalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                                pnDetailJEP = lnRow;
                                loadRecordDetailJEP();
                            }
                            loadRecordMasterJEP();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableDetailBIR = new JFXUtil.ReloadableTableTask(
                tblVwBIRDetails,
                BIR_data,
                () -> {
                    Platform.runLater(() -> {
                        pbEnteredBIR = false;
                        BIR_data.clear();
                        int lnCtr;
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadWTDeductions();
                            }
                            int lnRowCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getWTaxDeductionsCount(); lnCtr++) {
                                if (poController.WTaxDeduction(lnCtr).getModel().isReverse()) {
                                    lnRowCount += 1;
                                    BIR_data.add(new ModelBIR_Detail(String.valueOf(lnRowCount),
                                            poController.WTaxDeduction(lnCtr).getModel().WithholdingTax().AccountChart().getDescription(),
                                            poController.WTaxDeduction(lnCtr).getModel().getTaxCode(),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(lnCtr).getModel().getBaseAmount(), false),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(lnCtr).getModel().WithholdingTax().getTaxRate(), false),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(lnCtr).getModel().getTaxAmount(), false),
                                            String.valueOf(lnCtr))
                                    );
                                }
                            }
                            int lnTempRow = JFXUtil.getDetailRow(BIR_data, pnDetailBIR, 7); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= BIR_data.size()) {
                                if (!BIR_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwBIRDetails, 0);
                                    int lnRow = Integer.parseInt(BIR_data.get(0).getIndex07());
                                    pnDetailBIR = lnRow;
                                    loadRecordDetailBIR();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwBIRDetails, lnTempRow);
                                int lnRow = Integer.parseInt(BIR_data.get(tblVwBIRDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                                pnDetailBIR = lnRow;
                                loadRecordDetailBIR();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

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
                }
        );
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

    public void initAttachmentsGrid() {
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);

        tblAttachments.setItems(attachment_data);
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

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblTransDate, tblReferNo);
        JFXUtil.setColumnLeft(tblSupplier, tblPaymentForm);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblDVRowNo, tblReferenceNo);
        JFXUtil.setColumnLeft(tblTransactionTypeDetail);
        JFXUtil.setColumnRight(tblPurchasedAmount, tblNetAmount, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwDetails);
        tblVwDetails.setItems(details_data);
    }

    private void initDetailJEGrid() {
        JFXUtil.setColumnCenter(tblJournalRowNo, tblJournalReportMonthYear);
        JFXUtil.setColumnLeft(tblJournalAccountCode, tblJournalAccountDescription);
        JFXUtil.setColumnRight(tblJournalDebitAmount, tblJournalCreditAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwJournalDetails);
        tblVwJournalDetails.setItems(journal_data);
    }

    private void initMainJEPGrid() {
        JFXUtil.setColumnCenter(tblJournalProposalListRowNo, tblJournalProposalListTransNo);
        JFXUtil.setColumnLeft(tblJournalProposalListBranch, tblJournalProposalListDepartment);
        JFXUtil.setColumnRight(tblJournalProposalListDebitAmt, tblJournalProposalListCreditAmt);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwJournalProposalList);
        tblVwJournalProposalList.setItems(journalproposalmain_data);
    }

    private void initDetailJEPGrid() {
        JFXUtil.setColumnCenter(tblJournalProposalListRowNo, tblJournalProposalListTransNo);
        JFXUtil.setColumnLeft(tblJournalProposalListBranch, tblJournalProposalListDepartment);
        JFXUtil.setColumnRight(tblJournalProposalListDebitAmt, tblJournalProposalListCreditAmt);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwJournalProposalDetails);
        tblVwJournalProposalDetails.setItems(journalproposal_data);
    }

    private void initDetailBIRGrid() {
        JFXUtil.setColumnCenter(tblBIRRowNo);
        JFXUtil.setColumnLeft(tblBIRParticular, tblTaxCode);
        JFXUtil.setColumnRight(tblBaseAmount, tblTaxRate, tblTaxAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwBIRDetails);
        tblVwBIRDetails.setItems(BIR_data);
    }

    private void initTableOnClick() {
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

        tblVwDetails.setOnMouseClicked(event -> {
            if (!details_data.isEmpty()) {
                ModelDisbursementVoucher_Detail selected = (ModelDisbursementVoucher_Detail) tblVwDetails.getSelectionModel().getSelectedItem();
                switch (event.getClickCount()) {
                    case 1:
                        if (selected != null) {
                            int lnRow = Integer.parseInt(details_data.get(tblVwDetails.getSelectionModel().getSelectedIndex()).getIndex11());
                            pnDetail = lnRow;
                            loadRecordDetail();
                            moveNext(false, false);
                        }
                        break;
                    case 2:
                        if (selected != null) {
                            int lnRow = Integer.parseInt(details_data.get(tblVwDetails.getSelectionModel().getSelectedIndex()).getIndex11());
                            pnDetail = lnRow;
                            loadDetailView();
                        }
                        break;
                }
            }
        });
        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0 && event.getClickCount() == 2) {
                loadTableDetailFromMain();
                initButton(pnEditMode);
            }
        }
        );
        tblVwJournalDetails.setOnMouseClicked(event -> {
            if (!journal_data.isEmpty() && event.getClickCount() == 1) {
                int lnRow = Integer.parseInt(journal_data.get(tblVwJournalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnDetailJE = lnRow;
                loadRecordDetailJE();
                moveNextJE(false, false);
            }
        }
        );
        tblVwJournalProposalList.setOnMouseClicked(event -> {
            pnMainJEP = tblVwJournalProposalList.getSelectionModel().getSelectedIndex();
            if (pnMainJEP >= 0 && event.getClickCount() == 1) {
                loadTableDetailFromMainJEP();
                moveNextJEPMain(false, false);
                initButton(pnEditMode);
            }
        });
        tblVwJournalProposalDetails.setOnMouseClicked(event -> {
            if (!journalproposal_data.isEmpty() && event.getClickCount() == 1) {
                int lnRow = Integer.parseInt(journalproposal_data.get(tblVwJournalProposalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnDetailJEP = lnRow;
                loadRecordDetailJEP();
                moveNextJEP(false, false);
            }
        }
        );
        tblVwBIRDetails.setOnMouseClicked(event -> {
            if (!BIR_data.isEmpty() && event.getClickCount() == 1) { // Detect single click (or use another condition for double click)
                int lnRow = Integer.parseInt(BIR_data.get(tblVwBIRDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnDetailBIR = lnRow;
                loadRecordDetailBIR();
                moveNextBIR(false, false);
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelDisbursementVoucher_Main) item).getIndex01(), highlightedRowsMain);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblVwDetails, tblVwJournalDetails, tblVwJournalProposalList, tblVwJournalProposalDetails, tblVwBIRDetails, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList, tblVwDetails, tblVwJournalDetails, tblVwBIRDetails, tblAttachments, tblVwJournalProposalList, tblVwJournalProposalDetails);
    }

    private void loadDetailView() {
        try {
            String lsSourceCode = poController.Detail(pnDetail).getSourceCode();
            String lsSourceNo = poController.Detail(pnDetail).getSourceNo();

            if (lsSourceCode == null || "".equals(lsSourceCode)) {
                return;
            }

            if (DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE.equals(lsSourceCode)) {
                lsSourceCode = poController.Detail(pnDetail).SOADetail().getSourceCode();
                lsSourceNo = poController.Detail(pnDetail).SOADetail().getSourceNo();
            }
            loadBySourceCode(lsSourceCode, lsSourceNo);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName())
                    .log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadBySourceCode(String fsSourceCode, String fsSourceNo) {
        poJSON = new JSONObject();
        stageView.closeDialog();

        switch (fsSourceCode) {
            case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                PaymentRequest_ViewController loPRFController = new PaymentRequest_ViewController();
                loPRFController.setGRider(oApp);
                loPRFController.setTransaction(fsSourceNo);
                showDialog("/ph/com/guanzongroup/integsys/views/PaymentRequest_View.fxml", loPRFController);
                break;
            case DisbursementStatic.SourceCode.AP_ADJUSTMENT:
                APPaymentAdjustment_ViewController loAPAdjController = new APPaymentAdjustment_ViewController();
                loAPAdjController.setGRider(oApp);
                loAPAdjController.setTransaction(fsSourceNo);
                showDialog("/ph/com/guanzongroup/integsys/views/APPaymentAdjustment_View.fxml", loAPAdjController);
                break;
            case DisbursementStatic.SourceCode.PO_RECEIVING:
                SIPosting_ViewController loSIPostingController = new SIPosting_ViewController();
                loSIPostingController.setGRider(oApp);
                loSIPostingController.setTransaction(fsSourceNo);
                showDialog("/ph/com/guanzongroup/integsys/views/SIPosting_View.fxml", loSIPostingController);
                break;
            case DisbursementStatic.SourceCode.PO_RETURN:
                POReturnPosting_ViewController loPOReturnPostingController = new POReturnPosting_ViewController();
                loPOReturnPostingController.setGRider(oApp);
                loPOReturnPostingController.setTransaction(fsSourceNo);
                showDialog("/ph/com/guanzongroup/integsys/views/POReturnPosting_View.fxml", loPOReturnPostingController);
                break;
            default:
                ShowMessageFX.Warning(null, pxeModuleName, "Failed to open detail form for source: " + poController.getSourceCodeDescription(fsSourceCode) + ". Please contact the system administrator.");
                break;
        }
    }

    private void showDialog(String fsSource, Object controller) {
        try {
            stageView.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource(fsSource), controller,
                    "Disbursement Dialog", true, true, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblVwDetails":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex11())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex11());
                    pnDetail = newIndex;
                    loadRecordDetail();
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
                case "tblVwJournalProposalList":
                    if (journalproposalmain_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
                    pnMainJEP = newIndex;
                    loadTableDetailFromMainJEP();
                    break;
                case "tblVwJournalProposalDetails":
                    if (journalproposal_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(journalproposal_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(journalproposal_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailJEP = newIndex;
                    loadRecordDetailJEP();
                    break;
                case "tblVwBIRDetails":
                    if (BIR_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(BIR_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(BIR_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailBIR = newIndex;
                    loadRecordDetailBIR();
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

    private void initTextFields() {
        //Initialize  TextField Focus
        JFXUtil.setFocusListener(txtSearch_Focus, tfSearchIndustry, tfSearchSupplier, tfSearchTransaction);
        JFXUtil.setFocusListener(txtArea_Focus, taDVRemarks, taJournalRemarks, taJournalProposalRemarks);
        //apDVMaster1
        JFXUtil.setFocusListener(txtMaster_Focus, tfSupplier);
        //apDVDetail
        JFXUtil.setFocusListener(txtDetail_Focus, tfPurchasedAmountDetail, tfVatExemptDetail);
        //apCheck
        JFXUtil.setFocusListener(txtMasterCheck_Focus, tfBankNameCheck, tfBankAccountCheck, tfPayeeName, tfAuthorizedPerson);
        // apMasterDVBTransfer
        JFXUtil.setFocusListener(txtMasterBankTransfer_Focus, tfBankNameBTransfer, tfBankAccountBTransfer);
        //apMasterDVOp
        JFXUtil.setFocusListener(txtMasterOnlinePayment_Focus, tfBankNameOnlinePayment, tfBankAccountOnlinePayment);
        //apJournalDetails
        JFXUtil.setFocusListener(txtDetailJE_Focus, tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount);
        JFXUtil.setFocusListener(txtBIRDetail_Focus, tfBaseAmount, tfTaxCode, tfParticular, tfTaxCode);
        //apJournalProposalDetails
        JFXUtil.setFocusListener(txtJournalProposalMaster_Focus, apJournalProposalMaster);
        JFXUtil.setFocusListener(txtJournalProposalDetails_Focus, apJournalProposalDetails);

        JFXUtil.setFocusListener(txtBIRDetail_Focus, tfBaseAmount, tfTaxCode, tfParticular, tfTaxCode);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apDVMaster1, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apDVMaster2, apBrowse, apJournalMaster, apJournalDetails, apJournalProposalMaster, apJournalProposalDetails, apBIRDetail);
        JFXUtil.setCommaFormatter(tfDebitAmount, tfCreditAmount, tfBaseAmount, tfCheckAmount, tfJournalProposalDebitAmount, tfJournalProposalCreditAmount);

        taJournalProposalRemarks.setOnKeyPressed(this::txtField_KeyPressed);
        JFXUtil.setCommaFormatter2(tfVatExemptDetail);
        JFXUtil.applyHoverTooltip("Undo Reverse", btnUndo);
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taDVRemarks);
            JFXUtil.setVerticalScroll(taJournalRemarks);
            JFXUtil.setVerticalScroll(taJournalProposalRemarks);
        });

        JFXUtil.handleDisabledNodeClick(apJournalProposalMaster, pnEditMode, nodeID -> {
            boolean lbStat = JFXUtil.isObjectEqualTo(poController.JournalProposal(pnMainJEP).Master().getTransactionStatus(),
                    JournalProposalStatus.VOID, JournalProposalStatus.CANCELLED);
            if (lbStat) {
                ShowMessageFX.Information(null, pxeModuleName, "Only the 'Proposal Reverse' checkbox can be edited.");
            }
        });
    }
    ChangeListener<Boolean> txtSearch_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                    case "tfSearchSupplier":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");
                            loadTableMain.reload();
                        }
                        break;
                    case "tfSearchTransaction":
                        if (lsValue.isEmpty()) {
                            loadTableMain.reload();
                        }
                        break;
                }
                loadRecordSearch();
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taDVRemarks":
                        poJSON = poController.Master().setRemarks(lsValue);
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
                    case "taJournalProposalRemarks":
                        poJSON = poController.JournalProposal(pnMainJEP).Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterJEP();
                        break;
                }
            });
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSupplier":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.setSearchClient("");
                                poController.setSearchPayee("");
                                if (!JFXUtil.isObjectEqualTo(poController.Master().getSupplierClientID(), null, "")
                                && !JFXUtil.isObjectEqualTo(poController.Master().getPayeeID(), null, "")) {
                                    if (poController.getDetailCount() > 1) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the supplier name?\nPlease note that this action will delete all Disbursement voucher details.\n\nDo you wish to proceed?") == true) {
                                                poController.removeDetails();
                                                if (JFXUtil.isObjectEqualTo(poController.Master().getDisbursementType(), DisbursementStatic.DisbursementType.CHECK, DisbursementStatic.DisbursementType.CHECK_DEPOSIT)) {
                                                    poController.CheckPayments().getModel().setPayeeID("");
                                                    loadRecordMasterCheck();
                                                }
                                                poController.Master().setSupplierClientID("");
                                                poController.Master().setPayeeID("");
                                                psSupplierPayeeId = "";
                                                loadTableDetail.reload();
                                            } else {
                                                loadRecordMaster();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }
                            poController.Master().setSupplierClientID("");
                            poController.Master().setPayeeID("");
                        }
                        break;
                }
                loadRecordMaster();
            });
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfVatExemptDetail":
                        double lnOldVal = poController.Detail(pnDetail).getDetailVatExempt();
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setDetailVatExempt(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }

                        if (poController.getEditMode() == EditMode.ADDNEW || poController.getEditMode() == EditMode.UPDATE) {
                            poJSON = poController.computeDetailFields(true);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                poController.Detail(pnDetail).setDetailVatExempt(lnOldVal);
                            } else {
                                poJSON = poController.computeFields(true);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Modifying to this value may result in invalid computation.\n"
                                            + "Correction may be required. Proceed?")) {
                                    } else {
                                        poController.Detail(pnDetail).setDetailVatExempt(lnOldVal);
                                    }
                                }
                            }

                        }

                        if (pbEnteredDV) {
                            pbEnteredDV = false;
                        }
                        break;
                    case "tfPurchasedAmountDetail":
                        lsValue = JFXUtil.removeComma(lsValue);
                        Double ldblOrigAmt = poController.Detail(pnDetail).getAmountApplied();
                        poJSON = poController.Detail(pnDetail).setAmountApplied(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poJSON = poController.computeFields(true);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            poController.Detail(pnDetail).setAmountApplied(0.00);
                            tfPurchasedAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmountApplied(), true));
                            return;
                        }
                        if (pbEnteredDV) {
                            moveNext(false, true);
                            pbEnteredDV = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });

    ChangeListener<Boolean> txtMasterCheck_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfBankNameCheck":
                        if (lsValue.isEmpty()) {
                            poController.CheckPayments().getModel().setBankID("");
                            poController.CheckPayments().getModel().setBankAcountID("");
                        }
                        break;
                    case "tfBankAccountCheck":
                        if (lsValue.isEmpty()) {
                            poController.CheckPayments().getModel().setBankAcountID("");
                        }
                        break;
                    case "tfPayeeName":
                        //Similar as the Supplier Name, if deleted; then?
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Master().getSupplierClientID(), null, "")
                                && !JFXUtil.isObjectEqualTo(poController.Master().getPayeeID(), null, "")) {
                                    if (poController.getDetailCount() > 1) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the supplier name?\nPlease note that this action will delete all Disbursement voucher details.\n\nDo you wish to proceed?") == true) {
                                                poController.removeDetails();
                                                if (JFXUtil.isObjectEqualTo(poController.Master().getDisbursementType(), DisbursementStatic.DisbursementType.CHECK, DisbursementStatic.DisbursementType.CHECK_DEPOSIT)) {
                                                    poController.CheckPayments().getModel().setPayeeID("");
                                                    loadRecordMasterCheck();
                                                }
                                                poController.Master().setSupplierClientID("");
                                                poController.Master().setPayeeID("");
                                                psSupplierPayeeId = "";
                                                loadTableDetail.reload();
                                            } else {
                                                loadRecordMaster();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }
                            poController.Master().setSupplierClientID("");
                            poController.Master().setPayeeID("");
                        }
                        break;
                    case "tfAuthorizedPerson":
                        poJSON = poController.CheckPayments().getModel().setAuthorize(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMasterCheck();
            });
    ChangeListener<Boolean> txtMasterBankTransfer_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfBankNameBTransfer":
                        if (lsValue.isEmpty()) {
                            poController.OtherPayments().getModel().setBankID("");
                            poController.OtherPayments().getModel().setBankAcountID("");
                        }
                        break;
                    case "tfBankAccountBTransfer":
                        if (lsValue.isEmpty()) {
                            poController.OtherPayments().getModel().setBankAcountID("");
                        }
                        break;
                    case "tfSupplierBank":
                        if (lsValue.isEmpty()) {

                        }
                        break;
                    case "tfSupplierAccountNoBTransfer":
                        poJSON = poController.CheckPayments().getModel().setAuthorize(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMasterBankTransfer();
            });
    ChangeListener<Boolean> txtMasterOnlinePayment_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    /*Lost Focus*/
                    switch (lsID) {
                        case "tfBankNameOnlinePayment":
                            if (lsValue.isEmpty()) {
                                poController.OtherPayments().getModel().setBankID("");
                                poController.OtherPayments().getModel().setBankAcountID("");
                            }
                            break;
                        case "tfBankAccountOnlinePayment":
                            if (lsValue.isEmpty()) {
                                poController.OtherPayments().getModel().setBankAcountID("");
                            }
                            break;
                        case "tfSupplierServiceName":
                            if (lsValue.isEmpty()) {
                                poController.OtherPayments().getModel().Banks().setBankCode("");
                            }
                            break;
                        case "tfSupplierAccountNo":
                            if (lsValue.isEmpty()) {
                                poController.OtherPayments().getModel().Bank_Account_Master().setAccountCode("");
                            }
                            break;
                    }
                    loadRecordMasterOnlinePayment();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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

    ChangeListener<Boolean> txtJournalProposalMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfJournalProposalBranch":
                        if (lsValue.isEmpty()) {
                            poController.JournalProposal(pnMainJEP).Master().setBranchCode("");
                            loadTableDetailJEP.reload();
                        }
                        break;
                    case "tfJournalProposalDepartment":
                        if (lsValue.isEmpty()) {
                            poController.JournalProposal(pnMainJEP).Master().setDepartmentId("");
                            loadTableDetailJEP.reload();
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableMainJEP.reload();
                });
            });

    ChangeListener<Boolean> txtJournalProposalDetails_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfJournalProposalAccountCode":
                        if (lsValue.isEmpty()) {
                            poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).setAccountCode("");
                        }
                        break;
                    case "tfJournalProposalAccountDescription":
                        if (lsValue.isEmpty()) {
                            poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).setAccountCode("");
                        }
                        break;
                    case "tfJournalProposalDebitAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getCreditAmount() > 0.0000
                        && Double.parseDouble(lsValue) > 0) {

                            ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                            poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).setDebitAmount(0.0000);
                            JFXUtil.textFieldMoveNext(tfJournalProposalDebitAmount);
                            break;
                        } else {
                            poJSON = poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP)
                                    .setDebitAmount((Double.parseDouble(lsValue)));

                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.70, () -> {
                                    pnDetailJEP = lnReturned;
                                    loadTableDetailJEP.reload();
                                });
                                return;
                            }
                        }
                        break;

                    case "tfJournalProposalCreditAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getDebitAmount() > 0.0000
                        && Double.parseDouble(lsValue) > 0) {

                            ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                            poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).setCreditAmount(0.0000);
                            JFXUtil.textFieldMoveNext(tfJournalProposalCreditAmount);
                            break;
                        } else {
                            poJSON = poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP)
                                    .setCreditAmount((Double.parseDouble(lsValue)));

                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.70, () -> {
                                    pnDetailJEP = lnReturned;
                                    loadTableDetailJEP.reload();
                                });
                                return;
                            }
                        }

                        if (pbEnteredJEP) {
                            JFXUtil.runWithDelay(0.50, () -> {
                                loadTableDetailJEP.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    moveNextJEP(false, true);
                                });
                                pbEnteredJEP = false;
                            });
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetailJEP.reload();
                    if (JFXUtil.isObjectEqualTo(lsID, "tfJournalProposalDebitAmount", "tfJournalProposalCreditAmount")) {
                        loadTableMainJEP.reload();
                    }
                });
            });
    ChangeListener<Boolean> txtBIRDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfBaseAmount":
                            if (JFXUtil.isObjectEqualTo(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().AccountChart().getDescription(), null, "")) {
                                if (poController.WTaxDeduction(pnDetailBIR).getModel().getBaseAmount() > 0) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "Particular is blank, unable to input value!");
                                }
                                break;
                            }
                            lsValue = JFXUtil.removeComma(lsValue);
                            poJSON = poController.WTaxDeduction(pnDetailBIR).getModel().setBaseAmount(Double.valueOf(lsValue));
                            if ("error".equals((String) poJSON.get("result"))) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setBaseAmount(0.0);
                                poController.WTaxDeduction(pnDetailBIR).getModel().setTaxAmount(0.0);
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }

                            poJSON = poController.computeTaxAmount();
                            if ("error".equals((String) poJSON.get("result"))) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setBaseAmount(0.0);
                                poController.WTaxDeduction(pnDetailBIR).getModel().setTaxAmount(0.0);
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }
                            if (pbEnteredBIR) {
                                moveNextBIR(false, true);
                                pbEnteredBIR = false;
                            }
                            break;
                        case "tfTaxCode":
                            if (lsValue.isEmpty()) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setTaxCode("");
                            }
                            break;
                        case "tfParticular":
                            if (lsValue.isEmpty()) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setTaxRateId("");
                            }
                            break;
                    }
                    JFXUtil.runWithDelay(0.50, () -> {
                        loadTableDetailBIR.reload();
                    });
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextInputControl txtInput = (TextInputControl) event.getSource();
        String lsID = txtInput.getId();
        String lsValue = txtInput.getText() == null ? "" : txtInput.getText();
        String lsBranchCode = "";
        String lsDeparment = "";
        poJSON = new JSONObject();
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        if (tfPurchasedAmountDetail.isFocused()) {
                            pbEnteredDV = true;
                        }
                        if (tfCreditAmount.isFocused()) {
                            pbEnteredJE = true;
                        }
                        if (tfJournalProposalCreditAmount.isFocused()) {
                            pbEnteredJEP = true;
                        }
                        if (tfBaseAmount.isFocused()) {
                            pbEnteredBIR = true;
                        }
                        if (txtInput instanceof TextField) {
                            TextField txtField = (TextField) txtInput;
                            CommonUtils.SetNextFocus(txtField);
                        }
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            //apBrowse?
                            case "tfSearchIndustry":
                                poJSON = poController.SearchIndustry(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            case "tfSearchTransaction":
                                if (!tooltipShown) {
                                    JFXUtil.showTooltip("NOTE: Results appear directly in the table view, no pop-up dialog.", tfSearchTransaction);
                                    tooltipShown = true;
                                }
                                loadTableMain.reload();
                                break;
                            case "tfSearchSupplier":
                                poJSON = poController.SearchSupplier(lsValue, false, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            //apMasterDV1
                            case "tfSupplier":
                                //implement auto reload in similar supplier in tableMain
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    if (poController.getDetailCount() > 1) {
                                        pbKeyPressed = true;
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the supplier name?\nPlease note that this action will delete all Disbursement voucher details.\n\nDo you wish to proceed?") == true) {
                                            poController.removeDetails();
                                            loadTableDetail.reload();
                                        } else {
                                            return;
                                        }
                                        pbKeyPressed = false;
                                    }
                                }

                                poJSON = poController.SearchSupplier(lsValue, false, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMaster();
                                    break;
                                }
                                psSupplierPayeeId = poController.Master().getSupplierClientID();
                                loadRecordMaster();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    loadTableMain.reload();
                                });
                                break;

                            //apMasterDVCheck
                            case "tfBankNameCheck":
                                poJSON = poController.SearchBanks(lsValue, false, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfBankAccountCheck);
                                }
                                loadRecordMasterCheck();
                                break;
                            case "tfBankAccountCheck":
                                poJSON = poController.SearchBankAccount(lsValue, poController.CheckPayments().getModel().getBankID(), false, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfPayeeName);
                                }
                                loadRecordMaster();
                                loadRecordMasterCheck();
                                break;
                            case "tfPayeeName":
                                poJSON = poController.SearchPayee(poController.Master().getSupplierClientID(), true, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMasterCheck();
                                    break;
                                }
                                psSupplierPayeeId = poController.Master().getSupplierClientID();
                                loadRecordMasterCheck();
                                break;
                            case "tfAuthorizedPerson":
                                poJSON = poController.CheckPayments().getModel().setAuthorize(lsValue);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                }
                                loadRecordMasterCheck();
                                break;

                            //apMasterDVBTransfer
                            case "tfBankNameBTransfer":
                                poJSON = poController.SearchBanks(lsValue, false, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfBankAccountBTransfer);
                                }
                                loadRecordMasterBankTransfer();
                                break;
                            case "tfBankAccountBTransfer":
                                poJSON = poController.SearchBankAccount(lsValue, poController.OtherPayments().getModel().getBankID(), false, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfSupplierBank);
                                }
                                loadRecordMasterBankTransfer();
                                break;
                            case "tfSupplierBank":
//                                poJSON = poController.SearchPayee(poController.Master().getSupplierClientID(), true, false);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                    loadRecordMasterCheck();
//                                    break;
//                                } else {
//                                    JFXUtil.textFieldMoveNext(tfSupplierAccountNoBTransfer);
//                                }
                                psSupplierPayeeId = poController.Master().getSupplierClientID();
                                loadRecordMasterBankTransfer();
                                break;
                            case "tfSupplierAccountNoBTransfer":
//                                poJSON = poController.CheckPayments().getModel().setAuthorize(lsValue);
//                                if (!JFXUtil.isJSONSuccess(poJSON)) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
//                                } else {
//                                    JFXUtil.textFieldMoveNext(tfSupplierAccountNoBTransfer);
//                                }
                                loadRecordMasterBankTransfer();
                                break;

                            //apMasterDVOp
                            case "tfBankNameOnlinePayment":
                                poJSON = poController.SearchBanks(lsValue, false, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfBankAccountOnlinePayment);
                                }
                                loadRecordMasterOnlinePayment();
                                break;
                            case "tfBankAccountOnlinePayment":
                                poJSON = poController.SearchBankAccount(lsValue, poController.OtherPayments().getModel().getBankID(), false, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfSupplierServiceName);
                                }
                                loadRecordMasterOnlinePayment();
                                break;
                            case "tfSupplierServiceName":
                                loadRecordMasterOnlinePayment();
                                break;
                            case "tfSupplierAccountNo":
                                loadRecordMasterOnlinePayment();
                                break;

                            //apDVDetail
                            //apJournalDetails
                            case "tfAccountCode":
                                poJSON = poController.Journal().SearchAccountCode(pnDetailJE, lsValue, true, poController.Master().getIndustryID(), null);
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
                                poJSON = poController.Journal().SearchAccountCode(pnDetailJE, lsValue, false, poController.Master().getIndustryID(), null);
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
                            //apJournalProposalMaster
                            case "tfJournalProposalBranch":
                                lsBranchCode = poController.JournalProposal(pnMainJEP).Master().getBranchCode();
                                lsDeparment = poController.JournalProposal(pnMainJEP).Master().getDepartmentId();
                                poJSON = poController.JournalProposal(pnMainJEP).SearchBranch(lsValue, false);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    poJSON = poController.checkJEPExistBranchDept(pnMainJEP, lsBranchCode, lsDeparment);
                                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                    }
                                    JFXUtil.textFieldMoveNext(tfJournalProposalDepartment);
                                }
                                loadTableMainJEP.reload();
                                break;
                            case "tfJournalProposalDepartment":
                                lsBranchCode = poController.JournalProposal(pnMainJEP).Master().getBranchCode();
                                lsDeparment = poController.JournalProposal(pnMainJEP).Master().getDepartmentId();
                                poJSON = poController.JournalProposal(pnMainJEP).SearchDepartment(lsValue, false, false);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    poJSON = poController.checkJEPExistBranchDept(pnMainJEP, lsBranchCode, lsDeparment);
                                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                    }
                                    JFXUtil.textFieldMoveNext(taJournalProposalRemarks);
                                }
                                loadTableMainJEP.reload();
                                break;
                            //apJournalProposalDetails
                            case "tfJournalProposalAccountCode":
                                poJSON = poController.JournalProposal(pnMainJEP).SearchAccountCode(pnDetailJEP, lsValue, true, poController.Master().getIndustryID(), null);
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetailJEP = lnReturned;
                                        loadTableDetailJEP.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetailJEP = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadRecordDetailJEP();
                                    JFXUtil.textFieldMoveNext(tfJournalProposalDebitAmount);
                                }
                                break;
                            case "tfJournalProposalAccountDescription":
                                poJSON = poController.JournalProposal(pnMainJEP).SearchAccountCode(pnDetailJEP, lsValue, false, poController.Master().getIndustryID(), null);
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetailJEP = lnReturned;
                                        loadTableDetailJEP.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetailJEP = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadRecordDetailJEP();
                                    JFXUtil.textFieldMoveNext(tfJournalProposalDebitAmount);
                                }
                                break;
                            //apBIRDetail
                            case "tfTaxCode":
                                poJSON = poController.SearchTaxCode(lsValue, pnDetailBIR, true);
                                if ("error".equals(poJSON.get("result"))) {
//                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                    JFXUtil.runWithDelay(0.50, () -> {
                                        loadTableDetailBIR.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    pnDetailBIR = lnReturned;
                                    loadTableDetailBIR.reload();
                                    JFXUtil.textFieldMoveNext(tfParticular);
                                }
                                break;
                            case "tfParticular":
                                poJSON = poController.SearchParticular(lsValue, pnDetailBIR, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    if (poJSON.get("row") == null) {
                                        return;
                                    }
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.50, () -> {
                                        pnDetailBIR = lnReturned;
                                        loadTableDetailBIR.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    pnDetailBIR = lnReturned;
                                    loadTableDetailBIR.reload();
                                    JFXUtil.textFieldMoveNext(tfBaseAmount);
                                }
                                break;
                        }
                        event.consume();
                        break;
                    case UP:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfPurchasedAmountDetail", "tfTaxCodeDetail", "tfParticularsDetail"}, (Runnable) () -> moveNext(true, true)},
                            {new String[]{"tfAccountCode", "tfAccountDescription", "tfCreditAmount"}, (Runnable) () -> moveNextJE(true, true)},
                            {new String[]{"tfJournalProposalAccountCode", "tfJournalProposalAccountDescription", "tfJournalProposalCreditAmount"}, (Runnable) () -> moveNextJEP(true, true)},
                            {new String[]{"tfJournalProposalBranch", "tfJournalProposalDepartment", "taJournalProposalRemarks"}, (Runnable) () -> moveNextJEPMain(true, true)},
                            {new String[]{"tfTaxCode", "tfParticular", "tfBaseAmount", "tfTaxRate"}, (Runnable) () -> moveNextBIR(true, true)}
                        });
                        event.consume();
                        break;
                    case DOWN:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfPurchasedAmountDetail", "tfTaxCodeDetail", "tfParticularsDetail"}, (Runnable) () -> moveNext(false, true)},
                            {new String[]{"tfAccountCode", "tfAccountDescription", "tfCreditAmount"}, (Runnable) () -> moveNextJE(false, true)},
                            {new String[]{"tfJournalProposalAccountCode", "tfJournalProposalAccountDescription", "tfJournalProposalCreditAmount"}, (Runnable) () -> moveNextJEP(false, true)},
                            {new String[]{"tfJournalProposalBranch", "tfJournalProposalDepartment", "taJournalProposalRemarks"}, (Runnable) () -> moveNextJEPMain(false, true)},
                            {new String[]{"tfTaxCode", "tfParticular", "tfBaseAmount", "tfTaxRate"}, (Runnable) () -> moveNextBIR(false, true)}
                        });
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

    public void moveNext(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDVDetail.requestFocus();
            pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblVwDetails)).getIndex11())
                    : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblVwDetails)).getIndex11());
        }
        loadRecordDetail();
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.Detail(pnDetail).getAmountApplied(), tfPurchasedAmountDetail},}, tfPurchasedAmountDetail); // default
    }

    public void moveNextJEP(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apJournalProposalDetails.requestFocus();
                pnDetailJEP = isUp ? Integer.parseInt(journalproposal_data.get(JFXUtil.moveToPreviousRow(tblVwJournalProposalDetails)).getIndex07())
                        : Integer.parseInt(journalproposal_data.get(JFXUtil.moveToNextRow(tblVwJournalProposalDetails)).getIndex07());
            }
            loadRecordDetailJEP();
            if (pnDetailJEP < 0 || pnDetailJEP > poController.JournalProposal(pnMainJEP).getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getAccountCode(), tfJournalProposalAccountCode},
                {poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).Account_Chart().getDescription(), tfJournalProposalAccountDescription}, // if null or empty, then requesting focus to the txtfield
                {poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getDebitAmount(), tfJournalProposalDebitAmount},
                {poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getCreditAmount(), tfJournalProposalCreditAmount},}, tfJournalProposalCreditAmount); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void moveNextJEPMain(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apJournalProposalMaster.requestFocus();
            pnMainJEP = isUp ? JFXUtil.moveToPreviousRow(tblVwJournalProposalList) : JFXUtil.moveToNextRow(tblVwJournalProposalList);
        }
        loadTableDetailFromMainJEP();
        if (pnMainJEP < 0 || pnMainJEP > poController.getJournalProposalList().size()) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.JournalProposal(pnMainJEP).Master().getBranchCode(), tfJournalProposalBranch},
            {poController.JournalProposal(pnMainJEP).Master().getDepartmentId(), tfJournalProposalDepartment},
            {poController.JournalProposal(pnMainJEP).Master().getRemarks(), taJournalProposalRemarks},}, taJournalProposalRemarks); // default
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
                {poController.Journal().Detail(pnDetailJE).getCreditAmount(), tfCreditAmount},}, tfCreditAmount); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void moveNextBIR(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apBIRDetail.requestFocus();
                pnDetailBIR = isUp ? Integer.parseInt(BIR_data.get(JFXUtil.moveToPreviousRow(tblVwBIRDetails)).getIndex07())
                        : Integer.parseInt(BIR_data.get(JFXUtil.moveToNextRow(tblVwBIRDetails)).getIndex07());
            }
            loadRecordDetailBIR();
            if (pnDetailBIR < 0 || pnDetailBIR > poController.getWTaxDeductionsCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.WTaxDeduction(pnDetailBIR).getModel().getTaxCode(), tfTaxCode},
                {poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().AccountChart().getDescription(), tfParticular}, // if null or empty, then requesting focus to the txtfield
                {poController.WTaxDeduction(pnDetailBIR).getModel().getBaseAmount(), tfBaseAmount},}, tfBaseAmount); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordSearch() {
        tfSearchIndustry.setText(poController.getSearchIndustry());
        tfSearchSupplier.setText(poController.getSearchPayee());
        JFXUtil.updateCaretPositions(apBrowse);
    }

    private void loadRecordMaster() {
        try {
            initDVMasterTabs();
            poController.computeFields(false);

            String lsTransaction = "";
            if (DisbursementStatic.OPEN.equals(poController.Master().getTransactionStatus())) {
                lsTransaction = "Void";
                btnVoid.setText(lsTransaction);
            } else if (DisbursementStatic.CONFIRMED.equals(poController.Master().getTransactionStatus())) {
                lsTransaction = "Cancel";
                btnVoid.setText(lsTransaction);
            } else {
                lsTransaction = "Cancel";
                btnVoid.setText(lsTransaction);
            }

            JFXUtil.setStatusValue(lblDVTransactionStatus, DisbursementStatic.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            JFXUtil.setDisabled(true, tfSupplier);
            tfDVTransactionNo.setText(poController.Master().getTransactionNo() != null ? poController.Master().getTransactionNo() : "");
            dpDVTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            JFXUtil.setCmbValue(cmbPaymentMode, !poController.Master().getDisbursementType().equals("") ? Integer.valueOf(poController.Master().getDisbursementType()) : -1);
            tfVoucherNo.setText(poController.Master().getVoucherNo());
//            tfSupplier.setText(poController.Master().Payee().Client().getCompanyName() != null ? poController.Master().Payee().Client().getCompanyName() : "");
            String lsSupplier = poController.Master().Payee().APClient().getCompanyName();
            if (lsSupplier == null || "".equals(lsSupplier)) {
                lsSupplier = poController.Master().Payee().getPayeeName();
            }
            tfSupplier.setText(lsSupplier == null ? "" : lsSupplier);
            tfVatAmountMaster.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATAmount(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATExmpt(), true));
            tfLessWHTax.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getWithTaxTotal(), true));
            tfTotalNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getNetTotal(), true));
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotal(), true));
            tfVatZeroRatedSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getZeroVATSales(), true));
            tfVatableSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATSale(), true));
            taDVRemarks.setText(poController.Master().getRemarks());
            tfAdvances.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getAdvancesTotal(), true));
            JFXUtil.updateCaretPositions(apDVMaster1, apDVMaster2, apDVMaster3);
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
            boolean lbNotNull = !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getDetailVatAmount(), null, "");
            boolean lbNotZero = poController.Detail(pnDetail).getAmountApplied() != 0;
            cbReverse.selectedProperty().set(lbNotNull && lbNotZero);

            boolean lbShow = (poController.Detail(pnDetail).getSourceCode()).equals(DisbursementStatic.SourceCode.PAYMENT_REQUEST)
                    || (poController.Detail(pnDetail).getSourceCode()).equals(DisbursementStatic.SourceCode.AP_ADJUSTMENT)
                    || (poController.Detail(pnDetail).getSourceCode().equals(DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE)
                    && (poController.Detail(pnDetail).SOADetail().getSourceCode().equals(DisbursementStatic.SourceCode.AP_ADJUSTMENT)
                    || poController.Detail(pnDetail).SOADetail().getSourceCode().equals(DisbursementStatic.SourceCode.PAYMENT_REQUEST)));
            JFXUtil.setDisabled(!lbShow, chbkVatClassification, tfVatExemptDetail);

            JFXUtil.setDisabled(!lbShow, chbkVatClassification, tfVatExemptDetail);

            tfRefNoDetail.setText(poController.getReferenceNo(pnDetail));
            tfSourceNoDetail.setText(poController.getSourceNo(pnDetail));
            chbkVatClassification.setSelected(poController.Detail(pnDetail).isWithVat());
            tfVatableSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatSales(), true));
            tfVatExemptDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatExempt(), true));
            tfVatZeroRatedSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailZeroVat(), true));
            tfVatAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatAmount(), true));
            tfVatRateDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatRates(), false));
            tfPurchasedAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmountApplied(), true));
            tfNetAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount(), true));
            tfAdvancesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailAdvances(), true));
            JFXUtil.updateCaretPositions(apDVDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMasterCheck() {
        try {

            JFXUtil.setDisabled(true, tfCheckNo, tfCheckAmount);
            tfCheckNo.setText(poController.CheckPayments().getModel().getCheckNo());
//            if (JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getCheckNo(), null, "")) {
//                poController.CheckPayments().getModel().setCheckDate(null);
//            }
            dpCheckDate.setValue(poController.CheckPayments().getModel().getCheckDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);

            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.CheckPayments().getModel().getAmount(), true));
            chbkIsCrossCheck.setSelected(poController.CheckPayments().getModel().isCross());
            chbkIsPersonOnly.setSelected(poController.CheckPayments().getModel().isPayee());
            tfBankNameCheck.setText(poController.CheckPayments().getModel().Banks().getBankName() != null ? poController.CheckPayments().getModel().Banks().getBankName() : "");
            tfBankAccountCheck.setText(JFXUtil.isObjectEqualTo(poController.Master().getDisbursementType(), DisbursementStatic.DisbursementType.CHECK, DisbursementStatic.DisbursementType.CHECK_DEPOSIT)
                    ? (poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null
                    ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "") : "");
            chbkPrintByBank.setSelected(poController.Master().getBankPrint().equals(Logical.YES));

            tfPayeeName.setText(poController.Master().Payee().getPayeeName() != null ? poController.Master().Payee().getPayeeName() : "");
            JFXUtil.setCmbValue(cmbPayeeType, !poController.CheckPayments().getModel().getPayeeType().equals("") ? Integer.valueOf(poController.CheckPayments().getModel().getPayeeType()) : -1);
            JFXUtil.setCmbValue(cmbDisbursementMode, !poController.CheckPayments().getModel().getDesbursementMode().equals("") ? Integer.valueOf(poController.CheckPayments().getModel().getDesbursementMode()) : -1);
            JFXUtil.setCmbValue(cmbClaimantType, !poController.CheckPayments().getModel().getClaimant().equals("") ? Integer.valueOf(poController.CheckPayments().getModel().getClaimant()) : -1);

            tfAuthorizedPerson.setText(poController.CheckPayments().getModel().getAuthorize() != null ? poController.CheckPayments().getModel().getAuthorize() : "");
            JFXUtil.setCmbValue(cmbCheckStatus, !poController.CheckPayments().getModel().getTransactionStatus().equals("") ? Integer.valueOf(poController.CheckPayments().getModel().getTransactionStatus()) : -1);

            boolean lbValidation01 = poController.Master().getBankPrint().equals(Logical.YES);
            JFXUtil.setDisabled(!lbValidation01, cmbPayeeType, cmbDisbursementMode);

            boolean lbValidation02 = poController.Master().getBankPrint().equals(Logical.YES) && poController.CheckPayments().getModel().getDesbursementMode().equals("1");
            JFXUtil.setDisabled(!lbValidation02, cmbClaimantType);

            boolean lbValidation03 = poController.Master().getBankPrint().equals(Logical.YES)
                    && poController.CheckPayments().getModel().getDesbursementMode().equals("1")
                    && poController.CheckPayments().getModel().getClaimant().equals("0");
            JFXUtil.setDisabled(!lbValidation03, tfAuthorizedPerson);

            JFXUtil.updateCaretPositions(apMasterDVCheck);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMasterBankTransfer() {
        try {
            JFXUtil.setStatusValue(tfPaymentStatusBTransfer, OtherPaymentStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.OtherPayments().getModel().getTransactionStatus());
            tfBankNameBTransfer.setText(poController.OtherPayments().getModel().Banks().getBankName() != null ? poController.OtherPayments().getModel().Banks().getBankName() : "");
            tfBankAccountBTransfer.setText(poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            tfPaymentAmountBTransfer.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.OtherPayments().getModel().getTotalAmount(), true));

            if (true) {
                return; //temporarily as there is no getTotalAmount yet
            }
            tfSupplierBank.setText(poController.CheckPayments().getModel().Supplier().getCompanyName() != null ? poController.CheckPayments().getModel().Supplier().getCompanyName() : "");
            tfSupplierAccountNoBTransfer.setText(poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            tfBankTransReferNo.setText(poController.OtherPayments().getModel().getReferNox() != null ? poController.OtherPayments().getModel().getReferNox() : "");
            JFXUtil.updateCaretPositions(apMasterDVBTransfer);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMasterOnlinePayment() {
        try {
            JFXUtil.setStatusValue(tfOnlinePaymentStatus, OtherPaymentStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.OtherPayments().getModel().getTransactionStatus());
            tfBankNameOnlinePayment.setText(poController.OtherPayments().getModel().Banks().getBankName() != null ? poController.OtherPayments().getModel().Banks().getBankName() : "");
            tfBankAccountOnlinePayment.setText(poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            tfPaymentAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.OtherPayments().getModel().getTotalAmount(), true));
            if (true) {
                return;
            }
            tfSupplierServiceName.setText(poController.OtherPayments().getModel().Banks().getBankName() != null ? poController.OtherPayments().getModel().Banks().getBankName() : "");
            tfSupplierAccountNo.setText(poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            tfPaymentReferenceNo.setText(poController.OtherPayments().getModel().getReferNox() != null ? poController.OtherPayments().getModel().getReferNox() : "");
            JFXUtil.updateCaretPositions(apMasterDVOp);
        } catch (SQLException | GuanzonException ex) {
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

    private void loadRecordMasterJEP() {
        try {
            if (poController.getJournalProposalList() == null) {
                return;
            } else {
                if (poController.getJournalProposalList().isEmpty()) {
                    return;
                }
            }
            boolean lbShow = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
            String dbValue = poController.JournalProposal(pnMainJEP).Master().getTransactionStatus();
            boolean lbStat = JFXUtil.isObjectEqualTo(dbValue, JournalProposalStatus.VOID, JournalProposalStatus.CANCELLED);
            if (lbStat) {
                JFXUtil.setDisabledExcept(true, apJournalProposalMaster, cbJEMasterProposalReverse);
                JFXUtil.setDisabled(true, apJournalProposalDetails);
            } else {
                JFXUtil.setDisabledExcept(!lbShow, apJournalProposalMaster);
                JFXUtil.setDisabled(true, tfJournalProposalTransactionNo, dpJournalProposalTransactionDate);
                JFXUtil.setDisabled(!lbShow, apJournalProposalDetails);
            }
            JFXUtil.setDisabled(true, cmbJournalProposalStatus);

            //for hiding purposes
//            filteredStatuses.setPredicate(status -> true); //reshow all cmb values
//            if (lbEditMode) {
//            } else {
//                switch (dbValue) {
//                    case JournalProposalStatus.OPEN:
////                        filteredStatuses.setPredicate(status
////                                -> !JournalProposalStatus.CANCELLED.equals(status.getCode())
////                                && !JournalProposalStatus.POSTED.equals(status.getCode())
////                                && !JournalProposalStatus.RETURNED.equals(status.getCode())
////                                && !JournalProposalStatus.VOID.equals(status.getCode())
////                        );
//                        break;
//                    case JournalProposalStatus.CONFIRMED:
////                        filteredStatuses.setPredicate(status
////                                -> !JournalProposalStatus.VOID.equals(status.getCode())
////                                && !JournalProposalStatus.POSTED.equals(status.getCode())
////                                && !JournalProposalStatus.RETURNED.equals(status.getCode())
////                                && !JournalProposalStatus.OPEN.equals(status.getCode())
////                        );
//                        break;
//                }
//            }
            statusJEP.stream()
                    .filter(s -> s.getCode().equals(dbValue))
                    .findFirst()
                    .ifPresent(cmbJournalProposalStatus::setValue);

            tfJournalProposalTransactionNo.setText(poController.JournalProposal(pnMainJEP).Master().getTransactionNo());
            dpJournalProposalTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.JournalProposal(pnMainJEP).Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            double lnTotalDebit = 0;
            double lnTotalCredit = 0;
            for (int lnCtr = 0; lnCtr < poController.JournalProposal(pnMainJEP).getDetailCount(); lnCtr++) {
                if (!poController.JournalProposal(pnMainJEP).Detail(lnCtr).isReverse()) {
                    continue;
                }
                lnTotalDebit += poController.JournalProposal(pnMainJEP).Detail(lnCtr).getDebitAmount();
                lnTotalCredit += poController.JournalProposal(pnMainJEP).Detail(lnCtr).getCreditAmount();
            }
            tfTotalProposalDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDebit, true));
            tfTotalProposalCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalCredit, true));
            tfJournalProposalBranch.setText(poController.JournalProposal(pnMainJEP).Master().Branch().getBranchName());
            tfJournalProposalDepartment.setText(poController.JournalProposal(pnMainJEP).Master().Department().getDescription());
            taJournalProposalRemarks.setText(poController.JournalProposal(pnMainJEP).Master().getRemarks());
            cbJEMasterProposalReverse.setSelected(poController.JournalProposal(pnMainJEP).Master().isReverse());
            JFXUtil.updateCaretPositions(apJournalProposalMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetailJEP() {
        try {
            if (pnDetailJEP < 0 || pnDetailJEP > poController.JournalProposal(pnMainJEP).getDetailCount() - 1) {
                return;
            }
            boolean lbShow = poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getEditMode() == EditMode.UPDATE;
            JFXUtil.setDisabled(lbShow, tfJournalProposalAccountCode, tfJournalProposalAccountDescription);

            tfJournalProposalAccountCode.setText(poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getAccountCode());
            tfJournalProposalAccountDescription.setText(poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).Account_Chart().getDescription());
            String lsReportMonth = CustomCommonUtil.formatDateToShortString(poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getForMonthOf());
            JFXUtil.setDateValue(dpJournalProposalReportMonthYear, CustomCommonUtil.parseDateStringToLocalDate(lsReportMonth, "yyyy-MM-dd"));
            tfJournalProposalDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getDebitAmount(), true));
            tfJournalProposalCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getCreditAmount(), true));
            cbJEProposalReverse.setSelected(poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).isReverse());

            JFXUtil.updateCaretPositions(apJournalProposalDetails);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetailBIR() {
        try {
            if (pnDetailBIR < 0 || pnDetailBIR > poController.getWTaxDeductionsCount() - 1) {
                return;
            }
            boolean lbShow = poController.WTaxDeduction(pnDetailBIR).getModel().getEditMode() == EditMode.UPDATE;
            JFXUtil.setDisabled(lbShow, tfTaxCode, tfParticular);

            tfBIRTransactionNo.setText(poController.WTaxDeduction(pnDetailBIR).getModel().getTransactionNo());
            String lsPeriodFromDate = CustomCommonUtil.formatDateToShortString(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodFrom());
            dpPeriodFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsPeriodFromDate, "yyyy-MM-dd"));
            String lsPeriodToDate = CustomCommonUtil.formatDateToShortString(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodTo());
            dpPeriodTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsPeriodToDate, "yyyy-MM-dd"));
            tfTaxCode.setText(poController.WTaxDeduction(pnDetailBIR).getModel().getTaxCode());
            tfParticular.setText(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().AccountChart().getDescription());
            tfBaseAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().getBaseAmount(), false));
            tfTaxRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().getTaxRate()));
            tfTotalTaxAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().getTaxAmount(), false));
            cbBIRReverse.setSelected(poController.WTaxDeduction(pnDetailBIR).getModel().isReverse());

            JFXUtil.updateCaretPositions(apBIRDetail);
        } catch (SQLException | GuanzonException ex) {
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
                tfAttachmentSource.setText(poController.TransactionAttachmentSource(pnAttachment));
                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";
                        if (imageinfo_temp.containsKey((String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02())) {
                            filePath2 = imageinfo_temp.get((String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02());
                        } else {
                            // in server
                            if (poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                                filePath2 = poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            } else {
                                filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            }
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
                    // Clear previous content
                    stackPane1.getChildren().clear();
                    // Add ImageView directly to stackPane
                    stackPane1.getChildren().add(imageView);
                    stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);
                    Platform.runLater(() -> JFXUtil.stackPaneClip(stackPane1));
                    pnAttachment = 0;
                }
            }
        } catch (Exception ex) {
        }
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbPaymentMode":
                        poController.Master().setOldDisbursementType(poController.Master().getDisbursementType());
                        poJSON = poController.Master().setDisbursementType(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                    case "cmbPayeeType":
                        poJSON = poController.CheckPayments().getModel().setPayeeType(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterCheck();
                        break;
                    case "cmbDisbursementMode":
                        //define if claimant is not empty or the authorized person
                        if (!JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getClaimant(), null, "")
                        || !JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getAuthorize(), null, "")) {
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Claimant Type is not empty, \n"
                                    + "Are you sure you want to change Disbursement Type?")) {
                                poController.CheckPayments().getModel().setClaimant("");
                                poController.CheckPayments().getModel().setAuthorize("");
                                loadRecordMasterCheck();
                            } else {
                                loadRecordMasterCheck();
                                return;
                            }
                        }
                        poJSON = poController.CheckPayments().getModel().setDesbursementMode(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterCheck();
                        break;
                    case "cmbClaimantType":
                        if (!JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getAuthorize(), null, "")) {
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Authorized Person is not Empty, \n"
                                    + "Are you sure you want to change Claimant Type?")) {
                                poController.CheckPayments().getModel().setAuthorize("");
                                loadRecordMasterCheck();
                                return;
                            } else {
                                loadRecordMasterCheck();
                                return;
                            }
                        }
                        poJSON = poController.CheckPayments().getModel().setClaimant(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterCheck();
                        break;
                    case "cmbJournalProposalStatus":
                        JFXUtil.Status selectedstat = cmbJournalProposalStatus.getValue();
                        poJSON = poController.JournalProposal(pnMainJEP).Master().setTransactionStatus(selectedstat.getCode());
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                }
            }
    );

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(cPaymentMode, cmbPaymentMode), new JFXUtil.Pairs<>(cPayeeType, cmbPayeeType),
                new JFXUtil.Pairs<>(cDisbursementMode, cmbDisbursementMode), new JFXUtil.Pairs<>(cClaimantType, cmbClaimantType),
                new JFXUtil.Pairs<>(cCheckStatus, cmbCheckStatus), new JFXUtil.Pairs<>(documentType, cmbAttachmentType));

        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbCheckStatus, cmbJournalProposalStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbCheckStatus, cmbJournalProposalStatus);

        cmbJournalProposalStatus.setItems(statusJEP);
        JFXUtil.handleDisabledNodeClick(apMasterDVCheck, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "tfAuthorizedPerson":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "Authorized Person field is only available when the \"Claimant Type\" is Authorized Representative.");
                    break;
                case "cmbPayeeType":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "Payee Type is only available when \"Check Print by Bank\" is selected.");
                    break;
                case "cmbDisbursementMode":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "Disbursement mode is only available when \"Check Print by Bank\" is selected.");
                    break;
                case "cmbClaimantType":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "Claimant Type is only available when the \"Disbursement Mode\" is Pick-up.");
                    break;
            }
        });
        JFXUtil.handleDisabledNodeClick(apDVDetail, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "chbkVatClassification":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "Only available when the transaction Type is \"Payment Request\" or \"AP Adjustment\".");
                    break;
            }
        });
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
                String inputText = datePicker.getEditor().getText();
                LocalDate transactionDate = null, referenceDate = null, periodToDate = null, periodFromDate = null;
                String lsTransDate = "", lsRefDate = "", lsPeriodToDate = "", lsPeriodFromDate = "";
                switch (datePicker.getId()) {
                    case "dpCheckDate":
                        //back date not allowed
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                            transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (pbSuccess && (ldSelectedDate.isBefore(transactionDate))) {
                                JFXUtil.setJSONError(poJSON, "Check date cannot be later than the transaction date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.CheckPayments().getModel().setCheckDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpReportMonthYear":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsTransDate = sdfFormat.format(poController.Journal().Master().getTransactionDate());
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
                    case "dpJournalProposalReportMonthYear":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsTransDate = sdfFormat.format(poController.JournalProposal(pnMainJEP).Master().getTransactionDate());
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
                                poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).setForMonthOf((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }

                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadTableDetailJEP.reload();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    case "dpPeriodFrom":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsPeriodToDate = sdfFormat.format(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodTo());
                            periodToDate = LocalDate.parse(lsPeriodToDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (pbSuccess && (ldSelectedDate.isAfter(periodToDate))) {
                                JFXUtil.setJSONError(poJSON, "Period From cannot be later than the \"Period To\" date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setPeriodFrom(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordDetailBIR();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    case "dpPeriodTo":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsPeriodFromDate = sdfFormat.format(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodFrom());
                            periodFromDate = LocalDate.parse(lsPeriodFromDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (pbSuccess && (ldSelectedDate.isBefore(periodFromDate))) {
                                JFXUtil.setJSONError(poJSON, "Period To cannot be before than the \"Period From\" date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setPeriodTo(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordDetailBIR();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:

                        break;
                }
            });

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDVTransactionDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear, dpPeriodFrom, dpPeriodTo);
        JFXUtil.setActionListener(datepicker_Action, dpDVTransactionDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear, dpPeriodFrom, dpPeriodTo);
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "chbkPrintByBank":
                    if (poController.Master().getBankPrint().equals(Logical.YES)) {
                        if (!JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getPayeeType(), null, "")
                                || !JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getDesbursementMode(), null, "")
                                || !JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getClaimant(), null, "")
                                || !JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getAuthorize(), null, "")) {
                            //asks if should proceed
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Modes are not empty, changing will reset other check information fields, proceed?")) {
                                poController.CheckPayments().getModel().setPayeeType("");
                                poController.CheckPayments().getModel().setDesbursementMode("");
                                poController.CheckPayments().getModel().setClaimant("");
                                poController.CheckPayments().getModel().setAuthorize(null);
                            } else {
                                loadRecordMaster();
                                loadRecordMasterCheck();
                                return;
                            }
                        }
                    }

                    poJSON = poController.Master().setBankPrint(checkedBox.isSelected() == true ? "1" : "0");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }

                    loadRecordMaster();
                    loadRecordMasterCheck();
                    break;
                case "chbkIsCrossCheck":
                    poJSON = poController.CheckPayments().getModel().isCross(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecordMasterCheck();
                    break;
                case "chbkIsPersonOnly":
                    poJSON = poController.CheckPayments().getModel().isPayee(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecordMasterCheck();
                    break;
                case "chbkVatClassification":
                    if (poController.getEditMode() == EditMode.ADDNEW || poController.getEditMode() == EditMode.UPDATE) {
                        double lnOldVal = poController.Detail(pnDetail).getDetailVatExempt();
                        if (checkedBox.isSelected() && !poController.Detail(pnDetail).isWithVat()) {
                            poController.Detail(pnDetail).setDetailVatExempt(0.0000);
                        }
                        if (!checkedBox.isSelected()) {
                            poController.Detail(pnDetail).setDetailVatExempt(poController.Detail(pnDetail).getAmountApplied() + poController.Detail(pnDetail).getDetailAdvances());
                        }
                        poJSON = poController.computeFields(true);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Changing this option will update related values and may result to invalid computation.\n"
                                    + "Correction may be required. Proceed?")) {
                            } else {
                                poController.Detail(pnDetail).setDetailVatExempt(lnOldVal);
                            }
                        }
                        loadTableDetail.reload();
                    }

                    break;
                case "cbReverse":
                    if (!checkedBox.isSelected()) {
                        poController.Detail(pnDetail).setAmountApplied(0.0000);
                    }
                    poJSON = poController.computeFields(true);
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        poController.Detail(pnDetail).setAmountApplied(poController.Detail(pnDetail).getAmount());
                    }
                    loadRecordMaster();
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
                case "cbJEMasterProposalReverse":
                    if (!isProceed()) {
                        loadRecordMasterJEP();
                        return;
                    }
                    if (poController.JournalProposal(pnMainJEP).getEditMode() == EditMode.ADDNEW) {
                        poController.getJournalProposalList().remove(pnMainJEP);
                    } else {
                        poController.JournalProposal(pnMainJEP).Master().isReverse(checkedBox.isSelected());
                    }
                    Platform.runLater(() -> {
                        loadTableMainJEP.reload();
                        JFXUtil.runWithDelay(0.50, () -> {
                            loadTableDetailJEP.reload();
                        });
                    });
                    break;
                case "cbJEProposalReverse":
                    if (poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).getEditMode() == EditMode.ADDNEW) {
                        poController.JournalProposal(pnMainJEP).Detail().remove(pnDetailJEP);
                    } else {
                        poController.JournalProposal(pnMainJEP).Detail(pnDetailJEP).isReverse(checkedBox.isSelected());
                    }
                    Platform.runLater(() -> {
                        loadTableDetailJEP.reload();
                        JFXUtil.runWithDelay(0.50, () -> {
                            loadTableMainJEP.reload();
                        });
                    });
                    break;
                case "cbBIRReverse":
                    poJSON = poController.removeWTDeduction(pnDetailBIR);
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecordMaster();
                    loadTableDetailBIR.reload();
                    if (checkedBox.isSelected()) {
                        moveNextBIR(false, false);
                    }
                    break;
            }
        }
    }

    private boolean isProceed() {
        String dbValue = poController.JournalProposal(pnMainJEP).Master().getTransactionStatus();
        String lsMessage = "";
        boolean lbIsCbChecked = cbJEMasterProposalReverse.isSelected();
        switch (dbValue) {
            case JournalProposalStatus.OPEN:
                lsMessage = lbIsCbChecked ? "activate" : "void";
                return ShowMessageFX.YesNo(null, pxeModuleName, "This action will " + lsMessage + " the selected proposal.\nWould you like to proceed?");
            case JournalProposalStatus.VOID:
                lsMessage = lbIsCbChecked ? "activate" : "void";
                return ShowMessageFX.YesNo(null, pxeModuleName, "This action will " + lsMessage + " the selected proposal.\nWould you like to proceed?");
            case JournalProposalStatus.RETURNED:
            case JournalProposalStatus.CONFIRMED:
                lsMessage = lbIsCbChecked ? "activate" : "cancel";
                return ShowMessageFX.YesNo(null, pxeModuleName, "This action will " + lsMessage + " the selected proposal.\nWould you like to proceed?");
            case JournalProposalStatus.CANCELLED:
                lsMessage = lbIsCbChecked ? "activate" : "cancel";
                return ShowMessageFX.YesNo(null, pxeModuleName, "This action will " + lsMessage + " the selected proposal.\nWould you like to proceed?");
        }
        return true;
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);
        boolean lbShow2 = (fnEditMode == EditMode.READY);
        JFXUtil.setButtonsVisibility(!lbShow, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow2, btnConfirm);
        JFXUtil.setButtonsVisibility(fnEditMode == EditMode.READY, btnHistory);

        JFXUtil.setDisabled(!lbShow, apJournalProposalMaster, apJournalProposalDetails, apDVMaster1, apDVMaster2, apDVMaster3, apDVDetail,
                apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apJournalMaster, apJournalDetails, apBIRDetail, apAttachments);

        JFXUtil.setButtonsVisibility(fnEditMode == EditMode.UPDATE, btnUndo);

        if (fnEditMode == EditMode.READY) {
            switch (poController.Master().getTransactionStatus()) {
                case DisbursementStatic.OPEN:
                    JFXUtil.setButtonsVisibility(true, btnUpdate, btnVoid);
                    break;
                case DisbursementStatic.CONFIRMED:
                    JFXUtil.setButtonsVisibility(true, btnUpdate, btnVoid);
                    JFXUtil.setButtonsVisibility(false, btnConfirm);
                    break;
                case DisbursementStatic.RETURNED:
                    JFXUtil.setButtonsVisibility(true, btnUpdate, btnVoid);
                    break;
                case DisbursementStatic.VOID:
                case DisbursementStatic.CANCELLED:
                default:
                    JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate);
                    break;
            }
        }
    }

    private void clearTextFields() {
        stageAttachment.closeDialog();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apJournalProposalMaster, apJournalProposalDetails, apDVMaster1, apDVDetail, apDVMaster2, apDVMaster3, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apJournalMaster, apJournalDetails, apBIRDetail, apAttachments);
    }
}
