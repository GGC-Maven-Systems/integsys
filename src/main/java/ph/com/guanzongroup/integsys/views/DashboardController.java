package ph.com.guanzongroup.integsys.views;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.menu.MenuManager;
import org.guanzon.appdriver.agent.menu.TreeNode;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import ph.com.guanzongroup.cas.sysmonitor.SystemMonitorMenu;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONArray;
import ph.com.guanzongroup.integsys.views.AnimationPreviewManager.AnimTitle;

public class DashboardController implements Initializable {

    private final String MODULE = "Guanzon IntegSys";
    private GRiderCAS oApp;
    private String lastClickedBtnLeftSideBar = "";
    private String lastClickedBtnRightSideBar = "";

    private String psDefaultScreenFXML = "/ph/com/guanzongroup/integsys/views/Login.fxml";
    private String psDefaultScreenFXML2 = "/ph/com/guanzongroup/integsys/views/DefaultScreen.fxml";
//    private String psUserManagementFXML = "/ph/com/guanzongroup/integsys/views/UserManagement.fxml";

    private ToggleGroup toggleGroup;
    private static ToggleButton[] toggleBtnLeftUpperSideBar;
    private static Tooltip[] sideBarLeftUpperToolTip;

    private ToggleGroup toggleGroupLowerBtn;
    ToggleButton[] toggleBtnLeftLowerSideBar;
    private static Tooltip[] sideBarLeftLowerToolTip;

    private ToggleGroup toggleGroupRightSideBar;
    private static ToggleButton[] toggleBtnRightSideBar;
    private static Tooltip[] sideBarRightToolTip;
    private Map<TreeItem<String>, String> menuLocationMap = new HashMap<>();
    private boolean isListenerLeftAdded = false;
    private boolean isListenerRightAdded = false;

    private List<JSONObject> flatMenuItems;
    private int userLevel; // User's access level
    private int targetTabIndex = -1;
    private int intIndex = -1;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isFromFilter;

    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";

    List<String> tabName = new ArrayList<>();
    String psFormName = "";
    Object poController = null;
    boolean lbproceed = false;

    private final Map<String, Runnable> javaCommands = new HashMap<>();
    private Set<String> allowedMenuIds = new HashSet<>();
    List<Node> savedNodes = null;

    // Keep a shared lookup map for your left sidebar nodes
    private final Map<String, TreeNode> leftSidebarLookup = new HashMap<>();
    private final Map<AnchorPane, ToggleButton> lastOpenedButtonMap = new HashMap<>();
    @FXML
    private AnchorPane MainAnchor;
    @FXML
    private StackPane MainStack;
    @FXML
    private BorderPane main_container;
    @FXML
    private StackPane top_navbar;
    @FXML
    private Button btnMinimize;
    @FXML
    private Button btnClose;
    @FXML
    private AnchorPane anchorIconMenu;
    @FXML
    private VBox nav_bar;
    @FXML
    private ToggleButton btnMenu;
    @FXML
    private AnchorPane anchorIconMenu1, aprootleft;
    @FXML
    private VBox nav_bar1;
    @FXML
    private ToggleButton btnHelp;
    @FXML
    private ToggleButton btnLogout;
    @FXML
    private Label AppUser;
    @FXML
    private Label lblVersion;
    @FXML
    private AnchorPane apClock;
    @FXML
    private Label DateAndTime;
    @FXML
    private AnchorPane anchorSpacex, apAnchorTitle;
    @FXML
    private AnchorPane anchorSpace;
    @FXML
    private StackPane workingSpace;
    @FXML
    private Pane pane;
    @FXML
    private TabPane tabpane;
    @FXML
    private AnchorPane anchorRightSideBarMenu;
    @FXML
    private TreeView<TreeMonitor> tvRightSideBar;
    @FXML
    private AnchorPane anchorLeftSideBarMenu;
    @FXML
    private TreeView<TreeNode> tvLeftSideBar;
    @FXML
    private AnchorPane anchorIconMenu11;
    @FXML
    private VBox nav_bar11;
    @FXML
    private ToggleButton btnSysMonitor;
    @FXML
    private AnchorPane badgeNotification;
    @FXML
    private Label lblNotifCount;
    boolean menu1 = false;
    boolean menu2 = false;
    AnchorPane root;
    Scene scene;
    AnimationPreviewManager.AnimTitle animator = new AnimationPreviewManager.AnimTitle();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            setScene(loadAnimateAnchor(psDefaultScreenFXML));
            setPane();
            initMenu();

            ToggleGroupControlUpperLeftSideBar();
            ToggleGroupControlLowerLeftSideBar();
            ToggleGroupControlRightSideBar();

            loadUserInfo();
            getTime();
            setAppVersion("v1.00.01");

            initButtonClickActions();
            setTreeViewStyle(tvLeftSideBar);
            setTreeViewStyleMonitor(tvRightSideBar);
//            setDropShadowEffectsLeftSideBar(anchorLeftSideBarMenu);
//            setDropShadowEffectsRightSideBar(anchorRightSideBarMenu);
            JFXUtil.applyToggleHoverAnimation(btnMenu, btnHelp, btnLogout, btnSysMonitor);
            JFXUtil.applyHoverFadeToButtons("#FFFFFF", "#552B00", btnMinimize, btnClose);
            JFXUtil.placeClockInAnchorPane(apClock, 25);
            JSONObject poJSON = new JSONObject();
            JFXUtil.checkIfFolderExists(poJSON, System.getProperty("sys.default.path.temp") + "/Attachments//");
//            switch (System.getProperty("user.selected.industry")) {
//                case "06":
//                    loadDummyMenu();
//                    setAnchorPaneVisibleManage(true, anchorRightSideBarMenu);
//                    break;
//                case "09":
//                    loadDummyMenux();
//                    setAnchorPaneVisibleManage(true, anchorRightSideBarMenu);
//                    break;
//                default:
//                    setAnchorPaneVisibleManage(false, anchorRightSideBarMenu);
//            }
            apAnchorTitle.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((o, oldWin, newWin) -> {
                        if (newWin != null) {
                            newWin.showingProperty().addListener((ow, wasShowing, isShowing) -> {
                                if (isShowing) {
                                    animator.initialize(apAnchorTitle, 14);
                                }
                            });
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMenu(ActionEvent event) throws SQLException {
        loadMenu();
        toggleLeftSideBarMenuButton("btnMenu", 0);

    }

    @FXML
    private void switchHelp(ActionEvent event) {
    }

    @FXML
    private void switchLogout(ActionEvent event) {
    }

    @FXML
    private void switchSysMonitor(ActionEvent event) {
        loadSystemMonitor();
        toggleSidebarWidth();
        toggleRightSideBarMenuButton("btnSysMonitor", 0);
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    public void setUserIndustry(String lsIndustryId) {
        psIndustryID = lsIndustryId;
    }

    public void setUserCompany(String lsCompanyId) {
        psCompanyID = lsCompanyId;
    }
    boolean lbIsTriggered = false;

    private boolean animateMenu(AnchorPane pane, TreeView<?> tree,
            double targetWidth, double targetHeight, ToggleButton toggleButton,
            AnchorPane buttonParent, boolean isOpened) {

        ToggleButton lastBtn = lastOpenedButtonMap.get(pane);
        if (isOpened) {
            if (lastBtn == toggleButton) {
                // Collapse current menu
                lbIsTriggered = animateDiagonalMenu(pane, tree, targetWidth, targetHeight, toggleButton, buttonParent, isOpened);
                lastOpenedButtonMap.put(pane, null);
            } else {
                // Collapse previous (opened by different button) first
                if (lastBtn != null) {
                    lbIsTriggered = animateDiagonalMenu(pane, tree, targetWidth, targetHeight, lastBtn, buttonParent, isOpened);
                } else {
                    lbIsTriggered = animateDiagonalMenu(pane, tree, targetWidth, targetHeight, toggleButton, buttonParent, isOpened);
                }

                PauseTransition pt = new PauseTransition(Duration.millis(220));
                pt.setOnFinished(ev -> {
                    lbIsTriggered = animateDiagonalMenu(pane, tree, targetWidth, targetHeight, toggleButton, buttonParent, isOpened);
                });
                pt.play();

                lastOpenedButtonMap.put(pane, toggleButton);
            }
        } else {
            lbIsTriggered = animateDiagonalMenu(pane, tree, targetWidth, targetHeight, toggleButton, buttonParent, isOpened);
            lastOpenedButtonMap.put(pane, toggleButton);
        }
        return lbIsTriggered;
    }

    private boolean animateDiagonalMenu(
            AnchorPane pane,
            TreeView<?> tree,
            double targetWidth,
            double targetHeight,
            ToggleButton toggleButton,
            AnchorPane buttonParent, boolean isOpened
    ) {

        Rectangle clip = new Rectangle(0, 0);
        tree.setClip(clip);

        tree.layoutBoundsProperty().addListener((o, oldB, newB) -> {
            clip.setWidth(newB.getWidth());
            clip.setHeight(newB.getHeight());
        });

        try {
            toggleButton.localToScene(toggleButton.getBoundsInLocal());
        } catch (Exception e) {
            return false;
        }
        try {
            if (toggleButton.getBoundsInLocal() == null) {
                return isOpened;
            }
        } catch (Exception e) {
            return isOpened;
        }
        // Coordinates
        Bounds btnScene = toggleButton.localToScene(toggleButton.getBoundsInLocal());
        Bounds parentScene = pane.getParent().localToScene(buttonParent.getBoundsInLocal());
        buttonParent.getHeight();

        double parentHeight = buttonParent.getHeight();
        double startX;
        double startY = btnScene.getMinY() - parentScene.getMinY() + toggleButton.getHeight() / 2;
        Double leftAnchor = AnchorPane.getLeftAnchor(pane);
        Double rightAnchor = AnchorPane.getRightAnchor(pane);
        boolean isRightMenu = (rightAnchor != null && leftAnchor == null);
//            double startX;
        if (!isRightMenu) {
            // LEFT menu: expand from button center rightwards
            startX = parentScene.getMinX() - 50;
            AnchorPane.setRightAnchor(pane, null);   // remove conflicts
        } else {
            // RIGHT menu: expand from button center leftwards
            startX = parentScene.getWidth() - buttonParent.getPrefWidth() - 1;
            AnchorPane.setLeftAnchor(pane, null);    // remove conflicts
        }

        // Set initial left/right anchor
        if (!isRightMenu) {
            AnchorPane.setLeftAnchor(pane, startX);
        } else {
            AnchorPane.setRightAnchor(pane, startX - 40);
        }

        // FINAL top position (must align with buttonParent top)
        double finalTopAnchor = 0; // since anchor relative to parent container
        double finalHeight = parentHeight;

        // Set starting anchor
        AnchorPane.setTopAnchor(pane, startY);

        if (!isOpened) {

            pane.setVisible(true);
            tree.setVisible(true);

            // Start collapsed
            pane.setPrefWidth(0);
            pane.setPrefHeight(0);
            tree.setPrefWidth(0);
            tree.setPrefHeight(0);

            // Animated properties
            DoubleProperty animatedHeight = new SimpleDoubleProperty(0);
            DoubleProperty animatedTop = new SimpleDoubleProperty(startY);

            // Keep updating the pane in realtime
            animatedHeight.addListener((obs, oldVal, newVal) -> {
                pane.setPrefHeight(newVal.doubleValue());
                tree.setPrefHeight(newVal.doubleValue());
            });

            animatedTop.addListener((obs, oldVal, newVal) -> {
                AnchorPane.setTopAnchor(pane, newVal.doubleValue());
            });

            Timeline expand = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(pane.prefWidthProperty(), 0),
                            new KeyValue(animatedHeight, 0),
                            new KeyValue(animatedTop, startY),
                            new KeyValue(tree.prefWidthProperty(), 0)
                    ),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(pane.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
                            new KeyValue(tree.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
                            // Height becomes parent full height
                            new KeyValue(animatedHeight, finalHeight, Interpolator.EASE_BOTH),
                            // Top anchor moves to buttonParent's top
                            new KeyValue(animatedTop, finalTopAnchor, Interpolator.EASE_BOTH)
                    )
            );

            expand.setOnFinished(e -> tree.setClip(null));
            expand.play();
            return true;
        } else {
            if (pane.isVisible()) {
                double currentWidth = pane.getPrefWidth();
                if (pane.getPrefWidth() != targetWidth) {
                    currentWidth = targetWidth;
                }
                double currentHeight = pane.getPrefHeight() - 30;

                DoubleProperty animatedTop = new SimpleDoubleProperty(0);
                animatedTop.addListener((obs, oldVal, newVal) -> AnchorPane.setTopAnchor(pane, newVal.doubleValue()));

                Timeline collapse = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(pane.prefWidthProperty(), currentWidth),
                                new KeyValue(tree.prefWidthProperty(), currentWidth),
                                new KeyValue(pane.prefHeightProperty(), currentHeight),
                                new KeyValue(tree.prefHeightProperty(), currentHeight),
                                new KeyValue(animatedTop, 0)
                        ),
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(pane.prefWidthProperty(), 0, Interpolator.EASE_BOTH),
                                new KeyValue(tree.prefWidthProperty(), 0, Interpolator.EASE_BOTH),
                                new KeyValue(pane.prefHeightProperty(), 0, Interpolator.EASE_BOTH),
                                new KeyValue(tree.prefHeightProperty(), 0, Interpolator.EASE_BOTH),
                                new KeyValue(animatedTop, startY, Interpolator.EASE_BOTH)
                        )
                );

                collapse.setOnFinished(e -> {
                    pane.setVisible(false);
                    AnchorPane.setTopAnchor(pane, startY); // ready for next expand
                });

                collapse.play();
            }

        }
        return !isOpened;
    }

