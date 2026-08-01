/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelBankApplications_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelFollowUp_Detail;
import ph.com.guanzongroup.integsys.model.ModelRequirements_Detail;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.BankApplicationStatus;

/**
 *
 * @author Arsiela
 */
public class SalesInquiry_HistoryCarController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0, pnRequirements = 0, pnBankApplications = 0;
    boolean lsIsSaved = false;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesControllers poSalesInquiryController;
    public int pnEditMode;
    boolean pbKeyPressed = false;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchClientId = "";
    private int pnAttachment = 0;
    private int currentIndex = 0;
    private final Map<String, String> imageinfo_temp = new HashMap<>();
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    ObservableList<String> ClientType = ModelSalesInquiry_Detail.ClientType;
    ObservableList<String> PurchaseType = ModelSalesInquiry_Detail.PurchaseType;
    ObservableList<String> CategoryType = ModelSalesInquiry_Detail.CategoryType;
    ObservableList<String> CustomerGroup = ModelSalesInquiry_Detail.CustomerGroup;
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;

    private ObservableList<ModelSalesInquiry_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelSalesInquiry_Detail> filteredDataDetail;
    private ObservableList<ModelRequirements_Detail> requirements_data = FXCollections.observableArrayList();
    private ObservableList<ModelBankApplications_Detail> bankapplications_data = FXCollections.observableArrayList();
    private ObservableList<ModelFollowUp_Detail> followup_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain, loadTableRequirements, loadTableBankApplications, loadTableFollowUp, loadTableAttachment;
    private boolean pbEntered = false;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apInquiry, apFields, apMaster, apDetail, apTableDetail, apRequirements, apAttachments;
    @FXML
    private TextField tfSearchClient, tfSearchReferenceNo, tfTransactionNo, tfBranch, tfSalesPerson, tfReferralAgent, tfInquiryType, tfClient, tfAddress, tfContactNo, tfBrand, tfModel, tfColor, tfModelVariant, tfSellingPrice, tfRequirement, tfReceivedBy, tfAttachmentNo, tfAttachmentSource;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnHistory, btnClose, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabpane;
    @FXML
    private DatePicker dpTransactionDate, dpTargetDate, dpReceivedDate;
    @FXML
    private ComboBox cmbPurchaseType, cmbCategoryType, cmbClientType, cmbCustomerGroup, cmbAttachmentType;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetails, tblViewRequirements, tblViewBankApplications, tblViewFollowUpHistory, tblAttachments;
    @FXML
    private TableColumn tblRowNoDetail, tblBrandDetail, tblDescriptionDetail, tblRequirementRowNo, tblRequired, tblSubmitted, tblRequirements, tblReceivedBy, tblReceivedDate, tblBankAppRowNo, tblBankAppNo, tblBank, tblAppliedDate, tblApprovedDate, tblStatus, tblFollowUpRowNo, tblFollowUpTransNo, tblFollowUpTransDate, tblFollowUpMessage, tblFollowUpRemarks, tblFollowUpDate, tblFollowUpTime, tblFollowUpMethod, tblFollowUpSocMed, tblFollowUpResponse, tblFollowUpGoodsCompetitor, tblFollowUpMakeCompetitor, tblFollowUpDealerCompetitor, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private Tab tabAttachments;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

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
        initRequirementsGrid();
        initBankApplicationsGrid();
        initFollowUpGrid();
        initAttachmentsGrid();
        initTableOnClick();
        initAttachmentPreviewPane();
        clearTextFields();
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);

        Platform.runLater(() -> {
            poSalesInquiryController.SalesInquiry().Master().setIndustryId(psIndustryId);
            poSalesInquiryController.SalesInquiry().Master().setCompanyId(psCompanyId);
            poSalesInquiryController.SalesInquiry().Master().setCategoryCode(psCategoryId);
            poSalesInquiryController.SalesInquiry().setIndustryId(psIndustryId);
            poSalesInquiryController.SalesInquiry().setCompanyId(psCompanyId);
            poSalesInquiryController.SalesInquiry().setCategoryId(psCategoryId);
            loadRecordSearch();
        });

        initTabPane();
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

        Object source = event.getSource();
        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poJSON = poSalesInquiryController.SalesInquiry().searchTransaction(psIndustryId, psCompanyId, psCategoryId, tfSearchClient.getText(), tfSearchReferenceNo.getText());
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        poSalesInquiryController.SalesInquiry().loadRequirements();
                        poSalesInquiryController.SalesInquiry().loadBankApplications();
                        pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();

                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnHistory":
                        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                            ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                            return;
                        }

                        try {
                            poSalesInquiryController.SalesInquiry().ShowStatusHistory();
                            return;
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnArrowRight":
                        slideImage(1);
                        break;
                    case "btnArrowLeft":
                        slideImage(-1);
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisApprove", "btnCancelApplication")) {
                    loadTableBankApplications.reload();
                    return;
                }
                String currentTitle = tabpane.getSelectionModel().getSelectedItem().getText();
                switch (currentTitle) {
                    case "Requirements":
                        JFXUtil.clickTabByTitleText(tabpane, "Requirements");
                        break;
                    case "Bank Applications":
                        JFXUtil.clickTabByTitleText(tabpane, "Bank Applications");
                        break;
                    case "Followup History":
                        JFXUtil.clickTabByTitleText(tabpane, "Followup History");
                        break;
                    case "Attachments":
                        JFXUtil.clickTabByTitleText(tabpane, "Attachments");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel")) {
                    JFXUtil.clickTabByTitleText(tabpane, "Inquiry");
                }

                if (lsButton.equals("btnPrint")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                    loadTableFollowUp.reload();
                    loadTableAttachment.reload();
                }

                initButton(pnEditMode);
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    public void initTabPane() {
        tabpane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                String tabTitle = newTab.getText();
                switch (tabTitle) {
                    case "Inquiry":
                        break;
                    case "Requirements":
                        JFXUtil.clearTextFields(apRequirements);
                        loadTableRequirements.reload();
                        break;
                    case "Bank Applications":
//                        JFXUtil.clearTextFields(apBankApplications);
                        loadTableBankApplications.reload();
                        break;
                    case "Followup History":
                        loadTableFollowUp.reload();
                        break;
                    case "Attachments":
                        JFXUtil.clearTextFields(apAttachments);
                        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                            try {
                                poSalesInquiryController.SalesInquiry().loadAttachments();
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        loadTableAttachment.reload();
                        break;
                }
            }
        });
    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.ADDNEW;
        JFXUtil.setDisabled(!lbDisable, tfClient, cmbClientType, cmbCategoryType);
        try {
            Platform.runLater(() -> {
                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().Master().getTransactionStatus();
                lblStatus.setText(poSalesInquiryController.SalesInquiry().getInquiryStatus(lsActive).toUpperCase());
            });

            // Transaction Date
            tfTransactionNo.setText(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            String lsTargetDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().Master().getTargetDate());
            dpTargetDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTargetDate, "yyyy-MM-dd"));

            tfBranch.setText(poSalesInquiryController.SalesInquiry().Master().Branch().getBranchName());
            tfSalesPerson.setText(poSalesInquiryController.SalesInquiry().Master().SalesPerson().getFullName());
            tfReferralAgent.setText(poSalesInquiryController.SalesInquiry().Master().ReferralAgent().getCompanyName());

            tfClient.setText(poSalesInquiryController.SalesInquiry().Master().Client().getCompanyName());
            tfAddress.setText(poSalesInquiryController.SalesInquiry().Master().ClientAddress().getAddress());
            tfContactNo.setText(poSalesInquiryController.SalesInquiry().Master().ClientMobile().getMobileNo());
            tfInquiryType.setText(poSalesInquiryController.SalesInquiry().Master().Source().getDescription());

            taRemarks.setText(poSalesInquiryController.SalesInquiry().Master().getRemarks());

            if (pnEditMode != EditMode.UNKNOWN) {
                cmbPurchaseType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getPurchaseType()));
                if (poSalesInquiryController.SalesInquiry().Master().getClientId() != null && !"".equals(poSalesInquiryController.SalesInquiry().Master().getClientId())) {
                    cmbClientType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().Client().getClientType()));
                } else {
                    cmbClientType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getClientType()));
                }
                if (poSalesInquiryController.SalesInquiry().Master().getCategoryType() != null && !"".equals(poSalesInquiryController.SalesInquiry().Master().getCategoryType())) {
                    cmbCategoryType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getCategoryType()));
                }
            } else {
                cmbPurchaseType.getSelectionModel().select(0);
                cmbClientType.getSelectionModel().select(0);
                cmbCategoryType.getSelectionModel().select(0);
            }

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poSalesInquiryController.SalesInquiry().getDetailCount() - 1) {
                return;
            }
            tfBrand.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Brand().getDescription());
            tfModel.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Model().getDescription());
            tfModelVariant.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).ModelVariant().getDescription());
            tfColor.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Color().getDescription());
            tfSellingPrice.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSalesInquiryController.SalesInquiry().Detail(pnDetail).getSellPrice(), true));
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordRequirements() {
        try {
            int lnCustomerGroup = 0;
            if (pnEditMode != EditMode.UNKNOWN && poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() > 0) {
                if (!JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(0).getCustomerGroup(), null, "")) {
                    lnCustomerGroup = Integer.parseInt(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(0).getCustomerGroup());
                } else {
                    if (poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() > 0) {
                        poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(0).setCustomerGroup(String.valueOf(0));
                    }
                }
            }

            cmbCustomerGroup.getSelectionModel().select(lnCustomerGroup);
            if (pnRequirements < 0 || pnRequirements > poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount() - 1) { // intended to place here
                return;
            }
            tfRequirement.setText(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).RequirementSource().getDescription());
            tfReceivedBy.setText(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).SalesPerson().getFullName());
            String lsdpReceivedDate = JFXUtil.formatDateToString(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).getReceivedDate());
            dpReceivedDate.setValue(!lsdpReceivedDate.equals("") ? CustomCommonUtil.parseDateStringToLocalDate(lsdpReceivedDate, "yyyy-MM-dd") : null);

            boolean lbShow = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(pnRequirements).SalesPerson().getFullName(), null, "");
            JFXUtil.setDisabled(lbShow, dpReceivedDate);
            JFXUtil.updateCaretPositions(apRequirements);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewTransDetails":
                    if (!details_data.isEmpty()) {
                        pnDetail = isMovedDown ? JFXUtil.moveToNextRow(currentTable)
                                : JFXUtil.moveToPreviousRow(currentTable);
                        loadRecordDetail();
                    }
                    break;
                case "tblViewRequirements":
                    if (!requirements_data.isEmpty()) {
                        pnRequirements = isMovedDown ? JFXUtil.moveToNextRow(currentTable)
                                : JFXUtil.moveToPreviousRow(currentTable);
                        loadRecordRequirements();
                    }
                    break;
                case "tblViewBankApplications":
                    break;
                case "tblAttachments":
                    if (!attachment_data.isEmpty()) {
                        pnAttachment = isMovedDown ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03())
                                : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                        loadRecordAttachment(true);
                    }
                    break;
            }
        }
    };

    public void initTableOnClick() {
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
        tblViewTransDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                }
            }
        });
        tblViewRequirements.setOnMouseClicked(event -> {
            if (requirements_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnRequirements = tblViewRequirements.getSelectionModel().getSelectedIndex();
                    loadRecordRequirements();
                }
            }
        });
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewTransDetails, tblViewRequirements, tblViewBankApplications, tblViewFollowUpHistory, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails, tblViewRequirements, tblViewBankApplications, tblViewFollowUpHistory, tblAttachments);  // need to use computed-size in min-width of the column to work
        JFXUtil.addCheckboxColumns(ModelRequirements_Detail.class, tblViewRequirements, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                }, 1, 2);
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poSalesInquiryController.SalesInquiry().loadDetail();
                            }
                            poSalesInquiryController.SalesInquiry().sortPriority();
                            String lsBrand = "";
                            String lsModel = "";
                            String lsModelVariant = "";
                            String lsColor = "";
                            String lsDescription = "";
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getDetailCount(); lnCtr++) {
                                if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).getStockId() != null
                                        && !"".equals(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getStockId())) {
                                    lsBrand = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().Brand().getDescription();
                                    lsModel = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().Model().getDescription();
                                    lsModelVariant = " " + poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().Variant().getDescription();
                                    lsColor = " " + poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().Color().getDescription();
                                } else {
                                    if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).Brand().getDescription() != null) {
                                        lsBrand = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Brand().getDescription();
                                    }
                                    if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).Model().getDescription() != null) {
                                        lsModel = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Model().getDescription();
                                    }
                                    if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).ModelVariant().getDescription() != null) {
                                        lsModelVariant = " " + poSalesInquiryController.SalesInquiry().Detail(lnCtr).ModelVariant().getDescription();
                                    }
                                    if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).Color().getDescription() != null) {
                                        lsColor = " " + poSalesInquiryController.SalesInquiry().Detail(lnCtr).Color().getDescription();
                                    }
                                }
                                lsDescription = lsModel
                                        + lsModelVariant
                                        + lsColor;
                                details_data.add(
                                        new ModelSalesInquiry_Detail(
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getPriority()),
                                                lsBrand,
                                                lsDescription.trim().replaceAll("\\r?\\n", " ")
                                        ));
                                lsBrand = "";
                                lsModel = "";
                                lsModelVariant = "";
                                lsColor = "";
                            }

                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewTransDetails, 0);
                                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewTransDetails, 0);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableRequirements = new JFXUtil.ReloadableTableTask(
                tblViewRequirements,
                requirements_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        requirements_data.clear();
                        try {
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getSalesInquiryRequirementsCount(); lnCtr++) {
                                int lnIsRequired = poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).isRequired() ? 1 : 0;
                                int lnIsSubmitted = poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).isSubmitted() ? 1 : 0;

                                String lsReceivedDate = JFXUtil.formatDateToString(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).getReceivedDate());
                                requirements_data.add(
                                        new ModelRequirements_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(lnIsRequired),
                                                String.valueOf(lnIsSubmitted),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).RequirementSource().getDescription()),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().SalesInquiryRequimentsList(lnCtr).SalesPerson().getFullName()),
                                                String.valueOf(lsReceivedDate)
                                        ));
                            }
                            if (pnRequirements < 0 || pnRequirements
                                    >= requirements_data.size()) {
                                if (!requirements_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewRequirements, 0);
                                    pnRequirements = tblViewRequirements.getSelectionModel().getSelectedIndex();
                                }
                                loadRecordRequirements();
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewRequirements, pnRequirements);
                                loadRecordRequirements();
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableBankApplications = new JFXUtil.ReloadableTableTask(
                tblViewBankApplications,
                bankapplications_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        bankapplications_data.clear();
                        try {
                            if (pnEditMode != EditMode.UNKNOWN) {
                                poSalesInquiryController.SalesInquiry().loadBankApplicationList();
                            }
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getBankApplicationsCount(); lnCtr++) {
                                String lsAppliedDate = JFXUtil.formatDateToString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getAppliedDate());
                                String lsApprovedDate = JFXUtil.formatDateToString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getApprovedDate());

                                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getTransactionStatus();
                                Map<String, String> statusMap = new HashMap<>();
                                statusMap.put(BankApplicationStatus.OPEN, "OPEN");
                                statusMap.put(BankApplicationStatus.APPROVED, "APPROVED");
                                statusMap.put(BankApplicationStatus.DISAPPROVED, "DISAPPROVED");
                                statusMap.put(BankApplicationStatus.CANCELLED, "CANCELLED");
                                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN"); //default

                                String lsBank = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName(), null, "")
                                        ? "" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName();

                                bankapplications_data.add(
                                        new ModelBankApplications_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getPONumber()),
                                                String.valueOf(lsBank),
                                                String.valueOf(lsAppliedDate),
                                                String.valueOf(lsApprovedDate),
                                                String.valueOf(lsStat)
                                        )
                                );
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableFollowUp = new JFXUtil.ReloadableTableTask(
                tblViewFollowUpHistory,
                followup_data,
                () -> {
                    Platform.runLater(() -> {
                        Platform.runLater(() -> {
                            int lnCtr;
                            followup_data.clear();
                            try {
                                if (pnEditMode != EditMode.UNKNOWN) {
                                    poSalesInquiryController.SalesInquiry().loadBankApplicationList();
                                }
                                for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getBankApplicationsCount(); lnCtr++) {
                                    String lsAppliedDate = JFXUtil.formatDateToString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getAppliedDate());
                                    String lsApprovedDate = JFXUtil.formatDateToString(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getApprovedDate());

                                    String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getTransactionStatus();
                                    Map<String, String> statusMap = new HashMap<>();
                                    statusMap.put(BankApplicationStatus.OPEN, "OPEN");
                                    statusMap.put(BankApplicationStatus.APPROVED, "APPROVED");
                                    statusMap.put(BankApplicationStatus.DISAPPROVED, "DISAPPROVED");
                                    statusMap.put(BankApplicationStatus.CANCELLED, "CANCELLED");
                                    String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN"); //default

                                    String lsBank = JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName(), null, "")
                                            ? "" : poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).Bank().getBankName();

                                    followup_data.add(
                                            new ModelFollowUp_Detail(String.valueOf(lnCtr + 1),
                                                    String.valueOf(poSalesInquiryController.SalesInquiry().BankApplicationsList(lnCtr).getPONumber()),
                                                    String.valueOf(lsBank),
                                                    String.valueOf(lsAppliedDate),
                                                    String.valueOf(lsApprovedDate),
                                                    String.valueOf(lsStat)
                                            )
                                    );
                                }
                            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        });
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
                            int lnCount = 0;
                            for (int lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getTransactionAttachmentCount(); lnCtr++) {
                                if (RecordStatus.INACTIVE.equals(poSalesInquiryController.SalesInquiry().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    continue;
                                }
                                lnCount += 1;
                                attachment_data.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().TransactionAttachmentList(lnCtr).getModel().getFileName()),
                                                String.valueOf(lnCtr)
                                        )
                                );
                            }

                            int lnTempRow = JFXUtil.getDetailRow(attachment_data, pnAttachment, 3);
                            if (lnTempRow < 0 || lnTempRow >= attachment_data.size()) {
                                if (!attachment_data.isEmpty()) {
                                    JFXUtil.selectAndFocusRow(tblAttachments, 0);
                                    int lnRow = Integer.parseInt(attachment_data.get(0).getIndex03());
                                    pnAttachment = lnRow;
                                    loadRecordAttachment(true);
                                }
                            } else {
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
    }

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());

        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfSearchClient":
                    if (lsValue.equals("")) {
                        psSearchClientId = "";
                    }
                    break;
            }
            if (lsTxtFieldID.equals("tfSearchClient") || lsTxtFieldID.equals("tfSearchReferenceNo")) {
                loadRecordSearch();
            }
        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lsID) {
                        case "tfSearchClient":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchClient(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchClient.setText("");
                                psSearchClientId = "";
                                break;
                            }
                            psSearchClientId = poSalesInquiryController.SalesInquiry().Master().getClientId();
                            loadRecordSearch();
                            return;
                        case "tfSearchReferenceNo":
                            poJSON = poSalesInquiryController.SalesInquiry().searchTransaction(psIndustryId, psCompanyId, psCategoryId, tfSearchClient.getText(), tfSearchReferenceNo.getText());
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchReferenceNo.setText("");
                                break;
                            } else {
                                poSalesInquiryController.SalesInquiry().loadRequirements();
                                poSalesInquiryController.SalesInquiry().loadBankApplications();
                                pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();
                                loadRecordMaster();
                                loadTableDetail.reload();
                                initButton(pnEditMode);
                            }
                            String currentTitle = tabpane.getSelectionModel().getSelectedItem().getText();
                            switch (currentTitle) {
                                case "Requirements":
                                    JFXUtil.clickTabByTitleText(tabpane, "Requirements");
                                    break;
                                case "Bank Applications":
                                    JFXUtil.clickTabByTitleText(tabpane, "Bank Applications");
                                    break;
                            }
                            loadRecordSearch();
                            return;
                    }
                    break;
                default:
                    break;
            }

            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
            }
        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(ClientType, cmbClientType), new JFXUtil.Pairs<>(PurchaseType, cmbPurchaseType),
                new JFXUtil.Pairs<>(CategoryType, cmbCategoryType), new JFXUtil.Pairs<>(CustomerGroup, cmbCustomerGroup), new JFXUtil.Pairs<>(documentType, cmbAttachmentType));
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbClientType, cmbPurchaseType, cmbCategoryType, cmbCustomerGroup, cmbAttachmentType);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpTargetDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtField_Focus, tfSearchClient, tfSearchReferenceNo);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
    }

    private void initButton(int fnValue) {
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(lbShow2, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(true, taRemarks, apMaster, apDetail, apRequirements, cmbAttachmentType);//, apBankApplications, apBankApplicationsButtons);
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblBrandDetail, tblDescriptionDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);
        disableRowCheckbox.setValue(true);
        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        SortedList<ModelSalesInquiry_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewTransDetails.comparatorProperty());
        tblViewTransDetails.setItems(sortedData);
        tblViewTransDetails.autosize();
    }

    public void initRequirementsGrid() {
        JFXUtil.setColumnCenter(tblRequirementRowNo, tblReceivedDate);
        JFXUtil.setColumnLeft(tblRequired, tblSubmitted, tblRequirements, tblReceivedBy);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewRequirements);
        tblViewRequirements.setItems(requirements_data);
    }

    public void initBankApplicationsGrid() {
        JFXUtil.setColumnCenter(tblBankAppRowNo, tblBankAppNo, tblAppliedDate, tblApprovedDate, tblStatus);
        JFXUtil.setColumnLeft(tblBank);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewBankApplications);
        tblViewBankApplications.setItems(bankapplications_data);
    }

    public void initFollowUpGrid() {
        JFXUtil.setColumnCenter(tblFollowUpRowNo, tblFollowUpTransNo, tblFollowUpTransDate, tblFollowUpDate, tblFollowUpTime);
        JFXUtil.setColumnLeft(tblFollowUpMessage, tblFollowUpRemarks, tblFollowUpMethod, tblFollowUpSocMed, tblFollowUpResponse, tblFollowUpGoodsCompetitor, tblFollowUpMakeCompetitor, tblFollowUpDealerCompetitor);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewFollowUpHistory);
        tblViewFollowUpHistory.setItems(followup_data);
    }

    public void initAttachmentsGrid() {
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
        tblAttachments.setItems(attachment_data);
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

    public void slideImage(int direction) {
        if (attachment_data.size() <= 0) {
            return;
        }
        int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
        currentIndex = lnRow - 1;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
            slideOut.setByX(direction * -400);

            JFXUtil.selectAndFocusRow(tblAttachments, newIndex);
            int lnIndex = Integer.parseInt(attachment_data.get(newIndex).getIndex01());
            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data, lnIndex, 3);
            pnAttachment = lnTempRow;
            loadRecordAttachment(false);

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
                String lsAttachmentType = poSalesInquiryController.SalesInquiry().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poSalesInquiryController.SalesInquiry().TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poSalesInquiryController.SalesInquiry().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                }
                int lnAttachmentType = Integer.parseInt(lsAttachmentType);
                cmbAttachmentType.getSelectionModel().select(lnAttachmentType);

                if (lbloadImage) {
                    try {
                        String filePath = attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2;
                        if (imageinfo_temp.containsKey(filePath)) {
                            filePath2 = imageinfo_temp.get(filePath);
                        } else {
                            if (poSalesInquiryController.SalesInquiry().TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null
                                    && !"".equals(poSalesInquiryController.SalesInquiry().TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                                filePath2 = poSalesInquiryController.SalesInquiry().TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + filePath;
                            } else {
                                filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + filePath;
                            }
                        }

                        if (filePath != null && !filePath.isEmpty()) {
                            Path imgPath = Paths.get(filePath2);
                            String convertedPath = imgPath.toUri().toString();
                            boolean isPdf = filePath.toLowerCase().endsWith(".pdf");

                            stackPane1.getChildren().clear();
                            if (!isPdf) {
                                Image loimage = new Image(convertedPath);
                                imageView.setImage(loimage);
                                JFXUtil.adjustImageSize(loimage, imageView, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);

                                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                                delay.setOnFinished(event -> Platform.runLater(() -> JFXUtil.stackPaneClip(stackPane1)));
                                delay.play();

                                stackPane1.getChildren().add(imageView);
                                stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);
                                StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);
                                StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));
                            } else {
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
                    stackPane1.getChildren().clear();
                    stackPane1.getChildren().add(imageView);
                    stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);
                    Platform.runLater(() -> JFXUtil.stackPaneClip(stackPane1));
                    pnAttachment = 0;
                }
            }
        } catch (Exception ex) {
        }
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poSalesInquiryController.SalesInquiry().Master().Company().getCompanyName() + " - " + poSalesInquiryController.SalesInquiry().Master().Industry().getDescription());
            tfSearchClient.setText(psSearchClientId.equals("") ? "" : poSalesInquiryController.SalesInquiry().Master().Client().getCompanyName());

            tfSearchReferenceNo.setText("");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void clearTextFields() {
        psSearchClientId = "";
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse, apAttachments);
    }
}
