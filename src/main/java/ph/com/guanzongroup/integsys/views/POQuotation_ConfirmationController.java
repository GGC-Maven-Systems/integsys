package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelPOQuotation_Detail;
import ph.com.guanzongroup.integsys.model.ModelPOQuotation_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
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
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import static javafx.scene.input.KeyCode.F3;
import javafx.stage.Stage;
import org.guanzon.appdriver.constant.DocumentType;
import javafx.util.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.purchasing.services.QuotationControllers;
import org.guanzon.cas.purchasing.status.POQuotationStatus;

/**
 *
 * @author Team 2
 */
public class POQuotation_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static QuotationControllers poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String openedAttachment = "";
    private boolean pbEntered = false;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    private ObservableList<ModelPOQuotation_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelPOQuotation_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    private FilteredList<ModelPOQuotation_Main> filteredData;
    private FilteredList<ModelPOQuotation_Detail> filteredDataDetail;
    Map<String, String> imageinfo_temp = new HashMap<>();

    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain, loadTableAttachment;

    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    AnchorPane root = null;
    Scene scene = null;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSearchBranch, tfSearchSupplier, tfSearchCategory, tfSearchReferenceNo, tfSearchDepartment, tfTransactionNo, tfReferenceNo, tfCompany, tfBranch, tfDepartment, tfSupplier, tfAddress, tfSourceNo, tfCategory, tfTerm, tfContact, tfGrossAmount, tfDiscRate, tfAddlDiscAmt, tfFreight, tfVATAmount, tfTransactionTotal, tfReplaceId, tfReplaceDescription, tfUnitPrice, tfDiscRateDetail, tfAddlDiscAmtDetail, tfQuantity, tfCost, tfBrand, tfModel, tfColor, tfMeasure, tfAttachmentNo;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnVoid, btnReturn, btnHistory, btnRetrieve, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabInformation, tabAttachments;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate, dpValidityDate;
    @FXML
    private CheckBox cbVatable, cbReverse;
    @FXML
    private TextArea taRemarks, taDescription;
    @FXML
    private TableView tblViewTransDetails, tblAttachments, tblViewMainList;
    @FXML
    private TableColumn tblRowNoDetail, tblDescriptionDetail, tblReplacementDetail, tblUnitPriceDetail, tblDiscountDetail, tblQuantityDetail, tblTotalDetail, tblRowNoAttachment, tblFileNameAttachment, tblRowNo, tblBranch, tblSupplier, tblDate, tblReferenceNo, tblTransactionTotal;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;
    @FXML
    private Pagination pgPagination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poController = new QuotationControllers(oApp, null);
        poJSON = new JSONObject();
        try {
            poJSON = poController.POQuotation().InitTransaction(); // Initialize transaction
        } catch (Exception e) {
            poJSON.put("message", "Error in Initialize");
        }
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initAttachmentsGrid();
        initTableOnClick();
        clearTextFields();
        initLoadTable();

        Platform.runLater(() -> {
            poController.POQuotation().Master().setIndustryId(psIndustryId);
            poController.POQuotation().Master().setCompanyId(psCompanyId);
            poController.POQuotation().setIndustryId(psIndustryId);
            poController.POQuotation().setCompanyId(psCompanyId);
            poController.POQuotation().setCategoryId(psCategoryId);
            poController.POQuotation().initFields();
            poController.POQuotation().setWithUI(true);
            loadRecordSearch();
            TriggerWindowEvent();
        });

        initAttachmentPreviewPane();

        pgPagination.setPageCount(1);

        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
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
    ChangeListener<Scene> WindowKeyEvent = (obs, oldScene, newScene) -> {
        if (newScene != null) {
            setKeyEvent(newScene);
        }
    };

    public void TriggerWindowEvent() {
        root = (AnchorPane) apMainAnchor;
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

                if (JFXUtil.isObjectEqualTo(poController.POQuotation().getEditMode(), EditMode.ADDNEW, EditMode.READY, EditMode.UPDATE)) {
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
        openedAttachment = "";
        if (poController.POQuotation().getTransactionAttachmentCount() <= 0) {
            ShowMessageFX.Warning(null, pxeModuleName, "No transaction attachment to load.");
            return;
        }
        openedAttachment = poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getTransactionNo();
        Map<String, Pair<String, String>> data = new HashMap<>();
        data.clear();
        int lnCount = 0;
        for (int lnCtr = 0; lnCtr < poController.POQuotation().getTransactionAttachmentCount(); lnCtr++) {
            if (RecordStatus.INACTIVE.equals(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                continue;
            }
            lnCount += 1;
            data.put(String.valueOf(lnCount), new Pair<>(String.valueOf(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getFileName()),
                    poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getDocumentType()));
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

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbVatable":
                    poController.POQuotation().Master().isVatable(checkedBox.isSelected());
                    loadRecordMaster();
                    break;
                case "cbReverse":
                    if (poController.POQuotation().Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                        if (poController.POQuotation().Master().getSourceNo() != null && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                            if (!checkedBox.isSelected()) {
                                poController.POQuotation().ReverseItem(pnDetail);
                            } else {
                                poController.POQuotation().Detail(pnDetail).isReverse(checkedBox.isSelected());
                            }
                        } else {
                            poController.POQuotation().Detail().remove(pnDetail);
                        }
                    } else {
                        poController.POQuotation().Detail(pnDetail).isReverse(checkedBox.isSelected());
                    }
                    loadTableDetail.reload();
                    if (checkedBox.isSelected()) {
                        moveNext(false, false);
                    }
                    break;
            }
        }
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
                            stageAttachment.closeDialog();
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poController.POQuotation().OpenTransaction(poController.POQuotation().Master().getTransactionNo());
                        poJSON = poController.POQuotation().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poController.POQuotation().loadAttachments();
                        pnEditMode = poController.POQuotation().getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
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
                            poController.POQuotation().ShowStatusHistory();
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnRetrieve":
                        retrievePOQuotation();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.POQuotation().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.POQuotation().AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poController.POQuotation().OpenTransaction(poController.POQuotation().Master().getTransactionNo());
                                poController.POQuotation().loadAttachments();
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.POQuotation().Master().getTransactionStatus().equals(POQuotationStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poController.POQuotation().ConfirmTransaction("Confirmed");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poController.POQuotation().ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to void transaction?") == true) {
                            if (POQuotationStatus.CONFIRMED.equals(poController.POQuotation().Master().getTransactionStatus())) {
                                poJSON = poController.POQuotation().CancelTransaction("");
                            } else {
                                poJSON = poController.POQuotation().VoidTransaction("");
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnReturn":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to return transaction?") == true) {
                            poJSON = poController.POQuotation().ReturnTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAC898", highlightedRowsMain);
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

                            String imgPath2 = selectedFile.getName().toString();
                            for (int lnCtr = 0; lnCtr <= poController.POQuotation().getTransactionAttachmentCount() - 1; lnCtr++) {
                                if (imgPath2.equals(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getFileName())
                                        && RecordStatus.ACTIVE.equals(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
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
                                }
                            }

//                            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data, poController.POQuotation().addAttachment(imgPath2), 3);
//                            pnAttachment = lnTempRow;
                            pnAttachment = poController.POQuotation().addAttachment(imgPath2);
                            //Copy file to Attachment path
                            poController.POQuotation().copyFile(selectedFile.toString());
                            loadTableAttachment.reload();
                            tblAttachments.getFocusModel().focus(pnAttachment);
                            tblAttachments.getSelectionModel().select(pnAttachment);
                        }
                        break;
                    case "btnRemoveAttachment":
                        if (poController.POQuotation().getTransactionAttachmentCount() <= 0) {
                            return;
                        } else {
                            for (int lnCtr = 0; lnCtr < poController.POQuotation().getTransactionAttachmentCount(); lnCtr++) {
                                if (RecordStatus.INACTIVE.equals(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    if (pnAttachment == lnCtr) {
                                        return;
                                    }
                                }
                            }
                        }
                        poJSON = poController.POQuotation().removeAttachment(pnAttachment);
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
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnConfirm", "btnDisapprove", "btnVoid", "btnCancel")) {
                    poController.POQuotation().resetMaster();
                    poController.POQuotation().resetOthers();
                    poController.POQuotation().Detail().clear();
                    imageView.setImage(null);
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();

                    poController.POQuotation().Master().setIndustryId(psIndustryId);
                    poController.POQuotation().setCompanyId(psCompanyId);
                    poController.POQuotation().Master().setCategoryCode(psCategoryId);
//                    poController.POQuotation().initFields();
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnPrint", "btnAddAttachment", "btnRemoveAttachment",
                        "btnArrowRight", "btnArrowLeft", "btnRetrieve", "btnSearch")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                    loadTableAttachment.reload();
                }
                initButton(pnEditMode);
                if (lsButton.equals("btnUpdate")) {
                    moveNext(false, false);
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void retrievePOQuotation() {
        poJSON = new JSONObject();
        poController.POQuotation().setTransactionStatus(POQuotationStatus.OPEN + POQuotationStatus.CONFIRMED + POQuotationStatus.RETURNED);
        poJSON = poController.POQuotation().loadPOQuotationList(tfSearchBranch.getText(), tfSearchDepartment.getText(), tfSearchSupplier.getText(), tfSearchCategory.getText(),
                tfSearchReferenceNo.getText(), false);
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain.reload();
        }
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
//                try {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfReferenceNo":
                        poJSON = poController.POQuotation().Master().setReferenceNo(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                    case "tfTerm":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.POQuotation().Master().setTerm("");
                        }
                        break;
                    case "tfDiscRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.POQuotation().computeDiscountRate(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        poJSON = poController.POQuotation().Master().setDiscountRate(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                    case "tfAddlDiscAmt":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.POQuotation().computeDiscount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        poJSON = poController.POQuotation().Master().setAdditionalDiscountAmount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                    case "tfFreight":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (Double.valueOf(lsValue) > poController.POQuotation().Master().getTransactionTotal().doubleValue()) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid freight amount");
                            break;
                        }

                        poJSON = poController.POQuotation().Master().setFreightAmount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                }
                loadRecordMaster();
