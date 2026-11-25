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
import java.time.LocalDate;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.script.ScriptException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.inv.InvAdjustment;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.inv.status.InvAdjustmentStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.integsys.model.*;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class InventoryAdjustment_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private int pnDetail = 0;
    private int pnAttachment = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private InvAdjustment poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    boolean pbEntered = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelCashDisbursement_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelCashDisbursement_Main> filteredMain_Data;

    private ObservableList<ModelInventoryAdjustment_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEnteredDV = false;
    private int currentIndex = 0;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    Map<String, String> imageinfo_temp = new HashMap<>();
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    AnchorPane root = null;
    Scene scene = null;
    private FileChooser fileChooser;
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    JFXUtil.StageManager stageDialog = new JFXUtil.StageManager();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblStatus, lblTotalDebitQty, lblTotalCreditQty;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnSerial, btnConfirm, btnHistory, btnClose, btnRemove, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private TextField tfTransactionNo, tfBranch, tfReferenceNo, tfCategory, tfBarcode, tfDescription, tfCost, tfCreditQty, tfDebitQty, tfAttachmentNo;
    @FXML
    private DatePicker tfTransactionDate, dpExpiryDate;
    @FXML
    private TextArea taRemarks, taRemarksDetail;
    @FXML
    private TableView tblViewDetail, tblAttachments;
    @FXML
    private TableColumn tblNoDetail, tblBarcode, tblDescription, tblCost, tblDebitQty, tblCreditQty, tblNoAttachments, tblFileName;
    @FXML
    private Tab tabAttachments;
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
            poController = new InvControllers(oApp, null).InventoryAdjustment();
            poJSON = new JSONObject();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initTextFields();
            initDatePicker();
            initDetailGrid();
            initAttachmentsGrid();
            initTableOnClick();
            initTabPane();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;

            initButton(pnEditMode);

            Platform.runLater(() -> {
                try {
                    poController.setIndustryId(psIndustryId);
                    poController.setCompanyId(psCompanyId);
                    poController.setCategoryId(psCategoryId);
                    poController.Master().setBranchCode(oApp.getBranchCode());
                    lblSource.setText(poController.Master().Company().getCompanyName());
                    btnNew.fire();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField);
            initAttachmentPreviewPane();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Transaction Information":
                    if (pnEditMode == EditMode.UNKNOWN) {
                    } else {
                        loadRecordMaster();
                    }
                    break;
                case "Attachments":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apAttachments);
                        try {
                            poController.loadAttachments();
                        } catch (GuanzonException | SQLException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                        loadTableAttachment.reload();
                    }
                    break;
            }
        });
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
                    case "btnHistory":
                        if (poController.getEditMode() != EditMode.READY && poController.getEditMode() != EditMode.UPDATE) {
                            ShowMessageFX.Warning("No parameter status history to load!", pxeModuleName, null);
                            return;
                        }

                        try {
                            poController.ShowStatusHistory();
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No parameter status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            poController.InitTransaction();
                            clearTextFields();

                            poController.setIndustryId(psIndustryId);
                            pnEditMode = EditMode.UNKNOWN;
                            break;
                        } else {
                            return;
                        }
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                loadTableDetail.reload();
                                return;
                            } else {
                                //reshow the highlight
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.Master().getTransactionStatus().equals(InvAdjustmentStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poController.ConfirmTransaction();
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Warning((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }
                                pnEditMode = poController.getEditMode();
                                loadRecordMaster();
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poController.ConfirmTransaction();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        String lsStatus = "";
                        if (InvAdjustmentStatus.CONFIRMED.equals(poController.Master().getTransactionStatus())) {
                            lsStatus = "cancel";
                        } else {
                            lsStatus = "void";
                        }
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsStatus + " transaction?") == true) {
                            if (poController.Master().getTransactionStatus().equals(InvAdjustmentStatus.OPEN)) {
                                poJSON = poController.VoidTransaction();
                            } else {
                                poJSON = poController.CancelTransaction();
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                            }
                        } else {
                            return;
                        }
                        break;

                    case "btnBrowse":
                        poController.setTransactionStatus(InvAdjustmentStatus.OPEN);
                        poJSON = poController.SearchTransaction("", false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnNew":
                        //Clear data
                        poController.ResetTransaction();
                        clearTextFields();

                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poController.initFields();
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apDetail);
                        break;
                    case "btnSerial":
                        openParameter("", "");
                        break;
                    case "btnRemove":
                        if (poController.Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                            poController.Detail().remove(pnDetail);
                        } else {
                            poController.Detail(pnDetail).isReverse(true);
                        }
                        loadTableDetail.reload();
                        moveNext(false, false);
                        return;
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
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnConfirm", "btnApprove", "btnVoid", "btnCancel")) {
//                    resetValues();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnRetrieve", "btnSearch", "btnHistory")) {
                } else {
                    loadTableDetail.reload();
                    loadRecordMaster();
                }
                initButton(pnEditMode);
            }
        } catch (SQLException | GuanzonException | ParseException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void openParameter(String lsAddOnTypeId, String lsVariantId) {
        String lsFXML = "";
        Object controller = new Object();
        InventoryAdjustment_SerialController controller1 = new InventoryAdjustment_SerialController(); //Should differ the parameter calling
        lsFXML = "/ph/com/guanzongroup/integsys/views/InventoryAdjustment_Serial.fxml";

        controller1.setGRider(oApp);
        controller1.setIndustryID(psIndustryId);
        controller1.setCompanyID(psCompanyId);
        controller1.setEntryNo(poController.Detail(pnDetail).getEntryNo());
        controller = controller1;

        poJSON = new JSONObject();
        if (stageDialog != null) {
            stageDialog.closeDialog();
            stageDialog = new JFXUtil.StageManager();
        } else {
            stageDialog = new JFXUtil.StageManager();
        }
        try {
            stageDialog.setOnHidden(event -> {
                stageDialog = null;
                loadTableDetail.reload();
            });
            stageDialog.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource(lsFXML), controller, "Inventory Adjustment Serial Dialog", true, false, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            details_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            int lnRowCount = 0;
                            String lsParticular = "", lsOrNo = "";
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (!poController.Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelInventoryAdjustment_Detail(String.valueOf(lnRowCount),
                                                poController.Detail(lnCtr).Inventory().getBarCode(),
                                                poController.Detail(lnCtr).Inventory().getDescription(),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getInventoryCost(), false)),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDebitQuantity(), false),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getCreditQuantity(), false),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 11); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetail, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex07());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetail, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex07());
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
        JFXUtil.setColumnCenter(tblNoAttachments);
        JFXUtil.setColumnLeft(tblFileName);
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
        JFXUtil.setColumnCenter(tblNoDetail);
        JFXUtil.setColumnLeft(tblBarcode, tblDescription);
        JFXUtil.setColumnRight(tblCost, tblDebitQty, tblCreditQty);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);
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
        tblViewDetail.setOnMouseClicked(event -> {
            if (!details_data.isEmpty() && event.getClickCount() == 1) {
                ModelInventoryAdjustment_Detail selected = (ModelInventoryAdjustment_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex07());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetail = newIndex;
                    loadRecordDetail();
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
        //Initialise  TextField Focus
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks, taRemarksDetail);
        JFXUtil.setFocusListener(txtMaster_Focus, tfBranch, tfReferenceNo, tfCategory);
        JFXUtil.setFocusListener(txtDetail_Focus, tfBarcode, tfDescription, tfCost, tfCreditQty, tfDebitQty);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail);
        JFXUtil.setCommaFormatter(tfCost);
        CustomCommonUtil.inputIntegersOnly(tfCreditQty, tfDebitQty);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewDetail, tblAttachments);
    }

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                    case "taRemarksDetail":
                        poJSON = poController.Detail(pnDetail).setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordDetail();
                        break;
                }
            });
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBranch":
                        break;
                    case "tfReferenceNo":
                        poJSON = poController.Master().setReferenceNo(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfCategory":
                        if (lsValue.isEmpty()) {
                            poController.Master().setCategoryCode("");
                        }
                        break;
                }
                loadRecordMaster();
            });
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBarcode":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setStockId("");
                        }
                        break;
                    case "tfDescription":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setStockId("");
                        }
                        break;
                    case "tfCost":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setInventoryCost(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadTableDetail.reload();
                        break;
                    case "tfCreditQty":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setCreditQuantity(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadTableDetail.reload();
                        break;
                    case "tfDebitQty":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setDebitQuantity(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadTableDetail.reload();
                        break;
                }
                JFXUtil.runWithDelay(.50, () -> {
                    loadTableDetail.reload();
                });
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
                        pbEntered = true;
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            //apMaster
                            case "tfCategory":
                                poJSON = poController.SearchCategory(lsValue, false);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    JFXUtil.textFieldMoveNext(taRemarks);
                                }
                                break;
                            //apDetail
                            case "tfBarcode":
                                poJSON = poController.SearchInventory(lsValue, false, pnDetail);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    JFXUtil.textFieldMoveNext(dpExpiryDate);
                                }
                                break;
                            case "tfDescription":
                                poJSON = poController.SearchInventory(lsValue, false, pnDetail);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    JFXUtil.textFieldMoveNext(dpExpiryDate);
                                }
                                break;
                        }
                        event.consume();
                        break;
                    case UP:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfBarcode", "tfDebitQty"}, (Runnable) () -> moveNext(true, true)},});
                        event.consume();
                        break;
                    case DOWN:
                        JFXUtil.altSwitch(lsID, new Object[][]{
                            {new String[]{"tfBarcode", "tfDebitQty"}, (Runnable) () -> moveNext(false, true)},});
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
        try {
            if (continueNext) {
                apDetail.requestFocus();
                pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex07())
                        : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex07());
            }
            loadRecordDetail();
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnDetail).Inventory().getBarCode(), tfBarcode},
                {poController.Detail(pnDetail).getDebitQuantity(), tfDebitQty},}, tfDebitQty); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMaster() {
        try {
            JFXUtil.setStatusValue(lblStatus, InvAdjustmentStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            tfTransactionNo.setText(poController.Master().getTransactionNo());
            String lsTransactionDate = JFXUtil.formatDateToString(poController.Master().getTransactionDate());
            JFXUtil.setDateValue(tfTransactionDate, JFXUtil.isObjectEqualTo(lsTransactionDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            tfBranch.setText(poController.Master().Branch().getBranchName());
            tfReferenceNo.setText(poController.Master().getReferenceNo());
            tfCategory.setText(poController.Master().Category().getDescription());
            taRemarks.setText(poController.Master().getRemarks());

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
            tfBarcode.setText(poController.Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.Detail(pnDetail).Inventory().getDescription());
            dpExpiryDate.setValue(poController.Detail(pnDetail).getExpiryDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Detail(pnDetail).getExpiryDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getInventoryCost(), false));
            tfCreditQty.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getCreditQuantity(), false));
            tfDebitQty.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDebitQuantity(), false));
            taRemarksDetail.setText(poController.Detail(pnDetail).getRemarks());

