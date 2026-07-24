package ph.com.guanzongroup.integsys.utility;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.formula.functions.T;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import static ph.com.guanzongroup.integsys.GUI.oApp;
import ph.com.guanzongroup.integsys.views.ScreenInterface;

/**
 * Date : 4/28/2025 Recent update: 07/24/2026
 *
 * @author Aldrich
 */
public class JFXUtil {

    /* To auto resize one column when v scrolllbar appear */
 /* Required to set the min-width property of one column into USE_COMPUTED_SIZE to work*/
    public static void adjustColumnForScrollbar(TableView<?>... tableViews) {
        for (TableView<?> tableView : tableViews) {
            tableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                if (!(newSkin instanceof TableViewSkin<?>)) {
                    return;
                }

                TableViewSkin<?> skin = (TableViewSkin<?>) newSkin;
                VirtualFlow<?> flow = skin.getChildren().stream()
                        .filter(node -> node instanceof VirtualFlow<?>)
                        .map(node -> (VirtualFlow<?>) node)
                        .findFirst().orElse(null);

                if (flow == null) {
                    return;
                }

                ScrollBar vScrollBar = flow.getChildrenUnmodifiable().stream()
                        .filter(node -> node instanceof ScrollBar && ((ScrollBar) node).getOrientation() == Orientation.VERTICAL)
                        .map(node -> (ScrollBar) node)
                        .findFirst().orElse(null);

                if (vScrollBar == null || tableView.getColumns().isEmpty()) {
                    return;
                }

                TableColumn<?, ?> foundColumn = null;
                for (TableColumn<?, ?> column : tableView.getColumns()) {
                    double minWidth = column.getMinWidth();

                    // Safely compare with USE_COMPUTED_SIZE
                    if ((minWidth) == 0) {
                        foundColumn = column;
                        break;
                    }
                }

                if (foundColumn == null) {
//                    System.err.println("NO COLUMN WITH minWidth == 0 (USE_COMPUTED_SIZE) found in table: " + tableView.getId());
                    return;
                }

                final TableColumn<?, ?> targetColumn = foundColumn;
                // Optional debug log
                vScrollBar.visibleProperty().addListener((observable, oldValue, newValue) -> {
                    Platform.runLater(() -> {
                        double scrollBarWidth = newValue ? vScrollBar.getWidth() : 0;
                        double remainingWidth = tableView.getWidth() - scrollBarWidth;

                        double totalFixedWidth = tableView.getColumns().stream()
                                .filter(col -> col != targetColumn)
                                .mapToDouble(TableColumn::getWidth)
                                .sum();

                        double newWidth = Math.max(0, remainingWidth - totalFixedWidth);
                        targetColumn.setPrefWidth(newWidth - 5);
                    });
                });
            });
        }
    }

    public static boolean checkHighlightIfExists(String key, String color, Map<String, List<String>> highlightMap) {
        List<String> colors = highlightMap.computeIfAbsent(key, k -> new ArrayList<>());
        return colors.contains(color);
    }

    /* Dynamic Highlighter for TableView */
 /* Requires TableView, string key (basis value), String Color in hex(e.g. #H1H1H1), Map list for local storage of highlights */
    public static <T> void highlightByKey(TableView<T> table, String key, String color, Map<String, List<String>> highlightMap) {
        List<String> colors = highlightMap.computeIfAbsent(key, k -> new ArrayList<>());
        if (!colors.contains(color)) {
            colors.add(color);
            table.refresh();
        }
    }

    /* To disable particular highlight*/
 /* Requires TableView, string key (basis value), Map list for local storage of highlights */
    public static <T> void disableHighlightByKey(TableView<T> table, String key, Map<String, List<String>> highlightMap) {
        highlightMap.remove(key);
        table.refresh();
    }

    /* To conveniently disable all highlight*/
    public static <T> void disableAllHighlight(TableView<T> table, Map<String, List<String>> highlightMap) {
        highlightMap.clear();
        table.refresh();
    }

    /* To disable highlight by defining color in hex*/
 /* Requires TableView, , String Color in hex(e.g. #H1H1H1), Map list for local storage of highlights */
    public static <T> void disableAllHighlightByColor(TableView<T> table, String color, Map<String, List<String>> highlightMap) {
        highlightMap.forEach((key, colors) -> colors.removeIf(c -> c.equals(color)));
        highlightMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        table.refresh();
    }

    /* To make highlighting effective, apply in initialization; call only once*/
    public static <T> void applyRowHighlighting(
            final TableView<T> tableView,
            final Function<T, String> keyExtractor,
            final Map<String, List<String>> highlightMap) {

        tableView.setRowFactory(new javafx.util.Callback<TableView<T>, TableRow<T>>() {
            @Override
            public TableRow<T> call(final TableView<T> tv) {
                return new TableRow<T>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setStyle(""); // Reset style
                        } else {
                            String key = keyExtractor.apply(item);
                            if (highlightMap.containsKey(key)) {
                                List<String> colors = highlightMap.get(key);
                                if (!colors.isEmpty()) {
                                    setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";");
                                }
                            } else {
                                setStyle(""); // Default
                            }
                        }
                    }
                };
            }
        });
    }

    /*this is specifically used for ENTRY form*/
 /* To retain non-temporary highlights and remove temporary highlights; */
    public static void showRetainedHighlight(boolean isRetained, TableView<?> tblView, String color, List<Pair<String, String>> plPartial, List<Pair<String, String>> plFinal,
            Map<String, List<String>> highlightedRows, boolean resetpartial) {

        //decide if to allow adding to final of rows highlighted 
        //if contains 1 value, indicates will proceed
        if (isRetained) {
            plFinal.removeIf(finalPair
                    -> plPartial.stream()
                            .anyMatch(partialPair -> partialPair.getKey().equals(finalPair.getKey()))
            );
            for (Pair<String, String> pair : plPartial) {
                if (!"0".equals(pair.getValue())) {
                    plFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
                }
            }
        }

        //decide if to reset the temporary highlights made
        if (resetpartial) {
            disableAllHighlightByColor(tblView, color, highlightedRows);
            plPartial.clear();
        }
        //highlighting 
        for (Pair<String, String> pair : plFinal) {
            if (!"0".equals(pair.getValue())) {
                highlightByKey(tblView, pair.getKey(), color, highlightedRows);
            }
        }
    }

    /* Experimental function unused*/
    public static void setDatePickerNextFocusByEnter(DatePicker... datePickers) {
        for (DatePicker datePicker : datePickers) {
            datePicker.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    Node source = (Node) event.getSource();
                    source.fireEvent(new KeyEvent(
                            KeyEvent.KEY_PRESSED,
                            "",
                            "",
                            KeyCode.TAB,
                            false,
                            false,
                            false,
                            false
                    ));
                    event.consume();
                }
            });
        }
    }

    /* SUGGESTED To modify combobox selection lists color*/
    public static void initComboBoxCellDesignColor(String hexColor, ComboBox<?>... comboBoxes) {
        for (ComboBox<?> comboBox : comboBoxes) {
            initComboBoxCellDesignColor(comboBox, hexColor);
        }
    }

    /* To modify combobox lists color, includes hover and selected*/
    public static <T> void initComboBoxCellDesignColor(ComboBox<T> comboBox, String hexcolor) {
        PseudoClass selected = PseudoClass.getPseudoClass("selected");
        PseudoClass focused = PseudoClass.getPseudoClass("focused");

        comboBox.setCellFactory(param -> new ListCell<T>() {
            {
                // Handle hover color logic
                hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                    if (!isEmpty() && getItem() != null) {
                        if (isNowHovered) {
                            if (isSelected()) {
                                // hovering current selected item
                                setStyle("-fx-background-color: " + hexcolor + "; -fx-text-fill: black;");
                            } else {
                                // hovering other items
                                setStyle("-fx-background-color: " + hexcolor + "; -fx-text-fill: white;");
                            }
                        } else {
                            updateColor();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item.toString());
                updateColor();

                // respond to selection/focus changes
                selectedProperty().addListener((obs, oldSel, newSel) -> updateColor());
                focusedProperty().addListener((obs, oldFoc, newFoc) -> updateColor());
            }

            private void updateColor() {
                boolean isSel = isSelected();
                boolean isFoc = isFocused();

                // For keyboard navigation or selection, always black text
                if (isSel || isFoc) {
                    setStyle("-fx-background-color: " + hexcolor + "; -fx-text-fill: black;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
                }
            }
        });

        // Displayed value in the ComboBox (should look normal)
        comboBox.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
                }
            }
        });
    }

    /* To format datepicker to multiple nodes; old usage*/
    public static void setDatePickerFormat(DatePicker... datePickers) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (DatePicker datePicker : datePickers) {
            datePicker.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(formatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
                }
            });
        }
    }

    /* To format a datepicker; new usage*/
    public static void setDatePickerFormat(String pattern, DatePicker... datePickers) {
//        "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        for (DatePicker datePicker : datePickers) {
            datePicker.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(formatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
                }
            });
        }
    }

    /* To put caret position of a textfield to last character index*/
 /* Requires AnchorPane ID containing textfields*/
    public static void updateCaretPositions(AnchorPane... anchorPanes) {
        List<TextField> textFields = new ArrayList<>();

        // Collect all TextFields from all provided AnchorPanes
        for (AnchorPane anchorPane : anchorPanes) {
            textFields.addAll(getAllTextFields(anchorPane));
        }

        for (TextField textField : textFields) {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                Pos alignment = textField.getAlignment();
                if (alignment == Pos.CENTER_RIGHT || alignment == Pos.BASELINE_RIGHT
                        || alignment == Pos.TOP_RIGHT || alignment == Pos.BOTTOM_RIGHT) {
                    textField.positionCaret(0); // Caret at start
                } else {
                    if (textField.isFocused()) {
                        textField.positionCaret(text.length()); // Caret at end if focused
                    } else {
                        textField.positionCaret(0); // Caret at start if not focused
                    }
                }
            }
        }
    }

    // private
    private static List<TextField> getAllTextFields(Parent parent) {
        List<TextField> textFields = new ArrayList<>();

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                textFields.add((TextField) node);
            } else if (node instanceof DatePicker) {
                Node datePickerEditor = ((DatePicker) node).lookup(".text-field");
                if (datePickerEditor instanceof TextField) {
                    textFields.add((TextField) datePickerEditor);
                }
            } else if (node instanceof Parent) {
                textFields.addAll(getAllTextFields((Parent) node));
            }
        }
        return textFields;
    }

    /*Move the current row of TableView to next row and returns next index value*/
 /*Requires TableView*/
    public static int moveToNextRow(TableView table) {
        TableView<?> currentTable = table;
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
        if (focusedCell != null) {
            int nextRow = (focusedCell.getRow() + 1) % table.getItems().size();
            table.getSelectionModel().select(nextRow);
            return nextRow;
        } else {
            return 0;
        }
    }

    /*Move the current row of TableView to previous row and returns next index value*/
 /*Requires TableView*/
    public static int moveToPreviousRow(TableView table) {
        TableView<?> currentTable = table;
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
        if (focusedCell != null) {
            int previousRow = (focusedCell.getRow() - 1 + table.getItems().size()) % table.getItems().size();
            table.getSelectionModel().select(previousRow);
            return previousRow;
        } else {
            return 0;
        }

    }

    /* Use when pagination is present in tableView */
 /*Updates the pagination count dynamically*/
    public static void loadTab(Pagination pgPagination, int tbldata_list_size, int ROWS_PER_PAGE, TableView tbl, FilteredList filteredData) {
        int totalPage = (int) (Math.ceil(tbldata_list_size * 1.0 / ROWS_PER_PAGE));
        pgPagination.setPageCount(totalPage);
        pgPagination.setCurrentPageIndex(0);
        changeTableView(0, ROWS_PER_PAGE, tbl, tbldata_list_size, filteredData);
        pgPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            changeTableView(newValue.intValue(), ROWS_PER_PAGE, tbl, tbldata_list_size, filteredData);
            tbl.scrollTo(0);
        });
    }

    /* To calculate number of page of the TableView based on table data size*/
    public static void changeTableView(int index, int limit, TableView tbl, int tbldata_list_size, FilteredList filteredData) {
        tbl.getSelectionModel().clearSelection();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, tbldata_list_size);
        int minIndex = Math.min(toIndex, tbldata_list_size);
        try {
            SortedList<T> sortedData = new SortedList<>(
                    FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
            sortedData.comparatorProperty().bind(tbl.comparatorProperty());
        } catch (Exception e) {
        }
        try {
            tbl.setItems(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));
        } catch (Exception e) {

        }
    }

    /* used to display pop-up dialog form*/
 /*NOTE: setOnHidden must be called first before showDialog, to be initialized*/
    public static class StageManager {

        private Stage dialog;
        private EventHandler<WindowEvent> onHiddenHandler; // Store handler
        private final xyOffset xyOffset = new xyOffset();
        Scene scene = null;
        Parent root = null;

        // call to show the dialog
        public void showDialog(Stage parentStage, URL fxmlurl,
                Object controller,
                String lsDialogTitle,
                boolean enableWindowDrag,
                boolean enableblock,
                boolean stayOnTop
        ) throws IOException {

            FXMLLoader loader = new FXMLLoader(fxmlurl);
            loader.setController(controller);

            root = loader.load();

            root.setOnMousePressed(event -> {
                xyOffset.x = event.getSceneX();
                xyOffset.y = event.getSceneY();
            });

            if (enableWindowDrag) {
                root.setOnMouseDragged(event -> {
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setX(event.getScreenX() - xyOffset.x);
                    stage.setY(event.getScreenY() - xyOffset.y);
                });
            }

            dialog = new Stage();
            dialog.initStyle(StageStyle.UNDECORATED);
            if (enableblock) {
                dialog.initModality(Modality.WINDOW_MODAL);
                if (parentStage != null) {
                    dialog.initOwner(parentStage); // sets the blocking owner
                }
            }

            if (stayOnTop) {
                dialog.setAlwaysOnTop(true);
            }

            scene = new Scene(root);

            dialog.setTitle(lsDialogTitle);
            dialog.setScene(scene);

            // Attach stored onHiddenHandler if available
            if (onHiddenHandler != null) {
                dialog.setOnHidden(onHiddenHandler);
                onHiddenHandler = null; // Clear after assigning
            }

            dialog.show();
            dialog.toFront();
        }

        //allows set own functionality when the dialog is closed o hid
        public void setOnHidden(EventHandler<WindowEvent> handler) {
            onHiddenHandler = handler;
        }

        public Scene getScene() {
            return scene;
        }

        public Parent getRoot() {
            return root;
        }

        // call to close the dialog
        public void closeDialog() {
            if (dialog != null) {
                dialog.close();
                dialog = null;
            }
        }
    }

    private static class xyOffset {

        double x, y;
    }

    /* Used for displaying attachment */
 /* Enabling crop in bounds*/
    public static void stackPaneClip(StackPane stackPane1) {
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(
                stackPane1.getWidth() - 8,
                stackPane1.getHeight() - 8
        );
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        clip.setLayoutX(4);
        clip.setLayoutY(4);
        stackPane1.setClip(clip);
    }

    public static void PDFViewConfig(String filePath2, StackPane stackPane1, Button btnArrowLeft, Button btnArrowRight, Double ldstackPaneWidth, Double ldstackPaneHeight) {
        try {
            PDDocument document = PDDocument.load(new File(filePath2));
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            // Container for PDF pages
            VBox pdfContainer = new VBox(10);
            pdfContainer.setAlignment(Pos.CENTER); // center pages
            pdfContainer.setPrefWidth(ldstackPaneWidth);

            for (int i = 0; i < pageCount; i++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(i, 150);
                Image fxImage = SwingFXUtils.toFXImage(pageImage, null);
                ImageView pageView = new ImageView(fxImage);

                pageView.setPreserveRatio(true);
                pageView.setFitWidth(ldstackPaneWidth);
                JFXUtil.adjustImageSize(fxImage, pageView, ldstackPaneWidth, ldstackPaneHeight);

                pdfContainer.getChildren().add(pageView);
            }

            // Wrap VBox in a Group for scaling
            Group pdfGroup = new Group(pdfContainer);

            // Wrap Group in a StackPane to center content
            StackPane centerPane = new StackPane(pdfGroup);
            centerPane.setAlignment(Pos.CENTER);

            // ScrollPane wraps the centerPane
            ScrollPane scrollPane = new ScrollPane(centerPane);
            scrollPane.setPannable(true);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            // Stack PDF and buttons
            stackPane1.getChildren().setAll(scrollPane, btnArrowLeft, btnArrowRight);
            StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
            StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);
            StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
            StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));

            PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
            delay.setOnFinished(event -> {
                Platform.runLater(() -> {
                    JFXUtil.stackPaneClip(stackPane1);
                });
            });
            delay.play();
            document.close();

            // ----- ZOOM & PAN -----
            final DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);
            scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.isControlDown()) {
                    event.consume(); // stop default scroll behavior

                    double delta = event.getDeltaY() > 0 ? 1.1 : 0.9; // multiplier for smooth zoom in/out
                    double oldZoom = zoomFactor.get();
                    zoomFactor.set(oldZoom * delta); // scale by multiplier

                    // Apply scale
                    pdfGroup.setScaleX(zoomFactor.get());
                    pdfGroup.setScaleY(zoomFactor.get());

                    // Keep mouse position centered during zoom
                    Bounds viewportBounds = scrollPane.getViewportBounds();
                    Bounds contentBounds = pdfGroup.getBoundsInParent();
                    double mouseX = event.getX();
                    double mouseY = event.getY();

                    double hRatio = (scrollPane.getHvalue() * (contentBounds.getWidth() - viewportBounds.getWidth()) + mouseX) / contentBounds.getWidth();
                    double vRatio = (scrollPane.getVvalue() * (contentBounds.getHeight() - viewportBounds.getHeight()) + mouseY) / contentBounds.getHeight();

                    Platform.runLater(() -> {
                        Bounds newBounds = pdfGroup.getBoundsInParent();
                        double newH = (hRatio * newBounds.getWidth() - mouseX) / (newBounds.getWidth() - viewportBounds.getWidth());
                        double newV = (vRatio * newBounds.getHeight() - mouseY) / (newBounds.getHeight() - viewportBounds.getHeight());

                        scrollPane.setHvalue(Double.isNaN(newH) ? 0.5 : Math.min(Math.max(0, newH), 1.0));
                        scrollPane.setVvalue(Double.isNaN(newV) ? 0.5 : Math.min(Math.max(0, newV), 1.0));
                    });
                }
            });

            // Pan with mouse drag
            final ObjectProperty<Point2D> lastMouse = new SimpleObjectProperty<>();
            pdfGroup.setOnMousePressed(e -> lastMouse.set(new Point2D(e.getSceneX(), e.getSceneY())));

            pdfGroup.setOnMouseDragged(e -> {
                if (lastMouse.get() != null) {
                    double deltaX = e.getSceneX() - lastMouse.get().getX();
                    double deltaY = e.getSceneY() - lastMouse.get().getY();

                    pdfGroup.setTranslateX(pdfGroup.getTranslateX() + deltaX);
                    pdfGroup.setTranslateY(pdfGroup.getTranslateY() + deltaY);

                    lastMouse.set(new Point2D(e.getSceneX(), e.getSceneY()));
                }
            });
            pdfGroup.setOnMouseReleased(e -> lastMouse.set(null));
        } catch (IOException ex) {
            Logger.getLogger(JFXUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* Used for displaying attachment */
 /* Detects if image is out of bounds in view*/
    public static boolean isImageViewOutOfBounds(ImageView imageView, StackPane stackPane) {
        Bounds clipBounds = stackPane.getClip().getBoundsInParent();
        Bounds imageBounds = imageView.getBoundsInParent();

        return imageBounds.getMaxX() < clipBounds.getMinX()
                || imageBounds.getMinX() > clipBounds.getMaxX()
                || imageBounds.getMaxY() < clipBounds.getMinY()
                || imageBounds.getMinY() > clipBounds.getMaxY();
    }

    /* Used for displaying attachment */
 /* Revert the position and size of the image to default*/
    public static void resetImageBounds(ImageView imageView, StackPane stackPane1) {
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        stackPane1.setAlignment(imageView, javafx.geometry.Pos.CENTER);
    }

    /* Called to display Image by calling its class ImageViewer*/
 /* Allows multiple usage of ImageViewer*/
    public static class ImageViewer {

        public double ldstackPaneWidth = 0;
        public double ldstackPaneHeight = 0;
        public double mouseAnchorX;
        public double mouseAnchorY;
        public double scaleFactor = 1.0;

        public void initAttachmentPreviewPane(StackPane stackPane, ImageView imageView) {
            stackPane.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
                stackPane.setClip(new javafx.scene.shape.Rectangle(
                        newBounds.getMinX(),
                        newBounds.getMinY(),
                        newBounds.getWidth(),
                        newBounds.getHeight()
                ));
            });

            imageView.setOnScroll((ScrollEvent event) -> {
                double delta = event.getDeltaY();
                scaleFactor = Math.max(0.5, Math.min(scaleFactor * (delta > 0 ? 1.1 : 0.9), 5.0));
                imageView.setScaleX(scaleFactor);
                imageView.setScaleY(scaleFactor);
            });

            imageView.setOnMousePressed((MouseEvent event) -> {
                mouseAnchorX = event.getSceneX() - imageView.getTranslateX();
                mouseAnchorY = event.getSceneY() - imageView.getTranslateY();
            });

            imageView.setOnMouseDragged((MouseEvent event) -> {
                double translateX = event.getSceneX() - mouseAnchorX;
                double translateY = event.getSceneY() - mouseAnchorY;
                imageView.setTranslateX(translateX);
                imageView.setTranslateY(translateY);
            });

            stackPane.widthProperty().addListener((observable, oldValue, newWidth) -> {
                ldstackPaneWidth = newWidth.doubleValue();
            });
        }
    }

    /* Used to adjsut image size */
    public static void adjustImageSize(Image image, ImageView imageView, double ldstackPaneWidth, double ldstackPaneHeight) {
        double imageRatio = image.getWidth() / image.getHeight();
        double containerRatio = ldstackPaneWidth / ldstackPaneHeight;

        // Unbind before setting new values
        imageView.fitWidthProperty().unbind();
        imageView.fitHeightProperty().unbind();

        if (imageRatio > containerRatio) {
            // Image is wider than container → fit width
            imageView.setFitWidth(ldstackPaneWidth);
            imageView.setFitHeight(ldstackPaneWidth / imageRatio);
        } else {
            // Image is taller than container → fit height
            imageView.setFitHeight(ldstackPaneHeight);
            imageView.setFitWidth(ldstackPaneHeight * imageRatio);
        }

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
    }

    /* Used for table column value alignment to CENTER */
    public static void setColumnCenter(TableColumn... columns) {
        for (TableColumn column : columns) {
            column.setStyle("-fx-alignment: CENTER;");
        }
    }

    /* Used for table column value alignment to CENTER-LEFT */
    public static void setColumnLeft(TableColumn... columns) {
        for (TableColumn column : columns) {
            column.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        }
    }

    /* Used for table column value alignment to CENTER-RIGHT */
    public static void setColumnRight(TableColumn... columns) {
        for (TableColumn column : columns) {
            column.setStyle("-fx-alignment: CENTER-RIGHT;-fx-padding: 0 5 0 5;");
        }
    }

    /* Auto link & set table values & auto disable column re-alignment */
 /*Requires TableView*/
    public static void setColumnsIndexAndDisableReordering(final TableView<?> tableView) {
        int counter = 1;
        for (Object obj : tableView.getColumns()) {
            if (obj instanceof TableColumn) {
                @SuppressWarnings("unchecked")
                final TableColumn<Object, Object> column = (TableColumn<Object, Object>) obj;

                final String indexName = String.format("index%02d", counter++);
                column.setCellValueFactory(new PropertyValueFactory<>(indexName));

                // disable sorting and editing
                column.setSortable(false);
                column.setEditable(false);

                // Directly set cell factory without Label
                column.setCellFactory(col -> {
                    TableCell<Object, Object> cell = new TableCell<Object, Object>() {
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                String text = item.toString().replaceAll("\\r?\\n", "");
                                setText(text);
                            }
                        }
                    };
                    cell.setWrapText(false);
                    cell.setTextOverrun(OverrunStyle.ELLIPSIS);
                    return cell;
                });
            }
        }

        // disable column reordering
        tableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");
            if (header != null) {
                header.reorderingProperty().addListener((o, oldVal, newVal) -> header.setReordering(false));
            }
        });
    }

    /* Clears textFields, textAreas, checkboxes, combobxes, & datepickers by calling its parent Anchorpane */
 /* For datepicker it auto set value to null before clearing its text input; newly added safe operation*/
    public static void clearTextFields(AnchorPane... anchorPanes) {
        for (AnchorPane pane : anchorPanes) {
            clearTextInputsRecursive(pane);
        }
    }

    //private
    private static void clearTextInputsRecursive(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextInputControl) {
                ((TextInputControl) node).clear();
            } else if (node instanceof DatePicker) {
                DatePicker dp = (DatePicker) node;
                dp.setValue(null);
                if (dp.getEditor() != null) {
                    dp.getEditor().clear();
                }
            } else if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false); // uncheck
            } else if (node instanceof ComboBox) {
                ComboBox<?> combo = (ComboBox<?>) node;

                EventHandler<ActionEvent> savedHandler = combo.getOnAction(); // save

                combo.setOnAction(null);
                combo.getSelectionModel().select(null);
                combo.setOnAction(savedHandler);
            } else if (node instanceof Parent) {
                clearTextInputsRecursive((Parent) node);
            }
        }
    }

    /* Used to hide/unhide button*/
    public static void setButtonsVisibility(boolean visible, Button... buttons) {
        for (Button btn : buttons) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    /* Old usage(for particular use); used to add CSS class into textfield*/
    public static void AddStyleClass(String lsCssClassName, TextField... textFields) {
        for (TextField tf : textFields) {
            tf.getStyleClass().add(lsCssClassName);
        }
    }

    /* Old usage(for particular use); used to remove CSS class from textfield*/
    public static void RemoveStyleClass(String lsCssClassName, TextField... textFields) {
        for (TextField tf : textFields) {
            tf.getStyleClass().remove(lsCssClassName);
        }
    }

    /*Returns boolean*/
 /*Determines if textfield/s contains particular CSS class name*/
    public static boolean isTextFieldContainsStyleClass(String lsCssClassName, TextField... textFields) {
        //used for removal
        for (TextField tf : textFields) {
            if (tf.getStyleClass().contains(lsCssClassName)) {
                return true;
            }
        }
        return false;
    }

    /* Disables any node in UI*/
 /* Requires boolean & Nodes*/
    public static void setDisabled(boolean disable, Object... elements) {
        for (Object obj : elements) {

            if (obj instanceof DatePicker) {
                DatePicker dp = (DatePicker) obj;

                // Disable picker button but keep editor accessible
                dp.setDisable(disable);

                TextField editor = dp.getEditor();
                editor.setEditable(!disable);
                editor.setDisable(false); // VERY IMPORTANT
            } else if (obj instanceof TextArea) {
                TextArea ta = (TextArea) obj;
                ta.setDisable(disable);
                ta.setEditable(!disable);
                ta.setMouseTransparent(false);
                ta.setCursor(!disable ? Cursor.TEXT : Cursor.DEFAULT);
            } else if (obj instanceof TextField) {
                TextField tf = (TextField) obj;
                tf.setDisable(disable);
                tf.setEditable(!disable);

                if (disable) {
                    if (!tf.getStyleClass().contains("DisabledTextField")) {
                        tf.getStyleClass().add("DisabledTextField");
                    }
                } else {
                    tf.getStyleClass().remove("DisabledTextField");
                }

            } else if (obj instanceof Node) {
                ((Node) obj).setDisable(disable);

            } else if (obj instanceof Tab) {
                ((Tab) obj).setDisable(disable);

            } else if (obj instanceof MenuItem) {
                ((MenuItem) obj).setDisable(disable);

            } else if (obj instanceof ButtonBase) {
                ((ButtonBase) obj).setDisable(disable);

            } else if (obj instanceof Control) {
                ((Control) obj).setDisable(disable);

            } else {
                System.out.println("Unsupported element type: " + obj.getClass().getSimpleName());
            }
        }
    }

    /* Disables all nodes in a parent AnchorPane with exception*/
 /* Requires boolean & Nodes*/
    public static void setDisabledExcept(boolean disable, Object container, Object... exceptions) {
        if (!(container instanceof Parent)) {
            return;
        }

        Set<javafx.scene.Node> exceptionSet = new java.util.HashSet<>();
        for (Object obj : exceptions) {
            if (obj instanceof javafx.scene.Node) {
                exceptionSet.add((javafx.scene.Node) obj);
            }
        }

        disableRecursively((Parent) container, disable, exceptionSet);
    }

    private static void disableRecursively(
            Parent parent,
            boolean disable,
            java.util.Set<javafx.scene.Node> exceptions
    ) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {

            // EXCEPTION → fully enabled, do NOT touch its children
            if (exceptions.contains(node)) {
                node.setDisable(false);
                continue; // ← critical
            }

            // Do NOT disable GridPane itself
            if (!(node instanceof javafx.scene.layout.GridPane)) {
                node.setDisable(disable);
            }

            // Traverse children
            if (node instanceof Parent) {
                disableRecursively((Parent) node, disable, exceptions);
            }
        }
    }

    /*Sets a listener to any node*/
    public static void setFocusListener(ChangeListener<? super Boolean> listener, Node... nodes) {
        for (Node node : nodes) {
            processNode(node, listener);
        }
    }

    private static void processNode(Node node, ChangeListener<? super Boolean> listener) {

        if (node instanceof TextField) {
            TextField tf = (TextField) node;
            tf.focusedProperty().addListener(listener);
        } else if (node instanceof AnchorPane) {
            for (Node child : ((AnchorPane) node).getChildren()) {
                processNodeRecursive(child, listener);
            }
        } else {
            if (node instanceof Control) {
                ((Control) node).focusedProperty().addListener(listener);
            }
        }
    }

    private static void processNodeRecursive(Node node, ChangeListener<? super Boolean> listener) {

        if (node instanceof TextField) {
            TextField tf = (TextField) node;

            if (!tf.isDisabled() && tf.isEditable()) {
                tf.focusedProperty().addListener(listener);
            }

        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                processNodeRecursive(child, listener);
            }
        }
    }

    /*Converts MM/dd/yyyy into yyyy-MM-dd*/
    public static String convertToIsoFormat(String dateStr) {
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter usFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        // Try to parse as ISO format first
        try {
            LocalDate date = LocalDate.parse(dateStr, isoFormatter);
            // If it parses successfully, return as-is
            return dateStr;
        } catch (DateTimeParseException ignore) {
            // Not in ISO format, try MM/dd/yyyy
        }

        // Try to parse as MM/dd/yyyy and convert
        try {
            LocalDate date = LocalDate.parse(dateStr, usFormatter);
            return date.format(isoFormatter);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + dateStr);
            return null;
        }
    }

    /*Deprecated*/
    public static JFXUtilDateResult processDate(String inputText, DatePicker datePicker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy"); // accepted string
        JSONObject poJSON = new JSONObject();
        LocalDate selectedDate = null;

        if (inputText != null && !inputText.trim().isEmpty()) {
            try {
                LocalDate parsedDate = LocalDate.parse(inputText, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                datePicker.setValue(parsedDate);
                datePicker.getEditor().setText(formatter.format(parsedDate));
                inputText = datePicker.getEditor().getText();
            } catch (DateTimeParseException ignored) {
            }
        }

        if (inputText != null && !inputText.trim().isEmpty()) {
            try {
                selectedDate = LocalDate.parse(inputText, formatter);
                datePicker.setValue(selectedDate);
            } catch (Exception ex) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid date format. Please use MM/dd/yyyy format.");
                return new JFXUtilDateResult("", selectedDate, poJSON);
            }
        } else {
            selectedDate = datePicker.getValue();
        }
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return new JFXUtilDateResult(inputText, selectedDate, poJSON);
    }

    /*Deprecated*/
    public static class JFXUtilDateResult {

        public String inputText;
        public LocalDate selectedDate;
        public JSONObject poJSON;

        public JFXUtilDateResult(String inputText, LocalDate selectedDate, JSONObject poJSON) {
            this.inputText = inputText;
            this.selectedDate = selectedDate;
            this.poJSON = poJSON;
        }
    }

    /*Sets a keypress listener to textfields in any anchorPane*/
    public static void setKeyPressedListener(EventHandler<KeyEvent> listener, AnchorPane... anchorPanes) {
        for (AnchorPane pane : anchorPanes) {
            for (Node node : pane.getChildrenUnmodifiable()) {
                if (node instanceof TextField) {
                    ((TextField) node).setOnKeyPressed(listener);
                } else if (node instanceof Parent) {
                    // Recursively check for nested TextFields
                    applyListenerToNestedTextFields((Parent) node, listener);
                }
            }
        }
    }

    //private
    private static void applyListenerToNestedTextFields(Parent parent, EventHandler<KeyEvent> listener) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof TextField) {
                ((TextField) child).setOnKeyPressed(listener);
            } else if (child instanceof Parent) {
                applyListenerToNestedTextFields((Parent) child, listener);
            }
        }
    }

    public static List<String> getTextFieldsIDWithPrompt(String lsPromptMsg, AnchorPane... panes) {
        List<String> results = new ArrayList<>();
        for (AnchorPane pane : panes) {
            for (Node node : pane.getChildren()) {
                collectTextFieldIDs(node, lsPromptMsg, results);
            }
        }
        return results;
    }

    private static void collectTextFieldIDs(Node node, String lsprompt, List<String> results) {
        if (node instanceof TextField) {
            TextField tf = (TextField) node;
            String prompt = tf.getPromptText();
            if (prompt != null && prompt.contains(lsprompt) && tf.getId() != null) {
                results.add(tf.getId());
            }
        } else if (node instanceof Pane) {
            Pane pane = (Pane) node;
            for (Node child : pane.getChildren()) {
                collectTextFieldIDs(child, lsprompt, results);
            }
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                collectTextFieldIDs(child, lsprompt, results);
            }
        }
    }

    private static class scrollOffset {

        double y;
        int caretPos;
    }

    /*Applies customed orange colored vertical scroll bar in textArea*/
    public static void setVerticalScroll(TextArea textArea) {
        textArea.setWrapText(true);
        textArea.applyCss();
        textArea.layout();
        textArea.setStyle(
                "-fx-font-size: 10pt;"
                + "-fx-border-radius: 2px;"
                + "-fx-opacity: 1.0;"
                + "-fx-background-color: grey, white;"
                + "-fx-text-fill: black;"
                + "-fx-border-color: grey;"
        );

        // Access the internal ScrollPane
        ScrollPane scrollPane = (ScrollPane) textArea.lookup(".scroll-pane");
        if (scrollPane != null) {
            scrollPane.setStyle(
                    "-fx-focus-color: -fx-focus-color;"
                    + "-fx-vbar-policy: as-needed;"
                    + "-fx-background-color: transparent;"
                    + "-fx-opacity: 1.0;"
            );

            // Track vertical scroll position
            final scrollOffset xyOffset = new scrollOffset();
            final scrollOffset state = new scrollOffset();

            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.intValue() == 0) {

                } else {
                    xyOffset.y = newVal.doubleValue();
                    System.out.println(xyOffset.y);
                }
            });
            textArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.intValue() <= 0) {

                } else {
                    state.caretPos = newVal.intValue();
                }
            });
            // Restore scroll position on focus lost
            textArea.focusedProperty().addListener((obs, oldVal, isFocused) -> {
                if (isFocused) {
                    textArea.setStyle(
                            "-fx-background-color: orange, white;"
                            + "-fx-text-fill: black;"
                            + "-fx-border-color: orange;"
                    );

                } else {
                    textArea.setStyle(
                            "-fx-background-color: grey, white;"
                            + "-fx-text-fill: black;"
                            + "-fx-border-color: grey;"
                    );
                }
            });
        }

        textArea.getStyleClass().add("custom-text-area");
    }

    /*Old usage; Deprecated*/
    public static class LoadScreenComponents {

        public final ProgressIndicator progressIndicator;
        public final StackPane loadingPane;
        public final Label placeholderLabel;

        public LoadScreenComponents(ProgressIndicator pi, StackPane sp, Label lbl) {
            this.progressIndicator = pi;
            this.loadingPane = sp;
            this.placeholderLabel = lbl;
        }
    }

    /*Deprecated*/
    //JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
    //tblViewDetails.setPlaceholder(loading.loadingPane);
    public static LoadScreenComponents createLoadingComponents() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        progressIndicator.setVisible(true);

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;");

        return new LoadScreenComponents(progressIndicator, loadingPane, placeholderLabel);
    }

    /*Usage sample
        private Stage getOwnerStage() {
        return AnchorMain != null
                && AnchorMain.getScene() != null
                ? (Stage) AnchorMain.getScene().getWindow()
                : null;
    }

    private void handleSaveWithLoading() {
        AtomicReference<JSONObject> loProcessResult = new AtomicReference<>();
        AtomicReference<JSONObject> loOpenResultRef = new AtomicReference<>();
        JFXUtil.runWithLoading(
                getOwnerStage(),
                apButton,
                () -> {
               
                },
                () -> {
          
                }
        );
    }
     */
    // Shared loading helper for controllers that need a modal progress screen.
    public static void runWithLoading(
            Stage foOwner,
            Pane foButtonContainer,
            Runnable foBackgroundAction,
            Runnable foOnSuccess) {
        runWithLoading(foOwner, "Processing...", foButtonContainer, foBackgroundAction, foOnSuccess);
    }

    public static void runWithLoading(
            Stage foOwner,
            String fsMessage,
            Pane foButtonContainer,
            Runnable foBackgroundAction,
            Runnable foOnSuccess) {

        String lsMessage = (fsMessage == null || fsMessage.trim().isEmpty()) ? "Processing..." : fsMessage;
        Stage loLoadingStage = createLoadingStage(foOwner, lsMessage);
        setBrowseLoadingState(true, foButtonContainer);

        Task<Void> loTask = new Task<Void>() {
            @Override
            protected Void call() {
                if (foBackgroundAction != null) {
                    foBackgroundAction.run();
                }
                return null;
            }
        };

        loTask.setOnSucceeded(e -> {
            loLoadingStage.close();
            setBrowseLoadingState(false, foButtonContainer);
            if (foOnSuccess != null) {
                foOnSuccess.run();
            }
        });

        loTask.setOnFailed(e -> {
            loLoadingStage.close();
            setBrowseLoadingState(false, foButtonContainer);
        });

        loLoadingStage.show();

        Thread loThread = new Thread(loTask, "JFXUtil-runWithLoading");
        loThread.setDaemon(true);
        loThread.start();
    }

    public static void setBrowseLoadingState(boolean fbIsLoading, Pane foButtonContainer) {
        if (foButtonContainer != null) {
            foButtonContainer.lookupAll(".button").forEach(loNode -> loNode.setDisable(fbIsLoading));
        }
    }

    public static Stage createLoadingStage(Stage foOwner, String fsMessage) {
        ProgressIndicator loProgressIndicator = new ProgressIndicator();
        loProgressIndicator.setMaxSize(56, 56);
        loProgressIndicator.setStyle("-fx-progress-color: #FF8201;");

        Label loLabel = new Label(fsMessage);
        loLabel.setStyle("-fx-text-fill: #808080; -fx-font-weight: 700;");

        VBox loContainer = new VBox(12, loProgressIndicator, loLabel);
        loContainer.setAlignment(Pos.CENTER);
        loContainer.setPadding(new Insets(20));

        loContainer.setPrefSize(260, 130);
        loContainer.setMinSize(260, 130);
        loContainer.setMaxSize(260, 130);
        loContainer.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 5;"
                + "-fx-border-radius: 5;"
                + "-fx-border-color: #D8D8D8;"
                + "-fx-border-width: 1;"
        );

        StackPane loRoot = new StackPane(loContainer);
        loRoot.setStyle("-fx-background-color: transparent;");

        Scene loScene = new Scene(loRoot);
        loScene.setFill(Color.TRANSPARENT);

        Stage loStage = new Stage();
        loStage.initStyle(StageStyle.TRANSPARENT);

        if (foOwner != null) {
            loStage.initOwner(foOwner);
        }

        loStage.setAlwaysOnTop(true);
        loStage.setResizable(false);
        loStage.setOnCloseRequest(event -> event.consume());
        loStage.setScene(loScene);

        return loStage;
    }

    /*Sets in pxeModuleName ideally for convenient and dynamic getter of form title*/
 /*Requires controller class*/
    public static String getFormattedClassTitle(Class<?> javaclass) {
        String className = javaclass.getSimpleName();

        if (className.endsWith("Controller")) {
            className = className.substring(0, className.length() - "Controller".length());
        }

        // Handle specific company renaming
        className = className.replace("MonarchFood", "MF");
        className = className.replace("MonarchHospitality", "MH");

        // Replace underscores with space
        className = className.replace("_", " ");

        // Add space before capital letters, but preserve acronyms
        className = className.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        className = className.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

        className = className.trim();

        // Special replacements after spacing
        className = className.replace("SP Car", "SPCar");
        className = className.replace("SP MC", "SPMC");

        return className;
    }

    /*Identifies if general based on title*/
    public static boolean isGeneral(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        // Trim and normalize spaces
        title = title.trim();

        String[] generalSuffixes = {
            "Entry", "Confirmation", "History", "Approval"
        };

        for (String suffix : generalSuffixes) {
            // Check if the title ends exactly with one of the general suffixes
            if (title.endsWith(suffix)) {
                return true;
            }
        }

        // If it contains any of those words but continues after, it's not general
        for (String suffix : generalSuffixes) {
            if (title.contains(suffix + " ")) {
                return false;
            }
        }

        return false;
    }

    /*V2, allow modification on naming*/
    public static String getFormattedClassTitle(Class<?> javaclass, String lsChangeIdentifier) {
        String className = javaclass.getSimpleName();

        if (className.endsWith("Controller")) {
            className = className.substring(0, className.length() - "Controller".length());
        }

        className = className.replace("MonarchFood", "MF");
        className = className.replace("MonarchHospitality", "MH");

        className = className.replace("_", " ");

        className = className.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        className = className.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

        className = className.trim();

        className = className.replace("SP Car", "SPCar");
        className = className.replace("SP MC", "SPMC");

        //modification
        switch (lsChangeIdentifier) {
            case "PO":
                className = className.replaceAll("\\bPO\\b", "Purchase Order");
                break;
        }
        return className;
    }

    /*Depracated*/
    //gets title through fxml path
    //JFXUtil.getFormattedClassTitle(this.getClass());
    public static String getFormattedFXMLTitle(String fxmlPath) {
        // Extract the FXML file name without extension
        String fileName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1, fxmlPath.lastIndexOf('.'));

        // Remove common suffixes like "Controller"
        if (fileName.endsWith("Controller")) {
            fileName = fileName.substring(0, fileName.length() - "Controller".length());
        }

        // Handle specific company renaming
        fileName = fileName.replace("MonarchFood", "MF");
        fileName = fileName.replace("MonarchHospitality", "MH");

        // Replace underscores with space
        fileName = fileName.replace("_", " ");

        // Add space before capital letters, keeping acronyms intact
        fileName = fileName.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        fileName = fileName.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

        // Trim and apply custom replacements
        fileName = fileName.trim();
        fileName = fileName.replace("SP Car", "SPCar");
        fileName = fileName.replace("SP MC", "SPMC");

        return fileName;
    }

    /*Selects & focus row through an index number*/
    public static <T> void selectAndFocusRow(TableView<T> tableView, int index) {
        tableView.getSelectionModel().select(index);
        tableView.getFocusModel().focus(index);
    }

    /*Sets particular objects to null*/
    public static void setValueToNull(Object... items) {
        for (Object item : items) {
            if (item instanceof Node) {
                Node node = (Node) item;

                if (node instanceof TextInputControl) {
                    ((TextInputControl) node).clear();
                } else if (node instanceof ComboBox) {
                    ((ComboBox<?>) node).setValue(null);
                } else if (node instanceof CheckBox) {
                    ((CheckBox) node).setSelected(false);
                } else if (node instanceof DatePicker) {
                    ((DatePicker) node).setValue(null);
                } else {
                }
            } else if (item instanceof AtomicReference) {
                ((AtomicReference<?>) item).set(null);
            } else {
            }
        }
    }

    /*Sets action listener*/
    //JFXUtil.TextFieldControlInfo txtcontrol = JFXUtil.getControlInfo((Observable) o);
    public static void setActionListener(EventHandler<ActionEvent> handler, Node... nodes) {
        for (Node node : nodes) {
            if (node instanceof ComboBoxBase) {
                ((ComboBoxBase<?>) node).setOnAction(handler);
            } else if (node instanceof TextField) {
                ((TextField) node).setOnAction(handler);
            }
        }
    }

    public static void setJSONSuccess(JSONObject json, String message) {
        json.put("result", "success");
        json.put("message", message);
    }

    public static void setJSONError(JSONObject json, String message) {
        json.put("result", "error");
        json.put("message", message);
    }

    public static boolean isJSONSuccess(JSONObject json) {
        if (json == null) {
            return false;
        }

        Object result = json.get("result");
        return "success".equals(result);
    }

    public static String getJSONMessage(JSONObject json) {
        if (json == null) {
            return "";
        }
        return (String) json.get("message");
    }

    private static class CommaFormater {

        boolean isUpdating;
        AtomicBoolean isAdjusting;
        int newCaretPos;
    }

    /*Sets real-time comma formatting to a textfield containing numbers or digits*/
 /*Restricts 1 dot*/
 /*Does not recommend setting to other real time textfield formatter*/
    public static void setCommaFormatter(TextField... textFields) {

        DecimalFormat finalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        finalFormat.setGroupingUsed(true);
        finalFormat.setMinimumFractionDigits(2);
        finalFormat.setMaximumFractionDigits(2);

        for (TextField textField : textFields) {
            final CommaFormater data = new CommaFormater();
            data.isUpdating = false;
            data.isAdjusting = new AtomicBoolean(false);
            data.newCaretPos = 0;
            // Disables other character
            UnaryOperator<TextFormatter.Change> filter = change -> {
                String newText = change.getControlNewText();
                if (!newText.matches("[\\d,\\.]*")) {
                    return null;
                }

                long dotCount = newText.chars().filter(c -> c == '.').count();
                if (dotCount > 1) {
                    return null;
                }

                return change;
            };
            textField.setTextFormatter(new TextFormatter<>(filter));
            // Real-time formatting
            textField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (data.isAdjusting.get() == true) {
                    return;
                }
                try {
                    if (data.isUpdating) {
                        return;
                    }
                    data.isUpdating = true;
                    String clean = newValue.replaceAll(",", "");
                    if (clean.isEmpty() || clean.equals(".") || clean.matches("0*\\.0*")) {
                        data.isUpdating = false;
                        return;
                    }
                    try {
                        String integerPart = clean;
                        String decimalPart = "";
                        int dotIndex = clean.indexOf(".");
                        if (dotIndex >= 0) {
                            integerPart = clean.substring(0, dotIndex);
                            decimalPart = clean.substring(dotIndex);
                        }
                        long integerVal = integerPart.isEmpty() ? 0 : Long.parseLong(integerPart);
                        String formattedInteger = NumberFormat.getIntegerInstance(Locale.US).format(integerVal);
                        String formatted = formattedInteger + decimalPart;
                        Platform.runLater(() -> {
                            data.isAdjusting.set(true);
                            int originalCaretPos = textField.getCaretPosition();
                            textField.setText(formatted);
                            int offset = formatted.length() - newValue.length();
                            data.newCaretPos = originalCaretPos + offset;
                            data.newCaretPos = Math.max(0, Math.min(formatted.length(), data.newCaretPos));
                            data.isAdjusting.set(false);
                        });
                        Platform.runLater(() -> {
                            textField.positionCaret(data.newCaretPos);
                        });
                    } catch (Exception e) {
                    }
                    data.isUpdating = false;
                } catch (Exception e) {
                    data.isUpdating = false;
                }

            });
        }
    }

    /*V2 - Allows negative*/
    public static void setCommaFormatter2(TextField... textFields) {

        DecimalFormat finalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        finalFormat.setGroupingUsed(true);
        finalFormat.setMinimumFractionDigits(2);
        finalFormat.setMaximumFractionDigits(2);

        for (TextField textField : textFields) {
            final CommaFormater data = new CommaFormater();
            data.isUpdating = false;
            data.isAdjusting = new AtomicBoolean(false);
            data.newCaretPos = 0;
            // Disables other character
            UnaryOperator<TextFormatter.Change> filter = change -> {
                String newText = change.getControlNewText();

                // Allow digits, comma, dot, and minus
                if (!newText.matches("[\\d,\\.\\-]*")) {
                    return null;
                }

                // Allow only ONE dot
                long dotCount = newText.chars().filter(c -> c == '.').count();
                if (dotCount > 1) {
                    return null;
                }

                // Allow only ONE minus sign
                int minusCount = (int) newText.chars().filter(c -> c == '-').count();
                if (minusCount > 1) {
                    return null;
                }

                // Minus sign must be at the START only
                if (newText.contains("-") && !newText.startsWith("-")) {
                    return null;
                }

                return change;
            };
            textField.setTextFormatter(new TextFormatter<>(filter));
            // Real-time formatting
            textField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (data.isAdjusting.get() == true) {
                    return;
                }
                try {
                    if (data.isUpdating) {
                        return;
                    }
                    data.isUpdating = true;
                    String clean = newValue.replaceAll(",", "");
                    if (clean.isEmpty() || clean.equals(".") || clean.matches("-?0*\\.?0*")) {
                        data.isUpdating = false;
                        return;
                    }
                    try {
                        String integerPart = clean;
                        String decimalPart = "";
                        int dotIndex = clean.indexOf(".");
                        if (dotIndex >= 0) {
                            integerPart = clean.substring(0, dotIndex);
                            decimalPart = clean.substring(dotIndex);
                        }
                        long integerVal = integerPart.isEmpty() ? 0 : Long.parseLong(integerPart);
                        String formattedInteger = NumberFormat.getIntegerInstance(Locale.US).format(integerVal);
                        String formatted = formattedInteger + decimalPart;
                        Platform.runLater(() -> {
                            data.isAdjusting.set(true);
                            int originalCaretPos = textField.getCaretPosition();
                            textField.setText(formatted);
                            int offset = formatted.length() - newValue.length();
                            data.newCaretPos = originalCaretPos + offset;
                            data.newCaretPos = Math.max(0, Math.min(formatted.length(), data.newCaretPos));
                            data.isAdjusting.set(false);
                        });
                        Platform.runLater(() -> {
                            textField.positionCaret(data.newCaretPos);
                        });
                    } catch (Exception e) {
                    }
                    data.isUpdating = false;
                } catch (Exception e) {
                    data.isUpdating = false;
                }

            });
        }
    }

    /*Alternative version of inputDecimalOnly;*/
 /* call once */
    public static void inputIntegersOnly(TextField... foTxtFields) {
        Pattern pattern = Pattern.compile("[0-9]*");
        for (TextField txtField : foTxtFields) {
            if (txtField != null) {
                txtField.setTextFormatter(new TextFormaterUtil(pattern));
            }
        }
    }

    /*Alternative version of inputDecimalOnly; restricts to 1 dot */
    public static void inputDecimalOnly(TextField... foTxtFields) {
        for (TextField txtField : foTxtFields) {
            if (txtField != null) {
                UnaryOperator<TextFormatter.Change> filter = change -> {
                    String newText = change.getControlNewText();

                    // Allow only digits, commas, and at most one dot
                    if (!newText.matches("[0-9,]*\\.?[0-9]*")) {
                        return null;
                    }

                    // Only one dot allowed
                    long dotCount = newText.chars().filter(ch -> ch == '.').count();
                    if (dotCount > 1) {
                        return null;
                    }

                    return change;
                };

                txtField.setTextFormatter(new TextFormatter<>(filter));
            }
        }
    }

    /*Experimental; customed Month & Year only picker*/
    public static class MonthYearPicker {

        public static class Picker {

            public final TextField textField;
            public final Popup popup;
            public final Label yearLabel;
            public final GridPane monthGrid;
            public int selectedYear;
            public int selectedMonth;
            public final Consumer<YearMonth> onDateSelected;

            public Picker(TextField textField, Consumer<YearMonth> onDateSelected) {

                this.textField = textField;
                this.onDateSelected = onDateSelected;
                this.popup = new Popup();
                this.popup.setAutoHide(true);

                selectedYear = YearMonth.now().getYear();
                selectedMonth = YearMonth.now().getMonthValue();

                textField.setPromptText("MM/YYYY");
//                textField.setEditable(false);

                VBox popupContent = new VBox(10);
                popupContent.setPadding(new Insets(10));
                popupContent.getStyleClass().add("popup-content");

                yearLabel = new Label(String.valueOf(selectedYear));
                yearLabel.getStyleClass().add("year-label");

                Button btnPrev = new Button("<");
                Button btnNext = new Button(">");
                btnPrev.getStyleClass().add("year-button");
                btnNext.getStyleClass().add("year-button");

                btnPrev.setOnAction(e -> {
                    selectedYear--;
                    yearLabel.setText(String.valueOf(selectedYear));
                    refreshMonthSelection();
                });

                btnNext.setOnAction(e -> {
                    selectedYear++;
                    yearLabel.setText(String.valueOf(selectedYear));
                    refreshMonthSelection();
                });

                HBox yearControls = new HBox(10, btnPrev, yearLabel, btnNext);
                yearControls.setAlignment(Pos.CENTER);
                yearControls.getStyleClass().add("year-bar");
                monthGrid = new GridPane();
                monthGrid.setHgap(2);
                monthGrid.setVgap(2);
                monthGrid.setAlignment(Pos.CENTER);

                Month[] months = Month.values();
                for (int i = 0; i < months.length; i++) {
                    Button btn = new Button(months[i].getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH));
                    btn.getStyleClass().add("month-button");
                    int monthValue = i + 1;
                    btn.setOnAction(e -> {
                        selectedMonth = monthValue;
                        updateTextFieldAndNotify();
                        refreshMonthSelection();
                        popup.hide();
                        textField.getParent().requestFocus();

                    });
                    monthGrid.add(btn, i % 3, i / 3);
                }

                popupContent.getChildren().addAll(yearControls, monthGrid);
                popup.getContent().add(popupContent);

                // Load CSS
                popupContent.getStylesheets().add(
                        MonthYearPicker.class.getResource("/com/rmj/guanzongroup/sidebarmenus/css/StyleSheet.css").toExternalForm()
                );
                textField.setOnMouseClicked(e -> {
                    if (!popup.isShowing()) {
                        Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
                        double x = bounds.getMinX();
                        double y = bounds.getMaxY();
                        popup.show(textField, x - 13, y - 7);
                    } else {
                        popup.hide();
                    }
                });

                popup.setOnHiding(e -> {
                    updateTextFieldAndNotify();
                });

                // Initialize textField with current date
                updateTextFieldAndNotify();
                refreshMonthSelection();
            }

            public void updateTextFieldAndNotify() {
                YearMonth ym = YearMonth.of(selectedYear, selectedMonth);
                textField.setText(String.format("%02d/%d", ym.getMonthValue(), ym.getYear()));
                if (onDateSelected != null) {
                    onDateSelected.accept(ym);
                }
            }

            public void refreshMonthSelection() {
                monthGrid.getChildren().forEach(node -> node.getStyleClass().remove("selected-month"));
                for (javafx.scene.Node node : monthGrid.getChildren()) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        // Get the month value by matching the displayed short text to Month enum correctly
                        String shortMonthName = btn.getText();
                        Month m = Month.from(
                                java.time.format.DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
                                        .parse(shortMonthName)
                        );
                        if (m.getValue() == selectedMonth) {
                            btn.getStyleClass().add("selected-month");
                            break;
                        }
                    }
                }
                yearLabel.setText(String.valueOf(selectedYear));
            }

            public void setYearMonth(YearMonth ym) {
                selectedYear = ym.getYear();
                selectedMonth = ym.getMonthValue();
                updateTextFieldAndNotify();
                refreshMonthSelection();
            }

            public YearMonth getYearMonth() {
                return YearMonth.of(selectedYear, selectedMonth);
            }

            public void clear() {
                selectedYear = 0;
                selectedMonth = 0;
                textField.clear();
                monthGrid.getChildren().forEach(node -> node.getStyleClass().remove("selected-month"));
            }
        }

        public static Picker setupMonthYearPicker(TextField textField, Consumer<YearMonth> onDateSelected) {
            return new Picker(textField, onDateSelected);
        }
    }

    /*Programmatically press depending on keyCode*/
    public static void makeKeyPressed(Node targetNode, KeyCode keyCode) {
        if (targetNode == null || keyCode == null) {
            return;
        }
        KeyEvent keyEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", // character
                "", // text
                keyCode,
                false, // shiftDown
                false, // controlDown
                false, // altDown
                false // metaDown
        );
        targetNode.fireEvent(keyEvent);
    }

    /*Sets key event to node*/
 /*Requires event handles and any count of node*/
    public static void setKeyEventFilter(EventHandler<KeyEvent> handler, Node... nodes) {
        if (handler == null || nodes == null) {
            return;
        }

        for (Node node : nodes) {
            if (node != null) {
                node.addEventFilter(KeyEvent.KEY_PRESSED, handler);
            }
        }
    }

    /*Focuses in first textfield existing in anchorPane*/
 /*Requires anchorpane*/
    public static void focusFirstTextField(final AnchorPane anchorPane) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TextField textField = findFirstTextField(anchorPane);
                if (textField != null) {
                    textField.requestFocus();
                }
            }
        });
    }

    //private
    private static TextField findFirstTextField(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                return (TextField) node;
            } else if (node instanceof Parent) {
                TextField result = findFirstTextField((Parent) node);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /*Removes comma character existing in a string containing number*/
    public static String removeComma(String numberStr) {
        if (numberStr == null || numberStr.isEmpty()) {
            return "0";
        }

        // Remove commas
        String clean = numberStr.replace(",", "");

        // Check if it's exactly negative zero
        if (clean.matches("-0+(\\.0+)?")) {
            return "0";
        }

        return clean.isEmpty() ? "0" : clean;
    }


    /*Changes checkbox cursor type on hover, depending if enabled/disabled*/
 /*Requires anchorPane to check any checkbox existing in the anchorPane*/
    public static void setCheckboxHoverCursor(Parent... anchorpane) {
        for (Parent container : anchorpane) {
            applyToCheckBoxes(container);
        }
    }

    //private
    private static void applyToCheckBoxes(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof CheckBox) {
                final CheckBox checkBox = (CheckBox) node;
                checkBox.hoverProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        checkBox.setCursor(checkBox.isDisabled() ? Cursor.DEFAULT : Cursor.HAND);
                    } else {
                        checkBox.setCursor(Cursor.DEFAULT);
                    }
                });
            } else if (node instanceof Parent) {
                applyToCheckBoxes((Parent) node); // recursively check inner containers
            }
        }
    }

    @FunctionalInterface
    public interface Action<T> {

        T execute();
    }

    /*Experimental*/
    public static void executeConditional(boolean condition, Runnable trueAction, Runnable falseAction) {
        if (condition) {
            trueAction.run();
        } else {
            falseAction.run();
        }
    }

    /*Used in Dashboard*/
    public static void applyHoverFadeToButtons(String firstColorHex, String secondColorHex, Button... buttons) {
        for (Button button : buttons) {
            Node graphic = button.getGraphic();
            if (graphic instanceof FontAwesomeIconView) {
                FontAwesomeIconView icon = (FontAwesomeIconView) graphic;

                button.setOnMouseEntered(e -> animateColorFade(icon, firstColorHex, secondColorHex));
                button.setOnMouseExited(e -> animateColorFade(icon, secondColorHex, firstColorHex));
            }
        }
    }

    //private
    private static void animateColorFade(FontAwesomeIconView icon, String fromColorHex, String toColorHex) {
        Color startColor = Color.web(fromColorHex);
        Color endColor = Color.web(toColorHex);

        ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(startColor);
        colorProperty.addListener((obs, oldVal, newVal) -> icon.setFill(newVal));

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(colorProperty, startColor)),
                new KeyFrame(Duration.millis(300), new KeyValue(colorProperty, endColor))
        );

        timeline.play();
    }

    public static void applyButtonHoverFade(Button button, String colorHex) {
        if (button == null) {
            return;
        }

        final String baseStyle = (button.getStyle() == null || button.getStyle().trim().isEmpty())
                ? ""
                : button.getStyle().trim() + (button.getStyle().trim().endsWith(";") ? "" : ";");
        final Color accent = Color.web(colorHex);
        final int red = (int) Math.round(accent.getRed() * 255);
        final int green = (int) Math.round(accent.getGreen() * 255);
        final int blue = (int) Math.round(accent.getBlue() * 255);
        final DoubleProperty backgroundOpacity = new SimpleDoubleProperty(1.0);
        final AtomicReference<Timeline> activeAnimation = new AtomicReference<>();
        final int fadeMillis = 100;

        ChangeListener<Number> styleUpdater = (obs, oldVal, newVal) -> {
            boolean isTransparent = backgroundOpacity.get() < 0.999;
            button.setStyle(baseStyle + String.format(Locale.US,
                    " -fx-background-color: rgba(%d,%d,%d,%.3f); -fx-text-fill: %s;",
                    red,
                    green,
                    blue,
                    backgroundOpacity.get(),
                    isTransparent ? String.format(Locale.US, "rgba(%d,%d,%d,1.000)", red, green, blue) : "rgba(255,255,255,1.000)"
            ));
        };
        backgroundOpacity.addListener(styleUpdater);
        styleUpdater.changed(null, null, null);

        EventHandler<? super MouseEvent> previousOnMouseEntered = button.getOnMouseEntered();
        EventHandler<? super MouseEvent> previousOnMouseExited = button.getOnMouseExited();

        button.setOnMouseEntered(event -> {
            if (previousOnMouseEntered != null) {
                previousOnMouseEntered.handle(event);
            }
            Timeline timeline = activeAnimation.getAndSet(new Timeline(
                    new KeyFrame(Duration.millis(fadeMillis), new KeyValue(backgroundOpacity, 0.0))
            ));
            if (timeline != null) {
                timeline.stop();
            }
            activeAnimation.get().play();
        });

        button.setOnMouseExited(event -> {
            if (previousOnMouseExited != null) {
                previousOnMouseExited.handle(event);
            }
            Timeline timeline = activeAnimation.getAndSet(new Timeline(
                    new KeyFrame(Duration.millis(fadeMillis), new KeyValue(backgroundOpacity, 1.0))
            ));
            if (timeline != null) {
                timeline.stop();
            }
            activeAnimation.get().play();
        });
    }

    /*Used in Dashboard*/
    public static void applyToggleHoverAnimation(ToggleButton... toggleButtons) {
        for (ToggleButton toggleButton : toggleButtons) {
            FontAwesomeIconView icon = extractFontAwesomeIcon(toggleButton);
            if (icon != null) {
                // Hover
                toggleButton.setOnMouseEntered(e -> scaleIcon(icon, 1.2, 150));
                toggleButton.setOnMouseExited(e -> {
                    if (!toggleButton.isSelected()) {
                        scaleIcon(icon, 1.0, 150);
                    }
                });

                // Toggle click
                toggleButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        playClickBounce(icon);
                    } else {
                        scaleIcon(icon, 1.0, 150);
                    }
                });
            }
        }
    }

    //private
    private static FontAwesomeIconView extractFontAwesomeIcon(ToggleButton toggleButton) {
        Node graphic = toggleButton.getGraphic();
        if (graphic instanceof FontAwesomeIconView) {
            return (FontAwesomeIconView) graphic;
        }
        return null;
    }

    //private
    private static void scaleIcon(FontAwesomeIconView icon, double scaleTo, double durationMillis) {
        ScaleTransition st = new ScaleTransition(Duration.millis(durationMillis), icon);
        st.setToX(scaleTo);
        st.setToY(scaleTo);
        st.play();
    }

    //private
    private static void playClickBounce(FontAwesomeIconView icon) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(80), icon);
        shrink.setToX(0.9);
        shrink.setToY(0.9);

        ScaleTransition expand = new ScaleTransition(Duration.millis(150), icon);
        expand.setToX(1.2);
        expand.setToY(1.2);

        shrink.setOnFinished(e -> expand.play());
        shrink.play();
    }

    /*Used in Dashboard*/
    public static void placeClockInAnchorPane(AnchorPane anchorPane, double size) {
        if (anchorPane == null) {
            return;
        }

        Pane clockGraphic = createClockGraphic(size);
        anchorPane.getChildren().add(clockGraphic);
    }
    static double hourHand1 = 0.27;
    static double minuteHand1 = 0.3;

    private static Pane createClockGraphic(final double size) {
        final Pane clockPane = new Pane();
        final double center = size / 2;
        final double radius = center - 5;

        Circle clockFace = new Circle(center, center, radius);
        clockFace.setFill(Color.TRANSPARENT);
        clockFace.setStroke(Color.BLACK);
        clockFace.setStrokeWidth(2.5);

        final Line hourHand = new Line(center, center, center, center - radius * hourHand1);
        hourHand.setStrokeWidth(1.3);

        final Line minuteHand = new Line(center, center, center, center - radius * minuteHand1);
        minuteHand.setStrokeWidth(1.3);
        minuteHand.setStroke(Color.BLACK);

        clockPane.getChildren().addAll(clockFace, hourHand, minuteHand);

        Timeline clockUpdater = new Timeline(new KeyFrame(Duration.seconds(1),
                new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                updateHands(hourHand, minuteHand, center);
            }
        }
        ));
        clockUpdater.setCycleCount(Timeline.INDEFINITE);
        clockUpdater.play();

        clockPane.setPrefSize(size, size);
        return clockPane;
    }

    //private
    private static void updateHands(Line hourHand, Line minuteHand, double center) {
        LocalDateTime now = LocalDateTime.now();
        double hourAngle = (now.getHour() % 12 + now.getMinute() / 60.0) * 30;
        double minuteAngle = now.getMinute() * 6;

        setHandAngle(hourHand, hourAngle, center, center * hourHand1);
        setHandAngle(minuteHand, minuteAngle, center, center * minuteHand1);
    }

    //private
    private static void setHandAngle(Line hand, double angle, double center, double length) {
        double radians = Math.toRadians(angle - 90);
        hand.setEndX(center + length * Math.cos(radians));
        hand.setEndY(center + length * Math.sin(radians));
    }

    /*Used in enableRowDragAndDrop()*/
 /*Determines if drag drop will be disabled/enabled*/
    public static class RowDragLock {

        public boolean isEnabled;

        public RowDragLock(boolean enabled) {
            this.isEnabled = enabled;
        }
    }

    /*Enables drag and drop in tableView*/
 /*Returns new arranged indexes that should be set to dragged row*/
    public static <T> void enableRowDragAndDrop(
            TableView<T> tableView,
            Function<T, StringProperty> index01Getter,
            Function<T, StringProperty> index03Getter,
            Function<T, StringProperty> index04Getter,
            RowDragLock dragLock,
            Consumer<Integer> onDropCallback
    ) {
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();

            row.setOnMouseEntered(e -> {
                if (!row.isEmpty() && dragLock.isEnabled && !isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    row.setCursor(Cursor.OPEN_HAND);
                }
            });

            row.setOnMouseExited(e -> row.setCursor(Cursor.DEFAULT));

            row.setOnMousePressed(e -> {
                if (!row.isEmpty() && dragLock.isEnabled && !isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    row.setCursor(Cursor.CLOSED_HAND);
                }
            });

            row.setOnMouseReleased(e -> {
                if (!row.isEmpty() && dragLock.isEnabled && !isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    row.setCursor(Cursor.OPEN_HAND);
                }
            });

            row.setOnDragDetected(event -> {
                if (!dragLock.isEnabled || row.isEmpty() || isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    return;
                }

                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("drag");
                db.setContent(content);

                tableView.getProperties().put("dragSourceIndex", row.getIndex());
                row.setCursor(Cursor.CLOSED_HAND);
                event.consume();
            });

            row.setOnDragOver(event -> {
                if (!dragLock.isEnabled || event.getGestureSource() == row || row.isEmpty()
                        || isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    return;
                }

                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);

                    int dragIndex = (int) tableView.getProperties().get("dragSourceIndex");
                    int hoverIndex = row.getIndex();

                    double sceneY = event.getSceneY();
                    double rowY = row.localToScene(row.getBoundsInLocal()).getMinY();
                    double rowHeight = row.getHeight();
                    boolean isTopHalf = (sceneY - rowY) < rowHeight / 2;
                    int targetIndex = isTopHalf ? hoverIndex : hoverIndex + 1;

                    if (targetIndex == dragIndex || targetIndex == dragIndex + 1) {
                        row.setStyle("");
                    } else if (isTopHalf) {
                        row.setStyle("-fx-border-color: #FF8201; -fx-border-width: 2px 0 0 0;");
                    } else {
                        row.setStyle("-fx-border-color: #FF8201; -fx-border-width: 0 0 2px 0;");
                    }

                    tableView.getProperties().put("highlightIndex", targetIndex);
                    event.consume();
                }
            });

            row.setOnDragExited(e -> row.setStyle(""));

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (!db.hasString()) {
                    return;
                }

                int dragIndex = (int) tableView.getProperties().get("dragSourceIndex");
                int dropIndex = (int) tableView.getProperties().getOrDefault("highlightIndex", dragIndex);

                if (dropIndex > dragIndex) {
                    dropIndex--;
                }

                ObservableList<T> items = tableView.getItems();
                T draggedItem = items.remove(dragIndex);

                if (dropIndex >= items.size()) {
                    items.add(draggedItem);
                    dropIndex = items.size() - 1;
                } else {
                    items.add(dropIndex, draggedItem);
                }

                renumberIndex01(items, index01Getter);
                tableView.getSelectionModel().select(dropIndex);
                tableView.getProperties().remove("dragSourceIndex");
                tableView.getProperties().remove("highlightIndex");

                if (onDropCallback != null) {
                    onDropCallback.accept(dropIndex);
                }

                event.setDropCompleted(true);
                event.consume();
            });

            row.setOnDragDone(e -> row.setCursor(Cursor.DEFAULT));

            return row;
        });
    }

    //private
    private static <T> void renumberIndex01(ObservableList<T> items, Function<T, StringProperty> index01Getter) {
        for (int i = 0; i < items.size(); i++) {
            index01Getter.apply(items.get(i)).set(String.valueOf(i + 1));
        }
    }

    //private
    private static <T> boolean isBlankRow(
            T item,
            Function<T, StringProperty> index01Getter,
            Function<T, StringProperty> index03Getter,
            Function<T, StringProperty> index04Getter
    ) {
        if (item == null) {
            return true;
        }

        String val1 = index01Getter.apply(item).get();
        String val3 = index03Getter.apply(item).get();
        String val4 = index04Getter.apply(item).get();

        return val1 == null || val1.trim().isEmpty()
                || val3 == null || val3.trim().isEmpty()
                || val4 == null || val4.trim().isEmpty();
    }

    /*Used in setComboBoxItems()*/
    public static class Pairs<K, V> {

        public final K key;
        public final V value;

        public Pairs(K ObservableList, V comboBox) {
            this.key = ObservableList;
            this.value = comboBox;
        }
    }

    /*Shortened set of items in multiple comboboxes*/
    public static <T> void setComboBoxItems(Pairs<ObservableList<T>, ComboBox<T>>... comboPairs) {
        for (Pairs<ObservableList<T>, ComboBox<T>> pair : comboPairs) {
            ObservableList<T> list = pair.key;
            ComboBox<T> cb = pair.value;

            cb.getItems().clear();
            cb.setItems(list);

            if (!list.isEmpty()) {
                cb.getSelectionModel().select(0); // selects the first item
            }
        }
    }

    /*Sets combobox action listener*/
    public static void setComboBoxActionListener(EventHandler<ActionEvent> listener, ComboBox<?>... comboBoxes) {
        for (ComboBox<?> cb : comboBoxes) {
            cb.setOnAction(listener);
        }
    }
