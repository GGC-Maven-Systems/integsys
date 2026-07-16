package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.RecurringExpenseSchedule;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.model.ModelRecurringExpenseSchedule_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class RecurringExpenseScheduleController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static RecurringExpenseSchedule poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private boolean pbEntered = false;
    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster, apDetail, apBrowse;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnClose;
    @FXML
    private TextField tfRecurringID, tfPayee, tfParticular, tfBranchName, tfAccountNo, tfAccountName, tfDeparment, tfEmployee, tfBillDay, tfDueDay, tfAmount, tfCompany, tfSearchBranch, tfSearchPayee;
    @FXML
    private ComboBox cmbAccountable, cmbBillingFrequency;
    @FXML
    private DatePicker dpDateFrom;
    @FXML
    private CheckBox cbExcluded, cbActive;
    @FXML
    private TextArea taRemarks1;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblDetailRow, tblDetailBranch, tblDetailAccountNo, tblDetailAmount, tblDetailExcluded, tblDetailStatus;
    @FXML
    private Label lblSource1, lblSource;

    boolean pbKeyPressed = false;
    ObservableList<String> accountable_list = FXCollections.observableArrayList("Main Office", "Branch", "Department", "Employee");
    ObservableList<String> billingfrequency_list = FXCollections.observableArrayList("Monthly", "Quarterly", "Yearly");
    private ObservableList<ModelRecurringExpenseSchedule_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelRecurringExpenseSchedule_Detail> filteredDataDetail;

    JFXUtil.ReloadableTableTask loadTableDetail;
    private int pnDetail = 0;
    private ChangeListener<String> detailSearchListener;
    boolean lbresetpredicate = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            poJSON = new JSONObject();
            poController = new CashflowControllers(oApp, null).RecurringExpenseSchedule();
            poController.initialize();// Initialize transaction
