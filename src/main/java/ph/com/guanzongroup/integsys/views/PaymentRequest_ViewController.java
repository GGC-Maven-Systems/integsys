/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.InvTransCons;
import org.guanzon.cas.purchasing.controller.PurchaseOrder;
import org.guanzon.cas.purchasing.services.PurchaseOrderControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.PaymentRequest;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class PaymentRequest_ViewController implements Initializable {

    private GRiderCAS poApp;
    private PaymentRequest poController;
    private JSONObject poJSON;
    private String pxeModuleName = "Payment Request";
    private int pnEditMode;
    private int pnTblDetailRow = 0;
    private String psTransactionNo = "";
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster, apDetail;
    @FXML
    private Label lblSource, lblStatus, lblAdvances, lblSourceTranTotal;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnHistory, btnPOHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfBranch, tfDepartment, tfPayee, tfSeriesNo, tfTotalAmount, tfDiscountAmount, tfNetAmount, tfSourceNo, tfAdvances, tfSourceTranTotal, tfRecurringNo, tfBranchDetail, tfAccountNo, tfEmployee, tfParticular, tfAmount, tfDiscRate, tfDiscAmountDetail, tfAmountDetail;
    @FXML
    private DatePicker dpTransaction;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblVwPRDetail;
    @FXML
    private TableColumn tblRowNoDetail, tblParticular, tblAmount, tblDiscAmount, tbTotalAmount;

    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    public void setTransaction(String fsValue) {
        psTransactionNo = fsValue;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new CashflowControllers(poApp, null).PaymentRequest();
            JFXUtil.setKeyEventFilter(tableKeyEvents, tblVwPRDetail);
            initTableOnClick();
            initDetailGrid();
            initButtonsClickActions();
            JFXUtil.setDisabled(true, apMaster, apDetail);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                CommonUtils.closeStage(btnClose);
            }
            Platform.runLater((() -> {
                try {
                    poJSON = poController.OpenTransaction(psTransactionNo);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        CommonUtils.closeStage(btnClose);
                    } 
                    pnEditMode = poController.getEditMode();
                    loadRecordSearch();
                    loadTableDetail();
                    JFXUtil.setButtonsVisibility(InvTransCons.PURCHASE_ORDER.equals(poController.Master().getSourceCode()), btnPOHistory);
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                }
            }));
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initTableOnClick() {
        tblVwPRDetail.setOnMouseClicked(event -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
                int lnRow = Integer.parseInt(detail_data.get(tblVwPRDetail.getSelectionModel().getSelectedIndex()).getIndex06());
                pnTblDetailRow = lnRow;
                ModelTableDetail selectedItem = (ModelTableDetail) tblVwPRDetail.getSelectionModel().getSelectedItem();
                if (event.getClickCount() == 1) {
                    clearDetailFields();
                    if (selectedItem != null) {
                        if (pnTblDetailRow >= 0) {
                            loadRecordDetail();
                            initDetailFocus();
                        }
                    }
                }
            }
        });
    }

    private void loadRecordMaster() {
        try {

            JFXUtil.setStatusValue(lblStatus, PaymentRequestStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            poController.computeFields();
            tfTransactionNo.setText(poController.Master().getTransactionNo());
            dpTransaction.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfBranch.setText(poController.Master().Branch().getBranchName());
            if (poApp.isMainOffice() || poApp.isWarehouse()) {
                String lsDepartment = "";
                if (poController.Master().Department().getDescription() != null) {
                    lsDepartment = poController.Master().Department().getDescription();
                }
                tfDepartment.setText(lsDepartment);
            }
            String lsPayee = "";
            if (poController.Master().Payee().getPayeeName() != null) {
                lsPayee = poController.Master().Payee().getPayeeName();
            }
            tfPayee.setText(lsPayee);
            tfSeriesNo.setText(poController.Master().getSeriesNo());
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTranTotal(), true));
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getDiscountAmount(), true));
            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getNetTotal(), true));
            taRemarks.setText(poController.Master().getRemarks());
            tfAdvances.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().PurchaseOrder().getAmountPaid(), true));
            tfSourceNo.setText(poController.Master().getSourceNo());

            tfSourceTranTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().PurchaseOrder().getNetTotal(), true));
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordDetail() {

        if (pnTblDetailRow >= 0) {
            try {

                tfParticular.setText(poController.Detail(pnTblDetailRow).Particular().getDescription());
                tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                        poController.Detail(pnTblDetailRow).getAmount(), true));
                tfDiscRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnTblDetailRow).getDiscount())); // rate
                tfDiscAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnTblDetailRow).getAddDiscount(), true)); // amount
                cbReverse.setSelected(poController.Detail(pnTblDetailRow).isReverse());

                tfRecurringNo.setText(poController.Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getRecurringNo());
                tfBranchDetail.setText(poController.Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Branch().getBranchName());
                tfAccountNo.setText(poController.Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getAccountNo());
                tfEmployee.setText(poController.Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Employee().getCompanyName());
                tfAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnTblDetailRow).getNetTotal(), true));
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnHistory, btnPOHistory,btnClose);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    private void handleButtonAction(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        CommonUtils.closeStage(btnClose);
                    } else {
                        return;
                    }
                    break;
                case "btnHistory":
                    poController.ShowStatusHistory();
                    return;
                case "btnPOHistory":
                    if (InvTransCons.PURCHASE_ORDER.equals(poController.Master().getSourceCode())) {
                        try {
                            //load order history
                            PurchaseOrder loPOController = new PurchaseOrderControllers(poApp, null).PurchaseOrder();
                            poJSON = loPOController.InitTransaction(); // Initialize transaction
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                CommonUtils.closeStage(btnClose);
                            }
                            poJSON = loPOController.OpenTransaction(poController.Master().getSourceNo());
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            }
                            try {
                                loPOController.ShowStatusHistory();
                            } catch (NullPointerException npe) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                                ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                            } catch (Exception ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                            }

                        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    } else {
                        ShowMessageFX.Warning("No transaction source history to load.", pxeModuleName, null);
                    }
                    return;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", pxeModuleName, null);
                    break;
            }
            if (lsButton.equals("btnArrowRight") || lsButton.equals("btnArrowLeft")) {
            } else {
                clearDetailFields();
                loadRecordMaster();
                loadTableDetail();
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PaymentRequest_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void clearDetailFields() {
        JFXUtil.clearTextFields(apDetail);
    }

    private void loadTableDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: transparent;");

        tblVwPRDetail.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelTableDetail>> task = new Task<List<ModelTableDetail>>() {
            @Override
            protected List<ModelTableDetail> call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        detail_data.clear();
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            poController.ReloadDetail();
                        }
                        int lnRowCount = 0;
                        for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                            if (!poController.Detail(lnCtr).isReverse()) {
                                continue;
                            }
                            lnRowCount += 1;

                            double lnDetailDiscountRate = poController.Detail(lnCtr).getAmount() * poController.Detail(lnCtr).getDiscount();
                            double lnTotalDiscountAmount = poController.Detail(lnCtr).getAddDiscount() + lnDetailDiscountRate;

                            detail_data.add(new ModelTableDetail(
                                    String.valueOf(lnRowCount),
                                    poController.Detail(lnCtr).Particular().getDescription(),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), true),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDiscountAmount, true),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getNetTotal(), true),
                                    String.valueOf(lnCtr)
                            ));
                        }

                        tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTranTotal(), true));
                        tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getDiscountAmount(), true));
                        tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getNetTotal(), true));

                        int lnTempRow = JFXUtil.getDetailRow(detail_data, pnTblDetailRow, 6); //this method is used only when Reverse is applied
                        if (lnTempRow < 0 || lnTempRow
                                >= detail_data.size()) {
                            if (!detail_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblVwPRDetail, 0);
                                int lnRow = Integer.parseInt(detail_data.get(0).getIndex06());
                                pnTblDetailRow = lnRow;
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblVwPRDetail, lnTempRow);
                            int lnRow = Integer.parseInt(detail_data.get(tblVwPRDetail.getSelectionModel().getSelectedIndex()).getIndex06());
                            pnTblDetailRow = lnRow;
                            loadRecordDetail();
                        }
                        loadRecordMaster();

                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                });
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
            }
        };

        new Thread(task).start();
    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblParticular);
        JFXUtil.setColumnRight(tblAmount, tblDiscAmount, tbTotalAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwPRDetail);
        tblVwPRDetail.setItems(detail_data);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblVwPRDetail":
                    newIndex = isMovedDown
                            ? Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex06()) : Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex06());
                    if (!detail_data.isEmpty()) {
                        pnTblDetailRow = newIndex;
                        loadRecordDetail();
                        initDetailFocus();
                    }
                    break;
            }
        }
    };

    private void initDetailFocus() {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {

            if (pnTblDetailRow >= 0) {
                boolean isSourceNotEmpty = !JFXUtil.isObjectEqualTo(poController.Detail(pnTblDetailRow).getParticularID(), null, "");
                if (isSourceNotEmpty && !JFXUtil.isObjectEqualTo(poController.Detail(pnTblDetailRow).getParticularID(), null, "")) {
                    tfAmount.requestFocus();
                } else {
                    if (!JFXUtil.isObjectEqualTo(poController.Detail(pnTblDetailRow).getParticularID(), null, "")
                            && (pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW)) {
                        tfAmount.requestFocus();
                    } else {
                        tfParticular.requestFocus();
                    }
                }

            }

        }
    }

}
