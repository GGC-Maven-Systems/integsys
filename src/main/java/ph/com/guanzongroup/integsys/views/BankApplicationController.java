/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelBankApplications_Detail;
import ph.com.guanzongroup.integsys.model.ModelBankApplications_Main;
import ph.com.guanzongroup.integsys.model.ModelRequirements_Detail;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.SalesInquiryStatic;
import org.guanzon.appdriver.constant.UserRight;
import ph.com.guanzongroup.cas.sales.t1.SalesBankApplication;
import ph.com.guanzongroup.cas.sales.t1.model.Model_Bank_Application;
import ph.com.guanzongroup.cas.sales.t1.status.BankApplicationStatus;

/**
 *
 * @author Team 1
 */
public class BankApplicationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0, pnMain = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesBankApplication poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    boolean pbPurchaseTypeChanged = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private ObservableList<ModelBankApplications_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelBankApplications_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelBankApplications_Main> filteredData;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEntered = false;
    ObservableList<String> ClientType = ModelSalesInquiry_Detail.ClientType;
    ObservableList<String> PurchaseType = ModelSalesInquiry_Detail.PurchaseType;
    ObservableList<String> CategoryType = ModelSalesInquiry_Detail.CategoryType;
    ObservableList<String> CustomerGroup = ModelSalesInquiry_Detail.CustomerGroup;
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain;
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private static final int ROWS_PER_PAGE = 50;
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<Model_Bank_Application> checkedItems = new ArrayList<>();
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apInquiry, apFields, apMaster, apDetail, apTableDetail;
    @FXML
    private Label lblSource, lblStatus, lblBankApplicationStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnApprove, btnDisapprove, btnCancelBankApplication, btnHistory, btnRetrieve, btnClose;
    @FXML
    private TextField tfTransactionNo, tfBranch, tfSalesPerson, tfReferralAgent, tfInquiryStatus, tfInquiryType, tfClient, tfAddress, tfContactNo, tfClientType, tfCategoryType, tfPaymentMode, tfApplicationNo, tfBank;
    @FXML
    private DatePicker dpTransactionDate, dpTargetDate, dpAppliedDate, dpApprovedDate;
    @FXML
    private ComboBox cmbPurchaseType;
    @FXML
    private TextArea taRemarks, taBankAppRemarks;
    @FXML
    private TableView tblViewDetailList, tblViewMainList;
    @FXML
    private TableColumn tblBankAppRowCb, tblBankAppRowNo, tblBankAppNo, tblBank, tblAppliedDate, tblApprovedDate, tblStatus, tblRowNo, tblDate, tblTransactionNo, tblClient, tblMainStatus;
    @FXML
    private CheckBox chckSelectAll;
    @FXML
    private Pagination pgPagination;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        poController = new SalesControllers(oApp, null).SalesBankApplication();
        poJSON = new JSONObject();
        poJSON = poController.InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initComboBoxes();
        initTextFields();
        initDatePickers();
        initDetailsGrid();
        initMainGrid();
        initTableOnClick();
        clearTextFields();
        initCheckboxes();
        initLoadTable();
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
        pgPagination.setPageCount(1);
        Platform.runLater(() -> {
            poController.Master().setIndustryId(psIndustryId);
            poController.Master().setCompanyId(psCompanyId);
            poController.Master().setCategoryCode(psCategoryId);
            poController.setIndustryId(psIndustryId);
            poController.setCompanyId(psCompanyId);
            poController.setCategoryId(psCategoryId);
            poController.setWithUI(true);
            loadRecordSearch();

        });
        JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
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
                    case "btnRetrieve":
                        //Retrieve data from purchase order to table main
                        retrieveBankApplications();
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        resetCheckboxSelection();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apDetail, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
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
                        if (poController.Detail(pnMain).getEditMode() != EditMode.READY && poController.Detail(pnMain).getEditMode() != EditMode.UPDATE) {
                            ShowMessageFX.Warning("No parameter status history to load!", pxeModuleName, null);
                            return;
                        }

                        try {
//                            poController.ShowStatusHistory(pnMain);
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No parameter status history to load!", pxeModuleName, null);
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

                                // Confirmation Prompt
                                JSONObject loJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.Master().getTransactionStatus().equals(SalesInquiryStatic.OPEN)) {
//                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
//                                            loJSON = poController.ApproveBankApplication("");
//                                            if ("success".equals((String) loJSON.get("result"))) {
//                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
//                                            } else {
//                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
//                                            }
//                                        }
                                    }
                                }
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnApprove":
                    case "btnDispprove":
                    case "btnCancelBankApplication":
                        processAction(lsButton);
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVerify", "btnApprove", "btnDisapprove", "btnCancelBankApplication")) {
                    poController.Detail().clear();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnArrowRight", "btnArrowLeft", "btnRetrieve", "btnHistory")) {
                } else {
                    loadTableDetail.reload();
                }
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void processAction(String action) {
        try {
            String lsMessage = action.startsWith("btn") ? Character.toLowerCase(action.charAt(3)) + action.substring(4) : "";

            if (checkedItem.stream().anyMatch("1"::equals)) {
            } else {
                ShowMessageFX.Warning(null, pxeModuleName, "No items were selected to " + lsMessage + ".");
                return;
            }
            if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + lsMessage + " selected item/s?")) {
                return;
            }
            checkedItems.clear();
            List<String> list = new ArrayList<>();
            for (Object item : tblViewMainList.getItems()) {
                ModelBankApplications_Detail item1 = (ModelBankApplications_Detail) item;
                String lschecked = item1.getIndex01();
                int lnReference = Integer.valueOf(item1.getIndex02()) - 1;
                if (lschecked.equals("1")) {
                    list.add(item1.getIndex06());
                    checkedItems.add(poController.Detail(lnReference));
                }
            }

            boolean lbCondition1 = true;
            //able to approve: open(0) & void(3)
            for (String value : list) {
                if (JFXUtil.isObjectEqualTo(value, poController.getStatus("0"), poController.getStatus("3"))) {
                    continue;
                }
                lbCondition1 = false;
                break;
            }
            boolean lbCondition2 = true;
            //able to disapprove: open(0)
            for (String value : list) {
                if (JFXUtil.isObjectEqualTo(value, poController.getStatus("0"))) {
                    continue;
                }
                lbCondition2 = false;
                break;
            }
            boolean lbCondition3 = true;
            //able to cancel:   approved(1) 
            for (String value : list) {
                if (JFXUtil.isObjectEqualTo(value, poController.getStatus("1"))) {
                    continue;
                }
                lbCondition3 = false;
                break;
            }

            if (checkedItems.isEmpty()) {
                return;
            }
            boolean lbAllSame = true;

            if (!list.isEmpty()) {
                String first = list.get(0);

                for (String value : list) {
                    if (!first.equals(value)) {
                        lbAllSame = false;
                        break;
                    }
                }
            }

            boolean lbMoreThanOne = checkedItems.size() > 1;
            switch (action) {
                case "btnApprove":
                    if (!lbCondition1 && lbMoreThanOne && !lbAllSame) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Unable to simultaneously " + lsMessage + " records due to statuses.");
                        return;
                    }
                    poJSON = poController.ApproveBankApplication("", checkedItems);
                    break;
                case "btnDispprove":
                    if (!lbCondition2 && lbMoreThanOne && !lbAllSame) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Unable to simultaneously " + lsMessage + " records due to statuses.");
                        return;
                    }
                    poJSON = poController.DisapproveBankApplication("", checkedItems);
                    break;
                case "btnCancelBankApplication":
                    if (!lbCondition3 && lbMoreThanOne && !lbAllSame) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Unable to simultaneously " + lsMessage + " records due to statuses.");
                        return;
                    }
                    poJSON = poController.CancelBankApplication("", checkedItems);
                    break;
                default:
                    break;
            }
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            } else {
                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                resetCheckboxSelection();
            }
