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
import org.json.simple.parser.ParseException;
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
public class ReplenishmentRequest_HistoryController implements Initializable, ScreenInterface {

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
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfFundDescription, tfTransactionAmount, tfSearchFundDescription, tfSearchTransactionNo;
    @FXML
    private Label lblStatus, lblSource;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private ComboBox cmbFundType;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewDetails;
    @FXML
    private TableColumn tblDetailRow1, tblDetailLedgerNo, tblDetailSourceCode, tblDetailSourceNo, tblDetailDate, tblDetailAmount;

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
            Platform.runLater(() -> {
//                poController.setIndustryID(psIndustryId);
//                poController.setCompanyId(psCompanyId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
                poController.setRecordStatus(ReplenishmentRequestStatus.OPEN);
                try {
                    lblSource.setText(poController.getModel().Company().getCompanyName() + " - " + poController.getModel().Industry().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
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
                            ShowMessageFX.Warning("No status history to load!", pxeModuleName, null);
                            return;
                        }

                        try {
                            poController.ShowStatusHistory();
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No status history to load!", pxeModuleName, null);
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
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnConfirm", "btnApprove", "btnVoid", "btnCancel")) {
                    poController.resetTransaction();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                    loadTableDetail.reload();
                }
                if (lsButton.equals("btnRetrieve")) {
                } else {
                    loadRecordMaster();
                }
                initButton(pnEditMode);

            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetails,
                detail_data,
                () -> {
                    Platform.runLater(() -> {
                        detail_data.clear();
                        int lnRowCount = 0;

                        if (isCashFund()) {
                            for (int lnCtr = 0; lnCtr < poController.getCashFundLedgerListCount(); lnCtr++) {
                                lnRowCount += 1;
                                detail_data.add(new ModelReplenishment_Detail("",
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
                                detail_data.add(new ModelReplenishment_Detail("",
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
                    });
                });
    }

    public void loadRecordSearch() {

    }

    private void loadRecordMaster() {
        try {
            lblStatus.setText("UNKNOWN");
            JFXUtil.setStatusValue(lblStatus, ReplenishmentRequestStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.getModel().getTransactionStatus());
            tfTransactionNo.setText(poController.getModel().getTransactionNo());
            dpTransactionDate.setValue(poController.getModel().getTransactionDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.getModel().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            JFXUtil.setCmbValue(cmbFundType, !poController.getModel().getFundType().equals("") ? Integer.valueOf(poController.getModel().getFundType()) : -1);
            if (isCashFund()) {
                tfFundDescription.setText(poController.getModel().CashFund().getDescription());
            } else {
                tfFundDescription.setText(poController.getModel().PettyCash().getDescription());
            }
            tfTransactionAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getTransactionAmount().doubleValue(), true));
            taRemarks.setText(poController.getModel().getRemarks());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    boolean lbProceed = true;
    boolean pbKeyPressed = false;

    private boolean isCashFund() {
        return JFXUtil.isObjectEqualTo(poController.getModel().getFundType(), "1") ? true : false;
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
                        case "tfSearchFundDescription":
                            poJSON = poController.SearchFund(lsValue, false, true);
                            break;
                        case "tfSearchTransactionNo":
                            poJSON = poController.searchRecord(lsValue, false);
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
                    case "tfSearchFundDescription":
                        if (lsValue.isEmpty()) {
                        }
                        break;
                    case "tfSearchTransactionNo":
                        if (lsValue.isEmpty()) {
                        }
                        break;
                }
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchTransactionNo, tfSearchFundDescription);
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
//                    if (!detail_data.isEmpty()) {
//                        pnDetail = newIndex;
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
                }
            }
        });
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apBrowse, apMaster);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);

        JFXUtil.setButtonsVisibility(lbShow3, btnHistory);
        JFXUtil.setDisabled(true, apMaster);
        JFXUtil.setButtonsVisibility(lbShow4, btnBrowse, btnClose);
    }

}
