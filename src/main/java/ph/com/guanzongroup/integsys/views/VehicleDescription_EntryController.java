package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleDescription;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
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
    private String psCompanyId = "";
    private boolean pbEntered = false;
    private int pnDetail = 0;
    private boolean isForDialog = false;
    private boolean isForUpdate = false;
    private String lsStandardRateID = "";
    JFXUtil.StageManager stageParameter = new JFXUtil.StageManager();
    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster, apBrowse;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnDeactivate, btnHistory, btnClose, btnBrand, btnModel, btnColor;
    @FXML
    private Label lblStatus;
    @FXML
    private TextField tfVariantID, tfBrand, tfModel, tfColor, tfVariant, tfYearModel, tfAuthorizeCapacity, tfSearchVariant;
    @FXML
    private ComboBox cmbVehicleType, cmbBodyType, cmbTransmission;
    @FXML
    private CheckBox cbEndOfLife;

    ObservableList<String> comboboxlistTransmission = FXCollections.observableArrayList("Manual", "Automatic", "CVT", "DCT");
    ObservableList<String> comboboxlistVehicleType = FXCollections.observableArrayList("Private", "Commercial");
    ObservableList<String> comboboxlistBodyType = FXCollections.observableArrayList("Sedan", "SUV", "Hatchback", "MPV");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            poController = new SalesControllers(oApp, null).VehicleDescription();
            poController.initialize();
            poJSON = new JSONObject();
            initTextFields();
            clearTextFields();
            initComboboxes();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            poController.setWithUI(true);
            poController.setIndustryId(psIndustryId);
            Platform.runLater(() -> {
                btnNew.fire();
            });
            poController.setRecordStatus("01234");
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
        this.psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        //No category
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
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
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
                        poJSON = poController.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
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
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
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
                        break;
                    case "btnActivate":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to activate the record?") == false) {
                            return;
                        }
                        poJSON = poController.activateRecord();
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getVariantId());
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnDeactivate":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to deactivate the record?") == false) {
                            return;
                        }
                        poJSON = poController.DeactivateRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getVariantId());
                        pnEditMode = poController.getEditMode();
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
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                loadRecordMaster();
                initButton(pnEditMode);
            } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
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
        JFXUtil.clearTextFields(apMaster);
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
        JFXUtil.setButtonsVisibility(false, btnActivate,btnDeactivate);
        JFXUtil.setDisabledExcept(!lbShow, apMaster, btnBrand, btnModel, btnColor);
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
