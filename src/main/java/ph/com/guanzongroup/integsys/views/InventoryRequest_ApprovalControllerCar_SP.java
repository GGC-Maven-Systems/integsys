package ph.com.guanzongroup.integsys.views;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Master;
import org.json.simple.JSONObject;
import org.guanzon.cas.inv.warehouse.InventoryRequestApproval;
import org.guanzon.cas.inv.warehouse.services.DeliveryIssuanceControllers;

/**
 *
 * @author User
 */
public class InventoryRequest_ApprovalControllerCar_SP implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private InventoryRequestApproval poAppController;
    private String psFormName = "Stock Request Approval Car SP";
    private String psIndustryID, psCompanyID, psCategoryID;
    private Control lastFocusedControl;
    private ObservableList<Model_Inv_Stock_Request_Detail> laTransactionDetail;
    private int pnTransaction, pnCTransactionDetail, pnEditMode;

    @FXML
    private AnchorPane apMainAnchor, apDetail, apTransactionTable;

    @FXML
    private Label lblSource;

    @FXML
    private TextField tfClusterName, tfBranchName, tfBrand, tfBarcode, tfDescription, tfModel, tfVariant, tfRequestQty,
            tfApprovedQty, tfQOH, tfClassification, tfROQ, tfCancelQty, tfSourceNo;

    @FXML
    private TextArea taRemarks;

    @FXML
    private Button btnSearch, btnUpdate, btnSave, btnCancel, btnPrint, btnClose;

    @FXML
    private TableView<Model_Inv_Stock_Request_Master> tblTransaction;

    @FXML
    private TableView<Model_Inv_Stock_Request_Detail> tblRequestDetail;

    @FXML
    private TableColumn<Model_Inv_Stock_Request_Master, String> tblColStockRequestNo, tblColBranch, tblColTransaction, tblColTransactionDate;

    @FXML
    private TableColumn<Model_Inv_Stock_Request_Detail, String> tblColBrand, tblColBarcode, tblColNo, tblColDescription, tblColModel, tblColVariant, tblColQOH,
            tblColRequestQty, tblColClassification, tblColROQ, tblColCancelQty, tblColApprovedQty;

    @FXML
    void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();

            //trigger action event of last focused object, based on clicked button
            switch (btnID) {
                case "btnPrint":
                    if (poAppController.getMaster().getTransactionNo() == null || poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Stock Request Approval", "");
                        break;
                    }
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to print the transaction ?") == true) {
                        if (!isJSONSuccess(poAppController.printRecord(),
                                "Initialize Print Transaction")) {
                            break;
                        }
                    }
                    //refresh ui 