    private Pane loadAnimateAnchor(String fxml) {
        ScreenInterface fxObj = getController(fxml);
        fxObj.setGRider(oApp);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxObj.getClass().getResource(fxml));
        fxmlLoader.setController(fxObj);

        Pane root;
        try {
            root = (Pane) fxmlLoader.load();
            FadeTransition ft = new FadeTransition(Duration.millis(1500));
            ft.setNode(root);
            ft.setFromValue(1);
            ft.setToValue(1);
            ft.setCycleCount(1);
            ft.setAutoReverse(false);
            ft.play();

            return root;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }

    private ScreenInterface getController(String fxml) {
        switch (fxml) {
            case "Login.fxml":
            case "/ph/com/guanzongroup/integsys/views/Login.fxml":
                LoginControllerHolder locontroller = new LoginControllerHolder();
                locontroller.setMainController(this);
                return new LoginController();
            case "/ph/com/guanzongroup/integsys/views/DefaultScreen.fxml":
                return new DefaultScreenController();
            case "/ph/com/guanzongroup/integsys/views/UserManagement.fxml":
                return new UserManagement_Controller();
            default:
                return null;
        }
    }

    private void setScene(Pane foPane) {
        workingSpace.getChildren().clear();
        workingSpace.getChildren().add(foPane);
        tabpane.setVisible(false);
        tabpane.setManaged(false);
    }

    private void setScene2(TabPane foPane) {
        workingSpace.getChildren().clear();
        workingSpace.getChildren().add(foPane);
    }

    private void setPane() {
        pane.setOnMouseClicked(event -> {
            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            animator.expandContract(true, () -> {
            });
            for (int i = 0; i < toggleBtnLeftUpperSideBar.length; i++) {
                toggleBtnLeftUpperSideBar[i].setSelected(false);
            }

            for (int i = 0; i < toggleBtnRightSideBar.length; i++) {
                toggleBtnRightSideBar[i].setSelected(false);
            }

        });
    }

    private void setAnchorPane() {
        pane.setOnMouseClicked(event -> {
            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            for (int i = 0; i < toggleBtnLeftUpperSideBar.length; i++) {
                toggleBtnLeftUpperSideBar[i].setSelected(false);
            }

            for (int i = 0; i < toggleBtnRightSideBar.length; i++) {
                toggleBtnRightSideBar[i].setSelected(false);
            }
        });
    }

    private void setAnchorPaneVisibleManage(boolean fbVisibleManage, Node... nodes) {
        for (Node node : nodes) {
            if (node != null) {
                node.setVisible(fbVisibleManage);
                node.setManaged(fbVisibleManage);
            }
        }
    }

    private void initMenu() {
        setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
        setAnchorPaneVisibleManage(false, anchorRightSideBarMenu);
    }
    ChangeListener<Boolean> toggleSelectedListener = (obs, oldVal, newVal) -> {
        setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
        if (newVal) { //toggle on
            if (savedNodes != null) {
                savedNodes.clear();
            }
            savedNodes = new ArrayList<>(workingSpace.getChildren());
//            setScene(loadAnimateAnchor(psUserManagementFXML));
        } else { //toggle off
            //define also if logged in or not, if logged in
            if (LoginControllerHolder.getLogInStatus()) {
                tabpane.setVisible(true);
                tabpane.setManaged(true);

                workingSpace.getChildren().clear();
                workingSpace.getChildren().setAll(savedNodes);

            } else {
                setScene(loadAnimateAnchor(psDefaultScreenFXML));
            }
        }
    };

    private void ToggleGroupControlLowerLeftSideBar() {
        toggleGroupLowerBtn = new ToggleGroup();
        toggleBtnLeftLowerSideBar = new ToggleButton[]{
            btnHelp,
            btnLogout
        };

        String[] tooltipTexts = {
            "Help",
            "Exit"
        };

        for (int i = 0; i < toggleBtnLeftLowerSideBar.length; i++) {
            toggleBtnLeftLowerSideBar[i].setTooltip(new Tooltip(tooltipTexts[i]));
            toggleBtnLeftLowerSideBar[i].setToggleGroup(toggleGroupLowerBtn);
        }

        toggleBtnLeftLowerSideBar[0].selectedProperty().removeListener(toggleSelectedListener);
        toggleBtnLeftLowerSideBar[0].selectedProperty().addListener(toggleSelectedListener);
    }

    private void ToggleGroupControlRightSideBar() {
        toggleGroupRightSideBar = new ToggleGroup();
        toggleBtnRightSideBar = new ToggleButton[]{
            btnSysMonitor
        };

        // Tooltip texts for each button
        String[] tooltipTexts = {
            "System Monitor"
        };

        // Assign tooltips and toggle group in a loop
        for (int i = 0; i < toggleBtnRightSideBar.length; i++) {
            toggleBtnRightSideBar[i].setTooltip(new Tooltip(tooltipTexts[i]));
            toggleBtnRightSideBar[i].setToggleGroup(toggleGroupRightSideBar);
        }
    }

    private void ToggleGroupControlUpperLeftSideBar() {
        toggleGroup = new ToggleGroup();
        toggleBtnLeftUpperSideBar = new ToggleButton[]{
            btnMenu,};

        // Tooltip texts for each button
        String[] tooltipTexts = {
            "Menu",};

        // Assign tooltips to buttons
        for (int i = 0; i < toggleBtnLeftUpperSideBar.length; i++) {
            if (toggleBtnLeftUpperSideBar[i].isVisible()) { // Skip setting tooltip for hidden buttons
                toggleBtnLeftUpperSideBar[i].setTooltip(new Tooltip(tooltipTexts[i]));
                toggleBtnLeftUpperSideBar[i].setToggleGroup(toggleGroup);
            }
        }
    }

