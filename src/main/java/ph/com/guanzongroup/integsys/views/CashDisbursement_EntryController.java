/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.CashDisbursement;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CashDisbursementStatus;
import ph.com.guanzongroup.cas.cashflow.status.JournalProposalStatus;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;
import ph.com.guanzongroup.integsys.model.*;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class CashDisbursement_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON, poJSONVAT;
    private static final int ROWS_PER_PAGE = 50;
    private int pnMain = 0;
    private int pnMainJEP = 0;
    private int pnDetail = 0;
    private int pnDetailJE = 0;
    private int pnDetailJEP = 0;
    private int pnDetailBIR = 0;
    private int pnAttachment = 0;
    private boolean pbIsCheckedJournalTab = false;
    private boolean pbIsCheckedJEPTab = false;
    private boolean pbIsCheckedBIRTab = false;
    private boolean pbIsCheckedAttachmentTab = false;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private CashDisbursement poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierPayeeId = "";
    private String psTransactionType = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelCashDisbursement_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelCashDisbursement_Main> filteredMain_Data;

    private ObservableList<ModelCashDisbursement_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> journal_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntryProposal_Detail> journalproposal_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntryProposal_Main> journalproposalmain_data = FXCollections.observableArrayList();
    private ObservableList<ModelBIR_Detail> BIR_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEnteredDV = false;
    private boolean pbEnteredJE = false;
    private boolean pbEnteredJEP = false;
    private boolean pbEnteredBIR = false;
    private boolean tooltipShown = false;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    private int currentIndex = 0;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    Map<String, String> imageinfo_temp = new HashMap<>();
    JFXUtil.ReloadableTableTask loadTableMain, loadTableDetail, loadTableDetailJE, loadTableDetailJEP, loadTableMainJEP, loadTableDetailBIR, loadTableAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    AnchorPane root = null;
    Scene scene = null;
    private FileChooser fileChooser;
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    ObservableList<JFXUtil.Status> statusJEP = FXCollections.observableArrayList(
            new JFXUtil.Status(JournalProposalStatus.OPEN, "OPEN"),
            new JFXUtil.Status(JournalProposalStatus.CONFIRMED, "CONFIRMED"),
            new JFXUtil.Status(JournalProposalStatus.POSTED, "POSTED"),
            new JFXUtil.Status(JournalProposalStatus.CANCELLED, "CANCELLED"),
            new JFXUtil.Status(JournalProposalStatus.VOID, "VOID"),
            new JFXUtil.Status(JournalProposalStatus.RETURNED, "RETURNED")
    );
    FilteredList<JFXUtil.Status> filteredStatuses = new FilteredList<>(statusJEP);
    /* DV  & Journal */
    @FXML
    private AnchorPane AnchorMain, apButton, apMasterDetail, apDVMaster1, apDVMaster2, apDVDetail, apMainList, apBrowse, apJournalMaster, apJournalDetails, apJournalProposalList, apJournalProposalMaster, apJournalProposalDetails, apBIRDetail, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblDVTransactionStatus, lblJournalTransactionStatus;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnVoid, btnHistory, btnRetrieve, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private Tab tabDetails, tabJournal, tabJournalProposal, tabBIR, tabAttachments;
    @FXML
    private TextField tfDVTransactionNo, tfBranch, tfDepartment, tfCashFund, tfPayee, tfCreditTo, tfVoucherNo, tfCashAdvNo, tfTotalAmount, tfVatableSales, tfVatAmountMaster, tfVatZeroRatedSales, tfVatExemptSales, tfLessWHTax, tfTotalNetAmount, tfORNoDetail, tfParticularDetail, tfVatableSalesDetail, tfVatExemptDetail, tfVatZeroRatedSalesDetail, tfVatAmountDetail, tfAmountDetail, tfCashAdvParticular, tfSearchIndustry, tfSearchPayee, tfSearchCashAdvanceNo, tfJournalTransactionNo, tfTotalDebitAmount, tfTotalCreditAmount, tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount, tfBIRTransactionNo, tfTaxCode, tfParticular, tfBaseAmount, tfTaxRate, tfTotalTaxAmount, tfAttachmentNo,
            tfJournalProposalTransactionNo, tfTotalProposalDebitAmount, tfTotalProposalCreditAmount, tfJournalProposalBranch, tfJournalProposalDepartment, tfJournalProposalAccountCode, tfJournalProposalAccountDescription, tfJournalProposalDebitAmount, tfJournalProposalCreditAmount;
    @FXML
    private ComboBox<JFXUtil.Status> cmbJournalProposalStatus;
    @FXML
    private DatePicker dpDVTransactionDate, dpJournalTransactionDate, dpReportMonthYear, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear, dpPeriodFrom, dpPeriodTo;
    @FXML
    private TextArea taDVRemarks, taJournalRemarks, taJournalProposalRemarks;
    @FXML
    private CheckBox cbReverse, cbJEReverse, cbBIRReverse, cbJEProposalReverse, cbJEMasterProposalReverse;
    @FXML
    private TableView tblVwDetails, tblViewMainList, tblVwJournalDetails, tblVwJournalProposalList, tblVwJournalProposalDetails, tblVwBIRDetails, tblAttachments;
    @FXML
    private TableColumn tblDVRowNo, tblReceiptNoDetail, tblParticularDetail, tblAmountDetail, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales, tblNetAmount, tblRowNo, tblRefNo, tblLiquidationDate, tblPayee, tblAmount, tblJournalRowNo, tblJournalReportMonthYear, tblJournalAccountCode, tblJournalAccountDescription, tblJournalDebitAmount, tblJournalCreditAmount, tblJournalProposalListRowNo, tblJournalProposalListTransNo, tblJournalProposalListBranch, tblJournalProposalListDepartment, tblJournalProposalListDebitAmt, tblJournalProposalListCreditAmt, tblJournalProposalRowNo, tblJournalProposalReportMonthYear, tblJournalProposalAccountCode, tblJournalProposalAccountDescription, tblJournalProposalDebitAmount, tblJournalProposalCreditAmount, tblBIRRowNo, tblBIRParticular, tblTaxCode, tblBaseAmount, tblTaxRate, tblTaxAmount, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private Pagination pagination;
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
            poController = new CashflowControllers(oApp, null).CashDisbursement();
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
            initComboboxes();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;

            initButton(pnEditMode);
            pagination.setPageCount(1);

            Platform.runLater(() -> {
                try {
                    poController.Master().setIndustryId(psIndustryId);
                    poController.Master().setCompanyId(psCompanyId);
                    poController.setIndustryId(psIndustryId);
                    poController.setCompanyId(psCompanyId);
//                poController.setCategoryID(psCategoryId);
                    poController.Master().setBranchCode(oApp.getBranchCode());
                    loadRecordSearch();
                    btnNew.fire();
                    TriggerWindowEvent();
                    filterIndustry();
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
    ChangeListener<Scene> WindowKeyEvent = (obs, oldScene, newScene) -> {
        if (newScene != null) {
            setKeyEvent(newScene);
        }
    };

    public void filterIndustry() {
        try {
            if (!psIndustryId.equals(System.getProperty("sys.main.industry"))) {
                poController.Master().setIndustryId(psIndustryId);
                tfSearchIndustry.setText(poController.Master().Industry().getDescription());
                JFXUtil.setDisabled(true, tfSearchIndustry);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

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
                if (JFXUtil.isObjectEqualTo(pnEditMode, EditMode.ADDNEW, EditMode.READY, EditMode.UPDATE)) {
                    if (DoesContainValidDisbDetail()) {
                    } else {
                        ShowMessageFX.Warning(null, pxeModuleName, lsValidDisbMessage);
                        return;
                    }
                    showAttachmentDialog();
                }
            }
            if (event.getCode() == KeyCode.F12) {
                LoginControllerHolder.getMainController().eventf12(LoginControllerHolder.getMainController().getTab());
            }
        });
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

    private boolean DoesContainValidDisbDetail() {
        String lsParticular = "";

        if (poController.getDetailCount() <= 0) {
            return false;
        }
        if (poController.Master().getTransactionTotal() <= 0.0000) {
            return false;
        }
//        try {
//            if (poController.Master().getSourceNo() != null && !"".equals(poController.Master().getSourceNo())) {
//                lsParticular = poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular();
//            } else {
//                lsParticular = poController.Detail(0).Particular().getDescription();
//            }
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(CashDisbursement_EntryController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return !JFXUtil.isObjectEqualTo(lsParticular, null, "");
        return true;
    }
    String lsValidDisbMessage = "Please provide at least one valid disbursement detail with amount to proceed.";

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Cash Disbursement":
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
                        if (DoesContainValidDisbDetail()) {
                            pbIsCheckedJournalTab = true;
                            populateJE();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                            ShowMessageFX.Warning(null, pxeModuleName, lsValidDisbMessage);
                        }
                    }
                    break;
                case "Journal Proposal":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apJournalProposalMaster, apJournalProposalDetails);
                        if (DoesContainValidDisbDetail() && poController.Detail(0).getParticularId() != null && !poController.Detail(0).getParticularId().isEmpty()) {
                            pbIsCheckedJEPTab = true;
                            populateJEP();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                            ShowMessageFX.Warning(null, pxeModuleName, "Please provide at least one valid disbursement detail to proceed.");
                        }
                    }
                    break;
                case "BIR 2307":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apBIRDetail);
                        if (DoesContainValidDisbDetail()) {
                            pbIsCheckedBIRTab = true;
                            populateBIR();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                            ShowMessageFX.Warning(null, pxeModuleName, lsValidDisbMessage);
                        }
                    }
                    break;
                case "Attachments":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apAttachments);
                        if (DoesContainValidDisbDetail()) {
                            if (isSourceNoAvailable()) {
                                pbIsCheckedAttachmentTab = true;
                                try {
                                    poController.loadAttachments();
                                } catch (GuanzonException | SQLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                }

                            }
                            loadTableAttachment.reload();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                            ShowMessageFX.Warning(null, pxeModuleName, lsValidDisbMessage);
                        }
                    }
                    break;
            }
        });

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

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnRemoveAttachment, btnAddAttachment, btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnVoid, btnRetrieve, btnHistory, btnClose, btnArrowRight, btnArrowLeft);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnBrowse":
                    poController.Master().setIndustryId(psIndustryId);
                    poController.Master().setCompanyId(psCompanyId);
                    poController.Master().setBranchCode(oApp.getBranchCode());
                    poController.setTransactionStatus(CashDisbursementStatus.OPEN);
                    poJSON = poController.SearchTransaction();
                    if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                    pbIsCheckedJournalTab = false;
                    pbIsCheckedJEPTab = false;
                    pbIsCheckedBIRTab = false;
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                    pnEditMode = poController.getEditMode();
//                    psSupplierPayeeId = poController.Master().Payee().getClientID();
                    poController.populateJournal();
                    loadTableDetail.reload();
                    break;
                case "btnNew":
                    handleLoading(lsButton);
                    return;
                case "btnUpdate":
                    String lsUserId = oApp.getUserID();
                    String lsPosition = poController.checkPosition(CashDisbursementStatus.OPEN, lsUserId);
                    if (lsPosition == null || "".equals(lsPosition)) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized officer.");
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    //Recheck transaction status
                    poJSON = poController.checkUpdateTransaction(true);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }

                    poJSON = poController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pbIsCheckedJournalTab = false;
                    pbIsCheckedJEPTab = false;
                    pbIsCheckedBIRTab = false;
                    pnEditMode = poController.getEditMode();
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                    loadTableDetail.reload();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apDVMaster1, apDVDetail, apJournalDetails, apJournalProposalDetails, apJournalProposalMaster, apBIRDetail);
                    break;
                case "btnSave":
                    handleLoading(lsButton);
                    return;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
                        JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
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
                case "btnRetrieve":
                    loadTableMain.reload();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        stageAttachment.closeDialog();
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to void transaction?")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            poJSON = poController.VoidTransaction();
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
                        if (imageinfo_temp.containsKey(selectedFile.getName().toString())) {
                            ShowMessageFX.Warning(null, pxeModuleName, "File name already exists.");
                            loadRecordAttachment(true);
                            return;
                        } else {
                            imageinfo_temp.put(selectedFile.getName().toString(), imgPath.toString());
                        }

                        //Limit maximum pages of pdf to add
                        if (imgPath2.toLowerCase().endsWith(".pdf")) {
                            try ( PDDocument document = PDDocument.load(selectedFile)) {
                                PDFRenderer pdfRenderer = new PDFRenderer(document);
                                int pageCount = document.getNumberOfPages();
                                if (pageCount > 5) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "PDF exceeds maximum allowed pages.");
                                    return;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(CashDisbursement_EntryController.class.getName()).log(Level.SEVERE, null, ex);
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
                    imageinfo_temp.clear();
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
            cmdReloadProcess(lsButton);
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (IOException ex) {
            Logger.getLogger(CashDisbursement_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cmdReloadProcess(String lsButton) {
        if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVoid")) {
            pbIsCheckedJournalTab = false;
            pbIsCheckedJEPTab = false;
            pbIsCheckedBIRTab = false;
            poController.resetTransaction();
            clearTextFields();
            JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
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

    private void handleLoading(String lsButton) {
        switch (lsButton) {
            case "btnSave":
                AtomicReference<JSONObject> loSaveResultRef = new AtomicReference<>();
                AtomicReference<JSONObject> loOpenResultRef = new AtomicReference<>();

                if (pnEditMode == EditMode.UPDATE) {
                    try {
                        poJSON = poController.checkUpdateTransaction(true);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                    } catch (SQLException | CloneNotSupportedException | GuanzonException | ScriptException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        return;
                    }
                }

                if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                    return;
                }

                runWithLoadingAllowFxDialogs(
                        () -> {
                            try {
                                JSONObject loSaveJSON = poController.SaveTransaction();
                                loSaveResultRef.set(loSaveJSON);
                                if ("success".equals(String.valueOf(loSaveJSON.get("result")))) {
                                    JSONObject loOpenJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                                    loOpenResultRef.set(loOpenJSON);
                                }
                            } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
                                throw new RuntimeException(ex);
                            }

                        },
                        () -> {
                            try {
                                JSONObject loSaveJSON = loSaveResultRef.get();
                                if (loSaveJSON == null) {
                                    ShowMessageFX.Error(null, pxeModuleName, "Unable to save transaction.");
                                    return;
                                }

                                if (!"success".equals(String.valueOf(loSaveJSON.get("result")))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, String.valueOf(loSaveJSON.get("message")));
                                    return;
                                }

                                JFXUtil.showRetainedHighlight(true, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                                ShowMessageFX.Information(null, pxeModuleName, String.valueOf(loSaveJSON.get("message")));

                                JSONObject loOpenJSON = loOpenResultRef.get();

                                if (loOpenJSON != null && "success".equals(String.valueOf(loOpenJSON.get("result")))) {
                                    pnEditMode = poController.getEditMode();
                                    initButton(pnEditMode);
                                }

                                if (pnEditMode == EditMode.READY) {
                                    if (CashDisbursementStatus.OPEN.equals(poController.Master().getTransactionStatus())
                                    || CashDisbursementStatus.RETURNED.equals(poController.Master().getTransactionStatus())) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            if (!pbIsCheckedBIRTab && poController.Master().getVatAmount() > 0.0000) {
                                                ShowMessageFX.Warning(null, pxeModuleName, "Please check the BIR 2307 before confirming.");
                                                Platform.runLater(() -> btnNew.fire());
                                                return;
                                            }

                                            poJSON = poController.ConfirmTransaction("");
                                            if ("error".equals((String) poJSON.get("result"))) {
                                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                                Platform.runLater(() -> btnNew.fire());
                                                return;
                                            }
                                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                        }
                                    }
                                }

                                Platform.runLater(() -> btnNew.fire());
                            } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        },
                        ex -> {
                            Throwable loRoot = ex.getCause() != null ? ex.getCause() : ex;
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, loRoot);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(loRoot));
                        });
                break;
            case "btnNew":
                AtomicReference<JSONObject> loNewResultRef = new AtomicReference<>();
                clearTextFields();

                runWithLoadingAllowFxDialogs(
                        () -> {
                            try {
                                poController.initFields();
                                JSONObject loNewJSON = poController.NewTransaction();
                                loNewResultRef.set(loNewJSON);
                            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                                throw new RuntimeException(ex);
                            }
                        },
                        () -> {
                            JSONObject loNewJSON = loNewResultRef.get();
                            if (loNewJSON == null) {
                                ShowMessageFX.Error(null, pxeModuleName, "Unable to create a new transaction.");
                                return;
                            }

                            if ("error".equals(String.valueOf(loNewJSON.get("result")))) {
                                ShowMessageFX.Warning(null, pxeModuleName, String.valueOf(loNewJSON.get("message")));
                                return;
                            }

                            JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                            loadTableDetail.reload();
                            pnEditMode = poController.getEditMode();
                            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                            cmdReloadProcess("btnNew");
                        },
                        ex -> {
                            Throwable loRoot = ex.getCause() != null ? ex.getCause() : ex;
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, loRoot);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(loRoot));
                        });
                break;
        }

    }

    private void runWithLoadingAllowFxDialogs(Runnable fxTask, Runnable onSuccess, java.util.function.Consumer<Throwable> onError) {
        AtomicReference<Throwable> loError = new AtomicReference<>();

        JFXUtil.runWithLoading(
                getOwnerStage(),
                apButton,
                () -> {
                    try {
                        fxTask.run();
                    } catch (Throwable ex) {
                        loError.set(ex);
                    }
                },
                () -> {
                    if (loError.get() != null) {
                        onError.accept(loError.get());
                        return;
                    }
                    onSuccess.run();
                }
        );
    }

    private List<String> checkJEorJEP() {
        List<String> titles = new ArrayList<>();
        try {
            // allows JE and JEP to have value
            // require to review, either of two, if one have value then require it to check tab
            // if two have value require to check it both
            // if neither have value message that any of JE or JEP or both must have value
            // question is how to define valid entry for both
            if (!poController.existJournal().equals("")) {
                titles.add("Journal Entry");
            }
            if (poController.getJournalProposalList().size() >= 1) {
                if (!JFXUtil.isObjectEqualTo(poController.JournalProposal(0).Detail(0).getAccountCode(), null, "")) {
                    titles.add("Journal Proposal");
                }
            }
            return titles;
        } catch (SQLException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return titles;
    }

    private boolean checkJEorJEPSaving() {
        List<String> titles = checkJEorJEP();
        if ((titles.contains("Journal Entry")) && (titles.contains("Journal Proposal"))) {
            if (!pbIsCheckedJournalTab && !pbIsCheckedJEPTab) {
                ShowMessageFX.Warning(null, pxeModuleName, "Please check the Journal Entry & Journal Proposal before saving.");
                return false;
            }
        } else if ((titles.contains("Journal Proposal"))) {
            if (!pbIsCheckedJEPTab) {
                ShowMessageFX.Warning(null, pxeModuleName, "Please check the Journal Proposal before saving.");
                return false;
            }
        } else if (titles.contains("Journal Entry")) {
            if (!pbIsCheckedJournalTab) {
                ShowMessageFX.Warning(null, pxeModuleName, "Please check the Journal Entry before saving.");
                return false;
            }
        } else {
            ShowMessageFX.Warning(null, pxeModuleName, "No journal entry or journal proposal found. Add either one or both and save before verifying.");
            return false;
        }
        return true;
    }

    private void populateBIR() {
        try {
            poJSON = new JSONObject();
            JFXUtil.clearTextFields(apBIRDetail);
            poJSON = poController.populateWithholdingTaxDeduction();
            if (JFXUtil.isJSONSuccess(poJSON)) {
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

    public void loadHighlightFromDetail() {
//        try {
        for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
            String lsTransNo = !JFXUtil.isObjectEqualTo(poController.Master().getSourceNo(), null, "")
                    ? poController.Master().getSourceNo() : "";
            String lsHighlightbasis;
            lsHighlightbasis = lsTransNo;
            if (!JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getAmount(), null, "")) {
                if (poController.Detail(lnCtr).getAmount() != 0.0000) {
                    plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "1"));
                } else {
                    plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "0"));
                }
            }
        }
        for (Pair<String, String> pair : plOrderNoPartial) {
            if (!"".equals(pair.getKey()) && pair.getKey() != null) {
                JFXUtil.highlightByKey(tblViewMainList, pair.getKey(), "#A7C7E7", highlightedRowsMain);
            }
        }
        JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, false);
