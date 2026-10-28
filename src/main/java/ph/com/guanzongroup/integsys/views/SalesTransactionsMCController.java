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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.*;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.MCSales;
import ph.com.guanzongroup.cas.sales.SalesQoutationVersion;
import ph.com.guanzongroup.cas.sales.constant.SalesTransactionStatic;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Giveaways;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Insurance;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Registration;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationStatic;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.io.File;
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
 * CHANGES FOR THE SERIAL SEARCH FIX (units not showing in table / text fields)
 *   1. F3 serial search: pnUnitRow is guarded, the unit price is copied onto the detail,
 *      and the fields are filled by loadTableMCItem() -> loadUnitRecord() only.
 *   2. isBlankUnit(): a row also counts as filled when it has a serial id.
 *   3. loadUnitRecord(): tfMCColor is now filled.
 *   4. loadTableMCItem(): Color() lookup is warmed on the background thread.
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
    private int pnUnitLoadSeq = 0;
    private int pnRegInsurLoadSeq = 0;
    private int pnGawayLoadSeq = 0;
    /** The rows that were selected when a field got focus: the field's edit belongs to THESE rows even if the user
     *  clicks another row (which changes the selection before the field's lost-focus handler runs). */
    private Model_Sales_Detail poEditUnit;
    private Model_Sales_Giveaways poEditGaway;


    private int pnUnitRow = -1;
    private int pnGawayRow = -1;
    private int pnRegInsurRow = -1;
    private int pnAttachRow = -1;
    private boolean pbSyncing = false;
    final int lnSeq = ++pnUnitLoadSeq;// true while combos/checkboxes are set programmatically

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
    private final ObservableList<Model_Sales_Detail> unit_data = FXCollections.observableArrayList();
    private final ObservableList<Model_Sales_Giveaways> gaway_data = FXCollections.observableArrayList();
    private final ObservableList<Model> reginsur_data = FXCollections.observableArrayList();   // Model_Sales_Registration / Model_Sales_Insurance rows
    private final ObservableList<ModelTableDetail> invoice_data = FXCollections.observableArrayList();
    private final ObservableList<ModelTableDetail> attach_data = FXCollections.observableArrayList();

    // =========================================================================
    // Root containers & buttons
    // =========================================================================
    @FXML private AnchorPane ChildAnchorPane;
    @FXML private AnchorPane apSearchMaster;
    @FXML private AnchorPane AnchorInputs;
    @FXML private AnchorPane AnchorMCSales;
    @FXML private AnchorPane AnchorClient;
    @FXML private AnchorPane AnchorAttach;
    @FXML private AnchorPane AnchorAttach2;
    @FXML private AnchorPane AnchorMCPrice;
    @FXML private AnchorPane AnchorRegInsur;
    @FXML private AnchorPane AnchorGiveaway;
    @FXML private AnchorPane AnchorPaymType;
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
    @FXML private TextField tfRegInsurType;
    @FXML private CheckBox cbRegIsCharge;
    @FXML private TextField tfInsurType;
    //    @FXML private ComboBox cmbRegInsuranceOption;
    @FXML private TextField tfInsurDesc;
    @FXML private TextField tfInsurYear;
    @FXML private TextField tfInsurPayer;
    @FXML private TextField tfInsurAmt;
    @FXML private CheckBox cbInsurIsCharge;

    @FXML private TableView<Model> tblRegAndIsur;
    @FXML private TableColumn<Model, String> tblRowRegAndInsurNo;
    @FXML private TableColumn<Model, String> tblRowRegAndInsurEngine;
    @FXML private TableColumn<Model, String> tblRowRegAndInsurSource;
    @FXML private TableColumn<Model, String> tblRowRegAndInsurDesc;
    @FXML private TableColumn<Model, String> tblRowRegAndInsurAmount;
    @FXML private TableColumn<Model, String> tblRowRegAndInsurStat;
    @FXML private TableColumn<Model, String> tblRowRegAndInsurAction;   // Remove column

    // =========================================================================
    // Giveaways tab
    // =========================================================================
    @FXML private TextField tfGawayBarrcode;
    @FXML private TextField tfGawayDesc;
    @FXML private TextField tfGawayRemarks;
    @FXML private TextField tfGawayGiven;
    @FXML private TextField tfGawayIssued;
    @FXML private TextField tfGawayQty;
    @FXML private TableView<Model_Sales_Giveaways> tblGawayItem;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayNo;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayBarrcode;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayDesc;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayRemarks;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayQOH;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayGiven;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayIssued;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayQty;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayStatus;
    @FXML private TableColumn<Model_Sales_Giveaways, String> tblRowGawayAction11211;

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
    @FXML private TableView<Model_Sales_Detail> tblUnits;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailNo;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailBrand;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailModel;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailEngineNo;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailFramNo;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailVariant;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailWarrantyNo;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailKeySerial;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailFreight;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailNetSRP;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailSRP;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailInsReg;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailDiscount;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailAddDisc;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailRemarks;
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailQOH21;   // used as the Remove column
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailQOH22;   // unused in FXML, dropped at runtime
    @FXML private TableColumn<Model_Sales_Detail, String> tblColDetailQOH23;   // unused in FXML, dropped at runtime

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
                tfInsurDesc, tfInsurYear, tfInsurPayer, tfInsurAmt,tfRegInsurType,tfInsurType);

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

    /** Attaches the key press and lost focus handlers to every grouped field in one pass. */
    private void initFieldHandlers() {
        for (TextInputControl txtInput : poAllFields) {
            txtInput.setOnKeyPressed(this::txtField_KeyPressed);
            txtInput.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    poEditUnit = (pnUnitRow >= 1 && pnUnitRow <= unit_data.size()) ? unit_data.get(pnUnitRow - 1) : null;
                    poEditGaway = selectedGiveaway();
                } else {
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
                            int lnClientType = cmbClientType.getSelectionModel().getSelectedIndex();
                            if (lnClientType != 0 && lnClientType != 1) {
                                ShowMessageFX.Warning("Please select valid client type.", "Warning", null);
                                cmbClientType.requestFocus();
                                break;
                            }
                            oSalesController.MCSales().setClientType(String.valueOf(lnClientType));
                            poJSON = oSalesController.MCSales().searchClient(txtInput.getText(), true);
                            if (poJSON.get("result").equals("error")) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                break;
                            }
                            LoadClientInfo();
                            taCustomerName.setStyle("-fx-font-weight: bold;");
                            taCustomerName.setText(tfClientName.getText() + " | " + tfClientContactNo.getText() + " | " + tfClientAddress.getText());
                            // the search may have loaded a source quotation / reservation into the model:
                            // forget the previous client's selected rows so the table reloads from the first row
                            pnUnitRow = -1;
                            pnGawayRow = -1;
                            loadTableMCItem();
                            loadTableGiveaways();
                            loadTableRegAndInsur();
                            break;
                        case "tfCoClientName":
                            oSalesController.MCSales().getMaster().Client().setClientType("0");
                            poJSON = oSalesController.MCSales().searchCoClient(txtInput.getText(), true);
                            if(poJSON.get("result").equals("error")) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
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
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
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
                            poJSON = oSalesController.MCSales().searchSalesDetailPromotion(pnUnitRow, lsValuse, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                return;
                            }
                            tfPomoCode.setText(oSalesController.MCSales().getDetail(pnUnitRow).PromotionMaster().getPromoDescription());
                            loadTableMCItem();          // the promo can change discount / freight
                            loadTableGiveaways();       // ...add giveaways
                            loadTableRegAndInsur();     // ...and registration / insurance rows
                            break;
                        case "tfGawayBarrcode":
                        case "tfGawayDesc":
                            boolean lbByCode = "tfGawayBarrcode".equals(lsID);   // use your actual id variable
                            Model_Sales_Giveaways loGawayRow = selectedGiveaway();
                            if (loGawayRow == null && !gaway_data.isEmpty()) {
                                loGawayRow = gaway_data.get(gaway_data.size() - 1);   // nothing selected: use the last (blank) row
                            }
                            if (loGawayRow == null) {
                                break;
                            }
                            poJSON = oSalesController.MCSales().searchSalesGiveAway(loGawayRow.getEntryNo(), lsValuse, lbByCode);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                return;
                            }
                            loadTableGiveaways();   // store the searched giveaway in the table
                            break;
                        case "tfPaymentTerm":
                            ShowMessageFX.Error(SalesTransactionStatic.MSG_TEXTFIELD_UNDER_DEVELOPMENT, pxeModuleName, null);
                            break;
                        case "tfRegInsurType":
                            int lnTypeRow = regInsurRowForSearch(true);   // row of the selected unit's serial (created if none yet)
                            if (lnTypeRow < 1) {
                                return;
                            }
                            poJSON = oSalesController.MCSales().searchInsuranceTypeRegistration(lnTypeRow, lsValuse, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                return;
                            }
                            loadTableRegAndInsur();   // show the searched registration in the table

                            break;
                        case "tfInsurType":
                            int lnInsRow = regInsurRowForSearch(false);   // row of the selected unit's serial (created if none yet)
                            if (lnInsRow < 1) {
                                return;
                            }
                            poJSON = oSalesController.MCSales().searchInsuranceTypeInsurance(lnInsRow, lsValuse, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                                return;
                            }
                            loadTableRegAndInsur();   // show the searched insurance in the table
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
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
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
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                        return;
                    }
                    loadTableGiveaways();
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

            Platform.runLater(() -> btnNew.fire());
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    private void initButton(int fnValue) {
        CustomCommonUtil.setVisible(false, btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnHistory, btnClose);
        CustomCommonUtil.setManaged(false, btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApproved, btnVoid, btnHistory, btnClose);
//        AnchorMCSales.setDisable(true);
        AnchorClient.setDisable(true);
        AnchorAttach.setDisable(true);
        AnchorAttach2.setDisable(true);
        AnchorMCPrice.setDisable(true);
        AnchorRegInsur.setDisable(true);
        AnchorGiveaway.setDisable(true);
        AnchorPaymType.setDisable(true);
        switch (fnValue) {
            case EditMode.UNKNOWN:
                CustomCommonUtil.setVisible(true, btnBrowse, btnNew,  btnHistory, btnClose);
                CustomCommonUtil.setManaged(true, btnBrowse, btnNew,  btnHistory, btnClose);
                break;
            case EditMode.READY:
                CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnUpdate, btnHistory, btnClose);
                CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnUpdate, btnHistory, btnClose);
                break;
            case EditMode.ADDNEW:
            case EditMode.UPDATE:
                CustomCommonUtil.setVisible(true, btnSave, btnCancel);
                CustomCommonUtil.setManaged(true, btnSave, btnCancel);
