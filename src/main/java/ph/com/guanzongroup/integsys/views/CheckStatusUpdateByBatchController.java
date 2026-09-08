package ph.com.guanzongroup.integsys.views; 

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.CheckStatusUpdate;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.model.SelectedITems;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckStatusUpdateByBatchController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Check Clear Status";
    private CheckStatusUpdate poCheckStatusUpdateController;
    private DisbursementVoucher poDisbursementController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchBankID = "";
    private String psSearchBankAccountID = "";
    private int pnRow = -1;
    private double xOffset = 0;
    private double yOffset = 0;

    private unloadForm poUnload = new unloadForm();
    
    private final ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;

    ArrayList<SelectedITems> getSelectedItems = new ArrayList<>();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    List<String> listOfDVToAssign = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    
    @FXML
    private AnchorPane AnchorMain;
    @FXML
    private AnchorPane apBrowse;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchBankName, tfSearchBankAccount;
    @FXML
    private AnchorPane apButton;
    @FXML
    private Button btnClear, btnRetrieve, btnClose;
    
    @FXML
    private TableView<ModelDisbursementVoucher_Main> tblVwMain;
    @FXML
    private TableColumn<ModelDisbursementVoucher_Main, String> tblRowNo, tblDVNo, tblDVDate, tblBankName, tblBankAccount, tblCheckNo, tblCheckDate, tblCheckAmount, tblCheckStatus;
    @FXML
    private TableColumn<ModelDisbursementVoucher_Main, CheckBox> tblCheckBox;
    @FXML
    private CheckBox chckSelectAll;
    @FXML
    private Pagination pagination;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poCheckStatusUpdateController = new CashflowControllers(oApp, null).CheckStatusUpdate();
            poJSON = new JSONObject();
            poJSON = poCheckStatusUpdateController.InitTransaction(); 
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initAll();
            Platform.runLater(() -> {
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initAll() {
        poCheckStatusUpdateController.Master().setIndustryID(psIndustryId);
        poCheckStatusUpdateController.Master().setCompanyID(psCompanyId);
        initButtonsClickActions();
        initTextFields();
        initTableMain();
        initTableOnClick();
        initTextFieldsProperty();
        initButtons();
        if (main_data.isEmpty()) {
            Label placeholderLabel = new Label("NO RECORD TO LOAD");
            tblVwMain.setPlaceholder(placeholderLabel);
            pagination.setManaged(false);
            pagination.setVisible(false);
            listOfDVToAssign.clear();
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poCheckStatusUpdateController.Master().Company().getCompanyName() + " - " + poCheckStatusUpdateController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnClear, btnRetrieve, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private JSONObject validateSelectedItem() {
        poJSON = new JSONObject();
        listOfDVToAssign.clear();

        ObservableList<ModelDisbursementVoucher_Main> selectedItems = FXCollections.observableArrayList();
        for (ModelDisbursementVoucher_Main item : main_data) {
            if (item.getSelect().isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            poJSON.put("message", "No items selected to assign.");
            poJSON.put("result", "error");
            return poJSON;
        }

        for (ModelDisbursementVoucher_Main item : selectedItems) {
            listOfDVToAssign.add(item.getIndex03());
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private void loadTableMainAndClearSelectedItems() {
        chckSelectAll.setSelected(false);
        getSelectedItems.clear();
        listOfDVToAssign.clear();
        loadTableMain();
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();

            switch (lsButton) {
                case "btnClear":
                    poJSON = validateSelectedItem();
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        break;
                    }
                    if (!listOfDVToAssign.isEmpty()) {
                        loadAssignWindow(listOfDVToAssign);
                        chckSelectAll.setSelected(false);
                        getSelectedItems.clear();
                        listOfDVToAssign.clear();
                    }
                    break;
                case "btnRetrieve":
                    loadTableMainAndClearSelectedItems();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this Tab?", "Close Tab", null)) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", pxeModuleName, null);
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initTextFields() {
        List<TextField> loTxtFieldKeyPressed = Arrays.asList(tfSearchBankName, tfSearchBankAccount);
        loTxtFieldKeyPressed.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case F3:
                        switch (lsID) {
                            case "tfSearchBankName":
                                poCheckStatusUpdateController.setCheckpayment();
                                poJSON = poCheckStatusUpdateController.SearchBanks(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchBankName.setText(poCheckStatusUpdateController.CheckPayments().getModel().Banks().getBankName() != null ? poCheckStatusUpdateController.CheckPayments().getModel().Banks().getBankName() : "");
                                psSearchBankID = poCheckStatusUpdateController.CheckPayments().getModel().getBankID();
                                break;
                            case "tfSearchBankAccount":
                                poCheckStatusUpdateController.setCheckpayment();
                                poJSON = poCheckStatusUpdateController.SearhBankAccount(lsValue, psSearchBankID, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchBankAccount.setText(poCheckStatusUpdateController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poCheckStatusUpdateController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
                                psSearchBankAccountID = poCheckStatusUpdateController.CheckPayments().getModel().getBankAcountID();
                                break;
                        }
                        CommonUtils.SetNextFocus(txtField);
                        loadTableMain();
                        event.consume();
                    default:
                        break;
                }

            } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadTableMain() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblVwMain.setPlaceholder(loadingPane);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;");

        btnRetrieve.setDisable(true);

        Task<ObservableList<ModelDisbursementVoucher_Main>> task =
                new Task<ObservableList<ModelDisbursementVoucher_Main>>() {

            @Override
            protected ObservableList<ModelDisbursementVoucher_Main> call() throws Exception {
                ObservableList<ModelDisbursementVoucher_Main> tempData = FXCollections.observableArrayList();

                JSONObject result = poCheckStatusUpdateController.getDisbursementx(
                        psSearchBankID,
                        psSearchBankAccountID,
                        ""
                );

                if ("success".equals(result.get("result"))) {
                    JSONArray payload = (JSONArray) result.get("payload");

                    for (int i = 0; i < payload.size(); i++) {
                        JSONObject row = (JSONObject) payload.get(i);
                        String lsCheckStatus;

                        switch ((String) row.get("checkstat")) {
                            case CheckStatus.OPEN:
                                lsCheckStatus = "OPEN";
                                break;
                            case CheckStatus.STOP_PAYMENT:
                                lsCheckStatus = "HOLD";
                                break;
                            case CheckStatus.POSTED:
                                lsCheckStatus = "CLEAR";
                                break;
                            default:
                                lsCheckStatus = "UNKNOWN";
                                break;
                        }

                        tempData.add(new ModelDisbursementVoucher_Main(
                                String.valueOf(i + 1),
                                "",
                                (String) row.get("sTransNox"),
                                (String) row.get("dTransact"),
                                (String) row.get("sBankName"),
                                (String) row.get("sActNumbr"),
                                (String) row.get("sCheckNox"),
                                (String) row.get("dCheckDte"),
                                lsCheckStatus,
                                String.valueOf(row.get("nAmountxx"))
                        ));
                    }
                }
                return tempData;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);

                main_data.setAll(getValue());
                showRetainedHighlight(true);
                
                

                if (main_data.isEmpty()) {
                    tblVwMain.setPlaceholder(new Label("NO RECORD TO LOAD"));
                    ShowMessageFX.Warning("NO RECORD TO LOAD.", pxeModuleName, null);
                }

                // Load pagination system
                JFXUtil.loadTab(pagination,
                            main_data.size(),
                                          ROWS_PER_PAGE,
                                       tblVwMain,
                                filteredMain_Data
                );
                pagination.setVisible(true);
                pagination.setManaged(true);
                System.out.println("Page Count = " + pagination.getPageCount());
                System.out.println("Visible = " + pagination.isVisible());
                System.out.println("Managed = " + pagination.isManaged());
                // FIX: Map listeners immediately to the initial page items loaded
                attachListenersToVisibleTableItems();

                handleCheckboxSelection(null);
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                btnRetrieve.setDisable(false);
                tblVwMain.setPlaceholder(placeholderLabel);
                pagination.setManaged(false);
                pagination.setVisible(false);
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Helper method to dynamically bind listener logic to checkboxes.
     * This is explicitly re-triggered during page changes to prevent detached components.
     */
    private void attachListenersToVisibleTableItems() {
        tblVwMain.getItems().forEach(item -> {
            CheckBox selectCheckBox = item.getSelect();
            
            // Remove any legacy listeners to prevent leak chains
            selectCheckBox.setOnAction(null);
            
            // Assign active runtime action
            selectCheckBox.setOnAction(event -> {
                boolean allSelected = main_data.stream()
                        .allMatch(tableItem -> tableItem.getSelect().isSelected());
                chckSelectAll.setSelected(allSelected);

                handleCheckboxSelection(item);
            });
        });
    }

    private void initTableMain() {
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDVDate, tblBankName, tblBankAccount, tblCheckNo, tblCheckDate, tblCheckStatus);
        JFXUtil.setColumnRight(tblCheckAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwMain);
        
        tblCheckBox.setCellValueFactory(new PropertyValueFactory<>("select"));
        tblCheckBox.setCellFactory(col -> new TableCell<ModelDisbursementVoucher_Main, CheckBox>() {
            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item); 
                }
            }
        });

        chckSelectAll.setOnAction(event -> {
            boolean newValue = chckSelectAll.isSelected();
            
            String firstBank = null;
            String firstStatus = null;
            boolean validBulkCheck = true;

            for (ModelDisbursementVoucher_Main item : main_data) {
                if (newValue) { 
                    if (firstBank == null) {
                        firstBank = item.getIndex05(); // Bank Name Column Data
                        firstStatus = item.getIndex09(); // Check Status Column Data
                    } else if (!firstBank.equals(item.getIndex05()) || !firstStatus.equals(item.getIndex09())) {
                        validBulkCheck = false;
                        break;
                    }
                }
            }

            if (!validBulkCheck && newValue) {
                chckSelectAll.setSelected(false);
                ShowMessageFX.Warning("Cannot select all. Rows contain different banks or check statuses.", pxeModuleName, null);
                return;
            }

            main_data.forEach(item -> item.getSelect().setSelected(newValue));
            handleCheckboxSelection(null);
        });

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblVwMain.setItems(filteredMain_Data);

        // FIX: Listen to the Pagination index properties. Whenever the user flips pages, 
        // we instantly re-bind listeners to the fresh checkbox widgets generated for that page.
        pagination.currentPageIndexProperty().addListener((observable, oldIndex, newIndex) -> {
            Platform.runLater(() -> {
                attachListenersToVisibleTableItems();
                
                // Synchronize global select all toggle status on new page view layer
                boolean allSelectedOnNewPage = main_data.stream()
                        .allMatch(tableItem -> tableItem.getSelect().isSelected());
                chckSelectAll.setSelected(allSelectedOnNewPage);
            });
        });
    }

    private void showRetainedHighlight(boolean isRetained) {
        if (isRetained) {
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"0".equals(pair.getValue())) {
                    plOrderNoFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
                }
            }
        }
        JFXUtil.disableAllHighlight(tblVwMain, highlightedRowsMain);
        plOrderNoPartial.clear();
        for (Pair<String, String> pair : plOrderNoFinal) {
            if (!"0".equals(pair.getValue())) {
                JFXUtil.highlightByKey(tblVwMain, pair.getKey(), "#A7C7E7", highlightedRowsMain);
            }
        }
    }

    private void initTableOnClick() {
        tblVwMain.setOnMouseClicked(event -> {
            if (tblVwMain.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                try {
                    ModelDisbursementVoucher_Main selected = tblVwMain.getSelectionModel().getSelectedItem();
                    if (selected.getIndex03() == null || selected.getIndex03().isEmpty()) {
                        ShowMessageFX.Warning("Invalid to view no transaction no.", pxeModuleName, null);
                        return;
                    }
                    loadDVWindow(selected.getIndex03());
                } catch (SQLException ex) {
                    Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void loadAssignWindow(List<String> fsTransactionNos) throws SQLException, GuanzonException {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/ph/com/guanzongroup/integsys/views/CheckClearingAssign.fxml"));

            CheckClearingAssignController loControl = new CheckClearingAssignController();
            loControl.setGRider(oApp);
            loControl.setCheckStatusUpdate(poCheckStatusUpdateController);
            loControl.setTransaction(fsTransactionNos);  
            fxmlLoader.setController(loControl);

            Parent parent = fxmlLoader.load();
            parent.setOnMousePressed((MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            parent.setOnMouseDragged((MouseEvent event) -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            scene.setFill(Color.TRANSPARENT);
            stage.setTitle("");
            stage.showAndWait();

            loadTableMain();
        } catch (IOException e) {
            ShowMessageFX.Warning(e.getMessage(), "Warning", null);
            System.exit(1);
        }
    }

    private void loadDVWindow(String fsTransactionNo) throws SQLException {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/ph/com/guanzongroup/integsys/views/DisbursementVoucher_View.fxml"));
            DisbursementVoucher_ViewController loControl = new DisbursementVoucher_ViewController();
            loControl.setGRider(oApp);
            loControl.setDisbursement(poDisbursementController);
            loControl.setTransaction(fsTransactionNo);
            fxmlLoader.setController(loControl);
            
            Parent parent = fxmlLoader.load();
            parent.setOnMousePressed((MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            parent.setOnMouseDragged((MouseEvent event) -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
            
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            scene.setFill(Color.TRANSPARENT);
            stage.setTitle("");
            stage.showAndWait();
        } catch (IOException e) {
            ShowMessageFX.Warning(e.getMessage(), "Warning", null);
            System.exit(1);
        }
    }

    private void initTextFieldsProperty() {
        tfSearchBankName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                try {
                    poCheckStatusUpdateController.setCheckpayment();
                    poCheckStatusUpdateController.CheckPayments().getModel().setBankID("");
                    poCheckStatusUpdateController.CheckPayments().getModel().setBankAcountID("");
                    tfSearchBankName.setText("");
                    tfSearchBankAccount.setText("");
                    psSearchBankID = "";
                    psSearchBankAccountID = "";
                    loadTableMain();
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        tfSearchBankAccount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                try {
                    poCheckStatusUpdateController.setCheckpayment();
                    poCheckStatusUpdateController.CheckPayments().getModel().setBankAcountID("");
                    tfSearchBankAccount.setText("");
                    psSearchBankAccountID = "";
                    loadTableMain();
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void initButtons() {
        handleCheckboxSelection(null);
    }

    private void handleCheckboxSelection(ModelDisbursementVoucher_Main clickedItem) {
        ObservableList<ModelDisbursementVoucher_Main> selectedItems = FXCollections.observableArrayList();
        
        // 1. Collect all selected items globally from master array
        for (ModelDisbursementVoucher_Main item : main_data) {
            if (item.getSelect().isSelected()) {
                selectedItems.add(item);
            }
        }

        // 2. Run real-time validation checks on active components
        if (!selectedItems.isEmpty() && clickedItem != null && clickedItem.getSelect().isSelected()) {
            String baselineBank = null;
            String baselineStatus = null;

            // Establish baseline comparing existing selections against the clicked row instance
            for (ModelDisbursementVoucher_Main item : selectedItems) {
                if (item != clickedItem) {
                    baselineBank = item.getIndex05(); 
                    baselineStatus = item.getIndex09(); 
                    break;
                }
            }

            if (baselineBank != null && baselineStatus != null) {
                if (!baselineBank.equals(clickedItem.getIndex05()) || !baselineStatus.equals(clickedItem.getIndex09())) {
                    
                    // Temporarily cut the handler to clear selected state smoothly without recursion
                    clickedItem.getSelect().setOnAction(null);
                    clickedItem.getSelect().setSelected(false);
                    
                    // Rebind standard live validation logic back onto item
                    clickedItem.getSelect().setOnAction(event -> {
                        boolean allSelected = main_data.stream()
                                .allMatch(tableItem -> tableItem.getSelect().isSelected());
                        chckSelectAll.setSelected(allSelected);
                        handleCheckboxSelection(clickedItem);
                    });

                    ShowMessageFX.Warning("Selected items must belong to the same bank and check status.", pxeModuleName, null);
                    return;
                }
            }
        }

        // 3. Toggle visibility using the full global state
        boolean hasSelection = main_data.stream().anyMatch(item -> item.getSelect().isSelected());
        JFXUtil.setButtonsVisibility(hasSelection, btnClear);
    }
}