//                } catch (SQLException | GuanzonException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//                }
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
                    case "taRemarks"://Remarks
                        poJSON = poController.POQuotation().Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                if (lsValue == null) {
                    return;
                }
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSearchBranch":
                        if (lsValue.equals("")) {
                            poController.POQuotation().setSearchBranch("");
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchDepartment":
                        if (lsValue.equals("")) {
                            poController.POQuotation().setSearchDepartment("");
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchSupplier":
                        if (lsValue.equals("")) {
                            poController.POQuotation().setSearchSupplier("");
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchCategory":
                        if (lsValue.equals("")) {
                            poController.POQuotation().setSearchCategory("");
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchReferenceNo":
                        break;
                }
            });
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfDescription":
                        if (lsValue.isEmpty()) {
                            if (poController.POQuotation().Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                                poController.POQuotation().Detail(pnDetail).setStockId("");
                                poController.POQuotation().Detail(pnDetail).setDescription("");
                                poController.POQuotation().Detail(pnDetail).setReplaceId("");
                                poController.POQuotation().Detail(pnDetail).setReplaceDescription("");
                            }
                        }
                        break;
                    case "tfReplaceId":
                    case "tfReplaceDescription":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.POQuotation().RemovedExcessRequestItem(pnDetail);
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }

                            poController.POQuotation().Detail(pnDetail).setReplaceId("");
                            poController.POQuotation().Detail(pnDetail).setReplaceDescription("");
                        }
                        break;
                    case "tfUnitPrice":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.POQuotation().Detail(pnDetail).setUnitPrice((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                    case "tfDiscRateDetail":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (Double.valueOf(lsValue) > 100.00) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid discount rate.");
                            break;
                        }

                        poJSON = poController.POQuotation().computeCost(pnDetail, Double.valueOf(lsValue), poController.POQuotation().Detail(pnDetail).getDiscountAmount());
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        poJSON = poController.POQuotation().Detail(pnDetail).setDiscountRate((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        }
                        break;
                    case "tfAddlDiscAmtDetail":
                        lsValue = JFXUtil.removeComma(lsValue);

                        poJSON = poController.POQuotation().computeCost(pnDetail, poController.POQuotation().Detail(pnDetail).getDiscountRate(), Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        poJSON = poController.POQuotation().Detail(pnDetail).setDiscountAmount((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        break;
                    case "tfQuantity":
                        lsValue = JFXUtil.removeComma(lsValue);

                        poJSON = poController.POQuotation().Detail(pnDetail).setQuantity((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        if (pbEntered) {
                            moveNext(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });

    public void moveNext(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDetail.requestFocus();
            boolean lbContinue = true;
            while (lbContinue) {
                pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewTransDetails)).getIndex08())
                        : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewTransDetails)).getIndex08());
                if (poController.POQuotation().Detail(pnDetail).isReverse()) {
                    lbContinue = false;
                }
            }
        }
        loadRecordDetail();
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.POQuotation().Detail(pnDetail).getDescription(), taDescription},
            {poController.POQuotation().Detail(pnDetail).getReplaceId(), tfReplaceId}, // if null or empty, then requesting focus to the txtfield
            {poController.POQuotation().Detail(pnDetail).getReplaceDescription(), tfReplaceDescription},
            {poController.POQuotation().Detail(pnDetail).getUnitPrice(), tfCost},
            {poController.POQuotation().Detail(pnDetail).getDiscountRate(), tfDiscRateDetail},
            {poController.POQuotation().Detail(pnDetail).getDiscountAmount(), tfAddlDiscAmtDetail},
            {poController.POQuotation().Detail(pnDetail).getQuantity(), tfQuantity},}, tfQuantity); // default
    }

    private void txtArea_KeyPressed(KeyEvent event) {
        try {
            if (!(event.getSource() instanceof TextInputControl)) {
                return;
            }
            TextInputControl input = (TextInputControl) event.getSource();
            String lsID = input.getId();
            String lsValue = input.getText() == null ? "" : input.getText();
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case UP:
                    if (JFXUtil.isObjectEqualTo(lsID, "taDescription")) {
                        moveNext(true, true);
                        event.consume();
                    }
                    break;
                case DOWN:
                    if (JFXUtil.isObjectEqualTo(lsID, "taDescription")) {
                        moveNext(false, true);
                        event.consume();
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "taDescription":
                            poJSON = poController.POQuotation().SearchRequestItem(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                taDescription.setText("");
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (poJSON.get("row") == null) {
                                    return;
                                }
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                JFXUtil.runWithDelay(0.70, () -> {
                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 8);
                                    pnDetail = lnTempRow;
                                    loadTableDetail.reload();
                                });
                                break;
                            } else {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.80, () -> {
//                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 7);
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                loadTableDetail.reload();
                                if (!JFXUtil.isObjectEqualTo(poController.POQuotation().Detail(pnDetail).getDescription(), null, "")) {
                                    if (!JFXUtil.isObjectEqualTo(poController.POQuotation().Master().getSourceNo(), null, "")) {
                                        JFXUtil.textFieldMoveNext(tfReplaceId);
                                    } else {
                                        JFXUtil.textFieldMoveNext(tfUnitPrice);
                                    }
                                }
                            }
                            return;
                    }
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    if (tfQuantity.isFocused()) {
                        pbEntered = true;
                    }
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    if (JFXUtil.isObjectEqualTo(lsID, "tfDescription", "tfReplaceId", "tfReplaceDescription", "tfCost", "tfQuantity", "tfDiscRateDetail", "tfUnitPrice", "tfAddlDiscAmtDetail")) {
                        moveNext(true, true);
                        event.consume();
                    }
                    break;
                case DOWN:
                    if (JFXUtil.isObjectEqualTo(lsID, "tfDescription", "tfReplaceId", "tfReplaceDescription", "tfCost", "tfQuantity", "tfDiscRateDetail", "tfUnitPrice", "tfAddlDiscAmtDetail")) {
                        moveNext(false, true);
                        event.consume();
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchBranch":
                            poJSON = poController.POQuotation().SearchBranch(lsValue, false, true);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            }
                            loadRecordSearch();
                            retrievePOQuotation();
                            return;
                        case "tfSearchDepartment":
                            poJSON = poController.POQuotation().SearchDepartment(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            }
                            loadRecordSearch();
                            retrievePOQuotation();
                            return;
                        case "tfSearchSupplier":
                            poJSON = poController.POQuotation().SearchSupplier(lsValue, false, true);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            }
                            loadRecordSearch();
                            retrievePOQuotation();
                            return;
                        case "tfSearchCategory":
                            poJSON = poController.POQuotation().SearchCategory(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            }
                            loadRecordSearch();
                            retrievePOQuotation();
                            return;
                        case "tfSearchReferenceNo":
                            retrievePOQuotation();
                            return;
                        case "tfTerm":
                            poJSON = poController.POQuotation().SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfDiscRate);
                            }
                            loadRecordMaster();
                            return;

                        case "tfReplaceId":
                            poJSON = poController.POQuotation().SearchInventory(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                txtField.setText("");
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (poJSON.get("row") == null) {
                                    return;
                                }
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                JFXUtil.runWithDelay(0.70, () -> {
                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 8);
                                    pnDetail = lnTempRow;
                                    loadTableDetail.reload();
                                });
                                break;
                            } else {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.80, () -> {
//                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 7);
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                loadTableDetail.reload();
                                if (!JFXUtil.isObjectEqualTo(poController.POQuotation().Detail(pnDetail).getDescription(), null, "")) {
                                    JFXUtil.textFieldMoveNext(tfUnitPrice);
                                }
                            }
                            return;
                        case "tfReplaceDescription":
                            poJSON = poController.POQuotation().SearchInventory(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                txtField.setText("");
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (poJSON.get("row") == null) {
                                    return;
                                }
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                JFXUtil.runWithDelay(0.70, () -> {
                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 8);
                                    pnDetail = lnTempRow;
                                    loadTableDetail.reload();
                                });
                                break;
                            } else {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.80, () -> {
//                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 8);
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                loadTableDetail.reload();
                                if (!JFXUtil.isObjectEqualTo(poController.POQuotation().Detail(pnDetail).getDescription(), null, "")) {
                                    JFXUtil.textFieldMoveNext(tfUnitPrice);
                                }
                            }
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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

                if (JFXUtil.isObjectEqualTo(inputText, null, "", "01/01/1900")) {
                    switch (datePicker.getId()) {
                        case "dpValidityDate":
                            poJSON = poController.POQuotation().Master().setValidityDate(null);
                            break;
                    }
                    return;
                }
                if (!datePicker.isShowing() && !datePicker.getEditor().isFocused()) {
                    return;
                }
                String lsServerDate = sdfFormat.format(oApp.getServerDate());
                String lsTransDate = sdfFormat.format(poController.POQuotation().Master().getTransactionDate());
                String lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                LocalDate currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                LocalDate selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                LocalDate transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        if (poController.POQuotation().getEditMode() == EditMode.ADDNEW
                                || poController.POQuotation().getEditMode() == EditMode.UPDATE) {
                            if (JFXUtil.isObjectEqualTo(poController.POQuotation().Master().getSourceNo(), null, "")) {
                                if (pbSuccess) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "Source No cannot be empty");
                                }
                                pbSuccess = false;
                                loadRecordMaster();
                                pbSuccess = true;
                                return;
                            }
                            String lsPOQuotationRequestTransDate = sdfFormat.format(poController.POQuotation().Master().POQuotationRequest().getTransactionDate());
                            LocalDate POQuotationRequestTransactionDate = LocalDate.parse(lsPOQuotationRequestTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isBefore(POQuotationRequestTransactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Transaction date cannot be before the Quotation Request Date");
                                pbSuccess = false;
                            } else if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && ((poController.POQuotation().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
                                    || !lsServerDate.equals(lsSelectedDate))) {
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
                                            }
                                        }
                                    } else {
                                        pbSuccess = false;
                                    }
                                }
                            }

                            if (pbSuccess) {
                                poController.POQuotation().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpReferenceDate":
                        if (poController.POQuotation().getEditMode() == EditMode.ADDNEW
                                || poController.POQuotation().getEditMode() == EditMode.UPDATE) {
                            if (selectedDate.isBefore(transactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Reference Date cannot be before the transaction date.");
                                pbSuccess = false;
                            } else if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            } else {
                                poController.POQuotation().Master().setReferenceDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                            if (pbSuccess) {
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
                    case "dpValidityDate":
                        if (poController.POQuotation().getEditMode() == EditMode.ADDNEW
                                || poController.POQuotation().getEditMode() == EditMode.UPDATE) {
                            if (JFXUtil.isObjectEqualTo(poController.POQuotation().Master().getReferenceDate(), null, "")) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Reference Date is empty");
                                loadRecordMaster();
                                return;
                            }
                            String lsReferenceNoDate = sdfFormat.format(poController.POQuotation().Master().getReferenceDate());
                            LocalDate ReferenceDate = LocalDate.parse(lsReferenceNoDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isBefore(ReferenceDate)) {
                                JFXUtil.setJSONError(poJSON, "Validity date cannot be before the Reference Date");
                                pbSuccess = false;
                            } else {
                                poController.POQuotation().Master().setValidityDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                            if (pbSuccess) {
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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordSearch() {
        try {
            if (poController.POQuotation().Master().Industry().getDescription() != null && !"".equals(poController.POQuotation().Master().Industry().getDescription())) {
                lblSource.setText(poController.POQuotation().Master().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }
            tfSearchBranch.setText(poController.POQuotation().getSearchBranch());
            tfSearchDepartment.setText(poController.POQuotation().getSearchDepartment());
            tfSearchSupplier.setText(poController.POQuotation().getSearchSupplier());
            tfSearchCategory.setText(poController.POQuotation().getSearchCategory());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
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
                            if (poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                                filePath2 = poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
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
        } catch (Exception e) {
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.POQuotation().getDetailCount() - 1) {
                return;
            }
            if (!poController.POQuotation().Detail(pnDetail).isReverse()) {
                JFXUtil.setDisabled(true, taDescription, tfReplaceId, tfReplaceDescription, tfUnitPrice, tfQuantity, tfDiscRateDetail, tfAddlDiscAmtDetail);
            } else {
                boolean lbIsUpdate = poController.POQuotation().Detail(pnDetail).getEditMode() == EditMode.UPDATE;
                boolean lbReqItem = poController.POQuotation().Detail(pnDetail).getDescription() != null && !"".equals(poController.POQuotation().Detail(pnDetail).getDescription());
                boolean lbRepItem = poController.POQuotation().Detail(pnDetail).getReplaceDescription() != null && !"".equals(poController.POQuotation().Detail(pnDetail).getReplaceDescription());
                boolean lbIsReqMoreThanOne = poController.POQuotation().RequestMultipleItem(poController.POQuotation().Detail(pnDetail).getDescription());
//                boolean lbShow = (lbIsUpdate && (lbReqItem && lbRepItem));
//                JFXUtil.setDisabled(lbShow, tfReplaceId, tfReplaceDescription);

                if (null != poController.POQuotation().Master().getSourceNo() && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                    // if(lbIsUpdate){
//                    JFXUtil.setDisabled(true, tfDescription);
//                } else {
                    if (lbRepItem) {
                        JFXUtil.setDisabled(true, taDescription);
                    } else {
                        if ((!lbIsReqMoreThanOne && lbReqItem)) {
                            JFXUtil.setDisabled(true, taDescription);
                        } else {
                            JFXUtil.setDisabled(false, taDescription);
                        }
                    }
//                }
                    Platform.runLater(() -> {
                        JFXUtil.setDisabled(false, tfReplaceId, tfReplaceDescription, tfUnitPrice, tfQuantity, tfDiscRateDetail, tfAddlDiscAmtDetail);
                    });
                } else {
                    Platform.runLater(() -> {
                        JFXUtil.setDisabled(false, taDescription, tfUnitPrice, tfQuantity, tfDiscRateDetail, tfAddlDiscAmtDetail);
                        JFXUtil.setDisabled(true, tfReplaceId, tfReplaceDescription);
                        JFXUtil.setDisabled(lbIsUpdate, taDescription);
                    });
                }
            }
            tfBrand.setText(poController.POQuotation().Detail(pnDetail).Inventory().Brand().getDescription());
            tfModel.setText(poController.POQuotation().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poController.POQuotation().Detail(pnDetail).Inventory().Color().getDescription());
            tfMeasure.setText(poController.POQuotation().Detail(pnDetail).Inventory().Measure().getDescription());

            taDescription.setText(poController.POQuotation().Detail(pnDetail).getDescription());
            tfReplaceId.setText(poController.POQuotation().Detail(pnDetail).ReplacedInventory().getBarCode());
            tfReplaceDescription.setText(poController.POQuotation().Detail(pnDetail).getReplaceDescription());
            tfUnitPrice.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(pnDetail).getUnitPrice(), true));

            tfQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(pnDetail).getQuantity(), false));
            tfDiscRateDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(pnDetail).getDiscountRate(), false));
            tfAddlDiscAmtDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(pnDetail).getDiscountAmount(), true));
            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().getCost(pnDetail), true));
            cbReverse.setSelected(poController.POQuotation().Detail(pnDetail).isReverse());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordMaster() {
        try {
            boolean lbShow = (JFXUtil.isObjectEqualTo(pnEditMode, EditMode.UPDATE, EditMode.ADDNEW));
            JFXUtil.setDisabled(!lbShow, dpTransactionDate);

            JFXUtil.setStatusValue(lblStatus, POQuotationStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.POQuotation().Master().getTransactionStatus());

            poController.POQuotation().computeFields();

            tfTransactionNo.setText(poController.POQuotation().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.POQuotation().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfReferenceNo.setText(poController.POQuotation().Master().getReferenceNo());
            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poController.POQuotation().Master().getReferenceDate());
            dpReferenceDate.setValue(JFXUtil.isObjectEqualTo(lsReferenceDate, "1900-01-01") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));

            String lsValidityDate = CustomCommonUtil.formatDateToShortString(poController.POQuotation().Master().getValidityDate());
            dpValidityDate.setValue(JFXUtil.isObjectEqualTo(lsValidityDate, "1900-01-01") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidityDate, "yyyy-MM-dd"));

            tfBranch.setText(poController.POQuotation().Master().Branch().getBranchName());
            tfCompany.setText(poController.POQuotation().Master().Company().getCompanyName());
            tfSupplier.setText(poController.POQuotation().Master().Supplier().getCompanyName());
            tfAddress.setText(poController.POQuotation().Master().Address().getAddress());
            tfContact.setText(poController.POQuotation().Master().Contact().getMobileNo());
            tfSourceNo.setText(poController.POQuotation().Master().getSourceNo());
            tfTerm.setText(poController.POQuotation().Master().Term().getDescription());
            tfDepartment.setText(poController.POQuotation().Master().POQuotationRequest().Department().getDescription());
            tfCategory.setText(poController.POQuotation().Master().POQuotationRequest().Category2().getDescription());

            tfGrossAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getGrossAmount(), true));
            tfDiscRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getDiscountRate(), false));
            tfAddlDiscAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getAdditionalDiscountAmount(), true));
            tfFreight.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getFreightAmount(), true));

            cbVatable.setSelected(poController.POQuotation().Master().isVatable());
            tfVATAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getVatAmount(), true));
            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getTransactionTotal(), true));
            taRemarks.setText(poController.POQuotation().Master().getRemarks());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            ModelPOQuotation_Main selected = (ModelPOQuotation_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (pnEditMode == EditMode.UPDATE) {
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to open another transaction?\nTransaction changes will be disregard.") == false) {
                        return;
                    }
                }

                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                poJSON = poController.POQuotation().OpenTransaction(poController.POQuotation().POQuotationList(pnMain).getTransactionNo());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
            }

            poController.POQuotation().loadAttachments();
            if (poController.POQuotation().getTransactionAttachmentCount() > 1) {
                try {
                    if (!openedAttachment.equals(poController.POQuotation().POQuotationList(pnMain).getTransactionNo())) {
                        openedAttachment = "";
                        imageinfo_temp.clear();
                        stageAttachment.closeDialog();
                    }
                } catch (Exception e) {
                    openedAttachment = "";
                    imageinfo_temp.clear();
                    stageAttachment.closeDialog();
                }
            } else {
                openedAttachment = "";
                imageinfo_temp.clear();
                stageAttachment.closeDialog();
            }
            poController.POQuotation().loadAttachments();

            Platform.runLater(() -> {
                loadTableDetail.reload();
            });
            tfAttachmentNo.clear();
            cmbAttachmentType.setItems(documentType);

            imageView.setImage(null);
            JFXUtil.stackPaneClip(stackPane1);
            Platform.runLater(() -> {
                loadTableAttachment.reload();
            });
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private String safeGet(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public void initLoadTable() {
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
                            for (lnCtr = 0; lnCtr < poController.POQuotation().getTransactionAttachmentCount(); lnCtr++) {
                                if (RecordStatus.INACTIVE.equals(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    continue;
                                }
                                lnCount += 1;
                                attachment_data.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                                String.valueOf(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getFileName()),
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

        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        plOrderNoPartial.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.POQuotation().ReloadDetail();
                            }
                            JFXUtil.disableAllHighlight(tblViewTransDetails, highlightedRowsDetail);
                            int lnRowCount = 0;
                            for (lnCtr = 0; lnCtr < poController.POQuotation().getDetailCount(); lnCtr++) {
                                if (!poController.POQuotation().Detail(lnCtr).isReverse()) {
                                    JFXUtil.highlightByKey(tblViewTransDetails, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsDetail);
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelPOQuotation_Detail(
                                                String.valueOf(lnRowCount),
                                                String.valueOf(poController.POQuotation().Detail(lnCtr).getDescription()),
                                                String.valueOf(poController.POQuotation().Detail(lnCtr).getReplaceDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(lnCtr).getUnitPrice(), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().getDiscount(lnCtr), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(lnCtr).getQuantity(), false)),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(String.valueOf(poController.POQuotation().getCost(lnCtr)), true), String.valueOf(lnCtr)
                                        ));
//                                }
                            }
                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 8); //this method is only used when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewTransDetails, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex08());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewTransDetails, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewTransDetails.getSelectionModel().getSelectedIndex()).getIndex08());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        main_data.clear();
                        JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                        if (poController.POQuotation().getPOQuotationCount() > 0) {
                            //pending
                            //retreiving using column index
                            for (int lnCtr = 0; lnCtr <= poController.POQuotation().getPOQuotationCount() - 1; lnCtr++) {
                                try {
                                    main_data.add(new ModelPOQuotation_Main(String.valueOf(lnCtr + 1),
                                            //                                            String.valueOf(poController.POQuotation().POQuotationList(lnCtr).Company().getCompanyName()),
                                            String.valueOf(poController.POQuotation().POQuotationList(lnCtr).Branch().getBranchName()),
                                            String.valueOf(poController.POQuotation().POQuotationList(lnCtr).Supplier().getCompanyName()),
                                            String.valueOf(CustomCommonUtil.formatDateToShortString(poController.POQuotation().POQuotationList(lnCtr).getTransactionDate())),
                                            //                                            safeGet(poController.POQuotation().POQuotationList(lnCtr).POQuotationRequest().getTransactionNo()),
                                            safeGet(poController.POQuotation().POQuotationList(lnCtr).getReferenceNo()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().POQuotationList(lnCtr).getTransactionTotal(), true))
                                    ));
                                } catch (GuanzonException | SQLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                }
                                if (poController.POQuotation().POQuotationList(lnCtr).getTransactionStatus().equals(POQuotationStatus.CONFIRMED)) {
                                    JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCtr + 1), "#C1E1C1", highlightedRowsMain);
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
                        JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                    });
                });
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate, dpValidityDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate, dpValidityDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtField_Focus, tfSearchBranch, tfSearchSupplier, tfSearchDepartment, tfSearchCategory, tfSearchReferenceNo);
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks, taDescription);
        JFXUtil.setFocusListener(txtMaster_Focus, tfTerm, tfDiscRate, tfAddlDiscAmt, tfFreight, tfReferenceNo);

        JFXUtil.setFocusListener(txtDetail_Focus, tfReplaceId, tfReplaceDescription, tfUnitPrice, tfQuantity,
                tfDiscRateDetail, tfAddlDiscAmtDetail);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail, apBrowse);
        taDescription.setOnKeyPressed(this::txtArea_KeyPressed);

        JFXUtil.setCommaFormatter(tfGrossAmount, tfAddlDiscAmt, tfFreight, tfVATAmount, tfTransactionTotal,
                tfUnitPrice, tfQuantity, tfAddlDiscAmtDetail);

        CustomCommonUtil.inputDecimalOnly(tfDiscRate, tfDiscRateDetail);

        // Combobox
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (Exception e) {
                }
            }
        });
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAttachmentType);
        JFXUtil.setCheckboxHoverCursor(apMaster, apDetail);
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

        tblViewTransDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    int lnRow = Integer.parseInt(details_data.get(tblViewTransDetails.getSelectionModel().getSelectedIndex()).getIndex08());
                    pnDetail = lnRow;
                    moveNext(false, false);
                }
            }
        });

        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = poController.POQuotation().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelPOQuotation_Main) item).getIndex01(), highlightedRowsMain);
        JFXUtil.applyRowHighlighting(tblViewTransDetails, item -> ((ModelPOQuotation_Detail) item).getIndex01(), highlightedRowsDetail);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblViewTransDetails, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList, tblViewTransDetails, tblAttachments);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Update 
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);

        //Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnUpdate, btnHistory, btnVoid, btnConfirm);

        //Unkown || Ready
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail, apAttachments, apAttachmentButtons);
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);

        JFXUtil.setButtonsVisibility(false, btnReturn);
        try {
            if (JFXUtil.isObjectEqualTo(poController.POQuotationRequest().Master().getTransactionStatus(), POQuotationStatus.OPEN,
                    POQuotationStatus.CONFIRMED)) {
//                JFXUtil.setButtonsVisibility(true, btnReturn);
            } else {
                JFXUtil.setButtonsVisibility(false, btnReturn);
            }
        } catch (Exception e) {
        }
        switch (poController.POQuotation().Master().getTransactionStatus()) {
            case POQuotationStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnConfirm);
                break;
            case POQuotationStatus.APPROVED:
            case POQuotationStatus.VOID:
            case POQuotationStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnVoid);
                break;
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

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblReplacementDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblUnitPriceDetail, tblQuantityDetail, tblDiscountDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);

        tblViewTransDetails.setItems(details_data);
        tblViewTransDetails.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblBranch, tblSupplier);
        JFXUtil.setColumnRight(tblTransactionTotal);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
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
                case "tblViewTransDetails":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex08())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex08());
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

    public void clearTextFields() {
        Platform.runLater(() -> {
            stageAttachment.closeDialog();
            imageinfo_temp.clear();
            JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
            JFXUtil.clearTextFields(apBrowse, apMaster, apDetail, apAttachments);
        });
    }
}
