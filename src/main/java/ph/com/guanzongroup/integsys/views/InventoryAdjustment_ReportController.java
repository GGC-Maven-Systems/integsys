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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.InvAdjustment;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.inv.status.InvAdjustmentStatus;
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
public class InventoryAdjustment_ReportController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private int pnDetail = 0;
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
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    Map<String, String> imageinfo_temp = new HashMap<>();
    JFXUtil.ReloadableTableTask loadTableDetail;
    AnchorPane root = null;
    Scene scene = null;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList("OPEN", "CONFIRMED", "CANCELLED", "VOIDED", "ALL");
    JFXUtil.StageManager stageDialog = new JFXUtil.StageManager();

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblTotalAdjustments, lblTotalCreditQty, lblTotalDebitQty, lblOpen, lblConfirmed, lblVoided, lblCancelled;
    @FXML
    private Button btnPrint, btnRetrieve, btnClose;
    @FXML
    private RadioButton rbSummary, rbDetailed;
    @FXML
    private ToggleGroup type;
    @FXML
    private TextField tfCategory;
    @FXML
    private DatePicker dpDateFrom, dpTo;
    @FXML
    private ComboBox cmbStatus;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblNo, tblCompany, tblBranch, tblTransactionNo, tblDate, tblReferenceNo, tblCategory, tblTotalCreditQty, tblTotalCreditCost, tblTotalDebitQty, tblTotalDebitCost, tblRow, tblBrand, tblModel, tblColor, tblBarcode, tblDescription, tblCost, tblCreditQty, tblDebitQty, tblTotalCost, tblRemarks, tblStatus;
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
            clearTextFields();
            initDatepicker();
            initTableOnClick();
            initComboboxes();
            initToggleGroup();
            pnEditMode = EditMode.UNKNOWN;

            initButton(pnEditMode);

            Platform.runLater(() -> {
                try {
                    poController.setIndustryId(psIndustryId);
                    poController.setCompanyId(psCompanyId);
                    poController.Master().setCompanyId(psCompanyId);
                    poController.Master().setIndustryCode(psIndustryId);
                    lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });
            JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void retrieveLists() {
        try {
            poJSON = poController.loadTransactionList("", tfCategory.getText(), JFXUtil.formatStringToDate(dpDateFrom.getEditor().getText()),
                    JFXUtil.formatStringToDate(dpTo.getEditor().getText()),
                    String.valueOf(cmbStatus.getSelectionModel().getSelectedIndex()).equals("4") ? "" : String.valueOf(cmbStatus.getSelectionModel().getSelectedIndex()));
            loadTableDetail.reload();
        } catch (SQLException | GuanzonException | ScriptException | CloneNotSupportedException ex) {
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
                case "btnPrint":
                    break;
                case "btnRetrieve":
                    retrieveLists();
                    return;
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
                    return;
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
                                return;
                            }
                            loadTableDetail.reload();
                            break;
                        case "dpTo":
                            LocalDate selectedToDate = dpTo.getValue();
                            LocalDate fromDate = dpDateFrom.getValue();
                            if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                                dpTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(ldCurrentDate.toString()));
                                return;
                            }
                            loadTableDetail.reload();
                            break;
                        default:
                            break;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    private void initDatepicker() {
        try {
            JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDateFrom, dpTo);
            JFXUtil.setActionListener(datepicker_Action, dpDateFrom, dpTo);

            String lsDateFrom = CustomCommonUtil.formatDateToShortString(JFXUtil.getFirstDayOfMonth(oApp.getServerDate()));
            JFXUtil.setDateValue(dpDateFrom, JFXUtil.formatStringToLocalDate(lsDateFrom));

            String lsDateTo = CustomCommonUtil.formatDateToShortString(JFXUtil.getLastDayOfMonth(oApp.getServerDate()));
            JFXUtil.setDateValue(dpTo, JFXUtil.formatStringToLocalDate(lsDateTo));
        } catch (SQLException ex) {
            Logger.getLogger(InventoryAdjustment_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlist, cmbStatus));
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
                            int lnRowCount = 0;
                            String lsParticular = "", lsOrNo = "";
                            //should differ loop for summary and detail
                            for (int lnCtr = 0; lnCtr < poController.getTransactionListCount(); lnCtr++) {
                                lnRowCount += 1;
                                //by lnctrDetail is the index of detail per master
                                for (int lnCtrDetail = 0; lnCtrDetail < poController.TransactionList(lnCtr).getDetailCount(); lnCtrDetail++) {
                                    details_data.add(
                                            new ModelInventoryAdjustment_Detail(String.valueOf(lnRowCount),
                                                    poController.TransactionList(lnCtr).Master().Company().getCompanyName(),
                                                    poController.TransactionList(lnCtr).Master().Branch().getBranchName(),
                                                    poController.TransactionList(lnCtr).Master().getTransactionNo(),
                                                    CustomCommonUtil.formatDateToShortString(poController.TransactionList(lnCtr).Master().getTransactionDate()),
                                                    poController.TransactionList(lnCtr).Master().getReferenceNo(),
                                                    poController.TransactionList(lnCtr).Master().Category().getDescription(),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).getTotalCreditQty(), false),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).getTotalCreditCost(), false),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).getTotalDebitQty(), false),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).getTotalDebitCost(), false),
                                                    String.valueOf(lnCtr),
                                                    poController.TransactionList(lnCtr).Detail(lnCtrDetail).Inventory().Brand().getDescription(),
                                                    poController.TransactionList(lnCtr).Detail(lnCtrDetail).Inventory().Model().getDescription(),
                                                    poController.TransactionList(lnCtr).Detail(lnCtrDetail).Inventory().Color().getDescription(),
                                                    poController.TransactionList(lnCtr).Detail(lnCtrDetail).Inventory().getBarCode(),
                                                    poController.TransactionList(lnCtr).Detail(lnCtrDetail).Inventory().getDescription(),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).Detail(lnCtrDetail).getInventoryCost(), false),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).Detail(lnCtrDetail).getCreditQuantity(), false),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.TransactionList(lnCtr).Detail(lnCtrDetail).getDebitQuantity(), false),
                                                    poController.TransactionList(lnCtr).Master().getRemarks(),
                                                    poController.getStatus(poController.TransactionList(lnCtr).Master().getTransactionStatus()).toUpperCase()
                                            ));
                                }
                            }
                            loadRecordDetail();
                            JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblViewDetail, filteredMain_Data);
                        } catch (GuanzonException | SQLException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblNo, tblTransactionNo, tblDate, tblReferenceNo, tblStatus);
        JFXUtil.setColumnLeft(tblCompany, tblBranch, tblCategory, tblRow, tblBrand, tblModel, tblColor, tblBarcode, tblDescription, tblRemarks);
        JFXUtil.setColumnRight(tblTotalCreditQty, tblTotalCreditCost, tblTotalDebitQty, tblTotalDebitCost, tblCost, tblCreditQty, tblDebitQty, tblTotalCost);
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
        JFXUtil.setFocusListener(txtMaster_Focus, tfCategory);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);
