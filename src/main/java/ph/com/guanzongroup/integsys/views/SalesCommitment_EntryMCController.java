/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.parser.ParseException;
import javafx.animation.PauseTransition;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Pagination;
import javafx.util.Pair;
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
public class SalesCommitment_EntryMCController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0, pnMain = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesCommitment poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    boolean pbPurchaseTypeChanged = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private ObservableList<ModelSalesCommitment_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelSalesCommitment_Main> main_data = FXCollections.observableArrayList();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEntered = false;
    private final JFXUtil.RowDragLock dragLock = new JFXUtil.RowDragLock(true);

    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private static final int ROWS_PER_PAGE = 50;
    private FilteredList<ModelSalesCommitment_Main> filteredData;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apInquiry, apFields, apMaster, apDetail, apTableDetail;
    @FXML
    private Label lblSource, lblStatus, lblBankApplicationStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnCancelBankApplication, btnHistory, btnRetrieve, btnClose;
    @FXML
    private TextField tfTransNo, tfBranch, tfSalesPerson, tfReferralAgent, tfClientType, tfClient, tfAddress, tfInquiryType, tfUnitType, tfPaymentMode, tfInquiryNo, tfApplicationNo, tfBank, tfTerm, tfATDNumber, tfTransactionTotal, tfWTax, tfWTaxRate, tfVatAmount, tfVatSales, tfVATRate, tfSalesAmount, tfBarcode, tfDescription, tfUnitPrice, tfQuantity;
    @FXML
    private DatePicker dpInquiryDate, dpTargetDate, dpTransactionDate, dpAppliedDate, dpDueDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblViewDetailList, tblViewMainList;
    @FXML
    private TableColumn tblNoViewDetailList, tblDescription, tblUnitPrice, tblQuantity, tblTotal, tblNoViewMainList, tblInquiryDate, tblTransactionNo, tblClient, tblStatus;
    @FXML
    private Pagination pgPagination;

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
            pgPagination.setPageCount(1);
            initLoadTable();
            initTextFields();
            initDatePickers();
            initDetailsGrid();
            initMainGrid();
            initTableOnClick();
            clearTextFields();
            pnEditMode = poController.getEditMode();
            initButton(pnEditMode);

            Platform.runLater(() -> {
                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.Master().setCategoryCode(psCategoryId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
                loadRecordSearch();

                btnNew.fire();
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
                        poController.setTransactionStatus(BankApplicationStatus.OPEN);
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
                    case "btnNew":
                        //Clear data
                        clearTextFields();
                        poJSON = poController.InitTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apDetail);
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
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
//                                JSONObject loJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
//                                if ("success".equals(loJSON.get("result"))) {
//                                    if (poController.Master().getTransactionStatus().equals(BankApplicationStatus.OPEN)) {
//                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to approve this transaction?")) {
//                                            poController.Master().setApprovedDate(oApp.getServerDate());
//                                            loJSON = poController.ApproveTransaction();
//                                            if ("success".equals((String) loJSON.get("result"))) {
//                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
//                                            } else {
//                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
//                                            }
//                                        }
//                                    }
//                                }
                                Platform.runLater(() -> {
                                    btnNew.fire();
                                });
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnCancelBankApplication":
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to cancel the transaction?") == true) {
                            poJSON = poController.CancelTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            }

                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            Platform.runLater(() -> {
                                btnNew.fire();
                            });
                        }
                        return;
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
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
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
            tfVATRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATRates(), true));
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
            cbReverse.setSelected(poController.Detail(pnDetail).isReversed());

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
        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0 && event.getClickCount() == 2) {
                loadTableDetailFromMain();
                initButton(pnEditMode);
            }
        }
        );
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetailList);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList, tblViewDetailList); // need to use computed-size in min-width of the column to work
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();
        if (pnEditMode == EditMode.ADDNEW) {  //Do not allow to link when edit mode is not equal to add new
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            ModelSalesCommitment_Main selected = (ModelSalesCommitment_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                    String lsTransactionNo = selected.getIndex03();

                    if (!JFXUtil.loadValidation2(pnEditMode, pxeModuleName, poController.Master().getSourceNo(), lsTransactionNo, poController.Master().getTransactionTotal())) {
                        return;
                    }
                    pnMain = pnRowMain;
                    JFXUtil.clearTextFields(apMaster, apDetail);
                    poJSON = poController.populateDetail(lsTransactionNo);
                    if ("error".equals(poJSON.get("result"))) {
                        loadTableDetail.reload();
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    loadTableDetail.reload();
                    moveNext(false, false);

                    JFXUtil.runWithDelay(0.50, () -> {
                        loadTableMain.reload();
                    });
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            }
        } else {
            ShowMessageFX.Warning(null, pxeModuleName, "Data can only be inserted when in ADD mode.");
        }
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetailList,
                details_data,
                () -> {
                    pbEntered = false;
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

        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            main_data.clear();
                            if (poController.getSalesInquiryCount() > 0) {
                                for (int lnCtr = 0; lnCtr <= poController.getSalesInquiryCount() - 1; lnCtr++) {
                                    try {
                                        String lsDate = CustomCommonUtil.formatDateToShortString(poController.SalesInquiryList(lnCtr).getTransactionDate());
                                        main_data.add(new ModelSalesCommitment_Main(String.valueOf(lnCtr + 1),
                                                String.valueOf(lsDate),
                                                String.valueOf(poController.SalesInquiryList(lnCtr).getTransactionNo()),
                                                String.valueOf(poController.SalesInquiryList(lnCtr).Client().getCompanyName()),
                                                String.valueOf(poController.getStatus(poController.SalesInquiryList(lnCtr).getTransactionStatus()).toUpperCase())
                                        ));

                                    } catch (SQLException | GuanzonException ex) {
                                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                    }
                                }
                            }
                            if (pnMain < 0 || pnMain
                                    >= main_data.size()) {
                                if (!main_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewMainList, 0);
                                    pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewMainList, pnMain);
                            }
                            JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                        });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                });
    }

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
                    case "taRemarks"://Remarks
                        poJSON = poController.Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        loadRecordMaster();
                        break;
                }
            });
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfBarcode":
                        if (lsValue.equals("")) {
                            poController.Detail(pnDetail).setStockId("");
                        }
                        break;
                    case "tfDescription":
                        if (lsValue.equals("")) {
                            poController.Detail(pnDetail).setStockId("");
                        }
                        break;
                    case "tfUnitPrice":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setUnitPrice(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
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
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfClient":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.Master().getClientId() != null && !"".equals(poController.Master().getClientId())) {
                                    if (poController.getDetailCount() > 0) {
                                        if (!JFXUtil.isObjectEqualTo(poController.Detail(0).getStockId(), null, "")) {
                                            if (!pbKeyPressed) {
                                                if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                        "Are you sure you want to change the client name?\nPlease note that this action will delete all sales commitment details.\n\nDo you wish to proceed?") == true) {
                                                    poJSON = poController.Master().setClientId("");
//                                                    poController.removeDetails();
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
                                }
                            }

                            poJSON = poController.Master().setClientId("");
                        }
                        break;
                    case "tfApplicationNo": //po no
                        poJSON = poController.Master().setPONumber(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfBank":
                        if (lsValue.isEmpty()) {
                            poController.Master().setBankId("");
                        }
                        break;
                    case "tfTerm":
                        if (lsValue.isEmpty()) {
                            poController.Master().setTermCode("");
                        }
                        break;
                    case "tfATDNumber":
                        poJSON = poController.Master().setATDNumber(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfWTax":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Master().setWithholdingTax(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfWTaxRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Master().setWTaxRate(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                   case "tfVatAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.setVatableAmount(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfVatSales":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.setVatableSales(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfVATRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.setVatRate(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfSalesAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.setVatableSales(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;

                }
                loadRecordMaster();
            });

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
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfBarcode":
                        case "tfDescription":
                        case "tfUnitPrice":
                        case "tfQuantity":
                            moveNext(true, true);
                            event.consume();
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBarcode":
                        case "tfDescription":
                        case "tfUnitPrice":
                        case "tfQuantity":
                            moveNext(false, true);
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        //apMaster
                        case "tfClient":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.getDetailCount() > 1) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the client name?\nPlease note that this action will delete all sales inquiry details.\n\nDo you wish to proceed?") == true) {
