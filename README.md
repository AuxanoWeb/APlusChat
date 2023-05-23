# APlusChat
In Project Level gradle

allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

In App Level gradle

dependencies {
	        implementation 'com.github.AuxanoWeb:APlusChat:latestVersion'
	}
	
Use bellow Function for access Activity

CommonUtils.lunchActivityChat(this, Theme.RED) // Change three type of Theme, RED-BLUE-YELLOW

Use bellow Function for access Activity

CommonUtils.lunchFragmentChat(supportFragmentManager,Theme.RED,yourFramLayout) // Change three type of Theme, RED-BLUE-YELLOW

