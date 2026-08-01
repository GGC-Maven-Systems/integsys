package ph.com.guanzongroup.integsys.views;

import com.jfoenix.controls.JFXTimePicker;
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
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.CustomerInquiryFollowUpStatic;
import ph.com.guanzongroup.cas.sales.t1.status.SalesInquiryStatic;
import ph.com.guanzongroup.integsys.model.ModelCustomerInquiryFollowUpAttachment;
import ph.com.guanzongroup.integsys.model.ModelPRFAttachment;
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

public class CustomerInquiryFollowUpController implements Initializable, ScreenInterface {

    // =========================================================================
    // Application Objects
    // =========================================================================
    private GRiderCAS oApp;
    private SalesControllers oSales;
    private JSONObject poJSON;
    private int pnMain = 0;
    private boolean isFistFormLoad = true;
    // =========================================================================
    // Controller Information
    // =========================================================================
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Customer Inquiry Follow-Up";

    // =========================================================================
    // Controller State
    // =========================================================================
    private int pnEditMode;
    private boolean pbLoaded = false;
    private volatile boolean isLoadingMaster = false;
    private volatile boolean isLoadingDetail = false;
    private Boolean isSearching = false;
    private int pnAttachment;
    private int currentIndex = 0;
    double ldstackPaneWidth = 0;
    double ldstackPaneHeight = 0;
    // =========================================================================
    // Search Filters
    // =========================================================================
    private String fbInquiryType = "";
    private String fbCustomerID = "";
    private String fbSalesPersonID = "";
    private String fbSourceNo = "";
    private String fbSourceCode = "";

    private FileChooser fileChooser;

    private double scaleFactor = 1.0;

    private double mouseAnchorX;
    private double mouseAnchorY;
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
    private ObservableList<ModelCustomerInquiryFollowUpAttachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelCustomerInquiryFollowUpAttachment.documentType;
    // =========================================================================
    // Root Containers
    // =========================================================================
    @FXML private AnchorPane ChildAnchorPane;
    @FXML private AnchorPane AnchorInputs,anchorEntries;
    @FXML private AnchorPane apDetail;
    @FXML private AnchorPane apAttachments;
    @FXML private AnchorPane apAttachmentButtons;
    @FXML private AnchorPane apSearchMaster;
    @FXML private StackPane stackPane1;
    @FXML private HBox hbButtons;

    // =========================================================================
    // Action Buttons
    // =========================================================================
    @FXML private Button btnBrowse;
    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnRetrieve;
    @FXML private Button btnClose;

    @FXML private Button btnAddAttachment;
    @FXML private Button btnRemoveAttachment;

    @FXML private Button btnArrowLeft;
    @FXML private Button btnArrowRight;

    // =========================================================================
    // Main Information Controls
    // =========================================================================
    @FXML private TextField tfTransactionNo;
    @FXML private DatePicker dpTransactionDate;
    @FXML private DatePicker dpInquiryDate;

    @FXML private TextField tfClientID;
    @FXML private TextField tfCustomerName;
    @FXML private TextField tfAddress;
    @FXML private TextField tfContactNo;

    @FXML private TextField tfSocMed;
    @FXML private TextField tfSocMedAcct;
    @FXML private TextField tfInquiryStat;

    @FXML private TextField tfBrand;
    @FXML private TextField tfModel;
    @FXML private TextField tfModelVariant;
    @FXML private TextField tfColor;

    @FXML private TextField tfInquiryRemarks;

    // =========================================================================
    // Follow-up Details Controls
    // =========================================================================
    @FXML private ComboBox cmbCommMethod;
    @FXML private ComboBox cmbSocMed;
    @FXML private ComboBox cmbCustResponse;
    @FXML private ComboBox cmbStatus;
    @FXML private ComboBox cmbComVhcleCond;

    @FXML private TextField tfFollowedUpBy;
    @FXML private DatePicker dpSchedFollowUp;
    @FXML private JFXTimePicker tpShedTime;

    @FXML private TextField tfBrandComp;
    @FXML private TextField tfDealerComp;

    @FXML private TextArea taFollowUpMessage;
    @FXML private TextArea taFollowUpRemarks;

    // =========================================================================
    // Follow-up History Table Controls
    // =========================================================================
    @FXML private TableView<ModelTableDetail> tblDetail;

    @FXML private TableColumn<ModelTableDetail, String> tblDRowNo;
    @FXML private TableColumn<ModelTableDetail, String> tblDTransNo;
    @FXML private TableColumn<ModelTableDetail, String> tblDCustName;
    @FXML private TableColumn<ModelTableDetail, String> tblDResponse;
    @FXML private TableColumn<ModelTableDetail, String> tblDDate;
    @FXML private TableColumn<ModelTableDetail, String> tblDFollowUpBy;
    @FXML private TableColumn<ModelTableDetail, String> tblDStatus;

