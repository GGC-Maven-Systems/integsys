package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.ReplenishmentRequest;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Fund_Ledger;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.ReplenishmentRequestStatus;
import ph.com.guanzongroup.integsys.model.ModelReplenishment_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class ReplenishmentRequest_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static ReplenishmentRequest poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private boolean pbEntered = false;
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<Model_Cash_Fund_Ledger> checkedItems = new ArrayList<>();

    private ObservableList<ModelReplenishment_Detail> detail_data = FXCollections.observableArrayList();
    JFXUtil.ReloadableTableTask loadTableDetail;
    private int pnDetail = 0;

    ObservableList<String> comboboxlist = FXCollections.observableArrayList("Cash Fund", "Petty Cash Fund");
    JFXUtil.StageManager stageLedger = new JFXUtil.StageManager();

    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster, apTable, apBrowse;
    @FXML
    private Button btnBrowse, btnNew, btnSearch, btnSave, btnUpdate, btnAddLedger, btnRemoveLedger, btnCancel, btnVoid, btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfFundDescription, tfTransactionAmount;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private ComboBox cmbFundType;
    @FXML
    private Label lblStatus, lblSource;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewDetails;
    @FXML
    private TableColumn tblDetailRow1, tblDetailLedgerNo, tblDetailSourceCode, tblDetailSourceNo, tblDetailDate, tblDetailAmount;
    @FXML
    private CheckBox chckSelectAll;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            poJSON = new JSONObject();
            poController = new CashflowControllers(oApp, null).ReplenishmentRequest();
            poController.initialize();// Initialize transaction