    private void loadUserInfo() {
        try {
            if (nav_bar.isDisabled()) {
                AppUser.setText("");
            } else {
                AppUser.setText(oApp.getLogName() + " || " + getIndustryName(oApp.getIndustry()));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getIndustryName(String industryid) throws SQLException {
        String industryname = "";

        String lsSQL = "SELECT * FROM Industry"
                + " WHERE cRecdStat = '1'"
                + " AND sIndstCdx = " + SQLUtil.toSQL(industryid);

        ResultSet loRS = oApp.executeQuery(lsSQL);

        if (loRS.next()) {
            industryname = loRS.getString("sDescript");
        }

        MiscUtil.close(loRS);
        return industryname;
    }

    private void setAppVersion(String fsValue) {
        lblVersion.setText("Guanzon Integrated Systems " + fsValue);
    }

    private void getTime() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Calendar cal = Calendar.getInstance();
            int second = cal.get(Calendar.SECOND);

            Date date = new Date();
            String strTimeFormat = "hh:mm:";
            String strDateFormat = "MMMM dd, yyyy";
            String secondFormat = "ss";

            DateFormat timeFormat = new SimpleDateFormat(strTimeFormat + secondFormat);
            DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

            String formattedTime = timeFormat.format(date);
            String formattedDate = dateFormat.format(date);

            DateAndTime.setText(formattedDate + " || " + formattedTime);
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void initButtonClickActions() {
        btnClose.setOnAction(this::handleButtonAction);
        btnMinimize.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            switch (clickedButton.getId()) {
                case "btnClose":
                    Platform.exit();
                    break;
                case "btnMinimize":
                    Stage stage = (Stage) btnMinimize.getScene().getWindow();
                    stage.setIconified(true);
                    break;
                // Add more cases for other buttons if needed
            }
        }
    }

    public void notificationChecker() {
        ScheduledService<Void> service = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        checkNotifications();
                        return null;
                    }
                };
            }
        };
        service.setPeriod(Duration.minutes(1));
        service.start();
    }

    private void checkNotifications() {
//        notificationCount += (int) (Math.random() * 5);
//        cartCount += (int) (Math.random() * 5);
//        if (btnSysMonitor.isDisabled()) {
//            return;
//        }
        Platform.runLater(() -> {
            if (tvRightSideBar.isShowRoot()) {
                //refresh the tree monitor
                loadSystemMonitor();
            } else {
                SystemMonitorMenu monitorMenu = new SystemMonitorMenu(oApp, "CAS");

                monitorMenu.setIndustryCode(psIndustryID);
                monitorMenu.setCategoryCode(oApp.getCategory());

                monitorMenu.setCompanyFilter(new String[]{psCompanyID});
                JSONObject loSysMontrData = monitorMenu.processMonitoring();
                lblNotifCount.setText(String.valueOf(monitorMenu.getMonitoringCount()));

            }

        });
    }

    private void setTreeViewStyle(TreeView<TreeNode> treeView) {
        treeView.setCellFactory(tv -> new TreeCell<TreeNode>() {
            @Override
            protected void updateItem(TreeNode item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                    setStyle("-fx-background-color: #DFDFDF; -fx-border-color: #DFDFDF;");
                    if (!getStyleClass().contains("empty-tree-cell")) {
                        getStyleClass().add("empty-tree-cell");
                    }
                    setCursor(Cursor.DEFAULT);
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                    setOnMouseClicked(null);
                } else {
                    setText(item.getName());
                    setGraphic(getTreeItem().getGraphic());
                    getStyleClass().remove("empty-tree-cell");

                    boolean isContainer = item.getCommandType() == null || item.getCommandType().isEmpty();

                    // ✅ Tooltip with description (if available)
                    if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                        setTooltip(new Tooltip(item.getDescription()));
                    } else {
                        setTooltip(null);
                    }

                    if (isContainer) {
                        // 🎨 Container node styling
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
                        setCursor(Cursor.DEFAULT);
                    } else {
                        // 🎨 Command node styling
                        setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                        setCursor(Cursor.HAND);
                    }

                    // ✅ Hover effect only for command nodes
                    if (!isContainer) {
                        setOnMouseEntered(e -> {
                            setStyle("-fx-background-color: #E6F2FF; -fx-font-weight: normal; -fx-text-fill: black;");
                            setCursor(Cursor.HAND);
                        });
                        setOnMouseExited(e -> {
                            setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                            setCursor(Cursor.HAND);
                        });
                    } else {
                        setOnMouseEntered(null);
                        setOnMouseExited(null);
                    }

                    // ✅ Handle click actions
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1) {
                            if (isContainer) {
                                // Container: expand/collapse only
                                TreeItem<TreeNode> treeItem = getTreeItem();
                                if (treeItem != null && !treeItem.isLeaf()) {
                                    treeItem.setExpanded(!treeItem.isExpanded());
                                    event.consume();
                                }
                            } else {
                                // Executable node: run assigned action
                                if (item.getAction() != null) {
                                    item.getAction().run();
                                } else {
                                    System.out.println("⚠ No action defined for: " + item.getName());
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void setTreeViewStyleMonitor(TreeView<TreeMonitor> treeView) {
        treeView.setCellFactory(tv -> new javafx.scene.control.TreeCell<TreeMonitor>() {
            @Override
            protected void updateItem(TreeMonitor item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                    setStyle("-fx-background-color: #DFDFDF; -fx-border-color: #DFDFDF;");
                    if (!getStyleClass().contains("empty-tree-cell")) {
                        getStyleClass().add("empty-tree-cell");
                    }
                    setCursor(javafx.scene.Cursor.DEFAULT);
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                    setOnMouseClicked(null);
                } else {
                    setText(item.getName());
                    setGraphic(getTreeItem().getGraphic());
                    getStyleClass().remove("empty-tree-cell");

                    // Container node: action == null
                    boolean isContainer = item.getAction() == null;

                    // Tooltip with description (if available)
                    if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                        setTooltip(new javafx.scene.control.Tooltip(item.getDescription()));
                    } else {
                        setTooltip(null);
                    }

                    if (isContainer) {
                        // Container node styling
                        setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");
                        setCursor(javafx.scene.Cursor.DEFAULT);
                    } else {
                        // Executable node styling
                        setStyle("-fx-font-size: 12px; -fx-font-weight: normal; -fx-text-fill: black;");
                        setCursor(javafx.scene.Cursor.HAND);
                    }

                    // Hover effect only for command/executable nodes
                    if (!isContainer) {
                        setOnMouseEntered(e -> {
                            setStyle("-fx-font-size: 12px; -fx-background-color: #E6F2FF; -fx-font-weight: normal; -fx-text-fill: black;");
                            setCursor(javafx.scene.Cursor.HAND);
                        });
                        setOnMouseExited(e -> {
                            setStyle("-fx-font-size: 12px;-fx-font-weight: normal; -fx-text-fill: black;");
                            setCursor(javafx.scene.Cursor.HAND);
                        });
                    } else {
                        setOnMouseEntered(null);
                        setOnMouseExited(null);
                    }

                    // Handle click actions
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1) {
                            if (isContainer) {
                                // Expand/collapse only
                                javafx.scene.control.TreeItem<TreeMonitor> treeItem = getTreeItem();
                                if (treeItem != null && !treeItem.isLeaf()) {
                                    treeItem.setExpanded(!treeItem.isExpanded());
                                    event.consume();
                                }
                            } else {
                                // Executable node: run assigned action
                                if (item.getAction() != null) {
                                    item.getAction().run();
                                } else {
                                    System.out.println("⚠ No action defined for: " + item.getName());
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void setDropShadowEffectsLeftSideBar(AnchorPane anchorPane) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setWidth(21.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));

        shadow.setOffsetX(2);
        shadow.setOffsetY(0);

        anchorPane.setEffect(shadow);
    }

    private void setDropShadowEffectsRightSideBar(AnchorPane anchorPane) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setWidth(21.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));

        shadow.setOffsetX(-2);
        shadow.setOffsetY(0);

        // Apply to the AnchorPane
        anchorPane.setEffect(shadow);
    }

    public Tab getTab() {
        Tab currentTab = tabpane.getSelectionModel().getSelectedItem();
        return currentTab;
    }

    ChangeListener<Scene> WindowKeyEvent = (obs, oldScene, newScene) -> {
        if (newScene != null) {
            setKeyEvent(newScene);
        }
    };

    private void initKeyEvent() {
        Platform.runLater(() -> {
            root = (AnchorPane) MainAnchor;
            scene = root.getScene();
            if (scene != null) {
                setKeyEvent(scene);
            } else {
                root.sceneProperty().addListener(WindowKeyEvent);
            }
        });
    }

    private void setKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F12) {
                eventf12(getTab());
            }
        });
    }

    public void removewindowEvent() {
        if (root != null) {
            root.sceneProperty().removeListener(WindowKeyEvent);
        }
        if (scene != null) {
            scene.setOnKeyPressed(null);
        }
    }

    public void eventf12(Tab currentTab) {
        if (LoginControllerHolder.getLogInStatus()) {
            //check here if the user level is supervisor
            //check if the current tab is not entry
            if (LoginControllerHolder.getLogInStatus()) {
                if (currentTab != null) {
                    try {
                        if (oApp.getIndustry().equals(System.getProperty("sys.main.industry"))
                                || oApp.getIndustry().equals(System.getProperty("sys.general.industry"))) {
                            psFormName = tabpane.getSelectionModel().getSelectedItem().getText();
                            loadSelectIndustryAndCompany();
                        }
                    } catch (IOException e) {
                        ShowMessageFX.Warning("Unable to load selection window.", "Error", e.getMessage());
                    }
                }
            }
        }
    }

    private void loadSelectIndustryAndCompany() throws IOException {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/ph/com/guanzongroup/integsys/views/SelectIndustryCompany.fxml"));
            SelectIndustryCompany loControl = new SelectIndustryCompany();
            loControl.setGRider(oApp);
            fxmlLoader.setController(loControl);

            //get industry of current opend form
            SetTabTitle(psFormName);
            String lsOldForm = getFormIndustry(psIndustryID, psCategoryID);
            String lsOldCompany = psCompanyID;
            //load the main interface
            Parent parent = fxmlLoader.load();

            parent.setOnMousePressed((MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            parent.setOnMouseDragged((MouseEvent event) -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            //set the main interface as the scene/*
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            scene.setFill(Color.TRANSPARENT);
            stage.setTitle("");
            stage.showAndWait();
            if (loControl.isFromFilter()) {
                psIndustryID = loControl.getSelectedIndustryID();
                psCompanyID = loControl.getSelectedCompanyID();
                psCategoryID = loControl.getSelectedCategoryID();
                String lsIndustry = getFormIndustry(psIndustryID, psCategoryID);
                //change form name base on selected industry
                //  /com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationCar.fxml

                JSONObject loJSON = getFxml(psFormName);
                if ("error".equals((String) loJSON.get("result"))) {
                    ShowMessageFX.Warning("This form is currently unavailable.", "Computerized Accounting System", MODULE);
                    return;
                }
                if (oApp != null) {
                    //Mandatory close the current tab
                    for (Tab tab : tabpane.getTabs()) {
                        if (tab.getText().equals(psFormName)) {
                            tabpane.getTabs().remove(tab);
                            tabName.remove(psFormName);
                            break;
                        }
                    }
                    boolean isNewTab = (checktabs(SetTabTitle(psFormName)) == 1);
                    if (isNewTab || !lsOldCompany.equals(psCompanyID)) {
                        String command = (String) loJSON.get("sCommandx");
                        String commandType = (String) loJSON.get("sCmdTypex");
                        TreeNode node = new TreeNode((String) loJSON.get("sMenuCDxx"),
                                (String) loJSON.get("sMenuGrpx"),
                                (String) loJSON.get("sMenuName"),
                                (String) loJSON.get("sMenuDesc"),
                                command,
                                commandType,
                                (String) loJSON.get("sFormName"),
                                (String) loJSON.get("sObjectNm"),
                                (String) loJSON.get("sIndstCdx"),
                                (String) loJSON.get("sCategrCd"));

                        if (!command.isEmpty() && !commandType.isEmpty()) {
                            if (node.getFxmlPath() != null) {
                                setScene2(loadAnimate(node));
                            }
                        } else {
                            ShowMessageFX.Warning("This form is currently unavailable.", "Computerized Accounting System", MODULE);
                        }
                    } else {
                        ShowMessageFX.Warning("This form is already active.", "Computerized Accounting System", MODULE);
                    }
                    setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
                    animator.expandContract(true, () -> {
                    });
                    for (ToggleButton navButton : toggleBtnLeftUpperSideBar) {
                        navButton.setSelected(false);
                    }
                    pane.requestFocus();
                }

                isFromFilter = loControl.isFromFilter();
            }
        } catch (IOException e) {
            ShowMessageFX.Warning(e.getMessage(), "Warning", null);
            System.exit(1);

        }
    }

    private JSONObject getFxml(String fsFormName) {
        JSONObject loJSON = new JSONObject();
        try {
            String lsSQL = "SELECT a.sMenuCDxx"
                    + ", a.sMenuGrpx"
                    + ", a.sMenuName"
                    + ", a.sMenuDesc"
                    + ", b.sCommandx"
                    + ", b.sCmdTypex"
                    + ", b.sFormName"
                    + ", b.sObjectNm"
                    + ", b.sIndstCdx"
                    + ", b.sCategrCd from xxxSysMenu a "
                    + "LEFT JOIN xxxSysMenuOthers b ON b.sMenuCDxx = a.sMenuCDxx AND b.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE);
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sMenuDesc LIKE " + SQLUtil.toSQL(fsFormName)
                    + " AND a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
//            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = oApp.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if (loRS.next()) {
                        loJSON.put("sMenuCDxx", loRS.getString("sMenuCDxx"));
                        loJSON.put("sMenuGrpx", loRS.getString("sMenuGrpx"));
                        loJSON.put("sMenuName", loRS.getString("sMenuName"));
                        loJSON.put("sMenuDesc", loRS.getString("sMenuDesc"));
                        loJSON.put("sCommandx", loRS.getString("sCommandx"));
                        loJSON.put("sCmdTypex", loRS.getString("sCmdTypex"));
                        loJSON.put("sFormName", loRS.getString("sFormName"));
                        loJSON.put("sObjectNm", loRS.getString("sObjectNm"));
                        loJSON.put("sIndstCdx", loRS.getString("sIndstCdx"));
                        loJSON.put("sCategrCd", loRS.getString("sCategrCd"));
                        return loJSON;
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        loJSON.put("result", "error");
        return loJSON;
    }

    public String SetTabTitle(String menuaction) {
        if (menuaction.contains(".fxml")) {
        }

        return null;
    }

    private String getFormIndustry(String industryId, String categoryId) {
        String concatName = "";
        switch (industryId) {
            case "01":
                concatName = "MP";
                break;
            case "02":
                if ("0003".equals(categoryId)) {
                    concatName = "MC";   // Motorcycle
                }
                if ("0004".equals(categoryId)) {
                    concatName = "SPMC"; // Spare Parts
                }
            case "03":
                if ("0005".equals(categoryId)) {
                    concatName = "Car";   // Vehicle
                }
                if ("0006".equals(categoryId)) {
                    concatName = "SPCar"; // Spare Parts
                }
                break;
            case "04":
                if ("0021".equals(categoryId)) {
                    concatName = "MonarchFood";  // Food Service
                }
                if ("0009".equals(categoryId)) {
                    concatName = "MonarchHospitality";  // Hospitality
                }
                break;
            case "05":
                concatName = "LP";
                break;
            case "07":
                concatName = "Appliances";
                break;
            case "00":
                concatName = "";
                break;
            default:
                concatName = "";
                break;
        }
        return concatName;
    }

    public int checktabs(String tabtitle) {
        int ctr = -1;

        if (tabpane.getTabs().isEmpty()) {
            return ctr;
        }

        for (Tab tab : tabpane.getTabs()) {
            ctr++;
            if (tab.getText().equals(tabtitle)) {
                tabpane.getSelectionModel().select(tab);
                return ctr;
            }
        }

        return -1;
    }

    public void setTabPane() {
        tabpane.setOnMouseClicked(event -> {
//            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);

            animateMenu(
                    anchorLeftSideBarMenu, // AnchorPane pane
                    tvLeftSideBar, // TreeView<?> tree
                    300, // targetWidth,
                    aprootleft.getHeight(),
                    btnMenu,
                    aprootleft, true
            );
            animator.expandContract(true, () -> {
            });
            for (int i = 0; i < toggleBtnLeftUpperSideBar.length; i++) {
                toggleBtnLeftUpperSideBar[i].setSelected(false);
            }

            for (int i = 0; i < toggleBtnRightSideBar.length; i++) {
                toggleBtnRightSideBar[i].setSelected(false);
            }
            if (tabpane.getSelectionModel().getSelectedItem() != null) {
                //psFormName = getFormName(tabpane.getSelectionModel().getSelectedItem().getText());
                psFormName = tabpane.getSelectionModel().getSelectedItem().getText();
            }
        });

        tabpane.setOnDragDetected(event -> {
            Dragboard db = tabpane.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(tabpane.getSelectionModel().getSelectedItem().getText());
            db.setContent(content);
            event.consume();
        });

        tabpane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        tabpane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String tabText = db.getString();
                int draggedTabIndex = findTabIndex(tabText);
                double mouseX = event.getX();
                double mouseY = event.getY();
                double tabHeaderHeight = tabpane.lookup(".tab-header-area").getBoundsInParent().getHeight();

                targetTabIndex = (int) (mouseX / 180);
                if (mouseY < tabHeaderHeight) {

                    if (draggedTabIndex != targetTabIndex) {
                        Tab draggedTab = tabpane.getTabs().remove(draggedTabIndex);
                        if (targetTabIndex > tabpane.getTabs().size()) {
                            targetTabIndex = tabpane.getTabs().size();
                        }
                        tabpane.getTabs().add(targetTabIndex, draggedTab);
                        tabpane.getSelectionModel().select(draggedTab);
                        success = true;
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        tabpane.setOnDragDone(event -> {
            event.consume();
        });

    }

    private int findTabIndex(String tabText) {
        ObservableList<Tab> tabs = tabpane.getTabs();
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).getText().equals(tabText)) {
                return i;
            }
        }
        return -1;
    }

    public TabPane loadAnimateExchange(String fsFormName) {
        setTabPane();
        setPane();

        ScreenInterface fxObj = getController(fsFormName);
        fxObj.setGRider(oApp);
        fxObj.setCompanyID(psCompanyID);
        fxObj.setIndustryID(psIndustryID);
        fxObj.setCategoryID(psCategoryID);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxObj.getClass().getResource(fsFormName));
        fxmlLoader.setController(fxObj);

        try {
            Node content = fxmlLoader.load();
            Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();

            if (selectedTab != null) {
                // Update title and content of the selected tab
                String newTitle = SetTabTitle(fsFormName);

                // Update tab name in the tracking list
                int index = tabName.indexOf(selectedTab.getText());
                if (index != -1) {
                    tabName.set(index, newTitle);
                }

                selectedTab.setText(newTitle);
                selectedTab.setContent(content);
                selectedTab.setContextMenu(createContextMenu(tabpane, selectedTab, oApp));

                selectedTab.setOnCloseRequest(event -> {
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure, do you want to close tab?")) {
                        tabName.remove(selectedTab.getText());
                        Tabclose();
                    } else {
                        event.consume();
                    }
                });
                selectedTab.setOnSelectionChanged(event -> {
                    ObservableList<Tab> tabs = tabpane.getTabs();
                    for (Tab tab : tabs) {
                        if (tab.getText().equals(selectedTab.getText())) {
                            tabName.remove(selectedTab.getText());
                            tabName.add(selectedTab.getText());
                            break;
                        }
                    }
                });
            }

            return tabpane;

        } catch (IOException e) {
            ShowMessageFX.Warning(e.getMessage(), "FXML Load Error", null);
            return null;
        }
    }

    public String getFormName(String fsTabTitle) {
        return null;
    }

    public ContextMenu createContextMenu(TabPane tabPane, Tab tab, GRiderCAS oApp) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem closeTabItem = new MenuItem("Close Tab");
        MenuItem closeOtherTabsItem = new MenuItem("Close Other Tabs");
        MenuItem closeAllTabsItem = new MenuItem("Close All Tabs");

        closeTabItem.setOnAction(event -> closeSelectTabs(tabPane, tab));
        closeOtherTabsItem.setOnAction(event -> closeOtherTabs(tabPane, tab));
        closeAllTabsItem.setOnAction(event -> closeAllTabs(tabPane, oApp));

        contextMenu.getItems().add(closeTabItem);
        contextMenu.getItems().add(closeOtherTabsItem);
        contextMenu.getItems().add(closeAllTabsItem);

        tab.setContextMenu(contextMenu);

        closeOtherTabsItem.visibleProperty().bind(Bindings.size(tabPane.getTabs()).greaterThan(1));

        return contextMenu;
    }

    private void closeSelectTabs(TabPane tabPane, Tab currentTab) {
        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure, do you want to close tab?")) {
            // Remove the tab
            if (tabPane.getTabs().removeIf(tab -> tab == currentTab)) {
                if (tabPane.getTabs().isEmpty()) {
                    unloadForm unload = new unloadForm();
                    StackPane myBox = (StackPane) tabPane.getParent();
                    myBox.getChildren().clear();
                    myBox.getChildren().add(unload.getScene(psDefaultScreenFXML2, oApp));
                }
            }
            tabName.remove(currentTab.getText());
        }
    }

    private void closeOtherTabs(TabPane tabPane, Tab currentTab) {
        if (ShowMessageFX.YesNo(null, "Close Other Tab", "Are you sure, do you want to close other tab?")) {
            tabPane.getTabs().removeIf(tab -> tab != currentTab);
            List<String> currentTabNameList = Collections.singletonList(currentTab.getText());
            tabName.retainAll(currentTabNameList);
            for (Tab tab : tabPane.getTabs()) {
                String formName = tab.getText();
            }
        }
    }

    private void closeAllTabs(TabPane tabPane, GRiderCAS oApp) {
        if (tabPane == null) {
            System.out.println("tabPane is null");
            return;
        }

        if (ShowMessageFX.YesNo(null, "Close All Tabs", "Are you sure, do you want to close all tabs?")) {
            if (tabName != null) {
                tabName.clear();
            } else {
                System.out.println("tabName is null");
            }

            while (!tabPane.getTabs().isEmpty()) {
                int tabCount = tabPane.getTabs().size(); // count tabs before removing

                for (int i = 0; i < tabCount; i++) {
                    Tab tab = tabPane.getTabs().remove(0);

                    EventHandler<Event> onClosed = tab.getOnClosed();
                    if (onClosed != null) {
                        lbproceed = true;
                        onClosed.handle(new Event(Event.ANY));
                    }
                }
            }

            unloadForm unload = new unloadForm();

            if (tabPane.getParent() == null) {
                System.out.println("Parent of tabPane is null");
                return;
            }

            StackPane myBox = (StackPane) tabPane.getParent();
            myBox.getChildren().clear();
            myBox.getChildren().add(unload.getScene(psDefaultScreenFXML2, oApp));
        }
    }

