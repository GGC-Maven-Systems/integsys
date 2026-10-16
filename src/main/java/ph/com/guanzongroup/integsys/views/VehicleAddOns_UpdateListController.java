/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.io.IOException;
import ph.com.guanzongroup.integsys.model.ModelVehicleAddOns_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TablePosition;
import javafx.stage.Stage;
import org.guanzon.appdriver.base.SQLUtil;
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
    private Button btnRetrieve, btnPrint, btnClose;
    @FXML
    private TableView tblViewDetail;
    @FXML
    private TableColumn tblNo, tblBrand, tblModel, tblVariant, tblColor, tblSRP, tblsamp;
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
                poController.Master().setCompanyId(psCompanyId);
                poController.setCompanyId(psCompanyId);
//            poController.setIndustryId(psIndustryId);
//            poController.setCategoryId(psCategoryId);
                poController.setWithUI(true);
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
                    case "btnRetrieve":
                        poController.Master().setRecordStatus("01");//retrieve open / approved only
                        poController.Detail().clear();
                        poJSON = poController.loadVehicleList();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        break;
                    case "btnPrint":
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadTableDetail.reload();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void openParameter(String lsAddOnTypeId, String lsVariantId) {
        if (lsAddOnTypeId.isEmpty()) {
            ShowMessageFX.Warning(null, pxeModuleName, "Blank Add On Type Id, unable to display dialog!");
            return;
        }
        String lsFXML = "";
        Object controller = new Object();
        VehicleAddOns_EntryDialogController controller1 = new VehicleAddOns_EntryDialogController(); //Should differ the parameter calling
        lsFXML = "/ph/com/guanzongroup/integsys/views/VehicleAddOns_EntryDialog.fxml";

        controller1.setGRider(oApp);
        controller1.setAddOnTypeId(lsAddOnTypeId);
        controller1.setVariantId(lsVariantId);
        controller1.setIndustryID(psIndustryId);
        controller1.setCompanyID(psCompanyId);
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
        try {
            // DatePicker setup
            JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpAsOf);
            SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
            String lsServerDate = sdfFormat.format(oApp.getServerDate());
            LocalDate currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
            dpAsOf.setValue(currentDate);
        } catch (SQLException ex) {
            Logger.getLogger(VehicleAddOns_UpdateListController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                    // Setting data to table detail
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEntered = false;
                        details_data.clear();
                        try {
                            reInitializeColumns();
                            int lnDynamicColumnCount = 0;
                            array.clear();
                            for (lnCtr = 0; lnCtr < poController.getVehicleListCount(); lnCtr++) {
                                details_data.add(
                                        new ModelVehicleAddOns_Detail(
                                                String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poController.VehicleList(lnCtr).Model().Brand().getDescription())),
                                                getData(String.valueOf(poController.VehicleList(lnCtr).Model().getDescription())),
                                                getData(String.valueOf(poController.VehicleList(lnCtr).getDescription())),
                                                getData(String.valueOf(poController.VehicleList(lnCtr).Color().getDescription())),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.VehicleList(lnCtr).getSellingPrice(), false)),
                                                String.valueOf(lnCtr), //fixed column (this is not visible)
                                                "",
                                                "",
                                                ""
                                        ));
                                JSONArray loJSONArray = poController.loadUniqueAddOnTypeId(dpAsOf.getEditor().getText());
                                lnDynamicColumnCount = loJSONArray.size();
                                for (int lnRow = 0; lnRow < loJSONArray.size(); lnRow++) { //sample is it have size of 3
                                    JSONObject loJSONObject = (JSONObject) loJSONArray.get(lnRow);
                                    String lsAddTypeId = (String) loJSONObject.get("sAddTypex");
                                    getCell(lnCtr, lnRow).add(String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAmount(poController.VehicleList(lnCtr).getVariantId(),
                                            lsAddTypeId, dpAsOf.getEditor().getText()), false)));
                                }
                            }

                            //then re-add in here
                            int lnCount = 6;
                            for (lnCtr = 0; lnCtr < poController.getVehicleListCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.VehicleList(lnCtr).getDescription(), null, "")) {
                                    continue;
                                }
                                lnCount = 6;
                                //well need to define the number of loJSONArray
                                for (int lnMAcount = 0; lnMAcount < lnDynamicColumnCount; lnMAcount++) {
                                    lnCount += 1;
                                    details_data.get(lnCtr).setIndexDynamic(lnCount, getCellData(lnCtr, lnMAcount)); //11
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
        JFXUtil.setColumnRight(tblSRP);
        JFXUtil.setColumnLeft(tblBrand, tblModel, tblVariant, tblColor);
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
            if (details_data.size() > 0 && event.getClickCount() == 1) {
                ModelVehicleAddOns_Detail selected = (ModelVehicleAddOns_Detail) tblViewDetail.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    TablePosition position = null;
                    if (!tblViewDetail.getSelectionModel().getSelectedCells().isEmpty()) {
                        position = (TablePosition) tblViewDetail.getSelectionModel().getSelectedCells().get(0);
                    }
                    if (position != null) {
                        // Get clicked column index
                        int lnColumnIndex = position.getColumn();
                        // Only trigger for columns after the 6th column
                        if (lnColumnIndex > 5) {
                            int lnRow = Integer.parseInt(details_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex01());
                            pnDetail = lnRow - 1;
                            TableColumn column = (TableColumn) position.getTableColumn();
                            openParameter(column.getId(), poController.VehicleList(pnDetail).getVariantId());
                        }
                    }
                }
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

    public static void disableTableColumnReordering(final TableView<?> tableView) {
        Platform.runLater(() -> {
            TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");
            if (header == null) {
                return;
            }
            header.setReordering(false);
            if (!header.reorderingProperty().isBound()) {
                header.reorderingProperty().addListener(
                        (obs, oldValue, newValue) -> {
                            if (newValue) {
                                header.setReordering(false);
                            }
                        });
            }
        });
    }

    private void reInitializeColumns() {
        try {
            removeColumnsAfterSixth(tblViewDetail);
            JSONArray laArray = poController.loadUniqueAddOnTypeId(dpAsOf.getEditor().getText());
            addColumnsAfterSixth(tblViewDetail, laArray);
            Platform.runLater(() -> {
                disableTableColumnReordering(tblViewDetail);
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