//            poController.setRecordStatus("0123");

            initTextFields();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            initLoadTable();
            initDatePicker();
            initComboboxes();
            initTableOnClick();
            initDetailsGrid();
            Platform.runLater(() -> {
                poController.setIndustryID(psIndustryId);
                poController.setCompanyID(psCompanyId);
//                poController.setIndustryId(psIndustryId);
//                poController.setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
                btnNew.fire();
            });
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
        System.out.println(fsValue);
        this.psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        //No category
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
                        poJSON = poController.searchRecord(tfSearchPayee.getText(), false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        Platform.runLater(() -> {
                            try {
                                poJSON = poController.populateDetail();
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    poController.initialize();
                                    loadRecordMaster();
                                    loadTableDetail.reload();
                                    initButton(pnEditMode);
                                }
                            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        });
                        details_data.clear();
                        JFXUtil.clearTextFields(apDetail);
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        poController.Detail().clear();
                        clearTextFields();
                        poController.initFields();
                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            poController.initialize();
                            clearTextFields();

                            poController.Master().setIndustryCode(psIndustryId);
                            pnEditMode = EditMode.UNKNOWN;

                            break;
                        } else {
                            return;
                        }
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                loadTableDetail.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                loadRecordMaster();
                                btnNew.fire();
                            }
                        } else {
                            return;
                        }
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadRecordMaster();
                loadTableDetail.reload();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            details_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelRecurringExpenseSchedule_Detail(String.valueOf(lnRowCount),
                                                poController.Detail(lnCtr).Branch().getBranchName(),
                                                poController.Detail(lnCtr).getAccountNo(),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), true),
                                                poController.Detail(lnCtr).isExcluded() ? "Yes" : "No",
                                                poController.Detail(lnCtr).isActive() ? "Active" : "Inactive",
                                                poController.Detail(lnCtr).Company().getCompanyName()
                                        ));
                            }
                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetail, 0);
                                    int lnRow = 0;
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetail, pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    private void autoSearch(TextField txtField) {
        detailSearchListener = (observable, oldValue, newValue) -> {
            filteredDataDetail.setPredicate(orders -> {
                lbresetpredicate = true;
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return orders.getIndex02().toLowerCase().contains(lowerCaseFilter);
            });
            // If no results and autoSearchMain is enabled, remove listener and trigger autoSearchMain
            if (filteredDataDetail.isEmpty()) {
            } else {
                if (filteredDataDetail.size() == details_data.size()) {
                    tblViewDetail.getSelectionModel().select(pnDetail);
                    tblViewDetail.getFocusModel().focus(pnDetail);
                }
            }
        };
        txtField.textProperty().addListener(detailSearchListener);
    }

    public void loadRecordSearch() {
        try {
            lblSource.setVisible(false);
//            lblSource.setText(poController.Master().Industry().getDescription());
            tfSearchPayee.setText(poController.Master().Payee().getPayeeName());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordMaster() {
        try {
            boolean lbShow = JFXUtil.isObjectEqualTo(pnEditMode, EditMode.ADDNEW, EditMode.UPDATE, EditMode.READY) & !details_data.isEmpty();
            JFXUtil.setDisabled(!lbShow, tfSearchBranch);
            if (poController.Master().getParticularId() != null && !"".equals(poController.Master().getParticularId())) {
                tfRecurringID.setText(poController.Master().getRecurringId());
            } else {
                tfRecurringID.setText("");
            }
            tfPayee.setText(poController.Master().Payee().getPayeeName());
            tfParticular.setText(poController.Master().Particular().getDescription());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetail() {
        if (poController.Master().getParticularId() == null || "".equals(poController.Master().getParticularId())) {
            return;
        }
        if (poController.Detail(pnDetail).getEditMode() == EditMode.UPDATE) {
            JFXUtil.setDisabled(true, tfBranchName);
        } else {
            JFXUtil.setDisabled(false, tfBranchName);
        }
        try {
            tfCompany.setText(poController.Detail(pnDetail).Company().getCompanyName());
            tfBranchName.setText(poController.Detail(pnDetail).Branch().getBranchName());
            tfAccountNo.setText(poController.Detail(pnDetail).getAccountNo());
            tfAccountName.setText(poController.Detail(pnDetail).getAccountName());
            tfDeparment.setText(poController.Detail(pnDetail).Department().getDescription());
            tfEmployee.setText(poController.Detail(pnDetail).Employee().getCompanyName());
            taRemarks1.setText(poController.Detail(pnDetail).getRemarks());
            JFXUtil.setCmbValue(cmbAccountable, Integer.parseInt(poController.Detail(pnDetail).getAccountable())); //clarify
            dpDateFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Detail(pnDetail).getDateFrom(), SQLUtil.FORMAT_SHORT_DATE)));

            tfBillDay.setText(String.valueOf(poController.Detail(pnDetail).getBillDay()));
            tfDueDay.setText(String.valueOf(poController.Detail(pnDetail).getDueDay()));
            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount(), true));

            cbExcluded.setSelected(poController.Detail(pnDetail).isExcluded());
            cbActive.setSelected(poController.Detail(pnDetail).isActive());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    boolean lbProceed = true;

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = txtField.getId();
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchPayee":
                            poJSON = poController.searchRecord(tfSearchPayee.getText(), false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                txtField.setText("");
                            }
                            Platform.runLater(() -> {
                                try {
                                    poJSON = poController.populateDetail();
                                    if (!"success".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                        poController.initialize();
                                        loadRecordMaster();
                                        loadTableDetail.reload();
                                        initButton(pnEditMode);
                                    }
                                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                }
                            });
                            details_data.clear();
                            JFXUtil.clearTextFields(apDetail);
                            loadTableDetail.reload();
                            pnEditMode = poController.getEditMode();
                            initButton(pnEditMode);
                            loadRecordSearch();
                            break;
                        case "tfPayee":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.getDetailCount() > 1 && !JFXUtil.isObjectEqualTo(poController.Detail(0).getBranchCode(), null, "")) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the particular?\nPlease note that this action will delete all recurring expense schedule details.\n\nDo you wish to proceed?") == true) {
                                        poController.Detail().clear();
                                        poController.Master().setParticularId("");
                                        poController.Master().setPayeeId("");
                                        poController.Master().setRecurringId("");
                                        clearTextFields();
                                        loadTableDetail.reload();
                                        loadRecordMaster();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            lbProceed = false;
                            poJSON = poController.SearchPayee(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                loadRecordMaster();
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfParticular);
                            }
                            lbProceed = true;
                            loadTableDetail.reload();
                            break;
                        case "tfParticular":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.getDetailCount() > 1 && !JFXUtil.isObjectEqualTo(poController.Detail(0).getBranchCode(), null, "")) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the particular?\nPlease note that this action will delete all recurring expense schedule details.\n\nDo you wish to proceed?") == true) {
                                        poController.Detail().clear();
                                        poController.Master().setParticularId("");
                                        poController.Master().setRecurringId("");
                                        JFXUtil.clearTextFields(apDetail);
                                        loadTableDetail.reload();
                                        loadRecordMaster();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            lbProceed = false;
                            poJSON = poController.SearchParticular(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            }

                            Platform.runLater(() -> {
                                try {
                                    poJSON = poController.populateDetail();
                                    if (!"success".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                        poController.Detail().clear();
                                        loadTableDetail.reload();
                                    } else {
                                        poJSON = poController.UpdateTransaction();
                                        if (!"success".equals((String) poJSON.get("result"))) {
                                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                        }
                                    }
                                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                }
                            });

                            JFXUtil.runWithDelay(.8, () -> {
                                JFXUtil.textFieldMoveNext(tfBranchName); // must be in the success
                            });
                            lbProceed = true;
                            loadTableDetail.reload();
                            break;
                        case "tfBranchName":
                            lbProceed = false;
                            poJSON = poController.SearchBranch(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                txtField.setText("");
                            } else {
                                JFXUtil.textFieldMoveNext(tfAccountNo);
                            }
                            loadTableDetail.reload();
                            lbProceed = true;
                            break;
                        case "tfDeparment":
                            lbProceed = false;
                            poJSON = poController.SearchDepartment(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                txtField.setText("");
                            } else {
                                loadTableDetail.reload();
                                JFXUtil.textFieldMoveNext(tfEmployee);
                            }
                            lbProceed = true;
                            break;
                        case "tfEmployee":
                            lbProceed = false;
                            poJSON = poController.SearchEmployee(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                txtField.setText("");
                            } else {
                                loadTableDetail.reload();
                                JFXUtil.textFieldMoveNext(cmbAccountable);
                            }
                            lbProceed = true;
                            break;

                    }

                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchPayee":
                        if (lsValue.isEmpty()) {
                            if (!JFXUtil.isObjectEqualTo(pnEditMode, EditMode.ADDNEW, EditMode.UPDATE)) {
//                                    poController.getModel().setDescription("");
//                                    poController.InitTransaction(); // Initialize transaction
//                                    poController.setRecordStatus("0123");
                            }
                        }
                        break;
                }
            });
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfPayee":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Master().getPayeeId(), null, "") && lbProceed) {
                                    if (poController.getDetailCount() > 1 && !JFXUtil.isObjectEqualTo(poController.Detail(0).getBranchCode(), null, "")) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the payee?\nPlease note that this action will delete all recurring expense schedule details.\n\nDo you wish to proceed?") == true) {
                                                poController.Detail().clear();
                                                poController.Master().setPayeeId("");
                                                poController.Master().setParticularId("");
                                                poController.Master().setRecurringId("");
                                                clearTextFields();
                                                loadTableDetail.reload();
                                                loadRecordMaster();
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
                            }
                            if (lbProceed) { // uniquely inserted due to retrieval delay
                                poController.Master().setPayeeId("");
                                poController.Master().setParticularId("");
                                loadRecordMaster();
                            }
                        }
                        break;
                    case "tfParticular":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Master().getParticularId(), null, "") && lbProceed) {
                                    if (poController.getDetailCount() > 1 && !JFXUtil.isObjectEqualTo(poController.Detail(0).getBranchCode(), null, "")) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the particular?\nPlease note that this action will delete all recurring expense schedule details.\n\nDo you wish to proceed?") == true) {
                                                poController.Detail().clear();
                                                poController.Master().setParticularId("");
                                                poController.Master().setRecurringId("");
                                                JFXUtil.clearTextFields(apDetail);
                                                loadTableDetail.reload();
                                                loadRecordMaster();
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
                            }
                            if (lbProceed) { // uniquely inserted due to retrieval delay
                                poController.Master().setParticularId("");
                                loadRecordMaster();
                            }
                        }
                        break;
                }
            });
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                if (pnDetail < 0 || poController.getDetailCount() <= 0) {
                    JFXUtil.clearTextFields(apDetail);
                    return;
                }
                switch (lsID) {
                    case "tfAccountNo":
                        poJSON = poController.Detail(pnDetail).setAccountNo(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfAccountName":
                        poJSON = poController.Detail(pnDetail).setAccountName(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfDeparment":
                        if (lsValue.isEmpty() && lbProceed) {
                            poController.Detail(pnDetail).setDepartmentId(lsValue);
                            loadTableDetail.reload();
                        }
                        break;
                    case "tfEmployee":
                        if (lsValue.isEmpty() && lbProceed) {
                            poController.Detail(pnDetail).setEmployeeId(lsValue);
                            loadTableDetail.reload();
                        }
                        break;
                    case "tfBranchName":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setBranchCode(lsValue);
                        }
                        break;
                    case "tfAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setAmount(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfBillDay":
                        lsValue = JFXUtil.removeComma(lsValue);
//                        int lnDueDay = poController.Detail(pnDetail).getDueDay();
//                        if (lnDueDay != 0) {
//                            if (Integer.parseInt(lsValue) > lnDueDay) { //bill day cannot be greater than due day
//                                ShowMessageFX.Warning(null, pxeModuleName, "Bill day cannot be greater than Due day.");
//                                tfBillDay.requestFocus();
//                                loadRecordDetail();
//                                return;
//                            }
//                        }
                        if (Integer.parseInt(lsValue) > 31) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid bill day.");
                            loadRecordDetail();
                            tfBillDay.requestFocus();
                            return;
                        }
                        poJSON = poController.Detail(pnDetail).setBillDay(Integer.parseInt(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfDueDay":
                        lsValue = JFXUtil.removeComma(lsValue);
//                        int lnBillDay = poController.Detail(pnDetail).getBillDay();
//                        if (lnBillDay != 0) {
//                            if (lnBillDay > Integer.parseInt(lsValue)) { //bill day cannot be greater than due day
//                                ShowMessageFX.Warning(null, pxeModuleName, "Bill day cannot be greater than Due day.");
//                                tfDueDay.requestFocus();
//                                loadRecordDetail();
//                                return;
//                            }
//                        }
                        if (Integer.parseInt(lsValue) > 31) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid due day.");
                            loadRecordDetail();
                            tfDueDay.requestFocus();
                            return;
                        }
                        poJSON = poController.Detail(pnDetail).setDueDay(Integer.parseInt(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.3, () -> {
                    if (!JFXUtil.isObjectEqualTo(lsID, "tfDeparment", "tfEmployee")) {
                        loadTableDetail.reload();
                    }
                });
            });
    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks1":
                        poJSON = poController.Detail(pnDetail).setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbActive": // this is the id
                    poJSON = poController.Detail(pnDetail).isActive(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
                case "cbExcluded": // this is the id
                    poJSON = poController.Detail(pnDetail).isExcluded(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
            }
            JFXUtil.runWithDelay(.5, () -> {
                loadTableDetail.reload();
            });

        }
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchPayee);
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks1);
        JFXUtil.setFocusListener(txtMaster_Focus, tfRecurringID, tfPayee, tfParticular);
        JFXUtil.setFocusListener(txtDetail_Focus, tfAccountNo, tfAccountName, tfDueDay, tfDeparment, tfEmployee, tfBranchName, tfAmount, tfBillDay, tfSearchPayee);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setCommaFormatter(tfAmount);

        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);

        JFXUtil.adjustColumnForScrollbar(tblViewDetail);
        JFXUtil.inputIntegersOnly(tfDueDay, tfBillDay);

        JFXUtil.setDisabled(true, cmbBillingFrequency);
        JFXUtil.handleDisabledNodeClick(apDetail, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "cmbBillingFrequency":
                    ShowMessageFX.Warning(null, pxeModuleName, "This field is to-follow.");
                    break;
            }
        });
        JFXUtil.handleDisabledNodeClick(apMaster, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "tfRecurringID":
                    if (JFXUtil.isObjectEqualTo(poController.Master().getParticularId(), null, "")) {
                        ShowMessageFX.Warning(null, pxeModuleName,
                                "Complete the required fields (Payee and Particular) to enable viewing of the Recurring ID.");
                    }
                    break;
            }
        });
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbBillingFrequency":
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "cmbAccountable":
                        poJSON = poController.Detail(pnDetail).setAccountable(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadTableDetail.reload();
            });
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
                LocalDate currentDate = null, transactionDate = null, referenceDate = null, selectedDate = null;
                String lsServerDate = "", lsTransDate = "", lsRefDate = "", lsSelectedDate = "";

                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }

                lsServerDate = sdfFormat.format(oApp.getServerDate());
                currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                switch (datePicker.getId()) {
                    case "dpDateFrom":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                            if (pbSuccess && (selectedDate.isBefore(transactionDate))) {
//                                JFXUtil.setJSONError(poJSON, "Check date cannot be before the transaction date.");
//                                pbSuccess = false;
//                            }
                            if (pbSuccess) {
                                poController.Detail(pnDetail).setDateFrom((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                        }
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
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
            switch (currentTableID) {
                case "tblViewDetail":
                    if (!details_data.isEmpty()) {
                        pnDetail = newIndex;
                        loadRecordDetail();
                    }
                    break;
            }
        }
    };

    public void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDateFrom);
        JFXUtil.setActionListener(this::datepicker_Action, dpDateFrom);
    }

    public void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(billingfrequency_list, cmbBillingFrequency), new JFXUtil.Pairs<>(accountable_list, cmbAccountable));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbBillingFrequency, cmbAccountable);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbBillingFrequency, cmbAccountable);
    }

    private void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblDetailAccountNo, tblDetailRow, tblDetailExcluded, tblDetailStatus);
        JFXUtil.setColumnLeft(tblDetailBranch);
        JFXUtil.setColumnRight(tblDetailAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        autoSearch(tfSearchBranch);

        SortedList<ModelRecurringExpenseSchedule_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewDetail.comparatorProperty());
        tblViewDetail.setItems(sortedData);
    }

    private void initTableOnClick() {
        tblViewDetail.setOnMouseClicked(event -> {
            if (!details_data.isEmpty() && event.getClickCount() == 1) {
                ModelRecurringExpenseSchedule_Detail selected = (ModelRecurringExpenseSchedule_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                    loadRecordDetail();
                }
            }
        });
    }

    public void clearTextFields() {
        tfSearchBranch.setText("");
        JFXUtil.clearTextFields(apBrowse, apMaster, apDetail);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow, apMaster, apDetail);
    }
}