//                AnchorMCSales.setDisable(false);
                AnchorClient.setDisable(false);
                AnchorAttach.setDisable(false);
                AnchorAttach2.setDisable(false);
                AnchorMCPrice.setDisable(false);
                AnchorRegInsur.setDisable(false);
                AnchorGiveaway.setDisable(false);
                AnchorPaymType.setDisable(false);
                break;
            default:
                throw new IllegalStateException(SalesTransactionStatic.MSG_UNEXPECTED_VALUE + fnValue);
        }
        pbEditable.set(fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
//        loadStatus(fnValue);
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
                    oSalesController.MCSales().getMaster().setCategory(psCategoryId);
                    oSalesController.MCSales().getMaster().setBranchCode(oApp.getBranchCode());
                    LoadMaster();
                    loadTableMCItem();
                    loadTableGiveaways();
                    loadTableRegAndInsur();
                    break;
                case "btnBrowse":
                    poJSON = oSalesController.MCSales().searchTransaction("",true,false);
                    if(poJSON.get("result").equals("error")) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                        break;
                    }
                    LoadMaster();
                    LoadClientInfo();
                    LoadCoClientInfo();
                    loadTableGiveaways();
                    loadTableMCItem();
                    loadTableRegAndInsur();
                    loadSummary();
                    pnEditMode = oSalesController.MCSales().getEditMode();
                    break;
                case "btnUpdate":
                    pnEditMode = EditMode.UPDATE;
                    break;
                case "btnSave":
                    poJSON = oSalesController.MCSales().saveTransaction();
                    if(poJSON.get("result").equals("error")) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                        break;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), "Information", null);
                    ClearAll();                                       // clears the fields and the observable lists
                    oSalesController.MCSales().initTransaction();
                    btnNew.fire();
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
                        ClearAll();                                       // clears the fields and the observable lists
                        oSalesController.MCSales().initTransaction();
                        pnEditMode = EditMode.UNKNOWN;// ASSUMED: resets the model's detail / giveaway lists
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
//    private void loadStatus(int fnEditMode) {
//        String lsStatus;
//        switch (fnEditMode) {
//            case EditMode.ADDNEW:
//                lsStatus = SalesTransactionStatic.STATUS_NEW;
//                break;
//            case EditMode.UPDATE:
//                lsStatus = SalesTransactionStatic.STATUS_UPDATING;
//                break;
//            case EditMode.READY:
//                lsStatus = SalesTransactionStatic.STATUS_READY;
//                break;
//            default:
//                lsStatus = SalesTransactionStatic.STATUS_UNKNOWN;
//                break;
//        }
//        lblStatus.setText(lsStatus);
//    }

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
//        setComboItems(cmbRegInsurType, SalesTransactionStatic.INSURANCE_TYPES);
//        setComboItems(cmbInsuranceType, SalesTransactionStatic.INSURANCE_TYPES);
//        setComboItems(cmbRegInsuranceOption, SalesTransactionStatic.INSURANCE_OPTIONS);
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
    // NOTE: tblUnits and tblGawayItem are bound directly to Model_Sales_Detail / Model_Sales_Giveaways
    // (lambda cell factories, lookups pre-loaded in the Task). The registration/insurance, invoice and
    // attachment tables still use ModelTableDetail ("index01"...) until their backend models exist.
    // =========================================================================
    /** Remove column: a trash button per row, enabled only while editing. */
    /**
     * Remove column: a trash button per row. fpBlank tells which rows are still empty; those rows get no
     * button (null = every row gets one).
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

    /** Registration and insurance list (Registration & Insurance tab). */
    private void initTableRegAndInsur() {
        tblRowRegAndInsurNo.setCellValueFactory(cd -> cell(() -> cd.getTableView().getItems().indexOf(cd.getValue()) + 1));
        tblRowRegAndInsurEngine.setCellValueFactory(cd -> cell(() -> regInsurEngine(cd.getValue())));
        tblRowRegAndInsurSource.setCellValueFactory(cd -> cell(() -> cd.getValue() instanceof Model_Sales_Registration ? "Registration" : "Insurance"));
        tblRowRegAndInsurDesc.setCellValueFactory(cd -> cell(() -> regInsurTypeId(cd.getValue())));      // TODO(model): insurance type DESCRIPTION getter
        tblRowRegAndInsurAmount.setCellValueFactory(cd -> new ReadOnlyStringWrapper(""));                // TODO(model): amount getter
        tblRowRegAndInsurStat.setCellValueFactory(cd -> new ReadOnlyStringWrapper(""));                  // TODO(model): status / charge-to getter
        initRemoveColumn(tblRowRegAndInsurAction, this::removeRegAndInsur, SalesTransactionsMCController::isBlankRegInsur);
        tblRegAndIsur.setItems(reginsur_data);
        tblRegAndIsur.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_REGINSUR));
        tblRegAndIsur.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            Model loRow = newIdx.intValue() >= 0 ? reginsur_data.get(newIdx.intValue()) : null;
            pnRegInsurRow = loRow == null ? -1 : regInsurEntryNo(loRow);   // model entry no., as the search methods expect
            if (pnRegInsurRow >= 1) {
                loadRegAndInsurRecord(pnRegInsurRow);
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

    private void loadUnitRecord(int fnRow) {
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            clearUnitItemTextFields();
            Model_Sales_Detail loUnit = (fnRow >= 1 && fnRow <= unit_data.size()) ? unit_data.get(fnRow - 1) : null;
            if (loUnit != null && !isBlankUnit(loUnit)) {
                // ... keep your existing setText(...) lines unchanged ...
                if (loUnit != null && !isBlankUnit(loUnit)) {
                    tfMCModel.setText(textOf(() -> loUnit.Inventory().Model().getDescription()));
                    tfMCColor.setText(textOf(() -> loUnit.Inventory().Color().getDescription()));
                    tfMCEngineNo.setText(textOf(() -> loUnit.InventorySerial().getSerial01()));   // check getter
                    tfMCFrameNo.setText(textOf(() -> loUnit.InventorySerial().getSerial02()));    // check getter
                    tfMCWarrantyNo.setText(textOf(() -> loUnit.getWarrantyNo()));
                    tfMCKeyCode.setText(textOf(() -> loUnit.getKeyCode()));
                    tfMCRemarks.setText(textOf(() -> loUnit.getNotes()));
                    tfMCUnitPrice.setText(money(dbl(() -> loUnit.getUnitPrice())));
                    tfMCInsurAmt.setText(money(dbl(() -> loUnit.getInsuranceAmount())));
                    tfPromoDisc.setText(money(dbl(() -> loUnit.getDiscount())));
                    tfPromoAddDisc.setText(money(dbl(() -> loUnit.getAddDiscount())));
                    tfFreight.setText(money(dbl(() -> loUnit.getFreight())));
                    tfPomoCode.setText(textOf(() -> loUnit.PromotionMaster().getPromoDescription()));
                    tfRegEngineNo.setText(tfMCEngineNo.getText());
                    tfRegFrameNo.setText(tfMCFrameNo.getText());
                    // TODO(model): tfMCRegisAmt, cmbVehicleType and the other combos / check boxes
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        } finally {
            pbSyncing = lbPrev;
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
        boolean lbPrev = pbSyncing;
        pbSyncing = true;
        try {
            clearGawayItemTextFields();
            Model_Sales_Giveaways loGaway = (fnRow >= 1 && fnRow <= gaway_data.size()) ? gaway_data.get(fnRow - 1) : null;
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

    /** Click on a unit row: loads it into the motorcycle fields; double-click moves focus to Key Code. */
    private void setTblUnits_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            int lnIndex = tblUnits.getSelectionModel().getSelectedIndex()+1;
            Model_Sales_Detail loSelected = tblUnits.getSelectionModel().getSelectedItem();
            // model entry no. (1-based)
            clearUnitItemTextFields();
            if (loSelected != null ) {
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

    /** Click on a giveaway row: loads it into the giveaway fields; double-click moves focus to Qty. */
    private void setTblGawayItem_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            int lnIndex = tblGawayItem.getSelectionModel().getSelectedIndex();
            Model_Sales_Giveaways loSelected = tblGawayItem.getSelectionModel().getSelectedItem();
            pnGawayRow = lnIndex >= 0 ? lnIndex + 1 : -1;   // model entry no. (1-based)
            clearGawayItemTextFields();
            if (loSelected != null && pnGawayRow >= 1) {
                loadGiveawayRecord(pnGawayRow);
                if (event.getClickCount() == 2) {
                    tfGawayQty.requestFocus();
                }
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

        Task<List<Model_Sales_Detail>> task = new Task<List<Model_Sales_Detail>>() {
            private double pdTotal = 0.0;

            @Override
            protected List<Model_Sales_Detail> call() throws Exception {
                MCSales loSales = oSalesController.MCSales();
                // a COPY for the table: never add to the model's own list, or every load duplicates the rows
                List<Model_Sales_Detail> laRows = new ArrayList<Model_Sales_Detail>();

                int lnCount = loSales.getSalesDetailList().size();
                if (lbEditing) {
                    try {
                        if (lnCount == 0 || !isBlankUnit(loSales.getSalesDetailList().get(lnCount - 1))) {
                            // let the model create the blank row so it gets its trans no and entry no
                            int lnNextEntry = 0;
                            for (Model_Sales_Detail loTmp : loSales.getSalesDetailList()) {
                                lnNextEntry = Math.max(lnNextEntry, loTmp.getEntryNo());
                            }
                            loSales.getDetail(lnNextEntry + 1);
                            lnCount = loSales.getSalesDetailList().size();
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.WARNING, null, ex);
                        lnCount = loSales.getSalesDetailList().size();
                    }
                }

                for (int lnCtr = 0; lnCtr < lnCount; lnCtr++) {
                    Model_Sales_Detail loRow = loSales.getSalesDetailList().get(lnCtr);
                    if (loRow == null) {
                        break;
                    }
                    if (!isBlankUnit(loRow)) {
                        pdTotal += unitNet(loRow);
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
                if (lnSeq != pnUnitLoadSeq) { loading.progressIndicator.setVisible(false); return; }   // stale / cancelled
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
                tfSummaryUnitPrice.setText(String.format("%,.2f", pdTotal));
                loadSummary();
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (lnSeq != pnUnitLoadSeq) return;
                Logger.getLogger(SalesTransactionsMCController.class.getName()).log(Level.SEVERE, null, getException());
                tblUnits.setPlaceholder(loading.placeholderLabel);
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();
    }

    /**
     * A COPY of the model's giveaway entries for the table. Reads the model's list directly
     * (getSalesGiveawaysList()) so nothing is created: getSalesGiveaways(n) adds a new, empty entry whenever
     * entry n does not exist, which is what used to pile up blank rows on every load.
     * Rows with a barcode are listed in order; blank rows are skipped except one trailing blank row (when asked).
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

    /** The giveaway row currently selected in the table (null when none / out of range). */
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
     * Loads tblRegAndIsur from the model: every registration followed by every insurance entry that has a serial or an
     * insurance type. Reads the model's lists directly (nothing is created, so no blank rows pile up).
     */
    private void loadTableRegAndInsur() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblRegAndIsur.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        final int lnSelectRow = pnRegInsurRow;
        final int lnSeq = ++pnRegInsurLoadSeq;

        Task<List<Model>> task = new Task<List<Model>>() {
            @Override
            protected List<Model> call() throws Exception {
                MCSales loSales = oSalesController.MCSales();
                List<Model> laRows = new ArrayList<Model>();   // a COPY for the table
                for (Model_Sales_Registration loRow : loSales.getSalesRegistrationsList()) {
                    if (!isBlankRegInsur(loRow)) {
                        laRows.add(loRow);
                    }
                }
                for (Model_Sales_Insurance loRow : loSales.getSalesInsurancesList()) {
                    if (!isBlankRegInsur(loRow)) {
                        laRows.add(loRow);
                    }
                }
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
                    tblRegAndIsur.setPlaceholder(loading.placeholderLabel);
                } else {
                    int lnIndex = 0;
                    for (int lnCtr = 0; lnCtr < reginsur_data.size(); lnCtr++) {   // keep the previously selected entry selected
                        if (regInsurEntryNo(reginsur_data.get(lnCtr)) == lnSelectRow) {
                            lnIndex = lnCtr;
                            break;
                        }
                    }
                    JFXUtil.selectAndFocusRow(tblRegAndIsur, lnIndex);
                    pnRegInsurRow = regInsurEntryNo(reginsur_data.get(tblRegAndIsur.getSelectionModel().getSelectedIndex()));
                    loadRegAndInsurRecord(pnRegInsurRow);
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
     * Entry no. (in the registration or insurance list) that an insurance-type search should fill: the selected table row's
     * serial, else the selected unit's serial. Reuses that serial's row if it has no type yet, otherwise creates one.
     * Returns -1 (after a warning) when there is no serial to attach the type to.
     */
    private int regInsurRowForSearch(boolean fbRegistration) {
        MCSales loSales = oSalesController.MCSales();
        String lsSerial = null;
        int lnSel = tblRegAndIsur.getSelectionModel().getSelectedIndex();
        if (lnSel >= 0 && lnSel < reginsur_data.size()) {
            lsSerial = regInsurSerialId(reginsur_data.get(lnSel));
        }
        if ((lsSerial == null || lsSerial.isEmpty()) && pnUnitRow >= 1 && pnUnitRow <= unit_data.size()) {
            lsSerial = unit_data.get(pnUnitRow - 1).getSerialID();
        }
        if (lsSerial == null || lsSerial.isEmpty()) {
            ShowMessageFX.Warning("Please select a unit with an engine / serial first.", "Warning", null);
            return -1;
        }
        List<? extends Model> laRows = fbRegistration ? loSales.getSalesRegistrationsList() : loSales.getSalesInsurancesList();
        for (Model loRow : laRows) {
            String lsType = regInsurTypeId(loRow);
            if (lsSerial.equals(regInsurSerialId(loRow)) && (lsType == null || lsType.isEmpty())) {
                return regInsurEntryNo(loRow);   // the promo already made a row for this serial
            }
        }
        int lnNew = laRows.size() + 1;
        if (fbRegistration) {
            loSales.getSalesRegistration(lnNew).setSerialID(lsSerial);
        } else {
            loSales.getSalesInsurance(lnNew).setSerialID(lsSerial);
        }
        return lnNew;
    }

    private static int regInsurEntryNo(Model fo) {
        return fo instanceof Model_Sales_Registration ? ((Model_Sales_Registration) fo).getEntryNo()
                : fo instanceof Model_Sales_Insurance ? ((Model_Sales_Insurance) fo).getEntryNo() : -1;
    }

    private static String regInsurSerialId(Model fo) {
        return fo instanceof Model_Sales_Registration ? ((Model_Sales_Registration) fo).getSerialID()
                : fo instanceof Model_Sales_Insurance ? ((Model_Sales_Insurance) fo).getSerialID() : null;
    }

    private static String regInsurTypeId(Model fo) {
        return fo instanceof Model_Sales_Registration ? ((Model_Sales_Registration) fo).getInsuranceTypeID()
                : fo instanceof Model_Sales_Insurance ? ((Model_Sales_Insurance) fo).getInsuranceTypeID() : null;
    }

    /** A row with neither a serial nor an insurance type is still empty. */
    private static boolean isBlankRegInsur(Model fo) {
        String lsSerial = regInsurSerialId(fo);
        String lsType = regInsurTypeId(fo);
        return fo == null || ((lsSerial == null || lsSerial.isEmpty()) && (lsType == null || lsType.isEmpty()));
    }

    /** Engine no. of the unit this row belongs to: found through the serial id in the units table. */
    private String regInsurEngine(Model fo) {
        String lsSerial = regInsurSerialId(fo);
        if (lsSerial == null || lsSerial.isEmpty()) {
            return "";
        }
        for (final Model_Sales_Detail loUnit : unit_data) {
            if (lsSerial.equals(loUnit.getSerialID())) {
                return textOf(() -> loUnit.InventorySerial().getSerial01());
            }
        }
        return "";
    }

    // =========================================================================
    // Cell helpers for tables bound directly to the model classes
    // =========================================================================
    private interface Getter { Object get() throws Exception; }
    private interface UnitGetter { Object get(Model_Sales_Detail fo) throws Exception; }
    private interface GawayGetter { Object get(Model_Sales_Giveaways fo) throws Exception; }

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
     * A unit row counts as filled when it has a stock id OR a serial id, because the serial search
     * may set only the serial. TODO(model): check the getSerialID() getter name.
     */
    private static boolean isBlankUnit(Model_Sales_Detail fo) {
        return fo == null
                || ((fo.getStockID() == null || fo.getStockID().isEmpty())
                && (fo.getSerialID() == null || fo.getSerialID().isEmpty()));
    }

    private static boolean isBlankGiveaway(Model_Sales_Giveaways fo) {
        return fo == null || fo.getStockID() == null || fo.getStockID().isEmpty();
    }

    private static double unitNet(Model_Sales_Detail fo) {
        return ((dbl(() -> fo.getUnitPrice()) - dbl(() -> fo.getDiscount()) - dbl(() -> fo.getAddDiscount()))
                + dbl(() -> fo.getFreight()) + dbl(() -> fo.getInsuranceAmount())) * dbl(() -> fo.getQuantity());
    }

    /**
     * Number as double. Returns 0 when the getter throws (e.g. ClassCastException: the model holds a blank
     * String where a Number is expected), or when the value is null / blank / not numeric.
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

    /** Number getter that tolerates a model returning a String/blank where a Number is expected. */
    private static String numText(Getter fg) {
        try {
            Object loVal = fg.get();
            return loVal == null || String.valueOf(loVal).isEmpty() ? "0" : String.valueOf(loVal);
        } catch (Exception ex) {   // e.g. ClassCastException: String cannot be cast to Number
            return "0";
        }
    }

    /** Raw model field as text ("" when missing); used where a typed getter is not available. */
    private static String gawayRaw(Model_Sales_Giveaways fo, String fsField) throws Exception {
        Object loVal = fo.getValue(fsField);
        return loVal == null ? "" : String.valueOf(loVal).trim();
    }

    /** Typed getter first; if it fails (e.g. String stored where a Number is expected) read the raw field; else "0". */
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
        Model loRow = reginsur_data.get(fnRow);
        if (!isBlankRegInsur(loRow)) {
            poJSON = loRow instanceof Model_Sales_Registration
                    ? oSalesController.MCSales().removeSalesRegistration(regInsurEntryNo(loRow))   // removes it and realigns the entry nos
                    : oSalesController.MCSales().removeSalesInsurance(regInsurEntryNo(loRow));
            if ("error".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                return;
            }
        }
        pnRegInsurRow = -1;
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
            if ("error".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                return;
            }
        }
        pnGawayRow = -1;
        loadTableGiveaways();
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
        tfSummaryCustomer.setText(tfClientName.getText());
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
        lblStatus.setText(StatusText(oSalesController.MCSales().getMaster().getTransactionStatus()));
        tfTransNo.setText(oSalesController.MCSales().getMaster().getTransactionNo());
        tfSourceNo.setText(oSalesController.MCSales().getMaster().getSourceNo());
        tfSourceDesc.setText(oSalesController.MCSales().getMaster().getSourceCode());
        setDate(dpTransDate, toLocalDate(oSalesController.MCSales().getMaster().getTransactionDate()));
//        tfReceipt.setText(oSalesController.MCSales().getMaster().getr());
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
            return fsCode;   // already text, e.g. "Male"
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
    private void LoadClientInfo() {
        pbSyncing = true;
        try {
            cmbClientType.getSelectionModel().select(0);

            tfClientName.setText(oSalesController.MCSales().getMaster().Client().getCompanyName());
            tfClientGender.setText(genderText(oSalesController.MCSales().getMaster().Client().getGender()));
            tfClientCitizenship.setText(oSalesController.MCSales().getMaster().Client().Citizenship().getDescription());
            setDate(dpClientBday, toLocalDate(oSalesController.MCSales().getMaster().Client().getBirthDate()));

            tfClientBirthplace.setText(oSalesController.MCSales().getMaster().Client().BirthTown().getDescription() + ", "
                    + oSalesController.MCSales().getMaster().Client().BirthTown().Province().getDescription());

//            tfClientOccupation.setText(oSalesController.MCSales().getMaster().Client().getO);
            tfClientTinNo.setText(oSalesController.MCSales().getMaster().Client().getTaxIdNumber());
//            tfClientFathersName.setText(oSalesController.MCSales().getMaster().Client().getFathersName());
//            tfClientMothersName.setText("");
            tfClientContactNo.setText(oSalesController.MCSales().getMaster().ClientMobile().getMobileNo());
//            tfClientEmail.setText(oSalesController.MCSales().getMaster().M);
            tfClientAddress.setText(oSalesController.MCSales().getMaster().ClientAddress().getAddress() + ","
                    + oSalesController.MCSales().getMaster().ClientAddress().Barangay().getBarangayName() + ","
                    + oSalesController.MCSales().getMaster().ClientAddress().Town().getDescription() + ","
                    + oSalesController.MCSales().getMaster().ClientAddress().Town().Province().getDescription() + ","
                    + oSalesController.MCSales().getMaster().ClientAddress().Town().getZipCode());

            tfClientLTOID.setText(oSalesController.MCSales().getMaster().Client().getLTOClientId() );
            tfClientLTOAddress.setText("");

        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        } finally {
            pbSyncing = false;
        }
    }
    private void LoadCoClientInfo(){
        try{
            tfCoClientName.setText(oSalesController.MCSales().getMaster().CoClient().getCompanyName());
            tfCoClientGender.setText(genderText(oSalesController.MCSales().getMaster().CoClient().getGender()));
            tfCoClientCitezenship.setText(oSalesController.MCSales().getMaster().CoClient().Citizenship().getDescription());
            setDate(dpCoClientBirthday, toLocalDate(oSalesController.MCSales().getMaster().CoClient().getBirthDate()));
            tfCoClientBirthplace.setText(oSalesController.MCSales().getMaster().CoClient().BirthTown().getDescription() + ", "
                    + oSalesController.MCSales().getMaster().CoClient().BirthTown().Province().getDescription());
//            tfCoClientOccupation.setText(oSalesController.MCSales().getMaster().CoClient().getOccupation());
            tfCoClientTinNo.setText(oSalesController.MCSales().getMaster().CoClient().getTaxIdNumber());
            tfCoClientFathersName.setText("");
            tfCoClientMothersName.setText("");
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

    /** java.sql.Date has no toInstant(), so convert it with its own toLocalDate(). */
    private static LocalDate toLocalDate(java.util.Date fdDate) {
        if (fdDate == null) {
            return null;
        }
        if (fdDate instanceof java.sql.Date) {
            return ((java.sql.Date) fdDate).toLocalDate();
        }
        return fdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();   // java.util.Date / Timestamp
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