//            lblTotalDebitQty.setText(poController);
//            lblTotalCreditQty.setText(poController);
            JFXUtil.updateCaretPositions(apDetail);
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

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                LocalDate transactionDate = null, periodToDate = null, periodFromDate = null;
                String lsTransDate = "", lsPeriodToDate = "", lsPeriodFromDate = "";
                poJSON = new JSONObject();
                switch (datePicker.getId()) {
                    case "tfTransactionDate":
                        break;
                    case "dpExpiryDate":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                            if (pbSuccess && (ldSelectedDate.isAfter(periodToDate))) {
//                                JFXUtil.setJSONError(poJSON, "Period From cannot be later than the \"Period To\" date.");
//                                pbSuccess = false;
//                            }
                            if (pbSuccess) {
                                poController.Detail(pnDetail).setExpiryDate(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordDetail();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:
                        break;
                }
            });

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", tfTransactionDate, dpExpiryDate);
        JFXUtil.setActionListener(datepicker_Action, tfTransactionDate, dpExpiryDate);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnSerial, btnConfirm);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail);
        JFXUtil.setButtonsVisibility(lbShow1 || lbShow2, btnSerial);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case InvAdjustmentStatus.VOID:
            case InvAdjustmentStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnConfirm);
                break;
        }
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apAttachments);
        lblTotalDebitQty.setText("0.00");
        lblTotalCreditQty.setText("0.00");
    }
}