//

    /*Shortened loadTable loader*/
    //sample usage
// JFXUtil.ReloadableTableTask loadTableDetail;
//        loadTableMain = new JFXUtil.ReloadableTableTask(
//                tblViewTransDetails,
//                details_data,
//                () -> {
//                } );
    public static class ReloadableTableTask {

        private final TableView<?> tableView;
        private final ObservableList<?> data;
        private final Runnable content;

        public ReloadableTableTask(TableView<?> tableView, ObservableList<?> data, Runnable content) {
            this.tableView = tableView;
            this.data = data;
            this.content = content;
        }

        public void reload() {
            LoadScreenComponents loading = createLoadingComponents();
            tableView.setPlaceholder(loading.loadingPane);
            loading.progressIndicator.setVisible(true);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    if (content != null) {
                        content.run(); // Caller must wrap with Platform.runLater if needed
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    if (data == null || data.isEmpty()) {
                        tableView.setPlaceholder(loading.placeholderLabel);
                    } else {
                        tableView.toFront();
                    }
                    loading.progressIndicator.setVisible(false);
                }

                @Override
                protected void failed() {
                    if (data == null || data.isEmpty()) {
                        tableView.setPlaceholder(loading.placeholderLabel);
                    }
                    loading.progressIndicator.setVisible(false);
                }
            };
            new Thread(task).start();
        }
    }

    /*Requests focus in the node passed with 0.5 delay*/
    public static void textFieldMoveNext(Node node) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
                delay.setOnFinished(event -> {
                    if (node instanceof DatePicker) {
                        ((DatePicker) node).getEditor().requestFocus();
                    } else if (node instanceof TextInputControl) {
                        ((TextInputControl) node).requestFocus();
                    } else {
                        node.requestFocus();
                    }
                });
                delay.play();
            }
        });
    }

    /*Used particularly for btnSearch*/
 /*Referenced to pass data, basis for initateBtnSearch*/
    public static void initKeyClickObject(AnchorPane ap, AtomicReference<Object> lastFocusedTextField, AtomicReference<Object> previousSearchedTextField) {
        AnchorPane root = (AnchorPane) ap;
        Scene scene = root.getScene();
        if (scene != null) {
            setKeyEvent(scene, lastFocusedTextField, previousSearchedTextField);
        } else {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    setKeyEvent(newScene, lastFocusedTextField, previousSearchedTextField);
                }
            });
        }
    }

    //private
    private static void setKeyEvent(Scene scene, AtomicReference<Object> lastFocusedTextField, AtomicReference<Object> previousSearchedTextField) {
        scene.focusOwnerProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                if (newNode instanceof Button) {
                } else {
                    lastFocusedTextField.set(newNode);
                    previousSearchedTextField.set(null);
                }
            }
        });
    }

    /*Used particularly for btnSearch, connected to initKeyClickObject*/
 /*Detects & Validates if focused, identifies last focused node, and identifies if there is no focus in any textfield*/
    public static void initiateBtnSearch(
            String pxeModuleName,
            AtomicReference<Object> lastFocusedTextField,
            AtomicReference<Object> previousSearchedTextField,
            AnchorPane... anchorPanes
    ) {
        String lsMessage = "Focus a searchable textfield to search";
        Object lastNode = lastFocusedTextField.get();

        if (lastNode instanceof TextField) {
            TextField tf = (TextField) lastNode;

            boolean isSearchable = false;
            for (AnchorPane ap : anchorPanes) {
                if (JFXUtil.getTextFieldsIDWithPrompt("Press F3: Search", ap).contains(tf.getId())) {
                    isSearchable = true;
                    break;
                }
            }

            if (isSearchable) {
                if (lastNode == previousSearchedTextField.get()) {
                    return;
                }

                previousSearchedTextField.set(lastNode);
                JFXUtil.makeKeyPressed(tf, KeyCode.F3);
            } else {
                ShowMessageFX.Information(null, pxeModuleName, lsMessage);
            }

        } else if (lastNode != null) {
            ShowMessageFX.Information(null, pxeModuleName, lsMessage);
        } else {
            ShowMessageFX.Information(null, pxeModuleName, lsMessage);
        }
    }

    /*Shortened Lost focus Listener*/
