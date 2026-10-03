package ph.com.guanzongroup.integsys.views;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.SalesQoutation;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quotation list screen: shows every Sales Quotation as a parent row of the
 * tree table, with its versions as child rows.
 *
 * <p>Double-clicking a quotation row opens the quotation with its latest
 * version; double-clicking a version row opens that version.</p>
 *
 * @author Team 1 & Team 2
 */
public class SalesQoutationListController implements Initializable, ScreenInterface {

    private static final String MODULE_NAME = "Qoutation List";
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#,##0.00");
    private static final int ROWS_PER_PAGE = 50;   // quotations (parent rows) per page

    private GRiderCAS oApp;
    private SalesControllers oSalesController;
    private final unloadForm poUnload = new unloadForm();

    private String psIndustryId = "";
    private String psCategoryId = "";
    private String psCompanyId = "";

    /** All retrieved quotations; the tree table shows one page of them at a time. */
    private List<JSONObject> paQuotations = new ArrayList<>();

    // ------------------------------------------------------------------
    // FXML
    // ------------------------------------------------------------------

    @FXML private AnchorPane AnchorMain;
    @FXML private AnchorPane apBrowse;
    @FXML private AnchorPane apButton;
    @FXML private HBox hbButtons;
    @FXML private Label lblSource;

    @FXML private Button btnRetrieve;
    @FXML private Button btnClose;

    @FXML private TextField tfSearchTransNo;
    @FXML private TextField tfSearchCustomer;

    @FXML private TreeTableView<QuotationRow> tblTreeQoutationList;
    @FXML private TreeTableColumn<QuotationRow, String> tblRowNo;
    @FXML private TreeTableColumn<QuotationRow, String> tblDVNo;          // Quotation / Parent
    @FXML private TreeTableColumn<QuotationRow, String> tblDate;          // Version
    @FXML private TreeTableColumn<QuotationRow, String> tblSupplier;      // Status
    @FXML private TreeTableColumn<QuotationRow, String> tblPayeeName;     // Customer
    @FXML private TreeTableColumn<QuotationRow, String> tblPaymentForm;   // Amount
    @FXML private TreeTableColumn<QuotationRow, String> tblBankName;      // Created
    @FXML private TreeTableColumn<QuotationRow, String> tblBankAccount;   // Valid Until
    @FXML private TreeTableColumn<QuotationRow, String> tblTransAmount;   // Confirm Date

    @FXML private Pagination pagination;

    // ------------------------------------------------------------------
    // ScreenInterface
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // initialization
    // ------------------------------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oSalesController = new SalesControllers(oApp, logwrapr);
            oSalesController.SalesQoutation().initialize();
            oSalesController.SalesQoutation().setWithUI(true);

            initButtons();
            initSearchFields();
            initTreeTable();

            initPagination();

