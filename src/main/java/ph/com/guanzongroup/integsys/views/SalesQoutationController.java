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
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
import ph.com.guanzongroup.integsys.model.ModelCustomerInquiryFollowUpAttachment;
import ph.com.guanzongroup.integsys.model.ModelSalesReservationDetailx;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

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
    private String fbRegistration = "";
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private volatile boolean isLoadingMaster = false;
    private volatile boolean isLoadingDetail = false;
    private int pnGawayRow = -1;
    private static final int GAWAY_EMPTY_ROWS = 1;
    private  int pnMCRow = -1;
    private static final int MC_ITEMS_EMPTY_ROWS = 1;
    // =========================================================================
    // Table Data
    // =========================================================================
    private JSONArray data;
    private JSONArray data_details;
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();

    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private FilteredList<ModelTableMain> filteredMain_Data;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
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

    // =========================================================================
// Delivery Information Controls
// =========================================================================
    @FXML private ComboBox cmbDeliveryMethod;
    @FXML private TextField tfDeliveryAddress;
    @FXML private TextField tfDeliveryRemarks;
    @FXML private  Label lblDeliverTo;


    // =========================================================================
// Payment Information Controls
// =========================================================================
    @FXML private ComboBox<?> tfPaymentVAT;
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
//        initTableOnClick();
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

        Platform.runLater((
        ) -> btnNew.fire());
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
//                btnBrowse, btnNew, btnCreateFrom, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnLost, btnFollowUp, btnPrint, btnExport, btnClose;
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
                            CustomCommonUtil.setVisible(true, btnBrowse,  btnPrint, btnExport, btnClose);
                            CustomCommonUtil.setManaged(true, btnBrowse,  btnPrint, btnExport, btnClose);
                            break;

                    }
                case EditMode.UPDATE:
                    CustomCommonUtil.setVisible(true, btnSave, btnCancel, btnClose);
                    CustomCommonUtil.setManaged(true, btnSave, btnCancel, btnClose);
                    break;
                case EditMode.UNKNOWN:
                default:
                    // Default fallback: show only Browse and Close
                    CustomCommonUtil.setVisible(true,btnBrowse, btnNew, btnClose);
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

        // register explicitly in case the recursive walk misses nested/skinned containers
        TextField[] laMCFields = {tfMCItemBrand, tfMCItemModel, tfMCItemQuantity,
                tfMCItemDiscount, tfMCItemFreight, tfMCItemRegisAmt, tfMCItemInsuranceAmt,tfVersion,tfTransNo,tfVersionTransNo};
        for (TextField tf : laMCFields) {
            tf.focusedProperty().removeListener(txtField_Focus);
            tf.focusedProperty().addListener(txtField_Focus);
        }

        tblMCItem.setOnMouseClicked(this::setTblMCItem_Clicked);
    }

    private void initComboBoxField() {

        cmbDeliveryMethod.setItems(SalesQoutationStatic.DELIVERY_TYPE_DESCRIPTION);
        cmbPaymentForm.setItems(SalesQoutationStatic.PAYMENT_TYPE_DESCRIPTION);
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
            oSalesController.SalesQoutation().Version().Master().setParentId(fbPaymentType);
            if (!lbTerm) {
                oSalesController.SalesQoutation().Version().Master().setTermId("");
            }
        });



        tfPaymentVAT.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    fbPaymentType = SalesQoutationStatic.PAYMENT_TYPE_CODE[newValue.intValue()];

                    switch (fbPaymentType) {
                        case SalesQoutationStatic.PaymentType.TERM:
                            tfPaymentTerms.setPromptText("Press F3: Search");
                            tfPaymentTerms.setEditable(true);
                            break;
                        case SalesQoutationStatic.PaymentType.CASH:
                        case SalesQoutationStatic.PaymentType.CASH_BALANCE:
                        case SalesQoutationStatic.PaymentType.EMPTY:
                            tfPaymentTerms.setPromptText(null);
                            tfPaymentTerms.setEditable(false);
                            oSalesController.SalesQoutation().Version().Master().setTermId("");
                            break;
                        default:
                            tfPaymentTerms.setPromptText(null);
                            tfPaymentTerms.setEditable(false);
                            oSalesController.SalesQoutation().Version().Master().setTermId("");
                            break;
                    }
                }
            }
        });

        cmbInsurance.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    fbInsurance = SalesQoutationStatic.INSURANCE_CODE[newValue.intValue()];
                }
            }
        });

        cmbRegistration.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    fbRegistration = SalesQoutationStatic.REGISTRATION_CODE[newValue.intValue()];
                }
            }
        });
    }

    // =========================================================================
    // Load ReCord
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
            int lnIdx = java.util.Arrays.asList(SalesQoutationStatic.DELIVERY_TYPE_CODE).indexOf(lsType);
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
            int lnPayForm = java.util.Arrays.asList(SalesQoutationStatic.PAYMENT_TYPE_CODE).indexOf(lsPayForm);
            cmbPaymentForm.getSelectionModel().select(lnPayForm);

            tfPaymentTerms.setText(oSalesController.SalesQoutation().Version().Master().Terms().getDescription());
            tfPaymentRemarks.setText(oSalesController.SalesQoutation().Version().Master().getRemarks1());
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
    private void initTables(){
        initTableMCItems();
        initTableGawayItems();
    }


    private void initTableMCItems() {
        JFXUtil.setColumnCenter(tblRowMCitemNo, tblRowMCitemBrand, tblRowMCitemModel, tblRowMCitemDesc, tblRowMCitemColor, tblRowMCitemVariant, tblRowMCitemSRP, tblRowMCitemDiscount, tblRowMCitemFreight, tblRowMCitemReg, tblRowMCitemInsurance, tblRowMCitemQty, tblRowMCitemTotal, tblRowMCitemAction);
        JFXUtil.setColumnsIndexAndDisableReordering(tblMCItem);
        initActionColumnMCItem();   // <- add, after setColumnCenter so it isn't overwritten

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

            // Option A (no remove method needed): blank the row, ReloadDetail() drops empty rows
            loVersion.Detail(fnRow).setStockId(null);
            // Option B (if you have one): loVersion.RemoveDetail(fnRow);

            pnMCRow = -1;
            tfMCItemBrand.clear();
            tfMCItemModel.clear();
            tfMCItemQuantity.clear();
            tfMCItemDiscount.clear();
            tfMCItemFreight.clear();
            tfMCItemRegisAmt.clear();
            tfMCItemInsuranceAmt.clear();

            loadTableMCItem();
        } catch (Exception ex) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }
    private void initTableGawayItems() {
        JFXUtil.setColumnCenter(tblRowGawayNo, tblRowGawayItem, tblRowGawayItemCode, tblRowGawayDesc, tblRowGawayQty, tblRowGawayRemarks, tblRowGawayAction);
        JFXUtil.setColumnsIndexAndDisableReordering(tblGawayItem);
        tblGawayItem.setItems(detail_data);
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

                            // add a blank row only if the last row isn't already blank
                            int lnLast = loVersion.getDetailCount() - 1;
                            boolean lbLastBlank = lnLast >= 0
                                    && (loVersion.Detail(lnLast).getStockId() == null
                                    || loVersion.Detail(lnLast).getStockId().isEmpty());
                            if (!lbLastBlank) {
                                poJSON = loVersion.AddDetail();
                            }
                        }

                        for (int lnCtr = 0; lnCtr < loVersion.getDetailCount(); lnCtr++) {
                            Model_Sales_Quotation_Version_Detail loRow = loVersion.Detail(lnCtr);
                            boolean lbBlank = loRow.getStockId() == null || loRow.getStockId().isEmpty();

                            if (lbBlank) {
                                main_data.add(new ModelTableMain(String.valueOf(lnCtr + 1),
                                        "", "", "", "", "", "", "", "", "", "", "", "", ""));
                                continue;
                            }

                            main_data.add(new ModelTableMain(
                                    String.valueOf(lnCtr + 1),
                                    loRow.Inventory().Brand().getDescription(),
                                    loRow.Inventory().Model().getModelId(),
                                    loRow.Inventory().getDescription(),
                                    loRow.Inventory().Color().getDescription(),
                                    loRow.Inventory().Variant().getDescription(),
                                    safe(() -> loRow.Inventory().getSellingPrice()),
                                    safe(loRow::getDiscount),
                                    safe(loRow::getFreight),
                                    safe(loRow::getRegistrationAmount),
                                    safe(loRow::getInsuranceAmount),
                                    safe(loRow::getQuantity),
                                    "",
                                    ""));
                        }

                        if (pnMCRow < 0 || pnMCRow >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                JFXUtil.selectAndFocusRow(tblMCItem, 0);
                                pnMCRow = tblMCItem.getSelectionModel().getSelectedIndex();
                            }
                        } else {
                            JFXUtil.selectAndFocusRow(tblMCItem, pnMCRow);
                        }
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

        }
    }
   // empty rows shown at the end while editing

    private void loadTableGaway() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblGawayItem.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        detail_data.clear();
                        SalesQoutationVersionGiveaways loGive = oSalesController.SalesQoutation().Giveaways();

                        // In add new / update mode: drop old trailing empty rows, then add fresh ones at the end
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            for (int lnRow = loGive.getGiveawayCount() - 1; lnRow >= 0; lnRow--) {
                                String lsStock = loGive.Giveaway(lnRow).getStockId();
                                if (lsStock == null || lsStock.isEmpty()) {
                                    loGive.removeGiveaway(lnRow);
                                } else {
                                    break;
                                }
                            }
                            for (int lnCtr = 0; lnCtr < GAWAY_EMPTY_ROWS; lnCtr++) {
                                poJSON = loGive.addGiveaway();
                                if ("error".equals((String) poJSON.get("result"))) break;
                            }
                        }

                        for (int lnCtr = 0; lnCtr < loGive.getGiveawayCount(); lnCtr++) {
                            Model_Sales_Quotation_Version_Giveaways loRow = loGive.Giveaway(lnCtr);
                            Integer lnQty = loRow.getQuantity();
                            boolean lbBlank = loRow.getStockId() == null || loRow.getStockId().isEmpty();

                            detail_data.add(new ModelTableDetail(
                                    String.valueOf(lnCtr + 1),
                                    "",
                                    lbBlank ? "" : loRow.getStockId(),
                                    "",   // description: fill in once the row model's lookup is known
                                    (lbBlank || lnQty == null) ? "" : String.valueOf(lnQty),
                                    "",
                                    ""));  // remarks: same
                        }

                        if (pnGawayRow < 0 || pnGawayRow >= detail_data.size()) {
                            if (!detail_data.isEmpty()) {
                                JFXUtil.selectAndFocusRow(tblGawayItem, 0);
                                pnGawayRow = tblGawayItem.getSelectionModel().getSelectedIndex();
                            }
                        } else {
                            JFXUtil.selectAndFocusRow(tblGawayItem, pnGawayRow);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                return null;
            }

            @Override
            protected void succeeded() {
                if (detail_data.isEmpty()) tblGawayItem.setPlaceholder(loading.placeholderLabel);
                else tblGawayItem.toFront();
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (detail_data.isEmpty()) tblGawayItem.setPlaceholder(loading.placeholderLabel);
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();
    }

