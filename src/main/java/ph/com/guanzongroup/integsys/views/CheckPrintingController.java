/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelCheckPrinting;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * FXML Controller class
 *
 * @author Team 1 & Team 2
 */
public class CheckPrintingController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Check Printing";
    private DisbursementVoucher poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchBankID = "";
    private String psSearchBankAccountID = "";
    private String psSearchDVDateFrom = "";
    private String psSearchDVDateTo = "";

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelCheckPrinting> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelCheckPrinting> filteredMain_Data;

    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    JFXUtil.StageManager stageDV = new JFXUtil.StageManager();
    JFXUtil.StageManager stageAssignment = new JFXUtil.StageManager();
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    JFXUtil.ReloadableTableTask loadTableMain;
    int pnMain = 0;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchIndustry, tfSearchBankName, tfSearchBankAccount;
    @FXML
    private DatePicker dpDVDateFrom, dpDVDateTo;
    @FXML
    private Button btnAssign, btnRetrieve, btnPrintDV, btnPrintCheck, btnClose, btnPrintPaymentSummary;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblCheckBox, tblDVNo, tblDVDate, tblSupplier, tblPayeeName, tblBankName, tblBankAccount, tblCheckNo, tblCheckDate, tblTotalAmount;
    @FXML
    private CheckBox chckSelectAll;
    @FXML
    private Pagination pagination;

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

    /**
     * Initializes the controller class.
     */
    //Check Printing In-house
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new CashflowControllers(oApp, null).DisbursementVoucher();
            poController.setTransactionStatus(DisbursementStatic.AUTHORIZED);
            poJSON = new JSONObject();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction();
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initButtonsClickActions();
            initTextFields();
            initMainGrid();
            initTableOnClick();
            initCheckboxes();
            initButtons();
            initDatePickers();
            pagination.setPageCount(1);

            Platform.runLater(() -> {
                try {
                    poController.Master().setIndustryID(psIndustryId);
                    poController.Master().setCompanyID(psCompanyId);
                    poController.setIndustryID(psIndustryId);
                    poController.setCompanyID(psCompanyId);
                    poController.setCategoryID(psCategoryId);
                    loadRecordSearch();
                    lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            });
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
        JFXUtil.addCheckboxColumns(ModelCheckPrinting.class, tblViewMainList, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 1:
                            checkedItem.set(rowIndex, lbisTrue ? "1" : "0");
                            //set external temporary data of index to save as reference
                            // if detected unchecked then must update
                            pnMain = rowIndex;
                            Platform.runLater(() -> {
                                loadTableMain.reload();
                                JFXUtil.runWithDelay(0.30, () -> {
                                    if (lbisTrue) {
                                        JFXUtil.selectAndFocusRow(tblViewMainList, rowIndex);
                                    }
                                });
                            });
                            break;
                    }
                }, 1);//starts 0,1,2 
    }

    public static Date getFirstDayOfMonth(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Set to first day of the same month and year
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Optional: Reset time to start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private void loadRecordSearch() {
        try {
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchBankName.setText(poController.getSearchBankName());
            tfSearchBankAccount.setText(poController.getSearchBankAccountNo());
            //define if both are empty
            if (dpDVDateFrom.getEditor().getText().equals("")) {
                String lsDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                JFXUtil.setDateValue(dpDVDateFrom, CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
                psSearchDVDateFrom = CustomCommonUtil.formatLocalDateToShortString(CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
            }
            if (dpDVDateTo.getEditor().getText().equals("")) {
                String lsDateTo = CustomCommonUtil.formatDateToShortString(oApp.getServerDate());
                JFXUtil.setDateValue(dpDVDateTo, CustomCommonUtil.parseDateStringToLocalDate(lsDateTo, "yyyy-MM-dd"));
                psSearchDVDateTo = CustomCommonUtil.formatLocalDateToShortString(CustomCommonUtil.parseDateStringToLocalDate(lsDateTo, "yyyy-MM-dd"));
            }

            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnAssign, btnRetrieve, btnClose, btnPrintCheck, btnPrintDV, btnPrintPaymentSummary);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnAssign":
                handleDisbursementAction("assign");
                break;
            case "btnPrintCheck":
                handleDisbursementAction("print check");
                break;
            case "btnPrintDV":
                handleDisbursementAction("print dv");
                break;
            case "btnPrintPaymentSummary":
                handleDisbursementAction("print payment summary");
                break;
            case "btnRetrieve":
                retrieveDisbursement();
                break;
            case "btnClose":
                if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                    poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                }
                break;
            default:
                ShowMessageFX.Warning("Button is not registered, Please contact admin to assist about the unregistered button", pxeModuleName, null);
                break;
        }
        initButtons();
    }

    private void handleDisbursementAction(String action) {
        try {
            if (checkedItem.stream().anyMatch("1"::equals)) {
            } else {
                ShowMessageFX.Warning(null, pxeModuleName, "No items were selected to " + action + ".");
                return;
            }

            if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + action + " selected item/s?")) {
                return;
            }

            String firstBank = null;
            boolean allSameBank = true;
            checkedItems.clear();
            for (Object item : tblViewMainList.getItems()) {
                ModelCheckPrinting item1 = (ModelCheckPrinting) item;
                String lschecked = item1.getIndex02();
                String lsDVNO = item1.getIndex12();
                String banks = item1.getIndex07();

                if (lschecked.equals("1")) {
                    if (firstBank == null) {
                        firstBank = banks;
                    } else if (!firstBank.equals(banks)) {
                        allSameBank = false;
                        break;
                    }
                    checkedItems.add(lsDVNO);
                    System.out.println("check items : " + checkedItems.get(checkedItems.size() - 1));
                }
            }
            if (!allSameBank) {
                ShowMessageFX.Warning(null, pxeModuleName, "Selected items must belong to the same bank.");
                return;
            }

            switch (action) {
                case "assign":
                    if (!checkedItems.isEmpty()) {
                        showAssignWindow(checkedItems);
                    }
                    break;
                case "print check":
                    if (!checkedItems.isEmpty()) {
                        poJSON = poController.PrintCheck(checkedItems);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                        }


                        chckSelectAll.setSelected(false);
                        checkedItem.clear();
                    }
                    retrieveDisbursement();
                    loadTableMain.reload();
                    break;
                case "print dv":
                    if (!checkedItems.isEmpty()) {
                        poJSON = poController.printTransaction(checkedItems);
                        chckSelectAll.setSelected(false);
                        checkedItem.clear();
                    }
                    retrieveDisbursement();
                    loadTableMain.reload();
                    break;
                case "print payment summary":
                    if (!checkedItems.isEmpty()) {
                        poJSON = poController.printTransactionPaymentSummary(checkedItems);
                        chckSelectAll.setSelected(false);
                        checkedItem.clear();
                    }
                    retrieveDisbursement();
                    loadTableMain.reload();
                    break;
                default:
                    throw new AssertionError();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDVDateFrom, dpDVDateTo);
        JFXUtil.setActionListener(this::datepicker_Action, dpDVDateFrom, dpDVDateTo);
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtSearch_Focus, tfSearchIndustry, tfSearchBankName, tfSearchBankAccount);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    ChangeListener<Boolean> txtSearch_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                    case "tfSearchBankName":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBankName("");
                            poController.setSearchBankAccountNo("");
                            psSearchBankID = "";
                            psSearchBankAccountID = "";
                        }
                        break;
                    case "tfSearchBankAccount":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBankAccountNo("");
                            psSearchBankAccountID = "";
                        }
                        break;
                }
                loadRecordSearch();
            });

    private void datepicker_Action(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            JFXUtil.setJSONSuccess(poJSON, "success");
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                LocalDate dateNow = LocalDate.now();
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }
                switch (datePicker.getId()) {
                    case "dpDVDateFrom":
                        LocalDate selectedFromDate = dpDVDateFrom.getValue();
                        LocalDate toDate = dpDVDateTo.getValue();
                        if (toDate != null && selectedFromDate.isAfter(toDate)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                            String lsDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                            dpDVDateFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
                            psSearchDVDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                            return;
                        }
                        psSearchDVDateFrom = CustomCommonUtil.formatLocalDateToShortString(selectedFromDate);
                        retrieveDisbursement();
                        break;
                    case "dpDVDateTo":
                        LocalDate selectedToDate = dpDVDateTo.getValue();
                        LocalDate fromDate = dpDVDateFrom.getValue();
                        if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                            dpDVDateTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
                            psSearchDVDateTo = CustomCommonUtil.formatLocalDateToShortString(dateNow);
                            return;
                        }
                        psSearchDVDateTo = CustomCommonUtil.formatLocalDateToShortString(selectedToDate);
                        retrieveDisbursement();
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();

        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        CommonUtils.SetNextFocus(txtField);
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearchIndustry":
                                poJSON = poController.SearchIndustry(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
                                break;
                            case "tfSearchBankName":
                                poJSON = poController.SearchBanks(lsValue, false, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
                                psSearchBankID = poController.CheckPayments().getModel().getBankID();
                                break;
                            case "tfSearchBankAccount":
                                poJSON = poController.SearchBankAccount(lsValue, poController.CheckPayments().getModel().getBankID(), false, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
                                psSearchBankAccountID = poController.CheckPayments().getModel().getBankAcountID();
                                break;
                        }
                        event.consume();
                    default:
                        break;
                }
            } catch (GuanzonException | SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void retrieveDisbursement() {
        try {
//            dpDVDateFrom.getse, dpDVDateTo
            if (dpDVDateFrom.getEditor().getText().equals("") || dpDVDateTo.getEditor().getText().equals("")) {
                ShowMessageFX.Warning(null, pxeModuleName, "Please select both the From and To dates to retrieve/reload the disbursement records.");
                loadRecordSearch();
                return;
            }

            poJSON = poController.loadCheckPrintTransactionList(tfSearchIndustry.getText(), tfSearchBankName.getText(), tfSearchBankAccount.getText(), psSearchDVDateFrom, psSearchDVDateTo);
            if ("error".equals(poJSON.get("result"))) {
//                ShowMessageFX.Error(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
            } else {
                Platform.runLater(() -> {
                    chckSelectAll.setSelected(false);
                    checkedItem.clear();
                    for (int lnCntr = 0; lnCntr < poController.getMasterList().size(); lnCntr++) {
                        checkedItem.add("0");
                    }
                });
            }
            loadTableMain.reload();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            main_data.clear();
                            if (poController.getMasterList().size() > 0) {
                                
                                for (int lnCntr = 0; lnCntr < poController.getMasterList().size(); lnCntr++) {
                                    String lsSupplier = poController.getMaster(lnCntr).Payee().APClient().getCompanyName();
                                    if(lsSupplier == null || "".equals(lsSupplier)){
                                        lsSupplier = poController.getMaster(lnCntr).Payee().Client().getCompanyName();
                                    }
                                    main_data.add(new ModelCheckPrinting(
                                            String.valueOf(lnCntr + 1),
                                            checkedItem.get(lnCntr),
                                            poController.getMaster(lnCntr).getVoucherNo(),
                                            CustomCommonUtil.formatDateToShortString(poController.getMaster(lnCntr).getTransactionDate()),
                                            lsSupplier,
                                            poController.getMaster(lnCntr).Payee().getPayeeName(),
                                            poController.getMaster(lnCntr).CheckPayments().Banks().getBankName(),
                                            poController.getMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo(),
                                            poController.getMaster(lnCntr).CheckPayments().getCheckNo(),
                                            CustomCommonUtil.formatDateToShortString(poController.getMaster(lnCntr).getTransactionDate()),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getMaster(lnCntr).getNetTotal(), true),
                                            poController.getMaster(lnCntr).getTransactionNo()
                                    ));
                                }
                            } else {
                                checkedItem.clear();
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
                            JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredMain_Data);
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                        initButtons();
                    });
                });
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDVDate, tblCheckNo, tblCheckDate);
        JFXUtil.setColumnLeft(tblCheckBox, tblSupplier, tblPayeeName, tblBankName, tblBankAccount);
        JFXUtil.setColumnRight(tblTotalAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (tblViewMainList.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                try {
                    ModelCheckPrinting selected = (ModelCheckPrinting) tblViewMainList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                    }
                    if (JFXUtil.isObjectEqualTo(selected.getIndex12(), null, "")) {
                        ShowMessageFX.Warning("Unable to view transaction.", pxeModuleName, null);
                        return;
                    } else {
                        showDVWindow(selected.getIndex12());
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            }
        });
    }

    private void initButtons() {
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnAssign, btnPrintCheck, btnPrintDV, btnPrintPaymentSummary);
        disableRowCheckbox.set(main_data.isEmpty()); // set enable/disable in checkboxes in requirements
        JFXUtil.setDisabled(main_data.isEmpty(), chckSelectAll);
    }

    public void showDVWindow(String fsTransactionNo) throws SQLException {
        poJSON = new JSONObject();
        stageDV.closeDialog();

        DisbursementVoucher_ViewController controller = new DisbursementVoucher_ViewController();
        controller.setGRider(oApp);
        controller.setDisbursement(poController);
        controller.setTransaction(fsTransactionNo);
        try {
            stageDV.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/DisbursementVoucher_View.fxml"), controller,
                    "Disbursement Dialog", true, true, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void showAssignWindow(List<String> fsTransactionNos) throws SQLException {
        poJSON = new JSONObject();
        stageAssignment.closeDialog();

        CheckAssignmentController controller = new CheckAssignmentController();
        controller.setGRider(oApp);
        controller.setCheckPrinting(poController);
        controller.setTransaction(fsTransactionNos);
        try {
            stageAssignment.setOnHidden(event -> {
                chckSelectAll.setSelected(false);
                retrieveDisbursement();
                loadTableMain.reload();
                checkedItem.clear();
            });
            stageAssignment.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/CheckAssignment.fxml"), controller,
                    "Check Assignment Dialog", true, true, false);

        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
}
