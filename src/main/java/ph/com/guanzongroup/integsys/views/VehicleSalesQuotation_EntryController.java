/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelVehicleQuotation_AddOns;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleQuotation;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.VehicleQuotationStatus;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.model.ModelVehicleQuotation_Freebies;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehicleSalesQuotation_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0, pnAddOns = 0, pnFreebies = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static VehicleQuotation poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psValidityId = "";
    boolean lbresetpredicate = false;
    boolean pbEnteredAddOns = false, pbEnteredFreebies = false;
    boolean pbKeyPressed = false;

    private ObservableList<ModelVehicleQuotation_AddOns> addOns_data = FXCollections.observableArrayList();
    private ObservableList<ModelVehicleQuotation_Freebies> freebies_data = FXCollections.observableArrayList();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableAddOns, loadTableFreebies;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList();
    ArrayList<ArrayList<ArrayList<String>>> array = new ArrayList<ArrayList<ArrayList<String>>>();

    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    ObservableList<String> PurchaseType = ModelSalesInquiry_Detail.PurchaseType;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apAddOns, apPricing, apAdditionalInfo, apFreebies, apRemarks;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnVoid, btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfBranch, tfClient, tfAddress, tfContactNo, tfEmail, tfInquiryNo, tfSalesPerson, tfFinancingRate, tfBrand, tfModel, tfVariant, tfYear, tfColor, tfAddOns, tfAccessories, tfAmount, tfQuantity, tfSRP, tfDownpayment, tfReservationAmt, tfTPL, tfComprehensive, tfLTO, tfCHMODocStmp, tfTotalAmount, tfBank, tfTerms, tfAmountToFinance, tfNetMonthlyInstallment, tfPromptPaymentDiscount, tfGrossMonthlyInstallment, tfPromissoryNoteAmount, tfRate, tfDescription, tfQuantityFreebies;
    @FXML
    private DatePicker dpTransactionDate, dpInquiryDate, dpExpectedDate, dpValidThru;
    @FXML
    private ComboBox cmbModeOfPayment;
    @FXML
    private TableView tblViewAddOns, tblViewFreebies;
    @FXML
    private CheckBox cbSelectAll;
    @FXML
    private TableColumn tblNoAddOns, tblDescriptionAddOns, tblAmountAddOns, tblQtyAddOns, tblTotalAddOns, tblNoFreebies, tblDescriptionFreebies, tblQtyFreebies;
    @FXML
    private TextArea taRemarks;

    LocalDate ValidFrom;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehicleQuotation();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initAddOnsGrid();
            initFreebiesGrid();
            initComboboxes();
            initCheckboxes();
            initTextFields();
            initDatepickers();
            initTableOnClick();
            clearTextFields();

            loadTableAddOns.reload();
            pnEditMode = poController.getEditMode();
            poController.setWithUI(true);
            initButton(pnEditMode);
            Platform.runLater(() -> {
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
                loadRecordSearch();
                if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                } else {
                    poController.Detail(pnDetail).setCompanyId(psCompanyId);
                }
                btnNew.fire();
            });
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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
        psCategoryId = fsValue;
    }

    public void setValidityID(String fsValue) {
        psValidityId = fsValue;
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
                        if (psValidityId != null && !"".equals(psValidityId)) {
                            CommonUtils.closeStage(btnClose);
                        } else {
                            unloadForm appUnload = new unloadForm();
                            if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                                appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                            } else {
                                return;
                            }
                        }
                        return;
                    case "btnNew":
                        //Clear data
                        poController.Detail().clear();
                        clearTextFields();

                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poController.initFields();
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
//                        ValidFrom = CustomCommonUtil.parseDateStringToLocalDate(CustomCommonUtil.formatDateToShortString(poController.Master().getFromDate()));
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            poController.InitTransaction();
                            clearTextFields();
                            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                            } else {
                                poController.Detail(pnDetail).setCompanyId(psCompanyId);
                            }
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
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.AddDetail();
                                loadTableAddOns.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnBrowse":
//                        poController.Master().setRecordStatus("01234");
                        poJSON = poController.SearchTransaction(psValidityId, true);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                            tfValidityID.requestFocus();
                            return;
                        }
                        pnEditMode = poController.Master().getEditMode();