//            poController.populateDetail();
            pnEditMode = poController.getEditMode();
        } catch (SQLException | GuanzonException | ParseException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void resetCheckboxSelection() {
        chckSelectAll.setSelected(false);
        if (!checkedItem.isEmpty()) {
            checkedItem.clear();
        }
    }

    public void retrieveBankApplications() {
        poJSON = new JSONObject();
        poJSON = poController.loadTransactionList("", "");
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain.reload();
        }
    }

    private String getClientType(int index) {
        if (index >= 0 && index < ModelSalesInquiry_Detail.ClientType.size()) {
            return ModelSalesInquiry_Detail.ClientType.get(index);
        }
        return "";
    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.ADDNEW;
        JFXUtil.setDisabled(!lbDisable, tfClient);
        try {
            JFXUtil.setStatusValue(lblStatus, SalesInquiryStatic.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());

            // Transaction Date
            tfTransactionNo.setText(poController.Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            String lsTargetDate = CustomCommonUtil.formatDateToShortString(poController.Master().getTargetDate());
            dpTargetDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTargetDate, "yyyy-MM-dd"));

            tfBranch.setText(poController.Master().Branch().getBranchName());
            tfSalesPerson.setText(poController.Master().SalesPerson().getFullName());
            tfReferralAgent.setText(poController.Master().ReferralAgent().getCompanyName());
            tfClient.setText(poController.Master().Client().getCompanyName());
            tfAddress.setText(poController.Master().ClientAddress().getAddress());
            tfContactNo.setText(poController.Master().ClientMobile().getMobileNo());
            tfInquiryType.setText(poController.Master().Source().getDescription());
            taRemarks.setText(poController.Master().getRemarks());

            if (pnEditMode != EditMode.UNKNOWN) {

                cmbPurchaseType.getSelectionModel().select(Integer.parseInt(poController.Master().getPurchaseType()));
                if (poController.Master().getClientId() != null && !"".equals(poController.Master().getClientId())) {
                    tfClientType.setText(getClientType(Integer.parseInt(poController.Master().Client().getClientType())));
                } else {
                    tfClientType.setText(getClientType(Integer.parseInt(poController.Master().getClientType())));
                }
            } else {
                cmbPurchaseType.getSelectionModel().select(0);
            }

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            if (pnEditMode != EditMode.READY) {
                disableRowCheckbox.set(true); // set true to disable the checkboxes in multiple rows
                JFXUtil.setDisabled(true, chckSelectAll);
                return;
            } else {
                disableRowCheckbox.set(false); // set false to enable the checkboxes in multiple rows
                JFXUtil.setDisabled(details_data.isEmpty(), chckSelectAll);
            }

            boolean lbShow1 = poController.getDetailCount() > 0;
            JFXUtil.setDisabled(!lbShow1, apDetail);
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }

            JFXUtil.setStatusValue(lblBankApplicationStatus, BankApplicationStatus.class,
                    pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Detail(pnDetail).getTransactionStatus());

            JFXUtil.setDisabled(poController.Detail(pnDetail).getEditMode() == EditMode.ADDNEW, apDetail, dpApprovedDate);

            boolean lbShow = JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getTransactionStatus(),
                    BankApplicationStatus.APPROVED, BankApplicationStatus.DISAPPROVED, BankApplicationStatus.CANCELLED);
            boolean lbShow2 = JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getEditMode(), EditMode.UPDATE);
            JFXUtil.setDisabled(lbShow || lbShow2, tfBank);

            String lsPaymentMode = "";
            if (!JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getPaymentMode(), null, "")) {
                lsPaymentMode = PurchaseType.get(Integer.valueOf(poController.Detail(pnDetail).getPaymentMode()));
            } else {
                lsPaymentMode = "";
            }
            tfPaymentMode.setText(lsPaymentMode);
            tfApplicationNo.setText(poController.Detail(pnDetail).getApplicationNo());
            tfBank.setText(poController.Detail(pnDetail).Bank().getBankName());
            taBankAppRemarks.setText(poController.Detail(pnDetail).getRemarks());

            String lsdpAppliedDate = CustomCommonUtil.formatDateToShortString(poController.Detail(pnDetail).getAppliedDate());
            dpAppliedDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsdpAppliedDate, "yyyy-MM-dd"));

            String lsdpApprovedDate = CustomCommonUtil.formatDateToShortString(poController.Detail(pnDetail).getApprovedDate());
            dpApprovedDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsdpApprovedDate, "yyyy-MM-dd"));
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
            switch (currentTableID) {
                case "tblViewDetailList":
                    if (!details_data.isEmpty()) {
                        pnDetail = newIndex;
                        loadRecordDetail();
                    }
                    break;
            }
        }
    };

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelBankApplications_Main selected = (ModelBankApplications_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String lsTransactionNo = selected.getIndex03();
                if (!JFXUtil.loadValidation(pnEditMode, pxeModuleName, poController.Master().getTransactionNo(), lsTransactionNo)) {
                    return;
                }
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);

                poJSON = poController.OpenTransaction(lsTransactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
            }
            Platform.runLater(() -> {
                loadTableDetail.reload();
            });
            pnEditMode = poController.Master().getEditMode();
            initButton(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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
                case "chckSelectAll": // this is the id
                    //set to 1 all of column 2 row data value to enable checked
                    for (int lnCtr = 0; lnCtr < checkedItem.size(); lnCtr++) {
                        if (checkedBox.isSelected()) {
                            checkedItem.set(lnCtr, "1");
                        } else {
                            checkedItem.set(lnCtr, "0");
                        }
                    }
                    loadTableMain.reload();
                    break;
            }
        }
    }

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelBankApplications_Detail.class, tblViewDetailList, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 0:
                            checkedItem.set(rowIndex, lbisTrue ? "1" : "0");
                            boolean allOnes = checkedItem.stream().allMatch("1"::equals);
                            chckSelectAll.setSelected(allOnes);
                            // if detected unchecked then must update
                            pnMain = rowIndex;
                            Platform.runLater(() -> {
                                loadTableDetail.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    if (lbisTrue) {
                                        JFXUtil.selectAndFocusRow(tblViewDetailList, rowIndex);
                                    }
                                });
                            });
                            break;
                    }
                },
                (row, rowIndex, colIndex) -> {
                    switch (colIndex) {
                        case 0:
                            ShowMessageFX.Information(null, pxeModuleName, "Checkbox is available only when the record is not in Add or Update mode.");
                            break;
                        default:
                            break;
                    }
                },
                0);//starts 0,1,2 
        JFXUtil.handleDisabledNodeClick(apTableDetail, pnEditMode, nodeID -> {
            if (nodeID.equals("chckSelectAll")) {
                if (!main_data.isEmpty()) {
                    ShowMessageFX.Information(null, pxeModuleName, "Checkbox is available only when the record is not in Add or Update mode.");
                }
            }
        });
    }

    public void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                }
            }
        });
        tblViewDetailList.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewDetailList.getSelectionModel().getSelectedIndex();
                    moveNextBankApplications(false, false);
                }
            }
        });
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetailList);
        JFXUtil.adjustColumnForScrollbar(tblViewDetailList); // need to use computed-size in min-width of the column to work

        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelBankApplications_Main) item).getIndex01(), highlightedRowsMain);
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

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        main_data.clear();
                        Platform.runLater(() -> {
                            JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                            if (poController.getSalesInquiryCount() > 0) {
                                for (int lnCtr = 0; lnCtr <= poController.getSalesInquiryCount() - 1; lnCtr++) {
                                    try {
                                        String lsDate = CustomCommonUtil.formatDateToShortString(poController.SalesInquiryList(lnCtr).getTransactionDate());
                                        main_data.add(new ModelBankApplications_Main(String.valueOf(lnCtr + 1),
                                                String.valueOf(lsDate),
                                                String.valueOf(poController.SalesInquiryList(lnCtr).getTransactionNo()),
                                                String.valueOf(poController.SalesInquiryList(lnCtr).Client().getCompanyName()),
                                                String.valueOf(poController.getStatus(poController.SalesInquiryList(lnCtr).getTransactionStatus()))
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
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetailList,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                String lsAppliedDate = CustomCommonUtil.formatDateToShortString(poController.Detail(lnCtr).getAppliedDate());
                                String lsApprovedDate = CustomCommonUtil.formatDateToShortString(poController.Detail(lnCtr).getApprovedDate());
                                String lsStat = ""; //default
                                lsStat = JFXUtil.setStatusValue(lblBankApplicationStatus, BankApplicationStatus.class,
                                        pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Detail(lnCtr).getTransactionStatus());

                                String lsBank = JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).Bank().getBankName(), null, "")
                                        ? "" : poController.Detail(lnCtr).Bank().getBankName();
                                checkedItems(lnCtr);
                                details_data.add(
                                        new ModelBankApplications_Detail(checkedItem.get(lnCtr), String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.Detail(lnCtr).getApplicationNo()),
                                                String.valueOf(lsBank),
                                                String.valueOf(lsAppliedDate),
                                                String.valueOf(lsApprovedDate),
                                                String.valueOf(lsStat)
                                        ));
                            }
                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetailList, 0);
                                    pnDetail = tblViewDetailList.getSelectionModel().getSelectedIndex();
                                }
                                loadRecordDetail();
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetailList, pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });

    }

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
                    case "taBankAppRemarks"://Remarks
                        poJSON = poController.Detail(pnDetail).setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        loadRecordDetail();
                        break;
                }
            }
    );

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfApplicationNo":
                        poJSON = poController.Detail(pnDetail).setApplicationNo(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        JFXUtil.runWithDelay(0.70, () -> loadTableDetail.reload());
                        break;
                    case "tfBank":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.Detail(pnDetail).setBankId(lsValue);
                            JFXUtil.runWithDelay(0.70, () -> loadTableDetail.reload());
                        }
                        break;
                }
                loadRecordDetail();
            }
    );

    public void moveNextBankApplications(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDetail.requestFocus();
            pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewDetailList) : JFXUtil.moveToNextRow(tblViewDetailList);
        }
        loadRecordDetail();
        if (JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getApplicationNo(), null, "")) {
            tfApplicationNo.requestFocus();
        } else if (JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getBankId(), null, "")) {
            tfBank.requestFocus();
        } else {
            tfApplicationNo.requestFocus();
        }
    }

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
                    switch (lsID) {
                        case "tfApplicationNo":
                        case "tfBank":
                            moveNextBankApplications(true, true);
                            event.consume();
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfApplicationNo":
                        case "tfBank":
                            moveNextBankApplications(false, true);
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfBank":
                            poJSON = poController.SearchBank(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfBank.setText("");
                                break;
                            }
                            loadTableDetail.reload();
                            break;
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
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                String lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                LocalDate ldTransactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                poJSON = new JSONObject();
//                try {
                switch (datePicker.getId()) {
                    case "dpAppliedDate":
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Applied date cannot be before the transaction date.");
                            pbSuccess = false;
                        } else {
                            poController.Detail(pnDetail).setAppliedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                        }
                        if (pbSuccess) {
                        } else {
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                        pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                        loadTableDetail.reload();
                        pbSuccess = true; //Set to original value
                        break;
                    case "dpApprovedDate":
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Approved date cannot be before the transaction date.");
                            pbSuccess = false;
                        } else {
                            poController.Detail(pnDetail).setApprovedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                        }
                        if (pbSuccess) {
                        } else {
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                        pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                        loadTableDetail.reload();
                        pbSuccess = true; //Set to original value
                        break;
                    default:
                        break;
                }
//                } catch (SQLException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                }
            });

    final EventHandler<ActionEvent> comboBoxActionListener = event -> {
        Platform.runLater(() -> {
//            try {
            Object source = event.getSource();
            @SuppressWarnings("unchecked")
            ComboBox<?> cb = (ComboBox<?>) source;

            String cbId = cb.getId();
            int selectedIndex = cb.getSelectionModel().getSelectedIndex();
            switch (cbId) {
                case "cmbPurchaseType":
                    if (poController.getDetailCount() > 0) {
                        if (!poController.Master().getPurchaseType().equals(String.valueOf(cmbPurchaseType.getSelectionModel().getSelectedIndex()))) {
                            poJSON = poController.checkPendingBankApplication();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }

                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                    "Are you sure you want to change the Purchase Type?\nPlease note that this action will reset the Bank Applications list.\n\nDo you wish to proceed?") == true) {
                                poController.Master().setPurchaseType(String.valueOf(selectedIndex));
                                poController.Detail().clear();
//                                    if ("error".equals((String) poJSON.get("result"))) {
//                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                        break;
//                                    }
                                loadTableDetail.reload();
                                pbPurchaseTypeChanged = true;
                            }
                        }
                    } else {
                        poController.Master().setPurchaseType(String.valueOf(selectedIndex));
                    }
                    break;
                default:
                    System.out.println("Unrecognized ComboBox ID: " + cbId);
                    break;
            }

            if (!cbId.equals("cmbCustomerGroup")) {
                loadRecordMaster();
            }
