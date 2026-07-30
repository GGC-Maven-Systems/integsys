package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.systables.SystemUser;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Company;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Industry;
import ph.com.guanzongroup.integsys.model.ModelLog_In_User;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class LoginController implements Initializable, ScreenInterface {

    private final String MODULE = "Login";

    private GRiderCAS oApp;
    private String psIndustryID = "";
    private String psCompanyID = "";

    private boolean isMainOffice = false;
    private boolean isWarehouse = false;

    private LogWrapper poLogWrapper;
    private DashboardController dashboardController;

    ObservableList<ModelLog_In_Industry> industryOptions = FXCollections.observableArrayList();
    ObservableList<ModelLog_In_Company> companyOptions = FXCollections.observableArrayList();
    ObservableList<ModelLog_In_User> users = FXCollections.observableArrayList();
    boolean enterProceed = true;
    @FXML
    private TextField tfUsername;
    @FXML
    private Button btnSignIn;
    @FXML
    private BorderPane bpBorderMain;
    @FXML
    Label lblCopyright;
    @FXML
    private TextField tfPassword;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private Button btnEyeIcon;
    @FXML
    private ComboBox cmbIndustry, cmbCompany;
    @FXML
    private ImageView ivLoginLogo;
    @FXML
    private AnchorPane apRoot3, rootPane, loginPane, apRootPane2, apSign, apRoot2, apRoot1, spinnerPane, loadingPane;
    @FXML
    private VBox vboxmain;

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        JFXUtil.fadeIn(1, vboxmain);
        JFXUtil.fadeIn(2, apRoot3);
        JFXUtil.applyButtonHoverFade(btnSignIn, "#FF8201");

//        JFXUtil.fadeInFromBottom(1.5, rootPane, apRoot1, apRoot2,vboxmain);
        btnSignIn.setVisible(false);
//        fadeInFromBottom(2, rootPane, rootpane2, rootpane3, vbox1);
        JFXUtil.runWithDelay(0.2, () -> {
            JFXUtil.applyClockwiseFillAnimation(apSign, 4);
            JFXUtil.runWithDelay(1.5, () -> {
                JFXUtil.fadeIn(1, btnSignIn);
            });
        });

        DashboardController mainController = LoginControllerHolder.getMainController();
        mainController.triggervbox();
        mainController.removewindowEvent();

        tfPassword.textProperty().bindBidirectional(pfPassword.textProperty());

        String year = String.valueOf(Year.now().getValue());

        lblCopyright.setStyle("-fx-font-size: 13px;");
        lblCopyright.setText("© " + year + " Guanzon Group of Companies. All Rights Reserved.");

        cmbIndustry.managedProperty().bind(cmbIndustry.visibleProperty());
        cmbCompany.managedProperty().bind(cmbCompany.visibleProperty());

        initComboBox();
        initTextFields();
        TriggerWindowEvent();
    }

    public void TriggerWindowEvent() {
        AnchorPane root = (AnchorPane) rootPane;
        Scene scene = root.getScene();
        if (scene != null) {
            setKeyEvent(scene);
        } else {
            root.sceneProperty().addListener(WindowKeyEvent);
        }
    }
    ChangeListener<Scene> WindowKeyEvent = (obs, oldScene, newScene) -> {
        if (newScene != null) {
            setKeyEvent(newScene);
        }
    };

    private void setKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!LoginControllerHolder.getLogInStatus()) {
                    if (enterProceed) {
                        btnSignIn.fire();
                    }
                }
            }
            if (event.getCode() == KeyCode.F12) {
                LoginControllerHolder.getMainController().eventf12(LoginControllerHolder.getMainController().getTab());
            }
        }
        );

    }
    EventHandler<KeyEvent> tabKeyHandler = event -> {
        if (event.getCode() != KeyCode.TAB) {
            return;
        }
        Node source = (Node) event.getSource();
        String fieldId = source.getId();
        event.consume(); // prevent default focus traversal
        switch (fieldId) {
            case "tfUsername":
                if (pfPassword.isVisible()) {
                    pfPassword.requestFocus();
                } else {
                    tfPassword.requestFocus();
                }
                break;
            case "pfPassword":
            case "tfPassword":
                cmbIndustry.requestFocus();
                break;
            case "cmbIndustry":
                cmbCompany.requestFocus();
                break;
            case "cmbCompany":
                tfUsername.requestFocus();
                break;
        }
    };

    public void initTextFields() {
        tfUsername.setOnKeyPressed(tabKeyHandler);
        pfPassword.setOnKeyPressed(tabKeyHandler);
        tfPassword.setOnKeyPressed(tabKeyHandler);
        cmbCompany.setOnKeyPressed(tabKeyHandler);
        cmbIndustry.setOnKeyPressed(tabKeyHandler);
    }

    public void setMainController(DashboardController controller) {
        this.dashboardController = controller;
    }
    double lnTop = 0.0;
    double lnBottom = 0.0;

    private void handleLoadingScreen(AnchorPane pane, JSONObject poJSON) {
        enterProceed = false;
        rootPane.requestFocus();
        spinnerPane = new AnchorPane();
        spinnerPane.setPrefSize(400, 300);
        spinnerPane.setStyle("-fx-background-color: #F4F4F4;");
        spinnerPane.setOpacity(0);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: #F37C01;");
        spinner.setPrefSize(25, 25);
        spinner.setMinSize(25, 25);
        spinner.setMaxSize(25, 25);
        AnchorPane.setTopAnchor(spinner, lnTop);
        AnchorPane.setBottomAnchor(spinner, lnBottom);
        AnchorPane.setLeftAnchor(spinner, 120.0);
        AnchorPane.setRightAnchor(spinner, 120.0);

        spinnerPane.getChildren().add(spinner);

        rootPane.getChildren().add(spinnerPane);
        AnchorPane.setTopAnchor(spinnerPane, 0.0);
        AnchorPane.setLeftAnchor(spinnerPane, 0.0);
        AnchorPane.setRightAnchor(spinnerPane, 0.0);
        AnchorPane.setBottomAnchor(spinnerPane, 0.0);

        // Fade out the given pane (login form)
        FadeTransition fadeOutLogin = new FadeTransition(Duration.millis(600), pane);
        fadeOutLogin.setFromValue(1.0);
        fadeOutLogin.setToValue(0.0);
        fadeOutLogin.setOnFinished(e -> {

            FadeTransition fadeInLoad = new FadeTransition(Duration.millis(600), spinnerPane);
            fadeInLoad.setFromValue(0.0);
            fadeInLoad.setToValue(1.0);
            fadeInLoad.play();
            double lnDuration = 0.8;
            if ("success".equals((String) poJSON.get("result"))) {
                lnDuration = 1.5;
                JFXUtil.playUpwardFadeOut(0.6, .70, bpBorderMain,apRoot1, apRoot2, vboxmain, rootPane);
            }
            PauseTransition wait = new PauseTransition(Duration.seconds(lnDuration));
            wait.setOnFinished(done -> {
                if (!"success".equals((String) poJSON.get("result"))) {
                    FadeTransition fadeOutLoad = new FadeTransition(Duration.millis(600), spinnerPane);
                    fadeOutLoad.setFromValue(1.0);
                    fadeOutLoad.setToValue(0.0);
                    fadeOutLoad.setOnFinished(ea -> {
                        enterProceed = true;
                        rootPane.getChildren().remove(spinnerPane);
                        Platform.runLater(() -> {
                            ShowMessageFX.Warning((String) poJSON.get("message"), MODULE, null);
                        });
                    });
                    fadeOutLoad.play();
                } else {
                    enterProceed = true;
                    try {
                        if (!oApp.logUser("gRider", (String) poJSON.get("userId"))) {
                            Platform.runLater(() -> {
                                rootPane.getChildren().remove(spinnerPane);
                                ShowMessageFX.Warning(oApp.getMessage(), MODULE, null);
                            });
                        } else {

                            DashboardController dashboardController = LoginControllerHolder.getMainController();
                            dashboardController.triggervbox2();
                            dashboardController.setUserIndustry(psIndustryID);
                            dashboardController.setUserCompany(psCompanyID);
                            dashboardController.changeUserInfo();
                            dashboardController.notificationChecker();

                            //set the orignal industry and company
                            System.setProperty("sys.industry", psIndustryID);
                            System.setProperty("sys.company", psCompanyID);

                            //set the original to selected industry and company
                            System.setProperty("user.selected.industry", psIndustryID);
                            System.setProperty("user.selected.company", psCompanyID);

                            LoginControllerHolder.setLogInStatus(true);
                        }
                    } catch (SQLException | GuanzonException | IOException ex) {
                        rootPane.getChildren().remove(spinnerPane);
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }

            });
            wait.play();
        });
        fadeOutLogin.play();

    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnSignIn":
                JSONObject poJSON = ValidateLogin();
                handleLoadingScreen(loadingPane, poJSON);
                break;
            case "btnEyeIcon":
                FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                if (pfPassword.isVisible()) {
                    tfPassword.setText(pfPassword.getText());
                    pfPassword.setVisible(false);
                    tfPassword.setVisible(true);
                    eyeIcon.setIcon(FontAwesomeIcon.EYE);
                    eyeIcon.setStyle("-fx-fill: gray; -glyph-size: 20; ");
                    btnEyeIcon.setGraphic(eyeIcon);
                } else {
                    pfPassword.setText(tfPassword.getText());
                    tfPassword.setVisible(false);
                    pfPassword.setVisible(true);
                    eyeIcon.setIcon(FontAwesomeIcon.EYE_SLASH);
                    eyeIcon.setStyle("-fx-fill: gray; -glyph-size: 20; ");
                    btnEyeIcon.setGraphic(eyeIcon);
                }
                break;
            default:
                break;
        }
    }

    public void reloadCompnanyItems() throws SQLException {
        companyOptions = FXCollections.observableArrayList(getAllCompanies((ModelLog_In_Industry) cmbIndustry.getSelectionModel().getSelectedItem()));
        cmbCompany.setItems(companyOptions);

        if (!companyOptions.isEmpty()) {
            cmbCompany.getSelectionModel().select(companyOptions.get(0));
        } else {
            cmbCompany.getSelectionModel().select(companyOptions.get(-1));
        }
    }

    EventHandler<ActionEvent> comboBoxHandler = event -> {
        ComboBox<?> source = (ComboBox<?>) event.getSource();
        String id = source.getId();
        switch (id) {
            case "cmbCompany":
                try {
                ModelLog_In_Company selectedCompany = (ModelLog_In_Company) cmbCompany.getSelectionModel().getSelectedItem();
                if (selectedCompany != null) {
                    psCompanyID = selectedCompany.getCompanyId();
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }
            break;
            case "cmbIndustry":
                try {
                ModelLog_In_Industry selectedIndustry = (ModelLog_In_Industry) cmbIndustry.getSelectionModel().getSelectedItem();
                if (selectedIndustry != null) {
                    psIndustryID = selectedIndustry.getIndustryID();

                    reloadCompnanyItems();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }
    };

    private void initComboBox() {
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbCompany, cmbIndustry);

        loadComboBoxItems();

        JFXUtil.setActionListener(comboBoxHandler, cmbIndustry, cmbCompany);
    }

    private void loadComboBoxItems() {
        try {
            boolean lbShow = oApp.getIndustry().equals(System.getProperty("sys.main.industry"))
                    || oApp.getIndustry().equals(System.getProperty("sys.general.industry"));

            if (lbShow) {
                psIndustryID = oApp.getIndustry();

                industryOptions = FXCollections.observableArrayList(getAllIndustries());
                companyOptions = FXCollections.observableArrayList(getAllCompanies(industryOptions.get(0)));

                cmbIndustry.setItems(industryOptions);
                if (!cmbIndustry.getItems().isEmpty()) {
                    cmbIndustry.getSelectionModel().select(0);
                    psIndustryID = industryOptions.get(0).getIndustryID();
                }

                cmbCompany.setItems(companyOptions);
                if (!cmbCompany.getItems().isEmpty()) {
                    //select that contains GMC as auto selected company only if the industry is 09
                    int index = findIndex(companyOptions, "Merchandising");
                    if ((index != 0) && hasWord(String.valueOf(cmbIndustry.getSelectionModel().getSelectedItem()), "General")) {
                        cmbCompany.getSelectionModel().select(index);
                        psCompanyID = companyOptions.get(index).getCompanyId();
                    } else {
                        cmbCompany.getSelectionModel().select(0);
                        psCompanyID = companyOptions.get(0).getCompanyId();
                    }
                }
            } else {
                psIndustryID = oApp.getIndustry();
                psCompanyID = oApp.getCompnyId();
            }

            cmbIndustry.setVisible(lbShow);
            cmbCompany.setVisible(lbShow);

            lnTop = lbShow ? 120.0 : 50.0;
            lnBottom = lbShow ? 120.0 : 185.0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean hasWord(String source, String word) {
        return source != null && word != null
                && source.toLowerCase().contains(word.toLowerCase());
    }

    public static int findIndex(ObservableList<ModelLog_In_Company> list, String word) {
        if (list == null || word == null) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).getCompanyName();
            if (name != null && name.toLowerCase().contains(word.toLowerCase())) {
                return i; // found index
            }
        }
        return 0; // not found
    }

    public JSONObject ValidateLogin() {
        JSONObject poJSON = new JSONObject();
        boolean isUsernameFilled = tfUsername.getText().trim().isEmpty();

        if (pfPassword.isVisible()) {
            tfPassword.setText(pfPassword.getText());
        } else {
            pfPassword.setText(tfPassword.getText());
        }

        boolean isPasswordFilled = tfPassword.getText().trim().isEmpty();
        if (!isUsernameFilled && !isPasswordFilled) {
            if (psIndustryID.equals("") || psCompanyID.equals("")) {
                JFXUtil.setJSONError(poJSON, "Please fill all the fields");
            } else {
                try {
                    SystemUser sysuser = new SystemUser();
                    sysuser.setApplicationDriver(oApp);
                    sysuser.setLogWrapper(null);

                    poJSON = sysuser.isValidUser(tfUsername.getText().trim(), tfPassword.getText().trim(), oApp.getProductID());

                    if ("success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                } catch (SQLException | GuanzonException ex) {
                    JFXUtil.setJSONError(poJSON, MiscUtil.getException(ex));
                }
            }
        } else {
            JFXUtil.setJSONError(poJSON, "Please fill all the fields");
        }

        return poJSON;
    }

    private List<ModelLog_In_Company> getAllCompanies(ModelLog_In_Industry industry) throws SQLException {
        List<ModelLog_In_Company> companies = new ArrayList<>();

        String lsSQL = "SELECT * FROM Company"
                + " WHERE cRecdStat = '1'"
                + " ORDER BY sCompnyNm";

        String lsCompany = industry.getCompanyID();
        String[] laCompany = lsCompany.split(";");

        lsCompany = "";
        for (int lnCtr = 0; lnCtr <= laCompany.length - 1; lnCtr++) {
            lsCompany += ", " + SQLUtil.toSQL(laCompany[lnCtr]);
        }

        if (!lsCompany.isEmpty()) {
            lsCompany = "(" + lsCompany.substring(2) + ")";
            lsSQL = MiscUtil.addCondition(lsSQL, "sCompnyID IN " + lsCompany);
        }

        ResultSet rs = oApp.executeQuery(lsSQL);
        while (rs.next()) {
            String id = rs.getString("sCompnyID");
            String name = rs.getString("sCompnyNm");
            companies.add(new ModelLog_In_Company(id, name));
        }

        MiscUtil.close(rs);

        return companies;
    }

    private List<ModelLog_In_Industry> getAllIndustries() throws SQLException {
        List<ModelLog_In_Industry> industries = new ArrayList<>();

        String lsSQL = "SELECT * FROM Industry"
                + " WHERE cRecdStat = '1'"
                + " ORDER BY sDescript";

        if (!psIndustryID.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryID));
        }

        ResultSet loRS = oApp.executeQuery(lsSQL);

        while (loRS.next()) {
            industries.add(new ModelLog_In_Industry(
                    loRS.getString("sIndstCdx"),
                    loRS.getString("sDescript"),
                    loRS.getString("sCompnyID")));
        }
        MiscUtil.close(loRS);

        return industries;
    }
}
