package ph.com.guanzongroup.integsys.views;

import static org.junit.Assert.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.scene.Node;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

public class ProjectControllerTest extends ApplicationTest {

    private Parent root;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ph/com/guanzongroup/integsys/views/Project.fxml"));

        root = loader.load();

        stage.setScene(new Scene(root));
        stage.setTitle("Project Test");
        stage.show();
    }

    @Test
    public void testFXMLLoaded() {
        assertNotNull(root);
    }

    @Test
    public void testButtonsExist() {

        verifyControl("#btnBrowse", Button.class);
        verifyControl("#btnNew", Button.class);
        verifyControl("#btnSave", Button.class);
        verifyControl("#btnUpdate", Button.class);
        verifyControl("#btnConfirm", Button.class);
        verifyControl("#btnVoid", Button.class);
        verifyControl("#btnCancel", Button.class);
        verifyControl("#btnCancelRecord", Button.class);
        verifyControl("#btnClose", Button.class);
        verifyControl("#btnHistory", Button.class);
    }

    @Test
    public void testTextFieldsExist() {

        verifyControl("#txtField01", TextField.class);
        verifyControl("#txtField02", TextField.class);
        verifyControl("#txtSeeks01", TextField.class);
    }

    @Test
    public void testStatusLabelExists() {

        verifyControl("#lblStatus", Label.class);
    }

    @Test
    public void testCanTypeProjectID() {

        clickOn("#txtField01");
        write("PRJ001");

        TextField txt = lookup("#txtField01").query();

        assertEquals("PRJ001", txt.getText());
    }

    @Test
    public void testCanTypeDescription() {

        clickOn("#txtField02");
        write("Payroll System");

        TextField txt = lookup("#txtField02").query();

        assertEquals("Payroll System", txt.getText());
    }

    @Test
    public void testSearchTextbox() {

        clickOn("#txtSeeks01");
        write("Payroll");

        TextField txt = lookup("#txtSeeks01").query();

        assertEquals("Payroll", txt.getText());
    }

    @Test
    public void testButtonsClickable() {

        clickOn("#btnBrowse");
        clickOn("#btnNew");
        clickOn("#btnSave");
        clickOn("#btnUpdate");
        clickOn("#btnConfirm");
        clickOn("#btnVoid");
        clickOn("#btnCancel");
        clickOn("#btnCancelRecord");
        clickOn("#btnHistory");
        clickOn("#btnClose");
    }

    @Test
    public void testTabNavigation() {

        clickOn("#txtField01");
        write("ABC");

        type(javafx.scene.input.KeyCode.TAB);

        assertTrue(lookup("#txtField02").queryAs(TextField.class).isFocused());
    }

    @Test
    public void testWindowDisplayed() {

        assertTrue(root.isVisible());
    }

    private <T extends Node> void verifyControl(String id, Class<T> clazz) {
        T node = lookup(id).queryAs(clazz);
        assertNotNull(node);
    }
}
