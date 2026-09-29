package ph.com.guanzongroup.integsys.views;

import com.jfoenix.controls.JFXTimePicker;
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
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.CustomerInquiryFollowUpStatic;
import ph.com.guanzongroup.cas.sales.status.SalesInquiryStatic;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationStatic;
import ph.com.guanzongroup.integsys.model.ModelCustomerInquiryFollowUpAttachment;
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
    @FXML private TextField tfMCItemFreight1;


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
//        initDatePickers();
//        initAttachmentsGrid();
//        JFXUtil.setKeyEventFilter(tableKeyEvents, tblAttachments);
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
            AnchorInputs.setDisable(true);
            switch (fnValue) {
//                btnBrowse, btnNew, btnCreateFrom, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnLost, btnFollowUp, btnPrint, btnExport, btnClose;
                case EditMode.ADDNEW:
                    // When adding or updating, only show Save and Cancel
                    CustomCommonUtil.setVisible(true, btnSave, btnCancel, btnClose);
                    CustomCommonUtil.setManaged(true, btnSave, btnCancel, btnClose);
                    AnchorInputs.setDisable(false);
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
                    CustomCommonUtil.setVisible(true, btnNew, btnClose);
                    CustomCommonUtil.setManaged(true, btnNew, btnClose);
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void InitTextFields() {
        registerFocusListener(AnchorInputs);
        registerKeyEvents();

    }

    private void initComboBoxField() {

        cmbDeliveryMethod.setItems(SalesQoutationStatic.DELIVERY_TYPE_DESCRIPTION);
        cmbPaymentForm.setItems(SalesQoutationStatic.DELIVERY_TYPE_DESCRIPTION);
        cmbInsurance.setItems(SalesQoutationStatic.INSURANCE_DESCRIPTION);
        cmbRegistration.setItems(SalesQoutationStatic.REGISTRATION_DESCRIPTION);


        cmbDeliveryMethod.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    fbDeliveryType = SalesQoutationStatic.DELIVERY_TYPE_CODE[newValue.intValue()];
                    if (isFistFormLoad) {
                        isFistFormLoad = false;
                    } else {
//                        loadTableMaster();
                    }

                }
            }
        });

        cmbPaymentForm.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    fbPaymentType = SalesQoutationStatic.PAYMENT_TYPE_CODE[newValue.intValue()];
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
            tfDeliveryAddress.setText(oSalesController.SalesQoutation().Version().Master().getDeliverTo());
            tfDeliveryRemarks.setText(oSalesController.SalesQoutation().Version().Master().getRemarks());
            tfPaymentTerms.setText(oSalesController.SalesQoutation().Version().Master().Terms().getTermId());
            tfPaymentRemarks.setText(oSalesController.SalesQoutation().Version().Master().getRemarks1());
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }

    }


