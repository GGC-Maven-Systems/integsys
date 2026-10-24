/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelVehicleQuotation_AddOns;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleQuotation;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.VehicleQuotationStatus;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.model.ModelVehicleQuotation_Freebies;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehicleSalesQuotation_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0, pnAddOns = 0, pnFreebies = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static VehicleQuotation poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";

    private ObservableList<ModelVehicleQuotation_AddOns> addOns_data = FXCollections.observableArrayList();
    private ObservableList<ModelVehicleQuotation_Freebies> freebies_data = FXCollections.observableArrayList();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    boolean pbEnteredAddOns = false, pbEnteredFreebies = false;
    JFXUtil.ReloadableTableTask loadTableAddOns, loadTableFreebies;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList();
    ArrayList<ArrayList<ArrayList<String>>> array = new ArrayList<ArrayList<ArrayList<String>>>();

    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    ObservableList<String> PurchaseType = ModelSalesInquiry_Detail.PurchaseType;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apAddOns, apPricing, apAdditionalInfo, apFreebies, apRemarks;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnPrint, btnHistory, btnClose;
    @FXML
    private TextField tfSearchClient, tfSearchTransactionNo, tfTransactionNo, tfBranch, tfClient, tfAddress, tfContactNo, tfEmail, tfInquiryNo, tfSalesPerson, tfFinancingRate, tfBrand, tfModel, tfVariant, tfYear, tfColor, tfAddOns, tfAccessories, tfAmount, tfQuantity, tfSRP, tfDownpayment, tfReservationAmt, tfTPL, tfComprehensive, tfLTO, tfCHMODocStmp, tfTotalAmount, tfBank, tfTerms, tfAmountToFinance, tfNetMonthlyInstallment, tfPromptPaymentDiscount, tfGrossMonthlyInstallment, tfPromissoryNoteAmount, tfRate, tfDescription, tfQuantityFreebies;
    @FXML
    private DatePicker dpTransactionDate, dpInquiryDate, dpExpectedDate, dpValidThru;
    @FXML
    private ComboBox cmbModeOfPayment;
    @FXML
    private TableView tblViewAddOns, tblViewFreebies;
    @FXML
    private TableColumn tblNoAddOns, tblDescriptionAddOns, tblAmountAddOns, tblQtyAddOns, tblTotalAddOns, tblNoFreebies, tblDescriptionFreebies, tblQtyFreebies;
    @FXML
    private TextArea taRemarks;

    LocalDate ValidFrom;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehicleQuotation();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initAddOnsGrid();
            initFreebiesGrid();
            initComboboxes();
            initCheckboxes();
            initTextFields();
            initDatepickers();
            initTableOnClick();
            clearTextFields();

            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            Platform.runLater(() -> {
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
                loadRecordSearch();
                if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                } else {
                    poController.Detail(pnDetail).setCompanyId(psCompanyId);
                }
            });
            loadRecordSearch();
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
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
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        return;
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
                    case "btnBrowse":
                        poController.Master().setTransactionStatus("01234");
                        poJSON = poController.SearchTransaction(tfSearchClient.getText(), tfSearchTransactionNo.getText(), true);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.Master().getEditMode();
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadTableAddOns.reload();
                loadTableFreebies.reload();
                loadRecordMaster();
                loadRecordAdditionalInfo();
                loadRecordPricing();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnAddOns;
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    break;
                case F3:
                    switch (lsID) {
                        //apMaster
                        case "tfSearchClient":
//                            poJSON = poController.SearchTransaction(lsValue, tfSearchTransactionNo.getText(), false);

                            poJSON = poController.SearchClient(lsValue);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            tfSearchClient.setText(poController.getClientName());
//                            pnEditMode = poController.getEditMode();
//                            loadTableAddOns.reload();
//                            loadTableFreebies.reload();
//                            loadRecordMaster();
//                            loadRecordAdditionalInfo();
//                            loadRecordPricing();
//                            initButton(pnEditMode);
                            break;
                        case "tfSearchTransactionNo":
                            btnBrowse.fire();
                            break;
                    }
                    break;
                case UP:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfAddOns", "tfAccessories", "tfAmount", "tfQuantity"}, (Runnable) () -> moveNextAddOns(true, true)},
                        {new String[]{"tfDescription", "tfQuantityFreebies"}, (Runnable) () -> moveNextFreebies(true, true)},});

                    break;
                case DOWN:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfAddOns", "tfAccessories", "tfAmount", "tfQuantity"}, (Runnable) () -> moveNextAddOns(false, true)},
                        {new String[]{"tfDescription", "tfQuantityFreebies"}, (Runnable) () -> moveNextFreebies(false, true)},});
                    break;
                default:
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
//        catch ( CloneNotSupportedException | ScriptException  ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        } 
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewAddOns":
                    if (!addOns_data.isEmpty()) {
                        pnAddOns = isMovedDown ? Integer.parseInt(addOns_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                                : Integer.parseInt(addOns_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                        loadRecordAddOns();
                    }
                    break;
                case "tblViewFreebies":
                    if (!freebies_data.isEmpty()) {
                        pnFreebies = isMovedDown ? Integer.parseInt(freebies_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex06())
                                : Integer.parseInt(freebies_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex06());
                        loadRecordFreebies();
                    }
                    break;
            }
        }
    };

    private void resetAmounts() {
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
        } else {
            poController.Detail(pnDetail).setSRPAmount(0.0000);
            poController.Detail(pnDetail).setDownPayment(0.0000);
            poController.Detail(pnDetail).setReservationAmount(0.0000);
        }
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchClient":
                        if (lsValue.isEmpty()) {
                            poController.setClientName("");
                        }
                        break;
                }
            });

    private void loadRecordMaster() {
        try {
            Platform.runLater(() -> {
                JFXUtil.setStatusValue(lblStatus, VehicleQuotationStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());

                String lsStat = "Void";
                if (VehicleQuotationStatus.APPROVED.equals(poController.Master().getTransactionStatus())) {
                    lsStat = "Cancel";
                }
            });
            //if update then disable other nodes
            //if update only enable the expected and valid thru
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            tfTransactionNo.setText(poController.Master().getTransactionNo());
            String lsTransactionDate = JFXUtil.formatDateToString(poController.Master().getTransactionDate());
            JFXUtil.setDateValue(dpTransactionDate, JFXUtil.isObjectEqualTo(lsTransactionDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            String lsExpectedDate = JFXUtil.formatDateToString(poController.Detail(pnDetail).getExpectedDate());
            JFXUtil.setDateValue(dpExpectedDate, JFXUtil.isObjectEqualTo(lsExpectedDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsExpectedDate, "yyyy-MM-dd"));
            String lsValidThru = JFXUtil.formatDateToString(poController.Detail(pnDetail).getValidThruDate());
            JFXUtil.setDateValue(dpValidThru, JFXUtil.isObjectEqualTo(lsValidThru, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidThru, "yyyy-MM-dd"));

            tfBranch.setText(poController.Detail(pnDetail).Branch().getBranchName());
            tfClient.setText(poController.Master().Client().getCompanyName());
            tfAddress.setText(poController.getFullAddress());
            tfContactNo.setText(poController.Master().ClientMobile().getMobileNo());
            tfEmail.setText(poController.Master().ClientEmail().getMailAddress());
            tfInquiryNo.setText(poController.Detail(pnDetail).getSourceNo());
            String lsInquiryDate = JFXUtil.formatDateToString(poController.Detail(pnDetail).SalesInquiry().getTransactionDate());
            JFXUtil.setDateValue(dpInquiryDate, JFXUtil.isObjectEqualTo(lsInquiryDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsInquiryDate, "yyyy-MM-dd"));
            JFXUtil.setCmbValue(cmbModeOfPayment, !poController.Detail(pnDetail).SalesInquiry().getPurchaseType().equals("") ? Integer.valueOf(poController.Detail(pnDetail).SalesInquiry().getPurchaseType()) : -1);
            tfSalesPerson.setText(poController.Detail(pnDetail).SalesInquiry().SalesPerson().getFullName());

            tfBrand.setText(poController.Detail(pnDetail).Brand().getDescription());
            tfModel.setText(poController.Detail(pnDetail).Model().getDescription());
            tfVariant.setText(poController.Detail(pnDetail).ModelVariant().getDescription());
            tfYear.setText(String.valueOf(poController.Detail(pnDetail).ModelVariant().getYearModel()));
            tfColor.setText(poController.Detail(pnDetail).ModelVariant().Color().getDescription());
            taRemarks.setText(poController.Detail(pnDetail).getRemarks());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordAddOns() {
        try {
            if (pnAddOns < 0 || pnAddOns > poController.getAddOnListCount() - 1) {
                return;
            }
            poController.computeFields();
            boolean lbShow1 = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
            JFXUtil.setDisabled(!lbShow1, tfAddOns, tfAccessories);
            JFXUtil.clearNodes(tfAccessories, tfAddOns);
            if (JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).getAddOnId(), null, "")
                    && JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).getStockId(), null, "")) {
                JFXUtil.setDisabled(false, tfAddOns, tfAccessories);
            } else if (JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).getAddOnId(), null, "")) {
                JFXUtil.setDisabled(true, tfAddOns);
                JFXUtil.setDisabled(false, tfAccessories);
                tfAccessories.setText(poController.AddOnList(pnAddOns).Inventory().getDescription());
            } else {
                if (lbShow1) {
                    JFXUtil.setDisabled(false, tfAddOns);
                    JFXUtil.setDisabled(true, tfAccessories);
                }
                tfAddOns.setText(poController.AddOnList(pnAddOns).AddOnMaster().AddOnType().getAddTypeName());
            }

            JFXUtil.setDisabled(poController.AddOnList(pnAddOns).getEditMode() == EditMode.UPDATE, tfAddOns, tfAccessories);
            //dfine the addonid and stockid if available
            //if none enable the searching for add ons and accessories
            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(pnAddOns).getAmount().doubleValue(), true));
            tfQuantity.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(poController.AddOnList(pnAddOns).getQuantity()));
            JFXUtil.updateCaretPositions(apAddOns);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordPricing() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            poController.computeFields();
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), true));
            tfDownpayment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDownPayment().doubleValue(), true));
            tfReservationAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getReservationAmount().doubleValue(), true));
            tfTPL.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getTPLAmount().doubleValue(), true));
            tfComprehensive.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getComprehensiveAmount().doubleValue(), true));
            tfLTO.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getLTOAmount().doubleValue(), true));
            tfCHMODocStmp.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getCHMOAmount().doubleValue(), true));
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getTotalAmount().doubleValue(), true));
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordAdditionalInfo() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            poController.computeFields();
            tfBank.setText(poController.Detail(pnDetail).FinancingRate().Bank().getBankName());
            tfTerms.setText(String.valueOf(poController.Detail(pnDetail).getDuration()));
            tfAmountToFinance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getFinancingAmount().doubleValue(), true));
            tfNetMonthlyInstallment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getMonthlyAmount().doubleValue(), true));
            tfPromptPaymentDiscount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getPromptPaymentDiscount().doubleValue(), true));
            tfGrossMonthlyInstallment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getGrossMonthlyInstallment().doubleValue(), true));
            tfPromissoryNoteAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getPromissoryNoteAmt().doubleValue(), true));
            tfRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getRateValue().doubleValue(), false));
            JFXUtil.updateCaretPositions(apAdditionalInfo);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordFreebies() {
        try {
            if (pnFreebies < 0 || pnFreebies > poController.getFreebiesListCount() - 1) {
                return;
            }
            JFXUtil.setDisabled(poController.FreebiesList(pnFreebies).getEditMode() == EditMode.UPDATE, tfDescription);
            tfDescription.setText(poController.FreebiesList(pnFreebies).Inventory().getDescription());
            tfQuantityFreebies.setText(String.valueOf(poController.FreebiesList(pnFreebies).getQuantity()));
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbSelectAll": // this is the id
                    break;
                case "cbActive":
                    break;
            }
        }
    }

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbModeOfPayment":
                        break;
                }
            });

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
                String lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                LocalDate ldTransactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        break;
                    case "dpInquiryDate":
                        break;
                    case "dpExpectedDate":
                        break;
                    case "dpValidThru":
                        break;
                    default:
                        break;
                }
            });

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelVehicleQuotation_AddOns.class, tblViewAddOns, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 0:
                            break;
                    }
                }, 0);//starts 0,1,2 
    }

    private void initComboboxes() {
        // ComboBox setup
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(PurchaseType, cmbModeOfPayment));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbModeOfPayment);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbModeOfPayment);
    }

    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpInquiryDate, dpExpectedDate, dpValidThru);
        JFXUtil.setActionListener(datepicker_Action, dpTransactionDate, dpInquiryDate, dpExpectedDate, dpValidThru);
    }

    public void moveNextAddOns(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apAddOns.requestFocus();
                pnAddOns = isUp ? Integer.parseInt(addOns_data.get(JFXUtil.moveToPreviousRow(tblViewAddOns)).getIndex07())
                        : Integer.parseInt(addOns_data.get(JFXUtil.moveToNextRow(tblViewAddOns)).getIndex07());
            }
            loadRecordAddOns();
            if (pnAddOns < 0 || pnAddOns > poController.getAddOnListCount() - 1) {
                return;
            }
            if (JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).getAddOnId(), null, "")) {
                JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                    {poController.AddOnList(pnAddOns).Inventory().getDescription(), tfAccessories},
                    {poController.AddOnList(pnAddOns).AddOnMaster().getAmount(), tfAmount},
                    {poController.AddOnList(pnAddOns).getQuantity(), tfQuantity},}, tfQuantity); // default
            } else {
                JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                    {poController.AddOnList(pnAddOns).AddOnMaster().getAmount(), tfAmount},
                    {poController.AddOnList(pnAddOns).getQuantity(), tfQuantity},}, tfQuantity); // default
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void moveNextFreebies(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apFreebies.requestFocus();
            pnFreebies = isUp ? Integer.parseInt(freebies_data.get(JFXUtil.moveToPreviousRow(tblViewFreebies)).getIndex06())
                    : Integer.parseInt(freebies_data.get(JFXUtil.moveToNextRow(tblViewFreebies)).getIndex06());
        }
        loadRecordAddOns();
        if (pnFreebies < 0 || pnFreebies > poController.getFreebiesListCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.FreebiesList(pnFreebies).getAddOnId(), tfDescription},}, tfQuantityFreebies); // default
    }

    public boolean isContainsChecked() {
        return checkedItem != null && checkedItem.contains("1");
    }

    private void processAction(String identifier, String lsValue) {
        boolean lbcb = false;
        String lsMessage = "";
        lsMessage = "remove";
        switch (identifier) {
            case "cbActive":
                lbcb = lsValue.equals("1");
                lsMessage = "activate/deactivate";
                break;
            case "tfAmount":
                lsMessage = "change amount of the";
                break;
        }

        if (checkedItem.stream().anyMatch("1"::equals)) {
        } else {
            ShowMessageFX.Warning(null, pxeModuleName, "No items were selected to " + lsMessage + ".");
            return;
        }
        if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + lsMessage + " selected item/s?")) {
            return;
        }
        String firstValue = null;
        boolean isConsistent = true;
        checkedItems.clear();
        List<Integer> listCount = new ArrayList<>();
        for (Object item : tblViewAddOns.getItems()) {
            ModelVehicleQuotation_AddOns item1 = (ModelVehicleQuotation_AddOns) item;
            String lschecked = item1.getIndex01();
            int lnCtr = Integer.valueOf(item1.getIndex07());
            if (lschecked.equals("1")) {
                listCount.add(Integer.parseInt(item1.getIndex07()));
                String currentValue = item1.getIndex09();
                // checkedItems.add();
                switch (identifier) {
                    case "cbActive":
                        break;
                    case "tfAmount":
                        break;
                }
            }
        }
        if (checkedItem.isEmpty()) {
            return;
        }

        switch (identifier) {
            case "cbActive":
                break;
            case "tfAmount":
                break;
        }

        resetCheckboxSelection();
        pnEditMode = poController.getEditMode();
        loadTableAddOns.reload();
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, apMaster);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.inputDecimalOnly(tfRate);
        JFXUtil.setCommaFormatter(tfAmount, tfReservationAmt, tfTotalAmount, tfAmountToFinance, tfPromissoryNoteAmount);
        CustomCommonUtil.inputIntegersOnly(tfQuantity, tfQuantityFreebies);

        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewAddOns, tblViewFreebies);

        JFXUtil.adjustColumnForScrollbar(tblViewAddOns, tblViewFreebies);
    }

    private String getData(String lsValue) {
        return JFXUtil.isObjectEqualTo(lsValue, null, "") ? "" : lsValue;
    }

    public void initLoadTable() {
        loadTableAddOns = new JFXUtil.ReloadableTableTask(
                tblViewAddOns,
                addOns_data,
                () -> {
                    pbEnteredAddOns = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEnteredAddOns = false;
                        addOns_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadAddOnList();
                            }
                            int lnCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getAddOnListCount(); lnCtr++) {
                                if (pnEditMode == EditMode.READY
                                        && JFXUtil.isObjectEqualTo(poController.AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName(), null, "")
                                        && JFXUtil.isObjectEqualTo(poController.AddOnList(lnCtr).Inventory().getStockId(), null, "")) {
                                    continue;
                                }
                                lnCount += 1;
                                checkedItems(lnCtr);
                                if (poController.AddOnList(lnCtr).getRecordStatus()) { // if true = Active // only this should activate during editMode of the row
                                    checkedItem.set(lnCtr, "1");
                                } else {
                                    checkedItem.set(lnCtr, "0");
                                }

                                String lsDescription = "";
                                if (JFXUtil.isObjectEqualTo(poController.AddOnList(lnCtr).getAddOnId(), null, "")) {
                                    lsDescription = poController.AddOnList(lnCtr).Inventory().getDescription();
                                } else {
                                    lsDescription = poController.AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName();
                                }

                                addOns_data.add(
                                        new ModelVehicleQuotation_AddOns(checkedItem.get(lnCtr),
                                                String.valueOf(lnCount),
                                                getData(lsDescription),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getQuantity(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getTotalAmount(), false)),
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            if (pnAddOns < 0 || pnAddOns
                                    >= addOns_data.size()) {
                                if (!addOns_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewAddOns.getSelectionModel().select(0);
                                    tblViewAddOns.getFocusModel().focus(0);
                                    pnAddOns = tblViewAddOns.getSelectionModel().getSelectedIndex();
                                    loadRecordAddOns();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewAddOns.getSelectionModel().select(pnAddOns);
                                tblViewAddOns.getFocusModel().focus(pnAddOns);
                                loadRecordAddOns();
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableFreebies = new JFXUtil.ReloadableTableTask(
                tblViewFreebies,
                freebies_data,
                () -> {
                    pbEnteredAddOns = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEnteredFreebies = false;
                        freebies_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadFreebiesList();
                            }
                            int lnCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getFreebiesListCount(); lnCtr++) {
                                if (!poController.FreebiesList(lnCtr).getRecordStatus()) { // if inactive
                                    continue;
                                }
                                lnCount += 1;
                                if (pnEditMode == EditMode.READY
                                        && JFXUtil.isObjectEqualTo(poController.FreebiesList(lnCtr).Inventory().getStockId(), null, "")) {
                                    continue;
                                }
                                freebies_data.add(
                                        new ModelVehicleQuotation_Freebies(String.valueOf(lnCount),
                                                getData(poController.FreebiesList(lnCtr).Inventory().getDescription()),
                                                String.valueOf(poController.FreebiesList(lnCtr).getQuantity()),
                                                "",
                                                "",
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(freebies_data, pnFreebies, 06); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= freebies_data.size()) {
                                if (!freebies_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewFreebies, 0);
                                    int lnRow = Integer.parseInt(freebies_data.get(0).getIndex06());
                                    pnFreebies = lnRow;
                                    loadRecordFreebies();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewFreebies.getSelectionModel().select(lnTempRow);
                                tblViewFreebies.getFocusModel().focus(lnTempRow);
                                loadRecordFreebies();
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    private void initAddOnsGrid() {
        JFXUtil.setColumnCenter(tblNoAddOns);
        JFXUtil.setColumnLeft(tblDescriptionAddOns);
        JFXUtil.setColumnRight(tblAmountAddOns, tblQtyAddOns, tblTotalAddOns);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewAddOns);
        tblViewAddOns.setItems(addOns_data);
    }

    private void initFreebiesGrid() {
        JFXUtil.setColumnCenter(tblNoFreebies);
        JFXUtil.setColumnLeft(tblDescriptionFreebies);
        JFXUtil.setColumnRight(tblQtyFreebies);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewFreebies);
        tblViewFreebies.setItems(freebies_data);
    }

    public void clearTextFields() {
        resetCheckboxSelection();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apAddOns, apPricing, apAdditionalInfo, apFreebies);
    }

    private void resetCheckboxSelection() {
    }

    private void checkedItems(int lnCtr) {
        try {
            if (checkedItem.get(lnCtr) == null) {
                checkedItem.add("0");
            }
        } catch (Exception e) {
            checkedItem.add("0");
        }
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTableOnClick() {
        tblViewAddOns.setOnMouseClicked(event -> {
            if (addOns_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleQuotation_AddOns selected = (ModelVehicleQuotation_AddOns) tblViewAddOns.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(addOns_data.get(tblViewAddOns.getSelectionModel().getSelectedIndex()).getIndex07());
                        pnAddOns = lnRow;
                        loadRecordAddOns();
                        moveNextAddOns(false, false);
                    }
                }
            }
        });
        tblViewFreebies.setOnMouseClicked(event -> {
            if (freebies_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleQuotation_Freebies selected = (ModelVehicleQuotation_Freebies) tblViewFreebies.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(freebies_data.get(tblViewFreebies.getSelectionModel().getSelectedIndex()).getIndex06());
                        pnFreebies = lnRow;
                        loadRecordFreebies();
                        moveNextFreebies(false, false);
                    }
                }
            }
        });
    }

    private void initButton(int fnValue) {
        boolean lbShow2 = fnValue == EditMode.READY;
        JFXUtil.setButtonsVisibility(lbShow2, btnHistory, btnPrint);

        JFXUtil.setDisabled(true, apMaster, apAddOns, apPricing, apAdditionalInfo, apFreebies, apRemarks);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case VehicleQuotationStatus.OPEN:
            case VehicleQuotationStatus.VOID:
            case VehicleQuotationStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnPrint);
                break;
        }
    }
}
