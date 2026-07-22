/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelBankApplications_Detail;
import ph.com.guanzongroup.integsys.model.ModelRequirements_Detail;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Main;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import javafx.animation.PauseTransition;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.SalesInquiryStatic;
import org.guanzon.appdriver.constant.UserRight;
import ph.com.guanzongroup.cas.sales.t1.status.BankApplicationStatus;

/**
 * FXML Controller class
 *
 * @author Team 2
 */
public class SalesInquiry_ConfirmationMCController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0, pnMain = 0, pnRequirements = 0, pnBankApplications = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesControllers poSalesInquiryController;
    public int pnEditMode;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchClientId = "";
    private String psTransactionNo = "";
    private boolean pbEntered, pbPurchaseTypeChanged = false;

    private ObservableList<ModelSalesInquiry_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelSalesInquiry_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelRequirements_Detail> requirements_data = FXCollections.observableArrayList();
    private ObservableList<ModelBankApplications_Detail> bankapplications_data = FXCollections.observableArrayList();
    private FilteredList<ModelSalesInquiry_Main> filteredData;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    ObservableList<String> ClientType = ModelSalesInquiry_Detail.ClientType;
    ObservableList<String> PurchaseType = ModelSalesInquiry_Detail.PurchaseType;
    ObservableList<String> CategoryType = ModelSalesInquiry_Detail.CategoryType;
    ObservableList<String> CustomerGroup = ModelSalesInquiry_Detail.CustomerGroup;
    private final JFXUtil.RowDragLock dragLock = new JFXUtil.RowDragLock(true);
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain, loadTableRequirements, loadTableBankApplications;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apInquiry, apFields, apMaster, apDetail, apTableDetail, apRequirements;//, apBankApplications, apBankApplicationsButtons;
    @FXML
    private TextField tfSearchClient, tfSearchReferenceNo, tfTransactionNo, tfBranch, tfSalesPerson, tfReferralAgent, tfInquiryStatus, tfInquiryType, tfClient, tfAddress, tfContactNo, tfBrand, tfModel, tfColor, tfModelVariant, tfSellingPrice, tfRequirement, tfReceivedBy;//, tfPaymentMode, tfApplicationNo, tfBank;
    @FXML
    private Label lblSource, lblStatus;//, lblBankApplicationStatus;
    @FXML
    private HBox hbButtons, mainHbox;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnVoid, btnHistory, btnRetrieve, btnClose;//, btnApprove, btnDisApprove, btnCancelApplication;
    @FXML
    private TabPane tabpane;
    @FXML
    private DatePicker dpTransactionDate, dpTargetDate, dpReceivedDate;//, dpAppliedDate, dpApprovedDate;
    @FXML
    private ComboBox cmbPurchaseType, cmbCategoryType, cmbClientType, cmbCustomerGroup;
    @FXML
    private TextArea taRemarks;//, taBankAppRemarks;
    @FXML
    private TableView tblViewTransDetails, tblViewRequirements, tblViewBankApplications, tblViewMainList;
    @FXML
    private TableColumn tblRowNoDetail, tblBrandDetail, tblDescriptionDetail, tblRequirementRowNo, tblRequired, tblSubmitted, tblRequirements, tblReceivedBy, tblReceivedDate, tblBankAppRowNo, tblBankAppNo, tblBank, tblAppliedDate, tblApprovedDate, tblStatus, tblRowNo, tblClient, tblDate, tblReferenceNo;
    @FXML
    private Pagination pgPagination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        poSalesInquiryController = new SalesControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poSalesInquiryController.SalesInquiry().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initLoadTable();
        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initRequirementsGrid();
        initBankApplicationsGrid();
        initTableOnClick();
        clearTextFields();
        initComboBoxes();
        Platform.runLater(() -> {
            poSalesInquiryController.SalesInquiry().Master().setIndustryId(psIndustryId);
            poSalesInquiryController.SalesInquiry().Master().setCompanyId(psCompanyId);
            poSalesInquiryController.SalesInquiry().Master().setCategoryCode(psCategoryId);
            poSalesInquiryController.SalesInquiry().setIndustryId(psIndustryId);
            poSalesInquiryController.SalesInquiry().setCompanyId(psCompanyId);
            poSalesInquiryController.SalesInquiry().setCategoryId(psCategoryId);
//            poSalesInquiryController.SalesInquiry().initFields();
            poSalesInquiryController.SalesInquiry().setWithUI(true);
            loadRecordSearch();
            loadRecordMaster();
        });

        pgPagination.setPageCount(1);

        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
        JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference

        initTabPane();
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

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String tabText = "";

            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poSalesInquiryController.SalesInquiry().OpenTransaction(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
                        poJSON = poSalesInquiryController.SalesInquiry().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poSalesInquiryController.SalesInquiry().loadRequirements();
                        poSalesInquiryController.SalesInquiry().loadBankApplications();
                        pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail, apRequirements);//, apBankApplications);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnRetrieve":
                        retrieveSalesInquiry();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poSalesInquiryController.SalesInquiry().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poSalesInquiryController.SalesInquiry().AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON;

                                loJSON = poSalesInquiryController.SalesInquiry().OpenTransaction(psTransactionNo);

                                if ("success".equals(loJSON.get("result"))) {
                                    if (poSalesInquiryController.SalesInquiry().Master().getTransactionStatus().equals(SalesInquiryStatic.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poSalesInquiryController.SalesInquiry().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                            }
                        } else {
                            return;
                        }

                        break;

                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poSalesInquiryController.SalesInquiry().ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void transaction?") == true) {
                            if (SalesInquiryStatic.CONFIRMED.equals(poSalesInquiryController.SalesInquiry().Master().getTransactionStatus())) {
                                poJSON = poSalesInquiryController.SalesInquiry().CancelTransaction("");
                            } else {
                                poJSON = poSalesInquiryController.SalesInquiry().VoidTransaction("");
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisApprove", "btnCancelApplication")) {
                    loadTableBankApplications.reload();
                    return;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnConfirm", "btnReturn", "btnVoid", "btnCancel")) {
                    poSalesInquiryController.SalesInquiry().resetMaster();
                    poSalesInquiryController.SalesInquiry().Detail().clear();
                    poSalesInquiryController.SalesInquiry().resetOthers();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();

                    poSalesInquiryController.SalesInquiry().Master().setIndustryId(psIndustryId);
                    poSalesInquiryController.SalesInquiry().Master().setCompanyId(psCompanyId);
                    poSalesInquiryController.SalesInquiry().Master().setCategoryCode(psCategoryId);
//                    poSalesInquiryController.SalesInquiry().initFields();
                }
                String currentTitle = tabpane.getSelectionModel().getSelectedItem().getText();
                switch (currentTitle) {
                    case "Requirements":
                        JFXUtil.clickTabByTitleText(tabpane, "Requirements");
                        break;
                    case "Bank Applications":
                        JFXUtil.clickTabByTitleText(tabpane, "Bank Applications");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel")) {
                    JFXUtil.clickTabByTitleText(tabpane, "Inquiry");
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnPrint", "btnAddAttachment", "btnRemoveAttachment",
                        "btnArrowRight", "btnArrowLeft", "btnRetrieve")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                }
                initButton(pnEditMode);
                if (lsButton.equals("btnUpdate")) {
                    if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().Detail(pnDetail).getStockId(), null, "")) {
                        tfBrand.requestFocus();
                    } else {
                        tfBrand.requestFocus();
                    }
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void retrieveSalesInquiry() {
        poJSON = new JSONObject();
        poSalesInquiryController.SalesInquiry().setTransactionStatus(SalesInquiryStatic.OPEN + SalesInquiryStatic.CONFIRMED);
        poJSON = poSalesInquiryController.SalesInquiry().loadSalesInquiry(psIndustryId, tfSearchClient.getText(), tfSearchReferenceNo.getText());

        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain.reload();
        }
    }

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
                    case "taRemarks"://Remarks
                        poJSON = poSalesInquiryController.SalesInquiry().Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        loadRecordMaster();
                        break;
