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
 * Usage (see SalesTransactionsMCController.showNonItemizeGiveaways()):
 *   controller.setMCSales(oSalesController.MCSales());   // BEFORE the stage is shown
 *   controller.setCustomerName(...);                      // optional header values
 *   stage.showAndWait();
 *
 * ASSUMED (the Model_Sales_Quotation_Version_Giveaways class was not provided): the three columns below are read through
 * the raw getValue() of the model. If your table names them differently, change the three COL_* constants (one place).
 */
public class SalesTransactionNonItemizeController implements Initializable {

    private static final String MODULE_NAME = SalesTransactionStatic.MODULE_NAME;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(SalesTransactionStatic.DATE_PATTERN);

    /** ASSUMED raw column names of Sales_Quotation_Version_Giveaways. */
    private static final String COL_DESCRIPTION = "sDescript";
    private static final String COL_QUANTITY = "nQuantity";
    private static final String COL_STATUS = "cRecdStat";

    private MCSales poSales;
    private final ObservableList<Model_Sales_Quotation_Version_Giveaways> giveaway_data = FXCollections.observableArrayList();

    // ---- header ----
    @FXML private TextField tfTransactionNo;
    @FXML private DatePicker dpInquiryDate;
    @FXML private TextField tfCustomerName;

    // ---- table ----
    @FXML private TableView<Model_Sales_Quotation_Version_Giveaways> tblFollowUp;
    @FXML private TableColumn<Model_Sales_Quotation_Version_Giveaways, String> tblRowFUNo;
    @FXML private TableColumn<Model_Sales_Quotation_Version_Giveaways, String> tblRowFUDate;       // Description
    @FXML private TableColumn<Model_Sales_Quotation_Version_Giveaways, String> tblRowFUNextDate;   // Qty
    @FXML private TableColumn<Model_Sales_Quotation_Version_Giveaways, String> tblRowFUType;       // Status

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
        tblRowFUDate.setCellValueFactory(cd -> raw(cd.getValue(), COL_DESCRIPTION, ""));
        tblRowFUNextDate.setCellValueFactory(cd -> raw(cd.getValue(), COL_QUANTITY, "0"));
        tblRowFUType.setCellValueFactory(cd -> raw(cd.getValue(), COL_STATUS, ""));
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
            tblFollowUp.refresh();
        } catch (Exception ex) {
            Logger.getLogger(SalesTransactionNonItemizeController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), MODULE_NAME, null);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    /** Raw model column as text; fsDefault when the column is missing / null / blank. */
    private static ObservableValue<String> raw(Model_Sales_Quotation_Version_Giveaways fo, String fsColumn, String fsDefault) {
        try {
            Object loVal = fo == null ? null : fo.getValue(fsColumn);
            String lsVal = loVal == null ? "" : String.valueOf(loVal).trim();
            return new ReadOnlyStringWrapper(lsVal.isEmpty() ? fsDefault : lsVal);
        } catch (Exception ex) {
            return new ReadOnlyStringWrapper(fsDefault);
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