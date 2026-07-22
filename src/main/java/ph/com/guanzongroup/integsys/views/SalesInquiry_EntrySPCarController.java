/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.parser.ParseException;
import javafx.animation.PauseTransition;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.t1.status.SalesInquiryStatic;
import org.guanzon.appdriver.constant.UserRight;

/**
 *
 * @author Team 2
 */
public class SalesInquiry_EntrySPCarController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0;
    boolean lsIsSaved = false;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static SalesControllers poSalesInquiryController;
    public int pnEditMode;
    boolean pbKeyPressed = false;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";

    private ObservableList<ModelSalesInquiry_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelSalesInquiry_Detail> filteredDataDetail;

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEntered = false;
    private final JFXUtil.RowDragLock dragLock = new JFXUtil.RowDragLock(true);

    ObservableList<String> ClientType = ModelSalesInquiry_Detail.ClientType;

    ObservableList<String> PurchaseType = ModelSalesInquiry_Detail.PurchaseType;
    ObservableList<String> CategoryType = ModelSalesInquiry_Detail.CategoryType;
    JFXUtil.ReloadableTableTask loadTableDetail;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfBranch, tfSalesPerson, tfClient, tfAddress, tfInquiryStatus, tfContactNo, tfInquiryType, tfReferralAgent, tfBarcode, tfDescription, tfBrand, tfModel, tfColor, tfSellingPrice, tfModelVariant;
    @FXML
    private DatePicker dpTransactionDate, dpTargetDate;
    @FXML
    private ComboBox cmbClientType, cmbPurchaseType;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetails;
    @FXML
    private TableColumn tblRowNoDetail, tblBarcodeDetail, tblDescriptionDetail;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        poSalesInquiryController = new SalesControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poSalesInquiryController.SalesInquiry().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initLoadTable();
        initComboBoxes();
        initTextFields();
        initDatePickers();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();
        pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();
        initButton(pnEditMode);

        Platform.runLater(() -> {
            poSalesInquiryController.SalesInquiry().Master().setIndustryId(psIndustryId);
            poSalesInquiryController.SalesInquiry().Master().setCompanyId(psCompanyId);
            poSalesInquiryController.SalesInquiry().Master().setCompanyId(psCategoryId);
            poSalesInquiryController.SalesInquiry().setIndustryId(psIndustryId);
            poSalesInquiryController.SalesInquiry().setCompanyId(psCompanyId);
            poSalesInquiryController.SalesInquiry().setCategoryId(psCategoryId);
            poSalesInquiryController.SalesInquiry().setWithUI(true);
            loadRecordSearch();

            btnNew.fire();
        });
        JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
    }

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

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poSalesInquiryController.SalesInquiry().setTransactionStatus(SalesInquiryStatic.OPEN);
                        poJSON = poSalesInquiryController.SalesInquiry().searchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        //Clear data
                        poSalesInquiryController.SalesInquiry().resetMaster();
                        poSalesInquiryController.SalesInquiry().Detail().clear();
                        clearTextFields();

                        poJSON = poSalesInquiryController.SalesInquiry().NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        poSalesInquiryController.SalesInquiry().initFields();
                        pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();

                        break;
                    case "btnUpdate":
                        poJSON = poSalesInquiryController.SalesInquiry().OpenTransaction(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
                        poJSON = poSalesInquiryController.SalesInquiry().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poSalesInquiryController.SalesInquiry().getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            poSalesInquiryController.SalesInquiry().resetMaster();
                            poSalesInquiryController.SalesInquiry().Detail().clear();
                            clearTextFields();

                            poSalesInquiryController.SalesInquiry().Master().setIndustryId(psIndustryId);
                            poSalesInquiryController.SalesInquiry().Master().setCompanyId(psCompanyId);
                            poSalesInquiryController.SalesInquiry().Master().setCategoryCode(psCategoryId);
//                            poSalesInquiryController.SalesInquiry().initFields();
                            pnEditMode = EditMode.UNKNOWN;

                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                            ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                            return;
                        }

                        try {
                            poSalesInquiryController.SalesInquiry().ShowStatusHistory();
                            return;
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poSalesInquiryController.SalesInquiry().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poSalesInquiryController.SalesInquiry().AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poSalesInquiryController.SalesInquiry().OpenTransaction(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poSalesInquiryController.SalesInquiry().Master().getTransactionStatus().equals(SalesInquiryStatic.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poSalesInquiryController.SalesInquiry().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                btnNew.fire();

                            }
                        } else {
                            return;
                        }
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (lsButton.equals("btnPrint")) { //|| lsButton.equals("btnCancel")
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                }
                initButton(pnEditMode);

                if (lsButton.equals("btnUpdate")) {
                    if (poSalesInquiryController.SalesInquiry().Detail(pnDetail).getStockId() == null || "".equals(poSalesInquiryController.SalesInquiry().Detail(pnDetail).getStockId())) {
                        tfBarcode.requestFocus();
                    }
                }

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.ADDNEW;
        JFXUtil.setDisabled(!lbDisable, tfClient, cmbClientType);
        try {
            Platform.runLater(() -> {
                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSalesInquiryController.SalesInquiry().Master().getTransactionStatus();
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put(SalesInquiryStatic.QUOTED, "QUOTED");
                statusMap.put(SalesInquiryStatic.SALE, "SALE");
                statusMap.put(SalesInquiryStatic.CONFIRMED, "CONFIRMED");
                statusMap.put(SalesInquiryStatic.OPEN, "OPEN");
                statusMap.put(SalesInquiryStatic.VOID, "VOIDED");
                statusMap.put(SalesInquiryStatic.CANCELLED, "CANCELLED");
                statusMap.put(SalesInquiryStatic.LOST, "LOST");
                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN"); //default
                lblStatus.setText(lsStat);

                switch (poSalesInquiryController.SalesInquiry().Master().getInquiryStatus()) {
                    case "0":
                        tfInquiryStatus.setText("OPEN");
                        break;
                    default:
                        tfInquiryStatus.setText("");
                        break;
                }
            });

            // Transaction Date
            tfTransactionNo.setText(poSalesInquiryController.SalesInquiry().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            String lsTargetDate = CustomCommonUtil.formatDateToShortString(poSalesInquiryController.SalesInquiry().Master().getTargetDate());
            dpTargetDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTargetDate, "yyyy-MM-dd"));

            tfBranch.setText(poSalesInquiryController.SalesInquiry().Master().Branch().getBranchName());
            tfSalesPerson.setText(poSalesInquiryController.SalesInquiry().Master().SalesPerson().getFullName());
            tfReferralAgent.setText(poSalesInquiryController.SalesInquiry().Master().ReferralAgent().getCompanyName());

            tfClient.setText(poSalesInquiryController.SalesInquiry().Master().Client().getCompanyName());
            tfAddress.setText(poSalesInquiryController.SalesInquiry().Master().ClientAddress().getAddress());
            tfContactNo.setText(poSalesInquiryController.SalesInquiry().Master().ClientMobile().getMobileNo());
            tfInquiryType.setText(poSalesInquiryController.SalesInquiry().Master().Source().getDescription());

            taRemarks.setText(poSalesInquiryController.SalesInquiry().Master().getRemarks());

            if (pnEditMode != EditMode.UNKNOWN) {

                cmbPurchaseType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getPurchaseType()));
                if (poSalesInquiryController.SalesInquiry().Master().getClientId() != null && !"".equals(poSalesInquiryController.SalesInquiry().Master().getClientId())) {
                    cmbClientType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().Client().getClientType()));
                } else {
                    cmbClientType.getSelectionModel().select(Integer.parseInt(poSalesInquiryController.SalesInquiry().Master().getClientType()));
                }
            } else {

                cmbPurchaseType.getSelectionModel().select(0);
                cmbClientType.getSelectionModel().select(0);
            }

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poSalesInquiryController.SalesInquiry().getDetailCount() - 1) {
                return;
            }
            String lsBrand = "";
            if (!JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Brand().getDescription(), null, "")) {
                lsBrand = poSalesInquiryController.SalesInquiry().Detail(pnDetail).Brand().getDescription();
            } else {
                lsBrand = poSalesInquiryController.SalesInquiry().Detail(pnDetail).Inventory().Brand().getDescription();
            }
            tfBrand.setText(lsBrand);
            tfBarcode.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Inventory().getDescription());

            tfModel.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Model().getDescription());
            tfModelVariant.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).ModelVariant().getDescription());
            tfColor.setText(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Color().getDescription());
            tfSellingPrice.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Inventory().getSellingPrice(), true));
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView currentTable = (TableView) event.getSource();
            TablePosition focusedCell = currentTable.getFocusModel().getFocusedCell();
            if (focusedCell != null) {
                switch (event.getCode()) {
                    case TAB:
                    case DOWN:
                        pnDetail = JFXUtil.moveToNextRow(currentTable);
                        break;
                    case UP:
                        pnDetail = JFXUtil.moveToPreviousRow(currentTable);
                        break;

                    default:
                        break;
                }
                loadRecordDetail();
                event.consume();
            }
        }
    }

    public void initTableOnClick() {
        tblViewTransDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                    moveNext(false, false);
                }
            }
        });

        tblViewTransDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails); // need to use computed-size in min-width of the column to work
        JFXUtil.enableRowDragAndDrop(tblViewTransDetails, item -> ((ModelSalesInquiry_Detail) item).index01Property(),
                item -> ((ModelSalesInquiry_Detail) item).index03Property(),
                item -> ((ModelSalesInquiry_Detail) item).index04Property(), dragLock, index -> {

                    for (ModelSalesInquiry_Detail d : details_data) {
                        String brand = d.getIndex04();
                        String model = d.getIndex05();
                        String color = d.getIndex06();
                        String priorityStr = d.getIndex01();
                        for (int i = 0, n = poSalesInquiryController.SalesInquiry().getDetailCount(); i < n; i++) {
                            if (!brand.equals(poSalesInquiryController.SalesInquiry().Detail(i).getStockId())) {
                                continue;
                            }
                            try {
//                                System.out.println(d.getIndex02() +" - "+priorityStr);
                                poSalesInquiryController.SalesInquiry().Detail(i).setPriority(Integer.parseInt(priorityStr));
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid priority: " + priorityStr);
                            }
                            break;
                        }
                    }
                    pnDetail = index;
                    loadTableDetail.reload();
                });
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {

                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poSalesInquiryController.SalesInquiry().loadDetail();
                            }
                            poSalesInquiryController.SalesInquiry().sortPriority();
                            String lsBarcode = "";
                            String lsDescription = "";
                            for (lnCtr = 0; lnCtr < poSalesInquiryController.SalesInquiry().getDetailCount(); lnCtr++) {
                                if (poSalesInquiryController.SalesInquiry().Detail(lnCtr).getStockId() != null) {
                                    lsBarcode = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().getBarCode();
                                    lsDescription = poSalesInquiryController.SalesInquiry().Detail(lnCtr).Inventory().getDescription();
                                }
                                details_data.add(
                                        new ModelSalesInquiry_Detail(
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getPriority()),
                                                String.valueOf(lsBarcode),
                                                lsDescription,
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).getStockId()),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).Model().getModelId()),
                                                String.valueOf(poSalesInquiryController.SalesInquiry().Detail(lnCtr).Color().getColorId()), ""
                                        ));
                                lsBarcode = "";
                                lsDescription = "";
                            }

                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewTransDetails, 0);
                                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewTransDetails, pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }

                    });
                });
    }
    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
                    case "taRemarks"://Remarks
                        poJSON = poSalesInquiryController.SalesInquiry().Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        break;
                }
                loadRecordMaster();

            });

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfBrand":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poSalesInquiryController.SalesInquiry().Detail(pnDetail).setBrandId("");
                        }
                        break;
                    case "tfBarcode":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setStockId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setModelId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setColorId("");
                        }
                        break;
                    case "tfDescription":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setStockId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setModelId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Detail(pnDetail).setColorId("");
                        }
                        if (pbEntered) {
                            moveNext(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                Platform.runLater(() -> {
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                    delay.setOnFinished(event -> {
                        loadTableDetail.reload();
                    });
                    delay.play();
                });
            });
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSalesPerson":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setSalesMan("");
                        }
                        break;
                    case "tfReferralAgent":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setAgentId("");
                        }
                        break;
                    case "tfInquiryType":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setSourceCode("");
                        }
                        break;
                    case "tfClient":
                        if (lsValue.isEmpty()) {
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setClientId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setAddressId("");
                            poJSON = poSalesInquiryController.SalesInquiry().Master().setContactId("");
                        }
                        break;

                }

                loadRecordMaster();
            });

    public void moveNext(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apDetail.requestFocus();
                pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewTransDetails) : JFXUtil.moveToNextRow(tblViewTransDetails);
            }
            loadRecordDetail();
            if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().Detail(pnDetail).getBrandId(), null, "")) {
                tfBrand.requestFocus();
            } else if (JFXUtil.isObjectEqualTo(poSalesInquiryController.SalesInquiry().Detail(pnDetail).Inventory().getBarCode(), null, "")) {
                tfBarcode.requestFocus();
            } else {
                tfBrand.requestFocus();
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;

            TableView<?> currentTable = tblViewTransDetails;
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfBarcode":
                        case "tfDescription":
                            moveNext(true, true);
                            event.consume();
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfBarcode":
                        case "tfDescription":
                            moveNext(false, true);
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfClient":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchClient(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfClient.setText("");
                                break;
                            } else {
                                JFXUtil.focusFirstTextField(apDetail);
                            }
                            loadRecordMaster();
                            return;
                        case "tfSalesPerson":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchSalesPerson(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfReferralAgent);
                            }
                            loadRecordMaster();
                            return;
                        case "tfReferralAgent":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchReferralAgent(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSalesPerson.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfInquiryType);
                            }
                            loadRecordMaster();
                            return;
                        case "tfInquiryType":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchSource(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));

                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfClient);
                            }
                            loadRecordMaster();
                            return;
                        case "tfBrand":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchBrand(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                loadTableDetail.reload();
                                JFXUtil.textFieldMoveNext(tfBarcode);
                            }
                            loadRecordMaster();
                            return;
                        case "tfBarcode":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchInventory(lsValue, true, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                loadTableDetail.reload();
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        moveNext(false, true);
                                    });
                                    delay.play();
                                });
                            }
                            break;
                        case "tfDescription":
                            poJSON = poSalesInquiryController.SalesInquiry().SearchInventory(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                loadTableDetail.reload();
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        moveNext(false, true);
                                    });
                                    delay.play();
                                });
                            }
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");

        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                LocalDate currentDate = null, selectedDate = null, receivingDate = null;
                String lsServerDate = "", lsTransDate = "", lsSelectedDate = "", lsReceivingDate = "";

                JFXUtil.JFXUtilDateResult ldtResult = JFXUtil.processDate(inputText, datePicker);
                poJSON = ldtResult.poJSON;
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    loadRecordMaster();
                    return;
                }
                if (JFXUtil.isObjectEqualTo(inputText, null, "", "01/01/1900")) {
                    return;
                }
                selectedDate = ldtResult.selectedDate;

                switch (datePicker.getId()) {
                    case "dpTargetDate":
                        if (poSalesInquiryController.SalesInquiry().getEditMode() == EditMode.ADDNEW
                                || poSalesInquiryController.SalesInquiry().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poSalesInquiryController.SalesInquiry().Master().getTransactionDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isBefore(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Target date cannot be before the transaction date.");
                                pbSuccess = false;
                            } else {
                                poSalesInquiryController.SalesInquiry().Master().setTargetDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                            if (pbSuccess) {
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value

                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    final EventHandler<ActionEvent> comboBoxActionListener = event -> {
        Object source = event.getSource();
        @SuppressWarnings("unchecked")
        ComboBox<?> cb = (ComboBox<?>) source;

        String cbId = cb.getId();
        int selectedIndex = cb.getSelectionModel().getSelectedIndex();
        switch (cbId) {
            case "cmbClientType":
                //if client type is changed then remove the client 
                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                    if (!poSalesInquiryController.SalesInquiry().Master().getClientType().equals(selectedIndex)) {
                        poSalesInquiryController.SalesInquiry().Master().setClientId("");
                        poSalesInquiryController.SalesInquiry().Master().setAddressId("");
                        poSalesInquiryController.SalesInquiry().Master().setContactId("");
                    }
                }
                poSalesInquiryController.SalesInquiry().Master().setClientType(String.valueOf(selectedIndex));
                break;
            case "cmbPurchaseType":
                poSalesInquiryController.SalesInquiry().Master().setPurchaseType(String.valueOf(selectedIndex));
                break;
            case "cmbCategoryType":
                poSalesInquiryController.SalesInquiry().Master().setCategoryType(String.valueOf(selectedIndex));
                break;
            default:
                System.out.println("⚠ Unrecognized ComboBox ID: " + cbId);
                break;
        }
        loadRecordMaster();
    };

    private void initComboBoxes() {

        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(ClientType, cmbClientType),
                new JFXUtil.Pairs<>(PurchaseType, cmbPurchaseType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbClientType, cmbPurchaseType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbClientType, cmbPurchaseType);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpTargetDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpTargetDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfClient, tfSalesPerson, tfReferralAgent, tfInquiryType);
        JFXUtil.setFocusListener(txtDetail_Focus, tfBrand, tfBarcode, tfDescription);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setDisabled(oApp.getUserLevel() <= UserRight.ENCODER, tfSalesPerson);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        dragLock.isEnabled = lbShow; // for drag drop
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow, taRemarks, apMaster, apDetail);

        switch (poSalesInquiryController.SalesInquiry().Master().getTransactionStatus()) {
            case SalesInquiryStatic.QUOTED:
            case SalesInquiryStatic.SALE:
            case SalesInquiryStatic.LOST:
            case SalesInquiryStatic.VOID:
            case SalesInquiryStatic.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);

        tblViewTransDetails.setItems(details_data);
        tblViewTransDetails.autosize();
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poSalesInquiryController.SalesInquiry().Master().Company().getCompanyName() + " - " + poSalesInquiryController.SalesInquiry().Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse);
    }
}
