package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.account.Account_Accreditation;
import org.guanzon.cas.client.constants.AccountAccreditationStatus;
import org.guanzon.cas.client.services.ClientControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Guiller & Team 1
 */
public class AccountsAccreditation_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String pxeModuleName = "Accounts Accreditation Entry";
    private Control lastFocusedControl;
    private Account_Accreditation poController;
    private int pnAttachment = 0;
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    JFXUtil.ReloadableTableTask loadTableAttachment;
    private FileChooser fileChooser;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    private int currentIndex = 0;
    private unloadForm poUnload = new unloadForm();
    ObservableList<String> comboboxlistAccounttype = FXCollections.observableArrayList("Accounts Payable", "Accounts Receivable");
    ObservableList<String> comboboxlistTranstype = FXCollections.observableArrayList("Accreditation", "Black Listing");
    public int pnEditMode;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apAttachments;
    @FXML
    private Label lblSource, lblStatus1, lblStatus11, lblStatus;
    @FXML
    private TextField tfSearchCompany, tfTransactionNo, tfCategory, tfCompany, tfAddress, tfTIN, tfContactPerson, tfContactNo, tfContactEmail, tfContactRole, tfContactDepartment, tfContactPosition, tfAttachmentNo;
    @FXML
    private Button btnBrowse, btnCancel, btnHistory, btnClose, btnAddClompany, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private Tab tabDetails, tabAttachments;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private ComboBox cmbAccountType, cmbTransType, cmbAttachmentType;
    @FXML
    private FontAwesomeIconView faAdd;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblAttachments;
    @FXML
    private TableColumn tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
