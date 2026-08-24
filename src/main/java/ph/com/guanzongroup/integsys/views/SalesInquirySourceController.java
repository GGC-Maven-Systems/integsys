package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.integsys.model.ModelResultSet;

public class SalesInquirySourceController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private int pnEditMode;
    private ObservableList<ModelResultSet> data = FXCollections.observableArrayList();
    private JSONObject poJSON;
    static SalesControllers poInquirySourceController;

    @FXML
    private AnchorPane AnchorMain, apMaster, apBrowse, apMainAnchor;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnClose;
    @FXML
    private TextField tfInquiryID, tfDescription, tfSearchInquiry;

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
            poInquirySourceController = new SalesControllers(oApp, null);
            poJSON = new JSONObject();
            initTextFields();
            clearTextFields();

            pnEditMode = poInquirySourceController.SalesInquirySources().getEditMode();

            initButton(pnEditMode);

            Platform.runLater(() -> {
                loadRecordSearch();
                btnNew.fire();
            });
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchInquiry": {
                            poJSON = poInquirySourceController.SalesInquirySources().searchRecord(lsValue, false);
                        }
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfSearchInquiry.setText("");
                            break;
                        }
                        loadRecordMaster();
                        loadRecordSearch();
                        return;

                    }
                    break;
                default:
                    break;
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRecordSearch() {
        try {
            tfSearchInquiry.setText(poInquirySourceController.SalesInquirySources().getModel().getDescription());
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        JFXUtil.updateCaretPositions(apBrowse);
    }

    public void loadRecordMaster() {
        try {
            tfInquiryID.setText(poInquirySourceController.SalesInquirySources().getModel().getSourceId());
            tfDescription.setText(poInquirySourceController.SalesInquirySources().getModel().getDescription());
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    final ChangeListener<? super Boolean> txtMaster_Focus = (o, ov, nv) -> {
        try {
            poJSON = new JSONObject();
            TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
            String lsTxtFieldID = (txtPersonalInfo.getId());
            String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
            if (lsValue == null) {
                return;
            }
            if (!nv) {
                /*Lost Focus*/
                switch (lsTxtFieldID) {
                    case "tfDescription":
                        poInquirySourceController.SalesInquirySources().getModel().setDescription(lsValue);
                        loadRecordMaster();
                        break;
                    case "tfSearchInquiry":
                        break;

                }

            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    };

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfDescription, tfSearchInquiry);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poJSON = poInquirySourceController.SalesInquirySources().searchRecord("", true);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfInquiryID.requestFocus();
                            return;
                        }
                        pnEditMode = poInquirySourceController.SalesInquirySources().getEditMode();
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        //Clear data
                        poInquirySourceController.SalesInquirySources().initialize();
                        clearTextFields();
                        poJSON = poInquirySourceController.SalesInquirySources().newRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poInquirySourceController.SalesInquirySources().getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poInquirySourceController.SalesInquirySources().openRecord(poInquirySourceController.SalesInquirySources().getModel().getSourceId());
                        poJSON = poInquirySourceController.SalesInquirySources().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poInquirySourceController.SalesInquirySources().getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            poInquirySourceController.SalesInquirySources().initialize();
                            clearTextFields();
                            pnEditMode = EditMode.UNKNOWN;

                            break;
                        } else {
                            return;
                        }
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poInquirySourceController.SalesInquirySources().saveRecord();
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
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadRecordMaster();
                initButton(pnEditMode);

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void clearTextFields() {
        JFXUtil.clearTextFields(apMaster, apBrowse);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);
        JFXUtil.setDisabled(!lbShow, apMaster);
    }
}