//
//    // =========================================================================
//    // Button & Event Handlers
//    // =========================================================================
    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
        btnAddClient.setOnAction(this::handleButtonAction);
//        btnRetrieve.setOnAction(this::handleButtonAction);
//        btnAddAttachment.setOnAction(this::handleButtonAction);
//        btnRemoveAttachment.setOnAction(this::handleButtonAction);
//        btnArrowLeft.setOnAction(this::handleButtonAction);
//        btnArrowRight.setOnAction(this::handleButtonAction);
    }
//
    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                unloadForm appUnload = new unloadForm();
                switch (clickedButton.getId()) {
                    case "btnAddClient":
                        if (pnEditMode == EditMode.ADDNEW) {
                            poJSON = oSalesController.SalesQoutation().addClient();
                        } else {
                            ShowMessageFX.Warning(null, pxeModuleName, "Adding for new client must be during new entry of sales inquiry.");
                        }
                        break;
                        case "btnBrowse":
                            poJSON = oSalesController.SalesQoutation().searchRecord("",false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            pnEditMode = oSalesController.SalesQoutation().getEditMode();
                            initButton(pnEditMode);

                            LoadRecord();
                            loadTableMCItem();
                            loadTableGaway();

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
                        loadTableGaway();


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
            } catch ( Exception  ex) {
                Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
                try {
                    if (oApp != null) {

                        oApp.rollbackTrans(); // 🔥 force rollback
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

//
    private void registerKeyEvents() {
//        tfFilterCustName.setOnKeyPressed(this::txtField_KeyPressed);
//        tfFilterSalesperson.setOnKeyPressed(this::txtField_KeyPressed);
        tfCustomerName.setOnKeyPressed(this::txtField_KeyPressed);
        tfPaymentTerms.setOnKeyPressed(this::txtField_KeyPressed);
        tfMCItemBrand.setOnKeyPressed(this::txtField_KeyPressed);
        tfDeliveryAddress.setOnKeyPressed(this::txtField_KeyPressed);
    }
//
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
                    if (pnMCRow < 0) break;
                    try {
                        double lnAmt = lsValue == null || lsValue.trim().isEmpty()
                                ? 0.00 : Double.parseDouble(lsValue.replace(",", "").trim());
                        Model_Sales_Quotation_Version_Detail loDet
                                = oSalesController.SalesQoutation().Version().Detail(pnMCRow);
                        switch (lsID) {
                            case "tfMCItemDiscount":     loDet.setDiscount(lnAmt); break;
                            case "tfMCItemFreight":      loDet.setFreight(lnAmt); break;
                            case "tfMCItemRegisAmt":     loDet.setRegistrationAmount(lnAmt); break;
                            case "tfMCItemInsuranceAmt": loDet.setInsuranceAmount(lnAmt); break;
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
                        break;

                    case "taFollowUpMessage":
                        poJSON = oSalesController.CustomerInquiryFollowUp().getModel().setMessage(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                    case "taFollowUpRemarks":
                        poJSON = oSalesController.CustomerInquiryFollowUp().getModel().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                }
            });
//
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
                                poJSON = oSalesController.SalesQoutation().SearchMcItem(lsValue, pnMCRow, byCode);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                Model_Sales_Quotation_Version_Detail loDet = oSalesController.SalesQoutation().Version().Detail(pnMCRow);
                                tfMCItemBrand.setText(loDet.Inventory().Brand().getDescription());
                                tfMCItemModel.setText(loDet.Inventory().Model().getDescription());
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
    private String safe(java.util.concurrent.Callable<Object> fn) {
        try {
            Object o = fn.call();
            return o == null ? "" : String.valueOf(o);
        } catch (Exception e) {
            return "0.00";
        }
    }
    public void clearTextFields() {
        dpQoutationDate.setValue(null);
        dpTransDate.setValue(null);
        dpQoutationValidity.setValue(null);
        dpQoutationExpectedDate.setValue(null);
        JFXUtil.clearTextFields(apSearchMaster, anchorQoutation, anchorVersion, anchorOthers, anchorDetails);
        tfVersion.clear();
    }
    public void  clearMCItemTextFields() {
        tfMCItemBrand.clear();
        tfMCItemModel.clear();
        tfMCItemDiscount.clear();
        tfMCItemFreight.clear();
        tfMCItemRegisAmt.clear();
        tfMCItemInsuranceAmt.clear();
        tfMCItemQuantity.clear();
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
        } catch (Exception e) {
            Logger.getLogger(SalesQoutationController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}

