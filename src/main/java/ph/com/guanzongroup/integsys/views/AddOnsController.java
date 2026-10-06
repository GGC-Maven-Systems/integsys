package ph.com.guanzongroup.integsys.views;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.AddOnType;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.AddOnTypeStatus;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class AddOnsController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static AddOnType poController;
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
    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnVoid, btnDeactivate, btnHistory, btnClose;
    @FXML
    private TextField tfAddOnID, tfDescription;
    @FXML
    private ComboBox cmbSource;
    @FXML
    private Label lblStatus;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList("Parts", "Labor", "Other Sources");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            poController = new SalesControllers(oApp, null).AddOnType();
            poController.initialize();
            poJSON = new JSONObject();
            initTextFields();
            initComboboxes();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            poController.setWithUI(true);
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
        System.out.println(fsValue);
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
                            tfAddOnID.requestFocus();
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
                        clearTextFields();

                        poJSON = poController.NewRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
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
                        poJSON = poController.ActivateRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getAddTypeCode());
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnVoid":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to void the record?") == false) {
                            return;
                        }
                        poJSON = poController.VoidRecord("");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poController.openRecord(poController.getModel().getAddTypeCode());
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
                        poController.openRecord(poController.getModel().getAddTypeCode());
                        pnEditMode = poController.getEditMode();
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

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDescription":
                        poJSON = poController.getModel().setAddTypeName(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbSource":
                        poJSON = poController.getModel().setSource(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfDescription);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster);
    }

    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlist, cmbSource));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbSource);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbSource);
    }

    private void txtField_KeyPressed(KeyEvent event) {
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
                break;
            default:
                break;
        }
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apMaster);
    }

    private void loadRecordMaster() {
        Platform.runLater(() -> {
            JFXUtil.setStatusValue(lblStatus, AddOnTypeStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.getModel().getRecordStatus());
        });
        tfAddOnID.setText(poController.getModel().getAddTypeCode());

        if (JFXUtil.isObjectEqualTo(poController.getModel().getSource(), "", null)) {
            poController.getModel().setSource("0");
            JFXUtil.setCmbValue(cmbSource, !poController.getModel().getSource().equals("") ? Integer.valueOf(poController.getModel().getSource()) : -1);
        } else {
            JFXUtil.setCmbValue(cmbSource, !poController.getModel().getSource().equals("") ? Integer.valueOf(poController.getModel().getSource()) : -1);
        }
        tfDescription.setText(poController.getModel().getAddTypeName());
    }

    private boolean isActive() {
        switch (poController.getModel().getRecordStatus()) {
            case AddOnTypeStatus.ACTIVE:
                return true;
        }
        return false;
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
        JFXUtil.setButtonsVisibility(false, btnActivate, btnVoid, btnDeactivate);
        JFXUtil.setDisabled(!lbShow, apMaster);
        if (fnValue != EditMode.READY) {
            return;
        }
        //enables disables visibility of buttons
        switch (poController.getModel().getRecordStatus()) {
            case AddOnTypeStatus.OPEN:
                JFXUtil.setButtonsVisibility(true, btnVoid, btnActivate);
                JFXUtil.setButtonsVisibility(false, btnDeactivate);
                break;
            case AddOnTypeStatus.ACTIVE:
                JFXUtil.setButtonsVisibility(true, btnDeactivate);
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnActivate, btnVoid);
                break;
            case AddOnTypeStatus.INACTIVE:
                JFXUtil.setButtonsVisibility(true, btnActivate);
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                JFXUtil.setButtonsVisibility(false, btnVoid, btnDeactivate);
                break;
            case AddOnTypeStatus.VOID:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                JFXUtil.setButtonsVisibility(false, btnVoid, btnDeactivate);
                JFXUtil.setButtonsVisibility(false, btnActivate);
                break;
        }
    }
}
