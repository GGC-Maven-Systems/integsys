package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelCashLiquidation_Detail;
import ph.com.guanzongroup.integsys.model.ModelCashLiquidation_Main;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.guanzon.appdriver.constant.DocumentType;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.constant.RecordStatus;
import ph.com.guanzongroup.cas.cashflow.CashLiquidation;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class CashLiquidation_ApprovalController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static CashLiquidation poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    boolean pbEnteredDetail = false;
    private boolean pbEntered = false;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    private ObservableList<ModelCashLiquidation_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelCashLiquidation_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    private FilteredList<ModelCashLiquidation_Main> filteredData;
    private FilteredList<ModelCashLiquidation_Detail> filteredDataDetail;
    Map<String, String> imageinfo_temp = new HashMap<>();
    boolean tooltipShown = false;
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain, loadTableAttachment;

    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster, apDetail, apMainAnchor, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSearchIndustry, tfSearchPayee, tfSearchTransNo, tfTransactionNo, tfPayee, tfDepartment, tfCashAdvanceBalance, tfAdvancesAmount, tfLiquidationTotal, tfReceiptNo, tfAccountDescription, tfParticular, tfTransAmount, tfAttachmentNo;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnApprove, btnHistory, btnRetrieve, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane ImTabPane;
    @FXML
    private Tab tabDetails, tabAttachments;
    @FXML
    private DatePicker dpTransactionDate, dpLiquidationDate, dpTransDateDetail;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblViewDetail, tblViewMainList, tblAttachments;
    @FXML
    private TableColumn tblRowNoDetail, tblORNo, tblTransDateDetail, tblParticular, tblTransAmount, tblRowNo, tblReqDepartment1, tblTransDate, tblPayeeName, tblReqDepartment, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private Pagination pgPagination;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            poController = new CashflowControllers(oApp, null).CashLiquidation();
            poController.InitTransaction();
            initTextFields();
            initDatePickers();
            initMainGrid();
            initDetailsGrid();
            initAttachmentsGrid();
            initTableOnClick();
            clearTextFields();
            initLoadTable();
            initComboboxes();
            Platform.runLater(() -> {
                poController.setTransactionStatus("2");

                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
//              poController.Master().setCategoryCode(psCategoryId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
//              poController.setCategoryId(psCategoryId);
                poController.initFields();
                poController.setWithUI(true);
                loadRecordSearch();
            });

            initAttachmentPreviewPane();

            pgPagination.setPageCount(1);

            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CashLiquidation_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
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
//        psCategoryId = fsValue;
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
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());

                        //Recheck transaction status
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
                        poController.loadAttachments();
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                        break;
                    case "btnApprove":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to approve transaction?") == true) {
                            poJSON = poController.ConfirmTransaction("");
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
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            //Clear data
                            clearTextFields();
                            poController.resetTransaction();
                            poController.initFields();
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
                        retrieveCashAdvance();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.AddDetail();
                                loadRecordMaster();
                                loadTableDetail.reload();
                                loadTableAttachment.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                                poController.loadAttachments();
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.Master().getTransactionStatus().equals(CashAdvanceStatus.APPROVED)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to approve this transaction?")) {
                                            loJSON = poController.ConfirmTransaction("Confirmed");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                break;
                            }
                        } else {
                            return;
                        }
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to void transaction?") == true) {
                            if (CashAdvanceStatus.CONFIRMED.equals(poController.Master().getTransactionStatus())) {
//                                poJSON = poController.CancelTransaction();
                            } else {
//                                poJSON = poController.VoidTransaction();
                            }
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
                                }
                            }

//                            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data, poController.addAttachment(imgPath2), 3);
//                            pnAttachment = lnTempRow;
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

                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnApprove", "btnVoid")) {
                    clearTextFields();
                    poController.resetTransaction();
