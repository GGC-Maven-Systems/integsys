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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
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
import ph.com.guanzongroup.cas.sales.VehicleQuotation;
import ph.com.guanzongroup.integsys.model.ModelSalesQuotationDiscount;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class VehicleSalesQuotation_DiscountController implements Initializable, ScreenInterface {
    
    private GRiderCAS oApp;
    static VehicleQuotation poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private boolean pbEntered = false;
    private int pnDetail = 0;
    private ObservableList<ModelSalesQuotationDiscount> details_data = FXCollections.observableArrayList();
    JFXUtil.ReloadableTableTask loadTableDetail;
    @FXML
    private AnchorPane apBrowse, apButton, apDetail;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnClose;
    @FXML
    private TextField tfDiscountType, tfAmount;
    @FXML
    private CheckBox cbActive;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblNo, tblDiscountType, tblAmount;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poJSON = new JSONObject();
        initTextFields();
        initLoadTable();
        initDetailGrid();
        initTableOnClick();
        clearTextFields();
        Platform.runLater(() -> {
            loadTableDetail.reload();
        });
        pnEditMode = poController.getEditMode();
        initButton(pnEditMode);
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
    
    public void setObject(VehicleQuotation object) {
        poController = object;
    }
    
    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            String lsButton = clickedButton.getId();
            switch (lsButton) {
                case "btnClose":
                    CommonUtils.closeStage(btnClose);
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                    break;
            }
            loadRecordDetail();
            
            initButton(pnEditMode);
        }
    }
    
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfDiscountType":
                        if (lsValue.isEmpty()) {
                            poController.DiscountList(pnDetail).setDiscountType("");
                        }
                        break;
                    case "tfAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.DiscountList(pnDetail).setDiscountAmount(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEntered) {
                            JFXUtil.runWithDelay(0.60, () -> {
                                moveNext(false, true);
                                pbEntered = false;
                            });
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });
    
    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.DiscountList(pnDetail).setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordDetail();
            });
    
    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtDetail_Focus, tfDiscountType, tfAmount);
        
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apDetail);
        JFXUtil.setCommaFormatter(tfAmount);
        
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);
        
        JFXUtil.adjustColumnForScrollbar(tblViewDetail);
    }
    
    private void initTableOnClick() {
        tblViewDetail.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelSalesQuotationDiscount selected = (ModelSalesQuotationDiscount) tblViewDetail.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex04());
                        pnDetail = lnRow;
                        loadRecordDetail();
                        moveNext(false, false);
                    }
                }
            }
        });
    }
    
    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbActive": // freebies
                    if (poController.DiscountList(pnDetail).getEditMode() == EditMode.ADDNEW) {
                        if (!checkedBox.isSelected()) {
                            poController.DiscountList().remove(pnDetail);
                        }
                    } else {
                        poController.DiscountList(pnDetail).setRecordStatus(checkedBox.isSelected());
                    }
                    loadTableDetail.reload();
                    break;
            }
        }
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    newIndex = !isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex04())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex10());
                    if (!details_data.isEmpty()) {
                        pnDetail = newIndex;
                        loadRecordDetail();
                    }
                    break;
            }
        }
    };
    
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
                        case "tfDiscountType":
                            poJSON = poController.SearchDiscountType(lsValue, false, pnDetail);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                            } else {
                                JFXUtil.textFieldMoveNext(tfAmount);
                            }
                            break;
                    }
                    loadTableDetail.reload();
                    break;
                case UP:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfDiscountType", "tfAmount"}, (Runnable) () -> moveNext(true, true)},});
                    break;
                case DOWN:
                    JFXUtil.altSwitch(lsID, new Object[][]{
                        {new String[]{"tfDiscountType", "tfAmount"}, (Runnable) () -> moveNext(false, true)},});
                    break;
                default:
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    public void clearTextFields() {
        JFXUtil.clearTextFields(apDetail);
    }
    
    private void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDiscountListCount() - 1) {
                return;
            }
            tfDiscountType.setText(poController.DiscountList(pnDetail).DiscountType().getDiscountDescription());
            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.DiscountList(pnDetail).getDiscountAmount(), false));
            cbActive.setSelected(poController.DiscountList(pnDetail).getRecordStatus());
            taRemarks.setText(poController.DiscountList(pnDetail).getRemarks());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    private void moveNext(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apDetail.requestFocus();
                pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex04())
                        : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex04());
            }
            loadRecordDetail();
            if (pnDetail < 0 || pnDetail > poController.getDiscountListCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.DiscountList(pnDetail).DiscountType().getDiscountDescription(), tfDiscountType},}, tfAmount); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblDiscountType, tblNo);
        JFXUtil.setColumnRight(tblAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);;
    }
    
    private void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        pbEntered = false;
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDiscountList();
                            }
                            int lnCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getDiscountListCount(); lnCtr++) {
                                if (!poController.DiscountList(lnCtr).getRecordStatus()) {
                                    continue;
                                }
                                lnCount += 1;
                                details_data.add(
                                        new ModelSalesQuotationDiscount(String.valueOf(lnCount),
                                                poController.DiscountList(lnCtr).DiscountType().getDiscountDescription(),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.DiscountList(lnCtr).getDiscountAmount(), false)),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRowFilter(details_data, pnDetail, 4);
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetail, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex04());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetail, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex04());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }
    
    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        if (fnValue != EditMode.READY) {
            return;
        }
        JFXUtil.setDisabled(!lbShow, tfDiscountType, tfAmount, taRemarks);
    }
}
