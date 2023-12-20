package com.homeRepair.step_definitions;

import com.homeRepair.utilities.ConfigurationReader;
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

public class ProviderFilterViewSendEmailRequests {

    WebDriver driver;
    @Given("a logged-in provider is on the dashboard")
    public void provider_on_logged_in() throws InterruptedException {

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


        //a[.="Are you a service provider?"]
        WebElement serviceProviderButton = driver.findElement(By.xpath("//a[.=\"Are you a service provider?\"]"));
        serviceProviderButton.click();

        WebElement signInEmailProButton = driver.findElement(By.xpath("//input[@placeholder=\"name@company.com\"]"));
        signInEmailProButton.sendKeys(ConfigurationReader.get("pusername"));
        Thread.sleep(1000);
        WebElement signInpasswordProButton = driver.findElement(By.xpath("//input[@name=\"password\"]"));
        signInpasswordProButton.sendKeys("12345678");
        Thread.sleep(1000);


        WebElement signInProviderButton = driver.findElement(By.xpath("//button[.='Sign in']"));
        signInProviderButton.click();
        Thread.sleep(2000);
    }
    @When("the provider applies filters to view specific service requests")
    public void provider_applies_filters() throws InterruptedException {

        WebElement locationProButton = driver.findElement(By.xpath("//input[@placeholder=\"Search by location...\"]"));
        locationProButton.sendKeys("Cream Ridge, Upper Freehold, NJ, USA");
        Thread.sleep(2000);
        Actions keyProDown = new Actions(driver);
        keyProDown.sendKeys(Keys.chord(Keys.DOWN, Keys.RETURN)).perform();
        Thread.sleep(2000);

        WebElement priceProButton = driver.findElement(By.xpath("(//div[@class=\" css-13cymwt-control\"])[2]"));
        Thread.sleep(1000);
        priceProButton.click();
        //Thread.sleep(1000);
        //priceProButton.sendKeys("200");
        Thread.sleep(1000);
        Actions keyProDown1 = new Actions(driver);
        keyProDown1.sendKeys(Keys.chord(Keys.DOWN, Keys.RETURN)).perform();
        Thread.sleep(2000);
    }
    @When("a logged-in provider is viewing a service request")
    public void provider_viewing_requests() throws InterruptedException {

        WebElement openJobButton = driver.findElement(By.xpath("//button[.='Open Job']"));
        openJobButton.click();
        Thread.sleep(2000);


		//driver.quit(); */
    }
    @When("the provider clicks on the \"Request Email\" button")
    public void provider_email_requests() throws InterruptedException {

        WebElement openJobButton = driver.findElement(By.xpath("//button[.='Request Email']"));
        openJobButton.click();
        Thread.sleep(2000);


        //driver.quit(); */
    }
    @Then("the email should be sent successfully")
    public void provider_email_sent() throws InterruptedException {

        WebElement exitButton = driver.findElement(By.xpath("//button[.=\"X\"]"));
        exitButton.click();
        Thread.sleep(2000);


        driver.quit();
    }
}