    // =========================================================================
    // Master Table Controls
    // =========================================================================
    @FXML private TableView<ModelTableMain> tblMaster;

    @FXML private TableColumn<ModelTableMain, String> tblMRowNo;
    @FXML private TableColumn<ModelTableMain, String> tblMTransNo;
    @FXML private TableColumn<ModelTableMain, String> tblMCustName;
    @FXML private TableColumn<ModelTableMain, String> tblMDate;
    @FXML private TableColumn<ModelTableMain, String> tblMSalesPerson;
    @FXML private TableColumn<ModelTableMain, String> tblMStatus;

    // =========================================================================
    // Search / Filter Controls
    // =========================================================================
    @FXML private Pagination pagination;

    @FXML private ComboBox cmbFilterInquiryType;

    @FXML private TextField tfFilterCustName;
    @FXML private TextField tfFilterSalesperson;
    @FXML private TextField tfModel1221;
    @FXML private Label lblFilterSalesPerson;

    @FXML private DatePicker dpFilterDateFrom;
    @FXML private DatePicker dpFilterDateThru;

    // =========================================================================
    // Attachments Controls
    // =========================================================================
    @FXML private TextField tfAttachmentNo;
    @FXML private ComboBox cmbAttachmentType;

    @FXML private TableView tblAttachments;

    @FXML private TableColumn tblRowNoAttachment;
    @FXML private TableColumn tblFileNameAttachment;

    // =========================================================================
    // Image Viewer & Layout Controls
    // =========================================================================
    @FXML private ImageView imageView;
    @FXML private RowConstraints gridRow3;

