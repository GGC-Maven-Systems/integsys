/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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
import org.guanzon.cas.inv.InvTransCons;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStaticData;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.integsys.model.ModelPRFAttachment;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 2 & Team 1
 */
public class PaymentRequest_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private CashflowControllers poGLControllers;
    private String psFormName = "Payment Request Confirmation";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";

    private String psTransactionNo = "";
    private String psPayeeID = "";

    private int pnTblMainRow = -1;
    private int pnTblDetailRow = -1;
    private int pnTblMain_Page = 50;
    private TextField activeField;

    //attachments
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double scaleFactor = 1.0;
    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;
    double ldstackPaneWidth = 0;
    double ldstackPaneHeight = 0;
    private FilteredList<ModelTableMain> filteredData;
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private ObservableList<ModelPRFAttachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelPRFAttachment.documentType;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster, apDetail, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSourceTranTotal, tfAdvances, tfSearchTransaction, tfSearchPayee, tfTransactionNo, tfBranch, tfDepartment, tfPayee, tfSeriesNo, tfTotalAmount, tfDiscountAmount, tfNetAmount, tfSourceNo, tfRecurringNo, tfBranchDetail, tfAccountNo, tfEmployee, tfParticular, tfAmount, tfDiscRate, tfDiscAmountDetail, tfAmountDetail, tfAttachmentNo;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnReturn, btnVoid, btnRetrieve, btnHistory, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane ImTabPane;
    @FXML
    private Tab tabDetails, tabAttachments;
    @FXML
    private DatePicker dpTransaction;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblVwPRDetail, tblAttachments, tblVwPaymentRequest;
    @FXML
    private TableColumn tblRowNoDetail, tblParticular, tblAmount, tblDiscAmount, tbTotalAmount, tblRowNoAttachment, tblFileNameAttachment, tblRowNo, tblBranch, tblPayee, tblTransactionNo, tblTransactionDate;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;
    @FXML
    private Pagination pagination;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poGLControllers = new CashflowControllers(poApp, logWrapper);
            poGLControllers.PaymentRequest().setTransactionStatus(PaymentRequestStatus.OPEN
                    + PaymentRequestStatus.CONFIRMED);
            poGLControllers.PaymentRequest().setWithUI(true);
            poJSON = poGLControllers.PaymentRequest().InitTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
            }
            JFXUtil.setKeyEventFilter(tableKeyEvents, tblVwPRDetail, tblAttachments);
            Platform.runLater((() -> {
                try {
                    poGLControllers.PaymentRequest().setIndustryId(psIndustryID);
                    poGLControllers.PaymentRequest().setCompanyId(psCompanyID);
                    poGLControllers.PaymentRequest().Master().setIndustryID(psIndustryID);
                    poGLControllers.PaymentRequest().Master().setCompanyID(psCompanyID);
                    loadRecordSearch();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }));
            Platform.runLater(() -> setBranchAndDepartment());
            initAll();
            JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField);
            pagination.setPageCount(1);
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poGLControllers.PaymentRequest().Master().Company().getCompanyName() + " - " + poGLControllers.PaymentRequest().Master().Industry().getDescription());
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setBranchAndDepartment() {
        try {
            poGLControllers.PaymentRequest().Master().setBranchCode(poApp.getBranchCode());
            poGLControllers.PaymentRequest().Master().setDepartmentID(poApp.getDepartment());
            tfBranch.setText(poGLControllers.PaymentRequest().Master().Branch().getBranchName());
            if (poApp.isMainOffice() || poApp.isWarehouse()) {
                String lsDepartment = "";
                if (poGLControllers.PaymentRequest().Master().Department().getDescription() != null) {
                    lsDepartment = poGLControllers.PaymentRequest().Master().Department().getDescription();
                }
                tfDepartment.setText(lsDepartment);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initTableOnClick() {
        tblVwPRDetail.setOnMouseClicked(this::tblVwDetail_Clicked);
        tblVwPaymentRequest.setOnMouseClicked(this::tblVwMain_Clicked);
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                scaleFactor = 1.0;
                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                pnAttachment = lnRow;
                loadRecordAttachment(true);
                JFXUtil.resetImageBounds(imageView, stackPane1);
            }
        });
    }

    private void initAll() {
        initTableOnClick();
        initButtonsClickActions();
        initTextFieldFocus();
        initTextAreaFocus();
        initTextFieldKeyPressed();
        initTextFieldsProperty();
        initCheckBoxActions();
        initDatePickerActions();
        initTextFieldPattern();
        initTablePaymentRequest();
        initTableDetail();
        initAttachmentsGrid();
        initAttachmentPreviewPane();
        initStackPaneListener();
        initButtons(pnEditMode);
        initFields(pnEditMode);
        initComboBoxes();
        loadTableAttachment();
    }

    private void loadRecordMaster() {
        try {
            if (PaymentRequestStatus.CONFIRMED.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus())) {
                btnVoid.setText("Cancel");
            } else {
                btnVoid.setText("Void");
            }
//            JFXUtil.setDisabled(true, tfBranch); //Commented by Arsiela 05-16-2026 05:08PM
            boolean lbShow = pnEditMode == EditMode.UPDATE;
            if (lbShow) {
                //Do not allow to update payee when there's a transaction source
                if (poGLControllers.PaymentRequest().Master().getSourceNo() != null && !"".equals(poGLControllers.PaymentRequest().Master().getSourceNo())) {
                    JFXUtil.setDisabled(true, tfBranch, tfPayee, tfDepartment);
                } else {
                    //Do not allow to update payee when there's a transaction source
                    if (poGLControllers.PaymentRequest().getDetailCount() > 0) {
                        if ((poGLControllers.PaymentRequest().Detail(0).getRecurringNo() != null && !"".equals(poGLControllers.PaymentRequest().Detail(0).getRecurringNo()))) {
                            JFXUtil.setDisabled(true, tfBranch, tfPayee, tfDepartment);
                        } else {
                            JFXUtil.setDisabled(!PaymentRequestStatus.OPEN.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus()), tfBranch, tfPayee, tfDepartment);
                            JFXUtil.setDisabled(!PaymentRequestStatus.OPEN.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus()) || !poApp.isMainOffice(), tfBranch);
                        }
                    }
                }

                JFXUtil.setDisabled(!PaymentRequestStatus.OPEN.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus()), taRemarks);
            }

            JFXUtil.setStatusValue(lblStatus, PaymentRequestStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poGLControllers.PaymentRequest().Master().getTransactionStatus());

            poGLControllers.PaymentRequest().computeFields();
            tfTransactionNo.setText(poGLControllers.PaymentRequest().Master().getTransactionNo());
            dpTransaction.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poGLControllers.PaymentRequest().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfBranch.setText(poGLControllers.PaymentRequest().Master().Branch().getBranchName());
            if (poApp.isMainOffice() || poApp.isWarehouse()) {
                String lsDepartment = "";
                if (poGLControllers.PaymentRequest().Master().Department().getDescription() != null) {
                    lsDepartment = poGLControllers.PaymentRequest().Master().Department().getDescription();
                }
                tfDepartment.setText(lsDepartment);
            }
            String lsPayee = "";
            if (poGLControllers.PaymentRequest().Master().Payee().getPayeeName() != null) {
                lsPayee = poGLControllers.PaymentRequest().Master().Payee().getPayeeName();
            }
            tfPayee.setText(lsPayee);
            tfSeriesNo.setText(poGLControllers.PaymentRequest().Master().getSeriesNo());
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTranTotal(), true));
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getDiscountAmount(), true));
//            tfTotalVATableAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getVatAmount(), true));
            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getNetTotal(), true));
            taRemarks.setText(poGLControllers.PaymentRequest().Master().getRemarks());
            tfAdvances.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().PurchaseOrder().getAmountPaid(), true));
            tfSourceNo.setText(poGLControllers.PaymentRequest().Master().getSourceNo());

            tfSourceTranTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().PurchaseOrder().getNetTotal(), true));
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordDetail() {
        if (pnTblDetailRow >= 0) {
            try {
                boolean lbShow = !PaymentRequestStatus.OPEN.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus());
                JFXUtil.setDisabled(lbShow, cbReverse);
                boolean lbIsRecurring = !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getRecurringNo(), null, "")
                        || (!JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Master().getSourceNo(), null, "")
                        && PaymentRequestStaticData.recurring_expense_payment.equals(poGLControllers.PaymentRequest().Master().getSourceCode()));

                boolean lbShow2 = JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID(), null, "");
                boolean lbisUpdate = poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getEditMode() == EditMode.UPDATE;

                if (lbisUpdate && !lbShow2) {
                    JFXUtil.setDisabled(true, tfParticular);
                } else {
                    JFXUtil.setDisabled(false, tfParticular);
                }

                tfParticular.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).Particular().getDescription());
                tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                        poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAmount(), true));
                tfDiscRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getDiscount())); // rate
                tfDiscAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAddDiscount(), true)); // amount
