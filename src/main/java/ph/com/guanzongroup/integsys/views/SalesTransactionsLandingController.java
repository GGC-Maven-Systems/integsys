package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.MCSales;
import ph.com.guanzongroup.cas.sales.constant.SalesTransactionStatic;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Master;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for SalesTransactionsLanding.fxml (Motorcycle Sales Transactions list).
 *
 * Structure follows SalesTransactionsMCController: ScreenInterface + Initializable, a Task-based table load with a
 * sequence counter (a newer load makes an older one stale), JFXUtil loading placeholder, lockColumnReordering(),
 * and the same cell / number helpers.
 *
 * Data comes from MCSales.loadTransactionList() (filtered by industry / category). The filters (search text,
 * date range, payment form, status) and the paging are applied in memory on that list, because MCSales has no
 * filter API.
 *
 * ASSUMED (same assumptions as SalesTransactionsMCController, each used in ONE place):
 *   - Model_Sales_Master getters: getAmountDue, getAdvances, getCWTAmount, getTransactionStatus, getPaymentForm,
 *     getSourceNo, getTransactionDate, Client().getCompanyName(); amount paid is read from the raw column "nAmtPaidx".
 *   - Sales_Detail (sTransNox, sSerialID) and the serial table "Inv_Serial" (sSerialID, sSerial01, sSerial02):
 *     used only for the Units column and for searching by engine / frame no. If that query fails, the units column
 *     shows 0 and the search covers transaction no. and customer only (see loadUnitInfo()).
 *   - The entry screen FXML name (ENTRY_FXML).
 *
 * NAVIGATION: "New" opens the entry screen, which fires its own New. "Open", "Update" and "New From" need the entry
 * controller to accept a transaction no.; see openEntryScreen() for the hook to add.
 */
public class SalesTransactionsLandingController implements Initializable, ScreenInterface {

    private final String pxeModuleName = SalesTransactionStatic.MODULE_NAME;
    private static final String ENTRY_FXML = "SalesTransactionMC.fxml";   // ASSUMED location (same package folder as this one)
    private static final String PREVIEW_FXML = "SalesTransactionsMCPreview.fxml";   // ASSUMED: set to your preview FXML name
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(SalesTransactionStatic.DATE_PATTERN);
    private static final Integer[] PAGE_SIZES = {10, 25, 50, 100};
    private static final int DEFAULT_DAYS = 7;   // default date filter: today minus 7 days up to today

    private GRiderCAS oApp;
    private SalesControllers oSalesController;
    private boolean pbLoaded = false;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";

    private int pnLoadSeq = 0;   // a newer load makes an older one stale
    private int pnPage = 1;

    // =========================================================================
    // Table data
    // =========================================================================
    /** One list row, fully computed off the FX thread so the cell factories only read Strings. */
    private static class TransRow {

        Model_Sales_Master master;
        String transNo = "";
        LocalDate date;
        String customer = "";
        int units;
        String source = "";
        String payment = "";
        double total;
        double paid;
        double balance;
        String status = "";
        String searchText = "";   // lower-cased: trans no, customer, engine nos, frame nos
    }

    private final ObservableList<TransRow> poAllRows = FXCollections.observableArrayList();   // everything loaded
    private List<TransRow> poFiltered = new ArrayList<TransRow>();                            // after the filters
    private final ObservableList<TransRow> page_data = FXCollections.observableArrayList();   // the page shown

    // =========================================================================
    // FXML
    // =========================================================================
    @FXML private AnchorPane ChildAnchorPane;
    @FXML private AnchorPane apSearchMaster;
    @FXML private VBox vbMain;
    @FXML private HBox hbButtons;

    // Filters
    @FXML private TextField tfSearch;
    @FXML private DatePicker dpDateFrom;
    @FXML private DatePicker dpDateTo;
    @FXML private ComboBox<String> cmbPaymentFilter;
    @FXML private ComboBox<String> cmbStatusFilter;
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilter;

