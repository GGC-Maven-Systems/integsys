package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableRow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.inv.warehouse.InventoryCount;
import org.json.simple.JSONObject;
import org.guanzon.cas.inv.warehouse.status.InventoryCountStatus;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Count_Detail;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Count_Master;
import org.guanzon.cas.inv.warehouse.services.InvWarehouseControllers;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class InventoryCountController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Inventory Count Entry";
    private String psIndustryID, psCompanyID, psCategoryID;
    private Control lastFocusedControl;
    private InventoryCount poAppController;
    private ObservableList<Model_Inventory_Count_Master> laTransactionMaster;
    private ObservableList<Model_Inventory_Count_Detail> laTransactionDetail;
    private int pnSelectMaster, pnEditMode, pnTransactionDetail;
    private boolean pbIsProgrammaticSelection = false;

    @FXML
    private AnchorPane apMainAnchor, apMaster, apDetail, apDetailOther, apTransaction,
            apAttachmentButtons, apBrowse, apButton, apAttachments;

    @FXML
    private DatePicker dpTransactionDate, dpRequestedDate, dpCutOffDate;

    @FXML
    private TextArea taRemarks, taRemarksDetail;

    @FXML
    private TextField tfSearchTransNo, tfTransNo, tfBarcode, tfDescription, tfSupersede, tfBrand, tfModel, tfColor,
            tfVariant, tfMeasure, tfInvType, tfRequestedBy, tfCountNo, tfSearchInvCountType, tfInclusion,
            tfInventoryCountType, tfWarehouse, tfBin, tfClassification, tfSection, tfQOH,
            tfEntryNo, tfActualQuantity, tfMS, tfEX, tfSE, tfDE, tfDG, tfTD, tfAttachmentNo;

    @FXML
    private Button btnNew, btnUpdate, btnSearch, btnBrowse, btnSave, btnPrint, btnCancel,
            btnHistory, btnClose, btnVoid,
            btnArrowLeft, btnArrowRight, btnAddAttachment, btnRemoveAttachment;
    @FXML
    private TableView<Model_Inventory_Count_Detail> tblViewDetails;

    @FXML
    private TableColumn<Model_Inventory_Count_Detail, String> tblColNo, tblColBarcode, tblColDescription,
            tblColBrand, tblColMeasure, tblColQOH, tblColCount1, tblColCount2, tblColCount3, tblColVariance;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private StackPane stackPane1;

    @FXML
    private ImageView imageView;

    @FXML
    private ComboBox cmbAttachmentType;

    @FXML
    private TableView tblAttachments;

    @FXML
    private TableColumn tblRowNoAttachment, tblFileNameAttachment;

    @FXML
    private TabPane tabPaneMain;

    @FXML
    private ComboBox<String> cmbInclusion;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new InvWarehouseControllers(poApp, poLogWrapper).InventoryCount();
            poAppController.setTransactionStatus("10");

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {

                poAppController.setTransactionStatus("10");
                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCompanyID(psCompanyID);
                poAppController.setCategoryID(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID
                        + "\nCompany :" + psCompanyID
                        + "\nCategory:" + psCategoryID);
                try {
                    if (!isJSONSuccess(poAppController.isOfficerEmployee(), " Initialize Officer level")) {
                        unloadForm appUnload = new unloadForm();
                        appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(InventoryCountController.class.getName()).log(Level.SEVERE, null, ex);
                    unloadForm appUnload = new unloadForm();
                    appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                }
                btnNew.fire();

            });
            initializeTableDetail();
            initControlEvents();
            initAttachmentsGrid();
            initAttachmentPreviewPane();
            lblSource.setText(poAppController.getMaster().Industry().getDescription());

        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());

        }
    }

    @FXML
    void ontblDetailClicked(MouseEvent e) {
        try {
            pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1;
            if (pnTransactionDetail <= 0) {
                return;
            }

            loadSelectedTransactionDetail(pnTransactionDetail);
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnSearch":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfInventoryCountType":
                            if (!isJSONSuccess(poAppController.searchInventoryCountType(tfInventoryCountType.getText(), false),
                                    "Initialize Search Inventory Count! ")) {
                                return;
                            }
                            tfInventoryCountType.setText(poAppController.getMaster().InventoryCountType().getDescription());
                            loadTransactionDetailList();

                            JFXUtil.clickTabByTitleText(tabPaneMain, "Details");

                            break;
                        case "tfRequestedBy":
                            if (!isJSONSuccess(poAppController.searchRequestBy(tfRequestedBy.getText(), false),
                                    "Initialize Search Requested By! ")) {
                                return;
                            }
                            tfRequestedBy.setText(poAppController.getMaster().ClientRequestBy().getCompanyName());
                            break;
                    }
                    break;

                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchInvCountType":

                            if (!isJSONSuccess(poAppController.searchTransactionOpen(tfSearchInvCountType.getText(), true, false),
                                    "Initialize Search Inventory Count ! ")) {
                                return;
                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());

                            JFXUtil.clickTabByTitleText(tabPaneMain, "Details");
                            break;
                        case "tfSearchTransNo":

                            if (!isJSONSuccess(poAppController.searchTransactionOpen(tfSearchTransNo.getText(), true, true),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }

//                                tfSearchTransNo.setText(poAppController.getMaster().getTransactionNo());
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());

                            JFXUtil.clickTabByTitleText(tabPaneMain, "Details");
                            break;
                    }
                    break;

                case "btnNew":
                    if (!isJSONSuccess(poAppController.NewTransaction(), "Initialize New Transaction")) {
                        return;
                    }
                    clearAllInputs();
                    if (!isJSONSuccess(poAppController.loadInventoryCountType(), "Initialize load default Inventory Count type")) {
                        // no default must be auto close the entry form
                        unloadForm appUnload = new unloadForm();
                        stageAttachment.closeDialog();
                        appUnload.unloadForm(apMainAnchor, poApp, psFormName);

                        return;
                    }

                    tfInventoryCountType.setText(poAppController.getMaster().InventoryCountType().getDescription());
                    loadTransactionDetailList();

                    JFXUtil.clickTabByTitleText(tabPaneMain, "Details");
                    loadTableAttachment.reload();
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnUpdate":
                    if (poAppController.getMaster().getTransactionNo() == null || poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Inventory Count", "");
                        return;
                    }

                    if (!isJSONSuccess(poAppController.UpdateTransaction(), "Initialize Update Transaction")) {
                        return;
                    }
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnSave":
                    if (tfTransNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Inventory Count", "");
                        return;
                    }
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save transaction?") == false) {
                        return;
                    }
                    if (!isJSONSuccess(poAppController.SaveTransaction(), "Initialize Save Transaction")) {
                        return;
                    }

                    if (!isJSONSuccess(poAppController.CloseTransaction(), "Initialize Close Transaction")) {
                        return;
                    }
                    if (ShowMessageFX.YesNo(null, psFormName, "Do you want to print transaction?") == true) {
                        if (!isJSONSuccess(poAppController.printRecord(), "Initialize print Transaction")) {
//                            return;
                        }
                    }

                    reloadTableDetail();
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();

                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poAppController = new InvWarehouseControllers(poApp, poLogWrapper).InventoryCount();
                        poAppController.setTransactionStatus("10");
                        stageAttachment.closeDialog();
                        if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                            unloadForm appUnload = new unloadForm();
                            appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        }

                        Platform.runLater(() -> {
                            poAppController.setTransactionStatus("10");
//                            poAppController.getMaster().setIndustryId(psIndustryID);
                            poAppController.setIndustryID(psIndustryID);
                            poAppController.setCompanyID(psCompanyID);
                            poAppController.setCategoryID(psCategoryID);

                            clearAllInputs();
                        });
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;

                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", psFormName, null);
                        return;
                    }

                    try {
                        poAppController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", psFormName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                    }
                    break;

                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        stageAttachment.closeDialog();
                        appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                    }
                    break;

                case "btnPrint":
                    if (poAppController.getMaster().getTransactionNo() == null || poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Stock Request Approval", "");
                        return;
                    }
