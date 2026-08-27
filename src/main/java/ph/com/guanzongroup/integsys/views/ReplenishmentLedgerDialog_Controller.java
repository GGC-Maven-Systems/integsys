package ph.com.guanzongroup.integsys.views;

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
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.ReplenishmentRequest;
import ph.com.guanzongroup.cas.cashflow.model.Model_Cash_Fund_Ledger;
import ph.com.guanzongroup.cas.cashflow.model.Model_PettyCashLedger;
import ph.com.guanzongroup.integsys.model.ModelReplenishmentLedger;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class ReplenishmentLedgerDialog_Controller implements Initializable, ScreenInterface {

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
    ArrayList<Model_Cash_Fund_Ledger> checkedItems_cashFund = new ArrayList<>();
    ArrayList<Model_PettyCashLedger> checkedItems_pettyCash = new ArrayList<>();
    private ObservableList<ModelReplenishmentLedger> main_data = FXCollections.observableArrayList();
    JFXUtil.ReloadableTableTask loadTableDetail;
    private int pnDetail = 0;
    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apTable, apBrowse;
    @FXML
    private Button btnAddLedger, btnClose;
    @FXML
    private TableView tblViewDetails;
    @FXML
    private TableColumn tblDetailRow1, tblDetailLedgerNo, tblDetailSourceCode, tblDetailSourceNo, tblDetailDate, tblDetailAmount;
    @FXML
    private CheckBox chckSelectAll;
    @FXML
    private TextField tfSearchLedgerNo;
    private ChangeListener<String> detailSearchListener;
    private FilteredList<ModelReplenishmentLedger> filteredDataDetail;
    boolean lbresetpredicate = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poJSON = new JSONObject();
        initTextFields();
        clearTextFields();
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
        initLoadTable();
        initTableOnClick();
        initDetailGrid();
        initCheckboxes();
        Platform.runLater(() -> {
            loadTableDetail.reload();
        });

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
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            String lsButton = clickedButton.getId();
            switch (lsButton) {
                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                        CommonUtils.closeStage(btnClose);
                    } else {
                        return;
                    }
                    break;
                case "btnAddLedger":
                    processAction("btnAddLedger");
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                    break;
            }

        }
    }

    private void processAction(String action) {
        String lsMessage = "";
        switch (action) {
            case "btnAddLedger":
                lsMessage = "add";
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

        checkedItems_cashFund.clear();
        checkedItems_pettyCash.clear();
        List<String> list = new ArrayList<>();
        for (Object item : tblViewDetails.getItems()) {
            ModelReplenishmentLedger item1 = (ModelReplenishmentLedger) item;
            String lschecked = item1.getIndex01();
            int lnReference = Integer.valueOf(item1.getIndex07()) - 1;
            if (lschecked.equals("1")) {
                list.add(item1.getIndex06());
                if (isCashFund()) {
                    checkedItems_cashFund.add(poController.LoadCashFundLedgerList(lnReference));
                } else {
                    checkedItems_pettyCash.add(poController.LoadPettyCashLedgerList(lnReference));
                }
            }
        }
        if (isCashFund()) {
            if (checkedItems_cashFund.isEmpty()) {
                return;
            }
        } else {
            if (checkedItems_pettyCash.isEmpty()) {
                return;
            }
        }

        switch (action) {
            case "btnAddLedger":
                if (isCashFund()) {
                    poJSON = poController.AddCashFundLedger(checkedItems_cashFund);
                } else {
                    poJSON = poController.AddPettyCashLedger(checkedItems_pettyCash);
                }
                break;
            default:
                break;
        }
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        resetCheckboxSelection();
        CommonUtils.closeStage(btnClose);
        pnEditMode = poController.getEditMode();
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

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelReplenishmentLedger.class, tblViewDetails, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    switch (colIndex) {
                        case 0:
                            boolean lbisTrue = newVal;
                            // Get the actual index from the original data
                            int actualIndex = main_data.indexOf(row);
                            if (actualIndex == -1) {
                                break;
                            }
                            if (lbisTrue) {
                                for (int i = 0; i <= actualIndex; i++) {
                                    checkedItem.set(i, "1");
                                }
                            } else {
                                for (int i = actualIndex; i < checkedItem.size(); i++) {
                                    checkedItem.set(i, "0");
                                }
                            }
                            boolean allOnes = checkedItem.stream().allMatch("1"::equals);
                            chckSelectAll.setSelected(allOnes);
                            // Use actual index for your external reference
                            pnDetail = actualIndex;
                            Platform.runLater(() -> {
                                loadTableDetail.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    // Find the row's current position in the filtered list
                                    int filteredIndex = filteredDataDetail.indexOf(row);
                                    if (filteredIndex >= 0) {
                                        tblViewDetails.getSelectionModel().select(filteredIndex);
                                        tblViewDetails.getFocusModel().focus(filteredIndex);
                                    }
                                });
                            });
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
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        main_data.clear();
                        int lnCtrCount = 0;
                        if (isCashFund()) {
                            for (int lnCtr = 0; lnCtr < poController.getLoadCashFundLedgerListCount(); lnCtr++) {
                                lnCtrCount += 1;
                                checkedItems(lnCtr);
                                main_data.add(new ModelReplenishmentLedger(checkedItem.get(lnCtr),
                                        String.valueOf(poController.LoadCashFundLedgerList(lnCtr).getLedgerNo()),
                                        poController.LoadCashFundLedgerList(lnCtr).getSourceCode(),
                                        poController.LoadCashFundLedgerList(lnCtr).getSourceNo(),
                                        JFXUtil.formatDateToString(poController.LoadCashFundLedgerList(lnCtr).getTransactionDate()),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.LoadCashFundLedgerList(lnCtr).getCreditAmount(), true), String.valueOf(lnCtrCount)
                                ));
                            }
                        } else {
                            for (int lnCtr = 0; lnCtr < poController.getLoadPettyCashLedgerListCount(); lnCtr++) {
                                lnCtrCount += 1;
                                checkedItems(lnCtr);
                                main_data.add(new ModelReplenishmentLedger(checkedItem.get(lnCtr),
                                        String.valueOf(poController.LoadPettyCashLedgerList(lnCtr).getLedgerNo()),
                                        poController.LoadPettyCashLedgerList(lnCtr).getSourceCode(),
                                        poController.LoadPettyCashLedgerList(lnCtr).getSourceNo(),
                                        JFXUtil.formatDateToString(poController.LoadPettyCashLedgerList(lnCtr).getTransactionDate()),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.LoadPettyCashLedgerList(lnCtr).getCreditAmount(), true), String.valueOf(lnCtrCount)
                                ));
                            }
                        }
                        disableRowCheckbox.set(main_data.isEmpty()); // set enable/disable in checkboxes in requirements
                        JFXUtil.setDisabled(main_data.isEmpty(), chckSelectAll);

                        if (pnDetail < 0 || pnDetail
                                >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewDetails, 0);
                                int lnRow = 0;
                                pnDetail = lnRow;
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewDetails, pnDetail);
                        }
                    });
                });
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
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchLedgerNo":
//                            poController.load
                            poJSON = poController.SearchFund(lsValue, false, true);
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

    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchLedgerNo":
                        if (lsValue.isEmpty()) {
                        }
                        break;
                }
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchLedgerNo);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetails);
        JFXUtil.adjustColumnForScrollbar(tblViewDetails);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
            switch (currentTableID) {
                case "tblViewDetails":
                    if (!main_data.isEmpty()) {
                        pnDetail = newIndex;
                    }
                    break;
            }
        }
    };

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblDetailLedgerNo, tblDetailSourceNo, tblDetailDate);
        JFXUtil.setColumnLeft(tblDetailRow1, tblDetailSourceCode);
        JFXUtil.setColumnRight(tblDetailAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetails);

        filteredDataDetail = new FilteredList<>(main_data, b -> true);
        autoSearch(tfSearchLedgerNo);

        SortedList<ModelReplenishmentLedger> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewDetails.comparatorProperty());
        tblViewDetails.setItems(sortedData);
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
                if (filteredDataDetail.size() == main_data.size()) {
                    tblViewDetails.getSelectionModel().select(pnDetail);
                    tblViewDetails.getFocusModel().focus(pnDetail);
                }
            }
        };
        txtField.textProperty().addListener(detailSearchListener);
    }

    private void initTableOnClick() {
        tblViewDetails.setOnMouseClicked(event -> {
            if (!main_data.isEmpty() && event.getClickCount() == 1) {
                ModelReplenishmentLedger selected = (ModelReplenishmentLedger) tblViewDetails.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    pnDetail = Integer.parseInt(selected.getIndex02()) - 1;
                }
            }
        });
    }

    public void clearTextFields() {
        resetCheckboxSelection();
        JFXUtil.clearTextFields(apBrowse);
    }

    private boolean isCashFund() {
        return JFXUtil.isObjectEqualTo(poController.getModel().getFundType(), "1") ? true : false;
    }

    public void addController(ReplenishmentRequest loController) {
        poController = loController;
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        if (fnValue != EditMode.READY) {
            return;
        }

    }

}
