/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
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
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import ph.com.guanzongroup.cas.sales.VehicleQuotation;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.VehicleQuotationStatus;
import ph.com.guanzongroup.integsys.model.ModelVehicleQuotation_Freebies;
import ph.com.guanzongroup.integsys.model.ModelVehicleQuotation_SpecialRequests;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehicleSalesQuotation_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0, pnAddOns = 0, pnFreebies = 0, pnSpecialRequest = 0;
    private int pnAttachment = 0;
    private int currentIndex = 0;
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static VehicleQuotation poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";

    private ObservableList<ModelVehicleQuotation_AddOns> addOns_data = FXCollections.observableArrayList();
    private ObservableList<ModelVehicleQuotation_Freebies> freebies_data = FXCollections.observableArrayList();
    private ObservableList<ModelVehicleQuotation_SpecialRequests> specialrequests_data = FXCollections.observableArrayList();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    String lsTypeFreebies = "", lsTypeAddOns = "";
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    boolean pbEnteredAddOns = false, pbEnteredFreebies = false, pbEnteredSpecialRequests = false;
    JFXUtil.ReloadableTableTask loadTableAddOns, loadTableFreebies, loadTableSpecialRequests, loadTableAttachment;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList();
    ArrayList<ArrayList<ArrayList<String>>> array = new ArrayList<ArrayList<ArrayList<String>>>();

    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    public static ObservableList<String> PurchaseType = FXCollections.observableArrayList(
            "Cash",
            "Financing",
            "PO"
    );
    ObservableList<String> chargeTo = FXCollections.observableArrayList(
            "Client",
            "Dealer",
            "Bank",
            "Plant",
            "Free"
    );
    ObservableList<String> paymentTiming = FXCollections.observableArrayList(
            "Pay with Quotation",
            "Pay Later"
    );
    ObservableList<String> receivingmethod = FXCollections.observableArrayList("Pickup", "Delivery");
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apVehicleInfo, apPricing, apAdditionalInfo, apRemarks, apAddOns, apFreebies, apSpecialRequests, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblStatus, lblTotalDiscount;
    @FXML
    private TextField tfSearchClient, tfSearchTransactionNo, tfTransactionNo, tfClient, tfAddress, tfContactNo, tfEmail, tfInquiryNo, tfBranch, tfSalesPerson, tfBrand, tfModel, tfVariant, tfTransmission, tfYear, tfColor, tfSRP, tfDownpayment, tfReservationAmount, tfTPLAmount, tfComprehensiveAmount, tfLTOAmount, tfCHMODocStamp, tfOtherAddOnsTotal, tfTotalDiscount, tfTotalAmount, tfCoverageYear, tfBank, tfTerms, tfRate, tfAmountToFinance, tfNetMonthlyInstallment, tfPromptPaymentDiscount, tfGrossMonthlyInstallment, tfPromissoryNoteAmount, tfAddOns, tfAccessories, tfAmount, tfQuantity, tfDescription, tfQuantityFreebies, tfSpecialRequestDesc, tfSpecialRequestQuantity, tfSpecialRequestAmount, tfAttachmentNo;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnDeduct, btnPrint, btnRemake, btnHistory, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private Tab tabQuotation, tabAttachments;
    @FXML
    private DatePicker dpTransactionDate, dpExpectedDate, dpInquiryDate, dpValidThru;
    @FXML
    private ComboBox cmbReceivingMethod, cmbModeOfPayment, cmbPaymentTiming, cmbSpecialRequestChargeTo;
    @FXML
    private CheckBox cbLockIn, cbActive, cbActiveSpecialRequest;
    @FXML
    private TextArea taRemarks, taSpecialRequestRemarks;
    @FXML
    private TableView tblViewAddOns, tblViewFreebies, tblViewSpecialRequests, tblAttachments;
    @FXML
    private TableColumn tblNoAddOns, tblDescriptionAddOns, tblAmount, tblQuantityAddOns, tblTotal, tblNoFreebies, tblDescriptionFreebies, tblQuantityFreebies, tblSpecialRequestNo, tblSpecialRequestDescription, tblSpecialRequestAmount, tblSpecialRequestQuantity, tblSpecialRequestTotalAmount, tblSpecialRequestChargeTo, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

    LocalDate ValidFrom;
    JFXUtil.StageManager stageDialog = new JFXUtil.StageManager();

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
            initSpecialRequestGrid();
            initAttachmentsGrid();
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
//                    poController.Detail(pnDetail).setCompanyId(psCompanyId);
                }
            });
            loadRecordSearch();
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
            btnArrowLeft.setOnAction(this::cmdButton_Click);
            btnArrowRight.setOnAction(this::cmdButton_Click);
            initTabPane();
            initAttachmentPreviewPane();
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
                    case "btnPrint":
                        try {
                        poController.printTransaction();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error(MiscUtil.getException(npe), pxeModuleName, null);
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
                        JFXUtil.clickTabByTitleText(tabPaneMain, "Quotation");
                        break;
                    case "btnArrowRight":
                        slideImage(1);
                        return;
                    case "btnArrowLeft":
                        slideImage(-1);
                        return;
                    case "btnDeduct":
                        openParameter("deduct", "");
                        break;
                    case "btnRemake":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to remake the transaction?") == true) {
                            //OPEN Quotation Entry in new tab
                            openParameter("remake", poController.Master().getTransactionNo());
                        }
                        return;
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

    //unique OpenParameter() since btnRemake is available
    private void openParameter(String lsType, String lsTransactionNo) {
        String lsFXML = "";
        String lsTitle = "";
        Object controller = new Object();

        switch (lsType) {
            case "deduct":
                VehicleSalesQuotation_DiscountController controller1 = new VehicleSalesQuotation_DiscountController(); //Should differ the parameter calling
                lsFXML = "/ph/com/guanzongroup/integsys/views/VehicleSalesQuotation_Discount.fxml";
                controller1.setGRider(oApp);
                controller1.setIndustryID(psIndustryId);
                controller1.setCompanyID(psCompanyId);
                controller1.setObject(poController);
                controller = controller1;
                lsTitle = "Vehicle Sales QUuotation Discount Dialog";
                break;
            case "remake":
                if (lsTransactionNo.isEmpty()) {
                    ShowMessageFX.Warning(null, pxeModuleName, "Transaction No cannot be empty.");
                    return;
                }
                VehicleSalesQuotation_EntryController controller2 = new VehicleSalesQuotation_EntryController(); //Should differ the parameter calling
                //pass id in controller of entry
                lsFXML = "/ph/com/guanzongroup/integsys/views/VehicleSalesQuotation_Entry.fxml";
                controller2.setGRider(oApp);
                controller2.setIndustryID(psIndustryId);
                controller2.setCompanyID(psCompanyId);
                controller2.setTransactionNo(lsTransactionNo);
                controller = controller2;
                lsTitle = "Vehicle Sales QUuotation Entry Dialog";
                break;
        }
        poJSON = new JSONObject();
        if (stageDialog != null) {
            stageDialog.closeDialog();
            stageDialog = new JFXUtil.StageManager();
        } else {
            stageDialog = new JFXUtil.StageManager();
        }
        try {
            stageDialog.setOnHidden(event -> {
                stageDialog = null;
                reloadRecords();
            });
            stageDialog.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource(lsFXML), controller, lsTitle, true, false, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void reloadRecords() {
        loadRecordMaster();
        loadTableAddOns.reload();
        loadTableFreebies.reload();
        loadRecordPricing();
        loadRecordAdditionalInfo();
        loadTableSpecialRequests.reload();
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
                            poJSON = poController.SearchClient(lsValue);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            tfSearchClient.setText(poController.getClientName());
                            JFXUtil.updateCaretPositions(apBrowse);
                            break;
                        case "tfSearchTransactionNo":
                            btnBrowse.fire();
                            break;
                    }
                    break;
                case UP:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfAddOns", "tfAccessories", "tfAmount", "tfQuantity"}, (Runnable) () -> moveNextAddOns(true, true)},
                        {new String[]{"tfDescription", "tfQuantityFreebies"}, (Runnable) () -> moveNextFreebies(true, true)},
                        {new String[]{"tfSpecialRequestDesc", "tfSpecialRequestAmount"}, (Runnable) () -> moveNextSpecialRequests(true, true)},});

                    break;
                case DOWN:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfAddOns", "tfAccessories", "tfAmount", "tfQuantity"}, (Runnable) () -> moveNextAddOns(false, true)},
                        {new String[]{"tfDescription", "tfQuantityFreebies"}, (Runnable) () -> moveNextFreebies(false, true)},
                        {new String[]{"tfSpecialRequestDesc", "tfSpecialRequestAmount"}, (Runnable) () -> moveNextSpecialRequests(false, true)},});
                    break;
                default:
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewAddOns":
                    if (!addOns_data.isEmpty()) {
                        int lnNumber = isMovedDown ? JFXUtil.moveToNextRow(currentTable)
                                : JFXUtil.moveToPreviousRow(currentTable);
                        lsTypeAddOns = addOns_data.get(lnNumber).getIndex08();
                        int lnNum = Integer.parseInt(addOns_data.get(lnNumber).getIndex07());
                        pnAddOns = isMovedDown ? Integer.parseInt(addOns_data.get(getDetailFilter(addOns_data, lnNum, 7, lsTypeAddOns, 8, 2)).getIndex07())
                                : Integer.parseInt(addOns_data.get(getDetailFilter(addOns_data, lnNum, 7, lsTypeAddOns, 8, 2)).getIndex07());

                        loadRecordAddOns();
                    }
                    break;
                case "tblViewFreebies":
                    if (!freebies_data.isEmpty()) {
                        int lnNumber = isMovedDown ? JFXUtil.moveToNextRow(currentTable)
                                : JFXUtil.moveToPreviousRow(currentTable);
                        lsTypeFreebies = freebies_data.get(lnNumber).getIndex05();
                        int lnNum = Integer.parseInt(freebies_data.get(lnNumber).getIndex06());
                        pnFreebies = isMovedDown ? Integer.parseInt(freebies_data.get(getDetailFilter(freebies_data, lnNum, 6, lsTypeFreebies, 5, 1)).getIndex06())
                                : Integer.parseInt(freebies_data.get(getDetailFilter(freebies_data, lnNum, 6, lsTypeFreebies, 5, 1)).getIndex06());
                        loadRecordFreebies();
                    }
                    break;
                case "tblAttachments":
                    if (!attachment_data.isEmpty()) {
                        pnAttachment = isMovedDown ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03())
                                : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                        loadRecordAttachment(true);
                    }
                    break;
                case "tblViewSpecialRequests":
                    if (!specialrequests_data.isEmpty()) {
                        pnSpecialRequest = isMovedDown ? Integer.parseInt(specialrequests_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                                : Integer.parseInt(specialrequests_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                        loadRecordSpecialRequest();
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
            });

            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            tfTransactionNo.setText(poController.Master().getTransactionNo());
            String lsTransactionDate = JFXUtil.formatDateToString(poController.Master().getTransactionDate());
            JFXUtil.setDateValue(dpTransactionDate, JFXUtil.isObjectEqualTo(lsTransactionDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfClient.setText(poController.Master().Client().getCompanyName());
            tfAddress.setText(poController.getFullAddress());
            tfContactNo.setText(poController.Master().ClientMobile().getMobileNo());
            tfEmail.setText(poController.Master().ClientEmail().getMailAddress());

            String lsExpectedDate = JFXUtil.formatDateToString(poController.Detail(pnDetail).getExpectedDate());
            JFXUtil.setDateValue(dpExpectedDate, JFXUtil.isObjectEqualTo(lsExpectedDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsExpectedDate, "yyyy-MM-dd"));
            String lsValidThru = JFXUtil.formatDateToString(poController.Detail(pnDetail).getValidThruDate());
            JFXUtil.setDateValue(dpValidThru, JFXUtil.isObjectEqualTo(lsValidThru, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidThru, "yyyy-MM-dd"));
            tfInquiryNo.setText(poController.Detail(pnDetail).getSourceNo());
            String lsInquiryDate = JFXUtil.formatDateToString(poController.Detail(pnDetail).SalesInquiry().getTransactionDate());
            JFXUtil.setDateValue(dpInquiryDate, JFXUtil.isObjectEqualTo(lsInquiryDate, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsInquiryDate, "yyyy-MM-dd"));
            tfBranch.setText(poController.Detail(pnDetail).Branch().getBranchName());
            tfSalesPerson.setText(poController.Detail(pnDetail).SalesInquiry().SalesPerson().getFullName());
            taRemarks.setText(poController.Detail(pnDetail).getRemarks());

            JFXUtil.updateCaretPositions(apMaster);
            loadRecordVehicleInfo();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordVehicleInfo() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            tfBrand.setText(poController.Detail(pnDetail).Brand().getDescription());
            tfModel.setText(poController.Detail(pnDetail).Model().getDescription());
            tfVariant.setText(JFXUtil.concatStrings(poController.Detail(pnDetail).ModelVariant().getDescription(), poController.Detail(pnDetail).ModelVariant().VariantInsurance().getTransmission()));
            tfTransmission.setText(poController.Detail(pnDetail).ModelVariant().VariantInsurance().getTransmission());
            tfYear.setText(String.valueOf(poController.Detail(pnDetail).ModelVariant().getYearModel()));
            tfColor.setText(poController.Detail(pnDetail).ModelVariant().Color().getDescription());
            JFXUtil.setCmbValue(cmbReceivingMethod, !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getReceivingMethod(), null, "") ? poController.Detail(pnDetail).getReceivingMethod() : -1);
            JFXUtil.updateCaretPositions(apVehicleInfo);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordAddOns() {
        try {
            JFXUtil.clearTextFields(apAddOns);
            poController.computeFields();
            boolean lbShow1 = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
            JFXUtil.setDisabled(!lbShow1, tfAddOns, tfAccessories);
            JFXUtil.clearNodes(tfAccessories, tfAddOns);
            JFXUtil.setDisabled(false, tfAddOns, tfAccessories);
            switch (addOns_data.get(getDetailFilter(addOns_data, pnAddOns, 7, lsTypeAddOns, 8, 2)).getIndex08()) {
                case "AddOn":
                    if (pnAddOns < 0 || pnAddOns > poController.getAddOnListCount() - 1) {
                        return;
                    }
                    if (poController.AddOnList(pnAddOns).getEditMode() == EditMode.UPDATE) {
                        JFXUtil.setDisabled(true, tfAddOns, tfAccessories);
                    }
                    if (lbShow1) {
                        if (JFXUtil.isObjectEqualTo(poController.AddOnList(pnAddOns).AddOnMaster().getAddOnId(), null, "")) {
                            JFXUtil.setDisabled(false, tfAddOns, tfAccessories);
                        } else {
                            JFXUtil.setDisabled(false, tfAddOns);
                            JFXUtil.setDisabled(true, tfAccessories);
                        }
                    }
                    tfAddOns.setText(poController.AddOnList(pnAddOns).AddOnMaster().AddOnType().getAddTypeName());
                    tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(pnAddOns).getAmount().doubleValue(), false));
                    tfQuantity.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(poController.AddOnList(pnAddOns).getQuantity()));
                    break;
                case "Freebies":
                    if (pnAddOns < 0 || pnAddOns > poController.getFreebiesListCount() - 1) {
                        return;
                    }
                    if (poController.FreebiesList(pnAddOns).getEditMode() == EditMode.UPDATE) {
                        JFXUtil.setDisabled(true, tfAddOns, tfAccessories);
                    } else {
                        JFXUtil.setDisabled(true, tfAddOns);
                        JFXUtil.setDisabled(false, tfAccessories);
                    }

                    tfAccessories.setText(poController.FreebiesList(pnAddOns).Freebie().getDescription());
                    tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.FreebiesList(pnAddOns).getAmount().doubleValue(), false));
                    tfQuantity.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(poController.FreebiesList(pnAddOns).getQuantity()));
                    break;
                case "Inclusion":
                    if (pnAddOns < 0 || pnAddOns > poController.getInclusionListCount() - 1) {
                        return;
                    }
                    if (poController.InclusionList(pnAddOns).getEditMode() == EditMode.UPDATE) {
                        JFXUtil.setDisabled(true, tfAddOns, tfAccessories);
                    } else {
                        JFXUtil.setDisabled(true, tfAddOns);
                        JFXUtil.setDisabled(false, tfAccessories);
                    }
                    tfAccessories.setText(poController.InclusionList(pnAddOns).Inclusion().getDescription());
                    tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.InclusionList(pnAddOns).getAmount().doubleValue(), false));
                    tfQuantity.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(poController.InclusionList(pnAddOns).getQuantity()));
                    break;
            }
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
            boolean lbIsDiscountNone = poController.getDiscountListCount() <= 0;
            JFXUtil.setVisibility(!lbIsDiscountNone, tfTotalDiscount);

            poController.computeFields();

            JFXUtil.setDisabled(poController.isDisable(VehicleQuotationStatus.AddOnType.LTO), tfLTOAmount);
            JFXUtil.setDisabled(poController.isDisable(VehicleQuotationStatus.AddOnType.TPL), tfTPLAmount);
            JFXUtil.setDisabled(poController.isDisable(VehicleQuotationStatus.AddOnType.COMPREHENSIVE), tfComprehensiveAmount);
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), true));
            tfDownpayment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDownPayment().doubleValue(), true));
            tfReservationAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getReservationAmount().doubleValue(), true));
            tfTPLAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getTPLAmount().doubleValue(), true));
            tfComprehensiveAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getComprehensiveAmount().doubleValue(), true));
            tfLTOAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getLTOAmount().doubleValue(), true));
            tfCHMODocStamp.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getCHMOAmount().doubleValue(), true));
            JFXUtil.setVisibility(poController.getTotalDiscount().doubleValue() > 0.00, tfTotalDiscount, lblTotalDiscount);
            tfTotalDiscount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getTotalDiscount().doubleValue(), false));

            tfOtherAddOnsTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getOtherAddOnTotal().doubleValue(), true));
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getTotalAmount().doubleValue(), true));
            JFXUtil.setCmbValue(cmbModeOfPayment, !poController.Detail(pnDetail).getPaymentForm().equals("") ? Integer.valueOf(poController.Detail(pnDetail).getPaymentForm()) : -1);
            cbLockIn.setSelected(poController.Detail(pnDetail).isInsuranceLocked());
            tfCoverageYear.setText(String.valueOf(poController.Detail(pnDetail).getInsuranceYears()));
            JFXUtil.setCmbValue(cmbPaymentTiming, !"".equals(poController.Detail(pnDetail).getPaymentOption()) ? Integer.valueOf(poController.Detail(pnDetail).getPaymentOption()) : -1);

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
            tfBank.setText(poController.Detail(pnDetail).Banks().getBankName());
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
        JFXUtil.clearTextFields(apFreebies);
        try {

//            JFXUtil.setDisabled(poController.FreebiesList(pnFreebies).getEditMode() == EditMode.UPDATE, tfDescription);
            //identifier 2
            switch (freebies_data.get(getDetailFilter(freebies_data, pnFreebies, 6, lsTypeFreebies, 5, 1)).getIndex05()) {
                case "AddOn":
                    if (pnFreebies < 0 || pnFreebies > poController.getAddOnListCount() - 1) {
                        return;
                    }
                    JFXUtil.setDisabled(poController.AddOnList(pnFreebies).getEditMode() == EditMode.UPDATE, tfDescription);
                    tfDescription.setText(poController.AddOnList(pnFreebies).AddOnMaster().AddOnType().getAddTypeName());
                    tfQuantityFreebies.setText(String.valueOf(poController.AddOnList(pnFreebies).getQuantity()));
                    cbActive.setSelected(poController.AddOnList(pnFreebies).getRecordStatus());
                    break;
                case "Freebies":
                    if (pnFreebies < 0 || pnFreebies > poController.getFreebiesListCount() - 1) {
                        return;
                    }
                    JFXUtil.setDisabled(poController.FreebiesList(pnFreebies).getEditMode() == EditMode.UPDATE, tfDescription);
                    tfDescription.setText(poController.FreebiesList(pnFreebies).Freebie().getDescription());
                    tfQuantityFreebies.setText(String.valueOf(poController.FreebiesList(pnFreebies).getQuantity()));
                    cbActive.setSelected(poController.FreebiesList(pnFreebies).getRecordStatus());
                    break;
                case "Inclusion":
                    if (pnFreebies < 0 || pnFreebies > poController.getInclusionListCount() - 1) {
                        return;
                    }
                    JFXUtil.setDisabled(poController.InclusionList(pnFreebies).getEditMode() == EditMode.UPDATE, tfDescription);
                    tfDescription.setText(poController.InclusionList(pnFreebies).Inclusion().getDescription());
                    tfQuantityFreebies.setText(String.valueOf(poController.InclusionList(pnFreebies).getQuantity()));
                    cbActive.setSelected(poController.InclusionList(pnFreebies).getRecordStatus());
                    break;
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordSpecialRequest() {
        try {
            poController.computeFields();
            if (pnSpecialRequest < 0 || pnSpecialRequest > poController.getSpecialRequestListCount() - 1) {
                return;
            }
            tfSpecialRequestDesc.setText(poController.SpecialRequestList(pnSpecialRequest).getRequestDescription());
            tfSpecialRequestQuantity.setText(String.valueOf(poController.SpecialRequestList(pnSpecialRequest).getQuantity()));
            tfSpecialRequestAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.SpecialRequestList(pnSpecialRequest).getAmount(), false));
            JFXUtil.setCmbValue(cmbSpecialRequestChargeTo, !poController.SpecialRequestList(pnSpecialRequest).getChargeTo().equals("") ? Integer.valueOf(poController.SpecialRequestList(pnSpecialRequest).getChargeTo()) : -1);
            cbActiveSpecialRequest.setSelected(poController.SpecialRequestList(pnSpecialRequest).getRecordStatus());
            taSpecialRequestRemarks.setText(poController.SpecialRequestList(pnSpecialRequest).getRemarks());
            loadRecordPricing();
            loadRecordAdditionalInfo();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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
                    case "cmbReceivingMethod":
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
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(receivingmethod, cmbReceivingMethod), new JFXUtil.Pairs<>(PurchaseType, cmbModeOfPayment), new JFXUtil.Pairs<>(paymentTiming, cmbPaymentTiming), new JFXUtil.Pairs<>(chargeTo, cmbSpecialRequestChargeTo));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbReceivingMethod, cmbModeOfPayment, cmbPaymentTiming, cmbSpecialRequestChargeTo);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbReceivingMethod, cmbModeOfPayment, cmbPaymentTiming, cmbSpecialRequestChargeTo);
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
                int lnNumber = isUp ? JFXUtil.moveToPreviousRow(tblViewAddOns)
                        : JFXUtil.moveToNextRow(tblViewAddOns);
                lsTypeAddOns = addOns_data.get(lnNumber).getIndex08();
                int lnNum = Integer.parseInt(addOns_data.get(lnNumber).getIndex07());
                pnAddOns = isUp ? Integer.parseInt(addOns_data.get(getDetailFilter(addOns_data, lnNum, 7, lsTypeAddOns, 8, 2)).getIndex07())
                        : Integer.parseInt(addOns_data.get(getDetailFilter(addOns_data, lnNum, 7, lsTypeAddOns, 8, 2)).getIndex07());
            }
            loadRecordAddOns();

            //add identifier by getting its identifying index
            switch (addOns_data.get(getDetailFilter(addOns_data, pnAddOns, 7, lsTypeAddOns, 8, 2)).getIndex08()) {
                case "AddOn":
                    if (pnAddOns < 0 || pnAddOns > poController.getAddOnListCount() - 1) {
                        return;
                    }
                    JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                        {poController.AddOnList(pnAddOns).getAddOnId(), tfAddOns},
                        {poController.AddOnList(pnAddOns).AddOnMaster().getAmount(), tfAmount},
                        {poController.AddOnList(pnAddOns).getQuantity(), tfQuantity},}, tfQuantity); // default
                    break;
                case "Freebies":
                    if (pnAddOns < 0 || pnAddOns > poController.getFreebiesListCount() - 1) {
                        return;
                    }
                    JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                        {poController.FreebiesList(pnAddOns).Freebie().getDescription(), tfAddOns},
                        {poController.FreebiesList(pnAddOns).getAmount(), tfAmount},
                        {poController.FreebiesList(pnAddOns).getQuantity(), tfQuantity},}, tfQuantity); // default
                    break;
                case "Inclusion":
                    if (pnAddOns < 0 || pnAddOns > poController.getInclusionListCount() - 1) {
                        return;
                    }
                    JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                        {poController.InclusionList(pnAddOns).Inclusion().getDescription(), tfAddOns},
                        {poController.InclusionList(pnAddOns).getAmount(), tfAmount},
                        {poController.InclusionList(pnAddOns).getQuantity(), tfQuantity},}, tfQuantity); // default
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void moveNextFreebies(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apFreebies.requestFocus();
                //must get as well the lstype even in here for proper movenext
                int lnNumber = isUp ? JFXUtil.moveToPreviousRow(tblViewFreebies)
                        : JFXUtil.moveToNextRow(tblViewFreebies);
                lsTypeFreebies = freebies_data.get(lnNumber).getIndex05();
                int lnNum = Integer.parseInt(freebies_data.get(lnNumber).getIndex06());
                pnFreebies = isUp ? Integer.parseInt(freebies_data.get(getDetailFilter(freebies_data, lnNum, 6, lsTypeFreebies, 5, 1)).getIndex06())
                        : Integer.parseInt(freebies_data.get(getDetailFilter(freebies_data, lnNum, 6, lsTypeFreebies, 5, 1)).getIndex06());
            }
            loadRecordFreebies();
            //add identifier by getting its identifying index
            switch (freebies_data.get(getDetailFilter(freebies_data, pnFreebies, 6, lsTypeFreebies, 5, 1)).getIndex05()) {
                case "AddOn":
                    if (pnFreebies < 0 || pnFreebies > poController.getAddOnListCount() - 1) {
                        return;
                    }
                    JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                        {poController.AddOnList(pnFreebies).AddOnMaster().AddOnType().getAddTypeName(), tfDescription},}, tfQuantityFreebies); // default
                    break;
                case "Freebies":
                    if (pnFreebies < 0 || pnFreebies > poController.getFreebiesListCount() - 1) {
                        return;
                    }
                    JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                        {poController.FreebiesList(pnFreebies).Freebie().getDescription(), tfDescription},}, tfQuantityFreebies); // default
                    break;
                case "Inclusion":
                    if (pnFreebies < 0 || pnFreebies > poController.getInclusionListCount() - 1) {
                        return;
                    }
                    JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                        {poController.InclusionList(pnFreebies).Inclusion().getDescription(), tfDescription},}, tfQuantityFreebies); // default
                    break;
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void moveNextSpecialRequests(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apSpecialRequests.requestFocus();
            pnSpecialRequest = isUp ? Integer.parseInt(specialrequests_data.get(JFXUtil.moveToPreviousRow(tblViewSpecialRequests)).getIndex07())
                    : Integer.parseInt(specialrequests_data.get(JFXUtil.moveToNextRow(tblViewSpecialRequests)).getIndex07());
        }
        loadRecordSpecialRequest();
        if (pnSpecialRequest < 0 || pnSpecialRequest > poController.getSpecialRequestListCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.SpecialRequestList(pnSpecialRequest).getRequestDescription(), tfSpecialRequestDesc},}, tfSpecialRequestAmount); // default
    }

    public boolean isContainsChecked() {
        return checkedItem != null && checkedItem.contains("1");
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, apMaster);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.inputDecimalOnly(tfRate);
        JFXUtil.setCommaFormatter(tfAmount, tfReservationAmount, tfTotalAmount, tfAmountToFinance, tfPromissoryNoteAmount);
        CustomCommonUtil.inputIntegersOnly(tfQuantity, tfQuantityFreebies);

        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewAddOns, tblViewFreebies, tblViewSpecialRequests, tblAttachments);

        JFXUtil.adjustColumnForScrollbar(tblViewAddOns, tblViewFreebies, tblViewSpecialRequests, tblAttachments);
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
                        pbEnteredAddOns = false;
                        addOns_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadAddOnList();
                                poController.ReloadFreebiesList();
                                poController.ReloadInclusionList();
                            }
                            int lnCount = 0;
                            int lnCountCheck = 0;
                            for (int lnCtr = 0; lnCtr < poController.getAddOnListCount(); lnCtr++) {
                                if ((poController.AddOnList(lnCtr).getAddOnId() == null || "".equals(poController.AddOnList(lnCtr).getAddOnId()))
                                        || (poController.AddOnList(lnCtr).getTotalAmount() <= 0.00)
                                        || !poController.AddOnList(lnCtr).getRecordStatus()) {
                                    continue;
                                }
                                lnCount += 1;
                                checkedItems(lnCountCheck);
                                if (poController.AddOnList(lnCtr).getRecordStatus()) { // if true = Active // only this should activate during editMode of the row
                                    checkedItem.set(lnCountCheck, "1");
                                } else {
                                    checkedItem.set(lnCountCheck, "0");
                                }

                                String lsDescription = poController.AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName();

                                addOns_data.add(
                                        new ModelVehicleQuotation_AddOns(checkedItem.get(lnCountCheck),
                                                String.valueOf(lnCount),
                                                getData(lsDescription),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getQuantity(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnCtr).getTotalAmount(), false)),
                                                String.valueOf(lnCtr), "AddOn"
                                        ));
                                lnCountCheck += 1;
                            }
                            for (int lnCtr = 0; lnCtr < poController.getFreebiesListCount(); lnCtr++) {
                                if ((poController.FreebiesList(lnCtr).getFreebieId() == null || "".equals(poController.FreebiesList(lnCtr).getFreebieId())) //Do not reload extra row for freebie
                                        || (poController.FreebiesList(lnCtr).getTotalAmount() <= 0.00) || !poController.FreebiesList(lnCtr).getRecordStatus()) {
                                    if (poController.FreebiesList(lnCtr).getIsFree()) {
                                        continue;
                                    }
                                }
                                lnCount += 1;
                                checkedItems(lnCountCheck);
                                if (poController.FreebiesList(lnCtr).getRecordStatus()) { // if true = Active // only this should activate during editMode of the row
                                    checkedItem.set(lnCountCheck, "1");
                                } else {
                                    checkedItem.set(lnCountCheck, "0");
                                }
                                String lsDescription = poController.FreebiesList(lnCtr).Freebie().getDescription();
                                addOns_data.add(
                                        new ModelVehicleQuotation_AddOns(checkedItem.get(lnCountCheck),
                                                String.valueOf(lnCount),
                                                getData(lsDescription),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.FreebiesList(lnCtr).getAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.FreebiesList(lnCtr).getQuantity(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.FreebiesList(lnCtr).getTotalAmount(), false)),
                                                String.valueOf(lnCtr), "Freebies"
                                        ));
                                lnCountCheck += 1;
                            }

                            for (int lnCtr = 0; lnCtr < poController.getInclusionListCount(); lnCtr++) {
                                if ((poController.InclusionList(lnCtr).getInclusionId() == null || "".equals(poController.InclusionList(lnCtr).getInclusionId())) //Do not reload extra row for inclusion
                                        || (poController.InclusionList(lnCtr).getTotalAmount() <= 0.00) | !poController.InclusionList(lnCtr).getRecordStatus()) {
                                    if (poController.InclusionList(lnCtr).getIsFree()) {
                                        continue;
                                    }
                                }
                                lnCount += 1;
                                checkedItems(lnCountCheck);
                                if (poController.InclusionList(lnCtr).getRecordStatus()) { // if true = Active // only this should activate during editMode of the row
                                    checkedItem.set(lnCountCheck, "1");
                                } else {
                                    checkedItem.set(lnCountCheck, "0");
                                }
                                String lsDescription = poController.InclusionList(lnCtr).Inclusion().getDescription();
                                addOns_data.add(
                                        new ModelVehicleQuotation_AddOns(checkedItem.get(lnCountCheck),
                                                String.valueOf(lnCount),
                                                getData(lsDescription),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.InclusionList(lnCtr).getAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.InclusionList(lnCtr).getQuantity(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.InclusionList(lnCtr).getTotalAmount(), false)),
                                                String.valueOf(lnCtr), "Inclusion"
                                        ));
                                lnCountCheck += 1;
                            }
                            //Add Empty Row at LAST
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                int lnEmptyRow = poController.getAddOnListCount() - 1;
                                lnCount += 1;
                                checkedItems(lnCountCheck);
                                String lsDescription = poController.AddOnList(lnEmptyRow).AddOnMaster().AddOnType().getAddTypeName();
                                addOns_data.add(
                                        new ModelVehicleQuotation_AddOns(checkedItem.get(lnCountCheck),
                                                String.valueOf(lnCount),
                                                getData(lsDescription),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnEmptyRow).getAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnEmptyRow).getQuantity(), false)), String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.AddOnList(lnEmptyRow).getTotalAmount(), false)),
                                                String.valueOf(lnEmptyRow), "AddOn"
                                        ));
                            }

                            if (pnAddOns < 0 || pnAddOns
                                    >= addOns_data.size()) {
                                if (!addOns_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewAddOns, getDetailFilter(addOns_data, pnAddOns, 7, lsTypeAddOns, 8, 2));
                                    loadRecordAddOns();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewAddOns, getDetailFilter(addOns_data, pnAddOns, 7, lsTypeAddOns, 8, 2)); //focus
                                int lnRow = Integer.parseInt(addOns_data.get(getDetailFilter(addOns_data, pnAddOns, 7, lsTypeAddOns, 8, 2)).getIndex07());
                                pnAddOns = lnRow;
                                loadRecordAddOns();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableFreebies = new JFXUtil.ReloadableTableTask(
                tblViewFreebies,
                freebies_data,
                () -> {
                    pbEnteredFreebies = false;
                    Platform.runLater(() -> {
                        pbEnteredFreebies = false;
                        freebies_data.clear();
                        try {
                            int lnCount = 0;
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadAddOnList();
                                poController.ReloadFreebiesList();
                                poController.ReloadInclusionList();
                            }
                            for (int lnCtr = 0; lnCtr < poController.getAddOnListCount(); lnCtr++) {
                                if ((poController.AddOnList(lnCtr).getAddOnId() == null || "".equals(poController.AddOnList(lnCtr).getAddOnId())) //Do not reload empty add on Id in freebies table
                                        || (poController.AddOnList(lnCtr).getTotalAmount() > 0.00)
                                        || !poController.AddOnList(lnCtr).getRecordStatus()) {
                                    continue;
                                }
                                lnCount += 1;
                                freebies_data.add(
                                        new ModelVehicleQuotation_Freebies(String.valueOf(lnCount),
                                                getData(poController.AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName()),
                                                String.valueOf(poController.AddOnList(lnCtr).getQuantity()),
                                                "",
                                                "AddOn",
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            for (int lnCtr = 0; lnCtr < poController.getFreebiesListCount(); lnCtr++) {
                                if ((poController.FreebiesList(lnCtr).getFreebieId() == null || "".equals(poController.FreebiesList(lnCtr).getFreebieId()))
                                        || (poController.FreebiesList(lnCtr).getTotalAmount() > 0.00)
                                        || (!poController.FreebiesList(lnCtr).getIsFree())
                                        || !poController.FreebiesList(lnCtr).getRecordStatus()) {
                                    continue;
                                }
                                lnCount += 1;
                                freebies_data.add(
                                        new ModelVehicleQuotation_Freebies(String.valueOf(lnCount),
                                                getData(poController.FreebiesList(lnCtr).Freebie().getDescription()),
                                                String.valueOf(poController.FreebiesList(lnCtr).getQuantity()),
                                                "",
                                                "Freebies",
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            for (int lnCtr = 0; lnCtr < poController.getInclusionListCount(); lnCtr++) {
                                if ((poController.InclusionList(lnCtr).getInclusionId() == null || "".equals(poController.InclusionList(lnCtr).getInclusionId())) //Do not reload empty inclusion Id in freebies table
                                        || (poController.InclusionList(lnCtr).getTotalAmount() > 0.00)
                                        || (!poController.InclusionList(lnCtr).getIsFree())
                                        || !poController.InclusionList(lnCtr).getRecordStatus()) {
                                    continue;
                                }
                                lnCount += 1;
                                freebies_data.add(
                                        new ModelVehicleQuotation_Freebies(String.valueOf(lnCount),
                                                getData(poController.InclusionList(lnCtr).Inclusion().getDescription()),
                                                String.valueOf(poController.InclusionList(lnCtr).getQuantity()),
                                                "",
                                                "Inclusion",
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            //Add Empty Row at LAST
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                int lnEmptyRow = poController.getFreebiesListCount() - 1;
                                lnCount += 1;
                                freebies_data.add(
                                        new ModelVehicleQuotation_Freebies(String.valueOf(lnCount),
                                                getData(poController.FreebiesList(lnEmptyRow).Freebie().getDescription()),
                                                String.valueOf(poController.FreebiesList(lnEmptyRow).getQuantity()),
                                                "",
                                                "Freebies",
                                                String.valueOf(lnEmptyRow)
                                        ));
                            }

                            if (pnFreebies < 0 || pnFreebies
                                    >= freebies_data.size()) {
                                if (!freebies_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewFreebies, getDetailFilter(freebies_data, pnFreebies, 6, lsTypeFreebies, 5, 1)); //focus
                                    loadRecordFreebies();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewFreebies, getDetailFilter(freebies_data, pnFreebies, 6, lsTypeFreebies, 5, 1)); //focus
                                int lnRow = Integer.parseInt(freebies_data.get(getDetailFilter(freebies_data, pnFreebies, 6, lsTypeFreebies, 5, 1)).getIndex06());
                                pnFreebies = lnRow;
                                loadRecordFreebies();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableAttachment = new JFXUtil.ReloadableTableTask(
                tblAttachments,
                attachment_data,
                () -> {
                    imageviewerutil.scaleFactor = 1.0;
                    JFXUtil.resetImageBounds(imageView, stackPane1);
                    Platform.runLater(() -> {
                        try {
                            attachment_data.clear();
                            int lnCtr;
                            int lnCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getTransactionAttachmentCount(); lnCtr++) {
                                if (RecordStatus.INACTIVE.equals(poController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    continue;
                                }
                                lnCount += 1;
                                attachment_data.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                                String.valueOf(poController.TransactionAttachmentList(lnCtr).getModel().getFileName()),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(attachment_data, pnAttachment, 3); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= attachment_data.size()) {
                                if (!attachment_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblAttachments, 0);
                                    int lnRow = Integer.parseInt(attachment_data.get(0).getIndex03());
                                    pnAttachment = lnRow;
                                    loadRecordAttachment(true);
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblAttachments, lnTempRow);
                                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                                pnAttachment = lnRow;
                                loadRecordAttachment(true);
                            }
                            if (attachment_data.size() <= 0) {
                                loadRecordAttachment(false);
                            }
                        } catch (Exception e) {
                        }
                    });
                }
        );
        loadTableSpecialRequests = new JFXUtil.ReloadableTableTask(
                tblViewSpecialRequests,
                specialrequests_data,
                () -> {
                    pbEnteredSpecialRequests = false;
                    Platform.runLater(() -> {
                        pbEnteredSpecialRequests = false;
                        specialrequests_data.clear();
                        try {
                            int lnCount = 0;
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadSpecialRequestList();
                            }
                            for (int lnCtr = 0; lnCtr < poController.getSpecialRequestListCount(); lnCtr++) {
                                if (!poController.SpecialRequestList(lnCtr).getRecordStatus()) {
                                    continue;
                                }
                                lnCount += 1;
                                String lsChargeTo = getChargeTo(Integer.parseInt(poController.SpecialRequestList(lnCtr).getChargeTo()));
                                specialrequests_data.add(
                                        new ModelVehicleQuotation_SpecialRequests(String.valueOf(lnCount),
                                                getData(poController.SpecialRequestList(lnCtr).getRequestDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.SpecialRequestList(lnCtr).getAmount(), false)),
                                                String.valueOf(poController.SpecialRequestList(lnCtr).getQuantity()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.SpecialRequestList(lnCtr).getTotalAmount(), false)),
                                                String.valueOf(lsChargeTo),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRowFilter(specialrequests_data, pnSpecialRequest, 7); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= specialrequests_data.size()) {
                                if (!specialrequests_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewSpecialRequests, 0);
                                    int lnRow = Integer.parseInt(specialrequests_data.get(0).getIndex07());
                                    pnSpecialRequest = lnRow;
                                    loadRecordSpecialRequest();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewSpecialRequests, lnTempRow);
                                int lnRow = Integer.parseInt(specialrequests_data.get(tblViewSpecialRequests.getSelectionModel().getSelectedIndex()).getIndex07());
                                pnSpecialRequest = lnRow;
                                loadRecordSpecialRequest();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Attachment":
                    if (pnEditMode == EditMode.READY) {
                        JFXUtil.clearTextFields(apAttachments);
                        try {
                            poController.loadAttachments();
                        } catch (GuanzonException | SQLException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                        loadTableAttachment.reload();
                    }
                    break;
            }
        });
    }

    private void initAttachmentPreviewPane() {
        imageviewerutil.initAttachmentPreviewPane(stackPane1, imageView);
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            imageviewerutil.ldstackPaneHeight = computedHeight;
            loadTableAttachment.reload();
            loadRecordAttachment(true);
        });
    }

    public void initAttachmentsGrid() {
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);

        tblAttachments.setItems(attachment_data);
    }

    public void slideImage(int direction) {
        if (attachment_data.size() <= 0) {
            return;
        }
        int lnRow = Integer.valueOf(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
        currentIndex = lnRow - 1;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
            slideOut.setByX(direction * -400); // Move left or right

            JFXUtil.selectAndFocusRow(tblAttachments, newIndex);
            int lnIndex = Integer.valueOf(attachment_data.get(newIndex).getIndex01());
            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data, lnIndex, 3);
            pnAttachment = lnTempRow;
            loadRecordAttachment(false);

            // Create a transition animation
            slideOut.setOnFinished(event -> {
                imageView.setTranslateX(direction * 400);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), imageView);
                slideIn.setToX(0);
                slideIn.play();

                loadRecordAttachment(true);
            });

            slideOut.play();
        }
        if (JFXUtil.isImageViewOutOfBounds(imageView, stackPane1)) {
            JFXUtil.resetImageBounds(imageView, stackPane1);
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poController.TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                }
                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";
                        // in server
                        if (poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                            filePath2 = poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + filePath;
                        } else {
                            filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + filePath;
                        }

                        if (filePath != null && !filePath.isEmpty()) {
                            Path imgPath = Paths.get(filePath2);
                            String convertedPath = imgPath.toUri().toString();
                            boolean isPdf = filePath.toLowerCase().endsWith(".pdf");

                            // Clear previous content
                            stackPane1.getChildren().clear();
                            if (!isPdf) {
                                // ----- IMAGE VIEW -----
                                Image loimage = new Image(convertedPath);
                                imageView.setImage(loimage);
                                JFXUtil.adjustImageSize(loimage, imageView, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);

                                PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
                                delay.setOnFinished(event -> {
                                    Platform.runLater(() -> {
                                        JFXUtil.stackPaneClip(stackPane1);
                                    });
                                });
                                delay.play();

                                // Add ImageView directly to stackPane
                                stackPane1.getChildren().add(imageView);
                                stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);

                                // Align buttons on top
                                StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);

                                // Optional: add some margin
                                StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));

                            } else {
                                // ----- PDF VIEW -----
                                JFXUtil.PDFViewConfig(filePath2, stackPane1, btnArrowLeft, btnArrowRight, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);
                            }
                        } else {
                            imageView.setImage(null);
                        }

                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                }
            } else {
                if (!lbloadImage) {
                    imageView.setImage(null);
                    // Clear previous content
                    stackPane1.getChildren().clear();
                    // Add ImageView directly to stackPane
                    stackPane1.getChildren().add(imageView);
                    stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);
                    Platform.runLater(() -> JFXUtil.stackPaneClip(stackPane1));
                    pnAttachment = 0;
                }
            }
        } catch (Exception ex) {
        }
    }

    public String getChargeTo(int index) {
        if (index >= 0 && index < chargeTo.size()) {
            return chargeTo.get(index);
        }
        return null; // or "None" / throw an exception
    }

    public static int getDetailFilter(ObservableList<?> dataList,
            int lnpn, int originalIndex,
            String lsIdentifierType, int IdentifierIndex,
            int indexReturnValue) {

        String compareGetter = String.format("getIndex%02d", originalIndex);
        String secondGetter = String.format("getIndex%02d", IdentifierIndex);
        String returnGetter = String.format("getIndex%02d", indexReturnValue);

        try {
            for (Object item : dataList) {
                Class<?> clazz = item.getClass();

                // First basis
                Object compareValue = clazz.getMethod(compareGetter).invoke(item);
                if (!String.valueOf(lnpn).equals(String.valueOf(compareValue))) {
                    continue;
                }

                // Second basis
                Object secondCompareValue = clazz.getMethod(secondGetter).invoke(item);
                String actualSecond = secondCompareValue == null ? "" : String.valueOf(secondCompareValue).trim();
                String expectedSecond = lsIdentifierType == null ? "" : lsIdentifierType.trim();

                if (!expectedSecond.equals(actualSecond)) {
                    continue;
                }

                // Both matched -> return the value
                Object result = clazz.getMethod(returnGetter).invoke(item);
                if (result == null) {
                    return 0;
                }

                int value = Integer.parseInt(String.valueOf(result).trim());
                return value > 0 ? value - 1 : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0; // default: not found, null, or not a number
    }

    private void initAddOnsGrid() {
        JFXUtil.setColumnCenter(tblNoAddOns);
        JFXUtil.setColumnLeft(tblDescriptionAddOns);
        JFXUtil.setColumnRight(tblAmount, tblQuantityAddOns, tblTotal);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewAddOns);
        tblViewAddOns.setItems(addOns_data);
    }

    private void initFreebiesGrid() {
        JFXUtil.setColumnCenter(tblNoFreebies);
        JFXUtil.setColumnLeft(tblDescriptionFreebies);
        JFXUtil.setColumnRight(tblQuantityFreebies);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewFreebies);
        tblViewFreebies.setItems(freebies_data);
    }

    private void initSpecialRequestGrid() {
        JFXUtil.setColumnCenter(tblSpecialRequestNo);
        JFXUtil.setColumnLeft(tblSpecialRequestDescription, tblSpecialRequestChargeTo);
        JFXUtil.setColumnRight(tblSpecialRequestAmount, tblSpecialRequestQuantity, tblSpecialRequestTotalAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewSpecialRequests);
        tblViewSpecialRequests.setItems(specialrequests_data);
    }

    public void clearTextFields() {
        resetCheckboxSelection();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apRemarks, apMaster, apVehicleInfo, apAddOns, apPricing, apAdditionalInfo, apFreebies, apAttachments);
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
            lblSource.setText(poController.Company().getCompanyName());
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
                        lsTypeAddOns = addOns_data.get(tblViewAddOns.getSelectionModel().getSelectedIndex()).getIndex08();
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
                        lsTypeFreebies = freebies_data.get(tblViewFreebies.getSelectionModel().getSelectedIndex()).getIndex05();
                        pnFreebies = lnRow;
                        loadRecordFreebies();
                        moveNextFreebies(false, false);
                    }
                }
            }
        });
        tblViewSpecialRequests.setOnMouseClicked(event -> {
            if (specialrequests_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleQuotation_SpecialRequests selected = (ModelVehicleQuotation_SpecialRequests) tblViewSpecialRequests.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(specialrequests_data.get(tblViewSpecialRequests.getSelectionModel().getSelectedIndex()).getIndex07());
                        pnSpecialRequest = lnRow;
                        loadRecordSpecialRequest();
                        moveNextSpecialRequests(false, false);
                    }
                }
            }
        });
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                imageviewerutil.scaleFactor = 1.0;
                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                pnAttachment = lnRow;
                loadRecordAttachment(true);
                JFXUtil.resetImageBounds(imageView, stackPane1);
            }
        });
    }

    private void initButton(int fnValue) {
        boolean lbShow2 = fnValue == EditMode.READY;
        JFXUtil.setButtonsVisibility(lbShow2, btnHistory, btnPrint, btnDeduct, btnRemake);

        JFXUtil.setDisabled(true, apAttachmentButtons,apMaster, apAddOns, apPricing, apAdditionalInfo, apFreebies, apRemarks, apSpecialRequests, apAttachmentButtons);
        disableRowCheckbox.set(true);

        if (fnValue != EditMode.READY) {
            return;
        }
        try {
            boolean lbIsHide1 = poController.Detail(0).getValidThruDate().after(oApp.getServerDate())
                    && VehicleQuotationStatus.APPROVED.equals(poController.Master().getTransactionStatus());
            boolean lbIsHide2 = VehicleQuotationStatus.OPEN.equals(poController.Master().getTransactionStatus())
                    && poController.Detail(0).getValidThruDate().after(oApp.getServerDate());
            if (lbIsHide1 || lbIsHide2) {
                JFXUtil.setButtonsVisibility(false, btnRemake);
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        JFXUtil.setButtonsVisibility(poController.getTotalDiscount().doubleValue() > 0.00, btnDeduct);
        switch (poController.Master().getTransactionStatus()) {
            case VehicleQuotationStatus.OPEN:
            case VehicleQuotationStatus.VOID:
            case VehicleQuotationStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnPrint);
                break;
        }
    }
}
