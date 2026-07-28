/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import ph.com.guanzongroup.integsys.model.ModelPettyCashDisbursement_Detail;
import ph.com.guanzongroup.integsys.model.ModelCashDisbursement_Main;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import ph.com.guanzongroup.cas.cashflow.PettyCashDisbursement;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.PettyCashDisbursementStatus;
import ph.com.guanzongroup.integsys.model.ModelBIR_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class PettyCashDisbursement_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private int pnMain = 0;
    private int pnDetail = 0;
    private int pnAttachment = 0;
    private boolean pbIsCheckedAttachmentTab = false;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private PettyCashDisbursement poController;
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

    private ObservableList<ModelPettyCashDisbursement_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> journal_data = FXCollections.observableArrayList();
    private ObservableList<ModelBIR_Detail> BIR_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEnteredDV = false;

    private int currentIndex = 0;
    Map<String, String> imageinfo_temp = new HashMap<>();
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    AnchorPane root = null;
    Scene scene = null;
    private FileChooser fileChooser;
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;

    @FXML
    private AnchorPane AnchorMain, apButton, apMasterDetail, apDVMaster1, apDVMaster2, apDVDetail, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblDVTransactionStatus;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnVoid, btnHistory, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private Tab tabDetails, tabAttachments;
    @FXML
    private TextField tfDVTransactionNo, tfBranch, tfDepartment, tfPettyCashFund, tfPayee, tfCreditTo, tfTotalAmount, tfVoucherNo, tfReferNo, tfParticularDetail, tfAmountDetail, tfAttachmentNo;
    @FXML
    private DatePicker dpDVTransactionDate;
    @FXML
    private TextArea taDVRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblVwDetails, tblAttachments;
    @FXML
    private TableColumn tblDVRowNo, tblParticularDetail, tblAmountDetail, tblRowNoAttachment, tblFileNameAttachment;
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
            poController = new CashflowControllers(oApp, null).PettyCashDisbursement();
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
            initAttachmentsGrid();
            initTableOnClick();
            initTabPane();
            initComboboxes();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;

            initButton(pnEditMode);

            Platform.runLater(() -> {
                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
//                poController.setCategoryID(psCategoryId);
                poController.Master().setBranchCode(oApp.getBranchCode());
                loadRecordSearch();
                btnNew.fire();
                TriggerWindowEvent();
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
        return true;
    }
    String lsValidDisbMessage = "Please provide at least one valid disbursement detail with amount to proceed.";

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Petty Cash Disbursement":
                    if (pnEditMode == EditMode.UNKNOWN) {
                    } else {
                        loadRecordMaster();
                    }
                    break;
                case "Attachments":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apAttachments);
                        if (DoesContainValidDisbDetail()) {
                            pbIsCheckedAttachmentTab = true;
                            try {
                                poController.loadAttachments();
                            } catch (GuanzonException | SQLException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }

                            loadTableAttachment.reload();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Petty Cash Disbursement");
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
        List<Button> buttons = Arrays.asList(btnRemoveAttachment, btnAddAttachment, btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnVoid, btnHistory, btnClose, btnArrowRight, btnArrowLeft);
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
                    poController.setTransactionStatus(PettyCashDisbursementStatus.OPEN);
                    poJSON = poController.SearchTransaction();
                    if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Petty Cash Disbursement");
                    pnEditMode = poController.getEditMode();
//                    psSupplierPayeeId = poController.Master().Payee().getClientID();
                    loadTableDetail.reload();
                    break;
                case "btnNew":
                    clearTextFields();
                    poController.initFields();
                    poJSON = poController.NewTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }

//                    poController.Master().setDisbursementType(PettyCashDisbursementStatus.DisbursementType.CHECK);
//                    poController.Master().setSupplierClientID(psSupplierPayeeId);
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Petty Cash Disbursement");
                    loadTableDetail.reload();
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnUpdate":
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
                    pnEditMode = poController.getEditMode();
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Petty Cash Disbursement");
                    loadTableDetail.reload();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apDVMaster1, apDVDetail);
                    break;
                case "btnSave":
                    //Recheck transaction status
                    if (pnEditMode == EditMode.UPDATE) {
                        poJSON = poController.checkUpdateTransaction(true);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                    }

                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                        return;
                    }
                    poJSON = poController.SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }

                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                    //Arsiela 03-05-2026 Moved to class save others