//                    case "taBankAppRemarks"://Remarks
//                        poJSON = poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).setRemarks(lsValue);
//                        if ("error".equals((String) poJSON.get("result"))) {
//                            System.err.println((String) poJSON.get("message"));
//                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                            return;
//                        }
//                        loadRecordBankApplications();
//                        break;
                }
            });

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfBrand":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poSalesInquiryController.SalesInquiry().Detail(pnDetail).setBrandId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setStockId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setModelId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setColorId("");
                        }
                        break;
                    case "tfModel":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setModelId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setStockId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setColorId("");
                        }
                        break;
                    case "tfColor":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setColorId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setStockId("");
                        }
                        if (pbEntered) {
                            moveNext(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                Platform.runLater(() -> {
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                    delay.setOnFinished(event -> {
                        loadTableDetail.reload();
                    });
                    delay.play();
                });
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSalesPerson":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setSalesMan("");
                        }
                        break;
                    case "tfReferralAgent":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setAgentId("");
                        }
                        break;
                    case "tfInquiryType":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setSourceCode("");
                        }
                        break;
                    case "tfClient":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setClientId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setAddressId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setContactId("");
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtRequirements_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfRequirement":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).setRequirementCode(lsValue);
                            JFXUtil.runWithDelay(0.70, () -> loadTableRequirements.reload());
                        }
                        break;
                    case "tfReceivedBy":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).setReceivedBy("");
                            JFXUtil.runWithDelay(0.70, () -> loadTableRequirements.reload());
                        }
                        break;
                }
                loadRecordRequirements();
            });

    ChangeListener<Boolean> txtBankApplications_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfApplicationNo":
                        poJSON = poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).setApplicationNo(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        JFXUtil.runWithDelay(0.70, () -> loadTableBankApplications.reload());
                        break;
                    case "tfBank":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).setBankId(lsValue);
                            JFXUtil.runWithDelay(0.70, () -> loadTableBankApplications.reload());
                        }
                        break;
                }
//                loadRecordBankApplications();
            });

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSearchClient":
                        if (lsValue.equals("")) {
                            psSearchClientId = "";
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchReferenceNo":
                        break;
                }
                loadRecordSearch();
            });

    public void moveNext(boolean isUp, boolean continueNext) {
        apDetail.requestFocus();
        if (continueNext) {
            pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewTransDetails) : JFXUtil.moveToNextRow(tblViewTransDetails);
        }
        loadRecordDetail();
        if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().Detail(pnDetail).getBrandId(), null, "")) {
            tfBrand.requestFocus();
        } else if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().Detail(pnDetail).getModelId(), null, "")) {
            tfModel.requestFocus();
        } else {
            tfColor.requestFocus();
        }
    }

    public void moveNextRequirements(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apRequirements.requestFocus();
            pnRequirements = isUp ? JFXUtil.moveToPreviousRow(tblViewRequirements) : JFXUtil.moveToNextRow(tblViewRequirements);
        }
        loadRecordRequirements();
    }

