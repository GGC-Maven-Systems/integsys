/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import ph.com.guanzongroup.integsys.model.ModelVehicleFinancingPromo_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;
import javax.script.ScriptException;
import org.json.simple.JSONArray;
import ph.com.guanzongroup.cas.sales.VehicleAddOns;
import ph.com.guanzongroup.cas.sales.VehicleFinancingPrice;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.integsys.model.ModelVehicleAddOns_Detail;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class VehiclePromotionsController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static VehicleFinancingPrice poFinancing;
    static VehicleAddOns poAddOns;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    boolean pbEntered = false;

    private ObservableList<ModelVehicleFinancingPromo_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelVehicleAddOns_Detail> details_dataAddOns = FXCollections.observableArrayList();
    private FilteredList<ModelVehicleFinancingPromo_Detail> filteredDataDetail;

    JFXUtil.ReloadableTableTask loadTableFinancing, loadTableAddOns;
    ObservableList<String> comboboxlist = FXCollections.observableArrayList();
    ArrayList<ArrayList<ArrayList<String>>> array = new ArrayList<ArrayList<ArrayList<String>>>();
    ArrayList<ArrayList<ArrayList<String>>> arrayAddOns = new ArrayList<ArrayList<ArrayList<String>>>();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail, apDetail11;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnPrint, btnClose;
    @FXML
    private Tab tabFinancing;
    @FXML
    private ComboBox cmbDPRate;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private TableView tblViewFinancing, tblViewAddOns;
    @FXML
    private TableColumn tblFinancingNo, tblFinancingBrand, tblFinancingModel, tblFinancingVariant, tblFinancingColor, tblFinancingSRP, tblFinancingDPRate, tblFinancingReservationAmount, tblMonthlyAmortization, tblsamp,
                        tblAddOnsNo, tblAddOnsBrand, tblAddOnsModel, tblAddOnsVariant, tblAddOnsColor, tblAddOnsSRP;
    LocalDate ValidFrom;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poJSON = new JSONObject();
            
            poFinancing = new SalesControllers(oApp, null).VehicleFinancingPrice();
            poJSON = poFinancing.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            
            poAddOns = new SalesControllers(oApp, null).VehicleAddOns();
            poJSON = poAddOns.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            
            initLoadTable();
            initTextFields();
            initDetailsGrid();
            clearTextFields();
            initComboboxes();
            initTabPane();
            Platform.runLater(() -> {
                try {
                    poFinancing.setWithUI(true);
                    poFinancing.Master().setCompanyId(psCompanyId);
                    poFinancing.setCompanyId(psCompanyId);
                    poJSON = poFinancing.LoadVehicleFinancing();
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    } else {
                        loadTableFinancing.reload();
                    }
                    
                    poAddOns.setWithUI(true);
                    poAddOns.Master().setCompanyId(psCompanyId);
                    poAddOns.setCompanyId(psCompanyId);
                    poAddOns.setRequireApprove(true);
                    poJSON = poAddOns.loadVehicleList();
                    if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    } else {
                        loadTableAddOns.reload();
                    }
                    
                    loadRecordSearch();
                } catch (ExceptionInInitializerError | SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
                
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
                    case "btnPrint":
                        switch(lnTab){
                            case 1: //Insurance
                                ShowMessageFX.Warning(null, pxeModuleName, "No Lock-In insurance to be print.");
                            break;
                            case 2: //AddOns
                                poAddOns.printTransaction(getCurrentDate());
                            break;
                            default:
                                poFinancing.printTransaction(cmbDPRate.getSelectionModel().getSelectedItem().toString());
                            break;
                        }
                        return;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadTableFinancing.reload();
                loadTableAddOns.reload();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException  ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbDPRate":
                        //there should be method
                        //trigger filtering in table

                        filteredDataDetail.setPredicate(orders -> {
                            String lsval = selectedValue.toString();
                            if (lsval == null
                                    || lsval.isEmpty()
                                    || lsval.equalsIgnoreCase("--All--")) {
                                return true;
                            }
                            return lsval.equalsIgnoreCase(orders.getIndex07());
                        });
                        break;
                }
            });

    private void addChildColumns(TableView<?> tableView, TableColumn parentColumn, JSONArray jsonArray) {
        for (int lnRow = 0; lnRow < jsonArray.size(); lnRow++) {
            JSONObject loJSONObject = (JSONObject) jsonArray.get(lnRow);

            int lnDuration = ((Number) loJSONObject.get("nDuration")).intValue();

            TableColumn column = new TableColumn();

            column.setId("tbl_" + String.valueOf(lnDuration));
            column.setText(String.valueOf(lnDuration));
            JFXUtil.setColumnRight(column);

            column.setMinWidth(100);
            column.setPrefWidth(100);
            column.setMaxWidth(100);

            parentColumn.getColumns().add(column);
        }
        tblsamp.setVisible(false);
    }

    //create a dynamic loader of table column of tblViewFinancing
    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlist, cmbDPRate));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbDPRate);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbDPRate);
    }

    public void initTextFields() {
        JFXUtil.adjustColumnForScrollbar(tblViewFinancing, tblViewAddOns);
    }
    
    private int lnTab = 0;
    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Lock-In Insurance":
                    lnTab = 1;
                    JFXUtil.setButtonsVisibility(false, btnPrint);
                    break;
                case "Add-Ons":
                    lnTab = 2;
                    JFXUtil.setButtonsVisibility(poAddOns.getVehicleListCount() > 0, btnPrint);
                    break;
                default:
                    lnTab = 0;
                    JFXUtil.setButtonsVisibility(poFinancing.getDetailCount() > 0, btnPrint);
                    break;
            }
        });
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
    
    
    private String getData(String lsValue) {
        return JFXUtil.isObjectEqualTo(lsValue, null, "") ? "" : lsValue;
    }

    private ArrayList<String> getCellAddOns(int row, int column) {
        while (arrayAddOns.size() <= row) {
            arrayAddOns.add(new ArrayList<ArrayList<String>>());
        }
        while (arrayAddOns.get(row).size() <= column) {
            arrayAddOns.get(row).add(new ArrayList<String>());
        }
        return arrayAddOns.get(row).get(column);
    }

    private String getCellDataAddOns(int row, int column) {
        if (row >= arrayAddOns.size()) {
            return "";
        }
        if (column >= arrayAddOns.get(row).size()) {
            return "";
        }
        return String.join(", ", arrayAddOns.get(row).get(column));
    }

    public void initLoadTable() {
        loadTableFinancing = new JFXUtil.ReloadableTableTask(
                tblViewFinancing,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {
                            if (pnEditMode != EditMode.UNKNOWN) {
                                reInitializeColumns();
                                reInitializeComboboxes();
                            }
                            array.clear();
                            int lnAmmortizationColumnCount = 0;
                            double lnTotal = 0.0;
                            for (lnCtr = 0; lnCtr < poFinancing.getDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poFinancing.Detail(lnCtr).ModelVariant().getDescription(), null, "")
                                    || !poFinancing.Detail(lnCtr).getRecordStatus()) {
                                    continue;
                                }
                                details_data.add(
                                        new ModelVehicleFinancingPromo_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poFinancing.Detail(lnCtr).ModelVariant().Model().Brand().getDescription()),
                                                String.valueOf(poFinancing.Detail(lnCtr).ModelVariant().Model().getDescription()),
                                                String.valueOf(poFinancing.Detail(lnCtr).ModelVariant().getDescription()),
                                                String.valueOf(poFinancing.Detail(lnCtr).ModelVariant().Color().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poFinancing.Detail(lnCtr).getSRPAmount(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poFinancing.Detail(lnCtr).getDownPaymentRate(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poFinancing.Detail(lnCtr).getReservationAmount(), false)),
                                                String.valueOf(lnCtr), //fixed column (this is not visible)
                                                "",//starts ammortization column dynamic
                                                "",
                                                "",
                                                "",
                                                "",
                                                ""
                                        ));
                                JSONArray loJSONArray = poFinancing.loadStandardInterestRates();
                                lnAmmortizationColumnCount = loJSONArray.size();
                                for (int lnRow = 0; lnRow < loJSONArray.size(); lnRow++) { //sample is it have size of 3
                                    JSONObject loJSONObject = (JSONObject) loJSONArray.get(lnRow);
                                    int lnDuration = (int) loJSONObject.get("nDuration"); // this is the column title per array
                                    Double ldblRate = (Double) loJSONObject.get("nRateValx");
                                    getCell(lnCtr, lnRow).add(String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poFinancing.getMontlyAmortizationAmount(lnCtr, lnDuration, ldblRate), false)));
                                }
                                //store agein as a prepared and then will dynamically added in details_data
                                
                            }
                            //then re-add in here
                            int lnCount = 9;
                            for (lnCtr = 0; lnCtr < poFinancing.getDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poFinancing.Detail(lnCtr).ModelVariant().getDescription(), null, "")) {
                                    continue;
                                }
                                lnCount = 9;
                                //well need to define the number of loJSONArray
                                for (int lnMAcount = 0; lnMAcount < lnAmmortizationColumnCount; lnMAcount++) {
                                    lnCount += 1;
                                    details_data.get(lnCtr).setIndexDynamic(lnCount, getCellData(lnCtr, lnMAcount)); //11
                                }
                            }
                        } catch (SQLException | GuanzonException  ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    }
                    );
                }
        );
        
        loadTableAddOns = new JFXUtil.ReloadableTableTask(
                tblViewAddOns,
                details_dataAddOns,
                () -> {
                    // Setting data to table detail
                    Platform.runLater(() -> {
                        int lnCtr;
                        pbEntered = false;
                        details_dataAddOns.clear();
                        try {
                            reInitializeColumnsAddOns();
                            int lnDynamicColumnCount = 0;
                            arrayAddOns.clear();
                            for (lnCtr = 0; lnCtr < poAddOns.getVehicleListCount(); lnCtr++) {
                                details_dataAddOns.add(
                                        new ModelVehicleAddOns_Detail(
                                                String.valueOf(lnCtr + 1),
                                                getData(String.valueOf(poAddOns.VehicleList(lnCtr).Model().Brand().getDescription())),
                                                getData(String.valueOf(poAddOns.VehicleList(lnCtr).Model().getDescription())),
                                                getData(String.valueOf(poAddOns.VehicleList(lnCtr).getDescription())),
                                                getData(String.valueOf(poAddOns.VehicleList(lnCtr).Color().getDescription())),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poAddOns.VehicleList(lnCtr).getSellingPrice(), false)),
                                                String.valueOf(lnCtr), //fixed column (this is not visible)
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                "",
                                                ""
                                        ));
                                JSONArray loJSONArray = poAddOns.loadUniqueAddOnTypeId(getCurrentDate());
                                lnDynamicColumnCount = loJSONArray.size();
                                for (int lnRow = 0; lnRow < loJSONArray.size(); lnRow++) { //sample is it have size of 3
                                    JSONObject loJSONObject = (JSONObject) loJSONArray.get(lnRow);
                                    String lsAddTypeId = (String) loJSONObject.get("sAddTypex");
                                    getCellAddOns(lnCtr, lnRow).add(String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poAddOns.getAmount(poAddOns.VehicleList(lnCtr).getVariantId(),
                                            lsAddTypeId, getCurrentDate()), false)));
                                }
                            }

                            //then re-add in here
                            int lnCount = 6;
                            for (lnCtr = 0; lnCtr < poAddOns.getVehicleListCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poAddOns.VehicleList(lnCtr).getDescription(), null, "")) {
                                    continue;
                                }
                                lnCount = 6;
                                //well need to define the number of loJSONArray
                                for (int lnMAcount = 0; lnMAcount < lnDynamicColumnCount; lnMAcount++) {
                                    lnCount += 1;
                                    details_dataAddOns.get(lnCtr).setIndexDynamic(lnCount, getCellDataAddOns(lnCtr, lnMAcount)); //11
                                }
                            }

                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    public static int getDetailRowFilter(ObservableList<?> dataList, int lnpn, int columnIndex) {
        try {
            String getterName = String.format("getIndex%02d", columnIndex);

            for (int lnCtr = 0; lnCtr < dataList.size(); lnCtr++) {
                Object item = dataList.get(lnCtr);

                String value = (String) item.getClass()
                        .getMethod(getterName)
                        .invoke(item);

                if (String.valueOf(lnpn).equals(value)) {
                    return lnCtr;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // not found
    }

    private void removeChildColumns(TableView<?> tableView, TableColumn<?, ?> parentColumn) {
        if (parentColumn == null) {
            return;
        }

        if (parentColumn.getColumns().size() > 1) {
            parentColumn.getColumns().remove(1, parentColumn.getColumns().size());
        }
        tableView.refresh();
    }

    private boolean isCmbActive() {
        if (cmbDPRate.getSelectionModel().getSelectedItem() != null) {
            String lsSelected = cmbDPRate.getSelectionModel().getSelectedItem().toString();
            if (lsSelected != null && cmbDPRate.getItems().contains("--ALL--")) {
                return false;
            } else {
                return true;
            }
        } else {
        }
        return false;
    }

    private void reInitializeColumns() {
        try {
            removeChildColumns(tblViewFinancing, tblMonthlyAmortization);
            JSONArray loJSONArray = poFinancing.loadStandardInterestRates();
            addChildColumns(tblViewFinancing, tblMonthlyAmortization, loJSONArray);
            Platform.runLater(() -> {
                disableTableColumnReordering(tblViewFinancing);
                tblViewFinancing.refresh();
            });

            JFXUtil.setColumnCenter(tblFinancingNo);
            JFXUtil.setColumnLeft(tblFinancingBrand, tblFinancingModel, tblFinancingVariant, tblFinancingColor, tblFinancingSRP);
            JFXUtil.setColumnRight(tblFinancingDPRate, tblFinancingReservationAmount);
            JFXUtil.setColumnsIndexAndDisableReordering(tblViewFinancing);
            tblViewFinancing.setItems(details_data);

            if (isCmbActive()) {
            } else {
                filteredDataDetail = new FilteredList<>(details_data, b -> true);
            }

            SortedList<ModelVehicleFinancingPromo_Detail> sortedData = new SortedList<>(filteredDataDetail);
            sortedData.comparatorProperty().bind(tblViewFinancing.comparatorProperty());
            tblViewFinancing.setItems(sortedData);
            tblViewFinancing.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                TableHeaderRow header = (TableHeaderRow) tblViewFinancing.lookup("TableHeaderRow");
                if (header != null) {
                    header.reorderingProperty().addListener((o, oldVal, newVal) -> {
                        header.setReordering(false);
                    });
                }
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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

    private void reInitializeComboboxes() {
        try {
            String lsSelected = "";
            if (cmbDPRate.getSelectionModel().getSelectedItem() != null) {
                lsSelected = cmbDPRate.getSelectionModel().getSelectedItem().toString();
            }

            ArrayList<Double> laStandardDownpaymentRate = poFinancing.loadStandardDownpaymentRates();
            comboboxlist.clear();
            for (int lnCtr2 = 0; lnCtr2 < laStandardDownpaymentRate.size(); lnCtr2++) {
                comboboxlist.add(CustomCommonUtil.setIntegerValueToDecimalFormat(String.valueOf(laStandardDownpaymentRate.get(lnCtr2)), false));
            }
            comboboxlist.add("--All--");
            cmbDPRate.setItems(comboboxlist);
            if (!comboboxlist.isEmpty()) {
                cmbDPRate.getSelectionModel().selectLast();
            }

            if (isCmbActive() && !lsSelected.isEmpty()) {
                cmbDPRate.getSelectionModel().select(lsSelected);
            } else {
            }
            JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbDPRate);
            JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbDPRate);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initDetailsGrid() {
        //Financing
        JFXUtil.setColumnCenter(tblFinancingNo);
        JFXUtil.setColumnLeft(tblFinancingBrand, tblFinancingModel, tblFinancingVariant, tblFinancingColor);
        JFXUtil.setColumnRight(tblFinancingDPRate, tblFinancingReservationAmount, tblFinancingSRP);
        
        //Add Ons
        JFXUtil.setColumnCenter(tblAddOnsNo);
        JFXUtil.setColumnRight(tblAddOnsSRP);
        JFXUtil.setColumnLeft(tblAddOnsBrand, tblAddOnsModel, tblAddOnsVariant, tblAddOnsColor);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewAddOns);
        tblViewAddOns.setItems(details_dataAddOns);
        
    }

    public void clearTextFields() {
        loadTableFinancing.reload();
        loadTableAddOns.reload();
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poFinancing.Master().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    
    private void removeColumnsAfterSixth(TableView<?> tableView) {
        if (tableView.getColumns().size() > 6) {
            tableView.getColumns().remove(6, tableView.getColumns().size());
        }
    }

    private void addColumnsAfterSixth(TableView<?> tableView, JSONArray jsonArray) {
        final double MIN_WIDTH = 100;

        for (int lnRow = 0; lnRow < jsonArray.size(); lnRow++) {
            JSONObject loJSONObject = (JSONObject) jsonArray.get(lnRow);

            String lsTypeName = String.valueOf(loJSONObject.get("sAddTypNm"));
            String lsTypeId = (String) loJSONObject.get("sAddTypex");

            TableColumn column = new TableColumn();

            column.setId(lsTypeId);
            column.setText(lsTypeName);
            JFXUtil.setColumnRight(column);

            // Measure the header text and add padding for cell margins / sort arrow
            Text loText = new Text(lsTypeName);
            double lnTextWidth = loText.getLayoutBounds().getWidth() + 30;
            double lnWidth = Math.max(MIN_WIDTH, lnTextWidth);

            column.setMinWidth(MIN_WIDTH);
            column.setPrefWidth(lnWidth);
            column.setMaxWidth(Double.MAX_VALUE);

            int lnIndex = Math.min(6 + lnRow, tableView.getColumns().size());
            tableView.getColumns().add(lnIndex, column);
        }
    }

    private void reInitializeColumnsAddOns() {
        try {
            removeColumnsAfterSixth(tblViewAddOns);
            JSONArray laArray = poAddOns.loadUniqueAddOnTypeId(getCurrentDate());
            addColumnsAfterSixth(tblViewAddOns, laArray);
            Platform.runLater(() -> {
                tblViewAddOns.refresh();
            });
            initDetailsGrid();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    private String getCurrentDate() throws SQLException{
        return new SimpleDateFormat("MM/dd/yyyy").format(oApp.getServerDate());
    }

    
    private void initButton(int fnValue) {
//        boolean lbShow1 = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
//        boolean lbShow2 = fnValue == EditMode.READY;
//        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
//
//        JFXUtil.setButtonsVisibility(!lbShow1, btnNew);
//        JFXUtil.setButtonsVisibility(lbShow1, btnSave, btnCancel);
//        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
//        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);
//
//        JFXUtil.setDisabled(!lbShow1, apMaster);
//        JFXUtil.setButtonsVisibility(lbShow2, btnApprove, btnPrint);
//
//        if (fnValue != EditMode.READY) {
//            return;
//        }
//        switch (poController.Master().getRecordStatus()) {
//            case ValidityPeriodStatus.OPEN:
//                JFXUtil.setButtonsVisibility(false, btnPrint);
//                break;
//            case ValidityPeriodStatus.VOID:
//            case ValidityPeriodStatus.CANCELLED:
//                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove, btnVoid, btnPrint);
//                break;
//            case ValidityPeriodStatus.APPROVED:
//                JFXUtil.setButtonsVisibility(false, btnUpdate, btnApprove);
//                break;
//        }
    }
}
