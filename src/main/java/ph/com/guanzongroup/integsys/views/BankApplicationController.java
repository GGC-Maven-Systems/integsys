/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelBankApplications_Detail;
import ph.com.guanzongroup.integsys.model.ModelRequirements_Detail;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.parser.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.SalesInquiryStatic;
import org.guanzon.appdriver.constant.UserRight;
import ph.com.guanzongroup.cas.sales.t1.status.BankApplicationStatus;

/**
 *
 * @author Team 1
 */
public class BankApplicationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0, pnMain = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesControllers poSalesInquiryController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    boolean pbPurchaseTypeChanged = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private ObservableList<ModelBankApplications_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelBankApplications_Detail> main_data = FXCollections.observableArrayList();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEntered = false;
    private final JFXUtil.RowDragLock dragLock = new JFXUtil.RowDragLock(true);
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);

    ObservableList<String> ClientType = ModelSalesInquiry_Detail.ClientType;
    ObservableList<String> PurchaseType = ModelSalesInquiry_Detail.PurchaseType;
    ObservableList<String> CategoryType = ModelSalesInquiry_Detail.CategoryType;
    ObservableList<String> CustomerGroup = ModelSalesInquiry_Detail.CustomerGroup;
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apInquiry, apFields, apMaster, apDetail, apTableDetail;
    @FXML
    private Label lblSource, lblStatus, lblBankApplicationStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnApprove, btnDispprove, btnCancelBankApplication, btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfBranch, tfSalesPerson, tfReferralAgent, tfInquiryStatus, tfInquiryType, tfClient, tfAddress, tfContactNo, tfClientType, tfCategoryType, tfPaymentMode, tfApplicationNo, tfBank;
    @FXML
    private DatePicker dpTransactionDate, dpTargetDate, dpAppliedDate, dpApprovedDate;
    @FXML
    private ComboBox cmbPurchaseType;
    @FXML
    private TextArea taRemarks, taBankAppRemarks;
    @FXML
    private TableView tblViewDetailList, tblViewMainList;
    @FXML
    private TableColumn tblBankAppRowCb, tblBankAppRowNo, tblBankAppNo, tblBank, tblAppliedDate, tblApprovedDate, tblStatus, tblRowNo, tblDate, tblTransactionNo, tblClient, tblMainStatus;
    @FXML
    private Pagination pgPagination;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        poSalesInquiryController = new SalesControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poSalesInquiryController.SalesInquiry().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initLoadTable();
        initComboBoxes();
        initTextFields();
        initDatePickers();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();
        pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();
        initButton(pnEditMode);

        Platform.runLater(() -> {
            poSalesInquiryController.SalesInquiry().Master().setIndustryId(psIndustryId);
            poSalesInquiryController.SalesInquiry().Master().setCompanyId(psCompanyId);
            poSalesInquiryController.SalesInquiry().Master().setCategoryCode(psCategoryId);
            poSalesInquiryController.SalesInquiry().setIndustryId(psIndustryId);
            poSalesInquiryController.SalesInquiry().setCompanyId(psCompanyId);
            poSalesInquiryController.SalesInquiry().setCategoryId(psCategoryId);
            poSalesInquiryController.SalesInquiry().setWithUI(true);
            loadRecordSearch();

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
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poSalesInquiryController.SalesInquiry().OpenTransaction(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
                        poJSON = poSalesInquiryController.SalesInquiry().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poSalesInquiryController.SalesInquiry().loadRequirements();
                        poSalesInquiryController.SalesInquiry().loadBankApplications();
                        pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apDetail, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            poSalesInquiryController.SalesInquiry().resetMaster();
                            poSalesInquiryController.SalesInquiry().Detail().clear();
                            poSalesInquiryController.SalesInquiry().resetOthers();
                            clearTextFields();

                            poSalesInquiryController.SalesInquiry().Master().setIndustryId(psIndustryId);
                            poSalesInquiryController.SalesInquiry().Master().setCompanyId(psCompanyId);
                            poSalesInquiryController.SalesInquiry().Master().setCategoryCode(psCategoryId);
//                            poSalesInquiryController.SalesInquiry().initFields();
                            pnEditMode = EditMode.UNKNOWN;

                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poSalesInquiryController.SalesInquiry().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poSalesInquiryController.SalesInquiry().AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poSalesInquiryController.SalesInquiry().OpenTransaction(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poSalesInquiryController.SalesInquiry().Master().getTransactionStatus().equals(SalesInquiryStatic.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poSalesInquiryController.SalesInquiry().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnApprove":
                        if (JFXUtil.isObjectEqualTo(tblViewDetailList.getSelectionModel().getSelectedItem(), null, -1)) {
                            ShowMessageFX.Warning("No selected row to approve", pxeModuleName, null);
                            return;
                        }
//                        poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).setTransactionStatus(BankApplicationStatus.APPROVED);
                        poSalesInquiryController.SalesInquiry().ApproveBankApplication("", pnDetail);
                        break;
                    case "btnDispprove":
                        if (JFXUtil.isObjectEqualTo(tblViewDetailList.getSelectionModel().getSelectedItem(), null, -1)) {
                            ShowMessageFX.Warning("No selected row to disapprove", pxeModuleName, null);
                            return;
                        }
//                        poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).setTransactionStatus(BankApplicationStatus.DISAPPROVED);
                        poSalesInquiryController.SalesInquiry().DisApproveBankApplication("", pnDetail);
                        break;
                    case "btnCancelBankApplication":
                        if (JFXUtil.isObjectEqualTo(tblViewDetailList.getSelectionModel().getSelectedItem(), null, -1)) {
                            ShowMessageFX.Warning("No selected row to cancel", pxeModuleName, null);
                            return;
                        }
//                        poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).setTransactionStatus(BankApplicationStatus.CANCELLED);
                        poSalesInquiryController.SalesInquiry().CancelBankApplication("", pnDetail);
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisApprove", "btnCancelApplication")) {
                    loadTableDetail.reload();
                    return;
                }

                if (lsButton.equals("btnPrint")) { //|| lsButton.equals("btnCancel")
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                }

                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.ADDNEW;
        JFXUtil.setDisabled(!lbDisable, tfClient);

        try {
            JFXUtil.setStatusValue(lblStatus, SalesInquiryStatic.class,
                    pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().Master().getTransactionStatus());

            // Transaction Date
            tfTransactionNo.setText(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            String lsTargetDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().Master().getTargetDate());
            dpTargetDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTargetDate, "yyyy-MM-dd"));

            tfBranch.setText(poSalesInquiryController.SalesInquiry().Master().Branch().getBranchName());
            tfSalesPerson.setText(poSalesInquiryController.SalesInquiry().Master().SalesPerson().getFullName());
//            tfReferralAgent.setText(poSalesInquiryController.SalesInquiry().Master().ReferralAgent().getCompanyName());
//            tfClient.setText(poSalesInquiryController.SalesInquiry().Master().Client().getCompanyName());
//            tfAddress.setText(poSalesInquiryController.SalesInquiry().Master().ClientAddress().getAddress());
//            tfContactNo.setText(poSalesInquiryController.SalesInquiry().Master().ClientMobile().getMobileNo());
            tfInquiryType.setText(poSalesInquiryController.SalesInquiry().Master().Source().getDescription());

            taRemarks.setText(poSalesInquiryController.SalesInquiry().Master().getRemarks());

            if (pnEditMode != EditMode.UNKNOWN) {

                cmbPurchaseType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getPurchaseType()));
                if (poSalesInquiryController.SalesInquiry().Master().getClientId() != null && !"".equals(poSalesInquiryController.SalesInquiry().Master().getClientId())) {
//                    tfClientType.setText(String.valueOf(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().Client().getClientType())));
                } else {
                    tfClientType.setText(String.valueOf(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getClientType())));
                }
            } else {
                cmbPurchaseType.getSelectionModel().select(0);
            }

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordDetail() {
        try {
            boolean lbShow1 = poSalesInquiryController.SalesInquiry().getBankApplicationsCount() > 0;
            JFXUtil.setDisabled(!lbShow1, apDetail);
            if (pnDetail < 0 || pnDetail > poSalesInquiryController.SalesInquiry().getBankApplicationsCount() - 1) {
                return;

            }
            JFXUtil.setStatusValue(lblBankApplicationStatus, BankApplicationStatus.class,
                    pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getTransactionStatus());

            JFXUtil.setDisabled(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getEditMode() == EditMode.ADDNEW, apDetail, dpApprovedDate);

            boolean lbShow = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getTransactionStatus(),
                    BankApplicationStatus.APPROVED, BankApplicationStatus.DISAPPROVED, BankApplicationStatus.CANCELLED);
            boolean lbShow2 = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getEditMode(), EditMode.UPDATE);
            JFXUtil.setDisabled(lbShow || lbShow2, tfBank);

            String lsPaymentMode = "";
            if (!JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getPaymentMode(), null, "")) {
                lsPaymentMode = PurchaseType.get(Integer.valueOf(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getPaymentMode()));
            } else {
                lsPaymentMode = "";
            }
            tfPaymentMode.setText(lsPaymentMode);
            tfApplicationNo.setText(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getApplicationNo());
            tfBank.setText(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).Bank().getBankName());
            taBankAppRemarks.setText(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getRemarks());

            String lsdpAppliedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getAppliedDate());
            dpAppliedDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsdpAppliedDate, "yyyy-MM-dd"));

            String lsdpApprovedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getApprovedDate());
            dpApprovedDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsdpApprovedDate, "yyyy-MM-dd"));
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
            switch (currentTableID) {
                case "tblViewDetailList":
                    if (!details_data.isEmpty()) {
                        pnDetail = newIndex;
                        loadRecordDetail();
                    }
                    break;
            }
        }
    };

    public void initTableOnClick() {
        tblViewDetailList.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewDetailList.getSelectionModel().getSelectedIndex();
                    moveNextBankApplications(false, false);
                }
            }
        });
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetailList);
        JFXUtil.adjustColumnForScrollbar(tblViewDetailList); // need to use computed-size in min-width of the column to work

        JFXUtil.addCheckboxColumns(ModelRequirements_Detail.class,
                tblViewDetailList, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 0:
                            poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(rowIndex).isRequired(lbisTrue);
                            pnDetail = rowIndex;
                            loadTableDetail.reload();
                            break;
                    }
                },
                0);
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {
                            if (pnEditMode != EditMode.UNKNOWN) {
                                poSalesInquiryController.SalesInquiry().loadBankApplicationList();
                            }
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getBankApplicationsCount(); lnCtr++) {
                                String lsAppliedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getAppliedDate());
                                String lsApprovedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getApprovedDate());

                                String lsStat = ""; //default
                                lsStat
                                        = JFXUtil.setStatusValue(lblBankApplicationStatus, BankApplicationStatus.class,
                                                pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getTransactionStatus());

                                String lsBank = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName(), null, "")
                                        ? "" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName();

                                details_data.add(
                                        new ModelBankApplications_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getApplicationNo()),
                                                String.valueOf(lsBank),
                                                String.valueOf(lsAppliedDate),
                                                String.valueOf(lsApprovedDate),
                                                String.valueOf(lsStat)
                                        )
                                );
                            }
                            if (pnMain < 0 || pnMain
                                    >= main_data.size()) {
                                if (!main_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetailList, 0);
                                    pnMain = tblViewDetailList.getSelectionModel().getSelectedIndex();
                                }
                                loadRecordDetail();
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetailList, pnDetail);
                                loadRecordDetail();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetailList,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {
                            if (pnEditMode != EditMode.UNKNOWN) {
                                poSalesInquiryController.SalesInquiry().loadBankApplicationList();
                            }
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getBankApplicationsCount(); lnCtr++) {
                                String lsAppliedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getAppliedDate());
                                String lsApprovedDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getApprovedDate());

                                String lsStat = ""; //default
                                lsStat
                                        = JFXUtil.setStatusValue(lblBankApplicationStatus, BankApplicationStatus.class,
                                                pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getTransactionStatus());

                                String lsBank = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName(), null, "")
                                        ? "" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName();

                                details_data.add(
                                        new ModelBankApplications_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getApplicationNo()),
                                                String.valueOf(lsBank),
                                                String.valueOf(lsAppliedDate),
                                                String.valueOf(lsApprovedDate),
                                                String.valueOf(lsStat)
                                        )
                                );
                            }
                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetailList, 0);
                                    pnDetail = tblViewDetailList.getSelectionModel().getSelectedIndex();
                                }
                                loadRecordDetail();
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetailList, pnDetail);
                                loadRecordDetail();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });

    }

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
//                    case "taRemarks"://Remarks
//                        poJSON = poSalesInquiryController.SalesInquiry().Master().setRemarks(lsValue);
//                        if ("error".equals((String) poJSON.get("result"))) {
//                            System.err.println((String) poJSON.get("message"));
//                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                            return;
//                        }
//                        loadRecordMaster();
//                        break;
                    case "taBankAppRemarks"://Remarks
                        poJSON = poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        loadRecordDetail();
                        break;
                }
            }
    );