//                        poController.populateVehicleList();
                        break;
                    case "btnApprove":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to approve transaction?") == true) {
                            poJSON = poController.ApproveTransaction();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getTransactionStatus());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        String lsStat = "void";
                        if (VehicleQuotationStatus.APPROVED.equals(poController.Master().getTransactionStatus())) {
                            lsStat = "cancel";
                        }
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsStat + " transaction?") == true) {
                            if (VehicleQuotationStatus.OPEN.equals(poController.Master().getTransactionStatus())) {
                                poJSON = poController.VoidTransaction();
                            } else {
                                poJSON = poController.CancelTransaction();
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apAddOns, apAdditionalInfo, apFreebies);
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

//                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisapprove", "btnVoid")) {
//                } else {
//                    loadRecordAddOns();
//                    loadTableAddOns.reload();
//                }
                loadTableAddOns.reload();
                loadTableFreebies.reload();
                loadRecordMaster();
                loadRecordAdditionalInfo();
                loadRecordPricing();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnAddOns;
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    if (tfQuantity.isFocused()) {
                        pbEnteredAddOns = true;
                    }
                    if (tfQuantityFreebies.isFocused()) {
                        pbEnteredFreebies = true;
                    }
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        //apMaster
                        case "tfClient":
                            poJSON = poController.SearchClient(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfFinancingRate);
                            }
                            loadRecordMaster();
                            break;
                        case "tfFinancingRate":
                            poJSON = poController.SearchVehicleFinancing(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfBrand);
                            }
                            reloadRecords();
                            break;
                        case "tfBrand":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).Brand().getBrandId(), null, "")
                                        && !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).Model().getModelId(), null, "")) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the brand?\nPlease note that doing so will delete all the other existing details.\n\nDo you wish to proceed?") == true) {
                                        poController.Detail(pnDetail).setVariantId("");
                                        poController.Detail(pnDetail).setModelId("");
                                        poController.Detail(pnDetail).setBrandId("");
                                        resetAmounts();
                                        reloadRecords();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            poJSON = poController.SearchBrand(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfModel);
                            }
                            loadRecordMaster();
                            break;
                        case "tfModel":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).Brand().getBrandId(), null, "")
                                        && !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).Model().getModelId(), null, "")) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the model?\nPlease note that doing so will delete all the other existing details.\n\nDo you wish to proceed?") == true) {
                                        poController.Detail(pnDetail).setVariantId("");
                                        poController.Detail(pnDetail).setModelId("");
                                        resetAmounts();
                                        reloadRecords();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            poJSON = poController.SearchModel(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfVariant);
                            }
                            loadRecordMaster();
                            break;
                        case "tfVariant":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).ModelVariant().getVariantId(), null, "")) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the variant?\nPlease note that doing so will delete all the other existing details.\n\nDo you wish to proceed?") == true) {
                                        poController.Detail(pnDetail).setVariantId("");
                                        resetAmounts();
                                        reloadRecords();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            poJSON = poController.SearchModelVariant(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            reloadRecords();
                            break;

                        //apAddOns
                        case "tfAddOns":
                            poJSON = poController.SearchVehicleAddOn(lsValue, false, pnAddOns, true);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfAmount);
                            }
                            loadTableAddOns.reload();
                            break;
                        case "tfAccessories":
                            poJSON = poController.SearchVehicleAddOn(lsValue, false, pnAddOns, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfAmount);
                            }
                            loadTableAddOns.reload();
                            break;

                        //apAdditionalInfo
                        case "tfBank":
                            poJSON = poController.Searchbank(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.runWithDelay(.6, () -> {
                                    tfTerms.requestFocus();
                                });
                            }
                            loadRecordAdditionalInfo();
                            break;

                        //apFreebies
                        case "tfDescription":
                            poJSON = poController.SearchFreebies(lsValue, false, pnFreebies);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfQuantityFreebies);
                            }
                            loadTableFreebies.reload();
                            break;
                    }
                    break;
                case UP:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfAddOns", "tfAccessories", "tfAmount", "tfQuantity"}, (Runnable) () -> moveNextAddOns(true, true)},
                        {new String[]{"tfDescription", "tfQuantityFreebies"}, (Runnable) () -> moveNextFreebies(true, true)},});

                    break;
                case DOWN:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfAddOns", "tfAccessories", "tfAmount", "tfQuantity"}, (Runnable) () -> moveNextAddOns(false, true)},
                        {new String[]{"tfDescription", "tfQuantityFreebies"}, (Runnable) () -> moveNextFreebies(false, true)},});
                    break;
                default:
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewAddOns":
                    if (!addOns_data.isEmpty()) {
                        pnAddOns = isMovedDown ? Integer.parseInt(addOns_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                                : Integer.parseInt(addOns_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                        loadRecordAddOns();
                    }
                    break;
                case "tblViewFreebies":
                    if (!freebies_data.isEmpty()) {
                        pnFreebies = isMovedDown ? Integer.parseInt(freebies_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex06())
                                : Integer.parseInt(freebies_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex06());
                        loadRecordFreebies();
                    }
                    break;
            }
        }
    };

    private void resetAmounts() {
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
        } else {
            poController.Detail(pnDetail).setSRPAmount(0.0000);
            poController.Detail(pnDetail).setDownPayment(0.0000);
            poController.Detail(pnDetail).setReservationAmount(0.0000);
        }
    }

    private void reloadRecords() {
        loadRecordMaster();
        loadTableAddOns.reload();
        loadTableFreebies.reload();
        loadRecordPricing();
        loadRecordAdditionalInfo();
    }
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfClient":
                            if (lsValue.isEmpty()) {
                                poController.Master().setClientId("");
                                poController.Master().setAddressId("");
                                poController.Master().setContactId("");

                                poController.Detail(pnDetail).setSourceNo("");
                                String lsInquiryDate = JFXUtil.formatDateToString(oApp.getServerDate());
                                JFXUtil.setDateValue(dpInquiryDate, JFXUtil.isObjectEqualTo(lsInquiryDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsInquiryDate, "yyyy-MM-dd"));
                                JFXUtil.setCmbValue(cmbModeOfPayment, -1);
                                poController.Detail(pnDetail).SalesInquiry().setClientId("");
                            }
                            break;
                        case "tfFinancingRate":
                            return;
                        case "tfBrand":
                            if (lsValue.isEmpty()) {
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    if (!JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).Brand().getBrandId(), null, "")
                                    && !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).Model().getModelId(), null, "")
                                    && !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).ModelVariant().getVariantId(), null, "")) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the brand?\nPlease note that doing so will delete the other inputs exists.\n\nDo you wish to proceed?") == true) {
                                                poController.Detail(pnDetail).setVariantId("");
                                                poController.Detail(pnDetail).setModelId("");
                                                poController.Detail(pnDetail).setBrandId("");
                                                resetAmounts();
                                                reloadRecords();
                                            } else {
                                                loadRecordMaster();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                                poController.Detail(pnDetail).setBrandId("");
                            }
                            break;
                        case "tfModel":
                            if (lsValue.isEmpty()) {
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    if (!JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).ModelVariant().getVariantId(), null, "")
                                    && !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).Model().getModelId(), null, "")) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the model?\nPlease note that doing so will delete the other inputs exists.\n\nDo you wish to proceed?") == true) {
                                                poController.Detail(pnDetail).setVariantId("");
                                                poController.Detail(pnDetail).setModelId("");
                                                resetAmounts();
                                                reloadRecords();
                                            } else {
                                                loadRecordMaster();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                                poController.Detail(pnDetail).setModelId("");
                            }
                            break;
                        case "tfVariant":
                            if (lsValue.isEmpty()) {
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    if (!JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).ModelVariant().getVariantId(), null, "")) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the variant?\nPlease note that doing so will delete the other inputs exists.\n\nDo you wish to proceed?") == true) {
                                                poController.Detail(pnDetail).setVariantId("");
                                                resetAmounts();
                                                reloadRecords();
                                            } else {
                                                reloadRecords();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                                poController.Detail(pnDetail).setVariantId("");
                            }
                            reloadRecords();
                            return;
                    }
                    loadRecordMaster();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    ChangeListener<Boolean> txtAddOns_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfAddOns":
                        if (lsValue.isEmpty()) {
                            poController.AddOnList(pnAddOns).setAddOnId("");
                        }
                        break;
                    case "tfAccessories":
                        if (lsValue.isEmpty()) {
                            poController.AddOnList(pnAddOns).setStockId("");
                        }
                        break;
                    case "tfAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.AddOnList(pnAddOns).setAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfQuantity":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.AddOnList(pnAddOns).setQuantity(Integer.parseInt(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        JFXUtil.runWithDelay(0.40, () -> {
                            if (pbEnteredAddOns) {
                                moveNextAddOns(false, true);
                                pbEnteredAddOns = false;
                            }
                        });
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableAddOns.reload();
                });
            });

    ChangeListener<Boolean> txtPricing_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDownpayment":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setDownPayment(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfReservationAmt":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setReservationAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfTPL":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setTPLAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfComprehensive":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setComprehensiveAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfLTO":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setLTOAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfCHMODocStmp":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setCHMOAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordPricing();
            });

    ChangeListener<Boolean> txtAdditionalInfo_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfBank":
                            if (lsValue.isEmpty()) {
                                poController.Detail(pnDetail).FinancingRate().setBankId("");
                            }
                            break;
                        case "tfTerms":
                            lsValue = JFXUtil.removeComma(lsValue);
                            poJSON = poController.Detail(pnDetail).setDuration(Integer.parseInt(lsValue));
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                        case "tfPromptPaymentDiscount":
                            lsValue = JFXUtil.removeComma(lsValue);
                            poJSON = poController.Detail(pnDetail).setPromptPaymentDiscount(Double.parseDouble(lsValue));
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                        case "tfRate":
                            lsValue = JFXUtil.removeComma(lsValue);
                            poJSON = poController.Detail(pnDetail).setRateValue(Double.parseDouble(lsValue));
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                    }
                    loadRecordAdditionalInfo();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    ChangeListener<Boolean> txtFreebies_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDescription":
                        if (lsValue.isEmpty()) {
                            poController.FreebiesList(pnFreebies).setAddOnId("");
                        }
                        break;
                    case "tfQuantityFreebies":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.FreebiesList(pnFreebies).setQuantity(Integer.parseInt(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        JFXUtil.runWithDelay(0.40, () -> {
                            if (pbEnteredFreebies) {
                                moveNextFreebies(false, true);
                                pbEnteredFreebies = false;
                            }
                        });
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableFreebies.reload();
                });
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.Detail(pnDetail).setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbModeOfPayment":
                        break;
                }
            });

    private void loadRecordMaster() {
        try {
            Platform.runLater(() -> {
                lblStatus.setText(poController.getStatus(poController.Master().getTransactionStatus()).toUpperCase());

                String lsStat = "Void";
                if (VehicleQuotationStatus.APPROVED.equals(poController.Master().getTransactionStatus())) {
                    lsStat = "Cancel";
                }
                btnVoid.setText(lsStat);
            });
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            tfTransactionNo.setText(poController.Master().getTransactionNo());
            String lsTransactionDate = JFXUtil.formatDateToString(poController.Master().getTransactionDate());
            JFXUtil.setDateValue(dpTransactionDate, JFXUtil.isObjectEqualTo(lsTransactionDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            String lsExpectedDate = JFXUtil.formatDateToString(poController.Detail(pnAddOns).getExpectedDate());
            JFXUtil.setDateValue(dpExpectedDate, JFXUtil.isObjectEqualTo(lsExpectedDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsExpectedDate, "yyyy-MM-dd"));
            String lsValidThru = JFXUtil.formatDateToString(poController.Detail(pnAddOns).getValidThruDate());
            JFXUtil.setDateValue(dpValidThru, JFXUtil.isObjectEqualTo(lsValidThru, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidThru, "yyyy-MM-dd"));

            tfBranch.setText(poController.Detail(pnDetail).Branch().getBranchName());
            tfClient.setText(poController.Master().Client().getCompanyName());
            tfAddress.setText(poController.Master().ClientAddress().getAddress());
            tfContactNo.setText(poController.Master().ClientMobile().getMobileNo());
            tfEmail.setText(poController.Master().ClientEmail().getMailAddress());
            tfInquiryNo.setText(poController.Detail(pnDetail).getSourceNo());
            String lsInquiryDate = JFXUtil.formatDateToString(poController.Detail(pnDetail).SalesInquiry().getTransactionDate());
            JFXUtil.setDateValue(dpInquiryDate, JFXUtil.isObjectEqualTo(lsInquiryDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsInquiryDate, "yyyy-MM-dd"));
            JFXUtil.setCmbValue(cmbModeOfPayment, !poController.Detail(pnDetail).SalesInquiry().getPurchaseType().equals("") ? Integer.valueOf(poController.Detail(pnDetail).SalesInquiry().getPurchaseType()) : -1);
            tfSalesPerson.setText(poController.Detail(pnDetail).SalesInquiry().SalesPerson().getFullName());

            tfBrand.setText(poController.Detail(pnDetail).Brand().getDescription());
            tfModel.setText(poController.Detail(pnDetail).Model().getDescription());
            tfVariant.setText(poController.Detail(pnDetail).ModelVariant().getDescription());
            tfYear.setText(String.valueOf(poController.Detail(pnDetail).ModelVariant().getYearModel()));
            tfColor.setText(poController.Detail(pnDetail).ModelVariant().Color().getDescription());
            taRemarks.setText(poController.Detail(pnDetail).getRemarks());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordAddOns() {
        try {
            if (pnAddOns < 0 || pnAddOns > poController.getAddOnListCount() - 1) {
                return;
            }
            boolean lbShow1 = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
            JFXUtil.setDisabled(!lbShow1, tfAddOns, tfAccessories);
            JFXUtil.clearNodes(tfAccessories, tfAddOns);
            if (JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).getAddOnId(), null, "")
                    && JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).getStockId(), null, "")) {
                JFXUtil.setDisabled(false, tfAddOns, tfAccessories);
            } else if (JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).getAddOnId(), null, "")) {
                JFXUtil.setDisabled(true, tfAddOns);
                JFXUtil.setDisabled(false, tfAccessories);
                tfAccessories.setText(poController.AddOnList(pnAddOns).Inventory().getDescription());
            } else {
                if (lbShow1) {
                    JFXUtil.setDisabled(false, tfAddOns);
                    JFXUtil.setDisabled(true, tfAccessories);
                }
                tfAddOns.setText(poController.AddOnList(pnAddOns).AddOnMaster().AddOnType().getAddTypeName());
            }

            //dfine the addonid and stockid if available
            //if none enable the searching for add ons and accessories
            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(pnAddOns).getAmount().doubleValue(), true));
            tfQuantity.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(poController.AddOnList(pnAddOns).getQuantity()));
            JFXUtil.updateCaretPositions(apAddOns);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordPricing() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            poController.computeFields();
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), true));
            tfDownpayment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDownPayment().doubleValue(), true));
            tfReservationAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getReservationAmount().doubleValue(), true));
            tfTPL.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getTPLAmount().doubleValue(), true));
            tfComprehensive.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getComprehensiveAmount().doubleValue(), true));
            tfLTO.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getLTOAmount().doubleValue(), true));
            tfCHMODocStmp.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getCHMOAmount().doubleValue(), true));
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getTotalAmount().doubleValue(), true));
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordAdditionalInfo() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            poController.computeFields();
            tfBank.setText(poController.Detail(pnDetail).FinancingRate().Bank().getBankName());
            tfTerms.setText(String.valueOf(poController.Detail(pnDetail).getDuration()));
            tfAmountToFinance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getFinancingAmount().doubleValue(), true));
            tfNetMonthlyInstallment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getMonthlyAmount().doubleValue(), true));
            tfPromptPaymentDiscount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getPromptPaymentDiscount().doubleValue(), true));
            tfGrossMonthlyInstallment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getGrossMonthlyInstallment().doubleValue(), true));
            tfPromissoryNoteAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getPromissoryNoteAmt().doubleValue(), true));
            tfRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getRateValue().doubleValue(), false));
            JFXUtil.updateCaretPositions(apAdditionalInfo);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordFreebies() {
        try {
            if (pnFreebies < 0 || pnFreebies > poController.getFreebiesListCount() - 1) {
                return;
            }
            tfDescription.setText(poController.FreebiesList(pnFreebies).Inventory().getDescription());
            tfQuantityFreebies.setText(String.valueOf(poController.FreebiesList(pnFreebies).getQuantity()));
        } catch (SQLException | GuanzonException ex) {
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
                case "cbSelectAll": // this is the id
                    //set to 1 all of column 2 row data value to enable checked
                    for (int lnCtr = 0; lnCtr < checkedItem.size(); lnCtr++) {
                        if (checkedBox.isSelected()) {
                            poController.AddOnList(lnCtr).setRecordStatus(checkedBox.isSelected());
                            checkedItem.set(lnCtr, "1");
                        } else {
                            poController.AddOnList(lnCtr).setAmount(0.00);
                            poController.AddOnList(lnCtr).setQuantity(0);
                            poController.AddOnList(lnCtr).setTotalAmount(0.00);
                            poController.AddOnList(lnCtr).setRecordStatus(checkedBox.isSelected());
                            checkedItem.set(lnCtr, "0");
                        }
                    }
                    loadTableAddOns.reload();
                    break;
                case "cbActive": // this is the id
                    if (isContainsChecked()) {
                        processAction("cbActive", checkedBox.isSelected() ? "1" : "0");
                    } else {
                        poJSON = poController.Detail(pnAddOns).setRecordStatus(checkedBox.isSelected());
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadTableAddOns.reload();
                    }
                    break;
            }
        }
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
                String lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                LocalDate ldTransactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        break;
                    case "dpInquiryDate":
                        break;
                    case "dpExpectedDate":
                        //cannot be before transactiondate
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Expected date cannot be before the Transaction Date.");
                            pbSuccess = false;
                        } else {
                            poController.Detail(pnDetail).setExpectedDate(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
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
                        break;
                    case "dpValidThru":
                        //cannot be before expected date
                        String lsExpDate = sdfFormat.format(poController.Detail(pnDetail).getExpectedDate());
                        LocalDate ldExpDate = LocalDate.parse(lsExpDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                        if (ldSelectedDate.isBefore(ldExpDate)) {
                            JFXUtil.setJSONError(poJSON, "Valid Thru date cannot be before the Expected Date.");
                            pbSuccess = false;
                        } else {
                            poController.Detail(pnDetail).setValidThruDate(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
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
                        break;
                    default:
                        break;
                }
            });

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelVehicleQuotation_AddOns.class, tblViewAddOns, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 0:
                            poController.AddOnList(rowIndex).setRecordStatus(lbisTrue);
                            if (!lbisTrue) {
                                poController.AddOnList(rowIndex).setAmount(0.00);
                                poController.AddOnList(rowIndex).setQuantity(0);
                                poController.AddOnList(rowIndex).setTotalAmount(0.00);
                            }
                            checkedItem.set(rowIndex, lbisTrue ? "1" : "0");
                            //set external temporary data of index to save as reference
                            // if detected unchecked then must update
                            if (!checkedItem.isEmpty() && checkedItem.stream().allMatch(item -> "1".equals(item))) {
                                cbSelectAll.setSelected(true);
                            } else {
                                cbSelectAll.setSelected(false);
                            }
                            pnAddOns = rowIndex;
                            Platform.runLater(() -> {
                                loadTableAddOns.reload();
                                JFXUtil.runWithDelay(0.30, () -> {
                                    if (lbisTrue) {
                                        JFXUtil.selectAndFocusRow(tblViewAddOns, rowIndex);
                                    }
                                });
                            });
                            break;
                    }
                }, 0);//starts 0,1,2 
    }

    private void initComboboxes() {
        // ComboBox setup
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(PurchaseType, cmbModeOfPayment));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbModeOfPayment);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbModeOfPayment);
    }

    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpInquiryDate, dpExpectedDate, dpValidThru);
        JFXUtil.setActionListener(datepicker_Action, dpTransactionDate, dpInquiryDate, dpExpectedDate, dpValidThru);
    }

    public void moveNextAddOns(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apAddOns.requestFocus();
                pnAddOns = isUp ? Integer.parseInt(addOns_data.get(JFXUtil.moveToPreviousRow(tblViewAddOns)).getIndex07())
                        : Integer.parseInt(addOns_data.get(JFXUtil.moveToNextRow(tblViewAddOns)).getIndex07());
            }
            loadRecordAddOns();
            if (pnAddOns < 0 || pnAddOns > poController.getAddOnListCount() - 1) {
                return;
            }
            if (JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).getAddOnId(), null, "")) {
                JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                    {poController.AddOnList(pnAddOns).Inventory().getDescription(), tfAccessories},
                    {poController.AddOnList(pnAddOns).AddOnMaster().getAmount(), tfAmount},
                    {poController.AddOnList(pnAddOns).getQuantity(), tfQuantity},}, tfQuantity); // default
            } else {
                JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                    {poController.AddOnList(pnAddOns).AddOnMaster().getAmount(), tfAmount},
                    {poController.AddOnList(pnAddOns).getQuantity(), tfQuantity},}, tfQuantity); // default
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void moveNextFreebies(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apFreebies.requestFocus();
            pnFreebies = isUp ? Integer.parseInt(freebies_data.get(JFXUtil.moveToPreviousRow(tblViewFreebies)).getIndex07())
                    : Integer.parseInt(freebies_data.get(JFXUtil.moveToNextRow(tblViewFreebies)).getIndex07());
        }
        loadRecordAddOns();
        if (pnFreebies < 0 || pnFreebies > poController.getFreebiesListCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.FreebiesList(pnFreebies).getAddOnId(), tfDescription},}, tfQuantityFreebies); // default
    }

    public boolean isContainsChecked() {
        return checkedItem != null && checkedItem.contains("1");
    }

    private void processAction(String identifier, String lsValue) {
        boolean lbcb = false;
        String lsMessage = "";
        lsMessage = "remove";
        switch (identifier) {
            case "cbActive":
                lbcb = lsValue.equals("1");
                lsMessage = "activate/deactivate";
                break;
            case "tfAmount":
                lsMessage = "change amount of the";
                break;
        }

        if (checkedItem.stream().anyMatch("1"::equals)) {
        } else {
            ShowMessageFX.Warning(null, pxeModuleName, "No items were selected to " + lsMessage + ".");
            return;
        }
        if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + lsMessage + " selected item/s?")) {
            return;
        }
        String firstValue = null;
        boolean isConsistent = true;
        checkedItems.clear();
        List<Integer> listCount = new ArrayList<>();
        for (Object item : tblViewAddOns.getItems()) {
            ModelVehicleQuotation_AddOns item1 = (ModelVehicleQuotation_AddOns) item;
            String lschecked = item1.getIndex01();
            int lnCtr = Integer.valueOf(item1.getIndex07());
            if (lschecked.equals("1")) {
                listCount.add(Integer.parseInt(item1.getIndex07()));
                String currentValue = item1.getIndex09();
                // checkedItems.add();
                switch (identifier) {
                    case "cbActive":
                        if (firstValue == null) {
                            // First value found
                            firstValue = currentValue;
                        } else {
                            // Compare with the first value
                            if (!Objects.equals(firstValue, currentValue)) {
                                isConsistent = false;
                                ShowMessageFX.Warning(null, pxeModuleName, "Unable to simultaneously update different statuses");
                                return;
                            }
                        }
                        break;
                    case "tfAmount":
                        if (firstValue == null) {
                            // First value found
                            firstValue = currentValue;
                        } else {
                            // Compare with the first value
                            if (!Objects.equals(firstValue, currentValue)) {
                                isConsistent = false;
                                ShowMessageFX.Warning(null, pxeModuleName, "Unable to simultaneously update with an inactive status included");
                                return;
                            }
                        }
                        break;
                }
            }
        }
        if (checkedItem.isEmpty()) {
            return;
        }

        switch (identifier) {
            case "cbActive":
                if (isConsistent) {
                    for (int i = 0; i < listCount.size(); i++) {
                        int number = listCount.get(i);
                        poController.Detail(number).setRecordStatus(lbcb);
                    }
                }
                break;
            case "tfAmount":
                if (isConsistent) {
                    for (int i = 0; i < listCount.size(); i++) {
                        int number = listCount.get(i);
                        if (poController.Detail(number).getEditMode() == EditMode.UPDATE
                                && JFXUtil.isObjectEqualTo(poController.Detail(number).getRecordStatus(), RecordStatus.INACTIVE)) {
                            continue;
                        }
                        lsValue = JFXUtil.removeComma(lsValue);
//                        poController.Detail(number).setAmount(Double.parseDouble(lsValue));
                    }
                }
                break;
        }

        resetCheckboxSelection();
        pnEditMode = poController.getEditMode();
        loadTableAddOns.reload();
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, apMaster);
        JFXUtil.setFocusListener(txtAddOns_Focus, apAddOns);
        JFXUtil.setFocusListener(txtPricing_Focus, apPricing);
        JFXUtil.setFocusListener(txtAdditionalInfo_Focus, apAdditionalInfo);
        JFXUtil.setFocusListener(txtFreebies_Focus, apFreebies);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apAddOns, apPricing, apAdditionalInfo, apFreebies);
        JFXUtil.inputDecimalOnly(tfRate);
        JFXUtil.setCommaFormatter(tfAmount, tfReservationAmt, tfTotalAmount, tfAmountToFinance, tfPromissoryNoteAmount);
        CustomCommonUtil.inputIntegersOnly(tfQuantity, tfQuantityFreebies);

        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewAddOns, tblViewFreebies);

        JFXUtil.adjustColumnForScrollbar(tblViewAddOns, tblViewFreebies);
    }

    private String getData(String lsValue) {
        return JFXUtil.isObjectEqualTo(lsValue, null, "") ? "" : lsValue;
    }

    public void initLoadTable() {
        loadTableAddOns = new JFXUtil.ReloadableTableTask(
                tblViewAddOns,
                addOns_data,
                () -> {
                    pbEnteredAddOns = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEnteredAddOns = false;
                        addOns_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadAddOnList();
                            }
                            for (lnCtr = 0; lnCtr < poController.getAddOnListCount(); lnCtr++) {
                                if (pnEditMode == EditMode.READY
                                        && JFXUtil.isObjectEqualTo(poController.AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName(), null, "")
                                        && JFXUtil.isObjectEqualTo(poController.AddOnList(lnCtr).Inventory().getStockId(), null, "")) {
                                    continue;
                                }
                                checkedItems(lnCtr);
                                if (poController.AddOnList(lnCtr).getRecordStatus()) { // if true = Active // only this should activate during editMode of the row
                                    checkedItem.set(lnCtr, "1");
                                } else {
                                    checkedItem.set(lnCtr, "0");
                                }

                                String lsDescription = "";
                                if (JFXUtil.isObjectEqualTo(poController.AddOnList(lnCtr).getAddOnId(), null, "")) {
                                    lsDescription = poController.AddOnList(lnCtr).Inventory().getDescription();
                                } else {
                                    lsDescription = poController.AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName();
                                }

                                addOns_data.add(
                                        new ModelVehicleQuotation_AddOns(checkedItem.get(lnCtr),
                                                String.valueOf(lnCtr + 1),
                                                getData(lsDescription),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getQuantity(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getTotalAmount(), false)),
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            if (pnAddOns < 0 || pnAddOns
                                    >= addOns_data.size()) {
                                if (!addOns_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewAddOns.getSelectionModel().select(0);
                                    tblViewAddOns.getFocusModel().focus(0);
                                    pnAddOns = tblViewAddOns.getSelectionModel().getSelectedIndex();
                                    loadRecordAddOns();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewAddOns.getSelectionModel().select(pnAddOns);
                                tblViewAddOns.getFocusModel().focus(pnAddOns);
                                loadRecordAddOns();
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableFreebies = new JFXUtil.ReloadableTableTask(
                tblViewFreebies,
                freebies_data,
                () -> {
                    pbEnteredAddOns = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEnteredFreebies = false;
                        freebies_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadFreebiesList();
                            }
                            for (lnCtr = 0; lnCtr < poController.getFreebiesListCount(); lnCtr++) {
                                if (pnEditMode == EditMode.READY
                                        && JFXUtil.isObjectEqualTo(poController.FreebiesList(lnCtr).Inventory().getStockId(), null, "")) {
                                    continue;
                                }
                                freebies_data.add(
                                        new ModelVehicleQuotation_Freebies(String.valueOf(lnCtr + 1),
                                                getData(poController.FreebiesList(lnCtr).Inventory().getDescription()),
                                                String.valueOf(poController.FreebiesList(lnCtr).getQuantity()),
                                                "",
                                                "",
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            if (pnFreebies < 0 || pnFreebies
                                    >= freebies_data.size()) {
                                if (!freebies_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewFreebies.getSelectionModel().select(0);
                                    tblViewFreebies.getFocusModel().focus(0);
                                    pnFreebies = tblViewFreebies.getSelectionModel().getSelectedIndex();
                                    loadRecordFreebies();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewFreebies.getSelectionModel().select(pnFreebies);
                                tblViewFreebies.getFocusModel().focus(pnFreebies);
                                loadRecordFreebies();
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    private void initAddOnsGrid() {
        JFXUtil.setColumnCenter(tblNoAddOns);
        JFXUtil.setColumnLeft(tblDescriptionAddOns);
        JFXUtil.setColumnRight(tblAmountAddOns, tblQtyAddOns, tblTotalAddOns);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewAddOns);
        tblViewAddOns.setItems(addOns_data);
    }

    private void initFreebiesGrid() {
        JFXUtil.setColumnCenter(tblNoFreebies);
        JFXUtil.setColumnLeft(tblDescriptionFreebies);
        JFXUtil.setColumnRight(tblQtyFreebies);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewFreebies);
        tblViewFreebies.setItems(freebies_data);
    }

    public void clearTextFields() {
        resetCheckboxSelection();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apAddOns, apPricing, apAdditionalInfo, apFreebies);
    }

    private void resetCheckboxSelection() {
        cbSelectAll.setSelected(false);
        if (!checkedItem.isEmpty()) {
            checkedItem.clear();
        }
    }

    private void checkedItems(int lnCtr) {
        try {
            if (checkedItem.get(lnCtr) == null) {
                checkedItem.add("0");
            }
        } catch (Exception e) {
            checkedItem.add("0");
        }
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTableOnClick() {
        tblViewAddOns.setOnMouseClicked(event -> {
            if (addOns_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleQuotation_AddOns selected = (ModelVehicleQuotation_AddOns) tblViewAddOns.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(addOns_data.get(tblViewAddOns.getSelectionModel().getSelectedIndex()).getIndex07());
                        pnAddOns = lnRow;
                        loadRecordAddOns();
                        moveNextAddOns(false, false);
                    }
                }
            }
        });
        tblViewFreebies.setOnMouseClicked(event -> {
            if (freebies_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleQuotation_Freebies selected = (ModelVehicleQuotation_Freebies) tblViewFreebies.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(freebies_data.get(tblViewFreebies.getSelectionModel().getSelectedIndex()).getIndex06());
                        pnFreebies = lnRow;
                        loadRecordFreebies();
                        moveNextFreebies(false, false);
                    }
                }
            }
        });
//        JFXUtil.handleDisabledNodeClick(apAddOns, pnEditMode, nodeID -> {
//            if (JFXUtil.isObjectEqualTo(nodeID, "tfAddOns", "tfAccessories")) {
//                ShowMessageFX.Information(null, pxeModuleName, "");
//            }
//        });
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSave, btnCancel, btnSearch);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster, apAddOns, apPricing, apAdditionalInfo, apFreebies);

        if (psValidityId != null && !"".equals(psValidityId)) {
            JFXUtil.setButtonsVisibility(false, btnUpdate, btnBrowse, btnNew);
        }
        boolean lbShow4 = fnValue == EditMode.UPDATE;
        if (lbShow4) {
            JFXUtil.setDisabled(lbShow4, apMaster);
        }
        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case VehicleQuotationStatus.OPEN:
                break;
            case VehicleQuotationStatus.VOID:
            case VehicleQuotationStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
                break;
            case VehicleQuotationStatus.APPROVED:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
        }
    }
}