//                    clearAllInputs();
                    reloadTableDetail();

                    if (poAppController.getBranchCluster().getClusterDescription() != null && !poAppController.getBranchCluster().getClusterDescription().isEmpty()) {
                        loadSelectedBranchClusterDelivery();
                    }
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnSearch":

                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        break;
                    }

                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        break;
                    }
                    switch (lastFocusedControl.getId()) {
                        //Search Detail 
                        case "tfClusterName":

                            if (!isJSONSuccess(poAppController.searchClusterBranch(tfClusterName.getText(), false), "Initialize Search Cluster")) {
                                break;
                            }
                            loadSelectedBranchClusterDelivery();
                            break;
                        default:
                            ShowMessageFX.Information(null, psFormName,
                                    "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");

                            break;

                    }
                    break;

                case "btnUpdate":

                    if (poAppController.getMaster().getTransactionNo() == null || poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Stock Request Approval", "");
                        break;
                    }

                    if (!isJSONSuccess(poAppController.UpdateTransaction(), "Initialize Update Transaction")) {
                        break;
                    }
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnSave":
                    if (poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Stock Request Approval", "");
                        break;
                    }
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save transaction?") != true) {
                        return;
                    }
                    if (!isJSONSuccess(poAppController.SaveTransaction(), "Initialize Save Transaction")) {
                        break;
                    }

                    //refresh ui
                    if (!isJSONSuccess(poAppController.OpenTransaction(tblColTransaction.getCellData(pnTransaction)),
                            "Initialize Open Transaction")) {
                        return;

                    }
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {

                        if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                            unloadForm appUnload = new unloadForm();
                            appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                            break;
                        }

                        Platform.runLater(() -> {
                            poAppController.getMaster().setIndustryId(psIndustryID);
                            poAppController.setIndustryID(psIndustryID);
                            poAppController.setCompanyID(psCompanyID);
                            poAppController.setCategoryID(psCategoryID);
                            clearAllInputs();
                        });
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;

                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                        appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        break;
                    }
                    break;
            }

            initButtonDisplay(pnEditMode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void tblTransaction_MouseClicked(MouseEvent event) {

        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            return;
        }
        pnTransaction = tblTransaction.getSelectionModel().getSelectedIndex();
        if (pnTransaction < 0) {
            return;
        }
        pnCTransactionDetail = tblRequestDetail.getSelectionModel().getSelectedIndex();

        if (event.getClickCount() == 1 && !event.isConsumed()) {

            try {
                event.consume();
                if (!isJSONSuccess(poAppController.OpenTransaction(tblColTransaction.getCellData(pnTransaction)),
                        "Initialize Open Transaction")) {
                    return;

                }

//                clearAllInputs();
                getLoadedTransaction();
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(DeliverySchedule_EntryController.class.getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            }
        }
        return;
    }

    @FXML
    void tblRequestDetail_MouseClicked(MouseEvent event) {

        if (event.getClickCount() == 1 && !event.isConsumed()) {

            try {
                pnCTransactionDetail = tblRequestDetail.getSelectionModel().getSelectedIndex();
                if (pnCTransactionDetail < 0) {
                    return;
                }

                event.consume();
                loadSelectedDetail(pnCTransactionDetail);
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(DeliverySchedule_EntryController.class.getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            }
        }
        return;
    }

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {

                case "tfApprovedQty":
                    if (!isValidQty(lsValue)) {
                        loTextField.setText("0.0");
                        loTextField.requestFocus();
                        return;
                    }
                    poAppController.getDetail(pnCTransactionDetail + 1).getApproved();
                    poAppController.getDetail(pnCTransactionDetail + 1).setApproved(Double.parseDouble(lsValue));
                    break;
                case "tfCancelQty":
                    if (!isValidQty(lsValue)) {
                        loTextField.setText("0.0");
                        loTextField.requestFocus();
                        return;
                    }
                    poAppController.getDetail(pnCTransactionDetail + 1).getCancelled();
                    poAppController.getDetail(pnCTransactionDetail + 1).setCancelled(Double.parseDouble(lsValue));
                    break;
            }

            reloadTableDetail();
        } else {
            loTextField.selectAll();
        }
    };

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            //initialize class controller
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new DeliveryIssuanceControllers(poApp, poLogWrapper).InventoryRequestApproval();

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {

                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCompanyID(psCompanyID);
                poAppController.setCategoryID(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID
                        + "\nCompany :" + psCompanyID
                        + "\nCategory:" + psCategoryID);
            });
            poAppController.getMaster().setIndustryId(psIndustryID);
            poAppController.getMaster().setCompanyID(psCompanyID);
            poAppController.getMaster().setCategoryId(psCategoryID);
            lblSource.setText((poAppController.getMaster().Company().getCompanyName() == null ? "" : (poAppController.getMaster().Company().getCompanyName() + " - "))
                    + (poAppController.getMaster().Industry().getDescription() == null ? "" : poAppController.getMaster().Industry().getDescription()));

            tfClusterName.requestFocus();
            lastFocusedControl = tfClusterName;
            initControlEvents();
            initializeTableDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Fetching All Controller 
    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            if (message != null) {
                poLogWrapper.severe(psFormName + " :" + message);
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, message));
                }
            }
            return false;
        }

        String message = (String) loJSON.get("message");
        poLogWrapper.severe(psFormName + " :" + message);
        if (message != null) {
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Information(null, psFormName, message);
            } else {
                Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, message));
            }
        }

        poLogWrapper.info(psFormName + " : Success on " + fsModule);
        return true;
    }

    private boolean isValidQty(String fsVal) {
        if (fsVal == null ? true : fsVal.isEmpty()) {
            ShowMessageFX.Information("Invalid quantity! Please enter valid quantity", getClass().getSimpleName(), "");
            return false;
        }

        if (Double.parseDouble(fsVal) < 0) {
            ShowMessageFX.Information("Invalid quantity! Please enter greater than zero", getClass().getSimpleName(), "");
            return false;
        }

        return true;
    }

    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            }
        }
        clearAllInputs();
    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnSearch", "btnClose");

        System.out.print(!lbShow);
        System.out.print(!lbShow);

        // Show-only based on mode
        initButtonControls(lbShow, "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnPrint", "btnUpdate");
        apDetail.setDisable(!lbShow);
    }

    private void initButtonControls(boolean visible, String... buttonFxIdsToShow) {
        Set<String> showOnly = new HashSet<>(Arrays.asList(buttonFxIdsToShow));

        for (Field loField : getClass().getDeclaredFields()) {
            loField.setAccessible(true);
            String fieldName = loField.getName(); // fx:id

            // Only touch the buttons listed
            if (!showOnly.contains(fieldName)) {
                continue;
            }
            try {
                Object value = loField.get(this);
                if (value instanceof Button) {
                    Button loButton = (Button) value;
                    loButton.setVisible(visible);
                    loButton.setManaged(visible);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
    }

    private void clearAllInputs() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl != null && loControl instanceof TableView) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }

            }
        }
        pnEditMode = poAppController.getEditMode();
        initButtonDisplay(poAppController.getEditMode());
    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfClusterName":
                                if (!isJSONSuccess(poAppController.searchClusterBranch(lsValue, false), " Search Cluster! ")) {
                                    return;
                                }
                                loadSelectedBranchClusterDelivery();
                                return;

                            default:
                                CommonUtils.SetNextFocus((TextField) event.getSource());
                                return;
                        }
                    case UP:
                        CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        return;
                    case DOWN:
                        CommonUtils.SetNextFocus(loTxtField);
                        return;

                }
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(DeliverySchedule_EntryController.class
                    .getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private void loadSelectedBranchClusterDelivery() throws CloneNotSupportedException {
        StackPane overlay = getOverlayProgress(apTransactionTable);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_Inv_Stock_Request_Master>> loadTransactionTask = new Task<ObservableList<Model_Inv_Stock_Request_Master>>() {
            @Override
            protected ObservableList<Model_Inv_Stock_Request_Master> call() throws Exception {

                if (!isJSONSuccess(poAppController.loadTransactionList(),
                        "Initialize : Load of Transaction List")) {
                    return null;
                }

                List<Model_Inv_Stock_Request_Master> rawList = poAppController.getMasterList();
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {

                try {

                    ObservableList<Model_Inv_Stock_Request_Master> laMasterList = getValue();

                    tblTransaction.setItems(laMasterList);

                    tblColStockRequestNo.setCellValueFactory(loModel -> {
                        int index = tblTransaction.getItems().indexOf(loModel.getValue()) + 1;
                        return new SimpleStringProperty(String.valueOf(index));
                    });

                    tblColTransaction.setCellValueFactory(loModel
                            -> new SimpleStringProperty(loModel.getValue().getTransactionNo()));
                    tblColBranch.setCellValueFactory(loModel
                            -> {
                        try {
                            return new SimpleStringProperty(loModel.getValue().Branch().getBranchName());
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(InventoryRequest_ApprovalController.class.getName()).log(Level.SEVERE, null, ex);
                            return new SimpleStringProperty("");
                        }
                    }
                    );
                    tblColTransactionDate.setCellValueFactory(loModel
                            -> new SimpleStringProperty(SQLUtil.dateFormat(loModel.getValue().getTransactionDate(), SQLUtil.FORMAT_LONG_DATE)));

                    getLoadedTransaction();
                } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
                    Logger.getLogger(InventoryRequest_ApprovalController.class.getName()).log(Level.SEVERE, null, ex);
                }

                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                Logger
                        .getLogger(InventoryRequest_ApprovalController.class
                                .getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };

        Thread thread = new Thread(loadTransactionTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void initializeTableDetail() {
        if (laTransactionDetail == null) {

            laTransactionDetail = FXCollections.observableArrayList();

            tblRequestDetail.setItems(laTransactionDetail);
            tblColQOH.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColRequestQty.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColCancelQty.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColApprovedQty.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

            tblColNo.setCellValueFactory(loModel -> {
                int index = tblRequestDetail.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColBrand.setCellValueFactory(loModel -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().Brand().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryRequest_ApprovalController.class.getName()).log(Level.SEVERE, null, ex);
                    return new SimpleStringProperty("");
                }
            });

            tblColBarcode.setCellValueFactory(loModel -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().getBarCode());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryRequest_ApprovalController.class.getName()).log(Level.SEVERE, null, ex);
                    return new SimpleStringProperty("");
                }
            });

            tblColDescription.setCellValueFactory(loModel -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryRequest_ApprovalController.class.getName()).log(Level.SEVERE, null, ex);
                    return new SimpleStringProperty("");
                }
            });

            tblColModel.setCellValueFactory(loModel -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().Model().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryRequest_ApprovalController.class.getName()).log(Level.SEVERE, null, ex);
                    return new SimpleStringProperty("");
                }
            });

            tblColVariant.setCellValueFactory(loModel -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().Variant().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryRequest_ApprovalController.class.getName()).log(Level.SEVERE, null, ex);
                    return new SimpleStringProperty("");
                }
            });

            tblColClassification.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(loModel.getValue().getClassification());
            });

            tblColROQ.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getRecommendedOrder()));
            });

            tblColQOH.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getQuantityOnHand()));
            });

            tblColRequestQty.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getQuantity()));
            });

            tblColCancelQty.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getCancelled()));
            });

            tblColApprovedQty.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getApproved()));
            });

        }
    }

    private void loadSelectedDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        tfBranchName.setText(tblColBranch.getCellData(fnRow));
        tfBrand.setText(tblColBrand.getCellData(fnRow));
        tfBarcode.setText(tblColBarcode.getCellData(fnRow));
        tfDescription.setText(tblColDescription.getCellData(fnRow));
        tfModel.setText(tblColModel.getCellData(fnRow));
        tfVariant.setText(tblColVariant.getCellData(fnRow));
        tfClassification.setText(tblColClassification.getCellData(fnRow));
        tfROQ.setText(tblColROQ.getCellData(fnRow));
        tfQOH.setText(tblColQOH.getCellData(fnRow));
        tfRequestQty.setText(tblColRequestQty.getCellData(fnRow));
        tfCancelQty.setText(tblColCancelQty.getCellData(fnRow));
        tfApprovedQty.setText(tblColApprovedQty.getCellData(fnRow));
        taRemarks.setText(poAppController.getMaster().getRemarks());
        tfSourceNo.setText(poAppController.getMaster().getReferenceNo());

    }

    private void getLoadedTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        tfClusterName.setText(poAppController.getBranchCluster().getClusterDescription());
        reloadTableDetail();
        loadSelectedDetail(pnCTransactionDetail);
    }

    private void reloadTableDetail() {
        List<Model_Inv_Stock_Request_Detail> rawDetail = poAppController.getDetailList();
        laTransactionDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnCTransactionDetail >= 0 && pnCTransactionDetail < laTransactionDetail.size())
                ? pnCTransactionDetail
                : laTransactionDetail.size() - 1;

        tblRequestDetail.getSelectionModel().select(indexToSelect);

        pnCTransactionDetail = tblRequestDetail.getSelectionModel().getSelectedIndex(); // Not focusedIndex

        tblRequestDetail.refresh();
    }
}
