package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.cas.client.services.ClientControllers;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;

public class AccountChartController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private final String pxeModuleName = "Account Chart";
    private int pnEditMode;
    private CashflowControllers oParameters;
    private boolean state = false;
    private boolean pbLoaded = false;
    private String psPrimary = "";
    private String lbStat = "";
    @FXML
    private AnchorPane AnchorMain, AnchorInputs;

    @FXML
    private Button btnBrowse,
            btnNew,
            btnSave,
            btnUpdate,
            btnCancel,
            btnDeactivate,
            btnClose,
            btnConfirm,
            btnHistory,
            btnVoid;

    @FXML
    private FontAwesomeIconView faActivate;
    
    @FXML
    private Label lblStatus;

    @FXML
    private TextField txtField01,
            txtField02,
            txtField03,
            txtField04,
            txtField05,
            txtSeeks01;
    @FXML
    private TextArea txtArea01;

    @FXML
    private CheckBox cbField01;

    @FXML
    private ComboBox<String> cmbField01, cmbField02, cmbField03;

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
    }

    @Override
    public void setCompanyID(String fsValue) {
    }

    @Override
    public void setCategoryID(String fsValue) {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initializeObject();
            pnEditMode = oParameters.AccountChart().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            initComboBoxField();
            pbLoaded = true;
            btnNew.fire();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new CashflowControllers(oApp, logwrapr);
            oParameters.AccountChart().setRecordStatus("0123");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnDeactivate.setOnAction(this::handleButtonAction);
        btnVoid.setOnAction(this::handleButtonAction);
        btnConfirm.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
        btnHistory.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                unloadForm appUnload = new unloadForm();
                switch (clickedButton.getId()) {
                    case "btnClose":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", pxeModuleName, null)) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearAllFields();
                        txtField01.requestFocus();
                        poJSON = oParameters.AccountChart().newRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        pnEditMode = oParameters.AccountChart().getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.AccountChart().searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtSeeks01.clear();
                            return;
                        }
                        pnEditMode = oParameters.AccountChart().getEditMode();
                        loadRecord();
                        initButton(pnEditMode);
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.AccountChart().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            break;
                        }
                        pnEditMode = oParameters.AccountChart().getEditMode();
                        initButton(pnEditMode);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel editing this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode =  EditMode.READY;
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnSave":
                        oParameters.AccountChart().getModel().setModifyingId(oApp.getUserID());
                        oParameters.AccountChart().getModel().setModifiedDate(oApp.getServerDate());
                        poJSON = oParameters.AccountChart().saveRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                        btnNew.fire();
                        break;
                    case "btnVoid":
                        if (ShowMessageFX.YesNo("Are you sure you want to void this record?", pxeModuleName, null)) {
                            poJSON = oParameters.AccountChart().VoidRecord("");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                break;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            initializeObject();
                            pnEditMode = EditMode.READY;
                            clearAllFields();
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnConfirm":
                        if (ShowMessageFX.YesNo("Are you sure you want to confirm this record?", pxeModuleName, null)) {
                            poJSON = oParameters.AccountChart().ConfirmRecord("");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                clearAllFields();
                                break;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            initializeObject();
                            pnEditMode = EditMode.READY;
                            clearAllFields();
                            initButton(pnEditMode);
                        }
                        break;
                        
                    case "btnDeactivate":
                        if (ShowMessageFX.YesNo("Are you sure you want to confirm this record?", pxeModuleName, null)) {
                            poJSON = oParameters.AccountChart().DeactivateRecord("");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                                break;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                            initializeObject();
                            pnEditMode = EditMode.READY;
                            clearAllFields();
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnHistory":
                        if (oParameters.AccountChart().getModel().getAccountCode() == null) {
                            ShowMessageFX.Error("Unable to proceed. No record is currently loaded.", pxeModuleName, null);
                            return;
                        }
                            oParameters.AccountChart().ShowStatusHistory();
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException |ParseException ex) {
                Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
                 try {
                    if (oApp != null) {

                        oApp.rollbackTrans(); // 🔥 force rollback
                    }
                } catch (SQLException ex1) {
                    Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex1);
                }
               
            } catch (Exception ex) {
                Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
            }
        }
    }

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtField03.clear();
        txtField04.clear();
        txtField05.clear();
        txtArea01.clear();

        cmbField01.getSelectionModel().clearSelection();
        cmbField02.getSelectionModel().clearSelection();
        cmbField03.getSelectionModel().clearSelection();
        cbField01.setSelected(false);
        txtSeeks01.clear();
        lblStatus.setText("UNKNOWN");
    }

    private void initButton(int fnValue) {
        try {
            // First, hide and unmanage all buttons
            CustomCommonUtil.setVisible(false, btnSave, btnUpdate, btnVoid, btnDeactivate, btnCancel, btnConfirm,
                    btnBrowse, btnNew, btnClose,btnHistory);
            CustomCommonUtil.setManaged(false, btnSave, btnUpdate, btnVoid, btnDeactivate, btnCancel, btnConfirm,
                    btnBrowse, btnNew, btnClose,btnHistory);
            txtSeeks01.setDisable(false);
            AnchorInputs.setDisable(true);

            switch (fnValue) {
                case EditMode.ADDNEW:
                case EditMode.UPDATE:
                    // When adding or updating, only show Save and Cancel
                    CustomCommonUtil.setVisible(true, btnSave, btnCancel);
                    CustomCommonUtil.setManaged(true, btnSave, btnCancel);
                    txtSeeks01.setDisable(true);
                    AnchorInputs.setDisable(false);
                    break;

                case EditMode.READY:
                    txtSeeks01.setDisable(false);
                    AnchorInputs.setDisable(true);
                    
                    boolean projectExists = oParameters.AccountChart().getModel().getAccountCode()!= null
                            && !oParameters.AccountChart().getModel().getAccountCode().isEmpty();

                    if (!projectExists) {
                        // If no project selected, only show Browse, New, Close
                        CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnClose);
                        CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnClose);

                        CustomCommonUtil.setVisible(false, btnUpdate, btnConfirm, btnVoid, btnDeactivate);
                        CustomCommonUtil.setManaged(false, btnUpdate, btnConfirm, btnVoid, btnDeactivate);
                    } else {
                        // Project exists, show buttons based on status
                        String status = oParameters.AccountChart().getModel().getRecordStatus();
                        CustomCommonUtil.setVisible(true, btnHistory);
                        CustomCommonUtil.setManaged(true, btnHistory);
                        switch (status) {
                            case "0": // OPEN
                                CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnUpdate, btnConfirm, btnVoid, btnClose);
                                CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnUpdate, btnConfirm, btnVoid, btnClose);
                                break;
                            case "1": // CONFIRM
                                CustomCommonUtil.setVisible(true, btnBrowse, btnNew,  btnDeactivate, btnClose);
                                CustomCommonUtil.setManaged(true, btnBrowse, btnNew,  btnDeactivate, btnClose);
                                break;
                            case "3": // VOID
                            case "2": // CANCEL
                                CustomCommonUtil.setVisible(true, btnBrowse,btnNew, btnClose);
                                CustomCommonUtil.setManaged(true, btnBrowse,btnNew, btnClose);
                                break;
                            default:
                                // Fallback: show Browse, New, Close
                                CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnClose);
                                CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnClose);
                                break;
                        }
                    }
                    break;

                case EditMode.UNKNOWN:
                default:
                    // Default fallback: show only Browse and Close
                    CustomCommonUtil.setVisible(true, btnBrowse,btnNew, btnClose);
                    CustomCommonUtil.setManaged(true, btnBrowse,btnNew, btnClose);
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void InitTextFields() {
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtField03.focusedProperty().addListener(txtField_Focus);
        txtField04.focusedProperty().addListener(txtField_Focus);
        txtField05.focusedProperty().addListener(txtField_Focus);
        txtArea01.focusedProperty().addListener(txtArea_Focus);
        txtField03.setOnKeyPressed(this::txtField_KeyPressed);
        txtField04.setOnKeyPressed(this::txtField_KeyPressed);
        txtField05.setOnKeyPressed(this::txtField_KeyPressed);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
    }

    
    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        if (!pbLoaded) {
            return;
        }

        TextArea txtArea = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        int lnIndex = Integer.parseInt(txtArea.getId().substring(7, 9));
        String lsValue = txtArea.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            try {
                switch (lnIndex) {
                    case 01:
                        oParameters.AccountChart().getModel().setRemarks(lsValue);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        } else {
            txtArea.selectAll();
        }
    };
    
    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        if (!pbLoaded) {
            return;
        }

        TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            try {
                switch (lnIndex) {
                    case 1:
                        poJSON = oParameters.AccountChart().getModel().setAccountCode(lsValue);
                        if("error".equals((String)poJSON.get("result"))){
                            ShowMessageFX.Warning((String)poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                    case 2:
                        poJSON = oParameters.AccountChart().getModel().setDescription(lsValue);
                        if("error".equals((String)poJSON.get("result"))){
                            ShowMessageFX.Warning((String)poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                    case 4:
                        poJSON = oParameters.AccountChart().getModel().setParentAccountCode(lsValue);
                        if("error".equals((String)poJSON.get("result"))){
                            ShowMessageFX.Warning((String)poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
//                    case 5:
//                        oParameters.AccountChart().getModel().setAccountGroup(lsValue);
//                    case 6:
//                        oParameters.AccountChart().getModel().setReportGroup(lsValue);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        } else {
            txtField.selectAll();
        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJSON = new JSONObject();
            ClientControllers oAPClient;
            switch (event.getCode()) {
                case F3:

                    switch (lnIndex) {

                        case 03:
                            poJSON = oParameters.AccountChart().searchIndustry(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Information((String) poJSON.get("message"),  pxeModuleName,null);
                                return;
                            }
                            txtField03.setText(oParameters.AccountChart().getModel().Industry().getDescription());
                            break;
                        case 04:
                            poJSON = oParameters.AccountChart().searchParent(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Information((String) poJSON.get("message"),  pxeModuleName,null);
                                return;
                            }
                            txtField04.setText(oParameters.AccountChart().getModel().getParentAccountCode());
                            break;
                        case 05:
                            poJSON = oParameters.AccountChart().searchGLCode(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Information((String) poJSON.get("message"),  pxeModuleName,null);
                                return;
                            }
                            txtField05.setText(oParameters.AccountChart().getModel().General_Ledger().getDescription());
                            break;

                    }
                case ENTER:
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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txtSeeks_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJson = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 01:
                            poJson = oParameters.AccountChart().searchRecord(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.AccountChart().getModel().getDescription());
                            pnEditMode = oParameters.AccountChart().getEditMode();
                            loadRecord();
                            initButton(pnEditMode);
                            break;
                    }
                case ENTER:
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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecord() {
        try {
            boolean lbActive = oParameters.AccountChart().getModel().getRecordStatus() == "1";

            psPrimary = oParameters.AccountChart().getModel().getAccountCode();

            txtField01.setText(oParameters.AccountChart().getModel().getAccountCode());
            txtField02.setText(oParameters.AccountChart().getModel().getDescription());
            txtField03.setText(oParameters.AccountChart().getModel().Industry().getDescription());
            txtField04.setText(oParameters.AccountChart().getModel().getParentAccountCode() == null ? ""
                            : oParameters.AccountChart().getModel().getParentAccountCode());
            txtField05.setText(oParameters.AccountChart().getModel().General_Ledger().getGLCode());
            txtArea01.setText(oParameters.AccountChart().getModel().getRemarks());
            if (oParameters.AccountChart().getModel().getAccountType() != null && !oParameters.AccountChart().getModel().getAccountType().isEmpty()) {
                String acctType = oParameters.AccountChart().getModel().getAccountType();

                int lnIndex;
                switch (acctType) {
                    case "A":
                        lnIndex = 0; // Asset
                        break;
                    case "L":
                        lnIndex = 1; // Liability
                        break;
                    case "O":
                        lnIndex = 2; // Owner's Equity	
                        break;
                    case "R":
                        lnIndex = 3; // Revenue
                        break;
                    case "E":
                        lnIndex = 4; // Expenses
                        break;
                    default:
                        lnIndex = -1; // unknown
                        break;
                };

                if (lnIndex >= 0) {
                    cmbField01.getSelectionModel().select(lnIndex);
                }
            }
            
            if (oParameters.AccountChart().getModel().getBalanceType() != null && !oParameters.AccountChart().getModel().getBalanceType().isEmpty()) {
                String acctType = oParameters.AccountChart().getModel().getBalanceType();

                int lnIndex;
                switch (acctType) {
                    case "D":
                        lnIndex = 0; // Debit
                        break;
                    case "C":
                        lnIndex = 1; // Credit
                        break;
                    default:
                        lnIndex = -1;// unknown
                        break;
                };

                if (lnIndex >= 0) {
                    cmbField02.getSelectionModel().select(lnIndex);
                }
            }
            
            if (oParameters.AccountChart().getModel().getNature()!= null && !oParameters.AccountChart().getModel().getNature().isEmpty()) {
                String acctType = oParameters.AccountChart().getModel().getNature();

                int lnIndex;
                switch (acctType) {
                    case "P":
                        lnIndex = 0; // Permanent
                        break;
                    case "T":
                        lnIndex = 1; // Temporary
                        break;
                    case "A":
                        lnIndex = 2; // Adjustment	
                        break;
                    default:
                        lnIndex = -1;
                        break;
                };

                if (lnIndex >= 0) {
                    cmbField03.getSelectionModel().select(lnIndex);
                }
            }
            
            switch (oParameters.AccountChart().getModel().getRecordStatus()) {
                case "0":
                    lblStatus.setText("OPEN");
                    break;
                case "2":
                    lblStatus.setText("DEACTIVATED");
                    break;
                case "1":
                    lblStatus.setText("CONFIRM");
                    break;
                case "3":
                    lblStatus.setText("VOID");
                    break;
                default:
                    lblStatus.setText("UNKNOWN");
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cbField01_Clicked(MouseEvent event) {
        try {
            oParameters.AccountChart().getModel().isCash(cbField01.isSelected());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void initComboBoxField() {
        
        cmbField01.setItems(FXCollections.observableArrayList(
                "Asset",
                "Liability",
                "Owner's Equity",
                "Revenue",
                "Expenses"
        ));
        cmbField02.setItems(FXCollections.observableArrayList(
                "Debit",
                "Credit"
        ));
        cmbField03.setItems(FXCollections.observableArrayList(
                "Permanent",
                "Temporary",
                "Adjustment"
        ));

        cmbField01.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex != null && newIndex.intValue() >= 0) {
                try {
                    int lnIndex = newIndex.intValue(); // the selected index
                    String lsValue;
                    switch (lnIndex) {
                        case 0:
                            lsValue = "A";
                            break;
                        case 1:
                            lsValue = "L";
                            break;
                        case 2:
                            lsValue = "O";
                            break;
                        case 3:
                            lsValue = "R";
                            break;
                        case 4:
                            lsValue = "E";
                            break;
                        default:
                            lsValue = "";
                            break;

                    }
                    oParameters.AccountChart().getModel().setAccountType(lsValue);
                } catch (SQLException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GuanzonException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        cmbField02.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex != null && newIndex.intValue() >= 0) {
                try {
                    int lnIndex = newIndex.intValue(); // the selected index
                    String lsValue;
                    switch (lnIndex) {
                        case 0:
                            lsValue = "D";
                            break;
                        case 1:
                            lsValue = "C";
                            break;
                        default:
                            lsValue = "";
                            break;

                    }
                    oParameters.AccountChart().getModel().setBalanceType(lsValue);
                } catch (SQLException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GuanzonException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cmbField03.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex != null && newIndex.intValue() >= 0) {
                try {
                    int lnIndex = newIndex.intValue(); // the selected index
                    String lsValue;
                    switch (lnIndex) {
                        case 0:
                            lsValue = "P";
                            break;
                        case 1:
                            lsValue = "T";
                            break;
                        case 2:
                            lsValue = "A";
                            break;
                        default:
                            lsValue = "";
                            break;

                    }
                    oParameters.AccountChart().getModel().setNature(lsValue);
                } catch (SQLException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GuanzonException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

}
