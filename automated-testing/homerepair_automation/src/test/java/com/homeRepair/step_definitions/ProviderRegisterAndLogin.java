package com.homeRepair.step_definitions;

import com.homeRepair.utilities.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

public class ProviderRegisterAndLogin {

    WebDriver driver;
	WebElement signUpProviderButton;
    @Given("a user visits the provider registration page")
    public void provider_visits_registration() throws InterruptedException {

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

		Thread.sleep(2000);
		WebElement signUpProButton = driver.findElement(By.xpath("//a[.=\"Sign up here!\"]"));
		signUpProButton.click();
		Thread.sleep(2000);
	}
	@When("the provider enters valid registration details")
	public void provider_do_registration() throws InterruptedException {


		WebElement firstNameProButton = driver.findElement(By.xpath("//input[@placeholder=\"Business Name\"]"));
		firstNameProButton.sendKeys("Golden Horn LLC");
		Thread.sleep(1000);

		//Actions actions = new Actions(driver);
		signUpProviderButton = driver.findElement(By.xpath("//button[@type='submit']"));
		Actions actions = new Actions(driver);
		actions.moveToElement(signUpProviderButton).perform();

		WebElement emailProButton = driver.findElement(By.xpath("//input[@placeholder=\"Email\"]"));
		emailProButton.sendKeys(ConfigurationReader.get("pusername"));
		Thread.sleep(1000);
		WebElement addressProButton = driver.findElement(By.xpath("//input[@placeholder=\"Business Location\"]"));
		addressProButton.sendKeys("120 Elm Street, Edgewater Park, NJ, USA");
		Thread.sleep(1000);
		addressProButton.sendKeys(Keys.DOWN);
		addressProButton.sendKeys(Keys.RETURN);

		Thread.sleep(1000);
		WebElement phoneNumberProButton = driver.findElement(By.xpath("//input[@placeholder=\"Phone Number\"]"));
		phoneNumberProButton.sendKeys("6095006574");
		Thread.sleep(1000);
		WebElement descriptionButton = driver.findElement(By.xpath("//textarea[@placeholder=\"Provider Description: Enter a description of your services...\"]"));
		descriptionButton.sendKeys("hello guys");
		Thread.sleep(1000);

		WebElement projectCategoryProDropdown = driver.findElement(By.xpath("//div[@class=\" css-13cymwt-control\"]"));
		projectCategoryProDropdown.click();

		Thread.sleep(2000);

		Actions keycategoryProDown = new Actions(driver);
		keycategoryProDown.sendKeys(Keys.chord(Keys.DOWN, Keys.DOWN, Keys.ENTER)).perform();
		Thread.sleep(2000);

		WebElement passwordProButton = driver.findElement(By.xpath("//input[@placeholder=\"Password\"]"));
		passwordProButton.sendKeys("12345678");
		Thread.sleep(1000);
		WebElement confirmPasswordProButton = driver.findElement(By.xpath("//input[@placeholder=\"Confirm Password\"]"));
		confirmPasswordProButton.sendKeys("12345678");
		Thread.sleep(1000);
	}
	@When("the provider submits the registration form")
	public void provider_submit_registration() throws InterruptedException {

		signUpProviderButton.click();
		Thread.sleep(2000);

	}


	@Then("the provider should be registered successfully")
	public void provider_Complete_registration() throws InterruptedException {
		Alert successProResponse = driver.switchTo().alert();
		successProResponse.accept();

		Thread.sleep(3000);

		driver.quit();
	}

	@Given("a registered provider visits the login page")
	public void provider_visits_login() throws InterruptedException {

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
		Thread.sleep(2000);
	}

		@When("the provider enters valid login credentials")
		public void provider_fill_login() throws InterruptedException {

			WebElement signInEmailProButton = driver.findElement(By.xpath("//input[@placeholder=\"name@company.com\"]"));
			signInEmailProButton.sendKeys(ConfigurationReader.get("pusername"));
			Thread.sleep(1000);
			WebElement signInpasswordProButton = driver.findElement(By.xpath("//input[@name=\"password\"]"));
			signInpasswordProButton.sendKeys("12345678");
			Thread.sleep(1000);

		}

	@When("the provider submits the login form and should be logged in successfully")
	public void provider_submit_login() throws InterruptedException {
		WebElement signInProviderButton = driver.findElement(By.xpath("//button[.='Sign in']"));
		signInProviderButton.click();
		Thread.sleep(2000);



	}

	@When("the provider tries to update their info")
	public void provider_update_info() throws InterruptedException {
		WebElement profile = driver.findElement(By.xpath("(//div[@class=\"flex flex-row \"]/div)"));
		profile.click();
		Thread.sleep(1000);

		WebElement signout = driver.findElement(By.xpath("//a[.='Your Profile']"));
		signout.click();
		Thread.sleep(2000);

		WebElement signinc = driver.findElement(By.xpath("//button[.='Edit Account']"));
		signinc.click();

		Thread.sleep(2000);

		WebElement descriptionButton2 = driver.findElement(By.xpath("//textarea[@placeholder=\"Provider Description: Enter a description of your services...\"]"));
		descriptionButton2.sendKeys("\n" +
				"\"Golden Horn: Responsible for ensuring seamless operations and optimal performance, specializing in [specific role or industry]. Executes tasks with precision and contributes to the overall success of the team/company.\"");
		Thread.sleep(1000);

		WebElement signinc2 = driver.findElement(By.xpath("//button[.='Update']"));
		signinc2.click();

		Thread.sleep(1000);

	}
		@When("the provider should able to update their info successfully")
		public void provider_update_success() throws InterruptedException {
		Alert successProResponse5 = driver.switchTo().alert();
		successProResponse5.accept();

		Thread.sleep(3000);

		driver.quit();

	}
}
