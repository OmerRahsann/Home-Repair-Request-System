package com.homeRepair.step_definitions;

import com.homeRepair.utilities.ConfigurationReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

public class customerCreateEditRequest {

    WebDriver driver;
    WebElement createRequestformButton;

    @Given("a logged-in customer is on the dashboard")
    public void user_logged_in() throws InterruptedException {

        // Optional. If not specified, WebDriver searches the PATH for chromedriver.
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\omerr\\IdeaProjects\\Home-Repair-Request-System\\homerepair_automation\\chromedriver.exe");
        ChromeOptions option = new ChromeOptions();
        //String allowedOrigins = "http://52.90.184.85:3000/";
        option.addArguments("--remote-allow-origins=*");


        driver = new ChromeDriver(option);

        driver.get(ConfigurationReader.get("url"));

        driver.manage().window().maximize();

        Thread.sleep(2000);
        WebElement signInButton = driver.findElement(By.xpath("//a[.='Sign In']"));
        signInButton.click();

        Thread.sleep(2000);

        WebElement signInEmailButton = driver.findElement(By.xpath("//input[@placeholder=\"Email\"]"));
        signInEmailButton.sendKeys(ConfigurationReader.get("cusername"));
        Thread.sleep(1000);
        WebElement signInpasswordButton = driver.findElement(By.xpath("//input[@placeholder=\"Password\"]"));
        signInpasswordButton.sendKeys("12345678");
        Thread.sleep(1000);


        WebElement createRequestButton = driver.findElement(By.xpath("//button[@type='submit']"));
        createRequestButton.click();

        Thread.sleep(2000);

    }
    @When("the customer clicks on the \"Create Service Request\" button")
    public void user_clicks_request() throws InterruptedException {

        WebElement signInCustomerButton = driver.findElement(By.xpath("//button[@type='submit']"));
        signInCustomerButton.click();
        Thread.sleep(2000);

    }

    @And("the customer fills in the required details for the service request")
    public void user_fills_request() throws InterruptedException {

        WebElement projectTitleButton = driver.findElement(By.xpath("//form/div/input"));
        projectTitleButton.sendKeys("Home Repair Automation");
        Thread.sleep(1000);

        WebElement projectDescButton = driver.findElement(By.xpath("//input[@placeholder='Enter a location']/../../preceding-sibling::div[1]/textarea"));
        projectDescButton.sendKeys("homerepair application demo");
        Thread.sleep(1000);

        createRequestformButton = driver.findElement(By.xpath("//button[.='SUBMIT REQUEST']"));
        WebElement desiredPriceButton = driver.findElement(By.xpath("//input[@placeholder='ex: 250']"));

        Actions actions = new Actions(driver);
        actions.moveToElement(createRequestformButton).perform();
        Thread.sleep(1000);
        desiredPriceButton.sendKeys("250");

        Thread.sleep(1000);


        WebElement projectCategoryDropdown = driver.findElement(By.xpath("//div[@class=\" css-13cymwt-control\"]"));
        projectCategoryDropdown.click();

        Thread.sleep(2000);

        Actions keycategoryDown = new Actions(driver);
        keycategoryDown.sendKeys(Keys.chord(Keys.DOWN, Keys.DOWN, Keys.ENTER)).perform();
        Thread.sleep(2000);

        WebElement UploadImg = driver.findElement(By.xpath("//input[@name=\"pictures\"]"));
        Thread.sleep(2000);
        UploadImg.sendKeys("C:\\Users\\omerr\\Downloads\\homerepair.png");
        Thread.sleep(4000);
        //C:\Users\omerr\Downloads\robinson.png


        WebElement projectLocationButton = driver.findElement(By.xpath("//input[@placeholder='Enter a location']"));
        projectLocationButton.sendKeys("Cream Ridge, Upper Freehold, NJ, USA");
        Thread.sleep(2000);

        Actions keyDown = new Actions(driver);
        keyDown.sendKeys(Keys.chord(Keys.DOWN, Keys.RETURN)).perform();
        Thread.sleep(2000);
        //actions.click().perform();
        Thread.sleep(2000);

        desiredPriceButton.click();
        Thread.sleep(1000);

    }

    @And("the customer submits the service request form")
    public void user_submit_request() throws InterruptedException {


        //WebElement createRequestformButton = driver.findElement(By.xpath("//button[@type='submit']"));
        createRequestformButton.click();

    }

    @Then("the service request should be created successfully")
    public void user_success_request() throws InterruptedException {

        WebElement exitButton = driver.findElement(By.xpath("//button[.=\"X\"]"));
        exitButton.click();
        Thread.sleep(1000);

        driver.quit();

    }

//--------------------------------------------------------------------------------------------------------------
 @When("the customer clicks on the \"Edit\" button for the service request")
  public void user_clicks_edit() throws InterruptedException {

     WebElement myRequest = driver.findElement(By.xpath("//a[@href=\"/customer/myrequests\"]"));
     myRequest.click();
     Thread.sleep(2000);

     WebElement firstRequestEdit = driver.findElement(By.xpath("(//li[@class=\"slide selected previous\"])[1]"));
     firstRequestEdit.click();
     Thread.sleep(2000);


     WebElement svgEdit = driver.findElement(By.xpath("(//*[name()='svg'])[4]"));
     svgEdit.click();
     Thread.sleep(2000);

 }
    @When("the customer updates the details in the service request form")
    public void user_edit() throws InterruptedException {

        WebElement projectTitleButton1 = driver.findElement(By.xpath("//form/div/input"));
        projectTitleButton1.sendKeys(" Demo");
        Thread.sleep(1000);

        createRequestformButton = driver.findElement(By.xpath("//button[.='SAVE CHANGES']"));
        WebElement desiredPriceButton = driver.findElement(By.xpath("//input[@placeholder='ex: 250']"));

        Actions actions = new Actions(driver);
        actions.moveToElement(createRequestformButton).perform();
        Thread.sleep(1000);
        desiredPriceButton.clear();
        Thread.sleep(1000);
        desiredPriceButton.sendKeys("50");

        Thread.sleep(1000);


        WebElement projectCategoryDropdown = driver.findElement(By.xpath("//div[@class=\" css-13cymwt-control\"]"));
        projectCategoryDropdown.click();

        Thread.sleep(2000);

        Actions keycategoryDown = new Actions(driver);
        keycategoryDown.sendKeys(Keys.chord(Keys.DOWN, Keys.ENTER)).perform();
        Thread.sleep(2000);

    }
    @When("the customer submits the edited service request")
    public void user_edit_submit() throws InterruptedException {
        createRequestformButton.click();
        Thread.sleep(2000);



    }

    @Then("the service request should be updated successfully")
    public void user_complete_edit() throws InterruptedException {

        WebElement exitButton = driver.findElement(By.xpath("//button[.=\"X\"]"));
        exitButton.click();
        Thread.sleep(3000);

        driver.quit();

    }


}
