package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.*;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.constant.SalesTransactionStatic;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationStatic;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for SalesTransactionsLanding.fxml (Motorcycle Sales Transactions list).
 *
 * Structure follows SalesTransactionsMCController: ScreenInterface + Initializable,
 * initControlGroups() after FXML injection, grouped handlers, ClickButton() /
 * handleButtonAction() switch on button id, Task-based table loading, lockColumnReordering().
 *
 * The list is loaded once per search (Refresh / Search / Clear), kept in memory in
 * poAllRows, and paged on the client: only the current page is placed in main_data.
 *
 * ------------------------------------------------------------------------------
 * FXML FIX REQUIRED
 *   The Status ComboBox (row 1, column 1) has no fx:id. This controller expects
 *   fx:id="cmbStatusFilter" with promptText="All status". Add it to the FXML.
 *
 * ASSUMED BACKEND / CONSTANTS  (modelled on the SalesQoutation API, fix in one place)
 *   - oSalesController.SalesTransaction().loadTransactionList(search, dateFrom, dateTo, statusCode, paymentForm)
 *   - SalesTransaction().getMasterCount() / Master(i).getXxx()          -> see toRow()
 *   - ModelTableMain.setIndex01() ... setIndex11()                      -> bound to "index01"... below
 *   - SalesTransactionStatic.STATUS_FILTERS            (String[] for cmbStatusFilter)
 *   - SalesTransactionStatic.PAYMENT_FORMS             (String[], already used by the entry screen)
 *   - SalesTransactionStatic.TBL_NO_TRANSACTIONS       (table placeholder text)
 *   - SalesTransactionStatic.MSG_SELECT_TRANSACTION    ("Please select a transaction first.")
 *   - SalesTransactionStatic.MSG_DATE_FROM_AFTER_TO    ("Date From cannot be later than Date To.")
 *   - SalesTransactionStatic.MSG_UNDER_DEVELOPMENT / MSG_CONFIRM_CLOSE / MODULE_NAME / DATE_PATTERN / LOG_*  (existing)
 * ------------------------------------------------------------------------------
 */
public class SalesTransactionsLandingController implements Initializable, ScreenInterface {

    private final String pxeModuleName = SalesTransactionStatic.MODULE_NAME;

    private GRiderCAS oApp;
    private SalesControllers oSalesController;
    private JSONObject poJSON;
    private boolean pbLoaded = false;
    private boolean pbSyncing = false;   // true while filters are set programmatically

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";

    private int pnRow = -1;              // selected row index within the current page

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(SalesTransactionStatic.DATE_PATTERN);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final Integer[] PAGE_SIZES = {10, 25, 50, 100};
    private static final int DEFAULT_PAGE_SIZE = 25;

    /** true while a list load Task is running; disables the filters and buttons that would clash with it. */
    private final BooleanProperty pbLoading = new SimpleBooleanProperty(false);
    /** true when a row is selected; Open / Update / New From / Print / Export bind to it. */
    private final BooleanProperty pbHasSelection = new SimpleBooleanProperty(false);

    private final IntegerProperty pnPage = new SimpleIntegerProperty(1);       // 1-based
    private final IntegerProperty pnPageSize = new SimpleIntegerProperty(DEFAULT_PAGE_SIZE);
    private final IntegerProperty pnTotalRows = new SimpleIntegerProperty(0);

    // =========================================================================
    // Control groups (filled in initControlGroups(), NOT here:
    // @FXML fields are still null until the FXML loader injects them).
    // =========================================================================
    private List<ComboBox> poFilterCombos;
    private List<DatePicker> poFilterDatePickers;
    private List<Button> poActionButtons;
    private List<Button> poPageButtons;

    // =========================================================================
    // Table data
    // =========================================================================
    /** Every row returned by the last search. */
    private final List<ModelTableMain> poAllRows = new ArrayList<>();
    /** Only the rows of the current page. */
    private final ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();

    // =========================================================================
    // Root containers
    // =========================================================================
    @FXML private AnchorPane ChildAnchorPane;
    @FXML private AnchorPane apSearchMaster;
    @FXML private VBox vbMain;
    @FXML private HBox hbButtons;

    // =========================================================================
    // Filters
    // =========================================================================
    @FXML private TextField tfSearch;
    @FXML private DatePicker dpDateFrom;
    @FXML private DatePicker dpDateTo;
    @FXML private ComboBox cmbStatusFilter;     // NOTE: add this fx:id to the FXML
    @FXML private ComboBox cmbPaymentFilter;
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilter;

