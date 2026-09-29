package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
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
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.services.ParamControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class ParticularController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Particular";
    private int pnEditMode;
    private CashflowControllers oParameters;
    private ParamControllers oParamControllers;
    private boolean state = false;
    private boolean pbLoaded = false;
    private String psPrimary = "";
    @FXML
    private AnchorPane AnchorMain, AnchorInputs;

    @FXML
    private Button btnBrowse,
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
            txtField03,
            txtSeeks01;

    @FXML
    private CheckBox cbField01;

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
            pnEditMode = oParameters.Particular().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            initTabAnchor();

            if (oParameters.Particular().getEditMode() == EditMode.ADDNEW) {
                initButton(pnEditMode);
                initTabAnchor();
                loadRecord();
            }
            pbLoaded = true;
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ParticularController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new CashflowControllers(oApp, logwrapr);
            oParamControllers = new ParamControllers(oApp, logwrapr);
            oParameters.Particular().setRecordStatus("0123");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ParticularController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ClickButton() {
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
                        if (ShowMessageFX.OkayCancel("Are you sure you want to close this Tab?", "Close Tab", null) == true) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearAllFields();
                        txtField02.requestFocus();
                        JSONObject poJSON = oParameters.Particular().newRecord();
                        pnEditMode = oParameters.Particular().getEditMode();
                        if ("success".equals((String) poJSON.get("result"))) {
                            pnEditMode = oParameters.Particular().getEditMode();
                            initButton(pnEditMode);
                            initTabAnchor();
                            loadRecord();
                        } else {
                            ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                            initTabAnchor();
                        }
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.Particular().searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                            txtSeeks01.clear();
                            break;
                        }
                        pnEditMode = EditMode.READY;

                        loadRecord();
                        initTabAnchor();
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.Particular().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                            break;
                        }
                        pnEditMode = oParameters.Particular().getEditMode();
                        initButton(pnEditMode);
                        initTabAnchor();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", null)) {
                            initializeObject();
                            pnEditMode = EditMode.UNKNOWN;
                            oParameters.Particular().initialize();
                            oParameters.Particular().setRecordStatus("0123");
                            initTabAnchor();
                        }
                        break;
                    case "btnSave":
                        oParameters.Particular().getModel().setModifyingId(oApp.getUserID());
                        oParameters.Particular().getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oParameters.Particular().saveRecord();
                        if ("success".equals((String) saveResult.get("result"))) {
                            ShowMessageFX.Information((String) saveResult.get("message"), pxeModuleName, null);
                            pnEditMode = EditMode.UNKNOWN;
                            oParameters.Particular().initialize();
                            oParameters.Particular().setRecordStatus("0123");
                        } else {
                            ShowMessageFX.Information((String) saveResult.get("message"), pxeModuleName, null);
                        }
                        break;
                    case "btnActivate":
                        String Status = oParameters.Particular().getModel().getRecordStatus();
                        String id = oParameters.Particular().getModel().getParticularID();
                        JSONObject poJsON;
                        if (oApp.getUserLevel() < UserRight.SYSADMIN) {
                            ShowMessageFX.Warning("User is not allowed to " + btnActivate.getText().toLowerCase() + " paramater.", "Computerized Acounting System", pxeModuleName);
                            return;
                        }
                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to activate this record?") == true) {
                                    poJsON = oParameters.Particular().activateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), pxeModuleName, null);
                                        break;
                                    } else {
                                        ShowMessageFX.Information("Record activated successfully", pxeModuleName, null);
                                    }
                                    poJsON = oParameters.Particular().openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), pxeModuleName, null);
                                        break;
                                    }
                                }
                                break;
                            case "1":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to deactivate this record?") == true) {
                                    poJsON = oParameters.Particular().deactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), pxeModuleName, null);
                                        break;
                                    } else {
                                        ShowMessageFX.Information("Record deactivated successfully", pxeModuleName, null);
                                    }
                                    poJsON = oParameters.Particular().openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), pxeModuleName, null);
                                        break;
                                    }
                                }
                                break;
                        }

                }
                clearAllFields();
                initButton(pnEditMode);
                loadRecord();
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(ParticularController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtField03.clear();

        cbField01.setSelected(false);
        txtSeeks01.clear();
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY);
        btnCancel.setVisible(lbShow);
        btnCancel.setManaged(lbShow);
        btnSave.setVisible(lbShow);
        btnSave.setManaged(lbShow);

        btnBrowse.setVisible(!lbShow);
        btnBrowse.setManaged(!lbShow);
        btnNew.setVisible(!lbShow);
        btnNew.setManaged(!lbShow);

        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate);
        JFXUtil.setDisabled(!lbShow, AnchorInputs);
    }

    private void InitTextFields() {
        JFXUtil.setFocusListener(txtField_Focus, txtField01, txtField02, txtField03);
        txtField03.setOnKeyPressed(this::txtField_KeyPressed);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
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
                    case 1:
                        oParameters.Particular().getModel().setParticularID(lsValue);
                        break;
                    case 2:
                        oParameters.Particular().getModel().setDescription(lsValue);
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
            poJson = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 03:
                            poJson = oParamControllers.AccountChart().searchRecordByIndustry(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.Particular().getModel().setAccountCode(oParamControllers.AccountChart().getModel().getAccountCode());
                            txtField03.setText((String) oParamControllers.AccountChart().getModel().getDescription());
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
            Logger.getLogger(ParticularController.class.getName()).log(Level.SEVERE, null, ex);
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
                            poJson = oParameters.Particular().searchRecord(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.Particular().getModel().getDescription());
                            pnEditMode = EditMode.READY;
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
            Logger.getLogger(ParticularController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecord() {
        try {
            boolean lbActive = oParameters.Particular().getModel().getRecordStatus() == "1";
            boolean lbShow = pnEditMode == EditMode.READY;
            JFXUtil.setButtonsVisibility(lbShow, btnActivate);

            psPrimary = oParameters.Particular().getModel().getParticularID();

            txtField01.setText(oParameters.Particular().getModel().getParticularID());
            txtField02.setText(oParameters.Particular().getModel().getDescription());
            txtField03.setText(oParameters.Particular().getModel().Account_Chart().getDescription());

            switch (oParameters.Particular().getModel().getRecordStatus()) {
                case "1":
                    btnActivate.setText("Deactivate");
                    faActivate.setGlyphName("CLOSE");
                    cbField01.setSelected(true);
                    break;
                case "0":
                    btnActivate.setText("Activate");
                    faActivate.setGlyphName("CHECK");
                    cbField01.setSelected(false);
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ParticularController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initTabAnchor() {
        if (AnchorInputs == null) {
            System.err.println("Error: AnchorInput is not initialized.");
            return;
        }
        boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        AnchorInputs.setDisable(!isEditable);
    }

    @FXML
    void cbField01_Clicked(MouseEvent event) {
        try {
            if (cbField01.isSelected()) {
                oParameters.Particular().getModel().setRecordStatus("1");
            } else {
                oParameters.Particular().getModel().setRecordStatus("0");
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ParticularController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