//    public void moveNextBankApplications(boolean isUp, boolean continueNext) {
//        if (continueNext) {
//            apBankApplications.requestFocus();
//            pnBankApplications = isUp ? JFXUtil.moveToPreviousRow(tblViewBankApplications) : JFXUtil.moveToNextRow(tblViewBankApplications);
//        }
//        loadRecordBankApplications();
//        if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getApplicationNo(), null, "")) {
//            tfApplicationNo.requestFocus();
//        } else if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getBankId(), null, "")) {
//            tfBank.requestFocus();
//        } else {
//            tfApplicationNo.requestFocus();
//        }
//    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;

            TableView<?> currentTable = tblViewTransDetails;
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfModel":
                        case "tfColor":
                            moveNext(true, true);
                            event.consume();
                            break;
                        case "tfRequirement":
                        case "tfReceivedBy":
                            moveNextRequirements(true, true);
                            event.consume();
                            break;
//                        case "tfApplicationNo":
//                        case "tfBank":
//                            moveNextBankApplications(true, true);
//                            event.consume();
//                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfModel":
                        case "tfColor":
                            moveNext(false, true);
                            event.consume();
                            break;
                        case "tfRequirement":
                        case "tfReceivedBy":
                            moveNextRequirements(false, true);
                            event.consume();
                            break;
//                        case "tfApplicationNo":
//                        case "tfBank":
//                            moveNextBankApplications(false, true);
//                            event.consume();
//                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchClient":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchClient(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchClient.setText("");
                                psSearchClientId = "";
                                break;
                            }
                            psSearchClientId = poSalesInquiryController.SalesInquiry().Master().getClientId();
                            loadRecordSearch();
                            retrieveSalesInquiry();
                            return;
                        case "tfSearchReferenceNo":
                            retrieveSalesInquiry();
                            return;
                        case "tfClient":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchClient(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfClient.setText("");
                                break;
                            }
                            loadRecordMaster();
                            return;
                        case "tfSalesPerson":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchSalesPerson(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfReferralAgent);
                            }
                            loadRecordMaster();
                            return;
                        case "tfReferralAgent":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchReferralAgent(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfReferralAgent.setText("");
                                break;
                            }
                            loadRecordMaster();
                            return;
                        case "tfInquiryType":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchSource(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));

                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfClient);
                            }
                            loadRecordMaster();
                            return;
                        case "tfBrand":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchBrand(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfBrand.setText("");
                                break;
                            }
                            loadTableDetail.reload();
                            JFXUtil.textFieldMoveNext(tfModel);
                            break;
                        case "tfModel":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchModel(lsValue, false, pnDetail);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfModel.setText("");
                                break;
                            }
                            loadTableDetail.reload();
                            JFXUtil.textFieldMoveNext(tfColor);
                            break;
                        case "tfColor":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchColor(lsValue, false, pnDetail);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfColor.setText("");
                                break;
                            } else {
                                loadTableDetail.reload();
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        moveNext(false, true);
                                    });
                                    delay.play();
                                });
                            }
                            break;
                        case "tfReceivedBy":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchReceivedBy(lsValue, false, pnRequirements);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfBrand.setText("");
                                break;
                            }
                            loadTableRequirements.reload();
                            break;