    // =========================================================================
    // ScreenInterface Implementation
    // =========================================================================
    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
    }

    @Override
    public void setCompanyID(String fsValue) {
    }

    @Override
    public void setCategoryID(String fsValue) {
    }

    // =========================================================================
    // Initializable Implementation & Lifecycle Methods
    // =========================================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeObject();
        pnEditMode = oSales.CustomerInquiryFollowUp().getEditMode();
        initButton(pnEditMode);
        InitTextFields();
        initTables();
        initTableOnClick();
        ClickButton();
        initComboBoxField();
        initDatePickers();
        initAttachmentsGrid();
        initAttachmentPreviewPane();
        initStackPaneListener();
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblAttachments);
        pbLoaded = true;
        btnNew.fire();
    }

    private void initializeObject() {
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        oSales = new SalesControllers(oApp, logwrapr);
        oSales.CustomerInquiryFollowUp().setRecordStatus(CustomerInquiryFollowUpStatic.FOLLOWED_UP + CustomerInquiryFollowUpStatic.PENDING);
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

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblAttachments":
                    if (!attachment_data.isEmpty()) {
                        newIndex = isMovedDown
                                ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03()) : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                        pnAttachment = newIndex;
                        loadRecordAttachment(true);
                    }
                    break;
            }
        }
    };

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            boolean lbShow2 = pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW;
            JFXUtil.setDisabled(!lbShow2, cmbAttachmentType, btnAddAttachment, btnRemoveAttachment);
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = oSales.CustomerInquiryFollowUp().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    oSales.CustomerInquiryFollowUp().TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = oSales.CustomerInquiryFollowUp().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                }
                int lnAttachmentType = 0;
                lnAttachmentType = Integer.parseInt(lsAttachmentType);
                cmbAttachmentType.getSelectionModel().select(lnAttachmentType);
                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";

                        // in server
                        if (oSales.CustomerInquiryFollowUp().TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(oSales.CustomerInquiryFollowUp().TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                            filePath2 = oSales.CustomerInquiryFollowUp().TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
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

    // =========================================================================
    // Component Initialization Methods
    // =========================================================================
    private void initButton(int fnValue) {
        try {
            CustomCommonUtil.setVisible(false, btnSave, btnCancel,
                    btnBrowse, btnNew, btnClose,btnRetrieve);
            CustomCommonUtil.setManaged(false, btnSave,btnCancel,
                    btnBrowse, btnNew, btnClose,btnRetrieve);
                anchorEntries.setDisable(true);
            switch (fnValue) {
                case EditMode.ADDNEW:
                    // When adding or updating, only show Save and Cancel
                    CustomCommonUtil.setVisible(true, btnSave,btnRetrieve, btnCancel);
                    CustomCommonUtil.setManaged(true, btnSave,btnRetrieve, btnCancel);
                    anchorEntries.setDisable(false);
                    break;
                case EditMode.UNKNOWN:
                default:
                    // Default fallback: show only Browse and Close
                    CustomCommonUtil.setVisible(true, btnNew,btnRetrieve, btnClose);
                    CustomCommonUtil.setManaged(true, btnNew,btnRetrieve, btnClose);
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void InitTextFields() {
        registerFocusListener(AnchorInputs);
        registerFocusListener(apDetail);
        registerKeyEvents();

    }

    private void initComboBoxField() {
        cmbAttachmentType.setItems(FXCollections.observableArrayList(
                "Asset",
                "Liability",
                "Owner's Equity",
                "Revenue",
                "Expenses"
        ));

        cmbCommMethod.setItems(CustomerInquiryFollowUpStatic.COMMUNICATION_METHOD);
        cmbSocMed.setItems(CustomerInquiryFollowUpStatic.SOCIAL_MEDIA);
        cmbCustResponse.setItems(CustomerInquiryFollowUpStatic.CUSTOMER_RESPONSE);
        cmbStatus.setItems(CustomerInquiryFollowUpStatic.INQUIRY_STATUS);
        cmbFilterInquiryType.setItems(CustomerInquiryFollowUpStatic.INQUIRY_FILTER);
        cmbComVhcleCond.setItems(CustomerInquiryFollowUpStatic.COM_VHICLE_COND);

        cmbFilterInquiryType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    fbInquiryType = CustomerInquiryFollowUpStatic.INQUIRY_FILTER_CODE[newValue.intValue()];
                    if (isFistFormLoad) {
                        isFistFormLoad = false;
                    } else {
                        loadTableMaster();
                    }

                }
            }
        });

        cmbCommMethod.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    oSales.CustomerInquiryFollowUp()
                            .getModel()
                            .setMethodCode(CustomerInquiryFollowUpStatic.COMM_METHOD_CODE[newValue.intValue()]);
                }
            }
        });

        cmbSocMed.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    oSales.CustomerInquiryFollowUp()
                            .getModel()
                            .setSocialMediaCode(CustomerInquiryFollowUpStatic.SOCIAL_MEDIA_CODE[newValue.intValue()]);
                }
            }
        });
        cmbComVhcleCond.setDisable(true);
        tfBrandComp.setDisable(true);
        tfDealerComp.setDisable(true);
        cmbCustResponse.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue != null && newValue.intValue() >= 0) {

                        String responseCode = CustomerInquiryFollowUpStatic.CUSTOMER_RESPONSE_CODE[newValue.intValue()];

                        oSales.CustomerInquiryFollowUp()
                                .getModel()
                                .setResponseCode(responseCode);

                        // Enable only when "Negative Response - Lost Sale" is selected
                        boolean isLostSale = CustomerInquiryFollowUpStatic.RESPONSE_LOST_SALE.equals(responseCode);

                        cmbComVhcleCond.setDisable(!isLostSale);
                        tfBrandComp.setDisable(!isLostSale);
                        tfDealerComp.setDisable(!isLostSale);

                        // Optional: Clear values when disabled
                        if (!isLostSale) {
                            cmbComVhcleCond.getSelectionModel().clearSelection();
                            tfBrandComp.clear();
                            tfDealerComp.clear();
                        }
                    }
                });

        cmbStatus.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {

                    String status = CustomerInquiryFollowUpStatic.INQUIRY_STATUS_CODE[newValue.intValue()];

                    // Validate existing follow-up date
                    if (dpSchedFollowUp.getValue() != null) {

                        LocalDate today = LocalDate.now();
                        LocalDate selectedDate = dpSchedFollowUp.getValue();
                        long days = ChronoUnit.DAYS.between(today, selectedDate);

                        if (CustomerInquiryFollowUpStatic.FOLLOWED_UP.equals(status)
                                && (days < 0 || days > 30)) {

                            ShowMessageFX.Warning(
                                    "For Followed Up status, the follow-up date must be between today and 30 days from today.",
                                    pxeModuleName,
                                    null);

                            // Revert to previous status
                            cmbStatus.getSelectionModel().select(oldValue.intValue());
                            return;
                        }

                        if (CustomerInquiryFollowUpStatic.PENDING.equals(status)
                                && (days < 0 || days > 270)) {

                            ShowMessageFX.Warning(
                                    "For Pending status, the follow-up date must be between today and 270 days from today.",
                                    pxeModuleName,
                                    null);

                            // Revert to previous status
                            cmbStatus.getSelectionModel().select(oldValue.intValue());
                            return;
                        }
                    }

                    oSales.CustomerInquiryFollowUp()
                            .getModel()
                            .setRecordStatus(status);
                }
            }
        });
        cmbComVhcleCond.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue,
                                Number newValue) {

                if (newValue != null && newValue.intValue() >= 0) {
                    oSales.CustomerInquiryFollowUp()
                            .getModel()
                            .setCompetitorGoods(CustomerInquiryFollowUpStatic.COM_VHICLE_COND_CODE[newValue.intValue()]);
                }
            }
        });

        boolean isEncoder = UserRight.ENCODER == oApp.getUserLevel();

        if (isEncoder) {
            gridRow3.setMinHeight(0);
            gridRow3.setPrefHeight(0);
            gridRow3.setMaxHeight(0);
            tfFilterSalesperson.setVisible(false);
            lblFilterSalesPerson.setVisible(false);
            tfFollowedUpBy.setDisable(true);
            tfFollowedUpBy.setPromptText("");
        } else {
            gridRow3.setMinHeight(Region.USE_COMPUTED_SIZE);
            gridRow3.setPrefHeight(Region.USE_COMPUTED_SIZE);
            gridRow3.setMaxHeight(Region.USE_COMPUTED_SIZE);
            lblFilterSalesPerson.setVisible(true);
            tfFilterSalesperson.setVisible(true);
            tfFollowedUpBy.setDisable(false);
            tfFollowedUpBy.setPromptText("PRESS F3: Search");
        }


    }
    // =========================================================================
    // Load ReCord
    // =========================================================================
    private void LoadRecord() {
        try {
        tfTransactionNo.setText(oSales.CustomerInquiryFollowUp().getModel().getTransactionNo());
        dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                SQLUtil.dateFormat(oSales.CustomerInquiryFollowUp().getModel().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
        String inqStat = (oSales.CustomerInquiryFollowUp().getModel().SalesInquiryMaster().getInquiryStatus());
        switch (inqStat) {
            case SalesInquiryStatic.OPEN:
                tfInquiryStat.setText("OPEN");
                break;
            case SalesInquiryStatic.CONFIRMED:
                tfInquiryStat.setText("CONFIRMED");
                break;
            case SalesInquiryStatic.QUOTED:
                tfInquiryStat.setText("QUOTED");
                break;
            default:
                tfInquiryStat.setText("");
                break;
        }
        dpInquiryDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(oSales.CustomerInquiryFollowUp().getModel().SalesInquiryMaster().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));

        tfClientID.setText(oSales.CustomerInquiryFollowUp().getModel().SalesInquiryMaster().getClientId());
        poJSON = oSales.CustomerInquiryFollowUp().OpenClient(oSales.CustomerInquiryFollowUp().getModel().SalesInquiryMaster().getClientId());
            if ("success".equals(poJSON.get("result"))) {
                if (poJSON != null) {

                    tfCustomerName.setText(
                            oSales.CustomerInquiryFollowUp().getModel().SalesInquiryMaster().Client().getCompanyName());

                    tfAddress.setText((String) poJSON.get("address"));
                    tfContactNo.setText((String) poJSON.get("contact"));

                    String clientsocmed = (String) poJSON.get("SocMed");
                    switch (clientsocmed == null ? "" : clientsocmed) {
                        case "0":
                            tfSocMed.setText("FACEBOOK");
                            break;
                        case "1":
                            tfSocMed.setText("INSTAGRAM");
                            break;
                        case "2":
                            tfSocMed.setText("X");
                            break;
                        case "3":
                            tfSocMed.setText("OTHERS");
                            break;
                        default:
                            tfSocMed.setText("");
                            break;
                    }

                    tfSocMedAcct.setText((String) poJSON.get("SocMedAcct"));

                } else {
                    // Clear the fields when no client exists
                    tfCustomerName.clear();
                    tfAddress.clear();
                    tfContactNo.clear();
                    tfSocMed.clear();
                    tfSocMedAcct.clear();
                }
            }
        tfBrand.setText(oSales.CustomerInquiryFollowUp().getModel().SalesInquiryDetail().Model().Brand().getDescription());
        tfModel.setText(oSales.CustomerInquiryFollowUp().getModel().SalesInquiryDetail().Model().getDescription());
        tfModelVariant.setText(oSales.CustomerInquiryFollowUp().getModel().SalesInquiryDetail().ModelVariant().getDescription());
        tfColor.setText(oSales.CustomerInquiryFollowUp().getModel().SalesInquiryDetail().Color().getDescription());
        tfInquiryRemarks.setText(oSales.CustomerInquiryFollowUp().getModel().SalesInquiryMaster().getRemarks());

        tfFollowedUpBy.setText(oSales.CustomerInquiryFollowUp().getModel().Salesman().Client().getCompanyName());

        if (oSales.CustomerInquiryFollowUp().getModel().getFollowUpDate() != null) {
            dpSchedFollowUp.setValue(new java.sql.Date(
                    oSales.CustomerInquiryFollowUp().getModel().getFollowUpDate().getTime()).toLocalDate());
        }

        if (oSales.CustomerInquiryFollowUp().getModel().getFollowUpTime() != null) {
            tpShedTime.setValue(oSales.CustomerInquiryFollowUp().getModel().getFollowUpTime().toLocalTime());
        }


        } catch (SQLException | GuanzonException |CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    }

    private void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate,dpInquiryDate,dpFilterDateFrom,dpFilterDateThru,dpSchedFollowUp);

        LocalDate currentDate = LocalDate.now();
        dpFilterDateFrom.setValue(currentDate.withDayOfMonth(1));
        dpFilterDateThru.setValue(currentDate);

        setDefaultFollowUpSchedule();

        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate,dpFilterDateFrom,dpFilterDateThru,dpSchedFollowUp);
        JFXUtil.setActionListener(
                this::timepicker_Action,
                tpShedTime);
    }

    private void setDefaultFollowUpSchedule() {
        LocalDate currentDate = LocalDate.now();
        dpSchedFollowUp.setValue(currentDate);
        oSales.CustomerInquiryFollowUp().getModel().setFollowUpDate(java.sql.Date.valueOf(currentDate));
    }


    private void timepicker_Action(ActionEvent event) {

        switch (((JFXTimePicker) event.getSource()).getId()) {

            case "tpShedTime":
                if (tpShedTime.getValue() != null) {
                    oSales.CustomerInquiryFollowUp()
                            .getModel()
                            .setFollowUpTime(java.sql.Time.valueOf(tpShedTime.getValue()));
                }
                break;

            default:
                break;
        }
    }
    private void datepicker_Action(ActionEvent event) {

        DatePicker source = (DatePicker) event.getSource();

        switch (source.getId()) {
            case "dpSchedFollowUp":
                if (dpSchedFollowUp.getValue() != null) {

                    LocalDate selectedDate = dpSchedFollowUp.getValue();
                    LocalDate today = LocalDate.now();

                    long days = ChronoUnit.DAYS.between(today, selectedDate);

                    String recordStatus = oSales.CustomerInquiryFollowUp()
                            .getModel()
                            .getRecordStatus();

                    if (CustomerInquiryFollowUpStatic.FOLLOWED_UP.equals(recordStatus)) {

                        if (days < 0 || days > 30) {
                            ShowMessageFX.Warning(
                                    "Follow-up date must be between today and 30 days from today.",
                                    pxeModuleName,
                                    null);

                            dpSchedFollowUp.setValue(today);
                            return;
                        }

                    } else if (CustomerInquiryFollowUpStatic.PENDING.equals(recordStatus)) {

                        if (days < 0 || days > 270) {
                            ShowMessageFX.Warning(
                                    "Follow-up date must be between today and 270 days from today.",
                                    pxeModuleName,
                                    null);

                            dpSchedFollowUp.setValue(today);
                            return;
                        }
                    }

                    oSales.CustomerInquiryFollowUp()
                            .getModel()
                            .setFollowUpDate(java.sql.Date.valueOf(selectedDate));
                }
                break;
            default:
                break;
        }
    }
    // =========================================================================
    // Table Setup Methods
    // =========================================================================
    private void initTables(){
        initTableMaster();
        initTableDetail();
    }
    private void initTableMaster() {
        JFXUtil.setColumnCenter(tblMRowNo, tblMTransNo, tblMCustName, tblMDate, tblMSalesPerson);
        JFXUtil.setColumnsIndexAndDisableReordering(tblMaster);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblMaster.setItems(filteredMain_Data);
    }
    private void initTableDetail() {
        JFXUtil.setColumnCenter(tblDRowNo, tblDTransNo, tblDCustName, tblDResponse, tblDDate,tblDFollowUpBy,tblDStatus);
        JFXUtil.setColumnsIndexAndDisableReordering(tblDetail);
        tblDetail.setItems(detail_data);
    }


    // =========================================================================
    // Table Data Loading Methods
    // =========================================================================
    private void loadTableMaster() {
        if (isLoadingMaster) {
            return;
        }

        isLoadingMaster = true;

        btnRetrieve.setDisable(true);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        tblMaster.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        poJSON = new JSONObject();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ObservableList<ModelTableMain> tempData
                            = FXCollections.observableArrayList();

                    if (data != null) {
                        data.clear();
                    }

                    poJSON = oSales.CustomerInquiryFollowUp().RetreiveSource(
                            fbInquiryType,
                            fbSalesPersonID,
                            fbCustomerID,
                            dpFilterDateFrom.getValue(),
                            dpFilterDateThru.getValue());

                    if ("success".equals(poJSON.get("result"))) {

                    data = (JSONArray) poJSON.get("payload");

                        for (int i = 0; i < data.size(); i++) {
                            JSONObject obj = (JSONObject) data.get(i);

                                tempData.add(new ModelTableMain(
                                        String.valueOf(i + 1),
                                        obj.get("sTransNox") == null ? "" : obj.get("sTransNox").toString(),
                                        obj.get("CustomerName") == null ? "" : obj.get("CustomerName").toString(),
                                        obj.get("dFollowUp") == null
                                                ? (obj.get("dTransact") == null ? "" : obj.get("dTransact").toString())
                                                : obj.get("dFollowUp").toString(),
                                        obj.get("SalesPerson") == null ? "" : obj.get("SalesPerson").toString(),
                                        obj.get("cTranStat") == null ? "" : obj.get("cTranStat").toString()
                                ));
                        }
                    }

                    Platform.runLater(() -> {

                        main_data.setAll(tempData);

                        if (main_data.isEmpty()) {
                            tblMaster.setPlaceholder(
                                    new Label("NO RECORD TO LOAD"));
                        }

                        tblMaster.setItems(main_data);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                isLoadingMaster = false;
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);
                if (main_data.isEmpty()) {
                    tblMaster.setPlaceholder(
                            new Label("NO RECORD TO LOAD"));
                    if(isFistFormLoad == false){
                        ShowMessageFX.Warning(
                                "NO RECORD TO LOAD.",
                                pxeModuleName,
                                null);
                    }
                    isFistFormLoad = false;
                }
                setupPagination();
                showRetainedHighlight(true);
            }

            @Override
            protected void failed() {
                isLoadingMaster = false;

                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);

                if (getException() != null) {
                    getException().printStackTrace();
                }
            }
        };

        new Thread(task).start();
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

        tblDetail.setPlaceholder(loadingPane);
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
                            tblDetail.setPlaceholder(
                                    new Label("NO RECORD TO LOAD"));
                        }

                        tblDetail.setItems(detail_data);
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
                    tblDetail.setPlaceholder(
                            new Label("NO RECORD TO LOAD"));

                }
                setupPagination();
                showRetainedHighlight(true);
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

    // =========================================================================
    // Button & Event Handlers
    // =========================================================================
    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
        btnRetrieve.setOnAction(this::handleButtonAction);
        btnAddAttachment.setOnAction(this::handleButtonAction);
        btnRemoveAttachment.setOnAction(this::handleButtonAction);
        btnArrowLeft.setOnAction(this::handleButtonAction);
        btnArrowRight.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                unloadForm appUnload = new unloadForm();
                switch (clickedButton.getId()) {
                    case "btnClose":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", pxeModuleName, null)) {
                            appUnload.unloadForm(ChildAnchorPane, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        ClearAllFields();
                        attachment_data.clear();
                        oSales.CustomerInquiryFollowUp().resetattachment();
                        poJSON = oSales.CustomerInquiryFollowUp().newRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }

                        pnEditMode = oSales.CustomerInquiryFollowUp().getEditMode();
                        initButton(pnEditMode);
                        cmbFilterInquiryType.getSelectionModel().selectFirst();
                        LoadRecord();
                        if (dpSchedFollowUp.getValue() == null) {
                            setDefaultFollowUpSchedule();
                        }

                        break;

                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel editing this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            ClearAllFields();
                            initializeObject();
                            pnEditMode =  EditMode.READY;
                            initButton(pnEditMode);
//                            cmbFilterInquiryType.getSelectionModel().selectFirst();
                        }
                        break;
                    case "btnSave":
                        oSales.CustomerInquiryFollowUp().getModel().setEntryBy(oApp.getUserID());
                        oSales.CustomerInquiryFollowUp().getModel().setEntryDate(oApp.getServerDate());
                        poJSON = oSales.CustomerInquiryFollowUp().saveRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                        btnNew.fire();
                        break;
                    case "btnRetrieve":
                        isFistFormLoad = false;
                        loadTableMaster();
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
                            for (int lnCtr = 0; lnCtr <= oSales.CustomerInquiryFollowUp().getTransactionAttachmentCount() - 1; lnCtr++) {
                                if (imgPath2.equals(oSales.CustomerInquiryFollowUp().TransactionAttachmentList(lnCtr).getModel().getFileName())
                                        && RecordStatus.ACTIVE.equals(oSales.CustomerInquiryFollowUp().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "File name already exists.");
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
                                        ShowMessageFX.Warning(null, pxeModuleName, "PDF exceeds maximum allowed pages.");
                                        return;
                                    }
                                }
                            }

