/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import java.util.concurrent.atomic.AtomicReference;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.SalesCommitment;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.BankApplicationStatus;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;

/**
 *
 * @author Team 1
 */
public class SalesCommitment_HistoryCarController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesCommitment poController;
    public int pnEditMode;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apInquiry, apFields, apMaster;
    @FXML
    private Label lblSource, lblStatus, lblBankApplicationStatus;
    @FXML
    private TextField tfSearchClient, tfSearchTransactionNo, tfTransNo, tfClientType, tfClient, tfAddress, tfBranch, tfSalesPerson, tfReferralAgent, tfPriorityUnit, tfApplicationNo, tfATDNumber, tfPaymentMode, tfTerm, tfBank, tfInquiryNo, tfInquiryType, tfUnitType, tfSalesAmount, tfVATRate, tfVatSales, tfVatAmount, tfTransactionTotal;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private DatePicker dpTransactionDate, dpAppliedDate, dpApproveDate, dpDueDate, dpInquiryDate, dpTargetDate;
    @FXML
    private TextArea taRemarks;

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
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster);
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
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnCancelBankApplication")) {
                    poController.InitTransaction();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                } else {
                    loadRecordMaster();
                }

                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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

            tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATAmount(), true));
            tfVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATSale(), true));
            tfVATRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATRates(), false));
            tfSalesAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getSalesAmount(), true));
            taRemarks.setText(poController.Master().getRemarks());
            dpAppliedDate.setValue(poController.Master().getAppliedDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getAppliedDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            dpDueDate.setValue(poController.Master().getDueDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getDueDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);
            dpApproveDate.setValue(poController.Master().getApprovedDate() != null ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getApprovedDate(), SQLUtil.FORMAT_SHORT_DATE)) : null);

            tfPriorityUnit.setText(poController.getPriorityUnit());
            if (poController.Master().getSourceNo() != null && !"".equals(poController.Master().getSourceNo())) {
                tfPriorityUnit.setText(poController.getPriorityUnit());
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

    public void initTableOnClick() {
    }

    public void initLoadTable() {
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
                            poJSON = poController.SearchTransaction(tfSearchClient.getText(), tfSearchTransactionNo.getText(),true);
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

    public void retrieveSalesCommitment() {
        try {
            poJSON = new JSONObject();
            poJSON = poController.loadTransactionList(tfSearchClient.getText(), tfSearchTransactionNo.getText());
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            } else {
                loadRecordMaster();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initDatePickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpInquiryDate, dpTargetDate, dpTransactionDate, dpAppliedDate, dpDueDate);
    }

    public void initTextFields() {
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apBrowse);
    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(lbShow2, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(true, apMaster);
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
        JFXUtil.clearTextFields(apMaster, apBrowse);
    }
}