//                    poJSON = poController.updatePaymentsStatus();
//                    if ("error".equals(poJSON.get("result"))) {
//                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                    }
                    poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                    if ("success".equals(poJSON.get("result"))) {
                        pnEditMode = poController.getEditMode();
                        initButton(pnEditMode);
                    }
                    if (pnEditMode == EditMode.READY) {
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) { //requires to review journal entry
                            poJSON = poController.ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                    }
                    Platform.runLater(() -> btnNew.fire());
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
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
                            try (PDDocument document = PDDocument.load(selectedFile)) {
                                PDFRenderer pdfRenderer = new PDFRenderer(document);
                                int pageCount = document.getNumberOfPages();
                                if (pageCount > 20) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "PDF exceeds maximum allowed pages.");
                                    return;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(PettyCashDisbursement_EntryController.class.getName()).log(Level.SEVERE, null, ex);
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
            if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVoid")) {
                poController.resetTransaction();
//                poController.Master().setSupplierClientID(psSupplierPayeeId);
                clearTextFields();
                JFXUtil.clickTabByTitleText(tabPaneMain, "Petty Cash Disbursement");
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnRetrieve", "btnSearch", "btnUndo", "btnArrowRight", "btnArrowLeft", "btnHistory")) {
            } else {
                loadRecordMaster();
                loadTableDetail.reload();
                loadTableAttachment.reload();
            }
            initButton(pnEditMode);
            if (lsButton.equals("btnUpdate")) {
                moveNext(false, false);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (IOException ex) {
            Logger.getLogger(PettyCashDisbursement_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                                poController.ReloadDetail();
//                                poJSON = poController.computeDetailFields(true);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                }
                            }
                            int lnRowCount = 0;
                            String lsParticular = "", lsOrNo = "";
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (!poController.Detail(lnCtr).isReverse()) {
                                    continue;
                                }

                                lsParticular = poController.Detail(lnCtr).Particular().getDescription();
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelPettyCashDisbursement_Detail(String.valueOf(lnRowCount),
                                                lsParticular,
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), true),
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 4); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwDetails, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex04());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwDetails, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblVwDetails.getSelectionModel().getSelectedIndex()).getIndex04());
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
        JFXUtil.setColumnCenter(tblDVRowNo);
        JFXUtil.setColumnLeft(tblParticularDetail);
        JFXUtil.setColumnRight(tblAmountDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwDetails);
        tblVwDetails.setItems(details_data);
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
                ModelPettyCashDisbursement_Detail selected = (ModelPettyCashDisbursement_Detail) tblVwDetails.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(details_data.get(tblVwDetails.getSelectionModel().getSelectedIndex()).getIndex04());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });

        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblVwDetails);
        JFXUtil.adjustColumnForScrollbar(tblVwDetails);
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
                    newIndex = moveDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex04())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex04());
                    pnDetail = newIndex;
                    loadRecordDetail();
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
        JFXUtil.setFocusListener(txtArea_Focus, taDVRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, apDVMaster1, apDVMaster2);
        JFXUtil.setFocusListener(txtDetail_Focus, apDVDetail);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apDVMaster1, apDVMaster2, apDVDetail);
        JFXUtil.adjustColumnForScrollbar(tblVwDetails, tblAttachments);

        JFXUtil.setCommaFormatter(tfAmountDetail);
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taDVRemarks);
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
        JFXUtil.handleDisabledNodeClick(apDVMaster1, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "tfPettyCash":
                case "tfPayee":
                case "tfDepartment":
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "This field cannot be modified if the transaction is non-open and contains a reference number.");
                    break;

            }
        });
    }

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
                }
            });
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfBranch":
                        if (lsValue.isEmpty()) {
                            poController.Master().setBranchCode("");
                        }
                        break;
                    case "tfPettyCashFund":
                        if (lsValue.isEmpty()) {
                            poController.Master().setPettyId("");
                        }
                        break;
                    case "tfDepartment":
                        if (lsValue.isEmpty()) {
                            poController.Master().setDepartmentRequest("");
                        }
                        break;
                    case "tfPayee":
                        if (lsValue.isEmpty()) {

                            poController.Master().setPayeeName("");
                        }
                        break;
                    case "tfCreditTo":
                        if (lsValue.isEmpty()) {
                            poController.Master().setCreditedTo("");
                        }
                        break;
                    case "tfReferNo":
                        poJSON = poController.Master().setReferNo(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfParticularDetail":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setParticularId("");
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
                JFXUtil.runWithDelay(
                        0.50, () -> {
                            loadTableDetail.reload();
                        }
                );
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

                        if (tfAmountDetail.isFocused()) {
                            pbEnteredDV = true;
                        }
                        CommonUtils.SetNextFocus(txtField);
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfBranch":
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
                                poJSON = poController.SearchDepartment(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMaster();
                                    break;
                                } else {
                                    JFXUtil.textFieldMoveNext(tfPettyCashFund);
                                }
                                loadRecordMaster();
                                break;
                            case "tfPettyCashFund":
                                poJSON = poController.SearchPettyCash(lsValue, false);
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

                                    int lnReturned = Integer.parseInt(!JFXUtil.isObjectEqualTo(poJSON.get("row"), null, "")
                                            ? String.valueOf(poJSON.get("row"))
                                            : String.valueOf(pnDetail));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetail = lnReturned;
                                        loadTableDetail.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetail = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadTableDetail.reload();
                                    JFXUtil.textFieldMoveNext(tfAmountDetail);
                                }
                                break;

                        }
                        event.consume();
                        break;

                    case UP:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfAmountDetail", "tfParticularDetail"}, (Runnable) () -> moveNext(true, true)}
                        });
                        event.consume();
                        break;
                    case DOWN:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfAmountDetail", "tfParticularDetail"}, (Runnable) () -> moveNext(false, true)}
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
            pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblVwDetails)).getIndex04())
                    : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblVwDetails)).getIndex04());
        }
        loadRecordDetail();
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.Detail(pnDetail).getParticularId(), tfParticularDetail},
            {poController.Detail(pnDetail).getAmount(), tfAmountDetail},}, tfAmountDetail); // default
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMaster() {
        try {
            boolean lbShow3 = JFXUtil.isObjectEqualTo(poController.Master().getTransactionStatus(), PettyCashDisbursementStatus.OPEN);
            JFXUtil.setDisabled(!lbShow3, tfCreditTo);

            boolean lbShow4 = JFXUtil.isObjectEqualTo(poController.Master().getReferNo(), null, "");
            JFXUtil.setDisabled(!lbShow3 || !lbShow4, tfDepartment, tfPayee, tfPettyCashFund);

            poController.computeFields(false);
            JFXUtil
                    .setStatusValue(lblDVTransactionStatus, PettyCashDisbursementStatus.class,
                            pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            tfDVTransactionNo.setText(poController.Master().getTransactionNo() != null ? poController.Master().getTransactionNo() : "");
            dpDVTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfBranch.setText(poController.Master().Branch().getBranchName());
            tfDepartment.setText(poController.Master().Department().getDescription());
            tfPettyCashFund.setText(poController.Master().PettyCash().getDescription());
            tfPayee.setText(poController.Master().getPayeeName());

            tfCreditTo.setText(poController.Master().Credited().getCompanyName());
            tfVoucherNo.setText(poController.Master().getVoucherNo());
            taDVRemarks.setText(poController.Master().getRemarks());
            tfReferNo.setText(poController.Master().getReferNo());
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotal(), true));

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

            boolean lbShow2 = poController.Detail(pnDetail).getEditMode() == EditMode.UPDATE;
            JFXUtil.setDisabled(lbShow2, tfParticularDetail);

            tfParticularDetail.setText(poController.Detail(pnDetail).Particular().getDescription());
            cbReverse.setSelected(poController.Detail(pnDetail).isReverse());
            cbReverse.setSelected(poController.Detail(pnDetail).isReverse());
            tfAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount(), true));
            JFXUtil.updateCaretPositions(apDVDetail);
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
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                LocalDate transactionDate = null, periodToDate = null, periodFromDate = null;
                String lsTransDate = "", lsPeriodToDate = "", lsPeriodFromDate = "";
                poJSON = new JSONObject();
                switch (datePicker.getId()) {

                    default:
                        break;
                }
            });

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDVTransactionDate);
        JFXUtil.setActionListener(datepicker_Action, dpDVTransactionDate);
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
            }
        }
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(!lbShow, btnBrowse, btnClose, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
        JFXUtil.setButtonsVisibility(fnEditMode == EditMode.READY, btnHistory);

        JFXUtil.setDisabled(!lbShow, apDVMaster1, apDVMaster2, apDVDetail, apAttachments, apAttachmentButtons);
//        JFXUtil.setButtonsVisibility(fnEditMode == EditMode.UPDATE, btnUndo);
        if (fnEditMode == EditMode.READY) {

            switch (poController.Master().getTransactionStatus()) {
                case PettyCashDisbursementStatus.OPEN:
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                    JFXUtil.setButtonsVisibility(true, btnVoid);
                    break;
//                case PettyCashDisbursementStatus.VERIFIED:
//                    JFXUtil.setButtonsVisibility(true, btnUpdate);
//                    break;
            }
            if (!poController.Master().getTransactionStatus().equals(PettyCashDisbursementStatus.OPEN)) {
                JFXUtil.setButtonsVisibility(false, btnUpdate);
            }

        }

    }

    private void clearTextFields() {
        stageAttachment.closeDialog();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apButton, apMasterDetail, apDVMaster1, apDVMaster2, apDVDetail, apAttachments);
    }

}
