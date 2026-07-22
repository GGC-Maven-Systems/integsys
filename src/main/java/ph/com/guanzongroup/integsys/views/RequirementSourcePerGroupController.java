package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.RequirementsSourcePerGroup;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class RequirementSourcePerGroupController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Requirement Source Per Group";
    private int pnEditMode;
    private RequirementsSourcePerGroup oParameters;
    JSONObject poJSON = new JSONObject();
    private boolean pbLoaded = false;
    ObservableList<String> ClientType = ModelSalesInquiry_Detail.ClientType;
    ObservableList<String> cPaymentMode = ModelSalesInquiry_Detail.PurchaseType;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    @FXML
    private AnchorPane AnchorMain, apMaster, apSearch;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse,btnSearch,
            btnNew,
            btnSave,
            btnUpdate,
            btnCancel,
            btnActivate,
            btnClose;
    @FXML
    private FontAwesomeIconView faActivate;
    @FXML
    private TextField txtField01,
            txtField02,
            txtSeeks01;
    @FXML
    private CheckBox cbActive, cbRequired;
    @FXML
    private ComboBox cmbCustomerType, cmbPaymentMode;
  
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
        initializeObject();
        pnEditMode = oParameters.getEditMode();
        initButton(pnEditMode);
        InitTextFields();
        initComboBoxes();
        ClickButton();
        pbLoaded = true;
        JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        Platform.runLater(() -> {
            btnNew.fire();
        });
    }

    private void initializeObject() {
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        oParameters = new SalesControllers(oApp, logwrapr).RequirementsSourcePerGroup();
        oParameters.setWithUI(true);
        oParameters.setRecordStatus("0123");
    }

    private void ClickButton() {
        btnSearch.setOnAction(this::handleButtonAction);
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnActivate.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                unloadForm appUnload = new unloadForm();
                switch (clickedButton.getId()) {
                    case "btnClose":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearAllFields();
                        poJSON = oParameters.newRecord();
                        if ("success".equals((String) poJSON.get("result"))) {
                            txtField02.requestFocus();
                            pnEditMode = oParameters.getEditMode();
                            initButton(pnEditMode);
                            loadRecord();
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            txtSeeks01.clear();
                            break;
                        }
                        pnEditMode = oParameters.getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnSave":
                        poJSON = oParameters.saveRecord();
                        if ("success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            Platform.runLater(() -> {
                                btnNew.fire();
                            });
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apSearch);
                        break;
                    case "btnActivate":
                        String id = oParameters.getModel().getRequirementId();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to "+btnActivate.getText().toLowerCase()+" this Parameter?") == true) {
                            if(oParameters.getModel().isActive()){
                                poJSON = oParameters.DeactivateRecord();
                            } else {
                                poJSON = oParameters.ActivateRecord();
                            }
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                break;
                            }
                        }
                        
                        ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                        
                        poJSON = oParameters.openRecord(id);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            break;
                        }
                        
                        pnEditMode = oParameters.getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtSeeks01.clear();
        cbActive.setSelected(false);
        cbRequired.setSelected(false);
        JFXUtil.setCmbValue(cmbPaymentMode, -1);
        JFXUtil.setCmbValue(cmbCustomerType, -1);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.READY);
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        
        JFXUtil.setButtonsVisibility(!lbShow, btnNew,btnBrowse, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch,btnCancel,btnSave);
        JFXUtil.setButtonsVisibility(lbShow1, btnUpdate,btnActivate);
        
        //fields
        JFXUtil.setDisabled(!lbShow, txtField02, cmbCustomerType,cmbPaymentMode, cbRequired);
    }

    private void InitTextFields() {
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
        txtField02.setOnKeyPressed(this::txtSeeks_KeyPressed);
        JFXUtil.setCheckboxHoverCursor(cbActive, cbRequired);
    }

    private void txtSeeks_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 01:
                            poJSON = oParameters.searchRecord(lsValue, false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.getModel().RequirementSource().getDescription());
                            pnEditMode = oParameters.getEditMode();
                            initButton(pnEditMode);
                            loadRecord();
                            break;
                        case 02:
                            poJSON = oParameters.SearchRequirmentSource(lsValue, false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtField02.clear();
                                break;
                            }
                            loadRecord();
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
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

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
                    case 2:
                        if(lsValue.isEmpty()){
                            oParameters.getModel().setRequirementCode("");
                        }
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

    private void loadRecord() {
        try {
            txtField01.setText(oParameters.getModel().getRequirementId());
            txtField02.setText(oParameters.getModel().RequirementSource().getDescription());
            cbActive.setSelected(oParameters.getModel().isActive());
            cbRequired.setSelected(oParameters.getModel().isRequired());
            if(oParameters.getModel().isActive()){
                btnActivate.setText("Deactivate");
                faActivate.setGlyphName("CLOSE");
            } else {
                btnActivate.setText("Activate");
                faActivate.setGlyphName("CHECK");
                JFXUtil.setButtonsVisibility(false, btnUpdate);
            }
            
            JFXUtil.setCmbValue(cmbPaymentMode, !oParameters.getModel().getPaymentMode().equals("") ? Integer.valueOf(oParameters.getModel().getPaymentMode()) : -1);
            JFXUtil.setCmbValue(cmbCustomerType, !oParameters.getModel().getCustomerGroup().equals("") ? Integer.valueOf(oParameters.getModel().getCustomerGroup()) : -1);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Information(MiscUtil.getException(ex), "Computerized Acounting System", pxeModuleName);
        }
    }

    @FXML
    void checkBox_Clicked(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbRequired":
                    poJSON = oParameters.getModel().isRequired(cbRequired.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecord();
                    break;
            }
        }
        
    }
    
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbPaymentMode":
                        poJSON = oParameters.getModel().setPaymentMode(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecord();
                        break;
                    case "cmbCustomerType":
                        poJSON = oParameters.getModel().setCustomerGroup(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecord();
                        break;
                }
            }
    );

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(cPaymentMode, cmbPaymentMode), 
                                new JFXUtil.Pairs<>(ClientType, cmbCustomerType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbPaymentMode, cmbCustomerType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbPaymentMode, cmbCustomerType);

    }

}