// sample usage
//    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
//            (lsID, lsValue) -> {
//            });
    public static ChangeListener<Boolean> FocusListener(Class<? extends TextInputControl> nodeType, BiConsumer<String, String> onLostFocus) {
        return (observable, oldValue, newValue) -> {
            Object bean = ((ReadOnlyBooleanPropertyBase) observable).getBean();
            if (!nodeType.isInstance(bean)) {
                return;
            }

            TextInputControl control = nodeType.cast(bean);
            String id = control.getId();
            String value = control.getText();

            if (value == null) {
                return;
            }

            if (!newValue) { // Lost focus
                onLostFocus.accept(id, value.trim());
            }
        };
    }

    /*Disables up and down focus & selection in a tableView*/
    public static <T> void disableArrowNavigation(TableView<T> table) {
        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                event.consume(); // Prevents moving up/down between rows
            }
        });
    }

    /*Adds checkbox to any column, returns column index and row index, and check status*/
 /*Requires Table Model, tableView, boolean, and column int index/s that checkbox will be applied (e.g. 0,1,2)*/
    public static <T> void addCheckboxColumns(
            Class<T> modelClass,
            TableView<T> table,
            BooleanProperty disableAll,
            TriConsumer<T, Integer, Integer, Boolean> onChange,
            int... columnIndexes) {
        addCheckboxColumns(modelClass, table, disableAll, onChange, null, columnIndexes);
    }

    public static <T> void addCheckboxColumns(
            Class<T> modelClass,
            TableView<T> table,
            BooleanProperty disableAll,
            TriConsumer<T, Integer, Integer, Boolean> onChange,
            TriConsumer3<T, Integer, Integer> onDisabledClick,
            int... columnIndexes) {

        for (int colIndex : columnIndexes) {
            @SuppressWarnings("unchecked")
            TableColumn<T, ?> baseCol = table.getColumns().get(colIndex);

            @SuppressWarnings("unchecked")
            TableColumn<T, Boolean> column = (TableColumn<T, Boolean>) baseCol;

            final int finalColIndex = colIndex;

            column.setCellValueFactory(cellData -> {
                T row = cellData.getValue();
                try {
                    // Expect getters: getIndex01, getIndex02, etc.
                    String getterName = "getIndex" + String.format("%02d", colIndex + 1);
                    Method getter = modelClass.getMethod(getterName);

                    String value = (String) getter.invoke(row);
                    boolean boolVal = "1".equals(value);
                    return new javafx.beans.property.SimpleBooleanProperty(boolVal);

                } catch (Exception e) {
                    e.printStackTrace();
                    return new javafx.beans.property.SimpleBooleanProperty(false);
                }
            });

            column.setCellFactory(new Callback<TableColumn<T, Boolean>, TableCell<T, Boolean>>() {
                @Override
                public TableCell<T, Boolean> call(TableColumn<T, Boolean> param) {
                    return new TableCell<T, Boolean>() {
                        private final CheckBox checkBox = new CheckBox();

                        {
                            // Center checkbox
                            setStyle("-fx-alignment: CENTER;");
//                            setCheckboxStyle("#7B8182", checkBox);
                            // Cursor binding
                            checkBox.cursorProperty().bind(
                                    Bindings.when(disableAll)
                                            .then(Cursor.DEFAULT)
                                            .otherwise(Cursor.HAND)
                            );

                            // Disable binding
                            checkBox.disableProperty().bind(disableAll);

                            // Only trigger on user click
                            checkBox.setOnAction(evt -> {
                                if (getTableRow() != null && getTableRow().getItem() != null) {
                                    @SuppressWarnings("unchecked")
                                    T row = (T) getTableRow().getItem();
                                    int rowIndex = getTableRow().getIndex();

                                    try {
                                        // Reflect setter e.g. setIndex01
                                        String setterName = "setIndex" + String.format("%02d", finalColIndex + 1);
                                        Method setter = modelClass.getMethod(setterName, String.class);
                                        setter.invoke(row, checkBox.isSelected() ? "1" : "0");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if (onChange != null) {
                                        onChange.accept(row, rowIndex, finalColIndex, checkBox.isSelected());
                                    }
                                }
                            });

                            // Capture clicks while disabled so caller can react with row/column context.
                            setOnMouseClicked(evt -> {
                                if (!disableAll.get()) {
                                    return;
                                }

                                if (onDisabledClick == null) {
                                    return;
                                }

                                if (getTableRow() == null || getTableRow().getItem() == null) {
                                    return;
                                }

                                @SuppressWarnings("unchecked")
                                T row = (T) getTableRow().getItem();
                                int rowIndex = getTableRow().getIndex();

                                onDisabledClick.accept(row, rowIndex, finalColIndex);

                                evt.consume();
                            });
                        }

                        @Override
                        protected void updateItem(Boolean item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                checkBox.setSelected(item != null && item);
                                setGraphic(checkBox);
                            }
                        }
                    };
                }
            });
        }
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V, W> {

        void accept(T t, U u, V v, W w);
    }

    @FunctionalInterface
    public interface TriConsumer3<T, U, V> {

        void accept(T t, U u, V v);
    }

    /*Programmatically clicks particular tab based on its title*/
 /*Requires Tabpane & tab title*/
    public static void clickTabByTitleText(TabPane tabPane, String title) {
        for (Tab tab : tabPane.getTabs()) {
            if (title.equals(tab.getText())) {
                Tab current = tabPane.getSelectionModel().getSelectedItem();
                if (current == tab && !tabPane.getTabs().isEmpty()) {
                    // Temporarily select the first tab (index 0) to force deselection
                    tabPane.getSelectionModel().select(0);
                }
                // Now select the target tab again
                tabPane.getSelectionModel().select(tab);
                break;
            }
        }
    }

    /*Alternative to Platform.runlater() added seconds delay before execution*/
    public static void runWithDelay(double seconds, Runnable action) {
        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.seconds(seconds));
            delay.setOnFinished(e -> action.run());
            delay.play();
        });
    }

    private static final Map<Class<?>, Map<String, String>> cache = new HashMap<>();

    /*Sets value to a label, textField, textArea, or button from class various variable name w/ value*/
 /*Ideally used for set Status label of Transaction*/
 /*Compares Status Class variables Values (2nd parameter) to String value from (3rd parameter)*/
 /*Requires Node, Status Class, and a String value*/
    public static String setStatusValue(Node node, Class<?> clazz, String value) {
        String text = getNameByValue(clazz, value);

        Platform.runLater(() -> {
            if (node instanceof Label) {
                ((Label) node).setText(text);
            } else if (node instanceof TextField) {
                ((TextField) node).setText(text);
            } else if (node instanceof TextArea) {
                ((TextArea) node).setText(text);
            } else if (node instanceof Button) {
                ((Button) node).setText(text);
            } else {
                //if null
            }
        });
        return text;
    }

    //private
    private static String getNameByValue(Class<?> clazz, String value) {
        if ("-1".equals(value) || JFXUtil.isObjectEqualTo(value, null, "")) {
            return "UNKNOWN";
        }
        return buildValueToNameMap(clazz).getOrDefault(value, "UNKNOWN");
    }

    //private
    private static Map<String, String> buildValueToNameMap(Class<?> clazz) {
//        if (cache.containsKey(clazz)) {
//            return cache.get(clazz);
//        }
        Map<String, String> valueToNameMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    Object value = field.get(null);
                    if (value instanceof String) {
                        String fieldName = field.getName();
                        if (fieldName != null) {
                            fieldName = fieldName.replace("_", " ");
                        }
                        // Special handling: if field name is VOID, change to VOIDED
                        if ("VOID".equals(fieldName)) {
                            fieldName = "VOIDED";
                        }
//                        if ("DIGITAL PAYMENT".equals(fieldName)) {
//                            fieldName = "E-WALLET";
//                        }
                        if ("WIRED".equals(fieldName)) {
                            fieldName = "BANK TRANSFER";
                        }
                        valueToNameMap.putIfAbsent((String) value, fieldName);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        cache.put(clazz, valueToNameMap);
        return valueToNameMap;
    }

    /*Alternative ComboboxListener*/
//    sample usage
//    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
//            (cmbId, selectedIndex, selectedValue) -> {
//            }
//    );
    public static <T> EventHandler<ActionEvent> CmbActionListener(ComboBoxListener<T> listener) {
        return event -> {
            @SuppressWarnings("unchecked")
            ComboBox<T> comboBox = (ComboBox<T>) event.getSource();
            Platform.runLater(() -> {
                // Ignore programmatic changes (like setValue/select in code)
                if (!comboBox.isFocused()) {
                    return;
                }

                String comboId = comboBox.getId() != null ? comboBox.getId() : "NO_ID";
                int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
                T selectedValue = comboBox.getSelectionModel().getSelectedItem();

                listener.onChange(comboId, selectedIndex, selectedValue);
            });
        };
    }

    @FunctionalInterface
    public interface ComboBoxListener<T> {

        void onChange(String comboId, int selectedIndex, T selectedValue);
    }

    /*Requests focus on a textfield, only if its object condition is null or blank*/
    public static void requestFocusNullField(Object[][] checks, TextInputControl fallback) {
        TextInputControl target = Stream.of(checks)
                .filter(c -> {
                    try {
                        return isObjectEqualTo(c[0], null, "");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(c -> (TextInputControl) c[1])
                .filter(ctrl -> ctrl != null && !ctrl.isDisabled())
                .findFirst()
                .orElse(null);

        // focus result
        if (target != null) {
            target.requestFocus();
        } else if (fallback != null && !fallback.isDisabled()) {
            fallback.requestFocus();
        }
    }

    /*Compares an object to any object if equal*/
    public static boolean isObjectEqualTo(Object source, Object... others) {
        if (source == null && others != null) {
            for (Object other : others) {
                if (other == null) {
                    return true;
                }
            }
            return false;
        }

        for (Object other : others) {
            if (source != null && source.equals(other)) {
                return true;
            }
        }
        return false;
    }

    /*Returns title from class*/
    public static AbstractMap.SimpleEntry<String, Class<? extends ScreenInterface>> returnData(
            Class<? extends ScreenInterface> clazz) {
        return new AbstractMap.SimpleEntry<>(getFormattedClassTitle(clazz), clazz);
    }

    /*Alternative to break*/
 /*Throws similar break functionality*/
    public static class BreakLoopException extends RuntimeException {
    }

    /*Experimental*/
    public static void ifError(boolean isError, TextField txtField, JSONObject poJSON, String pxeModuleName, Runnable orElse) {
        if (isError) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            txtField.setText("");
            throw new BreakLoopException();
        } else {
            orElse.run();
        }
    }

    /*Used when Reverse is applied*/
 /*Requires Table's Observable list, pnDetail, and column index number that contains original index of the item from xml (e.g. 7 as index07-based on Model)*/
    public static int getDetailRow(ObservableList<?> dataList, int lnpn, int columnIndex) {
        int result = lnpn - 1;
        try {
            for (int lnCtr = 0; lnCtr < dataList.size(); lnCtr++) {
                Object item = dataList.get(lnCtr);
                // Build dynamic getter name like "getIndex07"
                String getterName = String.format("getIndex%02d", columnIndex);
                String value = (String) item.getClass().getMethod(getterName).invoke(item);

                if (String.valueOf(lnpn).equals(value)) { // (compares pnDetail & value of column)
                    // Always get index01
                    String index01 = (String) item.getClass().getMethod("getIndex01").invoke(item);
                    result = Integer.parseInt(index01) - 1;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*Used when Reverse is applied*/
 /*Requires Table's Observable list, Temporary Row No (RowNo visible in table), 
    and column index number that contains original index of the item from xml (e.g. 7 as index07-based on Model)*/
    public static int getDetailTempRow(ObservableList<?> dataList, int lnpn, int columnIndex) {
        int result = 0;
        try {
            for (int lnCtr = 0; lnCtr < dataList.size(); lnCtr++) {
                Object item = dataList.get(lnCtr);
                // Always search using Index01
                String index01 = (String) item.getClass().getMethod("getIndex01").invoke(item);

                if (String.valueOf(lnpn).equals(index01)) { // (compares temp Row No & value of column 1 or row 1)
                    // Build dynamic getter name for return field sample index07
                    String getterName = String.format("getIndex%02d", columnIndex); // gets the original index from columnIndex (e.g. 7)
                    String value = (String) item.getClass().getMethod(getterName).invoke(item);
                    result = Integer.parseInt(value);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*Checks if the folder exists from path, creates if there is none */
 /*Requires JSONObject and folder path */
    public static JSONObject checkIfFolderExists(JSONObject poJSON, String lsExportPath) {
        File folder = new File(lsExportPath);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                // Folder successfully created
                System.out.println("Folder created at: " + folder.getAbsolutePath());
            } else {
                // Failed to create folder
                poJSON.put("result", "error");
                poJSON.put("message", "Failed to create folder. \n\nEnsure the application has write permissions \"");
                return poJSON;
            }
        }
        return poJSON;
    }

    /*Removes TextField listener*/
    public static void removeTextFieldListener(ChangeListener<String> searchListener, TextField textField) {
        if (searchListener != null) {
            // Remove the listener if already attached
            textField.textProperty().removeListener(searchListener);
        }
    }

    /*Gets Node id and returns string English format of the id*/
    public static String IDToWord(String id) {
        if (id == null || id.isEmpty()) {
            return "";
        }
        // Remove known common prefixes like tf, dp, cb, btn, etc.
        String cleaned = id.replaceFirst("^(tf|dp|cb|btn|lbl|txt|cmb|ap|rb)", "");
        // Insert spaces before capital letters but not between consecutive capitals (e.g., "SI")
        String withSpaces = cleaned.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        // Capitalize first character if needed
        if (!withSpaces.isEmpty()) {
            withSpaces = withSpaces.substring(0, 1).toUpperCase() + withSpaces.substring(1);
        }
        return withSpaces.trim();
    }

    /*ComboBox value setter; Prevents listener to trigger while setting value*/
 /*requires combobox id and index value to be selected*/
    public static <T> void setCmbValue(ComboBox<T> comboBox, Object value) {
        if (comboBox == null) {
            return;
        }

        EventHandler<ActionEvent> originalHandler = comboBox.getOnAction();
        comboBox.setOnAction(null);

        // ✅ If null or -1 → select index -1
        if (value == null || (value instanceof Integer && (Integer) value == -1)) {
            comboBox.getSelectionModel().select(-1);
            comboBox.setOnAction(originalHandler);
            return;
        }

        if (value instanceof Integer) {
            int index = (Integer) value;
            if (index >= 0 && index < comboBox.getItems().size()) {
                comboBox.getSelectionModel().select(index);
            }

        } else if (value instanceof String) {
            String title = (String) value;
            for (T item : comboBox.getItems()) {
                if (title.equals(String.valueOf(item))) {
                    comboBox.getSelectionModel().select(item);
                    break;
                }
            }
        }

        comboBox.setOnAction(originalHandler);
    }


    /*Returns description or code of the source type*/
 /*Requires string value(for comparison) and boolean if the string value is code(to return description); alternatively*/
//    public static String getSourceType(String lsValue, boolean isCode) {
//        if (lsValue == null || lsValue.trim().isEmpty()) {
//            return "";
//        }
//
//        String val = lsValue.trim().toLowerCase();
//
//        for (Map.Entry<String, String> entry : SOURCE_MAP.entrySet()) {
//            String code = entry.getKey();
//            String desc = entry.getValue();
//
//            if (val.equalsIgnoreCase(code)) {
//                return isCode ? desc : code;
//            } else if (val.equalsIgnoreCase(desc)) {
//                return isCode ? code : desc;
//            }
//        }
//
//        return "";
//    }
//    //private static source
//    private static final Map<String, String> SOURCE_MAP = new HashMap<>();
//
//    static {
//        SOURCE_MAP.put("PRFx", "PRF");
//        SOURCE_MAP.put("SOAt", "SOA");
//        SOURCE_MAP.put("CcPy", "Cache Payable");
//        SOURCE_MAP.put("PORc", "PO Receiving");
//        SOURCE_MAP.put("APAd", "AP Adjustment");
//        SOURCE_MAP.put("PO", "Purchase Order");
//    }
    /*Sets Value in datepicker without triggering the Action Listener*/
    public static void setDateValue(DatePicker datePicker, LocalDate value) {
        if (datePicker == null) {
            return;
        }
        EventHandler<ActionEvent> originalHandler = datePicker.getOnAction();
        try {
            datePicker.setOnAction(null);
            datePicker.setValue(value);
        } finally {
            datePicker.setOnAction(originalHandler);
        }
    }

    /*Shortcut for detecting up and down of tableview*/
    public abstract static class TableKeyEvent implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            if (focusedCell == null) {
                return;
            }

            boolean moveDown = event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.DOWN;
            boolean moveUp = event.getCode() == KeyCode.UP;

            if (moveDown || moveUp) {
                onRowMove(currentTable, currentTable.getId(), moveDown);
                event.consume();
            }
        }

        protected abstract void onRowMove(TableView<?> currentTable, String currentTableID, boolean moveDown);
    }

    /*Detect clicks in disabled Nodes and returns IDs in callback*/
 /*Requires parent Anchorpane of the Node and pnEditMode*/
    public static void handleDisabledNodeClick(AnchorPane parent, Object editMode, Consumer<String> callback) {
        if (parent == null || callback == null) {
            return;
        }
        parent.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            // Validate edit mode
            if (!JFXUtil.isObjectEqualTo(editMode, EditMode.ADDNEW, EditMode.UPDATE)) {
                return;
            }
            // Collect nodes
            Set<Node> nodes = new HashSet<>();
            nodes.addAll(parent.lookupAll(".text-field"));
            nodes.addAll(parent.lookupAll(".combo-box"));
            nodes.addAll(parent.lookupAll(".check-box"));
            nodes.addAll(parent.lookupAll(".text-area"));
            nodes.addAll(parent.lookupAll(".date-picker"));
            nodes.addAll(parent.lookupAll(".button"));
            if (nodes.isEmpty()) {
                return; // nothing to check
            }
            for (Node node : nodes) {

                boolean inactive = node.isDisabled();

                // Also treat non-editable text controls as disabled
                if (node instanceof TextInputControl) {
                    TextInputControl textInput = (TextInputControl) node;
                    inactive |= !textInput.isEditable();
                }

                if (!inactive) {
                    continue;
                }

                String nodeId = node.getId();
                if (nodeId == null || nodeId.trim().isEmpty()) {
                    continue;
                }

                Bounds boundsInScene = node.localToScene(node.getBoundsInLocal());

                if (boundsInScene.contains(event.getSceneX(), event.getSceneY())) {
                    callback.accept(nodeId);
                    return;
                }
            }
            // No disabled node detected → callback is NOT called
        });
    }

    /*Enables glow effect around a Node*/
 /*Requires Node Id and hex Color (e.g. #H1H1H1)*/
    public static void glowOnce(Node node, String hexColor) {
        Color glowColor = Color.web(hexColor); // convert hex to Color

        DropShadow glow = new DropShadow();
        glow.setColor(glowColor);
        glow.setRadius(0);
        glow.setSpread(0);

        node.setEffect(glow);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 0),
                        new KeyValue(glow.spreadProperty(), 0)
                ),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(glow.radiusProperty(), 25),
                        new KeyValue(glow.spreadProperty(), 0.35)
                ),
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(glow.radiusProperty(), 0),
                        new KeyValue(glow.spreadProperty(), 0)
                )
        );

        timeline.setOnFinished(e -> node.setEffect(null));
        timeline.play();
    }

    /*Displays customized tooltip for Node hover*/
 /*Requires message & any count of nodes*/
    public static void applyHoverTooltip(String message, Node... nodes) {
        if (message == null || nodes == null) {
            return;
        }

        for (Node node : nodes) {
            if (node != null) {
                Tooltip tooltip = new Tooltip(message);
                tooltip.setStyle(
                        "-fx-font-size: 10px;"
                        + "-fx-padding: 6 10 6 10;"
                );

                Tooltip.install(node, tooltip);
            }
        }
    }

    /*Displays customized tooltip for Node hover with delay*/
 /*Requires double type number, message & any count of nodes*/
    public static void applyHoverTooltipDelay(double delay, String message, Node... nodes) {
        if (message == null || nodes == null) {
            return;
        }

        for (Node node : nodes) {
            if (node != null) {
                Tooltip tooltip = new Tooltip(message);

                //tooltip.setShowDelay(Duration.seconds(delay));
                tooltip.setStyle(
                        "-fx-font-size: 10px;"
                        + "-fx-padding: 6 10 6 10;"
                );

                Tooltip.install(node, tooltip);
            }
        }
    }

    public static void setVisibility(boolean visible, Node... nodes) {
        if (nodes == null) {
            return;
        }

        for (Node node : nodes) {
            if (node != null) {
                node.setVisible(visible);
            }
        }
    }

    /*Alternative to switch case, code lines saver*/
    public static void altSwitch(String key, Object[][] cases) {
        for (Object[] c : cases) {
            String[] keys = (String[]) c[0];
            Runnable action = (Runnable) c[1];

            for (String k : keys) {
                if (k.equals(key)) {
                    action.run();
                    return;
                }
            }
        }
    }

    /*Detects particular disabled tabs*/
    public static void checkDisabledTabs(TabPane tabPane, Consumer<Tab> action) {
        tabPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            for (Node node : tabPane.lookupAll(".tab")) {
                if (node.localToScene(node.getBoundsInLocal())
                        .contains(event.getSceneX(), event.getSceneY())) {

                    Label label = (Label) node.lookup(".tab-label");
                    if (label == null) {
                        return;
                    }
                    String tabText = label.getText();
                    for (Tab tab : tabPane.getTabs()) {
                        if (tab.isDisable() && tabText.equals(tab.getText())) {
                            action.accept(tab);
                            event.consume();
                            return;
                        }
                    }
                }
            }
        });
    }

    /*Detects tab selected returns id*/
 /*Used to enhance readability*/
    public static void onTabSelected(TabPane tabPane, Consumer<String> onTabTitleSelected) {
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldTab, newTab) -> {
                    if (newTab != null) {
                        onTabTitleSelected.accept(newTab.getText());
                    }
                });
    }

    /*Validator for confirmation for selection in lists*/
    public static boolean loadValidation(int pnEditMode, String pxeModuleName, String lsCurrentTransNo, String lsTransactionNo) {

        if (pnEditMode == EditMode.UPDATE) {
            if (lsCurrentTransNo.equals(lsTransactionNo)) {
                if (!ShowMessageFX.YesNo(null, pxeModuleName, "Transaction is currently in update mode.\n"
                        + "Reload the transaction?")) {
                    return false;
                }
            } else {
                if (!ShowMessageFX.YesNo(null, pxeModuleName, "Transaction is currently in update mode.\n"
                        + "Are you sure you want to select another transaction?")) {
                    return false;
                }
            }
        }
        return true;
    }

    /*Validator for entry for selection in lists*/
    public static boolean loadValidation2(int pnEditMode, String pxeModuleName, String lsCurrentTransNo, String lsTransactionNo, double lnTotal) {
        if (pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
            String lsTrans = pnEditMode == EditMode.UPDATE ? "update mode." : "edit mode.";
            if (lsCurrentTransNo.equals(lsTransactionNo)) {
                if (!ShowMessageFX.YesNo(null, pxeModuleName, "Transaction is currently in " + lsTrans + "\n"
                        + "Reload the transaction?")) {
                    return false;
                }
            } else {
                if (!isObjectEqualTo(lsCurrentTransNo, null, "")) {
                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Transaction is currently in " + lsTrans + "\n"
                            + "Are you sure you want to select another transaction?")) {
                        return false;
                    }
                } else {
                    //if has No SourceNo
                    if (lnTotal > 0) {
                        if (pnEditMode == EditMode.UPDATE) {
                            if (!ShowMessageFX.YesNo(null, pxeModuleName, "Transaction is currently in " + lsTrans + "\n"
                                    + "Are you sure you want to select another transaction?")) {
                                return false;
                            }
                        } else {
                            if (!ShowMessageFX.YesNo(null, pxeModuleName, "Transaction details are not empty.\n"
                                    + "Proceeding will replace all existing details.\nWould you like to proceed?")) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /*Creates fade in and moving up simultaneously animation*/
    public static void fadeInFromBottom(double seconds, Node... nodes) {
        for (Node node : nodes) {

            // store original translateY (important for layouts)
            double originalTranslateY = node.getTranslateY();

            node.setOpacity(0);
            node.setVisible(true);
            node.setTranslateY(originalTranslateY + 20); // start lower

            FadeTransition fade = new FadeTransition(Duration.seconds(seconds), node);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition moveUp = new TranslateTransition(Duration.seconds(seconds), node);
            moveUp.setFromY(originalTranslateY + 20);
            moveUp.setToY(originalTranslateY);

            ParallelTransition animation
                    = new ParallelTransition(node, fade, moveUp);

            animation.play();
        }
    }

    /*Adds fades in effect in node*/
    public static void fadeIn(double seconds, Node... nodes) {
        for (Node node : nodes) {
            node.setOpacity(0); // start invisible
            node.setVisible(true); // ensure node is visible
            FadeTransition ft = new FadeTransition(Duration.seconds(seconds), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    /*Displays 5 secs tooltip to a particular node*/
    public static void showTooltip(String message, Node... nodes) {
        if (message == null || message.trim().isEmpty() || nodes == null || nodes.length == 0) {
            return;
        }

        Platform.runLater(() -> {
            Tooltip tooltip = new Tooltip(message);
            tooltip.setStyle(
                    "-fx-font-size: 12px;"
                    + "-fx-padding: 6 10 6 10;"
            );

            for (Node node : nodes) {
                if (node == null || node.getScene() == null) {
                    continue;
                }

                double x = node.localToScreen(node.getBoundsInLocal()).getMinX();
                double y = node.localToScreen(node.getBoundsInLocal()).getMaxY();

                tooltip.show(node, x, y);

                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(e -> {
                    if (tooltip.isShowing()) {
                        tooltip.hide();
                    }
                });
                delay.play();
            }
        });
    }

    public static void animateFontAwesomeIcons(double timeSeconds, int direction, ToggleButton... buttons) {
        if (buttons == null || buttons.length == 0) {
            return;
        }

        for (ToggleButton button : buttons) {
            Node icon = button.getGraphic();
            if (icon == null) {
                continue;
            }

            double offset = 12;
            double fromX = (direction == 0) ? -offset : offset;

            // Initial state
            icon.setOpacity(0);
            icon.setTranslateX(fromX);

            FadeTransition fade = new FadeTransition(
                    Duration.seconds(1), icon
            );
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(
                    Duration.seconds(timeSeconds), icon
            );
            slide.setFromX(fromX);
            slide.setToX(0);

            ParallelTransition animation
                    = new ParallelTransition(fade, slide);

            animation.play();
        }
    }

    public static void playUpwardFadeOut(double delaySeconds, double durationSeconds, Node... nodes) {
        for (Node node : nodes) {
            if (node == null) {
                continue;
            }

            FadeTransition fade = new FadeTransition(
                    Duration.seconds(durationSeconds), node
            );
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            TranslateTransition moveUp = new TranslateTransition(
                    Duration.seconds(durationSeconds), node
            );
            moveUp.setFromY(0);
            moveUp.setToY(-20); // upward movement (adjust as needed)

            ParallelTransition animation
                    = new ParallelTransition(node, fade, moveUp);
            animation.setDelay(Duration.seconds(delaySeconds));
            animation.play();
        }
    }

    @FunctionalInterface
    public interface DatePickerCommand {

        void run(
                DatePicker DatePicker,
                SimpleDateFormat sdfFormat,
                String lsServerDate,
                LocalDate ldCurrentDate,
                String lsSelectedDate,
                LocalDate ldSelectedDate
        );
    }

    //alternative datepicker action event listener to lessen code line in ui controllers
    public static EventHandler<ActionEvent> DatePickerAction(DatePickerCommand command) {
        return event -> {

            JSONObject poJSON = new JSONObject();
            JFXUtil.setJSONSuccess(poJSON, "success");

            Object source = event.getSource();
            if (source instanceof DatePicker) {
                Platform.runLater(() -> {
                    try {
                        DatePicker datePicker = (DatePicker) source;
                        String inputText = datePicker.getEditor().getText();
                        SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                        LocalDate ldCurrentDate = null, ldSelectedDate = null;
                        String lsServerDate = "", lsTransDate = "", lsSelectedDate = "";
                        if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                            return;
                        }
                        lsServerDate = sdfFormat.format(oApp.getServerDate());
                        ldCurrentDate = LocalDate.parse(
                                lsServerDate,
                                DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE)
                        );
                        lsSelectedDate = sdfFormat.format(
                                SQLUtil.toDate(
                                        JFXUtil.convertToIsoFormat(inputText),
                                        SQLUtil.FORMAT_SHORT_DATE
                                )
                        );
                        ldSelectedDate = LocalDate.parse(
                                lsSelectedDate,
                                DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE)
                        );  // your custom command here
                        if (command != null) {
                            command.run(
                                    datePicker,
                                    sdfFormat,
                                    lsServerDate,
                                    ldCurrentDate,
                                    lsSelectedDate,
                                    ldSelectedDate
                            );
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(JFXUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
        };
    }

    private static Timeline radialTimeline;

    /*Anim used in login*/
    public static void applyClockwiseFillAnimation(AnchorPane targetPane, double speed) {
        Platform.runLater(() -> {
            double width = targetPane.getPrefWidth();
            double height = targetPane.getPrefHeight();

            // Stop previous animation if exists
            if (radialTimeline != null) {
                radialTimeline.stop();
            }

            // Clip for rounded rectangle
            Rectangle clip = new Rectangle(width, height);
            clip.setArcWidth(40);
            clip.setArcHeight(40);
            targetPane.setClip(clip);

            // Check if canvas already exists (avoid stacking)
            Canvas canvas;
            if (!targetPane.getChildren().isEmpty() && targetPane.getChildren().get(0) instanceof Canvas) {
                canvas = (Canvas) targetPane.getChildren().get(0);
            } else {
                canvas = new Canvas(width, height);
                targetPane.getChildren().add(0, canvas);
            }

            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Reset angle
            final double[] angle = {0};

            radialTimeline = new Timeline(new KeyFrame(Duration.millis(15), event -> {

                angle[0] += speed; // 🔥 speed now controls rotation increment

                if (angle[0] > 360) {
                    angle[0] = 360;
                    radialTimeline.stop(); // stop when fully filled (optional)
                }

                // redraw background
                gc.setFill(Color.web("#F4F4F4"));
                gc.fillRoundRect(0, 0, width, height, 40, 40);

                // draw arc
                gc.setFill(Color.web("#FF8201"));
                gc.fillArc(-100, -100, width + 200, height + 200, 90, -angle[0], javafx.scene.shape.ArcType.ROUND);
            }));

            radialTimeline.setCycleCount(Timeline.INDEFINITE);
            radialTimeline.playFromStart();
        });
    }
    /*Reserved values for cmdButton purposes*/
    public static String[] buttonPackArray1 = {"btnSave", "btnCancel", "btnApprove", "btnDisapprove", "btnVoid", "btnConfirm", "btnPost", "btnActivate", "btnDeactivate"};
    public static String[] buttonPackArray2 = {"btnRetrieve", "btnSearch", "btnUndo", "btnArrowRight", "btnArrowLeft", "btnHistory", "btnPrint", "btnRemoveAttachment", "btnAddAttachment"};

    /*Detects if a string is pure number*/
 /*Returns boolean*/
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /*Text reveal anim*/
    public static void runTextReveal(double duration, TextField textField) {
        String fullText = textField.getText();

        textField.setText(""); // hide first

        Timeline timeline = new Timeline();

        for (int i = 0; i <= fullText.length(); i++) {
            final int index = i;

            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(i * duration),
                    e -> textField.setText(fullText.substring(0, index))
            );

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setOnFinished(e -> {
            Platform.runLater(() -> {
                textField.requestFocus();
                textField.positionCaret(textField.getText().length());
            });
        });

        timeline.play();
    }

    /*Gets the first day of Month, returns a Date type*/
    public static Date getFirstDayOfMonth(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // Set to first day of the same month and year
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // Optional: Reset time to start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    //For combobox when basis value are two
    public static class Status {

        private final String code;
        private final String description;

        public Status(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description; // what ComboBox displays
        }
    }

    public static String formatDateToString(Date foDateValue) {
        if (foDateValue == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(foDateValue);
    }

    public static boolean areAllDisabled(Node... nodes) {
        if (nodes == null || nodes.length == 0) {
            return false;
        }
        for (Node node : nodes) {
            if (node == null || !node.isDisable()) {
                return false;
            }
        }
        return true;
    }
}
