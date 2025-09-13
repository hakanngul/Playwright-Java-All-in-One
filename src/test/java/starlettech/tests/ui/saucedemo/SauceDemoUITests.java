package starlettech.tests.ui.saucedemo;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.starlettech.annotations.Browser;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.BaseTest;
import com.starlettech.enums.BrowserType;
import com.starlettech.enums.TestPriority;
import com.starlettech.testdata.pages.saucedemo.CartPage;
import com.starlettech.testdata.pages.saucedemo.LoginPage;
import com.starlettech.testdata.pages.saucedemo.ProductsPage;
import com.starlettech.testdata.saucedemo.SauceDemoTestData;

/**
 * SauceDemo UI test suite
 * Tests the main functionality of SauceDemo e-commerce application
 */
@Browser(BrowserType.CHROMIUM)
public class SauceDemoUITests extends BaseTest {

    private LoginPage loginPage;
    private ProductsPage productsPage;
    private CartPage cartPage;

    @BeforeMethod
    public void setUp() {
        loginPage = new LoginPage();
        loginPage.navigateToLoginPage();
    }

    @Test
    @TestInfo(
        description = "Test successful login with valid credentials",
        author = "Test Automation",
        priority = TestPriority.HIGH,
        tags = {"smoke", "login", "positive"}
    )
    public void testSuccessfulLogin() {
        // Arrange
        logger.info("Starting successful login test");
        
        // Act
        productsPage = loginPage.login(SauceDemoTestData.STANDARD_USER, SauceDemoTestData.VALID_PASSWORD);
        
        // Wait and verify products page is displayed
        productsPage.waitForPageLoad();
        
        // Assert
        Assert.assertTrue(productsPage.isProductsPageDisplayed(), 
            "Products page should be displayed after successful login");
        
        Assert.assertEquals(productsPage.getPageTitle(), SauceDemoTestData.PRODUCTS_PAGE_TITLE,
            "Page title should be 'Products'");
        
        Assert.assertTrue(productsPage.getProductCount() > 0,
            "Products should be displayed on the page");
        
        logger.info("Successful login test completed successfully");
    }

    @Test
    @TestInfo(
        description = "Test adding products to cart and verifying cart functionality",
        author = "Test Automation", 
        priority = TestPriority.HIGH,
        tags = {"smoke", "cart", "positive", "e2e"}
    )
    public void testAddProductsToCartAndVerifyCart() {
        // Arrange
        logger.info("Starting add products to cart test");
        
        // Login first
        productsPage = loginPage.login(SauceDemoTestData.STANDARD_USER, SauceDemoTestData.VALID_PASSWORD);
        Assert.assertTrue(productsPage.isProductsPageDisplayed(), "Login should be successful");
        
        // Get initial product count
        int totalProducts = productsPage.getProductCount();
        logger.info("Total products available: {}", totalProducts);
        
        // Act - Add multiple products to cart
        String expectedProduct1 = SauceDemoTestData.SAUCE_LABS_BACKPACK;
        String expectedProduct2 = SauceDemoTestData.SAUCE_LABS_BIKE_LIGHT;
        
        // Add first product by name
        productsPage.addProductToCartByName(expectedProduct1);
        Assert.assertEquals(productsPage.getCartItemCount(), 1, 
            "Cart should contain 1 item after adding first product");
        
        // Add second product by index
        productsPage.addProductToCart(1); // Second product in the list
        Assert.assertEquals(productsPage.getCartItemCount(), 2,
            "Cart should contain 2 items after adding second product");
        
        // Navigate to cart
        cartPage = productsPage.clickCartButton();
        
        // Assert - Verify cart page and contents
        Assert.assertTrue(cartPage.isCartPageDisplayed(),
            "Cart page should be displayed");
        
        Assert.assertEquals(cartPage.getPageTitle(), SauceDemoTestData.CART_PAGE_TITLE,
            "Cart page title should be 'Your Cart'");
        
        Assert.assertEquals(cartPage.getCartItemCount(), 2,
            "Cart should contain exactly 2 items");
        
        Assert.assertFalse(cartPage.isCartEmpty(),
            "Cart should not be empty");
        
        // Verify specific product is in cart
        Assert.assertTrue(cartPage.isItemInCart(expectedProduct1),
            "'" + expectedProduct1 + "' should be in the cart");
        
        // Get all cart items and verify
        List<String> cartItems = cartPage.getCartItemNames();
        Assert.assertEquals(cartItems.size(), 2,
            "Cart should contain exactly 2 different products");
        
        logger.info("Cart contains products: {}", cartItems);
        
        // Test remove item functionality
        cartPage.removeItemFromCart(0); // Remove first item
        Assert.assertEquals(cartPage.getCartItemCount(), 1,
            "Cart should contain 1 item after removal");
        
        // Test continue shopping
        productsPage = cartPage.continueShopping();
        // Wait for navigation and verify
        productsPage.waitForPageLoad();
        Assert.assertTrue(productsPage.isProductsPageDisplayed(),
            "Should return to products page after continue shopping");
        
        Assert.assertEquals(productsPage.getCartItemCount(), 1,
            "Cart badge should still show 1 item");
        
        logger.info("Add products to cart test completed successfully");
    }

