package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.UnitConversion;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.integsys.model.ModelResultSet;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.lang.invoke.SwitchPoint;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnitConversionController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Unit Conversion";
    private int pnEditMode;
    private ParamControllers oParameters;
    private boolean state = false;
    private boolean pbLoaded = false;
    private int pnInventory = 0;
    private int pnRow = 0;
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    JSONObject poJSON = new JSONObject();
    JSONArray data;
    @FXML
    private AnchorPane AnchorMain, AnchorInputs;
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
            btnClose;

    @FXML
    private Label lblStatus;

    @FXML
    private FontAwesomeIconView faActivate;

    @FXML
    private TextField txtField01,
            txtField02,
            txtField03,
            txtField04,
            txtSeeks01;
    @FXML
    private TableView<ModelTableDetail> tblDetails;

    @FXML
    private TableColumn<ModelTableDetail, String> index00,
            index01,
            index02,
            index03,
            index04;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            clearAllFields();
            initializeObject();
            pnEditMode = oParameters.UnitConversion().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            initTabAnchor();
            initTableDetail();
            pbLoaded = true;
            
            if (oParameters.UnitConversion().getEditMode() == EditMode.ADDNEW) {
                initButton(pnEditMode);
                initTabAnchor();
                loadRecord();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new ParamControllers(oApp, logwrapr);
            oParameters.UnitConversion().setRecordStatus("0123");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnDeactivate.setOnAction(this::handleButtonAction);
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
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearAllFields();
                        txtField02.requestFocus();
                        JSONObject poJSON;
                        try {
                            poJSON = oParameters.UnitConversion().newRecord();

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
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();

                        try {
                            poJSON = oParameters.UnitConversion().searchRecord(lsValue, 1);

                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            clearAllFields();
                            pnEditMode = EditMode.READY;
                            loadRecord();
                            loadTableDetail();
                            initTabAnchor();
                            initButton(pnEditMode);
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        break;
                    case "btnUpdate":
                        poJSON = oParameters.UnitConversion().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.UnitConversion().getEditMode();
                        initButton(pnEditMode);
                        initTabAnchor();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            initTabAnchor();
                        }
                        break;
                    case "btnSave":
                        poJSON = oParameters.UnitConversion().checkConversionDuplicate(oParameters.UnitConversion().getModel().getMeasureID()
                                ,   oParameters.UnitConversion().getModel().getConvertedID());
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName,null);
                            return;
                        }
                        String conversionID = oParameters.UnitConversion().getModel().getConversionID();
                        oParameters.UnitConversion().getModel().setModifyingId(oApp.getUserID());
                        oParameters.UnitConversion().getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oParameters.UnitConversion().saveRecord();
                        if ("success".equals((String) saveResult.get("result"))) {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                            if(ShowMessageFX.YesNo("Do you want to approve this record?",pxeModuleName, null) == true){
                                oParameters.UnitConversion().openRecord(conversionID);
                                poJSON = oParameters.UnitConversion().ActivateRecord("");
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                    return;
                                }
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearAllFields();
                        } else {
                            ShowMessageFX.Warning((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;
                    case "btnDeactivate" :
                        if(!ShowMessageFX.YesNo("Are you sure you want to deactivate this record?",pxeModuleName,null)){
                            return;
                        }
                        poJSON = oParameters.UnitConversion().DeActivateRecord("");
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            return;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                        break;
                    case "btnActivate":
                        String Status = oParameters.UnitConversion().getModel().getRecordStatus();
                        String id = oParameters.UnitConversion().getModel().getConversionID();
                        JSONObject poJsON;
                        
                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                                    oParameters.UnitConversion().initialize();
                                    poJsON = oParameters.UnitConversion().activateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.UnitConversion().openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    clearAllFields();
                                    loadRecord();
                                    ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                }
                                break;
                            case "1":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Deactivate this Parameter?") == true) {
                                  
                                    poJsON = oParameters.UnitConversion().deactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.UnitConversion().openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    clearAllFields();
                                    loadRecord();
                                    ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                }
                                break;
                        }
                    break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void initTableDetail() {
        index00.setCellValueFactory(new PropertyValueFactory<>("index01"));
        index01.setCellValueFactory(new PropertyValueFactory<>("index02"));
        index02.setCellValueFactory(new PropertyValueFactory<>("index03"));
        index03.setCellValueFactory(new PropertyValueFactory<>("index04"));
        index04.setCellValueFactory(new PropertyValueFactory<>("index05"));
        index03.setStyle("-fx-alignment: CENTER-RIGHT;");
        index04.setStyle("-fx-alignment: CENTER;");
        // ✅ Add color for Active column
        index04.setCellFactory(col -> new TableCell<ModelTableDetail, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("OPEN".equals(item)) {
                        setStyle("-fx-text-fill: black;");
                    } else if ("ACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: green;");
                    }else if ("INACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: red;");
                    }else if ("DISAPPROVED".equals(item)) {
                        setStyle("-fx-text-fill: orange;");
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

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtField03.clear();
        txtField04.clear();
        txtSeeks01.clear();
        detail_data.clear();
        if (tblDetails != null) {
            tblDetails.setItems(detail_data);
            tblDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));
        }
        lblStatus.setText("UNKNOWN");
    }

    private void initButton(int fnValue) {

        try {
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
                switch (oParameters.UnitConversion().getModel().getRecordStatus()) {
                    case UnitConversion.UnitConversionConstant.OPEN:
                        CustomCommonUtil.setVisible(true,
                                btnBrowse, btnNew, btnUpdate, btnConfirm, btnDeactivate,btnHistory,btnClose);
                        CustomCommonUtil.setManaged(true,
                                btnBrowse, btnNew, btnUpdate, btnConfirm, btnDeactivate,btnHistory,btnClose);
                        break;
                    case UnitConversion.UnitConversionConstant.ACTIVE:
                        CustomCommonUtil.setVisible(true,
                                btnBrowse, btnNew, btnDeactivate,btnHistory,btnClose);
                        CustomCommonUtil.setManaged(true,
                                btnBrowse, btnNew, btnDeactivate,btnHistory,btnClose);
                        break;
                    case UnitConversion.UnitConversionConstant.INACTIVE:
                        CustomCommonUtil.setVisible(true,
                                btnBrowse, btnNew,btnHistory,btnClose);
                        CustomCommonUtil.setManaged(true,
                                btnBrowse, btnNew,btnHistory,btnClose);
                        break;
                    case UnitConversion.UnitConversionConstant.DISAPPROVE:
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    private void InitTextFields() {
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtField03.focusedProperty().addListener(txtField_Focus);
        txtField04.focusedProperty().addListener(txtField_Focus);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
        txtField02.setOnKeyPressed(this::txtField_KeyPressed);
        txtField03.setOnKeyPressed(this::txtField_KeyPressed);
        JFXUtil.inputDecimalOnly(txtField04);

    }

    private void txtSeeks_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJson = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 01:
                            poJson = oParameters.UnitConversion().searchRecord(lsValue, 1);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            clearAllFields();
                            txtSeeks01.setText((String) oParameters.UnitConversion().getModel().getConversionID());
                            pnEditMode = EditMode.READY;
                            loadRecord();
                            loadTableDetail();
                            break;
                    }
                case ENTER:
            }
            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJson = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 02:
                            poJson = oParameters.UnitConversion().SearchMeasure(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtField02.clear();
                                break;
                            }
                            txtField02.setText((String) oParameters.UnitConversion().getModel().Measurement().getDescription());
                            loadTableDetail();
                            break;
                        case 03:
                            poJson = oParameters.UnitConversion().SearchConversion(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtField03.clear();
                                break;
                            }
                            txtField03.setText((String) oParameters.UnitConversion().getModel().ConvertTo().getDescription());
                            break;
                    }
                case ENTER:
            }
            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        if (!pbLoaded) {
            return;
        }

        TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            try {
                switch (lnIndex) {
                    case 4:
                       poJSON =  oParameters.UnitConversion().getModel().setQuantityConverted(Double.valueOf(lsValue));
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        } else {
            txtField.selectAll();
        }
    };

    private void loadRecord() {
        try {
            boolean lbActive = oParameters.UnitConversion().getModel().getRecordStatus() == "1";
            txtField01.setText(oParameters.UnitConversion().getModel().getConversionID());
            txtField02.setText(oParameters.UnitConversion().getModel().Measurement().getDescription());
            txtField03.setText(oParameters.UnitConversion().getModel().ConvertTo().getDescription());
            txtField04.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oParameters.UnitConversion().getModel().getQuantityConverted(),false));

            switch (oParameters.UnitConversion().getModel().getRecordStatus()) {
                case "1":
                    btnDeactivate.setText("Deactivate");
                    lblStatus.setText("ACTIVE");
                    break;
                case "0":
                    btnDeactivate.setText("Activate");
                    lblStatus.setText("OPEN");
                    break;
                case "3":
                    lblStatus.setText("INACTIVE");
                    break;
                case "4":
                    lblStatus.setText("DISAPPROVE");
                break;
            }

            loadTableDetail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
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
    private void loadTableDetail() {
        String measureID;
        try {
            measureID = oParameters.UnitConversion().getModel().getMeasureID();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(UnitConversionController.class.getName()).log(Level.SEVERE, null, ex);
            detail_data.clear();
            tblDetails.setItems(detail_data);
            tblDetails.setPlaceholder(new Label("FAILED TO LOAD RECORDS"));
            return;
        }

        if (measureID == null || measureID.trim().isEmpty()) {
            detail_data.clear();
            tblDetails.setItems(detail_data);
            tblDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));
            return;
        }

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
                    List<ModelTableDetail> rows = new ArrayList<>();
                    JSONObject response = oParameters.UnitConversion().RetriveMeasurements(measureID);
                    if ("success".equals(response.get("result"))) {
                        JSONArray list = (JSONArray) response.get("data");
                        for (int i = 0; i < list.size(); i++) {

                            JSONObject obj = (JSONObject) list.get(i);
                            String status = obj.get("cRecdStat") == null ? "" : obj.get("cRecdStat").toString();
                            String statusIcon = "";
                            switch (status) {
                                case UnitConversion.UnitConversionConstant.OPEN :
                                    statusIcon = "OPEN";
                                    break;
                                case UnitConversion.UnitConversionConstant.ACTIVE :
                                    statusIcon = "ACTIVE";
                                    break;
                                case UnitConversion.UnitConversionConstant.INACTIVE:
                                    statusIcon = "INACTIVE";
                                    break;
                                case UnitConversion.UnitConversionConstant.DISAPPROVE:
                                    statusIcon = "DISAPPROVED";
                                    break;
                                default:
                                    statusIcon = "";
                            }

                            rows.add(new ModelTableDetail(
                                        String.valueOf(i + 1),
                                    obj.get("ConvertionFrom") == null ? "" : obj.get("ConvertionFrom").toString(),
                                    obj.get("ConvertionTo") == null ? "" : obj.get("ConvertionTo").toString(),
                                    obj.get("nQtyCnvrt") == null
                                            ? ""
                                            : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                            Double.parseDouble(obj.get("nQtyCnvrt").toString()),
                                            false),
                                    statusIcon
                                ,""));

                            }
                        }
                    return rows;
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(PaymentRequest_EntryController.class
                            .getName()).log(Level.SEVERE, null, ex);
                    throw ex;
                }
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                List<ModelTableDetail> rows = getValue();
                detail_data.setAll(rows == null ? FXCollections.observableArrayList() : rows);
                tblDetails.setItems(detail_data);
                if (detail_data.isEmpty()) {
                    tblDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));
                }
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                detail_data.clear();
                tblDetails.setItems(detail_data);
                tblDetails.setPlaceholder(new Label("FAILED TO LOAD RECORDS"));
            }
        };

        Thread loaderThread = new Thread(task, "unit-conversion-detail-loader");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }
}