//            poController.setRecordStatus("0123");

            initTextFields();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            initLoadTable();
            initTableOnClick();
            initDetailGrid();
            initCheckboxes();
            initComboboxes();
            Platform.runLater(() -> {
//                poController.setIndustryID(psIndustryId);
//                poController.setCompanyID(psCompanyId);
//                poController.setIndustryId(psIndustryId);
//                poController.setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
                poController.setRecordStatus("0134");
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
                    case "btnHistory":
                        if (poController.getEditMode() != EditMode.READY && poController.getEditMode() != EditMode.UPDATE) {
                            ShowMessageFX.Warning("No parameter status history to load!", pxeModuleName, null);
                            return;
                        }

                        try {
                            poController.ShowStatusHistory();
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No parameter status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnBrowse":
                        poController.setRecordStatus("0134");
                        poJSON = poController.searchRecord("", false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        } else {
//                            poJSON = poController.populateDetail();
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            }
                        }
                        resetCheckboxSelection();
                        detail_data.clear();
                        JFXUtil.clearTextFields(apMaster);
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
                        clearTextFields();
                        poController.initialize();
                        poJSON = poController.newRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        resetCheckboxSelection();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            poController.initialize();
                            clearTextFields();

                            poController.setIndustryId(psIndustryId);
                            pnEditMode = EditMode.UNKNOWN;
                            break;
                        } else {
                            return;
                        }
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveRecord();
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
                    case "btnAddLedger":
                        showLedgerDialog();
                        break;
                    case "btnRemoveLedger":
                        processAction("btnRemoveLedger");
                        break;
                    case "btnVoid":
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                loadRecordMaster();
                loadTableDetail.reload();
                initButton(pnEditMode);

            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void processAction(String action) {
//        try {
        String lsMessage = "";
        switch (action) {
            case "btnRemoveLedger":
                lsMessage = "remove";
                break;
            default:
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
        checkedItems.clear();
        List<String> list = new ArrayList<>();
        for (Object item : tblViewDetails.getItems()) {
            ModelReplenishment_Detail item1 = (ModelReplenishment_Detail) item;
            String lschecked = item1.getIndex01();
            int lnReference = Integer.valueOf(item1.getIndex07()) - 1;
            if (lschecked.equals("1")) {
                list.add(item1.getIndex06());
                checkedItems.add(poController.CashFundLedgerList(lnReference));
                System.out.println("check items : " + checkedItems.get(checkedItems.size() - 1));
            }
        }

        if (checkedItems.isEmpty()) {
            return;
        }
        switch (action) {
            case "btnRemoveLedger":
                poController.RemoveCashFundLedger(checkedItems);
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
        pnEditMode = poController.getEditMode();
//        } catch (SQLException | ParseException | CloneNotSupportedException | ScriptException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void resetCheckboxSelection() {
        chckSelectAll.setSelected(false);
        if (!checkedItem.isEmpty()) {
            checkedItem.clear();
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
                    loadTableDetail.reload();
                    break;
            }
        }
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbFundType":
                        //if changed then should clear all
                        poJSON = poController.getModel().setFundType(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                }
            });
    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlist, cmbFundType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbFundType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbFundType);
    }

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelReplenishment_Detail.class, tblViewDetails, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 0:
                            checkedItem.set(rowIndex, lbisTrue ? "1" : "0");
                            boolean allOnes = checkedItem.stream().allMatch("1"::equals);
                            chckSelectAll.setSelected(allOnes);
                            //set external temporary data of index to save as reference
                            // if detected unchecked then must update
                            pnDetail = rowIndex;
                            Platform.runLater(() -> {
                                loadTableDetail.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    if (lbisTrue) {
                                        JFXUtil.selectAndFocusRow(tblViewDetails, rowIndex);
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
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetails,
                detail_data,
                () -> {
                    Platform.runLater(() -> {
//                        try {
                        detail_data.clear();
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                                poController.ReloadDetail();
                        }
                        int lnRowCount = 0;

                        if (isCashFund()) {
                            for (int lnCtr = 0; lnCtr < poController.getCashFundLedgerListCount(); lnCtr++) {
                                lnRowCount += 1;
                                checkedItems(lnCtr);
                                detail_data.add(new ModelReplenishment_Detail(checkedItem.get(lnCtr),
                                        String.valueOf(poController.CashFundLedgerList(lnCtr).getLedgerNo()),
                                        poController.CashFundLedgerList(lnCtr).getSourceCode(),
                                        poController.CashFundLedgerList(lnCtr).getSourceNo(),
                                        JFXUtil.formatDateToString(poController.CashFundLedgerList(lnCtr).getTransactionDate()),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.CashFundLedgerList(lnCtr).getTransactionDate(), true),
                                        String.valueOf(lnRowCount)
                                ));
                            }
                        } else {
                            for (int lnCtr = 0; lnCtr < poController.getPettyCashLedgerListCount(); lnCtr++) {
                                lnRowCount += 1;
                                checkedItems(lnCtr);
                                detail_data.add(new ModelReplenishment_Detail(checkedItem.get(lnCtr),
                                        String.valueOf(poController.PettyCashLedgerList(lnCtr).getLedgerNo()),
                                        poController.PettyCashLedgerList(lnCtr).getSourceCode(),
                                        poController.PettyCashLedgerList(lnCtr).getSourceNo(),
                                        JFXUtil.formatDateToString(poController.PettyCashLedgerList(lnCtr).getTransactionDate()),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PettyCashLedgerList(lnCtr).getTransactionDate(), true),
                                        String.valueOf(lnRowCount)
                                ));
                            }
                        }

                        if (pnDetail < 0 || pnDetail
                                >= detail_data.size()) {
                            if (!detail_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewDetails, 0);
                                int lnRow = 0;
                                pnDetail = lnRow;
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewDetails, pnDetail);
                        }
                        loadRecordMaster();
//                        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
//                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//                        }
                    });
                });
    }

    public void showLedgerDialog() {
        poJSON = new JSONObject();
        stageLedger.closeDialog();
        if (isCashFund()) {
            if (poController.getLoadCashFundLedgerListCount() <= 0) {
                ShowMessageFX.Warning(null, pxeModuleName, "No ledger to load.");
                return;
            }
        } else {
            if (poController.getLoadPettyCashLedgerListCount() <= 0) {
                ShowMessageFX.Warning(null, pxeModuleName, "No ledger to load.");
                return;
            }
        }

        Map<String, JFXUtil.Data> data = new HashMap<>();
        data.clear();
        int lnCount = 0;
        if (isCashFund()) {
            for (int lnCtr = 0; lnCtr < poController.getLoadCashFundLedgerListCount(); lnCtr++) {
                lnCount += 1;
                data.put("0", new JFXUtil.Data(String.valueOf(poController.LoadCashFundLedgerList(lnCtr).getLedgerNo()),
                        poController.LoadCashFundLedgerList(lnCtr).getSourceCode(),
                        poController.LoadCashFundLedgerList(lnCtr).getSourceNo(),
                        JFXUtil.formatDateToString(poController.LoadCashFundLedgerList(lnCtr).getTransactionDate()),
                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.LoadCashFundLedgerList(lnCtr).getTransactionDate(), true)));
            }
        } else {
            for (int lnCtr = 0; lnCtr < poController.getLoadPettyCashLedgerListCount(); lnCtr++) {
                lnCount += 1;
                data.put("0", new JFXUtil.Data(String.valueOf(poController.PettyCashLedgerList(lnCtr).getLedgerNo()),
                        poController.PettyCashLedgerList(lnCtr).getSourceCode(),
                        poController.PettyCashLedgerList(lnCtr).getSourceNo(),
                        JFXUtil.formatDateToString(poController.PettyCashLedgerList(lnCtr).getTransactionDate()),
                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PettyCashLedgerList(lnCtr).getTransactionDate(), true)));
            }
        }

        ReplenishmentLedgerDialog_Controller controller = new ReplenishmentLedgerDialog_Controller();
        controller.addData(data);
        try {
            stageLedger.setOnHidden(event -> {
                stageLedger = null;
                loadTableDetail.reload();
            });
            stageLedger.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/ReplenishmentLedger_Dialog.fxml"), controller, "Ledger Dialog", false, false, true);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordSearch() {

    }

    private void checkboxState() {
        if (pnEditMode == EditMode.READY) {
            disableRowCheckbox.set(detail_data.isEmpty()); // set enable/disable in checkboxes in requirements
            JFXUtil.setDisabled(detail_data.isEmpty(), chckSelectAll);
        } else {
            disableRowCheckbox.set(true); // set enable/disable in checkboxes in requirements
            JFXUtil.setDisabled(true, chckSelectAll);
        }
    }

    private void loadRecordMaster() {
        try {
            lblStatus.setText("UNKNOWN");
            checkboxState();
//            if (isCashFund()) {
//                if (pnDetail < 0 || pnDetail > poController.getCashFundLedgerListCount() - 1) {
//                    return;
//                }
//            } else {
//                if (pnDetail < 0 || pnDetail > poController.getPettyCashLedgerListCount()- 1) {
//                    return;
//                }
//            }
            JFXUtil.setStatusValue(lblStatus, ReplenishmentRequestStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.getModel().getTransactionStatus());
            tfTransactionNo.setText(poController.getModel().getTransactionNo());
            dpTransactionDate.setValue(poController.getModel().getTransactionDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.getModel().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            JFXUtil.setCmbValue(cmbFundType, !poController.getModel().getFundType().equals("") ? Integer.valueOf(poController.getModel().getFundType()) : -1);
            tfFundDescription.setText(poController.getModel().CashFund().getDescription());
            tfTransactionAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getTransactionAmount().doubleValue(), true));
//        taRemarks.setText(poController.getModel().get());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    boolean lbProceed = true;
    boolean pbKeyPressed = false;

    private boolean isCashFund() {
        return cmbFundType.getSelectionModel().getSelectedIndex() == 0 ? true : false;
    }

    private boolean isDetailCountMoreThanOne() {
        if (isCashFund()) {
            return poController.getCashFundLedgerListCount() > 1 ? true : false;
        } else {
            return poController.getPettyCashLedgerListCount() > 1 ? true : false;
        }
    }

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
                        case "tfFundDescription":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (isDetailCountMoreThanOne()) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the Fund Description?\nPlease note that this action will reset all details.\n\nDo you wish to proceed?") == true) {
                                        btnNew.fire();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            lbProceed = false;
                            poJSON = poController.SearchFund(lsValue, false, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                    }
                    loadTableDetail.reload();
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfFundDescription":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                                if (!JFXUtil.isObjectEqualTo(poController.Master().getStockId(), null, "") && lbProceed) {
                                if (isDetailCountMoreThanOne()) {
                                    if (!pbKeyPressed) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the Fund Description?\nPlease note that this action will reset all details.\n\nDo you wish to proceed?") == true) {
                                            btnNew.fire();
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    } else {
                                        loadRecordMaster();
                                        return;
                                    }
                                }
