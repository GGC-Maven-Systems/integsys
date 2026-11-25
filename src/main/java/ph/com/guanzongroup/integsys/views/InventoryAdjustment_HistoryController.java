/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.InvAdjustment;
import org.guanzon.cas.inv.services.InvControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.*;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import static ph.com.guanzongroup.integsys.utility.JFXUtil.getFirstDayOfMonth;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class InventoryAdjustment_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private int pnDetail = 0;
    private int pnAttachment = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private InvAdjustment poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    boolean pbEntered = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierPayeeId = "";
    private String psTransactionType = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelCashDisbursement_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelCashDisbursement_Main> filteredMain_Data;

    private ObservableList<ModelInventoryAdjustment_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEnteredDV = false;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    private int currentIndex = 0;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    Map<String, String> imageinfo_temp = new HashMap<>();
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    AnchorPane root = null;
    Scene scene = null;
    private FileChooser fileChooser;
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblTotalAdjustments, lblTotalCreditQty, lblTotalDebitQty, lblOpen, lblConfirmed, lblVoided, lblCancelled;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnRetrieve, btnClose;
    @FXML
    private TextField tfSearch, tfCategory;
    @FXML
    private DatePicker dpDateFrom, dpTo;
    @FXML
    private ComboBox cmbStatus;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblNo, tblTransactionNo, tblDate, tblReferenceNo, tblBranch, tblCategory, tblTotalCreditQty, tblTotalDebitQty, tblStatus;
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
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new InvControllers(oApp, null).InventoryAdjustment();
            poJSON = new JSONObject();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            pagination.setPageCount(1);
            initLoadTable();
            initTextFields();
            initDetailGrid();
            initDatepicker();
            initTableOnClick();
            clearTextFields();
            initComboboxes();
            pnEditMode = EditMode.UNKNOWN;

            initButton(pnEditMode);

            Platform.runLater(() -> {
//                try {
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                poController.Master().setBranchCode(oApp.getBranchCode());
//                } catch (SQLException | GuanzonException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//                }
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
//        try {
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            String lsButton = clickedButton.getId();
            switch (lsButton) {
                case "btnRetrieve":
                    break;
                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
//                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnConfirm", "btnApprove", "btnVoid", "btnCancel")) {
//                    resetValues();
                pnEditMode = EditMode.UNKNOWN;
                clearTextFields();
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnRetrieve", "btnSearch", "btnHistory")) {
            } else {
                loadTableDetail.reload();
            }
            initButton(pnEditMode);
        }
//        } catch (SQLException | GuanzonException | ParseException | CloneNotSupportedException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        } catch (ScriptException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        } catch (IOException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        }
    }

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbStatus":
                        break;
                }
            });

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
                try {
                    switch (datePicker.getId()) {
                        case "dpDateFrom":
                            LocalDate selectedFromDate = dpDateFrom.getValue();
                            LocalDate toDate = dpTo.getValue();
                            if (toDate != null && selectedFromDate.isAfter(toDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                                String lsDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                                dpDateFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
//                                psSearchDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                                return;
                            }
//                            psSearchDateFrom = CustomCommonUtil.formatLocalDateToShortString(selectedFromDate);
                            loadTableDetail.reload();
                            break;
                        case "dpTo":
                            LocalDate selectedToDate = dpTo.getValue();
                            LocalDate fromDate = dpDateFrom.getValue();
                            if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                                dpTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(ldCurrentDate.toString()));
//                                psSearchDateTo = CustomCommonUtil.formatLocalDateToShortString(ldCurrentDate);
                                return;
                            }
//                            psSearchDateTo = CustomCommonUtil.formatLocalDateToShortString(selectedToDate);
                            loadTableDetail.reload();
                            break;
                        default:
                            break;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            });

    private void initDatepicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDateFrom, dpTo);
        JFXUtil.setActionListener(datepicker_Action, dpDateFrom, dpTo);
    }

    private void initComboboxes() {
//        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(samplelist, cmbStatus));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbStatus);
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
                            String lsParticular = "", lsOrNo = "";
                            for (int lnCtr = 0; lnCtr < poController.getTransactionListCount(); lnCtr++) {
                                if (!poController.Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelInventoryAdjustment_Detail(String.valueOf(lnRowCount),
                                                poController.TransactionList(lnCtr).Master().getTransactionNo(),
                                                CustomCommonUtil.formatDateToShortString(poController.TransactionList(lnCtr).Master().getTransactionDate()),
                                                poController.TransactionList(lnCtr).Master().getReferenceNo(),
                                                poController.TransactionList(lnCtr).Master().Branch().getBranchName(),
                                                poController.TransactionList(lnCtr).Master().Category().getDescription(),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).getTotalCreditQty(), false),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).getTotalDebitQty(), false),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 11); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetail, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex09());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetail, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex09());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblViewDetail, filteredMain_Data);
                        } catch (CloneNotSupportedException | GuanzonException | SQLException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblNo, tblTransactionNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblBranch, tblCategory, tblStatus);
        JFXUtil.setColumnRight(tblTotalCreditQty, tblTotalDebitQty);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);
    }

    private void initTableOnClick() {
        tblViewDetail.setOnMouseClicked(event -> {
            if (!details_data.isEmpty() && event.getClickCount() == 1) {
                ModelInventoryAdjustment_Detail selected = (ModelInventoryAdjustment_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex09());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex09())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex09());
                    pnDetail = newIndex;
                    loadRecordDetail();
                    break;
            }
        }
    };

    private void initTextFields() {
        //Initialise  TextField Focus
        JFXUtil.setFocusListener(txtMaster_Focus, tfSearch, tfCategory);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);
        JFXUtil.adjustColumnForScrollbar(tblViewDetail);
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearch":
//                        poJSON = poController.sear
                        break;
                    case "tfCategory":
                        if (lsValue.isEmpty()) {
                        }
                        break;
                }
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextInputControl txtInput = (TextInputControl) event.getSource();
        String lsID = txtInput.getId();
        String lsValue = txtInput.getText() == null ? "" : txtInput.getText();
        String lsBranchCode = "";
        String lsDeparment = "";
        poJSON = new JSONObject();
