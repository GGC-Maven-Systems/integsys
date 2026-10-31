package ph.com.guanzongroup.integsys.views;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import ph.com.guanzongroup.cas.sales.MCSales;
import ph.com.guanzongroup.cas.sales.constant.SalesTransactionStatic;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Giveaways;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for SalesTransactionNonItemize.fxml: a read-only modal that lists the NON-ITEMIZED giveaways of the source
 * quotation of the Motorcycle Sales transaction that is open in SalesTransactionsMCController.
 *
 * The @FXML fields below match the fx:ids of the FXML exactly:
 *   header : tfTransactionNo, dpInquiryDate, tfCustomerName
 *   table  : tblFollowUp with the columns tblRowFUNo (No), tblRowDesc (Description), tblRowQty (Qty)
 *   buttons: hbButtons, btnClose, btnCLoseModal
 *
 * Usage (see SalesTransactionsMCController.showNonItemizeGiveaways()):
 *   controller.setCustomerName(...);
 *   controller.setMCSales(oSalesController.MCSales());   // fills the form, BEFORE the stage is shown
 *   stage.showAndWait();
 *
 * ASSUMED (the Model_Sales_Quotation_Version_Giveaways class was not provided): the two columns below are read through
 * the raw getValue() of the model. If your table names them differently, change the two COL_* constants (one place).
 */
public class SalesTransactionNonItemizeController implements Initializable {

    private static final String MODULE_NAME = SalesTransactionStatic.MODULE_NAME;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(SalesTransactionStatic.DATE_PATTERN);

    /**
     * The real getter / column names of Model_Sales_Quotation_Version_Giveaways were not provided, so each cell tries
     * the typed getters first, then the raw columns, in this order. The first non-blank value wins.
     * When the console prints "[NONITEMIZE] ..." lines, use them to keep only the real name in each array.
     */
    private static final String[] DESC_GETTERS = {"getDescription", "getGiveawayDescription", "getRemarks", "getNotes", "getName"};
    private static final String[] DESC_COLUMNS = {"sDescript", "sGiveAway", "sGiveaway", "sRemarksx", "sNotesxxx", "sDescrip"};
    private static final String[] QTY_GETTERS = {"getQuantity", "getQty", "getGiveawayQuantity"};
    private static final String[] QTY_COLUMNS = {"nQuantity", "nQtyxxxxx", "nQty"};

    private MCSales poSales;
    private final ObservableList<Model_Sales_Quotation_Version_Giveaways> giveaway_data = FXCollections.observableArrayList();

    // ---- header ----
    @FXML private TextField tfTransactionNo;
    @FXML private DatePicker dpInquiryDate;
    @FXML private TextField tfCustomerName;

    // ---- table ----
    @FXML private TableView<Model_Sales_Quotation_Version_Giveaways> tblFollowUp;
    @FXML private TableColumn<Model_Sales_Quotation_Version_Giveaways, String> tblRowFUNo;   // No
    @FXML private TableColumn<Model_Sales_Quotation_Version_Giveaways, String> tblRowDesc;   // Description
    @FXML private TableColumn<Model_Sales_Quotation_Version_Giveaways, String> tblRowQty;    // Qty

    // ---- buttons ----
    @FXML private HBox hbButtons;
    @FXML private Button btnClose;
    @FXML private Button btnCLoseModal;