    @Test
    @TestInfo(
        description = "Test login with locked out user", 
        author = "Test Automation",
        priority = TestPriority.MEDIUM,
        tags = {"negative", "login", "security"}
    )
    public void testLockedOutUserLogin() {
        // Arrange
        logger.info("Starting locked out user login test");
        
        // Act
        loginPage.login(SauceDemoTestData.LOCKED_OUT_USER, SauceDemoTestData.VALID_PASSWORD);
        
        // Assert
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Error message should be displayed for locked out user");
        
        Assert.assertTrue(loginPage.getErrorMessage().contains("locked out"),
            "Error message should mention user being locked out");
        
        Assert.assertTrue(loginPage.isLoginPageDisplayed(),
            "Should remain on login page after failed login");
        
        logger.info("Locked out user login test completed successfully");
    }

    @Test
    @TestInfo(
        description = "Test product sorting functionality",
        author = "Test Automation",
        priority = TestPriority.MEDIUM, 
        tags = {"functional", "sorting", "products"}
    )
    public void testProductSorting() {
        // Arrange
        logger.info("Starting product sorting test");
        
        // Login first
        productsPage = loginPage.login(SauceDemoTestData.STANDARD_USER, SauceDemoTestData.VALID_PASSWORD);
        Assert.assertTrue(productsPage.isProductsPageDisplayed(), "Login should be successful");
        
        // Get initial product list
        List<String> initialProducts = productsPage.getProductNames();
        logger.info("Initial products order: {}", initialProducts);
        
        // Act & Assert - Test Z to A sorting
        productsPage.sortProducts(SauceDemoTestData.SORT_NAME_Z_TO_A);
        List<String> sortedProductsZtoA = productsPage.getProductNames();
        logger.info("Products after Z-A sort: {}", sortedProductsZtoA);
        
        Assert.assertNotEquals(initialProducts, sortedProductsZtoA,
            "Product order should change after sorting");
        
        // Act & Assert - Test A to Z sorting (back to original)
        productsPage.sortProducts(SauceDemoTestData.SORT_NAME_A_TO_Z);
        List<String> sortedProductsAtoZ = productsPage.getProductNames();
        logger.info("Products after A-Z sort: {}", sortedProductsAtoZ);
        
        Assert.assertEquals(initialProducts, sortedProductsAtoZ,
            "Product order should return to original after A-Z sorting");
        
        // Test price sorting
        productsPage.sortProducts(SauceDemoTestData.SORT_PRICE_LOW_TO_HIGH);
        List<String> productsPriceSort = productsPage.getProductNames();
        logger.info("Products after price low-high sort: {}", productsPriceSort);
        
        Assert.assertNotEquals(initialProducts, productsPriceSort,
            "Product order should change after price sorting");
        
        logger.info("Product sorting test completed successfully");
    }

    @Test
    @TestInfo(
        description = "Test complete user workflow: login, add products, view cart, logout",
        author = "Test Automation",
        priority = TestPriority.HIGH,
        tags = {"e2e", "workflow", "smoke", "critical"}
    )
    public void testCompleteUserWorkflow() {
        // Arrange
        logger.info("Starting complete user workflow test");
        
        // Act & Assert - Step 1: Login
        productsPage = loginPage.login(SauceDemoTestData.STANDARD_USER, SauceDemoTestData.VALID_PASSWORD);
        Assert.assertTrue(productsPage.isProductsPageDisplayed(), 
            "Step 1: Login should be successful");
        
        // Step 2: Verify products are displayed
        Assert.assertTrue(productsPage.getProductCount() >= 6,
            "Step 2: At least 6 products should be available");
        
        List<String> availableProducts = productsPage.getProductNames();
        logger.info("Available products: {}", availableProducts);
        
        // Step 3: Add products to cart
        productsPage.addProductToCartByName(SauceDemoTestData.SAUCE_LABS_BACKPACK);
        productsPage.addProductToCartByName(SauceDemoTestData.SAUCE_LABS_FLEECE_JACKET);
        
        Assert.assertEquals(productsPage.getCartItemCount(), 2,
            "Step 3: Cart should contain 2 products");
        
        // Step 4: View cart
        cartPage = productsPage.clickCartButton();
        Assert.assertTrue(cartPage.isCartPageDisplayed(),
            "Step 4: Cart page should be displayed");
        
        Assert.assertEquals(cartPage.getCartItemCount(), 2,
            "Step 4: Cart should display 2 items");
        
        Assert.assertTrue(cartPage.isItemInCart(SauceDemoTestData.SAUCE_LABS_BACKPACK),
            "Step 4: Backpack should be in cart");
        
        Assert.assertTrue(cartPage.isItemInCart(SauceDemoTestData.SAUCE_LABS_FLEECE_JACKET),
            "Step 4: Fleece Jacket should be in cart");
        
        // Step 5: Return to products
        productsPage = cartPage.continueShopping();
        Assert.assertTrue(productsPage.isProductsPageDisplayed(),
            "Step 5: Should return to products page");
        
        // Step 6: Logout
        loginPage = productsPage.logout();
        Assert.assertTrue(loginPage.isLoginPageDisplayed(),
            "Step 6: Should return to login page after logout");
        
        Assert.assertTrue(loginPage.isLogoDisplayed(),
            "Step 6: Login page logo should be displayed");
        
        logger.info("Complete user workflow test completed successfully");
    }
}