//        try {
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        //apMaster
                        case "tfCategory":
                            break;
                    }
                    event.consume();
                    break;

                case UP:
                    break;
                case DOWN:
                    break;
                default:
                    break;
            }
        }
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        }
    }

    public void moveNext(boolean isUp, boolean continueNext) {
//        if (continueNext) {
//            apDetail.requestFocus();
//            pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex09())
//                    : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex09());
//        }
//        loadRecordDetail();
//        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
//            return;
//        }
//        if (isSourceNoAvailable()) {
//            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
//                {poController.Detail(pnDetail).getParticularId(), tfParticularDetail},
//                {poController.Detail(pnDetail).getDetailVatExempt(), tfVatExemptDetail},}, tfVatExemptDetail); // default
//        } else {
//            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
//                {poController.Detail(pnDetail).getReferNo(), tfORNoDetail},
//                {poController.Detail(pnDetail).getParticularId(), tfParticularDetail},
//                {poController.Detail(pnDetail).getAmount(), tfAmountDetail},}, tfAmountDetail); // default
//        }
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
//                tblTransactionTotal.setVisible(lbShow);
//                tblQuantity.setVisible(!lbShow);
//                tblUnitPrice.setVisible(!lbShow);
//                tblTotal.setVisible(!lbShow);
//                tblRow.setVisible(!lbShow);
//                tblItem.setVisible(!lbShow);
//                loadTableMain.reload();
            }
        });
    }

    private void loadRecordDetail() {
//        try {
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        tfSearch.setText("");
        tfCategory.setText("");
        dpDateFrom.setValue(null);
        dpTo.setValue(null);
        cmbStatus.getSelectionModel().clearSelection();

        lblTotalAdjustments.setText("");
        lblTotalCreditQty.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getTotalCreditQty(), false));
        lblTotalDebitQty.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getTotalDebitQty(), false));
        lblOpen.setText("");
        lblConfirmed.setText("");
        lblVoided.setText("");
        lblCancelled.setText("");

        JFXUtil.updateCaretPositions(apMaster);
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        }
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster);

        if (fnValue != EditMode.READY) {
            return;
        }
//        switch (poController.Master().getTransactionStatus()) {
//            case InvAdjustmentStatus.VOID:
//            case InvAdjustmentStatus.CANCELLED:
//                JFXUtil.setButtonsVisibility(false, btnUpdate);
//                break;
//        }
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster);
    }
}