//            } catch (GuanzonException | SQLException ex) {
//                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            }
        });
    };

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(PurchaseType, cmbPurchaseType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbPurchaseType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbPurchaseType);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpTargetDate, dpAppliedDate, dpApprovedDate);
        JFXUtil.setActionListener(datepicker_Action, dpTransactionDate, dpTargetDate, dpAppliedDate, dpApprovedDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks, taBankAppRemarks);
//        JFXUtil.setFocusListener(txtMaster_Focus, tfClient, tfSalesPerson, tfReferralAgent, tfInquiryType);

        JFXUtil.setFocusListener(txtDetail_Focus, tfApplicationNo, tfBank);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setDisabled(oApp.getUserLevel() <= UserRight.ENCODER, tfSalesPerson);
    }

    private boolean hasValidDetail() {
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return false;
        }
        if (JFXUtil.isObjectEqualTo(poController.Detail(0).getBankId(), null, "")) {
            return false;
        }
        return true;
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        disableRowCheckbox.set(!lbShow); // set enable/disable in checkboxes in requirements
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow, taRemarks, apDetail);

        JFXUtil.setDisabledExcept(true, apMaster, cmbPurchaseType);
        JFXUtil.setDisabled(!lbShow, cmbPurchaseType);
        JFXUtil.setButtonsVisibility(false, btnApprove, btnDisapprove, btnCancelBankApplication);
        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case SalesInquiryStatic.OPEN:
                JFXUtil.setButtonsVisibility(hasValidDetail(), btnApprove, btnCancelBankApplication);
                JFXUtil.setButtonsVisibility(false, btnDisapprove);
                break;
            case SalesInquiryStatic.CONFIRMED:
                JFXUtil.setButtonsVisibility(hasValidDetail(), btnDisapprove);
                JFXUtil.setButtonsVisibility(false, btnApprove, btnCancelBankApplication);
                break;
            case SalesInquiryStatic.VOID:
            case SalesInquiryStatic.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove, btnDisapprove, btnCancelBankApplication);
                break;
        }
        switch (poController.Master().getTransactionStatus()) {
            case SalesInquiryStatic.QUOTED:
            case SalesInquiryStatic.SALE:
            case SalesInquiryStatic.LOST:
            case SalesInquiryStatic.VOID:
            case SalesInquiryStatic.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblBankAppRowNo, tblBankAppNo, tblAppliedDate, tblApprovedDate, tblStatus);
        JFXUtil.setColumnLeft(tblBank);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetailList);
        tblViewDetailList.setItems(details_data);
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblTransactionNo);
        JFXUtil.setColumnLeft(tblClient, tblMainStatus);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void clearTextFields() {
        resetCheckboxSelection();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse, apDetail);
    }
}