//                    chkbVatable.setSelected(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).isVatable());
                cbReverse.setSelected(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).isReverse());

                if (lbIsRecurring && (poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getRecurringNo() == null || "".equals(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getRecurringNo()))) {
                    tfRecurringNo.setText(poGLControllers.PaymentRequest().Master().RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getRecurringNo());
                    tfBranchDetail.setText(poGLControllers.PaymentRequest().Master().RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Branch().getBranchName());
                    tfAccountNo.setText(poGLControllers.PaymentRequest().Master().RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getAccountNo());
                    tfEmployee.setText(poGLControllers.PaymentRequest().Master().RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Employee().getCompanyName());
                } else {
                    tfRecurringNo.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getRecurringNo());
                    tfBranchDetail.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Branch().getBranchName());
                    tfAccountNo.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getAccountNo());
                    tfEmployee.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Employee().getCompanyName());
                }
//                    tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getVatAmount(), true));
                tfAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getNetTotal(), true));
//                tfTaxAmount.setText("0.00");
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnUpdate, btnSave, btnCancel, btnVoid, btnReturn,
                btnRetrieve, btnHistory, btnClose, btnConfirm,
                btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight, btnSearch);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    private void handleButtonAction(ActionEvent event) {
        poJSON = new JSONObject();
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poGLControllers.PaymentRequest().UpdateTransaction();
                    pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                    }
                    poGLControllers.PaymentRequest().loadAttachments();
                    clearDetailFields();
                    pnTblDetailRow = -1;
                    pagination.toFront();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(psFormName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                    break;
                case "btnSave":
                    handleLoading(lsButton);
                    return;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to cancel?")) {
                        clearMasterFields();
                        clearDetailFields();
                        detail_data.clear();
                        attachment_data.clear();
                        poJSON = poGLControllers.PaymentRequest().OpenTransaction(poGLControllers.PaymentRequest().Master().getTransactionNo());
                        if ("success".equals((String) poJSON.get("result"))) {
                            pagination.toBack();
                            CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                            pnTblDetailRow = -1;
                            loadRecordMaster();
                            clearDetailFields();
                            pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                            loadTableDetail();
                            pagination.toBack();
                        }
                        poGLControllers.PaymentRequest().loadAttachments();
                    }
                    break;
                case "btnConfirm":
                    poJSON = new JSONObject();
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to confirm transaction?")) {
                        poJSON = poGLControllers.PaymentRequest().ConfirmTransaction("Confirmed");
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        poGLControllers.PaymentRequest().resetMaster();
                        poGLControllers.PaymentRequest().resetOthers();
                        poGLControllers.PaymentRequest().Detail().clear();
                        clearMasterFields();
                        clearDetailFields();
                        CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                        detail_data.clear();
                        attachment_data.clear();
                        pnEditMode = EditMode.UNKNOWN;
                        pnTblDetailRow = -1;
                        //this code below use to highlight tblpurchase
                        tblVwPaymentRequest.refresh();
                        main_data.get(pnTblMainRow).setIndex06(PaymentRequestStatus.CONFIRMED);
                        pagination.toBack();
                    } else {
                        return;
                    }
                    break;
                case "btnRetrieve":
                    loadTableMain();
                    break;
                case "btnHistory":
                    poGLControllers.PaymentRequest().ShowStatusHistory();
                    return;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(AnchorMain, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
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
                        for (int lnCtr = 0; lnCtr <= poGLControllers.PaymentRequest().getTransactionAttachmentCount() - 1; lnCtr++) {
                            if (imgPath2.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getFileName())
                                    && RecordStatus.ACTIVE.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                ShowMessageFX.Warning(null, psFormName, "File name already exists.");
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
                                if (pageCount > 5) {
                                    ShowMessageFX.Warning(null, psFormName, "PDF exceeds maximum allowed pages.");
                                    return;
                                }
                            }
                        }