//                        case "tfBank":
//                            poJSON = poSalesInquiryController.SalesInquiry().SearchBank(lsValue, false, pnBankApplications);
//                            if ("error".equals(poJSON.get("result"))) {
//                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                tfBank.setText("");
//                                break;
//                            }
//                            loadTableBankApplications.reload();
//                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");

        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                LocalDate ldcurrentDate = null, ldselectedDate = null, ldTransactionDate = null;
                String lsServerDate = "", lsTransDate = "", lsSelectedDate = "", lsReceivingDate = "";

                JFXUtil.JFXUtilDateResult ldtResult = JFXUtil.processDate(inputText, datePicker);
                poJSON = ldtResult.poJSON;
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    loadRecordMaster();
                    return;
                }
                if (JFXUtil.isObjectEqualTo(inputText, null, "", "01/01/1900")) {
                    return;
                }
                ldselectedDate = ldtResult.selectedDate;
                lsServerDate = sdfFormat.format(oApp.getServerDate());
                lsTransDate = sdfFormat.format(poSalesInquiryController.SalesInquiry().Master().getTransactionDate());
                lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));

                ldcurrentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                ldTransactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                ldselectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                if (poSalesInquiryController.SalesInquiry().getEditMode() == EditMode.ADDNEW || poSalesInquiryController.SalesInquiry().getEditMode() == EditMode.UPDATE) {
                    switch (datePicker.getId()) {
                        case "dpTargetDate":
                            if (ldselectedDate.isBefore(ldTransactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Target date cannot be before the transaction date.");
                                pbSuccess = false;
                            } else {
                                poSalesInquiryController.SalesInquiry().Master().setTargetDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                        case "dpReceivedDate":
                            if (ldselectedDate.isBefore(ldTransactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Received date cannot be before the inquiry date.");
                                pbSuccess = false;
                            } else if (ldselectedDate.isAfter(ldcurrentDate)) {
                                JFXUtil.setJSONError(poJSON, "Received date cannot be after the current date.");
                                pbSuccess = false;
                            } else {
                                poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).setReceivedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                            if (pbSuccess) {
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadTableRequirements.reload();
                            pbSuccess = true; //Set to original value
                            break;
                        case "dpAppliedDate":
                            if (ldselectedDate.isBefore(ldTransactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Applied date cannot be before the transaction date.");
                                pbSuccess = false;
                            } else {
                                poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).setAppliedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                            if (pbSuccess) {
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadTableBankApplications.reload();
                            pbSuccess = true; //Set to original value
                            break;
                        case "dpApprovedDate":
                            if (ldselectedDate.isBefore(ldTransactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Approved date cannot be before the transaction date.");
                                pbSuccess = false;
                            } else {
                                poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).setApprovedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                            if (pbSuccess) {
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadTableBankApplications.reload();
                            pbSuccess = true; //Set to original value
                            break;
                        default:
                            break;
                    }
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initTabPane() {
        tabpane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            try {
                if (newTab != null) {
                    String tabTitle = newTab.getText();
                    switch (tabTitle) {
                        case "Inquiry":
                            break;
                        case "Requirements":
                            JFXUtil.clearTextFields(apRequirements);
                            if (pnEditMode == EditMode.UPDATE) {
                                if (poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() > 0 && !pbPurchaseTypeChanged) {
                                } else {

                                    poSalesInquiryController.SalesInquiry().getRequirements(String.valueOf(cmbCustomerGroup.getSelectionModel().getSelectedIndex()));
                                    pbPurchaseTypeChanged = false;
                                }
                            }
                            loadTableRequirements.reload();
                            break;
                        case "Bank Applications":
//                            JFXUtil.clearTextFields(apBankApplications);
                            if (pnEditMode == EditMode.UPDATE) {
                                if (poSalesInquiryController.SalesInquiry().getBankApplicationsCount() > 0 && !pbPurchaseTypeChanged) {
                                } else {
                                    pbPurchaseTypeChanged = false;
                                }
                            }
                            loadTableBankApplications.reload();
                            break;
                    }
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }
        });
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poSalesInquiryController.SalesInquiry().Master().Company().getCompanyName() + " - " + poSalesInquiryController.SalesInquiry().Master().Industry().getDescription());
            tfSearchClient.setText(psSearchClientId.equals("") ? "" : poSalesInquiryController.SalesInquiry().Master().Client().getCompanyName());

            tfSearchReferenceNo.setText("");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.ADDNEW;
        JFXUtil.setDisabled(!lbDisable, tfClient, cmbClientType, cmbCategoryType);
        try {
            Platform.runLater(() -> {
                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().Master().getTransactionStatus();
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put(SalesInquiryStatic.QUOTED, "QUOTED");
                statusMap.put(SalesInquiryStatic.SALE, "SALE");
                statusMap.put(SalesInquiryStatic.CONFIRMED, "CONFIRMED");
                statusMap.put(SalesInquiryStatic.OPEN, "OPEN");
                statusMap.put(SalesInquiryStatic.VOID, "VOIDED");
                statusMap.put(SalesInquiryStatic.CANCELLED, "CANCELLED");
                statusMap.put(SalesInquiryStatic.LOST, "LOST");
                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN"); //default
                lblStatus.setText(lsStat);

                switch (poSalesInquiryController.SalesInquiry().Master().getInquiryStatus()) {
                    case "0":
                        tfInquiryStatus.setText("OPEN");
                        break;
                    default:
                        tfInquiryStatus.setText("");
                        break;
                }
            });

            // Transaction Date
            tfTransactionNo.setText(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            String lsTargetDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().Master().getTargetDate());
            dpTargetDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTargetDate, "yyyy-MM-dd"));

            tfBranch.setText(poSalesInquiryController.SalesInquiry().Master().Branch().getBranchName());
            tfSalesPerson.setText(poSalesInquiryController.SalesInquiry().Master().SalesPerson().getFullName());
            tfReferralAgent.setText(poSalesInquiryController.SalesInquiry().Master().ReferralAgent().getCompanyName());

            tfClient.setText(poSalesInquiryController.SalesInquiry().Master().Client().getCompanyName());
            tfAddress.setText(poSalesInquiryController.SalesInquiry().Master().ClientAddress().getAddress());
            tfContactNo.setText(poSalesInquiryController.SalesInquiry().Master().ClientMobile().getMobileNo());
            tfInquiryType.setText(poSalesInquiryController.SalesInquiry().Master().Source().getDescription());

            taRemarks.setText(poSalesInquiryController.SalesInquiry().Master().getRemarks());

            if (pnEditMode != EditMode.UNKNOWN) {

                cmbPurchaseType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getPurchaseType()));
                if (poSalesInquiryController.SalesInquiry().Master().getClientId() != null && !"".equals(poSalesInquiryController.SalesInquiry().Master().getClientId())) {
                    cmbClientType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().Client().getClientType()));
                } else {
                    cmbClientType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getClientType()));
                }
                
                if(poSalesInquiryController.SalesInquiry().Master().getCategoryType() != null && !"".equals(poSalesInquiryController.SalesInquiry().Master().getCategoryType())){
                    cmbCategoryType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getCategoryType()));
                }
            } else {
                cmbPurchaseType.getSelectionModel().select(0);
                cmbClientType.getSelectionModel().select(0);
                cmbCategoryType.getSelectionModel().select(0);
            }

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poSalesInquiryController.SalesInquiry().getDetailCount() - 1) {
                return;
            }
            tfBrand.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Brand().getDescription());
            tfModel.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Model().getDescription());
            tfModelVariant.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).ModelVariant().getDescription());
            tfColor.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Color().getDescription());
            tfSellingPrice.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSalesInquiryController.SalesInquiry().Detail(pnDetail).getSellPrice(), true));
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordRequirements() {
        try {
            int lnCustomerGroup = 0;
            if (pnEditMode != EditMode.UNKNOWN && poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() > 0) {
                if (!JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(0).getCustomerGroup(), null, "")) {
                    lnCustomerGroup = Integer.parseInt(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(0).getCustomerGroup());
                } else {
                    if (poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() > 0) {
                        poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(0).setCustomerGroup(String.valueOf(0));
                    }
                }
            }
            cmbCustomerGroup.setOnAction(null);
            cmbCustomerGroup.getSelectionModel().select(lnCustomerGroup);
            cmbCustomerGroup.setOnAction(comboBoxActionListener);

            if (pnRequirements < 0 || pnRequirements > poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() - 1) { // intended to place here
                return;
            }
            tfRequirement.setText(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).RequirementSource().getDescription());
            tfReceivedBy.setText(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).SalesPerson().getFullName());
            String lsdpReceivedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).getReceivedDate());
            dpReceivedDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsdpReceivedDate, "yyyy-MM-dd"));

            boolean lbShow = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).SalesPerson().getFullName(), null, "");
            JFXUtil.setDisabled(lbShow, dpReceivedDate);
            JFXUtil.updateCaretPositions(apRequirements);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