//                    poController.resetMaster();
//                    poController.resetOthers();
//                    poController.Detail().clear();
                    imageView.setImage(null);
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnArrowRight", "btnArrowLeft", "btnRetrieve", "btnAddAttachment", "btnRemoveAttachment", "btnSearch")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                    poController.loadAttachments();
                    loadTableAttachment.reload();
                }
                initButton(pnEditMode);
                if (lsButton.equals("btnUpdate")) {
                    JFXUtil.runWithDelay(0.5, () -> {
                        if (poController.getDetailCount() <= 0) {
                            return;
                        }
                        moveNext(false, false);
                    });

                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | IOException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void retrieveCashAdvance() {
        try {
            poJSON = new JSONObject();
            poJSON = poController.loadTransactionList(tfSearchIndustry.getText(), tfSearchPayee.getText(), tfSearchTransNo.getText(), false);
            if (!"success".equals((String) poJSON.get("result"))) {
                //show this only if the focus is on btnRetrieve or tfSearchIndustry or tfSearchPayee or tfTransNo
                if (btnRetrieve.isFocused() || tfSearchIndustry.isFocused() || tfSearchIndustry.isFocused() || tfSearchPayee.isFocused() || tfSearchTransNo.isFocused()) {
                    if (tfSearchIndustry.getText().isEmpty()) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                }
            }
            loadTableMain.reload();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
        }
    }

    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchPayee":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchTransNo":
                        if (lsValue.isEmpty()) {
//                            poController.setSearchIndustry;
                        }
                        break;
                }
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfLiquidationTotal":
//                        poJSON = poController.Master().setLiquidationTotal(lsID);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
//                loadRecordMaster();
            });

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfReceiptNo":
                            poJSON = poController.setDetail(pnDetail, poController.Detail(pnDetail).getParticular(), lsValue, poController.Detail(pnDetail).getAccountCode());
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.70, () -> {
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                pnDetail = Integer.parseInt(String.valueOf(poJSON.get("row")));
                            }
                            loadRecordDetail();
                            break;
                        case "tfAccountDescription":
                            if (lsValue.isEmpty()) {
                                poController.Detail(pnDetail).setAccountCode("");
                            }
                            break;
                        case "tfParticular":
                            poJSON = poController.setDetail(pnDetail, lsValue, poController.Detail(pnDetail).getORNo(), poController.Detail(pnDetail).getAccountCode());
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.70, () -> {
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                pnDetail = Integer.parseInt(String.valueOf(poJSON.get("row")));
                            }
                            loadRecordDetail();
                            break;
                        case "tfTransAmount":
                            lsValue = JFXUtil.removeComma(lsValue);
                            poJSON = poController.Detail(pnDetail).setTransactionAmount(Double.parseDouble(lsValue));
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            JFXUtil.runWithDelay(0.90, () -> {
                                if (pbEnteredDetail) {
                                    moveNext(false, true);
                                    pbEnteredDetail = false;
                                }
                            });
                            break;
                    }
                    JFXUtil.runWithDelay(0.80, () -> {
                        loadTableDetail.reload();
                    });
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                }
            });

    public void moveNext(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDetail.requestFocus();
            boolean lbContinue = true;
            while (lbContinue) {
                pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex06())
                        : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex06());
                if (poController.Detail(pnDetail).isReverse()) {
                    lbContinue = false;
                }
            }
        }
        loadRecordDetail();
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.Detail(pnDetail).getORNo(), tfReceiptNo},
            {poController.Detail(pnDetail).getAccountCode(), tfAccountDescription}, // if null or empty, then requesting focus to the txtfield
            {poController.Detail(pnDetail).getAccountCode(), tfParticular},
            {poController.Detail(pnDetail).getParticular(), tfTransAmount},}, tfTransAmount); // default
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
                    if (tfTransAmount.isFocused()) {
                        pbEnteredDetail = true;
                    }
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    if (JFXUtil.isObjectEqualTo(lsID, "tfReceiptNo", "tfAccountDescription", "tfParticular", "tfTransAmount")) {
                        moveNext(true, true);
                        event.consume();
                    }
                    break;
                case DOWN:
                    if (JFXUtil.isObjectEqualTo(lsID, "tfReceiptNo", "tfAccountDescription", "tfParticular", "tfTransAmount")) {
                        moveNext(false, true);
                        event.consume();
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchIndustry":
                            poJSON = poController.SearchIndustry(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            }
                            loadRecordSearch();
                            retrieveCashAdvance();
                            break;
                        case "tfSearchPayee":
                            poJSON = poController.SearchPayee(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            }
                            loadRecordSearch();
                            retrieveCashAdvance();
                            break;
                        case "tfSearchTransNo":
                            retrieveCashAdvance();
                            if (!tooltipShown) {
                                JFXUtil.showTooltip("NOTE: Results appear directly in the table view, no pop-up dialog.", txtField);
                                tooltipShown = true;
                            }
                            break;
                        case "tfAccountDescription":
                            poJSON = poController.SearchAccount(lsValue, false, pnDetail);
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
                                JFXUtil.textFieldMoveNext(tfParticular);
                            }
                            loadRecordDetail();
                            break;
                    }
                    break;
                default:
                    break;
            }

        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
