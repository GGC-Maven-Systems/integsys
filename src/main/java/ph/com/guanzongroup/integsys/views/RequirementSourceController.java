package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
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
import ph.com.guanzongroup.cas.sales.t1.RequirementsSource;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class RequirementSourceController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static RequirementsSource poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private boolean pbEntered = false;
    @FXML
    private AnchorPane AnchorMain, apMaster, AnchorInputs, apBrowse;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnClose;
    @FXML
    private FontAwesomeIconView faActivate;
    @FXML
    private TextField tfRequirementSource, tfDescription, tfSearchDescription;
    @FXML
    private CheckBox cbActive;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            poJSON = new JSONObject();
            poController = new SalesControllers(oApp, null).RequirementsSource();
            poController.initialize(); // Initialize transaction
            poController.setRecordStatus("0123");

            initTextFields();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);

            Platform.runLater(() -> {
//                poController.getModel().setIndustryId(psIndustryId);
//                poController.getModel().setCompanyId(psCompanyId);
//                poController.setIndustryId(psIndustryId);
//                poController.setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
                btnNew.fire();
            });
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
        //Company is not autoset
    }

    @Override
    public void setCategoryID(String fsValue) {
        //No category
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = txtField.getId();
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchDescription":
                            poJSON = poController.searchRecord(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            }
                            loadRecordSearch();
                            loadRecordMaster();
                            pnEditMode = poController.getEditMode();
                            initButton(pnEditMode);
                            break;
                    }
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDescription":
                        poJSON = poController.getModel().setDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfSearchDescription":
                            if (lsValue.isEmpty()) {
                                if (!JFXUtil.isObjectEqualTo(pnEditMode, EditMode.ADDNEW, EditMode.UPDATE)) {
                                    poController.getModel().setDescription("");
                                    poController.initialize(); // Initialize transaction
                                    poController.setRecordStatus("0123");
                                }
                            }
                            break;
                    }
                    loadRecordSearch();
                    loadRecordMaster();
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfDescription);
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchDescription);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apBrowse);
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apBrowse, apMaster);
    }

    public void loadRecordSearch() {
        tfSearchDescription.setText(poController.getModel().getDescription());
        JFXUtil.updateCaretPositions(apBrowse);
    }

    public void loadRecordMaster() {
        if (poController.getModel().isActive()) {
            faActivate.setGlyphName("CLOSE");
            btnActivate.setText("Deactivate");
//            btnActivate.setGraphic(icon);
        } else {
            faActivate.setGlyphName("CHECK");
            btnActivate.setText("Activate");
        }
        tfRequirementSource.setText(poController.getModel().getRequirementCode());
        tfDescription.setText(poController.getModel().getDescription());
        cbActive.setSelected(poController.getModel().isActive());
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
//                        poController.setRecordStatus(RecordStatus.ACTIVE);
                        poJSON = poController.searchRecord("", pbEntered);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfRequirementSource.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnNew":
                        //Clear data
//                        poController.resetMaster();
                        clearTextFields();

                        poJSON = poController.newRecord();
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
                            //Clear data
                            poController.initialize(); // Initialize transaction
                            poController.setRecordStatus("0123");
                            clearTextFields();
//                            poController.getModel().setIndustryId(psIndustryId);
                            pnEditMode = EditMode.UNKNOWN;
                            break;
                        } else {
                            return;
                        }
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.saveRecord();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                btnNew.fire();
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnActivate":
                        //Validator
                        poJSON = new JSONObject();
                        String lsStat = "";
                        if (poController.getModel().isActive()) {
                            lsStat = "deactivate";
                            btnActivate.setText("Deactivate");
                        } else {
                            lsStat = "activate";
                            btnActivate.setText("Activate");
                        }
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsStat + " the transaction?") == true) {
                            if (poController.getModel().isActive()) {
                                poJSON = poController.DeactivateRecord();
                            } else {
                                poJSON = poController.ActivateRecord();
                            }
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, "Record " + lsStat + "d successfully");
                                poController.initialize(); // Initialize transaction
                                poController.setRecordStatus("0123");
                            }
                            pnEditMode = poController.getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    default:
                        break;
                }

                loadRecordMaster();
                initButton(pnEditMode);
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnActivate);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(lbShow3, apMaster);

        if (lbShow2) {
            JFXUtil.setButtonsVisibility(poController.getModel().isActive(), btnUpdate);
        }
    }
}
