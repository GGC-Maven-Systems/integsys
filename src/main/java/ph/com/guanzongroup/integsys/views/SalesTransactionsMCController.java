package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.systables.Model_Transaction_Attachment;
import org.guanzon.appdriver.base.*;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.MCSales;
import ph.com.guanzongroup.cas.sales.constant.SalesTransactionStatic;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Giveaways;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Insurance;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Registration;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.ExplorerWindowUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import ph.com.guanzongroup.cas.sales.authmatrix.MatrixRecipientDialog;
import javafx.scene.Node;

/**
 * Controller for SalesTransactionMC.fxml (Motorcycle Sales form).
 *
 * Structure follows SalesQoutationController: ScreenInterface + Initializable,
 * initButton(editMode), JFXUtil focus listeners that write straight to the
 * model, F3 search in txtField_KeyPressed, Task-based table loading, and a
 * transaction summary rebuilt from the model after every change.
 *
 * ------------------------------------------------------------------------------
 * CHANGES IN THIS VERSION 1-14. (earlier versions) compile + load fixes,
 * Registration & Insurance, Payment tab, Confirm / Void / History, Attachment
 * tab wired to MCSales. 15. CWT is now a Yes / No combo
 * (SalesTransactionStatic.CWT_RATES = {"Yes", "No"}): - Yes: tfPaymentCwt is
 * editable; the typed amount is written to the master, then the units table and
 * summary reload. - No : tfPaymentCwt is disabled and not editable; the CWT
 * amount is forced to 0. - syncCwt() no longer computes rate x base (it would
 * overwrite the typed amount); the rate helpers were removed. 16. Client type
 * combo shows the master's client type (LoadMaster), New defaults to the
 * model's default (Individual). 17. LoadClientInfo() / LoadCoClientInfo():
 * blank birthplace / citizenship no longer stop the rest of the load.
 *
 * ASSUMED (not visible in the classes provided) - each one is used in ONE
 * place, fix it there: Model_Sales_Master getters mirroring the setters MCSales
 * calls: getTransactionTotal, getDiscount, getAddDiscount, getFreight,
 * getVATSales, getVATAmount, getNonVATSales, getZeroVATSales, getAmountDue,
 * getTermCode (getAdvances and getCWTAmount are used by MCSales itself),
 * setCWTAmount(double), getDueDate()/setDueDate(Date),
 * Salesman().getCompanyName() for the credit advisor name. Amount paid is read
 * from the raw column "nAmtPaidx". Model_Sales_Insurance /
 * Model_Sales_Registration: getInsuranceAmount(), setInsuranceAmount(),
 * getInsuranceYear(), setInsuranceYear(), getChargeTo(), setChargeTo().
 * Model_Transaction_Attachment: the attachment type is kept in the raw column
 * ATTACH_TYPE_COLUMN through getValue()/setValue(); change the constant if your
 * table names it differently.
 *
 * NEW CONSTANTS NEEDED in SalesTransactionStatic: TBL_NO_REGINSUR,
 * MSG_CONFIRM_REMOVE_REGINSUR
 * ------------------------------------------------------------------------------
 */
public class SalesTransactionsMCController implements Initializable, ScreenInterface {

    private final String pxeModuleName = SalesTransactionStatic.MODULE_NAME;

    private GRiderCAS oApp;
    private SalesControllers oSalesController;
    private JSONObject poJSON;
    private int pnEditMode;
    private boolean pbLoaded = false;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private int pnUnitLoadSeq = 0;
    private int pnRegInsurLoadSeq = 0;
    private int pnGawayLoadSeq = 0;
    private int pnAttachLoadSeq = 0;

    double ldstackPaneWidth = 0;
    double ldstackPaneHeight = 0;
    /**
     * The rows that were selected when a field got focus: the field's edit
     * belongs to THESE rows even if the user clicks another row (which changes
     * the selection before the field's lost-focus handler runs).
     */
    private Model_Sales_Detail poEditUnit;
    private Model_Sales_Giveaways poEditGaway;
    /**
     * Row selected in tblRegAndIsur (tracked by object: registration and
     * insurance entry nos can be equal).
     */
    private Model poSelRegInsur;
    /**
     * Rows shown in the Registration / Insurance sections (either can be null).
     */
    private Model_Sales_Registration poSectionReg;
    private Model_Sales_Insurance poSectionIns;
    /**
     * The section rows that were showing when a field got focus.
     */
    private Model_Sales_Registration poEditReg;
    private Model_Sales_Insurance poEditIns;

    private int pnUnitRow = -1;
    private int pnGawayRow = -1;
    private int pnRegInsurRow = -1;
    private int pnAttachRow = -1;        // index in poAttachFiles / attach_data (NOT the model index: see poAttachModelRows)
    private boolean pbSyncing = false;   // true while combos/checkboxes are set programmatically
    private boolean pbSearching = false;   // true while the F3 promo search runs
    private String psPromoAtFocus = "";    // tfPomoCode text when it got focus
    /**
     * true when the row selected in tblRegAndIsur is a registration.
     */
    private final BooleanProperty pbRegistrationRow = new SimpleBooleanProperty(false);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(SalesTransactionStatic.DATE_PATTERN);
    /**
     * true when the payment form is Term; drives the Term field only.
     */
    private final BooleanProperty pbTermPayment = new SimpleBooleanProperty(false);
    /**
     * true when the CWT combo is "Yes"; drives the CWT amount field.
     */
    private final BooleanProperty pbCwtYes = new SimpleBooleanProperty(false);
    /**
     * Index of "Term" in SalesTransactionStatic.PAYMENT_FORMS = {"Cash",
     * "Term", "Cash Balance", "Financing"}.
     */
    private static final int PAYMENT_FORM_TERM = 1;
    /**
     * Index of "Yes" in SalesTransactionStatic.CWT_RATES = {"Yes", "No"}.
     */
    private static final int CWT_YES = 0;
    private static final int CWT_NO = 1;
    /**
     * CWT PERCENTAGE (0 - 100, e.g. 2 for 2%) typed in tfPaymentCwt. MCSales.computeTransaction() reads the master's CWT
     * field as this RATE and overwrites the same field with the AMOUNT (rate x (VAT sales + non-VAT sales)), and many MCSales
     * getters run it. So the rate lives HERE, and recomputeTransaction() writes it to the master right before every compute.
     */
    private double pdCwtRate = 0.0;
    /**
     * true only in ADDNEW / UPDATE; combos, check boxes, date pickers and
     * action buttons are bound to it.
     */
    private final BooleanProperty pbEditable = new SimpleBooleanProperty(false);
    /**
     * true when the payment transaction type is Credit; drives the Due Date
     * field.
     */
    private final BooleanProperty pbCreditPayment = new SimpleBooleanProperty(false);

    /**
     * Raw column of Transaction_Attachment that keeps the attachment type
     * (combo index as text). ASSUMED name.
     */
    private static final String ATTACH_TYPE_COLUMN = "sDocuType";

    /**
     * Files of the active attachments (same order as attach_data /
     * poAttachModelRows).
     */
    private final ObservableList<File> poAttachFiles = FXCollections.observableArrayList();
    /**
     * Attachment type (combo index) by file name: fallback when the model does
     * not keep the type column.
     */
    private final Map<String, Integer> poAttachTypes = new HashMap<>();
    private boolean pbShowingAttach = false;   // true while displayAttachment() changes the table selection itself
    // =========================================================================
    // TextInputControl groups (filled in initControlGroups(), NOT here:
    // @FXML fields are still null until the FXML loader injects them).
    // =========================================================================
    private List<TextInputControl> poMasterFields;
    private List<TextInputControl> poClientFields;
    private List<TextInputControl> poMotorcycleFields;
    private List<TextInputControl> poRegistrationFields;
    private List<TextInputControl> poGiveawayFields;
    private List<TextInputControl> poPaymentFields;
    private List<TextInputControl> poAllFields;   // every field that gets key press / lost focus handlers
    private List<ComboBox> poCombos;
    private List<CheckBox> poCheckBoxes;
    private List<DatePicker> poDatePickers;
    private List<DatePicker> poReadOnlyDatePickers;   // filled from the record, never edited by hand
    private List<TextField> poSummaryFields;

    // =========================================================================
    // Table data
    // =========================================================================
    private final ObservableList<Model_Sales_Detail> unit_data = FXCollections.observableArrayList();
    private final ObservableList<Model_Sales_Giveaways> gaway_data = FXCollections.observableArrayList();
    private final ObservableList<Model> reginsur_data = FXCollections.observableArrayList();   // Model_Sales_Registration / Model_Sales_Insurance rows
    private final ObservableList<ModelTableDetail> invoice_data = FXCollections.observableArrayList();
    private final ObservableList<Model_Transaction_Attachment> attach_data = FXCollections.observableArrayList();
    private final List<Integer> poAttachModelRows = new ArrayList<>();   // table row -> MCSales attachment index

    // =========================================================================
    // Root containers & buttons
    // =========================================================================
    @FXML
    private AnchorPane ChildAnchorPane;
    @FXML
    private AnchorPane apSearchMaster;
    @FXML
    private AnchorPane AnchorInputs;
    @FXML
    private AnchorPane AnchorMCSales;
    @FXML
    private AnchorPane AnchorClient;
    @FXML
    private AnchorPane AnchorAttach;
    @FXML
    private AnchorPane AnchorAttach2;
    @FXML
    private AnchorPane AnchorMCPrice;
    @FXML
    private AnchorPane AnchorRegInsur;
    @FXML
    private AnchorPane AnchorGiveaway;
    @FXML
    private AnchorPane AnchorPaymType;
    @FXML
    private AnchorPane anchorReg;
    @FXML
    private AnchorPane anchorInsur;
    @FXML
    private AnchorPane anchorGaways;
    @FXML
    private HBox hbButtons;

    @FXML
    private Button btnBrowse;
    @FXML
    private Button btnNew;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnApproved;   // "Confirm"
    @FXML
    private Button btnVoid;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnAddClient;
    @FXML
    private Button btnAddClient1;
    @FXML
    private Button btnNonItemize;// co-client add button (fx:id as in the FXML)
    @FXML
    private FontAwesomeIconView faAdd12;
    @FXML
    private FontAwesomeIconView faAdd121;

    // =========================================================================
    // Header
    // =========================================================================
    @FXML
    private TextField tfTransNo;
    @FXML
    private DatePicker dpTransDate;
    @FXML
    private TextField tfSourceDesc;
    @FXML
    private TextField tfSourceNo;
    @FXML
    private TextField tfReceipt;          // DR No (header, read-only mirror)
    @FXML
    private TextArea taCustomerName;
    @FXML
    private Label lblStatus;

    // =========================================================================
    // Client Information tab
    // =========================================================================
    @FXML
    private ComboBox cmbClientType;
    @FXML
    private TextField tfClientName;
    @FXML
    private TextField tfClientGender;
    @FXML
    private TextField tfClientCitizenship;
    @FXML
    private DatePicker dpClientBday;
    @FXML
    private TextField tfClientBirthplace;
    @FXML
    private TextField tfClientOccupation;
    @FXML
    private TextField tfClientTinNo;
    @FXML
    private TextField tfClientFathersName;
    @FXML
    private TextField tfClientMothersName;
    @FXML
    private TextField tfClientContactNo;
    @FXML
    private TextField tfClientEmail;
    @FXML
    private TextField tfClientAddress;
    @FXML
    private TextField tfClientLTOID;
    @FXML
    private TextField tfClientLTOAddress;

    // Co-Client (fx:id "tfCoClientCitezenship" is spelled that way in the FXML)
    @FXML
    private TextField tfCoClientName;
    @FXML
    private TextField tfCoClientGender;
    @FXML
    private TextField tfCoClientCitezenship;
    @FXML
    private DatePicker dpCoClientBirthday;
    @FXML
    private TextField tfCoClientBirthplace;
    @FXML
    private TextField tfCoClientOccupation;
    @FXML
    private TextField tfCoClientTinNo;
    @FXML
    private TextField tfCoClientFathersName;
    @FXML
    private TextField tfCoClientMothersName;
    @FXML
    private TextField tfCoClientContactNo;
    @FXML
    private TextField tfCoClientEmail;
    @FXML
    private TextField tfCoClientAddress;
    @FXML
    private TextField tfCoClientLTOID;
    @FXML
    private TextField tfCoClientLTOAddress;

    // =========================================================================
    // Motorcycle & Pricing tab
    // =========================================================================
    @FXML
    private TextField tfMCModel;
    @FXML
    private TextField tfMCColor;
    @FXML
    private TextField tfMCEngineNo;
    @FXML
    private TextField tfMCFrameNo;
    @FXML
    private TextField tfMCWarrantyNo;
    @FXML
    private TextField tfMCKeyCode;
    @FXML
    private TextField tfMCRemarks;
    @FXML
    private ComboBox cmbVehicleType;
    @FXML
    private TextField tfMCUnitPrice;     // Unit Price
    @FXML
    private TextField tfPomoCode;        // Promo
    @FXML
    private TextField tfMCRegisAmt;      // Regis. Amt
    @FXML
    private TextField tfMCInsurAmt;      // Insur. Amt
    @FXML
    private TextField tfPromoDisc;       // Discount (%)
    @FXML
    private TextField tfPromoAddDisc;    // Add. Discount
    @FXML
    private TextField tfFreight;         // Freight

    // =========================================================================
    // Registration & Insurance tab
    // =========================================================================
    @FXML
    private TextField tfRegEngineNo;
    @FXML
    private TextField tfRegFrameNo;
    @FXML
    private ComboBox cmbRegistrationType;
    @FXML
    private TextField tfRegAmount;
    @FXML
    private TextField tfRegInsurType;
    @FXML
    private CheckBox cbRegIsCharge;
    @FXML
    private TextField tfInsurType;
    @FXML
    private TextField tfInsurYear;
    @FXML
    private TextField tfInsurPayer;
    @FXML
    private TextField tfInsurAmt;
    @FXML
    private CheckBox cbInsurIsCharge;

    @FXML
    private TableView<Model> tblRegAndIsur;
    @FXML
    private TableColumn<Model, String> tblRowRegAndInsurNo;
    @FXML
    private TableColumn<Model, String> tblRowRegAndInsurEngine;
    @FXML
    private TableColumn<Model, String> tblRowRegAndInsurSource;
    @FXML
    private TableColumn<Model, String> tblRowRegAndInsurDesc;
    @FXML
    private TableColumn<Model, String> tblRowRegAndInsurAmount;
    @FXML
    private TableColumn<Model, String> tblRowRegAndInsurStat;
    @FXML
    private TableColumn<Model, String> tblRowRegAndInsurAction;   // Remove column

    // =========================================================================
    // Giveaways tab
    // =========================================================================
    @FXML
    private TextField tfGawayBarrcode;
    @FXML
    private TextField tfGawayDesc;
    @FXML
    private TextField tfGawayRemarks;
    @FXML
    private TextField tfGawayGiven;
    @FXML
    private TextField tfGawayIssued;
    @FXML
    private TextField tfGawayQty;
    @FXML
    private TableView<Model_Sales_Giveaways> tblGawayItem;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayNo;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayBarrcode;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayDesc;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayRemarks;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayQOH;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayGiven;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayIssued;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayQty;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayStatus;
    @FXML
    private TableColumn<Model_Sales_Giveaways, String> tblRowGawayAction11211;

    // =========================================================================
    // Payment Type tab
    // =========================================================================
    @FXML
    private ComboBox cmbPaymentTranType;
    @FXML
    private ComboBox cmbPaymentForm;
    @FXML
    private TextField tfPaymentCreditAdvisor;
    @FXML
    private ComboBox cmbPaymentCWT;
    @FXML
    private CheckBox chkPaymentHasPromo;
    @FXML
    private TextField tfPaymentDrNo;         // DR No (payment tab, read-only)
    @FXML
    private TextField tfPaymentTerm;
    @FXML
    private DatePicker dpPaymentDueDate;
    @FXML
    private TextField tfPaymentTotal;
    @FXML
    private TextField tfPaymentAdvances;
    @FXML
    private TextField tfPaymentCwt;
    @FXML
    private TextField tfPaymentAmountPaid;
    @FXML
    private TextField tfPaymentBalance;
    @FXML
    private TextField tfSalesman;

    @FXML
    private TableView<ModelTableDetail> tblInvoiceList;   // Invoice list
    @FXML
    private TableColumn<ModelTableDetail, String> tblRowInvoiceNo;
    @FXML
    private TableColumn<ModelTableDetail, String> tblRowInvoiceDate;
    @FXML
    private TableColumn<ModelTableDetail, String> tblRowInvoiceSource;
    @FXML
    private TableColumn<ModelTableDetail, String> tblRowInvoiceReceiptNo;
    @FXML
    private TableColumn<ModelTableDetail, String> tblRowInvoiceAmount;
    @FXML
    private TableColumn<ModelTableDetail, String> tblRowInvoiceStatus;

    // =========================================================================
    // Units grid (bottom of the Sales Information tab)
    // =========================================================================
    @FXML
    private TableView<Model_Sales_Detail> tblUnits;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailNo;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailBrand;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailModel;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailEngineNo;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailFramNo;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailVariant;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailWarrantyNo;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailKeySerial;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailFreight;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailNetSRP;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailSRP;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailInsReg;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailDiscount;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailAddDisc;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailRemarks;
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailQOH21;   // used as the Remove column
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailQOH22;   // unused in FXML, dropped at runtime
    @FXML
    private TableColumn<Model_Sales_Detail, String> tblColDetailQOH23;   // unused in FXML, dropped at runtime

    // =========================================================================
    // Attachment tab
    // =========================================================================
    @FXML
    private TextField tfAttachmentNo;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private TableView<Model_Transaction_Attachment> tblAttachments;
    @FXML
    private TableColumn<Model_Transaction_Attachment, String> tblRowNoAttachment;
    @FXML
    private TableColumn<Model_Transaction_Attachment, String> tblFileNameAttachment;
    @FXML
    private Button btnAddAttachment;
    @FXML
    private Button btnRemoveAttachment;
    @FXML
    private StackPane stackPane;
    @FXML
    private ImageView imageView;
    @FXML
    private Button btnArrowLeft;
    @FXML
    private Button btnArrowRight;

    // =========================================================================
    // Transaction summary
    // =========================================================================
    @FXML
    private TextField tfSummaryCustomer;
    @FXML
    private TextField tfSummaryTranType;
    @FXML
    private TextField tfSummaryPaymentForm;
    @FXML
    private TextField tfSummaryTerm;
    @FXML
    private TextField tfSummaryDueDate;
    @FXML
    private TextField tfSummaryUnitPrice;
    @FXML
    private TextField tfSummaryDiscount;
    @FXML
    private TextField tfSummaryAddDiscount;
    @FXML
    private TextField tfSummaryFreight;
    @FXML
    private TextField tfSummaryRegistration;
    @FXML
    private TextField tfSummaryInsurance;
    @FXML
    private TextField tfSummaryTotal;
    @FXML
    private TextField tfSummaryGiveaways;
    @FXML
    private TextField tfSummaryVatSales;
    @FXML
    private TextField tfSummaryVatAmount;
    @FXML
    private TextField tfSummaryNonVatSales;
    @FXML
    private TextField tfSummaryZeroVatSales;
    @FXML
    private TextField tfSummaryCwt;
    @FXML
    private TextField tfSummaryAdvances;
    @FXML
    private TextField tfSummaryAmountPaid;
    @FXML
    private TextField tfSummaryBalance;