//    public void loadRecordBankApplications() {
//        try {
//            boolean lbShow1 = poSalesInquiryController.SalesInquiry().getBankApplicationsCount() > 0;
//            JFXUtil.setDisabled(!lbShow1, apBankApplicationsButtons);
//            if (pnBankApplications < 0 || pnBankApplications > poSalesInquiryController.SalesInquiry().getBankApplicationsCount() - 1) {
//                return;
//            }
//            Platform.runLater(() -> {
//                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getTransactionStatus();
//                Map<String, String> statusMap = new HashMap<>();
//                statusMap.put(BankApplicationStatus.OPEN, "OPEN");
//                statusMap.put(BankApplicationStatus.APPROVED, "APPROVED");
//                statusMap.put(BankApplicationStatus.DISAPPROVED, "DISAPPROVED");
//                statusMap.put(BankApplicationStatus.CANCELLED, "CANCELLED");
//                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN"); //default
//                lblBankApplicationStatus.setText(lsStat);
//            });
//
//            JFXUtil.setDisabled(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getEditMode() == EditMode.ADDNEW, apBankApplicationsButtons, dpApprovedDate);
//
//            boolean lbShow = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getTransactionStatus(),
//                    BankApplicationStatus.APPROVED, BankApplicationStatus.DISAPPROVED, BankApplicationStatus.CANCELLED);
//            boolean lbShow2 = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getEditMode(), EditMode.UPDATE);
//            JFXUtil.setDisabled(lbShow || lbShow2, tfBank);
//
//            String lsPaymentMode = "";
//            if (!JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getPaymentMode(), null, "")) {
//                lsPaymentMode = PurchaseType.get(Integer.valueOf(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getPaymentMode()));
//            } else {
//                lsPaymentMode = "";
//            }
//            tfPaymentMode.setText(lsPaymentMode);
//            tfApplicationNo.setText(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getApplicationNo());
//            tfBank.setText(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).Bank().getBankName());
//            taBankAppRemarks.setText(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getRemarks());
//
//            String lsdpAppliedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getAppliedDate());
//            dpAppliedDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsdpAppliedDate, "yyyy-MM-dd"));
//
//            String lsdpApprovedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnBankApplications).getApprovedDate());
//            dpApprovedDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsdpApprovedDate, "yyyy-MM-dd"));
//            JFXUtil.updateCaretPositions(apBankApplications);
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//        }
//    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelSalesInquiry_Main selected = (ModelSalesInquiry_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                psTransactionNo = poSalesInquiryController.SalesInquiry().SalesInquiryList(pnMain).getTransactionNo();
                poJSON = poSalesInquiryController.SalesInquiry().OpenTransaction(psTransactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                } else {
                    poSalesInquiryController.SalesInquiry().loadRequirements();
                    poSalesInquiryController.SalesInquiry().loadBankApplications();
                }
            }
            JFXUtil.clearTextFields(apRequirements);//, apBankApplications);
            Platform.runLater(() -> {
                loadTableDetail.reload();
                String currentTitle = tabpane.getSelectionModel().getSelectedItem().getText();
                switch (currentTitle) {
                    case "Requirements":
                        loadTableRequirements.reload();
                        break;
                    case "Bank Applications":
                        loadTableBankApplications.reload();
                        break;
                }
            });

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        main_data.clear();
                        JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                        if (poSalesInquiryController.SalesInquiry().getSalesInquiryCount() > 0) {
                            //pending
                            //retreiving using column index
                            for (int lnCtr = 0; lnCtr <= poSalesInquiryController.SalesInquiry().getSalesInquiryCount() - 1; lnCtr++) {
                                try {
                                    main_data.add(new ModelSalesInquiry_Main(String.valueOf(lnCtr + 1),
                                            String.valueOf(poSalesInquiryController.SalesInquiry().SalesInquiryList(lnCtr).Client().getCompanyName()),
                                            String.valueOf(poSalesInquiryController.SalesInquiry().SalesInquiryList(lnCtr).getTransactionDate()),
                                            String.valueOf(poSalesInquiryController.SalesInquiry().SalesInquiryList(lnCtr).getTransactionNo())
                                    ));
                                } catch (GuanzonException | SQLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                }

                                if (poSalesInquiryController.SalesInquiry().SalesInquiryList(lnCtr).getTransactionStatus().equals(SalesInquiryStatic.CONFIRMED)) {
                                    JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCtr + 1), "#C1E1C1", highlightedRowsMain);
                                }
                            }
                        }

                        if (pnMain < 0 || pnMain
                                >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewMainList, 0);
                                pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();

                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewMainList, pnMain);
                        }

                        JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                    });
                });
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {

                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poSalesInquiryController.SalesInquiry().loadDetail();
                            }
                            poSalesInquiryController.SalesInquiry().sortPriority();

                            String lsBrand = "";
                            String lsModel = "";
                            String lsModelVariant = "";
                            String lsColor = "";
                            String lsDescription = "";
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getDetailCount(); lnCtr++) {
                                if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).getStockId() != null
                                        && !"".equals(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getStockId())) {
                                    lsBrand = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().Brand().getDescription();
                                    lsModel = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().Model().getDescription();
                                    lsModelVariant = " " + poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().Variant().getDescription();
                                    lsColor = " " + poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().Color().getDescription();
                                } else {
                                    if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).Brand().getDescription() != null) {
                                        lsBrand = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Brand().getDescription();
                                    }
                                    if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).Model().getDescription() != null) {
                                        lsModel = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Model().getDescription();
                                    }
                                    if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).ModelVariant().getDescription() != null) {
                                        lsModelVariant = " " + poSalesInquiryController.SalesInquiry().Detail(lnCtr).ModelVariant().getDescription();
                                    }
                                    if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).Color().getDescription() != null) {
                                        lsColor = " " + poSalesInquiryController.SalesInquiry().Detail(lnCtr).Color().getDescription();
                                    }
                                }
                                lsDescription = lsModel
                                        + lsModelVariant
                                        + lsColor;
                                details_data.add(
                                        new ModelSalesInquiry_Detail(
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getPriority()),
                                                lsBrand,
                                                lsDescription.trim().replaceAll("\\r?\\n", " "),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getBrandId()),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getModelId()),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getColorId()),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getModelVarianId())
                                        ));
                                lsBrand = "";
                                lsModel = "";
                                lsModelVariant = "";
                                lsColor = "";
                            }

                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewTransDetails, 0);
                                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewTransDetails, pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }

                    });
                });

        loadTableRequirements = new JFXUtil.ReloadableTableTask(
                tblViewRequirements,
                requirements_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        requirements_data.clear();
                        try {
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount(); lnCtr++) {
                                int lnIsRequired = poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).isRequired() ? 1 : 0;
                                int lnIsSubmitted = poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).isSubmitted() ? 1 : 0;

                                String lsReceivedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).getReceivedDate());
                                requirements_data.add(
                                        new ModelRequirements_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(lnIsRequired),
                                                String.valueOf(lnIsSubmitted),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).RequirementSource().getDescription()),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).SalesPerson().getFullName()),
                                                String.valueOf(CustomCommonUtil.parseDateStringToLocalDate(lsReceivedDate, "yyyy-MM-dd"))
                                        ));
                            }
                            if (pnRequirements < 0 || pnRequirements
                                    >= requirements_data.size()) {
                                if (!requirements_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewRequirements, 0);
                                    pnRequirements = tblViewRequirements.getSelectionModel().getSelectedIndex();

                                }
                                loadRecordRequirements();
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewRequirements, pnRequirements);
                                loadRecordRequirements();
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });

        loadTableBankApplications = new JFXUtil.ReloadableTableTask(
                tblViewBankApplications,
                bankapplications_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        bankapplications_data.clear();
                        try {
                            if (pnEditMode != EditMode.UNKNOWN) {
                                poSalesInquiryController.SalesInquiry().loadBankApplicationList();
                            }
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getBankApplicationsCount(); lnCtr++) {
                                String lsAppliedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getAppliedDate());
                                String lsApprovedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getApprovedDate());

                                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getTransactionStatus();
                                Map<String, String> statusMap = new HashMap<>();
                                statusMap.put(BankApplicationStatus.OPEN, "OPEN");
                                statusMap.put(BankApplicationStatus.APPROVED, "APPROVED");
                                statusMap.put(BankApplicationStatus.DISAPPROVED, "DISAPPROVED");
                                statusMap.put(BankApplicationStatus.CANCELLED, "CANCELLED");
                                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN"); //default

                                String lsBank = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName(), null, "")
                                        ? "" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName();

                                bankapplications_data.add(
                                        new ModelBankApplications_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getApplicationNo()),
                                                String.valueOf(lsBank),
                                                String.valueOf(lsAppliedDate),
                                                String.valueOf(lsApprovedDate),
                                                String.valueOf(lsStat)
                                        )
                                );
                            }
