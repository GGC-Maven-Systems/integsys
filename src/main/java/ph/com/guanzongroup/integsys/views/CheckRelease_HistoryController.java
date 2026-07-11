package ph.com.guanzongroup.integsys.views;


import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckReleaseStatus;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckRelease_HistoryController implements Initializable, ScreenInterface {
    
    private GRiderCAS poApp;
    private CashflowControllers poGLControllers;
    private String psFormName = "Payment Request";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    
    private int pnTblMainRow = -1;
    private int pnTblMain_Page = 50;
    private TextField activeField;
    private String prevPayee = "";
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    
    private int pnSelectedDetail = 0;
    private String psActiveField = "";
    
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    
    
    @FXML
    private CheckBox cbReverse,cbIsReceived;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apMaster, apDetail, apButton, apTransaction;

    @FXML
    private TextField tfSearchReceived,
            tfSearchTransNo,tfReceivedBy, tfTotal, tfPayee,
            tfBank, tfCheckAmount, tfCheckTransNo,
            tfCheckNo, tfNote,tfTransNo;

    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpCheckDate;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private Button btnClose, btnBrowse, btnPrint,btnHistory,btnPost;

    @FXML
    private TextArea taRemarks;
    
    @FXML
    private TableView<ModelTableDetail> tblViewDetails;
    
    @FXML
    private TableColumn<ModelTableDetail, String> tblColDetailNo, tblColDetailReference, tblColDetailPayee, tblColDetailBank,
            tblColDetailDate, tblColDetailCheckNo, tblColDetailCheckAmount;

    @FXML
    private TableView<ModelTableMain> tblViewMaster;

    @FXML
    private TableColumn<ModelTableMain, String> tblColNo, tblColTransNo,
            tblColTransDate, tblColCheckNo, tblColCheckAmount;
    
   
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
        ClearAll();
        initializeObject();
        initButtonsClickActions();
        initTableDetail();
        initTableOnClick();
        initCheckBox();
        initFields();
        
        initButton(EditMode.UNKNOWN);
    }
    

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            poGLControllers = new CashflowControllers(poApp, logwrapr);
            poGLControllers.CheckReleases().setTransactionStatus("0123456");
            poJSON = poGLControllers.CheckReleases().InitTransaction();

            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
            }
            poGLControllers.CheckReleases().Master().setIndustryId(psIndustryID);
            poGLControllers.CheckReleases().Master().setCompany(psCompanyID);
            lblSource.setText(poGLControllers.CheckReleases().Master().Company().getCompanyName() + " - " + poGLControllers.CheckReleases().Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckTransfer_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }
    }
    private void initButton(int fnEditMode) {

        btnPrint.setVisible(false);
        btnPrint.setManaged(false);
        btnPost.setVisible(false);
        btnPost.setManaged(false);
//       if (fnEditMode == EditMode.UNKNOWN){
////        btnPrint.setVisible(false);
////        btnPrint.setManaged(false);
//        btnPost.setVisible(false);
//        btnPost.setManaged(false);
////       }else if(fnEditMode == EditMode.READY){
////           btnPrint.setVisible(true);
////           btnPrint.setManaged(true);
//           btnPost.setVisible(true);
//           btnPost.setManaged(true);
//       }
    }
    private void ClearAll() {
        Arrays.asList(
                 tfSearchReceived,
                 tfSearchTransNo,
                 tfReceivedBy, 
                 tfTotal, 
                 tfPayee,
                 tfBank, 
                 tfCheckAmount, 
                 tfCheckTransNo,
                 tfCheckNo, 
                 tfNote,
                 tfTransNo
        ).forEach(TextField::clear);
        cbReverse.setSelected(false);
        dpTransactionDate.setValue(null);
        detail_data.clear();
        pnSelectedDetail = 0;
        psActiveField = "";
        taRemarks.clear();
        lblStatus.setText("UNKNOWN");
    }
    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnPrint,btnClose,btnHistory,btnPost);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }
    
    private void handleButtonAction(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(AnchorMain, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                    break;
                case "btnBrowse":
                    poJSON = poGLControllers.CheckReleases().SearchTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        ClearAll();
                        return;
                    }
                    ClearAll();
                    loadTableDetail();
                    LoadMaster();
                    LoadDetail();
                    initButton(EditMode.READY);
                    break;
                case "btnHistory":
                    if( poGLControllers.CheckReleases().Master().getTransactionNo() == null){
                        ShowMessageFX.Error("Unable to proceed. No transaction is currently loaded.", psFormName, null);
                        return;
                    }
                    
                    try {
                        poGLControllers.CheckReleases().ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", psFormName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                    }

                    break;
                /*This Block of Codes was disabled
                 * @Purpose : See Change br workshhet (Check Release - for clarification pa ang process kay sir rex)
                 * @Link    : https://docs.google.com/spreadsheets/d/13RYvX0UCRnsGeg9hozSY_EQyS128eBze/edit?gid=1782002725#gid=1782002725
                 * @Author  : TEEJEI
                 * @Date    : 07-03-2026
                 */
//                case "btnPrint":
//                   if(poGLControllers.CheckReleases().Master().getTransactionNo() != null ||
//                            poGLControllers.CheckReleases().Master().getTransactionNo().isEmpty()){
//
//                            poJSON = poGLControllers.CheckReleases().printTransaction();
//                            if (!"success".equals((String) poJSON.get("result"))) {
//                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
//                                return;
//                            }
//                   }
//
//                    break;

                  /*This Block of Codes was disabled
                  * @Purpose : See Change br workshhet (Check Release - for clarification pa ang process kay sir rex)
                  * @Link    : https://docs.google.com/spreadsheets/d/13RYvX0UCRnsGeg9hozSY_EQyS128eBze/edit?gid=1782002725#gid=1782002725
                  * @Author  : TEEJEI
                  * @Date    : 07-03-2026
                  */
//                case "btnPost":
//
//                    if (poGLControllers.CheckReleases().Master().getTransactionNo() == null
//                            || poGLControllers.CheckReleases().Master().getTransactionNo().isEmpty()) {
//
//                        ShowMessageFX.Warning("No transaction selected.", psFormName, null);
//                        return;
//                    }
//                    poJSON = poGLControllers.CheckReleases().OpenTransaction(poGLControllers.CheckReleases().Master().getTransactionNo());
//                            if (!"success".equals((String) poJSON.get("result"))) {
//                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
//                                return;
//                            }
//                            pnEditMode = poGLControllers.CheckReleases().getEditMode();
//                    if(poGLControllers.CheckReleases().Master().getTransactionStatus().equals(CheckTransferStatus.VOID)){
//                     ShowMessageFX.Warning(
//                                "Transaction already voided.\n Posting of this transaction is not allowed.",
//                                psFormName, null
//                        );
//
//                        return;
//                    }
//                     if(poGLControllers.CheckReleases().Master().getTransactionStatus().equals(CheckTransferStatus.POSTED)){
//                     ShowMessageFX.Warning(
//                                "Transaction already posted.\n Posting of this transaction is not allowed.",
//                                psFormName, null
//                        );
//
//                        return;
//                    }
//                    if(!poGLControllers.CheckReleases().Master().isPrintedStatus()){
//                     ShowMessageFX.Warning(
//                                "Posting is not allowed. Please print the transaction before proceeding.",
//                                psFormName, null
//                        );
//                        return;
//                    }
//
//                    if (!poGLControllers.CheckReleases().Master().getTransactionStatus().equals(CheckTransferStatus.CONFIRMED)) {
//                        ShowMessageFX.Warning(
//                                "Posting is not allowed. Please confirm  the transaction before proceeding.",
//                                psFormName, null
//                        );
//                        return;
//                    }
//
//                    poJSON = poGLControllers.CheckReleases().PostTransaction("");
//
//                    if (!"success".equals((String) poJSON.get("result"))) {
//                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
//                        return;
//                    }
//
//                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
//
//                    ClearAll();
//                    initializeObject();
//                    pnEditMode = poGLControllers.CheckReleases().getEditMode();
//                    break;
                

                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            initFields();
//            initFields(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(CheckRelease_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void initFields() {
        boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE ||pnEditMode == EditMode.READY);

        if (CheckTransferStatus.CONFIRMED.equals(poGLControllers.CheckReleases().Master().getTransactionStatus())) {
            apMaster.setDisable(true);
            apDetail.setDisable(false);
            JFXUtil.setDisabledExcept(true,
                    apDetail
                    
            );
        }
        tfTransNo.setDisable(true);
        if (CheckTransferStatus.OPEN.equals(poGLControllers.CheckReleases().Master().getTransactionStatus())
                || pnEditMode == EditMode.READY) {
            
            apMaster.setDisable(false);
            apDetail.setDisable(false);
        }
        
           List<TextField> loTxtField = Arrays.asList(tfCheckTransNo, tfCheckNo,tfSearchTransNo,tfSearchReceived);
            loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
//
            JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
            JFXUtil.setFocusListener(txtField_Focus, tfNote,tfReceivedBy);
            JFXUtil.setFocusListener(txtArea_Focus, taRemarks);

//            cmbAccountType.setItems(AccountType);
//            cmbAccountType.setOnAction(comboBoxActionListener);
//            JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAccountType);
//
//            tblDetails.setOnMouseClicked(this::tblDetails_Clicked);
//            makeClearableReadOnly(tfFieldName);

    }
    
    private void LoadMaster() {
//        try {
            tfTransNo.setText(poGLControllers.CheckReleases().Master().getTransactionNo());

            tfReceivedBy.setText(
                    poGLControllers.CheckReleases().Master().getReceivedBy()== null ? ""
                    : poGLControllers.CheckReleases().Master().getReceivedBy() );
            
            taRemarks.setText(poGLControllers.CheckReleases().Master().getRemarks() == null ? ""
                    : poGLControllers.CheckReleases().Master().getRemarks());
            
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(poGLControllers.CheckReleases().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            

            String lsStatus = "";
            switch (poGLControllers.CheckReleases().Master().getTransactionStatus()) {
                case CheckReleaseStatus.VOID:
                    lsStatus = "VOID";
                    break;
                case CheckReleaseStatus.OPEN:
                    lsStatus = "OPEN";
                    break;
                case CheckReleaseStatus.CONFIRMED:
                    lsStatus = "CONFIRMED";
                    break;
                case CheckReleaseStatus.RELEASED:
                    lsStatus = "RELEASED";
                    break;
                default:
                    lsStatus = "UNKNOWN";
                    break;
            }
            lblStatus.setText(lsStatus);
    }
    
    private void LoadDetail() {
        try {
            tfCheckTransNo.setText(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getTransactionNo()== null ? ""
                    : poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getTransactionNo());

            tfBank.setText(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().Banks().getBankName() == null ? ""
                    : poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().Banks().getBankName());

            tfPayee.setText(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().Payee().getPayeeName()== null ? ""
                    : poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().Payee().getPayeeName());
            
            
            tfCheckNo.setText(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getCheckNo() == null ? ""
                    : poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
            
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                    poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getAmount(), true));

            
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                    poGLControllers.CheckReleases().Master().getTransactionTotal(), true));
            
            dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getCheckDate(), 
                            SQLUtil.FORMAT_SHORT_DATE)));

            cbReverse.setSelected(
                poGLControllers.CheckReleases().Detail(pnSelectedDetail) != null
                && poGLControllers.CheckReleases().Detail(pnSelectedDetail).isReverse()
        );
            

        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    
    private void initTableDetail() {
        tblColDetailNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblColDetailReference.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblColDetailBank.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblColDetailPayee.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblColDetailDate.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblColDetailCheckNo.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblColDetailCheckAmount.setCellValueFactory(new PropertyValueFactory<>("index07"));

        tblViewDetails.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            Platform.runLater(() -> {
                TableHeaderRow header = (TableHeaderRow) tblViewDetails.lookup("TableHeaderRow");
                if (header != null) {
                    header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        header.setReordering(false);
                    });
                }
            });
        });
    }
    
    private void loadTableDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: transparent;");

        tblViewDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelTableDetail>> task = new Task<List<ModelTableDetail>>() {
            @Override
            protected List<ModelTableDetail> call() throws Exception {
                try {
                    int detailCount = poGLControllers.CheckReleases().getDetailCount();
                    int OriginalRow = 0;
                    List<ModelTableDetail> detailsList = new ArrayList<>();
                    for (int lnCtr = 0; lnCtr < poGLControllers.CheckReleases().getDetailCount(); lnCtr++) {
                        if (!poGLControllers.CheckReleases().Detail(lnCtr).isReverse()) {
                            continue;
                        }
                        OriginalRow += 1;
                        detailsList.add(new ModelTableDetail(
                                String.valueOf(OriginalRow),
                                poGLControllers.CheckReleases().Detail(lnCtr) != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().getTransactionNo() != null
                                ? poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().getTransactionNo()
                                : "",
                                poGLControllers.CheckReleases().Detail(lnCtr) != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().Banks() != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().Banks().getBankName() != null
                                ? poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().Banks().getBankName()
                                : "",
                                poGLControllers.CheckReleases().Detail(lnCtr) != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().Payee() != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().Payee().getPayeeName() != null
                                ? poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().Payee().getPayeeName()
                                : "",
                                poGLControllers.CheckReleases().Detail(lnCtr) != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().getCheckDate() != null
                                ? CustomCommonUtil.formatDateToMMDDYYYY(
                                        poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().getCheckDate()
                                )
                                : "",
                                poGLControllers.CheckReleases().Detail(lnCtr) != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().getCheckNo() != null
                                ? poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().getCheckNo()
                                : "",
                                CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.CheckReleases().Detail(lnCtr).CheckPayment().getAmount(),true),
                                        String.valueOf(lnCtr),"",""));
                    }
                    Platform.runLater(() -> {
                        detail_data.setAll(detailsList); // Properly update list
                        tblViewDetails.setItems(detail_data);
                        pnSelectedDetail = tblViewDetails.getItems().size() - 1;
                        tblViewDetails.getSelectionModel().clearAndSelect(pnSelectedDetail);
                        tblViewDetails.scrollTo(pnSelectedDetail);
                        LoadDetail();
//                        poJSON = poGLControllers.CheckReleases().computeMasterFields();
                    });
                    
                    return detailsList;
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(PaymentRequest_EntryController.class
                            .getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
            }
        };

        new Thread(task).start();
    }
    
    
    public void initTableOnClick() {
        tblViewDetails.setOnMouseClicked(this::tblViewDetails_Clicked);
    }
    
    private void initCheckBox() {
        cbReverse.setDisable(true);
        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            
            cbReverse.setOnAction(event -> {
                if (poGLControllers.CheckReleases().Master().getTransactionStatus().equals(CheckTransferStatus.OPEN)
                        || poGLControllers.CheckReleases().Master().getTransactionStatus().equals(CheckTransferStatus.CONFIRMED)) {
                    if (poGLControllers.CheckReleases().Detail(pnSelectedDetail).getSourceNo() != null
                            || !poGLControllers.CheckReleases().Detail(pnSelectedDetail).getSourceNo().isEmpty()) {
                        if (!cbReverse.isSelected()) {
                            poGLControllers.CheckReleases().Detail().remove(pnSelectedDetail);
                        }
                    }
                } else {
                     poGLControllers.CheckReleases().Detail(pnSelectedDetail).isReverse(cbReverse.isSelected());
                }
                
                loadTableDetail();
            });
            
        }
        
    }
    private void tblViewDetails_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnSelectedDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
            ModelTableDetail selectedItem = tblViewDetails.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                tfCheckTransNo.clear();
                tfBank.clear();
                tfPayee.clear();
                tfNote.clear();
                tfCheckNo.clear();
                dpCheckDate.setValue(null);
                cbReverse.setSelected(false);
