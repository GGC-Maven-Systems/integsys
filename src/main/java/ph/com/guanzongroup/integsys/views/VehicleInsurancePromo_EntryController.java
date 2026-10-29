/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TreeItem;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleInsurance;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;
import ph.com.guanzongroup.integsys.model.ModelVehicleInsurance_Detail;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehicleInsurancePromo_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static VehicleInsurance poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psVariantId = "";
    boolean pbEntered = false;
    private ObservableList<ModelVehicleInsurance_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelVehicleInsurance_Detail> filteredDataDetail;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    JFXUtil.ReloadableTableTask loadTableDetail;
    
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnApprove, btnVoid, btnHistory, btnPrint, btnClose;
    @FXML
    private TextField tfValidityID, tfValidityPeriod, tfInsuranceID, tfDescription, tfSRP, tf1stYear, tf2ndYear, tf3rdYear, tf4thYear, tf5thYear;
    @FXML
    private DatePicker dpValidFrom, dpValidTo;
    @FXML
    private CheckBox cbActive;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblNo, tblBrand, tblModel, tblVariant, tblColor, tblSRP, tbl1stYear, tbl2ndYear, tbl3rdYear, tbl4thYear, tbl5thYear;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new SalesControllers(oApp, null).VehicleInsurance();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initTextFields();
            initDatepickers();
            initDetailsGrid();
            initTableOnClick();
            clearTextFields();
            loadRecordDetail();
            loadTableDetail.reload();
            pnEditMode = poController.getEditMode();
            initButton(pnEditMode);
            Platform.runLater(() -> {
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
                btnNew.fire();
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
        psCategoryId = fsValue;
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
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        //Clear data
                        poController.Detail().clear();
                        clearTextFields();

                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.Master().getValidityId());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            clearTextFields();
                            poController.InitTransaction();
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
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.AddDetail();
                                loadTableDetail.reload();
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
                        poJSON = poController.SearchTransaction("",false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfValidityID.requestFocus();
                            return;
                        }
                        pnEditMode = poController.Master().getEditMode();
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
                    case "btnSearch":
                        break;
                    case "btnRemake":
                        String lsStatus = "void";
                        if (ValidityPeriodStatus.APPROVED.equals(poController.Master().getRecordStatus())) {
                            lsStatus = "cancel";
                        }
                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                "Are you sure you want to remake this transaction?"
                        //                                + "\nPlease note that the current transaction will be "+lsStatus+"."
                        ) == false) {
                            return;
                        }

                        if (ValidityPeriodStatus.OPEN.equals(poController.Master().getRecordStatus())
                                || ValidityPeriodStatus.APPROVED.equals(poController.Master().getRecordStatus())) {
                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                    "Do you want to " + lsStatus + " the current transaction?") == true) {
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
                            }
                        }

                        poJSON = poController.remake(poController.Master().getValidityId());
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnPrint":
                        poController.printTransaction();
                        return;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadRecordDetail();
                loadTableDetail.reload();
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

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;

                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case DELETE:
                    break;
                case F3:
                    break;
                case UP:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tf1stYear", "tf2ndYear", "tf3rdYear", "tf4thYear", "tf5thYear"}, (Runnable) () -> moveNext(true, true)},});
                    break;
                case DOWN:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tf1stYear", "tf2ndYear", "tf3rdYear", "tf4thYear", "tf5thYear"}, (Runnable) () -> moveNext(false, true)},});
                    break;
                default:
                    break;
            }
        } catch (ExceptionInInitializerError ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                int lnYear = 1;
                switch (lsID) {
                    case "tf2ndYear":
                        lnYear = 2;
                        break;
                    case "tf3rdYear":
                        lnYear = 3;
                        break;
                    case "tf4thYear":
                        lnYear = 4;
                        break;
                    case "tf5thYear":
                        lnYear = 5;
                        break;
                    default:
                        lnYear = 1;
                        break;
                }
                switch (lsID) {
                    case "tf1stYear":
                    case "tf2ndYear":
                    case "tf3rdYear":
                    case "tf4thYear":
                    case "tf5thYear":
                        String lsVariantId = poController.Detail(pnDetail).getVariantId();
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.setAmount(lsVariantId,lnYear,Double.parseDouble(lsValue)); //Need to identify kung anong row siya mag sset
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEntered) {
                            JFXUtil.runWithDelay(.6, () -> {
                                moveNext(false, true);
                                pbEntered = false;
                            });
                        }
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableDetail.reload();
                });
            });
    
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfValidityPeriod":
                        poJSON = poController.Master().setValidityDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                }
            });

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbActive":
                    if (poController.Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                        poController.Detail().remove(pnDetail);
                    } else {
                        poController.Detail(pnDetail).setRecordStatus(checkedBox.isSelected());
                    }
                    loadTableDetail.reload();
                    if (checkedBox.isSelected()) {
                        moveNext(false, false);
                    }
                    break;
            }
        }
    }

    private void loadRecordMaster() {
        String lsStat = "Void";
        if (ValidityPeriodStatus.APPROVED.equals(poController.Master().getRecordStatus())) {
            lsStat = "Cancel";
        }
        btnVoid.setText(lsStat);
        JFXUtil.setStatusValue(lblStatus, ValidityPeriodStatus.class, !JFXUtil.isObjectEqualTo(poController.Master().getRecordStatus(), null, "") ? poController.Master().getRecordStatus().toString() : "-1");
        tfValidityID.setText(poController.Master().getValidityId());
        String lsValidFrom = JFXUtil.formatDateToString(poController.Master().getFromDate());
        JFXUtil.setDateValue(dpValidFrom, JFXUtil.isObjectEqualTo(lsValidFrom, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidFrom, "yyyy-MM-dd"));
        String lsValidTo = JFXUtil.formatDateToString(poController.Master().getThruDate());
        JFXUtil.setDateValue(dpValidTo, JFXUtil.isObjectEqualTo(lsValidTo, "") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidTo, "yyyy-MM-dd"));
    }

    private void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            tfInsuranceID.setText(poController.Detail(pnDetail).getInsuranceId());
            tfDescription.setText(JFXUtil.concatStrings(poController.Detail(pnDetail).ModelVariant().Model().Brand().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().Model().getDescription(),
                    poController.Detail(pnDetail).ModelVariant().getDescription(),
//                    poController.Detail(pnDetail).ModelVariant().VariantInsurance().getTransmission(),
                    poController.Detail(pnDetail).ModelVariant().Color().getDescription()));
            tfSRP.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).ModelVariant().getSellingPrice(), false));
            String lsVariantId = poController.Detail(pnDetail).getVariantId();
            tf1stYear.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId , 1), false));
            tf2ndYear.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId , 2), false));
            tf3rdYear.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId , 3), false));
            tf4thYear.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId , 4), false));
            tf5thYear.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId , 5), false));
            
            cbActive.setSelected(poController.Detail(pnDetail).getRecordStatus());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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
                            LocalDate toDate = dpValidTo.getValue();
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

                            pbSuccess = false;
                            loadTableDetail.reload();
                            pbSuccess = true;
                            break;
                        case "dpValidTo":
                            LocalDate selectedToDate = dpValidTo.getValue();
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

                            pbSuccess = false;
                            loadTableDetail.reload();
                            pbSuccess = true;
                            break;
                        default:
                            break;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpValidFrom, dpValidTo);
        JFXUtil.setActionListener(datepicker_Action, dpValidFrom, dpValidTo);

        dpValidTo.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Lost focus
                if (JFXUtil.isObjectEqualTo(dpValidTo.getEditor().getText(), null, "")) {
                    if (pbSuccess) {
                        poJSON = poController.Master().setThruDate(null);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        dpValidTo.setValue(null);
                    }
                    pbSuccess = false;
                    loadTableDetail.reload();
                    pbSuccess = true;
                }
            }
        });
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
            @Override
            protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
                int newIndex = 0;
                switch (currentTableID) {
                    case "tblViewDetail":
                        newIndex = !isMovedDown ? Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex09())
                                : Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToNextRow(currentTable)).getIndex09());
                        if (!details_data.isEmpty()) {
                            pnDetail = newIndex;
                            loadRecordDetail();
                        }
                        break;
                }
            }
        };
    
    public void moveNext(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDetail.requestFocus();
            pnDetail = isUp ? Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex09())
                    : Integer.parseInt(filteredDataDetail.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex09());
        }
        loadRecordDetail();
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        String lsVariantId = poController.Detail(pnDetail).getVariantId();
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.getAmount(lsVariantId, 1), tf1stYear},}, tf1stYear); // default
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtDetail_Focus, apDetail);
        JFXUtil.setFocusListener(txtMaster_Focus, tfValidityPeriod);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apDetail);
        JFXUtil.setCommaFormatter(tfSRP, tf1stYear, tf2ndYear, tf3rdYear,tf4thYear,tf5thYear);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);
    }

    private String getData(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                null,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.populateVehicleList();
                            }
                            JFXUtil.disableAllHighlightByColor(tblViewDetail, "#FAA0A0", highlightedRowsMain);
                            details_data.clear();
                            String lsCurrentVariantId = null;
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                    continue;
                                }
                                String lsVariantId = String.valueOf(poController.Detail(lnCtr).ModelVariant().getDescription());

                                // Only parents get a row number; children are left blank
                                for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                    if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                        continue;
                                    }
                                    details_data.add(
                                            new ModelVehicleInsurance_Detail(String.valueOf(lnCtr + 1),
                                                    String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().Brand().getDescription()),
                                                    String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().getDescription()),
                                                    String.valueOf(poController.Detail(lnCtr).ModelVariant().getDescription()),
                                                    String.valueOf(poController.Detail(lnCtr).ModelVariant().Color().getDescription()),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).ModelVariant().getSellingPrice(), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId, 1), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId, 2), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId, 3), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId, 4), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(lsVariantId, 5), false)),
                                                    String.valueOf(lnCtr), //fixed column (this is not visible)
                                                    "",//starts ammortization column dynamic
                                                    "",
                                                    ""
                                            ));
                                        //store agein as a prepared and then will dynamically added in details_data
                                        if (!poController.Detail(lnCtr).getRecordStatus()) {
                                            JFXUtil.highlightByKey(tblViewDetail, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsMain);
                                        }
                                }

                            }
                            
                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewDetail.getSelectionModel().select(0);
                                    tblViewDetail.getFocusModel().focus(0);
                                    pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewDetail.getSelectionModel().select(pnDetail);
                                tblViewDetail.getFocusModel().focus(pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                }
        );

    }

    public static <T> TreeItem<T> getDetailRowFilter(TreeItem<T> root, int lnpn, int columnIndex) {
        if (root == null) {
            return null;
        }

        String getterName = String.format("getIndex%02d", columnIndex);

        try {
            for (TreeItem<T> child : root.getChildren()) {
                T model = child.getValue();

                if (model != null) {
                    String value = (String) model.getClass()
                            .getMethod(getterName)
                            .invoke(model);

                    if (String.valueOf(lnpn).equals(value)) {
                        return child;
                    }
                }

                // search the children of this node too
                TreeItem<T> found = getDetailRowFilter(child, lnpn, columnIndex);
                if (found != null) {
                    return found;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // not found
    }

    private void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblNo);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor);
        JFXUtil.setColumnRight(tblSRP, tbl1stYear, tbl2ndYear, tbl3rdYear, tbl4thYear, tbl5thYear);
    }
    
    public void clearTextFields() {
        JFXUtil.clearTextFields(apMaster, apDetail);
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTableOnClick() {
        tblViewDetail.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelVehicleInsurance_Detail selected = (ModelVehicleInsurance_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(filteredDataDetail.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex12());
                        pnDetail = lnRow;
                        loadRecordDetail();
                        moveNext(false, false);
                    }
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewDetail, item -> ((ModelVehicleInsurance_Detail) item).getIndex01(), highlightedRowsMain);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
        JFXUtil.setButtonsVisibility(lbShow1, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid, btnApprove, btnPrint);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail);
        
        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getRecordStatus()) {
            case ValidityPeriodStatus.OPEN:
                JFXUtil.setButtonsVisibility(false, btnPrint);
                break;
            case ValidityPeriodStatus.VOID:
            case ValidityPeriodStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove, btnVoid, btnPrint);
                break;
            case ValidityPeriodStatus.APPROVED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove);
                break;
        }
    }
}
