/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.tbjhandler.Services.TBJControllers;
import org.json.simple.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.tbjhandler.constant.TBJ_Constant;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class for the TBJ (Transaction Book Journal) Parameter settings.
 * * <p>This controller manages the user interface and business logic integration for 
 * configuring system parameters. It facilitates the mapping of transaction sources, 
 * database tables, and account titles (Debit/Credit) to specific journal entries.</p>
 * * <h3>Key Responsibilities:</h3>
 * <ul>
 * <li><b>State Management:</b> Orchestrates transitions between Edit Modes (Add New, Update, Ready).</li>
 * <li><b>Data Synchronization:</b> Coordinates data flow between the FXML view components 
 * and the underlying {@link org.guanzon.cas.tbjhandler.Services.TBJControllers} business logic.</li>
 * <li><b>Search & Lookup:</b> Implements contextual search functionality for Industry, 
 * Company, Category, and Chart of Accounts.</li>
 * <li><b>Validation:</b> Ensures data integrity for required fields and status-based 
 * record locking (Open vs. Confirmed status).</li>
 * </ul>
 * * <p>This class implements {@link ScreenInterface} to allow the parent application 
 * to inject global application drivers (GRider) and context IDs (Industry/Company).</p>
 * 
 * @author Teejei De Celis (mdot223)
 * @since 2026-02-05
 * @startDate 2026-02-05
 * @endDate   2026-02-06
 * @version 1.0
 * @see Initializable
 * @see ph.com.guanzongroup.integsys.views.ScreenInterface
 */
