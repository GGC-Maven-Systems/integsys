/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelVehicleAddOns_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
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
import javafx.scene.control.TablePosition;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehiclePromotion;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;
import ph.com.guanzongroup.integsys.model.ModelFreebies_Detail;
import ph.com.guanzongroup.integsys.model.ModelInclusion_Detail;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.model.ModelVehicleAddOn_Detail;
import ph.com.guanzongroup.integsys.model.ModelVehicleAddOn_List;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehiclePromotions_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0, pnAddOns = 0, pnFreebies = 0, pnInclusion = 0, pnMain = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static VehiclePromotion poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psValidityId = "";
    boolean pbEntered = false;
    boolean pbKeyPressed = false;

    private ObservableList<ModelVehicleAddOn_Detail> addons_data = FXCollections.observableArrayList();
    private ObservableList<ModelFreebies_Detail> freebies_data = FXCollections.observableArrayList();
    private ObservableList<ModelInclusion_Detail> inclusion_data = FXCollections.observableArrayList();
    private ObservableList<ModelVehicleAddOn_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelVehicleAddOn_List> main_data = FXCollections.observableArrayList();
    ObservableList<String> promotype = FXCollections.observableArrayList("Included", "Free", "Discounted");

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableMain, loadTableDetail, loadTableAddOns, loadTableFreebies, loadTableInclusion;
    ObservableList<String> PurchaseType = ModelSalesInquiry_Detail.PurchaseType;
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apAddOns, apFreebies, apInclusion, apPromotionApplicability;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnNew, btnUpdate, btnSearch, btnSave, btnVoid, btnCancel, btnHistory, btnPrint, btnRetrieve, btnClose;
    @FXML
    private TextField tfPromoCode, tfDescription, tfDiscountAmount, tfAddOn, tfDiscountAmountAddOns, tfFreebie, tfQuantity, tfInclusion, tfQuantityInclusion, tfBrand, tfModel, tfVariant;
    @FXML
    private DatePicker dpValidFrom, dpTo;
    @FXML
    private ComboBox cmbModeOfPayment, cmbPromoType;
    @FXML
    private CheckBox cbAddOns, cbActive, cbActiveInclusion, cbExclude, cbActivePromotionApplicability;
    @FXML
    private TableView tblViewAddOns, tblViewFreebies, tblViewInclusion, tblViewDetail, tblViewMain;
    @FXML
    private TableColumn tblNoAddOns, tblAddOn, tblAmount, tblDiscountAmt, tblNoFreebies, tblFreebie, tblQuantityFreebies, tblNoInclusion, tblInclusion, tblQuantityInclusion, tblNoDetail, tblBrand, tblModel, tblVariant, tblExclude, tblStatus, tblNoMain, tblFrom, tblTo, tblDescription;

    LocalDate ValidFrom;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehiclePromotion();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initAddOnsGrid();
            initMainGrid();
            initFreebiesGrid();
            initInclusionGrid();
            initDetailsGrid();

            initComboboxes();
            initTextFields();
            initDatepickers();
            initTableOnClick();
            clearTextFields();

            loadTableDetail.reload();
            pnEditMode = poController.getEditMode();
            poController.setWithUI(true);
            initButton(pnEditMode);
            Platform.runLater(() -> {
//            poController.setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.setCompanyId(psCompanyId);
//            poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
                loadRecordSearch();
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
                        poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        ValidFrom = CustomCommonUtil.parseDateStringToLocalDate(CustomCommonUtil.formatDateToShortString(poController.Master().getFromDate()));
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            poController.InitTransaction();
                            clearTextFields();
                            poController.Master().setCompanyId(psCompanyId);
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
                        return;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.AddDetail();
                                loadTableDetail.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getValidityId());
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
                        if (ValidityPeriodStatus.APPROVED.equals(poController.Master().getRecordStatus())) {
                            lsStat = "cancel";
                        }
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsStat + " transaction?") == true) {
                            if (ValidityPeriodStatus.OPEN.equals(poController.Master().getRecordStatus())) {
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

                            poJSON = poController.OpenTransaction(poController.Master().getValidityId());
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
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apAddOns, apFreebies, apInclusion, apPromotionApplicability);
                        return;
                    case "btnRetrieve":
                        retrievePromotions();
                        return;
                    case "btnPrint":
                        poController.printTransaction();
                        return;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

//                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisapprove", "btnVoid")) {
//                } else {
//                    loadRecordDetail();
//                    loadTableDetail.reload();
//                }
                loadTableAddOns.reload();
                loadTableFreebies.reload();
                loadTableInclusion.reload();
                loadTableDetail.reload();
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

    private void retrievePromotions() {
        poJSON = new JSONObject();
        poController.setTransactionStatus("0123");
        poJSON = poController.loadTransactionList();
        loadTableMain.reload();
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;
            TableView<?> currentTable = tblViewDetail;
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        //apAddOns
                        case "tfAddOn":
                            poJSON = poController.SearchAddOn(lsValue, false, pnAddOns);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfDiscountAmountAddOns);
                            }
                            loadTableAddOns.reload();
                            break;
                        //apFreebies
                        case "tfFreebie":
                            poJSON = poController.SearchFreebie(lsValue, false, pnFreebies);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfQuantity);
                            }
                            loadTableFreebies.reload();
                            break;
                        //apInclusion
                        case "tfInclusion":
                            poJSON = poController.SearchInclusion(lsValue, false, pnInclusion);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfQuantityInclusion);
                            }
                            loadTableInclusion.reload();
                            break;
                        //apPromotionApplicability
                        case "tfBrand":
                            poJSON = poController.SearchBrand(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfModel);
                            }
                            loadTableDetail.reload();
                            break;
                        case "tfModel":
                            poJSON = poController.SearchModel(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfVariant);
                            }
                            loadTableDetail.reload();
                            break;
                        case "tfVariant":
                            poJSON = poController.SearchModelVariant(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            loadTableDetail.reload();
                            break;
                    }
                    break;
                case UP:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfAddOns", "tfDiscountAmountAddOns"}, (Runnable) () -> moveNextAddOns(true, true)},
                        {new String[]{"tfFreebie", "tfQuantity"}, (Runnable) () -> moveNextFreebies(true, true)},
                        {new String[]{"tfInclusion", "tfQuantityInclusion"}, (Runnable) () -> moveNextInclusion(true, true)},
                        {new String[]{"tfBrand", "tfModel", "tfVariant"}, (Runnable) () -> moveNext(true, true)},});
                    break;
                case DOWN:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfAddOns", "tfDiscountAmountAddOns"}, (Runnable) () -> moveNextAddOns(false, true)},
                        {new String[]{"tfFreebie", "tfQuantity"}, (Runnable) () -> moveNextFreebies(false, true)},
                        {new String[]{"tfInclusion", "tfQuantityInclusion"}, (Runnable) () -> moveNextInclusion(false, true)},
                        {new String[]{"tfBrand", "tfModel", "tfVariant"}, (Runnable) () -> moveNext(false, true)},});
                    break;
                default:
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    newIndex = !isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex10())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex10());
                    if (!details_data.isEmpty()) {
                        pnDetail = newIndex;
                        loadRecordPromoAvailability();
                    }
                    break;
                case "tblViewAddOns":
                    if (!addons_data.isEmpty()) {
                        pnAddOns = newIndex;
                        loadRecordAddOns();
                    }
                    break;
                case "tblViewFreebies":
                    if (!freebies_data.isEmpty()) {
                        pnFreebies = newIndex;
                        loadRecordFreebies();
                    }
                    break;
                case "tblViewInclusion":
                    if (!inclusion_data.isEmpty()) {
                        pnInclusion = newIndex;
                        loadRecordInclusion();
                    }
                    break;
            }
        }
    };
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDescription":
                        poJSON = poController.PromotionMaster().setDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfDiscountAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.PromotionMaster().setDiscountAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });
    ChangeListener<Boolean> txtAddOns_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfAddOn":
                        if (lsValue.isEmpty()) {
                            poController.PromotionAddOnList(pnAddOns).setAddOnId("");
                        }
                        break;
                    case "tfDiscountAmountAddOns":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.PromotionAddOnList(pnAddOns).setDiscountAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEntered) {
                            moveNextAddOns(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableAddOns.reload();
                });
            });
    ChangeListener<Boolean> txtFreebies_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfFreebie":
                        if (lsValue.isEmpty()) {
                            poController.PromotionFreebieList(pnFreebies).setFreebieId("");
                        }
                        break;
                    case "tfQuantity":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.PromotionFreebieList(pnFreebies).setQuantity(Integer.parseInt(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEntered) {
                            moveNextFreebies(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableFreebies.reload();
                });
            });

    ChangeListener<Boolean> txtInclusion_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfInclusion":
                        if (lsValue.isEmpty()) {
                            poController.PromotionFreebieList(pnFreebies).setFreebieId("");
                        }
                        break;
                    case "tfQuantityInclusion":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.PromotionFreebieList(pnFreebies).setQuantity(Integer.parseInt(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEntered) {
                            moveNextInclusion(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableInclusion.reload();
                });
            });

    ChangeListener<Boolean> txtPromotionApplicability_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBrand":
                        if (lsValue.isEmpty()) {
                            poController.PromotionApplicabilityList(pnDetail).setBrandId("");
                            poController.PromotionApplicabilityList(pnDetail).setModelId("");
                            poController.PromotionApplicabilityList(pnDetail).setVariantId("");
                        }
                        break;
                    case "tfModel":
                        if (lsValue.isEmpty()) {
                            poController.PromotionApplicabilityList(pnDetail).setModelId("");
                            poController.PromotionApplicabilityList(pnDetail).setVariantId("");
                        }
                        break;
                    case "tfVariant":
                        if (lsValue.isEmpty()) {
                            poController.PromotionApplicabilityList(pnDetail).setVariantId("");
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });

    private void loadRecordMaster() {
        if (poController.getDetailCount() <= 0) {
            return;
        }
        JFXUtil.setStatusValue(lblStatus, ValidityPeriodStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getRecordStatus());
        tfPromoCode.setText(poController.PromotionMaster().getPromotionCode());
        String lsValidFrom = JFXUtil.formatDateToString(poController.Master().getFromDate());
        JFXUtil.setDateValue(dpValidFrom, JFXUtil.isObjectEqualTo(lsValidFrom, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidFrom, "yyyy-MM-dd"));
        String lsTo = JFXUtil.formatDateToString(poController.Master().getThruDate());
        JFXUtil.setDateValue(dpTo, JFXUtil.isObjectEqualTo(lsTo, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsTo, "yyyy-MM-dd"));
        tfDescription.setText(poController.Detail(0).getDescription());
        JFXUtil.setCmbValue(cmbModeOfPayment, !JFXUtil.isObjectEqualTo(poController.Detail(0).getPaymentForm(), "", null) ? Integer.valueOf(poController.Detail(0).getPaymentForm()) : -1);
        tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(0).getDiscountAmount().doubleValue(), true));
    }

    private void loadRecordAddOns() {
        try {
            if (pnAddOns < 0 || pnAddOns > poController.getPromotionAddOnCount() - 1) {
                return;
            }
            tfAddOn.setText(poController.PromotionAddOnList(pnAddOns).AddOnType().getAddTypeName());
            JFXUtil.setCmbValue(cmbPromoType, !poController.PromotionAddOnList(pnAddOns).getPromoType().equals("") ? Integer.valueOf(poController.PromotionAddOnList(pnAddOns).getPromoType()) : -1);
            tfDiscountAmountAddOns.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PromotionAddOnList(pnAddOns).getDiscountAmount().doubleValue(), true));
            cbAddOns.setSelected(poController.PromotionAddOnList(pnAddOns).getRecordStatus());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordFreebies() {
        try {
            if (pnFreebies < 0 || pnFreebies > poController.getPromotionFreebieCount() - 1) {
                return;
            }
            tfFreebie.setText(poController.PromotionFreebieList(pnFreebies).Freebie().getDescription());
            tfQuantity.setText(String.valueOf(poController.PromotionFreebieList(pnFreebies).getQuantity()));
            cbActive.setSelected(poController.PromotionFreebieList(pnFreebies).getRecordStatus());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordInclusion() {
        try {
            if (pnInclusion < 0 || pnInclusion > poController.getPromotionInclusionCount() - 1) {
                return;
            }
            tfInclusion.setText(poController.PromotionInclusionList(pnInclusion).Inclusion().getDescription());
            tfQuantityInclusion.setText(String.valueOf(poController.PromotionInclusionList(pnInclusion).getQuantity()));
            cbActiveInclusion.setSelected(poController.PromotionInclusionList(pnInclusion).getRecordStatus());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordPromoAvailability() {
        if (pnDetail < 0 || pnDetail > poController.getPromotionApplicabilityCount() - 1) {
            return;
        }
        try {
            tfBrand.setText(poController.PromotionApplicabilityList(pnDetail).Brand().getDescription());
            tfModel.setText(poController.PromotionApplicabilityList(pnDetail).Model().getDescription());
            tfVariant.setText(poController.PromotionApplicabilityList(pnDetail).ModelVariant().getDescription());
            cbExclude.setSelected(poController.PromotionApplicabilityList(pnDetail).getExcludeFlag().equals(""));
            cbActivePromotionApplicability.setSelected(poController.PromotionApplicabilityList(pnDetail).getRecordStatus());
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
                case "cbActive": // freebies
                    poJSON = poController.Detail(pnDetail).setRecordStatus(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableDetail.reload();
                    break;
                case "cbAddOns":
                    poJSON = poController.PromotionAddOnList(pnAddOns).setRecordStatus(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableAddOns.reload();
                    break;
                case "cbActiveInclusion":
                    poJSON = poController.PromotionInclusionList(pnInclusion).setRecordStatus(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableInclusion.reload();
                    break;
                case "cbExclude":
                    poJSON = poController.PromotionApplicabilityList(pnDetail).setExcludeFlag(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableDetail.reload();
                    break;
                case "cbActivePromotionApplicability":
                    poJSON = poController.PromotionApplicabilityList(pnDetail).setRecordStatus(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableDetail.reload();
                    break;
            }
        }
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbModeOfPayment":
                        poJSON = poController.Detail(0).setPaymentForm(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                    case "cmbPromoType":
                        poJSON = poController.PromotionAddOnList(pnAddOns).setPromoType(String.valueOf(selectedValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordAddOns();
                        break;
                }
            });
    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                try {
                    poJSON = new JSONObject();
                    JFXUtil.setJSONSuccess(poJSON, "success");
                    LocalDate dateNow = LocalDate.now();
                    String inputText = datePicker.getEditor().getText();
                    if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                        return;
                    }
                    switch (datePicker.getId()) {
                        case "dpValidFrom":
                            LocalDate selectedFromDate = dpValidFrom.getValue();
                            LocalDate toDate = dpTo.getValue();
                            LocalDate currentDate = oApp.getServerDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            LocalDate firstDayOfCurrentMonth = currentDate.withDayOfMonth(1);
                            if (selectedFromDate != null && selectedFromDate.isBefore(firstDayOfCurrentMonth)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be before the current month.");
                                dpValidFrom.setValue(firstDayOfCurrentMonth);
                                return;
                            }
                            if (toDate != null && selectedFromDate.isAfter(toDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                                loadRecordMaster();
                                return;
                            }
                            if (pbSuccess) {
                                poController.Master().setFromDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }

                            pbSuccess = false;
                            loadTableDetail.reload();
                            pbSuccess = true;
                            break;
                        case "dpTo":
                            LocalDate selectedToDate = dpTo.getValue();
                            LocalDate fromDate = dpValidFrom.getValue();
                            if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                                loadRecordMaster();
                                return;
                            }
                            if (pbSuccess) {
                                poController.Master().setThruDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }

                            pbSuccess = false;
                            loadTableDetail.reload();
                            pbSuccess = true;
                            break;
                        default:
                            break;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(PurchaseType, cmbModeOfPayment), new JFXUtil.Pairs<>(promotype, cmbPromoType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbModeOfPayment, cmbPromoType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbModeOfPayment, cmbPromoType);
    }

    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpValidFrom, dpTo);
        JFXUtil.setActionListener(datepicker_Action, dpValidFrom, dpTo);

        dpTo.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Lost focus
                if (JFXUtil.isObjectEqualTo(dpTo.getEditor().getText(), null, "")) {
//                    try {
                    if (pbSuccess) {
                        poJSON = poController.Master().setThruDate(null);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        dpTo.setValue(null);
                    }
                    pbSuccess = false;
                    loadTableDetail.reload();
                    pbSuccess = true;
//                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
//                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//                    }
                }
            }
        });
    }

    private void moveNext(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apPromotionApplicability.requestFocus();
            pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex10())
                    : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex10());
        }
        loadRecordPromoAvailability();
        if (pnDetail < 0 || pnDetail > poController.getPromotionApplicabilityCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.PromotionApplicabilityList(pnDetail).getBrandId(), tfBrand},
            {poController.PromotionApplicabilityList(pnDetail).getModelId(), tfModel},
            {poController.PromotionApplicabilityList(pnDetail).getVariantId(), tfVariant},}, tfVariant); // default
    }

    private void moveNextAddOns(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apAddOns.requestFocus();
            pnAddOns = isUp ? Integer.parseInt(addons_data.get(JFXUtil.moveToPreviousRow(tblViewAddOns)).getIndex10())
                    : Integer.parseInt(addons_data.get(JFXUtil.moveToNextRow(tblViewAddOns)).getIndex10());
        }
        loadRecordAddOns();
        if (pnAddOns < 0 || pnAddOns > poController.getPromotionAddOnCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.PromotionAddOnList(pnAddOns).getAddOnId(), tfAddOn},}, tfDiscountAmountAddOns); // default
    }

    private void moveNextFreebies(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apFreebies.requestFocus();
            pnFreebies = isUp ? Integer.parseInt(freebies_data.get(JFXUtil.moveToPreviousRow(tblViewFreebies)).getIndex10())
                    : Integer.parseInt(freebies_data.get(JFXUtil.moveToNextRow(tblViewFreebies)).getIndex10());
        }
        loadRecordFreebies();
        if (pnFreebies < 0 || pnFreebies > poController.getPromotionFreebieCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.PromotionFreebieList(pnFreebies).getPromoFreeId(), tfFreebie},}, tfQuantity); // default
    }

    private void moveNextInclusion(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apInclusion.requestFocus();
            pnInclusion = isUp ? Integer.parseInt(inclusion_data.get(JFXUtil.moveToPreviousRow(tblViewInclusion)).getIndex10())
                    : Integer.parseInt(inclusion_data.get(JFXUtil.moveToNextRow(tblViewInclusion)).getIndex10());
        }
        loadRecordInclusion();
        if (pnInclusion < 0 || pnInclusion > poController.getPromotionInclusionCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.PromotionInclusionList(pnInclusion).getInclusionId(), tfInclusion},}, tfQuantityInclusion); // default
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfDescription, tfDiscountAmount);
        JFXUtil.setFocusListener(txtAddOns_Focus, tfAddOn);
        JFXUtil.setFocusListener(txtFreebies_Focus, tfFreebie, tfQuantity);
        JFXUtil.setFocusListener(txtInclusion_Focus, tfInclusion, tfQuantityInclusion);
        JFXUtil.setFocusListener(txtPromotionApplicability_Focus, tfBrand, tfModel, tfVariant);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apAddOns, apFreebies, apInclusion, apPromotionApplicability);
        JFXUtil.setCommaFormatter(tfDiscountAmount);
        CustomCommonUtil.inputIntegersOnly(tfQuantity, tfQuantityInclusion);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewAddOns, tblViewFreebies, tblViewInclusion, tblViewDetail);
        JFXUtil.adjustColumnForScrollbar(tblViewAddOns, tblViewFreebies, tblViewInclusion, tblViewDetail);
    }

    private String getData(String lsValue) {
        return JFXUtil.isObjectEqualTo(lsValue, null, "") ? "" : lsValue;
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMain,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            int lnCtr;
                            main_data.clear();
                            for (lnCtr = 0; lnCtr < poController.getTransactionListCount(); lnCtr++) {
//                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getVariantId(), null, "")) {
//                                    continue;
//                                }
                                main_data.add(
                                        new ModelVehicleAddOn_List(
                                                String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poController.TransactionList(lnCtr).getValidityId())),
                                                String.valueOf(CustomCommonUtil.formatDateToShortString(poController.TransactionList(lnCtr).getFromDate())),
                                                String.valueOf(CustomCommonUtil.formatDateToShortString(poController.TransactionList(lnCtr).getFromDate())),
                                                "",
                                                "",
                                                "",
                                                "", "",
                                                "", "", "", "", "", ""
                                        ));
                            }
                        });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                });
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEntered = false;
                        details_data.clear();
                        try {
                            for (lnCtr = 0; lnCtr < poController.getPromotionApplicabilityCount(); lnCtr++) {
//                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getVariantId(), null, "")) {
//                                    continue;
//                                }
                                details_data.add(
                                        new ModelVehicleAddOn_Detail(String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poController.PromotionApplicabilityList(lnCtr).Brand().getDescription())),
                                                getData(String.valueOf(poController.PromotionApplicabilityList(lnCtr).Model().getDescription())),
                                                getData(String.valueOf(poController.PromotionApplicabilityList(lnCtr).ModelVariant().getDescription())),
                                                poController.PromotionApplicabilityList(lnCtr).getExcludeFlag().equals("") ? "YES" : "NO",
                                                poController.PromotionApplicabilityList(lnCtr).getRecordStatus() ? "Active" : "Inactive",
                                                "", "", "",
                                                String.valueOf(lnCtr), "", "", "", "", ""
                                        ));
                            }

                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewDetail.getSelectionModel().select(0);
                                    tblViewDetail.getFocusModel().focus(0);
                                    pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                                    loadRecordPromoAvailability();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewDetail.getSelectionModel().select(pnDetail);
                                tblViewDetail.getFocusModel().focus(pnDetail);
                                loadRecordPromoAvailability();
                            }
