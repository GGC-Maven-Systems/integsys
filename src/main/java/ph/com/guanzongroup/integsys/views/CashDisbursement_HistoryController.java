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
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
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
public class CashDisbursement_HistoryController implements Initializable, ScreenInterface {

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
    private boolean pbEnteredBIR = false;

    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    private int currentIndex = 0;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    Map<String, String> imageinfo_temp = new HashMap<>();
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableDetailJE, loadTableDetailBIR, loadTableAttachment, loadTableMainJEP, loadTableDetailJEP;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    AnchorPane root = null;
    Scene scene = null;
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    ObservableList<JFXUtil.Status> statusJEP = FXCollections.observableArrayList(
            new JFXUtil.Status(JournalProposalStatus.OPEN, "OPEN"),
            new JFXUtil.Status(JournalProposalStatus.CONFIRMED, "CONFIRMED"),
            new JFXUtil.Status(JournalProposalStatus.POSTED, "POSTED"),
            new JFXUtil.Status(JournalProposalStatus.CANCELLED, "CANCELLED"),
            new JFXUtil.Status(JournalProposalStatus.VOID, "VOID"),
            new JFXUtil.Status(JournalProposalStatus.RETURNED, "RETURNED")
    );

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMasterDetail, apDVMaster1, apDVMaster2, apDVDetail, apJournalMaster, apJournalDetails, apJournalProposalList, apJournalProposalMaster, apJournalProposalDetails, apBIRDetail, apAttachments;
    @FXML
    private Label lblSource, lblDVTransactionStatus, lblJournalTransactionStatus;
    @FXML
    private TextField tfSearchIndustry, tfSearchPayee, tfSearchCashAdvanceNo, tfDVTransactionNo, tfBranch, tfDepartment, tfCashFund, tfPayee, tfCreditTo, tfVoucherNo, tfCashAdvNo, tfTotalAmount, tfVatableSales, tfVatAmountMaster, tfVatZeroRatedSales, tfVatExemptSales, tfLessWHTax, tfTotalNetAmount, tfORNoDetail, tfParticularDetail, tfVatableSalesDetail, tfVatExemptDetail, tfVatZeroRatedSalesDetail, tfVatAmountDetail, tfAmountDetail, tfCashAdvParticular, tfJournalTransactionNo, tfTotalDebitAmount, tfTotalCreditAmount, tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount, tfJournalProposalTransactionNo, tfTotalProposalDebitAmount, tfTotalProposalCreditAmount, tfJournalProposalBranch, tfJournalProposalDepartment, tfJournalProposalAccountCode, tfJournalProposalAccountDescription, tfJournalProposalDebitAmount, tfJournalProposalCreditAmount, tfBIRTransactionNo, tfTaxCode, tfParticular, tfBaseAmount, tfTaxRate, tfTotalTaxAmount, tfAttachmentNo;
    @FXML
    private Button btnBrowse, btnPrint, btnHistory, btnClose, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private Tab tabDetails, tabJournal, tabJournalProposal, tabBIR, tabAttachments;
    @FXML
    private DatePicker dpDVTransactionDate, dpJournalTransactionDate, dpReportMonthYear, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear, dpPeriodFrom, dpPeriodTo;
    @FXML
    private TextArea taDVRemarks, taJournalRemarks, taJournalProposalRemarks;
    @FXML
    private CheckBox cbReverse, cbJEReverse, cbJEMasterProposalReverse, cbJEProposalReverse, cbBIRReverse;
    @FXML
    private TableView tblVwDetails, tblVwJournalDetails, tblVwJournalProposalList, tblVwJournalProposalDetails, tblVwBIRDetails, tblAttachments;
    @FXML
    private TableColumn tblDVRowNo, tblReceiptNoDetail, tblParticularDetail, tblAmountDetail, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales, tblNetAmount, tblJournalRowNo, tblJournalReportMonthYear, tblJournalAccountCode, tblJournalAccountDescription, tblJournalDebitAmount, tblJournalCreditAmount, tblJournalProposalListRowNo, tblJournalProposalListTransNo, tblJournalProposalListBranch, tblJournalProposalListDepartment, tblJournalProposalListDebitAmt, tblJournalProposalListCreditAmt, tblJournalProposalRowNo, tblJournalProposalReportMonthYear, tblJournalProposalAccountCode, tblJournalProposalAccountDescription, tblJournalProposalDebitAmount, tblJournalProposalCreditAmount, tblBIRRowNo, tblBIRParticular, tblTaxCode, tblBaseAmount, tblTaxRate, tblTaxAmount, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private ComboBox cmbJournalProposalStatus, cmbAttachmentType;
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
            initDetailJEGrid();
            initMainJEPGrid();
            initDetailJEPGrid();
            initDetailBIRGrid();
            initAttachmentsGrid();
            initTableOnClick();
            initTabPane();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);

            Platform.runLater(() -> {
                try {
                    poController.Master().setIndustryId(psIndustryId);
                    poController.Master().setCompanyId(psCompanyId);
                    poController.setIndustryId(psIndustryId);
                    poController.setCompanyId(psCompanyId);
                    loadRecordSearch();
                    lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
                    TriggerWindowEvent();
                    filterIndustry();
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
    String lsValidDisbMessage = "Please provide an amount for at least one valid disbursement detail to proceed.";

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
                        if (poController.Detail(0).getAmount() > 0.0000) {
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
                        if (poController.Detail(0).getParticularId() != null && !poController.Detail(0).getParticularId().isEmpty()) {
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
                        if (poController.Detail(0).getAmount() > 0.0000) {
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
                        if (poController.Detail(0).getAmount() > 0.0000) {
                            pbIsCheckedAttachmentTab = true;
                            try {
                                poController.loadAttachments();
                            } catch (GuanzonException | SQLException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnPrint, btnHistory, btnClose, btnArrowLeft, btnArrowRight);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnBrowse":
                    poJSON = poController.SearchTransaction(tfSearchIndustry.getText(), tfSearchPayee.getText(), tfSearchCashAdvanceNo.getText());
                    if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    } else {
                        stageAttachment.closeDialog();
                    }
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                    pnEditMode = poController.getEditMode();
                    poController.populateJournal();
                    break;
                case "btnPrint":
                    poJSON = poController.printTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
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

                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        stageAttachment.closeDialog();
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
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
            if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVoid")) {
                pbIsCheckedJournalTab = false;
                pbIsCheckedBIRTab = false;
                poController.resetTransaction();
                clearTextFields();
                JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnRetrieve", "btnSearch", "btnUndo", "btnArrowRight", "btnArrowLeft", "btnHistory")) {
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

    private void loadTableDetailFromMainJEP() {
        JFXUtil.clearTextFields(apJournalProposalMaster, apJournalProposalDetails);
        pnMainJEP = tblVwJournalProposalList.getSelectionModel().getSelectedIndex();
        loadRecordMasterJEP();
        loadTableDetailJEP.reload();
    }

    public void initLoadTable() {

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
                initButton(pnEditMode);
            }
        });
        tblVwJournalProposalDetails.setOnMouseClicked(event -> {
            if (!journalproposal_data.isEmpty() && event.getClickCount() == 1) {
                int lnRow = Integer.parseInt(journalproposal_data.get(tblVwJournalProposalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnDetailJEP = lnRow;
                loadRecordDetailJEP();
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
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblVwDetails, tblVwJournalDetails, tblVwJournalProposalList, tblVwJournalProposalDetails, tblVwBIRDetails);
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
        JFXUtil.setFocusListener(txtSearch_Focus, apBrowse);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apDVMaster1, apDVMaster2, apDVDetail, apBrowse, apJournalMaster, apJournalDetails, apBIRDetail);
        JFXUtil.adjustColumnForScrollbar(tblVwDetails, tblVwJournalDetails, tblVwBIRDetails, tblAttachments);

        JFXUtil.setCommaFormatter(tfDebitAmount, tfCreditAmount, tfBaseAmount);
        JFXUtil.setCommaFormatter2(tfVatExemptDetail);
        taJournalProposalRemarks.setOnKeyPressed(this::txtField_KeyPressed);
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taDVRemarks);
            JFXUtil.setVerticalScroll(taJournalRemarks);
            JFXUtil.setVerticalScroll(taJournalProposalRemarks);
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
                    case "tfSearchPayee":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");

                        }
                        break;
                    case "tfSearchCashAdvanceNo":
                        if (lsValue.isEmpty()) {
//                            
                        }
                        break;
                }
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextInputControl txtInput = (TextInputControl) event.getSource();
        String lsID = txtInput.getId();
        String lsValue = txtInput.getText() == null ? "" : txtInput.getText();
        poJSON = new JSONObject();
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        if (tfAmountDetail.isFocused()) {
                            pbEnteredDV = true;
                        }
                        if (tfCreditAmount.isFocused()) {
                            pbEnteredJE = true;
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
                            case "tfSearchIndustry":
                                poController.SearchIndustry(lsValue, false);
                                loadRecordSearch();

                                break;
                            case "tfSearchPayee":
                                poJSON = poController.SearchPayee(lsValue, false, true);
                                loadRecordSearch();

                                break;
                            case "tfSearchCashAdvanceNo":
                                poJSON = poController.SearchTransaction(tfSearchIndustry.getText(), tfSearchPayee.getText(), tfSearchCashAdvanceNo.getText());
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    stageAttachment.closeDialog();
                                    loadRecordSearch();
                                    JFXUtil.clickTabByTitleText(tabPaneMain, "Cash Disbursement");
                                    pnEditMode = poController.getEditMode();
                                    poController.populateJournal();
                                    loadTableDetail.reload();
                                    initButton(pnEditMode);
                                }
                                break;
                        }
                        event.consume();
                        break;

                    case UP:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfORNoDetail", "tfAmountDetail", "tfParticularDetail", "tfVatExemptDetail"}, (Runnable) () -> moveNext(true, true)},
                            {new String[]{"tfAccountCode", "tfAccountDescription", "tfCreditAmount"}, (Runnable) () -> moveNextJE(true, true)},
                            {new String[]{"tfTaxCode", "tfParticular", "tfBaseAmount", "tfTaxRate"}, (Runnable) () -> moveNextBIR(true, true)}
                        });
                        event.consume();
                        break;
                    case DOWN:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfORNoDetail", "tfAmountDetail", "tfParticularDetail", "tfVatExemptDetail"}, (Runnable) () -> moveNext(false, true)},
                            {new String[]{"tfAccountCode", "tfAccountDescription", "tfCreditAmount"}, (Runnable) () -> moveNextJE(false, true)},
                            {new String[]{"tfTaxCode", "tfParticular", "tfBaseAmount", "tfTaxRate"}, (Runnable) () -> moveNextBIR(false, true)}
                        });
                        event.consume();
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
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
            {poController.Detail(pnDetail).getAmount(), tfAmountDetail},}, tfAmountDetail); // default
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
            boolean lbShow2 = pnEditMode == EditMode.UPDATE;
            JFXUtil.setDisabled(true, tfBranch, tfDepartment, tfCashFund, tfPayee, tfCreditTo);
            JFXUtil.setDisabled(true, tfAmountDetail);
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

            String lsParticular = "";
            if (!JFXUtil.isObjectEqualTo(poController.Master().getSourceNo(), null, "")) {
                lsParticular = poController.Detail(pnDetail).CashAdvanceDetail(poController.Master().getSourceNo()).getParticular();
            }
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
            String dbValue = poController.JournalProposal(pnMainJEP).Master().getTransactionStatus();
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
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDVTransactionDate, dpJournalTransactionDate, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear, dpReportMonthYear, dpPeriodFrom, dpPeriodTo);
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
            case JournalProposalStatus.CONFIRMED:
                lsMessage = lbIsCbChecked ? "" : "cancel";
                return ShowMessageFX.YesNo(null, pxeModuleName, "This action will " + lsMessage + " the selected proposal.\nWould you like to proceed?");
        }
        return true;
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
    }

    private void initButton(int fnValue) {
        boolean lbShow2 = (fnValue == EditMode.READY);
        boolean lbShow3 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);

        JFXUtil.setButtonsVisibility(lbShow2, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);
        JFXUtil.setButtonsVisibility(lbShow2 && CashDisbursementStatus.APPROVED.equals(poController.Master().getTransactionStatus()), btnPrint);

        JFXUtil.setDisabled(true, apJournalProposalMaster, apJournalProposalDetails, apDVMaster1, apDVMaster2, apDVDetail, apJournalMaster, apJournalDetails, apBIRDetail);
    }

    private void clearTextFields() {
        stageAttachment.closeDialog();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apJournalProposalMaster, apJournalProposalDetails, apButton, apMasterDetail, apDVMaster1, apDVMaster2, apDVDetail,
                apJournalMaster, apJournalDetails, apBIRDetail, apAttachments);
        filterIndustry();
    }

}
