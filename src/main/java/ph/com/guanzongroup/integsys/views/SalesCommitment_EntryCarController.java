/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.parser.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.scene.control.Pagination;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.SalesCommitment;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.BankApplicationStatus;
import ph.com.guanzongroup.integsys.model.ModelSalesCommitment_Detail;
import ph.com.guanzongroup.integsys.model.ModelSalesCommitment_Main;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;

/**
 *
 * @author Team 1
 */
public class SalesCommitment_EntryCarController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0, pnMain = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesCommitment poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    boolean pbPurchaseTypeChanged = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private ObservableList<ModelSalesCommitment_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelSalesCommitment_Main> main_data = FXCollections.observableArrayList();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEntered = false;

    JFXUtil.ReloadableTableTask loadTableMain;
    private static final int ROWS_PER_PAGE = 50;
    private FilteredList<ModelSalesCommitment_Main> filteredData;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apInquiry, apFields, apMaster;
    @FXML
    private Label lblSource, lblStatus, lblBankApplicationStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnCancelBankApplication, btnHistory, btnRetrieve, btnClose;
    @FXML
    private TextField tfTransNo, tfClientType, tfClient, tfAddress, tfBranch, tfSalesPerson, tfReferralAgent, tfPriorityUnit, tfApplicationNo, tfATDNumber, tfPaymentMode, tfTerm, tfBank, tfInquiryNo, tfInquiryType, tfUnitType, tfSalesAmount, tfVATRate, tfVatSales, tfVatAmount, tfTransactionTotal;
    @FXML
    private DatePicker dpTransactionDate, dpAppliedDate, dpDueDate, dpInquiryDate, dpTargetDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblNoViewMainList, tblInquiryDate, tblTransactionNo, tblClient, tblStatus;
    @FXML
    private Pagination pgPagination;

    @Override

    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).SalesCommitment();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                System.err.println((String) poJSON.get("message"));
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            pgPagination.setPageCount(1);
            initLoadTable();
            initTextFields();
            initDatePickers();
            initMainGrid();
            initTableOnClick();
            clearTextFields();
            pnEditMode = poController.getEditMode();
            initButton(pnEditMode);

            Platform.runLater(() -> {
                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.Master().setCategoryCode(psCategoryId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
                loadRecordSearch();

                btnNew.fire();
            });
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poController.setTransactionStatus(BankApplicationStatus.OPEN);
                        poJSON = poController.SearchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransNo.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        //Clear data
                        clearTextFields();
                        poJSON = poController.InitTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            poController.resetMaster();
                            poController.Detail().clear();
                            clearTextFields();

                            poController.Master().setIndustryId(psIndustryId);
                            poController.Master().setCompanyId(psCompanyId);
                            poController.Master().setCategoryCode(psCategoryId);
                            poController.initFields();
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
                            return;
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
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                Platform.runLater(() -> {
                                    btnNew.fire();
                                });
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnCancelBankApplication":
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to cancel the transaction?") == true) {
                            poJSON = poController.CancelTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            }

                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            Platform.runLater(() -> {
                                btnNew.fire();
                            });
                        }
                        return;
                    case "btnRetrieve":
                        retrieveSalesInquiry();
                        break;

                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnCancelBankApplication")) {
                    poController.InitTransaction();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                } else {
                    loadRecordMaster();
                }

                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void retrieveSalesInquiry() {
        try {
            poJSON = new JSONObject();
            poJSON = poController.loadSalesInquiryList(tfClient.getText());
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            } else {
                loadTableMain.reload();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.ADDNEW;
        JFXUtil.setDisabled(!lbDisable, tfClient);
        try {
            JFXUtil.setStatusValue(lblStatus, BankApplicationStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            poController.computeFields(false);

            lblBankApplicationStatus.setText(poController.getStatus(pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus()).toUpperCase());
            tfTransNo.setText(poController.Master().getTransactionNo());
            tfClient.setText(poController.Master().Client().getCompanyName());
            tfAddress.setText(poController.Master().ClientAddress().getAddress());
            tfInquiryNo.setText(poController.Master().getSourceNo());
            dpTransactionDate.setValue(poController.Master().getTransactionDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            tfApplicationNo.setText(poController.Master().getPONumber());
            tfBank.setText(poController.Master().Bank().getBankName());
            tfTerm.setText(poController.Master().Term().getDescription());
            tfATDNumber.setText(poController.Master().getATDNumber());
            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotal(), true));

            tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATAmount(), true));
            tfVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATSale(), true));
            tfVATRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATRates(), true));
            tfSalesAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getSalesAmount(), true));
            taRemarks.setText(poController.Master().getRemarks());
            dpAppliedDate.setValue(poController.Master().getAppliedDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getAppliedDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            dpDueDate.setValue(poController.Master().getDueDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getDueDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);

            tfPriorityUnit.setText(poController.getPriorityUnit());
            if (poController.Master().getSourceNo() != null && !"".equals(poController.Master().getSourceNo())) {
                tfBranch.setText(poController.Master().Inquiry().Branch().getBranchName());
                tfSalesPerson.setText(poController.Master().Inquiry().SalesPerson().getFullName());
                tfReferralAgent.setText(poController.Master().Inquiry().ReferralAgent().getCompanyName());
                tfInquiryType.setText(poController.Master().Inquiry().Source().getDescription());
                tfClientType.setText(getClientType(Integer.parseInt(poController.Master().Inquiry().getClientType())));
                tfUnitType.setText(getCategoryType(Integer.parseInt(poController.Master().Inquiry().getCategoryType())));
                tfPaymentMode.setText(getPaymentmode(Integer.parseInt(poController.Master().getPaymentMode())));
                dpInquiryDate.setValue(poController.Master().Inquiry().getTransactionDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().Inquiry().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
                dpTargetDate.setValue(poController.Master().Inquiry().getTargetDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().Inquiry().getTargetDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);

            } else {
                JFXUtil.clearNodes(dpInquiryDate, dpTargetDate, tfInquiryType, tfClientType, tfUnitType, tfPaymentMode,
                        tfBranch, tfSalesPerson, tfReferralAgent);
            }

            JFXUtil.updateCaretPositions(apMaster);

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private String getClientType(int index) {
        if (index >= 0 && index < ModelSalesInquiry_Detail.ClientType.size()) {
            return ModelSalesInquiry_Detail.ClientType.get(index);
        }
        return "";
    }

    private String getCategoryType(int index) {
        if (index >= 0 && index < ModelSalesInquiry_Detail.CategoryType.size()) {
            return ModelSalesInquiry_Detail.CategoryType.get(index);
        }
        return "";
    }

    private String getPaymentmode(int index) {
        if (index >= 0 && index < ModelSalesInquiry_Detail.PurchaseType.size()) {
            return ModelSalesInquiry_Detail.PurchaseType.get(index);
        }
        return "";
    }

    public void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0 && event.getClickCount() == 2) {
                loadTableDetailFromMain();
                initButton(pnEditMode);
            }
        }
        );
        JFXUtil.adjustColumnForScrollbar(tblViewMainList); // need to use computed-size in min-width of the column to work
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();
        if (pnEditMode == EditMode.ADDNEW) {  //Do not allow to link when edit mode is not equal to add new
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            ModelSalesCommitment_Main selected = (ModelSalesCommitment_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                    String lsTransactionNo = selected.getIndex03();

                    if (!JFXUtil.loadValidation2(pnEditMode, pxeModuleName, poController.Master().getSourceNo(), lsTransactionNo, poController.Master().getTransactionTotal())) {
                        return;
                    }
                    pnMain = pnRowMain;
                    JFXUtil.clearTextFields(apMaster);
                    poJSON = poController.populateDetail(lsTransactionNo);
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    loadRecordMaster();

                    JFXUtil.runWithDelay(0.50, () -> {
                        loadTableMain.reload();
                    });
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            }
        } else {
            ShowMessageFX.Warning(null, pxeModuleName, "Data can only be inserted when in ADD mode.");
        }
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            main_data.clear();
                            if (poController.getSalesInquiryCount() > 0) {
                                for (int lnCtr = 0; lnCtr <= poController.getSalesInquiryCount() - 1; lnCtr++) {
                                    try {
                                        String lsDate = CustomCommonUtil.formatDateToShortString(poController.SalesInquiryList(lnCtr).getTransactionDate());
                                        main_data.add(new ModelSalesCommitment_Main(String.valueOf(lnCtr + 1),
                                                String.valueOf(lsDate),
                                                String.valueOf(poController.SalesInquiryList(lnCtr).getTransactionNo()),
                                                String.valueOf(poController.SalesInquiryList(lnCtr).Client().getCompanyName()),
                                                String.valueOf(poController.getStatus(poController.SalesInquiryList(lnCtr).getTransactionStatus()).toUpperCase())
                                        ));

                                    } catch (SQLException | GuanzonException ex) {
                                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                });
    }

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
                    case "taRemarks"://Remarks
                        poJSON = poController.Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        loadRecordMaster();
                        break;
                }
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfClient":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.Master().setClientId("");
                        }
                        break;
                    case "tfApplicationNo": //po no
                        poJSON = poController.Master().setPONumber(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfBank":
                        if (lsValue.isEmpty()) {
                            poController.Master().setBankId("");
                        }
                        break;
                    case "tfTerm":
                        if (lsValue.isEmpty()) {
                            poController.Master().setTermCode("");
                        }
                        break;
                    case "tfATDNumber":
                        poJSON = poController.Master().setATDNumber(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;

                    case "tfVatAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Master().setVATAmount(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfVatSales":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Master().setVATSale(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfVATRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Master().setVATRates(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfSalesAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Master().setSalesAmount(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;

                }
                loadRecordMaster();
            });

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    break;
                case DOWN:
                    break;
                case F3:
                    switch (lsID) {
                        //apMaster
                        case "tfClient":
                            poJSON = poController.SearchClient(lsValue, false, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfClient.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfApplicationNo);
                            }
                            loadRecordMaster();
                            retrieveSalesInquiry();
                            return;
                        case "tfTerm":
                            poJSON = poController.SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfBank);
                            }
                            loadRecordMaster();
                            break;
                        case "tfBank":
                            poJSON = poController.SearchBank(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfVATRate);
                            }
                            loadRecordMaster();

                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                String lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                LocalDate ldTransactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                poJSON = new JSONObject();
                switch (datePicker.getId()) {
                    case "dpInquiryDate":
                        break;
                    case "dpTargetDate":
                        break;
                    case "dpTransactionDate":
                        if (ldSelectedDate.isAfter(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Future date is not allowed.");
                            pbSuccess = false;
                        } else {
                            poController.Master().setAppliedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpAppliedDate":
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Applied date cannot be before the transaction date.");
                            pbSuccess = false;
                        } else if (ldSelectedDate.isAfter(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Future date is not allowed.");
                            pbSuccess = false;
                        } else {
                            poController.Master().setAppliedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpDueDate":
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Due date cannot be before the transaction date.");
                            pbSuccess = false;
                        } else if (poController.Master().getAppliedDate() != null) {
                            String lsAppliedDate = sdfFormat.format(poController.Master().getAppliedDate());
                            LocalDate ldAppliedDate = LocalDate.parse(lsAppliedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            if (ldSelectedDate.isBefore(ldAppliedDate)) {
                                JFXUtil.setJSONError(poJSON, "Due date cannot be before the applied date.");
                                pbSuccess = false;
                            } else {
                                poController.Master().setDueDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
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

    public void initDatePickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpInquiryDate, dpTargetDate, dpTransactionDate, dpAppliedDate, dpDueDate);
        JFXUtil.setActionListener(datepicker_Action, dpInquiryDate, dpTargetDate, dpTransactionDate, dpAppliedDate, dpDueDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfClient, tfApplicationNo, tfATDNumber, tfTerm, tfBank, tfSalesAmount, tfVATRate, tfVatSales, tfVatAmount);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster);
        JFXUtil.inputDecimalOnly(tfVATRate);
        JFXUtil.setCommaFormatter(tfSalesAmount, tfVatAmount, tfVatSales);

        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnCancelBankApplication);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster);
        JFXUtil.setButtonsVisibility(true, btnRetrieve);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case BankApplicationStatus.OPEN:
                JFXUtil.setButtonsVisibility(true, btnUpdate, btnCancelBankApplication);
                break;
        }
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblNoViewMainList, tblInquiryDate, tblTransactionNo);
        JFXUtil.setColumnLeft(tblClient, tblStatus);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        tblViewMainList.setItems(main_data);
        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apBrowse);
    }
}