            Platform.runLater(() -> {
                oSalesController.SalesQoutation().getModel().setIndustryCode(psIndustryId);
                oSalesController.SalesQoutation().getModel().setCategoryCode(psCategoryId);
                oSalesController.SalesQoutation().Version().setBranchCode(oApp.getBranchCode());
                retrieveQuotations();
            });
        } catch (SQLException | GuanzonException ex) {
            logAndShow(ex);
        }
    }

    private void initButtons() {
        btnRetrieve.setOnAction(this::cmdButton_Click);
        btnClose.setOnAction(this::cmdButton_Click);
    }

    private void initSearchFields() {
        // ENTER inside a search field retrieves the list using the typed filters
        // (set editable="true" on these fields in the FXML so they can be typed in)
        for (TextField loField : new TextField[]{tfSearchTransNo, tfSearchCustomer}) {
            loField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    retrieveQuotations();
                    event.consume();
                }
            });
        }
    }

    // ------------------------------------------------------------------
    // tree table
    // ------------------------------------------------------------------

    private void initTreeTable() {
        tblTreeQoutationList.setShowRoot(false);
        tblTreeQoutationList.setRoot(new TreeItem<>(new QuotationRow()));

        bind(tblRowNo, r -> r.rowNo, Pos.CENTER);
        bind(tblDVNo, r -> r.quotationNo, Pos.CENTER_LEFT);
        bind(tblDate, r -> r.version, Pos.CENTER);
        bind(tblSupplier, r -> r.status, Pos.CENTER_LEFT);
        bind(tblPayeeName, r -> r.customer, Pos.CENTER_LEFT);
        bind(tblPaymentForm, r -> r.amount, Pos.CENTER_RIGHT);
        bind(tblBankName, r -> r.created, Pos.CENTER);
        bind(tblBankAccount, r -> r.validUntil, Pos.CENTER);
        bind(tblTransAmount, r -> r.confirmDate, Pos.CENTER);

        // the expand/collapse arrow goes in Quotation / Parent; No. holds only the row number
        tblTreeQoutationList.setTreeColumn(tblDVNo);

        disableColumnReordering();
        tblTreeQoutationList.setOnMouseClicked(this::tree_OnClick);
    }

    private boolean pbRestoringColumns = false;

    /**
     * Java 8 has no TreeTableColumn.setReorderable (added in JavaFX 17), so a
     * dragged column is put back in its original place as soon as it moves.
     */
    @SuppressWarnings("unchecked")
    private void disableColumnReordering() {
        final TreeTableColumn<QuotationRow, ?>[] laOrder = new TreeTableColumn[]{
                tblRowNo, tblDVNo, tblDate, tblSupplier, tblPayeeName,
                tblPaymentForm, tblBankName, tblBankAccount, tblTransAmount};

        tblTreeQoutationList.getColumns().addListener(
                (ListChangeListener<TreeTableColumn<QuotationRow, ?>>) change -> {
                    if (pbRestoringColumns) return;
                    while (change.next()) {
                        if (change.wasPermutated() || change.wasReplaced()) {
                            pbRestoringColumns = true;
                            Platform.runLater(() -> {
                                try {
                                    tblTreeQoutationList.getColumns().setAll(laOrder);
                                } finally {
                                    pbRestoringColumns = false;
                                }
                            });
                            return;
                        }
                    }
                });
    }

    private void bind(TreeTableColumn<QuotationRow, String> column,
                      Function<QuotationRow, String> getter, Pos alignment) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue() == null || cell.getValue().getValue() == null
                        ? "" : getter.apply(cell.getValue().getValue())));
        column.setStyle("-fx-alignment: " + alignment.name().replace('_', '-') + ";");
    }

    private void initPagination() {
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(0);
        // the page factory is called whenever the page changes; the tree table
        // is the real content, so an empty node is returned
        pagination.setPageFactory(pageIndex -> {
            showPage(pageIndex);
            Region loEmpty = new Region();
            loEmpty.setPrefSize(0, 0);
            loEmpty.setMaxSize(0, 0);
            return loEmpty;
        });
    }

    /**
     * Retrieves every quotation with its versions, then shows the first page.
     * Quotations are the parent rows; versions are their children.
     */
    private void retrieveQuotations() {
        try {
            paQuotations = oSalesController.SalesQoutation().getQuotationList(
                    psIndustryId, psCategoryId,
                    tfSearchTransNo.getText(), tfSearchCustomer.getText(), "");

            int lnPages = Math.max(1, (int) Math.ceil(paQuotations.size() / (double) ROWS_PER_PAGE));
            pagination.setPageCount(lnPages);
            pagination.setCurrentPageIndex(0);
            showPage(0);   // the factory is not called when the index was already 0

            if (paQuotations.isEmpty()) {
                ShowMessageFX.Information(null, MODULE_NAME, "No quotation found.");
            }
        } catch (SQLException ex) {
            logAndShow(ex);
        }
    }

    /** Fills the tree table with one page of quotations (row numbers continue across pages). */
    private void showPage(int pageIndex) {
        int lnFrom = pageIndex * ROWS_PER_PAGE;
        int lnTo = Math.min(lnFrom + ROWS_PER_PAGE, paQuotations.size());

        TreeItem<QuotationRow> loRoot = new TreeItem<>(new QuotationRow());
        for (int lnCtr = lnFrom; lnCtr < lnTo; lnCtr++) {
            JSONObject loJSON = paQuotations.get(lnCtr);
            @SuppressWarnings("unchecked")
            List<JSONObject> laVersions = (List<JSONObject>) loJSON.get("aVersions");

            TreeItem<QuotationRow> loParent = new TreeItem<>(toQuotationRow(lnCtr + 1, loJSON, laVersions));
            for (int lnVer = 0; lnVer < laVersions.size(); lnVer++) {
                // newest first: the first row is the highest version number
                int lnVersionNo = laVersions.size() - lnVer;
                loParent.getChildren().add(new TreeItem<>(toVersionRow(loJSON, laVersions.get(lnVer), lnVersionNo)));
            }
            loParent.setExpanded(false);
            loRoot.getChildren().add(loParent);
        }
        tblTreeQoutationList.setRoot(loRoot);
    }

    /** Parent row: the quotation, showing the figures of its latest version. */
    private QuotationRow toQuotationRow(int rowNo, JSONObject quotation, List<JSONObject> versions) {
        QuotationRow loRow = new QuotationRow();
        loRow.rowNo = String.valueOf(rowNo);
        loRow.quotationNo = str(quotation.get("sTransNox"));
        loRow.quotationId = loRow.quotationNo;
        loRow.version = versions.isEmpty() ? "" : String.valueOf(versions.size());   // number of versions
        loRow.status = str(quotation.get("xStatus"));
        loRow.customer = str(quotation.get("sCompnyNm"));
        loRow.created = str(quotation.get("dTransact"));
        if (!versions.isEmpty()) {
            JSONObject loLatest = versions.get(0);
            loRow.amount = formatAmount(loLatest.get("nTranTotl"));
            loRow.validUntil = str(loLatest.get("dValdThru"));
        }
        return loRow;
    }

    /** Child row: one version; shows its version no., with "(Latest)" beside the newest one. */
    private QuotationRow toVersionRow(JSONObject quotation, JSONObject version, int versionNo) {
        QuotationRow loRow = new QuotationRow();
        loRow.quotationId = str(quotation.get("sTransNox"));
        loRow.versionId = str(version.get("sTransNox"));
        loRow.quotationNo = str(version.get("sTransNox"));
        loRow.version = Boolean.TRUE.equals(version.get("bLatest"))
                ? versionNo + " (Latest)" : String.valueOf(versionNo);
        loRow.status = str(version.get("xStatus"));
        loRow.customer = str(quotation.get("sCompnyNm"));
        loRow.amount = formatAmount(version.get("nTranTotl"));
        loRow.created = str(version.get("dTransact"));
        loRow.validUntil = str(version.get("dValdThru"));
        return loRow;
    }

    // ------------------------------------------------------------------
    // events
    // ------------------------------------------------------------------

    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();

        switch (lsButton) {
            case "btnRetrieve":
                retrieveQuotations();
                break;
            case "btnClose":
                if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                    poUnload.unloadForm(AnchorMain, oApp, MODULE_NAME);
                }
                break;
            default:
                ShowMessageFX.Warning("Button is not registered, Please contact admin to assist about the unregistered button", MODULE_NAME, null);
                break;
        }
    }

    private void tree_OnClick(MouseEvent event) {
        if (event.getClickCount() != 2) return;

        TreeItem<QuotationRow> loItem = tblTreeQoutationList.getSelectionModel().getSelectedItem();
        if (loItem == null || loItem.getValue() == null) return;

        QuotationRow loRow = loItem.getValue();
        try {
            // quotation row: versionId is empty, so the latest version is opened
            JSONObject loResult = oSalesController.SalesQoutation().openRecord(loRow.quotationId, loRow.versionId);
            if (!"success".equals((String) loResult.get("result"))) {
                ShowMessageFX.Warning(null, MODULE_NAME, (String) loResult.get("message"));
                return;
            }
            // TODO: show the quotation screen / dialog for the opened record
        } catch (SQLException | GuanzonException ex) {
            logAndShow(ex);
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private String str(Object value) {
        return value == null ? "" : value.toString();
    }

    private String formatAmount(Object value) {
        String lsValue = str(value).replace(",", "").trim();
        if (lsValue.isEmpty()) return "";
        try {
            return AMOUNT_FORMAT.format(Double.parseDouble(lsValue));
        } catch (NumberFormatException e) {
            return lsValue;
        }
    }

    private void logAndShow(Exception ex) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        ShowMessageFX.Error(null, MODULE_NAME, MiscUtil.getException(ex));
    }

    // ------------------------------------------------------------------
    // row model
    // ------------------------------------------------------------------

    /** One row of the tree table: a quotation (parent) or a version (child). */
    public static class QuotationRow {
        String rowNo = "";
        String quotationId = "";   // quotation transaction no., set on both parent and child rows
        String versionId = "";     // empty on a quotation row
        String quotationNo = "";
        String version = "";
        String status = "";
        String customer = "";
        String amount = "";
        String created = "";
        String validUntil = "";
        String confirmDate = "";
    }
}