    // =========================================================================
    // ScreenInterface
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
    // Lifecycle
    // =========================================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initControlGroups();      // must run first: FXML injection is done by now
        initializeObject();
        initFieldHandlers();
        initComboBoxes();
        initCheckBoxes();
        initDatePickers();
        initTables();
        initAttachmentTab();
        initOtherButtons();
        initSummary();
        initHotKeys();
        pnEditMode = oSalesController.MCSales().getEditMode();
        initButton(pnEditMode);
        ClickButton();
        pbLoaded = true;
    }

    /**
     * Groups the text fields once, after FXML injection. Each group is used by
     * its Clear*() method; poAllFields is used to attach handlers. tfSummary*
     * fields are display-only and intentionally NOT included.
     */
    private void initControlGroups() {
        // tfSalesman is in this group so it gets the F3 handler and is cleared with the rest of the header
        poMasterFields = Arrays.asList(tfTransNo, tfSourceDesc, tfSourceNo, tfReceipt, taCustomerName, tfSalesman);

        poClientFields = Arrays.asList(
                tfClientName, tfClientGender, tfClientCitizenship, tfClientBirthplace, tfClientOccupation,
                tfClientTinNo, tfClientFathersName, tfClientMothersName, tfClientContactNo, tfClientEmail,
                tfClientAddress, tfClientLTOID, tfClientLTOAddress,
                tfCoClientName, tfCoClientGender, tfCoClientCitezenship, tfCoClientBirthplace, tfCoClientOccupation,
                tfCoClientTinNo, tfCoClientFathersName, tfCoClientMothersName, tfCoClientContactNo, tfCoClientEmail,
                tfCoClientAddress, tfCoClientLTOID, tfCoClientLTOAddress);

        poMotorcycleFields = Arrays.asList(tfMCModel, tfMCColor, tfMCEngineNo, tfMCFrameNo, tfMCWarrantyNo, tfMCKeyCode,
                tfMCRemarks, tfMCUnitPrice, tfPomoCode, tfMCRegisAmt, tfMCInsurAmt, tfPromoDisc, tfPromoAddDisc, tfFreight);

        poRegistrationFields = Arrays.asList(tfRegEngineNo, tfRegFrameNo, tfRegAmount,
                tfInsurYear, tfInsurPayer, tfInsurAmt, tfRegInsurType, tfInsurType);

        poGiveawayFields = Arrays.asList(tfGawayBarrcode, tfGawayDesc, tfGawayRemarks, tfGawayGiven, tfGawayIssued, tfGawayQty);

        poPaymentFields = Arrays.asList(tfPaymentCreditAdvisor, tfPaymentDrNo, tfPaymentTerm, tfPaymentTotal,
                tfPaymentAdvances, tfPaymentCwt, tfPaymentAmountPaid, tfPaymentBalance);

        poAllFields = new ArrayList<>();
        poAllFields.addAll(poMasterFields);
        poAllFields.addAll(poClientFields);
        poAllFields.addAll(poMotorcycleFields);
        poAllFields.addAll(poRegistrationFields);
        poAllFields.addAll(poGiveawayFields);
        poAllFields.addAll(poPaymentFields);

        poCombos = Arrays.asList(cmbClientType, cmbVehicleType, cmbRegistrationType,
                cmbPaymentTranType, cmbPaymentForm,
                cmbPaymentCWT, cmbAttachmentType);

        poCheckBoxes = Arrays.asList(cbRegIsCharge, cbInsurIsCharge, chkPaymentHasPromo);

        poDatePickers = Arrays.asList(dpTransDate, dpClientBday, dpCoClientBirthday, dpPaymentDueDate);
        // disable="true" / editable="false" in the FXML: filled from the record, so the edit-mode binding skips them
        poReadOnlyDatePickers = Arrays.asList(dpTransDate, dpClientBday, dpCoClientBirthday);

        poSummaryFields = Arrays.asList(tfSummaryCustomer, tfSummaryTranType, tfSummaryPaymentForm, tfSummaryTerm,
                tfSummaryDueDate, tfSummaryUnitPrice, tfSummaryDiscount, tfSummaryAddDiscount, tfSummaryFreight,
                tfSummaryRegistration, tfSummaryInsurance, tfSummaryTotal, tfSummaryGiveaways, tfSummaryVatSales,
                tfSummaryVatAmount, tfSummaryNonVatSales, tfSummaryZeroVatSales, tfSummaryCwt, tfSummaryAdvances,
                tfSummaryAmountPaid, tfSummaryBalance);
    }

    /**
     * Attaches the key press and lost focus handlers to every grouped field in
     * one pass.
     */
    private void initFieldHandlers() {
        for (TextInputControl txtInput : poAllFields) {
            txtInput.setOnKeyPressed(this::txtField_KeyPressed);
            txtInput.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    poEditUnit = (pnUnitRow >= 1 && pnUnitRow <= unit_data.size()) ? unit_data.get(pnUnitRow - 1) : null;
                    poEditGaway = selectedGiveaway();
                    poEditReg = poSectionReg;
                    poEditIns = poSectionIns;
                    if (txtInput == tfPomoCode) {
                        psPromoAtFocus = tfPomoCode.getText() == null ? "" : tfPomoCode.getText();
                    }
                } else {
                    txtField_LostFocus(txtInput);
                }
            });
        }
    }
    /** Moves focus to the next focusable control, as if Tab was pressed. */
    private void focusNext(Node fnNode) {
        fnNode.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "\t", KeyCode.TAB,
                false, false, false, false));
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextInputControl txtInput = (TextInputControl) event.getSource();
        String lsID = txtInput.getId();
        String lsValuse = txtInput.getText();
        try {
            switch (event.getCode()) {
                case ENTER:
                    if (!(txtInput instanceof TextArea)) {   // a TextArea needs Enter for new lines
                        event.consume();
                        focusNext(txtInput);
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfClientName":
                            int lnClientType = cmbClientType.getSelectionModel().getSelectedIndex();
                            if (lnClientType != 0 && lnClientType != 1) {
                                ShowMessageFX.Warning("Please select valid client type.", "Warning", null);
                                cmbClientType.requestFocus();
                                break;
                            }
                            oSalesController.MCSales().setClientType(String.valueOf(lnClientType));
                            poJSON = oSalesController.MCSales().searchClient(txtInput.getText(), true);
                            if (hasError(poJSON)) {
                                break;
                            }
                            LoadMaster();       // the search may have set the source (tfSourceNo / tfSourceDesc), status, salesman
                            LoadClientInfo();   // also fills taCustomerName
                            // the search may have loaded a source quotation / reservation into the model:
                            // forget the previous client's selected rows so the table reloads from the first row
                            pnUnitRow = -1;
                            pnGawayRow = -1;
                            loadTableMCItem();
                            loadTableGiveaways();
                            loadTableRegAndInsur();
                            loadPaymentRecord();   // a source quotation also sets the payment form, term, advances...
                            break;
                        case "tfCoClientName":
                            oSalesController.MCSales().getMaster().CoClient().setClientType("0");
                            poJSON = oSalesController.MCSales().searchCoClient(txtInput.getText(), false);
                            if (hasError(poJSON)) {
                                break;
                            }
                            LoadCoClientInfo();
                            break;
                        case "tfMCEngineNo":
                        case "tfMCFrameNo":
                        case "tfMCModel":
                        case "tfMCColor":
                            // search type: 0 = engine no, 1 = frame no, 2 = model, 3 = color
                            int lnSearchType = "tfMCEngineNo".equals(lsID) ? 0
                                    : "tfMCFrameNo".equals(lsID) ? 1
                                    : "tfMCModel".equals(lsID) ? 2
                                    : "tfMCColor".equals(lsID) ? 3 : 0;

                            if (pnUnitRow < 1) {   // nothing selected yet: use the last (blank) row
                                pnUnitRow = Math.max(1, oSalesController.MCSales().getDetailCount());
                            }

                            poJSON = oSalesController.MCSales().searchDetailBySerial(pnUnitRow, lsValuse, lnSearchType);
                            if (hasError(poJSON)) {
                                return;
                            }

                            // the search may not copy the selling price onto the detail: do it here when it is still 0
                            // (getUnitPrice() throws ClassCastException while the stored value is still a blank String, so read it with dbl())
                            final Model_Sales_Detail loDet = oSalesController.MCSales().getDetail(pnUnitRow);
                            if (dbl(() -> loDet.getUnitPrice()) == 0) {
                                double lnSelling = dbl(() -> loDet.Inventory().getSellingPrice());
                                loDet.setUnitPrice(lnSelling);   // TODO(model): check setter name / type; this also replaces the blank String with a Number
                            }

                            // the fields are filled by loadTableMCItem() -> succeeded() -> loadUnitRecord(pnUnitRow)
                            loadTableMCItem();
                            loadTableRegAndInsur();
                            break;

                        case "tfPomoCode":
                            if (pnUnitRow < 1) {
                                ShowMessageFX.Warning("Please select a unit first.", "Warning", null);
                                return;
                            }
                            pbSearching = true;
                            try {
                                poJSON = oSalesController.MCSales().searchSalesDetailPromotion(pnUnitRow, lsValuse, false);
                            } finally {
                                pbSearching = false;
                            }
                            if (hasError(poJSON)) {
                                loadUnitRecord(pnUnitRow);
                                return;
                            }

                            psPromoAtFocus = oSalesController.MCSales().getDetail(pnUnitRow).PromotionMaster().getPromoDescription();
                            tfPomoCode.setText(psPromoAtFocus);
                            tfFreight.setText(money(dbl(() -> oSalesController.MCSales().getDetail(pnUnitRow).getFreight())));
                            loadTableMCItem();          // the promo can change discount / freight
                            loadTableGiveaways();       // ...add giveaways
                            // no loadTableRegAndInsur(): the promo only ticks the Charge boxes, it does not show rows
                            applyPromoFlags(selectedUnit());
                            break;

                        case "tfGawayBarrcode":
                        case "tfGawayDesc":
                            boolean lbByCode = "tfGawayBarrcode".equals(lsID);
                            Model_Sales_Giveaways loGawayRow = selectedGiveaway();
                            if (loGawayRow == null && !gaway_data.isEmpty()) {
                                loGawayRow = gaway_data.get(gaway_data.size() - 1);   // nothing selected: use the last (blank) row
                            }
                            if (loGawayRow == null) {
                                break;
                            }
                            poJSON = oSalesController.MCSales().searchSalesGiveAway(loGawayRow.getEntryNo(), lsValuse, lbByCode);
                            if (hasError(poJSON)) {
                                loadUnitRecord(pnUnitRow);
                                return;
                            }
                            loadTableGiveaways();   // store the searched giveaway in the table
                            break;
                        case "tfPaymentTerm":
                            // MCSales has no term search: the master keeps a term CODE (setTermCode), typed in this field
                            poJSON = oSalesController.MCSales().SearchTerm(lsValuse, false);
                            if (hasError(poJSON)) {
                                restoreTermText();
                                return;
                            }
                            restoreTermText();    // shows the searched term's description
                            applyTermDueDate();   // due date = transaction date + the term's days
                            loadSummary();        // the summary shows the due date too
                            break;
                        case "tfSalesman":
                            // the salesman can be searched with F3 on every transaction, with or without a source
                            if (!canEdit()) {
                                break;
                            }
                            poJSON = oSalesController.MCSales().searchSalesMan(lsValuse, false);
                            if (hasError(poJSON)) {
                                break;
                            }
                            LoadMaster();          // shows the searched salesman in tfSalesman
                            loadPaymentRecord();   // the credit advisor follows the salesman
                            break;
                        case "tfSourceNo":
                        case "tfSourceDesc":
                            if (!canEdit()) {
                                break;
                            }
                            // searching from the description field is by description (byCode = false); from the source no it is by code
                            boolean lbSourceByCode = !"tfSourceDesc".equals(lsID);
                            poJSON = oSalesController.MCSales().searchClientSource(txtInput.getText(), lbSourceByCode);
                            if (hasError(poJSON)) {
                                break;
                            }
                            LoadMaster();       // the search may have set the source (tfSourceNo / tfSourceDesc), status, salesman
                            LoadClientInfo();   // also fills taCustomerName
                            // the search may have loaded a source quotation / reservation into the model:
                            // forget the previous client's selected rows so the table reloads from the first row
                            pnUnitRow = -1;
                            pnGawayRow = -1;
                            loadTableMCItem();
                            loadTableGiveaways();
                            loadTableRegAndInsur();
                            loadPaymentRecord();   // a source quotation also sets the payment form, term, advances...
                            break;
                        case "tfPaymentCreditAdvisor":
                            // the credit advisor is the salesman of the transaction
                            poJSON = oSalesController.MCSales().searchSalesMan(lsValuse, false);
                            if (hasError(poJSON)) {
                                break;
                            }
                            loadPaymentRecord();
                            break;
                        case "tfRegInsurType":
                            if (pnUnitRow < 1) {
                                ShowMessageFX.Warning("Please select a unit first.", "Warning", null);
                                return;
                            }
                            // MCSales reads the serial from the selected UNIT and adds a registration/insurance row for it
                            poJSON = oSalesController.MCSales().searchInsuranceTypeRegistration(pnUnitRow, lsValuse, false);
                            if (hasError(poJSON)) {
                                loadSummary();
                                return;
                            }
                            selectNewRegInsur(false);   // the registration the search just added becomes the selected row
                            applyDefaultCharge(poSelRegInsur, false);   // Registration checkbox follows the unit's promo
                            // TODO(model): default the amount here once Model_Insurance_Type / Model_Sales_Registration
                            // expose it (Model_Insurance_Type has no getAmount())
                            loadTableRegAndInsur();   // show the searched registration in the table
                            break;
                        case "tfInsurType":
                            if (pnUnitRow < 1) {
                                ShowMessageFX.Warning("Please select a unit first.", "Warning", null);
                                return;
                            }
                            // MCSales reads the serial from the selected UNIT and adds a registration/insurance row for it
                            poJSON = oSalesController.MCSales().searchInsuranceTypeInsurance(pnUnitRow, lsValuse, false);
                            if (hasError(poJSON)) {
                                loadSummary();
                                return;
                            }
                            selectNewRegInsur(true);   // the insurance the search just added becomes the selected row
                            applyDefaultCharge(poSelRegInsur, true);   // Insurance checkbox follows the unit's promo
                            loadTableRegAndInsur();   // show the searched insurance in the table
                            break;
                        case "tfInsurPayer": {
                            final Model_Sales_Insurance loInsPayer = poSectionIns;   // the insurance showing in the section
                            if (isBlankRegInsur(loInsPayer)) {
                                ShowMessageFX.Warning("Please select or search an insurance first.", "Warning", null);
                                return;
                            }
                            normalizeRegInsurRows();   // before any backend call that computes over the rows
                            // searchInsuranceTypePayee(n) reads getSalesInsurance(n): n is the insurance ENTRY NO.
                            poJSON = oSalesController.MCSales().searchInsuranceTypePayee(loInsPayer.getEntryNo(), lsValuse, false);
                            if (hasError(poJSON)) {
                                loadSummary();
                                return;
                            }
                            poSelRegInsur = loInsPayer;   // keep the same row selected
                            loadTableRegAndInsur();       // reloads the row; loadRegAndInsurRecord() fills tfInsurPayer
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    /**
     * Re-reads the due date from the master into dpPaymentDueDate (without
     * firing its change handler). Call it after anything that can change the
     * due date on the master, such as the term search. Same rule as
     * loadPaymentRecord(): outside Credit the due date is the transaction date,
     * and a due date before the transaction date is not kept.
     */
    private void refreshDueDate() {
        try {
            LocalDate ldTrans = toLocalDate(master().getTransactionDate());
            LocalDate ldDue = toLocalDate(master().getDueDate());
            if (canEdit() && (!pbCreditPayment.get() || ldDue == null
                    || (ldTrans != null && ldDue.isBefore(ldTrans)))) {
                ldDue = ldTrans;
                writeDueDate(ldDue);
            }
            setDate(dpPaymentDueDate, ldDue);
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    /**
     * Number of days of the term saved on the master, 0 when it cannot be read.
     * The getter name on the term model was not provided, so the usual names
     * are tried by reflection; when none gives a number, every getter of the
     * term is printed as "[TERM] ..." lines so the real name can be put in
     * TERM_DAYS_GETTERS.
     */
    private static final String[] TERM_DAYS_GETTERS = {"getTermValue", "getTermDays", "getDays", "getNoOfDays", "getNumberOfDays", "getDueDays", "getTerm"};

    private int termDays() {
        Object loTerm;
        try {
            loTerm = master().Term();
        } catch (Exception ex) {
            System.out.println("[TERM] master().Term() threw " + ex);
            return 0;
        }
        if (loTerm == null) {
            System.out.println("[TERM] master().Term() is null (term code = '" + textOf(() -> master().getTermCode()) + "')");
            return 0;
        }
        for (String lsGetter : TERM_DAYS_GETTERS) {
            try {
                Object loVal = loTerm.getClass().getMethod(lsGetter).invoke(loTerm);
                if (loVal instanceof Number) {
                    return ((Number) loVal).intValue();
                }
                java.util.regex.Matcher loMatch = java.util.regex.Pattern.compile("\\d+").matcher(loVal == null ? "" : String.valueOf(loVal));
                if (loMatch.find()) {
                    return Integer.parseInt(loMatch.group());
                }
            } catch (Exception ex) {
                // no such getter: try the next one
            }
        }
        System.out.println("[TERM] no days getter found on " + loTerm.getClass().getName());
        for (java.lang.reflect.Method loMethod : loTerm.getClass().getMethods()) {
            if (loMethod.getName().startsWith("get") && loMethod.getParameterCount() == 0
                    && loMethod.getDeclaringClass() != Object.class) {
                try {
                    System.out.println("[TERM]   " + loMethod.getName() + "() = " + loMethod.invoke(loTerm));
                } catch (Exception ex) {
                    System.out.println("[TERM]   " + loMethod.getName() + "() threw " + ex.getCause());
                }
            }
        }
        return 0;
    }

    /**
     * After a term search: due date = transaction date + the term's days,
     * written to the master and shown in the picker. This ignores the Credit
     * rule on purpose (the picker itself stays disabled outside Credit, but it
     * must show the term's date). When the days cannot be read, falls back to
     * refreshDueDate().
     */
    private void applyTermDueDate() {
        try {
            int lnDays = termDays();
            LocalDate ldTrans = toLocalDate(master().getTransactionDate());
            if (lnDays > 0 && ldTrans != null) {
                LocalDate ldDue = ldTrans.plusDays(lnDays);
                writeDueDate(ldDue);
                setDate(dpPaymentDueDate, ldDue);
                return;
            }
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
        }
        refreshDueDate();
    }

    private void txtField_LostFocus(TextInputControl txtInput) {
        if (!pbLoaded || pbSyncing) {
            return;   // ignore while the form is initializing or fields are set programmatically
        }
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) {
            return;   // nothing to write to the model when not editing
        }
        String lsID = txtInput.getId();
        String lsValue = txtInput.getText();
        try {
            switch (lsID) {
                case "tfMCRemarks":
                case "tfMCWarrantyNo":
                case "tfMCKeyCode": {
                    // write to the unit that was being edited (not the row that is selected now), and only when it
                    // is a real unit whose value changed: nothing here may create a row or reload for nothing
                    final Model_Sales_Detail loUnit = poEditUnit;
                    if (isBlankUnit(loUnit)) {
                        break;
                    }
                    String lsOld = "tfMCRemarks".equals(lsID) ? textOf(() -> loUnit.getNotes())
                            : "tfMCWarrantyNo".equals(lsID) ? textOf(() -> loUnit.getWarrantyNo())
                            : textOf(() -> loUnit.getKeyCode());
                    if (lsValue.equals(lsOld)) {
                        break;
                    }
                    poJSON = "tfMCRemarks".equals(lsID) ? loUnit.setNotes(lsValue)
                            : "tfMCWarrantyNo".equals(lsID) ? loUnit.setWarrantyNo(lsValue)
                            : loUnit.setKeyCode(lsValue);
                    if (hasError(poJSON)) {
                        loadUnitRecord(pnUnitRow);
                        return;
                    }
                    loadTableMCItem();
                    break;
                }
                case "tfGawayQty": {
                    final Model_Sales_Giveaways loGaway = poEditGaway;   // the giveaway that was being edited
                    if (isBlankGiveaway(loGaway)) {
                        break;   // no giveaway chosen on this row yet: nothing to write
                    }
                    int lnQty = parseInt(lsValue.replace(",", ""));   // blank / invalid -> 0
                    if (String.valueOf(lnQty).equals(numText(() -> loGaway.getQuantity()))) {
                        break;   // unchanged
                    }
                    poJSON = loGaway.setQuantity(lnQty);
                    if (hasError(poJSON)) {
                        loadSummary();
                        break;
                    }
                    loadTableGiveaways();
                    break;
                }
                case "tfRegAmount": {
                    // write to the registration that was showing in the section when the field got focus
                    final Model_Sales_Registration loReg = poEditReg;
                    if (isBlankRegInsur(loReg)) {
                        break;   // no registration in the section yet
                    }
                    double lnRegAmt = parseDouble(lsValue);
                    if (lnRegAmt == dbl(() -> loReg.getInsuranceAmount())) {
                        break;   // unchanged
                    }
                    poJSON = loReg.setInsuranceAmount(lnRegAmt);
                    if (hasError(poJSON)) {
                        loadSummary();
                        return;
                    }
                    loadTableRegAndInsur();   // refreshes the Amount column and re-formats the field
                    break;
                }
                case "tfInsurAmt":
                case "tfInsurYear": {
                    // write to the insurance that was showing in the section when the field got focus
                    final Model_Sales_Insurance loIns = poEditIns;
                    if (isBlankRegInsur(loIns)) {
                        break;   // no insurance in the section yet
                    }
                    if ("tfInsurAmt".equals(lsID)) {
                        double lnInsAmt = parseDouble(lsValue);
                        if (lnInsAmt == dbl(() -> loIns.getInsuranceAmount())) {
                            break;   // unchanged
                        }
                        poJSON = loIns.setInsuranceAmount(lnInsAmt);   // ASSUMED setter
                    } else {
                        int lnInsYear = parseInt(lsValue);
                        if (lnInsYear == (int) dbl(() -> loIns.getInsuranceYear())) {
                            break;   // unchanged
                        }
                        poJSON = loIns.setInsuranceYear(lnInsYear);    // ASSUMED setter
                    }
                    if (hasError(poJSON)) {
                        loadSummary();
                        return;
                    }
                    loadTableRegAndInsur();   // refresh the Amount / Year columns
                    break;
                }
                case "tfPaymentTerm":
                    // The term is chosen ONLY with F3 (SearchTerm sets the term code on the master and the field shows its
                    // DESCRIPTION), so typed text is never a code.
                    //  - field emptied (blank / null): the term is cleared on the master, the previous value is NOT brought back
                    //  - field has text: it was not searched, so the saved term's description is put back
                    if (!pbTermPayment.get()) {
                        break;   // the field is disabled unless the payment form is Term
                    }
                    if (lsValue == null || lsValue.trim().isEmpty()) {
                        if (!textOf(() -> master().getTermCode()).trim().isEmpty()) {
                            poJSON = master().setTermCode("");   // same "no term" value cmb_Changed() uses
                            if (hasError(poJSON)) {
                                restoreTermText();
                                return;
                            }
                        }
                        tfPaymentTerm.clear();
                        refreshDueDate();
                    } else {
                        restoreTermText();
                    }
                    break;
                case "tfPomoCode": {
                    if (pbSearching) {
                        break;
                    }
                    final Model_Sales_Detail loUnit = poEditUnit;
                    if (isBlankUnit(loUnit)) {
                        break;
                    }
                    String lsText = lsValue == null ? "" : lsValue.trim();
                    if (!lsText.isEmpty()) {
                        if (!lsText.equals(psPromoAtFocus.trim())) {
                            loadUnitRecord(pnUnitRow);
                        }
                        break;
                    }
                    if (psPromoAtFocus.trim().isEmpty()) {
                        break;
                    }

                    // removes the promo's giveaways AND clears promo id / discount / add. discount / freight
                    poJSON = oSalesController.MCSales().removeSalesDetailPromotion(pnUnitRow);
                    if (hasError(poJSON)) {
                        loadUnitRecord(pnUnitRow);
                        return;
                    }

                    tfPromoAddDisc.setText(money(0.00));
                    tfPromoDisc.setText(money(0.00));
                    tfFreight.setText(money(0.00));
                    psPromoAtFocus = "";

                    pnGawayRow = -1;            // the selected giveaway row may no longer exist
                    loadTableMCItem();
                    loadTableGiveaways();
                    loadTableRegAndInsur();
                    break;
                }
                case "tfPromoDisc":
                case "tfPromoAddDisc":
                case "tfFreight": {
                    // write to the unit that was being edited (not the row selected now), only when it is a real unit and the value changed
                    final Model_Sales_Detail loUnit = poEditUnit;
                    if (isBlankUnit(loUnit)) {
                        break;
                    }
                    double lnNew = parseDouble(lsValue);   // blank / invalid -> 0, commas removed

                    if ("tfPromoDisc".equals(lsID)) {
                        if (lnNew < 0 || lnNew > 100) {
                            ShowMessageFX.Warning("Discount rate must be between 0 and 100.", "Warning", null);
                            loadUnitRecord(pnUnitRow);   // put the old value back
                            return;
                        }
                        if (lnNew == dbl(() -> loUnit.getDiscount())) {
                            break;
                        }
                        poJSON = loUnit.setDiscount(lnNew);          // ASSUMED setter
                    } else if ("tfPromoAddDisc".equals(lsID)) {
                        if (lnNew < 0) {
                            ShowMessageFX.Warning("Additional discount cannot be negative.", "Warning", null);
                            loadUnitRecord(pnUnitRow);
                            return;
                        }
                        if (lnNew == dbl(() -> loUnit.getAddDiscount())) {
                            break;
                        }
                        poJSON = loUnit.setAddDiscount(lnNew);       // ASSUMED setter
                    } else {
                        if (lnNew < 0) {
                            ShowMessageFX.Warning("Freight cannot be negative.", "Warning", null);
                            loadUnitRecord(pnUnitRow);
                            return;
                        }
                        if (lnNew == dbl(() -> loUnit.getFreight())) {
                            break;
                        }
                        poJSON = loUnit.setFreight(lnNew);           // ASSUMED setter
                    }
                    if (hasError(poJSON)) {
                        loadUnitRecord(pnUnitRow);
                        return;
                    }
                    loadTableMCItem();   // refreshes tblUnits (Net SRP, Discount, Add. Disc, Freight), re-formats the fields,
                    // then syncCwt()/recomputeTransaction() and loadSummary() run in its succeeded()
                    break;
                }
                case "tfPaymentCwt": {
                    if (!pbCwtYes.get()) {
                        break;   // field is disabled unless CWT = Yes
                    }
                    // the field holds the CWT PERCENTAGE: from 0.00 to 100.00 only
                    final String lsCwt = lsValue == null ? "" : lsValue.replace("%", "").replace(",", "").trim();
                    double lnCwt;
                    try {
                        lnCwt = lsCwt.isEmpty() ? 0.0 : Double.parseDouble(lsCwt);
                    } catch (NumberFormatException ex) {
                        lnCwt = -1.0;   // not a number: fails the range check below
                    }
                    if (Double.isNaN(lnCwt) || lnCwt < 0.0 || lnCwt > 100.0) {
                        ShowMessageFX.Warning("CWT must be from 0.00 to 100.00.", "Warning", null);
                        loadSummary();   // put the saved value back
                        return;
                    }
                    lnCwt = Math.round(lnCwt * 100.0) / 100.0;
                    if (Math.abs(lnCwt - pdCwtRate) < 0.005) {
                        break;   // unchanged
                    }
                    pdCwtRate = lnCwt;
                    recomputeTransaction();   // MCSales: master CWT amount = rate x (VAT sales + non-VAT sales)
                    loadTableMCItem();   // refreshes tblUnits; its succeeded() reloads the summary too
                    break;
                }
                case "tfGawayRemarks":
                    ShowMessageFX.Error(SalesTransactionStatic.MSG_TEXTFIELD_UNDER_DEVELOPMENT, pxeModuleName, null);
                    break;
                default:
                    break;
            }
            loadSummary();
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper(SalesTransactionStatic.LOG_SOURCE, System.getProperty(SalesTransactionStatic.LOG_PATH_PROPERTY) + SalesTransactionStatic.LOG_FILE_NAME);
            oSalesController = new SalesControllers(oApp, logwrapr);
            oSalesController.MCSales().initTransaction();
            oSalesController.SalesQoutation().setRecordStatus(SalesTransactionStatic.OPEN);
            oSalesController.SalesQoutation().setWithUI(true);
            oSalesController.MCSales().setIndustryID(psIndustryId);
            oSalesController.MCSales().setCompanyID(psCompanyId);
            oSalesController.MCSales().setCategoryID(psCategoryId);
            oSalesController.MCSales().setBranchCode(oApp.getBranchCode());
            Platform.runLater(() -> btnNew.fire());
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    private void initButton(int fnValue) {
        CustomCommonUtil.setVisible(false, btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnHistory, btnClose);
        CustomCommonUtil.setManaged(false, btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnHistory, btnClose);
        AnchorClient.setDisable(true);
        // AnchorAttach / AnchorAttach2 are NOT disabled here any more: they hold the viewer, the arrows and the list, which
        // must work on a saved transaction too. Add / Remove / type are bound to pbEditable in initAttachmentTab().
        AnchorMCPrice.setDisable(true);
        anchorReg.setDisable(true);
        anchorInsur.setDisable(true);
        anchorGaways.setDisable(true);
        AnchorPaymType.setDisable(true);
        switch (fnValue) {
            case EditMode.UNKNOWN:
                CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnHistory, btnClose);
                CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnHistory, btnClose);
                break;
            case EditMode.READY:
                CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnUpdate, btnApproved, btnVoid, btnHistory, btnClose);
                CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnUpdate, btnApproved, btnVoid, btnHistory, btnClose);
                break;
            case EditMode.ADDNEW:
            case EditMode.UPDATE:
                CustomCommonUtil.setVisible(true, btnSave, btnCancel);
                CustomCommonUtil.setManaged(true, btnSave, btnCancel);
                AnchorClient.setDisable(false);
                AnchorMCPrice.setDisable(false);
                anchorReg.setDisable(false);
                anchorInsur.setDisable(false);
                anchorGaways.setDisable(false);
                AnchorPaymType.setDisable(false);
                break;
            default:
                throw new IllegalStateException(SalesTransactionStatic.MSG_UNEXPECTED_VALUE + fnValue);
        }
        pbEditable.set(fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
    }

    private void ClickButton() {
        for (Button btn : new Button[]{btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnHistory, btnClose}) {
            btn.setOnAction(this::handleButtonAction);
        }
    }

    /**
     * Refreshes the whole form from the transaction that is loaded in MCSales
     * (after Browse, Update, Confirm, Void). The edit mode is read FIRST: the
     * tables add their blank trailing row only while editing.
     */
    private void reloadRecord() throws GuanzonException, SQLException {
        pnEditMode = oSalesController.MCSales().getEditMode();
        pdCwtRate = savedCwtRate();   // BEFORE anything can compute: in edit mode MCSales overwrites the CWT field
        pnUnitRow = -1;
        pnGawayRow = -1;
        pnRegInsurRow = -1;
        poSelRegInsur = null;
        LoadMaster();
        LoadClientInfo();
        try {
            LoadCoClientInfo();
        } catch (RuntimeException ex) {   // a transaction without a co-client must still load
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
        }
        loadTableMCItem();
        loadTableGiveaways();
        loadTableRegAndInsur();
        loadPaymentRecord();
        loadAttachmentRecords();   // downloads the attachments of the loaded transaction (background Task)
    }

    private void handleButtonAction(ActionEvent event) {
        String lsID = ((Button) event.getSource()).getId();
        try {
            switch (lsID) {
                case "btnNew":
                    ClearAll();                                   // clear the form and the table lists
                    oSalesController.MCSales().initTransaction(); // reset the model's detail / giveaway / reg-insur / attachment lists
                    pnEditMode = EditMode.ADDNEW;
                    poJSON = oSalesController.MCSales().newTransaction();
                    if (hasError(poJSON)) {
                        break;
                    }
                    oSalesController.MCSales().setIndustryID(psIndustryId);
                    oSalesController.MCSales().setCompanyID(psCompanyId);
                    oSalesController.MCSales().setCategoryID(psCategoryId);
                    oSalesController.MCSales().setBranchCode(oApp.getBranchCode());
                    LoadMaster();
                    loadTableMCItem();
                    loadTableGiveaways();
                    loadTableRegAndInsur();
                    loadPaymentRecord();
                    reloadAttachmentTable();   // newTransaction() emptied the attachment list: nothing to download
                    showAttachment(-1);
                    break;
                case "btnBrowse":
                    poJSON = oSalesController.MCSales().searchTransaction("", true, false);
                    if (hasError(poJSON)) {
                        break;
                    }
                    ClearAll();
                    reloadRecord();   // reads the edit mode first, then fills every section and tab
                    break;
                case "btnUpdate":
                    poJSON = oSalesController.MCSales().UpdateTransaction();
                    if (hasError(poJSON)) {
                        break;
                    }
                    reloadRecord();   // now UPDATE: the tables get their blank row, the payment fields become editable
                    break;
                case "btnSave":
                    String lsPaymentError = validatePayment();
                    if (lsPaymentError != null) {
                        ShowMessageFX.Warning(lsPaymentError, "Warning", null);
                        break;
                    }
                    recomputeTransaction();   // the master CWT field must hold the AMOUNT (not the rate) when it is saved
                    poJSON = oSalesController.MCSales().saveTransaction();
                    if (hasError(poJSON)) {
                        break;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), "Information", null);
                    ClearAll();                                       // clears the fields and the observable lists
                    oSalesController.MCSales().initTransaction();
                    btnNew.fire();
                    break;
                case "btnApproved":   // "Confirm"
                    if (!ShowMessageFX.YesNo("Do you want to confirm this transaction?", pxeModuleName, null)) {
                        break;
                    }
                    poJSON = oSalesController.MCSales().CloseTransaction();
                    if (hasError(poJSON)) {
                        break;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), "Information", null);
                    reloadRecord();   // MCSales re-opened the transaction
                    break;
                case "btnVoid":
                    if (!ShowMessageFX.YesNo("Do you want to void this transaction?", pxeModuleName, null)) {
                        break;
                    }
                    poJSON = oSalesController.MCSales().VoidTransaction();
                    if (hasError(poJSON)) {
                        break;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), "Information", null);
                    reloadRecord();
                    break;
                case "btnHistory":
                    String lsTransNo = oSalesController.MCSales().getMaster().getTransactionNo();
                    if (lsTransNo == null || lsTransNo.isEmpty()) {
                        ShowMessageFX.Warning("No transaction is loaded.", "Warning", null);
                        break;
                    }
                    oSalesController.MCSales().ShowStatusHistory();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_CANCEL, pxeModuleName, null)) {
                        ClearAll();                                       // clears the fields and the observable lists
                        oSalesController.MCSales().initTransaction();
                        pnEditMode = EditMode.UNKNOWN;
                        lblStatus.setText("UNKNOWN");
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_CLOSE, pxeModuleName, null)) {

                        new unloadForm().unloadForm(ChildAnchorPane, oApp, pxeModuleName);
                    }
                    break;
            }
            initButton(pnEditMode);
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
            try {
                if (oApp != null) {
                    oApp.rollbackTrans();
                }
            } catch (SQLException ex1) {
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    // =========================================================================
    // Edit mode helpers & status
    // =========================================================================
    private boolean canEdit() {
        return pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE;
    }

    /**
     * The master (header) record of the transaction loaded in MCSales.
     */
    private Model_Sales_Master master() {
        return oSalesController.MCSales().getMaster();
    }

    // =========================================================================
    // Combo boxes
    // =========================================================================
    @SuppressWarnings("unchecked")
    private void setComboItems(ComboBox fcmb, String[] fasItems) {
        fcmb.getItems().setAll(Arrays.asList(fasItems));
        fcmb.getSelectionModel().clearSelection();
    }

    private String comboText(ComboBox fcmb) {
        Object loValue = fcmb.getSelectionModel().getSelectedItem();
        return loValue == null ? "" : loValue.toString();
    }

    private void initComboBoxes() {
        cmbRegistrationType.setDisable(true);
        setComboItems(cmbClientType, SalesTransactionStatic.CLIENT_TYPES);
        setComboItems(cmbVehicleType, SalesTransactionStatic.VEHICLE_TYPES);
        setComboItems(cmbRegistrationType, SalesTransactionStatic.REGISTRATION_TYPES);
        setComboItems(cmbPaymentTranType, SalesTransactionStatic.PAYMENT_TRAN_TYPES);
        setComboItems(cmbPaymentForm, SalesTransactionStatic.PAYMENT_FORMS);
        setComboItems(cmbPaymentCWT, SalesTransactionStatic.CWT_RATES);
        setComboItems(cmbAttachmentType, SalesTransactionStatic.ATTACHMENT_TYPES);

        for (final ComboBox lcmb : poCombos) {
            if (lcmb != cmbRegistrationType) {
                lcmb.disableProperty().bind(pbEditable.not());   // every combo except the registration type follows the edit mode
            }
            lcmb.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                if (lcmb == cmbPaymentTranType) {
                    pbCreditPayment.set(newIdx.intValue() == SalesTransactionStatic.PAYMENT_CREDIT);   // always keep this in sync
                } else if (lcmb == cmbPaymentForm) {
                    pbTermPayment.set(newIdx.intValue() == PAYMENT_FORM_TERM);   // always keep this in sync
                } else if (lcmb == cmbPaymentCWT) {
                    pbCwtYes.set(newIdx.intValue() == CWT_YES);   // always keep this in sync
                }
                if (!pbLoaded || pbSyncing) {
                    return;
                }
                cmb_Changed(lcmb, newIdx.intValue());
            });
        }

        cmbRegistrationType.setDisable(true);   // permanently disabled: display only
        // Term only applies when the payment form is Term (it is the term CODE; the due date follows the Credit type)
        tfPaymentTerm.disableProperty().bind(pbTermPayment.not());
        // CWT amount can only be typed when CWT = Yes (and while editing)
        tfPaymentCwt.disableProperty().bind(pbEditable.not().or(pbCwtYes.not()));
        tfPaymentCwt.editableProperty().bind(pbCwtYes);
    }

    private void cmb_Changed(ComboBox fcmb, int fnIndex) {
        if (!canEdit() || fnIndex < 0) {
            return;
        }
        try {
            if (fcmb == cmbPaymentTranType) {
                // not credit: due date = transaction date. Credit: keep a valid due date, otherwise start at the transaction date
                LocalDate ldTrans = toLocalDate(master().getTransactionDate());
                LocalDate ldDue = toLocalDate(master().getDueDate());   // ASSUMED getter
                if (fnIndex != SalesTransactionStatic.PAYMENT_CREDIT || ldDue == null
                        || (ldTrans != null && ldDue.isBefore(ldTrans))) {
                    ldDue = ldTrans;
                }
                writeDueDate(ldDue);
                setDate(dpPaymentDueDate, ldDue);
            }
            if (fcmb == cmbPaymentForm && fnIndex != PAYMENT_FORM_TERM) {
                tfPaymentTerm.clear();
                master().setTermCode("");   // not a term payment: no term code
            }

            // index -> value stored in the database
            String lsValue = String.valueOf(fnIndex);

            if (fcmb == cmbClientType) {
                poJSON = oSalesController.MCSales().getMaster().Client().setClientType(lsValue);          // placeholder
            } else if (fcmb == cmbPaymentTranType) {
                poJSON = oSalesController.MCSales().getMaster().setTransactionType(lsValue);
            } else if (fcmb == cmbPaymentForm) {
                poJSON = oSalesController.MCSales().getMaster().setPaymentForm(lsValue);
            } else if (fcmb == cmbPaymentCWT) {
                syncCwt();           // No: CWT amount is set to 0; Yes: keeps the amount, then totals are recomputed
                loadTableMCItem();   // re-reads the unit cells (Net SRP, Discount, etc.) after the recompute
                loadSummary();       // payment tab totals + transaction summary
                poJSON = null;
            } else if (fcmb == cmbAttachmentType) {
                setSelectedAttachmentType(fnIndex);   // only changes the attachment selected in the table (if any)
                poJSON = null;
            } else if (fcmb == cmbVehicleType) {
                poJSON = oSalesController.MCSales().getDetail(pnUnitRow).InventorySerial().setCondition(lsValue); // placeholder
            } else if (fcmb == cmbRegistrationType) {
                if (poSectionReg != null && !isBlankRegInsur(poSectionReg)) {
                    poJSON = poSectionReg.setRegistrationType(lsValue);
                } else {
                    // no registration in the section: undo the pick without re-triggering this handler
                    boolean lbPrev = pbSyncing;
                    pbSyncing = true;
                    try {
                        fcmb.getSelectionModel().clearSelection();
                    } finally {
                        pbSyncing = lbPrev;
                    }
                    ShowMessageFX.Warning("Please select or search a registration first.", "Warning", null);
                    return;
                }
            } else {
                poJSON = null;   // combos with no model field yet
            }

            if (hasError(poJSON)) {
                return;
            }
            if (fcmb == cmbRegistrationType) {
                loadTableRegAndInsur();   // keeps the same row selected (tracked by object) and refreshes the columns
            }
            loadSummary();
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    // =========================================================================
    // Check boxes
    // =========================================================================
    private void initCheckBoxes() {
        for (final CheckBox lcb : poCheckBoxes) {
            if (lcb != chkPaymentHasPromo) {
                lcb.disableProperty().bind(pbEditable.not());
            }
            lcb.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (!pbLoaded || pbSyncing) {
                    return;
                }
                cb_Changed(lcb, isSelected);
            });
        }
        chkPaymentHasPromo.setDisable(true);   // shows whether a unit has a promo: never ticked by hand
    }

    private void cb_Changed(CheckBox fcb, boolean fbSelected) {
        if (!canEdit()) {
            return;
        }
        try {
            if (fcb == cbRegIsCharge || fcb == cbInsurIsCharge) {
                // ticked = Charge, unticked = Free; written to the row shown in that section
                final boolean lbReg = fcb == cbRegIsCharge;
                final Model loRow = lbReg ? poSectionReg : poSectionIns;
                if (isBlankRegInsur(loRow)) {
                    // nothing in this section yet: undo the tick without triggering this handler again
                    revertCheckBox(fcb, !fbSelected);
                    ShowMessageFX.Warning(lbReg ? "Please select or search a registration first."
                            : "Please select or search an insurance first.", "Warning", null);
                    return;
                }
                String lsCharge = fbSelected ? CHARGE_YES : CHARGE_NO;
                poJSON = lbReg
                        ? ((Model_Sales_Registration) loRow).setChargeTo(lsCharge)
                        : ((Model_Sales_Insurance) loRow).setChargeTo(lsCharge);
                if (hasError(poJSON)) {
                    revertCheckBox(fcb, !fbSelected);   // keep the box in sync with the model
                    return;
                }
                poSelRegInsur = loRow;      // keep this row selected after the reload
                loadTableMCItem();          // recomputes totals, refreshes the Ins/Reg column and the summary
                loadTableRegAndInsur();     // Status column (Charge / Free)
            }
            loadSummary();
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    // =========================================================================
    // Date pickers
    // =========================================================================
    private String dateText(DatePicker fdp) {
        return fdp.getValue() == null ? "" : DATE_FORMAT.format(fdp.getValue());
    }

    /**
     * Sets a date programmatically without triggering the change handler.
     */
    private void setDate(DatePicker fdp, LocalDate fdValue) {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            fdp.setValue(fdValue);
        } finally {
            pbSyncing = lbPrev;
        }
    }

    private void initDatePickers() {
        StringConverter<LocalDate> loConverter = new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fdValue) {
                return fdValue == null ? "" : DATE_FORMAT.format(fdValue);
            }

            @Override
            public LocalDate fromString(String fsValue) {
                if (fsValue == null || fsValue.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(fsValue.trim(), DATE_FORMAT);
                } catch (DateTimeParseException ex) {
                    return null;
                }
            }
        };

        for (final DatePicker ldp : poDatePickers) {
            ldp.setConverter(loConverter);
            ldp.setPromptText(SalesTransactionStatic.DATE_PATTERN);
            if (!poReadOnlyDatePickers.contains(ldp)) {
                ldp.disableProperty().bind(pbEditable.not());
            }
            ldp.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (!pbLoaded || pbSyncing) {
                    return;
                }
                dp_Changed(ldp);
            });
        }

        // The due date only applies to a credit transaction
        dpPaymentDueDate.disableProperty().bind(pbEditable.not().or(pbCreditPayment.not()));

        // A birthday cannot be in the future (client and co-client)
        for (final DatePicker ldpBday : Arrays.asList(dpClientBday, dpCoClientBirthday)) {
            ldpBday.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate fdItem, boolean fbEmpty) {
                    super.updateItem(fdItem, fbEmpty);
                    setDisable(fbEmpty || fdItem.isAfter(LocalDate.now()));
                }
            });
        }
    }

    private void dp_Changed(DatePicker fdp) {
        if (!canEdit()) {
            return;
        }
        try {
            if ((fdp == dpPaymentDueDate || fdp == dpTransDate)
                    && dpPaymentDueDate.getValue() != null && dpTransDate.getValue() != null
                    && dpPaymentDueDate.getValue().isBefore(dpTransDate.getValue())) {
                ShowMessageFX.Error(SalesTransactionStatic.MSG_DUE_DATE_BEFORE_TRANS_DATE, pxeModuleName, null);
                setDate(dpPaymentDueDate, null);
            }
            if (fdp == dpPaymentDueDate) {
                writeDueDate(dpPaymentDueDate.getValue());
            }
            // TODO(model): the transaction date is set by the model (newTransaction); nothing to write for dpTransDate
            loadSummary();
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    // =========================================================================
    // Tables
    // NOTE: tblUnits, tblGawayItem and tblAttachments are bound directly to the model classes
    // (lambda cell factories, lookups pre-loaded in the Task). The invoice table still uses
    // ModelTableDetail ("index01"...) until its backend model exists.
    // =========================================================================
    /**
     * Remove column: a trash button per row, enabled only while editing.
     * fpBlank tells which rows are still empty; those rows get no button (null
     * = every row gets one). Neither the IntConsumer nor the Predicate may
     * throw checked exceptions: callers must handle them.
     */
    private <S> void initRemoveColumn(TableColumn<S, String> fcol, final IntConsumer fiAction, final Predicate<S> fpBlank) {
        if (fcol == null) {
            return;
        }
        fcol.setSortable(false);
        fcol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(""));
        fcol.setCellFactory(col -> new TableCell<S, String>() {
            private final Button btnRow = new Button();

            {
                FontAwesomeIconView loIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                loIcon.setGlyphSize(14);
                loIcon.setFill(javafx.scene.paint.Color.WHITE);

                btnRow.setGraphic(loIcon);
                btnRow.setStyle("-fx-background-color: #d9534f; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
                btnRow.setTooltip(new Tooltip("Remove"));
                btnRow.disableProperty().bind(pbEditable.not());
                btnRow.setOnAction(e -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        fiAction.accept(getIndex());
                    }
                });
            }

            @Override
            protected void updateItem(String fsItem, boolean fbEmpty) {
                super.updateItem(fsItem, fbEmpty);
                setText(null);
                S loRow = null;
                if (!fbEmpty && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                    loRow = getTableView().getItems().get(getIndex());
                }
                boolean lbBlank = loRow == null || (fpBlank != null && fpBlank.test(loRow));
                setGraphic(lbBlank ? null : btnRow);   // no remove button on an empty row
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void initTables() {
        initTableUnits();
        initTableRegAndInsur();
        initTableGiveaways();
        initTableInvoices();
        initTableAttachments();
    }

    private void initTableUnits() {
        if (tblColDetailQOH22 != null) {
            tblUnits.getColumns().remove(tblColDetailQOH22);   // unused in FXML
        }
        if (tblColDetailQOH23 != null) {
            tblUnits.getColumns().remove(tblColDetailQOH23);   // unused in FXML
        }
        tblColDetailNo.setCellValueFactory(cd -> cell(() -> cd.getTableView().getItems().indexOf(cd.getValue()) + 1));
        tblColDetailBrand.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> d.Inventory().Brand().getDescription()));
        tblColDetailModel.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> d.Inventory().Model().getDescription()));
        tblColDetailEngineNo.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> d.InventorySerial().getSerial01()));   // check getter
        tblColDetailFramNo.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> d.InventorySerial().getSerial02()));     // check getter
        tblColDetailVariant.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> d.Inventory().Variant().getDescription()));
        tblColDetailWarrantyNo.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> d.getWarrantyNo()));
        tblColDetailKeySerial.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> d.getKeyCode()));
        tblColDetailNetSRP.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> money(unitNet(d))));
        tblColDetailSRP.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> money(dbl(() -> d.getUnitPrice()))));
        tblColDetailInsReg.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> money(dbl(() -> d.getInsuranceAmount()))));
        tblColDetailFreight.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> money(dbl(() -> d.getFreight()))));
        tblColDetailDiscount.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> money(dbl(() -> d.getDiscount()))));
        tblColDetailAddDisc.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> money(dbl(() -> d.getAddDiscount()))));
        tblColDetailRemarks.setCellValueFactory(cd -> unitCell(cd.getValue(), d -> d.getNotes()));
        initRemoveColumn(tblColDetailQOH21, this::removeUnit, SalesTransactionsMCController::isBlankUnit);
        tblUnits.setItems(unit_data);
        tblUnits.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_UNITS));
        tblUnits.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            pnUnitRow = newIdx.intValue() >= 0 ? newIdx.intValue() + 1 : -1;   // model entry no. (1-based)
            if (pnUnitRow >= 1) {
                loadUnitRecord(pnUnitRow);
            }
        });
        tblUnits.setOnMouseClicked(this::setTblUnits_Clicked);
        lockColumnReordering(tblUnits);
    }

    /**
     * Registration and insurance list (Registration & Insurance tab).
     */
    private void initTableRegAndInsur() {
        tblRowRegAndInsurNo.setCellValueFactory(cd -> cell(() -> cd.getTableView().getItems().indexOf(cd.getValue()) + 1));
        tblRowRegAndInsurEngine.setCellValueFactory(cd -> cell(() -> regInsurEngine(cd.getValue())));
        tblRowRegAndInsurSource.setCellValueFactory(cd -> cell(() -> cd.getValue() instanceof Model_Sales_Registration ? "Registration" : "Insurance"));
        tblRowRegAndInsurDesc.setCellValueFactory(cd -> cell(() -> cd.getValue() instanceof Model_Sales_Insurance
                ? ((Model_Sales_Insurance) cd.getValue()).InsuranceType().getInsuranceTypeName()
                : regInsurTypeId(cd.getValue())));
        tblRowRegAndInsurAmount.setCellValueFactory(cd -> cell(()
                -> cd.getValue() instanceof Model_Sales_Insurance
                ? money(dbl(() -> ((Model_Sales_Insurance) cd.getValue()).getInsuranceAmount()))
                : cd.getValue() instanceof Model_Sales_Registration
                ? money(dbl(() -> ((Model_Sales_Registration) cd.getValue()).getInsuranceAmount()))
                : ""));
        tblRowRegAndInsurStat.setCellValueFactory(cd -> cell(()
                -> isBlankRegInsur(cd.getValue()) ? "" : (isChargeTo(cd.getValue()) ? "Charge" : "Free")));

        // IntConsumer cannot throw checked exceptions: removeRegAndInsur() declares them, so they are handled here
        initRemoveColumn(tblRowRegAndInsurAction,
                row -> {
                    try {
                        removeRegAndInsur(row);
                    } catch (GuanzonException | SQLException ex) {
                        Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
                    }
                },
                SalesTransactionsMCController::isBlankRegInsur);

        tblRegAndIsur.setItems(reginsur_data);
        tblRegAndIsur.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_REGINSUR));
        tblRegAndIsur.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            Model loRow = newIdx.intValue() >= 0 ? reginsur_data.get(newIdx.intValue()) : null;
            pnRegInsurRow = loRow == null ? -1 : regInsurEntryNo(loRow);   // model entry no., as the search methods expect
            poSelRegInsur = loRow;
            if (loRow != null) {
                try {
                    loadRegAndInsurRecord(loRow);
                } catch (GuanzonException | SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        lockColumnReordering(tblRegAndIsur);
    }

    private void initTableGiveaways() {
        tblRowGawayNo.setCellValueFactory(cd -> cell(() -> cd.getTableView().getItems().indexOf(cd.getValue()) + 1));
        tblRowGawayBarrcode.setCellValueFactory(cd -> gawayCell(cd.getValue(), g -> g.Inventory().getBarCode()));
        tblRowGawayDesc.setCellValueFactory(cd -> gawayCell(cd.getValue(), g -> g.Inventory().getDescription()));
        tblRowGawayRemarks.setCellValueFactory(cd -> new ReadOnlyStringWrapper(""));
        tblRowGawayQOH.setCellValueFactory(cd -> gawayCell(cd.getValue(), g -> g.InventoryMaster().getQuantityOnHand()));
        tblRowGawayGiven.setCellValueFactory(cd -> new ReadOnlyStringWrapper(isBlankGiveaway(cd.getValue()) ? "" : gawayNum(cd.getValue(), () -> cd.getValue().getGiven(), "nGivenxxx")));
        tblRowGawayIssued.setCellValueFactory(cd -> new ReadOnlyStringWrapper(isBlankGiveaway(cd.getValue()) ? "" : gawayNum(cd.getValue(), () -> cd.getValue().getIssued(), "nIssuedxx")));
        tblRowGawayQty.setCellValueFactory(cd -> new ReadOnlyStringWrapper(isBlankGiveaway(cd.getValue()) ? "" : gawayNum(cd.getValue(), () -> cd.getValue().getQuantity(), "nQuantity")));
        tblRowGawayStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper(""));
        initRemoveColumn(tblRowGawayAction11211, this::removeGiveaway, SalesTransactionsMCController::isBlankGiveaway);
        tblGawayItem.setItems(gaway_data);
        tblGawayItem.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_GIVEAWAYS));
        tblGawayItem.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            pnGawayRow = newIdx.intValue() >= 0 ? newIdx.intValue() + 1 : -1;   // model entry no. (1-based)
            if (pnGawayRow >= 1) {
                loadGiveawayRecord(pnGawayRow);
            }
        });
        tblGawayItem.setOnMouseClicked(this::setTblGawayItem_Clicked);
        lockColumnReordering(tblGawayItem);
    }

    private void initTableInvoices() {
        tblRowInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblRowInvoiceDate.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblRowInvoiceSource.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblRowInvoiceReceiptNo.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblRowInvoiceAmount.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblRowInvoiceStatus.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblInvoiceList.setItems(invoice_data);
        tblInvoiceList.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_INVOICES));
        lockColumnReordering(tblInvoiceList);
    }

    /**
     * Attachment list. The rows are Model_Transaction_Attachment objects, so
     * the columns read them with lambdas (PropertyValueFactory("index01") only
     * works with ModelTableDetail).
     */
    private void initTableAttachments() {
        tblRowNoAttachment.setCellValueFactory(cd -> cell(() -> cd.getTableView().getItems().indexOf(cd.getValue()) + 1));
        tblFileNameAttachment.setCellValueFactory(cd -> cell(() -> cd.getValue().getFileName()));
        tblAttachments.setItems(attach_data);
        tblAttachments.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_ATTACHMENTS));

        // keyboard / programmatic selection (ignored while displayAttachment() is moving the selection itself)
        tblAttachments.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (pbShowingAttach) {
                return;
            }
            if (newIdx.intValue() >= 0 && newIdx.intValue() < poAttachFiles.size()) {
                showAttachment(newIdx.intValue());
            }
        });

        // a click always refreshes the viewer, even on the row that is already selected
        tblAttachments.setOnMouseClicked(e -> {
            int lnIndex = tblAttachments.getSelectionModel().getSelectedIndex();
            if (lnIndex >= 0 && lnIndex < poAttachFiles.size()) {
                showAttachment(lnIndex);
            }
        });

        // right-click menu
        MenuItem loOpenLocation = new MenuItem("Open file location");
        loOpenLocation.setOnAction(e -> openAttachmentLocation());
        tblAttachments.setContextMenu(new ContextMenu(loOpenLocation));

        lockColumnReordering(tblAttachments);
    }

    /**
     * JavaFX 8 has no per-column "reorderable" flag, so the header row is told
     * to cancel any column drag. The listener is attached to each header row
     * only once.
     */
    private void lockColumnReordering(final TableView<?> ftbl) {
        ftbl.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            final TableHeaderRow header = (TableHeaderRow) ftbl.lookup(SalesTransactionStatic.HEADER_ROW_SELECTOR);
            if (header != null && !header.getProperties().containsKey(SalesTransactionStatic.REORDER_LOCK_KEY)) {
                header.getProperties().put(SalesTransactionStatic.REORDER_LOCK_KEY, Boolean.TRUE);
                header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    header.setReordering(false);
                });
            }
        });
    }

    private static boolean isTrueText(String fs) {
        return "true".equalsIgnoreCase(fs) || "1".equals(fs);
    }

    /**
     * true when the unit's promo (model or brand) includes insurance / registration.
     */
    private boolean promoHas(final Model_Sales_Detail foUnit, final boolean fbInsurance) {
        if (isBlankUnit(foUnit)) {
            return false;
        }
        String lsModel = fbInsurance ? textOf(() -> foUnit.PromotionModel().isInsuranceFree())
                : textOf(() -> foUnit.PromotionModel().isRegistrationFree());
        String lsBrand = fbInsurance ? textOf(() -> foUnit.PromotionBrand().isInsuranceFree())
                : textOf(() -> foUnit.PromotionBrand().isRegistrationFree());
        return isTrueText(lsModel) || isTrueText(lsBrand);
    }

    /**
     * The unit row currently selected in tblUnits (null when none).
     */
    private Model_Sales_Detail selectedUnit() {
        return (pnUnitRow >= 1 && pnUnitRow <= unit_data.size()) ? unit_data.get(pnUnitRow - 1) : null;
    }

    /**
     * Ticks the Charge check boxes from the unit's promo, only while the
     * section has no real row. Writes nothing to the model and adds nothing to
     * the table.
     */
    private void applyPromoFlags(Model_Sales_Detail foUnit) {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;   // cb_Changed() must not run
        try {
            if (isBlankRegInsur(poSectionReg)) {
                cbRegIsCharge.setSelected(promoHas(foUnit, false));
            }
            if (isBlankRegInsur(poSectionIns)) {
                cbInsurIsCharge.setSelected(promoHas(foUnit, true));
            }
        } finally {
            pbSyncing = lbPrev;
        }
    }

    /**
     * Default of the Charge check box for a row that a registration / insurance
     * search just added: the same rule the promo uses (promoHas) for the
     * section's OWN check box. Registration search -> cbRegIsCharge only,
     * insurance search -> cbInsurIsCharge only. The default is written to the
     * row (setChargeTo) so the box, the table Status column and the totals
     * all agree; the user can still change it by hand afterwards.
     */
    private void applyDefaultCharge(Model foRow, boolean fbInsurance) {
        if (isBlankRegInsur(foRow)) {
            return;
        }
        try {
            String lsCharge = promoHas(selectedUnit(), fbInsurance) ? CHARGE_YES : CHARGE_NO;
            JSONObject loJSON = fbInsurance
                    ? ((Model_Sales_Insurance) foRow).setChargeTo(lsCharge)
                    : ((Model_Sales_Registration) foRow).setChargeTo(lsCharge);
            if (hasError(loJSON)) {
                return;
            }
            loadTableMCItem();   // recompute totals / Ins-Reg column / summary with the default
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    private void revertCheckBox(CheckBox fcb, boolean fbValue) {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            fcb.setSelected(fbValue);
        } finally {
            pbSyncing = lbPrev;
        }
    }

    private void loadUnitRecord(int fnRow) {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            clearUnitItemTextFields();
            final Model_Sales_Detail loUnit = (fnRow >= 1 && fnRow <= unit_data.size()) ? unit_data.get(fnRow - 1) : null;
            if (loUnit != null && !isBlankUnit(loUnit)) {
                tfMCModel.setText(textOf(() -> loUnit.Inventory().Model().getDescription()));
                tfMCColor.setText(textOf(() -> loUnit.Inventory().Color().getDescription()));
                tfMCEngineNo.setText(textOf(() -> loUnit.InventorySerial().getSerial01()));   // check getter
                tfMCFrameNo.setText(textOf(() -> loUnit.InventorySerial().getSerial02()));    // check getter
                tfMCWarrantyNo.setText(textOf(() -> loUnit.getWarrantyNo()));
                tfMCKeyCode.setText(textOf(() -> loUnit.getKeyCode()));
                tfMCRemarks.setText(textOf(() -> loUnit.getNotes()));
                tfMCUnitPrice.setText(money(dbl(() -> loUnit.getUnitPrice())));
                tfPromoDisc.setText(money(dbl(() -> loUnit.getDiscount())));
                tfPromoAddDisc.setText(money(dbl(() -> loUnit.getAddDiscount())));
                tfFreight.setText(money(dbl(() -> loUnit.getFreight())));
                tfPomoCode.setText(textOf(() -> loUnit.PromotionMaster().getPromoDescription()));
                tfRegEngineNo.setText(tfMCEngineNo.getText());
                tfRegFrameNo.setText(tfMCFrameNo.getText());

                tfMCInsurAmt.setText(promoHas(loUnit, true) ? "Yes" : "No");
                tfMCRegisAmt.setText(promoHas(loUnit, false) ? "Yes" : "No");
                applyPromoFlags(loUnit);   // tick Charge from the promo, no rows added
                // TODO(model): tfMCRegisAmt, cmbVehicleType and the other combos / check boxes
            }
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        } finally {
            pbSyncing = lbPrev;
        }
    }

    /**
     * The row of the other type that belongs to the same unit (same serial id),
     * or null.
     */
    private Model findSibling(Model foRow, boolean fbInsurance) {
        String lsSerial = regInsurSerialId(foRow);
        if (lsSerial == null || lsSerial.isEmpty()) {
            return null;
        }
        for (Model loRow : reginsur_data) {
            boolean lbRightType = fbInsurance ? loRow instanceof Model_Sales_Insurance
                    : loRow instanceof Model_Sales_Registration;
            if (lbRightType && lsSerial.equals(regInsurSerialId(loRow))) {
                return loRow;
            }
        }
        return null;
    }

    /**
     * Called when a registration / insurance row is selected: fills BOTH
     * sections. The selected row fills its own section; the row of the other
     * type for the same unit (same serial id) fills the other one, when it
     * exists.
     */
    private void loadRegAndInsurRecord(final Model foRow) throws GuanzonException, SQLException {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        pbRegistrationRow.set(foRow instanceof Model_Sales_Registration);
        try {
            poSectionReg = foRow instanceof Model_Sales_Registration
                    ? (Model_Sales_Registration) foRow
                    : (Model_Sales_Registration) findSibling(foRow, false);
            poSectionIns = foRow instanceof Model_Sales_Insurance
                    ? (Model_Sales_Insurance) foRow
                    : (Model_Sales_Insurance) findSibling(foRow, true);

            // follows Model_Sales_Insurance.xml: sSerialID, sInsTypID, nInsAmtxx, nInsurYrx, cChargeTo
            tfRegEngineNo.setText(regInsurEngine(foRow));
            tfRegFrameNo.setText(regInsurFrame(foRow));

            // registration section
            tfRegInsurType.clear();
            tfRegAmount.clear();
            cmbRegistrationType.getSelectionModel().clearSelection();
            cbRegIsCharge.setSelected(false);
            if (poSectionReg != null) {
                final Model_Sales_Registration loReg = poSectionReg;
                tfRegInsurType.setText(regInsurTypeId(loReg));
                tfRegAmount.setText(money(dbl(() -> loReg.getInsuranceAmount())));
                cbRegIsCharge.setSelected(isChargeTo(loReg));
                // select the registration type on the combo; a blank type leaves the combo empty
                String lsRegType = textOf(() -> loReg.getRegistrationType()).trim();
                if (!lsRegType.isEmpty()) {
                    int lnRegType = parseInt(lsRegType);
                    if (lnRegType >= 0 && lnRegType < cmbRegistrationType.getItems().size()) {
                        cmbRegistrationType.getSelectionModel().select(lnRegType);
                    }
                }
            }

            // insurance section
            tfInsurType.clear();
            tfInsurYear.clear();
            tfInsurAmt.clear();
            tfInsurPayer.clear();
            cbInsurIsCharge.setSelected(false);
            if (poSectionIns != null) {
                final Model_Sales_Insurance loIns = poSectionIns;
                tfInsurType.setText(textOf(() -> loIns.InsuranceType().getInsuranceTypeName()));   // assumes getInsuranceTypeName()
                tfInsurYear.setText(numText(() -> loIns.getInsuranceYear()));
                tfInsurAmt.setText(money(dbl(() -> loIns.getInsuranceAmount())));
                cbInsurIsCharge.setSelected(isChargeTo(loIns));
                tfInsurPayer.setText(textOf(() -> loIns.InusrancePayeeClient().getCompanyName()));
            }
            applyPromoFlags(selectedUnit());   // a section without a row follows the promo
        } finally {
            pbSyncing = lbPrev;
        }
    }

    /**
     * Called when a giveaway row is selected.
     */
    private void loadGiveawayRecord(int fnRow) {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            clearGawayItemTextFields();
            final Model_Sales_Giveaways loGaway = (fnRow >= 1 && fnRow <= gaway_data.size()) ? gaway_data.get(fnRow - 1) : null;
            if (loGaway != null && !isBlankGiveaway(loGaway)) {
                tfGawayBarrcode.setText(String.valueOf(loGaway.Inventory().getBarCode()));
                tfGawayDesc.setText(String.valueOf(loGaway.Inventory().getDescription()));
                tfGawayGiven.setText(numText(() -> loGaway.getGiven()));
                tfGawayIssued.setText(numText(() -> loGaway.getIssued()));
                tfGawayQty.setText(numText(() -> loGaway.getQuantity()));
            }
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        } finally {
            pbSyncing = lbPrev;
        }
    }
    /** true when the giveaway row came from a source (quotation / reservation). */
    private static boolean gawayHasSource(Model_Sales_Giveaways fo) {
        if (fo == null) {
            return false;
        }
        for (String lsGetter : SalesTransactionStatic.GAWAY_SOURCE_GETTERS) {
            try {
                Object loVal = fo.getClass().getMethod(lsGetter).invoke(fo);
                return loVal != null && !String.valueOf(loVal).trim().isEmpty();
            } catch (NoSuchMethodException ex) {
                // try the next name
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    private static String textOf(Getter fg) {
        try {
            Object loVal = fg.get();
            return loVal == null ? "" : String.valueOf(loVal);
        } catch (Exception ex) {
            return "";
        }
    }

    private void clearUnitItemTextFields() {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            poMotorcycleFields.forEach(TextInputControl::clear);
            tfRegEngineNo.clear();
            tfRegFrameNo.clear();
        } finally {
            pbSyncing = lbPrev;
        }
    }

    /**
     * Click on a unit row: loads it into the motorcycle fields; double-click
     * moves focus to Key Code.
     */
    private void setTblUnits_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            int lnIndex = tblUnits.getSelectionModel().getSelectedIndex() + 1;   // model entry no. (1-based)
            Model_Sales_Detail loSelected = tblUnits.getSelectionModel().getSelectedItem();
            clearUnitItemTextFields();
            if (loSelected != null) {
                loadUnitRecord(lnIndex);
                if (event.getClickCount() == 2) {
                    tfMCKeyCode.requestFocus();
                }
            }
        }
    }

    private void clearGawayItemTextFields() {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            poGiveawayFields.forEach(TextInputControl::clear);
        } finally {
            pbSyncing = lbPrev;
        }
    }

    /**
     * Click on a giveaway row: loads it into the giveaway fields; double-click
     * moves focus to Qty.
     */
    private void setTblGawayItem_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            int lnIndex = tblGawayItem.getSelectionModel().getSelectedIndex();
            Model_Sales_Giveaways loSelected = tblGawayItem.getSelectionModel().getSelectedItem();
            pnGawayRow = lnIndex >= 0 ? lnIndex + 1 : -1;   // model entry no. (1-based)
            clearGawayItemTextFields();
            if (loSelected != null && pnGawayRow >= 1) {
                loadGiveawayRecord(pnGawayRow);   // also sets the enabled / disabled state of tfGawayQty
                if (event.getClickCount() == 2 && !tfGawayQty.isDisabled()) {
                    tfGawayQty.requestFocus();    // only rows without a source can be edited
                }
            } else {
                tfGawayQty.setDisable(false);
            }
        }
    }

    private void removeUnit(int fnRow) {
        if (!canEdit() || fnRow < 0 || fnRow >= unit_data.size()) {
            return;
        }
        if (!ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_REMOVE_UNIT, pxeModuleName, null)) {
            return;
        }
        // TODO(model): remove the detail from the transaction first, then reload the table from the model.
        unit_data.remove(fnRow);
        pnUnitRow = -1;
        loadSummary();
    }

    private void loadTableMCItem() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblUnits.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        final boolean lbEditing = pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE;
        final int lnSelectRow = pnUnitRow;
        final int lnSeq = ++pnUnitLoadSeq;   // a newer load makes this one stale

        Task<List<Model_Sales_Detail>> task = new Task<List<Model_Sales_Detail>>() {
            @Override
            protected List<Model_Sales_Detail> call() throws Exception {
                MCSales loSales = oSalesController.MCSales();
                // a COPY for the table: never add to the model's own list, or every load duplicates the rows
                List<Model_Sales_Detail> laRows = new ArrayList<Model_Sales_Detail>();

                int lnCount = loSales.getDetailList().size();
                if (lbEditing) {
                    try {
                        if (lnCount == 0 || !isBlankUnit(loSales.getDetailList().get(lnCount - 1))) {
                            // let the model create the blank row so it gets its trans no and entry no
                            int lnNextEntry = 0;
                            for (Model_Sales_Detail loTmp : loSales.getDetailList()) {
                                lnNextEntry = Math.max(lnNextEntry, loTmp.getEntryNo());
                            }
                            loSales.getDetail(lnNextEntry + 1);
                            lnCount = loSales.getDetailList().size();
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
                        lnCount = loSales.getDetailList().size();
                    }
                }

                for (int lnCtr = 0; lnCtr < lnCount; lnCtr++) {
                    Model_Sales_Detail loRow = loSales.getDetailList().get(lnCtr);
                    if (loRow == null) {
                        break;
                    }
                    if (!isBlankUnit(loRow)) {
                        // warm the lookups on this background thread so the cell factories
                        // (FX thread) read cached objects instead of hitting the database
                        try {
                            loRow.Inventory().Brand().getDescription();
                            loRow.Inventory().Model().getDescription();
                            loRow.Inventory().Variant().getDescription();
                            loRow.Inventory().Color().getDescription();
                            loRow.InventorySerial().getSerialId();
                        } catch (Exception ex) {
                            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
                        }
                    }
                    laRows.add(loRow);
                }
                return laRows;
            }

            @Override
            protected void succeeded() {
                if (lnSeq != pnUnitLoadSeq) {   // stale / cancelled
                    loading.progressIndicator.setVisible(false);
                    return;
                }
                unit_data.setAll(getValue());
                tblUnits.refresh();
                if (unit_data.isEmpty()) {
                    pnUnitRow = -1;
                    tblUnits.setPlaceholder(loading.placeholderLabel);
                } else {
                    JFXUtil.selectAndFocusRow(tblUnits, lnSelectRow >= 1 && lnSelectRow <= unit_data.size() ? lnSelectRow - 1 : 0);
                    pnUnitRow = tblUnits.getSelectionModel().getSelectedIndex() + 1;
                    loadUnitRecord(pnUnitRow);
                    tblUnits.toFront();
                }
                refreshRegInsurEngine();
                if (lbEditing) {
                    syncCwt();               // ADDNEW / UPDATE: CWT = No forces the amount to 0, then recomputes
                } else {
                    recomputeTransaction();  // READY (browse): recompute so the totals exist for display
                }
                loadSummary();   // totals come from the master (MCSales.computeTransaction())
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (lnSeq != pnUnitLoadSeq) {
                    return;
                }
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, getException());
                tblUnits.setPlaceholder(loading.placeholderLabel);
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();
    }

    /**
     * A COPY of the model's giveaway entries for the table. Reads the model's
     * list directly (getSalesGiveawaysList()) so nothing is created:
     * getSalesGiveaways(n) adds a new, empty entry whenever entry n does not
     * exist, which is what used to pile up blank rows on every load. Rows with
     * a barcode are listed in order; blank rows are skipped except one trailing
     * blank row (when asked).
     */
    private List<Model_Sales_Giveaways> giveawayList(boolean fbWithBlankRow) {
        List<Model_Sales_Giveaways> laList = new ArrayList<Model_Sales_Giveaways>();
        Model_Sales_Giveaways loBlank = null;
        for (Model_Sales_Giveaways loRow : oSalesController.MCSales().getSalesGiveawaysList()) {
            if (loRow == null) {
                continue;
            }
            if (isBlankGiveaway(loRow)) {
                loBlank = loRow;
            } else {
                laList.add(loRow);
            }
        }
        if (fbWithBlankRow && loBlank != null) {
            laList.add(loBlank);
        }
        return laList;
    }

    /**
     * The giveaway row currently selected in the table (null when none / out of
     * range).
     */
    private Model_Sales_Giveaways selectedGiveaway() {
        return (pnGawayRow >= 1 && pnGawayRow <= gaway_data.size()) ? gaway_data.get(pnGawayRow - 1) : null;
    }

    private void loadTableGiveaways() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblGawayItem.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        final boolean lbEditing = pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE;
        final int lnSelectRow = pnGawayRow;
        final int lnSeq = ++pnGawayLoadSeq;

        Task<List<Model_Sales_Giveaways>> task = new Task<List<Model_Sales_Giveaways>>() {
            @Override
            protected List<Model_Sales_Giveaways> call() throws Exception {
                MCSales loSales = oSalesController.MCSales();
                // the table gets a COPY; blank rows are never added to the table itself
                if (lbEditing) {
                    boolean lbHasBlank = false;
                    int lnLastEntry = 0;
                    for (Model_Sales_Giveaways loTmp : loSales.getSalesGiveawaysList()) {
                        if (loTmp == null) {
                            continue;
                        }
                        lbHasBlank = lbHasBlank || isBlankGiveaway(loTmp);
                        lnLastEntry = Math.max(lnLastEntry, loTmp.getEntryNo());   // TODO(model): check getter
                    }
                    if (!lbHasBlank) {
                        // exactly one blank row for the next giveaway, created by the model so it gets its trans no / entry no
                        loSales.getSalesGiveaways(lnLastEntry + 1);
                    }
                }
                List<Model_Sales_Giveaways> laRows = giveawayList(lbEditing);

                for (Model_Sales_Giveaways loRow : laRows) {
                    if (!isBlankGiveaway(loRow)) {
                        try {
                            loRow.Inventory().getBarCode();   // warm the lookups off the FX thread
                            loRow.Inventory().getDescription();
                            loRow.InventoryMaster().getQuantityOnHand();
                        } catch (Exception ex) {
                            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
                        }
                    }
                }
                return laRows;
            }

            @Override
            protected void succeeded() {
                if (lnSeq != pnGawayLoadSeq) {   // stale / cancelled
                    loading.progressIndicator.setVisible(false);
                    return;
                }
                gaway_data.setAll(getValue());
                tblGawayItem.refresh();
                if (gaway_data.isEmpty()) {
                    pnGawayRow = -1;
                    tblGawayItem.setPlaceholder(loading.placeholderLabel);
                } else {
                    JFXUtil.selectAndFocusRow(tblGawayItem,
                            lnSelectRow >= 1 && lnSelectRow <= gaway_data.size() ? lnSelectRow - 1 : 0);
                    pnGawayRow = tblGawayItem.getSelectionModel().getSelectedIndex() + 1;
                    loadGiveawayRecord(pnGawayRow);
                    tblGawayItem.toFront();
                }
                loadSummary();
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (lnSeq != pnGawayLoadSeq) {
                    return;
                }
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, getException());
                tblGawayItem.setPlaceholder(loading.placeholderLabel);
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();
    }

    /**
     * Loads tblRegAndIsur from the model: every registration followed by every
     * insurance entry that has a serial or an insurance type. Reads the model's
     * lists directly (nothing is created, so no blank rows pile up).
     */
    private void loadTableRegAndInsur() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblRegAndIsur.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        final int lnSelectRow = pnRegInsurRow;
        final int lnSeq = ++pnRegInsurLoadSeq;
        final Model loKeep = poSelRegInsur;   // captured BEFORE setAll() clears the selection (listener nulls poSelRegInsur)

        Task<List<Model>> task = new Task<List<Model>>() {
            @Override
            protected List<Model> call() throws Exception {
                normalizeRegInsurRows();   // rows added by the promo search have blank amounts too
                MCSales loSales = oSalesController.MCSales();
                List<Model> laRows = new ArrayList<Model>();   // a COPY for the table
                laRows.addAll(loSales.getSalesRegistrationsList());   // every registration, then every insurance
                laRows.addAll(loSales.getSalesInsurancesList());
                return laRows;
            }

            @Override
            protected void succeeded() {
                if (lnSeq != pnRegInsurLoadSeq) {   // stale / cancelled
                    loading.progressIndicator.setVisible(false);
                    return;
                }
                reginsur_data.setAll(getValue());
                tblRegAndIsur.refresh();
                if (reginsur_data.isEmpty()) {
                    pnRegInsurRow = -1;
                    poSelRegInsur = null;
                    poSectionReg = null;
                    poSectionIns = null;
                    pbSyncing = true;
                    try {
                        tfRegInsurType.clear();
                        tfRegAmount.clear();
                        tfInsurType.clear();
                        tfInsurYear.clear();
                        tfInsurAmt.clear();
                        cmbRegistrationType.getSelectionModel().clearSelection();
                        applyPromoFlags(selectedUnit());   // no rows: the boxes follow the promo
                    } finally {
                        pbSyncing = false;
                    }
                    tblRegAndIsur.setPlaceholder(loading.placeholderLabel);
                    pbRegistrationRow.set(false);
                } else {
                    // keep the selected row (found by object, not entry no.); a just-searched row is selected by selectNewRegInsur()
                    int lnIndex = loKeep != null ? reginsur_data.indexOf(loKeep) : -1;
                    if (lnIndex < 0) {
                        lnIndex = 0;
                    }
                    JFXUtil.selectAndFocusRow(tblRegAndIsur, lnIndex);
                    Model loSel = reginsur_data.get(tblRegAndIsur.getSelectionModel().getSelectedIndex());
                    poSelRegInsur = loSel;
                    pnRegInsurRow = regInsurEntryNo(loSel);
                    try {
                        loadRegAndInsurRecord(loSel);
                    } catch (GuanzonException | SQLException e) {
                        throw new RuntimeException(e);
                    }
                    tblRegAndIsur.toFront();
                }
                loadSummary();
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (lnSeq != pnRegInsurLoadSeq) {
                    return;
                }
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, getException());
                tblRegAndIsur.setPlaceholder(loading.placeholderLabel);
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();
    }

    /**
     * cChargeTo values: "1" = charged to the client, anything else = free.
     * ASSUMED: confirm the codes.
     */
    private static final String CHARGE_YES = "1";
    private static final String CHARGE_NO = "0";

    /**
     * true when the registration / insurance row is charged (getChargeTo() is a
     * String, cChargeTo).
     */
    private static boolean isChargeTo(Model fo) {
        String lsCharge = fo instanceof Model_Sales_Registration ? ((Model_Sales_Registration) fo).getChargeTo()
                : fo instanceof Model_Sales_Insurance ? ((Model_Sales_Insurance) fo).getChargeTo() : null;
        return lsCharge != null && CHARGE_YES.equals(lsCharge.trim());
    }

    /**
     * New insurance / registration rows hold a blank String in their numeric
     * fields, and every MCSales getter that returns such a row
     * (getSalesInsurance(n), getSales...List()) runs computeTransaction() over
     * all rows, which throws a ClassCastException. So the rows are NOT read
     * through those getters: the backend's own row lists are read by reflection
     * and the blank values are replaced with 0 through the base Model
     * getValue()/setValue(), which do not compute. Must run BEFORE any
     * getSalesInsurance / getSalesRegistration / list getter is called. (Field
     * names paSalesInsurance / paSalesRegistration are the ones declared in
     * MCSales.)
     */
    private void normalizeRegInsurRows() {
        MCSales loSales = oSalesController.MCSales();
        zeroBlankNumbers(rawRows(loSales, "paSalesInsurance"), "nInsAmtxx", 0.0);
        zeroBlankNumbers(rawRows(loSales, "paSalesInsurance"), "nInsurYrx", 0);
        zeroBlankNumbers(rawRows(loSales, "paSalesRegistration"), "nInsAmtxx", 0.0);   // ASSUMED: same column name as the insurance amount
    }

    /**
     * The backend's own row list (private field of MCSales), without going
     * through a computing getter.
     */
    @SuppressWarnings("unchecked")
    private List<Model> rawRows(MCSales foSales, String fsField) {
        try {
            java.lang.reflect.Field loField = MCSales.class.getDeclaredField(fsField);
            loField.setAccessible(true);
            Object loList = loField.get(foSales);
            return loList == null ? new ArrayList<Model>() : new ArrayList<Model>((List<Model>) loList);
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
            return new ArrayList<Model>();
        }
    }

    /**
     * Replaces a blank / non-numeric value in the given column with fdDefault
     * (a Number), row by row.
     */
    private void zeroBlankNumbers(List<Model> faRows, String fsColumn, Number fnDefault) {
        for (Model loRow : faRows) {
            if (loRow == null) {
                continue;
            }
            try {
                if (!(loRow.getValue(fsColumn) instanceof Number)) {
                    loRow.setValue(fsColumn, fnDefault);
                }
            } catch (Exception ex) {
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
            }
        }
    }

    /**
     * After a search added a row: the new row is the last entry of its list, so
     * select that one.
     */
    private void selectNewRegInsur(boolean fbInsurance) {
        normalizeRegInsurRows();   // must run before getSales...List(), which computes over every row
        MCSales loSales = oSalesController.MCSales();
        if (fbInsurance) {
            List<Model_Sales_Insurance> laList = loSales.getSalesInsurancesList();
            poSelRegInsur = laList.isEmpty() ? null : laList.get(laList.size() - 1);
        } else {
            List<Model_Sales_Registration> laList = loSales.getSalesRegistrationsList();
            poSelRegInsur = laList.isEmpty() ? null : laList.get(laList.size() - 1);
        }
    }

    private static int regInsurEntryNo(Model fo) {
        return fo instanceof Model_Sales_Registration ? ((Model_Sales_Registration) fo).getEntryNo()
                : fo instanceof Model_Sales_Insurance ? ((Model_Sales_Insurance) fo).getEntryNo() : -1;
    }

    private static String regInsurSerialId(Model fo) {
        return fo instanceof Model_Sales_Registration ? ((Model_Sales_Registration) fo).getSerialID()
                : fo instanceof Model_Sales_Insurance ? ((Model_Sales_Insurance) fo).getSerialID() : null;
    }

    private static String regInsurTypeId(Model fo) throws GuanzonException, SQLException {
        return fo instanceof Model_Sales_Registration ? ((Model_Sales_Registration) fo).InsuranceType().getInsuranceTypeName()
                : fo instanceof Model_Sales_Insurance ? ((Model_Sales_Insurance) fo).InsuranceType().getInsuranceTypeName() : null;
    }

    /**
     * A row with neither a serial nor an insurance type is still empty. Used as
     * a Predicate, so it must not throw checked exceptions: a row that cannot
     * be read counts as empty.
     */
    private static boolean isBlankRegInsur(Model fo) {
        if (fo == null) {
            return true;   // null check first: the getters below would throw on null
        }
        try {
            String lsSerial = regInsurSerialId(fo);
            String lsType = regInsurTypeId(fo);
            return (lsSerial == null || lsSerial.isEmpty()) && (lsType == null || lsType.isEmpty());
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
            return true;
        }
    }

    /**
     * Engine no. of the unit this row belongs to: found through the serial id
     * in the units table.
     */
    private String regInsurEngine(Model fo) {
        return regInsurSerialValue(fo, true);
    }

    private String regInsurFrame(Model fo) {
        return regInsurSerialValue(fo, false);
    }

    private String regInsurSerialValue(Model fo, final boolean fbEngine) {
        String lsSerial = regInsurSerialId(fo);
        if (lsSerial == null || lsSerial.isEmpty()) {
            return "";
        }
        for (final Model_Sales_Detail loUnit : unit_data) {
            if (lsSerial.equals(loUnit.getSerialID())) {
                return fbEngine ? textOf(() -> loUnit.InventorySerial().getSerial01())
                        : textOf(() -> loUnit.InventorySerial().getSerial02());
            }
        }
        return "";
    }

    // =========================================================================
    // Cell helpers for tables bound directly to the model classes
    // =========================================================================
    private interface Getter {

        Object get() throws Exception;
    }

    private interface UnitGetter {

        Object get(Model_Sales_Detail fo) throws Exception;
    }

    private interface GawayGetter {

        Object get(Model_Sales_Giveaways fo) throws Exception;
    }

    private static ObservableValue<String> cell(Getter fg) {
        try {
            Object loVal = fg.get();
            return new ReadOnlyStringWrapper(loVal == null ? "" : String.valueOf(loVal));
        } catch (Exception ex) {
            return new ReadOnlyStringWrapper("");
        }
    }

    private static ObservableValue<String> unitCell(Model_Sales_Detail fo, UnitGetter fg) {
        return (fo == null || isBlankUnit(fo)) ? new ReadOnlyStringWrapper("") : cell(() -> fg.get(fo));
    }

    private static ObservableValue<String> gawayCell(Model_Sales_Giveaways fo, GawayGetter fg) {
        return (fo == null || isBlankGiveaway(fo)) ? new ReadOnlyStringWrapper("") : cell(() -> fg.get(fo));
    }

    /**
     * A unit row counts as filled when it has a stock id OR a serial id,
     * because the serial search may set only the serial. TODO(model): check the
     * getSerialID() getter name.
     */
    private static boolean isBlankUnit(Model_Sales_Detail fo) {
        return fo == null
                || ((fo.getStockID() == null || fo.getStockID().isEmpty())
                && (fo.getSerialID() == null || fo.getSerialID().isEmpty()));
    }

    private static boolean isBlankGiveaway(Model_Sales_Giveaways fo) {
        return fo == null || fo.getStockID() == null || fo.getStockID().isEmpty();
    }

    /**
     * Net amount of a unit line, as MCSales.computeTransaction() builds it:
     * nDiscount is a RATE (%) applied to the line gross, nAddDiscount and
     * nFreight are amounts; the charged registration / insurance is nInsAmtxx.
     */
    private static double unitNet(Model_Sales_Detail fo) {
        double lnGross = dbl(() -> fo.getUnitPrice()) * dbl(() -> fo.getQuantity());
        double lnDisc = lnGross * dbl(() -> fo.getDiscount()) / 100.0;
        return lnGross - lnDisc - dbl(() -> fo.getAddDiscount())
                + dbl(() -> fo.getFreight()) + dbl(() -> fo.getInsuranceAmount());
    }

    /**
     * Number as double. Returns 0 when the getter throws (e.g.
     * ClassCastException: the model holds a blank String where a Number is
     * expected), or when the value is null / blank / not numeric.
     */
    private static double dbl(Getter fg) {
        try {
            Object loVal = fg.get();
            if (loVal == null) {
                return 0.0;
            }
            if (loVal instanceof Number) {
                return ((Number) loVal).doubleValue();
            }
            String lsVal = String.valueOf(loVal).replace(",", "").trim();
            return lsVal.isEmpty() ? 0.0 : Double.parseDouble(lsVal);
        } catch (Exception ex) {
            return 0.0;
        }
    }

    /**
     * Number getter that tolerates a model returning a String/blank where a
     * Number is expected.
     */
    private static String numText(Getter fg) {
        try {
            Object loVal = fg.get();
            return loVal == null || String.valueOf(loVal).isEmpty() ? "0" : String.valueOf(loVal);
        } catch (Exception ex) {   // e.g. ClassCastException: String cannot be cast to Number
            return "0";
        }
    }

    /**
     * Raw model field as text ("" when missing); used where a typed getter is
     * not available.
     */
    private static String gawayRaw(Model_Sales_Giveaways fo, String fsField) throws Exception {
        Object loVal = fo.getValue(fsField);
        return loVal == null ? "" : String.valueOf(loVal).trim();
    }

    /**
     * Typed getter first; if it fails (e.g. String stored where a Number is
     * expected) read the raw field; else "0".
     */
    private static String gawayNum(Model_Sales_Giveaways fo, Getter fg, String fsField) {
        try {
            Object loVal = fg.get();
            if (loVal != null && !String.valueOf(loVal).trim().isEmpty()) {
                return String.valueOf(loVal);
            }
        } catch (Exception ex) {
            // fall through to the raw field
        }
        try {
            String lsRaw = gawayRaw(fo, fsField);
            if (!lsRaw.isEmpty()) {
                return lsRaw;
            }
        } catch (Exception ex) {
            // fall through
        }
        return "0";
    }

    private static String money(double fn) {
        return String.format("%,.2f", fn);
    }

    // Helper methods to safely parse String results
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void removeRegAndInsur(int fnRow) throws GuanzonException, SQLException {
        if (!canEdit() || fnRow < 0 || fnRow >= reginsur_data.size()) {
            return;
        }
        if (!ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_REMOVE_REGINSUR, pxeModuleName, null)) {
            return;
        }
        Model loRow = reginsur_data.get(fnRow);
        if (!isBlankRegInsur(loRow)) {
            poJSON = loRow instanceof Model_Sales_Registration
                    ? oSalesController.MCSales().removeSalesRegistration(regInsurEntryNo(loRow)) // removes it and realigns the entry nos
                    : oSalesController.MCSales().removeSalesInsurance(regInsurEntryNo(loRow));
            if (hasError(poJSON)) {
                return;
            }
        }
        pnRegInsurRow = -1;
        poSelRegInsur = null;
        loadTableRegAndInsur();
    }

    private void removeGiveaway(int fnRow) {
        if (!canEdit() || fnRow < 0 || fnRow >= gaway_data.size()) {
            return;
        }
        if (!ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_REMOVE_GIVEAWAY, pxeModuleName, null)) {
            return;
        }
        Model_Sales_Giveaways loRow = gaway_data.get(fnRow);
        if (!isBlankGiveaway(loRow)) {
            poJSON = oSalesController.MCSales().removeSalesGiveAway(loRow.getEntryNo());   // removes it and realigns the entry nos
            if (hasError(poJSON)) {
                return;
            }
        }
        pnGawayRow = -1;
        loadTableGiveaways();
    }

    /**
     * The engine / frame come from unit_data, which loads in its own Task:
     * re-read them once the units are there.
     */
    private void refreshRegInsurEngine() {
        tblRegAndIsur.refresh();   // rebuilds the cells, so the Engine column re-reads unit_data
        if (poSelRegInsur != null) {
            boolean lbPrev = pbSyncing;
            pbSyncing = true;
            try {
                tfRegEngineNo.setText(regInsurEngine(poSelRegInsur));
                tfRegFrameNo.setText(regInsurFrame(poSelRegInsur));
            } finally {
                pbSyncing = lbPrev;
            }
        }
    }

    // =========================================================================
    // Payment tab
    // Reads / writes Model_Sales_Master. The totals are computed by MCSales.computeTransaction() (only while editing);
    // this tab only displays them. CWT: the combo is Yes / No, the master keeps the typed CWT AMOUNT.
    // =========================================================================
    /**
     * "" / invalid -> -1 (no selection); the master stores combo indexes as
     * text ("0", "1"...).
     */
    private static int indexOf(String fsCode) {
        if (fsCode == null || fsCode.trim().isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(fsCode.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void selectIndex(ComboBox fcmb, int fnIndex) {
        if (fnIndex >= 0 && fnIndex < fcmb.getItems().size()) {
            fcmb.getSelectionModel().select(fnIndex);
        } else {
            fcmb.getSelectionModel().clearSelection();
        }
    }

    /**
     * Runs MCSales.computeTransaction() (edit modes only) after the blank
     * numeric fields were fixed.
     */
    private void recomputeTransaction() {
        try {
            normalizeRegInsurRows();
            if (canEdit()) {
                // MCSales reads this field as the RATE: put the rate back first, or a previous compute's AMOUNT
                // would be used as the rate (the CWT would multiply on every call)
                master().setCWTAmount(pdCwtRate);
            }
            oSalesController.MCSales().computeTransaction();   // the master field now holds the CWT AMOUNT
            System.out.println("[PAY] computeTransaction OK, mode=" + pnEditMode);
        } catch (Throwable ex) {
            System.out.println("[PAY] computeTransaction FAILED, mode=" + pnEditMode);
            ex.printStackTrace();
        }
    }

    /**
     * CWT = No: the percentage is set to 0 (MCSales then computes a 0 amount). CWT = Yes: the typed percentage
     * is kept. The amount itself is never written here: MCSales calculates it from the percentage.
     */
    private void syncCwt() {
        if (!canEdit() || cmbPaymentCWT.getSelectionModel().getSelectedIndex() < 0) {
            return;
        }
        if (!pbCwtYes.get()) {
            pdCwtRate = 0.0;   // No: no CWT
        }
        recomputeTransaction();
    }

    /**
     * The percentage that the CWT amount SAVED on the master is of its VAT-exclusive base. Must be read
     * BEFORE anything computes in edit mode (MCSales then overwrites the field). Not rounded, so computing
     * it back gives the saved amount again.
     */
    private double savedCwtRate() {
        final Model_Sales_Master loMas = master();
        double lnBase = dbl(() -> loMas.getVATSales()) + dbl(() -> loMas.getNonVATSales());
        double lnAmount = dbl(() -> loMas.getCWTAmount());
        return (lnBase > 0 && lnAmount > 0) ? Math.min(100.0, lnAmount / lnBase * 100.0) : 0.0;
    }

    /**
     * Fills the whole Payment tab from the master. Called after New / Browse /
     * Update / Confirm / Void / advisor search.
     */
    private void loadPaymentRecord() {
        final Model_Sales_Master loMas = master();
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            selectIndex(cmbPaymentTranType, indexOf(textOf(() -> loMas.getTransactionType())));
            selectIndex(cmbPaymentForm, indexOf(textOf(() -> loMas.getPaymentForm())));

            // any CWT percentage above 0 means CWT = Yes (the master only keeps the amount: see pdCwtRate)
            selectIndex(cmbPaymentCWT, (pdCwtRate > 0 || (canEdit() && pbCwtYes.get())) ? CWT_YES : CWT_NO);

            tfPaymentTerm.setText(pbTermPayment.get() ? termText() : "");   // the field shows the term DESCRIPTION, like after F3

            try {
                LocalDate ldTrans = toLocalDate(loMas.getTransactionDate());
                LocalDate ldDue = toLocalDate(loMas.getDueDate());   // ASSUMED getter
                // New / Update: initialize it. Browse: show what is saved, write nothing
                if (canEdit() && (!pbCreditPayment.get() || ldDue == null
                        || (ldTrans != null && ldDue.isBefore(ldTrans)))) {
                    ldDue = ldTrans;
                    writeDueDate(ldDue);
                }
                setDate(dpPaymentDueDate, ldDue);
            } catch (Exception ex) {
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
            }
            tfPaymentDrNo.setText(tfReceipt.getText());   // DR No is a read-only mirror of the header
        } finally {
            pbSyncing = lbPrev;
        }
        loadSummary();   // also refreshes the totals
    }

    private void writeDueDate(LocalDate fdDue) throws Exception {
        poJSON = master().setDueDate(fdDue == null ? null : java.sql.Date.valueOf(fdDue));   // ASSUMED setter
        if (hasError(poJSON)) {
            return;
        }
    }

    /**
     * True when at least one real unit has a promo (the unit's promo id is
     * set).
     */
    private boolean hasPromo() {
        for (final Model_Sales_Detail loUnit : unit_data) {
            if (!isBlankUnit(loUnit) && !textOf(() -> loUnit.getPromoID()).trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sum of the CHARGED registration or insurance amounts, read from the
     * backend's own lists (no compute).
     */
    private double chargedTotal(boolean fbInsurance) {
        normalizeRegInsurRows();
        double lnSum = 0.0;
        for (Model loRow : rawRows(oSalesController.MCSales(), fbInsurance ? "paSalesInsurance" : "paSalesRegistration")) {
            if (!isChargeTo(loRow)) {
                continue;
            }
            lnSum += fbInsurance ? dbl(() -> ((Model_Sales_Insurance) loRow).getInsuranceAmount())
                    : dbl(() -> ((Model_Sales_Registration) loRow).getInsuranceAmount());
        }
        return lnSum;
    }

    /**
     * Fills the Payment tab totals and the transaction summary figures from the
     * master (computed by MCSales). Amount due already subtracts the advances
     * and the CWT, so: Net total = amount due + advances + CWT, Balance =
     * amount due - amount paid. ASSUMED business rule.
     */
    private void loadPaymentTotals() {
        if (canEdit()) {
            recomputeTransaction();   // last compute with the rate in place: the master then holds the right CWT amount and amount due
        }
        final Model_Sales_Master loMas = master();
        double lnTotal = dbl(() -> loMas.getTransactionTotal());
        double lnDisc = dbl(() -> loMas.getDiscount());
        double lnAddDisc = dbl(() -> loMas.getAddDiscount());
        double lnFreight = dbl(() -> loMas.getFreight());
        double lnDue = dbl(() -> loMas.getAmountDue());
        double lnAdvances = dbl(() -> loMas.getAdvances());
        double lnCwt = dbl(() -> loMas.getCWTAmount());
        double lnPaid = dbl(() -> loMas.getValue("nAmtPaidx"));   // raw column: MCSales never sets it (stays 0)

        // Browse (READY) fallback: the master's amount due is 0 (computeTransaction does not fill it outside edit mode),
        // so build every figure from the loaded units
        boolean lbHasUnits = false;
        for (Model_Sales_Detail d : unit_data) {
            if (!isBlankUnit(d)) {
                lbHasUnits = true;
                break;
            }
        }
        if (!canEdit() && lnDue == 0 && lbHasUnits) {
            lnTotal = 0;
            lnDisc = 0;
            lnAddDisc = 0;
            lnFreight = 0;
            for (final Model_Sales_Detail d : unit_data) {
                if (isBlankUnit(d)) {
                    continue;
                }
                double lnGross = dbl(() -> d.getUnitPrice()) * dbl(() -> d.getQuantity());
                lnTotal += lnGross;
                lnDisc += lnGross * dbl(() -> d.getDiscount()) / 100.0;
                lnAddDisc += dbl(() -> d.getAddDiscount());
                lnFreight += dbl(() -> d.getFreight());
            }
            lnDue = lnTotal - lnDisc - lnAddDisc + lnFreight
                    + chargedTotal(false) + chargedTotal(true) - lnAdvances - lnCwt;   // MCSales' amount due is net of the CWT
        }

        // MCSales' amount due already subtracts the advances AND the CWT: the total adds them back,
        // and the balance must not subtract the CWT a second time
        double lnNet = lnDue + lnAdvances + lnCwt;
        double lnBalance = lnDue - lnPaid;

        tfPaymentTotal.setText(money(lnNet));
        tfPaymentAdvances.setText(money(lnAdvances));
        tfPaymentCwt.setText(String.format(Locale.US, "%.2f", pdCwtRate));   // the PERCENTAGE; the amount is in tfSummaryCwt
        tfPaymentAmountPaid.setText(money(lnPaid));
        tfPaymentBalance.setText(money(lnBalance));

        tfSummaryUnitPrice.setText(money(lnTotal));
        tfSummaryDiscount.setText(money(lnDisc));
        tfSummaryAddDiscount.setText(money(lnAddDisc));
        tfSummaryFreight.setText(money(lnFreight));
        tfSummaryRegistration.setText(money(chargedTotal(false)));
        tfSummaryInsurance.setText(money(chargedTotal(true)));
        tfSummaryTotal.setText(money(lnNet));
        tfSummaryGiveaways.setText(String.valueOf(giveawayList(false).size()));
        tfSummaryVatSales.setText(money(dbl(() -> loMas.getVATSales())));
        tfSummaryVatAmount.setText(money(dbl(() -> loMas.getVATAmount())));
        tfSummaryNonVatSales.setText(money(dbl(() -> loMas.getNonVATSales())));
        tfSummaryZeroVatSales.setText(money(dbl(() -> loMas.getZeroVATSales())));
        tfSummaryCwt.setText(money(lnCwt));
        tfSummaryAdvances.setText(money(lnAdvances));
        tfSummaryAmountPaid.setText(money(lnPaid));
        tfSummaryBalance.setText(money(lnBalance));

        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            chkPaymentHasPromo.setSelected(hasPromo());
        } finally {
            pbSyncing = lbPrev;
        }
    }

    /**
     * Returns the first problem on the Payment tab, or null when it is fine to
     * save.
     */
    private String validatePayment() {
        if (cmbPaymentTranType.getSelectionModel().getSelectedIndex() < 0) {
            cmbPaymentTranType.requestFocus();
            return "Please select a transaction type.";
        }
        if (cmbPaymentForm.getSelectionModel().getSelectedIndex() < 0) {
            cmbPaymentForm.requestFocus();
            return "Please select a payment form.";
        }
        // the term is valid only when it was searched (F3): the master must hold a term code, not just typed text
        if (pbTermPayment.get() && textOf(() -> master().getTermCode()).trim().isEmpty()) {
            tfPaymentTerm.requestFocus();
            return "Please search and select a term (press F3 on the Term field).";
        }
        if (pbCreditPayment.get() && dpPaymentDueDate.getValue() == null) {
            dpPaymentDueDate.requestFocus();
            return "Please enter the due date.";
        }
        return null;
    }

    // =========================================================================
    // Attachment tab
    // Three parallel lists describe the active attachments, in the same order:
    //   attach_data (table rows) = poAttachFiles (files in the temp attachment folder) = poAttachModelRows (index in MCSales).
    // pnAttachRow is an index in THESE lists; MCSales is always called with poAttachModelRows.get(pnAttachRow).
    // =========================================================================
    private void initAttachmentTab() {
        // A rotated ImageView still reports its UNrotated size to the layout: a 90 degree turn made it taller than the
        // pane, which stretched the StackPane and pushed the rotate buttons out of view. A Group measures the ROTATED
        // bounds of its child, so the pane keeps its size.
        stackPane.setMinSize(0, 0);
        int lnViewIndex = stackPane.getChildren().indexOf(imageView);
        if (lnViewIndex >= 0) {
            Group loViewGroup = new Group();
            stackPane.getChildren().set(lnViewIndex, loViewGroup);   // takes the image view's place (and z-order)
            loViewGroup.getChildren().add(imageView);
            poViewGroup = loViewGroup;
            initImageZoom();
        }
        imageView.setPreserveRatio(true);
        // on a 90 / 270 degree turn the image's width is the pane's height (and the other way round)
        imageView.fitWidthProperty().bind(Bindings.when(pbQuarterTurn).then(stackPane.heightProperty()).otherwise(stackPane.widthProperty()).subtract(4));
        imageView.fitHeightProperty().bind(Bindings.when(pbQuarterTurn).then(stackPane.widthProperty()).otherwise(stackPane.heightProperty()).subtract(4));
        initRotateButtons();
        initAttachmentKeys();
        tfAttachmentNo.setEditable(false);

        btnAddAttachment.disableProperty().bind(pbEditable.not());
        btnRemoveAttachment.disableProperty().bind(pbEditable.not().or(Bindings.isEmpty(poAttachFiles)));

        btnAddAttachment.setOnAction(e -> addAttachment());
        btnRemoveAttachment.setOnAction(e -> removeAttachment());

        stackPane.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY && e.getClickCount() == 2) {
                openAttachmentLocation();
            }
        });
        btnArrowLeft.setOnAction(e -> {
            if (pnAttachRow > 0) {
                showAttachment(pnAttachRow - 1);
            }
        });
        btnArrowRight.setOnAction(e -> {
            if (pnAttachRow >= 0 && pnAttachRow < poAttachFiles.size() - 1) {
                showAttachment(pnAttachRow + 1);
            }
        });
        updateAttachmentArrows();
    }

    /**
     * The attachment type is kept as the combo index in a raw column (see
     * ATTACH_TYPE_COLUMN) AND in poAttachTypes.
     */
    private void setAttachmentType(Model_Transaction_Attachment foModel, int fnType) {
        if (foModel == null || fnType < 0) {
            return;
        }
        String lsFile = foModel.getFileName();
        if (lsFile != null) {
            poAttachTypes.put(lsFile.toLowerCase(), fnType);
        }

        // 1) look for a real setter on the model: setDocumentType / setDocType / setFileType ...
        for (java.lang.reflect.Method loMethod : foModel.getClass().getMethods()) {
            String lsName = loMethod.getName().toLowerCase();
            if (lsName.startsWith("set") && (lsName.contains("doc") || lsName.contains("type"))
                    && loMethod.getParameterCount() == 1
                    && loMethod.getParameterTypes()[0] == String.class) {
                try {
                    Object loResult = loMethod.invoke(foModel, String.valueOf(fnType));
                    System.out.println("Attachment type written with " + loMethod.getName() + " -> " + loResult);
                    return;
                } catch (Exception ex) {
                    Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, loMethod.getName(), ex);
                }
            }
        }

        // 2) no setter found: raw column, but report the failure instead of hiding it
        try {
            foModel.setValue(ATTACH_TYPE_COLUMN, String.valueOf(fnType));
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE,
                    "Attachment type NOT written. Setters on the model: " + listSetters(foModel), ex);
        }
    }

    private String listSetters(Object fo) {
        StringBuilder loSb = new StringBuilder();
        for (java.lang.reflect.Method m : fo.getClass().getMethods()) {
            if (m.getName().startsWith("set")) {
                loSb.append(m.getName()).append(' ');
            }
        }
        return loSb.toString();
    }

    private int getAttachmentType(final Model_Transaction_Attachment foModel) {
        if (foModel == null) {
            return -1;
        }
        int lnType = indexOf(textOf(() -> foModel.getValue(ATTACH_TYPE_COLUMN)));
        if (lnType >= 0) {
            return lnType;
        }
        String lsFile = foModel.getFileName();
        Integer loType = lsFile == null ? null : poAttachTypes.get(lsFile.toLowerCase());
        return loType == null ? -1 : loType;
    }

    /**
     * The type combo was changed: it applies to the attachment selected in the
     * table (nothing selected = just a choice for the next one).
     */
    private void setSelectedAttachmentType(int fnType) {
        if (pnAttachRow >= 0 && pnAttachRow < attach_data.size()) {
            setAttachmentType(attach_data.get(pnAttachRow), fnType);
        }
    }

    /**
     * Loads the attachments of the transaction that is open in MCSales.
     * MCSales.loadAttachments() reads the attachment rows AND downloads the
     * files, so it runs in a Task; the table is rebuilt when it is done. Call
     * it after the edit mode is known (loadAttachments() opens the rows for
     * update when the master is in UPDATE).
     */
    private void loadAttachmentRecords() {
        final int lnSeq = ++pnAttachLoadSeq;   // a newer load makes this one stale

        // empty the tab now: the previous transaction's attachments must not show while the files are downloading
        attach_data.clear();
        poAttachFiles.clear();
        poAttachModelRows.clear();
        poAttachTypes.clear();
        showAttachment(-1);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                oSalesController.MCSales().loadAttachments();
                return null;
            }

            @Override
            protected void succeeded() {
                if (lnSeq != pnAttachLoadSeq) {   // stale / cancelled
                    return;
                }
                reloadAttachmentTable();
                showAttachment(poAttachFiles.isEmpty() ? -1 : 0);
            }

            @Override
            protected void failed() {
                if (lnSeq != pnAttachLoadSeq) {
                    return;
                }
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, getException());
                // the rows were already read into MCSales before the download failed: show them anyway
                reloadAttachmentTable();
                showAttachment(poAttachFiles.isEmpty() ? -1 : 0);
            }
        };
        Thread loThread = new Thread(task);
        loThread.setDaemon(true);
        loThread.start();
    }

    /**
     * Rebuilds the three lists from MCSales (only the ACTIVE attachments that
     * have a file name: removed ones are deactivated in the model and kept
     * there until save). Does not touch the selection: call showAttachment()
     * after.
     */
    private void reloadAttachmentTable() {
        MCSales loSales = oSalesController.MCSales();
        String lsDir = System.getProperty("sys.default.path.temp.attachments");

        attach_data.clear();
        poAttachFiles.clear();
        poAttachModelRows.clear();
        poAttachRotation.clear();
        pnAttachRow = -1;

        for (int lnCtr = 0; lnCtr < loSales.getTransactionAttachmentCount(); lnCtr++) {
            // getAttachmentList(int) works; the no-arg list version has a bad cast
            Model_Transaction_Attachment loModel = loSales.getAttachmentList(lnCtr);
            String lsFile = loModel.getFileName();
            if (lsFile == null || lsFile.isEmpty() || RecordStatus.INACTIVE.equals(loModel.getRecordStatus())) {
                continue;
            }
            attach_data.add(loModel);
            poAttachFiles.add(new File(lsDir, lsFile));
            poAttachModelRows.add(lnCtr);
        }
    }

    private void addAttachment() {
        if (!canEdit()) {
            return;
        }
        int lnType = cmbAttachmentType.getSelectionModel().getSelectedIndex();
        if (lnType < 0) {
            ShowMessageFX.Error(SalesTransactionStatic.MSG_SELECT_ATTACHMENT_TYPE, pxeModuleName, null);
            cmbAttachmentType.requestFocus();
            return;
        }

        FileChooser loChooser = new FileChooser();
        loChooser.setTitle(SalesTransactionStatic.ATTACH_CHOOSER_TITLE);
        loChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(SalesTransactionStatic.ATTACH_FILTER_NAME, SalesTransactionStatic.ATTACH_FILTER_EXTENSIONS));
        File loFile = loChooser.showOpenDialog(btnAddAttachment.getScene().getWindow());
        if (loFile == null) {
            return;
        }

        if (loFile.getName().toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(loFile)) {
                int pageCount = document.getNumberOfPages();
                if (pageCount > 20) {
                    ShowMessageFX.Warning(null, pxeModuleName, "PDF exceeds maximum allowed pages.");
                    return;
                }
            } catch (IOException ex) {
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // the picked file lives in another folder than the attachment files: compare the FILE NAMES
        for (File loExisting : poAttachFiles) {
            if (loExisting.getName().equalsIgnoreCase(loFile.getName())) {
                ShowMessageFX.Error(SalesTransactionStatic.MSG_ATTACHMENT_DUPLICATE, pxeModuleName, null);
                return;
            }
        }
        try {
            MCSales loSales = oSalesController.MCSales();
            loSales.copyFile(loFile.getAbsolutePath());                       // into sys.default.path.temp.attachments (saveOthers() reads it from there)
            int lnModelRow = loSales.addAttachment(loFile.getName());         // new row (or re-activates a removed one with the same name)
            setAttachmentType(loSales.getAttachmentList(lnModelRow), lnType);

            reloadAttachmentTable();
            showAttachment(poAttachModelRows.indexOf(lnModelRow));            // select and show the one just added
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    private void removeAttachment() {
        if (!canEdit() || pnAttachRow < 0 || pnAttachRow >= poAttachModelRows.size()) {
            return;
        }
        if (!ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_REMOVE_ATTACHMENT, pxeModuleName, null)) {
            return;
        }
        int lnKeep = pnAttachRow;
        try {
            // a new attachment is dropped, a saved one is deactivated: both are handled by MCSales
            poJSON = oSalesController.MCSales().removeAttachment(poAttachModelRows.get(pnAttachRow));
            if (hasError(poJSON)) {
                return;
            }
            reloadAttachmentTable();
            showAttachment(Math.min(lnKeep, poAttachFiles.size() - 1));   // -1 clears the preview when none are left
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    /**
     * The image of an attachment file, or null when the file is missing
     * (download failed) or is not a readable image.
     */
    private Image readImage(File ffile) {
        if (ffile == null || !ffile.isFile()) {
            return null;
        }
        Image loImage = new Image(ffile.toURI().toString());
        return loImage.isError() ? null : loImage;
    }

    /**
     * What the viewer pane held before a PDF replaced it (the image view and
     * the arrows). A PDF swaps the pane's children for a ScrollPane, so every
     * other selection must put these back or the new image has nowhere to show.
     */
    private List<Node> poViewerDefaultNodes;

    private void showAttachment(int fnIndex) {
        Image loImage = null;
        try {
            // remember the original viewer content once (before any PDF replaces it), and put it back every time
            if (poViewerDefaultNodes == null) {
                poViewerDefaultNodes = new ArrayList<Node>(stackPane.getChildren());
            }
            if (!stackPane.getChildren().equals(poViewerDefaultNodes)) {
                stackPane.getChildren().setAll(poViewerDefaultNodes);   // drops the previous PDF's ScrollPane
            }
            poPdfContainer = null;
            resetImageZoom();   // a new attachment always opens at 100%, centered
            if (fnIndex >= 0 && fnIndex < poAttachFiles.size()) {
                if (!isPdfAttachment(poAttachFiles.get(fnIndex))) {
                    loImage = readImage(poAttachFiles.get(fnIndex));
                } else {

                    PDDocument document = PDDocument.load(poAttachFiles.get(fnIndex));
                    PDFRenderer renderer = new PDFRenderer(document);
                    int pageCount = document.getNumberOfPages();

                    VBox pdfContainer = new VBox(10);
                    pdfContainer.setAlignment(Pos.CENTER);
                    pdfContainer.setPrefWidth(ldstackPaneWidth);

                    for (int i = 0; i < pageCount; i++) {

                        BufferedImage pageImage = renderer.renderImageWithDPI(i, 150);
                        Image fxImage = SwingFXUtils.toFXImage(pageImage, null);
                        ImageView pageView = new ImageView(fxImage);
                        pageView.setPreserveRatio(true);
                        pageView.setFitWidth(ldstackPaneWidth);
                        JFXUtil.adjustImageSize(fxImage, pageView, ldstackPaneWidth, ldstackPaneHeight);
                        pdfContainer.getChildren().add(pageView);

                    }

                    Group pdfGroup = new Group(pdfContainer);
                    StackPane centerPane = new StackPane(pdfGroup);
                    centerPane.setAlignment(Pos.CENTER);

                    ScrollPane scrollPane = new ScrollPane(centerPane);
                    scrollPane.setPannable(true);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setFitToHeight(true);

                    poPdfContainer = pdfContainer;
                    stackPane.getChildren().setAll(scrollPane, btnArrowLeft, btnArrowRight, poRotateBox);
                    StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                    StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);
                    StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                    StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));

                    PauseTransition delay = new PauseTransition(Duration.seconds(2));
                    delay.setOnFinished(event -> Platform.runLater(() -> JFXUtil.stackPaneClip(stackPane)));
                    delay.play();
                    document.close();

                    final DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);
                    scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                        if (event.isControlDown()) {
                            event.consume();
                            double delta = event.getDeltaY() > 0 ? 1.1 : 0.9;
                            zoomFactor.set(zoomFactor.get() * delta);
                            pdfGroup.setScaleX(zoomFactor.get());
                            pdfGroup.setScaleY(zoomFactor.get());

                            Bounds viewportBounds = scrollPane.getViewportBounds();
                            Bounds contentBounds = pdfGroup.getBoundsInParent();
                            double mouseX = event.getX();
                            double mouseY = event.getY();

                            double hRatio = (scrollPane.getHvalue() * (contentBounds.getWidth() - viewportBounds.getWidth()) + mouseX) / contentBounds.getWidth();
                            double vRatio = (scrollPane.getVvalue() * (contentBounds.getHeight() - viewportBounds.getHeight()) + mouseY) / contentBounds.getHeight();

                            Platform.runLater(() -> {
                                Bounds newBounds = pdfGroup.getBoundsInParent();
                                double newH = (hRatio * newBounds.getWidth() - mouseX) / (newBounds.getWidth() - viewportBounds.getWidth());
                                double newV = (vRatio * newBounds.getHeight() - mouseY) / (newBounds.getHeight() - viewportBounds.getHeight());
                                scrollPane.setHvalue(Double.isNaN(newH) ? 0.5 : Math.min(Math.max(0, newH), 1.0));
                                scrollPane.setVvalue(Double.isNaN(newV) ? 0.5 : Math.min(Math.max(0, newV), 1.0));
                            });
                        }
                    });

                    final ObjectProperty<Point2D> lastMouse = new SimpleObjectProperty<>();
                    pdfGroup.setOnMousePressed(e -> lastMouse.set(new Point2D(e.getSceneX(), e.getSceneY())));
                    pdfGroup.setOnMouseDragged(e -> {
                        if (lastMouse.get() != null) {
                            double deltaX = e.getSceneX() - lastMouse.get().getX();
                            double deltaY = e.getSceneY() - lastMouse.get().getY();
                            pdfGroup.setTranslateX(pdfGroup.getTranslateX() + deltaX);
                            pdfGroup.setTranslateY(pdfGroup.getTranslateY() + deltaY);
                            lastMouse.set(new Point2D(e.getSceneX(), e.getSceneY()));
                        }
                    });
                    pdfGroup.setOnMouseReleased(e -> lastMouse.set(null));

                }

            }
            displayAttachment(fnIndex, loImage);
            applyAttachmentRotation();   // after displayAttachment(): it sets the selected row the rotation is keyed on
        } catch (IOException ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isPdfAttachment(File file) {
        return file != null
                && file.getName() != null
                && file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private void displayAttachment(int fnIndex, Image foImage) {
        pbShowingAttach = true;   // the selection changes below must not call showAttachment() again
        try {
            if (fnIndex < 0 || fnIndex >= poAttachFiles.size()) {
                pnAttachRow = -1;
                imageView.setImage(null);
                tfAttachmentNo.clear();
                tblAttachments.getSelectionModel().clearSelection();
            } else {
                pnAttachRow = fnIndex;
                imageView.setImage(foImage);   // null when the file is missing: the viewer is cleared, not left on the old image
                tfAttachmentNo.setText(String.valueOf(fnIndex + 1));
                if (fnIndex < attach_data.size()) {
                    if (tblAttachments.getSelectionModel().getSelectedIndex() != fnIndex) {
                        tblAttachments.getSelectionModel().select(fnIndex);
                    }
                    // the combo follows the clicked attachment; an unknown type clears it instead of keeping the previous one
                    int lnType = getAttachmentType(attach_data.get(fnIndex));
                    boolean lbPrev = pbSyncing;
                    pbSyncing = true;
                    try {
                        selectIndex(cmbAttachmentType, lnType);   // -1 clears the combo
                    } finally {
                        pbSyncing = lbPrev;
                    }
                }
            }
        } finally {
            pbShowingAttach = false;
        }
        updateAttachmentArrows();
    }

    // =========================================================================
    // Attachment keys: LEFT / RIGHT = previous / next attachment, UP = rotate right, DOWN = rotate left
    // =========================================================================
    private void initAttachmentKeys() {
        stackPane.setFocusTraversable(true);
        // clicking the viewer gives it the keyboard focus
        stackPane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> stackPane.requestFocus());
        // the viewer and the attachment list react to the same keys (a filter runs before the ScrollPane / table use the arrows)
        stackPane.addEventFilter(KeyEvent.KEY_PRESSED, this::attachmentKeyPressed);
        tblAttachments.addEventFilter(KeyEvent.KEY_PRESSED, this::attachmentKeyPressed);
    }

    private void attachmentKeyPressed(KeyEvent event) {
        if (poAttachFiles.isEmpty() || pnAttachRow < 0) {
            return;
        }
        switch (event.getCode()) {
            case LEFT:
                if (pnAttachRow > 0) {
                    showAttachment(pnAttachRow - 1);
                }
                break;
            case RIGHT:
                if (pnAttachRow < poAttachFiles.size() - 1) {
                    showAttachment(pnAttachRow + 1);
                }
                break;
            case UP:
                rotateAttachment(90);
                break;
            case DOWN:
                rotateAttachment(-90);
                break;
            default:
                return;   // any other key is left alone
        }
        event.consume();
    }

    // =========================================================================
    // Image zoom / pan (a PDF has its own zoom inside showAttachment())
    // =========================================================================
    private Group poViewGroup;                 // wraps imageView; the zoom and the pan are applied to it
    private double pnImageDragX;
    private double pnImageDragY;

    /**
     * Ctrl + mouse wheel zooms the image towards the mouse pointer; dragging the image moves it. Only active while an
     * image is shown: when a PDF is shown the event is left alone for the PDF's own scroll pane.
     */
    private void initImageZoom() {
        stackPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (!event.isControlDown() || poPdfContainer != null || imageView.getImage() == null) {
                return;
            }
            event.consume();
            double lnOld = poViewGroup.getScaleX();
            double lnNew = Math.max(0.2, Math.min(8.0, lnOld * (event.getDeltaY() > 0 ? 1.1 : 0.9)));
            double lnFactor = lnNew / lnOld;
            // keep the point under the mouse where it is: the group scales around its own center
            Bounds loBounds = poViewGroup.getBoundsInParent();
            double lnCx = loBounds.getMinX() + loBounds.getWidth() / 2;
            double lnCy = loBounds.getMinY() + loBounds.getHeight() / 2;
            poViewGroup.setTranslateX(poViewGroup.getTranslateX() + (event.getX() - lnCx) * (1 - lnFactor));
            poViewGroup.setTranslateY(poViewGroup.getTranslateY() + (event.getY() - lnCy) * (1 - lnFactor));
            poViewGroup.setScaleX(lnNew);
            poViewGroup.setScaleY(lnNew);
        });
        poViewGroup.setOnMousePressed(e -> {
            pnImageDragX = e.getSceneX();
            pnImageDragY = e.getSceneY();
        });
        poViewGroup.setOnMouseDragged(e -> {
            poViewGroup.setTranslateX(poViewGroup.getTranslateX() + e.getSceneX() - pnImageDragX);
            poViewGroup.setTranslateY(poViewGroup.getTranslateY() + e.getSceneY() - pnImageDragY);
            pnImageDragX = e.getSceneX();
            pnImageDragY = e.getSceneY();
        });
    }

    private void resetImageZoom() {
        if (poViewGroup != null) {
            poViewGroup.setScaleX(1.0);
            poViewGroup.setScaleY(1.0);
            poViewGroup.setTranslateX(0);
            poViewGroup.setTranslateY(0);
        }
    }

    // =========================================================================
    // Attachment rotation (view only: the file itself is never changed)
    // =========================================================================
    /** Rotation in degrees (0 / 90 / 180 / 270) of each attachment, by file path: it stays while browsing the list. */
    private final Map<String, Integer> poAttachRotation = new HashMap<String, Integer>();
    /** true while the shown attachment is turned 90 / 270 degrees: the image view swaps its fit width / height. */
    private final BooleanProperty pbQuarterTurn = new SimpleBooleanProperty(false);
    /** The page container of the PDF being shown (null when an image is shown). */
    private Node poPdfContainer;
    private HBox poRotateBox;

    /** Adds the two rotate buttons (bottom middle) on top of the viewer pane. */
    private void initRotateButtons() {
        Button lbtnLeft = createRotateButton(FontAwesomeIcon.ROTATE_LEFT, "Rotate left");
        Button lbtnRight = createRotateButton(FontAwesomeIcon.ROTATE_RIGHT, "Rotate right");
        lbtnLeft.setOnAction(e -> rotateAttachment(-90));
        lbtnRight.setOnAction(e -> rotateAttachment(90));
        poRotateBox = new HBox(14, lbtnLeft, lbtnRight);
        poRotateBox.setAlignment(Pos.CENTER);
        poRotateBox.setPickOnBounds(false);
        poRotateBox.setOnMouseClicked(javafx.scene.input.MouseEvent::consume);   // a double click on a rotate button must not open the file location
        poRotateBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(poRotateBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(poRotateBox, new Insets(0, 0, 14, 0));
        // enabled only while an attachment is showing
        poRotateBox.disableProperty().bind(Bindings.createBooleanBinding(
                () -> pnAttachRowProperty.get() < 0, pnAttachRowProperty));
        stackPane.getChildren().add(poRotateBox);
    }

    private static final String ROTATE_STYLE = "-fx-background-color: rgba(30,41,59,0.85); -fx-background-radius: 28;"
            + " -fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 28; -fx-border-width: 1;"
            + " -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 8, 0.2, 0, 2);";
    private static final String ROTATE_STYLE_HOVER = ROTATE_STYLE.replace("rgba(30,41,59,0.85)", "#FF8201");

    /** A round, dark rotate button with a large white icon; it turns orange (#FF8201) on hover. */
    private Button createRotateButton(FontAwesomeIcon foIcon, String fsTip) {
        FontAwesomeIconView loIcon = new FontAwesomeIconView(foIcon);
        loIcon.setSize("26");
        loIcon.setFill(javafx.scene.paint.Color.WHITE);
        Button lbtn = new Button();
        lbtn.setGraphic(loIcon);
        lbtn.setTooltip(new Tooltip(fsTip));
        lbtn.setFocusTraversable(false);
        lbtn.setMinSize(56, 56);
        lbtn.setPrefSize(56, 56);
        lbtn.setMaxSize(56, 56);
        lbtn.setStyle(ROTATE_STYLE);
        lbtn.hoverProperty().addListener((obs, was, is) -> lbtn.setStyle(is ? ROTATE_STYLE_HOVER : ROTATE_STYLE));
        return lbtn;
    }

    private final javafx.beans.property.IntegerProperty pnAttachRowProperty = new javafx.beans.property.SimpleIntegerProperty(-1);

    private String attachmentKey() {
        if (pnAttachRow < 0 || pnAttachRow >= poAttachFiles.size() || poAttachFiles.get(pnAttachRow) == null) {
            return null;
        }
        return poAttachFiles.get(pnAttachRow).getAbsolutePath();
    }

    private int currentRotation() {
        String lsKey = attachmentKey();
        return lsKey == null ? 0 : poAttachRotation.getOrDefault(lsKey, 0);
    }

    private void rotateAttachment(int fnDelta) {
        String lsKey = attachmentKey();
        if (lsKey == null) {
            return;
        }
        poAttachRotation.put(lsKey, ((currentRotation() + fnDelta) % 360 + 360) % 360);
        applyAttachmentRotation();
    }

    /** Applies the stored rotation of the selected attachment to whatever the viewer is showing. */
    private void applyAttachmentRotation() {
        int lnDeg = currentRotation();
        pbQuarterTurn.set(lnDeg == 90 || lnDeg == 270);
        imageView.setRotate(lnDeg);
        if (poPdfContainer != null) {
            poPdfContainer.setRotate(lnDeg);   // the Group around it measures the turned pages, so scrolling / zoom still fit
        }
    }

    private void updateAttachmentArrows() {
        pnAttachRowProperty.set(pnAttachRow);
        btnArrowLeft.setDisable(pnAttachRow <= 0);
        btnArrowRight.setDisable(pnAttachRow < 0 || pnAttachRow >= poAttachFiles.size() - 1);
    }

    /**
     * Opens Windows File Explorer at the location of the attachment selected in
     * tblAttachments. The file name and full path come from poAttachFiles (same
     * order as attach_data), so no separate path cache is needed. All
     * validation, the confirmation and the Explorer handling are delegated to
     * ExplorerWindowUtil.confirmAndOpenAttachmentLocation(). Any exception is
     * logged and not propagated, so a failure here never interrupts the rest of
     * the UI.
     */
    private void openAttachmentLocation() {
        try {
            int lnSelectedIndex = tblAttachments.getSelectionModel().getSelectedIndex();

            if (lnSelectedIndex < 0 || lnSelectedIndex >= attach_data.size()
                    || lnSelectedIndex >= poAttachFiles.size()) {
                return;
            }

            String lsFileName = attach_data.get(lnSelectedIndex).getFileName();

            // same index as attach_data: this is <temp attachment folder>/<file name>
            File loFile = poAttachFiles.get(lnSelectedIndex);
            String lsFilePath = loFile == null ? null : loFile.getAbsolutePath();

            ExplorerWindowUtil.confirmAndOpenAttachmentLocation(lsFileName, lsFilePath);

        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // =========================================================================
    // Other buttons
    // =========================================================================
    private void initOtherButtons() {
        btnAddClient.disableProperty().bind(pbEditable.not());
        btnAddClient1.disableProperty().bind(pbEditable.not());
        // source no / source desc / salesman are searchable (F3) only while editing a loaded transaction:
        // with no transaction loaded (or when not editing) they are disabled
        for (TextField ltfSearch : Arrays.asList(tfSourceNo, tfSourceDesc, tfSalesman)) {
            ltfSearch.disableProperty().bind(pbEditable.not().or(tfTransNo.textProperty().isEmpty()));
        }
        btnAddClient.setOnAction(e -> addClient(false));
        btnAddClient1.setOnAction(e -> addClient(true));
        btnNonItemize.setOnAction(e -> showNonItemizeGiveaways());   // read-only list: works in every edit mode
    }

    /**
     * Opens SalesTransactionNonItemize.fxml as a modal listing the non-itemized
     * giveaways of the source quotation of this transaction. The list is loaded
     * by MCSales.loadSalesQuotationNonItemizedList(); the modal only displays
     * it.
     */
    private void showNonItemizeGiveaways() {
        try {
            String lsSourceNo = master().getSourceNo();
            if (lsSourceNo == null || lsSourceNo.trim().isEmpty()) {
                ShowMessageFX.Warning("This transaction has no source quotation.", "Warning", null);
                return;
            }
            poJSON = oSalesController.MCSales().loadSalesQuotationNonItemizedList();
            if (hasError(poJSON)) {
                return;
            }

            FXMLLoader loLoader = new FXMLLoader(getClass().getResource("/ph/com/guanzongroup/integsys/views/SalesTransactionNonItemize.fxml"));
            Parent loRoot = loLoader.load();
            SalesTransactionNonItemizeController loController = loLoader.getController();
            loController.setCustomerName(tfClientName.getText());
            loController.setMCSales(oSalesController.MCSales());

            Stage loStage = new Stage();
            loStage.setTitle("Non-Itemize Giveaways");
            loStage.initStyle(StageStyle.UNDECORATED);
            loStage.initModality(Modality.APPLICATION_MODAL);
            loStage.initOwner(btnNonItemize.getScene().getWindow());
            // 1. MAKE THE MAIN ANCHOR MOVABLE
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];

            loRoot.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });

            loRoot.setOnMouseDragged(event -> {
                loStage.setX(event.getScreenX() - xOffset[0]);
                loStage.setY(event.getScreenY() - yOffset[0]);
            });

            // 2. APPLY SHADOW TO THE MAIN ANCHOR
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0));
            shadow.setRadius(15.0);
            shadow.setOffsetX(0.0);
            shadow.setOffsetY(5.0);
            loRoot.setEffect(shadow);

            // 3. BOUNDARY FIX FOR UNDECORATED SHADOWS
            // To prevent the shadow from being clipped by the window edges,
            // the scene is made transparent.
            Scene scene = new Scene(loRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            loStage.setScene(scene);
            loStage.setResizable(false);
            loStage.showAndWait();
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    private void addClient(boolean fbCoClient) {
        // TODO: open the client search / add dialog and write the chosen client (or co-client) to the model
        // (MCSales.addClient() / addCoClient() open the client GUI and set the master).
        ShowMessageFX.Error(fbCoClient
                ? SalesTransactionStatic.MSG_ADD_COCLIENT_UNDER_DEVELOPMENT
                : SalesTransactionStatic.MSG_ADD_CLIENT_UNDER_DEVELOPMENT, pxeModuleName, null);
    }

    // =========================================================================
    // Transaction summary
    // =========================================================================
    private void initSummary() {
        for (TextField ltf : poSummaryFields) {
            ltf.setEditable(false);
            ltf.setFocusTraversable(false);
        }
        // payment tab figures are computed / mirrored, never typed.
        // tfPaymentCwt is NOT here: its editable / disable state is bound to the CWT combo in initComboBoxes().
        for (TextField ltf : Arrays.asList(tfPaymentDrNo, tfPaymentTotal, tfPaymentAdvances,
                tfPaymentAmountPaid, tfPaymentBalance)) {
            ltf.setEditable(false);
        }
    }

    /**
     * Refreshes the summary. The figures come from the master through
     * loadPaymentTotals(); the rest mirrors an input on the form.
     */
    private void loadSummary() {
        loadPaymentTotals();   // fills the payment tab and the figures of the summary first
        tfSummaryCustomer.setText(tfClientName.getText());
        tfSummaryTranType.setText(comboText(cmbPaymentTranType));
        tfSummaryPaymentForm.setText(comboText(cmbPaymentForm));
        tfSummaryTerm.setText(tfPaymentTerm.getText());
        tfSummaryDueDate.setText(dateText(dpPaymentDueDate));
    }

    // =========================================================================
    // Clearing (combos, check boxes, date pickers, tables, attachments)
    // =========================================================================
    private void ClearSelections() {
        pbSyncing = true;
        try {
            for (ComboBox lcmb : poCombos) {
                lcmb.getSelectionModel().clearSelection();
            }
            for (CheckBox lcb : poCheckBoxes) {
                lcb.setSelected(false);
            }
            for (DatePicker ldp : poDatePickers) {
                ldp.setValue(null);
            }
        } finally {
            pbSyncing = false;
        }
    }

    private void ClearTables() {
        unit_data.clear();
        gaway_data.clear();
        reginsur_data.clear();
        invoice_data.clear();
        attach_data.clear();
        poAttachFiles.clear();
        poAttachModelRows.clear();
        ++pnAttachLoadSeq;   // a download that is still running must not refill the table
        pnUnitRow = -1;
        pnGawayRow = -1;
        pnRegInsurRow = -1;
        poSelRegInsur = null;
        poSectionReg = null;
        poSectionIns = null;
        poEditReg = null;
        poEditIns = null;
        showAttachment(-1);
        pbRegistrationRow.set(false);
    }

    private void LoadMaster() throws GuanzonException, SQLException {
        lblStatus.setText(StatusText(oSalesController.MCSales().getMaster().getTransactionStatus()));
        tfTransNo.setText(oSalesController.MCSales().getMaster().getTransactionNo());
        // textOf() tolerates a missing source (null / exception) so the rest of LoadMaster() still runs
        tfSourceNo.setText(textOf(() -> master().getSourceNo()));
        tfSourceDesc.setText(textOf(() -> master().TransactionSource().getSourceName()));
        setDate(dpTransDate, toLocalDate(oSalesController.MCSales().getMaster().getTransactionDate()));
        // Salesman: filled from the master (the source's salesman when there is a source). Searchable (F3) whenever editing,
        // with or without a source; otherwise the field is display-only.
        tfSalesman.setText(salesmanName());
        tfSalesman.setEditable(canEdit());
        tfSalesman.setPromptText(canEdit() ? "PRESS F3: Search" : "");

        // source fields: the text was set above from the master; only the edit state / prompt are set here
        tfSourceDesc.setEditable(canEdit());
        tfSourceDesc.setPromptText(canEdit() ? "PRESS F3: Search" : "");

        tfSourceNo.setEditable(canEdit());
        tfSourceNo.setPromptText(canEdit() ? "PRESS F3: Search" : "");

        // client type: shows the model's value (the model defaults a new client to Individual) without firing cmb_Changed
        boolean lbPrevType = pbSyncing;
        pbSyncing = true;
        try {
            selectIndex(cmbClientType, indexOf(textOf(() -> master().Client().getClientType())));
        } finally {
            pbSyncing = lbPrevType;
        }
        // the customer line is built by showCustomerName() at the end of LoadClientInfo(): the client fields are
        // still empty here, because LoadMaster() runs before LoadClientInfo()
    }

    /**
     * Salesman name from the master. Unlike textOf(), a failure is LOGGED (not
     * hidden), and when the name comes back blank the master's salesman-related
     * getters are printed as "[SALESMAN] ..." lines so the real getter / column
     * can be found.
     */
    private String salesmanName() {
        String lsName = "";
        try {
            Object loName = master().SalesmanClient().getCompanyName();
            lsName = loName == null ? "" : String.valueOf(loName).trim();
        } catch (Exception ex) {
            System.out.println("[SALESMAN] SalesmanClient().getCompanyName() threw " + ex);
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
        }
        if (lsName.isEmpty()) {
            System.out.println("[SALESMAN] name is blank, source = '" + textOf(() -> master().getSourceNo()) + "'");
            for (java.lang.reflect.Method loMethod : master().getClass().getMethods()) {
                String lsMethod = loMethod.getName().toLowerCase();
                if (loMethod.getParameterCount() == 0 && loMethod.getDeclaringClass() != Object.class
                        && (lsMethod.contains("sales") || lsMethod.contains("agent") || lsMethod.contains("advis"))) {
                    try {
                        System.out.println("[SALESMAN]   " + loMethod.getName() + "() = " + loMethod.invoke(master()));
                    } catch (Exception ex) {
                        System.out.println("[SALESMAN]   " + loMethod.getName() + "() threw " + ex.getCause());
                    }
                }
            }
        }
        return lsName;
    }

    /**
     * Description of the term saved on the master ("" when none / not
     * readable).
     */
    private String termText() {
        return textOf(() -> master().Term().getDescription()).trim();
    }

    /**
     * Puts the saved term back into tfPaymentTerm (only when it differs, and
     * without firing change handlers).
     */
    private void restoreTermText() {
        String lsText = pbTermPayment.get() ? termText() : "";
        if (lsText.equals(tfPaymentTerm.getText() == null ? "" : tfPaymentTerm.getText())) {
            return;
        }
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            tfPaymentTerm.setText(lsText);
        } finally {
            pbSyncing = lbPrev;
        }
    }

    /**
     * true when the transaction has a source (quotation / reservation); null or
     * blank source = no source.
     */
    private boolean hasSource() {
        return !textOf(() -> master().getSourceNo()).trim().isEmpty();
    }

    /**
     * Header line "name | contact | address". Call it AFTER the client fields
     * are filled.
     */
    private void showCustomerName() {
        if (tfClientName.getText() == null || tfClientName.getText().trim().isEmpty()) {
            taCustomerName.clear();   // no client yet: do not show " |  | "
            return;
        }
        taCustomerName.setStyle("-fx-font-weight: bold;");
        taCustomerName.setText(tfClientName.getText() + " | " + tfClientContactNo.getText() + " | " + tfClientAddress.getText());
    }

    private String StatusText(String fsCode) {
        if (fsCode == null || fsCode.trim().isEmpty()) {
            return "";
        }
        try {
            int lnCode = Integer.parseInt(fsCode.trim());
            return (lnCode >= 0 && lnCode < SalesTransactionStatic.TRANSACTION_STATUS.length) ? SalesTransactionStatic.TRANSACTION_STATUS[lnCode] : fsCode;   // unknown code: show it as-is
        } catch (NumberFormatException ex) {
            return fsCode;   // already text
        }
    }

    private String genderText(String fsCode) {
        if (fsCode == null || fsCode.trim().isEmpty()) {
            return "";
        }
        try {
            int lnCode = Integer.parseInt(fsCode.trim());
            return (lnCode >= 0 && lnCode < SalesTransactionStatic.GENDER_TEXT.length) ? SalesTransactionStatic.GENDER_TEXT[lnCode] : fsCode;   // unknown code: show it as-is
        } catch (NumberFormatException ex) {
            return fsCode;   // already text, e.g. "Male"
        }
    }

    /**
     * Birthplace text "town, province" of a client; "" when the id is blank or
     * the town cannot be opened. No early returns in the callers: a blank
     * birthplace must not stop the rest of the client info from loading.
     */
    private String birthplaceText(Model_Client_Master foClient) throws GuanzonException, SQLException {
        String lsId = foClient.getBirthPlaceId();
        if (lsId == null || lsId.isEmpty()) {
            return "";
        }
        if (!"success".equals(foClient.BirthTown().openRecord(lsId).get("result"))) {
            return "";
        }
        return foClient.BirthTown().getDescription() + ", " + foClient.BirthTown().Province().getDescription();
    }

    /**
     * Nationality text of a client; "" when the id is blank or the country
     * cannot be opened.
     */
    private String citizenshipText(Model_Client_Master foClient) throws GuanzonException, SQLException {
        String lsId = foClient.getCitizenshipId();
        if (lsId == null || lsId.isEmpty()) {
            return "";
        }
        if (!"success".equals(foClient.Citizenship().openRecord(lsId).get("result"))) {
            return "";
        }
        return foClient.Citizenship().getNationality();
    }

    private void LoadClientInfo() {
        pbSyncing = true;
        try {
            // the client type combo is filled by LoadMaster() from the model: nothing to select here
            Model_Client_Master loClient = oSalesController.MCSales().getMaster().Client();

            tfClientName.setText(loClient.getCompanyName());
            tfClientGender.setText(genderText(loClient.getGender()));
            setDate(dpClientBday, toLocalDate(loClient.getBirthDate()));
            tfClientBirthplace.setText(birthplaceText(loClient));
            tfClientCitizenship.setText(citizenshipText(loClient));

            tfClientOccupation.setText(oSalesController.MCSales().getMaster().ClientEmployment().getPosition());
            tfClientTinNo.setText(loClient.getTaxIdNumber());
            tfClientFathersName.setText(loClient.getFatherName());
            tfClientMothersName.setText(loClient.getMotherName());
            tfClientContactNo.setText(oSalesController.MCSales().getMaster().ClientMobile().getMobileNo());
            tfClientAddress.setText(oSalesController.MCSales().getMaster().ClientAddress().getAddress() + ","
                    + oSalesController.MCSales().getMaster().ClientAddress().Barangay().getBarangayName() + ","
                    + oSalesController.MCSales().getMaster().ClientAddress().Town().getDescription() + ","
                    + oSalesController.MCSales().getMaster().ClientAddress().Town().Province().getDescription() + ","
                    + oSalesController.MCSales().getMaster().ClientAddress().Town().getZipCode());

            tfClientLTOID.setText(loClient.getLTOClientId());
            tfClientLTOAddress.setText("");
            showCustomerName();   // the header line needs the name, contact and address set above

        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        } finally {
            pbSyncing = false;
        }
    }

    private void LoadCoClientInfo() {
        try {
            Model_Client_Master loClient = oSalesController.MCSales().getMaster().CoClient();

            tfCoClientName.setText(loClient.getCompanyName());
            tfCoClientGender.setText(genderText(loClient.getGender()));
            setDate(dpCoClientBirthday, toLocalDate(loClient.getBirthDate()));
            tfCoClientBirthplace.setText(birthplaceText(loClient));
            tfCoClientCitezenship.setText(citizenshipText(loClient));

            tfCoClientOccupation.setText(oSalesController.MCSales().getMaster().CoClientEmployment().getPosition());
            tfCoClientTinNo.setText(loClient.getTaxIdNumber());
            tfCoClientFathersName.setText(loClient.getFatherName());
            tfCoClientMothersName.setText(loClient.getMotherName());
//            tfCoClientContactNo.setText(oSalesController.MCSales().getMaster().CoClient().getContactNumber());
//            tfCoClientEmail.setText(oSalesController.MCSales().getMaster().CoClient().getEmailAddress());
            tfCoClientAddress.setText(oSalesController.MCSales().getMaster().CoClientAddress().getAddress() + ","
                    + oSalesController.MCSales().getMaster().CoClientAddress().Barangay().getBarangayName() + ","
                    + oSalesController.MCSales().getMaster().CoClientAddress().Town().getDescription() + ","
                    + oSalesController.MCSales().getMaster().CoClientAddress().Town().Province().getDescription() + ","
                    + oSalesController.MCSales().getMaster().CoClientAddress().Town().getZipCode());
            tfCoClientLTOAddress.setText("");
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * java.sql.Date has no toInstant(), so convert it with its own
     * toLocalDate().
     */
    private static LocalDate toLocalDate(java.util.Date fdDate) {
        if (fdDate == null) {
            return null;
        }
        if (fdDate instanceof java.sql.Date) {
            return ((java.sql.Date) fdDate).toLocalDate();
        }
        return fdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();   // java.util.Date / Timestamp
    }

    /* Clears all text fields in the form.
     * This method is called when the user cancels a record or closes the form without saving.
     * It ensures that any unsaved data is not kept in the text fields.
     * */
    private void ClearAll() {
        pdCwtRate = 0.0;
        ClearMaster();
        ClearClient();
        ClearMotorcycle();
        ClearRegistration();
        ClearGiveaway();
        ClearPayment();
        ClearSelections();
        ClearTables();
        loadSummary();
    }

    private void ClearMaster() {
        poMasterFields.forEach(TextInputControl::clear);
    }

    private void ClearClient() {
        poClientFields.forEach(TextInputControl::clear);
    }

    private void ClearMotorcycle() {
        poMotorcycleFields.forEach(TextInputControl::clear);
    }

    private void ClearRegistration() {
        poRegistrationFields.forEach(TextInputControl::clear);
    }

    private void ClearGiveaway() {
        poGiveawayFields.forEach(TextInputControl::clear);
    }

    private void ClearPayment() {
        poPaymentFields.forEach(TextInputControl::clear);
    }

    /**
     * Form-wide hot keys. F5 = approval status (matrix recipients), view only.
     */
    private void initHotKeys() {
        ChildAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F5) {
                event.consume();
                showMatrixRecipients();
            }
        });
    }

    /**
     * Shows who the approval request of the loaded transaction was sent to and
     * their status. Works in every edit mode.
     */
    private void showMatrixRecipients() {
        try {
            if (oSalesController.MCSales().getEditMode() == EditMode.ADDNEW) {
                return;
            }
            String lsTransNo = master().getTransactionNo();
            if (lsTransNo == null || lsTransNo.isEmpty()) {
                ShowMessageFX.Warning("No transaction is loaded.", "Approval Status", null);
                return;
            }
            JSONObject loJSON = oSalesController.MCSales().getMatrixRecipients();
            if (hasError(loJSON)) {
                return;
            }
            MatrixRecipientDialog.show(ChildAnchorPane.getScene().getWindow(), "Approval Status", loJSON);
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    private boolean hasError(JSONObject foJSON) {
        if (foJSON == null || !"error".equals(foJSON.get("result"))) {
            return false;
        }
        String lsMessage = (String) foJSON.get("message");
        if (lsMessage != null && !lsMessage.trim().isEmpty()) {
            ShowMessageFX.Warning(lsMessage, "Warning", null);
        }
        return true;
    }
}