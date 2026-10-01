package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.SalesQoutation;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Follow-up modal of a Sales Quotation.
 * Flow: SalesQoutation.newFollowUp() on open -> fill model -> SalesQoutation.saveFollowUp().
 * The table shows every follow-up already made on the opened quotation's current
 * version (SalesQoutation.getFollowUps()).
 *
 * Parent usage (after FXMLLoader.load()):
 *   ctrl.setGRider(oApp);
 *   ctrl.setSalesQuotation(oSalesController.SalesQoutation());
 *   if (!ctrl.loadForm()) return;
 */
public class SalesQoutationFollowUpController implements Initializable {

    private final String pxeModuleName = "Sales Quotation Follow-Up";

    /** TODO: replace with the real cFllwUpTp codes. {code, description} */
    private static final String[][] FOLLOWUP_TYPES = {
            {"0", "Call"}, {"1", "SMS"}, {"2", "Email"}, {"3", "Visit"}
    };

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private GRiderCAS oApp;
    private SalesQoutation poController;
    private JSONObject poJSON;

    @FXML private Button btnCLoseModal;
    @FXML private FontAwesomeIconView faAdd;
    @FXML private HBox hbButtons;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    @FXML private TextField tfTransactionNo;
    @FXML private TextField tfVersionNo;
    @FXML private DatePicker dpInquiryDate;
    @FXML private TextField tfFollowUpBy;
    @FXML private TextField tfCustomerName;
    @FXML private ComboBox<String> cmbFollowUpType;
    @FXML private TextArea taRemarks;
    @FXML private DatePicker dpNextFollowUp;
    @FXML private TableView<ModelTableMain> tblFollowUp;
    @FXML private TableColumn<ModelTableMain, String> tblRowFUNo;
    @FXML private TableColumn<ModelTableMain, String> tblRowFUDate;
    @FXML private TableColumn<ModelTableMain, String> tblRowFUNextDate;
    @FXML private TableColumn<ModelTableMain, String> tblRowFUType;
    @FXML private TableColumn<ModelTableMain, String> tblRowFURemarks;
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    public void setSalesQuotation(SalesQoutation foValue) {
        poController = foValue;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        poJSON = new JSONObject();
        btnSave.setOnAction(this::cmdButton_Click);
        btnCancel.setOnAction(this::cmdButton_Click);
        btnCLoseModal.setOnAction(this::cmdButton_Click);

        for (String[] laType : FOLLOWUP_TYPES) {
            cmbFollowUpType.getItems().add(laType[1]);
        }
        tfTransactionNo.setEditable(false);
        tfVersionNo.setEditable(false);
        tfFollowUpBy.setEditable(false);
        tfCustomerName.setEditable(false);
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpNextFollowUp);
        JFXUtil.setActionListener(this::datepicker_Action, dpNextFollowUp);
        initTableFolloUpHistory();
    }

    private void datepicker_Action(ActionEvent event) {
        DatePicker source = (DatePicker) event.getSource();
        LocalDate loSelected = source.getValue();
        if (loSelected == null) return;

        try {
            switch (source.getId()) {
                case "dpNextFollowUp":
                    // Next follow-up must not be earlier than the follow-up date
                    LocalDate loFollowUpDate = dpInquiryDate.getValue() != null
                            ? dpInquiryDate.getValue() : LocalDate.now();
                    if (loSelected.isBefore(loFollowUpDate)) {
                        ShowMessageFX.Warning("Next follow-up date cannot be earlier than the follow-up date.",
                                pxeModuleName, null);
                        source.setValue(null); // listener returns early on null
                        return;
                    }
                    break;

                default:
                    return;
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    /**
     * Called by the parent after the setters.
     * @return true if the form is ready to be shown; false if it must not be shown
     *         (a message has already been displayed). The stage is NOT closed here
     *         because it does not exist yet when this runs.
     */
    public boolean loadForm() {
        try {
            if (poController == null) {
                ShowMessageFX.Warning("No quotation is loaded.", pxeModuleName, null);
                return false;
            }
            if (!poController.isLatestVersion()) {
                ShowMessageFX.Warning("Only the latest version can be followed up.", pxeModuleName, null);
                return false;
            }

            poJSON = poController.newFollowUp();
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                return false;
            }

            tfTransactionNo.setText(poController.getModel().getTransactionNo());
            tfVersionNo.setText(poController.getModel().getVersion().toString());
            tfFollowUpBy.setText(poController.FollowUp().getSysUser(poController.FollowUp().getModel().getFollowUpBy()));
            if (poController.getModel().Client() != null) {
                tfCustomerName.setText(poController.getModel().Client().getCompanyName());
            }
            dpInquiryDate.setValue(LocalDate.now());

            loadTableFollowUpHistory();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            return false;
        }
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnSave":
                    saveFollowUp();
                    break;
                case "btnCancel":
                case "btnCLoseModal":
                    CommonUtils.closeStage(btnCLoseModal);
                    break;
                default:
                    ShowMessageFX.Warning("Unknown button action.", pxeModuleName, null);
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void saveFollowUp() throws Exception {
        int lnType = cmbFollowUpType.getSelectionModel().getSelectedIndex();
        if (lnType < 0) {
            ShowMessageFX.Warning("Follow-Up Method must not be empty.", pxeModuleName, null);
            return;
        }

        // inferred setter names - check against Model_Sales_Quotation_FollowUp
        poController.FollowUp().getModel().setFollowUpDate(
                SQLUtil.toDate(dpInquiryDate.getValue().toString(), SQLUtil.FORMAT_SHORT_DATE));
        poController.FollowUp().getModel().setFollowUpType(FOLLOWUP_TYPES[lnType][0]);
        poController.FollowUp().getModel().setRemarks(taRemarks.getText());

        if (dpNextFollowUp.getValue() != null) {
            poController.FollowUp().getModel().setNextFollowUpDate(
                    SQLUtil.toDate(dpNextFollowUp.getValue().toString(), SQLUtil.FORMAT_SHORT_DATE));
        }

        poJSON = poController.saveFollowUp();
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
            return;
        }

        ShowMessageFX.Information("Follow-up has been saved.", pxeModuleName, null);
        CommonUtils.closeStage(btnCLoseModal);
    }

    // =====================================================================
    // follow-up history table
    // =====================================================================
    private void initTableFolloUpHistory() {
        JFXUtil.setColumnCenter(tblRowFUNo, tblRowFUDate, tblRowFUNextDate, tblRowFUType, tblRowFURemarks);
        JFXUtil.setColumnsIndexAndDisableReordering(tblFollowUp);// after setColumnCenter so it isn't overwritten

        tblFollowUp.setItems(main_data);
    }

    /**
     * Lists every follow-up already made on the opened quotation's current
     * version, oldest first (1 = first follow-up of this version). The rows come
     * from SalesQoutation.getFollowUps(), which filters by the version's number.
     * Columns: No. | Date | Next Follow-up | Method | Remarks.
     */
    private void loadTableFollowUpHistory() {
        main_data.clear();
        try {
            List<JSONObject> laFollowUps = poController.getFollowUps();   // newest first

            // walk backwards so the oldest is row 1
            int lnRow = 1;
            for (int lnCtr = laFollowUps.size() - 1; lnCtr >= 0; lnCtr--) {
                JSONObject loRow = laFollowUps.get(lnCtr);
                main_data.add(new ModelTableMain(
                        String.valueOf(lnRow++),
                        toDisplayDate(loRow.get("dFollowUp")),
                        toDisplayDate(loRow.get("dNextFlup")),
                        typeDescription(loRow.get("cFllwUpTp")),
                        loRow.get("sRemarksx") == null ? "" : String.valueOf(loRow.get("sRemarksx")),
                        "", "", "", "", "", "", "", "", ""));   // ModelTableMain takes 14 values
            }

            if (!main_data.isEmpty()) {
                JFXUtil.selectAndFocusRow(tblFollowUp, main_data.size() - 1);   // latest follow-up
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    /** "2026-10-01 00:00:00" -> "10/01/2026"; null/blank/invalid -> "" */
    private static String toDisplayDate(Object foValue) {
        if (foValue == null) return "";
        String lsValue = String.valueOf(foValue).trim();
        if (lsValue.length() < 10) return "";
        try {
            return LocalDate.parse(lsValue.substring(0, 10)).format(DISPLAY_DATE);
        } catch (Exception ex) {
            return lsValue;
        }
    }

    private static String typeDescription(Object foCode) {
        if (foCode == null) return "";
        String lsCode = String.valueOf(foCode);
        for (String[] laType : FOLLOWUP_TYPES) {
            if (laType[0].equals(lsCode)) return laType[1];
        }
        return lsCode;
    }
}