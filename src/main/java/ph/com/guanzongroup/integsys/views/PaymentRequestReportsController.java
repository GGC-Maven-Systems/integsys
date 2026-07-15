/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.PaymentRequest;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author user
 */
public class PaymentRequestReportsController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private CashflowControllers poPaymentRequest;
    private String psFormName = "PO Summary Report";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    boolean isSummarized = true;
    
    private String searchReqBranch = "";
    private String searchExpBranch = "";
    private String searchDept = "";
    private String searchPayee = "";
    private LocalDate datefrom ;
    private Boolean isSearching = false;
    private volatile boolean isLoading = false;
    
    private static final int ROWS_PER_PAGE = 50;
    private List<ModelTableDetail> allData = new ArrayList<>();
    
    JSONArray data;

    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    ObservableList<String> Status = FXCollections.observableArrayList("ALL", "OPEN", "CONFIRMED", "PROCESSED", "CANCELLED", "VOID", "APPROVED", "POSTED", "RETURNED", "APPROVED+");

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton;

    @FXML
    private RadioButton rbPresentation01, rbPresentation02;

    @FXML
    private ToggleGroup presentation;

    @FXML
    private TextField tfSearchReqBranch,
            tfSearchExpBranch, tfSearchDept, tfSearchPayee;

    @FXML
    private DatePicker dpDateFrom, dpDateThru;

    @FXML
    private ComboBox cmbStatus;

    @FXML
    private HBox hbButtons;

    @FXML
    private Button btnPrint, btnRetrieve, btnClose;

    @FXML
    private TableView<ModelTableDetail> tblVwOrderDetails;

    @FXML
    private TableColumn<ModelTableDetail, String> index01, index02, index03, index04,
            index05, index06, index07, index08,
            index09, index10, index11, index12,
            index13, index14, index15, index16,index17;

    @FXML
    private Pagination pagination;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poPaymentRequest = new CashflowControllers(poApp, logWrapper);
            poJSON = poPaymentRequest.PaymentRequest().InitTransaction();
            poPaymentRequest.PaymentRequest().Master().setIndustryID(psIndustryID);
            poPaymentRequest.PaymentRequest().Master().setCompanyID(psCompanyID);
        if (!"success".equals(poJSON.get("result"))) {
            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
        }
        initButtonsClickActions();
        InitCriterea();
        initComboboxes();
        initDefaultDateRange();
        poPaymentRequest.PaymentRequest().setTransactionStatus("1234560");
        initTableDetail();
        initPrint();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (GuanzonException e) {
            throw new RuntimeException(e);
        }

    }

    private void initPrint() {
        if (data == null || data.isEmpty()) {
            Platform.runLater(() -> {
                btnPrint.setVisible(false);
                btnPrint.setManaged(false);
            });
        } else {
            Platform.runLater(() -> {
                btnPrint.setVisible(true);
                btnPrint.setManaged(true);
            });
        }
    }

    public void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(Status, cmbStatus));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbStatus);
        cmbStatus.getSelectionModel().select(0);
    }

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                try {
                switch (cmbId) {

                    case "cmbStatus":
                        String transStat = getTranStatus((String) selectedValue);

                            poPaymentRequest.PaymentRequest().setTransactionStatus(String.valueOf(transStat));

                        loadTableMaster();
                        break;
                }
                } catch (SQLException | GuanzonException e) {
                    throw new RuntimeException(e);
                }
            }
    );

    private void initDefaultDateRange() {

        LocalDate currentDate = LocalDate.now();

        dpDateThru.setValue(currentDate);
        dpDateFrom.setValue(currentDate.minusMonths(1));

        dpDateFrom.setOnAction(datePickerActionListener);
        dpDateThru.setOnAction(datePickerActionListener);
    }

    EventHandler<ActionEvent> datePickerActionListener = event -> {

        DatePicker source = (DatePicker) event.getSource();

        if (dpDateFrom.getValue() == null || dpDateThru.getValue() == null) {
            ShowMessageFX.Warning(null,
                    "Please select both Date From and Date Thru.",
                    "Warning",
                    null);
            return;
        }

        if (dpDateThru.getValue().isBefore(dpDateFrom.getValue())) {

            ShowMessageFX.Warning(null,
                    "Date Thru cannot be earlier than Date From.",
                    "Warning",
                    null);

            // Reset Date Thru to current date
            dpDateThru.setValue(LocalDate.now());

            source.requestFocus();
            return;
        }

        loadTableMaster();
    };

    private String getTranStatus(String status) {
        switch (status) {
            case "ALL":
                return PaymentRequestStatus.OPEN
                        + PaymentRequestStatus.CONFIRMED
                        + PaymentRequestStatus.PAID
                        + PaymentRequestStatus.CANCELLED
                        + PaymentRequestStatus.VOID
                        + PaymentRequestStatus.POSTED
                        + PaymentRequestStatus.RETURNED;
            case "OPEN":
                return PaymentRequestStatus.OPEN;
            case "CONFIRMED":
                return PaymentRequestStatus.CONFIRMED;
            case "PROCESSED":
                return PaymentRequestStatus.PAID;
            case "CANCELLED":
                return PaymentRequestStatus.CANCELLED;
            case "VOID":
                return PaymentRequestStatus.VOID;
            case "POSTED":
                return PaymentRequestStatus.POSTED;
            case "RETURNED":
                return PaymentRequestStatus.RETURNED;
            default:
                return null;
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnPrint, btnClose, btnRetrieve);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    private void handleButtonAction(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        try {
            switch (lsButton) {
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(AnchorMain, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                    break;
                case "btnRetrieve":
                    loadTableMaster();
                    initPrint();
                    break;
                case "btnPrint":
                    poJSON = poPaymentRequest.PaymentRequest().printReports(() -> {},isSummarized, data);

                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                    }
                    break;
            }
        } catch (SQLException  | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    private void InitCriterea() {
        TextField[] textFields = {
                tfSearchReqBranch,
                tfSearchExpBranch,
                tfSearchDept,
                tfSearchPayee
        };

        for (TextField tf : textFields) {
            tf.focusedProperty().addListener(txtField_Focus);
            tf.setOnKeyPressed(this::txtField_KeyPressed);
        }

        rbPresentation01.setSelected(isSummarized);
        rbPresentation02.setSelected(!isSummarized);
        initTableDetail();
        presentation.selectToggle(rbPresentation01);
        if (presentation != null) {
            presentation.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    return;
                }
                RadioButton selected = (RadioButton) newVal;
                System.out.println("Selected: " + selected.getText());
                if (selected == rbPresentation01) {
                    isSummarized = true;
                    try {
                        poPaymentRequest.PaymentRequest().Master().setSummarized(true);
                    } catch (SQLException | GuanzonException e) {
                        throw new RuntimeException(e);
                    }
                } else if (selected == rbPresentation02) {
                    try {
                    isSummarized = false;
                    poPaymentRequest.PaymentRequest().Master().setSummarized(false);
                    } catch (SQLException | GuanzonException e) {
                        throw new RuntimeException(e);
                    }
                }

                clear();
                initTableDetail();
                initPrint();
            });
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (lsTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = lsTxtField.getText();
        }
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                        break;
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchReqBranch":
                                isSearching = true;

                                try {
                                    poJSON = poPaymentRequest.PaymentRequest().
                                            SearchReqBranch(lsValue, false);

                                    if ("error".equals(poJSON.get("result"))) {
                                        return;
                                    }

                                    tfSearchReqBranch.setText((String)poJSON.get("reqbranch"));
                                    searchReqBranch = (String)poJSON.get("reqbranchID");
                                    loadTableMaster();
                                } catch (GuanzonException | SQLException e) {
                                    throw new RuntimeException(e);
                                }  finally {
                                    isSearching = false;
                                }

                                break;
                            case "tfSearchExpBranch":
                                isSearching = true;

                                try {
                                    poJSON = poPaymentRequest.PaymentRequest().
                                            SearchExpBranch(lsValue, false);

                                    if ("error".equals(poJSON.get("result"))) {
                                        return;
                                    }

                                    tfSearchExpBranch.setText((String)poJSON.get("expbranch"));
                                    searchExpBranch = (String)poJSON.get("expbranchID");
                                    loadTableMaster();
                                } catch (GuanzonException | SQLException e) {
                                    throw new RuntimeException(e);
                                }  finally {
                                    isSearching = false;
                                }

                                break;

                            case "tfSearchDept":
                                isSearching = true;

                                try {
                                    poJSON = poPaymentRequest.PaymentRequest().
                                    SearchDepartmentReport(lsValue, false);

                                    if ("error".equals(poJSON.get("result"))) {
                                        return;
                                    }

                                    tfSearchDept.setText((String)poJSON.get("department"));
                                    searchDept = (String)poJSON.get("departmentID");
                                    loadTableMaster();
                                } catch (GuanzonException | SQLException e) {
                                    throw new RuntimeException(e);
                                }  finally {
                                    isSearching = false;
                                }

                                break;
                            case "tfSearchPayee":
                                isSearching = true;

                                try {
                                    poJSON = poPaymentRequest.PaymentRequest().
                                            SearchPayeeReport(lsValue, false);

                                    if ("error".equals(poJSON.get("result"))) {
                                        return;
                                    }

                                    tfSearchPayee.setText((String)poJSON.get("payee"));
                                    searchPayee = (String)poJSON.get("payeeID");
                                    loadTableMaster();
                                } catch (GuanzonException | SQLException e) {
                                    throw new RuntimeException(e);
                                }  finally {
                                    isSearching = false;
                                }

                                break;
                        }
                        break;
                    case UP:
                        break;
                    case DOWN:
                        break;
                    default:
                        break;
                }
            } catch (ExceptionInInitializerError ex) {
                Logger.getLogger(PaymentRequestReportsController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), psFormName, null);
            }
        }
    }

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                if (isSearching) {
                    return;
                }