//                                }
                            }
                            if (lbProceed) { // uniquely inserted due to retrieval delay
//                                poController.getModel().setCashFundId("");
                                loadRecordMaster();
                            }
                        }
                        break;
                    case "tfTransactionAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableDetail.reload();
                });
            });
    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
//                        poJSON = poController.getModel().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfTransactionNo, tfFundDescription, tfTransactionAmount);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetails);
        JFXUtil.adjustColumnForScrollbar(tblViewDetails);

        JFXUtil.handleDisabledNodeClick(apTable, pnEditMode, nodeID -> {
            if (nodeID.equals("chckSelectAll")) {
                if (!detail_data.isEmpty()) {
                    ShowMessageFX.Information(null, pxeModuleName, "Checkbox is available only when the record is not in Add or Update mode.");
                }
            }
        });
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
            switch (currentTableID) {
                case "tblViewDetails":
//                    if (!detail_data.isEmpty()) {
//                        pnDetail = newIndex;
//                        moveNext(false, false);
//                    }
                    break;
            }
        }
    };

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblDetailLedgerNo, tblDetailSourceNo, tblDetailDate);
        JFXUtil.setColumnLeft(tblDetailRow1, tblDetailSourceCode);
        JFXUtil.setColumnRight(tblDetailAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetails);
        tblViewDetails.setItems(detail_data);
    }

    private void initTableOnClick() {
        tblViewDetails.setOnMouseClicked(event -> {
            if (!detail_data.isEmpty() && event.getClickCount() == 1) {
                ModelReplenishment_Detail selected = (ModelReplenishment_Detail) tblViewDetails.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    pnDetail = Integer.parseInt(selected.getIndex02()) - 1;
//                    moveNext(false, false);
                }
            }
        });
    }

    public void clearTextFields() {
        resetCheckboxSelection();
        JFXUtil.clearTextFields(apBrowse, apMaster);
    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setDisabled(!lbShow1, apMaster);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.getModel().getTransactionStatus()) {
            case ReplenishmentRequestStatus.VOID:
            case ReplenishmentRequestStatus.CANCELLED:
                break;
        }
    }

}
