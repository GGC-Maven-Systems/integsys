package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.*;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.MCSales;
import ph.com.guanzongroup.cas.sales.SalesQoutationVersion;
import ph.com.guanzongroup.cas.sales.constant.SalesTransactionStatic;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Giveaways;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationStatic;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for SalesTransactionMC.fxml (Motorcycle Sales form).
 *
 * Structure follows SalesQoutationController: ScreenInterface + Initializable,
 * initButton(editMode), JFXUtil focus listeners that write straight to the model,
 * F3 search in txtField_KeyPressed, Task-based table loading, and a transaction
 * summary rebuilt from the model after every change.
 *
 * ------------------------------------------------------------------------------
 * CHANGES FOR THE LATEST FXML
 *   - Client / Co-Client: added Gender, Citizenship, Birthplace, Occupation, Father,
 *     Mother, LTO ID and LTO Address fields; co-client birthday picker is dpCoClientBirthday.
 *   - Co-client add button is btnAddClient1 (was btnAddCoClient).
 *   - Motorcycle tab: added tfMCUnitPrice.
 *   - Registration & Insurance tab: new tblRegAndIsur table (reg_data).
 *   - Giveaways: added tfGawayRemarks and the Remarks column (column indexes shifted by one
 *     from Remarks onward).
 *   - Payment tab: added tfPaymentCreditAdvisor.
 *   - Read-only date pickers (trans date, client / co-client birthday) are no longer
 *     re-enabled by the edit-mode binding.
 *   - Fixed the "taMCKeyCode" case id (the field is tfMCKeyCode).
 *
 * ASSUMED BACKEND API  (everything marked ASSUMED / trans() / SalesQoutationStatic)
 * The SalesTransaction backend classes were not provided, so every call below is
 * modelled on the SalesQoutation API. If a name differs, fix it in one place.
 *
 * NEW CONSTANTS NEEDED in SalesTransactionStatic:
 *   TBL_NO_REGINSUR, MSG_CONFIRM_REMOVE_REGINSUR
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

    private int pnUnitRow = -1;
    private int pnGawayRow = -1;
    private int pnRegInsurRow = -1;
    private int pnAttachRow = -1;
    private boolean pbSyncing = false;   // true while combos/checkboxes are set programmatically

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(SalesTransactionStatic.DATE_PATTERN);

    /** true only in ADDNEW / UPDATE; combos, check boxes, date pickers and action buttons are bound to it. */
    private final BooleanProperty pbEditable = new SimpleBooleanProperty(false);
    /** true when the payment transaction type is Credit; drives the Term / Due Date fields. */
    private final BooleanProperty pbCreditPayment = new SimpleBooleanProperty(false);

    /** Files picked in the Attachment tab (same order as the attachment rows). */
    private final ObservableList<File> poAttachFiles = FXCollections.observableArrayList();

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
    private final ObservableList<ModelTableMain> unit_data = FXCollections.observableArrayList();
    private final ObservableList<ModelTableMain> gaway_data = FXCollections.observableArrayList();
    private final ObservableList<ModelTableDetail> reginsur_data = FXCollections.observableArrayList();
    private final ObservableList<ModelTableDetail> invoice_data = FXCollections.observableArrayList();
    private final ObservableList<ModelTableDetail> attach_data = FXCollections.observableArrayList();

    // =========================================================================
    // Root containers & buttons
    // =========================================================================
    @FXML private AnchorPane ChildAnchorPane;
    @FXML private AnchorPane apSearchMaster;
    @FXML private AnchorPane AnchorInputs;
    @FXML private AnchorPane anchorQoutation;
    @FXML private AnchorPane anchorDetails;
    @FXML private AnchorPane apAttachments1;
    @FXML private AnchorPane apAttachmentButtons1;
    @FXML private HBox hbButtons;

    @FXML private Button btnBrowse;
    @FXML private Button btnNew;
    @FXML private Button btnUpdate;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnApproved;   // "Confirm"
    @FXML private Button btnVoid;
    @FXML private Button btnHistory;
    @FXML private Button btnClose;
    @FXML private Button btnAddClient;
    @FXML private Button btnAddClient1;      // co-client add button (fx:id as in the FXML)
    @FXML private FontAwesomeIconView faAdd12;
    @FXML private FontAwesomeIconView faAdd121;

    // =========================================================================
    // Header
    // =========================================================================
    @FXML private TextField tfTransNo;
    @FXML private DatePicker dpTransDate;
    @FXML private TextField tfSourceDesc;
    @FXML private TextField tfSourceNo;
    @FXML private TextField tfReceipt;          // DR No (header, read-only mirror)
    @FXML private TextArea taCustomerName;
    @FXML private Label lblStatus;

    // =========================================================================
    // Client Information tab
    // =========================================================================
    @FXML private ComboBox cmbClientType;
    @FXML private TextField tfClientName;
    @FXML private TextField tfClientGender;
    @FXML private TextField tfClientCitizenship;
    @FXML private DatePicker dpClientBday;
    @FXML private TextField tfClientBirthplace;
    @FXML private TextField tfClientOccupation;
    @FXML private TextField tfClientTinNo;
    @FXML private TextField tfClientFathersName;
    @FXML private TextField tfClientMothersName;
    @FXML private TextField tfClientContactNo;
    @FXML private TextField tfClientEmail;
    @FXML private TextField tfClientAddress;
    @FXML private TextField tfClientLTOID;
    @FXML private TextField tfClientLTOAddress;

    // Co-Client (fx:id "tfCoClientCitezenship" is spelled that way in the FXML)
    @FXML private TextField tfCoClientName;
    @FXML private TextField tfCoClientGender;
    @FXML private TextField tfCoClientCitezenship;
    @FXML private DatePicker dpCoClientBirthday;
    @FXML private TextField tfCoClientBirthplace;
    @FXML private TextField tfCoClientOccupation;
    @FXML private TextField tfCoClientTinNo;
    @FXML private TextField tfCoClientFathersName;
    @FXML private TextField tfCoClientMothersName;
    @FXML private TextField tfCoClientContactNo;
    @FXML private TextField tfCoClientEmail;
    @FXML private TextField tfCoClientAddress;
    @FXML private TextField tfCoClientLTOID;
    @FXML private TextField tfCoClientLTOAddress;

    // =========================================================================
    // Motorcycle & Pricing tab
    // =========================================================================
    @FXML private TextField tfMCModel;
    @FXML private TextField tfMCColor;
    @FXML private TextField tfMCEngineNo;
    @FXML private TextField tfMCFrameNo;
    @FXML private TextField tfMCWarrantyNo;
    @FXML private TextField tfMCKeyCode;
    @FXML private TextField tfMCRemarks;
    @FXML private ComboBox cmbVehicleType;
    @FXML private TextField tfMCUnitPrice;     // Unit Price
    @FXML private TextField tfPomoCode;        // Promo
    @FXML private TextField tfMCRegisAmt;      // Regis. Amt
    @FXML private TextField tfMCInsurAmt;      // Insur. Amt
    @FXML private TextField tfPromoDisc;       // Discount (%)
    @FXML private TextField tfPromoAddDisc;    // Add. Discount
    @FXML private TextField tfFreight;         // Freight

    // =========================================================================
    // Registration & Insurance tab
    // =========================================================================
    @FXML private TextField tfRegEngineNo;
    @FXML private TextField tfRegFrameNo;
    @FXML private ComboBox cmbRegistrationType;
    @FXML private TextField tfRegAmount;
    @FXML private TextField tfRegDesc;
    @FXML private ComboBox cmbRegInsurType;
    @FXML private CheckBox cbRegIsCharge;
    @FXML private ComboBox cmbInsuranceType;
    @FXML private ComboBox cmbRegInsuranceOption;
    @FXML private TextField tfInsurDesc;
    @FXML private TextField tfInsurYear;
    @FXML private TextField tfInsurPayer;
    @FXML private TextField tfInsurAmt;
    @FXML private CheckBox cbInsurIsCharge;

    @FXML private TableView<ModelTableDetail> tblRegAndIsur;
    @FXML private TableColumn<ModelTableDetail, String> tblRowRegAndInsurNo;
    @FXML private TableColumn<ModelTableDetail, String> tblRowRegAndInsurEngine;
    @FXML private TableColumn<ModelTableDetail, String> tblRowRegAndInsurSource;
    @FXML private TableColumn<ModelTableDetail, String> tblRowRegAndInsurDesc;
    @FXML private TableColumn<ModelTableDetail, String> tblRowRegAndInsurAmount;
    @FXML private TableColumn<ModelTableDetail, String> tblRowRegAndInsurStat;
    @FXML private TableColumn<ModelTableDetail, String> tblRowRegAndInsurAction;   // Remove column

    // =========================================================================
    // Giveaways tab
    // =========================================================================
    @FXML private TextField tfGawayBarrcode;
    @FXML private TextField tfGawayDesc;
    @FXML private TextField tfGawayRemarks;
    @FXML private TextField tfGawayGiven;
    @FXML private TextField tfGawayIssued;
    @FXML private TextField tfGawayQty;
    @FXML private TableView<ModelTableMain> tblGawayItem;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayNo;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayBarrcode;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayDesc;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayRemarks;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayQOH;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayGiven;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayIssued;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayQty;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayStatus;
    @FXML private TableColumn<ModelTableMain, String> tblRowGawayAction11211;

    // =========================================================================
    // Payment Type tab
    // =========================================================================
    @FXML private ComboBox cmbPaymentTranType;
    @FXML private ComboBox cmbPaymentForm;
    @FXML private TextField tfPaymentCreditAdvisor;
    @FXML private ComboBox cmbPaymentCWT;
    @FXML private CheckBox chkPaymentHasPromo;
    @FXML private TextField tfPaymentDrNo;         // DR No (payment tab, read-only)
    @FXML private TextField tfPaymentTerm;
    @FXML private DatePicker dpPaymentDueDate;
    @FXML private TextField tfPaymentTotal;
    @FXML private TextField tfPaymentAdvances;
    @FXML private TextField tfPaymentCwt;
    @FXML private TextField tfPaymentAmountPaid;
    @FXML private TextField tfPaymentBalance;

    @FXML private TableView<ModelTableDetail> tblInvoiceList;   // Invoice list
    @FXML private TableColumn<ModelTableDetail, String> tblRowInvoiceNo;
    @FXML private TableColumn<ModelTableDetail, String> tblRowInvoiceDate;
    @FXML private TableColumn<ModelTableDetail, String> tblRowInvoiceSource;
    @FXML private TableColumn<ModelTableDetail, String> tblRowInvoiceReceiptNo;
    @FXML private TableColumn<ModelTableDetail, String> tblRowInvoiceAmount;
    @FXML private TableColumn<ModelTableDetail, String> tblRowInvoiceStatus;

    // =========================================================================
    // Units grid (bottom of the Sales Information tab)
    // =========================================================================
    @FXML private TableView<ModelTableMain> tblUnits;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailNo;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailBrand;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailModel;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailEngineNo;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailFramNo;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailVariant;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailWarrantyNo;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailKeySerial;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailRegType;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailNetSRP;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailSRP;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailInsur;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailDiscount;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailAddDisc;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailRemarks;
    @FXML private TableColumn<ModelTableMain, String> tblColDetailQOH21;   // used as the Remove column
    @FXML private TableColumn<ModelTableMain, String> tblColDetailQOH22;   // unused in FXML, dropped at runtime
    @FXML private TableColumn<ModelTableMain, String> tblColDetailQOH23;   // unused in FXML, dropped at runtime

    // =========================================================================
    // Attachment tab
    // =========================================================================
    @FXML private TextField tfAttachmentNo;
    @FXML private ComboBox cmbAttachmentType;
    @FXML private TableView<ModelTableDetail> tblAttachments;
    @FXML private TableColumn<ModelTableDetail, String> tblRowNoAttachment;
    @FXML private TableColumn<ModelTableDetail, String> tblFileNameAttachment;
    @FXML private Button btnAddAttachment;
    @FXML private Button btnRemoveAttachment;
    @FXML private StackPane stackPane;
    @FXML private ImageView imageView;
    @FXML private Button btnArrowLeft;
    @FXML private Button btnArrowRight;

    // =========================================================================
    // Transaction summary
    // =========================================================================
    @FXML private TextField tfSummaryCustomer;
    @FXML private TextField tfSummaryTranType;
    @FXML private TextField tfSummaryPaymentForm;
    @FXML private TextField tfSummaryTerm;
    @FXML private TextField tfSummaryDueDate;
    @FXML private TextField tfSummaryUnitPrice;
    @FXML private TextField tfSummaryDiscount;
    @FXML private TextField tfSummaryAddDiscount;
    @FXML private TextField tfSummaryFreight;
    @FXML private TextField tfSummaryRegistration;
    @FXML private TextField tfSummaryInsurance;
    @FXML private TextField tfSummaryTotal;
    @FXML private TextField tfSummaryGiveaways;
    @FXML private TextField tfSummaryVatSales;
    @FXML private TextField tfSummaryVatAmount;
    @FXML private TextField tfSummaryNonVatSales;
    @FXML private TextField tfSummaryZeroVatSales;
    @FXML private TextField tfSummaryCwt;
    @FXML private TextField tfSummaryAdvances;
    @FXML private TextField tfSummaryAmountPaid;
    @FXML private TextField tfSummaryBalance;

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
        pnEditMode = oSalesController.SalesQoutation().getEditMode();
        initButton(pnEditMode);
        ClickButton();
        pbLoaded = true;
    }

    /**
     * Groups the text fields once, after FXML injection.
     * Each group is used by its Clear*() method; poAllFields is used to attach handlers.
     * tfSummary* fields are display-only and intentionally NOT included.
     */
    private void initControlGroups() {
        poMasterFields = Arrays.asList(tfTransNo, tfSourceDesc, tfSourceNo, tfReceipt, taCustomerName);

        poClientFields = Arrays.asList(
                tfClientName, tfClientGender, tfClientCitizenship, tfClientBirthplace, tfClientOccupation,
                tfClientTinNo, tfClientFathersName, tfClientMothersName, tfClientContactNo, tfClientEmail,
                tfClientAddress, tfClientLTOID, tfClientLTOAddress,
                tfCoClientName, tfCoClientGender, tfCoClientCitezenship, tfCoClientBirthplace, tfCoClientOccupation,
                tfCoClientTinNo, tfCoClientFathersName, tfCoClientMothersName, tfCoClientContactNo, tfCoClientEmail,
                tfCoClientAddress, tfCoClientLTOID, tfCoClientLTOAddress);

        poMotorcycleFields = Arrays.asList(tfMCModel, tfMCColor, tfMCEngineNo, tfMCFrameNo, tfMCWarrantyNo, tfMCKeyCode,
                tfMCRemarks, tfMCUnitPrice, tfPomoCode, tfMCRegisAmt, tfMCInsurAmt, tfPromoDisc, tfPromoAddDisc, tfFreight);

        poRegistrationFields = Arrays.asList(tfRegEngineNo, tfRegFrameNo, tfRegAmount, tfRegDesc,
                tfInsurDesc, tfInsurYear, tfInsurPayer, tfInsurAmt);

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

        poCombos = Arrays.asList(cmbClientType, cmbVehicleType, cmbRegistrationType, cmbRegInsurType,
                cmbInsuranceType, cmbRegInsuranceOption, cmbPaymentTranType, cmbPaymentForm,
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

    /** Attaches the key press and lost focus handlers to every grouped field in one pass. */
    private void initFieldHandlers() {
        for (TextInputControl txtInput : poAllFields) {
            txtInput.setOnKeyPressed(this::txtField_KeyPressed);
            txtInput.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    txtField_LostFocus(txtInput);
                }
            });
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextInputControl txtInput = (TextInputControl) event.getSource();
        String lsID = txtInput.getId();
        String lsValuse = txtInput.getText();
        try {
            switch (event.getCode()) {
                case F3:
                    switch (lsID) {
                        case "tfClientName":
                            poJSON = oSalesController.MCSales().searchClient(txtInput.getText(), true);
                            if(poJSON.get("result").equals("error")) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                break;
                            }
                            LoadClientInfo();
                            taCustomerName.setStyle("-fx-font-weight: bold;");
                            taCustomerName.setText(tfClientName.getText() + " | " + tfClientContactNo.getText() + " | " + tfClientAddress.getText());
                            break;
                        case "tfCoClientName":
                            LoadClientInfo();
                            break;
                        case "tfMCModel":
                        case "tfMCColor":
                        case "tfMCEngineNo":
                            poJSON = oSalesController.MCSales().searchDetailBySerial(pnUnitRow, lsValuse, 2);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                return;
                            }
                        case "tfMCFrameNo":
                            break;
                        case "tfPomoCode":
                            poJSON = oSalesController.MCSales().searchSalesDetailPromotion(pnUnitRow, lsValuse, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                return;
                            }
                            tfPomoCode.setText(oSalesController.MCSales().getDetail(pnUnitRow).PromotionMaster().getPromoDescription());
                            break;
                        case "tfGawayBarrcode":
                        case "tfGawayDesc":
                            boolean lbByCode = "tfGawayBarrcode".equals(lsID);   // use your actual id variable
                            poJSON = oSalesController.MCSales().searchSalesGiveAway(pnGawayRow, lsValuse, lbByCode);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                return;
                            }
                            tfGawayBarrcode.setText(oSalesController.MCSales().getSalesGiveaways(pnGawayRow).Inventory().getBarCode());
                            tfGawayDesc.setText(oSalesController.MCSales().getSalesGiveaways(pnGawayRow).Inventory().getDescription());
                            tfGawayGiven.setText(String.valueOf(oSalesController.MCSales().getSalesGiveaways(pnGawayRow).getGiven()));
                            tfGawayIssued.setText(String.valueOf(oSalesController.MCSales().getSalesGiveaways(pnGawayRow).getIssued()));
                            tfGawayQty.setText(String.valueOf(oSalesController.MCSales().getSalesGiveaways(pnGawayRow).getQuantity()));
                            break;
                        case "tfPaymentTerm":
//                            ShowMessageFX.Error(SalesTransactionStatic.MSG_TEXTFIELD_UNDER_DEVELOPMENT, pxeModuleName, null);
                            break;
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
                // TODO: write lsValue to the model, e.g.
                // case "tfMCUnitPrice": trans().getModel().setUnitPrice(lsValue); break;
                case "tfMCRemarks":
                    poJSON = oSalesController.MCSales().getDetail(pnUnitRow).setNotes(lsValue);
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                        return;
                    }
                    break;
                case "tfMCWarrantyNo":
                    poJSON = oSalesController.MCSales().getDetail(pnUnitRow).setWarrantyNo(lsValue);
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                        return;
                    }
                case "tfMCKeyCode":
                    poJSON = oSalesController.MCSales().getDetail(pnUnitRow).setKeyCode(lsValue);
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                        return;
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

            Platform.runLater(() -> btnNew.fire());
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    private void initButton(int fnValue) {
        CustomCommonUtil.setVisible(false, btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnHistory, btnClose);
        CustomCommonUtil.setManaged(false, btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnHistory, btnClose);
        switch (fnValue) {
            case EditMode.UNKNOWN:
            case EditMode.READY:
                CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnUpdate, btnHistory, btnClose);
                CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnUpdate, btnHistory, btnClose);
                break;
            case EditMode.ADDNEW:
            case EditMode.UPDATE:
                CustomCommonUtil.setVisible(true, btnSave, btnCancel);
                CustomCommonUtil.setManaged(true, btnSave, btnCancel);
                break;
            default:
                throw new IllegalStateException(SalesTransactionStatic.MSG_UNEXPECTED_VALUE + fnValue);
        }
        pbEditable.set(fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        loadStatus(fnValue);
    }

    private void ClickButton() {
        for (Button btn : new Button[]{btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnHistory, btnClose}) {
            btn.setOnAction(this::handleButtonAction);
        }
    }

    private void handleButtonAction(ActionEvent event) {
        String lsID = ((Button) event.getSource()).getId();
        try {
            switch (lsID) {
                case "btnNew":
                    pnEditMode = EditMode.ADDNEW;
                    poJSON = oSalesController.MCSales().newTransaction();
                    if(poJSON.get("result").equals("error")) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                        break;
                    }
                    oSalesController.MCSales().getMaster().setIndustryCode(psIndustryId);
                    oSalesController.MCSales().getMaster().setCompanyID(psCompanyId);
                    LoadMaster();
                    loadTableMCItem();
                    loadTableGiveaways();
                    break;
                case "btnBrowse":
                    pnEditMode = EditMode.READY;
                    LoadMaster();
                    LoadClientInfo();
                    break;
                case "btnUpdate":
                    pnEditMode = EditMode.UPDATE;
                    break;
                case "btnSave":
                    pnEditMode = EditMode.ADDNEW;
                    break;
                case "btnApproved":
                case "btnVoid":
                    pnEditMode = EditMode.READY;
                    break;
                case "btnHistory":
                    ShowMessageFX.Error(SalesTransactionStatic.MSG_UNDER_DEVELOPMENT, pxeModuleName, null);
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_CANCEL, pxeModuleName, null)) {
                        pnEditMode = EditMode.UNKNOWN;
                        ClearAll();
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
                if (oApp != null) oApp.rollbackTrans();
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

    /** Shows the form state. TODO(model): show the real transaction status (open / confirmed / void) for loaded records. */
    private void loadStatus(int fnEditMode) {
        String lsStatus;
        switch (fnEditMode) {
            case EditMode.ADDNEW:
                lsStatus = SalesTransactionStatic.STATUS_NEW;
                break;
            case EditMode.UPDATE:
                lsStatus = SalesTransactionStatic.STATUS_UPDATING;
                break;
            case EditMode.READY:
                lsStatus = SalesTransactionStatic.STATUS_READY;
                break;
            default:
                lsStatus = SalesTransactionStatic.STATUS_UNKNOWN;
                break;
        }
        lblStatus.setText(lsStatus);
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
        setComboItems(cmbClientType, SalesTransactionStatic.CLIENT_TYPES);
        setComboItems(cmbVehicleType, SalesTransactionStatic.VEHICLE_TYPES);
        setComboItems(cmbRegistrationType, SalesTransactionStatic.REGISTRATION_TYPES);
        setComboItems(cmbRegInsurType, SalesTransactionStatic.INSURANCE_TYPES);
        setComboItems(cmbInsuranceType, SalesTransactionStatic.INSURANCE_TYPES);
        setComboItems(cmbRegInsuranceOption, SalesTransactionStatic.INSURANCE_OPTIONS);
        setComboItems(cmbPaymentTranType, SalesTransactionStatic.PAYMENT_TRAN_TYPES);
        setComboItems(cmbPaymentForm, SalesTransactionStatic.PAYMENT_FORMS);
        setComboItems(cmbPaymentCWT, SalesTransactionStatic.CWT_RATES);
        setComboItems(cmbAttachmentType, SalesTransactionStatic.ATTACHMENT_TYPES);

        for (final ComboBox lcmb : poCombos) {
            lcmb.disableProperty().bind(pbEditable.not());
            lcmb.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                if (lcmb == cmbPaymentTranType) {
                    pbCreditPayment.set(newIdx.intValue() == SalesTransactionStatic.PAYMENT_CREDIT);   // always keep this in sync
                }
                if (!pbLoaded || pbSyncing) {
                    return;
                }
                cmb_Changed(lcmb, newIdx.intValue());
            });
        }

        // Term only applies to a credit transaction
        tfPaymentTerm.disableProperty().bind(pbCreditPayment.not());
    }

    private void cmb_Changed(ComboBox fcmb, int fnIndex) {
        if (!canEdit() || fnIndex < 0) {
            return;
        }
        try {
            if (fcmb == cmbPaymentTranType && fnIndex != SalesTransactionStatic.PAYMENT_CREDIT) {
                tfPaymentTerm.clear();
                setDate(dpPaymentDueDate, null);
            }

            // index -> value stored in the database
            String lsValue = String.valueOf(fnIndex);

            if (fcmb == cmbClientType) {
                poJSON = oSalesController.MCSales().getMaster().Client().setClientType(lsValue);          // placeholder
            } else if (fcmb == cmbPaymentTranType) {
                poJSON = oSalesController.MCSales().getMaster().setTransactionType(lsValue);     // placeholder
            } else if (fcmb == cmbPaymentForm) {
                poJSON = oSalesController.MCSales().getMaster().setPaymentForm(lsValue);         // placeholder
            } else if (fcmb == cmbVehicleType) {
                poJSON = oSalesController.MCSales().getDetail(pnUnitRow).InventorySerial().setCondition(lsValue); // placeholder
            } else {
                poJSON = null;   // combos with no model field yet
            }

            if (poJSON != null && "error".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                return;
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
            lcb.disableProperty().bind(pbEditable.not());
            lcb.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (!pbLoaded || pbSyncing) {
                    return;
                }
                cb_Changed(lcb, isSelected);
            });
        }
    }

    private void cb_Changed(CheckBox fcb, boolean fbSelected) {
        if (!canEdit()) {
            return;
        }
        try {
            // TODO(model): write fbSelected to the model, e.g.
            // if (fcb == cbRegIsCharge) trans().getModel().setRegistrationCharge(fbSelected);
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

    /** Sets a date programmatically without triggering the change handler. */
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
            // TODO(model): write the date to the model, e.g.
            // if (fdp == dpTransDate) trans().getModel().setTransactionDate(<java.util.Date from fdp.getValue()>);
            loadSummary();
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    // =========================================================================
    // Tables
    // NOTE: ModelTableMain / ModelTableDetail were not provided. Columns are bound to the
    // properties "index01", "index02", ... in the order the columns are declared in the FXML.
    // If your model classes use other property names, change them in the initTable*() methods.
    // =========================================================================
    /** Remove column: a trash button per row, enabled only while editing. */
    private <S> void initRemoveColumn(TableColumn<S, String> fcol, final IntConsumer fiAction) {
        if (fcol == null) {
            return;
        }
        fcol.setSortable(false);
        fcol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(""));
        fcol.setCellFactory(col -> new TableCell<S, String>() {
            private final Button btnRow = new Button();

            {
                btnRow.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TRASH));
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
                setGraphic(fbEmpty ? null : btnRow);
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
        tblColDetailNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblColDetailBrand.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblColDetailModel.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblColDetailEngineNo.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblColDetailFramNo.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblColDetailVariant.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblColDetailWarrantyNo.setCellValueFactory(new PropertyValueFactory<>("index07"));
        tblColDetailKeySerial.setCellValueFactory(new PropertyValueFactory<>("index08"));
        tblColDetailRegType.setCellValueFactory(new PropertyValueFactory<>("index09"));
        tblColDetailNetSRP.setCellValueFactory(new PropertyValueFactory<>("index10"));
        tblColDetailSRP.setCellValueFactory(new PropertyValueFactory<>("index11"));
        tblColDetailInsur.setCellValueFactory(new PropertyValueFactory<>("index12"));
        tblColDetailDiscount.setCellValueFactory(new PropertyValueFactory<>("index13"));
        tblColDetailAddDisc.setCellValueFactory(new PropertyValueFactory<>("index14"));
        tblColDetailRemarks.setCellValueFactory(new PropertyValueFactory<>("index15"));
        initRemoveColumn(tblColDetailQOH21, this::removeUnit);
        tblUnits.setItems(unit_data);
        tblUnits.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_UNITS));
        tblUnits.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            pnUnitRow = newIdx.intValue();
            if (pnUnitRow >= 0) {
                loadUnitRecord(pnUnitRow);
            }
        });
        lockColumnReordering(tblUnits);
    }

    /** Registration and insurance list (Registration & Insurance tab). */
    private void initTableRegAndInsur() {
        tblRowRegAndInsurNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblRowRegAndInsurEngine.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblRowRegAndInsurSource.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblRowRegAndInsurDesc.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblRowRegAndInsurAmount.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblRowRegAndInsurStat.setCellValueFactory(new PropertyValueFactory<>("index06"));
        initRemoveColumn(tblRowRegAndInsurAction, this::removeRegAndInsur);
        tblRegAndIsur.setItems(reginsur_data);
        tblRegAndIsur.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_REGINSUR));
        tblRegAndIsur.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            pnRegInsurRow = newIdx.intValue();
            if (pnRegInsurRow >= 0) {
                loadRegAndInsurRecord(pnRegInsurRow);
            }
        });
        lockColumnReordering(tblRegAndIsur);
    }

    private void initTableGiveaways() {
        tblRowGawayNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblRowGawayBarrcode.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblRowGawayDesc.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblRowGawayRemarks.setCellValueFactory(new PropertyValueFactory<>("index04"));   // new column
        tblRowGawayQOH.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblRowGawayGiven.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblRowGawayIssued.setCellValueFactory(new PropertyValueFactory<>("index07"));
        tblRowGawayQty.setCellValueFactory(new PropertyValueFactory<>("index08"));
        tblRowGawayStatus.setCellValueFactory(new PropertyValueFactory<>("index09"));
        initRemoveColumn(tblRowGawayAction11211, this::removeGiveaway);
        tblGawayItem.setItems(gaway_data);
        tblGawayItem.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_GIVEAWAYS));
        tblGawayItem.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            pnGawayRow = newIdx.intValue();
            if (pnGawayRow >= 0) {
                loadGiveawayRecord(pnGawayRow);
            }
        });
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

    private void initTableAttachments() {
        tblRowNoAttachment.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblFileNameAttachment.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblAttachments.setItems(attach_data);
        tblAttachments.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_ATTACHMENTS));
        tblAttachments.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (newIdx.intValue() >= 0 && newIdx.intValue() != pnAttachRow) {
                showAttachment(newIdx.intValue());
            }
        });
        lockColumnReordering(tblAttachments);
    }

    /**
     * JavaFX 8 has no per-column "reorderable" flag, so the header row is told to cancel
     * any column drag. The listener is attached to each header row only once.
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

    /** Called when a unit row is selected. */
    private void loadUnitRecord(int fnRow) {
        pbSyncing = true;
        try {
            // TODO(model): fill the motorcycle fields (incl. tfMCUnitPrice), combos and check boxes
            // from the unit at fnRow (e.g. trans().Detail(fnRow)); also set tfRegEngineNo / tfRegFrameNo.
        } finally {
            pbSyncing = false;
        }
    }

    /** Called when a registration / insurance row is selected. */
    private void loadRegAndInsurRecord(int fnRow) {
        pbSyncing = true;
        try {
            // TODO(model): fill the registration and insurance fields, combos and check boxes from the row at fnRow.
        } finally {
            pbSyncing = false;
        }
    }

    /** Called when a giveaway row is selected. */
    private void loadGiveawayRecord(int fnRow) {
        pbSyncing = true;
        try {
            // TODO(model): fill poGiveawayFields (incl. tfGawayRemarks) from the giveaway at fnRow.
        } finally {
            pbSyncing = false;
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

        Task<List<ModelTableMain>> task = new Task<List<ModelTableMain>>() {
            private double pdTotal = 0.0;

            @Override
            protected List<ModelTableMain> call() throws Exception {
                MCSales loSales = oSalesController.MCSales();
                List<ModelTableMain> laRows = new ArrayList<ModelTableMain>();
                String lsBlank = "";

                int lnCount = loSales.getDetailCount();
                if (lbEditing && (lnCount == 0 || loSales.getDetail(lnCount).getStockID() != null
                        && !loSales.getDetail(lnCount).getStockID().isEmpty())) {
                    loSales.getDetail(++lnCount);   // blank row for the next unit
                }

                for (int lnCtr = 1; lnCtr <= lnCount; lnCtr++) {
                    Model_Sales_Detail loRow = loSales.getDetail(lnCtr);
                    if (loRow == null) {
                        break;
                    }
                    String lsNo = String.valueOf(lnCtr);

                    if (loRow.getStockID() == null || loRow.getStockID().isEmpty()) {
                        laRows.add(new ModelTableMain(lsNo, lsBlank, lsBlank, lsBlank, lsBlank, lsBlank, lsBlank,
                                lsBlank, lsBlank, lsBlank, lsBlank, lsBlank, lsBlank, lsBlank, lsBlank));
                        continue;
                    }

                    double lnNet = ((loRow.getUnitPrice() - loRow.getDiscount() - loRow.getAddDiscount())
                            + loRow.getFreight() + loRow.getInsuranceAmount()) * loRow.getQuantity();
                    pdTotal += lnNet;

                    String lsBrand = "", lsModel = "", lsEngine = "", lsFrame = "", lsVariant = "";
                    try {
                        lsBrand = String.valueOf(loRow.Inventory().Brand().getDescription());
                        lsModel = String.valueOf(loRow.Inventory().Model().getDescription());
                        lsVariant = String.valueOf(loRow.Inventory().Variant().getDescription());
                        lsEngine = String.valueOf(loRow.InventorySerial().getSerial01());   // check getter
                        lsFrame = String.valueOf(loRow.InventorySerial().getSerial02());    // check getter
                    } catch (Exception ex) {
                        Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
                    }

                    laRows.add(new ModelTableMain(lsNo, lsBrand, lsModel, lsEngine, lsFrame, lsVariant,
                            String.valueOf(loRow.getWarrantyNo()), String.valueOf(loRow.getKeyCode()), "",
                            String.format("%,.2f", lnNet),
                            String.format("%,.2f", loRow.getUnitPrice()),
                            String.format("%,.2f", loRow.getInsuranceAmount()),
                            String.format("%,.2f", loRow.getDiscount()),
                            String.format("%,.2f", loRow.getAddDiscount()),
                            String.valueOf(loRow.getNotes())));
                }
                return laRows;
            }

            @Override
            protected void succeeded() {
                unit_data.setAll(getValue());
                if (unit_data.isEmpty()) {
                    pnUnitRow = -1;
                    tblUnits.setPlaceholder(loading.placeholderLabel);
                } else {
                    JFXUtil.selectAndFocusRow(tblUnits, lnSelectRow >= 0 && lnSelectRow < unit_data.size() ? lnSelectRow : 0);
                    pnUnitRow = tblUnits.getSelectionModel().getSelectedIndex();
                    loadUnitRecord(pnUnitRow);
                    tblUnits.toFront();
                }
                tfSummaryUnitPrice.setText(String.format("%,.2f", pdTotal));
                loadSummary();
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, getException());
                tblUnits.setPlaceholder(loading.placeholderLabel);
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();
    }
    private void loadTableGiveaways() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblGawayItem.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        final boolean lbEditing = pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE;
        final int lnSelectRow = pnGawayRow;

        Task<List<ModelTableMain>> task = new Task<List<ModelTableMain>>() {
            @Override
            protected List<ModelTableMain> call() throws Exception {
                MCSales loSales = oSalesController.MCSales();
                List<ModelTableMain> laRows = new ArrayList<ModelTableMain>();

                int lnCount = loSales.getCountSalesGiveAway();
                if (lbEditing && (lnCount == 0 || loSales.getSalesGiveaways(lnCount).getStockID() != null
                        && !loSales.getSalesGiveaways(lnCount).getStockID().isEmpty())) {
                    loSales.getSalesGiveaways(++lnCount);   // blank row for the next giveaway
                }

                for (int lnCtr = 1; lnCtr <= lnCount; lnCtr++) {   // entry numbers start at 1
                    Model_Sales_Giveaways loRow = loSales.getSalesGiveaways(lnCtr);
                    if (loRow == null) {
                        break;
                    }
                    String lsNo = String.valueOf(lnCtr);

                    if (loRow.getStockID() == null || loRow.getStockID().isEmpty()) {
                        laRows.add(new ModelTableMain(lsNo, "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                        continue;
                    }

                    String lsBarcode = "", lsDesc = "", lsQoh = "";
                    try {
                        lsBarcode = String.valueOf(loRow.Inventory().getBarCode());
                        lsDesc = String.valueOf(loRow.Inventory().getDescription());
//                        lsQoh = String.valueOf(loRow.Inventory());   // check getter
                    } catch (Exception ex) {
                        Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
                    }

                    laRows.add(new ModelTableMain(lsNo, lsBarcode, lsDesc,
//                            String.valueOf(loRow.getRemarks()),        // 04 Remarks (check getter)
                            "",
                            lsQoh,                                     // 05 QOH
                            String.valueOf(loRow.getGiven()),          // 06 Given
                            String.valueOf(loRow.getIssued()),         // 07 Issued
                            String.valueOf(loRow.getQuantity()),       // 08 Qty
                            "",                                        // 09 Status
                            "", "", "", "", "", ""));
                }
                return laRows;
            }

            @Override
            protected void succeeded() {
                gaway_data.setAll(getValue());
                if (gaway_data.isEmpty()) {
                    pnGawayRow = -1;
                    tblGawayItem.setPlaceholder(loading.placeholderLabel);
                } else {
                    JFXUtil.selectAndFocusRow(tblGawayItem,
                            lnSelectRow >= 0 && lnSelectRow < gaway_data.size() ? lnSelectRow : 0);
                    pnGawayRow = tblGawayItem.getSelectionModel().getSelectedIndex();
                    loadGiveawayRecord(pnGawayRow);
                    tblGawayItem.toFront();
                }
                loadSummary();
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, getException());
                tblGawayItem.setPlaceholder(loading.placeholderLabel);
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

    private void removeRegAndInsur(int fnRow) {
        if (!canEdit() || fnRow < 0 || fnRow >= reginsur_data.size()) {
            return;
        }
        if (!ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_REMOVE_REGINSUR, pxeModuleName, null)) {
            return;
        }
        // TODO(model): remove the registration / insurance entry from the transaction first, then reload the table.
        reginsur_data.remove(fnRow);
        pnRegInsurRow = -1;
        loadSummary();
    }

    private void removeGiveaway(int fnRow) {
        if (!canEdit() || fnRow < 0 || fnRow >= gaway_data.size()) {
            return;
        }
        if (!ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_REMOVE_GIVEAWAY, pxeModuleName, null)) {
            return;
        }
        // TODO(model): remove the giveaway from the transaction first, then reload the table from the model.
        gaway_data.remove(fnRow);
        pnGawayRow = -1;
        loadSummary();
    }

    // =========================================================================
    // Attachment tab
    // =========================================================================
    private void initAttachmentTab() {
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(stackPane.widthProperty().subtract(4));
        imageView.fitHeightProperty().bind(stackPane.heightProperty().subtract(4));
        tfAttachmentNo.setEditable(false);

        btnAddAttachment.disableProperty().bind(pbEditable.not());
        btnRemoveAttachment.disableProperty().bind(pbEditable.not().or(Bindings.isEmpty(poAttachFiles)));

        btnAddAttachment.setOnAction(e -> addAttachment());
        btnRemoveAttachment.setOnAction(e -> removeAttachment());
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

    private void addAttachment() {
        if (!canEdit()) {
            return;
        }
        if (cmbAttachmentType.getSelectionModel().getSelectedIndex() < 0) {
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
        if (poAttachFiles.contains(loFile)) {
            ShowMessageFX.Error(SalesTransactionStatic.MSG_ATTACHMENT_DUPLICATE, pxeModuleName, null);
            return;
        }
        Image loImage = new Image(loFile.toURI().toString());
        if (loImage.isError()) {
            ShowMessageFX.Error(SalesTransactionStatic.MSG_ATTACHMENT_INVALID_IMAGE, pxeModuleName, null);
            return;
        }

        // TODO(model): add the attachment (file + type from cmbAttachmentType) to the transaction,
        // then reload attach_data so the table shows the new row.
        poAttachFiles.add(loFile);
        displayAttachment(poAttachFiles.size() - 1, loImage);
    }

    private void removeAttachment() {
        if (!canEdit() || pnAttachRow < 0 || pnAttachRow >= poAttachFiles.size()) {
            return;
        }
        if (!ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_REMOVE_ATTACHMENT, pxeModuleName, null)) {
            return;
        }
        int lnRow = pnAttachRow;
        // TODO(model): remove the attachment from the transaction first, then reload attach_data.
        poAttachFiles.remove(lnRow);
        if (lnRow < attach_data.size()) {
            attach_data.remove(lnRow);
        }
        showAttachment(Math.min(lnRow, poAttachFiles.size() - 1));   // -1 clears the preview when none are left
    }

    private void showAttachment(int fnIndex) {
        Image loImage = null;
        if (fnIndex >= 0 && fnIndex < poAttachFiles.size()) {
            loImage = new Image(poAttachFiles.get(fnIndex).toURI().toString());
        }
        displayAttachment(fnIndex, loImage);
    }

    private void displayAttachment(int fnIndex, Image foImage) {
        if (fnIndex < 0 || fnIndex >= poAttachFiles.size()) {
            pnAttachRow = -1;
            imageView.setImage(null);
            tfAttachmentNo.clear();
        } else {
            pnAttachRow = fnIndex;
            imageView.setImage(foImage);
            tfAttachmentNo.setText(String.format(SalesTransactionStatic.ATTACH_POSITION_FORMAT, fnIndex + 1, poAttachFiles.size()));
            if (fnIndex < attach_data.size()) {
                tblAttachments.getSelectionModel().select(fnIndex);
            }
        }
        updateAttachmentArrows();
    }

    private void updateAttachmentArrows() {
        btnArrowLeft.setDisable(pnAttachRow <= 0);
        btnArrowRight.setDisable(pnAttachRow < 0 || pnAttachRow >= poAttachFiles.size() - 1);
    }

    // =========================================================================
    // Other buttons
    // =========================================================================
    private void initOtherButtons() {
        btnAddClient.disableProperty().bind(pbEditable.not());
        btnAddClient1.disableProperty().bind(pbEditable.not());
        btnAddClient.setOnAction(e -> addClient(false));
        btnAddClient1.setOnAction(e -> addClient(true));
    }

    private void addClient(boolean fbCoClient) {
        // TODO: open the client search / add dialog and write the chosen client (or co-client) to the model.
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
    }

    /**
     * Refreshes the summary. Fields that mirror an input on the form are filled from the form;
     * TODO(model): the computed fields (unit price, total, giveaways, VAT / non-VAT / zero-rated
     * sales, VAT amount) must come from the model once the transaction totals are available.
     */
    private void loadSummary() {
        tfSummaryCustomer.setText(taCustomerName.getText());
        tfSummaryTranType.setText(comboText(cmbPaymentTranType));
        tfSummaryPaymentForm.setText(comboText(cmbPaymentForm));
        tfSummaryTerm.setText(tfPaymentTerm.getText());
        tfSummaryDueDate.setText(dateText(dpPaymentDueDate));
        tfSummaryDiscount.setText(tfPromoDisc.getText());
        tfSummaryAddDiscount.setText(tfPromoAddDisc.getText());
        tfSummaryFreight.setText(tfFreight.getText());
        tfSummaryRegistration.setText(tfMCRegisAmt.getText());
        tfSummaryInsurance.setText(tfMCInsurAmt.getText());
        tfSummaryCwt.setText(tfPaymentCwt.getText());
        tfSummaryAdvances.setText(tfPaymentAdvances.getText());
        tfSummaryAmountPaid.setText(tfPaymentAmountPaid.getText());
        tfSummaryBalance.setText(tfPaymentBalance.getText());
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
        pnUnitRow = -1;
        pnGawayRow = -1;
        pnRegInsurRow = -1;
        showAttachment(-1);
    }

    private void LoadMaster() {
        lblStatus.setText(SalesTransactionStatic.STATUS_NEW);
        tfTransNo.setText(oSalesController.MCSales().getMaster().getTransactionNo());
        tfSourceNo.setText(oSalesController.MCSales().getMaster().getSourceNo());
        tfSourceDesc.setText(oSalesController.MCSales().getMaster().getSourceCode());
//        tfReceipt.setText(oSalesController.MCSales().getMaster().getr());
        taCustomerName.setText(tfClientName.getText() + " | " + tfClientContactNo.getText() + " | " + tfClientAddress.getText());
    }

    /** TODO(model): replace the sample values with the client / co-client from the model. */
    private void LoadClientInfo() {
        pbSyncing = true;
        try {
            cmbClientType.getSelectionModel().select(0);

            tfClientName.setText("DE CELIS, TEEJEI FERNANDEZ");
            tfClientGender.setText("Male");
            tfClientCitizenship.setText("Filipino");
            setDate(dpClientBday, LocalDate.of(1995, 8, 25));
            tfClientBirthplace.setText("STA. BARBARA, PANGASINAN");
            tfClientOccupation.setText("PROGRAMMER");
            tfClientTinNo.setText("123-456-789-000");
            tfClientFathersName.setText("");
            tfClientMothersName.setText("");
            tfClientContactNo.setText("09123456987");
            tfClientEmail.setText("tfdecelis@guanzongroup.com.ph");
            tfClientAddress.setText("264 BRGY. DALONGUE STA. BARBARA, PANGASINAN,2419");
            tfClientLTOID.setText("");
            tfClientLTOAddress.setText("");

            tfCoClientName.setText("LOMIBAO, JUDITH MARIE PEREZ");
            tfCoClientGender.setText("Female");
            tfCoClientCitezenship.setText("Filipino");
            setDate(dpCoClientBirthday, null);
            tfCoClientBirthplace.setText("");
            tfCoClientOccupation.setText("");
            tfCoClientTinNo.setText("");
            tfCoClientFathersName.setText("");
            tfCoClientMothersName.setText("");
            tfCoClientContactNo.setText("09123456988");
            tfCoClientEmail.setText("jmpdecelis@guanzongroup.com.ph");
            tfCoClientAddress.setText("264 BRGY. DALONGUE STA. BARBARA, PANGASINAN,2419");
            tfCoClientLTOID.setText("");
            tfCoClientLTOAddress.setText("");
        } finally {
            pbSyncing = false;
        }
    }

    private void LoadMotorcyleAndPricing() {
        tfTransNo.setText("M00126000001");
        tfSourceNo.setText("M00126000001");
        tfSourceDesc.setText("Sales Qoutation");
        tfReceipt.setText("");
    }

    /* Clears all text fields in the form.
     * This method is called when the user cancels a record or closes the form without saving.
     * It ensures that any unsaved data is not kept in the text fields.
     * */
    private void ClearAll() {
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
}