//                                        poController.removeDetails();
                                        loadTableDetail.reload();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            poJSON = poController.SearchClient(lsValue, false, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfClient.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfApplicationNo);
                            }
                            loadRecordMaster();
                            retrieveSalesInquiry();
                            return;
                        case "tfTerm":
                            poJSON = poController.SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfBank);
                            }
                            loadRecordMaster();
                            break;
                        case "tfBank":
                            poJSON = poController.SearchBank(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfVATRate);
                            }
                            loadRecordMaster();
                            return;
                        //apDetail
                        case "tfBarcode":
                            poJSON = poController.SearchInventory(lsValue, true, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfUnitPrice);
                            }
                            loadTableDetail.reload();
                            break;
                        case "tfDescription":
                            poJSON = poController.SearchInventory(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfUnitPrice);
                            }
                            loadTableDetail.reload();
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
// CheckBox handler

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
                        poController.Detail(pnDetail).isReversed(cbReverse.isSelected());
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
                String lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                LocalDate ldTransactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                poJSON = new JSONObject();
                switch (datePicker.getId()) {
                    case "dpInquiryDate":
                        break;
                    case "dpTargetDate":
                        break;
                    case "dpTransactionDate":
                        if (ldSelectedDate.isAfter(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Future date is not allowed.");
                            pbSuccess = false;
                        } else {
                            poController.Master().setAppliedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                        }
                        if (pbSuccess) {
                        } else {
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                        pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                        loadTableDetail.reload();
                        pbSuccess = true; //Set to original value
                        break;
                    case "dpAppliedDate":
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Applied date cannot be before the transaction date.");
                            pbSuccess = false;
                        } else if (ldSelectedDate.isAfter(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Future date is not allowed.");
                            pbSuccess = false;
                        } else {
                            poController.Master().setAppliedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                        }
                        if (pbSuccess) {
                        } else {
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                        pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                        loadTableDetail.reload();
                        pbSuccess = true; //Set to original value
                        break;
                    case "dpDueDate":
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Due date cannot be before the transaction date.");
                            pbSuccess = false;
                        } else if (poController.Master().getAppliedDate() != null) {
                            String lsAppliedDate = sdfFormat.format(poController.Master().getAppliedDate());
                            LocalDate ldAppliedDate = LocalDate.parse(lsAppliedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            if (ldSelectedDate.isBefore(ldAppliedDate)) {
                                JFXUtil.setJSONError(poJSON, "Due date cannot be before the applied date.");
                                pbSuccess = false;
                            } else {
                                poController.Master().setDueDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                        }
                        if (pbSuccess) {
                        } else {
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                        pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                        loadTableDetail.reload();
                        pbSuccess = true; //Set to original value
                        break;
                    default:
                        break;
                }
            });

    public void initDatePickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpInquiryDate, dpTargetDate, dpTransactionDate, dpAppliedDate, dpDueDate);
        JFXUtil.setActionListener(datepicker_Action, dpInquiryDate, dpTargetDate, dpTransactionDate, dpAppliedDate, dpDueDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfClient, tfApplicationNo, tfBank, tfTerm, tfATDNumber, tfWTax, tfWTaxRate, tfVatAmount, tfVatSales, tfVATRate, tfSalesAmount);
        JFXUtil.setFocusListener(txtDetail_Focus, tfBarcode, tfDescription, tfUnitPrice, tfQuantity);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail);
        JFXUtil.inputDecimalOnly(tfWTaxRate, tfVATRate);
        JFXUtil.setCommaFormatter(tfVatAmount, tfSalesAmount, tfUnitPrice, tfVatSales);
        CustomCommonUtil.inputIntegersOnly(tfQuantity);

        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetailList, tblViewMainList);

        JFXUtil.adjustColumnForScrollbar(tblViewDetailList, tblViewMainList);

    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnCancelBankApplication);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail);
        JFXUtil.setButtonsVisibility(true, btnRetrieve);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case BankApplicationStatus.OPEN:
                JFXUtil.setButtonsVisibility(true, btnUpdate, btnCancelBankApplication);
                break;
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblNoViewDetailList);
        JFXUtil.setColumnLeft(tblDescription);
        JFXUtil.setColumnRight(tblUnitPrice, tblQuantity, tblTotal);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetailList);
        tblViewDetailList.setItems(details_data);
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblNoViewMainList, tblInquiryDate, tblTransactionNo);
        JFXUtil.setColumnLeft(tblClient, tblStatus);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        tblViewMainList.setItems(main_data);
        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
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