//        JFXUtil.adjustColumnForScrollbar(tblViewDetail);
        tblViewDetail.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
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
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        pbEntered = true;
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearch":
                                poJSON = poController.SearchTransaction(lsValue, false);
                                break;
                            case "tfCategory":
                                poJSON = poController.SearchCategory(lsValue, false, true);
                                tfCategory.setText(poController.getCategory());
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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(InventoryAdjustment_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ScriptException ex) {
            Logger.getLogger(InventoryAdjustment_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordDetail() {
        lblTotalAdjustments.setText(String.valueOf(poController.getTransactionListTotalAdjustments("")));
        lblTotalCreditQty.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getTransactionListTotalCreditQty(), false));
        lblTotalDebitQty.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getTransactionListTotalDebitQty(), false));
        lblOpen.setText(String.valueOf(poController.getTransactionListTotalAdjustments(InvAdjustmentStatus.OPEN)));
        lblConfirmed.setText(String.valueOf(poController.getTransactionListTotalAdjustments(InvAdjustmentStatus.CONFIRMED)));
        lblVoided.setText(String.valueOf(poController.getTransactionListTotalAdjustments(InvAdjustmentStatus.VOID)));
        lblCancelled.setText(String.valueOf(poController.getTransactionListTotalAdjustments(InvAdjustmentStatus.CANCELLED)));
        JFXUtil.updateCaretPositions(apMaster);
    }

    private void initToggleGroup() {
        type.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                return;
            }

            boolean isSummary = "rbSummary".equals(((RadioButton) newToggle).getId());

            JFXUtil.setVisibility(false, tblRow);
            JFXUtil.setVisibility(!isSummary, tblBrand, tblModel, tblColor, tblBarcode,
                    tblDescription, tblCreditQty, tblDebitQty); // show for detailed
            JFXUtil.setVisibility(isSummary, tblTotalCreditQty, tblTotalCreditCost,
                    tblTotalDebitQty, tblTotalDebitCost, tblCreditQty, tblDebitQty); // show for summary
        });

        type.selectToggle(null);
        rbSummary.setSelected(true);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        JFXUtil.setButtonsVisibility(details_data.size() > 0, btnPrint);
        if (fnValue != EditMode.READY) {
            return;
        }
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster);
        lblTotalAdjustments.setText("0");
        lblTotalCreditQty.setText("0.00");
        lblTotalDebitQty.setText("0.00");
        lblOpen.setText("0");
        lblConfirmed.setText("0");
        lblVoided.setText("0");
        lblCancelled.setText("0");
    }
}