//                            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data,  poGLControllers.PaymentRequest().addAttachment(imgPath2), 3);
//                            pnAttachment = lnTempRow;
                            pnAttachment = oSales.CustomerInquiryFollowUp().addAttachment(imgPath2);
                            //Copy file to Attachment path
                            oSales.CustomerInquiryFollowUp().copyFile(selectedFile.toString());
                            loadTableAttachment();
                            tblAttachments.getFocusModel().focus(pnAttachment);
                            tblAttachments.getSelectionModel().select(pnAttachment);
                        }
                        break;
                    case "btnRemoveAttachment":
                        if (oSales.CustomerInquiryFollowUp().getTransactionAttachmentCount() <= 0) {
                            return;
                        } else {
                            for (int lnCtr = 0; lnCtr < oSales.CustomerInquiryFollowUp().getTransactionAttachmentCount(); lnCtr++) {
                                if (RecordStatus.INACTIVE.equals(oSales.CustomerInquiryFollowUp().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    if (pnAttachment == lnCtr) {
                                        return;
                                    }
                                }
                            }
                        }
                        poJSON = oSales.CustomerInquiryFollowUp().removeAttachment(pnAttachment);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
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
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException  ex) {
                Logger.getLogger(CustomerInquiryFollowUpController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
                try {
                    if (oApp != null) {

                        oApp.rollbackTrans(); // 🔥 force rollback
                    }
                } catch (SQLException ex1) {
                    Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex1);
                }

            } catch (Exception ex) {
                Logger.getLogger(CustomerInquiryFollowUpController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
            }
        }
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
                        for (lnCtr = 0; lnCtr < oSales.CustomerInquiryFollowUp().getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(oSales.CustomerInquiryFollowUp().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                continue;
                            }
                            lnCount += 1;
                            attachment_data.add(
                                    new ModelCustomerInquiryFollowUpAttachment(String.valueOf(lnCount),
                                            String.valueOf(oSales.CustomerInquiryFollowUp().TransactionAttachmentList(lnCtr).getModel().getFileName()),
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
    private void slideImage(int direction) {
        if (attachment_data.size() <= 0) {
            return;
        }

        currentIndex = pnAttachment;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
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


    private void initTableOnClick() {

            tblMaster.setOnMouseClicked(event -> {
                pnMain = tblMaster.getSelectionModel().getSelectedIndex();
                if (pnMain >= 0) {
                    if (event.getClickCount() == 2) {
                        if (pnEditMode == EditMode.UPDATE ) {
                            boolean lbProceed = ShowMessageFX.YesNo(
                                    "Loading another transaction will invalidate all current entry on the loaded transaction.\n\nDo you want to proceed?",
                                    pxeModuleName,
                                    "Confirm Action"
                            );

                            if (!lbProceed) {
                                return; // Stop loading another transaction
                            }

                        }
                        if(pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW){
                            loadTableRecordFromMain();
                        }


                    }
                }
            });

            tblMaster.setRowFactory(tv -> new TableRow<ModelTableMain>() {
                        @Override
                        protected void updateItem(ModelTableMain item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setStyle("");
                            } else {
                                String key = item.getIndex01();
                                if (highlightedRowsMain.containsKey(key)) {
                                    List<String> colors = highlightedRowsMain.get(key);
                                    if (!colors.isEmpty()) {
                                        setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";"); // Apply latest color
                                    }
                                } else {
                                    setStyle(""); // Default style
                                }
                            }
                        }
                    }
            );

            JFXUtil.adjustColumnForScrollbar(tblMaster);
            tblAttachments.setOnMouseClicked(event -> {
                pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
                if (pnAttachment >= 0) {
                    scaleFactor = 1.0;
                    loadRecordAttachment(true);
                    resetImageBounds();
                }
            });

    }
    private void loadTableRecordFromMain() {
        poJSON = new JSONObject();
        ModelTableMain selected = (ModelTableMain) tblMaster.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                System.out.println("index 1" +  selected.getIndex02() + "index 2" +  selected.getIndex01());
                String lsTransactionNo = selected.getIndex02();
                fbSourceNo = lsTransactionNo;
//                clearFields();
                System.out.println("TO OPEN RECORD IS : " + lsTransactionNo);
                poJSON = oSales.CustomerInquiryFollowUp().OpenSalesInquiry(lsTransactionNo);
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                JFXUtil.disableAllHighlightByColor(tblMaster, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblMaster, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                pnEditMode = oSales.CustomerInquiryFollowUp().getEditMode();
                LoadRecord();
                loadTableDetail();
//                initFields(pnEditMode);
                initButton(pnEditMode);
            } catch (SQLException | GuanzonException | RuntimeException | CloneNotSupportedException ex) {
                Logger.getLogger(CheckStatusUpdateController.class
                        .getName()).log(Level.SEVERE, null, ex);
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

    private void registerKeyEvents() {
        tfFilterCustName.setOnKeyPressed(this::txtField_KeyPressed);
        tfFilterSalesperson.setOnKeyPressed(this::txtField_KeyPressed);
        tfFollowedUpBy.setOnKeyPressed(this::txtField_KeyPressed);
    }

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                if (!pbLoaded) {
                    return;
                }
                if (isSearching) {
                    return;
                }
                switch (lsID) {
                    case "tfBrandComp":
                        poJSON = oSales.CustomerInquiryFollowUp().getModel().setCompetitorMake(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                    case "tfDealerComp":
                        poJSON = oSales.CustomerInquiryFollowUp().getModel().setCompetitorDealer(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                    case "tfFilterSalesperson":
                        if(lsValue == null || lsValue.isEmpty()){
                            fbSalesPersonID = "";
                            loadTableMaster();
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
                        poJSON = oSales.CustomerInquiryFollowUp().getModel().setMessage(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                    case "taFollowUpRemarks":
                        poJSON = oSales.CustomerInquiryFollowUp().getModel().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
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
                            case "tfFilterCustName":
                                poJSON = oSales.CustomerInquiryFollowUp().FilterByCustomerName(lsValue, false);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfFilterCustName.setText((String) poJSON.get("customerNme"));
                                fbCustomerID = (String) poJSON.get("clientID");
                                isFistFormLoad = false;
                                loadTableMaster();
                                break;
                            case "tfFilterSalesperson":
                                poJSON = oSales.CustomerInquiryFollowUp().FilterBySalesPerson(lsValue, false);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfFilterSalesperson.setText((String) poJSON.get("salesman"));
                                fbSalesPersonID = (String) poJSON.get("salesmanID");
                                isFistFormLoad = false;
                                loadTableMaster();
                                break;
                            case "tfFollowedUpBy":
                                poJSON = oSales.CustomerInquiryFollowUp().SearchSalesPerson(lsValue, false);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfFollowedUpBy.setText(oSales.CustomerInquiryFollowUp().getModel().Salesman().Client().getCompanyName());
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
            Logger.getLogger(CustomerInquiryFollowUpController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // =========================================================================
    // Helper / Utility Methods
    // =========================================================================
    private void ClearAllFields() {
        clearControls(AnchorInputs);
        clearControls(apDetail);
        imageView.setImage(null);
        tfAttachmentNo.clear();
        detail_data.clear();
        tblDetail.getItems().clear();
        if (data_details != null) {
            data_details.clear();
        }
    }

    private void clearControls(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {

            if (node instanceof TextField) {
                ((TextField) node).clear();
            } else if (node instanceof TextArea) {
                ((TextArea) node).clear();
            } else if (node instanceof DatePicker) {
                ((DatePicker) node).setValue(null);
            } else if (node instanceof ComboBox) {
                ComboBox<?> combo = (ComboBox<?>) node;
                if ("cmbFilterInquiryType".equals(combo.getId())) {
                    continue;
                }
                combo.getSelectionModel().clearSelection();
                combo.setValue(null);
            } else if (node instanceof JFXTimePicker) {
                ((JFXTimePicker) node).setValue(null);
            } else if (node instanceof Parent) {
                clearControls((Parent) node);
            }
        }

    }
    
    private void setupPagination() {

        if (main_data == null || main_data.isEmpty()) {
            pagination.setPageCount(0);
            pagination.setPageFactory(null);

            tblMaster.setItems(FXCollections.observableArrayList());
            tblMaster.setPlaceholder(new Label("NO RECORD TO LOAD"));
            return;
        }

        int pageCount = (int) Math.ceil(main_data.size() * 1.0 / ROWS_PER_PAGE);

        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {

        if (main_data == null || main_data.isEmpty()) {
            tblMaster.setItems(FXCollections.observableArrayList());
            return new StackPane();
        }

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, main_data.size());

        ObservableList<ModelTableMain> pageData
                = FXCollections.observableArrayList(main_data.subList(fromIndex, toIndex));

        tblMaster.setItems(pageData);
        showRetainedHighlight(true);

        return new StackPane(); // prevents layout re-render/flicker
    }

    private void showRetainedHighlight(boolean isRetained) {
        if (isRetained) {
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"0".equals(pair.getValue())) {

                    plOrderNoFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
                }
            }
        }
        JFXUtil.disableAllHighlightByColor(tblMaster, "#C1E1C1", highlightedRowsMain);
        plOrderNoPartial.clear();
        for (Pair<String, String> pair : plOrderNoFinal) {
            if (!"0".equals(pair.getValue())) {
                JFXUtil.highlightByKey(tblMaster, pair.getKey(), "#C1E1C1", highlightedRowsMain);
            }
        }
    }


}