    // =========================================================================
    // Transaction list
    // =========================================================================
    @FXML private TableView<ModelTableMain> tblTransactions;
    @FXML private TableColumn<ModelTableMain, String> tblRowNo;
    @FXML private TableColumn<ModelTableMain, String> tblRowTransNo;
    @FXML private TableColumn<ModelTableMain, String> tblRowDate;
    @FXML private TableColumn<ModelTableMain, String> tblRowCustomer;
    @FXML private TableColumn<ModelTableMain, String> tblRowUnits;
    @FXML private TableColumn<ModelTableMain, String> tblRowSource;
    @FXML private TableColumn<ModelTableMain, String> tblRowPaymentForm;
    @FXML private TableColumn<ModelTableMain, String> tblRowTotal;
    @FXML private TableColumn<ModelTableMain, String> tblRowPaid;
    @FXML private TableColumn<ModelTableMain, String> tblRowBalance;
    @FXML private TableColumn<ModelTableMain, String> tblRowStatus;
    @FXML private Label lblRecordInfo;

    // =========================================================================
    // Paging bar
    // =========================================================================
    @FXML private ComboBox cmbPageSize;
    @FXML private Button btnPageFirst;
    @FXML private Button btnPagePrev;
    @FXML private Button btnPageNext;
    @FXML private Button btnPageLast;
    @FXML private Label lblPage;

    // =========================================================================
    // Action buttons
    // =========================================================================
    @FXML private Button btnRefresh;
    @FXML private Button btnOpen;
    @FXML private Button btnNew;
    @FXML private Button btnCreateFrom;
    @FXML private Button btnUpdate;
    @FXML private Button btnPrint;
    @FXML private Button btnExport;
    @FXML private Button btnClose;

    // =========================================================================
    // ScreenInterface
    // =========================================================================
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