//    public void TabUserManagement1() { // if logged in
//        setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
//        tabpane.setVisible(true);
//        tabpane.setManaged(true);
//        workingSpace.getChildren().clear();
//        workingSpace.getChildren().setAll(savedNodes);
//    }
//
//    public void TabUserManagement2() { //if not logged in
//        setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
//        setScene(loadAnimateAnchor(psUserManagementFXML));
//    }
    public void Tabclose() {
        int tabsize = tabpane.getTabs().size();
        if (tabsize == 1) {
            setScene(loadAnimateAnchor(psDefaultScreenFXML2));
        }
    }

    public void Tabclose(TabPane tabpane) {
        int tabsize = tabpane.getTabs().size();
        if (tabsize == 1) {
            setScene(loadAnimateAnchor(psDefaultScreenFXML2));
        }
    }

    public TabPane loadAnimate(TreeNode node) {
        //set fxml controller class
        if (tabpane.getTabs().isEmpty()) {
            tabpane = new TabPane();
        }

        setTabPane();
        setPane();

        try {
            Class<?> cls = Class.forName(node.getControllerClass());
            ScreenInterface fxObj = (ScreenInterface) cls.getDeclaredConstructor().newInstance();

            fxObj.setGRider(oApp);
            fxObj.setIndustryID(node.getIndustry());
            fxObj.setCompanyID(psCompanyID);
            fxObj.setCategoryID(node.getCategory());

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(fxObj.getClass().getResource(node.getFxmlPath()));
            fxmlLoader.setController(fxObj);

            Tab newTab = new Tab(node.getDescription());
            newTab.setContent(new javafx.scene.control.Label("Content of Tab " + node.getFxmlPath()));

            // ✅ Store controller reference in the tab
            newTab.setUserData(fxObj);

            newTab.setContextMenu(createContextMenu(tabpane, newTab, oApp));
            tabName.add(node.getDescription());

            Node content = fxmlLoader.load();
            newTab.setContent(content);
            tabpane.getTabs().add(newTab);
            tabpane.getSelectionModel().select(newTab);

            newTab.setOnCloseRequest(event -> {
                if (ShowMessageFX.YesNo(null, "Close Tab", "Close this tab?")) {
                    tabName.remove(newTab.getText());
                    SIPostingWindowKeyEvent(newTab, fxObj, true);
                    Tabclose();
                } else {
                    event.consume();
                }

            });

            newTab.setOnClosed(event -> {
                if (lbproceed) {
                    SIPostingWindowKeyEvent(newTab, fxObj, true);
                    lbproceed = false;
                }
            });

            newTab.setOnSelectionChanged(event -> {

                ObservableList<Tab> tabs = tabpane.getTabs();
                for (Tab tab : tabs) {
                    if (tab.getText().equals(newTab.getText())) {
                        tabName.remove(newTab.getText());
                        tabName.add(newTab.getText());
                        //applied for specific use//
                        if (newTab.isSelected()) {
                            SIPostingWindowKeyEvent(newTab, fxObj, false);
                        } else {
                            SIPostingWindowKeyEvent(newTab, fxObj, true);
                        }

                        break;
                    }
                }
            });
            SIPostingWindowKeyEvent(newTab, fxObj, false);
            return (TabPane) tabpane;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public class ControllerBinding {

        public final Class<? extends ScreenInterface> controllerClass;

        public ControllerBinding(Class<? extends ScreenInterface> controllerClass) {
            this.controllerClass = controllerClass;
        }

        public ControllerBinding(AbstractMap.SimpleEntry<String, Class<? extends ScreenInterface>> entry) {
            this.controllerClass = entry.getValue();
        }
    }
    
    //Utility for Transaction Attachment F5
    ControllerBinding[] controllerArray = new ControllerBinding[]{
        new ControllerBinding(SIPosting_Controller.class),
        new ControllerBinding(SIPosting_CarController.class),
        new ControllerBinding(SIPosting_AppliancesController.class),
        new ControllerBinding(SIPosting_LPController.class),
        new ControllerBinding(SIPosting_MPController.class),
        new ControllerBinding(SIPosting_MCController.class),
        new ControllerBinding(SIPosting_SPMCController.class),
        new ControllerBinding(SIPosting_SPCarController.class),
        new ControllerBinding(SIPosting_MonarchFoodController.class),
        new ControllerBinding(SIPosting_MonarchHospitalityController.class),
        new ControllerBinding(SIPosting_EntryController.class),
        new ControllerBinding(SIPosting_EntryCarController.class),
        new ControllerBinding(SIPosting_EntryAppliancesController.class),
        new ControllerBinding(SIPosting_EntryLPController.class),
        new ControllerBinding(SIPosting_EntryMPController.class),
        new ControllerBinding(SIPosting_EntryMCController.class),
        new ControllerBinding(SIPosting_EntrySPMCController.class),
        new ControllerBinding(SIPosting_EntrySPCarController.class),
        new ControllerBinding(SIPosting_EntryMonarchFoodController.class),
        new ControllerBinding(SIPosting_EntryMonarchHospitalityController.class),
        new ControllerBinding(SIPosting_ConfirmationController.class),
        new ControllerBinding(SIPosting_ConfirmationCarController.class),
        new ControllerBinding(SIPosting_ConfirmationAppliancesController.class),
        new ControllerBinding(SIPosting_ConfirmationLPController.class),
        new ControllerBinding(SIPosting_ConfirmationMPController.class),
        new ControllerBinding(SIPosting_ConfirmationMCController.class),
        new ControllerBinding(SIPosting_ConfirmationSPMCController.class),
        new ControllerBinding(SIPosting_ConfirmationSPCarController.class),
        new ControllerBinding(SIPosting_ConfirmationMonarchFoodController.class),
        new ControllerBinding(SIPosting_ConfirmationMonarchHospitalityController.class),
        new ControllerBinding(SIPosting_VerificationController.class),
        new ControllerBinding(SIPosting_VerificationCarController.class),
        new ControllerBinding(SIPosting_VerificationAppliancesController.class),
        new ControllerBinding(SIPosting_VerificationLPController.class),
        new ControllerBinding(SIPosting_VerificationMPController.class),
        new ControllerBinding(SIPosting_VerificationMCController.class),
        new ControllerBinding(SIPosting_VerificationSPMCController.class),
        new ControllerBinding(SIPosting_VerificationSPCarController.class),
        new ControllerBinding(SIPosting_VerificationMonarchFoodController.class),
        new ControllerBinding(SIPosting_VerificationMonarchHospitalityController.class),
        new ControllerBinding(SIPosting_HistoryController.class),
        new ControllerBinding(SIPosting_HistoryAppliancesController.class),
        new ControllerBinding(SIPosting_HistoryCarController.class),
        new ControllerBinding(SIPosting_HistoryLPController.class),
        new ControllerBinding(SIPosting_HistoryMPController.class),
        new ControllerBinding(SIPosting_HistoryMCController.class),
        new ControllerBinding(SIPosting_HistorySPMCController.class),
        new ControllerBinding(SIPosting_HistorySPCarController.class),
        new ControllerBinding(SIPosting_HistoryMonarchFoodController.class),
        new ControllerBinding(SIPosting_HistoryMonarchHospitalityController.class),
        new ControllerBinding(POQuotation_EntryController.class),
        new ControllerBinding(POQuotation_ConfirmationController.class),
        new ControllerBinding(POQuotation_ApprovalController.class),
        new ControllerBinding(POQuotation_HistoryController.class),
        new ControllerBinding(CashDisbursement_EntryController.class),
        new ControllerBinding(CashDisbursement_ConfirmationController.class),
        new ControllerBinding(CashDisbursement_VerificationController.class),
        new ControllerBinding(CashDisbursement_ApprovalController.class),
        new ControllerBinding(CashDisbursement_HistoryController.class),
        new ControllerBinding(PettyCashDisbursement_EntryController.class),
        new ControllerBinding(PettyCashDisbursement_ConfirmationController.class),
        new ControllerBinding(PettyCashDisbursement_ApprovalController.class),
        new ControllerBinding(PettyCashDisbursement_HistoryController.class),
        new ControllerBinding(DisbursementVoucher_EntryController.class),
        new ControllerBinding(DisbursementVoucher_ConfirmationController.class),
        new ControllerBinding(DisbursementVoucher_VerificationController.class),
        new ControllerBinding(DisbursementVoucher_ApprovalController.class),
        new ControllerBinding(DisbursementVoucher_HistoryController.class)
    };

    private void SIPostingWindowKeyEvent(Tab newTab, ScreenInterface fxObj, boolean isRemove) {
        Platform.runLater(() -> {
            for (ControllerBinding cb : controllerArray) {
                Object userData = newTab.getUserData();

                if (!(userData instanceof ScreenInterface)) {
                    continue;
                }
                ScreenInterface screen = (ScreenInterface) userData;
                if (!cb.controllerClass.isInstance(screen)) {
                    continue;
                }
                Object casted = cb.controllerClass.cast(fxObj);
                try {
                    String methodName = isRemove ? "RemoveWindowEvent" : "TriggerWindowEvent";

                    Method method = cb.controllerClass.getMethod(methodName);
                    if (isRemove) {
                        initKeyEvent();
                    } else {
                        removewindowEvent();
                    }
                    if (!isRemove) {
                        JFXUtil.runWithDelay(.50, () -> {
                            try {
                                removewindowEvent();
                                method.invoke(casted);
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
                    } else {
                        method.invoke(casted);
                    }

                } catch (NoSuchMethodException e) {
                    initKeyEvent();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break; // stop loop once matched
            }
        });
    }

    public void triggervbox() {
        nav_bar.setDisable(true);
        nav_bar11.setDisable(true);

    }

    public void triggervbox2() {
        setAnchorPaneVisibleManage(false, anchorRightSideBarMenu);
        nav_bar.setDisable(false);
        nav_bar11.setDisable(false);
        setScene(loadAnimateAnchor(psDefaultScreenFXML2));

        toggleGroupLowerBtn = new ToggleGroup();
        toggleBtnLeftLowerSideBar = new ToggleButton[]{
            btnHelp,
            btnLogout
        };

        String[] tooltipTexts = {
            "Help",
            "Logout"
        };

        for (int i = 0; i < toggleBtnLeftLowerSideBar.length; i++) {
            toggleBtnLeftLowerSideBar[i].setTooltip(new Tooltip(tooltipTexts[i]));
            toggleBtnLeftLowerSideBar[i].setToggleGroup(toggleGroupLowerBtn);
        }
        toggleBtnLeftLowerSideBar[0].selectedProperty().removeListener(toggleSelectedListener);
        toggleBtnLeftLowerSideBar[0].selectedProperty().addListener(toggleSelectedListener);
    }

    public void changeUserInfo() {
        try {
            AppUser.setText(oApp.getLogName() + " || " + getIndustryName(psIndustryID));
        } catch (SQLException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void toggleLeftSideBarMenuButton(String buttonId, Integer btnIndex) {

        boolean isNoMenu = false;
        boolean isSameButton = anchorLeftSideBarMenu.isVisible() && lastClickedBtnLeftSideBar.equals(buttonId);

        if (tvLeftSideBar.getRoot() != null) {
            if (!tvLeftSideBar.getRoot().getChildren().isEmpty()) {
                if (!isSameButton) {
                    setAnchorPaneVisibleManage(!isSameButton, anchorLeftSideBarMenu);
                }
                lbIsTriggered = animateMenu(
                        anchorLeftSideBarMenu, // AnchorPane pane
                        tvLeftSideBar, // TreeView<?> tree
                        300, // targetWidth,
                        aprootleft.getHeight(),
                        btnMenu,
                        aprootleft, isSameButton
                );
                animator.expandContract(!lbIsTriggered, () -> {
                    animator.expandContract(!lbIsTriggered, () -> {
                    });
                });
            } else {
                setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
                animator.expandContract(true, () -> {
                });
                ShowMessageFX.Warning(null, "Computerized Accounting System", "No menus available");
                isNoMenu = true;
            }
        } else {
            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            animator.expandContract(true, () -> {
            });
            ShowMessageFX.Warning(null, "Computerized Accounting System", "No menus available");
            isNoMenu = true;
        }

        for (ToggleButton button : toggleBtnLeftUpperSideBar) {
            button.setSelected(false);
        }
        if (!isNoMenu) {
            toggleBtnLeftUpperSideBar[btnIndex].setSelected(!isSameButton);
            lastClickedBtnLeftSideBar = isSameButton ? "" : buttonId;
        }

    }

    private void toggleRightSideBarMenuButton(String buttonId, Integer btnIndex) {
        boolean isNoMenu = false;
        boolean isSameButton = anchorRightSideBarMenu.isVisible() && lastClickedBtnRightSideBar.equals(buttonId);

        if (tvRightSideBar.getRoot() != null) {
            if (!tvRightSideBar.getRoot().getChildren().isEmpty()) {
                if (!isSameButton) {
                    setAnchorPaneVisibleManage(!isSameButton, anchorRightSideBarMenu);
                }
                animateMenu(
                        anchorRightSideBarMenu, // AnchorPane pane
                        tvRightSideBar, // TreeView<?> tree
                        300, // targetWidth,
                        anchorIconMenu11.getHeight(),
                        btnSysMonitor,
                        anchorIconMenu11, isSameButton
                );
            } else {
                setAnchorPaneVisibleManage(false, anchorRightSideBarMenu);
                ShowMessageFX.Warning(null, "Computerized Accounting System", "No notifications available");
                isNoMenu = true;
            }
        } else {
            setAnchorPaneVisibleManage(false, anchorRightSideBarMenu);
            ShowMessageFX.Warning(null, "Computerized Accounting System", "No notifications available");
            isNoMenu = true;
        }

        for (ToggleButton button : toggleBtnRightSideBar) {
            button.setSelected(false);
        }
        if (!isNoMenu) {
            toggleBtnRightSideBar[btnIndex].setSelected(!isSameButton);
            lastClickedBtnRightSideBar = isSameButton ? "" : buttonId;
        }
    }

    private int calculateTreeViewWidth(TreeItem<TreeNode> root) {
        if (root == null) {
            return 200;
        }
        int baseWidth = 200;
        int textPadding = 20;

        int longestTextWidth = getMaxTextWidth(root);

        double parentWidth = anchorLeftSideBarMenu.getParent().getLayoutBounds().getWidth();

        int calculatedWidth = baseWidth + longestTextWidth + textPadding;
        return (int) Math.min(calculatedWidth, parentWidth * 0.9);
    }

    private int calculateTreeMonitorViewWidth(TreeItem<TreeMonitor> root) {
        if (root == null) {
            return 250;
        }
        int baseWidth = 250;
        int textPadding = 10;

        int longestTextWidth = getSysMonitorMaxTextWidth(root);

        double parentWidth = anchorRightSideBarMenu.getParent().getLayoutBounds().getWidth();

        int calculatedWidth = baseWidth + longestTextWidth + textPadding;
        return (int) Math.min(calculatedWidth, parentWidth * 0.9);
    }

    private int getMaxTextWidth(TreeItem<TreeNode> item) {
        if (item == null) {
            return 0;
        }

        int maxWidth = getTextWidth(item.getValue().getName());

        for (TreeItem<TreeNode> child : item.getChildren()) {
            maxWidth = Math.max(maxWidth, getMaxTextWidth(child));
        }

        return maxWidth;
    }

    private int getSysMonitorMaxTextWidth(TreeItem<TreeMonitor> item) {
        if (item == null) {
            return 0;
        }
        if (item.getValue() == null) {
            return 0;
        }
        int maxWidth = getTextWidth(item.getValue().getName() != null ? item.getValue().getName() : "");

        for (TreeItem<TreeMonitor> child : item.getChildren()) {
            maxWidth = Math.max(maxWidth, getSysMonitorMaxTextWidth(child));
        }

        return maxWidth;
    }

    private int getTextWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int charWidth = 7;
        return text.length() * charWidth;
    }
//    private void loadMenu(){
//        MenuManager menu = new MenuManager(oApp, "CAS");
//        menu.setIndustryCode(psIndustryID);
//        
//        try {
//            JSONObject json = menu.getMenu();
//            
//            if ("success".equals((String) json.get("result"))){
//                List<TreeNode> roots = loadMenuFromDatabase(menu.getMenuResult());
//                
//                TreeItem<TreeNode> rootItem = new TreeItem<>(new TreeNode("2500000000",
//                                null,
//                                "Menu", 
//                                "Root menu", 
//                                null, 
//                                null,
//                                null,
//                                null));
//                
//                for (TreeNode node : roots) {
//                    rootItem.getChildren().add(buildTree(node));
//                }
//                rootItem.setExpanded(false);
//                
//                tvLeftSideBar.setRoot(rootItem);
//
//                // Execute on click
//                tvLeftSideBar.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
//                    if (newVal != null) {
//                        newVal.getValue().runAction();
//                    }
//                });
//                
//                tvLeftSideBar.setShowRoot(false);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        
//    }
//    private void loadMenu(){
//        MenuManager menu = new MenuManager(oApp, "CAS");
//        menu.setIndustryCode(psIndustryID);
//        
//        try {
//            JSONObject json = menu.getMenu();
//            
//            if ("success".equals((String) json.get("result"))){
//                List<TreeNode> roots = loadMenuFromDatabase(menu.getMenuResult());
//                
//                TreeItem<TreeNode> rootItem = new TreeItem<>(new TreeNode("2500000000",
//                                null,
//                                "Menu", 
//                                "Root menu", 
//                                null, 
//                                null,
//                                null,
//                                null));
//                
//                for (TreeNode node : roots) {
//                    rootItem.getChildren().add(buildTree(node));
//                }
//                rootItem.setExpanded(false);
//                
//                tvLeftSideBar.setRoot(rootItem);
//
//                // Execute on click
//                tvLeftSideBar.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
//                    if (newVal != null) {
//                        newVal.getValue().runAction();
//                    }
//                });
//                
//                tvLeftSideBar.setShowRoot(false);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        
//    }

//    private TreeItem<TreeNode> buildTree(TreeNode node) {
//        TreeItem<TreeNode> item = new TreeItem<>(node);
//        for (TreeNode child : node.getChildren()) {
//            item.getChildren().add(buildTree(child));
//        }
//        return item;
//    }
    private void loadMenu() {
        MenuManager menu = new MenuManager(oApp, "CAS");
        menu.setIndustryCode(psIndustryID);
        menu.setCategoryCode(oApp.getCategory());

        try {
            JSONObject json = menu.getMenu();

            if ("success".equals((String) json.get("result"))) {
                List<TreeNode> roots = loadMenuFromDatabase(menu.getMenuResult());

                TreeItem<TreeNode> rootItem = new TreeItem<>(new TreeNode("2500000000",
                        null,
                        "Menu",
                        "Root menu",
                        null,
                        null,
                        null,
                        null));

                for (TreeNode node : roots) {
                    if ("2500000075»2500000076»2500000077".contains(node.getId())) {
                        System.err.println(node.getId());
                    }

                    rootItem.getChildren().add(buildTree(node, allowedMenuIds));
                }
                rootItem.getChildren().removeIf(child -> child == null || child.getValue() == null);
                rootItem.setExpanded(false);

                tvLeftSideBar.setRoot(rootItem);

                // Execute on click
                tvLeftSideBar.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        newVal.getValue().runAction();
                    }
                });

                tvLeftSideBar.setShowRoot(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Utility for menu key
    private String buildMenuKey(String menuCode, String industry, String category) {
        return (menuCode + "-"
                + (industry != null ? industry : "") + "-"
                + (category != null ? category : "")).toUpperCase();
    }

    private TreeItem<TreeNode> buildTree(TreeNode node, Set<String> allowedMenuIds) {
        // Node must have a non-empty commandType
        boolean hasValidCommand = node.getCommandType() != null && !node.getCommandType().isEmpty();

        TreeItem<TreeNode> treeItem = new TreeItem<>(node);
        boolean hasValidChild = false;

        // Build children recursively
        for (TreeNode child : node.getChildren()) {
            TreeItem<TreeNode> childItem = buildTree(child, allowedMenuIds);

            if (childItem != null) {
                treeItem.getChildren().add(childItem);
                hasValidChild = true;
            }
        }

        // Rules:
        // 1. If node has a valid command AND is allowed -> show it
        if (hasValidCommand && allowedMenuIds.contains(node.getId())) {
            return treeItem;
        }

        // 2. If node has no command BUT has valid children -> show it as a container
        if (!hasValidCommand && hasValidChild) {
            return treeItem;
        }

        // 3. Otherwise -> hide it
        return null;
    }

    private List<TreeNode> loadMenuFromDatabase(ResultSet menu) throws SQLException {
        Map<String, TreeNode> map = new HashMap<>();
        List<TreeNode> roots = new ArrayList<>();

        while (menu.next()) {
            String id = menu.getString("sMenuCDxx");
            String parentId = menu.getString("sMenuGrpx");
            String name = menu.getString("sMenuName");
            String desc = menu.getString("sMenuDesc");
            String command = menu.getString("sCommandx");
            String commandType = menu.getString("sCmdTypex");
            String formName = menu.getString("sFormName");
            String controller = menu.getString("sObjectNm");
            String industryCd = menu.getString("sIndstCdx");
            String categoryCd = menu.getString("sCategrCd");

            TreeNode node = new TreeNode(id, parentId, name, desc, command, commandType, formName, controller, industryCd, categoryCd);

            if (!command.isEmpty() && !commandType.isEmpty()) {
                node.setAction(createAction(node));

                // Register node in lookup map for later access
                String key = buildMenuKey(node.getId(), node.getIndustry(), node.getCategory());
                leftSidebarLookup.put(key, node);
                if (node.getId().equals("2500000058")) {
                    System.out.println(key);
                }
            }

            map.put(id, node);
            allowedMenuIds.add(id);

            if (parentId.isEmpty()) {
                roots.add(node);
            } else {
                TreeNode parent = map.get(parentId);
                if (parent != null) {
                    parent.addChild(node);
                }
            }
        }

        return roots;
    }

    private void toggleSidebarWidth() {
        if (tvLeftSideBar != null && tvLeftSideBar.getRoot() != null) {
            int calculatedWidth = calculateTreeViewWidth(tvLeftSideBar.getRoot());

            Platform.runLater(() -> {
                anchorLeftSideBarMenu.setPrefWidth(calculatedWidth);
            });
        }

        if (tvRightSideBar != null && tvRightSideBar.getRoot() != null) {
            int calculatedWidth = calculateTreeMonitorViewWidth(tvRightSideBar.getRoot());

            Platform.runLater(() -> {
                anchorRightSideBarMenu.setPrefWidth(calculatedWidth);
            });
        }
    }

    //menu actions
    public Runnable createAction(TreeNode node) {
        switch (node.getCommandType()) {
            case "JAVA":
                return () -> {
                    if (node.getFxmlPath() != null) {
                        openForm(node);
                    } else {
                        runJavaCommand(node.getCommand());
                    }
                };
            case "SQL":
                return () -> runSqlCommand(node.getCommand());
            case "EXEC":
                return () -> runExternalCommand(node.getCommand());
            case "URL":
                return () -> openUrl(node.getCommand());
            default:
                return () -> System.out.println("⚠ Unknown command type: " + node.getCommandType());
        }
    }

    private void registerJavaCommand(String name, Runnable action) {
        javaCommands.put(name, action);
    }

    private void runJavaCommand(String command) {
        // First try registered commands
        Runnable action = javaCommands.get(command);
        if (action != null) {
            action.run();
            return;
        }

        // Fallback: Try reflection (method in this class with same name)
        try {
            Method method = this.getClass().getDeclaredMethod(command);
            method.setAccessible(true);
            method.invoke(this);
            System.out.println(">>> JAVA command executed via reflection: " + command);
        } catch (NoSuchMethodException | NullPointerException e) {
            System.out.println("⚠ Unknown JAVA command: " + command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openForm(TreeNode node) {
        try {
            int tabIndex = checktabs(node.getDescription());

            if (tabIndex == -1) {
                if (!node.getFxmlPath().isEmpty() && node.getFxmlPath().contains(".fxml")) {
                    setScene2(loadAnimate(node));
                    poController = tabpane.getUserData();
                } else {
                    if (Platform.isFxApplicationThread()) {
                        ShowMessageFX.Warning(null, psFormName, "Invalid FXML path detected. Please inform MIS to configure the correct path.");
                    } else {
                        Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, "Invalid FXML path detected. Please inform MIS to configure the correct path."));
                    }

                }
            } else {
                tabpane.getSelectionModel().select(tabIndex);
                poController = tabpane.getUserData();
            }

            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            animator.expandContract(true, () -> {
            });
            for (ToggleButton navButton : toggleBtnLeftUpperSideBar) {
                navButton.setSelected(false);
            }
            pane.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runSqlCommand(String sql) {
        //execute sql
    }

    private void runExternalCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            System.out.println(">>> Executing external command: " + command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openUrl(String url) {
        try {
            System.out.println(">>> Opening URL: " + url);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                System.out.println("⚠ Desktop not supported, cannot open URL");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //end menu actions

// ======================= SYSTEM MONITOR =======================
    private void loadSystemMonitor() {
        try {
            SystemMonitorMenu monitorMenu = new SystemMonitorMenu(oApp, "CAS");
            monitorMenu.setIndustryCode(psIndustryID);
            monitorMenu.setCategoryCode(oApp.getCategory());
            monitorMenu.setCompanyFilter(new String[]{psCompanyID});
            JSONObject loSysMontrData = monitorMenu.processMonitoring();

            if (!"success".equals((String) loSysMontrData.get("result"))) {
                tvRightSideBar.setRoot(null);
                tvRightSideBar.setShowRoot(false);
                return;
            }

            JSONArray loSysMontrRecord = (JSONArray) loSysMontrData.get("data");
            TreeItem<TreeMonitor> loTreeNode = loadSystemMonitoMenu(loSysMontrRecord);
            tvRightSideBar.setRoot(loTreeNode);
            tvRightSideBar.setShowRoot(false);

            lblNotifCount.setText(String.valueOf(monitorMenu.getMonitoringCount()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TreeItem<TreeMonitor> loadSystemMonitoMenu(JSONArray jsonArray) {
        TreeItem<TreeMonitor> root = new TreeItem<>();
//        System.out.println("JSON Array : " + jsonArray.toJSONString());
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;

            String id = (String) json.get("sSysMnuCd");
            String group = (String) json.get("sMenuGrpx");
            String name = (String) json.get("sMenuName");
            String desc = (String) json.get("sDescript");
            String menuCode = (String) json.get("sMenuCDxx");
            String industry = (String) json.get("sIndstCdx");
            String category = (String) json.get("sCategrCd");
            JSONObject loJSON = getFxml(menuCode, industry, category);
            String fxmlPath = (String) loJSON.get("sFormName");
            String controllerClass = (String) loJSON.get("sObjectNm");
            String command = (String) loJSON.get("sCommandx");
            String commandType = (String) loJSON.get("sCmdTypex");

            TreeMonitor monitorNode = new TreeMonitor(
                    id, group, name, desc, menuCode, industry, category, fxmlPath, controllerClass, command, commandType
            );

            TreeItem<TreeMonitor> treeNode = new TreeItem<>(monitorNode);

            // Build submodules
            JSONArray subModules = (JSONArray) json.get("subModule");
            if (subModules != null && !subModules.isEmpty()) {
                TreeItem<TreeMonitor> subTree = loadSystemMonitoMenu(subModules);
                treeNode.getChildren().addAll(subTree.getChildren());
            }

            // Add transactions
            JSONArray transactions = (JSONArray) json.get("transaction");
            if (transactions != null && !transactions.isEmpty()) {
                for (Object t : transactions) {
                    JSONObject tx = (JSONObject) t;
                    String txId = (String) tx.get("sTransNox");
                    String txDisplayName = (String) tx.get("sDisplayNme");
                    String txDescToolTip = (String) tx.get("sToolTipx");
                    String txIndustry = (String) tx.get("sIndstCdx");
                    String txCategory = (String) tx.get("sCategrCd");

                    TreeMonitor txNode = new TreeMonitor(
                            txId, id, txDisplayName != null ? txDisplayName : txId,
                            txDescToolTip != null ? txDescToolTip : "Transaction", menuCode, txIndustry, txCategory != null ? txCategory : "", fxmlPath, controllerClass, command, commandType
                    );

                    if (!txId.isEmpty() && !menuCode.isEmpty()) {
                        txNode.setAction(createSysMonitorAction(txNode));
                    } else if (!txId.isEmpty() && menuCode.isEmpty()) {//still add 
                        txNode.setAction(createSysMonitorAction(txNode));

                    }

                    TreeItem<TreeMonitor> txItem = new TreeItem<>(txNode);
                    treeNode.getChildren().add(txItem);
                }
            }

            root.getChildren().add(treeNode);
        }

        return root;
    }

    public Runnable createSysMonitorAction(TreeMonitor node) {
        return () -> {
            try {

                if (node.getFxmlPath() != null) {
                    openMonitorForm(node);
                } else {
                    runJavaCommand(node.getCommand());
                }

                String menuCode = node.getMenuCode();
                String industry = node.getIndustry();
                String category = node.getCategory();

                if (menuCode.isEmpty()) {
                    return;
                }
                String key = buildMenuKey(menuCode, industry, category);
                //loadmenu if never clicked
                if (leftSidebarLookup.size() <= 0) {
                    loadMenu();
                }
                TreeNode sidebarNode = leftSidebarLookup.get(key);
                if (sidebarNode != null) {
                    System.out.println("Triggering left sidebar for: " + key);
                    sidebarNode.runAction();

                    System.out.println("Transaction no. " + node.getSystemId());
                    if (poController != null) {
                        Method retrieveMethod = poController.getClass().getMethod("retrieveBySystemMonitor", String.class);
                        retrieveMethod.invoke(poController, node.getSystemId());
                        System.out.println("Transaction no. " + node.getSystemId());
                    } else {
                        System.err.println("Controller not found for sidebar: " + key);
                    }
                } else {
                    System.err.println("No sidebar entry found for: " + key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private void openMonitorForm(TreeMonitor node) {
        try {
            int tabIndex = checktabs(node.getDescription());

            if (tabIndex == -1) {
                boolean lbError = false;
                if (node.getFxmlPath() != null && !"".equals(node.getFxmlPath())) {
                    if (node.getFxmlPath().contains(".fxml")) {
                        setScene2(loadAnimate(node));
                        poController = tabpane.getUserData();
                    } else {
                        lbError = true;
                    }
                } else {
                    lbError = true;
                }

                if (lbError) {
                    if (Platform.isFxApplicationThread()) {
                        ShowMessageFX.Warning(null, psFormName, "Invalid FXML path detected. Please inform MIS to configure the correct path.");
                    } else {
                        Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, "Invalid FXML path detected. Please inform MIS to configure the correct path."));
                    }
                }

//                if (!node.getFxmlPath().isEmpty() && node.getFxmlPath().contains(".fxml")) {
//                    setScene2(loadAnimate(node));
//                    poController = tabpane.getUserData();
//                } else {
//                    if (Platform.isFxApplicationThread()) {
//                        ShowMessageFX.Warning(null, psFormName, "Invalid FXML path detected. Please inform MIS to configure the correct path.");
//                    } else {
//                        Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, "Invalid FXML path detected. Please inform MIS to configure the correct path."));
//                    }
//                }
            } else {
                tabpane.getSelectionModel().select(tabIndex);
                poController = tabpane.getSelectionModel().getSelectedItem().getUserData();
                switch (node.getMenuCode()) {
                    case "2500000088": //PRM APM
                    case "2500000039": //PRF PUR
                        loadPRFForm(node, false);
                        break;
                }
            }

            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            for (ToggleButton navButton : toggleBtnLeftUpperSideBar) {
                navButton.setSelected(false);
            }
            pane.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TabPane loadAnimate(TreeMonitor node) {
        //set fxml controller class
        if (tabpane.getTabs().isEmpty()) {
            tabpane = new TabPane();
        }

        setTabPane();
        setPane();

        try {
            switch (node.getMenuCode()) {
                case "2500000088": //PRM APM
                case "2500000039": //PRF PUR
                    loadPRFForm(node, true);
                    break;
                default:
                    Class<?> cls = Class.forName(node.getControllerClass());
                    ScreenInterface fxObj = (ScreenInterface) cls.getDeclaredConstructor().newInstance();

                    fxObj.setGRider(oApp);
                    fxObj.setIndustryID(node.getIndustry());
                    fxObj.setCompanyID(psCompanyID);
                    fxObj.setCategoryID(node.getCategory());

                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(fxObj.getClass().getResource(node.getFxmlPath()));
                    fxmlLoader.setController(fxObj);

                    Tab newTab = new Tab(node.getDescription());
                    newTab.setContent(new javafx.scene.control.Label("Content of Tab " + node.getFxmlPath()));

                    // ✅ Store controller reference in the tab
                    newTab.setUserData(fxObj);

                    newTab.setContextMenu(createContextMenu(tabpane, newTab, oApp));
                    tabName.add(node.getDescription());

                    Node content = fxmlLoader.load();
                    newTab.setContent(content);
                    tabpane.getTabs().add(newTab);
                    tabpane.getSelectionModel().select(newTab);

                    newTab.setOnCloseRequest(event -> {
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Close this tab?")) {
                            tabName.remove(newTab.getText());
                            SIPostingWindowKeyEvent(newTab, fxObj, true);
                            Tabclose();
                        } else {
                            event.consume();
                        }

                    });

                    newTab.setOnClosed(event -> {
                        if (lbproceed) {
                            SIPostingWindowKeyEvent(newTab, fxObj, true);
                            lbproceed = false;
                        }
                    });

                    newTab.setOnSelectionChanged(event -> {

                        ObservableList<Tab> tabs = tabpane.getTabs();
                        for (Tab tab : tabs) {
                            if (tab.getText().equals(newTab.getText())) {
                                tabName.remove(newTab.getText());
                                tabName.add(newTab.getText());
                                //applied for specific use//
                                if (newTab.isSelected()) {
                                    SIPostingWindowKeyEvent(newTab, fxObj, false);
                                } else {
                                    SIPostingWindowKeyEvent(newTab, fxObj, true);
                                }

                                break;
                            }
                        }
                    });
                    SIPostingWindowKeyEvent(newTab, fxObj, false);
                    break;

            }

            return (TabPane) tabpane;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private JSONObject getFxml(String fsMenuCode, String fsIndustry, String fsCategory) {
        JSONObject loJSON = new JSONObject();
        try {
            String lsSQL = "SELECT a.sMenuCDxx"
                    + ", a.sMenuGrpx"
                    + ", a.sMenuName"
                    + ", a.sMenuDesc"
                    + ", b.sCommandx"
                    + ", b.sCmdTypex"
                    + ", b.sFormName"
                    + ", b.sObjectNm"
                    + ", b.sIndstCdx"
                    + ", b.sCategrCd from xxxSysMenu a "
                    + "LEFT JOIN xxxSysMenuOthers b ON b.sMenuCDxx = a.sMenuCDxx AND b.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE);
            lsSQL = MiscUtil.addCondition(lsSQL,
                    " a.sMenuCDxx = " + SQLUtil.toSQL(fsMenuCode)
                    + " AND b.sIndstCdx = " + SQLUtil.toSQL(fsIndustry)
                    + " AND b.sCategrCd = " + SQLUtil.toSQL(fsCategory)
                    + " AND a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
//            System.out.println(" getFxml Executing SQL: " + lsSQL);
            ResultSet loRS = oApp.executeQuery(lsSQL);
            try {
                if (MiscUtil.RecordCount(loRS) > 0) {
                    if (loRS.next()) {
                        loJSON.put("sMenuCDxx", loRS.getString("sMenuCDxx"));
                        loJSON.put("sMenuGrpx", loRS.getString("sMenuGrpx"));
                        loJSON.put("sMenuName", loRS.getString("sMenuName"));
                        loJSON.put("sMenuDesc", loRS.getString("sMenuDesc"));
                        loJSON.put("sCommandx", loRS.getString("sCommandx"));
                        loJSON.put("sCmdTypex", loRS.getString("sCmdTypex"));
                        loJSON.put("sFormName", loRS.getString("sFormName"));
                        loJSON.put("sObjectNm", loRS.getString("sObjectNm"));
                        loJSON.put("sIndstCdx", loRS.getString("sIndstCdx"));
                        loJSON.put("sCategrCd", loRS.getString("sCategrCd"));
                        return loJSON;
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("No record loaded.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        loJSON.put("result", "error");
        return loJSON;
    }

    private JSONObject loadPRFForm(TreeMonitor node, boolean fbNewTab) {
        JSONObject loJSON = new JSONObject();
        try {

            if (!fbNewTab) {
                PaymentRequest_EntryController fxObj = (PaymentRequest_EntryController) poController;
                fxObj.setReloadDetail(node.getSystemId());
                fxObj.ReloadDetail();
                return loJSON;
            }

            //Only load this for new tab
            PaymentRequest_EntryController fxObj = new PaymentRequest_EntryController();
            fxObj.setGRider(oApp);
            fxObj.setIndustryID(psIndustryID); //Mandatory Set to current logged in
            fxObj.setCompanyID(psCompanyID);
            fxObj.setCategoryID(node.getCategory());
            fxObj.setReloadDetail(node.getSystemId());

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(fxObj.getClass().getResource(node.getFxmlPath()));
            fxmlLoader.setController(fxObj);

            Tab newTab = new Tab(node.getDescription());
            newTab.setContent(new javafx.scene.control.Label("Content of Tab " + node.getFxmlPath()));

            // ✅ Store controller reference in the tab
            newTab.setUserData(fxObj);

            newTab.setContextMenu(createContextMenu(tabpane, newTab, oApp));
            tabName.add(node.getDescription());

            Node content = fxmlLoader.load();
            newTab.setContent(content);
            tabpane.getTabs().add(newTab);
            tabpane.getSelectionModel().select(newTab);

            newTab.setOnCloseRequest(event -> {
                if (ShowMessageFX.YesNo(null, "Close Tab", "Close this tab?")) {
                    tabName.remove(newTab.getText());
                    SIPostingWindowKeyEvent(newTab, fxObj, true);
                    Tabclose();
                } else {
                    event.consume();
                }

            });

            newTab.setOnClosed(event -> {
                if (lbproceed) {
                    SIPostingWindowKeyEvent(newTab, fxObj, true);
                    lbproceed = false;
                }
            });

            newTab.setOnSelectionChanged(event -> {

                ObservableList<Tab> tabs = tabpane.getTabs();
                for (Tab tab : tabs) {
                    if (tab.getText().equals(newTab.getText())) {
                        tabName.remove(newTab.getText());
                        tabName.add(newTab.getText());
                        //applied for specific use//
                        if (newTab.isSelected()) {
                            SIPostingWindowKeyEvent(newTab, fxObj, false);
                        } else {
                            SIPostingWindowKeyEvent(newTab, fxObj, true);
                        }
                        break;
                    }
                }
            });
            SIPostingWindowKeyEvent(newTab, fxObj, false);
            loJSON.put("result", "error");
            return loJSON;
        } catch (IOException | SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            loJSON.put("result", "error");
            loJSON.put("message", MiscUtil.getException(ex));
        }
        return loJSON;
    }
}