//                cmbAccountType.getSelectionModel().clearSelection();

                if (selectedItem != null) {
                    if (pnSelectedDetail >= 0) {
                        LoadDetail();
                        tfCheckTransNo.requestFocus();
                    }
                }
            }
        }
    }
    
    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
        (lsID, lsValue) -> {

//            try {
                /* Lost Focus */
                switch (lsID) {
//                    
                    case "taRemarks":
                        poGLControllers.CheckReleases().Master().setRemarks(lsValue.trim());
                        break;
                    case "tfNote":
                        psActiveField = lsID;
//                        poGLControllers.CheckReleases().Detail(pnSelectedDetail).setRemarks(lsValue.trim());
                        break;
                    case "tfCheckNo":
                        psActiveField = lsID;
                        JFXUtil.inputIntegersOnly(tfCheckNo);
                        break;
                    case "tfReceivedBy":
                        poGLControllers.CheckReleases().Master().setReceivedBy(lsValue.trim());
                        break;
                }
        });
    
    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
        (lsID, lsValue) -> {

            /* Lost Focus */
            switch (lsID) {
                
                case "taRemarks":
                    poGLControllers.CheckReleases().Master().setRemarks(lsValue.trim());
                    break;
                    
            }
        });

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (lsTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = lsTxtField.getText();
        }
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        switch (txtFieldID) {
                            case "tfFilterBank":
//                                loadTableMaster();
                                break;
                        }
                        break;
                    case F3:
                        switch (txtFieldID) {
                            case "tfCheckTransNo":
                                poJSON = poGLControllers.CheckReleases().SearchChecks(lsValue, "",pnSelectedDetail,false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                    return; 
                                }
                                tfCheckTransNo.setText(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getTransactionNo());
                                loadTableDetail();
                                return;   
                            case "tfCheckNo":
                                poJSON = poGLControllers.CheckReleases().SearchChecks("", lsValue,pnSelectedDetail,false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                    return; 
                                }
                                tfCheckNo.setText(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
                                loadTableDetail();
                                return; 
                            case "tfSearchTransNo":
                                poJSON = poGLControllers.CheckReleases().SearchTransaction( lsValue,null);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                    return; 
                                }
                                tfCheckNo.setText(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
                                ClearAll();
                                loadTableDetail();
                                LoadMaster();
                                LoadDetail();
                                initButton(EditMode.READY);
                                return; 
                             case "tfSearchReceived": 
                                  poJSON = poGLControllers.CheckReleases().SearchTransaction( null,lsValue);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                    return; 
                                }
                                tfCheckNo.setText(poGLControllers.CheckReleases().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
                                ClearAll();
                                loadTableDetail();
                                LoadMaster();
                                LoadDetail();
                                initButton(EditMode.READY);
                                 return;
                        }

                        break;
                    case UP:
                        break;
                    case DOWN:
                        break;
                    default:
                        break;

                }
            } catch (SQLException | GuanzonException | ExceptionInInitializerError ex) {
                Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(CheckRelease_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
