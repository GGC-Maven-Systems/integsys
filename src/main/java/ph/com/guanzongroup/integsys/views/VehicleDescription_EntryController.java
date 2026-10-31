package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleDescription;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class VehicleDescription_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static VehicleDescription poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCategoryId = "";
    private String psCompanyId = "";
    private boolean pbEntered = false;
    private int pnDetail = 0;
    private int pnAttachment = 0;
    private boolean isForDialog = false;
    private boolean isForUpdate = false;
    private String lsStandardRateID = "";
    JFXUtil.StageManager stageParameter = new JFXUtil.StageManager();
    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster, apAttachments, apAttachmentButtons, apBrowse;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnDeactivate, btnHistory, btnClose, btnBrand, btnModel, btnColor, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private Tab tabDescription, tabAttachments;
    @FXML
    private Label lblStatus;
    @FXML
    private TextField tfVariantID, tfBrand, tfModel, tfColor, tfVariant, tfYearModel, tfAuthorizeCapacity, tfAttachmentNo, tfSearchVariant;
    @FXML
    private ComboBox cmbTransmission, cmbVehicleType, cmbBodyType;
    @FXML
    private CheckBox cbEndOfLife;
    @FXML
    private TableView tblAttachments;
    @FXML
    private TableColumn tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

    ObservableList<String> comboboxlistTransmission = FXCollections.observableArrayList("Manual", "Automatic", "CVT", "DCT");
    ObservableList<String> comboboxlistVehicleType = FXCollections.observableArrayList("Private", "Commercial");
    ObservableList<String> comboboxlistBodyType = FXCollections.observableArrayList("Sedan", "SUV", "Hatchback", "MPV");
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    private String lsID = "";
    JFXUtil.ReloadableTableTask loadTableAttachment;
    private FileChooser fileChooser;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        this.psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryId = fsValue;
    }

    public void initializeDialog(GRiderCAS oApp1) {
        oApp = oApp1;
        poController = new SalesControllers(oApp, null).VehicleDescription();
    }

    public void ForDialog(boolean lbisForDialog) {
        isForDialog = lbisForDialog;
    }

    public boolean isForDialog() {
        return isForDialog;
    }

    public void openRecordForAdd() {
        btnNew.fire();
    }

    public void isForUpdate(boolean lbisForUpdate) {
        isForUpdate = lbisForUpdate;
    }

    private boolean isForUpdate() {
        return isForUpdate;
    }

    public void openRecordForUpdate(String lsValue) {
        lsID = lsValue;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (isForDialog()) {
            } else {
                poController = new SalesControllers(oApp, null).VehicleDescription();
            }

            poController.initialize();
            poJSON = new JSONObject();
            initTextFields();
            clearTextFields();
            initComboboxes();
            initAttachmentsGrid();
            initLoadTable();
            initTabPane();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            poController.setWithUI(true);
            Platform.runLater(() -> {
                poController.setIndustryId(psIndustryId);
                poController.setCategoryId(psCategoryId);

                if (isForUpdate()) {
                    try {
                        poController.OpenRecord(lsID);
                        loadRecordMaster();
                        pnEditMode = poController.getEditMode();
                        initButton(pnEditMode);
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                } else {
                    btnNew.fire();
                }
            });
            poController.setRecordStatus("01234");
            initAttachmentPreviewPane();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
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
                        poController.setRecordStatus("01234");
                        poJSON = poController.searchRecord("", false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfVariantID.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        JFXUtil.clickTabByTitleText(tabPaneMain, "Description");
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                        } else {
                            return;
                        }
                        if (isForDialog()) {
                            CommonUtils.closeStage(btnClose);
                        } else {
                            appUnload = new unloadForm();
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        JFXUtil.clickTabByTitleText(tabPaneMain, "Description");
                        poController.initialize();
                        clearTextFields();

                        poJSON = poController.NewRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        JFXUtil.clearTextFields(apBrowse);
                        break;
                    case "btnUpdate":
                        poJSON = poController.UpdateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        JFXUtil.clickTabByTitleText(tabPaneMain, "Description");
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            poController.initialize();
                            clearTextFields();
                            pnEditMode = EditMode.UNKNOWN;
                            JFXUtil.clearTextFields(apBrowse);
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
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.saveRecord();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                loadRecordMaster();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                // Print Transaction Prompt
                                poController.initialize();
                                pnEditMode = poController.getEditMode();
                            }
                        } else {
                            return;
                        }
                        if (isForDialog()) {
                            CommonUtils.closeStage(btnClose);
                        }
                        break;
                    case "btnActivate":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to activate the record?") == false) {
                            return;
                        }
                        poJSON = poController.ActivateRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            break;
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getVariantId());
                        pnEditMode = poController.getEditMode();
                        if (isForDialog()) {
                            CommonUtils.closeStage(btnClose);
                        }
                        break;
                    case "btnDeactivate":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to deactivate the record?") == false) {
                            return;
                        }
                        poJSON = poController.DeactivateRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            break;
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getVariantId());
                        pnEditMode = poController.getEditMode();
                        if (isForDialog()) {
                            CommonUtils.closeStage(btnClose);
                        }
                        break;
                    case "btnBrand":
                        openParameter(false, lsButton);
                        break;
                    case "btnModel":
                        openParameter(false, lsButton);
                        break;
                    case "btnColor":
                        openParameter(false, lsButton);
                        break;
                    case "btnAddAttachment":
                        if (poController.getTransactionAttachmentCount() > 0) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Unable to upload more than one image!\nRemove existing image to replace.");
                            return;
                        }
                        fileChooser = new FileChooser();
                        fileChooser.setTitle("Choose Image");
                        fileChooser.getExtensionFilters().addAll(
                                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg")
                        );
                        java.io.File selectedFile = fileChooser.showOpenDialog((Stage) btnAddAttachment.getScene().getWindow());

                        if (selectedFile != null) {
                            // Read image from the selected file
                            Path imgPath = selectedFile.toPath();
                            Image loimage = new Image(Files.newInputStream(imgPath));
                            imageView.setImage(loimage);

                            //Validate attachment
                            String imgPath2 = selectedFile.getName().toString();
                            for (int lnCtr = 0; lnCtr <= poController.getTransactionAttachmentCount() - 1; lnCtr++) {
                                if (imgPath2.equals(poController.TransactionAttachmentList(lnCtr).getModel().getFileName())
                                        && RecordStatus.ACTIVE.equals(poController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "File name already exists.");
                                    pnAttachment = lnCtr;
                                    loadRecordAttachment(true);
                                    return;
                                }
                            }

                            pnAttachment = poController.addAttachment(imgPath2);
                            //Copy file to Attachment path
                            poController.copyFile(selectedFile.toString());
                            loadTableAttachment.reload();
                            tblAttachments.getFocusModel().focus(pnAttachment);
                            tblAttachments.getSelectionModel().select(pnAttachment);
                        }
                        break;
                    case "btnRemoveAttachment":
                        if (poController.getTransactionAttachmentCount() <= 0) {
                            return;
                        } else {
                            for (int lnCtr = 0; lnCtr < poController.getTransactionAttachmentCount(); lnCtr++) {
                                if (RecordStatus.INACTIVE.equals(poController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    if (pnAttachment == lnCtr) {
                                        return;
                                    }
                                }
                            }
                        }
                        poJSON = poController.removeAttachment(pnAttachment);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        attachment_data.remove(tblAttachments.getSelectionModel().getSelectedIndex());
                        if (pnAttachment != 0) {
                            pnAttachment -= 1;
                        }
                        loadRecordAttachment(false);
                        loadTableAttachment.reload();
                        if (attachment_data.size() <= 0) {
                            JFXUtil.clearTextFields(apAttachments);
                        }
                        initAttachmentsGrid();
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVoid")) {
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Description");
                    clearTextFields();
                }
                initButton(pnEditMode);
                loadRecordMaster();
                loadTableAttachment.reload();
            } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            } catch (IOException ex) {
                Logger.getLogger(VehicleDescription_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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

    String lsId = "";

    private void openParameter(boolean isForUpdate, String lsValue) {
        String lsFXML = "";
        Object controller = new Object();

        switch (lsValue) {
            case "btnBrand":
                BrandController controller1 = new BrandController(); //Should differ the parameter calling
                lsFXML = "/ph/com/guanzongroup/integsys/views/Brand.fxml";
                controller1.ForDialog(true);
                if (isForUpdate) {
                    controller1.isForUpdate(true);
                    controller1.openRecordForUpdate(lsId);
                }
                controller1.initializeDialog(oApp);
                controller1.setIndustryID(psIndustryId);
                controller = controller1;
                break;
            case "btnModel":
                VehicleModel_EntryController controller2 = new VehicleModel_EntryController(); //Should differ the parameter calling
                lsFXML = "/ph/com/guanzongroup/integsys/views/VehicleModel_Entry.fxml";
                controller2.ForDialog(true);
                if (isForUpdate) {
                    controller2.isForUpdate(true);
                    controller2.openRecordForUpdate(lsId);
                }
                controller2.initializeDialog(oApp);
                controller2.setIndustryID(psIndustryId);
                controller = controller2;
                break;
            case "btnColor":
                ColorController controller3 = new ColorController(); //Should differ the parameter calling
                lsFXML = "/ph/com/guanzongroup/integsys/views/Color.fxml";
                controller3.ForDialog(true);
                if (isForUpdate) {
                    controller3.isForUpdate(true);
                    controller3.openRecordForUpdate(lsId);
                }
                controller3.initializeDialog(oApp);
                controller3.setIndustryID(psIndustryId);
                controller = controller3;
                break;
        }
        poJSON = new JSONObject();
        if (stageParameter != null) {
            stageParameter.closeDialog();
            stageParameter = new JFXUtil.StageManager();
        } else {
            stageParameter = new JFXUtil.StageManager();
        }
        try {
            stageParameter.setOnHidden(event -> {
                stageParameter = null;
                loadRecordMaster();
            });
            stageParameter.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource(lsFXML), controller, "Parameter Dialog", true, false, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poController.TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                }
                int lnAttachmentType = 0;
                lnAttachmentType = Integer.parseInt(lsAttachmentType);
//                tfAttachmentSource.setText(poController.TransactionAttachmentSource(pnAttachment));
                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";

                        // in server
                        if (poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                            filePath2 = poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        } else {
                            filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
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

    private void initLoadTable() {
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
    }

    private void initAttachmentsGrid() {
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
        tblAttachments.setItems(attachment_data);
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBrand":
                        if (lsValue.isEmpty()) {
                            poController.setBrandIdId(null);
                            poController.getModel().setModelId(null);
                            poController.getModel().setColorId(null);
                        }
                        break;
                    case "tfModel":
                        if (lsValue.isEmpty()) {
                            poController.getModel().setModelId(null);
                            poController.getModel().setColorId(null);
                        }
                        break;
                    case "tfColor":
                        if (lsValue.isEmpty()) {
                            poController.getModel().setColorId(null);
                        }
                        break;
                    case "tfVariant":
                        poJSON = poController.getModel().setDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfYearModel":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setYearModel(Integer.parseInt(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfAuthorizeCapacity":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModelVariantInsurance().setAuthCapx(Integer.parseInt(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfSearchVariant":
                        if (lsValue.isEmpty()) {
                        }
                        break;
                }
                loadRecordMaster();
            });

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbTransmission":
                        poJSON = poController.getModelVariantInsurance().setTransmission(String.valueOf(selectedValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "cmbVehicleType":
                        poJSON = poController.getModelVariantInsurance().setVehicleType(String.valueOf(selectedValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "cmbBodyType":
                        poJSON = poController.getModelVariantInsurance().setBodyType(String.valueOf(selectedValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfSearchVariant, tfBrand, tfModel, tfColor, tfVariant, tfYearModel, tfAuthorizeCapacity);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
        JFXUtil.inputIntegersOnly(tfYearModel, tfAuthorizeCapacity);
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        //apBrowse
                        case "tfSearchVariant":
                            poJSON = poController.searchRecord(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfBrand);
                            }
                            pnEditMode = poController.getEditMode();
                            initButton(pnEditMode);
                            tfSearchVariant.setText(poController.getModel().getDescription());
                            loadRecordMaster();
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Description");
                            break;
                        //apMaster
                        case "tfBrand":
                            poJSON = poController.SearchBrand(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(btnModel);
                            }
                            loadRecordMaster();
                            break;
                        case "tfModel":
                            poJSON = poController.SearchModel(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfColor);
                            }
                            loadRecordMaster();
                            break;
                        case "tfColor":
                            poJSON = poController.SearchColor(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfVariant);
                            }
                            loadRecordMaster();
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apMaster, apAttachments);
    }

    private void loadRecordMaster() {
        try {
            Platform.runLater(() -> {
                JFXUtil.setStatusValue(lblStatus, RecordStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.getModel().getRecordStatus());
            });
            boolean lbStat = pnEditMode == EditMode.UPDATE;
            if (lbStat) {
                JFXUtil.setDisabled(true, tfBrand, tfModel, tfColor);
            }

            tfVariantID.setText(poController.getModel().getVariantId());
            tfBrand.setText(poController.getBrand());
            tfModel.setText(poController.getModel().Model().getDescription());
            tfColor.setText(poController.getModel().Color().getDescription());
            tfVariant.setText(poController.getModel().getDescription());
            tfYearModel.setText(String.valueOf(poController.getModel().getYearModel()));

            if (JFXUtil.isObjectEqualTo(poController.getModelVariantInsurance().getBodyType(), "", null) && pnEditMode == EditMode.ADDNEW) {
                poController.getModelVariantInsurance().setBodyType("Sedan");
                JFXUtil.setCmbValue(cmbBodyType, !poController.getModelVariantInsurance().getBodyType().equals("") ? String.valueOf(poController.getModelVariantInsurance().getBodyType()) : -1);
            } else {
                JFXUtil.setCmbValue(cmbBodyType, !poController.getModelVariantInsurance().getBodyType().equals("") && pnEditMode != EditMode.UNKNOWN ? String.valueOf(poController.getModelVariantInsurance().getBodyType()) : -1);
            }

            if (JFXUtil.isObjectEqualTo(poController.getModelVariantInsurance().getVehicleType(), "", null) && pnEditMode == EditMode.ADDNEW) {
                poController.getModelVariantInsurance().setVehicleType("Private");
                JFXUtil.setCmbValue(cmbVehicleType, !poController.getModelVariantInsurance().getVehicleType().equals("") ? String.valueOf(poController.getModelVariantInsurance().getVehicleType()) : -1);
            } else {
                JFXUtil.setCmbValue(cmbVehicleType, !poController.getModelVariantInsurance().getVehicleType().equals("") && pnEditMode != EditMode.UNKNOWN ? String.valueOf(poController.getModelVariantInsurance().getVehicleType()) : -1);
            }

            if (JFXUtil.isObjectEqualTo(poController.getModelVariantInsurance().getTransmission(), "", null) && pnEditMode == EditMode.ADDNEW) {
                poController.getModelVariantInsurance().setTransmission("Manual");
                JFXUtil.setCmbValue(cmbTransmission, !poController.getModelVariantInsurance().getTransmission().equals("") ? String.valueOf(poController.getModelVariantInsurance().getTransmission()) : -1);
            } else {
                JFXUtil.setCmbValue(cmbTransmission, !poController.getModelVariantInsurance().getTransmission().equals("") && pnEditMode != EditMode.UNKNOWN ? String.valueOf(poController.getModelVariantInsurance().getTransmission()) : -1);
            }
            tfAuthorizeCapacity.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(poController.getModelVariantInsurance().getAuthCapx()));
            cbEndOfLife.setSelected(JFXUtil.isObjectEqualTo(poController.getModel().getEndOfLife(), "1"));
            JFXUtil.updateCaretPositions(apMaster);
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
                case "cbEndOfLife": // this is the id
                    poJSON = poController.getModel().setEndOfLife(checkedBox.isSelected() ? "1" : "0");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
            }
            loadRecordMaster();
        }
    }

    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlistBodyType, cmbBodyType),
                new JFXUtil.Pairs<>(comboboxlistVehicleType, cmbVehicleType), new JFXUtil.Pairs<>(comboboxlistTransmission, cmbTransmission));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbBodyType, cmbVehicleType, cmbTransmission);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbBodyType, cmbVehicleType, cmbTransmission);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew, btnBrowse);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);
        JFXUtil.setButtonsVisibility(false, btnActivate, btnDeactivate);
        JFXUtil.setDisabledExcept(!lbShow, apMaster, btnBrand, btnModel, btnColor);
        JFXUtil.setDisabled(!lbShow, tfBrand, tfModel, tfColor);
        JFXUtil.setDisabled(!lbShow, btnAddAttachment, btnRemoveAttachment, apAttachments);
        if (fnValue != EditMode.READY) {
            return;
        }
        //enables disables visibility of buttons
        switch (poController.getModel().getRecordStatus()) {
            case RecordStatus.ACTIVE:
                JFXUtil.setButtonsVisibility(true, btnDeactivate);
                JFXUtil.setButtonsVisibility(false, btnActivate);
                break;
            case RecordStatus.INACTIVE:
                JFXUtil.setButtonsVisibility(true, btnActivate);
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                JFXUtil.setButtonsVisibility(false, btnDeactivate);
                break;
        }
    }
}
