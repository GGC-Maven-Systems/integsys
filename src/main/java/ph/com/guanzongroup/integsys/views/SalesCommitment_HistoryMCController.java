/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
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
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.transformation.FilteredList;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.SalesCommitment;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.BankApplicationStatus;
import ph.com.guanzongroup.integsys.model.ModelSalesCommitment_Detail;
import ph.com.guanzongroup.integsys.model.ModelSalesCommitment_Main;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;

/**
 *
 * @author Team 1
 */
public class SalesCommitment_HistoryMCController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesCommitment poController;
    public int pnEditMode;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private ObservableList<ModelSalesCommitment_Detail> details_data = FXCollections.observableArrayList();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private FilteredList<ModelSalesCommitment_Main> filteredData;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apInquiry, apFields, apMaster, apDetail, apTableDetail;
    @FXML
    private Label lblSource, lblStatus, lblBankApplicationStatus;
    @FXML
    private TextField tfSearchClient, tfSearchTransactionNo, tfTransNo, tfClientType, tfClient, tfAddress, tfBranch, tfSalesPerson, tfReferralAgent, tfApplicationNo, tfATDNumber, tfPaymentMode, tfTerm, tfBank, tfInquiryNo, tfInquiryType, tfUnitType, tfTransactionTotal, tfVATRate, tfVatSales, tfVatAmount, tfWTaxRate, tfWTax, tfSalesAmount, tfBarcode, tfDescription, tfUnitPrice, tfQuantity;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private DatePicker dpTransactionDate, dpAppliedDate, dpApproveDate, dpDueDate, dpInquiryDate, dpTargetDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewDetailList;
    @FXML
    private TableColumn tblNo, tblDescription, tblUnitPrice, tblQuantity, tblTotal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).SalesCommitment();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                System.err.println((String) poJSON.get("message"));
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initTextFields();
            initDatePickers();
            initDetailsGrid();
            initTableOnClick();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);

            Platform.runLater(() -> {
                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.Master().setCategoryCode(psCategoryId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
                poController.setTransactionStatus(BankApplicationStatus.OPEN + BankApplicationStatus.APPROVED + BankApplicationStatus.CANCELLED + BankApplicationStatus.DISAPPROVED);
                loadRecordSearch();
            });
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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
                    case "btnBrowse":
                        poJSON = poController.SearchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransNo.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {

                            //Clear data
                            poController.resetMaster();
                            poController.Detail().clear();
                            clearTextFields();

                            poController.Master().setIndustryId(psIndustryId);
                            poController.Master().setCompanyId(psCompanyId);
                            poController.Master().setCategoryCode(psCategoryId);
                            poController.initFields();
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
                            return;
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnRetrieve":
                        retrieveSalesInquiry();
                        break;

                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnCancelBankApplication")) {
                    poController.InitTransaction();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnArrowRight", "btnArrowLeft", "btnRetrieve", "btnHistory")) {
                } else {
                    loadTableDetail.reload();
                }
                JFXUtil.runWithDelay(.5, () -> {
                    if (lsButton.equals("btnUpdate")) {
                        moveNext(false, false);
                    }
                });
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void retrieveSalesInquiry() {
        try {
            poJSON = new JSONObject();
            poJSON = poController.loadSalesInquiryList(tfClient.getText());
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            } else {
                loadTableMain.reload();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.ADDNEW;
        JFXUtil.setDisabled(!lbDisable, tfClient);
        try {
            JFXUtil.setStatusValue(lblStatus, BankApplicationStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            poController.computeFields(false);

            lblBankApplicationStatus.setText(poController.getStatus(pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus()).toUpperCase());

            tfTransNo.setText(poController.Master().getTransactionNo());
            tfClient.setText(poController.Master().Client().getCompanyName());
            tfAddress.setText(poController.Master().ClientAddress().getAddress());
            tfInquiryNo.setText(poController.Master().getSourceNo());
            dpTransactionDate.setValue(poController.Master().getTransactionDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            tfApplicationNo.setText(poController.Master().getPONumber());
            tfBank.setText(poController.Master().Bank().getBankName());
            tfTerm.setText(poController.Master().Term().getDescription());
            tfATDNumber.setText(poController.Master().getATDNumber());
            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotal(), true));
            tfWTax.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getWithholdingTax(), true));
            tfWTaxRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getWTaxRate(), true));
            tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATAmount(), true));
            tfVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATSale(), true));
            tfVATRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATRates(), false));
            tfSalesAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getSalesAmount(), true));
            taRemarks.setText(poController.Master().getRemarks());
            dpAppliedDate.setValue(poController.Master().getAppliedDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getAppliedDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            dpDueDate.setValue(poController.Master().getDueDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getDueDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);

            if (poController.Master().getSourceNo() != null && !"".equals(poController.Master().getSourceNo())) {
                tfBranch.setText(poController.Master().Inquiry().Branch().getBranchName());
                tfSalesPerson.setText(poController.Master().Inquiry().SalesPerson().getFullName());
                tfReferralAgent.setText(poController.Master().Inquiry().ReferralAgent().getCompanyName());
                tfInquiryType.setText(poController.Master().Inquiry().Source().getDescription());
                tfClientType.setText(getClientType(Integer.parseInt(poController.Master().Inquiry().getClientType())));
                tfUnitType.setText(getCategoryType(Integer.parseInt(poController.Master().Inquiry().getCategoryType())));
                tfPaymentMode.setText(getPaymentmode(Integer.parseInt(poController.Master().getPaymentMode())));
                dpInquiryDate.setValue(poController.Master().Inquiry().getTransactionDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().Inquiry().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
                dpTargetDate.setValue(poController.Master().Inquiry().getTargetDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().Inquiry().getTargetDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);

            } else {
                JFXUtil.clearNodes(dpInquiryDate, dpTargetDate, tfInquiryType, tfClientType, tfUnitType, tfPaymentMode,
                        tfBranch, tfSalesPerson, tfReferralAgent);
            }

            JFXUtil.updateCaretPositions(apMaster);

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            tfBarcode.setText(poController.Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.Detail(pnDetail).Inventory().getDescription());
            tfUnitPrice.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getUnitPrice(), true));
            tfQuantity.setText(String.valueOf(poController.Detail(pnDetail).getQuantity()));

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private String getClientType(int index) {
        if (index >= 0 && index < ModelSalesInquiry_Detail.ClientType.size()) {
            return ModelSalesInquiry_Detail.ClientType.get(index);
        }
        return "";
    }

    private String getCategoryType(int index) {
        if (index >= 0 && index < ModelSalesInquiry_Detail.CategoryType.size()) {
            return ModelSalesInquiry_Detail.CategoryType.get(index);
        }
        return "";
    }

    private String getPaymentmode(int index) {
        if (index >= 0 && index < ModelSalesInquiry_Detail.PurchaseType.size()) {
            return ModelSalesInquiry_Detail.PurchaseType.get(index);
        }
        return "";
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetailList":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex06())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex06());
                    pnDetail = newIndex;
                    loadRecordDetail();
                    break;

            }
        }
    };

    public void initTableOnClick() {
        tblViewDetailList.setOnMouseClicked(event -> {
            if (!details_data.isEmpty() && event.getClickCount() == 1) {
                ModelSalesCommitment_Detail selected = (ModelSalesCommitment_Detail) tblViewDetailList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(details_data.get(tblViewDetailList.getSelectionModel().getSelectedIndex()).getIndex06());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetailList);
        JFXUtil.adjustColumnForScrollbar(tblViewDetailList); // need to use computed-size in min-width of the column to work
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetailList,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            String lsBrand = "";
                            String lsModel = "";
                            String lsModelVariant = "";
                            String lsColor = "";
                            String lsDescription = "";
                            int lnRowCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (poController.Detail(lnCtr).getStockId() != null
                                        && !"".equals(poController.Detail(lnCtr).getStockId())) {
                                    lsBrand = poController.Detail(lnCtr).Inventory().Brand().getDescription();
                                    lsModel = poController.Detail(lnCtr).Inventory().Model().getDescription();
                                    lsModelVariant = poController.Detail(lnCtr).Inventory().Variant().getDescription();
                                    lsColor = poController.Detail(lnCtr).Inventory().Color().getDescription();
                                }
                                lsDescription = (lsBrand == null ? "" : lsBrand)
                                        + (lsModel == null ? "" : " " + lsModel)
                                        + (lsModelVariant == null ? "" : " " + lsModelVariant)
                                        + (lsColor == null ? "" : " " + lsColor);
                                if (!poController.Detail(lnCtr).isReversed()) {
                                    continue;
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelSalesCommitment_Detail(
                                                String.valueOf(lnRowCount),
                                                lsDescription.trim().replaceAll("\\r?\\n", " "),
                                                String.valueOf(poController.Detail(lnCtr).getUnitPrice()),
                                                String.valueOf(poController.Detail(lnCtr).getQuantity()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat((poController.Detail(lnCtr).getUnitPrice() * poController.Detail(lnCtr).getQuantity()), true)),
                                                String.valueOf(lnCtr)
                                        ));
                                lsBrand = "";
                                lsModel = "";
                                lsModelVariant = "";
                                lsColor = "";
                            }
                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 6); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetailList, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex06());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetailList, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewDetailList.getSelectionModel().getSelectedIndex()).getIndex06());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

    }

    public void moveNext(boolean isUp, boolean continueNext) {
        try {
            apDetail.requestFocus();
            if (continueNext) {
                pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewDetailList) : JFXUtil.moveToNextRow(tblViewDetailList);
            }
            loadRecordDetail();
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnDetail).Inventory().getBarCode(), tfBarcode},
                {poController.Detail(pnDetail).Inventory().getBarCode(), tfDescription}, // if null or empty, then requesting focus to the txtfield
                {poController.Detail(pnDetail).getUnitPrice(), tfUnitPrice}, // if null or empty, then requesting focus to the txtfield
                {poController.Detail(pnDetail).getQuantity(), tfQuantity},}, tfQuantity); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    break;
                case DOWN:
                    break;
                case F3:
                    switch (lsID) {
                        //apBrowse
                        case "tfSearchClient":
                            poJSON = poController.SearchTransaction(tfSearchClient.getText(), tfSearchTransactionNo.getText(), true);
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            loadRecordMaster();
                            return;
                        case "tfSearchTransactionNo":
                            poJSON = poController.SearchTransaction(tfSearchClient.getText(), tfSearchTransactionNo.getText(), false);
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            loadRecordMaster();
                            return;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initDatePickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpInquiryDate, dpTargetDate, dpTransactionDate, dpAppliedDate, dpDueDate);
    }

    public void initTextFields() {
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetailList);

        JFXUtil.adjustColumnForScrollbar(tblViewDetailList);

    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(lbShow2, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail);
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnLeft(tblDescription);
        JFXUtil.setColumnRight(tblUnitPrice, tblQuantity, tblTotal);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetailList);
        tblViewDetailList.setItems(details_data);
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse);
    }
}