    // List
    @FXML private Label lblRecordInfo;
    @FXML private TableView<TransRow> tblTransactions;
    @FXML private TableColumn<TransRow, String> tblRowNo;
    @FXML private TableColumn<TransRow, String> tblRowTransNo;
    @FXML private TableColumn<TransRow, String> tblRowDate;
    @FXML private TableColumn<TransRow, String> tblRowCustomer;
    @FXML private TableColumn<TransRow, String> tblRowUnits;
    @FXML private TableColumn<TransRow, String> tblRowSource;
    @FXML private TableColumn<TransRow, String> tblRowPaymentForm;
    @FXML private TableColumn<TransRow, String> tblRowTotal;
    @FXML private TableColumn<TransRow, String> tblRowPaid;
    @FXML private TableColumn<TransRow, String> tblRowBalance;
    @FXML private TableColumn<TransRow, String> tblRowStatus;

    // Paging
    @FXML private ComboBox<Integer> cmbPageSize;
    @FXML private Button btnPageFirst;
    @FXML private Button btnPagePrev;
    @FXML private Label lblPage;
    @FXML private Button btnPageNext;
    @FXML private Button btnPageLast;

    // Action buttons
    @FXML private Button btnRefresh;
    @FXML private Button btnOpen;
    @FXML private Button btnUpdate;
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
        initializeObject();
        initComboBoxes();
        initDatePickers();
        initTable();
        initButtons();
        initFilterHandlers();
        pbLoaded = true;
        loadTransactions();
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper(SalesTransactionStatic.LOG_SOURCE,
                    System.getProperty(SalesTransactionStatic.LOG_PATH_PROPERTY) + SalesTransactionStatic.LOG_FILE_NAME);
            oSalesController = new SalesControllers(oApp, logwrapr);
            oSalesController.MCSales().initTransaction();
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================================
    // Filters
    // =========================================================================
    private void initComboBoxes() {
        cmbPaymentFilter.getItems().setAll(Arrays.asList(SalesTransactionStatic.PAYMENT_FORMS));
        cmbPaymentFilter.getSelectionModel().clearSelection();   // nothing selected = "All payment forms"
        cmbStatusFilter.getItems().setAll(Arrays.asList(SalesTransactionStatic.TRANSACTION_STATUS));
        cmbStatusFilter.getSelectionModel().selectFirst();   // default: Opened  // nothing selected = "All status"

        cmbPageSize.getItems().setAll(PAGE_SIZES);
        cmbPageSize.getSelectionModel().select(Integer.valueOf(25));
        cmbPageSize.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!pbLoaded || newVal == null) {
                return;
            }
            pnPage = 1;
            showPage();
        });
    }

    private void initDatePickers() {
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
        for (DatePicker ldp : Arrays.asList(dpDateFrom, dpDateTo)) {
            ldp.setConverter(loConverter);
            ldp.setPromptText(SalesTransactionStatic.DATE_PATTERN);
        }
        setDefaultDateRange();   // runs before the first load, so the list starts on the last 7 days
    }

    /** Default date filter: the last 7 days, today included. */
    private void setDefaultDateRange() {
        dpDateTo.setValue(LocalDate.now());
        dpDateFrom.setValue(LocalDate.now().minusDays(DEFAULT_DAYS));
    }

    /** Enter in the search box runs the search. */
    private void initFilterHandlers() {
        tfSearch.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                applyFilter();
            }
        });
    }

    /** Filters poAllRows into poFiltered (in memory), then shows the first page. */
    private void applyFilter() {
        LocalDate ldFrom = dpDateFrom.getValue();
        LocalDate ldTo = dpDateTo.getValue();
        if (ldFrom != null && ldTo != null && ldFrom.isAfter(ldTo)) {
            ShowMessageFX.Warning("Date From must not be after Date To.", "Warning", null);
            dpDateFrom.requestFocus();
            return;
        }
        String lsSearch = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase(Locale.ROOT);
        String lsPayment = cmbPaymentFilter.getSelectionModel().getSelectedItem();
        String lsStatus = cmbStatusFilter.getSelectionModel().getSelectedItem();

        List<TransRow> laResult = new ArrayList<TransRow>();
        for (TransRow loRow : poAllRows) {
            if (!lsSearch.isEmpty() && !loRow.searchText.contains(lsSearch)) {
                continue;
            }
            if (ldFrom != null && (loRow.date == null || loRow.date.isBefore(ldFrom))) {
                continue;
            }
            if (ldTo != null && (loRow.date == null || loRow.date.isAfter(ldTo))) {
                continue;
            }
            if (lsPayment != null && !lsPayment.equals(loRow.payment)) {
                continue;
            }
            if (lsStatus != null && !lsStatus.equals(loRow.status)) {
                continue;
            }
            laResult.add(loRow);
        }
        poFiltered = laResult;
        pnPage = 1;
        showPage();
    }

    private void clearFilter() {
        tfSearch.clear();
        setDefaultDateRange();
        cmbPaymentFilter.getSelectionModel().clearSelection();
        cmbStatusFilter.getSelectionModel().selectFirst();   // back to Opened
        applyFilter();
    }

    // =========================================================================
    // Paging
    // =========================================================================
    private int pageSize() {
        Integer lnSize = cmbPageSize.getSelectionModel().getSelectedItem();
        return lnSize == null || lnSize <= 0 ? 25 : lnSize;
    }

    private int pageCount() {
        return Math.max(1, (int) Math.ceil(poFiltered.size() / (double) pageSize()));
    }

    /** Fills the table with the current page and refreshes the paging bar and the "Showing x of y" label. */
    private void showPage() {
        int lnSize = pageSize();
        int lnTotal = poFiltered.size();
        int lnPages = pageCount();
        pnPage = Math.max(1, Math.min(pnPage, lnPages));

        int lnFrom = (pnPage - 1) * lnSize;
        int lnTo = Math.min(lnTotal, lnFrom + lnSize);
        page_data.setAll(poFiltered.subList(lnFrom, lnTo));
        tblTransactions.refresh();
        if (!page_data.isEmpty()) {
            tblTransactions.getSelectionModel().selectFirst();
        }

        String lsInfo = "Showing " + page_data.size() + " of " + lnTotal;
        if (lnTotal != poAllRows.size()) {
            lsInfo += " (filtered from " + poAllRows.size() + ")";
        }
        lblRecordInfo.setText(lsInfo);
        lblPage.setText("Page " + pnPage + " of " + lnPages);

        btnPageFirst.setDisable(pnPage <= 1);
        btnPagePrev.setDisable(pnPage <= 1);
        btnPageNext.setDisable(pnPage >= lnPages);
        btnPageLast.setDisable(pnPage >= lnPages);
    }

    // =========================================================================
    // Table
    // =========================================================================
    private void initTable() {
        tblRowNo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                String.valueOf((pnPage - 1) * pageSize() + cd.getTableView().getItems().indexOf(cd.getValue()) + 1)));
        tblRowTransNo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().transNo));
        tblRowDate.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                cd.getValue().date == null ? "" : DATE_FORMAT.format(cd.getValue().date)));
        tblRowCustomer.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().customer));
        tblRowUnits.setCellValueFactory(cd -> new ReadOnlyStringWrapper(String.valueOf(cd.getValue().units)));
        tblRowSource.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().source));
        tblRowPaymentForm.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().payment));
        tblRowTotal.setCellValueFactory(cd -> new ReadOnlyStringWrapper(money(cd.getValue().total)));
        tblRowPaid.setCellValueFactory(cd -> new ReadOnlyStringWrapper(money(cd.getValue().paid)));
        tblRowBalance.setCellValueFactory(cd -> new ReadOnlyStringWrapper(money(cd.getValue().balance)));
        tblRowStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().status));

        tblTransactions.setItems(page_data);
        tblTransactions.setPlaceholder(new Label("No transactions found."));
        tblTransactions.setOnMouseClicked(this::tblTransactions_Clicked);
        lockColumnReordering(tblTransactions);
    }

    /** Double-click on a row opens it. */
    private void tblTransactions_Clicked(MouseEvent event) {
        if (event.getClickCount() == 2 && tblTransactions.getSelectionModel().getSelectedItem() != null) {
            openEntryScreen("OPEN", tblTransactions.getSelectionModel().getSelectedItem());
        }
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

    // =========================================================================
    // Loading
    // =========================================================================
    /** Reloads the list from the database (Task-based, like loadTableMCItem() in the entry controller). */
    private void loadTransactions() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblTransactions.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        final int lnSeq = ++pnLoadSeq;   // a newer load makes this one stale

        Task<List<TransRow>> task = new Task<List<TransRow>>() {
            @Override
            protected List<TransRow> call() throws Exception {
                MCSales loSales = oSalesController.MCSales();
                loSales.setIndustryID(psIndustryId);
                loSales.setCompanyID(psCompanyId);
                loSales.setCategoryID(psCategoryId);

                List<TransRow> laRows = new ArrayList<TransRow>();
                // the String overload opens every master with openRecord(), so the amounts are converted to the
                // types the getters expect; the no-arg overload copies raw JDBC values (BigDecimal), which the
                // getters cannot cast, and dbl() then showed 0.00. (This overload filters by industry, not category.)
                JSONObject loJSON = loSales.loadTransactionList("");
                if (loJSON != null && "error".equals(loJSON.get("result"))) {
                    String lsMessage = String.valueOf(loJSON.get("message"));
                    if ("No record found.".equals(lsMessage)) {
                        return laRows;   // an empty list is not an error
                    }
                    throw new Exception(lsMessage);
                }

                Map<String, Integer> loUnits = new HashMap<String, Integer>();
                Map<String, StringBuilder> loSerials = new HashMap<String, StringBuilder>();
                loadUnitInfo(loUnits, loSerials);

                for (Model_Sales_Master loMaster : loSales.getMasterList()) {
                    if (loMaster == null) {
                        continue;
                    }
                    laRows.add(buildRow(loMaster, loUnits, loSerials));
                }
                // newest first
                laRows.sort((a, b) -> b.transNo.compareTo(a.transNo));
                return laRows;
            }

            @Override
            protected void succeeded() {
                if (lnSeq != pnLoadSeq) {   // stale / cancelled
                    loading.progressIndicator.setVisible(false);
                    return;
                }
                poAllRows.setAll(getValue());
                tblTransactions.setPlaceholder(loading.placeholderLabel);
                applyFilter();
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (lnSeq != pnLoadSeq) {
                    return;
                }
                Logger.getLogger(SalesTransactionsLandingController.class.getName()).log(Level.SEVERE, null, getException());
                tblTransactions.setPlaceholder(loading.placeholderLabel);
                loading.progressIndicator.setVisible(false);
                final String lsMessage = getException() == null ? "Unable to load transactions." : getException().getMessage();
                Platform.runLater(() -> ShowMessageFX.Error(lsMessage, pxeModuleName, null));
            }
        };
        new Thread(task).start();
    }

    /**
     * Units per transaction (number of Sales_Detail rows) and the engine / frame nos of those units, for the search box.
     * ASSUMED serial table "Inv_Serial". If the joined query fails, falls back to a plain count (no engine / frame search).
     * Runs on the background thread.
     */
    private void loadUnitInfo(Map<String, Integer> foUnits, Map<String, StringBuilder> foSerials) {
        try {
            ResultSet loRS = oApp.executeQuery("SELECT a.sTransNox, IFNULL(b.sSerial01, '') sSerial01, IFNULL(b.sSerial02, '') sSerial02"
                    + " FROM Sales_Detail a LEFT JOIN Inv_Serial b ON b.sSerialID = a.sSerialID");
            while (loRS != null && loRS.next()) {
                String lsTrans = loRS.getString("sTransNox");
                foUnits.merge(lsTrans, 1, Integer::sum);
                StringBuilder loSb = foSerials.computeIfAbsent(lsTrans, k -> new StringBuilder());
                loSb.append(' ').append(loRS.getString("sSerial01")).append(' ').append(loRS.getString("sSerial02"));
            }
            MiscUtil.close(loRS);
            return;
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsLandingController.class.getName()).log(Level.WARNING, "Serial query failed, counting units only.", ex);
            foUnits.clear();
            foSerials.clear();
        }
        try {
            ResultSet loRS = oApp.executeQuery("SELECT sTransNox, COUNT(*) xUnits FROM Sales_Detail GROUP BY sTransNox");
            while (loRS != null && loRS.next()) {
                foUnits.put(loRS.getString("sTransNox"), loRS.getInt("xUnits"));
            }
            MiscUtil.close(loRS);
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsLandingController.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    /** Builds one list row from a master (background thread: the lookups hit the database here, not in the cell factories). */
    private TransRow buildRow(final Model_Sales_Master foMaster, Map<String, Integer> foUnits, Map<String, StringBuilder> foSerials) {
        TransRow loRow = new TransRow();
        loRow.master = foMaster;
        loRow.transNo = textOf(() -> foMaster.getTransactionNo());
        loRow.date = toLocalDate((java.util.Date) rawOf(() -> foMaster.getTransactionDate()));
        loRow.customer = textOf(() -> foMaster.Client().getCompanyName());
        Integer lnUnits = foUnits.get(loRow.transNo);
        loRow.units = lnUnits == null ? 0 : lnUnits;
        loRow.source = textOf(() -> foMaster.getSourceNo());
        loRow.payment = optionText(SalesTransactionStatic.PAYMENT_FORMS, textOf(() -> foMaster.getPaymentForm()));
        loRow.status = optionText(SalesTransactionStatic.TRANSACTION_STATUS, textOf(() -> foMaster.getTransactionStatus()));

        double lnDue = dbl(() -> foMaster.getAmountDue());
        double lnAdvances = dbl(() -> foMaster.getAdvances());
        double lnCwt = dbl(() -> foMaster.getCWTAmount());
        loRow.paid = dbl(() -> foMaster.getValue("nAmtPaidx"));   // raw column: MCSales never sets it
        loRow.total = lnDue + lnAdvances;                          // same rule as the entry screen: amount due + advances
        loRow.balance = lnDue - lnCwt - loRow.paid;                // same rule as the entry screen

        StringBuilder loSerials = foSerials.get(loRow.transNo);
        loRow.searchText = (loRow.transNo + " " + loRow.customer + (loSerials == null ? "" : loSerials)).toLowerCase(Locale.ROOT);
        return loRow;
    }

    // =========================================================================
    // Buttons
    // =========================================================================
    private void initButtons() {
        // the row actions need a selected row
        btnOpen.disableProperty().bind(tblTransactions.getSelectionModel().selectedItemProperty().isNull());
        btnUpdate.disableProperty().bind(tblTransactions.getSelectionModel().selectedItemProperty().isNull());


        for (Button btn : new Button[]{btnSearch, btnClearFilter, btnPageFirst, btnPagePrev, btnPageNext, btnPageLast,
                btnRefresh, btnOpen,  btnUpdate, btnClose}) {
            btn.setOnAction(this::handleButtonAction);
        }
    }

    private void handleButtonAction(ActionEvent event) {
        String lsID = ((Button) event.getSource()).getId();
        TransRow loSelected = tblTransactions.getSelectionModel().getSelectedItem();
        try {
            switch (lsID) {
                case "btnSearch":
                    applyFilter();
                    break;
                case "btnClearFilter":
                    clearFilter();
                    break;
                case "btnPageFirst":
                    pnPage = 1;
                    showPage();
                    break;
                case "btnPagePrev":
                    pnPage--;
                    showPage();
                    break;
                case "btnPageNext":
                    pnPage++;
                    showPage();
                    break;
                case "btnPageLast":
                    pnPage = pageCount();
                    showPage();
                    break;
                case "btnRefresh":
                    loadTransactions();
                    break;
                case "btnNew":
                    openEntryScreen("NEW", null);
                    break;
                case "btnOpen":
                    openEntryScreen("OPEN", loSelected);
                    break;
                case "btnUpdate":
                    openEntryScreen("UPDATE", loSelected);
                    break;
                case "btnCreateFrom":
                    openEntryScreen("NEWFROM", loSelected);
                    break;
                case "btnPrint":
                    // TODO: MCSales has no print yet (printRecord() is commented out); call it here once it exists.
                    ShowMessageFX.Information("Printing is under development.", pxeModuleName, null);
                    break;
                case "btnExport":
                    // TODO: export the filtered list (poFiltered) once the report / PDF is defined.
                    ShowMessageFX.Information("Export is under development.", pxeModuleName, null);
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

    /**
     * Opens another screen in place of this list.
     *
     * "NEW" opens the entry screen (SalesTransactionMC.fxml / SalesTransactionsMCController), which fires its own New.
     * "OPEN" / "UPDATE" open the selected record in SalesTransactionsMCPreviewController as a modal (showPreview());
     * UPDATE also puts it in edit mode. The preview's Close button closes the modal and the list reloads.
     * "NEWFROM" is not implemented yet.
     */
    private void openEntryScreen(String fsMode, TransRow foRow) {
        if (!"NEW".equals(fsMode) && foRow == null) {
            ShowMessageFX.Warning("Please select a transaction first.", "Warning", null);
            return;
        }
        if ("NEWFROM".equals(fsMode)) {
            ShowMessageFX.Information("Create From is under development.", pxeModuleName, null);
            return;
        }
        try {
            if ("NEW".equals(fsMode)) {
                FXMLLoader loLoader = new FXMLLoader(getClass().getResource(ENTRY_FXML));
                SalesTransactionsMCController loEntry = new SalesTransactionsMCController();
                loEntry.setGRider(oApp);
                loEntry.setIndustryID(psIndustryId);
                loEntry.setCompanyID(psCompanyId);
                loEntry.setCategoryID(psCategoryId);
                loLoader.setController(loEntry);
                showInPlace((AnchorPane) loLoader.load());
                return;
            }

            showPreview(foRow.transNo, "UPDATE".equals(fsMode));
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionsLandingController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
        }
    }

    /**
     * Opens SalesTransactionsMCPreviewController as a modal over this list and loads the selected transaction into it.
     * The preview's Close button closes the modal; the list is reloaded afterwards, so a Confirm / Void / Update done in
     * the preview shows up here.
     */
    private void showPreview(String fsTransNo, boolean fbUpdate) throws Exception {
        FXMLLoader loLoader = new FXMLLoader(getClass().getResource(PREVIEW_FXML));
        SalesTransactionsMCPreviewController loPreview = new SalesTransactionsMCPreviewController();
        loPreview.setGRider(oApp);
        loPreview.setIndustryID(psIndustryId);
        loPreview.setCompanyID(psCompanyId);
        loPreview.setCategoryID(psCategoryId);
        loLoader.setController(loPreview);
        Parent loRoot = loLoader.load();

        Stage loStage = new Stage();
        loStage.setTitle("Motorcycle Sales");
        loStage.initStyle(StageStyle.UNDECORATED);
        loStage.initModality(Modality.APPLICATION_MODAL);
        loStage.initOwner(btnOpen.getScene().getWindow());
        loPreview.setOnClose(loStage::close);   // the preview's Close button closes this modal

        // load the record BEFORE showing: nothing is shown when it cannot be opened
        if (!loPreview.openFromList(fsTransNo, fbUpdate)) {
            return;
        }

        // 1. MAKE THE MAIN ANCHOR MOVABLE
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];

        loRoot.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });

        loRoot.setOnMouseDragged(event -> {
            loStage.setX(event.getScreenX() - xOffset[0]);
            loStage.setY(event.getScreenY() - yOffset[0]);
        });

        // 2. APPLY SHADOW TO THE MAIN ANCHOR
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0)); // 40% Opacity black shadow
        shadow.setRadius(15.0);
        shadow.setOffsetX(0.0);
        shadow.setOffsetY(5.0);
        loRoot.setEffect(shadow);

        // 3. BOUNDARY FIX FOR UNDECORATED SHADOWS
        // To prevent the shadow from being clipped by the window edges,
        // the scene is made transparent.
        Scene scene = new Scene(loRoot);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        loStage.setScene(scene);
        loStage.setResizable(false);
        loStage.showAndWait();

        loadTransactions();   // the preview may have changed the record
    }

    /** Puts a loaded root in place of whatever is in ChildAnchorPane, stretched to the edges. */
    private void showInPlace(AnchorPane foRoot) {
        AnchorPane.setTopAnchor(foRoot, 0.0);
        AnchorPane.setBottomAnchor(foRoot, 0.0);
        AnchorPane.setLeftAnchor(foRoot, 0.0);
        AnchorPane.setRightAnchor(foRoot, 0.0);
        ChildAnchorPane.getChildren().setAll(foRoot);
    }

    // =========================================================================
    // Helpers (same behavior as in SalesTransactionsMCController)
    // =========================================================================
    private interface Getter {

        Object get() throws Exception;
    }

    private static Object rawOf(Getter fg) {
        try {
            return fg.get();
        } catch (Exception ex) {
            return null;
        }
    }

    private static String textOf(Getter fg) {
        Object loVal = rawOf(fg);
        return loVal == null ? "" : String.valueOf(loVal);
    }

    /** Number as double; 0 when the getter throws or the value is null / blank / not numeric. */
    private static double dbl(Getter fg) {
        try {
            Object loVal = fg.get();
            if (loVal == null) {
                return 0.0;
            }
            if (loVal instanceof Number) {
                return ((Number) loVal).doubleValue();
            }
            String lsVal = String.valueOf(loVal).replace(",", "").trim();
            return lsVal.isEmpty() ? 0.0 : Double.parseDouble(lsVal);
        } catch (Exception ex) {
            // logged so a failing getter (e.g. ClassCastException) is not silently shown as 0.00
            Logger.getLogger(SalesTransactionsLandingController.class.getName()).log(Level.WARNING, "Amount getter failed: " + ex, ex);
            return 0.0;
        }
    }

    /** Code ("0", "1"...) to its text in the given option list; an unknown code is shown as is. */
    private static String optionText(String[] faOptions, String fsCode) {
        if (fsCode == null || fsCode.trim().isEmpty()) {
            return "";
        }
        try {
            int lnCode = Integer.parseInt(fsCode.trim());
            return (lnCode >= 0 && lnCode < faOptions.length) ? faOptions[lnCode] : fsCode;
        } catch (NumberFormatException ex) {
            return fsCode;   // already text
        }
    }

    private static String money(double fn) {
        return String.format("%,.2f", fn);
    }

    /** java.sql.Date has no toInstant(), so convert it with its own toLocalDate(). */
    private static LocalDate toLocalDate(java.util.Date fdDate) {
        if (fdDate == null) {
            return null;
        }
        if (fdDate instanceof java.sql.Date) {
            return ((java.sql.Date) fdDate).toLocalDate();
        }
        return fdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}