    // =========================================================================
    // Lifecycle
    // =========================================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initHeader();
        initTable();
        btnClose.setOnAction(e -> closeStage());
        btnCLoseModal.setOnAction(e -> closeStage());
    }

    /** Header fields are display-only. */
    private void initHeader() {
        tfTransactionNo.setEditable(false);
        tfCustomerName.setEditable(false);
        dpInquiryDate.setEditable(false);
        dpInquiryDate.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fdValue) {
                return fdValue == null ? "" : DATE_FORMAT.format(fdValue);
            }

            @Override
            public LocalDate fromString(String fsValue) {
                return null;   // never typed
            }
        });
        // the FXML disables the picker; keep it readable (not greyed out) but not editable
        dpInquiryDate.setDisable(false);
        dpInquiryDate.setMouseTransparent(true);
        dpInquiryDate.setFocusTraversable(false);
    }

    private void initTable() {
        tblRowFUNo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                String.valueOf(cd.getTableView().getItems().indexOf(cd.getValue()) + 1)));
        tblRowDesc.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                pick(cd.getValue(), DESC_GETTERS, DESC_COLUMNS, "")));
        tblRowQty.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                pick(cd.getValue(), QTY_GETTERS, QTY_COLUMNS, "0")));
        // The FXML gives every column maxWidth = Double.MAX_VALUE, and with the constrained policy that lets the first
        // column take the whole table. Fix the No and Qty widths: Description gets all the remaining space.
        tblRowFUNo.setMinWidth(45);
        tblRowFUNo.setMaxWidth(45);
        tblRowQty.setMinWidth(90);
        tblRowQty.setMaxWidth(90);
        tblRowFUNo.setResizable(false);
        tblRowQty.setResizable(false);
        tblRowDesc.setMaxWidth(Double.MAX_VALUE);
        tblFollowUp.setItems(giveaway_data);
        tblFollowUp.setPlaceholder(new Label("No non-itemized giveaways."));
        tblFollowUp.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // =========================================================================
    // Called by the caller before the stage is shown
    // =========================================================================
    /**
     * Gives the controller the MCSales that is open in the sales form and fills the form.
     * MCSales.loadSalesQuotationNonItemizedList() must have been called already (it reads the source quotation of the
     * loaded transaction); this method only reads the list.
     */
    public void setMCSales(MCSales foSales) {
        poSales = foSales;
        loadRecord();
    }

    /** Header "Customer Name" (the caller already has it on its own form). */
    public void setCustomerName(String fsName) {
        tfCustomerName.setText(fsName == null ? "" : fsName);
    }

    // =========================================================================
    // Loading
    // =========================================================================
    private void loadRecord() {
        giveaway_data.clear();
        tfTransactionNo.clear();
        dpInquiryDate.setValue(null);
        if (poSales == null) {
            return;
        }
        try {
            // Parent ID = the source quotation of the transaction; Date = the transaction date
            String lsSource = poSales.getMaster().getSourceNo();
            tfTransactionNo.setText(lsSource == null ? "" : lsSource);
            dpInquiryDate.setValue(toLocalDate(poSales.getMaster().getTransactionDate()));

            // getSalesQuotationNonItemizedList() calls computeTransaction(), which only runs in ADDNEW / UPDATE
            List<Model_Sales_Quotation_Version_Giveaways> laList = poSales.getSalesQuotationNonItemizedList();
            if (laList != null) {
                giveaway_data.setAll(laList);
            }
            System.out.println("[NONITEMIZE] rows = " + giveaway_data.size());
            dumpRow(giveaway_data.isEmpty() ? null : giveaway_data.get(0));
            tblFollowUp.refresh();
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionNonItemizeController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), MODULE_NAME, null);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    /**
     * First non-blank value for a cell: the typed getters (by reflection, so a name that does not exist is simply
     * skipped instead of breaking the build), then the raw columns. fsDefault when nothing has a value.
     */
    private static String pick(Model_Sales_Quotation_Version_Giveaways fo, String[] fasGetters, String[] fasColumns, String fsDefault) {
        if (fo == null) {
            return fsDefault;
        }
        for (String lsGetter : fasGetters) {
            try {
                Object loVal = fo.getClass().getMethod(lsGetter).invoke(fo);
                String lsVal = loVal == null ? "" : String.valueOf(loVal).trim();
                if (!lsVal.isEmpty()) {
                    return lsVal;
                }
            } catch (Exception ex) {
                // no such getter (or it threw): try the next one
            }
        }
        for (String lsColumn : fasColumns) {
            try {
                Object loVal = fo.getValue(lsColumn);
                String lsVal = loVal == null ? "" : String.valueOf(loVal).trim();
                if (!lsVal.isEmpty()) {
                    return lsVal;
                }
            } catch (Exception ex) {
                // no such column: try the next one
            }
        }
        return fsDefault;
    }

    /** Prints every public no-arg getter of the first row, so the real names show up in the console. */
    private static void dumpRow(Model_Sales_Quotation_Version_Giveaways fo) {
        if (fo == null) {
            System.out.println("[NONITEMIZE] list is empty: nothing to dump");
            return;
        }
        System.out.println("[NONITEMIZE] row class = " + fo.getClass().getName());
        for (java.lang.reflect.Method loMethod : fo.getClass().getMethods()) {
            if (loMethod.getName().startsWith("get") && loMethod.getParameterCount() == 0
                    && loMethod.getDeclaringClass() != Object.class) {
                try {
                    System.out.println("[NONITEMIZE]   " + loMethod.getName() + "() = " + loMethod.invoke(fo));
                } catch (Exception ex) {
                    System.out.println("[NONITEMIZE]   " + loMethod.getName() + "() threw " + ex.getClass().getSimpleName());
                }
            }
        }
    }

    /** java.sql.Date has no toInstant(): convert it with its own toLocalDate(). */
    private static LocalDate toLocalDate(java.util.Date fdDate) {
        if (fdDate == null) {
            return null;
        }
        if (fdDate instanceof java.sql.Date) {
            return ((java.sql.Date) fdDate).toLocalDate();
        }
        return fdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void closeStage() {
        Stage loStage = (Stage) btnClose.getScene().getWindow();
        Platform.runLater(loStage::close);
    }
}