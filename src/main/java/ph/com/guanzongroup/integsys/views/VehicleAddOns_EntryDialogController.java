/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleAddOns;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehicleAddOns_EntryDialogController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "Vehicle Add On Dialog");
    static VehicleAddOns poController;
    public int pnEditMode;
    JFXUtil.StageManager stageAddOn = new JFXUtil.StageManager();
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psVariantId = "";
    private String psAddOnTypeId = "";
    private String psAsOfDate = "";

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApprove, btnVoid, btnHistory, btnClose, btnDetailed;
    @FXML
    private TextField tfValidityID, tfAddOnType, tfDescription, tfSRP, tfAmount; //tfAddOnID
    @FXML
    private DatePicker dpValidFrom, dpTo;
    
    LocalDate ValidFrom;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehicleAddOns();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initTextFields();
            initDatepickers();
            clearTextFields();
            loadRecordDetail();

            Platform.runLater(() -> {
                poController.setWithUI(true);
                poController.Master().setCompanyId(psCompanyId);
                poController.setCompanyId(psCompanyId);
//            poController.setIndustryId(psIndustryId);
//            poController.setCategoryId(psCategoryId);
                loadRecordSearch();
                poJSON = poController.OpenAddOn(psVariantId, psAddOnTypeId,psAsOfDate);
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                
                pnDetail = poController.getRow(psVariantId, psAddOnTypeId);
                loadRecordMaster();
                pnEditMode = poController.getEditMode();
                initButton(pnEditMode);
            });
//            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
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
        psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
//        psCategoryId = fsValue;
    }
    public void setVariantId(String fsValue) {
        psVariantId = fsValue;
    }
    public void setAddOnTypeId(String fsValue) {
        psAddOnTypeId = fsValue;
    }
    public void setAsOfDate(String fsValue) {
        psAsOfDate = fsValue;
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
                    case "btnClose":
                        CommonUtils.closeStage(btnClose);
                        break;
                    case "btnNew":
                        //Clear data
                        poController.InitTransaction();
                        poController.setCompanyId(psCompanyId);
                        clearTextFields();
                        
                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        
                        poController.initFields();

                        poController.ReloadDetail();
                        poController.Detail(poController.getDetailCount() -1).setVariantId(psVariantId);
                        poController.Detail(poController.getDetailCount() -1).setAddOnType(psAddOnTypeId);
                        poController.Detail(poController.getDetailCount() -1).setSRPAmount(poController.Detail(poController.getDetailCount() -1).ModelVariant().getSellingPrice());
                        poController.Master().setValidityDescription(poController.Detail(poController.getDetailCount() -1).AddOnType().getAddTypeName());
                        
                        pnDetail = poController.getRow(psVariantId, psAddOnTypeId);
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        ValidFrom = CustomCommonUtil.parseDateStringToLocalDate(CustomCommonUtil.formatDateToShortString(poController.Master().getFromDate()));
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            poController.InitTransaction();
                            clearTextFields();
                            poController.Master().setCompanyId(psCompanyId);
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
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?") == true) {
                            if(poController.Detail(pnDetail).getAmount() <= 0.0000){
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid add on amount.");
                                return;
                            }
                            
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnBrowse":
                        poController.Master().setRecordStatus("01234");
                        poJSON = poController.SearchTransaction("", true);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfValidityID.requestFocus();
                            return;
                        }
                        pnEditMode = poController.Master().getEditMode();
                        poController.populateVehicleList();
                        break;
                    case "btnApprove":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to approve transaction?") == true) {
                            poJSON = poController.ApproveTransaction();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        String lsStat = "void";
                        if (ValidityPeriodStatus.APPROVED.equals(poController.Master().getRecordStatus())) {
                            lsStat = "cancel";
                        }
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsStat + " transaction?") == true) {
                            if (ValidityPeriodStatus.OPEN.equals(poController.Master().getRecordStatus())) {
                                poJSON = poController.VoidTransaction();
                            } else {
                                poJSON = poController.CancelTransaction();
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                btnCancel.fire();
                            }
                            pnEditMode = poController.Master().getEditMode();
                        } else {
                            return;
                        }
                        break;
                    case "btnDetailed":
                        openParameter(poController.Master().getValidityId());
                        return;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }
                
                loadRecordMaster();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } catch (ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    private void openParameter(String fsValidId) {
        if (fsValidId == null || "".equals(fsValidId)) {
            ShowMessageFX.Warning(null, pxeModuleName, "No valid id, unable to display dialog!");
            return;
        }
        String lsFXML = "";
        Object controller = new Object();
        VehicleAddOns_EntryController controller1 = new VehicleAddOns_EntryController(); //Should differ the parameter calling
        lsFXML = "/ph/com/guanzongroup/integsys/views/VehicleAddOns_Entry.fxml";

        controller1.setGRider(oApp);
        controller1.setIndustryID(psIndustryId);
        controller1.setCompanyID(psCompanyId);
        controller1.setValidityID(fsValidId);
        controller = controller1;

        poJSON = new JSONObject();
        if (stageAddOn != null) {
            stageAddOn.closeDialog();
            stageAddOn = new JFXUtil.StageManager();
        } else {
            stageAddOn = new JFXUtil.StageManager();
        }
        try {
            stageAddOn.setOnHidden(event -> {
                stageAddOn = null;
            });
            stageAddOn.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource(lsFXML), controller, "Vehicle Add Ons Dialog", true, false, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }


    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                
                JFXUtil.runWithDelay(.5, () -> {
                    loadRecordMaster();
                });
            });

    private void loadRecordMaster() {
        Platform.runLater(() -> {
            lblStatus.setText(poController.getStatus(poController.Master().getRecordStatus()).toUpperCase());

            String lsStat = "Void";
            if (ValidityPeriodStatus.APPROVED.equals(poController.Master().getRecordStatus())) {
                lsStat = "Cancel";
            }
            btnVoid.setText(lsStat);
        });

        tfValidityID.setText(poController.Master().getValidityId());
        String lsValidFrom = JFXUtil.formatDateToString(poController.Master().getFromDate());
        JFXUtil.setDateValue(dpValidFrom, JFXUtil.isObjectEqualTo(lsValidFrom, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidFrom, "yyyy-MM-dd"));
        String lsTo = JFXUtil.formatDateToString(poController.Master().getThruDate());
        JFXUtil.setDateValue(dpTo, JFXUtil.isObjectEqualTo(lsTo, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsTo, "yyyy-MM-dd"));
        tfAddOnType.setText(poController.Master().getValidityDescription());
        
        loadRecordDetail();
    }

    private void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            boolean lbisActive = poController.Detail(pnDetail).getRecordStatus();
            JFXUtil.setDisabled(!lbisActive, tfAmount);

