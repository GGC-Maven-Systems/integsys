    package ph.com.guanzongroup.integsys.views;

    import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
    import java.net.URL;
    import java.sql.SQLException;
    import java.util.ResourceBundle;
    import java.util.logging.Level;
    import java.util.logging.Logger;
    import javafx.beans.property.ReadOnlyBooleanPropertyBase;
    import javafx.beans.value.ChangeListener;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.Initializable;
    import javafx.scene.control.*;

    import static javafx.scene.input.KeyCode.DOWN;
    import static javafx.scene.input.KeyCode.UP;
    import javafx.scene.input.KeyEvent;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.AnchorPane;
    import javafx.scene.layout.HBox;
    import org.guanzon.appdriver.agent.ShowDialogFX;
    import org.guanzon.appdriver.agent.ShowMessageFX;
    import org.guanzon.appdriver.base.*;
    import org.guanzon.appdriver.constant.EditMode;
    import org.guanzon.cas.parameter.UnitConversion;
    import org.guanzon.cas.parameter.services.ParamControllers;
    import org.json.simple.JSONObject;
    import org.json.simple.parser.ParseException;
    import ph.com.guanzongroup.cas.sales.t1.SalesAgent;
    import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
    import ph.com.guanzongroup.integsys.model.ModelListParameter;
    import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
    import ph.com.guanzongroup.integsys.utility.JFXUtil;

    /**
     * FXML Controller class
     *
     * @author Maynard / Arsiela
     */
    public class SalesagentController implements Initializable, ScreenInterface {

        private final String pxeModuleName = "Sales Agent";
        private GRiderCAS oApp;
        private SalesControllers oTrans;
        private JSONObject poJSON;
        private int pnEditMode;
        private int pnListRow;

        private ObservableList<ModelListParameter> ListData = FXCollections.observableArrayList();

        @FXML
        private AnchorPane ChildAnchorPane, apMaster, apSearchMaster,AnchorInputs;
        @FXML
        private HBox hbButtons;
        @FXML
        private Button btnBrowse,
                btnNew,
                btnSave,
                btnUpdate,
                btnConfirm,
                btnVoid,
                btnCancel,
                btnHistory,
                btnDeactivate,
                btnClose,
                btnAddAgent;
        @FXML
        private FontAwesomeIconView faActivate;
        @FXML
        private Label lblStatus;
        @FXML
        private TableColumn tblClientId, tblSalesAgent;
        @FXML
        private TextField tfSalesAgent, tfProfession, tfCompany, tfPosition, tfSearchSalesAgent,tfSalesAgentAddress,tfEmail,
                tfContact,tfSocMed,tfAccounts,tfClientID;

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            clearFields();
            initializeObject();
            pnEditMode = oTrans.SalesAgent().getEditMode();
            initButton(pnEditMode);
            initTextFields();
            ClickButton();

            ClickButton();
    //        loadTableDetail();
        }

        @Override
        public void setGRider(GRiderCAS foValue) {
            oApp = foValue;
        }

        @Override
        public void setIndustryID(String fsValue) {
        }

        @Override
        public void setCompanyID(String fsValue) {
        }

        @Override
        public void setCategoryID(String fsValue) {
        }

        private void initializeObject() {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oTrans = new SalesControllers(oApp, logwrapr);
            oTrans.SalesAgent().setRecordStatus("0134");
        }
        private void handleButtonAction(ActionEvent event) {
            Object source = event.getSource();

            if (source instanceof Button) {
                try {
                    Button clickedButton = (Button) source;
                    unloadForm appUnload = new unloadForm();
                    switch (clickedButton.getId()) {
                        case "btnClose":
                            if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                                appUnload.unloadForm(ChildAnchorPane, oApp, pxeModuleName);
                            } else {
                                return;
                            }
                            break;
                        case "btnNew":

                            clearFields();
                            tfSalesAgent.requestFocus();
                            JSONObject poJSON;
                            try {
                                poJSON = oTrans.SalesAgent().newRecord();

                                pnEditMode = EditMode.READY;
                                if ("success".equals((String) poJSON.get("result"))) {
                                    pnEditMode = EditMode.ADDNEW;
                                    initButton(pnEditMode);
                                    initTabAnchor();
                                    loadRecord();
                                } else {
                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                    initTabAnchor();
                                }
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            break;

                        case "btnBrowse":
                            String lsValue = (tfSearchSalesAgent.getText() == null) ? "" : tfSearchSalesAgent.getText();

                            try {
                                poJSON = oTrans.SalesAgent().searchRecord(lsValue, false);

                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                    tfSearchSalesAgent.clear();
                                    break;
                                }
                                clearFields();
                                pnEditMode = EditMode.READY;
                                loadRecord();
                                initTabAnchor();
                                initButton(pnEditMode);
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            break;
                        case "btnUpdate":

                            poJSON = oTrans.SalesAgent().updateRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            pnEditMode = oTrans.SalesAgent().getEditMode();
                            initButton(pnEditMode);
                            initTabAnchor();
                            break;
                        case "btnSave":
                            String clientID = oTrans.SalesAgent().getModel().getClientId();
                            oTrans.SalesAgent().getModel().setModifyingId(oApp.getUserID());
                            oTrans.SalesAgent().getModel().setModifiedDate(oApp.getServerDate());
                            JSONObject saveResult = oTrans.SalesAgent().saveRecord();
                            if ("success".equals((String) saveResult.get("result"))) {
                                ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                                if(ShowMessageFX.YesNo("Do you want to activate this record?",pxeModuleName, null) == true){
                                    oTrans.SalesAgent().openRecord(clientID);
                                    poJSON = oTrans.SalesAgent().ActivateRecord("");
                                    if ("error".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                        return;
                                    }
                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                }
                                pnEditMode = EditMode.UNKNOWN;
                                initButton(pnEditMode);
                                clearFields();
                            } else {
                                ShowMessageFX.Warning((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                            }

                            break;
                        case "btnCancel":
                            if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                                clearFields();
                                initializeObject();
                                pnEditMode = EditMode.UNKNOWN;
                                initButton(pnEditMode);
                                initTabAnchor();
                            }
                            break;
                        case "btnDeactivate" :
                            if(!ShowMessageFX.YesNo("Are you sure you want to deactivate this record?",pxeModuleName,null)){
                                return;
                            }
                            poJSON = oTrans.SalesAgent().DeActivateRecord("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                return;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearFields();
                            break;
                        case "btnVoid" :
                            if(!ShowMessageFX.YesNo("Are you sure you want to Disapprove this record?",pxeModuleName,null)){
                                return;
                            }
                            poJSON = oTrans.SalesAgent().DisapproveRecord("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                return;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearFields();
                            break;
                        case "btnConfirm" :
                            if(!ShowMessageFX.YesNo("Are you sure you want to activate this record?",pxeModuleName,null)){
                                return;
                            }
                            poJSON = oTrans.SalesAgent().ActivateRecord("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                return;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearFields();
                            break;
                        case "btnHistory" :
                            if (oTrans.SalesAgent().getModel().getClientId() == null) {
                                ShowMessageFX.Error("Unable to proceed. No record is currently loaded.", pxeModuleName, null);
                                return;
                            }
                            oTrans.SalesAgent().ShowStatusHistory();
                            break;

                        case "btnAddAgent" :
                            oTrans.SalesAgent().addAgent();
                            loadRecord();
                            break;
                    }
                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                    Logger.getLogger(SalesAgent.class.getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
                    try {
                        if (oApp != null) {
                            oApp.rollbackTrans(); // 🔥 force rollback
                        }
                    } catch (SQLException ex1) {
                        Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        private void initTabAnchor() {
            if (AnchorInputs == null) {
                System.err.println("Error: AnchorInput is not initialized.");
                return;
            }

            boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
            AnchorInputs.setDisable(!isEditable);
        }
        private void ClickButton() {
            btnBrowse.setOnAction(this::handleButtonAction);
            btnNew.setOnAction(this::handleButtonAction);
            btnSave.setOnAction(this::handleButtonAction);
            btnUpdate.setOnAction(this::handleButtonAction);
            btnCancel.setOnAction(this::handleButtonAction);
            btnDeactivate.setOnAction(this::handleButtonAction);
            btnClose.setOnAction(this::handleButtonAction);
            btnHistory.setOnAction(this::handleButtonAction);
            btnConfirm.setOnAction(this::handleButtonAction);
            btnAddAgent.setOnAction(this::handleButtonAction);
            btnVoid.setOnAction(this::handleButtonAction);
        }

        private void initButton(int fnValue) {

            CustomCommonUtil.setVisible(false, btnBrowse,
                    btnNew, btnSave,btnUpdate,btnConfirm,btnVoid,btnCancel,btnDeactivate,btnHistory,btnClose);
            CustomCommonUtil.setManaged(false, btnBrowse,
                    btnNew, btnSave,btnUpdate,btnConfirm,btnVoid,btnCancel,btnDeactivate,btnHistory,btnClose);

            switch (fnValue){
                case EditMode.ADDNEW :
                case EditMode.UPDATE :
                    CustomCommonUtil.setVisible(true,
                            btnSave,btnCancel,btnClose);
                    CustomCommonUtil.setManaged(true,
                            btnSave,btnCancel,btnClose);
                    break;
                case EditMode.READY:
                    switch (oTrans.SalesAgent().getModel().getRecordStatus()) {
                        case SalesAgent.SalesAgentConstants.OPEN:
                            CustomCommonUtil.setVisible(true,
                                    btnBrowse, btnNew, btnUpdate, btnConfirm, btnVoid,btnHistory,btnClose);
                            CustomCommonUtil.setManaged(true,
                                    btnBrowse, btnNew, btnUpdate, btnConfirm, btnVoid,btnHistory,btnClose);
                            break;
                        case SalesAgent.SalesAgentConstants.ACTIVE:
                            CustomCommonUtil.setVisible(true,
                                    btnBrowse, btnNew, btnDeactivate,btnHistory,btnClose);
                            CustomCommonUtil.setManaged(true,
                                    btnBrowse, btnNew, btnDeactivate,btnHistory,btnClose);
                            break;
                        case SalesAgent.SalesAgentConstants.INACTIVE:
                            CustomCommonUtil.setVisible(true,
                                    btnBrowse, btnNew,btnConfirm,btnHistory,btnClose);
                            CustomCommonUtil.setManaged(true,
                                    btnBrowse, btnNew,btnConfirm,btnHistory,btnClose);
                            break;
                        case SalesAgent.SalesAgentConstants.DISAPPROVE:
                            CustomCommonUtil.setVisible(true,
                                    btnBrowse, btnNew,btnHistory,btnClose);
                            CustomCommonUtil.setManaged(true,
                                    btnBrowse, btnNew,btnHistory,btnClose);
                            break;
                    }
                    break;
                case EditMode.UNKNOWN:
                    CustomCommonUtil.setVisible(true,
                            btnBrowse,
                            btnNew,
                            btnClose);
                    CustomCommonUtil.setManaged(true,
                            btnBrowse,
                            btnNew,
                            btnClose);
            }
        }

        private void initTextFields() {
            /*textFields FOCUSED PROPERTY*/
            JFXUtil.setFocusListener(txtField_Focus, tfSalesAgent, tfProfession, tfCompany, tfPosition);
            /*textFields KeyPressed PROPERTY*/
            JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apSearchMaster);
    //        JFXUtil.disableArrowNavigation(tblList);
        }

        private void txtField_KeyPressed(KeyEvent event) {
            try {
                TextField textField = (TextField) event.getSource();
                String lsTextField = textField.getId();
                String lsValue = textField.getText();
                switch (event.getCode()) {
                    case F3:
                        switch (lsTextField) {
                            case "tfSearchSalesAgent":
                                /*Browse Primary*/
                                poJSON = oTrans.SalesAgent().searchRecord(lsValue, false);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    tfSearchSalesAgent.requestFocus();
                                } else {
                                    loadRecord();
                                }
                                break;
                            case "tfSalesAgent":
                                /*search employee*/
                                poJSON = oTrans.SalesAgent().SearchClient(lsValue, false);
                                if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    tfSalesAgent.requestFocus();
                                } else {
                                    loadRecord();
                                }
                                break;
                        }
                }
                switch (event.getCode()) {
                    case ENTER:
                        CommonUtils.SetNextFocus(textField);
                    case DOWN:
                        CommonUtils.SetNextFocus(textField);
                        break;
                    case UP:
                        CommonUtils.SetPreviousFocus(textField);
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }
        }

        final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
            TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
            String lsTextField = txtField.getId();
            String lsValue = txtField.getText();

            if (lsValue == null) {
                return;
            }

            if (!nv) {
                /*Lost Focus*/
                switch (lsTextField) {
                    case "tfSalesAgent":
                        if (lsValue.isEmpty()) {
                            poJSON = oTrans.SalesAgent().getModel().setClientId("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                        }
                        break;
                    case "tfProfession":
                        poJSON = oTrans.SalesAgent().getModel().setProfession(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                    case "tfCompany":
                        poJSON = oTrans.SalesAgent().getModel().setCompany(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                    case "tfPosition":
                        poJSON = oTrans.SalesAgent().getModel().setPosition(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                }
            } else {
                txtField.selectAll();
            }
        };

        private void loadRecord() {
            try {
                boolean lbDisable = pnEditMode == EditMode.ADDNEW;
                JFXUtil.setDisabled(!lbDisable, tfSalesAgent);

                switch (oTrans.SalesAgent().getModel().getRecordStatus()) {
                    case "1":

                        lblStatus.setText("ACTIVE");
                        break;
                    case "0":

                        lblStatus.setText("OPEN");
                        break;
                    case "3":
                        lblStatus.setText("INACTIVE");
                        break;
                    case "4":
                        lblStatus.setText("DISAPPROVE");
                        break;
                }
                tfClientID.setText(oTrans.SalesAgent().getModel().Client().getClientId());
                tfSalesAgent.setText(oTrans.SalesAgent().getModel().Client().getCompanyName());
                poJSON = oTrans.SalesAgent().getClientSummary(oTrans.SalesAgent().getModel().Client().getClientId());
                if ("error".equals((String) poJSON.get("result"))) {
                    System.err.println((String) poJSON.get("message"));
    //                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                    return;
                }

                tfSalesAgentAddress.setText(poJSON.get("address").toString());
                tfEmail.setText(poJSON.get("email").toString());
                tfContact.setText(poJSON.get("mobile").toString());

                switch (poJSON.get("soctype").toString()) {
                    case "0":
                        tfSocMed.setText("Facebook");
                        break;
                    case "1":
                        tfSocMed.setText("Instagram");
                        break;
                    case "2":
                        tfSocMed.setText("X");
                        break;
                    case "3":
                        tfSocMed.setText("Others");
                        break;
                    default:
                        tfSocMed.clear();
                        break;
                }
                tfAccounts.setText(poJSON.get("acct").toString());
                tfProfession.setText(oTrans.SalesAgent().getModel().getProfession());
                tfCompany.setText(oTrans.SalesAgent().getModel().getCompany());
                tfPosition.setText(oTrans.SalesAgent().getModel().getPosition());
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }

        }

        private void clearFields() {
            lblStatus.setText("UNKNOWN");
            JFXUtil.clearTextFields(apMaster);
        }



    }