    // =========================================================================
    // Lifecycle
    // =========================================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initControlGroups();      // must run first: FXML injection is done by now
        initializeObject();
        initFilters();
        initTable();
        initPaging();
        initButton();
        ClickButton();
        pbLoaded = true;
        Platform.runLater(this::loadList);   // first load once the screen is shown
    }

    /** Groups the controls once, after FXML injection. */
    private void initControlGroups() {
        poFilterCombos = Arrays.asList(cmbStatusFilter, cmbPaymentFilter);
        poFilterDatePickers = Arrays.asList(dpDateFrom, dpDateTo);
        poActionButtons = Arrays.asList(btnRefresh, btnOpen, btnNew, btnCreateFrom, btnUpdate, btnPrint, btnExport, btnClose);
        poPageButtons = Arrays.asList(btnPageFirst, btnPagePrev, btnPageNext, btnPageLast);
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper(SalesTransactionStatic.LOG_SOURCE, System.getProperty(SalesTransactionStatic.LOG_PATH_PROPERTY) + SalesTransactionStatic.LOG_FILE_NAME);
            oSalesController = new SalesControllers(oApp, logwrapr);
            oSalesController.SalesQoutation().initialize();
            oSalesController.SalesQoutation().setRecordStatus(SalesQoutationStatic.OPEN);
            oSalesController.SalesQoutation().setWithUI(true);

            Platform.runLater(() -> btnNew.fire());
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================================
    // Buttons
    // =========================================================================
    private void initButton() {
        CustomCommonUtil.setVisible(true, btnRefresh, btnOpen, btnNew, btnCreateFrom, btnUpdate, btnPrint, btnExport, btnClose);
        CustomCommonUtil.setManaged(true, btnRefresh, btnOpen, btnNew, btnCreateFrom, btnUpdate, btnPrint, btnExport, btnClose);

        // Row based actions need a selected row and no running load
        for (Button lbtn : new Button[]{btnOpen, btnCreateFrom, btnUpdate, btnPrint, btnExport}) {
            lbtn.disableProperty().bind(pbHasSelection.not().or(pbLoading));
        }
        btnRefresh.disableProperty().bind(pbLoading);
        btnSearch.disableProperty().bind(pbLoading);
        btnClearFilter.disableProperty().bind(pbLoading);
    }

    private void ClickButton() {
        for (Button btn : poActionButtons) {
            btn.setOnAction(this::handleButtonAction);
        }
        btnSearch.setOnAction(this::handleButtonAction);
        btnClearFilter.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        String lsID = ((Button) event.getSource()).getId();
        try {
            switch (lsID) {
                case "btnSearch":
                case "btnRefresh":
                    if (!validateFilters()) {
                        return;
                    }
                    loadList();
                    break;
                case "btnClearFilter":
                    ClearFilters();
                    loadList();
                    break;
                case "btnNew":
                    openEntryScreen(ENTRY_NEW, "");
                    break;
                case "btnOpen":
                    if (hasSelectedTransaction()) {
                        openEntryScreen(ENTRY_OPEN, selectedTransNo());
                    }
                    break;
                case "btnUpdate":
                    if (hasSelectedTransaction()) {
                        openEntryScreen(ENTRY_UPDATE, selectedTransNo());
                    }
                    break;
                case "btnCreateFrom":
                    if (hasSelectedTransaction()) {
                        openEntryScreen(ENTRY_NEW_FROM, selectedTransNo());
                    }
                    break;
                case "btnPrint":
                case "btnExport":
                    // TODO: print / export the selected transaction (or the filtered list)
                    ShowMessageFX.Error(SalesTransactionStatic.MSG_UNDER_DEVELOPMENT, pxeModuleName, null);
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(SalesTransactionStatic.MSG_CONFIRM_CLOSE, pxeModuleName, null)) {
                        new unloadForm().unloadForm(ChildAnchorPane, oApp, pxeModuleName);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsLandingController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    // =========================================================================
    // Navigation to the entry screen
    // =========================================================================
    private static final String ENTRY_NEW = "NEW";
    private static final String ENTRY_OPEN = "OPEN";
    private static final String ENTRY_UPDATE = "UPDATE";
    private static final String ENTRY_NEW_FROM = "NEW_FROM";

    /**
     * Opens SalesTransactionMC.fxml (SalesTransactionsMCController) for the given mode.
     * TODO: replace with the project's screen loader. The entry controller currently fires
     * btnNew on start-up, so it needs a way to receive the mode and the transaction no.
     * (e.g. setters on the entry controller, called before the form is attached).
     */
    private void openEntryScreen(String fsMode, String fsTransNo) {
        ShowMessageFX.Error(SalesTransactionStatic.MSG_UNDER_DEVELOPMENT, pxeModuleName, null);
    }

    // =========================================================================
    // Filters
    // =========================================================================
    @SuppressWarnings("unchecked")
    private void initFilters() {
        // Status / Payment: first entry means "all"; the prompt text shows when nothing is selected
        cmbStatusFilter.getItems().setAll(Arrays.asList(SalesTransactionStatic.TRANSACTION_STATUS));
        cmbPaymentFilter.getItems().setAll(Arrays.asList(SalesTransactionStatic.PAYMENT_FORMS));
        cmbStatusFilter.getSelectionModel().clearSelection();
        cmbPaymentFilter.getSelectionModel().clearSelection();

        for (final ComboBox lcmb : poFilterCombos) {
            lcmb.disableProperty().bind(pbLoading);
            lcmb.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                if (!pbLoaded || pbSyncing) {
                    return;
                }
                loadList();
            });
        }

        StringConverter<LocalDate> loConverter = new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fdValue) {
                return fdValue == null ? "" : DATE_FORMAT.format(fdValue);
            }

            @Override
            public LocalDate fromString(String fsValue) {
                if (fsValue == null || fsValue.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(fsValue.trim(), DATE_FORMAT);
                } catch (DateTimeParseException ex) {
                    return null;
                }
            }
        };

        for (final DatePicker ldp : poFilterDatePickers) {
            ldp.setConverter(loConverter);
            ldp.setPromptText(SalesTransactionStatic.DATE_PATTERN);
            ldp.disableProperty().bind(pbLoading);
            ldp.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (!pbLoaded || pbSyncing) {
                    return;
                }
                if (validateFilters()) {
                    loadList();
                }
            });
        }

        tfSearch.disableProperty().bind(pbLoading);
        tfSearch.setOnKeyPressed(this::tfSearch_KeyPressed);
    }

    private void tfSearch_KeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (validateFilters()) {
                loadList();
            }
        }
    }

    /** Date From must not be later than Date To. */
    private boolean validateFilters() {
        LocalDate ldFrom = dpDateFrom.getValue();
        LocalDate ldTo = dpDateTo.getValue();
        if (ldFrom != null && ldTo != null && ldFrom.isAfter(ldTo)) {
            ShowMessageFX.Error(SalesTransactionStatic.MSG_DATE_FROM_AFTER_TO, pxeModuleName, null);
            return false;
        }
        return true;
    }

    private String dateText(DatePicker fdp) {
        return fdp.getValue() == null ? "" : DATE_FORMAT.format(fdp.getValue());
    }

    private String comboText(ComboBox fcmb) {
        Object loValue = fcmb.getSelectionModel().getSelectedItem();
        return loValue == null ? "" : loValue.toString();
    }

    /** Clears every filter without triggering the change handlers. */
    private void ClearFilters() {
        pbSyncing = true;
        try {
            tfSearch.clear();
            for (ComboBox lcmb : poFilterCombos) {
                lcmb.getSelectionModel().clearSelection();
            }
            for (DatePicker ldp : poFilterDatePickers) {
                ldp.setValue(null);
            }
        } finally {
            pbSyncing = false;
        }
    }

    // =========================================================================
    // Table
    // NOTE: ModelTableMain was not provided. Columns are bound to the properties
    // "index01", "index02", ... in the order the columns are declared in the FXML.
    // =========================================================================
    private void initTable() {
        tblRowNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblRowTransNo.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblRowDate.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblRowCustomer.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblRowUnits.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblRowSource.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblRowPaymentForm.setCellValueFactory(new PropertyValueFactory<>("index07"));
        tblRowTotal.setCellValueFactory(new PropertyValueFactory<>("index08"));
        tblRowPaid.setCellValueFactory(new PropertyValueFactory<>("index09"));
        tblRowBalance.setCellValueFactory(new PropertyValueFactory<>("index10"));
        tblRowStatus.setCellValueFactory(new PropertyValueFactory<>("index11"));

        tblTransactions.setItems(main_data);
        tblTransactions.setPlaceholder(new Label(SalesTransactionStatic.TBL_NO_TRANSACTIONS));
        tblTransactions.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            pnRow = newIdx.intValue();
            pbHasSelection.set(pnRow >= 0);
        });
        // Double click opens the selected transaction
        tblTransactions.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && hasSelectedTransaction()) {
                btnOpen.fire();
            }
        });
        // Enter opens the selected transaction
        tblTransactions.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && hasSelectedTransaction()) {
                btnOpen.fire();
            }
        });
        lockColumnReordering(tblTransactions);
    }

    /**
     * JavaFX 8 has no per-column "reorderable" flag, so the header row is told to cancel
     * any column drag. The listener is attached to each header row only once.
     */
    private void lockColumnReordering(final TableView<?> ftbl) {
        ftbl.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            final TableHeaderRow header = (TableHeaderRow) ftbl.lookup(SalesTransactionStatic.HEADER_ROW_SELECTOR);
            if (header != null && !header.getProperties().containsKey(SalesTransactionStatic.REORDER_LOCK_KEY)) {
                header.getProperties().put(SalesTransactionStatic.REORDER_LOCK_KEY, Boolean.TRUE);
                header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    header.setReordering(false);
                });
            }
        });
    }

    private boolean hasSelectedTransaction() {
        if (tblTransactions.getSelectionModel().getSelectedItem() == null) {
            ShowMessageFX.Error(SalesTransactionStatic.MSG_SELECT_TRANSACTION, pxeModuleName, null);
            return false;
        }
        return true;
    }

    /** Transaction no. of the selected row (column index02). */
    private String selectedTransNo() {
        ModelTableMain loRow = tblTransactions.getSelectionModel().getSelectedItem();
        return loRow == null ? "" : loRow.getIndex02();
    }

    // =========================================================================
    // List loading (Task based, then paged on the client)
    // =========================================================================
    /** Reads the filters, loads the list off the FX thread, then shows page 1. */
    private void loadList() {
        if (pbLoading.get()) {
            return;
        }
        final String lsSearch = tfSearch.getText() == null ? "" : tfSearch.getText().trim();
        final String lsFrom = dateText(dpDateFrom);
        final String lsTo = dateText(dpDateTo);
        final String lsStatus = cmbStatusFilter.getSelectionModel().getSelectedIndex() <= 0 ? "" : comboText(cmbStatusFilter);
        final String lsPayment = cmbPaymentFilter.getSelectionModel().getSelectedIndex() <= 0 ? "" : comboText(cmbPaymentFilter);

        Task<List<ModelTableMain>> loTask = new Task<List<ModelTableMain>>() {
            @Override
            protected List<ModelTableMain> call() throws Exception {
                // ASSUMED: runs the search on the backend; the list is then read from the model.
//                poJSON = oSalesController.SalesTransaction().loadTransactionList(lsSearch, lsFrom, lsTo, lsStatus, lsPayment);
//                if ("error".equals(String.valueOf(poJSON.get("result")))) {
//                    throw new Exception(String.valueOf(poJSON.get("message")));
//                }
//                List<ModelTableMain> loRows = new ArrayList<>();
//                int lnCount = oSalesController.SalesTransaction().getMasterCount();   // ASSUMED
//                for (int lnCtr = 0; lnCtr < lnCount; lnCtr++) {
//                    loRows.add(toRow(lnCtr));
//                }
                List<ModelTableMain> loRows = new ArrayList<>();
                return loRows;
            }
        };
        loTask.setOnRunning(e -> pbLoading.set(true));
        loTask.setOnSucceeded(e -> {
            poAllRows.clear();
            poAllRows.addAll(loTask.getValue());
            pnTotalRows.set(poAllRows.size());
            pbLoading.set(false);
            showPage(1);
        });
        loTask.setOnFailed(e -> {
            pbLoading.set(false);
            Throwable loError = loTask.getException();
            Logger.getLogger(SalesTransactionsLandingController.class.getName()).log(Level.SEVERE, null, loError);
            ShowMessageFX.Error(loError == null ? "" : loError.getMessage(), pxeModuleName, null);
        });

        Thread loThread = new Thread(loTask, "SalesTransactionsLanding-load");
        loThread.setDaemon(true);
        loThread.start();
    }



    // =========================================================================
    // Paging
    // =========================================================================
    @SuppressWarnings("unchecked")
    private void initPaging() {
        cmbPageSize.getItems().setAll(Arrays.asList(PAGE_SIZES));
        cmbPageSize.getSelectionModel().select(Integer.valueOf(DEFAULT_PAGE_SIZE));
        cmbPageSize.disableProperty().bind(pbLoading);
        cmbPageSize.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                return;
            }
            pnPageSize.set((Integer) newVal);
            if (pbLoaded) {
                showPage(1);
            }
        });

        btnPageFirst.setOnAction(e -> showPage(1));
        btnPagePrev.setOnAction(e -> showPage(pnPage.get() - 1));
        btnPageNext.setOnAction(e -> showPage(pnPage.get() + 1));
        btnPageLast.setOnAction(e -> showPage(totalPages()));

        // Page buttons: disabled while loading or at the matching end of the list
        btnPageFirst.disableProperty().bind(pbLoading.or(pnPage.lessThanOrEqualTo(1)));
        btnPagePrev.disableProperty().bind(pbLoading.or(pnPage.lessThanOrEqualTo(1)));
        btnPageNext.disableProperty().bind(pbLoading.or(Bindings.createBooleanBinding(
                () -> pnPage.get() >= totalPages(), pnPage, pnPageSize, pnTotalRows)));
        btnPageLast.disableProperty().bind(pbLoading.or(Bindings.createBooleanBinding(
                () -> pnPage.get() >= totalPages(), pnPage, pnPageSize, pnTotalRows)));
    }

    private int totalPages() {
        int lnSize = Math.max(1, pnPageSize.get());
        return Math.max(1, (int) Math.ceil(pnTotalRows.get() / (double) lnSize));
    }

    /** Shows page fnPage (clamped to the valid range) and refreshes the labels. */
    private void showPage(int fnPage) {
        int lnPage = Math.max(1, Math.min(fnPage, totalPages()));
        int lnSize = Math.max(1, pnPageSize.get());
        int lnFrom = (lnPage - 1) * lnSize;
        int lnTo = Math.min(lnFrom + lnSize, poAllRows.size());

        List<ModelTableMain> loPage = new ArrayList<>(poAllRows.subList(lnFrom, lnTo));
        for (int lnCtr = 0; lnCtr < loPage.size(); lnCtr++) {
            loPage.get(lnCtr).setIndex01(String.valueOf(lnFrom + lnCtr + 1));   // running number across pages
        }
        main_data.setAll(loPage);
        tblTransactions.getSelectionModel().clearSelection();
        pnRow = -1;
        pbHasSelection.set(false);

        pnPage.set(lnPage);
        lblPage.setText(String.format("Page %d of %d", lnPage, totalPages()));
        lblRecordInfo.setText(String.format("Showing %d of %d", loPage.size(), poAllRows.size()));
    }
}