//            tfAddOnID.setText(poController.Detail(pnDetail).getAddOnId());
            tfDescription.setText(JFXUtil.concatStrings(poController.Detail(pnDetail).ModelVariant().Model().Brand().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().Model().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().Color().getDescription()));
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getSRPAmount().doubleValue(), false));
//            cbActive.setSelected(poController.Detail(pnDetail).getRecordStatus());
            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount().doubleValue(), false));
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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
                case "cbActive": // this is the id
                    poJSON = poController.Detail(pnDetail).setRecordStatus(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
            }
        }
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                try {
                    poJSON = new JSONObject();
                    JFXUtil.setJSONSuccess(poJSON, "success");
                    LocalDate dateNow = LocalDate.now();
                    String inputText = datePicker.getEditor().getText();
                    if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                        return;
                    }
                    switch (datePicker.getId()) {
                        case "dpValidFrom":
                            LocalDate selectedFromDate = dpValidFrom.getValue();
                            LocalDate toDate = dpTo.getValue();
                            LocalDate currentDate = oApp.getServerDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            LocalDate firstDayOfCurrentMonth = currentDate.withDayOfMonth(1);
                            if (selectedFromDate != null && selectedFromDate.isBefore(firstDayOfCurrentMonth)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be before the current month.");
                                dpValidFrom.setValue(firstDayOfCurrentMonth);
                                return;
                            }
                            if (toDate != null && selectedFromDate.isAfter(toDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                                loadRecordMaster();
                                return;
                            }
                            if (pbSuccess) {
                                poController.Master().setFromDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            poJSON = poController.populateVehicleList();
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                        case "dpTo":
                            LocalDate selectedToDate = dpTo.getValue();
                            LocalDate fromDate = dpValidFrom.getValue();
                            if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                                loadRecordMaster();
                                return;
                            }
                            if (pbSuccess) {
                                poController.Master().setThruDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            poJSON = poController.populateVehicleList();
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                        default:
                            break;
                    }
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpValidFrom, dpTo);
        JFXUtil.setActionListener(datepicker_Action, dpValidFrom, dpTo);

        dpTo.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Lost focus
                if (JFXUtil.isObjectEqualTo(dpTo.getEditor().getText(), null, "")) {
                    try {
                        if (pbSuccess) {
                            poJSON = poController.Master().setThruDate(null);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            dpTo.setValue(null);
                        }
                        poJSON = poController.populateVehicleList();
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                }
            }
        });
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtDetail_Focus, apMaster);
        JFXUtil.setCommaFormatter(tfAmount);
    }

    private String getData(String lsValue) {
        return JFXUtil.isObjectEqualTo(lsValue, null, "") ? "" : lsValue;
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apMaster);
    }
    
    public void loadRecordSearch() {
//        try {
//            lblSource.setText(poController.Master().Company().getCompanyName());
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        }
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);
        JFXUtil.setButtonsVisibility(false, btnBrowse, btnDetailed);
        JFXUtil.setDisabled(!lbShow1, apMaster);
        JFXUtil.setButtonsVisibility(lbShow2, btnApprove);

        if (fnValue != EditMode.READY) {
            return;
        }
        
        poJSON = poController.isSingleEntry();
        JFXUtil.setButtonsVisibility(!"success".equals((String) poJSON.get("result")), btnDetailed);
        switch (poController.Master().getRecordStatus()) {
            case ValidityPeriodStatus.OPEN:
                break;
            case ValidityPeriodStatus.VOID:
            case ValidityPeriodStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove, btnVoid);
                break;
            case ValidityPeriodStatus.APPROVED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove);
                break;
        }
    }
}