//                            if (pnBankApplications < 0 || pnBankApplications
//                                    >= bankapplications_data.size()) {
//                                if (!bankapplications_data.isEmpty()) {
//                                    /* FOCUS ON FIRST ROW */
//                                    JFXUtil.selectAndFocusRow(tblViewBankApplications, 0);
//                                    pnBankApplications = tblViewBankApplications.getSelectionModel().getSelectedIndex();
//
//                                }
//                                loadRecordBankApplications();
//                            } else {
//                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//                                JFXUtil.selectAndFocusRow(tblViewBankApplications, pnBankApplications);
//                                loadRecordBankApplications();
//                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });
    }
    final EventHandler<ActionEvent> comboBoxActionListener = event -> {
        Platform.runLater(() -> {
            try {
                Object source = event.getSource();
                @SuppressWarnings("unchecked")
                ComboBox<?> cb = (ComboBox<?>) source;

                String cbId = cb.getId();
                int selectedIndex = cb.getSelectionModel().getSelectedIndex();
                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                    switch (cbId) {
                        case "cmbClientType":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!poSalesInquiryController.SalesInquiry().Master().getClientType().equals(String.valueOf(selectedIndex))) {
                                    if (poSalesInquiryController.SalesInquiry().getDetailCount() > 0) {
                                        if (!JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().Detail(0).getBrandId(), null, "")) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the client name?\nPlease note that doing so will delete all sales inquiry details.\n\nDo you wish to proceed?") == true) {
                                                poSalesInquiryController.SalesInquiry().Master().setClientId("");
                                                poSalesInquiryController.SalesInquiry().Master().setAddressId("");
                                                poSalesInquiryController.SalesInquiry().Master().setContactId("");
                                                poSalesInquiryController.SalesInquiry().removeDetails();
                                                poSalesInquiryController.SalesInquiry().Master().setClientType(String.valueOf(selectedIndex));
                                                loadTableDetail.reload();
                                            }
                                        } else {
                                            poSalesInquiryController.SalesInquiry().Master().setClientId("");
                                            poSalesInquiryController.SalesInquiry().Master().setAddressId("");
                                            poSalesInquiryController.SalesInquiry().Master().setContactId("");
                                            poSalesInquiryController.SalesInquiry().Master().setClientType(String.valueOf(selectedIndex));
                                        }
                                    }
                                }
                            }
                            break;
                        case "cmbPurchaseType":
                            if (pnEditMode == EditMode.UPDATE && poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() > 0 || poSalesInquiryController.SalesInquiry().getBankApplicationsCount() > 0) {
                                if (!poSalesInquiryController.SalesInquiry().Master().getPurchaseType().equals(String.valueOf(cmbPurchaseType.getSelectionModel().getSelectedIndex()))) {
                                    poJSON = poSalesInquiryController.SalesInquiry().checkPendingBankApplication();
                                    if ("error".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                        break;
                                    }

                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the Purchase Type?\nPlease note that doing so will reset the Requirements & Bank Applications list.\n\nDo you wish to proceed?") == true) {
                                        poSalesInquiryController.SalesInquiry().Master().setPurchaseType(String.valueOf(selectedIndex));
                                        poJSON = poSalesInquiryController.SalesInquiry().removeRequirements();
                                        poJSON = poSalesInquiryController.SalesInquiry().removeBankApplications();
                                        if ("error".equals((String) poJSON.get("result"))) {
                                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                            break;
                                        }
                                        pbPurchaseTypeChanged = true;
                                    }
                                }
                            } else {
                                poSalesInquiryController.SalesInquiry().Master().setPurchaseType(String.valueOf(selectedIndex));
                            }
                            break;
                        case "cmbCategoryType":
                            poSalesInquiryController.SalesInquiry().Master().setCategoryType(String.valueOf(selectedIndex));
                            break;
                        case "cmbCustomerGroup":
                            if (pnEditMode == EditMode.UPDATE) {
                                if (poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() > 0) {
                                    if (!poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(0).getCustomerGroup().equals(String.valueOf(selectedIndex))) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the Customer group?\nPlease note that doing so will delete all requirements list.\n\nDo you wish to proceed?") == true) {
                                            poJSON = poSalesInquiryController.SalesInquiry().getRequirements(String.valueOf(selectedIndex));
                                            if ("error".equals((String) poJSON.get("result"))) {
                                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                                poSalesInquiryController.SalesInquiry().getRequirements(String.valueOf(0));
                                            }
                                            JFXUtil.clearTextFields(apRequirements);
                                        }
                                    }
                                } else {
                                    poJSON = poSalesInquiryController.SalesInquiry().getRequirements(String.valueOf(selectedIndex));
                                    if ("error".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                        poSalesInquiryController.SalesInquiry().getRequirements(String.valueOf(0));
                                    }
                                }
                            }
                            loadTableRequirements.reload();
                            break;
                        default:
                            System.out.println("Unrecognized ComboBox ID: " + cbId);
                            break;
                    }
                }
                if (!cbId.equals("cmbCustomerGroup")) {
                    loadRecordMaster();
                }
            } catch (GuanzonException | SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }
        });
    };

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(ClientType, cmbClientType), new JFXUtil.Pairs<>(PurchaseType, cmbPurchaseType),
                new JFXUtil.Pairs<>(CategoryType, cmbCategoryType), new JFXUtil.Pairs<>(CustomerGroup, cmbCustomerGroup));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbClientType, cmbPurchaseType, cmbCategoryType, cmbCustomerGroup);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbClientType, cmbPurchaseType, cmbCategoryType, cmbCustomerGroup);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpTargetDate, dpReceivedDate); //, dpAppliedDate, dpApprovedDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpTargetDate, dpReceivedDate); //, dpAppliedDate, dpApprovedDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtField_Focus, tfSearchClient);
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks); //, taBankAppRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfClient, tfSalesPerson, tfReferralAgent, tfInquiryType);
        JFXUtil.setFocusListener(txtDetail_Focus, tfBrand, tfModel, tfColor);

        JFXUtil.setFocusListener(txtRequirements_Focus, tfRequirement, tfReceivedBy);
        JFXUtil.setFocusListener(txtBankApplications_Focus); //, tfApplicationNo, tfBank);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail, apRequirements); //, apBankApplications);
        JFXUtil.setDisabled(oApp.getUserLevel() <= UserRight.ENCODER, tfSalesPerson);
    }

    public void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        tblViewTransDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                    moveNext(false, false);
                }
            }
        });
        tblViewRequirements.setOnMouseClicked(event -> {
            if (requirements_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnRequirements = tblViewRequirements.getSelectionModel().getSelectedIndex();
                    moveNextRequirements(false, false);
                }
            }
        });