//            try {
                /* Lost Focus */
                switch (lsID) {
                    case "tfSearchReqBranch":
                        if(lsValue == null || lsValue.isEmpty()){
                            searchReqBranch = "";
                            loadTableMaster();
                        }
                        break;
                    case "tfSearchExpBranch":
                        if(lsValue == null || lsValue.isEmpty()){
                            searchExpBranch = "";
                            loadTableMaster();
                        }
                        break;
                    case "tfSearchDept":
                        if(lsValue == null || lsValue.isEmpty()){
                            searchDept = "";
                            loadTableMaster();
                        }
                        break;
                    case "tfSearchPayee":
                        if (lsValue == null || lsValue.isEmpty()) {
                            searchPayee = "";
                            loadTableMaster();
                        }
                        break;
                }
            });

    private void initTableDetail() {

//        isSummarized = poPaymentRequest.PaymentRequest().Master().isSummarized();
        index01.setCellValueFactory(new PropertyValueFactory<>("index01"));
        index02.setCellValueFactory(new PropertyValueFactory<>("index02"));
        index03.setCellValueFactory(new PropertyValueFactory<>("index03"));
        index04.setCellValueFactory(new PropertyValueFactory<>("index04"));
        index05.setCellValueFactory(new PropertyValueFactory<>("index05"));
        index06.setCellValueFactory(new PropertyValueFactory<>("index06"));
        index07.setCellValueFactory(new PropertyValueFactory<>("index07"));
        index08.setCellValueFactory(new PropertyValueFactory<>("index08"));
        index09.setCellValueFactory(new PropertyValueFactory<>("index09"));
        index10.setCellValueFactory(new PropertyValueFactory<>("index10"));
        index11.setCellValueFactory(new PropertyValueFactory<>("index11"));
        index12.setCellValueFactory(new PropertyValueFactory<>("index12"));
        index13.setCellValueFactory(new PropertyValueFactory<>("index13"));
        index14.setCellValueFactory(new PropertyValueFactory<>("index14"));
        index15.setCellValueFactory(new PropertyValueFactory<>("index15"));
        index16.setCellValueFactory(new PropertyValueFactory<>("index16"));

        applyTableMode(isSummarized);

        tblVwOrderDetails.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            Platform.runLater(() -> {
                TableHeaderRow header
                        = (TableHeaderRow) tblVwOrderDetails.lookup("TableHeaderRow");

                if (header != null) {
                    header.setReordering(false);
                }
            });
        });
    }

    private void applyTableMode(boolean isSummarized) {

        if (isSummarized) {

            // SUMMARY MODE
            index02.setText("Transaction No");
            index03.setText("Date");
            index04.setText("Requesting Branch");
            index05.setText("Expense Branch");
            index06.setText("Department");
            index07.setText("Payee");
            index08.setText("Series No");
            index09.setText("Source");
            index10.setText("Source No");
            index11.setText("Disc. Amt.");
            index12.setText("Tax Amt.");
            index13.setText("Net Total");
            index14.setText("Amt. Paid");
            index15.setText("Total");
            index16.setText("Status");

            // 🔥 ALIGNMENT
            index02.setStyle("-fx-alignment: CENTER-LEFT;");
            index03.setStyle("-fx-alignment: CENTER;");
            index04.setStyle("-fx-alignment: CENTER-LEFT;");
            index05.setStyle("-fx-alignment: CENTER-LEFT;");
            index06.setStyle("-fx-alignment: CENTER-LEFT;");
            index07.setStyle("-fx-alignment: CENTER-LEFT;");
            index08.setStyle("-fx-alignment: CENTER-LEFT;");
            index09.setStyle("-fx-alignment: CENTER;");
            index10.setStyle("-fx-alignment: CENTER-LEFT;");
            index11.setStyle("-fx-alignment: CENTER-RIGHT;");
            index12.setStyle("-fx-alignment: CENTER-RIGHT;");
            index13.setStyle("-fx-alignment: CENTER-RIGHT;");
            index14.setStyle("-fx-alignment: CENTER-RIGHT;");
            index15.setStyle("-fx-alignment: CENTER-RIGHT;");
            index16.setStyle("-fx-alignment: CENTER-LEFT;");
            index02.setPrefWidth(100);
            index03.setPrefWidth(80);
            index04.setPrefWidth(150);
            index05.setPrefWidth(150);
            index06.setPrefWidth(150);
            index07.setPrefWidth(180);
            index08.setPrefWidth(80);
            index09.setPrefWidth(50);
            index10.setPrefWidth(100);
            index11.setPrefWidth(100);
            index12.setPrefWidth(100);
            index13.setPrefWidth(100);
            index14.setPrefWidth(100);
            index15.setPrefWidth(100);
            index16.setPrefWidth(80);

            index17.setVisible(false);

        } else {


            // DETAIL MODE
            index02.setText("Transaction No");
            index03.setText("Date");
            index04.setText("Requesting Branch");
            index05.setText("Expense Branch");
            index06.setText("Department");
            index07.setText("Payee");
            index08.setText("Particular");
            index09.setText("Recurring");
            index10.setText("Remarks");
            index11.setText("Tax Withheld");
            index12.setText("Vatable");
            index13.setText("Add.Disc.");
            index14.setText("Disc. Amt.");
            index15.setText("Amount");
            index16.setText("Status");

            // 🔥 ALIGNMENT
            index02.setStyle("-fx-alignment: CENTER-LEFT;");
            index03.setStyle("-fx-alignment: CENTER;");
            index04.setStyle("-fx-alignment: CENTER-LEFT;");
            index05.setStyle("-fx-alignment: CENTER-LEFT;");
            index06.setStyle("-fx-alignment: CENTER-LEFT;");
            index07.setStyle("-fx-alignment: CENTER-LEFT;");
            index08.setStyle("-fx-alignment: CENTER-LEFT;");
            index09.setStyle("-fx-alignment: CENTER-LEFT;");
            index10.setStyle("-fx-alignment: CENTER-LEFT;");
            index11.setStyle("-fx-alignment: CENTER-RIGHT;");
            index12.setStyle("-fx-alignment: CENTER-RIGHT;");
            index13.setStyle("-fx-alignment: CENTER-RIGHT;");
            index14.setStyle("-fx-alignment: CENTER-RIGHT;"); // 🔢 qty
            index15.setStyle("-fx-alignment: CENTER-RIGHT;"); // 💰 price
            index16.setStyle("-fx-alignment: CENTER-LEFT;"); // 💰 total

            index02.setPrefWidth(100);
            index03.setPrefWidth(80);
            index04.setPrefWidth(150);
            index05.setPrefWidth(150);
            index06.setPrefWidth(150);
            index07.setPrefWidth(180);
            index08.setPrefWidth(130);
            index09.setPrefWidth(130);
            index10.setPrefWidth(130);
            index11.setPrefWidth(100);
            index12.setPrefWidth(60);
            index13.setPrefWidth(100);
            index14.setPrefWidth(100);
            index15.setPrefWidth(100);
            index16.setPrefWidth(80);
            // SHOW
            index17.setVisible(false);
        }
    }

    private void loadTableMaster() {

        if (isLoading) {
            return;
        }

        isLoading = true;

        btnRetrieve.setDisable(true);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        tblVwOrderDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        poJSON = new JSONObject();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                try {

                    ObservableList<ModelTableDetail> tempData
                            = FXCollections.observableArrayList();

                    if (data != null) {
                        data.clear();
                    }

                    if (isSummarized) {
                        poJSON = poPaymentRequest.PaymentRequest()
                                .RetriveSummaryReports(true,
                                        dpDateFrom.getValue(),
                                        dpDateThru.getValue(),
                                        searchReqBranch,
                                        searchExpBranch,
                                        searchDept,
                                        searchPayee);
                    } else {
                        poJSON = poPaymentRequest.PaymentRequest()
                                .RetriveSummaryDetailedReports(false,
                                        dpDateFrom.getValue(),
                                        dpDateThru.getValue(),
                                        searchReqBranch,
                                        searchExpBranch,
                                        searchDept,
                                        searchPayee);
                    }

                    if ("success".equals(poJSON.get("result"))) {

                        data = (JSONArray) poJSON.get("data");

                        for (int i = 0; i < data.size(); i++) {

                            JSONObject obj = (JSONObject) data.get(i);

                            if (isSummarized) {
                                tempData.add(new ModelTableDetail(
                                        String.valueOf(i + 1),
                                        obj.get("sTransNox") == null ? "" : obj.get("sTransNox").toString(),
                                        obj.get("dTransact") == null ? "" : obj.get("dTransact").toString(),
                                        obj.get("req_branch") == null ? "" : obj.get("req_branch").toString(),
                                        obj.get("expense_branch") == null ? "" : obj.get("expense_branch").toString(),
                                        obj.get("sDeptName") == null ? "" : obj.get("sDeptName").toString(),
                                        obj.get("sPayeeNme") == null ? "" : obj.get("sPayeeNme").toString(),
                                        obj.get("sSeriesNo") == null ? "" : obj.get("sSeriesNo").toString(),
                                        obj.get("sSourceCd") == null ? "" : obj.get("sSourceCd").toString(),
                                        obj.get("sSourceNo") == null ? "" : obj.get("sSourceNo").toString(),
                                        obj.get("nDiscAmtx") == null ? "" : CustomCommonUtil.setIntegerValueToDecimalFormat(Double.parseDouble(obj.get("nDiscAmtx").toString()), true),
                                        obj.get("nTaxAmntx") == null ? "" : CustomCommonUtil.setIntegerValueToDecimalFormat(Double.parseDouble(obj.get("nTaxAmntx").toString()), true),
                                        obj.get("nNetTotal") == null ? "" : CustomCommonUtil.setIntegerValueToDecimalFormat(Double.parseDouble(obj.get("nNetTotal").toString()), true),
                                        obj.get("nAmtPaidx") == null ? "" : CustomCommonUtil.setIntegerValueToDecimalFormat(Double.parseDouble(obj.get("nAmtPaidx").toString()), true),
                                        obj.get("nTranTotl") == null ? "" : CustomCommonUtil.setIntegerValueToDecimalFormat(Double.parseDouble(obj.get("nTranTotl").toString()), true),
                                        obj.get("cTranStat") == null ? "" : obj.get("cTranStat").toString()
                                ));
                            } else {
                                tempData.add(new ModelTableDetail(
                                        String.valueOf(i + 1),
                                        obj.get("sTransNox") == null ? "" : obj.get("sTransNox").toString(),
                                        obj.get("dTransact") == null ? "" : obj.get("dTransact").toString(),
                                        obj.get("req_branch") == null ? "" : obj.get("req_branch").toString(),
                                        obj.get("expense_branch") == null ? "" : obj.get("expense_branch").toString(),
                                        obj.get("sDeptName") == null ? "" : obj.get("sDeptName").toString(),
                                        obj.get("sPayeeNme") == null ? "" : obj.get("sPayeeNme").toString(),
                                        obj.get("particular_name") == null ? "" : obj.get("particular_name").toString(),
                                        obj.get("recurring_name") == null ? "" : obj.get("recurring_name").toString(),
                                        obj.get("sPRFRemxx") == null ? "" : obj.get("sPRFRemxx").toString(),
                                        obj.get("nTWithHld") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nTWithHld").toString()), true),
                                        obj.get("cVATaxabl") == null ? "" : obj.get("cVATaxabl").toString(),
                                        obj.get("nAddDiscx") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nAddDiscx").toString()), true),
                                        obj.get("nDiscount") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nDiscount").toString()), true),
                                        obj.get("nAmountxx") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nAmountxx").toString()), true),
                                        obj.get("cTranStat") == null ? "" : obj.get("cTranStat").toString()
                                ));
                            }
                        }
                    }

                    Platform.runLater(() -> {

                        detail_data.setAll(tempData);

                        if (detail_data.isEmpty()) {
                            tblVwOrderDetails.setPlaceholder(
                                    new Label("NO RECORD TO LOAD"));
                        }

                        tblVwOrderDetails.setItems(detail_data);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return null;
            }

            @Override
            protected void succeeded() {

                isLoading = false;

                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);

                initPrint();

                if (detail_data.isEmpty()) {
                    tblVwOrderDetails.setPlaceholder(
                            new Label("NO RECORD TO LOAD"));

                    ShowMessageFX.Warning(
                            "NO RECORD TO LOAD.",
                            psFormName,
                            null);
                }

                setupPagination();
            }

            @Override
            protected void failed() {

                isLoading = false;

                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);

                if (getException() != null) {
                    getException().printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }
    private void setupPagination() {

    if (detail_data == null || detail_data.isEmpty()) {
        pagination.setPageCount(0);
        pagination.setPageFactory(null);

        tblVwOrderDetails.setItems(FXCollections.observableArrayList());
        tblVwOrderDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));
        return;
    }

    int pageCount = (int) Math.ceil(detail_data.size() * 1.0 / ROWS_PER_PAGE);

    pagination.setPageCount(pageCount);
    pagination.setCurrentPageIndex(0);
    pagination.setPageFactory(this::createPage);
}
    
    private Node createPage(int pageIndex) {

        if (detail_data == null || detail_data.isEmpty()) {
            tblVwOrderDetails.setItems(FXCollections.observableArrayList());
            return new StackPane();
        }

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, detail_data.size());

        ObservableList<ModelTableDetail> pageData
                = FXCollections.observableArrayList(detail_data.subList(fromIndex, toIndex));

        tblVwOrderDetails.setItems(pageData);

        return new StackPane(); // prevents layout re-render/flicker
    }
    private void clear() {

        tfSearchPayee.clear();
        tfSearchExpBranch.clear();
        tfSearchDept.clear();
        tfSearchPayee.clear();

        searchReqBranch = "";
        searchExpBranch = "";
        searchDept = "";
        searchPayee = "";

        cmbStatus.getSelectionModel().select(0);

        initDefaultDateRange();

        // reset data source
        detail_data = FXCollections.observableArrayList();
        data = null; 

        // 🔥 STEP 1: clear table FIRST (break binding)
        tblVwOrderDetails.getItems().clear();
        tblVwOrderDetails.refresh();
        tblVwOrderDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));

        // 🔥 STEP 2: fully detach pagination BEFORE resetting
        pagination.setPageFactory(null);
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(1);

        // 🔥 STEP 3: force JavaFX layout refresh (IMPORTANT FIX)
        Platform.runLater(() -> {
            pagination.applyCss();
            pagination.layout();
        });
        
    }

}