//        } catch (GuanzonException | SQLException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        }
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();
        if (pnEditMode == EditMode.ADDNEW) {  //Do not allow to link cash advance when edit mode is not equal to add new

            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            ModelCashDisbursement_Main selected = (ModelCashDisbursement_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                    String lsTransactionNo = selected.getIndex02();
                    stageAttachment.closeDialog();

                    if (!JFXUtil.loadValidation2(pnEditMode, pxeModuleName, poController.Master().getSourceNo(), lsTransactionNo, poController.Master().getTransactionTotal())) {
                        return;
                    }
                    pnMain = pnRowMain;
                    JFXUtil.clearTextFields(apDVMaster1, apDVMaster2, apDVDetail);
                    poJSON = poController.populateDetail(lsTransactionNo);
                    if ("error".equals(poJSON.get("result"))) {
                        loadTableDetail.reload();
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    loadTableDetail.reload();
                    moveNext(false, false);

                    JFXUtil.runWithDelay(0.50, () -> {
                        loadTableMain.reload();

                    });
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            }
        } else {
            ShowMessageFX.Warning(null, pxeModuleName, "Data can only be inserted when in ADD mode.");
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
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                main_data.clear();
                                poJSON = poController.loadCashAdvanceList(tfSearchIndustry.getText(), tfSearchPayee.getText(), tfSearchCashAdvanceNo.getText());
                                if ("success".equals(poJSON.get("result"))) {
                                    String date = "";

                                    for (int lnCtr = 0; lnCtr <= poController.getCashAdvancesCount() - 1; lnCtr++) {
                                        if (!JFXUtil.isObjectEqualTo(poController.CashAdvancesList(lnCtr).getLiquidatedDate(), null, "")) {
                                            date = sdf.format(poController.CashAdvancesList(lnCtr).getLiquidatedDate());
                                        }

                                        main_data.add(new ModelCashDisbursement_Main(
                                                String.valueOf(lnCtr + 1),
                                                poController.CashAdvancesList(lnCtr).getTransactionNo(),
                                                date,
                                                poController.CashAdvancesList(lnCtr).Payee().getCompanyName(),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.CashAdvancesList(lnCtr).getAdvanceAmount(), true)),
                                                ""));
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
                                Logger.getLogger(CashDisbursement_EntryController.class.getName()).log(Level.SEVERE, null, ex);
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
                            details_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.Master().getSourceNo() == null || "".equals(poController.Master().getSourceNo())) {
                                    poController.ReloadDetail();
                                }
                                poJSON = poController.computeDetailFields(true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            int lnRowCount = 0;
                            String lsParticular = "", lsOrNo = "";
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (!poController.Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                if (poController.Master().getSourceNo() != null && !"".equals(poController.Master().getSourceNo())) {
                                    lsParticular = poController.Detail(lnCtr).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular();
                                } else {
                                    lsParticular = poController.Detail(lnCtr).Particular().getDescription();
                                }
                                lsOrNo = poController.Detail(lnCtr).getReferNo();
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelCashDisbursement_Detail(String.valueOf(lnRowCount),
                                                lsOrNo,
                                                lsParticular,
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatSales(), true),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatAmount(), true)),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatSales(), false),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailZeroVat(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatExempt(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), true),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                            loadHighlightFromDetail();

                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 11); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwDetails, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex11());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwDetails, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblVwDetails.getSelectionModel().getSelectedIndex()).getIndex11());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (CloneNotSupportedException | GuanzonException | SQLException ex) {
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
                        BIR_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadWTDeductions();
                            }
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.getWTaxDeductionsCount(); lnCtr++) {
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
        JFXUtil.setColumnCenter(tblRowNo, tblRefNo, tblLiquidationDate);
        JFXUtil.setColumnLeft(tblPayee);
        JFXUtil.setColumnRight(tblAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblDVRowNo, tblReceiptNoDetail);
        JFXUtil.setColumnLeft(tblParticularDetail);
        JFXUtil.setColumnRight(tblAmountDetail, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales, tblNetAmount);
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
            if (!details_data.isEmpty() && event.getClickCount() == 1) {
                ModelCashDisbursement_Detail selected = (ModelCashDisbursement_Detail) tblVwDetails.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(details_data.get(tblVwDetails.getSelectionModel().getSelectedIndex()).getIndex11());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
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
            pnMainJEP = tblVwJournalProposalList.getSelectionModel().getSelectedIndex();;
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
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelCashDisbursement_Main) item).getIndex02(), highlightedRowsMain);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblVwDetails, tblVwJournalDetails, tblVwJournalProposalList, tblVwJournalProposalDetails, tblVwBIRDetails);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList, tblVwDetails, tblVwJournalDetails, tblVwJournalProposalDetails, tblVwBIRDetails);
    }

    private void tableKeyEvents(KeyEvent event) {
        TableView<?> currentTable = (TableView<?>) event.getSource();
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
        if (focusedCell == null) {
            return;
        }
        boolean moveDown = event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.DOWN;
        boolean moveUp = event.getCode() == KeyCode.UP;
        int newIndex = 0;

        if (moveDown || moveUp) {
            switch (currentTable.getId()) {
                case "tblVwDetails":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex11())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex11());
                    pnDetail = newIndex;
                    loadRecordDetail();
                    break;
                case "tblVwJournalDetails":
                    if (journal_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(journal_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(journal_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailJE = newIndex;
                    loadRecordDetailJE();
                    break;
                case "tblVwJournalProposalList":
                    if (journalproposalmain_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
                    pnMainJEP = newIndex;
                    loadTableDetailFromMainJEP();
                    break;
                case "tblVwJournalProposalDetails":
                    if (journalproposal_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(journalproposal_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(journalproposal_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailJEP = newIndex;
                    loadRecordDetailJEP();
                    break;
                case "tblVwBIRDetails":
                    if (BIR_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(BIR_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(BIR_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailBIR = newIndex;
                    loadRecordDetailBIR();
                    break;
                case "tblAttachments":
                    if (attachment_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03())
                            : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                    pnAttachment = newIndex;
                    loadRecordAttachment(true);
                    break;
            }
            event.consume();
        }
    }

    private void initTextFields() {
        //Initialise  TextField Focus
        JFXUtil.setFocusListener(txtArea_Focus, taDVRemarks, taJournalRemarks, taJournalProposalRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, apDVMaster1, apDVMaster2);
        JFXUtil.setFocusListener(txtSearch_Focus, apBrowse);
        JFXUtil.setFocusListener(txtDetail_Focus, apDVDetail);
        JFXUtil.setFocusListener(txtDetailJE_Focus, apJournalDetails);
        //apJournalProposalDetails
        JFXUtil.setFocusListener(txtJournalProposalMaster_Focus, apJournalProposalMaster);
        JFXUtil.setFocusListener(txtJournalProposalDetails_Focus, apJournalProposalDetails);
        JFXUtil.setFocusListener(txtBIRDetail_Focus, apBIRDetail);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apDVMaster1, apDVMaster2, apDVDetail, apBrowse, apJournalMaster, apJournalDetails, apJournalProposalMaster, apJournalProposalDetails, apBIRDetail);
        JFXUtil.adjustColumnForScrollbar(tblVwDetails, tblViewMainList, tblVwJournalDetails, tblVwBIRDetails, tblAttachments);

        JFXUtil.setCommaFormatter(tfDebitAmount, tfCreditAmount, tfJournalProposalDebitAmount, tfJournalProposalCreditAmount, tfBaseAmount, tfAmountDetail);
        JFXUtil.setCommaFormatter2(tfVatExemptDetail);
        taJournalProposalRemarks.setOnKeyPressed(this::txtField_KeyPressed);
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

        JFXUtil.handleDisabledNodeClick(apDVDetail, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "cbReverse":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "Reverse is disabled for transactions linked to a cash advance.");
                    break;
                case "tfVatExemptDetail":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "VAT Exempt requires a Receipt No and an amount greater than 0.00 to be editable.");
                    break;
                case "tfAmountDetail":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "Amount is editable only in manual entry of details.");
                    break;
            }
        });
        JFXUtil.handleDisabledNodeClick(apAttachmentButtons, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "btnAddAttachment":
                case "btnRemoveAttachment":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "This button is disabled when linked from Cash Liquidation.");
                    break;
            }
        });
        JFXUtil.handleDisabledNodeClick(apDVMaster1, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "tfCashFund":
                case "tfPayee":
                case "tfDepartment":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "This field cannot be modified if the transaction is non-open and contains a Source");
                    break;

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
                            loadTableMain.reload();
                        }
                        break;
                    case "tfSearchPayee":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");
                            loadTableMain.reload();
                        }
                        break;
                    case "tfSearchCashAdvanceNo":
                        if (lsValue.isEmpty()) {
                            loadTableMain.reload();
                        }
                        break;
                }
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
                try {

                    /*Lost Focus*/
                    switch (lsID) {
                        case "tfBranch":
                            if (lsValue.isEmpty()) {
                                if (isSourceNoAvailable()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        if (!JFXUtil.isObjectEqualTo(poController.Master().getBranchCode(), null, "")) {
                                            boolean lbproceed = !JFXUtil.isObjectEqualTo(poController.Detail(0).getReferNo(), null, "")
                                            || !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular(), null, "")
                                            || !JFXUtil.isObjectEqualTo(poController.Detail(0).Particular().getDescription(), null, "");

                                            if (poController.getDetailCount() >= 1 && lbproceed) {
                                                if (!pbKeyPressed) {
                                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                            "Are you sure you want to change the Branch name?\nPlease note that this action will delete all Cash Disbursement details.\n\nDo you wish to proceed?") == true) {
                                                        poController.removeDetails();
                                                        poController.Master().setCashFundId("");
                                                        poController.Master().setBranchCode("");
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
                                }
                                poController.Master().setBranchCode("");
                            }
                            break;
                        case "tfCashFund":
                            if (lsValue.isEmpty()) {
                                if (isSourceNoAvailable()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        if (!JFXUtil.isObjectEqualTo(poController.Master().getCashFundId(), null, "")) {
                                            boolean lbproceed = !JFXUtil.isObjectEqualTo(poController.Detail(0).getReferNo(), null, "")
                                            || !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular(), null, "")
                                            || !JFXUtil.isObjectEqualTo(poController.Detail(0).Particular().getDescription(), null, "");

                                            if (poController.getDetailCount() >= 1 && lbproceed) {
                                                if (!pbKeyPressed) {
                                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                            "Are you sure you want to change the Cash Fund?\nPlease note that this action will delete all Cash Disbursement details.\n\nDo you wish to proceed?") == true) {
                                                        poController.removeDetails();
                                                        poController.Master().setCashFundId("");
                                                        poController.Master().setCashFundId("");
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
                                }
                                poController.Master().setCashFundId("");
                            }
                            break;
                        case "tfDepartment":
                            if (lsValue.isEmpty()) {
                                if (isSourceNoAvailable()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        if (!JFXUtil.isObjectEqualTo(poController.Master().getPayeeName(), null, "")) {
                                            boolean lbproceed = !JFXUtil.isObjectEqualTo(poController.Detail(0).getReferNo(), null, "")
                                            || !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular(), null, "")
                                            || !JFXUtil.isObjectEqualTo(poController.Detail(0).Particular().getDescription(), null, "");

                                            if (poController.getDetailCount() >= 1 && lbproceed) {
                                                if (!pbKeyPressed) {
                                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                            "Are you sure you want to change the Department name?\nPlease note that this action will delete all Cash Disbursement details.\n\nDo you wish to proceed?") == true) {
                                                        poController.removeDetails();
                                                        poController.Master().setCashFundId("");
                                                        poController.Master().setDepartmentRequest("");
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
                                }
                                poController.Master().setDepartmentRequest("");
                            }
                            break;
                        case "tfPayee":
                            if (lsValue.isEmpty()) {
                                if (isSourceNoAvailable()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        if (!JFXUtil.isObjectEqualTo(poController.Master().getPayeeName(), null, "")) {
                                            boolean lbproceed = !JFXUtil.isObjectEqualTo(poController.Detail(0).getReferNo(), null, "")
                                            || !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular(), null, "")
                                            || !JFXUtil.isObjectEqualTo(poController.Detail(0).Particular().getDescription(), null, "");

                                            if (poController.getDetailCount() >= 1 && lbproceed) {
                                                if (!pbKeyPressed) {
                                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                            "Are you sure you want to change the Payee name?\nPlease note that this action will delete all Cash Disbursement details.\n\nDo you wish to proceed?") == true) {
                                                        poController.removeDetails();
                                                        poController.Master().setCashFundId("");
                                                        poController.Master().setPayeeName("");
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
                                }
                                poController.Master().setPayeeName("");
                            }
                            break;
                        case "tfCreditTo":
                            if (lsValue.isEmpty()) {
                                poController.Master().setCreditedTo("");
                            }
                            break;
                    }
                    loadRecordMaster();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
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
                        if (isSourceNoAvailable()) {
                            if (pbEnteredDV) {
                                moveNext(false, true);
                                pbEnteredDV = false;
                            }
                        }
                        break;
                    case "tfParticularDetail":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setParticularId("");
                        }
                        break;
                    case "tfORNoDetail":

                        poJSON = poController.Detail(pnDetail).setReferNo(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfAmountDetail":

                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setAmount(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        JFXUtil.runWithDelay(0.60, () -> {
                            if (pbEnteredDV) {
                                moveNext(false, true);
                                pbEnteredDV = false;
                            }
                        });
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
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
                        }
                        break;
                    case "tfJournalProposalDepartment":
                        if (lsValue.isEmpty()) {
                            poController.JournalProposal(pnMainJEP).Master().setDepartmentId("");
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
                    JFXUtil.runWithDelay(0.50, () -> {
                        if (JFXUtil.isObjectEqualTo(lsID, "tfJournalProposalDebitAmount", "tfJournalProposalCreditAmount")) {
                            loadTableMainJEP.reload();
                        }
                    });
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
                        if (isSourceNoAvailable()) {
                            if (tfVatExemptDetail.isFocused()) {
                                pbEnteredDV = true;
                            }
                        } else {
                            if (tfAmountDetail.isFocused()) {
                                pbEnteredDV = true;
                            }
                        }

                        if (tfCreditAmount.isFocused()) {
                            pbEnteredJE = true;
                        }
                        if (tfBaseAmount.isFocused()) {
                            pbEnteredBIR = true;
                        }
                        if (tfJournalProposalCreditAmount.isFocused()) {
                            pbEnteredJEP = true;
                        }
                        if (txtInput instanceof TextField) {
                            TextField txtField = (TextField) txtInput;
                            CommonUtils.SetNextFocus(txtField);
                        }
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearchIndustry":
                                poController.SearchIndustry(lsValue, false);
                                loadRecordSearch();
                                loadTableMain.reload();
                                break;
                            case "tfSearchPayee":
                                poJSON = poController.SearchPayee(lsValue, false, true);
                                loadRecordSearch();
                                loadTableMain.reload();
                                break;
                            case "tfSearchCashAdvanceNo":
                                if (!tooltipShown) {
                                    JFXUtil.showTooltip("NOTE: Results appear directly in the table view, no pop-up dialog.", tfSearchCashAdvanceNo);
                                    tooltipShown = true;
                                }
                                loadTableMain.reload();
                                break;

                            case "tfBranch":
                                if (isSourceNoAvailable()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        boolean lbproceed = !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getORNo(), null, "")
                                                || !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular(), null, "")
                                                || !JFXUtil.isObjectEqualTo(poController.Detail(0).Particular().getDescription(), null, "");
                                        if (poController.getDetailCount() >= 1 && lbproceed) {
                                            pbKeyPressed = true;
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the Branch name?\nPlease note that this action will delete all Cash Disbursement details.\n\nDo you wish to proceed?") == true) {
                                                poController.Master().setCashFundId("");
                                                poController.removeDetails();
                                                loadTableDetail.reload();
                                            } else {
                                                return;
                                            }
                                            pbKeyPressed = false;
                                        }
                                    }
                                }

                                poJSON = poController.SearchBranch(lsValue, false, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMaster();
                                    break;
                                } else {
                                    JFXUtil.textFieldMoveNext(tfDepartment);
                                }
//                                psSupplierPayeeId = poController.Master().getSupplierClientID();
                                loadRecordMaster();
                                break;
                            case "tfDepartment":
                                if (isSourceNoAvailable()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        boolean lbproceed = !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getORNo(), null, "")
                                                || !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular(), null, "")
                                                || !JFXUtil.isObjectEqualTo(poController.Detail(0).Particular().getDescription(), null, "");
                                        if (poController.getDetailCount() >= 1 && lbproceed) {
                                            pbKeyPressed = true;
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the Department name?\nPlease note that this action will delete all Cash Disbursement details.\n\nDo you wish to proceed?") == true) {
                                                poController.Master().setCashFundId("");
                                                poController.removeDetails();
                                                loadTableDetail.reload();
                                            } else {
                                                return;
                                            }
                                            pbKeyPressed = false;
                                        }
                                    }
                                }
                                poJSON = poController.SearchDepartment(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMaster();
                                    break;
                                } else {
                                    JFXUtil.textFieldMoveNext(tfCashFund);
                                }
                                loadRecordMaster();
                                break;
                            case "tfCashFund":
                                if (isSourceNoAvailable()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        boolean lbproceed = !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getORNo(), null, "")
                                                || !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular(), null, "")
                                                || !JFXUtil.isObjectEqualTo(poController.Detail(0).Particular().getDescription(), null, "");
                                        if (poController.getDetailCount() >= 1 && lbproceed) {
                                            pbKeyPressed = true;
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the Cash Fund?\nPlease note that this action will delete all Cash Disbursement details.\n\nDo you wish to proceed?") == true) {
                                                poController.Master().setCashFundId("");
                                                poController.removeDetails();
                                                loadTableDetail.reload();
                                            } else {
                                                return;
                                            }
                                            pbKeyPressed = false;
                                        }
                                    }
                                }
                                poJSON = poController.SearchCashFund(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMaster();
                                    break;
                                } else {
                                    JFXUtil.textFieldMoveNext(tfPayee);
                                }
                                loadRecordMaster();
                                break;
                            case "tfPayee":
                                if (isSourceNoAvailable()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        boolean lbproceed = !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getORNo(), null, "")
                                                || !JFXUtil.isObjectEqualTo(poController.Detail(0).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular(), null, "")
                                                || !JFXUtil.isObjectEqualTo(poController.Detail(0).Particular().getDescription(), null, "");

                                        if (poController.getDetailCount() >= 1 && lbproceed) {
                                            pbKeyPressed = true;
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the Payee name?\nPlease note that this action will delete all Cash Disbursement details.\n\nDo you wish to proceed?") == true) {
                                                poController.Master().setCashFundId("");
                                                poController.removeDetails();
                                                loadTableDetail.reload();
                                            } else {
                                                return;
                                            }
                                            pbKeyPressed = false;
                                        }
                                    }
                                }
                                poJSON = poController.SearchPayee(lsValue, false, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMaster();
                                    break;
                                } else {
                                    JFXUtil.textFieldMoveNext(tfCreditTo);
                                }
                                loadRecordMaster();
                                break;
                            case "tfCreditTo":
                                poJSON = poController.SearchCreditTo(lsValue, false);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    JFXUtil.textFieldMoveNext(taDVRemarks);
                                }
                                loadRecordMaster();
                                break;

                            case "tfParticularDetail":
                                poJSON = poController.SearchParticular(lsValue, false, pnDetail);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    if (isSourceNoAvailable()) {
                                        JFXUtil.textFieldMoveNext(tfVatExemptDetail);
                                    } else {
                                        JFXUtil.textFieldMoveNext(tfAmountDetail);
                                    }
                                }
                                loadTableDetail.reload();
                                break;

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
                                poJSON = poController.JournalProposal(pnMainJEP).SearchAccountCode(pnDetailJEP, lsValue, true, poController.Master().getIndustryId(), null);
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
                                poJSON = poController.JournalProposal(pnMainJEP).SearchAccountCode(pnDetailJEP, lsValue, false, poController.Master().getIndustryId(), null);
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
//                                apBIRDetail
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
                                poJSON = poController.SearchWTParticular(lsValue, pnDetailBIR, false);
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
                            {new String[]{"tfORNoDetail", "tfAmountDetail", "tfParticularDetail", "tfVatExemptDetail"}, (Runnable) () -> moveNext(true, true)},
                            {new String[]{"tfAccountCode", "tfAccountDescription", "tfCreditAmount"}, (Runnable) () -> moveNextJE(true, true)},
                            {new String[]{"tfJournalProposalAccountCode", "tfJournalProposalAccountDescription", "tfJournalProposalCreditAmount"}, (Runnable) () -> moveNextJEP(true, true)},
                            {new String[]{"tfJournalProposalBranch", "tfJournalProposalDepartment", "taJournalProposalRemarks"}, (Runnable) () -> moveNextJEPMain(true, true)},
                            {new String[]{"tfTaxCode", "tfParticular", "tfBaseAmount", "tfTaxRate"}, (Runnable) () -> moveNextBIR(true, true)}
                        });
                        event.consume();
                        break;
                    case DOWN:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfORNoDetail", "tfAmountDetail", "tfParticularDetail", "tfVatExemptDetail"}, (Runnable) () -> moveNext(false, true)},
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

    private boolean isSourceNoAvailable() {
        return !JFXUtil.isObjectEqualTo(poController.Master().getSourceNo(), null, "");
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
        if (isSourceNoAvailable()) {
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnDetail).getParticularId(), tfParticularDetail},
                {poController.Detail(pnDetail).getDetailVatExempt(), tfVatExemptDetail},}, tfVatExemptDetail); // default
        } else {
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnDetail).getReferNo(), tfORNoDetail},
                {poController.Detail(pnDetail).getParticularId(), tfParticularDetail},
                {poController.Detail(pnDetail).getAmount(), tfAmountDetail},}, tfAmountDetail); // default
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
        tfSearchPayee.setText(poController.getSearchPayee());
        filterIndustry();
        JFXUtil.updateCaretPositions(apBrowse);
    }

    private void loadRecordMaster() {
        try {
            boolean lbShow3 = JFXUtil.isObjectEqualTo(poController.Master().getTransactionStatus(), CashDisbursementStatus.OPEN);
            JFXUtil.setDisabled(!lbShow3, tfCreditTo);

            boolean lbShow4 = JFXUtil.isObjectEqualTo(poController.Master().getSourceNo(), null, "");
            JFXUtil.setDisabled(!lbShow3 || !lbShow4, tfDepartment, tfPayee, tfCashFund);

            boolean lbShow = !JFXUtil.isObjectEqualTo(poController.Master().getSourceNo(), null, "");
            JFXUtil.setDisabled(lbShow, cbReverse);
//            initDVMasterTabs();
            poController.computeFields(false);
            JFXUtil.setStatusValue(lblDVTransactionStatus, CashDisbursementStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            tfDVTransactionNo.setText(poController.Master().getTransactionNo() != null ? poController.Master().getTransactionNo() : "");
            dpDVTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfBranch.setText(poController.Master().Branch().getBranchName());
            tfCashFund.setText(poController.Master().CashFund().getDescription());
            tfDepartment.setText(poController.Master().Department().getDescription());
            tfPayee.setText(poController.Master().getPayeeName());

            tfCreditTo.setText(poController.Master().Credited().getCompanyName());
            tfVoucherNo.setText(poController.Master().getVoucherNo());
            tfCashAdvNo.setText(poController.Master().getSourceNo());
            taDVRemarks.setText(poController.Master().getRemarks());

            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotal(), true));
            tfVatableSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatableSales(), true));
            tfVatAmountMaster.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatAmount(), true));
            tfVatZeroRatedSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getZeroVatSales(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatExempt(), true));
            tfLessWHTax.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getWithTaxTotal(), true));
            tfTotalNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getNetTotal(), true));

            taDVRemarks.setText(poController.Master().getRemarks());

            JFXUtil.updateCaretPositions(apDVMaster1, apDVMaster2);
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
            boolean lbShow3 = JFXUtil.isObjectEqualTo(poController.Master().getSourceNo(), null, "");
            String lsParticular = "";
            if (!lbShow3) {
                lsParticular = poController.Detail(pnDetail).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular();
                JFXUtil.setDisabled(false, tfParticularDetail);
            } else {
                //sourceno is empty
                boolean lbShow2 = poController.Detail(pnDetail).getEditMode() == EditMode.UPDATE;
                JFXUtil.setDisabled(lbShow2, tfParticularDetail);
            }
            JFXUtil.setDisabled(!lbShow3, tfORNoDetail, tfAmountDetail);
            boolean lbShow = !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getReferNo(), null, "")
                    && poController.Detail(pnDetail).getAmount() > 0.0000;
            JFXUtil.setDisabled(!lbShow, tfVatExemptDetail);

            tfORNoDetail.setText(poController.Detail(pnDetail).getReferNo());
            tfCashAdvParticular.setText(lsParticular);
            tfParticularDetail.setText(poController.Detail(pnDetail).Particular().getDescription());

            tfVatableSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatSales(), true));
            tfVatExemptDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatExempt(), true));
            tfVatZeroRatedSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailZeroVat(), true));
            tfVatAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatAmount(), true));
            cbReverse.setSelected(poController.Detail(pnDetail).isReverse());
            tfAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount(), true));

            JFXUtil.updateCaretPositions(apDVDetail);
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
//                tfAttachmentSource.setText(poController.TransactionAttachmentSource(pnAttachment));
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

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(documentType, cmbAttachmentType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbJournalProposalStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbJournalProposalStatus);
        cmbJournalProposalStatus.setItems(filteredStatuses);
    }

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbJournalProposalStatus":
                        JFXUtil.Status selectedstat = cmbJournalProposalStatus.getValue();
                        poJSON = poController.JournalProposal(pnMainJEP).Master().setTransactionStatus(selectedstat.getCode());
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterJEP();
                        break;
                }
            }
    );

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                LocalDate transactionDate = null, periodToDate = null, periodFromDate = null;
                String lsTransDate = "", lsPeriodToDate = "", lsPeriodFromDate = "";
                poJSON = new JSONObject();
                switch (datePicker.getId()) {
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
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDVTransactionDate, dpJournalTransactionDate, dpReportMonthYear, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear, dpPeriodFrom, dpPeriodTo);
        JFXUtil.setActionListener(datepicker_Action, dpDVTransactionDate, dpJournalTransactionDate, dpReportMonthYear, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear, dpPeriodFrom, dpPeriodTo);
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
                case "cbBIRReverse":
                    poJSON = poController.removeWTDeduction(pnDetailBIR);
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableDetailBIR.reload();
                    if (checkedBox.isSelected()) {
                        moveNextBIR(false, false);
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
        JFXUtil.setButtonsVisibility(!lbShow, btnBrowse, btnClose, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
        JFXUtil.setButtonsVisibility(fnEditMode == EditMode.READY, btnHistory);

        JFXUtil.setDisabled(!lbShow, apDVMaster1, apDVMaster2, apDVDetail, apJournalMaster, apJournalDetails, apJournalProposalMaster, apJournalProposalDetails, apBIRDetail, apAttachments, apAttachmentButtons);
//        JFXUtil.setButtonsVisibility(fnEditMode == EditMode.UPDATE, btnUndo);
        if (fnEditMode == EditMode.READY) {

            switch (poController.Master().getTransactionStatus()) {
                case CashDisbursementStatus.OPEN:
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                    JFXUtil.setButtonsVisibility(true, btnVoid);
                    break;
//                case CashDisbursementStatus.VERIFIED:
//                    JFXUtil.setButtonsVisibility(true, btnUpdate);
//                    break;
            }
            if (!poController.Master().getTransactionStatus().equals(CashDisbursementStatus.OPEN)) {
                JFXUtil.setButtonsVisibility(false, btnUpdate);
            }

        }
        boolean lbShow4 = !isSourceNoAvailable() && lbShow;
        JFXUtil.setDisabled(!lbShow4, cmbAttachmentType);
        JFXUtil.setDisabled(!lbShow4, btnAddAttachment, btnRemoveAttachment);
    }

    private void clearTextFields() {
        stageAttachment.closeDialog();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apButton, apMasterDetail, apDVMaster1, apDVMaster2, apDVDetail,
                apMainList, apJournalMaster, apJournalDetails, apJournalProposalMaster, apJournalProposalDetails, apBIRDetail, apAttachments);
        filterIndustry();
    }

}