//    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
//            (lsID, lsValue) -> {
//                /*Lost Focus*/
//                switch (lsID) {
//                    case "tfSalesPerson":
//                        if (lsValue.isEmpty()) {
//                            poJSON = poSalesInquiryController.SalesInquiry().Master().setSalesMan("");
//                        }
//                        break;
//                    case "tfReferralAgent":
//                        if (lsValue.isEmpty()) {
//                            poJSON = poSalesInquiryController.SalesInquiry().Master().setAgentId("");
//                        }
//                        break;
//                    case "tfInquiryType":
//                        if (lsValue.isEmpty()) {
//                            poJSON = poSalesInquiryController.SalesInquiry().Master().setSourceCode("");
//                        }
//                        break;
//                    case "tfClient":
//                        if (lsValue.isEmpty()) {
//                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                                if (poSalesInquiryController.SalesInquiry().Master().getClientId() != null && !"".equals(poSalesInquiryController.SalesInquiry().Master().getClientId())) {
//                                    if (poSalesInquiryController.SalesInquiry().getDetailCount() > 0) {
//                                        if (!JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().Detail(0).getBrandId(), null, "")) {
//                                            if (!pbKeyPressed) {
//                                                if (ShowMessageFX.YesNo(null, pxeModuleName,
//                                                        "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all purchase order receiving details.\n\nDo you wish to proceed?") == true) {
//                                                    poJSON = poSalesInquiryController.SalesInquiry().Master().setClientId("");
//                                                    poJSON = poSalesInquiryController.SalesInquiry().Master().setAddressId("");
//                                                    poJSON = poSalesInquiryController.SalesInquiry().Master().setContactId("");
//                                                    poSalesInquiryController.SalesInquiry().removeDetails();
//                                                    loadTableDetail.reload();
//                                                } else {
//                                                    loadRecordMaster();
//                                                    return;
//                                                }
//                                            } else {
//                                                loadRecordMaster();
//                                                return;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            poJSON = poSalesInquiryController.SalesInquiry().Master().setClientId("");
//                        }
//                        break;
//
//                }
//                loadRecordMaster();
//            }
//    );
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfApplicationNo":
                        poJSON = poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).setApplicationNo(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        JFXUtil.runWithDelay(0.70, () -> loadTableDetail.reload());
                        break;
                    case "tfBank":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).setBankId(lsValue);
                            JFXUtil.runWithDelay(0.70, () -> loadTableDetail.reload());
                        }
                        break;
                }
                loadRecordDetail();
            }
    );

    public void moveNextBankApplications(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDetail.requestFocus();
            pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewDetailList) : JFXUtil.moveToNextRow(tblViewDetailList);
        }
        loadRecordDetail();
        if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getApplicationNo(), null, "")) {
            tfApplicationNo.requestFocus();
        } else if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).getBankId(), null, "")) {
            tfBank.requestFocus();
        } else {
            tfApplicationNo.requestFocus();
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
                        case "tfApplicationNo":
                        case "tfBank":
                            moveNextBankApplications(true, true);
                            event.consume();
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfApplicationNo":
                        case "tfBank":
                            moveNextBankApplications(false, true);
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfBank":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchBank(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfBank.setText("");
                                break;
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
        }
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                String lsTransDate = sdfFormat.format(poSalesInquiryController.SalesInquiry().Master().getTransactionDate());
                LocalDate ldTransactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                poJSON = new JSONObject();