//                            loadRecordMaster();
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableAddOns = new JFXUtil.ReloadableTableTask(
                tblViewAddOns,
                addons_data,
                () -> {
                    pbEntered = false;
                    // Setting data to table detail
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEntered = false;
                        addons_data.clear();
                        try {
                            for (lnCtr = 0; lnCtr < poController.getPromotionAddOnCount(); lnCtr++) {
//                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getVariantId(), null, "")) {
//                                    continue;
//                                }
                                addons_data.add(
                                        new ModelVehicleAddOn_Detail(String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poController.PromotionAddOnList(lnCtr).AddOnType().getAddTypeName())),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PromotionAddOnList(lnCtr).AddOnType().getAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PromotionAddOnList(lnCtr).getDiscountAmount(), false)),
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                String.valueOf(lnCtr), "", "", "", "", ""
                                        ));
                            }

                            if (pnAddOns < 0 || pnAddOns
                                    >= addons_data.size()) {
                                if (!addons_data.isEmpty()) {
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
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableFreebies = new JFXUtil.ReloadableTableTask(
                tblViewFreebies,
                freebies_data,
                () -> {
                    pbEntered = false;
                    // Setting data to table detail
                    Platform.runLater(() -> {
                        try {

                            int lnCtr;
                            pbEntered = false;
                            freebies_data.clear();
                            for (lnCtr = 0; lnCtr < poController.getPromotionFreebieCount(); lnCtr++) {
                                //if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getVariantId(), null, "")) {
//                                    continue;
//                                }
                                freebies_data.add(
                                        new ModelFreebies_Detail(String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poController.PromotionFreebieList(lnCtr).Freebie().getDescription())),
                                                String.valueOf(poController.PromotionFreebieList(lnCtr).getQuantity()),
                                                "",
                                                "",
                                                "",
                                                "",
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
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                    });

                });
        loadTableInclusion = new JFXUtil.ReloadableTableTask(
                tblViewInclusion,
                inclusion_data,
                () -> {
                    pbEntered = false;
                    // Setting data to table detail
                    Platform.runLater(() -> {
                        try {
                            int lnCtr;
                            pbEntered = false;
                            inclusion_data.clear();
                            for (lnCtr = 0; lnCtr < poController.getPromotionInclusionCount(); lnCtr++) {
                                //  if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getVariantId(), null, "")) {
//                                    continue;
//                                }
                                inclusion_data.add(
                                        new ModelInclusion_Detail(String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poController.PromotionInclusionList(lnCtr).Inclusion().getDescription())),
                                                String.valueOf(poController.PromotionInclusionList(lnCtr).getQuantity()),
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                String.valueOf(lnCtr)
                                        ));

                            }
                            if (pnInclusion < 0 || pnInclusion
                                    >= inclusion_data.size()) {
                                if (!inclusion_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewInclusion.getSelectionModel().select(0);
                                    tblViewInclusion.getFocusModel().focus(0);
                                    pnInclusion = tblViewInclusion.getSelectionModel().getSelectedIndex();
                                    loadRecordInclusion();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewInclusion.getSelectionModel().select(pnInclusion);
                                tblViewInclusion.getFocusModel().focus(pnInclusion);
                                loadRecordInclusion();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                });
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblNoMain);
        JFXUtil.setColumnLeft(tblDescription, tblFrom, tblTo);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMain);
        tblViewMain.setItems(main_data);
    }

    private void initAddOnsGrid() {
        JFXUtil.setColumnCenter(tblNoAddOns);
        JFXUtil.setColumnLeft(tblAddOn);
        JFXUtil.setColumnRight(tblAmount, tblDiscountAmt);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewAddOns);
        tblViewAddOns.setItems(addons_data);
    }

    private void initFreebiesGrid() {
        JFXUtil.setColumnCenter(tblNoFreebies);
        JFXUtil.setColumnLeft(tblFreebie);
        JFXUtil.setColumnRight(tblQuantityFreebies);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewFreebies);
        tblViewFreebies.setItems(freebies_data);
    }

    private void initInclusionGrid() {
        JFXUtil.setColumnCenter(tblNoInclusion);
        JFXUtil.setColumnLeft(tblInclusion);
        JFXUtil.setColumnRight(tblQuantityInclusion);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewInclusion);
        tblViewInclusion.setItems(inclusion_data);
    }

    private void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblNoDetail);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblExclude);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apAddOns, apFreebies, apInclusion, apPromotionApplicability);
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelVehicleAddOn_List selected = (ModelVehicleAddOn_List) tblViewMain.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);

                poJSON = poController.OpenTransaction(poController.TransactionList(pnMain).getValidityId());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                loadRecordMaster();
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTableOnClick() {
        tblViewMain.setOnMouseClicked(event -> {
            pnMain = tblViewMain.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        tblViewAddOns.setOnMouseClicked(event -> {
            if (addons_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleAddOns_Detail selected = (ModelVehicleAddOns_Detail) tblViewAddOns.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(addons_data.get(tblViewAddOns.getSelectionModel().getSelectedIndex()).getIndex10());
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
                    ModelFreebies_Detail selected = (ModelFreebies_Detail) tblViewFreebies.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(freebies_data.get(tblViewFreebies.getSelectionModel().getSelectedIndex()).getIndex10());
                        pnFreebies = lnRow;
                        loadRecordFreebies();
                        moveNextFreebies(false, false);
                    }
                }
            }
        });
        tblViewInclusion.setOnMouseClicked(event -> {
            if (inclusion_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelInclusion_Detail selected = (ModelInclusion_Detail) tblViewInclusion.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(inclusion_data.get(tblViewInclusion.getSelectionModel().getSelectedIndex()).getIndex10());
                        pnInclusion = lnRow;
                        loadRecordInclusion();
                        moveNextInclusion(false, false);
                    }
                }
            }
        });
        tblViewDetail.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleAddOn_Detail selected = (ModelVehicleAddOn_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex10());
                        pnDetail = lnRow;
                        loadRecordPromoAvailability();
                        moveNext(false, false);
                    }
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMain, item -> ((ModelVehicleAddOn_List) item).getIndex01(), highlightedRowsMain);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster, apAddOns, apFreebies, apInclusion, apPromotionApplicability);
        JFXUtil.setButtonsVisibility(lbShow2, btnPrint);
        JFXUtil.setButtonsVisibility(false, btnVoid);
        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getRecordStatus()) {
            case ValidityPeriodStatus.OPEN:
            case ValidityPeriodStatus.APPROVED:
                JFXUtil.setButtonsVisibility(true, btnVoid);
                break;
            case ValidityPeriodStatus.VOID:
            case ValidityPeriodStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
                break;
        }
    }
}
