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
import javafx.scene.text.Text;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.*;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.SalesAgent;
import ph.com.guanzongroup.cas.sales.t1.Salesman;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.integsys.model.ModelListParameter;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Maynard / Arsiela
 */
public class SalesmanController implements Initializable, ScreenInterface {

    private final String pxeModuleName = "Salesman";
    private GRiderCAS oApp;
    private SalesControllers oTrans;
    private JSONObject poJSON;
    private int pnEditMode;
    private int pnListRow;
    private  String searchdept = "";
    private  String searchbranch = "";

    private ObservableList<ModelListParameter> ListData = FXCollections.observableArrayList();

    @FXML
    private AnchorPane ChildAnchorPane, apMaster, apSearchMaster,AnchorInputs;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse,btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnClose,btnDeactivate;
    @FXML
    private FontAwesomeIconView faActivate;
    @FXML
    private TextField tfSearchSalesman,tfEmployee,tfEmployeeID,tfDepartment, tfBranch, tfPosition;
    @FXML
    private CheckBox cbActive;
    @FXML
    private TableColumn tblEmployeeId, tblSalesman;
    @FXML
    private Label lblOptional;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        clearFields();
        initializeObject();
        pnEditMode = oTrans.Salesman().getEditMode();
        initButton(pnEditMode);
        initTextFields();
        ClickButton();
        btnNew.fire();
        System.out.println("isMainOffice || iswarehouse " + oApp.isMainOffice() + " = " + oApp.isWarehouse());

    }
    private void initializeObject() {
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        oTrans = new SalesControllers(oApp, logwrapr);
        oTrans.Salesman().setRecordStatus("01");
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

    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnDeactivate.setOnAction(this::handleButtonAction);
        btnActivate.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
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
                        tfEmployee.requestFocus();
                        JSONObject poJSON;
                        try {
                            poJSON = oTrans.Salesman().newRecord();

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
                        String lsValue = (tfSearchSalesman.getText() == null) ? "" : tfSearchSalesman.getText();

                        try {
                            poJSON = oTrans.Salesman().searchRecord(lsValue, false);

                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                tfSearchSalesman.clear();
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

                    case "btnSave":
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the record?") == true) {
                            poJSON = oTrans.Salesman().saveRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            ShowMessageFX.Information("Record saved successfully", pxeModuleName, null);
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearFields();
                        } else {
                            return;
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
                        if (oApp.getUserLevel() < UserRight.BRANCH_MANAGER) {
                            ShowMessageFX.Warning("User is not authorized to deactivate this record", pxeModuleName, null);
                            return;
                        }
                        poJSON = oTrans.Salesman().deactivateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            return;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                        pnEditMode = EditMode.UNKNOWN;
                        initButton(pnEditMode);
                        clearFields();
                        break;
                    case "btnActivate" :

                        if(!ShowMessageFX.YesNo("Are you sure you want to activate this record?",pxeModuleName,null)){
                            return;
                        }
                        if (oApp.getUserLevel() < UserRight.BRANCH_MANAGER) {
                            ShowMessageFX.Warning("User is not authorized to activate this record", pxeModuleName, null);
                            return;
                        }
                        poJSON = oTrans.Salesman().activateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            return;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                        pnEditMode = EditMode.UNKNOWN;
                        initButton(pnEditMode);
                        clearFields();
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(Salesman.class.getName()).log(Level.SEVERE, null, ex);
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

    private void initButtonx(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnBrowse,btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnActivate,btnDeactivate);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow, apMaster);
    }

    private void initButton(int fnValue) {

        CustomCommonUtil.setVisible(false, btnBrowse,
                btnNew, btnSave,btnUpdate,btnCancel,btnDeactivate,btnActivate,btnClose);
        CustomCommonUtil.setManaged(false, btnBrowse,
                btnNew, btnSave,btnUpdate,btnCancel,btnDeactivate,btnActivate,btnClose);

        switch (fnValue){
            case EditMode.ADDNEW :
            case EditMode.UPDATE :
                CustomCommonUtil.setVisible(true,
                        btnSave,btnCancel,btnClose);
                CustomCommonUtil.setManaged(true,
                        btnSave,btnCancel,btnClose);
                break;
            case EditMode.READY:
                String recordStatus = oTrans.Salesman().getModel().getRecordStatus()
                        ? RecordStatus.ACTIVE
                        : RecordStatus.INACTIVE;

                switch (recordStatus) {
                    case RecordStatus.ACTIVE:
                        CustomCommonUtil.setVisible(true,
                                btnBrowse, btnNew, btnDeactivate, btnClose);
                        CustomCommonUtil.setManaged(true,
                                btnBrowse, btnNew, btnDeactivate, btnClose);
                        break;

                    case RecordStatus.INACTIVE:
                        CustomCommonUtil.setVisible(true,
                                btnBrowse, btnNew, btnActivate, btnClose);
                        CustomCommonUtil.setManaged(true,
                                btnBrowse, btnNew, btnActivate, btnClose);
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
        JFXUtil.setFocusListener(txtField_Focus,  tfEmployee,tfEmployeeID,tfDepartment, tfBranch, tfPosition);
        /*textFields KeyPressed PROPERTY*/
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apSearchMaster);
        if (oApp.isMainOffice() ||oApp.isWarehouse() ) {
            tfBranch.setDisable(false);
            tfDepartment.setDisable(false);
            lblOptional.setVisible(true);
            lblOptional.setManaged(true);
            tfBranch.setPromptText("Press F3: Search");
            tfDepartment.setPromptText("Press F3: Search");

        }else{
            tfBranch.setDisable(true);
            tfDepartment.setDisable(false);
            lblOptional.setVisible(false);
            lblOptional.setManaged(false);
            tfBranch.setPromptText("");
            tfDepartment.setPromptText("");

        }


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
                        case "tfSearchSalesman":
//                            String lsValue = (tfSearchSalesman.getText() == null) ? "" : tfSearchSalesman.getText();

                            try {
                                poJSON = oTrans.Salesman().searchRecord(lsValue, false);

                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                    tfSearchSalesman.clear();
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
                        case "tfEmployee":
                            /*search employee*/
                            poJSON = oTrans.Salesman().searchEmployee(lsValue,  searchbranch,searchdept, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                tfEmployee.requestFocus();
                            } else {
                                loadRecord();
                            }
                            break;
                        case "tfBranch":
                            /*search branch*/
                            poJSON = oTrans.Salesman().SearchBranch(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                tfEmployee.requestFocus();
                            }
                            tfBranch.setText(oTrans.Salesman().getModel().Branch().getBranchName());
                            searchbranch = oTrans.Salesman().getModel().getBranchCode();
                            break;
                        case "tfDepartment":
                            /*search dept*/
                            poJSON = oTrans.Salesman().SearchDepartment(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                tfDepartment.requestFocus();
                            }
                            tfDepartment.setText((String) poJSON.get("department"));
                            searchdept = (String) poJSON.get("deptID");
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
                case "tfBranch":
                    if(lsValue == null || lsValue.isEmpty()){
                        searchbranch = "";
                    }
                    break;
                case "tfEmployee":
                    if (lsValue.isEmpty()) {
                        poJSON = oTrans.Salesman().getModel().setEmployeeId("");
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                    }
                    break;
                case "tfDepartment":
                    if(lsValue == null || lsValue.isEmpty()){
                        searchdept = "";
                    }
                    break;
//
            }
        } else {
            txtField.selectAll();
        }
    };

    private void loadRecord() {
        try {
            boolean lbDisable = pnEditMode == EditMode.ADDNEW;
            JFXUtil.setDisabled(!lbDisable, tfEmployee);

            boolean lbActive = oTrans.Salesman().getModel().getRecordStatus();
            cbActive.setSelected(lbActive);

            tfEmployeeID.setText(oTrans.Salesman().getModel().getEmployeeId());
            tfEmployee.setText(oTrans.Salesman().getModel().getFullName());

            poJSON = oTrans.Salesman().getOtherInfos(oTrans.Salesman().getModel().getEmployeeId());
            if ("error".equals((String) poJSON.get("result"))) {
                System.err.println((String) poJSON.get("message"));
                //                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                return;
            }
            tfBranch.setText(poJSON.get("branch").toString());
            tfPosition.setText(poJSON.get("position").toString());
            tfDepartment.setText(poJSON.get("department").toString());
//
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    private void clearFields() {
        btnActivate.setText("Activate");
        cbActive.setSelected(false);

        JFXUtil.clearTextFields(apMaster);
    }
    private void initTabAnchor() {
        if (AnchorInputs == null) {
            System.err.println("Error: AnchorInput is not initialized.");
            return;
        }

        boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        AnchorInputs.setDisable(!isEditable);
    }

}
