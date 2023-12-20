package com.homeRepair.step_definitions;


//import org.openqa.selenium.WebDriver;
import com.homeRepair.utilities.ConfigurationReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
		import org.openqa.selenium.chrome.ChromeOptions;


public class customerRegisterAndLogin {
	WebDriver driver;
	@Given("a user visits the registration page")
	public void user_visits_registration() throws InterruptedException {

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

		WebElement signUpButton = driver.findElement(By.xpath("//a[.='Sign up']"));
		signUpButton.click();

		Thread.sleep(1000);
	}
		@When("the user enters valid registration details")
		public void user_enter_details() throws InterruptedException {

			WebElement firstNameButton = driver.findElement(By.xpath("//input[@placeholder=\"First Name\"]"));
			firstNameButton.sendKeys("Home");
			Thread.sleep(1000);
			WebElement LastNameButton = driver.findElement(By.xpath("//input[@placeholder=\"Last Name\"]"));
			LastNameButton.sendKeys("Repair");
			Thread.sleep(1000);
			WebElement emailButton = driver.findElement(By.xpath("//input[@placeholder=\"Email\"]"));
			emailButton.sendKeys(ConfigurationReader.get("cusername"));
			Thread.sleep(1000);
			WebElement addressButton = driver.findElement(By.xpath("//input[@placeholder=\"Your Location\"]"));
			addressButton.sendKeys("120 Elm Street, Edgewater Park, NJ, USA");
			Thread.sleep(1000);
			addressButton.sendKeys(Keys.DOWN);
			addressButton.sendKeys(Keys.RETURN);

			Thread.sleep(1000);
			WebElement phoneNumberButton = driver.findElement(By.xpath("//input[@placeholder=\"Phone Number\"]"));
			phoneNumberButton.sendKeys("6095006574");
			Thread.sleep(1000);
			WebElement passwordButton = driver.findElement(By.xpath("//input[@placeholder=\"Password\"]"));
			passwordButton.sendKeys("12345678");
			Thread.sleep(1000);
			WebElement confirmPasswordButton = driver.findElement(By.xpath("//input[@placeholder=\"Confirm Password\"]"));
			confirmPasswordButton.sendKeys("12345678");
			Thread.sleep(1000);
		}
	@And("the user submits the registration form")
	public void user_submit_details() throws InterruptedException {

		WebElement signUpCustomerButton = driver.findElement(By.xpath("//button[@type='submit']"));
		signUpCustomerButton.click();
	}

	@Then("the user should be registered successfully")
	public void user_registered() throws InterruptedException {

		Thread.sleep(2000);
		Alert successResponse = driver.switchTo().alert();
		successResponse.accept();

		Thread.sleep(2000);
		driver.quit();

	}

	@Given("a registered user visits the login page")
	public void user_visits_login() throws InterruptedException {

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
	}

	@When("the user enters valid login credentials")
	public void user_enter_login_info() throws InterruptedException {


		WebElement signInEmailButton = driver.findElement(By.xpath("//input[@placeholder=\"Email\"]"));
		signInEmailButton.sendKeys(ConfigurationReader.get("cusername"));
		Thread.sleep(1000);
		WebElement signInpasswordButton = driver.findElement(By.xpath("//input[@placeholder=\"Password\"]"));
		signInpasswordButton.sendKeys("12345678");
		Thread.sleep(1000);


	}

	@Then("the user submits the login form and the user should be logged in successfully")
	public void user_loggedin() throws InterruptedException {

		WebElement createRequestButton = driver.findElement(By.xpath("//button[@type='submit']"));
		createRequestButton.click();

		Thread.sleep(2000);
		WebElement signInEmailButton = driver.findElement(By.xpath("//input[@placeholder=\"Email\"]"));
		signInEmailButton.sendKeys(ConfigurationReader.get("cusername"));
		Thread.sleep(1000);
		WebElement signInpasswordButton = driver.findElement(By.xpath("//input[@placeholder=\"Password\"]"));
		signInpasswordButton.sendKeys("12345678");
		Thread.sleep(2000);

		driver.quit();

	}
}
