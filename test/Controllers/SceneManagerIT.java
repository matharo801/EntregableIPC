/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Controllers;

import javafx.stage.Stage;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author The_T
 */
public class SceneManagerIT {
    
    public SceneManagerIT() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of init method, of class SceneManager.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        Stage s = null;
        SceneManager.init(s);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of switchScene method, of class SceneManager.
     */
    @Test
    public void testSwitchScene() {
        System.out.println("switchScene");
        String fxml = "";
        SceneManager.switchScene(fxml);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentUser method, of class SceneManager.
     */
    @Test
    public void testGetCurrentUser() {
        System.out.println("getCurrentUser");
        User expResult = null;
        User result = SceneManager.getCurrentUser();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCurrentUser method, of class SceneManager.
     */
    @Test
    public void testSetCurrentUser() {
        System.out.println("setCurrentUser");
        User user = null;
        SceneManager.setCurrentUser(user);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