//    private void initDatePickers() {
//        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpQoutationDate,dpQoutationExpectedDate,dpQoutationValidity,dpTransDate);
//
//        LocalDate currentDate = LocalDate.now();
////        dpFilterDateFrom.setValue(currentDate.withDayOfMonth(1));
////        dpFilterDateThru.setValue(currentDate);
////
////        setDefaultFollowUpSchedule();
//
//        JFXUtil.setActionListener(this::datepicker_Action, dpQoutationDate,dpQoutationExpectedDate,dpQoutationValidity,dpTransDate);
//
//    }
//
//    private void setDefaultFollowUpSchedule() {
//        LocalDate currentDate = LocalDate.now();
//        oSalesController.CustomerInquiryFollowUp().getModel().setFollowUpDate(java.sql.Date.valueOf(currentDate));
//    }
//
//
//
////    private void datepicker_Action(ActionEvent event) {
////
////        DatePicker source = (DatePicker) event.getSource();
////
////        switch (source.getId()) {
////            case "dpSchedFollowUp":
////                if (dpTransDate.getValue() != null) {
////
////                    LocalDate selectedDate = dpSchedFollowUp.getValue();
////                    LocalDate today = LocalDate.now();
////
////                    long days = ChronoUnit.DAYS.between(today, selectedDate);
////
////                    String recordStatus = oSalesController.CustomerInquiryFollowUp()
////                            .getModel()
////                            .getRecordStatus();
////
////                    if (CustomerInquiryFollowUpStatic.FOLLOWED_UP.equals(recordStatus)) {
////
////                        if (days < 0 || days > 30) {
////                            ShowMessageFX.Warning(
////                                    "Follow-up date must be between today and 30 days from today.",
////                                    pxeModuleName,
////                                    null);
////
////                            dpSchedFollowUp.setValue(today);
////                            return;
////                        }
////
////                    } else if (CustomerInquiryFollowUpStatic.PENDING.equals(recordStatus)) {
////
////                        if (days < 0 || days > 270) {
////                            ShowMessageFX.Warning(
////                                    "Follow-up date must be between today and 270 days from today.",
////                                    pxeModuleName,
////                                    null);
////
////                            dpSchedFollowUp.setValue(today);
////                            return;
////                        }
////                    }
////
////                    oSalesController.CustomerInquiryFollowUp()
////                            .getModel()
////                            .setFollowUpDate(java.sql.Date.valueOf(selectedDate));
////                }
////                break;
////            default:
////                break;
////        }
////    }
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

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblMCItem.setItems(filteredMain_Data);
    }
    private void initTableGawayItems() {
        JFXUtil.setColumnCenter(tblRowGawayNo, tblRowGawayItem, tblRowGawayItemCode, tblRowGawayDesc, tblRowGawayQty, tblRowGawayRemarks, tblRowGawayAction);
        JFXUtil.setColumnsIndexAndDisableReordering(tblGawayItem);
        tblGawayItem.setItems(detail_data);
    }


    public void loadTableDetail() {
        if (isLoadingDetail) {
            return;
        }

        isLoadingDetail = true;

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        tblGawayItem.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        poJSON = new JSONObject();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ObservableList<ModelTableDetail> tempData
                            = FXCollections.observableArrayList();

                    if (data_details != null) {
                        data_details.clear();
                    }

                    poJSON = oSales.CustomerInquiryFollowUp().RetreiveCustomerInquiryFollowUps(
                            fbSourceNo,null);

                    if ("success".equals(poJSON.get("result"))) {

                        data_details = (JSONArray) poJSON.get("payload");

                        for (int i = 0; i < data_details.size(); i++) {
                            JSONObject obj = (JSONObject) data_details.get(i);

                            tempData.add(new ModelTableDetail(
                                    String.valueOf(i + 1),
                                    obj.get("sTransNox") == null ? "" : obj.get("sTransNox").toString(),
                                    obj.get("CustomerName") == null ? "" : obj.get("CustomerName").toString(),
                                    obj.get("sRspnseCd") == null ? "" : obj.get("sRspnseCd").toString(),
                                    obj.get("dFollowUp") == null ? "" : obj.get("dFollowUp").toString(),
                                    obj.get("SalesPerson") == null ? "" : obj.get("SalesPerson").toString(),
                                    obj.get("cTranStat") == null ? "" : obj.get("cTranStat").toString()
                            ));
                        }
                    }

                    Platform.runLater(() -> {

                        detail_data.setAll(tempData);

                        if (detail_data.isEmpty()) {
                            tblGawayItem.setPlaceholder(
                                    new Label("NO RECORD TO LOAD"));
                        }

                        tblGawayItem.setItems(detail_data);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                isLoadingDetail = false;
                progressIndicator.setVisible(false);
                if (detail_data.isEmpty()) {
                    tblGawayItem.setPlaceholder(
                            new Label("NO RECORD TO LOAD"));

                }
            }

            @Override
            protected void failed() {
                isLoadingDetail = false;

                progressIndicator.setVisible(false);

                if (getException() != null) {
                    getException().printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }
//
//
//    // =========================================================================
//    // Table Data Loading Methods
//    // =========================================================================
//    private void loadTableMaster() {
//        if (isLoadingMaster) {
//            return;
//        }
//
//        isLoadingMaster = true;
//
//        btnRetrieve.setDisable(true);
//
//        ProgressIndicator progressIndicator = new ProgressIndicator();
//        progressIndicator.setMaxHeight(50);
//        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
//        StackPane loadingPane = new StackPane(progressIndicator);
//        loadingPane.setAlignment(Pos.CENTER);
//
//        tblMaster.setPlaceholder(loadingPane);
//        progressIndicator.setVisible(true);
//
//        poJSON = new JSONObject();
//
//        Task<Void> task = new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                try {
//                    ObservableList<ModelTableMain> tempData
//                            = FXCollections.observableArrayList();
//
//                    if (data != null) {
//                        data.clear();
//                    }
//
//                    poJSON = oSalesController.CustomerInquiryFollowUp().RetreiveSource(
//                            fbInquiryType,
//                            fbSalesPersonID,
//                            fbCustomerID,
//                            dpFilterDateFrom.getValue(),
//                            dpFilterDateThru.getValue());
//
//                    if ("success".equals(poJSON.get("result"))) {
//
//                    data = (JSONArray) poJSON.get("payload");
//
//                        for (int i = 0; i < data.size(); i++) {
//                            JSONObject obj = (JSONObject) data.get(i);
//
//                                tempData.add(new ModelTableMain(
//                                        String.valueOf(i + 1),
//                                        obj.get("sTransNox") == null ? "" : obj.get("sTransNox").toString(),
//                                        obj.get("CustomerName") == null ? "" : obj.get("CustomerName").toString(),
//                                        obj.get("dFollowUp") == null
//                                                ? (obj.get("dTransact") == null ? "" : obj.get("dTransact").toString())
//                                                : obj.get("dFollowUp").toString(),
//                                        obj.get("SalesPerson") == null ? "" : obj.get("SalesPerson").toString(),
//                                        obj.get("cTranStat") == null ? "" : obj.get("cTranStat").toString()
//                                ));
//                        }
//                    }
//
//                    Platform.runLater(() -> {
//
//                        main_data.setAll(tempData);
//
//                        if (main_data.isEmpty()) {
//                            tblMaster.setPlaceholder(
//                                    new Label("NO RECORD TO LOAD"));
//                        }
//
//                        tblMaster.setItems(main_data);
//                    });
//
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void succeeded() {
//                isLoadingMaster = false;
//                progressIndicator.setVisible(false);
//                btnRetrieve.setDisable(false);
//                if (main_data.isEmpty()) {
//                    tblMaster.setPlaceholder(
//                            new Label("NO RECORD TO LOAD"));
//                    if(isFistFormLoad == false){
//                        ShowMessageFX.Warning(
//                                "NO RECORD TO LOAD.",
//                                pxeModuleName,
//                                null);
//                    }
//                    isFistFormLoad = false;
//                }
//                setupPagination();
//                showRetainedHighlight(true);
//            }
//
//            @Override
//            protected void failed() {
//                isLoadingMaster = false;
//
//                progressIndicator.setVisible(false);
//                btnRetrieve.setDisable(false);
//
//                if (getException() != null) {
//                    getException().printStackTrace();
//                }
//            }
//        };
//
//        new Thread(task).start();
//    }
//
//    public void loadTableDetail() {
//        if (isLoadingDetail) {
//            return;
//        }
//
//        isLoadingDetail = true;
//
//        ProgressIndicator progressIndicator = new ProgressIndicator();
//        progressIndicator.setMaxHeight(50);
//        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
//        StackPane loadingPane = new StackPane(progressIndicator);
//        loadingPane.setAlignment(Pos.CENTER);
//
//        tblDetail.setPlaceholder(loadingPane);
//        progressIndicator.setVisible(true);
//
//        poJSON = new JSONObject();
//
//        Task<Void> task = new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                try {
//                    ObservableList<ModelTableDetail> tempData
//                            = FXCollections.observableArrayList();
//
//                    if (data_details != null) {
//                        data_details.clear();
//                    }
//
//                    poJSON = oSalesController.CustomerInquiryFollowUp().RetreiveCustomerInquiryFollowUps(
//                            fbSourceNo,null);
//
//                    if ("success".equals(poJSON.get("result"))) {
//
//                        data_details = (JSONArray) poJSON.get("payload");
//
//                        for (int i = 0; i < data_details.size(); i++) {
//                            JSONObject obj = (JSONObject) data_details.get(i);
//
//                            tempData.add(new ModelTableDetail(
//                                    String.valueOf(i + 1),
//                                    obj.get("sTransNox") == null ? "" : obj.get("sTransNox").toString(),
//                                    obj.get("CustomerName") == null ? "" : obj.get("CustomerName").toString(),
//                                    obj.get("sRspnseCd") == null ? "" : obj.get("sRspnseCd").toString(),
//                                    obj.get("dFollowUp") == null ? "" : obj.get("dFollowUp").toString(),
//                                    obj.get("SalesPerson") == null ? "" : obj.get("SalesPerson").toString(),
//                                    obj.get("cTranStat") == null ? "" : obj.get("cTranStat").toString()
//                            ));
//                        }
//                    }
//
//                    Platform.runLater(() -> {
//
//                        detail_data.setAll(tempData);
//
//                        if (detail_data.isEmpty()) {
//                            tblDetail.setPlaceholder(
//                                    new Label("NO RECORD TO LOAD"));
//                        }
//
//                        tblDetail.setItems(detail_data);
//                    });
//
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void succeeded() {
//                isLoadingDetail = false;
//                progressIndicator.setVisible(false);
//                if (detail_data.isEmpty()) {
//                    tblDetail.setPlaceholder(
//                            new Label("NO RECORD TO LOAD"));
//
//                }
//                setupPagination();
//                showRetainedHighlight(true);
//            }
//
//            @Override
//            protected void failed() {
//                isLoadingDetail = false;
//
//                progressIndicator.setVisible(false);
//
//                if (getException() != null) {
//                    getException().printStackTrace();
//                }
//            }
//        };
//
//        new Thread(task).start();
//    }
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
                    case"btnAddClient":
                        if(pnEditMode == EditMode.ADDNEW){
                            poJSON = oSalesController.SalesQoutation().addClient();
                        } else {
                            ShowMessageFX.Warning(null, pxeModuleName, "Adding for new client must be during new entry of sales inquiry.");
                        }
                        break;
                    case "btnClose":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", pxeModuleName, null)) {
                            appUnload.unloadForm(ChildAnchorPane, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        poJSON = oSalesController.SalesQoutation().newRecord();
                        oSalesController.SalesQoutation().getModel().setIndustryCode(psIndustryId);
                        oSalesController.SalesQoutation().getModel().setCategoryCode(psCategoryId);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }

                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        initButton(pnEditMode);

                        LoadRecord();


                        break;
                    case "btnSave":
                        poJSON = oSalesController.SalesQoutation().saveRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }

                        pnEditMode = oSalesController.SalesQoutation().getEditMode();
                        initButton(pnEditMode);

                        LoadRecord();

                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
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
//    private void initAttachmentsGrid() {
//        /*FOCUS ON FIRST ROW*/
//        JFXUtil.setColumnCenter(tblRowNoAttachment);
//        JFXUtil.setColumnLeft(tblFileNameAttachment);
//        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
//        tblAttachments.setItems(attachment_data);
//
//        if (pnAttachment < 0 || pnAttachment >= attachment_data.size()) {
//            if (!attachment_data.isEmpty()) {
//                /* FOCUS ON FIRST ROW */
//                tblAttachments.getSelectionModel().select(0);
//                tblAttachments.getFocusModel().focus(0);
//                pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
//            }
//        } else {
//            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//            tblAttachments.getSelectionModel().select(pnAttachment);
//            tblAttachments.getFocusModel().focus(pnAttachment);
//        }
//
//    }
//    private void loadTableAttachment() {
//        // Setting data to table detail
//        ProgressIndicator progressIndicator = new ProgressIndicator();
//        progressIndicator.setMaxHeight(50);
//        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
//        StackPane loadingPane = new StackPane(progressIndicator);
//        loadingPane.setAlignment(Pos.CENTER);
//        tblAttachments.setPlaceholder(loadingPane);
//        progressIndicator.setVisible(true);
//
//        Label placeholderLabel = new Label("NO RECORD TO LOAD");
//        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed
//
//        Task<Void> task = new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                scaleFactor = 1.0;
//                JFXUtil.resetImageBounds(imageView, stackPane1);
//                Platform.runLater(() -> {
//                    try {
//                        attachment_data.clear();
//                        int lnCtr;
//                        int lnCount = 0;
//                        for (lnCtr = 0; lnCtr < oSalesController.CustomerInquiryFollowUp().getTransactionAttachmentCount(); lnCtr++) {
//                            if (RecordStatus.INACTIVE.equals(oSalesController.CustomerInquiryFollowUp().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
//                                continue;
//                            }
//                            lnCount += 1;
//                            attachment_data.add(
//                                    new ModelCustomerInquiryFollowUpAttachment(String.valueOf(lnCount),
//                                            String.valueOf(oSalesController.CustomerInquiryFollowUp().TransactionAttachmentList(lnCtr).getModel().getFileName()),
//                                            String.valueOf(lnCtr)
//                                    ));
//                        }
//                        int lnTempRow = JFXUtil.getDetailRow(attachment_data, pnAttachment, 3); //this method is used only when Reverse is applied
//                        if (lnTempRow < 0 || lnTempRow
//                                >= attachment_data.size()) {
//                            if (!attachment_data.isEmpty()) {
//                                /* FOCUS ON FIRST ROW */
//                                JFXUtil.selectAndFocusRow(tblAttachments, 0);
//                                int lnRow = Integer.parseInt(attachment_data.get(0).getIndex03());
//                                pnAttachment = lnRow;
//                                loadRecordAttachment(true);
//                            }
//                        } else {
//                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//                            JFXUtil.selectAndFocusRow(tblAttachments, lnTempRow);
//                            int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
//                            pnAttachment = lnRow;
//                            loadRecordAttachment(true);
//                        }
//                        if (attachment_data.size() <= 0) {
//                            loadRecordAttachment(false);
//                        }
//                    } catch (Exception e) {
//                    }
//                });
//
//                return null;
//            }
//
//            @Override
//            protected void succeeded() {
//                if (attachment_data == null || attachment_data.isEmpty()) {
//                    tblAttachments.setPlaceholder(placeholderLabel);
//                } else {
//                    tblAttachments.toFront();
//                }
//                progressIndicator.setVisible(false);
//
//            }
//
//            @Override
//            protected void failed() {
//                if (attachment_data == null || attachment_data.isEmpty()) {
//                    tblAttachments.setPlaceholder(placeholderLabel);
//                }
//                progressIndicator.setVisible(false);
//            }
//
//        };
//        new Thread(task).start(); // Run task in background
//
//    }
//    private void slideImage(int direction) {
//        if (attachment_data.size() <= 0) {
//            return;
//        }
//
//        currentIndex = pnAttachment;
//        int newIndex = currentIndex + direction;
//
//        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
//            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
//            slideOut.setByX(direction * -400); // Move left or right
//
//            tblAttachments.getFocusModel().focus(newIndex);
//            tblAttachments.getSelectionModel().select(newIndex);
//            pnAttachment = newIndex;
//            loadRecordAttachment(false);
//
//            // Create a transition animation
//            slideOut.setOnFinished(event -> {
//                imageView.setTranslateX(direction * 400);
//                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), imageView);
//                slideIn.setToX(0);
//                slideIn.play();
//
//                loadRecordAttachment(true);
//            });
//
//            slideOut.play();
//        }
//        if (isImageViewOutOfBounds(imageView, stackPane1)) {
//            resetImageBounds();
//        }
//    }
//
//    private boolean isImageViewOutOfBounds(ImageView imageView, StackPane stackPane) {
//        Bounds clipBounds = stackPane.getClip().getBoundsInParent();
//        Bounds imageBounds = imageView.getBoundsInParent();
//
//        return imageBounds.getMaxX() < clipBounds.getMinX()
//                || imageBounds.getMinX() > clipBounds.getMaxX()
//                || imageBounds.getMaxY() < clipBounds.getMinY()
//                || imageBounds.getMinY() > clipBounds.getMaxY();
//    }
//
//    private void resetImageBounds() {
//        imageView.setScaleX(1.0);
//        imageView.setScaleY(1.0);
//        imageView.setTranslateX(0);
//        imageView.setTranslateY(0);
//        stackPane1.setAlignment(imageView, Pos.CENTER);
//    }
//
//    private void stackPaneClip() {
//        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(
//                stackPane1.getWidth() - 8, // Subtract 10 for padding (5 on each side)
//                stackPane1.getHeight() - 8 // Subtract 10 for padding (5 on each side)
//        );
//        clip.setArcWidth(8); // Optional: Rounded corners for aesthetics
//        clip.setArcHeight(8);
//        clip.setLayoutX(4); // Set padding offset for X
//        clip.setLayoutY(4); // Set padding offset for Y
//        stackPane1.setClip(clip);
//
//    }
//
//
//    private void initTableOnClick() {
//
//            tblMaster.setOnMouseClicked(event -> {
//                pnMain = tblMaster.getSelectionModel().getSelectedIndex();
//                if (pnMain >= 0) {
//                    if (event.getClickCount() == 2) {
//                        if (pnEditMode == EditMode.UPDATE ) {
//                            boolean lbProceed = ShowMessageFX.YesNo(
//                                    "Loading another transaction will invalidate all current entry on the loaded transaction.\n\nDo you want to proceed?",
//                                    pxeModuleName,
//                                    "Confirm Action"
//                            );
//
//                            if (!lbProceed) {
//                                return; // Stop loading another transaction
//                            }
//
//                        }
//                        if(pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW){
//                            loadTableRecordFromMain();
//                        }
//
//
//                    }
//                }
//            });
//
//            tblMaster.setRowFactory(tv -> new TableRow<ModelTableMain>() {
//                        @Override
//                        protected void updateItem(ModelTableMain item, boolean empty) {
//                            super.updateItem(item, empty);
//                            if (item == null || empty) {
//                                setStyle("");
//                            } else {
//                                String key = item.getIndex01();
//                                if (highlightedRowsMain.containsKey(key)) {
//                                    List<String> colors = highlightedRowsMain.get(key);
//                                    if (!colors.isEmpty()) {
//                                        setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";"); // Apply latest color
//                                    }
//                                } else {
//                                    setStyle(""); // Default style
//                                }
//                            }
//                        }
//                    }
//            );
//
//            JFXUtil.adjustColumnForScrollbar(tblMaster);
//            tblAttachments.setOnMouseClicked(event -> {
//                pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
//                if (pnAttachment >= 0) {
//                    scaleFactor = 1.0;
//                    loadRecordAttachment(true);
//                    resetImageBounds();
//                }
//            });
//
//    }
//    private void loadTableRecordFromMain() {
//        poJSON = new JSONObject();
//        ModelTableMain selected = (ModelTableMain) tblMaster.getSelectionModel().getSelectedItem();
//        if (selected != null) {
//            try {
//                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
//                pnMain = pnRowMain;
//                System.out.println("index 1" +  selected.getIndex02() + "index 2" +  selected.getIndex01());
//                String lsTransactionNo = selected.getIndex02();
//                fbSourceNo = lsTransactionNo;
////                clearFields();
//                System.out.println("TO OPEN RECORD IS : " + lsTransactionNo);
//                poJSON = oSalesController.CustomerInquiryFollowUp().OpenSalesInquiry(lsTransactionNo);
//                if ("error".equals(poJSON.get("result"))) {
//                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                    return;
//                }
//                JFXUtil.disableAllHighlightByColor(tblMaster, "#A7C7E7", highlightedRowsMain);
//                JFXUtil.highlightByKey(tblMaster, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
//                pnEditMode = oSalesController.CustomerInquiryFollowUp().getEditMode();
//                LoadRecord();
//                loadTableDetail();
////                initFields(pnEditMode);
//                initButton(pnEditMode);
//            } catch (SQLException | GuanzonException | RuntimeException | CloneNotSupportedException ex) {
//                Logger.getLogger(CheckStatusUpdateController.class
//                        .getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
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
    }
//
    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                if (!pbLoaded) {
                    return;
                }

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
                                throw new RuntimeException(e);
                            }
                        }
                        break;
                    case "tfBrandComp":
                        poJSON = oSalesController.CustomerInquiryFollowUp().getModel().setCompetitorMake(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                    case "tfDealerComp":
                        poJSON = oSalesController.CustomerInquiryFollowUp().getModel().setCompetitorDealer(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                    case "tfFilterSalesperson":
                        if(lsValue == null || lsValue.isEmpty()){
//                            fbSalesPersonID = "";
//                            loadTableMaster();
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
//
//    // =========================================================================
//    // Helper / Utility Methods
//    // =========================================================================
//    private void ClearAllFields() {
//        clearControls(AnchorInputs);
//        clearControls(apDetail);
//        imageView.setImage(null);
//        tfAttachmentNo.clear();
//        detail_data.clear();
//        tblDetail.getItems().clear();
//        if (data_details != null) {
//            data_details.clear();
//        }
//    }
//
//    private void clearControls(Parent parent) {
//        for (Node node : parent.getChildrenUnmodifiable()) {
//
//            if (node instanceof TextField) {
//                ((TextField) node).clear();
//            } else if (node instanceof TextArea) {
//                ((TextArea) node).clear();
//            } else if (node instanceof DatePicker) {
//                ((DatePicker) node).setValue(null);
//            } else if (node instanceof ComboBox) {
//                ComboBox<?> combo = (ComboBox<?>) node;
//                if ("cmbFilterInquiryType".equals(combo.getId())) {
//                    continue;
//                }
//                combo.getSelectionModel().clearSelection();
//                combo.setValue(null);
//            } else if (node instanceof JFXTimePicker) {
//                ((JFXTimePicker) node).setValue(null);
//            } else if (node instanceof Parent) {
//                clearControls((Parent) node);
//            }
//        }
//
//    }
//
//    private void setupPagination() {
//
//        if (main_data == null || main_data.isEmpty()) {
//            pagination.setPageCount(0);
//            pagination.setPageFactory(null);
//
//            tblMaster.setItems(FXCollections.observableArrayList());
//            tblMaster.setPlaceholder(new Label("NO RECORD TO LOAD"));
//            return;
//        }
//
//        int pageCount = (int) Math.ceil(main_data.size() * 1.0 / ROWS_PER_PAGE);
//
//        pagination.setPageCount(pageCount);
//        pagination.setCurrentPageIndex(0);
//        pagination.setPageFactory(this::createPage);
//    }
//
//    private Node createPage(int pageIndex) {
//
//        if (main_data == null || main_data.isEmpty()) {
//            tblMaster.setItems(FXCollections.observableArrayList());
//            return new StackPane();
//        }
//
//        int fromIndex = pageIndex * ROWS_PER_PAGE;
//        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, main_data.size());
//
//        ObservableList<ModelTableMain> pageData
//                = FXCollections.observableArrayList(main_data.subList(fromIndex, toIndex));
//
//        tblMaster.setItems(pageData);
//        showRetainedHighlight(true);
//
//        return new StackPane(); // prevents layout re-render/flicker
//    }
//
//    private void showRetainedHighlight(boolean isRetained) {
//        if (isRetained) {
//            for (Pair<String, String> pair : plOrderNoPartial) {
//                if (!"0".equals(pair.getValue())) {
//
//                    plOrderNoFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
//                }
//            }
//        }
//        JFXUtil.disableAllHighlightByColor(tblMaster, "#C1E1C1", highlightedRowsMain);
//        plOrderNoPartial.clear();
//        for (Pair<String, String> pair : plOrderNoFinal) {
//            if (!"0".equals(pair.getValue())) {
//                JFXUtil.highlightByKey(tblMaster, pair.getKey(), "#C1E1C1", highlightedRowsMain);
//            }
//        }
//    }
//

}
