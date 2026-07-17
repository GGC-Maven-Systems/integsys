package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.t1.SalesGiveaways;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.SalesGiveawaysStatus;
import ph.com.guanzongroup.integsys.model.ModelSales_Giveaways;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class SalesGiveawaysController implements Initializable, ScreenInterface {

    private final String pxeModuleName = "Sales Giveaways";
    private GRiderCAS oApp;
    private SalesGiveaways poController;
    private JSONObject poJSON;
    private int pnEditMode;
    private int pnDetail = 0;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private ObservableList<ModelSales_Giveaways> details_data = FXCollections.observableArrayList();
    JFXUtil.ReloadableTableTask loadTableDetail;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private boolean pbEntered = false;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSearchGiveaway, tfGiveawaycode, tfMasterDescription, tfCategory, tfBarcode, tfDescription, tfQuantity;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnActivate, btnDisapprove, btnDeactivate, btnHistory, btnClose;
    @FXML
    private DatePicker dbFromDate, dpThruDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblViewTransDetails;
    @FXML
    private TableColumn tblRowNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblQuantityDetail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        poController = new SalesControllers(oApp, null).SalesGiveaways();
        poJSON = new JSONObject();
        poController.setWithUI(true);
        poJSON = poController.InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
        initLoadTable();
        initDatePicker();
        initTextFields();
        clearTextFields();
        initDetailGrid();
        initTableOnClick();
        clearTextFields();
        pnEditMode = EditMode.UNKNOWN;

        initButton(pnEditMode);
        Platform.runLater(() -> {
            try {
                poController.Master().setIndustryId(psIndustryId);
//            poController.Master().setCompanyId(psCompanyId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                lblSource.setText(poController.Master().Industry().getDescription());
//                poController.setCategoryID(psCategoryId);
//            poController.Master().setBranchCode(oApp.getBranchCode());
                poController.setTransactionStatus("0134");
                btnNew.fire();
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
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
    void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnBrowse":
//                    poController.setTransactionStatus(POQuotationStatus.OPEN);
                    poController.setTransactionStatus("0134");
                    poJSON = poController.searchRecord();
                    if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfGiveawaycode.requestFocus();
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnNew":
                    clearTextFields();
                    poJSON = poController.NewTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    loadTableDetail.reload();
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnUpdate":
                    String lsUserId = oApp.getUserID();
//                    String lsPosition = poController.checkPosition(DisbursementStatic.OPEN, lsUserId);
//                    if (lsPosition == null || "".equals(lsPosition)) {
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "User is not an authorized officer.");
//                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                        return;
//                    }
                    //Recheck transaction status
//                    poJSON = poController.checkUpdateTransaction(true);
//                    if (!"success".equals((String) poJSON.get("result"))) {
//                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                        return;
//                    }

                    poJSON = poController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                    break;
                case "btnSave":
                    //Recheck transaction status
//                    if (pnEditMode == EditMode.UPDATE) {
//                        poJSON = poController.checkUpdateTransaction(true);
//                        if (!"success".equals((String) poJSON.get("result"))) {
//                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                            return;
//                        }
//                    }

                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                        return;
                    }
                    poJSON = poController.SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }

                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                    poJSON = poController.OpenTransaction(poController.Master().getGiveawayCode());
                    if ("success".equals(poJSON.get("result"))) {
                        pnEditMode = poController.getEditMode();
                    }
                    if (pnEditMode == EditMode.READY) {
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to activate this transaction?")) { //requires to review journal entry
                            poJSON = poController.ActivateTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                    }
                    Platform.runLater(() -> btnNew.fire());
                    break;
                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                        pnEditMode = EditMode.UNKNOWN;
                    } else {
                        return;
                    }

                    break;
                case "btnActivate":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this record?") == true) {
                        poJSON = poController.ActivateTransaction("");
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                    } else {
                        return;
                    }
                    ShowMessageFX.Information("Record activated successfully", pxeModuleName, null);
                    pnEditMode = EditMode.UNKNOWN;
                    break;
                case "btnDisapprove":
                    poJSON = poController.DisapproveTransaction("");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
                case "btnDeactivate":
                    poJSON = poController.DeactiveTransaction("");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    } else {
                        ShowMessageFX.Information("Record deactivated successfully", pxeModuleName, null);
                    }
                    break;
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                        return;
                    }
                    try {
                        poController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                    }
                    break;
                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                        appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                    return;
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnActivate", "btnDeactivate", "btnDisapprove")) {
                poController.InitTransaction();
                clearTextFields();
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnSearch", "btnHistory")) {
            } else {
                loadRecordMaster();
                loadTableDetail.reload();
            }
            initButton(pnEditMode);
            if (lsButton.equals("btnUpdate")) {
                moveNext(false, false);
            }

        } catch (SQLException | GuanzonException | CloneNotSupportedException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    boolean pbKeyPressed = false;

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField textField = (TextField) event.getSource();
            String lsTextField = textField.getId();
            String lsValue = textField.getText() == null ? "" : textField.getText();
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    if (tfQuantity.isFocused()) {
                        pbEntered = true;
                    }
                    CommonUtils.SetNextFocus(textField);
                    break;
                case F3:
                    switch (lsTextField) {
                        //apBrowse
                        case "tfSearchGiveaway":
                            poController.setTransactionStatus("0134");
                            poJSON = poController.searchRecord(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            loadTableDetail.reload();
                            break;
                        //apMaster
                        case "tfCategory":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Detail(0).getStockId(), null, "")) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the category?\nPlease note that this action will reset all details.\n\nDo you wish to proceed?") == true) {
                                        poController.Detail().clear();
                                        loadTableDetail.reload();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            poJSON = poController.SearchCategory(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            loadTableDetail.reload();
                            break;
                        //apDetail
                        case "tfBarcode":
                            poJSON = poController.SearchInventory("", true, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfQuantity);
                            }
                            loadTableDetail.reload();
                            break;
                        case "tfDescription":
                            poJSON = poController.SearchInventory("", false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfQuantity);
                            }
                            loadTableDetail.reload();
                            break;
                    }
                    break;
                case UP:
                    JFXUtil.altSwitch(lsTextField, new Object[][]{
                        {new String[]{"tfBarcode", "tfDescription", "tfQuantity"}, (Runnable) () -> moveNext(true, true)},});
                    break;
                case DOWN:
                    JFXUtil.altSwitch(lsTextField, new Object[][]{
                        {new String[]{"tfBarcode", "tfDescription", "tfQuantity"}, (Runnable) () -> moveNext(false, true)},});
                    break;
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(SalesGiveawaysController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void moveNext(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apDetail.requestFocus();
                boolean lbContinue = true;
                while (lbContinue) {
                    pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewTransDetails)).getIndex05())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewTransDetails)).getIndex05());
                    if (poController.Detail(pnDetail).isReversed()) {
                        lbContinue = false;
                    }
                }
            }
            loadRecordDetail();
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnDetail).Inventory().getBarCode(), tfBarcode},
                {poController.Detail(pnDetail).Inventory().getDescription(), tfDescription},
                {poController.Detail(pnDetail).getQuantity(), tfQuantity},}, tfQuantity); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMaster() {
        try {
            boolean lbShow = pnEditMode == EditMode.UPDATE;
            JFXUtil.setDisabled(lbShow, tfCategory);
            lblStatus.setText(poController.getStatus(poController.Master().getTransactionStatus()));
            tfGiveawaycode.setText(poController.Master().getGiveawayCode());
            tfMasterDescription.setText(poController.Master().getDescription());
            tfCategory.setText(poController.Master().Category().getDescription());

            dbFromDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getFromDate(), SQLUtil.FORMAT_SHORT_DATE)));
            dpThruDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getThruDate(), SQLUtil.FORMAT_SHORT_DATE)));
            taRemarks.setText(poController.Master().getRemarks());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            boolean lbShow = poController.Detail(pnDetail).getEditMode() == EditMode.UPDATE;
            JFXUtil.setDisabled(lbShow, tfBarcode, tfDescription);

            tfBarcode.setText(poController.Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.Detail(pnDetail).Inventory().getDescription());
            tfQuantity.setText(String.valueOf(poController.Detail(pnDetail).getQuantity()));
            cbReverse.setSelected(poController.Detail(pnDetail).isReversed());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchGiveaway":
                        break;
                }
            });

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBarcode":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setStockId("");
                        }
                        break;
                    case "tfDescription":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetail).setStockId("");
                        }
                        break;
                    case "tfQuantity":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setQuantity(Integer.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }

                        if (pbEntered) {
                            JFXUtil.runWithDelay(0.50, () -> {
                                loadTableDetail.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    moveNext(false, true);
                                });
                                pbEntered = false;
                            });
                        }
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableDetail.reload();
                });
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfMasterDescription":
                        poJSON = poController.Master().setDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfCategory":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Detail(0).getStockId(), null, "")) {
                                    if (!pbKeyPressed) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the category?\nPlease note that this action will reset all details.\n\nDo you wish to proceed?") == true) {
                                            poController.Detail().clear();
                                            loadTableDetail.reload();
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
                            poController.Master().setCategoryCode("");
                            loadRecordMaster();
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    private void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        try {
                            details_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (!poController.Detail(lnCtr).isReversed()) {
                                    continue;
                                }

                                lnRowCount += 1;
                                details_data.add(
                                        new ModelSales_Giveaways(String.valueOf(lnRowCount),
                                                poController.Detail(lnCtr).Inventory().getBarCode(),
                                                poController.Detail(lnCtr).Inventory().getDescription(),
                                                String.valueOf(poController.Detail(lnCtr).getQuantity()),
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 5); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewTransDetails, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex05());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewTransDetails, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewTransDetails.getSelectionModel().getSelectedIndex()).getIndex05());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewTransDetails":
                    if (!details_data.isEmpty()) {
                        newIndex = isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex05())
                                : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex05());
                        pnDetail = newIndex;
                        loadRecordDetail();
                    }
                    break;
            }
        }
    };

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbReverse":
                    if (poController.Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                        poController.Detail().remove(pnDetail);
                    } else {
                        poController.Detail(pnDetail).setReversed(cbReverse.isSelected());
                    }
                    loadTableDetail.reload();
                    if (checkedBox.isSelected()) {
                        moveNext(false, false);
                    }
                    break;
            }
        }
    }
    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
