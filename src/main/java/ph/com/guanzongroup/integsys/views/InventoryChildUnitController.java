package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.InventoryChildUnit;
import org.guanzon.cas.parameter.model.Model_Inventory_Child_Unit;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.integsys.model.ModelInventoryChildUnit;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class InventoryChildUnitController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static InventoryChildUnit poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private boolean pbEntered = false;
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<Model_Inventory_Child_Unit> checkedItems = new ArrayList<>();

    private ObservableList<ModelInventoryChildUnit> main_data = FXCollections.observableArrayList();
    JFXUtil.ReloadableTableTask loadTableMain;
    private int pnMain = 0;

    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster, apBrowse;
    @FXML
    private Button btnHistory, btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnDisapprove, btnDeactivate, btnClose;
    @FXML
    private TextField tfStockID, tfBarcode, tfConversion, tfDescription, tfMeasure, tfSearchStock;
    @FXML
    private Label lblStatus, lblSource1;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblDetailRow1, tblDetailRow, tblDetailMeasure, tblDetailConversion, tblDetailQtyConvert, tblDetailStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            poJSON = new JSONObject();
            poController = new ParamControllers(oApp, null).InventoryChildUnit();
            poController.initialize();// Initialize transaction
//            poController.setRecordStatus("0123");

            initTextFields();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            initLoadTable();
            initTableOnClick();
            initMainGrid();
            initCheckboxes();
            Platform.runLater(() -> {
//                poController.setIndustryID(psIndustryId);
//                poController.setCompanyID(psCompanyId);
//                poController.setIndustryId(psIndustryId);
//                poController.setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
                poController.setRecordStatus("0134");
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
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        //No category
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
                    case "btnHistory":
                        if (poController.Detail(pnMain).getEditMode() != EditMode.READY && poController.Detail(pnMain).getEditMode() != EditMode.UPDATE) {
                            ShowMessageFX.Warning("No parameter status history to load!", pxeModuleName, null);
                            return;
                        }

                        try {
                            poController.ShowStatusHistory(pnMain);
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No parameter status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnBrowse":
                        poController.setRecordStatus("0134");
                        poJSON = poController.searchRecord("", false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        } else {
                            poJSON = poController.populateDetail();
                            if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            }
                        }
                        if (!checkedItem.isEmpty()) {
                            checkedItem.clear();
                        }
                        main_data.clear();
                        JFXUtil.clearTextFields(apMaster);
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
                        poController.initialize();
                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.UpdateTransaction();
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

                            poController.Master().setIndustryCode(psIndustryId);
                            pnEditMode = EditMode.UNKNOWN;

                            break;
                        } else {
                            return;
                        }
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                loadTableMain.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                loadRecordMaster();
                                btnNew.fire();
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnDisapprove":
                    case "btnDeactivate":
                    case "btnActivate":
                        processAction(lsButton);
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadRecordMaster();
                loadTableMain.reload();
                initButton(pnEditMode);
                JFXUtil.runWithDelay(.5, () -> {
                    if (JFXUtil.isObjectEqualTo(lsButton, "btnUpdate", "btnNew")) {
                        moveNext(false, false);
                    }
                });
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void processAction(String action) {
        try {
            String lsMessage = "";
            switch (action) {
                case "btnActivate":
                    lsMessage = "activate";
                    break;
                case "btnDeactivate":
                    lsMessage = "deactivate";
                    break;
                case "btnDisapprove":
                    lsMessage = "disapprove";
                    break;
                default:
                    break;
            }
            if (checkedItem.stream().anyMatch("1"::equals)) {
            } else {
                ShowMessageFX.Warning(null, pxeModuleName, "No items were selected to " + lsMessage + ".");
                return;
            }

            if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + lsMessage + " selected item/s?")) {
                return;
            }
            checkedItems.clear();
            List<String> list = new ArrayList<>();
            boolean lbAllSame = true;
            for (Object item : tblViewMainList.getItems()) {
                ModelInventoryChildUnit item1 = (ModelInventoryChildUnit) item;
                String lschecked = item1.getIndex01();
                int lnReference = Integer.valueOf(item1.getIndex02()) - 1;
                if (lschecked.equals("1")) {
                    list.add(item1.getIndex06());
                    checkedItems.add(poController.Detail(lnReference));
                    System.out.println("check items : " + checkedItems.get(checkedItems.size() - 1));
                }
            }
            if (!list.isEmpty()) {
                String first = list.get(0);

                for (String value : list) {
                    if (!first.equals(value)) {
                        lbAllSame = false;
                        break;
                    }
                }
            }
            if (lbAllSame) {
            } else {
                ShowMessageFX.Warning(null, pxeModuleName, "Ensure all selected items are similar.");
                return;
            }
            if (checkedItems.isEmpty()) {
                return;
            }
            switch (action) {
                case "btnActivate":
                    poJSON = poController.Activate(checkedItems);
                    break;
                case "btnDeactivate":
                    poJSON = poController.Deactivate(checkedItems);
                    break;
                case "btnDisapprove":
                    poJSON = poController.Disapprove(checkedItems);
                    break;
                default:
                    break;
            }
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            } else {
                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
            }
            if (!checkedItem.isEmpty()) {
                checkedItem.clear();
            }
            poController.populateDetail();
            pnEditMode = poController.getEditMode();
        } catch (SQLException | GuanzonException | ParseException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelInventoryChildUnit.class, tblViewMainList, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 0:
                            checkedItem.set(rowIndex, lbisTrue ? "1" : "0");
                            //set external temporary data of index to save as reference
                            // if detected unchecked then must update
                            pnMain = rowIndex;
                            Platform.runLater(() -> {
                                loadTableMain.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    if (lbisTrue) {
                                        JFXUtil.selectAndFocusRow(tblViewMainList, rowIndex);
                                    }
                                });
                            });
                            break;
                    }
                }, 0);//starts 0,1,2 
    }

    private void checkedItems(int lnCtr) {
        try {
            if (checkedItem.get(lnCtr) == null) {
                checkedItem.add("0");
            }
        } catch (Exception e) {
            checkedItem.add("0");
        }
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            main_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                lnRowCount += 1;
                                checkedItems(lnCtr);
                                main_data.add(
                                        new ModelInventoryChildUnit(checkedItem.get(lnCtr),
                                                String.valueOf(lnRowCount),
                                                poController.Detail(lnCtr).Measure().getDescription(),
                                                poController.Detail(lnCtr).UnitConversion().ConvertTo().getDescription(),
                                                String.valueOf(poController.Detail(lnCtr).UnitConversion().getQuantityConverted()),
                                                poController.getStatus(poController.Detail(lnCtr).getRecordStatus())
                                        ));
                            }
                            if (pnMain < 0 || pnMain
                                    >= main_data.size()) {
                                if (!main_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewMainList, 0);
                                    int lnRow = 0;
                                    pnMain = lnRow;
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnMainBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewMainList, pnMain);
                            }
                            loadRecordMaster();
                        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    public void loadRecordSearch() {
//        try {
//        tfSearchStock.setText(poController.Master().Payee().getPayeeName());
//        JFXUtil.updateCaretPositions(apBrowse);
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
//        }
    }

    public void loadRecordMaster() {
        try {
            lblStatus.setText("UNKNOWN");
            if (pnEditMode == EditMode.READY) {
                disableRowCheckbox.set(main_data.isEmpty()); // set enable/disable in checkboxes in requirements
            } else {
                disableRowCheckbox.set(true); // set enable/disable in checkboxes in requirements
            }
            if (pnMain < 0 || pnMain > poController.getDetailCount() - 1) {
                return;
            }
            if (pnEditMode == EditMode.UNKNOWN || pnEditMode == EditMode.READY) {
                JFXUtil.setDisabled(true, apMaster);
            } else {
                boolean lbShow = poController.Detail(pnMain).getEditMode() == EditMode.UPDATE;
                JFXUtil.setDisabled(lbShow, apMaster);
                boolean lbShow2 = pnEditMode == EditMode.UPDATE;
                JFXUtil.setDisabled(lbShow2, tfBarcode, tfDescription);
                boolean lbShow3 = poController.Detail(pnMain).getEditMode() == EditMode.UPDATE || poController.Detail(pnMain).getEditMode() == EditMode.READY;
                JFXUtil.setButtonsVisibility(lbShow3, btnHistory);
            }

            lblStatus.setText(poController.getStatus(poController.Detail(pnMain).getRecordStatus()));
            tfStockID.setText(poController.Detail(pnMain).Inventory().getStockId());
            tfBarcode.setText(poController.Detail(pnMain).Inventory().getBarCode());
            tfDescription.setText(poController.Detail(pnMain).Inventory().getDescription());
            tfMeasure.setText(poController.Detail(pnMain).Measure().getDescription());
            tfConversion.setText(poController.Detail(pnMain).UnitConversion().ConvertTo().getDescription());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    boolean lbProceed = true;
    boolean pbKeyPressed = false;

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
                        case "tfSearchStock":
                            poController.setRecordStatus("0134");
                            poJSON = poController.searchRecord(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                poJSON = poController.populateDetail();
                                if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                }
                                if (!checkedItem.isEmpty()) {
                                    checkedItem.clear();
                                }
                                main_data.clear();
                                JFXUtil.clearTextFields(apMaster);
                                pnEditMode = poController.getEditMode();
                                initButton(pnEditMode);
                            }
                            break;
                        case "tfBarcode":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.getDetailCount() > 1) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the inventory?\nPlease note that this action will reset all details.\n\nDo you wish to proceed?") == true) {
                                        btnNew.fire();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            lbProceed = false;

                            poJSON = poController.SearchInventory(lsValue, true);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfMeasure);
                            }
                            break;
                        case "tfDescription":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.getDetailCount() > 1) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the inventory?\nPlease note that this action will reset all details.\n\nDo you wish to proceed?") == true) {
                                        btnNew.fire();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }
                            lbProceed = false;
                            poJSON = poController.SearchInventory(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                        case "tfMeasure":
                            poJSON = poController.SearchMeasure(lsValue, false, pnMain);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfConversion);
                            }
                            break;
                        case "tfConversion":
                            poJSON = poController.SearchConversion(lsValue, false, pnMain);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            }
                            break;
                    }
                    loadTableMain.reload();
                    break;
                case UP:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfBarcode", "tfDescription", "tfMeasure", "tfConversion"}, (Runnable) () -> moveNext(true, true)}
                    });
                    event.consume();
                    break;
                case DOWN:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfBarcode", "tfDescription", "tfMeasure", "tfConversion"}, (Runnable) () -> moveNext(false, true)}
                    });
                    event.consume();
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchStock":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnMain).setStockId("");
                        }
                        break;
                }
                loadTableMain.reload();
            });
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfBarcode":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Master().getStockId(), null, "") && lbProceed) {
                                    if (poController.getDetailCount() > 1) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the inventory?\nPlease note that this action will reset all details.\n\nDo you wish to proceed?") == true) {
                                                btnNew.fire();
                                            } else {
                                                loadRecordMaster();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }
                            if (lbProceed) { // uniquely inserted due to retrieval delay
                                poController.Detail(pnMain).setStockId("");
                                loadRecordMaster();
                            }
                        }
                        break;
                    case "tfDescription":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Master().getStockId(), null, "") && lbProceed) {
                                    if (poController.getDetailCount() > 1) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the inventory?\nPlease note that this action will reset all details.\n\nDo you wish to proceed?") == true) {
                                                btnNew.fire();
                                            } else {
                                                loadRecordMaster();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }
                            if (lbProceed) { // uniquely inserted due to retrieval delay
                                poController.Detail(pnMain).setStockId("");
                                loadRecordMaster();
                            }
                        }
                        break;
                    case "tfMeasure":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnMain).setMeasureId("");
                        }
                        break;
                    case "tfConversion":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnMain).setConversionId("");
                        }
                        break;
                }
                JFXUtil.runWithDelay(.5, () -> {
                    loadTableMain.reload();
                });
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, apMaster);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewMainList);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
            switch (currentTableID) {
                case "tblViewMainList":
                    if (!main_data.isEmpty()) {
                        pnMain = newIndex;
                        moveNext(false, false);
                    }
                    break;
            }
        }
    };

    public void moveNext(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apMaster.requestFocus();
                pnMain = isUp ? JFXUtil.moveToPreviousRow(tblViewMainList) : JFXUtil.moveToNextRow(tblViewMainList);
            }
            loadRecordMaster();
            if (pnMain < 0 || pnMain > poController.getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnMain).Inventory().getBarCode(), tfBarcode},
                {poController.Detail(pnMain).Inventory().getDescription(), tfDescription}, // if null or empty, then requesting focus to the txtfield
                {poController.Detail(pnMain).Measure().getDescription(), tfMeasure},
                {poController.Detail(pnMain).UnitConversion().ConvertTo().getDescription(), tfConversion},}, tfConversion); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblDetailRow1, tblDetailRow, tblDetailStatus);
        JFXUtil.setColumnRight(tblDetailQtyConvert);
        JFXUtil.setColumnLeft(tblDetailMeasure, tblDetailConversion);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        tblViewMainList.setItems(main_data);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (!main_data.isEmpty() && event.getClickCount() == 1) {
                ModelInventoryChildUnit selected = (ModelInventoryChildUnit) tblViewMainList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    pnMain = Integer.parseInt(selected.getIndex02()) - 1;
                    moveNext(false, false);
                }
            }
        });
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apBrowse, apMaster);
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
        JFXUtil.setButtonsVisibility(fnValue != EditMode.UNKNOWN, btnHistory);

        JFXUtil.setDisabled(!lbShow, apMaster);
        JFXUtil.setButtonsVisibility(lbShow2, btnActivate, btnDeactivate, btnDisapprove);
    }
}