//                try {
                switch (datePicker.getId()) {
//                    case "dpTargetDate":
//                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
//                            JFXUtil.setJSONError(poJSON, "Target date cannot be before the transaction date.");
//                            pbSuccess = false;
//                        } else {
//                            poSalesInquiryController.SalesInquiry().Master().setTargetDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
//                        }
//                        if (pbSuccess) {
//                        } else {
//                            if ("error".equals((String) poJSON.get("result"))) {
//                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                            }
//                        }
//                        pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
//                        loadRecordMaster();
//                        pbSuccess = true; //Set to original value
//                        break;
                    case "dpAppliedDate":
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Applied date cannot be before the transaction date.");
                            pbSuccess = false;
                        } else {
                            poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).setAppliedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpApprovedDate":
                        if (ldSelectedDate.isBefore(ldTransactionDate)) {
                            JFXUtil.setJSONError(poJSON, "Approved date cannot be before the transaction date.");
                            pbSuccess = false;
                        } else {
                            poSalesInquiryController.SalesInquiry().BankApplicationsList(pnDetail).setApprovedDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
//                } catch (SQLException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                }
            });

    final EventHandler<ActionEvent> comboBoxActionListener = event -> {
        Platform.runLater(() -> {
            try {
                Object source = event.getSource();
                @SuppressWarnings("unchecked")
                ComboBox<?> cb = (ComboBox<?>) source;

                String cbId = cb.getId();
                int selectedIndex = cb.getSelectionModel().getSelectedIndex();
                switch (cbId) {
                    case "cmbPurchaseType":
                        if (poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() > 0 || poSalesInquiryController.SalesInquiry().getBankApplicationsCount() > 0) {
                            if (!poSalesInquiryController.SalesInquiry().Master().getPurchaseType().equals(String.valueOf(cmbPurchaseType.getSelectionModel().getSelectedIndex()))) {
                                poJSON = poSalesInquiryController.SalesInquiry().checkPendingBankApplication();
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                }

                                if (ShowMessageFX.YesNo(null, pxeModuleName,
                                        "Are you sure you want to change the Purchase Type?\nPlease note that doing so will reset the Requirements & Bank Applications list.\n\nDo you wish to proceed?") == true) {
                                    poSalesInquiryController.SalesInquiry().Master().setPurchaseType(String.valueOf(selectedIndex));
                                    poJSON = poSalesInquiryController.SalesInquiry().removeRequirements();
                                    poJSON = poSalesInquiryController.SalesInquiry().removeBankApplications();
                                    if ("error".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                        break;
                                    }
                                    pbPurchaseTypeChanged = true;
                                }
                            }
                        } else {
                            poSalesInquiryController.SalesInquiry().Master().setPurchaseType(String.valueOf(selectedIndex));
                        }
                        break;
//                    case "cmbCategoryType":
//                        poSalesInquiryController.SalesInquiry().Master().setCategoryType(String.valueOf(selectedIndex));
//                        break;

                    default:
                        System.out.println("Unrecognized ComboBox ID: " + cbId);
                        break;
                }

                if (!cbId.equals("cmbCustomerGroup")) {
                    loadRecordMaster();
                }
            } catch (GuanzonException | SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }
        });
    };

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(PurchaseType, cmbPurchaseType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbPurchaseType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbPurchaseType);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpTargetDate, dpAppliedDate, dpApprovedDate);
        JFXUtil.setActionListener(datepicker_Action, dpTransactionDate, dpTargetDate, dpAppliedDate, dpApprovedDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks, taBankAppRemarks);