//        tblViewBankApplications.setOnMouseClicked(event -> {
//            if (bankapplications_data.size() > 0) {
//                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
//                    pnBankApplications = tblViewBankApplications.getSelectionModel().getSelectedIndex();
//                    moveNextBankApplications(false, false);
//                }
//            }
//        });
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelSalesInquiry_Main) item).getIndex01(), highlightedRowsMain);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblViewTransDetails, tblViewRequirements, tblViewBankApplications);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails, tblViewRequirements, tblViewBankApplications); // need to use computed-size in min-width of the column to work
        JFXUtil.enableRowDragAndDrop(tblViewTransDetails, item -> ((ModelSalesInquiry_Detail) item).index01Property(),
                item -> ((ModelSalesInquiry_Detail) item).index03Property(),
                item -> ((ModelSalesInquiry_Detail) item).index04Property(), dragLock, index -> {

                    for (ModelSalesInquiry_Detail d : details_data) {
                        String brand = d.getIndex04();
                        String model = d.getIndex05();
                        String color = d.getIndex06();
                        String variant = d.getIndex07();
                        String priorityStr = d.getIndex01();
                        for (int i = 0, n = poSalesInquiryController.SalesInquiry().getDetailCount(); i < n; i++) {
                            try {
                                if (!brand.equals(poSalesInquiryController.SalesInquiry().Detail(i).getBrandId())
                                || !model.equals(poSalesInquiryController.SalesInquiry().Detail(i).getModelId())
                                || !color.equals(poSalesInquiryController.SalesInquiry().Detail(i).getColorId())
                                || !variant.equals(poSalesInquiryController.SalesInquiry().Detail(i).ModelVariant().getVariantId())) {
                                    continue;
                                }
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            }
                            try {
                                poSalesInquiryController.SalesInquiry().Detail(i).setPriority(Integer.parseInt(priorityStr));
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid priority: " + priorityStr);
                            }
                            break;
                        }
                    }
                    pnDetail = index;
                    loadTableDetail.reload();
                });

        JFXUtil.addCheckboxColumns(ModelRequirements_Detail.class, tblViewRequirements, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 1:
                            poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(rowIndex).isRequired(lbisTrue);
                            pnRequirements = rowIndex;
                            loadTableRequirements.reload();
                            break;
                        case 2:
                            poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(rowIndex).isSubmitted(lbisTrue);
                            poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(rowIndex).setReceivedBy(lbisTrue ? oApp.getUserID() : "");
                            try {
                                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                                String lsDummyDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat("01/01/1900"), SQLUtil.FORMAT_SHORT_DATE));
                                LocalDate localDate = LocalDate.parse(lsDummyDate);
                                Timestamp timestamp = Timestamp.valueOf(localDate.atStartOfDay());
                                poJSON = poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(rowIndex).setReceivedDate(lbisTrue ? oApp.getServerDate() : timestamp);
                                pnRequirements = rowIndex;
                                loadTableRequirements.reload();
                            } catch (SQLException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                    }
                }, 1, 2);
    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        dragLock.isEnabled = lbShow1; // for drag drop 
        disableRowCheckbox.set(!lbShow1); // set enable/disable in checkboxes in requirements
        // Manage visibility and managed state of other buttons
        //Update 
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);

        //Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnUpdate, btnHistory, btnConfirm, btnVoid);

        //Unkown || Ready
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail, apRequirements); //, apBankApplications);
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);

        switch (poSalesInquiryController.SalesInquiry().Master().getTransactionStatus()) {
            case SalesInquiryStatic.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnConfirm);
                if (poSalesInquiryController.SalesInquiry().Master().isProcessed()) {
                    JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
                }
                break;
            case SalesInquiryStatic.QUOTED:
            case SalesInquiryStatic.SALE:
            case SalesInquiryStatic.LOST:
            case SalesInquiryStatic.VOID:
            case SalesInquiryStatic.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnVoid);
                break;
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblBrandDetail, tblDescriptionDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);

        tblViewTransDetails.setItems(details_data);
        tblViewTransDetails.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblClient);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);

    }

    public void initRequirementsGrid() {
        JFXUtil.setColumnCenter(tblRequirementRowNo, tblReceivedDate);
        JFXUtil.setColumnLeft(tblRequired, tblSubmitted, tblRequirements, tblReceivedBy);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewRequirements);
        tblViewRequirements.setItems(requirements_data);
    }

    public void initBankApplicationsGrid() {
        JFXUtil.setColumnCenter(tblBankAppRowNo, tblBankAppNo, tblAppliedDate, tblApprovedDate, tblStatus);
        JFXUtil.setColumnLeft(tblBank);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewBankApplications);
        tblViewBankApplications.setItems(bankapplications_data);
    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView currentTable = (TableView) event.getSource();
            TablePosition focusedCell = currentTable.getFocusModel().getFocusedCell();
            int index = 0;
            if (focusedCell != null) {
                switch (event.getCode()) {
                    case TAB:
                    case DOWN:
                        index = JFXUtil.moveToNextRow(currentTable);
                        break;
                    case UP:
                        index = JFXUtil.moveToPreviousRow(currentTable);
                        break;
                    default:
                        break;
                }
                switch (currentTable.getId()) {
                    case "tblViewTransDetails":
                        pnDetail = index;
                        loadRecordDetail();
                        break;
                    case "tblViewRequirements":
                        pnRequirements = index;
                        loadRecordRequirements();
                        break;
                    case "tblViewBankApplications":
                        pnBankApplications = index;
//                        loadRecordBankApplications();
                        break;
                }
                event.consume();
            }
        }
    }

    public void clearTextFields() {
        psSearchClientId = "";
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse, apRequirements); //, apBankApplications);
    }

}
