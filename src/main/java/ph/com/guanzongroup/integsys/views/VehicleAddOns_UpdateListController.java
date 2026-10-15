/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import ph.com.guanzongroup.integsys.model.ModelVehicleAddOns_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import javafx.event.EventHandler;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TablePosition;
import javafx.stage.Stage;
import javax.script.ScriptException;
import org.json.simple.JSONArray;
import ph.com.guanzongroup.cas.sales.VehicleAddOns;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehicleAddOns_UpdateListController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static VehicleAddOns poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    boolean lbresetpredicate = false;
    boolean pbEntered = false;
    boolean pbKeyPressed = false;

    private ObservableList<ModelVehicleAddOns_Detail> details_data = FXCollections.observableArrayList();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableDetail;
    ArrayList<ArrayList<ArrayList<String>>> array = new ArrayList<ArrayList<ArrayList<String>>>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    JFXUtil.StageManager stageAddOn = new JFXUtil.StageManager();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apDetail1121;
    @FXML
    private Label lblSource;
    @FXML
    private DatePicker dpAsOf;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnPrint, btnClose;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblNo, tblBrand, tblModel, tblVariant, tblColor, tblSRP;
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
            initLoadTable();
            initDetailsGrid();
            initTextFields();
            initDatepickers();
            initTableOnClick();

            loadTableDetail.reload();
            pnEditMode = poController.getEditMode();
            poController.setWithUI(true);
            initButton(pnEditMode);
            Platform.runLater(() -> {
//            psIndustryId = "";
//            poController.setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
//            poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
//            poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
//            poController.setPurpose(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT);
                loadRecordSearch();
            });
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
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

                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
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

                    case "btnBrowse":
                        poController.Master().setRecordStatus("01234");
                        poJSON = poController.SearchTransaction("", true);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.Master().getEditMode();
                        poController.populateVehicleList();
                        break;
                    case "btnPrint":
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

//                if (JFXUtil.isObjectEqualTo(lsButton, "btnApprove", "btnDisapprove", "btnVoid")) {
//                } else {
//                    loadRecordDetail();
//                    loadTableDetail.reload();
//                }
                loadTableDetail.reload();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void openParameter(String lsAddOnTypeId, String lsVariantId) {
        String lsFXML = "";
        Object controller = new Object();
        VehicleAddOns_EntryDialogController controller1 = new VehicleAddOns_EntryDialogController(); //Should differ the parameter calling
        lsFXML = "/ph/com/guanzongroup/integsys/views/VehicleAddOns_EntryDialog.fxml";

        controller1.setGRider(oApp);
        controller1.setAddOnTypeId(lsAddOnTypeId);
        controller1.setVariantId(lsVariantId);
        controller1.setIndustryID(psIndustryId);
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

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    newIndex = !isMovedDown ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex09())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex09());
                    if (!details_data.isEmpty()) {
                        pnDetail = newIndex;
                    }
                    break;
            }
        }
    };

    private void initDatepickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpAsOf);
    }

    public void initTextFields() {
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail);
        JFXUtil.adjustColumnForScrollbar(tblViewDetail);
    }

    private String getData(String lsValue) {
        return JFXUtil.isObjectEqualTo(lsValue, null, "") ? "" : lsValue;
    }

    private ArrayList<String> getCell(int row, int column) {
        while (array.size() <= row) {
            array.add(new ArrayList<ArrayList<String>>());
        }
        while (array.get(row).size() <= column) {
            array.get(row).add(new ArrayList<String>());
        }
        return array.get(row).get(column);
    }

    private String getCellData(int row, int column) {
        if (row >= array.size()) {
            return "";
        }
        if (column >= array.get(row).size()) {
            return "";
        }
        return String.join(", ", array.get(row).get(column));
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    pbEntered = false;
                    // Setting data to table detail
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEntered = false;
                        details_data.clear();
                        try {
                            if (pnEditMode != EditMode.UNKNOWN) {
                                reInitializeColumns();
                            }
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                details_data.add(
                                        new ModelVehicleAddOns_Detail("",
                                                String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().Brand().getDescription())),
                                                getData(String.valueOf(poController.Detail(lnCtr).ModelVariant().Model().getDescription())),
                                                getData(String.valueOf(poController.Detail(lnCtr).ModelVariant().getDescription())),
                                                getData(String.valueOf(poController.Detail(lnCtr).ModelVariant().Color().getDescription())),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getSRPAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), false)),
                                                String.valueOf(lnCtr)
                                        ));
                                JSONArray loJSONArray = poController.loadUniqueAddOnTypeId(dpAsOf.getEditor().getText());
                                for (int lnRow = 0; lnRow < loJSONArray.size(); lnRow++) { //sample is it have size of 3
                                    JSONObject loJSONObject = (JSONObject) loJSONArray.get(lnRow);
                                    String lsAddTypeId = (String) loJSONObject.get("sAddTypex");
                                    getCell(lnCtr, lnRow).add(String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(poController.Detail(lnCtr).getVariantId(),
                                            lsAddTypeId, dpAsOf.getEditor().getText()), false)));
                                }
                            }

                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewDetail.getSelectionModel().select(0);
                                    tblViewDetail.getFocusModel().focus(0);
                                    pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewDetail.getSelectionModel().select(pnDetail);
                                tblViewDetail.getFocusModel().focus(pnDetail);
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblNo);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor, tblSRP);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(details_data);
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
            try {
                if (details_data.size() > 0) {
                    if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                        ModelVehicleAddOns_Detail selected = (ModelVehicleAddOns_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex01());
                            pnDetail = lnRow - 1;
                            TablePosition position = null;
                            if (!tblViewDetail.getSelectionModel().getSelectedCells().isEmpty()) {
                                position = (TablePosition) tblViewDetail.getSelectionModel().getSelectedCells().get(0);
                            }
                            if (position != null) {
                                TableColumn column = (TableColumn) position.getTableColumn();
                                openParameter(column.getId(), poController.Detail(pnDetail).ModelVariant().getVariantId());

                            }
                        }
                    }
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        });
    }

    private void removeColumnsAfterSixth(TableView<?> tableView) {
        if (tableView.getColumns().size() > 6) {
            tableView.getColumns().remove(6, tableView.getColumns().size());
        }
    }

    private void addColumnsAfterSixth(TableView<?> tableView, JSONArray jsonArray) {
        for (int lnRow = 0; lnRow < jsonArray.size(); lnRow++) {
            JSONObject loJSONObject = (JSONObject) jsonArray.get(lnRow);

            String lsTypeName = ((String) loJSONObject.get("sAddTypNm"));
            String lsTypeId = ((String) loJSONObject.get("sAddTypex"));

            TableColumn column = new TableColumn();

            column.setId(lsTypeId);
            column.setText(String.valueOf(lsTypeName));
            JFXUtil.setColumnRight(column);

            column.setMinWidth(100);
            column.setPrefWidth(100);
            column.setMaxWidth(100);

            int lnIndex = Math.min(6 + lnRow, tableView.getColumns().size());
            tableView.getColumns().add(lnIndex, column);
        }
    }

    private void reInitializeColumns() {
        try {
            removeColumnsAfterSixth(tblViewDetail);
            JSONArray laArray = poController.loadUniqueAddOnTypeId(dpAsOf.getEditor().getText());
            addColumnsAfterSixth(tblViewDetail, laArray);
            Platform.runLater(() -> {
//                disableTableColumnReordering(tblViewDetail);
                tblViewDetail.refresh();
            });

            JFXUtil.setColumnCenter(tblNo);
            JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor, tblSRP);
            JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
            tblViewDetail.setItems(details_data);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
    }
}
