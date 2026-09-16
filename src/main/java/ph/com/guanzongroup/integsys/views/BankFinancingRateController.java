/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
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
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import org.json.simple.parser.ParseException;
import java.util.concurrent.atomic.AtomicReference;
import ph.com.guanzongroup.cas.sales.FinancingRates;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus.StandardRateType;
import ph.com.guanzongroup.integsys.model.ModelBankFinancingRate_Detail;
import ph.com.guanzongroup.integsys.model.ModelBankFinancingRate_Standard;

/**
 * FXML Controller class
 *
 * @author User
 */
public class BankFinancingRateController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnFinancingTerms = 0;
    boolean lsIsSaved = false;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static FinancingRates poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    boolean pbEntered = false;
    boolean pbKeyPressed = false;

    private ObservableList<ModelBankFinancingRate_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelBankFinancingRate_Standard> financingterms_data = FXCollections.observableArrayList();
    private FilteredList<ModelBankFinancingRate_Detail> filteredDataDetail;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    private ChangeListener<String> detailSearchListener;
    private ChangeListener<String> mainSearchListener;
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableFinancingTerms;
    JFXUtil.StageManager stageRateDialog = new JFXUtil.StageManager();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apMaster2, apMaster1;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSearchBank, tfRateID, tfBank, tfDuration, tfDIRate, tfInterestRate, tfSIRate;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnActivate, btnVoid, btnDeactivate, btnHistory, btnClose, btnStandardFinancingRates;
    @FXML
    private TableView tblViewFinancingTerms, tblViewDetail;
    @FXML
    private TableColumn tblType, tblDurationFinancingTerms, tblRateFinancingTerms, tblRateID, tblBank, tblDurationViewDetail, tblRateViewDetail, tblDIRate, tblSIRate, tblStatus;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).FinancingRates();
            poJSON = new JSONObject();
            poController.initialize(); // Initialize transaction
            initDetailsGrid();
            initLoadTable();
            initTextFields();
            initFinancingTerms();
            initTableOnClick();
            clearTextFields();
            loadRecordDetail();
            loadTableDetail.reload();
            pnEditMode = poController.getEditMode();
            initButton(pnEditMode);

            Platform.runLater(() -> {
//            psIndustryId = "";
//            poController.Master().setIndustryId(psIndustryId);
//            poController.Master().setCompanyId(psCompanyId);
//            poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
//            poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
            });
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference

            try {
                lblSource.setText(poController.getModel().Company().getCompanyName());
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
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
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            stageRateDialog.closeDialog();
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        //Clear data
//                        poController.resetMaster();
//                        poController.resetOthers();
//                        poController.Detail().clear();
                        clearTextFields();

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
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
//                            psSupplierId = poController.Master().getSupplierId();

                            //Clear data
//                            poController.resetMaster();
//                            poController.resetOthers();
//                            poController.Detail().clear();
                            poController.initialize();
                            clearTextFields();

//                            poController.Master().setIndustryId(psIndustryId);
//                            poController.Master().setCompanyId(psCompanyId);
//                            poController.Master().setSupplierId(psSupplierId);
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
                            poJSON = poController.saveRecord();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                loadTableDetail.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                // Print Transaction Prompt
                                lsIsSaved = false;
                                poController.initialize();
                                pnEditMode = poController.getEditMode();
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnActivate":
                        poJSON = poController.ActivateRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "btnVoid":
                        poJSON = poController.VoidRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "btnDeactivate":
                        poJSON = poController.DeactivateRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "btnStandardFinancingRates":
                        //triggers popup
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (lsButton.equals("btnPrint")) { //|| lsButton.equals("btnCancel")
                } else {
                    loadRecordDetail();
                    loadTableDetail.reload();
                }
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void stageRateDialog() {
////        try {
//        poJSON = new JSONObject();
//        if (stageRateDialog != null) {
//            stageRateDialog.closeDialog();
//            stageRateDialog = new JFXUtil.StageManager();
//        } else {
//            stageRateDialog = new JFXUtil.StageManager();
//        }
////        poController.loadLedger(true);
////        if (JFXUtil.isObjectEqualTo(poController.getModel().getFundId(), null, "")) {
////            ShowMessageFX.Warning(null, pxeModuleName, "Fund Description must have a value.");
////            return;
////        }
//
//        StandardFinancingRateDialog_Controller controller = new StandardFinancingRateDialog_Controller();
////        controller.addController(poController);
//        try {
//            stageRateDialog.setOnHidden(event -> {
//                stageRateDialog = null;
//                loadTableDetail.reload();
//            });
//            stageRateDialog.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/StandardFinancingRateDialog_Controller.fxml"), controller, "Standard Financing Rate Dialog", true, false, false);
//        } catch (IOException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        }
////        } catch (SQLException | GuanzonException ex) {
////            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
////            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
////        }
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBank":
                        if (lsValue.isEmpty()) {
                        }
                        break;
                    case "tfDuration":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setDuration(Integer.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfDIRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setDIRate(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfInterestRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setRate(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfSIRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setSIRate(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordDetail();
            });

    public void moveNext(boolean isUp, boolean continueNext) {
//        if (details_data.size() <= 0) {
//            return;
//        }
//
//        if (continueNext) {
//            apDetail.requestFocus();
//
//            pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewDetail) : JFXUtil.moveToNextRow(tblViewDetail);
//        }
//        loadRecordDetail();
//        tfReceiveQuantity.requestFocus();
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;
            TableView<?> currentTable = tblViewDetail;
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchBank":
                            poJSON = poController.SearchBank(lsValue, false, true);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            loadRecordSearch();
                            loadTableDetail.reload();
                            break;
                        case "tfBank":
                            poJSON = poController.SearchBank(lsValue, false, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            loadTableDetail.reload();
                            break;
                    }
                    break;
                case UP:
                    break;
                case DOWN:
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchBank":
                        if (lsValue.isEmpty()) {
                            loadTableDetail.reload();
                        }
                        break;
                }
            });
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

    public void initTextFields() {
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchBank);
        JFXUtil.setFocusListener(txtMaster_Focus, tfBank, tfDuration, tfDIRate, tfInterestRate, tfSIRate);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
        JFXUtil.inputDecimalOnly(tfDIRate, tfInterestRate, tfSIRate);
        JFXUtil.inputIntegersOnly(tfDuration);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewFinancingTerms, tblViewDetail);

        JFXUtil.adjustColumnForScrollbar(tblViewFinancingTerms, tblViewDetail);
    }

    private String getStandardType(String lsValue) {
        return JFXUtil.setStatusValue(null, StandardRateType.class, lsValue);
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    pbEntered = false;
                    // Setting data to table detail
                    JFXUtil.disableAllHighlight(tblViewDetail, highlightedRowsDetail);
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {
                            poController.loadRecord(tfSearchBank.getText());
                            for (lnCtr = 0; lnCtr < poController.getRecordListCount(); lnCtr++) {
                                details_data.add(
                                        new ModelBankFinancingRate_Detail(String.valueOf(poController.RecordList(lnCtr).getRateId()),
                                                String.valueOf(poController.RecordList(lnCtr).Bank().getBankName()),
                                                String.valueOf(poController.RecordList(lnCtr).getDuration()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.RecordList(lnCtr).getRate(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.RecordList(lnCtr).getDIRate(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.RecordList(lnCtr).getSIRate(), false)),
                                                String.valueOf(poController.RecordList(lnCtr).getRecordStatus()),
                                                String.valueOf(lnCtr + 1)
                                        ));
                            }

                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewDetail.getSelectionModel().select(0);
                                    tblViewDetail.getFocusModel().focus(0);
                                    pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewDetail.getSelectionModel().select(pnDetail);
                                tblViewDetail.getFocusModel().focus(pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordDetail();
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableFinancingTerms = new JFXUtil.ReloadableTableTask(
                tblViewFinancingTerms,
                financingterms_data,
                () -> {
                    pbEntered = false;
                    // Setting data to table detail
                    JFXUtil.disableAllHighlight(tblViewDetail, highlightedRowsDetail);
                    Platform.runLater(() -> {
                        int lnCtr;
                        financingterms_data.clear();
                        try {
                            poController.loadStandardRates();

                            for (lnCtr = 0; lnCtr < poController.getStandardRateListCount(); lnCtr++) {
                                financingterms_data.add(
                                        new ModelBankFinancingRate_Standard(getStandardType(poController.StandardRateList(lnCtr).getRateType()),
                                                String.valueOf(poController.StandardRateList(lnCtr).getDuration()),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.StandardRateList(lnCtr).getRate(), false),
                                                String.valueOf(lnCtr + 1)
                                        ));
                            }

                            if (pnFinancingTerms < 0 || pnFinancingTerms
                                    >= financingterms_data.size()) {
                                if (!financingterms_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewFinancingTerms.getSelectionModel().select(0);
                                    tblViewFinancingTerms.getFocusModel().focus(0);
                                    pnDetail = tblViewFinancingTerms.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewFinancingTerms.getSelectionModel().select(pnFinancingTerms);
                                tblViewFinancingTerms.getFocusModel().focus(pnFinancingTerms);
                                loadRecordDetail();
                            }
                            loadRecordDetail();
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    private void initFinancingTerms() {
        JFXUtil.setColumnLeft(tblType);
        JFXUtil.setColumnRight(tblDurationFinancingTerms, tblRateFinancingTerms);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewFinancingTerms);
        tblViewFinancingTerms.setItems(financingterms_data);
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRateID);
        JFXUtil.setColumnLeft(tblBank, tblStatus);
        JFXUtil.setColumnRight(tblDurationViewDetail, tblRateViewDetail, tblDIRate, tblSIRate);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster);

        loadRecordDetail();
        loadTableDetail.reload();
        loadTableFinancingTerms.reload();
    }

    public void loadRecordSearch() {
        tfSearchBank.setText(poController.getSearchBank());
    }

    public void loadRecordDetail() {
        try {
            JFXUtil.setStatusValue(lblStatus, FinancingRateStatus.class,
                    pnEditMode == EditMode.UNKNOWN ? "-1" : poController.getModel().getRecordStatus());
            boolean lbPrintStat = pnEditMode == EditMode.READY && poController.getModel().getRecordStatus() != FinancingRateStatus.VOID;

            tfRateID.setText(poController.getModel().getRateId());
            tfBank.setText(poController.getModel().Bank().getBankName());
            tfDuration.setText(String.valueOf(poController.getModel().getDuration()));
            tfDIRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getDIRate(), false));
            tfInterestRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getRate(), false));
            tfSIRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getSIRate(), false));
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTableOnClick() {
        tblViewDetail.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 2) {  // Detect single click (or use another condition for double click)
                    ModelBankFinancingRate_Detail selected = (ModelBankFinancingRate_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        try {
                            stageRateDialog.closeDialog();
                            poController.openRecord(selected.getIndex01());
                            pnEditMode = poController.getEditMode();
                            initButton(pnEditMode);
                            loadRecordDetail();
//                            moveNext(false, false);
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
        tblViewFinancingTerms.setOnMouseClicked(event -> {
            if (financingterms_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelBankFinancingRate_Standard selected = (ModelBankFinancingRate_Standard) tblViewFinancingTerms.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnFinancingTerms = Integer.parseInt(selected.getIndex04()) - 1;
                    }
                }
            }
        });
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);
        JFXUtil.setButtonsVisibility(false, btnActivate, btnVoid, btnDeactivate);
        JFXUtil.setDisabled(!lbShow, apMaster);
        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.getModel().getRecordStatus()) {
            case FinancingRateStatus.OPEN:
                JFXUtil.setButtonsVisibility(true, btnActivate, btnDeactivate,btnVoid);
                break;
            case FinancingRateStatus.ACTIVE:
                JFXUtil.setButtonsVisibility(true, btnDeactivate);
                JFXUtil.setButtonsVisibility(false, btnActivate, btnVoid);
                break;
            case FinancingRateStatus.DEACTIVATE:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                JFXUtil.setButtonsVisibility(false, btnVoid, btnDeactivate);
                JFXUtil.setButtonsVisibility(true, btnActivate);
                break;
            case FinancingRateStatus.VOID:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                JFXUtil.setButtonsVisibility(false, btnVoid, btnDeactivate);
                JFXUtil.setButtonsVisibility(false, btnActivate);
                break;
        }
    }
}