//                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to print the transaction ?") == true) {
                    if (!isJSONSuccess(poAppController.printRecord(),
                            "Initialize Print Transaction")) {
                        return;
                    }
                    break;
//                    }
                //ref
                case "btnVoid":
                    if (tfTransNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null, "Issuance Approval");
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to Cancel transaction?") == true) {

                        if (!isJSONSuccess(poAppController.CancelTransaction(), "Initialize Cancel Transaction")) {
                            return;
                        }

                        reloadTableDetail();
                        getLoadedTransaction();
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }

                    break;
                case "btnAddAttachment":
                    fileChooser = new FileChooser();
                    fileChooser.setTitle("Choose Image");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.pdf")
                    );
                    java.io.File selectedFile = fileChooser.showOpenDialog((Stage) btnAddAttachment.getScene().getWindow());

                    if (selectedFile != null) {
                        // Read image from the selected file
                        Path imgPath = selectedFile.toPath();
                        Image loimage = new Image(Files.newInputStream(imgPath));
                        imageView.setImage(loimage);

                        //Validate attachment
                        String imgPath2 = selectedFile.getName().toString();
                        for (int lnCtr = 0; lnCtr <= poAppController.getTransactionAttachmentCount() - 1; lnCtr++) {
                            if (imgPath2.equals(poAppController.TransactionAttachmentList(lnCtr).getModel().getFileName())
                                    && RecordStatus.ACTIVE.equals(poAppController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                ShowMessageFX.Warning(null, psFormName, "File name already exists.");
                                pnAttachment = lnCtr;
                                loadRecordAttachment(true);
                                return;
                            }
                        }
                        if (imageinfo_temp.containsKey(selectedFile.getName().toString())) {
                            ShowMessageFX.Warning(null, psFormName, "File name already exists.");
                            loadRecordAttachment(true);
                            return;
                        } else {
                            imageinfo_temp.put(selectedFile.getName().toString(), imgPath.toString());
                        }

                        //Limit maximum pages of pdf to add
                        if (imgPath2.toLowerCase().endsWith(".pdf")) {
                            try (PDDocument document = PDDocument.load(selectedFile)) {
                                PDFRenderer pdfRenderer = new PDFRenderer(document);
                                int pageCount = document.getNumberOfPages();
                                if (pageCount > 20) {
                                    ShowMessageFX.Warning(null, psFormName, "PDF exceeds maximum allowed pages.");
                                    return;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(CashDisbursement_EntryController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        pnAttachment = poAppController.addAttachment(imgPath2);
                        //Copy file to Attachment path
                        poAppController.copyFile(selectedFile.toString());
                        loadTableAttachment.reload();
                        tblAttachments.getFocusModel().focus(pnAttachment);
                        tblAttachments.getSelectionModel().select(pnAttachment);
                    }
                    break;
                case "btnRemoveAttachment":
                    if (poAppController.getTransactionAttachmentCount() <= 0) {
                        return;
                    } else {
                        for (int lnCtr = 0; lnCtr < poAppController.getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poAppController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                if (pnAttachment == lnCtr) {
                                    return;
                                }
                            }
                        }
                    }
                    if (!isJSONSuccess(poAppController.removeAttachment(pnAttachment), "Initialize remove Attachment")) {
                        return;
                    }
                    attachment_data.remove(tblAttachments.getSelectionModel().getSelectedIndex());
                    if (pnAttachment != 0) {
                        pnAttachment -= 1;
                    }
                    imageinfo_temp.clear();
                    loadRecordAttachment(false);
                    loadTableAttachment.reload();
                    if (attachment_data.size() <= 0) {
                        JFXUtil.clearTextFields(apAttachments);
                    }
                    initAttachmentsGrid();
                    break;
                case "btnArrowRight":
                    slideImage(1);
                    break;
                case "btnArrowLeft":
                    slideImage(-1);
                    break;
            }

            initButtonDisplay(poAppController.getEditMode());

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }
    private final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }
        try {
            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {
                    case "taRemarks":
                        poAppController.getMaster().setRemarks(lsValue);
                        loadTransactionMaster();
                        break;

                    case "taRemarksDetail":
                        poAppController.getDetail(pnTransactionDetail).setRemarks(lsValue);
                        reloadTableDetail();
                        loadSelectedTransactionDetail(pnTransactionDetail);
                        break;

                }
            } else {
                loTextField.selectAll();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        try {
            if (lsValue == null) {
                return;
            }

            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {
                    case "tfActualQuantity":
                        if (poAppController.getDetail(pnTransactionDetail).getStockId() == null
                                || poAppController.getDetail(pnTransactionDetail).getStockId().isEmpty()) {
                            if (Double.parseDouble(tfActualQuantity.getText()) > 0.0) {
                                tfActualQuantity.setText("0.00");
                                loTextField.requestFocus();
                                ShowMessageFX.Information("Unable to set quantity! No Stock Invetory Detected", psFormName, null);
                            }
                            return;
                        }
                        double lnActualQty;
                        try {
                            lnActualQty = Double.parseDouble(lsValue);
                        } catch (NumberFormatException e) {
                            lnActualQty = 0.0; // default if parsing fails
                            reloadTableDetail();
                            loadSelectedTransactionDetail(pnTransactionDetail);
                            loTextField.requestFocus();

                        }
                        if (lnActualQty <= 0.00) {
                            return;
                        }
                        switch (poAppController.getMaster().getCounterNo()) {
                            case 1:
                                poAppController.getDetail(pnTransactionDetail).setActualCounter01(lnActualQty);
                                break;
                            case 2:
                                poAppController.getDetail(pnTransactionDetail).setActualCounter02(lnActualQty);
                                break;
                            case 3:
                                poAppController.getDetail(pnTransactionDetail).setActualCounter03(lnActualQty);
                                break;
                            default:
                                ShowMessageFX.Information("Unable to set quantity! Count is only on generation", psFormName, null);
                        }
                        reloadTableDetail();
                        loadSelectedTransactionDetail(pnTransactionDetail);
                        break;
                }
            } else {
                loTextField.selectAll();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            default:
                                CommonUtils.SetNextFocus(loTxtField);
                                break;
                            case "tfSearchInvCountType":

                                if (!isJSONSuccess(poAppController.searchTransactionOpen(lsValue, true, false),
                                        "Initialize Search Inventory Count ! ")) {
                                    return;
                                }

//                                tfSearchSourceno.setText(poAppController.getMaster().Branch().getBranchName());
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());

                                JFXUtil.clickTabByTitleText(tabPaneMain, "Details");
                                break;
                            case "tfSearchTransNo":
                                if (!tfTransNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Transaction", "Are you sure you want to replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransactionOpen(lsValue, true, true),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }

//                                tfSearchTransNo.setText(poAppController.getMaster().getTransactionNo());
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());

                                JFXUtil.clickTabByTitleText(tabPaneMain, "Details");
                                break;
                            case "tfInventoryCountType":
                                if (!isJSONSuccess(poAppController.searchInventoryCountType(tfInventoryCountType.getText(), false),
                                        "Initialize Search Inventory Count! ")) {
                                    return;
                                }
                                tfInventoryCountType.setText(poAppController.getMaster().InventoryCountType().getDescription());
                                loadTransactionDetailList();

                                JFXUtil.clickTabByTitleText(tabPaneMain, "Details");

                                break;
                            case "tfRequestedBy":
                                if (!isJSONSuccess(poAppController.searchRequestBy(tfRequestedBy.getText(), false),
                                        "Initialize Search Requested by! ")) {
                                    return;
                                }
                                tfRequestedBy.setText(poAppController.getMaster().ClientRequestBy().getCompanyName());
                                break;

                        }
                        break;
                }
            }
        } catch (Exception ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private void loadTransactionDetailList() {
        StackPane overlay = getOverlayProgress(apMaster);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_Inventory_Count_Detail>> loadTransaction = new Task<ObservableList<Model_Inventory_Count_Detail>>() {
            @Override
            protected ObservableList<Model_Inventory_Count_Detail> call() throws Exception {
                if (!isJSONSuccess(poAppController.generateDetail(),
                        "Initialize : Load of generate List")) {
                    return null;
                }

                List<Model_Inventory_Count_Detail> rawList = poAppController.getDetailList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                ObservableList<Model_Inventory_Count_Detail> laDetailList = getValue();
                tblViewDetails.setItems(laDetailList);

                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                Logger
                        .getLogger(DeliverySchedule_EntryController.class
                                .getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadTransaction);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadTransactionMaster() {
        try {
            lblSource.setText(poAppController.getMaster().Industry().getDescription());
            lblStatus.setText(InventoryCountStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())) == null ? "STATUS"
                    : InventoryCountStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));

            tfTransNo.setText(poAppController.getMaster().getTransactionNo());
            dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getTransactionDate()));
            tfInventoryCountType.setText(poAppController.getMaster().InventoryCountType().getDescription());
            tfCountNo.setText(String.valueOf(poAppController.getMaster().getCounterNo()));
            pbIsProgrammaticSelection = true;
            if (!poAppController.getMaster().getIncluded().isEmpty()) {
                cmbInclusion.getSelectionModel().select(
                        Inclusion.fromCode(poAppController.getMaster().getIncluded()).getDisplayName());
            } else {
                cmbInclusion.getSelectionModel().select(-1);
            }
            pbIsProgrammaticSelection = false;
            tfRequestedBy.setText(String.valueOf(poAppController.getMaster().ClientRequestBy().getCompanyName()));
            dpRequestedDate.setValue(ParseDate(poAppController.getMaster().getRequestedDate()));
            dpCutOffDate.setValue(ParseDate(poAppController.getMaster().getCutOff()));
            taRemarks.setText(poAppController.getMaster().getRemarks());

            if (poAppController.getMaster().getTransactionStatus().equals(InventoryCountStatus.CONFIRMED)) {
                btnVoid.setText("Cancel");
            }
            if (tfTransNo.getText().trim().isEmpty()) {
                lblStatus.setText("UNKNOWN");
            }
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

            poLogWrapper.severe(psFormName + " :" + e.getMessage());

        }
    }

    private void loadSelectedTransactionDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {
        if (fnRow <= 0 ){
        return;
        }
        int tblIndex = fnRow - 1;

        tfBarcode.setText(tblColBarcode.getCellData(tblIndex));
        tfDescription.setText(tblColDescription.getCellData(tblIndex));
        tfBrand.setText(tblColBrand.getCellData(tblIndex));
        tfMeasure.setText(tblColMeasure.getCellData(tblIndex));

        //---------------------------Stock Detail------------------------------------
        tfSupersede.setText( poAppController.getDetail(fnRow).getStockId().isEmpty()
                ? "" : poAppController.getDetail(fnRow).Inventory().Superseded().getBarCode());
        tfModel.setText(poAppController.getDetail(fnRow).Inventory().Model().getDescription());
        tfVariant.setText(poAppController.getDetail(fnRow).Inventory().Variant().getDescription());
        tfColor.setText(poAppController.getDetail(fnRow).Inventory().Color().getDescription());
        tfInvType.setText(poAppController.getDetail(fnRow).Inventory().InventoryType().getDescription());
        //---------------------------Detail to Modify------------------------------------
        double lnActualCount = 0;
        switch (poAppController.getMaster().getCounterNo()) {
            case 1:
                lnActualCount = poAppController.getDetail(fnRow).getActualCounter01();
                break;
            case 2:
                lnActualCount = poAppController.getDetail(fnRow).getActualCounter02();
                break;
            case 3:
                lnActualCount = poAppController.getDetail(fnRow).getActualCounter03();
                break;
            default:
                lnActualCount = 0.0;
        }
        tfEntryNo.setText(String.valueOf(poAppController.getDetail(fnRow).getEntryNo()));
        tfActualQuantity.setText(String.valueOf(lnActualCount));
        tfQOH.setText(String.valueOf(poAppController.getDetail(fnRow).getQuantityOnHand()));
        taRemarksDetail.setText(poAppController.getDetail(fnRow).getRemarks());

        //---------------------------Dif Cause to Concatication------------------------------------
        tfDE.setText("0.0");
        tfMS.setText("0.0");
        tfTD.setText("0.0");
        tfEX.setText("0.0");
        tfDG.setText("0.0");
        tfSE.setText("0.0");

    }

    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TextArea) {
                TextArea loControlField = (TextArea) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(txtArea_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof DatePicker) {
                DatePicker loControlField = (DatePicker) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(dPicker_Focus);
                loControlField.getEditor().setOnKeyPressed(this::dPicker_KeyPressed);
            }
        }

        imageView.setImage(null);
        clearAllInputs();

        cmbInclusion.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null && !pbIsProgrammaticSelection) {
                        try {
                            Inclusion selected = Inclusion.fromDisplay(newVal);

                            StackPane overlay = getOverlayProgress(apMaster);
                            ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
                            overlay.setVisible(true);
                            pi.setVisible(true);

                            Task<ObservableList<Model_Inventory_Count_Detail>> overrideTask
                            = new Task<ObservableList<Model_Inventory_Count_Detail>>() {

                        @Override
                        protected ObservableList<Model_Inventory_Count_Detail> call() throws Exception {
                            JSONObject loJSON = poAppController.setInclusion(selected.name());
                            if (!isJSONSuccess(loJSON, "Initialize override detail")) {
                                return null; // signal failure
                            }

                            List<Model_Inventory_Count_Detail> rawDetail = poAppController.getDetailList();
                            System.out.println("Override detail size: " + rawDetail.size());
                            return FXCollections.observableArrayList(new ArrayList<>(rawDetail));
                        }

                        @Override
                        protected void succeeded() {
                            ObservableList<Model_Inventory_Count_Detail> result = getValue();

                            overlay.setVisible(false);
                            pi.setVisible(false);

                            if (result == null) {
                                // setInclusion failed — restore previous selection
                                pbIsProgrammaticSelection = true;
                                if (oldVal != null) {
                                    cmbInclusion.getSelectionModel().select(oldVal);
                                } else {
                                    cmbInclusion.getSelectionModel().select(-1);
                                }
                                pbIsProgrammaticSelection = false;
                                return;
                            }

                            // Force full refresh of table with new paDetail contents
                            laTransactionDetail.clear();
                            laTransactionDetail.addAll(result);
                            tblViewDetails.setItems(laTransactionDetail);
                            tblViewDetails.refresh();

                            // Reset selection to first row
                            if (!laTransactionDetail.isEmpty()) {
                                pnTransactionDetail = 1;
                                tblViewDetails.getSelectionModel().select(0);
                                tblViewDetails.scrollTo(0);
                                try {
                                    loadSelectedTransactionDetail(pnTransactionDetail);
                                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                    Logger.getLogger(InventoryCountController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }

                        @Override
                        protected void failed() {
                            overlay.setVisible(false);
                            pi.setVisible(false);

                            // Restore previous selection on failure
                            pbIsProgrammaticSelection = true;
                            if (oldVal != null) {
                                cmbInclusion.getSelectionModel().select(oldVal);
                            } else {
                                cmbInclusion.getSelectionModel().select(-1);
                            }
                            pbIsProgrammaticSelection = false;

                            Throwable ex = getException();
                            Logger.getLogger(InventoryCountController.class.getName()).log(Level.SEVERE, null, ex);
                            poLogWrapper.severe(psFormName + " : " + ex.getMessage());
                        }

                        @Override
                        protected void cancelled() {
                            overlay.setVisible(false);
                            pi.setVisible(false);
                        }
                    };

                            Thread thread = new Thread(overrideTask);
                            thread.setDaemon(true);
                            thread.start();

                        } catch (Exception ex) {
                            Logger.getLogger(InventoryCountController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
        );
    }

    public enum Inclusion {
        AI("All Items"),
        BB("Bins"),
        RX("Random"),
        C("By Classification");

        private final String displayName;

        Inclusion(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Inclusion fromDisplay(String displayName) {
            for (Inclusion i : values()) {
                if (i.displayName.equalsIgnoreCase(displayName)) {
                    return i;
                }
            }
            return AI; // default
        }

        public static Inclusion fromCode(String code) {
            if (code == null || code.isEmpty()) {
                return AI;
            }
            try {
                return valueOf(code.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return AI;
            }
        }

    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
    }

    private void clearAllInputs() {

        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl != null && (loControl instanceof TableView)) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }
            } else if (loControl != null && (loControl instanceof ComboBox)) {
                ComboBox cbox = (ComboBox) loControl;
                if (cbox.getItems() != null) {
                    cbox.getItems().clear();
                }
            }
        }
        cmbInclusion.setItems(FXCollections.observableArrayList(
                Arrays.stream(Inclusion.values())
                        .map(Inclusion::getDisplayName)
                        .collect(Collectors.toList())
        ));
        pnEditMode = poAppController.getEditMode();
        initButtonDisplay(poAppController.getEditMode());
        if (tfTransNo.getText().trim().isEmpty()) {
            lblStatus.setText("UNKNOWN");
        }
        clearAttachment();
    }

    private void initButtonDisplay(int fnEditMode) {

        boolean lbEditing = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        String lsTransNo = tfTransNo.getText();
        boolean lbHasTransaction = lsTransNo != null && !lsTransNo.isEmpty();
        boolean lbIsApproved = lbHasTransaction
                && "1".equals(poAppController.getMaster().getTransactionStatus());

        // Always visible
        initButtonControls(true, "btnRetrieve", "btnClose");

        // Editing mode buttons
        initButtonControls(lbEditing, "btnSearch", "btnSave", "btnCancel",
                "btnAddAttachment", "btnRemoveAttachment");
        initButtonControls(!lbEditing, "btnBrowse", "btnNew");

        // Transaction-dependent buttons (only when not editing)
        initButtonControls(!lbEditing && lbHasTransaction, "btnUpdate", "btnVoid", "btnHistory", "btnPrint");
        initButtonControls(!lbEditing && lbHasTransaction && !lbIsApproved, "btnUpdate");

        tfInventoryCountType.setDisable(fnEditMode == EditMode.UPDATE);
        // Disable panes during editing
        apMaster.setDisable(!lbEditing);
        apDetail.setDisable(!lbEditing);
        apDetailOther.setDisable(!lbEditing);
        //reload attachment
        loadTableAttachment.reload();

    }

    private void initButtonControls(boolean visible, String... buttonFxIdsToShow) {
        Set<String> showOnly = new HashSet<>(Arrays.asList(buttonFxIdsToShow));

        for (Field loField : getClass().getDeclaredFields()) {
            loField.setAccessible(true);
            String fieldName = loField.getName(); // fx:id

            // Only touch the buttons listed
            if (!showOnly.contains(fieldName)) {
                continue;
            }
            try {
                Object value = loField.get(this);
                if (value instanceof Button) {
                    Button loButton = (Button) value;
                    loButton.setVisible(visible);
                    loButton.setManaged(visible);
                }
            } catch (IllegalAccessException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
                ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

                poLogWrapper.severe(psFormName + " :" + e.getMessage());
                ;
            }
        }
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

    private void initializeTableDetail() {
        if (laTransactionDetail == null) {
            laTransactionDetail = FXCollections.observableArrayList();

            tblViewDetails.setItems(laTransactionDetail);
            tblViewDetails.setRowFactory(tv -> new TableRow<Model_Inventory_Count_Detail>() {
                @Override
                protected void updateItem(Model_Inventory_Count_Detail item, boolean empty) {
                    super.updateItem(item, empty);

                    // Always reset style classes first
                    getStyleClass().removeAll("row-variance", "row-selected-variance");

                    if (empty || item == null) {
                        setStyle("");
                        return;
                    }

                    if (poAppController.getMaster().getCounterNo() <= 0) {
                        setStyle("");
                        return;
                    }

                    try {
                        double lnQtyOnHand = item.getQuantityOnHand() != null
                                ? item.getQuantityOnHand() : 0.0;

                        double lnActualCount = 0.0;
                        switch (poAppController.getMaster().getCounterNo()) {
                            case 1:
                                lnActualCount = item.getActualCounter01() != null
                                        ? item.getActualCounter01() : 0.0;
                                break;
                            case 2:
                                lnActualCount = item.getActualCounter02() != null
                                        ? item.getActualCounter02() : 0.0;
                                break;
                            case 3:
                                lnActualCount = item.getActualCounter03() != null
                                        ? item.getActualCounter03() : 0.0;
                                break;
                        }

                        double lnVariance = lnActualCount - lnQtyOnHand;

                        if (Math.abs(lnVariance) < 0.0000001) {
                            // Equal — no highlight
                            setStyle("");
                        } else {
                            // Has variance — highlight yellow, selection handled by CSS
                            getStyleClass().add("row-variance");
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                        }

                    } catch (Exception e) {
                        setStyle("");
                    }

                    // Re-apply selection highlight on top of variance style
                    selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                        if (getItem() == null) {
                            return;
                        }
                        if (isSelected) {
                            getStyleClass().removeAll("row-variance");
                            getStyleClass().add("row-selected-variance");
                            setStyle("-fx-background-color: #e0a800; -fx-text-fill: #ffffff;");
                        } else {
                            getStyleClass().removeAll("row-selected-variance");
                            try {
                                double lnQOH = getItem().getQuantityOnHand() != null
                                        ? getItem().getQuantityOnHand() : 0.0;
                                double lnCount = 0.0;
                                switch (poAppController.getMaster().getCounterNo()) {
                                    case 1:
                                        lnCount = getItem().getActualCounter01() != null
                                                ? getItem().getActualCounter01() : 0.0;
                                        break;
                                    case 2:
                                        lnCount = getItem().getActualCounter02() != null
                                                ? getItem().getActualCounter02() : 0.0;
                                        break;
                                    case 3:
                                        lnCount = getItem().getActualCounter03() != null
                                                ? getItem().getActualCounter03() : 0.0;
                                        break;
                                }
                                double lnVar = lnCount - lnQOH;
                                if (Math.abs(lnVar) < 0.0000001) {
                                    setStyle("");
                                } else {
                                    getStyleClass().add("row-variance");
                                    setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                                }
                            } catch (Exception e) {
                                setStyle("");
                            }
                        }
                    });
                }
            });
            tblColQOH.setStyle("-fx-alignment: CENTER;");
            tblColCount1.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColCount2.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColCount3.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColVariance.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

            tblColNo.setCellValueFactory((loModel) -> {
                int index = tblViewDetails.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColBarcode.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().getBarCode());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDescription.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().getDescription());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColBrand.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().Brand().getDescription());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColMeasure.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().Measure().getDescription());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColQOH.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getQuantityOnHand()));

            });

            tblColCount1.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getActualCounter01()));

            });

            tblColCount2.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getActualCounter02()));

            });

            tblColCount3.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getActualCounter03()));

            });

            tblColVariance.setCellValueFactory((loModel) -> {

                double lnQOH = loModel.getValue().getQuantityOnHand() != null
                        ? loModel.getValue().getQuantityOnHand() : 0.0;
                double lnCount = 0.0;
                switch (poAppController.getMaster().getCounterNo()) {
                    case 1:
                        lnCount = loModel.getValue().getActualCounter01() != null
                                ? loModel.getValue().getActualCounter01() : 0.0;
                        break;
                    case 2:
                        lnCount = loModel.getValue().getActualCounter02() != null
                                ? loModel.getValue().getActualCounter02() : 0.0;
                        break;
                    case 3:
                        lnCount = loModel.getValue().getActualCounter03() != null
                                ? loModel.getValue().getActualCounter03() : 0.0;
                        break;
                }
                double lnVar = lnCount - lnQOH;
                return new SimpleStringProperty(String.valueOf(lnVar));

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
                                for (lnCtr = 0; lnCtr < poAppController.getTransactionAttachmentCount(); lnCtr++) {
                                    if (RecordStatus.INACTIVE.equals(poAppController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                        continue;
                                    }
                                    lnCount += 1;
                                    attachment_data.add(
                                            new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                                    String.valueOf(poAppController.TransactionAttachmentList(lnCtr).getModel().getFileName()),
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
        }
    }

    private void reloadTableDetail() {
        List<Model_Inventory_Count_Detail> rawDetail = poAppController.getDetailList();
        laTransactionDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnTransactionDetail >= 1 && pnTransactionDetail < laTransactionDetail.size())
                ? pnTransactionDetail - 1
                : laTransactionDetail.size() - 1;

        tblViewDetails.getSelectionModel().select(indexToSelect);

        pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        tblViewDetails.refresh();
    }

    private void getLoadedTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadTransactionMaster();
        reloadTableDetail();
        loadSelectedTransactionDetail(pnTransactionDetail);
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            if (message != null) {
                poLogWrapper.severe(psFormName + " :" + message);
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, message));
                }
            }
            return false;
        }

        String message = (String) loJSON.get("message");
        poLogWrapper.severe(psFormName + " :" + message);
        if (message != null) {
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Information(null, psFormName, message);
            } else {
                Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, message));
            }
        }

        poLogWrapper.info(psFormName + " : Success on " + fsModule);
        return true;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
    }

    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
                ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);

                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }

    final ChangeListener<? super Boolean> dPicker_Focus = (o, ov, nv) -> {
        DatePicker loDatePicker = (DatePicker) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsDatePickerID = loDatePicker.getId();
        LocalDate loValue = loDatePicker.getValue();

        if (loValue == null) {
            return;
        }

        LocalDateTime ldDateTimeValue = loValue.atTime(LocalTime.now());
        Date ldDateValue = Date.from(loValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!nv) {
            /*Lost Focus*/
            switch (lsDatePickerID) {
                case "dpRequestedDate":
                    poAppController.getMaster().setRequestedDate((ldDateTimeValue));
                    return;
                case "dpCutOffDate":
                    poAppController.getMaster().setCutOff((ldDateTimeValue));
                    return;

            }
        }
    };

    private void dPicker_KeyPressed(KeyEvent event) {

        TextField loTxtField = (TextField) event.getSource();
        String loDatePickerID = ((DatePicker) loTxtField.getParent()).getId(); // cautious cast
        String loValue = loTxtField.getText();
        String lsValue = "";
        if (loValue != null && !loValue.isEmpty()) {
            Date toDateValue = SQLUtil.toDate(loValue, "dd/MM/yyyy");
            lsValue = SQLUtil.dateFormat(toDateValue, SQLUtil.FORMAT_SHORT_DATE);

        }

        if (event.getCode() != null) {
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                case F3:
                    event.consume();
                    switch (loDatePickerID) {

                    }
            }
        }
        event.consume();

    }

    //-----------------------------Attachment Code--------------------------------------------------------------------------------------
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    public static ObservableList<String> documentType = FXCollections.observableArrayList("Other");
    String lsValidDisbMessage = "Please provide generate inventory count detail to proceed.";

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    JFXUtil.ReloadableTableTask loadTableAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    Map<String, String> imageinfo_temp = new HashMap<>();
    private FileChooser fileChooser;
    private int pnAttachment = 0;
    private int currentIndex = 0;

    private boolean pbIsCheckedAttachmentTab = false;

    AnchorPane root = null;
    Scene scene = null;
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    ChangeListener<Scene> WindowKeyEvent = (obs, oldScene, newScene) -> {
        if (newScene != null) {
            setKeyEvent(newScene);
        }
    };

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Details": {
                    try {
                        getLoadedTransaction();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(InventoryCountController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;

                case "Attachments":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apAttachments);
                        if (DoesContainValidDisbDetail()) {
                            pbIsCheckedAttachmentTab = true;
                            try {
                                poAppController.loadAttachments();
                            } catch (GuanzonException | SQLException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, psFormName, MiscUtil.getException(ex));
                            }

                            loadTableAttachment.reload();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Details");
                            ShowMessageFX.Warning(null, psFormName, lsValidDisbMessage);
                        }
                    }
                    break;
            }
        });

    }

    private void setKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5) {
                if (JFXUtil.isObjectEqualTo(pnEditMode, EditMode.ADDNEW, EditMode.READY, EditMode.UPDATE)) {
                    if (DoesContainValidDisbDetail()) {
                    } else {
                        ShowMessageFX.Warning(null, psFormName, lsValidDisbMessage);
                        return;
                    }
                    showAttachmentDialog();
                }
            }
            if (event.getCode() == KeyCode.F12) {
                LoginControllerHolder.getMainController().eventf12(LoginControllerHolder.getMainController().getTab());
            }
        });
        scene.focusOwnerProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                if (newNode instanceof Button) {
                } else {
                    lastFocusedTextField.set(newNode);
                    previousSearchedTextField.set(null);
                }
            }
        });
    }

    private boolean DoesContainValidDisbDetail() {

        if (poAppController.getDetailCount() <= 0) {
            return false;
        }
        return true;
    }

    public void resetImageBounds() {
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        stackPane1.setAlignment(imageView, javafx.geometry.Pos.CENTER);
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

    public void clearAttachment() {

        stageAttachment.closeDialog();
        tfAttachmentNo.clear();
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setDisable(true);

    }

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(documentType, cmbAttachmentType));

    }

    public void initComboboxes() {

        initComboBoxes();
        // ComboBox setup
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poAppController.TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (Exception e) {
                }
            }
        });
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAttachmentType);
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poAppController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poAppController.TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poAppController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                }
                int lnAttachmentType = 0;
                lnAttachmentType = Integer.parseInt(lsAttachmentType);
                cmbAttachmentType.getSelectionModel().select(lnAttachmentType);
