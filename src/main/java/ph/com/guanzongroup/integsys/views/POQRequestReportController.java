/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.purchasing.controller.POQuotationRequest;
import org.guanzon.cas.purchasing.services.QuotationControllers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.integsys.model.ModelPOQRequestReport;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class POQRequestReportController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Check Printing";
    private POQuotationRequest poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchDateFrom = "";
    private String psSearchDateTo = "";

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelPOQRequestReport> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelPOQRequestReport> filteredMain_Data;

    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    JFXUtil.ReloadableTableTask loadTableMain;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList("ALL", "OPEN", "CONFIRMED", "APPROVED", "POSTED", "RETURNED", "VOID", "CANCELLED");

    int pnMain = 0;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton;
    @FXML
    private Label lblSource;
    @FXML
    private RadioButton rbSummary, rbDetailed;
    @FXML
    private ToggleGroup type;
    @FXML
    private TextField tfSearchCompany, tfSearchIndustry, tfSearchCategory, tfSearchBranch, tfSearchDestination, tfSearchDepartment, tfSearchSupplier;
    @FXML
    private DatePicker dpDateFrom, dpDateTo;
    @FXML
    private ComboBox cmbStatus;
    @FXML
    private Button btnPrint, btnRetrieve, btnClose;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblTransactionNo, tblTransactionDate, tblReferenceNo, tblBranch, tblDestination, tblDepartment, tblCategory, tblCompany, tblSupplier, tblTerm, tblTransactionTotal, tblRow, tblItem, tblUnitPrice, tblQuantity, tblTotal, tblStatus;
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
//        try {
        poController = new QuotationControllers(oApp, null).POQuotationRequest();
        poController.setTransactionStatus(DisbursementStatic.AUTHORIZED);
        poJSON = new JSONObject();
        poController.setWithUI(true);
        poJSON = poController.InitTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initLoadTable();
        initTextFields();
        initMainGrid();
        initTableOnClick();
        initButtons();
        initDatePickers();
        initToggleGroup();
        initComboBox();
        pagination.setPageCount(1);

        Platform.runLater(() -> {
            try {
                poController.Master().setIndustryId(psIndustryId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                
                if (!psIndustryId.equals(System.getProperty("sys.main.industry")) && !psIndustryId.equals(System.getProperty("sys.general.industry"))) {
                    poController.initializeFilter();
                    JFXUtil.setDisabled(true, tfSearchCompany,tfSearchIndustry,tfSearchCategory, tfSearchBranch);
                }
                
                String lsCompany = poController.getCompanyName(psCompanyId) ;
                if(lsCompany != null && !"".equals(lsCompany)){
                    lblSource.setText(lsCompany + " - " + poController.Master().Industry().getDescription());
                } else {
                    lblSource.setText(poController.Master().Industry().getDescription());
                }
                
                loadRecordSearch();
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
        boolean lbShow = rbSummary.isSelected();
        tblTransactionTotal.setVisible(lbShow);
        tblQuantity.setVisible(!lbShow);
        tblUnitPrice.setVisible(!lbShow);
        tblTotal.setVisible(!lbShow);
        tblRow.setVisible(!lbShow);
        tblItem.setVisible(!lbShow);
//        loadTableMain.reload();
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        }
    }

    public static Date getFirstDayOfMonth(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void loadRecordSearch() {
        try {
            tfSearchCompany.setText(poController.getSearchCompany());
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchCategory.setText(poController.getSearchCategory());
            tfSearchBranch.setText(poController.getSearchBranch());
            tfSearchDestination.setText(poController.getSearchDestination());
            tfSearchDepartment.setText(poController.getSearchDepartment());
            tfSearchSupplier.setText(poController.getSearchSupplier());

            //define if both are empty
            if (dpDateFrom.getEditor().getText().equals("")) {
                String lsDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                JFXUtil.setDateValue(dpDateFrom, CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
                psSearchDateFrom = CustomCommonUtil.formatLocalDateToShortString(CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
            }
            if (dpDateTo.getEditor().getText().equals("")) {
                String lsDateTo = CustomCommonUtil.formatDateToShortString(oApp.getServerDate());
                JFXUtil.setDateValue(dpDateTo, CustomCommonUtil.parseDateStringToLocalDate(lsDateTo, "yyyy-MM-dd"));
                psSearchDateTo = CustomCommonUtil.formatLocalDateToShortString(CustomCommonUtil.parseDateStringToLocalDate(lsDateTo, "yyyy-MM-dd"));
            }

            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnRetrieve":
                    loadTableMain.reload();
                    break;
                case "btnPrint":
                    poJSON = poController.printReport(rbSummary.isSelected(), psSearchDateFrom, psSearchDateTo);
//                    if (!JFXUtil.isJSONSuccess(poJSON)) {
//                        ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
//                    }
                    loadTableMain.reload();
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
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(POQRequestReportController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initToggleGroup() {
        type.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selected = (RadioButton) newToggle;
                switch (selected.getId()) {
                    case "rbSummary":
                        // tblRowNo.setVisible(false);
                        break;
                    case "rbDetailed":
                        break;
                }
                boolean lbShow = (selected.getId()).equals("rbSummary");
                tblTransactionTotal.setVisible(lbShow);
                tblQuantity.setVisible(!lbShow);
                tblUnitPrice.setVisible(!lbShow);
                tblTotal.setVisible(!lbShow);
                tblRow.setVisible(!lbShow);
                tblItem.setVisible(!lbShow);
                loadTableMain.reload();
            }
        });
    }

    private void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDateFrom, dpDateTo);
        JFXUtil.setActionListener(datepicker_Action, dpDateFrom, dpDateTo);
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtSearch_Focus, apBrowse);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    ChangeListener<Boolean> txtSearch_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchCompany":
                        if (lsValue.isEmpty()) {
                            poController.setSearchCompany("");
                        }
                        break;
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                    case "tfSearchCategory":
                        if (lsValue.isEmpty()) {
                            poController.setSearchCategory("");
                        }
                        break;
                    case "tfSearchBranch":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBranch("");
                        }
                        break;
                    case "tfSearchDestination":
                        if (lsValue.isEmpty()) {
                            poController.setSearchDestination("");
                        }
                        break;
                    case "tfSearchDepartment":
                        if (lsValue.isEmpty()) {
                            poController.setSearchDepartment("");
                        }
                        break;
                    case "tfSearchSupplier":
                        if (lsValue.isEmpty()) {
                            poController.setSearchSupplier("");
                        }
                        break;
                }
                loadRecordSearch();
                loadTableMain.reload();
            });

    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
                try {
                    switch (datePicker.getId()) {
                        case "dpDateFrom":
                            LocalDate selectedFromDate = dpDateFrom.getValue();
                            LocalDate toDate = dpDateTo.getValue();
                            if (toDate != null && selectedFromDate.isAfter(toDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                                String lsDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                                dpDateFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
                                psSearchDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                                return;
                            }
                            psSearchDateFrom = CustomCommonUtil.formatLocalDateToShortString(selectedFromDate);
                            loadTableMain.reload();
                            break;
                        case "dpDateTo":
                            LocalDate selectedToDate = dpDateTo.getValue();
                            LocalDate fromDate = dpDateFrom.getValue();
                            if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                                dpDateTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(ldCurrentDate.toString()));
                                psSearchDateTo = CustomCommonUtil.formatLocalDateToShortString(ldCurrentDate);
                                return;
                            }
                            psSearchDateTo = CustomCommonUtil.formatLocalDateToShortString(selectedToDate);
                            loadTableMain.reload();
                            break;
                        default:
                            break;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

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
                            case "tfSearchCompany":
                                poJSON = poController.SearchCompany(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            case "tfSearchIndustry":
                                poJSON = poController.SearchIndustry(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            case "tfSearchCategory":
                                poJSON = poController.SearchCategory(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            case "tfSearchBranch":
                                poJSON = poController.SearchBranch(lsValue, false, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            case "tfSearchDestination":
                                poJSON = poController.SearchDestination(lsValue, false, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            case "tfSearchDepartment":
                                poJSON = poController.SearchDepartment(lsValue, false, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            case "tfSearchSupplier":
                                poJSON = poController.SearchSupplier(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
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

    private void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            main_data.clear();
                            poJSON = poController.loadReport(rbSummary.isSelected(), psSearchDateFrom, psSearchDateTo, cmbStatus.getSelectionModel().getSelectedItem().toString());
                            if ("success".equals(poJSON.get("result"))) {
                                JSONArray unifiedPayments = (JSONArray) poJSON.get("data");
                                if (unifiedPayments != null && !unifiedPayments.isEmpty()) {
                                    for (Object requestObj : unifiedPayments) {
                                        JSONObject obj = (JSONObject) requestObj;
                                        ModelPOQRequestReport loMain = new ModelPOQRequestReport(
                                                String.valueOf(main_data.size() + 1), //Row No
                                                obj.get("sTransNox") != null ? obj.get("sTransNox").toString() : "", // Transaction No
                                                obj.get("dTransact") != null ? obj.get("dTransact").toString() : "", // Date
                                                obj.get("sReferNox") != null ? obj.get("sReferNox").toString() : "", // Reference No
                                                obj.get("xBranchNm") != null ? obj.get("xBranchNm").toString() : "", // Branch
                                                obj.get("xDestintn") != null ? obj.get("xDestintn").toString() : "", // Destination
                                                obj.get("xDeptName") != null ? obj.get("xDeptName").toString() : "", // Department
                                                obj.get("xCategLv2") != null ? obj.get("xCategLv2").toString() : "", // Category
                                                obj.get("xCompanyx") != null ? obj.get("xCompanyx").toString() : "", // Company
                                                obj.get("xSupplier") != null ? obj.get("xSupplier").toString() : "", // Supplier
                                                obj.get("xTermCode") != null ? obj.get("xTermCode").toString() : "", // Term
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(obj.get("xTranTotal") != null ? obj.get("xTranTotal").toString() : "", false), // Transaction Total
                                                obj.get("xEntryNox") != null ? obj.get("xEntryNox").toString() : "",
                                                obj.get("xDetDescx") != null ? obj.get("xDetDescx").toString() : "",
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(obj.get("xUnitPrce") != null ? obj.get("xUnitPrce").toString() : "", false),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(obj.get("xQuantity") != null ? obj.get("xQuantity").toString() : "", false),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(obj.get("xDetTotal") != null ? obj.get("xDetTotal").toString() : "", false),
                                                obj.get("xTranStat") != null ? obj.get("xTranStat").toString() : ""
                                        );

                                        main_data.add(loMain);
                                    }
                                } else {
                                    main_data.clear();
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
                            JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredMain_Data);
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                        initButtons();
                    });
                });
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbStatus":
                        loadTableMain.reload();
                        break;
                }

            });

    private void initComboBox() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlist, cmbStatus));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbStatus);
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblTransactionNo, tblTransactionDate, tblReferenceNo, tblRow);
        JFXUtil.setColumnLeft(tblBranch, tblDestination, tblDepartment, tblCategory, tblCompany, tblSupplier, tblTerm, tblItem, tblStatus);
        JFXUtil.setColumnRight(tblTransactionTotal, tblUnitPrice, tblQuantity, tblTotal);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (tblViewMainList.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                ModelPOQRequestReport selected = (ModelPOQRequestReport) tblViewMainList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                }
//                if (JFXUtil.isObjectEqualTo(selected.getIndex12(), null, "")) {
//                    ShowMessageFX.Warning("Unable to view transaction.", pxeModuleName, null);
//                    return;
//                } 
            }
        });
    }

    private void initButtons() {
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnPrint);
        disableRowCheckbox.set(main_data.isEmpty()); // set enable/disable in checkboxes in requirements
    }
}