//        JFXUtil.setFocusListener(txtMaster_Focus, tfClient, tfSalesPerson, tfReferralAgent, tfInquiryType);

        JFXUtil.setFocusListener(txtDetail_Focus, tfApplicationNo, tfBank);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setDisabled(oApp.getUserLevel() <= UserRight.ENCODER, tfSalesPerson);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        dragLock.isEnabled = lbShow;
        disableRowCheckbox.set(!lbShow); // set enable/disable in checkboxes in requirements
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow, taRemarks, apMaster, apDetail, apDetail);

        switch (poSalesInquiryController.SalesInquiry().Master().getTransactionStatus()) {
            case SalesInquiryStatic.QUOTED:
            case SalesInquiryStatic.SALE:
            case SalesInquiryStatic.LOST:
            case SalesInquiryStatic.VOID:
            case SalesInquiryStatic.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblBankAppRowNo, tblBankAppNo, tblAppliedDate, tblApprovedDate, tblStatus);
        JFXUtil.setColumnLeft(tblBank);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetailList);
        tblViewDetailList.setItems(details_data);
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblTransactionNo);
        JFXUtil.setColumnLeft(tblClient, tblMainStatus);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        tblViewMainList.setItems(main_data);
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poSalesInquiryController.SalesInquiry().Master().Company().getCompanyName() + " - " + poSalesInquiryController.SalesInquiry().Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse, apDetail);
    }
}