//                try {
                switch (datePicker.getId()) {
                    case "dbFromDate":
                        //back date not allowed
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                                lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
//                                transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

//                                if (pbSuccess && (ldSelectedDate.isBefore(transactionDate))) {
//                                    JFXUtil.setJSONError(poJSON, "Check date cannot be later than the transaction date.");
//                                    pbSuccess = false;
//                                }
                            LocalDate toDate = dpThruDate.getValue();
                            if (toDate != null && ldSelectedDate.isAfter(toDate)) {
                                JFXUtil.setJSONError(poJSON, "Invalid Date, The 'From' date cannot be after the 'To' date.");
//                                    ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.Master().setFromDate(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }

                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    case "dpThruDate":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            LocalDate fromDate = dbFromDate.getValue();
                            if (fromDate != null && ldSelectedDate.isBefore(fromDate)) {
                                JFXUtil.setJSONError(poJSON, "Invalid Date, The 'To' date cannot be before the 'From' date.");
//                                    ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                                pbSuccess = false;
                            }
                            if (pbSuccess) {
                                poController.Master().setThruDate(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                        }

                        break;
                    default:
                        break;
                }
//                } catch (SQLException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                }
            });

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnBrowse, btnNew, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel, btnSearch);
        JFXUtil.setButtonsVisibility(lbShow2, btnHistory, btnUpdate, btnActivate);

        JFXUtil.setDisabled(!lbShow, apMaster);
        JFXUtil.setButtonsVisibility(false, btnActivate, btnDeactivate, btnDisapprove);
        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case SalesGiveawaysStatus.OPEN:
                JFXUtil.setButtonsVisibility(true, btnActivate, btnDeactivate, btnDisapprove);
                break;
            case SalesGiveawaysStatus.ACTIVE:
                JFXUtil.setButtonsVisibility(true, btnDeactivate, btnDisapprove);
                JFXUtil.setButtonsVisibility(false, btnActivate, btnDeactivate, btnDisapprove);
                break;
            case SalesGiveawaysStatus.DEACTIVATE:
                JFXUtil.setButtonsVisibility(true, btnActivate, btnDisapprove);
                JFXUtil.setButtonsVisibility(false, btnUpdate,btnActivate, btnDisapprove);
                break;
            case SalesGiveawaysStatus.DISAPPROVE:
                JFXUtil.setButtonsVisibility(false, btnUpdate,btnActivate, btnDeactivate, btnDisapprove);
                break;
        }
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtBrowse_Focus, apBrowse);
        JFXUtil.setFocusListener(txtMaster_Focus, tfMasterDescription, tfCategory);
        JFXUtil.setFocusListener(txtDetail_Focus, tfBarcode, tfDescription, tfQuantity);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        CustomCommonUtil.inputIntegersOnly(tfQuantity);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewTransDetails);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails);
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dbFromDate, dpThruDate);
        JFXUtil.setActionListener(datepicker_Action, dbFromDate, dpThruDate);
    }

    public void initDetailGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblQuantityDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);
        tblViewTransDetails.setItems(details_data);
    }

    private void initTableOnClick() {
        tblViewTransDetails.setOnMouseClicked(event -> {
            if (!details_data.isEmpty() && event.getClickCount() == 1) {
                ModelSales_Giveaways selected = (ModelSales_Giveaways) tblViewTransDetails.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(details_data.get(tblViewTransDetails.getSelectionModel().getSelectedIndex()).getIndex05());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });

    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail);
    }
}
