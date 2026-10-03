package ph.com.guanzongroup.integsys.views;

import com.jfoenix.controls.JFXTimePicker;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.*;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.SalesQoutation;
import ph.com.guanzongroup.cas.sales.SalesQoutationVersion;
import ph.com.guanzongroup.cas.sales.SalesQoutationVersionGiveaways;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Giveaways;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.CustomerInquiryFollowUpStatic;
import ph.com.guanzongroup.cas.sales.status.SalesInquiryStatic;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationStatic;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationVersionStatic;
import ph.com.guanzongroup.integsys.model.ModelCustomerInquiryFollowUpAttachment;
import ph.com.guanzongroup.integsys.model.ModelSalesReservationDetailx;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalesQoutationController implements Initializable, ScreenInterface {
    private String pxeModuleName = "Sales Qoutation";

    private GRiderCAS oApp;
    private SalesControllers oSalesController;
    private SalesQoutation poQuotation;
    private JSONObject poJSON;
    private int pnEditMode;
    private boolean pbLoaded = false;
    private boolean isFistFormLoad = true;

    private String fbDeliveryType = "";
    private String fbPaymentType = "";
    private String fbInsurance = "";
    private String fbVatType = "";
    private String fbRegistration = "";
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private volatile boolean isLoadingMaster = false;
    private volatile boolean isLoadingDetail = false;
    private int pnGawayRow = -1;
    private static final int GAWAY_EMPTY_ROWS = 1;
    private int pnMCRow = -1;
    private static final int MC_ITEMS_EMPTY_ROWS = 1;
    // =========================================================================
    // Table Data
    // =========================================================================
    private JSONArray data;
    private JSONArray data_details;
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();

    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private FilteredList<ModelTableMain> filteredMain_Data;
    private FilteredList<ModelTableDetail> filteredDetail_Data;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private static final double SUMMARY_MAX_HEIGHT = 800;
    // =========================================================================
    // Root Containers
    // =========================================================================
    @FXML private AnchorPane ChildAnchorPane;
    @FXML private AnchorPane apSearchMaster;
    @FXML private AnchorPane AnchorInputs;
    @FXML private AnchorPane anchorQoutation;
    @FXML private AnchorPane anchorVersion;
    @FXML private AnchorPane anchorOthers;
    @FXML private AnchorPane anchorDetails;
    @FXML private AnchorPane anchorMCItems;
    @FXML private AnchorPane anchorGawayItems;
    @FXML private AnchorPane anchorAdditionalRemarks;
    @FXML private HBox hbButtons;

    // =========================================================================
    // Action Buttons
    // =========================================================================
    @FXML private Button btnBrowse;
    @FXML private Button btnNew;
    @FXML private Button btnCreateFrom;
    @FXML private Button btnUpdate;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnApproved;
    @FXML private Button btnVoid;
    @FXML private Button btnLost;
    @FXML private Button btnFollowUp;
    @FXML private Button btnPrint;
    @FXML private Button btnExport;
    @FXML private Button btnClose;
    @FXML private Button btnAddClient;
    // =========================================================================
    // Customer Information Controls
    // =========================================================================
    @FXML private TextField tfTransNo;
    @FXML private TextField tfCustomerName;
    @FXML private FontAwesomeIconView faAdd;

    @FXML private DatePicker dpTransDate;
    @FXML private TextField tfContactNo;
    @FXML private TextField tfAddress;
    @FXML private Label lblStatus;
    @FXML private Label lblStatusVersion;


    // =========================================================================
    // Quotation Information Controls
    // =========================================================================
    @FXML private TextField tfVersionTransNo;
    @FXML private DatePicker dpQoutationDate;
    @FXML private TextField tfVersion;
    @FXML private TextField tfQoutationTitle;
    @FXML private DatePicker dpQoutationExpectedDate;
    @FXML private DatePicker dpQoutationValidity;
    @FXML private TextArea taReason;
    @FXML private TextArea taAdditionalRemarks;
    // =========================================================================
    // Delivery Information Controls
    // =========================================================================
    @FXML private ComboBox cmbDeliveryMethod;
    @FXML private TextField tfDeliveryAddress;
    @FXML private TextField tfDeliveryRemarks;
    @FXML private Label lblDeliverTo;


    // =========================================================================
    // Payment Information Controls
    // =========================================================================
    @FXML private ComboBox cmbPaymentVAT;
    @FXML private ComboBox cmbPaymentForm;
    @FXML private TextField tfPaymentRemarks;
    @FXML private TextField tfPaymentNoOfDays1;
    @FXML private ComboBox cmbInsurance;
    @FXML private ComboBox cmbRegistration;
    @FXML private TextField tfPaymentTerms;


    // =========================================================================
    // Motorcycle Item Information Controls
    // =========================================================================
    @FXML private TextField tfMCItemBrand;
    @FXML private TextField tfMCItemModel;
    @FXML private TextField tfMCItemInsuranceAmt;
    @FXML private TextField tfMCItemRegisAmt;
    @FXML private TextField tfMCItemDiscount;
    @FXML private TextField tfMCItemAddDiscount;
    @FXML private TextField tfMCItemFreight;
    @FXML private TextField tfMCItemQuantity;
    @FXML private TextField tfMCItemPromo;


    // =========================================================================
    // Motorcycle Item Table Controls
    // =========================================================================
    @FXML private TableView<ModelTableMain> tblMCItem;

    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemNo;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemBrand;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemModel;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemDesc;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemColor;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemVariant;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemSRP;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemDiscount;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemAddDiscount;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemFreight;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemReg;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemInsurance;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemQty;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemTotal;
    @FXML private TableColumn<ModelTableMain, String> tblRowMCitemAction;


    // =========================================================================
    // Giveaway Information Controls
    // =========================================================================
    @FXML private TextField tfGawayBarrcode;
    @FXML private TextField tfGawayDescription;
    @FXML private TextField tfGawayRemarks;
    @FXML private TextField tfGawayQty;


    // =========================================================================
    // Giveaway Table Controls
    // =========================================================================
    @FXML private TableView<ModelTableDetail> tblGawayItem;

    @FXML private TableColumn<ModelTableDetail, String> tblRowGawayNo;
    @FXML private TableColumn<ModelTableDetail, String> tblRowGawayItem;
    @FXML private TableColumn<ModelTableDetail, String> tblRowGawayItemCode;
    @FXML private TableColumn<ModelTableDetail, String> tblRowGawayDesc;
    @FXML private TableColumn<ModelTableDetail, String> tblRowGawayQty;
    @FXML private TableColumn<ModelTableDetail, String> tblRowGawayRemarks;
    @FXML private TableColumn<ModelTableDetail, String> tblRowGawayAction;

    // =========================================================================
    // Transaction Summary Controls
    // =========================================================================
    @FXML private TextArea taSummaryItem;
    @FXML private TextField tfSummarySubTotal;
    @FXML private TextField tfSummaryVat;
    @FXML private TextField tfSummaryVatEx;
    @FXML private TextField tfSummaryTotal;

    // =========================================================================
    // ScreenInterface Implementation
    // =========================================================================
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

    // =========================================================================
    // Initializable Implementation & Lifecycle Methods
    // =========================================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        initializeObject();
        pnEditMode = oSalesController.SalesQoutation().getEditMode();
        initButton(pnEditMode);
        InitTextFields();
        initTables();
        ClickButton();
        initComboBoxField();
        initDatePickers();
        pbLoaded = true;
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oSalesController = new SalesControllers(oApp, logwrapr);
            oSalesController.SalesQoutation().initialize();
            oSalesController.SalesQoutation().setRecordStatus(SalesQoutationStatic.OPEN);
            oSalesController.SalesQoutation().setWithUI(true);

            Platform.runLater(() -> btnNew.fire());
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }


    // =========================================================================
    // Component Initialization Methods
    // =========================================================================
    private void initButton(int fnValue) {
        try {

            CustomCommonUtil.setVisible(false, btnBrowse, btnNew, btnCreateFrom, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnLost, btnFollowUp, btnPrint, btnExport, btnClose);
            CustomCommonUtil.setManaged(false, btnBrowse, btnNew, btnCreateFrom, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnLost, btnFollowUp, btnPrint, btnExport, btnClose);
            anchorQoutation.setDisable(true);
            anchorVersion.setDisable(true);
            anchorOthers.setDisable(true);
            anchorMCItems.setDisable(true);
            anchorGawayItems.setDisable(true);
            anchorAdditionalRemarks.setDisable(true);
            switch (fnValue) {
                case EditMode.ADDNEW:
                    // When adding or updating, only show Save and Cancel
                    CustomCommonUtil.setVisible(true, btnSave, btnCancel, btnClose);
                    CustomCommonUtil.setManaged(true, btnSave, btnCancel, btnClose);
                    anchorQoutation.setDisable(false);
                    anchorVersion.setDisable(false);
                    anchorOthers.setDisable(false);
                    anchorMCItems.setDisable(false);
                    anchorGawayItems.setDisable(false);
                    anchorAdditionalRemarks.setDisable(false);
                    break;
                case EditMode.READY:
                    switch (oSalesController.SalesQoutation().getModel().getTransactionStatus()) {
                        case SalesQoutationStatic.OPEN:
                            CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnCreateFrom, btnUpdate, btnApproved, btnVoid, btnLost, btnFollowUp, btnPrint, btnExport, btnClose);
                            CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnCreateFrom, btnUpdate, btnApproved, btnVoid, btnLost, btnFollowUp, btnPrint, btnExport, btnClose);
                            break;
                        case SalesQoutationStatic.LOST:
                        case SalesQoutationStatic.SALES:
                        case SalesQoutationStatic.VOID:
                            CustomCommonUtil.setVisible(true, btnBrowse, btnPrint, btnExport, btnClose);
                            CustomCommonUtil.setManaged(true, btnBrowse, btnPrint, btnExport, btnClose);
                            break;
                    }
                    break; // added: READY no longer falls through into UPDATE
                case EditMode.UPDATE:
                    CustomCommonUtil.setVisible(true, btnSave, btnCancel, btnClose);
                    CustomCommonUtil.setManaged(true, btnSave, btnCancel, btnClose);
                    anchorQoutation.setDisable(false);
                    anchorVersion.setDisable(false);
                    anchorOthers.setDisable(false);
                    anchorMCItems.setDisable(false);
                    anchorGawayItems.setDisable(false);
                    anchorAdditionalRemarks.setDisable(false);
                    break;
                case EditMode.UNKNOWN:
                default:
                    // Default fallback: show only Browse, New and Close
                    CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnClose);
                    CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnClose);
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void InitTextFields() {
        registerFocusListener(anchorQoutation);
        registerFocusListener(anchorVersion);
        registerFocusListener(anchorOthers);
        registerFocusListener(anchorMCItems);
        registerFocusListener(anchorGawayItems);
        registerFocusListener(anchorAdditionalRemarks);
        registerKeyEvents();

        taSummaryItem.setEditable(false);
        taSummaryItem.setWrapText(true);
        taSummaryItem.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");   // monospace so the amounts line up
        taSummaryItem.setMaxHeight(SUMMARY_MAX_HEIGHT);
        taSummaryItem.widthProperty().addListener((obs, o, n) -> resizeSummaryItems());

        // register explicitly in case the recursive walk misses nested/skinned containers
        TextField[] laMCFields = {tfMCItemBrand, tfMCItemModel, tfMCItemQuantity,
                tfMCItemDiscount,tfMCItemAddDiscount, tfMCItemFreight, tfMCItemRegisAmt, tfMCItemInsuranceAmt, tfVersion, tfTransNo, tfVersionTransNo};
        for (TextField tf : laMCFields) {
            tf.focusedProperty().removeListener(txtField_Focus);
            tf.focusedProperty().addListener(txtField_Focus);
        }

        tblMCItem.setOnMouseClicked(this::setTblMCItem_Clicked);
        tblGawayItem.setOnMouseClicked(this::setTblGawayItem_Clicked);
    }

    private void initComboBoxField() {
        tfMCItemInsuranceAmt.setEditable(false);
        tfMCItemRegisAmt.setEditable(false);
        cmbDeliveryMethod.setItems(SalesQoutationStatic.DELIVERY_TYPE_DESCRIPTION);
        cmbPaymentForm.setItems(SalesQoutationStatic.PAYMENT_TYPE_DESCRIPTION);
        cmbPaymentVAT.setItems(SalesQoutationVersionStatic.VAT_TYPE_DESCRIPTION);
        cmbInsurance.setItems(SalesQoutationStatic.INSURANCE_DESCRIPTION);
        cmbRegistration.setItems(SalesQoutationStatic.REGISTRATION_DESCRIPTION);

        cmbDeliveryMethod.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.intValue() < 0) return;

            fbDeliveryType = SalesQoutationStatic.DELIVERY_TYPE_CODE[newValue.intValue()];
            boolean lbPickUp = SalesQoutationStatic.DeliveryType.PICK_UP.equals(fbDeliveryType);

            lblDeliverTo.setText(lbPickUp ? "Branch" : "Address");
            tfDeliveryAddress.setPromptText(lbPickUp ? "Press F3: Search" : null);
            tfDeliveryAddress.clear();

            oSalesController.SalesQoutation().Version().Master().setDeliveryType(fbDeliveryType);
            if (lbPickUp) {
                oSalesController.SalesQoutation().Version().Master().setDeliverTo("");
            } else {
                oSalesController.SalesQoutation().Version().Master().setBranchCode("");
            }
        });

        cmbPaymentForm.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.intValue() < 0) return;

            fbPaymentType = SalesQoutationStatic.PAYMENT_TYPE_CODE[newValue.intValue()];
            boolean lbTerm = SalesQoutationStatic.PaymentType.TERM.equals(fbPaymentType);

            tfPaymentTerms.setPromptText(lbTerm ? "Press F3: Search" : null);
            tfPaymentTerms.setEditable(lbTerm);
            // fixed: was setParentId(...), which overwrites the quotation link
            oSalesController.SalesQoutation().Version().Master().setPaymentForm(fbPaymentType);
            if (!lbTerm) {
                oSalesController.SalesQoutation().Version().Master().setTermId("");
            }
        });

        cmbInsurance.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.intValue() < 0) return;

            fbInsurance = SalesQoutationStatic.INSURANCE_CODE[newValue.intValue()];
            boolean isYes = SalesQoutationStatic.InsuranceType.YES.equals(fbInsurance);
            tfMCItemInsuranceAmt.setEditable(isYes);
        });

        cmbRegistration.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.intValue() < 0) return;

            fbRegistration = SalesQoutationStatic.REGISTRATION_CODE[newValue.intValue()];
            boolean isYes = SalesQoutationStatic.Registration.YES.equals(fbRegistration);
            tfMCItemRegisAmt.setEditable(isYes);
        });

        cmbPaymentVAT.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.intValue() < 0) return;

            fbVatType = SalesQoutationVersionStatic.VAT_TYPE_CODE[newValue.intValue()];
            loadTableMCItem();
        });
    }


    private void initMCItemFields(){
        if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
            String lsStockID = oSalesController.SalesQoutation().Version().Detail(pnMCRow).getStockId();
            String lsPromoCode = oSalesController.SalesQoutation().Version().Detail(pnMCRow).getPromoCode();

            boolean lbDisable = lsStockID == null || lsStockID.isEmpty()
                    || (lsPromoCode != null && !lsPromoCode.isEmpty());

            tfMCItemPromo.setDisable(lbDisable);
            tfMCItemRegisAmt.setDisable(lbDisable);
            tfMCItemInsuranceAmt.setDisable(lbDisable);
            tfMCItemDiscount.setDisable(lbDisable);
            tfMCItemAddDiscount.setDisable(lbDisable);
            tfMCItemFreight.setDisable(lbDisable);
            cmbInsurance.setDisable(lbDisable);
            cmbRegistration.setDisable(lbDisable);
        }
    }
    // =========================================================================
    // Load Record
    // =========================================================================
    private void LoadRecord() {
        try {
            tfTransNo.setText(oSalesController.SalesQoutation().getModel().getTransactionNo());
            dpTransDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(oSalesController.SalesQoutation().getModel().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            String inqStat = (oSalesController.SalesQoutation().getModel().getTransactionStatus());

            switch (inqStat) {
                case SalesQoutationStatic.OPEN:
                    lblStatus.setText(SalesQoutationStatic.STATUS_DESCRIPTION.OPEN);
                    break;
                case SalesQoutationStatic.SALES:
                    lblStatus.setText(SalesQoutationStatic.STATUS_DESCRIPTION.SALES);
                    break;
                case SalesQoutationStatic.LOST:
                    lblStatus.setText(SalesQoutationStatic.STATUS_DESCRIPTION.LOST);
                    break;
                case SalesQoutationStatic.VOID:
                    lblStatus.setText(SalesQoutationStatic.STATUS_DESCRIPTION.VOID);
                    break;
                default:
                    lblStatus.setText("");
                    break;
            }
            String VersionStatus = (oSalesController.SalesQoutation().Version().Master().getTransactionStatus());
            switch (VersionStatus) {
                case SalesQoutationVersionStatic.OPEN:
                    lblStatusVersion.setText(SalesQoutationVersionStatic.STATUS_DESCRIPTION.OPEN);
                    break;
                case SalesQoutationVersionStatic.CONFIRMED:
                    lblStatusVersion.setText(SalesQoutationVersionStatic.STATUS_DESCRIPTION.CONFIRMED);
                    break;
                case SalesQoutationVersionStatic.SALES:
                    lblStatusVersion.setText(SalesQoutationVersionStatic.STATUS_DESCRIPTION.SALES);
                    break;
                case SalesQoutationVersionStatic.REJECTED:
                    lblStatusVersion.setText(SalesQoutationVersionStatic.STATUS_DESCRIPTION.REJECTED);
                    break;
                case SalesQoutationVersionStatic.VOID:
                    lblStatusVersion.setText(SalesQoutationVersionStatic.STATUS_DESCRIPTION.VOID);
                    break;
                case SalesQoutationVersionStatic.SUPERCEDED:
                    lblStatusVersion.setText(SalesQoutationVersionStatic.STATUS_DESCRIPTION.SUPERCEDED);
                    break;
                case SalesQoutationVersionStatic.EXPIRED:
                    lblStatusVersion.setText(SalesQoutationVersionStatic.STATUS_DESCRIPTION.EXPIRED);
                    break;
                default:
                    lblStatusVersion.setText("");
                    break;
            }
            tfCustomerName.setText(oSalesController.SalesQoutation().getModel().Client().getCompanyName());
            tfAddress.setText(oSalesController.SalesQoutation().getModel().ClientAddress().getAddress());
            tfContactNo.setText(oSalesController.SalesQoutation().getModel().ClientMobile().getMobileNo());

            tfVersionTransNo.setText(oSalesController.SalesQoutation().Version().Master().getTransactionNo());
            dpQoutationDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(oSalesController.SalesQoutation().Version().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));

            tfVersion.setText(String.valueOf(oSalesController.SalesQoutation().getModel().getVersion()));
            tfQoutationTitle.setText(oSalesController.SalesQoutation().Version().Master().getTitleName());
            dpQoutationExpectedDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(oSalesController.SalesQoutation().Version().Master().getExpectedDate(), SQLUtil.FORMAT_SHORT_DATE)));
            dpQoutationValidity.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(oSalesController.SalesQoutation().Version().Master().getValidThruDate(), SQLUtil.FORMAT_SHORT_DATE)));

            taReason.setText(oSalesController.SalesQoutation().Version().Master().getReasons());

            String lsType = oSalesController.SalesQoutation().Version().Master().getDeliveryType();
            if (lsType == null) lsType = "";
            int lnIdx = Arrays.asList(SalesQoutationStatic.DELIVERY_TYPE_CODE).indexOf(lsType);
            cmbDeliveryMethod.getSelectionModel().select(lnIdx);

            switch (lsType) {
                case SalesQoutationStatic.DeliveryType.PICK_UP:
                    tfDeliveryAddress.setText(oSalesController.SalesQoutation().Version().Master().Branch().getBranchName());
                    break;
                case SalesQoutationStatic.DeliveryType.DELIVERY:
                    tfDeliveryAddress.setText(oSalesController.SalesQoutation().Version().Master().getDeliverTo());
                    break;
                default:
                    tfDeliveryAddress.setText("");
                    break;
            }

            tfDeliveryRemarks.setText(oSalesController.SalesQoutation().Version().Master().getRemarks());

            String lsPayForm = oSalesController.SalesQoutation().Version().Master().getPaymentForm();
            int lnPayForm = Arrays.asList(SalesQoutationStatic.PAYMENT_TYPE_CODE).indexOf(lsPayForm);
            cmbPaymentForm.getSelectionModel().select(lnPayForm);
            tfPaymentTerms.setText(oSalesController.SalesQoutation().Version().Master().Terms().getDescription());
            tfPaymentRemarks.setText(oSalesController.SalesQoutation().Version().Master().getRemarks1());
            taAdditionalRemarks.setText(oSalesController.SalesQoutation().Version().Master().getRemarks2());
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpQoutationDate, dpQoutationExpectedDate, dpQoutationValidity, dpTransDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpQoutationDate, dpQoutationExpectedDate, dpQoutationValidity);
    }

    private void datepicker_Action(ActionEvent event) {
        DatePicker source = (DatePicker) event.getSource();
        LocalDate loSelected = source.getValue();
        if (loSelected == null) return;

        try {
            switch (source.getId()) {
                case "dpQoutationDate":
                    poJSON = oSalesController.SalesQoutation().Version().Master()
                            .setTransactionDate(java.sql.Date.valueOf(loSelected));
                    break;

                case "dpQoutationExpectedDate":
                    if (dpQoutationDate.getValue() != null && loSelected.isBefore(dpQoutationDate.getValue())) {
                        ShowMessageFX.Warning("Expected date cannot be earlier than the quotation date.", pxeModuleName, null);
                        source.setValue(dpQoutationDate.getValue());
                        return;
                    }
                    poJSON = oSalesController.SalesQoutation().Version().Master()
                            .setExpectedDate(java.sql.Date.valueOf(loSelected));
                    break;

                case "dpQoutationValidity":
                    if (dpQoutationDate.getValue() != null && loSelected.isBefore(dpQoutationDate.getValue())) {
                        ShowMessageFX.Warning("Validity date cannot be earlier than the quotation date.", pxeModuleName, null);
                        source.setValue(dpQoutationDate.getValue());
                        return;
                    }
                    poJSON = oSalesController.SalesQoutation().Version().Master()
                            .setValidThruDate(java.sql.Date.valueOf(loSelected));
                    break;

                default:
                    return;
            }

            if (poJSON != null && "error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
            }
        } catch (Exception ex) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // =========================================================================
    // Table Setup Methods
    // =========================================================================
    private void initTables() {
        initTableMCItems();
        initTableGawayItems();
    }

    private void initTableMCItems() {
        JFXUtil.setColumnCenter(tblRowMCitemNo, tblRowMCitemBrand, tblRowMCitemModel, tblRowMCitemDesc, tblRowMCitemColor, tblRowMCitemVariant);
        JFXUtil.setColumnRight(tblRowMCitemSRP, tblRowMCitemDiscount, tblRowMCitemAddDiscount, tblRowMCitemFreight, tblRowMCitemReg, tblRowMCitemInsurance, tblRowMCitemQty, tblRowMCitemTotal, tblRowMCitemAction);
        JFXUtil.setColumnsIndexAndDisableReordering(tblMCItem);
        initActionColumnMCItem();   // after setColumnCenter so it isn't overwritten

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblMCItem.setItems(filteredMain_Data);
    }

    private void initActionColumnMCItem() {
        tblRowMCitemAction.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(""));
        tblRowMCitemAction.setCellFactory(col -> new TableCell<ModelTableMain, String>() {
            private final Button btnRemove = new Button();
            private final Tooltip tooltip = new Tooltip();

            {
                FontAwesomeIconView loIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                loIcon.setGlyphSize(14);
                loIcon.setFill(javafx.scene.paint.Color.WHITE);

                btnRemove.setGraphic(loIcon);
                btnRemove.setStyle("-fx-background-color: #d9534f; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
                tooltip.setShowDelay(Duration.millis(200));
                btnRemove.setTooltip(tooltip);

                btnRemove.setOnAction(e -> {
                    int lnRow = getIndex();
                    if (lnRow >= 0 && lnRow < getTableView().getItems().size()) {
                        removeMCItemRow(lnRow);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                setText(null);

                boolean lbEditing = pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE;
                if (empty || !lbEditing || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                ModelTableMain loRowData = getTableView().getItems().get(getIndex());
                boolean lbBlank = loRowData == null || loRowData.getIndex03().isEmpty();
                if (lbBlank) {
                    setGraphic(null);
                    return;
                }

                // tooltip: "Remove" + the item description
                tooltip.setText("Remove\n" + loRowData.getIndex04());
                setGraphic(btnRemove);
            }
        });
    }

    private void removeMCItemRow(int fnRow) {
        if (!ShowMessageFX.YesNo("Remove this item from the quotation?", pxeModuleName, null)) {
            return;
        }
        try {
            SalesQoutationVersion loVersion = oSalesController.SalesQoutation().Version();
            if (fnRow < 0 || fnRow >= loVersion.getDetailCount()) return;

            // blank the row; ReloadDetail() in loadTableMCItem() drops empty rows
            loVersion.Detail(fnRow).setStockId(null);

            pnMCRow = -1;
            clearMCItemTextFields();

            loadTableMCItem();
        } catch (Exception ex) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    private void initTableGawayItems() {
        JFXUtil.setColumnCenter(tblRowGawayNo, tblRowGawayItem, tblRowGawayItemCode, tblRowGawayDesc, tblRowGawayQty, tblRowGawayRemarks, tblRowGawayAction);
        JFXUtil.setColumnsIndexAndDisableReordering(tblGawayItem);
        initActionColumnGawayItem();   // after setColumnCenter so it isn't overwritten

        filteredDetail_Data = new FilteredList<>(detail_data, b -> true);
        tblGawayItem.setItems(filteredDetail_Data);
    }

    private void initActionColumnGawayItem() {
        tblRowGawayAction.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(""));
        tblRowGawayAction.setCellFactory(col -> new TableCell<ModelTableDetail, String>() {
            private final Button btnRemove = new Button();
            private final Tooltip tooltip = new Tooltip();

            {
                FontAwesomeIconView loIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                loIcon.setGlyphSize(14);
                loIcon.setFill(javafx.scene.paint.Color.WHITE);

                btnRemove.setGraphic(loIcon);
                btnRemove.setStyle("-fx-background-color: #d9534f; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
                tooltip.setShowDelay(Duration.millis(200));
                btnRemove.setTooltip(tooltip);

                btnRemove.setOnAction(e -> {
                    int lnRow = getIndex();
                    if (lnRow >= 0 && lnRow < getTableView().getItems().size()) {
                        removeGawayItemRow(lnRow);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                setText(null);

                boolean lbEditing = pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE;
                if (empty || !lbEditing || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                ModelTableDetail loRowData = getTableView().getItems().get(getIndex());
                // FIX: a genuinely blank placeholder row now has an empty
                // index02 (the Giveaways/Service category column) - checking
                // index03 (StockId/ItemCode) alone would wrongly treat every
                // Service row (which has no stock item) as blank and hide
                // its remove button.
                boolean lbBlank = loRowData == null || loRowData.getIndex02().isEmpty();
                if (lbBlank) {
                    setGraphic(null);
                    return;
                }

                // tooltip: "Remove" + the item description
                tooltip.setText("Remove\n" + loRowData.getIndex04());
                setGraphic(btnRemove);
            }
        });
    }

    /*
     * FIX: previously called loVersionGiveaway.Detail(fnRow) - that method
     * does not exist on SalesQoutationVersionGiveaways (it only exposes
     * Giveaway(int row)), and the whole row-removal approach was borrowed
     * from the MC item side without a matching "ReloadDetail() drops blank
     * rows" mechanism on this class. SalesQoutationVersionGiveaways instead
     * has a purpose-built removeGiveaway(int row) that removes the row from
     * the in-memory list outright (it is deleted from the DB on save via
     * saveGiveaways()'s own cleanup), so that is used directly here. This
     * also now reloads the GIVEAWAY table (loadTableGawayItem()) instead of
     * the MC item table.
     */
    private void removeGawayItemRow(int fnRow) {
        if (!ShowMessageFX.YesNo("Remove this item from the quotation?", pxeModuleName, null)) {
            return;
        }
        try {
            SalesQoutationVersionGiveaways loVersionGiveaway = oSalesController.SalesQoutation().Giveaways();
            if (fnRow < 0 || fnRow >= loVersionGiveaway.getGiveawayCount()) return;

            poJSON = loVersionGiveaway.removeGiveaway(fnRow);
            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                return;
            }

            pnGawayRow = -1;
            clearGawayItemTextFields();

            loadTableGawayItem();
        } catch (Exception ex) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    private void loadTableMCItem() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblMCItem.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        main_data.clear();
                        SalesQoutationVersion loVersion = oSalesController.SalesQoutation().Version();

                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            loVersion.ReloadDetail();

                            int lnLast = loVersion.getDetailCount() - 1;
                            boolean lbLastBlank = lnLast >= 0
                                    && (loVersion.Detail(lnLast).getStockId() == null
                                    || loVersion.Detail(lnLast).getStockId().isEmpty());
                            if (!lbLastBlank) {
                                poJSON = loVersion.AddDetail();
                            }
                        }

                        // 1. Initialize total variable
                        double grandTotal = 0.0;

                        for (int lnCtr = 0; lnCtr < loVersion.getDetailCount(); lnCtr++) {
                            Model_Sales_Quotation_Version_Detail loRow = loVersion.Detail(lnCtr);
                            boolean lbBlank = loRow.getStockId() == null || loRow.getStockId().isEmpty();

                            if (lbBlank) {
                                main_data.add(new ModelTableMain(String.valueOf(lnCtr + 1),
                                        "", "", "", "", "", "", "", "", "", "", "", "","", ""));
                                continue;
                            }

                            // 2. Extract unit values (converting safe String outputs or direct numeric getters to double)
                            double price = parseDouble(safe(() -> loRow.Inventory().getSellingPrice()));
                            double discount = parseDouble(safe(loRow::getDiscount));
                            double adddiscount = parseDouble(safe(loRow::getAdditionalDiscount));
                            double freight = parseDouble(safe(loRow::getFreight));
                            double reg = parseDouble(safe(loRow::getRegistrationAmount));
                            double ins = parseDouble(safe(loRow::getInsuranceAmount));

                            int qty = parseInt(safe(loRow::getQuantity));

                            double rowTotal = oSalesController.SalesQoutation().computeMCItemDetail(price,
                                    qty, discount, adddiscount, freight, reg, ins,String.valueOf(cmbPaymentVAT.getSelectionModel().getSelectedIndex()));
                            // 3. Compute row total: ((Price - Discount - Additional Discount) * Qty) + Charges
//                            double rowTotal = ((price - discount - adddiscount)  + freight + reg + ins)* qty;
//                            grandTotal += rowTotal;

                            main_data.add(new ModelTableMain(
                                    String.valueOf(lnCtr + 1),
                                    loRow.Inventory().Brand().getDescription(),
                                    loRow.Inventory().Model().getModelId(),
                                    loRow.Inventory().getDescription(),
                                    loRow.Inventory().Color().getDescription(),
                                    loRow.Inventory().Variant().getDescription(),
                                    safe(() -> loRow.Inventory().getSellingPrice()),
                                    safe(loRow::getDiscount),
                                    safe(loRow::getAdditionalDiscount),
                                    safe(loRow::getFreight),
                                    safe(loRow::getRegistrationAmount),
                                    safe(loRow::getInsuranceAmount),
                                    safe(loRow::getQuantity),
                                    String.format("%.2f", rowTotal) , // Position 13: Total
                                    ""
                            ));
                        }

                        // 4. Update your JavaFX UI component with the grand total
                        // lblGrandTotal.setText(String.format("%,.2f", grandTotal));

                        if (pnMCRow < 0 || pnMCRow >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                JFXUtil.selectAndFocusRow(tblMCItem, 0);
                                pnMCRow = tblMCItem.getSelectionModel().getSelectedIndex();
                            }
                        } else {
                            JFXUtil.selectAndFocusRow(tblMCItem, pnMCRow);
                        }

                        updateMCComboState();
                        initMCItemFields();
                        loadTransactionSummary();
                    } catch (Exception ex) {
                        Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                return null;
            }

            @Override
            protected void succeeded() {
                if (main_data.isEmpty()) tblMCItem.setPlaceholder(loading.placeholderLabel);
                else tblMCItem.toFront();
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (main_data.isEmpty()) tblMCItem.setPlaceholder(loading.placeholderLabel);
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();
    }

    // Helper methods to safely parse String results returned by safe()
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /*
     * NEW: this method did not exist before. The giveaway table's backing
     * list (detail_data) was never populated anywhere, so giveaways loaded
     * via SalesQoutation.openRecord()/newRecord() -> Giveaways() were never
     * shown on screen.
     *
     * Mirrors loadTableMCItem()'s structure, but SalesQoutationVersionGiveaways
     * has no ReloadDetail()/AddDetail() pair - only addGiveaway() and
     * removeGiveaway(row) - so the "keep exactly one blank trailing row while
     * editing" logic is implemented directly here instead of delegating to
     * the giveaways controller.
     *
     * Column mapping assumed (based on the Inventory()/Brand() accessor
     * already referenced - commented out - in the original source, and the
     * same getIndex03()/getIndex04() blank-check/tooltip convention used by
     * initActionColumnMCItem() above):
     *   index01 = row no.            index02 = Inventory().Brand().getDescription()
     *   index03 = getStockId()       index04 = Inventory().getDescription()
     *   index05 = getQuantity()      index06 = getRemarks()
     *   index07 = (action column placeholder, unused)
     *
     * UNVERIFIED: getRemarks()/setRemarks(String) on Model_Sales_Quotation_
     * Version_Giveaways - this file wasn't available to confirm the exact
     * getter/setter name. If it differs, update this method and the
     * "tfGawayRemarks" case in txtField_Focus below.
     */
    /**
     * Classifies a giveaway row for display/sorting purposes.
     * <p>
     * 0 = "Giveaways" - has an actual stock item (barcode + description).<br>
     * 1 = "Service"   - no stock item, but has remarks and/or a quantity
     *     entered directly (e.g. a free installation/service line).<br>
     * 2 = blank placeholder - neither an item nor service info; this is the
     *     trailing empty row kept open for new entry.
     * <p>
     * UNVERIFIED: assumes getRemarks() exists on
     * Model_Sales_Quotation_Version_Giveaways (not available to confirm).
     */
    private int gawayRowRank(Model_Sales_Quotation_Version_Giveaways row) {
        try {
            boolean lbHasStock = row.getStockId() != null && !row.getStockId().trim().isEmpty();
            String lsDesc = lbHasStock ? row.Inventory().getDescription() : null;
            boolean lbHasItem = lbHasStock && lsDesc != null && !lsDesc.trim().isEmpty();
            if (lbHasItem) return 0;

            String lsRemarks = row.getRemarks();
            Integer lnQty = row.getQuantity();
            boolean lbHasServiceInfo = (lsRemarks != null && !lsRemarks.trim().isEmpty())
                    || (lnQty != null && lnQty > 0);
            return lbHasServiceInfo ? 1 : 2;
        } catch (Exception e) {
            return 2;
        }
    }

    private void loadTableGawayItem() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblGawayItem.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        detail_data.clear();

                        SalesQoutationVersionGiveaways loGiveaways
                                = oSalesController.SalesQoutation().Giveaways();

                        /*
                         * Keep the currently selected giveaway object before sorting.
                         *
                         * pnGawayRow is only a display/list index. Once the actual
                         * backing list is sorted, that index may point to another row.
                         *
                         * Keeping the object reference allows us to find its new
                         * index after sorting.
                         */
                        Model_Sales_Quotation_Version_Giveaways loSelectedGiveaway = null;

                        if (pnGawayRow >= 0
                                && pnGawayRow < loGiveaways.getGiveawayCount()) {

                            loSelectedGiveaway
                                    = loGiveaways.Giveaway(pnGawayRow);
                        }

                        /*
                         * Add one blank row while adding/updating.
                         *
                         * Only add a blank row if the last existing row is not
                         * already a blank placeholder.
                         */
                        if (pnEditMode == EditMode.ADDNEW
                                || pnEditMode == EditMode.UPDATE) {

                            int lnLast = loGiveaways.getGiveawayCount() - 1;

                            boolean lbLastBlank = lnLast >= 0
                                    && gawayRowRank(loGiveaways.Giveaway(lnLast)) == 2;

                            if (!lbLastBlank) {
                                poJSON = loGiveaways.addGiveaway();
                            }
                        }

                        /*
                         * Sort the ACTUAL backing list.
                         *
                         * Rank:
                         *
                         * 0 = Giveaway
                         * 1 = Service
                         * 2 = Blank placeholder
                         *
                         * Sorting the actual list is important because the table
                         * row index is also used when accessing Giveaway(row).
                         */
                        loGiveaways.Giveaways().sort(
                                java.util.Comparator.comparingInt(
                                        SalesQoutationController.this::gawayRowRank
                                )
                        );

                        /*
                         * Find the selected giveaway's NEW index after sorting.
                         *
                         * Do not use the old pnGawayRow because sorting may have
                         * changed the position of the selected record.
                         */
                        if (loSelectedGiveaway != null) {
                            int lnSelectedIndex = -1;

                            for (int lnCtr = 0;
                                 lnCtr < loGiveaways.getGiveawayCount();
                                 lnCtr++) {

                                if (loGiveaways.Giveaway(lnCtr)
                                        == loSelectedGiveaway) {

                                    lnSelectedIndex = lnCtr;
                                    break;
                                }
                            }

                            if (lnSelectedIndex >= 0) {
                                pnGawayRow = lnSelectedIndex;
                            }
                        }

                        /*
                         * Populate the table backing list using the SAME order
                         * as the sorted Giveaways() list.
                         */
                        for (int lnCtr = 0;
                             lnCtr < loGiveaways.getGiveawayCount();
                             lnCtr++) {

                            Model_Sales_Quotation_Version_Giveaways loRow
                                    = loGiveaways.Giveaway(lnCtr);

                            int lnRank = gawayRowRank(loRow);

                            /*
                             * Blank trailing row.
                             */
                            if (lnRank == 2) {
                                detail_data.add(
                                        new ModelTableDetail(
                                                String.valueOf(lnCtr + 1),
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                ""
                                        )
                                );

                                continue;
                            }

                            boolean lbHasItem = lnRank == 0;
                            String lsRowType = lbHasItem
                                    ? "Giveaways"
                                    : "Service";

                            detail_data.add(
                                    new ModelTableDetail(
                                            String.valueOf(lnCtr + 1),
                                            lsRowType,
                                            lbHasItem
                                                    ? loRow.getStockId()
                                                    : "",
                                            lbHasItem
                                                    ? loRow.Inventory().getDescription()
                                                    : "",
                                            safe(loRow::getQuantity),
                                            safe(loRow::getRemarks),
                                            ""
                                    )
                            );
                        }

                        /*
                         * Restore the selected row after rebuilding the table.
                         *
                         * If the previously selected record still exists,
                         * select its new position.
                         *
                         * Otherwise, select the first available row.
                         */
                        if (pnGawayRow >= 0
                                && pnGawayRow < detail_data.size()) {

                            JFXUtil.selectAndFocusRow(
                                    tblGawayItem,
                                    pnGawayRow
                            );

                        } else if (!detail_data.isEmpty()) {

                            JFXUtil.selectAndFocusRow(
                                    tblGawayItem,
                                    0
                            );

                            pnGawayRow
                                    = tblGawayItem
                                    .getSelectionModel()
                                    .getSelectedIndex();
                        }

                    } catch (Exception ex) {
                        Logger.getLogger(
                                SalesQoutationController.class.getName()
                        ).log(
                                Level.SEVERE,
                                null,
                                ex
                        );
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (detail_data.isEmpty()) {
                    tblGawayItem.setPlaceholder(
                            loading.placeholderLabel
                    );
                } else {
                    tblGawayItem.toFront();
                }

                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (detail_data.isEmpty()) {
                    tblGawayItem.setPlaceholder(
                            loading.placeholderLabel
                    );
                }

                loading.progressIndicator.setVisible(false);
            }
        };

        new Thread(task).start();
    }

    private void setTblMCItem_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnMCRow = tblMCItem.getSelectionModel().getSelectedIndex();
            ModelTableMain selectedItem = (ModelTableMain) tblMCItem.getSelectionModel().getSelectedItem();
            clearMCItemTextFields();
            if (selectedItem != null) {
                if (pnMCRow >= 0) {
                    LoadMCItemRecord();
                    if (event.getClickCount() == 2) {
                        tfMCItemQuantity.requestFocus();
                    }
                }
            }
            updateMCComboState();
        }
    }

    /*
     * NEW: no equivalent existed before, so selecting a giveaway row never
     * loaded it into tfGawayBarrcode/tfGawayDescription/tfGawayQty/
     * tfGawayRemarks. Mirrors setTblMCItem_Clicked().
     */
    private void setTblGawayItem_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnGawayRow = tblGawayItem.getSelectionModel().getSelectedIndex();
            ModelTableDetail selectedItem = tblGawayItem.getSelectionModel().getSelectedItem();
            clearGawayItemTextFields();
            if (selectedItem != null) {
                if (pnGawayRow >= 0) {
                    LoadGawayItemRecord();
                    if (event.getClickCount() == 2) {
                        tfGawayQty.requestFocus();
                    }
                }
            }
        }
    }


    // =========================================================================
    // Button & Event Handlers
    // =========================================================================
    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
        btnAddClient.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnFollowUp.setOnAction(this::handleButtonAction);
        btnApproved.setOnAction(this::handleButtonAction);
        btnVoid.setOnAction(this::handleButtonAction);
        btnLost.setOnAction(this::handleButtonAction);
        btnCreateFrom.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                unloadForm appUnload = new unloadForm();
                switch (clickedButton.getId()) {
                    case "btnCreateFrom":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Create From is only available during view mode.");
                            return;
                        }
                        poJSON = oSalesController.SalesQoutation().createFromVersion();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        pnMCRow = -1;
                        pnGawayRow = -1;
                        initButton(pnEditMode);

                        LoadRecord();
                        loadTableMCItem();
                        loadTableGawayItem();
                        break;

                    case "btnLost":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Lost is only available during view mode.");
                            return;
                        }
                        poJSON = oSalesController.SalesQoutation().lostRecord("");
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                        poJSON = oSalesController.SalesQoutation().searchRecord(oSalesController.SalesQoutation().getModel().getTransactionNo(), true);
                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        initButton(pnEditMode);

                        LoadRecord();
                        loadTableMCItem();
                        loadTableGawayItem();
                        break;
                    case "btnVoid":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Void is only available during view mode.");
                            return;
                        }
                        poJSON = oSalesController.SalesQoutation().voidRecord("");
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                        poJSON = oSalesController.SalesQoutation().searchRecord(oSalesController.SalesQoutation().getModel().getTransactionNo(), true);
                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        initButton(pnEditMode);

                        LoadRecord();
                        loadTableMCItem();
                        loadTableGawayItem();
                        break;
                    case "btnApproved":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Approval is only available during view mode.");
                            return;
                        }
                        oSalesController.SalesQoutation().Version().setWithParent(false);
                        poJSON = oSalesController.SalesQoutation().Version().ConfirmTransaction("");
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
//                        poJSON = oSalesController.SalesQoutation().confirmRecord("");
//                        if ("error".equals((String) poJSON.get("result"))) {
//                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
//                            return;
//                        }

                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                        poJSON = oSalesController.SalesQoutation().searchRecord(oSalesController.SalesQoutation().getModel().getTransactionNo(), true);
                        clearTextFields();
                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        initButton(pnEditMode);

                        LoadRecord();
                        loadTableMCItem();
                        loadTableGawayItem();
                        break;
                    case "btnFollowUp":
                        // 1. Guard check: Only allow follow-up during view mode
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Follow up is only available during view mode.");
                            return;
                        }
                        openFollowUpModal();
                        break;
                    case "btnAddClient":
                        if (pnEditMode == EditMode.ADDNEW) {
                            poJSON = oSalesController.SalesQoutation().addClient();
                        } else {
                            ShowMessageFX.Warning(null, pxeModuleName, "Adding for new client must be during new entry of sales inquiry.");
                        }
                        break;
                    case "btnBrowse":
                        poJSON = oSalesController.SalesQoutation().searchRecord("", false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        pnMCRow = -1;
                        pnGawayRow = -1;
                        initButton(pnEditMode);

                        LoadRecord();
                        loadTableMCItem();
                        loadTableGawayItem();
                        break;
                    case "btnUpdate":
                        poJSON = oSalesController.SalesQoutation().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        initButton(pnEditMode);
                        loadTableMCItem();
                        loadTableGawayItem();
                        break;
                    case "btnClose":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", pxeModuleName, null)) {
                            appUnload.unloadForm(ChildAnchorPane, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearTextFields();
                        poJSON = oSalesController.SalesQoutation().newRecord();
                        oSalesController.SalesQoutation().getModel().setIndustryCode(psIndustryId);
                        oSalesController.SalesQoutation().getModel().setCategoryCode(psCategoryId);
                        oSalesController.SalesQoutation().Version().setBranchCode(oApp.getBranchCode());
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }

                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        initButton(pnEditMode);

                        LoadRecord();
                        loadTableMCItem();
                        loadTableGawayItem();
                        break;
                    case "btnSave":
                        poJSON = oSalesController.SalesQoutation().saveRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }

                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        initButton(pnEditMode);

                        LoadRecord();
                        btnNew.fire();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", pxeModuleName, null)) {
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearTextFields();
                        }
                        break;
                }
            } catch (Exception ex) {
                Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
                try {
                    if (oApp != null) {
                        oApp.rollbackTrans();
                    }
                } catch (SQLException ex1) {
                    Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    // =========================================================================
    // Input Listeners & Key Events
    // =========================================================================
    private void registerFocusListener(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {

            if (node instanceof TextField) {
                ((TextField) node).focusedProperty().addListener(txtField_Focus);
            } else if (node instanceof TextArea) {
                ((TextArea) node).focusedProperty().addListener(txtArea_Focus);
            } else if (node instanceof Parent) {
                registerFocusListener((Parent) node);
            }
        }
    }

    /*
     * FIX: tfGawayBarrcode and tfGawayDescription already had F3-search
     * cases written in txtField_KeyPressed()'s switch below, but neither
     * field was ever actually wired to that handler - so F3 (and the
     * generic Enter/Down/Up navigation in the same handler) silently did
     * nothing on either field. Registered here the same way tfMCItemBrand/
     * tfMCItemModel already are.
     */
    private void registerKeyEvents() {
        tfCustomerName.setOnKeyPressed(this::txtField_KeyPressed);
        tfPaymentTerms.setOnKeyPressed(this::txtField_KeyPressed);
        tfMCItemBrand.setOnKeyPressed(this::txtField_KeyPressed);
        tfMCItemModel.setOnKeyPressed(this::txtField_KeyPressed);
        tfDeliveryAddress.setOnKeyPressed(this::txtField_KeyPressed);
        tfGawayBarrcode.setOnKeyPressed(this::txtField_KeyPressed);
        tfGawayDescription.setOnKeyPressed(this::txtField_KeyPressed);
        tfMCItemPromo.setOnKeyPressed(this::txtField_KeyPressed);
    }

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                switch (lsID) {
                    case "tfCustomerName":
                        if (tfCustomerName.getText().trim().isEmpty()) {
                            tfCustomerName.clear();
                            tfAddress.clear();
                            tfContactNo.clear();
                        } else {
                            try {
                                if (oSalesController.SalesQoutation().getModel().Client() != null) {
                                    tfCustomerName.setText(oSalesController.SalesQoutation().getModel().Client().getCompanyName());
                                    tfAddress.setText(oSalesController.SalesQoutation().getModel().ClientAddress().getAddress() + ", "
                                            + oSalesController.SalesQoutation().getModel().ClientAddress().Barangay().getBarangayName() + ", "
                                            + oSalesController.SalesQoutation().getModel().ClientAddress().Town().getDescription() + ", "
                                            + oSalesController.SalesQoutation().getModel().ClientAddress().Town().Province().getDescription() + ", "
                                            + oSalesController.SalesQoutation().getModel().ClientAddress().Town().getZipCode());
                                    tfContactNo.setText(oSalesController.SalesQoutation().getModel().ClientMobile().getMobileNo());
                                }
                            } catch (SQLException | GuanzonException e) {
                                Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
                            }
                        }
                        break;
                    case "tfQoutationTitle":
                        poJSON = oSalesController.SalesQoutation().Version().Master().setTitleName(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            break;
                        }
                        break;
                    case "tfDeliveryRemarks":
                        poJSON = oSalesController.SalesQoutation().Version().Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break; // added: it used to fall through into tfMCItemBrand

                    case "tfMCItemBrand":
                    case "tfMCItemModel":
                        if (pnMCRow < 0) break;
                        try {
                            if (tfMCItemBrand.getText().trim().isEmpty() || tfMCItemModel.getText().trim().isEmpty()) {
                                tfMCItemBrand.clear();
                                tfMCItemModel.clear();
                                oSalesController.SalesQoutation().Version().Detail(pnMCRow).setStockId(null);
                                loadTableMCItem();
                            }
                        } catch (Exception e) {
                            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
                        }
                        break;

                    case "tfMCItemQuantity":
                        if (pnMCRow < 0) break;
                        try {
                            int lnQty = lsValue == null || lsValue.trim().isEmpty() ? 0 : Integer.parseInt(lsValue.trim());
                            poJSON = oSalesController.SalesQoutation().Version().Detail(pnMCRow).setQuantity(lnQty);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                break;
                            }
                            tfMCItemQuantity.setText(String.valueOf(oSalesController.SalesQoutation().Version().Detail(pnMCRow).getQuantity()));
                            loadTableMCItem();
                        } catch (NumberFormatException e) {
                            ShowMessageFX.Warning("Quantity must be a whole number.", pxeModuleName, null);
                        } catch (Exception e) {
                            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
                        }
                        break;

                    case "tfMCItemDiscount":
                    case "tfMCItemFreight":
                    case "tfMCItemRegisAmt":
                    case "tfMCItemInsuranceAmt":
                    case "tfMCItemAddDiscount":
                        if (pnMCRow < 0) break;
                        try {
                            double lnAmt = lsValue == null || lsValue.trim().isEmpty()
                                    ? 0.00 : Double.parseDouble(lsValue.replace(",", "").trim());
                            Model_Sales_Quotation_Version_Detail loDet
                                    = oSalesController.SalesQoutation().Version().Detail(pnMCRow);
                            String lsFormatted = CustomCommonUtil.setIntegerValueToDecimalFormat(lnAmt, false);

                            switch (lsID) {
                                case "tfMCItemDiscount":
                                    if (lnAmt > 100) {
                                        ShowMessageFX.Warning("Invalid Discount: Discount percentage cannot exceed 100%.",pxeModuleName, null);
                                        lnAmt = 100.00;
                                        lsFormatted = String.format("%.2f", lnAmt);
                                        tfMCItemDiscount.setText(lsFormatted);
                                        tfMCItemDiscount.requestFocus();
                                        tfMCItemRegisAmt.selectAll();
                                    }

                                    poJSON = loDet.setDiscount(lnAmt);
                                    tfMCItemDiscount.setText(lsFormatted);
                                    break;
                                case "tfMCItemFreight":
                                    poJSON = loDet.setFreight(lnAmt);
                                    tfMCItemFreight.setText(lsFormatted);
                                    break;
                                case "tfMCItemRegisAmt":
                                    poJSON = loDet.setRegistrationAmount(lnAmt);
                                    tfMCItemRegisAmt.setText(lsFormatted);
                                    break;
                                case "tfMCItemInsuranceAmt":
                                    poJSON = loDet.setInsuranceAmount(lnAmt);
                                    tfMCItemInsuranceAmt.setText(lsFormatted);
                                    break;
                                case "tfMCItemAddDiscount":
                                    poJSON = loDet.setAdditionalDiscount(lnAmt);
                                    tfMCItemAddDiscount.setText(lsFormatted);
                                    break;
                            }

                            if (poJSON != null && "error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                break;
                            }
                            loadTableMCItem();
                        } catch (NumberFormatException e) {
                            ShowMessageFX.Warning("Enter a valid amount.", pxeModuleName, null);
                        } catch (Exception e) {
                            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
                        }
                        break;

                    case "tfDeliveryAddress":
                        String lsDeliveryMethod = String.valueOf(cmbDeliveryMethod.getSelectionModel().getSelectedIndex());
                        if (!SalesQoutationStatic.DeliveryType.DELIVERY.equalsIgnoreCase(lsDeliveryMethod)) {
                            return;
                        }
                        poJSON = oSalesController.SalesQoutation().Version().Master().setDeliverTo(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        }
                        break;

                    case "tfPaymentRemarks":
                        poJSON = oSalesController.SalesQoutation().Version().Master().setRemarks1(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        }
                        break;

                    /*
                     * NEW: neither case previously existed, so typing a
                     * giveaway quantity or remark and tabbing away wrote
                     * nothing back to the model - the row looked edited in
                     * the UI but nothing was actually persisted. Mirrors the
                     * "tfMCItemQuantity" case above.
                     */
                    case "tfGawayQty":
                        if (pnGawayRow < 0) break;
                        try {
                            int lnGawayQty = lsValue == null || lsValue.trim().isEmpty() ? 0 : Integer.parseInt(lsValue.trim());
                            poJSON = oSalesController.SalesQoutation().Giveaways().Giveaway(pnGawayRow).setQuantity(lnGawayQty);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                break;
                            }
                            tfGawayQty.setText(String.valueOf(oSalesController.SalesQoutation().Giveaways().Giveaway(pnGawayRow).getQuantity()));
                            loadTableGawayItem();
                        } catch (NumberFormatException e) {
                            ShowMessageFX.Warning("Quantity must be a whole number.", pxeModuleName, null);
                        } catch (Exception e) {
                            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
                        }
                        break;

                    // UNVERIFIED: assumes setRemarks(String) exists on
                    // Model_Sales_Quotation_Version_Giveaways - adjust if the
                    // real method name differs.
                    case "tfGawayRemarks":
                        if (pnGawayRow < 0) break;
                        try {
                            poJSON = oSalesController.SalesQoutation().Giveaways().Giveaway(pnGawayRow).setRemarks(lsValue);
                            if (poJSON != null && "error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            }
                        } catch (Exception e) {
                            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
                        }
                        break;

                    default:
                        break;
                }
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {

                /* Lost Focus */
                switch (lsID) {
                    case "taReason":
                        poJSON = oSalesController.SalesQoutation().Version().Master().setReasons(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        taReason.setText(oSalesController.SalesQoutation().Version().Master().getReasons());
                        break;
                    case "taAdditionalRemarks":
                        poJSON = oSalesController.SalesQoutation().Version().Master().setRemarks2(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        taAdditionalRemarks.setText(oSalesController.SalesQoutation().Version().Master().getRemarks2());
                        break;
                }
            });

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case F3:
                        switch (lsID) {
                            case "tfCustomerName":
                                poJSON = oSalesController.SalesQoutation().SearchClient(lsValue, false);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfCustomerName.setText(oSalesController.SalesQoutation().getModel().Client().getCompanyName());
                                tfAddress.setText(oSalesController.SalesQoutation().getModel().ClientAddress().getAddress() + ", "
                                        + oSalesController.SalesQoutation().getModel().ClientAddress().Barangay().getBarangayName() + ", "
                                        + oSalesController.SalesQoutation().getModel().ClientAddress().Town().getDescription() + ", "
                                        + oSalesController.SalesQoutation().getModel().ClientAddress().Town().Province().getDescription() + ", "
                                        + oSalesController.SalesQoutation().getModel().ClientAddress().Town().getZipCode());
                                tfContactNo.setText(oSalesController.SalesQoutation().getModel().ClientMobile().getMobileNo());
                                break;

                            case "tfPaymentTerms":
                                poJSON = oSalesController.SalesQoutation().SearchTerm(lsValue, false);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfPaymentTerms.setText(oSalesController.SalesQoutation().Version().Master().Terms().getDescription());
                                break;

                            case "tfMCItemBrand":
                            case "tfMCItemModel":
                                int byCode = lsID.equals("tfMCItemBrand") ? 1 : 2;
                                poJSON = oSalesController.SalesQoutation().SearchDetailItem(lsValue, pnMCRow, SalesQoutationStatic.Category.MC_UNIT, byCode);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                Model_Sales_Quotation_Version_Detail loDet = oSalesController.SalesQoutation().Version().Detail(pnMCRow);
                                tfMCItemBrand.setText(loDet.Inventory().Brand().getDescription());
                                tfMCItemModel.setText(loDet.Inventory().Model().getDescription());
                                loadTableMCItem();
                                break;
                            case "tfMCItemPromo":
                                poJSON = oSalesController.SalesQoutation().SearchMCItemPromo(lsValue, pnMCRow, 1);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                Model_Sales_Quotation_Version_Detail loDetx = oSalesController.SalesQoutation().Version().Detail(pnMCRow);
                                tfMCItemPromo.setText(poJSON.get("PromoDesc").toString());
                                tfMCItemInsuranceAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(loDetx.getInsuranceAmount(), false));
                                tfMCItemRegisAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(loDetx.getRegistrationAmount(), false));
                                tfMCItemDiscount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(loDetx.getDiscount(), false));
                                tfMCItemAddDiscount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(loDetx.getAdditionalDiscount(), false));
                                tfMCItemFreight.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(loDetx.getFreight(), false));
                                loadTableMCItem();
                                break;
                            case "tfDeliveryAddress":
                                String lsDeliveryMethod = String.valueOf(cmbDeliveryMethod.getSelectionModel().getSelectedIndex());
                                if (!SalesQoutationStatic.DeliveryType.PICK_UP.equalsIgnoreCase(lsDeliveryMethod)) {
                                    return;
                                }
                                poJSON = oSalesController.SalesQoutation().SearchBranch(lsValue, false);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfDeliveryAddress.setText(oSalesController.SalesQoutation().Version().Master().Branch().getBranchName());
                                break;

                            /*
                             * FIX: previously called
                             * oSalesController.SalesQoutation().Giveaways(pnGawayRow)
                             * - Giveaways() takes no arguments (it returns
                             * the SalesQoutationVersionGiveaways controller
                             * itself); the row accessor is
                             * .Giveaways().Giveaway(pnGawayRow). The result
                             * was also being written into tfMCItemBrand /
                             * tfMCItemModel (copy-paste leftover from the MC
                             * item case above) instead of the actual
                             * giveaway fields, and it refreshed the MC item
                             * table instead of the giveaway table.
                             */
                            case "tfGawayBarrcode":
                            case "tfGawayDescription":
                                int GawaybyCode = lsID.equals("tfGawayBarrcode") ? 1 : 2;
                                poJSON = oSalesController.SalesQoutation().SearchGawayItem(lsValue, pnGawayRow, null, GawaybyCode);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                Model_Sales_Quotation_Version_Giveaways loDetGaway = oSalesController.SalesQoutation().Giveaways().Giveaway(pnGawayRow);
                                tfGawayBarrcode.setText(loDetGaway.Inventory().getBarCode());
                                tfGawayDescription.setText(loDetGaway.Inventory().getDescription());
                                loadTableGawayItem();
                                break;
                            default:
                                break;
                        }
                        break;
                    case ENTER:
                }
                switch (event.getCode()) {
                    case ENTER:
                        CommonUtils.SetNextFocus(txtField);
                    case DOWN:
                        CommonUtils.SetNextFocus(txtField);
                        break;
                    case UP:
                        CommonUtils.SetPreviousFocus(txtField);
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private String safe(java.util.concurrent.Callable<Object> fn) {
        try {
            Object o = fn.call();
            return o == null ? "" : String.valueOf(o);
        } catch (Exception e) {
            return "0.00";
        }
    }

    private boolean isMCRowBlank() {
        try {
            if (pnMCRow < 0 || pnMCRow >= oSalesController.SalesQoutation().Version().getDetailCount()) return true;
            String lsStock = oSalesController.SalesQoutation().Version().Detail(pnMCRow).getStockId();
            return lsStock == null || lsStock.isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    /** Registration/Insurance combos are editable only while editing and only on a row that has an item. */
    private void updateMCComboState() {
        boolean lbEditing = pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE;
        boolean lbLock = !lbEditing || isMCRowBlank();
        cmbRegistration.setDisable(lbLock);
        cmbInsurance.setDisable(lbLock);
    }

    public void clearTextFields() {
        dpQoutationDate.setValue(null);
        dpTransDate.setValue(null);
        dpQoutationValidity.setValue(null);
        dpQoutationExpectedDate.setValue(null);
        JFXUtil.clearTextFields(apSearchMaster, anchorQoutation, anchorVersion, anchorOthers, anchorDetails);
        tfVersion.clear();
        main_data.clear();
        detail_data.clear();
        pnMCRow = -1;
        pnGawayRow = -1;
        lblStatus.setText("UNKNOWN");
        lblStatusVersion.setText("UNKNOWN");
    }

    public void clearMCItemTextFields() {
        String lsZero = CustomCommonUtil.setIntegerValueToDecimalFormat(0.00, false);
        tfMCItemBrand.clear();
        tfMCItemModel.clear();
        tfMCItemDiscount.setText(lsZero);
        tfMCItemAddDiscount.setText(lsZero);
        tfMCItemFreight.setText(lsZero);
        tfMCItemRegisAmt.setText(lsZero);
        tfMCItemInsuranceAmt.setText(lsZero);
        tfMCItemQuantity.clear();
        tfMCItemPromo.clear();

        // the constants are codes, not combo indexes, so look the index up in the code arrays
        cmbRegistration.getSelectionModel().select(
                Arrays.asList(SalesQoutationStatic.REGISTRATION_CODE).indexOf(SalesQoutationStatic.Registration.EMPTY));
        cmbInsurance.getSelectionModel().select(
                Arrays.asList(SalesQoutationStatic.INSURANCE_CODE).indexOf(SalesQoutationStatic.InsuranceType.EMPTY));
        updateMCComboState();

    }
    public void clearGawayItemTextFields() {
        tfGawayBarrcode.clear();
        tfGawayDescription.clear();
        tfGawayRemarks.clear();
        tfGawayQty.clear();
    }

    public void LoadMCItemRecord() {
        if (pnMCRow < 0) return;
        try {
            Model_Sales_Quotation_Version_Detail loDet = oSalesController.SalesQoutation().Version().Detail(pnMCRow);
            tfMCItemBrand.setText(loDet.Inventory().Brand().getDescription());
            tfMCItemModel.setText(loDet.Inventory().Model().getDescription());
            tfMCItemDiscount.setText(safe(loDet::getDiscount));
            tfMCItemFreight.setText(safe(loDet::getFreight));
            tfMCItemRegisAmt.setText(safe(loDet::getRegistrationAmount));
            tfMCItemInsuranceAmt.setText(safe(loDet::getInsuranceAmount));
            tfMCItemQuantity.setText(safe(loDet::getQuantity));
            tfMCItemPromo.setText(loDet.getPromoCode());
            tfMCItemAddDiscount.setText(safe(loDet::getAdditionalDiscount));

            if (loDet.getRegistrationAmount() > 0) {
                cmbRegistration.getSelectionModel().select(
                        Arrays.asList(SalesQoutationStatic.REGISTRATION_CODE).indexOf(SalesQoutationStatic.Registration.YES));
            }
            if (loDet.getInsuranceAmount() > 0) {
                cmbInsurance.getSelectionModel().select(
                        Arrays.asList(SalesQoutationStatic.INSURANCE_CODE).indexOf(SalesQoutationStatic.InsuranceType.YES));
            }
        } catch (Exception e) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /*
     * NEW: no equivalent existed before. Mirrors LoadMCItemRecord().
     * UNVERIFIED: assumes getRemarks() exists on
     * Model_Sales_Quotation_Version_Giveaways.
     */
    public void LoadGawayItemRecord() {
        if (pnGawayRow < 0) return;
        try {
            Model_Sales_Quotation_Version_Giveaways loDet = oSalesController.SalesQoutation().Giveaways().Giveaway(pnGawayRow);
            tfGawayBarrcode.setText(loDet.getStockId());
            tfGawayDescription.setText(loDet.Inventory().getDescription());
            tfGawayQty.setText(safe(loDet::getQuantity));
            tfGawayRemarks.setText(safe(loDet::getRemarks));
        } catch (Exception e) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void openFollowUpModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ph/com/guanzongroup/integsys/views/SalesQoutationFollowUp.fxml"));
            Parent root = loader.load();
// Pass the data to the modal controller
            // Pass the data to the modal controller
            SalesQoutationFollowUpController modalController = loader.getController();
            modalController.setGRider(oApp);
            modalController.setSalesQuotation(oSalesController.SalesQoutation());
            modalController.loadForm();   // must be AFTER the setters

            Stage modalStage = new Stage();
            modalStage.setTitle("Sales Quotation Follow-Up");
            modalStage.initStyle(StageStyle.UNDECORATED);
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tblMCItem.getScene().getWindow());

            // 1. MAKE THE MAIN ANCHOR MOVABLE
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];

            root.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                modalStage.setX(event.getScreenX() - xOffset[0]);
                modalStage.setY(event.getScreenY() - yOffset[0]);
            });

            // 2. APPLY SHADOW TO THE MAIN ANCHOR
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0)); // 40% Opacity black shadow
            shadow.setRadius(15.0);
            shadow.setOffsetX(0.0);
            shadow.setOffsetY(5.0);
            root.setEffect(shadow);

            // 3. BOUNDARY FIX FOR UNDECORATED SHADOWS
            // To prevent the shadow from being clipped by the window edges,
            // the scene is made transparent.
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            modalStage.setScene(scene);
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (IOException e) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, "Failed to load Follow-Up Modal", e);
            ShowMessageFX.Error("Unable to open follow-up window.", pxeModuleName, null);
        }
    }

    private void loadTransactionSummary() {
        try {
            SalesQoutationVersion loVersion = oSalesController.SalesQoutation().Version();
            loVersion.computeMasterTotals();   // same computation that willSave() uses

            StringBuilder lsItems = new StringBuilder();
            int lnNo = 0;
            for (int lnCtr = 0; lnCtr < loVersion.getDetailCount(); lnCtr++) {
                Model_Sales_Quotation_Version_Detail loRow = loVersion.Detail(lnCtr);
                if (loRow.getStockId() == null || loRow.getStockId().isEmpty()) continue;   // blank row

                int lnQty        = parseInt(safe(loRow::getQuantity));
                double lnPrice   = parseDouble(safe(loRow::getUnitPrice));
                double lnDiscAmt = lnPrice * parseDouble(safe(loRow::getDiscount)) / 100.0;   // per unit, same as computeMasterTotals()
                double lnAddDisc = parseDouble(safe(loRow::getAdditionalDiscount));
                double lnFreight = parseDouble(safe(loRow::getFreight));
                double lnReg     = parseDouble(safe(loRow::getRegistrationAmount));
                double lnIns     = parseDouble(safe(loRow::getInsuranceAmount));

                // every line is per-unit x qty
                double lnSrpTotal   = lnPrice * lnQty;
                double lnDiscTotal  = lnDiscAmt * lnQty;
                double lnAddTotal   = lnAddDisc * lnQty;
                double lnFrghtTotal = lnFreight * lnQty;
                double lnRegTotal   = lnReg * lnQty;
                double lnInsTotal   = lnIns * lnQty;
                double lnAmount     = lnSrpTotal - lnDiscTotal - lnAddTotal + lnFrghtTotal + lnRegTotal + lnInsTotal;

                lnNo++;
                lsItems.append(lnNo).append("  ").append(loRow.Inventory().getDescription())
                        .append("  x").append(lnQty).append("\n");
                lsItems.append(summaryLine("SRP", String.format("%,.2f", lnSrpTotal)));
                if (lnDiscAmt > 0) lsItems.append(summaryLine("Discount", "-" + String.format("%,.2f", lnDiscTotal)));
                if (lnAddDisc > 0) lsItems.append(summaryLine("Add'l Discount", "-" + String.format("%,.2f", lnAddTotal)));
                if (lnFreight > 0) lsItems.append(summaryLine("Freight", String.format("%,.2f", lnFrghtTotal)));
                lsItems.append(summaryLine("Registration", lnReg > 0 ? String.format("%,.2f", lnRegTotal) : "Free"));
                lsItems.append(summaryLine("Insurance", lnIns > 0 ? String.format("%,.2f", lnInsTotal) : "Free"));
                lsItems.append(summaryLine("Amount", String.format("%,.2f", lnAmount)));
                lsItems.append("\n");
            }

            double lnVatSales    = parseDouble(safe(() -> loVersion.Master().getVatSales()));
            double lnNonVatSales = parseDouble(safe(() -> loVersion.Master().getNonVatSales()));
            double lnVat         = parseDouble(safe(() -> loVersion.Master().getVatAmount()));
            double lnTotal       = parseDouble(safe(() -> loVersion.Master().getTransactionTotal()));

            taSummaryItem.setText(lsItems.toString().trim());
            taSummaryItem.positionCaret(0);
            resizeSummaryItems();

            tfSummarySubTotal.setText(String.format("%,.2f", lnVatSales + lnNonVatSales));
            tfSummaryVat.setText(String.format("%,.2f", lnVat));
            tfSummaryVatEx.setText(String.format("%,.2f", lnNonVatSales));
            tfSummaryTotal.setText(String.format("%,.2f", lnTotal));
        } catch (Exception ex) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void clearTransactionSummary() {
        String lsZero = String.format("%,.2f", 0.00);
        taSummaryItem.clear();
        resizeSummaryItems();
        tfSummarySubTotal.setText(lsZero);
        tfSummaryVat.setText(lsZero);
        tfSummaryVatEx.setText(lsZero);
        tfSummaryTotal.setText(lsZero);
    }
    /** Sizes taSummaryItem to its text; the scrollbar only appears once the text is taller than SUMMARY_MAX_HEIGHT. */
    private void resizeSummaryItems() {
        String lsText = taSummaryItem.getText() == null ? "" : taSummaryItem.getText();

        javafx.scene.text.Text loMeasure = new javafx.scene.text.Text(lsText.isEmpty() ? " " : lsText);
        loMeasure.setFont(taSummaryItem.getFont());
        if (taSummaryItem.getWidth() > 0) loMeasure.setWrappingWidth(taSummaryItem.getWidth() - 30);   // minus scrollbar + padding

        double lnHeight = loMeasure.getLayoutBounds().getHeight() + 24;   // + TextArea padding/border
        lnHeight = Math.max(40, Math.min(lnHeight, SUMMARY_MAX_HEIGHT));

        taSummaryItem.setMinHeight(lnHeight);
        taSummaryItem.setPrefHeight(lnHeight);
    }

    /** "     Registration        2,500.00" - label left, amount right-aligned. */
    private String summaryLine(String fsLabel, String fsValue) {
        return String.format("     %-15s%14s\n", fsLabel, fsValue);
    }
}