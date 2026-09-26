package ph.com.guanzongroup.integsys.views;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import net.sf.jasperreports.engine.JRException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.constant.Model_Shop_Type;
import ph.com.guanzongroup.cas.sales.mcpromo.MCPromoSales;
import ph.com.guanzongroup.cas.sales.mcpromo.common.PromoSource;
import ph.com.guanzongroup.cas.sales.mcpromo.common.PromoType;
import ph.com.guanzongroup.cas.sales.mcpromo.common.SalesPromotionStatus;
import ph.com.guanzongroup.cas.sales.mcpromo.common.TransactionType;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Branch_Area;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Brand;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_GiveAway_Item;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Master;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Model;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Model_Exception;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Province;
import ph.com.guanzongroup.cas.sales.mcpromo.services.PromoControllers;
import ph.com.guanzongroup.integsys.views.ScreenInterface;
import ph.com.guanzongroup.integsys.views.child.APListController;
import ph.com.guanzongroup.integsys.views.child.APStatusController;
import ph.com.guanzongroup.integsys.views.unloadForm;

public class PromoMaintenanceEntryController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "MC Promo Entry";
    private String psIndustryID, psCompanyID, psCategoryID;
    private Control lastFocusedControl;
    private MCPromoSales poAppController;
    private ObservableList<Model_Shop_Type> paShopType;
    private ObservableList<Model_Sales_Promotion_Province> paProvince;
    private ObservableList<Model_Sales_Promotion_Branch_Area> paBranchArea;
    private ObservableList<Model_Sales_Promotion_Brand> paBrand;
    private ObservableList<Model_Sales_Promotion_Model> paModel;
    private ObservableList<Model_Sales_Promotion_Model_Exception> paModelException;
    private ObservableList<Model_Sales_Promotion_GiveAway_Item> paGiveAway;
    private ObservableList<Model> paCombined;
    private int pnEditMode, pnRow, pnGiveAway, pnBrand, pnModel, pnModelException;

    private ToggleGroup tgInsurance;
    private ToggleGroup tgRegistration;
    private ToggleGroup tgIncentive;
    private boolean pbSuppressToggleListener = false;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apShopType,
            apPromoDetail, apOtherInfo;

    @FXML
    private TextField tfSearchPromoID, tfSearchDescription;

    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnPreview,
            btnConfirm, btnVoid, btnSave, btnCancel, btnSaveNew, btnDuplicate,
            btnRetrieve, btnClose, btnDuplicateMechanic;

    @FXML
    private TextField tfPromo, tfSRPFrom, tfSRPThru, tfSupplier, tfReferenceNo, tfSubject, tfRemarks;

    @FXML
    private DatePicker dpTransactionDate, dpPromoStart, dpPromoEnd;

    @FXML
    private ComboBox cbTransactionType, cbPromoSource, cbPromoType;

    @FXML
    private HBox hbProvince, hbArea, hbShopType, hbBrand, hbModel, hbModelException, hbGiveAway;
    @FXML
    private TabPane tabPaneDetail;

    @FXML
    private Tab tabModel, tabGiveAway;

    @FXML
    private TableView<Model> tblViewModel;

    @FXML
    private TableColumn<Model, String> tblColModelbrand, tblColModelmodel, tblColModelSRP, tblColModeldiscRate,
            tblColModeldiscAmt, tblColModelnetSRP, tblColModelRegistration, tblColModelinsurance,
            tblColModelfreight, tblColModelincentiveAmount, tblColModelNote, tblColModelAction;

    @FXML
    private TableView<Model_Sales_Promotion_GiveAway_Item> tblViewGiveAway;

    @FXML
    private TableColumn<Model_Sales_Promotion_GiveAway_Item, String> tblColGiveAwayBarcode, tblColGiveAwayDescription, tblColGiveAwayQuality,
            tblColGiveAwayNotes, tblColGiveAwayAction;

    @FXML
    private TextArea taNoteModel, taNoteGiveAway;

    @FXML
    private TextField tfBrand, tfModel, tfSRP, tfDiscRate, tfDiscAmount, tfNetSRP, tfFreight, tfIncentiveAmount,
            tfBarcode, tfDescription, tfQuantity;

    @FXML
    private RadioButton rbInsurance1, rbRegistration1, rbInsurance0, rbRegistration0, rbIncentive1, rbIncentive0;

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
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new PromoControllers(poApp, poLogWrapper).MCPromoSales();
            poAppController.setTransactionStatus("10");

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {
                poAppController.setTransactionStatus("10");
                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCompanyID(psCompanyID);
                poAppController.setCategoryID(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID
                        + "\nCompany :" + psCompanyID
                        + "\nCategory:" + psCategoryID);

                btnNew.fire();
            });
            initializeTableGiveAway();
            initializeTableModel();
            initControlEvents();
            initializeChipInputs();
            clearPromoDetail();
            clearPromoGiveAway();
            initRadioGroups();
            pnBrand = -1;
            pnModel = -1;
            pnModelException = -1;
            pnGiveAway = -1;
            lblSource.setText(poAppController.getMaster().Industry().getDescription());

        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());

        }
    }

    @FXML
    void ontblModelClicked(MouseEvent e) {
        pnRow = tblViewModel.getSelectionModel().getSelectedIndex() + 1;
        if (pnRow < 0) {
            return;
        }

        if (e.getClickCount() == 1 && !e.isConsumed()) {
            try {
                e.consume();
                Model loSelected = tblViewModel.getSelectionModel().getSelectedItem();
                if (loSelected == null) {
                    return;
                }
                if (loSelected instanceof Model_Sales_Promotion_Brand) {
                    Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loSelected;
                    pnBrand = paBrand.indexOf(loPromoBrand) + 1;       // 1-based index in paModel
                    pnModelException = -1;
                    pnModel = -1;   // not applicable for this row
                } else if (loSelected instanceof Model_Sales_Promotion_Model) {
                    Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loSelected;
                    pnModel = paModel.indexOf(loPromoModel) + 1;       // 1-based index in paModel
                    pnModelException = -1;
                    pnBrand = -1;// not applicable for this row
                } else if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                    Model_Sales_Promotion_Model_Exception loException = (Model_Sales_Promotion_Model_Exception) loSelected;
                    pnModelException = paModelException.indexOf(loException) + 1; // 1-based index in paModelException
                    pnModel = -1;
                    pnBrand = -1;// not applicable for this row
                }

                if (loSelected instanceof Model_Sales_Promotion_Brand) {
                    // Brand: SRP / disc rate / disc amount are free text, Net SRP always 0
                    tfBrand.setDisable(false);
                    tfModel.setDisable(true);
                    tfSRP.setDisable(false);
                    tfDiscRate.setDisable(false);
                    tfDiscAmount.setDisable(false);
                    tfNetSRP.setDisable(true);
                    tfNetSRP.setText("0.00");
                    tfFreight.setDisable(false);
                    rbInsurance1.setDisable(false);
                    rbInsurance0.setDisable(false);
                    rbRegistration1.setDisable(false);
                    rbRegistration0.setDisable(false);
                    rbIncentive1.setDisable(false);
                    rbIncentive0.setDisable(false);
                    tfIncentiveAmount.setDisable(rbIncentive0.isSelected());

                } else if (loSelected instanceof Model_Sales_Promotion_Model) {
                    // Model: SRP/discRate/discAmount editable, Net SRP is computed+display-only
                    tfBrand.setDisable(true);
                    tfModel.setDisable(false);
                    tfSRP.setDisable(false);
                    tfDiscRate.setDisable(false);
                    tfDiscAmount.setDisable(false);
                    tfNetSRP.setDisable(true); // computed, not directly editable
                    tfFreight.setDisable(false);
                    rbInsurance1.setDisable(false);
                    rbInsurance0.setDisable(false);
                    rbRegistration1.setDisable(false);
                    rbRegistration0.setDisable(false);
                    rbIncentive1.setDisable(false);
                    rbIncentive0.setDisable(false);
                    tfIncentiveAmount.setDisable(rbIncentive0.isSelected());

                } else if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                    // Exception: lock everything down
                    tfBrand.setDisable(true);
                    tfModel.setDisable(false);
                    tfSRP.setDisable(true);
                    tfDiscRate.setDisable(true);
                    tfDiscAmount.setDisable(true);
                    tfNetSRP.setDisable(true);
                    tfFreight.setDisable(true);
                    rbInsurance1.setDisable(true);
                    rbInsurance0.setDisable(true);
                    rbRegistration1.setDisable(true);
                    rbRegistration0.setDisable(true);
                    rbIncentive1.setDisable(true);
                    rbIncentive0.setDisable(true);
                    taNoteModel.setDisable(true);
                    tfIncentiveAmount.setDisable(true);
                }

                loadSelectedTransactionModel(pnRow);
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {

                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            }

        }
        return;
    }

    @FXML
    void ontblGiveAwayClicked(MouseEvent e) {
        try {
            pnGiveAway = tblViewGiveAway.getSelectionModel().getSelectedIndex() + 1;
            if (pnGiveAway <= 0) {
                return;
            }

            loadSelectedTransactionGiveAway(pnGiveAway);
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnPreview":
                    if (tfPromo.getText() == null || tfPromo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", psFormName, null);
                        return;
                    }

                    if (!isJSONSuccess(poAppController.printRecord(), "Initialize Print Preview")) {
                        return;
                    }
                    break;
                case "btnSearch":
                    if (lastFocusedControl == null) {
                        if (tfBrand.isDisable()) {
                            if (!isJSONSuccess(poAppController.searchPromotionByBrand(-1, (tfBrand.getText() == null ? "" : tfBrand.getText()), false),
                                    "Initialize Search Brand! ")) {
                                return;
                            }
                        } else {
                            if (!isJSONSuccess(poAppController.searchPromotionByModel(-1, (tfModel.getText() == null ? "" : tfModel.getText()), false),
                                    "Initialize Search Model! ")) {
                                return;
                            }
                        }
                        getLoadedTransaction();
                        refreshChipBoxes();
                        break;

                    }
                    switch (lastFocusedControl.getId()) {
                        case "tfBrand":
                        case "tfModel":
                            Model loSelected = tblViewModel.getSelectionModel().getSelectedItem();
                            if (loSelected == null) {
                                if (lastFocusedControl.getId().equals(tfBrand)) {
                                    if (!isJSONSuccess(poAppController.searchPromotionByBrand(-1, (tfBrand.getText() == null ? "" : tfBrand.getText()), false),
                                            "Initialize Search Brand! ")) {
                                        return;
                                    }
                                } else {
                                    if (!isJSONSuccess(poAppController.searchPromotionByModel(-1, (tfModel.getText() == null ? "" : tfModel.getText()), false),
                                            "Initialize Search Model! ")) {
                                        return;
                                    }
                                }
                            }
                            if (loSelected instanceof Model_Sales_Promotion_Brand) {
                                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loSelected;
                                if (!isJSONSuccess(poAppController.searchPromotionByBrand(pnBrand, tfBrand.getText(), false),
                                        "Initialize Search Brand! ")) {
                                    return;
                                }
                            } else if (loSelected instanceof Model_Sales_Promotion_Model) {
                                if (!isJSONSuccess(poAppController.searchPromotionByModel(pnModel, tfModel.getText(), false),
                                        "Initialize Search Model! ")) {
                                    return;
                                }
                            } else if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                                if (!isJSONSuccess(poAppController.searchPromotionByModelException(pnModelException, tfModel.getText(), false),
                                        "Initialize Search Model! ")) {
                                    return;
                                }
                            }
                            refreshChipBoxes();
                            break;

                        case "tfBarcode":
                            if (!isJSONSuccess(poAppController.searchPromotionByGiveAway(pnGiveAway, (tfBarcode.getText() == null ? "" : tfBarcode.getText()), true),
                                    "Initialize Search Give Away! ")) {
                                return;
                            }
                            refreshChipBoxes();
                            break;
                        case "tfDescription":
                            if (!isJSONSuccess(poAppController.searchPromotionByGiveAway(pnGiveAway, (tfDescription.getText() == null ? "" : tfDescription.getText()), false),
                                    "Initialize Search Give Away! ")) {
                                return;
                            }

                            refreshChipBoxes();
                            break;

                        default:
                            if (tfBrand.isDisable()) {
                                if (!isJSONSuccess(poAppController.searchPromotionByBrand(-1, (tfBrand.getText() == null ? "" : tfBrand.getText()), false),
                                        "Initialize Search Brand! ")) {
                                    return;
                                }
                            } else {
                                if (!isJSONSuccess(poAppController.searchPromotionByModel(-1, (tfModel.getText() == null ? "" : tfModel.getText()), false),
                                        "Initialize Search Model! ")) {
                                    return;
                                }
                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;
                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        if (!isJSONSuccess(poAppController.searchTransaction(tfPromo.getText(), true, true),
                                "Initialize Browse Transaction")) {
                            return;
                        }
                        getLoadedTransaction();
                        initButtonDisplay(poAppController.getEditMode());
                        break;

                    }
                    switch (lastFocusedControl.getId()) {
                        case "tfSearchPromoID":

                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchPromoID.getText() == null ? "" : tfSearchPromoID.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;

                        case "tfSearchDescription":
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchDescription.getText() == null ? "" : tfSearchDescription.getText(), false, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;

                        default:
                            //Search record
                            if (!isJSONSuccess(poAppController.searchTransaction("", true, true),
                                    "Initialize Browse Transaction")) {
                                return;
                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;

                case "btnNew":
                    if (!isJSONSuccess(poAppController.newTransaction(), "Initialize New Transaction")) {
                        return;
                    }
//                    clearAllInputs();
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnUpdate":
                    if (poAppController.getMaster().getPromoID() == null || poAppController.getMaster().getPromoID().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Promo Maintenance", "");
                        return;
                    }

                    if (!isJSONSuccess(poAppController.UpdateTransaction(), "Initialize Update Transaction")) {
                        return;
                    }

                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnSave":
                    if (tfPromo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Promo Maintenance", "");
                        return;
                    }
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save transaction?") != true) {
                        return;
                    }
                    if (!isJSONSuccess(poAppController.saveTransaction(), "Initialize Save Transaction")) {
                        return;
                    }
                    if (ShowMessageFX.YesNo(null, psFormName, "Do you want to open preview of this Promo?") == true) {
                        if (!isJSONSuccess(poAppController.printRecord(), "Initialize Print Preview")) {
                            return;
                        }
                    }
                    if (ShowMessageFX.YesNo(null, psFormName, "Do you want to confirm transaction?") == true) {
                        if (!isJSONSuccess(poAppController.CloseTransaction(), "Initialize Close Transaction")) {
                            return;
                        }
                        if (ShowMessageFX.YesNo(null, psFormName, "Do you want to open preview of this Promo?") == true) {
                            if (!isJSONSuccess(poAppController.printRecord(), "Initialize Print Preview")) {
                            return;
                        }
                        }
                    }

                    reloadTableModel();
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();

                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        if (poAppController.getEditMode() != EditMode.ADDNEW) {
                            if (!isJSONSuccess(poAppController.OpenTransaction(tfPromo.getText()),
                                    "Initialize Open Transaction")) {

                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        }
                        poAppController = new PromoControllers(poApp, poLogWrapper).MCPromoSales();
                        poAppController.setTransactionStatus("10");
                        if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                            unloadForm appUnload = new unloadForm();
                            appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        }

                        Platform.runLater(() -> {

                            poAppController.setTransactionStatus("10");
                            poAppController.getMaster().setIndustryCode(psIndustryID);
                            poAppController.setIndustryID(psIndustryID);
                            poAppController.setCompanyID(psCompanyID);
                            poAppController.setCategoryID(psCategoryID);

                            clearAllInputs();
                        });
                        refreshChipBoxes();
                        pnEditMode = poAppController.getEditMode();
                        return;
                    }
                    break;
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", psFormName, null);
                        return;
                    }

                    try {
                        poAppController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", psFormName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                    }
                    break;

                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                    }
                    break;
                case "btnVoid":
                    if (tfPromo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null, "Promo Maintenance");
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to Void/Cancel transaction?") == true) {
                        if (btnVoid.getText().equals("Void")) {
                            if (!isJSONSuccess(poAppController.VoidTransaction(), "Initialize Void Transaction")) {
                                return;
                            }
                        } else {
                            if (!isJSONSuccess(poAppController.CancelTransaction(), "Initialize Cancel Transaction")) {
                                return;
                            }

                        }
                        reloadTableModel();
                        getLoadedTransaction();
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;
                case "btnDuplicate":
                    if (!isJSONSuccess(poAppController.DuplicateTransaction(), "Duplicate Transaction")) {
                        return;
                    }
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnSaveNew":
                    if (ShowMessageFX.YesNo(null, psFormName, "Save current changes as a new transaction?") != true) {
                        return;
                    }
                    if (!isJSONSuccess(poAppController.SaveAsNewTransaction(), "Save As New Transaction")) {
                        return;
                    }
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;
            }

            initButtonDisplay(poAppController.getEditMode());

        } catch (SQLException | GuanzonException | CloneNotSupportedException | JRException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }
    final ChangeListener<? super Boolean> dPicker_Focus = (o, ov, nv) -> {
        DatePicker loDatePicker = (DatePicker) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsDatePickerID = loDatePicker.getId();
        LocalDate loValue = loDatePicker.getValue();

        if (loValue == null) {
            return;
        }
        Date ldDateValue = Date.from(loValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!nv) {
            /*Lost Focus*/
            switch (lsDatePickerID) {
                case "dpTransactionDate":
                    poAppController.getMaster().setDate((ldDateValue));
                    return;
                case "dpPromoStart":
                    if (poAppController.getMaster().getThruDate().before(ldDateValue)) {
                        loadTransactionMaster();
                        return;
                    }
                    poAppController.getMaster().setFromDate((ldDateValue));
                    return;
                case "dpPromoEnd":
                    if (poAppController.getMaster().getFromDate().after(ldDateValue)) {
                        loadTransactionMaster();
                        return;
                    }
                    poAppController.getMaster().setThruDate((ldDateValue));
                    return;

            }
        }
    };

    private final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }
        Model loSelected;
        try {
            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {
                    case "taNoteGiveAway":

                        poAppController.getSalesPromotionGiveAway(pnGiveAway).setRemarks(lsValue);

                        loadSelectedTransactionGiveAway(pnGiveAway);
                        reloadTableGiveAway();
                        break;
                    case "taNoteModel":

                        loSelected = tblViewModel.getSelectionModel().getSelectedItem();
                        if (loSelected == null) {
                            return;
                        }
                        if (loSelected instanceof Model_Sales_Promotion_Model) {
                            Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loSelected;
                            if (loPromoModel.getModelID() == null || loPromoModel.getModelID().isEmpty()) {
                                ShowMessageFX.Information("Unable to set value! No Model Detected", psFormName, null);
                                loTextField.requestFocus();
                                return;
                            }
                            poAppController.getSalesPromotionModel(pnModel).setRemarks(lsValue);
                            loadSelectedTransactionModel(pnModel);
                        } else if (loSelected instanceof Model_Sales_Promotion_Brand) {
                            Model_Sales_Promotion_Brand loPromoModel = (Model_Sales_Promotion_Brand) loSelected;
                            if (loPromoModel.getBrandID() == null || loPromoModel.getBrandID().isEmpty()) {
                                ShowMessageFX.Information("Unable to set value! No Brand Detected", psFormName, null);
                                loTextField.requestFocus();
                                return;
                            }
                            poAppController.getSalesPromotionBrand(pnBrand).setRemarks(lsValue);
                            loadSelectedTransactionModel(pnBrand);
                        } else if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                            return;
                        }
                        reloadTableModel();
                        break;

                }
            } else {
                loTextField.selectAll();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        try {
            if (lsValue == null) {
                lsValue = "";
            }
            Model loSelected;

            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {
                    case "tfSupplier":
                        if (lsValue.isEmpty()) {
                            poAppController.getMaster().setClientID(lsValue);
                        }
                        break;
                    case "tfReferenceNo":
                        if (lsValue.isEmpty()) {
                            return;
                        }

                        poAppController.getMaster().setReferNo(lsValue);
                        loadTransactionMaster();

                        break;

                    case "tfSubject":
                        if (lsValue.isEmpty()) {
                            return;
                        }

                        poAppController.getMaster().setPromoDescription(lsValue);
                        loadTransactionMaster();

                        break;
                    case "tfRemarks":
                        if (lsValue.isEmpty()) {
                            return;
                        }

                        poAppController.getMaster().setRemarks(lsValue);
                        loadTransactionMaster();

                        break;

                    case "tfSRPFrom":
                         try {
                        if (lsValue.isEmpty()) {
                            ShowMessageFX.Information("Invalid SRP From amount", psFormName, null);
                            loTextField.requestFocus();
                            return;
                        }
                        if (tfSRPThru.getText() != null && !tfSRPThru.getText().isEmpty()) {
                            try {
                                double srpFrom = Double.parseDouble(tfSRPFrom.getText());
                                double srpThru = Double.parseDouble(tfSRPThru.getText());
                                if (srpFrom > srpThru) {
                                    ShowMessageFX.Information(
                                            "Invalid amount range. SRP From cannot be Higher than SRP Thru.",
                                            psFormName, null
                                    );
                                    tfSRPFrom.setText("0.0");
                                    poAppController.getMaster().setAmountFrom(0.0);
                                    return;
                                }

                                if (Double.parseDouble(tfSRPFrom.getText()) < 0) {
                                    ShowMessageFX.Information(
                                            "Invalid amount",
                                            psFormName, null
                                    );
                                    tfSRPFrom.setText("0.0");
                                    poAppController.getMaster().setAmountFrom(0.0);
                                    return;
                                }

                            } catch (NumberFormatException e) {
                                ShowMessageFX.Information(
                                        "Invalid input. Please enter a valid numeric SRP",
                                        psFormName, null
                                );
                                tfSRPFrom.setText("0.0");
                                poAppController.getMaster().setAmountFrom(0.0);
                                return;
                            }
                        } else {
                            tfSRPFrom.setText("0.0");
                            poAppController.getMaster().setAmountFrom(0.0);
                        }
                    } catch (NumberFormatException e) {
                        ShowMessageFX.Information(
                                "Invalid input. Please enter a valid numeric SRP",
                                psFormName, null
                        );
                        tfSRPFrom.setText("0.0");
                        poAppController.getMaster().setAmountTo(0.0);
                        return;
                    }
                    poAppController.getMaster().setAmountFrom(Double.parseDouble(lsValue));
                    loadTransactionMaster();

                    return;
                    case "tfSRPThru":
                        if (lsValue.isEmpty()) {
                            ShowMessageFX.Information("Invalid SRP Thru amount", psFormName, null);
                            loTextField.requestFocus();
                            return;
                        }
                        if (tfSRPThru.getText() != null && !tfSRPThru.getText().isEmpty()) {
                            try {
                                double srpFrom = Double.parseDouble(tfSRPFrom.getText());
                                double srpThru = Double.parseDouble(tfSRPThru.getText());
                                if (srpFrom > srpThru) {
                                    ShowMessageFX.Information(
                                            "Invalid amount range. SRP Thru cannot be lower that SRP From.",
                                            psFormName, null
                                    );
                                    tfSRPThru.setText(tfSRPFrom.getText());
                                    poAppController.getMaster().setAmountTo(srpFrom);
                                    return;
                                }
                                if (Double.parseDouble(tfSRPThru.getText()) < 0) {
                                    ShowMessageFX.Information(
                                            "Invalid amount",
                                            psFormName, null
                                    );
                                    tfSRPThru.setText(tfSRPFrom.getText());
                                    poAppController.getMaster().setAmountTo(srpFrom);
                                    return;
                                }

                            } catch (NumberFormatException e) {
                                ShowMessageFX.Information(
                                        "Invalid input. Please enter a valid numeric SRP",
                                        psFormName, null
                                );
                                tfSRPThru.setText("0.0");
                                poAppController.getMaster().setAmountTo(0.0);
                                return;
                            }
                        } else {
                            tfSRPThru.setText("0.0");
                            poAppController.getMaster().setAmountTo(0.0);
                        }
                        poAppController.getMaster().setAmountTo(Double.parseDouble(lsValue));
                        loadTransactionMaster();
                        break;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //Other Setter
                    case "tfQuantity":

                        int lnQuantity = parseOrZeroint(tfQuantity.getText());
                        if (lnQuantity < 0) {
                            ShowMessageFX.Information("Invalid amount", psFormName, null);
                            tfQuantity.setText("0.00");
                            lnQuantity = 0;
                        }
                        poAppController.getSalesPromotionGiveAway(pnGiveAway).setQuantity(lnQuantity);      // SRP
                        reloadTableGiveAway();
                        break;
                    case "tfIncentiveAmount":
                        loSelected = tblViewModel.getSelectionModel().getSelectedItem();
                        if (loSelected == null) {
                            return;
                        }
                        if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                            return;
                        }
                        if (loSelected instanceof Model_Sales_Promotion_Model) {
                            Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loSelected;
                            if (loPromoModel.getModelID() == null || loPromoModel.getModelID().isEmpty()) {
                                ShowMessageFX.Information("Unable to set value! No Model Detected", psFormName, null);
                                loTextField.requestFocus();
                                return;
                            }

                            double lnIncentiveAmount = parseOrZero(tfIncentiveAmount.getText());

                            if (lnIncentiveAmount < 0.0) {
                                ShowMessageFX.Information("Invalid amount", psFormName, null);
                                tfIncentiveAmount.setText("0.00");
                                lnIncentiveAmount = 0.0;
                            }

                            poAppController.getSalesPromotionModel(pnModel).setAmount(lnIncentiveAmount);

                        } else if (loSelected instanceof Model_Sales_Promotion_Brand) {
                            Model_Sales_Promotion_Brand loPromoModel = (Model_Sales_Promotion_Brand) loSelected;
                            if (loPromoModel.getBrandID() == null || loPromoModel.getBrandID().isEmpty()) {
                                ShowMessageFX.Information("Unable to set value! No Model Detected", psFormName, null);
                                loTextField.requestFocus();
                                return;
                            }
                            double lnIncentiveAmount = parseOrZero(tfIncentiveAmount.getText());

                            if (lnIncentiveAmount < 0.0) {
                                ShowMessageFX.Information("Invalid amount", psFormName, null);
                                tfIncentiveAmount.setText("0.00");
                                lnIncentiveAmount = 0.0;
                            }

                            poAppController.getSalesPromotionBrand(pnBrand).setAmount(lnIncentiveAmount);

                        }
                        reloadTableModel();
                        loadSelectedTransactionModel(pnRow);
                        break;
                    case "tfFreight":
                        loSelected = tblViewModel.getSelectionModel().getSelectedItem();
                        if (loSelected == null) {
                            return;
                        }
                        if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                            return;
                        }
                        if (loSelected instanceof Model_Sales_Promotion_Model) {
                            Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loSelected;
                            if (loPromoModel.getModelID() == null || loPromoModel.getModelID().isEmpty()) {
                                ShowMessageFX.Information("Unable to set value! No Model Detected", psFormName, null);
                                loTextField.requestFocus();
                                return;
                            }

                            double lnFeight = parseOrZero(tfFreight.getText());

                            if (lnFeight < 0.0) {
                                ShowMessageFX.Information("Invalid amount", psFormName, null);
                                tfFreight.setText("0.00");
                                lnFeight = 0.0;
                            }

                            poAppController.getSalesPromotionModel(pnModel).setFreight(lnFeight);

                        } else if (loSelected instanceof Model_Sales_Promotion_Brand) {
                            Model_Sales_Promotion_Brand loPromoModel = (Model_Sales_Promotion_Brand) loSelected;
                            if (loPromoModel.getBrandID() == null || loPromoModel.getBrandID().isEmpty()) {
                                ShowMessageFX.Information("Unable to set value! No Model Detected", psFormName, null);
                                loTextField.requestFocus();
                                return;
                            }
                            double lnFeight = parseOrZero(tfFreight.getText());

                            if (lnFeight < 0.0) {
                                ShowMessageFX.Information("Invalid amount", psFormName, null);
                                tfFreight.setText("0.00");
                                lnFeight = 0.0;
                            }

                            poAppController.getSalesPromotionBrand(pnBrand).setFreight(lnFeight);

                        }
                        reloadTableModel();
                        loadSelectedTransactionModel(pnRow);
                        break;
                    case "tfSRP":
                    case "tfDiscRate":
                    case "tfDiscAmount":
                        loSelected = tblViewModel.getSelectionModel().getSelectedItem();
                        if (loSelected == null) {
                            return;
                        }
                        if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                            return;
                        }
                        if (loSelected instanceof Model_Sales_Promotion_Model) {
                            Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loSelected;
                            if (loPromoModel.getModelID() == null || loPromoModel.getModelID().isEmpty()) {
                                ShowMessageFX.Information("Unable to set value! No Model Detected", psFormName, null);
                                loTextField.requestFocus();
                                return;
                            }

                            double lnSRP = parseOrZero(tfSRP.getText());
                            double lnDiscRate = parseOrZero(tfDiscRate.getText());
                            double lnDiscAmount = parseOrZero(tfDiscAmount.getText());

                            if (lnSRP < 0.0 || lnDiscRate < 0.0 || lnDiscAmount < 0.0) {
                                ShowMessageFX.Information("Invalid amount", psFormName, null);
                                tfSRP.setText("0.00");
                                tfDiscRate.setText("0.00");
                                tfDiscAmount.setText("0.00");
                                lnSRP = 0.0;
                                lnDiscRate = 0.0;
                                lnDiscAmount = 0.0;
                            }
                            if (lnDiscRate > 99.99) {
                                ShowMessageFX.Information("Discount rate cannot exceed 99.99%", psFormName, null);
                                lnDiscRate = 99.99;
                                tfDiscRate.setText("99.99");
                            }

                            poAppController.getSalesPromotionModel(pnModel).setTotalAmount(lnSRP);      // SRP
                            poAppController.getSalesPromotionModel(pnModel).setDiscountRate(lnDiscRate);
                            poAppController.getSalesPromotionModel(pnModel).setDiscAmount(lnDiscAmount);

                            double lnNetSRP = computeNetSRP(lnSRP, lnDiscRate, lnDiscAmount);
                            tfNetSRP.setText(CommonUtils.NumberFormat(lnNetSRP, "###,###,##0.00"));
                            // If Model_Sales_Promotion_Model exposes a Net SRP setter, persist it too:
                            // loPromoModel.setNetSRP(String.valueOf(lnNetSRP));
                        } else if (loSelected instanceof Model_Sales_Promotion_Brand) {
                            Model_Sales_Promotion_Brand loPromoModel = (Model_Sales_Promotion_Brand) loSelected;
                            if (loPromoModel.getBrandID() == null || loPromoModel.getBrandID().isEmpty()) {
                                ShowMessageFX.Information("Unable to set value! No Model Detected", psFormName, null);
                                loTextField.requestFocus();
                                return;
                            }

                            double lnSRP = parseOrZero(tfSRP.getText());
                            double lnDiscRate = parseOrZero(tfDiscRate.getText());
                            double lnDiscAmount = parseOrZero(tfDiscAmount.getText());

                            if (lnSRP < 0.0 || lnDiscRate < 0.0 || lnDiscAmount < 0.0) {
                                ShowMessageFX.Information("Invalid amount", psFormName, null);
                                tfSRP.setText("0.00");
                                tfDiscRate.setText("0.00");
                                tfDiscAmount.setText("0.00");
                                lnSRP = 0.0;
                                lnDiscRate = 0.0;
                                lnDiscAmount = 0.0;
                            }
                            if (lnDiscRate > 99.99) {
                                ShowMessageFX.Information("Discount rate cannot exceed 99.99%", psFormName, null);
                                lnDiscRate = 99.99;
                                tfDiscRate.setText("99.99");
                            }

                            poAppController.getSalesPromotionBrand(pnBrand).setTotalAmount(lnSRP);      // SRP
                            poAppController.getSalesPromotionBrand(pnBrand).setDiscountRate(lnDiscRate);
                            poAppController.getSalesPromotionBrand(pnBrand).setDiscAmount(lnDiscAmount);

                            double lnNetSRP = computeNetSRP(lnSRP, lnDiscRate, lnDiscAmount);
                            tfNetSRP.setText(CommonUtils.NumberFormat(lnNetSRP, "###,###,##0.00"));
                            // If Model_Sales_Promotion_Model exposes a Net SRP setter, persist it too:
                            // loPromoModel.setNetSRP(String.valueOf(lnNetSRP));
                        }
                        reloadTableModel();
                        loadSelectedTransactionModel(pnRow);
                        break;

                }
            } else {
                loTextField.selectAll();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        if (lsValue == null) {
            lsValue = "";
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:

                        switch (txtFieldID) {
                            default:
                                CommonUtils.SetNextFocus(loTxtField);
                                break;
                            case "tfSearchPromoID":
                                if (!isJSONSuccess(poAppController.searchTransaction(lsValue, true, true),
                                        "Initialize Search Source No! ")) {
                                    return;
                                }

//                                tfSearchSourceno.setText(poAppController.getMaster().Branch().getBranchName());
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchDescription":
                                if (!isJSONSuccess(poAppController.searchTransaction(lsValue, true, true),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }

//                                tfSearchTransNo.setText(poAppController.getMaster().getTransactionNo());
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSupplier":
                                if (!isJSONSuccess(poAppController.searchPromotionBySupplier(lsValue, true),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }

//                                tfSearchTransNo.setText(poAppController.getMaster().getTransactionNo());
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;

                            ////////////////////////////////////////////////////////////////////////////////////////////////////
                            //Search or Replace ChipBox
                            case "tfBrand":
                            case "tfModel":
                                Model loSelected = tblViewModel.getSelectionModel().getSelectedItem();
                                if (loSelected == null) {
                                    if (txtFieldID.equals(tfBrand)) {
                                        if (!isJSONSuccess(poAppController.searchPromotionByBrand(-1, (tfBrand.getText() == null ? "" : tfBrand.getText()), false),
                                                "Initialize Search Brand! ")) {
                                            return;
                                        }
                                    } else {
                                        if (!isJSONSuccess(poAppController.searchPromotionByModel(-1, (tfModel.getText() == null ? "" : tfModel.getText()), false),
                                                "Initialize Search Model! ")) {
                                            return;
                                        }
                                    }
                                }
                                if (loSelected instanceof Model_Sales_Promotion_Brand) {
                                    Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loSelected;
                                    if (!isJSONSuccess(poAppController.searchPromotionByBrand(pnBrand, tfBrand.getText(), false),
                                            "Initialize Search Brand! ")) {
                                        return;
                                    }
                                } else if (loSelected instanceof Model_Sales_Promotion_Model) {
                                    if (!isJSONSuccess(poAppController.searchPromotionByModel(pnModel, tfModel.getText(), false),
                                            "Initialize Search Model! ")) {
                                        return;
                                    }
                                } else if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                                    if (!isJSONSuccess(poAppController.searchPromotionByModelException(pnModelException, tfModel.getText(), false),
                                            "Initialize Search Model! ")) {
                                        return;
                                    }
                                }
                                refreshChipBoxes();
                                break;

                            case "tfBarcode":
                                if (!isJSONSuccess(poAppController.searchPromotionByGiveAway(pnGiveAway, (tfBarcode.getText() == null ? "" : tfBarcode.getText()), true),
                                        "Initialize Search Give Away! ")) {
                                    return;
                                }
                                refreshChipBoxes();
                                break;
                            case "tfDescription":
                                if (!isJSONSuccess(poAppController.searchPromotionByGiveAway(pnGiveAway, (tfDescription.getText() == null ? "" : tfDescription.getText()), false),
                                        "Initialize Search Give Away! ")) {
                                    return;
                                }

                                refreshChipBoxes();
                                break;

                        }
                }
            }
        } catch (Exception ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private void loadComboBoxList() {
        List<String> promoTypes = PromoType.PromoType;
        cbPromoType.setItems(FXCollections.observableArrayList(promoTypes));

        List<String> promoSource = PromoSource.PromoSource;
        cbPromoSource.setItems(FXCollections.observableArrayList(promoSource));

        List<String> transactionType = TransactionType.TransactionType;
        cbTransactionType.setItems(FXCollections.observableArrayList(transactionType));
    }

    private void loadTransactionMaster() {
        try {
            lblSource.setText((poAppController.getMaster().Industry().getDescription() == null ? "" : poAppController.getMaster().Industry().getDescription()));
            lblStatus.setText(SalesPromotionStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())) == null ? "STATUS"
                    : SalesPromotionStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));

            lblSource.setText(poAppController.getMaster().getPromoID());
            tfPromo.setText(poAppController.getMaster().getPromoID());
            dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getDate()));
            dpPromoStart.setValue(ParseDate(poAppController.getMaster().getFromDate()));
            dpPromoEnd.setValue(ParseDate(poAppController.getMaster().getThruDate()));
            tfSRPFrom.setText(String.valueOf(poAppController.getMaster().getAmountFrom()));
            tfSRPThru.setText(String.valueOf(poAppController.getMaster().getAmountTo()));
            tfReferenceNo.setText(String.valueOf(poAppController.getMaster().getReferNo()));
            tfSupplier.setText(poAppController.getMaster().ClientMaster().getCompanyName());
            tfSubject.setText(poAppController.getMaster().getPromoDescription());
            tfRemarks.setText(poAppController.getMaster().getRemarks());

            cbPromoSource.getSelectionModel().select(Integer.parseInt(poAppController.getMaster().getPromoSource()));
            cbPromoType.getSelectionModel().select(Integer.parseInt(poAppController.getMaster().getPromoType()));
            cbTransactionType.getSelectionModel().select(Integer.parseInt(poAppController.getMaster().getTransactionType()));
            if (poAppController.getMaster().getTransactionStatus().equals(SalesPromotionStatus.CONFIRMED)) {
                btnVoid.setText("Cancel");
            } else {
                btnVoid.setText("Void");
            }

            if (tfPromo.getText() == null || tfPromo.getText().trim().isEmpty()) {
                lblStatus.setText("UNKNOWN");
            }
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());

        }
    }

    private void loadSelectedTransactionModel(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        int tblIndex = fnRow - 1;
        tfBrand.setText(tblColModelbrand.getCellData(tblIndex));
        tfModel.setText(tblColModelmodel.getCellData(tblIndex));
        tfSRP.setText(tblColModelSRP.getCellData(tblIndex));
        tfDiscRate.setText(tblColModeldiscRate.getCellData(tblIndex));
        tfDiscAmount.setText(tblColModeldiscAmt.getCellData(tblIndex));
        tfNetSRP.setText(tblColModelnetSRP.getCellData(tblIndex));
        tfFreight.setText(tblColModelfreight.getCellData(tblIndex));
        taNoteModel.setText(tblColModelNote.getCellData(tblIndex));
        tfIncentiveAmount.setText(tblColModelincentiveAmount.getCellData(tblIndex));
        // --- Load radio toggles from the actual model object ---
        pbSuppressToggleListener = true;
        try {
            if (tblIndex >= 0 && tblIndex < paCombined.size()) {
                Model loSelected = paCombined.get(tblIndex);

                if (loSelected instanceof Model_Sales_Promotion_Brand) {
                    Model_Sales_Promotion_Brand loBrand = (Model_Sales_Promotion_Brand) loSelected;
                    rbInsurance1.setSelected(loBrand.isInsuranceFree());
                    rbInsurance0.setSelected(!loBrand.isInsuranceFree());
                    rbRegistration1.setSelected(loBrand.isRegistrationFree());
                    rbRegistration0.setSelected(!loBrand.isRegistrationFree());
                    rbIncentive1.setSelected(loBrand.isWithIncentive());
                    rbIncentive0.setSelected(!loBrand.isWithIncentive());

                } else if (loSelected instanceof Model_Sales_Promotion_Model) {
                    Model_Sales_Promotion_Model loModel = (Model_Sales_Promotion_Model) loSelected;
                    rbInsurance1.setSelected(loModel.isInsuranceFree());
                    rbInsurance0.setSelected(!loModel.isInsuranceFree());
                    rbRegistration1.setSelected(loModel.isRegistrationFree());
                    rbRegistration0.setSelected(!loModel.isRegistrationFree());
                    rbIncentive1.setSelected(loModel.isWithIncentive());
                    rbIncentive0.setSelected(!loModel.isWithIncentive());

                } else if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                    // Exceptions don't carry these flags — default to "No" and lock them
                    rbInsurance0.setSelected(true);
                    rbRegistration0.setSelected(true);
                    rbIncentive0.setSelected(true);
                }
            }

            tfIncentiveAmount.setDisable(rbIncentive0.isSelected());
        } finally {
            pbSuppressToggleListener = false;
        }

    }

    private void loadSelectedTransactionGiveAway(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {
        int tblIndex = fnRow - 1;
        tfBarcode.setText(tblColGiveAwayBarcode.getCellData(tblIndex));
        tfDescription.setText(tblColGiveAwayDescription.getCellData(tblIndex));
        tfQuantity.setText(tblColGiveAwayQuality.getCellData(tblIndex));
        taNoteGiveAway.setText(tblColGiveAwayNotes.getCellData(tblIndex));

    }

    private void initControlEvents() {

        List<Control> laControls = getAllSupportedControls();
        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TextArea) {
                TextArea loControlField = (TextArea) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(txtArea_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof DatePicker) {
                DatePicker loControlField = (DatePicker) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(dPicker_Focus);
            }
        }

        cbPromoType.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (poAppController.getEditMode() == EditMode.ADDNEW
                        || poAppController.getEditMode() == EditMode.UPDATE) {
                    poAppController.getMaster().setPromoType(String.valueOf(newVal.intValue()));
                }
            }
        });

        cbPromoSource.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (poAppController.getEditMode() == EditMode.ADDNEW
                        || poAppController.getEditMode() == EditMode.UPDATE) {
                    poAppController.getMaster().setPromoSource(String.valueOf(newVal.intValue()));
                }
            }
        });

        cbTransactionType.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (poAppController.getEditMode() == EditMode.ADDNEW
                        || poAppController.getEditMode() == EditMode.UPDATE) {
                    poAppController.getMaster().setTransactionType(String.valueOf(newVal.intValue()));
                }
            }
        });

        clearAllInputs();
    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
    }

    private void clearAllInputs() {

        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl != null && (loControl instanceof TableView)) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }
            } else if (loControl != null && (loControl instanceof ComboBox)) {
                ComboBox cbox = (ComboBox) loControl;
                if (cbox.getItems() != null) {
                    cbox.getItems().clear();
                }
            }
        }
        pnEditMode = poAppController.getEditMode();
        loadComboBoxList();

        initButtonDisplay(poAppController.getEditMode());
        if (tfPromo.getText().trim().isEmpty()) {
            lblStatus.setText("UNKNOWN");
        }
    }

    private void clearPromoDetail() {
        tfBrand.clear();
        tfModel.clear();
        tfSRP.setText("0.00");
        tfDiscRate.setText("0.00");
        tfDiscAmount.setText("0.00");
        tfNetSRP.clear();
        tfFreight.clear();
        taNoteModel.clear();
        pbSuppressToggleListener = true;
        try {
            rbInsurance0.setSelected(true);
            rbRegistration0.setSelected(true);
            rbIncentive0.setSelected(true);
        } finally {
            pbSuppressToggleListener = false;
        }
        tfIncentiveAmount.setText("0.00");
    }

    private void clearPromoGiveAway() {
        tfBarcode.clear();
        tfDescription.clear();
        tfQuantity.setText("0.00");
        taNoteGiveAway.clear();
    }

    private void initButtonDisplay(int fnEditMode) {

        boolean lbEditing = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        String lsTransNo = tfPromo.getText();
        boolean lbHasTransaction = lsTransNo != null && !lsTransNo.isEmpty();
        boolean lbIsApproved = lbHasTransaction
                && "1".equals(poAppController.getMaster().getTransactionStatus());
        String lsStatus = lbHasTransaction ? poAppController.getMaster().getTransactionStatus() : "";
        boolean lbRestrictedStatus = "2".equals(lsStatus) || "3".equals(lsStatus) || "4".equals(lsStatus);

        // Always visible
        initButtonControls(true, "btnClose");

        // Editing mode buttons
        initButtonControls(lbEditing, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(false, "btnSaveNew");
        if (fnEditMode == EditMode.UPDATE) {
            initButtonControls(fnEditMode == EditMode.UPDATE, "btnSearch", "btnSaveNew", "btnSave", "btnCancel");
        }
        initButtonControls(!lbEditing, "btnBrowse", "btnNew");

        // Transaction-dependent buttons (only when not editing)
        initButtonControls(!lbEditing && lbHasTransaction, "btnUpdate", "btnVoid", "btnPreview", "btnDuplicate");
        initButtonControls(!lbEditing && lbHasTransaction && !lbIsApproved, "btnUpdate");
        initButtonControls(!lbEditing && lbHasTransaction && !lbRestrictedStatus, "btnUpdate", "btnVoid");

        // Disable panes during editing
        apMaster.setDisable(!lbEditing);
        apOtherInfo.setDisable(!lbEditing);
        hbArea.setDisable(!lbEditing);
        hbBrand.setDisable(!lbEditing);
        hbProvince.setDisable(!lbEditing);
        hbModel.setDisable(!lbEditing);
        hbModelException.setDisable(!lbEditing);
        hbGiveAway.setDisable(!lbEditing);

    }

    private void initButtonControls(boolean visible, String... buttonFxIdsToShow) {
        Set<String> showOnly = new HashSet<>(Arrays.asList(buttonFxIdsToShow));

        for (Field loField : getClass().getDeclaredFields()) {
            loField.setAccessible(true);
            String fieldName = loField.getName(); // fx:id

            // Only touch the buttons listed
            if (!showOnly.contains(fieldName)) {
                continue;
            }
            try {
                Object value = loField.get(this);
                if (value instanceof Button) {
                    Button loButton = (Button) value;
                    loButton.setVisible(visible);
                    loButton.setManaged(visible);
                }
            } catch (IllegalAccessException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
                ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

                poLogWrapper.severe(psFormName + " :" + e.getMessage());
                ;
            }
        }
    }

    private void initializeTableModel() {
        if (paBrand == null) {
            paBrand = FXCollections.observableArrayList();
        }
        if (paModel == null) {
            paModel = FXCollections.observableArrayList();
        }

        if (paModelException == null) {
            paModelException = FXCollections.observableArrayList();
        }
        if (paCombined == null) {
            paCombined = FXCollections.observableArrayList();
        }
        paCombined.addAll(paBrand);
        paCombined.addAll(paModel);
        paCombined.addAll(paModelException);

        tblViewModel.setItems(paCombined);

        tblColModelSRP.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
        tblColModeldiscRate.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
        tblColModeldiscAmt.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
        tblColModelnetSRP.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
        tblColModelfreight.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
        tblColModelincentiveAmount.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

        tblColModelAction.setStyle("-fx-alignment: CENTER;");
//        tblColDetailNo.setCellValueFactory((loModel) -> {
//            int index = tblViewDetails.getItems().indexOf(loModel.getValue()) + 1;
//            return new SimpleStringProperty(String.valueOf(index));
//        });
        tblColModelbrand.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoModel = (Model_Sales_Promotion_Brand) loModel;
                try {
                    value = loPromoModel.Brand().getDescription();
                } catch (SQLException | GuanzonException ex) {
                    value = "";
                    Logger.getLogger(PromoMaintenanceEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                try {
                    value = loPromoModel.Model().Brand().getDescription();
                } catch (SQLException | GuanzonException ex) {
                    value = "";
                    Logger.getLogger(PromoMaintenanceEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                Model_Sales_Promotion_Model_Exception loException = (Model_Sales_Promotion_Model_Exception) loModel;
                try {
                    value = loException.Model().Brand().getDescription();
                } catch (SQLException | GuanzonException ex) {
                    value = "";
                    Logger.getLogger(PromoMaintenanceEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return new SimpleStringProperty(value);
        });

        tblColModelmodel.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";

            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                try {
                    value = "All " + loPromoBrand.Brand().getDescription();
                } catch (SQLException | GuanzonException ex) {
                    value = "";
                    Logger.getLogger(PromoMaintenanceEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                try {
                    value = loPromoModel.Model().getDescription();
                } catch (SQLException | GuanzonException ex) {
                    value = "";
                    Logger.getLogger(PromoMaintenanceEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                Model_Sales_Promotion_Model_Exception loException = (Model_Sales_Promotion_Model_Exception) loModel;
                try {
                    value = loException.Model().getDescription();
                } catch (SQLException | GuanzonException ex) {
                    value = "";
                    Logger.getLogger(PromoMaintenanceEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return new SimpleStringProperty(value);
        });

        tblColModelSRP.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                value = CommonUtils.NumberFormat(loPromoBrand.getTotalAmount(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                value = CommonUtils.NumberFormat(loPromoModel.getTotalAmount(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                value = "";
            }

            return new SimpleStringProperty(value);
        });

        tblColModeldiscRate.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                value = CommonUtils.NumberFormat(loPromoBrand.getDiscountRate(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                value = CommonUtils.NumberFormat(loPromoModel.getDiscountRate(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                value = "";
            }

            return new SimpleStringProperty(value);
        });

        tblColModeldiscAmt.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                value = CommonUtils.NumberFormat(loPromoBrand.getDiscAmount(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                value = CommonUtils.NumberFormat(loPromoModel.getDiscAmount(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                value = "";
            }

            return new SimpleStringProperty(value);
        });

        tblColModelnetSRP.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoModel = (Model_Sales_Promotion_Brand) loModel;
                double lnNetSRP = computeNetSRP(loPromoModel.getTotalAmount(), loPromoModel.getDiscountRate(), loPromoModel.getDiscAmount());
                value = CommonUtils.NumberFormat(lnNetSRP, "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                double lnNetSRP = computeNetSRP(loPromoModel.getTotalAmount(), loPromoModel.getDiscountRate(), loPromoModel.getDiscAmount());
                value = CommonUtils.NumberFormat(lnNetSRP, "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                value = "";
            }

            return new SimpleStringProperty(value);
        });

        tblColModelRegistration.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                value = loPromoBrand.isRegistrationFree() == true ? "YES" : "NO";

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                value = loPromoModel.isRegistrationFree() == true ? "YES" : "NO";

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                value = "";
            }

            return new SimpleStringProperty(value);
        });

        tblColModelinsurance.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                value = loPromoBrand.isInsuranceFree() == true ? "YES" : "NO";

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                value = loPromoModel.isInsuranceFree() == true ? "YES" : "NO";

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                value = "";
            }

            return new SimpleStringProperty(value);
        });

        tblColModelfreight.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                value = CommonUtils.NumberFormat(loPromoBrand.getFreight(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                value = CommonUtils.NumberFormat(loPromoModel.getFreight(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                value = "";
            }

            return new SimpleStringProperty(value);
        });

        tblColModelincentiveAmount.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                value = CommonUtils.NumberFormat(loPromoBrand.getAmount(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                value = CommonUtils.NumberFormat(loPromoModel.getAmount(), "###,###,##0.00");

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                value = "";
            }

            return new SimpleStringProperty(value);
        });

        tblColModelNote.setCellValueFactory((loCell) -> {
            Model loModel = loCell.getValue();
            String value = "";
            if (loModel instanceof Model_Sales_Promotion_Brand) {
                Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loModel;
                value = loPromoBrand.getRemarks();

            } else if (loModel instanceof Model_Sales_Promotion_Model) {
                Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loModel;
                value = loPromoModel.getRemarks();

            } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
//                Model_Sales_Promotion_Model_Exception loPromoModelException = (Model_Sales_Promotion_Model_Exception) loModel;
                value = "";

            }

            return new SimpleStringProperty(value);
        });

        tblColModelAction.setCellValueFactory((loCell) -> new SimpleStringProperty(""));

        tblColModelAction.setCellFactory((loColumn) -> {
            return new TableCell<Model, String>() {

                @Override
                protected void updateItem(String value, boolean empty) {
                    super.updateItem(value, empty);

                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        return;
                    }

                    Model loModel = (Model) getTableRow().getItem();
                    HBox hbActions = new HBox(5);

                    if (loModel instanceof Model_Sales_Promotion_Brand) {
                        Model_Sales_Promotion_Brand loBrand = (Model_Sales_Promotion_Brand) loModel;
                        Button btnStatus = createStatusActionButton(loModel,
                                () -> {
                                    poAppController.removeSalesPromotionByBrand(loBrand.getEntryNo());
                                    try {
                                        refreshChipBoxes();
                                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                    }
                                },
                                (newState) -> {
                                    loBrand.isWithActive(newState);
                                    reloadTableModel();
                                    try {
                                        refreshChipBoxes();
                                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                    }

                                });
                        hbActions.getChildren().add(btnStatus);

                    } else if (loModel instanceof Model_Sales_Promotion_Model) {
                        Model_Sales_Promotion_Model loModelItem = (Model_Sales_Promotion_Model) loModel;
                        Button btnStatus = createStatusActionButton(loModel,
                                () -> {
                                    poAppController.removeSalesPromotionByModel(loModelItem.getEntryNo());
                                    try {
                                        refreshChipBoxes();
                                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                    }
                                },
                                (newState) -> {
                                    loModelItem.isWithActive(newState);
                                    reloadTableModel();
                                });
                        hbActions.getChildren().add(btnStatus);

                    } else if (loModel instanceof Model_Sales_Promotion_Model_Exception) {
                        Model_Sales_Promotion_Model_Exception loException = (Model_Sales_Promotion_Model_Exception) loModel;
                        Button btnStatus = createStatusActionButton(loModel,
                                () -> {
                                    poAppController.removeSalesPromotionByModelException(loException.getEntryNo());
                                    try {
                                        refreshChipBoxes();
                                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                    }
                                },
                                (newState) -> {
                                    loException.isWithActive(newState);
                                    reloadTableModel();
                                    try {
                                        refreshChipBoxes();
                                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                    }
                                });
                        hbActions.getChildren().add(btnStatus);
                    }

                    setGraphic(hbActions);
                }
            };
        });

        tblViewModel.setRowFactory((tv) -> {
            return new javafx.scene.control.TableRow<Model>() {
                @Override
                protected void updateItem(Model item, boolean empty) {
                    super.updateItem(item, empty);
                    setStyle("");
                    if (empty || item == null) {
                        return;
                    }
                    if (item instanceof Model_Sales_Promotion_Model_Exception) {
                        setStyle("-fx-background-color: #FFE0E0;");
                    } else if (item instanceof Model_Sales_Promotion_Brand
                            && !((Model_Sales_Promotion_Brand) item).isWithActive()) {
                        setStyle("-fx-background-color: #FFC0CB;");
                    } else if (item instanceof Model_Sales_Promotion_Model
                            && !((Model_Sales_Promotion_Model) item).isWithActive()) {
                        setStyle("-fx-background-color: #FFC0CB;");
                    }
                }
            };
        });

    }

    private Button createStatusActionButton(
            Model item,
            Runnable removeAction,
            java.util.function.Consumer<Boolean> toggleActiveAction) {

        Button btn = new Button();
        boolean lbTransactionIsNew = (poAppController.getEditMode() == EditMode.ADDNEW);
        boolean lbItemIsNew = lbTransactionIsNew || (item.getEditMode() == EditMode.ADDNEW);

        de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView icon = new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView();
        boolean lbActive = lbItemIsNew || (item instanceof Model_Sales_Promotion_Brand
                ? ((Model_Sales_Promotion_Brand) item).isWithActive()
                : item instanceof Model_Sales_Promotion_Model
                        ? ((Model_Sales_Promotion_Model) item).isWithActive()
                        : item instanceof Model_Sales_Promotion_Model_Exception
                                ? ((Model_Sales_Promotion_Model_Exception) item).isWithActive()
                                : item instanceof Model_Sales_Promotion_GiveAway_Item
                                        ? ((Model_Sales_Promotion_GiveAway_Item) item).isWithActive()
                                        : true);

        icon.setGlyphName(lbItemIsNew ? "TRASH" : (lbActive ? "CHECK" : "CLOSE"));
        btn.setGraphic(icon);

        // Disable action entirely when the transaction is not in an editable state
        boolean lbEditing = (poAppController.getEditMode() == EditMode.ADDNEW
                || poAppController.getEditMode() == EditMode.UPDATE);
        btn.setDisable(!lbEditing);

        btn.setOnAction(e -> {
            if (lbItemIsNew) {
                removeAction.run();
            } else {
                toggleActiveAction.accept(!lbActive);
            }
        });

        return btn;
    }

    private void initializeTableGiveAway() {
        if (paGiveAway == null) {
            paGiveAway = FXCollections.observableArrayList();
        }
        tblViewGiveAway.setItems(paGiveAway);
        tblColGiveAwayAction.setStyle("-fx-alignment: CENTER;");

        tblColGiveAwayQuality.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

        tblColGiveAwayBarcode.setCellValueFactory(loModel -> {
            try {
                String desc = loModel.getValue().Inventory().getBarCode();
                return new SimpleStringProperty(desc != null ? desc : "");

            } catch (SQLException | GuanzonException ex) {
                poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                return new SimpleStringProperty("");
            }
        });

        tblColGiveAwayDescription.setCellValueFactory(loModel -> {
            try {
                String desc = loModel.getValue().Inventory().getDescription();
                return new SimpleStringProperty(desc != null ? desc : "");

            } catch (SQLException | GuanzonException ex) {
                poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                return new SimpleStringProperty("");
            }
        });

        tblColGiveAwayQuality.setCellValueFactory(loModel -> {
            String qty = String.valueOf(loModel.getValue().getQuantity());
            return new SimpleStringProperty(qty != null ? qty : "");
        });

        tblColGiveAwayNotes.setCellValueFactory(loModel -> {
            String remarks = loModel.getValue().getRemarks();
            return new SimpleStringProperty(remarks != null ? remarks : "");
        });

        tblColGiveAwayAction.setCellValueFactory((loCell) -> new SimpleStringProperty(""));

        tblColGiveAwayAction.setCellFactory((loColumn) -> {
            return new TableCell<Model_Sales_Promotion_GiveAway_Item, String>() {

                @Override
                protected void updateItem(String value, boolean empty) {
                    super.updateItem(value, empty);

                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        return;
                    }

                    Model_Sales_Promotion_GiveAway_Item loGiveAway = (Model_Sales_Promotion_GiveAway_Item) getTableRow().getItem();
                    HBox hbActions = new HBox(5);

                    Button btnStatus = createStatusActionButton(loGiveAway,
                            () -> {
                                poAppController.removeSalesPromotionByGiveAway(loGiveAway.getEntryNo());
                                try {
                                    refreshChipBoxes();
                                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                }
                            },
                            (newState) -> {
                                loGiveAway.isWithActive(newState);
                                reloadTableGiveAway();
                                try {
                                    refreshChipBoxes();
                                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                }
                            });
                    hbActions.getChildren().add(btnStatus);

                    setGraphic(hbActions);
                }
            };
        });

        tblViewGiveAway.setRowFactory((tv) -> {
            return new javafx.scene.control.TableRow<Model_Sales_Promotion_GiveAway_Item>() {
                @Override
                protected void updateItem(Model_Sales_Promotion_GiveAway_Item item, boolean empty) {
                    super.updateItem(item, empty);
                    setStyle("");
                    if (empty || item == null) {
                        return;
                    }
                    if (!item.isWithActive()) {
                        setStyle("-fx-background-color: #FFC0CB;");
                    }
                }
            };
        });

    }

    private void reloadTableModel() {
        List<Model_Sales_Promotion_Brand> rawBrand = poAppController.getSalesPromotionBrandList();
        List<Model_Sales_Promotion_Model> rawModel = poAppController.getSalesPromotionModelList();
        List<Model_Sales_Promotion_Model_Exception> rawModelException = poAppController.getSalesPromotionModelExceptionList();

        // merge all four lists into one ObservableList<Model>
        paBrand.setAll(rawBrand);
        paModel.setAll(rawModel);
        paModelException.setAll(rawModelException);
        paCombined.clear();
        paCombined.addAll(paBrand);
        paCombined.addAll(paModel);
        paCombined.addAll(paModelException);

        // Restore or select last row
        int indexToSelect = (pnRow >= 1 && pnRow < paCombined.size())
                ? pnRow - 1
                : paCombined.size() - 1;

        tblViewModel.getSelectionModel().select(indexToSelect);
        Model loSelected = tblViewModel.getSelectionModel().getSelectedItem();
        if (loSelected == null) {
            return;
        }
        if (loSelected instanceof Model_Sales_Promotion_Brand) {
            Model_Sales_Promotion_Brand loPromoBrand = (Model_Sales_Promotion_Brand) loSelected;
            pnBrand = paBrand.indexOf(loPromoBrand) + 1;       // 1-based index in paModel
            pnModelException = -1;
            pnModel = -1;   // not applicable for this row
        } else if (loSelected instanceof Model_Sales_Promotion_Model) {
            Model_Sales_Promotion_Model loPromoModel = (Model_Sales_Promotion_Model) loSelected;
            pnModel = paModel.indexOf(loPromoModel) + 1;       // 1-based index in paModel
            pnModelException = -1;
            pnBrand = -1;// not applicable for this row
        } else if (loSelected instanceof Model_Sales_Promotion_Model_Exception) {
            Model_Sales_Promotion_Model_Exception loException = (Model_Sales_Promotion_Model_Exception) loSelected;
            pnModelException = paModelException.indexOf(loException) + 1; // 1-based index in paModelException
            pnModel = -1;
            pnBrand = -1;// not applicable for this row
        }
        pnRow = tblViewModel.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        if (pnRow <= 0) {
            if (paCombined.size() > 0) {
                pnRow = 1;
            } else {
                pnModel = -1;
                pnBrand = -1;
                pnModelException = -1;
                clearPromoDetail();
            }
        }
        tblViewModel.refresh();
    }

    private void reloadTableGiveAway() {
        List<Model_Sales_Promotion_GiveAway_Item> rawGiveAway = poAppController.getSalesPromotionGiveAwayItemList();
        paGiveAway.setAll(rawGiveAway);

        // Restore or select last row
        int indexToSelect = (pnGiveAway >= 1 && pnGiveAway < paGiveAway.size())
                ? pnGiveAway - 1
                : paGiveAway.size() - 1;

        tblViewGiveAway.getSelectionModel().select(indexToSelect);

        pnGiveAway = tblViewGiveAway.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        if (pnGiveAway <= 0) {
            if (paGiveAway.size() > 0) {
                pnGiveAway = 1;
            } else {
                clearPromoGiveAway();
            }
        }
        tblViewGiveAway.refresh();
    }

    private void getLoadedTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        loadTransactionMaster();
        reloadTableModel();
        reloadTableGiveAway();
        loadSelectedTransactionModel(pnRow);
        loadSelectedTransactionGiveAway(pnGiveAway);
        refreshChipBoxes();
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            if (message != null) {
                poLogWrapper.severe(psFormName + " :" + message);
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, message));
                }
            }
            return false;
        }

        String message = (String) loJSON.get("message");
        poLogWrapper.severe(psFormName + " :" + message);
        if (message != null) {
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Information(null, psFormName, message);
            } else {
                Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, message));
            }
        }

        poLogWrapper.info(psFormName + " : Success on " + fsModule);
        return true;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
    }

    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
                ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }

    private void initializeChipInputs() {
        if (paProvince == null) {
            paProvince = FXCollections.observableArrayList();
        }

        if (paBranchArea == null) {
            paBranchArea = FXCollections.observableArrayList();
        }
        attachChipInput(hbShopType, (tf) -> setupChipKeyHandler(tf, hbShopType, "shopType"));
        attachChipInput(hbBrand, (tf) -> setupChipKeyHandler(tf, hbBrand, "brand"));
        attachChipInput(hbModel, (tf) -> setupChipKeyHandler(tf, hbModel, "model"));
        attachChipInput(hbModelException, (tf) -> setupChipKeyHandler(tf, hbModelException, "modelException"));
        attachChipInput(hbProvince, (tf) -> setupChipKeyHandler(tf, hbProvince, "province"));
        attachChipInput(hbArea, (tf) -> setupChipKeyHandler(tf, hbArea, "area"));
        attachChipInput(hbGiveAway, (tf) -> setupChipKeyHandler(tf, hbGiveAway, "giveaway"));
    }

    private TextField createChipTextField() {
        TextField tf = new TextField();
        tf.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0 5 0 5;");
        tf.setPromptText("Press F3: Search/Add");
        tf.setMaxHeight(Double.MAX_VALUE);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private void attachChipInput(HBox hbox, java.util.function.Consumer<TextField> onKeyHandlerSetup) {
        hbox.setOnMouseClicked((e) -> {
            // avoid stacking multiple textfields if one is already active
            boolean alreadyHasField = hbox.getChildren().stream().anyMatch(n -> n instanceof TextField);
            if (alreadyHasField) {
                return;
            }

            TextField tf = createChipTextField();
            hbox.getChildren().add(tf); // always appended last
            HBox.setHgrow(tf, Priority.ALWAYS); // only THIS field grows to fill leftover space
            tf.requestFocus();

            onKeyHandlerSetup.accept(tf);

            tf.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    // lost focus - remove the field regardless of whether it committed
                    hbox.getChildren().remove(tf);
                }
            });
        });
    }

    private void setupChipKeyHandler(TextField tf, HBox hbox, String context) {
        tf.setOnKeyPressed((KeyEvent event) -> {
            String lsValue = tf.getText() == null ? "" : tf.getText();

            switch (event.getCode()) {
                case F3:
                case ENTER:
                case TAB:

                    handleChipSearch(context, lsValue, hbox, tf);

                    hbox.getChildren().remove(tf); // triggers rebuild via refresh methods below
                     {
                        try {
                            refreshChipBoxes();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            return;
                        }
                    }
                    event.consume();
                    break;

                default:
                    break;
            }
        });
    }

    private void handleChipSearch(String idKey, String typedValue, HBox hbName, TextField tf) {

        try {
            switch (idKey) {
                case "shopType":
                    if (!isJSONSuccess(poAppController.searchPromotionByShop(typedValue, false),
                            "Initialize Search Shop Type! ")) {
                        return;
                    }
                    break;

                case "brand":
                    if (!isJSONSuccess(poAppController.searchPromotionByBrand(-1, typedValue, false),
                            "Initialize Search Brand! ")) {
                        return;
                    }
                    break;

                case "model":
                    if (!isJSONSuccess(poAppController.searchPromotionByModel(-1, typedValue, false),
                            "Initialize Search Model! ")) {
                        return;
                    }

                    break;
                case "modelException":
                    if (!isJSONSuccess(poAppController.searchPromotionByModelException(-1, typedValue, false),
                            "Initialize Search Model Exceptioon! ")) {
                        return;
                    }

                    break;
                case "province":
                    if (!isJSONSuccess(poAppController.searchPromotionByProvince(typedValue, false),
                            "Initialize Search Province! ")) {
                        return;
                    }

                    break;

                case "area":
                    if (!isJSONSuccess(poAppController.searchPromotionByBranchArea(typedValue, false),
                            "Initialize Search Branch Area! ")) {
                        return;
                    }
                    break;
                case "giveaway":
                    if (!isJSONSuccess(poAppController.searchPromotionByGiveAway(-1, typedValue, false),
                            "Initialize Search Give Away! ")) {
                        return;
                    }
                    break;
                default:
                    break;

            }
            refreshChipBoxes();
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
        }
    }

    private void refreshChipBoxes() throws SQLException, GuanzonException, CloneNotSupportedException {

        if (paProvince == null) {
            paProvince = FXCollections.observableArrayList();
        }
        if (paBranchArea == null) {
            paBranchArea = FXCollections.observableArrayList();
        }
        if (paShopType == null) {
            paShopType = FXCollections.observableArrayList();
        }
        paProvince.setAll(poAppController.getSalesPromotionProvinceList());
        paBranchArea.setAll(poAppController.getSalesPromotionBranchAreaList());
        paBrand.setAll(poAppController.getSalesPromotionBrandList());
        paModel.setAll(poAppController.getSalesPromotionModelList());
        paModelException.setAll(poAppController.getSalesPromotionModelExceptionList());
        paGiveAway.setAll(poAppController.getSalesPromotionGiveAwayItemList());

        // Rebuild Shop Type list from the delimited string on master
        String lsShopType = poAppController.getMaster().getShopType();
        String[] laShopType = (lsShopType == null || lsShopType.trim().isEmpty())
                ? new String[0]
                : lsShopType.split("»");

        List<Model_Shop_Type> loShopTypeList = new ArrayList<>();
        for (int i = 0; i < laShopType.length; i++) {
            String lsCode = laShopType[i].trim();
            if (!lsCode.isEmpty()) {
                loShopTypeList.add(new Model_Shop_Type(i, lsCode));
            }
        }
        paShopType.setAll(loShopTypeList);
        hbModel.setDisable(paBrand.size() > 0);
        hbBrand.setDisable(paModel.size() > 0);
        hbModelException.setDisable(paModel.size() <= 0 && paBrand.size() <= 0);

        renderStatusChips(
                hbProvince,
                paProvince,
                (p) -> {
                    try {
                        return p.Province().getDescription();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        return "";
                    }
                },
                (p) -> poAppController.removeSalesPromotionProvince(p.getEntryNo()),
                (p, newState) -> poAppController.getSalesPromotionProvince(p.getEntryNo()).isWithActive(newState)
        );

        renderStatusChips(
                hbArea,
                paBranchArea,
                (p) -> {
                    try {
                        return p.BranchArea().getAreaDescription();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        return "";
                    }
                },
                (p) -> poAppController.removeSalesPromotionBranchArea(p.getEntryNo()),
                (p, newState) -> poAppController.getSalesPromotionBranchArea(p.getEntryNo()).isWithActive(newState)
        );

        renderStatusChips(
                hbBrand,
                paBrand,
                (p) -> {
                    try {
                        return p.Brand().getDescription();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        return "";
                    }
                },
                (p) -> poAppController.removeSalesPromotionByBrand(p.getEntryNo()),
                (p, newState) -> poAppController.getSalesPromotionBrand(p.getEntryNo()).isWithActive(newState)
        );

        renderStatusChips(
                hbModel,
                paModel,
                (p) -> {
                    try {
                        return p.Model().getDescription();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        return "";
                    }
                },
                (p) -> poAppController.removeSalesPromotionByModel(p.getEntryNo()),
                (p, newState) -> poAppController.getSalesPromotionModel(p.getEntryNo()).isWithActive(newState)
        );

        renderStatusChips(
                hbModelException,
                paModelException,
                (p) -> {
                    try {
                        return p.Model().getDescription();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        return "";
                    }
                },
                (p) -> poAppController.removeSalesPromotionByModelException(p.getEntryNo()),
                (p, newState) -> poAppController.getSalesPromotionModelException(p.getEntryNo()).isWithActive(newState)
        );

        renderStatusChips(
                hbGiveAway,
                paGiveAway,
                (p) -> {
                    try {
                        return p.Inventory().getBarCode();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        return "";
                    }
                },
                (p) -> poAppController.removeSalesPromotionByGiveAway(p.getEntryNo()),
                (p, newState) -> poAppController.getSalesPromotionGiveAway(p.getEntryNo()).isWithActive(newState)
        );
        reloadTableModel(); // keep model/exception table in sync since brand/model chips feed it
        reloadTableGiveAway(); // keep model/exception table in sync since brand/model chips feed it
        loadSelectedTransactionGiveAway(pnGiveAway);
        loadSelectedTransactionModel(pnRow);

    }

    private <T extends Model> void renderStatusChips(
            HBox hbox,
            List<T> list,
            java.util.function.Function<T, String> labelMapper,
            java.util.function.Consumer<T> removeAction, // ADDNEW path: hard remove
            java.util.function.BiConsumer<T, Boolean> toggleActiveAction // UPDATE path: soft toggle
    ) {

        hbox.getChildren().clear();
        List<APStatusController> chipControllers = new ArrayList<>();

        boolean lbTransactionIsNew = (poAppController.getEditMode() == EditMode.ADDNEW);

        for (T item : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ph/com/guanzongroup/integsys/views/child/APStatus.fxml"));

                APStatusController chipCtrl = new APStatusController();
                loader.setController(chipCtrl);
                Node chip = loader.load();

                chipCtrl.setDisplayText(labelMapper.apply(item));

                // If the whole transaction is brand new, OR this particular item was
                // just added this session (not yet loaded from DB), treat as TRASH.
                boolean lbItemIsNew = lbTransactionIsNew || (item.getEditMode() == EditMode.ADDNEW);
                chipCtrl.setMode(lbItemIsNew);

                if (!lbItemIsNew) {
                    chipCtrl.setActive(item.getValue("cActivexx").toString().equals("1"));
                }

                chipCtrl.setOnRemove(() -> {
                    removeAction.accept(item);
                    try {
                        refreshChipBoxes();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        return;
                    }
                });

                chipCtrl.setOnToggleActive((newState) -> {
                    toggleActiveAction.accept(item, newState);
                    try {
                        refreshChipBoxes();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        return;
                    }
                });

                chipCtrl.setOnSelect(() -> {
                    for (APStatusController other : chipControllers) {
                        if (other != chipCtrl) {
                            other.setSelected(false);
                        }
                    }
                });

                chipControllers.add(chipCtrl);
                hbox.getChildren().add(chip);
            } catch (java.io.IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to load APStatus.fxml", ex);
            }
        }
    }

    private <T> void renderChips(HBox hbox, List<T> list,
            java.util.function.Function<T, String> labelMapper,
            java.util.function.Consumer<T> removeAction) {

        hbox.getChildren().clear();
        List<APListController> chipControllers = new ArrayList<>();

        for (T item : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ph/com/guanzongroup/integsys/views/child/APList.fxml"));

                APListController chipCtrl = new APListController();
                loader.setController(chipCtrl);
                Node chip = loader.load();

                chipCtrl.setDisplayText(labelMapper.apply(item));
                chipCtrl.setOnRemove(() -> {
                    removeAction.accept(item);
                    try {
                        refreshChipBoxes();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        return;
                    }
                });
                chipCtrl.setOnSelect(() -> {
                    for (APListController other : chipControllers) {
                        if (other != chipCtrl) {
                            other.setSelected(false);
                        }
                    }
                });

                chipControllers.add(chipCtrl);
                hbox.getChildren().add(chip);
            } catch (java.io.IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to load APList.fxml", ex);
            }
        }
    }

    private void initRadioGroups() {

        // --- Insurance ---
        tgInsurance = new ToggleGroup();
        rbInsurance1.setToggleGroup(tgInsurance);
        rbInsurance0.setToggleGroup(tgInsurance);

        tgInsurance.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (pbSuppressToggleListener || newToggle == null) {
                return;
            }
            boolean lbValue = (newToggle == rbInsurance1); // true = Yes
            applyToSelectedModel((loSelected) -> {
                if (loSelected instanceof Model_Sales_Promotion_Brand) {
                    ((Model_Sales_Promotion_Brand) loSelected).isInsuranceFree(lbValue);
                } else if (loSelected instanceof Model_Sales_Promotion_Model) {
                    ((Model_Sales_Promotion_Model) loSelected).isInsuranceFree(lbValue);
                }
            });
        });

        // --- Registration ---
        tgRegistration = new ToggleGroup();
        rbRegistration1.setToggleGroup(tgRegistration);
        rbRegistration0.setToggleGroup(tgRegistration);

        tgRegistration.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (pbSuppressToggleListener || newToggle == null) {
                return;
            }
            boolean lbValue = (newToggle == rbRegistration1);
            applyToSelectedModel((loSelected) -> {
                if (loSelected instanceof Model_Sales_Promotion_Brand) {
                    ((Model_Sales_Promotion_Brand) loSelected).isRegistrationFree(lbValue);
                } else if (loSelected instanceof Model_Sales_Promotion_Model) {
                    ((Model_Sales_Promotion_Model) loSelected).isRegistrationFree(lbValue);
                }
            });
        });

        // --- Incentive ---
        tgIncentive = new ToggleGroup();
        rbIncentive1.setToggleGroup(tgIncentive);
        rbIncentive0.setToggleGroup(tgIncentive);

        tgIncentive.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (pbSuppressToggleListener || newToggle == null) {
                return;
            }
            boolean lbValue = (newToggle == rbIncentive1);
            tfIncentiveAmount.setDisable(!lbValue);
            applyToSelectedModel((loSelected) -> {
                if (loSelected instanceof Model_Sales_Promotion_Brand) {
                    ((Model_Sales_Promotion_Brand) loSelected).isWithIncentive(lbValue);
                } else if (loSelected instanceof Model_Sales_Promotion_Model) {
                    ((Model_Sales_Promotion_Model) loSelected).isWithIncentive(lbValue);
                }
            });
        });
    }

    private void applyToSelectedModel(java.util.function.Consumer<Model> loAction) {
        try {
            Model loSelected = tblViewModel.getSelectionModel().getSelectedItem();
            if (loSelected == null || loSelected instanceof Model_Sales_Promotion_Model_Exception) {
                return;
            }
            loAction.accept(loSelected);
            reloadTableModel();
            loadSelectedTransactionModel(pnRow);
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private double computeNetSRP(double fnSRP, double fnDiscRate, double fnDiscAmount) {
        double lnNet = fnSRP - (fnSRP * (fnDiscRate / 100.0)) - fnDiscAmount;
        return lnNet < 0.0 ? 0.0 : lnNet;
    }

    private double parseOrZero(String fsValue) {
        if (fsValue == null) {
            return 0.0;
        }
        String lsClean = fsValue.trim().replace(",", "");
        if (lsClean.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(lsClean);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private int parseOrZeroint(String fsValue) {
        if (fsValue == null) {
            return 0;
        }
        String lsClean = fsValue.trim().replace(",", "");
        if (lsClean.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(lsClean);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

}