//                try {
                switch (datePicker.getId()) {
                    case "dpTransDateDetail":
                        // Date should be >= Released 
                        if (pnEditMode != EditMode.UPDATE) {
                            return;
                        }
                        String lsReleasedDate = sdfFormat.format(poController.Master().getIssuedDate());
                        LocalDate ldReleasedDate = LocalDate.parse(lsReleasedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                        if (ldSelectedDate.isBefore(ldReleasedDate)) {
                            JFXUtil.setJSONError(poJSON, "Date should not be before the released/issued date.");
                            pbSuccess = false;
                        }
                        if (ldSelectedDate.isAfter(ldCurrentDate)) {
                            JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                            pbSuccess = false;
                        }
                        if (pbSuccess) {
                            poController.Detail(pnDetail).setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                        } else {
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }

                        pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                        loadTableDetail.reload();
                        pbSuccess = true; //Set to original value
                        break;
                    default:
                        break;
                }
//                } catch (SQLException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                }
            });

    public void loadRecordSearch() {
        try {
            if (poController.Master().Company().getCompanyName() != null && !"".equals(poController.Master().Company().getCompanyName())) {
                lblSource.setText(poController.Master().Company().getCompanyName());
            } else {
                lblSource.setText("");
            }
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchPayee.setText(poController.getSearchPayee());
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
        } catch (Exception e) {
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            tfReceiptNo.setText(poController.Detail(pnDetail).getORNo());
            dpTransDateDetail.setValue(poController.Detail(pnDetail).getTransactionDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Detail(pnDetail).getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);

            tfAccountDescription.setText(poController.Detail(pnDetail).Account().getDescription());
            tfParticular.setText(poController.Detail(pnDetail).getParticular());
            tfTransAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getTransactionAmount().doubleValue(), false));

            cbReverse.setSelected(poController.Detail(pnDetail).isReverse());

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordMaster() {
        try {
            Platform.runLater(() -> {
                lblStatus.setText(pnEditMode == EditMode.UNKNOWN ? "UNKNOWN" : poController.getStatus(poController.Master().getTransactionStatus()).toUpperCase());
            });
            poController.computeFields(true);

            tfTransactionNo.setText(poController.Master().getTransactionNo());

            dpTransactionDate.setValue(poController.Master().getTransactionDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);

            tfPayee.setText(poController.Master().Payee().getCompanyName());
            tfDepartment.setText(poController.Master().Department().getDescription());
            taRemarks.setText(poController.Master().getRemarks());

            dpLiquidationDate.setValue(poController.Master().getLiquidatedDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getLiquidatedDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);

            tfCashAdvanceBalance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getCashAdvanceBalance(), true));
            tfAdvancesAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getAdvanceAmount(), false));
            tfLiquidationTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getLiquidationTotal().doubleValue(), false));

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            ModelCashLiquidation_Main selected = (ModelCashLiquidation_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                if (null != poController.Master().getTransactionNo()) {
                    if (!JFXUtil.loadValidation(pnEditMode, pxeModuleName, poController.Master().getTransactionNo(), selected.getIndex02())) {
                        return;
                    }
                }
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                poController.resetTransaction();
                poController.loadAttachments();
                clearTextFields();
                poJSON = poController.OpenTransaction(poController.CashAdvanceList(pnMain).getTransactionNo());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                pnEditMode = poController.getEditMode();
                loadRecordMaster();
            }
            imageinfo_temp.clear();

            poController.loadAttachments();
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
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
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

        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        plOrderNoPartial.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            JFXUtil.disableAllHighlight(tblViewDetail, highlightedRowsDetail);
                            int lnRowCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (!poController.Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                String date = "";
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                if (!JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getTransactionDate(), null, "")) {
                                    date = sdf.format(poController.Detail(lnCtr).getTransactionDate());
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelCashLiquidation_Detail(
                                                String.valueOf(lnRowCount),
                                                String.valueOf(poController.Detail(lnCtr).getORNo()),
                                                String.valueOf(date),
                                                String.valueOf(poController.Detail(lnCtr).getParticular()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getTransactionAmount(), true)),
                                                String.valueOf(lnCtr)
                                        ));