//        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
//        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
//        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poLogWrapper = new LogWrapper(pxeModuleName, pxeModuleName);
            poController = new ClientControllers(poApp, poLogWrapper).AccountAccreditation();

            initComboboxes();
            initAttachmentsGrid();
            initLoadTable();
            initAttachmentPreviewPane();
            Platform.runLater(() -> {
                poController.setRecordStatus("01234");
            });
            lblSource.setText(poController.getCompany());
            initControlEvents();
            loadRecordMaster();
            initTabPane();
            initTableOnClick();
            initButtonDisplay(EditMode.UNKNOWN);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Attachments":
                    JFXUtil.clearTextFields(apAttachments);
                    if (pnEditMode == EditMode.READY) {
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

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnBrowse":
                    if (!isJSONSuccess(poController.searchRecord(tfSearchCompany.getText(), false), "")) {
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    clearAllInputs();
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Account Information");
                    break;
                case "btnHistory":

                    if (poController.getEditMode() != EditMode.READY && poController.getEditMode() != EditMode.UPDATE) {
                        ShowMessageFX.Warning(null, pxeModuleName, "No transaction status history to load!");
                        return;
                    }

                    try {
                        poController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(npe));
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to block this transaction?")) {
                        JSONObject loJSON = new JSONObject();
                        loJSON = poController.BlockTransaction();
                        if (!JFXUtil.isJSONSuccess(loJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(loJSON));
                        }
                        loadRecordMaster();
                    }
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", pxeModuleName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(apMainAnchor, poApp, pxeModuleName);
                        } else {
                            ShowMessageFX.Warning(null, pxeModuleName, "Please notify the system administrator to configure the null value at the close button.");
                        }
                    }
                    break;
            }
            if (JFXUtil.isObjectEqualTo(btnID, "btnSave", "btnCancel", "btnVoid", "btnConfirm")) {
                poController.resetOthers();
                clearAllInputs();
                JFXUtil.clickTabByTitleText(tabPaneMain, "Account Information");
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(btnID, "btnSearch", "btnUndo", "btnArrowRight", "btnArrowLeft", "btnHistory", "btnPrint", "btnRemoveAttachment", "btnAddAttachment")) {
            } else {
                loadRecordMaster();
                loadTableAttachment.reload();
            }
            initButtonDisplay(poController.getEditMode());
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        CommonUtils.SetNextFocus(loTxtField);
                        event.consume();
                        break;
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchCompany":
                                if (!isJSONSuccess(poController.searchRecord(tfSearchCompany.getText(), false),
                                        "")) {
                                    return;
                                }
                                loadRecordMaster();
                                loadTableAttachment.reload();
                                JFXUtil.clickTabByTitleText(tabPaneMain, "Account Information");
                                initButtonDisplay(poController.getEditMode());
                                break;
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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
                });
    }

    private void loadRecordMaster() {
        try {
            boolean lbShow2 = !JFXUtil.isObjectEqualTo(poController.getModel().Client().getCompanyName(), null, "");
            JFXUtil.setDisabled(lbShow2, tfCompany);
            faAdd.setIcon(lbShow2 ? FontAwesomeIcon.PENCIL_SQUARE_ALT : FontAwesomeIcon.PLUS);
            JFXUtil.setStatusValue(lblStatus, AccountAccreditationStatus.class, poController.getModel().getEditMode() == EditMode.UNKNOWN ? "-1" : poController.getModel().getRecordStatus());

            tfTransactionNo.setText(poController.getModel().getTransactionNo() != null ? poController.getModel().getTransactionNo() : "");
            dpTransactionDate.setValue(ParseDate(poController.getModel().getDateTransact()));
            tfCategory.setText(poController.getModel().Category().getDescription());
            tfCompany.setText(poController.getModel().Client().getCompanyName());

            tfContactPerson.setText(poController.getModel().ClientInstitutionContact().getContactPersonName());
            tfContactRole.setText(poController.getModel().ClientInstitutionContact().ContactRole().getsRoleDesc());

            //set landline no (mobile no is empty), set fax no(landline no is empty), by default set mobile no
            String lsMobile = poController.getModel().ClientInstitutionContact().getMobileNo();
            String lsLandline = poController.getModel().ClientInstitutionContact().getLandlineNo();
            String lsFaxno = poController.getModel().ClientInstitutionContact().getFaxNo();

            tfContactNo.setText(lsMobile == null ? (lsLandline == null ? (lsFaxno == null ? "" : lsFaxno) : lsLandline) : lsMobile);
            tfContactEmail.setText(poController.getModel().ClientInstitutionContact().getMailAddress());
            tfContactDepartment.setText(poController.getModel().ClientInstitutionContact().getsDeprtmnt());
            tfContactPosition.setText(poController.getModel().ClientInstitutionContact().getContactPersonPosition());

            String lshouseno = poController.getModel().ClientAddress().getHouseNo() == null || poController.getModel().ClientAddress().getHouseNo().isEmpty() ? "" : poController.getModel().ClientAddress().getHouseNo() + " ";
            String lsaddress = poController.getModel().ClientAddress().getAddress() == null || poController.getModel().ClientAddress().getAddress().isEmpty() ? "" : poController.getModel().ClientAddress().getAddress();
            String lsbrgy = poController.getModel().ClientAddress().Barangay().getBarangayName() == null || poController.getModel().ClientAddress().Barangay().getBarangayName().isEmpty() ? "" : ", " + poController.getModel().ClientAddress().Barangay().getBarangayName();
            String lscity = poController.getModel().ClientAddress().Town().getDescription() == null || poController.getModel().ClientAddress().Town().getDescription().isEmpty() ? " " : ", " + poController.getModel().ClientAddress().Town().getDescription();
            String lsprovince = poController.getModel().ClientAddress().Town().Province().getDescription() == null || poController.getModel().ClientAddress().Town().Province().getDescription().isEmpty() ? " " : " " + poController.getModel().ClientAddress().Town().Province().getDescription();

            tfAddress.setText(lshouseno + lsaddress + lsbrgy + lscity + lsprovince);

            tfTIN.setText(poController.getModel().Client().getTaxIdNumber() == null ? "" : poController.getModel().Client().getTaxIdNumber());
            taRemarks.setText(poController.getModel().getRemarks());
            cmbAccountType.getSelectionModel().select(Integer.parseInt(poController.getModel().getAccountType()));
            cmbTransType.getSelectionModel().select(Integer.parseInt(poController.getModel().getTransactionType()));
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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
                cmbAttachmentType.getSelectionModel().select(lnAttachmentType);
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

    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(documentType, cmbAttachmentType));

        // ComboBox setup
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poController.TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (Exception e) {
                }
            }
        });
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAttachmentType);
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlistAccounttype, cmbAccountType), new JFXUtil.Pairs<>(comboboxlistTranstype, cmbTransType));
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAccountType, cmbTransType);
    }

    private void initButtonDisplay(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.READY);
        boolean lbShow2 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);

        JFXUtil.setButtonsVisibility(lbShow1, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow2, btnClose);

        JFXUtil.setDisabled(true, apMaster);
        JFXUtil.setButtonsVisibility(true, btnBrowse);
        JFXUtil.setButtonsVisibility(false, btnAddClompany, btnCancel);
        JFXUtil.setDisabled(true, cmbAttachmentType);
        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.getModel().getRecordStatus()) {
            case AccountAccreditationStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(true, btnCancel);
                break;
        }
    }

    private void initControlEvents() {
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
        clearAllInputs();
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

    private void initTableOnClick() {
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

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(pxeModuleName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, pxeModuleName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, pxeModuleName, message));
                }
            }
            return false;
        }

        if ("success".equalsIgnoreCase(result)) {
            poLogWrapper.info("Success on " + fsModule);
            return true;
        }
        // Unknown or null result
        poLogWrapper.warning("Unrecognized result: " + result);
        return false;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void clearAllInputs() {
        JFXUtil.clearTextFields(apMaster, apAttachments);
    }
}