public class TBJ_ParameterController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private TBJControllers poTBJControllers;
    private String psFormName = "TBJ Parameter";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    private int pnSelectedDetail = 0;
    private String psActiveField = "";
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    unloadForm poUnload = new unloadForm();

    ObservableList<String> AccountType = FXCollections.observableArrayList(
            "Debit",
            "Credit");

    @FXML
    private AnchorPane AnchorMain,
            apBrowse,
            apButton,
            apMaster,
            apDetail,
            apTableDetail;

    @FXML
    private TextField tfSearchTransaction,
            tfSearchSource,
            tfSourceCode,
            tfCategory,
            tfTransactionNo,
            tfFieldName,
            tfAccountTitle,
            tfTableName,
            tfIndustry;

    @FXML
    private TextArea taRemarks;

    @FXML
    private Label lblSource,
            lblStatus;

    @FXML
    private Button btnBrowse,
            btnNew,
            btnUpdate,
            btnSave,
            btnCancel,
            btnClose,
            btnVoid,
            btnConfirm;

    @FXML
    private FontAwesomeIconView btnStatusGlyph;

    @FXML
    private CheckBox cbIsRequired,
            cbIsActive;

    @FXML
    private ComboBox cmbAccountType;

    @FXML
    private TableView<ModelTableDetail> tblDetails;

    @FXML
    private TableColumn<ModelTableDetail, String> index00,
            index01,
            index02,
            index03,
            index04,
            index05,
            index06;

    /**
     * Sets the GRider application driver instance.
     *
     * * @param foValue The GRider instance.
     */
    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    /**
     * Sets the Industry ID for the transaction context.
     *
     * * @param fsValue The industry ID.
     */
    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    /**
     * Sets the Company ID for the transaction context.
     *
     * * @param fsValue The company ID.
     */
    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    /**
     * Sets the Category ID for the transaction context.
     *
     * * @param fsValue The category ID.
     */
    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class after its root element has been
     * completely processed.
     * <p>
     * This method sets up the initial UI state and internal data structures by:
     * <ul>
     * <li><b>UI Cleanup:</b> Invoking {@link #ClearAll()} to reset all text
     * fields and variables.</li>
     * <li><b>Object Lifecycle:</b> Initializing core controller objects via
     * {@link #initializeObject()}.</li>
     * <li><b>Event Wiring:</b> Binding action listeners to buttons, checkboxes,
     * and table interactions.</li>
     * <li><b>Component Setup:</b> Configuring the TableView columns and focus
     * listeners.</li>
     * <li><b>Default State:</b> Using {@link Platform#runLater} to
     * programmatically trigger the 'New' button action, ensuring the form
     * starts in "Add New" mode once the layout is ready.</li>
     * </ul>
     *
     * * @param url The location used to resolve relative paths for the root
     * object.
     * @param rb The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        ClearAll();
        initComboBox();
        initializeObject();
        initButtonsClickActions();
        initFields();
        initTableDetail();
        initCheckBox();
        lblSource.setText("");
        Platform.runLater(() -> btnNew.fire());
    }

    /**
     * Initializes the TBJ controller and transaction objects.
     */
    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            poTBJControllers = new TBJControllers(poApp, logWrapper);
            poJSON = poTBJControllers.TBJParameter().InitTransaction();
            poTBJControllers.TBJParameter().setTransactionStatus("130");
//            poTBJControllers.TBJParameter().Master().setIndustryID(psIndustryID);
//            lblSource.setText(poGLControllers.PaymentRequest().Master().Company().getCompanyName() + " - " + poGLControllers.PaymentRequest().Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BrandController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Dynamically adjusts the visibility and layout management of toolbar
     * buttons based on the current edit mode and transaction status.
     * <p>
     * This method implements a state-driven UI pattern:
     * <ul>
     * <li><b>Edit Modes (ADDNEW/UPDATE):</b> Hides navigation buttons (Browse,
     * New, Close) and displays action buttons (Save, Cancel).</li>
     * <li><b>Ready Mode:</b> Displays the "Update" button to allow transitions
     * into edit mode.</li>
     * <li><b>Status-Based Controls:</b>
     * <ul>
     * <li>If the transaction is {@code OPEN}, it reveals "Void" and "Confirm"
     * buttons.</li>
     * <li>If the transaction is {@code CONFIRMED}, it only reveals the "Void"
     * button, as confirmation has already occurred.</li>
     * </ul>
     * </li>
     * </ul>
     * <p>
     * <b>Technical Note:</b> Uses {@code CustomCommonUtil} to sync both the
     * {@code visible} property (visual presence) and the {@code managed}
     * property (space reservation in the layout container).</p>
     *
     * * @param fnEditMode The current state of the form as defined in
     * {@link EditMode}.
     */
    private void initButtons(int fnEditMode) {
        try {
            boolean lbShow = (fnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);

            CustomCommonUtil.setVisible(!lbShow, btnBrowse, btnClose, btnNew);
            CustomCommonUtil.setManaged(!lbShow, btnBrowse, btnClose, btnNew);

            CustomCommonUtil.setVisible(lbShow, btnSave, btnCancel, btnVoid, btnConfirm);
            CustomCommonUtil.setManaged(lbShow, btnSave, btnCancel, btnVoid, btnConfirm);

            CustomCommonUtil.setVisible(false, btnUpdate, btnVoid, btnConfirm);
            CustomCommonUtil.setManaged(false, btnUpdate, btnVoid, btnConfirm);
            if (fnEditMode == EditMode.READY) {
                CustomCommonUtil.setVisible(true, btnUpdate);
                CustomCommonUtil.setManaged(true, btnUpdate);
                if (poTBJControllers.TBJParameter().Master().getTransactionStatus().equals(TBJ_Constant.OPEN)) {
                    CustomCommonUtil.setVisible(true, btnVoid, btnConfirm);
                    CustomCommonUtil.setManaged(true, btnVoid, btnConfirm);
                } else if (poTBJControllers.TBJParameter().Master().getTransactionStatus().equals(TBJ_Constant.CONFIRMED)) {
                    CustomCommonUtil.setVisible(true, btnVoid);
                    CustomCommonUtil.setManaged(true, btnVoid);
                }
            }
            if(poTBJControllers.TBJParameter().Master().getTransactionStatus().equals(TBJ_Constant.VOID)){
                CustomCommonUtil.setVisible(false, btnUpdate);
                CustomCommonUtil.setManaged(false, btnUpdate);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Aggregates and assigns action event handlers to the form's primary
     * command buttons.
     * <p>
     * This method streamlines the initialization process by:
     * <ul>
     * <li>Collecting all toolbar buttons (Browse, New, Update, Save, Cancel,
     * and Close) into a unified {@link List}.</li>
     * <li>Iteratively applying the {@link #handleButtonAction(ActionEvent)}
     * reference to each button's {@code setOnAction} property.</li>
     * </ul>
     * <p>
     * <b>Design Note:</b> Using a list-based assignment ensures that all
     * primary interactions follow a consistent execution path through the
     * central dispatcher, making the code easier to maintain and debug.</p>
     */
    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnClose,btnConfirm,btnVoid);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    /**
     * Central event handler for all button-driven actions in the interface.
     * <p>
     * This method manages the high-level workflow of the transaction parameter
     * form, including lifecycle transitions and data persistence. Key
     * functionalities include:
     * <ul>
     * <li><b>Navigation & Cleanup:</b> Handles form closure with confirmation
     * and clearing of UI states during cancellations.</li>
     * <li><b>Transaction Lifecycle:</b> Manages "New", "Update", "Save",
     * "Confirm", and "Void" operations by communicating with the business logic
     * controller.</li>
     * <li><b>Search (Browse):</b> Executes context-sensitive searches based on
     * {@code psActiveField} (e.g., searching by Transaction vs. Source).</li>
     * <li><b>User Permissions:</b> Implements logic to prompt for immediate
     * confirmation after saving if the user has sufficient access levels (above
     * {@code ENCODER}).</li>
     * <li><b>State Synchronization:</b> Automatically refreshes button
     * visibility and field editability by invoking {@link #initButtons} and
     * {@link #initFields} after every successful action.</li>
     * </ul>
     * <p>
     * <b>Implementation Note:</b> Uses {@code poJSON} results from the
     * controller to determine success or failure, displaying visual feedback
     * via {@link ShowMessageFX}.</p>
     *
     * * @param event The {@link ActionEvent} triggered by the user
     * interaction.
     * @throws SQLException If database communication fails during transaction
     * updates.
     * @throws GuanzonException If business rule violations occur.
     * @throws CloneNotSupportedException If object cloning fails during state
     * transitions.
     */
    private void handleButtonAction(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
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
                case "btnBrowse":
                    switch (psActiveField) {
                        case "tfSearchTransaction":
                        case "":
                            poJSON = poTBJControllers.TBJParameter().SearchTransaction(tfSearchTransaction.getText(), psActiveField);
                            break;
                        case "tfSearchSource":
                            poJSON = poTBJControllers.TBJParameter().SearchTransaction(tfSearchSource.getText(), psActiveField);
                            break;
                    }
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poTBJControllers.TBJParameter().getEditMode();
                    loadTableDetail();
                    LoadMaster();
                    LoadDetail();
                    break;
                case "btnNew":
                    ClearAll();
                    poJSON = poTBJControllers.TBJParameter().NewTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poTBJControllers.TBJParameter().getEditMode();

                    loadTableDetail();
                    LoadMaster();
                    LoadDetail();
                    initButtons(pnEditMode);
                    break;
                case "btnUpdate":
                    if(poTBJControllers.TBJParameter().Master().getTransactionNo()== null ||
                            poTBJControllers.TBJParameter().Master().getTransactionNo().isEmpty()){
                        ShowMessageFX.Warning("No transaction was loaded. ", psFormName, null);
                        return;
                    }
                    
                    poJSON = poTBJControllers.TBJParameter().UpdateTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poTBJControllers.TBJParameter().getEditMode();
                    LoadMaster();
                    LoadDetail();
                    loadTableDetail();
                    initButtons(pnEditMode);
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo("Are you sure you want to cancel? \nAny data you have entered will not be saved.", psFormName,null )) {
                        ClearAll();
                        pnEditMode = EditMode.UNKNOWN;
                        initButtons(pnEditMode);
                    }
                    break;
                case "btnSave":
                    poJSON = poTBJControllers.TBJParameter().SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                    if (poApp.getUserLevel() > UserRight.ENCODER) {
                        if (ShowMessageFX.YesNo(null, psFormName, "Do you want to confirm this transaction?")) {
                            try {
                                poJSON = poTBJControllers.TBJParameter().ConfirmTransaction("");
                            } catch (ParseException ex) {
                                Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                return;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        }
                    }

                    ClearAll();
                    btnNew.fire();
                    break;

                case "btnVoid":
                    try {
                    poJSON = poTBJControllers.TBJParameter().VoidTransaction("");

                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        ClearAll();
                        btnNew.fire();
                } catch (ParseException ex) {
                    Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
                case "btnConfirm":
                    try {
                    poJSON = poTBJControllers.TBJParameter().ConfirmTransaction("");

                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        ClearAll();
                        btnNew.fire();
                } catch (ParseException ex) {
                    Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(ex.getMessage(), psFormName, null);
                }
                break;

                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            initButtons(pnEditMode);
            initFields();
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, psFormName);
        }
    }

    /**
     * Orchestrates the initialization of UI component states, event listeners,
     * and styling.
     * <p>
     * This method is responsible for the dynamic behavior of the form based on
     * the current {@code pnEditMode} and the transaction's status. It performs
     * the following:
     * <ul>
     * <li><b>State Management:</b> Enables or disables input fields
     * (TextFields, CheckBoxes, ComboBoxes) based on whether the form is in an
     * editable mode (ADDNEW or UPDATE).</li>
     * <li><b>Conditional Logic:</b>
     * <ul>
     * <li>If the transaction is {@code CONFIRMED}, it locks the master panel
     * and limits interaction to specific detail fields like the 'Active'
     * checkbox.</li>
     * <li>If the transaction is {@code OPEN} or the mode is {@code READY},
     * specific controls like the 'Active' checkbox are further restricted.</li>
     * </ul>
     * </li>
     * <li><b>Event Binding:</b>
     * <ul>
     * <li>Attaches {@link #txtField_KeyPressed(KeyEvent)} to all primary input
     * fields.</li>
     * <li>Applies {@code txtArea_Focus} and {@code txtField_Focus} listeners
     * via {@link JFXUtil}.</li>
     * <li>Assigns the {@link #comboBoxActionListener} and configures the
     * ComboBox cell styling.</li>
     * <li>Sets the mouse click handler for the {@code tblDetails}
     * {@link TableView}.</li>
     * </ul>
     * </li>
     * <li><b>Utility Integration:</b> Utilizes {@code makeClearableReadOnly} to
     * ensure the Field Name cannot be typed into manually while still allowing
     * clearance.</li>
     * </ul>
     *
     * * @throws SQLException If a database error occurs while checking
     * transaction status.
     * @throws GuanzonException If a business logic error occurs during
     * initialization.
     */
    private void initFields() {
        try {
            //        try {
            boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
            JFXUtil.setDisabled(!isEditable,
                    tfSourceCode,
                    tfCategory,
                    tfTransactionNo,
                    tfFieldName,
                    tfAccountTitle,
                    tfTableName,
                    cbIsActive,
                    cbIsRequired,
                    cmbAccountType,
                    taRemarks,
                    tfIndustry
            );

            if (pnEditMode == EditMode.ADDNEW) {
                apMaster.setDisable(false);
                apDetail.setDisable(false);
                cbIsActive.setDisable(isEditable);
            } else if (pnEditMode == EditMode.UPDATE && TBJ_Constant.CONFIRMED.equals(poTBJControllers.TBJParameter().Master().getTransactionStatus())) {
                JFXUtil.setDisabledExcept(true,
                        apMaster,
                        taRemarks);
                JFXUtil.setDisabledExcept(true,
                        apDetail,
                        cbIsActive);

                if (poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getEditMode() == EditMode.ADDNEW) {
                    JFXUtil.setDisabledExcept(false,
                            apDetail);
                }
            } else if (pnEditMode == EditMode.UPDATE && TBJ_Constant.OPEN.equals(poTBJControllers.TBJParameter().Master().getTransactionStatus())) {
                apMaster.setDisable(false);
                apDetail.setDisable(false);
            }

            List<TextField> loTxtField = Arrays.asList(tfTableName, tfIndustry,tfCategory, tfSourceCode, tfAccountTitle, tfFieldName);
            loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));

            JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
            JFXUtil.setFocusListener(txtField_Focus, tfSearchSource, tfSearchTransaction);

            tblDetails.setOnMouseClicked(this::tblDetails_Clicked);
            makeClearableReadOnly(tfFieldName);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }

    }

    /**
     * Initializes the Account Type ComboBox and binds it to the detail model.
     * <p>
     * This method performs the following:
     * <ul>
     * <li><b>Data Binding:</b> Assigns the {@code AccountType} ObservableList
     * (e.g., "Debit", "Credit") as the data source of
     * {@code cmbAccountType}.</li>
     *
     * <li><b>UI Styling:</b> Applies custom styling using
     * {@code JFXUtil.initComboBoxCellDesignColor}.</li>
     *
     * <li><b>Selection Handling:</b> Attaches an {@code setOnAction} listener
     * that captures the selected index whenever the user changes the
     * value.</li>
     *
     * <li><b>Model Update:</b> Updates the currently selected detail record by
     * setting {@code setAccountType} using the selected index (stored as a
     * {@link String}).</li>
     *
     * <li><b>UI Refresh:</b> Calls {@link #loadTableDetail()} to refresh the
     * TableView and reflect the updated account type selection.</li>
     * </ul>
     *
     * <p>
     * <b>Safety Check:</b> Ensures that {@code pnSelectedDetail} is valid
     * (greater than or equal to 0) before applying updates.</p>
     *
     * <p>
     * <b>Error Handling:</b> Catches and logs {@link SQLException} and
     * {@link GuanzonException} to prevent application crashes during data/model
     * updates.</p>
     */
    private void initComboBox(){
        cmbAccountType.setItems(AccountType); // your ObservableList
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAccountType);

        // Attach combo box listener
        cmbAccountType.setOnAction(event -> {
            try {
                if (pnSelectedDetail >= 0) { // safety check
                    if(cmbAccountType.getSelectionModel().getSelectedIndex() == 0){
                            poTBJControllers.TBJParameter()
                                .Detail(pnSelectedDetail)
                                .setAccountType("D");
                    }else if(cmbAccountType.getSelectionModel().getSelectedIndex() == 1){
                            poTBJControllers.TBJParameter()
                                .Detail(pnSelectedDetail)
                                .setAccountType("C");
                    }
                    poJSON = poTBJControllers.TBJParameter().checkDuplicateDetail();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    loadTableDetail(); // refresh table after change
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(TBJ_ParameterController.class.getName())
                        .log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), psFormName, null);
            }
        });
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAccountType);
    
    }


    /**
     * A focus change listener for {@link TextField} components that tracks the
     * currently active search field.
     * <p>
     * Utilizing {@code JFXUtil.FocusListener}, this handler specifically
     * targets "Lost Focus" events to update the {@code psActiveField} state
     * variable. This tracking is essential for:
     * <ul>
     * <li>Identifying which search criteria (Transaction vs. Source) should be
     * prioritized during global search executions.</li>
     * <li>Maintaining state for context-sensitive keyboard shortcuts.</li>
     * </ul>
     * <p>
     * <b>Managed Fields:</b></p>
     * <ul>
     * <li>{@code tfSearchTransaction}: Sets active field for transaction-based
     * lookups.</li>
     * <li>{@code tfSearchSource}: Sets active field for source-based
     * lookups.</li>
     * </ul>
     */
    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                switch (lsID) {
                    case "tfSearchTransaction":
                        psActiveField = lsID;
                        break;
                    case "tfSearchSource":
                        psActiveField = lsID;
                        break;
                }
            });

    /**
     * Initializes action listeners for the detail-level checkboxes.
     * <p>
     * This method binds the 'Active' and 'Required' {@link CheckBox} components
     * to the underlying data model, but only when the application is in
     * {@code ADDNEW} or {@code UPDATE} mode.
     * <p>
     * When a checkbox state changes:
     * <ul>
     * <li>The corresponding boolean property in the current
     * {@code pnSelectedDetail} record is updated via the controller.</li>
     * <li>{@link #LoadDetail()} is called to synchronize the UI and ensure any
     * dependent logic is refreshed.</li>
     * </ul>
     * <p>
     * <b>Side Effects:</b> If an error occurs during model update, the
     * exception is logged and the UI state might become desynchronized from the
     * model.</p>
     *
     * * @throws SQLException if a database access error occurs.
     * @throws GuanzonException if a business logic error is encountered.
     */
    private void initCheckBox() {
        
        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            cbIsActive.setOnAction(event -> {
                try {
                    poTBJControllers.TBJParameter().Detail(pnSelectedDetail).isActive(cbIsActive.isSelected());
                    loadTableDetail();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            cbIsRequired.setOnAction(event -> {
                try {
                    poTBJControllers.TBJParameter().Detail(pnSelectedDetail).isRequired(cbIsRequired.isSelected());
                    loadTableDetail();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    /**
     * A focus change listener for {@link TextArea} components that manages data
     * persistence and user selection behavior.
     * <p>
     * When the focus state changes, this listener performs the following:
     * <ul>
     * <li><b>Gained Focus (nv = true):</b> Automatically selects all text
     * within the TextArea to facilitate quick replacement or editing.</li>
     * <li><b>Lost Focus (nv = false):</b> Captures the current text and updates
     * the corresponding "Master" record in the controller. Specifically, it
     * maps {@code taRemarks} to the Remarks property of the transaction.</li>
     * </ul>
     * <p>
     * <b>Technical Note:</b> It uses
     * {@code ((ReadOnlyBooleanPropertyBase) o).getBean()} to dynamically
     * identify the source control, allowing a single listener to be reused
     * across multiple TextAreas.</p>
     *
     * * @throws SQLException If a database error occurs during the update.
     * @throws GuanzonException If a business logic violation occurs.
     */
    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextArea = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextAreaID = loTextArea.getId();
        String lsValue = loTextArea.getText();
        if (lsValue == null) {
            return;
        }
        try {
            if (!nv) {
                /*Lost Focus*/
                switch (lsTextAreaID) {
                    case "taRemarks":
                        poTBJControllers.TBJParameter().Master().setRemarks(lsValue);
                        break;
                }
            } else {
                loTextArea.selectAll();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    };

    /**
     * Handles keyboard events for various text input fields to trigger search
     * and lookup actions.
     * <p>
     * This method listens for specific keys ({@code TAB}, {@code ENTER}, and
     * {@code F3}) to initiate asynchronous searches via the controller.
     * Depending on the focused field, it performs:
     * <ul>
     * <li><b>Category Search:</b> Lookups based on {@code tfCategory} and
     * updates the master record.</li>
     * <li><b>Source Code Search:</b> Lookups based on
     * {@code tfSourceCode}.</li>
     * <li><b>Account Chart Search:</b> Filters account titles for the specific
     * detail row.</li>
     * <li><b>Table & Field Search:</b> Dynamically retrieves available database
     * tables and fields, ensuring that a Table Name is selected before allowing
     * a Field Name lookup.</li>
     * </ul>
     * <p>
     * Errors during search (e.g., no records found) are caught from the
     * {@code poJSON} result and displayed to the user via
     * {@link ShowMessageFX}.
     * </p>
     *
     * * @param event The {@link KeyEvent} containing the key code and source
     * field information.
     */
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
                    case ENTER:
                        
                    case F3:
                        switch (txtFieldID) {
                            case "tfIndustry":
                                poJSON = poTBJControllers.TBJParameter().SearchIndustry(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                }
                                tfIndustry.setText(poTBJControllers.TBJParameter().Master().Industry().getDescription());
                                return;
                            case "tfCategory":
                                poJSON = poTBJControllers.TBJParameter().SearchCategory(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                }
                                tfCategory.setText(poTBJControllers.TBJParameter().Master().Category().getDescription());
                                return;

                            case "tfSourceCode":
                                poJSON = poTBJControllers.TBJParameter().SearchSourceCode(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                }
                                tfSourceCode.setText(poTBJControllers.TBJParameter().Master().TransactionSource().getSourceName());
                                tfTableName.clear();
                                return;

                            case "tfAccountTitle":
                                poJSON = poTBJControllers.TBJParameter().SearchAccountChart(lsValue, false, pnSelectedDetail);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                }
                                tfAccountTitle.setText(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).AccountChart().getDescription());
                                
                                poJSON = poTBJControllers.TBJParameter().checkDuplicateDetail();
                                 if("error".equals(poJSON.get("result"))){
                                      ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                      tfAccountTitle.requestFocus();
                                      tfAccountTitle.selectAll();
                                      return;
                                 }
                                 loadTableDetail();
                                return;

                            case "tfTableName":
                                poJSON = poTBJControllers.TBJParameter().SearchSourceCodeTable(lsValue, pnSelectedDetail);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                }
                                tfTableName.setText(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm().trim().replace("_", " "));
                                poJSON = poTBJControllers.TBJParameter().checkDuplicateDetail();
                                 if("error".equals(poJSON.get("result"))){
                                      ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                      tfAccountTitle.requestFocus();
                                      tfAccountTitle.selectAll();
                                      return;
                                 }
                                loadTableDetail();
                                return;
                            case "tfFieldName":
                                if (poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm() == null
                                        || poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm().isEmpty()) {
                                    ShowMessageFX.Warning("Table Name is not set!", psFormName, null);
                                    return;
                                }
                                
                                poTBJControllers.TBJParameter().show(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm().trim().replace(" ", "_"), pnSelectedDetail);
                                tfFieldName.setText(poTBJControllers.TBJParameter().getFieldName(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getDerivedField(), pnSelectedDetail));
                                poJSON = poTBJControllers.TBJParameter().checkDuplicateDetail();
                                 if("error".equals(poJSON.get("result"))){
                                      ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                      tfAccountTitle.requestFocus();
                                      tfAccountTitle.selectAll();
                                      return;
                                 }
                                loadTableDetail();
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
            } catch (SQLException | GuanzonException | ExceptionInInitializerError ex) {
                Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), psFormName, null);
            }
        }
    }

    /**
     * Populates the primary header (Master) fields with data from the current
     * transaction.
     * <p>
     * This method retrieves high-level transaction data from the controller and
     * updates the following UI elements:
     * <ul>
     * <li><b>Transaction Identifiers:</b> Sets the Transaction Number and
     * Source Code.</li>
     * <li><b>Classification:</b> Loads the Category description, defaulting to
     * empty if null.</li>
     * <li><b>Notes:</b> populates the Remarks {@link TextArea}.</li>
     * <li><b>Visual Status:</b> Translates the numeric transaction status into
     * a human-readable string (e.g., "VOID", "OPEN", "CONFIRMED") and updates
     * the status label.</li>
     * </ul>
     * <p>
     * <b>Implementation Note:</b> Uses a {@code switch} expression based on
     * {@link TBJ_Constant} to ensure status labels remain consistent with
     * business logic constants.</p>
     *
     * * @throws SQLException If a database access error occurs during record
     * retrieval.
     * @throws GuanzonException If an error occurs within the core business
     * logic.
     * @throws NullPointerException If the Master object or its sub-properties
     * are uninitialized.
     */
    private void LoadMaster() {
        try {
            tfTransactionNo.setText(poTBJControllers.TBJParameter().Master().getTransactionNo());
            tfIndustry.setText(poTBJControllers.TBJParameter().Master().Industry().getDescription() == null ? ""
                    : poTBJControllers.TBJParameter().Master().Industry().getDescription());
            tfSourceCode.setText(
                    poTBJControllers.TBJParameter().Master().getSourceCode() == null ? ""
                    : poTBJControllers.TBJParameter().Master().getSourceCode());
            tfCategory.setText(
                    poTBJControllers.TBJParameter().Master().Category().getDescription() == null ? ""
                    : poTBJControllers.TBJParameter().Master().Category().getDescription());
            tfSourceCode.setText(
                    poTBJControllers.TBJParameter().Master().getSourceCode() == null ? ""
                    : poTBJControllers.TBJParameter().Master().getSourceCode());

            taRemarks.setText(poTBJControllers.TBJParameter().Master().getRemarks() == null ? ""
                    : poTBJControllers.TBJParameter().Master().getRemarks());

            String lsStatus = "";
            switch (poTBJControllers.TBJParameter().Master().getTransactionStatus()) {
                case TBJ_Constant.VOID:
                    lsStatus = "VOID";
                    break;
                case TBJ_Constant.OPEN:
                    lsStatus = "OPEN";
                    break;
                case TBJ_Constant.CONFIRMED:
                    lsStatus = "CONFIRMED";
                    break;
            }
            lblStatus.setText(lsStatus);
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }
    }

    /**
     * Populates the detail input fields with data from the currently selected
     * record.
     * <p>
     * This method retrieves data from the underlying controller based on
     * {@code pnSelectedDetail} and performs the following UI updates:
     * <ul>
     * <li><b>Account Title:</b> Sets text from the account chart description,
     * defaulting to empty if null.</li>
     * <li><b>Table Name:</b> Formats the table name by trimming whitespace and
     * replacing underscores with spaces for better readability (e.g.,
     * "sys_user" becomes "sys user").</li>
     * <li><b>Field Name:</b> Fetches the derived field name using the
     * controller's helper method.</li>
     * <li><b>Account Type:</b> Parses the account type string to an integer to
     * set the {@link ComboBox} selection index.</li>
     * <li><b>Checkboxes:</b> Updates the 'Active' and 'Required' states based
     * on the boolean flags in the detail model.</li>
     * </ul>
     *
     * * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If an error occurs within the business logic
     * layer.
     * @throws NullPointerException If certain detail components are
     * uninitialized.
     */
    private void LoadDetail() {
        try {
            tfAccountTitle.setText(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).AccountChart().getDescription() == null ? ""
                    : poTBJControllers.TBJParameter().Detail(pnSelectedDetail).AccountChart().getDescription());

            tfTableName.setText(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm() == null ? ""
                    : poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm().trim().replace("_", " "));

            tfFieldName.setText(poTBJControllers.TBJParameter().getFieldName(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getDerivedField(), pnSelectedDetail));

            String accountTypeStr = poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getAccountType();

            if (accountTypeStr != null && !accountTypeStr.trim().isEmpty()) {

                    if(accountTypeStr.equals("D")){
                        cmbAccountType.getSelectionModel().select(0);
                    }else if(accountTypeStr.equals("C")){
                        cmbAccountType.getSelectionModel().select(1);
                    }
//                    int getAccountType = Integer.parseInt(accountTypeStr);
//                    cmbAccountType.getSelectionModel().select(getAccountType);

            }
            cbIsActive.setSelected(
                    poTBJControllers.TBJParameter().Detail(pnSelectedDetail) != null
                    && poTBJControllers.TBJParameter().Detail(pnSelectedDetail).isActive()
            );
            cbIsRequired.setSelected(
                    poTBJControllers.TBJParameter().Detail(pnSelectedDetail) != null
                    && poTBJControllers.TBJParameter().Detail(pnSelectedDetail).isRequired()
            );

        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }
    }

    /**
     * Initializes the configuration and data mapping for the transaction
     * details table.
     * <p>
     * This method handles the structural setup of the {@link TableView}:
     * <ul>
     * <li>Maps {@link TableColumn} instances (index00 through index06) to the
     * corresponding properties in the {@link ModelTableDetail} class using
     * {@link PropertyValueFactory}.</li>
     * <li>Configures a listener on the table's width property to access the
     * skin's {@code TableHeaderRow} once it is rendered.</li>
     * <li>Disables column reordering (drag-and-drop moving of columns) by
     * forcing the reordering property to remains {@code false}.</li>
     * </ul>
     * * <p>
     * <b>Note:</b> The use of {@link Platform#runLater} ensures that the table
     * header lookup occurs after the UI has finished its layout pass.</p>
     */
    private void initTableDetail() {
        index00.setCellValueFactory(new PropertyValueFactory<>("index01"));
        index01.setCellValueFactory(new PropertyValueFactory<>("index02"));
        index02.setCellValueFactory(new PropertyValueFactory<>("index03"));
        index03.setCellValueFactory(new PropertyValueFactory<>("index04"));
        index04.setCellValueFactory(new PropertyValueFactory<>("index05"));
        index05.setCellValueFactory(new PropertyValueFactory<>("index06"));
        index06.setCellValueFactory(new PropertyValueFactory<>("index07"));

//        tblDetails.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
//            Platform.runLater(() -> {
//                TableHeaderRow header = (TableHeaderRow) tblDetails.lookup("TableHeaderRow");
//                if (header != null) {
//                    header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                        header.setReordering(false);
//                    });
//                }
//            });
//        });
        // ✅ Add color for Active column
        index06.setCellFactory(col -> new TableCell<ModelTableDetail, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("✔".equals(item)) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });

        tblDetails.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            Platform.runLater(() -> {
                TableHeaderRow header = (TableHeaderRow) tblDetails.lookup("TableHeaderRow");
                if (header != null) {
                    header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        header.setReordering(false);
                    });
                }
            });
        });
        
    }

    /**
     * Asynchronously loads the transaction detail data into the TableView.
     * <p>
     * This method improves UI responsiveness by performing data retrieval on a
     * background thread. It performs the following steps:
     * <ul>
     * <li>Displays a custom {@link ProgressIndicator} within the table
     * placeholder during loading.</li>
     * <li>Checks the current {@code pnEditMode}; if in ADDNEW or UPDATE mode
     * and the last row is valid, it automatically appends a new blank detail
     * row.</li>
     * <li>Maps raw data from the controller to a {@link List} of
     * {@link ModelTableDetail} objects, converting numeric types (e.g., "0" to
     * "Debit") and boolean states to visual icons (✔/✗).</li>
     * <li>Updates the UI's {@code detail_data} observable list and refreshes
     * table items via {@link Platform#runLater}.</li>
     * <li>Cleans up the loading indicator upon task completion (success or
     * failure).</li>
     * </ul>
     */
    private void loadTableDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: transparent;");

        tblDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelTableDetail>> task = new Task<List<ModelTableDetail>>() {
            @Override
            protected List<ModelTableDetail> call() throws Exception {
                try {
                    int detailCount = poTBJControllers.TBJParameter().getDetailCount();
                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                        if (poTBJControllers.TBJParameter().Detail(detailCount - 1).getAccountNo() != null
                                && !poTBJControllers.TBJParameter().Detail(detailCount - 1).getAccountNo().isEmpty()) {
                            poTBJControllers.TBJParameter().AddDetail();
                            detailCount++;
                        }
                    }

                    List<ModelTableDetail> detailsList = new ArrayList<>();
                    for (int lnCtr = 0; lnCtr < poTBJControllers.TBJParameter().getDetailCount(); lnCtr++) {
                        String accountTypeText = (poTBJControllers.TBJParameter().Detail(lnCtr).getAccountType() == null
                                || poTBJControllers.TBJParameter().Detail(lnCtr).getAccountType().trim().isEmpty())
                                ? ""
                                : poTBJControllers.TBJParameter().Detail(lnCtr).getAccountType().equals("D") ? "Debit" : "Credit";

                        detailsList.add(new ModelTableDetail(
                                String.valueOf(lnCtr + 1),
                                poTBJControllers.TBJParameter().Detail(lnCtr).AccountChart().getDescription(),
                                accountTypeText,
                                poTBJControllers.TBJParameter().Detail(lnCtr).getTableNm(),
                                poTBJControllers.TBJParameter().getFieldName(poTBJControllers.TBJParameter().Detail(lnCtr).getDerivedField(), lnCtr),
                                poTBJControllers.TBJParameter().Detail(lnCtr).isRequired() ? "✔" : "✗",
                                poTBJControllers.TBJParameter().Detail(lnCtr).isActive() ? "✔" : "✗",
                                "", "", ""
                        ));
                    }
                    Platform.runLater(() -> {
                        detail_data.setAll(detailsList); // Properly update list
                        tblDetails.setItems(detail_data);
                        initFields();
                    });
                    return detailsList;
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(PaymentRequest_EntryController.class
                            .getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
            }
        };

        new Thread(task).start();
    }

    /**
     * Resets all user interface controls and class variables to their default
     * states.
     * <p>
     * This method performs the following cleanup:
     * <ul>
     * <li>Clears the text content of all transaction and search
     * {@link TextField}s.</li>
     * <li>Unchecks the 'Active' and 'Required' {@link CheckBox}es.</li>
     * <li>Clears the selection in the 'Account Type' {@link ComboBox}.</li>
     * <li>Empties the {@link TableView} underlying data list
     * (detail_data).</li>
     * <li>Resets internal indices and trackers (pnSelectedDetail,
     * psActiveField).</li>
     * </ul>
     */
    private void ClearAll() {
        Arrays.asList(
                tfSearchTransaction,
                tfSearchSource,
                tfSourceCode,
                tfCategory,
                tfTransactionNo,
                tfFieldName,
                tfAccountTitle,
                tfTableName ,
                tfIndustry
        ).forEach(TextField::clear);
        cbIsActive.setSelected(false);
        cbIsRequired.setSelected(false);
//        cmbAccountType.getSelectionModel().clearSelection();
        detail_data.clear();
        pnSelectedDetail = 0;
        psActiveField = "";
        taRemarks.clear();
        lblStatus.setText("UNKNOWN");
    }

    /**
     * Handles mouse click events on the transaction details table.
     * <p>
     * When a row is clicked during Add, Update, or Ready modes, this method:
     * <ul>
     * <li>Identifies the selected index and item from the TableView.</li>
     * <li>Clears the detail input fields (Account Title, Table Name, Field
     * Name, etc.) to prepare for fresh data loading.</li>
     * <li>Invokes {@link #LoadDetail()} to populate the input fields with the
     * data corresponding to the selected row.</li>
     * <li>Sets the input focus to the Account Title field for easier
     * editing.</li>
     * </ul>
     *
     * * @param event The {@link MouseEvent} triggered by clicking the table.
     */
    private void tblDetails_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnSelectedDetail = tblDetails.getSelectionModel().getSelectedIndex();
            ModelTableDetail selectedItem = tblDetails.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                tfAccountTitle.clear();
                tfTableName.clear();
                tfFieldName.clear();
                cbIsActive.setSelected(false);
                cbIsRequired.setSelected(false);
                cmbAccountType.getSelectionModel().clearSelection();

                if (selectedItem != null) {
                    if (pnSelectedDetail >= 0) {
                        LoadDetail();
                        tfAccountTitle.requestFocus();                        
                        initFields();
                    }
                }
            }
        }
    }

    /**
     * Configures a {@link TextField} to behave as a "Clearable Read-Only"
     * field.
     * <p>
     * This utility restricts direct manual input while maintaining the ability
     * for the user to remove existing values. It implements the following
     * behaviors:
     * <ul>
     * <li><b>Input Blocking:</b> Consumes {@code KEY_TYPED} events to prevent
     * alphanumeric characters from being entered manually.</li>
     * <li><b>Auto-Selection:</b> Automatically selects all text when the field
     * gains focus, allowing for immediate clearing or visual emphasis.</li>
     * <li><b>Deletion Support:</b> Explicitly allows the {@code BACK_SPACE} and
     * {@code DELETE} keys to clear the field's content, which is useful for
     * resetting lookup-based selections.</li>
     * </ul>
     * <p>
     * This is ideal for fields where data must be selected via a search/browse
     * dialog rather than direct typing.</p>
     *
     * * @param tf The {@link TextField} to be modified with read-only
     * clearable behavior.
     */
    private void makeClearableReadOnly(final TextField tf) {
        // 1️⃣ Block typing (letters/numbers)
        tf.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                event.consume();
            }
        });

        // 2️⃣ Select all text when focused
        tf.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
                if (newVal) { // gained focus
                    tf.selectAll();
                }
            }
        });

        // 3️⃣ Clear value if Backspace or Delete is pressed
        tf.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case BACK_SPACE:
                    case DELETE:
                        tf.clear();
                        event.consume();
                        break;
                    default:
                        break;
                }
            }
        });
    }

}