//                                }
                            }

                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 6); //this method is only used when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetail, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex06());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetail, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex06());
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
                        if (poController.getCashAdvanceCount() > 0) {
                            //pending
                            //retreiving using column index
                            for (int lnCtr = 0; lnCtr <= poController.getCashAdvanceCount() - 1; lnCtr++) {
                                try {

                                    main_data.add(new ModelCashLiquidation_Main(String.valueOf(lnCtr + 1),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).getTransactionNo()),
                                            String.valueOf(CustomCommonUtil.formatDateToShortString(poController.CashAdvanceList(lnCtr).getTransactionDate())),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).Payee().getCompanyName()),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).Department().getDescription()),
                                            ""
                                    ));
                                    if (JFXUtil.isObjectEqualTo(poController.CashAdvanceList(lnCtr).getTransactionStatus(), CashAdvanceStatus.LIQUIDATED)) {
                                        JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCtr + 1), "#C1E1C1", highlightedRowsMain);
                                    }
                                } catch (GuanzonException | SQLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpLiquidationDate, dpTransDateDetail);
        JFXUtil.setActionListener(datepicker_Action, dpTransactionDate, dpLiquidationDate, dpTransDateDetail);
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

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtBrowse_Focus, apBrowse);
        JFXUtil.setFocusListener(txtMaster_Focus, apMaster);
        JFXUtil.setFocusListener(txtDetail_Focus, apDetail);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setCommaFormatter(tfTransAmount);

        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail, tblViewMainList, tblAttachments);

        JFXUtil.adjustColumnForScrollbar(tblViewDetail, tblViewMainList, tblAttachments);

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
            if (!details_data.isEmpty() && event.getClickCount() == 1) {
                ModelCashLiquidation_Detail selected = (ModelCashLiquidation_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex06());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });

        tblViewMainList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                if (pnMain >= 0) {
                    loadTableDetailFromMain();
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelCashLiquidation_Main) item).getIndex01(), highlightedRowsMain);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList, tblViewDetail, tblAttachments);
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
        JFXUtil.setColumnCenter(tblRowNoDetail, tblORNo, tblTransDateDetail);
        JFXUtil.setColumnLeft(tblParticular);
        JFXUtil.setColumnRight(tblTransAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        tblViewDetail.setItems(filteredDataDetail);
        tblViewDetail.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblTransDate);
        JFXUtil.setColumnLeft(tblReqDepartment1, tblPayeeName, tblReqDepartment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    if (!details_data.isEmpty()) {
                        newIndex = isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex06())
                                : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex06());
                        pnDetail = newIndex;
                        loadRecordDetail();
                    }
                    break;
                case "tblAttachments":
                    if (!attachment_data.isEmpty()) {
                        newIndex = isMovedDown ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03())
                                : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                        pnAttachment = newIndex;
                        loadRecordAttachment(true);
                    }
                    break;
            }
        }
    };

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnApprove, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow, taRemarks, apMaster, apDetail, apAttachments, apAttachmentButtons);
        if (pnEditMode != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case CashAdvanceStatus.LIQUIDATED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove);
            case CashAdvanceStatus.VOID:
            case CashAdvanceStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove);
                break;
        }
    }

    public void clearTextFields() {
        Platform.runLater(() -> {
            imageinfo_temp.clear();
            JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
            JFXUtil.clearTextFields(apMaster, apDetail, apAttachments);
        });
    }
}