//                            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data,  poGLControllers.PaymentRequest().addAttachment(imgPath2), 3);
//                            pnAttachment = lnTempRow;
                        pnAttachment = poGLControllers.PaymentRequest().addAttachment(imgPath2);
                        //Copy file to Attachment path
                        poGLControllers.PaymentRequest().copyFile(selectedFile.toString());
                        loadTableAttachment();
                        tblAttachments.getFocusModel().focus(pnAttachment);
                        tblAttachments.getSelectionModel().select(pnAttachment);
                    }
                    break;
                case "btnRemoveAttachment":
                    if (poGLControllers.PaymentRequest().getTransactionAttachmentCount() <= 0) {
                        return;
                    } else {
                        for (int lnCtr = 0; lnCtr < poGLControllers.PaymentRequest().getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                if (pnAttachment == lnCtr) {
                                    return;
                                }
                            }
                        }
                    }
                    poJSON = poGLControllers.PaymentRequest().removeAttachment(pnAttachment);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, psFormName, (String) poJSON.get("message"));
                        return;
                    }
                    attachment_data.remove(tblAttachments.getSelectionModel().getSelectedIndex());
                    if (pnAttachment != 0) {
                        pnAttachment -= 1;
                    }
                    loadRecordAttachment(false);
                    loadTableAttachment();
                    if (attachment_data.size() <= 0) {
                        JFXUtil.clearTextFields(apAttachments);
                    }
                    initAttachmentsGrid();
                    break;
                case "btnArrowLeft":
                    slideImage(-1);
                    break;
                case "btnArrowRight":
                    slideImage(1);
                    break;
                case "btnReturn":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to return transaction?")) {
                        poJSON = poGLControllers.PaymentRequest().ReturnTransaction("Returned");
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        tblVwPaymentRequest.refresh();
                        main_data.get(pnTblMainRow).setIndex06(PaymentRequestStatus.RETURNED);
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    String lsStat = "";
                    if (PaymentRequestStatus.CONFIRMED.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus())) {
                        lsStat = "cancel";
                    } else {
                        lsStat = "void";
                    }
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to " + lsStat + " the transaction?")) {
                        if (PaymentRequestStatus.CONFIRMED.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus())) {
                            poJSON = poGLControllers.PaymentRequest().CancelTransaction("");
                        } else {
                            poJSON = poGLControllers.PaymentRequest().VoidTransaction("");
                        }
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        tblVwPaymentRequest.refresh();
                        main_data.get(pnTblMainRow).setIndex06(PaymentRequestStatus.VOID);
                    } else {
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            cmdReloadProcess(lsButton);
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cmdReloadProcess(String lsButton) {
        try {
            if (JFXUtil.isObjectEqualTo(lsButton, "btnVoid", "btnReturn")) {

                poGLControllers.PaymentRequest().resetMaster();
                poGLControllers.PaymentRequest().resetOthers();
                poGLControllers.PaymentRequest().Detail().clear();
                poGLControllers.PaymentRequest().setIndustryId(psIndustryID);
                poGLControllers.PaymentRequest().setCompanyId(psCompanyID);
                poGLControllers.PaymentRequest().Master().setIndustryID(psIndustryID);
                poGLControllers.PaymentRequest().Master().setCompanyID(psCompanyID);
                clearMasterFields();
                clearDetailFields();
                CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                pnEditMode = EditMode.UNKNOWN;

            }
            if (lsButton.equals("btnRetrieve") || lsButton.equals("btnAddAttachment") || lsButton.equals("btnRemoveAttachment")
                    || lsButton.equals("btnArrowRight") || lsButton.equals("btnArrowLeft") || lsButton.equals("btnRetrieve") || lsButton.equals("btnHistory")) {
            } else {
                loadRecordMaster();
                loadTableDetail();
                loadTableAttachment();
            }
            initButtons(pnEditMode);
            initFields(pnEditMode);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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

                if (!ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save?")) {
                    return;
                }

                pnTblDetailRow = -1;
                try {
                    if (pnEditMode == EditMode.UPDATE) {
                        poGLControllers.PaymentRequest().Master().setModifiedDate(poApp.getServerDate());
                        poGLControllers.PaymentRequest().Master().setModifyingId(poApp.getUserID());
                        for (int i = 0; i < poGLControllers.PaymentRequest().getDetailCount(); i++) {
                            poGLControllers.PaymentRequest().Detail(i).setModifiedDate(poApp.getServerDate());
                        }
                    }

                    poJSON = poGLControllers.PaymentRequest().isDetailHasZeroAmount();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        if ("true".equals((String) poJSON.get("warning"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            pnTblDetailRow = (int) poJSON.get("tableRow");
                            loadTableDetail();
                            initDetailFocus();
                            return;
                        } else {
                            if (!ShowMessageFX.YesNo((String) poJSON.get("message"), psFormName, null)) {
                                pnTblDetailRow = (int) poJSON.get("tableRow");
                                loadTableDetail();
                                initDetailFocus();
                                return;
                            }
                        }
                    }
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                    return;
                }

                runWithLoadingAllowFxDialogs(
                        () -> {
                            try {
                                JSONObject loSaveJSON = poGLControllers.PaymentRequest().SaveTransaction();
                                loSaveResultRef.set(loSaveJSON);

                                if ("success".equals(String.valueOf(loSaveJSON.get("result")))) {
                                    JSONObject loOpenJSON = poGLControllers.PaymentRequest().OpenTransaction(poGLControllers.PaymentRequest().Master().getTransactionNo());
                                    loOpenResultRef.set(loOpenJSON);
                                }
                            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                                throw new RuntimeException(ex);
                            }

                        },
                        () -> {
                            try {
                                JSONObject loSaveJSON = loSaveResultRef.get();
                                if (loSaveJSON == null) {
                                    ShowMessageFX.Error("Unable to save transaction.", psFormName, null);
                                    return;
                                }

                                if (!"success".equals(String.valueOf(loSaveJSON.get("result")))) {
                                    ShowMessageFX.Warning((String) loSaveJSON.get("message"), psFormName, null);
                                    loadTableDetail();
                                    return;
                                }

                                ShowMessageFX.Information((String) loSaveJSON.get("message"), psFormName, null);

                                JSONObject loOpenJSON = loOpenResultRef.get();
                                if (loOpenJSON != null
                                && "success".equals(String.valueOf(loOpenJSON.get("result")))
                                && PaymentRequestStatus.OPEN.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus())) {
                                    if (ShowMessageFX.YesNo(null, psFormName, "Do you want to confirm this transaction?")) {
                                        JSONObject loConfirmJSON = poGLControllers.PaymentRequest().ConfirmTransaction("Confirmed");
                                        if ("success".equals(String.valueOf(loConfirmJSON.get("result")))) {
                                            ShowMessageFX.Information((String) loConfirmJSON.get("message"), psFormName, null);
                                            poGLControllers.PaymentRequest().OpenTransaction(poGLControllers.PaymentRequest().Master().getTransactionNo());
                                            tblVwPaymentRequest.refresh();
                                            if (pnTblMainRow >= 0 && pnTblMainRow < main_data.size()) {
                                                main_data.get(pnTblMainRow).setIndex06(PaymentRequestStatus.CONFIRMED);
                                            }
                                        } else {
                                            ShowMessageFX.Warning((String) loConfirmJSON.get("message"), psFormName, null);
                                        }
                                    }
                                }
                                loadRecordMaster();
                                loadTableDetail();
                                pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                                pagination.toBack();
                                cmdReloadProcess("btnSave");
                            } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                            }

                        },
                        ex -> {
                            Throwable loRoot = ex.getCause() != null ? ex.getCause() : ex;
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, loRoot);
                            ShowMessageFX.Error(MiscUtil.getException(loRoot), psFormName, null);
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

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            if (poGLControllers.PaymentRequest().getEditMode() == EditMode.READY || poGLControllers.PaymentRequest().getEditMode() == EditMode.UPDATE) {
                poGLControllers.PaymentRequest().loadAttachments();
                Platform.runLater(() -> {
                    loadTableDetail();
                });
                imageView.setImage(null);
                stackPaneClip();
                Platform.runLater(() -> {
                    loadTableAttachment();
                });
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            boolean lbShow2 = pnEditMode == EditMode.UPDATE;
            JFXUtil.setDisabled(!lbShow2, cmbAttachmentType, btnAddAttachment, btnRemoveAttachment);
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                }
                int lnAttachmentType = 0;
                lnAttachmentType = Integer.parseInt(lsAttachmentType);
                cmbAttachmentType.getSelectionModel().select(lnAttachmentType);
                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";

                        // in server
                        if (poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                            filePath2 = poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
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
                                JFXUtil.adjustImageSize(loimage, imageView, ldstackPaneWidth, ldstackPaneHeight);

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
                                JFXUtil.PDFViewConfig(filePath2, stackPane1, btnArrowLeft, btnArrowRight, ldstackPaneWidth, ldstackPaneHeight);
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

    private void loadTableAttachment() {
        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblAttachments.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                scaleFactor = 1.0;
                JFXUtil.resetImageBounds(imageView, stackPane1);
                Platform.runLater(() -> {
                    try {
                        attachment_data.clear();
                        int lnCtr;
                        int lnCount = 0;
                        for (lnCtr = 0; lnCtr < poGLControllers.PaymentRequest().getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                continue;
                            }
                            lnCount += 1;
                            attachment_data.add(
                                    new ModelPRFAttachment(String.valueOf(lnCount),
                                            String.valueOf(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getFileName()),
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

                return null;
            }

            @Override
            protected void succeeded() {
                if (attachment_data == null || attachment_data.isEmpty()) {
                    tblAttachments.setPlaceholder(placeholderLabel);
                } else {
                    tblAttachments.toFront();
                }
                progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (attachment_data == null || attachment_data.isEmpty()) {
                    tblAttachments.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background

    }

    private void initStackPaneListener() {
        stackPane1.widthProperty().addListener((observable, oldValue, newWidth) -> {
            double computedWidth = newWidth.doubleValue();
            ldstackPaneWidth = computedWidth;

        });
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            ldstackPaneHeight = computedHeight;
            loadTableAttachment();
            loadRecordAttachment(true);
            initAttachmentsGrid();
        });
    }

    private void initAttachmentPreviewPane() {
        stackPane1.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            stackPane1.setClip(new javafx.scene.shape.Rectangle(
                    newBounds.getMinX(),
                    newBounds.getMinY(),
                    newBounds.getWidth(),
                    newBounds.getHeight()
            ));
        });
        imageView.setOnScroll((ScrollEvent event) -> {
            double delta = event.getDeltaY();
            scaleFactor = Math.max(0.5, Math.min(scaleFactor * (delta > 0 ? 1.1 : 0.9), 5.0));
            imageView.setScaleX(scaleFactor);
            imageView.setScaleY(scaleFactor);
        });

        imageView.setOnMousePressed((MouseEvent event) -> {
            mouseAnchorX = event.getSceneX() - imageView.getTranslateX();
            mouseAnchorY = event.getSceneY() - imageView.getTranslateY();
        });

        imageView.setOnMouseDragged((MouseEvent event) -> {
            double translateX = event.getSceneX() - mouseAnchorX;
            double translateY = event.getSceneY() - mouseAnchorY;
            imageView.setTranslateX(translateX);
            imageView.setTranslateY(translateY);
        });

        stackPane1.widthProperty().addListener((observable, oldValue, newWidth) -> {
            double computedWidth = newWidth.doubleValue();
            ldstackPaneWidth = computedWidth;

        });
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            ldstackPaneHeight = computedHeight;

            //Placed to get height and width of stack pane in computed size before loading the image
            initStackPaneListener();
            initAttachmentsGrid();
        });

    }

    private void initAttachmentsGrid() {
        /*FOCUS ON FIRST ROW*/
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
        tblAttachments.setItems(attachment_data);

        if (pnAttachment < 0 || pnAttachment >= attachment_data.size()) {
            if (!attachment_data.isEmpty()) {
                /* FOCUS ON FIRST ROW */
                tblAttachments.getSelectionModel().select(0);
                tblAttachments.getFocusModel().focus(0);
                pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            }
        } else {
            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
            tblAttachments.getSelectionModel().select(pnAttachment);
            tblAttachments.getFocusModel().focus(pnAttachment);
        }

    }

    private void slideImage(int direction) {
        if (attachment_data.size() <= 0) {
            return;
        }

        currentIndex = pnAttachment;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
            ModelPRFAttachment image = attachment_data.get(newIndex);
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
            slideOut.setByX(direction * -400); // Move left or right

            tblAttachments.getFocusModel().focus(newIndex);
            tblAttachments.getSelectionModel().select(newIndex);
            pnAttachment = newIndex;
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
        if (isImageViewOutOfBounds(imageView, stackPane1)) {
            resetImageBounds();
        }
    }

    private boolean isImageViewOutOfBounds(ImageView imageView, StackPane stackPane) {
        Bounds clipBounds = stackPane.getClip().getBoundsInParent();
        Bounds imageBounds = imageView.getBoundsInParent();

        return imageBounds.getMaxX() < clipBounds.getMinX()
                || imageBounds.getMinX() > clipBounds.getMaxX()
                || imageBounds.getMaxY() < clipBounds.getMinY()
                || imageBounds.getMinY() > clipBounds.getMaxY();
    }

    private void resetImageBounds() {
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        stackPane1.setAlignment(imageView, javafx.geometry.Pos.CENTER);
    }

    private void stackPaneClip() {
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(
                stackPane1.getWidth() - 8, // Subtract 10 for padding (5 on each side)
                stackPane1.getHeight() - 8 // Subtract 10 for padding (5 on each side)
        );
        clip.setArcWidth(8); // Optional: Rounded corners for aesthetics
        clip.setArcHeight(8);
        clip.setLayoutX(4); // Set padding offset for X
        clip.setLayoutY(4); // Set padding offset for Y
        stackPane1.setClip(clip);

    }

    private void initTextFieldFocus() {
        tfBranch.setOnMouseClicked(e -> activeField = tfBranch);
        tfPayee.setOnMouseClicked(e -> activeField = tfPayee);
        tfDepartment.setOnMouseClicked(e -> activeField = tfDepartment);
        tfSearchPayee.setOnMouseClicked(e -> activeField = tfSearchPayee);
        tfSearchTransaction.setOnMouseClicked(e -> activeField = tfSearchTransaction);
        tfParticular.setOnMouseClicked(e -> activeField = tfParticular);
    }

    private void initTextAreaFocus() {
        taRemarks.focusedProperty().addListener(txtArea_Focus);
    }

    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextArea = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextAreaID = loTextArea.getId();
        String lsValue = loTextArea.getText();
        if (lsValue == null) {
            return;
        }
        try {
            if (!nv) {
                /*Lost Focus*/
                switch (lsTextAreaID) {
                    case "taRemarks":
                        poGLControllers.PaymentRequest().Master().setRemarks(lsValue);
                        break;
                }
            } else {
                loTextArea.selectAll();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    };

    //revised by Arsiela 05-05-2026
    private boolean checkSource() throws SQLException, GuanzonException {
        if (poGLControllers.PaymentRequest().Master().getSourceNo() != null && !"".equals(poGLControllers.PaymentRequest().Master().getSourceNo())) {
            return true;
        } else {
            if (poGLControllers.PaymentRequest().getDetailCount() > 0) {
                if ((poGLControllers.PaymentRequest().Detail(0).getRecurringNo() != null && !"".equals(poGLControllers.PaymentRequest().Detail(0).getRecurringNo()))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void reloadDetails() throws SQLException, GuanzonException {
        int detailCount = poGLControllers.PaymentRequest().getDetailCount();
        for (int lnCtr = detailCount - 1; lnCtr >= 0; lnCtr--) {
            if (poGLControllers.PaymentRequest().Detail(lnCtr).getParticularID().isEmpty()
                    || poGLControllers.PaymentRequest().Detail(lnCtr).getAmount() == 0.0000) {
                continue; // Skip deleting this row
            }
            poGLControllers.PaymentRequest().Detail().remove(lnCtr);
        }
        poGLControllers.PaymentRequest().Master().setSourceCode("");
        poGLControllers.PaymentRequest().Master().setSourceNo("");
        pnTblDetailRow = -1;
        pnTblMainRow = -1;
        clearDetailFields();
        loadTableDetail();
    }

    boolean pbShow = false;

    private boolean isExchangingPayee(String fsPayeeID, boolean keyPressed) {
        if (pnEditMode == EditMode.UPDATE) {
            try {
                if (checkSource()) {
                    if (pnEditMode == EditMode.UPDATE) {
                        if (!pbShow) {
                            ShowMessageFX.Warning("A transaction source is already linked to this Payment Request.\nChanging the payee during an update is not permitted.", psFormName, null);
                            pbShow = true;
                        }
                        return false;
                    }

                    if (!pbShow) {
                        pbShow = true;
                        if (!ShowMessageFX.YesNo("This Payment Request is linked to a transaction source. Are you sure you want to change the payee?\nPlease note that proceeding will reset all currently entered information.", psFormName, null)) {
                            poGLControllers.PaymentRequest().Master().setPayeeID(fsPayeeID);
                            tfPayee.setText(poGLControllers.PaymentRequest().Master().Payee().getPayeeName());
                            return false;
                        }

                        //Reload Details
                        reloadDetails();

                        try {
                            if (keyPressed) {
                                poJSON = new JSONObject();
                                poJSON = poGLControllers.PaymentRequest().SearchPayee(tfPayee.getText(), false);
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return false;
                                }
                                tfPayee.setText(poGLControllers.PaymentRequest().Master().Payee().getPayeeName());
                                return false;
                            }

                        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass()
                                    .getName()).log(Level.SEVERE, null, ex);

                        }
                    }
                } else {
                    if (!pbShow && keyPressed) {
                        pbShow = true;
                        try {
                            poJSON = new JSONObject();
                            poJSON = poGLControllers.PaymentRequest().SearchPayee(tfPayee.getText(), false);
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                return false;
                            }
                            tfPayee.setText(poGLControllers.PaymentRequest().Master().Payee().getPayeeName());
                            return false;

                        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass()
                                    .getName()).log(Level.SEVERE, null, ex);

                        }
                    }
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass()
                        .getName()).log(Level.SEVERE, null, ex);

            }
        }

        return true;
    }

    //Added by arsiela 05-06-2026
    private boolean isExchangingDepartment(String fsDepartmentId, boolean keyPressed) {
        if (pnEditMode == EditMode.UPDATE) {
            try {
                if (checkSource() && !InvTransCons.PURCHASE_ORDER.equals(poGLControllers.PaymentRequest().Master().getSourceCode())) {
                    if (pnEditMode == EditMode.UPDATE) {
                        if (!pbShow) {
                            ShowMessageFX.Warning("A transaction source is already linked to this Payment Request.\nChanging the department during an update is not permitted.", psFormName, null);
                            pbShow = true;
                        }
                        return false;
                    }

                    if (!pbShow) {
                        pbShow = true;
                        if (!ShowMessageFX.YesNo("This Payment Request is linked to a transaction source. Are you sure you want to change the department?\nPlease note that proceeding will reset all currently entered information.", psFormName, null)) {
                            poGLControllers.PaymentRequest().Master().setDepartmentID(fsDepartmentId);
                            tfDepartment.setText(poGLControllers.PaymentRequest().Master().Department().getDescription());
                            return false;
                        }

                        //Reload Details
                        reloadDetails();

                        try {
                            if (keyPressed) {
                                poJSON = new JSONObject();
                                poJSON = poGLControllers.PaymentRequest().SearchDepartment(tfDepartment.getText(), false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfDepartment.setText("");
                                    return false;
                                }
                                tfDepartment.setText(poGLControllers.PaymentRequest().Master().Department().getDescription());
                                return false;
                            }

                        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass()
                                    .getName()).log(Level.SEVERE, null, ex);

                        }
                    }
                } else {
                    if (!pbShow && keyPressed) {
                        pbShow = true;
                        try {
                            poJSON = new JSONObject();
                            poJSON = poGLControllers.PaymentRequest().SearchDepartment(tfDepartment.getText(), false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                tfDepartment.setText("");
                                return false;
                            }
                            tfDepartment.setText(poGLControllers.PaymentRequest().Master().Department().getDescription());
                            return false;

                        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass()
                                    .getName()).log(Level.SEVERE, null, ex);

                        }
                    }
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass()
                        .getName()).log(Level.SEVERE, null, ex);

            }
        }

        return true;
    }

    //Added by Arsiela 05-16-2026 05:09 PM
    private boolean isExchangingBranch(String fsBranchID, boolean keyPressed) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            try {
                if (checkSource()) {
                    if (pnEditMode == EditMode.UPDATE) {
                        if (!pbShow) {
                            ShowMessageFX.Warning("A transaction source is already linked to this Payment Request.\nChanging the branch during an update is not permitted.", psFormName, null);
                            pbShow = true;
                        }
                        return false;
                    }

                    if (!pbShow) {
                        pbShow = true;
                        if (!ShowMessageFX.YesNo("This Payment Request is linked to a transaction source. Are you sure you want to change the branch?\nPlease note that proceeding will reset all currently entered information.", psFormName, null)) {
                            poGLControllers.PaymentRequest().Master().setBranchCode(fsBranchID);
                            tfBranch.setText(poGLControllers.PaymentRequest().Master().Branch().getBranchName());
                            return false;
                        }

                        //Reload Details
                        reloadDetails();

                        try {
                            if (keyPressed) {
                                poJSON = new JSONObject();
                                poJSON = poGLControllers.PaymentRequest().SearchBranch(tfBranch.getText(), false);
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return false;
                                }
                                tfBranch.setText(poGLControllers.PaymentRequest().Master().Branch().getBranchName());
                                return false;
                            }

                        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass()
                                    .getName()).log(Level.SEVERE, null, ex);

                        }
                    }
                } else {
                    if (!pbShow && keyPressed) {
                        pbShow = true;
                        try {
                            poJSON = new JSONObject();
                            poJSON = poGLControllers.PaymentRequest().SearchBranch(tfBranch.getText(), false);
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                return false;
                            }
                            tfBranch.setText(poGLControllers.PaymentRequest().Master().Branch().getBranchName());
                            return false;

                        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass()
                                    .getName()).log(Level.SEVERE, null, ex);

                        }
                    }
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass()
                        .getName()).log(Level.SEVERE, null, ex);

            }
        }

        return true;
    }

    private void initTextFieldKeyPressed() {
        List<TextField> loTxtField = Arrays.asList(
                tfParticular, tfSearchTransaction, tfSearchPayee,
                tfAmount, tfDiscRate, tfDiscAmountDetail, tfBranch, tfPayee, tfDepartment);

        loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (lsTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = lsTxtField.getText();
        }
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfPayee":
                                if (!isExchangingPayee(poGLControllers.PaymentRequest().Master().getPayeeID(), true)) {
                                    pbShow = false;
                                    return;
                                }
                                pbShow = false;
                                break;
                            case "tfDepartment":
                                if (!isExchangingDepartment(poGLControllers.PaymentRequest().Master().getDepartmentID(), true)) {
                                    pbShow = false;
                                    return;
                                }
                                pbShow = false;
                                break;
                            case "tfBranch":
                                if (!isExchangingBranch(poGLControllers.PaymentRequest().Master().getBranchCode(), true)) {
                                    pbShow = false;
                                    return;
                                }
                                pbShow = false;
                                break;
                            case "tfSearchTransaction":
                                loadTableMain();
                                break;
                            case "tfSearchPayee":
                                poJSON = poGLControllers.PaymentRequest().SearchPayee(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfSearchPayee.setText("");
                                    break;
                                }
                                psPayeeID = poGLControllers.PaymentRequest().Master().getPayeeID();
                                tfSearchPayee.setText(poGLControllers.PaymentRequest().Master().Payee().getPayeeName());
//                                if (!tfSearchPayee.getText().isEmpty()) {
                                loadTableMain();
//                                }
                                break;
                            case "tfParticular":
                                if (!tfPayee.getText().isEmpty()) {
                                    if (pnTblDetailRow < 0) {
                                        ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                                        clearDetailFields();
                                        break;
                                    }
                                    poJSON = poGLControllers.PaymentRequest().SearchParticular(lsValue, false, pnTblDetailRow);
                                    if ("error".equals(poJSON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                        tfParticular.setText("");
                                        if (poJSON.get("row") != null) {
                                            Object obj = poJSON.get("row");
                                            int value = Integer.valueOf(String.valueOf(obj));
                                            pnTblDetailRow = value;
                                        } else {
                                            break;
                                        }
                                    } else {
                                        if (poJSON.get("row") != null) {
                                            Object obj = poJSON.get("row");
                                            int value = Integer.valueOf(String.valueOf(obj));
                                            pnTblDetailRow = value;
                                        } else {
                                            JFXUtil.textFieldMoveNext(tfAmountDetail);
                                        }
                                    }
                                    loadTableDetail();
                                    initDetailFocus();

                                } else {
                                    ShowMessageFX.Warning("Please enter Payee field first.", psFormName, null);
                                    tfPayee.requestFocus();
                                }
                                break;
                        }
                        switch (txtFieldID) {
                            case "tfBranch":
                            case "tfPayee":
                            case "tfDepartment":
                                CommonUtils.SetNextFocus((TextField) event.getSource());
                                break;
                            case "tfAmount":
                                lsValue = JFXUtil.removeComma(lsValue);
                                poJSON = poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAmount(Double.parseDouble(lsValue));
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    CommonUtils.SetNextFocus((TextField) event.getSource());
                                }
                                loadTableDetail();
                                break;
                            case "tfDiscRate":
                                lsValue = JFXUtil.removeComma(lsValue);
                                poJSON = poGLControllers.PaymentRequest().setDiscountRate(Double.parseDouble(lsValue), pnTblDetailRow);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    CommonUtils.SetNextFocus((TextField) event.getSource());
                                }
                                loadTableDetail();
                                break;
                            case "tfDiscAmountDetail":
                                lsValue = JFXUtil.removeComma(lsValue);
                                poJSON = poGLControllers.PaymentRequest().setDiscountAmount(Double.parseDouble(lsValue), pnTblDetailRow);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    pnTblDetailRow = Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(tblVwPRDetail)).getIndex06());
                                    initDetailFocus();
                                }
                                loadTableDetail();

                                break;
                        }
                        event.consume();
                        break;
                    case UP:
                        if (JFXUtil.isObjectEqualTo(lsTxtField.getId(), "tfParticular", "tfAmount", "tfDiscRate", "tfDiscAmountDetail")) {
                            pnTblDetailRow = Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(tblVwPRDetail)).getIndex06());
                        }
                        loadRecordDetail();
                        initDetailFocus();
                        event.consume();
                        break;
                    case DOWN:
                        if (JFXUtil.isObjectEqualTo(lsTxtField.getId(), "tfParticular", "tfAmount", "tfDiscRate", "tfDiscAmountDetail")) {
                            pnTblDetailRow = Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(tblVwPRDetail)).getIndex06());
                        }
                        loadRecordDetail();
                        initDetailFocus();
                        event.consume(); // Consume event after handling focus
                        break;
                    default:
                        break;

                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void initTextFieldPattern() {
        CustomCommonUtil.inputDecimalOnly(
                tfTotalAmount, tfDiscountAmount, tfNetAmount,
                tfAmount, tfDiscRate, tfDiscAmountDetail);
        JFXUtil.setCommaFormatter(tfAmountDetail);

        JFXUtil.handleDisabledNodeClick(apDetail, pnEditMode, nodeID -> {
            try {
                switch (nodeID) {
                    case "tfParticular":
                        //define if recurring id exists if not define if the editmode of the item is update
                        boolean lbIsRecurring;
                        lbIsRecurring = !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getRecurringNo(), null, "");
                        if (lbIsRecurring) {
                            ShowMessageFX.Warning(null, psFormName, "This field is disabled by default as it is linked to Recurring.");
                        } else {
                            ShowMessageFX.Warning(null, psFormName, "This field is disabled by default as the item has already been saved.");
                        }
                        break;
                    case "cbReverse":
                        ShowMessageFX.Warning(null, psFormName,
                                "Reverse is disabled for non-open transactions.");
                        break;
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });

        JFXUtil.handleDisabledNodeClick(apMaster, pnEditMode, nodeID -> {
            try {
                switch (nodeID) {
                    case "taRemarks":
                        if (!PaymentRequestStatus.OPEN.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus())) {
                            ShowMessageFX.Warning(null, psFormName,
                                    "Remarks is disabled for non-open transactions.");
                        }
                        break;
                    case "tfBranch":
                    case "tfPayee":
                    case "tfDepartment":
                        //define if source no is exist
                        boolean lbIsRecurring = !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getRecurringNo(), null, "") || !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Master().getSourceNo(), null, "");
                        if (pnEditMode == EditMode.UPDATE && lbIsRecurring) {
                            ShowMessageFX.Warning(null, psFormName, "This field is disabled by default as it is linked to recurring expenses.");
                        } else {
                            if (!PaymentRequestStatus.OPEN.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus())) {
                                ShowMessageFX.Warning(null, psFormName,
                                        "This field is disabled for non-open transactions.");
                            }
                        }
                        break;
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void initDatePickerActions() {
        dpTransaction.setOnAction(e -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                if (dpTransaction.getValue() != null) {
                    try {
                        poGLControllers.PaymentRequest().Master().setTransactionDate(SQLUtil.toDate(dpTransaction.getValue().toString(), SQLUtil.FORMAT_SHORT_DATE));
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private void initCheckBoxActions() {
        cbReverse.setOnAction(event -> {
            try {
                if (poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getEditMode() == EditMode.ADDNEW) {
                    poGLControllers.PaymentRequest().Detail().remove(pnTblDetailRow);
                } else {
                    poGLControllers.PaymentRequest().Detail(pnTblDetailRow).isReverse(cbReverse.isSelected());
                }
                loadTableDetail();
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void clearMasterFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        pnTblDetailRow = -1;
        lblStatus.setText("");
        imageView.setImage(null);
        JFXUtil.clearTextFields(apMaster, apAttachments);
    }

    private void clearDetailFields() {
        JFXUtil.clearTextFields(apDetail);
    }

    private void initButtons(int fnEditMode) {
        try {
            boolean lbShow = (pnEditMode == EditMode.UPDATE);

            btnClose.setVisible(!lbShow);
            btnClose.setManaged(!lbShow);

            CustomCommonUtil.setVisible(lbShow, btnSave, btnCancel);
            CustomCommonUtil.setManaged(lbShow, btnSave, btnCancel);

            CustomCommonUtil.setVisible(false, btnConfirm, btnReturn, btnVoid, btnUpdate, btnSearch);
            CustomCommonUtil.setManaged(false, btnConfirm, btnReturn, btnVoid, btnUpdate, btnSearch);
            CustomCommonUtil.setVisible(lbShow, btnSearch);
            CustomCommonUtil.setManaged(lbShow, btnSearch);
            JFXUtil.setButtonsVisibility(fnEditMode == EditMode.READY, btnHistory);
            JFXUtil.setDisabled(!lbShow, apMaster, apDetail, apAttachments);
            if (fnEditMode == EditMode.READY) {

                switch (poGLControllers.PaymentRequest().Master().getTransactionStatus()) {
                    case PaymentRequestStatus.OPEN:
                        CustomCommonUtil.setVisible(true, btnConfirm, btnVoid, btnUpdate);
                        CustomCommonUtil.setManaged(true, btnConfirm, btnVoid, btnUpdate);
                        break;
                    case PaymentRequestStatus.CONFIRMED:
                        CustomCommonUtil.setVisible(true, btnVoid, btnUpdate);
                        CustomCommonUtil.setManaged(true, btnVoid, btnUpdate);
                        break;
                }

            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        CustomCommonUtil.setDisable(!lbShow, tfPayee, tfAmount, taRemarks);
        CustomCommonUtil.setDisable(true, tfPayee, dpTransaction, tfTransactionNo, tfBranch,
                tfSeriesNo, tfTotalAmount, tfDiscountAmount, tfNetAmount,
                tfDepartment);
        if (poApp.isMainOffice() || poApp.isWarehouse()) {
            tfDepartment.setDisable(!lbShow); //mag open siya pag add new or update sa editmode
        }
        if (poApp.isMainOffice()) {
            tfBranch.setDisable(!lbShow); //mag open siya pag add new or update sa editmode
        }
    }

    private void loadTableMain() {
        btnRetrieve.setDisable(true);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        tblVwPaymentRequest.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        poJSON = new JSONObject();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                main_data.clear();
                poJSON = poGLControllers.PaymentRequest().getPaymentRequest(tfSearchTransaction.getText(), psPayeeID);
                Platform.runLater(() -> {
                    if ("success".equals(poJSON.get("result"))) {
                        try {
                            if (poGLControllers.PaymentRequest().getPRFMasterCount() > 0) {
                                for (int lnCntr = 0; lnCntr < poGLControllers.PaymentRequest().getPRFMasterCount(); lnCntr++) {
                                    main_data.add(new ModelTableMain(
                                            String.valueOf(lnCntr + 1),
                                            poGLControllers.PaymentRequest().poPRFMaster(lnCntr).getTransactionNo(),
                                            CustomCommonUtil.formatDateToShortString(poGLControllers.PaymentRequest().poPRFMaster(lnCntr).getTransactionDate()),
                                            poGLControllers.PaymentRequest().poPRFMaster(lnCntr).Branch().getBranchName(),
                                            poGLControllers.PaymentRequest().poPRFMaster(lnCntr).Payee().getPayeeName(),
                                            poGLControllers.PaymentRequest().poPRFMaster(lnCntr).getTransactionStatus(),
                                            "", "", "", ""));
                                }
                            } else {
                                main_data.clear();
                            }
                        } catch (GuanzonException | SQLException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    if (main_data.isEmpty()) {
                        tblVwPaymentRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                    }
                    JFXUtil.loadTab(pagination, main_data.size(), 50, tblVwPaymentRequest, filteredData);
                });
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false); // ✅ Re-enable the button

                if (main_data == null || main_data.isEmpty()) {
                    tblVwPaymentRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                } else {
                    tblVwPaymentRequest.toFront();
                }
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false); // ✅ Re-enable the button even if failed
            }
        };

        new Thread(task).start();
    }

    private void initTablePaymentRequest() {
        JFXUtil.setColumnCenter(tblRowNo, tblTransactionNo, tblTransactionDate);
        JFXUtil.setColumnLeft(tblBranch, tblPayee);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwPaymentRequest);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwPaymentRequest);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblVwPaymentRequest.setItems(filteredData);
        initTableHighlithers();
    }

    private void initTableHighlithers() {
        tblVwPaymentRequest.setRowFactory(tv -> new TableRow<ModelTableMain>() {
            @Override
            protected void updateItem(ModelTableMain item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Assuming empIndex06 corresponds to an employee status
                    String status = item.getIndex06(); // Replace with actual getter
                    switch (status) {
                        case PaymentRequestStatus.CONFIRMED:
                            setStyle("-fx-background-color: #C1E1C1;");
                            break;
                        case PaymentRequestStatus.VOID:
                            setStyle("-fx-background-color: #FAA0A0;");
                            break;
                        case PaymentRequestStatus.RETURNED:
                            setStyle("-fx-background-color: #FAC898;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                    tblVwPaymentRequest.refresh();
                }
            }
        });
    }

    private void loadTableDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: transparent;");

        tblVwPRDetail.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelTableDetail>> task = new Task<List<ModelTableDetail>>() {
            @Override
            protected List<ModelTableDetail> call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        detail_data.clear();
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            poGLControllers.PaymentRequest().ReloadDetail();
                        }

                        int detailCount = poGLControllers.PaymentRequest().getDetailCount();
                        int lnRowCount = 0;
                        for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
                            String lsIsVatable = "N";
                            if (poGLControllers.PaymentRequest().Detail(lnCtr).isVatable()) {
                                lsIsVatable = "Y";
                            }
                            if (!poGLControllers.PaymentRequest().Detail(lnCtr).isReverse()) {
                                continue;
                            }

                            double lnDetailDiscountRate = poGLControllers.PaymentRequest().Detail(lnCtr).getAmount() * poGLControllers.PaymentRequest().Detail(lnCtr).getDiscount();
                            double lnTotalDiscountAmount = poGLControllers.PaymentRequest().Detail(lnCtr).getAddDiscount() + lnDetailDiscountRate;
                            lnRowCount += 1;
                            detail_data.add(new ModelTableDetail(
                                    String.valueOf(lnRowCount),
                                    poGLControllers.PaymentRequest().Detail(lnCtr).Particular().getDescription(),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getAmount(), true),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDiscountAmount, true),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getNetTotal(), true),
                                    String.valueOf(lnCtr)
                            ));
                        }
                        tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTranTotal(), true));
                        tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getDiscountAmount(), true));
                        tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getNetTotal(), true));
                        reselectLastRow();
                        initFields(pnEditMode);

                        int lnTempRow = JFXUtil.getDetailRow(detail_data, pnTblDetailRow, 6); //this method is used only when Reverse is applied
                        if (lnTempRow < 0 || lnTempRow
                                >= detail_data.size()) {
                            if (!detail_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblVwPRDetail, 0);
                                int lnRow = Integer.parseInt(detail_data.get(0).getIndex06());
                                pnTblDetailRow = lnRow;
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblVwPRDetail, lnTempRow);
                            int lnRow = Integer.parseInt(detail_data.get(tblVwPRDetail.getSelectionModel().getSelectedIndex()).getIndex06());
                            pnTblDetailRow = lnRow;
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
            }
        };

        new Thread(task).start();
    }

    private void initTableDetail() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblParticular);
        JFXUtil.setColumnRight(tblAmount, tblDiscAmount, tbTotalAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwPRDetail);
        tblVwPRDetail.setItems(detail_data);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblVwPRDetail":
                    newIndex = isMovedDown
                            ? Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex06()) : Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex06());
                    if (!detail_data.isEmpty()) {
                        pnTblDetailRow = newIndex;
                        loadRecordDetail();
                        initDetailFocus();
                    }
                    break;
                case "tblAttachments":
                    newIndex = isMovedDown
                            ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03()) : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                    if (!attachment_data.isEmpty()) {
                        pnAttachment = newIndex;
                        loadRecordAttachment(true);
                    }
                    break;
            }
        }
    };

    // Method to reselect the last clicked row
    private void reselectLastRow() {
        if (pnTblDetailRow >= 0 && pnTblDetailRow < tblVwPRDetail.getItems().size()) {
            tblVwPRDetail.getSelectionModel().clearAndSelect(pnTblDetailRow);
            tblVwPRDetail.getSelectionModel().focus(pnTblDetailRow); // Scroll to the selected row if needed
        }
    }

    private void initTextFieldsProperty() {
        tfSearchPayee.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    try {
                        poGLControllers.PaymentRequest().Master().setPayeeID("");
                        tfSearchPayee.setText("");
                        psPayeeID = "";
                        loadTableMain();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
        tfSearchTransaction.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    tfSearchTransaction.setText("");
                    psTransactionNo = "";
                    loadTableMain();
                }
            }
        }
        );

        tfPayee.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) { // lost focus
                try {
                    if (tfPayee.getText() == null || tfPayee.getText().isEmpty()) {
                        if (!isExchangingPayee(poGLControllers.PaymentRequest().Master().getPayeeID(), false)) {
                            return;
                        }
                        poGLControllers.PaymentRequest().Master().setPayeeID("");
                        tfPayee.setText("");
                    }
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        tfDepartment.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) { // lost focus
                try {
                    if (tfDepartment.getText() == null || tfDepartment.getText().isEmpty()) {
                        if (!isExchangingDepartment(poGLControllers.PaymentRequest().Master().getDepartmentID(), false)) {
                            return;
                        }
                        poGLControllers.PaymentRequest().Master().setDepartmentID("");
                        tfDepartment.setText("");
                    }
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        tfBranch.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) { // lost focus
                try {
                    if (tfBranch.getText() == null || tfBranch.getText().isEmpty()) {
                        if (!isExchangingBranch(poGLControllers.PaymentRequest().Master().getBranchCode(), false)) {
                            return;
                        }
                        poGLControllers.PaymentRequest().Master().setBranchCode("");
                        tfBranch.setText("");
                    }
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void tblVwMain_Clicked(MouseEvent event) {
        poJSON = new JSONObject();
        if (event.getClickCount() == 2) {
            pnTblMainRow = tblVwPaymentRequest.getSelectionModel().getSelectedIndex();
            if (pnTblMainRow < 0 || pnTblMainRow >= tblVwPaymentRequest.getItems().size()) {
                ShowMessageFX.Warning("Please select valid payment request information.", "Warning", null);
                return;
            }
            ModelTableMain loSelectedPaymentRequest = (ModelTableMain) tblVwPaymentRequest.getSelectionModel().getSelectedItem();
            if (loSelectedPaymentRequest != null) {
                try {
                    String lsTransactionNo = loSelectedPaymentRequest.getIndex02();
                    if (!JFXUtil.loadValidation(pnEditMode, psFormName, poGLControllers.PaymentRequest().Master().getTransactionNo(), lsTransactionNo)) {
                        return;
                    }
                    poJSON = poGLControllers.PaymentRequest().InitTransaction();
                    if ("success".equals((String) poJSON.get("result"))) {
                        clearMasterFields();
                        clearDetailFields();
                        poJSON = poGLControllers.PaymentRequest().OpenTransaction(lsTransactionNo);
                        if ("success".equals((String) poJSON.get("result"))) {
                            CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                            loadTableDetailFromMain();
                            pnTblDetailRow = -1;
                            pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            pnEditMode = EditMode.UNKNOWN;
                        }
                        initButtons(pnEditMode);
                        initFields(pnEditMode);

                    }
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Warning("Error loading data: " + ex.getMessage(), psFormName, null);
                }
            }
        }

    }

    private void tblVwDetail_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            int lnRow = Integer.parseInt(detail_data.get(tblVwPRDetail.getSelectionModel().getSelectedIndex()).getIndex06());
            pnTblDetailRow = lnRow;
            ModelTableDetail selectedItem = (ModelTableDetail) tblVwPRDetail.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                clearDetailFields();
                if (selectedItem != null) {
                    if (pnTblDetailRow >= 0) {
                        loadRecordDetail();
                        initDetailFocus();
                    }
                }
            }
        }
    }

    private void initDetailFocus() {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {

            if (pnTblDetailRow >= 0) {
                try {
                    boolean isSourceNotEmpty = !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID(), null, "");
                    if (isSourceNotEmpty && !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID(), null, "")) {
                        tfAmount.requestFocus();
                    } else {
                        if (!JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID(), null, "")
                                && (pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW)) {
                            tfAmount.requestFocus();
                        } else {
                            tfParticular.requestFocus();
                        }
                    }
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    private void initComboBoxes() {
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAttachmentType);
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
