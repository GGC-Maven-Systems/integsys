package ph.com.guanzongroup.integsys.views.child;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class APStatusController implements Initializable {

    @FXML
    private AnchorPane apMainAnchor;
    @FXML
    private Label lblDisplay;
    @FXML
    private Button btnAction;
    @FXML
    private FontAwesomeIconView iconAction;

    private boolean pbIsAddNew = true;   // true = trash/delete behavior
    private boolean pbIsActive = true;   // meaningful only when pbIsAddNew == false
    private boolean pbSelected = false;

    private Runnable poOnRemove;
    private Consumer<Boolean> poOnToggleActive;
    private Runnable poOnSelect;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnAction.setOnAction(e -> {
            e.consume();
            handleAction();
        });
        apMainAnchor.setOnMouseClicked(e -> {
            if (poOnSelect != null) {
                poOnSelect.run();
            }
        });
    }

    public void setDisplayText(String text) {
        lblDisplay.setText(text);
    }

    /**
     * @param isAddNew true  -> record does not exist on file yet (brand-new
     *                        transaction, or item added this session):
     *                        action button behaves as TRASH / hard remove.
     *                 false -> record already persisted: action button
     *                        toggles isWithActive() instead of removing.
     */
    public void setMode(boolean isAddNew) {
        this.pbIsAddNew = isAddNew;
        refreshIcon();
    }

    /** Only relevant when setMode(false) was used. */
    public void setActive(boolean isActive) {
        this.pbIsActive = isActive;
        refreshIcon();
        refreshHighlight();
    }

    private void refreshIcon() {
        if (pbIsAddNew) {
            iconAction.setGlyphName("TRASH");
        } else {
            iconAction.setGlyphName(pbIsActive ? "CHECK" : "CLOSE");
        }
    }

    private void refreshHighlight() {
        if (!pbIsAddNew && !pbIsActive) {
            apMainAnchor.setStyle("-fx-background-color: #FFC0CB; -fx-background-radius: 15;");
        } else {
            apMainAnchor.setStyle("-fx-background-color: #B5B5B5; -fx-background-radius: 15;");
        }
    }

    private void handleAction() {
        if (pbIsAddNew) {
            if (poOnRemove != null) {
                poOnRemove.run();
            }
        } else {
            boolean lbNewState = !pbIsActive;
            setActive(lbNewState);
            if (poOnToggleActive != null) {
                poOnToggleActive.accept(lbNewState);
            }
        }
    }

    public void setOnRemove(Runnable action) {
        this.poOnRemove = action;
    }

    public void setOnToggleActive(Consumer<Boolean> action) {
        this.poOnToggleActive = action;
    }

    public void setOnSelect(Runnable action) {
        this.poOnSelect = action;
    }

    public void setSelected(boolean selected) {
        this.pbSelected = selected;
        refreshHighlight();
        if (selected) {
            apMainAnchor.setStyle(apMainAnchor.getStyle() + " -fx-border-color: #FF8201; -fx-border-width: 2;");
        }
    }
}