//                tfAttachmentSource.setText(poController.TransactionAttachmentSource(pnAttachment));
                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";
                        if (imageinfo_temp.containsKey((String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02())) {
                            filePath2 = imageinfo_temp.get((String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02());
                        } else {
                            // in server
                            if (poAppController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null
                                    && !"".equals(poAppController.TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                                filePath2 = poAppController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            } else {
                                filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            }
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

    public void RemoveWindowEvent() {
        root.sceneProperty().removeListener(WindowKeyEvent);
        scene.setOnKeyPressed(null);
        stageAttachment.closeDialog();
    }

    public void showAttachmentDialog() {
        stageAttachment.closeDialog();
        if (poAppController.getTransactionAttachmentCount() <= 0) {
            ShowMessageFX.Warning(null, psFormName, "No transaction attachment to load.");
            return;
        }
        Map<String, Pair<String, String>> data = new HashMap<>();
        data.clear();
        int lnCount = 0;
        for (int lnCtr = 0; lnCtr < poAppController.getTransactionAttachmentCount(); lnCtr++) {
            if (RecordStatus.INACTIVE.equals(poAppController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                continue;
            }
            lnCount += 1;
            data.put(String.valueOf(lnCount), new Pair<>(String.valueOf(poAppController.TransactionAttachmentList(lnCtr).getModel().getFileName()),
                    poAppController.TransactionAttachmentList(lnCtr).getModel().getDocumentType()));
        }
        AttachmentDialogController controller = new AttachmentDialogController();
        controller.setOpenedImage(pnAttachment);
        controller.addData(data);

        try {
            stageAttachment.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/AttachmentDialog.fxml"), controller, "Attachment Dialog", false, false, true);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, psFormName, MiscUtil.getException(ex));
        }
    }
}
